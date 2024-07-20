/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 *  11/10/23	A Boomker			CAP-44487		Added handling for load user profile
 *  07/01/24	R Ruth				CAP-46503		Added handling for load alt profile
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXAlternateProfileOptions;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSelectionBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUserProfileSearchRequest;
import com.rrd.c1ux.api.services.admin.locators.ProfileComponentLocatorService;
import com.rrd.custompoint.customdocs.ui.ProfileSelectorImpl;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.orderentry.customdocs.ProfileOption;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

class CustomDocsProfileServiceImplTests extends BaseOEServiceTest {

	  @InjectMocks
	  private CustomDocsProfileServiceImpl service;

	  @Mock
	  private VolatileSessionBean mockVolatileSessionBean;

	  @Mock
	  private OrderEntrySession mockOrderEntrySession;

	  @Mock
	  private ApplicationVolatileSession mockApplicationVolatileSession;

	  @Mock
	  private UserInterfaceImpl mockUI;

	  @Mock
	  private CustomDocumentItemImpl mockItem;

	  @Mock
	  private ProfileSelectorImpl mockProfileSelector;

	  @Mock
	  private C1UXCustDocProfileSearchResponse mockSearchResponse;

	  @Mock
	  private ProfileVO mockUserProfile;
	  
	  @Mock
	  private C1UXCustDocLoadAltProfileResponse mockLoadAltProfileResponse;
	  

		@Test
		void searchUserProfiles()
				throws Exception {
			C1UXCustDocUserProfileSearchRequest request = new C1UXCustDocUserProfileSearchRequest();
			setUpModuleSessionNoBase();
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		    when(mockItem.getUserInterface()).thenReturn(mockUI);
		    when(mockUI.getProfileSelector()).thenReturn(null);

		    // initial proof type of null or empty will default to image
		    C1UXCustDocProfileSearchResponse searchResponse = service.searchUserProfiles(mockSessionContainer, request);
			assertTrue(searchResponse.isSuccess());
			assertEquals(0, searchResponse.getProfileOptions().size());

		    when(mockUI.getProfileSelector()).thenReturn(mockProfileSelector);
		    List<ProfileOption> profiles = new ArrayList<>();
		    profiles.add(new ProfileOption(50, "Amy", true));
		    profiles.add(new ProfileOption(51, "Jim", true));
		    when(mockProfileSelector.doSearchC1UX(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(profiles);
			searchResponse = service.searchUserProfiles(mockSessionContainer, request);
			assertTrue(searchResponse.isSuccess());
			assertEquals(0, searchResponse.getProfileOptions().size());

			when(mockProfileSelector.isShown()).thenReturn(true);
			searchResponse = service.searchUserProfiles(mockSessionContainer, request);
			assertTrue(searchResponse.isSuccess());
			assertEquals(2, searchResponse.getProfileOptions().size());

		    populateUserSearchRequest(request);
			searchResponse = service.searchUserProfiles(mockSessionContainer, request);
			assertTrue(searchResponse.isSuccess());
			assertEquals(2, searchResponse.getProfileOptions().size());

		    when(mockProfileSelector.doSearchC1UX(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new NullPointerException());
			searchResponse = service.searchUserProfiles(mockSessionContainer, request);
			assertTrue(searchResponse.isSuccess());
			assertEquals(0, searchResponse.getProfileOptions().size());
		}


		private void populateUserSearchRequest(C1UXCustDocUserProfileSearchRequest request) {
			request.setTerm1("something");
			request.setValue1("something else");
			request.setTerm2("anotherone");
			request.setValue2("anothervalue");
			request.setTerm3("yetanother");
			request.setValue3("and here we go");
		}

		@Test
		void loadUserProfile()
				throws Exception {
		    service = Mockito.spy(service);
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(AppSessionBean.class), anyString(), anyString());
			C1UXCustDocLoadProfileRequest request = new C1UXCustDocLoadProfileRequest();
			setUpModuleSessionNoBase();
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		    when(mockItem.getUserInterface()).thenReturn(mockUI);
		    when(mockUI.getProfileSelector()).thenReturn(null);

		    // initial proof type of null or empty will default to image
		    C1UXCustDocLoadProfileResponse response = null;

		    assertThrows(AccessForbiddenException.class, () -> {
		        service.loadUserProfile(mockSessionContainer, request);
		    });

		    when(mockUI.getProfileSelector()).thenReturn(mockProfileSelector);

		    assertThrows(AccessForbiddenException.class, () -> {
		        service.loadUserProfile(mockSessionContainer, request);
		    });

		    when(mockProfileSelector.isShown()).thenReturn(true);
	        response = service.loadUserProfile(mockSessionContainer, request);
			assertFalse(response.isSuccess());
		    assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());

		    request.setProfileNumber(1);
		    doThrow(new AtWinXSException(GENERIC_ERROR_MSG, "myclass")).when(service).confirmUserTypeProfileNumber(anyInt(), any(AppSessionBean.class));
	        response = service.loadUserProfile(mockSessionContainer, request);
			assertFalse(response.isSuccess());
		    assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());

		    doReturn(false).when(service).confirmUserTypeProfileNumber(anyInt(), any(AppSessionBean.class));
	        response = service.loadUserProfile(mockSessionContainer, request);
			assertFalse(response.isSuccess());
		    assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());

