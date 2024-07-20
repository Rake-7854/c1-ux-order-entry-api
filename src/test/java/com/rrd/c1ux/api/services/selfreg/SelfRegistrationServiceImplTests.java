/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/27/23	L De Leon			CAP-45907				Initial Version
 *	01/12/24	Satishkumar A		CAP-46380				C1UX BE - Create api to retrieve initial user/profile information
 *	01/01/24	M Sakthi			CAP-45913				C1UX BE - Modify and test new API to pre-populate fields for Corporate Profile
 *	01/08/24	N Caceres			CAP-45910				C1UX BE - Modify and test new API to pre-populate fields for Extended Profile
 *	02/28/24	S Ramachandran		CAP-47410				Added junit tests for save Self Registration method - Basic profile
 *	03/01/24	M Sakthi			CAP-47450				C1UX BE - Create API to validate the extended profile fields
 *	03/04/24	C Codina			CAP-47324				C1UX BE - Self Registration save corporate profile information
 *	03/02/24    M Sakthi			CAP-47323				C1UX BE - Self Registration save extended profile information
 *	03/02/24    Satishkumar A		CAP-47592				C1UX BE - Create validation story for Password validation
 *	03/05/24	S Ramachandran		CAP-47410				Added junit tests for validate basic self Registration fields email, no space in userid
 *	03/07/24	L De Leon			CAP-47615				Added tests methods for populating profile site attributes
 *	03/07/24	M Sakthi			CAP-47657				Added junit tests for validate attributes for self Registration
 *	03/08/24	C Codina			CAP-47669				Added junit for buildSiteAttributes
 *	03/05/24	S Ramachandran		CAP-47410				Added junit tests for validate basic self Registration fields email, no space in userid
 *	03/05/24	N Caceres			CAP-47670				Added unit testing for saving UDFs
 *	03/07/24	Krishna Natarajan	CAP-47671				Added line for Junit on self registration save
 *	03/12/24	L De Leon			CAP-47775				Added tests methods for validating and setting default address code
 *	03/12/24	Krishna Natarajan	CAP-47671				Added test methods for tbomb and loginQueryString
 *	03/13/24	C Codina			CAP-47853				Added test methods for corporate profile validateCountry and validateStateField
 *  05/09/24	Krishna Natarajan	CAP-49057				Added logic and commented out unnecessary stubbings in validating Pattern after user in validate basic profile test methods
 */
package com.rrd.c1ux.api.services.selfreg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.velocity.util.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributeValue;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationPatternAfterResponse;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveRequest;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveResponse;
import com.rrd.c1ux.api.models.users.C1UXProfileImpl;
import com.rrd.c1ux.api.models.users.SelfAdminSiteAttributes;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.admin.locators.PasswordComponentLocatorService;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.admin.entity.SelfRegistration;
import com.rrd.custompoint.admin.entity.SelfRegistrationImpl;
import com.rrd.custompoint.admin.entity.SiteAttributeValue;
import com.rrd.custompoint.admin.entity.SiteAttributeValueImpl;
import com.rrd.custompoint.admin.entity.SiteAttributeValues;
import com.rrd.custompoint.admin.profile.dao.ProfileDAO;
import com.rrd.custompoint.admin.profile.entity.ProfileImpl;
import com.rrd.custompoint.admin.profile.entity.ReferenceFieldDefinition.DisplayType;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.rrd.custompoint.admin.profile.entity.UserDefinedFieldImpl;
import com.rrd.custompoint.admin.profile.entity.UserDefinedFields;
import com.rrd.custompoint.framework.util.objectfactory.DAOObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.util.PasswordSecuritySettings;
import com.wallace.atwinxs.admin.vo.BusinessUnitVO;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.systemfunctions.SystemFunctionsConstants;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.IBusinessUnit;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.ILoginInterface;
import com.wallace.atwinxs.interfaces.IPassword;
import com.wallace.atwinxs.locale.ao.CountryBean;
import com.wallace.atwinxs.locale.ao.StateBean;

class SelfRegistrationServiceImplTests extends BaseOEServiceTest {

	private static final String FAIL = "Failed";
	private static final String TEST_LOGIN_ID = "USER_ID";
	private static final String TEST_DISPLAY_NAME = "DISPLAY_NAME";
	private static final String TEST_DISPLAY_NAME_MORECHARS = "DISPLAY_NAME_DISPLAY_NAME_DISPLAY_NAME";
	private static final int TEST_SITE_ID = 1234;
	private static final int TEST_SITE_ID0 = 0;
	private static final int TEST_BU_ID = 4321;
	private static final String TEST_INVALID_DEFAULT_ADDRESS_CD = "D";
	private static final String DUMMY_TEXT = "mi bibendum neque egestas congue quisque egestas diam in arcu cursus euismod quis viverra nibh cras pulvinar mattis nunc sed blandit libero volutpat sed cras ornare arcu dui vivamus arcu felis bibendum ut tristique et egestas quis ipsum suspendisse ultrices gravida dictum fusce ut placerat orci nulla pellentesque dignissim enim sit amet venenatis urna cursus eget nunc scelerisque viverra mauris in aliquam sem fringilla ut morbi tincidunt augue interdum velit euismod in pellentesque massa placerat duis ultricies lacus sed turpis tincidunt id aliquet risus feugiat in ante metus dictum at tempor commodo ullamcorper a lacus vestibulum sed arcu non odio euismod lacinia at quis risus sed vulputate odio ut enim blandit volutpat maecenas volutpat blandit aliquam etiam erat velit scelerisque in dictum non consectetur a erat nam at lectus urna duis convallis convallis tellus id interdum velit laoreet id donec ultrices tincidunt arcu non sodales neque sodales ut etiam sit amet nisl purus in mollis nunc sed id semper risus in hendrerit gravida rutrum quisque non tellus orci ac auctor augue mauris augue neque gravida in fermentum et sollicitudin ac orci phasellus egestas tellus rutrum tellus pellentesque eu tincidunt tortor aliquam nulla facilisi cras fermentum odio eu feugiat pretium nibh ipsum consequat nisl vel pretium lectus quam id leo in vitae turpis massa sed elementum tempus egestas sed sed risus pretium quam vulputate dignissim suspendisse in est ante in nibh mauris cursus mattis molestie a iaculis at erat pellentesque adipiscing commodo elit at imperdiet dui accumsan sit amet nulla facilisi morbi tempus iaculis urna id volutpat";

	private static final String COUNTRY_NAME_USA = "United States";
	private static final String COUNTRY_CODE_USA = "USA";
	private static final String ERROR_MSG422 = "ERROR_MSG422";
	private SelfRegistrationPatternAfterResponse selfRegistrationPatternAfterResponse;
	private SelfRegistrationSaveResponse selfRegistrationSaveResponse;

	private SelfRegistrationSaveResponse srSaveResponse_InValidPatternAfterUser;
	private SelfRegistrationSaveResponse srSaveResponse_ValidPatternAfterUser;
	private SelfRegistrationSaveRequest selfRegistrationSaveRequest;
	private SelfRegistrationSaveResponse c1SelfRegistrationSaveResponse;

	@Mock
	private EntityObjectMap mockEntityObjectMap;

	@Mock
	private SelfRegistration mockSelfRegistration;

	@Mock
	private Properties mockProperties;

	@Mock
	private IBusinessUnit mockIBusinessUnit;

	@Mock
	private BusinessUnitVO mockBusinessUnitVO;

	@Mock
	private ILoginInterface mockILoginInterface;

	@Mock
	CountryBean mockCountryBean;

	@Mock
	private ICatalog mockCatalogComp;

	@Mock
	ProfileDAO mockProfileDAO;

	@Mock
	SelfAdminService mockSelfAdminService;

	@Mock
	DAOObjectMap mockDAOObjectMap;

	@Mock
	PasswordComponentLocatorService mockPasswordComponentLocatorService;

	@Mock
	IPassword mockPassword;

	@InjectMocks
	private SelfRegistrationServiceImpl service;

	@Mock
	private SelfRegistrationServiceImpl mockService;

	@Mock
	private Map<String, String> mockTranslationMap;

	private MockedStatic<UtilCountryInfo> mockUtilCountryInfo;

	@Mock
	private SiteAttributes mockSiteAttributes;

	@Mock
	private SiteAttribute mockSiteAttribute;

	@Mock
	private SiteAttributeValues mockSiteAttributeValues;

	@Mock
	private SiteAttributeValue mockSiteAttributeValue;

	@Mock
	private UserDefinedFields mockUserDefinedFields;

	@Mock
	C1UserSiteAttribute mockC1UserSiteAttribute;

	@Mock
	SelfRegistrationSaveRequest mockSelfRegistrationSaveRequest;

	@Mock
	private XSProperties mockXSProperty;

	// CAP-47915
	@Mock
	private List<UserDefinedField> mockUDFlist;

	@BeforeEach
	void setup() throws Exception {

		mockUtilCountryInfo = mockStatic(UtilCountryInfo.class);
		selfRegistrationPatternAfterResponse = null;
	}

	@AfterEach()
	public void cleanUp() {
		mockUtilCountryInfo.close();
	}

	@Test
	void that_getPatternAfterUsers_return_success_when_pabu_is_not_valid() throws Exception {

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_LOGIN_ID);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(mockProperties);
		doNothing().when(mockSelfRegistration).init(mockProperties, TEST_SITE_ID, TEST_LOGIN_ID);
		doReturn(AtWinXSConstant.INVALID_BUS_UNIT_ID).when(mockSelfRegistration).getPaBU();
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		doReturn(patternAfterUsers).when(mockILoginInterface).getPAAccounts(TEST_SITE_ID,
				AtWinXSConstant.INVALID_BUS_UNIT_ID);

