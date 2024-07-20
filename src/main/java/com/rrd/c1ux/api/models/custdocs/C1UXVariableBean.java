/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/20/23				A Boomker				CAP-42295					Initial Version
 *	07/31/23				A Boomker				CAP-42224					Add event actions map
 *	09/11/23				A Boomker				CAP-43528					Adding checked status for checkbox input type
 *	09/14/23				A Boomker				CAP-43660					Added dateFormat for calendar date input type
 *	11/21/23				A Boomker				CAP-44780					Adding fields for upload information
 *	12/06/23				A Boomker				CAP-45656					Fixed comment for list upload type code
 *	01/11/24				A Boomker				CAP-43031					Added showThumbnailLabels
 *	02/13/24				A Boomker				CAP-46309					Added alternateDownloadLinkURL
 *	02/19/24				A Boomker				CAP-44837					Changes to list options to indicate uploads
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.Collection;
import java.util.Map;

import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXVariableBean", description = "Response Class equivalent to VariableImpl in CP - info to display a single variable", type = "object")
public class C1UXVariableBean {

	@Schema(name ="name", description = "Unique variable name", type = "string")
	protected String name;
	@Schema(name ="displayLabel", description = "Variable display label - does not have to be non-empty and can be non-unique", type = "string")
	protected String displayLabel;

	@Schema(name ="instructionText", description = "Instructions for the group itself - may include html", type = "string")
	protected String instructionText;
	@Schema(name ="instructionTypeCode", description = "Instructions display type code for the group itself. Values are empty - no display, A for text above the field, H for hyperlink on the variable label, and M for mouseover text.", type = "string",
			allowableValues = {"", "A", "H", "M"})
	protected String instructionTypeCode;

