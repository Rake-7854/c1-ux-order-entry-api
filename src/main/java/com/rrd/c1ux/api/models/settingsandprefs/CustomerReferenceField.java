/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			DTS#				Description
 * 	--------	-----------			----------			-----------------------------------------------------------
 *	04/30/22	Krishna Natarajan	CAP-34022			Reuse the model from CP
 */
package com.rrd.c1ux.api.models.settingsandprefs;

/**
 * @author Krishna Natarajan
 *
 */
public class CustomerReferenceField {
	//Copied from CustomerReferenceField from CP
	//CP-10565 RAR - Supported tag list.
		public static final String REP_TAG_CR_CNTCT_LOGIN									= "[--O USERLOGINID--]";
		public static final String REP_TAG_CR_PROFILE_ID									= "[--O USERPROFILEID--]";
		public static final String REP_TAG_CR_FIRST_NAME									= "[--O USERFIRSTNAME--]";
		public static final String REP_TAG_CR_LAST_NAME										= "[--O USERLASTNAME--]";
		public static final String REP_TAG_CR_CNTCT_EMAIL									= "[--O USEREMAIL--]";
		public static final String REP_TAG_CR_CNTCT_PHONE									= "[--O USERPHONE--]";
		
		public static final String REP_TAG_CR_REQ_CNTCT_LOGIN								= "[--O REQLOGINID--]";
		public static final String REP_TAG_CR_REQ_PROFILE_ID								= "[--O REQPROFILEID--]";
		public static final String REP_TAG_CR_REQ_FIRST_NAME								= "[--O REQFIRSTNAME--]";
		public static final String REP_TAG_CR_REQ_LAST_NAME									= "[--O REQLASTNAME--]";
		public static final String REP_TAG_CR_REQ_CNTCT_EMAIL								= "[--O REQEMAIL--]";
		public static final String REP_TAG_CR_REQ_CNTCT_PHONE								= "[--O REQPHONE--]";
		
		public static final String REP_TAG_CR_PRFL_PROFILE_ID								= "[--P PTYPE.PROFILEID--]";
		public static final String REP_TAG_CR_PRFL_FIRST_NAME								= "[--P PTYPE.FIRSTNAME--]";
		public static final String REP_TAG_CR_PRFL_LAST_NAME								= "[--P PTYPE.LASTNAME--]";
		public static final String REP_TAG_CR_PRFL_CNTCT_EMAIL								= "[--P PTYPE.EMAIL--]";
		public static final String REP_TAG_CR_PRFL_CNTCT_PHONE								= "[--P PTYPE.PHONE--]";
		public static final String REP_TAG_CR_PRFL_COMPANY_NAME								= "[--P PTYPE.COMPANYNAME--]";
		public static final String REP_TAG_CR_PRFL_UDF1TO80									= "[--P PTYPE.PROFILEUDF#--]"; //CAP-33308
		
		public static final String REP_TAG_CR_SHIP_TO_NAME1									= "[--O SHIPTONAME1--]";
		public static final String REP_TAG_CR_SHIP_TO_NAME2									= "[--O SHIPTONAME2--]";
		public static final String REP_TAG_CR_SHIP_TO_STATE									= "[--O SHIPTOSTATEPROV--]";
		public static final String REP_TAG_CR_SHIP_TO_ZIP									= "[--O SHIPTOZIPPOSTAL--]";
		public static final String REP_TAG_CR_SHIP_TO_COUNTRY								= "[--O SHIPTOCOUNTRY--]";
		
		//CAP-9571 SRN Added tags for Ship To First Name and Ship To Last Name
		public static final String REP_TAG_CR_SHIP_TO_FIRST_NAME							= "[--O SHIPTOFIRSTNAME--]";
		public static final String REP_TAG_CR_SHIP_TO_LAST_NAME								= "[--O SHIPTOLASTNAME--]";
			
		public static final String REP_TAG_CR_SHIP_TO_LOCATION1								= "[--O SHIPTOLOCATION1--]"; //CAP-50
		public static final String REP_TAG_CR_SHIP_TO_LOCATION2								= "[--O SHIPTOLOCATION2--]"; //CAP-50 
		public static final String REP_TAG_CR_SHIP_TO_LOCATION3								= "[--O SHIPTOLOCATION3--]"; //CAP-50
		
