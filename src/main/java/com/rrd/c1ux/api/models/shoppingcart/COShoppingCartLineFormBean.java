/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  10/12/22	A Boomker		CAP-36437	Lombok messes up any variables beginning uppercase
 *  05/28/24	Krishna Natarajan	CAP-49728	Added a new variable to identify/validate selected Delivery Option
 */
package com.rrd.c1ux.api.models.shoppingcart;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class COShoppingCartLineFormBean implements Serializable
{
	private String orderId = "";
	private String lineNumber = "";
	private String itemQuantity = "";
	private String uomFactor = "";
	private String uomCode = "";
	private String userIPAddress = null;
	private String selectedDeliveryOption =null;
}
