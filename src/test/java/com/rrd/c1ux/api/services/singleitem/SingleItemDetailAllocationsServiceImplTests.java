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
 *	02/14/24	S Ramachandran	CAP-47145	added test for SingleItemDetailAllocations Service
 */
package com.rrd.c1ux.api.services.singleitem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsResponse;
import com.rrd.c1ux.api.services.items.locator.ItemServiceComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.ItemValidationComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.SiteComponentLocatorService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.orderentry.entity.ItemAllocation;
import com.rrd.custompoint.orderentry.entity.ItemAllocationImpl;
import com.rrd.custompoint.orderentry.entity.ItemAllocations;
import com.rrd.custompoint.orderentry.entity.ItemAllocationsImpl;
import com.rrd.custompoint.services.interfaces.IItemServicesComponent;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IItemValidation;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.orderentry.admin.vo.UserGroupOrderPropertiesVO;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

class SingleItemDetailAllocationsServiceImplTests extends BaseServiceTest {
	
	//CAP-47145
	@InjectMocks
	private SingleItemDetailAllocationsServiceImpl service;
	
	//CAP-47145
	private static final String TEST_VALID_CUSTOMER_ITEM_NUMBER="14444";
    private static final String TEST_VALID_VENDOR_ITEM_NUMBER="255105";
    
    private static final String TEST_INVALID_CUSTOMER_ITEM_NUMBER="14444 Iinvalid";
    private static final String TEST_INVALID_VENDOR_ITEM_NUMBER="255105 Invalid";
    
    private static final String TEST_INVALID_CUSTOMER_ITEM_NUMBER_GT30_CHAR="14444 GREATER THAN 30 CHAR GREATER THAN 30 CHAR";
    private static final String TEST_INVALID_VENDOR_ITEM_NUMBER_GT15_CHAR="255105 GREATER THAN 15 CHAR";
    
	private static final String GENERIC_TRANSLATION_MSG = "TRANSLATION MESSAGE";
	
	//CAP-47145
	private SingleItemDetailAllocationsResponse singleItemDetailAllocationsResponse;
  	private SingleItemDetailAllocationsRequest singleItemDetailAllocationsRequest;
  	private ItemAllocations itemAllocations;
  	
  	@Mock
	protected OEOrderSessionBean mockOEOrderSession;
	
	@Mock
	protected Properties mockProperties;

	@Mock
	protected Map<String, String> mockTranslationMap;
	
	@Mock
	protected SiteComponentLocatorService mockSiteComponentLocatorService;
	
	@Mock
	protected ItemServiceComponentLocatorService mockItemServiceComponentLocatorService;
	
	@Mock
	protected ISite mockSite;
	
	@Mock
	protected IItemServicesComponent mockItemServicesComponent;
	
	@Mock
	protected LoginVOKey mockLoginVOKey;
	
	@Mock
	protected SiteBUGroupLoginProfileVO mockSiteBUGroupLoginProfileVO;
	
	@Mock 
	protected ItemRptVO mockItemRptVO;
	
	@Mock
	protected ItemValidationComponentLocatorService mockItemValidationComponentLocatorService;
	
	@Mock
	protected IItemValidation mockItemValidation;
	
	@Mock
	protected UserGroupOrderPropertiesVO mockUserGroupOrderPropertiesVO;
	
	@Mock
	protected ItemAllocations mockItemAllocations;
	
	@Mock 
	UserGroupVOKey mockUserGroupVOKey;
  	
	@BeforeEach
	void setup() throws Exception {
		
		//CAP-47145
		itemAllocations = getItemAllocations();
	}
	
	//CAP-47145
	@Test
	void that_retrieveItemAllocations_success200() throws Exception {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);
		when(mockAppSessionBean.getGroupName()).thenReturn("IDC-CP-GRP1");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(true);

