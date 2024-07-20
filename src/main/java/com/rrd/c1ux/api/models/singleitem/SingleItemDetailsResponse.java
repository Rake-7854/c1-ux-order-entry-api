/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		--------------		--------------------------------
 *	09/28/22	A Boomker		CAP-36084			Modify API response for categories so top level cats have label
 *	08/29/23	L De Leon		CAP-43197			Modified API response to add featureMap
 *	09/07/23	Krishna Natarajan		CAP-43656			Added boolean variable allowFavorites 
 *	09/12/23	Krishna Natarajan		CAP-43384			Added Map variable to get the routing messages
 *	05/08/24	M Sakthi				CAP-49015	Added fileDeliveryOption,fileDeliveryLabel list into the response object
 *	06/05/24    S Ramachandran  		CAP-49887   Return components in SingleItemDetailsResponse if the item is a kit template
 *	06/13/24	M Sakthi				CAP-50002	C1UX BE - Return Kit Information for the item details page which include information like container and max/min item counts
 */
package com.rrd.c1ux.api.models.singleitem;



import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rrd.c1ux.api.models.items.UOMItems;
import com.rrd.custompoint.gwt.common.cell.ItemThumbnailCell.ItemThumbnailCellData;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleItemDetailsResponse {

	CatalogItem item=null;
	Map<String, String>  uomList;
	boolean showAddToCartButton;
	boolean isItemInCart;
	boolean isDisplayUOMOptions;
	boolean orderableItem;
	boolean isSuggestedItems;
	boolean showVendorItemNum;
	boolean eDocFlag;
	String edocURL;
	String quantity;
	List<UOMItems> uomArrLst;
	double itemMinimumOrderQty;
	double itemMaximumOrderQty;
	double itemMultiplesOrderQty;
	String itemDescription;
	//CAP-35080
	boolean showOrderLinePrice;
	//CAP-35080
	boolean showOrderLineAvailability;
	//CAP-35080
	String priceLineAvailabilityLbl;
	//CAP-35464
	String returnLinkText;
	String returnLinkURL;
	Collection<CategoryListing> categories;
	
	//CAP-46544
	Collection<MainAttribute> attribute;

	// CAP-43197
	@Schema(name ="featureMap", description = "A map with the key being the mouse-over text and the value being the icon path for SF", type = "string",  example="\"featureMap\": { \"Hot\": \"/icons/png/Icon_Hot_Item.png\"}")
	private Map<String, String> featureMap;

	//CAP-38708
	@Schema(name ="translation", description = "Messages from \"item\" translation file will load here.", type = "string",  example="\"translation\": { \"OrigDateLbl\": \"Original Date\"}")
	private Map<String, String> translation;
	
	//CAP-43656
	@Schema(name ="allowFavorites", description = "Flag to set if the item is allowed to toggle favorite.", type = "boolean",  example="true")
	boolean allowFavorites=false;
	
	//CAP-43384
	@Schema(name ="routingBadge", description = "A map with the key and the value being the routing messages", type = "string",  example="\"routingBadge\": { \"Hot\": \"messages\"}")
	Map<String, String>  routingBadge;
	
	@Schema(name ="quantityAllocationMessage", description = "Display the Quantity allocation Message", type = "string",  example="")
	private String quantityAllocationMessage=AtWinXSConstant.EMPTY_STRING;
	
	//CAP-49015
	private String fileDeliveryOption;
	private String fileDeliveryLabel;
	
	//CAP-38708
	@Schema(name ="componentItems", description = "List of Component Items", type = "array")
	List<ItemThumbnailCellData> componentItems;
	
	//CAP-50002
	private int kitOrderQuantityMin=0;
	private int kitOrderQuantityMax=0;
	private int kitLineItemMin=0;
	private int kitLineItemMax=0;
	private String containerImagePath="";
	private String containerItemDescription="";
	private String containerItemNumber="";
}
