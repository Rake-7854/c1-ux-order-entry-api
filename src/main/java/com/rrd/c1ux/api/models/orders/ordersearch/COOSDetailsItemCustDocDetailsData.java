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

public class COOSDetailsItemCustDocDetailsData implements Serializable
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String orderID;
	private String salesReferenceNumber;
	private String customerItemNumber;
	private String vendorItemNumber;

	private int lineNum;
	private String itemNumber;
	private String description;
	private int customDocOrderLineID;
	private String transID;

	private boolean isViewProof;
	private boolean hasProof; //CAP-20954
	private String viewProofURL;
	private String proofID;
	private String proofList;
	private String viewPDFProofURL;
	private boolean isViewPrintFile;
	private boolean isViewListFile;

	private Collection<COOrderPrintFileDetails> printFiles;
	private COOrderListFileDetails listFiles;
//- PREVIEW
//- PRINT FILES
//- LIST FILES
	//CP-12605 - 2.2 OS - XERT Dashboard
	private String emailStatistics;
	//CP-12606 - 2.1 OS - Order Details - Link to proof viewer
	private boolean isXertEnabled;

	public String getOrderID()
	{
		return orderID;
	}

	public void setOrderID(String orderID)
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

	public int getLineNum()
	{
		return lineNum;
	}

	public void setLineNum(int lineNum)
	{
		this.lineNum = lineNum;
	}

	public String getItemNumber()
	{
		return itemNumber;
	}

	public void setItemNumber(String itemNumber)
	{
		this.itemNumber = itemNumber;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getCustomDocOrderLineID()
	{
		return customDocOrderLineID;
	}

	public void setCustomDocOrderLineID(int customDocOrderLineID)
	{
		this.customDocOrderLineID = customDocOrderLineID;
	}

	public String getTransID()
	{
		return transID;
	}

	public void setTransID(String transID)
	{
		this.transID = transID;
	}

	public boolean isViewProof()
	{
		return isViewProof;
	}

	public void setViewProof(boolean isViewProof)
	{
		this.isViewProof = isViewProof;
	}

	public String getViewProofURL()
	{
		return viewProofURL;
	}

	public void setViewProofURL(String viewProofURL)
	{
		this.viewProofURL = viewProofURL;
	}

	public String getProofID()
	{
		return proofID;
	}

	public void setProofID(String proofID)
	{
		this.proofID = proofID;
	}

	public String getProofList()
	{
		return proofList;
	}

	public void setProofList(String proofList)
	{
		this.proofList = proofList;
	}

	public String getViewPDFProofURL()
	{
		return viewPDFProofURL;
	}

	public void setViewPDFProofURL(String viewPDFProofURL)
	{
		this.viewPDFProofURL = viewPDFProofURL;
	}

	public boolean isViewPrintFile()
	{
		return isViewPrintFile;
	}

	public void setViewPrintFile(boolean isViewPrintFile)
	{
		this.isViewPrintFile = isViewPrintFile;
	}

	public boolean isViewListFile()
	{
		return isViewListFile;
	}

	public void setViewListFile(boolean isViewListFile)
	{
		this.isViewListFile = isViewListFile;
	}

	public Collection<COOrderPrintFileDetails> getPrintFiles()
	{
		return printFiles;
	}

	public void setPrintFiles(Collection<COOrderPrintFileDetails> printFiles)
	{
		this.printFiles = printFiles;
	}

	public COOrderListFileDetails getListFiles()
	{
		return listFiles;
	}

	public void setListFiles(COOrderListFileDetails listFiles)
	{
		this.listFiles = listFiles;
	}
	//CP-12605 - 2.2 OS - XERT Dashboard
	public String getEmailStatistics()
	{
		return emailStatistics;
	}

	public void setEmailStatistics(String emailStatistics)
	{
		this.emailStatistics = emailStatistics;
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

	//CAP-20954
	public boolean isHasProof() {
		return hasProof;
	}

	public void setHasProof(boolean hasProof) {
		this.hasProof = hasProof;
	}
}

