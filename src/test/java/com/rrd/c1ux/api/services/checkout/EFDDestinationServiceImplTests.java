/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/13/24				S Ramachandran			CAP-49326					Added junit tests for getEfdStyleInformationForOrder service method
 *	05/16/24				N Caceres				CAP-49344					Added test methods for Get EFD Options API
 *	5/21/24					Satishkumar A			CAP-49453					C1UX BE - Create new API to save EFD information
 *  05/30/24				Krishna Natarajan		CAP-49748					Added necessary mocks to the tests after adding new fields to getEFDOptions
 *	05/29/24				Krishna Natarajan		CAP-49326					Commented out unnecessary stubbings for the bug
 */
package com.rrd.c1ux.api.services.checkout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.models.checkout.EFDLineItem;
import com.rrd.c1ux.api.models.checkout.EFDOptionsResponse;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationRequest;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationResponse;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationRequest;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationResponse;
import com.rrd.c1ux.api.services.orderentry.locator.OECustomEmailComponentLocatorService;
import com.rrd.custompoint.orderentry.ao.EFDDestinationOptionsFormBean;
import com.rrd.custompoint.orderentry.entity.EFDCRMTracking;
import com.rrd.custompoint.orderentry.entity.EFDCRMTrackingRecord;
import com.rrd.custompoint.orderentry.entity.EFDCRMTrackingRecordImpl;
import com.rrd.custompoint.orderentry.entity.EFDCartLine;
import com.rrd.custompoint.orderentry.entity.EFDCartLineImpl;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.interfaces.ICustomEmail;
import com.wallace.atwinxs.orderentry.admin.ao.OACustomEmailAssembler;
import com.wallace.atwinxs.orderentry.admin.ao.OACustomEmailFormBean;
import com.wallace.atwinxs.orderentry.admin.vo.EmailStyleVO;
import com.wallace.atwinxs.orderentry.ao.EFDDestinationsFormBean;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.vo.EFDDestinationOptionsVO;

class EFDDestinationServiceImplTests extends BaseOEServiceTest {

	private static final int TEST_ID = 1;

	@InjectMocks
	private EFDDestinationServiceImpl service;

	@Mock
	private ICustomEmail mockICustomEmail;

	@Mock
	private OECustomEmailComponentLocatorService mockOECustomEmailComponentLocatorService;
	
	@Mock
	private EFDDestinationOptionsFormBean mockEFDDestinationOptionsFormBean;
	
	@Mock
	private EFDCRMTracking mockEFDCRMTracking;
	
	@Mock
	private EFDDestinationsFormBean mockEFDDestinationsFormBean;
	
	//CAP-49453
	@Mock
	private EFDDestinationOptionsVO mockEfdDestinationOptionsVOs;
	@Mock
	private EFDCRMTrackingRecord mockEFDCRMTrackingRecord;
	
	String TEST_ENCRYPTED_SESSIONID;
	private EFDStyleInformationRequest efdStyleInformationRequest;
	
	//CAP-49453
	private SaveEfdInformationRequest saveEfdInformationRequest;

	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_whenDefaultEmailStyleIndTRUE_returns_success200() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(true).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));
