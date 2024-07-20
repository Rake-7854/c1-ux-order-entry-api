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
 * 	04/22/24	R Ruth				CAP-42226		Initial version
 *	05/28/24	A Boomker			CAP-48604		Add save new list API
 *	06/13/24	A Boomker			CAP-50156		Fix addList() to match BE
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXSaveListResponse;
import com.rrd.c1ux.api.models.custdocs.C1uxCustDocListDetails;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.lists.ao.ManageListsResponseBean;
import com.wallace.atwinxs.lists.ao.UploadListAssembler;
import com.wallace.atwinxs.lists.ao.UploadListRequestBean;
import com.wallace.atwinxs.lists.ao.UploadListResponseBean;
import com.wallace.atwinxs.lists.session.ManageListsSession;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class CustomDocsListServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsListService{
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsListServiceImpl.class);

	public CustomDocsListServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
			SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	public C1UXCustDocListResponse getListsApi(SessionContainer sc) throws AtWinXSException {
		C1UXCustDocListResponse response = new C1UXCustDocListResponse();
		List<C1uxCustDocListDetails> returnLists = new ArrayList<>();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();
		C1uxCustDocListDetails detail = null;

		if(!validateCustDocMergeUI(ui)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {

			// get the list
			IManageList listComp = getManageListComponent(asb);
			Collection<ListVO> retrieveLists = listComp.retrieveLists(asb.getSiteID(), asb.getBuID(), asb.getProfileID(), asb.getLoginID(), "LIST_ID", ui.getSelectedListIDs());

			// load retrieveLists into returnLists
			if ((retrieveLists != null) && (!retrieveLists.isEmpty())) {
				for(ListVO list : retrieveLists)
				{
					if(Util.isBlankOrNull(list.getCrmRecordID())) {
						detail = makeListDetailObjectFromVO(list, ui, asb);
						returnLists.add(detail);
					}
				}
			}
			response.setListOfLists(returnLists);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error(e.toString());
			response.setMessage(getTranslation(asb, TranslationTextConstants.TRANS_NM_UNKNOWN_ERROR, SFTranslationTextConstants.TRANS_NM_UNKNOWN_ERROR_DEFAULT));
		}

		return response;
	}

	@Override
	public C1UXSaveListResponse saveNewList(SessionContainer sc) throws AtWinXSException {

		C1UXSaveListResponse response = new C1UXSaveListResponse();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

		if(!validateCustDocMergeUI(ui)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {
			ManageListsSession mlistsSession = loadListSession(sc);
			// Throw 422 if they are trying to do this with session missing info
			if(mlistsSession == null) {
				makeListInfoMissingError(asb);
			}
			else {
				ManageListsResponseBean manageLists = (ManageListsResponseBean) mlistsSession
						.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT);
				DistributionListDetails distributionListDetails = (DistributionListDetails) mlistsSession
						.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT);

				// Throw 422 if they are trying to do this with session missing info
				if (null == manageLists || null == distributionListDetails) {
					makeListInfoMissingError(asb);
				}

				UploadListResponseBean newList = addList(distributionListDetails, asb);

				response.setListID(Util.safeStringToInt(newList.getListID()));
				response.setListRecords(Util.safeStringToInt(newList.getRecordCount()));//CAP-48898
				response.setListName(newList.getListName());
				response.setSuccess(true);
				removeListSession(sc);
			}
		} catch (AtWinXSMsgException me) {
			logger.error(me.getMessage(), me);
			response.setMessage(me.getMessage());
		} catch (AtWinXSException e) {
			logger.error(e.getMessage(), e);
			response.setMessage(getTranslation(asb, SFTranslationTextConstants.TRANS_NM_LIST_MISSING_ERROR, SFTranslationTextConstants.TRANS_NM_LIST_MISSING_ERROR_DEFAULT));
		} catch(Exception ue) { // catch anything unhandled and clean it up
			logger.error(ue.getMessage(), ue);
			response.setMessage(getTranslation(asb, SFTranslationTextConstants.TRANS_NM_LIST_SAVE_ERROR, SFTranslationTextConstants.TRANS_NM_LIST_SAVE_ERROR_DEFAULT));
		}

		return response;
	}

	protected UploadListResponseBean addList(DistributionListDetails distributionListDetails, AppSessionBean appSessionBean) throws AtWinXSException {
		if (distributionListDetails.getIsExcel()) {
			moveFileIfNotConvertingExcel(distributionListDetails, appSessionBean);
		} else {
			// CAP-50156 - must match C1UX BE handling
			distributionListDetails.setModifiedFileName(Util.replace(distributionListDetails.getSourceFileName(), " ", "%20"));
		}
		UploadListRequestBean anULReqBean = instantiateULReqBean(distributionListDetails, appSessionBean);
		UploadListAssembler anULAssembler = getUploadListAssembler(appSessionBean);
		return anULAssembler.performAddList(anULReqBean, ManageListsConstants.UPLOAD_LIST_CONFIRMATION_EVENT);
	}

	protected void removeListSession(SessionContainer sc) throws AtWinXSException {
		SessionHandler.deleteSession(sc.getApplicationSession().getAppSessionBean().getSessionID(), AtWinXSConstant.LISTS_SERVICE_ID);
	}

	protected void makeListInfoMissingError(AppSessionBean asb) throws AtWinXSException {
		Message msg = new Message();
		msg.setErrGeneralMsg(getTranslation(asb, SFTranslationTextConstants.TRANS_NM_LIST_MISSING_ERROR, SFTranslationTextConstants.TRANS_NM_LIST_MISSING_ERROR_DEFAULT));
		throw new AtWinXSMsgException(msg, this.getClass().getName());
	}
}