		    doReturn(true).when(service).confirmUserTypeProfileNumber(anyInt(), any(AppSessionBean.class));
			doNothing().when(service).saveFullOESessionInfo(any(), anyInt());
		    response = service.loadUserProfile(mockSessionContainer, request);
			assertTrue(response.isSuccess());

			doThrow(new AtWinXSException(GENERIC_ERROR_MSG, "myclass")).when(service).saveFullOESessionInfo(any(), anyInt());
//		    when(mockUI.populateFromProfileSelection(any(AppSessionBean.class), any(OEOrderSessionBean.class), anyInt())).thenThrow(new AtWinXSException(GENERIC_ERROR_MSG, "myclass"));
		    response = service.loadUserProfile(mockSessionContainer, request);
			assertFalse(response.isSuccess());
		    assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());
		}

		@Test
		void confirmUserTypeProfileNumber() throws AtWinXSException {
		    service = Mockito.spy(service);
		    doReturn(null).when(service).getProfileVO(anyInt(), any());
		    int def = 25;
		    doReturn(def).when(service).getUserProfileDefinitionNumber(mockAppSessionBean);
		    int num = 1;
		    assertFalse(service.confirmUserTypeProfileNumber(num, mockAppSessionBean));

		    doReturn(mockUserProfile).when(service).getProfileVO(anyInt(), any());
		    when(mockUserProfile.getProfileDefinitionID()).thenReturn(355);
		    assertFalse(service.confirmUserTypeProfileNumber(num, mockAppSessionBean));

		    when(mockUserProfile.getProfileDefinitionID()).thenReturn(0);
		    assertTrue(service.confirmUserTypeProfileNumber(num, mockAppSessionBean));

		    when(mockUserProfile.getProfileDefinitionID()).thenReturn(def);
		    assertTrue(service.confirmUserTypeProfileNumber(num, mockAppSessionBean));
		}
		
		@Test
		void loadAltProfile() throws Exception {
			service = Mockito.spy(service);
			
			C1UXCustDocLoadAltProfileResponse  response=null;
			
			C1UXCustDocLoadAltProfileRequest request = new C1UXCustDocLoadAltProfileRequest();
			C1UXCustDocProfileSelectionBean bean=new C1UXCustDocProfileSelectionBean();
			bean.setDefinitionId(1);
			bean.setProfileNum(123);
			List<C1UXCustDocProfileSelectionBean> beanList=new ArrayList<>();
			beanList.add(bean);
			request.setAltProfileSelections(beanList);
			
			setUpModuleSessionNoBase();
			
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		    when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		    when(mockItem.getUserInterface()).thenReturn(mockUI);
		    when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		    when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		    
		    List<C1UXAlternateProfileOptions> apoList=new ArrayList<>();
		    C1UXAlternateProfileOptions profileOption=new C1UXAlternateProfileOptions();
		    profileOption.setCurrentProfileID(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		    profileOption.setCurrentProfileNumber(111);
		    profileOption.setDefinitionID(1);
		    profileOption.setLabel("LABEL");
		    apoList.add(profileOption);
		    doReturn(apoList).when(service).getAltProfileOptions(mockUI, mockAppSessionBean, mockVolatileSessionBean);
		    
		    
		    doNothing().when(service).validateLoadAlternateProfileRequest(apoList,request);
		    doReturn(true).when(service).findAltProfileNumberInList(bean.getDefinitionId(), bean.getProfileNum(), apoList);
		    doNothing().when(service).saveFullOESessionInfo(any(), anyInt());
		    response = service.loadAltProfile(mockSessionContainer, request);
		    assertTrue(response.isSuccess());
		    
		    
		    doReturn(false).when(service).findAltProfileNumberInList(bean.getDefinitionId(), bean.getProfileNum(), apoList);
		    response = service.loadAltProfile(mockSessionContainer, request);
		    assertFalse(response.isSuccess());
		    
		    
		    assertThrows(Exception.class, () -> {
		    	service.loadAltProfile(mockSessionContainer, null);
		    });
		    
		    
		    request = new C1UXCustDocLoadAltProfileRequest();
			bean=new C1UXCustDocProfileSelectionBean();
			bean.setDefinitionId(1);
			bean.setProfileNum(0);
			beanList=new ArrayList<>();
			beanList.add(bean);
			request.setAltProfileSelections(beanList);
			response = service.loadAltProfile(mockSessionContainer, request);
			assertFalse(response.isSuccess());
		}
		
		@Test
		void findAltProfileDefinitionInList() {
			service = Mockito.spy(service);
			
			List<C1UXAlternateProfileOptions> apoList=new ArrayList<>();
		    C1UXAlternateProfileOptions profileOption=new C1UXAlternateProfileOptions();
		    profileOption.setCurrentProfileID(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		    profileOption.setCurrentProfileNumber(111);
		    profileOption.setDefinitionID(1);
		    profileOption.setLabel("LABEL");
		    apoList.add(profileOption);
			
			boolean result=service.findAltProfileDefinitionInList(2, apoList);
			assertFalse(result);
			
			result=service.findAltProfileDefinitionInList(1, apoList);
			assertTrue(result);
		}
		
		@Test
		void getAltProfileDefinitionType() {
			service = Mockito.spy(service);
			
			List<C1UXAlternateProfileOptions> apoList=new ArrayList<>();
		    C1UXAlternateProfileOptions profileOption=new C1UXAlternateProfileOptions();
		    profileOption.setCurrentProfileID(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		    profileOption.setCurrentProfileNumber(111);
		    profileOption.setDefinitionID(1);
		    profileOption.setLabel("LABEL");
		    apoList.add(profileOption);
			
			String result=service.getAltProfileDefinitionType(1, apoList);
			assertTrue(result.equalsIgnoreCase("LABEL"));
			
			result=service.getAltProfileDefinitionType(7, apoList);
			assertTrue(result.equalsIgnoreCase("7"));
		}
		
		@Test
		void validateLoadAlternateProfileRequest() throws AccessForbiddenException {
			service = Mockito.spy(service);
			
			assertThrows(AccessForbiddenException.class, () -> {
		    	service.validateLoadAlternateProfileRequest(null, null);
		    });
			
			List<C1UXAlternateProfileOptions> apoList=new ArrayList<>();
		    C1UXAlternateProfileOptions profileOption=new C1UXAlternateProfileOptions();
		    profileOption.setCurrentProfileID(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		    profileOption.setCurrentProfileNumber(111);
		    profileOption.setDefinitionID(1);
		    profileOption.setLabel("LABEL");
		    apoList.add(profileOption);
		    
		    C1UXCustDocProfileSelectionBean selectionBean=new C1UXCustDocProfileSelectionBean();
		    selectionBean.setDefinitionId(DEVTEST_SITE_ID);
		    selectionBean.setProfileNum(123);
		    
		    List<C1UXCustDocProfileSelectionBean> altProfileSelections=new ArrayList<>();
		    altProfileSelections.add(selectionBean);
		    
			C1UXCustDocLoadAltProfileRequest altProfileRequest=new C1UXCustDocLoadAltProfileRequest();
			altProfileRequest.setAltProfileSelections(altProfileSelections);
			
			assertThrows(AccessForbiddenException.class, () -> {
				service.validateLoadAlternateProfileRequest(apoList, altProfileRequest);
			});
		}
		
		@Test
		void getAltProfileOptions() {
			service = Mockito.spy(service);
			
			C1UXCustDocPageBean mockPageBean=new C1UXCustDocPageBean();
			List<C1UXAlternateProfileOptions> returnList=new ArrayList<>();
			
			List<C1UXAlternateProfileOptions> apoList=new ArrayList<>();
		    C1UXAlternateProfileOptions profileOption=new C1UXAlternateProfileOptions();
		    profileOption.setCurrentProfileID(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		    profileOption.setCurrentProfileNumber(111);
		    profileOption.setDefinitionID(1);
		    profileOption.setLabel("LABEL");
		    apoList.add(profileOption);
		    
		    mockPageBean.setAlternateProfiles(apoList);
			
		    returnList=service.getAltProfileOptions(mockUI, mockAppSessionBean, mockVolatileSessionBean);
			
			assertTrue(returnList.isEmpty());
		}
		
		@Test
		void findAltProfileNumberInList() {
			service = Mockito.spy(service);
			
			List<C1UXAlternateProfileOptions> apoList=new ArrayList<>();
		    C1UXAlternateProfileOptions profileOption=new C1UXAlternateProfileOptions();
		    profileOption.setCurrentProfileID(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		    profileOption.setCurrentProfileNumber(111);
		    profileOption.setDefinitionID(1);
		    profileOption.setLabel("LABEL");
		    ProfileOption option=new ProfileOption(111, null, false);
		    
		    List<ProfileOption> profileOptionsList = new ArrayList<>();
		    profileOptionsList.add(option);
		 
		    profileOption.setProfileOptions(profileOptionsList);
		    apoList.add(profileOption);
			
			boolean result=service.findAltProfileNumberInList(1, 111, apoList);
			assertTrue(result);
			
			result=service.findAltProfileNumberInList(1, 1717, apoList);
			assertFalse(result);
			
		}
		
		@Test
		void validationError() throws Exception {
			service = Mockito.spy(service);
			
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			doReturn("TEST ERROR").when(service).getTranslation(mockAppSessionBean, "MY NAME", "DEFAULT");
			
			assertThrows(Exception.class, () -> {
				service.validationError(mockAppSessionBean);
			});
		}
		
}
