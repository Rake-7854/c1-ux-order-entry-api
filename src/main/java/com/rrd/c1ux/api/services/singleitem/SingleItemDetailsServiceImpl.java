/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		--------------		--------------------------------
 *	09/19/22    Sakthi M        CAP-35464           Modify API loading of product details to make catalog line
 *                                                  number optional for cart call and return results fields
 *	09/19/22	A Boomker		CAP-35958			Efficiency improvements
 *	09/26/22	A Boomker		CAP-35610			Fix URL for catalog repeat load
 *  09/27/22    Sakthi M        CAP-35948           Punchout inspect needs to set item unorderable in existing SingleItemDetails call for Product Details
 *  09/26/22	Krishna Natarajan 	CAP-36083			Modify API - added generateCategoryTree() to Get the category tree
 *	09/28/22	A Boomker		CAP-36084			Modify API response for categories so top level cats have label
 *	10/21/22	A Boomker		CAP-36713			Add handling for no image response
 *  02/23/23	Satishkumar A   CAP-38708           API Fix - Add Translation to ResponseObject for /api/singleitem/details
 *  02/27/23    C Porter        CAP-38708           Refactor translation service into base service object.
 *  03/23/23 	Sakthi M		CAP-38561 			Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
 *  05/01/23	N Caceres		CAP-39334			Use translation text values for return to labels
 *  05/09/23	N Caceres		CAP-39047			Resolve concurrency issues in SingleItemDetails Service
 *	08/29/23	L De Leon		CAP-43197			Refactored code for easier JUnit and set the value for featureMap
 *	08/30/23	A Boomker		CAP-43405					Fixing item in cart flags for customizable items
 *	09/07/23	Krishna Natarajan		CAP-43656			Added boolean variable allowFavorites in the response 
 *	09/12/23	Krishna Natarajan		CAP-43384			Added lines to call the ItemUtil methods to get the routing information
 *  10/11/23	T Harmon		CAP-44548									Modified edoc URL
 *  01/22/24	M Sakthi		CAP-46544			C1UX BE - Modify SingleItemDetailsServiceImpl method to return Attributes for Item
 *  02/19/24	M Sakthi		CAP-47063			C1UX BE - Add information to the /singleitem/details method to show the current allocation information.
 *  04/09/24	Krishna Natarajan		CAP-48537	Added logic to determine return to search results from single item details to wizard search
 *  05/08/24	M Sakthi				CAP-49015	C1UX BE - Create a new list in item details to show what the translation text is for all the options.
 *	05/13/23	L De Leon		CAP-48938/CAP-48977			Refactored getFileDeliveryLabel() method and moved to BaseOEService class
 *	05/23/24	Ramachandran S	CAP-49489			Include tiered info and efd service charges for items if available.
 *	06/05/24    S Ramachandran  CAP-49887   		Return components in SingleItemDetailsResponse if the item is a kit template
 *  06/13/24	M Sakthi		CAP-50002			C1UX BE - Return Kit Information for the item details page which include information like container and max/min item counts
 *  06/17/24	Krishna Natarajan CAP-50203			Added a null check on the itemClassification
 *  06/18/24	Krishna Natarajan CAP-50285 		Added a method setUOMFullText to update full text
 *  06/26/24	Krishna Natarajan CAP-50184			Added a method to get the suggested items flag
 */
package com.rrd.c1ux.api.services.singleitem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.items.UOMItems;
import com.rrd.c1ux.api.models.singleitem.AttributeList;
import com.rrd.c1ux.api.models.singleitem.CategoryListing;
import com.rrd.c1ux.api.models.singleitem.MainAttribute;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.KitComponentItemsService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderentry.entity.AllocationProcessor;
import com.rrd.custompoint.orderentry.entity.AllocationProcessorFields;
import com.rrd.custompoint.orderentry.entity.AllocationUsage;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.QuantityAllocation;
import com.rrd.custompoint.service.helper.ItemHelper;
import com.wallace.atwinxs.catalogs.ao.CMCatalogLineAssembler;
import com.wallace.atwinxs.catalogs.ao.CMCatalogLineSorting;
import com.wallace.atwinxs.catalogs.ao.CMCompanionItemAssembler;
import com.wallace.atwinxs.catalogs.ao.CMViewItemsCatalogFormBean;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.util.CatalogConstant;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.Util.ContextPath;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.ICustomDocsItem;
import com.wallace.atwinxs.interfaces.IGSAComponent;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.items.locator.ManageItemsInterfaceLocator;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.ao.MKKitAssembler;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKContainerTypeInfo;
import com.wallace.atwinxs.kits.session.MKHeaderInfo;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.customdocs.locator.CustomDocsAdminLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.ItemAttribute;
import com.wallace.atwinxs.orderentry.util.ItemAttributeValue;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.TieredPrice;
import com.wallace.atwinxs.orderentry.vo.OECompanionItemResultsVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.reports.vo.ItemRptVO;
import com.wallace.atwinxs.reports.vo.ItemRptVOKey;

@Service
public class SingleItemDetailsServiceImpl extends BaseOEService implements SingleItemDetailsService  {


	private static final Logger logger = LoggerFactory.getLogger(SingleItemDetailsServiceImpl.class);

	//CP-12787
	protected static final String STR_DEFAULT_XERT_QTY = "1";
	//CP-13104
	protected static final String STR_DEFAULT_BUNDLE_QTY = "1";

	protected static final String STR_EDOC_FLAG = "eDocFlag";
	protected static final String STR_EDOC_URL = "eDocURL";
	
