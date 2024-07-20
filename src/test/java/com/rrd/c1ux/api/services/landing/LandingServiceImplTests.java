/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By			DTS#		Description
 *	--------	-----------			----------	-----------------------------------------------------------
 *  08/16/23	Krishna Natarajan 	CAP-42819	Added this test for method coverage
 *  10/17/23	Krishna Natarajan	CAP-44685	Added more tests to cover added method groupFeatFav
 *	01/22/24	Krishna Natarajan	CAP-46645	Added more tests to cover the OOB search widget to align with allow OOB	 
 */


package com.rrd.c1ux.api.services.landing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.addtocart.landing.LandingResponse;
import com.rrd.custompoint.admin.entity.PluggableWidgetAssignmentImpl;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.rrd.custompoint.admin.entity.UserGroupImpl;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.wallace.atwinxs.admin.ao.CustomizationAssembler;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ProfileParserUtil;
import com.wallace.atwinxs.framework.util.Util;

class LandingServiceImplTests extends BaseServiceTest{
	
	@InjectMocks
	private LandingServiceImpl landingServiceToTest;
	
	@Mock
	private UserGroupImpl mockUserGroup;
	
	@Mock
	private UserGroup mockUserGroup1;
	
	@Mock
	private CustomizationAssembler mockCustomizationAssembler;
	
	@Mock
	private ProfileParserUtil mockProfileParserUtil;
	
	@Mock
	private PluggableWidgetAssignmentImpl mockPluggableWidgetAssignmentImpl;
	
	LandingResponse objLandingResponse = new LandingResponse();

	@BeforeEach
	public void setUp() throws Exception {
		
		  when(mockSessionContainer.getApplicationSession()).thenReturn(
		  mockApplicationSession);
		  when(mockApplicationSession.getAppSessionBean()).thenReturn(
		  mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		 
	}
	
	public List<String> setList() {
		List<String> testlist = new ArrayList<String>();
		testlist.add(0, "a");
		testlist.add(1, "b");
		testlist.add(2, "c");
		testlist.add(3, "d");
		testlist.add(4, "e");
		testlist.add(5, "f");
		return testlist;
	}
	
	public List<String> setListSecond() {
		List<String> testlist = new ArrayList<String>();
		testlist.add(0, "a");
		testlist.add(1, "b");
		testlist.add(2, "c");
		testlist.add(3, "d");
		testlist.add(4, "e");
		testlist.add(5, "pw_oob");
		return testlist;
	}
	
	@Test
	void that_loadlanding_returns_success() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of());
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 0, false, List.of());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_testfaveitems() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of(mockPluggableWidgetAssignmentImpl));
			when(mockPluggableWidgetAssignmentImpl.getWidgetName()).thenReturn("pw_fave_items");
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 0, false, List.of());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_testfeatitems() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of(mockPluggableWidgetAssignmentImpl));
			when(mockPluggableWidgetAssignmentImpl.getWidgetName()).thenReturn("pw_feat_items");
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 0, false, List.of());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_allfalse() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of());
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 0, false, List.of());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_alltrue_featfave() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of());
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(5, true, 6, true, setList());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_alltrue_favefeat() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of());
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(6, true, 5, true, setList());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_falsefave_truefeat() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of());
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 2, true, new ArrayList<String>());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_truefave_falsefeat() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of());
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, true, 2, false, new ArrayList<String>());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_testfeatitems_oobfalse() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of(mockPluggableWidgetAssignmentImpl));
			when(mockPluggableWidgetAssignmentImpl.getWidgetName()).thenReturn("pw_feat_items");
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(false);
			
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 0, false, List.of());
			landingServiceToTest.checkOOBWidget(mockOESession, setList());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
	@Test
	void that_loadlanding_returns_success_testfeatitems_oobfalse_secondlist() throws AtWinXSException {

		try (MockedConstruction<CustomizationAssembler> mockedCustomizationAssembler = mockConstruction(
				CustomizationAssembler.class, (mock, context) -> {
					when(mock.getLandingHTML(any(), anyInt())).thenReturn("123");
				});
				MockedConstruction<ProfileParserUtil> mockedProfileParserUtilr = mockConstruction(
						ProfileParserUtil.class, (mock1, context) -> {
							when(mock1.parseHTML(anyString(), any(), anyString())).thenReturn("123");
						});
				MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			mockUtil.when(() -> Util.getContextPath(any())).thenReturn("123");
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), any())).thenReturn(mockUserGroup);
			when(mockUserGroup.getHomePageWidgets()).thenReturn(List.of(mockPluggableWidgetAssignmentImpl));
			when(mockPluggableWidgetAssignmentImpl.getWidgetName()).thenReturn("pw_feat_items");
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(false);
			
			objLandingResponse = landingServiceToTest.loadLanding(mockSessionContainer);
			landingServiceToTest.groupFeatFav(0, false, 0, false, List.of());
			landingServiceToTest.checkOOBWidget(mockOESession, setListSecond());
			Assertions.assertNotNull(objLandingResponse);
		}
	}
	
}
