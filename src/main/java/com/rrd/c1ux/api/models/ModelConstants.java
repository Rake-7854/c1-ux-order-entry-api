/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By			JIRA#						Description
 * 	--------	-----------			-----------		--------------------------------
 *	09/12/22	A Boomker			CAP-35436		Adding service for returning messages for message center
 *   10/10/22	Krishna Natarajan	CAP-36438/36448 Added new fields with the comment
 *   11/03/22	Krishna Natarajan	CAP-36981		Added new constants for CAP-36981
 *   11/23/22	S Ramachandran  	CAP-36557   	Added constant for field emails order files
 *   03/17/23	S Ramachandran		CAP-38720 		added validation constant for Order Detail by Sales ref with Standardization
 *   03/30/23	N Caceres			CAP-39159		Added constants for CP and C1UX (106x106) no image path
 *	 04/13/23	A Boomker			CAP-39904 		Add constants for max size for self-admin fields
 *   04/20/23	S Ramachandran		CAP-39973		Added constants for Company Master Address search and Max address result count
 *   05/11/23   Satishkumar A   	CAP-39247       API Change - Modify Message Center Response API to make/use new translation text values
 *   05/09/23	S Ramachandran		CAP-38156		Added constant for CP No Image No Context for extended item quantity
 *	 05/14/23	L De Leon			CAP-40324		Add constants for save delivery info fields
 *	 05/19/23   S Ramachandran		CAP-39973      	Added constants for Quick Start Guide path property and target
 * 	 05/22/23  	Satishkumar A   	CAP-40617		API Build - Save Order assuming all data already saved
 * 	 06/16/23	S Ramachandran		CAP-41136		Add constant for currency format display in cart total price
 * 	 07/13/23	S Ramachandran		CAP-42258		Removed SF extended profile specific constant and added to SelfAdminService interface
 * 	 07/25/23	Krishna Natarajan	CAP-42241		Update USPS correct state/city/zip code back into request and response objects
 * 	 08/09/23	Krishna Natarajan	CAP-42803		added constant for picking Zip/Postal Code translation
 *   08/11/23	Krishna Natarajan	CAP-42862		Added constant to handle the Address Not Found - USPS error
 * 	 08/15/23	C Codina			CAP-41550		Added field for PAB ID
 *	 09/22/23	L De Leon			CAP-44032		Add constants for payment information
 *	 10/04/23	L De Leon			CAP-44312		Add constants for show third party account number options
 *	 11/07/23	A Boomker			CAP-44427		Added constant for proof image not found
 * 	11/10/23	A Boomker			CAP-44487		Added load user profile constants
 * 	11/14/23	N Caceres			CAP-45055		Added cust refs search criteria constants
 * 	11/20/23	Satishkumar A		CAP-38135		C1UX BE - Modification of Manual Enter Address to use new USPS validation
 * 	11/27/23	M Sakthi			CAP-45057		C1UX BE - Add Ship To Name and Ship To Attention as search criteria for Order Search
 *	11/27/23	L De Leon			CAP-44467		Added constants for Order On Behalf search criteria
 *	12/22/23 	Satishkumar A		CAP-45709		C1UX BE - Set OOB Mode for CustomPoint session
 *	12/28/23	C Codina			CAP-45677		Added constant for Invoice number valid length
 *	01/26/24	L De Leon			CAP-46322		Added constants for attribute filters
 *	02/01/24	Sakthi M			CAP-46634		C1UX BE - Modify /api/users/get-fullprofileoforiginator method to return Attributes for Profile
 *	02/08/24	M Sakthi			CAP-46964		C1UX BE - Code Fix - OOB - during checkout - PAB to bring requestor PAB address
 *  02/17/24	Krishna Natarajan 	CAP-47085 		Added a new constant for period
 *	02/14/24	S Ramachandran		CAP-47145		Added contant for customer and vendor item validation
 *	03/18/24	S Ramachandran		CAP-48002		Added manageLists, distributionListDetails session object constant
 *	03/22/24	L De Leon			CAP-47969		Added constants for listing error messages
 *	04/09/24	A Boomker			CAP-48503		moved 403 message here
 *	04/16/24	L De Leon			CAP-48457		Added constants for DTD status codes
 *  04/18/24	Krishna Natarajan	CAP-48777       Added a new constant for validateListNameCharacters method to validate no special characters on List Name
 *	04/22/24	A Boomker			CAP-46498		Added NUM_MAPPED_DATA_RECORDS_PER_PAGE_DISPLAYED
 *	05/01/24	C Codina			CAP-48890		Added constants for template ordering
 *	04/25/24	S Ramachandran		CAP-48889		Added constant for delete order template
 *	05/13/24	S Ramachandran		CAP-49326		Added constant for Style ID
 *	05/17/24	L De Leon			CAP-49280		Added constants for order files EFD contents
 */
