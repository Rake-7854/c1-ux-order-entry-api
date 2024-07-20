/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/31/24				N Caceres				CAP-46698					Initial Version
 *	02/21/24				C Codina				CAP-47086					C1UX BE - Order wizard api that will perform search
 *  03/05/24				T Harmon				CAP-47210					Fixed ~~ issue
 *  04/09/24				Krishna Natarajan		CAP-48537					Set isWizard to true for wizard search
 *  04/22/24				Krishna Natarajan		CAP-48856					Removed unnecessary line of code on method performWizardSearch
 */
package com.rrd.c1ux.api.services.catalog.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchRequest;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchResponse;
import com.rrd.c1ux.api.models.catalog.OrderWizardSelectedAttributes;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardAttributeValues;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionRequest;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.admin.locators.SiteAttributeComponentLocatorService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.admin.ao.SiteAssembler;
import com.wallace.atwinxs.admin.vo.AttributeFamilyVOKey;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ISiteAttribute;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardFormBean;
import com.wallace.atwinxs.orderentry.admin.vo.OrderWizardQuestionVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;


@Service
public class OrderWizardServiceImpl extends BaseOEService implements OrderWizardService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderWizardServiceImpl.class);
	
	protected final SiteAttributeComponentLocatorService siteAttributeComponentLocatorService;
	
	protected OrderWizardServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			SiteAttributeComponentLocatorService siteAttributeComponentLocatorService) {
		super(translationService, objectMapFactoryService);
		this.siteAttributeComponentLocatorService = siteAttributeComponentLocatorService;
	}
	
	public OrderWizardQuestionResponse getOrderWizardQuestion(SessionContainer sc, OrderWizardQuestionRequest request)
			throws AccessForbiddenException {
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		OrderWizardQuestionResponse response;
		
		//validations
		isOrderWizardAllowed(appSessionBean, oeSession);
		
		OAOrderWizardAssembler oaasm = new OAOrderWizardAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		OEOrderWizardAssembler oeasm = new OEOrderWizardAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		
		if (AtWinXSConstant.INVALID_ID != request.getAttributeID() && AtWinXSConstant.INVALID_ID != request.getAttributeValueID()
				&& AtWinXSConstant.INVALID_ID != request.getAttributeQuestionID()) {
			response = getNextOrderWizardQuestion(appSessionBean, oeSession, applicationVolatileSession, request, oaasm, oeasm);
		} else {
			response = getMainOrderWizardQuestion(appSessionBean, oeSession, applicationVolatileSession, request, oaasm, oeasm);
		}
				
		return response;
	}
	
	private OrderWizardQuestionResponse getMainOrderWizardQuestion(AppSessionBean appSessionBean, OrderEntrySession oeSession,
			ApplicationVolatileSession applicationVolatileSession, OrderWizardQuestionRequest request, OAOrderWizardAssembler oaasm,
			OEOrderWizardAssembler oeasm) {

		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		if (volatileSessionBean != null && volatileSessionBean.getOrderOnBehalf() != null)
		{
			appSessionBean.setInRequestorMode(volatileSessionBean.getOrderOnBehalf().isInRequestorMode());
		}
		
		//clear the search attributes every time.
		oeSession.getOESessionBean().setPrevWizardSearchAttributes(oeSession.getOESessionBean().getOrderWizardSearchAttributes());
		oeSession.getOESessionBean().setOrderWizardSearchAttributes(null); 
		if (volatileSessionBean != null && volatileSessionBean.getShoppingCartCount() == 0)
		{
	   		OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
	   		try {
	   			assembler.setSkippedEOOAttributeValues(appSessionBean, volatileSessionBean, oeSession.getOESessionBean(), true);
	   	   		volatileSessionBean.setIsDirty(true);
			} 
				
	   		catch (AtWinXSException e)
	   		{
	   			LOGGER.error(this.getClass().getName() + " - " + e.getMessage(),e);
			}
		}
		
		SiteAssembler siteAsm = new SiteAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		
		OrderWizardQuestionResponse response = new OrderWizardQuestionResponse();
		try
		{
			int siteID = appSessionBean.getSiteID();
			int orderWizardID = oeSession.getOESessionBean().getUserSettings().getOrderWizardID();
			// get order wizard (first question)
			OAOrderWizardFormBean owFormBean = oaasm.getOrderWizard(siteID, orderWizardID);
			if (owFormBean.isSinglePath())
			{
				persistInSession(oeSession, applicationVolatileSession, appSessionBean);
				OrderWizardQuestionVO vo = 
						oeasm.getNextQuestion(
						owFormBean.getWizardId(),
						owFormBean.getKeyAttributeId(),
						0);
				request.setAttributeQuestionID(vo.getQuestionAttributeId());
				return getNextOrderWizardQuestion(appSessionBean, oeSession, applicationVolatileSession, request, oaasm, oeasm);
			}
			
			Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes =
					oeasm.getOrderWizardSearchAttributes(
			    		appSessionBean.getSiteID(),	
			    		owFormBean.getWizardFamilyId(),
			    		0, // no questionAttrID
			    		0, // no questionAttrValue
			    		owFormBean.getKeyAttributeId(),
			    		oeasm,
			    		oeSession.getOESessionBean(),
			    		volatileSessionBean,
			    		appSessionBean);
			
			if (volatileSessionBean != null) {
				buildOrderWizardSearchAttributes(volatileSessionBean, owFormBean, orderWizardSearchAttributes);
			}

			Map<String, Object> requestMap = oeasm.getOrderWizardRequestObject(owFormBean, orderWizardSearchAttributes, 
					oeasm, siteAsm, appSessionBean, oeSession);
			List<OrderWizardAttributeValues> attributeValues = new ArrayList<>();

			buildAttributeValues(owFormBean, requestMap, attributeValues);
			
			populateOrderWizardResponse(response, attributeValues, owFormBean.getWizardName(), 
					owFormBean.getKeyAttributeId(), true, owFormBean.getKeyAttributeQuestionText(), false);

			oeSession.getOESessionBean().setOrderWizardSearchAttributes(orderWizardSearchAttributes); 
	    	persistInSession(oeSession, applicationVolatileSession, appSessionBean);
		} 
		catch (AtWinXSException e)
		{
			LOGGER.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
		
		return response;
	
	}

	private void buildOrderWizardSearchAttributes(VolatileSessionBean volatileSessionBean,
			OAOrderWizardFormBean owFormBean, Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes) {
		@SuppressWarnings("unchecked")
		HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttributes = volatileSessionBean.getSelectedSiteAttribute();
		if (selectedSiteAttributes != null) {
			ArrayList<Integer> questionAttrList = new ArrayList<>();
			populateMainQuestionSearchAttributes(owFormBean, orderWizardSearchAttributes, selectedSiteAttributes, questionAttrList);
		}
	}

	private void populateMainQuestionSearchAttributes(OAOrderWizardFormBean owFormBean,
			Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes,
			HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttributes, ArrayList<Integer> questionAttrList) {
		SiteAttrValuesVO[] eooValues;
		if (selectedSiteAttributes.containsKey(owFormBean.getKeyAttributeId())) {
			eooValues = selectedSiteAttributes.get(owFormBean.getKeyAttributeId());
			if (eooValues.length > 0) {
				for (SiteAttrValuesVO savVO : eooValues) {
					questionAttrList.add(savVO.getAttrValID());
				}
				orderWizardSearchAttributes.put(owFormBean.getKeyAttributeId(), questionAttrList);
			}
		} else {
			for (Entry<Integer, SiteAttrValuesVO[]> entry : selectedSiteAttributes.entrySet()) {
				ArrayList<Integer> values = new ArrayList<>();
				for (SiteAttrValuesVO vals : entry.getValue()) {
					values.add(vals.getAttrValID());
					orderWizardSearchAttributes.put(entry.getKey(), values);
				}
			}
		}
	}

	private void buildAttributeValues(OAOrderWizardFormBean owFormBean, Map<String, Object> requestMap,
			List<OrderWizardAttributeValues> attributeValues) {
		if (requestMap.get("keyAttrValues") != null)
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> questionValues = (Map<Integer, String>) requestMap.get("keyAttrValues");
			questionValues.forEach((key, value) ->	populateAttributeValues(attributeValues, key, value, 
					owFormBean.getKeyAttributeId(), AtWinXSConstant.INVALID_ID, false));
		}
		else if (requestMap.get("keyattrvaldesc") != null)
		{
			populateAttributeValues(attributeValues, (int) requestMap.get("keyattrvalid"), (String) requestMap.get("keyattrvaldesc"), 
					owFormBean.getKeyAttributeId(), AtWinXSConstant.INVALID_ID, true);
		}
	}
	
	private OrderWizardQuestionResponse getNextOrderWizardQuestion(AppSessionBean appSessionBean, OrderEntrySession oeSession,
			ApplicationVolatileSession applicationVolatileSession, OrderWizardQuestionRequest request, OAOrderWizardAssembler oaasm,
			OEOrderWizardAssembler oeasm) {

		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderWizardQuestionResponse response = new OrderWizardQuestionResponse();
		if (volatileSessionBean!=null && volatileSessionBean.getOrderOnBehalf()!=null)
		{
			appSessionBean.setInRequestorMode(volatileSessionBean.getOrderOnBehalf().isInRequestorMode());
		}
		
		Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes;
		try
		{
			int siteID = appSessionBean.getSiteID();
			int orderWizardID = oeSession.getOESessionBean().getUserSettings().getOrderWizardID();
			// get order wizard (first question)
			OAOrderWizardFormBean owFormBean = oaasm.getOrderWizard(siteID, orderWizardID);
			int wizardFamilyID = owFormBean.getWizardFamilyId();
			
			if (owFormBean.getKeyAttributeId() == request.getAttributeID() &&
					isAttributeIdValid(appSessionBean.getCustomToken(), siteID, orderWizardID, 
							wizardFamilyID, request.getAttributeID(), request.getAttributeQuestionID(), request.getAttributeValueID())) {
				
				orderWizardSearchAttributes = oeasm.getOrderWizardSearchAttributes(siteID,
		    										wizardFamilyID,
		    										request.getAttributeQuestionID(),
		    										request.getAttributeValueID(),
		    										request.getAttributeID(),
		    										oeasm,
		    										oeSessionBean,
		    										volatileSessionBean,
		    										appSessionBean);
				
				oeSession.getOESessionBean().setOrderWizardSearchAttributes(orderWizardSearchAttributes);
				
				if (request.getWizardQuestionID() != AtWinXSConstant.INVALID_ID) {
					request.setAttributeValueID(orderWizardSearchAttributes.get(owFormBean.getKeyAttributeId()).get(0));
				} else {
					request.setWizardQuestionID(request.getAttributeID());
				}
				
				OrderWizardQuestionVO owqVO = oeasm.getNextQuestion(orderWizardID, request.getAttributeValueID(), request.getWizardQuestionID());
				
		    	if (owqVO != null) //We have a next question	
		    	{
		    		if (volatileSessionBean != null) {
						populateOrderWizardSearchAttributes(volatileSessionBean, orderWizardSearchAttributes, owqVO); 
					}
		    		List<OrderWizardAttributeValues> attributeValues = populateNextQuestionAttributeValues(appSessionBean,
							oeSessionBean, oeasm, orderWizardSearchAttributes, siteID, wizardFamilyID, owqVO);
		    		populateOrderWizardResponse(response, attributeValues, 
		    				owFormBean.getWizardName(), request.getAttributeID(), true, owqVO.getQuestionText(), false);
		    	} else {
		    		populateOrderWizardResponse(response, null, AtWinXSConstant.EMPTY_STRING, 
		    				AtWinXSConstant.INVALID_ID, false, AtWinXSConstant.EMPTY_STRING, true);
		    	}
		    	persistInSession(oeSession, applicationVolatileSession, appSessionBean);
			} else {
				response.setSuccess(false);
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.ORDER_WIZARD_ERROR_MSG));
			}
		} 
		catch (AtWinXSException e)
		{
			LOGGER.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
    	return response;
	}

	private List<OrderWizardAttributeValues> populateNextQuestionAttributeValues(AppSessionBean appSessionBean,
			OEOrderSessionBean oeSessionBean, OEOrderWizardAssembler wizasm,
			Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes, int siteID, int wizardFamilyID,
			OrderWizardQuestionVO owqVO) throws AtWinXSException {
		@SuppressWarnings("unchecked")
		Map<Integer,String> questionValues = wizasm.getOrderWizardNextValues(owqVO, orderWizardSearchAttributes, siteID, wizardFamilyID, 
				appSessionBean.getProfileAttributes(), oeSessionBean);
		List<OrderWizardAttributeValues> attributeValues = new ArrayList<>();
		
		if (questionValues.size() == 2)
		{
			questionValues.forEach((key, value) -> {
				if (key > 0) {
					populateAttributeValues(attributeValues, key, value, 
							owqVO.getQuestionAttributeId(), owqVO.getOrderWizardQuestionId(), true);
				}
			});
		}
		else
		{
			questionValues.forEach((key, value) -> populateAttributeValues(attributeValues, key, value, 
					owqVO.getQuestionAttributeId(), owqVO.getOrderWizardQuestionId(), false));
		}
		return attributeValues;
	}

	private void populateOrderWizardSearchAttributes(VolatileSessionBean volatileSessionBean,
			Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes, OrderWizardQuestionVO owqVO) {
		@SuppressWarnings("unchecked")
		HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttributes = volatileSessionBean.getSelectedSiteAttribute();
		if (selectedSiteAttributes != null) {
			populateNextQuestionSearchAttributes(volatileSessionBean, orderWizardSearchAttributes, owqVO, selectedSiteAttributes);
		}
	}

	private void populateNextQuestionSearchAttributes(VolatileSessionBean volatileSessionBean,
			Map<Integer, ArrayList<Integer>> orderWizardSearchAttributes, OrderWizardQuestionVO owqVO,
			HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttributes) {
		SiteAttrValuesVO[] eooValues;
		ArrayList<Integer> questionAttrList = new ArrayList<>();
		if (selectedSiteAttributes.containsKey(owqVO.getQuestionAttributeId())) {
			eooValues = selectedSiteAttributes.get(owqVO.getQuestionAttributeId());
			if (eooValues.length > 0) {
				for (SiteAttrValuesVO savVO : eooValues) {
					questionAttrList.add(savVO.getAttrValID());
				}
				orderWizardSearchAttributes.put(owqVO.getQuestionAttributeId(), questionAttrList);
			}
		} else if (volatileSessionBean.getShoppingCartCount() > 0) {
			for (Entry<Integer, SiteAttrValuesVO[]> entry : selectedSiteAttributes.entrySet()) {
				ArrayList<Integer> values = new ArrayList<>();
				for (SiteAttrValuesVO vals : entry.getValue()) {
					values.add(vals.getAttrValID());
					orderWizardSearchAttributes.put(entry.getKey(), values);
				}
			}
		}
	}
	
	private void persistInSession(OrderEntrySession oeSession, ApplicationVolatileSession volatileSession, AppSessionBean appSessionBean) throws AtWinXSException {
		SessionHandler.saveSession(volatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);

	}

	@Override
	public OrderWizardSearchResponse performWizardSearch(SessionContainer sc, OrderWizardSearchRequest request)
			throws AtWinXSException {
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		HashMap<Integer, ArrayList<Integer>> searchAttributes = new HashMap<>();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();

		OrderWizardSearchResponse response = new OrderWizardSearchResponse();
		response.setSuccess(true);

		boolean isWizardSearch = userSettings.isAllowOrderWizard();

		CatalogSearchResultsCriteria searchCriteria = new CatalogSearchResultsCriteria();
		oeOrderSessionBean.setSearchCriteria(searchCriteria);
		oeOrderSessionBean.getSearchCriteriaBean().clearStandardAttributes();

		if (!isWizardSearch) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try { // CAP-14294 - need to save the session values
			oeOrderSessionBean.setContinueShopping(false);
			populateSearchAttributes(request.getAttributeValues(), searchAttributes, sc,
					appSessionBean.getCustomToken(), response);
			if (oeOrderSessionBean != null)
			{
				oeOrderSessionBean.setPrevWizardSearchAttributes(oeOrderSessionBean.getOrderWizardSearchAttributes());
			}
			oeOrderSessionBean.setWizard(true);//CAP-48537
			
			oeOrderSessionBean.getSearchCriteriaBean().setAttributesCriteria(searchAttributes);
			oeSession.setOESessionBean(oeOrderSessionBean);

			SessionHandler.persistServiceInSession(oeSession, appSessionBean.getEncodedSessionId(),
					AtWinXSConstant.ORDERS_SERVICE_ID);
		} catch (CPRPCException e) {
			setErrorMessage(appSessionBean, response);
			LOGGER.error(this.getClass().getName() + " - " + e.getMessage(), e);
		}
			
		return response;

	}

	private void setErrorMessage(AppSessionBean appSessionBean, OrderWizardSearchResponse response) {
		response.setSuccess(false);
		response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.INLIVAD_WIZARD_SEARCH_LBL,
				SFTranslationTextConstants.INLIVAD_WIZARD_SEARCH_VAL));
	}

	protected void populateSearchAttributes(List<OrderWizardSelectedAttributes> selectedAttributes,
			HashMap<Integer, ArrayList<Integer>> searchAttributes, SessionContainer sc, CustomizationToken token,
			OrderWizardSearchResponse response) throws AtWinXSException {

		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();

		OAOrderWizardAssembler oaasm = new OAOrderWizardAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());

		int orderWizardID = oeSession.getOESessionBean().getUserSettings().getOrderWizardID();
		int siteID = appSessionBean.getSiteID();

		OAOrderWizardFormBean owFormBean = oaasm.getOrderWizard(siteID, orderWizardID);
		int keyAttributeID = owFormBean.getKeyAttributeId();
		int familyID = owFormBean.getWizardFamilyId();

		if (null != selectedAttributes && !selectedAttributes.isEmpty()) {
			ISiteAttribute siteAttr = siteAttributeComponentLocatorService.locate(token);
			Map<Integer, String> familyAttrs = siteAttr.getAttributesForFamily(siteID, orderWizardID, familyID,
					keyAttributeID, false);
			AttributeFamilyVOKey key = new AttributeFamilyVOKey(siteID, familyID);
			Map<Integer, Collection<SiteAttrValuesVO>> siteAttrValueVOList = siteAttr.getAttrValuesPerFamily(key,
					new HashMap<>());
			List<Integer> attrIds = selectedAttributes.stream().map(OrderWizardSelectedAttributes::getAttributeID)
					.distinct().collect(Collectors.toList());

			processAttribute(selectedAttributes, searchAttributes, response, appSessionBean, familyAttrs,
					siteAttrValueVOList, attrIds);
		}
	}

	private void processAttribute(List<OrderWizardSelectedAttributes> selectedAttributes,
			HashMap<Integer, ArrayList<Integer>> searchAttributes, OrderWizardSearchResponse response,
			AppSessionBean appSessionBean, Map<Integer, String> familyAttrs,
			Map<Integer, Collection<SiteAttrValuesVO>> siteAttrValueVOList, List<Integer> attrIds) {
		for (Integer attrId : attrIds) {
			if (familyAttrs.containsKey(attrId)) {
				Collection<SiteAttrValuesVO> attrValueList = siteAttrValueVOList.get(attrId);
				List<OrderWizardSelectedAttributes> filteredAttrValIds = selectedAttributes.stream()
						.filter(attrValId -> attrValId.getAttributeID() == attrId).collect(Collectors.toList());
				if (null != filteredAttrValIds && !filteredAttrValIds.isEmpty()) {
					ArrayList<Integer> attrValueIds = getAttrValueIds(response, appSessionBean, attrValueList,
							filteredAttrValIds);
					searchAttributes.put(attrId, attrValueIds);
				}
			} else {
				setErrorMessage(appSessionBean, response);
				break;
			}
		}
	}

	private ArrayList<Integer> getAttrValueIds(OrderWizardSearchResponse response, AppSessionBean appSessionBean,
			Collection<SiteAttrValuesVO> attrValueList, List<OrderWizardSelectedAttributes> filteredAttrValIds) {
		ArrayList<Integer> attrValueIds = new ArrayList<>();
		for (OrderWizardSelectedAttributes selectedAttrValId : filteredAttrValIds) {
			if (attrValueList.stream()
					.anyMatch(attrValue -> attrValue.getAttrValID() == selectedAttrValId.getAttributeValueID())) {
				attrValueIds.add(selectedAttrValId.getAttributeValueID());

			} else {
				setErrorMessage(appSessionBean, response);
				break;
			}
		}
		return attrValueIds;
	}

	
	private void isOrderWizardAllowed(AppSessionBean appSessionBean, OrderEntrySession oeSession) throws AccessForbiddenException {
		if (!oeSession.getOESessionBean().getUserSettings().isAllowOrderWizard()) {
			LOGGER.error(getErrorPrefix(appSessionBean), " is not allowed to access this service.");
			throw new AccessForbiddenException(this.getClass().getName());
		}
	}
	
	private void populateOrderWizardResponse(OrderWizardQuestionResponse response, List<OrderWizardAttributeValues> attributeValues,
			String wizardName, int attributeID, boolean isKeyAttribute, String attributeQuestion, boolean noMoreQuestion) {
		response.setAttributeValues(attributeValues);
		response.setName(wizardName);
		response.setAttributeID(attributeID);
		response.setKeyAttribute(isKeyAttribute);
		response.setAttributeQuestion(attributeQuestion);
		response.setNoMoreQuestion(noMoreQuestion);
		response.setSuccess(true);
	}
	
	private void populateAttributeValues(List<OrderWizardAttributeValues> attributeValues,
			int attributeID, String attributeValue, int questionAttributeID, int wizardQuestionID, boolean isSelected) {
		OrderWizardAttributeValues owAttributes = new OrderWizardAttributeValues();
		owAttributes.setAttributeValueID(attributeID);		
		if (attributeValue.contains("~~")) {
			isSelected = true;
			attributeValue = Util.replace(attributeValue, "~~", "");  // CAP-47210
		}
		
		owAttributes.setAttributeValueDescription(attributeValue);
		owAttributes.setAttributeQuestionID(questionAttributeID);
		owAttributes.setWizardQuestionID(wizardQuestionID);		
		owAttributes.setSelected(isSelected);
		attributeValues.add(owAttributes);
	}
	
	private boolean isAttributeIdValid(CustomizationToken token, int siteID, int orderWizardID,
			int familyID, int keyAttributeID, int attributeQuestionID, int attributeValueID) throws AtWinXSException {
		ISiteAttribute siteAttr = siteAttributeComponentLocatorService.locate(token);
        Map<Integer, String> familyAttrs = siteAttr.getAttributesForFamily(siteID, orderWizardID, familyID,
                keyAttributeID, false);
        AttributeFamilyVOKey key = new AttributeFamilyVOKey(siteID, familyID);
        Map<Integer, Collection<SiteAttrValuesVO>> siteAttrValueVOList = siteAttr.getAttrValuesPerFamily(key,
                new HashMap<>());
        if (familyAttrs.containsKey(attributeQuestionID)) {
			Collection<SiteAttrValuesVO> attrValueList = siteAttrValueVOList.get(attributeQuestionID);
			return attrValueList.stream().anyMatch(attribute -> attribute.getAttrValID() == attributeValueID);
        }
        
        return false;
	}
	
}
