/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#																				Description
 * 	--------	-----------		-------------------------------------------------------------------------------		--------------------------------
 *	04/20/22	Sakthi M		CAP-33762- Item Detail Pricing/Availability Call (Single Item no tiered pricing)	Initial creation
 */
package com.rrd.c1ux.api.models.notiereditems;

public class PNANoTieredPriceRequest {
	
	private boolean showPrice;
	private boolean showAvailability;
	private String itemNumber;
	private String orderType;
	private int orderQtyEA;
	private String corporateNumber;
	private String soldToNumber;
	private boolean checkJobs;
	private boolean useCSSListPrice;
	private boolean useJLJLSPrice;
	private boolean useCustomersCustomerPrice;
	private boolean useCatalogPrice;
	private int siteID;
	private String customerItemNumber;
	private double lastJLMarkupPct;
	private String promoCode;
	private int rounding;
	private boolean useTPP;
	private String tppClass;
	private boolean component;
	
	public boolean isShowPrice() {
		return showPrice;
	}
	public void setShowPrice(boolean showPrice) {
		this.showPrice = showPrice;
	}
	public boolean isShowAvailability() {
		return showAvailability;
	}
	public void setShowAvailability(boolean showAvailability) {
		this.showAvailability = showAvailability;
	}
	public String getItemNumber() {
		return itemNumber;
	}
	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public int getOrderQtyEA() {
		return orderQtyEA;
	}
	public void setOrderQtyEA(int orderQtyEA) {
		this.orderQtyEA = orderQtyEA;
	}
	public String getCorporateNumber() {
		return corporateNumber;
	}
	public void setCorporateNumber(String corporateNumber) {
		this.corporateNumber = corporateNumber;
	}
	public String getSoldToNumber() {
		return soldToNumber;
	}
	public void setSoldToNumber(String soldToNumber) {
		this.soldToNumber = soldToNumber;
	}
	public boolean isCheckJobs() {
		return checkJobs;
	}
	public void setCheckJobs(boolean checkJobs) {
		this.checkJobs = checkJobs;
	}
	public boolean isUseCSSListPrice() {
		return useCSSListPrice;
	}
	public void setUseCSSListPrice(boolean useCSSListPrice) {
		this.useCSSListPrice = useCSSListPrice;
	}
	public boolean isUseJLJLSPrice() {
		return useJLJLSPrice;
	}
	public void setUseJLJLSPrice(boolean useJLJLSPrice) {
		this.useJLJLSPrice = useJLJLSPrice;
	}
	public boolean isUseCustomersCustomerPrice() {
		return useCustomersCustomerPrice;
	}
	public void setUseCustomersCustomerPrice(boolean useCustomersCustomerPrice) {
		this.useCustomersCustomerPrice = useCustomersCustomerPrice;
	}
	public boolean isUseCatalogPrice() {
		return useCatalogPrice;
	}
	public void setUseCatalogPrice(boolean useCatalogPrice) {
		this.useCatalogPrice = useCatalogPrice;
	}
	public int getSiteID() {
		return siteID;
	}
	public void setSiteID(int siteID) {
		this.siteID = siteID;
	}
	public String getCustomerItemNumber() {
		return customerItemNumber;
	}
	public void setCustomerItemNumber(String customerItemNumber) {
		this.customerItemNumber = customerItemNumber;
	}
	public double getLastJLMarkupPct() {
		return lastJLMarkupPct;
	}
	public void setLastJLMarkupPct(double lastJLMarkupPct) {
		this.lastJLMarkupPct = lastJLMarkupPct;
	}
	public String getPromoCode() {
		return promoCode;
	}
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	public int getRounding() {
		return rounding;
	}
	public void setRounding(int rounding) {
		this.rounding = rounding;
	}
	public boolean isUseTPP() {
		return useTPP;
	}
	public void setUseTPP(boolean useTPP) {
		this.useTPP = useTPP;
	}
	public String getTppClass() {
		return tppClass;
	}
	public void setTppClass(String tppClass) {
		this.tppClass = tppClass;
	}
	public boolean isComponent() {
		return component;
	}
	public void setComponent(boolean component) {
		this.component = component;
	} 


}
