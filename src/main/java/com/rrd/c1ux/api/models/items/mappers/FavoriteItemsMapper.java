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

package com.rrd.c1ux.api.models.items.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rrd.c1ux.api.models.items.FavouriteItems;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;

/**
 * @author Krishna Natarajan
 * An interface to aid mapper action
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoriteItemsMapper {
/**
 * @param items {@link CatalogLineVO}
 * @return FavouriteItems
 */
FavouriteItems fromVO(CatalogLineVO items);
}
