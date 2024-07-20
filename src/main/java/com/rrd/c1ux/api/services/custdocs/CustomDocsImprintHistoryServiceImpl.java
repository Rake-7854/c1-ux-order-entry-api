/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 * 	11/10/23	A Boomker			CAP-44487		Added load user profile
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 *	06/26/24	R Ruth				CAP-46503		Added loadAltProfiles
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBasicImprintHistorySearchRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocImprintHistorySearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadImprintHistoryRequest;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.ImprintHistorySelector;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class CustomDocsImprintHistoryServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsImprintHistoryService {
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsImprintHistoryServiceImpl.class);

	public CustomDocsImprintHistoryServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
	        SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	public C1UXCustDocImprintHistorySearchResponse basicImprintHistorySearch(SessionContainer sc, C1UXCustDocBasicImprintHistorySearchRequest request)
			throws AtWinXSException {
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();

		if (!asb.isAllowImpHistSrchInd()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		C1UXCustDocImprintHistorySearchResponse response = new C1UXCustDocImprintHistorySearchResponse();
		try {
			if ((ui.getImprintHistorySelector() != null) && (ui.getImprintHistorySelector().isShown()))
			{
				ui.getImprintHistorySelector().setMyOrdersOnly(request.isMyOrdersOnly());
				ui.getImprintHistorySelector().setThisItemOnly(request.isThisItemOnly());
				ui.getImprintHistorySelector().loadOrderHistoryOptions();
			}
			response.setImprintHistory(loadImprintHistory(ui, getCurrentPageOutsideSubmit(ui), asb, true));
			saveFullOESessionInfo(oeSession, asb.getSessionID());
			response.setSuccess(true);
		} catch(Exception e) {
			logger.error("Exception thrown attempting to search history", e);
			response.setMessage(getUnhandledSearchError(asb));
		}

		return response;
	}

	@Override
	public C1UXCustDocBaseResponse getSelectedImprintHistory(SessionContainer sc, C1UXCustDocLoadImprintHistoryRequest request) throws AtWinXSException {
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
		CustomDocumentItem item = oeOrderSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();
		ImprintHistorySelector selector = ui.getImprintHistorySelector();
		loadHistorySelection(ui, selector, sc, response, request.getCustomDocOrderLineID());
		return response;
	}

	protected String getHistoryErrorNoneAvailable(AppSessionBean appSessionBean) {
		return getTranslation(appSessionBean, SFTranslationTextConstants.TRANS_NM_UI_NO_IMPRINT_HISTORY,
				SFTranslationTextConstants.TRANS_NM_UI_NO_IMPRINT_HISTORY_DEFAULT);
	}

	protected String getHistoryErrorSelectedNotAvailable(AppSessionBean appSessionBean) {
		return getTranslation(appSessionBean, SFTranslationTextConstants.TRANS_NM_UI_CANNOT_SELECT_IMPRINT_HISTORY,
				SFTranslationTextConstants.TRANS_NM_UI_CANNOT_SELECT_IMPRINT_HISTORY_DEFAULT);
	}

	protected void loadHistorySelection(UserInterface ui, ImprintHistorySelector selector, SessionContainer sc,
			C1UXCustDocBaseResponse response, int selectedCustomDocOrderLine) throws AccessForbiddenException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		if ((selector == null) || (!selector.isShown()) || (!onEditableFirstPageOE(ui))) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		if (validateHistorySelection(selector, selectedCustomDocOrderLine, response, appSessionBean)) {
			ArrayList<String> errors = new ArrayList<>();
			try {
				VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
				OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
				OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

				ui.populateFromImprintHistory(appSessionBean, volatileSessionBean, selectedCustomDocOrderLine, errors, oeOrderSessionBean);
				saveFullOESessionInfo(oeSession, appSessionBean.getSessionID()); //save the session
				response.setMessage(getCombinedMessage(errors));
				response.setSuccess(true);
			} catch (AtWinXSException e) {
				response.setMessage(getHistoryErrorSelectedNotAvailable(appSessionBean));
			}
		}
	}

	protected boolean validateHistorySelection(ImprintHistorySelector selector, int selectedCustomDocOrderLine,
			C1UXCustDocBaseResponse response, AppSessionBean appSessionBean) {
		if (selector.getHistoryOptions() == null) {
			try {
				selector.loadOrderHistoryOptions();
			} catch (AtWinXSException e) {
				logger.error(e.toString());
				response.setMessage(getHistoryErrorNoneAvailable(appSessionBean));
				return false;
			}
		}

		if ((selector.getHistoryOptions() != null) && (selector.isOrdersFound())) {
			if ((selectedCustomDocOrderLine > 0) // the ID must be a positive number
					&& (!selector.getHistoryOptions().stream().noneMatch
			        (option -> option.getCustDocOrderLineNum() == selectedCustomDocOrderLine))) {
					return true;
			} else {
				response.setMessage(getHistoryErrorSelectedNotAvailable(appSessionBean));
			}
		} else {
			response.setMessage(getHistoryErrorNoneAvailable(appSessionBean));
		}

		return false;
	}

}
