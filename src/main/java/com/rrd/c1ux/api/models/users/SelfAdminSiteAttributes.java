/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	02/01/24				Sakthi M				CAP-46634					Initial Version
 */

package com.rrd.c1ux.api.models.users;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelfAdminSiteAttributes{
	private int attributeID;
	private String attributeDisplayName;
	private int minAttributeValues;
	private int maxAttributeValues;
	private boolean viewOnly;
	private String displayType;
	private List<SelfAdminSiteAttributeValues> availableAttributes;
	private List<SelfAdminSiteAttributeValues> assignedAttributes;
}
