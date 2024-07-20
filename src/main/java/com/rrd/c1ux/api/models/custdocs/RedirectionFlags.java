package com.rrd.c1ux.api.models.custdocs;

/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *    Date        	Modified By     JIRA#       Description
 *    --------    	-----------     ----------  -----------------------------------------------------------
 *	07/26/23		A Boomker		CAP-42225 - CAP-25569	Copied from CP
*/

public class RedirectionFlags
{
	private boolean redirecting;
	private boolean redirectingOutsideCustDocs;

	public RedirectionFlags(boolean a, boolean b)
	{
		redirecting = a;
		redirectingOutsideCustDocs = b;
	}

	public boolean isRedirecting()
	{
		return redirecting;
	}

	public void setRedirecting(boolean redirecting)
	{
		this.redirecting = redirecting;
	}

	public boolean isRedirectingOutsideCustDocs()
	{
		return redirectingOutsideCustDocs;
	}

	public void setRedirectingOutsideCustDocs(boolean redirectingOutsideCustDocs)
	{
		this.redirectingOutsideCustDocs = redirectingOutsideCustDocs;
	}
}