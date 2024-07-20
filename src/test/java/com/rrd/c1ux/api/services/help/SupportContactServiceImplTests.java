/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/23/2023	S Ramachandran	CAP-40614					added Junit for getMessageSendToAddress 
 */

package com.rrd.c1ux.api.services.help;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.wallace.atwinxs.admin.vo.SupportContactVO;
import com.wallace.atwinxs.otherservices.ao.SupportContactEmailFormBean;

class SupportContactServiceImplTests extends BaseOEServiceTest {

	public static final String SUCCESS = "Success";
	public static final String FAIL = "Failed";

	public static final String FAKE_VALID_SUPPORT_CONTACT_ID = "7125";
	public static final String FAKE_INVALID_SUPPORT_CONTACT_ID = "-1";

	public static final String FAKE_SC_USER_NAME = "Subbu";
	public static final String FAKE_SC_USER_EMAIL = "testusermail@rrd.com";
	public static final String FAKE_SC_USER_PHONE = "4343-00898";
	public static final String FAKE_SC_USER_MESSAGES_SUBJECT = "MessageSubject";
	public static final String FAKE_SC_USER_MESSAGES_TEXT = "MessageText";
	public static final String FAKE_SC_MESSAGE_SENTO = "TestMessageSennTo@rrd.com";

	public static final int FAKE_SUPPORT_CONTACT_ID = 7125;
	public static final int FAKE_SITE_ID = 4366;
	public static final int FAKE_BUID = 4366;
	public static final int FAKE_PRIORITY_CODE = 0;
	public static final String UPDATE_USER = "Amy";

	public static final String OTHER_SERVICES_BUNDLE = "otherservices";

	private SupportContactEmailFormBean supportContactEmailFormBean;

	private ArrayList<SupportContactVO> supportContactVOArray;

	private SupportContactVO supportContactVO, supportContactVO2;

	@InjectMocks
	private SupportContactServiceImpl service;

	@BeforeEach
	void setup() {

		supportContactEmailFormBean = new SupportContactEmailFormBean();
		supportContactEmailFormBean.setUserName(FAKE_SC_USER_NAME);
		supportContactEmailFormBean.setUserEmail(FAKE_SC_USER_EMAIL);
		supportContactEmailFormBean.setUserPhone(FAKE_SC_USER_PHONE);
		supportContactEmailFormBean.setMessageSubject(FAKE_SC_USER_MESSAGES_SUBJECT);
		supportContactEmailFormBean.setMessageText(FAKE_SC_USER_MESSAGES_TEXT);
		supportContactEmailFormBean.setMessageSendTo(FAKE_SC_MESSAGE_SENTO);

		supportContactVO = new SupportContactVO(FAKE_SUPPORT_CONTACT_ID, FAKE_SITE_ID, FAKE_BUID, FAKE_SC_USER_EMAIL,
				FAKE_SC_USER_NAME, FAKE_PRIORITY_CODE, UPDATE_USER, new Date());
		supportContactVO2 = new SupportContactVO(FAKE_SUPPORT_CONTACT_ID, FAKE_SITE_ID, FAKE_BUID, FAKE_SC_USER_EMAIL,
				FAKE_SC_USER_NAME, FAKE_PRIORITY_CODE, UPDATE_USER, new Date());
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNOTNULL_scVOlengthEQUALONE_scIDMatch_success() throws Exception {

		supportContactVOArray = new ArrayList<>();
		supportContactVOArray.add(0, supportContactVO);
		supportContactEmailFormBean.setSupportContacts(supportContactVOArray);

		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_VALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNOTNULL_scVOlengthEQUALONE_scIDNotMatch_success() throws Exception {

		supportContactVOArray = new ArrayList<>();
		supportContactVOArray.add(0, null);
		supportContactVOArray.add(1, supportContactVO);
		supportContactEmailFormBean.setSupportContacts(supportContactVOArray);

		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_INVALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNOTNULL_scVOlengthGTTHANONE_scIDMatch_success() throws Exception {

		supportContactVOArray = new ArrayList<>();
		supportContactVOArray.add(0, supportContactVO);
		supportContactVOArray.add(1, supportContactVO2);
		supportContactEmailFormBean.setSupportContacts(supportContactVOArray);

		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_VALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNOTNULL_scVOlengthGTTHANONE_scIDNotMatch_success() throws Exception {

		supportContactVOArray = new ArrayList<>();
		supportContactVOArray.add(0, supportContactVO);
		supportContactVOArray.add(1, supportContactVO2);
		supportContactEmailFormBean.setSupportContacts(supportContactVOArray);

		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_INVALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNOTNULL_scVOlengthEQUALZEROvalidCPSite_success() throws Exception {

		supportContactVOArray = new ArrayList<>();
		supportContactEmailFormBean.setSupportContacts(supportContactVOArray);

		when(mockAppSessionBean.isCustomPointSite()).thenReturn(true);

		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_VALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNOTNULL_scVOlengthEQUALZEROinValidCPSite_success() throws Exception {

		supportContactVOArray = new ArrayList<>();
		supportContactEmailFormBean.setSupportContacts(supportContactVOArray);

		when(mockAppSessionBean.isCustomPointSite()).thenReturn(false);

		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_VALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}

	@Test
	void that_getMessageSendToAddress_scEmailBeanisNULL_success() throws Exception {

		supportContactEmailFormBean = null;
		String sendToEmailAddress = service.getMessageSendToAddress(supportContactEmailFormBean,
				FAKE_VALID_SUPPORT_CONTACT_ID, mockAppSessionBean);
		Assertions.assertNotNull(sendToEmailAddress);
	}
}
