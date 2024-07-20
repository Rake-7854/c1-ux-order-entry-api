/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *	06/08/23	S Ramachandran		CAP-41235		Review order - Submit order
  *	06/22/23	L De Leon			CAP-41373		Modified how volatile session is being saved
  *	09/05/23	Satishkumar A      	CAP-42763		C1UX BE - Order Routing Justification Text Submit Order
 *	09/08/23	Krishna Natarajan	CAP-42763		Modified method to fix complexity
 *	09/13/23	Satishkumar A		CAP-43685		C1UX - BE - Add translation messages for order submit and routed orders
 *	09/25/23	Satishkumar A		CAP-44097		C1UX - BE - Fixing Routing Justification - Submit Order - when order justification is not displayed
 *	09/26/23	A Salcedo			CAP-44145		Fix R&A null pointer issue.
 *	11/10/23	L De Leon			CAP-44841		Modified loadSubmitResponse() to get different text for order header and description if demo user
 *	03/15/24	L De Leon			CAP-47894		Modified clearSessionAfterSubmit() method to reset isForceCCOption in on order entry session bean
 *	05/15/24	Krishna Natarajan	CAP-49176		Modified the translation messages for order submit and routed orders
 */

package com.rrd.c1ux.api.services.checkout;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.checkout.SubmitOrderResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.orders.savedorders.SavedOrderServiceImpl;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OECancelOrderAssembler;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderLinesRoutingFormBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderRoutingFormBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;

@Service
public class SubmitOrderServiceImpl extends BaseOEService  implements SubmitOrderService {

	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	protected SubmitOrderServiceImpl(TranslationService translationService) {

		super(translationService);
	}


	/**
	 * This method should submit order
	 *
	 * @param SessionContainer sc
	 * @param String remoteIPAddr
	 * @param OrderEntrySession oeSession
	 * @return SubmitOrderResponse
	 * @throws AtWinXSException
	 */
	//CAP-38157
	@Override
	public SubmitOrderResponse submitOrder(SessionContainer sc, String remoteIPAddr)
			throws AtWinXSException {

		ApplicationSession applicationSession = sc.getApplicationSession();
		AppSessionBean applicationSessionBean = applicationSession.getAppSessionBean();

		ApplicationVolatileSession appVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = appVolatileSession.getVolatileSessionBean();

		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		//CAP-43685
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		String justificationText = "";

		SubmitOrderResponse response = new SubmitOrderResponse();

		stopPunchoutSubmit(applicationSessionBean);

		// CAP-38157 - if user does not have a current order (no orderId ) & current order is already submitted
		if (!validateOrder(response, sc, applicationSessionBean))	{

			try {
				appVolatileSession.getVolatileSessionBean().setOrderId(null);
				appVolatileSession.getVolatileSessionBean().setShoppingCartCount(0);
				SessionHandler.saveSession(appVolatileSession, applicationSessionBean.getSessionID(),
						AtWinXSConstant.APPVOLATILESESSIONID);
			}catch(AtWinXSException e) {

				logger.error("from SubmitOrder - validateOrder(), failed to save the session", e);
			}

			return response;
		}

		try {

			OECheckoutAssembler assembler = getCheckoutAssembler(applicationSessionBean, volatileSessionBean);

			//CAP-44097
			boolean isOrderRouted = false;

			//CAP-44145
			if(userSettings.isRoutingAvailable() && userSettings.isSubjToRnA())
			{
				isOrderRouted = isOrderRoutedOrNot(assembler, applicationSessionBean, oeOrderSessionBean);
			}

			//CAP-42763 CAP-43685 CAP-44097
			if(isOrderRouted && !validateJustificationText( userSettings, applicationSessionBean, oeOrderSessionBean, justificationText, response))
				return response;

			// Get Current OrderId to be submitted from volatile session
			int currentOrderID = volatileSessionBean.getOrderId().intValue();

			// submit order
			String salesRef = assembler.submitOrder(applicationSessionBean, oeOrderSessionBean, remoteIPAddr);

			//CAP-43685
			if (Util.isBlankOrNull(salesRef)) {//CAP-42673 added the code as suggested by Tony
				OrderHeaderVO orderHeaderVO = assembler.getOrderHeader(currentOrderID);
				if ("A".equalsIgnoreCase(orderHeaderVO.getOrderXSStatusCode())
						&& !Util.isBlankOrNull(orderHeaderVO.getSalesReferenceNum())) {
					salesRef = orderHeaderVO.getSalesReferenceNum();
				}
			}

			if (!Util.isBlankOrNull(salesRef)) {

				int scenarioNumber = loadSubmitResponse(response, applicationSessionBean, currentOrderID, assembler);

				// setup Quick Copy
				setUpQuickCopy(response, oeOrderSessionBean, volatileSessionBean, currentOrderID, scenarioNumber);

				// make this to have the clear calls
				// CAP-41373 passed volatile session instead of volatile session bean
				clearSessionAfterSubmit(oeSession, appVolatileSession, applicationSession);

				response.setSuccess(true);

			} else {

				getOrderCouldNotComplete(response, applicationSessionBean);
			}

		} catch (AtWinXSException e) {

			getOrderCouldNotComplete(response, applicationSessionBean);
		}
		return response;
	}

