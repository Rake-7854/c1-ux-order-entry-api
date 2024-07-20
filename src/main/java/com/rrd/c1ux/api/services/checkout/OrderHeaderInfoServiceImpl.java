/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				A Salcedo				CAP-39396					Initial Version
 *	05/03/23				A Salcedo				CAP-39396					Added null check.
 *	05/09/23				S Ramachandran			CAP-38156					Added Extended Item Quantity in Checkout Review Order
 *	05/18/23				Sakthi M				CAP-40547					C1UX BE - API Fixes - Error checks for valid and non-submitted order not coded in Delivery and Order info load APIs
 *	05/16/23				A Boomker				CAP-40687					Add saveOrderHeaderInfo() and modifications to reuse validate with BaseResponse
 *	06/07/23				A Boomker				CAP-38154					Convert save validation to be exclusively C1UX code
 *	06/15/23				A Boomker				CAP-40687/CAP-38154			Fix validation of masks on cust refs
 *	06/16/23				A Boomker				CAP-41412					Fix to ignore cust ref value length like CP if do not use
 *	06/27/23				A Boomker				CAP-41719					Change updateHeaderCustRefs() to update with defaults and lists
 *	06/27/23				A Boomker				CAP-41729					Added fixUneditableDropdownCustRefs() to help FE with display
 *	06/28/23				A Boomker				CAP-41722					Do not throw get header info errors if review = false unless bad admin errors
 *	07/03/23				A Salcedo				CAP-41852 					Added CustomerRefList null check.
 *	07/06/23				A Salcedo				CAP-41853					Fixed OT display.
 *	07/07/23				A Boomker				CAP-42122					Fix validateOrderTitle() to check enable before required
 *  07/12/23				L De Leon 				CAP-41618/CAP-41619			Added method to update header info settings based on selected delivery method
 *  07/24/23				C Codina				CAP-41546					Save Order Header Info API needs to parse, validate, and save order header messages
 *  08/07/23				Krishna Natarajan		CAP-41546					Made changes to perform null check on orderMessageComposite, getOrderDetailsMessages(), getOrderDetailsHeaderMessages() 
 *  08/07/23				N Caceres				CAP-42169					Add order header messages to order of display only in Get Order Header Info API
 *  08/17/23				N Caceres				CAP-41551					Save OrderDetailsShippingInfoImpl bean info on save call for order info
 *  09/01/23				Krishna Natarajan		CAP-43382					added new method processShowPaymentInfo for billing info
 *  09/07/23				N Caceres				CAP-43158					Display view only order header messages in order of display
 *  09/12/23				C Codina				CAP-42170					API Change - Get Order Header Info API needs to add ship info object and carrier fields to order of display
 *  10/03/23				T Harmon				CAP-44313					Fixed issue with carrier save and the semi-colon field.
 *	10/04/23				L De Leon				CAP-44312					Updated showThirdPartyAccountNumber as string with possible values Y/C/N
 *  10/13/23				Krishna Natarajan		CAP-44103					Fixed using FE jira as JIRA site isn't working - fixed the validation on the service /TPAccount
 *  11/03/23				N Caceres				CAP-44840					Add requested ship date to the get order header info service
 *  11/06/23				Krishna Natarajan		CAP-45036					Fixed the missing carrier service type description text 
 *  11/16/23				Krishna Natarajan 		CAP-44840 					Added null check along with the blank check
 *  12/22/23				Krishna Natarajan		CAP-46108 					Added block to display requestedShipDate if requested ship to date settings is allowed
 *  01/09/24				S Ramachandran			CAP-46294					Add order due date to Checkout pages	
 *	04/04/24				L De Leon				CAP-48274					Added doDateToDestination() method
 *	04/08/24				C Codina				CAP-48436					C1UX BE - Modification of /api/checkout/getorderheaderinfo method to indicate to front-end if they need to to the DTD call
 *	04/16/24				L De Leon				CAP-48457					Updated doDateToDestination() method to add implementation
 *	04/18/24				S Ramachandran			CAP-48719					Modify OrderHeaderInfo method to return Expedited Flag and Earliest Delivery Date
 *	04/15/24				Satishkumar A			CAP-48437					C1UX BE - Modify /api/user/saveorderheaderinfo method to save information for DTD
 *	04/24/24				Krishna Natarajan		CAP-48961					Added code to set due date settings from user settings at the UG level to the response
 *	04/29/24				Krishna Natarajan		CAP-49048					Added a code to set the orderDueDate in response of /api/checkout/getorderheaderinfo orderHeaderInfoDisplayOrder
 *  05/06/24				T Harmon				CAP-49118, 48782			Small fixes for DTD and Due Date
 *  05/07/24				Krishna Natarajan		CAP-49216					Added a block of code to include 3 new fiedlds in response of /api/checkout/getorderheaderinfo orderHeaderInfoDisplayOrder 
 *  05/13/24				C Codina				CAP-49122					C1UX BE - Add special instructions to the get order header info service
 *  05/17/24				M Sakthi				CAP-49278					C1UX BE - Modify Order Summary call to return efd information for order
 *  05/20/24				Krishna Natarajan		CAP-49122					Added logic for special instructions 
 *  06/13/24				Krishna Natarajan		CAP-50146					Added in the response to return the allow multiple email flag boolean
 *  06/24/24				Krishna Natarajan		CAP-50471					Added a line to set the ItemExtendedPrice and update currency description for efd chargers - getorderheaderinfo
 *  06/25/24				Krishna Natarajan		CAP-50471					Added a line for getorderheaderinfo to bring File delivery options map
 *  06/26/24				Krishna Natarajan		CAP-50482					Changed check on linenumber instead of description - getEfdEmailAddressesAndEfdDeliveryTypes method - inorder to have the right efdDeliveryTypes, efdDeliveryEmailAddresses & efdCharges
 *  06/28/24				Krishna Natarajan		CAP-49811 					Added new method to handle the itemTotalPricing based on deliverymethods  
 *  07/08/24				Krishna Natarajan		CAP-50581					Added code to set delivery Options List 
 *  07/09/24				Krishna Natarajan		CAP-50886					Added code to set flag if order is EFD only order
 */
package com.rrd.c1ux.api.services.checkout;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.checkout.DateToDestinationRequest;
import com.rrd.c1ux.api.models.checkout.DateToDestinationResponse;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoRequest;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoResponse;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveRequest;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveResponse;
import com.rrd.c1ux.api.models.common.GenericNameValuePair;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.CMCatalogComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.OAOrderAdminLocatorService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.orderentry.util.OrderEntryUtilService;
import com.rrd.c1ux.api.services.orderentry.util.OrderEntryUtilServiceImpl;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.orderentry.ao.EFDDestinationOptionsFormBean;
import com.rrd.custompoint.orderentry.ao.EFDFTPLocationFormBean;
import com.rrd.custompoint.orderentry.entity.EFDCRMTracking;
import com.rrd.custompoint.orderentry.entity.EFDCRMTrackingRecord;
import com.rrd.custompoint.orderentry.entity.EFDCartLine;
import com.rrd.custompoint.orderentry.entity.EFDEmail;
import com.rrd.custompoint.orderentry.entity.ExpeditedOrderService;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderDetails;
import com.rrd.custompoint.orderentry.entity.OrderDetailsBillingInfo;
import com.rrd.custompoint.orderentry.entity.OrderDetailsCustRefs;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderCustRef;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderInfo;
import com.rrd.custompoint.orderentry.entity.OrderDetailsMessages;
import com.rrd.custompoint.orderentry.entity.OrderDetailsShippingInfo;
import com.rrd.custompoint.orderentry.entity.OrderShipping;
import com.rrd.custompoint.orderentry.validators.entity.DueDateValidator;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.catalogs.vo.EFDSourceSetting;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.MaskFormatter;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOrderAdmin;
import com.wallace.atwinxs.orderentry.admin.util.OrderReferenceFieldList;
import com.wallace.atwinxs.orderentry.admin.vo.UserGroupOrderPropertiesVOKey;
import com.wallace.atwinxs.orderentry.ao.EFDDestinationsFormBean;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEExtendedQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderSummaryResponseBean;
import com.wallace.atwinxs.orderentry.dao.WcssFieldValidationDAO;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.session.OrderMessageComposite;
import com.wallace.atwinxs.orderentry.util.NameValuePair;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.OrderDueDateVO;
import com.wallace.atwinxs.orderentry.vo.OrderInfoShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVOKey;

@Service
public class OrderHeaderInfoServiceImpl extends BaseOEService implements OrderHeaderInfoService
{
	private static final Logger logger = LoggerFactory.getLogger(OrderHeaderInfoServiceImpl.class);
	public static final String ORDER_TITLE_FIELD_NAME = "orderTitle";
	public static final String PO_NUMBER_FIELD_NAME = "poNumber";
	public static final String CONTACT_NAME_FIELD_NAME = "contactName";
	public static final String CONTACT_PHONE_FIELD_NAME = "contactPhone";
	public static final String CONTACT_EMAIL_FIELD_NAME = "contactEmail";
	public static final int ORDER_TITLE_MAX_SIZE = 150;
	public static final int PO_NUMBER_MAX_SIZE = 20;
	public static final int CONTACT_NAME_MAX_SIZE = 50;
	public static final int CONTACT_EMAIL_MAX_SIZE = 128;
	// CAP-38154 - changing to pull labels from the non-standard keys from the translation map
	public static final String CONTACT_NAME_LABEL_KEY = "customerName";
	public static final String CONTACT_PHONE_LABEL_KEY = "phoneNumber";
	public static final String CONTACT_EMAIL_LABEL_KEY = "customerEmail";
	// CAP-42169
	public static final String OHM = "OHM";
	public static final String SHOW = "S";
	// CAP-41551
	public static final String CARRIER_SERVICE_LEVEL_FIELD_NAME = "carrierServiceLevel";
	public static final String THIRD_PARTY_ACCOUNT_FIELD_NAME = "thirdPartyAcount";
	public static final String FEDX = "FEDX";
	public static final String UPSS = "UPSS";
	public static final String FDEF = "FDEF";
	public static final String FEDG3 = "FEDG3";
	public static final String FEDGP = "FEDGP";
	public static final String FEDGC = "FEDGC";
	public static final String POBOX = "POBOX";
	public static final String CARRIER_CODES = "PO_BOX_CARRIER_CODES";
	// CAP-43158
	public static final String HIDDEN = "H";
	//CAP-42170
	public static final String THIRD_PARTY_ACCOUNT_REQUIRED_FIELD_NAME = "thirdPartyAccountRequired";
	public static final String CARRIER_SERVICE_LIST_FIELD_NAME = "carrierServiceList";
	public static final String REQUIRED_SIGNATURE_CR_FIELD_NAME = "requiredSignatureCR";
	public static final String SHOW_MESSAGE_CARRIER_CHANGE = "showMessageCarrierChange";
	public static final String MESSAGE_TEXT_FIELD_NAME = "messageText";
	public static final String CARRIER_URL_FIELD_NAME = "carrierUrl";
	// CAP-44840
	private static final String DATE_PATTERN = "MM/dd/yyyy";
	private static final String REQUESTED_SHIP_DATE_FIELD_NAME = "requestedShipDate";
	//CAP-49122
	private static final String SPECIAL_INSTRUCTIONS_FIELD_NAME = "specialInstructions";
	private static final String DEFAULT_VALUE = "Value";
	
	//CAP-46294
	private static final String ORDER_DUE_FIELD_NAME = "orderDueDate";
	
	//CAP-49216
	private static final String EXPEDITE_MANUFACTURING = "expediteManufacturing";
	private static final String EXPEDITE_DATE = "expediteDate";
	private static final String ORIGINAL_DATE = "originalDate";

	protected OrderEntryUtilService oeUtilService;
	protected final OAOrderAdminLocatorService oaOrderAdminLocatorService;
	
	// CAP-48719
	protected OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService;
	
	//CAP-49452
	protected CMCatalogComponentLocatorService  cmCatalogComponentLocatorService;

	protected OrderHeaderInfoServiceImpl(TranslationService translationService, ObjectMapFactoryService objService,
			OAOrderAdminLocatorService oaOrderAdminLocatorService,
			OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService,CMCatalogComponentLocatorService cmCatalogComponentLocatorService) {
		super(translationService, objService);
		oeUtilService = new OrderEntryUtilServiceImpl();
		this.oaOrderAdminLocatorService = oaOrderAdminLocatorService;
		this.oeManageOrdersComponentLocatorService = oeManageOrdersComponentLocatorService;
		this.cmCatalogComponentLocatorService =cmCatalogComponentLocatorService;
	}

