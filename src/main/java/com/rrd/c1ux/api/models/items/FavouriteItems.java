/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	04/08/22	Krishna Natarajan	CAP-33867	    Created as per the requiremet to fetch the favorite items
 */

package com.rrd.c1ux.api.models.items;

import java.util.Date;

import com.wallace.atwinxs.catalogs.vo.AlternateCatalogData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@author Krishna Natarajan
 *
 *This is a bean to hold and give back item detail
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteItems {
	private AlternateCatalogData altCatData;
	private int catalogID;
	private int catalogLineNum;
	private int categoryID;
	private String description;
	private String itemMedImgLocURL;
	private String itemNum;
	private String wcsItemNum;
	private String classification;
	private String reasonNotOrderableTxt;
	private Date notOrderableDt;
	private String replaceItemNum;
	private String primaryCategorization;
	private String secondaryCategorization;
	private String AddToCartAnchorAndIcon;
}