	/**
	 * This method should submit order
	 *
	 * @param SessionContainer sc
	 * @param String remoteIPAddr
	 * @param OrderEntrySession oeSession
	 * @return SubmitOrderResponse
	 * @throws AtWinXSException
	 */
	//CAP-42763
	@Override
	public SubmitOrderResponse submitOrder(SessionContainer sc, String remoteIPAddr, String justificationText)
			throws AtWinXSException {

		ApplicationSession applicationSession = sc.getApplicationSession();
		AppSessionBean applicationSessionBean = applicationSession.getAppSessionBean();

		ApplicationVolatileSession appVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = appVolatileSession.getVolatileSessionBean();

		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();

		SubmitOrderResponse response = new SubmitOrderResponse();

		stopPunchoutSubmit(applicationSessionBean);

		// CAP-38157 - if user does not have a current order (no orderId ) & current order is already submitted
		if (!validateOrder(response, sc, applicationSessionBean))	{

			try {
				appVolatileSession.getVolatileSessionBean().setOrderId(null);
				appVolatileSession.getVolatileSessionBean().setShoppingCartCount(0);
				SessionHandler.saveSession(appVolatileSession, applicationSessionBean.getSessionID(),
						AtWinXSConstant.APPVOLATILESESSIONID);
			}catch(AtWinXSException e) {

				logger.error("from SubmitOrder - validateOrder(), failed to save the session", e);
			}

			return response;
		}

		try {

			OECheckoutAssembler assembler = getCheckoutAssembler(applicationSessionBean, volatileSessionBean);

			//CAP-44097
			boolean isOrderRouted = false;

			//CAP-44145
			if(userSettings.isRoutingAvailable() && userSettings.isSubjToRnA())
			{
				isOrderRouted = isOrderRoutedOrNot(assembler, applicationSessionBean, oeOrderSessionBean);
			}

			//CAP-42763 CAP-44097
			if(isOrderRouted && !validateJustificationText( userSettings, applicationSessionBean, oeOrderSessionBean, justificationText, response))
				return response;

			// Get Current OrderId to be submitted from volatile session
			int currentOrderID = volatileSessionBean.getOrderId().intValue();

			// submit order
			String salesRef = assembler.submitOrder(applicationSessionBean, oeOrderSessionBean, remoteIPAddr);


			if (Util.isBlankOrNull(salesRef)) {//CAP-42673 added the code as suggested by Tony
				OrderHeaderVO orderHeaderVO = assembler.getOrderHeader(currentOrderID);
				if ("A".equalsIgnoreCase(orderHeaderVO.getOrderXSStatusCode())
						&& !Util.isBlankOrNull(orderHeaderVO.getSalesReferenceNum())) {
					salesRef = orderHeaderVO.getSalesReferenceNum();

				}
			}

			if (!Util.isBlankOrNull(salesRef)) {

				int scenarioNumber = loadSubmitResponse(response, applicationSessionBean, currentOrderID, assembler);

				// setup Quick Copy
				setUpQuickCopy(response, oeOrderSessionBean, volatileSessionBean, currentOrderID, scenarioNumber);

				// make this to have the clear calls
				// CAP-41373 passed volatile session instead of volatile session bean
				clearSessionAfterSubmit(oeSession, appVolatileSession, applicationSession);

				response.setSuccess(true);

			} else {

				getOrderCouldNotComplete(response, applicationSessionBean);
			}

		} catch (AtWinXSException e) {

			getOrderCouldNotComplete(response, applicationSessionBean);
		}
		return response;
	}

