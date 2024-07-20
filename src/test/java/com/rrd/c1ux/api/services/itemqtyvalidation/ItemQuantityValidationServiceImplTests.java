/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/25/2023  N Caceres		CAP-39046	Initial version
 *  05/31/23    C Porter        CAP-40530   JUnit cleanup
 *  06/27/23	N Caceres		CAP-41120	Validate uomDesc and orderQty
 *  03/05/24	A Boomker		CAP-47679	Junits for refactoring
 */
package com.rrd.c1ux.api.services.itemqtyvalidation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationRequest;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationResponse;
import com.rrd.c1ux.api.util.ItemQtyValidation;
import com.rrd.c1ux.api.util.ItemUtility;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

class ItemQuantityValidationServiceImplTests extends BaseServiceTest {

    public static final String ONE = "1";
    public static final String NEGATIVE_ONE = "-1";
    public static final String INVALID_QUANTITY_ERROR_MESSAGE = "Please enter a valid quantity.";
    public static final String INVALID_UOM_ERROR_MESSAGE = "Please select a unit of measure.";
    public static final String SUCCESS = "Success";
    public static final String FAIL = "Failed";
    public static final String MAX_VALUE = "2147483648";
    public static final String ITEM_NUMBER = "10116020";
    public static final String UOM_DESCRIPTION = "EA_1";
    public static final String INVALID_UOM_DESCRIPTION = "EA_of_1";
    public static final String VENDOR_ITEM_NUMBER = "JL10116020";
    public static final String INVALID_QUANTITY = "A1";

    @InjectMocks
    private ItemQuantityValidationServiceImpl serviceToTest;

    @Mock
    private ItemQtyValidation mockItemQtyValidation;

    @Mock
    private ItemRptVO mockItemRptVO;

    ItemQtyValidationResponse validationResponse;
    ItemQtyValidationRequest validationRequest;

    @BeforeEach
    void setup() {
        validationRequest = buildItemQtyValidationRequest();
        validationResponse = new ItemQtyValidationResponse();
        when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
        when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

    }