package com.rrd.c1ux.api.models;

public class ModelConstants {

	public final static String[][] MESSAGE_CATEGORY_IMG_MAP_CP_TO_C1UX = {
			{ "/images/icons/png/System_Alert.png", "assets/images/Message-system-24px.svg" },
			{ "/images/icons/png/Order_Reminders.png", "assets/images/Order-reminders-24px.svg" },
			{ "/images/icons/png/Order_Alerts.png", "assets/images/Order-alert-24px.svg" },
			{ "/images/icons/png/Message_Video.png", "assets/images/Message-video-24px.svg" },
			{ "/images/icons/png/Message_Timely.png", "assets/images/Message-timely-24px.svg" },
			{ "/images/icons/png/Message_Note.png", "assets/images/Message-note-24px.svg" },
			{ "/images/icons/png/Message_News.png", "assets/images/Message-news-24px.svg" },
			{ "/images/icons/png/Message_Hot.png", "assets/images/Message-hot-24px.svg" },
			{ "/images/icons/png/Message_Chatty.png", "assets/images/Message-chatty-24px.svg" },
			{ "/images/icons/png/Inventory_Alerts.png", "assets/images/Inventory-alert.svg" },
			{ "/images/icons/png/Icon_Dollar.png", "assets/images/Dollar-24px.svg" },
			{ "", "assets/images/FAQ-24px.svg" } };

	public final static String HIGH_VALUE_MESSAGE_CATEGORY_IMAGE_PATH = "assets/images/Important-24px.svg";

	// CAP-36438/36448 Change the return type to Object Modify the API to get the
	// required JSON response for FE
	public static final String LABEL = "label";
	public static final String VALUE = "value";
	public static final String VENDOR_ITEM_NUMBER = "Vendor Item number";
	public static final String VENDOR_ITEM_NUMBER_NOSPACE = "vendorItemNumber";
	public static final String ITEM_NUMBER = "Item Number";
	public static final String ITEM_NUMBER_NOSPACE = "itemNumber";
	public static final String ITEM_DESCRIPTION = "Item Description";
	public static final String ITEM_DESCRIPTION_NOSPACE = "itemDescription";
	public static final String EXPRESS_SHOPPING = "ExpressShopping";
	public static final String PROMPT_FOR_SAVING_UI_PAGES = "PromptForSavingUIPages";
	public static final String DEFAULT_DISPLAY_AS = "DefaultDisplayAs";
	public static final String DEFAULT_SORTBY = "DefaultSortBy";
	public static final String DEFAULT_NO_OF_ITEMS = "DefaultNoOfItems";

	//CAP-36981
	public static final String PRICE_NOT_FOUND       		= "Price Not Found";
	public static final String AVAILABILITY_IN_STOCK 		= "In stock";
	public static final String AVAILABILITY_BACK_ORDER 		= "Backorder";
	public static final String AVAILABILITY_ON_DEMAND		= "On Demand";
	public static final String AVAILABILITY_UNABLE_TO_DET	= "Unable to determine availability";

	//CAP-37102
	public static final String DEFAULT_USER="Questions Regarding Storefront";

	//CAP-36560
	public static final String ITEM_NO = "Item No.";
	public static final String ITEM_NO_NOSPACE = "itemNo";
	public static final String ITEM_NAME = "Item Name";
	public static final String ITEM_NAME_NOSPACE = "itemName";

