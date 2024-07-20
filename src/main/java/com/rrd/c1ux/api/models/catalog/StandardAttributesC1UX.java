/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/18/24				S Ramachandran			CAP-46304					Retrieve standard options to use for the Catalog Page
 */

package com.rrd.c1ux.api.models.catalog;

import java.util.ArrayList;
import java.util.Collection;

import com.rrd.c1ux.api.models.standardattributes.COFeaturedSearchCriteria;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class StandardAttributesC1UX {
	
	@Schema(name ="showFavorites", description = "Show favorites flag. Default is false.", type = "boolean")
	boolean showFavorites = false;
	@Schema(name ="showNewItems", description = "Show new items flag. Default is false.", type = "boolean")
	boolean showNewItems = false;
	@Schema(name ="showFeatured", description = "Show featured flag. Default is false.", type = "boolean")
	boolean showFeatured = false;
	@Schema(name ="showHot", description = "Show hot flag. Default is false.", type = "boolean")
	boolean showHot = false;
	@Schema(name ="showNew", description = "Show new flag. Default is false.", type = "boolean")
	boolean showNew = false;
	@Schema(name ="showEDeliveryMethod", description = "Show EDelivery method flag. Default is false.", type = "boolean")
	boolean showEDeliveryMethod = false;
	@Schema(name ="showHasPDFPreview", description = "Show Has PDF preview method flag. Default is false.", type = "boolean")
	boolean showHasPDFPreview = false;
	@Schema(name ="showInStock", description = "Show in stock flag. Default is false.", type = "boolean")
	boolean showInStock = false;
	@Schema(name ="showPrintOnDemand", description = "Show print on demand flag. Default is false.", type = "boolean")
	boolean showPrintOnDemand = false;
	@Schema(name ="showBackOrdered", description = "Show back ordered flag. Default is false.", type = "boolean")
	boolean showBackOrdered = false;
	
	@Schema(name ="favoritesLabel", description = "Favorites label. Default is empty.", type = "string")
	String favoritesLabel = "";
	@Schema(name ="newItemsLabel", description = "New items label. Default is empty.", type = "string")
	String newItemsLabel = "";
	@Schema(name ="featuredLabel", description = "Featured label. Default is empty.", type = "string")
	String featuredLabel = "";
	@Schema(name ="hotLabel", description = "Hot label. Default is empty.", type = "string")
	String hotLabel = "";
	@Schema(name ="newLabel", description = "New label. Default is empty.", type = "string")
	String newLabel = "";
	@Schema(name ="hasPDFPreviewLabel", description = "Has PDF Preview label. Default is empty.", type = "string")
	String hasPDFPreviewLabel = "";
	@Schema(name ="inStockLabel", description = "InStock label. Default is empty.", type = "string")
	String inStockLabel = "";
	@Schema(name ="printOnDemandLabel", description = "Print On Demand label. Default is empty.", type = "string")
	String printOnDemandLabel = "";
	@Schema(name ="backOrderedLabel", description = "Back Ordered label. Default is empty.", type = "string")
	String backOrderedLabel = "";
	@Schema(name ="eDeliveryMethodLabel", description = "EDelivery Method label. Default is empty.", type = "string")
	String eDeliveryMethodLabel = "";

	@Schema(name ="FilterFavorites", description = "Filter Favorites flag. Default is false.", type = "boolean")
	boolean filterFavorites = false;
	@Schema(name ="filterNewItems", description = "Filter New items flag. Default is false.", type = "boolean")
	boolean filterNewItems = false;
	@Schema(name ="filterFeatured", description = "Filter Featured flag. Default is false.", type = "boolean")
	boolean filterFeatured = false;
	@Schema(name ="filterHot", description = "Filter Hot flag. Default is false.", type = "boolean")
	boolean filterHot = false;
	@Schema(name ="filterNew", description = "Filter New flag. Default is false.", type = "boolean")
	boolean filterNew = false;
	@Schema(name ="filterEDeliveryMethod", description = "Flag indicating Filter EDelivery Method flag. Default is false.", type = "boolean")
	boolean filterEDeliveryMethod = false;
	@Schema(name ="filterHasPDFPreview", description = "Filter Has PDF Preview flag. Default is false.", type = "boolean")
	boolean filterHasPDFPreview = false;
	@Schema(name ="filterInStock", description = "Filter InStock flag. Default is false.", type = "boolean")
	boolean filterInStock = false;
	@Schema(name ="filterPrintOnDemand", description = "Filter Print OnDemand flag. Default is false.", type = "boolean")
	boolean filterPrintOnDemand = false;
	@Schema(name ="filterBackOrdered", description = "Filter Back Ordered flag. Default is false.", type = "boolean")
	boolean filterBackOrdered = false;

	@Schema(name ="featuredSearchCriteria", description = "List of featured search criteria", type = "array")
	Collection<COFeaturedSearchCriteria> featuredSearchCriteria = new ArrayList<>();
}

