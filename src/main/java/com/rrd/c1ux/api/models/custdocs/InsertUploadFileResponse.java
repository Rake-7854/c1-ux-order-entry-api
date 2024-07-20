/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Created By			JIRA #			Description
 *	--------	-----------			----------		-----------------------------------------------------------
 *	01/24/2024	R Ruth				CAP-44862		Initial Version
 */

package com.rrd.c1ux.api.models.custdocs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="InsertUploadFileResponse", description = "Response Class for InsertUploadFile API", type = "object")
public class InsertUploadFileResponse  extends BaseResponse implements StreamingResponseBody {

	private File exportedFile = null;
	private boolean deleteWhenDown = false;
	private String failureMessage = AtWinXSConstant.EMPTY_STRING;
	private static final Logger logger = LoggerFactory.getLogger(InsertUploadFileResponse.class);
	private FileInputStream fileToStream = null;

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		if (exportedFile != null) {
		logger.debug("About to write file to stream");
		try {
				byte[] bytes = new byte[AtWinXSConstant.TWO_KB];
				int bytesRead;

				// Loop through our file and stream to the user here
				while ((bytesRead = fileToStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, bytesRead);
				}

				outputStream.flush();
			} catch (Exception e) {
				logger.error("Failed to stream back file in CDOE when requested", e);
				setSuccess(false);
				setMessage(failureMessage);
				outputStream.write(failureMessage.getBytes());
				outputStream.flush();
			} finally {
				try {
				    if (fileToStream != null) {
				    	fileToStream.close();
				    }
					if(deleteWhenDown) {
						Files.deleteIfExists(Paths.get(exportedFile.getPath()));
					}
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				this.exportedFile = null;
			}
		} else {
			outputStream.write(("<HTML>"+failureMessage+"</HTML>").getBytes());
			outputStream.flush();
		}
	}

	public void setDeleteWhenDown(boolean deleteWhenDown) {
		this.deleteWhenDown = deleteWhenDown;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public void setExportedFile(File exportedFile) {
		this.exportedFile = exportedFile;
	}

	public boolean attemptToOpenStream() {
		try {
			fileToStream = new FileInputStream(this.exportedFile);
			return true;
		} catch (IOException e) {
			logger.error("Failed to stream back file in CDOE when requested", e);
			setSuccess(false);
			setMessage(failureMessage);
		}
		return false;
	}
}
