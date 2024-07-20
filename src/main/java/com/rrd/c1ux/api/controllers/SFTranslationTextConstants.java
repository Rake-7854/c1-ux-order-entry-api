/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		JIRA#						Description
 * 	--------	-----------		---------		--------------------------------
 *	04/06/23	M Sakthi		CAP-39244        Change Footer API response handling to make and use new translation text values
 *  04/13/23	A Boomker		CAP-39904 		Add constants for validation of fields
 *  04/20/23	Sakthi M		CAP-39245		 Modify Navigation Menu API (the one used) to refactor generation and to make/use new translation text values
 *	04/20/23	A Boomker		CAP-39337		Added CATALOG_SEARCH_NAV_VIEW_NAME
 *	04/25/23    Sakthi M		CAP-39335  		Validation labels in Item Quantity Validation API to make/use translation text values
 *  04/24/23	N Caceres		CAP-39246		Backend updates for translation.
 *  04/28/23	A Boomker		CAP-40206		Added translation for resume saved order errors
 *  04/26/23	C Codina		CAP-39333		 PNA labels in multiple APIs to make/use translation text values
 *  05/10/23	C Codina		CAP-39336		API Change - Success/Error messages in Remove Item from Cart API to make/use translation text values
 *  05/01/23	N Caceres		CAP-39334		Use translation text values for return to labels
 *  05/11/23    Satishkumar A   CAP-39247       API Change - Modify Message Center Response API to make/use new translation text values
 *  05/17/23	A Boomker		CAP-40687		Created constants for simple checkout view names
 *	05/17/23	L De Leon		CAP-40324		Added constants for Save Delivery Information
 *	05/17/23    S Ramachandran	CAP-39973       Added SF Translation Constant for Quick Start Guide Support sub Menu
 *	05/22/23  	Satishkumar A   CAP-40617		API Build - Save Order assuming all data already saved
 *	05/29/23	C Codina		CAP-39338		API Change - Header labels in Order File list API to make/use translation text values
 *	06/07/23	A Boomker		CAP-38154		New Errors converted from CP for C1UX validation
 *	06/08/23	S Ramachandran	CAP-41235		Added Constants for submit order error and success message
 *  06/13/23	A Boomker		CAP-38154		Changed to use the view name FE applied for order info section
 *  06/20/23	A Boomker		CAP-41121		Added SAVED_ORDER_CONTINUE_PROMPT_ERR_DEF
 *	06/22/23	L De Leon		CAP-41373		Added constants for Quick Copy Order
 *	06/27/23	A Salcedo		CAP-41693		Added CLOSE.
 *	06/27/23	N Caceres		CAP-41120		Added constants for uomDesc validation
 *	08/18/23	A Boomker		CAP-42225		Added view name for custom docs
 *	08/30/23	Satishkumar A	CAP-43283		C1UX BE - Routing Information For Justification Section on Review Order Page
 *	08/17/23	N Caceres		CAP-41551		Add constants for validating shipping info errors
 *	09/07/23	L De Leon		CAP-43631		Added constants for PAB Address Delete implementation
 *	09/07/23	S Ramachandran	CAP-43630		Added constant for search PAB Request validation
 *	09/13/23	Satishkumar A	CAP-43685		C1UX - BE - Add translation messages for order submit and routed orders
 *	09/22/23	L De Leon		CAP-44032		Added constants for retrieving payment information
 *	09/28/23	N Caceres		CAP-42806		Added constant for addressLine1 translation
 *	10/06/23	M Sakthi		CAP-44387 		Added constants for Import Address translation
 *	10/31/23	C Codina		CAP-44742		Added constants for carousel messages
 *	11/03/23	N Caceres		CAP-44840		Add constants for saving and retrieving requested ship date
 *	11/07/23	A Boomker		CAP-44427 		Added constants for working proofs
 * 	11/10/23	A Boomker		CAP-44487		Added load user profile constants
 *	11/10/23	L De Leon		CAP-44841		Added constants for demo order labels
 *  11/14/23	AKJT Omisol		CAP-43024		Added constants for imprint history endpoint
 *  11/07/23	Ramachandran	CAP-44961 		Added USPS validation constants for save PAB
 *  11/29/23	Satishkumar A	CAP-45375		C1UX BE - Modify the errors returned from the USPS validation to be translated
 *  11/29/23	M Sakthi		CAP-45481		Added limited search error message
 *  11/29/23	C Codina		CAP-45299		Added constants for OOB
 *	11/27/23	L De Leon		CAP-44467		Added constants for order on behalf
 *	12/13/23	C Codina		CAP-45600		Added constants for Entry Point
 *  12/19/23	Krishna Natarajan	CAP-45596		Added constants for Entry Point Catalog Ordering
 *  12/20/23	C Codina		CAP-45934		Added constants for itemWillBeRemoved
 * 	12/22/23 	Satishkumar A	CAP-45709		C1UX BE - Set OOB Mode for CustomPoint session
 * 	01/09/24	S Ramachandran	CAP-46294		Add order due date to Checkout pages
 *	01/19/24    Satishkumar A	CAP-46380		Added Translation field
 *	01/18/24	S Ramachandran	CAP-46304		Added constant for standard attribute options
 *	01/26/24	L De Leon		CAP-46322		Added constants for attribute filters
 *	02/01/24	S Ramachandran	CAP-46801		Added constants for site attribute validation
 *	02/19/24	M Sakthi		CAP-47063		Added constants for quantity allocation
 *	02/14/24	S Ramachandran	CAP-47145		Added contants for customer and vendor item validation
 *	02/21/24	C Codina		CAP-47086		Added constants for wizard search
 *	02/20/24	N Caceres		CAP-47141		Added constant for order wizard validation
 *	02/26/24	Krishna NatarajanCAP-47337		Added constants to validate pattern after user
 *	02/28/24	S Ramachandran	CAP-47410		Added constants to validate basic profile in self Regs
 *	03/02/24    Satishkumar A	CAP-47592		C1UX BE - Create validation story for Password validation
 *	03/28/24	N Caceres		CAP-47795		Added constants for budget allocation validation
 *	03/21/24	L De Leon		CAP-47969		Added constants for budget allocation error for copy order
 *	03/26/24	S Ramachandran	CAP-48002		Added constants for upload List file validation Error Message
 *	04/01/24	Krishna Natarajan CAP-48376		Added constant for unableRetrieveCartValueMsg
 *	04/01/24	Satishkumar A	CAP-48123		Added constants for dist list not found.
 *	04/05/24	A Boomker		CAP-48729 		Added admin error for an item
 *	04/08/24	S Ramachandran	CAP-48434		Added constants for generic Service Failed error message
 *	04/15/24	N Caceres		CAP-48487		Added constants for expedite disclaimer message
 *	04/23/24	N Caceres		CAP-48821		Added constants for Order Template error message
 *	04/26/24	L De Leon		CAP-48622		Added constant for template ordering label
 *	04/25/24	S Ramachandran	CAP-48889		Added constant for delete order template message
 *	05/03/24	A Boomker		CAP-42226		Added default for unknown error
 *	05/06/24	Krishna Natarajan CAP-49176		Added constants for Order Request Confirmed messages
 *	05/08/24	M Sakthi		CAP-49015		Added fileDeliveryLabel list.
 *	05/15/24	Krishna Natarajan CAP-49176		Added constants for submit order request
 *	05/13/24	S Ramachandran	CAP-49326		Added constant for email style info
 * 	05/16/24	A Boomker		CAP-42228		Added DLIST_NOT_EXIST_DEFAULT_ERR
 *	05/16/24	Krishna Natarajan	CAP-49429	Added a constant for budget alloc and force CC while exceeding budget
 *	05/28/24	A Boomker		CAP-48604		Added TRANS_NM_LIST_MISSING_ERROR and default
 *	05/27/24	L De Leon		CAP-49609		Added constant for linked logins
 *	05/30/24	A Boomker		CAP-42230		Added error handling for list mapping save
 *	05/31/24	Satishkumar A	CAP-49731		Added constant for linked logins error
 *	06/10/24	N Caceres		CAP-50036		Added constant for add kit component errors
 *	06/14/24	S Ramachandran	CAP-50031		Added constant for validation error messages for Kit Template
 *	06/26/24	N Caceres		CAP-50537		Kit catalog browse error
 *	07/02/24	A Boomker		CAP-46489		Added CANNOT_UPDATE_KIT_TEMPLATE_ERROR
 *	07/01/24	S Ramachandran	CAP-50502		Add locations constants for the add buttons when in KitTemplateMode
 *	07/08/24	Satishkumar A	CAP-50737		Added constant for kit search term error
 *	07/05/24	S Ramachandran	CAP-50732		Add	Trannslation Message constant for Validate location code
 *	07/09/24	A Boomker		CAP-46538		Added TRANS_NM_GENERIC_SEARCH_FAILS_ERROR
 */
