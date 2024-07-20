/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  09/27/22    S Ramachandran  CAP-35439   Get Punchout Transfer Cart validation
 *  09/30/2022 	Sumit kumar		CAP-35440  	Create API service to do Transfer Cart
 *  
 */

package com.rrd.c1ux.api.services.punchout;

import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface PunchoutService {

	public COShoppingCartResponse validatePunchoutTransferCart(SessionContainer mainSession,
			COShoppingCartResponse objCOShoppingCartResponse) throws AtWinXSException;
	public String transferPunchoutCart(SessionContainer sc) throws AtWinXSException;

}
