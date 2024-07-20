/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/19/23				A Boomker				CAP-42295					Initial Version
 *	08/28/23				A Boomker				CAP-43022					Add imprint history
 *	08/29/23				A Boomker				CAP-42841					Added sample PDF URL
 *	10/26/23				A Boomker				CAP-44459					Added workingProofLabels
 *	11/07/23				A Boomker				CAP-44427 and CAP-44463		Added changes to getUIPage for working proofs and profile search
 *	01/17/24				A Boomker				CAP-44835					Added UI level flags for upload for lists later
 * 	03/29/24				A Boomker				CAP-46493/CAP-46494			fixes for navigation/bundles
 * 	04/22/24				A Boomker				CAP-46498					Added fields for list pages
 * 	06/03/24				A Boomker				CAP-46501					Added alternateProfiles
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.custompoint.orderentry.customdocs.CustDocStepOption;
import com.rrd.custompoint.orderentry.entity.ItemInstructions;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.lists.util.ManageListsConstants;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXCustDocPageBean", description = "Response Class equivalent to CustDocUIFormBean in CP - info to display a single UI page", type = "object")
public class C1UXCustDocPageBean extends C1UXCustDocBaseResponse {

	@Schema(name ="instructions", description = "List of instructions to display", type = "array")
	private Collection<String> instructions = null;

