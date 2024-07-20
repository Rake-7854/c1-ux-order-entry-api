/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 *  11/29/23	Krishna Natarajan	CAP-45483		Added tests for the method that gets the Module and Customer Item Number from login query string parameters
 *  12/19/23	Krishna Natarajan	CAP-45596		Added tests for the Modify SSO to look for Entry Point Catalog Ordering  
 *  12/07/23	N Caceres			CAP-45601		Added tests for Order Search Entry Point
 *  12/30/23	N Caceres			CAP-45599		Update tests to cover catalog search results
 */

package com.rrd.c1ux.api.services.settingsandprefs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.catalog.CatalogMenuProcessor;
import com.rrd.c1ux.api.models.settingsandprefs.SettingsandPrefs;
import com.rrd.custompoint.gwt.catalog.entity.CatalogSearchResultsCriteria;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.admin.component.GroupUserComponentHelper;
import com.wallace.atwinxs.orderentry.admin.vo.OrderOnBehalfVO;
import com.wallace.atwinxs.orderentry.admin.vo.UserGroupOrderStatusPropertiesVO;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

class CatalogUtilityNavigationInfoServiceImplTests extends BaseOEServiceTest {
	
	private static final String CUST_ITEM_NUM = "custItemNum";

	private static final String UG_NAME = "C1UX";

	private static final int ID = 1;

	private static final String SALESREFNUM = "salesrefnum";

	private static final String MODULE = "Module";

	@InjectMocks
	CatalogUtilityNavigationInfoServiceImpl catalogUtilityNavigationInfoServiceImpl;
	
	@Mock
	CatalogMenuProcessor mockCatalogMenuProcessor;
	
	@Mock
	OrderEntrySession mockOrderEntrySession;
	
	@Mock
	OrderOnBehalfVO mockOrderOnBehalfVO;
	
	@Mock
	OEItemSearchCriteriaSessionBean mockOEItemSearchCriteriaSessionBean;
	
	@Mock
	CatalogSearchResultsCriteria mockCatalogSearchResultsCriteria;
	
	@Mock
	SettingsandPrefs mockSettingsandPrefs;
	
	@Mock
	private Order mockOrder;
	
	@Mock
	private GroupUserComponentHelper mockHelper;
	
	@Mock
	private UserGroupOrderStatusPropertiesVO mockStatus;
	
