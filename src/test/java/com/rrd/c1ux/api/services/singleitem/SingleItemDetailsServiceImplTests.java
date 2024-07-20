/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------------------
 *	08/29/23	L De Leon		CAP-43197	Initial version
 *	08/30/23	A Boomker		CAP-43405	Fixing item in cart flags for customizable items
 *	01/23/24	M Sakthi		CAP-46544	C1UX BE - Modify SingleItemDetailsServiceImpl method to return Attributes for Item
 *	02/19/24	M Sakthi		CAP-47063	C1UX BE - Add information to the /singleitem/details method to show the current allocation information.
 *	05/25/24	S Ramachandran	CAP-49489	Added unit tests to include tiered info and efd service charges for items if available.
 *	06/05/24    S Ramachandran  CAP-49887  	Added tests to check return components if the item is a kit template
 *  06/13/24	M Sakthi		CAP-50002	Return Kit Information for the item details page which include information like container and max/min item counts
 *  06/18/24	Krishna Natarajan CAP-50285 Added additional mocks for new method setUOMFullText 
 */
package com.rrd.c1ux.api.services.singleitem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsResponse;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.framework.util.objectfactory.ComponentObjectMap;
import com.rrd.custompoint.gwt.common.cell.ItemThumbnailCell.ItemThumbnailCellData;
import com.rrd.custompoint.gwt.titlepanel.searchresults.widget.KitComponentSearchResult;
import com.rrd.custompoint.orderentry.entity.AllocationProcessor;
import com.rrd.custompoint.orderentry.entity.AllocationProcessorFields;
import com.rrd.custompoint.orderentry.entity.AllocationUsage;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.orderentry.entity.ItemAttributeImpl;
import com.rrd.custompoint.orderentry.entity.ItemAttributeValueImpl;
import com.rrd.custompoint.orderentry.entity.QuantityAllocation;
import com.wallace.atwinxs.catalogs.vo.AlternateCatalogData;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.FeatureFavoriteItemData;
import com.wallace.atwinxs.catalogs.vo.FeaturedItemsCompositeVO;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.ao.MKKitAssembler;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKContainerTypeInfo;
import com.wallace.atwinxs.kits.session.MKHeaderInfo;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.util.ItemAttribute;
import com.wallace.atwinxs.orderentry.util.ItemAttributeValue;
import com.wallace.atwinxs.orderentry.util.ItemServiceCharge;
import com.wallace.atwinxs.orderentry.util.TieredPrice;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

class SingleItemDetailsServiceImplTests extends BaseOEServiceTest {

	public static final String SUCCESS = "Success";
	public static final String FAIL = "Failed";
	private static final String ITEM_NUMBER = "ITEM_NUMBER";
	private static final String VENDOR_ITEM_NUMBER = "VENDOR_ITEM_NUMBER";
	private static final String ITEM_VIEW_NAME = "item";
	private static final String HOT_ITEM = "Hot";
	private static final String FEATURED_ITEM = "Featured";
	private static final String HOT_ITEM_PATH = "Hot.jpeg";
	private static final String FEATURED_ITEM_PATH = "Featured.jpeg";
	private static final String EDOC = "EDOC";

	public static final String ERROR_MESSAGE = "error message";

	private SingleItemDetailsResponse singleItemDetailsResponse;
	private SingleItemDetailsRequest singleItemDetailsRequest;
	private Collection<OrderLineVO> orderLines = new ArrayList<>();
	@Mock
	private CatalogLineVO lineVO;
	private Map<String, String> emptyMap = new HashMap<>();
	private Collection<FeaturedItemsCompositeVO> featuredItems;
	private List<ItemServiceCharge> itemServiceChargeList;
	private List<TieredPrice> tieredPriceList;

	@Mock
	private OrderOnBehalfVO mockOrderOnBehalf;

	@Mock
	private OECatalogAssembler mockCatalogAssembler;

	@Mock
	private ComponentObjectMap mockComponentObjectMap;

	@Mock
	private ICatalog mockCatalogComp;

	@Mock
	private CatalogItemRetriveServices mockCatalogItemRetriveServices;

	@Mock
	private OEResolvedUserSettingsSessionBean mockUserSettings;

	@Mock
	private FeatureFavoriteItemData mockFeatureFavoriteItemData;

	@Mock
	private FeaturedItemsCompositeVO mockFeaturedItemsCompositeVO;

	@Mock
	private AlternateCatalogData mockAlternateCatalogData;
	
	@Mock
	private QuantityAllocation mockQuantityAllocation;
	
