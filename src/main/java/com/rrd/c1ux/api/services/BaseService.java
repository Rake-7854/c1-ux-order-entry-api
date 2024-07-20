/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions:
 *  Date        Modified By     Jira                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  02/27/2023  C Porter        CAP-38708                   Refactor Translation service into base service object
 *  04/24/23	A Boomker		CAP-40002					Moved errorPrefix string generator to here for reuse
 *  04/26/23	C Codina		CAP-39333					API Change - PNA labels in multiple APIs to make/use translation text values
 *  05/15/23	L De Leon		CAP-40324					Added an overloaded method getTranslation() method
 *	05/16/23	A Boomker		CAP-40687					Added getCombinedMessage()
 *	05/25/23	C Codina		CAP-39338					Modified getTranslation for it to be tested in JUnit
 *	06/07/23	A Boomker		CAP-38154					Moving some validation methods here from CP to use correct translation
 *	06/08/23	A Boomker		CAP-41266					Made getTranslation public
 *	08/30/23	A Boomker		CAP-43405					Fixing item in cart flags for customizable items
 *	09/14/23	A Boomker		CAP-43843					Add access to translation methods
 *	01/25/23	R Ruth			CAP-44862					Creating new API to stream back insert or upload file like CP servlet
 *	02/12/24	Krishna Natarajan	CAP-47109 				Added another constructor to accommodate changes for tests
 *	07/09/24	A Boomker		CAP-46538					Added generic search error for unhandled exceptions during search NOT for no results
 */
package com.rrd.c1ux.api.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryServiceImpl;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryServiceImpl;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.ui.upload.UploadFile;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.framework.util.MaskFormatter;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

public abstract class BaseService {

  // CAP-44862
  private static final Logger logger = LoggerFactory.getLogger(BaseService.class);

  protected final TranslationService translationService;
  protected final ObjectMapFactoryService objectMapFactoryService;
  protected final OEAssemblerFactoryService oeAssemblerFactoryService;

  protected BaseService(TranslationService translationService) {
	    this.translationService = translationService;
	   	this.objectMapFactoryService = new ObjectMapFactoryServiceImpl();
	   	this.oeAssemblerFactoryService = new OEAssemblerFactoryServiceImpl();
	  }

  protected BaseService(TranslationService translationService, ObjectMapFactoryService objectMapFacService) {
    this.translationService = translationService;
   	this.objectMapFactoryService = objectMapFacService;
   	this.oeAssemblerFactoryService = new OEAssemblerFactoryServiceImpl();
  }

  //CAP-47109
	protected BaseService(TranslationService translationService, ObjectMapFactoryService objectMapFacService,
			OEAssemblerFactoryService oeAssemblerFactoryService) {
		this.translationService = translationService;
		this.objectMapFactoryService = objectMapFacService;
		this.oeAssemblerFactoryService = oeAssemblerFactoryService;
	}

	// Sonarqube keeps complaining about this prefix we're adding on each error being logged - moved with CAP-40002 to this base class
  protected String getErrorPrefix(AppSessionBean asb)
  {
	  return "User " + asb.getLoginID() + " on site " + asb.getSiteID() + AtWinXSConstant.BLANK_SPACE;
  }
  //CAP-39333: C1UX BE - API Change - PNA labels in multiple APIs to make/use translation text values
  public String getTranslation(AppSessionBean appSessionBean, String translationName, String defaultValue) {
	  return getTranslation(appSessionBean, translationName, defaultValue, null);
  }

  public String getCombinedMessage(Collection<String> collection)
  {
	  if (collection != null)
	  {
		  if (collection.isEmpty())
		  {
			  return AtWinXSConstant.EMPTY_STRING;
		  }
		  else
		  {
			  StringBuilder msg = new StringBuilder();
			  for (String part : collection)
			  {
				  if (msg.length() > 0)
				  {
					  msg.append(AtWinXSConstant.BLANK_SPACE);
				  }
				  msg.append(part);
			  }
			  return msg.toString();
		  }
	  }
	  else
		  return null;
  }

