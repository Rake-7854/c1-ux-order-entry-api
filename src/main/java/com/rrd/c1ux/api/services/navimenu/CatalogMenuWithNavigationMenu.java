/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				DTS#						Description
 * 	--------	-----------				-----------------------		--------------------------------
 *  08/25/23	Krishna Natarajan		CAP-43281 					Added boolean field isshopbycatalog with getters and setters
 */
package com.rrd.c1ux.api.services.navimenu;

import java.util.List;

public class CatalogMenuWithNavigationMenu {
	protected int id;
	protected String label;
	protected String routerLink;
	protected String styleClass;
	protected List<Object> items;
	protected boolean displayShopByCatalog=false;

	public boolean isDisplayShopByCatalog() {
		return displayShopByCatalog;
	}
	public void setDisplayShopByCatalog(boolean displayShopByCatalog) {
		this.displayShopByCatalog = displayShopByCatalog;
	}
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
	public List<Object> getItems() {
		return items;
	}
	public void setItems(List<Object> catalogs) {
		this.items = catalogs;
	}
	public String getStyleClass() {
		return styleClass;
	}
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
}
