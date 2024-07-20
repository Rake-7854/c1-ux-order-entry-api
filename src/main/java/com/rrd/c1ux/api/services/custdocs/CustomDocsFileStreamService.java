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
 * 	01/17/24	A Boomker			CAP-44835		Added handling for uploading variable files
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 */
package com.rrd.c1ux.api.services.custdocs;

import org.springframework.http.MediaType;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadVariableFileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUploadedFileDetailsResponse;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileApiRequest;
import com.rrd.c1ux.api.models.custdocs.InsertUploadFileResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface CustomDocsFileStreamService extends CustomDocsBaseService {
	public C1UXCustDocUploadedFileDetailsResponse getUploadedFileDetails(SessionContainer sc, C1UXCustDocUploadedFileDetailsRequest request);

	public MediaType getMediaType(String fileName);
	public String getDisposition(String fileName);

	public InsertUploadFileResponse prepareDownload(InsertUploadFileApiRequest req, AppSessionBean asb) throws AtWinXSException;
	public String getFailureDownloadMessage(AppSessionBean asb);

	public C1UXCustDocUploadVariableFileResponse uploadVariableFile(SessionContainer sc, C1UXCustDocUploadVariableFileRequest request)
			 throws AccessForbiddenException;
}
