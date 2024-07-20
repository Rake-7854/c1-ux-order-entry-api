/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	01/31/24				N Caceres				CAP-46698					Initial Version
 *	02/16/24				C Codina				CAP-47086					C1UX BE - Order wizard api that will perform search
 */
package com.rrd.c1ux.api.services.catalog.wizard;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchRequest;
import com.rrd.c1ux.api.models.catalog.OrderWizardSearchResponse;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionRequest;
import com.rrd.c1ux.api.models.catalog.wizard.OrderWizardQuestionResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface OrderWizardService {

	OrderWizardQuestionResponse getOrderWizardQuestion(SessionContainer sc, OrderWizardQuestionRequest request)
			throws AccessForbiddenException;
	OrderWizardSearchResponse performWizardSearch(SessionContainer sc, OrderWizardSearchRequest request) throws AtWinXSException;
}
