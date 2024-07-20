/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  CAP-38715   C Porter                                    refactor
 *  03/14/23    C Porter        CAP-37146                   Spring Session
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup
 *  06/02/23	A Salcedo		CAP-39210					Added mockRedirectService.
 *  06/09/23	N Caceres		CAP-39051					Add new mocks for GetFavorites Controller and fix alignment
 *  06/22/23	L De Leon		CAP-41373					Added mockQuickCopyOrderService.
 *  07/11/23	Satishkumar A	CAP-41970					Added mockCountriesAndStatesService.
 *  07/28/23    Sakthi M        CAP-42545					Added mockCancelOrderService
 *  08/09/23 	Satishkumar A	CAP-42720					API Build - Favorite Toggle Call
 *	08/01/23	L De Leon		CAP-42519					Added mockCopyRecentOrderService
 *	08/23/23	A Boomker		CAP-43223					Added custom docs services and shared base handling
 *	08/30/23	Krishna Natarajan	CAP-43371				Added UserStateProfileService for use of translation
 *	08/29/23	Satishkumar A	CAP-43283					C1UX BE - Routing Information For Justification Section on Review Order Page
 *	09/22/23	L De Leon		CAP-44032					Added mockPaymentInformationService
 *	10/03/23	Satishkumar A	CAP-43282	C1UX BE - API Build - Get OE Item Filter Options - including favorites, featured types
 *  10/18/2023  C Porter        CAP-44260                   Allow for custom Content Security Policies by site
 *  10/31/23	C Codina		CAP-44742					Added mockCarouselMessageService
 *  11/08/23	A Boomker		CAP-44427					Added check 403 response method
 *  11/08/23	A Boomker		CAP-44486					Added mockCustDocProfileService
 *	11/27/23	L De Leon		CAP-44467					Added mockOrderOnBehalfService
 *	12/22/23	S Ramachandran	CAP-46081					Added mockUSPSValidationService
 *	12/27/23	L De Leon		CAP-45907					Added mockSelfRegistrationService
 *	01/18/24	C Codina 		CAP-46379					Added mockCatalogService
 *	01/31/24	A Boomker		CAP-44862					Added methods for testing streaming response codes
 *	02/01/24	Satishkumar A	CAP-46675					Added mockEnforceOnOrderingService
 *	02/13/24	L De Leon		CAP-46960					Added mockBudgetAllocationService
 *	02/14/24	S Ramachandran	CAP-47145					Added mockSiteComponentLocatorService,mockItemServiceComponentLocatorService,mockSingleItemDetailAllocationsService
 *	03/13/24	S Ramachandran	CAP-47841					Added mockManageListsLocatorService
 *	04/23/24	C Codina		CAP-48623					Added mockOrderTemplateService
 *	05/13/24	S Ramachandran	CAP-49326					Added mockEFDDestinationService
 *	05/27/24	L De Leon		CAP-49609					Added mockLinkedLoginsService
 *	06/07/24	N Caceres		CAP-50006					Added mockKitTemplateService
 *	07/09/24	A Boomker		CAP-46538					Added mock for imprint history service
 */
