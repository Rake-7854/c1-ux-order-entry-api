/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  10/17/23	Satishkumar A	CAP-44664	C1UX BE - Create api to retrieve edoc for Storefront
 */
package com.rrd.c1ux.api.services.edoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.edoc.EdocUrlResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CPUrlBuilder;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OEViewItemImageAssembler;

@Service
public class EdocServiceImpl extends BaseOEService implements EdocService{

	protected EdocServiceImpl(TranslationService translationService ,ObjectMapFactoryService objectMapFactoryService) {
		super(translationService, objectMapFactoryService);
	}

	private static final Logger logger = LoggerFactory.getLogger(EdocServiceImpl.class);
	public static final FileNameMap fileNameMap = URLConnection.getFileNameMap();	

	public static final String EDOC_PARM_URL                 = "URL";
    public static final String EDOC_PARM_ITEM_NUM             = "itemNumber";
    public static final String EDOC_PARM_VENDOR_ITEM_NUM    = "vendorItemNumber";
    public static final String EDOC_PARM_LOG_FLAG            = "logFlag";
    public static final String EDOC_PARM_LOCAL_FILE            = "localFile";
    

	@Override
	public EdocUrlResponse getEdocUrl(AppSessionBean appSessionBean, HttpServletRequest request,  HttpServletResponse response)
			throws AtWinXSException, UnsupportedEncodingException {
		
		EdocUrlResponse edocResponse = new EdocUrlResponse();
		Map<String,String>   params;
		try {
			params = processEncryptedData( request);
		}catch(Exception e) {
			edocResponse.setSuccess(false); 
			edocResponse.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "invalidRequestErrMsg"));
			return edocResponse;
		}
		
		String localFile = params.get(EDOC_PARM_LOCAL_FILE);
		String url = params.get(EDOC_PARM_URL);
		String itemNumber = params.get(EDOC_PARM_ITEM_NUM);
		String vendorItemNumber = params.get(EDOC_PARM_VENDOR_ITEM_NUM);
		String logFlag = params.get(EDOC_PARM_LOG_FLAG);

		if(params.isEmpty() || (Util.isBlankOrNull(itemNumber) && Util.isBlankOrNull(vendorItemNumber)) || (Util.isBlankOrNull(localFile) && Util.isBlankOrNull(url))){
			edocResponse.setSuccess(false); 
			edocResponse.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "invalidRequestErrMsg"));
			return edocResponse;
		}		
		if(Util.isBlankOrNull(logFlag) || Boolean.parseBoolean(logFlag)) {
			itemNumber = Util.nullToEmpty(itemNumber);
			createEdocTransaction(itemNumber, vendorItemNumber, url, appSessionBean.getSiteID(), appSessionBean.getLoginID(), appSessionBean.getDefaultLocale());
		}
		if(Util.isBlankOrNull(localFile)) {
			edocResponse.setEdocUrl(url);
			edocResponse.setSuccess(true);
			return edocResponse;
		} else
		{
			try {
				
				File outputFile =createFile(localFile);
				if(!outputFile.exists()) {
					edocResponse.setSuccess(false); 
					edocResponse.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "fileNotExistErr"));
					return edocResponse;
				}
				streamFileToBrowser(outputFile, response, edocResponse);
				
			} catch (IOException e) {
				edocResponse.setSuccess(false); 
				edocResponse.setMessage(e.getMessage());
		}
		}
		return edocResponse;
	}

	protected File createFile(String localFile) {
		return new File(localFile);
	}
	public EdocUrlResponse streamFileToBrowser(File outputFile, HttpServletResponse response,EdocUrlResponse edocResponse) throws IOException {
		
		
		try(InputStream inputStream = new FileInputStream(outputFile)) {

		response.setHeader("Content-Disposition","attachment; filename=\"" + outputFile.getName() + "\"");
		IOUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		edocResponse.setSuccess(true); 
		edocResponse.setEdocUrl("");

		}catch (Exception e) {
			logger.error("Error processing file " + outputFile.getName() + ".  Unable to generate source for the File.", e);
			edocResponse.setSuccess(false);
			edocResponse.setMessage(e.getMessage());
		}
		return edocResponse;
	}
	
	// CAP-11237 TH - Added method to check to see if we have encrypted data
	protected Map<String,String>  processEncryptedData(HttpServletRequest request)
	{
		return CPUrlBuilder.decryptParameters(request);
		
	}
	//CAP-1702 Start
	public void createEdocTransaction(String itemNumber, String vendorItemNumber,String url, int siteID,String loginID, Locale loc)
	{
		
		OEViewItemImageAssembler oeViewItemImgAssembler = OEAssemblerFactory.getViewItemImageAssembler(null, loc);
		
		try
		{
			oeViewItemImgAssembler.addEDocTransaction(itemNumber, vendorItemNumber, url, 0, loginID, -1, siteID);
		}
		catch (AtWinXSException e)
		{
			logger.error("Could not add Edoc Transaction to XST089 for "+ url + " with the following error:" + e.getMessage(), e);
		}
	}

}
