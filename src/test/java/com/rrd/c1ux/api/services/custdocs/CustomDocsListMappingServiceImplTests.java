/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     JIRA#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 * 	05/16/24	A Boomker		CAP-42228			Initial version
 * 	06/04/24	A Boomker		CAP-42231			Adding get mapped data page
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataPage;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingResponse;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.MergeOption;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.UiListMapper;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.UiMappedVariables;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.UiMappedVariablesImpl;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.mapper.ColumnNameWrapper;
import com.wallace.atwinxs.framework.util.mapper.MapperData;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.session.OECustomDocOrderLineMapSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

class CustomDocsListMappingServiceImplTests extends BaseCustomDocsServiceTests {
	@Mock
	 private CustomDocumentItemImpl mockItem;

	@Mock
	private CustomDocumentItem mockDocItem;

	@Mock
	private CustDocUIFormBean mockFormBean;

	@Mock
	private C1UXCustDocListForMappingResponse mockResponse;

	@Mock
	private IManageList mockManageList;

	@Mock
	private UserInterface mockUI;

	@InjectMocks
	private CustomDocsListMappingServiceImpl service;

	@Mock
	private UiListMapper mockUiListMapper;

	@Mock
	private ListVO mockListVO;

	@Mock
	private MapperData mockMapperData;

	@Mock
	private UiMappedVariables mockUiMappedVariable;

	@Mock
	private Map<String, List<OECustomDocOrderLineMapSessionBean>> mockUIListMappings;

	private static final int LIST_ID = 67;

	@Test
	void getListMappings() throws Exception {
	      service = Mockito.spy(service);
	      doReturn(mockManageList).when(service).getManageListComponent(any());
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);

		      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
				when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
				when(mockAppSessionBean.getProfileID()).thenReturn(TEST_USER);
//				when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER);
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenReturn(getListVO());

		doReturn(mockUiListMapper).when(service).getListMapper(any(), any(), anyBoolean(), anyInt());
		doNothing().when(service).populateResponseFromMapper(any(), any(), any(), anyInt());

		C1UXCustDocListForMappingResponse response = service.getListMappings(mockSessionContainer, LIST_ID);
		assertTrue(response.isSuccess());

