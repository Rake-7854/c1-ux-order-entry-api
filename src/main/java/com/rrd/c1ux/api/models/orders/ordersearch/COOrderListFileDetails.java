/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			DTS#				Description
 * 	--------	-----------			----------			-----------------------------------------------------------
 *  07/23/14    Aarom Laus          CP-11449            Initial Draft
 *  09/04/14	J Publico			CP-11774			Added property to handle list cleansing
 *  01/22/15	C Buluran			CP-12155			Added code stubs for XERT Email - Order Status
 *  01/28/15	C Buluran			CP-12546			Code Stubs for XERT Email - Link Valid-Invalid List
 *  02/19/15    C Buluran			Cp-12546			Added codes for XERT Email - Link Valid-Invalid List
 *  02/20/15	C Buluran			CP-12606			Added codes for XERT Email - Link to proof viewer
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import java.io.Serializable;

public class COOrderListFileDetails implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String validURL;
	private String invalidURL;
	private String validCount;
	private String invalidCount;
	private String undelivered;
	private String duplicate;
	private String suppressed;
	private boolean viewListFiles;
	private boolean isDoneCleansing;
	//CP-12546 - 2.1 OS - Order Details - Link Valid-Invalid list
	private String optout;
	//CP-12606 - 2.1 OS - Order Details - Link to proof viewer
	private boolean isXertEnabled;

	public String getValidURL()
	{
		return validURL;
	}

	public void setValidURL(String validURL)
	{
		this.validURL = validURL;
	}

	public String getInvalidURL()
	{
		return invalidURL;
	}

	public void setInvalidURL(String invalidURL)
	{
		this.invalidURL = invalidURL;
	}

	public String getValidCount()
	{
		return validCount;
	}

	public void setValidCount(String validCount)
	{
		this.validCount = validCount;
	}

	public String getInvalidCount()
	{
		return invalidCount;
	}

	public void setInvalidCount(String invalidCount)
	{
		this.invalidCount = invalidCount;
	}

	public String getUndelivered()
	{
		return undelivered;
	}

	public void setUndelivered(String undelivered)
	{
		this.undelivered = undelivered;
	}

	public String getDuplicate()
	{
		return duplicate;
	}

	public void setDuplicate(String duplicate)
	{
		this.duplicate = duplicate;
	}

	public String getSuppressed()
	{
		return suppressed;
	}

	public void setSuppressed(String suppressed)
	{
		this.suppressed = suppressed;
	}

	public boolean isViewListFiles()
	{
		return viewListFiles;
	}

	public void setViewListFiles(boolean viewListFiles)
	{
		this.viewListFiles = viewListFiles;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}
	
	public boolean isDoneCleansing()
	{
		return isDoneCleansing;
	}

	public void setDoneCleansing(boolean isDoneCleansing)
	{
		this.isDoneCleansing = isDoneCleansing;
	}
	//CP-12546 - 2.1 OS - Order Details - Link Valid-Invalid list
	public String getOptout()
	{
		return optout;
	}

	public void setOptout(String optout)
	{
		this.optout = optout;
	}
	//CP-12606 - 2.1 OS - Order Details - Link to proof viewer
	public boolean isXertEnabled()
	{
		return isXertEnabled;
	}

	public void setXertEnabled(boolean isXertEnabled)
	{
		this.isXertEnabled = isXertEnabled;
	}

}