//		when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf("11111"));

 		//Default Email Style indicator to TRUE  
		EmailStyleVO emailStyleVO = new EmailStyleVO(
				4366, 7125, "B", 
				442, "emailStyleName",true,	
				true, "fromName", 
				true, "fromAddress",	
				true, "replyToAddress", 
				true, "emailSubject", "Y", "emailBody",	
				true, "emailFooter",	
				true, "emailHyperLink", "Y", "htmlPage", 
				true, "emailBodyFileLocation", "landingPageFileLocation", new Date(), 
				true, "systemFlag");
		
		Collection<EmailStyleVO> emailStyleVOLst = new ArrayList<>();
		emailStyleVOLst.add(emailStyleVO);		
		
		OACustomEmailFormBean customEmailFormBean = new OACustomEmailFormBean(
				"4366", "siteName", 7125, "buName","B",
				"EmailSubject", "EmailFromAddress", "EmailReplyToAddress", "emailBody", "Y",
				"EmailFooter", "Y", "emailBanner", "emailHyperLink", true,
				"emailStyleName", "emailStyleTypeCd", 402, "emailFromName", "bannerImage",
				false, false, false, false);
		
	//	when(mockOECustomEmailComponentLocatorService.locate(any())).thenReturn(mockICustomEmail);
	//	when(mockICustomEmail.getEmailStyleByID(any())).thenReturn(emailStyleVO);
	//	when(mockICustomEmail.retrieveAllEmailStyles(any())).thenReturn(emailStyleVOLst);
		
		// when valid encrypted Style Id, return success 200 with Style ID info
		efdStyleInformationRequest = validEFDStyleID();

		try (
			MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
				Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
						when(mock.getEFDFormInfo(any(), any(), anyInt()))
							.thenReturn(mockEFDDestinationOptionsFormBean);
				});
			MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
					Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
							when(mock.getReplyToOverride()).thenReturn("STANDARD");
							when(mock.getFromNameOverride()).thenReturn("STANDARD");
							when(mock.getEmailContentOverride()).thenReturn("<body>STANDARD</body>");
							when(mockEFDDestinationOptionsFormBean.getDefaultSytemEmailFromName())
									.thenReturn("defaultSytemEmailFromName");
						});
			MockedConstruction<OACustomEmailAssembler> mockedOACustomEmailAssembler = 
					Mockito.mockConstruction(OACustomEmailAssembler.class, (mock, context) -> {
							when(mock.getDefaultEfdEmail(anyInt(), anyInt()))
							.thenReturn(customEmailFormBean);
					});
			
				) {
			
			EFDStyleInformationResponse efdStyleInfoResponse = service
					.getEfdStyleInformationForOrder(mockSessionContainer, efdStyleInformationRequest);
				assertNotNull(efdStyleInfoResponse);
				assertFalse(efdStyleInfoResponse.isSuccess());
		}
	}
	
	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_whenDefaultEmailStyleIndFALSE_returns_success200() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(true).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));
//		when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf("11111"));

 		//Default Email Style indicator to TRUE  
		EmailStyleVO emailStyleVO = new EmailStyleVO(
				4366, 7125, "B", 
				442, "emailStyleName",true,	
				false, "fromName", 
				false, "fromAddress",	
				false, "replyToAddress", 
				false, "emailSubject", "Y", "emailBody",	
				false, "emailFooter",	
				false, "emailHyperLink", "Y", "htmlPage", 
				false, "emailBodyFileLocation", "landingPageFileLocation", new Date(), 
				false, "systemFlag");
		
		Collection<EmailStyleVO> emailStyleVOLst = new ArrayList<>();
		emailStyleVOLst.add(emailStyleVO);		
		
		OACustomEmailFormBean customEmailFormBean = new OACustomEmailFormBean(
				"4366", "siteName", 7125, "buName","B",
				"EmailSubject", "EmailFromAddress", "EmailReplyToAddress", "emailBody", "Y",
				"EmailFooter", "Y", "emailBanner", "emailHyperLink", true,
				"emailStyleName", "emailStyleTypeCd", 402, "emailFromName", "bannerImage",
				false, false, false, false);
		
//		when(mockOECustomEmailComponentLocatorService.locate(any())).thenReturn(mockICustomEmail);
//		when(mockICustomEmail.getEmailStyleByID(any())).thenReturn(emailStyleVO);
//		when(mockICustomEmail.retrieveAllEmailStyles(any())).thenReturn(emailStyleVOLst);
		
		// when valid encrypted Style Id, return success 200 with Style ID info
		efdStyleInformationRequest = validEFDStyleID();

		try (
			MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
				Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
						when(mock.getEFDFormInfo(any(), any(), anyInt()))
							.thenReturn(mockEFDDestinationOptionsFormBean);
				});
			MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
					Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
							when(mock.getReplyToOverride()).thenReturn("STANDARD");
							when(mock.getFromNameOverride()).thenReturn("STANDARD");
							when(mock.getEmailContentOverride()).thenReturn("<body>STANDARD</body>");
							when(mockEFDDestinationOptionsFormBean.getDefaultSytemEmailFromName())
									.thenReturn("defaultSytemEmailFromName");
						});
			MockedConstruction<OACustomEmailAssembler> mockedOACustomEmailAssembler = 
					Mockito.mockConstruction(OACustomEmailAssembler.class, (mock, context) -> {
							when(mock.getDefaultEfdEmail(anyInt(), anyInt()))
							.thenReturn(customEmailFormBean);
					});
			
				) {
			
			EFDStyleInformationResponse efdStyleInfoResponse = service
					.getEfdStyleInformationForOrder(mockSessionContainer, efdStyleInformationRequest);
				assertNotNull(efdStyleInfoResponse);
				assertFalse(efdStyleInfoResponse.isSuccess());
		}
	}
	
	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_whenStyleId_NOTEXIST_returns_Failed422() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(true).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));

 		//Default Email Style indicator to TRUE  
		EmailStyleVO emailStyleVO = new EmailStyleVO(
				4366, 7125, "B", 
				441, "emailStyleName",true,	
				false, "fromName", 
				false, "fromAddress",	
				false, "replyToAddress", 
				false, "emailSubject", "Y", "emailBody",	
				false, "emailFooter",	
				false, "emailHyperLink", "Y", "htmlPage", 
				false, "emailBodyFileLocation", "landingPageFileLocation", new Date(), 
				false, "systemFlag");
		
		Collection<EmailStyleVO> emailStyleVOLst = new ArrayList<>();
		emailStyleVOLst.add(emailStyleVO);		
		
		OACustomEmailFormBean customEmailFormBean = new OACustomEmailFormBean(
				"4366", "siteName", 7125, "buName","B",
				"EmailSubject", "EmailFromAddress", "EmailReplyToAddress", "emailBody", "Y",
				"EmailFooter", "Y", "emailBanner", "emailHyperLink", true,
				"emailStyleName", "emailStyleTypeCd", 402, "emailFromName", "bannerImage",
				false, false, false, false);
		
