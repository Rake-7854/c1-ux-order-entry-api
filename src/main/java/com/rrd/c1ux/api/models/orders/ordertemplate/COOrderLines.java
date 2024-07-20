package com.rrd.c1ux.api.models.orders.ordertemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class COOrderLines {

	@Schema(name = "lineNumber", description = "Line Number", type = "String", example = "32432")
	private String lineNumber;
	
	@Schema(name = "itemDescription", description = "Item Description", type = "String", example = "Test Desc")
	private String itemDescription;
	
	@Schema(name = "vendorItem", description = "Vendor Item", type = "String", example = "test item")
	private String vendorItem;
	
	@Schema(name = "customerItemNumber", description = "Customer Item Number", type = "String", example = "211231")
	private String customerItemNumber;
	
	@Schema(name = "quantity", description = "Quantity", type = "int", example = "2")
	private int quantity;
	
	@Schema(name = "uom", description = "Unit of Measure", type = "String", example = "CT")
	private String uom;
}
