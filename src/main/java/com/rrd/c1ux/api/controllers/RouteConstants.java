/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		JIRA#						Description
 * 	--------	-----------		---------		--------------------------------
 *				T Harmon			CAP-35537		Changes for SAML
 *	09/12/22	A Boomker			CAP-35436		Adding service for returning messages for message center
 *  09/27/22    S Ramachandran  	CAP-35439       Add service for Punchout Transfer Cart validation
 *  10/10/22	Krishna Natarajan	CAP-36438/36448 added new fields with the comment
 *	10/25/22	A Boomker			CAP-36153		Add constant to route to cart
 *	11/07/22	Krishna Natarajan 	CAP-36999		Add constant REPEAT_SEARCH_UNIQUE_TERM
 *  12/01/2022	E Anderson			CAP-36154		BE Updates for translation.
 *  11/23/22	S Ramachandran  	CAP-36557   	Add route constant to get Order details for an order/salesref
 *  12/23/2022	E Anderson			CAP-36154		BE Updates for translation.
 *  12/23/22	S Ramachandran		CAP-36916		Add route constant to get Order shipments and tracking, items under shipments
 *  01/10/2023  E Anderson          CAP-36154       Derive the appNamePrefix from XST522.
 *  01/12/2023  S Ramachandran		CAP-37781       Add route constant to get Order file email details
 *	03/03/23	L De Leon			CAP-38053		Added constants for the implementation of retrieving delivery information
 *	03/15/23    Satishkumar A       CAP-38736       API Standardization - Search Navigation in Order Search conversion to standards
 *	03/15/23	A Salcedo			CAP-38152		Added constant for Order Header Info Load API.
 *	03/17/23	S Ramachandran		CAP-38720 		Added route constant for Order Detail by Sales ref with Standardization
 *	03/21/23    Satishkumar A       CAP-38738       API Standardization - Order Files response in Order Search conversion to standards
 *  03/23/23	Sakthi M			CAP-38561		Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
 *	03/24/23	A Boomker			CAP-38155		Adding routing for simple checkout order summary API
 * 	03/27/23	A Boomker			CAP-37891		Adding routing for update password API
 * 	03/27/23	S Ramachandran		CAP-39201 		Adding routing for Profile Definition of User Type
 *  04/10/23	A Boomker			CAP-37890		Adding routing for update basic profile API
 *  04/10/23	S Ramachandran		CAP-38159		Adding routing for Company Master Address search
 *  04/12/23	Satishkumar A       CAP-37497	    Saved Order – Getting the list of saved orders into the saved order page
 *	04/12/23	A Boomker			CAP-38160		Added routing for SAVED_ORDER_EXPANSION
 *	03/14/23	L De Leon			CAP-38151		Added constants for the implementation of saving delivery information
 *  04/19/23	Sakthi M			CAP-39245		Modify Navigation Menu API (the one used) to refactor generation and to make/use new translation text values
 *  04/25/23    Sakthi M			CAP-39335       Validation labels in Item Quantity Validation API to make/use translation text values
 *	04/26/23	A Boomker			CAP-39340		Added routing for SAVED_ORDER_DELETE
 *	04/26/23	A Boomker			CAP-39341		Added routing for SAVED_ORDER_RESUME
 *	04/27/23    Satishkumar A   	CAP-39247       API Change - Modify Message Center Response API to make/use new translation text values
 *	05/04/23    Satishkumar A   	CAP-37503       API Build - Save Order assuming all data already saved
 *	05/08/23	A Boomker			CAP-38153		Added routing for ORDER_HEADER_INFO_SAVE
 *	05/25/23	L De Leon			CAP-38158		Added route constant for the quick copy API
 *	05/25/23	S Ramachandran		CAP-38157		Added route constant for Submit Order API
 *	06/07/23    Sakthi M			CAP-39209		API Change - Modify Navigation Menu API (the one used) to add Admin Tools options
 *	06/14/23	A Salcedo			CAP-39210		Added constants for SAML.
 *  06/15/23    Sakthi M			CAP-39752		C1UX BE - API Change - Modify Navigation Menu API (the one used) to add Reports option
 *  06/23/23	A Salcedo			CAP-41241		Updated CP_REDIRECT_RPT.
 *  06/27/23	A Salcedo			CAP-41693		Added CP_REDIRECT_URL_CD.
 *  06/28/23	Satishkumar A		CAP-41594		C1UX API - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 *  06/30/23	S Ramachandran		CAP-37894		Adding routing for update extended profile API
 *  07/04/23	N Caceres			CAP-37898		Adding routing for Update Company Profile API
 *  07/05/23	Sakthi M			CAP-41961		C1UX BE - API Change - Modify Navigation Menu API - update 'reports' style class for "Reports" Menu
 *	07/18/23	A Boomker			CAP-42295		Adding routing for custom docs UI prototype
 *	07/18/23	L De Leon			CAP-41552		Added route constant for the copy recent API
 *	07/25/23	A Boomker			CAP-42223		Add actual initialize UI and get ui page separate from prototypes
 *	07/25/23	A Boomker			CAP-42225		More changes for cust docs
 *	07/31/23    Satishkumar A      	CAP-33059       C1UX API - API Build - Favorite Toggle Call
 *	07/31/23	S Ramachandran		CAP-41784       Added routing for Get featured items
 *	08/16/23	Satishkumar A      	CAP-42745		C1UX API - Routing Information For Justification Section on Review Order Page
 *	09/01/23	S Ramachandran		CAP-41590		Added routing for Get PAB all or a search
 *	09/04/23	L De Leon			CAP-41595		Added route constant for delete Personal Address Book API
 *	09/05/23	Satishkumar A      	CAP-42763		C1UX BE - Order Routing Justification Text Submit Order
 *	09/18/23	A Boomker			CAP-42298		Add cust docs cancel methods direct access
 *	09/19/23	L De Leon			CAP-43665		Added route constant for get payment information API
 *	10/03/23	L De Leon			CAP-44059		Added route constant for export Personal Address Book addresses API
 *	10/10/23	Satishkumar A		CAP-44196		C1UX API - Create api to retrieve edoc for Storefront
 *	10/11/23	C Codina			CAP-41549		C1UX API - API Build - Make new API for getCarouselMessages using CP logic
 *	10/11/23	N Caceres			CAP-44349		Added route constant to retrieve HTML text assigned to the selected category
 *  10/17/23	AKJ Omisol			CAP-43024		C1UX API - New API for getImprintHistory
 *  10/16/23	M Sakthi			CAP-44468		Added route constants for get alerts.
 *  10/30/23    S Ramachandran		CAP-44469		Added constants for SAML Catalog Alerts PARAM
 *  11/08/23	A Boomker			CAP-44486		Added user profile search API
 *  11/10/23	Krishna Natarajan	CAP-44548		Additional changes made to indicate EDOC internal or external
 *  11/10/23	A Boomker			CAP-44487		Added user profile load API
 *  11/13/23	A Boomker			CAP-44426		Added update working proof API
 *	11/16/23	L De Leon			CAP-45180		Added route constant for get countries
 *	11/07/23	S Ramachandran		CAP-44961 		Added route constant for New version(v1) in save PAB and to show suggested address
 *	11/20/23	Satishkumar A		CAP-38135		C1UX BE - Modification of Manual Enter Address to use new USPS validation
 *	11/16		C Codina			CAP-45054		Added constant for WCSS Order Number
 *	11/27/23	L De Leon			CAP-44467		Added route constant for get Order On Behalf information
 *	12/04/23	Satishkumar A		CAP-45280		C1UX API - Set OOB Mode for CustomPoint session
 *	12/07/23    S Ramachandran  	CAP-45485   	Added route constant to retrieve full user profile of originator in OOB Mode
 *	12/20/23	S Ramachandran		CAP-45953		Added route constant for USPS validation
 *	12/27/23	L De Leon			CAP-45907		Added route constant for get Pattern After Users
 *	01/04/24	Satishkumar A		CAP-45908		Added route constant for retrieve initial user/profile information
 *	01/05/24	Derek L				CAP-43217		Added route constant for App Alive
 *	01/08/24	C Codina			CAP-46200		C1UX API - Method to retrieve Attribute Filters for Order Entry
 *	01/17/24	A Boomker			CAP-44835		Added routing for new upload variable file API
 *	01/22/24	Satishkumar A		CAP-46407		Added routing for Validate EOO attributes
 *	01/29/24	S Ramachandran		CAP-46635		Added route constant for save site attribute information for a user
 *	01/30/24	R Ruth				CAP-44862		Added route constant for insert upload file
 *	01/31/24	N Caceres			CAP-46698		Added route constant for order wizard questions
 *	02/06/24	C Codina			CAP-46723		Added route constant for order wizard search
 *	02/12/24	S Ramachandran		CAP-47062		Added route constant to return list of orders used in current budget time frame
 *	02/13/24	L De Leon			CAP-46960		Added route constant for get budget allocation banner message
 *	02/16/24	Satishkumar A		CAP-46961		Added route constant for remaining budget allocations
 *  02/20/24	T Harmon			CAP-46543		Added route constant for api for eoo save
 *	02/26/24	Krishna Natarajan	CAP-47337		Added route constant for api to validate pattern after user
 *	02/28/24	L De Leon			CAP-47376		Added route constant for validate corporate profile API
 *	02/29/24	Satishkumar A		CAP-47448		Added route constant for validate password
 *	03/01/24	S Ramachandran		CAP-47629		Added route constant for basic profile validation API
 *	03/05/24	Satishkumar A		CAP-47616		Added route constant for api to validate User Defined Fields.
 *  03/12/24	M Sakthi			CAP-47386		Added route constant for api distribution list in checkout
 *	03/12/24	S Ramachandran		CAP-47744		Added route constant for api to download Order file from order in Order Search
 *	03/11/24	N Caceres			CAP-47732		Added route constant for distribution list file link
 *	03/14/24	A Boomker			CAP-46526		Added route constant for initializing cust doc UI from a CP link
 *	03/15/24	C Codina			CAP-47778		Added route constant for distribution list record count
 *	03/15/24	S Ramachandran		CAP-47387		Added route constant to get worksheets and upload file to CP
 *	03/21/24	Satishkumar A		CAP-47389		Added route constant to get retrieve Dist List addresses
 *	03/26/24	S Ramachandran		CAP-47388		Added route constant to upload Dist List and return mapperData.
 *	03/29/24	Krishna Natarajan	CAP-47391		Added route constant to update Dist List info.
 *	04/04/24	L De Leon			CAP-48274		Added route constant for date to destination
 *	04/09/24	Krishna Natarajan	CAP-48537		Added route constant for back to results on wizard search
 *	04/17/24	M Sakthi			CAP-48582		Added route constant for save order template
 *	04/23/24	C Codina			CAP-48623		Added route constant for get template order list
 *  04/23/24	T Harmon			CAP-48796		Added constant for budget allocation summary
 * 	04/23/24	S Ramachandran		CAP-48136		Added route constant for delete order template
 * 	05/02/24	R Ruth				CAP-42226		Added route constant for get list.
 * 	05/10/24	S Ramachandran		CAP-49205		Added route constant to get style information for EFD order
 * 	05/13/24	N Caceres			CAP-49151		Added route constant to get EFD Options
 * 	05/15/24	R Ruth				CAP-42228		Added route constant for get list mappings.
 *  05/20/24	R Ruth				CAP-42230		Added route constant for save list mappings.
 * 	05/28/24	A Boomker			CAP-48604		Added route constant for save list
 *	05/27/24	L De Leon			CAP-49609		Added route constant for get linked logins
 * 	05/28/24	Satishkumar A		CAP-49610		Added route constant to login as a linked login ID/user
 *  05/29/24	M Sakthi			CAP-49694		Added route constant for get suggested items
 * 	06/04/24	A Boomker			CAP-42231		Adding get mapped data page
 * 	06/06/24	C Codina			CAP-38842		Changing the get-pnatiered endpoint
 * 	06/07/24	N Caceres			CAP-50006		Added route constant for add kit component
 *	06/10/24	L De Leon			CAP-49882		Added route constant for init kit template
 *	06/11/24	Satishkumar A		CAP-50007		Added route constant for remove kit component from session
 *	06/18/24	N Caceres			CAP-50186		Added route constant for adding wild card component to kit template
 *	06/19/24	M Sakthi			CAP-50145		Added route constant for add the components to our order
 *	06/25/24	Satishkumar A		CAP-50308		Added route constant for reload KitSession when coming back to kit editor from search or custom docs
 *	06/26/24	N Caceres			CAP-50309		Added constant for kit catalog browse API
 *	06/26/24	L De Leon			CAP-50359		Added route constant for canceling kit template editing process
 *	07/01/24	A Boomker			CAP-46488		Added CUST_DOCS_INITIALIZE_API_FOR_KIT
 *	07/02/24	A Boomker			CAP-46489		Added KIT_EDITOR_ROUTING
 *	07/03/24	Satishkumar A		CAP-50560		Added route constant for catalog search for kit template
 *	07/04/24	C Codina			CAP-46486		Added route constant for edit custom doc
 *	07/09/24	A Boomker			CAP-46538		Added routings for cust doc imprint history searches
 */

