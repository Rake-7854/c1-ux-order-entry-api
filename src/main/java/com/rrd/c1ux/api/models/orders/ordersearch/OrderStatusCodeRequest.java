package com.rrd.c1ux.api.models.orders.ordersearch;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderStatusCodeRequest", description = "Request Class for order status code", type = "object")
public class OrderStatusCodeRequest {
	@Schema(name ="type", description = "Status type", type = "string", example="S", allowableValues = {"S", "A"})
	@Size(min=0, max=1)
	private String type;
}