package com.rrd.c1ux.api.controllers;

import com.wallace.atwinxs.framework.util.TranslationTextConstants;

public class SFTranslationTextConstants{

	 private SFTranslationTextConstants() {
	        // private to ensure constants only
	 }

	//CAP-39244
	public static final String PREFIX_SF="sf.";
	public static final String COOKIE_POLICY_POPUP_HTML="cookiePolicyPopupHtml";
	public static final String COOKIE_POLICY_CONSENT="cookiePolicyConsentBtnTxt";
	public static final String COOKIE_POLICY_DISSENT="cookiePolicyDissentBtnTxt";
	public static final String TERM_LINK_TEXT="termsLinkText";
	public static final String PRIVACY_POLICY_TEXT="privacyLinkText";
	public static final String WCAG_LINK_TEXT="wcagLinkText";
	public static final String WCAG_FULL_TEXT="wcagFullText";
	public static final String VENDOR_COPY_RIGHT_INFO="vendorCopyrightInfo";
	public static final String VENDOR_TRADEMARK_INFO1="vendorTrademarkInfo1";
	public static final String VENDOR_TRADEMARK_INFO2="vendorTrademarkInfo2";
	public static final String VENDOR_ADDRESS_LINE1="vendorAddressLine1";
	public static final String VENDOR_ADDRESS_LINE2="vendorAddressLine2";
	public static final String VENDOR_PHONE="vendorPhone";

	// CAP-39904
	public static final String MUST_NOT_BE_BLANK_ERR = "mustNotBeBlank";
	public static final String MAX_CHARS_ERR = "sf.charsMax";
	public static final String MAX_CHARS_REPLACEMENT_TAG = "{num}";
	public static final String NOT_VALID_ERR = "isNotValidLbl";
	public static final String GENERIC_SAVE_FAILED_ERR = "saveFailed";
	public static final String SELF_ADMIN_VIEW_NAME = "selfAdmin";

	//CAP-45054
	public static final String CHARS_MIN_ERROR = "charsRequired";
	public static final String CHARS_MIN_ERROR_DEF = "{num} character(s) are required.";
	public static final String CRITERIA_FIELD_VALUE = "criteriaFieldValue";
	public static final String NUMERIC_ERROR = "numericError";
	public static final String NUMERIC_ERROR_DEF = "The value must be numeric.";

	// CAP-39973
	public static final String EXACT_CHARS_ERR = "charsRequired";
	public static final String SEARCH_CRITERIA_ERR = "search_criteria_lbl_error";

	//CAP-39245
	public static final String SUB_MENU_ORDER_SEARCH="ordersearch";
	public static final String SUB_MENU_ORDER_SAVED="savedorders";
	public static final String SUB_MENU_MESSAGE_CENTER="messagecenter";
	public static final String SUB_MENU_HELP_CONTACT="helpandcontact";

	//CAP-45600
	public static final String MODULE = "Module";
	public static final String CUST_ITEM_NUM = "custItemNum";
	public static final String ORDER_SEARCH = "OS";

	//CAP-45596
	public static final String CATALOG_ORDERING = "CO";
	public static final String CATALOG="Cat";

	//CAP-39753
	public static final String SUB_MENU_QUICK_START_GUIDE="quickStartGuide";

	// CAP-39337
	public static final String CATALOG_SEARCH_NAV_VIEW_NAME = "catalogSearchNav";

