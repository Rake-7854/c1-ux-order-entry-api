package com.rrd.c1ux.api.models.singleitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemAllocationOrder {
	
	@Schema(name ="salesRefNumber", description = "Sales Reference Number", type = "string", example="80030653")
	private String salesRefNumber;
	
	@Schema(name ="orderDate", description = "Ordered submitted Date/Time in the format yyyy-MM-dd hh:mm:ss", 
			type = "string", example="2024-12-02 04:30:00")
	private String orderDate;
	
	@Schema(name ="orderDateDisplay", description = "Ordered submitted in formatted for User's default locale", 
			type = "string", example="2024-12-01")
	private String orderDateDisplay;
	
	@Schema(name ="quantityOrdered", description = "Quantity Ordered in UOM factor Each of 1 in formatted in ###,###,###", 
			type = "string", example="15")
	private String quantityOrdered;
	
	@Schema(name ="requestorName", description = "Requestor's Name", type = "string", example="80030653")
	private String requestorName;
}
