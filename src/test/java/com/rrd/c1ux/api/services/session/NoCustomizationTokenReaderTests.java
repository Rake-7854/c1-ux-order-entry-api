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

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import com.rrd.c1ux.api.BaseServiceTest;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

class NoCustomizationTokenReaderTests extends BaseServiceTest {
    
    @Test
    void that_getToken_returnsNotCustomizationToken() {

        // given service to test
        TokenReader serviceToTest = new NoCustomizationTokenReader();

        // when getToken is called
        CustomizationToken actual = serviceToTest.getToken();

        // then 'no customization' token is returned
        assertSame(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN, actual);
    }
}
