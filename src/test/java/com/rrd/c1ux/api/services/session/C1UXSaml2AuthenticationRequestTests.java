/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 *  Date        Modified By     Jira                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *  06/22/2023  C Porter        CAP-41584                   Address server 500 issue when user session times out.
 */
package com.rrd.c1ux.api.services.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2PostAuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2RedirectAuthenticationRequest;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;

@ExtendWith(MockitoExtension.class)
class C1UXSaml2AuthenticationRequestTests {

  @Mock
  Saml2RedirectAuthenticationRequest mockSaml2RedirectAuthenticationRequest;

  @Mock
  Saml2PostAuthenticationRequest mockSaml2PostAuthenticationRequest;

  private final String samlRequest = "samlRequest";
  private final String relayState = "relayState";
  private final String authenticatedRequestUri = "authenticationRequestUri";
  private final String sigAlg = "sigAlg";
  private final String signature = "signature";

  void setupCommonMocks(AbstractSaml2AuthenticationRequest mockRequest, Saml2MessageBinding binding) {
    
    when(mockRequest.getSamlRequest()).thenReturn(samlRequest);
    when(mockRequest.getRelayState()).thenReturn(relayState);
    when(mockRequest.getAuthenticationRequestUri()).thenReturn(authenticatedRequestUri);
    when(mockRequest.getBinding()).thenReturn(binding);
    
  }
  
  @Test
  void that_RedirectRequest_constructs() {

    final var redirectBinding = Saml2MessageBinding.REDIRECT;
    
    setupCommonMocks(mockSaml2RedirectAuthenticationRequest, redirectBinding);
    when(mockSaml2RedirectAuthenticationRequest.getSigAlg()).thenReturn(sigAlg);
    when(mockSaml2RedirectAuthenticationRequest.getSignature()).thenReturn(signature);
    
    var request = new C1UXSaml2AuthenticationRequest(mockSaml2RedirectAuthenticationRequest);
    
    assertSharedProperties(redirectBinding, request);
    assertEquals(sigAlg, request.getSigAlg());
    assertEquals(signature, request.getSignature());
    
  }

  @Test
  void that_PostRequest_constructs() {
    
    final var postBinding = Saml2MessageBinding.POST;
    
    setupCommonMocks(mockSaml2PostAuthenticationRequest, postBinding);
    
    var request = new C1UXSaml2AuthenticationRequest(mockSaml2PostAuthenticationRequest);
    
    assertSharedProperties(postBinding, request);
    assertNull(request.getSigAlg());
    assertNull(request.getSignature());
    
  }

  protected void assertSharedProperties(final Saml2MessageBinding binding, C1UXSaml2AuthenticationRequest request) {
    assertEquals(samlRequest, request.getSamlRequest());
    assertEquals(relayState, request.getRelayState());
    assertEquals(authenticatedRequestUri, request.getAuthenticationRequestUri());
    assertEquals(binding.getUrn(), request.getBindingUrn());
  }

}