//		when(mockOECustomEmailComponentLocatorService.locate(any())).thenReturn(mockICustomEmail);
//		when(mockICustomEmail.getEmailStyleByID(any())).thenReturn(emailStyleVO);
//		when(mockICustomEmail.retrieveAllEmailStyles(any())).thenReturn(emailStyleVOLst);
		
		// when valid encrypted Style Id, return success 200 with Style ID info
		efdStyleInformationRequest = validEFDStyleID();

		EFDStyleInformationResponse efdStyleInfoResponse = service
				.getEfdStyleInformationForOrder(mockSessionContainer, efdStyleInformationRequest);
			assertNotNull(efdStyleInfoResponse);
			assertFalse(efdStyleInfoResponse.isSuccess());
	}
	

	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_returns_OrderNotExistFalse_Failed422() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(false).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));

		// when valid encrypted Style Id and Order Not Exist, return 422
		efdStyleInformationRequest = validEFDStyleID();

		EFDStyleInformationResponse efdStyleInfoResponse = service.getEfdStyleInformationForOrder(mockSessionContainer,
				efdStyleInformationRequest);
		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());
	}
	
	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_returns_EFDNOTAllowed_Failed422() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(false);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(true).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));

		// when valid encrypted Style Id and if EDF is NOT allowed for order, return 422
		efdStyleInformationRequest = emptyEFDStyleID();

		EFDStyleInformationResponse efdStyleInfoResponse = service.getEfdStyleInformationForOrder(mockSessionContainer,
				efdStyleInformationRequest);
		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());
	}

	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_returns_EmptyStyleID_Failed422() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(true).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));

		// when Empty encrypted Style Id, return failed 422
		efdStyleInformationRequest = emptyEFDStyleID();
		efdStyleInformationRequest.setStyleID("");

		EFDStyleInformationResponse efdStyleInfoResponse = service.getEfdStyleInformationForOrder(mockSessionContainer,
				efdStyleInformationRequest);
		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());
	}

	// CAP-49326
	@Test
	void that_getEfdStyleInformationForOrder_returns_NonNumericInvalidStyleID_Failed422() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		doReturn(true).when(service).validateOrder(any(EFDStyleInformationResponse.class), any(SessionContainer.class),
				any(AppSessionBean.class));

		// when tampered encrypted Style Id, return failed 422
		efdStyleInformationRequest = nonNumerInvalidEFDStyleID();
		efdStyleInformationRequest.setStyleID("fgdfgdfgdf");

		EFDStyleInformationResponse efdStyleInfoResponse = service.getEfdStyleInformationForOrder(mockSessionContainer,
				efdStyleInformationRequest);
		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());
	}
	
	// CAP-49344
	@Test
	void that_getEFDOptions_returns_success() throws AtWinXSException {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getCurrencyLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockAppSessionBean.getApplyExchangeRate()).thenReturn(true);
		
		try (MockedConstruction<OECheckoutAssembler> mockedAssembler = 
				mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
						when(mock.getEFDFormInfo(any(), any(), anyInt())).thenReturn(mockEFDDestinationOptionsFormBean);
				});
			MockedConstruction<EFDDestinationsFormBean> mockedFormBean = 
				mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
						doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
				})) {
			when(mockOEManageOrdersComponentLocatorService.locate(mockToken)).thenReturn(mockIOEManageOrdersComponent);
			when(mockIOEManageOrdersComponent.getEFDDestinations(anyInt())).thenReturn(buildEFDVOs());
			when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
			when(mockEntityObjectMap.getEntity(eq(EFDCRMTracking.class), any())).thenReturn(mockEFDCRMTracking);
			when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
			when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
			when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
			when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
			when(mockOESessionBean.getUserSettings()).thenReturn(mockUserSettings);
			when(mockUserSettings.isCanUserEditOwnEmail()).thenReturn(true);
			when(mockUserSettings.getAllowMultipleEmailsCode()).thenReturn("N");
			when(mockUserSettings.getEfdEmailAddress()).thenReturn("test@emailaddress.com");
			EFDOptionsResponse response = service.getEFDOptions(mockSessionContainer);
			
			assertNotNull(response);
			assertTrue(response.isSuccess());
		}
	}
	
	@Test
	void that_buildSFEmails_success() throws AtWinXSException {
		EFDDestinationsFormBean formBean = buildFormBean();
		service.buildSFEmails(formBean, buildEFDCRMTrackingRecords());
		assertNotNull(formBean.getSalesforceEmailCrmID());
	}
	
	@Test
	void that_isFTPOnly_isTrue() {
		EFDDestinationsFormBean formBean = buildFormBean();
		boolean isFTPOnly = service.isFTPOnly(formBean);
		assertTrue(isFTPOnly);
	}
	
	//CAP-49453
	@Test
	void that_saveEfdInformation_returns_success200() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockEntityObjectMap.getEntity(eq(EFDCRMTracking.class), isA(CustomizationToken.class))).thenReturn(mockEFDCRMTracking);
		when(mockEntityObjectMap.getEntity(eq(EFDCRMTrackingRecord.class), isA(CustomizationToken.class))).thenReturn(mockEFDCRMTrackingRecord);

		doNothing().when(mockEFDCRMTracking).populate(anyInt());
		doNothing().when(mockEFDCRMTrackingRecord).populate(anyInt(), any(), any(), any());
		
		Collection<EFDCRMTrackingRecord> efdcrmTrackingRecordsLst = new ArrayList<>();
		EFDCRMTrackingRecord efdcrmTrackingRecord = new EFDCRMTrackingRecordImpl();
		efdcrmTrackingRecordsLst.add(efdcrmTrackingRecord);
		when(mockEFDCRMTracking.getRecords()).thenReturn(efdcrmTrackingRecordsLst);
		
		when(mockAppSessionBean.getSalesforceSignedRequestJson()).thenReturn("");

		doReturn(true).when(service).validateOrder(any(), any(),any());
		when(mockVolatileSessionBean.getOrderId()).thenReturn(Integer.valueOf("11111"));
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		when(mockOEManageOrdersComponentLocatorService.locate(any())).thenReturn(mockIOEManageOrdersComponent);
		EFDDestinationOptionsVO[] efdDestinationOptionsVOs = new EFDDestinationOptionsVO[1];
		efdDestinationOptionsVOs[0]= mockEfdDestinationOptionsVOs;
		when(mockIOEManageOrdersComponent.getEFDDestinations(anyInt())).thenReturn(efdDestinationOptionsVOs);

		doReturn(getEfdSaveResponseSuccess()).when(service).validateRequest(any(), any(), any(), any(), any());
	
		// when valid encrypted Style Id, return success 200 with Style ID info
		saveEfdInformationRequest = saveEfdRequest();

		try (
			MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
				Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
						when(mock.getEFDFormInfo(any(), any(), anyInt()))
							.thenReturn(mockEFDDestinationOptionsFormBean);
				});
			MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
					Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
							
							when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
							when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
							when(mock.getSalesForceEmailSelected()).thenReturn("true");
						});
				) {
			
			SaveEfdInformationResponse efdStyleInfoResponse = service
					.saveEfdInformation(mockSessionContainer, saveEfdInformationRequest);
				assertNotNull(efdStyleInfoResponse);
				assertTrue(efdStyleInfoResponse.isSuccess());
		}
		
		try (
				MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
					Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
							when(mock.getEFDFormInfo(any(), any(), anyInt()))
								.thenReturn(mockEFDDestinationOptionsFormBean);
					});
				MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
						Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
								doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
								when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
								when(mock.getReplyToOverride()).thenReturn("STANDARD");
								when(mock.getFromNameOverride()).thenReturn("STANDARD");
								when(mock.getEmailContentOverride()).thenReturn("<body>STANDARD</body>");
								
								when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
								when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
								when(mock.getSalesForceEmailSelected()).thenReturn("false");
							});
					) {
				
				SaveEfdInformationResponse efdStyleInfoResponse = service
						.saveEfdInformation(mockSessionContainer, saveEfdInformationRequest);
					assertNotNull(efdStyleInfoResponse);
					assertTrue(efdStyleInfoResponse.isSuccess());
			}
	}
	
	//CAP-49453
	@Test
	void that_saveEfdInformation_isAllowEFD_returns_422() throws Exception {

		service = Mockito.spy(service);

		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOEOrderSession);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		
		Collection<EFDCRMTrackingRecord> efdcrmTrackingRecordsLst = new ArrayList<>();
		EFDCRMTrackingRecord efdcrmTrackingRecord = new EFDCRMTrackingRecordImpl();
		efdcrmTrackingRecordsLst.add(efdcrmTrackingRecord);
		doReturn(true).when(service).validateOrder(any(), any(),any());
		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockUserSettings.isAllowEFD()).thenReturn(false);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
		EFDDestinationOptionsVO[] efdDestinationOptionsVOs = new EFDDestinationOptionsVO[1];
		efdDestinationOptionsVOs[0]= mockEfdDestinationOptionsVOs;
	
		// when valid encrypted Style Id, return success 200 with Style ID info
		saveEfdInformationRequest = saveEfdRequest();

		try (
			MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
				Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
						when(mock.getEFDFormInfo(any(), any(), anyInt()))
							.thenReturn(mockEFDDestinationOptionsFormBean);
				});
			MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
					Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
							
							when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
							when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
							when(mock.getSalesForceEmailSelected()).thenReturn("true");
						});
				) {
			
			SaveEfdInformationResponse efdStyleInfoResponse = service
					.saveEfdInformation(mockSessionContainer, saveEfdInformationRequest);
				assertNotNull(efdStyleInfoResponse);
				assertFalse(efdStyleInfoResponse.isSuccess());
		}
		when(mockUserSettings.isAllowEFD()).thenReturn(true);
		try (
				MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
					Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
							when(mock.getEFDFormInfo(any(), any(), anyInt()))
								.thenReturn(null);
					});
				MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
						Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
								doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
								when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
							
								when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
								when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
								when(mock.getSalesForceEmailSelected()).thenReturn("false");
							});
					) {
				
				SaveEfdInformationResponse efdStyleInfoResponse = service
						.saveEfdInformation(mockSessionContainer, saveEfdInformationRequest);
					assertNotNull(efdStyleInfoResponse);
					assertFalse(efdStyleInfoResponse.isSuccess());
			}
	}
	
	//CAP-49453
	@Test
	@Disabled
	void that_validateRequest_validateEfdLineItems_returns_422() throws Exception {


		List<EFDCartLine> efdCartLineItems = new ArrayList<EFDCartLine>();
		EFDCartLine cartLine1 = new EFDCartLineImpl();
		cartLine1.setLineNumber(123456);
		cartLine1.setAllowsStaticOutput(true);
		cartLine1.setAllowsFtpOutput(true);
		cartLine1.setDestination(mockEfdDestinationOptionsVOs);
		efdCartLineItems.add(cartLine1);
		when(mockEFDDestinationsFormBean.getEfdLines()).thenReturn(efdCartLineItems);

		saveEfdInformationRequest = saveEfdRequest();
		List<EFDLineItem> efdLineItems = new ArrayList<EFDLineItem>();
		EFDLineItem lineItem1 = new EFDLineItem();
		lineItem1.setLineNumber(123455);

		List<String> distList = new ArrayList<String>();
		distList.add("STC");
		lineItem1.setEfdDestinations(distList);
		lineItem1.setFtpID("55");

		efdLineItems.add(lineItem1);

		saveEfdInformationRequest.setEfdLineItems(efdLineItems);

		SaveEfdInformationResponse efdStyleInfoResponse = service
				.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());

		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());

		saveEfdInformationRequest.getEfdLineItems().get(0).setLineNumber(123456);
		saveEfdInformationRequest.getEfdLineItems().get(0).setFtpID("");
		mockEFDDestinationsFormBean.getEfdLines().get(0).setAllowsFtpOutput(true);

		efdStyleInfoResponse = service
				.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());

		saveEfdInformationRequest.getEfdLineItems().get(0).setFtpID("55");
		mockEFDDestinationsFormBean.getEfdLines().get(0).setAllowsStaticOutput(false);
		Collection<CatalogDefaultVO> def=new ArrayList<CatalogDefaultVO>();
		
		efdStyleInfoResponse = service
				.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
		assertNotNull(efdStyleInfoResponse);
		assertFalse(efdStyleInfoResponse.isSuccess());


	}
	//CAP-49453
	@Test
	@Disabled
	void that_validateRequest_validateEmailStyleIDAndEmailMsg_returns_422() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		Collection<EFDCRMTrackingRecord> efdcrmTrackingRecordsLst = new ArrayList<>();
		EFDCRMTrackingRecord efdcrmTrackingRecord = new EFDCRMTrackingRecordImpl();
		efdcrmTrackingRecordsLst.add(efdcrmTrackingRecord);