		when(mockSiteComponentLocatorService.locate(any())).thenReturn(mockSite);
		when(mockSite.getSessionSettings(any())).thenReturn(mockSiteBUGroupLoginProfileVO);
		when(mockSiteBUGroupLoginProfileVO.getLoginID()).thenReturn("IDC-CP-USER1");
		when(mockSiteBUGroupLoginProfileVO.getUserGroupName()).thenReturn("IDC-CP-GRP1");
		
		when(mockItemServiceComponentLocatorService.locate(any())).thenReturn(mockItemServicesComponent);
		when(mockItemServicesComponent.getWcssItemInformation(any(),any(),any())).thenReturn(mockItemRptVO);
		when(mockItemRptVO.getCustomerItemNumber()).thenReturn("14444");
		when(mockItemRptVO.getWallaceItemNumber()).thenReturn("255105");
		
		when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
		when(mockIOrderAdmin.getGroupPlaceOrderDetails(any())).thenReturn(mockUserGroupOrderPropertiesVO);
		when(mockUserGroupOrderPropertiesVO.isSearchCatalogItemsOnlyInd()).thenReturn(true);
		
		when(mockItemValidationComponentLocatorService.locate(any())).thenReturn(mockItemValidation);
		when(mockItemValidation.isItemValidForCatalog(anyInt(),anyInt(),anyString(),
				anyInt(),anyString(),anyString(),anyString(),anyBoolean(),anyBoolean(),any())).thenReturn(true);
		
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
	    when(mockEntityObjectMap.getEntity(ItemAllocations.class, mockToken)).thenReturn(itemAllocations);

		try
		( 	MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
			MockedConstruction<UserGroupVOKey> mockUserGroupVOKey = mockConstruction(UserGroupVOKey.class, (mock, context) -> {
			  when(mock.getSiteID()).thenReturn(0);
			  when(mock.getBuID()).thenReturn(0);
			  when(mock.getUserGroupName()).thenReturn("IDC-CP-GRP1");
		  })
		) 
		{
			mockUtil.when(() -> Util.getDateFormatForLocale(Locale.US)).thenReturn("MM/dd/yyyy");		
			
			//service = Mockito.spy(service);
			singleItemDetailAllocationsRequest = getSingleItemDetailAllocationsRequest();

			//With Customer Item Number = VALID and Vendor Item Number = VALID
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
			singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_VALID_VENDOR_ITEM_NUMBER);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertTrue(singleItemDetailAllocationsResponse.isSuccess());
			
			//With Customer Item Number = VALID and Vendor Item Number = BLANK     
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
			singleItemDetailAllocationsRequest.setVendorItemNumber("");
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertTrue(singleItemDetailAllocationsResponse.isSuccess());
			
