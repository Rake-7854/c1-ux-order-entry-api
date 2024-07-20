/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 *  Date        Modified By     DTS#                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  03/07/23   C Porter        CAP-38715                   refactor
 *  05/31/23   C Porter        CAP-40530                   JUnit cleanup  
 */
package com.rrd.c1ux.api.services.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;

class LoginReaderImplTests extends BaseServiceTest {

    private final static int TEST_SITE_ID = 1234;

    @InjectMocks
    private LoginReaderImpl serviceToTest;

    @BeforeEach
    void setUpMockTokenReader() {

        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
    }

    @Test
    void that_getLoginVO_throws_whenUsernameIsNull() {

        that_getLoginVO_throws_whenUsernameIs(null, "noBlanks");
    }

    @Test
    void that_getLoginVO_throws_whenUsernameIsBlank() {

        that_getLoginVO_throws_whenUsernameIs("", "noBlanks");
    }

    @Test
    void that_getLoginVO_throws_whenUsernameIsTooLong() {

        that_getLoginVO_throws_whenUsernameIs("ThisUsernameIsTooLong", "charLimit");
    }

    private void that_getLoginVO_throws_whenUsernameIs(String testUsername, String expectedMessage) {

        // when getLoginVO is called with blank username
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getLoginVO(TEST_SITE_ID, testUsername);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.LoginReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals(expectedMessage, actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains "User Name"
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue("User Name"));
    }

    @Test
    void that_getLoginVO_throws_whenUsernameIsNotFound() throws AtWinXSException {

        // given an @winXS login component
        when(mockComponentLocator.locateLoginComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mockLoginComponent);

        // that returns a null login VO 
        when(mockLoginComponent.getLogin(any())).thenReturn(null);

        // when getLoginVO is called
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getLoginVO(TEST_SITE_ID, "testusername");
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.LoginReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals("invalidFieldValue", actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains "User Name"
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue("User Name"));
    }

    @Test
    void that_getLoginVO_returnsExpected() throws AtWinXSException {

        // given an @winXS login component
        when(mockComponentLocator.locateLoginComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mockLoginComponent);

        // that returns a login VO 
        String testUsername = "testusername";
        LoginVO expected = BaseServiceTest.getTestLoginVO(testUsername, TEST_SITE_ID);
        when(mockLoginComponent.getLogin(any())).thenReturn(expected);

        // when getLoginVO is called
        LoginVO actual = serviceToTest.getLoginVO(TEST_SITE_ID, testUsername);

        // then expected login VO is returned
        assertSame(expected, actual);
    }
}
