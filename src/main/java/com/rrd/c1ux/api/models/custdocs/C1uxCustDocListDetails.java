/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/22/24				R Ruth					CAP-48222					Initial Version
 *	04/22/24				A Boomker				CAP-48496					Added openapi3 doc
 */
package com.rrd.c1ux.api.models.custdocs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name ="C1uxCustDocListDetails", description = "Information about a specific list and its availability within Custom Document order entry", type = "object")
public class C1uxCustDocListDetails {
	@Schema(name ="listName", description = "Unique name used for the list in Manage Lists", type = "String")
	private String listName;
	@Schema(name ="listDescription", description = "Description used for the list in Manage Lists", type = "String")
	private String listDescription;
	@Schema(name = "listId", description = "Number of the list in Manage Lists", type = "number")
	private int listId;
	@Schema(name = "numRecords", description = "Number of record rows in the list", type = "number")
	private int numRecords;
	@Schema(name ="uploadDate", description = "Date of list upload formatted for User's locale", type = "string", example="01/25/2023")
	private String uploadDate;
	@Schema(name ="lastUsedDate", description = "Date of list last use formatted for User's locale", type = "string", example="01/25/2023")
	private String lastUsedDate;
	@Schema(name = "upload", description = "Flag indicating list was uploaded. Default value is true.", type = "boolean", allowableValues = {"false", "true"})
	private boolean upload = true;
	@Schema(name = "mapped", description = "Flag indicating list was mapped for this session. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean mapped = false;
	@Schema(name = "selected", description = "Flag indicating list is currently selected for this specification. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	private boolean selected = false;
}