	@Schema(name = "initializeFailure", description = "Flag indicating cust doc could not initialize properly. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean initializeFailure = false;

	// fields used to control events/handling by the controller
	@Schema(name = "CDOL_TS", description = "Value for when this custom document specification was last saved in the database (change_ts on 84 table)", type = "String", example = "January 1, 1970 12:00:00 AM CST")
	private String CDOL_TS = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="CDOL_TS" name="CDOL_TS" value="January 1, 1970 12:00:00 AM CST"> // custDocOrderLineTimestamp
		// used to prevent re-entering of cust doc using browser buttons when it has already been added to cart
	@Schema(name = "hdnUIStepPageNumber", description = "Page number currently returned for this UI", type = "String", example = "2")
	private String hdnUIStepPageNumber = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" name="hdnStepNumber" value="0"> // NOT USED AS FAR AS I CAN SEE
		// and <input type="hidden" id="hdnUIStepPageNumber" name="hdnUIStepPageNumber" value="0">  // used all over the place with this name exactly (134 references on 8/5/13)
	@Schema(name = "hdnIsDirty", description = "Flag from CP requests indicating fields were changed on this UI page. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean hdnIsDirty = false; // <input type="hidden" id="hdnIsDirty" name="hdnIsDirty" value=""> // used in javascript validation
	@Schema(name = "hdnPDFProof", description = "Flag indicating PDF proof is allowed on this UI. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean hdnPDFProof = false; // <input type="hidden" id="IsPDFProof" name="IsPDFProof"  value="">
	@Schema(name = "eventAction", description = "Flag from CP requests indicating action being performed.", type = "String", example = "RESET",
			allowableValues = {	"NEXT", //UserInterface.UIEvent.NEXT,
					"PREV", //UserInterface.UIEvent.PREVIOUS.toString(),
					"NAVPAGE", //UserInterface.UIEvent.NAVIGATE_PAGES.toString(),
					"UPLOAD", //UserInterface.UIEvent.UPLOAD.toString(), // going to upload and search and such where we leave the flow of the UI temporarily but will return here
					"PSEARCH", //UserInterface.UIEvent.PROFILE_SEARCH.toString(),
					"HSEARCH", //UserInterface.UIEvent.HISTORY_SEARCH.toString(),
					"SEL_PROFILE", //UserInterface.UIEvent.PROFILE_SELECT.toString(),
					"ALT_SEL_PROFILE", //UserInterface.UIEvent.ALTERNATE_PROFILE_SELECT.toString(), //CP-10858
					"KEY_SEL_PROFILE", //UserInterface.UIEvent.KEY_PROFILE_SELECT.toString(), //CP-10858
					"SEL_HISTORY", //UserInterface.UIEvent.HISTORY_SELECT.toString(),
					"ISEARCH", //UserInterface.UIEvent.IMAGE_SEARCH.toString(),
					"WPROOF", //UserInterface.UIEvent.WORKING_PROOF.toString(),
					"PDFPROOF", //UserInterface.UIEvent.PDF_PROOF.toString(),
					"CANCEL", //UserInterface.UIEvent.CANCEL.toString(),
					"CANCELORDER", //UserInterface.UIEvent.CANCEL_ORDER.toString(), // CP-10842
					"SAVEORDER", //UserInterface.UIEvent.SAVE_ORDER.toString(), // CP-10842
					"PUBLISH", //UserInterface.UIEvent.PUBLISH.toString(),
					"REFRESH", //UserInterface.UIEvent.REFRESH.toString(),
					"BACKTOCART", //UserInterface.UIEvent.BACK_TO_CART(), // leaving an order question flow to return all the way to the cart
					"PREVCHECKOUT", //UserInterface.UIEvent.PREVIOUS_CHECKOUT(), // leave an order question flow to go just previous
					"", //UserInterface.UIEvent.STAY(""),
					"SAVE", //UserInterface.UIEvent.SAVE_AND_CONTINUE(), // basically an apply
					"EXIT", //UserInterface.UIEvent.SAVE_AND_EXIT(), // leaves the UI
					"CHG_COLOR", //UserInterface.UIEvent.APPLY_COLOR_CHANGE(), // selects from font color set and applies to current  UI
					"SAVESEARCH", //UserInterface.UIEvent.SAVE_AND_SEARCH(), // CAP-26025
					"REORDERMD" //UserInterface.UIEvent.REORDER_MESSAGE_DOC() // CAP-31455
	})
	private String eventAction = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "hdnUploadControlName", description = "Flag from CP requests indicating field name for upload control.", type = "String")
	private String hdnUploadControlName = AtWinXSConstant.EMPTY_STRING; //	<input type="hidden" id="hdnUploadControlName" name="hdnUploadControlName" value="">
	@Schema(name = "hdnUploadDocumentMax", description = "Value indicating the number of uploads allowed at a maximum for upload control.", type = "String")
	private String hdnUploadDocumentMax = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnUploadDocumentMax" name="hdnUploadDocumentMax" value="0">

	@Schema(name = "hdnFillKeyValue", description = "Flag indicating this UI requires key values to be filled to trigger external source webservice call at UI level. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean hdnFillKeyValue = false; // <input type="hidden" id="hdnFillKeyValue" name="hdnFillKeyValue" value="false">

	@Schema(name = "hdnVarName", description = "Value on CP request indicating the name of a variable triggering the specific request action.", type = "String")
	private String hdnVarName = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnVarName" name="hdnVarName" value=""> // used for dynamic list dependent variables
	@Schema(name = "hdnVarValue", description = "Value on CP request indicating the value of a variable triggering the specific request action.", type = "String")
	private String hdnVarValue = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnVarValue" name="hdnVarValue" value=""> // used for dynamic list dependent variables
	@Schema(name = "hdnVar", description = "Value on CP request indicating the number of a variable triggering the specific request action.", type = "String")
	private String hdnVar = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="hdnVar" name="hdnVar" value=""> // used for dynamic list dependent variables - dependent var name
	@Schema(name = "hdnActiveTextGroupName", description = "Value on CP request indicating the name of a text group variable triggering the specific request action.", type = "String")
	private String hdnActiveTextGroupName = AtWinXSConstant.EMPTY_STRING; //<input type="hidden" id="hdnActiveTextGroupName" name="hdnActiveTextGroupName" value="">

	@Schema(name = "hdnUINumber", description = "Value indicating the number of the template UI on the project.", type = "String")
	private String hdnUINumber = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="<%=ICustomDocsAdminConstants.HDN_UI_NUMBER%>" name="<%=ICustomDocsAdminConstants.HDN_UI_NUMBER%>" value="<%=docDefn.getCdTemplateUserInterfaceVersionVO().getTemplateNum()%>">
	@Schema(name = "hdnUIVersion", description = "Value indicating the version (always 1 right now) for the template UI on the project.", type = "String")
	private String hdnUIVersion = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="<%=ICustomDocsAdminConstants.HDN_UI_VERSION%>" name="<%=ICustomDocsAdminConstants.HDN_UI_VERSION%>" value="<%=docDefn.getCdTemplateUserInterfaceVersionVO().getVersionNum()%>">
	@Schema(name = "hdnProjectID", description = "Value indicating the number of the project.", type = "String")
	private String hdnProjectID = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "hdnProjectName", description = "Value indicating the name of the project.", type = "String")
	private String hdnProjectName = AtWinXSConstant.EMPTY_STRING; // <input type="hidden" id="<%=ICustomDocsAdminConstants.HDN_PROJECT_NAME%>" name="<%=ICustomDocsAdminConstants.HDN_PROJECT_NAME%>" value="<%=docDefn.getCdProjectVO().getProjectFileName()%>">
	@Schema(name = "hdnPageFlexIndicator", description = "Value indicating the project is pageflex. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean hdnPageFlexIndicator = false; // <input type="hidden" id="<%=ICustomDocsAdminConstants.HDN_PAGEFLEX_INDICATOR%>" name="<%=ICustomDocsAdminConstants.HDN_PAGEFLEX_INDICATOR%>" value="<%=oeSessionBean.getCustomDocInProgress().isUiIsPageflex()%>">

	@Schema(name = "starting", description = "Value indicating the UI hasn't saved anything and is freshly initialized. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean starting = false; // this is set upon initialize only, for use by the controller
	@Schema(name = "formName", description = "Value indicating the name of the form to be serialized with fields.", type = "String")
	private String formName = "form1";

	// store display info
	@Schema(name = "headerNavigation", description = "HTML generated for the top of the CP UI page navigation. This is based off the uiStepOptions and the location.", type = "String")
	private String headerNavigation;
	@Schema(name = "footerNavigation", description = "HTML generated for the bottom of the CP UI page navigation. This is based off the uiStepOptions and the location.", type = "String")
	private String footerNavigation;
	@Schema(name = "leftNavigation", description = "HTML generated for the left side of the CP UI page navigation. This is based off the uiStepOptions and the location.", type = "String")
	private String leftNavigation;

	@Schema(name ="uiStepOptions", description = "List of pages for the UI that can be shown for navigation. Includes page number, the current state (PRIOR,SELECTED,FUTURE,FUTURE_NAVIGABLE), and labels for display.", type = "array")
	private List<CustDocStepOption> uiStepOptions = null;
	@Schema(name = "profileSection", description = "HTML generated for the group of profile selection in CP UI page.", type = "String")
	private String profileSection;
	@Schema(name = "alternateProfileSection", description = "HTML generated for the group of alternate profile selection in CP UI page.", type = "String")
	private String alternateProfileSection; //CP-10858
	@Schema(name = "keyProfileSection", description = "HTML generated for the group of key profile selection in CP UI page.", type = "String")
	private String keyProfileSection; //CP-10858
	@Schema(name = "historySection", description = "HTML generated for the group of imprint history selection in CP UI page.", type = "String")
	private String historySection;
	@Schema(name = "variablesSection", description = "HTML generated for the group(s) of variables in CP UI page.", type = "String")
	private String variablesSection;
	@Schema(name = "itemImageURL", description = "URL for the image display in CP UI page. This will be blank for working proofs.", type = "String")
	private String itemImageURL = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "itemDescription", description = "Item description in CP UI page.", type = "String")
	private String itemDescription;
	@Schema(name = "itemNumber", description = "Customer item number for display in CP UI page.", type = "String")
	private String itemNumber;
	@Schema(name = "optionalButtons", description = "HTML generated for the optional buttons display in CP UI page.", type = "String")
	private String optionalButtons;
	@Schema(name = "popupHTML", description = "HTML generated for the popup display in CP UI page.", type = "String")
	private String popupHTML;

	@Schema(name = "showHideJavascript", description = "Javascript generated for controlling group and variable display in CP UI page.", type = "String")
	private String showHideJavascript;
	@Schema(name = "instructionsJavascript", description = "Javascript generated for controlling instructions display in CP UI page.", type = "String")
	private String instructionsJavascript;
	@Schema(name = "dragDropJS", description = "Javascript generated for controlling drag and drop in CP UI page.", type = "String")
	private String dragDropJS;

	@Schema(name = "pageTitle", description = "Value for the current page title in CP UI page.", type = "String")
	private String pageTitle;
	@Schema(name = "itemInstructions", description = "Instructions settings object from CP. Display location can be '' (none), 'T' (top), or 'B' (bottom). Display option can be '' (none), 'T' (text), or 'P' (popup). If display option is text, a group should appear on the cust doc at either the top or bottom. If it's a popup, it occurs over hover over the image.", type = "object")
	private ItemInstructions itemInstructions = null;

	@Schema(name = "externalSource", description = "Value for the full UI level external source URL.", type = "String")
	private String externalSource;
	@Schema(name = "orderFromFile", description = "Boolean returned as a string indicating whether we are in an order from file order.", type = "String")
	private String orderFromFile;
	@Schema(name = "datePattern", description = "Pattern to be used in calendar widget.", type = "String", example="MM/dd/yyyy")
	private String datePattern; // CP-10750 - for date validation
	@Schema(name = "profileLabel", description = "Label used for a profile for the user.", type = "String")
	private String profileLabel; // CP-10750
	@Schema(name = "instanceNameLabel", description = "Label used for instance name for the user.", type = "String")
	private String instanceNameLabel; // CP-10750

	@Schema(name = "selectedId", description = "Flag from CP requests indicating which profile or order ID should populate.", type = "String")
	private String selectedId = AtWinXSConstant.EMPTY_STRING; // this will store a selected profile or history ID to populate the UI

	@Schema(name = "error", description = "Error message if applicable.", type = "String")
	protected String error;
	@Schema(name = "warning", description = "Warning message if applicable.", type = "String")
	protected String warning;


	// CP-10750 CAP-26233
	@Schema(name = "historySearchLayer", description = "HTML generated for the optional imprint history advanced search in CP UI page.", type = "String")
	protected String historySearchLayer = AtWinXSConstant.EMPTY_STRING;

	// CP-11273
	@Schema(name = "skipPreference", description = "Value indicating the user does or does not want to be prompted for whether to save on previous. Skipping the prompting would be true. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean skipPreference = false;
	@Schema(name = "skipPreferenceSaved", description = "Value indicating the user has a saved preference for whether to save on previous. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean skipPreferenceSaved = false;
	@Schema(name = "saveThisSkipPreference", description = "Value indicating the user still needs to save a preference for whether to save on previous. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean saveThisSkipPreference = false;

	@Schema(name = "showHideAltProfSelJS", description = "Javascript generated for controlling alternate profile selection in CP UI page.", type = "String")
	protected String showHideAltProfSelJS; // CAP-3377
	@Schema(name = "skipProofJS", description = "Javascript generated for controlling skip proof handling in CP UI page.", type = "String")
	protected String skipProofJS; // CAP-11103


	// CAP-25569 - cannot pass ui or page
	// values from page
	@Schema(name = "pageValidation", description = "Javascript generated for page validation in CP UI page.", type = "String")
	protected String pageValidation = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "sampleImageURL", description = "Sample image URL for the page in CP UI page.", type = "String")
	protected String sampleImageURL = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "samplePDFURL", description = "Sample PDF URL for the page in CP UI page.", type = "String")
	protected String samplePDFURL = AtWinXSConstant.EMPTY_STRING;
	// values from ui
	@Schema(name = "newRequest", description = "Boolean returned as a string indicating whether we are in a new request flow. Default is false.", type = "String")
	protected String newRequest = Boolean.FALSE.toString();
	@Schema(name = "orderQuestions", description = "Boolean returned as a string indicating whether we are in order questions checkout flow. Default is false.", type = "String")
	protected String orderQuestions = Boolean.FALSE.toString();
	@Schema(name = "variablePage", description = "Boolean returned as a string indicating whether the project allows a varying number of proof pages. Default is false.", type = "String")
	protected String variablePage = Boolean.FALSE.toString();
	@Schema(name = "workingProof", description = "Boolean returned as a string indicating whether this UI shows a working proof. Default is false.", type = "String")
	protected String workingProof = Boolean.FALSE.toString();
	@Schema(name ="workingProofURLs", description = "List of working proof URLs that should be called to display the working proof for this page.", type = "array")
	Collection<String> workingProofURLs = null;
	@Schema(name ="workingProofLabels", description = "List of working proof Labels that should be shown for the working proof URLs for this page.", type = "array")
	Collection<String> workingProofLabels = null;

	@Schema(name = "expandTooltip", description = "Text shown over expand icon.", type = "String")
	protected String expandTooltip = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "collapseTooltip", description = "Text shown over collapse icon.", type = "String")
	protected String collapseTooltip = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "imageEditorWidth", description = "Image cropper width if applicable.", type = "String")
	protected String imageEditorWidth = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "imageEditorHeight", description = "Image cropper height if applicable.", type = "String")
	protected String imageEditorHeight = AtWinXSConstant.EMPTY_STRING;

	// CAP-29609 - move this from request to bean
	@Schema(name = "viewCollapsed", description = "Boolean returned as a string indicating whether this UI should display with the item info collapsed. Default is false.", type = "String")
	protected String viewCollapsed = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "rightSideWidth", description = "Number of pixels the variables section should have for width.", type = "String")
	protected String rightSideWidth = AtWinXSConstant.EMPTY_STRING;