	protected OECheckoutAssembler getCheckoutAssembler(AppSessionBean applicationSessionBean, VolatileSessionBean volatileSessionBean ) {

		return (new OECheckoutAssembler(volatileSessionBean,
				applicationSessionBean.getCustomToken(), applicationSessionBean.getDefaultLocale(),
				applicationSessionBean.getApplyExchangeRate(), applicationSessionBean.getCurrencyLocale()));
	}


	// CAP-38157 - load response after Order Submit
	protected int loadSubmitResponse(SubmitOrderResponse response, AppSessionBean appSessionBean, int currentOrderID, OECheckoutAssembler assembler)
			throws AtWinXSException {

		OrderHeaderVO orderHeaderVO = assembler.getOrderHeader(currentOrderID);

		response.setSalesReferenceNumber(orderHeaderVO.getSalesReferenceNum());
		response.setWcssOrderNumber(orderHeaderVO.getWCSSOrderNumber());

		if(Util.isBlankOrNull(response.getWcssOrderNumber())) {
			response.setMessage(
					getOrderSubmitSuccessMsgDirect(appSessionBean, response.getSalesReferenceNumber(),
							orderHeaderVO.getOrderContactEmailAddr()));
		}
		else {
			response.setMessage(
					getOrderSubmitSuccessMsgRealtime(appSessionBean, response.getWcssOrderNumber(), response.getSalesReferenceNumber(),
							orderHeaderVO.getOrderContactEmailAddr()));
		}

		// CAP-44841
		setOrderLabelAndHeaderDescription(response, appSessionBean, orderHeaderVO);

		return orderHeaderVO.getOrderScenarioNum();
	}

	// CAP-44841
	protected void setOrderLabelAndHeaderDescription(SubmitOrderResponse response, AppSessionBean appSessionBean,
			OrderHeaderVO orderHeaderVO) throws AtWinXSException {
		String orderLabel = getTranslation(appSessionBean, SFTranslationTextConstants.SUBMIT_ORDER_LABEL,
				SFTranslationTextConstants.SUBMIT_ORDER_DEF_TXT);
		String orderHeaderDesc = getTranslation(appSessionBean, SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DESC,
				SFTranslationTextConstants.SUBMIT_ORDER_HEADER_DEF_DESC);
		if (appSessionBean.isDemoUser()) {
			orderLabel = getTranslation(appSessionBean, SFTranslationTextConstants.DEMO_SUBMIT_ORDER_LABEL,
					SFTranslationTextConstants.DEMO_SUBMIT_ORDER_DEF_TXT);
		}
		
		// CAP-43685
		else if ("A".equalsIgnoreCase(orderHeaderVO.getOrderXSStatusCode())) {
			response.setMessage(getRoutedOrderSuccessMsgDirect(appSessionBean, response.getSalesReferenceNumber(),
					orderHeaderVO.getOrderContactEmailAddr()));
			orderLabel = getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_ROUTED_LABEL,
					SFTranslationTextConstants.ORDER_ROUTED_DEF_TXT);

			orderHeaderDesc = getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_ROUTED_HEADER_DESC,
					SFTranslationTextConstants.ORDER_ROUTED_HEADER_DEF_DESC);
			
