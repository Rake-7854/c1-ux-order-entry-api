/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 * 	08/30/23				Krishna Natarajan		CAP-43371					Added service method to send back translation 
 */

package com.rrd.c1ux.api.services.users;

import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.wallace.atwinxs.framework.session.ApplicationSession;

public interface UserStateProfileService {
	    UserStateProfile getTranslation(UserStateProfile profile, ApplicationSession sc);
}
