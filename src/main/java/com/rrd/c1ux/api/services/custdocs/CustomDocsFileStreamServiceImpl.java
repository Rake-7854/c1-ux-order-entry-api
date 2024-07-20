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
 * 	01/09/24	A Boomker			CAP-44839		Updates after CP side implemented c1ux code
 * 	01/17/24	A Boomker			CAP-44835		Added handling for uploading variable files
 *	02/02/24	A Boomker			CAP-46337		Added handling for file upload
 *	02/14/24	A Boomker			CAP-46309		Moved some things to new base class
 *	02/19/24	A Boomker			CAP-44837		Changes to list options to indicate uploads
 *	02/27/24	A Boomker			CAP-47446		Uploaded list options should default to selected
 *	02/29/24	R Ruth				CAP-46510		Need to throw new 422 error because file could not be found.
 *	03/06/24	A Boomker			CAP-46508		Support insertion group uploads
 *	03/08/24	A Boomker			CAP-47760		Fix error message to remove stack trace info
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 */
package com.rrd.c1ux.api.services.custdocs;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.exceptions.BadRequestException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXUIListOption;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileApiRequest;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileResponse;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.Variable;
import com.rrd.custompoint.orderentry.customdocs.Variable.PageflexType;
import com.rrd.custompoint.orderentry.customdocs.ui.download.DownloadFile;
import com.rrd.custompoint.orderentry.customdocs.ui.download.DownloadFileC1uxBean;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileC1uxBean;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileImage;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileInsert;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileUpload;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICustomDocsCopyFiles;
import com.wallace.atwinxs.orderentry.customdocs.compserv.util.CSCopyFilesResponse;
import com.wallace.atwinxs.orderentry.customdocs.locator.CustomDocsAdminLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class CustomDocsFileStreamServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsFileStreamService {
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsFileStreamServiceImpl.class);
	public static final String UPLOAD_FAILED_DEFAULT = "Upload failed.";

	public CustomDocsFileStreamServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
	        SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	public C1UXCustDocUploadedFileDetailsResponse getUploadedFileDetails(SessionContainer sc, C1UXCustDocUploadedFileDetailsRequest request) {
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocUploadedFileDetailsResponse sfBean = new C1UXCustDocUploadedFileDetailsResponse();

			CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
			UserInterface ui = item.getUserInterface();
			String varName = request.getHdnVar();
			String varValue = request.getHdnVarValue();
			AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
			boolean invalidVarOrValue = (Util.isBlankOrNull(varName) || (Util.isBlankOrNull(varValue)));
			Variable cdVar = (invalidVarOrValue) ? null : ui.getAllVariables().find(varName);
			if (invalidVarOrValue || (cdVar == null))
			{
				setGenericHeaderError(sfBean, asb); // we don't want to tell people so they can try this over and over
			}
			else {
				getDownloadInfoFromCustomPoint(varValue, sfBean, cdVar, asb);
			}
			return sfBean;
	}

	protected DownloadFile getDownloadFileObject(AppSessionBean appSessionBean) {
		return this.objectMapFactoryService.getEntityObjectMap().getEntity(DownloadFile.class, appSessionBean.getCustomToken());
	}

	protected DownloadFileC1uxBean callCustomPointDownloadInfo(DownloadFile downloadFile, AppSessionBean appSessionBean, Variable cdVar, String varValue) throws AtWinXSException {
		return downloadFile.downloadFileC1UX(appSessionBean, cdVar, varValue);
	}

	protected void getDownloadInfoFromCustomPoint(String varValue, C1UXCustDocUploadedFileDetailsResponse sfBean, Variable cdVar, AppSessionBean appSessionBean){
		boolean uploadValue = isVarValueUploadOrSearch(varValue);
		boolean success = false;

		if (validRequestForVar(cdVar, uploadValue)) {
			try {
			DownloadFile downloadFile = getDownloadFileObject(appSessionBean);
			com.rrd.custompoint.orderentry.customdocs.ui.download.DownloadFileC1uxBean info = callCustomPointDownloadInfo(downloadFile, appSessionBean, cdVar, varValue);
			if (info != null)
			{
				sfBean.setDownloadInfo(info);
				sfBean.setCanDownloadImmediately(downloadFile.isReadyToViewFile()); // this should have been set to downloadFile.isReadyToViewFile
				sfBean.setSuccess(true);
				success = true;
			}
			} catch(AtWinXSException e) {
				//logger.error(e.toString()); // if this fails, we just need to log it. Not much we can do.

				// CAP-46510 -  message already on the AtWinXSException, so take that message and put it in the sfBean message field
				sfBean.setMessage(e.getMessage()); // CAP-47760
			}
		}

		if ((!success) && (Util.isBlankOrNull(sfBean.getMessage())))
		{
			sfBean.setMessage(getFailureDownloadMessage(appSessionBean)); // we don't want to tell people so they can try this over and over
		}
	}

	protected boolean validRequestForVar(Variable variable, boolean uploadValue) {
		// only uploads can be downloaded for both image and file upload types
		// anything can be viewed for insertion groups
		return ((variable.getVariablePageflexType() == Variable.PageflexType.INSERTION_GROUP) ||
				((((variable.getVariablePageflexType() == Variable.PageflexType.IMAGE) && (variable.isUploadAllowedInd() || variable.isSearchImages()))
						|| (variable.getVariablePageflexType() == Variable.PageflexType.FILE_UPLOAD)) && uploadValue));
		// no other pageflex type can do a download, including hosted resource who do uploads but they go to the cloud not the SAN
	}

	protected void validateFilePassed(C1UXCustDocUploadVariableFileRequest request) {
		if ((request == null) || (request.getFile() == null)) {
			throw new BadRequestException("File not passed on upload.");
		}
	}

	@Override
	public C1UXCustDocUploadVariableFileResponse uploadVariableFile(SessionContainer sc,
			C1UXCustDocUploadVariableFileRequest request) throws AccessForbiddenException {
    	validateFilePassed(request);

	    OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocUploadVariableFileResponse sfBean = new C1UXCustDocUploadVariableFileResponse();
		// want to wrap this whole thing in a try/catch
			CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
			UserInterface ui = item.getUserInterface();
			String varName = request.getHdnVar();
			AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
			boolean invalidVar = Util.isBlankOrNull(varName);
			Variable cdVar = (invalidVar) ? null : ui.getAllVariables().find(varName.trim());
			if (invalidVar || (cdVar == null) || (!cdVar.isUploadAllowedInd()))
			{
				throw new AccessForbiddenException(this.getClass().getName());
			}
			else {
				uploadFileThroughCustomPoint(request, sfBean, cdVar, asb, ui, oeSession);
			}
			return sfBean;
	}

	protected void uploadFileThroughCustomPoint(C1UXCustDocUploadVariableFileRequest request,
			C1UXCustDocUploadVariableFileResponse sfBean, Variable cdVar, AppSessionBean asb, UserInterface ui, OrderEntrySession oeSession) {
		PageflexType varType = cdVar.getVariablePageflexType();
		UploadFileC1uxBean cpBean = convertVariableRequestForCP(request, cdVar, ui);
		try {
			switch(varType) {
				case IMAGE :
					logger.info("uploading an IMAGE variable file");
					cpBean.setUfType("IMAGE");

					UploadFileImage uploadFile = (UploadFileImage) getUploadFileObject(varType, cdVar, asb);
					sfBean.setSuccess(uploadFile.uploadFileC1UX(cpBean, asb, ui, oeSession));
					break;
				case INSERTION_GROUP : // CAP-46508
					logger.info("Uploading an INSERTION GROUP variable file.");
					cpBean.setUfType("INSERT");

					UploadFileInsert uploadInsertFile = (UploadFileInsert) getUploadFileObject(varType, cdVar, asb);
					sfBean.setSuccess(uploadInsertFile.uploadFileC1UX(cpBean, asb, ui, oeSession));
					break;
				case FILE_UPLOAD :
					logger.info("Uploading a FILE UPLOAD variable file.");
					cpBean.setUfType("FILE");

					UploadFileUpload uploadNonPFFile = (UploadFileUpload) getUploadFileObject(varType, cdVar, asb);
					sfBean.setSuccess(uploadNonPFFile.uploadFileC1UX(cpBean, asb, ui, oeSession));
					break;
				case HOSTED_RESOURCE :
					sfBean.setMessage("Uploading a HOSTED RESOURCE variable file. This is NOT supported yet!");
					cpBean.setUfType("HOSTED");
					/* this next part is not yet supported but this is what will have to happen
					UploadFileHostedResource uploadFile
					 (UploadFileHostedResource) getUploadFileObject(varType, cdVar, asb)
					sfBean setSuccess(uploadFile.uploadFileC1UX(cpBean, asb, ui))
					*/

					break;
				default :
					sfBean.setMessage("Upload is not allowed on this type of variable. This will NOT be supported!");
			}
		} catch(Exception e) {
			logger.error(e.getMessage());
			if (cpBean.getErrors().isEmpty()) {
				sfBean.setMessage(getTranslation(asb, "uploadFailedMsg", UPLOAD_FAILED_DEFAULT));
			}
		}
		persistReuse(ui, oeSession, asb);
		populateUploadResponseFromCP(sfBean, cpBean);
	}

	protected void persistReuse(UserInterface ui, OrderEntrySession oeSession, AppSessionBean asb) {
		// CAP-46337
		if (ui.isReuseUploadFiles()) { // upload could have modified session lists, so need to save
			try {
				saveFullOESessionInfo(oeSession, asb.getSessionID());
			} catch (AtWinXSException e) {
				logger.error("Failed to save order entry session after an upload when files are reused", e.getMessage());
			}
		}
	}

	protected void populateUploadResponseFromCP(C1UXCustDocUploadVariableFileResponse sfBean, UploadFileC1uxBean cpBean) {
		sfBean.setUploadId(cpBean.getFileID());
		sfBean.setUploadFileName(cpBean.getTxtUploadFileName());
		sfBean.setDownloadLinkURL(cpBean.getDownloadLinkURL());
		buildListOption(sfBean);
		if ((!sfBean.isSuccess()) && (!cpBean.getErrors().isEmpty()) && (Util.isBlankOrNull(sfBean.getMessage()))) {
			String[] errorList = new String[cpBean.getErrors().size()];
			errorList = cpBean.getErrors().toArray(errorList);
			sfBean.setMessage(errorList[0]);
		}
	}

	private void buildListOption(C1UXCustDocUploadVariableFileResponse sfBean) {
		if (sfBean.isSuccess()) {
			C1UXUIListOption option = new C1UXUIListOption();
			String label = OrderEntryConstants.IDENTIFIER_UPLOAD + sfBean.getUploadFileName();
			String listID = OrderEntryConstants.IDENTIFIER_UPLOAD_SHORT + sfBean.getUploadId();
			String value = listID + "^" + label;
			option.setProofValue(value);
			option.setPlantValue(value);
			option.setListId(listID);
			option.setHtmlValue(value);
			option.setHtmlLabel(label);
			option.setUpload(true);
			option.setSelected(true);
			option.setTextValue(value);
			sfBean.setListOption(option);
		}
	}
	protected UploadFileC1uxBean convertVariableRequestForCP(C1UXCustDocUploadVariableFileRequest request, Variable cdVar, UserInterface ui) {
		UploadFileC1uxBean cpBean = new UploadFileC1uxBean();
		cpBean.setFile(request.getFile());
		cpBean.setVar(cdVar);
		cpBean.setHdnVar(request.getHdnVar());
		cpBean.setUfIsReuseUploadFiles(ui.isReuseUploadFiles());
		return cpBean;
	}

	protected String cleanFileName(String name) {
		String fileName = Util.nullToEmpty(name);
		if (fileName.startsWith("D_")){
			fileName = Util.tail(fileName,"D_");
		}
		return fileName;
	}

	public InsertUploadFileResponse prepareDownload(InsertUploadFileApiRequest req, AppSessionBean appSessionBean)  {
		// setup response
		InsertUploadFileResponse response = new InsertUploadFileResponse();
		try {
			// Request will need file ID and file name - the rest we can pull from appSessionBean.  We'll need to do this type of logic.
			String fileName = cleanFileName(req.getFileName());
			int fileId = Util.safeStringToDefaultInt(req.getFileId(), -1);
			CSCopyFilesResponse copyFilesResponse = copyFileToTempLocation(appSessionBean, fileName, fileId);

			if (copyFilesResponse!=null && copyFilesResponse.getStatus().equalsIgnoreCase("Success")) {
				File file = new File(getTempFileDirectory(appSessionBean) + fileName);
				response.setDeleteWhenDown(req.isDeleteWhenDown());
				response.setExportedFile(file);
				response.setFailureMessage(getFailureDownloadMessage(appSessionBean));
				response.setSuccess(response.attemptToOpenStream());
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}

		if (!response.isSuccess()) {
			response.setMessage(getFailureDownloadMessage(appSessionBean));
		}
		return response;
	}

	protected ICustomDocsCopyFiles getCopyFilesComponent(CustomizationToken customToken) throws AtWinXSException {
		return CustomDocsAdminLocator.locateCustomDocumentsCopyFilesComponent(customToken);
	}

	protected CSCopyFilesResponse copyFileToTempLocation(AppSessionBean appSessionBean, String fileName, int fileId) throws AtWinXSException {
		ICustomDocsCopyFiles copyFilesComponent  = getCopyFilesComponent(appSessionBean.getCustomToken());
		return copyFilesComponent.copyDSAFile(appSessionBean.getCorporateNumber(), fileName,
				appSessionBean.getCurrentEnvCd(), fileId);
	}

	protected String getTempFileDirectory(AppSessionBean asb) {
		return getWebserviceProperty(asb, "convertExcelTempDir");
	}

	public String getFailureDownloadMessage(AppSessionBean asb) {
		return getTranslation(asb, "reportExportErrMsg", "Download Failed.");
	}

	public String getDisposition(String fileName) {
		return "attachment; filename=\"" + fileName + "\"";
	}
}
