package com.rrd.c1ux.api.models.singleitem;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleItemDetailsRequest {

	 String itemNumber="";
	 String vendorItemNumber="";
	 int catalogLnNbr=0;
	 
}
