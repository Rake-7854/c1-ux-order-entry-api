/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/31/24				N Caceres				CAP-46698					Initial Version
 */
package com.rrd.c1ux.api.models.catalog.wizard;

public class OrderWizardAttributeValues {
	
	private int attributeValueID;
	private String attributeValueDescription;
	private int attributeQuestionID;
	private int wizardQuestionID;
	private boolean selected;
	
	public int getAttributeValueID() {
		return attributeValueID;
	}
	public void setAttributeValueID(int attributeValueID) {
		this.attributeValueID = attributeValueID;
	}
	public String getAttributeValueDescription() {
		return attributeValueDescription;
	}
	public void setAttributeValueDescription(String attributeValueDescription) {
		this.attributeValueDescription = attributeValueDescription;
	}
	public int getAttributeQuestionID() {
		return attributeQuestionID;
	}
	public void setAttributeQuestionID(int attributeQuestionID) {
		this.attributeQuestionID = attributeQuestionID;
	}
	public int getWizardQuestionID() {
		return wizardQuestionID;
	}
	public void setWizardQuestionID(int wizardQuestionID) {
		this.wizardQuestionID = wizardQuestionID;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
