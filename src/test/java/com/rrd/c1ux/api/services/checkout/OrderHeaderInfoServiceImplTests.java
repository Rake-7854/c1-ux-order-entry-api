/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  05/23/23    A Boomker       CAP-40687                   Initial
 *	06/06/23	A Boomker		CAP-38156					Updates for changes to load with review
 *	06/07/23	A Boomker		CAP-38154					Methods converted from CP for C1UX validation
 *  06/15/23	A Boomker		CAP-40687/CAP-38154			Fix validation of masks on cust refs
 *  06/16/23	A Boomker		CAP-41412					Tweaked cust ref length test to test do not show situation
 *  07/07/23	A Boomker		CAP-42122					Fix order title validation method
 *  07/12/23	L De Leon		CAP-41618/CAP-41619			Added method to update header info settings based on selected delivery method
 *  08/01/23	C Codina		CAP-41546					Added Junit testing on order header messages
 *  08/10/23	N Caceres		CAP-42169					Added unit testing for adding order header message fields in getOrderHeaderInfo response
 *  08/24/23	C Codina		CAP-43160					Add junits for the Save order header info API change
 *  08/17/23	N Caceres		CAP-41551					Add junits for saving carrierServiceLevel and thirdPartyAccount
 *  09/01/23	Krishna Natarajan	CAP-43382				updated tests to add object map factory for billing info methods, on missing existing test cases
 *  09/13/23	c Codina		CAP-42170					Add junits for oder shipping info
 *  10/13/23	Krishna Natarajan CAP-44103					Momentary handling added to setCarrierServiceLevel method
 *  11/06/23	Krishna Natarajan	CAP-45036				Commented 2 tests and correct one for the JIRA to get expedited
 *  10/09/23	N Caceres		CAP-44840					Add junits for saving and retrieving requested ship date
 *  01/09/2024	S Ramachandran	CAP-46294					Add order due date to Checkout pages
 *  04/09/2024	C Codina		CAP-48436					Add JUnits for DTD Service Call
 *  04/15/24	Satishkumar A	CAP-48437					C1UX BE - Modify /api/user/saveorderheaderinfo method to save information for DTD
 *	04/16/24	L De Leon		CAP-48457					Added test methods for doDateToDestination and sub methods
 *  04/18/24	S Ramachandran	CAP-48719					Added tests for setEarliestDeliveryInfo	
 *  05/07/24	Krishna Natarajan	CAP-49216				Added New lines to mock orderDueDateVO 
 *  05/20/24	Krishna Natarajan	CAP-49122				Added lines for special instructions and disabled a test
 *  05/23/24	M Sakthi			CAP-49452				Added JUnit for EfdEmailAddressesAndEfdDeliveryTypes
 *  07/09/24	Krishna Natarajan	CAP-50886				Added Junit mocks for mocking Order EntityObjectMap
 */
package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.checkout.DateToDestinationRequest;
import com.rrd.c1ux.api.models.checkout.DateToDestinationResponse;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoRequest;
import com.rrd.c1ux.api.models.checkout.OrderHeaderInfoResponse;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveRequest;
import com.rrd.c1ux.api.models.checkout.OrderInfoHeaderSaveResponse;
import com.rrd.c1ux.api.models.common.GenericNameValuePair;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.items.locator.CMCatalogComponentLocatorService;
import com.rrd.custompoint.framework.util.objectfactory.ComponentObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.ao.EFDDestinationOptionsFormBean;
import com.rrd.custompoint.orderentry.entity.EFDCRMTracking;
import com.rrd.custompoint.orderentry.entity.ExpeditedOrderService;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderDetailsBillingInfo;
import com.rrd.custompoint.orderentry.entity.OrderDetailsCustRefSettings;
import com.rrd.custompoint.orderentry.entity.OrderDetailsCustRefSettingsImpl;
import com.rrd.custompoint.orderentry.entity.OrderDetailsCustRefs;
import com.rrd.custompoint.orderentry.entity.OrderDetailsCustRefsImpl;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderCustRef;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderCustRefImpl;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderInfo;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderInfoImpl;
import com.rrd.custompoint.orderentry.entity.OrderDetailsMessages;
import com.rrd.custompoint.orderentry.entity.OrderDetailsMessagesImpl;
import com.rrd.custompoint.orderentry.entity.OrderDetailsShippingInfo;
import com.rrd.custompoint.orderentry.entity.OrderLineImpl;
import com.rrd.custompoint.orderentry.entity.OrderShipping;
import com.rrd.custompoint.orderentry.validators.entity.DueDateValidator;
import com.rrd.custompoint.validator.orderentry.OrderDetailsValidator;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.catalogs.vo.EFDSourceSetting;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.MaskFormatter;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOrderAdmin;
import com.wallace.atwinxs.orderentry.admin.util.OrderReferenceFieldList;
import com.wallace.atwinxs.orderentry.ao.EFDDestinationsFormBean;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEExtendedQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderSummaryResponseBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.session.OrderMessageComposite;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderDetailsFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderDueDateVO;
import com.wallace.atwinxs.orderentry.vo.OrderInfoShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;


class OrderHeaderInfoServiceImplTests extends BaseOEServiceTest {

	private OrderInfoHeaderSaveRequest saveRequest;
    private OrderInfoHeaderSaveResponse saveResponse;
    private OrderHeaderInfoRequest loadRequest;
    private OrderHeaderInfoResponse loadResponse;

    private static final String GENERIC_ERROR_MSG = "Failure message generic";
    private static final String REALLY_LONG_STRING = "INeed150CharactersToExceedThemaximumorder title ok and I can add spaces I forgot about that so I can add more while putting in a bunch of junk to fail the size.";
    private static final String BAD_WCSS_CHAR_VALUE = "blah ᓁ";
    private static final String TEST_ORDERTITLE = AtWinXSConstant.EMPTY_STRING;
    // CAP-41551
    private static final int ORDER_ID = 581863;
    private static final String SERVICE_TYPE_CD = "CA";
    private static final String COMPANY_ID = "FEDX";
    private static final String FREIGHT_CODE = "01";
    private static final String THIRD_PARTY_ACCOUNT = "TEST123456789";
    public static final String POBOX = "POBOX 12345";
    public static final String UPSS = "UPSS";
    private static final String NAME = "Lebron James";
	private static final String ADDRESS_LINE_2 = "Address Line 2";
	private static final String INVALID_DATE = "11/32/2023";
	private static final String PAST_DATE = "11/09/2023";

	// CAP-48457
	private static final String TEST_MESSAGE = "TEST_MESSAGE";
	private static final String DATE_PATTERN = "MM/dd/yyyy";
	private static final double TEST_CHARGE = 50d;
	private static final Date TEST_DATE = new Date(1700668800000l);

	@Mock
	protected OrderDetailsCustRefsImpl mockHeaderCustRefs;
    @Mock
    private OrderDetailsCustRefsImpl mockCustRefs;
    @Mock
    private OrderDetailsHeaderCustRefImpl mockCR1, mockCR2, mockCR3, mockCR5, mockCR6;

	@InjectMocks
	private OrderHeaderInfoServiceImpl service;

	@Mock
	private OEOrderSessionBean mockOEOrderSessionBean;

	@Mock
	private VolatileSessionBean mockVolatileSessionBean;

	@Mock
	private EntityObjectMap mockEntityObjectMap;

	// CAP-41618/CAP-41619
	@Mock
	private ComponentObjectMap mockComponentObjectMap;

	@Mock
	private OrderShippingVO mockOrderShippingVo;

	@Mock
	private OrderDetailsHeaderInfoImpl mockHeaderInfo;

	@Mock
	private OrderDetailsCustRefs mockOrderDetailsCustRefs;

	@Mock
	private CustomizationToken mockCustomizationToken;

	@Mock
	private OrderEntrySession mockOrderEntrySession;

	@Mock
	private ApplicationVolatileSession mockApplicationVolatileSession;

	@Mock
	private OrderDetailsValidator validator;

	@Mock
	protected Collection<OrderMessageComposite> mockOrderMessageComposite;
  
	@Mock
	protected OrderDetailsBillingInfo mockBillingInfo;

	@Mock
	protected OrderDetailsMessages mockOrderDetailsMessages;
	
    @Mock
    protected OrderShipping mockOrderShipping;
    
    @Mock
    protected OrderDetailsShippingInfo mockOrderDetailsShipping;
    
    @Mock
    protected XSProperties mockOEProperty;
    
    @Mock
    OrderDetailsShippingInfo mockShippingInfo;
    
    @Mock
    OrderInfoShippingVO mockOrderInfoShippingVO;	// CAP-49118
    
    @Mock
    IOrderAdmin mockIOrderAdmin;
    
	@Mock
	DueDateValidator mockDueDateValidator;

	@Mock
	OrderDueDateVO mockOrderDueDateVO;

	@Mock
	Map<String, Object> mockReplaceMap;

	@Mock
	ExpeditedOrderService mockExpeditedOrderService;

	@Mock
	XSCurrency mockXSCurrency;
	
	
	@Mock
	CMCatalogComponentLocatorService mockCMCatalogComponentLocatorService;
	
	@Mock
	OEAssemblerFactoryService mockOEAssemblerFactoryService;
	
	@Mock
	ICatalog mockICatalog;
	
	@Mock
	CatalogDefaultVO mockCatalogDefaultVO;
	
	@Mock
	EFDSourceSetting mockEFDSourceSetting;
	
	@Mock
	OECheckoutAssembler mockOECheckoutAssembler;
	
	@Mock
	OEOrderSummaryResponseBean mockOEOrderSummaryResponseBean;
	
	@Mock
	EFDDestinationOptionsFormBean mockEFDDestinationOptionsFormBean;
	
	@Mock
	EFDCRMTracking mockEFDCRMTracking;
	
	@Mock
	OEExtendedQuantityResponseBean mockOEExtendedQuantityResponseBean;
	
	@Mock
	OEExtendedItemQuantityResponseBean mockOEExtendedItemQuantityResponseBean;
	
	@BeforeEach
	public void setup() {
		saveRequest = new OrderInfoHeaderSaveRequest();
		loadResponse = new OrderHeaderInfoResponse();
	}

  @Test
  void that_loadHeaderInfo_returns_OrderDetailsHeaderInfo() throws Exception {
	  
      when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
      when(mockEntityObjectMap.getEntity(eq(OrderDetailsHeaderInfo.class), any())).thenReturn(mockHeaderInfo);
      when(mockEntityObjectMap.getEntity(eq(OrderDetailsCustRefs.class), any())).thenReturn(mockOrderDetailsCustRefs);
      //CAP-41546
      when(mockEntityObjectMap.getEntity(eq(OrderDetailsMessages.class), any())).thenReturn(mockOrderDetailsMessages);
      when(mockHeaderInfo.getOrderDetailsMessages()).thenReturn(buildOrderHeaderMessages());
      
      setUpObjectMapFactory();
      when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(mockOrderShippingVo);
     
      
      ArrayList<String> errMsgs = new ArrayList<>();
      doNothing().when(mockHeaderInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean, List.of());
      doNothing().when(mockOrderDetailsCustRefs).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean,
          mockHeaderInfo, List.of());
      doNothing().when(mockOrderDetailsMessages).populate(mockOEOrderSessionBean, mockAppSessionBean, mockVolatileSessionBean, TEST_ORDERTITLE);

