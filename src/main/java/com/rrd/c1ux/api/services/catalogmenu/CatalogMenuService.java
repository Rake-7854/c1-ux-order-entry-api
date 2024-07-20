/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/27/22	A Boomker			CAP-35610	Fixing menu check 
 */
package com.rrd.c1ux.api.services.catalogmenu;

import java.util.Collection;

import com.rrd.c1ux.api.models.catalog.CatalogRequest;
import com.rrd.c1ux.api.models.catalog.CatalogTreeResponse;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.rrd.custompoint.gwt.catalog.entity.CatalogTree;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;

public interface CatalogMenuService {
	public CatalogTreeResponse retrieveCatalogMenuDetails(SessionContainer sc,CatalogRequest request) throws AtWinXSException, CPRPCException;
	public Collection<TreeNodeVO> getMergedCatalogCategories(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, VolatileSessionBean volatileSessionBean);
	public void setSelectedCategory(CatalogSearchResultsCriteria searchCriteria,OEItemSearchCriteriaSessionBean searchCriteriaBean, 
			OEOrderSessionBean oeSessionBean,AppSessionBean appSessionBean,Collection<TreeNodeVO> categories) throws AtWinXSException;
	public Collection<TreeNodeVO> getPunchoutCategories(String aisleName, Collection<TreeNodeVO> categories, CatalogSearchResultsCriteria searchCriteria);
	public CatalogTree genTree(CatalogTree tree, Collection<TreeNodeVO> categories, int selectedCategoryId);
}
