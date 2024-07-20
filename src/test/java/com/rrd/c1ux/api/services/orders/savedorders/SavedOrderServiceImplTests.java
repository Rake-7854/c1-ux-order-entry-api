/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/20/23    A Boomker       CAP-41121                   Initial
 *  06/26/23	A Boomker		CAP-41640					Add handling for load saved order details
 *  06/27/23	Satishkumar A	CAP-41308					API Fix - Saved Order Expand - Item image for items without an image should return null
 */
package com.rrd.c1ux.api.services.orders.savedorders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeResponse;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEExtendedQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderDetailsResponseBean;
import com.wallace.atwinxs.orderentry.ao.OrderLineVOFilter;
import com.wallace.atwinxs.orderentry.dao.OrderLineDAO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;

class SavedOrderServiceImplTests extends BaseOEServiceTest {

	private static final String FAKE_VENDOR_ITEM1 = "FAKEWCSS1";
	private static final String FAKE_VENDOR_ITEM2 = "FAKEWCSS2";
	private static final String FAKE_DESC = "FAKE Description";
	private static final String FAKE_CUSTOMER_ITEM1 = "FAKECUST1";
	private static final String FAKE_CUSTOMER_ITEM2 = "FAKECUST2";
	private static final String FAKE_IMAGE = "FakeImage.png";
	private static final String FAKE_CT = "CT";
	private static final String FAKE_CARTON_COUNT = "500";


  @InjectMocks
  private SavedOrderServiceImpl service;

  @Mock
  private Map<String, String> mockTranslationMap;

  @Mock
  private Properties mockProperties;

  @Mock
  private OESavedOrderDetailsResponseBean mockOrderDetailBean;

  @Mock
  private OEExtendedQuantityResponseBean mockEQBean;
  @Mock
  private OEExtendedQuantityResponseBean mockEQEmptyBean;
  @Mock
  private OEExtendedItemQuantityResponseBean mockInvalidItem1;
  @Mock
  private OEExtendedItemQuantityResponseBean mockInvalidItem2;
  
  //CAP-41308
  @Mock
  private OrderLineVOFilter mockOrderLine;
  @Mock
  private OrderLineVO mockOrderLineVO;

  @Mock
  private OESavedOrderDetailsResponseBean mockDetailsBean;
  @Mock
  private OESavedOrderAssembler mockAssembler;

  @BeforeEach
  public void setup() {

  }

  @Test
  void that_buildErrorForInvalidItems_works() throws Exception {
	  when(mockInvalidItem1.getItemDescription()).thenReturn(FAKE_DESC);
	  when(mockInvalidItem2.getItemDescription()).thenReturn(FAKE_DESC);
	  SavedOrderResumeResponse response = new SavedOrderResumeResponse();
	  when(mockOrderDetailBean.getItemsToBeRemoved()).thenReturn(mockEQBean);
	  when(mockOrderDetailBean.getOrderName()).thenReturn("a name");
	  when(mockAppSessionBean.showWCSSItemNumber()).thenReturn(false);
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[2];
	  items[0] = mockInvalidItem1;
	  items[1] = mockInvalidItem2;
	  when(mockEQBean.getItems()).thenReturn(items);
	  service = Mockito.spy(service);
	  doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), anyString(),	anyString(), anyMap());