	@ParameterizedTest
	@MethodSource("getTestDataForVIandCSR")
	void setModuleAndOtherParameters(String module, String custIteNum, boolean hasService) throws CPRPCException, AtWinXSException {
		SettingsandPrefs settingsAndPref= new SettingsandPrefs();
		Properties props= new Properties();
		props.put(MODULE, module);
		props.put(CUST_ITEM_NUM, custIteNum);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(AtWinXSConstant.ORDERS_SERVICE_ID)).thenReturn(hasService);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(props);
		when(mockAppSessionBean.getEntryPoint()).thenReturn("");
		catalogUtilityNavigationInfoServiceImpl.setModuleAndOtherParameters(mockSessionContainer, settingsAndPref);
		assertNotNull(props);
	}
	
	@Test
	void setModuleAndOtherParameters_emptyparams() throws CPRPCException, AtWinXSException {
		SettingsandPrefs settingsAndPref= new SettingsandPrefs();
		Properties props= new Properties();
		props.put(MODULE, "");
		props.put(CUST_ITEM_NUM, "");
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(props);
		when(mockAppSessionBean.getEntryPoint()).thenReturn("");
		catalogUtilityNavigationInfoServiceImpl.setModuleAndOtherParameters(mockSessionContainer, settingsAndPref);
		assertNotNull(props);
	}
	
	@Test
	void setModuleAndOtherParameters_withoutparams() throws CPRPCException, AtWinXSException {
		SettingsandPrefs settingsAndPref= new SettingsandPrefs();
		Properties props= new Properties();
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(props);
		when(mockAppSessionBean.getEntryPoint()).thenReturn("");
		catalogUtilityNavigationInfoServiceImpl.setModuleAndOtherParameters(mockSessionContainer, settingsAndPref);
		assertNotNull(props);
	}
	
	private static Stream<Arguments> getTestDataForVIandCSR() {
		return Stream.of(Arguments.of("VI", "1018", true),
				Arguments.of("VI", "1018", false),
				Arguments.of("CSR", "1234", true),
				Arguments.of("CSR", "1234", false));
	}
	
	private static Stream<Arguments> checkCustItemNumberData() {
		return Stream.of(Arguments.of("VI", "1018"),
				Arguments.of("VI", ""),
				Arguments.of("", "1018"),
				Arguments.of("CSR", "1234"),
				Arguments.of("", "1234"),
				Arguments.of("CSR", ""),
				Arguments.of("CSR", "CSR"),
				Arguments.of("VI", "VI"),
				Arguments.of("ABC", "1234"),
				Arguments.of("", ""));
	}
	
	@ParameterizedTest
	@MethodSource("checkCustItemNumberData")
	void checkCustItemNumber() {
		Properties propswithoutvalues= new Properties();
		propswithoutvalues.put(MODULE, "");
		propswithoutvalues.put(CUST_ITEM_NUM, "");
		catalogUtilityNavigationInfoServiceImpl.checkCustItemNumber(propswithoutvalues.entrySet().iterator());
		assertNotNull(propswithoutvalues);
	}
	
	@Test
	void getRouterLink_withParams() {
		ArrayList tnVO1= new ArrayList<>();
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		tnVO.setChildren(tnVO1);
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.getRouterLink(ltn,"ABC","ABC","ABC","ABC");
		assertNotNull(tnVO);
	}
	
	@Test
	void getRouterLink_withParams_wchildren() {
		ArrayList tnVO1= new ArrayList<>();
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		tnVO.setChildren(tnVO1);
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.getRouterLink(ltn,"abc","CDE","abc1","abc2");
		assertNotNull(tnVO);
	}
	
	@Test
	void getRouterLink_withParams_woutchildren() {
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.getRouterLink(ltn,"abc","CDE","abc1","abc2");
		assertNotNull(tnVO);
	}
	
	@Test
	void getRouterLink_withoutParams() {
		TreeNodeVO tnVO= new TreeNodeVO();
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.getRouterLink(ltn,"abc","abc","abc1","abc2");
		assertNotNull(tnVO);
	}
	
	@Test 
	void updateSetAndPrefsForCat(){
		Properties propswithmodulevalue = new Properties();
		propswithmodulevalue.put("Module", "CO");
		propswithmodulevalue.put("Cat", "Availability");
		catalogUtilityNavigationInfoServiceImpl.updateSetAndPrefsForCat(propswithmodulevalue.entrySet().iterator(), mockSettingsandPrefs);
		assertNotNull(propswithmodulevalue);
	}

	@ParameterizedTest
	@MethodSource("getTestData")
	void testProcessOrderSearchDetailsEntryPoint(String module, String salesRefNum, Date date, boolean hasService) throws AtWinXSException, CPRPCException {
		SettingsandPrefs settingsAndPref= new SettingsandPrefs();
		Properties props= new Properties();
		props.put(MODULE, module);
		props.put(SALESREFNUM, salesRefNum);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(props);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getSiteID()).thenReturn(ID);
		when(mockAppSessionBean.getBuID()).thenReturn(ID);
		when(mockAppSessionBean.getGroupName()).thenReturn(UG_NAME);
		when(mockAppSessionBean.getEntryPoint()).thenReturn("");
		when(mockOrder.getOrderPlacedTimestamp()).thenReturn(date);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockOrder);
		SettingsandPrefs setandprefs = catalogUtilityNavigationInfoServiceImpl.setModuleAndOtherParameters(mockSessionContainer, settingsAndPref);
		assertNotNull(setandprefs);
	}
	
	@ParameterizedTest
	@MethodSource("getTestDataInvalid")
	void testProcessOrderSearchDetailsEntryPoint_fail(String module, String salesRefNum, Date date, boolean hasService) throws AtWinXSException, CPRPCException {
		SettingsandPrefs settingsAndPref= new SettingsandPrefs();
		Properties props= new Properties();
		props.put(MODULE, module);
		props.put(SALESREFNUM, salesRefNum);
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockAppSessionBean.hasService(anyInt())).thenReturn(true);
		when(mockAppSessionBean.getLoginQueryString()).thenReturn(props);
		when(mockAppSessionBean.getSiteID()).thenReturn(ID);
		when(mockAppSessionBean.getBuID()).thenReturn(ID);
		when(mockAppSessionBean.getGroupName()).thenReturn(UG_NAME);
		when(mockAppSessionBean.getEntryPoint()).thenReturn("");
		SettingsandPrefs setandprefs = catalogUtilityNavigationInfoServiceImpl.setModuleAndOtherParameters(mockSessionContainer, settingsAndPref);
		assertNotNull(setandprefs);
	}
	
	private static Stream<Arguments> getTestData() {
		return Stream.of(Arguments.of("OSD", "80033253", new Date(), true),
				Arguments.of("OSD", "1234", new Date(), true),
				Arguments.of("OSD", "1234", null, true),
				Arguments.of("OSD", "80033253", new Date(), false));
	}
	
	private static Stream<Arguments> getTestDataInvalid() {
		return Stream.of(Arguments.of("OSX", "1234", null, true),
				Arguments.of("OSX", "1234", new Date(), true),
				Arguments.of("OSX", "1234", new Date(), false),
				Arguments.of("", "", new Date(), true));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"Y", "S", "N", ""})
	void testSetOrderSearchScope(String input) {
		SettingsandPrefs setandprefs = new SettingsandPrefs();
		catalogUtilityNavigationInfoServiceImpl.setOrderSearchScope(setandprefs, input);
		assertNotNull(setandprefs.getScope());
	}

	@Test
	void loopThroughLoopChildren() {
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.loopThroughLoopChildren(ltn, "ABC", "shopbycatalog", "ABC", "ABC");
		assertNotNull(tnVO);
	}
	
	@Test
	void loopThroughLoopChildren_different_param() {
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.loopThroughLoopChildren(ltn, "CDE", "shopbycatalog", "CDE", "CDE");
		assertNotNull(tnVO);
	}
	
	@Test
	void loopThroughLoopChildren_different_param_child() {
		ArrayList tnVO1= new ArrayList<>();
		tnVO1.add("123");
		tnVO1.add("ABC");
		
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		tnVO.setChildren(tnVO1);
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.loopThroughLoopChildren(ltn, "CDE", "shopbycatalog", "CDE", "CDE");
		assertNotNull(tnVO);
	}
	
	@Test
	void loopThroughLoopChildren_different_param_child_true() {
		ArrayList tnVO1= new ArrayList<>();
		tnVO1.add("123");
		tnVO1.add("ABC");
		
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		tnVO.setChildren(tnVO1);
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.loopThroughLoopChildren(ltn, "CDE", "shopbycatalog", "CDE", "CDE");
		assertNotNull(tnVO);
	}
	
	@Test
	void loopThroughInnerLoopChildren_different_param() {
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.loopThroughInnerLoopChildren(ltn, "ABC", "shopbycatalog", "ABC", false);
		assertNotNull(tnVO);
	}
	
	@Test
	void loopThroughInnerLoopChildren_different_param_truevalue() {
		TreeNodeVO tnVO= new TreeNodeVO();
		tnVO.setNodeID(123);
		tnVO.setNodeName("ABC");
		List<TreeNodeVO> ltn= new ArrayList();
		ltn.add(tnVO);
		catalogUtilityNavigationInfoServiceImpl.loopThroughInnerLoopChildren(ltn, "ABC", "shopbycatalog", "ABC", true);
		assertNotNull(tnVO);
	}
}
