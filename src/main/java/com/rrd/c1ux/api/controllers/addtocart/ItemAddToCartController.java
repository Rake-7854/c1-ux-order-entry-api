/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	08/29/22	A Boomker			CAP-35537		Make session optional on all API calls
 *  12/05/22    M Sakthi			CAP-37387       Fix Unauthorized API Exception Handling - item add to cart, navigation menu, get cart items
 *	07/25/23	A Boomker			CAP-42223		Flag to redirect to cust doc UI added
 *	04/05/24	A Boomker			CAP-48729 		Added admin error translation for a cust doc item
 *	06/05/24	C Codina			CAP-49893		Validation added to check if needs to redirect to kit builder
 */
package com.rrd.c1ux.api.controllers.addtocart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.controllers.custdocs.CustomDocsUserInterfaceController;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartRequest;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartResponse;
import com.rrd.c1ux.api.models.addtocart.mappers.ItemAddToCartMapper;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.services.addtocart.ItemAddToCartService;
import com.rrd.c1ux.api.services.addtocart.ItemAddToCartServiceImpl;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.gwt.common.exception.CPRPCRedirectException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.tt.arch.TTException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Satishkumar Abburi
 *
 */
@RestController("ItemAddToCart")
@RequestMapping(RouteConstants.GET_ITEM_ADD_TO_CART)
@Tag(name = "api/items/addtocart")
public class ItemAddToCartController extends BaseCPApiController{

    private static final Logger logger = LoggerFactory.getLogger(ItemAddToCartController.class);
    private final ItemAddToCartMapper mItemAddToCartMapper;

    private final ItemAddToCartService itemAddToCartService;

    protected CustomDocsService customDocsService;

    /**
     * @param tokenReader
     * @param sessionReader
     * */
    protected ItemAddToCartController(TokenReader tokenReader, CPSessionReader sessionReader, ItemAddToCartMapper itemAddToCartMapper, ItemAddToCartService itemAddToCartService, CustomDocsService customDocsService) {
	super(tokenReader, sessionReader);
	mItemAddToCartMapper = itemAddToCartMapper;
	this.itemAddToCartService = itemAddToCartService;
	this.customDocsService = customDocsService;
    }

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
		}

	/**
	 * @param ttsession {@link String}
	 * @param itemInputs {@link ItemAddToCartRequest}
	 * @return the item is added to the cart or not
	 * @throws AtWinXSException
	 * @throws TTException
	 * @throws CPRPCException
	 * @throws CPRPCRedirectException
	 */
	@PostMapping(
	        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	    @Operation(
	        summary = "Add the requested item to the cart of corresponding login user")
	public ItemAddToCartResponse itemAddToCart(@RequestHeader(value = "ttsession", required=false) String ttsession, @RequestBody ItemAddToCartRequest itemInputs) throws AtWinXSException,  CPRPCRedirectException, CPRPCException {
			SessionContainer sc = getSessionContainer(ttsession);
			ItemAddToCartResponse  addToCartResponse = itemAddToCartService.addItemToCart(sc, itemInputs);
			// CAP-42223 - check if need to initialize a cust doc ui
			if (addToCartResponse.isRedirectToCustomDocumentUI())
			{
				callCustomDocumentInitializeAPI(itemInputs, addToCartResponse, sc);
			}
			//CAP-49893
			if (addToCartResponse.isRedirectToKitBuilder()) 
			{
				addToCartResponse.setStatus(ItemAddToCartServiceImpl.STATUS_SUCCESS);
			}
			return mItemAddToCartMapper.isItemAddedToCart(addToCartResponse);

	}

	// CAP-42223 - try to intialize the cust doc ui
	protected void callCustomDocumentInitializeAPI(ItemAddToCartRequest itemInputs, ItemAddToCartResponse response, SessionContainer sc) {
		CustomDocsUserInterfaceController cdController = new CustomDocsUserInterfaceController(mTokenReader, mSessionReader, customDocsService);
		try {
			ResponseEntity<C1UXCustDocBaseResponse> cdResponse = cdController.initializeUI(null, new C1UXCustDocInitializeRequest(itemInputs));
			if (cdResponse.getStatusCode() == HttpStatus.OK)
			{
				response.setStatus(ItemAddToCartServiceImpl.STATUS_SUCCESS);
			}
			else
			{
				response.setRedirectToCustomDocumentUI(false);
				C1UXCustDocBaseResponse body = cdResponse.getBody();
				if (body != null)
				{
					response.setMessage(Util.nullToEmpty(body.getMessage()));
				}
				else
				{
					setItemAdminSettingsError(response, sc);
				}
			}
		}
		catch(AtWinXSException e)
		{
			logger.error(e.toString());
			response.setStatus(ItemAddToCartServiceImpl.STATUS_SUCCESS);
			response.setRedirectToCustomDocumentUI(false);
			setItemAdminSettingsError(response, sc);
		}
	}
		
	// CAP-48729 - admin error for an item
	protected void setItemAdminSettingsError(ItemAddToCartResponse response, SessionContainer sc) {
		response.setMessage(customDocsService.getTranslation(sc.getApplicationSession().getAppSessionBean(), SFTranslationTextConstants.AUTH_ERROR_ADMIN_ADDING_ITEM,
				 SFTranslationTextConstants.AUTH_ERROR_ADMIN_ADDING_ITEM_DEFAULT, null));
	}
}
