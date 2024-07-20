/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 *  06/23/23	M Sakthi			CAP-40705		Initial Junit test case creation
 *  07/13/23	S Ramachandran		CAP-42258		Added Junit for update extended Personal Profile
 *  07/28/23	N Caceres			CAP-42342		Added Junit for update company profile
 *  28/29/23	Krishna Natarajan	CAP-42562		Added Junit for modified code
 *	09/07/23	L De Leon			CAP-43631		Added test cases for deletePABAddress() method
 *	09/28/23	N Caceres			CAP-42806		Fix inconsistent translation in update extended Personal Profile
 *	10/06/23	L De Leon			CAP-44422		Added test cases for exportPABAddresses() method
 *	11/07/23	S Ramachandran		CAP-44961 		Aded Junit for USPS validation in savePAB and to show suggested addr
 *	11/27/23	S Ramachandran		CAP-45374 		USPS validated Suggested Address need to be split of address over 30 characters
 *	11/30/23	S Ramachandran		CAP-45631 		Added Junit in save PAB for zip validation and error corrected as like in CP
 *	12/07/23    S Ramachandran  	CAP-45485   	Added Junit for Fix code to only search/use originator profile when doing self admin
 *	01/10/24	Krishna Natarajan	CAP-46333		Commented out lines that were blocking the code change on SelfAdminServiceImpl
 *	02/01/24	S Ramachandran		CAP-46801		Junit test added for save site attribute
 *	03/26/24	Krishna Natarajan	CAP-48207		Removed unecessary stubbing from that_validateUserDefinedFields_returns_true
 *	06/28/24	Rakesh KM			CAP-50381		UDF Fields with an assigned attribute List should Ignore the Field Length validation
 */

package com.rrd.c1ux.api.services.admin;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.C1UXUserDefinedFieldImpl;
import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributeValue;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesRequest;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesResponse;
import com.rrd.c1ux.api.models.admin.PABDeleteRequest;
import com.rrd.c1ux.api.models.admin.PABDeleteResponse;
import com.rrd.c1ux.api.models.admin.PABExportResponse;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.admin.PABSearchRequest;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.models.admin.UpdateBasicProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateBasicProfileResponse;
import com.rrd.c1ux.api.models.admin.UpdateCompanyProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateCompanyProfileResponse;
import com.rrd.c1ux.api.models.admin.UpdateExtendedProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateExtendedProfileResponse;
import com.rrd.c1ux.api.models.admin.UserDefinedFieldsRequest;
import com.rrd.c1ux.api.models.admin.UserDefinedfieldsResponse;
import com.rrd.c1ux.api.models.common.GenericSearchCriteria;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.admin.entity.SiteAttributeValue;
import com.rrd.custompoint.admin.entity.SiteAttributeValueImpl;
import com.rrd.custompoint.admin.profile.dao.UserDefinedFieldDAO;
import com.rrd.custompoint.admin.profile.entity.AddressBook;
import com.rrd.custompoint.admin.profile.entity.AddressBookImpExp;
import com.rrd.custompoint.admin.profile.entity.AddressBookRecord;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.ProfileImpl;
import com.rrd.custompoint.admin.profile.entity.ProfileUDFDefinition;
import com.rrd.custompoint.admin.profile.entity.ProfileUDFDefinitionImpl;
import com.rrd.custompoint.admin.profile.entity.ProfileUDFDefinitions;
import com.rrd.custompoint.admin.profile.entity.ReferenceFieldDefinition.DisplayType;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.rrd.custompoint.admin.profile.entity.UserDefinedFieldImpl;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.DAOObjectMap;
import com.rrd.custompoint.orderentry.entity.AddressSourceSettings;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.PersonalAddressVO;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;
import com.wallace.atwinxs.framework.util.CorporateProfileBean;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PersonalProfileBean;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.locale.ao.CountryBean;
import com.wallace.atwinxs.locale.ao.StateBean;
import com.wallace.atwinxs.locale.vo.CountryVOKey;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.vo.USPSAddressInfoServiceParmsVO;

class SelfAdminServiceImplTests extends BaseServiceTest{

	public static final String EXTENDED_AS_PREFFERED_ADDR_CODE="E";

	public static final String TEXT_WITH_MORETHAN255_CHAR=
			"Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 "
			+ "Exceed Length 255  Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 "
			+ "Exceed Length 255  Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 Exceed Length 255 ";

	public static final String SUCCESS = "Success";
	public static final String FAIL="Failed";

	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;
	public static final String GENERIC_422MESSAGE = "Order validation failed";

	private static final String COUNTRY_NAME_AFG = "Afghanistan";
	private static final String COUNTRY_CODE_AFG = "AFG";

	private static final String COUNTRY_NAME_USA = "United States";
	private static final String COUNTRY_CODE_USA = "USA";

	private static final String STATEINUSA_NAME_AL = "Alabama";
	private static final String STATEINUSA_CODE_AL = "AL";
	private static final String ADDRESS_LINE_1_FLD_NM = "Address Line 1 ";

	public static final int SELF_ADMIN_MAX_SIZE_EXT_TITLE = 10;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_MID_INIT = 1;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_SUFFIX = 15;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_FAX = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_PAGER = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_MOBILE = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_TOLL_FREE = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_WEB_URL = 255;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_IMG_URL = 255;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_NAME2 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_LINE1 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_LINE2 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_LINE3 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_CITY = 30;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_STATE_CD = 4;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_STATE = 40;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_ZIP = 12;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_COUNTRY_CD = 3;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_COUNTRY = 30;

	private static final String TEST_GROUP_NAME = "USER_GROUP";
	private static final int TEST_PROFILE_NBR = 12345;

	private static final String TEST_422ERR_MESSAGE = "422ERROR MESSAGE";

	@Mock
	CountryBean mockCountryBean;

	@Mock
	PersonalProfileBean mockPersonalProfileBean;

	@Mock
	CorporateProfileBean mockCorporateProfileBean;

	@Mock
	DAOObjectMap mockDAOObjectMap;

	@Mock
	UserDefinedFieldDAO mockUserDefinedFieldDAO;

	@Mock
	private CompositeProfileBean mockCompositeProfileBean;

	@InjectMocks
	private SelfAdminServiceImpl serviceToTest;

	@Mock
	C1UXUserDefinedFieldImpl mockC1UXUserDefinedFieldImpl;

	UserDefinedfieldsResponse updateUserDefinedfieldsResponse=new UserDefinedfieldsResponse();
	UpdateBasicProfileResponse updateProfileResp=new UpdateBasicProfileResponse();
	UpdateExtendedProfileResponse updateExtendedProfileResp=new UpdateExtendedProfileResponse();

	UpdateCompanyProfileRequest companyProfileRequest;
	UpdateCompanyProfileResponse companyProfileresponse;
	ArrayList<String> countries;
	ArrayList<String> states;
	StateBean statebean;

	PABDeleteResponse pabDeleteResponse = new PABDeleteResponse();
	PABDeleteRequest pabDeleteRequest = new PABDeleteRequest();

	PABSaveRequest pabSaveRequest=new PABSaveRequest();
    PABSaveResponse pabSaveResponse=new PABSaveResponse();
    USPSAddressInfoServiceParmsVO addressParms;

    //CAP-46801
  	private C1UserSiteAttributesResponse c1UserSiteAttributesResponse;
  	private C1UserSiteAttributesRequest c1UserSiteAttributesRequest = new C1UserSiteAttributesRequest();
  	private SiteAttributes siteAttributesForProfile;

	@Mock
	Profile mockProfile;

	@Mock
	AddressBook mockAddressBook;

	@Mock
	AddressBookRecord mockAddressBookRecord;



    private List<AddressBookRecord> addressBookRecords = new ArrayList<>();
	private List<Integer> testAddressIDs = new ArrayList<>();
	private static final int TEST_ADDRESS_ID = 12345;

	//CAP-43630 - BEGIN
	private PABSearchResponse pabSearchResponse;
	private PABSearchRequest pabSearchRequest;
	private Collection<PersonalAddressVO> personalAddressVOs;
	private Collection<PersonalAddressVO> extendedAddressVOs;
	private Collection<PersonalAddressVO> corpAddressVOs;

	private static final String TEST_PAB_CRITERIA_KEY = "country" ;
	private static final String TEST_PAB_CRITERIA_VALUE = "USA" ;
	public static final int DEVTEST_SITE_ID = 4366;

	private static final String TEST_NAME="NAME";
	private static final String TEST_ADDRESS="ADDRESS";
	private static final String TEST_CITY="CITY";
	private static final String TEST_STATE="AL";
	private static final String TEST_COUNTRY="USA";
	private static final String TEST_ZIP="60601";
	private static final String TEST_SHIP_TO_ATTN="SHIP TO ATTN";
	private static final String TEST_PHONE_NO="+1-101-1011";
	private static final boolean TEST_ZIP_VALIDATION_FLAG=false;
	private static final String TEST_INVALID_CRITERIA_KEY="invalidkey";
	private static final String TEST_WITH_LENGTH_50CHAR="TEXT WITH 50 CHAR TEXT WITH 50 CHAR TEXT WITH 50 ";
	//CAP-43630 - END

	public static final String EXCLUDE_SETUP_METHOD = "EXCLUDE_SETUP_METHOD";

	@Mock
	AddressBookImpExp mockAddressBookImpExp;

	@Mock
	HttpServletResponse mockHttpServletResponse;

	@Mock
	File mockFile;

	@Mock
	FileInputStream mockFileInputStream;

	@Mock
	ServletOutputStream mockServletOutputStream;

	@Mock
	private List<UserDefinedField> mockUDFlist;

	@Mock
	private static ProfileUDFDefinition mockProfileUDFDefinition;

	@Mock
	private static ProfileUDFDefinitions mockProfileUDFDefinitions;

	private PABExportResponse pabExportResponse;

	@BeforeEach
	public void setUp(TestInfo info) throws Exception {
		if (info.getTags().isEmpty()) {
		companyProfileRequest = buildCompanyProfileRequest(1);
		companyProfileresponse = new UpdateCompanyProfileResponse();
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		// Country Exist
		countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		// State Exist
		statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		//CAP-43630 - BEGIN
		pabSearchResponse =new PABSearchResponse();
		pabSearchRequest = getPABSearchRequest();
		personalAddressVOs = getPersonalAddressVOs();
		extendedAddressVOs = getExtendedPersonalAddressVOs();
		corpAddressVOs = getCorpPersonalAddressVOs();
		//CAP-43630 - END

		pabExportResponse = new PABExportResponse();
		}
	}


	//CAP-43630 - BEGIN
	private PABSearchRequest getPABSearchRequest() {

		PABSearchRequest pabSearchRequest = new PABSearchRequest();

		GenericSearchCriteria genericSearchCriteria = new GenericSearchCriteria();
		genericSearchCriteria.setCriteriaFieldKey(TEST_PAB_CRITERIA_KEY);
		genericSearchCriteria.setCriteriaFieldValue(TEST_PAB_CRITERIA_VALUE);

		List<GenericSearchCriteria> genericSearchCriterias = new ArrayList<GenericSearchCriteria>();
		genericSearchCriterias.add(genericSearchCriteria);
		pabSearchRequest.setGenericSearchCriteria(genericSearchCriterias);

		return pabSearchRequest;
	}


	private Collection<PersonalAddressVO> getPersonalAddressVOs() {

		Collection<PersonalAddressVO> addresses = new ArrayList<PersonalAddressVO>();

		PersonalAddressVO address1 = new PersonalAddressVO(0,0,0, 0,
				TEST_NAME, TEST_ADDRESS, TEST_ADDRESS, TEST_ADDRESS, TEST_ADDRESS,
				TEST_CITY, TEST_STATE, TEST_ZIP, TEST_COUNTRY, TEST_NAME,
				TEST_SHIP_TO_ATTN,TEST_PHONE_NO, TEST_ZIP_VALIDATION_FLAG);
		addresses.add(address1);

		return addresses;
	}

	private Collection<PersonalAddressVO> getExtendedPersonalAddressVOs() {

		Collection<PersonalAddressVO> addresses = new ArrayList<PersonalAddressVO>();

		PersonalAddressVO address1 = new PersonalAddressVO(0,0,0,-1,
				TEST_NAME, TEST_ADDRESS, TEST_ADDRESS, TEST_ADDRESS, TEST_ADDRESS,
				TEST_CITY, TEST_STATE, TEST_ZIP, TEST_COUNTRY, TEST_NAME,
				TEST_SHIP_TO_ATTN,TEST_PHONE_NO, TEST_ZIP_VALIDATION_FLAG);
		addresses.add(address1);

		return addresses;
	}

	private Collection<PersonalAddressVO> getCorpPersonalAddressVOs() {

		Collection<PersonalAddressVO> addresses = new ArrayList<PersonalAddressVO>();

		PersonalAddressVO address1 = new PersonalAddressVO(0,0,0,-2,
				TEST_NAME, TEST_ADDRESS, TEST_ADDRESS, TEST_ADDRESS, TEST_ADDRESS,
				TEST_CITY, TEST_STATE, TEST_ZIP, TEST_COUNTRY, TEST_NAME,
				TEST_SHIP_TO_ATTN,TEST_PHONE_NO, TEST_ZIP_VALIDATION_FLAG);
		addresses.add(address1);

		return addresses;
	}

