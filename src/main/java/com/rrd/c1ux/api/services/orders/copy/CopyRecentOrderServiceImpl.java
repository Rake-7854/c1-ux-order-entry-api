/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/01/23				L De Leon				CAP-42519					Initial Version
 *	08/15/23				L De Leon				CAP-42519					Added validation for order submitted by the same user
 *	02/15/24				Krishna Natarajan		CAP-47255					Setting the correct shopping cart count in vsb
 *	03/21/24				L De Leon				CAP-47969					Modified copyRecentOrder() method to add code to handle budget allocation checks
 */
package com.rrd.c1ux.api.services.orders.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.CopyRecentOrderResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.budgetallocation.BudgetAllocationService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.gwt.ordersearch.widget.CopyOrderResult;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.admin.vo.OrderAttributeVO;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEReplacementsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;

@Service
public class CopyRecentOrderServiceImpl extends BaseOEService implements CopyRecentOrderService {

	private static final Logger logger = LoggerFactory.getLogger(CopyRecentOrderServiceImpl.class);

	// CAP-47969
	private final BudgetAllocationService budgetAllocationService;

	// CAP-47969 Modified method signature to add BudgetAllocationService to the parameters
	protected CopyRecentOrderServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService,
			OEManageOrdersComponentLocatorService oeManageOrdersLocatorService,
			BudgetAllocationService budgetAllocationService) {
		super(translationService, objectMapFactoryService, oeManageOrdersLocatorService);
		this.budgetAllocationService = budgetAllocationService; // CAP-47969
	}

	@Override
	public CopyRecentOrderResponse copyRecentOrder(SessionContainer sc, CopyRecentOrderRequest copyRecentOrderRequest)
			throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		if (!oeOrderSessionBean.getUserSettings().isShowCopyRecentOrder() || appSessionBean.isPunchout()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		CopyRecentOrderResponse response = new CopyRecentOrderResponse();

		// validate no current order exists
		boolean hasNoOrderInProgress = validateNoOrderInProgress(response, sc, appSessionBean);

		// validate requested order exists and already submitted
		int requestedOrderID = copyRecentOrderRequest.getOrderID();
		boolean isOrderExistingAndSubmitted = hasNoOrderInProgress
				&& validateOrderExistandSubmitted(response, requestedOrderID, appSessionBean);

		// CAP-42519
		boolean isOrderSubmittedBySameProfile = isOrderExistingAndSubmitted
				&& validateOrderSubmittedBySameProfile(response, requestedOrderID, appSessionBean);

		if (isOrderSubmittedBySameProfile) {
			// all validations passed, proceed with copying order
			try {
				CopyOrderResult result = handleCopyOrder(requestedOrderID, oeOrderSessionBean, appSessionBean,
						volatileSessionBean);
				// call handleBudgetAllocations(oeOrderSessionBean, appSessionBean, result)
				// if we want to validate if budget allocation is depleted
				response.setSuccess(result.isRedirectToCart());
				response.setMessage(getErrorMessageFromList(result, "\n"));
				persistInSession(applicationVolatileSession);
			} catch (AtWinXSException ex) {
				logger.error("CopyRecent - copyRecentOrder() call to handleCopyOrder(), failed to copy the order", ex);
				response.setSuccess(false);
				response.setMessage(
						getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_NOT_COPIED_AT_THIS_TIME_ERR_MSG,
								SFTranslationTextConstants.ORDER_NOT_COPIED_AT_THIS_TIME_DEF_ERR_MSG));
			}
		}

		return response;
	}

	// CAP-47969
	protected void handleBudgetAllocations(OEOrderSessionBean oeOrderSessionBean, AppSessionBean appSessionBean,
			CopyOrderResult result) {
		String budgetDepletedErrorMsg = budgetAllocationService.validateBudgetAllocations(oeOrderSessionBean,
				appSessionBean, result.isRedirectToCart());
		if (!Util.isBlankOrNull(budgetDepletedErrorMsg)) {
			result.getError().add(budgetDepletedErrorMsg);
		}
	}

	// CAP-42519
	protected boolean validateOrderSubmittedBySameProfile(CopyRecentOrderResponse response, int orderID,
			AppSessionBean appSessionBean) {

		boolean isOrderSubmittedBySameProfile = false;
		OrderHeaderVO headerVo = getOrderHeader(orderID);

		if (null != headerVo) {
			isOrderSubmittedBySameProfile = appSessionBean.getLoginID().equalsIgnoreCase(headerVo.getLoginID())
					&& appSessionBean.getProfileNumber() == headerVo.getProfileNum();
		}

		if (!isOrderSubmittedBySameProfile) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_NOT_COPIED_ERR_MSG,
					SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG));
		}

		return isOrderSubmittedBySameProfile;
	}

	public CopyOrderResult handleCopyOrder(int orderID, OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException {

		CopyOrderResult copyOrderResult = new CopyOrderResult();
		OESavedOrderAssembler assembler = getSavedOrderAssembler(appSessionBean, volatileSessionBean);
		List<String> errorMessages = new ArrayList<>();

		boolean runCopyOrder = prepareAttributes(orderID, appSessionBean, oeSessionBean, volatileSessionBean, assembler,
				copyOrderResult, errorMessages);

		// If we were able to copy the attributes to session correctly, continue here
		if (runCopyOrder) {
			try {
				handleCopyResult(appSessionBean, copyOrderResult, errorMessages, volatileSessionBean, orderID,
						assembler, oeSessionBean);
			} catch (AtWinXSException e) {
				throw new AtWinXSException(e.getMessage(), this.getClass().getName());
			}
		}

		return copyOrderResult;
	}

	protected boolean prepareAttributes(int orderID, AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, OESavedOrderAssembler assembler, CopyOrderResult copyOrderResult,
			List<String> errorMessages) {
		boolean runCopyOrder = false;
		// CP-12061 TH BEGIN - Added code to get order attributes to session from copied
		// order
		try {
			loadOrderAttributesToSession(orderID, appSessionBean, oeSessionBean, volatileSessionBean, assembler); // CAP-10262
			runCopyOrder = true;
		} catch (Exception ex) {
			errorMessages.add(ex.getMessage());
			copyOrderResult.setRedirectToCart(false);
			copyOrderResult.setRedirectToReplacement(false);
			copyOrderResult.setError(errorMessages);
		}
		return runCopyOrder;
	}

	protected void loadOrderAttributesToSession(int orderID, AppSessionBean appSessionBean,
			OEOrderSessionBean oeSessionBean, VolatileSessionBean volatileSessionBean, OESavedOrderAssembler assembler)
			throws AtWinXSException {

		if (appSessionBean.hasEnforceOnOrdering()) {
			@SuppressWarnings("unchecked")
			ArrayList<OrderAttributeVO> orderAttrVOs = assembler.getOrderAttrsByOrderId(orderID);
			volatileSessionBean.setSelectedMASSSiteAttribute(assembler.getMassAttributeMap(orderAttrVOs)); // CAP-10262
			if (orderAttrVOs == null) {
				throw new AtWinXSException(getTranslation(appSessionBean,
						ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR2_MSG, AtWinXSConstant.EMPTY_STRING),
						this.getClass().getName());
			} else {
				// Get the user group options here to call our method
				if (!assembler.validateOrdAttrWithProfileAttr(oeSessionBean.getUsrSrchOptions(), orderAttrVOs,
						appSessionBean.getProfileAttributes(), appSessionBean.getSiteID())) {
					throw new AtWinXSException(getTranslation(appSessionBean,
							ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR2_MSG, AtWinXSConstant.EMPTY_STRING),
							this.getClass().getName());
				}
			}

			HashMap<Integer, SiteAttrValuesVO[]> siteAttribMap = assembler.getSiteAttribMap(orderAttrVOs,
					appSessionBean);
			assembler.loadOrderAttributeForValidOrder(siteAttribMap, oeSessionBean, appSessionBean);
		}
	}

	protected void handleCopyResult(AppSessionBean appSessionBean, CopyOrderResult copyOrderResult,
			List<String> errorMessages, VolatileSessionBean volatileSessionBean, int orderID,
			OESavedOrderAssembler assembler, OEOrderSessionBean oeSessionBean) throws AtWinXSException {

		ArrayList<String> errorlist = new ArrayList<>();
		Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class,
				appSessionBean.getCustomToken());
		order.populate(orderID);
		OEReplacementsSessionBean replacements = new OEReplacementsSessionBean(
				OrderEntryConstants.REPLACEMENT_SELECTION_TYPE_COPY, orderID, null, null);

		boolean hasTemplate = assembler.copyRecentOrder(appSessionBean.getSiteID(), appSessionBean.getLoginID(),
				orderID, oeSessionBean, errorlist, appSessionBean, false, "", false, null, false,
				order.getProfileNumber() != appSessionBean.getProfileNumber(), // CP-12056 RAR
				replacements, volatileSessionBean, false); // CAP-33906 - not repeat order

		//CAP-47255
		volatileSessionBean.setShoppingCartCount(volatileSessionBean.getOrderId(), appSessionBean.getCustomToken());
		
		if (!hasTemplate) {
			processNonTemplateResult(appSessionBean, copyOrderResult, errorMessages, volatileSessionBean, orderID,
					errorlist, replacements);
		} else {
			sendErrorDueToKit(copyOrderResult, appSessionBean, errorMessages);
		}
	}

	protected void processNonTemplateResult(AppSessionBean appSessionBean, CopyOrderResult copyOrderResult,
			List<String> errorMessages, VolatileSessionBean volatileSessionBean, int orderID,
			ArrayList<String> errorlist, OEReplacementsSessionBean replacements) throws AtWinXSException {
		if (replacements.isEmpty()) {
			parseErrorMessages(errorlist, appSessionBean, copyOrderResult, errorMessages);

			// CP-11709 Set order ID for redirect to shipping cart
			if (volatileSessionBean.getOrderId() != null) {
				copyOrderSaveSessions(appSessionBean, copyOrderResult, errorlist, volatileSessionBean, logger);
			}
		} else {
			sendCopyToReplacements(copyOrderResult, errorMessages, volatileSessionBean, orderID);
		}
	}

	protected void parseErrorMessages(ArrayList<String> errorlist, AppSessionBean appSessionBean,
			CopyOrderResult copyOrderResult, List<String> errorMessages) {
		// CP-11709 [PDN] if has errors
		if (!errorlist.isEmpty()) {
			String errorMessage = getTranslation(appSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR3_MSG,
					AtWinXSConstant.EMPTY_STRING);
			errorMessages.add(errorMessage);

			for (String error : errorlist) {
				errorMessages.add(error);
			}

			copyOrderResult.setRedirectToCart(false);
			copyOrderResult.setRedirectToReplacement(false);
			copyOrderResult.setError(errorMessages);
		}
	}

	protected void copyOrderSaveSessions(AppSessionBean appSessionBean, CopyOrderResult copyOrderResult,
			ArrayList<String> errorlist, VolatileSessionBean volatileSessionBean, Logger logger)
			throws AtWinXSException {
		if (!errorlist.isEmpty()) { // CP-13606 - save the error for showing at the cart
			SessionContainer sc = getSessionContainer(appSessionBean);
			ApplicationVolatileSession avs = sc.getApplicationVolatileSession();
			OrderEntrySession oeSession = (OrderEntrySession) loadOESession(appSessionBean.getSessionID(),
					appSessionBean, avs.getVolatileSessionBean().isRequestorLocked());
			String error = getErrorMessageFromList(copyOrderResult, "<br />");
			oeSession.getOESessionBean().setUsabilityRedirectWarningMessage(error);
			Message msg = new Message();
			msg.setErrGeneralMsg(error);
			avs.putParameter(AtWinXSConstant.MSG_ATTRIB_NAME, msg);

			try {
				saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
				saveSession(avs, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID); // CP-11840
			} catch (AtWinXSException ae) {
				logger.error(this.getClass().getName() + " - " + ae.getMessage(), ae);
			}
		}

		copyOrderResult.setRedirectToCart(true);
		copyOrderResult.setOrderID(volatileSessionBean.getOrderId().toString());
		copyOrderResult.setOrderScenarioNum(Integer.valueOf(volatileSessionBean.getOrderScenarioNumber()));
	}

	protected SessionContainer getSessionContainer(AppSessionBean appSessionBean) throws AtWinXSException {
		return SessionHandler.getFullSessionInfo(appSessionBean.getSessionID(),
				AtWinXSConstant.ORDER_SEARCH_SERVICE_ID);
	}

	protected String getErrorMessageFromList(CopyOrderResult copyOrderResult, String nextLine) {
		StringBuilder error = new StringBuilder();
		boolean firstString = true;
		for (String err : copyOrderResult.getError()) {
			if (firstString) {
				firstString = false;
			} else {
				error.append(nextLine);
			}
			error.append(err);
		}
		return error.toString();
	}

	protected BaseSession loadOESession(int sID, AppSessionBean asb, boolean requestorLocked) throws AtWinXSException {
		BaseSession bc = null;
		try {
			bc = SessionHandler.loadSession(sID, AtWinXSConstant.ORDERS_SERVICE_ID);
		} catch (Exception e) {
			bc = initOESession(asb, requestorLocked);
		}
		return bc;
	}

	protected BaseSession initOESession(AppSessionBean asb, boolean requestorLocked) throws AtWinXSException {
		OrderEntrySession oeSession = new OrderEntrySession();
		SessionHandler.initNewModuleSession(asb, null, requestorLocked, oeSession, AtWinXSConstant.ORDERS_SERVICE_ID,
				org.apache.logging.log4j.LogManager.getLogger(CopyRecentOrderServiceImpl.class));
		return oeSession;
	}

	protected void sendCopyToReplacements(CopyOrderResult copyOrderResult, List<String> errorMessages,
			VolatileSessionBean volatileSessionBean, int orderID) {
		// CP-11709 [PDN] replacement
		copyOrderResult.setRedirectToCart(false);
		copyOrderResult.setRedirectToReplacement(true);
		copyOrderResult.setError(errorMessages);
		// CAP-24276
		volatileSessionBean.setShoppingCartCount(0);
		volatileSessionBean.setOrderId(orderID);
	}

	protected void sendErrorDueToKit(CopyOrderResult copyOrderResult, AppSessionBean appSessionBean,
			List<String> errorMessages) {
		// CP-11709 [PDN] if has kit
		String errorMessage = getTranslation(appSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR5_MSG,
				AtWinXSConstant.EMPTY_STRING);
		errorMessages.add(errorMessage);
		copyOrderResult.setRedirectToCart(false);
		copyOrderResult.setRedirectToReplacement(false);
		copyOrderResult.setError(errorMessages);
	}

	protected void persistInSession(ApplicationVolatileSession applicationVolatileSession) throws AtWinXSException {
		applicationVolatileSession.setIsDirty(true);
		saveSession(applicationVolatileSession, applicationVolatileSession.getSessionID(),
				AtWinXSConstant.APPVOLATILESESSIONID);
	}
}