package com.rrd.c1ux.api;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rrd.c1ux.api.config.properties.ClaimsProperties;
import com.rrd.c1ux.api.config.properties.CorsProperties;
import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.filter.CSPHeaderResolver;
import com.rrd.c1ux.api.filter.PropertyHeaderWriter;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.addtocart.mappers.ItemAddToCartMapper;
import com.rrd.c1ux.api.models.catalog.mappers.CatalogMenuMapper;
import com.rrd.c1ux.api.models.catalogitems.mappers.CatalogItemsMapper;
import com.rrd.c1ux.api.models.items.mappers.FavoriteItemsMapper;
import com.rrd.c1ux.api.models.items.mappers.ItemRptMapper;
import com.rrd.c1ux.api.models.notiereditems.mappers.NotieredItemMapper;
import com.rrd.c1ux.api.models.singleitem.mappers.SingleItemDetailsMapper;
import com.rrd.c1ux.api.models.users.mappers.UserStateProfileMapper;
import com.rrd.c1ux.api.services.addtocart.ItemAddToCartService;
import com.rrd.c1ux.api.services.admin.LinkedLoginService;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.alert.AlertsService;
import com.rrd.c1ux.api.services.budgetallocation.BudgetAllocationService;
import com.rrd.c1ux.api.services.catalog.CatalogService;
import com.rrd.c1ux.api.services.catalog.wizard.OrderWizardService;
import com.rrd.c1ux.api.services.catalogforprime.CatalogForPrimeService;
import com.rrd.c1ux.api.services.catalogitem.CatalogItemRetriveServices;
import com.rrd.c1ux.api.services.catalogmenu.CatalogMenuService;
import com.rrd.c1ux.api.services.checkout.CODeliveryInformationService;
import com.rrd.c1ux.api.services.checkout.EFDDestinationService;
import com.rrd.c1ux.api.services.checkout.OrderHeaderInfoService;
import com.rrd.c1ux.api.services.checkout.OrderSummaryService;
import com.rrd.c1ux.api.services.checkout.PaymentInformationService;
import com.rrd.c1ux.api.services.checkout.SubmitOrderService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsFileStreamService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsImprintHistoryService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsListMappingService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsListService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProfileService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProofingService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.edoc.EdocService;
import com.rrd.c1ux.api.services.eoo.EnforceOnOrderingService;
import com.rrd.c1ux.api.services.favorite.ToggleFavoriteService;
import com.rrd.c1ux.api.services.footer.FooterService;
import com.rrd.c1ux.api.services.help.SupportContactService;
import com.rrd.c1ux.api.services.itemqtyvalidation.ItemQuantityValidationService;
import com.rrd.c1ux.api.services.items.GetFavoritesService;
import com.rrd.c1ux.api.services.items.GetItemDetailWithQuantityService;
import com.rrd.c1ux.api.services.items.PNAProcessor;
import com.rrd.c1ux.api.services.items.PNATieredProcessor;
import com.rrd.c1ux.api.services.items.UniversalSearchService;
import com.rrd.c1ux.api.services.items.locator.ItemServiceComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.ItemValidationComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.SiteComponentLocatorService;
import com.rrd.c1ux.api.services.kittemplate.KitTemplateService;
import com.rrd.c1ux.api.services.landing.LandingService;
import com.rrd.c1ux.api.services.locators.ManageListsLocatorService;
import com.rrd.c1ux.api.services.login.saml.SamlSpService;
import com.rrd.c1ux.api.services.messages.CarouselMessagesService;
import com.rrd.c1ux.api.services.messages.MessageCenterService;
import com.rrd.c1ux.api.services.navimenu.NavigationMenuService;
import com.rrd.c1ux.api.services.notiereditems.PNANoTieredServices;
import com.rrd.c1ux.api.services.orders.cancelorder.CancelOrderService;
import com.rrd.c1ux.api.services.orders.copy.CopyRecentOrderService;
import com.rrd.c1ux.api.services.orders.copy.QuickCopyOrderService;
import com.rrd.c1ux.api.services.orders.orderonbehalf.OrderOnBehalfService;
import com.rrd.c1ux.api.services.orders.ordersearch.COOrderFilesService;
import com.rrd.c1ux.api.services.orders.ordersearch.OrderSearchService;
import com.rrd.c1ux.api.services.orders.ordertemplate.OrderTemplateService;
import com.rrd.c1ux.api.services.orders.savedorders.SavedOrderService;
import com.rrd.c1ux.api.services.punchout.PunchoutService;
import com.rrd.c1ux.api.services.redirect.saml.RedirectService;
import com.rrd.c1ux.api.services.routing.RoutingInformationService;
import com.rrd.c1ux.api.services.selfreg.SelfRegistrationService;
import com.rrd.c1ux.api.services.session.C1UXSamlAuthenticationSuccessHandler;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.settingsandprefs.CatalogUtilityNavigationInfoService;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartService;
import com.rrd.c1ux.api.services.shoppingcartitemindicator.ShoppingCartItemIndicatorServices;
import com.rrd.c1ux.api.services.singleitem.SingleItemDetailAllocationsService;
import com.rrd.c1ux.api.services.singleitem.SingleItemDetailsService;
import com.rrd.c1ux.api.services.standardattributes.StandardAttributesService;
import com.rrd.c1ux.api.services.styles.StyleService;
import com.rrd.c1ux.api.services.suggesteditems.SuggestedItemsService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.services.users.UserFullProfileProcessor;
import com.rrd.c1ux.api.services.users.UserStateProfileService;
import com.rrd.c1ux.api.services.usps.USPSValidationService;
import com.rrd.c1ux.api.services.util.CountriesAndStatesService;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

