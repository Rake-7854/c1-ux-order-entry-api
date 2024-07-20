/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	04/23/24	C Codina			CAP-48623				Initial Version
 *	04/23/24	N Caceres			CAP-48821				Add method for saving Order Template
 *	04/26/24	C Codina			CAP-48890				C1UX BE - Create API to show list of template orders that the user can order from
 *  05/01/24	M Sakthi			CAP-48745				C1UX BE - Create new methods to start the save order template process
 *  05/02/24	A Salcedo			CAP-48890				Added missing order template name in response.
 *	04/25/24	S Ramachandran		CAP-48889				Added service method to delete an order template
 *	05/06/24	Satishkumar A		CAP-48975				C1UX BE - Create new API to actually load template order and to use in cart
 *	05/14/24	Krishna Natarajan	CAP-48582				Added missing field shipToAttention
 *  05/15/24	Krishna Natarajan	CAP-48745				Set template ID encrypted
 *  05/15/24	Krishna Natarajan	CAP-49427				Added a check on isShowBillToInfo - update the shared flag
 *  05/17/24	Krishna Natarajan	CAP-49465				Changed the constant while setting the template type
 *  05/21/24	Krishna Natarajan	CAP-49485				Adjusted the line number of order lines, in order template items list
 *  05/21/24	Krishna Natarajan	CAP-49537				updated logic to bring UOM full text for order lines, in order template items list
 *  05/23/24	Krishna Natarajan	CAP-48998				updated temporary logic to get the sharable boolean updated for no template
 */

package com.rrd.c1ux.api.services.orders.ordertemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.ordertemplate.COOrderLines;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.DeleteTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.LoadSaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.OrderTemplate;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.SaveOrderTemplateResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.TemplateOrderListResponse;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateRequest;
import com.rrd.c1ux.api.models.orders.ordertemplate.UseOrderTemplateResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.orderentry.locator.OESavedOrderComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchVO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOESavedOrderComponent;
import com.wallace.atwinxs.orderentry.admin.vo.OrderTemplateAttributeVO;
import com.wallace.atwinxs.orderentry.ao.OEOrderSharingFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplateDeleteFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplateDetailsFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplateFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplatesDeleteFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderTemplatesFormBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderLineResponseBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEReplacementsSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderTemplateHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderTemplateHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderTemplateOrderLineVO;

@Service
public class OrderTemplateServiceImpl extends BaseOEService implements OrderTemplateService {
	
	private final Logger logger = LoggerFactory.getLogger(OrderTemplateServiceImpl.class);
	
	private static final String TEMPLATE_ID_URL_PARAM = "&orderTemplateID=";
	 private final OESavedOrderComponentLocatorService oeSavedOrderComponentLocatorService;
	private static final String DATE_PATTERN = "MM/dd/yyyy";
	private static final String ORDER_TEMPLATE_ID_PARAM = "&orderTemplateID=";
	private static final String NEW_ORDER_TEMPLATE = "-1";

