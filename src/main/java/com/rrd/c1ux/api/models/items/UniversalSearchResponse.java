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
 *  04/21/22    S Ramachandran  CAP-33686   Initial Creation, added for Multiple Item Search (predictive search)
 *  12/01/22    M Sakthi        CAP-37551  Change universal search API to only return a max of 5 items and add flag indicating if more
 */

package com.rrd.c1ux.api.models.items;

import com.rrd.c1ux.api.controllers.RouteConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversalSearchResponse {

	private String status = RouteConstants.REST_RESPONSE_FAIL;
	private UniversalSearch[] LineItemsVO; 
	private boolean moreResultsAvailable=false;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public UniversalSearch[] getLineItemsVO() {
		return LineItemsVO;
	}

	public void setLineItemsVO(UniversalSearch[] lineItemsVO) {
		LineItemsVO = lineItemsVO;
	}
	
	public boolean getMoreResultsAvailable() {
		return moreResultsAvailable;
	}
	
	public void setMoreResultsAvailable(boolean moreResultsAvailable) {
		this.moreResultsAvailable = moreResultsAvailable;
	}
}


