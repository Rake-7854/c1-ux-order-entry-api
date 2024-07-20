/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	06/22/23				L De Leon				CAP-41373					Initial Version
 *	06/28/23				L De Leon				CAP-41373					Refactored code for JUnits
 *	03/22/24				L De Leon				CAP-47969					Added code to validate budget allocations
 */
package com.rrd.c1ux.api.services.checkout;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderRequest;
import com.rrd.c1ux.api.models.orders.copy.QuickCopyOrderResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.budgetallocation.BudgetAllocationService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.orders.copy.QuickCopyOrderService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchVO;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
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

@Service
public class QuickCopyOrderServiceImpl extends BaseOEService implements QuickCopyOrderService {

	private static final Logger logger = LoggerFactory.getLogger(QuickCopyOrderServiceImpl.class);

	// CAP-47969
	private final BudgetAllocationService budgetAllocationService;

	protected QuickCopyOrderServiceImpl(TranslationService translationService, OEManageOrdersComponentLocatorService oeManageOrdersLocatorService,
			BudgetAllocationService budgetAllocationService) {
		super(translationService, oeManageOrdersLocatorService);
		this.budgetAllocationService = budgetAllocationService;
	}

	@Override
	public QuickCopyOrderResponse quickCopyOrder(SessionContainer sc, QuickCopyOrderRequest quickCopyOrderRequest)
			throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		if (!oeOrderSessionBean.getUserSettings().isAllowQuickCopy() || appSessionBean.isPunchout()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		QuickCopyOrderResponse response = new QuickCopyOrderResponse();

		// validate no current order exists
		boolean hasNoOrderInProgress = validateNoOrderInProgress(response, sc, appSessionBean);

		// validate requested order exists and already submitted
		int requestedOrderID = quickCopyOrderRequest.getOrderID();
		boolean isOrderExistingAndSubmitted = hasNoOrderInProgress
				&& validateOrderExistandSubmitted(response, requestedOrderID, appSessionBean);

		// validate requested order matches last submitted order id in
		// volatileSessionBean
		boolean isRequestedOrderMatchedSession = isOrderExistingAndSubmitted
				&& isRequestedOrderMatchedLastSubmittedOrder(appSessionBean, response, requestedOrderID,
						volatileSessionBean.getLastSubmittedOrderID());

		if (isRequestedOrderMatchedSession) {
			// all validations passed, proceed with copying order
			try {
				handleCopyOrder(oeSession, oeOrderSessionBean, appSessionBean, volatileSessionBean, response);
				applicationVolatileSession.getVolatileSessionBean().clearLastSubmittedOrderID();
				persistInSession(applicationVolatileSession);
			} catch (AtWinXSException ex) {
				logger.error("QuickCopy - quickCopyOrder() call to handleCopyOrder(), failed to copy the order", ex);
				response.setSuccess(false);
				response.setMessage(
						getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_NOT_COPIED_AT_THIS_TIME_ERR_MSG,
								SFTranslationTextConstants.ORDER_NOT_COPIED_AT_THIS_TIME_DEF_ERR_MSG));
			}
		}