		selfRegistrationPatternAfterResponse = service.getPatternAfterUsers(mockSessionContainer);

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());
		assertFalse(selfRegistrationPatternAfterResponse.getPatternAfterUsers().isEmpty());
	}

	@Test
	void that_getPatternAfterUsers_return_success_when_pabu_is_valid() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_LOGIN_ID);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(mockProperties);
		doNothing().when(mockSelfRegistration).init(mockProperties, TEST_SITE_ID, TEST_LOGIN_ID);
		doReturn(TEST_BU_ID).when(mockSelfRegistration).getPaBU();
		doReturn(mockIBusinessUnit).when(mockComponentLocator).locateBusinessUnitComponent(null);
		doReturn(mockBusinessUnitVO).when(mockIBusinessUnit).findByBuID(TEST_BU_ID);
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		doReturn(new ArrayList<LoginVOKey>()).when(mockILoginInterface).getPAAccounts(TEST_SITE_ID, TEST_BU_ID);

		selfRegistrationPatternAfterResponse = service.getPatternAfterUsers(mockSessionContainer);

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());
		assertTrue(selfRegistrationPatternAfterResponse.getPatternAfterUsers().isEmpty());
	}

	@Test
	void that_getPatternAfterUsers_returnErrorMessage_when_exception_thrown() throws Exception {

		// add mock Objects for sessions
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_LOGIN_ID);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(mockProperties);
		doNothing().when(mockSelfRegistration).init(mockProperties, TEST_SITE_ID, TEST_LOGIN_ID);
		doReturn(TEST_BU_ID).when(mockSelfRegistration).getPaBU();
		doReturn(mockIBusinessUnit).when(mockComponentLocator).locateBusinessUnitComponent(null);
		doReturn(null).when(mockIBusinessUnit).findByBuID(TEST_BU_ID);
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockILoginInterface)
				.getPAAccounts(TEST_SITE_ID, AtWinXSConstant.INVALID_BUS_UNIT_ID);

		selfRegistrationPatternAfterResponse = service.getPatternAfterUsers(mockSessionContainer);

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertFalse(selfRegistrationPatternAfterResponse.isSuccess());
		assertTrue(selfRegistrationPatternAfterResponse.getPatternAfterUsers().isEmpty());
	}

	// CAP-46380
	@Test
	void that_getInitialSelfRegUser_return_failure_when_getPatternAfterUsers_empty() throws Exception {

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// add mock Objects for sessions
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(mockProperties);

		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, null);

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertFalse(selfRegistrationPatternAfterResponse.isSuccess());

	}

	void that_populateSelfRegistrationCorporateProfile_Test() throws AtWinXSException {
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);

		service.populateSelfRegistrationCorporateProfile(selfRegistrationSaveRequest, mockSelfRegistration);
	}

	// CAP-45913
	@Test
	void that_getInitialSelfRegUser_corporateProfile_return_canUserEdit_success() throws Exception {

		List<User> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(mockUser);

		patternAfterUsers.add(mockUser);
		// Country Exist
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		// add mock Objects for sessions

		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		// add mock Objects for sessions
		mockProfileMethods(patternAfterUsers);
		mockCoporateProfileMethods();
//		mockExtendedProfileMethods();
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
		mockPasswordSettings();

		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, "USER1");

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());

	}

	@Test
	void that_getInitialSelfRegUser_corporateProfile_return_canUserNotEdit_success() throws Exception {

		List<User> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(mockUser);

		// add mock Objects for sessions
		mockProfileMethods(patternAfterUsers);
		// mockCoporateProfileMethods();
//		mockExtendedProfileMethods();
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(false);
		mockPasswordSettings();

		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, "USER1");

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());

	}

	private void mockProfileMethods(List<User> patternAfterUsers) throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(any(), any());
		when(mockAppSessionBean.getSiteID()).thenReturn(TEST_SITE_ID);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(mockProperties);
		doReturn(patternAfterUsers).when(mockSelfRegistration).getPatternAfterUsers();
		when(mockUser.getLoginID()).thenReturn("User1");
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
	}

	private void mockCoporateProfileMethods() {
		when(mockCorporateProfile.getCompanyName()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyName2()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getLine1Address()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getLine2Address()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCityName()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getStateCd()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getZipCd()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCountryCd()).thenReturn("USA");
		when(mockCorporateProfile.getCompanyPhoneNumber()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyFaxNumber()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyDivisionName()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyDepartmentName()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyTitleText()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyURLAddress()).thenReturn(DUMMY_TEXT);
		when(mockCorporateProfile.getCompanyLogoURLAddress()).thenReturn(DUMMY_TEXT);
	}

	private void mockExtendedProfileMethods() {
		when(mockExtendedProfile.getContactName2()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getLine1Address()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getLine2Address()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getCityName()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getStateCd()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getZipCd()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getCountryCd()).thenReturn("USA");
		when(mockExtendedProfile.getContactMiddleInitialName()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactTitleName()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactSuffixName()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactFaxNumber()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactPagerNumber()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactMobileNumber()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactTollFreePhoneNumber()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactWebAddress()).thenReturn(DUMMY_TEXT);
		when(mockExtendedProfile.getContactPhotoURLAddress()).thenReturn(DUMMY_TEXT);
	}

	private void mockCoporateProfileCountryEmptyMethods() {
		when(mockCorporateProfile.getCompanyName()).thenReturn("");
		when(mockCorporateProfile.getCompanyName2()).thenReturn("");
		when(mockCorporateProfile.getLine1Address()).thenReturn("");
		when(mockCorporateProfile.getLine2Address()).thenReturn("");
		when(mockCorporateProfile.getCityName()).thenReturn("");
		when(mockCorporateProfile.getStateCd()).thenReturn("");
		when(mockCorporateProfile.getZipCd()).thenReturn("");
		when(mockCorporateProfile.getCountryCd()).thenReturn("");
		when(mockCorporateProfile.getCompanyPhoneNumber()).thenReturn("");
		when(mockCorporateProfile.getCompanyFaxNumber()).thenReturn("");
		when(mockCorporateProfile.getCompanyDivisionName()).thenReturn("");
		when(mockCorporateProfile.getCompanyDepartmentName()).thenReturn("");
		when(mockCorporateProfile.getCompanyTitleText()).thenReturn("");
		when(mockCorporateProfile.getCompanyURLAddress()).thenReturn("");
		when(mockCorporateProfile.getCompanyLogoURLAddress()).thenReturn("");
	}

	// CAP-45910
	@Test
	void that_getInitialSelfRegUser_extendedProfile_return_success() throws Exception {

		List<User> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(mockUser);
		// Country Exist
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		// add mock Objects for sessions

		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		mockProfileMethods(patternAfterUsers);
		mockCoporateProfileMethods();
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
		mockExtendedProfileMethods();
		mockPasswordSettings();

		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, "USER1");

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());
	}

	@Test
	void that_getInitialSelfRegUser_corporate_return_success() throws Exception {

		List<User> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(mockUser);
		// Country Exist
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		// add mock Objects for sessions

		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		mockProfileMethods(patternAfterUsers);
		mockCoporateProfileMethods();
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
		mockExtendedProfileMethods();
		mockPasswordSettings();

		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, "USER1");

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());

	}

	@Test
	void that_getInitialSelfRegUser_corporate_empty_return_success() throws Exception {

		List<User> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(mockUser);
		// Country Exist
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		// add mock Objects for sessions

		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		mockProfileMethods(patternAfterUsers);
		mockCoporateProfileCountryEmptyMethods();
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
		mockPasswordSettings();

		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, "USER1");

		assertNotNull(selfRegistrationPatternAfterResponse);
		assertTrue(selfRegistrationPatternAfterResponse.isSuccess());
	}

	@Test
	void that_getInitialSelfRegUser_return_success_Catch() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doThrow(new RuntimeException()).when(mockObjectMapFactoryService).getEntityObjectMap();
		selfRegistrationPatternAfterResponse = service.getInitialSelfRegUser(mockSessionContainer, "USER1");
		assertFalse(selfRegistrationPatternAfterResponse.isSuccess());
	}

	@Test
	void that_validatepatterafter() throws Exception {
		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		doReturn(AtWinXSConstant.INVALID_BUS_UNIT_ID).when(mockSelfRegistration).getPaBU();
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		doReturn(patternAfterUsers).when(mockILoginInterface).getPAAccounts(TEST_SITE_ID0,
				AtWinXSConstant.INVALID_BUS_UNIT_ID);
		boolean ABC = service.checkPAUser(mockAppSessionBean, "USER1");
		selfRegistrationSaveResponse = service.validatePatternAfter(mockSessionContainer, "USER1");
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	@Test
	void that_validatepatterafter_validbuid() throws Exception {
		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		doReturn(7125).when(mockSelfRegistration).getPaBU();
		doReturn(mockIBusinessUnit).when(mockComponentLocator).locateBusinessUnitComponent(null);
		doReturn(mockBusinessUnitVO).when(mockIBusinessUnit).findByBuID(7125);
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		doReturn(patternAfterUsers).when(mockILoginInterface).getPAAccounts(TEST_SITE_ID0, 7125);
		boolean ABC = service.checkPAUser(mockAppSessionBean, "USER1");
		selfRegistrationSaveResponse = service.validatePatternAfter(mockSessionContainer, "USER1");
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	@Test
	void that_validatepatterafter_checkmaxlength() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		selfRegistrationSaveResponse = service.validatePatternAfter(mockSessionContainer,
				"TEST_DISPLAY_NAME_MORECHARS_TEST_DISPLAY_NAME_MORECHARS");
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	private void mockPasswordSettings() throws AtWinXSException {
		doReturn(mockToken).when(mockAppSessionBean).getCustomToken();
		doReturn(mockUser).when(mockSelfRegistration).getPatternAfterUserSelected();
		doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		doReturn(buildPasswordRule()).when(mockPassword).getPasswordSecuritySettings(any(), any(), any());
	}

	private PasswordSecuritySettings buildPasswordRule() {
		PasswordSecuritySettings passwordRule = new PasswordSecuritySettings();
		passwordRule.setMinimumPasswordChars(6);
		passwordRule.setMinimumPasswordNumericChars(1);
		passwordRule.setMinimumSpecialChars(1);
		passwordRule.setMinimumUpperCaseChars(1);
		return passwordRule;
	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_return_success200() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		selfRegistrationSaveRequest = getSRBasicProfileSaveRequest_Valid();
		selfRegistrationSaveRequest.setC1UserDefinedFields(buildC1UserDefinedFields());
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// CAP-47324
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		// mockCoporateProfileMethods();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
//		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

//		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockEntityObjectMap.getEntity(eq(SelfRegistration.class), any())).thenReturn(mockSelfRegistration);
//		doReturn(mockUser).when(mockEntityObjectMap).getEntity(User.class, null);
		when(mockEntityObjectMap.getEntity(eq(User.class), any())).thenReturn(mockUser);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(false);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(false);
		when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1234);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		when(mockTranslationService.getResourceBundle(any(), any())).thenReturn(mockProperties);
		when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
		mockUDFMethods();
		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);
		when(mockProfile.getSiteAttributes()).thenReturn(mockSiteAttributes);
		doReturn(true).when(mockSelfAdminService).validateSiteAttrIDAndAttrValueID(any(), any(), any());
		when(mockProfile.hasSiteAttributes()).thenReturn(true);
		List<SiteAttribute> getSiteAttrs = new ArrayList<>();
		getSiteAttrs.add(mockSiteAttr);
		when(mockSiteAttributes.getSiteAttrs()).thenReturn(getSiteAttrs);
		when(mockSiteAttr.getAttrID()).thenReturn(5302);
		when(mockSiteAttr.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
		List<SiteAttributeValue> getSiteAttributeValues = new ArrayList<>();
		getSiteAttributeValues.add(mockSiteAttributeValue);
		when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(getSiteAttributeValues);
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class);) {// CAP-47671
			mockedStatic.when(() -> PropertyUtil.getProperties(SystemFunctionsConstants.PROP_GLOBAL_BUNDLE_NAME))
					.thenReturn(mockXSProperty);// CAP-47671
			doReturn("HTTPS://").when(service).loginQueryString(mockAppSessionBean, mockSelfRegistration);
			selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer,
					selfRegistrationSaveRequest);
			assertTrue(selfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_PatternAfterUserFailed_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when Invalid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_Valid();
		srSaveResponse_InValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserFailed();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(srSaveResponse_InValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_DuplicateUSERID_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Not-Editable, valid selfReg request
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_Valid();
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// CAP-47324
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		// mockCoporateProfileMethods();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		// doReturn(mockUser).when(mockEntityObjectMap).getEntity(User.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		// when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		// when(mockUser.isExisting()).thenReturn(true);
		// CAP-47915
		when(mockProfile.getUserDefinedFields()).thenReturn(mockUserDefinedFields);
		when(mockUserDefinedFields.getUserDefinedFields()).thenReturn(mockUDFlist);

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_InValid1_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		// Basic profile attributes were BLANK
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_InValid1();
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// CAP-47324
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		// mockCoporateProfileMethods();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isFirstNameRequired()).thenReturn(true);
		when(mockProfileDefinition.isLastNameRequired()).thenReturn(true);
		when(mockProfileDefinition.isEmailRequired()).thenReturn(true);
		when(mockProfileDefinition.isPhoneRequired()).thenReturn(true);
		// when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		// CAP-47915
		when(mockProfile.getUserDefinedFields()).thenReturn(mockUserDefinedFields);
		when(mockUserDefinedFields.getUserDefinedFields()).thenReturn(mockUDFlist);

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());

	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_InValid2_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		// Basic profile attributes GT THAN DB field size
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_InValid2();
		srSaveResponse_InValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// CAP-47324
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		// mockCoporateProfileMethods();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_InValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any(String.class));

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		// when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		// CAP-47915
		when(mockProfile.getUserDefinedFields()).thenReturn(mockUserDefinedFields);
		when(mockUserDefinedFields.getUserDefinedFields()).thenReturn(mockUDFlist);

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());

	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_InValid3_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		// Basic profile attribute email in Invalid Format
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_InValid3();
		srSaveResponse_InValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		// CAP-47324
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);
		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		// mockCoporateProfileMethods();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_InValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		doReturn(mockUser).when(mockEntityObjectMap).getEntity(User.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		// when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		// CAP-47915
		when(mockProfile.getUserDefinedFields()).thenReturn(mockUserDefinedFields);
		when(mockUserDefinedFields.getUserDefinedFields()).thenReturn(mockUDFlist);

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_InValid4_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		// if throws error NoClassDefFoundError when USER ID already exists
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_Valid();
		srSaveResponse_InValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		doThrow(new NoClassDefFoundError()).when(service).validatePatternAfter(any(SessionContainer.class), any());

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-47410
	@Test
	void that_saveSelfRegistration_InValid5_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		// if throws error AtWinXSException
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_Valid();
		srSaveResponse_InValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(service)
				.validatePatternAfter(any(SessionContainer.class), any());

		selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-46801
	private SelfRegistrationSaveResponse getSelfRegistrationSaveResponse_PatternAfterUserSuccess() {

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		selfRegistrationSaveResponse.setMessage(AtWinXSConstant.EMPTY_STRING);
		return selfRegistrationSaveResponse;
	}

	// CAP-46801
	private SelfRegistrationSaveResponse getSelfRegistrationSaveResponse_PatternAfterUserFailed() {

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(false);
		selfRegistrationSaveResponse.setMessage(AtWinXSConstant.EMPTY_STRING);
		return selfRegistrationSaveResponse;
	}

	// CAP-46801
	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_InValid1() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setUserID("");
		selfRegistrationSaveRequest.setPassword("");
		selfRegistrationSaveRequest.setFirstName("");
		selfRegistrationSaveRequest.setLastName("");
		selfRegistrationSaveRequest.setEmail("");
		selfRegistrationSaveRequest.setPhone("");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		return selfRegistrationSaveRequest;
	}

	// CAP-46801
	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_InValid2() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setUserID("SF-USER1 SF-USER1 Char GT 16");
		selfRegistrationSaveRequest.setPassword("test1234");
		selfRegistrationSaveRequest.setFirstName("SF2 Subbu First SF2 Subbu First Char GT 24");
		selfRegistrationSaveRequest.setLastName("SF2 Subbu LastSF2 Subbu Last Char GT 24");
		selfRegistrationSaveRequest.setEmail(
				"subbu1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@rrd.com");
		selfRegistrationSaveRequest.setPhone("1-800-588-23001-800-588-2300");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		return selfRegistrationSaveRequest;
	}

	// CAP-46801
	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_InValid3() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setUserID("SF-USER4");
		selfRegistrationSaveRequest.setPassword("test1234");
		selfRegistrationSaveRequest.setFirstName("SF2 Subbu First");
		selfRegistrationSaveRequest.setLastName("SF2 Subbu Last");
		selfRegistrationSaveRequest.setEmail("subbu1rrd.com");
		selfRegistrationSaveRequest.setPhone("1-800-588-2300");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		return selfRegistrationSaveRequest;
	}

	// CAP-46801
	private SelfRegistrationSaveRequest getSRBasicProfileSaveRequest_Valid() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setUserID("SF-USER4");
		selfRegistrationSaveRequest.setPassword("test1234");
		selfRegistrationSaveRequest.setFirstName("SF2 Subbu First");
		selfRegistrationSaveRequest.setLastName("SF2 Subbu Last");
		selfRegistrationSaveRequest.setEmail("subbu1@rrd.com");
		selfRegistrationSaveRequest.setPhone("1-800-588-2300");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		selfRegistrationSaveRequest.setValidatePassword("test1234");
		selfRegistrationSaveRequest.setCheckWarning(false);

		selfRegistrationSaveRequest.setCpName1("TestCPName1");
		selfRegistrationSaveRequest.setCpName2("TestCPName2");
		selfRegistrationSaveRequest.setCpAddressLine1("testAddressLine1");
		selfRegistrationSaveRequest.setCpAddressLine2("TestAddressLine2");
		selfRegistrationSaveRequest.setCpAddressLine2("TestAddressLine3");
		selfRegistrationSaveRequest.setCpCity("TestCity");
		selfRegistrationSaveRequest.setCpStateCd("USA");
		selfRegistrationSaveRequest.setCpZipCd("123");
		selfRegistrationSaveRequest.setCpCountryCd("USA");
		selfRegistrationSaveRequest.setCpTitle("TestTitle");
		selfRegistrationSaveRequest.setCpPhoneNumber("test");
		selfRegistrationSaveRequest.setCpFaxNumber("test");
		selfRegistrationSaveRequest.setCpTitle("testTitle");
		selfRegistrationSaveRequest.setCpDivision("testdivision");
		selfRegistrationSaveRequest.setCpDepartment("testcpdepartment");
		selfRegistrationSaveRequest.setCpWebUrl("testURL");
		selfRegistrationSaveRequest.setCpImageUrl("testImageURL");

		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst;
		List<C1UserSiteAttribute> c1UserSiteAttributeLst = new ArrayList<>();

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(258387));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(258333));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(258334));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(5302, c1UserSiteAttributeValueLst));

		selfRegistrationSaveRequest.setC1UserSiteAttributes(c1UserSiteAttributeLst);

		return selfRegistrationSaveRequest;
	}

	// CAP-47450 - Extended Profile validate
	@Test
	void that_getSelfRegUser_extendedProfile_return_success() throws Exception {

		service = Mockito.spy(service);
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1234);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);

		Map<String, String> countryMap = new HashMap<String, String>();
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS, "Yes");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL, "USA");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS, "Yes");
		try (MockedConstruction<SelfAdminUtil> mockedSelfAdminUtil = mockConstruction(SelfAdminUtil.class,
				(mock, context) -> {
					when(mock.validateAndGetCountryStateName(any(), any())).thenReturn(countryMap);
				})) {
			selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_Valid();
			when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
			selfRegistrationSaveResponse = service.validateExtendedProfile(mockSessionContainer,
					selfRegistrationSaveRequest);

			assertNotNull(selfRegistrationSaveResponse);
			assertTrue(selfRegistrationSaveResponse.isSuccess());
		}
	}

	@Test
	void that_getSelfRegUser_extendedProfile_return_maxLength_422() throws Exception {

		service = Mockito.spy(service);
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1234);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);

		Map<String, String> countryMap = new HashMap<String, String>();
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS, "No");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL, "");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS, "NA");
		try (MockedConstruction<SelfAdminUtil> mockedSelfAdminUtil = mockConstruction(SelfAdminUtil.class,
				(mock, context) -> {
					when(mock.validateAndGetCountryStateName(any(), any())).thenReturn(countryMap);
				})) {
			selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_maxLengh();
			when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
			selfRegistrationSaveResponse = service.validateExtendedProfile(mockSessionContainer,
					selfRegistrationSaveRequest);
			assertNotNull(selfRegistrationSaveResponse);
			assertFalse(selfRegistrationSaveResponse.isSuccess());
		}
	}

	@Test
	void that_getSelfRegUser_extendedProfile_return_empty_422() throws Exception {

		service = Mockito.spy(service);
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1234);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isAddressRequired()).thenReturn(true);
		when(mockProfileDefinition.isMiddleInitialRequired()).thenReturn(true);
		when(mockProfileDefinition.isTitleRequired()).thenReturn(true);
		when(mockProfileDefinition.isSuffixRequired()).thenReturn(true);
		when(mockProfileDefinition.isFaxRequired()).thenReturn(true);
		when(mockProfileDefinition.isMobileRequired()).thenReturn(true);
		when(mockProfileDefinition.isPagerRequired()).thenReturn(true);
		when(mockProfileDefinition.isTollFreeRequired()).thenReturn(true);
		when(mockProfileDefinition.isWebRequired()).thenReturn(true);
		when(mockProfileDefinition.isPhotoURLRequired()).thenReturn(true);

		Map<String, String> countryMap = new HashMap<String, String>();
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS, "No");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL, "");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS, "NA");
		try (MockedConstruction<SelfAdminUtil> mockedSelfAdminUtil = mockConstruction(SelfAdminUtil.class,
				(mock, context) -> {
					when(mock.validateAndGetCountryStateName(any(), any())).thenReturn(countryMap);
				})) {
			selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_empty();
			when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
			selfRegistrationSaveResponse = service.validateExtendedProfile(mockSessionContainer,
					selfRegistrationSaveRequest);
			assertNotNull(selfRegistrationSaveResponse);
			assertFalse(selfRegistrationSaveResponse.isSuccess());
		}
	}

	@Test
	void that_getSelfRegUser_extendedProfile_return_countrylength_422() throws Exception {

		service = Mockito.spy(service);
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1234);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);

		Map<String, String> countryMap = new HashMap<String, String>();
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS, "No");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL, "");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS, "NA");
		try (MockedConstruction<SelfAdminUtil> mockedSelfAdminUtil = mockConstruction(SelfAdminUtil.class,
				(mock, context) -> {
					when(mock.validateAndGetCountryStateName(any(), any())).thenReturn(countryMap);
				})) {
			selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_countryLength();
			when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
			selfRegistrationSaveResponse = service.validateExtendedProfile(mockSessionContainer,
					selfRegistrationSaveRequest);
			assertNotNull(selfRegistrationSaveResponse);
			assertFalse(selfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser_return_success() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		doReturn(true).when(mockPassword).validatePassword(any(), any(), any(), any(), anyBoolean(), anyBoolean(),
				any(), anyBoolean());
		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();
		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);

			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);

			assertTrue(selfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser_PWDBLK_returnstatus_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		// doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		// doReturn(true).when(mockPassword).validatePassword(any(), any(), any(),
		// any(), anyBoolean(), anyBoolean(), any(), anyBoolean());
		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();
		request.setPassword("PWD");
		request.setValidatePassword("PWD1");

		Message message = new Message();
		Collection<String> errors = new ArrayList<String>();
		errors.add("Password must not be blank.");
		message.setErrMsgItems(errors);
		doThrow(new AtWinXSMsgException(message, "test")).when(mockPasswordComponentLocatorService).locate(any());

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);

			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertFalse(selfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser_PWDBLK2_returnstatus_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		// doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		// doReturn(true).when(mockPassword).validatePassword(any(), any(), any(),
		// any(), anyBoolean(), anyBoolean(), any(), anyBoolean());
		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();
		request.setPassword("PWD");
		request.setValidatePassword("PWD1");

		Message message = new Message();
		Collection<String> errors = new ArrayList<String>();
		errors.add("Password must not be blank.");
		message.setErrMsgItems(errors);
		doThrow(new AtWinXSException("test", "test")).when(mockPasswordComponentLocatorService).locate(any());

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);
			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertFalse(selfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser3_returnstatus_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		// doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		// doReturn(true).when(mockPassword).validatePassword(any(), any(), any(),
		// any(), anyBoolean(), anyBoolean(), any(), anyBoolean());
		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();
		request.setPassword("PWD");
		request.setValidatePassword("PWD1");

		Message message = new Message();
		Collection<String> errors = new ArrayList<String>();
		errors.add("Password must not be blank.");
		message.setErrMsgItems(errors);
		doThrow(new AtWinXSException("test", "test")).when(mockPasswordComponentLocatorService).locate(any());

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);

			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertFalse(selfRegistrationSaveResponse.isSuccess());

		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser4_returnstatus_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		doReturn(true).when(mockPassword).validatePassword(any(), any(), any(), any(), anyBoolean(), anyBoolean(),
				any(), anyBoolean());
		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();
		request.setPassword("PWD");
		request.setValidatePassword("PWD1");

		Message message = new Message();
		Collection<String> errors = new ArrayList<String>();
		errors.add("Password must not be blank.");
		message.setErrMsgItems(errors);

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);

			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertFalse(selfRegistrationSaveResponse.isSuccess());

		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser5_returnstatus_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();
		request.setPassword("ABC@5678901234567");
		request.setValidatePassword("ABC@5678901234567");

		Message message = new Message();
		Collection<String> errors = new ArrayList<String>();
		errors.add("Password must not be blank.");
		message.setErrMsgItems(errors);

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);

			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertFalse(selfRegistrationSaveResponse.isSuccess());

		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser6_returnstatus_200() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		doReturn(true).when(mockPassword).validatePassword(any(), any(), any(), any(), anyBoolean(), anyBoolean(),
				any(), anyBoolean());
		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();

		Message message = new Message();
		Collection<String> errors = new ArrayList<String>();
		errors.add("Password must not be blank.");
		message.setErrMsgItems(errors);

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);
			request.setPassword("ABC@567890123456");
			request.setValidatePassword("ABC@567890123456");
			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertTrue(selfRegistrationSaveResponse.isSuccess());

			doReturn(false).when(mockPassword).validatePassword(any(), any(), any(), any(), anyBoolean(), anyBoolean(),
					any(), anyBoolean());
			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);
			assertFalse(selfRegistrationSaveResponse.isSuccess());

		}
	}

	// CAP-47592
	@Test
	void that_validatePasswordAndPatternAfterUser_return_422() throws Exception {
		service = Mockito.spy(service);

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();

		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);

		try (MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);

			selfRegistrationSaveResponse = service.validatePasswordAndPatternAfterUser(mockSessionContainer, request);

			assertFalse(selfRegistrationSaveResponse.isSuccess());
		}
	}

	@Test
	void that_saveSelfRegistration_with_ep_return_success200() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, BasicProfile=Editable, valid selfReg request
		selfRegistrationSaveRequest = getSRBasicProfileExtendedSaveRequest_Valid();
		selfRegistrationSaveRequest.setC1UserDefinedFields(buildC1UserDefinedFields());
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
//		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);

		doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
				any());