/**
 * Base test class for MVC / Controller test cases.
 *
 * Controller JUnit classes that extend this class will already have a
 * configured MockMvc object available for use.
 *
 * Mock services that are needed for tests can be added to this class, so that
 * they are available for use in other controller test cases.
 */
@WebMvcTest
@ActiveProfiles(resolver = C1UXDefaultActiveProfilesResolver.class)
@TestPropertySource(locations = { "/application-test.yml" })
@ContextConfiguration(classes = { StaticResourceHandler.class, ClaimsProperties.class, CorsProperties.class,
		SecurityConfig.class, StaticHeadersProperties.class })
@ComponentScan(basePackageClasses = { BaseCPApiController.class })
@ExtendWith(MockitoExtension.class)
public abstract class BaseMvcTest {

	public static final String EXPECTED_403MESSAGE = ModelConstants.EXPECTED_403MESSAGE;

	@Autowired
	protected MockMvc mockMvc;

	// needed to stub out the Spring Security Config
	@MockBean
	protected RelyingPartyRegistrationRepository repo;

	@MockBean
	protected CPSessionReader mockSessionReader;

	@MockBean
	protected TokenReader mockTokenReader;

	@MockBean
	protected UniversalSearchService mockUniversalSearchService;

	@MockBean
	protected CatalogItemsMapper mockCatalogItemsMapper;

	@MockBean
	protected SessionContainer mockSessionContainer;

	@MockBean
	protected AppSessionBean mockAppSessionBean;

	@MockBean
	protected ApplicationSession mockApplicationSession;

	@MockBean
	protected SingleItemDetailsService mSingleItemDetailsService;

	@MockBean
	protected OrderEntrySession mockOrderEntrySession;

	@MockBean
	protected ApplicationVolatileSession mockApplicationVolatileSession;

	@Mock
	protected VolatileSessionBean mockVolatileSessionBean;

	@MockBean
	protected PNANoTieredServices mockPNANoTieredServices;

	@MockBean
	protected CatalogItemRetriveServices mCatalogItemRetriveServices;

	@MockBean
	protected ShoppingCartItemIndicatorServices mShoppingCartItemIndicatorServices;

	@MockBean
	protected CatalogMenuService mockCatalogMenuService;

	@MockBean
	protected CatalogMenuMapper mockCatalogMenuMapper;

	@MockBean
	protected ItemAddToCartMapper mockItemAddToCartMapper;

	@MockBean
	protected ItemAddToCartService mockItemAddToCartService;

	@MockBean
	protected CatalogForPrimeService mockCatalogForPrimeService;

	@MockBean
	protected NavigationMenuService mockNavigationMenuService;

	@MockBean
	protected FooterService mockFooterService;

	@MockBean
	protected SupportContactService mockSupportContactService;

	@MockBean
	protected ItemQuantityValidationService mockItemQuantityValidationService;

	@MockBean
	protected FavoriteItemsMapper mockFavoriteItemsMapper;

	@MockBean
	protected GetFavoritesService mockGetFavoritesService;

	@MockBean
	protected GetItemDetailWithQuantityService getItemDetailsWithQuanityService;

	@MockBean
	protected ItemRptMapper mockItemRptMapper;

	@MockBean
	protected PNATieredProcessor mockPNATieredProcessor;

	@MockBean
	protected PNAProcessor mockPNAProcessor;

