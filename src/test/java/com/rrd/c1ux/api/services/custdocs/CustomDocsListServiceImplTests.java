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
 * 	04/22/24	R Ruth				CAP-42226		Initial version
 *	05/28/24	A Boomker			CAP-48604		Add save new list API
 */
package com.rrd.c1ux.api.services.custdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXSaveListResponse;
import com.rrd.c1ux.api.models.custdocs.C1uxCustDocListDetails;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.MergeOption;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItemImpl;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.lists.ao.ManageListsResponseBean;
import com.wallace.atwinxs.lists.ao.UploadListAssembler;
import com.wallace.atwinxs.lists.ao.UploadListResponseBean;
import com.wallace.atwinxs.lists.session.ManageListsSession;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;

class CustomDocsListServiceImplTests extends BaseCustomDocsServiceTests {
	@Mock
	 private CustomDocumentItemImpl mockItem;

	@Mock
	private CustomDocumentItem mockDocItem;

	@Mock
	private CustDocUIFormBean mockFormBean;

	@Mock
	private C1UXCustDocListResponse mockResponse;

	@Mock
	private IManageList mockManageList;

	@Mock
	ManageListsResponseBean mockManageLists;

	  @Mock
	  private DistributionListDetails mockDistributionListDetails;

	  @Mock
	  private ManageListsSession mockManageListsSession;

	@Mock
	private UserInterface mockUI;

	@InjectMocks
	private CustomDocsListServiceImpl service;

	@Mock
	private UploadListAssembler mockUploadListAssembler;

	@Test
	void performGetListsApiC1ux() throws Exception {
	      service = Mockito.spy(service);
	      doReturn(mockManageList).when(service).getManageListComponent(any());
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);
//		      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//		      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(AppSessionBean.class), any(),
						any(String.class));

		      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
				when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
				when(mockAppSessionBean.getProfileID()).thenReturn("myProfile");
				when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER);
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
		when(mockManageList.retrieveLists(anyInt(), anyInt(),
				anyString(), anyString(), anyString(), anyList())).thenReturn(null);
		C1UXCustDocListResponse response = service.getListsApi(mockSessionContainer);
		assertTrue(response.isSuccess());
		assertEquals(0, response.getListOfLists().size());

		ArrayList<ListVO> list = new ArrayList<>();
		when(mockManageList.retrieveLists(anyInt(), anyInt(),
				anyString(), anyString(), anyString(), anyList())).thenReturn(list);
		response = service.getListsApi(mockSessionContainer);
		assertTrue(response.isSuccess());
		assertEquals(0, response.getListOfLists().size());

		doReturn(new C1uxCustDocListDetails()).when(service).makeListDetailObjectFromVO(any(), any(), any());
		list.add(getListVO());
		response = service.getListsApi(mockSessionContainer);
		assertTrue(response.isSuccess());
		assertEquals(1, response.getListOfLists().size());

		list.add(null);
		response = service.getListsApi(mockSessionContainer);
		assertFalse(response.isSuccess());

		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.getListsApi(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	private static final String TEST_USER = "AMY";
	private ListVO getListVO() {
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
	void validateCustDocMergeUI()  throws Exception {
		assertFalse(service.validateCustDocMergeUI(null));
		when(mockUI.getMergeOption()).thenReturn(null);
		assertFalse(service.validateCustDocMergeUI(mockUI));
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MERGE);
		assertTrue(service.validateCustDocMergeUI(mockUI));
		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT);
		assertFalse(service.validateCustDocMergeUI(mockUI));
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
		assertTrue(service.validateCustDocMergeUI(mockUI));
	}

	@Test
	void saveNewList() throws Exception {
	      service = Mockito.spy(service);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		      when(mockItem.getUserInterface()).thenReturn(mockUI);
		doReturn(mockManageListsSession).when(service).loadListSession(mockSessionContainer);
		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT)).thenReturn(mockDistributionListDetails);
		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT)).thenReturn(mockManageLists);

	      when(mockOEOrderSession.getCurrentCustomDocumentItem()).thenReturn(mockItem);
			when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
			when(mockAppSessionBean.getProfileID()).thenReturn("myProfile");
			when(mockAppSessionBean.getLoginID()).thenReturn(TEST_USER);
		when(mockUI.getMergeOption()).thenReturn(MergeOption.MAIL_MERGE);
	      when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
	      when(mockAppSessionBean.getCustomToken()).thenReturn(mockCustomizationToken);
		assertNotNull(service.getUploadListAssembler(mockAppSessionBean));
		doReturn(mockUploadListAssembler).when(service).getUploadListAssembler(mockAppSessionBean);
		UploadListResponseBean assemblerResponse = new UploadListResponseBean();
		assemblerResponse.setListID("259");
		assemblerResponse.setListName(TEST_USER);
		assemblerResponse.setRecordCount("999");
		when(mockUploadListAssembler.performAddList(any(), anyString())).thenReturn(assemblerResponse);
		when(mockDistributionListDetails.getIsExcel()).thenReturn(true);
		doNothing().when(service).moveFileIfNotConvertingExcel(any(), any());
		when(mockDistributionListDetails.getOrigExcelFileName()).thenReturn(TEST_USER);
		C1UXSaveListResponse response = service.saveNewList(mockSessionContainer);
		assertTrue(response.isSuccess());
		assertTrue(0 < response.getListID());

		when(mockDistributionListDetails.getIsExcel()).thenReturn(false);
		response = service.saveNewList(mockSessionContainer);
		assertTrue(response.isSuccess());
		assertTrue(0 < response.getListID());

	      doReturn(GENERIC_ERROR_MSG).when(service).getTranslation(any(AppSessionBean.class), any(),
					any(String.class));

		when(mockUploadListAssembler.performAddList(any(), anyString())).thenThrow(new NullPointerException("npe"));
		response = service.saveNewList(mockSessionContainer);
		assertEquals(GENERIC_ERROR_MSG, response.getMessage());


		when(mockManageListsSession.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT)).thenReturn(null);
		response = service.saveNewList(mockSessionContainer);
		assertEquals(GENERIC_ERROR_MSG, response.getMessage());

		when(mockManageListsSession.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT)).thenReturn(null);
		response = service.saveNewList(mockSessionContainer);
		assertEquals(GENERIC_ERROR_MSG, response.getMessage());

		doReturn(null).when(service).loadListSession(mockSessionContainer);
		response = service.saveNewList(mockSessionContainer);
		assertEquals(GENERIC_ERROR_MSG, response.getMessage());

		when(mockUI.getMergeOption()).thenReturn(MergeOption.IMPRINT);
		Exception exception = assertThrows(AccessForbiddenException.class, () -> {
			service.saveNewList(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));
	}

	@Test
	void moveFileIfNotConvertingExcel() throws AtWinXSException {
	    service = Mockito.spy(service);
	    doReturn(true).when(service).isConvertingExcel();
		service.moveFileIfNotConvertingExcel(mockDistributionListDetails, mockAppSessionBean);
		assertTrue(true);
	}
}
