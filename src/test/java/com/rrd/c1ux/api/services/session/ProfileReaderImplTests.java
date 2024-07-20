/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  05/31/23    C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.services.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.admin.vo.BusinessUnitVOKey;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;

class ProfileReaderImplTests extends BaseServiceTest {

    private final static int TEST_SITE_ID = 1234;

    @InjectMocks
    private ProfileReaderImpl serviceToTest;
    
    @BeforeEach
    void setUpMockTokenReader() {

        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
    }

    @Test
    void that_getProfileVO_throws_whenProfileIdIsTooLong() {

        // given too long of a profile ID
        int lengthToTest = 129;
        StringBuilder testProfileId = new StringBuilder(lengthToTest);
        for (int i = 0; i < lengthToTest; i++) {
            testProfileId.append("1");
        }

        // should throw "charLimit"
        that_getProfileVO_throws_whenUserIsAndProfileIdIs(
            BaseServiceTest.getTestLoginVO("testUsername", 1234), testProfileId.toString(), 
            "charLimit", "Profile ID");
    }

    void that_getProfileVO_throws_whenUserIsSharedAndProfileIdIsBlank() {

        // given shared user 
        String testUsername = "testUsername";
        LoginVO sharedUser = BaseServiceTest.getTestSharedLoginVO(testUsername, 1234);

        // and blank profile ID, should throw "blankProfileForSharedUser"
        that_getProfileVO_throws_whenUserIsAndProfileIdIs(sharedUser, "", 
            "blankProfileForSharedUser", testUsername);
    }

    private void that_getProfileVO_throws_whenUserIsAndProfileIdIs(
        LoginVO testUser, String testProfileId, String expectedErrorName, String expectedMapValue
    ) {

        // when getProfileVO is called
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getProfileVO(testUser, testProfileId);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.ProfileReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals(expectedErrorName, actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains expected value
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue(expectedMapValue));
    }

    void that_getProfileVO_throws_whenProfileIsNotFound() throws AtWinXSException {

        String testUsername = "testUsername";
        LoginVO testLoginVO = BaseServiceTest.getTestLoginVO(testUsername, TEST_SITE_ID);
        int buId = testLoginVO.getBusinessUnitID();

        // given an @winXS profile component
        when(mockComponentLocator.locateProfileComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mockProfileComponent);

        // given profile component returns null profile VO
        when(mockProfileComponent.getProfileByLoginID(TEST_SITE_ID, testUsername, buId)).thenReturn(null);

        // when getProfileVO is called
        AtWinXSMsgException actualEx = null;
        String testProfileId = "";
        try {

            serviceToTest.getProfileVO(testLoginVO, testProfileId);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.ProfileReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals("invalidFieldValue", actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains "Profile ID"
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue("Profile ID"));

        // and the error map contains the profile ID
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue(testProfileId));
    }

    
    void that_getProfileVO_throws_whenProfileIsNotFoundForSharedLogin() throws AtWinXSException {

        String testUsername = "testUsername";
        LoginVO testLoginVO = BaseServiceTest.getTestSharedLoginVO(testUsername, TEST_SITE_ID);

        // given an @winXS profile component
        when(mockComponentLocator.locateProfileComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mockProfileComponent);

        // given profile component returns null profile VO
        String testProfileId = "12345";
        when(mockProfileComponent.getProfileByProfileID(any(BusinessUnitVOKey.class), eq(testProfileId))).thenReturn(null);

        // when getProfileVO is called
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getProfileVO(testLoginVO, testProfileId);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.ProfileReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals("invalidFieldValue", actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains "Profile ID"
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue("Profile ID"));

        // and the error map contains the profile ID
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue(testProfileId));
    }

    @Test
    void that_getProfileVO_returnsExpected_forNonSharedLogin() throws AtWinXSException {

        String testUsername = "testUsername";
        LoginVO testLoginVO = BaseServiceTest.getTestLoginVO(testUsername, TEST_SITE_ID);
        int buId = testLoginVO.getBusinessUnitID();

        // given an @winXS profile component
        when(mockComponentLocator.locateProfileComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mockProfileComponent);

        // given profile component returns null profile VO
        int testProfileId = 12345;
        String sTestProfileId = String.valueOf(testProfileId);
        ProfileVO expected = BaseServiceTest.getTestProfileVO(TEST_SITE_ID, testProfileId, sTestProfileId);
        when(mockProfileComponent.getProfileByLoginID(TEST_SITE_ID, testUsername, buId)).thenReturn(expected);

        // when getProfileVO is called
        ProfileVO actual = serviceToTest.getProfileVO(testLoginVO, sTestProfileId);

        // then expected profile VO is returned
        assertSame(expected, actual);
    }

    @Test
    void that_getProfileVO_returnsExpected_forSharedLogin() throws AtWinXSException {

        String testUsername = "testUsername";
        LoginVO testLoginVO = BaseServiceTest.getTestSharedLoginVO(testUsername, 1234);

        // given an @winXS profile component
        when(mockComponentLocator.locateProfileComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mockProfileComponent);

        // given profile component returns null profile VO
        int testProfileId = 12345;
        String sTestProfileId = String.valueOf(testProfileId);
        ProfileVO expected = BaseServiceTest.getTestProfileVO(TEST_SITE_ID, testProfileId, sTestProfileId);
        when(mockProfileComponent.getProfileByProfileID(any(BusinessUnitVOKey.class), eq(sTestProfileId)))
            .thenReturn(expected);

        // when getProfileVO is called
        ProfileVO actual = serviceToTest.getProfileVO(testLoginVO, sTestProfileId);

        // then expected profile VO is returned
        assertSame(expected, actual);
    }
}
