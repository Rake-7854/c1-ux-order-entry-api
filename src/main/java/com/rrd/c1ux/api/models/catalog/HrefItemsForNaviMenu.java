/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#						Description
 * 	--------	-----------				-----------------------		----------------------------------------
 *	05/19/23    S Ramachandran			CAP-39973       			Added Href Item in PrimeNG object notation
 */

package com.rrd.c1ux.api.models.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="HrefItemsForNaviMenu", description = "Class to specify PrimeNG Sub Menu Item with external links and target", type = "object")
public class HrefItemsForNaviMenu {
	
	@Schema(name ="id", description = "id", type = "int", example="1")
	protected int id;
	
	@Schema(name ="label", description = "Sub Menu Item Label in locale Translation", type = "String", example="Quick Start Guide")
	protected String label;
	
	@Schema(name ="url", description = "External linked URL or Document", type = "String", example="https://www.rrd.com/c1-storefront/guide/index.html")
	protected String url;
	
	@Schema(name ="target", description = "Specifies where to open the linked document", type = "String", example="_blank")
	protected String target;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

}
