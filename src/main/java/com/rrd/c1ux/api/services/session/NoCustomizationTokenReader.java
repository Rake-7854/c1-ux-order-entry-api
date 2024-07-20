package com.rrd.c1ux.api.services.session;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

import org.springframework.stereotype.Service;

@Service
public class NoCustomizationTokenReader implements TokenReader {

    @Override
    public CustomizationToken getToken() {

        return AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN;
    }
    
}
