
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 * 	08/30/23				Krishna Natarajan		CAP-43371					Added service implementation method to send back translation 
 */
package com.rrd.c1ux.api.services.users;

import java.util.Properties;

import org.springframework.stereotype.Repository;

import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.framework.session.ApplicationSession;

@Repository
public class UserStateProfileImpl extends BaseService implements UserStateProfileService{

	protected UserStateProfileImpl(TranslationService translationService) {
		super(translationService);
	}

	@Override
	public UserStateProfile getTranslation(UserStateProfile profile, ApplicationSession sc) {
		Properties resourceBundleProps = translationService.getResourceBundle(sc.getAppSessionBean(), "profileMenu");
		profile.setTranslation(translationService.convertResourceBundlePropsToMap(resourceBundleProps));
		return profile;
	}

}
