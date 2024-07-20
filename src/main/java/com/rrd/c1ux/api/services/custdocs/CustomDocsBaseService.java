/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	07/25/23	A Boomker			CAP-92223		Initial version
 * 	07/26/23	A Boomker			CAP-92225		Added redirect routing to be used when the result of this call should indicate routing
 *	08/22/23	A Boomker			CAP-43223		Moving things around for junits
 *	11/07/23	A Boomker			CAP-44427 		Refactored some things for working proofs
 *	11/13/23	A Boomker			CAP-44426		Added handling for update working proof
 *	12/04/23	A Boomker			CAP-45654		Added method to set dirty flag
 *	02/14/24	A Boomker			CAP-46309		Moved some methods here
 *	02/27/24	A Boomker			CAP-47446		Moved checkIfBUAllowsEPS() here
 *	03/13/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public interface CustomDocsBaseService {

	public static final String ENTRY_POINT_NEW_REQUEST_STRING = ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST;
	public static final String ENTRY_POINT_ORDER_QUESTIONS_STRING = ICustomDocsAdminConstants.ENTRY_POINT_ORDER_QUESTIONS;

	public String getItemClassification(AppSessionBean asb, Map<String, String> initializeParams) throws AtWinXSException;

	public String getItemClassFromSession(SessionContainer sc);

	public String getItemClassFromRequest(SessionContainer sc, Map<String, String> requestParams);

	public UserInterface.UIEvent lookupEvent(String code);

	public void saveFullOESessionInfo(OrderEntrySession sess, int sessionID) throws AtWinXSException;

	public void saveFullSessionInfo(BaseSession sess, int sessionID, int serviceID) throws AtWinXSException;

	// CAP-44839 - added method for when giving real error details would be too useful to hackers, so make it generic
	public void setGenericHeaderError(C1UXCustDocBaseResponse sfBean, AppSessionBean asb);

	// CAP-46490 - refactor so that services can be extended by different item classes
	public void setCustomizationTokenForSession(CustomizationToken token);

	public TranslationService getTranslationService();

	public ObjectMapFactoryService getObjectMapFactoryService();

	public SessionHandlerService getSessionHandlerService();

	public ManageItemsInterfaceLocatorService getManageItemsService();

}