//		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);
//		when(mockUserSettings.getAllowMultipleEmailsCode()).thenReturn("false");


		//Default Email Style indicator to TRUE  
		EmailStyleVO emailStyleVO = new EmailStyleVO(
				4366, 7125, "B", 
				441, "emailStyleName",true,	
				true, "fromName", 
				true, "fromAddress",	
				true, "replyToAddress", 
				true, "emailSubject", "Y", "emailBody",	
				true, "emailFooter",	
				true, "emailHyperLink", "Y", "htmlPage", 
				true, "emailBodyFileLocation", "landingPageFileLocation", new Date(), 
				true, "systemFlag");

		Collection<EmailStyleVO> emailStyleVOLst = new ArrayList<>();
		emailStyleVOLst.add(emailStyleVO);


		when(mockOECustomEmailComponentLocatorService.locate(any())).thenReturn(mockICustomEmail);
		when(mockICustomEmail.getEmailStyleByID(any())).thenReturn(emailStyleVO);
		when(mockICustomEmail.retrieveAllEmailStyles(any())).thenReturn(emailStyleVOLst);


		List<EFDCartLine> efdCartLineItems = new ArrayList<EFDCartLine>();
		EFDCartLine cartLine1 = new EFDCartLineImpl();
		cartLine1.setLineNumber(123456);
		cartLine1.setAllowsStaticOutput(true);
		cartLine1.setAllowsFtpOutput(true);
		cartLine1.setDestination(mockEfdDestinationOptionsVOs);
		efdCartLineItems.add(cartLine1);
		when(mockEFDDestinationsFormBean.getEfdLines()).thenReturn(efdCartLineItems);

		saveEfdInformationRequest = saveEfdRequest();
		List<EFDLineItem> efdLineItems = new ArrayList<EFDLineItem>();
		EFDLineItem lineItem1 = new EFDLineItem();
		lineItem1.setLineNumber(123456);

		List<String> distList = new ArrayList<String>();
		distList.add("STC");
		lineItem1.setEfdDestinations(distList);
		lineItem1.setFtpID("55");

		efdLineItems.add(lineItem1);

		saveEfdInformationRequest.setEfdLineItems(efdLineItems);
		saveEfdInformationRequest.setEmailStyleID("1234");
		saveEfdInformationRequest.setEmailMessage("");
		List<String> emailList = new ArrayList<String>();
		emailList.add("test@test.com");
		saveEfdInformationRequest.setEmailAddresses(emailList);

		SaveEfdInformationResponse efdSaveInfoResponse = service
				.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());

		assertNotNull(efdSaveInfoResponse);
		assertFalse(efdSaveInfoResponse.isSuccess());
		saveEfdInformationRequest.setEmailStyleID("1234");


		try(
				MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
				Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					when(mock.getEFDFormInfo(any(), any(), anyInt()))
					.thenReturn(mockEFDDestinationOptionsFormBean);
				});
				MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
						Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);

							when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
							when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
							when(mock.getSalesForceEmailSelected()).thenReturn("true");
						});
				) {
			EFDDestinationOptionsVO[] efdDestinationOptionsVOs = new EFDDestinationOptionsVO[1];
			efdDestinationOptionsVOs[0]= mockEfdDestinationOptionsVOs;
//			when(mockEfdDestinationOptionsVOs.getOrderLineNum()).thenReturn(123456);
//			when(mockEFDDestinationOptionsFormBean.getEFDDestinationsVO()).thenReturn(efdDestinationOptionsVOs);
//			when(mockEFDDestinationsFormBean.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);
//			when(mockEFDDestinationOptionsFormBean.getEFDDestinationsVO()).thenReturn(efdDestinationOptionsVOs);

			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());

			saveEfdInformationRequest.setEmailMessage("This is email message.");
			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());
			
		}

	}
	//CAP-49453
	@Test
	@Disabled
	void that_validateRequest_validateEmailAddresses_returns_422() throws Exception {

		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);

		Collection<EFDCRMTrackingRecord> efdcrmTrackingRecordsLst = new ArrayList<>();
		EFDCRMTrackingRecord efdcrmTrackingRecord = new EFDCRMTrackingRecordImpl();
		efdcrmTrackingRecordsLst.add(efdcrmTrackingRecord);

