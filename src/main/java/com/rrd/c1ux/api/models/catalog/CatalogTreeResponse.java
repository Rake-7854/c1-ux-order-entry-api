/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				A Salcedo				CAP-39524					Added translationCatalogSearchNav.
 */
package com.rrd.c1ux.api.models.catalog;

import java.util.Map;

import com.rrd.custompoint.gwt.catalog.entity.CatalogTree;

public class CatalogTreeResponse {
	public String searchTerm="";
	public String categoryName="";
	public String categoryID="";
	public CatalogTree categoryTreeBranch;
	
	//CAP-38706
	private Map<String, String> translationCatalogMenu;
	private Map<String, String> translationCatalogLine;
	private Map<String, String> translationCatalogGrid;
	private Map<String, String> translationCatalogSearchNav;//CAP-39524
	
	public String getSearchTerm() {
		return searchTerm;
	}
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	public CatalogTree getCategoryTreeBranch() {
		return categoryTreeBranch;
	}
	public void setCategoryTreeBranch(CatalogTree categoryTreeBranch) {
		this.categoryTreeBranch = categoryTreeBranch;
	}
	public Map<String, String> getTranslationCatalogMenu() {
		return translationCatalogMenu;
	}
	public void setTranslationCatalogMenu(Map<String, String> translationCatalogMenu) {
		this.translationCatalogMenu = translationCatalogMenu;
	}
	public Map<String, String> getTranslationCatalogLine() {
		return translationCatalogLine;
	}
	public void setTranslationCatalogLine(Map<String, String> translationCatalogLine) {
		this.translationCatalogLine = translationCatalogLine;
	}
	public Map<String, String> getTranslationCatalogGrid() {
		return translationCatalogGrid;
	}
	public void setTranslationCatalogGrid(Map<String, String> translationCatalogGrid) {
		this.translationCatalogGrid = translationCatalogGrid;
	}
	public Map<String, String> getTranslationCatalogSearchNav() {
		return translationCatalogSearchNav;
	}
	public void setTranslationCatalogSearchNav(Map<String, String> translationCatalogSearchNav) {
		this.translationCatalogSearchNav = translationCatalogSearchNav;
	}
	
	
	
}
