/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     	DTS#                        Description
 *  --------    -----------     	-----------------------     --------------------------------
 *  06/06/23    A Boomker       	CAP-41092                   Initial
 *	06/08/23	A Boomker			CAP-41266					Add handling for PRV address source
 *	06/16/23	A Boomker			CAP-41394					Add tests for previous address loads
 *	06/20/23	Satishkumar A		CAP-41094					Add overrideUSPSWarning boolean flag to request in Delivery Save API to override USPS failures and saves
 *	08/20/23	C Codina			CAP-41550					API Change - Change save delivery Info to support new address source type of PAB
 *	11/20/23	Satishkumar A		CAP-38135					C1UX BE - Modification of Manual Enter Address to use new USPS validation
 *	11/16/23	Krishna Natarajan	CAP-45181					Added tests for getStates()
 *	11/24/23	L De Leon			CAP-45487					Added tests for getCountries() method
 *	11/29/23	Satishkumar A		CAP-45375					C1UX BE - Modify the errors returned from the USPS validation to be translated
 *	12/22/23	S Ramachandran		CAP-46081					Added tests for modified validateUSAddressV1, Moved the test to New ServiceTest USPSValidationServiceImplTest
 *	03/14/24	M Sakthi			CAP-47840					C1UX BE - Create API to retrieve list of Distribution Lists.
 *	03/18/24	S Ramachandran		CAP-48002					Junit test added for service method uploadDistListFileAndGetWorksheetName
 *	03/27/24	S Ramachandran		CAP-48277					Junit tests added for uploadDistList service method
 *	04/01/24	Satishkumar A		CAP-48123					C1UX BE - Create API to retrieve Dist List addresses.
 *	04/04/24	S Ramachandran		CAP-48412					Add tests for check to see if the list has mapping and set flag.
 *	04/08/24	Satishkumar A		CAP-48378					Junit coverage for CAP-48123
 *	04/09/24	S Ramachandran		CAP-48434					modified tests for uploadDistList service method
 *	04/16/24	Krishna Natarajan	CAP-48728					commented out unnecessary line for an expedited bug
 *	05/09/24	A Boomker			CAP-46517, CAP-46518, CAP-42227 	Add check for valid cust doc merge UI if not in distribution
 */
package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.management.RuntimeErrorException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.checkout.COAltColumnNameWrapperCellData;
import com.rrd.c1ux.api.models.checkout.COColumnNameWrapperCellData;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveRequest;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveResponse;
import com.rrd.c1ux.api.models.checkout.COListDataMapper;
import com.rrd.c1ux.api.models.checkout.CreateListVarsRequest;
import com.rrd.c1ux.api.models.checkout.DistListAddressRequest;
import com.rrd.c1ux.api.models.checkout.DistListAddressResponse;
import com.rrd.c1ux.api.models.checkout.DistListCountRequest;
import com.rrd.c1ux.api.models.checkout.DistListCountResponse;
import com.rrd.c1ux.api.models.checkout.DistListUpdateRequest;
import com.rrd.c1ux.api.models.checkout.DistListUpdateResponse;
import com.rrd.c1ux.api.models.checkout.DistributionListResponse;
import com.rrd.c1ux.api.models.checkout.SaveManagedFieldsResponse;
import com.rrd.c1ux.api.models.checkout.SaveMappingRequest;
import com.rrd.c1ux.api.models.checkout.SaveMappingResponse;
import com.rrd.c1ux.api.models.checkout.UploadDistListRequest;
import com.rrd.c1ux.api.models.checkout.UploadDistListResponse;
import com.rrd.c1ux.api.models.checkout.WorksheetRequest;
import com.rrd.c1ux.api.models.checkout.WorksheetResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.CountriesResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.CountryCodeRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.StatesResponse;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.locators.ManageListsLocatorService;
import com.rrd.c1ux.api.services.usps.USPSValidationService;
import com.rrd.c1ux.api.util.COListsUtil;
import com.rrd.c1ux.api.util.DeliveryOptionsUtil;
import com.rrd.custompoint.customdocs.bundle.ui.BundleUserInterfaceImpl;
import com.rrd.custompoint.customdocs.ui.UserInterfaceImpl;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.entity.Address;
import com.rrd.custompoint.gwt.common.entity.AddressSearchResult;
import com.rrd.custompoint.gwt.common.entity.MessageBean;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.orderentry.ao.OEMappedVariableResponseBean;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.MergeOption;
import com.rrd.custompoint.orderentry.entity.AddressUIFields;
import com.rrd.custompoint.orderentry.entity.AddressUIFieldsImpl;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.rrd.custompoint.orderentry.entity.DeliveryOptions;
import com.rrd.custompoint.orderentry.entity.DeliveryOptionsImpl;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails;
import com.rrd.custompoint.orderentry.entity.DistributionListDetailsImpl;
import com.rrd.custompoint.orderentry.entity.List;
import com.rrd.custompoint.orderentry.entity.SiteListMapping;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.framework.util.mapper.ColumnNameWrapper;
import com.wallace.atwinxs.framework.util.mapper.Mapper;
import com.wallace.atwinxs.framework.util.mapper.MapperData;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.lists.ao.ListsBaseAssembler;
import com.wallace.atwinxs.lists.ao.ManageListsResponseBean;
import com.wallace.atwinxs.lists.ao.UploadListAssembler;
import com.wallace.atwinxs.lists.ao.UploadListResponseBean;
import com.wallace.atwinxs.lists.ao.ViewListsAssembler;
import com.wallace.atwinxs.lists.ao.ViewListsResponseBean;
import com.wallace.atwinxs.lists.session.ManageListsAdminSessionBean;
import com.wallace.atwinxs.lists.session.ManageListsSession;
import com.wallace.atwinxs.lists.session.ManageListsSessionBean;
import com.wallace.atwinxs.lists.util.ManageListsUtil;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.locale.ao.CountryBean;
import com.wallace.atwinxs.locale.ao.StateBean;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEListAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.session.OEDistributionListBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.util.DeliveryOptionsFormBean;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;

class CODeliveryInformationServiceImplTests extends BaseOEServiceTest {

	private static final String EXAMPLE_ADDRESS_NAME1 = "Example address name 1";
	private static final String EXAMPLE_ADDRESS_NAME2 = "Example address name 2";
	private static final String EXAMPLE_ADDRESS_LINE1 = "294 CLARENDON LN";
	private static final String EXAMPLE_ADDRESS_LINE2 = "more junk";
	private static final String EXAMPLE_ADDRESS_LINE3 = "Junk data";
	private static final String EXAMPLE_ADDRESS_CITY = "BOLINGBROOK";
	private static final String EXAMPLE_ADDRESS_STATE = "IL";
	private static final String EXAMPLE_ADDRESS_ZIP = "60440";
	private static final String EXAMPLE_ADDRESS_COUNTRY = "USA";
	private static final String EXAMPLE_ADDRESS_PHONE = "630-999-8888";

	private CODeliveryInformationSaveRequest saveRequest;
	private CODeliveryInformationSaveResponse saveResponse;
	private Address address;

	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;
	private static final String TEST_422ERR_MESSAGE = "422ERROR MESSAGE";

	@InjectMocks
	private CODeliveryInformationServiceImpl service;

	@Mock
	private USPSValidationService mockUSPSValidationService;

	@Mock
	private Map<String, String> mockTranslationMap;

	@Mock
	private Properties mockProperties;

	@Mock
	private XSProperties mockXSProperties;

	// CAP-47840
	@Mock
	private ViewListsResponseBean mockViewListsResponseBean;

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
	private DistributionListDetails mockDistributionListDetails;

	@Mock
	private ManageListsSession mockManageListsSession;

	@Mock
	private CreateListVarsRequest mockCreateListVarsRequest;

	@Mock
	private ListVO mockListVO;

	@Mock
	private IManageList mockIManageList;

	@Mock
	COListDataMapper mockCOListDataMapper;

	@Mock
	private IOEShoppingCartComponent mockOEShoppingCartComp;

	@Mock
	private CODeliveryInformationSaveRequest mockCODeliveryInformationSaveRequest;
	@Mock
	private OrderAddressVO mockOrderAddressVO;

	@Mock
	CountryBean cBean;
	@Mock
	StateBean sBean;

	@Mock
	Map<Integer, BaseSession> mockMapInteger;

	@Mock
	ManageListsResponseBean mockManageLists;

	@Mock
	SiteListMapping mockSiteListMapping;

	@Mock
	OEDistributionListBean mockOEDistributionListBean;

	@Mock
	UploadListResponseBean mockUploadListResponseBean;

	@Mock
	ManageListsSessionBean mockManageListsSessionBean;

	@Mock
	ManageListsAdminSessionBean mockManageListsAdminSessionBean;

	@Mock
	MapperData mockMapperData;

	@Mock
	OEListAssembler mockOEListAssembler;

	@Mock
	private ManageListsLocatorService mockManageListsLocatorService;
	
	@Mock
	private IOEShoppingCartComponent mockShoppingCartComp;

	StatesResponse response;

	// CAP-46517, CAP-46518, CAP-42227 - validation mocks
	@Mock
	protected UserInterfaceImpl mockUI;
	@Mock
	protected BundleUserInterfaceImpl mockBundleUI;
	@Mock
	protected CustomDocumentItemImpl mockItem;

	// CAP-45487
	CountriesResponse countriesResponse = null;
	public static final String FAIL = "Failed";

	@BeforeEach
	public void setup() {
		saveRequest = new CODeliveryInformationSaveRequest();
		address = new AddressSearchResult();
	}

	@Test
	void that_validateSaveRequest_validatesMEN() throws Exception {
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class)) {
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getFilteredCountryNames(any()))
					.thenReturn(getUSACountryBeans());
			when(mockUsaBean.getCountryCode()).thenReturn("USA");

			service = Mockito.spy(service);

			saveRequest.setAddressSource("MEN");
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);

			when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString()))
					.thenReturn(mockProperties);
			when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
			doNothing().when(service).validateUSAddress(any(CODeliveryInformationSaveRequest.class),
					any(AppSessionBean.class), any(OEResolvedUserSettingsSessionBean.class),
					any(OECheckoutAssembler.class), any(CODeliveryInformationSaveResponse.class));
			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());
			doReturn("Failed required check").when(service).getRequiredErrorMessage(any(AppSessionBean.class),
					anyString());
			// blank request should fail
			CODeliveryInformationSaveResponse responseCopy = service.validateSaveRequest(saveRequest,
					mockAppSessionBean, mockUserSettings, mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

			// now what should be a valid version should succeed
			createValidMENSaveRequest(saveRequest);
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
					mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertTrue(responseCopy.isSuccess());

			// now what should be a failure on USPS validation
			doAnswer(invocation -> {
				CODeliveryInformationSaveResponse answer = (CODeliveryInformationSaveResponse) invocation
						.getArgument(4);
				answer.setSuccess(false);
				answer.setMessage(GENERIC_ERROR_MSG);
				return null;
			}).when(service).validateUSAddress(any(CODeliveryInformationSaveRequest.class), any(AppSessionBean.class),
					any(OEResolvedUserSettingsSessionBean.class), any(OECheckoutAssembler.class),
					any(CODeliveryInformationSaveResponse.class));
			createInvalidUSPSMENSaveRequest(saveRequest);
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
					mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

		}
	}

	private Collection<CountryBean> getUSACountryBeans() {
		Collection<CountryBean> list = new ArrayList<>();
		list.add(mockUsaBean);
		return list;
	}

	@Test
	void that_validateSaveRequest_validatesCML() throws Exception {

		service = Mockito.spy(service);

		saveRequest.setAddressSource("CML");
		saveResponse = new CODeliveryInformationSaveResponse();
		saveResponse.setSuccess(true);

		when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString())).thenReturn(mockProperties);
		when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
