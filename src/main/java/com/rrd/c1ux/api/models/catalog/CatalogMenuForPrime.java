/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *															
 */
package com.rrd.c1ux.api.models.catalog;

import java.util.ArrayList;

public class CatalogMenuForPrime {
	protected int id;
	protected String label;
	protected String routerLink;
	protected ArrayList<CatalogMenuForPrimeWithSecondLevelChildren> items;

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

	public String getRouterLink() {
		return routerLink;
	}

	public void setRouterLink(String routerLink) {
		this.routerLink = routerLink;
	}

	public ArrayList<CatalogMenuForPrimeWithSecondLevelChildren> getItems() {
		return items;
	}

	public void setItems(ArrayList<CatalogMenuForPrimeWithSecondLevelChildren> items) {
		this.items = items;
	}

}
