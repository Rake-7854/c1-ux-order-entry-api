/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  05/02/22    S Ramachandran  CAP-34043   Cloned CP Interface to C1 to detach GWT dependency   
 *  
 */

package com.rrd.c1ux.api.models.items;


public class SimpleThumbnail {
	private String imgURL;
	private String primaryText;
	private String secondaryText;
	private String href;
	// CAP-2774
	private String categorizationAttribVal;
	private boolean hasPrimaryAttrib;
	private boolean isDisplayAttrVal;
	
	public String getImgURL() {
		return imgURL;
	}
	public void setImgURL(String imgURL) {
		this.imgURL = imgURL;
	}
	public String getPrimaryText() {
		return primaryText;
	}
	public void setPrimaryText(String primaryText) {
		this.primaryText = primaryText;
	}
	public String getSecondaryText() {
		return secondaryText;
	}
	public void setSecondaryText(String secondaryText) {
		this.secondaryText = secondaryText;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	//CAP-2774
	public String getCategorizationAttribVal()
	{
		return categorizationAttribVal;
	}
	public void setCategorizationAttribVal(String categorizationAttribVal)
	{
		this.categorizationAttribVal = categorizationAttribVal;
	}
	public boolean isHasPrimaryAttrib()
	{
		return hasPrimaryAttrib;
	}
	public void setHasPrimaryAttrib(boolean hasPrimaryAttrib)
	{
		this.hasPrimaryAttrib = hasPrimaryAttrib;
	}
	public boolean isDisplayAttrVal()
	{
		return isDisplayAttrVal;
	}
	public void setDisplayAttrVal(boolean isDisplayAttrVal)
	{
		this.isDisplayAttrVal = isDisplayAttrVal;
	}
	// CAP-27678
	public String getLargeImageURL() 
	{
		return null;
	}
	
	public void setLargeImageURL(String largeImageURL) 
	{
		// do nothing - this only applies to Item thumbnails
	}

}
