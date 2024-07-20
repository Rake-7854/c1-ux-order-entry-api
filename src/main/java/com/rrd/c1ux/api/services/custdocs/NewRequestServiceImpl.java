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
 * 	03/11/24	A Boomker			CAP-46490		Initial version
 * 	03/15/24	A Boomker			CAP-46526		Changes for initialize and more
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.entity.CustomRequestInfo;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.NewRequestCustomDocumentItem;
import com.wallace.atwinxs.admin.vo.ProjectManagerVO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public class NewRequestServiceImpl extends CustomDocsServiceImpl implements NewRequestUIService {

	private static final Logger logger = LoggerFactory.getLogger(NewRequestServiceImpl.class);

	public NewRequestServiceImpl(CustomDocsService service, SessionContainer sc) {
		super(service.getTranslationService(), service.getObjectMapFactoryService(), service.getSessionHandlerService());
		this.setCustomizationTokenForSession(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		this.setOeJavascriptServerAccessPath(service.getOeJavascriptServerAccessPath());
	}

	// the next constructor version is ONLY to be used for junits!
	public NewRequestServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFacService,
			SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	protected String getProjectManagerEmails(CustomDocumentItem item) {
		StringBuilder emailAdds = new StringBuilder();
		if (item.getRequestPMs()!=null)
		{
			for (ProjectManagerVO pmVO: item.getRequestPMs())
			{
				if(emailAdds.length() == 0)
				{
					emailAdds.append(",");
				}
				emailAdds.append(Util.nullToEmpty(pmVO.getProjectManagerEmailAddress()));
			}
		}
		return emailAdds.toString();
	}

	protected int saveRequestData(AppSessionBean appSessionBean, String emailAdds, UserInterface ui) throws AtWinXSException {
		CustomRequestInfo custReqInfo = objectMapFactoryService.getEntityObjectMap().getEntity(CustomRequestInfo.class, appSessionBean.getCustomToken());
		custReqInfo.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getLoginID(), appSessionBean.getProfileNumber(),
				             ui.getWorkingXML(false, true), emailAdds, appSessionBean.getSiteLoginID(), appSessionBean.getCorporateNumber(), appSessionBean.getEmailAddress(), ui.getUiKey()); // CAP-13615, CAP-13861, CAP-17617 - Added ui.getUiKey() param
		custReqInfo.save();
		custReqInfo.sendCustomRequestEmail(ui); //CAP-13929
		return custReqInfo.getRequestID();
	}

	protected void saveAndSendCustomRequest(CustomDocumentItem item, SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		UserInterface ui = item.getUserInterface();
		String emailAdds = this.getProjectManagerEmails(item);

		int savedID = saveRequestData(appSessionBean, emailAdds, ui);
		Message msg = new Message();
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put("{requestnum}", savedID);
		msg.setSuccessMsg(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "newrequestsuccess", "Your Special Request #{requestnum} has been submitted.", replaceMap));
		oeSessionBean.setUsabilityRedirectSuccessMessage(msg);
		resetUIAndItem(sc.getApplicationVolatileSession().getVolatileSessionBean(), oeSessionBean);
		saveAllUpdatedSessions(oeSession, sc.getApplicationVolatileSession(), sc.getApplicationSession(), appSessionBean.getSessionID()); // CAP-44307
	}

	@Override
	public C1UXCustDocBaseResponse addToCart(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocBaseResponse sfBean = new C1UXCustDocBaseResponse();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();

		try {
			saveAndSendCustomRequest(item, sc);
			sfBean.setSuccess(true);
			sfBean.setRedirectRouting(RouteConstants.HOME);
		} catch (AtWinXSException e) {
			logger.error(failedAtWinXSExceptionPrefix, e);
			sfBean.setMessage(e.getMessage());
		} catch (Exception e) {
			logger.error(failedNonAtWinXSExceptionPrefix, e);
			sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					RouteConstants.CANNOT_COMPLETE_REQUEST));
		}

		return sfBean;

	}

	@Override
	protected CustomDocumentItem createAndInitItem(Map<String, String> requestParams, OEOrderSessionBean orderSession,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			PunchoutSessionBean punchoutSessionBean, Collection<String> errors) throws AtWinXSException {
		NewRequestCustomDocumentItem item = objectMapFactoryService.getEntityObjectMap().getEntity(NewRequestCustomDocumentItem.class,
				appSessionBean.getCustomToken()); // CAP-1434 create new item every time in initialize
		item.initializeItemC1UX(requestParams, appSessionBean, volatileSessionBean, -1, orderSession.getUserSettings(),
				orderSession, errors, punchoutSessionBean); // CAP-20338
		return item;
	}

}
