package com.rrd.c1ux.api.models.orders.ordersearch;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderSearchScope", description = "Order Search Scope objects with lable and values", type = "object")
public class COOrderSearchScope {
	@Schema(name ="scopeLabel", description = "Limits the orders returned to a specific visibility related to the user, like My Orders Only, Team Sharing, or All.", type = "string", example="My Orders Only", allowableValues = { "All", "My Orders Only", "Team Sharing"})
	@Size(min=0)
	private String scopeLabel;
	@Schema(name ="scopeValue", description = "Limits the orders returned to a specific visibility related to the user, like M, T, or A.", type = "string", example="M", allowableValues = { "A", "M", "T"})
	@Size(min=1, max=1)
	private String scopeValue;

}
