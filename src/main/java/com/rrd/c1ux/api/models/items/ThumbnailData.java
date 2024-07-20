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

public interface ThumbnailData 
{
	public String getImgURL();
	public void setImgURL(String imgURL);
	public String getPrimaryText();
	public void setPrimaryText(String primaryText);
	public String getSecondaryText();
	public void setSecondaryText(String secondaryText);
	public String getHref();
	public void setHref(String href);
	// CAP-2774
	public String getCategorizationAttribVal();
	public void setCategorizationAttribVal(String categorizationAttribVal);
	public boolean isHasPrimaryAttrib();
	public void setHasPrimaryAttrib(boolean hasPrimaryAttrib);
	public boolean isDisplayAttrVal();
	public void setDisplayAttrVal(boolean isDisplayAttrVal);
	// CAP-27678
	public String getLargeImageURL();
	public void setLargeImageURL(String largeImageURL);
}
