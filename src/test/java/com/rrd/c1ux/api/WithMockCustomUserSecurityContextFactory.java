package com.rrd.c1ux.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory 
    implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {

            SecurityContext context = SecurityContextHolder.createEmptyContext();
    
            AuthenticatedPrincipal principal = new Saml2AuthenticatedPrincipal() {
                @Override
                public String getName() {
                    return customUser.username();
                }

                @Override
                public String getRelyingPartyRegistrationId() {
                    return "testRelyingPartyRegistrationId";
                }
            };

            final List<GrantedAuthority> grantedAuths = new ArrayList<>();
            Authentication auth = new Saml2Authentication(principal, "saml2Response", grantedAuths);
            context.setAuthentication(auth);
            return context;
        }
}