	@MockBean
	protected LandingService mockLandingService;

	@MockBean
	protected SamlSpService mockSamlSpService;

	@MockBean
	protected MessageCenterService mockMessageCenterService;

	@MockBean
	protected NotieredItemMapper mockNoTieredItemMapper;

	@MockBean
	protected OrderSearchService mockOrderSearchService;

	@MockBean
	protected COOrderFilesService mockCOOrderFilesService;

	@MockBean
	protected PunchoutService mockPunchoutService;

	@MockBean
	protected ShoppingCartService mockShoppingCartService;

	@MockBean
	protected CatalogUtilityNavigationInfoService mockCatalogUtilityNavigationInfoService;

	@MockBean
	protected SingleItemDetailsMapper mockSingleItemDetailsMapper;

	@MockBean
	protected StyleService mockStyleService;

	@MockBean
	protected TranslationService mockTranslationService;

	@MockBean
	protected UserFullProfileProcessor mockUserFullProfileProcessor;

	@MockBean
	protected UserStateProfileMapper mockUserStateProfileMapper;

	@MockBean
	protected C1UXSamlAuthenticationSuccessHandler mockC1UXSamlAuthenticationSuccessHandler;

	@MockBean
	protected SelfAdminService mockSelfAdminService;

	@MockBean
	protected CODeliveryInformationService mockCODeliveryInformationService;

	@MockBean
	protected OrderSummaryService mockOrderSummaryService;

	@MockBean
	protected OrderHeaderInfoService mockOrderheaderInfoService;

	@MockBean
	protected SavedOrderService mockSavedOrderService;

	@MockBean
	protected RedirectService mockRedirectService;

	@MockBean
	protected SubmitOrderService mockSubmitOrderService;

	@MockBean
	protected QuickCopyOrderService mockQuickCopyOrderService;

	@MockBean
	protected PunchoutSessionBean mockPunchoutSessionBean;

	@MockBean
	protected CustomizationToken mockToken;

	@MockBean
	protected OEOrderSessionBean mockOEOrderSessionBean;

	@MockBean
	protected OEResolvedUserSettingsSessionBean mockUserSettings;

	@MockBean
	protected OEItemSearchCriteriaSessionBean mockSearchCriteriaBean;

	@MockBean
	protected CountriesAndStatesService mockCountriesAndStatesService;

	@MockBean
	protected CancelOrderService mockCancelOrderService;

	//CAP-42720
	@MockBean
	protected ToggleFavoriteService mockToggleFavoriteService;


	// CAP-42519
	@MockBean
	protected CopyRecentOrderService mockCopyRecentOrderService;
	// CAP-43223
	@MockBean
	protected CustomDocsService mockCustDocsUIService;
	@MockBean
	protected CustomDocsProofingService mockCustDocsProofService;
	@MockBean
	protected CustomDocsProfileService mockCustDocsProfileService;
	@MockBean
	protected CustomDocsFileStreamService mockCustDocsFileStreamService;

	@MockBean
	protected UserStateProfileService mockUserStateProfileService;//CAP-43371

	//CAP-43283
	@MockBean
	protected RoutingInformationService mockRoutingInformationService;

	// CAP-44032
	@MockBean
	protected PaymentInformationService mockPaymentInformationService;

	@MockBean
	protected StandardAttributesService mockStandardAttributesService;

	@MockBean
	protected AlertsService mockAlertsService;

	@MockBean
	protected EdocService mockEdocService;

	@MockBean
	private PropertyHeaderWriter mockCSPHeaderWriter;

	@MockBean
	protected CarouselMessagesService mockCarouselMessageService;

	// CAP-44467
	@MockBean
	protected OrderOnBehalfService mockOrderOnBehalfService;

	@MockBean
	protected USPSValidationService mockUSPSValidationService;

	// CAP-45907
	@MockBean
	protected SelfRegistrationService mockSelfRegistrationService;

	//CAP-46379
	@MockBean
	protected CatalogService mockCatalogService;

	//CAP-46675
	@MockBean
	protected EnforceOnOrderingService mockEnforceOnOrderingService;

