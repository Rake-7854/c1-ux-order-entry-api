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
 *  04/30/22    S Ramachandran  CAP-33686   Initial Creation 
 *  05/02/22    S Ramachandran  CAP-34043   Cloned CP Classes,Interface to C1 to detach GWT dependency
 *  09/18/23	Krishna Natarajan	CAP-43967 added variable to set and get FeatureFavoriteItemData
 */

package com.rrd.c1ux.api.models.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wallace.atwinxs.catalogs.vo.FeatureFavoriteItemData;

public class SearchResult extends SimpleThumbnail implements ItemThumbnailCellData 
{
	private String itemNumber;
	private String vendorItemNumber;
	private String itemDescription;
	private String cartImgURL;
	private double price;
	Map<String, String> uomOptions;
	private int catalogLineNumber;
	private String selectedUOM;
	private boolean isKitTemplateMode;
	private int kitContainerLocations;
	private boolean allowFavorites = true;
	private boolean favorite = true;
	private String itemClassification;
	private boolean isCheckInventoryEnabled;
	private boolean isManageItemsEnabled;
	private boolean isEdocEnabled;
	private String edocUrl;
	private String fileDeliveryOptionCd;
	private String specialItemTypeCd;
	private boolean isItemAlwaysRoute;
	private int itemRouteQuantity;
	private boolean isAlwaysRoute;
	private int routeQuantity;
	private boolean isShowVendorItemNum;
	private Map<String, String> featureMap;
	private boolean isItemInCart;
	private int itemInKitInd;
	private boolean isItemOrderable;
	private boolean isViewOnlyFlag;
	private boolean isInvalidItemFlag;
	private boolean isAllowDupCustDoc;
	private String itemQuantity;
	private boolean isDisplayQuantityAsText;
	private boolean isAllowPrintOverride;
	private boolean isAllowEFD;
	private boolean isShowRAInfo;
	private boolean showPrice;
	private boolean showAvailability;
	private boolean isStaticData; // CP-10235
	private String renderLocation; // CP-10066
	
	//CP-9054 added new properties for additional routing messages
	private boolean routeOnShipMethodChange = false;
	private double routeDollarAmount;
	private String routeDollarAmountText;
	private String routeOnShipMethodChangeText;
	private boolean alwaysRouteOrders;
	private String alwaysRouteOrdersText;
	private String routingMessageReasonsText;
	
	private int displayOrder = 0;//CP-11027
	
	//CP-9635
	private boolean showAdditionalItemStatInfo;
	private String replacementItemNumber;
	private String unorderableDate;
	private String unorderableReason;
	//CAP-2774
	private String categorizationAttribVal;
	private boolean hasPrimaryAttrib;
	private boolean isDisplayAttrVal;
	// CAP-27678
	private String largeImageURL;
	
	// CAP-15146 - add storage for location names in kits
	private List<String> kitContainerLocationNames;

	// CAP-16158 - add custom values for display here
	List<String> customColumnData = new ArrayList<String>();
	
	//CAP-15083 SRN - Add Additional Fld label, value
	private String additionalFieldLabel = "";
	private String additionalFieldValue = "";
	
	//Added for Min And Max value 
	private double itemMininumOrderQty;
	private double maxinumOrderQty;
	
	private List<UOMItems> uomArrLst; 
	
	Map<String, String>  routingBadge;
	
	FeatureFavoriteItemData featureFavoriteItemData;//CAP-43967

	public FeatureFavoriteItemData getFeatureFavoriteItemData() {
		return featureFavoriteItemData;
	}
	public void setFeatureFavoriteItemData(FeatureFavoriteItemData featureFavoriteItemData) {
		this.featureFavoriteItemData = featureFavoriteItemData;
	}
	public List<UOMItems> getUomArrLst() {
		return uomArrLst;
	}
	public void setUomArrLst(List<UOMItems> uomArrLst) {
		this.uomArrLst = uomArrLst;
	}
	public SearchResult() {}
	public SearchResult(String itemNumber, String itemDescription)
	{
		this.itemNumber = itemNumber;
		this.itemDescription = itemDescription;
	}
	
