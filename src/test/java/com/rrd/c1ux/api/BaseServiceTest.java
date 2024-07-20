/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  CAP-38715   C Porter                                    refactor
 *  03/17/2023  C Porter        CAP-39295                   Handle "Could not load session" Exception
 *  04/05/2023  C Porter        CAP-39674                   Intermittent Session Timeout fix
 *  04/18/2023  C Porter        CAP-39954                   Occasional session timeout fix
 *  05/16/2023  N Caceres		CAP-39045					Add mocks for testing ItemAddToCart Service
 *  05/25/2023	C Codina		CAP-39338					Add mock for testing getOrderFilesContent
 *  05/26/2023	N Caceres		CAP-49046					Add mocks for testing ItemQuantityValidation Service
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup
 *  6/05/2023	C Codina		CAP-39049					Add mocks for testing getTrackingDetails
 *  6/26/2023	C Codina		CAP-40613					Add mocks for testing processPNA
 *  6/28/23     M Sakthi		CAP-40615					Add mocks for testing getSearchOrdersDetail
 *	6/28/2023	L De Leon		CAP-41373					Add mocks for quick copy order JUnits
 *	07/31/2023	N Caceres		CAP-42342					Add mocks for update company profile junits
 *	08/22/2023	S Ramachandran	CAP-43234					Add mocks for Order Routing Information for Order Search
 *	09/20/23	N Caceres		CAP-42856					Add mocks for retrieving all featured items
 *	09/28/23	C Codina		CAP-43159					Add mocks for profile selection
 *	10/16/23    S Ramachandran	CAP-44515					Add mocks for Retrieve Order Approval Alerts
 *	11/10/23	M Sakthi		CAP-44979					Add mocks forModify order search to return credit card fields and carrier service fields
 *	12/22/23	S Ramachandran	CAP-46081					Added mockUSPSValidationResponse
 *	03/05/24	N Caceres		CAP-47670					Added mock for UserDefinedFields
 *	03/12/24	N Caceres		CAP-47732					Added mock for list object
 *	03/13/24	S Ramachandran	CAP-47841					Added mock for testing download order file
 *	06/05/24    S Ramachandran  CAP-49887					Added mock for KitComponentItemsService
 */
