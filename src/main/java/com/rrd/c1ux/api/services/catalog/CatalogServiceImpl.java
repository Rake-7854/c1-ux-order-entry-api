/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/18/24				C Codina 				CAP-46379					C1UX BE - Method to retrieve Attribute Filters for Order Entry
 *	01/18/24				S Ramachandran			CAP-46304					Retrieve standard options to use for the Catalog Page
 *	01/26/24				L De Leon				CAP-46322					Added methods for toggling attribute values
 *	01/30/24				Krishna Natarajan		CAP-46821					Set the standard attribute header label
 *	02/05/24				M Sakthi				CAP-46865					C1UX BE - Modify Attribute Filtering - add reset/clear all selection
 *  02/14/24				T Harmon				CAP-46323					Fixed issue with standard attributes
 *  02/16/24				Krishna Natarajan		CAP-46323					Fixed issue with standard attributes for clearAll Flag block
 *  02/22/24				Krishna Natarajan		CAP-47345					Added logic to update flag to indicate Allow Order Wizard
 *  02/22/24				Krishna Natarajan		CAP-47356					Added logic to update flag to indicate Allow Refine Search
 *  02/28/24				Krishna Natarajan		CAP-47543					Added a line of code logic to set unified search to false while clearing attributes
 *  03/07/24				T Harmon				CAP-46723					Remove attribute filters if wizard search	
 *  04/10/24				A Salcedo				CAP-48074					Added order wizard filters.		
 */