	// CAP-46698
	@MockBean
	protected OrderWizardService mockOrderWizardService;

	// CAP-46960
	@MockBean
	protected BudgetAllocationService mockBudgetAllocationService;

	//CAP-47145
	@MockBean
	protected SiteComponentLocatorService mockSiteComponentLocatorService;

	//CAP-47145
	@MockBean
	protected ItemServiceComponentLocatorService mockItemServiceComponentLocatorService;

	//CAP-47145
	@MockBean
	protected SingleItemDetailAllocationsService mockSingleItemDetailAllocationsService;

	//CAP-47145
	@MockBean
	protected ItemValidationComponentLocatorService mockItemValidationComponentLocatorService;

	//CAP-47841
	@MockBean
	protected ManageListsLocatorService mockManageListsLocatorService;

	//CAP-48623
	@MockBean
	protected OrderTemplateService mockOrderTemplateService;

	//CAP-42226
	@MockBean
	protected CustomDocsListService mockCustomDocsListService;

	//CAP-42228
	@MockBean
	protected CustomDocsListMappingService mockCustomDocsListMappingService;

	//CAP-49326
	@MockBean
	protected EFDDestinationService mockEFDDestinationService;

	// CAP-49609
	@MockBean
	protected LinkedLoginService mockLinkedLoginService;

	//CAP-49782
	@MockBean
	protected SuggestedItemsService mockSuggestedItemsService;

	// CAP-50006
	@MockBean
	protected KitTemplateService mockKitTemplateService;

	@MockBean
	protected CSPHeaderResolver mockCSPheaderResolver;

	// CAP-46538
	@MockBean
	protected CustomDocsImprintHistoryService mockCustDocsImprintHistoryService;

	public void setupBaseMockSessions() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getEncodedSessionId()).thenReturn("");
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
	}

	public String getJsonRequest(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		return ow.writeValueAsString(obj);
	}

	public void check422Response(String url, String requestString, String expectedError, String sessionID) throws Exception {
		ResultActions result = null;
		if (Util.isBlankOrNull(requestString))
		{
			result = doGetTest(url, sessionID);
		}
		else
		{
			result = doPostTest(url, sessionID, requestString);
		}
		result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
		.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedError));
	}

	public void checkStreaming422Response(String url, String requestString, String sessionID) throws Exception {
		ResultActions result = null;
		if (Util.isBlankOrNull(requestString))
		{
			result = doGetTest(url, sessionID);
		}
		else
		{
			result = doPostTest(url, sessionID, requestString);
		}
		result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
	}

	public void check403Response(String url, String requestString, String expectedError, String sessionID) throws Exception {
		ResultActions result = null;
		if (Util.isBlankOrNull(requestString))
		{
			result = doGetTest(url, sessionID);
		}
		else
		{
			result = doPostTest(url, sessionID, requestString);
		}
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	public void checkStandard200Response(String url, String requestString, String expectedMessage, String sessionID) throws Exception {
		ResultActions result = null;
		if (Util.isBlankOrNull(requestString))
		{
			result = doGetTest(url, sessionID);
		}
		else
		{
			result = doPostTest(url, sessionID, requestString);
		}
		result.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
		.andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedMessage));

	}

	public void checkStreaming200Response(String url, String requestString, String sessionID) throws Exception {
		ResultActions result = null;
		if (Util.isBlankOrNull(requestString))
		{
			result = doGetTest(url, sessionID);
		}
		else
		{
			result = doPostTest(url, sessionID, requestString);
		}
		result.andExpect(MockMvcResultMatchers.status().isOk());

	}

	public ResultActions doPostTest(String url, String sessionID, String requestString) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.post(url)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", sessionID)
				.contentType(MediaType.APPLICATION_JSON).content(requestString)
				.characterEncoding("utf-8"));
	}

	public ResultActions doGetTest(String url, String sessionID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.get(url)
				.accept(MediaType.APPLICATION_JSON)
				.header("ttsessionid", sessionID)
				.characterEncoding("utf-8"));
	}

}