package com.rrd.c1ux.api.controllers;

public class RouteConstants {

    private RouteConstants() {
        // private to ensure constants only
    }

    // non-API routes
    public static final String OAUTH_LOGIN = "/oidc-login";
    public static final String OAUTH_LOGOUT = "/oidc-logout";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String HOME = "/";
    public static final String ERROR = "/error";
    public static final String SSO_LOGIN_SAML2 = "/sso-login/saml2";	// CAP-35537

    // API routes
    public static final String HEALTH = "/api/health";
    public static final String VERSION = "/api/version";

    public static final String USERS_PROFILE = "/api/users/profile";
    public static final String USERS_FULL_PROFILE = "/api/users/get-fullprofile";
    public static final String FULL_USER_PROFILE = "/api/users/get-fullprofileoforiginator";
    public static final String ITEMS_UNIVERSAL_SEARCH = "/api/items/universal-search";
    public static final String ITEMS_UNIVERSAL_SEARCH_LINEITEM_RESULT = "/api/items/universal-search-lineitem-result";
    public static final String CATALOG_ITEMS = "/api/items/catalogitems";
    public static final String CATALOG_MENU = "/api/catalog/menu";
    public static final String GET_FAVORITE = "/api/oe/getfavorites";
    public static final String GET_SETTINGS_AND_PREFERENCES = "/api/settingsandprefs/";
    public static final String GET_ITEM_ADD_TO_CART ="api/items/addtocart";