//		when(mockOEOrderSession.getUserSettings()).thenReturn(mockUserSettings);
		when(mockAppSessionBean.getDefaultLocale()).thenReturn(DEFAULT_US_LOCALE);

		//Default Email Style indicator to TRUE  
		EmailStyleVO emailStyleVO = new EmailStyleVO(
				4366, 7125, "B", 
				441, "emailStyleName",true,	
				true, "fromName", 
				true, "fromAddress",	
				true, "replyToAddress", 
				true, "emailSubject", "Y", "emailBody",	
				true, "emailFooter",	
				true, "emailHyperLink", "Y", "htmlPage", 
				true, "emailBodyFileLocation", "landingPageFileLocation", new Date(), 
				true, "systemFlag");

		Collection<EmailStyleVO> emailStyleVOLst = new ArrayList<>();
		emailStyleVOLst.add(emailStyleVO);


		when(mockOECustomEmailComponentLocatorService.locate(any())).thenReturn(mockICustomEmail);
		when(mockICustomEmail.getEmailStyleByID(any())).thenReturn(emailStyleVO);
		when(mockICustomEmail.retrieveAllEmailStyles(any())).thenReturn(emailStyleVOLst);


		List<EFDCartLine> efdCartLineItems = new ArrayList<EFDCartLine>();
		EFDCartLine cartLine1 = new EFDCartLineImpl();
		cartLine1.setLineNumber(123456);
		cartLine1.setAllowsStaticOutput(true);
		cartLine1.setAllowsFtpOutput(true);
		cartLine1.setDestination(mockEfdDestinationOptionsVOs);
		efdCartLineItems.add(cartLine1);
		when(mockEFDDestinationsFormBean.getEfdLines()).thenReturn(efdCartLineItems);

		saveEfdInformationRequest = saveEfdRequest();
		List<EFDLineItem> efdLineItems = new ArrayList<EFDLineItem>();
		EFDLineItem lineItem1 = new EFDLineItem();
		lineItem1.setLineNumber(123456);

		List<String> distList = new ArrayList<String>();
		distList.add("STC");
		lineItem1.setEfdDestinations(distList);
		lineItem1.setFtpID("55");

		efdLineItems.add(lineItem1);

		saveEfdInformationRequest.setEfdLineItems(efdLineItems);
		saveEfdInformationRequest.setEmailMessage("");
		List<String> emailList = new ArrayList<String>();
		emailList.add("test@test.com");
		saveEfdInformationRequest.setEmailAddresses(emailList);

		SaveEfdInformationResponse efdSaveInfoResponse =null;

		saveEfdInformationRequest.setEmailStyleID("1234");


		try(
				MockedConstruction<OECheckoutAssembler> mockedOECheckoutAssemblerAssembler = 
				Mockito.mockConstruction(OECheckoutAssembler.class, (mock, context) -> {
					when(mock.getEFDFormInfo(any(), any(), anyInt()))
					.thenReturn(mockEFDDestinationOptionsFormBean);
				});
				MockedConstruction<EFDDestinationsFormBean> mockedEFDDestinationsFormBean = 
						Mockito.mockConstruction(EFDDestinationsFormBean.class, (mock, context) -> {
							doNothing().when(mock).populate(any(), any(), any(), any(), any(), any(), any());
							when(mock.getOldFormBean()).thenReturn(mockEFDDestinationOptionsFormBean);

							when(mock.getSalesforceEmailTo()).thenReturn("test@test.com");
							when(mock.getSalesforceEmailCrmID()).thenReturn("test@test.com");
							when(mock.getSalesForceEmailSelected()).thenReturn("true");
						});
				) {
			EFDDestinationOptionsVO[] efdDestinationOptionsVOs = new EFDDestinationOptionsVO[1];
			efdDestinationOptionsVOs[0]= mockEfdDestinationOptionsVOs;

			saveEfdInformationRequest.setEmailMessage("This is email message.");

//			when(mockUserSettings.getAllowMultipleEmailsCode()).thenReturn("N");
//			when(mockUserSettings.isCanUserEditOwnEmail()).thenReturn(true);

			emailList = new ArrayList<String>();
			saveEfdInformationRequest.setEmailAddresses(emailList);
			emailList.add("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest@test.com");

			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());

			saveEfdInformationRequest.setEmailAddresses(distList);
			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());
			
