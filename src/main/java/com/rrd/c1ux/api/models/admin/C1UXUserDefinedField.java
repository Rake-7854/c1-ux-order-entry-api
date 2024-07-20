package com.rrd.c1ux.api.models.admin;

import java.io.Serializable;
import java.util.Date;

import com.rrd.custompoint.framework.customizable.ICustomizable;

public interface C1UXUserDefinedField extends ICustomizable, Serializable, Comparable<C1UXUserDefinedField>{
	public int getSiteID();
	public void setSiteID(int siteID);
	public int getBusinessUnitID();
	public void setBusinessUnitID(int businessUnitID);
	public int getProfileNumber();
	public void setProfileNumber(int profileNumber);
	public int getUdfFieldNumber();
	public void setUdfFieldNumber(int udfFieldNumber);
	public String getUdfValueText();
	public void setUdfValueText(String udfValueText);
	public String getCreateUserID();
	public void setCreateUserID(String createUserID);
	public String getCreateProgramID();
	public void setCreateProgramID(String createProgramID);
	public Date getCreateTimestamp();
	public void setCreateTimestamp(Date createTimestamp);
	
	//RAR - Expose isExisting getter/setter
	public boolean isExisting();
	public void setExisting(boolean isExisting);
}
