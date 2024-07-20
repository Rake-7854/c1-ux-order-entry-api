/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	05/14/24	Krishna Natarajan	CAP-48582				Added missing field shipToAttention
 */

package com.rrd.c1ux.api.models.orders.ordertemplate;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor

@Schema(name ="LoadSaveOrderTemplateResponse", description = "Response Class for Load Save Order Template to load the Checkout", type = "object")
public class LoadSaveOrderTemplateResponse extends BaseResponse{
	
	@Schema(name = "orderTemplateId", description = "Order template Id", type = "String", example = "34234")
	private String orderTemplateId ="";
	
	@Schema(name = "orderTemplateName", description = "Order template Name", type = "String", example = "Test Order")
	private String orderTemplateName ="";
	
	@Schema(name = "isTemplateSharable", description = "Is Template Sharable", type = "boolean", example = "true")
	private boolean isTemplateSharable=false;
	
	@Schema(name = "isEditable", description = "Is Editable", type = "boolean", example = "true")
	private boolean isEditable=false;
	
	@Schema(name = "isShared", description = "Is Shared", type = "boolean", example = "true")
	private boolean isShared=false;
	
	@Schema(name = "showVendorItem", description = "Show vendor Item", type = "boolean", example = "true")
	private boolean showVendorItem=false;
	
	@Schema(name = "shipToName1", description = "Ship to Name 1", type = "String", example = "Test Address 1")
	private String shipToName1 ="";
	
	@Schema(name = "shipToName2", description = "Ship to Name 2", type = "String", example = "Test Address 2")
	private String shipToName2 ="";
	
	@Schema(name = "shipToAddress1", description = "Ship to Address 1", type = "String", example = "Test ship to address1")
	private String shipToAddress1 ="";
	
	@Schema(name = "shipToAddress2", description = "Ship to Address 2", type = "String", example = "Test ship to address2")
	private String shipToAddress2 ="";
	
	@Schema(name = "shipToAddress3", description = "Ship to Address 3", type = "String", example = "Test ship to address3")
	private String shipToAddress3 ="";
	
	@Schema(name = "shipToCity", description = "Ship to City", type = "String", example = "Test ship to City")
	private String shipToCity ="";
	
	@Schema(name = "shipToState", description = "Ship to State", type = "String", example = "Test ship to State")
	private String shipToState ="";
	
	@Schema(name = "shipToZip", description = "Ship to Zip", type = "String", example = "Test ship to Zip")
	private String shipToZip ="";
	
	@Schema(name = "shipToCountry", description = "Ship to Country", type = "String", example = "Test ship to county")
	private String shipToCountry ="";
	
	@Schema(name = "shipToAttention", description = "Ship to Attention", type = "String", example = "Test ship to Attn")
	private String shipToAttention ="";
	
	@Schema(name = "billToName1", description = "Bill to Name 1", type = "String", example = "Test bill to name 1")
	private String billToName1 ="";
	
	@Schema(name = "billToName2", description = "Bill to Name 2", type = "String", example = "Test bill to name 2")
	private String billToName2 ="";
	
	@Schema(name = "billToAddress1", description = "Bill to Address 1", type = "String", example = "Test bill to Address 1")
	private String billToAddress1 ="";
	
	@Schema(name = "billToAddress2", description = "Bill to Address 2", type = "String", example = "Test bill to Address 2")
	private String billToAddress2 ="";
	
	@Schema(name = "billToAddress3", description = "Bill to Address 3", type = "String", example = "Test bill to Address 3")
	private String billToAddress3 ="";
	
	@Schema(name = "billToCity", description = "Bill to City", type = "String", example = "Test bill to City")
	private String billToCity ="";
	
	@Schema(name = "billToState", description = "Bill to State", type = "String", example = "Test bill to State")
	private String billToState ="";
	
	@Schema(name = "billToZip", description = "Bill to Zip", type = "String", example = "Test bill to Zip")
	private String billToZip ="";
	
	@Schema(name = "billToCountry", description = "Bill to Country", type = "String", example = "Test bill to Country")
	private String billToCountry ="";
	
	@Schema(name = "orderLine", description = "Bill to Country", type = "object")
	private Collection<COOrderLines> orderLine =null;
	
	@Schema(name = "canCreateOrder", description = "Can create Order", type = "boolean", example = "true")
	private boolean canCreateOrder=false;

}
