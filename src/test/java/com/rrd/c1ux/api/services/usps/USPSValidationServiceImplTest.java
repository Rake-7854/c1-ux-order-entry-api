/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     	DTS#        	Description
 *  --------    -----------     	------------ 	--------------------------------
 *	12/22/23	S Ramachandran		CAP-46081		Initial Version. Added tests for USPS validation Service methods
 */

package com.rrd.c1ux.api.services.usps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.util.DeliveryOptionsUtil;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.locale.ao.CountryBean;
import com.wallace.atwinxs.locale.ao.StateBean;
import com.wallace.atwinxs.locale.vo.CountryVOKey;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;
import com.wallace.atwinxs.orderentry.vo.USPSAddressInfoServiceParmsVO;

class USPSValidationServiceImplTest extends BaseOEServiceTest {

	@InjectMocks
	private USPSValidationServiceImpl service;
	  
	@Mock
	private USPSValidationService mockUSPSValidationService;
	
	@Mock
    private Map<String, String> mockTranslationMap;

    @Mock
    private Properties mockProperties;

    @Mock
    private com.wallace.atwinxs.locale.ao.CountryBean mockUsaBean;

    @Mock
    private OECheckoutAssembler mockCheckoutAssembler;

    @Mock
    private OENewAddressFormBean mockAddressFormBean;

    @Mock
    private OrderShippingVO mockOrderShipping;
    
    @Mock
    private SelfAdminService mockSelfAdminService;
    
    @Mock
  	private CODeliveryInformationSaveRequest mockCODeliveryInformationSaveRequest;
  	
    @Mock
  	private OrderAddressVO mockOrderAddressVO;
    
  	
  	@Mock
  	CountryBean cBean;
  	
  	@Mock
  	StateBean sBean;
  	
  	private USPSValidationRequest uspsValidationRequest;
    private USPSValidationResponse uspsValidationResponse;
    
    
    @BeforeEach
    public void setup() {
    	
      uspsValidationRequest = new USPSValidationRequest();
    }
    