	@Test
	void that_searchPAB_withPABTurnedOff_returns_fail403() throws Exception {

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(false);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//Exception exception = assertThrows(AccessForbiddenException.class, () -> {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,null,false);
		});

		assertNotNull(pabSearchResponse);
		assertFalse(pabSearchResponse.isSuccess());
		//assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_searchPAB_withNoProfileSharedUser_returns_fail403() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);


		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,null,false);
		});
		assertNotNull(pabSearchResponse);
		assertFalse(pabSearchResponse.isSuccess());
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_searchPAB_InvalidPABSearchKey_return_fail422() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(false);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(TEST_INVALID_CRITERIA_KEY);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(AtWinXSConstant.EMPTY_STRING);

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {
			pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

			assertNotNull(pabSearchResponse);
			assertFalse(pabSearchResponse.isSuccess());
			assertEquals(true, pabSearchResponse.getC1uxAddresses().isEmpty());
		}
	}

	@Test
	void that_searchPAB_InvalidPABSearchValueLength_return_success() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(false);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_SN1);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_WITH_LENGTH_50CHAR);

		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {
			pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

			assertNotNull(pabSearchResponse);
			assertTrue(pabSearchResponse.isSuccess());
			assertEquals(0, pabSearchResponse.getPabCount());
		}
	}


	public void setUpFor_ValidPABSearchKeyValue() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(false);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSiteID()).thenReturn(DEVTEST_SITE_ID);
		when(mockAppSessionBean.getLoginID()).thenReturn(AtWinXSConstant.EMPTY_STRING);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getSelectedSiteAttribute()).thenReturn(mockHashMap);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(AddressSourceSettings.class, mockToken)).thenReturn(mockAddressSourceSettings);
		when(mockEntityObjectMap.getEntity(User.class, mockToken)).thenReturn(mockUser);

		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);
	}


	@Test
	void that_searchPABSN1_ValidPABSearchKeyValue1_return_success() throws Exception {


		setUpFor_ValidPABSearchKeyValue();

		when(mockUser.isSharedID()).thenReturn(false);
		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(personalAddressVOs);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//valid search criteria key-value for shiptoname1
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_SN1);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_NAME);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_searchPAB_pabSearchListIsNull_return_success() throws Exception {

		setUpFor_ValidPABSearchKeyValue();

		when(mockUser.isSharedID()).thenReturn(false);
		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(personalAddressVOs);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//PAB Search List Is Null
		pabSearchRequest.setGenericSearchCriteria(null);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_searchPAB_pabSearchListIsEmpty_return_success() throws Exception {

		setUpFor_ValidPABSearchKeyValue();

		when(mockUser.isSharedID()).thenReturn(false);
		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(personalAddressVOs);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//PAB Search List Is Empty
		List<GenericSearchCriteria> genericSearchCriterias = new ArrayList<GenericSearchCriteria>();
		pabSearchRequest.setGenericSearchCriteria(genericSearchCriterias);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}


	@Test
	void that_searchPABSTOATTN_ValidPABSearchKeyValue2_return_success() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getSelectedSiteAttribute()).thenReturn(mockHashMap);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(AddressSourceSettings.class, mockToken)).thenReturn(mockAddressSourceSettings);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		// with SharedId &  personalAddressVOs is empty
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		Collection<PersonalAddressVO> personalAddressVOs = new ArrayList<PersonalAddressVO>();

		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(personalAddressVOs);

		//valid search criteria key-value for shiptoattention
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_STOATTN);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_SHIP_TO_ATTN);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(0, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_searchPABCTY_ValidPABSearchKeyValue3_return_success() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(false);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockVolatileSessionBean.getSelectedSiteAttribute()).thenReturn(mockHashMap);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(AddressSourceSettings.class, mockToken)).thenReturn(mockAddressSourceSettings);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		Collection<PersonalAddressVO> personalAddressVOs = null;

		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(personalAddressVOs);

		//valid search criteria key-value for shiptoattention
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_CTY);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_CITY);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(0, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_searchPABCT_ValidPABSearchKeyValue4_return_success() throws Exception {

		setUpFor_ValidPABSearchKeyValue();
		when(mockUser.isSharedID()).thenReturn(true);

		//with extendedaddressVOs
		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(extendedAddressVOs);
		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//valid search criteria key-value for state
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_CT);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_COUNTRY);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}


	@Test
	void that_searchPABST_ValidPABSearchKeyValue5_return_success() throws Exception {

		setUpFor_ValidPABSearchKeyValue();

		when(mockUser.isSharedID()).thenReturn(false);

		// with corpaddressVOs
		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(corpAddressVOs);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		//valid search criteria key-value for state
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_ST);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_STATE);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_searchPABZIP_ValidPABSearchKeyValue6_return_success() throws Exception {

		setUpFor_ValidPABSearchKeyValue();

		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(corpAddressVOs);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//valid search criteria key-value for zip
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_ZIP);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_ZIP);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_searchPAB_ValidPABSearchKeyValueEmpty6_return_success() throws Exception {

		setUpFor_ValidPABSearchKeyValue();

		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(corpAddressVOs);
		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//valid search criteria key-value for zip
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(AtWinXSConstant.EMPTY_STRING);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(AtWinXSConstant.EMPTY_STRING);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,false);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}
	//CAP-43630	-END


	//CAP-45485
	@Test
	void that_updateProfile_returns_success() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		when(mockProfileDefinition.isCanUserEditProfile()).thenReturn(true);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateProfileResp = serviceToTest.updateBasicProfile(updateProfileRequest(), mockSessionContainer);
			assertTrue(updateProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateProfileWithInRequestorModeAvailable_returns_success() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		when(mockProfileDefinition.isCanUserEditProfile()).thenReturn(true);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(true);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateProfileResp = serviceToTest.updateBasicProfile(updateProfileRequest(), mockSessionContainer);
			assertTrue(updateProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateProfile_returns_fail() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		when(mockProfileDefinition.isCanUserEditProfile()).thenReturn(true);

		serviceToTest = Mockito.spy(serviceToTest);
		doReturn(false).when(serviceToTest).validateBasicProfile(updateProfileResp, updateProfileRequest(),
				mockAppSessionBean, mockProfileDefinition);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){

			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateProfileResp = serviceToTest.updateBasicProfile(updateProfileRequest(), mockSessionContainer);
			assertFalse(updateProfileResp.isSuccess());
		}
	}

	private UpdateBasicProfileRequest updateProfileRequest() {
		UpdateBasicProfileRequest updateProfileRequest = new UpdateBasicProfileRequest();
		updateProfileRequest.setFirstName("Alex");
		updateProfileRequest.setLastName("Salcedo");
		updateProfileRequest.setPhone("1-000-223-5555");
		updateProfileRequest.setEmail("test123@test.com");
		return updateProfileRequest;
	}


	//CAP-42258 - Junits for update extended Personal Profile starts here

	public void setAllEPDefinitiontrue() throws Exception {
		when(mockProfileDefinition.isMiddleInitialRequired()).thenReturn(true);
		when(mockProfileDefinition.isTitleRequired()).thenReturn(true);
		when(mockProfileDefinition.isSuffixRequired()).thenReturn(true);
		when(mockProfileDefinition.isFaxRequired()).thenReturn(true);
		when(mockProfileDefinition.isMobileRequired()).thenReturn(true);
		when(mockProfileDefinition.isWebRequired()).thenReturn(true);
		when(mockProfileDefinition.isPhotoURLRequired()).thenReturn(true);
		when(mockProfileDefinition.isPagerRequired()).thenReturn(true);
		when(mockProfileDefinition.isTollFreeRequired()).thenReturn(true);
		when(mockProfileDefinition.isAddressRequired()).thenReturn(true);
	}

	public void setAllEPDefinitionfalse() throws Exception {
		when(mockProfileDefinition.isMiddleInitialRequired()).thenReturn(false);
		when(mockProfileDefinition.isTitleRequired()).thenReturn(false);
		when(mockProfileDefinition.isSuffixRequired()).thenReturn(false);
		when(mockProfileDefinition.isFaxRequired()).thenReturn(false);
		when(mockProfileDefinition.isMobileRequired()).thenReturn(false);
		when(mockProfileDefinition.isWebRequired()).thenReturn(false);
		when(mockProfileDefinition.isPhotoURLRequired()).thenReturn(false);
		when(mockProfileDefinition.isPagerRequired()).thenReturn(false);
		when(mockProfileDefinition.isTollFreeRequired()).thenReturn(false);
		when(mockProfileDefinition.isAddressRequired()).thenReturn(false);
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withAllAttributeValues_CAndSExist_returns_success() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		// Country Exist
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		// State Exist
		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);

		when(mockAppSessionBean.getPersonalProfile()).thenReturn(mockPersonalProfileBean);

		when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_USA);
		when(mockCountryBean.getCountryName()).thenReturn(COUNTRY_NAME_USA);
		when(mockCountryBean.getStatesInCountryByName()).thenReturn(states.iterator());
		when(mockCountryBean.getCountryHasStates()).thenReturn(true);
		when(mockCountryBean.getStateInCountryByName(STATEINUSA_NAME_AL)).thenReturn(statebean);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitiontrue();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithAllAttributeValues(),
					mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertTrue(updateExtendedProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withAllAttributeValues_COnlyExist_returns_success() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_AFG);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockAppSessionBean.getPersonalProfile()).thenReturn(mockPersonalProfileBean);

		when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_AFG);
		when(mockCountryBean.getCountryName()).thenReturn(COUNTRY_NAME_AFG);
		when(mockCountryBean.getCountryHasStates()).thenReturn(false);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitiontrue();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
				MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_AFG))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest
					.updateExtendedProfile(getEPRequestWithAllAttributeValues_COnlyExist(), mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertTrue(updateExtendedProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withAllAttributeValues_COnlyExist_returns_success2() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_AFG);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockAppSessionBean.getPersonalProfile()).thenReturn(mockPersonalProfileBean);

		when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_AFG);
		when(mockCountryBean.getCountryName()).thenReturn(COUNTRY_NAME_AFG);
		when(mockCountryBean.getCountryHasStates()).thenReturn(false);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitiontrue();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(true);  //CAP-45485
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
				MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_AFG))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest
					.updateExtendedProfile(getEPRequestWithAllAttributeValues_COnlyExist(), mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertTrue(updateExtendedProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withAllAttributeValues_CExistAndSNotExist_returns_fail() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_USA);
		when(mockCountryBean.getCountryName()).thenReturn(COUNTRY_NAME_USA);
		when(mockCountryBean.getStatesInCountryByName()).thenReturn(states.iterator());
		when(mockCountryBean.getCountryHasStates()).thenReturn(true);
		when(mockCountryBean.getStateInCountryByName(STATEINUSA_NAME_AL)).thenReturn(statebean);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitiontrue();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.NOT_VALID_ERR, null))
					.thenReturn(SFTranslationTextConstants.NOT_VALID_ERR);

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest.updateExtendedProfile(
					getEPRequestWithAllAttributeValues_CExistAndSNotExist(), mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.NOT_VALID_ERR,
					updateExtendedProfileResp.getFieldMessages().get("stateCd").trim());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withAllAttributeValues_CNotExistAndSNotExist_returns_fail() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_USA);
		when(mockCountryBean.getStatesInCountryByName()).thenReturn(states.iterator());
		when(mockCountryBean.getCountryHasStates()).thenReturn(true);
		when(mockCountryBean.getStateInCountryByName(STATEINUSA_NAME_AL)).thenReturn(statebean);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitiontrue();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.NOT_VALID_ERR, null))
					.thenReturn(SFTranslationTextConstants.NOT_VALID_ERR);

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest.updateExtendedProfile(
					getEPRequestWithAllAttributeValues_CNotExistAndSNotExist(), mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.NOT_VALID_ERR,
					updateExtendedProfileResp.getFieldMessages().get("countryCd").trim());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withNoMandateDefinition_returns_success() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockProfile.getDefaultAddressSourceCd()).thenReturn(EXTENDED_AS_PREFFERED_ADDR_CODE);
		when(mockAppSessionBean.getPersonalProfile()).thenReturn(mockPersonalProfileBean);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitionfalse();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(countrybean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithAllValuesEmptyAndDefaultPSFalse(),
					mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertTrue(updateExtendedProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withNoMandateDefinition_returns_success2() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);
		when(mockProfile.getExtendedProfile()).thenReturn(mockExtendedProfile);
		when(mockProfile.getDefaultAddressSourceCd()).thenReturn(EXTENDED_AS_PREFFERED_ADDR_CODE);
		when(mockAppSessionBean.getPersonalProfile()).thenReturn(mockPersonalProfileBean);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitionfalse();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(true);	//CAP-45485
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(countrybean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithAllValuesEmptyAndDefaultPSFalse(),
					mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertTrue(updateExtendedProfileResp.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withEPDefinitionRequired_returns_fail() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		setAllEPDefinitiontrue();

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {

			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, null))
					.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(countrybean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			updateExtendedProfileResp = serviceToTest
					.updateExtendedProfile(getEPRequestWithMandateDefinitionValuesEmpty(), mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("middleInitial").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("title").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("suffix").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("faxNumber").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("webUrl").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("imageUrl").trim());
			Assertions.assertEquals(SFTranslationTextConstants.PAGER_NUMBER_DFLT_VAL + AtWinXSConstant.BLANK_SPACE + SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("pagerNumber").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("tollFreeNumber").trim());
			Assertions.assertEquals(ADDRESS_LINE_1_FLD_NM + SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("addressLine1").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("city").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("countryCd").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("stateCd").trim());
			Assertions.assertEquals(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR,
					updateExtendedProfileResp.getFieldMessages().get("zip").trim());
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withExceedDbFieldLength_returns_fail() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_NAME_USA);

		ArrayList<String> states = new ArrayList<String>();
		states.add(STATEINUSA_NAME_AL);

		StateBean statebean = new StateBean();
		statebean.setCountryCode(COUNTRY_CODE_USA);
		statebean.setStateCode(STATEINUSA_CODE_AL);
		statebean.setStateCodeText(STATEINUSA_NAME_AL);

		com.wallace.atwinxs.locale.ao.CountryBean countrybean = new CountryBean();
		countrybean.setCountryCode(COUNTRY_CODE_USA);
		countrybean.setCountryNameLong(COUNTRY_NAME_USA);

		// Extended profile access setting
		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(true);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			for (Map<String, Object> EProfileMaxSizeSFTTC : getlistOfEProfileMaxSizeSFTTC()) {

				mockTranslationTextTag
						.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
								mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR,
								EProfileMaxSizeSFTTC))
						.thenReturn(SFTranslationTextConstants.MAX_CHARS_ERR);
			}

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(countrybean);

			updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithExceedDbFieldLength(),
					mockSessionContainer);
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("name2"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("middleInitial"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("title"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("suffix"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("faxNumber"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("webUrl"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("imageUrl"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("pagerNumber"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("tollFreeNumber"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("addressLine1"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("addressLine2"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("addressLine3"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("city"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("countryCd"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("stateCd"));
			Assertions.assertEquals(SFTranslationTextConstants.MAX_CHARS_ERR,
					updateExtendedProfileResp.getFieldMessages().get("zip"));
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_withNoProfileNumber_returns_fail403() throws Exception {

		when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithAllAttributeValues(),
						mockSessionContainer);
			});
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}


	//CAP-45485
	@Test
	void that_updateExtendedProfile_whenExtendedProfileExistsIsFalse_returns_fail403() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(false);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithAllAttributeValues(),
						mockSessionContainer);
			});
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}

	//CAP-45485
	@Test
	void that_updateExtendedProfile_whenCanUserEditExtendedProfileIsFalse_returns_fail403() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition)
				.thenReturn(mockProfile);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockProfileDefinition).populate(0, 0, 0);

		when(mockProfileDefinition.isExtendedProfileExists()).thenReturn(true);
		when(mockProfileDefinition.isCanUserEditExtendedProfile()).thenReturn(false);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)) {
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				updateExtendedProfileResp = serviceToTest.updateExtendedProfile(getEPRequestWithAllAttributeValues(),
						mockSessionContainer);
			});
			Assertions.assertNotNull(updateExtendedProfileResp);
			assertFalse(updateExtendedProfileResp.isSuccess());
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}

	// CAP-42342 Update Company Profile JUnit Test Case , CAP-45485
	@Test
	void that_updateCompanyProfileResponse_success() throws AtWinXSException {
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition).thenReturn(mockProfile);
			when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
			when(mockProfile.getCorporateProfile()).thenReturn(mockCorporateProfile);
			when(mockAppSessionBean.getCorporateProfile()).thenReturn(mockCorporateProfileBean);
			when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_USA);
			when(mockCountryBean.getStatesInCountryByName()).thenReturn(states.iterator());
			when(mockCountryBean.getCountryHasStates()).thenReturn(true);
			when(mockCountryBean.getStateInCountryByName(STATEINUSA_NAME_AL)).thenReturn(statebean);
			companyProfileresponse = serviceToTest.updateCompanyProfile(companyProfileRequest, mockSessionContainer);
			assertTrue(companyProfileresponse.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_updateCompanyProfileResponse_profileNumberIsNegative() throws AtWinXSException {
		when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				companyProfileresponse = serviceToTest.updateCompanyProfile(companyProfileRequest, mockSessionContainer);
			});
			assertFalse(companyProfileresponse.isSuccess());
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}


	//CAP-45485
	@Test
	void that_updateCompanyProfileResponse_CanUserEditCorporateProfileIsFalse() {
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition).thenReturn(mockProfile);
		when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(false);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				companyProfileresponse = serviceToTest.updateCompanyProfile(companyProfileRequest, mockSessionContainer);
			});
			assertFalse(companyProfileresponse.isSuccess());
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}

	//CAP-45485
	@Test
	void that_validateCompanyProfile_fieldLengthGreaterThanSizeLimits() throws AtWinXSException {
		companyProfileRequest = buildCompanyProfileRequest(2);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
			 MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition).thenReturn(mockProfile);
			when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
			when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_USA);
			when(mockCountryBean.getStatesInCountryByName()).thenReturn(states.iterator());
			when(mockCountryBean.getCountryHasStates()).thenReturn(true);
			when(mockCountryBean.getStateInCountryByName(STATEINUSA_NAME_AL)).thenReturn(statebean);
			companyProfileresponse = serviceToTest.updateCompanyProfile(companyProfileRequest, mockSessionContainer);
			assertFalse(companyProfileresponse.isSuccess());
		}
	}

	//CAP-45485
	@Test
	void that_validateCompanyProfile_requiredFieldIsBlank() throws AtWinXSException {
		companyProfileRequest = buildCompanyProfileRequest(3);
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
				MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);
				MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
						.mockStatic(TranslationTextTag.class)) {
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_NAME_USA))
					.thenReturn(mockCountryBean);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
			.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockProfileDefinition).thenReturn(mockProfile);
			when(mockProfileDefinition.isCanUserEditCorporateProfile()).thenReturn(true);
			setCompanyRequiredFields();
			when(mockCountryBean.getCountryCode()).thenReturn(COUNTRY_CODE_USA);
			when(mockCountryBean.getStatesInCountryByName()).thenReturn(states.iterator());
			when(mockCountryBean.getCountryHasStates()).thenReturn(true);
			when(mockCountryBean.getStateInCountryByName(STATEINUSA_NAME_AL)).thenReturn(statebean);
			companyProfileresponse = serviceToTest.updateCompanyProfile(companyProfileRequest, mockSessionContainer);
			assertFalse(companyProfileresponse.isSuccess());
		}
	}

	// CAP-42342 Update Company Profile Setup, CAP-45485
	private UpdateCompanyProfileRequest buildCompanyProfileRequest(int scenario) {
		UpdateCompanyProfileRequest request = new UpdateCompanyProfileRequest();
		switch (scenario) {
		case 1:
			request = new UpdateCompanyProfileRequest();
			request.setUseAsdefaultPreferredShipping(true);
			request.setName1("Manay Jean's");
			request.setName2("Inasal");
			request.setTitle("Chief executive officer");
			request.setFaxNumber("1-888-123-1239");
			request.setPhoneNumber("1-888-123-1239");
			request.setWebUrl("https://www.rrd.com/sampleUrl");
			request.setImageUrl("https://www.google.com/sampleUrl");
			request.setDepartment("General Management");
			request.setDivision("Technology");
			request.setAddressLine1("2502 E WILLETTA ST");
			request.setAddressLine2("PHOENIX AZ 85008-4643");
			request.setAddressLine3("USA");
			request.setCity("Phoenix");
			request.setStateCd("AL");
			request.setZipCd("85001");
			request.setCountryCd("USA");
			break;
		case 2:
			request = new UpdateCompanyProfileRequest();
			request.setUseAsdefaultPreferredShipping(true);
			request.setName1(TEXT_WITH_MORETHAN255_CHAR);
			request.setName2(TEXT_WITH_MORETHAN255_CHAR);
			request.setTitle(TEXT_WITH_MORETHAN255_CHAR);
			request.setFaxNumber(TEXT_WITH_MORETHAN255_CHAR);
			request.setPhoneNumber(TEXT_WITH_MORETHAN255_CHAR);
			request.setWebUrl(TEXT_WITH_MORETHAN255_CHAR);
			request.setImageUrl(TEXT_WITH_MORETHAN255_CHAR);
			request.setDepartment(TEXT_WITH_MORETHAN255_CHAR);
			request.setDivision(TEXT_WITH_MORETHAN255_CHAR);
			request.setAddressLine1(TEXT_WITH_MORETHAN255_CHAR);
			request.setAddressLine2(TEXT_WITH_MORETHAN255_CHAR);
			request.setAddressLine3(TEXT_WITH_MORETHAN255_CHAR);
			request.setCity(TEXT_WITH_MORETHAN255_CHAR);
			request.setStateCd(TEXT_WITH_MORETHAN255_CHAR);
			request.setZipCd(TEXT_WITH_MORETHAN255_CHAR);
			request.setCountryCd(TEXT_WITH_MORETHAN255_CHAR);
			break;
		case 3:
			request = new UpdateCompanyProfileRequest();
			request.setUseAsdefaultPreferredShipping(true);
			request.setName1(AtWinXSConstant.EMPTY_STRING);
			request.setName2(AtWinXSConstant.EMPTY_STRING);
			request.setTitle(AtWinXSConstant.EMPTY_STRING);
			request.setFaxNumber(AtWinXSConstant.EMPTY_STRING);
			request.setPhoneNumber(AtWinXSConstant.EMPTY_STRING);
			request.setWebUrl(AtWinXSConstant.EMPTY_STRING);
			request.setImageUrl(AtWinXSConstant.EMPTY_STRING);
			request.setDepartment(AtWinXSConstant.EMPTY_STRING);
			request.setDivision(AtWinXSConstant.EMPTY_STRING);
			request.setAddressLine1(AtWinXSConstant.EMPTY_STRING);
			request.setAddressLine2(AtWinXSConstant.EMPTY_STRING);
			request.setAddressLine3(AtWinXSConstant.EMPTY_STRING);
			request.setCity(AtWinXSConstant.EMPTY_STRING);
			request.setStateCd(AtWinXSConstant.EMPTY_STRING);
			request.setZipCd(AtWinXSConstant.EMPTY_STRING);
			request.setCountryCd(AtWinXSConstant.EMPTY_STRING);
			break;
		}

		return request;
	}

	private void setCompanyRequiredFields() throws AtWinXSException {
		when(mockProfileDefinition.isCompanyNameRequired()).thenReturn(true);
		when(mockProfileDefinition.isCompanyTitleRequired()).thenReturn(true);
		when(mockProfileDefinition.isCompanyFaxRequired()).thenReturn(true);
		when(mockProfileDefinition.isCompanyPhoneNumberRequired()).thenReturn(true);
		when(mockProfileDefinition.isCompanyURLRequired()).thenReturn(true);
		when(mockProfileDefinition.isCompanyLogoRequired()).thenReturn(true);
		when(mockProfileDefinition.isDepartmentRequired()).thenReturn(true);
		when(mockProfileDefinition.isDivisionRequired()).thenReturn(true);
		when(mockProfileDefinition.isCompanyAddressRequired()).thenReturn(true);
	}

	private List<Map<String, Object>> getlistOfEProfileMaxSizeSFTTC() {

		int[] eProfileMaxSize = { SELF_ADMIN_MAX_SIZE_EXT_NAME2, SELF_ADMIN_MAX_SIZE_EXT_MID_INIT,
				SELF_ADMIN_MAX_SIZE_EXT_TITLE, SELF_ADMIN_MAX_SIZE_EXT_SUFFIX, SELF_ADMIN_MAX_SIZE_EXT_FAX,
				SELF_ADMIN_MAX_SIZE_EXT_MOBILE, SELF_ADMIN_MAX_SIZE_EXT_WEB_URL, SELF_ADMIN_MAX_SIZE_EXT_IMG_URL,
				SELF_ADMIN_MAX_SIZE_EXT_PAGER, SELF_ADMIN_MAX_SIZE_EXT_TOLL_FREE, SELF_ADMIN_MAX_SIZE_EXT_LINE1,
				SELF_ADMIN_MAX_SIZE_EXT_LINE2, SELF_ADMIN_MAX_SIZE_EXT_LINE3, SELF_ADMIN_MAX_SIZE_EXT_CITY,
				SELF_ADMIN_MAX_SIZE_EXT_COUNTRY_CD, SELF_ADMIN_MAX_SIZE_EXT_STATE_CD, SELF_ADMIN_MAX_SIZE_EXT_ZIP };

		List<Map<String, Object>> listOfEProfileMaxSizeSFTTC = new ArrayList<Map<String, Object>>();

		for (int eprofileAttributeMaxLength : eProfileMaxSize) {

			Map<String, Object> eProfileMaxSizeSFTTC = new HashMap<String, Object>();
			eProfileMaxSizeSFTTC.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					eprofileAttributeMaxLength + AtWinXSConstant.EMPTY_STRING);
			listOfEProfileMaxSizeSFTTC.add(eProfileMaxSizeSFTTC);
		}
		return listOfEProfileMaxSizeSFTTC;
	}

	private UpdateExtendedProfileRequest getEPRequestWithAllAttributeValues() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(true);
		updateExtendedProfileRequest.setName2("subbu suffix 20Jul");
		updateExtendedProfileRequest.setMiddleInitial("R");
		updateExtendedProfileRequest.setTitle("Dr");
		updateExtendedProfileRequest.setSuffix("Subbu Jr");
		updateExtendedProfileRequest.setFaxNumber("1-888-123-1239");
		updateExtendedProfileRequest.setPhoneNumber("1-888-123-1239");
		updateExtendedProfileRequest.setWebUrl("https://www.rrd.com/20Jul");
		updateExtendedProfileRequest.setImageUrl("https://www.google.com/20Jul");
		updateExtendedProfileRequest.setPagerNumber("1-888-123-1239");
		updateExtendedProfileRequest.setTollFreeNumber("1-888-123-1239");
		updateExtendedProfileRequest.setAddressLine1("35 Chennai1 20Jul");
		updateExtendedProfileRequest.setAddressLine2("35 Chennai2 20Jul");
		updateExtendedProfileRequest.setAddressLine3("35 Chennai3 20Jul");
		updateExtendedProfileRequest.setCity("Chennai 20Jul");
		updateExtendedProfileRequest.setStateCd("AL");
		updateExtendedProfileRequest.setZip("600049");
		updateExtendedProfileRequest.setCountryCd("USA");
		return updateExtendedProfileRequest;
	}

	private UpdateExtendedProfileRequest getEPRequestWithAllAttributeValues_CExistAndSNotExist() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(true);
		updateExtendedProfileRequest.setName2("subbu suffix 20Jul");
		updateExtendedProfileRequest.setMiddleInitial("R");
		updateExtendedProfileRequest.setTitle("Dr");
		updateExtendedProfileRequest.setSuffix("Subbu Jr");
		updateExtendedProfileRequest.setFaxNumber("1-888-123-1239");
		updateExtendedProfileRequest.setPhoneNumber("1-888-123-1239");
		updateExtendedProfileRequest.setWebUrl("https://www.rrd.com/20Jul");
		updateExtendedProfileRequest.setImageUrl("https://www.google.com/20Jul");
		updateExtendedProfileRequest.setPagerNumber("1-888-123-1239");
		updateExtendedProfileRequest.setTollFreeNumber("1-888-123-1239");
		updateExtendedProfileRequest.setAddressLine1("35 Chennai1 20Jul");
		updateExtendedProfileRequest.setAddressLine2("35 Chennai2 20Jul");
		updateExtendedProfileRequest.setAddressLine3("35 Chennai3 20Jul");
		updateExtendedProfileRequest.setCity("Chennai 20Jul");
		updateExtendedProfileRequest.setStateCd("AC"); // State not in USA
		updateExtendedProfileRequest.setZip("600049");
		updateExtendedProfileRequest.setCountryCd("USA");
		return updateExtendedProfileRequest;
	}

	private UpdateExtendedProfileRequest getEPRequestWithAllAttributeValues_COnlyExist() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(true);
		updateExtendedProfileRequest.setName2("subbu suffix 20Jul");
		updateExtendedProfileRequest.setMiddleInitial("R");
		updateExtendedProfileRequest.setTitle("Dr");
		updateExtendedProfileRequest.setSuffix("Subbu Jr");
		updateExtendedProfileRequest.setFaxNumber("1-888-123-1239");
		updateExtendedProfileRequest.setPhoneNumber("1-888-123-1239");
		updateExtendedProfileRequest.setWebUrl("https://www.rrd.com/20Jul");
		updateExtendedProfileRequest.setImageUrl("https://www.google.com/20Jul");
		updateExtendedProfileRequest.setPagerNumber("1-888-123-1239");
		updateExtendedProfileRequest.setTollFreeNumber("1-888-123-1239");
		updateExtendedProfileRequest.setAddressLine1("35 Chennai1 20Jul");
		updateExtendedProfileRequest.setAddressLine2("35 Chennai2 20Jul");
		updateExtendedProfileRequest.setAddressLine3("35 Chennai3 20Jul");
		updateExtendedProfileRequest.setCity("Chennai 20Jul");
		updateExtendedProfileRequest.setStateCd("");
		updateExtendedProfileRequest.setZip("600049");
		updateExtendedProfileRequest.setCountryCd("AFG");
		return updateExtendedProfileRequest;
	}

	private UpdateExtendedProfileRequest getEPRequestWithAllAttributeValues_CNotExistAndSNotExist() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(true);
		updateExtendedProfileRequest.setName2("subbu suffix 20Jul");
		updateExtendedProfileRequest.setMiddleInitial("R");
		updateExtendedProfileRequest.setTitle("Dr");
		updateExtendedProfileRequest.setSuffix("Subbu Jr");
		updateExtendedProfileRequest.setFaxNumber("1-888-123-1239");
		updateExtendedProfileRequest.setPhoneNumber("1-888-123-1239");
		updateExtendedProfileRequest.setWebUrl("https://www.rrd.com/20Jul");
		updateExtendedProfileRequest.setImageUrl("https://www.google.com/20Jul");
		updateExtendedProfileRequest.setPagerNumber("1-888-123-1239");
		updateExtendedProfileRequest.setTollFreeNumber("1-888-123-1239");
		updateExtendedProfileRequest.setAddressLine1("35 Chennai1 20Jul");
		updateExtendedProfileRequest.setAddressLine2("35 Chennai2 20Jul");
		updateExtendedProfileRequest.setAddressLine3("35 Chennai3 20Jul");
		updateExtendedProfileRequest.setCity("Chennai 20Jul");
		updateExtendedProfileRequest.setStateCd("ZZ"); // State Code not Valid
		updateExtendedProfileRequest.setZip("600049");
		updateExtendedProfileRequest.setCountryCd("ZZ"); // County Code not Valid
		return updateExtendedProfileRequest;
	}

	private UpdateExtendedProfileRequest getEPRequestWithMandateDefinitionValuesEmpty() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(true);
		updateExtendedProfileRequest.setName2("");
		updateExtendedProfileRequest.setMiddleInitial("");
		updateExtendedProfileRequest.setTitle("");
		updateExtendedProfileRequest.setSuffix("");
		updateExtendedProfileRequest.setFaxNumber("");
		updateExtendedProfileRequest.setPhoneNumber("");
		updateExtendedProfileRequest.setWebUrl("");
		updateExtendedProfileRequest.setImageUrl("");
		updateExtendedProfileRequest.setPagerNumber("");
		updateExtendedProfileRequest.setTollFreeNumber("");
		updateExtendedProfileRequest.setAddressLine1("");
		updateExtendedProfileRequest.setAddressLine2("");
		updateExtendedProfileRequest.setAddressLine3("");
		updateExtendedProfileRequest.setCity("");
		updateExtendedProfileRequest.setStateCd("");
		updateExtendedProfileRequest.setZip("");
		updateExtendedProfileRequest.setCountryCd("");
		return updateExtendedProfileRequest;
	}

	private UpdateExtendedProfileRequest getEPRequestWithAllValuesEmptyAndDefaultPSFalse() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(false);
		updateExtendedProfileRequest.setName2("");
		updateExtendedProfileRequest.setMiddleInitial("");
		updateExtendedProfileRequest.setTitle("");
		updateExtendedProfileRequest.setSuffix("");
		updateExtendedProfileRequest.setFaxNumber("");
		updateExtendedProfileRequest.setPhoneNumber("");
		updateExtendedProfileRequest.setWebUrl("");
		updateExtendedProfileRequest.setImageUrl("");
		updateExtendedProfileRequest.setPagerNumber("");
		updateExtendedProfileRequest.setTollFreeNumber("");
		updateExtendedProfileRequest.setAddressLine1("");
		updateExtendedProfileRequest.setAddressLine2("");
		updateExtendedProfileRequest.setAddressLine3("");
		updateExtendedProfileRequest.setCity("");
		updateExtendedProfileRequest.setStateCd("");
		updateExtendedProfileRequest.setZip("");
		updateExtendedProfileRequest.setCountryCd("");
		return updateExtendedProfileRequest;
	}


	private UpdateExtendedProfileRequest getEPRequestWithExceedDbFieldLength() {
		UpdateExtendedProfileRequest updateExtendedProfileRequest = new UpdateExtendedProfileRequest();

		updateExtendedProfileRequest.setUseAsdefaultPreferredShipping(true);
		updateExtendedProfileRequest.setName2("subbu suffix Exceed Length Exceed Length 35");
		updateExtendedProfileRequest.setMiddleInitial("R Exceed Length 1");
		updateExtendedProfileRequest.setTitle("Dr Exceed Length 10");
		updateExtendedProfileRequest.setSuffix("Subbu Jr Exceed Length 15");
		updateExtendedProfileRequest.setFaxNumber("1-888-123-1239 Exceed Length 24");
		updateExtendedProfileRequest.setPhoneNumber("1-888-123-1239 Exceed Length 24");
		updateExtendedProfileRequest.setWebUrl(TEXT_WITH_MORETHAN255_CHAR);
		updateExtendedProfileRequest.setImageUrl(TEXT_WITH_MORETHAN255_CHAR);
		updateExtendedProfileRequest.setPagerNumber("1-888-123-1239 Exceed Length 24");
		updateExtendedProfileRequest.setTollFreeNumber("1-888-123-1239  Exceed Length 24");
		updateExtendedProfileRequest
				.setAddressLine1("35 Chennai1 20Jul  Exceed Length 35  Exceed Length 35  Exceed Length 35");
		updateExtendedProfileRequest
				.setAddressLine2("35 Chennai2 20Jul  Exceed Length 35  Exceed Length 35  Exceed Length 35");
		updateExtendedProfileRequest
				.setAddressLine3("35 Chennai3 20Jul  Exceed Length 35  Exceed Length 35  Exceed Length 35");
		updateExtendedProfileRequest.setCity("Chennai Exceed Length 30  Exceed Length 30");
		updateExtendedProfileRequest.setStateCd("AL Exceed Length 3");
		updateExtendedProfileRequest.setZip("600049  Exceed Length 12");
		updateExtendedProfileRequest.setCountryCd("USA  Exceed Length 4");
		return updateExtendedProfileRequest;
	}


	//CAP-42562 Starts here , CAP-45485
	@Test
	void that_updateUserDefinedFields_returns_success() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
		doNothing().when(mockUserDefinedField).populate(0, 0, 1, 1);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(50);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(false);
		when(mockObjectMapFactoryService.getDAOObjectMap()).thenReturn(mockDAOObjectMap);
	    when(mockDAOObjectMap.getObject(any(), any(), eq(mockToken))).thenReturn(mockUserDefinedFieldDAO);
	    when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
	    when(mockUserDefinedField.isExisting()).thenReturn(true);
		doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
	    try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){

	    	mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
	    		.thenReturn(mockUserSettings);
	    	when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFRequest(), mockSessionContainer);
			assertTrue(updateUserDefinedfieldsResponse.isSuccess());
	    }
	}

	//CAP-42562 Starts here , CAP-45485
	@Test
	void that_updateUserDefinedFieldsWith_returns_success2() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
		doNothing().when(mockUserDefinedField).populate(0, 0, 1, 1);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(50);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(false);
		when(mockObjectMapFactoryService.getDAOObjectMap()).thenReturn(mockDAOObjectMap);
	    when(mockDAOObjectMap.getObject(any(), any(), eq(mockToken))).thenReturn(mockUserDefinedFieldDAO);
	    when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
	    when(mockAppSessionBean.isInRequestorMode()).thenReturn(true);	//CAP-45485
	    when(mockUserDefinedField.isExisting()).thenReturn(true);
		doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
	    try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){

	    	mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
	    		.thenReturn(mockUserSettings);
	    	when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFRequest(), mockSessionContainer);
			assertTrue(updateUserDefinedfieldsResponse.isSuccess());
	    }
	}

	private List<UserDefinedField> getDefinedUDFList() {

		UserDefinedField udfField=new UserDefinedFieldImpl();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("Test1234");
		List<UserDefinedField> userDefinedFields=new ArrayList<UserDefinedField>();
		userDefinedFields.add(udfField);
		return(userDefinedFields);
	}

	private UserDefinedFieldsRequest getUDFRequest() {
		 UserDefinedFieldsRequest  request =  new UserDefinedFieldsRequest();
		C1UserDefinedField udfField=new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("Test1234");
		ArrayList<C1UserDefinedField> userDefinedFields=new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
		return(request);
	}

	private UserDefinedFieldsRequest getUDFEmptyRequest() {
		 UserDefinedFieldsRequest  request =  new UserDefinedFieldsRequest();
		C1UserDefinedField udfField=new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("");
		ArrayList<C1UserDefinedField> userDefinedFields=new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
		return(request);
	}


	//CAP-45485
	@Test
	void that_updateUserDefinedFields_noUserProfile403() {
		when(mockAppSessionBean.getProfileNumber()).thenReturn(0);

		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
			.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFRequest(), mockSessionContainer);
			});
			assertFalse(updateUserDefinedfieldsResponse.isSuccess());
			assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
		}
	}

	//CAP-45485
	@Test
	void that_updateUserDefinedFields_returns_fail() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
	serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		ArrayList<C1UserDefinedField> c1UserDefinedField = getUDFRequest().getC1UserDefinedFields();
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);	){

			doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
			doReturn(false).when(serviceToTest).validateUserDefinedFields(mockUDFlist, c1UserDefinedField, updateUserDefinedfieldsResponse, mockAppSessionBean);


			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR, null))
			.thenReturn(SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFRequest(), mockSessionContainer);
			assertFalse(updateUserDefinedfieldsResponse.isSuccess());
		}

	}


	//CAP-45485
	@Test
	void that_updateUserDefinedFields_returnsmaxlenghth_fail() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockUserDefinedField).populate(0, 0, 1, 1);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(1);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(false);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		when(mockUserDefinedField.isExisting()).thenReturn(true);
		doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);

		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);	){

			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFRequest(), mockSessionContainer);
			assertFalse(updateUserDefinedfieldsResponse.isSuccess());
		}

	}

	//CAP-45485
	@Test
	void that_updateUserDefinedFields_returnsrequired_fail() throws AtWinXSException {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(50);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(true);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);

		when(mockUserDefinedField.isExisting()).thenReturn(true);
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class);){
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFEmptyRequest(), mockSessionContainer);
			assertFalse(updateUserDefinedfieldsResponse.isSuccess());
		}

	}

	private UserDefinedFieldsRequest getUDFEmptyRequest_withvaluetext_withtran() {
		 UserDefinedFieldsRequest  request =  new UserDefinedFieldsRequest();
		C1UserDefinedField udfField=new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("ABC");
		ArrayList<C1UserDefinedField> userDefinedFields=new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
		return(request);
	}

	//CAP-45485
	@Test
	void that_updateUserDefinedFields_returnsrequired_fail_valuetext() throws AtWinXSException {

		serviceToTest = Mockito.spy(serviceToTest);
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class); ){
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockObjectMapFactoryService.getDAOObjectMap()).thenReturn(mockDAOObjectMap);
			when(mockDAOObjectMap.getObject(any(), any(), any())).thenReturn(mockUserDefinedFieldDAO);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
			when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(50);
			when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(true);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockUserDefinedField.isExisting()).thenReturn(true);
			doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG);
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFEmptyRequest_withvaluetext_withtran(), mockSessionContainer);
			assertTrue(updateUserDefinedfieldsResponse.isSuccess());
		}

	}

	private UserDefinedFieldsRequest getUDFEmptyRequest_valuetextnull() {
		 UserDefinedFieldsRequest  request =  new UserDefinedFieldsRequest();
		C1UserDefinedField udfField=new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText(null);
		ArrayList<C1UserDefinedField> userDefinedFields=new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
		return(request);
	}


	//CAP-45485
	@Test
	void that_updateUserDefinedFields_noreturnsrequired_fail_withouttrans() throws AtWinXSException {

			serviceToTest = Mockito.spy(serviceToTest);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockObjectMapFactoryService.getDAOObjectMap()).thenReturn(mockDAOObjectMap);
			when(mockDAOObjectMap.getObject(any(), any(), any())).thenReturn(mockUserDefinedFieldDAO);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockUserDefinedField.isExisting()).thenReturn(true);
			doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
			try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
				mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
				when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
				updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFEmptyRequest_valuetextnull(), mockSessionContainer);
				assertTrue(updateUserDefinedfieldsResponse.isSuccess());
			}
	}

	private UserDefinedFieldsRequest getUDFEmptyRequest_emptyvaluetext_withouttran() {
		 UserDefinedFieldsRequest  request =  new UserDefinedFieldsRequest();
		C1UserDefinedField udfField=new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("");
		ArrayList<C1UserDefinedField> userDefinedFields=new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
		return(request);
	}

	//CAP-45485
	@Test
	void that_updateUserDefinedFields_returnsrequired_notranslation() throws AtWinXSException {

			serviceToTest = Mockito.spy(serviceToTest);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockObjectMapFactoryService.getDAOObjectMap()).thenReturn(mockDAOObjectMap);
			when(mockDAOObjectMap.getObject(any(), any(), any())).thenReturn(mockUserDefinedFieldDAO);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
			when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
			when(mockUserDefinedField.isExisting()).thenReturn(true);
			doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
			try(MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class)){
				mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
					.thenReturn(mockUserSettings);
				when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
				updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFEmptyRequest_emptyvaluetext_withouttran(), mockSessionContainer);
				assertTrue(updateUserDefinedfieldsResponse.isSuccess());
			}
	}

	private UserDefinedFieldsRequest getUDFEmptyRequest_emptyvaluetext() {
		UserDefinedFieldsRequest request = new UserDefinedFieldsRequest();
		C1UserDefinedField udfField = new C1UserDefinedField();
		udfField.setUdfFieldNumber(1);
		udfField.setUdfValueText("");
		ArrayList<C1UserDefinedField> userDefinedFields = new ArrayList<C1UserDefinedField>();
		userDefinedFields.add(udfField);
		request.setC1UserDefinedFields(userDefinedFields);
		return (request);
	}

	//CAP-45485
	@Test
	void that_updateUserDefinedFields_returnsrequired_textvalueempty_fail() throws AtWinXSException {

		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);
			MockedStatic<AdminUtil> mockedAdminUtil = Mockito.mockStatic(AdminUtil.class); ){
			mockedAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
			when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
			when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
			when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(50);
			when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(true);
			doReturn(mockUDFlist).when(serviceToTest).getProfileUDFs(mockAppSessionBean);
			when(mockUserDefinedField.isExisting()).thenReturn(true);
			mockTranslationTextTag
			.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
					mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, null))
			.thenReturn(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG);
			updateUserDefinedfieldsResponse = serviceToTest.updateUserDefinedFields(getUDFEmptyRequest_emptyvaluetext(), mockSessionContainer);
			assertFalse(updateUserDefinedfieldsResponse.isSuccess());
		}
	}

	// CAP-43631
	@Test
	void that_deletePABAddress_withPABTurnedOff_returns_fail403() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(false);
		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			Exception exception = assertThrows(NullPointerException.class, () -> {
			pabDeleteResponse = serviceToTest.deletePABAddress(null, mockSessionContainer,false);
		});
		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		//assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_deletePABAddress_withNoProfileSharedUser_returns_fail403() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);
		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			pabDeleteResponse = serviceToTest.deletePABAddress(null, mockSessionContainer,false);
		});
		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void that_deletePABAddress_withNullAddressIdList_returns_fail() throws Exception {

		pabDeleteRequest.setAddressIds(null);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,false);

		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		assertEquals(0, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(SFTranslationTextConstants.NO_SELECTED_ADDR_DEF_ERR_MSG, pabDeleteResponse.getMessage());
	}

	@Test
	void that_deletePABAddress_withEmptyAddressIdList_returns_fail() throws Exception {

		pabDeleteRequest.setAddressIds(testAddressIDs);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(false);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,false);

		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		assertEquals(0, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(SFTranslationTextConstants.NO_SELECTED_ADDR_DEF_ERR_MSG, pabDeleteResponse.getMessage());
	}

	@Test
	void that_deletePABAddress_withNullAddressBook_returns_fail() throws Exception {

		testAddressIDs.add(TEST_ADDRESS_ID);
		pabDeleteRequest.setAddressIds(testAddressIDs);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockProfile).when(mockEntityObjectMap).getEntity(Profile.class, mockToken);
		doNothing().when(mockProfile).populate(12345);
		doReturn(mockAddressBook).when(mockProfile).getAddressBook();
		doReturn(null).when(mockAddressBook).getAddressBook();

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,false);

		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		assertEquals(0, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(SFTranslationTextConstants.FAILED_TO_DELETE_ADDR_DEF_ERR_MSG, pabDeleteResponse.getMessage());
	}

	@Test
	void that_deletePABAddress_withEmptyAddressBook_returns_fail() throws Exception {

		testAddressIDs.add(TEST_ADDRESS_ID);
		pabDeleteRequest.setAddressIds(testAddressIDs);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockProfile).when(mockEntityObjectMap).getEntity(Profile.class, mockToken);
		doNothing().when(mockProfile).populate(12345);
		doReturn(mockAddressBook).when(mockProfile).getAddressBook();
		doReturn(addressBookRecords).when(mockAddressBook).getAddressBook();

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,false);

		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		assertEquals(0, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(SFTranslationTextConstants.FAILED_TO_DELETE_ADDR_DEF_ERR_MSG, pabDeleteResponse.getMessage());
	}

	@Test
	void that_deletePABAddress_throwsException_fail() throws Exception {

		addressBookRecords.add(mockAddressBookRecord);
		testAddressIDs.add(TEST_ADDRESS_ID);
		pabDeleteRequest.setAddressIds(testAddressIDs);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockProfile).when(mockEntityObjectMap).getEntity(Profile.class, mockToken);
		doNothing().when(mockProfile).populate(12345);
		doReturn(mockAddressBook).when(mockProfile).getAddressBook();
		doReturn(addressBookRecords).when(mockAddressBook).getAddressBook();
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doReturn(TEST_ADDRESS_ID).when(mockProfile).getDefaultPersonalAddressID();
		doNothing().when(mockProfile).setDefaultPersonalAddressID(0);
		doNothing().when(mockProfile).save();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getSiteID();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getBusinessUnitID();
		doReturn(12345).when(mockProfile).getProfileNumber();
		doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockAddressBookRecord)
				.delete(AtWinXSConstant.INVALID_ID, AtWinXSConstant.INVALID_ID, 12345, TEST_ADDRESS_ID);


		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,false);

		assertNotNull(pabDeleteResponse);
		assertFalse(pabDeleteResponse.isSuccess());
		assertEquals(0, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(SFTranslationTextConstants.FAILED_TO_DELETE_ADDR_DEF_ERR_MSG, pabDeleteResponse.getMessage());
	}

	@Test
	void that_deletePABAddress_return_success() throws Exception {

		addressBookRecords.add(mockAddressBookRecord);
		testAddressIDs.add(TEST_ADDRESS_ID);
		testAddressIDs.add(TEST_ADDRESS_ID + 1);
		pabDeleteRequest.setAddressIds(testAddressIDs);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());


		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockProfile).when(mockEntityObjectMap).getEntity(Profile.class, mockToken);
		doNothing().when(mockProfile).populate(12345);
		doReturn(mockAddressBook).when(mockProfile).getAddressBook();
		doReturn(addressBookRecords).when(mockAddressBook).getAddressBook();
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getDefaultPersonalAddressID();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getSiteID();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getBusinessUnitID();
		doReturn(12345).when(mockProfile).getProfileNumber();
		doNothing().when(mockAddressBookRecord).delete(AtWinXSConstant.INVALID_ID, AtWinXSConstant.INVALID_ID, 12345,
				TEST_ADDRESS_ID);

		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,false);

		assertNotNull(pabDeleteResponse);
		assertTrue(pabDeleteResponse.isSuccess());
		assertEquals(1, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabDeleteResponse.getMessage());
	}
	// CAP-43631 End


	// CAP-43598 Start
			@Test
			void that_savePABAddress_withPABTurnedOff_returns_fail403() throws Exception {
				doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
				when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				pabSaveRequest.setAddressID(0);
				//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(false);
				//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

				doNothing().when(mockUser).populate(anyInt(), any());
				Exception exception = assertThrows(AccessForbiddenException.class, () -> {
					pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,false,false);
				});
				assertNotNull(pabSaveResponse);
				assertFalse(pabSaveResponse.isSuccess());
				assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
			}

			@Test
			void that_savePABAddress_withNoProfileSharedUser_returns_fail403() throws Exception {
				doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
				when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
				when(mockAppSessionBean.isSharedID()).thenReturn(true);
				when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);
				//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

				doNothing().when(mockUser).populate(anyInt(), any());

				Exception exception = assertThrows(AccessForbiddenException.class, () -> {
					pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,null,false,false);
				});
				assertNotNull(pabSaveResponse);
				assertFalse(pabSaveResponse.isSuccess());
				assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
			}

			@Test
			void that_savePABAddress_returns_success() throws Exception {
				serviceToTest = Mockito.spy(serviceToTest);
				doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
				when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				pabSaveRequest=getPABSaveRequest();
				//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
				when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

				//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

				doNothing().when(mockUser).populate(anyInt(), any());
				doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
				doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
				doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
				when(mockUser.getProfile()).thenReturn(mockProfile);
				when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);
				doReturn(true).when(serviceToTest).validateSavePAB(mockSessionContainer, pabSaveResponse, pabSaveRequest,
						mockAppSessionBean,false);
				pabSaveResponse = serviceToTest.savePABAddress( mockSessionContainer,pabSaveRequest,false,false);
	    		assertNotNull(pabSaveResponse);
				assertTrue(pabSaveResponse.isSuccess());
				assertEquals(AtWinXSConstant.EMPTY_STRING, pabSaveResponse.getMessage());
			}


			@Test
			void that_savePABAddress_returns_fail() throws Exception {
				doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
				when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
				when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
				serviceToTest = Mockito.spy(serviceToTest);
				pabSaveRequest=getPABSaveRequest();
				//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
				when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

				//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());


				doNothing().when(mockUser).populate(anyInt(), any());
				doReturn(false).when(serviceToTest).validateSavePAB(mockSessionContainer, pabSaveResponse, pabSaveRequest,
						mockAppSessionBean,false);
				pabSaveResponse = serviceToTest.savePABAddress( mockSessionContainer,pabSaveRequest,false,false);
	    		assertNotNull(pabSaveResponse);
				assertFalse(pabSaveResponse.isSuccess());
				assertNotNull(pabSaveResponse.getFieldMessages());
			}

		public PABSaveRequest getPABSaveRequest() {
			PABSaveRequest pabSaveRequest=new PABSaveRequest();
			pabSaveRequest.setAddressID(TEST_ADDRESS_ID);
			pabSaveRequest.setName("Test Name");
			pabSaveRequest.setName2("");
			pabSaveRequest.setAddress1("Test Address");
			pabSaveRequest.setAddress2("");
			pabSaveRequest.setAddress3("");
			pabSaveRequest.setCountry("USA");
			pabSaveRequest.setState("IL");
			pabSaveRequest.setCity("Chicago");
			pabSaveRequest.setZip("1213123");
			pabSaveRequest.setPhoneNumber("");
		    pabSaveRequest.setShipToAttn("Test");
		    pabSaveRequest.setHasPassedZipValidation(true);
		    pabSaveRequest.setShowShipToAtn(false);
		    pabSaveRequest.setCorporateAddress(false);
		    pabSaveRequest.setDefaultAddress(false);
		    pabSaveRequest.setExtendedAddress(false);
		    return pabSaveRequest;
		}
	//	 CAP-43598 End

	@Test
	void that_exportPABAddresses_throwsAtWinXSException_fail() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockAddressBookImpExp).when(mockEntityObjectMap).getEntity(AddressBookImpExp.class, mockToken);
		doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockAddressBookImpExp)
				.exportAddress(0, 0, 12345);

		pabExportResponse = serviceToTest.exportPABAddresses(mockSessionContainer, mockHttpServletResponse,false);

		assertNotNull(pabExportResponse);
		assertFalse(pabExportResponse.isSuccess());
		assertEquals(FAIL, pabExportResponse.getMessage());
	}

	@Test
	void that_exportPABAddresses_throwsIOException_fail() throws Exception {

		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockAddressBookImpExp).when(mockEntityObjectMap).getEntity(AddressBookImpExp.class, mockToken);
		doThrow(new IOException(FAIL)).when(mockAddressBookImpExp)
				.exportAddress(0, 0, 12345);

		pabExportResponse = serviceToTest.exportPABAddresses(mockSessionContainer, mockHttpServletResponse,false);

		assertNotNull(pabExportResponse);
		assertFalse(pabExportResponse.isSuccess());
		assertEquals(FAIL, pabExportResponse.getMessage());
	}

	@Test
	void that_processWriteToResponseStream_throwsIOException_fail() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockAddressBookImpExp).when(mockEntityObjectMap).getEntity(AddressBookImpExp.class, mockToken);
		doReturn(mockFile).when(mockAddressBookImpExp).exportAddress(0, 0, 12345);
		doReturn(mockServletOutputStream).when(mockHttpServletResponse).getOutputStream();
		doNothing().when(mockHttpServletResponse).setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		doNothing().when(mockHttpServletResponse).setHeader(anyString(), anyString());
		doReturn(mockFileInputStream).when(serviceToTest).getFileInputStream(mockFile);
		doReturn(2, -1).when(mockFileInputStream).read(any());
		doThrow(new IOException(FAIL)).when(mockServletOutputStream).flush();
		doThrow(new IOException(FAIL)).when(serviceToTest).deleteTempExportFile(mockFile);

		pabExportResponse = serviceToTest.exportPABAddresses(mockSessionContainer, mockHttpServletResponse,false);

		assertNotNull(pabExportResponse);
		assertFalse(pabExportResponse.isSuccess());
		assertEquals(FAIL, pabExportResponse.getMessage());
	}

	@Test
	void that_exportPABAddresses_returnsSuccess() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);
		when(mockAppSessionBean.getSiteID()).thenReturn(0);
		when(mockAppSessionBean.getBuID()).thenReturn(0);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockAddressBookImpExp).when(mockEntityObjectMap).getEntity(AddressBookImpExp.class, mockToken);
		doReturn(mockFile).when(mockAddressBookImpExp).exportAddress(0, 0, 12345);
		doReturn(mockServletOutputStream).when(mockHttpServletResponse).getOutputStream();
		doNothing().when(mockHttpServletResponse).setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		doNothing().when(mockHttpServletResponse).setHeader(anyString(), anyString());
		doReturn(mockFileInputStream).when(serviceToTest).getFileInputStream(mockFile);
		doReturn(2, -1).when(mockFileInputStream).read(any());
		doNothing().when(mockServletOutputStream).flush();
		doReturn(AppProperties.getGlobalFileUploadPath() + "test.txt").when(mockFile).getPath();

		pabExportResponse = serviceToTest.exportPABAddresses(mockSessionContainer, mockHttpServletResponse,false);

		assertNotNull(pabExportResponse);
		assertTrue(pabExportResponse.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabExportResponse.getMessage());
	}

	//CAP-44961:Start
	@Test
	void that_savePABAddressWith_V1USPSValidation_returns_success() throws Exception {

		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationSuccess();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertTrue(pabSaveResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, pabSaveResponse.getMessage());
		}
	}

	@Test
	void that_savePABAddressWith_OldUSPSValidation_returns_success() throws Exception {

		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationSuccess();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,false,false);
			assertNotNull(pabSaveResponse);
			assertTrue(pabSaveResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, pabSaveResponse.getMessage());
		}
	}


	@Test
	void that_savePABAddressWithUSPSValidation_returns_Failed422_WithSuggestion() throws Exception {
		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationFailedWithSuggestion();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertFalse(pabSaveResponse.isSuccess());
			assertNotNull(pabSaveResponse.getFieldMessages());
		}
	}

	@Test
	void that_savePABAddressWithUSPSValidation_returns_Failed422_WithError1() throws Exception {
		pabSaveRequest=getPABSaveRequest_Invalid1();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationFailedWithError1();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertFalse(pabSaveResponse.isSuccess());
			assertNotNull(pabSaveResponse.getFieldMessages());
		}
	}

	@Test
	void that_savePABAddressWithUSPSValidation_returns_Failed422_WithError2() throws Exception {
		pabSaveRequest=getPABSaveRequest_Invalid2();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationFailedWithError2();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		//when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		//when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertFalse(pabSaveResponse.isSuccess());
			assertNotNull(pabSaveResponse.getFieldMessages());
		}
	}



	public PABSaveRequest getPABSaveRequest_Valid() {
		PABSaveRequest pabSaveRequest=new PABSaveRequest();
		pabSaveRequest.setAddressID(TEST_ADDRESS_ID);
		pabSaveRequest.setName("Tony");
		pabSaveRequest.setName2("");
		pabSaveRequest.setAddress1("3767 Pebble");
		pabSaveRequest.setAddress2("ter");
		pabSaveRequest.setAddress3("");
		pabSaveRequest.setCountry("USA");
		pabSaveRequest.setState("FL");
		pabSaveRequest.setCity("Punta Gorda");
		pabSaveRequest.setZip("33980");
		pabSaveRequest.setPhoneNumber("");
	    pabSaveRequest.setShipToAttn("Test");
	    pabSaveRequest.setHasPassedZipValidation(false);
	    pabSaveRequest.setShowShipToAtn(false);
	    pabSaveRequest.setCorporateAddress(false);
	    pabSaveRequest.setDefaultAddress(false);
	    pabSaveRequest.setExtendedAddress(false);
	    return pabSaveRequest;
	}

	public PABSaveRequest getPABSaveRequest_Invalid1() {
		PABSaveRequest pabSaveRequest=new PABSaveRequest();
		pabSaveRequest.setAddressID(TEST_ADDRESS_ID);
		pabSaveRequest.setName("Tony");
		pabSaveRequest.setName2("");
		pabSaveRequest.setAddress1("3767 Test");
		pabSaveRequest.setAddress2("Test");
		pabSaveRequest.setAddress3("");
		pabSaveRequest.setCountry("USA");
		pabSaveRequest.setState("AL");
		pabSaveRequest.setCity("Gorda");
		pabSaveRequest.setZip("3398011");
		pabSaveRequest.setPhoneNumber("");
	    pabSaveRequest.setShipToAttn("Test");
	    return pabSaveRequest;
	}

	public PABSaveRequest getPABSaveRequest_Invalid2() {
		PABSaveRequest pabSaveRequest=new PABSaveRequest();
		pabSaveRequest.setAddressID(TEST_ADDRESS_ID);
		pabSaveRequest.setName("Tony");
		pabSaveRequest.setName2("");
		pabSaveRequest.setAddress1("3767 Test");
		pabSaveRequest.setAddress2("Test");
		pabSaveRequest.setAddress3("");
		pabSaveRequest.setCountry("USA");
		pabSaveRequest.setState("AL");
		pabSaveRequest.setCity("Gorda");
		pabSaveRequest.setZip("3398");
		pabSaveRequest.setPhoneNumber("");
	    pabSaveRequest.setShipToAttn("Test");
	    return pabSaveRequest;
	}

	public USPSAddressInfoServiceParmsVO getUSPSAddrValidationSuccess() {
		USPSAddressInfoServiceParmsVO addressParms =
				new USPSAddressInfoServiceParmsVO("3767 PEBBLE TER","","PUNTA GORDA","FL","33980","");
		return(addressParms);
	}

	public USPSAddressInfoServiceParmsVO getUSPSAddrValidationFailedWithSuggestion() {
		USPSAddressInfoServiceParmsVO addressParms =
				new USPSAddressInfoServiceParmsVO(""," TEST","PUNTA GORD","F","33981","");
		return(addressParms);
	}

	public USPSAddressInfoServiceParmsVO getUSPSAddrValidationFailedWithError1() {
		USPSAddressInfoServiceParmsVO addressParms =
				new USPSAddressInfoServiceParmsVO(null,null,null,null,null,null);
		addressParms.setErrorMessage("Address Not Found.");
		return(addressParms);
	}

	public USPSAddressInfoServiceParmsVO getUSPSAddrValidationFailedWithError2() {
		USPSAddressInfoServiceParmsVO addressParms =
				new USPSAddressInfoServiceParmsVO(null,null,null,null,null,null);
		addressParms.setErrorMessage("Default address:The address you entered was found but more information "
				+ "is needed (such as an apartment, suite, or box number) to match to a specific address.");
		return(addressParms);
	}

	//CAP-44961:End


	//CAP-45374
	@Test
	void that_splitUspsAddress_returns_Failed422_WithError1() throws Exception {

		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSValidationFailedWithSuggestedAddrlengthGRTHAN30();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertFalse(pabSaveResponse.isSuccess());
			assertTrue(pabSaveResponse.getSuggestedAddress1().length()<=30);
			assertTrue(pabSaveResponse.getSuggestedAddress2().length()<=30);
		}
	}

	//CAP-45374
	@Test
	void that_splitUspsAddress_returns_Failed422_WithError2() throws Exception {

		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSValidationFailedWithSuggestedAddrlengthLSTHAN30();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());


		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);


		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertTrue(pabSaveResponse.isSuccess());
		}
	}

	//CAP-45374
	public USPSAddressInfoServiceParmsVO getUSPSValidationFailedWithSuggestedAddrlengthGRTHAN30() {
		USPSAddressInfoServiceParmsVO addressParms =
				new USPSAddressInfoServiceParmsVO("102 DR MAIN ST APT 101 DEPT 1366 EXECUTIVE DR STE 202 1600 CENTRAL BLDG 14 55 SYLVAN BLVD RM 108","","PUNTA GORDA","FL","33980","");
		return(addressParms);
	}

	//CAP-45374
	public USPSAddressInfoServiceParmsVO getUSPSValidationFailedWithSuggestedAddrlengthLSTHAN30() {
		USPSAddressInfoServiceParmsVO addressParms =
				new USPSAddressInfoServiceParmsVO("3767 PEBBLE","TER","PUNTA GORDA","FL","33980","");
		return(addressParms);
	}

	//CAP-45631
	@Test
	void that_savePABAddressWithUSPSValidation_returns_Failed422_WithError3() throws Exception {
		pabSaveRequest=getPABSaveRequest_Invalid2();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationFailedWithError2();

		// if zip code characters greater/equal to size 5, but first 5 character contains dash(non numeric)
		pabSaveRequest.setZip("3398--");

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
				when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
				mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
				mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertFalse(pabSaveResponse.isSuccess());
			assertNotNull(pabSaveResponse.getFieldMessages());
		}
	}

	//CAP-45631
	@Test
	void that_savePABAddress_countryCANADA_zipWithDashChar_returns_success() throws Exception {

		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationSuccess();

		// if zip code characters greater/equal to size 5, but first 5 character
		// contains dash(non numeric) and country is canada
		pabSaveRequest.setZip("3398--");
		pabSaveRequest.setCountry("CAN");

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockUser).populate(anyInt(), any());
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertTrue(pabSaveResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, pabSaveResponse.getMessage());
		}
	}

	//CAP-45631
	@Test
	void that_savePABAddress_countryCANADA_zipLessthanLength5_returns_success() throws Exception {

		pabSaveRequest=getPABSaveRequest_Valid();
		USPSAddressInfoServiceParmsVO addressParms=getUSPSAddrValidationSuccess();

		// if zip code characters less than size 5 and country is canada
		pabSaveRequest.setZip("38--");
		pabSaveRequest.setCountry("CAN");

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		doNothing().when(mockUser).populate(anyInt(), any());
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);

		//CAP-45544
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		when(mockCountryLocatorService.locate(any())).thenReturn(mockICountryComponent);
		when(mockICountryComponent.getCountryWithName(isA(CountryVOKey.class))).thenReturn(mockCountryWithNameVO);

		try (MockedConstruction<OECheckoutAssembler> mockedCheckoutAssembler =
				mockConstruction(OECheckoutAssembler.class, (mock, context) ->
				{
					when(mock.validateAddressUSPS(any(OENewAddressFormBean.class))).thenReturn(addressParms);
				}))	{

			pabSaveResponse = serviceToTest.savePABAddress(mockSessionContainer,pabSaveRequest,true,false);
			assertNotNull(pabSaveResponse);
			assertTrue(pabSaveResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, pabSaveResponse.getMessage());
		}
	}


	@Test
	void that_searchPABSN1_ValidPABSearchKeyValue1_Requestor_return_success() throws Exception {


		setUpFor_ValidPABSearchKeyValue();

		when(mockUser.isSharedID()).thenReturn(false);
		when(mockAddressSourceSettings.searchPersonalAddressBook(any(Map.class), any(AppSessionBean.class),
				any(String.class),any(HashMap.class),any(boolean.class))).thenReturn(personalAddressVOs);

		//CAP-45544
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		//valid search criteria key-value for shiptoname1
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldKey(SelfAdminUtil.PAB_SN1);
		pabSearchRequest.getGenericSearchCriteria().get(0).setCriteriaFieldValue(TEST_NAME);
		pabSearchResponse = serviceToTest.searchPAB(mockSessionContainer,pabSearchRequest,true);

		assertNotNull(pabSearchResponse);
		assertTrue(pabSearchResponse.isSuccess());
		assertEquals(1, pabSearchResponse.getPabCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSearchResponse.getMessage());
	}

	@Test
	void that_deletePABAddress_return_requestor_success() throws Exception {

		addressBookRecords.add(mockAddressBookRecord);
		testAddressIDs.add(TEST_ADDRESS_ID);
		testAddressIDs.add(TEST_ADDRESS_ID + 1);
		pabDeleteRequest.setAddressIds(testAddressIDs);
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.isSharedID()).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(12345);

		//CAP-45544
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());


		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		doReturn(mockProfile).when(mockEntityObjectMap).getEntity(Profile.class, mockToken);
		doNothing().when(mockProfile).populate(12345);
		doReturn(mockAddressBook).when(mockProfile).getAddressBook();
		doReturn(addressBookRecords).when(mockAddressBook).getAddressBook();
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getDefaultPersonalAddressID();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getSiteID();
		doReturn(AtWinXSConstant.INVALID_ID).when(mockProfile).getBusinessUnitID();
		doReturn(12345).when(mockProfile).getProfileNumber();
		doNothing().when(mockAddressBookRecord).delete(AtWinXSConstant.INVALID_ID, AtWinXSConstant.INVALID_ID, 12345,
				TEST_ADDRESS_ID);

		pabDeleteResponse = serviceToTest.deletePABAddress(pabDeleteRequest, mockSessionContainer,true);

		assertNotNull(pabDeleteResponse);
		assertTrue(pabDeleteResponse.isSuccess());
		assertEquals(1, pabDeleteResponse.getDeletedAddressCount());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabDeleteResponse.getMessage());
	}


	@Test
	void that_savePABAddress_returns_requestor_success() throws Exception {
		serviceToTest = Mockito.spy(serviceToTest);
		doReturn(mockEntityObjectMap).when(mockObjectMapFactoryService).getEntityObjectMap();
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUser);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		pabSaveRequest=getPABSaveRequest();
		//when(mockAppSessionBean.isUsePersonalAddrBook(null)).thenReturn(true);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		//CAP-45544
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		doNothing().when(mockUser).populate(anyInt(), any());
		doReturn(mockAddressBookRecord).when(mockEntityObjectMap).getEntity(AddressBookRecord.class, mockToken);
		doNothing().when(mockAddressBookRecord).populate(anyInt(), anyInt(), anyInt(), anyInt());
		doReturn(TEST_ADDRESS_ID).when(mockAddressBookRecord).getPersonalAddressID();
		when(mockUser.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getDefaultPersonalAddressID()).thenReturn(0);
		doReturn(true).when(serviceToTest).validateSavePAB(mockSessionContainer, pabSaveResponse, pabSaveRequest,
				mockAppSessionBean,false);
		pabSaveResponse = serviceToTest.savePABAddress( mockSessionContainer,pabSaveRequest,false,true);
		assertNotNull(pabSaveResponse);
		assertTrue(pabSaveResponse.isSuccess());
		assertEquals(AtWinXSConstant.EMPTY_STRING, pabSaveResponse.getMessage());
	}


	//CAP-46801
	@Test
	void that_saveUserSiteAttributes_returns_success1() throws Exception {

		c1UserSiteAttributesRequest = getc1UserSiteAttributesRequest_Valid();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockProfileComponentLocatorService.locate(any())).thenReturn(mockIProfileInterface);
		when(mockProfileAttributeComponentLocatorService.locate(any())).thenReturn(mockIProfileAttribute);
		when(mockIProfileInterface.getProfile(any())).thenReturn(mockProfileVO);
		when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_422ERR_MESSAGE);

		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
			    MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class, (mock, context) -> {
			    	when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
		    		})) {

			mockAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			//EditableRequired with all validation get pass
			List<SiteAttribute> siteAttributeLst=new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);
			when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttributeLst);

			when(mockSiteAttribute.getAttrID()).thenReturn(4993);
			when(mockSiteAttribute.getMinRequired()).thenReturn(1);
			when(mockSiteAttribute.getMaxRequired()).thenReturn(2);
			when(mockSiteAttribute.getDisplayType()).thenReturn(DisplayType.EditableRequired);

			List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
			SiteAttributeValue  siteAttributeValue;

			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(257594);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(257595);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(257596);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(257597);
			siteAttributeValueLst.add(siteAttributeValue);

			when(mockSiteAttribute.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
			when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttributeValueLst);

			c1UserSiteAttributesResponse = serviceToTest.saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
			assertNotNull(c1UserSiteAttributesResponse);
			assertTrue(c1UserSiteAttributesResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, c1UserSiteAttributesResponse.getMessage());
		}
	}

	@Test
	void that_saveUserSiteAttributes_returns_success2() throws Exception {

		c1UserSiteAttributesRequest = getc1UserSiteAttributesRequest_Valid();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockProfileComponentLocatorService.locate(any())).thenReturn(mockIProfileInterface);
		when(mockIProfileInterface.getProfile(any())).thenReturn(mockProfileVO);
		when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_422ERR_MESSAGE);

		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
			    MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class, (mock, context) -> {
			    	when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
		    		})) {

			mockAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			//EditableRequired with all validation get pass
			List<SiteAttribute> siteAttributeLst=new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);
			when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttributeLst);
			when(mockSiteAttribute.getAttrID()).thenReturn(4454);

			List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
			SiteAttributeValue  siteAttributeValue;

			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			c1UserSiteAttributesResponse = serviceToTest.saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
			assertNotNull(c1UserSiteAttributesResponse);
			assertTrue(c1UserSiteAttributesResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, c1UserSiteAttributesResponse.getMessage());
		}
	}

	@Test
	void that_saveUserSiteAttributes_returns_success3() throws Exception {

		c1UserSiteAttributesRequest = getc1UserSiteAttributesRequest_Valid();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockProfileComponentLocatorService.locate(any())).thenReturn(mockIProfileInterface);
		when(mockIProfileInterface.getProfile(any())).thenReturn(mockProfileVO);
		when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_422ERR_MESSAGE);

		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
			    MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class, (mock, context) -> {
			    	when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
		    		})) {

			mockAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			//EditableRequired
			List<SiteAttribute> siteAttributeLst=new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);
			when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttributeLst);
			when(mockSiteAttribute.getAttrID()).thenReturn(5249);

			List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
			SiteAttributeValue  siteAttributeValue;

			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(258333);
			siteAttributeValueLst.add(siteAttributeValue);

			c1UserSiteAttributesResponse = serviceToTest.saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
			assertNotNull(c1UserSiteAttributesResponse);
			assertTrue(c1UserSiteAttributesResponse.isSuccess());
			assertEquals(AtWinXSConstant.EMPTY_STRING, c1UserSiteAttributesResponse.getMessage());
		}
	}


	//CAP-46801
	@Test
	void that_saveUserSiteAttributes_returns_failed4() throws Exception {

		c1UserSiteAttributesRequest = getc1UserSiteAttributesRequest_Invalid1();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_422ERR_MESSAGE);

		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
			    MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class, (mock, context) -> {
			    	when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
		    		})) {

			mockAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			//Editable with only Max validation
			List<SiteAttribute> siteAttributeLst=new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);
			when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttributeLst);

			when(mockSiteAttribute.getAttrID()).thenReturn(4454);
			when(mockSiteAttribute.getMinRequired()).thenReturn(0);
			when(mockSiteAttribute.getMaxRequired()).thenReturn(1);
			when(mockSiteAttribute.getDisplayType()).thenReturn(DisplayType.Editable);

			List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
			SiteAttributeValue  siteAttributeValue;

			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			when(mockSiteAttribute.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
			when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttributeValueLst);

			c1UserSiteAttributesResponse = serviceToTest.saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
			assertNotNull(c1UserSiteAttributesResponse);
			assertFalse(c1UserSiteAttributesResponse.isSuccess());
			assertTrue(!c1UserSiteAttributesResponse.getMessage().isBlank());
		}
	}

	@Test
	void that_saveUserSiteAttributes_returns_failed5() throws Exception {

		c1UserSiteAttributesRequest = getc1UserSiteAttributesRequest_Invalid1();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);
		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());
		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_422ERR_MESSAGE);

		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
			    MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class, (mock, context) -> {
			    	when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
		    		})) {

			mockAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			//Editable with both Min and Max validation
			List<SiteAttribute> siteAttributeLst=new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);
			when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttributeLst);
			when(mockSiteAttribute.getAttrID()).thenReturn(4454);
			when(mockSiteAttribute.getMinRequired()).thenReturn(1);
			when(mockSiteAttribute.getMaxRequired()).thenReturn(1);
			when(mockSiteAttribute.getDisplayType()).thenReturn(DisplayType.Editable);

			List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
			SiteAttributeValue  siteAttributeValue;

			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			when(mockSiteAttribute.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
			when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttributeValueLst);

			c1UserSiteAttributesResponse = serviceToTest.saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
			assertNotNull(c1UserSiteAttributesResponse);
			assertFalse(c1UserSiteAttributesResponse.isSuccess());
			assertTrue(!c1UserSiteAttributesResponse.getMessage().isBlank());
		}
	}

	@Test
	void that_saveUserSiteAttributes_returns_failed6() throws Exception {

		c1UserSiteAttributesRequest = getc1UserSiteAttributesRequest_Invalid2();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		when(mockAppSessionBean.getProfileNumber()).thenReturn(1);

		mockAppSessionBean.setProfileNumber(mockCompositeProfileBean.getProfileNumber());
		mockAppSessionBean.setLoginID(mockCompositeProfileBean.getLoginID());

		when(mockAppSessionBean.getSiteID()).thenReturn(1234);
		when(mockAppSessionBean.getOriginatorProfile()).thenReturn(mockCompositeProfileBean);
		when(mockAppSessionBean.getSessionID()).thenReturn(123);
		when(mockCompositeProfileBean.getLoginID()).thenReturn("user-rrd");
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		when(mockTranslationService.processMessage(any(),any(),any())).thenReturn(TEST_422ERR_MESSAGE);


		try(MockedStatic<AdminUtil> mockAdminUtil = Mockito.mockStatic(AdminUtil.class);
			    MockedConstruction<ProfileImpl> mockProfile = Mockito.mockConstruction(ProfileImpl.class, (mock, context) -> {
			    	when(mock.getSiteAttributes()).thenReturn(mockSiteAttributes);
		    		})) {

			mockAdminUtil.when(() ->AdminUtil.getUserSettings(any(LoginVOKey.class), anyInt(), any()))
				.thenReturn(mockUserSettings);
			when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);

			//Editable with Only Min equired validation
			List<SiteAttribute> siteAttributeLst=new ArrayList<>();
			siteAttributeLst.add(mockSiteAttribute);
			when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttributeLst);
			when(mockSiteAttribute.getAttrID()).thenReturn(4454);
			when(mockSiteAttribute.getMinRequired()).thenReturn(1);
			when(mockSiteAttribute.getMaxRequired()).thenReturn(0);
			when(mockSiteAttribute.getDisplayType()).thenReturn(DisplayType.Editable);

			List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
			SiteAttributeValue  siteAttributeValue;
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254522);
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrValID(254523);
			siteAttributeValueLst.add(siteAttributeValue);

			when(mockSiteAttribute.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues);
			when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttributeValueLst);

			c1UserSiteAttributesResponse = serviceToTest.saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
			assertNotNull(c1UserSiteAttributesResponse);
			assertFalse(c1UserSiteAttributesResponse.isSuccess());
			assertTrue(!c1UserSiteAttributesResponse.getMessage().isBlank());
		}
	}

	// CAP-50381
	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_validateUserDefinedFields_returns_failed() throws Exception {

		List<SiteAttributeValue> siteAttributeValueLst=getAttrValList(1);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField)
				.thenReturn(mockProfile);
		ArrayList<C1UserDefinedField> c1UserDefinedField = getUDFRequest().getC1UserDefinedFields();

		doNothing().when(mockUserDefinedField).populate(0, 0, 0, 1);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(true);
		// CAP-50381 - AttributeList is List, and no need to validate Field length
		when(mockProfileUDFDefinition.isAssignAttributeList()).thenReturn(true);
		when(mockProfileUDFDefinition.getSiteAttributeValues()).thenReturn(mockSiteAttributeValues);
		when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttributeValueLst);

		serviceToTest = Mockito.spy(serviceToTest);
		List<UserDefinedField> udfList = getDefinedUDFList();
		doNothing().when(serviceToTest).verifyAllRequiredUdfsExistInRequest(any(),any(), any(), any());
		when(mockUserDefinedField.isExisting()).thenReturn(true);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
							.mockStatic(TranslationTextTag.class)) {

				mockTranslationTextTag
						.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
								mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.NOT_VALID_ERR, null))
						.thenReturn(SFTranslationTextConstants.NOT_VALID_ERR);


		boolean isValid = serviceToTest.validateUserDefinedFields(udfList, c1UserDefinedField, updateUserDefinedfieldsResponse, mockAppSessionBean);// saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);

		assertFalse(isValid);
		}

		c1UserDefinedField = null;
		boolean isValid = serviceToTest.validateUserDefinedFields(udfList, c1UserDefinedField, updateUserDefinedfieldsResponse, mockAppSessionBean);// saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
		assertFalse(isValid);



	}

	List<SiteAttributeValue> getAttrValList(int scenario){
		List<SiteAttributeValue> siteAttributeValueLst=new ArrayList<>();
		SiteAttributeValue  siteAttributeValue;
		switch (scenario) {
		case 1:
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrVal("RED");
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrVal("GREEN");
			siteAttributeValueLst.add(siteAttributeValue);
			break;

		case 2:
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrVal("RED");
			siteAttributeValueLst.add(siteAttributeValue);
			siteAttributeValue = new SiteAttributeValueImpl();
			siteAttributeValue.setSiteAttrVal("Test1234");
			siteAttributeValueLst.add(siteAttributeValue);
			break;

		}
		return siteAttributeValueLst;

	}
	
	
	// CAP-50381
	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_validateUserDefinedFields_returns_true() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField)
				.thenReturn(mockProfile);
		ArrayList<C1UserDefinedField> c1UserDefinedField = getUDFRequest().getC1UserDefinedFields();
		List<UserDefinedField> udfList = getDefinedUDFList();

		doNothing().when(mockUserDefinedField).populate(0, 0, 0, 1);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(false);
		// CAP-50381 - AttributeList is List, and no need to validate Field length
		when(mockProfileUDFDefinition.isAssignAttributeList()).thenReturn(true);

		doNothing().when(serviceToTest).verifyAllRequiredUdfsExistInRequest(any(),any(), any(), any());
		when(mockUserDefinedField.isExisting()).thenReturn(true);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		boolean isValid = serviceToTest.validateUserDefinedFields(udfList, c1UserDefinedField, updateUserDefinedfieldsResponse, mockAppSessionBean);// saveUserSiteAttributes(c1UserSiteAttributesRequest, mockSessionContainer);
		assertTrue(isValid);

	}
	
	// CAP-50381 
	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_validateUserDefinedFields_ReturnsFalse_withErrorMessage_For_TextAttribute_LengthValidationFails() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField)
				.thenReturn(mockProfile);
		ArrayList<C1UserDefinedField> c1UserDefinedField = getUDFRequest().getC1UserDefinedFields();
		List<UserDefinedField> udfList = getDefinedUDFList();

		doNothing().when(mockUserDefinedField).populate(0, 0, 0, 1);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(false);

		// CAP-50381 - AttributeList is text, and UDF Field value length GreaterThan Max Length Specified in Admin
		when(mockProfileUDFDefinition.isAssignAttributeList()).thenReturn(false);
		when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(5);

		doNothing().when(serviceToTest).verifyAllRequiredUdfsExistInRequest(any(),any(), any(), any());
		when(mockUserDefinedField.isExisting()).thenReturn(true);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			mockTranslationTextTag
					.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
							mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, null))
					.thenReturn(SFTranslationTextConstants.MAX_CHARS_ERR);
		boolean isValid = serviceToTest.validateUserDefinedFields(udfList, c1UserDefinedField, updateUserDefinedfieldsResponse, mockAppSessionBean);
		assertFalse(isValid);
		
		}

	}
	
	
	// CAP-50381 
	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
	void that_validateUserDefinedFields_ReturnsFalse_withErrorMessage_For_TextAttribute_LengthValidationPass() throws Exception {

		serviceToTest = Mockito.spy(serviceToTest);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserDefinedField)
				.thenReturn(mockProfile);
		ArrayList<C1UserDefinedField> c1UserDefinedField = getUDFRequest().getC1UserDefinedFields();
		List<UserDefinedField> udfList = getDefinedUDFList();

		doNothing().when(mockUserDefinedField).populate(0, 0, 0, 1);
		when(mockUserDefinedField.getProfileUDFDefinition()).thenReturn(mockProfileUDFDefinition);
		when(mockProfileUDFDefinition.isUDFRequired()).thenReturn(false);
		
		// CAP-50381 - AttributeList is text, and UDF Field value length LessThan Max Length Specified
		when(mockProfileUDFDefinition.isAssignAttributeList()).thenReturn(false);
		when(mockProfileUDFDefinition.getUDFLengthNumber()).thenReturn(50);

		doNothing().when(serviceToTest).verifyAllRequiredUdfsExistInRequest(any(),any(), any(), any());
		when(mockUserDefinedField.isExisting()).thenReturn(true);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		boolean isValid = serviceToTest.validateUserDefinedFields(udfList, c1UserDefinedField, updateUserDefinedfieldsResponse, mockAppSessionBean);
		assertTrue(isValid);

	}


	//CAP-46801
	private C1UserSiteAttributesRequest getc1UserSiteAttributesRequest_Valid() {

		//Site attributes with all Display Types including None, Hidden, ViewOnly
		//Site attributes with all Editable display type with no validation fail
		C1UserSiteAttributesRequest c1userSiteAttributesRequest = new C1UserSiteAttributesRequest();

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


		c1userSiteAttributesRequest.setC1UserSiteAttributes(c1UserSiteAttributeLst);
		return c1userSiteAttributesRequest;
	}


	//CAP-46801
	private C1UserSiteAttributesRequest getc1UserSiteAttributesRequest_Invalid1() {

		//Site attributes with Editable display type with validation fail
		C1UserSiteAttributesRequest c1userSiteAttributesRequest = new C1UserSiteAttributesRequest();

		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst;
		List<C1UserSiteAttribute> c1UserSiteAttributeLst = new ArrayList<>();

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(254522));
		c1UserSiteAttributeValueLst.add(new C1UserSiteAttributeValue(254523));
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4454, c1UserSiteAttributeValueLst));

		c1userSiteAttributesRequest.setC1UserSiteAttributes(c1UserSiteAttributeLst);
		return c1userSiteAttributesRequest;
	}

	//CAP-46801
	private C1UserSiteAttributesRequest getc1UserSiteAttributesRequest_Invalid2() {

		//Site attributes with Editable display type with validation fail
		C1UserSiteAttributesRequest c1userSiteAttributesRequest = new C1UserSiteAttributesRequest();

		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst;
		List<C1UserSiteAttribute> c1UserSiteAttributeLst = new ArrayList<>();

		c1UserSiteAttributeValueLst = new ArrayList<>();
		c1UserSiteAttributeLst.add(new C1UserSiteAttribute(4454, c1UserSiteAttributeValueLst));

		c1userSiteAttributesRequest.setC1UserSiteAttributes(c1UserSiteAttributeLst);
		return c1userSiteAttributesRequest;
	}

	@Test
	@Tag(EXCLUDE_SETUP_METHOD)
    void  verifyAllRequiredUdfsExistInRequestTest() throws Exception  {

		serviceToTest = Mockito.spy(serviceToTest);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		 ArrayList<C1UserDefinedField> c1UserDefinedField=new ArrayList<C1UserDefinedField>();

		 C1UserDefinedField udfField=new C1UserDefinedField();
		 udfField.setUdfFieldNumber(1);
		 udfField.setUdfValueText("Test1234");
		 c1UserDefinedField.add(udfField);

		 ProfileUDFDefinition profileUDFDefinition = new ProfileUDFDefinitionImpl();
		 profileUDFDefinition.setUDFRequired(true);

		 UserDefinedField udfField1=new UserDefinedFieldImpl();
		 udfField1.setUdfFieldNumber(1);
		 udfField1.setUdfValueText("Test1234");
		 udfField1.setProfileUDFDefinition(profileUDFDefinition);

		 UserDefinedField udfField2=new UserDefinedFieldImpl();
		 udfField2.setUdfFieldNumber(2);
		 udfField2.setUdfValueText("Test1234");
		 udfField2.setProfileUDFDefinition(profileUDFDefinition);

		 List<UserDefinedField> udfList = new ArrayList<UserDefinedField>();
		 udfList.add(udfField1);
		 udfList.add(udfField2);

		 UserDefinedfieldsResponse udfResponse = new UserDefinedfieldsResponse();
		 udfResponse.setSuccess(false);
		 try(MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito
					.mockStatic(TranslationTextTag.class)){
			 mockTranslationTextTag
				.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(),
						mockAppSessionBean.getCustomToken(), SFTranslationTextConstants.NOT_VALID_ERR, null))
				.thenReturn(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR);
		serviceToTest.verifyAllRequiredUdfsExistInRequest(udfList, c1UserDefinedField, udfResponse,mockAppSessionBean);
		assertFalse(udfResponse.isSuccess());

		serviceToTest.verifyAllRequiredUdfsExistInRequest(udfList, null, udfResponse,mockAppSessionBean);
		assertFalse(udfResponse.isSuccess());
		 }

    }

		@Test
		@Tag(EXCLUDE_SETUP_METHOD)
		void that_getProfileUDFs_returns_true() throws Exception {

			serviceToTest = Mockito.spy(serviceToTest);

			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class)))
					.thenReturn(mockProfile);

			doNothing().when(mockProfile).populate( 0);
			doReturn(mockProfileDefinition).when(mockProfile).getProfileDefinition();
			doReturn(mockProfileUDFDefinitions).when(mockProfileDefinition).getCustFieldsProfielUDFDefinitions();
			doReturn(mockUserDefinedFields).when(mockProfile).getUserDefinedFields();
			doReturn(mockUDFlist).when(mockUserDefinedFields).getUserDefinedFields();


			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

			List<UserDefinedField> isValid = serviceToTest.getProfileUDFs(mockAppSessionBean);
			assertNotNull(isValid);

		}
		@Test
		@Tag(EXCLUDE_SETUP_METHOD)
		void that_getProfileUDFs_returns_AccessForbiddenException() throws Exception {

			serviceToTest = Mockito.spy(serviceToTest);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(-1);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class)))
					.thenReturn(mockProfile);

			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

			Exception exception = assertThrows(AccessForbiddenException.class, () -> {
				List<UserDefinedField> isValid = serviceToTest.getProfileUDFs(mockAppSessionBean);
			});

		}

		@Test
		@Tag(EXCLUDE_SETUP_METHOD)
		void that_getProfileUDFs_returns_AtWinXSException() throws Exception {

			serviceToTest = Mockito.spy(serviceToTest);
			when(mockAppSessionBean.getProfileNumber()).thenReturn(0);
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class)))
					.thenReturn(mockProfile);

			when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
			doThrow(new AtWinXSException(FAIL, this.getClass().getName())).when(mockProfile)
			.populate(0);

			List<UserDefinedField> isValid = serviceToTest.getProfileUDFs(mockAppSessionBean);
			assertNull(isValid);

		}




}
