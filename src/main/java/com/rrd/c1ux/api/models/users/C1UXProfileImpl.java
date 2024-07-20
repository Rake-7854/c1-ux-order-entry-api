/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				Sakthi M				CAP-39612					Initial Version
 */
package com.rrd.c1ux.api.models.users;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.rrd.custompoint.admin.profile.entity.AddressBook;
import com.rrd.custompoint.admin.profile.entity.CorporateProfile;
import com.rrd.custompoint.admin.profile.entity.ExtendedProfile;
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;
import com.rrd.custompoint.admin.profile.entity.UserDefinedFields;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C1UXProfileImpl implements C1UXProfile, Serializable {

	public static final long serialVersionUID = -2824760886745773968L;
	
	protected int siteID;
	protected int businessUnitID;
	protected int profileNumber;
	protected String contactFirstName;
	protected String contactLastName;
	protected String contactPhoneNumber;
	protected String contactEmailAddress;
	protected String profileID;
	protected boolean deletedProfileInd;
	protected int defaultPersonalAddressID;
	protected String upperCaseProfileID;
	protected String upperCaseContactLastName;
	protected int hierarchyID;
	protected String crmLoginID;
	protected String crmPassword;
	protected String defaultAddressSourceCd;
	protected String defaultAddressID;
	protected String currencyOverride;
	protected boolean forceUpdateInd;
	protected int profileDefinitionID;
	protected boolean active;
	protected String priceClass;    //CP-11319
	
	protected String createUserID;
	protected String createProgramID;
	protected Date createTimestamp;
	
	protected String changeUserID;
	protected String changeProgramID;
	protected Date changeTimestamp;	

	// CAP-31051
	protected String profileImageFileName;
	protected boolean profileImageApproved;
	
	//NON- DB Data
	protected boolean dirty;
	protected boolean existing;
	
	protected CorporateProfile corporateProfile;
	protected ExtendedProfile extendedProfile;
	protected ProfileDefinition profileDefinition;
	protected AddressBook addressBook;
	protected UserDefinedFields userDefinedFields;
	
	//CAP-4232 RAR
	protected String emailAddressTemp = AtWinXSConstant.EMPTY_STRING;
	protected boolean emailChecked = false;
	protected boolean showExistingEmailWarning = false;
	
	//CAP-46634
	private transient List<SelfAdminSiteAttributes> selfAdminSiteAttributes;
	
}
