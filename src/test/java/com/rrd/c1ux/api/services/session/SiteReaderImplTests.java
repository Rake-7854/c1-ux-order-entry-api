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
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;

class SiteReaderImplTests extends BaseServiceTest {

    private final static int TEST_SITE_ID = 1234;
    private final static String TEST_ACCOUNT = "tstAccount";

    @InjectMocks
    private SiteReaderImpl serviceToTest;
    
    @BeforeEach
    void setUpMockTokenReader() {

        when(mockTokenReader.getToken()).thenReturn(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
    }

    @Test
    void that_getSiteForAccount_throws_whenAccountIsNull() {

        that_getSiteForAccount_throws_whenAccountIs(null, "noBlanks");
    }

    @Test
    void that_getSiteForAccount_throws_whenAccountIsBlank() {

        that_getSiteForAccount_throws_whenAccountIs("", "noBlanks");
    }

    @Test
    void that_getSiteForAccount_throws_whenAccountIsTooLong() {

        that_getSiteForAccount_throws_whenAccountIs("AccountTooLong", "charLimit");
    }

    private void that_getSiteForAccount_throws_whenAccountIs(
        String testAccount, String expectedMessage
    ) {

        // when getSiteForAccount is called with blank account
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getSiteForAccount(testAccount);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.SiteReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals(expectedMessage, actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains "Account"
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue("Account"));
    }

    @Test
    void that_getSiteForAccount_throws_whenAccountIsNotFound() throws AtWinXSException {

        // given an @winXS login component
        when(mockComponentLocator.locateSiteComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mocSiteComponent);

        // that returns a null login VO 
        when(mocSiteComponent.getSiteVOByAccount(any())).thenReturn(null);

        // when getSiteForAccount is called
        AtWinXSMsgException actualEx = null;
        try {

            serviceToTest.getSiteForAccount(TEST_ACCOUNT);
        }
        catch (AtWinXSException ex) {

            actualEx = (AtWinXSMsgException)ex;
        }

        // then exception is thrown
        assertNotNull(actualEx, "Expected an exception");

        // and the error class name is expected
        assertEquals("com.rrd.c1ux.api.services.session.SiteReaderImpl", actualEx.getClassName());

        // and the error name is expected
        assertEquals("invalidFieldValue", actualEx.getMsg().getErrorCode().getErrorName());

        // and the error map contains "Account"
        assertTrue(actualEx.getMsg().getErrorCode().getReplaceMap().containsValue("Account"));
    }

    @Test
    void that_getSiteForAccount_returnsExpected() throws AtWinXSException {

        // given an @winXS login component
        when(mockComponentLocator.locateSiteComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN))
            .thenReturn(mocSiteComponent);

        // that returns a login VO 
        SiteVO expected = BaseServiceTest.getTestSiteVO(TEST_SITE_ID, TEST_ACCOUNT);
        when(mocSiteComponent.getSiteVOByAccount(any())).thenReturn(expected);

        // when getSiteForAccount is called
        SiteVO actual = serviceToTest.getSiteForAccount(TEST_ACCOUNT);

        // then expected login VO is returned
        assertSame(expected, actual);
    }
}