	// CAP-33686
	protected static final String KIT_TEMPLATE_ITEM_CLASSIFICATION =  "Kit Template";

	ItemUtility altDesc = new ItemUtility();
	
	private final KitComponentItemsService kitComponentItemsService;

	public SingleItemDetailsServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService,
			KitComponentItemsService kitComponentItemsService) {
      super(translationService, objectMapFactoryService);
      this.kitComponentItemsService = kitComponentItemsService;
    }


	/**
	 *
	 * @param sc - {@link SessionContainer}
	 * @param ttsessionid - {@link String}
	 * @return - This is a Collection of {@link TreeNodeVO}
	 * @throws AtWinXSException
	 */
	@Override
	public SingleItemDetailsResponse retrieveSingleItemDetails(SessionContainer sc , String ttsessionid, SingleItemDetailsRequest itemDetailsRequest,CatalogItemRetriveServices catalogItemRetriveServices) throws  AtWinXSException
	{
		// CAP-39047 Resolve concurrency issues in SingleItemDetails Service
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = appSession.getPunchoutSessionBean();
		OrderOnBehalfVO orderOnBehalf = volatileSessionBean.getOrderOnBehalf();

		//CAP-35080
		OEResolvedUserSettingsSessionBean oeResUserSettingsSessionBean=oeOrderSessionBean.getUserSettings();

		appSessionBean.setInRequestorMode(orderOnBehalf.isInRequestorMode());

		 String itemNumber="";
		 String vendorItemNumber="";
		 int catalogLnNbr=0;
		 String quantity="";
		 boolean edocFlag ;
		 String lineLevelRouteMessage=""; 
		 String orderLevelRouteMessage="";
		 SingleItemDetailsResponse response = new SingleItemDetailsResponse();
		logger.debug("Single Item Details start");

		CatalogItem item = objectMapFactoryService.getEntityObjectMap().getEntity(CatalogItem.class, appSessionBean.getCustomToken());

		itemNumber = Util.nullToEmpty(itemDetailsRequest.getItemNumber());
		vendorItemNumber = Util.nullToEmpty(itemDetailsRequest.getVendorItemNumber());
		catalogLnNbr = Util.emptyToZero(Util.nullToEmpty(itemDetailsRequest.getCatalogLnNbr()+""));

		// Call populate method from ItemImpl
		// CAP-49489 -  tiered info pricing and efd service charges must be based on admin setting   
		item.populate(appSessionBean, oeOrderSessionBean, itemNumber, vendorItemNumber, catalogLnNbr, !oeResUserSettingsSessionBean.isShowPricingGrid());
			// this needs to be turned off for now
			//	!oeOrderSessionBean.getUserSettings().isShowPricingGrid()); // CAP-28540 - pricing must be based on admin
		//CAP-16674 SRN Check if Item is Orderable based on its Unit Price Amount per each
		//CAP-16676 SRN Moved method that checks if item is orderable for reusability
    
		//CAP-46544 get the Item Attributes	
		getItemAttributes(oeResUserSettingsSessionBean, item, response);
		
		
		logger.info("Got past item.populate");
		//CAP-CAP-35948 Punchout inspect needs to set item unorderable in existing SingleItemDetails call for Product Details
		//CP-10678 RAR - Added conditions on when to show the Customize/Add to Cart button.
		boolean isItemOrderable = item.isItemOrderable(); //CP-11268 RAR - Moved the method from ItemHelper to CatalogItem.
		if  ((punchoutSessionBean != null) && (punchoutSessionBean.getOperation().equals(OrderEntryConstants.PUNCHOUT_OPERATION_INSPECT))) // CP-8971
		{ // punchout inspect means you cannot add any items to the cart
					isItemOrderable = false;
		}
		if(isItemOrderable) {
				response.setOrderableItem(item.checkIfOrderable(item.getUnitPriceAmt(), isItemOrderable,
							oeOrderSessionBean.getUserSettings().getItemPrcLimitPerOrder()));
		}
		else {
			   response.setOrderableItem(isItemOrderable);
		}

		//CP-13132 look up order lines once, so we don't have to do it for each isItemInCart call
		OECatalogAssembler assembler = getCatalogAssembler(appSessionBean);
		Collection<OrderLineVO> orderLines = assembler.getOrderLines(null != volatileSessionBean.getOrderId() ? volatileSessionBean.getOrderId().intValue() : AtWinXSConstant.INVALID_ID);

		//CAP-35080
		ICatalog catalogComp = objectMapFactoryService.getComponentObjectMap().getObject(ICatalog.class, appSessionBean.getCustomToken());
		// CP-8780 - in order to correctly load EFD settings, you must pull line info ONLY
		// the other methods pull a combination of line and default that is causing EFD to be lost
		CatalogLineVO lineVO = catalogComp.getCatalogLineByLineNumberOnly(appSessionBean.getSiteID(), catalogLnNbr);
		// CAP-43405 - need to set customizable items "item in cart" flag differently
		boolean isCustomizableItem = isCustomizableItem(item.getItemClassification(), vendorItemNumber, appSession.getAppSessionBean(), appSession.getPunchoutSessionBean());

		boolean showAddToCartButton = showAddToCartButton( item, appSessionBean,  itemNumber,  vendorItemNumber,  orderLines,  isItemOrderable, isCustomizableItem);


		//CP-13132 pass order lines instead of volatile session
		boolean isItemInCart = !isCustomizableItem && isItemInCart(appSessionBean, itemNumber, vendorItemNumber, orderLines);

		//CP-9159
		//CAP-35080
		// CAP-39047 Resolve concurrency issues in SingleItemDetails Service
		Map<String, String> eDocDetails = getEDocDetails( appSessionBean,   catalogLnNbr,  itemNumber, vendorItemNumber, lineVO, catalogComp);

		// CAP-44548 TH - Added code to get appropriate edoc url
		String edocUrl = ItemUtility.getEdocUrl(itemNumber, vendorItemNumber, true, appSession.getAppSessionBean().getQualifiedImageSrc(eDocDetails.get(STR_EDOC_URL), AtWinXSConstant.ITEM_EDOC),eDocDetails.get(STR_EDOC_URL), appSessionBean.getSiteLoginID());		
		response.setEdocURL(edocUrl);


		if(eDocDetails.get(STR_EDOC_FLAG)!=null && "true".equalsIgnoreCase(eDocDetails.get(STR_EDOC_FLAG)))
			edocFlag = true ;
		else
			edocFlag = false ;

		response.setEDocFlag(edocFlag);
		// CAP-36713
		getMediumImageURL(appSession, itemNumber, vendorItemNumber, item);

		response.setItem(item);
		response.setUomArrLst(getUOMBeanList(appSessionBean, volatileSessionBean, oeOrderSessionBean, item));

		ItemRptVO itemRptVO= getItemOrderQtyDetails(appSession, vendorItemNumber) ;
		if(itemRptVO!=null) {
		response.setItemMinimumOrderQty(itemRptVO.getItemMininumOrderQty());
		response.setItemMaximumOrderQty(itemRptVO.getMaxinumOrderQty());
		response.setItemMultiplesOrderQty(itemRptVO.getItemMultipleOrderQty());
		}
		//CP-10211 don't default to 0
		quantity = (appSessionBean.getSiteDefaultQty() != 0 ? Integer.toString(appSessionBean.getSiteDefaultQty()) : "");//CP-9225 use admin default quantity
		response.setShowAddToCartButton(showAddToCartButton);
		response.setItemInCart(isItemInCart);
		response.setShowVendorItemNum(appSessionBean.isShowWCSSItemNumber());
		//CP-8942 - RAR Determine if the UOM Options need to be displayed or not.
		//JW - Added view only to the logic to determine to show UOM options or not.
		response.setDisplayUOMOptions(isDisplayUomOptions(item));

		//CP-8970 changed token from String to an Object

		response.setSuggestedItems(loadSuggestedItems(itemNumber, vendorItemNumber,volatileSessionBean,sc));//CAP-50184
		response.setQuantity(quantity);
		//Description+AlternateDescription in single item details
		response.setItemDescription(getItemDescription(item));
		//CAP-35080
		response.setShowOrderLinePrice(oeResUserSettingsSessionBean.isShowOrderLinePrice());
		response.setShowOrderLineAvailability(oeResUserSettingsSessionBean.isShowOrderLineAvailability());
		response.setPriceLineAvailabilityLbl(catalogItemRetriveServices.getVariantLabelValues(oeResUserSettingsSessionBean.isShowOrderLinePrice(),oeResUserSettingsSessionBean.isShowOrderLineAvailability(),appSessionBean));

		//CAP-35464
		response=determineReturnLink(sc, response);
		//CAP-36083
		response.setCategories(generateCategoryTree(item, appSessionBean));
		//CAP-38708
		Properties resourceBundleProps = translationService.getResourceBundle(appSessionBean, "item");
		response.setTranslation(translationService.convertResourceBundlePropsToMap(resourceBundleProps));

		// CAP-43197
		response.setFeatureMap(buildFeatureMap(item.getFeatureFavoriteItemData()));
		
		//CAP-43656 
		response.setAllowFavorites(oeOrderSessionBean.isAllowUserFavorites());
		logger.debug("Single Item Details end");
		
		//CAP-43384
		if(item.isShowRAInfo() && item.isItemRouting()) {
		lineLevelRouteMessage = ItemUtility.getLineLevelRoutingInformation(item.isAlwaysRouteInd(),
				item.isRouteIndicatorLineReview(), item.getApprovalQuantity(), item.getApprovalQuantityLineReview(),
				appSessionBean, oeOrderSessionBean);
		orderLevelRouteMessage = ItemUtility.getOrderLevelRoutingInformation(
				oeResUserSettingsSessionBean.isAlwaysRouteOrders(), oeResUserSettingsSessionBean.getRouteDollarAmount(),
				oeResUserSettingsSessionBean.isRouteOnShipMethodChange(), appSessionBean, oeOrderSessionBean);
		response.setRoutingBadge(
				ItemUtility.getRoutingInformation(lineLevelRouteMessage, orderLevelRouteMessage, appSessionBean));
		}
		
		//CAP-47063
		getQuantityAllocationMessage(response, appSessionBean, itemDetailsRequest, oeOrderSessionBean);
		
		//CAP-49015
		response.setFileDeliveryOption(item.getDeliveryOption());
		response.setFileDeliveryLabel(getFileDeliveryLabel(appSessionBean,item.getDeliveryOption()));
		
		// CAP-33686
		if( item.getItemClassification()!=null && item.getItemClassification().equals(ItemConstants.ITEM_CLASS_KIT_TEMPLATE)) {//CAP-50203

			response.setComponentItems( 
					kitComponentItemsService.getKitComponents(sc, itemNumber, vendorItemNumber, item.getItemClassification()));
		
		}	
		
		//CAP-50002
		getKitTemplateDetails(appSessionBean,item, response);
		setUOMFullText(item,appSessionBean);//CAP-50285
		return response;

	}
	
	//CAP-50285
	protected void setUOMFullText(CatalogItem item, AppSessionBean appSessionBean) throws AtWinXSException {
		if(item.getTieredPrice()!=null) {
		for(TieredPrice tieredPrice: item.getTieredPrice()) {
			tieredPrice.setPriceUOMCode(ItemUtility.getUOMAcronyms(tieredPrice.getPriceUOMCode(), false, appSessionBean));
		}
		}
	}

	// CAP-43197
	protected String getItemDescription(CatalogItem item) throws AtWinXSException {
		return altDesc.doAlternateDescriptionProcessing(item);
	}


	protected OECatalogAssembler getCatalogAssembler(AppSessionBean appSessionBean) {
		return new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
	}


	protected boolean isDisplayUomOptions(CatalogItem item) {
		return ItemHelper.isDisplayUOMOptions(item.getDeliveryOption(), item.isViewOnlyFlag());
	}


	protected boolean isItemInCart(AppSessionBean appSessionBean, String itemNumber, String vendorItemNumber,
			Collection<OrderLineVO> orderLines) throws AtWinXSException {
		return ItemHelper.isItemInCart(appSessionBean, itemNumber, vendorItemNumber, orderLines);
	}

	// CAP-39047 Extract code blocks to reduce complexity of retrieveSingleItemDetails method
	protected void getMediumImageURL(ApplicationSession appSession, String itemNumber, String vendorItemNumber,
			CatalogItem item) throws AtWinXSException {
		if (Util.isBlankOrNull(item.getMediumImageURL()))
		{
			item.setMediumImageURL("assets/images/No-image-316x316.svg");
		}
		else
		{
			if (!item.getMediumImageURL().toLowerCase().startsWith("http"))
			{
				ItemImagesVO medImageVO = new ItemImagesVO(appSession.getAppSessionBean().getSiteID(), itemNumber,
						vendorItemNumber,	AtWinXSConstant.EMPTY_STRING, item.getMediumImageURL(), AtWinXSConstant.EMPTY_STRING);
				ItemImagesVOFilter image = new ItemImagesVOFilter(medImageVO);
				String medImage = image.getQualifiedItemMedImgLocURL(appSession.getAppSessionBean());
				// CAP-39047 Remove code smell
				logger.info("Search result object pulled back med image: {} but we calculated this one instead- {}", item.getMediumImageURL(), medImage);
						// CAP-35224
				if (Util.isBlankOrNull(medImage))
				{
					item.setMediumImageURL("assets/images/No-image-316x316.svg");
				}
				else
				{
					item.setMediumImageURL(medImage);
				}
			}
		}
	}

	public boolean showAddToCartButton(CatalogItem item,AppSessionBean appSessionBean, String itemNumber, String vendorItemNumber, Collection<OrderLineVO> orderLines, boolean isItemOrderable, boolean isCustomizable) {

		boolean showAddToCartButton = false ;
		try {

			 showAddToCartButton = (!item.isViewOnlyFlag() &&
					   !item.isInvalidItemFlag() &&
					   //CP-13132 pass order lines instead of volatile session
					   (!isItemInCart(appSessionBean, itemNumber, vendorItemNumber, orderLines) ||
							   isCustomizable)
					   && isItemOrderable);
		} catch (AtWinXSException e) {
			logger.error("Failed in ItemDetailsController " + e.getMessage(), e);
		}
		return showAddToCartButton ;


	}
	// CAP-39047 Resolve concurrency issues in SingleItemDetails Service
	protected List<UOMItems> getUOMBeanList(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeOrderSessionBean, CatalogItem item) throws AtWinXSException
	{
		//CP-9431, use the common logic to get UOM, including wild card etc
		String mergeOptionCode = "";

		//CP-13132 determine if External ESP item
		boolean isExternalESP = false;

		//CP-9486 RAR - Get the item's mergeCode to handle Mail Merge Items.
		if(ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(item.getItemClassification()))
		{
			ICustomDocsItem cdItem = CustomDocsAdminLocator.locateCustomDocumentsItemComponent(appSessionBean.getCustomToken());
			mergeOptionCode = cdItem.getMergeCode(appSessionBean.getSiteID(), item.getVendorItemNumber());

			//CP-13132 determine if External ESP item
			CustomDocumentItem cdItemEntity = ObjectMapFactory.getEntityObjectMap().getEntity(CustomDocumentItem.class, appSessionBean.getCustomToken());
			isExternalESP = cdItemEntity.isExternalESP(appSessionBean.getSiteID(), item.getItemNumber(), item.getVendorItemNumber());

		}

		//CP-9486 RAR - Pass the mergeCode to handle Mail Merge Items.
		//JW - Added view only to the logic to determine to show UOM options or not.
		//CP-8970 removed token parameter
		//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
		Map<String, String> uomOpt=ItemHelper.getItemUOMOptions(item.getVendorItemNumber(), item.getItemNumber(), item.getItemClassification(), oeOrderSessionBean.getUserSettings(), volatileSessionBean, appSessionBean, item.getDeliveryOption(), mergeOptionCode, item.isViewOnlyFlag(), isExternalESP);
		List<UOMItems> uomArrList=new ArrayList<>();
		for (Map.Entry<String,String> entry : uomOpt.entrySet())
		{
			UOMItems uomitm=new UOMItems();
			uomitm.setUnitName(entry.getKey());
			// CAP-35958 - call new location of acronym map
			uomitm.setUnitValue(ItemUtility.getUOMAcronyms(entry.getValue(),true,appSessionBean));
			uomitm.setSoldAs(ItemUtility.getUOMAcronyms(entry.getValue(),false,appSessionBean));
			uomArrList.add(uomitm);
		}
		return uomArrList;

	}

	//CAP-35080
	protected Map<String, String> getEDocDetails(AppSessionBean appSessionBean,  int catalogLnNbr, String itemNumber,String vendorItemNumber,CatalogLineVO lineVO,ICatalog catalogComp) throws AtWinXSException
	{


		Map<String,String> edocDetails = new HashMap<>();

		if (catalogLnNbr > 0)
		{
			if (lineVO != null)
			{
				edocDetails.put(STR_EDOC_URL,lineVO.getEdocUrl());
				edocDetails.put(STR_EDOC_FLAG,lineVO.isEDocFlag()+"");
			}
		}
		else
		{
			//CP-9635 changed how we get the default data depending on what info we have
			CatalogDefaultVO dfltVO = null;
			if(!Util.isBlankOrNull(itemNumber) && !Util.isBlankOrNull(vendorItemNumber))
			{
				dfltVO = catalogComp.getCatalogDefault(vendorItemNumber, itemNumber, appSessionBean.getSiteID());
			}
			else if(!Util.isBlankOrNull(vendorItemNumber))
			{
				dfltVO = catalogComp.getCatalogDefaultByVendorItem(appSessionBean.getSiteID(), vendorItemNumber);
			}
			else if(!Util.isBlankOrNull(itemNumber))
			{
				dfltVO = catalogComp.getCatalogDefaultByCustItem(appSessionBean.getSiteID(), itemNumber);
			}

			if (dfltVO != null)
			{
				edocDetails.put(STR_EDOC_URL,dfltVO.getEdocUrl());
				edocDetails.put(STR_EDOC_FLAG,dfltVO.isEdocFlag()+"");
			}
		}
		return edocDetails;
	}

	protected ItemRptVO getItemOrderQtyDetails(ApplicationSession appSession,  String vendorItemNumber) throws AtWinXSException
	{
		ItemRptVO itemVO = null;
		// CAP-34332-Min.Max integration with CataloglineItems
		if(vendorItemNumber!= null && !"".equals(vendorItemNumber)) {
			IManageItemsInterface itemInterface = ManageItemsInterfaceLocator.locate(appSession.getAppSessionBean().getCustomToken());
			ItemRptVOKey rptKey = new ItemRptVOKey(vendorItemNumber);
			itemVO = itemInterface.getWCSSItem(rptKey, appSession.getAppSessionBean().getCorporateNumber(), null, false);
		}
		return itemVO;
	}


	//CAP-35464 -Modify API loading of product details to make catalog line number optional for cart call and return results fields
	public SingleItemDetailsResponse determineReturnLink(SessionContainer sc, SingleItemDetailsResponse response) {
		boolean isselectedCatagoryId = false;
		boolean isUnifiedSearchCreteria = false;
		// CAP-39047 Resolve concurrency issues in SingleItemDetails Service
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		if (oeOrderSessionBean.getSearchCriteriaBean() != null && response.getItem().getCatalogLineNbr() > 0) {
			// CAP-39334 Remove the unnecessary boolean literals
			isselectedCatagoryId = (oeOrderSessionBean.getSearchCriteriaBean().getSelectedCategoryId() > 0
					&& oeOrderSessionBean.getSearchCriteriaBean().isSearchSelectedCategory());
			isUnifiedSearchCreteria = (oeOrderSessionBean.getSearchCriteriaBean().getUnifiedSearchCriteria() != null
					&& !oeOrderSessionBean.getSearchCriteriaBean().getUnifiedSearchCriteria().isEmpty());

			if (isselectedCatagoryId) {
				// CAP-39334 Use translation text values for return to labels
				response.setReturnLinkText(getTranslation(appSessionBean, SFTranslationTextConstants.RETURN_TO_CATALOG_TEXT,
								SFTranslationTextConstants.RETURN_TO_CATALOG));
				response.setReturnLinkURL(RouteConstants.RETURN_TO_CATALOG_URL + "/0"); // CAP-35610
			} else if (isUnifiedSearchCreteria) {
				// CAP-39334 Use translation text values for return to labels
				response.setReturnLinkText(getTranslation(appSessionBean, SFTranslationTextConstants.RETURN_TO_RESULTS_TEXT,
								SFTranslationTextConstants.RETURN_TO_RESULTS));
				response.setReturnLinkURL(RouteConstants.RETURN_TO_RESULTS_URL);
			}
		}
		if (!isselectedCatagoryId && !isUnifiedSearchCreteria) {
			response.setReturnLinkText(RouteConstants.RETURN_TO_EMPTY);
			response.setReturnLinkURL(RouteConstants.HOME_PAGE_URL);
		}
		if (oeOrderSessionBean.isWizard()) {// CAP-48537
			response.setReturnLinkText(getTranslation(appSessionBean, SFTranslationTextConstants.RETURN_TO_RESULTS_TEXT,
					SFTranslationTextConstants.RETURN_TO_RESULTS));
			response.setReturnLinkURL(RouteConstants.RETURN_TO_WIDGET_RESULTS_URL);
		}
		return response;
	}

	/**
	 *Method added to get the category tree CAP-36083
	 */
	public Collection<CategoryListing> generateCategoryTree(CatalogItem item, AppSessionBean appSessionBean) throws AtWinXSException {

				Collection<Integer> assignedCatalogs = ObjectMapFactory.getComponentObjectMap().getComponent(IGSAComponent.class, appSessionBean.getCustomToken()).loadCatalogIDs(appSessionBean.getSiteID(),
						appSessionBean.getBuID(),
						appSessionBean.getGroupName(),
						appSessionBean.getProfileNumber(),
						appSessionBean.getLoginID());
		CMViewItemsCatalogFormBean assignedCategoriesBean = new CMCatalogLineAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale())
		.getItemCatalogResponseBean(item.getItemNumber(),
		item.getVendorItemNumber(),
		item.getDescription(),
		appSessionBean.getSiteID(),
		CatalogConstant.NULL_ID,
		CatalogConstant.SORT_USEDEFAULT,
		assignedCatalogs); // CAP-5387

		CMCatalogLineSorting[] assignedCategoryList = assignedCategoriesBean.getCataloglinelist();

		//CP-9386 Changed to a collection of collections
		Map<String, Collection<Collection<String>>> expandableElements = new LinkedHashMap<String, Collection<Collection<String>>>();
		Collection<String> subElements = null;
		//CP-9624, only add category element when category assigned
		if (assignedCategoryList != null)
		{
			for(CMCatalogLineSorting line : assignedCategoryList)
			{
				if(assignedCatalogs.contains(Util.safeStringToDefaultInt(line.getCatalogLine().getCatalogID(), AtWinXSConstant.INVALID_ID)))
				{
					subElements = new ArrayList<String>();

					String temp = line.getCatalogStruct();
					if(temp.indexOf(CatalogConstant.CATALOG_SEPERATE) >= 0)
					{//First token is the catalog name, which we don't want.
						temp = temp.substring(temp.indexOf(CatalogConstant.CATALOG_SEPERATE) + CatalogConstant.CATALOG_SEPERATE.length());
					}

					int delimiterIndex = temp.indexOf(CatalogConstant.CATALOG_SEPERATE);
					boolean first = true;
					String lastNode = ""; // CAP-5387
					while(delimiterIndex >= 0)
					{
						String node = temp.substring(0, temp.indexOf(CatalogConstant.CATALOG_SEPERATE));
						temp = temp.substring(delimiterIndex + CatalogConstant.CATALOG_SEPERATE.length());

						if(first)
						{
							//CP-9386 Changed to a collection of collections
							Collection<Collection<String>> existing = expandableElements.get(node) == null ? new ArrayList<Collection<String>>() : expandableElements.get(node);
							existing.add(subElements);
							expandableElements.put(node, existing);
							first = false;
						}
						else
						{
							subElements.add(node);
						}

						delimiterIndex = temp.indexOf(CatalogConstant.CATALOG_SEPERATE);

						if (!Util.isBlankOrNull(node))
						{ // CAP-5387
							lastNode = node;
						}
					}

					if(first)
					{
					//CP-9386 Changed to a collection of collections
						Collection<Collection<String>> existing = expandableElements.get(temp) == null ? new ArrayList<Collection<String>>() : expandableElements.get(temp);
						existing.add(subElements);
						expandableElements.put(temp, existing);
					}
					else
						{ // CAP-5387 - make sure this isn't an existing path in expandable elements
						Collection<Collection<String>> existing = expandableElements.get(lastNode) != null ? expandableElements.get(lastNode) : new ArrayList<Collection<String>>();
						Collection<String> tempCol = new ArrayList<String>();
						tempCol.add(temp);
						if ((!subElements.contains(temp)) && (!existing.contains(tempCol)))
						{ // CAP-5387
						subElements.add(temp);
						}
					}
				}
			}
		}

		Collection<CategoryListing> finalMap = new ArrayList<>();
		if ((expandableElements != null) && (!expandableElements.isEmpty()))
		{
			Set<String> listCats = expandableElements.keySet();
			Iterator<String> listKey = listCats.iterator();
			String key = null;
			Collection<Collection<String>> value = null;
			Collection<Collection<String>> finalValue = null;
			while (listKey.hasNext())
			{
				key = listKey.next();
				value = expandableElements.get(key);
				finalValue = new ArrayList<Collection<String>>();
				for (Collection<String> subList : value)
				{
					if (!subList.isEmpty())
					{
						finalValue.add(subList);
					}
				}
				finalMap.add(new CategoryListing(key, finalValue));
			}
		}
		return finalMap;

	}
	
	//CAP-46544
	public void getItemAttributes(OEResolvedUserSettingsSessionBean oeResUserSettingsSessionBean,CatalogItem item,SingleItemDetailsResponse response) {
		if(oeResUserSettingsSessionBean.isDisplayItemAttrAvail()) {
			Map<ItemAttribute,Collection<ItemAttributeValue>> tempAttr=item.getAttributeMap();
			Iterator<Map.Entry<ItemAttribute,Collection<ItemAttributeValue>>> itr = tempAttr.entrySet().iterator(); 
		    Collection<AttributeList> subElements = null;
		    Collection<MainAttribute> mainAttr=new ArrayList<>();
		    while(itr.hasNext()) 
	        { 
		    	 MainAttribute mAttr=new MainAttribute();
		    	 subElements = new ArrayList<>();
		    	 Map.Entry<ItemAttribute,Collection<ItemAttributeValue>> entry = itr.next(); 
		    	 List<ItemAttributeValue> attrVals = (List<ItemAttributeValue>)entry.getValue();
					
					if (attrVals != null)
					{
						for (ItemAttributeValue attrVal : attrVals)
						{
							AttributeList attrList=new AttributeList();
							attrList.setAttributeValues(attrVal.getAttrVal());
							attrList.setAttrValueDescription(attrVal.getAttrValDesc());
							subElements.add(attrList);
						}
					}
					mAttr.setAttributeDesc(entry.getKey().getAttrDisplayName());
					mAttr.setItemAttributeValues(subElements);
					mainAttr.add(mAttr);
					
	      }
			response.setAttribute(mainAttr);
	             
		}else {
			response.setAttribute(null);
		}
	}
	
	//CAP-47063
	public void getQuantityAllocationMessage(SingleItemDetailsResponse resp,AppSessionBean appSessionBean,SingleItemDetailsRequest itemDetailsRequest,OEOrderSessionBean oeOrderSessionBean) throws AtWinXSException {
	
		QuantityAllocation qtyAlloc = objectMapFactoryService.getEntityObjectMap().getEntity(QuantityAllocation.class, appSessionBean.getCustomToken());
		qtyAlloc.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getProfileNumber(), itemDetailsRequest.getVendorItemNumber(), itemDetailsRequest.getItemNumber(), 
				oeOrderSessionBean.getUserSettings().getAllocationsTimeframeCode(), appSessionBean.getDefaultLocale());
		
		LinkedHashMap<String, Object> replaceMap=new LinkedHashMap<>();
		double usedQty;
		NumberFormat nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
		
		//CP-13594 , add logic , if quantity alloation , and we need to calculate the used qty
		if (oeOrderSessionBean.getUserSettings().isAllowAllocationsInd() && oeOrderSessionBean.getUserSettings().isAllowItemQtyAllocation() && qtyAlloc.getAllocationQuantity() > 0 )
		{
			if (oeOrderSessionBean.getUserSettings().isApplyQuantityAllocationsApproval() && qtyAlloc.getCarryOverQuantity() < 0) //CP-13749 only factor in when carryover < 0
			{
				usedQty = qtyAlloc.getAllocationQuantity() - qtyAlloc.getRemainingQuantity() - qtyAlloc.getCarryOverQuantity();
			}
			else
			{
				usedQty = qtyAlloc.getAllocationQuantity() - qtyAlloc.getRemainingQuantity();
			}
			
			
			
			replaceMap.put(SFTranslationTextConstants.ITEM_QTY_ALLOCATIION_TAG, String.valueOf(nf.format(qtyAlloc.getAllocationQuantity()))); 
			replaceMap.put(SFTranslationTextConstants.ITEM_QTY_USED_TAG, String.valueOf(nf.format(usedQty))); 
			replaceMap.put(SFTranslationTextConstants.ITEM_DATE_RANGE_TAG, qtyAlloc.getAllocationEndDate());
			replaceMap.put(SFTranslationTextConstants.ITEM_UOM_TAG,translationService.processMessage(appSessionBean.getDefaultLocale(),
																appSessionBean.getCustomToken(),SFTranslationTextConstants.ITEM_UOM_LBL));
			
			resp.setQuantityAllocationMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.ITEM_ALLOCATION_COUNT_MESSAGE,replaceMap));
		}
		else if(oeOrderSessionBean.getUserSettings().isAllowNewItemQtyAllocation()) // CAP-18566
		{
			setNewItemAllocationsQuantity(replaceMap, oeOrderSessionBean.getUserSettings(), appSessionBean, itemDetailsRequest.getItemNumber(),itemDetailsRequest.getVendorItemNumber(), resp);
		}
		// CAP-18566 - End
	}
	
	
	protected void setNewItemAllocationsQuantity(Map<String, Object> replaceMap, OEResolvedUserSettingsSessionBean userSettings, AppSessionBean appSessionBean, String itemNumber, String vendorItemNumber,SingleItemDetailsResponse resp) throws AtWinXSException
	{
		AllocationUsage usage = null;
		int allocQty = 0;
		double usedQty;
		NumberFormat nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

		AllocationProcessor allocProcessor = getAllocationProcessor(appSessionBean);
		AllocationProcessorFields fields = getAllocationProcessorFields(appSessionBean);
		fields.setSiteId(appSessionBean.getSiteID());
		fields.setBuId(appSessionBean.getBuID());
		fields.setCorporateNumber(appSessionBean.getCorporateNumber());
		fields.setUserGroupName(userSettings.getGroupName());
		fields.setUserLoginID(appSessionBean.getLoginID());
		fields.setProfileID(appSessionBean.getProfileNumber());
		fields.setCustItemNr(itemNumber);
		fields.setVendorItemNr(vendorItemNumber);
		fields.setOrderLineNr(AtWinXSConstant.INVALID_ID);
		usage = allocProcessor.checkIfNewAllocationApply(fields, null);

		if(null != usage)
		{
			allocQty = usage.getAllocationQty();
			//CAP-19338	SRN If item's allocation rule is set to R&A and carryOver qty is -, include it in the computation
			if(usage.getApprovalQueueID() > -1 && usage.getCarryoverQty() < 0)
			{
				usedQty = allocQty - usage.getRemainingQty() - Double.valueOf(usage.getCarryoverQty());
			}
			else
			{
				usedQty = allocQty - Double.valueOf(usage.getRemainingQty());
			}
			
			if(usage.getCarryoverQty() > 0)
			{
				allocQty = allocQty + usage.getCarryoverQty();
			}
			replaceMap.put(SFTranslationTextConstants.ITEM_DATE_RANGE_TAG, Util.getStringFromDate(usage.getTimeFrameEnd(), appSessionBean.getDefaultLocale()));
			replaceMap.put(SFTranslationTextConstants.ITEM_QTY_USED_TAG, String.valueOf(nf.format(usedQty))); 
			replaceMap.put(SFTranslationTextConstants.ITEM_QTY_ALLOCATIION_TAG, String.valueOf(nf.format(allocQty)));
			replaceMap.put(SFTranslationTextConstants.ITEM_UOM_TAG,translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(),SFTranslationTextConstants.ITEM_UOM_LBL));
			
			resp.setQuantityAllocationMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.ITEM_ALLOCATION_COUNT_MESSAGE,replaceMap));
			}

		}

	public AllocationProcessor getAllocationProcessor(AppSessionBean appSessionBean)
	{
		return objectMapFactoryService.getEntityObjectMap().getEntity(AllocationProcessor.class, appSessionBean.getCustomToken());
	}

	public AllocationProcessorFields getAllocationProcessorFields(AppSessionBean appSessionBean)
	{
		return objectMapFactoryService.getEntityObjectMap().getEntity(AllocationProcessorFields.class, appSessionBean.getCustomToken());
	}
	
	
	//CAP-50002
	public SingleItemDetailsResponse getKitTemplateDetails(AppSessionBean appSessionBean, CatalogItem item,SingleItemDetailsResponse response) throws AtWinXSException {
		if(item.isKitOrKitTemplate())
		{
			KitSession kitSession = getKitSession(item,appSessionBean);
			final MKHeaderInfo header = kitSession.getHeader();
			final MKContainerTypeInfo containerType = header.getSelectedContainerType();
			boolean mkDefinedKitInd=kitSession.getHeader().getMKDefinedKitInd();
			if (item.isKitTemplate() || mkDefinedKitInd)
			{ 
				response.setContainerImagePath(Util.getContextPath(ContextPath.Usability)+ containerType.getImageURLAd());
				response.setContainerItemDescription(header.getContainerCustomerItemDesc());
				response.setContainerItemNumber(header.getContainerCustomerItemNum());
			}
			if (item.isKitTemplate())
			{ 
				response.setKitOrderQuantityMin(Integer.parseInt(header.getMinimumOrderQty()));
				response.setKitOrderQuantityMax(Integer.parseInt(header.getMaximumOrderQty()));
				response.setKitLineItemMin(Integer.parseInt(header.getMinimumItemCnt()));
				response.setKitLineItemMax(Integer.parseInt(header.getMaximumItemCnt()));
			}
		}
		
		return response;
		
	}

	public KitSession getKitSession(CatalogItem item,AppSessionBean appSessionBean) throws AtWinXSException
	{
		KitSession kitsession = null;
		MKKitAssembler asmKit = new MKKitAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		if (ItemConstants.ITEM_CLASS_KIT.equals(item.getItemClassification()))
		{

			kitsession = new KitSession();
			MKHeaderInfo header = new MKHeaderInfo();
			header.setWCSSItemNum(item.getVendorItemNumber());
			kitsession.setHeader(header);		
			// CP-11779 - load differently depending on status
			String status = asmKit.getKitStatus(appSessionBean.getSiteID(), item.getItemNumber());
			if ((ItemConstants.KIT_STATUS_ASSOCIATIONS.equals(status)) || (ItemConstants.KIT_STATUS_FULL.equals(status))) 
			{
				asmKit.loadKit(kitsession, appSessionBean, item.getItemNumber(), true, false);
			} 
			else if (((ItemConstants.KIT_STATUS_STUB.equals(status)) || (ItemConstants.KIT_STATUS_DELETED.equals(status))) && (item.isMasterComponent())) 
			{
				try 
				{ // CAP-5720 - if this fails, treat as not MK defined
					asmKit.loadKit(kitsession, appSessionBean, item.getItemNumber(), true, true);
				}
				catch(AtWinXSException wine)
				{
					header.setMKDefinedKitInd(false);
				}
			} 
			else
			{ // if neither of these, don't load the kit - there IS NO INFO
				header.setMKDefinedKitInd(false);
			}
		}
		else if (ItemConstants.ITEM_CLASS_KIT_TEMPLATE.equals(item.getItemClassification()))
		{
			kitsession = new KitSession();
			kitsession.init(appSessionBean);
			asmKit.loadKitTemplate(kitsession, appSessionBean, item.getItemNumber(), appSessionBean.getSiteID());
		}	
		return kitsession;
	}
	
	//CAP-50184
	private boolean loadSuggestedItems(String cItemNum, String vItemNum, VolatileSessionBean volatileSessionBean, SessionContainer sc )
	{
		CMCompanionItemAssembler cia = new CMCompanionItemAssembler(sc.getApplicationSession().getAppSessionBean().getCustomToken(), sc.getApplicationSession().getAppSessionBean().getCurrencyLocale());
		try
		{
			OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
			OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
			String attrSQL = cia.loadCompanionItemSiteAttrFilterSQL(volatileSessionBean.getSelectedSiteAttribute(),
					sc.getApplicationSession().getAppSessionBean().getProfileAttributes(),oeOrderSessionBean.getUsrSrchOptions(), sc.getApplicationSession().getAppSessionBean().getSiteID(), true);
			Collection<OECompanionItemResultsVO> resultsVOs = 
				cia.retrieveCompanionItemsByUseLevel(
					0, 
					volatileSessionBean, 
					sc.getApplicationSession().getAppSessionBean(),
					oeOrderSessionBean.getUserSettings(), 
					attrSQL,  
					false,
					vItemNum,
					cItemNum);
			
			if(resultsVOs != null && !resultsVOs.isEmpty())
			{
				sc.getModuleSession().putParameter("suggestedItems", resultsVOs);
				sc.getModuleSession().setClearParameters(false);
				
				return true;
			}
		}
		catch (AtWinXSException e)
		{
			logger.error("Failed in ItemDetailsController " + e.getMessage(), e); // CAP-16459
		}
		return false;
	}
	
}