			//With Customer Item Number = BLANK and Vendor Item Number = VALID     
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
			singleItemDetailAllocationsRequest.setVendorItemNumber("");
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertTrue(singleItemDetailAllocationsResponse.isSuccess());
		}
	}
	
	//CAP-47145
	@Test
	void that_retrieveItemAllocations_CustNBR_VendorNBR_Validation_Failed422() throws Exception {
			
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(true);


		try
		(	MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				) {
			
			singleItemDetailAllocationsRequest = getSingleItemDetailAllocationsRequest();
			
			//With Customer Item Number = BLANK and Vendor Item Number = BLANK
			singleItemDetailAllocationsRequest.setCustomerItemNumber(null);
			singleItemDetailAllocationsRequest.setVendorItemNumber(null);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertFalse(singleItemDetailAllocationsResponse.isSuccess());
			
			//With Customer Item Number = GREATER THAN 30 CHAR and Vendor Item Number = GREATER THAN 15 CHAR
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_INVALID_CUSTOMER_ITEM_NUMBER_GT30_CHAR);
			singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_INVALID_VENDOR_ITEM_NUMBER_GT15_CHAR);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertFalse(singleItemDetailAllocationsResponse.isSuccess());
			
			//With Customer Item Number = GREATER THAN 30 CHAR and Vendor Item Number = VALID
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_INVALID_CUSTOMER_ITEM_NUMBER_GT30_CHAR);
			singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_VALID_VENDOR_ITEM_NUMBER);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertFalse(singleItemDetailAllocationsResponse.isSuccess());
			
			//With Customer Item Number = VALID and Vendor Item Number = GREATER THAN 15 CHAR
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
			singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_INVALID_VENDOR_ITEM_NUMBER_GT15_CHAR);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertFalse(singleItemDetailAllocationsResponse.isSuccess());
		}
	}
	
	//CAP-47145
	@Test
	void that_retrieveItemAllocations_InvalidItemServCall_Validation_Failed422() throws Exception {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);
		when(mockAppSessionBean.getGroupName()).thenReturn("IDC-CP-GRP1");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(true);
		
		when(mockSiteComponentLocatorService.locate(any())).thenReturn(mockSite);
		when(mockSite.getSessionSettings(any())).thenReturn(mockSiteBUGroupLoginProfileVO);
		when(mockSiteBUGroupLoginProfileVO.getLoginID()).thenReturn("IDC-CP-USER1");
		when(mockSiteBUGroupLoginProfileVO.getUserGroupName()).thenReturn("IDC-CP-GRP1");
		
		when(mockItemServiceComponentLocatorService.locate(any())).thenReturn(mockItemServicesComponent);
		when(mockItemServicesComponent.getWcssItemInformation(any(),any(),any())).thenReturn(mockItemRptVO);
		when(mockItemRptVO.getCustomerItemNumber()).thenReturn("14444");
		when(mockItemRptVO.getWallaceItemNumber()).thenReturn("255105");
		
		when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
		when(mockIOrderAdmin.getGroupPlaceOrderDetails(any())).thenReturn(mockUserGroupOrderPropertiesVO);
		when(mockUserGroupOrderPropertiesVO.isSearchCatalogItemsOnlyInd()).thenReturn(true);
		
		when(mockItemValidationComponentLocatorService.locate(any())).thenReturn(mockItemValidation);
		when(mockItemValidation.isItemValidForCatalog(anyInt(),anyInt(),anyString(),
				anyInt(),anyString(),anyString(),anyString(),anyBoolean(),anyBoolean(),any())).thenReturn(false);
		
		try
		( 	MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				) {
			
			singleItemDetailAllocationsRequest = getSingleItemDetailAllocationsRequest();

			//With Customer Item Number = VALID and Vendor Item Number = VALID
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_INVALID_CUSTOMER_ITEM_NUMBER);
			singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_INVALID_VENDOR_ITEM_NUMBER);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertFalse(singleItemDetailAllocationsResponse.isSuccess());
		}
	}
	
	//CAP-47145
	@Test
	void that_retrieveItemAllocations_NOProfile_Shared_User_returnAllocationNULL_Success200() throws Exception {
			
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);

		//With Customer Item Number = VALID and Vendor Item Number = VALID
		singleItemDetailAllocationsRequest = getSingleItemDetailAllocationsRequest();
		singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
		singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_VALID_VENDOR_ITEM_NUMBER);
	
		singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
		assertTrue(singleItemDetailAllocationsResponse.isSuccess());
		assertNull(singleItemDetailAllocationsResponse.getItemAllocationOrderList());
	}
	
	//CAP-47145
	@Test
	void that_retrieveItemAllocations_UserWithNOAllowItemQtyAllocation_returnAllocationNULL_Success200() throws Exception {
			
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(false);

		//With Customer Item Number = VALID and Vendor Item Number = VALID
		singleItemDetailAllocationsRequest = getSingleItemDetailAllocationsRequest();
		singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
		singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_VALID_VENDOR_ITEM_NUMBER);
	
		singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
		assertTrue(singleItemDetailAllocationsResponse.isSuccess());
		assertNull(singleItemDetailAllocationsResponse.getItemAllocationOrderList());
	}
	
	//CAP-47145
	@Test
	void that_retrieveItemAllocations_throwInvalidItemServCallError_onAnyError_Failed422() throws Exception {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);
		when(mockAppSessionBean.getGroupName()).thenReturn("IDC-CP-GRP1");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowItemQtyAllocation()).thenReturn(true);

		when(mockSiteComponentLocatorService.locate(any())).thenReturn(mockSite);
		when(mockSite.getSessionSettings(any())).thenReturn(mockSiteBUGroupLoginProfileVO);
		when(mockSiteBUGroupLoginProfileVO.getLoginID()).thenReturn("IDC-CP-USER1");
		when(mockSiteBUGroupLoginProfileVO.getUserGroupName()).thenReturn("IDC-CP-GRP1");
		
		when(mockItemServiceComponentLocatorService.locate(any())).thenReturn(mockItemServicesComponent);
		when(mockItemServicesComponent.getWcssItemInformation(any(),any(),any())).thenReturn(mockItemRptVO);
		when(mockItemRptVO.getCustomerItemNumber()).thenReturn("14444");
		when(mockItemRptVO.getWallaceItemNumber()).thenReturn("255105");
		
		when(mockoaOrderAdminService.locate(any())).thenReturn(mockIOrderAdmin);
		when(mockIOrderAdmin.getGroupPlaceOrderDetails(any())).thenReturn(mockUserGroupOrderPropertiesVO);
		when(mockUserGroupOrderPropertiesVO.isSearchCatalogItemsOnlyInd()).thenReturn(true);
		
		when(mockItemValidationComponentLocatorService.locate(any())).thenReturn(mockItemValidation);
		
		Message errMesssage = new Message();
		AtWinXSMsgException actualEx = new AtWinXSMsgException(errMesssage,"myclass");
		
		when(mockItemValidation.isItemValidForCatalog(anyInt(),anyInt(),anyString(),
			anyInt(),anyString(),anyString(),anyString(),anyBoolean(),anyBoolean(),any()))
				.thenThrow(actualEx);
		
		try
		( 	MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
				) {

			//service = Mockito.spy(service);
			singleItemDetailAllocationsRequest = getSingleItemDetailAllocationsRequest();

			//With Customer Item Number = VALID and Vendor Item Number = VALID
			singleItemDetailAllocationsRequest.setCustomerItemNumber(TEST_VALID_CUSTOMER_ITEM_NUMBER);
			singleItemDetailAllocationsRequest.setVendorItemNumber(TEST_VALID_VENDOR_ITEM_NUMBER);
			singleItemDetailAllocationsResponse = service.retrieveItemAllocations(mockSessionContainer, singleItemDetailAllocationsRequest);
			assertFalse(singleItemDetailAllocationsResponse.isSuccess());
		}
	}
	

	//CAP-47145
  	private SingleItemDetailAllocationsRequest getSingleItemDetailAllocationsRequest() {
  		
  		SingleItemDetailAllocationsRequest singleItemDetailAllocationsRequest = new SingleItemDetailAllocationsRequest("14444","255105");
  		return singleItemDetailAllocationsRequest;
  	}

  	//CAP-47145
  	private ItemAllocations getItemAllocations() throws ParseException {
  		
  		ItemAllocations itemAllocations = new ItemAllocationsImpl();
  		
  		Collection<ItemAllocation> itemAllocationLst = new ArrayList<>();;
  		
  		ItemAllocation itemAllocation = new ItemAllocationImpl();
  		itemAllocation.setSalesRefNo("80033944");
  		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss"); 
  		Date date = dateformat.parse("2024-02-20 08:02:55"); 
  		itemAllocation.setOrderedDate(date);
  		itemAllocation.setOrderDateDisplay("02/20/2024");
  		itemAllocation.setQuantityOrdered(1);
  		itemAllocation.setRequestorName("IDC-CP-USER1");
  		itemAllocation.setOrderQt(1);
  		itemAllocation.setUOMFactor(1);
  		itemAllocationLst.add(itemAllocation);
		
  		itemAllocations.setItemAllocations(itemAllocationLst);
		return itemAllocations;
  	}
}