		Message msg = new Message();
		msg.setErrGeneralMsg(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		doThrow(new AtWinXSMsgException(msg, this.getClass().getName())).when(service).getListMapper(any(), any(), anyBoolean(), anyInt());
		response = service.getListMappings(mockSessionContainer, LIST_ID);
		assertFalse(response.isSuccess());
		assertEquals(BLANK_NOT_ALLOWED_ERR_ENGLISH, response.getMessage());

		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT); // fail merge ui check
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getListMappings(mockSessionContainer, LIST_ID);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockUI.getMergeOption()).thenReturn(MergeOption.MERGE);

		when(mockAppSessionBean.getProfileID()).thenReturn("NotAmy"); // fail visibility check
		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenReturn(getPrivateListVO());
		exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getListMappings(mockSessionContainer, LIST_ID);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenReturn(null); // fail to find the list check
		exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getListMappings(mockSessionContainer, LIST_ID);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	}

	private static final String TEST_USER = "AMY";
	private ListVO getListVO() {
		return new ListVO(DEVTEST_SITE_ID, DEVTEST_UX_BU_ID, 45, CLASSIC_CONTEXT_PATH, AtWinXSConstant.EMPTY_STRING, false,
				new Date(), new Date(),
           		25, TEST_USER, 236236236, "another list name", true,
           		"filea.txt", "fileb.txt", true, "sheet 1", ManageListsConstants.UPLOAD_LIST_STAT_CD_ACTIVE,
           		TEST_USER, GENERIC_ERROR_MSG, TEST_USER, new Date(),
           		ManageListsConstants.LIST_FORMAT_TEXT, ManageListsConstants.UPLOAD_LIST_DELIMITER_COMMA, false,
				false,false, //CP-1253
				TEST_USER,//CP-1557	Store uploaded lists using profileID
				TEST_USER, //CP-2316 paf id // CP-2363 TH - Added to make sure we add to list when creating file
				new Date(), //CP-2316 paf expiration  // CP-2363 TH - Added to make sure we add to list when creating file
				AtWinXSConstant.EMPTY_STRING,//CP-4282 Convert Excel (no original excel here)
				null,
				false); //CAP-29070 IS_ENCRPT_IN
	}

	private ListVO getPrivateListVO() {
		return new ListVO(DEVTEST_SITE_ID, DEVTEST_UX_BU_ID, 45, CLASSIC_CONTEXT_PATH, AtWinXSConstant.EMPTY_STRING, true,
				new Date(), new Date(),
           		25, TEST_USER, 236236236, "another list name", true,
           		"filea.txt", "fileb.txt", true, "sheet 1", ManageListsConstants.UPLOAD_LIST_STAT_CD_ACTIVE,
           		TEST_USER, GENERIC_ERROR_MSG, TEST_USER, new Date(),
           		ManageListsConstants.LIST_FORMAT_TEXT, ManageListsConstants.UPLOAD_LIST_DELIMITER_COMMA, false,
				false,false, //CP-1253
				TEST_USER,//CP-1557	Store uploaded lists using profileID
				TEST_USER, //CP-2316 paf id // CP-2363 TH - Added to make sure we add to list when creating file
				new Date(), //CP-2316 paf expiration  // CP-2363 TH - Added to make sure we add to list when creating file
				AtWinXSConstant.EMPTY_STRING,//CP-4282 Convert Excel (no original excel here)
				null,
				false); //CAP-29070 IS_ENCRPT_IN
	}

	@Test
	void populateResponseFromMapper()  throws Exception {
	      service = Mockito.spy(service);
	      doReturn(new ArrayList<>()).when(service).loadHeaders(mockMapperData);
	      doReturn(new ArrayList<>()).when(service).loadSampleData(mockMapperData);
	      when(mockMapperData.getMaxColumns()).thenReturn(DEVTEST_SITE_ID);
	      doReturn(new ArrayList<>()).when(service).loadMergeVars(mockUiListMapper);
	      doReturn(new ArrayList<>()).when(service).loadMappingFromUI(any(), anyInt(), any());
	      when(mockUiListMapper.getListMapData(anyInt(), anyBoolean())).thenReturn(mockMapperData);
	      C1UXCustDocListForMappingResponse response = new C1UXCustDocListForMappingResponse();
	      service.populateResponseFromMapper(mockUiListMapper, response, mockUI, LIST_ID);
	      assertEquals(DEVTEST_SITE_ID, response.getFileColumns());
	}

	@Test
	void getListMapper() throws AtWinXSException {
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		Mockito.lenient().when(mockEntityObjectMap.getEntity(UiListMapper.class, mockCustomizationToken)).thenReturn(mockUiListMapper);
		when(mockUI.getCurrentCustomDocOrderLineID()).thenReturn(4);
		assertEquals(mockUiListMapper, service.getListMapper(mockAppSessionBean, mockUI, false, LIST_ID));
	}

	@Test
	void validateListVisibility() {
		when(mockListVO.getStatusCode()).thenReturn("D");
		assertFalse(service.validateListVisibility(mockListVO, mockAppSessionBean));

		when(mockListVO.getStatusCode()).thenReturn("A");
		when(mockListVO.isPrivate()).thenReturn(true);
		when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER);
		when(mockAppSessionBean.getProfileID()).thenReturn(TEST_USER);
		when(mockListVO.getLoginID()).thenReturn(TEST_USER+"3");
		when(mockListVO.getProfileID()).thenReturn(TEST_USER+"2");
		assertFalse(service.validateListVisibility(mockListVO, mockAppSessionBean));

		when(mockListVO.getProfileID()).thenReturn(TEST_USER);
		assertTrue(service.validateListVisibility(mockListVO, mockAppSessionBean));

		when(mockAppSessionBean.getProfileID()).thenReturn(AtWinXSConstant.EMPTY_STRING);
		assertFalse(service.validateListVisibility(mockListVO, mockAppSessionBean));

		when(mockListVO.getLoginID()).thenReturn(TEST_USER);
		assertTrue(service.validateListVisibility(mockListVO, mockAppSessionBean));

		when(mockListVO.isPrivate()).thenReturn(false);
		assertTrue(service.validateListVisibility(mockListVO, mockAppSessionBean));
	}

	@Test
	void loadMergeVars() throws AtWinXSException {
		when(mockUiListMapper.getUiMappedVariablesC1UX()).thenReturn(null);
		assertEquals(0, service.loadMergeVars(mockUiListMapper).size());

		ArrayList<UiMappedVariables> vars = new ArrayList<>();
		vars.add(mockUiMappedVariable);
		when(mockUiMappedVariable.getTextVariableName()).thenReturn(FAKE_UPLOAD_LIST_VALUE);
		when(mockUiMappedVariable.getDisplayLabel()).thenReturn(TEST_USER);
		when(mockUiMappedVariable.isRequired()).thenReturn(false);
		when(mockUiListMapper.getUiMappedVariablesC1UX()).thenReturn(vars);
		assertEquals(1, service.loadMergeVars(mockUiListMapper).size());

		when(mockUiMappedVariable.isRequired()).thenReturn(true);
		assertEquals(1, service.loadMergeVars(mockUiListMapper).size());
	}

	@Test
	void loadHeaders() {
		int numColumns = 5;
		when(mockMapperData.getMaxColumns()).thenReturn(numColumns);
		when(mockMapperData.getHeadings()).thenReturn(null);
		assertEquals(numColumns, service.loadHeaders(mockMapperData).size());

		ArrayList<ColumnNameWrapper> headings = new ArrayList<>();
		when(mockMapperData.getHeadings()).thenReturn(headings);
		assertEquals(numColumns, service.loadHeaders(mockMapperData).size());

		headings = new ArrayList<>();
		headings.add(new ColumnNameWrapper(TEST_USER));
		headings.add(new ColumnNameWrapper(BLANK_NOT_ALLOWED_ERR_ENGLISH));
		headings.add(new ColumnNameWrapper(CURRENT_CART_ERR_ENGLISH));
		when(mockMapperData.getHeadings()).thenReturn(headings);
		assertEquals(headings.size(), service.loadHeaders(mockMapperData).size());
	}

	private static final String varCheck = "VarB";
	private static final String varCheck2 = "VarC";

	@Test
	void loadMappingFromUI() {
		when(mockUI.getListMappings()).thenReturn(mockUIListMappings);
		when(mockUIListMappings.get(anyString())).thenReturn(null);
		assertEquals(0, service.loadMappingFromUI(mockUI, LIST_ID, null).size());

		List<OECustomDocOrderLineMapSessionBean> mapBeans = new ArrayList<>();
		when(mockUIListMappings.get(anyString())).thenReturn(mapBeans);
		List<String> fakeHeaders = getFakeHeaders();
		assertEquals(0, service.loadMappingFromUI(mockUI, LIST_ID, fakeHeaders).size());

		mapBeans = getFakeMapBeans();
		when(mockUIListMappings.get(anyString())).thenReturn(mapBeans);
		List<String> mappings = service.loadMappingFromUI(mockUI, LIST_ID, fakeHeaders);
		assertEquals(fakeHeaders.size(), mappings.size());
		assertFalse(mappings.contains(varCheck));
		assertTrue(mappings.contains(varCheck2));
	}

	private List<String> getFakeHeaders() {
		ArrayList<String> headers = new ArrayList<>();
		headers.add(TEST_USER);
		headers.add(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		headers.add(CURRENT_CART_ERR_ENGLISH);
		headers.add(DEFAULT_VIEW);
		return headers;
	}

	private ArrayList<OECustomDocOrderLineMapSessionBean> getFakeMapBeans() {
		ArrayList<OECustomDocOrderLineMapSessionBean> mapBeans = new ArrayList<>();
		OECustomDocOrderLineMapSessionBean bean1 = new OECustomDocOrderLineMapSessionBean();
		bean1.setMapColName(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		bean1.setPageFlxVariableName("VarA");

		OECustomDocOrderLineMapSessionBean bean2 = new OECustomDocOrderLineMapSessionBean();
		bean2.setMapColName(AtWinXSConstant.EMPTY_STRING);
		bean2.setPageFlxVariableName(varCheck);

		OECustomDocOrderLineMapSessionBean bean3 = new OECustomDocOrderLineMapSessionBean();
		bean3.setMapColName(TEST_USER);
		bean3.setPageFlxVariableName(varCheck2);

		OECustomDocOrderLineMapSessionBean bean4 = new OECustomDocOrderLineMapSessionBean();
		bean4.setMapColName(CURRENT_CART_ERR_ENGLISH);
		bean4.setPageFlxVariableName("VarD");

		mapBeans.add(bean1);
		mapBeans.add(bean2);
		mapBeans.add(bean3);
		mapBeans.add(bean4);
		return mapBeans;
	}

	@Test
	void loadSampleData() {
		when(mockMapperData.getDataVector()).thenReturn(null);
		when(mockMapperData.getMaxColumns()).thenReturn(4);
		assertTrue(service.loadSampleData(mockMapperData).isEmpty());

		when(mockMapperData.getDataVector()).thenReturn(getSampleData());
		List<ArrayList<String>> results = service.loadSampleData(mockMapperData);
		assertFalse(results.isEmpty());
		assertEquals(4, results.size());
		assertEquals(3, results.get(0).size());
	}

	private Collection<String[]> getSampleData() {
		Collection<String[]> data = new ArrayList<>();
		String[] row1 = { "A", "B", "C", "D"};
		String[] row2 = { "A2", "B2", "C2", "D2"};
		String[] row3 = { "A3", "B3", "C3", "D3"};
		String[] row4 = { "A4", "B4", "C4", "D4"};

		data.add(row1);
		data.add(row2);
		data.add(row3);
		data.add(row4);

		return data;
	}

	@Test
	void checkForDuplicateMappings() {
	      service = Mockito.spy(service);
	      OECustomDocOrderLineMapSessionBean bean = new OECustomDocOrderLineMapSessionBean();
	      bean.setPageFlxVariableName(varCheck);
	      ArrayList<UiMappedVariables> mapVars = new ArrayList<>();
	      when(mockUiMappedVariable.getTextVariableName()).thenReturn(varCheck2);
	      UiMappedVariables mapvar = new UiMappedVariablesImpl();
			doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));

	      HashSet<String> mappedVarNames = new HashSet<>();
	      ArrayList<String> errors = new ArrayList<>();
	      ArrayList<String> dups = new ArrayList<>();
	      HashSet<UiMappedVariables> required = new HashSet<UiMappedVariables>();
	      service.checkForDuplicateMappings(bean, mapVars, mappedVarNames, dups, required, errors, mockAppSessionBean );
	      assertFalse(errors.isEmpty());

	      errors.clear();
	      mapVars.add(mockUiMappedVariable);
	      service.checkForDuplicateMappings(bean, mapVars, mappedVarNames, dups, required, errors, mockAppSessionBean );
	      assertFalse(errors.isEmpty());

	      errors.clear();
	      when(mockUiMappedVariable.getTextVariableName()).thenReturn(varCheck);
	      when(mockUiMappedVariable.isRequired()).thenReturn(true);
	      service.checkForDuplicateMappings(bean, mapVars, mappedVarNames, dups, required, errors, mockAppSessionBean );
	      assertTrue(errors.isEmpty());
	      assertFalse(mappedVarNames.isEmpty());
	      assertFalse(required.isEmpty());

	      service.checkForDuplicateMappings(bean, mapVars, mappedVarNames, dups, required, errors, mockAppSessionBean );
	      assertFalse(dups.isEmpty());

	      when(mockUiMappedVariable.isRequired()).thenReturn(false);
	      when(mockUiMappedVariable.getTextVariableName()).thenReturn(varCheck2);
	      bean.setPageFlxVariableName(varCheck2);
	      required.clear();
	      service.checkForDuplicateMappings(bean, mapVars, mappedVarNames, dups, required, errors, mockAppSessionBean );
	      assertTrue(errors.isEmpty());
	      assertFalse(mappedVarNames.isEmpty());
	      assertTrue(required.isEmpty());
	}

	@Test
	void makeMapBean() {
	      service = Mockito.spy(service);
	      doNothing().when(service).checkForDuplicateMappings(any(), any(), any(), any(), any(), any(), any());
	      ArrayList<String> errors = new ArrayList<>();
		Set<String> variableNamesSet = new HashSet<String>();
		Set<UiMappedVariables> requiredMappedVars = new HashSet<UiMappedVariables>();
		List<String> duplicateMappings = new ArrayList<>();
		ArrayList<UiMappedVariables> uiMappedVariables = new ArrayList<>();
		C1UXCustDocSaveListMappingBean col = new C1UXCustDocSaveListMappingBean();
		OECustomDocOrderLineMapSessionBean bean = service.makeMapBean(LIST_ID, col, uiMappedVariables, variableNamesSet, duplicateMappings, requiredMappedVars, errors, mockAppSessionBean);
		assertEquals(OrderEntryConstants.NOT_MAPPED_COL, bean.getPageFlxVariableLabel());
		col.setVariableDisplayLabel(TEST_USER + "*");
		col.setVariableName(varCheck);

		bean = service.makeMapBean(LIST_ID, col, uiMappedVariables, variableNamesSet, duplicateMappings, requiredMappedVars, errors, mockAppSessionBean);
		assertNotEquals(OrderEntryConstants.NOT_MAPPED_COL, bean.getPageFlxVariableLabel());
		assertEquals(varCheck, bean.getPageFlxVariableName());
	}

	private C1UXCustDocSaveListMappingRequest getValidSaveMappingRequest() {
	      C1UXCustDocSaveListMappingRequest request = new C1UXCustDocSaveListMappingRequest();
	      List<C1UXCustDocSaveListMappingBean> listMapping = new ArrayList<>();
			C1UXCustDocSaveListMappingBean col = new C1UXCustDocSaveListMappingBean();
			listMapping.add(col);
			col = new C1UXCustDocSaveListMappingBean();
			col.setVariableDisplayLabel(TEST_USER + "*");
			col.setVariableName(varCheck);
			listMapping.add(col);
			request.setListColumnMap(listMapping);
			return request;
	}

	@Test
	void buildAndValidateMapping() throws AtWinXSException {
	      service = Mockito.spy(service);
	      ArrayList<String> errors = new ArrayList<>();
	      C1UXCustDocSaveListMappingRequest request = getValidSaveMappingRequest();
			when(mockListVO.getListID()).thenReturn(LIST_ID);
			doReturn(mockUiListMapper).when(service).getListMapper(any(), any(), anyBoolean(), anyInt());
			ArrayList<UiMappedVariables> vars = new ArrayList<>();
			vars.add(mockUiMappedVariable);
			when(mockUiListMapper.getUiMappedVariablesC1UX()).thenReturn(vars);
			doReturn(new OECustomDocOrderLineMapSessionBean()).when(service).makeMapBean(anyInt(), any(), any(), any(), any(), any(), any(), any());
			doNothing().when(service).checkForMissingRequiredMappings(any(), any(), any(), any());
			doNothing().when(service).checkForMissingColumns(anyInt(), anyInt(), any());
			doNothing().when(service).makeDuplicationErrors(any(), any(), any());
			service.buildAndValidateMapping(mockUI, request, mockListVO, mockAppSessionBean, errors);
			assertTrue(errors.isEmpty());
	}

	@Test
	void createMappingBeans() throws AtWinXSException {
		C1UXCustDocSaveListMappingResponse response = new C1UXCustDocSaveListMappingResponse();
		C1UXCustDocSaveListMappingRequest request = new C1UXCustDocSaveListMappingRequest();
	      service = Mockito.spy(service);
	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));
	      doReturn(new ArrayList<OECustomDocOrderLineMapSessionBean>()).when(service).buildAndValidateMapping(any(), any(), any(), any(), any());
	      service.createMappingBeans(response, mockUI, request, mockListVO, mockAppSessionBean);
	      assertFalse(response.isSuccess());
	      assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());

	      request.setMaxColumns(5);
	      when(mockListVO.getRecordCount()).thenReturn(3);
	      when(mockListVO.getListID()).thenReturn(267);
	      doReturn(3).when(service).countMappedColumns(any());
	      response = new C1UXCustDocSaveListMappingResponse();
	      service.createMappingBeans(response, mockUI, request, mockListVO, mockAppSessionBean);
	      assertTrue(response.isSuccess());
	      assertEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());

			doAnswer(invocation -> {
				List<String> errors = (List<String>) invocation.getArgument(4);
				errors.add(GENERIC_ERROR_MSG);
				return new ArrayList<OECustomDocOrderLineMapSessionBean>();
			}).when(service).buildAndValidateMapping(any(), any(), any(), any(), any());
		      response = new C1UXCustDocSaveListMappingResponse();
		      service.createMappingBeans(response, mockUI, request, mockListVO, mockAppSessionBean);
		      assertFalse(response.isSuccess());
		      assertNotEquals(AtWinXSConstant.EMPTY_STRING, response.getMessage());
	}

	@Test
	void countMappedColumns() {
		List<OECustomDocOrderLineMapSessionBean> map = new ArrayList<>();
		assertEquals(0, service.countMappedColumns(map));
		map.add(new OECustomDocOrderLineMapSessionBean());
		assertEquals(0, service.countMappedColumns(map));

		OECustomDocOrderLineMapSessionBean bean = new OECustomDocOrderLineMapSessionBean();
		bean.setPageFlxVariableName(TEST_USER);
		map.add(bean);
		assertEquals(1, service.countMappedColumns(map));
	}

	@Test
	void updateUIMapping() throws AtWinXSException {
		when(mockListVO.getListID()).thenReturn(2);
		Map<String, List<OECustomDocOrderLineMapSessionBean>> maps = new HashMap<String, List<OECustomDocOrderLineMapSessionBean>>();
		when(mockUI.getListMappings()).thenReturn(maps);
		service.updateUIMapping(mockUI, new ArrayList<OECustomDocOrderLineMapSessionBean>(), mockListVO);
		assertFalse(maps.isEmpty());
	}

	@Test
	void makeDuplicationErrors() {
	      service = Mockito.spy(service);
	      ArrayList<String> errors = new ArrayList<>();
	      ArrayList<String> dups = new ArrayList<>();
	      service.makeDuplicationErrors(dups, errors, mockAppSessionBean);
	      assertTrue(errors.isEmpty());

	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));
	      dups.add(varCheck);
	      service.makeDuplicationErrors(dups, errors, mockAppSessionBean);
	      assertFalse(errors.isEmpty());
	}

	@Test
	void checkForMissingColumns() {
	      service = Mockito.spy(service);
	      ArrayList<String> errors = new ArrayList<>();
	      service.checkForMissingColumns(LIST_ID, LIST_ID, errors);
	      assertTrue(errors.isEmpty());

	      service.checkForMissingColumns(LIST_ID, 1, errors);
	      assertFalse(errors.isEmpty());
	}

	@Test
	void checkForMissingRequiredMappings() {
	      service = Mockito.spy(service);
	      ArrayList<UiMappedVariables> mapVars = new ArrayList<>();
	      mapVars.add(mockUiMappedVariable);
	      when(mockUiMappedVariable.getTextVariableName()).thenReturn(varCheck2);
	      when(mockUiMappedVariable.getDisplayLabel()).thenReturn(varCheck2);
	      when(mockUiMappedVariable.isRequired()).thenReturn(false);
	      ArrayList<String> errors = new ArrayList<>();
	      HashSet<String> mappedVarNames = new HashSet<>();
	      service.checkForMissingRequiredMappings(mappedVarNames, mapVars, errors, mockAppSessionBean);
	      assertTrue(errors.isEmpty());

	      when(mockUiMappedVariable.isRequired()).thenReturn(true);
	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));
	      service.checkForMissingRequiredMappings(mappedVarNames, mapVars, errors, mockAppSessionBean);
	      assertFalse(errors.isEmpty());

	      errors.clear();
	      mappedVarNames.add(varCheck2);
	      service.checkForMissingRequiredMappings(mappedVarNames, mapVars, errors, mockAppSessionBean);
	      assertTrue(errors.isEmpty());
	}

	@Test
	void saveListMappings() throws Exception {
	      service = Mockito.spy(service);
	      doReturn(mockManageList).when(service).getManageListComponent(any());
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);

		      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
				when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
				when(mockAppSessionBean.getProfileID()).thenReturn(TEST_USER);
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenReturn(getListVO());

	      C1UXCustDocSaveListMappingRequest request = getValidSaveMappingRequest();
			ArrayList<UiMappedVariables> vars = new ArrayList<>();
			vars.add(mockUiMappedVariable);

			doNothing().when(service).saveFullOESessionInfo(any(), anyInt());
			doAnswer(invocation -> {
				C1UXCustDocSaveListMappingResponse response = (C1UXCustDocSaveListMappingResponse) invocation.getArgument(0);
				response.setSuccess(true);
				return null;
			}).when(service).createMappingBeans(any(), any(), any(), any(), any());

		C1UXCustDocSaveListMappingResponse response = service.saveListMapping(mockSessionContainer, request);
		assertTrue(response.isSuccess());

		Message msg = new Message();
		msg.setErrGeneralMsg(BLANK_NOT_ALLOWED_ERR_ENGLISH);
		doThrow(new AtWinXSMsgException(msg, this.getClass().getName())).when(service).createMappingBeans(any(), any(), any(), any(), any());
		response = service.saveListMapping(mockSessionContainer, request);
		assertFalse(response.isSuccess());
		assertEquals(BLANK_NOT_ALLOWED_ERR_ENGLISH, response.getMessage());

		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT); // fail merge ui check
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.saveListMapping(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockUI.getMergeOption()).thenReturn(MergeOption.MERGE);

		when(mockAppSessionBean.getProfileID()).thenReturn("NotAmy"); // fail visibility check
		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenReturn(getPrivateListVO());
		exception = assertThrows(AccessForbiddenException.class, () -> {
			service.saveListMapping(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenReturn(null); // fail to find the list check
		exception = assertThrows(AccessForbiddenException.class, () -> {
			service.saveListMapping(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(eq(mockAppSessionBean), any(String.class),
					any(String.class));
		when(mockManageList.retrieveSingleList(anyInt(), anyInt(), anyInt())).thenThrow(new NullPointerException("npx"));
		response = service.saveListMapping(mockSessionContainer, request);
		assertFalse(response.isSuccess());
		assertEquals(GENERIC_ERROR_MSG, response.getMessage());
	}

	@Test
	void updateSavedMappingFlags() throws AtWinXSException	{
		String mappedListID = String.valueOf(DEVTEST_UX_BU_ID);
		HashMap<String, List<OECustomDocOrderLineMapSessionBean>> maps = new HashMap<>();
		ArrayList<OECustomDocOrderLineMapSessionBean> mapBeans = new ArrayList<>();
		HashMap<String, Boolean> changeMap = new HashMap<>();
		when(mockUI.getListMappings()).thenReturn(maps);
		when(mockUI.getSavedMappingChanged()).thenReturn(changeMap);

		service.updateSavedMappingFlags(mockUI, new ArrayList<OECustomDocOrderLineMapSessionBean>(), mappedListID);
		assertFalse(changeMap.isEmpty());
		assertNotNull(changeMap.get(mappedListID));
		assertEquals(false, changeMap.get(mappedListID));

		maps.put(mappedListID, new ArrayList<OECustomDocOrderLineMapSessionBean>());
		changeMap.clear();
		service.updateSavedMappingFlags(mockUI, new ArrayList<OECustomDocOrderLineMapSessionBean>(), mappedListID);
		assertFalse(changeMap.isEmpty());
		assertNotNull(changeMap.get(mappedListID));
		assertEquals(false, changeMap.get(mappedListID));

		changeMap.clear();
		service = Mockito.spy(service);
		doReturn(true).when(service).compareMappings(any(), any());
		service.updateSavedMappingFlags(mockUI, getFakeMapBeans(), mappedListID);
		assertFalse(changeMap.isEmpty());
		assertNotNull(changeMap.get(mappedListID));
		assertEquals(false, changeMap.get(mappedListID));

		maps.put(mappedListID, getFakeMapBeans());
		changeMap.clear();
		service.updateSavedMappingFlags(mockUI, getFakeMapBeans(), mappedListID);
		assertFalse(changeMap.isEmpty());
		assertNotNull(changeMap.get(mappedListID));
		assertEquals(true, changeMap.get(mappedListID));

		changeMap.clear();
		doReturn(false).when(service).compareMappings(any(), any());
		service.updateSavedMappingFlags(mockUI, getFakeMapBeans(), mappedListID);
		assertFalse(changeMap.isEmpty());
		assertNotNull(changeMap.get(mappedListID));
		assertEquals(false, changeMap.get(mappedListID));
	}

	@Test
	void getMappedDataPage() throws Exception {
	      service = Mockito.spy(service);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);
		      when(mockUI.getNextPageNumber()).thenReturn(UserInterface.NEXT_PAGE_NUMBER_LIST_DATA);
		      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
				when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
		doReturn(mockOrderListVO).when(service).getOrderListVO(mockUI, mockAppSessionBean);
		doReturn(new C1UXCustDocMappedDataPage()).when(service).loadListDataPage(anyInt(), anyBoolean(), any(), any());
		C1UXCustDocMappedDataResponse formBean = null;
		C1UXCustDocMappedDataRequest request = new C1UXCustDocMappedDataRequest();
		formBean = service.getMappedDataPage(mockSessionContainer, request);
		assertTrue(formBean.isSuccess());

		doThrow(new AtWinXSException(BLANK_NOT_ALLOWED_ERR_ENGLISH, "amylistclass")).when(service).loadListDataPage(anyInt(), anyBoolean(), any(), any());
		request.setValidValues(false);
		formBean = service.getMappedDataPage(mockSessionContainer, request);
		assertFalse(formBean.isSuccess());

		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT); // fail merge ui check
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getMappedDataPage(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		when(mockUI.getMergeOption()).thenReturn(MergeOption.MERGE);
	    when(mockUI.getNextPageNumber()).thenReturn(4);
		exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getMappedDataPage(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));

		doReturn(null).when(service).getOrderListVO(mockUI, mockAppSessionBean);
		exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getMappedDataPage(mockSessionContainer, request);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

}
