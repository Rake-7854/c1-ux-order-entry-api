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

import java.io.Serializable;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2RedirectAuthenticationRequest;

public class C1UXSaml2AuthenticationRequest implements Serializable {

  private static final long serialVersionUID = -3419909593935693301L;

  private final String samlRequest;

  private final String relayState;

  private final String authenticationRequestUri;

  private final String sigAlg;

  private final String signature;

  private final String bindingUrn;

  C1UXSaml2AuthenticationRequest(String samlRequest, String relayState, String authenticationRequestUri, String bindingUrn, String sigAlg,
      String signature) {
    this.samlRequest = samlRequest;
    this.relayState = relayState;
    this.authenticationRequestUri = authenticationRequestUri;
    this.sigAlg = sigAlg;
    this.signature = signature;
    this.bindingUrn = bindingUrn;
  }

  public C1UXSaml2AuthenticationRequest(AbstractSaml2AuthenticationRequest request) {
    this.samlRequest = request.getSamlRequest();
    this.relayState = request.getRelayState();
    this.authenticationRequestUri = request.getAuthenticationRequestUri();
    this.bindingUrn = request.getBinding().getUrn();

    if (request instanceof Saml2RedirectAuthenticationRequest) {
      Saml2RedirectAuthenticationRequest redirectRequest = (Saml2RedirectAuthenticationRequest) request;
      this.sigAlg = redirectRequest.getSigAlg();
      this.signature = redirectRequest.getSignature();
    } else {
      this.sigAlg = null;
      this.signature = null;
    }
  }

  public String getBindingUrn() {
    return bindingUrn;
  }

  public String getSamlRequest() {
    return samlRequest;
  }

  public String getRelayState() {
    return relayState;
  }

  public String getAuthenticationRequestUri() {
    return authenticationRequestUri;
  }

  public String getSigAlg() {
    return sigAlg;
  }

  public String getSignature() {
    return signature;
  }

}
