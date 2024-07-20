/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				Sakthi M				CAP-39612					Initial Version
 *	03/07/24				L De Leon				CAP-47615					Added methods for site attributes
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
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface C1UXProfile extends  Serializable{

	 public int getSiteID();
	 public void setSiteID(int siteID);
	 public int getBusinessUnitID();
	 public void setBusinessUnitID(int businessUnitID);
	 public int getProfileNumber();
	 public void setProfileNumber(int profileNumber);
	 public String getContactFirstName();
	 public void setContactFirstName(String contactFirstName);
	 public String getContactLastName();
	 public void setContactLastName(String contactLastName);
	 public String getContactPhoneNumber();
	 public void setContactPhoneNumber(String contactPhoneNumber);
	 public String getContactEmailAddress();
	 public void setContactEmailAddress(String contactEmailAddress);
	 public String getProfileID();
	 public void setProfileID(String profileID);
	 public boolean isDeletedProfileInd();
	 public void setDeletedProfileInd(boolean isDeletedProfileInd);
	 public int getDefaultPersonalAddressID();
	 public void setDefaultPersonalAddressID(int defaultPersonalAddressID);
	 public String getUpperCaseProfileID();
	 public void setUpperCaseProfileID(String upperCaseProfileID);
	 public String getUpperCaseContactLastName();
	 public void setUpperCaseContactLastName(String upperCaseContactLastName);
	 public int getHierarchyID();
	 public void setHierarchyID(int hierarchyID);
	 public String getCrmLoginID();
	 public void setCrmLoginID(String crmLoginID);
	 public String getCrmPassword();
	 public void setCrmPassword(String crmPassword);
	 public String getDefaultAddressSourceCd();
	 public void setDefaultAddressSourceCd(String defaultAddressSourceCd);
	 public String getDefaultAddressID();
	 public void setDefaultAddressID(String defaultAddressID);
	 public String getCurrencyOverride();
	 public void setCurrencyOverride(String currencyOverride);
	 public boolean isForceUpdateInd();
	 public void setForceUpdateInd(boolean isForceUpdateInd);
	 public int getProfileDefinitionID();
	 public void setProfileDefinitionID(int profileDefinitionID);
	 public boolean isActive();
	 public void setActive(boolean isActive);
	 public String getCreateUserID();
	 public void setCreateUserID(String createUserID);
	 public String getCreateProgramID();
	 public void setCreateProgramID(String createProgramID);
	 public Date getCreateTimestamp();
	 public void setCreateTimestamp(Date createTimestamp);
	 public String getChangeUserID();
	 public void setChangeUserID(String changeUserID);
	 public String getChangeProgramID();
	 public void setChangeProgramID(String changeProgramID);
	 public Date getChangeTimestamp();
	 public void setChangeTimestamp(Date changeTimestamp);
	 public boolean isExisting();
	 public void setPriceClass(String priceClass);    //CP-11319
	 public String getPriceClass();   //CP-11319

	// CAP-31051
	 public String getProfileImageFileName();
	 public void setProfileImageFileName(String profileImageFileName);
	 public boolean isProfileImageApproved();
	 public void setProfileImageApproved(boolean profileImageApproved);

	/**
	 * Method getCorporateProfile()
	 * 
	 * This method will populate the {@link CorporateProfile} for the User details in this object.
	 * 
	 * @throws AtWinXSException
	 */
	 public CorporateProfile getCorporateProfile() throws AtWinXSException;
	
	/**
	 * Method getExtendedProfile()
	 * 
	 * This method will populate the {@link ExtendedProfile} for the User details in this object.
	 * 
	 * @throws AtWinXSException
	 */
	 public ExtendedProfile getExtendedProfile() throws AtWinXSException;

	/**
	 * Method getProfileDefinition()
	 * 
	 * This method will populate the {@link ProfileDefinition} for the User details in this object.
	 * 
	 * @throws AtWinXSException
	 */
	 public ProfileDefinition getProfileDefinition() throws AtWinXSException;

	/**
	 * Method getAddressBook()
	 * 
	 * This method will populate the {@link AddressBook} for the User details in this object.
	 * 
	 * @throws AtWinXSException
	 */
	 public AddressBook getAddressBook() throws AtWinXSException;

	/**
	 * Method getUserDefinedFields()
	 * 
	 * This method will populate the {@link UserDefinedFields} for the User details in this object.
	 * 
	 * @throws AtWinXSException
	 */
	 public UserDefinedFields getUserDefinedFields() throws AtWinXSException;
		
	// CAP-47615
	public List<SelfAdminSiteAttributes> getSelfAdminSiteAttributes();
	public void setSelfAdminSiteAttributes(List<SelfAdminSiteAttributes> selfAdminSiteAttributes);
}
