/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/17/23				A Boomker				CAP-42295					Initial Version
 *	07/25/23				A Boomker				CAP-42223					Initialize UI added
 *	09/14/23				A Boomker				CAP-43843					Add access to translation methods
 *	09/18/23				A Boomker				CAP-42298					Add cancel methods direct access
 *  10/17/23				AKJ Omisol				CAP-43024					Added getImprintHistory method
 *  12/04/23				A Boomker				CAP-45654					Added visibility to method to set dirty flag
 *	02/13/24				A Boomker				CAP-46309					Added changes for file upload
 *	03/12/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 *  04/08/24				A Boomker				CAP-48500					Fix for re-initializing back to cust docs
 *  07/01/24				A Boomker				CAP-46488					Added initializeFromKitComponent()
 *	07/09/24				A Boomker				CAP-46538		Refactored some handling to base for imprint history
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.Locale;
import java.util.Map;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorService;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;


public interface CustomDocsService extends CustomDocsBaseService {
	public static final String UI_INIT_PARAM_ITEM_NR = "itemNumber";
	public static final String UI_INIT_PARAM_VENDOR_ITEM_NR = "vendorItemNumber";
	public static final String UI_INIT_PARAM_CATALOG_LINE_NR = "catalogLnNbr";

	// CAP-46309 - expand static references
	public static final String UPLOAD_FILE_FORMATS_ALL_IMAGES = "A"; // CP translation key is "cdSupportedFileTypesImagesEps"
	public static final String UPLOAD_FILE_FORMATS_IMAGES_STANDARD_ONLY = "S"; // CP translation key is "cdSupportedFileTypesImages"
	public static final String UPLOAD_FILE_FORMATS_IMAGES_EPS_ONLY = "E"; // CP translation key is "cdSupportedFileTypesEpsOnly"
	public static final String UPLOAD_FILE_FORMATS_INSERTS = "I"; // CP translation key is "cdSupportedFileTypesInsert"
	public static final String UPLOAD_FILE_FORMATS_FILE_UPLOAD = "F"; // CP translation key is "cdSupportedFileTypesFileUpload"
	public static final String UPLOAD_FILE_FORMATS_NEW_REQUEST = "R"; // CP translation key is "cdSupportedFileTypesNewRequest"
	public static final String UPLOAD_FILE_FORMATS_NEW_REQUEST_ZIP = "Z"; // CP translation key is "cdSupportedFileTypesNewReqZip"
	public static final String UPLOAD_FILE_FORMATS_HOSTED_RESOURCE = "H"; // CP translation key is "TRANS_NM_HR_FILE_TYPES"



	public C1UXCustDocBaseResponse initializeUIOnly(SessionContainer sc, Map<String, String> uiRequest) throws AtWinXSException;

	public C1UXCustDocPageBean getCurrentPageUI(SessionContainer sc) throws AtWinXSException;

	public C1UXCustDocBaseResponse performPageSubmitAction(SessionContainer sc, Map<String, String> uiRequest) throws AtWinXSException;

	public C1UXCustDocBaseResponse addToCart(SessionContainer sc) throws AtWinXSException;

	public String getTranslation(AppSessionBean appSessionBean, String translationName, String defaultValue,
			Map<String, Object> replaceMap);
	public String getTranslation(Locale locale, CustomizationToken token, String translationName, String defaultValue,
			Map<String, Object> replaceMap);
	public String getTranslation(Locale locale, CustomizationToken token, String translationName, String defaultValue);

	public String getTranslation(Locale locale, CustomizationToken token, String translationName);
	// CAP-42298
	public C1UXCustDocBaseResponse cancelAction(SessionContainer sc, boolean basicCancel) throws AtWinXSException;

	public void setDirtyFlag(Map<String, String> uiRequest);
	public boolean valueIsTrue(String value);

	// CAP-46490 - need exposure for extensions
	public String getOeJavascriptServerAccessPath();
	public void setItemLocator(ManageItemsInterfaceLocatorService locator);

	public void setOeJavascriptServerAccessPath(String path);

	// CAP-46488 - new initialize for kit template components
	 public C1UXCustDocBaseResponse initializeFromKitComponent(SessionContainer sc, int index) throws AtWinXSException;

}
