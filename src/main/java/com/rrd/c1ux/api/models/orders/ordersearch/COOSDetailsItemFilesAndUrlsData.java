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


public class COOSDetailsItemFilesAndUrlsData  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int orderID;
	private String salesReferenceNumber;
	private String customerItemNumber;
	private String vendorItemNumber;
	private COOSDetailsItemCustDocDetailsData custDocDetails;
	private COOSDetailsItemEFDDetailsData efdDetails;
	
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
	
	public COOSDetailsItemCustDocDetailsData getCustDocDetails()
	{
		return custDocDetails;
	}
	
	public void setCustDocDetails(COOSDetailsItemCustDocDetailsData custDocDetails)
	{
		this.custDocDetails = custDocDetails;
	}
	
	public COOSDetailsItemEFDDetailsData getEfdDetails()
	{
		return efdDetails;
	}

	public void setEfdDetails(COOSDetailsItemEFDDetailsData efdDetails)
	{
		this.efdDetails = efdDetails;
	}

}