		public static final String REP_TAG_SITE_ATTR										= "[--SA_Site Attribute Name--]";
		public static final String REP_TAG_CR_USER_PROFILE_UDF1TO80							= "[--O USERPROFILEUDF#--]"; //CAP-33308
		public static final String REP_TAG_CR_REQUESTOR_PROFILE_UDF1TO80					= "[--O REQPROFILEUDF#--]"; //CAP-33308	
		
		public static final String REP_TAG_CR_USER_PROFILE_UDF								= "[--O USERPROFILEUDF--]";
		public static final String REP_TAG_CR_REQUESTOR_PROFILE_UDF							= "[--O REQPROFILEUDF--]";
		
		// CAP-2156 [NKM] New supported tag for Order Title
		public static final String REP_TAG_ORD_TITLE										= "[--O ORDERTITLE--]";

		//CAP-25061
		public static final String REP_TAG_CD_INSTANCE_NM 									= "[--O CDINSTANCENAME--]";

		//CAP-29683 SRN Added tag for Cust Doc variable name line only
		public static final String REP_TAG_CD_VARIABLE_NAME_LINE_ONLY						= "[--CD CustomDocumentVariableName--]";
			
		public static final String REP_TAG_ITEM_ATTR										= "[--SAI_Item Attribute--]";
		//CAP-31012
		public static final String REP_TAG_CD_DELIVERY_OPT 									= "[--O CDDELIVERYOPTION--]";
		
		protected String referenceFieldCode = "";
		protected String referenceFieldLabel = "";
		protected String wcssFieldID = "";
		protected int fieldSize = 0;
		protected int maxLength = 0;
		protected String fieldMask = "";
		protected String fieldSelectCode = "";
		protected boolean validate = false;
		protected int minRequired = 0;
		protected int maxRequired = 0;
		protected int seqNumber = 0;
		protected int displayType = 1;
		protected String ugDefaultSetting = "";
		protected String custRefShowCd = "";
		
		public String getReferenceFieldCode() {
			return referenceFieldCode;
		}
		public void setReferenceFieldCode(String referenceFieldCode) {
			this.referenceFieldCode = referenceFieldCode;
		}
		public String getReferenceFieldLabel() {
			return referenceFieldLabel;
		}
		public void setReferenceFieldLabel(String referenceFieldLabel) {
			this.referenceFieldLabel = referenceFieldLabel;
		}
		public String getWcssFieldID() {
			return wcssFieldID;
		}
		public void setWcssFieldID(String wcssFieldID) {
			this.wcssFieldID = wcssFieldID;
		}
		public int getFieldSize() {
			return fieldSize;
		}
		public void setFieldSize(int fieldSize) {
			this.fieldSize = fieldSize;
		}
		public int getMaxLength() {
			return maxLength;
		}
		public void setMaxLength(int maxLength) {
			this.maxLength = maxLength;
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
		public boolean isValidate() {
			return validate;
		}
		public void setValidate(boolean validate) {
			this.validate = validate;
		}
		public int getMinRequired() {
			return minRequired;
		}
		public void setMinRequired(int minRequired) {
			this.minRequired = minRequired;
		}
		public int getMaxRequired() {
			return maxRequired;
		}
		public void setMaxRequired(int maxRequired) {
			this.maxRequired = maxRequired;
		}
		public int getSeqNumber() {
			return seqNumber;
		}
		public void setSeqNumber(int seqNumber) {
			this.seqNumber = seqNumber;
		}
		public int getDisplayType() {
			return displayType;
		}
		public void setDisplayType(int displayType) {
			this.displayType = displayType;
		}
		public String getUgDefaultSetting() {
			return ugDefaultSetting;
		}
		public void setUgDefaultSetting(String ugDefaultSetting) {
			this.ugDefaultSetting = ugDefaultSetting;
		}
		public String getCustRefShowCd() {
			return custRefShowCd;
		}
		public void setCustRefShowCd(String custRefShowCd) {
			this.custRefShowCd = custRefShowCd;
		}

}