    @Test
    void that_getQtyValidation_returns_successfully() throws AtWinXSException {
    	serviceToTest = Mockito.spy(serviceToTest);

    	try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.getUOMAcronyms(anyString(), anyBoolean(), any()))
                    .thenReturn(AtWinXSConstant.EMPTY_STRING);
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
     		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
            when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
            when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
    		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
            when(mockItemsInterfaceLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockItemsInterface);
            validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
            assertEquals(SUCCESS, validationResponse.getStatus());
        }
    }

    @Test
    void that_getQtyValidation_has_negative_quantity() throws AtWinXSException {
        setNegativeQuantity(validationRequest);
        try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
            when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
            when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
    		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
            when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
            when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
    		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
	        when(mockItemsInterfaceLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockItemsInterface);
	        when(mockTranslationService.processMessage(any(), any(),  any())).thenReturn(INVALID_QUANTITY_ERROR_MESSAGE);
	        validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
	        assertEquals(FAIL, validationResponse.getStatus());
        }
    }

    @Test
    void that_getQtyValidation_uomDescription_is_blank() throws AtWinXSException {
        setBlankUomDescription(validationRequest);
        validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
        assertEquals(SUCCESS, validationResponse.getStatus());
    }

    @Test
    void that_getQtyValidation_has_max_value_quantity() throws AtWinXSException {
    	serviceToTest = Mockito.spy(serviceToTest);
        try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.getUOMAcronyms(anyString(), anyBoolean(), any()))
                    .thenReturn(AtWinXSConstant.EMPTY_STRING);
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
    		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
            when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
            when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
    		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockItemsInterfaceLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockItemsInterface);
			when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(INVALID_QUANTITY_ERROR_MESSAGE);
            setQuantityMoreThanMaxValue(validationRequest);
            validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
            assertEquals(FAIL, validationResponse.getStatus());
        }
    }

    @Test
    void that_getQtyValidation_has_invalid_UOM_and_blank_quantity() throws AtWinXSException {
    	setQuantityToBlank(validationRequest);
    	setInvalidUOM(validationRequest);
    	try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
    		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
	        when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
	        when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
			when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockItemsInterfaceLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockItemsInterface);
	    	when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(INVALID_QUANTITY_ERROR_MESSAGE);
	    	validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
	        assertEquals(FAIL, validationResponse.getStatus());
	        assertEquals(INVALID_QUANTITY_ERROR_MESSAGE, validationResponse.getStatusMessage());
    	}
    }

    @Test
    void that_getQtyValidation_has_invalid_UOM() throws AtWinXSException {
    	setInvalidUOM(validationRequest);
        try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
    		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
            when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
            when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
    		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockItemsInterfaceLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockItemsInterface);
			when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(INVALID_UOM_ERROR_MESSAGE);
	    	validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
            assertEquals(FAIL, validationResponse.getStatus());
            assertEquals(INVALID_UOM_ERROR_MESSAGE, validationResponse.getStatusMessage());
        }
    }

    @Test
    void that_getQtyValidation_has_invalid_orderQuantity() throws AtWinXSException {
    	setInvalidQuantity(validationRequest);
    	try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
    		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
            when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
            when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
            when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
    		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
			when(mockItemsInterfaceLocatorService.locate(isA(CustomizationToken.class))).thenReturn(mockItemsInterface);
	    	when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(INVALID_QUANTITY_ERROR_MESSAGE);
	    	validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
	        assertEquals(FAIL, validationResponse.getStatus());
	        assertEquals(INVALID_QUANTITY_ERROR_MESSAGE, validationResponse.getStatusMessage());
    	}
    }

    @Test
    void that_getQtyValidation_uomDescription_and_quantity_are_blank() throws AtWinXSException {
        setBlankUomDescription(validationRequest);
        setQuantityToBlank(validationRequest);
        validationResponse = serviceToTest.getQtyValidation(mockSessionContainer, validationRequest);
        assertEquals(SUCCESS, validationResponse.getStatus());
    }

    private ItemQtyValidationRequest buildItemQtyValidationRequest() {
        ItemQtyValidationRequest request = new ItemQtyValidationRequest();
        request.setItemNumber(ITEM_NUMBER);
        request.setUomDesc(UOM_DESCRIPTION);
        request.setOrderQty(ONE);
        request.setVendorItemNumber(VENDOR_ITEM_NUMBER);
        return request;
    }

    private void setNegativeQuantity(ItemQtyValidationRequest validationRequest) {
        validationRequest.setOrderQty(NEGATIVE_ONE);
    }

    private void setBlankUomDescription(ItemQtyValidationRequest validationRequest) {
        validationRequest.setUomDesc(AtWinXSConstant.EMPTY_STRING);
    }
    private void setQuantityMoreThanMaxValue(ItemQtyValidationRequest validationRequest) {
        validationRequest.setOrderQty(MAX_VALUE);
    }

    private void setQuantityToBlank(ItemQtyValidationRequest validationRequest) {
    	validationRequest.setOrderQty(AtWinXSConstant.EMPTY_STRING);
    }

    private void setInvalidUOM(ItemQtyValidationRequest validationRequest) {
    	validationRequest.setUomDesc(INVALID_UOM_DESCRIPTION);
    }

    private void setInvalidQuantity(ItemQtyValidationRequest validationRequest) {
    	validationRequest.setOrderQty(INVALID_QUANTITY);
    }

    // CAP-47679 - adding tests to increase coverage
    @Test
    void validateOrderQuantityWithValidUOM() throws AtWinXSException {
    	serviceToTest = Mockito.spy(serviceToTest);
        try (MockedStatic<ItemUtility> mockedItemUtility = mockStatic(ItemUtility.class)) {
            mockedItemUtility.when(() -> ItemUtility.getUOMAcronyms(anyString(), anyBoolean(), any()))
                    .thenReturn(AtWinXSConstant.EMPTY_STRING);
            mockedItemUtility.when(() -> ItemUtility.setItemUOMOptions(any(), any(),
            		anyString(), anyBoolean(), any(), anyString(), any(), any(), any(), anyString())).thenAnswer((Answer<Void>) invocation -> null);
    		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
    		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
			when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(INVALID_QUANTITY_ERROR_MESSAGE);
            setQuantityMoreThanMaxValue(validationRequest);
            doReturn(mockItemRptVO).when(serviceToTest).getItemDetailWithQuantity(any(), any());
            when(mockItemRptVO.getMaxinumOrderQty()).thenReturn(2500.0);
            String errMsg = serviceToTest.validateOrderQuantityWithValidUOM(mockSessionContainer, validationRequest);
            assertTrue(errMsg.startsWith(INVALID_QUANTITY_ERROR_MESSAGE));

            validationRequest.setOrderQty("-25");
            errMsg = serviceToTest.validateOrderQuantityWithValidUOM(mockSessionContainer, validationRequest);
            assertTrue(errMsg.startsWith(INVALID_QUANTITY_ERROR_MESSAGE));

            validationRequest.setOrderQty("100");
            validationRequest.setUomDesc("CT_25");
            assertNull(serviceToTest.validateOrderQuantityWithValidUOM(mockSessionContainer, validationRequest));
        }
    }
}
