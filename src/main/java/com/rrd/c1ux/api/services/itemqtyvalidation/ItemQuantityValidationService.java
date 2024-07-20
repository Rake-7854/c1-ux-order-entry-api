/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	26/07/22	Sakthi M			 CAP-34855	Initial creation, Catalog Line View - Units and Qty revised validation
 */

package com.rrd.c1ux.api.services.itemqtyvalidation;

import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationRequest;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

public interface ItemQuantityValidationService {
	public ItemQtyValidationResponse getQtyValidation(SessionContainer sc,ItemQtyValidationRequest req) throws AtWinXSException;
	public ItemRptVO getItemDetailWithQuantity(SessionContainer sc, ItemQtyValidationRequest request) throws AtWinXSException;
}
