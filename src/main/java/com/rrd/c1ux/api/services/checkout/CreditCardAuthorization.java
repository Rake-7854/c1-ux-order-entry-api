/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *  10/06/23	T Harmon			CAP-44417		Added credit card authorization
 */

package com.rrd.c1ux.api.services.checkout;

import java.util.Locale;

import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.orderentry.util.OrderDetailsFormBean;

public interface CreditCardAuthorization {
	public boolean authorizeCreditCard(OrderDetailsFormBean formBean, Message errMsg, CustomizationToken customToken, Locale locale, AppSessionBean appSessionBean) throws AtWinXSException;
	public String displayErrorMsg(Message msg);
}