//		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		when(mockEntityObjectMap.getEntity(eq(SelfRegistration.class), any())).thenReturn(mockSelfRegistration);
//		doReturn(mockUser).when(mockEntityObjectMap).getEntity(User.class, null);
		when(mockEntityObjectMap.getEntity(eq(User.class), any())).thenReturn(mockUser);
		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(false);

		Map<String, String> countryMap = new HashMap<String, String>();
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS, "Yes");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL, "USA");
		countryMap.put(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS, "Yes");

		when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1234);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		when(mockTranslationService.getResourceBundle(any(), any())).thenReturn(mockProperties);
		when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
		mockUDFMethods();

		// doReturn(mockPassword).when(mockPasswordComponentLocatorService).locate(any());
		// doReturn(true).when(mockPassword).validatePassword(any(), any(), any(),
		// any(), anyBoolean(), anyBoolean(), any(), anyBoolean());

		try (MockedConstruction<SelfAdminUtil> mockedSelfAdminUtil = mockConstruction(SelfAdminUtil.class,
				(mock, context) -> {
					when(mock.validateAndGetCountryStateName(any(), any())).thenReturn(countryMap);
				});
				MockedStatic<BeanUtils> mockBeanUtils = Mockito.mockStatic(BeanUtils.class);
				MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class);) {

			mockedStatic.when(() -> PropertyUtil.getProperties(SystemFunctionsConstants.PROP_GLOBAL_BUNDLE_NAME))
					.thenReturn(mockXSProperty);// CAP-47671

			// when(mockXSProperty.getProperty("SERVERNAME")).thenReturn("custompoint.rrd.com");
			// doReturn("123").when(service).getContextPath();

			// mockedStatic.when(() ->
			// PropertyUtil.getProperties("system")).thenReturn(mockXSProperty);// CAP-47671
			// when(mockXSProperty.getProperty(LoginConstants.PROP_AUTO_LOGIN_TIMEBOMB)).thenReturn("AUTO_LOGIN_TIMEBOMB");
			// doReturn("123000").when(service).createTimeBomb();

			doReturn("HTTPS://").when(service).loginQueryString(mockAppSessionBean, mockSelfRegistration);
			mockBeanUtils.when(() -> BeanUtils.copyProperties(any(), any()))
					.thenAnswer((Answer<Void>) invocation -> null);
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);
			when(mockProfileDefinition.isCompanyAddressRequired()).thenReturn(true);
			selfRegistrationSaveResponse = service.saveSelfRegistration(mockSessionContainer,
					selfRegistrationSaveRequest);
			assertTrue(selfRegistrationSaveResponse.isSuccess());
		}
	}

	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_Valid() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setEpName2("Test-USer2");
		selfRegistrationSaveRequest.setEpAddr1("Test Addr1");
		selfRegistrationSaveRequest.setEpAddr2("Test Addr2");
		selfRegistrationSaveRequest.setEpAddr3("Test Addr3");
		selfRegistrationSaveRequest.setEpCity("Test City");
		selfRegistrationSaveRequest.setEpCountry("USA");
		selfRegistrationSaveRequest.setEpState("IL");
		selfRegistrationSaveRequest.setEpZip("12133");
		selfRegistrationSaveRequest.setEpMinitial("A");
		selfRegistrationSaveRequest.setEpTitle("Test title");
		selfRegistrationSaveRequest.setEpSuffix("Test Addr1");
		selfRegistrationSaveRequest.setEpFaxnum("12345");
		selfRegistrationSaveRequest.setEpMobilenum("213123");
		selfRegistrationSaveRequest.setEpPagernum("34232434");
		selfRegistrationSaveRequest.setEpTollfreenum("1212333");
		selfRegistrationSaveRequest.setEpWeburl("www.test.com");
		selfRegistrationSaveRequest.setEpPhotourl("www.testphoto.com");
		selfRegistrationSaveRequest.setCheckWarning(false);
		return selfRegistrationSaveRequest;
	}

	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_empty() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setEpName2("");
		selfRegistrationSaveRequest.setEpAddr1("");
		selfRegistrationSaveRequest.setEpAddr2("");
		selfRegistrationSaveRequest.setEpAddr3("");
		selfRegistrationSaveRequest.setEpCity("");
		selfRegistrationSaveRequest.setEpCountry("");
		selfRegistrationSaveRequest.setEpState("");
		selfRegistrationSaveRequest.setEpZip("");
		selfRegistrationSaveRequest.setEpMinitial("");
		selfRegistrationSaveRequest.setEpTitle("");
		selfRegistrationSaveRequest.setEpSuffix("");
		selfRegistrationSaveRequest.setEpFaxnum("");
		selfRegistrationSaveRequest.setEpMobilenum("");
		selfRegistrationSaveRequest.setEpPagernum("");
		selfRegistrationSaveRequest.setEpTollfreenum("");
		selfRegistrationSaveRequest.setEpWeburl("");
		selfRegistrationSaveRequest.setEpPhotourl("");
		return selfRegistrationSaveRequest;
	}

	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_countryLength() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setEpName2("");
		selfRegistrationSaveRequest.setEpAddr1("");
		selfRegistrationSaveRequest.setEpAddr2("");
		selfRegistrationSaveRequest.setEpAddr3("");
		selfRegistrationSaveRequest.setEpCity("");
		selfRegistrationSaveRequest.setEpCountry("zs");
		selfRegistrationSaveRequest.setEpState("as");
		selfRegistrationSaveRequest.setEpZip("");
		selfRegistrationSaveRequest.setEpMinitial("");
		selfRegistrationSaveRequest.setEpTitle("");
		selfRegistrationSaveRequest.setEpSuffix("");
		selfRegistrationSaveRequest.setEpFaxnum("");
		selfRegistrationSaveRequest.setEpMobilenum("");
		selfRegistrationSaveRequest.setEpPagernum("");
		selfRegistrationSaveRequest.setEpTollfreenum("");
		selfRegistrationSaveRequest.setEpWeburl("");
		selfRegistrationSaveRequest.setEpPhotourl("");
		return selfRegistrationSaveRequest;
	}

	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_maxLengh() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setEpName2(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpAddr1(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpAddr2(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpAddr3(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpCity(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpCountry(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpState(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpZip(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpMinitial(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpTitle(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpSuffix(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpFaxnum(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpMobilenum(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpPagernum(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpTollfreenum(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpWeburl(DUMMY_TEXT);
		selfRegistrationSaveRequest.setEpPhotourl(DUMMY_TEXT);
		return selfRegistrationSaveRequest;
	}

	private SelfRegistrationSaveRequest getSRBasicProfileExtendedSaveRequest_Valid() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setUserID("SF-USER4");
		selfRegistrationSaveRequest.setPassword("test1234");
		selfRegistrationSaveRequest.setFirstName("SF2 Subbu First");
		selfRegistrationSaveRequest.setLastName("SF2 Subbu Last");
		selfRegistrationSaveRequest.setEmail("subbu1@rrd.com");
		selfRegistrationSaveRequest.setPhone("1-800-588-2300");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		selfRegistrationSaveRequest.setEpName2("Test-USer2");
		selfRegistrationSaveRequest.setEpAddr1("Test Addr1");
		selfRegistrationSaveRequest.setEpAddr2("Test Addr2");
		selfRegistrationSaveRequest.setEpAddr3("Test Addr3");
		selfRegistrationSaveRequest.setEpCity("Test City");
		selfRegistrationSaveRequest.setEpCountry("USA");
		selfRegistrationSaveRequest.setEpState("IL");
		selfRegistrationSaveRequest.setEpZip("12133");
		selfRegistrationSaveRequest.setEpMinitial("A");
		selfRegistrationSaveRequest.setEpTitle("Test title");
		selfRegistrationSaveRequest.setEpSuffix("Test Addr1");
		selfRegistrationSaveRequest.setEpFaxnum("12345");
		selfRegistrationSaveRequest.setEpMobilenum("213123");
		selfRegistrationSaveRequest.setEpPagernum("34232434");
		selfRegistrationSaveRequest.setEpTollfreenum("1212333");
		selfRegistrationSaveRequest.setEpWeburl("www.test.com");
		selfRegistrationSaveRequest.setEpPhotourl("www.testphoto.com");
		selfRegistrationSaveRequest.setValidatePassword("test1234");
		selfRegistrationSaveRequest.setCheckWarning(true);

		selfRegistrationSaveRequest.setCpName1("TestCPName1");
		selfRegistrationSaveRequest.setCpName2("TestCPName2");
		selfRegistrationSaveRequest.setCpAddressLine1("testAddressLine1");
		selfRegistrationSaveRequest.setCpAddressLine2("TestAddressLine2");
		selfRegistrationSaveRequest.setCpAddressLine2("TestAddressLine3");
		selfRegistrationSaveRequest.setCpCity("TestCity");
		selfRegistrationSaveRequest.setCpStateCd("USA");
		selfRegistrationSaveRequest.setCpZipCd("123");
		selfRegistrationSaveRequest.setCpCountryCd("USA");
		selfRegistrationSaveRequest.setCpTitle("TestTitle");
		selfRegistrationSaveRequest.setCpPhoneNumber("test");
		selfRegistrationSaveRequest.setCpFaxNumber("test");
		selfRegistrationSaveRequest.setCpTitle("testTitle");
		selfRegistrationSaveRequest.setCpDivision("testdivision");
		selfRegistrationSaveRequest.setCpDepartment("testcpdepartment");
		selfRegistrationSaveRequest.setCpWebUrl("testURL");
		selfRegistrationSaveRequest.setCpImageUrl("testImageURL");
		return selfRegistrationSaveRequest;
	}

	// CAP-47629
	@Test
	void that_validateBasicProfile_return_success200() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, valid selfReg request
		// Warning Check Flag false and email in Profile for SiteID/BUID (
		// checkEmailIfExisting)
		selfRegistrationSaveRequest = getSRBasicProfileSaveRequest_Valid1();
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		// doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
		// any());

		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		// doReturn(mockUser).when(mockEntityObjectMap).getEntity(User.class, null);
		// when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);

		// doReturn(mockDAOObjectMap).when(mockObjectMapFactoryService).getDAOObjectMap();
		// doReturn(mockProfileDAO).when(mockDAOObjectMap).getObject(ProfileDAO.class,
		// null);
		// when(mockProfileDAO.checkEmailIfExisting(anyInt(),
		// anyString())).thenReturn(false);

		doReturn(mockIBusinessUnit).when(mockComponentLocator).locateBusinessUnitComponent(null);
		doReturn(mockBusinessUnitVO).when(mockIBusinessUnit).findByBuID(anyInt());
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		patternAfterUsers = service.retrievePatternAfterUsers(mockAppSessionBean);

		selfRegistrationSaveResponse = service.validateBasicProfile(mockSessionContainer, selfRegistrationSaveRequest);
		assertNotNull(selfRegistrationSaveResponse);
	}

	// CAP-47629
	@Test
	void that_validateBasicProfile_InValid_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when valid PatternAfterUser, Invalid selfReg request
		// Basic profile attribute email already Exist in Profile for SiteID/BUID (
		// checkEmailIfExisting)
		// Warning Check Flag true and email existing in Profile for SiteID/BUID (
		// checkEmailIfExisting)
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_InValid4();
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserSuccess();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();

		// doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),
		// any());
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		// when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);

		// doReturn(mockDAOObjectMap).when(mockObjectMapFactoryService).getDAOObjectMap();
		// doReturn(mockProfileDAO).when(mockDAOObjectMap).getObject(ProfileDAO.class,
		// null);
		// when(mockProfileDAO.checkEmailIfExisting(anyInt(),
		// anyString())).thenReturn(true);

		doReturn(mockIBusinessUnit).when(mockComponentLocator).locateBusinessUnitComponent(null);
		doReturn(mockBusinessUnitVO).when(mockIBusinessUnit).findByBuID(anyInt());
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		patternAfterUsers = service.retrievePatternAfterUsers(mockAppSessionBean);

		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(ERROR_MSG422);

		selfRegistrationSaveResponse = service.validateBasicProfile(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-47629
	@Test
	void that_validateBasicProfile_PatternAfterUser_Failed422() throws Exception {

		service = Mockito.spy(service);

		// when Invalid PatternAfterUser in validateBasic Profile APi
		selfRegistrationSaveRequest = getSelfRegistrationSaveRequest_InValid4();
		srSaveResponse_ValidPatternAfterUser = getSelfRegistrationSaveResponse_PatternAfterUserFailed();

		List<LoginVOKey> patternAfterUsers = new ArrayList<>();
		patternAfterUsers.add(new LoginVOKey(TEST_SITE_ID, TEST_LOGIN_ID, TEST_DISPLAY_NAME));

		doReturn(mockIBusinessUnit).when(mockComponentLocator).locateBusinessUnitComponent(null);
		doReturn(mockBusinessUnitVO).when(mockIBusinessUnit).findByBuID(anyInt());
		doReturn(mockILoginInterface).when(mockComponentLocator).locateLoginComponent(null);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		// doReturn(srSaveResponse_ValidPatternAfterUser).when(service).validatePatternAfter(any(SessionContainer.class),any());
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);
		patternAfterUsers = service.retrievePatternAfterUsers(mockAppSessionBean);
		selfRegistrationPatternAfterResponse = service.getPatternAfterUsers(mockSessionContainer);
		// when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		// when(mockProfile.getProfileDefinition()).thenReturn(mockProfileDefinition);
		selfRegistrationSaveResponse = service.validateBasicProfile(mockSessionContainer, selfRegistrationSaveRequest);
		assertFalse(selfRegistrationSaveResponse.isSuccess());
	}

	// CAP-47629
	private SelfRegistrationSaveRequest getSelfRegistrationSaveRequest_InValid4() {

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		// invalid userid with space
		selfRegistrationSaveRequest.setUserID("SF-US ER4");
		selfRegistrationSaveRequest.setPassword("test1234");
		selfRegistrationSaveRequest.setFirstName("SF2 Subbu First");
		selfRegistrationSaveRequest.setLastName("SF2 Subbu Last");
		selfRegistrationSaveRequest.setEmail("subbu1@rrd.com");
		selfRegistrationSaveRequest.setPhone("1-800-588-2300");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		// warning check true
		selfRegistrationSaveRequest.setCheckWarning(true);
		return selfRegistrationSaveRequest;
	}

	// CAP-47629
	private SelfRegistrationSaveRequest getSRBasicProfileSaveRequest_Valid1() {
		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setUserID("SF-USER4");
		selfRegistrationSaveRequest.setPassword("test1234");
		selfRegistrationSaveRequest.setFirstName("SF2 Subbu First");
		selfRegistrationSaveRequest.setLastName("SF2 Subbu Last");
		selfRegistrationSaveRequest.setEmail("subbu1@rrd.com");
		selfRegistrationSaveRequest.setPhone("1-800-588-2300");
		selfRegistrationSaveRequest.setSelectedPatternAfter("IDC-CP-USER1");
		// warning check true
		selfRegistrationSaveRequest.setCheckWarning(true);
		return selfRegistrationSaveRequest;
	}

	// CAP-47615
	@Test
	void that_populateProfileSiteAttributes_has_siteAttributes() throws Exception {

		service = Mockito.spy(service);

		C1UXProfileImpl c1uxProfile = new C1UXProfileImpl();

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.hasSiteAttributes()).thenReturn(true);
		when(mockProfile.getSiteAttributes()).thenReturn(null);

		service.populateProfileSiteAttributes(mockSelfRegistration, c1uxProfile);
		assertTrue(c1uxProfile.getSelfAdminSiteAttributes().isEmpty());
	}

	// CAP-47615
	@Test
	void that_populateProfileSiteAttributes_has_nullSiteAttributes() throws Exception {

		service = Mockito.spy(service);

		C1UXProfileImpl c1uxProfile = new C1UXProfileImpl();

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.hasSiteAttributes()).thenReturn(true);
		when(mockProfile.getSiteAttributes()).thenReturn(mockSiteAttributes);
		when(mockSiteAttributes.getSiteAttrs()).thenReturn(null);

		service.populateProfileSiteAttributes(mockSelfRegistration, c1uxProfile);
		assertTrue(c1uxProfile.getSelfAdminSiteAttributes().isEmpty());
	}

	// CAP-47615
	@Test
	void that_populateProfileSiteAttributes_has_emptySiteAttributes() throws Exception {

		service = Mockito.spy(service);

		C1UXProfileImpl c1uxProfile = new C1UXProfileImpl();

		when(mockSelfRegistration.getProfile()).thenReturn(mockProfile);
		when(mockProfile.hasSiteAttributes()).thenReturn(true);
		when(mockProfile.getSiteAttributes()).thenReturn(mockSiteAttributes);
		when(mockSiteAttributes.getSiteAttrs()).thenReturn(Collections.emptyList());

		service.populateProfileSiteAttributes(mockSelfRegistration, c1uxProfile);
		assertTrue(c1uxProfile.getSelfAdminSiteAttributes().isEmpty());
	}

	// CAP-47615
	@Test
	void that_populateSiteAttributeList_has_nullAttributeValues() throws Exception {

		service = Mockito.spy(service);

		List<SiteAttribute> siteAttrList = new ArrayList<>();
		siteAttrList.add(mockSiteAttribute);

		when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttrList);
		when(mockSiteAttribute.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
		when(mockSiteAttribute.getDisplayType()).thenReturn(DisplayType.ViewOnly);
		when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(null);

		List<SelfAdminSiteAttributes> siteAttributeList = service.populateSiteAttributeList(mockSiteAttributes);
		assertFalse(siteAttributeList.isEmpty());
		assertTrue(siteAttributeList.get(0).getAssignedAttributes().isEmpty());
		assertTrue(siteAttributeList.get(0).getAvailableAttributes().isEmpty());
	}

	// CAP-47615
	@Test
	void that_setSiteAttributes_has_AttributeValues() throws Exception {

		service = Mockito.spy(service);

		SelfAdminSiteAttributes siteAttribute = new SelfAdminSiteAttributes();

		List<SiteAttributeValue> siteAttrValueList = new ArrayList<>();
		siteAttrValueList.add(mockSiteAttributeValue);
		siteAttrValueList.add(mockSiteAttributeValue);

		when(mockSiteAttribute.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
		when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttrValueList);
		when(mockSiteAttributeValue.isAssigned()).thenReturn(true, false, true, false);

		service.setSiteAttributes(mockSiteAttribute, siteAttribute);
		assertEquals(1, siteAttribute.getAssignedAttributes().size());
		assertEquals(1, siteAttribute.getAvailableAttributes().size());
	}

	// CAP-47657
	@Test
	void that_validateUserSiteAttributes_returns_success1() throws Exception {

		service = Mockito.spy(service);
		SelfRegistrationSaveRequest c1SelfRegistrationSaveRequest = getc1UserSiteAttributesRequest_Valid();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		try (MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class,
						(mock, context) -> {
							when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
						})) {

			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);

			// EditableRequired with all validation get pass
			List<SiteAttribute> siteAttributeLst = new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);

			List<SiteAttributeValue> siteAttributeValueLst = new ArrayList<>();
			SiteAttributeValue siteAttributeValue;
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			List<C1UserSiteAttribute> editableSiteAttributesLst = new ArrayList<>();

			doReturn(true).when(mockSelfAdminService).validateSiteAttrIDAndAttrValueID(mockSiteAttributes,
					c1SelfRegistrationSaveRequest.getC1UserSiteAttributes(), editableSiteAttributesLst);
			c1SelfRegistrationSaveResponse = service.validateAttributes(mockSessionContainer,
					c1SelfRegistrationSaveRequest);
			assertNotNull(c1SelfRegistrationSaveResponse);
			assertTrue(c1SelfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47657
	@Test
	void that_validateUserSiteAttr_returnsValidationError_422Fail() throws Exception {

		service = Mockito.spy(service);
		SelfRegistrationSaveRequest c1SelfRegistrationSaveRequest = getc1UserSiteAttributesRequest_Valid();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		try (MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class,
						(mock, context) -> {
							when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
						})) {

			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);

			// EditableRequired with all validation get pass
			List<SiteAttribute> siteAttributeLst = new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);

			List<SiteAttributeValue> siteAttributeValueLst = new ArrayList<>();
			SiteAttributeValue siteAttributeValue;
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			List<C1UserSiteAttribute> editableSiteAttributesLst = new ArrayList<>();

			doReturn(true).when(mockSelfAdminService).validateSiteAttrIDAndAttrValueID(mockSiteAttributes,
					c1SelfRegistrationSaveRequest.getC1UserSiteAttributes(), editableSiteAttributesLst);
			ArrayList<String> errMsgs = new ArrayList<>();
			errMsgs.add(ERROR_MSG422);
			doReturn(errMsgs).when(mockSelfAdminService).validateSiteAttributeMinMax(any(), any(SiteAttributes.class),
					eq(editableSiteAttributesLst));

			c1SelfRegistrationSaveResponse = service.validateAttributes(mockSessionContainer,
					c1SelfRegistrationSaveRequest);
			assertNotNull(c1SelfRegistrationSaveResponse);
			assertFalse(c1SelfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47657
	@Test
	void that_validateUserSiteAttr_returnsAtWinXSException_422Fail() throws Exception {

		service = Mockito.spy(service);
		SelfRegistrationSaveRequest c1SelfRegistrationSaveRequest = getc1UserSiteAttributesRequest_Valid();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		try (MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class,
						(mock, context) -> {
							when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
						})) {

			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);

			// EditableRequired with all validation get pass
			List<SiteAttribute> siteAttributeLst = new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);

			List<SiteAttributeValue> siteAttributeValueLst = new ArrayList<>();
			SiteAttributeValue siteAttributeValue;
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			List<C1UserSiteAttribute> editableSiteAttributesLst = new ArrayList<>();
			doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockSelfAdminService)
					.validateSiteAttrIDAndAttrValueID(mockSiteAttributes,
							c1SelfRegistrationSaveRequest.getC1UserSiteAttributes(), editableSiteAttributesLst);

			c1SelfRegistrationSaveResponse = service.validateAttributes(mockSessionContainer,
					c1SelfRegistrationSaveRequest);
			assertNotNull(c1SelfRegistrationSaveResponse);
			assertFalse(c1SelfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47657
	@Test
	void that_validateUserSiteAttributes_returns_invalid() throws Exception {

		service = Mockito.spy(service);
		SelfRegistrationSaveRequest c1SelfRegistrationSaveRequest = getc1UserSiteAttributesRequest_Invalid1();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		try (MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class,
						(mock, context) -> {
							when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
						})) {

			mockAdminUtil.when(() -> AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);

			List<SiteAttribute> siteAttributeLst = new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);

			List<SiteAttributeValue> siteAttributeValueLst = new ArrayList<>();
			SiteAttributeValue siteAttributeValue;
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(0);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(1);
			siteAttributeValueLst.add(siteAttributeValue);

			List<C1UserSiteAttribute> editableSiteAttributesLst = new ArrayList<>();

			doReturn(false).when(mockSelfAdminService).validateSiteAttrIDAndAttrValueID(mockSiteAttributes,
					c1SelfRegistrationSaveRequest.getC1UserSiteAttributes(), editableSiteAttributesLst);
			c1SelfRegistrationSaveResponse = service.validateAttributes(mockSessionContainer,
					c1SelfRegistrationSaveRequest);
			assertNotNull(c1SelfRegistrationSaveResponse);
			assertFalse(c1SelfRegistrationSaveResponse.isSuccess());
		}
	}

	// CAP-47657
	private SelfRegistrationSaveRequest getc1UserSiteAttributesRequest_Valid() {

		// Site attributes with all Display Types including None, Hidden, ViewOnly
		SelfRegistrationSaveRequest c1SelfRegistrationSaveRequest = new SelfRegistrationSaveRequest();

		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst;
		List<C1UserSiteAttribute> c1UserSiteAttributeLst = new ArrayList<>();

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(258332));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(258333));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(258334));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(1111, c1UserSiteAttributeValueLst));

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(111111));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(222222));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4657, c1UserSiteAttributeValueLst));

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4861, c1UserSiteAttributeValueLst));
		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(257294));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(257295));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4849, c1UserSiteAttributeValueLst));

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(257594));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(257595));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4993, c1UserSiteAttributeValueLst));

		c1SelfRegistrationSaveRequest.setC1UserSiteAttributes(c1UserSiteAttributeLst);
		return c1SelfRegistrationSaveRequest;
	}

	private void mockUDFMethods() throws AtWinXSException {
		when(mockSelfAdminService.validateUserDefinedFields(any(), any(), any(), any())).thenReturn(true);
		when(mockProfile.getUserDefinedFields()).thenReturn(mockUserDefinedFields);
		when(mockUserDefinedFields.getUserDefinedFields()).thenReturn(buildUserDefinedFields());
		when(mockEntityObjectMap.getEntity(eq(UserDefinedField.class), isA(CustomizationToken.class)))
				.thenReturn(mockUserDefinedField);
		doNothing().when(mockUserDefinedField).populate(anyInt(), anyInt(), anyInt(), anyInt());
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.isCanUserEdit()).thenReturn(true);
	}

	private ArrayList<C1UserDefinedField> buildC1UserDefinedFields() {
		ArrayList<C1UserDefinedField> c1UDFs = new ArrayList<>();
		C1UserDefinedField c1UDF = new C1UserDefinedField();
		c1UDF.setUdfFieldNumber(1);
		c1UDF.setUdfValueText("UDF1");
		c1UDFs.add(c1UDF);
		return c1UDFs;
	}

	private List<UserDefinedField> buildUserDefinedFields() {
		List<UserDefinedField> cpUDFs = new ArrayList<>();
		UserDefinedField cpUDF = new UserDefinedFieldImpl();
		cpUDF.setUdfFieldNumber(1);
		cpUDF.setUdfValueText("UDF1");
		cpUDFs.add(cpUDF);
		return cpUDFs;
	}

	// CAP-47657
	private SelfRegistrationSaveRequest getc1UserSiteAttributesRequest_Invalid1() {

		// Site attributes with Editable display type with validation fail
		SelfRegistrationSaveRequest c1SelfRegistrationSaveRequest = new SelfRegistrationSaveRequest();

		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst;
		List<C1UserSiteAttribute> c1UserSiteAttributeLst = new ArrayList<>();

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(254522));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(254523));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4454, c1UserSiteAttributeValueLst));

		c1SelfRegistrationSaveRequest.setC1UserSiteAttributes(c1UserSiteAttributeLst);
		return c1SelfRegistrationSaveRequest;
	}

	// CAP-47672
	@Test
	void that_validateUserDefinedFields_return_success() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockProfile).when(mockSelfRegistration).getProfile();
		doReturn(mockUserDefinedFields).when(mockProfile).getUserDefinedFields();
		doReturn(true).when(mockSelfAdminService).validateUserDefinedFields(any(), any(), any(), any());

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);

		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();

		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		selfRegistrationSaveResponse = service.validateUserDefinedFields(mockSessionContainer, request,
				selfRegistrationSaveResponse);

		assertTrue(selfRegistrationSaveResponse.isSuccess());

	}

	// CAP-47672
	@Test
	void that_validateUserDefinedFields_return_success_2() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockProfile).when(mockSelfRegistration).getProfile();
		doReturn(null).when(mockProfile).getUserDefinedFields();

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();

		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		selfRegistrationSaveResponse = service.validateUserDefinedFields(mockSessionContainer, request,
				selfRegistrationSaveResponse);

		assertTrue(selfRegistrationSaveResponse.isSuccess());

	}

	// CAP-47672
	@Test
	void that_validateUserDefinedFields_return_422_1() throws Exception {
		service = Mockito.spy(service);

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();

		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();

		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		selfRegistrationSaveResponse = service.validateUserDefinedFields(mockSessionContainer, request,
				selfRegistrationSaveResponse);

		assertFalse(selfRegistrationSaveResponse.isSuccess());

	}

	// CAP-47672
	@Test
	void that_validateUserDefinedFields_return_422_3() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		doReturn(mockSelfRegistration).when(mockEntityObjectMap).getEntity(SelfRegistration.class, null);

		doReturn(mockProfile).when(mockSelfRegistration).getProfile();
		doReturn(mockUserDefinedFields).when(mockProfile).getUserDefinedFields();
		doReturn(false).when(mockSelfAdminService).validateUserDefinedFields(any(), any(), any(), any());

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(true);
		SelfRegistrationSaveRequest request = new SelfRegistrationSaveRequest();

		doReturn(selfRegistrationSaveResponse).when(service).validatePatternAfter(mockSessionContainer, null);
		selfRegistrationSaveResponse = service.validateUserDefinedFields(mockSessionContainer, request,
				selfRegistrationSaveResponse);

		assertFalse(selfRegistrationSaveResponse.isSuccess());

	}

	@Test
	void test_createTimeBomb() throws AtWinXSException {
		service = Mockito.spy(service);
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class);) {
			mockedStatic.when(() -> PropertyUtil.getProperties("system")).thenReturn(mockXSProperty);// CAP-47671
			String checkt = service.createTimeBomb();
			assertNotNull(checkt);
		}
	}

	@Test
	void test_loginQueryString() throws AtWinXSException {
		service = Mockito.spy(service);
		try (MockedStatic<PropertyUtil> mockedStatic = Mockito.mockStatic(PropertyUtil.class);) {
			doReturn("123").when(service).getContextPath();
			mockedStatic.when(() -> PropertyUtil.getProperties("system")).thenReturn(mockXSProperty);
			mockedStatic.when(() -> PropertyUtil.getProperties(SystemFunctionsConstants.PROP_GLOBAL_BUNDLE_NAME))
					.thenReturn(mockXSProperty);
			when(mockXSProperty.getProperty("SERVERNAME")).thenReturn("custompoint.rrd.com");
			Properties props = new Properties();
			when(mockAppSessionBean.getLoginQueryString()).thenReturn(props);
			String checkQueryString = service.loginQueryString(mockAppSessionBean, mockSelfRegistration);
			assertNull(checkQueryString);
		}
	}

	// CAP-47775
	@Test
	void that_validateDefaultAddressField_has_maxSizeError() throws Exception {

		SelfRegistrationSaveResponse testResponse = new SelfRegistrationSaveResponse();

		service.validateDefaultAddressField(testResponse, mockAppSessionBean, mockTranslationMap, COUNTRY_CODE_USA,
				SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD, mockProfileDefinition);
		assertFalse(Util.isBlankOrNull(testResponse.getFieldMessage(SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD)));
	}

	// CAP-47775
	@Test
	void that_validateDefaultAddressField_has_noExtAndCorpProfile() throws Exception {

		SelfRegistrationSaveResponse testResponse = new SelfRegistrationSaveResponse();

		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(false);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(false);

		service.validateDefaultAddressField(testResponse, mockAppSessionBean, mockTranslationMap,
				TEST_INVALID_DEFAULT_ADDRESS_CD, SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD, mockProfileDefinition);
		assertFalse(Util.isBlankOrNull(testResponse.getFieldMessage(SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD)));
	}

	// CAP-47775
	@Test
	void that_validateDefaultAddressField_has_invalidError() throws Exception {

		SelfRegistrationSaveResponse testResponse = new SelfRegistrationSaveResponse();

		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);

		service.validateDefaultAddressField(testResponse, mockAppSessionBean, mockTranslationMap,
				TEST_INVALID_DEFAULT_ADDRESS_CD, SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD, mockProfileDefinition);
		assertFalse(Util.isBlankOrNull(testResponse.getFieldMessage(SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD)));
	}

	// CAP-47775
	@Test
	void that_validateDefaultAddressField_has_correctDefaultCorpCode() throws Exception {

		SelfRegistrationSaveResponse testResponse = new SelfRegistrationSaveResponse();

		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);

		service.validateDefaultAddressField(testResponse, mockAppSessionBean, mockTranslationMap,
				SelfRegistrationServiceImpl.DEFAULT_CORPORATE_CD, SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD,
				mockProfileDefinition);
		assertTrue(Util.isBlankOrNull(testResponse.getFieldMessage(SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD)));
	}

	// CAP-47775
	@Test
	void that_validateDefaultAddressField_has_correctDefaultExtCode() throws Exception {

		SelfRegistrationSaveResponse testResponse = new SelfRegistrationSaveResponse();

		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);

		service.validateDefaultAddressField(testResponse, mockAppSessionBean, mockTranslationMap,
				SelfRegistrationServiceImpl.DEFAULT_EXTENDED_CD, SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD,
				mockProfileDefinition);
		assertTrue(Util.isBlankOrNull(testResponse.getFieldMessage(SelfRegistrationServiceImpl.DEFAULT_ADDRESS_CD)));
	}

	// CAP-47775
	@Test
	void that_setDefaultAddressSourceCode_has_noExtAndCorpProfile() throws Exception {

		SelfRegistration testSelfReg = new SelfRegistrationImpl();
		testSelfReg.setProfile(new ProfileImpl());

		when(mockSelfRegistrationSaveRequest.getDefaultAddressSourceCd())
				.thenReturn(SelfRegistrationServiceImpl.DEFAULT_EXTENDED_CD);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(false);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(false);

		service.setDefaultAddressSourceCode(mockSelfRegistrationSaveRequest, testSelfReg, mockProfileDefinition);
		assertTrue(Util.isBlankOrNull(testSelfReg.getProfile().getDefaultAddressSourceCd()));
	}

	// CAP-47775
	@Test
	void that_setDefaultAddressSourceCode_can_notEditExtAndCorpProfile() throws Exception {

		SelfRegistration testSelfReg = new SelfRegistrationImpl();
		testSelfReg.setProfile(new ProfileImpl());

		when(mockSelfRegistrationSaveRequest.getDefaultAddressSourceCd())
				.thenReturn(SelfRegistrationServiceImpl.DEFAULT_EXTENDED_CD);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(false);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(false);

		service.setDefaultAddressSourceCode(mockSelfRegistrationSaveRequest, testSelfReg, mockProfileDefinition);
		assertTrue(Util.isBlankOrNull(testSelfReg.getProfile().getDefaultAddressSourceCd()));
		// assertEquals(SelfRegistrationServiceImpl.DEFAULT_EXTENDED_CD,
		// testSelfReg.getProfile().getDefaultAddressSourceCd());
	}

	// CAP-47775
	@Test
	void that_setDefaultAddressSourceCode_can_editExtProfile() throws Exception {

		SelfRegistration testSelfReg = new SelfRegistrationImpl();
		testSelfReg.setProfile(new ProfileImpl());

		when(mockSelfRegistrationSaveRequest.getDefaultAddressSourceCd())
				.thenReturn(SelfRegistrationServiceImpl.DEFAULT_EXTENDED_CD);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		service.setDefaultAddressSourceCode(mockSelfRegistrationSaveRequest, testSelfReg, mockProfileDefinition);
		assertEquals(SelfRegistrationServiceImpl.DEFAULT_EXTENDED_CD,
				testSelfReg.getProfile().getDefaultAddressSourceCd());
	}

	// CAP-47775
	@Test
	void that_setDefaultAddressSourceCode_can_editCorpProfile() throws Exception {

		SelfRegistration testSelfReg = new SelfRegistrationImpl();
		testSelfReg.setProfile(new ProfileImpl());

		when(mockSelfRegistrationSaveRequest.getDefaultAddressSourceCd())
				.thenReturn(SelfRegistrationServiceImpl.DEFAULT_CORPORATE_CD);
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(false);
		when(mockProfileDefinition.isUseCorporateProfile()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);

		service.setDefaultAddressSourceCode(mockSelfRegistrationSaveRequest, testSelfReg, mockProfileDefinition);
		assertEquals(SelfRegistrationServiceImpl.DEFAULT_CORPORATE_CD,
				testSelfReg.getProfile().getDefaultAddressSourceCd());
	}

	@ParameterizedTest
	@MethodSource("getCountryFieldArgument")
	void validateCountryFieldTest(boolean isRequired, String country, boolean isCountryValid, int maxSize) {
		String fieldName = new StringBuilder("cp")
				.append(StringUtils.capitalizeFirstLetter(SelfAdminUtil.SELF_ADMIN_COUNTRY_CODE)).toString();

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(false);

		service.validateCountryField(selfRegistrationSaveResponse, mockAppSessionBean, isRequired, country,
				isCountryValid, mockTranslationMap, maxSize);

		assertTrue(selfRegistrationSaveResponse.getFieldMessages().containsKey(fieldName));
	}

	private static Stream<Arguments> getCountryFieldArgument() {
		return Stream.of(Arguments.of(true, "", true, 3), Arguments.of(true, "TestInvalidCountry", true, 3),
				Arguments.of(true, "USA", false, 3));
	}

	private static Stream<Arguments> getStateFieldArgument() {
		return Stream.of(Arguments.of(false, "TestInvalidState", false, 4),
				Arguments.of(true, "TestInvalidState", true, 4));
	}

	@ParameterizedTest
	@MethodSource("getStateFieldArgument")
	void validateStateFieldTest(boolean isRequired, String country, boolean hasState, int maxSize) {
		service = Mockito.spy(service);
		String STATEINUSA_NAME_AL = "Alabama";

		String fieldName = new StringBuilder("cp")
				.append(StringUtils.capitalizeFirstLetter(SelfAdminUtil.SELF_ADMIN_STATE_CODE)).toString();

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_NAME_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		selfRegistrationSaveRequest = new SelfRegistrationSaveRequest();
		selfRegistrationSaveRequest.setCpStateCd(country);

		mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountry(COUNTRY_CODE_USA)).thenReturn(mockCountryBean);
		when(mockCountryBean.getCountryHasStates()).thenReturn(hasState);

		selfRegistrationSaveResponse = new SelfRegistrationSaveResponse();
		selfRegistrationSaveResponse.setSuccess(false);

		service.validateStateField(selfRegistrationSaveResponse, mockAppSessionBean, isRequired, mockCountryBean,
				selfRegistrationSaveRequest, maxSize);

		assertTrue(selfRegistrationSaveResponse.getFieldMessages().containsKey(fieldName));
	}

}