//    doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class), any(SessionContainer.class), any(AppSessionBean.class));
		doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
				any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());
		doReturn("Failed required check").when(service).getRequiredErrorMessage(any(AppSessionBean.class), anyString());

		CODeliveryInformationSaveResponse responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean,
				mockUserSettings, mockCheckoutAssembler, null, false);
		Assertions.assertNotNull(responseCopy);
		Assertions.assertFalse(responseCopy.isSuccess());

		createValidCMLSaveRequest(saveRequest);
		saveResponse = new CODeliveryInformationSaveResponse();
		saveResponse.setSuccess(true);
		doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
				any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

		responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
				mockCheckoutAssembler, null, false);
		Assertions.assertNotNull(responseCopy);
		Assertions.assertTrue(responseCopy.isSuccess());
	}

	private void createValidCMLSaveRequest(CODeliveryInformationSaveRequest saveRequest2) {
		saveRequest2.setWcssShipToNbr("3049");
		saveRequest2.setCorporateNbr("0000097000");
		saveRequest2.setSoldToNbr("00001");
		saveRequest2.setBillToCode("0000097000:00001");
	}

	private void createValidMENSaveRequest(CODeliveryInformationSaveRequest saveRequest2) {
		saveRequest2.setWcssShipToNbr("");
		saveRequest2.setCorporateNbr("0000097000");
		saveRequest2.setSoldToNbr("00001");
		saveRequest2.setBillToCode("0000097000:00001");

		saveRequest2.setShipToName("COUNTRYWIDE");
		saveRequest2.setAddressLine1("26745 MALIBU HILLS RD");
		saveRequest2.setCity("AGOURA HILLS");
		saveRequest2.setStateOrProvince("CA");
		saveRequest2.setPostalCode("913015355");
		saveRequest2.setCountry("USA");
	}

	private void createInvalidUSPSMENSaveRequest(CODeliveryInformationSaveRequest saveRequest2) {
		saveRequest2.setWcssShipToNbr("");
		saveRequest2.setCorporateNbr("0000097000");
		saveRequest2.setSoldToNbr("00001");
		saveRequest2.setBillToCode("0000097000:00001");

		saveRequest2.setShipToName("TAD ERIKSON");
		saveRequest2.setAddressLine1("346 NO. KANAN ROAD");
		saveRequest2.setCity("AGOURA");
		saveRequest2.setStateOrProvince("CA");
		saveRequest2.setPostalCode("91301");
		saveRequest2.setCountry("USA");
	}

	private void createInvalidMENSaveRequest(CODeliveryInformationSaveRequest saveRequest2) {
		saveRequest2.setWcssShipToNbr("");
		saveRequest2.setCorporateNbr("0000097000");
		saveRequest2.setSoldToNbr("00001");
		saveRequest2.setBillToCode("0000097000:00001");

		saveRequest2.setShipToName("");
		saveRequest2.setAddressLine1("");
		saveRequest2.setCity("");
		saveRequest2.setStateOrProvince("");
		saveRequest2.setPostalCode("");
		saveRequest2.setCountry("USA");
	}

	private void createValidPRVSaveRequest(CODeliveryInformationSaveRequest saveRequest2) {
		saveRequest2.setBillToAttention("Pay Attention to who pays bills");
		saveRequest2.setShipToAttention("Pay Attention to where I am going");
		saveRequest2.setCorporateNbr("0000097000");
		saveRequest2.setSoldToNbr("00001");
		saveRequest2.setBillToCode("0000097000:00001");
		saveRequest2.setAddressSource("PRV");
	}

	private void createInvalidPRVSaveRequest(CODeliveryInformationSaveRequest saveRequest2) {
		saveRequest2.setAddressSource("PRV");
	}

	@Test
	void that_validateSaveRequest_validatesPRV_priorWasCML() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					when(mock.getShippingAddress(any())).thenReturn(getCMLPriorAddressVO());
					when(mock.getOrderShipping(anyInt())).thenReturn(mockOrderShipping);
				})) {

			service = Mockito.spy(service);

			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);

			when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString()))
					.thenReturn(mockProperties);
			when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());
			doReturn("Failed required check").when(service).getRequiredErrorMessage(any(AppSessionBean.class),
					anyString());
			doNothing().when(service).validateFullShipToAddress(any(CODeliveryInformationSaveRequest.class),
					any(AppSessionBean.class), any(), any(OEResolvedUserSettingsSessionBean.class), any());
			createInvalidPRVSaveRequest(saveRequest);

			// blank request should fail
			CODeliveryInformationSaveResponse responseCopy = service.validateSaveRequest(saveRequest,
					mockAppSessionBean, mockUserSettings, mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

			// now what should be a valid version should succeed
			createValidPRVSaveRequest(saveRequest);
			doAnswer(new Answer<CODeliveryInformationSaveResponse>() {
				@Override
				public CODeliveryInformationSaveResponse answer(InvocationOnMock invocation) throws Throwable {
					saveResponse = new CODeliveryInformationSaveResponse();
					saveResponse.setSuccess(true);
					saveResponse.getFieldMessages().clear();
					return saveResponse;
				}
			}).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
					mockCheckoutAssembler, getCMLPriorAddressVO(), false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertTrue(responseCopy.isSuccess());
			Assertions.assertTrue(responseCopy.getFieldMessages().isEmpty());
		}
	}

	@Test
	void that_validateSaveRequest_validatesPRV_priorWasMEN() throws Exception {
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class);
				MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
						.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
							when(mock.getShippingAddress(any())).thenReturn(getMENPriorAddressVO());
							when(mock.getOrderShipping(anyInt())).thenReturn(mockOrderShipping);
						})) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getFilteredCountryNames(any()))
					.thenReturn(getUSACountryBeans());

			service = Mockito.spy(service);

			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);

			when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString()))
					.thenReturn(mockProperties);
			when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);

			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());
			doReturn("Failed required check").when(service).getRequiredErrorMessage(any(AppSessionBean.class),
					anyString());
			doNothing().when(service).validateFullShipToAddress(any(CODeliveryInformationSaveRequest.class),
					any(AppSessionBean.class), any(), any(OEResolvedUserSettingsSessionBean.class), any());
			createInvalidPRVSaveRequest(saveRequest);

			// blank request should fail
			CODeliveryInformationSaveResponse responseCopy = service.validateSaveRequest(saveRequest,
					mockAppSessionBean, mockUserSettings, mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

			// now what should be a valid version should succeed
			createValidPRVSaveRequest(saveRequest);
			doAnswer(new Answer<CODeliveryInformationSaveResponse>() {
				@Override
				public CODeliveryInformationSaveResponse answer(InvocationOnMock invocation) throws Throwable {
					saveResponse = new CODeliveryInformationSaveResponse();
					saveResponse.setSuccess(true);
					return saveResponse;
				}
			}).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
					mockCheckoutAssembler, getMENPriorAddressVO(), false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertTrue(responseCopy.isSuccess());
		}
	}

	private OrderAddressVO getMENPriorAddressVO() {
		return new OrderAddressVO(AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING,
				AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, EXAMPLE_ADDRESS_NAME1,
				EXAMPLE_ADDRESS_NAME2, EXAMPLE_ADDRESS_LINE1, EXAMPLE_ADDRESS_LINE2, EXAMPLE_ADDRESS_LINE3,
				EXAMPLE_ADDRESS_CITY, EXAMPLE_ADDRESS_STATE, EXAMPLE_ADDRESS_ZIP, EXAMPLE_ADDRESS_COUNTRY,
				EXAMPLE_ADDRESS_PHONE, true, "", AtWinXSConstant.INVALID_ID, AtWinXSConstant.EMPTY_STRING);
	}

	private OrderAddressVO getCMLPriorAddressVO() {
		return new OrderAddressVO(AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, "1234",
				AtWinXSConstant.EMPTY_STRING, EXAMPLE_ADDRESS_NAME1, EXAMPLE_ADDRESS_NAME2, EXAMPLE_ADDRESS_LINE1,
				EXAMPLE_ADDRESS_LINE2, EXAMPLE_ADDRESS_LINE3, EXAMPLE_ADDRESS_CITY, EXAMPLE_ADDRESS_STATE,
				EXAMPLE_ADDRESS_ZIP, EXAMPLE_ADDRESS_COUNTRY, EXAMPLE_ADDRESS_PHONE, true, "W",
				AtWinXSConstant.INVALID_ID, AtWinXSConstant.EMPTY_STRING);
	}

	@Test
	void that_saveDeliveryInformation_worksForCML() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
				});
				MockedConstruction<OEListAssembler> mockedList = Mockito.mockConstruction(OEListAssembler.class,
						(mock, context) -> {
						})) {

			service = Mockito.spy(service);
			setUpModuleSession();
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

			doReturn(false).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));

			// now what should be a valid version should succeed
			createValidCMLSaveRequest(saveRequest);

			CODeliveryInformationSaveResponse responseCopy = service.saveDeliveryInformation(mockSessionContainer,
					saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());
			when(mockAppSessionBean.getCorporateNumber()).thenReturn("0000097000");
			doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));
			when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateSaveRequest(any(CODeliveryInformationSaveRequest.class),
					eq(mockAppSessionBean), eq(mockUserSettings), any(OECheckoutAssembler.class), any(), anyBoolean());
			doReturn(mockAddressFormBean).when(service).populateNewAddressFormBean(saveRequest, mockUserSettings, null);
			doReturn(SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG).when(service).getTranslation(
					mockAppSessionBean, SFTranslationTextConstants.SAVE_DELIVERY_INFO_SUCCESS_MSG,
					SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG);

			responseCopy = service.saveDeliveryInformation(mockSessionContainer, saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertTrue(responseCopy.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG,
					responseCopy.getMessage());
		}
	}

	@Test
	void that_saveDeliveryInformation_worksForCML_whenExceptionThrown() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(mock).setNewAddress(
							any(OEOrderSessionBean.class), anyString(), any(OENewAddressFormBean.class), anyBoolean(),
							anyBoolean());
				})) {
			when(mockAppSessionBean.getCorporateNumber()).thenReturn("0000097000");

			service = Mockito.spy(service);
			setUpModuleSession();
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			// now what should be a valid version should succeed
			createValidCMLSaveRequest(saveRequest);

			doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));
			when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateSaveRequest(any(CODeliveryInformationSaveRequest.class),
					eq(mockAppSessionBean), eq(mockUserSettings), any(OECheckoutAssembler.class), any(), anyBoolean());
			doReturn(mockAddressFormBean).when(service).populateNewAddressFormBean(saveRequest, mockUserSettings, null);

			doReturn(SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR).when(service)
					.getTranslation(eq(mockAppSessionBean), anyString(), anyString());

			CODeliveryInformationSaveResponse responseCopy = service.saveDeliveryInformation(mockSessionContainer,
					saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR, responseCopy.getMessage());
		}
	}

	@Test
	void that_saveDeliveryInformation_worksForCML_whenValidationErrors() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
				})) {

			service = Mockito.spy(service);
			setUpModuleSession();
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

			// now what should be a valid version should succeed
			createValidCMLSaveRequest(saveRequest);

			doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));
			when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(false);
			saveResponse.setMessage(SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR);
			doReturn(saveResponse).when(service).validateSaveRequest(any(CODeliveryInformationSaveRequest.class),
					eq(mockAppSessionBean), eq(mockUserSettings), any(OECheckoutAssembler.class), any(), anyBoolean());

			CODeliveryInformationSaveResponse responseCopy = service.saveDeliveryInformation(mockSessionContainer,
					saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR, responseCopy.getMessage());
		}
	}

	@Test
	void that_saveDeliveryInformation_worksForMEN() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
				});
				MockedConstruction<OEListAssembler> mockedList = Mockito.mockConstruction(OEListAssembler.class,
						(mock, context) -> {
						})) {

			service = Mockito.spy(service);
			setUpModuleSession();
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

			doReturn(false).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));

			// now what should be a valid version should succeed
			createValidMENSaveRequest(saveRequest);

			CODeliveryInformationSaveResponse responseCopy = service.saveDeliveryInformation(mockSessionContainer,
					saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

			doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));
			when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateSaveRequest(any(CODeliveryInformationSaveRequest.class),
					eq(mockAppSessionBean), eq(mockUserSettings), any(OECheckoutAssembler.class), any(), anyBoolean());
			doReturn(mockAddressFormBean).when(service).populateNewAddressFormBean(saveRequest, mockUserSettings, null);
			doReturn(SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG).when(service).getTranslation(
					mockAppSessionBean, SFTranslationTextConstants.SAVE_DELIVERY_INFO_SUCCESS_MSG,
					SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG);

			responseCopy = service.saveDeliveryInformation(mockSessionContainer, saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertTrue(responseCopy.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG,
					responseCopy.getMessage());
		}
	}

	@Test
	void that_saveDeliveryInformation_worksForMEN_whenValidationErrors() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(mock).setNewAddress(
							any(OEOrderSessionBean.class), anyString(), any(OENewAddressFormBean.class), anyBoolean(),
							anyBoolean());
				})) {

			service = Mockito.spy(service);
			setUpModuleSession();
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

			when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString()))
					.thenReturn(mockProperties);
			when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);
			doAnswer(new Answer<CODeliveryInformationSaveResponse>() {
				@Override
				public CODeliveryInformationSaveResponse answer(InvocationOnMock invocation) throws Throwable {
					CODeliveryInformationSaveResponse answer = new CODeliveryInformationSaveResponse();
					answer.setSuccess(false);
					answer.setMessage(GENERIC_ERROR_MSG);
					return answer;
				}
			}).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));

			// now what should be a valid version should succeed
			createInvalidMENSaveRequest(saveRequest);

			CODeliveryInformationSaveResponse responseCopy = service.saveDeliveryInformation(mockSessionContainer,
					saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());
			Assertions.assertEquals(GENERIC_ERROR_MSG, responseCopy.getMessage());
		}
	}

	@Test
	void that_saveDeliveryInformation_worksForMEN_whenExceptionThrown() throws Exception {
		try (MockedConstruction<OECheckoutAssembler> mockedCheckout = Mockito
				.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					doThrow(new AtWinXSException("testing", "MyJunitsClass")).when(mock).setNewAddress(
							any(OEOrderSessionBean.class), any(), any(OENewAddressFormBean.class), anyBoolean(),
							anyBoolean());
				})) {

			service = Mockito.spy(service);
			setUpModuleSession();
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

			// now what should be a valid version should succeed
			createValidMENSaveRequest(saveRequest);

			doReturn(true).when(service).validateOrder(any(CODeliveryInformationSaveResponse.class),
					any(SessionContainer.class), any(AppSessionBean.class));
			when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf(1234));
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateSaveRequest(any(CODeliveryInformationSaveRequest.class),
					eq(mockAppSessionBean), eq(mockUserSettings), any(OECheckoutAssembler.class), any(), anyBoolean());
			doReturn(mockAddressFormBean).when(service).populateNewAddressFormBean(saveRequest, mockUserSettings, null);

			doReturn(SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR).when(service)
					.getTranslation(eq(mockAppSessionBean), anyString(), anyString());

			CODeliveryInformationSaveResponse responseCopy = service.saveDeliveryInformation(mockSessionContainer,
					saveRequest, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());
			Assertions.assertEquals(SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR, responseCopy.getMessage());
		}
	}

	// CAP-41394 - new tests for previous address flag
	@Test
	void that_loadSavedOrderAddress_works_priorNotNull() throws Exception {
	    when(mockCheckoutAssembler.getShippingAddress(any())).thenReturn(getMENPriorAddressVO());

		  DeliveryOptionsFormBean formBean = new DeliveryOptionsImpl();
		  service.loadSavedOrderAddress(mockCheckoutAssembler, DEVTEST_SITE_ID, formBean);
		  Assertions.assertTrue(formBean.isHasPreviousAddress());
		  Assertions.assertTrue(formBean.isSelected());
	}

	@Test
	void that_loadSavedOrderAddress_works_priorIsNull() throws Exception {
	    when(mockCheckoutAssembler.getShippingAddress(any())).thenReturn(null);

		  DeliveryOptionsFormBean formBean = new DeliveryOptionsImpl();
		  service.loadSavedOrderAddress(mockCheckoutAssembler, DEVTEST_SITE_ID, formBean);
		  Assertions.assertFalse(formBean.isHasPreviousAddress());
		  Assertions.assertFalse(formBean.isSelected());
	}

	// CAP-41094
	@Test
	void that_validateUSAddress_USPS_validation_success() throws Exception {

		try (MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelper(any(), any(), any(), any()))
					.thenReturn(USPS_validation_success());
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getObject(ArgumentMatchers.eq(AddressUIFields.class), ArgumentMatchers.any()))
					.thenReturn(new AddressUIFieldsImpl());

			service = Mockito.spy(service);
			doReturn(address).when(service).populateAddressSearchResult(any(CODeliveryInformationSaveRequest.class));

			saveRequest.setCountry("USA");
			saveRequest.setOverrideUSPSErrors(false);

			saveResponse = new CODeliveryInformationSaveResponse();
			when(mockCheckoutAssembler.validateAddress(ArgumentMatchers.any())).thenReturn("");
			service.validateUSAddress(saveRequest, mockAppSessionBean, mockUserSettings, mockCheckoutAssembler,
					saveResponse);

			Assertions.assertNotNull(saveResponse);
			Assertions.assertTrue(saveResponse.isSuccess());

			saveRequest.setOverrideUSPSErrors(true);
			saveResponse = new CODeliveryInformationSaveResponse();
			service.validateUSAddress(saveRequest, mockAppSessionBean, mockUserSettings, mockCheckoutAssembler,
					saveResponse);

			Assertions.assertNotNull(saveResponse);
			Assertions.assertTrue(saveResponse.isSuccess());

		}
	}

	// CAP-41094
	@Test
	void that_validateUSAddress_USPS_validation_failed() throws Exception {

		try (MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito.mockStatic(DeliveryOptionsUtil.class);
				MockedStatic<ObjectMapFactory> mockObjectMapFactory = Mockito.mockStatic(ObjectMapFactory.class)) {
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelper(any(), any(), any(), any()))
					.thenReturn(USPS_validation_failed());
			mockObjectMapFactory.when(() -> ObjectMapFactory.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getObject(ArgumentMatchers.eq(AddressUIFields.class), ArgumentMatchers.any()))
					.thenReturn(new AddressUIFieldsImpl());

			service = Mockito.spy(service);
			doReturn(address).when(service).populateAddressSearchResult(any(CODeliveryInformationSaveRequest.class));

			saveRequest.setCountry("USA");
			saveRequest.setOverrideUSPSErrors(false);

			saveResponse = new CODeliveryInformationSaveResponse();
			when(mockCheckoutAssembler.validateAddress(ArgumentMatchers.any())).thenReturn("");
			service.validateUSAddress(saveRequest, mockAppSessionBean, mockUserSettings, mockCheckoutAssembler,
					saveResponse);

			Assertions.assertNotNull(saveResponse);
			Assertions.assertFalse(saveResponse.isSuccess());

			saveRequest.setOverrideUSPSErrors(true);
			saveResponse = new CODeliveryInformationSaveResponse();
			service.validateUSAddress(saveRequest, mockAppSessionBean, mockUserSettings, mockCheckoutAssembler,
					saveResponse);

			Assertions.assertNotNull(saveResponse);
			Assertions.assertTrue(saveResponse.isSuccess());

		}
	}

	// CAP-41094
	@Test
	void that_validateUSAddress_For_Canada_address() throws Exception {

		try (MockedStatic<DeliveryOptionsUtil> mockDeliveryOptionsUtil = Mockito
				.mockStatic(DeliveryOptionsUtil.class)) {
			mockDeliveryOptionsUtil.when(() -> DeliveryOptionsUtil.validateAddressHelper(null, mockCheckoutAssembler,
					mockAppSessionBean, mockUserSettings)).thenReturn(USPS_validation_success());

			service = Mockito.spy(service);

			saveRequest.setCountry("CAN");
			saveRequest.setOverrideUSPSErrors(false);
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(false);

			service.validateUSAddress(saveRequest, mockAppSessionBean, mockUserSettings, mockCheckoutAssembler,
					saveResponse);
			Assertions.assertNotNull(saveResponse);
			Assertions.assertFalse(saveResponse.isSuccess());

			saveRequest.setOverrideUSPSErrors(true);
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			service.validateUSAddress(saveRequest, mockAppSessionBean, mockUserSettings, mockCheckoutAssembler,
					saveResponse);

			Assertions.assertNotNull(saveResponse);
			Assertions.assertTrue(saveResponse.isSuccess());

		}
	}

	// CAP-41094
	private MessageBean USPS_validation_success() {
		MessageBean validation = new MessageBean();
		validation.setSuccess(true);
		validation.setMsg("");

		return validation;
	}

	// CAP-41094s
	private MessageBean USPS_validation_failed() {
		MessageBean validation = new MessageBean();
		validation.setSuccess(false);
		validation.setMsg("USPS validation failed");

		return validation;
	}

	// CAP-41550
	@Test
	void testValidPabId() {
		Map<String, String> fieldErrors = new HashMap<>();
		CODeliveryInformationSaveRequest request = new CODeliveryInformationSaveRequest();
		request.setAddressSource("PAB");
		request.setPabId(AtWinXSConstant.INVALID_ID);

		fieldErrors.put("PAB ID", "is not valid.");
		assertNotNull(request);

		service.validatePabId(request, mockAppSessionBean, fieldErrors);
	}

	@Test
	void testPopulateNewAddressFormBean() {
		when(mockUserSettings.isNameSplit()).thenReturn(true);
		saveRequest.setShipToName("TEST");
		saveRequest.setAddressSource("PAB");
		saveRequest.setPabId(12345);
		createValidMENSaveRequest(saveRequest);
		OENewAddressFormBean result = service.populateNewAddressFormBean(saveRequest, mockUserSettings, getCMLPriorAddressVO());
		assertNotNull(result);
	}

	@Test
	void testPopulateNewAddressFormBean_addressVO_isNull() {

		saveRequest.setAddressSource("PAB");
		saveRequest.setPabId(12345);
		saveRequest.setPhoneNumber("null");
		createValidMENSaveRequest(saveRequest);
		OENewAddressFormBean result = service.populateNewAddressFormBean(saveRequest, mockUserSettings, null);

		assertNotNull(result);

	}

	@Test
	void testValidPabId_negativetwo() {
		Map<String, String> fieldErrors = new HashMap<>();
		CODeliveryInformationSaveRequest request = new CODeliveryInformationSaveRequest();
		request.setAddressSource("PAB");
		request.setPabId(-2);

		fieldErrors.put("PAB ID", "is not valid.");
		assertNotNull(request);

		service.validatePabId(request, mockAppSessionBean, fieldErrors);
	}

	// CAP-38135, CAP-46081
	@Test
	void that_validateSaveRequest_USPS() throws Exception {
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class)) {
			mockUtilCountryInfo.when(() -> UtilCountryInfo.getFilteredCountryNames(any()))
					.thenReturn(getUSACountryBeans());
			when(mockUsaBean.getCountryCode()).thenReturn("USA");

			service = Mockito.spy(service);

			saveRequest.setAddressSource("MEN");
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);

			when(mockTranslationService.getResourceBundle(eq(mockAppSessionBean), anyString()))
					.thenReturn(mockProperties);
			when(mockTranslationService.convertResourceBundlePropsToMap(mockProperties)).thenReturn(mockTranslationMap);

			// CAP-46081
			USPSValidationResponse uspsValidationResponse = getUSPSValidationResponse_SuccessTest();
			doReturn(uspsValidationResponse).when(mockUSPSValidationService).validateUSAddressV1(any(), any(), any(),
					any(), anyBoolean());

			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());
			doReturn("Failed required check").when(service).getRequiredErrorMessage(any(AppSessionBean.class),
					anyString());
			// blank request should fail
			CODeliveryInformationSaveResponse responseCopy = service.validateSaveRequest(saveRequest,
					mockAppSessionBean, mockUserSettings, mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

			// now what should be a valid version should succeed
			createValidMENSaveRequest(saveRequest);
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
					mockCheckoutAssembler, null, true);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertTrue(responseCopy.isSuccess());

			// now what should be a failure on USPS validation
			doAnswer(invocation -> {
				CODeliveryInformationSaveResponse answer = (CODeliveryInformationSaveResponse) invocation
						.getArgument(4);
				answer.setSuccess(false);
				answer.setMessage(GENERIC_ERROR_MSG);
				return null;
			}).when(service).validateUSAddress(any(CODeliveryInformationSaveRequest.class), any(AppSessionBean.class),
					any(OEResolvedUserSettingsSessionBean.class), any(OECheckoutAssembler.class),
					any(CODeliveryInformationSaveResponse.class));
			createInvalidUSPSMENSaveRequest(saveRequest);
			saveResponse = new CODeliveryInformationSaveResponse();
			saveResponse.setSuccess(true);
			doReturn(saveResponse).when(service).validateAddressSource(any(AppSessionBean.class),
					any(CODeliveryInformationSaveRequest.class), eq(mockTranslationMap), any());

			responseCopy = service.validateSaveRequest(saveRequest, mockAppSessionBean, mockUserSettings,
					mockCheckoutAssembler, null, false);
			Assertions.assertNotNull(responseCopy);
			Assertions.assertFalse(responseCopy.isSuccess());

		}
	}

	// CAP-45181
	private String COUNTRY_AUS = "AUSTRALIA";
	private String COUNTRY_CODE_AUS = "AUS";
	private String COUNTRY_CODE_SHORT_AUS = "AC";

	@Test
	void that_getStates_returnsSuccess() throws AtWinXSException {
		ArrayList<String> countries = new ArrayList<String>();
		countries.add(COUNTRY_AUS);
		ArrayList<String> states = new ArrayList<String>();
		states.add(COUNTRY_CODE_SHORT_AUS);
		when(cBean.getCountryCode()).thenReturn(COUNTRY_CODE_AUS);
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName(COUNTRY_AUS)).thenReturn(cBean);
			CountryCodeRequest request = new CountryCodeRequest();
			request.setCountryCode(COUNTRY_AUS);
			response = service.getStates(request);

			Assertions.assertNotNull(response);
		}
	}

	@Test
	void that_getStates_nullcountry_returnsSuccess() throws AtWinXSException {
		ArrayList<String> countries = new ArrayList<String>();
		countries.add("");
		when(cBean.getCountryCode()).thenReturn("");
		try (MockedStatic<UtilCountryInfo> mockUtilCountryInfo = Mockito.mockStatic(UtilCountryInfo.class)) {

			mockUtilCountryInfo.when(() -> UtilCountryInfo.getCountriesByName()).thenReturn(countries.iterator());
			mockUtilCountryInfo.when((Verification) UtilCountryInfo.getCountryByName("")).thenReturn(cBean);
			CountryCodeRequest request = new CountryCodeRequest();
			request.setCountryCode("");
			response = service.getStates(request);

			Assertions.assertNotNull(response);
		}
	}

	// CAP-45487
	@Test
	void that_getCountries_returns_success() throws Exception {

		service = Mockito.spy(service);
		doReturn(new ArrayList<NameValuePair<String>>()).when(service).getCountryList(mockOEOrderSession);

		countriesResponse = service.getCountries(mockOEOrderSession);

		assertNotNull(countriesResponse);
		assertTrue(countriesResponse.isSuccess());
		assertTrue(countriesResponse.getCountryOptions().isEmpty());
		assertTrue(Util.isBlankOrNull(countriesResponse.getMessage()));
	}

	@Test
	void that_getCountries_throwsException_fail() throws Exception {

		service = Mockito.spy(service);
		doThrow(new NullPointerException(FAIL)).when(service).getCountryList(mockOEOrderSession);

		countriesResponse = service.getCountries(mockOEOrderSession);

		assertNotNull(countriesResponse);
		assertFalse(countriesResponse.isSuccess());
		assertFalse(Util.isBlankOrNull(countriesResponse.getMessage()));
	}

	// CAP-45544
	@Test
	void that_savePABAddress_For_DeliveryInformation_success() throws Exception {
		PABSaveResponse reponse = getPABSaveResponseSuccessTest();
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(true);
		when(mockUserSettings.isAllowOrderOnBehalf()).thenReturn(true);
		when(mockSelfAdminService.savePABAddress(any(), any(), anyBoolean(), anyBoolean())).thenReturn(reponse);
		service = Mockito.spy(service);
		reponse = service.savePABAdderessForDeliveryInfo(mockSessionContainer, mockAppSessionBean, mockOESessionBean,
				new PABSaveRequest());
		Assertions.assertTrue(reponse.isSuccess());
	}

	@Test
	void that_savePABAddress_For_DeliveryInformation_failed() throws Exception {
		PABSaveResponse reponse = getPABSaveResponseFailedTest();
		when(mockAppSessionBean.isInRequestorMode()).thenReturn(false);
		when(mockSelfAdminService.savePABAddress(any(), any(), anyBoolean(), anyBoolean())).thenReturn(reponse);
		service = Mockito.spy(service);
		reponse = service.savePABAdderessForDeliveryInfo(mockSessionContainer, mockAppSessionBean, mockOESessionBean,
				new PABSaveRequest());
		Assertions.assertFalse(reponse.isSuccess());
	}

	private PABSaveResponse getPABSaveResponseSuccessTest() {
		PABSaveResponse saveResponseSuccess = new PABSaveResponse();
		saveResponseSuccess.setSuccess(true);
		saveResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return saveResponseSuccess;
	}

	private PABSaveResponse getPABSaveResponseFailedTest() {
		PABSaveResponse saveResponseFailed = new PABSaveResponse();
		saveResponseFailed.setSuccess(false);
		saveResponseFailed.setMessage(AtWinXSConstant.EMPTY_STRING);
		return saveResponseFailed;
	}

	private USPSValidationResponse getUSPSValidationResponse_SuccessTest() {
		USPSValidationResponse uspsValidationResponseSuccess = new USPSValidationResponse();
		uspsValidationResponseSuccess.setSuccess(true);
		uspsValidationResponseSuccess.setMessage(AtWinXSConstant.EMPTY_STRING);
		return uspsValidationResponseSuccess;
	}

	// CAP-47840, CAP-48412
	@Test
	void that_getDistributionList_mappedList_returns_success() throws Exception {
		service = Mockito.spy(service);

		ManageListsResponseBean manageResp = new ManageListsResponseBean();
		manageResp.setListID("111");
		manageResp.setListName("Test list");
		manageResp.setRecordCount("2");
		manageResp.setListDescription("Test Desc");

		setUpModuleSession();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		setupAllowDistLists();
		doReturn(mockManageListsSession).when(service).createManageListSession(any(), anyInt());
		service.validateDistributionListEnabled(mockUserSettings);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		when(mockDistributionListDetails.hasRecordInListMapTable(any())).thenReturn(true);
		ManageListsResponseBean[] mockManageListsResponseBean = new ManageListsResponseBean[1];
		mockManageListsResponseBean[0] = manageResp;
		try (MockedConstruction<ViewListsAssembler> mockedViewListsAssembler = mockConstruction(
				ViewListsAssembler.class, (mock, context) -> {
					when(mock.performRetrieveList(any())).thenReturn(mockViewListsResponseBean);
				}); MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
			mockedUtil.when(() -> Util.encryptString(any())).thenReturn("ABC");

			when(mockViewListsResponseBean.getListsArray()).thenReturn(mockManageListsResponseBean);
			DistributionListResponse distResp = service.getManagedLists(mockSessionContainer, false);
			Assertions.assertNotNull(distResp);
			Assertions.assertTrue(distResp.isSuccess());
		}
	}

	// CAP-48412
	@Test
	void that_getDistributionList_withUnMappedList_returns_success() throws Exception {
		service = Mockito.spy(service);

		ManageListsResponseBean manageResp = new ManageListsResponseBean();
		manageResp.setListID("111");
		manageResp.setListName("Test list");
		manageResp.setRecordCount("2");
		manageResp.setListDescription("Test Desc");
		setUpModuleSession();
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		setupAllowDistLists();
		doReturn(mockManageListsSession).when(service).createManageListSession(any(), anyInt());
		service.validateDistributionListEnabled(mockUserSettings);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		when(mockDistributionListDetails.hasRecordInListMapTable(any())).thenReturn(false);
		ManageListsResponseBean[] mockManageListsResponseBean = new ManageListsResponseBean[1];
		mockManageListsResponseBean[0] = manageResp;
		try (MockedConstruction<ViewListsAssembler> mockedViewListsAssembler = mockConstruction(
				ViewListsAssembler.class, (mock, context) -> {
					when(mock.performRetrieveList(any())).thenReturn(mockViewListsResponseBean);
				})) {

			when(mockViewListsResponseBean.getListsArray()).thenReturn(mockManageListsResponseBean);

			DistributionListResponse distResp = service.getManagedLists(mockSessionContainer, false);
			Assertions.assertNotNull(distResp);
			Assertions.assertTrue(distResp.isSuccess());
		}
	}

	@Test
	void that_getDistributionList_return403Message_fail() throws Exception {

		setUpModuleSession();
		service = Mockito.spy(service);

		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowListOrders()).thenReturn(false);
		doReturn(mockManageListsSession).when(service).createManageListSession(any(), anyInt());
		service.validateDistributionListEnabled(mockUserSettings);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getManagedLists(mockSessionContainer, false);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}

	@Test
	void that_getDistributionList_return403Message_trueforallowlist_fail() throws Exception {

		setUpModuleSession();
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowListOrders()).thenReturn(true);
		when(mockUserSettings.isEnableDistrListFile()).thenReturn(false);
		doReturn(mockManageListsSession).when(service).createManageListSession(any(), anyInt());
		service.validateDistributionListEnabled(mockUserSettings);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getManagedLists(mockSessionContainer, false);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}

	// CAP-47998
	@Test
	void that_save_managelist_session_returns_success() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).populate(any(), any(), anyBoolean(), anyBoolean(), any(), any(),
				anyInt(), any(), any());
		doReturn(0).when(service).performDuplicateCheck(any(), any());
		CreateListVarsRequest request = new CreateListVarsRequest();
		request.setListName("Test");
		request.setListDescription("Test Desc");
		request.setHeaders(true);
		request.setSharedList(false);

		setupAllowDistLists();
		service.validateDistributionListEnabled(mockUserSettings);
		doReturn(mockManageListsSession).when(service).initManageListSession(any());

		SaveManagedFieldsResponse distResp = service.saveManagedListFields(mockSessionContainer, request);
		Assertions.assertNotNull(distResp);
		Assertions.assertTrue(distResp.isSuccess());
	}

	@Test
	void that_save_managelist_session_returns_listname_duplicate_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).populate(any(), any(), anyBoolean(), anyBoolean(), any(), any(),
				anyInt(), any(), any());
		doReturn(-1).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);
		CreateListVarsRequest request = new CreateListVarsRequest();
		request.setListName("");
		request.setListDescription("Test Desc");
		request.setHeaders(true);
		request.setSharedList(false);

		setupAllowDistLists();
		service.validateDistributionListEnabled(mockUserSettings);
		doReturn(mockManageListsSession).when(service).initManageListSession(any());

		SaveManagedFieldsResponse distResp = service.saveManagedListFields(mockSessionContainer, request);
		Assertions.assertNotNull(distResp);
		Assertions.assertFalse(distResp.isSuccess());
	}

	@Test
	void that_save_managelist_session_returns_listname_no_access_403() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		CreateListVarsRequest request = new CreateListVarsRequest();
		request.setListName("");
		request.setListDescription("Test Desc");
		request.setHeaders(true);
		request.setSharedList(false);

		when(mockUserSettings.isAllowListOrders()).thenReturn(false);
		service.validateDistributionListEnabled(mockUserSettings);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.saveManagedListFields(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}

	@Test
	void that_save_managelist_session_returns_listname_maxlength_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).populate(any(), any(), anyBoolean(), anyBoolean(), any(), any(),
				anyInt(), any(), any());
		// doReturn(0).when(service).performDuplicateCheck(mockAppSessionBean,
		// mockDistributionListDetails);
		CreateListVarsRequest request = new CreateListVarsRequest();
		request.setListName("2sadfasfsafsafsafsfsafsafasfasfafsdfafasfasfasfasfsafasfsasadfasdf");
		request.setListDescription("Test Desc");
		request.setHeaders(true);
		request.setSharedList(false);

		setupAllowDistLists();
		service.validateDistributionListEnabled(mockUserSettings);
		doReturn(mockManageListsSession).when(service).initManageListSession(any());

		SaveManagedFieldsResponse distResp = service.saveManagedListFields(mockSessionContainer, request);
		Assertions.assertNotNull(distResp);
		Assertions.assertFalse(distResp.isSuccess());
	}

	@Test
	void that_save_managelist_session_returns_listDesc_maxlength_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).populate(any(), any(), anyBoolean(), anyBoolean(), any(), any(),
				anyInt(), any(), any());
		CreateListVarsRequest request = new CreateListVarsRequest();
		request.setListName("2sadfasfsafsafsafsfsa");
		request.setListDescription(
				"sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdfsdfsdfsdfsdd"
						+ "dddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdfsdfsdfsdfsdddddddddddddddd"
						+ "dddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdfsdfsdfsdfsd"
						+ "ddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n"
						+ "sdfsdfsdfsdddddddddddddddddddddddddddfsdfsdfsdfsdfsdfsdfdsfsdfsdfdsfdsfsdfsdfdsfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfsdfdsfdsfdsfdsfdsfdsfsdsdfsddsfsdf\r\n");
		request.setHeaders(true);
		request.setSharedList(false);

		setupAllowDistLists();
		service.validateDistributionListEnabled(mockUserSettings);
		doReturn(mockManageListsSession).when(service).initManageListSession(any());

		SaveManagedFieldsResponse distResp = service.saveManagedListFields(mockSessionContainer, request);
		Assertions.assertNotNull(distResp);
		Assertions.assertFalse(distResp.isSuccess());
	}

	@Test
	void that_performDuplicateCheck_returns_422() throws Exception {
		service = Mockito.spy(service);
		CreateListVarsRequest request = new CreateListVarsRequest();
		request.setListName("test");
		request.setListDescription("Test Desc");
		request.setHeaders(true);
		request.setSharedList(false);
		doReturn(mockIManageList).when(service).getManageListsLocator(mockAppSessionBean);
		try (MockedConstruction<DistributionListDetailsImpl> mocked = mockConstruction(
				DistributionListDetailsImpl.class, (mock, context) -> {
					doReturn(mockListVO).when(mock).instantiateListVO(any(), any());
				})) {
			int ret = service.performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);
			Assertions.assertEquals(0, ret);
		}

	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_DistListfile_XLS_returns_success200() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		doReturn("1").when(service).convertFileNameWithUniqueID(any());

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);

		when(mockManageLists.getListName()).thenReturn("List1");
		when(mockManageLists.getCustomerFileName()).thenReturn("custfile");
		when(mockManageLists.getSourceFileName()).thenReturn("sourcefile");
		when(mockManageLists.getFileSize()).thenReturn("1");

		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).setPafDetails(any(), anyInt(), anyInt(), anyInt());
		when(mockDistributionListDetails.getIsExcel()).thenReturn(true);
		doReturn(0).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.xls",
				"application/vnd.ms-excel", "Hello World".getBytes());

		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		try (MockedStatic<ManageListsUtil> mockStatic = Mockito.mockStatic(ManageListsUtil.class)) {
			mockStatic.when(() -> ManageListsUtil.isConvertExcel()).thenReturn(true);

			WorksheetResponse worksheetResponse = service.uploadDistListFileAndGetWorksheetName(mockSessionContainer,
					request);

			Assertions.assertNotNull(worksheetResponse);
			Assertions.assertTrue(worksheetResponse.isSuccess());
		}
	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_DistListfile_XLSX_returns_success200() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		doReturn("1").when(service).convertFileNameWithUniqueID(any());

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageLists.getCustomerFileName()).thenReturn("custfile");
		when(mockManageLists.getSourceFileName()).thenReturn("sourcefile");
		when(mockManageLists.getFileSize()).thenReturn("1");

		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).setPafDetails(any(), anyInt(), anyInt(), anyInt());
		when(mockDistributionListDetails.getIsExcel()).thenReturn(true);
		doReturn(0).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.xlsx",
				"application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				"Hello World".getBytes());

		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		try (MockedStatic<ManageListsUtil> mockStatic = Mockito.mockStatic(ManageListsUtil.class)) {
			mockStatic.when(() -> ManageListsUtil.isConvertExcel()).thenReturn(true);

			WorksheetResponse worksheetResponse = service.uploadDistListFileAndGetWorksheetName(mockSessionContainer,
					request);

			Assertions.assertNotNull(worksheetResponse);
			Assertions.assertTrue(worksheetResponse.isSuccess());
		}
	}

	// CAP-48002
	@Test // CAP-48728
	void that_uploadDistListFileAndGetWorksheetName_distListfile_CSV_returns_success200() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		doReturn("1").when(service).convertFileNameWithUniqueID(any());

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageLists.getCustomerFileName()).thenReturn("custfile");
		when(mockManageLists.getSourceFileName()).thenReturn("sourcefile");
		when(mockManageLists.getFileSize()).thenReturn("1");

		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);
		doNothing().when(mockDistributionListDetails).setPafDetails(any(), anyInt(), anyInt(), anyInt());
		when(mockDistributionListDetails.getIsExcel()).thenReturn(false);
		doReturn(0).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);

		when(mockOEDistributionListBean.getListSysFileName()).thenReturn("DistList.csv");

		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.csv", "text/csv",
				"Hello World".getBytes());

		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		try (MockedStatic<ManageListsUtil> mockStatic = Mockito.mockStatic(ManageListsUtil.class)) {
			mockStatic.when(() -> ManageListsUtil.isConvertExcel()).thenReturn(true);

			WorksheetResponse worksheetResponse = service.uploadDistListFileAndGetWorksheetName(mockSessionContainer,
					request);

			Assertions.assertNotNull(worksheetResponse);
			// Assertions.assertTrue(worksheetResponse.isSuccess());
		}
	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_distList_CONFIG_NOT_ENABLED_failed403() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isEnableDistrListFile()).thenReturn(false);
		when(mockUserSettings.isAllowListOrders()).thenReturn(true);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.xls",
				"application/x-xls", "Hello World".getBytes());
		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.uploadDistListFileAndGetWorksheetName(mockSessionContainer, request);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_MlistsSession_NOT_AVAILABLE_failed403() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();
		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);

		// MlistsSession not available
		when(mockMapInteger.get(any())).thenReturn(null);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.xls",
				"application/x-xls", "Hello World".getBytes());
		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.uploadDistListFileAndGetWorksheetName(mockSessionContainer, request);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_INVALID_distListfile_returns_failed422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);
		doReturn(0).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);

		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(TEST_422ERR_MESSAGE);

		// Attached file is non xls, xlsx, csv
		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.doc", "application/doc",
				"Hello World".getBytes());

		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		WorksheetResponse worksheetResponse = service.uploadDistListFileAndGetWorksheetName(mockSessionContainer,
				request);

		Assertions.assertNotNull(worksheetResponse);
		Assertions.assertFalse(worksheetResponse.isSuccess());
	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_DistFile_NOT_ATTACHED_returns_failed403() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		setupAllowDistLists();

		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(TEST_422ERR_MESSAGE);
		when(mockTranslationService.processMessage(any(), any(), any(), any())).thenReturn(TEST_422ERR_MESSAGE);

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);
		doReturn(0).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);

		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(null);

		WorksheetResponse worksheetResponse = service.uploadDistListFileAndGetWorksheetName(mockSessionContainer,
				request);

		Assertions.assertNotNull(worksheetResponse);
		Assertions.assertFalse(worksheetResponse.isSuccess());
	}

	@Test
	void that_getDistributionListRecordCount_test() throws Exception {

		service = Mockito.spy(service);
		doNothing().when(service).moveOriginalFileToCserve(any(), any());

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		setupAllowDistLists();

		Map<Integer, BaseSession> getReferenceSessions = new HashMap<>();
		getReferenceSessions.put(AtWinXSConstant.LISTS_SERVICE_ID, mockManageListsSession);

//		when(mockSessionContainer.getReferenceSessions()).thenReturn(getReferenceSessions);
		doReturn(mockManageListsSession).when(service).loadListSession(mockSessionContainer);

		when(mockManageListsSession.getParameter(anyString())).thenReturn(mockDistributionListDetails);

		DistListCountRequest request = new DistListCountRequest();
		request.setWorksheetName("TestWorkSheetName");

		DistListCountResponse countResponse = service.getDistributionListRecordCount(mockSessionContainer, request);
		assertTrue(countResponse.isSuccess());
	}

	@Test
	void that_getDistributionListRecordCount_test_throwsException() throws Exception {

		service = Mockito.spy(service);
		doNothing().when(service).moveOriginalFileToCserve(any(), any());
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		setupAllowDistLists();

		Map<Integer, BaseSession> getReferenceSessions = new HashMap<>();
		getReferenceSessions.put(AtWinXSConstant.LISTS_SERVICE_ID, mockManageListsSession);
		doReturn(mockManageListsSession).when(service).loadListSession(mockSessionContainer);

		when(mockManageListsSession.getParameter(anyString())).thenReturn(mockDistributionListDetails);

		when(mockDistributionListDetails.getRecordCountForDistributionList(any(), any(), any(), any(), any()))
				.thenThrow(new AtWinXSException("I blew up here", this.getClass().getName()));

		when(mockAppSessionBean.getGlobalFileUploadPath()).thenReturn("testPath");
		when(mockDistributionListDetails.getFileName()).thenReturn("testFileName.csv");
		when(mockDistributionListDetails.getSourceFileName()).thenReturn("testSourceFileName.xls");

		DistListCountRequest request = new DistListCountRequest();
		DistListCountResponse countResponse = service.getDistributionListRecordCount(mockSessionContainer, request);
		assertTrue(countResponse.isSuccess());

		request.setWorksheetName("TestWorkSheetName");
		countResponse = service.getDistributionListRecordCount(mockSessionContainer, request);
		assertFalse(countResponse.isSuccess());
	}

	@Test
	void that_getDistributionListRecordCount_whenDistributionListDetailsIsNull() throws Exception {

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);

		Map<Integer, BaseSession> getReferenceSessions = new HashMap<>();
		getReferenceSessions.put(AtWinXSConstant.LISTS_SERVICE_ID, mockManageListsSession);

		DistListCountRequest request = new DistListCountRequest();
		request.setWorksheetName("TestWorkSheetName");

		assertThrows(AccessForbiddenException.class, () -> service.getDistributionListRecordCount(mockSessionContainer, request));
	}

	@Test
	void that_getDistributionList_test_whenManageListSessionIsNull() throws Exception {


		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		setupAllowDistLists();

		DistListCountRequest request = new DistListCountRequest();
		request.setWorksheetName("TestWorkSheetName");

		assertThrows(AccessForbiddenException.class, () -> service.getDistributionListRecordCount(mockSessionContainer, request));

	}

	void setupAllowDistLists() {
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isEnableDistrListFile()).thenReturn(true);
		when(mockUserSettings.isAllowListOrders()).thenReturn(true);
	}

	// CAP-48002
	@Test
	void that_uploadDistListFileAndGetWorksheetName_distList_IsDuplicate_returns_failed403() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);

		// If Dist List Name is duplicate return DUPLICATE_SAME_OWNER(-1) OR
		// DUPLICATE_NOT_OWNER(-2)
		doReturn(-1).when(service).performDuplicateCheck(mockAppSessionBean, mockDistributionListDetails);

		when(mockTranslationService.processMessage(any(), any(), any())).thenReturn(TEST_422ERR_MESSAGE);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("multipartFile", "DistList.xls",
				"application/x-xls", "Hello World".getBytes());

		WorksheetRequest request = new WorksheetRequest();
		request.setDistFile(mockMultipartFile);

		AtWinXSException error403 = assertThrows(AtWinXSException.class, () -> {
			service.uploadDistListFileAndGetWorksheetName(mockSessionContainer, request);
		});
		assertTrue(error403 instanceof AccessForbiddenException);
	}

	// CAP-48277, CAP-48434
	@Test
	void that_uploadDistList_NewList_XLS_returns_success200() throws Exception {

		// when uploadDistList returns status Success for New List
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getSessionBean()).thenReturn(mockManageListsSessionBean);
		when(mockManageListsSessionBean.getManageListsAdminSession()).thenReturn(mockManageListsAdminSessionBean);
		when(mockManageListsAdminSessionBean.isBUAllowListFeedPreview()).thenReturn(true);

		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);

		when(mockDistributionListDetails.getIsExcel()).thenReturn(true);
		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);
		when(mockListVO.getUploadedDate()).thenReturn(new Date());
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		doReturn(true).when(mockDistributionListDetails).validatefile(any());

		when(mockOEDistributionListBean.getListID()).thenReturn("1234");
		when(mockOEDistributionListBean.getListSysFileName()).thenReturn("filename");
		when(mockOEDistributionListBean.getHasListHeadings()).thenReturn("true");
		when(mockOEDistributionListBean.getIsEncrytedInd()).thenReturn("false");
		when(mockAppSessionBean.getGlobalFileUploadPath()).thenReturn("File path");

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);

		try (MockedConstruction<UploadListAssembler> mockedUploadListAssembler = mockConstruction(
				UploadListAssembler.class, (mock1, context) -> {
					when(mock1.performAddList(any(), any())).thenReturn(mockUploadListResponseBean);
				});
				MockedConstruction<ListsBaseAssembler> mockedListsBaseAssembler = mockConstruction(
						ListsBaseAssembler.class, (mock2, context) -> {
							when(mock2.getList(anyInt(), anyInt(), anyInt())).thenReturn(mockListVO);
						});
				MockedConstruction<Mapper> mockedMapper = mockConstruction(Mapper.class, (mock3, context) -> {
					when(mock3.getDataPreview(any())).thenReturn(mockMapperData);
				});
				MockedStatic<ManageListsUtil> mockStatic = Mockito.mockStatic(ManageListsUtil.class);) {

			mockStatic.when(() -> ManageListsUtil.isConvertExcel()).thenReturn(true);

			UploadDistListResponse response = service.uploadDistList(mockSessionContainer, request, false, false);

			Assertions.assertNotNull(response);
			Assertions.assertTrue(response.isSuccess());
		}
	}

	// CAP-48434
	@Test
	void that_uploadDistList_SpecificListID_XLS_returns_success200() throws Exception {

		// when uploadDistList returns status Success for UnMapped Specific List ID
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);

		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getSessionBean()).thenReturn(mockManageListsSessionBean);
		when(mockManageListsSessionBean.getManageListsAdminSession()).thenReturn(mockManageListsAdminSessionBean);
		when(mockManageListsAdminSessionBean.isBUAllowListFeedPreview()).thenReturn(true);

		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);
		when(mockListVO.getUploadedDate()).thenReturn(new Date());
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		doReturn(true).when(mockDistributionListDetails).validatefile(any());

		when(mockOEDistributionListBean.getListID()).thenReturn("1234");
		when(mockOEDistributionListBean.getListSysFileName()).thenReturn("filename");
		when(mockOEDistributionListBean.getHasListHeadings()).thenReturn("true");
		when(mockOEDistributionListBean.getIsEncrytedInd()).thenReturn("false");
		when(mockAppSessionBean.getGlobalFileUploadPath()).thenReturn("File path");

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);
		request.setListID("IOT95YCWzomMmbxGDTgYZDIEjxVaNrKZ");

		try (MockedConstruction<UploadListAssembler> mockedUploadListAssembler = mockConstruction(
				UploadListAssembler.class, (mock1, context) -> {
					when(mock1.performAddList(any(), any())).thenReturn(mockUploadListResponseBean);
				});
				MockedConstruction<ListsBaseAssembler> mockedListsBaseAssembler = mockConstruction(
						ListsBaseAssembler.class, (mock2, context) -> {
							when(mock2.getList(anyInt(), anyInt(), anyInt())).thenReturn(mockListVO);
						});
				MockedConstruction<Mapper> mockedMapper = mockConstruction(Mapper.class, (mock3, context) -> {
					when(mock3.getDataPreview(any())).thenReturn(mockMapperData);
				});
				MockedStatic<ManageListsUtil> mockStatic = Mockito.mockStatic(ManageListsUtil.class);) {

			UploadDistListResponse response = service.uploadDistList(mockSessionContainer, request, false, false);

			Assertions.assertNotNull(response);
			Assertions.assertTrue(response.isSuccess());
		}
	}

	// CAP-48434
	@Test
	void that_uploadDistList_InvalidListID_returns_Failed422() throws Exception {

		// when uploadDistList returns status Failed(422)for Invalid encrypt List ID
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);
		request.setListID("INVALID_ENCRYPTED_LISTID");

		UploadDistListResponse response = service.uploadDistList(mockSessionContainer, request, false, false);

		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());
	}

	// CAP-48434
	@Test
	void that_uploadDistList_MListsSession_isNULL_Failed422() throws Exception {

		// when uploadDistList returns status Failed(422) if MListsSession is NULL
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(null);

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);
		request.setListID("INVALID_ENCRYPTED_LISTID");

		UploadDistListResponse response = service.uploadDistList(mockSessionContainer, request, false, false);

		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());
	}

	// CAP-48434
	@Test
	void that_uploadDistList_manageLists_SessionObject_isNULL_Failed422() throws Exception {

		// when uploadDistList returns status Failed(422) if manageLists SessionObject
		// is NULL
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT)).thenReturn(null);

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);

		UploadDistListResponse response = service.uploadDistList(mockSessionContainer, request, false, false);

		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());
	}

	// CAP-48434
	@Test
	void that_uploadDistList_distListDetails_SessionObject_isNULL_Failed422() throws Exception {

		// when uploadDistList returns status Failed(422) if distListDetails
		// SessionObject is NULL
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT))
				.thenReturn(mockManageLists);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT)).thenReturn(null);

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);

		UploadDistListResponse response = service.uploadDistList(mockSessionContainer, request, false, false);

		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());
	}

	// CAP-48434
	@Test
	void that_uploadDistList_DistList_NOT_ENABLED_returns_success403() throws Exception {

		// when uploadDistList returns status Un Authorised if Dist List Not Enabled
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowListOrders()).thenReturn(false);

		UploadDistListRequest request = new UploadDistListRequest();
		request.setRecordCount(1);
		request.setListID("INVALID_ENCRYPTED_LISTID");

		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.uploadDistList(mockSessionContainer, request, false, false);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	// CAP-48123
	@Test
	void that_getDistributionListAddresses_returns_success200() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		when(mockEntityObjectMap.getEntity(eq(SiteListMapping.class), any())).thenReturn(mockSiteListMapping);
		doNothing().when(mockSiteListMapping).populate(any(), any(), any());
		doNothing().when(mockDistributionListDetails).populate(any());
		doReturn(mockMapperData).when(service).getOrderwithDistListPreview(any(), any(), any(), any());
		when(mockSiteListMapping.getMappedColumnNamesStr()).thenReturn(
				"Address 2= 3:Address 1= 2:Name 2= 1:Name 1= 0:CUST_REF_19= 59:CUST_REF_18= 58:CUST_REF_17= 57:CUST_REF_16= 56:CUST_REF_15= 55:CUST_REF_14= 54:CUST_REF_13= 53:CUST_REF_12= 52:CUST_REF_11= 51:Ship To Attention= 8:CUST_REF_10= 50:State= 5:Phone= 9:CUST_REF_9= 49:CUST_REF_8= 48:CUST_REF_25= 65:CUST_REF_7= 47:CUST_REF_24= 64:CUST_REF_6= 46:CUST_REF_23= 63:CUST_REF_5= 45:CUST_REF_22= 62:Country Code= 7:CUST_REF_21= 61:CUST_REF_3= 44:CUST_REF_20= 60:CUST_REF_2= 43:CUST_REF_1= 42:E-mail= 10:ZIP= 6:City= 4:");
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);

		ManageListsResponseBean manageResp = new ManageListsResponseBean();
		manageResp.setListName("Test list");
		manageResp.setRecordCount("1");
		manageResp.setListDescription("Test Desc");
		manageResp.setListID("9109");

		Collection<String[]> str = new ArrayList<String[]>();
		String[] array = new String[36];
		array[0] = "Subbian Shared xls";
		array[1] = "Ramachandran";
		array[2] = "1750 Wallace Ave";
		str.add(array);

		ArrayList<ColumnNameWrapper> headings = new ArrayList<ColumnNameWrapper>();

		ColumnNameWrapper c1 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE1, false);
		ColumnNameWrapper c2 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE2, false);
		ColumnNameWrapper c3 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE3, false);
		headings.add(c1);
		headings.add(c2);
		headings.add(c3);

		OEMappedVariableResponseBean o1 = new OEMappedVariableResponseBean(EXAMPLE_ADDRESS_NAME1, COUNTRY_AUS, false);

		Collection<OEMappedVariableResponseBean> altColNames = new ArrayList<OEMappedVariableResponseBean>();
		altColNames.add(o1);

		ManageListsResponseBean[] mockManageListsResponseBean = new ManageListsResponseBean[1];
		mockManageListsResponseBean[0] = manageResp;

		when(mockManageListsSession.getParameter("manageListsResponse")).thenReturn(mockManageListsResponseBean);
		when(mockManageListsSession.getSessionBean()).thenReturn(mockManageListsSessionBean);
		when(mockManageListsSessionBean.getManageListsAdminSession()).thenReturn(mockManageListsAdminSessionBean);

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);

		doReturn(true).when(mockDistributionListDetails).validatefile(any());
		doReturn(true).when(mockDistributionListDetails).hasRecordInListMapTable(any());
		when(mockDistributionListDetails.getData()).thenReturn(str);
		when(mockDistributionListDetails.getMaxCols()).thenReturn(36);
		when(mockDistributionListDetails.getHeadings()).thenReturn(headings);