	//CAP-39335
	public static final String VALIDATION_MIN_MSG="minOrderQtyErrMsg";
	public static final String VALIDATION_MAX_MSG="maxOrderQtyErrMsg";
	public static final String INVALID_QTY="shoppingCartQuantityErrorMsg" ;
	public static final String NOT_ORDERABLE="itemCannotOrderUomErrMsg";
	public static final String MULTIPLE_QTY="orderInMultiplesErrMsg";
	//CAP-392246
	public static final String DEFAULT_USER_TEXT_NAME = "sf.defaultUser";
	public static final String SUPPORT_PHONE_TEXT_NAME = "sf.supportPhoneText";
	public static final String USER_EMAIL_TEXT_NAME = "sf.userEmailText";
	public static final String APPLICATION_TEXT_NAME = "storefrontLbl";
	public static final String SC_SUCCESS_MESSAGE_TEXT_NAME = "sf.supportSuccess";
	public static final String SC_ERROR_MESSAGE_TEXT_NAME = "sf.supportError";
	public static final String SUPPORT_PHONE_TEXT = "The number for the Storefront help line is";
	public static final String USER_EMAIL_TEXT = "The email address is";
	public static final String APPLICATION_TEXT = "Storefront";
	public static final String SC_SUCCESS_MESSAGE_TEXT = "Support Contact details loaded successfully.";
	public static final String SC_ERROR_MESSAGE_TEXT = "Error in loading the Support Contact details.";
	// CAP-40206
	public static final String SAVED_ORDER_CURRENT_CART_ERR = "sf.savedOrderCurrentOrderErr";
	public static final String SAVED_ORDER_CANNOT_CONTINUE_ERR = "sf.savedOrderHardStopErr";
	public static final String SAVED_ORDER_CONTINUE_PROMPT_ERR = "sf.savedOrderContinueWarning";
	public static final String SAVED_ORDER_CONTINUE_ORD_NAME_TAG = "{orderName}";
	public static final String SAVED_ORDER_CONTINUE_ITEM_LIST_TAG = "{itemList}";
	// CAP-41121
	public static final String SAVED_ORDER_CONTINUE_PROMPT_ERR_DEF ="We have restored order {orderName} however, the following item(s) are no longer valid and have been removed: {itemList}. Please contact support if you have an questions.";
	public static final String LOAD_ORDER_ERR = "sf.errorLoadOrder";

	// CAP-38157
	public static final String ORDER_SUBMIT_COULD_NOT_COMPLETE_ERR = "sf.orderCouldNotCompleteErr";
	public static final String ORDER_SUBMIT_SUCESS_DIRECT = "sf.orderSubmitSuccessDirect";
	public static final String ORDER_SUBMIT_SUCESS_REALTIME = "sf.orderSubmitSuccessRealtime";
	public static final String ORDER_SUBMIT_WCSS_ORDER_NUMBER_TAG = "{wcssOrderNumber}";
	public static final String ORDER_SUBMIT_SALES_REF_NUMBER_TAG = "{salesRefNum}";
	public static final String ORDER_SUBMIT_CONTACT_EMAIL_ADDR_TAG = "{contactEmailAddr}";

	// CAP-38156
	public static final String LOAD_ORDER_NOTREADY_FOR_REVIEW_ERR = "sf.errorOrderNotReadyForReview";

	//CAP-39333
	public static final String CHECK_PRC_AVL_LBL_VAL = "sf.check_prc_avl_lbl";
	public static final String CHECK_PRC_LBL_VAL = "sf.check_prc_lbl";
	public static final String CHECK_AVL_LBL_VAL = "sf.check_avl_lbl";
	public static final String PRICING_AND_AVAIL_VAL = "Check Current Pricing And Availability";
	public static final String PRICING_VAL = "Check Current Pricing";
	public static final String AVAIL_VAL = "Check Current Availability";

	//CAP-39336
	public static final String SC_RESPONSE_SUCCESS_TEXT = "Item Deleted Successfully";
	public static final String SC_RESPONSE_ERROR_TEXT = "Unable to delete the item at this time";
	public static final String SC_RESPONSE_SUCESS_VAL = "sf.removeItemFromCartSuccess";
	public static final String SC_RESPONSE_ERROR_VAL = "sf.removeItemFromCartFailed";

	//CAP-39334
	public static final String RETURN_TO_RESULTS_TEXT= PREFIX_SF + "returnToResults";
	public static final String RETURN_TO_CATALOG_TEXT= PREFIX_SF + "returnToCatalog";
	public static final String RETURN_TO_RESULTS="Return to Results";
	public static final String RETURN_TO_CATALOG="Return to Catalog";
	//CAP-39247
	public static final String IMPORTANT_LABEL="sf.importantLabel";

	//CAP45934
	public static final String ITEM_WILL_BE_REMOVED_WARNING = "itemwillberemoved";

	//CAP-39338
	public static final String EMAIL_ORDER_FILES_TITLE = "emailsOrderFilesPopUpTitle";
	public static final String EMAILS_LBL_VAL = "emailsLbl";

	// CAP-40687
	// FE didn't use this view name - changing places using this to use the right view name
	public static final String CHECKOUT_FRAMEWORK_SECTION_VIEW_NAME = "checkoutFramework";
	public static final String CHECKOUT_DELIVERY_SECTION_VIEW_NAME = "checkoutDelivery";
	public static final String CHECKOUT_SUMMARY_SECTION_VIEW_NAME = "checkoutSummary";
	public static final String ORDER_ALREADY_SUBMITTED_ERR = "orderalreadysubmitted";

	// CAP-40324
	public static final String DELIVERY_INFO_VIEW_NAME = "deliveryInfo";
	public static final String REQUIRED_FLD_ERR_MSG = "isReqFldLbl";
	public static final String INVALID_FLD_VAL_DEF_ERR_MSG = "is not valid.";
	public static final String REQUIRED_FLD_DEF_ERR_MSG = " is a required field";
	public static final String GENERIC_SAVE_FAILED_DEF_ERR = "Save failed.";
	public static final String BILL_TO_CODE_LBL = "Bill To Code";
	public static final String CORP_NBR_LBL = "Corporate Number";
	public static final String SOLD_TO_NBR_LBL = "Sold To Number";
	public static final String WCSS_SHIP_TO_NBR_LBL = "WCSS Ship To Number";
	public static final String MAX_CHARS_DEF_ERR = "Value cannot be more than ({num}) characters long";
	public static final String EXACT_CHARS_DEF_ERR = "{num} character(s) are required.";
	public static final String SAVE_DELIVERY_INFO_SUCCESS_MSG = "sf.deliveryInfoSavedMsg";
	public static final String SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG = "Delivery information saved.";

