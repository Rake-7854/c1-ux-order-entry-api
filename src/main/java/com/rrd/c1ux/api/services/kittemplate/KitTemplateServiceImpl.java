/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/07/24	N Caceres			CAP-50006				Initial Version
 *	06/11/24	Satishkumar A		CAP-50007				C1UX API - Create new api to remove component kit item from kit
 *	06/10/24	L De Leon			CAP-49882				Added initKitTemplate() method
 *	06/10/24	N Caceres			CAP-50036				Add kit component backed changes
 *	06/18/24	N Caceres			CAP-50186				Create new method for adding wild card component to kit template
 *	06/19/24	N Caceres			CAP-50260				Back-end changes for adding wild card component to kit template
 *	06/25/24	Satishkumar A		CAP-50308				C1UX API - Creation of service to reload KitSession when coming back to kit editor from search or custom docs
 *	06/24/24	S Ramachandran		CAP-50356				Validate when add items to kits to check amount of custom docs exceed
 *	06/26/24	N Caceres			CAP-50309				Create new method for browsing catalog from Kit Editor
 *	06/26/24	C Codina			CAP-50033				C1UX BE - Modify /api/kittemplate/addcomponent method to add more information about the added component for the front-end to build panel correctly
 *	06/27/24	M Sakthi			CAP-50330				C1UX BE - When adding components for a kit, we need to create an API to add the components to our order
 *	06/26/24	L De Leon			CAP-50359				Added cancelKitTemplate() method
 *	06/28/24	Satishkumar A		CAP-50504				C1UX BE - Creation of service to reload KitSession when coming back to kit editor from search or custom docs
 *	06/26/24	N Caceres			CAP-50537				Back-end changes for Catalog Browse API
 *	07/02/24	C Codina			CAP-50500				C1UX BE - Modify /api/kittemplate/addwildcardcomponent method to add more information about the added component for the front-end to build panel correctly
 *	07/03/24	Satishkumar A		CAP-50560				Added catalogSearchForKitTemplates() method
 *	07/08/24	Satishkumar A		CAP-50737				C1UX BE - Create API to perform Catalog Search for Kit Templates
 *	07/09/24	L De Leon			CAP-50842				Added populateUomDisplayForComponentItems method
 *	07/05/24	S Ramachandran		CAP-50732				Validate location code
 */
