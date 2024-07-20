/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  10/17/23	Satishkumar A	CAP-44664	C1UX BE - Create api to retrieve edoc for Storefront
 */
package com.rrd.c1ux.api.services.edoc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.ehcache.shadow.org.terracotta.utilities.io.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.edoc.EdocUrlResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OEViewItemImageAssembler;

@WithMockUser
class EdocServiceImplTests extends BaseServiceTest{
	
	@TempDir
	Path tempDir;

	@InjectMocks
	private EdocServiceImpl serviceToTest;
	
	private MockHttpServletRequest mockRequest;
	private MockHttpServletResponse mockResponse;
	
	@BeforeEach
	public void setup() throws AtWinXSException {
		
		mockRequest = new MockHttpServletRequest();
		serviceToTest = Mockito.spy(serviceToTest);
	}
	
	@Test
	void that_getEdocUrl_success() throws Exception {
		
		mockRequest.setParameter("a", "BCgujtW1tGkD9kaH3iaN yNMJI0M 0gvPskZfxLb6UXwTQayeT/GkTzI7IcbJfxeEjQfsSNsd4f5bogme8FCP8Kf7wCM8sZk rC2OuREdhoF/re5NqUeXRjNrJ8Vr cIyxi9FgNmx8gF/re5NqUeXRtpz7SZgLytXOGAbb2AgzH4FY2AqFMWeEomV4aZp4U1CFzT4DV/CBTVQN5dG1m ph RmWYkIUn9KWI4zqg7mBBDdTm81MVlHgDg2/y1bfKDfJ/B4TqK7yt6T9otLQC6mg==");
	
		EdocUrlResponse response = new EdocUrlResponse();
		serviceToTest = Mockito.spy(serviceToTest);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);MockedConstruction<OEViewItemImageAssembler> mockedCheckout =
	    		Mockito.mockConstruction(OEViewItemImageAssembler.class, (mock, context) -> {
	    		})) {
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"")).thenReturn("");

		response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		
		mockRequest.setParameter("a","Nxjn5JCd+ubFDV5BuN3RNKh/tThbDQrRFxRxyX1tG8HJ/VEb5PJ6Ug/ON/PMJMS2OCDgMFonjO/8TeCRFAVYHBdYogpFrZNPFV6FCCBX+QIO+s7HfZavcG7JU1hJHvPK");
		response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
		assertNotNull(response);
		assertTrue(response.isSuccess());
		
		mockRequest.setParameter("a","Nxjn5JCd+ubFDV5BuN3RNKh/tThbDQrRFxRxyX1tG8HJ/VEb5PJ6Ug/ON/PMJMS2OCDgMFonjO/8TeCRFAVYHBdYogpFrZNPprCRqwjAJAie9zRPtBoABCP/2JtD17E8");
		response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
		assertNotNull(response);
		assertTrue(response.isSuccess());

		}
		
	}

	@Test
	void that_getEdocUrl_failure() throws Exception {
		
		EdocUrlResponse response = new EdocUrlResponse();
		
		doNothing().when(serviceToTest).createEdocTransaction(any(), any(), any(), anyInt(), any(), any());

		mockRequest.setParameter("a", "BCgujtW1tGkD9kaH3iaN yNMJI0M 0gvPskZfxLb6UXwTQayeT/GkTzI7IcbJfxeEjQfsSNsd4f5bogme8FCP8Kf7wCM8sZk rC2OuREdhoF/re5NqUeXRjNrJ8Vr cIyxi9FgNmx8gF/re5NqUeXRtpz7SZgLytXOGAbb2AgzH4FY2AqFMWeEomV4aZp4U1CFzT4DV/CBTVQN5dG1m ph RmWYkIUn9KWI4zqg7mBBDdTm81MVlHgDg2/y1bfKDfJ/B4TqK7yt6T9otLQC6m");
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class)) {
			mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"")).thenReturn("");
			response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
			assertNotNull(response);
			assertFalse(response.isSuccess());
			
			mockRequest.setParameter("a", "a");
			response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
			assertNotNull(response);
			assertFalse(response.isSuccess());

			mockRequest.setParameter("a","qLqi/j2mYm2ldmeYzEXh7mcbKWKDKL11BQ7wWw3jKImDjo+RS4M55ncPBB6e5YPDcR6bl6sa64dvhGJWFPEj4gWTS9h5CzxUFcEk0JVPG4ijA8SKQL6F8GjErf+FQZScIatywMlnm6ISNB+xI2x3h6g0TtNMk9wb");
			response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
			assertNotNull(response);
			assertFalse(response.isSuccess());
			
			mockRequest.setParameter("a","qLqi/j2mYm2ldmeYzEXh7kpWCw8UyQ7RHbPhbAZt6o6ldmeYzEXh7lD4ehO6p4VTfYVpYKwKQ/l0M1MgD48+ATIEjxVaNrKZ");
			response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
			assertNotNull(response);
			assertFalse(response.isSuccess());
			
			mockRequest.setParameter("a","qLqi/j2mYm2ldmeYzEXh7mcbKWKDKL11BQ7wWw3jKIk6Vv0MzB+WolD4ehO6p4VTfYVpYKwKQ/l0M1MgD48+ATIEjxVaNrKZ");
			response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
			assertNotNull(response);
			assertFalse(response.isSuccess());
			
			mockRequest.setParameter("a","qLqi/j2mYm2ldmeYzEXh7mcbKWKDKL11BQ7wWw3jKIk6Vv0MzB+WolD4ehO6p4VTFlDEPtBFwCdJ7efywWboL9BcgSXYZEIw3TlIZvnAu7PdfZIKyHlUhlow3SDOXWUGj7jJ/ZIKb+h70ObNDjntdTACqcqueZojMgSPFVo2spk=");
			response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
			assertNotNull(response);
			assertFalse(response.isSuccess());

		}
		
	}
	
	@Test
	void that_getEdocUrl_IOException() throws Exception {
		
		File testfile = Files.createFile(tempDir.resolve("testFile.txt")).toFile();
		
		mockRequest.setParameter("a", "BCgujtW1tGkD9kaH3iaN yNMJI0M 0gvPskZfxLb6UXwTQayeT/GkTzI7IcbJfxeEjQfsSNsd4f5bogme8FCP8Kf7wCM8sZk rC2OuREdhoF/re5NqUeXRjNrJ8Vr cIyxi9FgNmx8gF/re5NqUeXRtpz7SZgLytXOGAbb2AgzH4FY2AqFMWeEomV4aZp4U1CFzT4DV/CBTVQN5dG1m ph RmWYkIUn9KWI4zqg7mBBDdTm81MVlHgDg2/y1bfKDfJ/B4TqK7yt6T9otLQC6mg==");
	
		EdocUrlResponse response = new EdocUrlResponse();
		serviceToTest = Mockito.spy(serviceToTest);
		doReturn(testfile).when(serviceToTest).createFile(any());
		doThrow(new IOException()).when(serviceToTest).streamFileToBrowser(any(), any(), any());
		doNothing().when(serviceToTest).createEdocTransaction(any(), any(), any(), anyInt(), any(), any());

		response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
		assertNotNull(response);
		assertFalse(response.isSuccess());
		}
	
	
	@Test
	void that_getEdocUrl_createEdocTransaction_Exception() throws Exception {
		
		EdocUrlResponse response = new EdocUrlResponse();
		serviceToTest = Mockito.spy(serviceToTest);
		try (MockedStatic<TranslationTextTag> mockTranslationTextTag = Mockito.mockStatic(TranslationTextTag.class);MockedConstruction<OEViewItemImageAssembler> mockedCheckout =
	    		Mockito.mockConstruction(OEViewItemImageAssembler.class, (mock, context) -> {
	    			doThrow(new AtWinXSException("", "")).when(mock).addEDocTransaction(any(), any(), any(), anyInt(), any(), anyInt(), anyInt());

	    		})) {
				serviceToTest = Mockito.spy(serviceToTest);
				mockTranslationTextTag.when(() -> TranslationTextTag.processMessage(mockAppSessionBean.getDefaultLocale(), mockAppSessionBean.getCustomToken(),"")).thenReturn("");
		
				mockRequest.setParameter("a","Nxjn5JCd+ubFDV5BuN3RNKh/tThbDQrRFxRxyX1tG8HJ/VEb5PJ6Ug/ON/PMJMS2OCDgMFonjO/8TeCRFAVYHBdYogpFrZNPprCRqwjAJAie9zRPtBoABCP/2JtD17E8");
				response = serviceToTest.getEdocUrl(mockAppSessionBean, mockRequest, mockResponse);
				assertNotNull(response);
				assertTrue(response.isSuccess());
		}
	}
	
}
