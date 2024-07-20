/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			DTS#				Description
 * 	--------	-----------			----------			-----------------------------------------------------------
 *  11/23/22	S Ramachandran  	CAP-36557   		Added from CP GMT module
 */

package com.rrd.c1ux.api.models.orders.ordersearch;

import java.io.Serializable;

public class COOrderPrintFileDetails implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String printFileName;
	private String printtFileURL;
	private boolean isAvailable = true; //CAP-20954

	public String getPrintFileName()
	{
		return printFileName;
	}

	public void setPrintFileName(String printFileName)
	{
		this.printFileName = printFileName;
	}

	public String getPrinttFileURL()
	{
		return printtFileURL;
	}

	public void setPrinttFileURL(String printtFileURL)
	{
		this.printtFileURL = printtFileURL;
	}

	//CAP-20954
	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
}
