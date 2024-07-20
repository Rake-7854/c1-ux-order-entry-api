package com.rrd.c1ux.api.models.orders.ordersearch;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderFileRequest", description = "Request Class for get the order file email link", type = "object")
public class COOrderFileRequest {
	@Schema(name ="salesRefNum", description = "Sales Reference Number", type = "String", example="80031311")
	@Size(min=0, max=20)
	public String salesRefNum;
	@Schema(name ="orderNumber", description = "Order Number - Optional", type = "String", example="56389739")
	@Size(min=0, max=8)
	public String orderNumber;

}
