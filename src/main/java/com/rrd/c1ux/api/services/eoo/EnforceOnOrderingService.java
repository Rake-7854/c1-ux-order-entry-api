/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/01/24	Satishkumar A		CAP-46675				C1UX BE - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 *  02/20/24	T Harmon			CAP-46543				Added code for EOO
 */
package com.rrd.c1ux.api.services.eoo;

import javax.servlet.http.HttpServletRequest;

import com.rrd.c1ux.api.models.eoo.ValidateCheckoutResponse;
import com.rrd.c1ux.api.models.shoppingcart.SaveSelectedAttributesRequest;
import com.rrd.c1ux.api.models.shoppingcart.SaveSelectedAttributesResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface EnforceOnOrderingService {
	
	public ValidateCheckoutResponse validateCheckout(SessionContainer sc, HttpServletRequest request) throws AtWinXSException;
	
	// CAP-46543 TH
	public SaveSelectedAttributesResponse saveEooAttributes(SessionContainer sc, SaveSelectedAttributesRequest request) throws AtWinXSException;

}
