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

public interface ThumbnailCellData extends ThumbnailData
{
	String getPrimaryText();
	String getImgURL();
}
