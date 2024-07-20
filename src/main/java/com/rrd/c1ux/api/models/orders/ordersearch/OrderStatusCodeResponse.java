package com.rrd.c1ux.api.models.orders.ordersearch;

import java.util.List;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="OrderStatusCodeResponse", description = "Response class for order status code", type = "object")
public class OrderStatusCodeResponse  extends BaseResponse { 
	@Schema(name ="status", description = "Response status", type = "string", example="Success", allowableValues = {"Failed","Success"})
	private String status;
	@Schema(name ="cosharedOrderStatusCodeList", description = "List of Order status code", type = "array")
	private List<COSharedOrderStatusCode> cosharedOrderStatusCodeList;
}