	public String getItemNumber()
	{
		return itemNumber;
	}
	public void setItemNumber(String itemNumber)
	{
		this.itemNumber = itemNumber;
	}
	
	@Override
	public String getSecondaryText() {
		return getItemNumber();
	}
	@Override
	public void setSecondaryText(String secondaryText) {
		setItemNumber(secondaryText);
	}
	
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	@Override
	public String getPrimaryText() {
		return getItemDescription();
	}
	@Override
	public void setPrimaryText(String primaryText) {
		setItemDescription(primaryText);
	}

	public String getCartImgURL() {
		return cartImgURL;
	}

	public void setCartImgURL(String cartImgURL) {
		this.cartImgURL = cartImgURL;
	}

	@Override
	public Map<String, String> getUomOptions() {
		return uomOptions;
	}
	
	public void setUomOptions(Map<String, String> uomOptions) {
		this.uomOptions = uomOptions;
	}


	@Override
	public double getPrice() {
		return price;
	}
	
	public String getVendorItemNumber() 
	{
		return vendorItemNumber;
	}
	
	public void setVendorItemNumber(String vendorItemNumber) 
	{
		this.vendorItemNumber = vendorItemNumber;
	}
	
	public int getCatalogLineNumber() 
	{
		return catalogLineNumber;
	}
	
	public void setCatalogLineNumber(int catalogLineNumber) 
	{
		this.catalogLineNumber = catalogLineNumber;
	}
	
	public String getSelectedUom()
	{
		return selectedUOM;
	}
	
	public void setSelectedUom(String selectedUOM) 
	{
		this.selectedUOM = selectedUOM;
	}
	
	@Override
	public boolean isKitTemplateMode() 
	{
		return this.isKitTemplateMode;
	}
	
	public void setKitTemplateMode(boolean isKitTemplateMode) 
	{
		this.isKitTemplateMode = isKitTemplateMode;
	}
	
	@Override
	public int getKitContainerLocations() 
	{
		return this.kitContainerLocations;
	}
	
	public void setKitContainerLocations(int kitContainerLocations) 
	{
		this.kitContainerLocations = kitContainerLocations;
	}
	
	@Override
	public boolean isAllowFavorites() 
	{
		return allowFavorites;
	}
	
	public void setAllowFavorites(boolean allowFavorites)
	{
		this.allowFavorites = allowFavorites;
	}
	
	@Override
	public boolean isFavorite() 
	{
		return favorite;
	}
	
	public void setFavorite(boolean favorite) 
	{
		this.favorite = favorite;
	}
	
	@Override
	public boolean isCheckInventoryEnabled() 
	{
		return isCheckInventoryEnabled;
	}
	
	public void setCheckInventoryEnabled(boolean isCheckInventoryEnabled) 
	{
		this.isCheckInventoryEnabled = isCheckInventoryEnabled;
	}
	
	@Override
	public boolean isManageItemsEnabled() 
	{
		return isManageItemsEnabled;
	}
	
	public void setManageItemsEnabled(boolean isManageItemsEnabled) 
	{
		this.isManageItemsEnabled = isManageItemsEnabled;
	}
	
	@Override
	public boolean isEdocEnabled() 
	{
		return isEdocEnabled;
	}
	
	public void setEdocEnabled(boolean isEdocEnabled) 
	{
		this.isEdocEnabled = isEdocEnabled;
	}
	
	@Override
	public String getEdocUrl() 
	{
		return edocUrl;
	}
	
	public void setEdocUrl(String edocUrl) 
	{
		this.edocUrl = edocUrl;
	}
	
	@Override
	public String getFileDeliveryOptionCd() 
	{
		return fileDeliveryOptionCd;
	}
	
	public void setFileDeliveryOptionCd(String fileDeliveryOptionCd) 
	{
		this.fileDeliveryOptionCd = fileDeliveryOptionCd;
	}
	