	//CAP-40617
		public static final String NO_ITEM_SELECTED_ERR = "no_item_selected_msg";
		public static final String ORDER_NOT_ELIGIBLE_TO_SAVE_ERR = "sf.orderNotEligibleToSave";
		public static final String SAVED_ORDER_NAME_REPLACEMENT_TAG = "{SavedOrderName}";

		// CAP-38154 - new translations for copied CP validation
		public static final String STATE_RESTRICTION_FIELD_TAG_STATE = "{state}";
		public static final String STATE_RESTRICTION_FIELD_TAG_ITEM = "{itemDesc}";
		public static final String STATE_RESTRICTION_WARNING_KIT_MASTER = "sf.badStateKitMasterWarning";
		public static final String STATE_RESTRICTION_NOTIFICATION_KIT_MASTER = "sf.badStateKitMasterError";
		public static final String STATE_RESTRICTION_WARNING_NON_KIT = "sf.badStateNonKitWarning";
		public static final String STATE_RESTRICTION_NOTIFICATION_NON_KIT = "sf.badStateNonKitError";

	// CAP-41373
	public static final String ORDER_IN_PROGRESS_ERR_MSG = "sf.orderInProgressErr";
	public static final String ORDER_NOT_COPIED_ERR_MSG = "sf.orderNotCopiedErr";
	public static final String ORDER_NOT_SUBMITTED_ERR_MSG = "sf.orderNotSubmittedErr";
	public static final String ORDER_NOT_COPIED_AT_THIS_TIME_ERR_MSG = "sf.orderNotCopiedErr2";
	public static final String ORDER_IN_PROGRESS_DEF_ERR_MSG = "Order is already in progress.";
	public static final String ORDER_NOT_COPIED_DEF_ERR_MSG = "Order could not be copied.";
	public static final String ORDER_NOT_SUBMITTED_DEF_ERR_MSG = "Order has not been submitted.";
	public static final String ORDER_NOT_COPIED_AT_THIS_TIME_DEF_ERR_MSG = "Order could not be copied at this time.";

	//CAP-44742
	public static final String CAROUSEL_MESSAGE_ERR_MSG_LBL = "sf.carouselMessageErr";
	public static final String CAROUSEL_MESSAGE_ERR_MSG = "Carousel Message cannot be retrieved";

	//CAP-41693
	public static final String CLOSE = "sf.close";

	// CAP-41120
	public static final String INVALID_UOM = "shoppingCartUomErrorMsg";

	// CAP-41120
	 public static final String CANCEL_ORDER_FAILED = "cancelfailed";

	 //CAP-42562
	 public static final String MUST_NOT_BE_BLANK_KEY="{udflbl}";
	 public static final String MUST_NOT_BE_BLANK_LABEL="sf.mustNotBeBlankLbl";
	 // CAP-42225
	 public static final String CUSTOM_DOCS_VIEW_NAME = "custdocs";

	 //CAP-43283
	 public static final String ORDER_NOT_FOUND_FOR_ROUTING_INFO = "sf.orderNotFoundForRoutingInfo";

	 // CAP-41551
	 public static final String TRANS_NM_INVALID_THIRD_PARTY_ACCT_NUM_ERROR = "Third Party Account Number is not valid for this carrier.";
	 public static final String TRANS_NM_INVALID_SERVICE_LEVEL_ERROR = "The shipping service level that you selected is invalid for a post office box.";
	 public static final String TRANS_NM_ACCTNUM_LENGTH_FEDEX_MSG = "Third Party Account Number must be 9 characters long for this carrier.  Please do not include special characters such as dashes.";
	 public static final String TRANS_NM_ACCTNUM_LENGTH_UPS_MSG = "Third Party Account Number must be 6 characters long for this carrier.  Please do not include special characters such as dashes.";

	// CAP-43631
	public static final String NO_SELECTED_ADDR_ERR_MSG = "noAddrSelMsg";
	public static final String NO_SELECTED_ADDR_DEF_ERR_MSG = "No address is selected. Please select one.";
	public static final String FAILED_TO_DELETE_ADDR_ERR_MSG = "sf.failedToDeleteAddrErrMsg";
	public static final String FAILED_TO_DELETE_ADDR_DEF_ERR_MSG = "One or more addresses failed to be removed.";

	//CAP-43630
	public static final String VALID_SEARCH_CRITERIA_ERR = "LLAdminPopupCritErrMsg";
	public static final String PAB_SHIPTONNAME1 = "sf.shipToName1";
	public static final String PAB_SHIPTOATTN = "sf.shipToAttention";
	public static final String PAB_CITY = "sf.city";
	public static final String PAB_COUNTRY_CD = "sf.country";
	public static final String PAB_STATE_CD = "sf.state";
	public static final String PAB_ZIP = "sf.zip";

	//CAP-43384
	public static final String ROUTING_MESSAGE_REASON_MESG="routingMessageReasonsMsg";
	public static final String ROUTING_ALWAYS_ROUTE_ORDERS="routingAlwaysRouteOrders";
	public static final String ROUTING_SHIPPING_METHOD_MESSAGE="routingShippingMethodMsg";
	public static final String ROUTING_EXCEED_AMOUT_MESSAGE="routingExceedAmountMsg";
	public static final String ITEM_REQUIRE_APPROVAL_LABEL="item_req_approval_lbl";
	public static final String ROUTE_QUANTITY_WARN_LABEL="route_qty_warn_lbl";
	public static final String BREAK_LINE="<BR>";
	public static final String ORDER_AMOUNT_ROUTING_MAP_VALUE="{orderamount}";
	public static final String ORDER_QUANTITY_ROUTING_MAP_VALUE="{orderquantity}";