	//CAP-39070
	public static final String SALES_REF_NO_NOSPACE = "salesRefNumber";
	public static final String PO_NO_NOSPACE = "purchaseOrderNumber";
	public static final String ORDER_DATE_NOSPACE = "orderTime";
	public static final String ORDER_STATUS_NOSPACE = "orderStatusCode";

	//CAP-36557
	public static final String ORDER_COOS_EMAILSORDER_FILE_IMG = "/images/icons/png/Icon_Files.png";
	public static final String UOM_FACTOR_FORMAT_WITHSLASH = "/";
	public static final String UOM_FACTOR_FORMAT_WITHOF = " of ";

	//CAP-37757
	public static final String ORDER_EMAIL_DESC = "_DESC";
	public static final String HEADER_EMAIL_TEXT= "Emails";
	public static final String HTTPS_PROTOCAL_DESC="https://";

	//CAP-36916
	public static final String ITEM_UNDERSHIPMENT_INFO_AVAILABLE = "Item information available";
	public static final String NO_ITEM_UNDERSHIPMENT_INFO_AVAILABLE= "No Item information available";
	public static final String SHIPMENT_INFO_AVAILABLE= "Shipment information available.";
	public static final String NO_SHIPMENT_INFO_AVAILABLE= "No Shipment information available.";

	//CAP-38720 - generic error message constant to load message
	public static final String ERROR_LOAD_ORDER = "sf.errorLoadOrder";

	//CAP-38720 - validation constant
	public static final int MINLEN_0 = 0;
	public static final int MAXLEN_10 = 10;
	public static final String OS_SORTLINEBY="ORD_LN_NR";

	//CAP-38156
	public static final String CP_NO_IMAGE_NO_CONTEXT = "/images/global/NoImageAvailable.png";

	//CAP-39159
	public static final String CP_NO_IMAGE = "/cp/images/global/NoImageAvailable.png";
	public static final String C1UX_NO_IMAGE_MEDIUM = "/assets/images/No-image-106x106.svg";

	// CAP-39904 - add constants for max size for self-admin fields
	public static final int SELF_ADMIN_MAX_SIZE_FIRST_NAME = 25;
	public static final int SELF_ADMIN_MAX_SIZE_LAST_NAME = 25;
	public static final int SELF_ADMIN_MAX_SIZE_BASIC_PHONE = 24;
	public static final int SELF_ADMIN_MAX_SIZE_BASIC_EMAIL = 128;

	public static final int SELF_ADMIN_MAX_SIZE_COMP_NAME1 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_NAME2 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_TITLE = 50;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_FAX = 24;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_PHONE = 24;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_DEPARTMENT = 30;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_DIVISION = 30;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_WEB_URL = 255;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_IMG_URL = 255;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_LINE1 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_LINE2 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_LINE3 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_CITY = 30;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_STATE_CD = 4;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_STATE = 40;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_ZIP = 12;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_COUNTRY_CD = 3;
	public static final int SELF_ADMIN_MAX_SIZE_COMP_COUNTRY = 30;

	//CAP-39973
	public static final int MAX_ADDRESS_RESULTS_COUNT=100;
	public static final String CMALS_CT = "country";
	public static final String CMALS_ST = "state";
	public static final String CMALS_ZIP = "zip";
	public static final String CMALS_SN1 = "shiptoname1";
	public static final String CMALS_SN2 = "shiptoname2";
	public static final String CMALS_B2C = "billToCode";
	public static final String CMALS_LC1 = "loc1";
	public static final String CMALS_LC2 = "loc2";
	public static final String CMALS_LC3 = "loc3";

	public static final int NUMERIC_3 = 3;
	public static final int NUMERIC_4 = 4;
	// CAP-45133
	public static final int NUMERIC_9 = 9;
	public static final int NUMERIC_12 = 12;
	public static final int NUMERIC_16 = 16;
	public static final int NUMERIC_25 = 25;
	public static final int NUMERIC_30 = 30;
	public static final int NUMERIC_35 = 35;

