/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		 Modified By		Jira#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	2022.08.16	 T Harmon		 CAP-35537					 Initial creation
 *  2022.09.16   E Anderson      CAP-35362                   Add account.
 *  10/25/22	A Boomker		CAP-36153					Add entry point
 */
package com.rrd.c1ux.api.models.users;

import java.io.Serializable;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserContext", 
    title = "User Context",
    description = "Holds the user session context properties")
public class UserContext implements Serializable {

	private static final long serialVersionUID = -7148441083159609340L;
	private String userName;
    private String org;
    private String profileId;
    private String cpSessionId;
    private String firstName;
    private String lastName;
    private String email;
    private String account;
    private String entryPoint; 


    @Override
    public String toString() {
        if (profileId == null) {
            profileId = AtWinXSConstant.EMPTY_STRING;
        }
        //TODO: add profileId
        return String.format("{ userName: %s, org: %s, profileId: %s , cpSessionId: %s, firstName: %s, lastName: %s, email: %s, account: %s, entryPoint: %s}", userName, org, profileId, cpSessionId, firstName, lastName, email, account, entryPoint);
    }
}
