/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	07/26/23		A Boomker			CAP-42225				Initialize request
 */

package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXCustDocUIPageSubmitRequest", description = "Request during a User Interface where the UI page form has been serialized and sent for a specific action", type = "object")
public class C1UXCustDocUIPageSubmitRequest {
	@Schema(name = "validate", description = "Value indicating the UI should validate when saving values. Default value is true.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean validate = true;
	@Schema(name = "form1", description = "Serialized form field values for fields within form1 form for the cust doc UI page. This must include all the standard hidden form fields. These fields must include hdnUIStepPageNumber, hdnIsDirty, hdnProjectID, hdnUINumber, eventAction, etc. If any values are duplicated by being included here, they will be overridden by values in other JSON fields outside this field. Standard format will be expected with (parameterName)=(parameterValue), and each parameter after the first must be preceded by &.", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	protected String form1 = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "eventAction", description = "Action that can be passed separately when the whole UI form isn't being serialized for a request.", type = "String", example = AtWinXSConstant.EMPTY_STRING,
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
	protected String eventAction = AtWinXSConstant.EMPTY_STRING;
	@Schema(name = "otherSerializedForm", description = "Serialized form field values for fields within a different form (NOT the UI page form1) for the cust doc UI page. This does not have to include all the standard hidden form1 fields. If any values are duplicated by being included here, they will be overridden by values in other JSON fields outside this field. These fields will override fields seriaized into the form1 field though. Standard format will be expected with (parameterName)=(parameterValue), and each parameter after the first must be preceded by &.", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	protected String otherSerializedForm = AtWinXSConstant.EMPTY_STRING;


	public boolean isValidate() {
		return validate;
	}
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	public String getForm1() {
		return form1;
	}
	public void setForm1(String form1) {
		this.form1 = form1;
	}
	public String getEventAction() {
		return eventAction;
	}
	public void setEventAction(String eventAction) {
		this.eventAction = eventAction;
	}
	public String getOtherSerializedForm() {
		return otherSerializedForm;
	}
	public void setOtherSerializedForm(String otherSerializedForm) {
		this.otherSerializedForm = otherSerializedForm;
	}

}