package com.rrd.c1ux.api.services.catalog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeRequest;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeResponse;
import com.rrd.c1ux.api.models.catalog.CatalogAttributeValues;
import com.rrd.c1ux.api.models.catalog.CatalogAttributes;
import com.rrd.c1ux.api.models.catalog.StandardAttributesC1UX;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.catalog.entity.FeaturedSearchCriteria;
import com.rrd.custompoint.gwt.catalog.entity.OrderWizardFilters;
import com.rrd.custompoint.gwt.catalog.entity.StandardAttributes;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.alerts.util.AlertCountResponseBean;
import com.wallace.atwinxs.alerts.util.AlertCounts;
import com.wallace.atwinxs.catalogs.util.CatalogSearchFeaturesFavoritesBean;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.admin.ao.OAOrderWizardFormBean;
import com.wallace.atwinxs.orderentry.ao.DynamicItemAttributeVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderWizardAssembler;
import com.wallace.atwinxs.orderentry.servlet.ComplexAttributeBaseJSP;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class CatalogServiceImpl extends BaseOEService implements CatalogService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CatalogServiceImpl.class);

	private SessionHandlerService sessionHandlerService;

	protected CatalogServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			SessionHandlerService sessionHandlerService) {
		super(translationService, objectMapFactoryService);
		this.sessionHandlerService = sessionHandlerService;
	}

	@Override
	public CatalogAttributeResponse getCatalogAttributes(SessionContainer sc, CatalogAttributeRequest request)
			throws AtWinXSException, IllegalAccessException, InvocationTargetException, CPRPCException {

		ApplicationSession appSession = sc.getApplicationSession();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();

		CatalogAttributeResponse response = new CatalogAttributeResponse();
		response.setSuccess(true);

		List<CatalogAttributes> attributeListOptionList = new ArrayList<>();
		StandardAttributesC1UX standardAttributesC1UX = new StandardAttributesC1UX();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		if (oeSessionBean.getUsrSrchOptions() != null && !oeSessionBean.getUsrSrchOptions().isEmpty()) {
			
			//CAP-46865
			OEItemSearchCriteriaSessionBean criteria=oeSessionBean.getSearchCriteriaBean();
			// CAP-46323 TH - Added to initialize standard attributes
			if (criteria.getStandardAttributes() == null)
			{
				standardAttributesC1UX = getStandardAttributeList(appSessionBean, oeSession);
			}
			
			if(request.isClearAll()) {
				criteria.clearStandardAttributes();
				criteria.setStandardAttributes(null);//CAP-46323
				getStandardAttributeList(appSessionBean, oeSession); // CAP-46323 TH
				criteria.setAttributesCriteria(null);
				criteria.setUnifiedSearch(false);//CAP-47543
			}else {
			// CAP-46322
			toggleSelectedAttributeValue(request, oeSession, appSessionBean, volatileSessionBean, response);
			}

			if (response.isSuccess()) {
				try {
					attributeListOptionList = convertList(
							getItemAttributeListOptionsVO(appSessionBean, volatileSessionBean, oeSession),
							oeSessionBean);
					standardAttributesC1UX = getStandardAttributeList(appSessionBean, oeSession);
				} catch (AtWinXSException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
		
		// CAP-46723 TH - Fixed issue with removing filters if order wizard is on
		// Create a method to remove wizard values here
		if (request.isWizard())
		{
			//CAP-48074
			OrderWizardFilters owFilters = getWizardFilters(sc);
			attributeListOptionList = processOrderWizardFilters(owFilters, oeSessionBean.getSearchCriteriaBean().getAttributesCriteria());
		}
		
		response.setCatalogAttributes(attributeListOptionList);

		// CAP-46304 - Get StandardAttributes
		response.setStandardAttributes(standardAttributesC1UX);
		response.setStandardAttributeHeaderLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "lblStandardAttr"));// CAP-46821

		//CAP-47345
		OEResolvedUserSettingsSessionBean userSettings = oeSession.getOESessionBean().getUserSettings();
		boolean isWizardSearch = userSettings.isAllowOrderWizard();
		response.setHasWizard(isWizardSearch);
		//CAP-47345
		
		//CAP-47356
		boolean hasRefine = userSettings.isAllowRefineWizardSearch();
		response.setHasRefine(hasRefine && isWizardSearch);
		//CAP-47356
		
		return response;
	}
	
	//CAP-48074
	protected OrderWizardFilters getWizardFilters(SessionContainer sc) 
	{
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderWizardFilters orderWizardFilters = new OrderWizardFilters();
		
		try
		{
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeOrderSession = oeSession.getOESessionBean();
			
			OAOrderWizardAssembler oaasm = new OAOrderWizardAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
			OEOrderWizardAssembler oeasm = new OEOrderWizardAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
			
			OAOrderWizardFormBean wizbean =  oaasm.getOrderWizard(appSessionBean.getSiteID(), oeOrderSession.getUserSettings().getOrderWizardID());

			//CAP-3418
			int keyAttrID = -1;
			int keyAttrValID = -1;
			if (!wizbean.isSinglePath())
			{
				keyAttrID = wizbean.getKeyAttributeId();
			}
			
			if (oeOrderSession.getOrderWizardSearchAttributes() != null)
			{
				keyAttrValID = oeOrderSession.getOrderWizardSearchAttributes().get(wizbean.getKeyAttributeId()).get(0);
				
				Map<String, Map<String,String>> wizardFilters =  oeasm.getOrderWizardFilters(appSessionBean.getSiteID(), 
				                                                                             wizbean.getWizardId(),
				                                                                             wizbean.getWizardFamilyId(),
				                                                                             keyAttrID, //CAP-3418
				                                                                 			 keyAttrValID,
				                                                                             oeOrderSession.getOrderWizardSearchAttributes(),
				                                                                             appSessionBean.getProfileAttributes(),
				                                                                             oeSession.getOESessionBean().getUsrSrchOptions());
				orderWizardFilters.setWizardFilters(wizardFilters);
			}
		}
		catch(AtWinXSException e)
		{
			LOGGER.error(e.getMessage());
		}
		
		return orderWizardFilters;
	}
	
	//CAP-48074
	protected List<CatalogAttributes> processOrderWizardFilters(OrderWizardFilters owFilters, Map<Integer, List<Integer>> attributesCriteria)
	{
		List<CatalogAttributes> attributeListOptionList = new ArrayList<>();
		
		for(var filters : owFilters.getWizardFilters().entrySet())
		{
			CatalogAttributes catalogAttributes = new CatalogAttributes();
			catalogAttributes.setAttributeDisplayName(filters.getKey());
			catalogAttributes.setMultiSelect(true);
			
			List<CatalogAttributeValues> listCatalogAttributeValues = new ArrayList<>();
			
			for(var filterValues : filters.getValue().entrySet())
			{
				String parseAttrIDs = filterValues.getKey();
				String[] arrAttrIDs = parseAttrIDs.split("\\~");
				
				CatalogAttributeValues catalogAttributeValues = new CatalogAttributeValues();
				catalogAttributeValues.setAttributeID(Integer.parseInt(arrAttrIDs[0]));
				catalogAttributeValues.setAttributeValueID(Integer.parseInt(arrAttrIDs[1]));
				catalogAttributeValues.setAttributeValueDisplay(filterValues.getValue());
				
				boolean isSelected = false;
				if (null != attributesCriteria && !attributesCriteria.isEmpty()) 
				{
					List<Integer> list = attributesCriteria.get(Integer.parseInt(arrAttrIDs[0]));
					isSelected = null != list && !list.isEmpty()
							&& list.stream().anyMatch(value -> value.equals(Integer.parseInt(arrAttrIDs[1])));
				}
				
				catalogAttributeValues.setSelected(isSelected);
				
				//CAP-48074 Do not add "Please Select" attribute value ID 0
				if(catalogAttributeValues.getAttributeValueID() > 0)
				{
					listCatalogAttributeValues.add(catalogAttributeValues);
				}
				
				catalogAttributes.setAttributeID(Integer.parseInt(arrAttrIDs[0]));
			}
			
			catalogAttributes.setCatalogAttributeValues(listCatalogAttributeValues);
			attributeListOptionList.add(catalogAttributes);
		}
		return attributeListOptionList;
	}

	// CAP-46322
	protected void toggleSelectedAttributeValue(CatalogAttributeRequest request, OrderEntrySession oeSession,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, CatalogAttributeResponse response) {

		OEItemSearchCriteriaSessionBean criteria = oeSession.getOESessionBean().getSearchCriteriaBean();

		int selectedAttrID = request.getSelectedAttributeID();
		int selectedAttrValueID = request.getSelectedAttributeValueID();

		try {

			if (selectedAttrID == AtWinXSConstant.INVALID_ID && selectedAttrValueID == AtWinXSConstant.INVALID_ID) {
				// check standard options
				validateAndToggleStandardAttributesOption(appSessionBean, request, response, criteria);
			} else if (selectedAttrID > AtWinXSConstant.INVALID_ID) {
				validateAndToggleSelectedAttributeValue(appSessionBean, oeSession, volatileSessionBean, response,
						criteria, selectedAttrID, selectedAttrValueID);
			} else {
				setFailureAndInvalidAttributeValueMessage(appSessionBean, response);
			}

			oeSession.getOESessionBean().setSearchCriteriaBean(criteria);
			sessionHandlerService.saveFullSessionInfo(oeSession, appSessionBean.getSessionID(),
					AtWinXSConstant.ORDERS_SERVICE_ID);
		} catch (AtWinXSException e) {
			LOGGER.error(e.getMessage());
		}
	}

	// CAP-46322
	protected void validateAndToggleSelectedAttributeValue(AppSessionBean appSessionBean, OrderEntrySession oeSession,
			VolatileSessionBean volatileSessionBean, CatalogAttributeResponse response,
			OEItemSearchCriteriaSessionBean criteria, int selectedAttrID, int selectedAttrValueID)
			throws AtWinXSException {
		boolean isSelectedValueValid = false;
		boolean isMultiSelect = false;
		if (oeSession.getOESessionBean().getUsrSrchOptions().stream()
				.anyMatch(attr -> selectedAttrID == attr.getAttrID())) {
			List<DynamicItemAttributeVO> attributeList = getItemAttributeListOptionsVO(appSessionBean,
					volatileSessionBean, oeSession);
			if (null != attributeList && !attributeList.isEmpty()) {
				DynamicItemAttributeVO attributeVO = attributeList.stream()
						.filter(attr -> selectedAttrID == attr.getSearchOptions().getAttrID()).findAny().orElse(null);
				if (null != attributeVO) {
					isMultiSelect = (attributeVO.getSearchOptions().getSearchCatalogInd() == 4
							|| attributeVO.getSearchOptions().getSearchCatalogInd() == 5);
					List<SiteAttrValuesVO> atrrValueList = attributeVO.getSiteAttrValueVOList();
					isSelectedValueValid = (!isMultiSelect && selectedAttrValueID == AtWinXSConstant.INVALID_ID)
							|| (null != atrrValueList && !atrrValueList.isEmpty()
									&& atrrValueList.stream()
											.anyMatch(attrValue -> (selectedAttrID == attrValue.getAttrID()
													&& selectedAttrValueID == attrValue.getAttrValID())));
				}
			}
			toggleSelectedAttributeValue(criteria, selectedAttrID, selectedAttrValueID, isSelectedValueValid,
					isMultiSelect);
		}

		if (!isSelectedValueValid) {
			setFailureAndInvalidAttributeValueMessage(appSessionBean, response);
		}
	}

	// CAP-46322
	protected void toggleSelectedAttributeValue(OEItemSearchCriteriaSessionBean criteria, int selectedAttrID,
			int selectedAttrValueID, boolean isSelectedValueValid, boolean isMultiSelect) {
		if (isSelectedValueValid) {
			HashMap<Integer, List<Integer>> attributesCriteriaMap = criteria.getAttributesCriteria();
			if (null != attributesCriteriaMap && !attributesCriteriaMap.isEmpty()) {
				List<Integer> attributesCriterialist = attributesCriteriaMap.get(selectedAttrID);
				if (null != attributesCriterialist && !attributesCriterialist.isEmpty()) {
					buildAttributesCriteriaList(selectedAttrValueID, isMultiSelect, attributesCriterialist);
					setAttributesMapToCriteriaSessionBean(criteria, attributesCriteriaMap, selectedAttrID,
							attributesCriterialist);
				} else {
					putNewAttributeCriteriaListToMap(criteria, attributesCriteriaMap, selectedAttrID,
							selectedAttrValueID);
				}
			} else {
				attributesCriteriaMap = new HashMap<>();
				putNewAttributeCriteriaListToMap(criteria, attributesCriteriaMap, selectedAttrID, selectedAttrValueID);
			}
		}
	}

	// CAP-46322
	protected void buildAttributesCriteriaList(int selectedAttrValueID, boolean isMultiSelect,
			List<Integer> attributesCriterialist) {
		if (!isMultiSelect) {
			attributesCriterialist.clear();
			if (selectedAttrValueID > AtWinXSConstant.INVALID_ID) {
				attributesCriterialist.add(selectedAttrValueID);
			}
		} else if (attributesCriterialist.contains(selectedAttrValueID)) {
			attributesCriterialist.remove(Integer.valueOf(selectedAttrValueID));
		} else {
			attributesCriterialist.add(selectedAttrValueID);
		}
	}

	// CAP-46322
	protected void putNewAttributeCriteriaListToMap(OEItemSearchCriteriaSessionBean criteria,
			HashMap<Integer, List<Integer>> attributesCriteriaMap, int selectedAttrID, int selectedAttrValueID) {
		List<Integer> attributesCriterialist = new ArrayList<>();
		attributesCriterialist.add(selectedAttrValueID);
		setAttributesMapToCriteriaSessionBean(criteria, attributesCriteriaMap, selectedAttrID, attributesCriterialist);
	}

	// CAP-46322
	protected void setAttributesMapToCriteriaSessionBean(OEItemSearchCriteriaSessionBean criteria,
			HashMap<Integer, List<Integer>> attributesCriteriaMap, int selectedAttrID,
			List<Integer> attributesCriterialist) {

		if (attributesCriterialist.isEmpty()) {
			attributesCriteriaMap.remove(selectedAttrID);
		} else {
			attributesCriteriaMap.put(selectedAttrID, attributesCriterialist);
		}
		criteria.setAttributesCriteria(attributesCriteriaMap);
	}

	// CAP-46322
	protected void validateAndToggleStandardAttributesOption(AppSessionBean appSessionBean,
			CatalogAttributeRequest request, CatalogAttributeResponse response,
			OEItemSearchCriteriaSessionBean criteria) {
		String standardOption = request.getToggleFeature();
		StandardAttributes standardAttrs = criteria.getStandardAttributes();
		if (!Util.isBlankOrNull(standardOption)) {
			switch (standardOption) {
			case ModelConstants.STANDARD_ATTRIBUTE_FILTER_FAVORITE:
				if (standardAttrs.isShowFavorites()) {
					standardAttrs.setFilterFavorites(!standardAttrs.isFilterFavorites());
				}
				break;

			case ModelConstants.STANDARD_ATTRIBUTE_FILTER_NEW_ITEM:
				if (standardAttrs.isShowNewItems()) {
					standardAttrs.setFilterNewItems(!standardAttrs.isFilterNewItems());
				}
				break;

			default:
				if (standardAttrs.getFeaturedSearchCriteria().stream().anyMatch(feature -> Util
						.safeStringToDefaultInt(standardOption, AtWinXSConstant.INVALID_ID) == feature.getTypeID())) {
					standardAttrs.getFeaturedSearchCriteria().stream()
							.filter(feature -> Util.safeStringToDefaultInt(standardOption,
									AtWinXSConstant.INVALID_ID) == feature.getTypeID())
							.forEach(feature -> feature.setSelected(!feature.isSelected()));
				} else {
					setFailureAndInvalidStandardAttributeValueMessage(appSessionBean, response);
				}
				break;
			}
			criteria.setStandardAttributes(standardAttrs);
		}
	}

	// CAP-46322
	protected void setFailureAndInvalidStandardAttributeValueMessage(AppSessionBean appSessionBean,
			CatalogAttributeResponse response) {
		setFailureAndMessage(response,
				getTranslation(appSessionBean, SFTranslationTextConstants.INVALID_STD_ATTR_SELECTED_MSG,
						SFTranslationTextConstants.INVALID_STD_ATTR_SELECTED_DEF_MSG));
	}

	// CAP-46322
	protected void setFailureAndInvalidAttributeValueMessage(AppSessionBean appSessionBean,
			CatalogAttributeResponse response) {
		setFailureAndMessage(response,
				getTranslation(appSessionBean, SFTranslationTextConstants.INVALID_ATTR_VAL_SELECTED_MSG,
						SFTranslationTextConstants.INVALID_ATTR_VAL_SELECTED_DEF_MSG));
	}

	// CAP-46322
	protected void setFailureAndMessage(CatalogAttributeResponse response, String message) {
		response.setSuccess(false);
		response.setMessage(message);
	}

	protected List<CatalogAttributes> convertList(List<DynamicItemAttributeVO> voList, OEOrderSessionBean oeSessionBean) {
		OEItemSearchCriteriaSessionBean searchCriteriaBean = oeSessionBean.getSearchCriteriaBean();
		Map<Integer, List<Integer>> attributesCriteria = searchCriteriaBean.getAttributesCriteria();
		List<CatalogAttributes> result = new ArrayList<>();

		if (null != voList && !voList.isEmpty()) {
			for (DynamicItemAttributeVO vo : voList) {
				List<CatalogAttributeValues> catalogAttrValuesList = new ArrayList<>();
				int attrID = vo.getSearchOptions().getAttrID();
				CatalogAttributes catalogAttributes = new CatalogAttributes();

				int searchCatalogInd = vo.getSearchOptions().getSearchCatalogInd();

				if (vo.getSearchOptions() != null) {
					catalogAttributes.setAttributeDisplayName(vo.getSearchOptions().getAttrDispName());
					catalogAttributes.setAttributeID(attrID);
					catalogAttributes.setMultiSelect(searchCatalogInd == 4 || searchCatalogInd == 5);

					List<SiteAttrValuesVO> siteAttrValueVOList =  vo.getSiteAttrValueVOList();

					setCatalogAttrValues(attributesCriteria, attrID, catalogAttrValuesList, catalogAttributes,
							siteAttrValueVOList);
					result.add(catalogAttributes);
				}

			}
		}
		return result;
	}

	private void setCatalogAttrValues(Map<Integer, List<Integer>> attributesCriteria, int attrID,
			List<CatalogAttributeValues> catalogAttrValuesList, CatalogAttributes catalogAttributes,
			List<SiteAttrValuesVO> siteAttrValueVOList) {
		if (siteAttrValueVOList != null) {
			for (SiteAttrValuesVO siteAttrValuesVO : siteAttrValueVOList) {
				if (siteAttrValuesVO.getAttrID() == attrID) {
					CatalogAttributeValues catalogAttrValues = new CatalogAttributeValues();
					boolean isSelected = false;
					if (null != attributesCriteria && !attributesCriteria.isEmpty()) {
						List<Integer> list = attributesCriteria.get(attrID);
						isSelected = null != list && !list.isEmpty()
								&& list.stream().anyMatch(value -> value.equals(siteAttrValuesVO.getAttrValID()));
					}
					catalogAttrValues.setSelected(isSelected);
					catalogAttrValues.setAttributeID(siteAttrValuesVO.getAttrID());
					catalogAttrValues.setAttributeValueDisplay(siteAttrValuesVO.getAttrValDesc());
					catalogAttrValues.setAttributeValueID(siteAttrValuesVO.getAttrValID());

					catalogAttrValuesList.add(catalogAttrValues);
				}
			}
			catalogAttributes.setCatalogAttributeValues(catalogAttrValuesList);

		}
	}

	private List<DynamicItemAttributeVO> getItemAttributeListOptionsVO(AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession) throws AtWinXSException {
		List<DynamicItemAttributeVO> nonUDFDynamicItemAttributeVOs = null;

		ComplexAttributeBaseJSP complexAttribute = new ComplexAttributeBaseJSP();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		oeSessionBean.setFamilyMemberSiteAttrIDs(getHasFamilyItemSearchAttrs(oeSessionBean, appSessionBean));

		// CP-11231 RAR - Set the filtered attribute values in order entry session to
		// make sure that that displayed
		// attribute values are correct based on profile.
		filterBasedOnProfileAttr(oeSessionBean, appSessionBean);

		complexAttribute.init(appSessionBean, volatileSessionBean, oeSessionBean,
				ComplexAttributeBaseJSP.CATALOG_SEARCH);
		DynamicItemAttributeVO[] attributeListOptionVOs = null;

		if (oeSessionBean.getUsrSrchOptions() != null && !oeSessionBean.getUsrSrchOptions().isEmpty()) {
			StringBuilder attributes = new StringBuilder();
			attributeListOptionVOs = complexAttribute.buildAttributeListOptions(attributes,
					oeSessionBean.getUsrSrchOptions(), appSessionBean);
			if (null != attributeListOptionVOs && attributeListOptionVOs.length > 0) {
				List<DynamicItemAttributeVO> dynamicItemAttributeVOs = Arrays.asList(attributeListOptionVOs);
				nonUDFDynamicItemAttributeVOs = new ArrayList<>();
				for (DynamicItemAttributeVO vo : dynamicItemAttributeVOs) {
					if (vo.getSearchOptions() != null && vo.getSearchOptions().getKey() != null
							&& vo.getSearchOptions().getKey().getAttrType().equalsIgnoreCase("L")) {
						nonUDFDynamicItemAttributeVOs.add(vo);
					}
				}
			}
		}
		// CAP-1203 moved session save to the end of the method so any changes to
		// session during the request will be saved.
		sessionHandlerService.saveFullSessionInfo(oeSession, appSessionBean.getSessionID(),
				AtWinXSConstant.ORDERS_SERVICE_ID);
		return nonUDFDynamicItemAttributeVOs;
	}

	private Set<Integer> getHasFamilyItemSearchAttrs(OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean)
			throws AtWinXSException {

		OECatalogAssembler catalogAssembler = new OECatalogAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		Set<Integer> familyMemberAttrIds = new HashSet<>();
		try {
			familyMemberAttrIds = catalogAssembler.getHasFamilyItemSearchAttrs(oeSessionBean.getUsrSrchOptions(),
					appSessionBean);
		} catch (AtWinXSException e) {
			// CAP-16460 call to logger.
			LOGGER.error(this.getClass().getName() + " - " + e.getMessage(), e);
		}
		return familyMemberAttrIds;
	}

	private void filterBasedOnProfileAttr(OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean)
			throws AtWinXSException {

		OECatalogAssembler catalogAssembler = new OECatalogAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		catalogAssembler.filterBasedonProfileAttr(oeSessionBean, appSessionBean);

	}
	
	// CAP-46304 - Get List of StandardAttributes 
	public StandardAttributesC1UX getStandardAttributeList(AppSessionBean appSessionBean, OrderEntrySession oeSession) 
			throws CPRPCException, AtWinXSException, IllegalAccessException, InvocationTargetException {
		
		StandardAttributesC1UX standardAttributesC1UX = new StandardAttributesC1UX();
		StandardAttributes result = null;
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		
		if (oeSessionBean != null) {
			
			if (oeSessionBean.getSearchCriteriaBean().getStandardAttributes() != null) {
				
				return getStandardAttributesFromSession(oeSessionBean, standardAttributesC1UX);
			}
			else {
				
				CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean = null;
				catSearchFeatFaveBean = getCatSearchFeatFaveBean(appSessionBean, oeSession, catSearchFeatFaveBean);
				OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
				result = new StandardAttributes();

				if (catSearchFeatFaveBean != null) {

					setFavAndFeaToResultObject(appSessionBean, oeSessionBean, catSearchFeatFaveBean,  result );
				}

				if (userSettings != null && userSettings.isNewItemsFlag()) {
					
						result.setShowNewItems(true);
						result.setNewItemsLabel(Util.nullToEmpty(userSettings.getNewItemsLabel()));
				}
			}
		}
		
		oeSession.getOESessionBean().getSearchCriteriaBean().setStandardAttributes(result);
		SessionHandler.persistServiceInSession(oeSession, appSessionBean.getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
		BeanUtils.copyProperties(standardAttributesC1UX, result);
		
		return standardAttributesC1UX;
	}

	// CAP-46304 - Get standardAttributes from OE Session 
	public StandardAttributesC1UX  getStandardAttributesFromSession(OEOrderSessionBean oeSessionBean, StandardAttributesC1UX response) 
			throws IllegalAccessException, InvocationTargetException {
				
		// CAP-46323 - Removed bad code
		StandardAttributes result = oeSessionBean.getSearchCriteriaBean().getStandardAttributes();				
		BeanUtils.copyProperties(response, result);

		return response;
	}
	
	// CAP-46304 - Get defined featured and favorite status and count   
	public CatalogSearchFeaturesFavoritesBean  getCatSearchFeatFaveBean(AppSessionBean appSessionBean, 
			OrderEntrySession oeSession, CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean) 
			throws CPRPCException, AtWinXSException {
		
		OECatalogAssembler catalogAssembler = new OECatalogAssembler(appSessionBean.getCustomToken(), 
				appSessionBean.getDefaultLocale());
		List<AlertCountResponseBean> featFaveItemsLst = null;
		try
		{
			
			featFaveItemsLst = catalogAssembler.getFavoritesFeaturedItemsCount(appSessionBean);
		} catch (AtWinXSException e) {
			
			throw Util.asCPRPCException(e);
		}
	
		if (featFaveItemsLst != null && !featFaveItemsLst.isEmpty()) {
			
			catSearchFeatFaveBean = new CatalogSearchFeaturesFavoritesBean();
			for (AlertCountResponseBean featFaveItems: featFaveItemsLst) {
				
				if (featFaveItems.getAlertCategory().equals(AtWinXSConstant.QUICK_FIND_FAVORITE_ITEMS)) {
					
					catSearchFeatFaveBean.setHasFavoriteItems(true);
				} else {
					
					catSearchFeatFaveBean.setFeaturedItemsDefined(featFaveItems.getAlertCounts());
				}
			}
	
			oeSession.getOESessionBean().setCatalogSearchFeaturesFavoritesBean(catSearchFeatFaveBean);
			SessionHandler.persistServiceInSession(oeSession, appSessionBean.getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
		}
		
		return catSearchFeatFaveBean;
	}
	
	// CAP-46304 - set the defined favorite and feature search criteria bean
	public void setFavAndFeaToResultObject(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, 
			CatalogSearchFeaturesFavoritesBean catSearchFeatFaveBean, StandardAttributes result ) 
			throws AtWinXSException {
		
		if (oeSessionBean.isAllowUserFavorites() && catSearchFeatFaveBean.hasFavoriteItems()) {
			
			result.setShowFavorites(true);
			result.setFavoritesLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), SFTranslationTextConstants.STANDARD_FAVORITE_ATTRIBUTE_LBL));
			
		}
	
		if (catSearchFeatFaveBean.getFeaturedItemsDefined() != null) {

			for (AlertCounts alertCnt : catSearchFeatFaveBean.getFeaturedItemsDefined()) {
			
				if (!Util.isBlankOrNull(alertCnt.getAlertCd()) && catSearchFeatFaveBean.getFeaturedItemsSearch() != null) {
				
					FeaturedSearchCriteria criteria = new FeaturedSearchCriteria();
					criteria.setLabel(alertCnt.getAlertName());
					criteria.setTypeID(Util.safeStringToDefaultInt(alertCnt.getAlertCd(), AtWinXSConstant.INVALID_ID));
					result.getFeaturedSearchCriteria().add(criteria);
				}
			}
		}
	}
}