	//CAP-47450
	public static final int NUMERIC_1 = 1;
	public static final int NUMERIC_10 = 10;
	public static final int NUMERIC_15 = 15;
	public static final int NUMERIC_24 = 24;
	public static final int NUMERIC_255 = 255;

	//CAP-40617
	public static final int SAVED_ORDER_NAME_VALID_MAX_LENGTH = 150;

	public static final String CRITERIAFIELDKEY = "criteriaFieldKey";
	public static final String CRITERIAFIELDVALUE = "criteriaFieldValue";
	//CAP-45054
	public static final int VALID_WCSS_ORDERNUMBER_LENGTH = 8;
	//CAP-45677
	public static final int VALID_INVOICE_NBR_LENGTH = 9;



	// CAP-40324
	public static final String ADDR_SRC_FIELD = "addressSource";
	public static final String COUNTRY_FIELD = "country";
	public static final String STATE_FIELD = "stateOrProvince";
	public static final String CITY_FIELD = "city";
	public static final String ADDR_LINE_1_FIELD = "addressLine1";
	public static final String ADDR_LINE_2_FIELD = "addressLine2";
	public static final String ADDR_LINE_3_FIELD = "addressLine3";
	public static final String SHIP_TO_NAME_FIELD = "shipToName";
	public static final String SHIP_TO_NAME_2_FIELD = "shipToName2";
	public static final String POSTAL_FIELD = "postalCode";
	public static final String PHONE_FIELD = "phoneNumber";
	public static final String SHIP_TO_ATTN_FIELD = "shipToAttention";
	public static final String BILL_TO_ATTN_FIELD = "billToAttention";
	public static final String BILL_TO_CD_FIELD = "billToCode";
	public static final String CORP_NBR_FIELD = "corporateNbr";
	public static final String SOLD_TO_NBR_FIELD = "soldToNbr";
	public static final String WCSS_SHIP_TO_NBR_FIELD = "wcssShipToNbr";
	public static final String PHONE_NBR_REGEX = "[0-9\\s+\\-().]+";
	public static final String NUMBER_REGEX = "[\\d ]*";

	//CAP-38135
	public static final String NON_DEGIT_REGEX = "[\\D ]*";

	// CAP-40324 - add constants for max size for self-admin fields
	public static final int DELIVERY_INFO_MAX_SIZE_SHIP_TO_NAME = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_SHIP_TO_NAME_2 = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_ADDRESS_LINE_1 = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_ADDRESS_LINE_2 = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_ADDRESS_LINE_3 = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_CITY = 30;
	public static final int DELIVERY_INFO_MAX_SIZE_STATE_CD = 4;
	public static final int DELIVERY_INFO_MAX_SIZE_ZIP_CD = 12;
	public static final int DELIVERY_INFO_MAX_SIZE_COUNTRY_CD = 3;
	public static final int DELIVERY_INFO_MAX_SIZE_PHONE = 24;
	public static final int DELIVERY_INFO_MAX_SIZE_SHIP_TO_ATTENTION = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_BILL_TO_ATTENTION = 35;
	public static final int DELIVERY_INFO_MAX_SIZE_ADDRESS_SOURCE = 3;
	public static final int DELIVERY_INFO_REQ_SIZE_BILL_TO_CODE = 16;
	public static final int DELIVERY_INFO_REQ_SIZE_CORPORATE_NUMBER = 10;
	public static final int DELIVERY_INFO_REQ_SIZE_SOLD_TO_NUMBER = 5;
	public static final int DELIVERY_INFO_REQ_SIZE_WCSS_SHIP_TO_NUMBER = 4;

	//CAP-39753
	public static final String C1UX_PROPERTY_TYPE="c1ux";
	public static final String C1UX_PROPERTY_KEY_QUICK_START_GUIDE_PATH="quickStartGuidePath";
	public static final String HLINK_TARGET="_blank";

	//CAP-41550
	public static final String PAB_ID_FIELD = "PAB ID";

	//CAP-41136
	public static final String PRICING_TYPE_IT = "IT";