	//CAP-29474
	@Schema(name = "orderDeliveryTypeSection", description = "HTML generated for the optional order delivery section in CP UI page.", type = "String")
	protected String orderDeliveryTypeSection = AtWinXSConstant.EMPTY_STRING;

	// CAP-31099
	@Schema(name = "allKeyVariablesPopulated", description = "Boolean returned as a string indicating whether the UI can call the external source webservice since all key vars are populated. Default is false.", type = "String")
	protected String allKeyVariablesPopulated = Boolean.TRUE.toString();
	//CAP-30048
	@Schema(name = "allowFailedProof", description = "Value indicating the user should be allowed to add to cart even if proofing fails. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean allowFailedProof = false;

	@Schema(name = "imageSearchTermsOfUse", description = "Disclaimer text for image upload.", type = "String")
	protected String imageSearchTermsOfUse = AtWinXSConstant.EMPTY_STRING; // CP-9757

	@Schema(name = "reloadOrdersAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String reloadOrdersAjaxUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "reloadProfilesAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String reloadProfilesAjaxUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "loadImageSearchAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadImageSearchAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CP-9757
	@Schema(name = "loadImageSearchResultsAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadImageSearchResultsAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CP-9757
	@Schema(name = "loadSaveAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadSaveAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CP-8615
	@Schema(name = "downloadInsertUploadAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String downloadInsertUploadAjaxUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "loadWorkingProofAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadWorkingProofAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CP-8615
	@Schema(name = "reloadKeyProfileSelectionsAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String reloadKeyProfileSelectionsAjaxUrl = AtWinXSConstant.EMPTY_STRING; //CP-10858
	@Schema(name = "loadAlternateProfileSelectionsAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadAlternateProfileSelectionsAjaxUrl = AtWinXSConstant.EMPTY_STRING; //CAP-1197
	@Schema(name = "loadAemImagesAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadAemImagesAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CAP-13562
	@Schema(name = "uploadAemImagesAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String uploadAemImagesAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CAP-13780
	@Schema(name = "saveImageAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String saveImageAjaxUrl = AtWinXSConstant.EMPTY_STRING; // CAP-13780
	@Schema(name = "deleteTempImageUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String deleteTempImageUrl = AtWinXSConstant.EMPTY_STRING; // CAP-17049
	@Schema(name = "loadHistoryAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadHistoryAjaxUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "loadMoreDataAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadMoreDataAjaxUrl = AtWinXSConstant.EMPTY_STRING;
	// CAP-32631
	@Schema(name = "loadBrandfolderImagesAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String loadBrandfolderImagesAjaxUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "uploadBrandfolderImagesAjaxUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String uploadBrandfolderImagesAjaxUrl = AtWinXSConstant.EMPTY_STRING;

	// CAP-29897
	@Schema(name = "ajaxLoadCustomSearchUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String ajaxLoadCustomSearchUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "ajaxPerformCustomSearchUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String ajaxPerformCustomSearchUrl = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "ajaxApplyCustomSearchUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String ajaxApplyCustomSearchUrl = AtWinXSConstant.EMPTY_STRING;
	//CAP-32298
	@Schema(name = "ajaxRemoveSelectedValuesUrl", description = "URL for CP ajax call - not applicable in C1UX", type = "String")
	protected String ajaxRemoveSelectedValuesUrl = AtWinXSConstant.EMPTY_STRING;

	@Schema(name ="groups", description = "List of groups to display on this page in the sequence of display", type = "array")
	protected List<C1UXCustDocGroupBean> groups = new ArrayList<>();

	@Schema(name = "javascriptFilePath", description = "Browser file path for unique javascript file for the session. This file is rewritten with every call to this service.", type = "String")
	protected String javascriptFilePath = null;

	@Schema(name = "pageAllowsSave", description = "Flag indicating cust doc can do the actions of SAVE and SAVEEXIT. This depends on several things, including entry point and the type of page. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean pageAllowsSave = false;
	@Schema(name = "pageAllowsBackButton", description = "Flag indicating cust doc page can navigate to a prior page. This depends on the UI itself and which page is the current page. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean pageAllowsBackButton = false;
	@Schema(name = "pageAllowsAddToCartButton", description = "Flag indicating cust doc page can add to the cart from this page. This depends on the UI itself and which page is the current page. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean pageAllowsAddToCartButton = false;

	@Schema(name="custDocpromptValue", description = "Request field submitted on the form for ui.jsp read", type = "boolean", allowableValues = {"false", "true"})
	protected boolean custDocpromptValue = false;
	@Schema(name = "pageLoadedDirty", description = "Flag from CP requests indicating fields were changed on this UI page. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean pageLoadedDirty = false;  // value will be same as hdnIsDirty
	@Schema(name="hdnHasChanged", description = "Request field submitted on the form for ui.jsp read", type = "String")
	protected String hdnHasChanged = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "pcijspname", description = "JSP for CP page - not applicable in C1UX", type = "String")
	protected String pcijspname = "ui.jsp";
	@Schema(name = "navPage", description = "Request field submitted on the form for ui.jsp read", type = "String")
	protected String navPage = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "eventSeqNum", description = "Request field submitted on the form for ui.jsp read", type = "String")
	protected String eventSeqNum = AtWinXSConstant.EMPTY_STRING;

	@Schema(name="hiddenFieldList", description="Array of hidden field names for the form1", type="array")
	protected Collection<String> hiddenFieldList = null;

	@Schema(name = "hiddenTransactionID", description = "Transaction ID for main proof - used on the proof page only!", type = "String")
	protected String hiddenTransactionID = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "imageProofUrl", description = "Url to be used for getting image proofs. This should include the transaction ID but should not include the page number. That should be the LAST parameter that can be appended by the FE - used on the proof page only!", type = "String")
	protected String imageProofUrl = AtWinXSConstant.EMPTY_STRING;

	@Schema(name = "lastProofPageNbr", description = "Number of the last proof page ready for main proof - used on the proof page only!", type = "number")
	protected int lastProofPageNbr = 0;
	@Schema(name="agreeChecked", description = "Hidden field on proof page indicating that this proof has had the terms checkbox checked. Used on Items with multiple proofs only.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean agreeChecked = false;

	@Schema(name="pagesToProof", description="Array of proof pages selected for proofing in a non-variable page UI - used on the proof page only! This is NOT a required setting. If these are not saved, then the typical handling should be used.", type="array")
	protected List<Integer> pagesToProof = null;
	@Schema(name="pdfProofAvailable", description = "Field indicating the UI has a job for PDF proofing. This means that the PDF proof button should be shown on the proof page only. This does not need to be a hidden field and will be used on the proof page only. Default is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean pdfProofAvailable = false;
	@Schema(name="jellyVisionProofAvailable", description = "Field indicating the UI has a job for jellyVision proofing. This means that the Other OE Resources (Jellyvision) proof button should be shown on the proof page only. This does not need to be a hidden field and will be used on the proof page only. Default is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean jellyVisionProofAvailable = false;

	@Schema(name="uiListMap", description="Map of lists used by variables. For each mapped pair, key is listTypeCode_listDataType_listId and will be stored on variables using it as the listKey. Value is a C1UXUIList which will contain a Collection of list options in order.", type="array")
	protected Map<String, C1UXUIList> uiListMap = new HashMap<>();

	@Schema(name="editing", description = "Flag indicating that the user is in the UI on an edit and not doing the initial add to cart.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean editing = false;

	@Schema(name ="translationMap", description = "Messages from \"custdocs\" translation file will load here.", type = "string",  example="\"translation\": { \"nameLabel\": \"Your Name\"}")
	private Map<String, String> translationMap;

	@Schema(name = "imprintHistory", description = "Imprint history section that will only be populated if this is the first page of the UI and if allowed by admin.", type = "object")
	private C1UXImprintHistoryOptions imprintHistory;

	@Schema(name = "userProfileSearch", description = "User profile search section that will only be populated if this is the first page of the UI and if allowed by admin.", type = "object")
	private C1UXUserProfileSearchOptions userProfileSearch;

	@Schema(name = "addRowButtonText", description = "Applicable to grid groups only - Button label for add that depends on item classification back-end.", type = "String")
	private String addRowButtonText = "Add Row";

	boolean singleListOnly = false; // only used for display ONLY of Merrill and John Hancock CP customizations
	boolean imageMergeZipFlag = false; // only used to hide display ONLY of list privacy settings for image merge upload lists - peppermint

	@Schema(name = "componentDescription", description = "Bundle Component Item description currently being proofed", type = "String")
	private String componentDescription = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "truncatedItemInstructions", description = "This contains the truncated version of item instructions text if that is necessary due to size constraints on the proof page.", type = "String")
	private String truncatedItemInstructions = AtWinXSConstant.EMPTY_STRING;

	@Schema(name="reviewOnly", description = "Flag indicating that the user is in the UI as a review only approver and is not allowed to change data or list settings.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean reviewOnly = false;
	@Schema(name = "selectedListId", description = "Number of the selected list for this order", type = "number")
	protected int selectedListId = AtWinXSConstant.INVALID_ID;
	@Schema(name = "selectedListType", description = "Type of list selected for use. Default is none (empty string).", type = "String", allowableValues = {
			AtWinXSConstant.EMPTY_STRING,
			ManageListsConstants.LIST_SOURCE_UPLOAD, // U
			ManageListsConstants.LIST_SOURCE_LIST_FEED, // F
			ManageListsConstants.LIST_SOURCE_SALESFORCE, // S
			ManageListsConstants.LIST_SOURCE_SALESFORCE_SINGLE, //  X
			ManageListsConstants.LIST_SOURCE_DATA_PIPE, // P
			ManageListsConstants.LIST_SOURCE_DATA_CUSTOM_LIST, // C
			ManageListsConstants.LIST_SOURCE_IMAGE_ZIP_MERGE, // Z
			ManageListsConstants.LIST_SOURCE_WEBSERVICE // W //CAP-32579

	})
	protected String selectedListType = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "currentlySelectedList", description = "This object contains information about the currently selected list, if there is a list selected.", type = "object")
	protected C1uxCustDocListDetails currentlySelectedList = null;
	@Schema(name="listMapped", description = "Flag indicating that there is a currently selected list in session and it is mapped", type = "boolean", allowableValues = {"false", "true"})
	protected boolean listMapped = false;
	@Schema(name="listDataSaved", description = "Flag indicating that there is a currently selected list in session, it is mapped, and the row of data are saved to the list data table.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean listDataSaved = false;
	@Schema(name="singleUseLists", description = "Flag indicating that the user should apply the customization that they are only allowed to use the currently selected list or upload a new list. They cannot see other lists or allow the list selection to change.", type = "boolean", allowableValues = {"false", "true"})
	boolean singleUseLists = false; // only used for display ONLY of Merrill and John Hancock CP customizations
	@Schema(name="allowListSecuritySelection", description = "Flag indicating that the user can select between private or shared lists. If false, the user can only have private lists.", type = "boolean", allowableValues = {"false", "true"})
	boolean allowListSecuritySelection = false; // only used to hide display ONLY of list privacy settings for upload lists
	@Schema(name = "validRecordCount", description = "Number of validated records in the list that passed validation.", type = "number")
	private int validRecordCount = 0;
	@Schema(name = "invalidRecordCount", description = "Number of validated records in the list that failed validation.", type = "number")
	private int invalidRecordCount = 0;
	@Schema(name = "dataRecordsPerPage", description = "Number of record rows of mapped list data to show per page. This constant is the same whether invalid or valid.", type = "number")
	private int dataRecordsPerPage = ModelConstants.NUM_MAPPED_DATA_RECORDS_PER_PAGE_DISPLAYED;
	@Schema(name = "initialRecords", description = "Populated only for the list data page, this object will contain the info to display one page of records. It will default to valid records, but if there are no valid records, it will load up invalid records instead.", type = "object")
	protected C1UXCustDocMappedDataPage initialRecords = null;

	@Schema(name="alternateProfiles", description="Array of alternate profile options to display in an alternate profile section at the top of the page if not empty.", type="array")
	protected List<C1UXAlternateProfileOptions> alternateProfiles = new ArrayList<>();


	public Collection<String> getInstructions() {
		return instructions;
	}
	public void setInstructions(Collection<String> instructions) {
		this.instructions = instructions;
	}
	public boolean isInitializeFailure() {
		return initializeFailure;
	}
	public void setInitializeFailure(boolean initializeFailure) {
		this.initializeFailure = initializeFailure;
	}
	public String getCDOL_TS() {
		return CDOL_TS;
	}
	public void setCDOL_TS(String cDOL_TS) {
		CDOL_TS = cDOL_TS;
	}
	public String getHdnUIStepPageNumber() {
		return hdnUIStepPageNumber;
	}
	public void setHdnUIStepPageNumber(String hdnUIStepPageNumber) {
		this.hdnUIStepPageNumber = hdnUIStepPageNumber;
	}
	public boolean isHdnIsDirty() {
		return hdnIsDirty;
	}
	public void setHdnIsDirty(boolean hdnIsDirty) {
		this.hdnIsDirty = hdnIsDirty;
	}
	public boolean isHdnPDFProof() {
		return hdnPDFProof;
	}
	public void setHdnPDFProof(boolean hdnPDFProof) {
		this.hdnPDFProof = hdnPDFProof;
	}
	public String getEventAction() {
		return eventAction;
	}
	public void setEventAction(String eventAction) {
		this.eventAction = eventAction;
	}
	public String getHdnUploadControlName() {
		return hdnUploadControlName;
	}
	public void setHdnUploadControlName(String hdnUploadControlName) {
		this.hdnUploadControlName = hdnUploadControlName;
	}
	public String getHdnUploadDocumentMax() {
		return hdnUploadDocumentMax;
	}
	public void setHdnUploadDocumentMax(String hdnUploadDocumentMax) {
		this.hdnUploadDocumentMax = hdnUploadDocumentMax;
	}
	public boolean isHdnFillKeyValue() {
		return hdnFillKeyValue;
	}
	public void setHdnFillKeyValue(boolean hdnFillKeyValue) {
		this.hdnFillKeyValue = hdnFillKeyValue;
	}
	public String getHdnVarName() {
		return hdnVarName;
	}
	public void setHdnVarName(String hdnVarName) {
		this.hdnVarName = hdnVarName;
	}
	public String getHdnVarValue() {
		return hdnVarValue;
	}
	public void setHdnVarValue(String hdnVarValue) {
		this.hdnVarValue = hdnVarValue;
	}
	public String getHdnVar() {
		return hdnVar;
	}
	public void setHdnVar(String hdnVar) {
		this.hdnVar = hdnVar;
	}
	public String getHdnActiveTextGroupName() {
		return hdnActiveTextGroupName;
	}
	public void setHdnActiveTextGroupName(String hdnActiveTextGroupName) {
		this.hdnActiveTextGroupName = hdnActiveTextGroupName;
	}
	public String getHdnUINumber() {
		return hdnUINumber;
	}
	public void setHdnUINumber(String hdnUINumber) {
		this.hdnUINumber = hdnUINumber;
	}
	public String getHdnUIVersion() {
		return hdnUIVersion;
	}
	public void setHdnUIVersion(String hdnUIVersion) {
		this.hdnUIVersion = hdnUIVersion;
	}
	public String getHdnProjectID() {
		return hdnProjectID;
	}
	public void setHdnProjectID(String hdnProjectID) {
		this.hdnProjectID = hdnProjectID;
	}
	public String getHdnProjectName() {
		return hdnProjectName;
	}
	public void setHdnProjectName(String hdnProjectName) {
		this.hdnProjectName = hdnProjectName;
	}
	public boolean isHdnPageFlexIndicator() {
		return hdnPageFlexIndicator;
	}
	public void setHdnPageFlexIndicator(boolean hdnPageFlexIndicator) {
		this.hdnPageFlexIndicator = hdnPageFlexIndicator;
	}
	public boolean isStarting() {
		return starting;
	}
	public void setStarting(boolean starting) {
		this.starting = starting;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getHeaderNavigation() {
		return headerNavigation;
	}
	public void setHeaderNavigation(String headerNavigation) {
		this.headerNavigation = headerNavigation;
	}
	public String getFooterNavigation() {
		return footerNavigation;
	}
	public void setFooterNavigation(String footerNavigation) {
		this.footerNavigation = footerNavigation;
	}
	public String getLeftNavigation() {
		return leftNavigation;
	}
	public void setLeftNavigation(String leftNavigation) {
		this.leftNavigation = leftNavigation;
	}
	public String getProfileSection() {
		return profileSection;
	}
	public void setProfileSection(String profileSection) {
		this.profileSection = profileSection;
	}
	public String getAlternateProfileSection() {
		return alternateProfileSection;
	}
	public void setAlternateProfileSection(String alternateProfileSection) {
		this.alternateProfileSection = alternateProfileSection;
	}
	public String getKeyProfileSection() {
		return keyProfileSection;
	}
	public void setKeyProfileSection(String keyProfileSection) {
		this.keyProfileSection = keyProfileSection;
	}
	public String getHistorySection() {
		return historySection;
	}
	public void setHistorySection(String historySection) {
		this.historySection = historySection;
	}
	public String getVariablesSection() {
		return variablesSection;
	}
	public void setVariablesSection(String variablesSection) {
		this.variablesSection = variablesSection;
	}
	public String getOptionalButtons() {
		return optionalButtons;
	}
	public void setOptionalButtons(String optionalButtons) {
		this.optionalButtons = optionalButtons;
	}
	public String getPopupHTML() {
		return popupHTML;
	}
	public void setPopupHTML(String popupHTML) {
		this.popupHTML = popupHTML;
	}
	public String getShowHideJavascript() {
		return showHideJavascript;
	}
	public void setShowHideJavascript(String showHideJavascript) {
		this.showHideJavascript = showHideJavascript;
	}
	public String getInstructionsJavascript() {
		return instructionsJavascript;
	}
	public void setInstructionsJavascript(String instructionsJavascript) {
		this.instructionsJavascript = instructionsJavascript;
	}
	public String getDragDropJS() {
		return dragDropJS;
	}
	public void setDragDropJS(String dragDropJS) {
		this.dragDropJS = dragDropJS;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public String getExternalSource() {
		return externalSource;
	}
	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}
	public String getOrderFromFile() {
		return orderFromFile;
	}
	public void setOrderFromFile(String orderFromFile) {
		this.orderFromFile = orderFromFile;
	}
	public String getDatePattern() {
		return datePattern;
	}
	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
	public String getProfileLabel() {
		return profileLabel;
	}
	public void setProfileLabel(String profileLabel) {
		this.profileLabel = profileLabel;
	}
	public String getInstanceNameLabel() {
		return instanceNameLabel;
	}
	public void setInstanceNameLabel(String instanceNameLabel) {
		this.instanceNameLabel = instanceNameLabel;
	}
	public String getSelectedId() {
		return selectedId;
	}
	public void setSelectedId(String selectedId) {
		this.selectedId = selectedId;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getWarning() {
		return warning;
	}
	public void setWarning(String warning) {
		this.warning = warning;
	}
	public String getHistorySearchLayer() {
		return historySearchLayer;
	}
	public void setHistorySearchLayer(String historySearchLayer) {
		this.historySearchLayer = historySearchLayer;
	}
	public boolean isSkipPreference() {
		return skipPreference;
	}
	public void setSkipPreference(boolean skipPreference) {
		this.skipPreference = skipPreference;
	}
	public boolean isSkipPreferenceSaved() {
		return skipPreferenceSaved;
	}
	public void setSkipPreferenceSaved(boolean skipPreferenceSaved) {
		this.skipPreferenceSaved = skipPreferenceSaved;
	}
	public boolean isSaveThisSkipPreference() {
		return saveThisSkipPreference;
	}
	public void setSaveThisSkipPreference(boolean saveThisSkipPreference) {
		this.saveThisSkipPreference = saveThisSkipPreference;
	}
	public String getShowHideAltProfSelJS() {
		return showHideAltProfSelJS;
	}
	public void setShowHideAltProfSelJS(String showHideAltProfSelJS) {
		this.showHideAltProfSelJS = showHideAltProfSelJS;
	}
	public String getSkipProofJS() {
		return skipProofJS;
	}
	public void setSkipProofJS(String skipProofJS) {
		this.skipProofJS = skipProofJS;
	}
	public String getPageValidation() {
		return pageValidation;
	}
	public void setPageValidation(String pageValidation) {
		this.pageValidation = pageValidation;
	}
	public String getSampleImageURL() {
		return sampleImageURL;
	}
	public void setSampleImageURL(String sampleImageURL) {
		this.sampleImageURL = sampleImageURL;
	}
	public String getNewRequest() {
		return newRequest;
	}
	public void setNewRequest(String newRequest) {
		this.newRequest = newRequest;
	}
	public String getOrderQuestions() {
		return orderQuestions;
	}
	public void setOrderQuestions(String orderQuestions) {
		this.orderQuestions = orderQuestions;
	}
	public String getVariablePage() {
		return variablePage;
	}
	public void setVariablePage(String variablePage) {
		this.variablePage = variablePage;
	}
	public String getWorkingProof() {
		return workingProof;
	}
	public void setWorkingProof(String workingProof) {
		this.workingProof = workingProof;
	}
	public String getExpandTooltip() {
		return expandTooltip;
	}
	public void setExpandTooltip(String expandTooltip) {
		this.expandTooltip = expandTooltip;
	}
	public String getCollapseTooltip() {
		return collapseTooltip;
	}
	public void setCollapseTooltip(String collapseTooltip) {
		this.collapseTooltip = collapseTooltip;
	}
	public String getImageEditorWidth() {
		return imageEditorWidth;
	}
	public void setImageEditorWidth(String imageEditorWidth) {
		this.imageEditorWidth = imageEditorWidth;
	}
	public String getImageEditorHeight() {
		return imageEditorHeight;
	}
	public void setImageEditorHeight(String imageEditorHeight) {
		this.imageEditorHeight = imageEditorHeight;
	}
	public String getViewCollapsed() {
		return viewCollapsed;
	}
	public void setViewCollapsed(String viewCollapsed) {
		this.viewCollapsed = viewCollapsed;
	}
	public String getRightSideWidth() {
		return rightSideWidth;
	}
	public void setRightSideWidth(String rightSideWidth) {
		this.rightSideWidth = rightSideWidth;
	}
	public String getOrderDeliveryTypeSection() {
		return orderDeliveryTypeSection;
	}
	public void setOrderDeliveryTypeSection(String orderDeliveryTypeSection) {
		this.orderDeliveryTypeSection = orderDeliveryTypeSection;
	}
	public String getAllKeyVariablesPopulated() {
		return allKeyVariablesPopulated;
	}
	public void setAllKeyVariablesPopulated(String allKeyVariablesPopulated) {
		this.allKeyVariablesPopulated = allKeyVariablesPopulated;
	}
	public boolean isAllowFailedProof() {
		return allowFailedProof;
	}
	public void setAllowFailedProof(boolean allowFailedProof) {
		this.allowFailedProof = allowFailedProof;
	}
	public String getImageSearchTermsOfUse() {
		return imageSearchTermsOfUse;
	}
	public void setImageSearchTermsOfUse(String imageSearchTermsOfUse) {
		this.imageSearchTermsOfUse = imageSearchTermsOfUse;
	}
	public List<C1UXCustDocGroupBean> getGroups() {
		return groups;
	}
	public void setGroups(List<C1UXCustDocGroupBean> groups) {
		this.groups = groups;
	}

	public void setJavascriptFilePath(String string) {
		this.javascriptFilePath = string;
	}
	public String getJavascriptFilePath() {
		return javascriptFilePath;
	}
	public String getItemImageURL() {
		return itemImageURL;
	}
	public void setItemImageURL(String itemImageURL) {
		this.itemImageURL = itemImageURL;
	}
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	public String getItemNumber() {
		return itemNumber;
	}
	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}
	public ItemInstructions getItemInstructions() {
		return itemInstructions;
	}
	public void setItemInstructions(ItemInstructions itemInstructions) {
		this.itemInstructions = itemInstructions;
	}
	public Collection<String> getWorkingProofURLs() {
		return workingProofURLs;
	}
	public void setWorkingProofURLs(Collection<String> workingProofURLs) {
		this.workingProofURLs = workingProofURLs;
	}
	public List<CustDocStepOption> getUiStepOptions() {
		return uiStepOptions;
	}
	public void setUiStepOptions(List<CustDocStepOption> uiStepOptions) {
		this.uiStepOptions = uiStepOptions;
	}
	public boolean isPageAllowsSave() {
		return pageAllowsSave;
	}
	public void setPageAllowsSave(boolean pageAllowsSave) {
		this.pageAllowsSave = pageAllowsSave;
	}
	public boolean isPageAllowsBackButton() {
		return pageAllowsBackButton;
	}
	public void setPageAllowsBackButton(boolean pageAllowsBackButton) {
		this.pageAllowsBackButton = pageAllowsBackButton;
	}
	public Collection<String> getHiddenFieldList() {
		return hiddenFieldList;
	}
	public void setHiddenFieldList(Collection<String> hiddenFieldList) {
		this.hiddenFieldList = hiddenFieldList;
	}
	public boolean isCustDocpromptValue() {
		return custDocpromptValue;
	}
	public void setCustDocpromptValue(boolean custDocpromptValue) {
		this.custDocpromptValue = custDocpromptValue;
	}
	public boolean isPageLoadedDirty() {
		return pageLoadedDirty;
	}
	public void setPageLoadedDirty(boolean pageLoadedDirty) {
		this.pageLoadedDirty = pageLoadedDirty;
	}
	public String getHdnHasChanged() {
		return hdnHasChanged;
	}
	public void setHdnHasChanged(String hasChanged) {
		this.hdnHasChanged = hasChanged;
	}
	public String getPcijspname() {
		return pcijspname;
	}
	public void setPcijspname(String pcijspname) {
		this.pcijspname = pcijspname;
	}
	public String getNavPage() {
		return navPage;
	}
	public void setNavPage(String navPage) {
		this.navPage = navPage;
	}
	public String getEventSeqNum() {
		return eventSeqNum;
	}
	public void setEventSeqNum(String eventSeqNum) {
		this.eventSeqNum = eventSeqNum;
	}
	public String getHiddenTransactionID() {
		return hiddenTransactionID;
	}
	public void setHiddenTransactionID(String hiddenTransactionID) {
		this.hiddenTransactionID = hiddenTransactionID;
	}
	public int getLastProofPageNbr() {
		return lastProofPageNbr;
	}
	public void setLastProofPageNbr(int lastProofPageNbr) {
		this.lastProofPageNbr = lastProofPageNbr;
	}
	public boolean isAgreeChecked() {
		return agreeChecked;
	}
	public void setAgreeChecked(boolean agreeChecked) {
		this.agreeChecked = agreeChecked;
	}
	public boolean isPageAllowsAddToCartButton() {
		return pageAllowsAddToCartButton;
	}
	public void setPageAllowsAddToCartButton(boolean pageAllowsAddToCartButton) {
		this.pageAllowsAddToCartButton = pageAllowsAddToCartButton;
	}
	public String getImageProofUrl() {
		return imageProofUrl;
	}
	public void setImageProofUrl(String imageProofUrl) {
		this.imageProofUrl = imageProofUrl;
	}
	public List<Integer> getPagesToProof() {
		return pagesToProof;
	}
	public void setPagesToProof(List<Integer> pagesToProof) {
		this.pagesToProof = pagesToProof;
	}
	public boolean isPdfProofAvailable() {
		return pdfProofAvailable;
	}
	public void setPdfProofAvailable(boolean pdfProofAvailable) {
		this.pdfProofAvailable = pdfProofAvailable;
	}
	public boolean isJellyVisionProofAvailable() {
		return jellyVisionProofAvailable;
	}
	public void setJellyVisionProofAvailable(boolean jellyVisionProofAvailable) {
		this.jellyVisionProofAvailable = jellyVisionProofAvailable;
	}
	public Map<String, C1UXUIList> getUiListMap() {
		return uiListMap;
	}
	public void setUiListMap(Map<String, C1UXUIList> uiListMap) {
		this.uiListMap = uiListMap;
	}
	public boolean isEditing() {
		return editing;
	}
	public void setEditing(boolean editing) {
		this.editing = editing;
	}
	public Map<String, String> getTranslationMap() {
		return translationMap;
	}
	public void setTranslationMap(Map<String, String> translationMap) {
		this.translationMap = translationMap;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if ((obj == null) || (obj.getClass() == this.getClass())) {
			return false;
		}

		C1UXCustDocPageBean fobj = (C1UXCustDocPageBean) obj;
		return ((super.equals(fobj))
				&& (this.getJavascriptFilePath().equals(fobj.getJavascriptFilePath())));
	}

	@Override
	public int hashCode() {
		return super.hashCode();

	}
	public C1UXImprintHistoryOptions getImprintHistory() {
		return imprintHistory;
	}
	public void setImprintHistory(C1UXImprintHistoryOptions imprintHistory) {
		this.imprintHistory = imprintHistory;
	}
	public String getSamplePDFURL() {
		return samplePDFURL;
	}
	public void setSamplePDFURL(String samplePDFURL) {
		this.samplePDFURL = samplePDFURL;
	}
	public Collection<String> getWorkingProofLabels() {
		return workingProofLabels;
	}
	public void setWorkingProofLabels(Collection<String> workingProofLabels) {
		this.workingProofLabels = workingProofLabels;
	}
	public C1UXUserProfileSearchOptions getUserProfileSearch() {
		return userProfileSearch;
	}
	public void setUserProfileSearch(C1UXUserProfileSearchOptions userProfileSearch) {
		this.userProfileSearch = userProfileSearch;
	}
	public boolean isSingleListOnly() {
		return singleListOnly;
	}
	public void setSingleListOnly(boolean singleListOnly) {
		this.singleListOnly = singleListOnly;
	}
	public boolean isImageMergeZipFlag() {
		return imageMergeZipFlag;
	}
	public void setImageMergeZipFlag(boolean imageMergeZipFlag) {
		this.imageMergeZipFlag = imageMergeZipFlag;
	}
	public String getAddRowButtonText() {
		return addRowButtonText;
	}
	public void setAddRowButtonText(String addRowButtonText) {
		this.addRowButtonText = addRowButtonText;
	}
	public void setComponentDescription(String description) {
		this.componentDescription = description;
	}
	public String getComponentDescription() {
		return this.componentDescription;
	}
	public String getTruncatedItemInstructions() {
		return truncatedItemInstructions;
	}
	public void setTruncatedItemInstructions(String truncatedItemInstructions) {
		this.truncatedItemInstructions = truncatedItemInstructions;
	}
	public boolean isReviewOnly() {
		return reviewOnly;
	}
	public void setReviewOnly(boolean reviewOnly) {
		this.reviewOnly = reviewOnly;
	}
	public int getSelectedListId() {
		return selectedListId;
	}
	public void setSelectedListId(int selectedListId) {
		this.selectedListId = selectedListId;
	}
	public String getSelectedListType() {
		return selectedListType;
	}
	public void setSelectedListType(String selectedListType) {
		this.selectedListType = selectedListType;
	}
	public C1uxCustDocListDetails getCurrentlySelectedList() {
		return currentlySelectedList;
	}
	public void setCurrentlySelectedList(C1uxCustDocListDetails currentlySelectedList) {
		this.currentlySelectedList = currentlySelectedList;
	}
	public boolean isListMapped() {
		return listMapped;
	}
	public void setListMapped(boolean listMapped) {
		this.listMapped = listMapped;
	}
	public boolean isListDataSaved() {
		return listDataSaved;
	}
	public void setListDataSaved(boolean listDataSaved) {
		this.listDataSaved = listDataSaved;
	}
	public boolean isSingleUseLists() {
		return singleUseLists;
	}
	public void setSingleUseLists(boolean singleUseLists) {
		this.singleUseLists = singleUseLists;
	}
	public boolean isAllowListSecuritySelection() {
		return allowListSecuritySelection;
	}
	public void setAllowListSecuritySelection(boolean allowListSecuritySelection) {
		this.allowListSecuritySelection = allowListSecuritySelection;
	}
	public int getValidRecordCount() {
		return validRecordCount;
	}
	public void setValidRecordCount(int validRecordCount) {
		this.validRecordCount = validRecordCount;
	}
	public int getInvalidRecordCount() {
		return invalidRecordCount;
	}
	public void setInvalidRecordCount(int invalidRecordCount) {
		this.invalidRecordCount = invalidRecordCount;
	}
	public int getDataRecordsPerPage() {
		return dataRecordsPerPage;
	}
	public void setDataRecordsPerPage(int dataRecordsPerPage) {
		this.dataRecordsPerPage = dataRecordsPerPage;
	}
	public C1UXCustDocMappedDataPage getInitialRecords() {
		return initialRecords;
	}
	public void setInitialRecords(C1UXCustDocMappedDataPage initialRecords) {
		this.initialRecords = initialRecords;
	}
	public List<C1UXAlternateProfileOptions> getAlternateProfiles() {
		return alternateProfiles;
	}
	public void setAlternateProfiles(List<C1UXAlternateProfileOptions> alternateProfiles) {
		this.alternateProfiles = alternateProfiles;
	}


}
