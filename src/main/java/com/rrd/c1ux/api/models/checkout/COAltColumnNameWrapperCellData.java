package com.rrd.c1ux.api.models.checkout;

public class COAltColumnNameWrapperCellData {
	private String textVariableName;
	private String displayLabel;
	private boolean isRequired;
		
	public String getTextVariableName() {
		return textVariableName;
	}
	public void setTextVariableName(String textVariableName) {
		this.textVariableName = textVariableName;
	}
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	public boolean isRequired() {
		return isRequired;
	}
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
}