//	  when(service.getTranslation(eq(mockAppSessionBean), anyString(), anyString(), anyMap())).thenReturn(GENERIC_ERROR_MSG);

	  service.buildErrorForInvalidItems(mockOrderDetailBean, response, mockAppSessionBean);
	  Assertions.assertEquals(GENERIC_ERROR_MSG, response.getMessage());
  }

  @Test
  void that_buildErrorItemList_worksforMultipleItems() throws Exception {
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[2];
	  items[0] = mockInvalidItem1;
	  items[1] = mockInvalidItem2;
	  when(mockInvalidItem1.getItemDescription()).thenReturn(FAKE_DESC);
	  when(mockInvalidItem1.getItemNumber()).thenReturn(FAKE_CUSTOMER_ITEM1);
	  when(mockInvalidItem2.getItemNumber()).thenReturn(FAKE_CUSTOMER_ITEM2);
	  when(mockInvalidItem2.getItemDescription()).thenReturn(FAKE_DESC);

	  String message = service.buildErrorItemList(items, false);
	  Assertions.assertTrue(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertTrue(message.contains(FAKE_CUSTOMER_ITEM2));
	  Assertions.assertTrue(message.contains(FAKE_DESC));

	  message = service.buildErrorItemList(items, true);
	  Assertions.assertTrue(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertTrue(message.contains(FAKE_CUSTOMER_ITEM2));
	  Assertions.assertTrue(message.contains(FAKE_DESC));

	  when(mockInvalidItem1.getItemNumber()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	  when(mockInvalidItem2.getItemNumber()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	  when(mockInvalidItem1.getVendorItemNumber()).thenReturn(FAKE_VENDOR_ITEM1);
	  when(mockInvalidItem2.getVendorItemNumber()).thenReturn(FAKE_VENDOR_ITEM2);

	  message = service.buildErrorItemList(items, true);
	  Assertions.assertFalse(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertFalse(message.contains(FAKE_CUSTOMER_ITEM2));
	  Assertions.assertTrue(message.contains(FAKE_DESC));
	  Assertions.assertTrue(message.contains(AtWinXSConstant.COMMA));
	  Assertions.assertTrue(message.contains(FAKE_VENDOR_ITEM1));
	  Assertions.assertTrue(message.contains(FAKE_VENDOR_ITEM2));

	  message = service.buildErrorItemList(items, false);
	  Assertions.assertFalse(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertFalse(message.contains(FAKE_CUSTOMER_ITEM2));
	  Assertions.assertTrue(message.contains(FAKE_DESC));
	  Assertions.assertFalse(message.contains(FAKE_VENDOR_ITEM1));
	  Assertions.assertFalse(message.contains(FAKE_VENDOR_ITEM2));
	  Assertions.assertFalse(message.contains("("));
	  Assertions.assertFalse(message.contains(")"));

  }

  @Test
  void that_buildErrorItemList_worksforSingleItem() throws Exception {
	  when(mockInvalidItem1.getItemDescription()).thenReturn(FAKE_DESC);

	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[1];
	  items[0] = mockInvalidItem1;
	  when(mockInvalidItem1.getItemNumber()).thenReturn(FAKE_CUSTOMER_ITEM1);
	  when(mockInvalidItem1.getVendorItemNumber()).thenReturn(FAKE_VENDOR_ITEM1);
	  String message = service.buildErrorItemList(items, false);
	  Assertions.assertTrue(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertFalse(message.contains(AtWinXSConstant.COMMA));
	  Assertions.assertTrue(message.contains(FAKE_DESC));
	  Assertions.assertFalse(message.contains(FAKE_VENDOR_ITEM1));

	  message = service.buildErrorItemList(items, true);
	  Assertions.assertTrue(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertFalse(message.contains(AtWinXSConstant.COMMA));
	  Assertions.assertTrue(message.contains(FAKE_DESC));
	  Assertions.assertFalse(message.contains(FAKE_VENDOR_ITEM1));

	  when(mockInvalidItem1.getItemNumber()).thenReturn(AtWinXSConstant.EMPTY_STRING);
	  message = service.buildErrorItemList(items, false);
	  Assertions.assertFalse(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertFalse(message.contains(AtWinXSConstant.COMMA));
	  Assertions.assertTrue(message.contains(FAKE_DESC));
	  Assertions.assertFalse(message.contains(FAKE_VENDOR_ITEM1));

	  message = service.buildErrorItemList(items, true);
	  Assertions.assertFalse(message.contains(FAKE_CUSTOMER_ITEM1));
	  Assertions.assertFalse(message.contains(AtWinXSConstant.COMMA));
	  Assertions.assertTrue(message.contains(FAKE_DESC));
	  Assertions.assertTrue(message.contains(FAKE_VENDOR_ITEM1));

  }


  @Test
  void that_loadSavedOrderDetails_worksForExpand() throws AtWinXSException
  {
	  when(mockOrderDetailBean.getItems()).thenReturn(mockEQBean);
	  when(mockOrderDetailBean.getOrderName()).thenReturn("a name");
	  when(mockAppSessionBean.getDefaultTimeZone()).thenReturn("CDT");
	  when(mockAppSessionBean.getProfileNumber()).thenReturn(26);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[2];
	  items[0] = mockInvalidItem1;
	  items[1] = mockInvalidItem2;
	  when(mockEQBean.getItems()).thenReturn(items);
	  service = Mockito.spy(service);
	  doReturn(mockAssembler).when(service).getAssembler(mockAppSessionBean, mockVolatileSessionBean);
	  when(mockAssembler.getSavedOrderDetail(anyInt(), anyString(), eq(mockOEOrderSession), eq(mockAppSessionBean), anyString(), anyInt()))
	  		.thenReturn(mockOrderDetailBean);

	  when(mockInvalidItem1.getItemImageURL()).thenReturn(ModelConstants.CP_NO_IMAGE_NO_CONTEXT);

	  OESavedOrderDetailsResponseBean bean = service.loadSavedOrderDetails(25, mockAppSessionBean, mockOESession, mockVolatileSessionBean, false);
	  Assertions.assertNotNull(bean);
	  Assertions.assertNotNull(bean.getItems());
	  Assertions.assertNotNull(bean.getItems().getItems());
	  Assertions.assertNull(bean.getItemsToBeRemoved());

	  doNothing().when(service).checkForBadLines(anyInt(), any(), any(), any(), any());
	  bean = service.loadSavedOrderDetails(25, mockAppSessionBean, 	mockOESession, mockVolatileSessionBean, true);
	  Assertions.assertNotNull(bean);
	  Assertions.assertNotNull(bean.getItems());
	  Assertions.assertNotNull(bean.getItems().getItems());
	  Assertions.assertNull(bean.getItemsToBeRemoved());

	  SavedOrderExpansionResponse response = new SavedOrderExpansionResponse();

	  service.getSavedOrderExpansionDetails(response, 25, mockAppSessionBean, mockOESession, mockVolatileSessionBean);
	  Assertions.assertNotNull(response.getExpansion());
	  Assertions.assertNotNull(response.getExpansion().getItems());
	  Assertions.assertNotNull(response.getExpansion().getItems().getItems());
	  Assertions.assertNull(response.getExpansion().getItemsToBeRemoved());

  }

  @Test
  void that_loadSavedOrderDetails_worksForResumeSuccess() throws AtWinXSException
  {
		try (MockedConstruction<OrderLineDAO> mockedDAO =
	    		Mockito.mockConstruction(OrderLineDAO.class, (mock, context) -> {
	    	        doReturn(new OrderLineVO[0]).when(mock).getNonComponentOrderLines(any());
	    		})) {

	  when(mockOrderDetailBean.getItems()).thenReturn(mockEQBean);
	  when(mockOrderDetailBean.getItemsToBeRemoved()).thenReturn(mockEQEmptyBean);
	  when(mockOrderDetailBean.getOrderName()).thenReturn("a name");
	  when(mockAppSessionBean.getDefaultTimeZone()).thenReturn("CDT");
	  when(mockAppSessionBean.getProfileNumber()).thenReturn(26);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[2];
	  items[0] = mockInvalidItem1;
	  items[1] = mockInvalidItem2;
	  when(mockEQBean.getItems()).thenReturn(items);
	  when(mockEQEmptyBean.getItems()).thenReturn(null);

	  service = Mockito.spy(service);
	  doReturn(mockAssembler).when(service).getAssembler(mockAppSessionBean, mockVolatileSessionBean);
	  when(mockAssembler.getSavedOrderDetail(anyInt(), anyString(), eq(mockOEOrderSession), eq(mockAppSessionBean), anyString(), anyInt()))
	  		.thenReturn(mockOrderDetailBean);

	   OESavedOrderDetailsResponseBean bean = service.loadSavedOrderDetails(25, mockAppSessionBean,
			   mockOESession, mockVolatileSessionBean, true);
	   Assertions.assertNotNull(bean);
		  Assertions.assertNotNull(bean.getItems());
		  Assertions.assertNotNull(bean.getItems().getItems());
		  Assertions.assertNotNull(bean.getItemsToBeRemoved());
		  Assertions.assertNull(bean.getItemsToBeRemoved().getItems());

		  doNothing().when(service).checkForBadLines(anyInt(), any(), any(), any(), any());

	  SavedOrderResumeResponse response = new SavedOrderResumeResponse();
	  when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
	  when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
	  when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(100));
	  when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);

	  bean = service.validateResumeOrder(response, 25, mockAppSessionBean, mockOESession, mockVolatileSessionBean, false, mockSessionContainer);
	  Assertions.assertEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());
	   Assertions.assertNotNull(bean);
		  Assertions.assertNotNull(bean.getItems());
		  Assertions.assertNotNull(bean.getItems().getItems());
		  Assertions.assertNotNull(bean.getItemsToBeRemoved());
		  Assertions.assertNull(bean.getItemsToBeRemoved().getItems());

		}
  }

  @Test
  void that_loadSavedOrderDetails_worksForResumeFailure() throws AtWinXSException
  {
	  when(mockOrderDetailBean.getItems()).thenReturn(mockEQBean);
	  when(mockOrderDetailBean.getOrderName()).thenReturn("a name");
	  when(mockAppSessionBean.getDefaultTimeZone()).thenReturn("CDT");
	  when(mockAppSessionBean.getProfileNumber()).thenReturn(26);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[2];
	  items[0] = mockInvalidItem1;
	  items[1] = mockInvalidItem2;
	  when(mockEQBean.getItems()).thenReturn(items);
	  service = Mockito.spy(service);
	  doReturn(mockAssembler).when(service).getAssembler(mockAppSessionBean, mockVolatileSessionBean);
	  when(mockAssembler.getSavedOrderDetail(anyInt(), anyString(), eq(mockOEOrderSession), eq(mockAppSessionBean), anyString(), anyInt()))
	  		.thenReturn(mockOrderDetailBean);

	   doAnswer(invocation -> {
			  when(mockOrderDetailBean.getItemsToBeRemoved()).thenReturn(mockEQBean);
	    	return null;
	    }).when(service).checkForBadLines(anyInt(), any(), any(), any(), any());
	   OESavedOrderDetailsResponseBean bean = service.loadSavedOrderDetails(25, mockAppSessionBean,
			   mockOESession, mockVolatileSessionBean, true);
	   Assertions.assertNotNull(bean);
		  Assertions.assertNotNull(bean.getItems());
		  Assertions.assertNotNull(bean.getItems().getItems());
		  Assertions.assertNotNull(bean.getItemsToBeRemoved());

		  doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), anyString(),	anyString(), anyMap());

	  SavedOrderResumeResponse response = new SavedOrderResumeResponse();
	  when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
	  when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
	  when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(100));
	  when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(0);

	  Assertions.assertNull(service.validateResumeOrder(response, 25, mockAppSessionBean, mockOESession, mockVolatileSessionBean, false, mockSessionContainer));
	  Assertions.assertFalse(response.isSuccess());
	  Assertions.assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());

	  when(mockVolatileSessionBean.getShoppingCartCount()).thenReturn(2);

	  Assertions.assertNull(service.validateResumeOrder(response, 25, mockAppSessionBean, mockOESession, mockVolatileSessionBean, false, mockSessionContainer));
	  Assertions.assertFalse(response.isSuccess());
	  Assertions.assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());


  }
  
  //CAP-41308
  @Test
  void that_updateImagesAndUOMsForC1UX_itemsNotNull() throws Exception {


	  when(mockOrderLine.getVO()).thenReturn(mockOrderLineVO);
	  
	  SavedOrderExpansionResponse response = new SavedOrderExpansionResponse();
	  OESavedOrderDetailsResponseBean orderDetailBean = new OESavedOrderDetailsResponseBean(null, null, null, mockEQBean, false, false);
	  response.setExpansion(orderDetailBean);
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[5];
	  
	  OEExtendedItemQuantityResponseBean oeExtItemQtyCpNoImg = new OEExtendedItemQuantityResponseBean(mockOrderLine);
	  OEExtendedItemQuantityResponseBean oeExtItemQtyCpNoImgNoCtxt = new OEExtendedItemQuantityResponseBean(mockOrderLine);
	  OEExtendedItemQuantityResponseBean oeExtItemQtyNull = new OEExtendedItemQuantityResponseBean(mockOrderLine);
	  OEExtendedItemQuantityResponseBean oeExtItemQtyValidImg = new OEExtendedItemQuantityResponseBean(mockOrderLine);
	  
	  oeExtItemQtyCpNoImg.setItemImageURL(ModelConstants.CP_NO_IMAGE);
	  items[0] = oeExtItemQtyCpNoImg;
	  
	  oeExtItemQtyCpNoImgNoCtxt.setItemImageURL(ModelConstants.CP_NO_IMAGE_NO_CONTEXT);
	  items[1] = oeExtItemQtyCpNoImgNoCtxt;
	  
	  oeExtItemQtyNull.setItemImageURL(null);
	  items[2] = oeExtItemQtyNull;
	  
	  oeExtItemQtyValidImg.setItemImageURL(FAKE_IMAGE);
	  items[3] = oeExtItemQtyValidImg;
	  
	  items[4] = mockInvalidItem1;
	 
	  when(mockEQBean.getItems()).thenReturn(items);
	  when(mockInvalidItem1.getUOMCode()).thenReturn(FAKE_CT);
	  when(mockInvalidItem1.getUOMFactor()).thenReturn(FAKE_CARTON_COUNT);
	  
	  
	  try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)	){

		mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(), "ofLbl")).thenReturn(" of ");
		mockItemUtility.when(() -> ItemUtility.getUOMAcronyms(mockInvalidItem1.getUOMCode(), false, mockAppSessionBean)).thenReturn("Carton");
		
		service.updateImagesAndUOMsForC1UX(response, mockAppSessionBean);
	  

		Assertions.assertNull(response.getExpansion().getItems().getItems()[0].getItemImageURL());
		Assertions.assertNull(response.getExpansion().getItems().getItems()[1].getItemImageURL());
		Assertions.assertNull(response.getExpansion().getItems().getItems()[2].getItemImageURL());
		Assertions.assertNotNull(response.getExpansion().getItems().getItems()[3].getItemImageURL());
		Assertions.assertEquals(FAKE_IMAGE, response.getExpansion().getItems().getItems()[3].getItemImageURL());

		Assertions.assertEquals(FAKE_CT, response.getExpansion().getItems().getItems()[4].getUOMCode());	
		Assertions.assertEquals(FAKE_CARTON_COUNT, response.getExpansion().getItems().getItems()[4].getUOMFactor());
		}
  }

  //CAP-41308
  @Test
  void that_updateImagesAndUOMsForC1UX_itemsSizeZero() throws Exception {

	  SavedOrderExpansionResponse response = new SavedOrderExpansionResponse();
	  OESavedOrderDetailsResponseBean orderDetailBean = new OESavedOrderDetailsResponseBean(null, null, null, mockEQBean, false, false);
	  response.setExpansion(orderDetailBean);
	  OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[0];

	  when(mockEQBean.getItems()).thenReturn(items);

	  service.updateImagesAndUOMsForC1UX(response, mockAppSessionBean);
	  
	  Assertions.assertFalse(response.getExpansion().getItems().getItems().length>0);
  }
  
  //CAP-41308
  @Test
  void that_updateImagesAndUOMsForC1UX_itemsNull() throws Exception {

	  SavedOrderExpansionResponse response = new SavedOrderExpansionResponse();
	  OESavedOrderDetailsResponseBean orderDetailBean = new OESavedOrderDetailsResponseBean(null, null, null, mockEQBean, false, false);
	  response.setExpansion(orderDetailBean);
	  OEExtendedItemQuantityResponseBean[] items = null;

	  when(mockEQBean.getItems()).thenReturn(items);
	  
	  service.updateImagesAndUOMsForC1UX(response, mockAppSessionBean);
	  
	  Assertions.assertNull(response.getExpansion().getItems().getItems());



}
}
