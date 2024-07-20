package com.rrd.c1ux.api.models.alert;

import java.util.Collection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlertsCategory", description = "Alert Category Object", type = "object")
public class AlertsCategory {
	
	@Schema(name ="categoryName", description = "Alert Category Name", type = "String", example="Order Alerts")
	private String categoryName;
	
	@Schema(name ="categoryDesc", description = "Alert Category Description", type = "String", 
			example="Here are importent notification about item inventory. "
					+ "Clicking the links below will sent to you to the older version of the system.")
	private String categoryDesc;
	
	@Schema(name ="categoryCount", description = "Alert Category Total Count", type = "int", example="40")
	private int categoryCount;
	
	@Schema(name ="categoryIcon", description = "Alert Category Icon Identifier", type = "String", example="Inventory")
	private String categoryIcon;
	
	@Schema(name ="alertCategorySubType", description = "Collection of Alert Category Sub Type objects", type = "array")
	private Collection<AlertCategorySubType> alertCategorySubType;
}