    @Test
  	void that_validateUSPS_validation_Failed1() throws Exception {
  		
    	// when validateUSPS is called, expected 422
  		service = Mockito.spy(service);
  		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
  		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
  		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  		
  		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		// when No Order available 
		doReturn(false).when(service).validateOrder(any(USPSValidationResponse.class), any(SessionContainer.class), 
				any(AppSessionBean.class));
		uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		Assertions.assertNotNull(uspsValidationResponse);
		Assertions.assertFalse(uspsValidationResponse.isSuccess());
	}
    
    
    //CAP-38135
  	@Test
  	void that_validateUSPS_validation_Failed2() throws Exception {
  		
  		// when validateUSPS is called, expected 422
  		service = Mockito.spy(service);
  		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
  		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
  		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  		
  		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		doReturn(true).when(service).validateOrder(any(USPSValidationResponse.class), any(SessionContainer.class), 
				any(AppSessionBean.class));
		
		
		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);
		
		
		try(	

			MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.NOT_VALID_ERR, null))
					.thenReturn(SFTranslationTextConstants.NOT_VALID_ERR);
			
			// when USPSValidationRequest=> attributes are Empty
	  		uspsValidationRequest.setCountry("");
	  		uspsValidationRequest.setAddress1("");
	  		uspsValidationRequest.setAddress2("");
	  		uspsValidationRequest.setCity("");
	  		uspsValidationRequest.setState("");
	  		uspsValidationRequest.setZip("");
			uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
			Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
			
			
			// when USPSValidationRequest=> State attributes is Empty for USA
			uspsValidationRequest.setCountry("USA");
	  		uspsValidationRequest.setAddress1("");
	  		uspsValidationRequest.setAddress2("");
	  		uspsValidationRequest.setCity("");
	  		uspsValidationRequest.setState("");
	  		uspsValidationRequest.setZip("");
			uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
			Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
			
			// when USPSValidationRequest=> attributes exceeds field size
			uspsValidationRequest.setCountry("USAUSA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108 Apt 1830");
			uspsValidationRequest.setAddress2("55 Sylvan Lake Blvd RM 108 Apt 1830");
			uspsValidationRequest.setCity("BayvilleBayville");
			uspsValidationRequest.setState("NJNJ");
			uspsValidationRequest.setZip("0872108721");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
			
		}
  	}
  	
	@Test
  	void that_validateUSPS_validation_Failed3() throws Exception {
  		
		// when validateUSPS is called, expected 422
  		service = Mockito.spy(service);
  		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
  		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
  		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  		
  		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		doReturn(true).when(service).validateOrder(any(USPSValidationResponse.class), any(SessionContainer.class), 
				any(AppSessionBean.class));
		
		
		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);
		
		USPSAddressInfoServiceParmsVO validation = getUSPSAddressAfterValidation();

		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Address Not Found'
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {

			validation.setErrorMessage("Invalid Address");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("Invalid Address");
			uspsValidationRequest.setAddress2("Apt 1830 &><%");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08721");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}

		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Address Not Found'
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("Address Not Found");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("Address Not Found");
			uspsValidationRequest.setAddress2("Apt 1830 &><%");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08721");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
		
		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Address Not Found'
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("Multiple addresses were found");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("Sylvan Lake");
			uspsValidationRequest.setAddress2("");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08721");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
		
		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Default address: The address you'
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("Default address: The address you entered was found but more information is needed (such as an apartment, suite, or box number) to match to a specific address.");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("Sylvan Lake");
			uspsValidationRequest.setAddress2("");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08721");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
  	}
	
	@Test
  	void that_validateUSPS_validation_Failed4() throws Exception {
  		
		// when validateUSPS is called, expected 422
  		service = Mockito.spy(service);
  		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
  		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
  		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  		
  		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		doReturn(true).when(service).validateOrder(any(USPSValidationResponse.class), any(SessionContainer.class), 
				any(AppSessionBean.class));
		
		
		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);
		
		USPSAddressInfoServiceParmsVO validation = getUSPSAddressAfterValidation();

		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Invalid City'
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("Invalid City");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830 &><%");
			uspsValidationRequest.setCity("Invalid City");
			uspsValidationRequest.setState("AL");
			uspsValidationRequest.setZip("08723");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
		
		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Invalid State Code'
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("Invalid State Code");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830 &><%");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("AL");
			uspsValidationRequest.setZip("08723");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
		
		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Invalid Zip Code'
		try(MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			validation.setErrorMessage("Invalid Zip Code");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08723");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
		
		// when USPS validation returns USPSAddressInfoServiceParmsVO with Error Message 'Invalid Zip Code'
		try(MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
				) {
			validation.setErrorMessage("Invalid Zip Code");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("0872-3");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
		
		// when uspsValidationRequest with Zip Code Characters less than 4 for USA
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			validation.setErrorMessage("");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("0872");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
		}
  	}
	
	@Test
  	void that_validateUSPS_validation_Failed5() throws Exception {
  		
		// when validateUSPS is called, expected 422, ShowSuggestedAddress=TRUE
  		service = Mockito.spy(service);
  		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
  		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
  		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  		
  		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		doReturn(true).when(service).validateOrder(any(USPSValidationResponse.class), any(SessionContainer.class), 
				any(AppSessionBean.class));
		
		
		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);
		
		// when USPSAddressInfoServiceParmsVO with Address1 and Address2 length Less than 30
		USPSAddressInfoServiceParmsVO validation = getUSPSValidationFailedWithSuggestedAddrlengthGRTHAN30();
		// when uspsValidationRequest with City, State, Zip Not Match with Suggested
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830");
			uspsValidationRequest.setCity("Punta Gorda");
			uspsValidationRequest.setState("FL");
			uspsValidationRequest.setZip("33980");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
			Assertions.assertTrue(uspsValidationResponse.isShowSuggestedAddress());
		}
		
		// when USPSAddressInfoServiceParmsVO with Address1 and Address2 length Less than 30
		validation = getUSPSValidationFailedWithSuggestedAddrlengthLSTHAN30();
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830");
			uspsValidationRequest.setCity("Punta Gorda");
			uspsValidationRequest.setState("FL");
			uspsValidationRequest.setZip("33980");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
			Assertions.assertTrue(uspsValidationResponse.isShowSuggestedAddress());
		}
	 	
		// when uspsValidationRequest with Zip Length greater than 5 and Not Matched to USPSAddressInfoServiceParmsVO 
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			
			validation.setErrorMessage("");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830");
			uspsValidationRequest.setCity("BAYVILLE");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08721-12345");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertFalse(uspsValidationResponse.isSuccess());
			Assertions.assertTrue(uspsValidationResponse.isShowSuggestedAddress());
		}
  	}
	
	@Test
  	void that_validateUSPS_Success() throws Exception {
  		
		// when validateUSPS is called, expected 200
  		service = Mockito.spy(service);
  		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
  		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
  		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
  		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  		
  		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		
		doReturn(true).when(service).validateOrder(any(USPSValidationResponse.class), any(SessionContainer.class), 
				any(AppSessionBean.class));
		
		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);
		
		USPSAddressInfoServiceParmsVO validation = getUSPSAddressAfterValidation();
		
		// when validateUSPS validation success for Country USA
		try(	
			MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
			) {
			validation.setErrorMessage("");
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelperUSPS(any(), any(), any(), any()))
				.thenReturn(validation);
			uspsValidationRequest.setCountry("USA");
			uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
			uspsValidationRequest.setAddress2("Apt 1830");
			uspsValidationRequest.setCity("Bayville");
			uspsValidationRequest.setState("NJ");
			uspsValidationRequest.setZip("08721");
		    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
		    Assertions.assertNotNull(uspsValidationResponse);
			Assertions.assertTrue(uspsValidationResponse.isSuccess());
			Assertions.assertFalse(uspsValidationResponse.isShowSuggestedAddress());
		}
	
		// when validateUSPS validation success for Not USA Country
		validation.setErrorMessage("");
		uspsValidationRequest.setCountry("CAN");
		uspsValidationRequest.setAddress1("55 Sylvan Lake Blvd RM 108");
		uspsValidationRequest.setAddress2("Apt 1830");
		uspsValidationRequest.setCity("Bayville");
		uspsValidationRequest.setState("AB");
		uspsValidationRequest.setZip("08721");
	    uspsValidationResponse = service.validateUSPS(mockSessionContainer,uspsValidationRequest);
	    Assertions.assertNotNull(uspsValidationResponse);
		Assertions.assertTrue(uspsValidationResponse.isSuccess());
		Assertions.assertFalse(uspsValidationResponse.isShowSuggestedAddress());
		
  	}
	
	public USPSAddressInfoServiceParmsVO getUSPSAddressAfterValidation() {
		USPSAddressInfoServiceParmsVO addressParms = 
				new USPSAddressInfoServiceParmsVO("55 SYLVAN LAKE BLVD RM 108 APT 1830","","BAYVILLE","NJ","08721-1234","");
		return(addressParms);
	}
	
	//CAP-45374
	private USPSAddressInfoServiceParmsVO getUSPSValidationFailedWithSuggestedAddrlengthGRTHAN30() {
		USPSAddressInfoServiceParmsVO addressParms = 
				new USPSAddressInfoServiceParmsVO("55 SYLVAN LAKE BLVD RM 108 APT 1830","","BAYVILLE","NJ","08721-1234","");
		return addressParms;
	}
	
	//CAP-45374
	private USPSAddressInfoServiceParmsVO getUSPSValidationFailedWithSuggestedAddrlengthLSTHAN30() {
		USPSAddressInfoServiceParmsVO addressParms = 
				new USPSAddressInfoServiceParmsVO("55 SYLVAN LAKE BLVD","","BAYVILLE","NJ","08721-1234","");
		return addressParms;
	}
  	
}
