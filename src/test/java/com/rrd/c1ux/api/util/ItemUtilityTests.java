package com.rrd.c1ux.api.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.catalogs.vo.CatalogLineVO;
import com.wallace.atwinxs.catalogs.vo.FeatureFavoriteItemData;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;

@WithMockUser
class ItemUtilityTests extends BaseServiceTest {

	@Mock
	CatalogLineVO mockCatalogLineVO;

	@Mock
	FeatureFavoriteItemData featureFavoriteItemData;

	@Mock
	OEOrderSessionBean mockOEOrderSessionBean;
	
	@Mock
	XSCurrency mockXSCurrency;

	@BeforeEach
	public void setUp() throws Exception {
	}

	@Test
	void testBuildFeatureMapSuccess() throws AtWinXSException {
		Map<String, String> expected = new HashMap<>();
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.buildFeatureMap(mockCatalogLineVO, mockAppSessionBean))
					.thenReturn(featureFavoriteItemData);
			assertThat(expected.size(), is(0));
		}
	}

	@Test
	void testgetLineLevelRoutingInformation_true() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.getAssignedApprovalQueue()).thenReturn(1);
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(true, true, 1, 1, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(true, true, 1, 1, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetLineLevelRoutingInformation_true_second() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.getAssignedApprovalQueue()).thenReturn(1);
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(true, true, 0, 0, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(true, true, 0, 0, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}

	@Test
	void testgetLineLevelRoutingInformation_false() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(false, false, 1, 1, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(false, false, 1, 1, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetLineLevelRoutingInformation_false_second() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(false, false, 0, 0, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(false, false, 0, 0, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetLineLevelRoutingInformation_mixed() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(false, true, 5, 0, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(false, true, 5, 0, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetLineLevelRoutingInformation_mixed_replaced() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(false, true, 0, 5, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(false, true, 0, 5, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetLineLevelRoutingInformation_mixed_second() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.getAssignedApprovalQueue()).thenReturn(0);
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(true, false, 0, 1, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(true, false, 0, 1, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}

	@Test
	void testgetLineLevelRoutingInformation_mixed_third() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.getAssignedApprovalQueue()).thenReturn(1);
			mockItemUtility.when(() -> ItemUtility.getLineLevelRoutingInformation(true, false, 0, 1, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getLineLevelRoutingInformation(true, false, 0, 1, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}

	@Test
	void getRoutingInformation() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			Map<String, String> expected = new HashMap<>();
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getRoutingInformation("ABC", "DEF", mockAppSessionBean))
					.thenCallRealMethod();
			expected = ItemUtility.getRoutingInformation("ABC", "DEF", mockAppSessionBean);
			assertThat(expected.size(), is(1));
		}
	}

	@Test
	void getRoutingInformation_empty() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			Map<String, String> expected = new HashMap<>();
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getRoutingInformation("", "", mockAppSessionBean))
					.thenCallRealMethod();
			expected = ItemUtility.getRoutingInformation("", "", mockAppSessionBean);
			assertThat(expected.size(), is(1));
		}
	}

	@Test
	void getRoutingInformation_onewithvalue_otherempty() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			Map<String, String> expected = new HashMap<>();
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getRoutingInformation("ABC", "", mockAppSessionBean))
					.thenCallRealMethod();
			expected = ItemUtility.getRoutingInformation("ABC", "", mockAppSessionBean);
			assertThat(expected.size(), is(1));
		}
	}

	@Test
	void getRoutingInformation_empty_otheronewithvalue() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class)) {
			Map<String, String> expected = new HashMap<>();
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockItemUtility.when(() -> ItemUtility.getRoutingInformation("", "DEF", mockAppSessionBean))
					.thenCallRealMethod();
			expected = ItemUtility.getRoutingInformation("", "DEF", mockAppSessionBean);
			assertThat(expected.size(), is(1));
		}
	}
	
	@Test
	void testgetOrderLevelRoutingInformation_true() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class); MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			when(mockOEOrderSessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.getRouteDollarAmount()).thenReturn(1.0);
			when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);
			when(mockAppSessionBean.getApplyExchangeRate()).thenReturn(true);
			mockUtil.when(()-> Util.getStringFromCurrency(anyDouble(),any(),anyBoolean())).thenReturn(mockXSCurrency);
			when(mockXSCurrency.getAmountText()).thenReturn("1.00");
			mockItemUtility.when(() -> ItemUtility.getOrderLevelRoutingInformation(true, 1, true, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getOrderLevelRoutingInformation(true, 1, true, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetOrderLevelRoutingInformation_true_zero() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class); MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockUtil.when(()-> Util.getStringFromCurrency(anyDouble(),any(),anyBoolean())).thenReturn(mockXSCurrency);
			mockItemUtility.when(() -> ItemUtility.getOrderLevelRoutingInformation(true, 0, true, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getOrderLevelRoutingInformation(true, 0, true, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
	
	@Test
	void testgetOrderLevelRoutingInformation_false_zero() throws AtWinXSException {
		try (MockedStatic<ItemUtility> mockItemUtility = Mockito.mockStatic(ItemUtility.class); MockedStatic<Util> mockUtil = Mockito.mockStatic(Util.class)) {
			mockItemUtility.when(() -> ItemUtility.getTranslationService()).thenReturn(mockTranslationService);
			mockTranslationService.processMessage(any(), any(), any());
			mockUtil.when(()-> Util.getStringFromCurrency(anyDouble(),any(),anyBoolean())).thenReturn(mockXSCurrency);
			mockItemUtility.when(() -> ItemUtility.getOrderLevelRoutingInformation(false, 0, false, mockAppSessionBean,
					mockOEOrderSessionBean)).thenCallRealMethod();
			assertNotNull(ItemUtility.getOrderLevelRoutingInformation(false, 0, false, mockAppSessionBean, mockOEOrderSessionBean));
		}
	}
}
