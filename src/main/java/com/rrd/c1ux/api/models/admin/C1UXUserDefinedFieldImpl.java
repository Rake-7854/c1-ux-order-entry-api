package com.rrd.c1ux.api.models.admin;

import java.util.Date;

import com.rrd.custompoint.admin.profile.entity.ProfileUDFDefinition;
import com.rrd.custompoint.framework.entity.BaseEntity;
import com.wallace.atwinxs.framework.util.Util;

public class C1UXUserDefinedFieldImpl extends BaseEntity implements C1UXUserDefinedField{
private static final long serialVersionUID = -6132422671903769032L;
	
	transient ProfileUDFDefinition profileUDFDefinition = null;
	
	//DB DATA
	protected int siteID;
	protected int businessUnitID;
	protected int profileNumber;
	protected int udfFieldNumber;
	protected String udfValueText;
	
	protected String createUserID;
	protected String createProgramID;
	protected Date createTimestamp;
	
	//NON-DB Data
	protected boolean isDirty;
	protected boolean isExisting;
	
	@Override
	public int getSiteID()
	{
		return siteID;
	}

	@Override
	public void setSiteID(int siteID)
	{
		checkDirty(this.siteID == siteID);
		this.siteID = siteID;
	}

	@Override
	public int getBusinessUnitID()
	{
		return businessUnitID;
	}
	
   
   @Override
	public void setBusinessUnitID(int businessUnitID)
	{
	   checkDirty(this.businessUnitID == businessUnitID);
	   this.businessUnitID = businessUnitID;
	}

	@Override
	public int getProfileNumber()
	{
		return profileNumber;
	}

	@Override
	public void setProfileNumber(int profileNumber)
	{
		checkDirty(this.profileNumber == profileNumber);
		this.profileNumber = profileNumber;
	}

	@Override
	public int getUdfFieldNumber()
	{
		return udfFieldNumber;
	}

	@Override
	public void setUdfFieldNumber(int udfFieldNumber)
	{
		checkDirty(this.udfFieldNumber == udfFieldNumber); 
		this.udfFieldNumber = udfFieldNumber;
	}

	@Override
	public String getUdfValueText()
	{
		return udfValueText;
	}
	
	protected void checkDirty(boolean isEqual) {
		   if(!this.isDirty) {
				if(isEqual) {
					this.isDirty=false;
				}
				else {
					this.isDirty=true;
				}
			}
			else {
				this.isDirty=true;
			}
    } 

	@Override
	public void setUdfValueText(String udfValueText)
	{
		this.isDirty = Util.isDirty(this.udfValueText, udfValueText, isDirty);
		this.udfValueText = udfValueText;
	}

	@Override
	public String getCreateUserID()
	{
		return createUserID;
	}

	@Override
	public void setCreateUserID(String createUserID)
	{
		this.createUserID = createUserID;
	}

	@Override
	public String getCreateProgramID()
	{
		return createProgramID;
	}

	@Override
	public void setCreateProgramID(String createProgramID)
	{
		this.createProgramID = createProgramID;
	}

	@Override
	public Date getCreateTimestamp()
	{
		return createTimestamp;
	}

	@Override
	public void setCreateTimestamp(Date createTimestamp)
	{
		this.createTimestamp = createTimestamp;
	}

	//RAR - Expose isExisting getter
	@Override
	public boolean isExisting()
	{
		return isExisting;
	}

	//RAR - Expose isExisting setter
	@Override
	public void setExisting(boolean isExisting)
	{
		this.isExisting = isExisting;
	}

	@Override
	public int compareTo(C1UXUserDefinedField o) {
		return 0;
	}
}
