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

import java.util.List;
import java.util.Map;

public interface ItemRolloverPopupData
{
	String getImgURL();
	String getItemNumber();
	String getVendorItemNumber();
	String getItemDescription();
	Map<String, String> getUomOptions();
	double getPrice();
	int getCatalogLineNumber();
	String getSelectedUom();
	boolean isKitTemplateMode();
	int getKitContainerLocations();
	boolean isAllowFavorites();
	boolean isFavorite();
	boolean isCheckInventoryEnabled();
	boolean isManageItemsEnabled();
	boolean isEdocEnabled();
	String getEdocUrl();
	String getFileDeliveryOptionCd();
	String getSpecialItemTypeCd();
	boolean isItemAlwaysRoute();
	int getItemRouteQuantity();
	boolean isAlwaysRoute(); //Make sure we have approval queue ID assigned for this?
	int getRouteQuantity();
	boolean isShowVendorItemNum();
	String getItemClassification();
	Map<String, String> getFeatureMap();
	boolean isItemInCart();
	int getItemInKitInd();
	boolean isItemOrderable();
	boolean isViewOnlyFlag();
	boolean isInvalidItemFlag();
	boolean isAllowDupCustDoc();
	String getItemQuantity();
	boolean isDisplayQuantityAsText();
	boolean isAllowEFD();
	boolean isAllowPrintOverride();
	boolean isShowRAInfo(); //added logic to determine if we show r&A icons based on settings (combine logic: userSettings.isShowRoutingInfo()&&userSettings.isRoutingAvailable()&&userSettings.isSubjToRnA()) && !isKitTemplateMode)
	boolean isShowPrice();
	boolean isShowAvailability();
	boolean isStaticData(); // CP-10235/CP-10216 EZL
	String getRenderLocation(); // CP-10066 EZL
	
	//CP-9054
	String getRoutingMessageReasonsText();
	double getRouteDollarAmount();
	String getRouteDollarAmountText();
	boolean isRouteOnShipMethodChange();
	String getRouteOnShipMethodChangeText();
	boolean isAlwaysRouteOrders();
	String getAlwaysRouteOrdersText();
	//CP-9635
	String getReplacementItemNumber();
	String getUnorderableDate();
	String getUnorderableReason();
	boolean isShowAdditionalItemStatInfo();
	//CAP-2774
	String getCategorizationAttribVal();
	// CAP-15474
	List<String> getKitContainerLocationNames();
	// CAP-16158 
	String getCustomColumnData(int index);
	// CAP-27678
	public String getLargeImageURL();
	public void setLargeImageURL(String largeImageURL);
}
