package com.rrd.c1ux.api.models.standardattributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class COFeaturedSearchCriteria {
	 int typeID;
	 String label;
	 boolean selected;

}
