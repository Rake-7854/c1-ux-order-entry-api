package com.rrd.c1ux.api.models.orders.ordersearch;

import java.io.Serializable;
import java.util.ArrayList;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderSearchRequest", description = "Request Class for order status search", type = "object")
public class COOrderSearchRequest implements Serializable {
	private static final long serialVersionUID = -6394009747625356231L;

	@Schema(name ="searchCriteriaRequest", description = "Set of none to many criteria objects with values", type = "array")
	ArrayList<COSharedSearchCriteriaRequest> searchCriteriaRequest;
	@Schema(name ="fromDate", description = "Earliest date to include in search results. Date should be in MM/DD/YYYY format only.", type = "string", example="01/25/2023")
	@Size(min=0, max=10)
	String fromDate;
	@Schema(name ="toDate", description = "Latest date to include in search results. Date should be in MM/DD/YYYY format only.", type = "string", example="01/27/2023")
	@Size(min=0, max=10)
	String toDate;
	@Schema(name ="scope", description = "Limits the orders returned to a specific visibility related to the user, like My Orders Only (M), or All (A)", type = "string", example="M", allowableValues = {"", "A", "M"})
	@Size(min=0, max=1)
	String scope;
	@Schema(name ="recentOrderSearch", description = "Flag indicating if this should return recent orders, not search results. This takes priority over search criteria passed if true. Default is false.", type = "boolean")
	boolean recentOrderSearch = false;
	@Schema(name ="searchForWidgetDisplay", description = "Flag indicating if this should return up to the widget limit only. Default is false.", type = "boolean")
	boolean searchForWidgetDisplay = false;
	@Schema(name ="repeatSearch", description = "Flag indicating if this should return orders from XST092 cache, not search results. This takes priority over search criteria and recentOrderSearch passed if true. Default is false.", type = "boolean", example = "false")
	boolean repeatSearch = false;
}
