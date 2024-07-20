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
import java.util.Collection;

public class COOSDetailsItemEFDDetailsData implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int orderID;
	private String salesReferenceNumber;
	private String customerItemNumber;
	private String vendorItemNumber;
	private String ftpSource;
	
	private String efdEmailLinkDisplayText;
	private String efdEmailLinkURL;
	private String efdLandingPageDisplayText;
	private String efdLandingLinkURL;
	private String emailTos;
	

	private Collection<String> itemEFDSources;
	
	public int getOrderID()
	{
		return orderID;
	}
	
	public void setOrderID(int orderID)
	{
		this.orderID = orderID;
	}
	
	public String getSalesReferenceNumber()
	{
		return salesReferenceNumber;
	}
	
	public void setSalesReferenceNumber(String salesReferenceNumber)
	{
		this.salesReferenceNumber = salesReferenceNumber;
	}
	
	public String getCustomerItemNumber()
	{
		return customerItemNumber;
	}
	
	public void setCustomerItemNumber(String customerItemNumber)
	{
		this.customerItemNumber = customerItemNumber;
	}
	
	public String getVendorItemNumber()
	{
		return vendorItemNumber;
	}
	
	public void setVendorItemNumber(String vendorItemNumber)
	{
		this.vendorItemNumber = vendorItemNumber;
	}
	
	public String getFtpSource()
	{
		return ftpSource;
	}

	public void setFtpSource(String ftpSource)
	{
		this.ftpSource = ftpSource;
	}

	public String getEfdEmailLinkDisplayText()
	{
		return efdEmailLinkDisplayText;
	}
	
	public void setEfdEmailLinkDisplayText(String efdEmailLinkDisplayText)
	{
		this.efdEmailLinkDisplayText = efdEmailLinkDisplayText;
	}
	
	public String getEfdEmailLinkURL()
	{
		return efdEmailLinkURL;
	}
	
	public void setEfdEmailLinkURL(String efdEmailLinkURL)
	{
		this.efdEmailLinkURL = efdEmailLinkURL;
	}
	
	public String getEfdLandingPageDisplayText()
	{
		return efdLandingPageDisplayText;
	}
	
	public void setEfdLandingPageDisplayText(String efdLandingPageDisplayText)
	{
		this.efdLandingPageDisplayText = efdLandingPageDisplayText;
	}
	
	public String getEfdLandingLinkURL()
	{
		return efdLandingLinkURL;
	}
	
	public void setEfdLandingLinkURL(String efdLandingLinkURL)
	{
		this.efdLandingLinkURL = efdLandingLinkURL;
	}
	
	public String getEmailTos()
	{
		return emailTos;
	}
	
	public void setEmailTos(String emailTos)
	{
		this.emailTos = emailTos;
	}

	public Collection<String> getItemEFDSources()
	{
		return itemEFDSources;
	}

	public void setItemEFDSources(Collection<String> itemEFDSources)
	{
		this.itemEFDSources = itemEFDSources;
	}
	
	
}