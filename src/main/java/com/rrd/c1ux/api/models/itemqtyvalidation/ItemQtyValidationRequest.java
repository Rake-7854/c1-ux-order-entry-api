package com.rrd.c1ux.api.models.itemqtyvalidation;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ItemQtyValidationRequest {
	 String itemNumber;
	 String uomDesc="";
	 String orderQty="";
	 String vendorItemNumber;
}