    public static final String NO_TIERED_ITEM_PRICING = "/api/items/notieredpricingitems";
    public static final String SINGLE_ITEM_DETAILS = "/api/singleitem/details";

    public static final String GET_PNA = "/api/items/get-pna";
    public static final String GET_PNATIERED = "/api/items/getpnatiered";
	public static final String GET_NAVIGATION_MENU="/api/navigationmenu/";
    public static final String ITEM_INDICATOR="/api/itemindicator/shoppingcartitemindicator/";
	public static final String REST_SESSIONID = "";
	public static final String ServletContextImageURL = "https://dev.custompoint.rrd.com";
	public static final String ITEM_DETAIL_WITH_QUANTITY="users/itemdetailwithquantity/";
	public static final String CATALOG_MENU_FOR_PRIME = "/api/catalog/menuforprime";
	public static final String NAVI_MENU_AND_CATALOG_MENU_FOR_PRIME = "/api/catalog/naviandcatalogmenuforprime";
	public static final String GET_CART_ITEM_DETAIL = "/api/items/getcartitemdetail";
	public static final String UPDATE_SHOPPING_CART_ITEMS = "/api/shoppingcart/updateshoppingcartitemdetail";
	public static final String REMOVE_SPECIFIC_ITEM ="/api/removespecificitem/removespecificitem";
	public static final String CACHE_CATALOG_UTILITY_NAVIGATION ="/api/items/cachenavigation";
	public static final String GET_CSS_LINK ="/api/styles/getstandardstylefileurl";
	public static final String SHOW_MESSAGE_CENTER_LINK="/api/messages/showmessagecenter/";
	public static final String GET_LANDING_INFO="/api/landing/landinginfo";
	public static final String SUPPORT_PAGE_LOAD_INFO = "/api/supportpage/loadinfo";
	public static final String SUPPORT_PAGE_SUBMIT_FORM ="/api/supportpage/submitform";
	public static final String SEARCH_ORDERS_DETAIL ="/api/orders/getsearchordersdetail";
	public static final String SEARCH_ORDERS_REPEAT ="api/orders/getordersearchrepeat";



	//CAP-35439
	public static final String PUNCHOUT_TRANSFER_CART_VALIDATION ="/api/punchout/transfercartvalidation";
	//CAP-35440
	public static final String PUNCHOUT_TRANSFER_CART="/api/punchout/punchoutTransferCart";

    // General Constant
	public static final String REST_RESPONSE_INVALID_SECURITY = "Invalid Security";
	public static final String REST_RESPONSE_FAIL = "Failed";
    public static final String REST_RESPONSE_INVALID = "Invalid";
	public static final String REST_RESPONSE_SUCCESS = "Success";
	public static final String REST_RESPONSE_NOT_FOUND = "Not Found";
	public static final String HTML_H3_OPEN_TAG = "<H3>";
	public static final String HTML_H3_CLOSE_TAG = "</H3>";
	public static final String DOUBLE_DASH = "--";
	public static final String DOUBLE_DASH_API = "--API";
	public static final String EMPTY_STRING="";
	public static final String SPLIT_CHAR_COMMA=",";
	public static final String YES_FLAG="Y";
	public static final String NO_FLAG="N";
	public static final String MESSAGE_BOARD_WIDGET_NAME="pw_msg_board";
	public static final String SINGLE_SPACE=" ";
	public static final String SINGLE_DASH="-";
	public static final String FOWARD_SLASH="/";
	public static final String LOAD_FOOTER="/api/footertext";
	public static final String TERMS_FULL_TEXT_LINK="https://custompoint.rrd.com/xs2/global/disclaimers/termsofuse.htm";
	public static final String PRIVACY_FULL_TEXT_LINK="https://www.rrd.com/privacy-policy";
	public static final String CANCEL_ORDER_LINK="https://dev.custompoint.rrd.com/cp/orders/cancelorder.cp";
	public static final String ADD_QUERY_PARAM="?";
	public static final String ADD_AMP_ANOTHER_PARAM="&";
	public static final String TTSESSIONID_PARAM="ttsessionid=";
	public static final String CANCEL_ACTION_PARAM="cancelAction=";


