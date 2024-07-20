/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/15/22	Satish kumar A	CAP-35429- support page 	Initial creation
 *  09/28/22	Satish kumar A	CAP-35430- support page 	Send email with support page form data
 */

package com.rrd.c1ux.api.services.help;

import com.rrd.c1ux.api.models.help.SupportContactRequest;
import com.rrd.c1ux.api.models.help.SupportContactResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SupportContactService {

	//CAP-35429- To load support page information
	public SupportContactResponse populateSupportContactDetails(SessionContainer sc) throws AtWinXSException;
	
	//CAP-35430 - Support Page - Handle submission of email information and sending of email
	public SupportContactResponse sendMailToSupportContactDetails(SessionContainer sc, SupportContactRequest scRequest) throws AtWinXSException;


}
