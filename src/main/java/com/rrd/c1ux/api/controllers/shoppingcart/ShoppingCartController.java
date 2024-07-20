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
 *  08/01/22    S Ramachandran  CAP-35025   Initial creation, added for update shopping cart controller
 *  08/16/22    S Ramachandran  CAP-35559   Refactor shopping cart controller to include load, update, remove method
 *  08/22/22    M Sakthi        CAP-35244   Add parameters with full text on UOM & sold as acronyms in the existing API for get shopping cart
 *	08/29/22	A Boomker		CAP-35537	Make session optional on all API calls
 *  09/08/2022  Sakthi M        CAP-35437   Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
 *  16/09/22	Krishna Natarajan CAP-36082 Modify getCartItemDetail API to add disclaimers to response
 *  23/09/22	Satishkumar A   CAP-36164   Modify getCartItemDetail API to add default UOM, Quantity error message to response
 *  11/10/22	Krishna Natarajan CAP-37191 Add the showvendor flag
 *  12/05/22    M Sakthi		 CAP-37387  Fix Unauthorized API Exception Handling - item add to cart, navigation menu, get cart items
 *  08/11/23	A Boomker		CAP-42295	Added initialize edit from cart API for cust docs
 *  09/25/23	A Boomker		CAP-44035 	In editCustDocInCart(), make sure we didn't blow up, but allow failed validation
 *  03/28/24	N Caceres		CAP-47795	Add validation for Budget Allocation
 */

package com.rrd.c1ux.api.controllers.shoppingcart;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.controllers.custdocs.CustomDocsUserInterfaceController;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.shoppingcart.CORemoveSpecificItemRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.rrd.c1ux.api.models.shoppingcart.ContinueShoppingResponse;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartResponse;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartService;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.gwt.common.exception.CPRPCRedirectException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("ShoppingCartController")
public class ShoppingCartController extends BaseCPApiController{

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    private final ShoppingCartService mService;

    private final CustomDocsService customDocsService;