	//CAP-35439
	public static final String TRANSFER_CART_VALIDATION_ERROR ="Transfer cart validation success with Errors";
	public static final String TRANSFER_CART_VALIDATION_NOERROR ="Transfer cart validation success with NoErrors";
	public static final String TRANSFER_CART_REST_MESSAGE_FAIL ="Transfer cart validation failed with Exception";
	public static final String UPDATE_CART_VALIDATION_ERROR ="Update cart validation success with Error";
	public static final String UPDATE_CART_VALIDATION_NOERROR ="Update cart validation success with NoError";

	//CAP-36999
	public static final String REPEAT_SEARCH_UNIQUE_TERM = "SakthivelVladimirAmeliaBhuvaneshwaranKrishnaMohammedSatishAlexanderAnzarSubbianDineshKatherineAswinEric";


	//Menu for PrimeNG response
	public enum Menu {
	    SHOP_BY_CATALOG("1,Shop By Catalog,shopbycatalog,"),
	    SHOP_BY_CATALOG_API("11,--API,API,false"),
	    ORDERS("2,Orders,orders,"),//CAP-37367 adding orders sub menu
	    ORDERS_ORDER_SEARCH("21,--Order Search,ordersearch,false"),
	    ORDERS_SAVED_ORDERS("22,--Saved Orders,savedorders,true"),
	    SUPPORT("3,Support,support,"),
	    SUPPORT_MESSAGE_CENTER("31,--Message Center,messagecenter,false"),
	    HELP_AND_CONTACT("32,--Help & Contact,helpandcontact,true");

		String menuitems;
		Menu(String sMenu) {
			menuitems = sMenu;
		}
		@Override
		public String toString() {
		return menuitems;
		}
	}

	// CA-34510 At present UOM description added in Constant. Later need to get the value in
	// translation file once implemented in C1UX
	 public static final String C1UX_UOM_EA = "EA";
	 public static final String C1UX_UOM_CS = "CS";
	 public static final String C1UX_UOM_RL = "RL";
	 public static final String C1UX_UOM_CT = "CT";
	 public static final String C1UX_UOM_M = "M";
	 public static final String C1UX_UOM_BX = "BX";

	//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
	 public static final String C1UX_UOM_EA_VALUE = "uomFullTextEA";
	 public static final String C1UX_UOM_CS_VALUE = "uomFullTextCS";
	 public static final String C1UX_UOM_RL_VALUE = "uomFullTextRL";
	 public static final String C1UX_UOM_CT_VALUE = "uomFullTextCT";
	 public static final String C1UX_UOM_M_VALUE = "uomFullTextM";
	 public static final String C1UX_UOM_BX_VALUE = "uomFullTextBX";

	//CAP-35069 additional UOM for catalog line items with full text retrieved from WCSS
	 public static final String C1UX_UOM_BG ="BG";
	 public static final String C1UX_UOM_BK ="BK";
	 public static final String C1UX_UOM_BL ="BL";
	 public static final String C1UX_UOM_BR ="BR";
	 public static final String C1UX_UOM_C  ="C";
	 public static final String C1UX_UOM_DZ ="DZ";
	 public static final String C1UX_UOM_FM ="FM";
	 public static final String C1UX_UOM_GR ="GR";
	 public static final String C1UX_UOM_JK ="JK";
	 public static final String C1UX_UOM_KT ="KT";
	 public static final String C1UX_UOM_LR ="LR";
	 public static final String C1UX_UOM_LT ="LT";
	 public static final String C1UX_UOM_PD ="PD";
	 public static final String C1UX_UOM_PK ="PK";
	 public static final String C1UX_UOM_PL ="PL";
	 public static final String C1UX_UOM_PR ="PR";
	 public static final String C1UX_UOM_RM ="RM";
	 public static final String C1UX_UOM_SH ="SH";
	 public static final String C1UX_UOM_ST ="ST";
	 public static final String C1UX_UOM_TB ="TB";
	 public static final String C1UX_UOM_TT ="TT";
	 public static final String C1UX_UOM_US ="US";
	 public static final String C1UX_UOM_UT ="UT";

	//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
	 public static final String C1UX_UOM_BG_VALUE = "uomFullTextBG";
	 public static final String C1UX_UOM_BK_VALUE = "uomFullTextBK";
	 public static final String C1UX_UOM_BL_VALUE = "uomFullTextBL";
	 public static final String C1UX_UOM_BR_VALUE = "uomFullTextBR";
	 public static final String C1UX_UOM_C_VALUE = "uomFullTextC";
	 public static final String C1UX_UOM_DZ_VALUE = "uomFullTextDZ";
	 public static final String C1UX_UOM_FM_VALUE = "uomFullTextFM";
	 public static final String C1UX_UOM_GR_VALUE = "uomFullTextGR";
	 public static final String C1UX_UOM_JK_VALUE = "uomFullTextJK";
	 public static final String C1UX_UOM_KT_VALUE = "uomFullTextKT";
	 public static final String C1UX_UOM_LR_VALUE = "uomFullTextLR";
	 public static final String C1UX_UOM_LT_VALUE = "uomFullTextLT";
	 public static final String C1UX_UOM_PD_VALUE = "uomFullTextPD";
	 public static final String C1UX_UOM_PK_VALUE = "uomFullTextPK";
	 public static final String C1UX_UOM_PL_VALUE = "uomFullTextPL";
	 public static final String C1UX_UOM_PR_VALUE = "uomFullTextPR";
	 public static final String C1UX_UOM_RM_VALUE = "uomFullTextRM";
	 public static final String C1UX_UOM_SH_VALUE = "uomFullTextSH";
	 public static final String C1UX_UOM_ST_VALUE = "uomFullTextST";
	 public static final String C1UX_UOM_TB_VALUE = "uomFullTextTB";
	 public static final String C1UX_UOM_TT_VALUE = "uomFullTextTT";
	 public static final String C1UX_UOM_US_VALUE = "uomFullTextUS";
	 public static final String C1UX_UOM_UT_VALUE = "uomFullTextUT";

