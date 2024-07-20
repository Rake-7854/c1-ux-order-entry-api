package com.rrd.c1ux.api.models.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogRequest {
	public String categoryIDPassed = "";
	public String searchTermPassed = ""; 
	public Boolean searching = false;
}
