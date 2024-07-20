/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date				Modified By				JIRA#						Description
 * 	--------			-----------				-----------------------		-------------------------------------------------------------
 *	05/08/23			A Boomker				CAP-38153					Initial Version
 */

package com.rrd.c1ux.api.models.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="GenericNameValuePair", description = "Class to define Name-Value Pair objects for requests", type = "object")
public class GenericNameValuePair implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8992826339221640751L;

	@JsonProperty("name")
	@Schema(name ="name", description = "Name for pair", type = "string", example="country")
	private String name = AtWinXSConstant.EMPTY_STRING;

	@JsonProperty("value")
	@Schema(name ="value", description = "Value for pair", type = "string", example="USA")
	private String value = AtWinXSConstant.EMPTY_STRING;

}