	 //To remove item
	 public static final String REMOVE_ITEM_FROM_CART_ACTION= "REMOVE_ITEM_FROM_CART_ACTION";

	 //To Item Quantity validation

	 public static final String ITEM_QTY_VALIDATION="/api/itemqtyvalidation/itemquantityvalidation";
	 public static final String VALIDATION_SUCCESS="Item Quantity Validated successfully";

	//CAP-35437   Create API service to return URL for destination for continue shopping
	 public static final String CART_CONTINUE_SHOPPING_REDIRECT="/api/shoppingcart/determineContinueDestination";
	 public static final String REPEAT_ITEM_SEARCH="/api/items/repeatItemSearch";
	 public static final String REPEAT_CATALOG_ITEMS="/api/catalogitem/repeatItemSearch";
	 public static final String RETURN_TO_EMPTY="";
	 public static final String HOME_PAGE_URL="/home";
	 public static final String RETURN_TO_RESULTS_URL="search/"+REPEAT_SEARCH_UNIQUE_TERM;
	 public static final String RETURN_TO_CATALOG_URL="shopbycatalog";

	 // CAP-35436
	 public static final String MESSAGE_CENTER_MESSAGES = "/api/messagecenter/getMessages";
	 public static final String C1UX_CONTEXT_PATH = "/";

	 //CAP-35767
	 public static final String CANCEL_PUNCHOUT_QUOTE="/api/cancelPunchoutQuote";

	 //CAP-35768
	 public static final String CANCEL_PUNCHOUT_EDITS="/api/cancelPunchoutQuoteEdit";

	 //CAP-35765
	 public static final String PUNCHOUT_SIGN_OUT="/api/punchout/logout";

	 //CAP-36418
	 public static final String KEEP_SESSION_ALIVE="/api/sessionlive";

	 // CAP-36153
	 public static final String CART_ENTRY_ROUTING_URL = "cart";

	 //CAP-37029
	 public static final String SESSION_TIMEOUT_STATUS_UPDATE="/api/timeoutSession";

	 //CAP-36557
	 public static final String ORDER_SEARCH_DETAILS_FORSALESREF = "/api/orders/getorderdetails-forsalesref";

	 //CAP-42746
	 public static final String ORDER_SEARCH_ROUTING_DETAILS = "/api/oe/routingdetails";

	 //CAP-38720
	 public static final String ORDER_SEARCH_LOAD_DETAIL = "/api/os/loaddetail";

	 //CAP-37781
	 public static final String ORDER_FILE_EMAIL_DETAIL	 = "api/orders/getorderfileemailDetail";

	//CAP-36916
	public static final String ORDER_SEARCH_SHIPMENTS_AND_TRACKING = "/api/orders/getorderdetails-shipmentsandtracking";

	//CAP-36916
	public static final String ORDER_SEARCH_ITEMS_UNDER_SHIPMENTS = "/api/orders/getorderdetails-itemsundershipments";

	 //CAP_36560
	 public static final String GET_ORDER_SORT_PAGINATION = "/api/orders/getordersearchsortpagination";

	 //CAP-36154
	 public static final String GET_TRANSLATION_FOR_VIEW = "/api/gettranslationforview/{viewName}";

	 //CAP-36854
	 public static final String ORDER_SEARCH_NAVIGATION = "/api/orders/order-search-navigation";
	 public	static final String CUSTOMTEXT_LBL_TITLE = "ordersearch_lbl_title";
	 public static final String CUSTOMTEXT_LBL_SEARCH = "ordersearch_lbl_search";
	 public	static final String CUSTOMTEXT_LBL_FOR = "ordersearch_lbl_for";
	 public	static final String CUSTOMTEXT_LBL_DATERANGE = "ordersearch_lbl_daterange";
	 public	static final String CUSTOMTEXT_LBL_SCOPE = "ordersearch_lbl_scope";
	 public	static final String CUSTOMTEXT_SCOPE_A = "ordersearch_scope_A";
	 public static final String CUSTOMTEXT_SCOPE_M = "ordersearch_scope_M";
	 public	static final String CUSTOMTEXT_SCOPE_T = "ordersearch_scope_T";
	 public	static final String CUSTOMTEXT_SCOPE_A_VAL = "A";
	 public	static final String CUSTOMTEXT_SCOPE_M_VAL = "M";
	 public static final String CUSTOMTEXT_SCOPE_T_VAL = "T";

	 //CAP-37295
	 public static final String ORDER_STATUS_SEARCH = "/api/orders/getorderstatuscodeoptions";

	 //CAP-37757
	 public static final String ORDER_FILE_CONTENT= "/api/orders/getorderfilecontent";

	//CAP-37494
	public static final String SAVED_ORDERS="savedorders";

	// CAP-38053
	public static final String GET_DELIVERY_INFO = "/api/checkout/getdeliveryinformation";
	public static final String HTTP_OK = "200";
	public static final String HTTP_UNPROCESSABLE_ENTITY = "422";


	// CAP-38159
	public static final String GET_COMPANY_MASTER_ADDR = "/api/checkout/getcompanymasteraddr";

	//CAP-46200
	public static final String GET_ATTRIBUTE_FILTERS = "/api/catalog/attributes";

	//CAP-38152
	public static final String ORDER_HEADER_INFO_LOAD = "/api/checkout/getorderheaderinfo";

	//CAP-38736
	public static final String ORDER_SEARCH_LOAD_OPTIONS = "/api/os/loadoptions";
	public static final String HTTP_BAD_REQUEST = "400";
	public static final String HTTP_UNAUTHORIZED = "401";
	public static final String HTTP_FORBIDDEN = "403";

