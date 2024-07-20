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

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelfAdminSiteAttributeValues implements  Serializable{
	 public static final long serialVersionUID = -2824760886745773968L;
	 int attributeValueID;
	 String attribtueValueDescription;
}