package com.rrd.c1ux.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rrd.c1ux.api.config.properties.ClaimsProperties;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSFullDetailRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.COOSShipmentAndTrackingResponse;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.admin.locators.CountryLocatorService;
import com.rrd.c1ux.api.services.admin.locators.ProfileAttributeComponentLocatorService;
import com.rrd.c1ux.api.services.admin.locators.ProfileComponentLocatorService;
import com.rrd.c1ux.api.services.admin.locators.ServiceComponentLocatorService;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryServiceImpl;
import com.rrd.c1ux.api.services.items.KitComponentItemsService;
import com.rrd.c1ux.api.services.items.locator.AdminUtilService;
import com.rrd.c1ux.api.services.items.locator.CMFeaturedItemsComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorService;
import com.rrd.c1ux.api.services.items.locator.OAOrderAdminLocatorService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.OEPricingAndAvailabilityComponentLocatorService;
import com.rrd.c1ux.api.services.kits.MKComponentInterfaceLocatorService;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.rrd.c1ux.api.services.orderentry.locator.OESavedOrderComponentLocatorService;
import com.rrd.c1ux.api.services.orderentry.locator.OEShoppingCartComponentLocatorService;
import com.rrd.c1ux.api.services.orderentry.util.OrderEntryUtilService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.LoginReader;
import com.rrd.c1ux.api.services.session.ProfileReader;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.session.SiteReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.admin.entity.SiteAttributeValues;
import com.rrd.custompoint.admin.profile.dao.UserDefinedFieldDAO;
import com.rrd.custompoint.admin.profile.entity.CorporateProfile;
import com.rrd.custompoint.admin.profile.entity.ExtendedProfile;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;
import com.rrd.custompoint.admin.profile.entity.ProfileUDFDefinition;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.rrd.custompoint.admin.profile.entity.UserDefinedFields;
import com.rrd.custompoint.framework.util.objectfactory.ComponentObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;
import com.rrd.custompoint.orderentry.entity.AddressSourceSettings;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.orderentry.entity.List;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.PaymentVerification;
import com.rrd.custompoint.orderentry.entity.ProfileSelection;
import com.rrd.custompoint.orderentry.entity.RoutedOrders;
import com.rrd.custompoint.orderstatus.ao.OrderSearchDetailFormBeanImpl;
import com.rrd.custompoint.orderstatus.entity.OSDetailsItemsOrdered;
import com.rrd.custompoint.orderstatus.entity.OrderStatusCodes;
import com.rrd.custompoint.orderstatus.entity.OrderStatusSearch;
import com.rrd.custompoint.orderstatus.entity.OrderStatusTrackingInfo;
import com.wallace.atwinxs.admin.vo.BusinessUnitVO;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.admin.vo.UserGroupVO;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CompositeProfileBean;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.interfaces.IBusinessUnit;
import com.wallace.atwinxs.interfaces.ICountryComponent;
import com.wallace.atwinxs.interfaces.ILoginInterface;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOEPricingAndAvailability;
import com.wallace.atwinxs.interfaces.IOESavedOrderComponent;
import com.wallace.atwinxs.interfaces.IOrderAdmin;
import com.wallace.atwinxs.interfaces.IProfileAttribute;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.interfaces.IServiceInterface;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.interfaces.IUserGroup;
import com.wallace.atwinxs.locale.vo.CountryWithNameVO;
import com.wallace.atwinxs.orderentry.admin.vo.LoginOrderPropertiesVO;
import com.wallace.atwinxs.orderentry.ao.OECancelOrderAssembler;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderFilterVO;
import com.wallace.atwinxs.orderentry.vo.OrderInfoShippingVO;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSession;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSessionBean;
import com.wallace.atwinxs.orderstatus.util.OSBatchOrderStatusBean;
import com.wallace.atwinxs.orderstatus.vo.OrderStatusHeaderVO;
import com.wallace.tt.vo.TTSession;