	//CAP-38737
	public static final String ORDER_STATUS_CODES = "/api/os/statuscodes";
	//CAP-38651
	public static final String OS_DETAIL = "/api/os/search";
	public static final int VALID_SALESREF_LENGTH = 20;
	public static final int VALID_PONUMBER_LENGTH = 20;
	public static final int VALID_ITEMNUMBER_LENGTH = 30;
	public static final int VALID_STATUS_LENGTH = 4;
	public static final int VALID_STATUSMODIFIER_LENGTH = 2;

	//CAP-45054
	public static final int VALID_WCSS_ORDERNUMBER_LENGTH = 8;

	//CAP-38561
	public static final String SF_PREFIX= "sf.";

	//CAP-38738
	public static final String ORDER_SEARCH_ORDER_FILES = "/api/orderfilecontent";
	public static final String EMAIL_RETRIVAL_SUCCESS = "sf.emailRetrivalSuccess";
	public static final String CANNOT_COMPLETE_REQUEST = "sf.cannotCompleteRequest";

	//CAP-38155
	public static final String ORDER_SUMMARY_LOAD = "/api/checkout/ordersummaryload";

	// CAP-37891
	public static final String UPDATE_PW_API = "/api/admin/updatepw";

	//CAP-39201
	public static final String PROFILE_DEFINITION = "/api/admin/profiledef";

	//CAP-39164
	public static final String GET_PASSWORD_RULE = "/api/admin/getpasswordrule";

	// CAP-37890
	public static final String UPDATE_BASIC_PROFILE_API = "/api/admin/updatebasicprfl";

	//CAP-37901
	public static final String UPDATE_USER_DEFINE_FIELDS_API = "/api/admin/updateuserdefinedfields";

	//CAP-37894
	public static final String UPDATE_EXTENDED_PROFILE_API = "/api/admin/updateextendedprfl";

	//CAP-46635
	public static final String SAVE_SITE_ATTRIBUTES = "/api/admin/savesiteattributes";

	//CAP-41590
	public static final String SEARCH_PAB = "/api/pab/search";

	//CAP-41591
	public static final String SEARCH_OE_PAB = "/api/pab/oesearch";

	//CAP-41593
	public static final String SAVE_PAB = "/api/pab/save";

	//CAP-44961
	public static final String SAVE_PAB_V1 = "/api/pab/v1/save";

	//CAP-CAP-43996
	public static final String IMPORT_PAB = "/api/pab/import";

	//CAP-41595
	public static final String DELETE_PAB = "/api/pab/delete";

	// CAP-44059
	public static final String EXPORT_PAB = "/api/pab/export";

	//CAP-37497
	public static final String SAVED_ORDERS_LIST="/api/orders/savedorders";
	//CAP-38160
	public static final String SAVED_ORDER_EXPANSION="/api/orders/savedorderexpand";

	// CAP-38151
	public static final String SAVE_DELIVERY_INFO = "/api/checkout/savedeliveryinformation";

	//CAP-38135
	public static final String SAVE_DELIVERY_INFO_V1 = "/api/checkout/v1/savedeliveryinformation";

	//CAP-45953
	public static final String VALIDATE_US_ADDRESS = "/api/usps/validateAddress";

	//CAP-39245
	public static final String TOP_LVL_MENU_SHOP_BY_CATALOG="shopbycatalog";
	public static final String TOP_LVL_MENU_ORDERS="orders";
	public static final String TOP_LVL_MENU_SUPPORT="support";
	//CAP-39340
	public static final String SAVED_ORDER_DELETE = "/api/orders/savedorderdelete";
	// CAP-39341
	public static final String SAVED_ORDER_RESUME = "/api/orders/savedorderresume";
	//CAP-37503
	public static final String SAVE_ORDER="/api/orders/saveorder";
	// CAP-44349
	public static final String GET_CATALOG_ITEMS="/api/item/getcatalogitems";

	// CAP-38153
	public static final String ORDER_HEADER_INFO_SAVE = "/api/checkout/saveheaderinfo";

	//CAP-39210
	public static final String CP_REDIRECT = "/api/cpredirect";
	public static final String CP_REDIRECT_EP = "/api/cpredirect/{entryPoint}";
	public static final String CP_REDIRECT_PARAMS = "/api/cpredirect/params";//CAP-44686

	// CAP-38158
	public static final String QUICK_COPY_ORDER = "/api/orders/quickcopyorder";

	// CAP-38157
	public static final String SUBMIT_ORDER = "/api/checkout/submitorder";

	//CAP-39209
	public static final String MENU_ADMIN_TOOLS ="menuAdminTools";
	public static final String ADMIN_TOOLS_SYTLE_CLASS="adminTools";
	//CAP-41961
	public static final String REPORTS_SYTLE_CLASS="reports";

	//CAP-39210 SAML Redirect Constants.
	public static final String SAML_LOGIN_ID = "LOGIN_ID";
	public static final String SAML_ACCOUNT = "ACCOUNT";
	public static final String SAML_ENTRY_POINT = "ENTRY_POINT";
	public static final String SAML_PROFILE_ID = "PROFILE_ID";
	public static final String SAML_LOCALE = "LOCALE";

	//CAP-39752
	public static final String TOP_LVL_MENU_REPORTS="reports";
	public static final String CP_REDIRECT_RPT = "RPT";//CAP-41241

	//CAP-41693
	public static final String CP_REDIRECT_URL_CD = "CP_REDIRECT_CLOSE";

	//CAP-41594
	public static final String COUNTRY_STATE_LIST = "/api/util/countriesandstates";

	// CAP-42295/ CAP-42223
	public static final String CUST_DOCS_UI_API = "/api/custdocs/uipage";
	public static final String CUST_DOCS_UI_PAGE_SUBMIT_API = "/api/custdocs/uipagesubmit";
	public static final String CUST_DOCS_INITIALIZE_API = "/api/custdocs/init";
	public static final String CUST_DOCS_GET_UPLOADED_FILE_INFO_API = "/api/custdocs/getuploadinfo";
	public static final String CUST_DOCS_CHECK_IMAGE_PROOF_STATUS = "/api/custdocs/checkproofstatus";
	public static final String CUST_DOCS_GET_PROOF_URL = "/api/custdocs/getprooflink";
	public static final String CUST_DOCS_ADD_TO_CART_API = "/api/custdocs/addtocart";
	public static final String CUST_DOCS_INITIALIZE_API_EDIT_CART = "/api/custdocs/initcartedit";