	//CAP-43685
	public static final String ORDER_ROUTED_LABEL = "order_routed_lbl";
	public static final String SUBMIT_ORDER_LABEL = "submit_order_btn";
	public static final String SUBMIT_ORDER_HEADER_DESC = "default_html_conf_email_header";
	public static final String ORDER_ROUTED_HEADER_DESC = "sf.orderRouted";
	public static final String ORDER_ROUTED_MESSAGE_DESC = "sf.orderRoutedDesc";
	public static final String ORDER_REQUESTED_DESC="sf.orderRequestedDesc";//CAP-49176

	//CAP-43884
	public static final String DASH_NO_SPACE="-";
	public static final String ORDER_QTY_ROUTING_MAP_VALUE="<QTY>";

	// CAP-45299
	public static final String ORDER_ON_BEHALF_MENU_LABEL = "order_on_behalf_menu";
	public static final String ORDER_FOR_ANOTHER_USER = "orderforanotheruser";

	// CAP-44032
	public static final String INVOICE_ACCOUNT_LBL = "invoice_accnt_lbl";
	public static final String INVOICE_ACCOUNT_DEF_LBL = "Invoice Your Account";
	public static final String PAY_BY_NEW_CC_LBL = "pay_credit_card_lbl";
	public static final String PAY_BY_NEW_CC_DEF_LBL = "Pay by New Credit Card";
	public static final String PAY_BY_SAVED_CC_LBL = TranslationTextConstants.TRANS_NM_PAY_BY_CC_MSG;
	public static final String PAY_BY_SAVED_CC_DEF_LBL = "Pay by {existingCardType} ending in {existingCardLastFour}";
	public static final String EXISTING_CARD_TYPE_REPLACE_TXT = "{existingCardType}";
	public static final String EXISTING_CARD_LAST_FOUR_DIGITS_REPLACE_TXT = "{existingCardLastFour}";

	// CAP-42806
	public static final String ADDRESS_LINE_1_DFLT_VAL = "Address Line 1";
	public static final String PAGER_NUMBER_LBL = "PagerNumLbl";
	public static final String PAGER_NUMBER_DFLT_VAL = "Pager Number";

	//CAP-44387
	public static final String FILE_NOT_FOUND="sf.Import_file_not_found";
	public static final String INVALID_FILE_EXTN="sf.import_file_format";
	public static final String INPUT_FIELD_INVALID="sf.Import_add_replace";
	public static final String IMPORT_FAILED="sf.Import_failed";
	public static final String IMPORT_UPLOAD_TYPE="sf.import_type_blank";

	//CAP-44515
	public static final String NO_ORDERS_APPROVAL_LBL="noOrdersApproveLbl";
	// CAP-44840
	public static final String PAST_DATE_ERROR_LBL = "date_past_error_msg";
	public static final String REQUESTED_SHIP_DATE_LBL = "requested_ship_date_lbl";
	public static final String REQUESTED_SHIP_DATE_VAL = "Requested Shipping Date";
	public static final String INVALID_REQ_SHIP_DATE_ERROR_LBL = "invalid_req_shipdate_error";
	public static final String INVALID_DATE_ERROR_LBL = "date_unexpected_char_error";
	public static final String DATE_FORMAT_LBL = "dateformat";
	public static final String DATE_FORMAT_TAG = "{dateFormat}";
	public static final String REP_DATE_FIELD_TAG = "{rep_date_field}";

	// CAP-46294
	public static final String ORDER_DUE_DATE_LBL = "order_due_date_lbl";
	public static final String ORDER_DUE_DATE_VAL = "Order Due Date";
	public static final String INVALID_ORDER_DUE_DATE_ERROR = "invalid_due_date_error";

	// CAP-44427
	public static final String CUST_DOC_PROOF_FAILED = "sf.proofFailed";
	public static final String CUST_DOC_PROOF_PAGE_PREFIX = "sf.proofPagePrefix";

	// CAP-44487
	public static final String TRANS_NM_UI_NO_PRFL_FOUND_DEFAULT = "Profile not found.";
	public static final String TRANS_NM_UI_SEL_PRFL_DEFAULT = "Please select a profile.";

	// CAP-44841
	public static final String SUBMIT_ORDER_TRANS_NAME = "submitOrder";
	public static final String DEMO_SUBMIT_ORDER_LABEL = "submit_demo_order_btn";
	public static final String DEMO_SUBMIT_ORDER_DEF_TXT = "Submit Demo Order";
	public static final String SUBMIT_ORDER_DEF_TXT = "Submit Order";
	public static final String ORDER_ROUTED_DEF_TXT = "Order Routed";
	public static final String SUBMIT_ORDER_HEADER_DEF_DESC = "Thank you for your order.";
	public static final String ORDER_ROUTED_HEADER_DEF_DESC = "Your order has been routed.";

	//CAP-43024
	public static final String TRANS_NM_UI_NO_IMPRINT_HISTORY="sf.noImprintHistory";
	public static final String TRANS_NM_UI_CANNOT_SELECT_IMPRINT_HISTORY="sf.imprintHistorySelectFailed";
	public static final String TRANS_NM_UI_NO_IMPRINT_HISTORY_DEFAULT="No orders available to load.";
	public static final String TRANS_NM_UI_CANNOT_SELECT_IMPRINT_HISTORY_DEFAULT="Selected order cannot be loaded.";

	//CAP-44961
	public static final String TRANS_USPS_ADDRESS_WARNING="addresswarning";

	//CAP-45055
	public static final String INVOICE_NO_LENGH_ERR_MSG="sf.InvoiceNoValidationLbl";
	public static final String ORDER_NAME_LENGH_ERR_MSG="sf.OrderNameValidationLbl";

	//CAP-45057
	public static final String SHIP_NAME_LENGH_ERR_MSG="sf.ShipNameValidationLbl";
	public static final String SHIP_ATTN_LENGH_ERR_MSG="sf.ShiptoAttnValidationLbl";

	//CAP-45375
	public static final String USPS_VALIDATION_MORE_INFO="sf.USPSMoreInformation";
	public static final String USPS_VALIDATION_ADDRESS_NOT_FOUND="sf.USPSAddressNotFound";
	public static final String USPS_VALIDATION_MULTIPLE_ADDRESSES="sf.USPSMultipleAddresses";
	public static final String USPS_VALIDATION_INVALID_ZIPCODE="sf.USPSInvalidZipCode";
	public static final String USPS_VALIDATION_STATE_NOT_VALID="sf.USPSStateNotValid";
	public static final String USPS_VALIDATION_INVALID_CITY="sf.USPSInvalidCity";