	@Override
	public OrderHeaderInfoResponse getOrderHeaderInfo(SessionContainer sc, OrderHeaderInfoRequest orderHeaderInfoRequest) throws IllegalAccessException, InvocationTargetException, AtWinXSException
	{
		OrderHeaderInfoResponse response = new OrderHeaderInfoResponse();

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		Message msg = new Message();
		List<String> errMsgs = new ArrayList<>();
		String orderTitle = AtWinXSConstant.EMPTY_STRING;
		msg.setErrMsgItems(errMsgs);

		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();//CAP-48961
		
		//CAP-40547-C1UX BE - API Fixes - Error checks for valid and non-submitted order not coded in Delivery and Order info load APIs
		if (!validateOrder(response, sc, appSessionBean)){
			return response;
		}

		response.setSuccess(true);

		// populate Header Info
		OrderDetailsHeaderInfo headerInfo = this.loadHeaderInfo(appSessionBean, oeSessionBean, volatileSessionBean, errMsgs,orderTitle);
		OrderDetailsShippingInfo shippingInfo = this.loadShippingInfo(appSessionBean, oeSessionBean, volatileSessionBean, errMsgs, orderTitle, msg, headerInfo);				
		
		// CAP-49118 TH
		shippingInfo.setOrderDueDate(getOrderDueDate(shippingInfo.getOrderID(), appSessionBean));
		
		if (!errMsgs.isEmpty())
		{ // if bad admin, respond right away
			  response.setSuccess(false);
			  response.setMessage(getCombinedMessage(msg.getErrMsgItems()));
		}
		// CAP-38156 - If Saved ContactName, ContactPhoneNum, ContactEmailAddr from XST076 loaded, then validate
		else if(!headerInfo.isFirstVisit())
		{
			// CAP-38156 - do fieldLevelValidation & cpValidation, if false throw 422
			validateOrderHeaderInfo(headerInfo, msg, oeSessionBean, appSessionBean, volatileSessionBean, response);

			if (!response.isValidAndComplete())	{
				// CAP-41722 - do not throw a 422 here unless review is true
				if (orderHeaderInfoRequest.isReview())
				{
					response.setSuccess(false);
					response.setMessage(getCombinedMessage(msg.getErrMsgItems()));
				}
				else
				{
					response.getFieldMessages().clear();
					response.setMessage(AtWinXSConstant.EMPTY_STRING);
				}
			}
 
		}

		processOrderDetailsHeaderInfo(headerInfo, response);
		
		//CAP-42170
		processOrderDetailsShippingInfo(shippingInfo, response, appSessionBean, oeSessionBean, headerInfo);

		processOrderOfDisplay(headerInfo, response, oeSessionBean,sc);
		
		processShowPaymentInfo(response, sc);
		
		//CAP-48436
		processDTDServiceCall(oeSessionBean, response);

		if(orderHeaderInfoRequest.isReview() ) {

			// CAP-38156 - load Extended Item Quantity only if review=true and validAndComplete=true
			if (response.isValidAndComplete()) {

				loadExtendedItemQuantity(sc,headerInfo.getOrderID(), response, appSessionBean,oeSessionBean, volatileSessionBean);
			}
			else if(headerInfo.isFirstVisit()) {  // CAP-38156 - FirstVisit not happened for Review

				response.setSuccess(false);
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.LOAD_ORDER_NOTREADY_FOR_REVIEW_ERR));
			}
		}
		
		//CAP-48719 - get Expedited Flag and Earliest Delivery Date from XST303
		setEarliestDeliveryInfo(appSessionBean, volatileSessionBean, response ); 

		response.setValidateDueDateSetting(userSettings.getValidateOrderDueDate());//CAP-48961
		
		response.setAllowMultipleEmails(userSettings.isAlwUsrToEnterMultpleEmails());//CAP-50146
		response.setFiledeliveryOptions(populateFileDeliveryOption());//CAP-50471
		
		response.setDeliveryOptionsList(populateDeliveryOptionsList(userSettings, appSessionBean)); // CAP-50581
		
		Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class,
				appSessionBean.getCustomToken());
		order.populateWithLatest(headerInfo.getOrderID());
		response.setEfdOnly(order.isEDeliveryOnly());//CAP-50886
		return response;
	}

	@Override
	public boolean validateOrderHeaderInfo(OrderDetailsHeaderInfo headerInfo, Message msg, OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, BaseResponse response) throws AtWinXSException
	{
		Map<String, String> translationMap = getTranslationMap(appSessionBean);

		try
		{
			// CAP-38154 - AAB - only do validation in C1UX now
			if (validateFields(headerInfo, response, appSessionBean, translationMap, oeSessionBean))
			{
				if (response instanceof OrderHeaderInfoResponse)
				{
					((OrderHeaderInfoResponse)response).setValidAndComplete(true);
				}
				return true;
			}
		}
		catch (AtWinXSException e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}

	// CAP-49118 TH
	protected String getOrderDueDate(int orderID, AppSessionBean appSessionBean) throws AtWinXSException
	{
		// Add new method to check order due date if DTD is turned on - the code is bad for DTD that loads initial shippingInfo and always seems to use the earliest due date
		IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService
				.locate(appSessionBean.getCustomToken());
		OrderInfoShippingVO shippingVO = ordersService.getOrderInfoShipping(new OrderShippingVOKey(orderID));
		if (shippingVO.getOrderDueDate() == null)
		{
			return "";
		}
		else
		{
			String dateFormat = new StringBuilder(Util.getDateFormatForLocale(appSessionBean.getDefaultLocale())).toString();
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			
			return formatter.format(shippingVO.getOrderDueDate());	
		}
				
	}
	
	private Map<String, String> getTranslationMap(AppSessionBean appSessionBean) {
		Properties translationProps = translationService.getResourceBundle(appSessionBean, SFTranslationTextConstants.DELIVERY_INFO_VIEW_NAME); // CAP-38154 - changing to view name used
		return translationService.convertResourceBundlePropsToMap(translationProps);
	}
	
	//CAP-48719 - get Expedited Flag and Earliest Delivery Date from XST303
	protected void setEarliestDeliveryInfo(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OrderHeaderInfoResponse response) throws AtWinXSException {

		IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService
				.locate(appSessionBean.getCustomToken());
		OrderDueDateVO orderDueDateVO = ordersService.getOrderDueDate(volatileSessionBean.getOrderId());
		if (orderDueDateVO != null && orderDueDateVO.isExpeditedOrder()) {

			response.setExpeditedOrder(orderDueDateVO.isExpeditedOrder());
			response.setEarliestDueDate(
					Util.getStringFromDate(orderDueDateVO.getEarliestDeliveryDt(), appSessionBean.getDefaultLocale()));
		}
	}

	protected String getLabelFromTranslationMap(Map<String, String> translationMap, String fieldName)
	{
		String label = translationMap.get(fieldName);
		if (Util.isBlankOrNull(label)) {
			// if it's not in the map, it's going to be an English default anyway, so use Value instead
			label = DEFAULT_VALUE;
		}
		return label;
	}
	// CAP-40687 - add missing validation that isn't available in CP
	public boolean validateOrderTitle(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean,
			Map<String, String> translationMap) throws AtWinXSException
	{
		String label = getLabelFromTranslationMap(translationMap, ORDER_TITLE_FIELD_NAME);
		// CAP-42122 - check for enable first
		if ((headerInfo.isEnableOrderTitle()) && (headerInfo.isOrderTitleReq()) && (Util.isBlankOrNull(headerInfo.getOrderTitle()))) {
				response.setFieldMessage(ORDER_TITLE_FIELD_NAME, label + AtWinXSConstant.BLANK_SPACE +
						translationService.processMessage(appSessionBean.getDefaultLocale(),
								appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
				return false;
		}
		else if ((!Util.isBlankOrNull(headerInfo.getOrderTitle())) && (headerInfo.getOrderTitle().length() > ORDER_TITLE_MAX_SIZE)) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ORDER_TITLE_MAX_SIZE + AtWinXSConstant.EMPTY_STRING);
				response.setFieldMessage(ORDER_TITLE_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
				return false;
		}
		else if (!validWCSSCharWithErrorPopulation(response, ORDER_TITLE_FIELD_NAME,
				label, headerInfo.getContactName(), appSessionBean.getDefaultLocale(), 	appSessionBean.getCustomToken()))
		{
			return false;
		}

		return true;
	}

	// CAP-40687 - add missing validation that isn't available in CP
	public boolean validateContactName(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean, Map<String, String> translationMap)
			throws AtWinXSException
	{
		String label = getLabelFromTranslationMap(translationMap, CONTACT_NAME_LABEL_KEY);

		if (Util.isBlankOrNull(headerInfo.getContactName()))
		{
			response.setFieldMessage(CONTACT_NAME_FIELD_NAME, label + AtWinXSConstant.BLANK_SPACE +
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
			return false;
		}
		else if ((!Util.isBlankOrNull(headerInfo.getContactName())) && (headerInfo.getContactName().length() > CONTACT_NAME_MAX_SIZE)) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, CONTACT_NAME_MAX_SIZE + AtWinXSConstant.EMPTY_STRING);
				response.setFieldMessage(CONTACT_NAME_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
				return false;
		}
		else if (!validWCSSCharWithErrorPopulation(response, CONTACT_NAME_FIELD_NAME,
				label, headerInfo.getContactName(), appSessionBean.getDefaultLocale(), 	appSessionBean.getCustomToken()))
		{
			return false;
		}
		return true;
	}

	// CAP-40687 - add missing validation that isn't available in CP
	public boolean validateContactEmail(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean, boolean allowMultipleEmails, Map<String, String> translationMap)
			throws AtWinXSException
	{
		boolean hasInvalidSingleEmail = hasInvalidSingleEmail(headerInfo, allowMultipleEmails);
		boolean hasInvalidSingleEmailEnteredInMultipleEmailField = hasInvalidSingleEmailButAllowsMultiple(headerInfo, allowMultipleEmails);
		String label = getLabelFromTranslationMap(translationMap, CONTACT_EMAIL_LABEL_KEY);
		if (Util.isBlankOrNull(headerInfo.getContactEmail()))
		{
			response.setFieldMessage(CONTACT_EMAIL_FIELD_NAME, label + AtWinXSConstant.BLANK_SPACE +
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
			return false;
		}
		else if (headerInfo.getContactEmail().length() > CONTACT_EMAIL_MAX_SIZE) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, CONTACT_EMAIL_MAX_SIZE + AtWinXSConstant.EMPTY_STRING);
				response.setFieldMessage(CONTACT_EMAIL_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
				return false;
		}
		else if (!validWCSSCharWithErrorPopulation(response, CONTACT_EMAIL_FIELD_NAME,
				label, headerInfo.getContactEmail(), appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken()))
		{
			return false;
		}
		else if (hasInvalidSingleEmail || hasInvalidSingleEmailEnteredInMultipleEmailField)
		{
			response.setFieldMessage(CONTACT_EMAIL_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_EMAIL_FORMAT_ERROR));
			return false;
		}
		else if ((allowMultipleEmails) && (Util.isEmailDelimited(headerInfo.getContactEmail())))
		{
			String formattedEmail = parseDelimitedEmails(headerInfo.getContactEmail());
			if (formattedEmail == null)
			{
				response.setFieldMessage(CONTACT_EMAIL_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_EMAIL_FORMAT_ERROR));
				return false;
			}
			headerInfo.setContactEmail(formattedEmail);
		}

		return true;
	}

	private boolean hasInvalidSingleEmailButAllowsMultiple(OrderDetailsHeaderInfo headerInfo, boolean allowMultipleEmails) {
		return ((!Util.isBlankOrNull(headerInfo.getContactEmail()))
				&& (allowMultipleEmails) && (!Util.isEmailDelimited(headerInfo.getContactEmail()))
				&& (!Util.isValidEmailFormat(headerInfo.getContactEmail())));
	}

	private boolean hasInvalidSingleEmail(OrderDetailsHeaderInfo headerInfo, boolean allowMultipleEmails) {
		return ((!Util.isBlankOrNull(headerInfo.getContactEmail())) && (!allowMultipleEmails) && (!Util.isValidEmailFormat(headerInfo.getContactEmail())));
	}

	// CAP-38154 - copied from OEManageOrdersComponent
	/**
	 * This method will validate the phone number entered
	 *
	 * @param errMsgs
	 * @param contactPhone
	 * @return
	 */
	public boolean validateContactPhone(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean, Map<String, String> translationMap) throws AtWinXSException
	{
		String label = getLabelFromTranslationMap(translationMap, CONTACT_PHONE_LABEL_KEY);
		if (Util.isBlankOrNull(headerInfo.getContactPhone()))
		{
			response.setFieldMessage(CONTACT_PHONE_FIELD_NAME, label + AtWinXSConstant.BLANK_SPACE +
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
			return false;
		}
		else
		{
			String formattedPhone = formatPhone(headerInfo.getContactPhone());
			if (formattedPhone == null)
			{
				response.setFieldMessage(CONTACT_PHONE_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_PHONE_FORMAT_ERROR));
				return false;
			}
			else if (formattedPhone.length() > OrderEntryConstants.PHONE_CHARS_MAX_LENGTH)
			{
				response.setFieldMessage(CONTACT_PHONE_FIELD_NAME, translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_PHONE_LENGTH_ERROR));
				headerInfo.setContactPhone(formattedPhone);
				return false;
			}
			else
			{
				headerInfo.setContactPhone(formattedPhone);
			}

		}

		return true;
	}


	// CAP-40687 - add missing validation that isn't available in CP
	public boolean validatePONumber(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean, String poMask, String poLabel,
			String poValidationOption) throws AtWinXSException
	{
		// initially, use the required validation in CP
		if ((!headerInfo.isAutoGenPO()) && (Util.isBlankOrNull(headerInfo.getPoNumber())))
		{
			response.setFieldMessage(PO_NUMBER_FIELD_NAME, poLabel + AtWinXSConstant.BLANK_SPACE +
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
			return false;
		}
		else if (!Util.isBlankOrNull(headerInfo.getPoNumber()))
		{
			if (headerInfo.isAllowPONumberEdit() && (headerInfo.getPoNumber().length() > PO_NUMBER_MAX_SIZE))
			{
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, PO_NUMBER_MAX_SIZE + AtWinXSConstant.EMPTY_STRING);
				response.setFieldMessage(PO_NUMBER_FIELD_NAME,
						getTranslation(appSessionBean, SFTranslationTextConstants.MAX_CHARS_ERR, SFTranslationTextConstants.MAX_CHARS_DEF_ERR,
								replaceMap));
				return false;
			}
			else if (!Util.isBlankOrNull(poMask))
			{
			   	try {
			   		MaskFormatter maskUtil = new MaskFormatter(poMask);
					maskUtil.setRequireLiteralsOnEntry(true);
					maskUtil.setLocale(appSessionBean.getDefaultLocale());
					headerInfo.setPoNumber((String) maskUtil.stringToValue(headerInfo.getPoNumber()));
			   	}
				catch (ParseException pe)
				{
					Map<String, Object> replaceMap = new HashMap<>();
					replaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_PO_LABEL, poLabel);
					replaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_EXCEPTION_MSG, pe.getMessage());
					response.setFieldMessage(PO_NUMBER_FIELD_NAME, getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_OD_VALIDATOR_PO_EXCEPTION_MSG,
							"{rep_po_num} is not a valid format. {rep_exception_msg}",
							replaceMap));
					return false;
				}
			}

			if (!validWCSSCharWithErrorPopulation(response, PO_NUMBER_FIELD_NAME,
					poLabel, headerInfo.getPoNumber(), appSessionBean.getDefaultLocale(), 	appSessionBean.getCustomToken()))
			{
				return false;
			}
			else if (!validateWCSSOption(poValidationOption, headerInfo.getPoNumber(),
					headerInfo.getCorporateNumber(), headerInfo.getOverrideCorpToNum(),
					headerInfo.getSoldToNumber(), headerInfo.getOverrideSoldToNum(), OrderEntryConstants.FIELD_ID_PO_NUMBER))
			{
				response.setFieldMessage(PO_NUMBER_FIELD_NAME, poLabel + AtWinXSConstant.BLANK_SPACE +
						getTranslation(appSessionBean, SFTranslationTextConstants.NOT_VALID_ERR,
						SFTranslationTextConstants.INVALID_FLD_VAL_DEF_ERR_MSG));
				return false;
			}

		}
		return true;
	}

	// CAP-40687 - add missing validation that isn't available in CP
	public boolean validateHeaderCustRefs(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean)
			throws AtWinXSException
	{
		boolean allGood = true;
		if ((headerInfo.getOrderDetailsHeaderCustRefs() != null) && (!headerInfo.getOrderDetailsHeaderCustRefs().isEmpty()))
		{
			Collection<OrderDetailsHeaderCustRef> headerCustRefs = headerInfo.getOrderDetailsHeaderCustRefs();
			for (OrderDetailsHeaderCustRef ref : headerCustRefs)
			{
				if (!validBlankCustRef(ref))
				{
					response.setFieldMessage(ref.getCustRefCode(), ref.getCustRefLabel() + AtWinXSConstant.BLANK_SPACE +
							translationService.processMessage(appSessionBean.getDefaultLocale(),
									appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
					allGood = false;
				}
				else if (!Util.isBlankOrNull(ref.getSelectedCustRef()))
				{
					if ((!validCustRefFieldForLength(ref, appSessionBean, response)) // CAP-41412
						|| (!validWCSSCharWithErrorPopulation(response, ref.getCustRefCode(), ref.getCustRefLabel(), ref.getSelectedCustRef(),
							appSessionBean.getDefaultLocale(), 	appSessionBean.getCustomToken())))
					{
						allGood = false;
					}
					else if (!validateWCSSOption(ref.getCustRefOption(), ref.getSelectedCustRef(),
							headerInfo.getCorporateNumber(), headerInfo.getOverrideCorpToNum(),
							headerInfo.getSoldToNumber(), headerInfo.getOverrideSoldToNum(),
							ref.getCustRefCode().substring(1, ref.getCustRefCode().length())))
					{
						response.setFieldMessage(ref.getCustRefCode(), ref.getCustRefLabel() + AtWinXSConstant.BLANK_SPACE +
								getTranslation(appSessionBean, SFTranslationTextConstants.NOT_VALID_ERR,
								SFTranslationTextConstants.INVALID_FLD_VAL_DEF_ERR_MSG));
						allGood = false;
					}
					else if (!validateCustRefMask(ref, appSessionBean, response))
					{
						allGood = false;
					}

				}
			}
		}
		return allGood;
	}

	protected boolean validBlankCustRef(OrderDetailsHeaderCustRef ref) {
		return (!ref.isRequireCustRef()
				|| (OrderAdminConstants.SHOW_CUST_REF_DO_NOT_SHOW_CD.equals(ref.getShowCustRef()))
				|| (!Util.isBlankOrNull(ref.getSelectedCustRef())));
	}

	protected boolean validCustRefFieldForLength(OrderDetailsHeaderCustRef ref, AppSessionBean appSessionBean, BaseResponse response)
			throws AtWinXSException {
		if ((!OrderAdminConstants.SHOW_CUST_REF_DO_NOT_SHOW_CD.equals(ref.getShowCustRef()) 	// CAP-41412 - ignore length issues
				&& (ref.getSelectedCustRef().length() > ref.getCustRefLength()))) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ref.getCustRefLength() + AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage(ref.getCustRefCode(), translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
			return false;
		}
		return true;
	}

	protected boolean validateCustRefMask(OrderDetailsHeaderCustRef ref, AppSessionBean appSessionBean, BaseResponse response)
	{
		if (!Util.isBlankOrNull(ref.getCustRefMask()))
		{
		   	try {
		   		MaskFormatter maskUtil = new MaskFormatter(ref.getCustRefMask());
				maskUtil.setRequireLiteralsOnEntry(true);
				maskUtil.setLocale(appSessionBean.getDefaultLocale());
				ref.setSelectedCustRef((String) maskUtil.stringToValue(ref.getSelectedCustRef()));
		   	}
			catch (ParseException pe)
			{
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_PO_LABEL, ref.getCustRefLabel());
				replaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_EXCEPTION_MSG, pe.getMessage());
				response.setFieldMessage(ref.getCustRefCode(), getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_OD_VALIDATOR_PO_EXCEPTION_MSG,
						"{rep_po_num} is not a valid format. {rep_exception_msg}",
						replaceMap));
				return false;
			}
		}
		return true;
	}

	public boolean validateFields(OrderDetailsHeaderInfo headerInfo, BaseResponse response, AppSessionBean appSessionBean,
			Map<String, String> translationMap, OEOrderSessionBean oeSessionBean) throws AtWinXSException
	{
		// multiple fields missing this validation
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		boolean poValid = (userSettings != null) ? validatePONumber(headerInfo, response, appSessionBean, userSettings.getPoMask(),
				userSettings.getPoLabel(), userSettings.getPoNumberListOption()) :
					validatePONumber(headerInfo, response, appSessionBean, null, "PO Number", AtWinXSConstant.EMPTY_STRING);
		boolean titleValid = validateOrderTitle(headerInfo, response, appSessionBean, translationMap);
		boolean nameValid = validateContactName(headerInfo, response, appSessionBean, translationMap);
		boolean emailValid = validateContactEmail(headerInfo, response, appSessionBean,
				((userSettings != null) && (userSettings.isAlwUsrToEnterMultpleEmails())), translationMap);
		boolean phoneValid = validateContactPhone(headerInfo, response, appSessionBean, translationMap);
		boolean crValid = validateHeaderCustRefs(headerInfo, response, appSessionBean);
		boolean orderDetailValid = validateOrderDetailMessage(headerInfo, response, appSessionBean);
	
		return poValid && titleValid && nameValid && emailValid && phoneValid && crValid && orderDetailValid;
	}


	private void processOrderDetailsHeaderInfo(OrderDetailsHeaderInfo headerInfo, OrderHeaderInfoResponse response) throws IllegalAccessException, InvocationTargetException
	{
		OrderDetailsHeaderInfoC1UX orderDeatilsHeaderInfoC1UX = new OrderDetailsHeaderInfoC1UXImpl();

		BeanUtils.copyProperties(orderDeatilsHeaderInfoC1UX, headerInfo);

		response.setOrderDetailsHeaderInfo(orderDeatilsHeaderInfoC1UX);
	}
	//CAP-42170
	public void processOrderDetailsShippingInfo(OrderDetailsShippingInfo shippingInfo, OrderHeaderInfoResponse response, AppSessionBean appSessionBean,
			OEOrderSessionBean oeSessionBean, OrderDetailsHeaderInfo headerInfo) throws IllegalAccessException, InvocationTargetException, AtWinXSException {
		
		OrderDetailsShippingInfoC1UX orderDetailsShippingInfoC1UX = new OrderDetailsShippingInfoC1UXImpl();
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		BeanUtils.copyProperties(orderDetailsShippingInfoC1UX, shippingInfo);
		String shipMethodChangeMsgTxt = AtWinXSConstant.EMPTY_STRING;
		String shipMethodChangeMsgURL = AtWinXSConstant.EMPTY_STRING;
		String requiredSignatureCR = AtWinXSConstant.EMPTY_STRING;
		boolean isShowDefaultSignature = isShowDefaultSignatureRequired(headerInfo,userSettings);
		
		if (isShowDefaultSignature) {	
			requiredSignatureCR = userSettings.getDefaultSignatureRequired();
		}
		
		if (userSettings.isShowShipMethodChangeMsgInd()) {
			UserGroupOrderPropertiesVOKey key = new UserGroupOrderPropertiesVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getGroupName()); 
			IOrderAdmin orderAdmin = oaOrderAdminLocatorService.locate(appSessionBean.getCustomToken());
			String[] shipMethodChangeInfo = orderAdmin.getShipMethodChangeMsgInfo(key);
			if (shipMethodChangeInfo != null) {

				shipMethodChangeMsgTxt = shipMethodChangeInfo[0];
				shipMethodChangeMsgURL = shipMethodChangeInfo[1];	
			}
		}

		// CAP-44312
		String showThirdPartyAccountNumberCd = ModelConstants.OPTION_NO;
		if (userSettings.isThirdPartyOnChange()) {
			showThirdPartyAccountNumberCd = ModelConstants.OPTION_ON_CARRIER_CHANGE;
		} else if (userSettings.isShowThirdPartyAccountNumber()) {
			showThirdPartyAccountNumberCd = ModelConstants.OPTION_YES;
		}
		orderDetailsShippingInfoC1UX.setShowThirdPartyAccountNumber(showThirdPartyAccountNumberCd);
		orderDetailsShippingInfoC1UX.setCarrierServiceLevel(shippingInfo.getShippingServiceCode());
		orderDetailsShippingInfoC1UX.setCarrierServiceList(shippingInfo.getShippingServiceMethods());	
		orderDetailsShippingInfoC1UX.setShowDefaultSignature(isShowDefaultSignature);
		orderDetailsShippingInfoC1UX.setRequiredSignatureCR(requiredSignatureCR);
		orderDetailsShippingInfoC1UX.setMessageText(shipMethodChangeMsgTxt);
		orderDetailsShippingInfoC1UX.setCarrierUrl(shipMethodChangeMsgURL);	
		orderDetailsShippingInfoC1UX.setShowMessageOnCarrierChange(userSettings.isShowShipMethodChangeMsgInd());
		// CAP-44840
		orderDetailsShippingInfoC1UX.setShowRequestedShipDate(userSettings.isAllowRequestedShipDate());
		
		//CAP-46294
		orderDetailsShippingInfoC1UX.setShowOrderDueDate(userSettings.isEnableOrderDueDateInd());
		orderDetailsShippingInfoC1UX.setOrderDueDate(shippingInfo.getOrderDueDate());
		orderDetailsShippingInfoC1UX.setOrderDueDateRequired(userSettings.isEnableOrderDueDateRequiredInd() || userSettings.isEnableOrderDueDateShipNowLater());
		orderDetailsShippingInfoC1UX.setShowShipNowLater(userSettings.isEnableOrderDueDateShipNowLater());
		
		//CAP-49122
		orderDetailsShippingInfoC1UX.setShowSpecialShippingInstructions(userSettings.isShowSpecialInstructions());
		orderDetailsShippingInfoC1UX.setSpecialShippingInstructions(shippingInfo.getSpecialInstructions());		
		
		response.setOrderDetailsShippingInfo(orderDetailsShippingInfoC1UX);
	}

	public boolean isShowDefaultSignatureRequired(OrderDetailsHeaderInfo headerInfo,
			OEResolvedUserSettingsSessionBean userSettings) {
		return userSettings.isSignatureRequiredOnCarrierChange() && userSettings.getDefaultSignatureRequired() != null &&
				!headerInfo.getOrderDetailsHeaderCustRefs().isEmpty()
				&&  (headerInfo.getOrderDetailsHeaderCustRefs().stream().anyMatch
						(custRef -> custRef.getCustRefCode().equalsIgnoreCase(userSettings.getDefaultSignatureRequired())));
	}

	private void processOrderOfDisplay(OrderDetailsHeaderInfo headerInfo, OrderHeaderInfoResponse response,OEOrderSessionBean oeSessionBean, SessionContainer sc) throws AtWinXSException
	{
		
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		List<List<String>> orderHeaderInfoDisplayOrder = new ArrayList<>();

		List<String> contactInfo = new ArrayList<>();
		contactInfo.add(CONTACT_NAME_FIELD_NAME);
		contactInfo.add(CONTACT_EMAIL_FIELD_NAME);
		contactInfo.add(CONTACT_PHONE_FIELD_NAME);
		
		orderHeaderInfoDisplayOrder.add(contactInfo);

		List<String> headerFields = new ArrayList<>();
		//CAP-41853 Fix OT display.
		if(headerInfo.isEnableOrderTitle())
		{
			headerFields.add(ORDER_TITLE_FIELD_NAME);
		}

		//isHasPONumberMappedFromFile is out of scope for now but will need to be added later. The rest of the scenarios are front end logic. We will always
		//show poNumber if !isAutoGenPO().
		if(!headerInfo.isAutoGenPO()/*oeSessionBean.isHasPONumberMappedFromFile()*/)
		{
			headerFields.add(PO_NUMBER_FIELD_NAME);
		}

		orderHeaderInfoDisplayOrder.add(headerFields);

		List<String> custRefFields = new ArrayList<>();
		for(OrderDetailsHeaderCustRef headerCustRef : headerInfo.getOrderDetailsHeaderCustRefs())
		{
			//CAP-41852 Added CustomerRefList & CustRefSettings null check.
			if(!headerCustRef.getShowCustRef().equalsIgnoreCase("N") && null != headerCustRef.getCustRefSettings() &&
					((null != headerCustRef.getCustomerRefList() && !headerCustRef.getCustomerRefList().isEmpty() && (headerCustRef.getCustRefSettings().isLabel() || headerCustRef.getCustRefSettings().isDropdown())) || headerCustRef.getCustRefSettings().isTextBox()))
			{
				custRefFields.add(headerCustRef.getCustRefCode());
			}
		}

		orderHeaderInfoDisplayOrder.add(custRefFields);

		// CAP-42169 - Add order header message fields
		List<String> orderHeaderMessageFields = new ArrayList<>();
		if (null != headerInfo.getOrderDetailsMessages() && CollectionUtils.isNotEmpty(headerInfo.getOrderDetailsMessages().getOrderDetailsHeaderMessages()))
		{
			for (OrderMessageComposite orderHeaderMessage : headerInfo.getOrderDetailsMessages().getOrderDetailsHeaderMessages())
			{
				// Display all messages except hidden messages
				if (!orderHeaderMessage.getDisplaySetting().equalsIgnoreCase(HIDDEN))
				{
					orderHeaderMessageFields.add(OHM + orderHeaderMessage.getNoteSegmentID());
				}
			}
		}
		orderHeaderInfoDisplayOrder.add(orderHeaderMessageFields);
		
		List<String> orderShippingInfoFields = processOrderOfDisplayShippingInfo(headerInfo, response, userSettings, sc);
		
		orderHeaderInfoDisplayOrder.add(orderShippingInfoFields);
		response.setOrderHeaderInfoDisplayOrder(orderHeaderInfoDisplayOrder);
	}
	
	public List<String> processOrderOfDisplayShippingInfo(OrderDetailsHeaderInfo headerInfo,
			OrderHeaderInfoResponse response, OEResolvedUserSettingsSessionBean userSettings, SessionContainer sc) throws AtWinXSException {
		//CAP-42170
		List<String> orderShippingInfoFields = new ArrayList<>();

		if (response.getOrderDetailsShippingInfo().isShowServiceLevelField() && 
				!response.getOrderDetailsShippingInfo().getCarrierServiceList().isEmpty()) {
			orderShippingInfoFields.add(CARRIER_SERVICE_LEVEL_FIELD_NAME);
		}
		
		if (userSettings.isShowThirdPartyAccountNumber()) {
			orderShippingInfoFields.add(THIRD_PARTY_ACCOUNT_FIELD_NAME);
		}
		if (isShowDefaultSignatureRequired(headerInfo, userSettings)) {
			orderShippingInfoFields.add(response.getOrderDetailsShippingInfo().getRequiredSignatureCR());
		}
		if (userSettings.isShowShipMethodChangeMsgInd()) {
			orderShippingInfoFields.add(MESSAGE_TEXT_FIELD_NAME);
			orderShippingInfoFields.add(CARRIER_URL_FIELD_NAME);
		}
		//CAP-46108
		if(userSettings.isAllowRequestedShipDate())
        {
            orderShippingInfoFields.add(REQUESTED_SHIP_DATE_FIELD_NAME);
        }
		//CAP-49122
		if(userSettings.isShowSpecialInstructions())
		{
			orderShippingInfoFields.add(SPECIAL_INSTRUCTIONS_FIELD_NAME);
		}
		
		//CAP-49048
		if(userSettings.isEnableOrderDueDateInd())
		{
			orderShippingInfoFields.add(ORDER_DUE_FIELD_NAME);
		}
		
		//CAP-49216
		IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService
				.locate(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		OrderDueDateVO orderDueDateVO = ordersService
				.getOrderDueDate(sc.getApplicationVolatileSession().getVolatileSessionBean().getOrderId());
		if(null!=orderDueDateVO && orderDueDateVO.isExpeditedOrder()) {
				orderShippingInfoFields.add(EXPEDITE_MANUFACTURING);
				orderShippingInfoFields.add(EXPEDITE_DATE);
				orderShippingInfoFields.add(ORIGINAL_DATE);
		}
		
		return orderShippingInfoFields;
	}

	// CAP-38156 - load Extended Item Quantity
	protected void loadExtendedItemQuantity(SessionContainer sc, int orderID, OrderHeaderInfoResponse response, AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException {

		OECheckoutAssembler cAssembler = new OECheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

		// Get Extended Item Quantity for checkout Review Summary
		OEExtendedQuantityResponseBean extendedQuantityBean=null;

		try	{

			extendedQuantityBean = cAssembler.getExtendedQuantitySummary(orderID, oeSessionBean, appSessionBean, appSessionBean.getDefaultTimeZone());
		}
		catch (Exception e) {

			//unhandled error or if getExtendedQuantitySummary() returns empty items
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CANNOT_COMPLETE_REQUEST));
			response.setSuccess(false);
		}

		if (extendedQuantityBean!=null) {
			
			Map<String, String> reqEmail=new HashMap<>();
			String reqEmailAddr="";
			
			OEExtendedItemQuantityResponseBean[] extendedItemQuantity = extendedQuantityBean.getItems();
			// Parse and fix C1UX display for full text UOM Code with locale translation and C1UX no image path
			for (OEExtendedItemQuantityResponseBean item : extendedItemQuantity) {
				item.setItemExtendedSellPrice(item.getItemTotalPrice());//CAP-50471
				//CAP-49278
				reqEmailAddr=getRequiredEmailAddress(appSessionBean, item);
				if(!reqEmailAddr.isBlank()) {
					reqEmail.put(reqEmailAddr, reqEmailAddr);
				}
				getEfdEmailAddressesAndEfdDeliveryTypes(sc,item);
				
				// full text UOM Code with translation
				String tmpUOMCodeFactor = item.getUOMCode() + ModelConstants.UOM_FACTOR_FORMAT_WITHSLASH
						+ item.getUOMFactor();

				item.setUOMCode(
						ItemUtility.getUOMAcronyms(
								tmpUOMCodeFactor.replace(ModelConstants.UOM_FACTOR_FORMAT_WITHSLASH,
										AtWinXSConstant.BLANK_SPACE + translationService.processMessage(
												appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
												TranslationTextConstants.TRANS_NM_OF_LBL) + AtWinXSConstant.BLANK_SPACE),
								true, appSessionBean));

				// Item Image path must reference C1UX no image instead of CP no image
				String itemImagePath = item.getItemImageURL();
				if (Util.isBlankOrNull(itemImagePath) || ModelConstants.CP_NO_IMAGE.equalsIgnoreCase(itemImagePath)
						|| itemImagePath.contains(ModelConstants.CP_NO_IMAGE_NO_CONTEXT)) {
					item.setItemImageURL(ModelConstants.C1UX_NO_IMAGE_MEDIUM);
				}
			}
			response.setRequiredEmailAddress(new ArrayList<>(reqEmail.values()));
			response.setExtendedItemQuantity(extendedItemQuantity);
			setEFDChargesDes(extendedItemQuantity,appSessionBean);//CAP-50471

		}
		else {
			// unhandled error or if getExtendedQuantitySummary() returns empty items
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CANNOT_COMPLETE_REQUEST));
			response.setSuccess(false);
		}

	}
	
	//CAP-50471//CAP-49811
	public void setEFDChargesDes(OEExtendedItemQuantityResponseBean[] extendedQuantityBeanItems, AppSessionBean asb) throws AtWinXSException {
		for (OEExtendedItemQuantityResponseBean shoppingcartlineitem : extendedQuantityBeanItems) {
			setEFDChargesCurrencyDesc(shoppingcartlineitem.getEfdCharges(), asb);
			double extPrice=shoppingcartlineitem.getItemExtendedCurrency().getAmountValue();
			double efdCharge=shoppingcartlineitem.getEfdCharge();
			if(extPrice<0) {
				shoppingcartlineitem.getItemExtendedCurrency().setAmountText("TBD");
				shoppingcartlineitem.setItemExtendedSellPrice("TBD");
			}
			setItemTotalPrice(shoppingcartlineitem,extPrice,efdCharge,asb);
		}
	}
	
	//CAP-49811
	public void setItemTotalPrice(OEExtendedItemQuantityResponseBean shoppingcartlineitem, double extPrice,
			double efdCharge, AppSessionBean asb) {
		if ((shoppingcartlineitem.getFileDeliveryMethod().equals("P") && extPrice < 0)
				|| (shoppingcartlineitem.getFileDeliveryMethod().equals("E") && efdCharge < 0)
				|| shoppingcartlineitem.getFileDeliveryMethod().equals("B") && (extPrice < 0 || efdCharge < 0)) {
			shoppingcartlineitem.getItemExtendedCurrency().setAmountText("TBD");
			shoppingcartlineitem.setItemExtendedSellPrice("TBD");
			shoppingcartlineitem.setItemTotalPrice("TBD");
		} else if (shoppingcartlineitem.getFileDeliveryMethod().equals("B") && (extPrice >= 0 && efdCharge >= 0)) {
			shoppingcartlineitem.setItemTotalPrice(
					Util.getStringFromCurrency(extPrice + efdCharge, asb.getCurrencyLocale(), false, 2)
							.getAmountText() + AtWinXSConstant.EMPTY_STRING);
		}
	}
	
	//CAP-50471
	public void setEFDChargesCurrencyDesc(Map<String, XSCurrency> edfcharges, AppSessionBean asb)
			throws AtWinXSException {
		if (edfcharges != null) {
			for (Entry<String, XSCurrency> entry : edfcharges.entrySet()) {
				entry.getValue().setCurrencyDesc(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), entry.getKey()));
			}
		}
	}

	protected OrderDetailsHeaderInfo loadHeaderInfo(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, List<String> errMsgs, String orderTitle) throws AtWinXSException
	{
		// populate Header Info
		OrderDetailsHeaderInfo headerInfo = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsHeaderInfo.class, appSessionBean.getCustomToken());
		try
		{
			headerInfo.populate(volatileSessionBean, appSessionBean, oeSessionBean, errMsgs);
		}
		catch (AtWinXSException e)
		{
			logger.error(e.getMessage());
		}

		// populate Customer Reference Details
		OrderDetailsCustRefs orderDetailsCustRefs = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsCustRefs.class, appSessionBean.getCustomToken());
		orderDetailsCustRefs.setOrderID(headerInfo.getOrderID());
		orderDetailsCustRefs.setUserOrderReferenceFields(headerInfo.getUserOrderReferenceFields());
		//CAP-41546
		OrderDetailsMessages orderDetailsMessages = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsMessages.class, appSessionBean.getCustomToken());
		orderDetailsMessages.setOrderDetailsHeaderMessages(headerInfo.getOrderDetailsMessages().getOrderDetailsHeaderMessages());
		try
		{
			orderDetailsCustRefs.populate(volatileSessionBean, appSessionBean, oeSessionBean, headerInfo, errMsgs);
			orderDetailsMessages.populate(oeSessionBean, appSessionBean, volatileSessionBean, orderTitle);
		}
		catch (AtWinXSException e)
		{
			logger.error(e.getMessage());
		}
		if (orderDetailsCustRefs.getBadCustRefSetupDetected() != null && !orderDetailsCustRefs.getBadCustRefSetupDetected().isEmpty())
		{
			headerInfo.setHasBadAdminCustRef(true);
			headerInfo.setBadCustRefSetupDetected(orderDetailsCustRefs.getBadCustRefSetupDetected());
		}
		headerInfo.setOrderDetailsHeaderCustRefs(orderDetailsCustRefs.getHeaderCustRefs());
		// CAP-41546
		headerInfo.setOrderDetailsMessages(orderDetailsMessages);
		// CAP-41729
		fixUneditableDropdownCustRefs(headerInfo.getOrderDetailsHeaderCustRefs());
		// CAP-41618/CAP-41619
		updateInfoSettingsForDeliverySelected(headerInfo, appSessionBean);
		return headerInfo;
	}
	//CAP-42170
	protected OrderDetailsShippingInfo loadShippingInfo(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, List<String> errMsgs, String orderTitle, Message msg, OrderDetailsHeaderInfo headerInfo) {
		OrderDetailsShippingInfo shippingInfo = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsShippingInfo.class, appSessionBean.getCustomToken());
		shippingInfo.setOrderID(headerInfo.getOrderID());
		shippingInfo.setScenarioNumber(headerInfo.getScenarioNumber());
		
		try
		{
			shippingInfo.populate(volatileSessionBean, appSessionBean, oeSessionBean, errMsgs, msg, orderTitle);
		}
		catch (AtWinXSException e)
		{
			logger.error(e.getMessage());
		}	
		return shippingInfo;
	}

	// CAP-41618/CAP-41619
	/**
	 * Method to update header info settings based on selected delivery method
	 * 
	 * @param headerInfo
	 * @param appSessionBean
	 * @throws AtWinXSException
	 */
	protected void updateInfoSettingsForDeliverySelected(OrderDetailsHeaderInfo headerInfo,
			AppSessionBean appSessionBean) throws AtWinXSException {

		OrderShippingVO shippingVo = getOrderShipping(appSessionBean.getCustomToken(), headerInfo.getOrderID());
		Collection<OrderDetailsHeaderCustRef> headerCustRefs = headerInfo.getOrderDetailsHeaderCustRefs();

		if (null != shippingVo && null != headerCustRefs && !headerCustRefs.isEmpty()) {
			boolean hasNoWcssAddress = Util.isBlankOrNull(shippingVo.getWcssShipToNum());
			for (OrderDetailsHeaderCustRef headerCustRef : headerInfo.getOrderDetailsHeaderCustRefs()) {
				if (!headerCustRef.isRequireCustRef() && headerCustRef.isForceCustRefOnNewShip() && hasNoWcssAddress) {
					headerCustRef.setRequireCustRef(true);
				}
			}
		}
	}

	// CAP-41729 - add method to fix the dropdown settings the way FE needs them
	protected void fixUneditableDropdownCustRefs(Collection<OrderDetailsHeaderCustRef> headerCustRefs) {
		if ((headerCustRefs != null) && (!headerCustRefs.isEmpty()))
		{
			for (OrderDetailsHeaderCustRef ref : headerCustRefs)
			{
				if ((!ref.isAllowCustRefEdit()) && (ref.getCustomerRefList() != null)
						&& (ref.getCustomerRefList().size() > 1) && (ref.getCustRefSettings() != null)
						&& (ref.getCustRefSettings().isDropdown()))
				{
					ref.setAllowCustRefEdit(true); // set this to be editable even though in CP it is false
				}
			}
		}
	}

	public OrderInfoHeaderSaveResponse saveOrderHeaderInfo(SessionContainer sc, OrderInfoHeaderSaveRequest request) throws AtWinXSException
	{
		OrderInfoHeaderSaveResponse response = new OrderInfoHeaderSaveResponse();

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		Message msg = new Message();
		List<String> errMsgs = new ArrayList<>();

		//CAP-41546
		String orderTitle = AtWinXSConstant.EMPTY_STRING;
		
		
		msg.setErrMsgItems(errMsgs); // this is required by the CP validator

		if (!validateOrder(response, sc, appSessionBean))
		{
			return response;
		}

		// populate Header Info
		OrderDetailsHeaderInfo headerInfo = this.loadHeaderInfo(appSessionBean, oeSessionBean, volatileSessionBean, errMsgs, orderTitle);
		//CAP-45036 added this line to generate the shippingInfo
		OrderDetailsShippingInfo shippingInfo = this.loadShippingInfo(appSessionBean, oeSessionBean, volatileSessionBean, errMsgs, orderTitle, msg, headerInfo);
		
		if (!errMsgs.isEmpty())
		{ // if bad admin, respond right away
			  response.setSuccess(false);
			  response.setMessage(getCombinedMessage(msg.getErrMsgItems()));
		}
		else
		{
			updateOrderHeaderInfo(request, headerInfo);
			if (validateOrderHeaderInfo(headerInfo, msg, oeSessionBean, appSessionBean,	volatileSessionBean, response)
					&& validateOrderShippingInfo(appSessionBean, response, oeSessionBean.getUserSettings(), headerInfo.getOrderID(), request)
					&& isRequestedShipDateValid(request.getRequestedShipDate(), response, appSessionBean, oeSessionBean.getUserSettings().isAllowRequestedShipDate()))
			{
				saveUpdatedHeaderInfo(headerInfo, response, appSessionBean, oeSessionBean, errMsgs);
				// Save Order Shipping Info
				// CAP-44313 // CAP-45036 - added another parameter shippingInfo // CAP-44840
				saveOrderShippingInfo(appSessionBean, headerInfo.getOrderID(), request, shippingInfo, oeSessionBean.getUserSettings());
				//CAP-48437
				updateExpeditedOrderDetails(appSessionBean, volatileSessionBean, request);
			}
			else
			{
				  response.setSuccess(false);
				  response.setMessage(getCombinedMessage(msg.getErrMsgItems()));
			}
		}
		return response;

	}

	// CAP-49122
	protected boolean hasValidSpecialInstructions(String specialInstruction, BaseResponse response,
			AppSessionBean appSessionBean) throws AtWinXSException {
		if (!Util.isBlankOrNull(specialInstruction) && specialInstruction.length() > 60) {
			response.setSuccess(false);
			response.setFieldMessage(
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), "sf.special_instructions_lbl"),
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), "sf.spl_instructions_excd_msg"));
			return false;
		}
		return Util.isBlankOrNull(specialInstruction) || specialInstruction.length() <= 60;
	}

	protected void updateOrderHeaderInfo(OrderInfoHeaderSaveRequest request, OrderDetailsHeaderInfo headerInfo) {
		headerInfo.setContactName(Util.nullToEmpty(request.getContactName()).trim());
		headerInfo.setContactEmail(Util.nullToEmpty(request.getContactEmail()).trim());
		headerInfo.setContactPhone(Util.nullToEmpty(request.getContactPhone()).trim());
		if (headerInfo.isAllowPONumberEdit()) {
			headerInfo.setPoNumber(Util.nullToEmpty(request.getPoNumber()).trim());
		}
		if ((headerInfo.isOrderTitleEnterable()) && (headerInfo.isEnableOrderTitle())) {
			headerInfo.setOrderTitle(Util.nullToEmpty(request.getOrderTitle()).trim());
		}
		updateHeaderCustRefs(request.getHeaderCustRefs(), headerInfo.getOrderDetailsHeaderCustRefs());
		updateOrderDetailsMessages(request.getHeaderMessages(), headerInfo.getOrderDetailsMessages().getOrderDetailsHeaderMessages());
	}

	protected boolean canUpdateHeaderCustRefs(GenericNameValuePair[] newRefValues,
			Collection<OrderDetailsHeaderCustRef> headerCustRefs) {
		return ((headerCustRefs != null) && (!headerCustRefs.isEmpty())
				&& (newRefValues != null) && (newRefValues.length > 0));
	}

	// CAP-41719 - refactored due to complexity
	protected boolean canUpdateHeaderCustRef(OrderDetailsHeaderCustRef ref)
	{
		return ((ref.isAllowCustRefEdit()) || ((ref.getCustomerRefList() != null) && (ref.getCustomerRefList().size() > 1)));
	}
	protected void updateHeaderCustRef(OrderDetailsHeaderCustRef ref, GenericNameValuePair[] newRefValues)
	{
		for (GenericNameValuePair update : newRefValues)
		{
			if (ref.getCustRefCode().equals(update.getName()))
			{
				ref.setSelectedCustRef(Util.nullToEmpty(update.getValue()).trim());
			}
		}
	}

	// CAP-41546
	protected void updateOrderDetailsMessage(OrderMessageComposite orderDetailMessages,
			GenericNameValuePair[] newRefValues) {

		String noteSegmentIdStr = String.valueOf(orderDetailMessages.getNoteSegmentID());
		for (GenericNameValuePair update : newRefValues) {
			if (update.getName().startsWith(OHM)){
				String name = update.getName();
				String noteSegmentId = name.substring(3, name.length());	
				if (noteSegmentId.equals(noteSegmentIdStr)){
					orderDetailMessages.setOrderMessageText(update.getValue().trim());				
				}
			}
		}
	}
	
	protected boolean canDefaultHeaderCustRef(OrderDetailsHeaderCustRef ref)
	{
		// this will assume canUpdateHeaderCustRef is FALSE - do NOT call this without calling it first
		return ((Util.isBlankOrNull(ref.getSelectedCustRef()))
				&& (ref.getCustomerRefList() != null) && (ref.getCustomerRefList().size() == 1));
	}
	
	protected void defaultHeaderCustRef(OrderDetailsHeaderCustRef ref)
	{
		if ((ref.getCustomerRefList() != null) && (!ref.getCustomerRefList().isEmpty()))
		{
			for (OrderReferenceFieldList val : ref.getCustomerRefList())
			{
				if (val.isPrimaryDefault() || ref.getCustomerRefList().size() == 1)
				{
					ref.setSelectedCustRef(val.getReferenceFieldValue());
				}
			}
		}
	}

	protected void defaultOrderMessage(OrderMessageComposite orderMessages) {
		if (orderMessages != null) {
			orderMessages.setOrderMessageText(orderMessages.getDefaultValue());
			}

		}
	protected void updateHeaderCustRefs(GenericNameValuePair[] newRefValues,
			Collection<OrderDetailsHeaderCustRef> headerCustRefs) {
		if (canUpdateHeaderCustRefs(newRefValues, headerCustRefs))
		{
			for (OrderDetailsHeaderCustRef ref : headerCustRefs)
			{
				// CAP-41719 - Lindsay said customers are setting up admin to be not editable but have a list
				if (canUpdateHeaderCustRef(ref))
				{
					updateHeaderCustRef(ref, newRefValues);
				}
				// CAP-41719 - only look up default on non-editable cust refs if there is no current value
				else if (canDefaultHeaderCustRef(ref))
				{
					defaultHeaderCustRef(ref);
				}
			}
		}
	}
	//CAP-41546
	protected void updateOrderDetailsMessages(GenericNameValuePair[] newRefValues,
			Collection<OrderMessageComposite> orderMessageComposite) {
		if (null != newRefValues && null != orderMessageComposite) {// made change to perform null check //CAP-41546 08/07/2023 added null check
			for (OrderMessageComposite message : orderMessageComposite) {
				defaultOrderMessage(message);
				if (message.getDisplaySetting().equals(SHOW)) {
					updateOrderDetailsMessage(message, newRefValues);
				}
			}
		}
	}

	protected void saveUpdatedHeaderInfo(OrderDetailsHeaderInfo headerInfo, OrderInfoHeaderSaveResponse response, AppSessionBean appSessionBean,
			OEOrderSessionBean oeSession, List<String> errMsgs) throws AtWinXSException {
		try {
			OrderDetails orderDetails = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetails.class,
					appSessionBean.getCustomToken());
			orderDetails.saveC1UXHeaderDetails(headerInfo, appSessionBean, oeSession, errMsgs);
			response.setSuccess(true);
		}
		catch(Exception e)
		{
			  logger.error("Error saving updated header info", e);
			  response.setSuccess(false);
			  response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}
	}

	// CAP-38154 - converted from CP OEManageOrdersComponent
	protected boolean validateWCSSOption(String validationOption, String value, String corpNr, String overrideCorpNr,
			String soldToNum, String overrideSoldToNum, String wcssCode) throws AtWinXSException {
		if ((OrderEntryConstants.WCSS_FIELD_OPTION_VALIDATE.equalsIgnoreCase(validationOption)) ||
			(OrderEntryConstants.WCSS_FIELD_OPTION_LIST.equalsIgnoreCase(validationOption)))
		{
			return validateWCSSInfo(value, wcssCode,
					(!Util.isBlankOrNull(overrideCorpNr)) ? overrideCorpNr : corpNr,
					(!Util.isBlankOrNull(overrideSoldToNum)) ? overrideSoldToNum : soldToNum);
		}
		return true;
	}

	protected boolean validateWCSSInfo(String value, String fieldCode, String corporateNumber, String soldToNumber) throws AtWinXSException
	{
	    if (!Util.isBlankOrNull(value))
		{
			WcssFieldValidationDAO dao = new WcssFieldValidationDAO();
			return dao.valueExists(corporateNumber, soldToNumber, fieldCode, value);
		}
		return true;
	}
	//CAP-41546
	public boolean validateOrderDetailMessage(OrderDetailsHeaderInfo headerInfo, BaseResponse response,
			AppSessionBean appSessionBean) throws AtWinXSException {
		
		boolean isValid = true;	
		if (null != headerInfo.getOrderDetailsMessages()
				&& null != headerInfo.getOrderDetailsMessages().getOrderDetailsHeaderMessages() // made change to perform null check //CAP-41546 08/07/2023 added null check
				&& !headerInfo.getOrderDetailsMessages().getOrderDetailsHeaderMessages().isEmpty()) {
			Collection<OrderMessageComposite> orderMessages = headerInfo.getOrderDetailsMessages()
					.getOrderDetailsHeaderMessages();
			for (OrderMessageComposite orderMessageComposite : orderMessages) {
				if (orderMessageComposite.getDisplaySetting().equals(SHOW)) {	
					isValid = isValid && validWCSSCharWithErrorPopulation(response, OHM + orderMessageComposite.getNoteSegmentID(),
							orderMessageComposite.getMessageLabel(), orderMessageComposite.getOrderMessageText(),
							appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken()) 
							&& validOrderHeaderMessageForLength(orderMessageComposite, response, appSessionBean);
				}	
			}
		}	
		return isValid;

	}
	protected boolean validOrderHeaderMessageForLength(OrderMessageComposite orderMessage, BaseResponse response, AppSessionBean appSessionBean) 
			throws AtWinXSException {

		boolean isValid = true;
		if (orderMessage.getOrderMessageText().length() > orderMessage.getNoteSegmentMaxTextLength()) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, orderMessage.getNoteSegmentMaxTextLength() +
					AtWinXSConstant.EMPTY_STRING);
			response.setFieldMessage(OHM + orderMessage.getNoteSegmentID(), translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
			isValid = false;

		}
		return isValid;

	}	
	
	// CAP-41551 CAP-44313 CAP-45036
	// CAP-44840 - Refactor this method to reduce parameters
	protected void saveOrderShippingInfo(AppSessionBean appSessionBean, int orderId, OrderInfoHeaderSaveRequest request, 
			OrderDetailsShippingInfo shippingInfo, OEResolvedUserSettingsSessionBean userSettings) throws AtWinXSException {
		OrderShipping orderShipping = objectMapFactoryService.getEntityObjectMap().getEntity(OrderShipping.class, appSessionBean.getCustomToken());
		orderShipping.populate(orderId);
		// CAP-44840
		String serviceTypeCd = getServiceTypeCode(request.getCarrierServiceLevel());
		String companyID = getCompanyId(request.getCarrierServiceLevel());
		String freightCode = getFreightCode(request.getCarrierServiceLevel());
		//CAP-45036
		List<NameValuePair> carrierServiceList = shippingInfo.getShippingServiceMethods();
		String[] carrierList = null;
		orderShipping.setServiceTypeDescriptionTxt(AtWinXSConstant.EMPTY_STRING);
		for (NameValuePair carrierLst : carrierServiceList) {
			if (carrierLst.getValue().toString().indexOf(";") > 1) {
				carrierList = carrierLst.getValue().toString().split(";");
				if (carrierList[0].equals(serviceTypeCd) && carrierList[1].equals(companyID)
						&& carrierList[2].equals(freightCode)) {
					orderShipping.setServiceTypeDescriptionTxt(carrierLst.getName());
					break;
				}
			} else if (carrierLst.getValue().toString().indexOf(";") == -1
					&& serviceTypeCd.equals(carrierLst.getValue())) {
				orderShipping.setServiceTypeDescriptionTxt(carrierLst.getName());
			}
		}
		//CAP-45036 -end
		orderShipping.setServiceTypeCd(serviceTypeCd);
		orderShipping.setCompanyID(companyID);
		orderShipping.setThirdPartyShipperNum(request.getThirdPartyAccount());
		orderShipping.setFreightTermsCd(freightCode); // CAP-44313
		//CAP-49122
		orderShipping.setSpecialInstructionsTxt(request.getSpecialInstruction());
		
		// CAP-44840
		if (userSettings.isAllowRequestedShipDate()) {
			orderShipping.setRequestedShipDate(formatDateString(request.getRequestedShipDate()));
		}
		
		//CAP-46294 - set & save OrderDueDate
		if (userSettings.isEnableOrderDueDateInd() || userSettings.isEnableOrderDueDateShipNowLater()) {
			orderShipping.setOrderDueDate(formatDateString(request.getOrderDueDate()));
		}
		
		orderShipping.save(AtWinXSConstant.INVALID_ID, appSessionBean.getLoginID());
	}
	
	// CAP-46294 - added OrderDueDate Validation method call
	// CAP-41551 // CAP-44840 - Refactor this method to reduce parameters
	protected boolean validateOrderShippingInfo(AppSessionBean appSessionBean, BaseResponse response,
			OEResolvedUserSettingsSessionBean userSettings, int orderId, OrderInfoHeaderSaveRequest request) throws AtWinXSException {
		OrderShippingVO shippingVo = getOrderShipping(appSessionBean.getCustomToken(), orderId);
		Map<String, String> translationMap = getTranslationMap(appSessionBean);
		boolean isShippingInfoValid = true;
		// CAP-44840
		String serviceTypeCd = getServiceTypeCode(request.getCarrierServiceLevel());
		String companyID = getCompanyId(request.getCarrierServiceLevel());
		// CAP-44313
		if (!Util.isBlankOrNull(serviceTypeCd) && !isValidCarrierCodeForPOBox(companyID) && isAddressHasPOBox(shippingVo)) {
			String carrierServiceLevelLabel = getLabelFromTranslationMap(translationMap, CARRIER_SERVICE_LEVEL_FIELD_NAME);				
			response.setFieldMessage(CARRIER_SERVICE_LEVEL_FIELD_NAME, carrierServiceLevelLabel + AtWinXSConstant.BLANK_SPACE + 
					getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_INVALID_SERVICE_LEVEL_ERROR,
							SFTranslationTextConstants.TRANS_NM_INVALID_SERVICE_LEVEL_ERROR));
			isShippingInfoValid = false;				
		}

		if (!validWCSSCharWithErrorPopulation(response, THIRD_PARTY_ACCOUNT_FIELD_NAME,
				TranslationTextConstants.TRANS_NM_THIRD_PARTY_LBL, request.getThirdPartyAccount(), appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken())) {
			isShippingInfoValid = false;
		}
		
		//not having a bug for c1 - with jira not working - CAP-44103
		boolean isThirdPartyShown = (userSettings.isThirdPartyOnChange() || userSettings.isShowThirdPartyAccountNumber()) && userSettings.isAllowCarrierMethodSelection();
        boolean isThirdPartyRequired = userSettings.isReqThirdPartyAccountNumber();
        boolean isCarrierServiceNotStandard = !Util.isBlankOrNull(serviceTypeCd) || !Util.isBlankOrNull(companyID);
        
        if (isThirdPartyShown && isThirdPartyRequired && isCarrierServiceNotStandard && Util.isBlankOrNull(request.getThirdPartyAccount()))  // Checks if the service or company is not standard
        {
            String label = getLabelFromTranslationMap(translationMap, THIRD_PARTY_ACCOUNT_FIELD_NAME);
            response.setFieldMessage(THIRD_PARTY_ACCOUNT_FIELD_NAME, label + AtWinXSConstant.BLANK_SPACE +
                    translationService.processMessage(appSessionBean.getDefaultLocale(),
                            appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
            isShippingInfoValid = false;
        }

		if (FEDX.equalsIgnoreCase(companyID) && !Util.isBlankOrNull(request.getThirdPartyAccount())
				&& !OrderEntryUtil.isValidFedExAcct(request.getThirdPartyAccount())) {
			response.setFieldMessage(THIRD_PARTY_ACCOUNT_FIELD_NAME, getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_INVALID_THIRD_PARTY_ACCT_NUM_ERROR,
							SFTranslationTextConstants.TRANS_NM_INVALID_THIRD_PARTY_ACCT_NUM_ERROR));
			isShippingInfoValid = false;
		}
		
		isShippingInfoValid = validateThirdPartyAccountLength(appSessionBean, response, userSettings, companyID, request.getThirdPartyAccount(), isShippingInfoValid);

		// CAP-44313 - Added extra validation for service code - if any ; characters left, return invalid due to bad carrier
		if (isShippingInfoValid)
		{
			isShippingInfoValid = serviceTypeCd.indexOf(";") == -1;
		}
		
		isShippingInfoValid = validateOrderDueDate(appSessionBean, response, userSettings, 
				request.getOrderDueDate(), isShippingInfoValid, translationService);
		
		isShippingInfoValid = isShippingInfoValid && hasValidSpecialInstructions(request.getSpecialInstruction(),response,appSessionBean);//CAP-49122
		
		return isShippingInfoValid;

	}
	
	// CAP-48782 TH
	private boolean validateOrderDueDate(AppSessionBean appSessionBean, BaseResponse response,
			OEResolvedUserSettingsSessionBean userSettings, String date,
			boolean isShippingInfoValid, TranslationService  translationService) throws AtWinXSException {
		return validateOrderDueDate(appSessionBean, response, userSettings, date,
				isShippingInfoValid, translationService, false);
	}
	
	// CAP-46294 - added OrderDueDate Validation method
	// CAP-48782 TH - Added boolean isOrderDueDateCall to allow us to check correctly for order due date
	private boolean validateOrderDueDate(AppSessionBean appSessionBean, BaseResponse response,
			OEResolvedUserSettingsSessionBean userSettings, String date,
			boolean isShippingInfoValid, TranslationService  translationService, boolean isOrderDueDateCall) throws AtWinXSException {
		
		boolean isOrderDueDateRequired = userSettings.isEnableOrderDueDateRequiredInd();
		boolean isOrderDueDateInd = userSettings.isEnableOrderDueDateInd();
		String label = getTranslation(appSessionBean, 
					SFTranslationTextConstants.ORDER_DUE_DATE_LBL, SFTranslationTextConstants.ORDER_DUE_DATE_VAL);
			
		// CAP-48782 TH
		if(isOrderDueDateRequired && Util.isBlankOrNull(date) && !isOrderDueDateCall) {
	          
			response.setFieldMessage(ORDER_DUE_FIELD_NAME, label + AtWinXSConstant.BLANK_SPACE +
	                    translationService.processMessage(appSessionBean.getDefaultLocale(),
	                    		appSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR));
			isShippingInfoValid = false;
	    }
		else if(isOrderDueDateInd && !Util.isBlankOrNull(date)) {
			 
			if (!Util.isDate(date, DATE_PATTERN, true)) {
						
					response.setFieldMessage(ORDER_DUE_FIELD_NAME,
							translationService.processMessage(appSessionBean.getDefaultLocale(),
									appSessionBean.getCustomToken(), SFTranslationTextConstants.INVALID_ORDER_DUE_DATE_ERROR));
					isShippingInfoValid = false;
			} 
			else if (Util.datePastDays(date, 1, DATE_PATTERN, appSessionBean.getDefaultLocale())) {
					
					Map<String, Object> replaceMap = new HashMap<>();
					replaceMap.put(SFTranslationTextConstants.REP_DATE_FIELD_TAG,
							(label != null) ? label : DEFAULT_VALUE);
					response.setFieldMessage(ORDER_DUE_FIELD_NAME,
							translationService.processMessage(appSessionBean.getDefaultLocale(), 
							appSessionBean.getCustomToken(), SFTranslationTextConstants.PAST_DATE_ERROR_LBL, replaceMap));
					isShippingInfoValid = false;
			}
		}
	
		return isShippingInfoValid;
	}

	private boolean validateThirdPartyAccountLength(AppSessionBean appSessionBean, BaseResponse response,
			OEResolvedUserSettingsSessionBean userSettings, String companyID, String thirdPartyAccount,
			boolean isShippingInfoValid) {
		if (userSettings.isAllowCarrierMethodSelection() && userSettings.isShowThirdPartyAccountNumber()
				&& !Util.isBlankOrNull(thirdPartyAccount)) {
			if (companyID.equalsIgnoreCase(UPSS) && thirdPartyAccount.length() != 6) {
				response.setFieldMessage(THIRD_PARTY_ACCOUNT_FIELD_NAME, getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_ACCTNUM_LENGTH_UPS_MSG,
								SFTranslationTextConstants.TRANS_NM_ACCTNUM_LENGTH_UPS_MSG));
				isShippingInfoValid = false;
			} else if ((companyID.equalsIgnoreCase(FEDX) || companyID.equalsIgnoreCase(FDEF)
					|| companyID.equalsIgnoreCase(FEDG3) || companyID.equalsIgnoreCase(FEDGP)
					|| companyID.equalsIgnoreCase(FEDGC)) && thirdPartyAccount.length() != 9) {
				response.setFieldMessage(THIRD_PARTY_ACCOUNT_FIELD_NAME, getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_ACCTNUM_LENGTH_FEDEX_MSG,
								SFTranslationTextConstants.TRANS_NM_ACCTNUM_LENGTH_FEDEX_MSG));
				isShippingInfoValid = false;
			}
		}
		return isShippingInfoValid;
	}
	
	// CAP-41551
	private boolean isAddressHasPOBox(OrderShippingVO shippingVo) {
		StringBuilder sbValidateShipAddress = new StringBuilder();
		sbValidateShipAddress.append(shippingVo.getShipToLineOneAd());
		sbValidateShipAddress.append(shippingVo.getShipToLineTwoAd());
		sbValidateShipAddress.append(shippingVo.getShipToAttentionTxt());
		String validateShipAddress = Util.replace(sbValidateShipAddress.toString(), ".", "");
		validateShipAddress = Util.replace(validateShipAddress, " ", "");

		return validateShipAddress.toUpperCase().indexOf(POBOX) > -1;
	}
	
	// CAP-41551
	private OrderShippingVO getOrderShipping(CustomizationToken token, int orderId) throws AtWinXSException {
		IOEManageOrdersComponent manageOrdersComponent = objectMapFactoryService.getComponentObjectMap()
				.getComponent(IOEManageOrdersComponent.class, token);
		return manageOrdersComponent.getOrderShipping(orderId);
	}
	
	// CAP-41551
	private boolean isValidCarrierCodeForPOBox(String companyID) throws AtWinXSException
	{
		List<String> carrierCodes = new ArrayList<>();
		XSProperties oeProperty = PropertyUtil.getProperties(AtWinXSConstant.PROP_ORDER_ENTRY);
		String ccodes = oeProperty.getProperty(CARRIER_CODES);
		if(!Util.isBlankOrNull(ccodes))
		{
			StringTokenizer stk = new StringTokenizer(ccodes, "|");
			while (stk.hasMoreTokens()) 
			{
				carrierCodes.add(stk.nextToken());
			}
		}
		return carrierCodes.contains(companyID);
	}
	
	protected void processShowPaymentInfo(OrderHeaderInfoResponse response, SessionContainer sc) throws AtWinXSException {
		OrderDetailsBillingInfo billingInfo = objectMapFactoryService.getEntityObjectMap().getEntity(
				OrderDetailsBillingInfo.class, sc.getApplicationSession().getAppSessionBean().getCustomToken());
		billingInfo.populate(sc.getApplicationVolatileSession().getVolatileSessionBean(),
				sc.getApplicationSession().getAppSessionBean(),
				((OrderEntrySession) sc.getModuleSession()).getOESessionBean());

		if (!billingInfo.isCreditCardRequired() && !billingInfo.isCreditCardOptional()) {
			response.getOrderDetailsHeaderInfo().setShowpayment(false);
		} else if ((billingInfo.isCreditCardRequired() || billingInfo.isCreditCardOptional())) {
			response.getOrderDetailsHeaderInfo().setShowpayment(true);
		}
	}
	
	protected void processDTDServiceCall(OEOrderSessionBean oeSessionBean, OrderHeaderInfoResponse response) {
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();

		response.setCallDtdService(userSettings.isEnableOrderDueDateShipNowLater() || !"N".equalsIgnoreCase(userSettings.getValidateOrderDueDate()));

	}
	
	// CAP-44840
	protected boolean isRequestedShipDateValid(String date, BaseResponse response, AppSessionBean appSessionBean, boolean isAllowRequestedShipDate) throws AtWinXSException {
		boolean isValid = true;
		String label = getTranslation(appSessionBean, SFTranslationTextConstants.REQUESTED_SHIP_DATE_LBL, SFTranslationTextConstants.REQUESTED_SHIP_DATE_VAL);
		if (isAllowRequestedShipDate && !Util.isBlankOrNull(date)) {//CAP-44840 - added null check along with the blank check, skip validation if it does not allow requested ship date
			if (!Util.isDate(date, DATE_PATTERN, true)) {
				response.setFieldMessage(REQUESTED_SHIP_DATE_FIELD_NAME,
						label + AtWinXSConstant.BLANK_SPACE + buildInvalidRequestedShipDateError(
								appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken()));
				isValid = false;
			} else {
				if (Util.datePastDays(date, 1, DATE_PATTERN, appSessionBean.getDefaultLocale())) {
					Map<String, Object> replaceMap = new HashMap<>();
					replaceMap.put(SFTranslationTextConstants.REP_DATE_FIELD_TAG,
							(label != null) ? label : DEFAULT_VALUE);
					response.setFieldMessage(REQUESTED_SHIP_DATE_FIELD_NAME,
							translationService.processMessage(appSessionBean.getDefaultLocale(),
									appSessionBean.getCustomToken(), SFTranslationTextConstants.PAST_DATE_ERROR_LBL,
									replaceMap));
					isValid = false;
				}
			} 
		}
		return isValid;
	}
	
	private String buildInvalidRequestedShipDateError(Locale locale, CustomizationToken token) throws AtWinXSException {
		StringBuilder builder = new StringBuilder();
		Map<String,Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.DATE_FORMAT_TAG, DATE_PATTERN);
		builder.append(translationService.processMessage(locale, token, SFTranslationTextConstants.INVALID_REQ_SHIP_DATE_ERROR_LBL));
		builder.append(translationService.processMessage(locale, token, SFTranslationTextConstants.INVALID_DATE_ERROR_LBL));
		builder.append(translationService.processMessage(locale, token, SFTranslationTextConstants.DATE_FORMAT_LBL, replaceMap));
		return builder.toString();
	}
	
	private Date formatDateString(String stringDate) {
		Date date = null;
		if (!Util.isBlankOrNull(stringDate)) { //CAP-44840 - added null check along with the blank check
			date = Util.formatDateString(stringDate, DATE_PATTERN);
		}
		return date;
	}
	// CAP-41551 CAP-44313
	private String getServiceTypeCode(String carrierServiceLevel) {
		String serviceTypeCd = AtWinXSConstant.EMPTY_STRING;
		if (!Util.isBlankOrNull(carrierServiceLevel)) {
			String[] values = carrierServiceLevel.split(";");
			if (values.length == 3) {
				serviceTypeCd = values[0];
			} else {
				// CAP-44313
				serviceTypeCd = carrierServiceLevel;
			}

		}
		return serviceTypeCd;
	}
	
	private String getCompanyId(String carrierServiceLevel) {
		String companyID = AtWinXSConstant.EMPTY_STRING;
		if (!Util.isBlankOrNull(carrierServiceLevel)) {
			String[] values = carrierServiceLevel.split(";");
			if (values.length == 3) {
				companyID = values[1];
			}
		}
		return companyID;
	}
	private String getFreightCode(String carrierServiceLevel) {
		String freightCode = AtWinXSConstant.EMPTY_STRING;
		if (!Util.isBlankOrNull(carrierServiceLevel)) {
			String[] values = carrierServiceLevel.split(";");
			if (values.length == 3) {
				freightCode = values[2];
			}
		}
		return freightCode;
	}

	// CAP-48274 // CAP-48457 - Begin
	public DateToDestinationResponse doDateToDestination(SessionContainer sc, DateToDestinationRequest request)
			throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();

		DateToDestinationResponse response = new DateToDestinationResponse();

		if (!isCallDateToDestinationService(userSettings)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		if (!validateOrder(response, sc, appSessionBean)) {
			return response;
		}

		boolean shipDateReplacesOrderDueDate = isShipDateReplacesOrderDueDate(oeSessionBean, userSettings);

		String orderDate = request.getOrderDueDate();
		String requestedShipDate = request.getRequestedShipDate();

		// CAP-48782 TH
		boolean isValidOrderDueDate = validateOrderDueDate(appSessionBean, response, userSettings, orderDate, true, translationService, true);
		boolean isValidRequestedShipDate = isRequestedShipDateValid(requestedShipDate, response, appSessionBean, userSettings.isAllowRequestedShipDate());
		response.setSuccess(isValidOrderDueDate && isValidRequestedShipDate);

		if (response.isSuccess()) {
			DueDateValidator dueDateValidator = objectMapFactoryService.getEntityObjectMap()
					.getEntity(DueDateValidator.class, appSessionBean.getCustomToken());
			dueDateValidator.validate(volatileSessionBean.getOrderId().intValue(), userSettings,
					oeSessionBean.getLoginID(), request.getServiceTypeCode(),
					Util.formatDateString(orderDate, DATE_PATTERN),
					Util.formatDateString(requestedShipDate, DATE_PATTERN), 
					false, appSessionBean.getDefaultLocale(),
					Util.isBlankOrNull(requestedShipDate) && Util.isBlankOrNull(orderDate), shipDateReplacesOrderDueDate);

			processDateToDestinationValidation(sc, response, shipDateReplacesOrderDueDate, orderDate, requestedShipDate,
					dueDateValidator);
		}

		return response;
	}
	
	//CAP-48437
	public void updateExpeditedOrderDetails(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OrderInfoHeaderSaveRequest request) throws AtWinXSException {
		
		IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService.locate(appSessionBean.getCustomToken());
	    OrderDueDateVO orderDueDateVO = ordersService.getOrderDueDate(volatileSessionBean.getOrderId());
		    
	  //CP-13410: [PDN] if Expedited Order is available, save info to XST303_ORD_DUE_DT table 
		if (orderDueDateVO!=null)
		{
			if (request.isExpediteOrder())
			{
				//update the vo with the expedited order flag and fee
				orderDueDateVO.setExpeditedOrder(request.isExpediteOrder());
				orderDueDateVO.setExpeditedOrderFee(request.getExpediteOrderFee());
			}
			else
			{
				//update the vo with the expedited order flag and fee
				orderDueDateVO.setExpeditedOrder(false);
				orderDueDateVO.setExpeditedOrderFee(0);
			}
		    // call the service that will persist the update 
		    ordersService.updateOrderDueDate(orderDueDateVO);					    
		}
	}

	protected void processDateToDestinationValidation(SessionContainer sc, DateToDestinationResponse response,
			boolean shipDateReplacesOrderDueDate, String orderDate, String requestedShipDate,
			DueDateValidator dueDateValidator) throws AtWinXSException {
		response.setDueDateValidator(dueDateValidator);
		response.setDateToDestionationAvailable(true);
		setDateToDestinationStatusAndMessage(response, dueDateValidator);

		boolean isOrderDueDateMet = isOrderDueDateMet(dueDateValidator);

		processOrderDueDateNotMet(sc, response, shipDateReplacesOrderDueDate, orderDate, requestedShipDate,
				dueDateValidator, isOrderDueDateMet);
	}

	protected void processOrderDueDateNotMet(SessionContainer sc, DateToDestinationResponse response,
			boolean shipDateReplacesOrderDueDate, String orderDate, String requestedShipDate,
			DueDateValidator dueDateValidator, boolean isOrderDueDateMet) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();

		response.setShipNowOrLater(userSettings.isEnableOrderDueDateShipNowLater());

		if (!isOrderDueDateMet) {
			Date earliestDueDate = dueDateValidator.getOrderDueDate();

			response.setPromptExpedite(true);

			String datePattern = DATE_PATTERN;
			boolean isConvertDateString = true;

			if (OrderEntryConstants.DUE_DATE_INFORM_ONLY.equals(userSettings.getValidateOrderDueDate())
					|| null != oeSessionBean.getApprovalSummaryInfo()) {
				datePattern = getDateFormatForLocale(appSessionBean);
				isConvertDateString = false;
			}

			if (isConvertDateString) {
				orderDate = convertDateStringToLocaleFormat(orderDate, appSessionBean, datePattern);
				requestedShipDate = convertDateStringToLocaleFormat(requestedShipDate, appSessionBean, datePattern);
			}

			boolean isCarrierSelectionEnabled = userSettings.isAllowCarrierMethodSelection();

			String orderDueDateForExpedite = orderDate;
			String orderDueDateForExpediteLbl = getTranslation(appSessionBean,
					TranslationTextConstants.TRANS_NM_ORDER_DUE_DATE_LBL,
					OrderEntryConstants.ORDER_DUE_DATE_DEFAULT_LABEL);
			if (shipDateReplacesOrderDueDate) {
				earliestDueDate = dueDateValidator.getRequestedShipDate();
				orderDueDateForExpedite = requestedShipDate;
				orderDueDateForExpediteLbl = getTranslation(appSessionBean,
						TranslationTextConstants.TRANS_NM_REQUESTED_SHIP_DATE_LBL,
						OrderEntryConstants.REQUESTED_SHIP_DATE_DEFAULT_LABEL);
			}

			OrderDueDateVO orderDueDateVO = dueDateValidator.getOrderDueDateVO();
			String originalOrderDueDate = getOriginalOrderDueDate(appSessionBean, earliestDueDate, orderDueDateVO);

			Map<String, Object> replaceMap = setupReplaceMap(orderDueDateForExpediteLbl, originalOrderDueDate);

			boolean isExpediteEnabled = isExpediteEnabled(userSettings);
			boolean expediteAllowed = isExpediteAllowed(orderDueDateVO, isExpediteEnabled);

			if (expediteAllowed) {
				processExpediteOrder(appSessionBean, volatileSessionBean, userSettings, response,
						orderDueDateForExpedite, orderDueDateVO, replaceMap);
			}

			StringBuilder earliestDueDateHeaderMsg = new StringBuilder(dueDateValidator.getDueDateNotMetMsg());
			String earliestDueDateTranslationKey = getEarliestDueDateTranslationKey(shipDateReplacesOrderDueDate,
					isCarrierSelectionEnabled);
			earliestDueDateHeaderMsg.append(AtWinXSConstant.BLANK_SPACE);
			earliestDueDateHeaderMsg.append(getTranslation(appSessionBean, earliestDueDateTranslationKey,
					AtWinXSConstant.EMPTY_STRING, replaceMap));
			response.setExpediteMessage(earliestDueDateHeaderMsg.toString());
			response.setExpediteOriginalDateMessage(
					getTranslation(appSessionBean, OrderEntryConstants.EXPEDITE_ORDER_UNAVAIL_EARLIEST_DATE_LBL,
							AtWinXSConstant.EMPTY_STRING, replaceMap));
		}
	}

	protected Map<String, Object> setupReplaceMap(String orderDueDateForExpediteLbl, String originalOrderDueDate) {
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(OrderEntryConstants.WCSS_RETURN_DATE, Util.nullToEmpty(originalOrderDueDate));
		replaceMap.put(OrderEntryConstants.ORD_DUE_DATE, orderDueDateForExpediteLbl);
		return replaceMap;
	}

	protected String getDateFormatForLocale(AppSessionBean appSessionBean) {
		return Util.getDateFormatForLocale(appSessionBean.getDefaultLocale());
	}

	protected String convertDateStringToLocaleFormat(String date, AppSessionBean appSessionBean, String datePattern) {
		return Util.convertDateStringToLocaleFormat(date, datePattern, appSessionBean.getDefaultLocale());
	}

	protected boolean isShipDateReplacesOrderDueDate(OEOrderSessionBean oeSessionBean,
			OEResolvedUserSettingsSessionBean userSettings) {
		return userSettings.isEnableOrderDueDateInd()
				&& oeSessionBean.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY;
	}

	protected boolean isCallDateToDestinationService(OEResolvedUserSettingsSessionBean userSettings) {
		return userSettings.isEnableOrderDueDateShipNowLater()
				|| (!Util.isBlankOrNull(userSettings.getValidateOrderDueDate())
						&& !OrderEntryConstants.DUE_DATE_NO.equalsIgnoreCase(userSettings.getValidateOrderDueDate()));
	}

	protected void setDateToDestinationStatusAndMessage(DateToDestinationResponse response,
			DueDateValidator dueDateValidator) {
		String statusCode = ModelConstants.DTD_STATUS_NO_MESSAGE;
		String informMessage = AtWinXSConstant.EMPTY_STRING;
		String warningMessage = AtWinXSConstant.EMPTY_STRING;
		String errorMessage = AtWinXSConstant.EMPTY_STRING;
		switch (dueDateValidator.getMessageType()) {
		case Success:
			statusCode = ModelConstants.DTD_STATUS_SUCCESS;
			informMessage = dueDateValidator.getValidationMessage();
			break;
		case Error:
		case Warning:
			if (dueDateValidator.getLastReturnReasonCode() == 0) {
				statusCode = ModelConstants.DTD_STATUS_WARNING;
				warningMessage = dueDateValidator.getValidationMessage();
			} else {
				statusCode = ModelConstants.DTD_STATUS_ERROR;
				errorMessage = dueDateValidator.getValidationMessage();
			}
			break;
		default:
			break;
		}

		response.setStatusCode(statusCode);
		response.setInformMessage(informMessage);
		response.setMessage(errorMessage);
		response.setWarningMessage(warningMessage);
	}

	protected boolean isExpediteAllowed(OrderDueDateVO orderDueDateVO, boolean isExpediteEnabled) {
		return null != orderDueDateVO && isExpediteEnabled;
	}

	protected String getEarliestDueDateTranslationKey(boolean shipDateReplacesOrderDueDate,
			boolean isCarrierSelectionEnabled) {
		String earliestDueDateTranslationKey = OrderEntryConstants.EXPEDITE_ORDER_UNAVAIL_POPUP_MSG1;
		if (isCarrierSelectionEnabled && !shipDateReplacesOrderDueDate) {
			earliestDueDateTranslationKey = OrderEntryConstants.EXPEDITE_ORDER_UNAVAIL_POPUP_MSG2;
		}
		return earliestDueDateTranslationKey;
	}

	protected void processExpediteOrder(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, DateToDestinationResponse response,
			String orderDueDateForExpedite, OrderDueDateVO orderDueDateVO, Map<String, Object> replaceMap)
			throws AtWinXSException {
		double expediteCharge = 0;
		String expediteDateStr = AtWinXSConstant.EMPTY_STRING;
		boolean isExpeditedOrderServiceEnabled = false;

		OrderShippingVO orderShippingVo = getOrderShipping(appSessionBean.getCustomToken(),
				volatileSessionBean.getOrderId());
		ExpeditedOrderService expeditedOrderService = getExpeditedOrderService(appSessionBean, volatileSessionBean,
				orderDueDateForExpedite, orderDueDateVO, orderShippingVo);

		try {
			// Call populate to call Expedite Order Date WCSS Service
			expeditedOrderService.populate();

			if (null == expeditedOrderService.getExpediteDate()) {
				isExpeditedOrderServiceEnabled = false;
			} else {
				expediteDateStr = getFormattedDate(expeditedOrderService.getExpediteDate(), appSessionBean);
				isExpeditedOrderServiceEnabled = true;
			}
			if (!expeditedOrderService.isSuccess()) {
				logger.error(expeditedOrderService.getErrorMessage());
			}
			expediteCharge = expeditedOrderService.getExpediteCharge();
		} catch (Exception e) {
			if (null == expeditedOrderService.getExpediteDate() || !expeditedOrderService.isSuccess()) // CP-13795
			{
				isExpeditedOrderServiceEnabled = false;
			} else {
				expediteDateStr = getFormattedDate(expeditedOrderService.getExpediteDate(), appSessionBean);
				isExpeditedOrderServiceEnabled = true;
			}
			logger.error(expeditedOrderService.getErrorMessage());
		}

		if (isExpeditedOrderServiceEnabled) {
			setExpediteData(appSessionBean, userSettings, response, replaceMap, expediteCharge, expediteDateStr);
		}
	}

	protected void setExpediteData(AppSessionBean appSessionBean, OEResolvedUserSettingsSessionBean userSettings,
			DateToDestinationResponse response, Map<String, Object> replaceMap, double expediteCharge,
			String expediteDateStr) {
		String expediteChargeStr = AtWinXSConstant.EMPTY_STRING;
		replaceMap.put(OrderEntryConstants.EXPEDITE_AMT, expediteChargeStr);
		String expediteServiceFeeMessageTranslationKey = OrderEntryConstants.EXPEDITE_ORDER_DISCLAIMER_WITHOUT_FEE_MSG;

		if (userSettings.isShowOrderLinePrice()) {
			XSCurrency expediteChargeCurrency = getStringFromCurrency(appSessionBean, expediteCharge);
			expediteChargeStr = expediteChargeCurrency.getAmountText();
			replaceMap.put(OrderEntryConstants.EXPEDITE_AMT, expediteChargeStr);
			expediteServiceFeeMessageTranslationKey = OrderEntryConstants.EXPEDITE_ORDER_DISCLAIMER_WITH_FEE_MSG;
		}

		String expediteServiceMessage = Util.nullToEmpty(getTranslation(appSessionBean,
				OrderEntryConstants.EXPEDITE_ORDER_REQ_TO_EXPEDITE, AtWinXSConstant.EMPTY_STRING));
		String expediteServiceFeeMessage = Util.nullToEmpty(getTranslation(appSessionBean,
				expediteServiceFeeMessageTranslationKey, AtWinXSConstant.EMPTY_STRING, replaceMap));

		response.setExpediteDate(expediteDateStr);
		response.setExpediteServiceMessage(expediteServiceMessage);
		response.setExpediteServiceFeeMessage(expediteServiceFeeMessage);
		response.setExpediteServiceCharge(expediteCharge);
	}

	protected XSCurrency getStringFromCurrency(AppSessionBean appSessionBean, double expediteCharge) {
		return Util.getStringFromCurrency(expediteCharge, appSessionBean.getCurrencyLocale(), false);
	}

	protected ExpeditedOrderService getExpeditedOrderService(AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean, String orderDueDateForExpedite, OrderDueDateVO orderDueDateVO,
			OrderShippingVO orderShippingVo) {
		ExpeditedOrderService expeditedOrderService = objectMapFactoryService.getEntityObjectMap()
				.getEntity(ExpeditedOrderService.class, appSessionBean.getCustomToken());

		expeditedOrderService.setStartDate(orderDueDateForExpedite);
		expeditedOrderService.setSoldToNumber(orderShippingVo.getSoldToNum());
		expeditedOrderService.setShipToNumber(orderShippingVo.getShipToNum());
		expeditedOrderService.setCorporateNumber(appSessionBean.getCorporateNumber());
		expeditedOrderService.setOrderID(volatileSessionBean.getOrderId());
		expeditedOrderService.setOrderDueDateVO(orderDueDateVO);
		expeditedOrderService.setToken(appSessionBean.getCustomToken());
		expeditedOrderService.setLocal(appSessionBean.getDefaultLocale());
		return expeditedOrderService;
	}

	protected boolean isExpediteEnabled(OEResolvedUserSettingsSessionBean userSettings) {
		return userSettings.getValidateOrderDueDate().equals(OrderEntryConstants.DUE_DATE_VALIDATE_CAN_EXPEDITE)
				|| userSettings.getValidateOrderDueDate().equals(OrderEntryConstants.DUE_DATE_INFORM_CAN_EXPEDITE);
	}

	protected String getOriginalOrderDueDate(AppSessionBean appSessionBean, Date earliestDueDate,
			OrderDueDateVO orderDueDateVO) {
		String originalOrderDueDate = getFormattedDate(earliestDueDate, appSessionBean); // CP-13795
		if (orderDueDateVO != null) {
			originalOrderDueDate = getFormattedDate(orderDueDateVO.getEarliestDeliveryDt(), appSessionBean);
		}
		return originalOrderDueDate;
	}

	protected boolean isOrderDueDateMet(DueDateValidator dueDateValidator) {
		boolean isOrderDueDateMet = dueDateValidator.isDueDateMet();
		if (dueDateValidator.getLastReturnReasonCode() == AtWinXSConstant.INVALID_ID
				|| dueDateValidator.getLastReturnReasonCode() == 316) {
			isOrderDueDateMet = true;
		}
		return isOrderDueDateMet;
	}

	protected String getFormattedDate(Date origDate, AppSessionBean appSessionBean) {
		String formattedDateStr = AtWinXSConstant.EMPTY_STRING;

		if (origDate != null) {
			String dateFormat = getDateFormatForLocale(appSessionBean);
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

			formattedDateStr = sdf.format(origDate);
		}

		return formattedDateStr;
	}
	// CAP-48457 - End
	
	
	// CAP-49278-EFD starts Here 
		public void getEfdEmailAddressesAndEfdDeliveryTypes(SessionContainer sc,OEExtendedItemQuantityResponseBean item) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		//CP-9865 Changed to use assembler factory
				OECheckoutAssembler assembler = oeAssemblerFactoryService.getCheckoutAssembler(volatileSessionBean,appSessionBean);
				
				OEOrderSummaryResponseBean oeSummaryResponseBean = assembler.getOrderSummary(appSessionBean, oeSessionBean, true);
				
		// CP-9215 - populate with EFD popup info
				EFDDestinationsFormBean efdFormBean = new EFDDestinationsFormBean();
				EFDDestinationOptionsFormBean oldEfdFormBean = assembler.getEFDFormInfo(appSessionBean, oeSessionBean, 
						volatileSessionBean.getOrderId().intValue());
				HashMap<String, String> ftpLocationMap = new HashMap<>(); 
				//CAP-161
				HashMap<String, String> selectedFtpLocationMap = new HashMap<>(); 

				Collection<String> emailRecipients = new ArrayList<>();
				if (oldEfdFormBean != null)
				{	
					List<String> errMsgs = new ArrayList<>();
					//CAP-430 Passed BaseSession
					efdFormBean.populate(volatileSessionBean, appSessionBean,  oeSessionBean, oldEfdFormBean, errMsgs, assembler, sc.getModuleSession());
					 
					if (!Util.isBlankOrNull(efdFormBean.getEmailTo()))
					{
						emailRecipients = Util.buildEmailList(efdFormBean.getEmailTo());
					}
					
					//CAP-7427 JBS change email recipient format if email address is single entry with names
					if(efdFormBean.getEfdEmailRecords() != null && //CAP-7960
							oeSessionBean.getUserSettings().isCanUserEditOwnEmail() &&
							oeSessionBean.getUserSettings().getAllowMultipleEmailsCode().equals(OrderAdminConstants.EFD_ALLOW_MULTIPLE_EMAIL_SINGLE_LINE))
					{
						emailRecipients.clear();
						//if single line, get info from emailRecords(XST472) instead of emailTo(XST239)
						for(EFDEmail efdEmail : efdFormBean.getEfdEmailRecords())
						{
							//format: email address (name)
							String emailRecipient = efdEmail.getEmailAddress();
							
							emailRecipient += !Util.isBlankOrNull(efdEmail.getEmailName()) ? " (" + efdEmail.getEmailName() + ")" : "";
							
							emailRecipients.add(emailRecipient);
						}
					}
					
					//CP-12640 RAR - Add Salesforce Email addresses.
					EFDCRMTracking efdCRMTracking = objectMapFactoryService.getEntityObjectMap().getEntity(EFDCRMTracking.class, appSessionBean.getCustomToken());
					efdCRMTracking.populate(volatileSessionBean.getOrderId().intValue());
					
					if(null != efdCRMTracking && (null != efdCRMTracking.getRecords() && !efdCRMTracking.getRecords().isEmpty()))
					{
						for(EFDCRMTrackingRecord rec : efdCRMTracking.getRecords())
						{
							emailRecipients.add(rec.getCrmEmailAddress());
						}
					}
					
					if (oldEfdFormBean.getFTPLocations() != null)
					{
						for (EFDFTPLocationFormBean location : oldEfdFormBean.getFTPLocations())
						{
							ftpLocationMap.put(String.valueOf(location.getFtpID()), location.getHostName() + " - " + location.getHostDescription());
						}
					}
					
					
					Map<String,String> types = new LinkedHashMap<>(); //CAP-1454
					Map<String,XSCurrency> efdCharges=new HashMap<>();
					for (OEExtendedItemQuantityResponseBean singleItem : oeSummaryResponseBean.getItems().getItems())
					{
					 if(item.getLineNumber().equalsIgnoreCase(singleItem.getLineNumber())) {//CAP-50482
							if (!OrderEntryConstants.PRINT_METHOD.equals(singleItem.getFileDeliveryOption()))
							{ // if this is EFD, find the info
							int count = 0; //CAP-1705
							
							for (EFDCartLine efdLine : efdFormBean.getEfdLines())
							{
								if (String.valueOf(efdLine.getLineNumber()).equals(singleItem.getLineNumber()))
								{ // if this is the same as the EFD line, set the info on the bean
									singleItem.setEfdDeliveryEmailAddresses(emailRecipients);
									item.setEfdDeliveryEmailAddresses(emailRecipients);	
								
									for (String efdMethod : efdLine.getDestination().getEFDMethod())
									{
										//CP-8970 changed token from String to an Object
										count++; //CAP-1705
										String label = getEFDEmailSourceLabel(efdMethod, appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken());
										if (efdMethod.indexOf(OrderEntryConstants.EFD_SOURCES_FTP) >= 0)
										{ // need to append the FTP destination
											String ftpID = String.valueOf(efdLine.getFtpDestinationID());
											if (ftpLocationMap.containsKey(ftpID))
											{
												label += " - " + ftpLocationMap.get(ftpID);
											}
											selectedFtpLocationMap.put(efdLine.getLineNumber()+"", efdLine.getFtpDestinationID()+"");//CAP-1455
											types.put(efdLine.getLineNumber()+"", label); //CAP-1454
										} else {
											types.put(efdLine.getLineNumber() + "_" + count, label); //CAP-1454 //CAP-1705
											
										}
									}
									
								}
								efdCharges=singleItem.getEfdCharges();
							}
							
						}
						item.setEfdCharges(efdCharges);
						item.setEfdDeliveryTypes(types);
					}
				  }	
				}
			
		}	
		
		
		//CP-8970 changed token from String to an Object
			public  String getEFDEmailSourceLabel(String method, Locale locale, CustomizationToken customToken) throws AtWinXSException
			{
				String label = null;

				if (!Util.isBlankOrNull(method))
				{
					if (method.indexOf(OrderEntryConstants.EFD_SOURCES_PDF) >= 0)
					{
						//CP-8970 changed token from String to an Object
						//CAP-14451 SRN Changed PDF label to Digital
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_DIGITAL_LBL);
					} else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_DIGIMAG) >= 0)
					{
						//CP-8970 changed token from String to an Object
						// CP-11880 8.1.4 [NKM] Added jsEncode for issue #92 of translation text issues
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_DIGIMAG_LBL); 
					} else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_JELLY_VISION) >= 0)
					{
						//CP-8970 changed token from String to an Object
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_OTHER_OE_LBL);
					} else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_STATIC_CONTENT) >= 0)
					{
						//CP-8970 changed token from String to an Object
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_STATIC_CONTENT_LBL);
					} else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_EDOC) >= 0)
					{
						//CP-8970 changed token from String to an Object
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_EDOC_LBL);
					} else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_EXACT_TARGET) >= 0)
					{
						//CP-8970 changed token from String to an Object
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_EXACT_TARGET_LBL);
					} else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_FTP) >= 0)
					{
						//CP-8970 changed token from String to an Object
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_FTP_LBL);
					}
					//CAP-10856 JBS
					else if (method.indexOf(OrderEntryConstants.EFD_SOURCES_EGC) >= 0)
					{
						label = translationService.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_EFD_SOURCE_EGC);
					}
				}
				return label;
			}
			
			
	public String	getRequiredEmailAddress(AppSessionBean appSessionBean, OEExtendedItemQuantityResponseBean item) throws AtWinXSException{
		
		String res="";
		ICatalog iCatalogMgr = cmCatalogComponentLocatorService.locate(appSessionBean.getCustomToken());
		CatalogDefaultVO catalogDefaultVo = null;
		catalogDefaultVo = iCatalogMgr.getCatalogDefaultWithEfdSettings(item.getVendorItemNumber(),item.getItemNumber(),appSessionBean.getSiteID());
		if(catalogDefaultVo.getEfdSourceSettings()!=null) {
			for(EFDSourceSetting edfSource: catalogDefaultVo.getEfdSourceSettings()) {
			  if(!edfSource.getRequiredEmailAddresses().isBlank()) {	
				  res=edfSource.getRequiredEmailAddresses();
			  }	
			}
		}	
		return res;
	}
		
	//CAP-49278 -EFD End here	
	
	
}