		return response;
	}

	protected boolean isRequestedOrderMatchedLastSubmittedOrder(AppSessionBean appSessionBean,
			QuickCopyOrderResponse response, int requestedOrderID, int lastSubmittedOrderID) {

		boolean isRequestedOrderMatchedLastSubmittedOrder = lastSubmittedOrderID > AtWinXSConstant.INVALID_ID
				&& requestedOrderID == lastSubmittedOrderID;

		if (!isRequestedOrderMatchedLastSubmittedOrder) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_NOT_COPIED_ERR_MSG,
					SFTranslationTextConstants.ORDER_NOT_COPIED_DEF_ERR_MSG));
		}

		return isRequestedOrderMatchedLastSubmittedOrder;
	}

	// Copied method from OrderConfirmationController.handleCopyOrder() with
	// modifications
	// Need to add code for replacements and error handling - currently not in scope
	// for C1UX Phase 1b release
	protected void handleCopyOrder(OrderEntrySession oeSession, OEOrderSessionBean oeOrderSessionBean,
			AppSessionBean applicationSessionBean, VolatileSessionBean volatileSessionBean,
			QuickCopyOrderResponse response) throws AtWinXSException {

		Message m = new Message();
		boolean isRedirectToCart = true;
		// CAP-41373 removed boolean isRedirectToReplacement declaration as replacements
		// are out of scope
		OEReplacementsSessionBean replacements = null;

		OESavedOrderAssembler assembler = getSavedOrderAssembler(applicationSessionBean, volatileSessionBean);

		if (volatileSessionBean.getShoppingCartCount() > 0) {
			isRedirectToCart = false;
			m.setErrGeneralMsg(getTranslation(applicationSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR1_MSG, AtWinXSConstant.EMPTY_STRING));
		} else {
			int orderID = volatileSessionBean.getLastSubmittedOrderID();
			isRedirectToCart = !validateEnforeOnOrdering(oeOrderSessionBean, applicationSessionBean,
					volatileSessionBean, m, assembler, orderID);

			ArrayList<String> errorlist = new ArrayList<>();
			boolean lockedListFeedsPreventQuickCopy = assembler.orderLocksListFeedRecords(orderID, errorlist,
					applicationSessionBean);
			replacements = new OEReplacementsSessionBean(OrderEntryConstants.REPLACEMENT_SELECTION_TYPE_COPY, orderID,
					null, null);
			boolean copiedOrderFromFileContainsKitTemplate = !lockedListFeedsPreventQuickCopy && assembler
					.copyRecentOrder(applicationSessionBean.getSiteID(), applicationSessionBean.getLoginID(), orderID,
							oeOrderSessionBean, errorlist, applicationSessionBean, false, "", false, null, true, false,
							replacements, volatileSessionBean, false); // CAP-33906 - cannot repeat order from order
																		// confirmation

			if ((!copiedOrderFromFileContainsKitTemplate) && (!lockedListFeedsPreventQuickCopy)) {
				isRedirectToCart = isRedirectToCartIfNoReplacements(applicationSessionBean, m, isRedirectToCart,
						replacements, errorlist);
			} else if (lockedListFeedsPreventQuickCopy) {
				isRedirectToCart = false;
				// Orders using list feeds which lock records cannot be Quick Copied.
				// CP-8970 changed token from String to an Object
				m.setErrGeneralMsg(getTranslation(applicationSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR4_MSG, null));
			} else {
				isRedirectToCart = false;
				// Order from file orders containing kit/kit templates cannot be copied.
				m.setErrGeneralMsg(getTranslation(applicationSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR5_MSG, null));
			}
		}

		setCopyOrderError(oeSession, m);

		// CAP-47969
		String message = handleBudgetAllocations(oeOrderSessionBean, applicationSessionBean, isRedirectToCart, m);

		response.setSuccess(isRedirectToCart);
		response.setMessage(Util.nullToEmpty(message));
	}

	// CAP-47969
	protected String handleBudgetAllocations(OEOrderSessionBean oeOrderSessionBean, AppSessionBean appSessionBean,
			boolean isRedirectToCart, Message m) {
		String budgetDepletedErrorMsg = budgetAllocationService.validateBudgetAllocations(oeOrderSessionBean,
				appSessionBean, isRedirectToCart);
		StringBuilder errorMsg = new StringBuilder();
		if (!Util.isBlankOrNull(budgetDepletedErrorMsg)) {
			errorMsg.append(budgetDepletedErrorMsg);
		}

		if (!Util.isBlankOrNull(m.getErrGeneralMsg())) {
			if (!Util.isBlankOrNull(errorMsg.toString())) {
				errorMsg.append(ModelConstants.BREAK);
			}
			errorMsg.append(m.getErrGeneralMsg());

			if (null != m.getErrMsgItems() && !m.getErrMsgItems().isEmpty()) {
				for (String error : m.getErrMsgItems()) {
					errorMsg.append(ModelConstants.BREAK);
					errorMsg.append(ModelConstants.RIGHT_ANGLE_QUOTE).append(AtWinXSConstant.BLANK_SPACE).append(error);
				}
			}
		}
		return errorMsg.toString();
	}

	protected boolean isRedirectToCartIfNoReplacements(AppSessionBean applicationSessionBean, Message m,
			boolean isRedirectToCart, OEReplacementsSessionBean replacements,
			ArrayList<String> errorlist) {
		if (replacements.isEmpty()) {
			if (!errorlist.isEmpty()) {
				// Not all items from the original order may be included in this order. The
				// following item(s) have been removed:
				// CP-8970 changed token from String to an Object
				m.setErrGeneralMsg(getTranslation(applicationSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR3_MSG, null));
				m.setErrMsgItems(errorlist);
			}
		} else {
			isRedirectToCart = false;
			// CAP-41373 removed isRedirectToReplacement assignment to true as replacements
			// are out of scope
		}
		return isRedirectToCart;
	}

	protected boolean validateEnforeOnOrdering(OEOrderSessionBean oeOrderSessionBean,
			AppSessionBean applicationSessionBean, VolatileSessionBean volatileSessionBean, Message m,
			OESavedOrderAssembler assembler, int orderID) throws AtWinXSException {
		boolean hasErrors = false;
		if (applicationSessionBean.hasEnforceOnOrdering()) {
			ArrayList<OrderAttributeVO> orderAttrVOs = assembler.getOrderAttrsByOrderId(orderID);
			ArrayList<UserGroupSearchVO> userGroupOpts = oeOrderSessionBean.getUsrSrchOptions();
			if (orderAttrVOs != null && !orderAttrVOs.isEmpty() && userGroupOpts != null && !userGroupOpts.isEmpty()
					&& assembler.validateOrdAttrWithProfileAttr(userGroupOpts, orderAttrVOs,
							applicationSessionBean.getProfileAttributes(), applicationSessionBean.getSiteID())) {
				HashMap<Integer, SiteAttrValuesVO[]> siteAttribMap = assembler.getSiteAttribMap(orderAttrVOs,
						applicationSessionBean);
				volatileSessionBean.setSelectedMASSSiteAttribute(assembler.getMassAttributeMap(orderAttrVOs));
				// CAP-10262
				assembler.loadOrderAttributeForValidOrder(siteAttribMap, oeOrderSessionBean, applicationSessionBean);
			} else {
				hasErrors = true;
				// Selected Order can not be continued as profile attributes have been changed
				// CP-8970 changed token from String to an Object
				m.setErrGeneralMsg(getTranslation(applicationSessionBean, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR2_MSG, null));
			}
		}
		return hasErrors;
	}

	protected void setCopyOrderError(OrderEntrySession oeSession, Message m) throws AtWinXSException {
		if (!Util.isBlankOrNull(m.getErrGeneralMsg())) {
			oeSession.putParameter("copyOrderError", m);
			oeSession.setClearParameters(false);
		}
	}

	protected void persistInSession(ApplicationVolatileSession applicationVolatileSession) throws AtWinXSException {
		applicationVolatileSession.setIsDirty(true);
		SessionHandler.saveSession(applicationVolatileSession, applicationVolatileSession.getSessionID(),
				AtWinXSConstant.APPVOLATILESESSIONID);
	}
}