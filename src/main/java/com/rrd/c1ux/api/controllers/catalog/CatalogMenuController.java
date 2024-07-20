/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	04/05/22	Satishkumar Abburi	 CP-33689	Initial creation
 *	04/11/22	Satishkumar Abburi	 CP-33689	Post Branch check-in, Alex has given few feedbacks which is being addressed
 *	06/01/22	Krishna Natarajan	 CP-34331	Added another method "getCatalogMenuForPrime()" to adapt response structure for PrimeNG
 *	08/29/22	A Boomker			CAP-35537	Make session optional on all API calls
 *	09/27/22	A Boomker			CAP-35610	Fixing menu check 
 *  10/18/22    Sakthi M 			CAP-36216	Change existing catalog/menu API to get take search term and selected 
 *  											category and return widget header/subcategories/breadcrumbs
 *  04/19/23	Sakthi M			CAP-39245	Modify Navigation Menu API (the one used) to refactor generation and to make/use new translation text values
 *  06/07/23    Sakthi M			CAP-39209	API Change - Modify Navigation Menu API (the one used) to add Admin Tools options
 */
package com.rrd.c1ux.api.controllers.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalog.CatalogMenuProcessor;
import com.rrd.c1ux.api.models.catalog.CatalogRequest;
import com.rrd.c1ux.api.models.catalog.CatalogTreeResponse;
import com.rrd.c1ux.api.models.catalog.mappers.CatalogMenuMapper;
import com.rrd.c1ux.api.services.catalogforprime.CatalogForPrimeService;
import com.rrd.c1ux.api.services.catalogmenu.CatalogMenuService;
import com.rrd.c1ux.api.services.navimenu.CatalogMenuWithNavigationMenu;
import com.rrd.c1ux.api.services.navimenu.NavigationMenuService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("CatalogMenuController")
public class CatalogMenuController extends BaseCPApiController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogMenuController.class);
	private CatalogMenuMapper mCatalogMenuMapper;
	CatalogForPrimeService catalogForPrimeService;
	NavigationMenuService navMenuService;
	
	@Autowired
	private CatalogMenuService mCatalogMenuService;
	

    protected CatalogMenuController(
        TokenReader tokenReader, 
        CPSessionReader sessionReader,
        CatalogMenuMapper catalogMenuMapper,
			CatalogForPrimeService sCatalogForPrimeService,
			NavigationMenuService snavMenuService,
			CatalogMenuService catalogMenuService
			
    ) {
        super(tokenReader, sessionReader);
        mCatalogMenuMapper = catalogMenuMapper;
        catalogForPrimeService=sCatalogForPrimeService;
        navMenuService=snavMenuService;
        mCatalogMenuService=catalogMenuService;
    }

    /*@Override*/
    protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
    }
    
    
    @PostMapping(value=RouteConstants.CATALOG_MENU, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "catalog/menu")
    @Operation(
        summary = "Get calalog menu")
    public CatalogTreeResponse getCatalogMenu(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid,@RequestBody CatalogRequest request) throws AtWinXSException, CPRPCException {
        logger.debug("In getCatalogMenu");

       SessionContainer sc = getSessionContainer(ttsessionid);
      
       CatalogTreeResponse catalogs= mCatalogMenuService.retrieveCatalogMenuDetails(sc,request);
       return mCatalogMenuMapper.getCatalogMenu(catalogs);	  	 
    }
	
    /**
     * @param ttsessionid {@link String}
     * @return Object of Catalog menu as altered for PrimeNG {@link Object}
     * @throws AtWinXSException
     * @throws CPRPCException
     */
    @GetMapping(value=RouteConstants.CATALOG_MENU_FOR_PRIME, 
    		    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Tag(name = "catalog/menuforprime")
    @Operation(
        summary = "Get calalog modified for prime")
    public List<Object> getCatalogMenuForPrime(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid) throws AtWinXSException, CPRPCException {
        logger.debug("In getCatalogMenuForPrime");
       
       SessionContainer sc = getSessionContainer(ttsessionid);
       CatalogMenuProcessor catalogMenuProcessor = new CatalogMenuProcessor();
       Collection<TreeNodeVO> calalogs= catalogMenuProcessor.retrieveCatalogMenuDetails(sc);
       return catalogForPrimeService.getCatalogMenuForPrime(calalogs);
    }
    
    /**
     * @param ttsessionid {@link String}
     * @return Object of Catalog menu as altered for PrimeNG {@link Object}
     * @throws AtWinXSException
     * @throws CPRPCException
     */
    @GetMapping(value=RouteConstants.NAVI_MENU_AND_CATALOG_MENU_FOR_PRIME,
    		 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Tag(name = "catalog/naviandcatalogmenuforprime")
    @Operation(
        summary = "Get calalog menu and catalog modified for prime")
    public List<CatalogMenuWithNavigationMenu> getNaviMenuAndCatalogMenuForPrime(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid) throws AtWinXSException, CPRPCException {
        logger.debug("In getNaviMenuAndCatalogMenuForPrime");
       
       SessionContainer sc = getSessionContainer(ttsessionid);
       CatalogMenuProcessor catalogMenuProcessor=null;
       Collection<TreeNodeVO> catalogs=null;
       ArrayList<Object>catalogMenu =null;
       catalogMenuProcessor = new CatalogMenuProcessor();
       boolean aMenuTools=checkAdminRedirectAllowed(sc);
       try {
       catalogs= catalogMenuProcessor.retrieveCatalogMenuDetails(sc);
       catalogMenu= catalogForPrimeService.getCatalogMenuForPrime(catalogs);
       }catch(Exception ex){
    	   logger.info("Catalog menu is not loaded");
       }
       //CAP-39245
       return navMenuService.buildMenu(catalogMenu,sc,aMenuTools);
    }

}
