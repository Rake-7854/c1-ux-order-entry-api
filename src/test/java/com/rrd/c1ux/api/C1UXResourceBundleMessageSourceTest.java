/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  06/16/2023  E Anderson      CAP-41262                   Unit Testing.
 */
package com.rrd.c1ux.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.custompoint.framework.entity.TranslationText;

class C1UXResourceBundleMessageSourceTest extends BaseServiceTest {
	
	@Mock
	TranslationText transText;
			
	@InjectMocks
	C1UXResourceBundleMessageSource rbms;
	
	public static final String BASE_NAME_ROOT = "file:///C:/xs2files/translation/sf/";
	public static final String BASE_NAME = "recentOrder";
	public static final Integer VARIANT = 701;
	
	
	@BeforeEach
	void setUp() throws Exception {
		
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(new Locale("en", "US"));
		when(mockAppSessionBean.getSiteID()).thenReturn(4366);
		when(mockAppSessionBean.getBuID()).thenReturn(7125);
		when(mockAppSessionBean.getGroupName()).thenReturn("C1UX");
		
		when(transText.getLocaleVariantForViewName(mockAppSessionBean.getDefaultLocale().toString(), mockAppSessionBean.getSiteID(), mockAppSessionBean.getBuID(), mockAppSessionBean.getGroupName(), BASE_NAME)).thenReturn(VARIANT);
		
		List<String> fileNames = new ArrayList<>();
		fileNames.add(BASE_NAME_ROOT + BASE_NAME  + "_" + mockAppSessionBean.getDefaultLocale() + "_" + VARIANT);
		fileNames.add(BASE_NAME_ROOT + BASE_NAME  + "_" + mockAppSessionBean.getDefaultLocale());
		fileNames.add(BASE_NAME_ROOT + BASE_NAME  + "_" + "en");
		
		rbms = Mockito.spy(rbms);
			
		Mockito.doReturn(fileNames).when(rbms).getCalculatedFiles(anyString(), anyString(), any(Locale.class));
	}
	

	@Test
	void testGetMergedBasenameProperties() {
		assertNotNull(rbms.getMergedBasenameProperties(BASE_NAME_ROOT, BASE_NAME, mockAppSessionBean));
	}
	
}