	@Override
	public String getSpecialItemTypeCd() 
	{
		return specialItemTypeCd;
	}
	
	public void setSpecialItemTypeCd(String specialItemTypeCd) 
	{
		this.specialItemTypeCd = specialItemTypeCd;
	}
	
		
	@Override
	public int getItemRouteQuantity() 
	{
		return itemRouteQuantity;
	}
	
	public void setItemRouteQuantity(int itemRouteQuantity) 
	{
		this.itemRouteQuantity = itemRouteQuantity;
	}
	
		
	@Override
	public int getRouteQuantity() 
	{
		return routeQuantity;
	}
	
	public void setRouteQuantity(int routeQuantity) 
	{
		this.routeQuantity = routeQuantity;
	}
	
	@Override
	public boolean isShowVendorItemNum() 
	{
		return isShowVendorItemNum;
	}
	
	public void setShowVendorItemNum(boolean isShowVendorItemNum) 
	{
		this.isShowVendorItemNum = isShowVendorItemNum;
	}
	
	@Override
	public String getItemClassification() 
	{
		
		return this.itemClassification;
	}
	
	public void setItemClassification(String itemClassification) 
	{
		this.itemClassification = itemClassification;
	}
	
	@Override
	public Map<String, String> getFeatureMap() 
	{
		return featureMap;
	}
	
	public void setFeatureMap(Map<String, String> featureMap) 
	{
		this.featureMap = featureMap;
	}
	
	@Override
	public boolean isItemInCart() 
	{
		return isItemInCart;
	}
	
	public void setItemInCart(boolean isItemInCart) 
	{
		this.isItemInCart = isItemInCart;
	}
	
	@Override
	public int getItemInKitInd() 
	{
		return itemInKitInd;
	}
	
	public void setItemInKitInd(int itemInKitInd) 
	{
		this.itemInKitInd = itemInKitInd;
	}
	
	@Override
	public boolean isItemOrderable() 
	{
		return isItemOrderable;
	}
	
	public void setItemOrderable(boolean isItemOrderable) 
	{
		this.isItemOrderable = isItemOrderable;
	}
	
	@Override
	public boolean isViewOnlyFlag() 
	{
		return isViewOnlyFlag;
	}
	
	public void setViewOnlyFlag(boolean isViewOnlyFlag) 
	{
		this.isViewOnlyFlag = isViewOnlyFlag;
	}
	
	@Override
	public boolean isInvalidItemFlag() 
	{
		return isInvalidItemFlag;
	}
	
	public void setInvalidItemFlag(boolean isInvalidItemFlag) 
	{
		this.isInvalidItemFlag = isInvalidItemFlag;
	}
	
	@Override
	public boolean isAllowDupCustDoc() 
	{
		return isAllowDupCustDoc;
	}
	
	public void setAllowDupCustDoc(boolean isAllowDupCustDoc) 
	{
		this.isAllowDupCustDoc = isAllowDupCustDoc;
	}
	
	@Override
	public String getItemQuantity() 
	{
		return this.itemQuantity;
	}
	
	public void setItemQuantity(String itemQuantity) 
	{
		this.itemQuantity = itemQuantity;
	}
	
	@Override
	public boolean isDisplayQuantityAsText() 
	{
		return this.isDisplayQuantityAsText;
	}
	
	public void setDisplayQuantityAsText(boolean isDisplayQuantityAsText) 
	{
		this.isDisplayQuantityAsText = isDisplayQuantityAsText;
	}
	
	@Override
	public boolean isItemAlwaysRoute() {
		return isItemAlwaysRoute;
	}
	public void setItemAlwaysRoute(boolean isItemAlwaysRoute) {
		this.isItemAlwaysRoute = isItemAlwaysRoute;
	}
	
	@Override
	public boolean isAlwaysRoute() {
		return isAlwaysRoute;
	}
	public void setAlwaysRoute(boolean isAlwaysRoute) {
		this.isAlwaysRoute = isAlwaysRoute;
	}
	