      Assertions.assertNotNull(service.loadHeaderInfo(mockAppSessionBean, mockOEOrderSessionBean, mockVolatileSessionBean, errMsgs,TEST_ORDERTITLE));
  }

	
	void that_saveOrderHeaderInfo_returns_saveOrderShippingInfo() throws Exception {
		service = Mockito.spy(service);
		doNothing().when(service).saveUpdatedHeaderInfo(any(), any(), any(), any(), any());
		setUpSaveOrderHeaderInfo();
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
	    when(mockEntityObjectMap.getEntity(eq(OrderShipping.class), any())).thenReturn(mockOrderShipping);
		OrderInfoHeaderSaveResponse result = service.saveOrderHeaderInfo(mockSessionContainer, saveRequest);
		Assertions.assertNotNull(result);
	}

  @Test
  void that_saveOrderHeaderInfo_has_errmsg_onload() throws Exception {

    service = Mockito.spy(service);

    setUpModuleSession();
    doReturn(true).when(service).validateOrder(any(OrderInfoHeaderSaveResponse.class), any(SessionContainer.class), any(AppSessionBean.class));

    doAnswer(new Answer<OrderDetailsHeaderInfo>() {
      @Override
      public OrderDetailsHeaderInfo answer(InvocationOnMock invocation) throws Throwable {
        List<String> errMsg = invocation.getArgument(3);
        errMsg.add("this is my error msg");
        return mockHeaderInfo;
      }
    }).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
        any(VolatileSessionBean.class), any(), any());
    
	doAnswer(new Answer<OrderDetailsShippingInfo>() {//CAP-45036
		@Override
		public OrderDetailsShippingInfo answer(InvocationOnMock invocation) throws Throwable {
			List<String> errMsg = invocation.getArgument(3);
			errMsg.add("this is my error msg");
			return mockShippingInfo;
		}
	}).when(service).loadShippingInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
			any(VolatileSessionBean.class), any(), any(), any(), any());

    saveResponse = service.saveOrderHeaderInfo(mockSessionContainer, saveRequest);

    Assertions.assertNotNull(saveResponse);
    assertFalse(saveResponse.isSuccess());
    assertFalse(Util.isBlankOrNull(saveResponse.getMessage()));
  }

  @Test
  void that_loadOrderHeaderInfo_has_errmsg_onload() throws Exception {

	  try (MockedConstruction<OrderDetailsValidator> mockedValidator =
			    		Mockito.mockConstruction(OrderDetailsValidator.class, (mock, context) -> {

			        Mockito.when(mock.validate(any(OrderDetailsFormBean.class), any(Message.class), any(OEOrderSessionBean.class),
			     			  any(AppSessionBean.class), anyBoolean(), any(VolatileSessionBean.class), anyBoolean()))
			        		.thenAnswer(new Answer<Boolean>() {
			            @Override
			            public Boolean answer(InvocationOnMock invocation) throws Throwable {
			            	Message msg = invocation.getArgument(1);
			            	msg.getErrMsgItems().add(GENERIC_ERROR_MSG);
			              return false;
			            }
			       	  });}
				)) {

	      when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
	      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsHeaderInfo.class), any())).thenReturn(mockHeaderInfo);
	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsCustRefs.class), any())).thenReturn(mockOrderDetailsCustRefs);
	      setUpObjectMapFactory();
	      when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(mockOrderShippingVo);
	      //CAP-41546
	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsMessages.class), any())).thenReturn(mockOrderDetailsMessages);
	      when(mockHeaderInfo.getOrderDetailsMessages()).thenReturn(buildOrderHeaderMessages());
	      
	      ArrayList<String> errMsgs = new ArrayList<>();
	      doNothing().when(mockHeaderInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean, List.of());
	      doNothing().when(mockOrderDetailsCustRefs).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean,
	          mockHeaderInfo, List.of());

	      Assertions.assertNotNull(service.loadHeaderInfo(mockAppSessionBean, mockOEOrderSessionBean, mockVolatileSessionBean, errMsgs, TEST_ORDERTITLE));

			// CAP-43382
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsBillingInfo.class), any())).thenReturn(mockBillingInfo);

			service = Mockito.spy(service);

	        setUpModuleSession();

		    doReturn(true).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