	@Mock
	private AllocationProcessorFields mockAllocationProcessorFields;
	
	@Mock
	private AllocationProcessor mockAllocationProcessor;
	
	@Mock
	private AllocationUsage mockAllocationUsage;
	
	@Mock
	private ItemServiceCharge mockItemServiceCharge;
	
	@Mock
	private TieredPrice mockTieredPrice; 
	
	@Mock
	private Locale mockLocale;
	
	@Mock
	private MKHeaderInfo mockMKHeaderInfo;
	
	@Mock
	private KitSession mockKitSession;
	
	@Mock
	private MKContainerTypeInfo mockMKContainerTypeInfo;

	@InjectMocks
	private SingleItemDetailsServiceImpl service;
	
	@Mock
	  private OEExtendedItemQuantityResponseBean mockInvalidItem1;

	@BeforeEach
	void setup() throws Exception {
		singleItemDetailsRequest = new SingleItemDetailsRequest(ITEM_NUMBER, VENDOR_ITEM_NUMBER,
				AtWinXSConstant.INVALID_ID);
	}

	@Test
	@Disabled
	void that_retrieveSingleItemDetails_success() throws Exception {
		
		setUpModuleSession();
		when(mockAppSessionBean.getSiteID()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		featuredItems = new ArrayList<>();
		featuredItems.add(mockFeaturedItemsCompositeVO);
		featuredItems.add(mockFeaturedItemsCompositeVO);

		setupRetrieveSingleItemDetails();

		when(mockFeatureFavoriteItemData.getFeaturedItems()).thenReturn(featuredItems);
		when(mockFeaturedItemsCompositeVO.getFeaturedMouseOverText()).thenReturn(FEATURED_ITEM, HOT_ITEM);
		when(mockFeaturedItemsCompositeVO.getFeaturedSFIconPath()).thenReturn(FEATURED_ITEM_PATH, HOT_ITEM_PATH);
		doNothing().when(service).getQuantityAllocationMessage(any(), any(), any(), any());
		when(mockCatalogItem.getDeliveryOption()).thenReturn("PO");
		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);

		assertNotNull(singleItemDetailsResponse);
		assertNotNull(singleItemDetailsResponse.getFeatureMap());
		assertFalse(singleItemDetailsResponse.getFeatureMap().isEmpty());
	}
	
	// CAP-49887
	@Test
	@Disabled
	void that_retrieveSingleItemDetails_itemClassificationISKitTemplate_success() throws Exception {
		
		setUpModuleSession();
		when(mockAppSessionBean.getSiteID()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		featuredItems = new ArrayList<>();
		featuredItems.add(mockFeaturedItemsCompositeVO);
		featuredItems.add(mockFeaturedItemsCompositeVO);

		setupRetrieveSingleItemDetails();

		when(mockFeatureFavoriteItemData.getFeaturedItems()).thenReturn(featuredItems);
		when(mockFeaturedItemsCompositeVO.getFeaturedMouseOverText()).thenReturn(FEATURED_ITEM, HOT_ITEM);
		when(mockFeaturedItemsCompositeVO.getFeaturedSFIconPath()).thenReturn(FEATURED_ITEM_PATH, HOT_ITEM_PATH);
		doNothing().when(service).getQuantityAllocationMessage(any(), any(), any(), any());
		when(mockCatalogItem.getDeliveryOption()).thenReturn("PO");

		// CAP-49887
		when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_KIT_TEMPLATE);
		
		// CAP-49887
		List<ItemThumbnailCellData> componentItems = new ArrayList<ItemThumbnailCellData>();
		KitComponentSearchResult kitComponent = new KitComponentSearchResult("111","Desc");
		componentItems.add(kitComponent);
		doReturn(componentItems).when(mockKitComponentItemsService).getKitComponents(any(),any(),any(),any());
				
		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);

		assertNotNull(singleItemDetailsResponse);
		assertNotNull(singleItemDetailsResponse.getFeatureMap());
		assertFalse(singleItemDetailsResponse.getFeatureMap().isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	void that_retrieveSingleItemDetails_has_null_featureMap() throws Exception {

		setUpModuleSession();
		when(mockAppSessionBean.getSiteID()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockCatalogItem.getDeliveryOption()).thenReturn("E");
		featuredItems = new ArrayList<>();

		setupRetrieveSingleItemDetails();

		when(mockFeatureFavoriteItemData.getFeaturedItems()).thenReturn(null, featuredItems);
		doNothing().when(service).getQuantityAllocationMessage(any(), any(), any(), any());
		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);

		assertNotNull(singleItemDetailsResponse);
		assertNull(singleItemDetailsResponse.getFeatureMap());


		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);