	//CAP-43024
	public static final String CUST_DOCS_IMPRINT_HISTORY = "/api/custdocs/imprinthistory";

	// CAP-42298
	public static final String CUST_DOCS_CANCEL_API = "/api/custdocs/cancel";
	public static final String CUST_DOCS_CANCEL_ORDER_API = "/api/custdocs/cancelorder";

	// CAP-42226
	public static final String CUST_DOCS_GET_LISTS_API = "/api/custdocs/getLists";

	// CAP-42228
	public static final String CUST_DOCS_GET_LISTS_MAPPING_API = "/api/custdocs/getListsMapping";

	// CAP-42230
	public static final String CUST_DOCS_SAVE_LISTS_MAPPING_API = "/api/custdocs/saveListsMapping";

	// CAP-46503
	public static final String CUST_DOCS_LOAD_ALT_PROFILE_API = "/api/custdocs/loadAltProfile";

	// CAP-37898
	public static final String UPDATE_COMPANY_PROFILE_API = "/api/admin/updatecompanyprfl";

	// CAP-41552
	public static final String COPY_RECENT_ORDER = "/api/orders/copyrecentorder";

	//CAP-42545
	public static final String CANCEL_ORDER="/api/orders/cancelorder";

	//CAP-33059
	public static final String FAVORITE_TOGGLE_CALL = "/api/oe/togglefavorite";

	//CAP-41784
	public static final String GET_FEATURED_ITEMS = "/api/oe/getfeatured";

	// CAP-42295 - avoiding repeating this everywhere
	public static final String UNDERSCORE = "_";

	//CAP-41802
	public static final String GET_ITEM_FILTERS = "/api/oe/getitemfilters";

	//CAP-42745
	public static final String GET_ROUTING_INFORMATION = "/api/oe/routingreview";

	//CAP-42763
	public static final String SUBMIT_ORDER_JUSTIFICATION = "/api/checkout/submitorder-justify";

	// CAP-43665
	public static final String GET_PAYMENT_INFO = "/api/payment/getpaymentinfo";

	//CAP-43668
	public static final String SAVE_PAYMENT_INFO = "/api/payment/savepaymentinfo";

	//CAP-44196
	public static final String GET_EDOC_URL = "/api/edoc/retrieve";

	//CAP-41549
	public static final String GET_CAROUSEL_MESSAGES = "/api/widget/getCarouselMessages";

	//CAP-44468
	public static final String GET_ALERTS ="/api/alerts/getalerts";

	//CAP-44515
	public static final String CP_REDIRECT_FORALERTS ="/api/redirectcp";
	public static final String SAML_PARAM_VALUE_APPROVAL_ALERT ="APR";
	public static final String SAML_PARAM_APPROVAL_QUEUE_ID ="approvalQueueID";
	public static final String SAML_PARAM_VALUE_INVENTORY_ALERT ="ALV";
	public static final String SAML_PARAM_VALUE_INVENTORY_EVENT ="EVT";
	public static final String SAML_PARAM_VALUE_ALERT_TYPE ="alertType";
	public static final String SAML_PARAM_VALUE_CATALOG_ALERT="ALC";
	//CAP-44514
	public static final String SAML_PARAM_VALUE_ITEM_ALERT="ALI";

	//CAP-44663
	public static final String CHECK_ALERTS ="/api/alerts/checkAlerts";

	// CAP-44486, CAP-44487
	public static final String CUST_DOCS_USER_PROFILE_SEARCH = "/api/custdocs/uprofilesearch";
	public static final String CUST_DOCS_USER_PROFILE_LOAD = "/api/custdocs/uprofileload";

	//CAP-44548 additional changes
	public static final String GET_EDOC_EXTERNAL_URL = "/api/edoc/retrieveExternal";

	// CAP-44426		Added update working proof API
	public static final String CUST_DOCS_UPDATE_WORKING_PROOF = "/api/custdocs/updateproof";

	//CAP-45180
	public static final String GET_COUNTRIES = "/api/orders/getcountries";

	// CAP-45181
	public static final String GET_STATES = "/api/orders/getstates";

	// CAP-44467
	public static final String GET_OOB_INFO = "/api/oob/search";

	//CAP-45280
	public static final String OOB_TOGGLE_CALL = "/api/oob/toggleoob";

	// CAP-45907
	public static final String GET_PATTERN_AFTER_USERS = "/api/selfreg/getpatternafterusers";

	//CAP-45908
	public static final String GET_INITIAL_SELF_REG_USER = "/api/selfreg/getinitialselfreguser";

	// CAP-43217
	public static final String API_ALIVE = "/api/alive";
	public static final String CUST_DOCS_VAR_UPLOAD_FILE_API = "/api/custdocs/varupload";

	// CAP-44862
	public static final String CUST_DOCS_INSERT_UPLOAD_FILE = "/api/custdocs/insertuploadfile";

	//CAP-46407
	public static final String GET_VALIDATE_EOO_CHECKOUT = "/api/shoppingcart/validateEOOCheckout";

	//CAP-46543
	public static final String SAVE_EOO_ATTRIBUTES = "/api/shoppingcart/saveSelectedAttributes";

	// CAP-46698
	public static final String GET_ORDER_WIZARD = "/api/catalog/wizard";

	//CAP-46723
	public static final String WIZARD_SEARCH = "/api/catalog/wizardsearch";

	//CAP-47062
	public static final String GET_SINGLE_ITEM_DETAIL_ALLOCATIONS = "/api/singleitem/details/allocations";

	// CAP-46960
	public static final String GET_BUDGET_ALLOCATION_BANNER_MSG = "/api/budgetallocation/getbannermessage";

	//CAP-46961
	public static final String GET_REMAINING_BUDGET_ALLOCATIONS = "/api/budgetallocation/remainingbudget";

	// CAP-48796
	public static final String GET_REMAINING_BUDGET_ALLOCATIONS_SUMMARY = "/api/budgetallocation/remainingbudgetsummary";

