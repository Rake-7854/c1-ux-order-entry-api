package com.rrd.c1ux.api.models.checkout;

public class COColumnNameWrapperCellData {
	
	private String columnName;
	private boolean isRequired;
		
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public boolean isRequired() {
		return isRequired;
	}
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
}