	// CAP-42241 adding to update new address fields to the request and response objects
	public static final String USPS_RETURNING_CITY_AND_STATE_MESSAGE="USPS returned a city and state combination of ";
	public static final String USPS_FOR_THE_ENTERED_ZIPCODE_MESSAGE=" for the entered ZipCode.";
	public static final String USPS_INVALID_CITY_MESSAGE="USPS Zipcode lookup returned an error of Invalid City.  <br>USPS returned a city and state combination of ";
	public static final String USPS_INVALID_ZIPCODE_MESSAGE="USPS Zipcode lookup returned a zip code of ";
	public static final String USPS_ERROR_OF_ZIPCODE_AND_NUM_OF_CHARS_MESSAGE=" for the entered address information<br>USPS City and State lookup returned an error of ZIPCode must be 5 characters";
	public static final String USPS_FIELDS_CORRECTION_MESSAGE="<br>Please correct the following fields:<br>&#187;US Zip Codes should be at least 5 digits.";
	public static final String USPS_FOR_THE_ENTERED_INFORMATION_MESSAGE=" for the entered address information<br>USPS City and State lookup returned an error of Invalid Zip Code.";
	//CAP-42675 updated constants and method to handle the multiple/ mixed USPS error
	public static final String USPS_MIXED_MESSAGE_ZIP_STATE_AND_CITY=" for the entered address information<br>";
	public static final String USPS_MESSAGE_AND=" and ";

	//CAP-42803 - added constant for picking Zip/Postal Code translation
	public static final String ZIP_POSTAL_LABEL="zip_postal_lbl";

	//CAP-42862 added constants and method to handle the Address Not Found
	public static final String USPS_ADDRESS_NOT_FOUND = "USPS Zipcode lookup returned an error of Address Not Found.  <br>USPS returned a city and state combination of ";

	// CAP-44032
	public static final String INVOICE_ACCOUNT_CD = "IV";
	public static final String CREDIT_CARD_CD = "CC";

	// CAP-44312
	public static final String OPTION_YES = "Y";
	public static final String OPTION_NO = "N";
	public static final String OPTION_ON_CARRIER_CHANGE = "C";

	//CAP-44387
	public static final String PROFILE_TYPE_USER = "USER";
	public static final String UPLOAD_TYPE_REPLACE = "R";

	// CAP-44427
	public static final String CUST_DOC_PROOF_FILE_NOT_FOUND_PATH = "/assets/images/FileNotFound.jpg";

	// CAP-44487
	public static final int DEFAULT_USER_PROFILE_DEFINITION_NUMBER = 0;

	// CAP-45040
	public static final String LINE_ONLY = "L";

	//CAP-45057
	public static final int CRITERIA_INV_NUMBER_LENGTH = 9;
	public static final int CRITERIA_ORD_TITLE_LENGTH = 150;
	public static final int CRITERIA_SHIP_TO_NAME_LENGTH = 140;
	public static final int CRITERIA_SHIP_TO_ATTENTION_LENGTH = 35;
	public static final int CRITERIA_PO_NUMBER_LENGTH = 20;
	public static final int CRITERIA_ORD_NUMBER_LENGTH = 8;
	public static final int CRITERIA_ITEM_NUMBER_LENGTH = 30;

	// CAP-44467
	public static final int OOB_USER_ID_CRITERIA_MAX_LENGTH = 16;
	public static final int OOB_PROFILE_ID_CRITERIA_MAX_LENGTH = 128;
	public static final int OOB_NAME_CRITERIA_MAX_LENGTH = 25;
	public static final String OOB_USER_ID_CRITERIA_FIELD_NAME = "userID";
	public static final String OOB_PROFILE_ID_CRITERIA_FIELD_NAME = "profileID";
	public static final String OOB_FIRST_NAME_CRITERIA_FIELD_NAME = "firstName";
	public static final String OOB_LAST_NAME_CRITERIA_FIELD_NAME = "lastName";

	//CAP-45709
	public static final int OOB_LOGIN_ID_CRITERIA_MAX_LENGTH = 16;
	public static final String OOB_LOGIN_ID_CRITERIA_FIELD_NAME = "loginID";
	public static final String OOB_PROFILE_NUMBER_CRITERIA_FIELD_NAME = "profileNumber";

