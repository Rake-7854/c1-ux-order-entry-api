/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/27/22	A Boomker		CAP-35610					Fixing menu check 
 *  10/18/22    Sakthi M 		CAP-36216					Change existing catalog/menu API to get take search term and selected 
 *  														category and return widget header/subcategories/breadcrumbs
 *	10/24/22	A Boomker		CAP-36705					Fixes for category and search
 *  10/31/22    Sakthi M        CAP-36139                   Set parents reverse order as per FE team request
 *  11/07/22	Krishna Natarajan CAP-36999					Handling searchPassed in method retrieveCatalogMenuDetails() 
 *  03/01/23	S Ramachandran	CAP-38706					API Fix - Add Translation to ResponseObject for /api/catalog/menu,
 *  03/29/23	A Salcedo		CAP-39524					Added translation for viewName "catalogSearchNav" in retrieveCatalogMenuDetails().
 */
package com.rrd.c1ux.api.services.catalogmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalog.CatalogRequest;
import com.rrd.c1ux.api.models.catalog.CatalogTreeResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.rrd.custompoint.gwt.catalog.entity.CatalogTree;
import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.catalogs.vo.CategoryVOKey;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class CatalogMenuServiceImpl extends BaseService implements CatalogMenuService {
	
private static final Logger logger = LoggerFactory.getLogger(CatalogMenuServiceImpl.class);

public CatalogMenuServiceImpl(TranslationService translationService) {
    super(translationService);
  }

public CatalogTreeResponse retrieveCatalogMenuDetails(SessionContainer sc,CatalogRequest request) throws AtWinXSException
{        
	
    ApplicationSession appSession = sc.getApplicationSession();
    AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
    OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession(); 
    ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
    OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
    
    VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
    OEItemSearchCriteriaSessionBean searchCriteriaBean =  oeSessionBean.getSearchCriteriaBean();
	PunchoutSessionBean punchoutSessionBean = appSession.getPunchoutSessionBean();

	OrderOnBehalfVO orderOnBehalf = volatileSessionBean.getOrderOnBehalf();
	appSessionBean.setInRequestorMode(orderOnBehalf.isInRequestorMode());
			
	CatalogSearchResultsCriteria searchCriteria = new CatalogSearchResultsCriteria();		
	//searchCriteria.set
	//CP-10769 NMB  Removed unnecessary null checks.	

	logger.debug("getCatalogTree() loading categories");
	
	CatalogTreeResponse response=new CatalogTreeResponse(); //CAP-36216
	
	boolean catIDPassed = !Util.isBlankOrNull(request.getCategoryIDPassed()) && (Util.safeStringToDefaultInt(request.getCategoryIDPassed(), -1) > 0);
	int selectedCategoryID = -1;
	String selectedSearchTerm = AtWinXSConstant.EMPTY_STRING;
	boolean searchPassed = (Boolean.TRUE.equals(request.getSearching()));//CAP-36999
	if (searchPassed) {
		selectedSearchTerm = Util.nullToEmpty(request.getSearchTermPassed());
	}
	int cachedID = (oeSessionBean.getSearchCriteriaBean() != null) ? oeSessionBean.getSearchCriteriaBean().getSelectedCategoryId() : -1;
	String cachedTerm = (oeSessionBean.getSearchCriteriaBean() != null) ? oeSessionBean.getSearchCriteriaBean().getUnifiedSearchCriteria() : null;
	if (!searchPassed) {// CAP-36999
		if (catIDPassed) {
			selectedCategoryID = Util.safeStringToDefaultInt(request.getCategoryIDPassed(), -1);
		} else {
			selectedCategoryID = cachedID;
		}
	}
	//CP-10988 Auto login set selected category.
	//CAP-1260 try to get merged categories from session.  If not there, then look them up from DB and save to session.
	Collection<TreeNodeVO> categories = volatileSessionBean.isKitTemplateMode() ? oeSessionBean.getMergedCategoriesKitTemplateMode() : oeSessionBean.getMergedCategories();

	if(categories == null)
	{
		categories = getMergedCatalogCategories(appSessionBean, oeSessionBean, volatileSessionBean);
		
		if(volatileSessionBean.isKitTemplateMode())
		{
			try
			{ // CAP-2589 if we have no kit session, we should not be in kit template mode
				SessionHandler.loadSession(volatileSession.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
			}
			catch(AtWinXSException eofex)
			{
				// CAP-16460 call to logger.
				logger.error(this.getClass().getName() + " - " + eofex.getMessage(),eofex);
			}
			oeSessionBean.setMergedCategoriesKitTemplateMode(categories);
		}
		else
		{	
			oeSessionBean.setMergedCategories(categories);
		}
	}

	logger.debug("getCatalogTree() done loading categories");

	if (!catIDPassed && !searchPassed && (cachedID > -1))
	{
		if(oeSessionBean.getLastSelectedCatalogId() == -1)
		{
			logger.debug("getCatalogTree() setting selected category");
			try
			{
				setSelectedCategory(searchCriteria, searchCriteriaBean, oeSessionBean, appSessionBean, categories);
			} catch (AtWinXSException ae)
			{
				logger.error("getCatalogTree()", ae);
				throw ae;
			}
			logger.debug("getCatalogTree() done setting selected category");
		}
		
		//CP-12046
		if (oeSessionBean.getDirectCatalog()!=null && !oeSessionBean.getDirectCatalog().isEmpty())
		{
			try
			{
				OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
				assembler.setSelectedCategory(oeSessionBean,
						oeSessionBean.getDirectCatalog(),
						categories,
						appSessionBean.getSiteID(),
						appSessionBean.getBuID());
				searchCriteriaBean.setSelectedCategoryId(oeSessionBean.getLastSelectedCatalogId());
				searchCriteriaBean.getPreviouslySelectedPeers().put(oeSessionBean.getLastSelectedCatalogId(), searchCriteria.getSelectedCategoryPeers());
				searchCriteria.setSelectedParentCategoryID(oeSessionBean.getLastSelectedCatalogId());
				oeSessionBean.setDirectCatalog("");
				
			} catch (AtWinXSException e)
			{
				logger.debug("getCatalogTree()", e);
				oeSessionBean.setDirectCatalog("");
			}
		}
	} 
	else if (catIDPassed) 
	{ 	
		logger.debug("getCatalogTree() setting selected category in search criteria");
		selectedCategoryID = Util.safeStringToDefaultInt(request.getCategoryIDPassed(), -1);
		searchCriteriaBean.setSelectedCategoryId(selectedCategoryID);
		searchCriteriaBean.getPreviouslySelectedPeers().put(selectedCategoryID, searchCriteria.getSelectedCategoryPeers());
		logger.debug("getCatalogTree() done setting selected category in search criteria");
	}
	
	searchCriteriaBean.setSelectedCategoryPeers(searchCriteriaBean.getPreviouslySelectedPeers().get(searchCriteriaBean.getSelectedCategoryId()));

	if (punchoutSessionBean != null && punchoutSessionBean.getPunchoutType() != null
			&& punchoutSessionBean.getPunchoutType().equals(OrderEntryConstants.PUNCHOUT_TYPE_AISLE))
	{
		categories = getPunchoutCategories(Util.nullToEmpty(punchoutSessionBean.getDescription()), categories, searchCriteria); // CP-8971, CP-11669
	}
	
	CatalogTree tree = null;
	
	//CAP-36216-Change existing catalog/menu API to get take search term and selected category and return widget header/subcategories/breadcrumbs

	if(selectedCategoryID > -1)
	{
	   tree = genTree(null, categories, selectedCategoryID);
	   Collections.reverse(tree.getParents());//  CAP-36139 Set parents reverse order as per FE team request
	   response.setCategoryName(tree.getNodeName());
	   response.setSearchTerm(AtWinXSConstant.EMPTY_STRING);
	}
	else
	{
	   tree = new CatalogTree();
	   response.setCategoryName(AtWinXSConstant.EMPTY_STRING);
	   if(RouteConstants.REPEAT_SEARCH_UNIQUE_TERM.equals(selectedSearchTerm)) {//CAP-36999
		   searchPassed=false;
	   }
	   response.setSearchTerm(searchPassed? selectedSearchTerm : cachedTerm);
	}
	response.setCategoryID(String.valueOf(selectedCategoryID));
	response.setCategoryTreeBranch(tree);
	
	//CP-9722 End
	//CP-13415 Using entry point link doesn't set peers for selected ID. If selected cat peers empty, check tree that was built for peers.
	//CP-13508 Tree null check. Search and Adv Search, there is no tree.
	if(searchCriteriaBean.getSelectedCategoryPeers().isEmpty() && tree != null)
	{
		searchCriteriaBean.setSelectedCategoryPeers(tree.getPeers());
	}
	
	// CP-10769
	oeSessionBean.setSearchCriteria(searchCriteria);
	logger.debug("getCatalogTree() persisting session.");
	
	SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
	logger.debug("getCatalogTree() done persisting session."); 
	
	//CAP-38706 - translationCatalogMenu
	Properties catalogMenuBundleProps = translationService.getResourceBundle(appSessionBean, "catalogMenu");
	response.setTranslationCatalogMenu(translationService.convertResourceBundlePropsToMap(catalogMenuBundleProps));
	
	//CAP-38706 - translationCatalogLine
	Properties catalogLineBundleProps = translationService.getResourceBundle(appSessionBean, "catalogLine");
	response.setTranslationCatalogLine(translationService.convertResourceBundlePropsToMap(catalogLineBundleProps));

	//CAP-38706 - translationCatalogGrid
	Properties catalogGridBundleProps = translationService.getResourceBundle(appSessionBean, "catalogGrid");
	response.setTranslationCatalogGrid(translationService.convertResourceBundlePropsToMap(catalogGridBundleProps));
	
	//CAP-39524
	Properties translationCatalogSearchNav = translationService.getResourceBundle(appSessionBean, "catalogSearchNav");
	response.setTranslationCatalogSearchNav(translationService.convertResourceBundlePropsToMap(translationCatalogSearchNav));
	
	return response;
}

/**
 * 
 * @param appSessionBean - {@link AppSessionBean}
 * @param oeSessionBean - {@link OEOrderSessionBean}
 * @param volatileSessionBean - {@link VolatileSessionBean}
 * @return - This is a Collection of {@link TreeNodeVO}
 */
public Collection<TreeNodeVO> getMergedCatalogCategories(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, VolatileSessionBean volatileSessionBean)
{
	ICatalog cat = ObjectMapFactory.getComponentObjectMap().getObject(ICatalog.class, appSessionBean.getCustomToken());
	Collection<TreeNodeVO> categories = null;
	try
	{
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		categories = cat.getMergedCatalogCategories(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getGroupName(), appSessionBean.getLoginID(),
				appSessionBean.getProfileNumber(), oeSessionBean.getSiteAttrFilterSQL(), appSessionBean.isMobileSession(), volatileSessionBean.isKitTemplateMode(),
				userSettings.isAllowCustomDocumentsInd(), userSettings.getPrimaryAttribute(), userSettings.getSecondaryAttribute());
	} catch (AtWinXSException e)
	{
		// CAP-16460 call to logger.
		logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
	}
	return categories;
}

//CP-10988
/**
 * Method setSelectedCategory()
 * 
 * Set selected category for Auto Login.
 * 
 * @param searchCriteria - {@link CatalogSearchResultsCriteria}
 * @param searchCriteriaBean - {@link OEItemSearchCriteriaSessionBean}
 * @param oeSessionBean - {@link OEOrderSessionBean}
 * @param appSessionBean - {@link AppSessionBean}
 * @param categories - This is a Collection of {@link TreeNodeVO}
 * @throws AtWinXSException 
 */
public void setSelectedCategory(
		CatalogSearchResultsCriteria searchCriteria, 
		OEItemSearchCriteriaSessionBean searchCriteriaBean, 
		OEOrderSessionBean oeSessionBean,
		AppSessionBean appSessionBean, 
		Collection<TreeNodeVO> categories) throws AtWinXSException
{
	if (!Util.isBlankOrNull(appSessionBean.getCatalogCategory()))
	{
		OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		StringTokenizer tokenizer = new StringTokenizer(appSessionBean.getCatalogCategory(), OrderEntryConstants.CATEGORY_DELIMITER);
		ArrayList<String> tokenList = new ArrayList<>();

		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if (!Util.isBlankOrNull(token))
			{
				tokenList.add(token);
			}
		}
		
		String[] categoryList = tokenList.toArray(new String[0]);
		ICatalog catalogManager = CMCatalogComponentLocator.locate(appSessionBean.getCustomToken());
		CategoryVOKey[] categoryIDs = catalogManager.getCategoryIDs(categoryList, appSessionBean.getSiteID(), appSessionBean.getBuID());
		
		if (categoryIDs != null 
			&& categoryIDs.length > 0)
		{
			int selectedID = assembler.getPreselectedCategory(categoryIDs, categories, categoryList.length, 1);
			
			searchCriteria.setSelectedParentCategoryID(selectedID);
			searchCriteria.setSelectedCategoryID(selectedID);
			searchCriteriaBean.setSelectedCategoryId(selectedID);
			oeSessionBean.setLastSelectedCatalogId(selectedID);
		}
	}
}

/**
 * 
 * @param aisleName - {@link String}
 * @param categories - This is a Collection of {@link TreeNodeVO}
 * @param searchCriteria - {@link CatalogSearchResultsCriteria}
 * @return - This is a Collection of {@link TreeNodeVO}
 */
public Collection<TreeNodeVO> getPunchoutCategories(String aisleName, Collection<TreeNodeVO> categories, CatalogSearchResultsCriteria searchCriteria) // CP-8971, CP-11669
{
	if (!Util.isBlankOrNull(aisleName) && (categories != null && !categories.isEmpty()))
	{
		ArrayList<TreeNodeVO> aisle = null;
		for (TreeNodeVO childNodeVO : categories)
		{
			if (childNodeVO.getNodeName().equals(aisleName))
			{
				aisle = new ArrayList<>();
				aisle.add(childNodeVO);
				if (searchCriteria.getSelectedCategoryID() == -1) // CP-8971
				{
					searchCriteria.setSelectedCategoryID(childNodeVO.getNodeID());
				}
				break;
			}
		}
		if (aisle != null)
		{ // CP-11681
			categories = aisle;
		}
	}
	return categories; // CP-11669
}

/**
 * Generate the Catalog tree for the category selected.
 * @param tree Either null when first starting or the recursive tree
 * @param categories All of the catalog categories.
 * @param selectedCategoryId Either Id of the category that got selected or -1 when recursing.
 * @return The Catalog Tree for the category selected.
 */
@SuppressWarnings("unchecked")
public CatalogTree genTree(CatalogTree tree, Collection<TreeNodeVO> categories, int selectedCategoryId)
{
	CatalogTree trunk = null;
	ArrayList<CatalogTree> branches = null;
	
	if(categories != null && !categories.isEmpty())
	{
		branches = new ArrayList<>();
		for(TreeNodeVO category : categories)
		{
			if(tree == null)//Tree not started
			{
				if(category.getNodeID() == selectedCategoryId)//Start the tree when there is a match.
				{
					trunk = new CatalogTree();
					trunk.setNodeID(category.getNodeID());
					trunk.setNodeName(Util.nullToEmpty(category.getNodeName()));
					trunk.setNumberOfItems(category.getNumberOfItems());
					//CP-11838 set the peers as well
					trunk.setPeers(Arrays.asList(category.getPeerIds()));
					
					//CP-11422 - Add as parent
					ArrayList<String> parents = new ArrayList<>();
					parents.add(category.getNodeName() + "|" + (category.getNodeID()));
					trunk.setParents(parents);
					
					//Build out the rest of the tree.
					if(category.getChildren() != null && !category.getChildren().isEmpty())
					{
						genTree(trunk, category.getChildren(), -1);
					}
				}
				else//Check the sub categories for a place to start.
				{
					if(category.getChildren() != null && !category.getChildren().isEmpty())
					{						
						trunk = genTree(tree, category.getChildren(), selectedCategoryId);
					}	
					//CP-11422 - as we exit each recursion after getting our trunk, we add the category values as parents.
					// This puts them in the list in reverse order, but it's the only way.  We will read back to front when
					// we generate the appliedCriteria. 
					if (trunk!=null)
					{
						trunk.getParents().add(category.getNodeName() + "|" + (category.getNodeID()));
					}
				}
				if (trunk != null) 
				{
					tree = trunk;
				}
			}
			else//The tree has already been started.  Add to it.
			{
				CatalogTree branch = new CatalogTree();
				branch.setNodeID(category.getNodeID());
				branch.setNodeName(Util.nullToEmpty(category.getNodeName()));
				branch.setNumberOfItems(category.getNumberOfItems());
				branch.setPeers(Arrays.asList(category.getPeerIds()));
				branches.add(branch);
				
				if(category.getChildren() != null && !category.getChildren().isEmpty())
				{
					genTree(branch, category.getChildren(), -1);
				}
				
				tree.setChildren(branches);//Add the branches to current place on tree.
			}
			
			if(trunk != null)//Tree got started and branched out.  Stop processing.
			{
				break;
			}
		}
		if(tree == null)
		{
			tree = trunk;
		}
	}
	return tree;
}
//CP-9722 End	

}