/**
 * Base test class for service implementation test cases.
 *
 * Mock services that are needed for tests can be added to this class, so that they are available for
 * use in other service JUnit classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;

  protected static final String ENCRYPTED_SESSION_ID = "";

  public static BusinessUnitVO getTestBusinessUnitVO(int siteId, int buId) {

    return new BusinessUnitVO(siteId, buId, "buLoginID", "buName", "buDescription", "childAccountNumber", "customizationToken", false,
        false, false, false, false, false, false, "defaultLanguageCode", "defaultLocaleCode", "defaultTimeZone", 8, 1, 60, true, 1, true,
        false, "updateUser", new Date(), "helpPhone", "helpEmail", "helpHTML", 1, 1, true, false, 0, false, "currencyLocaleVariant", true,
        true, "applicationName", 1, 1, true, true, true, true);
  }

  public static UserGroupVO getTestUserGroupVO(int siteId, int buId, String userGroup) {

    return new UserGroupVO(siteId, buId, userGroup, false, "division", "department", true, "defaultView", true, true, false, true, true,
        true, false, "customizationToken", true, 10, "defaultLanguageCode", "defaultLocaleCode", "defaultTimeZone", 8, 1, 60, true, 1, true,
        true, "updateUser", new Date(), false, 0, true, true, 1, 1, "currencyOverride", true);
  }

  public static SiteVO getTestSiteVO(int siteId, int buId, String account, String userName, String userGroup, String profileId) {
    return new SiteVO(siteId, account, account, account, buId, account, account, siteId, buId, account, account, null, account, false,
        userName, userGroup, profileId, false, false, false, account, false);
  }

  public static SiteVO getTestSiteVO(int siteId, String testAccount) {

    return new SiteVO(siteId, "siteLoginID", testAccount, "siteDescription", 0, "corporateNumber", "soldToNumber", 0, 3, "A", "updateUser",
        new Date(), "customizationToken", false, "defaultLanguageCode", "defaultLocaleCode", "defaultTimeZone", false, false, false,
        "currencyLocaleVariant", false);
  }

  public static LoginVO getTestLoginVO(String testUsername, int testSiteId) {

    return new LoginVO(testSiteId, 1, "userGroupName", testUsername, 1, false, false, false, false, true, false, true, false, true, false,
        "passwordCode", "passwordHintText", false, "sharedIDDesc", false, false, false, "userTypeCode", false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, "loginStatus", "custDocAdminLevel", "defaultLanguageCode",
        "defaultLocaleCode", "defaultTimeZone", false, "updateUser", new Date(), false, false, false, false, false, false, false, false,
        "hashEncryptedPassword", false);
  }

  public static LoginVO getTestSharedLoginVO(String testUsername, int testSiteId) {

    return new LoginVO(testSiteId, 1, "userGroupName", testUsername, 1, false, false, false, false, true, false, true, false, true, false,
        "passwordCode", "passwordHintText", true, "sharedIDDesc", false, false, false, "userTypeCode", false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, "loginStatus", "custDocAdminLevel", "defaultLanguageCode",
        "defaultLocaleCode", "defaultTimeZone", false, "updateUser", new Date(), false, false, false, false, false, false, false, false,
        "hashEncryptedPassword", false);

  }

  public static ProfileVO getTestProfileVO(int siteId, int testProfileNumber, String testProfileId) {

    return new ProfileVO(siteId, 12, testProfileNumber, "firstName", "lastName", "phoneNumber", "emailAddress", testProfileId, "updateUser",
        new Date(), 1, true, "thirdPartyPriceClass", "thirdPartyPriceClassDesc");
  }

  @Mock
  protected ProfileReader mockProfileReader;

  @Mock
  protected LoginReader mockLoginReader;

  @Mock
  protected SiteReader mockSiteReader;

  @Mock
  protected IBusinessUnit mockBUComponent;

  @Mock
  protected SessionHandlerService mockSessionHandlerService;

  @Mock
  protected SessionContainer mockSessionContainer;

  @Mock
  protected ApplicationSession mockApplicationSession;

  @Mock
  protected AppSessionBean mockAppSessionBean;

  @Mock
  protected TTSession mockTTSession;

  @Mock
  protected ClaimsProperties mockClaimsProperties;

  @Mock
  protected CPSessionReader mockCPSessionReader;

  @Mock
  protected TokenReader mockTokenReader;

  @Mock
  protected IAdminComponentLocator mockComponentLocator;

  @Mock
  protected ISite mocSiteComponent;

  @Mock
  protected IProfileInterface mockProfileComponent;

  @Mock
  protected ILoginInterface mockLoginComponent;

  @Mock
  protected IUserGroup mockGroupComponent;

  // CAP-39045 Resolve concurrency issues in ItemAddToCart Service
  @Mock
  protected ObjectMapFactoryService mockObjectMapFactoryService = new ObjectMapFactoryServiceImpl();

  @Mock
  protected EntityObjectMap mockEntityObjectMap;

  @Mock
  protected OrderEntrySession mockOESession;

  @Mock
  protected OEOrderSessionBean mockOESessionBean;

  @Mock
  protected ApplicationVolatileSession mockApplicationVolatileSession;

  @Mock
  protected VolatileSessionBean mockVolatileSessionBean;

  @Mock
  protected PunchoutSessionBean mockPunchoutSessionBean;

  @Mock
  protected OrderEntryUtilService mockOrderEntryUtilService;

  @Mock
  protected OESavedOrderComponentLocatorService mockOESavedOrderComponentLocatorService;

  @Mock
  protected IOESavedOrderComponent mockIOESavedOrderComponent;

  @Mock
  protected OEResolvedUserSettingsSessionBean mockUserSettings;

  @Mock
  protected CatalogItem mockCatalogItem;

 //CAP-40615
  @Mock
  protected OrderStatusSearch mockOrderStatusSearch;

  @Mock
  protected Order mockOrder;

  @Mock
  protected BaseSession mockBaseSession;

  @Mock
  protected OrderStatusCodes mockOrderStatusCodes;

  @Mock
  protected ProfileDefinition mockProfileDefinition;

  @Mock
  protected Profile mockProfile;

  @Mock
  protected ExtendedProfile mockExtendedProfile;

  @Mock
  protected CorporateProfile mockCorporateProfile;

  @Mock
  protected UserDefinedFields mockUserDefinedFields;

  @Mock
  protected ManageItemsInterfaceLocatorService mockItemsInterfaceLocatorService;

  @Mock
  protected IManageItemsInterface mockItemsInterface;

  @Mock
  protected CustomizationToken mockToken;

  @Mock
  protected TranslationService mockTranslationService;

  @Mock
  protected OrderStatusSession mockOrderStatusSession;

  @Mock
  protected OrderStatusSessionBean mockOsBean;

  @Mock
  protected OrderSearchDetailFormBeanImpl mockOrderSearchDetailFromBean;

  @Mock
  protected OrderStatusTrackingInfo mockOrderStatusTrackingInfo;

  @Mock
  protected COOSShipmentAndTrackingResponse mockTrackingResponse;

  @Mock
  protected COOSFullDetailRequest mockDetailRequest;

  @Mock
  protected OEAssemblerFactoryService mockOEAssembler;

  @Mock
  protected OEShoppingCartComponentLocatorService mockOEShoppingCartComponentLocatorService;

  @Mock
  protected OEManageOrdersComponentLocatorService mockOEManageOrdersComponentLocatorService;

  @Mock
  protected IOEManageOrdersComponent mockIOEManageOrdersComponent;

  @Mock
  protected OrderFilterVO mockFilterVOS;

  @Mock
  protected AdminUtilService mockAdminUtilService;

  @Mock
  protected OEPricingAndAvailabilityComponentLocatorService mockOEPricingAndAvailabilityComponentLocatorService;

  @Mock
  protected IOEPricingAndAvailability mockIOEPricingAndAvailability;

  @Mock
  protected CatalogDefaultVO mockCatDefVO;

  @Mock
  protected OECancelOrderAssembler mockOECancelOrderAssembler;

	@Mock
	protected OESavedOrderAssembler mockSavedOrderAssembler;

	@Mock
	protected UserDefinedField mockUserDefinedField;

	@Mock
	protected UserDefinedFieldDAO mockUserDefinedFieldDAO;

	@Mock
	protected ProfileUDFDefinition mockProfileUDFDefinition;

  @Mock
  protected AddressSourceSettings mockAddressSourceSettings;

  @Mock
  protected User mockUser;

  @Mock
  protected HashMap<Object, String> mockHashMap;

  @Mock
  protected SearchResult mockSearchResult;

  @Mock
  protected CMFeaturedItemsComponentLocatorService mockFeaturedItemsLocator;

  @Mock
  protected ComponentObjectMap mockComponentObjectMap;

  @Mock
  protected ServletContext mockServletContext;

  @Mock
  protected ProfileSelection mockProfileSelection;

  @Mock
  protected Connection mockConnection;

  @Mock
  protected OAOrderAdminLocatorService mockoaOrderAdminService;

  @Mock
  protected IOrderAdmin mockIOrderAdmin;

  @Mock
  protected ServiceComponentLocatorService mockscLocatorService;

  @Mock
  protected IServiceInterface mockIServiceInterface;

  @Mock
  protected RoutedOrders mockRoutedOrders;

  @Mock
  protected LoginOrderPropertiesVO mockLoginOrderPropertiesVO;

  @Mock
  protected OSBatchOrderStatusBean mockOSBatchOrderStatusBean;

  @Mock
  protected OrderStatusHeaderVO mockOrderStatusHeaderVO;

  @Mock
  protected OSDetailsItemsOrdered mockOSDetailsItemsOrdered;

  @Mock
  protected PaymentVerification mockPaymentVerification;

  @Mock
  protected OrderInfoShippingVO mockOrderInfoShippingVO;

  @Mock
  protected CountryLocatorService mockCountryLocatorService;

  @Mock
  protected ICountryComponent mockICountryComponent;

  @Mock
  protected CountryWithNameVO mockCountryWithNameVO;

  @Mock
  protected CompositeProfileBean mockCompositeProfileBean;

  @Mock
  protected  USPSValidationResponse mockUSPSValidationResponse;

  @Mock
  protected  SiteAttributes mockSiteAttributes;

  @Mock
  protected  SiteAttribute mockSiteAttribute;

  @Mock
  protected  SiteAttributeValues mockSiteAttributeValues;

  @Mock
  protected  SiteAttribute mockSiteAttr;

  @Mock
  protected  ProfileVO mockProfileVO;

  @Mock
  protected ProfileComponentLocatorService mockProfileComponentLocatorService;

  @Mock
  protected  IProfileInterface mockIProfileInterface;

  @Mock
  protected ProfileAttributeComponentLocatorService mockProfileAttributeComponentLocatorService;

  @Mock
  protected IProfileAttribute mockIProfileAttribute;

  @Mock
  protected List mockList;
  
  @Mock
  protected KitComponentItemsService mockKitComponentItemsService; 
  
  @Mock
  protected MKComponentInterfaceLocatorService mockMKComponentInterfaceLocatorService;

  public static final String BLANK_NOT_ALLOWED_ERR_ENGLISH = "blank not allowed English";
  public static final String BLANK_NOT_ALLOWED_ERR_FRENCH = "blank not allowed French";
  public static final String MAX_CHARS_ERR_ENGLISH = "maximum chars English";
  public static final String MAX_CHARS_ERR_FRENCH = "maximum chars French";
  public static final String CURRENT_CART_ERR_ENGLISH = "CURRENT_CART_ERR English";
  public static final String CURRENT_CART_ERR_FRENCH = "CURRENT_CART_ERR French";
  public static final String GENERIC_SAVE_FAILED_ERR_ENGLISH = "Save failed English";
  public static final String GENERIC_SAVE_FAILED_ERR_FRENCH = "Save failed French";
  public static final String NOT_VALID_ERR_ENGLISH = "NOT_VALID_ERR English";
  public static final String NOT_VALID_ERR_FRENCH = "NOT_VALID_ERR French";
  public static final String NO_ORDER_IN_PROGRESS_ERR_ENGLISH = "NO_ORDER_IN_PROGRESS_ERR English";
  public static final String NO_ORDER_IN_PROGRESS_ERR_FRENCH = "NO_ORDER_IN_PROGRESS_ERR French";
  public static final String ORDER_ALREADY_SUBMITTED_ERR_ENGLISH = "ORDER_ALREADY_SUBMITTED_ERR English";
  public static final String ORDER_ALREADY_SUBMITTED_ERR_FRENCH = "ORDER_ALREADY_SUBMITTED_ERR French";
  public static final String GENERIC_ERROR_MSG = "Failure message generic";

  public static Locale FRENCH_CA_LOCALE = new java.util.Locale("fr_CA");
  public static Locale DEFAULT_US_LOCALE = new java.util.Locale("en_US");

  public AppSessionBean getFrenchSession() {
    mockAppSessionBean.setDefaultLocale(FRENCH_CA_LOCALE);
    mockAppSessionBean.setDefaultLanguage("fr");
    return mockAppSessionBean;
  }

  public AppSessionBean getEnglishSession() {
    mockAppSessionBean.setDefaultLocale(DEFAULT_US_LOCALE);
    mockAppSessionBean.setDefaultLanguage("en");
    return mockAppSessionBean;
  }

  public void setupBaseMockSessions() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
  }

  public void setUpTranslationServiceErrors() throws AtWinXSException {
    doReturn(GENERIC_ERROR_MSG).when(mockTranslationService).processMessage(any(), any(), anyString(), any());
  }

  public void setUpMockEntityObjectFactory() {
      when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);


  }
}
