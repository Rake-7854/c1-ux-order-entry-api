/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			DTS#				Description
 * 	--------	-----------			----------			-----------------------------------------------------------
 *	04/30/22	Krishna Natarajan	CAP-34022			Reused the model from CP
 */

package com.rrd.c1ux.api.models.settingsandprefs;

/**
 * @author Krishna Natarajan
 *
 */
public class CustomerReferenceFieldList {
	//Copied from CustomerReferenceFieldList from CP
	protected String referenceFieldCode = "";
	protected String referenceFieldValue = "";
	protected String wcssFieldCode = "";
	protected String fieldMask = "";
	protected String fieldSelectCode = "";
	protected boolean requiresValidation = false;
	protected boolean wcssValidated = true;
	protected boolean maskValidated = true;
	protected boolean primaryDefault = false;
	
	public String getReferenceFieldCode() {
		return referenceFieldCode;
	}
	public void setReferenceFieldCode(String referenceFieldCode) {
		this.referenceFieldCode = referenceFieldCode;
	}
	public String getReferenceFieldValue() {
		return referenceFieldValue;
	}
	public void setReferenceFieldValue(String referenceFieldValue) {
		this.referenceFieldValue = referenceFieldValue;
	}
	public String getWcssFieldCode() {
		return wcssFieldCode;
	}
	public void setWcssFieldCode(String wcssFieldCode) {
		this.wcssFieldCode = wcssFieldCode;
	}
	public String getFieldMask() {
		return fieldMask;
	}
	public void setFieldMask(String fieldMask) {
		this.fieldMask = fieldMask;
	}
	public String getFieldSelectCode() {
		return fieldSelectCode;
	}
	public void setFieldSelectCode(String fieldSelectCode) {
		this.fieldSelectCode = fieldSelectCode;
	}
	public boolean isRequiresValidation() {
		return requiresValidation;
	}
	public void setRequiresValidation(boolean requiresValidation) {
		this.requiresValidation = requiresValidation;
	}
	public boolean isWcssValidated() {
		return wcssValidated;
	}
	public void setWcssValidated(boolean wcssValidated) {
		this.wcssValidated = wcssValidated;
	}
	public boolean isMaskValidated() {
		return maskValidated;
	}
	public void setMaskValidated(boolean maskValidated) {
		this.maskValidated = maskValidated;
	}
	public boolean isPrimaryDefault() {
		return primaryDefault;
	}
	public void setPrimaryDefault(boolean primaryDefault) {
		this.primaryDefault = primaryDefault;
	}

	
}
