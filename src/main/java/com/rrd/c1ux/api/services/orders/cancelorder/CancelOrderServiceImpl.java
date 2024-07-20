/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	07/28/23	Sakthi M			 CAP-42545	C1UX BE - Create API to cancel the current order (cancelOrderInProgress)
 */

package com.rrd.c1ux.api.services.orders.cancelorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.cancelorder.CancelOrderResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.entity.ProfileSelection;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.orderentry.ao.OECancelOrderAssembler;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class CancelOrderServiceImpl extends BaseOEService implements CancelOrderService {

	private final SessionHandlerService sessionHandlerService;
	protected CancelOrderServiceImpl(TranslationService translationService, SessionHandlerService sessionHandlerService) {
		super(translationService);
		this.sessionHandlerService=sessionHandlerService;
	}

	private static final Logger logger = LoggerFactory.getLogger(CancelOrderServiceImpl.class);

	@Override
	public CancelOrderResponse cancelOrder(SessionContainer sc, ApplicationSession appSession,
			AppSessionBean appSessionBean, OrderEntrySession oeSession, OECancelOrderAssembler oeCancelOrderAssembler)
			throws AtWinXSException {

		boolean isOrderRequest = false;
		boolean isFailedException = false;
		CancelOrderResponse response = new CancelOrderResponse();
		checkAccessForbiden(appSessionBean, response);
		if (oeSession.getOESessionBean() != null) { // look for distribution list
			isOrderRequest = oeCancelOrderAssembler
					.isOrderRequest(oeSession.getOESessionBean().getOrderScenarioNumber());
		}

		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		Integer orderId = volatileSessionBean.getOrderId();
		if (orderId != null) {
			checkOrderNotNewStatus(appSession, response, orderId);
			try {
				boolean isAllowTeamOrderSharing = oeSession.getOESessionBean().getUserSettings()
						.isAllowTeamOrderSharing();
				int teamOrderSharingSiteAttr = oeSession.getOESessionBean().getUserSettings()
						.getTeamOrdSharingSiteAttr();
				oeCancelOrderAssembler.performCancelOrder(orderId, true, isAllowTeamOrderSharing,
						teamOrderSharingSiteAttr, appSessionBean.getSessionID());
				volatileSessionBean.setSelectedSiteAttribute(null);
				volatileSessionBean.setSelectedMASSSiteAttribute(null); // CAP-10262
				oeSession.clearOrder(volatileSessionBean, appSessionBean);

				// CAP-15495 SRN Set loaded for catalog refresh
				ProfileSelection profileSelectionObj = oeSession.getOESessionBean().getProfileSelections();
				if (appSessionBean.isRefreshCatalogEnabled() && null != profileSelectionObj
						&& profileSelectionObj.isHasEnforceOnCatalog()
						&& null != profileSelectionObj.getProfileSelections()
						&& !profileSelectionObj.getProfileSelections().isEmpty()) {
					boolean isReset = profileSelectionObj.resetLoadedForCatRefresh(appSessionBean, oeSession,
							volatileSessionBean, true);
					if (isReset) {
						SessionHandler.saveSession(appSession, appSessionBean.getSessionID(),
								AtWinXSConstant.APPSESSIONSERVICEID);
					}
				}

			} catch (Exception e) {
				isFailedException = true;
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(),
						SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.CANCEL_ORDER_FAILED));
				// CAP-35769 - punchout C1UX users are missing some settings causing this to
				// blow up
				if (appSession.getPunchoutSessionBean() != null) { // do NOT let this prevent the return from a
																	// punchout!
					logger.error("Failed to fully cancel order " + orderId + ": " + e.toString(), e);
				} else if (e instanceof AtWinXSException) {
					throw (AtWinXSException) e;
				} else {
					throw new AtWinXSWrpException(e, this.getClass().getName());
				}
			}
		}

		cancelOFFAndClearOSOverrides(isOrderRequest, oeSession, volatileSessionBean, response, oeCancelOrderAssembler,
				appSession, isFailedException);

		ApplicationVolatileSession appVolatileSession = (ApplicationVolatileSession) sessionHandlerService.loadSession(appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
		appVolatileSession.getVolatileSessionBean().setOrderId(orderId);
		appVolatileSession.getVolatileSessionBean().setShoppingCartCount(volatileSessionBean.getShoppingCartCount());
		SessionHandler.saveSession(appVolatileSession, appSessionBean.getSessionID(),
				AtWinXSConstant.APPVOLATILESESSIONID);
		SessionHandler.saveSession(appSession, appSessionBean.getSessionID(), AtWinXSConstant.APPSESSIONSERVICEID);

		return response;
	}

	public void checkAccessForbiden(AppSessionBean appSessionBean, CancelOrderResponse response)
			throws AtWinXSException {
		if (!appSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID)) {
			response.setSuccess(false);
			throw new AccessForbiddenException(CancelOrderServiceImpl.class.getName());
		}
	}

	public void checkOrderNotNewStatus(ApplicationSession appSession, CancelOrderResponse response, int orderId)
			throws AtWinXSException {
		if (appSession.getPunchoutSessionBean() != null || !isCancelNewOrderOnly(orderId)) {
			response.setSuccess(false);
			throw new AccessForbiddenException(CancelOrderServiceImpl.class.getName());
		}
	}

	public void cancelOFFAndClearOSOverrides(boolean isOrderRequest, OrderEntrySession oeSession,
			VolatileSessionBean volatileSessionBean, CancelOrderResponse response,
			OECancelOrderAssembler oeCancelOrderAssembler, ApplicationSession appSession, boolean isFailedException)
			throws AtWinXSException {

		AppSessionBean appSessionBean = appSession.getAppSessionBean();

		if (isOrderRequest) {
			if (OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE == oeSession.getOESessionBean()
					.getOrderScenarioNumber()) {
				if ((OrderEntryConstants.IN_PROCESS_CANCEL_ORDER_TEXT
						.equals(volatileSessionBean.getCancelProcessMessageText())))
					oeSession.getOESessionBean().setOrderFromFileBean(null);
			} else {
				oeSession.getOESessionBean().setDistributionListBean(null);
			}
		}

		volatileSessionBean.clearStacks();
		volatileSessionBean.setOrderScenarioNumber(OrderEntryConstants.ORDER_SCENARIO_NUMBER_DEFAULT_VALUE);
		try {
			volatileSessionBean.cancelOrderFromFile(appSessionBean.getCustomToken());
		} catch (AtWinXSException ignored) { // ignore it
		}

		try {
			oeCancelOrderAssembler.clearOrderSessionOverrides(appSession);
		} catch (AtWinXSException e) {
			logger.error("Failed in CancelOrderServiceImpl " + e.getMessage(), e); // CAP-16459
			response.setSuccess(false);
		}

		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
		response.setSuccess(!isFailedException);

	}

}
