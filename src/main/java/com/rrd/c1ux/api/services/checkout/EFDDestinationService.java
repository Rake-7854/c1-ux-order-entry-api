/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				JIRA#			Description
 * 	--------	-----------				----------		--------------------------------
 *	05/13/24	S Ramachandran			CAP-49326		Initial Version
 *	05/13/24	N Caceres				CAP-49344		Get EFD Options API
 *	05/16/24	Satishkumar A			CAP-49311		C1UX BE - Create new API to save EFD information
 */

package com.rrd.c1ux.api.services.checkout;

import com.rrd.c1ux.api.models.checkout.EFDOptionsResponse;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationRequest;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationResponse;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationRequest;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface EFDDestinationService {

	public EFDStyleInformationResponse getEfdStyleInformationForOrder(SessionContainer sc,
			EFDStyleInformationRequest request) throws AtWinXSException;
	
	public EFDOptionsResponse getEFDOptions(SessionContainer sc) throws AtWinXSException;
	//CAP-49311
	public SaveEfdInformationResponse saveEfdInformation(SessionContainer sc, SaveEfdInformationRequest efdInformationRequest) throws AtWinXSException;
	
}