	//CAP-45481
	public static final String LIMITED_SEARCH_ERR_MSG="sf.limitedsearcherrmsg";

	// CAP-44467
	public static final String OOB_CANNOT_CHANGE_REQUESTOR_MSG = "oobCannotChangeReqstrMsg";
	public static final String SPECIFY_SEARCH_CRITERIA_MSG = "specifySearchCritMsg";
	public static final String OOB_CANNOT_CHANGE_REQUESTOR_DEFAULT_MSG = "The Order on Behalf Requestor cannot be changed once item(s) are present in the shopping cart.";
	public static final String SPECIFY_SEARCH_CRITERIA_DEFAULT_MSG = "You must specify at least one search criteria.";
	//CAP-45709
	public static final String OOB_LOGINID_LABEL="loginIDLabel_oobo";

	// CAP-44839 - adding reference to existing error for when we really don't want to tell user details in case they're hacking
	public static final String GENERIC_INVALID_REQUEST_ERROR_KEY = "invalidRequestErrMsg"; // this does not have a sf prefix since it's existing CP
	public static final String GENERIC_INVALID_REQUEST_ERROR_DEFAULT = "Invalid Request.";

	//CAP-46380
	public static final String SELF_REGISTRATION_ERROR = "sf.SelfRegError" ;

	//CAP-46304
	public static final String STANDARD_FAVORITE_ATTRIBUTE_LBL = "favoritesStandardAttrLbl";

	//CAP-46322
	public static final String INVALID_ATTR_VAL_SELECTED_MSG = "invalidAtrrValSelMsg";
	public static final String INVALID_ATTR_VAL_SELECTED_DEF_MSG = "Invalid attribute value selected.";
	public static final String INVALID_STD_ATTR_SELECTED_MSG = "invalidStdAtrrSelMsg";
	public static final String INVALID_STD_ATTR_SELECTED_DEF_MSG = "Invalid standard attribute selected.";

	//CAP-46801
	public static final String SITE_ATTR_LBL = "siteattr_lbl";
	public static final String VALS_LBL = "vals_lbl";
	public static final String MUST_HAVE_MAX = "musthavemax_lbl";
	public static final String MUST_HAVE_MIN = "mustHaveMinLbl";
	public static final String AND_MAX_OF = "andmaxof_lbl";
	public static final String UNABLE_TO_SAVE_SITE_ATTR = "attrSaveError";

	//CAP-46960
	public static final String BUDGET_ALLOCATION_MSG = "sf.budgetAllocation";
	public static final String BUDGET_ALLOCATION_DEF_MSG = "Budget Allocation: [budgetAmount]";
	public static final String BUDGET_AMOUNT_REPLACE_TAG = "[budgetAmount]";
	public static final String BUDGET_ALLOCATION_PERIOD_MSG = "sf.budgetAllocationPeriod";
	public static final String BUDGET_ALLOCATION_PERIOD_DEF_MSG = "From [startDate] to [endDate]";
	public static final String START_DATE_REPLACE_TAG = "[startDate]";
	public static final String END_DATE_REPLACE_TAG = "[endDate]";

	//CAP-47063
	public static final String ITEM_ALLOCATION_COUNT_MESSAGE = "sf.itemAllocationMessage";
	public static final String ITEM_QTY_ALLOCATIION_TAG = "{qtyAllocation}";
	public static final String ITEM_QTY_USED_TAG = "{qtyUsed}";
	public static final String ITEM_DATE_RANGE_TAG = "{allocDateRange}";
	public static final String ITEM_UOM_TAG = "{qtyUom}";
	public static final String ITEM_UOM_LBL="sf.uomFullTextEA";

	//CAP-47145
	public static final String CUST_ITEM_NUMBER_LBL	= "custitemnbr";
	public static final String VENDOR_ITEM_NUMBER_LBL	= "vendorTtemNmLbl";
	public static final String INVALID_ITEM_SERVICE_ERR = "sf.invalidItemServCallErrMsg";

	//CAP-47086
	public static final String INLIVAD_WIZARD_SEARCH_LBL = "sf.orderWizardSearchFailed";
	public static final String INLIVAD_WIZARD_SEARCH_VAL = "Order Wizard Search Failed - Invalid data not allowed";
	// CAP-47141
	public static final String ORDER_WIZARD_ERROR_MSG = "sf.orderWizardSearchFailed";

	//CAP-47337
	public static final String VALID_PATTERN_AFTER="sf.valid_pattern_after";
	public static final String INVALID_PATTERN_AFTER="sf.invalid_pattern_after";
	public static final String MAX_LENGTH_PATTERN="sf.maxlength_pattern";

	//CAP-47410
	public static final String DUPLICATE_ERR_MSG ="sf.duplicateErrMsg";

	//CAP-47592
	public static final String PASSWORD_MATCH_ERROR="sf.passwordMatchError";
	public static final String INVALID_PASSWORD_ERROR="sf.invalidPassword";

	//CAP-47629
	public static final String USERID_NO_SPACE = "useridnospace";
	public static final String EXISTING_EMAIL_ERROR_MSG = "existingEmailErr";
	public static final String EMAIL_ADDRESS_PARAM = "{emailAddress}";

	//47657
	public static final String ATTR_NAME="sf.attributes";
	//CAP-47776
	public static final String INVALID_VALUE_ERROR_MSG="sf.invalidValueError";
	//CAP-47795
	public static final String INSUFFICIENT_REMAINING_BUDGET = "sf.insufficientRemainingBudget";
	public static final String EXCEED_BUDGET_WARNING = "exceedBudgetWarnMsg";

	//CAP-47998
	public static final String LIST_ERROR_MSG ="TextMaxSizeError";
	public static final String LIST_ERROR_MSG_DESC ="The value for {varLabel} must be at most {maxChars} characters long. Currently the length of this field is at {currentNum} characters.";
	public static final String LIST_NAME_EMPTY_MSG="list_name_empty_lbl";
	public static final String LIST_NAME_DUPLICATE_MSG="duplicate_dyn_list_msg";

