/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	11/14/23	N Caceres			CAP-45040		Add new field criteriaLength
 */
package com.rrd.c1ux.api.models.orders.ordersearch;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COOrderSearchCriteria", description = "Order Search Criteria object with label and value", type = "object")
public class COOrderSearchCriteria {

	@Schema(name ="criteriaLabel", description = "Order Search Criteria Label.", type = "string", example="PO #" , allowableValues = {"PO #", "Sales Ref No.", "Order Status", "Item No.","Order Name.","Invoice No.", "Order No."})
	@Size(min=0)
	private String criteriaLabel;
	@Schema(name ="criteriaValue", description = "Order Search Criteria Value.", type = "string", example="CriteriaPONumber", allowableValues = {"CriteriaPONumber", "CriteriaSalesRef", "CriteriaStatusCode", "CriteriaItemNumber","CriteriaInvoiceNo","CriteriaOrderTitle", "CriteriaOrderNumber"})
	@Size(min=0, max=20)
	private String criteriaValue;
	@Schema(name ="criteriaLength", description = "Order Search Criteria Length.", type = "int", example="1")
	@Size(min=0, max=25)
	private int criteriaLength;

}