	@Override
	public boolean isAllowPrintOverride() {
		return isAllowPrintOverride;
	}
	public void setAllowPrintOverride(boolean isAllowPrintOverride) {
		this.isAllowPrintOverride = isAllowPrintOverride;
	}
	
	@Override
	public boolean isAllowEFD() {
		return isAllowEFD;
	}
	public void setAllowEFD(boolean isAllowEFD) {
		this.isAllowEFD = isAllowEFD;
	}
	
	@Override
	public boolean isShowRAInfo() {
		return isShowRAInfo;
	}
	public void setShowRAInfo(boolean isShowRAInfo) {
		this.isShowRAInfo = isShowRAInfo;
	}
	@Override
	public boolean isShowPrice() {
		return showPrice;
	}
	public void setShowPrice(boolean showPrice)
	{
		this.showPrice = showPrice;
	}
	
	@Override
	public boolean isShowAvailability() {
		return showAvailability;
	}
	public void setShowAvailability(boolean showAvailability)
	{
		this.showAvailability = showAvailability;
	}
	// CP-10235/CP-10216 
	@Override
	public boolean isStaticData() {
		return isStaticData;
	}
	public void setStaticData(boolean isStaticData)
	{
		this.isStaticData = isStaticData;
	}
	
	// CP-9054 start of additional routing setter/getter
	public boolean isRouteOnShipMethodChange() 
	{
		return routeOnShipMethodChange;
	}

	public void setRouteOnShipMethodChange(boolean routeOnShipMethodChange) 
	{
		this.routeOnShipMethodChange = routeOnShipMethodChange;
	}

	public double getRouteDollarAmount()
	{
		return routeDollarAmount;
	}

	public void setRouteDollarAmount(double routeDollarAmount) 
	{
		this.routeDollarAmount = routeDollarAmount;
	}

	public String getRouteDollarAmountText() 
	{
		return routeDollarAmountText;
	}

	public void setRouteDollarAmountText(String routeDollarAmountText) 
	{
		this.routeDollarAmountText = routeDollarAmountText;
	}

	public String getRouteOnShipMethodChangeText() 
	{
		return routeOnShipMethodChangeText;
	}
	
	public void setRouteOnShipMethodChangeText(String routeOnShipMethodChangeText) 
	{
		this.routeOnShipMethodChangeText = routeOnShipMethodChangeText;
	}
	
	public boolean isAlwaysRouteOrders() 
	{
		return alwaysRouteOrders;
	}
	
	public void setAlwaysRouteOrders(boolean alwaysRouteOrders)
	{
		this.alwaysRouteOrders = alwaysRouteOrders;
	}
	
	public String getAlwaysRouteOrdersText() 
	{
		return alwaysRouteOrdersText;
	}
	
	public void setAlwaysRouteOrdersText(String alwaysRouteOrdersText)
	{
		this.alwaysRouteOrdersText = alwaysRouteOrdersText;
	}
	
	public String getRoutingMessageReasonsText() 
	{
		return routingMessageReasonsText;
	}
	
	public void setRoutingMessageReasonsText(String routingMessageReasonsText) 
	{
		this.routingMessageReasonsText = routingMessageReasonsText;
	}
	// CP-9054 end of additional routing setter/getter
	
	
	// CP-10066
	@Override
	public String getRenderLocation() {
		return renderLocation;
	}
	public void setRenderLocation(String renderLocation)
	{
		this.renderLocation = renderLocation;
	}