	// CAP-40324 Overloaded getTranslation() method
	public String getTranslation(AppSessionBean appSessionBean, String translationName, String defaultValue,
			Map<String, Object> replaceMap) {
		String translationValue = AtWinXSConstant.EMPTY_STRING;
		if ((!Util.isBlankOrNull(defaultValue)) && (replaceMap != null)) {
			defaultValue = Util.replace(defaultValue, replaceMap);
		}
		try {
			translationValue = translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), translationName, replaceMap);
			if (Util.isBlankOrNull(translationValue)) {
				translationValue = defaultValue;
			}
		} catch (AtWinXSException e) {
			translationValue = defaultValue;
		}
		return Util.nullToEmpty(translationValue);
	}

	// CAP-38154 - make versions without appSession directly
	public String getTranslation(Locale locale, CustomizationToken token, String translationName, String defaultValue,
			Map<String, Object> replaceMap) {
		String translationValue = AtWinXSConstant.EMPTY_STRING;
		if ((!Util.isBlankOrNull(defaultValue)) && (replaceMap != null)) {
			defaultValue = Util.replace(defaultValue, replaceMap);
		}
		try {
			translationValue = translationService.processMessage(locale, token, translationName, replaceMap);
			if (Util.isBlankOrNull(translationValue)) {
				translationValue = defaultValue;
			}
		} catch (AtWinXSException e) {
			translationValue = defaultValue;
		}
		return Util.nullToEmpty(translationValue);
	}

	// CAP-38154- make versions without appsession directly
	  public String getTranslation(Locale locale, CustomizationToken token, String translationName, String defaultValue) {
		  return getTranslation(locale, token, translationName, defaultValue, null);
	  }

	  // not everything has a default - needed for cust docs
	  public String getTranslation(Locale locale, CustomizationToken token, String translationName) {
		  return getTranslation(locale, token, translationName, null, null);
	  }

	// CAP-38154 - moved here for field level validation messaging
	/**
	 * Validate field masks based on the defined pattern in Admin.
	 * copied from OEManageOrdersComponent and modified to take locale, token for translation
	 *
	 * @param fieldToValidate
	 * @param maskPattern
	 * @param requireLiteralsOnEntry
	 * @param locale
	 * @param token
	 * @return
	 * @throws ParseException
	 */
	public String validateFieldMask(String fieldToValidate, String maskPattern, boolean requireLiteralsOnEntry,
			Locale locale, CustomizationToken token) throws ParseException
    {
    	String field = "";
    	if (!Util.isBlankOrNull(fieldToValidate) && !Util.isBlankOrNull(maskPattern))
    	{
    		MaskFormatter maskUtil = new MaskFormatter(maskPattern, null, locale, token); // errorLabel is never used
			maskUtil.setRequireLiteralsOnEntry(requireLiteralsOnEntry);
			field = (String) maskUtil.stringToValue(fieldToValidate);
    	}
    	return field;
    }

	/**
	 * Validate field masks based on the defined pattern in Admin.
	 * copied from OEManageOrdersComponent and modified to take locale, token for translation
	 *
	 * @param response
	 * @param labelName
	 * @param value
	 * @param locale
	 * @param token
	 * @return
	 * @throws ParseException
	 */
	public boolean validWCSSCharWithErrorPopulation(BaseResponse response, String fieldKey, String fieldLabel, String value,
			Locale locale, CustomizationToken token)
	{
		if (!Util.isBlankOrNull(value) && !OrderEntryConstants.WCSS_ENCODING_TYPE.canEncode(value))
		{
			Map<String, Object> replaceMap = new HashMap<>();
	        replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME,
	        		(fieldLabel != null) ? fieldLabel : "Value"); // if it wasn't in the translation map, it's going to be English anyway, so say value
	        response.setFieldMessage(fieldKey, getTranslation(locale, token, ErrorCodeConstants.ERR_NAME_INVALID_CHARS, "{fieldName} contains invalid characters.", replaceMap));
			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean isCustomizableItem(String itemClassification, String vendorItemNumber, AppSessionBean asb, PunchoutSessionBean psb) {
		return ItemHelper.isCustomizableItem(itemClassification, vendorItemNumber, asb, psb);
	}

	// CAP-44862
	public MediaType getMediaType(String fileName) {
		String ext=fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();

		if (!Util.isBlankOrNull(ext)){
			ext = Util.replaceCarriageReturns(ext).trim();
		}

		try {
		if (UploadFile.FILE_EXT_PDF.equalsIgnoreCase(ext)) { // adobe pdf
			return MediaType.APPLICATION_PDF;
		}
		else if (ManageListsConstants.EXCEL_EXTENTIONS.contains(ext)){ // microsoft excel
			return MediaType.valueOf("application/vnd.ms-excel");
		}
		else if ( ManageListsConstants.WORD_EXTENSIONS.contains(ext) ) { // microsoft word
			return MediaType.valueOf("application/msword");
		}
		else if (UploadFile.FILE_EXT_GIF.equalsIgnoreCase(ext)) {
			return MediaType.IMAGE_GIF;
		}
		else if ((UploadFile.FILE_EXT_JPG.equalsIgnoreCase(ext)) || (UploadFile.FILE_EXT_JPEG.equalsIgnoreCase(ext))) {
			return MediaType.IMAGE_JPEG;
		}
		else if (UploadFile.FILE_EXT_PNG.equalsIgnoreCase(ext)) {
			return MediaType.IMAGE_PNG;
		}
		return MediaType.APPLICATION_OCTET_STREAM;
		} catch(InvalidMediaTypeException me) {
			logger.debug(me.getMessage());
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	// CAP-44862
	public FileInputStream getFileInputStream(File exportedFile) throws FileNotFoundException {
		return new FileInputStream(exportedFile);
	}

	// CAP-44862
	public void deleteTempExportFile(File exportedFile) throws IOException {
		Files.deleteIfExists(Paths.get(exportedFile.getPath()));
	}

	// CAP-46538 - added generic search error for unhandled exceptions during search NOT for no results
	protected String getUnhandledSearchError(AppSessionBean appSessionBean) {
		return getTranslation(appSessionBean, SFTranslationTextConstants.TRANS_NM_GENERIC_SEARCH_FAILS_ERROR,
				SFTranslationTextConstants.TRANS_NM_GENERIC_SEARCH_FAILS_ERROR_DEFAULT);
	}

}
