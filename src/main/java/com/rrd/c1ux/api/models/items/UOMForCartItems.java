package com.rrd.c1ux.api.models.items;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UOMForCartItems {
	String vendorItemNumber;
	String customerItemNumber;
	String lineNumeber;
	List<UOMItems> uomList;
}
