/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-------------------------------------------------------------------
 *  08/01/22    S Ramachandran  CAP-35025   Initial creation, added for update shopping cart connectone Service
 *  08/18/22    S Ramachandran  CAP-35559   Refactor shopping cart service to include load, update, remove method
 *  09/08/2022  Sakthi M        CAP-35437   Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
 *  23/09/22	Satishkumar A   CAP-36164   Added getUOMAndQuantityErrorMessage method to add default UOM, Quantity error message to response
 *  02/27/23    M Sakthi         CAP-38710  API Fix - Add Translation to ResponseObject for /api/items/getcartitemdetail
 *  03/23/23 	Sakthi M		CAP-38561 	Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
 *  08/11/23	A Boomker		CAP-42295	Added initialize edit from cart API for cust docs
 *  03/28/24	N Caceres		CAP-47795	Add validation for Budget Allocation
 */

package com.rrd.c1ux.api.services.shoppingcart;

import java.util.List;

import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.models.items.UOMForCartItems;
import com.rrd.c1ux.api.models.shoppingcart.CORemoveSpecificItemRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.rrd.c1ux.api.models.shoppingcart.ContinueShoppingResponse;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartRequest;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.orderentry.vo.CartUpdateLineVO;

public interface ShoppingCartService  {

	public OEShoppingCartFormBean loadShoppingCart(SessionContainer sc, CartUpdateLineVO[] cartLinesWithErrors,
			boolean validateItems) throws AtWinXSException;

	public COShoppingCartResponse processUpdateShoppingCart(SessionContainer mainSession,
			COShoppingCartRequest scRequest) throws AtWinXSException;

	public COShoppingCartResponse removedSpecificItem(SessionContainer sc, CORemoveSpecificItemRequest req,
			OEShoppingCartFormBean oeShoppingCartFormBean, String actionId) throws AtWinXSException;

	//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
	public List<UOMForCartItems> getUOMForShoppingCartItems(OEShoppingCartFormBean formBean,AppSessionBean appSessionBean) throws AtWinXSException;

	// CAP-35437-Create 3 API services - one to return URL for destination for
	// continue shopping and 2 for repeats
	public ContinueShoppingResponse determineContinueDestination(SessionContainer sc);

	// CAP-36082 added a new method
	public COShoppingCartResponse loadDisclaimers(SessionContainer sc, COShoppingCartResponse coresponseObject);

	//CAP-36164 - Basic Error Message - for FE validation.
	public COShoppingCartResponse getUOMAndQuantityErrorMessage(SessionContainer sc, COShoppingCartResponse response) throws AtWinXSException;

	// CAP-38710  API Fix - Add Translation to ResponseObject for /api/items/getcartitemdetail
	public COShoppingCartResponse getShoppingCartItemDetail(SessionContainer sc) throws AtWinXSException;

	// CAP-42295
	public C1UXCustDocInitializeRequest createInitializeUIEditRequest(SessionContainer sc,
			CustDocEditCartRequest itemInputs) throws AtWinXSException;
}
