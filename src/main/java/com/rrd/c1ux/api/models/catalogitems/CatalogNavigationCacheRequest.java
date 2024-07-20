package com.rrd.c1ux.api.models.catalogitems;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogNavigationCacheRequest {
	String view="";
	String sort="";
	String numShown="";
	String page="";
	String sortOrder="";//CAP-36079

}