	protected Collection<String> errorMessages;
	@Schema(name ="defaultValue", description = "Default value for the variable in case redefaulting is needed", type = "string")
	protected String defaultValue;
	@Schema(name="editable", description = "Flag indicating that the variable should render this input as editable. If false, then this must display as view only and not allow the user to make any changes.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean editable = true;
	@Schema(name ="variablePageflexTypeCode", description = "Code corresponding to the variable type in 141 table - also corresponds to codes used in Pageflex project definitions.", type = "string")
	protected String variablePageflexTypeCode;
	@Schema(name ="textValue", description = "Stored text value for the variable for plant output. For file pageflex types, this is a file name for plant output - high resolution.", type = "string")
	protected String textValue;
	@Schema(name ="proofValue", description = "Stored value for a file pageflex type variable that should be used for low-resolution proofing output. This should be a file name.", type = "string")
	protected String proofValue;
	@Schema(name ="listValue", description = "Stored value for an input type that uses lists that indicates which list value ID is currently selected for the variable to correspond to the text value.", type = "string")
	protected String listValue;
	@Schema(name="functionalDisplayVariable", description = "Flag indicating that the input type allows the variable to be able to trigger show hide.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean functionalDisplayVariable = false;
	@Schema(name ="listTypeCd", description = "For input types that use lists, this is a code corresponding to how the list was generated and whether refreshes are needed. Values are S - static values, P - profile image repository values, D - dynamic list with sql, '' (empty value) for external source. For input types not using a list, null will be passed.", type = "string")
	protected String listTypeCd;
	@Schema(name="keyValueInd", description = "Flag indicating that the variable is specified in the UI as a key variable. Changes to this variable may trigger an external source webservice call, depending on whether the UI requires all key variables to be populated before one is made.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean keyValueInd;
	@Schema(name="inputRequired", description = "Flag indicating that the variable must force the selection/entry of a value when it is shown.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean inputRequired;
	@Schema(name="minMaxValidationRequired", description = "Flag indicating that the input type must validate the length of the value.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean minMaxValidationRequired;
	@Schema(name="minTextLength", description = "Minimum number of characters allowed that should be validated if minMaxValidationRequired is true.", type = "number")
	protected int minTextLength;
	@Schema(name="maxTextLength", description = "Maximum number of characters allowed that should be validated if minMaxValidationRequired is true.", type = "number")
	protected int maxTextLength;
	@Schema(name="numericValidationRequired", description = "Flag indicating that the input type must validate that the value is a number.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean numericValidationRequired;
	@Schema(name="validateDate", description = "Flag indicating that the input type must validate the date entered against a timebomb validation setting.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean validateDate;
	@Schema(name ="timeframe", description = "Number of days timeframe for a timebomb validation on a calendar/date input type if one applies.", type = "number")
	protected int timeframe;
	@Schema(name="affectsDynamicList", description = "Flag indicating that the variable is used in dynamic list SQL in the population of other variables in the UI. Changes to this variable may need to trigger a save of the UI page and a refresh call.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean affectsDynamicList;
	@Schema(name="dependentVarPages", description = "Array of the page numbers on which this specific variable must trigger a save and reload due to other variables needing its value in SQL or otherwise.", type = "array")
	protected Integer[] dependentVarPages;
	@Schema(name ="inputTypeCode", description = "Code corresponding to the input type in 139 table - controls how the variable will be populated.", type = "string")
	protected String inputTypeCode;

	@Schema(name="number", description = "Unique variable number", type = "number")
	protected int number;
	@Schema(name ="noneLabel", description = "Translated label for a none option if needed.", type = "string")
	protected String noneLabel;
	@Schema(name ="noValueLabel", description = "Translated label for non-editable display if no value is populated on the variable.", type = "string")
	protected String noValueLabel;

	@Schema(name="uploadAllowedInd", description = "Flag indicating that the input type allows the user to upload a file to populate the variable.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean uploadAllowedInd = false;
	@Schema(name="initialValue", description = "Flag indicating that the variable is still in its defaulted state and has not been changed or saved since that time.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean initialValue = false; // CP-10983

	@Schema(name="maskAddsCharacters", description = "Flag indicating that the specific mask assigned to this variable prevents min/max length validation.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean maskAddsCharacters; // CP-11812 - need to know if can validate max length and numeric
	@Schema(name ="source", description = "This code indicates whether the variable type is from CP, Pageflex, or somewhere else. It should not be needed but included just in case.", type = "string")
	protected String source;
	@Schema(name="imagesPerRow", description = "If this is an image variable displaying thumbnails, this is the number of thumbnails that should display across a row of options.", type = "number")
	protected int imagesPerRow;

	//CAP-3758
	@Schema(name="reuseUploadFiles", description = "Flag indicating that the UI allows for multiple variables to access a common pool of uploaded files across multiple variables.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean reuseUploadFiles = false;
	// CAP-4801
	@Schema(name="blankedByShowHide", description = "Flag indicating that the variable was blanked by show hide and should reset to the default if it is shown again.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean blankedByShowHide = false;
	@Schema(name ="defaultListValue", description = "This holds the default list value ID that should re-default if needed on a list input type.", type = "string")
	protected String defaultListValue;

	//CAP-9798 RAR
	@Schema(name="useNewFormatting", description = "Flag indicating that the text area input type will convert to full XML for saving. This allows for a more simplified conversion before sending to Pageflex for proofing.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean useNewFormatting = false;
	// CAP-15529 - add access to flag
	@Schema(name="lockedSequencing", description = "Flag indicating that the multiple selection list input type cannot allow the user to resequence the list options. Their order must be locked to that defined in the list.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean lockedSequencing;

	@Schema(name="showCharsRemain", description = "Flag indicating that the input type allows the display of characters remaining.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showCharsRemain;
	// CAP-14753
	@Schema(name="cropImageHeight", description = "Double value of the maximum height allowed for a cropped uploaded image.", type = "float")
	protected double cropImageHeight;
	@Schema(name="cropImageWidth", description = "Double value of the maximum width allowed for a cropped uploaded image.", type = "float")
	protected double cropImageWidth;

	// CAP-16751
	@Schema(name="skipDynamicDataCalls", description = "Flag indicating that the UI was entered within the timeframe that allows dynamic lists to not be regenerated yet.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean skipDynamicDataCalls = false;

	// CAP-16751 - this method is for when we need to display currently selected list options from previously generated data that is
	// no longer in the current UI list - FOR MULTIPLE SELECTION LISTS
	@Schema(name="generateSelectedOptionsFromOldData", description="This holds additional already selected list options that must be added to a specified list when the list options may vary. This typically applies to external source and dynamic lists.", type="array")
	protected Collection<C1UXUIListOption> generateSelectedOptionsFromOldData;

	// CAP-26025/CAP-26026
	@Schema(name ="searchButtonURL", description = "This holds the external source URL that should be called when a search is triggered onclick of a varible-specific search button.", type = "string")
	protected String searchButtonURL;
	@Schema(name ="searchButtonText", description = "This holds the text label override for a varible-specific search button if Search should not be used.", type = "string")
	protected String searchButtonText;
	// CAP-28128
	@Schema(name ="level", description = "This code indicates whether the variable type is defined at the project or UI level. It should not be needed but included just in case.", type = "string")
	protected String level;

	// CAP-29609
	@Schema(name="hybridMessageDocsItem", description = "Flag indicating that the Item is a special item that must follow special display and other rules.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean hybridMessageDocsItem = false;
	// CAP-31592
	@Schema(name="usingProfileImage", description = "Flag indicating that the variable is defaulting to the main profile image that may require approval.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean usingProfileImage;
	// CAP-32151
	@Schema(name ="profileImageWebPath", description = "This holds the path that should be used on a web page to display a profile image if it is in use for this variable.", type = "string")
	protected String profileImageWebPath;

	@Schema(name ="divID", description = "ID that must be used on the div for the variable for show/hide to work correctly = 'Var' + varNum + 'Div'", type = "string")
	protected String divID;
	@Schema(name ="hiddenShowHideFieldID", description = "ID and name that must be used on the hidden input for the variable for show/hide to work correctly on the back end during save = 'showVar' + varNum", type = "string")
	protected String hiddenShowHideFieldID;
	@Schema(name ="hiddenShowHideFieldValue", description = "Initial Y/N value the hidden input for the variable for show/hide to work correctly on the back end during save = 'showVar' + varNum", type = "string", allowableValues= {"Y", "N"})
	protected String hiddenShowHideFieldValue;

	@Schema(name ="formInputName", description = "ID and name that must be used on the input for a text field variable for javascript to work correctly = 'varVal' + varName", type = "string")
	protected String formInputName;
	@Schema(name ="formInputName", description = "ID and name that must be used on the input for a list input variable for javascript to work correctly = 'varList' + varName", type = "string")
	protected String formListInputName;
	@Schema(name ="formInputHiddenName", description = "ID and name that must be used on the hidden input for every variable for javascript to work correctly and save to work correctly = 'varHdn' + varName", type = "string")
	protected String formInputHiddenName;
	@Schema(name ="variableErrorDivName", description = "ID and name that must be used on the div that shows under a field when the field has an error at the variable level for javascript to work correctly = 'err' + varName", type = "string")
	protected String variableErrorDivName;
	@Schema(name ="variableErrorTextDivName", description = "ID and name that must be used on the div that will contain the error text within the error div for when the field has an error at the variable level for javascript to work correctly = 'errText' + varName", type = "string")
	protected String variableErrorTextDivName;
	@Schema(name ="keyInputName", description = "ID and name that must be used on the hidden input for every KEY variable for javascript to work correctly and save to work correctly. Non-key variables will have a null. If it is a Key variable, the value for this field on the form must be 'Y'. = 'varValHdnKeyValue' + varName", type = "string")
	protected String keyInputName;
// CAP-42224 - add event actions
	@Schema(name ="eventActionJavascript", description = "Hashmap of event actions for the specific variable. Key options are: onkeyup, onkeydown, onkeypress, onclick, onchange, onfocus.", type = "object")
	protected Map<String, String> eventActionJavascript = null;

	@Schema(name ="listKey", description = "Key to retrieve the variable's list from the page map. listTypeCode_listDataType_listId will the be value if there is a list. Null if there is no list.", type = "string")
	protected String listKey;
	@Schema(name="textBoxSize", description = "Maximum number of characters to be displayed as the size of a text box variable.", type = "number")
	protected int textBoxSize;
	@Schema(name="textAreaRows", description = "Starting number of rows to be displayed as the height of a text area variable.", type = "number")
	protected int textAreaRows;
	@Schema(name="textAreaColumns", description = "Starting number of columns to be displayed as the width of a text area variable.", type = "number")
	protected int textAreaColumns;
	@Schema(name="wordProcess", description = "Flag indicating that the input type should use word processing.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean wordProcess = false;
	@Schema(name ="uncheckedValue", description = "Value to be used on a checkbox input type if the checkbox is not checked.", type = "string")
	protected String uncheckedValue;
	// CAP-43528
	@Schema(name="currentlyChecked", description = "Flag indicating that the variable is a checkbox and should default to checked. Variables that are not input type checkbox will be false. Variables that are input type checkbox may be false or true.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean currentlyChecked = false;
	// CAP-43660
	@Schema(name ="dateFormat", description = "This is only applicable to calendar date input type variables. It should be the date format that will be used by the primeNG calendar component. It may not match the exact format used to validate with Java on the back end. This will be null if the input type is anything other than calendar/date.", type = "string")
	protected String dateFormat;

	// CAP-44780
	@Schema(name ="uploadFileFormatsCode", description = "If upload is turned on, this is the value to determine which file format message to display on an upload popup."
			+ "'A' for All images (CP translation key is \"cdSupportedFileTypesImagesEps\"). 'E' for EPS only for images. (CP translation key is \"cdSupportedFileTypesEpsOnly\") "
			+ "'S' for standard only for images. (CP translation key is \"cdSupportedFileTypesImages\") 'I' for regular inserts. (CP translation key is \"cdSupportedFileTypesInsert\") "
			+ "'F' for regular file upload. (CP translation key is \"cdSupportedFileTypesFileUpload\") 'R' for regular new request. (CP translation key is \"cdSupportedFileTypesNewRequest\") "
			+ "'Z' for new request with zip allowed. (CP translation key is \"cdSupportedFileTypesNewReqZip\") 'H' for hosted resources. (CP translation key is \"TRANS_NM_HR_FILE_TYPES\")",
			type = "string", allowableValues = {
				CustomDocsService.UPLOAD_FILE_FORMATS_ALL_IMAGES, // CP translation key is "cdSupportedFileTypesImagesEps"
				CustomDocsService.UPLOAD_FILE_FORMATS_IMAGES_STANDARD_ONLY, // CP translation key is "cdSupportedFileTypesImages"
				CustomDocsService.UPLOAD_FILE_FORMATS_IMAGES_EPS_ONLY, // CP translation key is "cdSupportedFileTypesEpsOnly"
				CustomDocsService.UPLOAD_FILE_FORMATS_INSERTS, // CP translation key is "cdSupportedFileTypesInsert"
				CustomDocsService.UPLOAD_FILE_FORMATS_FILE_UPLOAD, // CP translation key is "cdSupportedFileTypesFileUpload"
				CustomDocsService.UPLOAD_FILE_FORMATS_NEW_REQUEST, // CP translation key is "cdSupportedFileTypesNewRequest"
				CustomDocsService.UPLOAD_FILE_FORMATS_NEW_REQUEST_ZIP, // CP translation key is "cdSupportedFileTypesNewReqZip"
				CustomDocsService.UPLOAD_FILE_FORMATS_HOSTED_RESOURCE // CP translation key is "TRANS_NM_HR_FILE_TYPES"
	})
	protected String uploadFileFormatsCode = AtWinXSConstant.EMPTY_STRING;
	@Schema(name ="uploadListOptionName", description = "If upload is turned on, this is the id for a list input type option to add when a file is saved from an upload popup. ", type = "string")
	protected String uploadListOptionName = AtWinXSConstant.EMPTY_STRING;
	@Schema(name="imageCropperAllowed", description = "If upload is turned on, this indicates that this image variable should allow the cropper.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean imageCropperAllowed = false;
	@Schema(name="imageSearchAllowed", description = "Flag indicating that image search is turned on for this image variable.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean imageSearchAllowed = false;
	@Schema(name="minFiles", description = "Minimum number of files required to be uploaded for this variable.", type = "number")
	protected int minFiles = 0;
	@Schema(name="maxFiles", description = "Maximum number of files allowed to be uploaded for this variable.", type = "number")
	protected int maxFiles = 0;

	@Schema(name="showThumbnailLabels", description = "Flag indicating that this image variable uses thumbnails and should show the labels for the option.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showThumbnailLabels = false;

	@Schema(name ="gridAssignment", description = "Applicable to grid groups only - location of this variable within the grid layout.", type = "object")
	protected C1UXCustDocGridAssignment gridAssignment = null;

	// CAP-46309
	@Schema(name ="alternateDownloadLinkURL", description = "This is only applicable to upload variables on new request flow. It should be the link to view the currently uploaded file from the new request location. It is empty if not applicable.", type = "string")
	protected String alternateDownloadLinkURL = AtWinXSConstant.EMPTY_STRING;

	@Schema(name="selectedUploadedListValues", description="This holds additional already selected uploaded list options that must be added to a specified list. For single select input types, this can be a length of one at most.", type="array")
	protected Collection<C1UXUIListOption> selectedUploadedListValues = null;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	public String getInstructionText() {
		return instructionText;
	}
	public void setInstructionText(String instructionText) {
		this.instructionText = instructionText;
	}
	public Collection<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(Collection<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public String getVariablePageflexTypeCode() {
		return variablePageflexTypeCode;
	}
	public void setVariablePageflexTypeCode(String variablePageflexTypeCode) {
		this.variablePageflexTypeCode = variablePageflexTypeCode;
	}
	public String getTextValue() {
		return textValue;
	}
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}
	public String getProofValue() {
		return proofValue;
	}
	public void setProofValue(String proofValue) {
		this.proofValue = proofValue;
	}
	public String getListValue() {
		return listValue;
	}
	public void setListValue(String listValue) {
		this.listValue = listValue;
	}
	public boolean isFunctionalDisplayVariable() {
		return functionalDisplayVariable;
	}
	public void setFunctionalDisplayVariable(boolean functionalDisplayVariable) {
		this.functionalDisplayVariable = functionalDisplayVariable;
	}
	public String getListTypeCd() {
		return listTypeCd;
	}
	public void setListTypeCd(String listTypeCd) {
		this.listTypeCd = listTypeCd;
	}
	public boolean isKeyValueInd() {
		return keyValueInd;
	}
	public void setKeyValueInd(boolean keyValueInd) {
		this.keyValueInd = keyValueInd;
	}
	public boolean isInputRequired() {
		return inputRequired;
	}
	public void setInputRequired(boolean inputRequired) {
		this.inputRequired = inputRequired;
	}
	public boolean isMinMaxValidationRequired() {
		return minMaxValidationRequired;
	}
	public void setMinMaxValidationRequired(boolean minMaxValidationRequired) {
		this.minMaxValidationRequired = minMaxValidationRequired;
	}
	public int getMinTextLength() {
		return minTextLength;
	}
	public int getMaxTextLength() {
		return maxTextLength;
	}
	public void setMaxTextLength(int maxNumberForValidation) {
		this.maxTextLength = maxNumberForValidation;
	}
	public boolean isNumericValidationRequired() {
		return numericValidationRequired;
	}
	public void setNumericValidationRequired(boolean numericValidationRequired) {
		this.numericValidationRequired = numericValidationRequired;
	}
	public boolean isValidateDate() {
		return validateDate;
	}
	public void setValidateDate(boolean validateDate) {
		this.validateDate = validateDate;
	}
	public int getTimeframe() {
		return timeframe;
	}
	public void setTimeframe(int timeframe) {
		this.timeframe = timeframe;
	}
	public boolean isAffectsDynamicList() {
		return affectsDynamicList;
	}
	public void setAffectsDynamicList(boolean affectsDynamicList) {
		this.affectsDynamicList = affectsDynamicList;
	}
	public Integer[] getDependentVarPages() {
		return dependentVarPages;
	}
	public void setDependentVarPages(Integer[] dependentVarPages) {
		this.dependentVarPages = dependentVarPages;
	}
	public String getInputTypeCode() {
		return inputTypeCode;
	}
	public void setInputTypeCode(String inputTypeCode) {
		this.inputTypeCode = inputTypeCode;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getNoneLabel() {
		return noneLabel;
	}
	public void setNoneLabel(String noneLabel) {
		this.noneLabel = noneLabel;
	}
	public String getNoValueLabel() {
		return noValueLabel;
	}
	public void setNoValueLabel(String noValueLabel) {
		this.noValueLabel = noValueLabel;
	}
	public boolean isUploadAllowedInd() {
		return uploadAllowedInd;
	}
	public void setUploadAllowedInd(boolean isUploadAllowedInd) {
		this.uploadAllowedInd = isUploadAllowedInd;
	}
	public boolean isInitialValue() {
		return initialValue;
	}
	public void setInitialValue(boolean initialValue) {
		this.initialValue = initialValue;
	}
	public boolean isMaskAddsCharacters() {
		return maskAddsCharacters;
	}
	public void setMaskAddsCharacters(boolean maskAddsCharacters) {
		this.maskAddsCharacters = maskAddsCharacters;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public int getImagesPerRow() {
		return imagesPerRow;
	}
	public void setImagesPerRow(int imagesPerRow) {
		this.imagesPerRow = imagesPerRow;
	}
	public boolean isReuseUploadFiles() {
		return reuseUploadFiles;
	}
	public void setReuseUploadFiles(boolean reuseUploadFiles) {
		this.reuseUploadFiles = reuseUploadFiles;
	}
	public boolean isBlankedByShowHide() {
		return blankedByShowHide;
	}
	public void setBlankedByShowHide(boolean blankedByShowHide) {
		this.blankedByShowHide = blankedByShowHide;
	}
	public String getDefaultListValue() {
		return defaultListValue;
	}
	public void setDefaultListValue(String defaultListValue) {
		this.defaultListValue = defaultListValue;
	}
	public boolean isUseNewFormatting() {
		return useNewFormatting;
	}
	public void setUseNewFormatting(boolean useNewFormatting) {
		this.useNewFormatting = useNewFormatting;
	}
	public boolean isLockedSequencing() {
		return lockedSequencing;
	}
	public void setLockedSequencing(boolean lockedSequencing) {
		this.lockedSequencing = lockedSequencing;
	}
	public double getCropImageHeight() {
		return cropImageHeight;
	}
	public void setCropImageHeight(double cropImageHeight) {
		this.cropImageHeight = cropImageHeight;
	}
	public double getCropImageWidth() {
		return cropImageWidth;
	}
	public void setCropImageWidth(double cropImageWidth) {
		this.cropImageWidth = cropImageWidth;
	}
	public boolean isSkipDynamicDataCalls() {
		return skipDynamicDataCalls;
	}
	public void setSkipDynamicDataCalls(boolean skipDynamicDataCalls) {
		this.skipDynamicDataCalls = skipDynamicDataCalls;
	}
	public Collection<C1UXUIListOption> getGenerateSelectedOptionsFromOldData() {
		return generateSelectedOptionsFromOldData;
	}
	public void setGenerateSelectedOptionsFromOldData(Collection<C1UXUIListOption> generateSelectedOptionsFromOldData) {
		this.generateSelectedOptionsFromOldData = generateSelectedOptionsFromOldData;
	}
	public String getSearchButtonURL() {
		return searchButtonURL;
	}
	public void setSearchButtonURL(String searchButtonURL) {
		this.searchButtonURL = searchButtonURL;
	}
	public String getSearchButtonText() {
		return searchButtonText;
	}
	public void setSearchButtonText(String searchButtonText) {
		this.searchButtonText = searchButtonText;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public boolean isHybridMessageDocsItem() {
		return hybridMessageDocsItem;
	}
	public void setHybridMessageDocsItem(boolean hybridMessageDocsItem) {
		this.hybridMessageDocsItem = hybridMessageDocsItem;
	}
	public boolean isUsingProfileImage() {
		return usingProfileImage;
	}
	public void setUsingProfileImage(boolean usingProfileImage) {
		this.usingProfileImage = usingProfileImage;
	}
	public String getProfileImageWebPath() {
		return profileImageWebPath;
	}
	public void setProfileImageWebPath(String profileImageWebPath) {
		this.profileImageWebPath = profileImageWebPath;
	}
	public String getDivID() {
		return divID;
	}
	public void setDivID(String divID) {
		this.divID = divID;
	}
	public String getHiddenShowHideFieldID() {
		return hiddenShowHideFieldID;
	}
	public void setHiddenShowHideFieldID(String hiddenShowHideFieldID) {
		this.hiddenShowHideFieldID = hiddenShowHideFieldID;
	}
	public String getFormInputName() {
		return formInputName;
	}
	public void setFormInputName(String formInputName) {
		this.formInputName = formInputName;
	}
	public String getFormListInputName() {
		return formListInputName;
	}
	public void setFormListInputName(String formListInputName) {
		this.formListInputName = formListInputName;
	}
	public String getFormInputHiddenName() {
		return formInputHiddenName;
	}
	public void setFormInputHiddenName(String formInputHiddenName) {
		this.formInputHiddenName = formInputHiddenName;
	}
	public String getVariableErrorDivName() {
		return variableErrorDivName;
	}
	public void setVariableErrorDivName(String variableErrorDivName) {
		this.variableErrorDivName = variableErrorDivName;
	}
	public String getVariableErrorTextDivName() {
		return variableErrorTextDivName;
	}
	public void setVariableErrorTextDivName(String variableErrorTextDivName) {
		this.variableErrorTextDivName = variableErrorTextDivName;
	}
	public String getKeyInputName() {
		return keyInputName;
	}
	public void setKeyInputName(String keyInputName) {
		this.keyInputName = keyInputName;
	}
	public Map<String, String> getEventActionJavascript() {
		return eventActionJavascript;
	}
	public void setEventActionJavascript(Map<String, String> eventActionJavascript) {
		this.eventActionJavascript = eventActionJavascript;
	}
	public String getHiddenShowHideFieldValue() {
		return hiddenShowHideFieldValue;
	}
	public void setHiddenShowHideFieldValue(String hiddenShowHideFieldValue) {
		this.hiddenShowHideFieldValue = hiddenShowHideFieldValue;
	}
	public String getInstructionTypeCode() {
		return instructionTypeCode;
	}
	public void setInstructionTypeCode(String instructionTypeCode) {
		this.instructionTypeCode = instructionTypeCode;
	}
	public boolean isShowCharsRemain() {
		return showCharsRemain;
	}
	public void setShowCharsRemain(boolean showCharsRemain) {
		this.showCharsRemain = showCharsRemain;
	}
	public String getListKey() {
		return listKey;
	}
	public void setListKey(String listKey) {
		this.listKey = listKey;
	}
	public int getTextBoxSize() {
		return textBoxSize;
	}
	public void setTextBoxSize(int textBoxSize) {
		this.textBoxSize = textBoxSize;
	}
	public int getTextAreaRows() {
		return textAreaRows;
	}
	public void setTextAreaRows(int textAreaRows) {
		this.textAreaRows = textAreaRows;
	}
	public int getTextAreaColumns() {
		return textAreaColumns;
	}
	public void setTextAreaColumns(int textAreaColumns) {
		this.textAreaColumns = textAreaColumns;
	}
	public boolean isWordProcess() {
		return wordProcess;
	}
	public void setWordProcess(boolean wordProcess) {
		this.wordProcess = wordProcess;
	}
	public String getUncheckedValue() {
		return uncheckedValue;
	}
	public void setUncheckedValue(String uncheckedValue) {
		this.uncheckedValue = uncheckedValue;
	}
	public void setMinTextLength(int minTextLength) {
		this.minTextLength = minTextLength;
	}
	public boolean isCurrentlyChecked() {
		return currentlyChecked;
	}
	public void setCurrentlyChecked(boolean currentlyChecked) {
		this.currentlyChecked = currentlyChecked;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getUploadFileFormatsCode() {
		return uploadFileFormatsCode;
	}
	public void setUploadFileFormatsCode(String uploadFileFormatsCode) {
		this.uploadFileFormatsCode = uploadFileFormatsCode;
	}
	public String getUploadListOptionName() {
		return uploadListOptionName;
	}
	public void setUploadListOptionName(String uploadListOptionName) {
		this.uploadListOptionName = uploadListOptionName;
	}
	public boolean isImageCropperAllowed() {
		return imageCropperAllowed;
	}
	public void setImageCropperAllowed(boolean imageCropperAllowed) {
		this.imageCropperAllowed = imageCropperAllowed;
	}
	public boolean isImageSearchAllowed() {
		return imageSearchAllowed;
	}
	public void setImageSearchAllowed(boolean imageSearchAllowed) {
		this.imageSearchAllowed = imageSearchAllowed;
	}
	public int getMinFiles() {
		return minFiles;
	}
	public void setMinFiles(int minFiles) {
		this.minFiles = minFiles;
	}
	public int getMaxFiles() {
		return maxFiles;
	}
	public void setMaxFiles(int maxFiles) {
		this.maxFiles = maxFiles;
	}
	public boolean isShowThumbnailLabels() {
		return showThumbnailLabels;
	}
	public void setShowThumbnailLabels(boolean showThumbnailLabels) {
		this.showThumbnailLabels = showThumbnailLabels;
	}
	public C1UXCustDocGridAssignment getGridAssignment() {
		return gridAssignment;
	}
	public void setGridAssignment(C1UXCustDocGridAssignment gridAssignment) {
		this.gridAssignment = gridAssignment;
	}
	public String getAlternateDownloadLinkURL() {
		return alternateDownloadLinkURL;
	}
	public void setAlternateDownloadLinkURL(String alternateDownloadLinkURL) {
		this.alternateDownloadLinkURL = alternateDownloadLinkURL;
	}
	public Collection<C1UXUIListOption> getSelectedUploadedListValues() {
		return selectedUploadedListValues;
	}
	public void setSelectedUploadedListValues(Collection<C1UXUIListOption> selectedUploadedListValues) {
		this.selectedUploadedListValues = selectedUploadedListValues;
	}

}