			if (orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_NO_MERGE_KIT_AND_SINGLE_SHIP
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_KIT_AND_DIST_LIST
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_NO_KIT_AND_DIST_LIST
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS_KITTED
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY_KITTED
					|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MERGE_KITTED_AND_SINGLE_SHIP) {
				response.setMessage(getOrderRequestSuccessMsg(appSessionBean, response.getSalesReferenceNumber(),
						orderHeaderVO.getOrderContactEmailAddr(), orderHeaderVO));
			}

		}
		
		// CAP-49176
		else if (orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_NO_MERGE_KIT_AND_SINGLE_SHIP
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_KIT_AND_DIST_LIST
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_NO_KIT_AND_DIST_LIST
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS_KITTED
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY_KITTED
				|| orderHeaderVO.getOrderScenarioNum() == OrderEntryConstants.SCENARIO_MERGE_KITTED_AND_SINGLE_SHIP) {

			if ("A".equalsIgnoreCase(orderHeaderVO.getOrderXSStatusCode())) {
				response.setMessage(getRoutedOrderSuccessMsgDirect(appSessionBean, response.getSalesReferenceNumber(),
						orderHeaderVO.getOrderContactEmailAddr()));
				orderLabel = getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_ROUTED_LABEL,
						SFTranslationTextConstants.ORDER_ROUTED_DEF_TXT);

				orderHeaderDesc = getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_ROUTED_HEADER_DESC,
						SFTranslationTextConstants.ORDER_ROUTED_HEADER_DEF_DESC);

			} else {
				response.setMessage(getOrderRequestSuccessMsg(appSessionBean, response.getSalesReferenceNumber(),
						orderHeaderVO.getOrderContactEmailAddr(), orderHeaderVO));
				orderLabel = getTranslation(appSessionBean, SFTranslationTextConstants.SF_ORDER_REQUESTED_DESC_LBL,
						SFTranslationTextConstants.SF_ORDER_REQUESTED_DESC_LBL);

				orderHeaderDesc = getTranslation(appSessionBean, SFTranslationTextConstants.SF_ORDER_REQUESTED_DESC_LBL,
						SFTranslationTextConstants.SF_ORDER_REQUESTED_DESC_LBL);
			}
		} // CAP-49176
		
		response.setOrderLabel(orderLabel);
		response.setOrderHeaderDescription(orderHeaderDesc);
	}

	// CAP-38157 - get Success message for Realtime
	protected String getOrderSubmitSuccessMsgRealtime(AppSessionBean appSessionBean, String wcssOrderNumber, String salesReferenceNumber, String orderContactEmailAddr)
			throws AtWinXSException {

		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_SALES_REF_NUMBER_TAG, salesReferenceNumber);
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_WCSS_ORDER_NUMBER_TAG, wcssOrderNumber);
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_CONTACT_EMAIL_ADDR_TAG, orderContactEmailAddr);

		return translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				SFTranslationTextConstants.ORDER_SUBMIT_SUCESS_REALTIME,replaceMap);
	}


	// CAP-38157 - get Success message for Direct
	protected String getOrderSubmitSuccessMsgDirect(AppSessionBean appSessionBean,  String salesReferenceNumber, String orderContactEmailAddr)
			throws AtWinXSException {

		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_SALES_REF_NUMBER_TAG, salesReferenceNumber);
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_CONTACT_EMAIL_ADDR_TAG, orderContactEmailAddr);

		return translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				SFTranslationTextConstants.ORDER_SUBMIT_SUCESS_DIRECT,replaceMap);
	}

	// CAP-43685 - Routed order message
	protected String getRoutedOrderSuccessMsgDirect(AppSessionBean appSessionBean,  String salesReferenceNumber, String orderContactEmailAddr)
			throws AtWinXSException {

		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_SALES_REF_NUMBER_TAG, salesReferenceNumber);
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_CONTACT_EMAIL_ADDR_TAG, orderContactEmailAddr);

		return translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				SFTranslationTextConstants.ORDER_ROUTED_MESSAGE_DESC,replaceMap);
	}



	// CAP-38157 - check if user current order is a punchout , return true
	protected void stopPunchoutSubmit(AppSessionBean appSessionBean)
			throws AtWinXSException {

		if (appSessionBean.isPunchout()) {

			logger.error(getErrorPrefix(appSessionBean),
					"from SubmitOrderServiceImpl(), punchout session don't have access to Submit Order", -1);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}
	}


	// CAP-38157 - get Order Could Not Complete translation Message
	protected void getOrderCouldNotComplete(SubmitOrderResponse response, AppSessionBean appSessionBean) throws AtWinXSException {

		response.setSuccess(false);
		response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				SFTranslationTextConstants.ORDER_SUBMIT_COULD_NOT_COMPLETE_ERR));
	}

	// CAP-41373 - updated how volatile session bean is saved
	// CAP-38157 - clear the session after Order Submit
	protected void clearSessionAfterSubmit(OrderEntrySession orderEntrySession, ApplicationVolatileSession appVolatileSession,
			ApplicationSession applicationSession) throws AtWinXSException {

		orderEntrySession.clearOrder(appVolatileSession.getVolatileSessionBean(), applicationSession.getAppSessionBean());
		orderEntrySession.getOESessionBean().setForceCCOptionAllocation(false); // CAP-47894

		appVolatileSession.getVolatileSessionBean().setSelectedMASSSiteAttribute(null);
		appVolatileSession.getVolatileSessionBean().incrementSessionOrderCount();

		OECancelOrderAssembler oeCancelOrderAssembler = OEAssemblerFactory.getCancelOrderAssembler(
				applicationSession.getAppSessionBean().getCustomToken(),
				applicationSession.getAppSessionBean().getDefaultLocale());
		oeCancelOrderAssembler.clearOrderSessionOverrides(applicationSession);

		saveOESessions(orderEntrySession, appVolatileSession, applicationSession);
	}


	// CAP-41373 - Updated how volatile session is saved
	// CAP-38157 - Save OEsesion after clearing the session after submit
	private void saveOESessions(OrderEntrySession oeSession, ApplicationVolatileSession appVolatileSession, ApplicationSession applicationSession)
			throws AtWinXSException {

		SessionHandler.saveSession(oeSession, applicationSession.getAppSessionBean().getSessionID(),
				AtWinXSConstant.ORDERS_SERVICE_ID);

		appVolatileSession.getVolatileSessionBean().setOrderId(AtWinXSConstant.INVALID_ID);
		appVolatileSession.getVolatileSessionBean().setShoppingCartCount(0);

		SessionHandler.saveSession(appVolatileSession, applicationSession.getAppSessionBean().getSessionID(),
				AtWinXSConstant.APPVOLATILESESSIONID);
		SessionHandler.saveSession(applicationSession, applicationSession.getAppSessionBean().getSessionID(),
				AtWinXSConstant.APPSESSIONSERVICEID);
	}


	// CAP-38157 - setup Quick Copy flag/orderid,
	// if Order is eligible to COPY, set the OrderId and LastSubmittedOrderUd
	protected void setUpQuickCopy(SubmitOrderResponse response, OEOrderSessionBean oeOrderSessionBean,
			VolatileSessionBean volatileSessionBean, int currentOrderID, int scenarioNumber) {

		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		if (isOrderEligibleToCopy(scenarioNumber) && userSettings.isAllowQuickCopy()) {

			response.setOrderIdCanQuickCopy(currentOrderID);
			volatileSessionBean.setLastSubmittedOrderID(currentOrderID);
		}
		else {
			volatileSessionBean.clearLastSubmittedOrderID();
		}

	}


	// CAP-38157 - check if Order is Eligible to copy as per order scenerio Number
	protected boolean isOrderEligibleToCopy(int orderScenarioNumber) {

		return (orderScenarioNumber != OrderEntryConstants.SCENARIO_KIT_AND_DIST_LIST
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_NO_KIT_AND_DIST_LIST
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS_KITTED
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY_KITTED
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_CAMPAIGN_BULK_SHIP_ONLY
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_CAMPAIGN_ONLY
				&& orderScenarioNumber != OrderEntryConstants.SCENARIO_SUBSCRIPTION_ONLY);
	}

	//CAP-42763
	public boolean validateJustificationText(OEResolvedUserSettingsSessionBean userSettings,
			AppSessionBean applicationSessionBean, OEOrderSessionBean oeOrderSessionBean, String justificationText,
			SubmitOrderResponse response) throws AtWinXSException {

		if (userSettings.isRequireJustificationTxtInd() && !applicationSessionBean.isDemoUser()
				&& userSettings.isRoutingAvailable()
				&& (userSettings.isSubjToRnA() || oeOrderSessionBean.isAllowCstmListUpload())) {
			if (null == justificationText || justificationText.isBlank()) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put("{varLabel}",
						TranslationTextTag.processMessage(applicationSessionBean.getDefaultLocale(),
								applicationSessionBean.getCustomToken(), "just_text_lbl"));
				response.setSuccess(false);
				response.setMessage(TranslationTextTag.processMessage(applicationSessionBean.getDefaultLocale(),
						applicationSessionBean.getCustomToken(),
						TranslationTextConstants.TRANS_NM_UI_TEXT_REQUIRED_ERROR, replaceMap));
				return false;
			} else if (justificationText.length() > 1000) {
				response.setSuccess(false);
				response.setMessage(TranslationTextTag.processMessage(applicationSessionBean.getDefaultLocale(),
						applicationSessionBean.getCustomToken(),
						TranslationTextConstants.TRANS_NM_JUST_EXCEEDS_LMT_MSG));
				return false;
			} else {
				oeOrderSessionBean.setJustificationTxt(justificationText);
			}

		} else {
			oeOrderSessionBean.setJustificationTxt("");
		}
		return true;
	}


	public boolean isOrderRoutedOrNot(OECheckoutAssembler assembler, AppSessionBean applicationSessionBean, OEOrderSessionBean oeOrderSessionBean) throws AtWinXSException {

		boolean isOrderRouted = false;
		OEOrderRoutingFormBean orderDetailsFormBean = assembler.getOrderRoutingDetails(oeOrderSessionBean, applicationSessionBean);

		if(orderDetailsFormBean.getOrderLineDetailsFormBean()!=null) {
			for(OEOrderLinesRoutingFormBean bean : orderDetailsFormBean.getOrderLineDetailsFormBean()) {
				if(bean.getRoutingReason()!= null && !bean.getRoutingReason().isEmpty()) {
					isOrderRouted = true;
					break;
				}
			}
		}
		if(!isOrderRouted && orderDetailsFormBean.getOrderRoutingReason() != null && !orderDetailsFormBean.getOrderRoutingReason().isEmpty())
			isOrderRouted = true;

		if(!isOrderRouted)
			oeOrderSessionBean.setJustificationTxt("");

		return isOrderRouted;

	}
	
	// CAP-49176
	protected String getOrderRequestSuccessMsg(AppSessionBean appSessionBean, String salesReferenceNumber,
			String orderContactEmailAddr, OrderHeaderVO orderHeaderVO) throws AtWinXSException {

		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_SALES_REF_NUMBER_TAG, salesReferenceNumber);
		replaceMap.put(SFTranslationTextConstants.ORDER_SUBMIT_CONTACT_EMAIL_ADDR_TAG, orderContactEmailAddr);

		String orderRequestMessage = "";
		
		if ("A".equalsIgnoreCase(orderHeaderVO.getOrderXSStatusCode())) {
			orderRequestMessage = translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.ORDER_ROUTED_MESSAGE_DESC, replaceMap);
		}else {
			orderRequestMessage = translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.ORDER_REQUESTED_DESC, replaceMap);
		}

		return orderRequestMessage;
	}
}
