/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/11/22				A Boomker			      CAP-35766 				Modification for Punchout flags
 */
package com.rrd.c1ux.api.models.users.mappers;

import org.mapstruct.Mapper;

import com.rrd.c1ux.api.models.users.UserStateProfile;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;

@Mapper(componentModel = "spring")
public interface UserStateProfileMapper {
    
    UserStateProfile fromSiteBUGroupLoginProfileVO(SiteBUGroupLoginProfileVO profileVO, ApplicationSession sc);
}
