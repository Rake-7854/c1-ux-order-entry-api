/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	06/01/22	Krishna Natarajan	 CP-34331	implementation for "CatalogMenuController - getCatalogMenuForPrime()" to adapt response structure for PrimeNG
 *  09/14/22	Krishna Natarajan	 CP-35165	made changes for adaptability string amaconcatenator is added
 *  09/30/22	A Boomker			 CAP-36360	Fix logic around initializing concatenator
 *  10/27/22	Sumit Kumar			 CAP-36215	Add 3rd level of the sub category menu
 *  
 */
package com.rrd.c1ux.api.services.catalogforprime;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalog.CatalogMenuForPrime;
import com.rrd.c1ux.api.models.catalog.CatalogMenuForPrimeWithSecondLevelChildren;
import com.rrd.c1ux.api.models.catalog.CatalogMenuForPrimeWoChildren;
import com.rrd.c1ux.api.models.catalog.Items;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;

/**
 * @author Krishna Natarajan
 *
 */
@Service
public class CatalogForPrimeServiceImpl implements CatalogForPrimeService {

	@SuppressWarnings("unchecked")
	public ArrayList<Object> getCatalogMenuForPrime(Collection<TreeNodeVO> calalogs) {
		CatalogMenuForPrime catalogMenuForPrime;
		CatalogMenuForPrimeWoChildren catalogforPrimeWoChildred;
		ArrayList<Object> objects = new ArrayList<>();
		String amaconcatenator = "shopbycatalog";
		for (TreeNodeVO calalog : calalogs) {
			if (calalog.getChildren().isEmpty()) {
				catalogforPrimeWoChildred = new CatalogMenuForPrimeWoChildren();
				catalogforPrimeWoChildred.setId(calalog.getNodeID());
				catalogforPrimeWoChildred.setLabel(calalog.getNodeName());
				catalogforPrimeWoChildred
						.setRouterLink(amaconcatenator + RouteConstants.FOWARD_SLASH + calalog.getNodeID());
				objects.add(catalogforPrimeWoChildred);
			} else {
				catalogMenuForPrime = new CatalogMenuForPrime();

				catalogMenuForPrime.setId(calalog.getNodeID());
				catalogMenuForPrime.setLabel(calalog.getNodeName());
				catalogMenuForPrime.setRouterLink(amaconcatenator + RouteConstants.FOWARD_SLASH + calalog.getNodeID());
				ArrayList<CatalogMenuForPrimeWithSecondLevelChildren> childarraywithincatalogs = new ArrayList<>();
				ArrayList<TreeNodeVO> children = calalog.getChildren();
				CatalogMenuForPrimeWithSecondLevelChildren childitems;
				for (TreeNodeVO loopchildren : children) {
					childitems = new CatalogMenuForPrimeWithSecondLevelChildren();
					childitems.setId(loopchildren.getNodeID());
					childitems.setLabel(loopchildren.getNodeName());
					childitems.setEscape(true);
					childitems.setRouterLink(amaconcatenator + RouteConstants.FOWARD_SLASH + loopchildren.getNodeID());
					ArrayList<Items> itemsOfItem =null;
					//CAP-36215
					if (loopchildren.getChildren().size() > 0) {
						itemsOfItem= new ArrayList<>();
						ArrayList<TreeNodeVO> childOfChild = loopchildren.getChildren();
						Items innerchilditems;
						for (TreeNodeVO innerloopchildren : childOfChild) {
							innerchilditems = new Items();
							innerchilditems.setId(innerloopchildren.getNodeID());
							innerchilditems.setLabel(innerloopchildren.getNodeName());
							innerchilditems.setEscape(true);
							innerchilditems.setRouterLink(
									amaconcatenator + RouteConstants.FOWARD_SLASH + innerloopchildren.getNodeID());
							itemsOfItem.add(innerchilditems);
						}
					}

					childitems.setItems(itemsOfItem);
					childarraywithincatalogs.add(childitems);
				}
				catalogMenuForPrime.setItems(childarraywithincatalogs);
				objects.add(catalogMenuForPrime);
			}
		}
		return objects;
	}
}