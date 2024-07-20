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
 *  05/19/22    S Ramachandran  CAP-34140   Initial Creation
 *  08/04/22    Satishkumar A   CAP-35247   Add availability status in Price & Availability API
 *  04/03/24	Krishna Natarajan	CAP-48388 Added new fields for flag and messaging on EnabledAddToCartBudget
 */

package com.rrd.c1ux.api.models.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PNAResponse {
	
		private String priceLabel = "Price"; 
		private String itemExtendedSellPrice;
		private String availabilityLabel = "Status";
		private String availabilityCode = "";
		private String availQtyLabel = "Available Qty";
		private String availableQty = "";
		private String priceTypeCode = "";
		private String isReplaceBackorderedMsgFlag = "";
		//CAP-35247
		private String availabilityStatus = "";
		//CAP-48388
		private boolean isEnabledAddToCartBudget =true;
		private String enabledAddToCartBudgetMessage="";
		
		public void setReplaceBackorderedMsgFlag(String isReplaceBackorderedMsgFlag)
		{
			this.isReplaceBackorderedMsgFlag = isReplaceBackorderedMsgFlag;
		}
		

}
