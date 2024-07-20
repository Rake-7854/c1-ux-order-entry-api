/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By			JIRA#		Description
 * 	--------	----------------	---------	--------------------------------------------------
 *	06/03/24	C Codina			CAP-38842	Initial Version
 */

package com.rrd.c1ux.api.services.items;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.items.PNATieredPriceRequest;
import com.rrd.c1ux.api.services.items.locator.OEPricingAndAvailabilityComponentLocatorService;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.IOEPricingAndAvailability;
import com.wallace.atwinxs.orderentry.util.TierPriceRequest;

@WithMockUser
class PNATieredProcessorImplTests extends BaseServiceTest{
	
	@Mock
	PNATieredPriceRequest mockRequest;
	
	@InjectMocks
	private PNATieredProcessorImpl serviceToTest;
	
	@Mock
	IOEPricingAndAvailability mockIOEPricingAndAvailability;
	
	@Mock
	OEPricingAndAvailabilityComponentLocatorService mockOEPricingAndAvailabilityLocatorService;
	
	@Mock
	TierPriceRequest mockTierPriceRequest;
	
	
	@BeforeEach
	public void setup() throws AtWinXSException {
		
	}
	
	@Test
	void that_test_process_PNA_Tier_Price() throws AtWinXSException {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isShowOrderLinePrice()).thenReturn(true);
		
		PNATieredPriceRequest request = buildPNARequest();
		
		when(mockAppSessionBean.hasService(1)).thenReturn(true);
		serviceToTest.processPNATierPrice(request, mockSessionContainer);
		assertNotNull(request);
	}

	private PNATieredPriceRequest buildPNARequest() {
		PNATieredPriceRequest request = new PNATieredPriceRequest();
		
		request.setCorpNum("Test");
		request.setSiteId(1);
		request.setPriceClass("Test");
		return request;
	}

}