    @Override
    protected int getServiceID() {

		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	protected ShoppingCartController(TokenReader tokenReader, CPSessionReader sessionReader,
			ShoppingCartService tShoppingCartService, CustomDocsService customDocsService) {
		super(tokenReader, sessionReader);
		mService=tShoppingCartService;
		this.customDocsService = customDocsService;
	}


	/**
	 * @param ttsession with the session ID
	 * @return {@link OEShoppingCartFormBean}
	 * @throws AtWinXSException
	 * @throws CPRPCRedirectException
	 * @throws CPRPCException
	 */
	//CAP-35559  - Merged GetShoppingCartItemsController CAP-35055 to ShoppingCartController controller
	@PostMapping(value = RouteConstants.GET_CART_ITEM_DETAIL, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "api/items/getcartitemdetail")
	@Operation(summary = "Get all items added to cart")
	public COShoppingCartResponse getShoppingCartItemDetail(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		logger.debug("In ShoppingCartController - getShoppingCartItemDetail() ");

		SessionContainer sc = getSessionContainer(ttsession);
		return mService.getShoppingCartItemDetail(sc);
	}


	/**
	 * @param sessionID with the session ID
	 * @return {@link OEShoppingCartFormBean}
	 * @throws AtWinXSException
	 * @throws CPRPCRedirectException
	 * @throws CPRPCException
	 */
	//CAP-35025 - Added update Shopping cart Controller
	@PostMapping(value=RouteConstants.UPDATE_SHOPPING_CART_ITEMS, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "/api/shoppingcart/updateshoppingcartitemdetail")
	@Operation(summary = "Update the requested item with UOM and Quantity to the cart line item table")
	public COShoppingCartResponse updateShoppingCartItemDetail(
			@RequestHeader(value = RouteConstants.REST_SESSIONID, required=false) String sessionID,
			@RequestBody COShoppingCartRequest scRequest) throws AtWinXSException {

		logger.debug("In ShoppingCartController - updateShoppingCartItemDetail() ");

		SessionContainer mainSession = getSessionContainer(sessionID);

		return doCartUpdate(mainSession, scRequest);
	}


	protected COShoppingCartResponse doCartUpdate(SessionContainer mainSession, COShoppingCartRequest scRequest) throws AtWinXSException {
		return mService.processUpdateShoppingCart(mainSession, scRequest);
	}

	//CAP-35559  - Merged RemoveSpecificItemFromCartController CAP-35055 to ShoppingCartController controller
	@PostMapping(value=RouteConstants.REMOVE_SPECIFIC_ITEM, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "removespecificitem/removespecificitem")
	@Operation(summary = "Remove specific item in the shoping cart")
	public COShoppingCartResponse removeSpecificItem(@RequestHeader(required=false) String ttsessionid,
			@RequestBody CORemoveSpecificItemRequest req) throws AtWinXSException {

		logger.debug("In ShoppingCartController - removeSpecificItem() ");

		SessionContainer sc = getSessionContainer(ttsessionid);

		OEShoppingCartFormBean oeShoppingCartFormBean = mService.loadShoppingCart(sc, null, true);

		return mService.removedSpecificItem(sc, req, oeShoppingCartFormBean, OrderEntryConstants.REMOVE_ITEM_FROM_CART_ACTION);
	}


	//CAP-35437-Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
	@PostMapping(value = RouteConstants.CART_CONTINUE_SHOPPING_REDIRECT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Tag(name = "api/shoppingcart/determineContinueDestination")
	@Operation(summary = "Add the requested item to the cart of corresponding login user")
	public ContinueShoppingResponse determineContinueDestination(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
		logger.debug("In ShoppingCartController - getContinueShopping() ");
		SessionContainer sc = getSessionContainer(ttsession);
		return mService.determineContinueDestination(sc);
	}

	@PostMapping(value = RouteConstants.CUST_DOCS_INITIALIZE_API_EDIT_CART, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Initialize edit session for UI requested")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CustDocEditCartResponse> editCustDocInCart(@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody CustDocEditCartRequest itemInputs) throws AtWinXSException {
			SessionContainer sc = getSessionContainer(ttsession);
			COShoppingCartResponse cartResponse = null;
			CustDocEditCartResponse response = new CustDocEditCartResponse();
			try {
				cartResponse = doCartUpdate(sc, itemInputs);
				if (cartResponse != null) { // CAP-44035 - make sure we didn't blow up, but allow failed validation
					response = callCustomDocumentInitializeAPI(sc, itemInputs);
				}
			}
			catch(Exception e) {
				logger.error("Failed to update shopping cart on edit custom document request.", e);
			}
			defaultGenericFailedMessage(response, sc);

	        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}


	protected void defaultGenericFailedMessage(CustDocEditCartResponse response, SessionContainer sc) {
		if ((!response.isSuccess()) && (Util.isBlankOrNull(response.getMessage())))
		{
			response.setMessage(getMessage(sc.getApplicationSession().getAppSessionBean(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR, SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR));
		}
	}

	protected String getMessage(AppSessionBean asb, String key, String defaultVal) {
		return customDocsService.getTranslation(asb.getDefaultLocale(), asb.getCustomToken(), key, defaultVal);
	}

	protected CustomDocsUserInterfaceController getCDController() {
		return new CustomDocsUserInterfaceController(mTokenReader, mSessionReader, customDocsService);
	}

	// CAP-42223 - try to intialize the cust doc ui
	protected CustDocEditCartResponse callCustomDocumentInitializeAPI(SessionContainer sc,
			CustDocEditCartRequest itemInputs) throws AtWinXSException {
		CustomDocsUserInterfaceController cdController = getCDController();
		CustDocEditCartResponse response = new CustDocEditCartResponse();

		try {
			ResponseEntity<C1UXCustDocBaseResponse> cdResponse = cdController.initializeUI(null, mService.createInitializeUIEditRequest(sc, itemInputs));
			if (cdResponse.getStatusCode() == HttpStatus.OK)
			{
				response.setSuccess(true);
			}
			else if (Optional.ofNullable(cdResponse.getBody()).isPresent())
			{
				response.setMessage(cdResponse.getBody().getMessage());
			}
		}
		catch(Exception e)
		{
			logger.error(e.toString());
			response.setMessage(getMessage(sc.getApplicationSession().getAppSessionBean(), TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE, AtWinXSConstant.EMPTY_STRING));
		}

		return response;
	}

}
