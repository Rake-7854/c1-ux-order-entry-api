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
 * 	08/07/23	A Boomker			CAP-42223		Initial version
 * 	09/12/23	A Boomker			CAP-42839		Added session save if new proof is generated
 *  11/13/23	A Boomker			CAP-44426		Added handling for update working proof
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 *  04/03/24	A Boomker			CAP-46494		Proofing overrides for bundle
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofLinkResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProofStatusResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocWorkingProofResponse;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.ProofControl;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.entity.ProofTrackingAudit;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.customdocs.compserv.util.CSStatusResponse;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class CustomDocsProofingServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsProofingService {
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsProofingServiceImpl.class);
	private static final String PROOF_GENERATION_FAILED_MSG = "A problem occured trying to create your proof.  Please contact support.";

	public CustomDocsProofingServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
	        SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	public C1UXCustDocProofStatusResponse getCurrentImageProofStatus(SessionContainer sc) throws AtWinXSException
	{
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocProofStatusResponse response = new C1UXCustDocProofStatusResponse();
		try {
			CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
			UserInterface ui = item.getUserInterface();
			if ((validateImageProofType(ui, false, null)) && (ui.getProofID(ProofType.IMAGE) != null))
			{
				String transID = ui.getProofID(ProofType.IMAGE);
				ProofControl proofControl = ui.getNumberOfPages(transID, appSessionBean.getCurrentEnvCd(), true); // in CDOE, always create proofs
				populateStatusResponse(proofControl, response);
			}
			else
			{
				response.setMessage("Hardcoded - UI does not use image proof type");
			}
		} catch(Exception e) {
			logger.error("Exception thrown attempting to get image proof status", e);
			setProofFailedMessage(response, appSessionBean);
		}
		return response;
	}

	protected void populateStatusResponse(ProofControl proofControl, C1UXCustDocProofStatusResponse response) {
		response.setCompleted(proofControl.getStatus() == CSStatusResponse.StatusCodes.Success);
		response.setAnyReady(((proofControl.getStatus() == CSStatusResponse.StatusCodes.Success) ||
				(proofControl.getStatus() == CSStatusResponse.StatusCodes.Processing)) && (proofControl.getNumPages() > 0));
		response.setSuccess(proofControl.getStatus() != CSStatusResponse.StatusCodes.Failure);
		if (response.isSuccess()) {
			response.setLastProofPageNbr(proofControl.getNumPages());
		}
	}

	protected boolean validateImageProofType(UserInterface ui, boolean allowWorkingProof, ProofType pt)
	{
		if (pt == null) {
			pt = getProofType(ui);
		}
		return ((ProofType.IMAGE == pt) || (allowWorkingProof && (ProofType.WORKING == pt)));
	}

	@Override
	public C1UXCustDocProofLinkResponse getProofLink(SessionContainer sc, C1UXCustDocProofLinkRequest request)
			throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocProofLinkResponse response = new C1UXCustDocProofLinkResponse();
		try {
			CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
			UserInterface ui = item.getUserInterface();
			ProofType proofType = lookupProofType(request.getProofType());
			if (validateImageProofType(ui, true, proofType))
			{
				String transID = ui.getProofID(proofType);
				if (request.getProofPageNbr() <= 0)
				{ // default to page 1 if not provided
					request.setProofPageNbr(1);
				}
				response.setProofUrl(ui.getProofURL(proofType, appSessionBean.getCurrentEnvCd(), ui.isAlwaysSsl(),
						transID, String.valueOf(request.getProofPageNbr())));
				response.setSuccess(true);
			}
			else if (proofType != ProofType.EMAIL)
			{
				generateNonImageProof(ui, appSessionBean, response, proofType, oeSession);
			}
			else
			{
				response.setMessage("Hardcoded - UI does not use current proof type");
			}
		} catch(Exception e) {
			logger.error("Exception thrown attempting to get proof link", e);
			setProofFailedMessage(response, appSessionBean);
		}
		return response;
	}

	protected void setProofFailedMessage(C1UXCustDocBaseResponse response, AppSessionBean appSessionBean) {
		response.setMessage(getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_CUST_DOC_PROOF_FAILURE, PROOF_GENERATION_FAILED_MSG));
	}

	protected void generateNonImageProof(UserInterface ui, AppSessionBean appSessionBean,
			C1UXCustDocProofLinkResponse response, ProofType proofType, OrderEntrySession oeSession) throws AtWinXSException {
			String proofID = ui.getProofID(proofType);
			if (proofID == null)
			{
				ui.generateNewProof(proofType, appSessionBean, null, null);
				proofID = ui.getProofID(proofType);
				if (!Util.isBlankOrNull(proofID)) {
					saveFullOESessionInfo(oeSession, appSessionBean.getSessionID());
				}
			}
			String pageNum = (proofType == ProofType.PDF) ? "1" : "0";
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

	protected ProofTrackingAudit createProofTrackingAudit(AppSessionBean appSessionBean) {
		return objectMapFactoryService.getEntityObjectMap().getEntity(ProofTrackingAudit.class, appSessionBean.getCustomToken());
	}

	// copied from CustDocAjaxController
	/**
	 * This method will create a tracking record for viewing the PDF proof.
	 * @param ui - The {@link UserInterface} that is being used to generate the proof.
	 * @param transID - The String holding the trans id for the proof.
	 */
	protected int createPdfProofAuditRecord(UserInterface ui, String transID, AppSessionBean appSessionBean, OEOrderSessionBean orderSessionBean) throws AtWinXSException
	{
		int trackingId = -1;

		ProofTrackingAudit proofTrackingAudit = createProofTrackingAudit(appSessionBean);
		if(proofTrackingAudit != null)
		{
			proofTrackingAudit.setSiteID(appSessionBean.getSiteID());
			proofTrackingAudit.setBusUnitID(appSessionBean.getBuID());
			proofTrackingAudit.setUserGroupName(orderSessionBean == null ? AtWinXSConstant.EMPTY_STRING : orderSessionBean.getUserGroupID());
			proofTrackingAudit.setUserID(appSessionBean.getLoginID());
			proofTrackingAudit.setTransactionID(transID);
			proofTrackingAudit.setItemNumber(ui == null ? AtWinXSConstant.EMPTY_STRING : ui.getCustomerItemNumber());
			proofTrackingAudit.setProofType(ProofTrackingAudit.ProofType.PDF);
			proofTrackingAudit.setCreateUserID(appSessionBean.getLoginID());
			proofTrackingAudit.setProfileID(appSessionBean.getProfileID());
			proofTrackingAudit.setVendorItemNumber(ui == null ? AtWinXSConstant.EMPTY_STRING : ui.getVendorItemNumber());

			proofTrackingAudit.saveTrackingInfo();

			trackingId = proofTrackingAudit.getTrackingID();
		}

		return trackingId;

	}

	@Override
	public C1UXCustDocWorkingProofResponse getWorkingProof(SessionContainer sc, Map<String, String> uiRequest) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocWorkingProofResponse response = new C1UXCustDocWorkingProofResponse();
		try {
			CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
			UserInterface ui = item.getUserInterface();
			String pageNum = AtWinXSConstant.EMPTY_STRING;
			if ((uiRequest != null) && (uiRequest.get(ICustomDocsAdminConstants.HDN_UI_STEP_PAGE_NUM) != null)) {
				pageNum = uiRequest.get(ICustomDocsAdminConstants.HDN_UI_STEP_PAGE_NUM);
			}
			Page thisPage = ui.getPage(Util.safeStringToDefaultInt(pageNum, -1));
			if (!loadWorkingProof(response, ui, thisPage, appSessionBean)) {
				defaultFailedWorkingProofValues(response, appSessionBean);
			}
		} catch(Exception e) {
			defaultFailedWorkingProofValues(response, appSessionBean);
		}
		response.setSuccess(true);
		return response;
	}

}