		assertNotNull(singleItemDetailsResponse);
		assertNull(singleItemDetailsResponse.getFeatureMap());
	}

	protected void setupRetrieveSingleItemDetails() throws AtWinXSException {
		service = Mockito.spy(service);

		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(mockOrderOnBehalf);
		when(mockOrderOnBehalf.isInRequestorMode()).thenReturn(false);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockCatalogItem).when(mockEntityObjectMap).getEntity(CatalogItem.class, null);
		doNothing().when(mockCatalogItem).populate(mockAppSessionBean, mockOEOrderSession, ITEM_NUMBER, VENDOR_ITEM_NUMBER, AtWinXSConstant.INVALID_ID, true);
		when(mockCatalogItem.isItemOrderable()).thenReturn(false);

		doReturn(mockCatalogAssembler).when(service).getCatalogAssembler(mockAppSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(AtWinXSConstant.INVALID_ID);
		doReturn(orderLines).when(mockCatalogAssembler).getOrderLines(AtWinXSConstant.INVALID_ID);
		doReturn(mockComponentObjectMap).when(mockObjectMapFactoryService).getComponentObjectMap();
		doReturn(mockCatalogComp).when(mockComponentObjectMap).getObject(ICatalog.class, null);
		doReturn(lineVO).when(mockCatalogComp).getCatalogLineByLineNumberOnly(AtWinXSConstant.INVALID_ID, AtWinXSConstant.INVALID_ID);
		doReturn(false).when(service).showAddToCartButton(mockCatalogItem, mockAppSessionBean, ITEM_NUMBER, VENDOR_ITEM_NUMBER, orderLines, false, false);
		doReturn(false).when(service).isItemInCart(mockAppSessionBean, ITEM_NUMBER, VENDOR_ITEM_NUMBER, orderLines);
		when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_PRINTED_ITEM);
		doReturn(false).when(service).isCustomizableItem(anyString(),  anyString(), any(AppSessionBean.class), any());
		doReturn(emptyMap).when(service).getEDocDetails(mockAppSessionBean, AtWinXSConstant.INVALID_ID, ITEM_NUMBER, VENDOR_ITEM_NUMBER, lineVO, mockCatalogComp);
		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockAppSessionBean).getQualifiedImageSrc(null, EDOC);
		doNothing().when(service).getMediumImageURL(mockApplicationSession, ITEM_NUMBER, VENDOR_ITEM_NUMBER, mockCatalogItem);
		doReturn(null).when(service).getUOMBeanList(mockAppSessionBean, mockVolatileSessionBean, mockOEOrderSession, mockCatalogItem);
		doReturn(null).when(service).getItemOrderQtyDetails(mockApplicationSession, VENDOR_ITEM_NUMBER);
		when(mockAppSessionBean.getSiteDefaultQty()).thenReturn(0);
		doReturn(false).when(service).isDisplayUomOptions(mockCatalogItem);
		when(mockCatalogItem.getAltCatData()).thenReturn(mockAlternateCatalogData);
		when(mockCatalogItem.getDescription()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockAlternateCatalogData.getAlternateCatalogDesc()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockAlternateCatalogData.getAlternateCatalogDescDisplayType()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockCatalogItemRetriveServices).getVariantLabelValues(false, false, mockAppSessionBean);
		when(mockOEOrderSession.getSearchCriteriaBean()).thenReturn(null);
		doReturn(null).when(service).generateCategoryTree(mockCatalogItem, mockAppSessionBean);
		doReturn(null).when(mockTranslationService).getResourceBundle(mockAppSessionBean, ITEM_VIEW_NAME);
		doReturn(emptyMap).when(mockTranslationService).convertResourceBundlePropsToMap(null);
		when(mockCatalogItem.getFeatureFavoriteItemData()).thenReturn(mockFeatureFavoriteItemData);
	}
	
	//CAP-46544
	@Test
	void that_retrieveSingleItemDetails_has_attributesMap_not_available() throws Exception {
		singleItemDetailsResponse=new SingleItemDetailsResponse();
		when(mockUserSettings.isDisplayItemAttrAvail()).thenReturn(false);
		service.getItemAttributes(mockUserSettings, mockCatalogItem, singleItemDetailsResponse);
		assertNull(singleItemDetailsResponse.getAttribute());
	}	
	
	@SuppressWarnings("unchecked")
	@Test
	void that_retrieveSingleItemDetails_has_attributesMap_available() throws Exception {
		Map<ItemAttribute,Collection<ItemAttributeValue>> tempAttr=getAttributeMap();
		singleItemDetailsResponse=new SingleItemDetailsResponse();
		when(mockUserSettings.isDisplayItemAttrAvail()).thenReturn(true);
		doReturn(tempAttr).when(mockCatalogItem).getAttributeMap();
		service.getItemAttributes(mockUserSettings, mockCatalogItem, singleItemDetailsResponse);
		assertNotNull(singleItemDetailsResponse.getAttribute());
	}	
	
	
	public Map<ItemAttribute,Collection<ItemAttributeValue>> getAttributeMap() {
		Map<ItemAttribute,Collection<ItemAttributeValue>> tempAttr=new HashMap<>();
		ItemAttributeImpl itemattr=new ItemAttributeImpl();
		itemattr.setAttrID(1);
		itemattr.setAttrDisplayName("Color");
		Collection<ItemAttributeValue> subitemAttr=new ArrayList<>();
		ItemAttributeValueImpl itemAttrVal=new ItemAttributeValueImpl();
		itemAttrVal.setAttrVal("Red");
		itemAttrVal.setAttrValDesc("Red");
		subitemAttr.add(itemAttrVal);
		tempAttr.put(itemattr, subitemAttr);
		return tempAttr;
	}
	
	//CAP-47063	
	@Test
	void that_retrieveSingleItemDetails_has_allocation_old_available() throws Exception {
		singleItemDetailsResponse=new SingleItemDetailsResponse();
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockQuantityAllocation).when(mockEntityObjectMap).getEntity(QuantityAllocation.class, null);
		doNothing().when(mockQuantityAllocation).populate(anyInt(), anyInt(), anyInt(), any(),any(),any(),any());
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getAllocationsTimeframeCode()).thenReturn("");
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(true);
		when(mockQuantityAllocation.getAllocationQuantity()).thenReturn(100.0);
		when(mockUserSettings.isApplyQuantityAllocationsApproval()).thenReturn(true);
		when(mockQuantityAllocation.getCarryOverQuantity()).thenReturn(0.0);
		service.getQuantityAllocationMessage(singleItemDetailsResponse, mockAppSessionBean, singleItemDetailsRequest, mockOEOrderSession);
		assertNotNull(singleItemDetailsResponse);
	}	
	
	@Test
	void that_retrieveSingleItemDetails_has_allocation_old_carryover_available() throws Exception {
		singleItemDetailsResponse=new SingleItemDetailsResponse();
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockQuantityAllocation).when(mockEntityObjectMap).getEntity(QuantityAllocation.class, null);
		doNothing().when(mockQuantityAllocation).populate(anyInt(), anyInt(), anyInt(), any(),any(),any(),any());
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getAllocationsTimeframeCode()).thenReturn("");
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(true);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(true);
		when(mockQuantityAllocation.getAllocationQuantity()).thenReturn(100.0);
		when(mockUserSettings.isApplyQuantityAllocationsApproval()).thenReturn(true);
		when(mockQuantityAllocation.getCarryOverQuantity()).thenReturn(-1.0);
		service.getQuantityAllocationMessage(singleItemDetailsResponse, mockAppSessionBean, singleItemDetailsRequest, mockOEOrderSession);
		assertNotNull(singleItemDetailsResponse);
	}
	
	@Test
	void that_retrieveSingleItemDetails_has_allocation_new_available() throws Exception {
		service = Mockito.spy(service);
		singleItemDetailsResponse=new SingleItemDetailsResponse();
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockQuantityAllocation).when(mockEntityObjectMap).getEntity(QuantityAllocation.class, null);
		doReturn(mockAllocationProcessorFields).when(service).getAllocationProcessorFields(mockAppSessionBean);
		doReturn(mockAllocationProcessor).when(service).getAllocationProcessor(mockAppSessionBean);
		when(mockAllocationProcessor.checkIfNewAllocationApply(mockAllocationProcessorFields, null)).thenReturn(mockAllocationUsage);
		when(mockAllocationUsage.getApprovalQueueID()).thenReturn(1);
		when(mockAllocationUsage.getCarryoverQty()).thenReturn(-1);
		doNothing().when(mockQuantityAllocation).populate(anyInt(), anyInt(), anyInt(), any(),any(),any(),any());
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getAllocationsTimeframeCode()).thenReturn("");
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(false);
		when(mockUserSettings.isAllowNewItemQtyAllocation()).thenReturn(true);
		service.getQuantityAllocationMessage(singleItemDetailsResponse, mockAppSessionBean, singleItemDetailsRequest, mockOEOrderSession);
		assertNotNull(singleItemDetailsResponse);
	}
	
	@Test
	void that_retrieveSingleItemDetails_has_allocation_new_carry_available() throws Exception {
		service = Mockito.spy(service);
		singleItemDetailsResponse=new SingleItemDetailsResponse();
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockQuantityAllocation).when(mockEntityObjectMap).getEntity(QuantityAllocation.class, null);
		doReturn(mockAllocationProcessorFields).when(service).getAllocationProcessorFields(mockAppSessionBean);
		doReturn(mockAllocationProcessor).when(service).getAllocationProcessor(mockAppSessionBean);
		when(mockAllocationProcessor.checkIfNewAllocationApply(mockAllocationProcessorFields, null)).thenReturn(mockAllocationUsage);
		when(mockAllocationUsage.getApprovalQueueID()).thenReturn(1);
		when(mockAllocationUsage.getCarryoverQty()).thenReturn(1);
		doNothing().when(mockQuantityAllocation).populate(anyInt(), anyInt(), anyInt(), any(),any(),any(),any());
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.getAllocationsTimeframeCode()).thenReturn("");
		when(mockUserSettings.isAllowAllocationsInd()).thenReturn(false);
		when(mockUserSettings.isAllowNewItemQtyAllocation()).thenReturn(true);
		service.getQuantityAllocationMessage(singleItemDetailsResponse, mockAppSessionBean, singleItemDetailsRequest, mockOEOrderSession);
		assertNotNull(singleItemDetailsResponse);
	}

	// CAP-48977
	@ParameterizedTest
	@MethodSource("fileDeliveryOptions")
	@Disabled
	void that_retrieveSingleItemDetails_delivery_opt_success(String deliveryOption) throws Exception {
		
		setUpModuleSession();
		when(mockAppSessionBean.getSiteID()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		featuredItems = new ArrayList<>();
		featuredItems.add(mockFeaturedItemsCompositeVO);
		featuredItems.add(mockFeaturedItemsCompositeVO);

		setupRetrieveSingleItemDetails();

		when(mockFeatureFavoriteItemData.getFeaturedItems()).thenReturn(featuredItems);
		when(mockFeaturedItemsCompositeVO.getFeaturedMouseOverText()).thenReturn(FEATURED_ITEM, HOT_ITEM);
		when(mockFeaturedItemsCompositeVO.getFeaturedSFIconPath()).thenReturn(FEATURED_ITEM_PATH, HOT_ITEM_PATH);
		doNothing().when(service).getQuantityAllocationMessage(any(), any(), any(), any());
		when(mockCatalogItem.getDeliveryOption()).thenReturn(deliveryOption);
		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);

		assertNotNull(singleItemDetailsResponse);
		assertNotNull(singleItemDetailsResponse.getFeatureMap());
		assertFalse(singleItemDetailsResponse.getFeatureMap().isEmpty());
	}

	private static Stream<Arguments> fileDeliveryOptions() {
		return Stream.of(Arguments.of("EP"), Arguments.of("PE"), Arguments.of("EA"), Arguments.of("P"));
	}
	
	// CAP-49489
	protected void setupRetrieveSingleItemDetailsWithEFDServiceNameCharge() throws AtWinXSException {
		service = Mockito.spy(service);

		when(mockApplicationSession.getPunchoutSessionBean()).thenReturn(null);
		when(mockVolatileSessionBean.getOrderOnBehalf()).thenReturn(mockOrderOnBehalf);
		when(mockOrderOnBehalf.isInRequestorMode()).thenReturn(false);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockCatalogItem).when(mockEntityObjectMap).getEntity(CatalogItem.class, null);
		when(mockCatalogItem.isItemOrderable()).thenReturn(false);

		doReturn(mockCatalogAssembler).when(service).getCatalogAssembler(mockAppSessionBean);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(AtWinXSConstant.INVALID_ID);
		doReturn(orderLines).when(mockCatalogAssembler).getOrderLines(AtWinXSConstant.INVALID_ID);
		doReturn(mockComponentObjectMap).when(mockObjectMapFactoryService).getComponentObjectMap();
		doReturn(mockCatalogComp).when(mockComponentObjectMap).getObject(ICatalog.class, null);
		doReturn(lineVO).when(mockCatalogComp).getCatalogLineByLineNumberOnly(AtWinXSConstant.INVALID_ID, AtWinXSConstant.INVALID_ID);
		doReturn(false).when(service).showAddToCartButton(mockCatalogItem, mockAppSessionBean, ITEM_NUMBER, VENDOR_ITEM_NUMBER, orderLines, false, false);
		doReturn(false).when(service).isItemInCart(mockAppSessionBean, ITEM_NUMBER, VENDOR_ITEM_NUMBER, orderLines);
		when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_PRINTED_ITEM);
		doReturn(false).when(service).isCustomizableItem(anyString(),  anyString(), any(AppSessionBean.class), any());
		doReturn(emptyMap).when(service).getEDocDetails(mockAppSessionBean, AtWinXSConstant.INVALID_ID, ITEM_NUMBER, VENDOR_ITEM_NUMBER, lineVO, mockCatalogComp);
		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockAppSessionBean).getQualifiedImageSrc(null, EDOC);
		doNothing().when(service).getMediumImageURL(mockApplicationSession, ITEM_NUMBER, VENDOR_ITEM_NUMBER, mockCatalogItem);
		doReturn(null).when(service).getUOMBeanList(mockAppSessionBean, mockVolatileSessionBean, mockOEOrderSession, mockCatalogItem);
		doReturn(null).when(service).getItemOrderQtyDetails(mockApplicationSession, VENDOR_ITEM_NUMBER);
		when(mockAppSessionBean.getSiteDefaultQty()).thenReturn(0);
		doReturn(false).when(service).isDisplayUomOptions(mockCatalogItem);
		when(mockCatalogItem.getAltCatData()).thenReturn(mockAlternateCatalogData);
		when(mockCatalogItem.getDescription()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockAlternateCatalogData.getAlternateCatalogDesc()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		when(mockAlternateCatalogData.getAlternateCatalogDescDisplayType()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		doReturn(AtWinXSConstant.EMPTY_STRING).when(mockCatalogItemRetriveServices).getVariantLabelValues(false, false, mockAppSessionBean);
		when(mockOEOrderSession.getSearchCriteriaBean()).thenReturn(null);
		doReturn(null).when(service).generateCategoryTree(mockCatalogItem, mockAppSessionBean);
		doReturn(null).when(mockTranslationService).getResourceBundle(mockAppSessionBean, ITEM_VIEW_NAME);
		doReturn(emptyMap).when(mockTranslationService).convertResourceBundlePropsToMap(null);
		when(mockCatalogItem.getFeatureFavoriteItemData()).thenReturn(mockFeatureFavoriteItemData);
		when(mockFeatureFavoriteItemData.getFeaturedItems()).thenReturn(featuredItems);
		when(mockFeaturedItemsCompositeVO.getFeaturedMouseOverText()).thenReturn(FEATURED_ITEM, HOT_ITEM);
		when(mockFeaturedItemsCompositeVO.getFeaturedSFIconPath()).thenReturn(FEATURED_ITEM_PATH, HOT_ITEM_PATH);
	}
	
	// CAP-49489
	@ParameterizedTest
	@MethodSource("fileDeliveryOptions")
	@Disabled
	void that_retrieveSingleItemDetails_ShowPricingGridIsTrue_success(String deliveryOption) throws Exception {
		
		setUpModuleSession();
		when(mockAppSessionBean.getSiteID()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowPricingGrid()).thenReturn(true);

		featuredItems = new ArrayList<>();
		featuredItems.add(mockFeaturedItemsCompositeVO);
		featuredItems.add(mockFeaturedItemsCompositeVO);
		
		//when ShowPricingGrid=True and Service Charge is available for Item 
		itemServiceChargeList = new ArrayList<>();
		itemServiceChargeList.add(mockItemServiceCharge);
		
		//when ShowPricingGrid=True and Tier Price is available
		tieredPriceList = new ArrayList<>();  
		tieredPriceList.add(mockTieredPrice);
		 
		when(mockCatalogItem.getDeliveryOption()).thenReturn(deliveryOption);
		when(mockCatalogItem.getServiceChargeBeanList()).thenReturn(itemServiceChargeList);
		when(mockCatalogItem.getTieredPrice()).thenReturn(tieredPriceList);

		setupRetrieveSingleItemDetailsWithEFDServiceNameCharge();
		doNothing().when(service).getQuantityAllocationMessage(any(), any(), any(), any());
		tieredPriceList=null;
		when(mockCatalogItem.getTieredPrice()).thenReturn(tieredPriceList);
		service.setUOMFullText(mockCatalogItem, mockAppSessionBean);

		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getUOMAcronyms(mockInvalidItem1.getUOMCode(), false, mockAppSessionBean)).thenReturn("Carton");//CAP-49537
		}
		when(mockCatalogItem.getTieredPrice()).thenReturn(tieredPriceList);

		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);
		assertNotNull(singleItemDetailsResponse);
		assertNotNull(singleItemDetailsResponse.getItem().getServiceChargeBeanList());
		assertNull(singleItemDetailsResponse.getItem().getTieredPrice());
	}
	
	// CAP-49489
	@ParameterizedTest
	@MethodSource("fileDeliveryOptions")
	@Disabled
	void that_retrieveSingleItemDetails_ShowPricingGridIsFalse_success(String deliveryOption) throws Exception {
		
		setUpModuleSession();
		when(mockAppSessionBean.getSiteID()).thenReturn(AtWinXSConstant.INVALID_ID);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowPricingGrid()).thenReturn(false);

		featuredItems = new ArrayList<>();
		featuredItems.add(mockFeaturedItemsCompositeVO);
		featuredItems.add(mockFeaturedItemsCompositeVO);

		//when ShowPricingGrid=False, then Service Charge & Tiered Pricset to NULL
		itemServiceChargeList = null;
		tieredPriceList = null;

		when(mockCatalogItem.getDeliveryOption()).thenReturn(deliveryOption);
		when(mockCatalogItem.getServiceChargeBeanList()).thenReturn(itemServiceChargeList);
		when(mockCatalogItem.getTieredPrice()).thenReturn(tieredPriceList);
		setupRetrieveSingleItemDetailsWithEFDServiceNameCharge();
		doNothing().when(service).getQuantityAllocationMessage(any(), any(), any(), any());
	
		service.setUOMFullText(mockCatalogItem, mockAppSessionBean);

		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getUOMAcronyms(mockInvalidItem1.getUOMCode(), false, mockAppSessionBean)).thenReturn("Carton");//CAP-49537
		}

		singleItemDetailsResponse = service.retrieveSingleItemDetails(mockSessionContainer, null, singleItemDetailsRequest, mockCatalogItemRetriveServices);
		assertNotNull(singleItemDetailsResponse);
		assertNull(singleItemDetailsResponse.getItem().getServiceChargeBeanList());
		assertNull(singleItemDetailsResponse.getItem().getTieredPrice());
	}
	
	//CAP-50002
	
		@Test
		void that_retrieveSingleItemDetails_getKitTemplateDetails_success() throws Exception {
			service = Mockito.spy(service);
			
			SingleItemDetailsResponse singleItemDetailsResponse=new SingleItemDetailsResponse();
			when(mockCatalogItem.isKitOrKitTemplate()).thenReturn(true);
			when(mockCatalogItem.isKitTemplate()).thenReturn(true);
			when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_KIT_TEMPLATE);
			
			try (MockedConstruction<KitSession> mockedKitSession = mockConstruction(KitSession.class, (mock, context) -> {
				doNothing().when(mock).init(any());
				when(mock.getHeader()).thenReturn(mockMKHeaderInfo);
				
			});
			MockedConstruction<MKKitAssembler> mockedMKKitAssembler = Mockito.mockConstruction(MKKitAssembler.class,
					(mock2, context) ->{
			} );
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {
				
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("test");
			when(mockMKHeaderInfo.getSelectedContainerType()).thenReturn(mockMKContainerTypeInfo);
			when(mockMKHeaderInfo.getMaximumOrderQty()).thenReturn("2");
			when(mockMKHeaderInfo.getMinimumOrderQty()).thenReturn("3");
			when(mockMKHeaderInfo.getMaximumItemCnt()).thenReturn("4");
			when(mockMKHeaderInfo.getMinimumItemCnt()).thenReturn("5");
			
				
			singleItemDetailsResponse = service.getKitTemplateDetails(mockAppSessionBean, mockCatalogItem, singleItemDetailsResponse);

			assertNotNull(singleItemDetailsResponse);
		  }
		}	
		
		
		@Test
		void that_retrieveSingleItemDetails_getKitDetails_kitstatusassociation_success() throws Exception {
			service = Mockito.spy(service);
			
			SingleItemDetailsResponse singleItemDetailsResponse=new SingleItemDetailsResponse();
			when(mockCatalogItem.isKitOrKitTemplate()).thenReturn(true);
			when(mockCatalogItem.isKitTemplate()).thenReturn(true);
			when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_KIT);
		
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(mockLocale);
			
			try (MockedConstruction<KitSession> mockedKitSession = mockConstruction(KitSession.class, (mock, context) -> {
				doNothing().when(mock).init(any());
				when(mock.getHeader()).thenReturn(mockMKHeaderInfo);
				
			});
			MockedConstruction<MKKitAssembler> mockedMKKitAssembler = Mockito.mockConstruction(MKKitAssembler.class,
					(mock2, context) ->{
						doReturn("K").when(mock2).getKitStatus(anyInt(), any());
			} );
			
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("test");
			when(mockMKHeaderInfo.getSelectedContainerType()).thenReturn(mockMKContainerTypeInfo);
			when(mockMKHeaderInfo.getMaximumOrderQty()).thenReturn("2");
			when(mockMKHeaderInfo.getMinimumOrderQty()).thenReturn("3");
			when(mockMKHeaderInfo.getMaximumItemCnt()).thenReturn("4");
			when(mockMKHeaderInfo.getMinimumItemCnt()).thenReturn("5");
		
			singleItemDetailsResponse = service.getKitTemplateDetails(mockAppSessionBean, mockCatalogItem, singleItemDetailsResponse);
			assertNotNull(singleItemDetailsResponse);
		  }
		}	
		
		@Test
		void that_retrieveSingleItemDetails_getKitDetails_kitstatusstub_success() throws Exception {
			service = Mockito.spy(service);
			
			SingleItemDetailsResponse singleItemDetailsResponse=new SingleItemDetailsResponse();
			when(mockCatalogItem.isKitOrKitTemplate()).thenReturn(true);
			when(mockCatalogItem.isKitTemplate()).thenReturn(true);
			when(mockCatalogItem.isMasterComponent()).thenReturn(true);
			when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_KIT);
		
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(mockLocale);
			
			try (MockedConstruction<KitSession> mockedKitSession = mockConstruction(KitSession.class, (mock, context) -> {
				doNothing().when(mock).init(any());
				when(mock.getHeader()).thenReturn(mockMKHeaderInfo);
				
			});
			MockedConstruction<MKKitAssembler> mockedMKKitAssembler = Mockito.mockConstruction(MKKitAssembler.class,
					(mock2, context) ->{
						doReturn("S").when(mock2).getKitStatus(anyInt(), any());
			} );
			
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("test");
			when(mockMKHeaderInfo.getSelectedContainerType()).thenReturn(mockMKContainerTypeInfo);
			when(mockMKHeaderInfo.getMaximumOrderQty()).thenReturn("2");
			when(mockMKHeaderInfo.getMinimumOrderQty()).thenReturn("3");
			when(mockMKHeaderInfo.getMaximumItemCnt()).thenReturn("4");
			when(mockMKHeaderInfo.getMinimumItemCnt()).thenReturn("5");
		
			singleItemDetailsResponse = service.getKitTemplateDetails(mockAppSessionBean, mockCatalogItem, singleItemDetailsResponse);
			assertNotNull(singleItemDetailsResponse);
		  }
		}	
		
		@Test
		void that_retrieveSingleItemDetails_getKitDetails_success() throws Exception {
			service = Mockito.spy(service);
			
			SingleItemDetailsResponse singleItemDetailsResponse=new SingleItemDetailsResponse();
			when(mockCatalogItem.isKitOrKitTemplate()).thenReturn(true);
			when(mockCatalogItem.isKitTemplate()).thenReturn(true);
			when(mockCatalogItem.getItemClassification()).thenReturn(ItemConstants.ITEM_CLASS_KIT);
		
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(mockLocale);
			
			try (MockedConstruction<KitSession> mockedKitSession = mockConstruction(KitSession.class, (mock, context) -> {
				doNothing().when(mock).init(any());
				when(mock.getHeader()).thenReturn(mockMKHeaderInfo);
				
			});
			MockedConstruction<MKKitAssembler> mockedMKKitAssembler = Mockito.mockConstruction(MKKitAssembler.class,
					(mock2, context) ->{
				//		doReturn("S").when(mock2).getKitStatus(anyInt(), any());
			} );
			
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);) {
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("test");
			when(mockMKHeaderInfo.getSelectedContainerType()).thenReturn(mockMKContainerTypeInfo);
			when(mockMKHeaderInfo.getMaximumOrderQty()).thenReturn("2");
			when(mockMKHeaderInfo.getMinimumOrderQty()).thenReturn("3");
			when(mockMKHeaderInfo.getMaximumItemCnt()).thenReturn("4");
			when(mockMKHeaderInfo.getMinimumItemCnt()).thenReturn("5");
		
			singleItemDetailsResponse = service.getKitTemplateDetails(mockAppSessionBean, mockCatalogItem, singleItemDetailsResponse);
			assertNotNull(singleItemDetailsResponse);
		  }
		}	
}