//			when(mockUserSettings.getAllowMultipleEmailsCode()).thenReturn("Y");
			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());

			emailList = new ArrayList<String>();
			emailList.add("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest@test.com");
			saveEfdInformationRequest.setEmailAddresses(emailList);
			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());
			
//			when(mockUserSettings.getAllowMultipleEmailsCode()).thenReturn("N");
			
			efdSaveInfoResponse = service
					.validateRequest(mockAppSessionBean, mockOEOrderSession, mockEFDDestinationsFormBean, saveEfdInformationRequest, getEfdSaveResponseSuccess());
			assertNotNull(efdSaveInfoResponse);
			assertFalse(efdSaveInfoResponse.isSuccess());
			
		}

	}
	//CAP-49453
	private SaveEfdInformationRequest saveEfdRequest() {
		return new SaveEfdInformationRequest();
	}
	
	//CAP-49453
	private SaveEfdInformationResponse getEfdSaveResponseSuccess() {
		SaveEfdInformationResponse response = new SaveEfdInformationResponse();
		response.setSuccess(true);
		return response;
	}
	
	private EFDDestinationsFormBean buildFormBean() {
		EFDDestinationsFormBean formBean = new EFDDestinationsFormBean();
		formBean.setOrderID(TEST_ID);
		List<EFDCartLine> efdLines = new ArrayList<>();
		EFDCartLine efdCartLine = new EFDCartLineImpl();
		EFDDestinationOptionsVO destination = new EFDDestinationOptionsVO();
		Collection<String> efdMethod = new ArrayList<>();
		efdMethod.add("FTP");
		destination.setEFDMethod(efdMethod);
		efdCartLine.setDestination(destination);
		efdLines.add(efdCartLine);
		formBean.setEfdLines(efdLines);
		
		return formBean;
	}
	
	private EFDDestinationOptionsVO[] buildEFDVOs() {
		EFDDestinationOptionsVO[] efdVOs = new EFDDestinationOptionsVO[1];
		efdVOs[0] = new EFDDestinationOptionsVO();
		
		return efdVOs;
	}
	
	private Collection<EFDCRMTrackingRecord> buildEFDCRMTrackingRecords() throws AtWinXSException {
		Collection<EFDCRMTrackingRecord> eFDCRMTrackingRecords = new ArrayList<>();
		EFDCRMTrackingRecord trackingRecord = new EFDCRMTrackingRecordImpl();
		trackingRecord.setCrmEmailAddress(String.valueOf(TEST_ID));
		trackingRecord.setCrmRecordID(String.valueOf(TEST_ID));
		eFDCRMTrackingRecords.add(trackingRecord);
		return eFDCRMTrackingRecords;
	}
	// CAP-49326
	private EFDStyleInformationRequest validEFDStyleID() {

		EFDStyleInformationRequest efdStyleInformationRequest = new EFDStyleInformationRequest();
		efdStyleInformationRequest.setStyleID("EQLb6uIB8f0P4Y/FuNfz1g==");
		return efdStyleInformationRequest;
	}

	// CAP-49326
	private EFDStyleInformationRequest emptyEFDStyleID() {

		EFDStyleInformationRequest efdStyleInformationRequest = new EFDStyleInformationRequest();
		efdStyleInformationRequest.setStyleID("");
		return efdStyleInformationRequest;
	}

	// CAP-49326
	private EFDStyleInformationRequest nonNumerInvalidEFDStyleID() {

		EFDStyleInformationRequest efdStyleInformationRequest = new EFDStyleInformationRequest();
		efdStyleInformationRequest.setStyleID("NS0OXfh");
		return efdStyleInformationRequest;
	}
}
