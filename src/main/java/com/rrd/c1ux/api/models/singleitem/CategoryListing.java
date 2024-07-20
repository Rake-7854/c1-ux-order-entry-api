/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		--------------		--------------------------------
 *	09/28/22	A Boomker		CAP-36084			Modify API response for categories so top level cats have label
 */
package com.rrd.c1ux.api.models.singleitem;

import java.util.Collection;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

public class CategoryListing {
	String topCat = AtWinXSConstant.EMPTY_STRING;
	Collection<Collection<String>> branches = null;
	public CategoryListing(String cat, Collection<Collection<String>> subList)
	{
		topCat = cat;
		branches = subList;
	}
	public String getTopCat() {
		return topCat;
	}
	public void setTopCat(String topCat) {
		this.topCat = topCat;
	}
	public Collection<Collection<String>> getBranches() {
		return branches;
	}
	public void setBranches(Collection<Collection<String>> branches) {
		this.branches = branches;
	}
}
