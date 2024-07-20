/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     		DTS#                        Description
 *  --------    -----------     		-----------------------     --------------------------------
 *  08/31/23    Krishna Natarajan       CAP-43371                   JUnit addition for the added method getTranslation in UserStateProfileImpl  
 */
package com.rrd.c1ux.api.services.users;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.wallace.atwinxs.framework.util.AtWinXSException;

class UserStateProfileImplTest extends BaseServiceTest{
	
	@InjectMocks
	private UserStateProfileImpl serviceToTest;
	@Mock
	UserStateProfile mockProfile;
	@Mock
	private Properties mockProperties;
	@Mock
	private Map<String, String> mockTranslationMap;
	
	@Test
	void that_getTranslation_returns_success() throws AtWinXSException {
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString())).thenReturn(mockProperties);
	    when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
		UserStateProfile usp = serviceToTest.getTranslation(mockProfile, mockApplicationSession);
		assertNotNull(usp);
	}
}