	protected OrderTemplateServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			OESavedOrderComponentLocatorService oeSavedOrderComponentLocatorService) {
		super(translationService, objectMapFactoryService);
		this.oeSavedOrderComponentLocatorService = oeSavedOrderComponentLocatorService;
	}

	@Override
	public TemplateOrderListResponse getTemplateOrderList(SessionContainer sc) throws AtWinXSException {
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		
		TemplateOrderListResponse response = new TemplateOrderListResponse();
		response.setSuccess(true);
		
		if (!userSettings.isShowTemplatesLink()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		List<OrderTemplate> orderTemplates = getOrderTemplateList(volatileSessionBean, oeOrderSessionBean, appSessionBean);
		
		response.setOrderTemplateCount(orderTemplates.size());
		response.setItemInCart(hasValidCart(sc));
		response.setOrderTemplates(orderTemplates);
		
		return response;
	}
	
	//CAP-48745
	@Override
	public LoadSaveOrderTemplateResponse getOrderTemplateDetails(SessionContainer sc,
			LoadSaveOrderTemplateRequest request) throws AtWinXSException {
		LoadSaveOrderTemplateResponse response = new LoadSaveOrderTemplateResponse();
	
		boolean hasOrdersService = sc.getApplicationSession().getAppSessionBean().hasService(AtWinXSConstant.ORDERS_SERVICE_ID); // PMO service	
		OEOrderSessionBean modulSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean usersetting = modulSessionBean.getUserSettings();
	
		if(!hasOrdersService || !usersetting.isShowTemplatesLink()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}	
		String selectedTemplateID="";
		
		try {
		  if(request.getOrderTemplateID().equals("-1")) {
			  selectedTemplateID = request.getOrderTemplateID();
		  }else {
			selectedTemplateID = Util.decryptString(request.getOrderTemplateID());
			selectedTemplateID = selectedTemplateID.replace(TEMPLATE_ID_URL_PARAM, "");
		  }
		}catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
					sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.INVALID_ORDER_TEMPLATES));
					
			return response;
		}
		
		OEOrderTemplateDetailsFormBean oeOrderTemplateDetailsFormBean = null;
		if (Integer.parseInt(selectedTemplateID) > -1) {
			oeOrderTemplateDetailsFormBean = getTemplateOrderDetailFromTemplateOrder(sc, selectedTemplateID);
		} else {
			oeOrderTemplateDetailsFormBean = getOrderDetailsBeanFromOrderSummary(sc);
		}

		setOeOrderTempBeanIntoRes(sc, oeOrderTemplateDetailsFormBean, response);
		return response;
	}
		
		
		
	public OEOrderTemplateDetailsFormBean getTemplateOrderDetailFromTemplateOrder(SessionContainer sc,
			String selectedTemplateID) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean modulSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OESavedOrderAssembler oeSavedOrderAssembler = new OESavedOrderAssembler(null, null, null, false, null);
	
		int tempID = Integer.parseInt(selectedTemplateID);
		IOESavedOrderComponent savedOrderComponent = oeSavedOrderComponentLocatorService
				.locate(appSessionBean.getCustomToken());
		OrderTemplateHeaderVOKey headervokey = new OrderTemplateHeaderVOKey(tempID);

		OEResolvedUserSettingsSessionBean usersetting = modulSessionBean.getUserSettings();
		// billing to address vo
		OrderAddressVO billingAddressvo = null;
		if (usersetting.isShowBillToInfo()) {
			billingAddressvo = savedOrderComponent.getTemplateBillingAddress(headervokey);
		}

		// shipping to address vo
		OrderAddressVO shippingAddressvo = savedOrderComponent.getTemplateShippingAddress(headervokey);
		OrderTemplateOrderLineVO[] vos = savedOrderComponent.getTemplateExtendedQuantitySummary(tempID);

		// determine if user can edit this template
		String templateUpdateLevel = usersetting.getTemplateUpdateLevel();
		boolean isEditable = OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_ALL.equalsIgnoreCase(templateUpdateLevel);
		
		OrderTemplateHeaderVO orderTemplateHeaderVO = savedOrderComponent.getTemplate(headervokey);
		String temtemplateName= orderTemplateHeaderVO.getTemplateName();
		isEditable = getEditable(isEditable, templateUpdateLevel, orderTemplateHeaderVO, appSessionBean);

		// CP-2948 Order Sharing - START
		OEOrderSharingFormBean orderSharingFormBean = null;

		if (usersetting.isAllowTeamOrderSharing()) {
			// CP-3323 [RBC] - Added profile number
			orderSharingFormBean = oeSavedOrderAssembler.getOECheckoutAssembler().buildOEOrderSharingFormBean(
					appSessionBean, modulSessionBean, -1, tempID, appSessionBean.getProfileNumber(), false);// CP-7457

		}
		// CP-2948 Order Sharing - END

		// [ECO] CP-4073 START - Check template order for EFD Only.
		boolean isEFDOnly = false;
		if (vos != null && vos.length > 0) {
			for (OrderTemplateOrderLineVO templateOrder : vos) {
				String fileDeliveryMethod = getFileDeliveryMethod(templateOrder.getEFDOption(),
						String.valueOf(templateOrder.getOrderQty()), templateOrder.getUomCode(),
						templateOrder.getEFDOption());
				isEFDOnly = checkEFDOnly(fileDeliveryMethod);
				if (!isEFDOnly) {
					break;
				}
			}
		}
		// CP-4073 END

		// populate OEOrderTemplateDetailsFormBean with VOs
		return new OEOrderTemplateDetailsFormBean(selectedTemplateID, temtemplateName, orderTemplateHeaderVO.getTemplateType(), billingAddressvo,
				shippingAddressvo, vos, isEditable, orderSharingFormBean, // CP-2948 Order Sharing
				// CP-3337 - Added templateUpdateLevel
				templateUpdateLevel,
				// CP-4073 [ECO] Added isEFDOnly.
				isEFDOnly);
	}
		
	public String getFileDeliveryMethod(String efdOption, String orderQty, String uomCode, String fileDeliveryMethod) {
		if (!Util.isBlankOrNull(efdOption)) {
			String fileDeliveryOption = efdOption;
			// CP-7037 EZL Added check condition for EFD_PRINT_ENABLED_OPTION
			if (fileDeliveryOption.equals(OrderEntryConstants.PRINT_EFD_ENABLED_OPTION)
					|| fileDeliveryOption.equals(OrderEntryConstants.EFD_PRINT_ENABLED_OPTION)
					|| fileDeliveryOption.equals(OrderEntryConstants.EFD_PRINT_OVERRIDE_OPTION)) // CP-8680 JRA
			{
				if (fileDeliveryMethod.equals(OrderEntryConstants.PRINT_EFD_METHOD)
						|| fileDeliveryMethod.equals(OrderEntryConstants.EFD_PRINT_METHOD)) {
					if (!Util.isBlankOrNull(orderQty) && !Util.isBlankOrNull(uomCode)) {

						if ("0".equals(orderQty)) {
							fileDeliveryMethod = OrderEntryConstants.EFD_METHOD;
						}
					} else {
						fileDeliveryMethod = OrderEntryConstants.EFD_METHOD;
					}
				} else {
					fileDeliveryMethod = OrderEntryConstants.PRINT_METHOD;
				}
			} else if (fileDeliveryOption.equals(OrderEntryConstants.EFD_ENABLED_OPTION)) {
				fileDeliveryMethod = OrderEntryConstants.EFD_METHOD;
			} else {
				fileDeliveryMethod = OrderEntryConstants.PRINT_METHOD;
			}
		}
		return fileDeliveryMethod;
	}

	public boolean checkEFDOnly(String fileDeliveryMethod){
		boolean isEfdOnly = true;
		if (!OrderEntryConstants.EFD_METHOD.equals(fileDeliveryMethod)) {
			isEfdOnly = false;
		}
		return isEfdOnly;
	}

	public boolean getEditable(boolean isEditable, String templateUpdateLevel,
			OrderTemplateHeaderVO orderTemplateHeaderVO,AppSessionBean appSessionBean)  {

		/* DTS 8759 Fix - allow users to edit their own templates */
		if (!isEditable && (OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_OWN.equalsIgnoreCase(templateUpdateLevel)
				|| OrderEntryConstants.TEMPLATE_UPDATE_LVL_NONE.equalsIgnoreCase(templateUpdateLevel))) {
			
			// get owner info
			int tempProfileNumber = orderTemplateHeaderVO.getProfileNum();

			// CP-3339 [RBC] - Template can be editable if it is team shared
			if (OrderEntryConstants.TEMPLATE_TYPE_SHARED_IND
					.equalsIgnoreCase(orderTemplateHeaderVO.getTemplateType())) {
				
				isEditable = true;
			} else if (tempProfileNumber > -1) {
				
				if (appSessionBean.getProfileNumber() == tempProfileNumber) {
					
					isEditable = true;
				}
			} else {
				// no profile, so check Login ID
				if (appSessionBean.getLoginID().equals(orderTemplateHeaderVO.getLoginID())) {
					
					isEditable = true;
				}
			}
		}
		return isEditable;
	}
		
	private OEOrderTemplateDetailsFormBean getOrderDetailsBeanFromOrderSummary(SessionContainer sc)
			throws AtWinXSException {
		OEOrderSessionBean moduleSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OESavedOrderAssembler oeSavedOrderAssembler = new OESavedOrderAssembler(
				sc.getApplicationVolatileSession().getVolatileSessionBean(), appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());

		// call component to get detail -
		OEOrderTemplateDetailsFormBean templateDetailBean = null;
		try {
			templateDetailBean = oeSavedOrderAssembler.getSavedOrderDetailForTemplate(appSessionBean.getSiteID(),
					moduleSessionBean, appSessionBean, appSessionBean.getDefaultTimeZone(),
					appSessionBean.getProfileNumber());

		} catch (AtWinXSMsgException me) {
			
			List<String> errMsgs = new ArrayList<>();
			errMsgs.add(me.getMessage());
		} catch (AtWinXSException me) {
			throw me;
		}

		return templateDetailBean;
	}
		
	public void setOeOrderTempBeanIntoRes(SessionContainer sc,
			OEOrderTemplateDetailsFormBean oeOrderTemplateDetailsFormBean, LoadSaveOrderTemplateResponse response) throws AtWinXSException {
		
		OEOrderSessionBean modulSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean usersetting = modulSessionBean.getUserSettings();

		List<COOrderLines> orderLine = new ArrayList<>();
		response.setOrderTemplateId(Util.encryptString(ORDER_TEMPLATE_ID_PARAM + oeOrderTemplateDetailsFormBean.getTemplateID()));//CAP-48745
		response.setOrderTemplateName(oeOrderTemplateDetailsFormBean.getTemplateName());
		response.setTemplateSharable(usersetting.isShowTemplatesLink());
		response.setEditable(oeOrderTemplateDetailsFormBean.isEditable());
		response.setShowVendorItem(sc.getApplicationSession().getAppSessionBean().showWCSSItemNumber());
		if(Integer.parseInt(oeOrderTemplateDetailsFormBean.getTemplateID()) == -1) {
			response.setShared(false);
			response.setTemplateSharable(!usersetting.getTemplateUpdateLevel().equals("0"));//CAP-48998
		}else {
			response.setShared(Util.yToBool(oeOrderTemplateDetailsFormBean.getTemplateType()));//CAP-49427
		}	
		response.setShipToName1(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getCustomerName1());
		response.setShipToName2(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getCustomerName2());
		response.setShipToAddress1(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getAddressLine1());
		response.setShipToAddress2(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getAddressLine2());
		response.setShipToAddress3(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getAddressLine3());
		response.setShipToCity(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getCity());
		response.setShipToState(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getState());
		response.setShipToZip(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getZip());
		response.setShipToCountry(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getCountry());
		response.setShipToAttention(oeOrderTemplateDetailsFormBean.getAddresses().getShippingAddress().getAttention());

		if(usersetting.isShowBillToInfo()) {//CAP-49427
		response.setBillToName1(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getCustomerName1());
		response.setBillToName2(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getCustomerName2());
		response.setBillToAddress1(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getAddressLine1());
		response.setBillToAddress2(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getAddressLine2());
		response.setBillToAddress3(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getAddressLine3());
		response.setBillToCity(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getCity());
		response.setBillToState(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getState());
		response.setBillToZip(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getZip());
		response.setBillToCountry(oeOrderTemplateDetailsFormBean.getAddresses().getBillingAddress().getCountry());
		}
		response.setCanCreateOrder(sc.getApplicationVolatileSession().getVolatileSessionBean().getShoppingCartCount()>0);
		
		OESavedOrderLineResponseBean[] ol = oeOrderTemplateDetailsFormBean.getOrderLines();

		for (int i = 0; i < ol.length; i++) {
			COOrderLines coOl = new COOrderLines();
			coOl.setLineNumber(String.format("%03d",i+1));//CAP-49485
			coOl.setItemDescription(ol[i].getItemDescription());
			coOl.setVendorItem(ol[i].getVendorItemNumber());
			coOl.setCustomerItemNumber(ol[i].getItemNumber());
			coOl.setQuantity(Integer.parseInt(ol[i].getOrderQuantity()));
			coOl.setUom(ItemUtility.getUOMAcronyms(ol[i].getUom(), false, sc.getApplicationSession().getAppSessionBean()));//CAP-49537
			orderLine.add(coOl);
		}
		
		response.setOrderLine(orderLine);
		response.setSuccess(true);
	}

	protected List<OrderTemplate> getOrderTemplateList(VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeOrderSessionBean, AppSessionBean appSessionBean) throws AtWinXSException
	{
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();

		String templateUpdateLevel = userSettings.getTemplateUpdateLevel();

		OESavedOrderAssembler assembler = getAssembler(appSessionBean, volatileSessionBean);

		OEOrderTemplatesFormBean templateOrders =
				assembler.getOrderTemplates(appSessionBean.getSiteID(), 
						appSessionBean.getLoginID(), 
						appSessionBean.getProfileNumber(), 
						appSessionBean.getBuID(), 
						ModelConstants.TEMPLATE_ORDER_TEMPLATE_NAME,
						templateUpdateLevel, 
						appSessionBean.getGroupName());

		List<OrderTemplate> orderTemplates = new ArrayList<>();

		if(null != templateOrders.getOrders()) {

			for (OEOrderTemplateFormBean oeTemplateFormBean : templateOrders.getOrders()) {

				OrderTemplate orderTemplate = new OrderTemplate();
				orderTemplate.setOrderTemplateID(Util.encryptString(ORDER_TEMPLATE_ID_PARAM + oeTemplateFormBean.getTemplateID()));
				orderTemplate.setOrderTemplateName(oeTemplateFormBean.getTemplateName());//CAP-48890
				orderTemplate.setDateCreated(formatDateString(oeTemplateFormBean.getCreatedDate()));
				orderTemplate.setAccessType(oeTemplateFormBean.getTemplateType());
				orderTemplate.setItemCount(Integer.valueOf(oeTemplateFormBean.getLineCount()));
				orderTemplate.setCanEditDelete(oeTemplateFormBean.isEditable());

				orderTemplates.add(orderTemplate);
				
			}
		}
		return orderTemplates;
	}

	private Date formatDateString(String stringDate) {
		Date date = null;
		if (!Util.isBlankOrNull(stringDate)) {
			date = Util.formatDateString(stringDate, DATE_PATTERN);
		}
		return date;
	}

	protected OESavedOrderAssembler getAssembler(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
	{
		return new OESavedOrderAssembler(volatileSessionBean, 
				appSessionBean.getCustomToken(), 
				appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());

	}
	
	// CAP-48821
	@Override
	public SaveOrderTemplateResponse saveOrderTemplate(SessionContainer sc,
			SaveOrderTemplateRequest saveOrderTemplateRequest) throws AtWinXSException {
		
		SaveOrderTemplateResponse saveOrderTemplateResponse = new SaveOrderTemplateResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		OrderTemplateHeaderVO orderTemplateVO = null;
		
		if(!appSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID) || !userSettings.isShowTemplatesLink()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		OESavedOrderAssembler savedOrderAssembler = new OESavedOrderAssembler(volatileSessionBean, 
				appSessionBean.getCustomToken(), 
				appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());
		
		orderTemplateVO = buildOrderTemplateVO(saveOrderTemplateResponse, appSessionBean, saveOrderTemplateRequest);
				
		if (orderTemplateVO != null) {
			
			if (!isOrderTemplateValid(saveOrderTemplateResponse, appSessionBean, orderTemplateVO, userSettings)) {
				saveOrderTemplateResponse.setSuccess(false);
				return saveOrderTemplateResponse;
			}
			
			if (NEW_ORDER_TEMPLATE.equals(saveOrderTemplateRequest.getOrderTemplateID()) 
					&&  null != volatileSessionBean.getOrderId()) {
				if (isTemplateNameUnique(appSessionBean.getCustomToken(), orderTemplateVO, appSessionBean.getBuID())) {
					createOrderTemplate(sc, saveOrderTemplateResponse, 
							savedOrderAssembler, orderTemplateVO);
				} else {
					saveOrderTemplateResponse.setSuccess(false);
					saveOrderTemplateResponse.setMessage(buildErrorMessage(appSessionBean, TranslationTextConstants.TRANS_NM_TEMPLATE_NAME_UNIQUE_ERROR_MSG));
					
				}
			} else {
				updateOrderTemplate(saveOrderTemplateResponse, appSessionBean, userSettings, savedOrderAssembler, orderTemplateVO);
			}
		}
		
		return saveOrderTemplateResponse;
	}
	
	private OrderTemplateHeaderVO buildOrderTemplateVO(SaveOrderTemplateResponse saveOrderTemplateResponse, AppSessionBean appSessionBean,
			SaveOrderTemplateRequest saveOrderTemplateRequest) throws AtWinXSException {
		OrderTemplateHeaderVO orderTemplateVO = null;
		String templateType = saveOrderTemplateRequest.isShared() ? OrderEntryConstants.TEMPLATE_TYPE_PUBLIC_IND : OrderEntryConstants.TEMPLATE_TYPE_PRIVATE_IND;//CAP-49465
		try {
			orderTemplateVO = new OrderTemplateHeaderVO(templateType,
						decryptOrderTemplateID(saveOrderTemplateRequest.getOrderTemplateID()),
						appSessionBean.getLoginID(),
						saveOrderTemplateRequest.getOrderTemplateName(),
						appSessionBean.getProfileNumber(),
						appSessionBean.getSiteID(),
						appSessionBean.getOriginatorProfile().getLoginID(), 
						appSessionBean.getOriginatorProfile().getProfileNumber());
		} catch (Exception e) {
			saveOrderTemplateResponse.setSuccess(false);
			saveOrderTemplateResponse.setMessage(buildErrorMessage(appSessionBean, SFTranslationTextConstants.ORDER_TEMPLATE_INVALID));
		}
		return orderTemplateVO;
	}

	private boolean isOrderTemplateValid(SaveOrderTemplateResponse saveOrderTemplateResponse, 
			AppSessionBean appSessionBean, OrderTemplateHeaderVO orderTemplateVO, OEResolvedUserSettingsSessionBean userSettings) throws AtWinXSException {
		boolean isValid = true;
		if (!isTemplateNameLengthValid(orderTemplateVO.getTemplateName())) {
			
			saveOrderTemplateResponse.setMessage(buildErrorMessage(appSessionBean, SFTranslationTextConstants.ORDER_TEMPLATE_NAME_MAX));
			isValid = false;
		}
		if (!isOrderTemplateSettingsValid(appSessionBean, orderTemplateVO.getTemplateID(), orderTemplateVO.getTemplateType(), userSettings.getTemplateUpdateLevel())) {
			saveOrderTemplateResponse.setMessage(buildErrorMessage(appSessionBean, SFTranslationTextConstants.ORDER_TEMPLATE_INVALID_SETTINGS));
			isValid = false;
		}
		return isValid;
	}

	// create new order template
	private void createOrderTemplate(SessionContainer sc, SaveOrderTemplateResponse saveOrderTemplateResponse, OESavedOrderAssembler savedOrderAssembler, 
			OrderTemplateHeaderVO orderTemplateVO) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		Message message = null;

		try {
			OrderTemplateHeaderVOKey orderTemplateKey = savedOrderAssembler.addOrderTemplate(
					appSessionBean.getSiteID(),
					volatileSessionBean.getOrderId().toString(),
					orderTemplateVO,
					appSessionBean.getBuID(),
					null,
					userSettings.getTeamOrdSharingSiteAttr(),
					appSessionBean.getOriginatorProfile().getLoginID(), 
					appSessionBean.getOriginatorProfile().getProfileNumber()); 
			
			if (appSessionBean.hasEnforceOnOrdering()) {
				int templateID = (orderTemplateKey != null) ? orderTemplateKey.getTemplateID() : AtWinXSConstant.INVALID_ID;

				OrderTemplateAttributeVO[] orderTemplateAttrVOs = OrderEntryUtil.getOrderTemplateAttributeVO(volatileSessionBean.getSelectedSiteAttribute(), 
						templateID, appSessionBean.getLoginID());

				if (orderTemplateAttrVOs != null && orderTemplateAttrVOs.length > 0) {
					savedOrderAssembler.saveOrderTempAttributes(orderTemplateAttrVOs);
				}
			}
			saveOrderTemplateResponse.setSuccess(true);
			saveOrderTemplateResponse.setMessage(buildSuccessMessage(appSessionBean, 
					TranslationTextConstants.TRANS_NM_TEMPLATE_ORDER_SAVED_SUCCESS_MSG, orderTemplateVO.getTemplateName()));
		} catch (AtWinXSMsgException me) {
			OEOrderTemplateDetailsFormBean templateDetailBean = savedOrderAssembler.getSavedOrderDetailForTemplate(
					appSessionBean.getSiteID(),
					oeOrderSessionBean,
					appSessionBean,
					appSessionBean.getDefaultTimeZone(), appSessionBean.getProfileNumber());
			if (templateDetailBean.containsInvalidItems()) {
				String templateInvalidItemsErrorMsg = Util.nullToEmpty(buildErrorMessage(appSessionBean, TranslationTextConstants.TRANS_NM_TEMPLATE_INVALID_ITEMS_ERROR_MSG));
				message = me.getMsg();
				Collection<String> errMsgs = message.getErrMsgItems();
				errMsgs.add(templateInvalidItemsErrorMsg);
				message.setErrMsgItems(errMsgs);
				saveOrderTemplateResponse.setSuccess(false);
				saveOrderTemplateResponse.setMessage(errMsgs.toString());
			}
			volatileSessionBean.setHelpBookmark(OrderEntryConstants.HELP_TEMPLATE_ORDERING);
		}
	}

	// update existing order template
	private void updateOrderTemplate(SaveOrderTemplateResponse saveOrderTemplateResponse, AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, OESavedOrderAssembler savedOrderAssembler,
			OrderTemplateHeaderVO orderTemplateVO) throws AtWinXSException {
		if (orderTemplateVO.getTemplateID() > 0) {
			try {
				String duplicateTemplateNameError = savedOrderAssembler.saveOrderTemplateWithValidation(orderTemplateVO, appSessionBean.getBuID(), 
						null, userSettings.getTeamOrdSharingSiteAttr(),
						appSessionBean.getOriginatorProfile().getLoginID(), 
						appSessionBean.getOriginatorProfile().getProfileNumber(),
						appSessionBean.getDefaultLocale());
				// check template name is unique
				if (!Util.isBlankOrNull(duplicateTemplateNameError)) {
					saveOrderTemplateResponse.setSuccess(false);
					saveOrderTemplateResponse.setMessage(duplicateTemplateNameError);
				} else {
					saveOrderTemplateResponse.setSuccess(true);
					saveOrderTemplateResponse.setMessage(buildSuccessMessage(appSessionBean,
							SFTranslationTextConstants.ORDER_TEMPLATE_UPDATE_SUCCESS, orderTemplateVO.getTemplateName()));
				}
			} catch (AtWinXSException e) {
				saveOrderTemplateResponse.setSuccess(false);
				saveOrderTemplateResponse.setMessage(buildErrorMessage(appSessionBean, SFTranslationTextConstants.ORDER_TEMPLATE_INVALID));
			}
		} else {
			saveOrderTemplateResponse.setSuccess(false);
			saveOrderTemplateResponse.setMessage(buildErrorMessage(appSessionBean, SFTranslationTextConstants.ORDER_TEMPLATE_INVALID));
		}
	}
	
	private boolean isTemplateNameLengthValid(String templateName) {
		boolean isValid = true;
		
		if (!Util.isBlankOrNull(templateName) 
				&& templateName.length() > 50) {
			isValid = false;
		}
		
		return isValid;
	}
	
	private int decryptOrderTemplateID(String encryptedOrderTemplateID) {
		String decryptedOrderTemplateID = AtWinXSConstant.EMPTY_STRING;
		if (!Util.isBlankOrNull(encryptedOrderTemplateID)) {
			if (NEW_ORDER_TEMPLATE.equals(encryptedOrderTemplateID)) {
				decryptedOrderTemplateID = encryptedOrderTemplateID;
			} else {
				decryptedOrderTemplateID = Util.decryptString(encryptedOrderTemplateID);
				decryptedOrderTemplateID = decryptedOrderTemplateID.replace(ORDER_TEMPLATE_ID_PARAM, "");
			}
		}
		return Integer.valueOf(decryptedOrderTemplateID);
	}
	
	private boolean isTemplateNameUnique(CustomizationToken token, OrderTemplateHeaderVO orderTemplateVO, int buID) throws AtWinXSException {
		IOESavedOrderComponent savedOrderComponent = oeSavedOrderComponentLocatorService.locate(token);
		return savedOrderComponent.validateOrderTemplate(orderTemplateVO, buID);
	}
	
	private boolean isOrderTemplateSettingsValid(AppSessionBean appSessionBean, int orderTemplateID, String templateType, String templateUpdateLevel) throws AtWinXSException {
		boolean isValidSettings =  OrderEntryConstants.TEMPLATE_TYPE_PRIVATE_IND.equalsIgnoreCase(templateType);
		if (OrderEntryConstants.TEMPLATE_TYPE_PUBLIC_IND.equalsIgnoreCase(templateType)//CAP-48998 changed the flag constant from S to Y
				&& OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_OWN.equalsIgnoreCase(templateUpdateLevel)
					|| OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_ALL.equalsIgnoreCase(templateUpdateLevel)) {
			isValidSettings = true;
		}
		if (orderTemplateID > 0) {
			isValidSettings = canUpdateOrderTemplate(appSessionBean, orderTemplateID, templateUpdateLevel, templateType);
		}
		return isValidSettings;
	}
	private boolean canUpdateOrderTemplate(AppSessionBean appSessionBean, int orderTemplateID,
			String templateUpdateLevel, String templateType) throws AtWinXSException {
		boolean isValidSettings = OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_ALL.equalsIgnoreCase(templateUpdateLevel);
		if (shouldCheckTemplateLevel(templateUpdateLevel, templateType)) {
			OrderTemplateHeaderVO orderTemplateHeaderVO = getOrderTemplateHeaderVO(appSessionBean, orderTemplateID);
			isValidSettings = isValidOwner(appSessionBean, orderTemplateHeaderVO);
		}
		return isValidSettings;
	}

	private boolean shouldCheckTemplateLevel(String templateUpdateLevel, String templateType) {
		return OrderEntryConstants.TEMPLATE_UPDATE_LVL_EDIT_OWN.equalsIgnoreCase(templateUpdateLevel)
				|| (OrderEntryConstants.TEMPLATE_UPDATE_LVL_NONE.equalsIgnoreCase(templateUpdateLevel)
						&& OrderEntryConstants.TEMPLATE_TYPE_PRIVATE_IND.equalsIgnoreCase(templateType));
	}

	private OrderTemplateHeaderVO getOrderTemplateHeaderVO(AppSessionBean appSessionBean, int orderTemplateID)
			throws AtWinXSException {
		OrderTemplateHeaderVOKey headerVOKey = new OrderTemplateHeaderVOKey(orderTemplateID);
		IOESavedOrderComponent savedOrderComponent = oeSavedOrderComponentLocatorService.locate(appSessionBean.getCustomToken());
		return savedOrderComponent.getTemplate(headerVOKey);
	}

	private boolean isValidOwner(AppSessionBean appSessionBean, OrderTemplateHeaderVO orderTemplateHeaderVO) {
		if (orderTemplateHeaderVO != null) {
			int profileNumber = orderTemplateHeaderVO.getProfileNum();
			if (profileNumber > -1) {
				return appSessionBean.getProfileNumber() == profileNumber;
			} else {
				return appSessionBean.getLoginID().equals(orderTemplateHeaderVO.getLoginID());
			}
		}
		return false;
	}
	
	private String buildSuccessMessage(AppSessionBean appSessionBean, String event, String templateName) throws AtWinXSException {
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.ORDER_TEMPLATE_NAME_TAG, templateName);
		return translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				event, replaceMap);
	}
	
	private String buildErrorMessage(AppSessionBean appSessionBean, String errorName) throws AtWinXSException {
		return translationService.processMessage(appSessionBean.getDefaultLocale(), 
				appSessionBean.getCustomToken(), errorName);
	}
	
	// CAP-48889 - Method to delete selected order template
	@Override
	public DeleteTemplateResponse deleteOrderTemplate(SessionContainer sc, DeleteTemplateRequest request)
			throws AtWinXSException {

		DeleteTemplateResponse response = new DeleteTemplateResponse();

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean vsb = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSetting = oeSessionBean.getUserSettings();

		// throw 403 error if logged in user cannot access order templates.
		if (!userSetting.isShowTemplatesLink()) {

			logger.error(getErrorPrefix(asb), " is not allowed to access this service. getProfileDefinition()");
			throw new AccessForbiddenException(this.getClass().getName());
		}

		// throw 422 error if Order Template ID is Empty/Blank
		if (Util.isBlankOrNull(request.getOrderTemplateID())) {

			response.getFieldMessages().put(ModelConstants.ORDER_TEMPLATE_ID_FIELDNAME, AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			response.setSuccess(false);
			return response;
		}

		try {

			String encSelectedOrderTemplateID = request.getOrderTemplateID();
			String selectedOrderTemplateID = Util.decryptString(encSelectedOrderTemplateID);
			selectedOrderTemplateID = selectedOrderTemplateID.replace(ORDER_TEMPLATE_ID_PARAM, "");

			OESavedOrderAssembler assembler = new OESavedOrderAssembler(vsb, asb.getCustomToken(),
					asb.getDefaultLocale(), asb.getApplyExchangeRate(), asb.getCurrencyLocale());

			OEOrderTemplatesFormBean templateOrders = assembler.getOrderTemplates(asb.getSiteID(), asb.getLoginID(),
					asb.getProfileNumber(), asb.getBuID(), OrderEntryConstants.TEMPLATE_ORDER_TEMPLATE_NAME,
					userSetting.getTemplateUpdateLevel(), asb.getGroupName());

			OEOrderTemplatesFormBean formBeanToDelete = getDeleteOrders(templateOrders, selectedOrderTemplateID,
					asb);

			if (null != formBeanToDelete.getOrders() && formBeanToDelete.getOrders().length > 0) {

				OEOrderTemplatesDeleteFormBean ordersDeleteFormBean = generateOEOrderTemplatesDeleteFormBean(
						formBeanToDelete, asb);

				assembler.deleteOrderTemplates(ordersDeleteFormBean);

				String orderTemplateName = ordersDeleteFormBean.getDeletedTemplates()[0].getTemplateName();
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.ORDER_TEMPLATE_NAME_REPLACEMENT_TAG, orderTemplateName);

				response.setMessage(buildErrorMessage(SFTranslationTextConstants.ORDER_TEMPLATE_DELETE_SUCCESS, asb,
						replaceMap));
				response.setSuccess(true);
			} else {

				response.setSuccess(false);
				response.setMessage(
						buildErrorMessage(SFTranslationTextConstants.ORDER_TEMPLATE_DELETE_FAIL, asb, null));
			}
		} catch (Exception e) {

			response.setSuccess(false);
			response.setMessage(buildErrorMessage(SFTranslationTextConstants.ORDER_TEMPLATE_DELETE_FAIL, asb, null));
			return response;
		}

		return response;
	}
	
	/**
	 * Method getDeleteOrders extract the eligible order(s) to delete from
	 * OEOrderTemplatesFormBean
	 * 
	 * @param templateOrders
	 * @param selectedOrderTemplateID
	 * @return OEOrderTemplatesFormBean
	 */
	// CAP-48889 - CP Method from TemplateOrderController used to filter eligible
	// 			   order template to delete from selected one based on Admin setting for UG 
	private OEOrderTemplatesFormBean getDeleteOrders(OEOrderTemplatesFormBean templateOrders,
			String selectedOrderTemplateID, AppSessionBean asb) {

		ArrayList<OEOrderTemplateFormBean> list = new ArrayList<>();
		for (OEOrderTemplateFormBean order : templateOrders.getOrders()) {

			if (order.getTemplateID().equals(selectedOrderTemplateID) && order.isEditable()) {

				list.add(order);
				order.setIsToBeDeleted(true);
				break;
			}
		}
		OEOrderTemplatesFormBean formBeanToDelete = new OEOrderTemplatesFormBean();
		if(!list.isEmpty()) {
			OEOrderTemplateFormBean[] ordersToDelete = new OEOrderTemplateFormBean[list.size()];
			formBeanToDelete.setLoginID(asb.getLoginID());
			formBeanToDelete.setSiteID(asb.getSiteID());
			formBeanToDelete.setOrders(list.toArray(ordersToDelete));
		}
		return formBeanToDelete;
	}

	/**
	 * Method generateOEOrderTemplatesDeleteFormBean.
	 * 
	 * @param deletedorders
	 * @param appSessionBean
	 * @return OEOrderTemplatesDeleteFormBean
	 */
	// CAP-48889 - CP Method from TemplateOrderController used to build
	// 			   OEOrderTemplatesDeleteFormBean
	public static OEOrderTemplatesDeleteFormBean generateOEOrderTemplatesDeleteFormBean(
			OEOrderTemplatesFormBean deletedorders, AppSessionBean asb) {
		OEOrderTemplateFormBean[] templatebeans = deletedorders.getOrders();
		int index = templatebeans.length;

		ArrayList<OEOrderTemplateDeleteFormBean> list = new ArrayList<>();
		for (int i = 0; i < index; i++) {

			list.add(new OEOrderTemplateDeleteFormBean(templatebeans[i].getTemplateID(),
					templatebeans[i].getTemplateName()));
		}
		OEOrderTemplatesDeleteFormBean deletedorder = new OEOrderTemplatesDeleteFormBean();
		OEOrderTemplateDeleteFormBean[] deletedbeans = new OEOrderTemplateDeleteFormBean[index];
		deletedorder.setLoginID(asb.getLoginID());
		deletedorder.setSiteID(asb.getSiteID());
		deletedorder.setDeletedTemplates(list.toArray(deletedbeans));

		return deletedorder;
	}
	
	@Override
	public UseOrderTemplateResponse loadTemplateOrder(SessionContainer sc, UseOrderTemplateRequest request) throws AtWinXSException {
		
		UseOrderTemplateResponse response = new UseOrderTemplateResponse();

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean moduleSessionBean =   oeSession.getOESessionBean();
		Locale defaultLocale = appSessionBean.getDefaultLocale();
		
		String invalidTemplateErrorMsg = Util.nullToEmpty(translationService.processMessage(defaultLocale, appSessionBean.getCustomToken(), SFTranslationTextConstants.INVALID_ORDER_TEMPLATES));

		String strtemplateId =	"";
		try {
		strtemplateId =	Util.decryptString(request.getOrderTemplateID());
		strtemplateId = strtemplateId.replace(TEMPLATE_ID_URL_PARAM, ""); 
		}catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(invalidTemplateErrorMsg);
			return response;
		}
		//CP-9913.  See CP-5919 set order on behalf flag
		appSessionBean.setInRequestorMode(volatileSessionBean.getOrderOnBehalf().isInRequestorMode());
				
		if (strtemplateId == null)
		{
			//CP-8970 changed token from String to an Object
			String noTemplateIdErrorMsg = Util.nullToEmpty(translationService.processMessage(defaultLocale, appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_TEMPLATE_NO_ID_ERROR_MSG));
			throw new AtWinXSException(noTemplateIdErrorMsg, this.getClass().getName());
		}
		
		int templateID = -1;
		
		try
		{
			templateID = Integer.parseInt(strtemplateId);
		}
		catch (Exception e)
		{	
			//CP-8970 changed token from String to an Object
			String notValidTemplateIdErrorMsg = Util.nullToEmpty(translationService.processMessage(defaultLocale, appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_TEMPLATE_NOT_VALID_ID_ERROR_MSG));
			throw new AtWinXSException(notValidTemplateIdErrorMsg, this.getClass().getName());
		}
		
		Message errors = new Message();//CP-9082
		if (volatileSessionBean.getShoppingCartCount() > 0)
		{
			String itemsInCartErrorMsg = Util.nullToEmpty(translationService.processMessage(defaultLocale, appSessionBean.getCustomToken(), SFTranslationTextConstants.TEMPLATES_ITEMS_IN_CART_WARNING));
			response.setSuccess(false);
			response.setMessage(itemsInCartErrorMsg);
			return response;
		}
			
			moduleSessionBean.setOrderSharingDefaultsHaveBeenSet(true);
			OESavedOrderAssembler assembler = getAssembler(appSessionBean,volatileSessionBean);
			ArrayList<ErrorCode> errorlist = new ArrayList<>();
			
			try
			{
				boolean isRedirectToCart = true;
				ArrayList orderTempAttrVOs = null;
				if(appSessionBean.hasEnforceOnOrdering()) 
				{  
					orderTempAttrVOs = assembler.getOrderAttrsByOrderTmpltId(templateID);
					isRedirectToCart = validateAndLoadAttrForEOO(appSessionBean, orderTempAttrVOs, assembler, isRedirectToCart, errors, moduleSessionBean);

				}
				isRedirectToCart = validateBudgetAllocation(appSessionBean, oeSession, volatileSessionBean, punchoutSessionBean, assembler, isRedirectToCart, errors);


				if(!isRedirectToCart) 
				{
					response.setSuccess(false);
					response.setMessage(errors.getErrGeneralMsg());
					return response;
				}

					OEReplacementsSessionBean replacements = new OEReplacementsSessionBean(
							OrderEntryConstants.REPLACEMENT_SELECTION_TYPE_TEMPLATE, templateID, null, null); 

				    assembler.orderFromTemplate(
							appSessionBean.getLoginID(),
							appSessionBean.getSiteID(),
							templateID,
							moduleSessionBean,
							errorlist,
							appSessionBean,
							replacements, 
							volatileSessionBean); 

				    if (replacements.isEmpty())
				    { 
						if (!errorlist.isEmpty())
						{
							//CP-9301 Fixed templateNotOrderableMsg.
							//CP-8970 changed token from String to an Object
							String templateNotOrderableMsg = Util.nullToEmpty(translationService.processMessage(defaultLocale, appSessionBean.getCustomToken(), ErrorCodeConstants.ERR_TEMP_NOT_ORDER_MSG));
							response.setMessage(templateNotOrderableMsg);
						}

						int ordId = volatileSessionBean.getOrderId().intValue();
						
						assembler.copyOrderAttributes(orderTempAttrVOs, ordId);
							
						//CAP-8079
						volatileSessionBean.setShoppingCartCount(new OrderLineVOKey(ordId, -1), appSessionBean.getCustomToken());

						response.setSuccess(true);
						response.setOrderLineMessages(errorlist);
				    }
				    else
				    {
						//CAP-24278
						oeSession.setReplacements(replacements);
						//CP-11880 PTB Use translation for error message
						String errMsg = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "noneOrdItemsLbl");
						response.setMessage(errMsg);
						response.setSuccess(false);
						return response;

				    }
				
			}
			catch (AtWinXSMsgException e)
			{
				String msg = e.getMessage();

				response.setSuccess(false);
				response.setMessage(msg);

			}
			catch (AtWinXSException e)
			{
				throw e;

			}
		
			
		SessionHandler.saveSession(sc.getApplicationVolatileSession(), appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);

			
		return response;
	}
	
	public boolean validateBudgetAllocation(AppSessionBean appSessionBean, OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean, PunchoutSessionBean punchoutSessionBean, OESavedOrderAssembler assembler, boolean  isRedirectToCart, Message errors) throws AtWinXSException {
		try
		{
		    OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		    OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		    //CP-9394 , move this method to assembler to share
		    assembler.validateBudgetAllocation(appSessionBean.getBuID(), 
		    								appSessionBean.getSiteID(), 
		    								appSessionBean.getProfileNumber(), 
		    								volatileSessionBean.getOrderScenarioNumber(), 
		    								userSettings,
		    								OrderEntryConstants.ORDER_FROM_TEMPLATE_EVT, appSessionBean, punchoutSessionBean);
		}
		catch(AtWinXSMsgException msg)
		{
		    errors.setErrGeneralMsg(msg.getMsg().getErrGeneralMsg()); 
		    isRedirectToCart = false;
		}
		return isRedirectToCart;
	}
	public boolean validateAndLoadAttrForEOO(AppSessionBean appSessionBean, ArrayList orderTempAttrVOs, OESavedOrderAssembler assembler, boolean  isRedirectToCart,Message errors,OEOrderSessionBean moduleSessionBean) throws AtWinXSException {
		
			//CP-8970 changed token from String to an Object
			String savedOrderErrorMsg2 = Util.nullToEmpty(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR2_MSG));

		    if(orderTempAttrVOs == null) 
			{
		        errors.setErrGeneralMsg(savedOrderErrorMsg2);
		        isRedirectToCart = false;
		        return isRedirectToCart;
			}
		    else 
		    {
		    	ArrayList<UserGroupSearchVO> userGroupOpts = moduleSessionBean.getUsrSrchOptions();

				if(!(assembler.validateOrdAttrWithProfileAttr(userGroupOpts, orderTempAttrVOs, appSessionBean.getProfileAttributes(), appSessionBean.getSiteID())
		                && orderTempAttrVOs != null && !orderTempAttrVOs.isEmpty()))
		        {
		        	
		            errors.setErrGeneralMsg(savedOrderErrorMsg2);
			        isRedirectToCart = false;
		        }
		        if (isRedirectToCart)
		        {
		        	HashMap<Integer, SiteAttrValuesVO[]> siteAttribMap = assembler.getSiteAttribMap(orderTempAttrVOs, appSessionBean);
				    assembler.loadOrderAttributeForValidOrder(siteAttribMap, moduleSessionBean, appSessionBean);
		        }
		        
		    }
		    return isRedirectToCart;
		
	}


	// CAP-48889 - common method to build error message
	private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {

		return Util.nullToEmpty(
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), errorKey, replaceMap));
	}
	
}
