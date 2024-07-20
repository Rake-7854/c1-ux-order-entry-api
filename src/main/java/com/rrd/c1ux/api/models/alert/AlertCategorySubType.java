package com.rrd.c1ux.api.models.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlertCategorySubType", description = "Alert Category Sub Type object", type = "object")
public class AlertCategorySubType {
	
	@Schema(name ="categorySubTypeDesc", description = "Category Sub Type Description", type = "String", example="Backordered")
	public String categorySubTypeDesc;
	
	@Schema(name ="categorySubTypeCount", description = "Category Sub Type Count", type = "int", example="20")
	public int categorySubTypeCount;
	
	@Schema(name ="categorySubTypelink", description = "Category Sub Type Link", type = "String", example="https://custompoint.rrd.com/cp/home/home.cp")
	public String categorySubTypelink;
}