	// CAP-47969
	public static final String BUDGET_ALLOCATION_DEPLETED_ERR_MSG = "sf.budgetDepletedErrMsg";
	public static final String BUDGET_ALLOCATION_DEPLETED_DEF_ERR_MSG = "Your budgeted allocation has been depleted for the current timeframe of [startDate] - [endDate]. Adding items to your cart at this time is not permitted.";

	//CAP-48002
	public static final String DLIST_UPLOAD_FAILED = "uploadFailedMsg";
	public static final String UPLOAD_DLIST_ERR_MSG = "sf.uploadListFileErrMsg";

	//CAP-48376
	public static final String UNABLE_RETRIEVE_CART_VALUE_MSG="unableRetrieveCartValueMsg";

	//CAP-48123
	public static final String DLIST_NOT_EXIST = "sf.listNotExistErrMsg";
	public static final String DLIST_INFO_NULL = "sf.distListInfoNull";
	public static final String DLIST_ID_NULL = "sf.distListIdNull";
	public static final String DLIST_FILENAME_NULL = "sf.distListFileNameNull";

	// CAP-48729 - admin error for an item
	public static final String AUTH_ERROR_ADMIN_ADDING_ITEM = "sf.itemNotOrderableAdmin"; // same value as 'nolists' for CustomPoint, but it applies to cust docs too
	public static final String AUTH_ERROR_ADMIN_ADDING_ITEM_DEFAULT = "This item cannot be ordered due to your Administration settings. Please contact your Account Administrator.";

	// CAP-48497-Added Mandatory and Duplication in lmapper data
	public static final String DUBLICATE_COL_MAPPED = "dupColsMapped";
	public static final String REQUIRED_COL_MAPPING = "mapColErr";

	// CAP-48434
	public static final String GENERIC_SERVICE_FAILED_ERR = "sf.serviceFailed";

	public static final String EXPEDITED_DISCLAIMER_MSG = "expDisclaimerForNotesSec";
	public static final String EXPEDITED_DATE_LABEL_TAG = "{expeditedDateLbl}";
	public static final String EXPEDITED_DATE_LABEL = "expeditedDateLbl";

	//CAP-48745
	public static final String INVALID_ORDER_TEMPLATES = "sf.invalidOrderTemplates";

	// CAP-48821
	public static final String ORDER_TEMPLATE_NAME_MAX = "sf.orderTemplateNameMax";
	public static final String ORDER_TEMPLATE_INVALID_SETTINGS = "sf.orderTemplateSettingsError";
	public static final String ORDER_TEMPLATE_UPDATE_SUCCESS = "sf.orderTemplateUpdateSuccess";
	public static final String ORDER_TEMPLATE_INVALID = "sf.orderTemplateInvalid";
	public static final String ORDER_TEMPLATE_NAME_TAG = "{templateName}";

	// CAP-48622
	public static final String TEMPLATE_ORDERING_MENU_LBL = "template_ordering_menu";
	public static final String TEMPLATE_ORDERING = "templateordering";

	// CAP-48889
	public static final String ORDER_TEMPLATE_NAME_REPLACEMENT_TAG = "{OrderTemplateName}";
	public static final String ORDER_TEMPLATE_DELETE_FAIL = "sf.orderTemplateDeleteFail";
	public static final String ORDER_TEMPLATE_DELETE_SUCCESS = "sf.orderTemplateDeleteSuccess";

	public static final String TEMPLATES_ITEMS_IN_CART_WARNING = "sf.tempItemsInCartWarningMsg";

	// CAP-42226
	public static final String TRANS_NM_UNKNOWN_ERROR_DEFAULT = "An unknown error has occurred.  Please contact support.";

	//CAP-49176
	public static final String ORDERCONFMSG01INSTR = "orderConfMsg01Instr";
	public static final String ORDERCONFMSG02INSTR = "orderConfMsg02Instr";
	public static final String ORDERCONFMSG03INSTR = "orderConfMsg03Instr";
	public static final String ORDERCONFMSG04INSTR = "orderConfMsg04Instr";
	public static final String ORDERCONFMSG05INSTR = "orderConfMsg05Instr";
	public static final String ORDERCONFMSG07INSTR = "orderConfMsg07Instr";

	//CAP-49015
	public static final String EFD_METHOD_LBL = "efd_only_lbl";
	public static final String PRINT_ENABLED_OPTION_LBL = "print_only_lbl";
	public static final String EFD_PRINT_ENABLED_OPTION_LBL = "efd_pref_print_avl_lbl";
	public static final String PRINT_EFD_ENABLED_OPTION_LBL = "print_pref_efd_avl_lbl";
	public static final String EFD_PRINT_OVERRIDE_OPTION_LBL = "efd_only_print_override_lbl";
	public static final String EFD_REQ_PRINT_PREF_OPTION_LBL = "efdReqPrintPrefLbl";

	//CAP-48977
	public static final String EFD_METHOD_DEF_LBL = "Edelivery only";
	public static final String PRINT_ENABLED_OPTION_DEF_LBL = "Print only";
	public static final String EFD_PRINT_ENABLED_OPTION_DEF_LBL = "EFD Preferred/Print Available";
	public static final String PRINT_EFD_ENABLED_OPTION_DEF_LBL = "Print Preferred/EFD Available";
	public static final String EFD_PRINT_OVERRIDE_OPTION_DEF_LBL = "EFD Only/Print Override";
	public static final String EFD_REQ_PRINT_PREF_OPTION_DEF_LBL = "EFD Required/Print Preferred";
	public static final String EDELIVERY_LBL = "eDeliveryLbl";
	public static final String EDELIVERY_DEF_LBL = "eDelivery";
	public static final String PRINT_LBL = "printDeliveryLbl";
	public static final String PRINT_DEF_LBL = "Print Delivery";
	public static final String EDELIVERY_PREF_LBL = "eDeliveryPrefLbl";
	public static final String EDELIVERY_PREF_DEF_LBL = "eDelivery (Preferred)";
	public static final String EDELIVERY_PRINT_LBL = "eDeliveryPrint";
	public static final String EDELIVERY_PRINT_DEF_LBL = "eDelivery + Print";
	public static final String PRINT_PREF_LBL = "printDeliveryPrefLbl";
	public static final String PRINT_PREF_DEF_LBL = "Print Delivery (Preferred)";
	public static final String PRINT_EDELIVERY_LBL = "printeDeliveryLbl";
	public static final String PRINT_EDELIVERY_DEF_LBL = "Print + eDelivery";

