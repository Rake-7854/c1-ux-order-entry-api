/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	07/11/23	Satishkumar A      CAP-41970		C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 */
package com.rrd.c1ux.api.services.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.util.CountriesAndStatesResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.locale.ao.CountryBean;
import com.wallace.atwinxs.locale.ao.StateBean;

@WithMockUser
class CountriesAndStatesServiceImplTests extends BaseServiceTest {

	private CountriesAndStatesResponse response;
	
	private String COUNTRY_AFG = "AFGHANISTAN";
	private String COUNTRY_CODE_AFG = "AFG";
	private String COUNTRY_CODE_SHORT_AFG = "AF";
	
	private String COUNTRY_AUS = "AUSTRALIA";
	private String COUNTRY_CODE_AUS = "AUS";
	private String COUNTRY_CODE_SHORT_AUS = "AC";
	private String COUNTRY_AUS_STATE_AC = "AUSTRALIAN CAPITAL TERRITORY";
	
	private String REQUEST_FAILED = "Request Failed";
	private String PROPERTY_REQUEST_FAILED = "sf.requestFailed";
	
	@InjectMocks
	CountriesAndStatesServiceImpl service;
	
	@Mock
	CountryBean cBean;
	@Mock
	StateBean sBean;
	
	
	@BeforeEach
	void setUp() throws Exception {

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
	}

	@Test
	void that_getCountriesAndStatesOrProvincesList_countriesWithoutStates_returnsSuccess() throws AtWinXSException {

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_AFG);
		
		when(cBean.getCountryCode()).thenReturn(COUNTRY_CODE_AFG);
		when(cBean.getCountryCodeShort()).thenReturn(COUNTRY_CODE_SHORT_AFG);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_AFG)).thenReturn(cBean);
			
			response = service.getCountriesAndStatesOrProvincesList(mockSessionContainer);
			
			Assertions.assertNotNull(response);
			Assertions.assertEquals(COUNTRY_CODE_AFG, response.getCountriesAndStates().get(0).getName());
			Assertions.assertEquals(COUNTRY_CODE_SHORT_AFG, response.getCountriesAndStates().get(0).getValue().getCountryCodeShort());
		}

	}

	@Test
	void that_getCountriesAndStatesOrProvincesList_countriesWithStates_returnsSuccess() throws AtWinXSException {

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_AUS);
		
		ArrayList<String> states = new ArrayList<String>();
		states.add(COUNTRY_CODE_SHORT_AUS);
		
		when(cBean.getCountryCode()).thenReturn(COUNTRY_CODE_AUS);
		when(cBean.getCountryCodeShort()).thenReturn(COUNTRY_CODE_SHORT_AUS);
		when(cBean.getCountryHasStates()).thenReturn(true);
		when(cBean.getStatesInCountryByName()).thenReturn(states.iterator());
		when(cBean.getStateInCountryByName(any())).thenReturn(sBean);
		
		when(sBean.getStateCode()).thenReturn(COUNTRY_CODE_SHORT_AUS);
		when(sBean.getStateCodeText()).thenReturn(COUNTRY_AUS_STATE_AC);
		
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class); MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_AUS)).thenReturn(cBean);

			response = service.getCountriesAndStatesOrProvincesList(mockSessionContainer);
			
			Assertions.assertNotNull(response);
			Assertions.assertEquals(COUNTRY_CODE_AUS, response.getCountriesAndStates().get(0).getName());
			Assertions.assertEquals(COUNTRY_CODE_SHORT_AUS, response.getCountriesAndStates().get(0).getValue().getCountryCodeShort());
			Assertions.assertTrue(response.getCountriesAndStates().get(0).getValue().isHasStates());
			Assertions.assertEquals(COUNTRY_AUS_STATE_AC,response.getCountriesAndStates().get(0).getValue().getStates().iterator().next().getName());
			Assertions.assertEquals(COUNTRY_CODE_SHORT_AUS,response.getCountriesAndStates().get(0).getValue().getStates().iterator().next().getValue());
		}

	}
	
	@Test
	void that_getCountriesAndStatesOrProvincesList_countriesWithoutStates_returns_422() throws AtWinXSException,Exception {

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class)) {
			
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(null);
			when(mockTranslationService.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), PROPERTY_REQUEST_FAILED)).thenReturn(REQUEST_FAILED);

	        response = service.getCountriesAndStatesOrProvincesList(mockSessionContainer);
			
			Assertions.assertNotNull(response);
			Assertions.assertFalse( response.isSuccess());
			Assertions.assertEquals(REQUEST_FAILED, response.getMessage());
			
		}
		

}
	
	@Test
	void that_getCountriesAndStatesOrProvincesList_countriesWithStates_returns_zip_422() throws AtWinXSException,Exception {
		
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_AUS);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class)) {
			
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountryByName(COUNTRY_AUS)).thenReturn(cBean);
			when(mockTranslationService.processMessage(ArgumentMatchers.any(), ArgumentMatchers.any(),
					ArgumentMatchers.any())).thenReturn(REQUEST_FAILED);
			when(cBean.getZipLabelTag()).thenReturn(COUNTRY_AUS);		
	        response = service.getCountriesAndStatesOrProvincesList(mockSessionContainer);
			
			Assertions.assertNotNull(response);
			Assertions.assertTrue( response.isSuccess());
			
		}

}
}