	// CAP-46322
	public static final String STANDARD_ATTRIBUTE_FILTER_FAVORITE = "favorite";
	public static final String STANDARD_ATTRIBUTE_FILTER_NEW_ITEM = "newItem";

	//CAP-46634
	public static final String ATTR_DISP_TYPE_VIEWONLY="ViewOnly";
	public static final String ATTR_DISP_TYPE_EDITABLE="Editable";
	public static final String ATTR_DISP_TYPE_EDITABLEREQ="EditableRequired";

	//CAP-46964
	public static final String CALLING_FROM_CHECKOUT="Checkout";

	//CAP-47085
	public static final String PERIOD=".";

	//CAP-47145
	public static final String ALLOC_CUST_ITEM_NUMBER_FIELD_NAME	= "customerItemNumber";
	public static final String ALLOC_VENDOR_ITEM_NUMBER_FIELD_NAME	= "vendorItemNumber";
	public static final int MAX_LENGTH_CUSTOMER_ITEM_NUMBER	= 30;
	public static final int MAX_LENGTH_VENDOR_ITEM_NUMBER	= 15;

	//CAP-47998
	public static final int MAX_LENGTH_LIST_NAME= 25;
	public static final int MAX_LENGTH_LIST_DESC= 2000;
	public static final String LIST_NAME_LBL ="listName";
	public static final String LIST_DESC_LBL ="listDesc";

	// CAP-47969
	public static final String BREAK = "<br>";
	public static final String RIGHT_ANGLE_QUOTE = "&raquo;";

	//CAP-48002
	public static final String MANAGE_LIST_SESSION_OBJECT = "manageLists";
	public static final String DISTRIBUTION_LIST_SESSION_OBJECT = "distributionListDetails";

	// CAP-48503
	public static final String EXPECTED_403MESSAGE = "Access to this service is not allowed";

	// CAP-48497
	public static final String LMAP_LIST_NAME1 = "NM_1";
	public static final String LMAP_LIST_NAME1_VALUE = "Name 1";
	public static final String LMAP_LIST_ADDR1 = "AD_1";
	public static final String LMAP_LIST_ADDR1_VALUE = "Address 1";
	public static final String LMAP_LIST_STATENM = "STATE_NM";
	public static final String LMAP_LIST_STATENM_VALUE = "State";
	public static final String LMAP_LIST_CITYNM = "CITY_NM";
	public static final String LMAP_LIST_CITYNM_VALUE = "City";
	public static final String LMAP_LIST_ZIPCD = "ZIP_CD";
	public static final String LMAP_LIST_ZIPCD_VALUE = "ZIP";

	// CAP-48457
	public static final String DTD_STATUS_SUCCESS = "0";
	public static final String DTD_STATUS_ERROR = "1";
	public static final String DTD_STATUS_WARNING = "2";
	public static final String DTD_STATUS_NO_MESSAGE = "3";

	// CAP-48777
	public static final String TRANS_NM_LIST_UPLOAD_INVALID_CHARS = "list_upload_inv_chars";
	// CAP-46498 - this will be the number of records displayed per page of mapped data in custom docs order entry only
	public static final int NUM_MAPPED_DATA_RECORDS_PER_PAGE_DISPLAYED = 10;
	
	//CAP-48890
	public static final String TEMPLATE_ORDER_TEMPLATE_NAME = "ORD_TMPLT_NM";
	public static final String TEMPLATE_DELETE_EVT = "TEMPLATE_DELETE_EVT";
	
	// CAP-48889- add constants for order template ID field Name
	public static final String ORDER_TEMPLATE_ID_FIELDNAME = "orderTemplateID";
	
	// CAP-49326- add constants for Style ID field Name
	public static final String STYLE_ID_FIELDNAME = "styleID";

	// CAP-49280
	public static final String DICE = "DICE";
	public static final String LINK_TO_EMAIL = "LinkToEmail";
	public static final String LINK_TO_LANDING_PAGE = "LinkToLandingPage";
	
	//CAP-49731 add constants for targetLoginID field Name
	public static final String TARGET_LOGINID_FIELDNAME = "targetLoginID";
	
}