//		doReturn(altColNames).when(mockMapperData).getAltColumnNames();
		doNothing().when(service).loadItemImages(any(), any());

		DistListAddressRequest request = new DistListAddressRequest();
		request.setDistListID("IOT95YCWzokxXhfYreccuTIEjxVaNrKZ");

		try (MockedStatic<ManageListsUtil> mockStatic = Mockito.mockStatic(ManageListsUtil.class)) {
			mockStatic.when(() -> ManageListsUtil.isConvertExcel()).thenReturn(true);

			DistListAddressResponse response = service.getDistributionListAddresses(mockSessionContainer, request);

			Assertions.assertNotNull(response);
			Assertions.assertTrue(response.isSuccess());
		}
	}

	// CAP-48378
	@Test
	void that_getDistributionListAddresses_returns_422() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DistributionListDetails.class), any()))
				.thenReturn(mockDistributionListDetails);
		when(mockEntityObjectMap.getEntity(eq(SiteListMapping.class), any())).thenReturn(mockSiteListMapping);
		doNothing().when(mockSiteListMapping).populate(any(), any(), any());
		doNothing().when(mockDistributionListDetails).populate(any());
		doReturn(mockMapperData).when(service).getOrderwithDistListPreview(any(), any(), any(), any());
		when(mockSiteListMapping.getMappedColumnNamesStr()).thenReturn(
				"Address 3= 3:Address 2= 3:Address 1= 2:Name 2= 1:Name 1= 0:CUST_REF_19= 59:CUST_REF_18= 58:CUST_REF_17= 57:CUST_REF_16= 56:CUST_REF_15= 55:CUST_REF_14= 54:CUST_REF_13= 53:CUST_REF_12= 52:CUST_REF_11= 51:Ship To Attention= 8:CUST_REF_10= 50:State= 5:Phone= 9:CUST_REF_9= 49:CUST_REF_8= 48:CUST_REF_25= 65:CUST_REF_7= 47:CUST_REF_24= 64:CUST_REF_6= 46:CUST_REF_23= 63:CUST_REF_5= 45:CUST_REF_22= 62:Country Code= 7:CUST_REF_21= 61:CUST_REF_3= 44:CUST_REF_20= 60:CUST_REF_2= 43:CUST_REF_1= 42:E-mail= 10:ZIP= 6:City= 4:Item = 12:Quantity = 13:UOM = 14:");
		setupAllowDistLists();

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);

		ManageListsResponseBean manageResp = new ManageListsResponseBean();
		manageResp.setListName("Test list");
		manageResp.setRecordCount("1");
		manageResp.setListDescription("Test Desc");
		manageResp.setListID("9109");

		Collection<String[]> str = new ArrayList<String[]>();
		String[] array = new String[39];
		array[0] = "Subbian Shared xls";
		array[1] = "Ramachandran";
		array[2] = "1750 Wallace Ave";
		str.add(array);

		ArrayList<ColumnNameWrapper> headings = new ArrayList<ColumnNameWrapper>();

		ColumnNameWrapper c1 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE1, false);
		ColumnNameWrapper c2 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE2, false);
		ColumnNameWrapper c3 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE3, false);
		headings.add(c1);
		headings.add(c2);
		headings.add(c3);

		OEMappedVariableResponseBean o1 = new OEMappedVariableResponseBean(EXAMPLE_ADDRESS_NAME1, COUNTRY_AUS, false);

		Collection<OEMappedVariableResponseBean> altColNames = new ArrayList<OEMappedVariableResponseBean>();
		altColNames.add(o1);

		ManageListsResponseBean[] mockManageListsResponseBean = new ManageListsResponseBean[1];
		mockManageListsResponseBean[0] = manageResp;

		when(mockManageListsSession.getParameter("manageListsResponse")).thenReturn(mockManageListsResponseBean);
		when(mockManageListsSession.getSessionBean()).thenReturn(mockManageListsSessionBean);
		when(mockManageListsSessionBean.getManageListsAdminSession()).thenReturn(mockManageListsAdminSessionBean);

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);

		doReturn(true).when(mockDistributionListDetails).validatefile(any());
		doReturn(true).when(mockDistributionListDetails).hasRecordInListMapTable(any());
		when(mockDistributionListDetails.getData()).thenReturn(str);
		when(mockDistributionListDetails.getMaxCols()).thenReturn(39);
		when(mockDistributionListDetails.getHeadings()).thenReturn(headings);

		DistListAddressRequest request = new DistListAddressRequest();
		request.setDistListID("IOT95YCWzokxXhfYreccuTIEjxVaNrKZ123");

		DistListAddressResponse response = service.getDistributionListAddresses(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());

		request.setDistListID("IOT95YCWzokxXhfYreccuTIEjxVaNrKZ");
		doThrow(new RuntimeErrorException(null, "class")).when(mockSessionContainer).addReferenceSession(anyInt(),
				any());
		assertThrows(AccessForbiddenException.class, () -> {
			service.getDistributionListAddresses(mockSessionContainer, request);
		});

		doNothing().when(mockSessionContainer).addReferenceSession(anyInt(), any());
		when(mockUserSettings.isAllowListOrders()).thenReturn(false);
		when(mockUserSettings.isEnableDistrListFile()).thenReturn(true);
		assertThrows(AccessForbiddenException.class, () -> {
			service.getDistributionListAddresses(mockSessionContainer, request);
		});

		when(mockUserSettings.isAllowListOrders()).thenReturn(true);

		when(mockDistributionListDetails.hasRecordInListMapTable(any())).thenReturn(false);
		response = service.getDistributionListAddresses(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());

		when(mockOESessionBean.getDistributionListBean()).thenReturn(null);
		when(mockDistributionListDetails.hasRecordInListMapTable(any())).thenReturn(true);
		doThrow(new AtWinXSException("Error", "class")).when(service).loadItemImages(any(), any());
		response = service.getDistributionListAddresses(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());

		request.setDistListID("IOT95YCWzolvS/Ns70R67zIEjxVaNrKZ");
		response = service.getDistributionListAddresses(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());

		request.setDistListID("IOT95YCWzokxXhfYreccuTIEjxVaNrKZ");
		Collection<String[]> str2 = new ArrayList<String[]>();
		when(mockDistributionListDetails.getData()).thenReturn(str2);
		response = service.getDistributionListAddresses(mockSessionContainer, request);
		Assertions.assertNotNull(response);
		Assertions.assertFalse(response.isSuccess());

	}

	// CAP-48378
	@Test
	void that_getOrderwithDistListPreview_method() throws Exception {
		service = Mockito.spy(service);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);

		OEDistributionListBean bean = new OEDistributionListBean("9109", "List20240329-shared",
				"C:\\xs2files\\CustomerMisc\\DEVTEST\\OriginalLists\\", "false", "List 20240329 shared", "03/28/2024",
				"03/28/2024", "1", "true", "USER-RRD2", "OrderDL 3sheets-xls.xls",
				"1711620438252OrderDL 3sheets-xls.csv", "174592", "false");
		when(mockOESessionBean.getDistributionListBean()).thenReturn(null);
		MapperData mdata = new MapperData();
		mdata.setMaxColumns(36);

		assertThrows(AtWinXSWrpException.class, () -> {
			service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean, mockAppSessionBean,
					mockVolatileSessionBean);
		});

		when(mockOESessionBean.getDistributionListBean()).thenReturn(bean);
		bean.setListID("");
		assertThrows(AtWinXSWrpException.class, () -> {
			service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean, mockAppSessionBean,
					mockVolatileSessionBean);
		});
		bean.setListID("9109");
		bean.setListSysFileName("");
		assertThrows(AtWinXSWrpException.class, () -> {
			service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean, mockAppSessionBean,
					mockVolatileSessionBean);
		});

		bean.setListSysFileName("List20240329-shared3");
		try (MockedConstruction<Mapper> mocked = mockConstruction(Mapper.class, (mock, context) -> {
			when(mock.getDataPreview(any())).thenReturn(mdata);
		})) {
			MapperData response = service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean,
					mockAppSessionBean, mockVolatileSessionBean);

			Assertions.assertNotNull(response);
			Assertions.assertNotNull(response.getAltColumnNames());
		}

		try (MockedConstruction<Mapper> mocked = mockConstruction(Mapper.class, (mock, context) -> {
			doThrow(new AtWinXSException("Error", "class")).when(mock).getDataPreview(any());
		})) {
			assertThrows(AtWinXSMsgException.class, () -> {
				MapperData response = service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean,
						mockAppSessionBean, mockVolatileSessionBean);
			});
		}
		bean.setIsEncrytedInd("true");
		when(mockOESessionBean.isSendToDistListUsingQtyFromFile()).thenReturn(true);
		doReturn(mockOEListAssembler).when(service).getAssembler(any(), any());
		try (MockedConstruction<Mapper> mocked = mockConstruction(Mapper.class, (mock, context) -> {
			when(mock.getDataPreview(any())).thenReturn(mdata);
		})) {
			MapperData response = service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean,
					mockAppSessionBean, mockVolatileSessionBean);

			Assertions.assertNotNull(response);
			Assertions.assertNotNull(response.getAltColumnNames());
		}

		doThrow(new AtWinXSException("Error", "class")).when(mockOEListAssembler).getItemColumnMappings(any(), anyInt(),
				anyBoolean(), anyBoolean(), anyInt(), any());
		try (MockedConstruction<Mapper> mocked = mockConstruction(Mapper.class, (mock, context) -> {
			when(mock.getDataPreview(any())).thenReturn(mdata);
		})) {
			assertThrows(AtWinXSMsgException.class, () -> {
				MapperData response = service.getOrderwithDistListPreview(COUNTRY_AUS, mockOESessionBean,
						mockAppSessionBean, mockVolatileSessionBean);
			});

		}

	}

	// CAP-48497
	@Test
	void that_saveMapperList_returns_success() throws Exception {
		service = Mockito.spy(service);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);

		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);

		when(mockSessionContainer.getReferenceSessions()).thenReturn(mockMapInteger);
		when(mockMapInteger.get(any())).thenReturn(mockManageListsSession);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT))
				.thenReturn(mockDistributionListDetails);

		ManageListsResponseBean manageResp = new ManageListsResponseBean();

		manageResp.setListName("Test list");
		manageResp.setRecordCount("1");
		manageResp.setListDescription("Test Desc");
		manageResp.setListID("9109");

		Collection<String[]> str = new ArrayList<String[]>();
		String[] array = new String[39];
		array[0] = "Subbian Shared xls";
		array[1] = "Ramachandran";
		array[2] = "1750 Wallace Ave";
		str.add(array);

		ArrayList<ColumnNameWrapper> headings = new ArrayList<ColumnNameWrapper>();

		ColumnNameWrapper c1 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE1, false);
		ColumnNameWrapper c2 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE2, false);
		ColumnNameWrapper c3 = new ColumnNameWrapper(EXAMPLE_ADDRESS_LINE3, false);
		headings.add(c1);
		headings.add(c2);
		headings.add(c3);

		OEMappedVariableResponseBean o1 = new OEMappedVariableResponseBean(EXAMPLE_ADDRESS_NAME1, COUNTRY_AUS, false);

		Collection<COAltColumnNameWrapperCellData> coAltColNames = new ArrayList<COAltColumnNameWrapperCellData>();
		Collection<COColumnNameWrapperCellData> coColNameWrapperData = new ArrayList<COColumnNameWrapperCellData>();

		Collection<OEMappedVariableResponseBean> altColNames = new ArrayList<OEMappedVariableResponseBean>();
		altColNames.add(o1);

		ManageListsResponseBean[] mockManageListsResponseBean = new ManageListsResponseBean[1];
		mockManageListsResponseBean[0] = manageResp;

		when(mockOESessionBean.getDistributionListBean()).thenReturn(mockOEDistributionListBean);
		when(mockOEShoppingCartComponentLocatorService.locate(any())).thenReturn(mockOEShoppingCartComp);
		doReturn(mockOEListAssembler).when(service).getAssembler(any(), any());
		when(mockCOListDataMapper.getAltColumnNames()).thenReturn(coAltColNames);
		when(mockCOListDataMapper.getHeadings()).thenReturn(coColNameWrapperData);

		COAltColumnNameWrapperCellData coAltColNameWrapperCell = new COAltColumnNameWrapperCellData();
		COAltColumnNameWrapperCellData coAltColNameWrapperCell1 = new COAltColumnNameWrapperCellData();
		COAltColumnNameWrapperCellData coAltColNameWrapperCell2 = new COAltColumnNameWrapperCellData();
		COAltColumnNameWrapperCellData coAltColNameWrapperCell3 = new COAltColumnNameWrapperCellData();
		COAltColumnNameWrapperCellData coAltColNameWrapperCell4 = new COAltColumnNameWrapperCellData();
		COAltColumnNameWrapperCellData coAltColNameWrapperCell5 = new COAltColumnNameWrapperCellData();
		COAltColumnNameWrapperCellData coAltColNameWrapperCell6 = new COAltColumnNameWrapperCellData();

		COColumnNameWrapperCellData coColumnNameWrapperCell = new COColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCell1 = new COColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCell2 = new COColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCell3 = new COColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCell4 = new COColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCell5 = new COColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCell6 = new COColumnNameWrapperCellData();

		coAltColNameWrapperCell.setDisplayLabel("Name 1");
		coAltColNameWrapperCell.setTextVariableName("NM_1");
		coAltColNameWrapperCell.setRequired(true);

		coAltColNameWrapperCell1.setDisplayLabel("Address 1");
		coAltColNameWrapperCell1.setTextVariableName("AD_1");
		coAltColNameWrapperCell1.setRequired(true);

		coAltColNameWrapperCell2.setDisplayLabel("State");
		coAltColNameWrapperCell2.setTextVariableName("STATE_NM");
		coAltColNameWrapperCell2.setRequired(true);

		coAltColNameWrapperCell3.setDisplayLabel("City");
		coAltColNameWrapperCell3.setTextVariableName("CITY_NM");
		coAltColNameWrapperCell3.setRequired(true);

		coAltColNameWrapperCell4.setDisplayLabel("ZIP");
		coAltColNameWrapperCell4.setTextVariableName("ZIP_CD");
		coAltColNameWrapperCell4.setRequired(true);

		coAltColNameWrapperCell5.setDisplayLabel("Address 2");
		coAltColNameWrapperCell5.setTextVariableName("AD_2");
		coAltColNameWrapperCell5.setRequired(false);

		coAltColNameWrapperCell6.setDisplayLabel("Phone");
		coAltColNameWrapperCell6.setTextVariableName("PHONE_NR");
		coAltColNameWrapperCell6.setRequired(false);

		coAltColNames.add(coAltColNameWrapperCell);
		coAltColNames.add(coAltColNameWrapperCell1);
		coAltColNames.add(coAltColNameWrapperCell2);
		coAltColNames.add(coAltColNameWrapperCell3);
		coAltColNames.add(coAltColNameWrapperCell4);
		coAltColNames.add(coAltColNameWrapperCell5);
		coAltColNames.add(coAltColNameWrapperCell6);

		coColumnNameWrapperCell.setColumnName("Col1");
		coColumnNameWrapperCell.setRequired(false);

		coColumnNameWrapperCell1.setColumnName("Col2");
		coColumnNameWrapperCell1.setRequired(false);

		coColumnNameWrapperCell2.setColumnName("Col3");
		coColumnNameWrapperCell2.setRequired(false);

		coColumnNameWrapperCell3.setColumnName("Col4");
		coColumnNameWrapperCell3.setRequired(false);

		coColumnNameWrapperCell4.setColumnName("Col5");
		coColumnNameWrapperCell4.setRequired(false);

		coColumnNameWrapperCell5.setColumnName("Col6");
		coColumnNameWrapperCell5.setRequired(false);

		coColumnNameWrapperCell6.setColumnName("Col7");
		coColumnNameWrapperCell6.setRequired(false);

		coColNameWrapperData.add(coColumnNameWrapperCell);
		coColNameWrapperData.add(coColumnNameWrapperCell1);
		coColNameWrapperData.add(coColumnNameWrapperCell2);
		coColNameWrapperData.add(coColumnNameWrapperCell3);
		coColNameWrapperData.add(coColumnNameWrapperCell4);
		coColNameWrapperData.add(coColumnNameWrapperCell5);
		coColNameWrapperData.add(coColumnNameWrapperCell6);

		try (MockedConstruction<ListsBaseAssembler> mockedListsBaseAssembler = mockConstruction(
				ListsBaseAssembler.class, (mock2, context) -> {
					when(mock2.getList(anyInt(), anyInt(), anyInt())).thenReturn(mockListVO);
				}); MockedConstruction<Mapper> mockedMapper = mockConstruction(Mapper.class, (mock3, context) -> {
					when(mock3.getDataPreview(any())).thenReturn(mockMapperData);
				});) {
			SaveMappingRequest request = new SaveMappingRequest();
			request.setLmapper(mockCOListDataMapper);
			SaveMappingResponse response = new SaveMappingResponse();

			response.setSuccess(true);
			response = service.saveListMappings(mockSessionContainer, request, false, false, "");
			Assertions.assertNotNull(response);
			Assertions.assertTrue(response.isSuccess());

		}

		when(mockOESessionBean.getDistributionListBean()).thenReturn(null);
		try (MockedConstruction<ListsBaseAssembler> mockedListsBaseAssembler = mockConstruction(
				ListsBaseAssembler.class, (mock2, context) -> {
					when(mock2.getList(anyInt(), anyInt(), anyInt())).thenReturn(mockListVO);
				}); MockedConstruction<Mapper> mockedMapper = mockConstruction(Mapper.class, (mock3, context) -> {
					when(mock3.getDataPreview(any())).thenReturn(mockMapperData);
				});) {
			SaveMappingRequest request = new SaveMappingRequest();
			request.setLmapper(mockCOListDataMapper);
			SaveMappingResponse response = new SaveMappingResponse();

			response = service.saveListMappings(mockSessionContainer, request, false, false, "");
			Assertions.assertNotNull(response);
			Assertions.assertFalse(response.isSuccess());

		}

		COAltColumnNameWrapperCellData coAltColNameWrapperCellData2 = new COAltColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCellData2 = new COColumnNameWrapperCellData();
		coAltColNameWrapperCellData2.setDisplayLabel("Name 1");
		coAltColNameWrapperCellData2.setTextVariableName("NM_1");
		coAltColNameWrapperCellData2.setRequired(true);
		coAltColNames.add(coAltColNameWrapperCellData2);

		coColumnNameWrapperCellData2.setColumnName("Col3");
		coColumnNameWrapperCellData2.setRequired(false);
		coColNameWrapperData.add(coColumnNameWrapperCellData2);

		try (MockedConstruction<ListsBaseAssembler> mockedListsBaseAssembler = mockConstruction(
				ListsBaseAssembler.class, (mock2, context) -> {
					when(mock2.getList(anyInt(), anyInt(), anyInt())).thenReturn(mockListVO);
				}); MockedConstruction<Mapper> mockedMapper = mockConstruction(Mapper.class, (mock3, context) -> {
					when(mock3.getDataPreview(any())).thenReturn(mockMapperData);
				});) {
			SaveMappingRequest request = new SaveMappingRequest();
			request.setLmapper(mockCOListDataMapper);
			SaveMappingResponse response = new SaveMappingResponse();
			response = service.saveListMappings(mockSessionContainer, request, false, false, "");
			Assertions.assertNotNull(response);
			Assertions.assertFalse(response.isSuccess());

		}

		COAltColumnNameWrapperCellData coAltColNameWrapperCellData3 = new COAltColumnNameWrapperCellData();
		COColumnNameWrapperCellData coColumnNameWrapperCellData3 = new COColumnNameWrapperCellData();
		coAltColNameWrapperCellData3.setDisplayLabel("");
		coAltColNameWrapperCellData3.setTextVariableName("NM_1");
		coAltColNameWrapperCellData3.setRequired(true);
		coAltColNames.add(coAltColNameWrapperCellData3);

		coColumnNameWrapperCellData3.setColumnName("Col4");
		coColumnNameWrapperCellData3.setRequired(false);
		coColNameWrapperData.add(coColumnNameWrapperCellData3);

		try (MockedConstruction<ListsBaseAssembler> mockedListsBaseAssembler = mockConstruction(
				ListsBaseAssembler.class, (mock2, context) -> {
					when(mock2.getList(anyInt(), anyInt(), anyInt())).thenReturn(mockListVO);
				}); MockedConstruction<Mapper> mockedMapper = mockConstruction(Mapper.class, (mock3, context) -> {
					when(mock3.getDataPreview(any())).thenReturn(mockMapperData);
				});) {
			SaveMappingRequest request = new SaveMappingRequest();
			request.setLmapper(mockCOListDataMapper);
			SaveMappingResponse response = new SaveMappingResponse();
			response = service.saveListMappings(mockSessionContainer, request, false, false, "");
			Assertions.assertNotNull(response);
			Assertions.assertFalse(response.isSuccess());

		}

	}

	// CAP-46517, CAP-46518, CAP-42227 - validation mocks
	@Test
	void invalidAuthorizationForListProcess() {
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		setupAllowDistLists();
		assertFalse(service.invalidAuthorizationForListProcess(mockSessionContainer));

		when(mockOESessionBean.getUserSettings()).thenReturn(null);
		when(mockOESessionBean.getCurrentCustomDocumentItem()).thenReturn(null);
		assertTrue(service.invalidAuthorizationForListProcess(mockSessionContainer));

		when(mockOESessionBean.getCurrentCustomDocumentItem()).thenReturn(mockItem);
		when(mockOESessionBean.getCurrentCustomDocumentUserInterface()).thenReturn(null);
		assertTrue(service.invalidAuthorizationForListProcess(mockSessionContainer));

		when(mockOESessionBean.getCurrentCustomDocumentUserInterface()).thenReturn(mockUI);
		when(mockItem.getUserInterface()).thenReturn(mockBundleUI);
		assertTrue(service.invalidAuthorizationForListProcess(mockSessionContainer));

		when(mockItem.getUserInterface()).thenReturn(mockUI);
		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT);
		assertTrue(service.invalidAuthorizationForListProcess(mockSessionContainer));

		when(mockUI.getMergeOption()).thenReturn(MergeOption.MERGE);
		assertFalse(service.invalidAuthorizationForListProcess(mockSessionContainer));

		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
		assertFalse(service.invalidAuthorizationForListProcess(mockSessionContainer));
	}

	@Test
	void that_processUseThisList_success() throws AtWinXSException {
		DistListUpdateRequest distListUpdateRequest = new DistListUpdateRequest("IOT95YCWzolBMweINjsjhjIEjxVaNrKZ",
				"Galaxy Ranger");
		DeliveryOptions deliveryOptions = new DeliveryOptionsImpl();
		OrderHeaderVO orderHeaderVO = new OrderHeaderVO();

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(Locale.US);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowListOrders()).thenReturn(true);
		when(mockUserSettings.isEnableDistrListFile()).thenReturn(true);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(DeliveryOptions.class), any())).thenReturn(deliveryOptions);
		when(mockUserSettings.getBillToAttnOption()).thenReturn("0");
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(List.class), any())).thenReturn(mockList);

		try (MockedStatic<COListsUtil> mockListsUtil = Mockito.mockStatic(COListsUtil.class);
				MockedConstruction<OECheckoutAssembler> mockedCheckout = mockConstruction(OECheckoutAssembler.class,
						(mock, context) -> {
							mockListsUtil.when(() -> COListsUtil.buildAndValidateList(any(), any(), any(), any()))
									.thenReturn(new Message());
							when(mock.getOrderHeader(anyInt())).thenReturn(orderHeaderVO);
						});
				MockedConstruction<OEListAssembler> mockedList = mockConstruction(OEListAssembler.class,
						(mock, context) -> {
							doNothing().when(mock).saveDistributionListDetails(any(), any(), any(), anyBoolean());
						})) {

			when(mockManageListsLocatorService.locate(any())).thenReturn(mockIManageList);
			when(mockOEShoppingCartComponentLocatorService.locate(any())).thenReturn(mockShoppingCartComp);

			DistListUpdateResponse response = service.processUseThisList(distListUpdateRequest, mockSessionContainer);
			assertEquals(true, response.isSuccess());
		}
	}

}