package com.rrd.c1ux.api.services.kittemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.kittemplate.ComponentItemErrors;
import com.rrd.c1ux.api.models.kittemplate.ComponentItems;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateRequest;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateResponse;
import com.rrd.c1ux.api.models.kittemplate.KitCatalogBrowseResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateCancelResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateEditCustomDocRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateEditCustomDocResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateSearchResponse;
import com.rrd.c1ux.api.models.kittemplate.UOMOption;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUOMAcronyms;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.orderentry.ao.KitFormBean;
import com.rrd.custompoint.orderentry.entity.KitFormEntity;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.textprocessing.TextProcessor;
import com.wallace.atwinxs.items.ao.ItemUdfKywdAssembler;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.ao.MKTemplateOrderAssembler;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKComponentInfo;
import com.wallace.atwinxs.kits.session.MKDirectAddInfo;
import com.wallace.atwinxs.kits.session.MKHeaderInfo;
import com.wallace.atwinxs.kits.session.MKUOMInfo;
import com.wallace.atwinxs.kits.session.OEComponentInfo;
import com.wallace.atwinxs.kits.util.KitsConstants;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OECatalogTreeResponseBean;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class KitTemplateServiceImpl extends BaseOEService implements KitTemplateService {

	private static final Logger logger = LoggerFactory.getLogger(KitTemplateServiceImpl.class);
	
	
	private static final String KIT_REASON_TAG = "{reason}";
	private static final String KIT_VENDOR_ITEM_NUMBER_TAG = "{vendorItemNumber}";
	
	//CAP-50330
	public static final String ASSEMBLY_INS_FIELD_NAME = "assemblyInstructions";
	
	protected final SessionHandlerService sessionHandlerService;
	
	protected KitTemplateServiceImpl(TranslationService translationService, ObjectMapFactoryService objService,
			SessionHandlerService sessionHandlerService) {
		super(translationService, objService);
		this.sessionHandlerService = sessionHandlerService;
	}

	@Override
	public KitTemplateAddCompResponse addKitComponent(SessionContainer sc,
			KitTemplateAddCompRequest kitTemplateAddCompRequest) throws AtWinXSException {
		KitTemplateAddCompResponse kitTemplateAddCompResponse = new KitTemplateAddCompResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		if(!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		KitSession kitSession = getKitSession(appSessionBean);
		TextProcessor textProcessor = getTextProcessor(appSessionBean);
		
		if (null != kitSession) {
			
				ArrayList<String> errors = new ArrayList<>();
				MKTemplateOrderAssembler kitAssembler = initKitAssembler(appSessionBean);
				
				int i = getComponentIndex(kitTemplateAddCompRequest.getCompVendorItemNumber(),
						kitTemplateAddCompRequest.getCompCustomerItemNumber(), kitSession.getComponents());

				// CAP-50732 - location code validation
				validateLocationCode(kitTemplateAddCompRequest, volatileSessionBean, appSessionBean,
						kitTemplateAddCompResponse);
				if (kitTemplateAddCompResponse.isSuccess()) {
					validateVendorItem(kitTemplateAddCompRequest, i, textProcessor, kitSession, kitAssembler,
						appSessionBean, kitTemplateAddCompResponse);
				}
				if (!kitTemplateAddCompResponse.isSuccess()) { 
					kitSession.setClearParameters(false);
					return kitTemplateAddCompResponse;
				}
				
			try {
				if (i > AtWinXSConstant.INVALID_ID) {
					kitTemplateAddCompResponse.setSuccess(kitAssembler.addComponentToKit(i, String.valueOf(kitTemplateAddCompRequest.getLocationCode()),
							errors, volatileSessionBean, kitSession, appSessionBean, textProcessor));
					buildKitTemplateComponent(kitTemplateAddCompResponse, kitSession.getComponents()[i], appSessionBean);
					saveSession(appSessionBean, applicationVolatileSession, kitSession);
					kitTemplateAddCompResponse.setKitLineNumber(Integer.valueOf(kitSession.getComponents()[i].getKitLineNum()));
				} else {
					String addKitError = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
							TranslationTextConstants.TRANS_NM_KIT_TEMPLATE_ADDED_ERROR);
					buildAddKitComponentError(kitTemplateAddCompResponse, kitTemplateAddCompRequest.getCompVendorItemNumber(), appSessionBean, addKitError);
				}
			} catch (AtWinXSException wine) {
				
				// CAP-50356
				ArrayList<String> errorMsgsValidateAddComponent = new ArrayList<>();
				int orderNum = -1;
				if (Util.isBlankOrNull(kitSession.getHeader().getOrderNum())) {
					
					orderNum = Util.emptyToZero(kitSession.getHeader().getOrderNum());
				}
				if (orderNum == -1 &&  ((volatileSessionBean.getOrderId() != null) && 
					        (!volatileSessionBean.getOrderId().toString().equals("")))) {
						
						orderNum = volatileSessionBean.getOrderId().intValue();
				}
				int specNum = kitSession.getCurrentCustDocSpecID();
				String location = String.valueOf(kitTemplateAddCompRequest.getLocationCode());
						
				kitAssembler.validateAddComponent(kitSession, appSessionBean,
						kitSession.getComponents()[i].getWCSSItemNum(),
						kitSession.getComponents()[i].getCustomerItemNum(), location, errorMsgsValidateAddComponent,
						orderNum, specNum);
				
				kitTemplateAddCompResponse.setSuccess(false);
				kitTemplateAddCompResponse.setMessage(errorMsgsValidateAddComponent.get(0));
			}
			kitSession.setClearParameters(false);
		} else {
			kitTemplateAddCompResponse.setSuccess(false);
			kitTemplateAddCompResponse.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.KIT_INIT_ERROR));
		}
		return kitTemplateAddCompResponse;
	}
	
	// CAP-50732 - location code validation
	protected void validateLocationCode(
			KitTemplateAddCompRequest kitTemplateAddCompRequest, VolatileSessionBean volatileSessionBean,
			AppSessionBean appSessionBean, KitTemplateAddCompResponse kitTemplateAddCompResponse)
			throws AtWinXSException {

		kitTemplateAddCompResponse.setSuccess(true);
		if (null == kitTemplateAddCompRequest.getLocationCode()
				|| kitTemplateAddCompRequest.getLocationCode() < 0 || kitTemplateAddCompRequest
						.getLocationCode() >= volatileSessionBean.getKitTemplateContainerLocations()) {

			kitTemplateAddCompResponse.setSuccess(false);
			kitTemplateAddCompResponse.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.INVALID_LOCATION_ERR_MSG));
		}
	}
	
	
	// CAP-50732 - Moved here to reduce cognitive complex
	protected void validateVendorItem(KitTemplateAddCompRequest kitTemplateAddCompRequest, int componentIndex,
			TextProcessor textProcessor, KitSession kitSession, MKTemplateOrderAssembler kitAssembler,
			AppSessionBean appSessionBean, KitTemplateAddCompResponse kitTemplateAddCompResponse)
			throws AtWinXSException {

		kitTemplateAddCompResponse.setSuccess(true);
		try {

			if (componentIndex > AtWinXSConstant.INVALID_ID) {

				kitAssembler.validateKitComponentVendorItemNumber(componentIndex,
						String.valueOf(kitTemplateAddCompRequest.getLocationCode()), kitSession, textProcessor);
			}
		} catch (AtWinXSException wine) {
			buildAddKitComponentError(kitTemplateAddCompResponse, kitTemplateAddCompRequest.getCompVendorItemNumber(),
					appSessionBean, wine.getMessage());
		}
	}

	private MKTemplateOrderAssembler initKitAssembler(AppSessionBean appSessionBean) {
		return new MKTemplateOrderAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
	}

	private void buildAddKitComponentError(KitTemplateAddCompResponse kitTemplateAddCompResponse, String vendorItemNumber,
			 AppSessionBean appSessionBean, String errorMessage)
			throws AtWinXSException {
		kitTemplateAddCompResponse.setSuccess(false);
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(KIT_VENDOR_ITEM_NUMBER_TAG, vendorItemNumber);
		replaceMap.put(KIT_REASON_TAG, errorMessage);
		kitTemplateAddCompResponse.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), 
				SFTranslationTextConstants.KIT_ADD_COMP_ERROR, replaceMap));
	}
	
	//CAP-50033
	protected void buildKitTemplateComponent(KitTemplateAddCompResponse response, MKComponentInfo component, AppSessionBean appSessionBean) {

		Collection<UOMOption> uomOptions = new ArrayList<>();
		
		MKUOMInfo[] uoms = component.getItemUOMs();
		
		String criticalItem = component.getTmpltComponentItemCriticalInd().toUpperCase();
		String requiredItem = component.getRequiredItemInd().toUpperCase();
		
		response.setVendorItemNumber(component.getwCSSItemNum());
		response.setCustomerItemNumber(component.getCustomerItemNum());
		response.setItemDescription(component.getCustomerItemDesc());
		response.setRequiredItem("Y".equals(requiredItem));
		response.setOptionalItem("N".equals(requiredItem));
		response.setSuggestedItem("S".equals(requiredItem));
		response.setSequenceAvailable(Boolean.valueOf(component.getItemSequenceNum()));
		response.setLocation(Integer.valueOf(component.getSequenceLocationID()));
		response.setImageUrl(component.getItemImageURL());
		response.setCriticalItem("Y".equals(criticalItem));
		response.setNonCriticalItem("N".equals(criticalItem));
		response.setShipLaterBackorder("B".equals(criticalItem));
		response.setCanModifyCritical("U".equals(criticalItem));

		if(null!= uoms) {
			ItemUOMAcronyms itemUOMAcronyms = new ItemUOMAcronyms();
			Map<String,String> uomMap = itemUOMAcronyms.getFullTextforUomAcronyms();
			for (MKUOMInfo mkuomInfo : uoms) {
				UOMOption uomOption = new UOMOption();

				uomOption.setUomCode(mkuomInfo.getUOMCd());
				uomOption.setUomDisplay(getUomDisplay(appSessionBean, uomMap, mkuomInfo)); // CAP-50842 Refactored code into a reusable method
				uomOption.setMinQuantity(Util.safeStringToInt(mkuomInfo.getUOMMinimumQty()));
				uomOption.setMaxQuantity(Util.safeStringToInt(mkuomInfo.getUOMMaximumQty()));

				uomOptions.add(uomOption);
			}
		}
		response.setUomOptions(uomOptions);

		response.setSelectedQuantity(Integer.valueOf(component.getItemQty()));
		response.setSelectedUOM(component.getSelectedUOMCd());
		response.setNeedsContent(component.isCustomDoc());


	}


	private void saveSession(AppSessionBean appSessionBean, ApplicationVolatileSession applicationVolatileSession,
			KitSession kitSession) throws AtWinXSException {
		sessionHandlerService.saveFullSessionInfo(kitSession, appSessionBean.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
		sessionHandlerService.saveFullSessionInfo(applicationVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
	}
	

	private KitSession getKitSession(AppSessionBean appSessionBean)
			throws AtWinXSException {
		KitSession kitSession = null;
		try {
			kitSession = (KitSession) sessionHandlerService.loadSession(appSessionBean.getSessionID(),
					AtWinXSConstant.KITS_SERVICE_ID);
		} catch (AtWinXSWrpException eofex) {
			logger.error(this.getClass().getName() + " - " + eofex.getMessage(), eofex);
		}
		return kitSession;
	}
	
	public int getComponentIndex(String wcssItemNumber, String custItemNumber, MKComponentInfo[] components) {
		
		if (null != components) {
			return IntStream.range(0, components.length)
					.filter(i -> (components[i].getEncodedCustomerItemNum().equals(custItemNumber)
									&& components[i].getEncodedWCSSItemNum().equals(wcssItemNumber)
									&& !components[i].getInOrderInd()))
					.findFirst()
					.orElse(AtWinXSConstant.INVALID_ID);
		}
		else 
			return AtWinXSConstant.INVALID_ID;
	}
	
	protected TextProcessor getTextProcessor(AppSessionBean appSessionBean) throws AtWinXSException
	{
		return new TextProcessor(appSessionBean.getDefaultLocale(), appSessionBean.getDefaultLanguage(), Integer.toString(appSessionBean.getSessionID()));
	}

	//CAP-50007 //CAP-50112
	@Override
	public KitTemplateRemoveCompResponse removeKitComponent(SessionContainer sc,
			KitTemplateRemoveCompRequest kitTemplateRemoveCompRequest) throws AtWinXSException {
		
		KitTemplateRemoveCompResponse response = new KitTemplateRemoveCompResponse();
		response.setSuccess(true);
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		String kitLineNum = kitTemplateRemoveCompRequest.getKitLineNumber()+"";
		
		if(!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		TextProcessor textProcessor = getTextProcessor(appSessionBean);
		KitSession kitSession = null;
		MKHeaderInfo header = null;
		MKComponentInfo[] comps = null;
		try {
			kitSession = (KitSession) SessionHandler.loadSession(appSessionBean.getSessionID(),
					AtWinXSConstant.KITS_SERVICE_ID);
			header = kitSession.getHeader();
			comps = kitSession.getComponents();

		} catch (Exception eofex) {
			buildRemoveKitComponentError(response, appSessionBean, SFTranslationTextConstants.KIT_INIT_ERROR ,"");
			return response;
		}
		
		boolean kitLineNumValid = false;
		int remIndex = 0;
		
		if (header != null &&  comps != null)
		{
			for(int i = 0; i < comps.length ; i++)
			{
				if(comps[i].getKitLineNum().equals(kitLineNum))
				{
					kitLineNumValid = true;
					remIndex = i;
					break;
				}
			}
			
		}

       if(!kitLineNumValid) {
    	   buildRemoveKitComponentError(response, appSessionBean, SFTranslationTextConstants.KIT_INVALID_LINE_NUMBER_ERROR ,"");
			return response;
       }
		ArrayList<String> errors = new ArrayList<>();
		
		try {
		MKTemplateOrderAssembler kitAssembler = initKitAssembler(appSessionBean);
		boolean isRemoveSuccessful = kitAssembler.isRemoveCompSuccessful(comps, remIndex, errors, kitSession, volatileSessionBean, appSessionBean, textProcessor);

		kitSession.setClearParameters(false);
		
		if(!isRemoveSuccessful){
			buildRemoveKitComponentError(response, appSessionBean, SFTranslationTextConstants.KIT_INVALID_LINE_NUMBER_ERROR ,!errors.isEmpty() ? errors.get(0):"");
		}
		saveSession(appSessionBean, applicationVolatileSession, kitSession);
		}catch (Exception e) {
			response.setSuccess(false);
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(KIT_VENDOR_ITEM_NUMBER_TAG, comps[remIndex].getWCSSItemNum());
			replaceMap.put(KIT_REASON_TAG, e.getMessage());
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), 
					SFTranslationTextConstants.KIT_REMOVE_COMP_ERROR, replaceMap));

		}
		
		return response;
	}

	private void buildRemoveKitComponentError(KitTemplateRemoveCompResponse kitTemplateRemoveCompResponse,
			 AppSessionBean appSessionBean, String translationKey, String errorMessage)
			throws AtWinXSException {
		kitTemplateRemoveCompResponse.setSuccess(false);
		
		if(errorMessage.length() > 0) {
			kitTemplateRemoveCompResponse.setMessage(errorMessage);
		} else {
			kitTemplateRemoveCompResponse.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), 
					translationKey));
		}
	}

	// CAP-49882 Begin
	@Override
	public InitKitTemplateResponse initKitTemplate(SessionContainer sc, InitKitTemplateRequest request) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();

		InitKitTemplateResponse response = new InitKitTemplateResponse();

		validateKitTemplate(request, appSessionBean, response);

		if (response.isSuccess()) {
			Map<String, String> fieldErrors = new HashMap<>();
			validateCustomerItemNumber(appSessionBean, request.getCustomerItemNumber(), fieldErrors);
			validateVendorItemNumber(appSessionBean, request.getVendorItemNumber(), fieldErrors);

			if (fieldErrors.isEmpty()) {
				clearKitSession(appSessionBean, volatileSession, response);
				setResponseValues(appSessionBean, oeSessionBean, volatileSessionBean, response, request);
				saveSession(volatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
			} else {
				response.setSuccess(false);
				response.setFieldMessages(fieldErrors);
			}
		}

		return response;
	}

	protected void validateKitTemplate(InitKitTemplateRequest request, AppSessionBean appSessionBean,
			InitKitTemplateResponse response) throws AtWinXSException {
		MKTemplateOrderAssembler kitAssembler = initKitAssembler(appSessionBean);

		String customerItem = Util.nullToEmpty(request.getCustomerItemNumber());
		boolean isApproved = kitAssembler.isApproved(appSessionBean.getSiteID(), customerItem);

		boolean isDeleted = kitAssembler.isKitTemplateDeleted(appSessionBean.getSiteID(), customerItem);

		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		Date currentDate = cal.getTime();
		boolean itemIsWithinDates = kitAssembler.validateItemWithinAvailAndExpDates(appSessionBean.getSiteID(),
				customerItem, currentDate);

		boolean isLegalIfPunchout = true;
		if (appSessionBean.isPunchout()) {
			isLegalIfPunchout = kitAssembler.isPunchoutAbleToOrder(appSessionBean.getSiteID(), customerItem);
		}

		if (isDeleted) {
			response.setMessage("The Kit Template item you are selecting is marked as deleted.");
		} else if (!isApproved) {
			response.setMessage("The Kit Template item you are selecting is not approved for ordering at this time.");
		} else if (!isLegalIfPunchout) {
			response.setMessage("The Kit Template item you are selecting is not available for ordering.");
		} else if (!itemIsWithinDates) {
			response.setMessage("The Kit Template is either not yet available or has expired.");
		} else {
			response.setSuccess(true);
		}
	}

	protected void setResponseValues(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, InitKitTemplateResponse response, InitKitTemplateRequest request) {
		KitFormBean kitFormBean = retrieveKitFormBean(appSessionBean, oeSessionBean, volatileSessionBean, response, request);

		if (null != kitFormBean) {
			response.setKitFormBean(kitFormBean);
			response.setAllowCategoryViewSelection(appSessionBean.isAllowCatViewSelection());
			response.setKitMaxCharacters(Util.safeStringToInt(KitsConstants.ASSEMBLY_INSTRUCTIONS_MAX_SIZE));

			List<MKComponentInfo> compInfo = (List<MKComponentInfo>) kitFormBean.getKitComponentItems();
			setContainsOptionalItems(response, compInfo);
			setAllowDuplicateCustDoc(appSessionBean, response, compInfo);
		}
	}

	protected void setAllowDuplicateCustDoc(AppSessionBean appSessionBean, InitKitTemplateResponse response,
			List<MKComponentInfo> compInfo) {
		boolean dupFound = !compInfo.stream().map(MKComponentInfo::getWCSSItemNum).collect(Collectors.toList()).stream()
				.allMatch(new HashSet<>()::add);

		MKTemplateOrderAssembler kitAssembler = initKitAssembler(appSessionBean);
		boolean isAllowDupCustDoc = false;
		try {
			isAllowDupCustDoc = kitAssembler.isAllowDupCustDoc(appSessionBean.getSiteID(), appSessionBean.getBuID());
		} catch (AtWinXSException e) {
			logger.error(e.getMessage(), e);
		}
		response.setAllowDuplicateCustomDocs(isAllowDupCustDoc);

		if (!isAllowDupCustDoc && dupFound) {
			response.setWarningMessage(
					getTranslation(appSessionBean, SFTranslationTextConstants.KIT_TEMP_DUPLICATE_WARN,
							SFTranslationTextConstants.KIT_TEMP_DUPLICATE_DEF_WARN));
		}
	}

	protected void setContainsOptionalItems(InitKitTemplateResponse response, List<MKComponentInfo> compInfo) {
		boolean containsOptionalItems = compInfo.stream()
				.anyMatch(kitComp -> kitComp.getRequiredItemInd().equalsIgnoreCase("N"));
		response.setContainsOptionalItems(containsOptionalItems);
	}

	protected KitSession initKitSession(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			InitKitTemplateRequest request, InitKitTemplateResponse response) throws AtWinXSException {
		KitSession kitSession = new KitSession();
		sessionHandlerService.initNewModuleSession(appSessionBean, null, volatileSessionBean.isRequestorLocked(),
				kitSession, AtWinXSConstant.KITS_SERVICE_ID, LogManager.getLogger(this.getClass()));
		MKTemplateOrderAssembler kitAssembler = initKitAssembler(appSessionBean);

		loadKitAdmin(appSessionBean, kitSession, kitAssembler);

		this.setupUDFFormBean(kitSession, appSessionBean);
		// reset things in case getting here from a confirmation page
		this.clearOutSession(kitSession);
		MKHeaderInfo header = new MKHeaderInfo();
		header.setCustomerItemNum(request.getCustomerItemNumber());

		String orderNum = KitsConstants.ORDER_ID_DEFAULT;
		String orderLineNum = KitsConstants.ORDER_LINE_NUM_DEFAULT;
		if ((volatileSessionBean.getOrderId() != null) && (!volatileSessionBean.getOrderId().toString().equals(""))) {
			orderNum = volatileSessionBean.getOrderId().toString();
		}
		header.setOrderNum(orderNum);

		kitSession.setHeader(header);

		int wildCardItemsCount = kitAssembler.getWildCardItemsCount(appSessionBean.getSiteID(), request.getCustomerItemNumber());
		boolean hasWildCardItems = false;

		if (wildCardItemsCount > 0) {
			hasWildCardItems = true;
		}

		kitSession.setHasWildCardItems(hasWildCardItems);
		kitSession.setWildCardItemsCount(wildCardItemsCount);
		kitSession.setWildCardItemsRemaining(wildCardItemsCount);
		kitSession.setCurrentWildCardKitLineNumber(0);

		ArrayList rejectedComponents = new ArrayList();

		try {
			kitAssembler.loadOrder(kitSession, appSessionBean, orderNum, orderLineNum, volatileSessionBean,
					rejectedComponents);

			this.setCustomDocCounts(kitSession);

			if (hasWildCardItems) {
				this.setWildCardCounts(kitSession, wildCardItemsCount);
			}
			this.setSequence(kitSession, appSessionBean.getSiteID(), kitAssembler);

			kitAssembler.validateContainer(kitSession, appSessionBean);
			if (((kitSession.getComponents() == null) || (kitSession.getComponents().length == 0))
					&& (!hasWildCardItems)) {
				Message errorMsg = new Message();
				errorMsg.setErrGeneralMsg(translationService.processMessage(Locale.getDefault(), null,
						TranslationTextConstants.TRANS_NO_VALID_COMPONENTS_KIT_TEMPLATE_ERROR_MSG));
				throw new AtWinXSMsgException(errorMsg, this.getClass().getName());
			}

			header = kitSession.getHeader();
			header.setKitInd(KitsConstants.KIT_IND_ORDER);
			header.setAdditionalAssemblyInstructionsTxt(
					Util.nullToEmpty(header.getAdditionalAssemblyInstructionsTxt()));
			kitSession.setHeader(header);

			if (!rejectedComponents.isEmpty()) {
				StringBuilder rejectedWarningMessage = new StringBuilder(
						"The following item(s) have been removed from your kit template:");
				for (int x = 0; x < rejectedComponents.size(); x++) {
					MKComponentInfo mkComponent = (MKComponentInfo) rejectedComponents.get(x);
					rejectedWarningMessage.append("<br />Â» Item number ").append(mkComponent.getCustomerItemNum())
							.append(" is not a valid item.");
				}
				response.setWarningMessage(rejectedWarningMessage.toString());
			}
		} catch (AtWinXSMsgException me) {
			logger.error(this.getClass().getName() + " - " + me.getMessage(), me);
		} catch (AtWinXSException e) {
			logger.error(this.getClass().getName() + " - " + e.getMessage(), e);
		}

		return kitSession;
	}

	protected void loadKitAdmin(AppSessionBean appSessionBean, KitSession kitSession,
			MKTemplateOrderAssembler kitAssembler) throws AtWinXSException {
		if (kitSession.getAdminBean() == null || (kitSession.getAdminBean() != null
				&& Util.isBlankOrNull(kitSession.getAdminBean().getEmailAddress()))) {
			kitAssembler.loadKitAdmin(appSessionBean, kitSession);
		}
	}

	protected KitFormBean retrieveKitFormBean(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, InitKitTemplateResponse response, InitKitTemplateRequest request) {
		String activity = KitsConstants.ACTIVITY_ORDER;
		volatileSessionBean.setServiceID(AtWinXSConstant.ORDERS_SERVICE_ID);

		KitSession kitSession = null;
		try {
			kitSession = initKitSession(appSessionBean, volatileSessionBean, request, response);
		} catch (AtWinXSException e) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.KIT_INIT_ERROR,
					AtWinXSConstant.EMPTY_STRING));
		}

		return populateKitFormBean(appSessionBean, oeSessionBean, volatileSessionBean, response, activity, kitSession);
	}

	protected void setWildCardCounts(KitSession kitsession, int wildCardItemsCount) {
		MKComponentInfo[] comps = kitsession.getComponents();

		int wildCardItemsAdded = 0;
		int wildCardItemsRemaining = wildCardItemsCount;
		for (int i = 0; comps != null && i < comps.length; i++) {
			MKComponentInfo currentComp = comps[i];
			if (currentComp.isWildCardItemInOE() && currentComp.getInOrderInd().booleanValue()) {
				++wildCardItemsAdded;
				--wildCardItemsRemaining;
			}
		}

		kitsession.setWildCardItemsAdded(wildCardItemsAdded);
		kitsession.setWildCardItemsRemaining(wildCardItemsRemaining);
	}

	protected void setSequence(KitSession kitsession, int siteID, MKTemplateOrderAssembler asm)
			throws AtWinXSException {
		MKComponentInfo[] compInfo = kitsession.getComponents();
		if (KitsConstants.SEQUENCE_IND_USER_DEF.equals(kitsession.getHeader().getSeqTypeInd())) {
			for (int i = 0; compInfo != null && i < compInfo.length; i++) {
				compInfo[i]
						.setTmpltComponentItemSeqNR(asm.getDefaultSequence(siteID, compInfo[i].getCustomerItemNum()));
			}
		}
	}

	protected void setupUDFFormBean(KitSession kitsession, AppSessionBean appSessionBean) throws AtWinXSException {
		ItemUdfKywdAssembler asm = new ItemUdfKywdAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		kitsession.setUdfKywdBean(asm.buildScreenInformation(appSessionBean.getSiteID()));
	}

	protected void clearOutSession(KitSession kitsession) {
		kitsession.setComponents(null);
		kitsession.setHeader(null);
		kitsession.setStandardContainerSelected(null);
		MKDirectAddInfo[] thislist = new MKDirectAddInfo[KitsConstants.COUNT_DIRECT_ADD];
		for (int i = 0; i < KitsConstants.COUNT_DIRECT_ADD; i++) {
			thislist[i] = new MKDirectAddInfo();
		}

		kitsession.setDirectAddList(thislist);
		kitsession.setCheckedComponents(new HashMap());
	}

	protected void setCustomDocCounts(KitSession kitsession) {
		MKComponentInfo[] comps = kitsession.getComponents();

		int mailMergeCount = 0;
		int mergeCount = 0;
		int imprintCount = 0;
		// CP-2917 - KTOE [RBA] - Added checking of null
		for (int i = 0; comps != null && i < comps.length; i++) {
			MKComponentInfo currentComp = comps[i];

			if (OrderEntryConstants.ITEM_CLASS_CUSTOM_DOC.equals(currentComp.getItemClassificationCd())
					&& currentComp.getInOrderInd().booleanValue()) {
				// CP-2917 - Kit OE - Used existing ICustomDocsAdminConstants merge constants
				// For Imprint
				if (ICustomDocsAdminConstants.TMPLT_UI_MERGE_OPT_CD_I.equals(currentComp.getCdMergeOptCode())) {
					++imprintCount;
				}
				// For Mail Merge
				else if (ICustomDocsAdminConstants.TMPLT_UI_MERGE_OPT_CD_M.equals(currentComp.getCdMergeOptCode())) {
					++mailMergeCount;
				}
				// For Merge
				else if (ICustomDocsAdminConstants.TMPLT_UI_MERGE_OPT_CD_N.equals(currentComp.getCdMergeOptCode())) {
					++mergeCount;
				}
			}
		}

		kitsession.setMailMergeCount(mailMergeCount);
		kitsession.setMergeCount(mergeCount);
		kitsession.setImprintCount(imprintCount);
	}

	protected KitFormBean populateKitFormBean(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, InitKitTemplateResponse response, String activity,
			KitSession kitSession) {
		KitFormBean kitFormBean = null;
		if (null != kitSession) {
			kitSession.setOrderFromFile(null != oeSessionBean.getOrderFromFileBean());

			if (null == kitSession.getHeader()) {
				response.setSuccess(false);
				response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.KIT_INIT_ERROR, AtWinXSConstant.EMPTY_STRING));
			} else {
				try {
					populateUomDisplayForComponentItems(appSessionBean, kitSession);

					KitFormEntity kitFormEntity = objectMapFactoryService.getEntityObjectMap()
							.getObject(KitFormEntity.class, appSessionBean.getCustomToken());
					kitFormEntity.setActivity(activity);
					kitFormEntity.populate(kitSession, appSessionBean.getSiteID(), appSessionBean.getBuID(),
							appSessionBean.getDefaultLocale());
					saveSession(kitSession, appSessionBean.getSessionID(),
							AtWinXSConstant.KITS_SERVICE_ID);
					kitFormEntity.setShowVendorItemNumber(appSessionBean.isShowWCSSItemNumber());
					kitFormBean = (KitFormBean) kitFormEntity;

					volatileSessionBean.setCatalogSearchMode(0);
					OEItemSearchCriteriaSessionBean criteria = oeSessionBean.getSearchCriteriaBean();
					if (criteria == null) {
						criteria = new OEItemSearchCriteriaSessionBean();
					}
					criteria.setUnifiedSearch(false);
					criteria.setUnifiedSearchCriteria(null);
					oeSessionBean.setSearchCriteriaBean(criteria);

					volatileSessionBean
							.setKitTemplateContainerLocations(kitFormBean.getContainerSequenceLocations().size());
					volatileSessionBean
							.setKitTemplateContainerLocationNames(kitFormBean.getContainerSequenceLocationsNames());

					kitFormEntity.setVolatilSessionBeanValues(kitSession, volatileSessionBean);
				} catch (AtWinXSException e) {
					logger.error(e.getMessage(), e);
					response.setSuccess(false);
					response.setMessage(e.getMessage());
				}
			}
		}
		return kitFormBean;
	}

	// CAP-50842
	protected void populateUomDisplayForComponentItems(AppSessionBean appSessionBean, KitSession kitSession) {
		if (null != kitSession.getComponents()) {
			ItemUOMAcronyms itemUOMAcronyms = new ItemUOMAcronyms();
			Map<String, String> uomMap = itemUOMAcronyms.getFullTextforUomAcronyms();
			for (MKComponentInfo component : kitSession.getComponents()) {
				MKUOMInfo[] uoms = component.getItemUOMs();
				if (null != uoms) {
					for (MKUOMInfo mkuomInfo : uoms) {
						mkuomInfo.setUomDisplay(getUomDisplay(appSessionBean, uomMap, mkuomInfo));
					}
				}
				MKUOMInfo selectedUom = component.getSelectedItemUOM();
				if (null != selectedUom) {
					selectedUom.setUomDisplay(getUomDisplay(appSessionBean, uomMap, selectedUom));
				}
			}
		}
	}

	// CAP-50842
	protected String getUomDisplay(AppSessionBean appSessionBean, Map<String, String> uomMap,
			MKUOMInfo mkuomInfo) {
		String uomCode = mkuomInfo.getUOMCd();
		String translationKey = uomMap.get(uomCode);
		String uomFactor = mkuomInfo.getUOMFactor();
		String uomDescription = getTranslation(appSessionBean,
				SFTranslationTextConstants.PREFIX_SF + translationKey,
				AtWinXSConstant.EMPTY_STRING);
		String ofLabel = getTranslation(appSessionBean, SFTranslationTextConstants.OF_LABEL_FIELD,
				SFTranslationTextConstants.OF_LABEL_FIELD_VALUE);

		StringBuilder uomDisplay = new StringBuilder();
		uomDisplay.append(uomDescription).append(AtWinXSConstant.BLANK_SPACE).append(ofLabel)
				.append(AtWinXSConstant.BLANK_SPACE).append(uomFactor);
		return uomDisplay.toString();
	}

	protected void validateCustomerItemNumber(AppSessionBean appSessionBean, String fieldValue,
			Map<String, String> fieldErrors) {
		int maxSize = ModelConstants.MAX_LENGTH_CUSTOMER_ITEM_NUMBER;
		String label = getTranslation(appSessionBean, SFTranslationTextConstants.CUST_ITEM_NUMBER_LBL,
				AtWinXSConstant.EMPTY_STRING);
		String message = AtWinXSConstant.EMPTY_STRING;
		if (Util.isBlankOrNull(fieldValue)) {
			message = getRequiredErrorMessage(appSessionBean, label);
		} else if (fieldValue.length() > maxSize) {
			message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
		}

		String fieldName = ModelConstants.ALLOC_CUST_ITEM_NUMBER_FIELD_NAME;
		addToFieldErrorMessages(fieldErrors, message, fieldName);
	}

	protected void validateVendorItemNumber(AppSessionBean appSessionBean, String fieldValue,
			Map<String, String> fieldErrors) {
		int maxSize = ModelConstants.MAX_LENGTH_VENDOR_ITEM_NUMBER;
		String message = AtWinXSConstant.EMPTY_STRING;
		if (fieldValue.length() > maxSize) {
			message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
		}

		String fieldName = ModelConstants.ALLOC_VENDOR_ITEM_NUMBER_FIELD_NAME;
		addToFieldErrorMessages(fieldErrors, message, fieldName);
	}

	protected void addToFieldErrorMessages(Map<String, String> fieldErrors, String message, String fieldName) {
		if (!Util.isBlankOrNull(message)) {
			fieldErrors.put(fieldName, message);
		}
	}
	// CAP-49882 End
	
	// CAP-50186 CAP-50260
	@Override
	public KitTemplateAddCompResponse addWildCardComponent(SessionContainer sc,
			KitTemplateAddCompRequest request) throws AtWinXSException {
		KitTemplateAddCompResponse response = new KitTemplateAddCompResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		if(!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		KitSession kitSession = getKitSession(appSessionBean);
		TextProcessor textProcessor = getTextProcessor(appSessionBean);
		
		if (null != kitSession) {
			
			// CAP-50732 - location code validation
			validateLocationCode(request, volatileSessionBean, appSessionBean, response);
			if (!response.isSuccess()) { 
				kitSession.setClearParameters(false);
				return response;
			}
			
			ArrayList<String> errors = new ArrayList<>();
			MKTemplateOrderAssembler kitAssembler = initKitAssembler(appSessionBean);
			kitAssembler.addWildCardItemToKit(volatileSessionBean, kitSession, request.getCompCustomerItemNumber(), request.getCompVendorItemNumber(), 
					String.valueOf(request.getLocationCode()), appSessionBean, textProcessor, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.INVALID_ID);
			if(!Util.isBlankOrNull(volatileSessionBean.getErrorInKitBuild()))
			{
				response.setSuccess(false);
				if(OrderEntryConstants.ERROR_MSG_IS_PART_OF_KIT_TEMP.equals(volatileSessionBean.getErrorInKitBuild()))
				{
					response.setSuccess(true);
				}
				errors.add(volatileSessionBean.getErrorInKitBuild());	
				response.setMessage(buildErrorMessage(errors));
				// Need to clear out the error message after it was retrieved.
				volatileSessionBean.setErrorInKitBuild(null);
			} else {
				response.setSuccess(true);
				//CAP-50500
				Optional<MKComponentInfo> component = getComponentDetails(request.getCompVendorItemNumber(), request.getCompCustomerItemNumber(), kitSession.getComponents());
				if (component.isPresent()) {
					buildKitTemplateComponent(response, component.get(), appSessionBean);
					response.setKitLineNumber(Integer.valueOf(component.get().getKitLineNum()));
				}
				
			}
			
			saveSession(appSessionBean, applicationVolatileSession, kitSession);
		} else {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.KIT_INIT_ERROR));
		}
		
		return response;
	}
	
	private Optional<MKComponentInfo> getComponentDetails(String wcssItemNumber, String custItemNumber, MKComponentInfo[] components){
		List<MKComponentInfo> componentList = Arrays.asList(components);
		return componentList.stream()
				.filter(c -> c.getEncodedWCSSItemNum().equals(wcssItemNumber) && c.getEncodedCustomerItemNum().equals(custItemNumber))
				.findFirst();
		
	}
	
	private String buildErrorMessage(ArrayList<String> errors) {
		StringBuilder sb = new StringBuilder();
		for (String error : errors) {
			sb.append(error).append(",");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	//CAP-50504
	@Override
	public InitKitTemplateResponse reloadKitTemplate(SessionContainer sc) throws AtWinXSException {
		InitKitTemplateResponse response = new InitKitTemplateResponse();
		response.setSuccess(true);
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		if(!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		KitSession kitSession = getKitSession(appSessionBean);
		String activity = KitsConstants.ACTIVITY_ORDER;
		KitFormBean kitFormBean = populateKitFormBean(appSessionBean, oeSessionBean, volatileSessionBean, response, activity, kitSession);
		if (null != kitFormBean) {
			response.setKitFormBean(kitFormBean);
			response.setAllowCategoryViewSelection(appSessionBean.isAllowCatViewSelection());
			response.setKitMaxCharacters(Util.safeStringToInt(KitsConstants.ASSEMBLY_INSTRUCTIONS_MAX_SIZE));

			List<MKComponentInfo> compInfo = (List<MKComponentInfo>) kitFormBean.getKitComponentItems();
			setContainsOptionalItems(response, compInfo);
			setAllowDuplicateCustDoc(appSessionBean, response, compInfo);
			saveSession(applicationVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);

		} else {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.KIT_INIT_ERROR));

		}
		return response;
	}
	
	// CAP-50330 - Start Here
			@Override
			public KitTemplateAddToCartResponse addToCartKitTemplate(SessionContainer sc, KitTemplateAddToCartRequest request)
					throws AtWinXSException {
				KitTemplateAddToCartResponse response = new KitTemplateAddToCartResponse();
				AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
				OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
				OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
				ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
				VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();

				if (!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
					throw new AccessForbiddenException(this.getClass().getName());
				}

				KitSession kitSession = (KitSession) SessionHandler.loadSession(appSessionBean.getSessionID(),
						AtWinXSConstant.KITS_SERVICE_ID);
				MKTemplateOrderAssembler kitAssembler = new MKTemplateOrderAssembler(appSessionBean.getCustomToken(),
						appSessionBean.getDefaultLocale());

				KitFormBean kitFormBean=populateKitFormBean(appSessionBean, oeSessionBean, volatileSessionBean, new InitKitTemplateResponse(),
						KitsConstants.ACTIVITY_ORDER, kitSession);

				// CP-9419, need to populate the assembler instructions
				if(kitFormBean!=null) {
					kitSession.setHeader(kitFormBean.getKitHeader());
				}
				
				if(kitSession.getHeader()==null) {
					response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
							SFTranslationTextConstants.KIT_NOT_ADDED_CART));
					response.setSuccess(false);
				}
				
				else if (OrderEntryConstants.ADD_TO_CART_ACTION.equals("ADD_TO_CART_ACTION")) {
					kitFormBean=getKitFormBeanValues(appSessionBean, oeSessionBean, volatileSessionBean, new InitKitTemplateResponse(),
							KitsConstants.ACTIVITY_ORDER, kitSession, request);
					
					KitFormEntity kitFormEntity = (KitFormEntity) kitFormBean;
					
					if(!validKitSession(kitFormBean, request)) {
						response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								SFTranslationTextConstants.NOT_VALID_KIT_LINE));
						response.setSuccess(false);
						
					}
					else if(validCustDocKitSession(kitFormBean, request)) {
							response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
									SFTranslationTextConstants.NEEDS_CUSTDOC_CONTENT));
							response.setSuccess(false);
					}
					
					else {
					
						// CP-8485 SPB - update other properties of SelectedItemUOM.
						kitFormEntity.updateSelectedItemUOM();
						// CP-8485 SPB - get the updated data from KitFormBean and update the component
						// items on KitSession.
						kitFormEntity.updateSessionComponents(kitSession);
		
						// CP-8485 SPB Getting error messages
						boolean ok = false;
						try {
							ok = validateKit(kitSession, appSessionBean.getDefaultLocale(),
									appSessionBean,response);
						} catch (AtWinXSException e) {
							ok = false;
							response.setMessage(e.getMessage());
							response.setSuccess(false);
						}
						if (!ok) {
							// there must be components
							response.setSuccess(false);
		
						} // end if validation failed
						else if(request.getAssemblyInstructions().length()>4000) {
							assemnblylengthValidation(appSessionBean, response);
						}
							// CP-9493 RAR - Validate if minimum number of kit components is reached.
						else if (!kitFormEntity.validateMininmunItemsReached(kitSession)) {
							response.setSuccess(false);
							Map<String, Object> replaceMap = new HashMap<>();
							replaceMap.put("{kitMinCount}", kitSession.getHeader().getMinimumItemCnt());
							// CP-8970 changed token from String to an Object
							String errMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
									appSessionBean.getCustomToken(), "kit_min_not_reached_err_msg", replaceMap);
							response.setFieldMessage(AtWinXSConstant.MSG_ATTRIB_NAME,errMsg);
						}	
						else {
							saveAddToKit(appSessionBean, kitSession, volatileSessionBean, kitAssembler, response, request);
						}	

					} // validation ok

				}
				return response;
			}

			public KitFormBean getKitFormBeanValues(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
					VolatileSessionBean volatileSessionBean, InitKitTemplateResponse initResponse, String activity,
					KitSession kitSession, KitTemplateAddToCartRequest request) {
				KitFormBean kitFormBean = populateKitFormBean(appSessionBean, oeSessionBean, volatileSessionBean, initResponse,
						activity, kitSession);
				Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = kitFormBean
						.getKitItemDistributionMap();
				for (ComponentItems cItem : request.getComponentItems()) {
					for (Entry<Integer, Collection<MKComponentInfo>> entry : kitComponentsDistributionMap.entrySet()) {
						for (MKComponentInfo compInfo : entry.getValue()) {
							if (cItem.getKitLineNumber().equals(compInfo.getKitLineNum())) {
								compInfo.setItemQty(Integer.toString(cItem.getQuantity()));
								compInfo.setSelectedUOM(cItem.getUomCode());
								compInfo.setSequenceLocationID(Integer.toString(cItem.getSequenceLocationId()));
								compInfo.setItemSequenceNum(Integer.toString(cItem.getItemSequenceNumber()));
								compInfo.setKitComponentItemCriticalInd(cItem.getCriticalIndicator());
							}
						}

					}

				}
				return kitFormBean;
			}

			public void getKitAssemblyIns(KitTemplateAddToCartRequest request, MKHeaderInfo header, KitSession kitsession) {
				if (request.getAssemblyInstructions() != null) {
					// if assembly instructions there
					header.setAdditionalAssemblyInstructionsTxt(request.getAssemblyInstructions());
					kitsession.setHeader(header);
				} // end else when assembly instructions
				else {
					// no assembly instructions
					header.setAdditionalAssemblyInstructionsTxt("");
					kitsession.setHeader(header);
				}
			}
			
			
			public boolean validKitSession(KitFormBean kitFormBean, KitTemplateAddToCartRequest request) {
				boolean validKit=false;
					Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = kitFormBean
						.getKitItemDistributionMap();
				for (ComponentItems cItem : request.getComponentItems()) {
					for (Entry<Integer, Collection<MKComponentInfo>> entry : kitComponentsDistributionMap.entrySet()) {
						for (MKComponentInfo compInfo : entry.getValue()) {
							if (cItem.getKitLineNumber().equals(compInfo.getKitLineNum())) {
								validKit=true;
							}else {
								validKit=false;
							}
							
						}

					}

				}
				return validKit;
			}

			
			public KitTemplateAddToCartResponse assemnblylengthValidation(AppSessionBean appSessionBean,KitTemplateAddToCartResponse response) {
				response.setSuccess(false);
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, KitsConstants.ASSEMBLY_INSTRUCTIONS_MAX_SIZE + AtWinXSConstant.EMPTY_STRING);
				response.setFieldMessage(ASSEMBLY_INS_FIELD_NAME,
						getTranslation(appSessionBean, SFTranslationTextConstants.MAX_CHARS_ERR, SFTranslationTextConstants.MAX_CHARS_DEF_ERR,
								replaceMap));
				return response;
			}
			
		public KitTemplateAddToCartResponse validateAssemblyInsAndMinimum(AppSessionBean appSessionBean,KitTemplateAddToCartResponse response,KitTemplateAddToCartRequest request,
				KitSession kitSession,KitFormEntity kitFormEntity) throws AtWinXSException {
			if(request.getAssemblyInstructions().length()>4000) {
				assemnblylengthValidation(appSessionBean, response);
			}
				// CP-9493 RAR - Validate if minimum number of kit components is reached.
			else if (!kitFormEntity.validateMininmunItemsReached(kitSession)) {
				response.setSuccess(false);
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put("{kitMinCount}", kitSession.getHeader().getMinimumItemCnt());
				// CP-8970 changed token from String to an Object
				String errMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), "kit_min_not_reached_err_msg", replaceMap);
				response.setFieldMessage(AtWinXSConstant.MSG_ATTRIB_NAME,errMsg);
			}
			return response;
		 }	
		
		
		
		public void saveAddToKit(AppSessionBean appSessionBean, KitSession kitSession,VolatileSessionBean volatileSessionBean,MKTemplateOrderAssembler kitAssembler,KitTemplateAddToCartResponse response,KitTemplateAddToCartRequest request) throws AtWinXSException {
			kitSession.reorderComponentsForOrderingIfLocked(appSessionBean.getSiteID()); // CAP-10846 - allow kit
			// session to reorder
			// its components
			// Get master order line number back
			int saveOrderMasterOrdLine = kitAssembler.saveOrder(appSessionBean, kitSession, StringUtils.EMPTY,
			volatileSessionBean);
			getKitAssemblyIns(request, kitSession.getHeader(), kitSession);
			if (saveOrderMasterOrdLine > Integer.parseInt(KitsConstants.ORDER_LINE_NUM_DEFAULT))// success
			{
			
			// CP-9587 RAR - When the Kit Template is added to the cart, clear kit template
			// related variables and call the removeProcess() in volatile session bean.
			volatileSessionBean.clearKitTemplateModeObjects();
			volatileSessionBean.removeProcess();
			
			// CAP-3972 - Clear variables related to Kit after Add to Cart
			kitSession.setHeader(null);
			kitSession.setComponents(null);
			response.setSuccess(true);
			}
		}
		
		
		public boolean isValidLineNumbers(KitSession kitsession)
		{
			boolean isValid = false;
			Collection<MKComponentInfo> kitComponentItems;
			if (null != kitsession.getComponents())
			{
				kitComponentItems = new LinkedList<>(Arrays.asList(kitsession.getComponents()));
				for (MKComponentInfo kitComponentItem : kitComponentItems)
				{
					boolean inOrderInd=kitComponentItem.getInOrderInd();
					if (inOrderInd)
					{
						isValid = true;
					}
				}
			}
			return isValid;
		}
		
		
		public boolean validateKit(KitSession kitSession, Locale locale, AppSessionBean appSessionBean,KitTemplateAddToCartResponse response)  throws AtWinXSException
		{
			boolean valid = true;
			
			valid = isValidLineNumbers(kitSession);
			
			if(valid)
			{
				valid = validateQuantities(kitSession.getComponents(), locale, appSessionBean,response);
			}
			
			return valid;
		}
		
		
		protected boolean validateQuantities(MKComponentInfo[] components, Locale locale,AppSessionBean appSessionBean,KitTemplateAddToCartResponse response) throws AtWinXSException
		{
			boolean canProceed = true;
			
			StringBuilder errorMessage = new StringBuilder();

			Map<String, Object> replaceMap;
			Collection<ComponentItemErrors> compItemError=new ArrayList<>();
			
			for(MKComponentInfo component : components)
			{
				//RAR - Validate only the Quantities of Kit Components that are added in Kit.
				if(component.getInOrderInd().booleanValue())
				{
					errorMessage.append("");

					for(MKUOMInfo availableUOM: component.getAvailableUOMs())
					{
						if(availableUOM.getUOMCd().equals(component.getSelectedUOMCd()))
						{
							if(Util.safeStringToDefaultInt(component.getItemQty(), -1) < Util.safeStringToDefaultInt(availableUOM.getUOMMinimumQty(), -1) ||
									Util.safeStringToDefaultInt(component.getItemQty(), -1) > Util.safeStringToDefaultInt(availableUOM.getUOMMaximumQty(), -1))
							{
								canProceed = false;
								
								if(errorMessage.length() > 0)
								{
									errorMessage.append("<br>");
								}
								ComponentItemErrors componentItemErrors=new ComponentItemErrors();
								replaceMap = new HashMap<>();
								replaceMap.put("{min}", availableUOM.getUOMMinimumQty());
								replaceMap.put("{max}", availableUOM.getUOMMaximumQty());
								
								//CP-8970 changed to use customization token instead of site ID
								errorMessage.append(Util.nullToEmpty(TranslationTextTag.processMessage(locale, appSessionBean.getCustomToken(), 
										TranslationTextConstants.TRANS_NM_ITEM_QTY_ERROR, replaceMap)));
								componentItemErrors.setKitLineNumber(Integer.parseInt(component.getKitLineNum()));
								componentItemErrors.setErrorDescription(errorMessage.toString());
								compItemError.add(componentItemErrors);
							}
							
							break;
						}
					}
					
					//CP-10076 RAR
					component.setComponentErrorMessage(errorMessage.toString());
					response.setComponentItemErrors(compItemError);
				    
				}
			}

			return canProceed;
		}
		
		
		public boolean validCustDocKitSession(KitFormBean kitFormBean, KitTemplateAddToCartRequest request) {
			boolean validKitCustDoc=false;
			Map<Integer, Collection<MKComponentInfo>> kitComponentsDistributionMap = kitFormBean
					.getKitItemDistributionMap();
			for (ComponentItems cItem : request.getComponentItems()) {
				for (Entry<Integer, Collection<MKComponentInfo>> entry : kitComponentsDistributionMap.entrySet()) {
					for (MKComponentInfo compInfo : entry.getValue()) {
						if (cItem.getKitLineNumber().equals(compInfo.getKitLineNum()) && compInfo.getItemClassificationCd().equalsIgnoreCase(ItemConstants.ITEM_CLASS_CUSTOM_DOC)) {
							if((compInfo.getOldCustomDocID().isEmpty() || compInfo.getNewCustomDocID().isEmpty())&& !compInfo.isExpiredCustomDoc()) {
								validKitCustDoc=true;
							}
						}else {
							validKitCustDoc=false;
						}
						
					}

				}

			}
			return validKitCustDoc;
		}
  // CAP-50330 End Here

	// CAP-50537
	@Override
	public KitCatalogBrowseResponse kitBrowseCatalog(SessionContainer sc, KitTemplateAddToCartRequest request)
			throws AtWinXSException {
		KitCatalogBrowseResponse response = new KitCatalogBrowseResponse();
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		if(!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		PunchoutSessionBean punchoutSessionBean = appSession.getPunchoutSessionBean();
		
		KitSession kitSession = getKitSession(appSessionBean);
		
		if (null != kitSession) {
			if (kitSession.isAllowWildCard() && request.getAssemblyInstructions().length() < 4000) {
				updateKitSessionComponents(kitSession, request);
				updateVolatileSessionBean(volatileSessionBean, kitSession, OrderEntryConstants.CATALOG_SEARCH_MODE_KIT_BROWSE);
				setInitialCategory(appSessionBean, punchoutSessionBean, volatileSessionBean, oeSession);
				saveSession(appSessionBean, applicationVolatileSession, kitSession, oeSession);
				response.setSuccess(true);
			} else {
				response.setSuccess(false);
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
						SFTranslationTextConstants.KIT_CATALOG_BROWSE_ERROR));
			}
		} else {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.KIT_INIT_ERROR));
		}
		return response;
	}

	private void setInitialCategory(AppSessionBean appSessionBean, PunchoutSessionBean punchoutSessionBean, 
			VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession) throws AtWinXSException
	{
		OECatalogTreeResponseBean catalogTree = null;
		OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		if (punchoutSessionBean != null
				&& punchoutSessionBean.getPunchoutType() != null
				&& punchoutSessionBean.getPunchoutType().equals(OrderEntryConstants.PUNCHOUT_TYPE_AISLE))
		{
		    catalogTree = assembler.getPunchoutCatalog(
							appSessionBean,
							AtWinXSConstant.EMPTY_STRING,
							AtWinXSConstant.EMPTY_STRING,
							oeSessionBean,
							punchoutSessionBean.getDescription(),
							oeSessionBean.getSiteAttrFilterSQL(),
							volatileSessionBean.isKitTemplateMode());
		}
		else
		{
		    catalogTree = assembler.getCatalog(
						appSessionBean,
						AtWinXSConstant.EMPTY_STRING,
						AtWinXSConstant.EMPTY_STRING,
						oeSessionBean,
						oeSessionBean.getSiteAttrFilterSQL(),
						volatileSessionBean.isKitTemplateMode());						
		}

		if(null != catalogTree && CollectionUtils.isNotEmpty(catalogTree.getCategories()))
		{
			oeSessionBean.setLastSelectedCatalogId(catalogTree.getSelectedCategory());
			oeSessionBean.setCurrentCatalogCategory(catalogTree.getSelectedNode().getNodeName());
		}
		oeSession.setOESessionBean(oeSessionBean);
	}
	
	private void saveSession(AppSessionBean appSessionBean, ApplicationVolatileSession applicationVolatileSession,
			KitSession kitSession, OrderEntrySession oeSession) throws AtWinXSException {
		saveSession(appSessionBean, applicationVolatileSession, kitSession);
		sessionHandlerService.saveFullSessionInfo(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
	}
	
	private void updateKitSessionComponents(KitSession kitSession, KitTemplateAddToCartRequest request) {
		List<MKComponentInfo> componentsList = Arrays.asList(kitSession.getComponents());
		MKHeaderInfo kitHeader = kitSession.getHeader();
		for (ComponentItems componentItem : request.getComponentItems()) {
			componentsList.stream().filter(c1 -> c1.getKitLineNum().equals(componentItem.getKitLineNumber()))
				.findFirst()
				.ifPresent(c -> {
					c.setItemQty(String.valueOf(componentItem.getQuantity()));
					c.setSequenceLocationID(String.valueOf(componentItem.getSequenceLocationId()));
					c.setItemSequenceNum(String.valueOf(componentItem.getItemSequenceNumber()));
					c.setKitComponentItemCriticalInd(componentItem.getCriticalIndicator());
					
					List<MKUOMInfo> uomsList = Arrays.asList(c.getItemUOMs());
					Optional<MKUOMInfo> selectedUOM = uomsList.stream().filter(uom -> uom.getUOMCd().equals(componentItem.getUomCode())).findFirst();
					if (selectedUOM.isPresent()) {
						c.setSelectedItemUOM(selectedUOM.get());
					}
				});
		}
		kitHeader.setAdditionalAssemblyInstructionsTxt(request.getAssemblyInstructions());
		kitSession.setHeader(kitHeader);
		kitSession.setComponents(componentsList.toArray(new MKComponentInfo[componentsList.size()]));
	}
	
	private void updateVolatileSessionBean(VolatileSessionBean volatileSessionBean, KitSession kitSession, int catalogSearchMode) {
		volatileSessionBean.setCatalogSearchMode(catalogSearchMode);
		volatileSessionBean.setKitTemplateContainerLocations(kitSession.getHeader().getSelectedContainerType().getContainerSequenceLocations().length);
		volatileSessionBean.setKitTemplateContainerLocationNames(kitSession.getContainerSequenceLocationsNames());
		volatileSessionBean.setKitTemplateItemNumber(kitSession.getHeader().getCustomerItemNum());
		ArrayList<OEComponentInfo> selectedKitComponents = new ArrayList<>();
		for (MKComponentInfo component : kitSession.getComponents()) {
			if (component.getInOrderInd().booleanValue()) {
				OEComponentInfo selectedComponent = new OEComponentInfo();
				selectedComponent.setCustomerItemNum(component.getCustomerItemNum());
				selectedComponent.setWCSSItemNum(component.getWCSSItemNum());
				selectedComponent.setSequenceLocationID(component.getSequenceLocationID());
				selectedComponent.setRequiredItemInd(component.getRequiredItemInd());
				selectedKitComponents.add(selectedComponent);
			}
		}
		volatileSessionBean.setSelectedKitComponents(selectedKitComponents);
	}

	// CAP-50359
	@Override
	public KitTemplateCancelResponse cancelKitTemplate(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();

		KitTemplateCancelResponse response = new KitTemplateCancelResponse();
		response.setSuccess(true);

		clearKitSession(appSessionBean, applicationVolatileSession, response);

		return response;
	}

	protected void clearKitSession(AppSessionBean appSessionBean, ApplicationVolatileSession applicationVolatileSession,
			BaseResponse response) throws AtWinXSException {
		clearKitTemplateTraces(appSessionBean, applicationVolatileSession);
		deleteKitSession(appSessionBean, response);
	}

	protected void deleteKitSession(AppSessionBean appSessionBean, BaseResponse response) {
		try {
			if (sessionHandlerService.sessionExists(appSessionBean.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID)) {
				if (response instanceof KitTemplateCancelResponse) {
					KitSession kitSession = (KitSession) sessionHandlerService.loadSession(appSessionBean.getSessionID(),
							AtWinXSConstant.KITS_SERVICE_ID);
					((KitTemplateCancelResponse) response).setKitCustomerItemNumber(kitSession.getHeader().getCustomerItemNum());
				}
				sessionHandlerService.deleteSession(appSessionBean.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
			}
		} catch (AtWinXSException e) {
			response.setSuccess(true);
			response.setMessage(e.getMessage());
		}
	}

	protected void clearKitTemplateTraces(AppSessionBean appSessionBean,
			ApplicationVolatileSession applicationVolatileSession)
			throws AtWinXSException {
		applicationVolatileSession.getVolatileSessionBean().clearKitTemplateModeObjects();
		applicationVolatileSession.getVolatileSessionBean().removeProcess();
		sessionHandlerService.saveFullSessionInfo(applicationVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
	}
	// CAP-50359 End
	
	//CAP-50560
	@Override
	public KitTemplateSearchResponse catalogSearchForKitTemplates(SessionContainer sc, KitTemplateAddToCartRequest request)
			throws AtWinXSException {
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		if(!appSessionBean.hasService(AtWinXSConstant.KITS_SERVICE_ID)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		KitTemplateSearchResponse response = new KitTemplateSearchResponse();
		response.setSearchTerm(request.getSearchTerm());
		response.setSuccess(true);

		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		KitSession kitSession = getKitSession(appSessionBean);
		String searchTerm = request.getSearchTerm();
		
		
		if (null != kitSession) {
			
				if (kitSession.isAllowWildCard()) {
					
					if(searchTerm !=null && (!searchTerm.isBlank())) {
						updateKitSessionComponents(kitSession, request);
						updateVolatileSessionBean(volatileSessionBean, kitSession,OrderEntryConstants.CATALOG_SEARCH_MODE_KIT_SEARCH);
					
						OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
						setSearchCriteria(oeOrderSessionBean, request.getSearchTerm());
					
						saveSession(appSessionBean, applicationVolatileSession, kitSession, oeSession);
					} else {
						response.setSuccess(false);
						response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
								SFTranslationTextConstants.KIT_SEARCH_TERM_ERROR));
					}
				} else {
					response.setSuccess(false);
					response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
							SFTranslationTextConstants.KIT_CATALOG_BROWSE_ERROR));
				}
				
				
		} else {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.KIT_INIT_ERROR));
		}

		return response;
	}

	
	private void setSearchCriteria(OEOrderSessionBean oeOrderSessionBean, String searchTerm) {
		OEItemSearchCriteriaSessionBean criteria  = oeOrderSessionBean.getSearchCriteriaBean();
		if (criteria == null)
		{
			criteria = new OEItemSearchCriteriaSessionBean();
		}
		//CP-8485 SPB - set the unifiedSearchCriteria from kit search value.
		criteria.setUnifiedSearch(true);
		criteria.setUnifiedSearchCriteria(Util.nullToEmpty(searchTerm));
		//CAP-21009 removed GSA References
		//CAP-20165
		criteria.setSearchAppliance(oeOrderSessionBean.getUserSettings().getSearchAppliance());
		//CP-9288 reset search criteria
		oeOrderSessionBean.setLastSelectedCatalogId(AtWinXSConstant.INVALID_ID);
		criteria.setVendorItemNum(null);
		criteria.setSelectedCategoryId(AtWinXSConstant.INVALID_ID);
		criteria.setSearchSelectedCategory(false);
		criteria.setCustItemNum(null);
		criteria.setDescription(null);
		criteria.setOrderWizard(false);
		oeOrderSessionBean.setSearchCriteriaBean(criteria);

	}
	
	@Override
	public KitTemplateEditCustomDocResponse getKitComponentIndex(SessionContainer sc,
			KitTemplateEditCustomDocRequest request) throws AtWinXSException {
		KitTemplateEditCustomDocResponse response = new KitTemplateEditCustomDocResponse();
		response.setSuccess(true);
		return response;
	}

		
}
