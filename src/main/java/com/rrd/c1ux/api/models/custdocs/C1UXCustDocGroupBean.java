/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/19/23				A Boomker				CAP-42295					Initial Version
 *	01/29/24				A Boomker				CAP-46336					Adding grid information
 */
package com.rrd.c1ux.api.models.custdocs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rrd.custompoint.orderentry.customdocs.C1UXCustDocGridColumn;
import com.rrd.custompoint.orderentry.customdocs.C1UXCustDocGridRow;
import com.rrd.custompoint.orderentry.customdocs.Group.WorkingProofControl;
import com.rrd.custompoint.orderentry.customdocs.SampleImageControl;
import com.rrd.custompoint.orderentry.customdocs.Variable;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name ="C1UXCustDocGroupBean", description = "Response Class equivalent to AbstractGroupImpl in CP - info to display a single UI group", type = "object")
public class C1UXCustDocGroupBean {


	@Schema(name ="groupDisplayControls", description = "Display Control rules for show-hide for the group itself", type = "object")
	protected C1UXDisplayControls groupDisplayControls;
	@Schema(name ="workingProofControlCode", description = "Working Proof Control rules string for variables on the group. Values are: N for none, C for updating the proof on the change of any variable (when focus goes off of text fields), and B for updating the proof on click of the Update button.",
			type = "string", allowableValues = {"B", "C", "N"})
	protected String workingProofControlCode = WorkingProofControl.NONE.toString();
	@Schema(name ="sampleImageControl", description = "Sample Image Control rules for the group", type = "object")
	protected SampleImageControl sampleImageControl = null;

	@Schema(name ="instructionText", description = "Instructions for the group itself - may include html", type = "string")
	protected String instructionText;
	@Schema(name ="instructionTypeCode", description = "Instructions display type code for the group itself. Values are empty - no display, A for text above the field, H for hyperlink on the group name, and M for mouseover text.", type = "string",
			allowableValues = {"", "A", "H", "M"})
	protected String instructionsTypeCode = Variable.InstructionType.NONE.toString();

