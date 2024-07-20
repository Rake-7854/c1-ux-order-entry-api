package com.rrd.c1ux.api.models.standardattributes;

import java.util.ArrayList;
import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="StandardAttributesResponse", description = "Response Class for Standard Attributes", type = "object")
public class StandardAttributesResponse extends BaseResponse {
	@Schema(name ="showFavorites", description = "Flag indicating show favorites. Default is false.", type = "boolean")
	 boolean showFavorites = false;
	 @Schema(name ="showFeatured", description = "Flag indicating show featured. Default is false.", type = "boolean")
	 boolean showFeatured = false;
	 @Schema(name ="showHot", description = "Flag indicating show hot. Default is false.", type = "boolean")
	 boolean showHot = false;
	 @Schema(name ="showNew", description = "Flag indicating show new. Default is false.", type = "boolean")
	 boolean showNew = false;
	 @Schema(name ="showNewItems", description = "Flag indicating show new items. Default is false.", type = "boolean")
	 boolean showNewItems = false;
	 @Schema(name ="showEDeliveryMethod", description = "Flag indicating show EDelivery method. Default is false.", type = "boolean")
	 boolean showEDeliveryMethod = false;
	 @Schema(name ="showHasPDFPreview", description = "Flag indicating show has PDF preview method. Default is false.", type = "boolean")
	 boolean showHasPDFPreview = false;
	 @Schema(name ="showInStock", description = "Flag indicating show in stock. Default is false.", type = "boolean")
	 boolean showInStock = false;
	 @Schema(name ="showPrintOnDemand", description = "Flag indicating show print on demand. Default is false.", type = "boolean")
	 boolean showPrintOnDemand = false;
	 @Schema(name ="showBackOrdered", description = "Flag indicating show back ordered. Default is false.", type = "boolean")
	 boolean showBackOrdered = false;
	 @Schema(name ="favoritesLabel", description = "Flag indicating favorites label. Default is empty.", type = "string")
	 String favoritesLabel = "";
	 @Schema(name ="featuredLabel", description = "Flag indicating featured label. Default is empty.", type = "string")
	 String featuredLabel = "";
	 @Schema(name ="hotLabel", description = "Flag indicating hot label. Default is empty.", type = "string")
	 String hotLabel = "";
	 @Schema(name ="newLabel", description = "Flag indicating new label. Default is empty.", type = "string")
	 String newLabel = "";
	 @Schema(name ="newItemsLabel", description = "Flag indicating new items label. Default is empty.", type = "string")
	 String newItemsLabel = "";
	 @Schema(name ="hasPDFPreviewLabel", description = "Flag indicating has PDF Preview label. Default is empty.", type = "string")
	 String hasPDFPreviewLabel = "";
	 @Schema(name ="inStockLabel", description = "Flag indicating inStock label. Default is empty.", type = "string")
	 String inStockLabel = "";
	 @Schema(name ="printOnDemandLabel", description = "Flag indicating print On Demand label. Default is empty.", type = "string")
	 String printOnDemandLabel = "";
	 @Schema(name ="backOrderedLabel", description = "Flag indicating back Ordered label. Default is empty.", type = "string")
	 String backOrderedLabel = "";
	 @Schema(name ="eDeliveryMethodLabel", description = "Flag indicating eDelivery Method label. Default is empty.", type = "string")
	 String eDeliveryMethodLabel = "";
	 @Schema(name ="isFilterFavorites", description = "Flag indicating Filter Favorites. Default is false.", type = "boolean")
	 boolean filterFavorites = false;
	 @Schema(name ="filterFeatured", description = "Flag indicating Filter Featured. Default is false.", type = "boolean")
	 boolean filterFeatured = false;
	 @Schema(name ="filterHot", description = "Flag indicating Filter Hot. Default is false.", type = "boolean")
	 boolean filterHot = false;
	 @Schema(name ="filterNew", description = "Flag indicating Filter New. Default is false.", type = "boolean")
	 boolean filterNew = false;
	 @Schema(name ="filterNewItems", description = "Flag indicating Filter New items. Default is false.", type = "boolean")
	 boolean filterNewItems = false;
	 @Schema(name ="filterEDeliveryMethod", description = "Flag indicating Filter EDelivery Method. Default is false.", type = "boolean")
	 boolean filterEDeliveryMethod = false;
	 @Schema(name ="filterHasPDFPreview", description = "Flag indicating Filter Has PDF Preview. Default is false.", type = "boolean")
	 boolean filterHasPDFPreview = false;
	 @Schema(name ="filterInStock", description = "Flag indicating Filter InStock. Default is false.", type = "boolean")
	 boolean filterInStock = false;
	 @Schema(name ="filterPrintOnDemand", description = "Flag indicating Filter Print OnDemand. Default is false.", type = "boolean")
	 boolean filterPrintOnDemand = false;
	 @Schema(name ="filterBackOrdered", description = "Flag indicating Filter Back Ordered. Default is false.", type = "boolean")
	 boolean filterBackOrdered = false;
	
	 @Schema(name ="featuredSearchCriteria", description = "List of featured search criteria", type = "array")
	 Collection<COFeaturedSearchCriteria> featuredSearchCriteria = new ArrayList<>();
}