	//CP-11027
	@Override
	public int getDisplayOrder()
	{
		return displayOrder;
	}
	public void setDisplayOrder(int displayOrder)
	{
		this.displayOrder = displayOrder;
	}
	//CP-9635
	public String getReplacementItemNumber()
	{
		return replacementItemNumber;
	}
	public void setReplacementItemNumber(String replacementItemNumber)
	{
		this.replacementItemNumber = replacementItemNumber;
	}
	public String getUnorderableDate()
	{
		return unorderableDate;
	}
	public void setUnorderableDate(String unorderableDate)
	{
		this.unorderableDate = unorderableDate;
	}
	public String getUnorderableReason()
	{
		return unorderableReason;
	}
	public void setUnorderableReason(String unorderableReason)
	{
		this.unorderableReason = unorderableReason;
	}
	public boolean isShowAdditionalItemStatInfo()
	{
		return showAdditionalItemStatInfo;
	}
	public void setShowAdditionalItemStatInfo(boolean showAdditionalItemStatInfo)
	{
		this.showAdditionalItemStatInfo = showAdditionalItemStatInfo;
	}
	
	//CAP-2774
	@Override
	public String getCategorizationAttribVal()
	{
		return categorizationAttribVal;
	}
	
	@Override
	public void setCategorizationAttribVal(String categorizationAttribVal)
	{
		this.categorizationAttribVal = categorizationAttribVal;
	}
	
	@Override
	public boolean isHasPrimaryAttrib()
	{
		return hasPrimaryAttrib;
	}
	
	@Override
	public void setHasPrimaryAttrib(boolean hasPrimaryAttrib)
	{
		this.hasPrimaryAttrib = hasPrimaryAttrib;
	}
	
	@Override
	public boolean isDisplayAttrVal()
	{
		return isDisplayAttrVal;
	}
	
	@Override
	public void setDisplayAttrVal(boolean isDisplayAttrVal)
	{
		this.isDisplayAttrVal = isDisplayAttrVal;
	}

	// CAP-15146 - add storage for location names in kits
	public List<String> getKitContainerLocationNames()
	{
		return kitContainerLocationNames;
	}
	public void setKitContainerLocationNames(List<String> kitContainerLocationNames)
	{
		this.kitContainerLocationNames = kitContainerLocationNames;
	}
	
	// CAP-16158 - add custom values for display here
	public List<String> getCustomColumnData()
	{
		return customColumnData;
	}

	public void setCustomColumnData(List<String> customColumnData)
	{
		this.customColumnData = customColumnData;
	}
	
	public String getCustomColumnData(int index)
	{
		if ((customColumnData != null) && (customColumnData.size() > index) && (index >= 0))
		{
			if (customColumnData.get(index) != null)
			{
				return customColumnData.get(index);
			}
			else
			{
				return "";
			}
		}
		else
		{
			return "";
		}
	}
	
	//CAP-15083 SRN Setters and Getters for Additional Field and Additional value
	public String getAdditionalFieldLabel()
	{
		return additionalFieldLabel;
	}
	
	public void setAdditionalFieldLabel(String additionalFieldLabel)
	{
		this.additionalFieldLabel = additionalFieldLabel;
	}
	
	public String getAdditionalFieldValue()
	{
		return additionalFieldValue;
	}
	
	public void setAdditionalFieldValue(String additionalFieldValue)
	{
		this.additionalFieldValue = additionalFieldValue;
	}
	// CAP-27678
	@Override
	public String getLargeImageURL() 
	{
		return largeImageURL;
	}
	
	@Override
	public void setLargeImageURL(String largeImageURL) 
	{
		this.largeImageURL = largeImageURL;
	}
	
	public double getItemMininumOrderQty() {
		return itemMininumOrderQty;
	}
	public void setItemMininumOrderQty(double itemMininumOrderQty) {
		this.itemMininumOrderQty = itemMininumOrderQty;
	}
	public double getMaxinumOrderQty() {
		return maxinumOrderQty;
	}
	public void setMaxinumOrderQty(double maxinumOrderQty) {
		this.maxinumOrderQty = maxinumOrderQty;
	}

	public Map<String, String> getRoutingBadge() {
		return routingBadge;
	}
	public void setRoutingBadge(Map<String, String> routingBadge) {
		this.routingBadge = routingBadge;
	}
	
	
}
