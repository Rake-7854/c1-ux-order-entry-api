/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	05/25/23				S Ramachandran			CAP-38157					Initial Version
 *	09/13/23				Satishkumar A			CAP-43685					C1UX - BE - Add translation messages for order submit and routed orders
 */
package com.rrd.c1ux.api.models.checkout;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.rrd.c1ux.api.models.BaseResponse;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Schema(name = "SubmitOrderResponse", description = "Response Class for Submit Order", type = "object")
public class SubmitOrderResponse extends BaseResponse {

	@Schema(name ="salesReferenceNumber", description = "Sales Reference Number in CustomPoint", type = "string", example="80031326")
	@Size(min=0, max=20)
	private String salesReferenceNumber;

	@Schema(name ="wcssOrderNumber", description = "WCSS Order Number in WCSS - may be empty if order is not in WCSS", type = "string", example="27889425")
	@Size(min=0, max=8)
	private String wcssOrderNumber;
	
	@Schema(name ="orderIdCanQuickCopy", description = "'Order Id' of Order that has just been submitted, if it is eligible for Quick Copy", type = "int", example="581863")
	@Min(-1)
	@Max(2147483647)
	private int orderIdCanQuickCopy= AtWinXSConstant.INVALID_ID;
	
	//CAP-43685
	@Schema(name ="orderLabel", description = "Label to indicate if order is submitted or routed.", type = "string", example="Submit Order")
	@Size(min=0, max=25)
	private String orderLabel;
	
	//CAP-43685
	@Schema(name ="orderHeaderDescription", description = "Description to indicate if order is submitted or routed.", type = "string", example="Thank you for your order.")
	@Size(min=0, max=100)
	private String orderHeaderDescription;

		
}