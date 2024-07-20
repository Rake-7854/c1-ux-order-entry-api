/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  04/03/24	A Boomker		CAP-46494					Proofing overrides for bundle
 */
package com.rrd.c1ux.api.services.custdocs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.bundle.BundleUserInterface;
import com.rrd.custompoint.orderentry.customdocs.ProofControl;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.entity.BundleItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

public class BundleProofingServiceImpl extends CustomDocsProofingServiceImpl implements CustomDocsProofingService {

	private static final Logger logger = LoggerFactory.getLogger(BundleProofingServiceImpl.class);

	public BundleProofingServiceImpl(CustomDocsProofingService service, SessionContainer sc) {
		super(service.getTranslationService(), service.getObjectMapFactoryService(), service.getSessionHandlerService());
		this.setCustomizationTokenForSession(sc.getApplicationSession().getAppSessionBean().getCustomToken());
	}

	// the next constructor version is ONLY to be used for junits!
	public BundleProofingServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFacService,
			SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	public C1UXCustDocProofStatusResponse getCurrentImageProofStatus(SessionContainer sc) throws AtWinXSException
	{
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocProofStatusResponse response = new C1UXCustDocProofStatusResponse();
		try {
			BundleItem item = (BundleItem) oeSessionBean.getCurrentCustomDocumentItem();
			BundleUserInterface ui = item.getUserInterface();
			String transID = getCurrentBundleComponentProofID(ui, ProofType.IMAGE);
			UserInterface compUI = getCurrentComponentUserInterface(ui);
			if (!Util.isBlankOrNull(transID))
			{
				ProofControl proofControl = compUI.getNumberOfPages(transID, appSessionBean.getCurrentEnvCd(), true); // in Bundle OE, always create proofs
				populateStatusResponse(proofControl, response);
			}
			else
			{
				setProofFailedMessage(response, appSessionBean);
			}
		} catch(Exception e) {
			logger.error("Exception thrown attempting to get image proof status", e);
			setProofFailedMessage(response, appSessionBean);
		}
		return response;
	}

	@Override
	public C1UXCustDocProofLinkResponse getProofLink(SessionContainer sc, C1UXCustDocProofLinkRequest request)
			throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocProofLinkResponse response = new C1UXCustDocProofLinkResponse();
		try {
			BundleItem item = (BundleItem) oeSessionBean.getCurrentCustomDocumentItem();
			BundleUserInterface ui = item.getUserInterface();
			ProofType proofType = lookupProofType(request.getProofType());
			if (proofType == ProofType.IMAGE)
			{
				String transID = getCurrentBundleComponentProofID(ui, proofType);
				// image proof is not generated on the fly
				if (!Util.isBlankOrNull(transID))
				{
					if (request.getProofPageNbr() <= 0)
					{ // default to page 1 if not provided
						request.setProofPageNbr(1);
					}
					response.setProofUrl(ui.getProofURL(proofType, appSessionBean.getCurrentEnvCd(), ui.isAlwaysSsl(),
							transID, String.valueOf(request.getProofPageNbr())));
					response.setSuccess(true);
				}
				else
				{
					setProofFailedMessage(response, appSessionBean);
				}
			}
			else if (proofType != ProofType.EMAIL)
			{
				generateNonImageProof(ui, response, proofType, sc);
			}
			else
			{
				setProofFailedMessage(response, appSessionBean);
			}
		} catch(Exception e) {
			logger.error("Exception thrown attempting to get proof link", e);
			setProofFailedMessage(response, appSessionBean);
		}
		return response;
	}

	protected void generateNonImageProof(BundleUserInterface ui,
			C1UXCustDocProofLinkResponse response, ProofType proofType, SessionContainer sc) throws AtWinXSException {
		String proofID = getCurrentBundleComponentProofID(ui, proofType);
		String pageNum = (proofType == ProofType.PDF) ? "1" : "0";
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();

		if (Util.isBlankOrNull(proofID))
		{
			proofID = generateNewBundleComponentProofID(ui, sc, ui.getConvertedCustDocItemsMap(), proofType);
			if (!Util.isBlankOrNull(proofID)) {
				saveFullOESessionInfo(oeSession, appSessionBean.getSessionID());
			}
		}

		if (!Util.isBlankOrNull(proofID)) {
			String url = ui.getProofURL(proofType, appSessionBean.getCurrentEnvCd(), appSessionBean.alwaysUseSSL(), proofID, pageNum);

			if (proofType == ProofType.PDF) {
			// Create an audit record for the proof
				createPdfProofAuditRecord(ui, proofID, appSessionBean, oeSession.getOESessionBean());
			}
			response.setProofUrl(url);
			response.setSuccess(true);
		}
		else
		{
			setProofFailedMessage(response, appSessionBean);
		}
	}

	protected UserInterface getCurrentComponentUserInterface(BundleUserInterface bundleUI)
	{
		CustomDocumentItem component = getBundleComponentFromMap(bundleUI.getConvertedCustDocItemsMap(), bundleUI.getBundleComponentSequenceCtr());
		return (component != null) ? component.getUserInterface() : bundleUI;
	}

}
