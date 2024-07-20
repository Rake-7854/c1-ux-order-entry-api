package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderSearchResultResponse", description = "Response Class for order status search", type = "object")
public class COOrderSearchResultResponse extends BaseResponse {
	@Schema(name ="ordersFound", description = "Total number of orders found meeting criteria up to the limit for the search type", type = "int", example="0")
	@Min(0)
	int ordersFound;

	@Schema(name ="searchResults", description = "List of results meeting the search criteria", type = "array")
	List<COOrderSearchResult> searchResults;
	@Schema(name ="recentOrderSearch", description = "Flag indicating if this should return recent orders, not search results. This takes priority over search criteria passed if true. Default is false.", type = "boolean")
	boolean recentOrderSearch = false;
	
	//CAP-41553
	@Schema(name ="showOrderOptions", description = "Flag indicating if the user has access to copy orders or not. Default is false.", type = "boolean")
	boolean showOrderOptions = false;
	
	//CAP-38707
	@Schema(name ="translationOrderSearch", description = "Messages from \"orderSearch\" translation file will load here.", type = "string",  example="\"translation\": { \"ordersearch_lbl_scope\": \"Scope\"}")
	private Map<String, String> translationOrderSearch;
	@Schema(name ="translationRecentOrder", description = "Messages from \"recentOrder\" translation file will load here.", type = "string",  example="\"translation\": { \"recentOrdersLbl\": \"Recent Orders\"}")
	private Map<String, String> translationRecentOrder;
	@Schema(name ="translationOrderSearch", description = "Messages from \"orderDetails\" translation file will load here.", type = "string",  example="\"translation\": { \"orderDetailsLbl\": \"Order Details\"}")
	private Map<String, String> translationOrderDetails;

}