//		    doReturn(true).when(service).validateFields(any(OrderDetailsHeaderInfo.class), any(BaseResponse.class), any(AppSessionBean.class), anyMap(), any(OEOrderSessionBean.class));

		    doAnswer(new Answer<OrderDetailsHeaderInfo>() {
		    	@Override
		      public OrderDetailsHeaderInfo answer(InvocationOnMock invocation) throws Throwable {
		        List<String> errMsg = invocation.getArgument(3);
		        errMsg.add("this is my error msg");
		        return mockHeaderInfo;
		      }
		    }).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
		        any(VolatileSessionBean.class), any(), any());

		    loadRequest = new OrderHeaderInfoRequest();

		    //CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    
		    // CAP-48719
			doNothing().when(service).setEarliestDeliveryInfo(any(AppSessionBean.class),
					any(VolatileSessionBean.class), any(OrderHeaderInfoResponse.class));
			
			// CAP-49118
			when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		    when(mockIOEManageOrdersComponent.getOrderInfoShipping(any())).thenReturn(mockOrderInfoShippingVO);
			//CAP-49216
		    when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
		    when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
		    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-50886
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertFalse(loadResponse.isSuccess());
		    assertFalse(loadResponse.isValidAndComplete());
		    assertFalse(Util.isBlankOrNull(loadResponse.getMessage()));
	  }
  }


  @Test
  void that_loadOrderHeaderInfo_hasno_errmsg_onvalidate() throws Exception {
      when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
//      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
//      when(mockEntityObjectMap.getEntity(eq(OrderDetailsHeaderInfo.class), any())).thenReturn(mockHeaderInfo);
//      when(mockEntityObjectMap.getEntity(eq(OrderDetailsCustRefs.class), any())).thenReturn(mockOrderDetailsCustRefs);

      ArrayList<String> errMsgs = new ArrayList<>();
//      doNothing().when(mockHeaderInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean, List.of());
//      doNothing().when(mockOrderDetailsCustRefs).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean,
//          mockHeaderInfo, List.of());

	  try (MockedConstruction<OrderDetailsValidator> mockedValidator =
			    		Mockito.mockConstruction(OrderDetailsValidator.class, (mock, context) -> {

			        Mockito.when(mock.validate(any(OrderDetailsFormBean.class), any(Message.class), any(OEOrderSessionBean.class),
			     			  any(AppSessionBean.class), anyBoolean(), any(VolatileSessionBean.class), anyBoolean()))
			        		.thenAnswer(new Answer<Boolean>() {
			            @Override
			            public Boolean answer(InvocationOnMock invocation) throws Throwable {
			            	Message msg = invocation.getArgument(1);
			              return true;
			            }
			       	  });}
				)) {


		    service = Mockito.spy(service);

	        setUpModuleSession();

		    doReturn(true).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
		    doReturn(true).when(service).validateFields(any(OrderDetailsHeaderInfo.class), any(BaseResponse.class), any(AppSessionBean.class), anyMap(), any(OEOrderSessionBean.class));
			doNothing().when(service).loadExtendedItemQuantity(any(),anyInt(), any(OrderHeaderInfoResponse.class), any(AppSessionBean.class), any(OEOrderSessionBean.class), any(VolatileSessionBean.class));

		    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
		        any(VolatileSessionBean.class), any(), any());

		    loadRequest = new OrderHeaderInfoRequest();
		    // with review false, there will be no attempt to load item info
		    
			// CAP-43382
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsBillingInfo.class), any())).thenReturn(mockBillingInfo);
			when(mockBillingInfo.isCreditCardOptional()).thenReturn(false);
			when(mockBillingInfo.isCreditCardRequired()).thenReturn(false);
		     
			//CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    
		    // CAP-48719
			doNothing().when(service).setEarliestDeliveryInfo(any(AppSessionBean.class),
					any(VolatileSessionBean.class), any(OrderHeaderInfoResponse.class));
			
			// CAP-49118
			when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		    when(mockIOEManageOrdersComponent.getOrderInfoShipping(any())).thenReturn(mockOrderInfoShippingVO);
		    //CAP-49216
		    when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
		    when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
		    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-50886
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertTrue(loadResponse.isSuccess());
		    assertTrue(loadResponse.isValidAndComplete());
		    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

		    loadRequest.setReview(true);
		    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
		    errMsgs.clear();
		    // since review is true, it'll attempt to load item info now
		    //CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertTrue(loadResponse.isSuccess());
		    assertTrue(loadResponse.isValidAndComplete());
		    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

		    when(mockHeaderInfo.isFirstVisit()).thenReturn(true);
		    errMsgs.clear();
		    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());

		    //CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);
		    // if you attempt to do review = true when firstVisit is true, we're going to specifically error now
		    Assertions.assertNotNull(loadResponse);
		    assertFalse(loadResponse.isSuccess());
		    assertFalse(loadResponse.isValidAndComplete());
		    assertFalse(Util.isBlankOrNull(loadResponse.getMessage()));

		    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
			        any(VolatileSessionBean.class), any(), any());
		    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
		    errMsgs.clear();

		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertTrue(loadResponse.isSuccess());
		  }

  }

  @Test
  void that_getOrderHeaderInfo_has_errmsg_onCPload() throws Exception {
		try (MockedConstruction<OrderDetailsValidator> mockedValidator =
			    		Mockito.mockConstruction(OrderDetailsValidator.class, (mock, context) -> {

			        Mockito.when(mock.validate(any(OrderDetailsFormBean.class), any(Message.class), any(OEOrderSessionBean.class),
			     			  any(AppSessionBean.class), anyBoolean(), any(VolatileSessionBean.class), anyBoolean()))
			        		.thenAnswer(new Answer<Boolean>() {
			            @Override
			            public Boolean answer(InvocationOnMock invocation) throws Throwable {
			            	Message msg = invocation.getArgument(1);
			            	msg.getErrMsgItems().add(GENERIC_ERROR_MSG);
			              return false;
			            }
			       	  });}
				);
			MockedConstruction<OECheckoutAssembler> mockedAssembler = Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
			  when(mock.getExtendedQuantitySummary(anyInt(), any(OEOrderSessionBean.class), any(AppSessionBean.class), any())).thenThrow(new RuntimeException("simulated exception"));
			})) {

		    service = Mockito.spy(service);

	        setUpModuleSession();

		    doReturn(true).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
		    doReturn(true).when(service).validateFields(any(OrderDetailsHeaderInfo.class), any(BaseResponse.class), any(AppSessionBean.class), anyMap(), any(OEOrderSessionBean.class));

		    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
		        any(VolatileSessionBean.class), any(), any());

		    loadRequest = new OrderHeaderInfoRequest();

		    loadRequest.setReview(true);
			// CAP-43382
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsBillingInfo.class), any())).thenReturn(mockBillingInfo);
		      
		    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
		    //CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    
		    // CAP-48719
			doNothing().when(service).setEarliestDeliveryInfo(any(AppSessionBean.class),
					any(VolatileSessionBean.class), any(OrderHeaderInfoResponse.class));
			
			// CAP-49118
			when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		    when(mockIOEManageOrdersComponent.getOrderInfoShipping(any())).thenReturn(mockOrderInfoShippingVO);
		    //CAP-49216
		    when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
		    when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
		    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-50886
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertFalse(loadResponse.isSuccess());
		  }

  }
  @Test
  void that_loadOrderHeaderInfo_validandcomplete_onload() throws Exception {
		try (MockedConstruction<OrderDetailsValidator> mockedValidator =
			    		Mockito.mockConstruction(OrderDetailsValidator.class, (mock, context) -> {

			        Mockito.when(mock.validate(any(OrderDetailsFormBean.class), any(Message.class), any(OEOrderSessionBean.class),
			     			  any(AppSessionBean.class), anyBoolean(), any(VolatileSessionBean.class), anyBoolean()))
			        		.thenAnswer(new Answer<Boolean>() {
			            @Override
			            public Boolean answer(InvocationOnMock invocation) throws Throwable {
			            	Message msg = invocation.getArgument(1);
			            	msg.getErrMsgItems().clear();
			              return true;
			            }
			       	  });}
				)) {

		    service = Mockito.spy(service);

	        setUpModuleSession();
		    doReturn(true).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
		    doReturn(true).when(service).validateFields(any(OrderDetailsHeaderInfo.class), any(BaseResponse.class), any(AppSessionBean.class), anyMap(), any(OEOrderSessionBean.class));
			doNothing().when(service).loadExtendedItemQuantity(any(),anyInt(), any(OrderHeaderInfoResponse.class), any(AppSessionBean.class), any(OEOrderSessionBean.class), any(VolatileSessionBean.class));
		    doAnswer(new Answer<OrderDetailsHeaderInfo>() {
			      @Override
			      public OrderDetailsHeaderInfo answer(InvocationOnMock invocation) throws Throwable {
			        List<String> errMsg = invocation.getArgument(3);
			        errMsg.clear();
			        return mockHeaderInfo;
			      }
			    }).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
			        any(VolatileSessionBean.class), any(), any());

		    loadRequest = new OrderHeaderInfoRequest();

		    when(mockHeaderInfo.isFirstVisit()).thenReturn(true);
		    
		    //CAP-43382
		      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		      when(mockEntityObjectMap.getEntity(eq(OrderDetailsBillingInfo.class), any())).thenReturn(mockBillingInfo);
		      //CAP-42170
		      when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
		      when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		      doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    
		    // CAP-48719
			doNothing().when(service).setEarliestDeliveryInfo(any(AppSessionBean.class),
					any(VolatileSessionBean.class), any(OrderHeaderInfoResponse.class));
			
			// CAP-49118
			when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		    when(mockIOEManageOrdersComponent.getOrderInfoShipping(any())).thenReturn(mockOrderInfoShippingVO);
		    //CAP-49216
		    when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
		    when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
		    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-50886
		      loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertTrue(loadResponse.isSuccess());
		    assertFalse(loadResponse.isValidAndComplete());
		    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

		    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
		    
		    //CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertTrue(loadResponse.isSuccess());
		    assertTrue(loadResponse.isValidAndComplete());
		    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

		    loadRequest.setReview(true);
		    //CAP-42170
			when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
			when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

		    Assertions.assertNotNull(loadResponse);
		    assertTrue(loadResponse.isSuccess());
		    assertTrue(loadResponse.isValidAndComplete());
		    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));
		  }
  }

  @Test
  void that_saveOrderHeaderInfo_has_errmsg_onvalidate() throws Exception {

    service = Mockito.spy(service);

    setUpModuleSession();

    doAnswer(new Answer<Boolean>() {
        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
        	BaseResponse answerThis = invocation.getArgument(0);
          	answerThis.setFieldMessage("orderTitle", "something");
          return false;
        }
   	  }).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));


    saveResponse = service.saveOrderHeaderInfo(mockSessionContainer, saveRequest);

    Assertions.assertNotNull(saveResponse);
    assertFalse(saveResponse.isSuccess());
    assertTrue(Util.isBlankOrNull(saveResponse.getMessage()));
    assertFalse(saveResponse.getFieldMessages().isEmpty());
  }

  @Test
  void that_saveOrderHeaderInfo_is_not_valid_order() throws Exception {

    service = Mockito.spy(service);

    OrderInfoHeaderSaveRequest request = new OrderInfoHeaderSaveRequest();

    setUpModuleSession();

    doReturn(false).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));

    OrderInfoHeaderSaveResponse result = service.saveOrderHeaderInfo(mockSessionContainer, request);

    Assertions.assertNotNull(result);
  }

  @Test
  void that_validationMethods_work() throws Exception {

	    service = Mockito.spy(service);

	    OrderInfoHeaderSaveResponse response = new OrderInfoHeaderSaveResponse();
	    Map<String, String> translationMap = makeNewTranslationMap();

	    when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
	    when(mockUserSettings.isAlwUsrToEnterMultpleEmails()).thenReturn(false);
	    doReturn(false).when(service).validatePONumber(eq(mockHeaderInfo), any(OrderInfoHeaderSaveResponse.class), eq(mockAppSessionBean), any(), any(), any());
	    doReturn(true).when(service).validateOrderTitle(mockHeaderInfo, response, mockAppSessionBean, translationMap);
	    doReturn(true).when(service).validateContactName(mockHeaderInfo, response, mockAppSessionBean, translationMap);
	    doReturn(true).when(service).validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, false, translationMap);
	    doReturn(true).when(service).validateContactPhone(mockHeaderInfo, response, mockAppSessionBean, translationMap);
	    doReturn(true).when(service).validateHeaderCustRefs(mockHeaderInfo, response, mockAppSessionBean);
	    doReturn(true).when(service).validateOrderDetailMessage(mockHeaderInfo, response, mockAppSessionBean);
	    assertFalse(service.validateFields(mockHeaderInfo, response, mockAppSessionBean, translationMap, mockOEOrderSessionBean));

	    doReturn(true).when(service).validatePONumber(mockHeaderInfo, response, mockAppSessionBean, null, null, null);
	    assertTrue(service.validateFields(mockHeaderInfo, response, mockAppSessionBean, translationMap, mockOEOrderSessionBean));
  }

	@Test
	void that_po_validationMethods_error2long() throws Exception {

		BaseResponse response = new OrderInfoHeaderSaveResponse();
		String fieldLabel = "poNumber";

		when(mockHeaderInfo.isAllowPONumberEdit()).thenReturn(true);
		when(mockHeaderInfo.getPoNumber()).thenReturn("ASTRINGOVER20CHARACTERSLONG");
		assertFalse(service.validatePONumber(mockHeaderInfo, response, mockAppSessionBean, null, null, null));
		assertFalse(response.getFieldMessages().isEmpty());
		assertNotNull(response.getFieldMessage(fieldLabel));
	}

	@Test
	void that_po_validationMethods_errormaskfails() throws Exception {

	  try (MockedConstruction<MaskFormatter> mockedMaskFormatter = Mockito.mockConstruction(MaskFormatter.class,
	      (mock, context) -> {
	        doThrow(new ParseException("foo", 0)).when(mock).stringToValue(anyString());
	      })) {

	      BaseResponse response = new OrderInfoHeaderSaveResponse();
	      String fieldLabel = "poNumber";

	      when(mockHeaderInfo.isAllowPONumberEdit()).thenReturn(true);

	      String poLabel = "Test PO Label";
	      String poMask = "UUUUU";
	      when(mockHeaderInfo.getPoNumber()).thenReturn("SHORTERTHAN20CHARS");

	      assertFalse(service.validatePONumber(mockHeaderInfo, response, mockAppSessionBean, poMask, poLabel, null));
	      assertNotNull(response.getFieldMessage(fieldLabel));
	  }
	}

	@Test
	void that_po_validationMethods_success() throws Exception {
      try (MockedConstruction<MaskFormatter> mockedMaskFormatter = Mockito.mockConstruction(MaskFormatter.class,
          (mock, context) -> {
            doReturn("foo").when(mock).stringToValue(anyString());
          })) {

	    BaseResponse response = new OrderInfoHeaderSaveResponse();

	    when(mockHeaderInfo.isAllowPONumberEdit()).thenReturn(true);

	    String poLabel = "Test PO Label";
	    String poMask = "UUUUU";

	    when(mockHeaderInfo.getPoNumber()).thenReturn("ABCDE");
	    assertTrue(service.validatePONumber(mockHeaderInfo, response, mockAppSessionBean, poMask, poLabel, null));
	    assertTrue(response.getFieldMessages().isEmpty());

		  when(mockHeaderInfo.getPoNumber()).thenReturn(BAD_WCSS_CHAR_VALUE);
		  response = new OrderInfoHeaderSaveResponse();
		  // bad wcss char should fail
		  assertFalse(service.validatePONumber(mockHeaderInfo, response, mockAppSessionBean, AtWinXSConstant.EMPTY_STRING, poLabel, null));
		  assertFalse(response.getFieldMessages().isEmpty());
		  assertTrue(response.getFieldMessages().containsKey(OrderHeaderInfoServiceImpl.PO_NUMBER_FIELD_NAME));

	  }
	}

	@Test
	void that_contactEmail_validationMethods() throws Exception {
		try (MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			  	mockUtil.when(() -> Util.isEmailDelimited(any())).thenReturn(false);

	  BaseResponse response = new OrderInfoHeaderSaveResponse();
	  Map<String, String> translationMap = makeNewTranslationMap();
	  when(mockHeaderInfo.getContactEmail()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString(), any());

	  // empty should fail
	  assertFalse(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, false, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactEmail()).thenReturn("Below Max Length");
	  response = new OrderInfoHeaderSaveResponse();
	  // value not in an email format should fail
	  assertFalse(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, false, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactEmail()).thenReturn(REALLY_LONG_STRING);
	  response = new OrderInfoHeaderSaveResponse();
	  // value too long should fail
	  assertFalse(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, false, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactEmail()).thenReturn(BAD_WCSS_CHAR_VALUE);
	  response = new OrderInfoHeaderSaveResponse();
	  // bad wcss char should fail
	  assertFalse(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, false, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  	mockUtil.when(() -> Util.isEmailDelimited(any())).thenReturn(false);
	  	mockUtil.when(() -> Util.isValidEmailFormat(any())).thenReturn(true);

	  when(mockHeaderInfo.getContactEmail()).thenReturn("amy@gmail.com");
	  response = new OrderInfoHeaderSaveResponse();
	  // single good email should succeed
	  assertTrue(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, false, translationMap));
	  assertTrue(response.getFieldMessages().isEmpty());

	  	mockUtil.when(() -> Util.isEmailDelimited(any())).thenReturn(true);
	  	mockUtil.when(() -> Util.isValidEmailFormat(any())).thenReturn(true);

	  	String multipleEmails = "amy@gmail.com,jim@gmail.com";
	  when(mockHeaderInfo.getContactEmail()).thenReturn(multipleEmails);
	  mockUtil.when(() -> Util.nullToEmpty(multipleEmails)).thenReturn(multipleEmails);

	  response = new OrderInfoHeaderSaveResponse();
	  // multiple good email should succeed
	  assertTrue(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, true, translationMap));
	  assertTrue(response.getFieldMessages().isEmpty());

	  	mockUtil.when(() -> Util.isEmailDelimited(any())).thenReturn(true);
	  	String multipleEmails2 = "amy@gmail.com,jim";
	  when(mockHeaderInfo.getContactEmail()).thenReturn(multipleEmails2);
	  mockUtil.when(() -> Util.nullToEmpty(multipleEmails2)).thenReturn(multipleEmails2);

	  	mockUtil.when(() -> Util.isValidEmailFormat("amy@gmail.com")).thenReturn(true);
	  	mockUtil.when(() -> Util.isValidEmailFormat("jim")).thenReturn(false);

	  response = new OrderInfoHeaderSaveResponse();
	  // one good email and one bad email should fail
	  assertFalse(service.validateContactEmail(mockHeaderInfo, response, mockAppSessionBean, true, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());
	  assertTrue(response.getFieldMessages().containsKey(OrderHeaderInfoServiceImpl.CONTACT_EMAIL_FIELD_NAME));
		}
	}

	private Map<String, String> makeNewTranslationMap() {
		Map<String, String> translationMap = new HashMap<>();
		translationMap.put(OrderHeaderInfoServiceImpl.ORDER_TITLE_FIELD_NAME, "Order Title");
		translationMap.put(OrderHeaderInfoServiceImpl.PO_NUMBER_FIELD_NAME, "PO Number");
		translationMap.put(OrderHeaderInfoServiceImpl.CONTACT_EMAIL_FIELD_NAME, "Email Address");
		translationMap.put(OrderHeaderInfoServiceImpl.CONTACT_PHONE_FIELD_NAME, "Phone Number");
		translationMap.put(OrderHeaderInfoServiceImpl.CONTACT_NAME_FIELD_NAME, "Contact Name");
		return translationMap;
	}

	@Test
	void that_contactName_validationMethods() throws Exception {

	  BaseResponse response = new OrderInfoHeaderSaveResponse();
	  when(mockHeaderInfo.getContactName()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString(), any());

	  Map<String, String> translationMap = makeNewTranslationMap();
	  // contact name required so blank must fail
	  assertFalse(service.validateContactName(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactName()).thenReturn("Below Max Length");
	  response = new OrderInfoHeaderSaveResponse();
	  // valid contact name should succeed
	  assertTrue(service.validateContactName(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertTrue(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactName()).thenReturn(REALLY_LONG_STRING);
	  response = new OrderInfoHeaderSaveResponse();
	  // contact name too long should fail
	  assertFalse(service.validateContactName(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactName()).thenReturn(BAD_WCSS_CHAR_VALUE);
	  response = new OrderInfoHeaderSaveResponse();
	  // bad wcss char should fail
	  assertFalse(service.validateContactName(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());
	  assertTrue(response.getFieldMessages().containsKey(OrderHeaderInfoServiceImpl.CONTACT_NAME_FIELD_NAME));
	}

	@Test
	void that_contactPhone_validationMethods() throws Exception {
	  BaseResponse response = new OrderInfoHeaderSaveResponse();
	  when(mockHeaderInfo.getContactPhone()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());
//	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString(), any());

	  Map<String, String> translationMap = makeNewTranslationMap();
	  // contact name required so blank must fail
	  assertFalse(service.validateContactPhone(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactPhone()).thenReturn("18005882300");
	  response = new OrderInfoHeaderSaveResponse();
	  // valid contact name should succeed
	  assertTrue(service.validateContactPhone(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertTrue(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactPhone()).thenReturn(REALLY_LONG_STRING);
	  response = new OrderInfoHeaderSaveResponse();
	  // contact name too long should fail
	  assertFalse(service.validateContactPhone(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getContactPhone()).thenReturn(BAD_WCSS_CHAR_VALUE);
	  response = new OrderInfoHeaderSaveResponse();
	  // bad wcss char should fail
	  assertFalse(service.validateContactPhone(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());
	  assertTrue(response.getFieldMessages().containsKey(OrderHeaderInfoServiceImpl.CONTACT_PHONE_FIELD_NAME));
	}
	@Test
	void that_ordertitle_validationMethod() throws Exception {
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString(), any());

	  BaseResponse response = new OrderInfoHeaderSaveResponse();
	  Map<String, String> translationMap = new HashMap<>();
	  translationMap.put(OrderHeaderInfoServiceImpl.ORDER_TITLE_FIELD_NAME, "Order Title");
	  when(mockHeaderInfo.isEnableOrderTitle()).thenReturn(true);	  	 // CAP-42122
	  when(mockHeaderInfo.isOrderTitleReq()).thenReturn(false);
	  // empty value when not required is fine
	  assertTrue(service.validateOrderTitle(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertTrue(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.isOrderTitleReq()).thenReturn(true);
	  response = new OrderInfoHeaderSaveResponse();
	  // required value that is missing should fail
	  assertFalse(service.validateOrderTitle(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());

	  when(mockHeaderInfo.getOrderTitle()).thenReturn(REALLY_LONG_STRING);
	  response = new OrderInfoHeaderSaveResponse();
	  // value too long should fail
	  assertFalse(service.validateOrderTitle(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertFalse(response.getFieldMessages().isEmpty());
	  assertTrue(response.getFieldMessages().containsKey(OrderHeaderInfoServiceImpl.ORDER_TITLE_FIELD_NAME));

	  when(mockHeaderInfo.isEnableOrderTitle()).thenReturn(false);	 // CAP-42122
	  when(mockHeaderInfo.getOrderTitle()).thenReturn("ABCDE");
	  response = new OrderInfoHeaderSaveResponse();
	  // valid value should succeed
	  assertTrue(service.validateOrderTitle(mockHeaderInfo, response, mockAppSessionBean, translationMap));
	  assertTrue(response.getFieldMessages().isEmpty());
	}

	protected void mockCustRef(OrderDetailsHeaderCustRef mockCustRef, String code, int size, String value)
	{
	  when(mockCustRef.getSelectedCustRef()).thenReturn(value);
	  if (!"".equals(value)) {
	    when(mockCustRef.getCustRefLength()).thenReturn(size);
//	    if (value.length() > size) {
	      when(mockCustRef.getCustRefCode()).thenReturn(code);
//	    }
	  }
	}

	@Test
	void that_custrefs_validationMethods_success() throws Exception {
	  setUpTranslationServiceErrors();
	    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());

	  Collection<OrderDetailsHeaderCustRef> headerCustRefs = new ArrayList<>();
	  headerCustRefs.add(mockCR3);
	  headerCustRefs.add(mockCR5);
	  headerCustRefs.add(mockCR6);
	  String cr1Code = OrderAdminConstants.ORD_REF_CUST_REF_PREFIX_CD + 1;
	  String cr2Code = OrderAdminConstants.ORD_REF_CUST_REF_PREFIX_CD + 2;
	  String cr3Code = OrderAdminConstants.ORD_REF_CUST_REF_PREFIX_CD + 3;
	  String cr5Code = OrderAdminConstants.ORD_REF_CUST_REF_PREFIX_CD + 5;
	  String cr6Code = OrderAdminConstants.ORD_REF_CUST_REF_PREFIX_CD + 6;
	  when(mockHeaderInfo.getOrderDetailsHeaderCustRefs()).thenReturn(headerCustRefs);
	  mockCustRef(mockCR1, cr1Code, 20, REALLY_LONG_STRING);
	  mockCustRef(mockCR2, cr2Code, 40, REALLY_LONG_STRING);
	  mockCustRef(mockCR3, cr3Code, 40, AtWinXSConstant.EMPTY_STRING);
	  mockCustRef(mockCR5, cr5Code, 40, "ABC");
	  mockCustRef(mockCR6, cr6Code, 3, "ABC");

	  when(mockCR3.isRequireCustRef()).thenReturn(false);
	  BaseResponse response = new OrderInfoHeaderSaveResponse();
	  assertTrue(service.validateHeaderCustRefs(mockHeaderInfo, response, mockAppSessionBean));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr1Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr2Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr3Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr5Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr6Code));

	  headerCustRefs = new ArrayList<>();
	  headerCustRefs.add(mockCR1);
	  headerCustRefs.add(mockCR2);
	  headerCustRefs.add(mockCR3);
	  headerCustRefs.add(mockCR5);
	  headerCustRefs.add(mockCR6);
	  response = new OrderInfoHeaderSaveResponse();
	  when(mockHeaderInfo.getOrderDetailsHeaderCustRefs()).thenReturn(headerCustRefs);
	  when(mockCR3.isRequireCustRef()).thenReturn(true);

	  assertFalse(service.validateHeaderCustRefs(mockHeaderInfo, response, mockAppSessionBean));
	  Assertions.assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr1Code));
	  Assertions.assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr2Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr3Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr5Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr6Code));

	  response = new OrderInfoHeaderSaveResponse();
	  when(mockCR1.getShowCustRef()).thenReturn(OrderAdminConstants.SHOW_CUST_REF_DO_NOT_SHOW_CD);

	  assertFalse(service.validateHeaderCustRefs(mockHeaderInfo, response, mockAppSessionBean));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr1Code));
	  Assertions.assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr2Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr3Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr5Code));
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getFieldMessage(cr6Code));

	}

	// CAP-41618/CAP-41619
	@Test
	void that_updateInfoSettingsForDeliverySelected_altersCustRefs() throws Exception {
		List<OrderDetailsHeaderCustRef> headerCustRefs = new ArrayList<>();
		OrderDetailsHeaderCustRef cr1 = new OrderDetailsHeaderCustRefImpl();
		cr1.setRequireCustRef(true);
		OrderDetailsHeaderCustRef cr2 = new OrderDetailsHeaderCustRefImpl();
		cr2.setRequireCustRef(false);
		cr2.setForceCustRefOnNewShip(false);
		OrderDetailsHeaderCustRef cr3 = new OrderDetailsHeaderCustRefImpl();
		cr3.setRequireCustRef(false);
		cr3.setForceCustRefOnNewShip(true);
		headerCustRefs.add(cr1);
		headerCustRefs.add(cr2);
		headerCustRefs.add(cr3);

		when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		setUpObjectMapFactory();
		when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(null, mockOrderShippingVo);
		when(mockOrderShippingVo.getWcssShipToNum()).thenReturn("1234", AtWinXSConstant.EMPTY_STRING);
		when(mockHeaderInfo.getOrderDetailsHeaderCustRefs()).thenReturn(null, null, Collections.emptyList(),
				headerCustRefs);

		// shipping address is null, looping through headerCustRefs is skipped
		service.updateInfoSettingsForDeliverySelected(mockHeaderInfo, mockAppSessionBean);
		assertNotNull(mockHeaderInfo);

		// shipping address is not null, headerCustRefs is null, looping through
		// headerCustRefs is skipped
		service.updateInfoSettingsForDeliverySelected(mockHeaderInfo, mockAppSessionBean);
		assertNotNull(mockHeaderInfo);

		// shipping address is not null, headerCustRefs is empty, looping through
		// headerCustRefs is skipped
		service.updateInfoSettingsForDeliverySelected(mockHeaderInfo, mockAppSessionBean);
		assertNotNull(mockHeaderInfo);

		// shipping address is not null but is CML, headerCustRefs is not empty,
		// headerCustRefs should not change
		service.updateInfoSettingsForDeliverySelected(mockHeaderInfo, mockAppSessionBean);
		assertTrue(headerCustRefs.get(0).isRequireCustRef());
		assertFalse(headerCustRefs.get(1).isRequireCustRef());
		assertFalse(headerCustRefs.get(2).isRequireCustRef());

		// shipping address is not null and not CML, headerCustRefs should not change if
		// on force ship to
		service.updateInfoSettingsForDeliverySelected(mockHeaderInfo, mockAppSessionBean);
		assertTrue(headerCustRefs.get(0).isRequireCustRef());
		assertFalse(headerCustRefs.get(1).isRequireCustRef());
		assertTrue(headerCustRefs.get(2).isRequireCustRef());
	}
	
	// CAP-42169
	@Test
	void that_validateOrderDetailMessage_isNotValid() throws AtWinXSException {
		OrderHeaderInfoResponse response = buildOrderHeaderInfoResponse();
		OrderDetailsHeaderInfo headerInfo = buildHeaderInfo();
		boolean isValid = service.validateOrderDetailMessage(headerInfo, response, mockAppSessionBean);
		assertFalse(isValid);
	}
	
	@Test
	void that_updateOrderDetailsMessages_setOrderMessageText() {
		GenericNameValuePair[] newRefValues = new GenericNameValuePair[]{new GenericNameValuePair("OHM1", "Message")};
		Collection<OrderMessageComposite> orderMessageComposite = buildOrderMessageComposites();
		service.updateOrderDetailsMessages(newRefValues, orderMessageComposite);
		assertNotNull(orderMessageComposite);
		
	}
	//CAP-43160
	@Test
	void testUpdateOrderHeaderInfo() {
		OrderDetailsHeaderInfo headerInfo = new OrderDetailsHeaderInfoImpl();
		Collection<OrderDetailsHeaderCustRef> headerCustRefs = new ArrayList<>();
		OrderInfoHeaderSaveRequest request = new OrderInfoHeaderSaveRequest();
		
		headerCustRefs.add(mockCR3);
		headerCustRefs.add(mockCR5);
		headerCustRefs.add(mockCR6);
		headerInfo.setContactName("John Doe");
		headerInfo.setContactEmail("john_doe@gmail.com");
		headerInfo.setContactPhone("008112");
		headerInfo.setAllowPONumberEdit(true);
		headerInfo.setPoNumber(AtWinXSConstant.EMPTY_STRING);
		headerInfo.setOrderTitleEnterable(true);
		headerInfo.setEnableOrderTitle(true);
		headerInfo.setOrderTitle(TEST_ORDERTITLE);
		headerInfo.setOrderDetailsMessages(buildOrderHeaderMessages());
		
		service.updateOrderHeaderInfo(request, headerInfo);
		
		assertNotNull(request);
	}
	@Test
	void testCanUpdateHeaderCustRefs() {
		GenericNameValuePair[] newRefValues = new GenericNameValuePair[]{new GenericNameValuePair("OHM1", "Message")};
		Collection<OrderDetailsHeaderCustRef> headerCustRefs = new ArrayList<>();
		headerCustRefs.add(mockCR3);
		headerCustRefs.add(mockCR5);
		headerCustRefs.add(mockCR6);
		boolean isValid = service.canUpdateHeaderCustRefs(newRefValues, headerCustRefs);
		assertTrue(isValid);
	}
	@Test
	void testCanUpdateHeaderCustRefs_invalid() {
		boolean isValid = service.canUpdateHeaderCustRefs(null, null);
		assertFalse(isValid);
	}
	@Test
	void testCanUpdateHeaderCustRef_invalid() {
		OrderDetailsHeaderCustRef ref = new OrderDetailsHeaderCustRefImpl();
		boolean isValid = service.canUpdateHeaderCustRef(ref);
		assertFalse(isValid);
	}
	@Test
	void testUpdateHeaderCustRef() {
		OrderDetailsHeaderCustRef ref = new OrderDetailsHeaderCustRefImpl();
		GenericNameValuePair[] newRefValues = new GenericNameValuePair[]{
				new GenericNameValuePair("refCode", "newMessage")};
		ref.setCustRefCode("refCode");
		ref.setSelectedCustRef("oldMessage");
		
		service.updateHeaderCustRef(ref, newRefValues);
		assertEquals("newMessage", ref.getSelectedCustRef());
		
	}
	@Test
	void testFixUneditableDropdownCustRefs() {
		Collection<OrderDetailsHeaderCustRef> headerCustRefs = new ArrayList<>();
		Collection<OrderReferenceFieldList> getCustomerRefList = new ArrayList<>();
		OrderReferenceFieldList ref1 = new OrderReferenceFieldList("Test", "Test", true);
		OrderReferenceFieldList ref2 = new OrderReferenceFieldList("Test", "Test", true);
		OrderDetailsHeaderCustRef ref = new OrderDetailsHeaderCustRefImpl();
		OrderDetailsCustRefSettings getCustRefSettings = new OrderDetailsCustRefSettingsImpl();
		headerCustRefs.add(mockCR3);
		headerCustRefs.add(mockCR5);
		headerCustRefs.add(mockCR6);
		getCustRefSettings.setDropdown(true);
		ref.setAllowCustRefEdit(false);
		getCustomerRefList.add(ref1);
		getCustomerRefList.add(ref2);
		ref.setCustomerRefList(getCustomerRefList);;
		ref.setCustRefSettings(getCustRefSettings);
		
		assertEquals(2, getCustomerRefList.size());
		assertFalse(ref.isAllowCustRefEdit());
		service.fixUneditableDropdownCustRefs(headerCustRefs);
	}
	
	// CAP-41551 //CAP-48437
	@Test
	void that_saveOrderHeaderInfo_returns_OrderInfoHeaderSaveResponse() throws Exception {
		service = Mockito.spy(service);
		setOrderShippingInfoRequest();
		
		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		OrderDueDateVO orderDueDateVO = new OrderDueDateVO(ORDER_ID, ORDER_ID, null, null, null, ORDER_ID, ORDER_ID, POBOX, null, PAST_DATE, NAME, INVALID_DATE, GENERIC_ERROR_MSG, FREIGHT_CODE, COMPANY_ID, BAD_WCSS_CHAR_VALUE, false, ADDRESS_LINE_2, ORDER_ID, null);
		when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(orderDueDateVO);
		saveRequest.setExpediteOrder(false);
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			when(mockOEProperty.getProperty(anyString())).thenReturn(COMPANY_ID);
			setUpSaveOrderHeaderInfo();
			when(mockUserSettings.isAllowRequestedShipDate()).thenReturn(true);
			setUpOrderDetailsShippingEntity();
			setUpOrderShippingEntity();
			setUpOrderShippingVO();
			OrderInfoHeaderSaveResponse result = service.saveOrderHeaderInfo(mockSessionContainer, saveRequest);
			Assertions.assertNotNull(result);
			
			saveRequest.setExpediteOrder(true);
			result = service.saveOrderHeaderInfo(mockSessionContainer, saveRequest);
			Assertions.assertNotNull(result);
			
			when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(null);
			result = service.saveOrderHeaderInfo(mockSessionContainer, saveRequest);
			Assertions.assertNotNull(result);

		}
	}
	
	@Test
	void that_validateOrderShippingInfo_invalidServiceLevelError() throws AtWinXSException {
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setCarrierServiceLevel(SERVICE_TYPE_CD + ";" + COMPANY_ID + ";" + FREIGHT_CODE);
		saveRequest.setThirdPartyAccount(THIRD_PARTY_ACCOUNT);
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpUserSettings();
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			setShipToAddress();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	@Test
	void that_validateOrderShippingInfo_invalidAccountNumberError() throws AtWinXSException {
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setCarrierServiceLevel(SERVICE_TYPE_CD + ";" + COMPANY_ID + ";" + FREIGHT_CODE);
		saveRequest.setThirdPartyAccount(THIRD_PARTY_ACCOUNT);
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpUserSettings();
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	@Test
	void that_validateOrderShippingInfo_accountNumberIsRequired() throws AtWinXSException {
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setCarrierServiceLevel(SERVICE_TYPE_CD + ";" + COMPANY_ID + ";" + FREIGHT_CODE);
		saveRequest.setThirdPartyAccount(AtWinXSConstant.EMPTY_STRING);
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpUserSettings();
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			thirdPartyAccountIsRequired();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	@Test
	void that_validateOrderShippingInfo_invalidCharError() throws AtWinXSException {
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setCarrierServiceLevel(SERVICE_TYPE_CD + ";" + COMPANY_ID + ";" + FREIGHT_CODE);
		saveRequest.setThirdPartyAccount(BAD_WCSS_CHAR_VALUE);
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpUserSettings();
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	@Test
	void that_validateOrderShippingInfo_invalidAccountNumberLengthUPS() throws AtWinXSException {
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setCarrierServiceLevel(SERVICE_TYPE_CD + ";" + UPSS + ";" + FREIGHT_CODE);
		saveRequest.setThirdPartyAccount(THIRD_PARTY_ACCOUNT);
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpUserSettings();
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	// CAP-46294
	@Test
	void that_validateOrderShippingInfo_NoOrderDueDate() throws AtWinXSException {
		
		//given that OrderDueDate Set to NO	
		//when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(false);
		when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(false);
		//when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setOrderDueDate("");
		saveRequest.setSpecialInstruction("ABCD");
		
		//when validateOrderShippingInfo is called, return no validation error with result true 
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertTrue(isValid);
		}
	}
	
	// CAP-46294
	@Test
	void that_validateOrderShippingInfo_EmptyOrderDueDate() throws AtWinXSException {
		
		//given that OrderDueDate Set to YES
		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(false);
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setOrderDueDate("");
		saveRequest.setSpecialInstruction("ABCD");
		
		//when validateOrderShippingInfo is called, return no validation error with result true 
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertTrue(isValid);
		}
	}
	
	// CAP-46294
	@Test
	void that_validateOrderShippingInfo_InvalidOrderDueDate1() throws AtWinXSException {
		
		//given that OrderDueDate Set to YES, but empty order due date
		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(true);
		// when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setOrderDueDate("");
		
		//when validateOrderShippingInfo is called, return no validation error with result true 
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	// CAP-46294
	@Test
	void that_validateOrderShippingInfo_InvalidOrderDueDate2() throws AtWinXSException {
		
		//given that OrderDueDate Set to YES, but Invalid order due date
		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(true);
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setOrderDueDate("01/122024");
			
		//when validateOrderShippingInfo is called, return no validation error with result true 
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	// CAP-46294
	@Test
	void that_validateOrderShippingInfo_InvalidOrderDueDate3() throws AtWinXSException {
		
		//given that OrderDueDate Set to YES, but Invalid order due date
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(true);
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setOrderDueDate("01/12/2024");
		
		//when validateOrderShippingInfo is called, return no validation error with result true 
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertFalse(isValid);
		}
	}
	
	// CAP-46294
	@Test
	void that_validateOrderShippingInfo_ValidOrderDueDate_return200() throws AtWinXSException {
		
		//given that OrderDueDate Set to YES, but Valid order due date
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(true);
		loadResponse = new OrderHeaderInfoResponse();
		saveRequest.setOrderDueDate("01/30/2030");
		saveRequest.setSpecialInstruction("ABCD");
		
		//when validateOrderShippingInfo is called, return 200 Success with no validation error
		try (MockedStatic<PropertyUtil> mockedPropertyUtil = mockStatic(PropertyUtil.class)) {
			mockedPropertyUtil.when(() -> PropertyUtil.getProperties(anyString())).thenReturn(mockOEProperty);
			setUpObjectMapFactory();
			setUpOrderShippingVO();
			boolean isValid = service.validateOrderShippingInfo(mockAppSessionBean, loadResponse, mockUserSettings, 
					ORDER_ID, saveRequest);
			assertTrue(isValid);
		}
	}
                                                              	
	private void setUpSaveOrderHeaderInfo() throws Exception, AtWinXSException {
		setUpModuleSession();
	    doReturn(true).when(service).validateOrder(any(OrderInfoHeaderSaveResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
	    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
	        any(VolatileSessionBean.class), any(), any());
	    doNothing().when(service).updateOrderHeaderInfo(any(OrderInfoHeaderSaveRequest.class), any(OrderDetailsHeaderInfo.class));
	    doReturn(true).when(service).validateOrderHeaderInfo(any(), any(), any(), any(), any(), any());
	    doReturn(true).when(service).validateOrderShippingInfo(any(), any(), any(), anyInt(), any());
	    doReturn(true).when(service).isRequestedShipDateValid(anyString(), any(), any(), anyBoolean());
	    when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	    when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
	    setUpUserSettings();
	    setUpObjectMapFactory();
	}

	private void setUpObjectMapFactory() throws AtWinXSException {
		when(mockObjectMapFactoryService.getComponentObjectMap()).thenReturn(mockComponentObjectMap);
	    when(mockComponentObjectMap.getComponent(eq(IOEManageOrdersComponent.class), any())).thenReturn(mockIOEManageOrdersComponent);
	    
	}
	
	private void setUpOrderDetailsShippingEntity() {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockOrderDetailsShipping);
	}
	
	private void setUpOrderShippingEntity() {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(OrderShipping.class), any())).thenReturn(mockOrderShipping);
	}
	
	private void setUpOrderShippingVO() throws AtWinXSException {
		when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(mockOrderShippingVo);
	}

	private void setShipToAddress() {
		when(mockOrderShippingVo.getShipToLineOneAd()).thenReturn(POBOX);
	    when(mockOrderShippingVo.getShipToLineTwoAd()).thenReturn(ADDRESS_LINE_2);
	    when(mockOrderShippingVo.getShipToAttentionTxt()).thenReturn(NAME);
	}

	private void setUpUserSettings() {
	    when(mockUserSettings.isAllowCarrierMethodSelection()).thenReturn(true);
	    when(mockUserSettings.isShowThirdPartyAccountNumber()).thenReturn(true);
	}
	
	private void thirdPartyAccountIsRequired() {
		when(mockUserSettings.isReqThirdPartyAccountNumber()).thenReturn(true);
	}
	
	// CAP-42169
	private OrderDetailsMessages buildOrderHeaderMessages() {
		OrderDetailsMessages orderDetailsMessages = new OrderDetailsMessagesImpl();
		Collection<OrderMessageComposite> orderMessageComposites = buildOrderMessageComposites();
		orderDetailsMessages.setOrderDetailsHeaderMessages(orderMessageComposites);
		return orderDetailsMessages;
	}

	private Collection<OrderMessageComposite> buildOrderMessageComposites() {
		Collection<OrderMessageComposite> orderMessageComposites = new ArrayList<>();
		OrderMessageComposite orderMessageCompositeShow = new OrderMessageComposite("Order Customer Message", "Sample Display Message", "2062");
		orderMessageCompositeShow.setDisplaySetting("S");
		orderMessageComposites.add(orderMessageCompositeShow);
		OrderMessageComposite orderMessageCompositeHidden = new OrderMessageComposite("Order Billing Message", "Sample Hidden Message", "2063");
		orderMessageCompositeHidden.setDisplaySetting("H");
		orderMessageComposites.add(orderMessageCompositeHidden);
		return orderMessageComposites;
	}
	
	private OrderHeaderInfoResponse buildOrderHeaderInfoResponse() {
		OrderHeaderInfoResponse response = new OrderHeaderInfoResponse();
		OrderDetailsHeaderInfoC1UX headerInfo = new OrderDetailsHeaderInfoC1UXImpl();
		headerInfo.setOrderDetailsMessages(buildOrderHeaderMessages());
		response.setOrderDetailsHeaderInfo(headerInfo);
		return response;
	}
	
	private OrderDetailsHeaderInfo buildHeaderInfo() {
		OrderDetailsHeaderInfo headerInfo = new OrderDetailsHeaderInfoImpl();
		headerInfo.setOrderDetailsMessages(buildOrderHeaderMessages());
		return headerInfo;		
	}
	
	private void setOrderShippingInfoRequest() {
		saveRequest.setCarrierServiceLevel(SERVICE_TYPE_CD + ";" + COMPANY_ID);
		saveRequest.setThirdPartyAccount(THIRD_PARTY_ACCOUNT);
		saveRequest.setRequestedShipDate(currentDateString());
	}
	 @Test
	  void that_loadOrderHeaderInfo_hasno_errmsg_onvalidate2() throws Exception {
	      when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
//	      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
//	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsHeaderInfo.class), any())).thenReturn(mockHeaderInfo);
//	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsCustRefs.class), any())).thenReturn(mockOrderDetailsCustRefs);

	      ArrayList<String> errMsgs = new ArrayList<>();
//	      doNothing().when(mockHeaderInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean, List.of());
//	      doNothing().when(mockOrderDetailsCustRefs).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean,
//	          mockHeaderInfo, List.of());

		  try (MockedConstruction<OrderDetailsValidator> mockedValidator =
				    		Mockito.mockConstruction(OrderDetailsValidator.class, (mock, context) -> {

				        Mockito.when(mock.validate(any(OrderDetailsFormBean.class), any(Message.class), any(OEOrderSessionBean.class),
				     			  any(AppSessionBean.class), anyBoolean(), any(VolatileSessionBean.class), anyBoolean()))
				        		.thenAnswer(new Answer<Boolean>() {
				            @Override
				            public Boolean answer(InvocationOnMock invocation) throws Throwable {
				            	Message msg = invocation.getArgument(1);
				              return true;
				            }
				       	  });}
					)) {


			    service = Mockito.spy(service);

		        setUpModuleSession();

			    doReturn(true).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
			    doReturn(true).when(service).validateFields(any(OrderDetailsHeaderInfo.class), any(BaseResponse.class), any(AppSessionBean.class), anyMap(), any(OEOrderSessionBean.class));
				doNothing().when(service).loadExtendedItemQuantity(any(),anyInt(), any(OrderHeaderInfoResponse.class), any(AppSessionBean.class), any(OEOrderSessionBean.class), any(VolatileSessionBean.class));								
				
			    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
			        any(VolatileSessionBean.class), any(), any());

			    loadRequest = new OrderHeaderInfoRequest();
			    // with review false, there will be no attempt to load item info
			    
				// CAP-43382
				when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
				when(mockEntityObjectMap.getEntity(eq(OrderDetailsBillingInfo.class), any())).thenReturn(mockBillingInfo);
				when(mockBillingInfo.isCreditCardRequired()).thenReturn(true);
				//CAP-42170
				when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
				when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
			    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
			   
				// CAP-49118
			    when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
			    when(mockIOEManageOrdersComponent.getOrderInfoShipping(any())).thenReturn(mockOrderInfoShippingVO);
			    //CAP-49216
			    when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
			    when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
			    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-50886
			    // CAP-48719
				doNothing().when(service).setEarliestDeliveryInfo(any(AppSessionBean.class),
						any(VolatileSessionBean.class), any(OrderHeaderInfoResponse.class));
				loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);
			    Assertions.assertNotNull(loadResponse);
			    assertTrue(loadResponse.isSuccess());
			    assertTrue(loadResponse.isValidAndComplete());
			    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

			    loadRequest.setReview(true);
			    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
			    errMsgs.clear();
			    // since review is true, it'll attempt to load item info now
			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);
			    
			    Assertions.assertNotNull(loadResponse);
			    assertTrue(loadResponse.isSuccess());
			    assertTrue(loadResponse.isValidAndComplete());
			    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

			    when(mockHeaderInfo.isFirstVisit()).thenReturn(true);
			    errMsgs.clear();
			    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());

			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);
			    // if you attempt to do review = true when firstVisit is true, we're going to specifically error now
			    Assertions.assertNotNull(loadResponse);
			    assertFalse(loadResponse.isSuccess());
			    assertFalse(loadResponse.isValidAndComplete());
			    assertFalse(Util.isBlankOrNull(loadResponse.getMessage()));

			    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
				        any(VolatileSessionBean.class), any(), any());
			    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
			    errMsgs.clear();

			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

			    Assertions.assertNotNull(loadResponse);
			    assertTrue(loadResponse.isSuccess());
			  }

	  }
	 
	 
	 @Test
	  void that_loadOrderHeaderInfo_hasno_errmsg_onvalidate3() throws Exception {
	      when(mockAppSessionBean.getCustomToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
//	      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
//	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsHeaderInfo.class), any())).thenReturn(mockHeaderInfo);
//	      when(mockEntityObjectMap.getEntity(eq(OrderDetailsCustRefs.class), any())).thenReturn(mockOrderDetailsCustRefs);

	      ArrayList<String> errMsgs = new ArrayList<>();
//	      doNothing().when(mockHeaderInfo).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean, List.of());
//	      doNothing().when(mockOrderDetailsCustRefs).populate(mockVolatileSessionBean, mockAppSessionBean, mockOEOrderSessionBean,
//	          mockHeaderInfo, List.of());

		  try (MockedConstruction<OrderDetailsValidator> mockedValidator =
				    		Mockito.mockConstruction(OrderDetailsValidator.class, (mock, context) -> {

				        Mockito.when(mock.validate(any(OrderDetailsFormBean.class), any(Message.class), any(OEOrderSessionBean.class),
				     			  any(AppSessionBean.class), anyBoolean(), any(VolatileSessionBean.class), anyBoolean()))
				        		.thenAnswer(new Answer<Boolean>() {
				            @Override
				            public Boolean answer(InvocationOnMock invocation) throws Throwable {
				            	Message msg = invocation.getArgument(1);
				              return true;
				            }
				       	  });}
					)) {


			    service = Mockito.spy(service);

		        setUpModuleSession();

			    doReturn(true).when(service).validateOrder(any(BaseResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
			    doReturn(true).when(service).validateFields(any(OrderDetailsHeaderInfo.class), any(BaseResponse.class), any(AppSessionBean.class), anyMap(), any(OEOrderSessionBean.class));
				doNothing().when(service).loadExtendedItemQuantity(any(),anyInt(), any(OrderHeaderInfoResponse.class), any(AppSessionBean.class), any(OEOrderSessionBean.class), any(VolatileSessionBean.class));

			    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
			        any(VolatileSessionBean.class), any(), any());

			    loadRequest = new OrderHeaderInfoRequest();
			    // with review false, there will be no attempt to load item info
			    
				// CAP-43382
				when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
				when(mockEntityObjectMap.getEntity(eq(OrderDetailsBillingInfo.class), any())).thenReturn(mockBillingInfo);
				when(mockBillingInfo.isCreditCardOptional()).thenReturn(true);
				//CAP-42170
				when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
				when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
			    doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings); 
			    
			    // CAP-48719
				doNothing().when(service).setEarliestDeliveryInfo(any(AppSessionBean.class),
						any(VolatileSessionBean.class), any(OrderHeaderInfoResponse.class));
				
				// CAP-49118
				when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
			    when(mockIOEManageOrdersComponent.getOrderInfoShipping(any())).thenReturn(mockOrderInfoShippingVO);	
			   // CAP-49216
			    when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
			    when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
			    when(mockEntityObjectMap.getEntity(eq(Order.class), any())).thenReturn(mockOrder);//CAP-50886
			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

			    Assertions.assertNotNull(loadResponse);
			    assertTrue(loadResponse.isSuccess());
			    assertTrue(loadResponse.isValidAndComplete());
			    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

			    loadRequest.setReview(true);
			    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
			    errMsgs.clear();
			    // since review is true, it'll attempt to load item info now
			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

			    Assertions.assertNotNull(loadResponse);
			    assertTrue(loadResponse.isSuccess());
			    assertTrue(loadResponse.isValidAndComplete());
			    assertTrue(Util.isBlankOrNull(loadResponse.getMessage()));

			    when(mockHeaderInfo.isFirstVisit()).thenReturn(true);
			    errMsgs.clear();
			    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString());

			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);
			    // if you attempt to do review = true when firstVisit is true, we're going to specifically error now
			    Assertions.assertNotNull(loadResponse);
			    assertFalse(loadResponse.isSuccess());
			    assertFalse(loadResponse.isValidAndComplete());
			    assertFalse(Util.isBlankOrNull(loadResponse.getMessage()));

			    doReturn(mockHeaderInfo).when(service).loadHeaderInfo(any(AppSessionBean.class), any(OEOrderSessionBean.class),
				        any(VolatileSessionBean.class), any(), any());
			    when(mockHeaderInfo.isFirstVisit()).thenReturn(false);
			    errMsgs.clear();

			    loadResponse = service.getOrderHeaderInfo(mockSessionContainer, loadRequest);

			    Assertions.assertNotNull(loadResponse);
			    assertTrue(loadResponse.isSuccess());
			  }

	 }
	 @Test
	 void testIsShowDefaultSignatureRequired() {
		 
		 OrderDetailsHeaderCustRef ref = new OrderDetailsHeaderCustRefImpl();
		 ref.setCustRefCode("refCode");
		 Collection<OrderDetailsHeaderCustRef> headerCustRefs = new ArrayList<>();
		 List<OrderDetailsHeaderCustRef> custRefs = new ArrayList<>();
		 ref.setCustRefCode("refCode");
		 headerCustRefs.add(mockCR3);
		 headerCustRefs.add(mockCR5);
		 headerCustRefs.add(mockCR6);
		 when(mockUserSettings.isSignatureRequiredOnCarrierChange()).thenReturn(true);
		 when(mockUserSettings.getDefaultSignatureRequired()).thenReturn("refCode");
		 custRefs.add(ref);
		 when(mockHeaderInfo.getOrderDetailsHeaderCustRefs()).thenReturn(custRefs);

		 boolean isValid = service.isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		 assertTrue(isValid); 
	 }
	 @Test
	 void testLoadShippingInfo() {
		 when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		 when(mockEntityObjectMap.getEntity(eq(OrderDetailsShippingInfo.class), any())).thenReturn(mockShippingInfo);
		 
		 service.loadShippingInfo(mockAppSessionBean, mockOEOrderSessionBean, mockVolatileSessionBean, null, TEST_ORDERTITLE, null, mockHeaderInfo);
		 assertNotNull(mockShippingInfo);
	 }
	 @Test
	 void testProcessOrderDetailsShippingInfo() throws IllegalAccessException, InvocationTargetException, AtWinXSException {
		 service = Mockito.spy(service);
		 loadResponse = new OrderHeaderInfoResponse();
	
		 OrderDetailsShippingInfoC1UX orderDetailsShippingInfoC1UX = new OrderDetailsShippingInfoC1UXImpl();
		 String[] shipMethodChangeInfo = new String[3];
		 shipMethodChangeInfo[0] = "Test1";
		 shipMethodChangeInfo[1] = "Test2";
		 shipMethodChangeInfo[2] = "Test3";
		 
		 when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		 doReturn(true).when(service).isShowDefaultSignatureRequired(mockHeaderInfo, mockUserSettings);
		 when(mockUserSettings.isShowShipMethodChangeMsgInd()).thenReturn(true);
		 when(mockAppSessionBean.getSiteID()).thenReturn(12321);
		 when(mockAppSessionBean.getBuID()).thenReturn(4556);
		 when(mockAppSessionBean.getGroupName()).thenReturn("anyString");
		 when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken); 
		 when(mockoaOrderAdminService.locate(any(CustomizationToken.class))).thenReturn(mockIOrderAdmin);
		 when(mockIOrderAdmin.getShipMethodChangeMsgInfo(any())).thenReturn(shipMethodChangeInfo);
		 
		 orderDetailsShippingInfoC1UX.setCarrierServiceLevel("Test");
		 loadResponse.setOrderDetailsShippingInfo(orderDetailsShippingInfoC1UX);
		 
		 service.processOrderDetailsShippingInfo(mockShippingInfo, loadResponse, mockAppSessionBean, mockOEOrderSessionBean, mockHeaderInfo);
		 
		 assertNotNull(loadResponse);
	 }
	 
	 //CAP-46294
	 @Test
	 void processOrderDetailsShippingInfo_OrderDueDate_() throws IllegalAccessException, InvocationTargetException, AtWinXSException {
		 service = Mockito.spy(service);
		 loadResponse = new OrderHeaderInfoResponse();
	
		 //given that OrderDueDate Set to NO	
		 when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		 when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(false);
		 when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(false);
		 when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		 
		 //when processOrderDetailsShippingInfo is called, OrderDueDateRequired=FALSE and showShipNowLater=FALSE
		 service.processOrderDetailsShippingInfo(mockShippingInfo, loadResponse, mockAppSessionBean, mockOEOrderSessionBean, mockHeaderInfo);
		 assertNotNull(loadResponse);
		 assertNotNull(loadResponse.getOrderDetailsShippingInfo());
		 assertFalse(loadResponse.getOrderDetailsShippingInfo().getisOrderDueDateRequired());
		 assertFalse(loadResponse.getOrderDetailsShippingInfo().isShowOrderDueDate());
		 assertFalse(loadResponse.getOrderDetailsShippingInfo().isShowShipNowLater());
	 }
	 
	 //CAP-46294
	 @Test
	 void processOrderDetailsShippingInfo_OrderDueDate_SetToYESOPT() throws IllegalAccessException, InvocationTargetException, AtWinXSException {
		 service = Mockito.spy(service);
		 loadResponse = new OrderHeaderInfoResponse();
	
		 //given that OrderDueDate Set to YES OPTIONAL
		 when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		 when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		 when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(false);
		 when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		 
		 //when processOrderDetailsShippingInfo is called, OrderDueDateRequired=FALSE and showShipNowLater=FALSE
		 service.processOrderDetailsShippingInfo(mockShippingInfo, loadResponse, mockAppSessionBean, mockOEOrderSessionBean, mockHeaderInfo);
		 assertNotNull(loadResponse);
		 assertNotNull(loadResponse.getOrderDetailsShippingInfo());
		 assertFalse(loadResponse.getOrderDetailsShippingInfo().getisOrderDueDateRequired());
		 assertTrue(loadResponse.getOrderDetailsShippingInfo().isShowOrderDueDate());
		 assertFalse(loadResponse.getOrderDetailsShippingInfo().isShowShipNowLater());
	 }
	 
	 //CAP-46294
	 @Test
	 void processOrderDetailsShippingInfo_OrderDueDate_SetToYesREQ() throws IllegalAccessException, InvocationTargetException, AtWinXSException {
		 service = Mockito.spy(service);
		 loadResponse = new OrderHeaderInfoResponse();
		 
		 //given that OrderDueDate Set to Yes REQUIRED
		 when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		 when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		 when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(true);
		 when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		 
		 //when processOrderDetailsShippingInfo is called, OrderDueDateRequired=TRUE and showShipNowLater=FALSE
		 service.processOrderDetailsShippingInfo(mockShippingInfo, loadResponse, mockAppSessionBean, mockOEOrderSessionBean, mockHeaderInfo);
		 assertNotNull(loadResponse);
		 assertNotNull(loadResponse.getOrderDetailsShippingInfo());
		 assertTrue(loadResponse.getOrderDetailsShippingInfo().getisOrderDueDateRequired());
		 assertTrue(loadResponse.getOrderDetailsShippingInfo().isShowOrderDueDate());
		 assertFalse(loadResponse.getOrderDetailsShippingInfo().isShowShipNowLater());
	 }
	 
	 //CAP-46294
	 @Test
	 void processOrderDetailsShippingInfo_OrderDueDate_SetToYesSHIPNOWLATER() throws IllegalAccessException, InvocationTargetException, AtWinXSException {
		 service = Mockito.spy(service);
		 loadResponse = new OrderHeaderInfoResponse();
	
		 //given that OrderDueDate Set to Yes SHIPNOW or LATER
		 when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		 when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		 when(mockUserSettings.isEnableOrderDueDateRequiredInd()).thenReturn(false);
		 when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(true);
		 
		 //when processOrderDetailsShippingInfo is called, OrderDueDateRequired=TRUE and showShipNowLater=TRUE
		 service.processOrderDetailsShippingInfo(mockShippingInfo, loadResponse, mockAppSessionBean, mockOEOrderSessionBean, mockHeaderInfo);
		 
		 assertNotNull(loadResponse);
		 assertNotNull(loadResponse.getOrderDetailsShippingInfo());
		 assertTrue(loadResponse.getOrderDetailsShippingInfo().getisOrderDueDateRequired());
		 assertTrue(loadResponse.getOrderDetailsShippingInfo().isShowOrderDueDate());
		 assertTrue(loadResponse.getOrderDetailsShippingInfo().isShowShipNowLater());
	 }
	 
	 @Test
	 void that_isRequestedShipDateValid_isValidDate() throws AtWinXSException {
		 when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		 boolean isValid = service.isRequestedShipDateValid(currentDateString(), loadResponse, mockAppSessionBean, true);
		 assertTrue(isValid);
	 }
	 
	 @Test
	 void that_isRequestedShipDateValid_isInvalidDate() throws AtWinXSException {
		 mockTranslationMethods();
		 boolean isValid = service.isRequestedShipDateValid(INVALID_DATE, loadResponse, mockAppSessionBean, true);
		 assertFalse(isValid);
	 }

	 @Test
	 void that_isRequestedShipDateValid_isPastDate() throws AtWinXSException {
		 mockTranslationMethods();
		 boolean isValid = service.isRequestedShipDateValid(PAST_DATE, loadResponse, mockAppSessionBean, true);
		 assertFalse(isValid);
	 }
	 
	 @Test
	 void that_isRequestedShipDateValid_isNotAllowRequestedShipDate_nullDate() throws AtWinXSException {
		 boolean isValid = service.isRequestedShipDateValid(null, loadResponse, mockAppSessionBean, false);
		 assertTrue(isValid);
	 }
	 
	 @Test
	 void that_isRequestedShipDateValid_isNotAllowRequestedShipDate_validDate() throws AtWinXSException {
		 boolean isValid = service.isRequestedShipDateValid(currentDateString(), loadResponse, mockAppSessionBean, false);
		 assertTrue(isValid);
	 }
	 
	 @Test
	 void that_isRequestedShipDateValid_nullDate() throws AtWinXSException {
		 boolean isValid = service.isRequestedShipDateValid(null, loadResponse, mockAppSessionBean, true);
		 assertTrue(isValid);
	 }
	 
	 private void mockTranslationMethods() {
		 when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		 when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
	 }
	 
	 private String currentDateString() {
		 Date currentDate = new Date();
		 SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		 return dateFormat.format(currentDate);
	 }
	 @ParameterizedTest
	 @MethodSource("processDTDCall")
	 void that_processDTDServiceCallTest(boolean isEnableShipNowLater, String orderDueDate) {
		 
		 when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
		 
		 when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(isEnableShipNowLater);
		 service.processDTDServiceCall(mockOEOrderSessionBean, loadResponse);
		 assertTrue(loadResponse.isCallDtdService());
		 }
	 
	 private static Stream<Arguments> processDTDCall(){
		return Stream.of(
				Arguments.of(true, "Y"),
				Arguments.of(true, "N"),
				Arguments.of(false, "N"),
				Arguments.of(false, "Y")
				);
		 
	 }

	// CAP-48457
	@Test
	void that_doDateToDestination_has_noAccessToService_validateOrderDueDate_isEmpty() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		when(mockUserSettings.getValidateOrderDueDate()).thenReturn(AtWinXSConstant.EMPTY_STRING);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.doDateToDestination(mockSessionContainer, null);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	// CAP-48457
	@Test
	void that_doDateToDestination_has_noAccessToService_validateOrderDueDate_isNo() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		when(mockUserSettings.getValidateOrderDueDate()).thenReturn(OrderEntryConstants.DUE_DATE_NO);

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.doDateToDestination(mockSessionContainer, null);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	// CAP-48457
	@Test
	void that_doDateToDestination_has_noValidOrder() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		when(mockUserSettings.getValidateOrderDueDate()).thenReturn(OrderEntryConstants.DUE_DATE_VALIDATE_CAN_EXPEDITE);
		doReturn(false).when(service).validateOrder(any(DateToDestinationResponse.class), any(SessionContainer.class), any(AppSessionBean.class));

		DateToDestinationResponse response = service.doDateToDestination(mockSessionContainer, null);

		assertNotNull(response);
		assertFalse(response.isSuccess());
	}

	// CAP-48457
	@Test
	void that_doDateToDestination_return_success() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(true);
		doReturn(true).when(service).validateOrder(any(DateToDestinationResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DueDateValidator.class), any())).thenReturn(mockDueDateValidator);
		doNothing().when(mockDueDateValidator).validate(anyInt(), any(), any(), any(), eq(null), eq(null), anyBoolean(), any(), anyBoolean(), anyBoolean());
		doNothing().when(service).processDateToDestinationValidation(any(), any(), anyBoolean(), eq(null), any(), any());

		DateToDestinationResponse response = service.doDateToDestination(mockSessionContainer, new DateToDestinationRequest());

		assertNotNull(response);
		assertTrue(response.isSuccess());
	}

	// CAP-48457
	@Test
	void that_processDateToDestinationValidation_return_success() throws Exception {

		service = Mockito.spy(service);
		DateToDestinationResponse response = new DateToDestinationResponse();

		when(mockDueDateValidator.getMessageType()).thenReturn(DueDateValidator.MessageType.Success);
		when(mockDueDateValidator.getValidationMessage()).thenReturn(TEST_MESSAGE);
		when(mockDueDateValidator.getLastReturnReasonCode()).thenReturn(AtWinXSConstant.INVALID_ID);
		doNothing().when(service).processOrderDueDateNotMet(any(), eq(response), anyBoolean(), any(), any(), eq(mockDueDateValidator), anyBoolean());

		service.processDateToDestinationValidation(mockSessionContainer, response, false, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator);

		assertNotNull(response);
		assertTrue(response.isDateToDestionationAvailable());
		assertEquals(ModelConstants.DTD_STATUS_SUCCESS, response.getStatusCode());
		assertEquals(TEST_MESSAGE, response.getInformMessage());
	}

	// CAP-48457
	@Test
	void that_processDateToDestinationValidation_return_error() throws Exception {

		service = Mockito.spy(service);
		DateToDestinationResponse response = new DateToDestinationResponse();

		when(mockDueDateValidator.getMessageType()).thenReturn(DueDateValidator.MessageType.Error);
		when(mockDueDateValidator.getValidationMessage()).thenReturn(TEST_MESSAGE);
		when(mockDueDateValidator.getLastReturnReasonCode()).thenReturn(316);
		doNothing().when(service).processOrderDueDateNotMet(any(), eq(response), anyBoolean(), any(), any(), eq(mockDueDateValidator), anyBoolean());

		service.processDateToDestinationValidation(mockSessionContainer, response, false, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator);

		assertNotNull(response);
		assertTrue(response.isDateToDestionationAvailable());
		assertEquals(ModelConstants.DTD_STATUS_ERROR, response.getStatusCode());
		assertEquals(TEST_MESSAGE, response.getMessage());
	}

	// CAP-48457
	@Test
	void that_processDateToDestinationValidation_return_warning() throws Exception {

		service = Mockito.spy(service);
		DateToDestinationResponse response = new DateToDestinationResponse();

		when(mockDueDateValidator.getMessageType()).thenReturn(DueDateValidator.MessageType.Warning);
		when(mockDueDateValidator.getValidationMessage()).thenReturn(TEST_MESSAGE);
		when(mockDueDateValidator.getLastReturnReasonCode()).thenReturn(0);
		doNothing().when(service).processOrderDueDateNotMet(any(), eq(response), anyBoolean(), any(), any(), eq(mockDueDateValidator), anyBoolean());

		service.processDateToDestinationValidation(mockSessionContainer, response, false, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator);

		assertNotNull(response);
		assertTrue(response.isDateToDestionationAvailable());
		assertEquals(ModelConstants.DTD_STATUS_WARNING, response.getStatusCode());
		assertEquals(TEST_MESSAGE, response.getWarningMessage());
	}

	// CAP-48457
	@Test
	void that_processDateToDestinationValidation_return_noMessage() throws Exception {

		service = Mockito.spy(service);
		DateToDestinationResponse response = new DateToDestinationResponse();

		when(mockDueDateValidator.getMessageType()).thenReturn(DueDateValidator.MessageType.NoMessage);
		when(mockDueDateValidator.getLastReturnReasonCode()).thenReturn(0);
		doNothing().when(service).processOrderDueDateNotMet(any(), eq(response), anyBoolean(), any(), any(), eq(mockDueDateValidator), anyBoolean());

		service.processDateToDestinationValidation(mockSessionContainer, response, false, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator);

		assertNotNull(response);
		assertTrue(response.isDateToDestionationAvailable());
		assertEquals(ModelConstants.DTD_STATUS_NO_MESSAGE, response.getStatusCode());
	}

	// CAP-48457
	@Test
	void that_processOrderDueDateNotMet_has_orderDueDateMet() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);

		DateToDestinationResponse response = new DateToDestinationResponse();

		service.processOrderDueDateNotMet(mockSessionContainer, response, false, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator, true);

		assertNotNull(response);
		assertFalse(response.isShipNowOrLater());
		assertFalse(response.isPromptExpedite());
	}

	// CAP-48457
	@Test
	void that_processOrderDueDateNotMet_has_orderDueDateNotMet_and_expediteNotAllowed() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(true);
		when(mockUserSettings.isAllowCarrierMethodSelection()).thenReturn(true);
		when(mockUserSettings.getValidateOrderDueDate()).thenReturn(OrderEntryConstants.DUE_DATE_INFORM_ONLY);
		doReturn(DATE_PATTERN).when(service).getDateFormatForLocale(mockAppSessionBean);
		doReturn(PAST_DATE).when(service).getFormattedDate(any(), eq(mockAppSessionBean));
		when(mockDueDateValidator.getDueDateNotMetMsg()).thenReturn(TEST_MESSAGE);

		DateToDestinationResponse response = new DateToDestinationResponse();

		service.processOrderDueDateNotMet(mockSessionContainer, response, false, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator, false);

		assertNotNull(response);
		assertTrue(response.isShipNowOrLater());
		assertTrue(response.isPromptExpedite());
		assertFalse(Util.isBlankOrNull(response.getExpediteMessage()));
	}

	// CAP-48457
	@Test
	void that_processOrderDueDateNotMet_has_orderDueDateNotMet_and_expediteAllowed() throws Exception {

		service = Mockito.spy(service);
		setUpModuleSession();

		DateToDestinationResponse response = new DateToDestinationResponse();

		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isEnableOrderDueDateShipNowLater()).thenReturn(false);
		when(mockUserSettings.isAllowCarrierMethodSelection()).thenReturn(false);
		when(mockUserSettings.getValidateOrderDueDate()).thenReturn(OrderEntryConstants.DUE_DATE_INFORM_CAN_EXPEDITE);
		doReturn(PAST_DATE).when(service).convertDateStringToLocaleFormat(any(), eq(mockAppSessionBean), eq(DATE_PATTERN));
		doReturn(PAST_DATE).when(service).getFormattedDate(any(), eq(mockAppSessionBean));
		when(mockDueDateValidator.getOrderDueDateVO()).thenReturn(mockOrderDueDateVO);
		when(mockDueDateValidator.getDueDateNotMetMsg()).thenReturn(TEST_MESSAGE);
		doReturn(TEST_MESSAGE).when(service).getTranslation(eq(mockAppSessionBean), any(), any());
		doReturn(mockReplaceMap).when(service).setupReplaceMap(any(), any());
		setUpObjectMapFactory();
		when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(mockOrderShippingVo);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(ExpeditedOrderService.class), any())).thenReturn(mockExpeditedOrderService);
		doNothing().when(mockExpeditedOrderService).populate();

		service.processOrderDueDateNotMet(mockSessionContainer, response, true, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, mockDueDateValidator, false);

		assertNotNull(response);
		assertFalse(response.isShipNowOrLater());
		assertTrue(response.isPromptExpedite());
		assertFalse(Util.isBlankOrNull(response.getExpediteMessage()));
	}

	// CAP-48457
	@Test
	void that_processExpediteOrder_has_expeditedOrderServiceEnabled() throws Exception {

		service = Mockito.spy(service);

		DateToDestinationResponse response = new DateToDestinationResponse();

		setUpObjectMapFactory();
		when(mockIOEManageOrdersComponent.getOrderShipping(anyInt())).thenReturn(mockOrderShippingVo);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(ExpeditedOrderService.class), any())).thenReturn(mockExpeditedOrderService);
		doNothing().when(mockExpeditedOrderService).populate();
		when(mockExpeditedOrderService.getExpediteDate()).thenReturn(TEST_DATE);
		when(mockExpeditedOrderService.isSuccess()).thenReturn(true);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(false);
		doReturn(TEST_MESSAGE).when(service).getTranslation(eq(mockAppSessionBean), any(), any());
		doReturn(DATE_PATTERN).when(service).getDateFormatForLocale(mockAppSessionBean);

		service.processExpediteOrder(mockAppSessionBean, mockVolatileSessionBean, mockUserSettings, response, PAST_DATE, mockOrderDueDateVO, mockReplaceMap);

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

		assertNotNull(response);
		assertEquals(sdf.format(TEST_DATE), response.getExpediteDate());
		assertEquals(0, response.getExpediteServiceCharge());
	}

	// CAP-48457
	@Test
	void that_setExpediteData_has_showOrderLinePrice() throws Exception {

		service = Mockito.spy(service);
		DateToDestinationResponse response = new DateToDestinationResponse();

		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		doReturn(mockXSCurrency).when(service).getStringFromCurrency(mockAppSessionBean, TEST_CHARGE);
		doReturn(TEST_MESSAGE).when(service).getTranslation(eq(mockAppSessionBean), any(), any());

		service.setExpediteData(mockAppSessionBean, mockUserSettings, response, mockReplaceMap, TEST_CHARGE, PAST_DATE);

		assertNotNull(response);
		assertEquals(PAST_DATE, response.getExpediteDate());
		assertEquals(TEST_CHARGE, response.getExpediteServiceCharge());
	}

	// CAP-48457
	@Test
	void that_isShipDateReplacesOrderDueDate_returns_false() throws Exception {

		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockOEOrderSession.getOrderScenarioNumber()).thenReturn(AtWinXSConstant.INVALID_ID);

		boolean response = service.isShipDateReplacesOrderDueDate(mockOEOrderSession, mockUserSettings);

		assertFalse(response);
	}

	// CAP-48457
	@Test
	void that_isShipDateReplacesOrderDueDate_returns_true() throws Exception {

		when(mockUserSettings.isEnableOrderDueDateInd()).thenReturn(true);
		when(mockOEOrderSession.getOrderScenarioNumber()).thenReturn(OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY);

		boolean response = service.isShipDateReplacesOrderDueDate(mockOEOrderSession, mockUserSettings);

		assertTrue(response);
	}

	// CAP-48457
	@Test
	void that_isExpediteAllowed_returns_false() throws Exception {

		boolean response = service.isExpediteAllowed(mockOrderDueDateVO, false);

		assertFalse(response);
	}

	// CAP-48457
	@Test
	void that_getEarliestDueDateTranslationKey_returns_defaultMessage() throws Exception {

		String response = service.getEarliestDueDateTranslationKey(true, true);

		assertNotNull(response);
		assertEquals(OrderEntryConstants.EXPEDITE_ORDER_UNAVAIL_POPUP_MSG1, response);
	}

	// CAP-48457
	@Test
	void that_isExpediteEnabled_returns_true() throws Exception {

		when(mockUserSettings.getValidateOrderDueDate()).thenReturn(OrderEntryConstants.DUE_DATE_VALIDATE_CAN_EXPEDITE);

		boolean response = service.isExpediteEnabled(mockUserSettings);

		assertTrue(response);
	}

	// CAP-48457
	@Test
	void that_getFormattedDate_returns_default() throws Exception {

		String response = service.getFormattedDate(null, mockAppSessionBean);

		assertNotNull(response);
		assertEquals(AtWinXSConstant.EMPTY_STRING, response);
	}
	
	// CAP-48719
	@Test
	void that_setEarliestDeliveryInfo_ManageOrders_ISNULL() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));

		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);

		OrderHeaderInfoResponse respone = new OrderHeaderInfoResponse();
		service.setEarliestDeliveryInfo(mockAppSessionBean, mockVolatileSessionBean, respone);
		assertFalse(respone.isExpeditedOrder());
	}

	// CAP-48719
	@Test
	void that_setEarliestDeliveryInfo_ManageOrders_NotExpediate() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
		when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(false);

		try (MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

			OrderHeaderInfoResponse respone = new OrderHeaderInfoResponse();
			service.setEarliestDeliveryInfo(mockAppSessionBean, mockVolatileSessionBean, respone);
			assertFalse(respone.isExpeditedOrder());
		}
	}

	//CAP-48719
	@Test
	void that_setEarliestDeliveryInfo_ManageOrders_ExpediateWithEarliestDeliveryDt() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		when(mockIOEManageOrdersComponent.getOrderDueDate(anyInt())).thenReturn(mockOrderDueDateVO);
		when(mockOrderDueDateVO.isExpeditedOrder()).thenReturn(true);
		when(mockOrderDueDateVO.getEarliestDeliveryDt()).thenReturn(new Date());

		try (MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			mockUtil.when(() -> Util.getStringFromDate(any(Date.class), any(Locale.class))).thenReturn("08/24/2023");

			OrderHeaderInfoResponse respone = new OrderHeaderInfoResponse();
			service.setEarliestDeliveryInfo(mockAppSessionBean, mockVolatileSessionBean, respone);
			assertTrue(respone.isExpeditedOrder());
		}
	}
	//CAP-49122
	@ParameterizedTest
	@MethodSource("getSpecialInstructions")
	@Disabled("To be temporarily disabled")
	void that_hasValidSpecialInstructions_test_success(String specialInstruction, BaseResponse response, AppSessionBean appSessionBean) throws AtWinXSException {
		service.hasValidSpecialInstructions(specialInstruction, response, appSessionBean);
		assertNotNull(specialInstruction);
	}
	private static Stream<Arguments> getSpecialInstructions(){
		return Stream.of(
				Arguments.of("Valid Special Instruction"),
				Arguments.of("This is an invalid special instruction. This should not sabe saved on the database..........................."));
	}
	
	
	
	//CAP-49278
	@ParameterizedTest
	@MethodSource("getEFDEmailSourceLabelMethod")
	void that_getEFDEmailSourceLabel_test_success(String method, Locale locale) throws AtWinXSException {

		String label = service.getEFDEmailSourceLabel(method, locale, mockToken);
		assertNull(label);
	}

	private static Stream<Arguments> getEFDEmailSourceLabelMethod() {
		return Stream.of(Arguments.of("DIG", DEFAULT_US_LOCALE), 
				Arguments.of("PDF", DEFAULT_US_LOCALE),
				Arguments.of("FTP", DEFAULT_US_LOCALE), 
				Arguments.of("STC", DEFAULT_US_LOCALE),
				Arguments.of("EDC", DEFAULT_US_LOCALE), 
				Arguments.of("EXT", DEFAULT_US_LOCALE),
				Arguments.of("JEL", DEFAULT_US_LOCALE), 
				Arguments.of("EGC", DEFAULT_US_LOCALE));
	}
		
	@Test
	void that_getRequiredEmailAddress_expected_result() throws AtWinXSException {
		service = Mockito.spy(service);
		when(mockCMCatalogComponentLocatorService.locate(any())).thenReturn(mockICatalog);
		OEExtendedItemQuantityResponseBean respone = new OEExtendedItemQuantityResponseBean(new OrderLineImpl(),
				DEFAULT_US_LOCALE, true);
		respone.setVendorItemNumber("1234");
		respone.setItemNumber("123");
		Collection<EFDSourceSetting> efdSource = new ArrayList<>();
		when(mockEFDSourceSetting.getRequiredEmailAddresses()).thenReturn("Test@test.com");
		efdSource.add(mockEFDSourceSetting);
		when(mockICatalog.getCatalogDefaultWithEfdSettings(any(), any(), anyInt())).thenReturn(mockCatalogDefaultVO);
		when(mockCatalogDefaultVO.getEfdSourceSettings()).thenReturn(efdSource);
		service.getRequiredEmailAddress(mockAppSessionBean, respone);
		assertNotNull(respone);
	}
	
	
	
	@Test
	void that_getEfdEmailAddressesAndEfdDeliveryTypes_expected_result() throws AtWinXSException {

		try (

				MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = Mockito
						.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
							when(mock.getOrderSummary(any(), any(), anyBoolean()))
									.thenReturn(mockOEOrderSummaryResponseBean);
							when(mock.getEFDFormInfo(any(), any(), anyInt()))
									.thenReturn(mockEFDDestinationOptionsFormBean);
						});
				MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = Mockito
						.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
							when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
							when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
							when(mock.getSalesForceEmailSelected()).thenReturn("true");
						});) {
			setupBaseMockSessions();
			service = Mockito.spy(service);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockOEOrderSummaryResponseBean.getItems()).thenReturn(mockOEExtendedQuantityResponseBean);
			OEExtendedItemQuantityResponseBean[] oeExtendedItemQuantityResponseBean = new OEExtendedItemQuantityResponseBean[1];
			oeExtendedItemQuantityResponseBean[0] = mockOEExtendedItemQuantityResponseBean;
			when(mockOEExtendedQuantityResponseBean.getItems()).thenReturn(oeExtendedItemQuantityResponseBean);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(eq(EFDCRMTracking.class), any())).thenReturn(mockEFDCRMTracking);

			OEExtendedItemQuantityResponseBean respone = new OEExtendedItemQuantityResponseBean(new OrderLineImpl(),
					DEFAULT_US_LOCALE, true);
			respone.setVendorItemNumber("1234");
			respone.setItemNumber("123");
			service.getEfdEmailAddressesAndEfdDeliveryTypes(mockSessionContainer, respone);
			assertNotNull(respone);
		}
	}
}