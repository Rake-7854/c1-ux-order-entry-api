
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         	DTS#            Description
 *	--------    -----------        	----------      -----------------------------------------------------------
 *  08/09/23 	Satishkumar A		CAP-42720		C1UX API - API Build - Favorite Toggle Call
 */
package com.rrd.c1ux.api.services.favorite;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.favorite.ToggleFavoriteResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;

@WithMockUser
class ToggleFavoriteServiceImplTests extends BaseServiceTest {

	public static final String FAKE_VENDOR_ITEM_NUMBER = "123456";
	public static final String FAKE_CUSTOMER_ITEM_NUMBER = "456789";
	public static final String FAKE_EMPTY_STRING = "";

	@InjectMocks
	private ToggleFavoriteServiceImpl serviceToTest;

	@BeforeEach
	public void setup() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);

	}

	@Test
	void that_toggleFavorite_success() throws Exception {

		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockOESessionBean.isAllowUserFavorites()).thenReturn(true);

		ToggleFavoriteResponse response = new ToggleFavoriteResponse();

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.toggleFavorite(mockSessionContainer, FAKE_CUSTOMER_ITEM_NUMBER,
					FAKE_VENDOR_ITEM_NUMBER);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}
	}

	@Test
	void that_toggleFavorite_AllowUserFavorites_false() throws Exception {

		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockOESessionBean.isAllowUserFavorites()).thenReturn(false);

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			 serviceToTest.toggleFavorite(mockSessionContainer, FAKE_CUSTOMER_ITEM_NUMBER,
					FAKE_VENDOR_ITEM_NUMBER);
			});

			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}

	@Test
	void that_toggleFavorite_itemNumber_vendorItemNumber_empty() throws Exception {

		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockOESessionBean.isAllowUserFavorites()).thenReturn(true);

		ToggleFavoriteResponse response = new ToggleFavoriteResponse();

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.toggleFavorite(mockSessionContainer, FAKE_EMPTY_STRING, FAKE_VENDOR_ITEM_NUMBER);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.toggleFavorite(mockSessionContainer, FAKE_CUSTOMER_ITEM_NUMBER, FAKE_EMPTY_STRING);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
		}

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			response = serviceToTest.toggleFavorite(mockSessionContainer, FAKE_EMPTY_STRING, FAKE_EMPTY_STRING);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(false, response.isSuccess());
		}

	}

	@Test
	void that_toggleFavorite_profileNumber_zero() throws Exception {

		when(mockAppSessionBean.getProfileNumber()).thenReturn(0);

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {
			serviceToTest = Mockito.spy(serviceToTest);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			 serviceToTest.toggleFavorite(mockSessionContainer, FAKE_CUSTOMER_ITEM_NUMBER,
					FAKE_VENDOR_ITEM_NUMBER);

			});

			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}

}