	@Schema(name = "alwaysShown", description = "Value indicating the group is never hidden. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean alwaysShown = true;

	@Schema(name = "workingProof", description = "Value indicating the group can affect a working proof. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean workingProof = false; // this indicates whether in a working proof UI for sizing html

	@Schema(name ="sequence", description = "Numeric ordering for the group within the page.", type = "number")
	protected int sequence;
	@Schema(name ="groupNumber", description = "Unique id for the group within the UI.", type = "number")
	protected int groupNumber;
	@Schema(name ="minCheckboxes", description = "Minimum number of checkboxes that must be checked within the group if checkbox validation is turned on.", type = "number")
	protected int minCheckboxes = 0;
	@Schema(name ="maxCheckboxes", description = "Maximum number of checkboxes that may be checked within the group if checkbox validation is turned on.", type = "number")
	protected int maxCheckboxes = 0;
	@Schema(name = "hidden", description = "Value indicating the group is currently hidden. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean hidden = false;

	@Schema(name ="groupName", description = "Name for the group. This is always displayed in admin and may or may not be displayed during order entry.", type = "string")
	protected String groupName;
	@Schema(name = "showName", description = "Value indicating the group name should be displayed. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showName = false;
	@Schema(name = "valuesBlanked", description = "Value indicating the group was blanked out by show hide when hidden. This means that upon redisplay, it should re-default. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean valuesBlanked = false;
	@Schema(name = "useRollerblind", description = "Value indicating the classic expand/collapse functionality is turned on. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean useRollerblind = false;
	@Schema(name = "rollerblindDefaultOpen", description = "Value indicating the classic expand/collapse functionality is turned on AND the group should default to expanded. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean rollerblindDefaultOpen = false;

	@Schema(name = "useCheckboxLimits", description = "Value indicating the group has a validation limit to the number of checkboxes that can be cheked. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean useCheckboxLimits = false;
	@Schema(name ="checkboxCustomError", description = "If checkbox limits are turned on, this will be the custom error text to display when the number of checked boxes are outside the limits.", type = "string")
	protected String checkboxCustomError;
	// CAP-16749 - added reentry flag to affect validation
	@Schema(name = "skipDynamicDataCalls", description = "Value indicating the order was resumed in an item association that allows the user to skip remaking dynamic list calls within a specific time frame. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean skipDynamicDataCalls = false;

	@Schema(name = "grid", description = "Value indicating the group should use grid display. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean grid = false;
	@Schema(name = "groupGridNeverAddRowsInd", description = "Value indicating the group is a grid group AND has turned on the option to not allow the user to add rows even if they are hidden. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean groupGridNeverAddRowsInd = false;

	// CAP-3952 - variable level show/hide
	@Schema(name ="variableLevelDisplayControls", description = "Map of variables (keyed by variable number) and the display controls that will affect that variable's display", type = "array")
	protected Map<Integer, C1UXDisplayControls> variableLevelDisplayControls = new HashMap<>(); // CAP-3951 - add hashmap keyed off variable number assigned to the group

	@Schema(name ="c1uxVariables", description = "List of variables to display on this group in the sequence of display", type = "array")
	protected List<C1UXVariableBean> c1uxVariables = new ArrayList<>();

	@Schema(name ="divID", description = "ID that must be used on the div for the group for show/hide to work correctly = 'groupDiv' + groupNum", type = "string")
	protected String divID;
	@Schema(name ="hiddenShowHideFieldID", description = "ID and name that must be used on the hidden input for the group for show/hide to work correctly on the back end during save = 'showGroup' + groupNum", type = "string")
	protected String hiddenShowHideFieldID;
	@Schema(name ="hiddenShowHideFieldValue", description = "Initial Y/N value the hidden input for the group for show/hide to work correctly on the back end during save = 'showVar' + varNum", type = "string", allowableValues= {"Y", "N"})
	protected String hiddenShowHideFieldValue;

	@Schema(name ="emptyCellAssignments", description = "Applicable to grid groups only - List of empty cells on this group in the sequence of display", type = "array")
	protected List<C1UXCustDocGridAssignment> emptyCellAssignments  = new ArrayList<>();
	@Schema(name ="rows", description = "Applicable to grid groups only - List of rows on this group in the sequence of display", type = "array")
	protected List<C1UXCustDocGridRow> rows  = new ArrayList<>();
	@Schema(name ="columns", description = "Applicable to grid groups only - List of columns on this group in the sequence of display", type = "array")
	protected List<C1UXCustDocGridColumn> columns  = new ArrayList<>();
	@Schema(name = "showRowHeaders", description = "Applicable to grid groups only - value indicating whether the leftmost column for row headers should display. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showRowHeaders = false;
	@Schema(name = "showGridAddRowButton", description = "Applicable to grid groups only - value indicating whether this specific grid has rows hidden because they were blanked and should display the add rows button for that reason. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean showGridAddRowButton = false;
	@Schema(name = "condensedFormat", description = "Applicable to grid groups only - value indicating whether this specific grid needs to use styles with less padding and smaller font due to size constraints. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean condensedFormat = false;

	public C1UXDisplayControls getGroupDisplayControls() {
		return groupDisplayControls;
	}

	public void setGroupDisplayControls(C1UXDisplayControls displayControls) {
		this.groupDisplayControls = displayControls;
	}

	public String getWorkingProofControlCode() {
		return workingProofControlCode;
	}

	public void setWorkingProofControlCode(String workingProofControl) {
		this.workingProofControlCode = workingProofControl;
	}

	public SampleImageControl getSampleImageControl() {
		return sampleImageControl;
	}

	public void setSampleImageControl(SampleImageControl sampleImageControl) {
		this.sampleImageControl = sampleImageControl;
	}

	public String getInstructionText() {
		return instructionText;
	}

	public void setInstructionText(String instructionText) {
		this.instructionText = instructionText;
	}

	public String getInstructionsTypeCode() {
		return instructionsTypeCode;
	}

	public void setInstructionsTypeCode(String instructionsType) {
		this.instructionsTypeCode = instructionsType;
	}

	public boolean isAlwaysShown() {
		return alwaysShown;
	}

	public void setAlwaysShown(boolean alwaysShown) {
		this.alwaysShown = alwaysShown;
	}

	public boolean isWorkingProof() {
		return workingProof;
	}

	public void setWorkingProof(boolean workingProof) {
		this.workingProof = workingProof;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public int getMinCheckboxes() {
		return minCheckboxes;
	}

	public void setMinCheckboxes(int minCheckboxes) {
		this.minCheckboxes = minCheckboxes;
	}

	public int getMaxCheckboxes() {
		return maxCheckboxes;
	}

	public void setMaxCheckboxes(int maxCheckboxes) {
		this.maxCheckboxes = maxCheckboxes;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean isShowName() {
		return showName;
	}

	public void setShowName(boolean showName) {
		this.showName = showName;
	}

	public boolean isValuesBlanked() {
		return valuesBlanked;
	}

	public void setValuesBlanked(boolean valuesBlanked) {
		this.valuesBlanked = valuesBlanked;
	}

	public boolean isUseRollerblind() {
		return useRollerblind;
	}

	public void setUseRollerblind(boolean useRollerblind) {
		this.useRollerblind = useRollerblind;
	}

	public boolean isRollerblindDefaultOpen() {
		return rollerblindDefaultOpen;
	}

	public void setRollerblindDefaultOpen(boolean rollerblindDefaultOpen) {
		this.rollerblindDefaultOpen = rollerblindDefaultOpen;
	}

	public boolean isUseCheckboxLimits() {
		return useCheckboxLimits;
	}

	public void setUseCheckboxLimits(boolean useCheckboxLimits) {
		this.useCheckboxLimits = useCheckboxLimits;
	}

	public String getCheckboxCustomError() {
		return checkboxCustomError;
	}

	public void setCheckboxCustomError(String checkboxCustomError) {
		this.checkboxCustomError = checkboxCustomError;
	}

	public boolean isSkipDynamicDataCalls() {
		return skipDynamicDataCalls;
	}

	public void setSkipDynamicDataCalls(boolean skipDynamicDataCalls) {
		this.skipDynamicDataCalls = skipDynamicDataCalls;
	}

	public boolean isGrid() {
		return grid;
	}

	public void setGrid(boolean isGrid) {
		this.grid = isGrid;
	}

	public boolean isGroupGridNeverAddRowsInd() {
		return groupGridNeverAddRowsInd;
	}

	public void setGroupGridNeverAddRowsInd(boolean groupGridNeverAddRowsInd) {
		this.groupGridNeverAddRowsInd = groupGridNeverAddRowsInd;
	}

	public Map<Integer, C1UXDisplayControls> getVariableLevelDisplayControls() {
		return variableLevelDisplayControls;
	}

	public void setVariableLevelDisplayControls(Map<Integer, C1UXDisplayControls> variableDisplayControls) {
		this.variableLevelDisplayControls = variableDisplayControls;
	}


	public List<C1UXVariableBean> getC1uxVariables() {
		return c1uxVariables;
	}

	public void setC1uxVariables(List<C1UXVariableBean> variables) {
		this.c1uxVariables = variables;
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

	public String getHiddenShowHideFieldValue() {
		return hiddenShowHideFieldValue;
	}

	public void setHiddenShowHideFieldValue(String hiddenShowHideFieldValue) {
		this.hiddenShowHideFieldValue = hiddenShowHideFieldValue;
	}

	public List<C1UXCustDocGridAssignment> getEmptyCellAssignments() {
		return emptyCellAssignments;
	}

	public void setEmptyCellAssignments(List<C1UXCustDocGridAssignment> emptyCellAssignments) {
		this.emptyCellAssignments = emptyCellAssignments;
	}

	public List<C1UXCustDocGridRow> getRows() {
		return rows;
	}

	public void setRows(List<C1UXCustDocGridRow> rows) {
		this.rows = rows;
	}

	public List<C1UXCustDocGridColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<C1UXCustDocGridColumn> columns) {
		this.columns = columns;
	}

	public boolean isShowRowHeaders() {
		return showRowHeaders;
	}

	public void setShowRowHeaders(boolean showRowHeaders) {
		this.showRowHeaders = showRowHeaders;
	}

	public boolean isShowGridAddRowButton() {
		return showGridAddRowButton;
	}

	public void setShowGridAddRowButton(boolean showGridAddRowButton) {
		this.showGridAddRowButton = showGridAddRowButton;
	}

	public boolean isCondensedFormat() {
		return condensedFormat;
	}

	public void setCondensedFormat(boolean condensedFormat) {
		this.condensedFormat = condensedFormat;
	}


}