	//CAP-47198
	public static final String SAVE_SELF_REG_USER = "/api/selfreg/saveselfregistration";

	//CAP-47337
	public static final String VALIDATE_PATTERN_AFTER = "/api/selfreg/validatePatternAfter";

	//CAP-47375
	public static final String VALIDATE_EXTENDED_PROFILE = "/api/selfreg/validateExtendedProfile";

	// CAP-47376
	public static final String VALIDATE_CORPORATE_PROFILE = "/api/selfreg/validateCorporateProfile";

	//CAP-47448
	public static final String VALIDATE_PASSWORD = "/api/selfreg/validatepassword";

	//CAP-47617
	public static final String VALIDATE_SELF_REG_ATTRIBUTES = "/api/selfreg/validateAttributes";

	//CAP-47629
	public static final String VALIDATE_BASIC_PROFILE = "/api/selfreg/validatebasicprofile";

	//CAP-47616
	public static final String VALIDATE_USER_DEFINED_FIELDS = "/api/selfregistration/validateUserDefinedFields";

	//CAP-47386
	public static final String GET_DISTRIBUTION_LIST = "/api/checkout/getdistributionlists";

	//CAP-47744
	public static final String DOWNLOAD_ORDER_FILES = "/api/orders/getorderlist";

	// CAP-46526
	public static final String CUST_DOCS_INITIALIZE_API_FROM_URL = "/api/custdocs/initurl";

	//CAP-47777
	public static final String CREATE_LIST_VARS = "/api/checkout/createlistvars";

	public static final String GET_DISTRIBUTION_LIST_RECORD_COUNT = "/api/checkout/getdistlistcount";

	//CAP-47387
	public static final String GET_WORKSHEETS = "/api/checkout/getworksheets";

	//CAP-47389
	public static final String GET_DISTRIBUTION_LIST_ADDRESSES = "/api/checkout/getdistlistaddresses";

	//CAP-47390
	public static final String SAVE_LIST_MAPPINGS = "/api/checkout/savelistmappings";

	// CAP-47388
	public static final String UPLOAD_DIST_LIST = "/api/checkout/uploaddistlist";

	//CAP-47391
	public static final String UPDATE_DIST_LIST_INFO="/api/checkout/updatedistlistinfo";

	// CAP-48274
	public static final String DATE_TO_DESTINATION = "/api/checkout/datetodestination";

	//CAP-48537
	public static final String RETURN_TO_WIDGET_RESULTS_URL="search/";

	// CAP-48534
	public static final String GET_CATALOG_MESSAGES="/api/items/getcatalogmessages";

	// CAP-48582
	public static final String LOAD_SAVE_ORDER_TEMPLATE="/api/ordertemplate/loadsaveordertemplate";

	// CAP-48584
	public static final String SAVE_ORDER_TEMPLATE="/api/ordertemplate/saveordertemplate";

	//CAP-48623
	public static final String GET_TEMPLATE_ORDER_LIST = "/api/ordertemplate/gettemplateorderlist";

 	// CAP-48136
 	public static final String DELETE_ORDER_TEMPLATE="/api/ordertemplate/deletetemplate";

 	// CAP-48716
 	public static final String LOAD_TEMPLATE_ORDER="/api/ordertemplate/useordertemplate";

 	//CAP-49204
 	public static final String  SAVE_EFD_INFORMATION= "/api/checkout/saveefdinformation";

 	// CAP-49205
 	public static final String GET_EFD_STYLEINFO = "/api/checkout/getefdstyleinformation";

 	// CAP-49151
 	public static final String GET_EFD_OPTIONS= "/api/checkout/getefdoptions";

	// CAP-48604
	public static final String SAVE_CUST_DOC_LIST = "/api/custdoc/savelist";

 	//CAP-49610
 	public static final String LOGIN_LINKED_USER = "/api/admin/loginlinkeduser";

 	//CAP-49694
 	public static final String GET_SUGGESTED_ITEMS="/api/items/suggesteditems";

	// CAP-49609
	public static final String GET_LINKED_LOGINS = "/api/admin/linkedlogins";
	// CAP-42231
	public static final String CUST_DOCS_GET_MAPPED_DATA_PAGE_API = "/api/custdocs/getmappedpage";
	// CAP-50006
	public static final String KIT_ADD_COMPONENT = "/api/kittemplate/addcomponent";

	//CAP-50007
	public static final String KIT_REMOVE_COMPONENT = "/api/kittemplate/removecomponent";

	// CAP-49882
	public static final String INIT_KIT_TEMPLATE = "/api/kittemplate/initialize";

	// CAP-50186
	public static final String KIT_ADD_WILD_CARD_COMPONENT = "/api/kittemplate/addwildcardcomponent";

	//CAP-50145
	public static final String KIT_ADD_TO_CART = "/api/kittemplate/addtocart";

	//CAP-50308
	public static final String RELOAD_KIT_TEMPLATE = "/api/kittemplate/loadkiteditor";

	// CAP-50309
	public static final String KIT_CATALOG_BROWSE = "/api/kittemplate/catalogbrowse";

	// CAP-50359
	public static final String RELOAD_KIT_CANCEL = "/api/kittemplate/cancel";
	// CAP-46488
	public static final String CUST_DOCS_INITIALIZE_API_FOR_KIT = "/api/custdocs/initkitcdoe";

	//CAP-50560
	public static final String KIT_CATALOG_SEARCH = "/api/kittemplate/catalogsearch";

	//CAP-46486
	public static final String KIT_EDIT_CUSTOM_DOC = "/api/kittemplate/editcustomdoc";

	// CAP-46489
	public static final String KIT_EDITOR_ROUTING = "kit-templates/build";
	// CAP-46538 - also added stubs for other API search routings
	public static final String CUST_DOCS_IMPRINT_HISTORY_BASIC_SEARCH = "/api/custdocs/basichistorysearch";
	public static final String CUST_DOCS_IMPRINT_HISTORY_ENHANCED_SEARCH = "/api/custdocs/enhhistorysearch";
	public static final String CUST_DOCS_IMPRINT_HISTORY_ADVANCED_SEARCH = "/api/custdocs/advhistorysearch";
}