	//CAP-49176
	public static final String SF_ORDER_REQUESTED_DESC_LBL="sf.orderRequestedDesclbl";
	//CAP-49326
	public static final String EFD_INVALID_STYLE_INFO_ERR = "sf.invalidStyleInfo";

	//CAP-49311
	public static final String EFD_INVALID_FTP = "sf.efdInvalidFtp";
	public static final String EFD_INVALID_INFO = "sf.efdInvalidInfo";
	public static final String EFD_NOT_ALLOWED = "sf.efdNotAllowed";
	public static final String EFD_INVALID_TYPE = "sf.invalidEfdType";
	public static final String EFD_INVALID_EMAIL = "sf.efdInvalidEmail";
	public static final String EMAIL_MESSAGE = "emailmessage";

	// CAP-42228
	public static final String DLIST_NOT_EXIST_DEFAULT_ERR = "The selected list could not be found on the server. Please select another list or contact the system administrator.";

	//CAP-49429
	public static final String EXCEED_BUDGET_ERR_MSG_FOR_CC="sf.exceedBudgetErrMsgForCC";

	public static final String ERRORS_FOUND = "errorsFound";
	public static final String EFD_LOAD_INFO_ERROR = "sf.efdLoadInfoError";
	public static final String EFD_LOAD_DESTINATION_ERROR = "sf.efdLoadDestinationError";
	public static final String ORDER_ID_TAG = "{orderId}";

	// CAP-48604
	public static final String TRANS_NM_LIST_MISSING_ERROR = "sf.listInfoMissing";
	public static final String TRANS_NM_LIST_MISSING_ERROR_DEFAULT = "List information not found.";
	public static final String TRANS_NM_LIST_SAVE_ERROR = "sf.listSaveUnhandledErr";
	public static final String TRANS_NM_LIST_SAVE_ERROR_DEFAULT = "Unable to save the list as defined. Please try again later.";

	//CAP-49609
	public static final String LINKED_LOGIN_SWITCH_ERR = "linked_login_switch_err";
	public static final String LINKED_LOGIN_SWITCH_DEF_ERR = "Switching to a different user is not allowed once item(s) are present in the shopping cart.";

	//CAP-49731
	public static final String LINKED_LOGIN_INVALID_LOGIN_ERR = "sf.invalidLinkedLogin";
	public static final String LINKED_LOGIN_INVALID_LOGIN_ERR_DEFAULT = "Login Error - Cannot login to the specified user.";

	// CAP-42230
	public static final String TRANS_NM_LIST_NO_COL_MAPPED_ERROR = "sf.noColumnsMapped";
	public static final String TRANS_NM_LIST_NO_COL_MAPPED_ERROR_DEFAULT = "At least one column must be mapped.";

	public static final String TIERED_PRICING_ERR = "Item tiered pricing could not be loaded at this time.";

	// CAP-49882
	public static final String KIT_TEMP_ADDED_ERR = TranslationTextConstants.TRANS_NM_KIT_TEMPLATE_ADDED_ERROR;
	public static final String KIT_TEMP_ADDED_DEF_ERR = "This kit template has already been added to the cart. Please use CustomPoint navigation only on a single browser tab.";
	public static final String KIT_TEMP_DUPLICATE_WARN = "kitdups";
	public static final String KIT_TEMP_DUPLICATE_DEF_WARN = "Duplicate item(s) detected. Please review and remove the duplicate item.";

	// CAP-50036
	public static final String KIT_ADD_COMP_ERROR = "sf.kitAddCompError";
	public static final String KIT_INIT_ERROR = "sf.kitInitError";

	//CAP-50112
	public static final String KIT_REMOVE_COMP_ERROR = "sf.kitRemoveCompError";
	public static final String KIT_INVALID_LINE_NUMBER_ERROR = "sf.kitInvalidLineNumberError";

	//CAP-50031
	public static final String KIT_TEMP_ITEM_NOT_APPROVED_ERR = "sf.ktItemNotApprovedMsg";
	public static final String EARLIEST_POSSIBLE_DATE_REPLACEMENT_TAG = "{earliestPossibleDate}";
	public static final String KIT_TEMP_ITEM_EARLIEST_POSSIBLE_ORDER_ERR = "sf.ktItemEarliestOrderMsg";
	public static final String KIT_TEMP_ITEM_EXPIRED_ORDER_ERR = "sf.ktItemExpiredMsg";
	public static final String ITEM_CANNOT_ADDED2CART = "sf.itemCannotAddedToCart";

	//CAP-50033
	public static final String OF_LABEL_FIELD = "ofLbl";
	public static final String OF_LABEL_FIELD_VALUE = "of";

	// CAP-50537
	public static final String KIT_CATALOG_BROWSE_ERROR = "sf.kitCatalogBrowseError";
	//CAP-50330
	public static final String NOT_VALID_KIT_LINE= "sf.kitInvalidComponent";
	public static final String KIT_NOT_ADDED_CART= "sf.kitNotAddedCart";
	public static final String NEEDS_CUSTDOC_CONTENT= "sf.needCustomDocContent";

	// CAP-46489
	public static final String CANNOT_UPDATE_KIT_TEMPLATE_ERROR = "sf.cannotUpdateKit";
	public static final String CANNOT_UPDATE_KIT_TEMPLATE_ERROR_DEFAULT = "Unable to update the kit. Please try again later.";

	// CAP-50502
	public static final String KIT_LABEL = "add_to_kit_lbl";

	//CAP-50737
	public static final String KIT_SEARCH_TERM_ERROR= "must_enter_srch_criteria";

	// CAP-50732
	public static final String INVALID_LOCATION_ERR_MSG= "sf.invalidLocation" ;

	// CAP-46538
	public static final String TRANS_NM_GENERIC_SEARCH_FAILS_ERROR="sf.searchFailed";
	public static final String TRANS_NM_GENERIC_SEARCH_FAILS_ERROR_DEFAULT="Search could not be completed. Please try again.";

}

