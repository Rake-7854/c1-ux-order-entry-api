/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	---------------------------------------------------------------------
 *	26/07/22	Sakthi M			CAP-34855	Initial creation, Catalog Line View - Units and Qty revised validation
 *	11/08/22	A Boomker			CAP-37089	In getItemDetailWithQuantity(), allow retrieve of any vendor item
 *	12/12/22	A Boomker			CAP-37637	In getQtyValidation(), only validate if both are not empty/null
 *	01/19/23	A Boomker			CAP-38255 	In getQtyValidation(), add validation to check if above max-int
 *  02/23/23    Sakthi M            CAP-38336   Item Qty Validation API - functional change - fix display of UOM in max validation message with qty over MAXINT
 *  03/23/23 	Sakthi M			CAP-38561 	Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
 *  04/25/23    Sakthi M			CAP-39335   Validation labels in Item Quantity Validation API to make/use translation text values
 *  05/23/23	N Caceres			CAP-39046	Resolve concurrency issues in ItemQuantityValidation Service
 *  06/27/23	N Caceres			CAP-41120	Validate uomDesc and orderQty
 *  03/05/24	A Boomker			CAP-47679	Validate must always return true for some scenarios like bundles
 */

package com.rrd.c1ux.api.services.itemqtyvalidation;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationRequest;
import com.rrd.c1ux.api.models.itemqtyvalidation.ItemQtyValidationResponse;
import com.rrd.c1ux.api.models.items.SearchResult;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemQtyValidation;
import com.rrd.c1ux.api.util.ItemUtility;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

@Service
public class ItemQuantityValidationServiceImpl extends BaseService implements ItemQuantityValidationService {

	  private static final Logger logger = LoggerFactory.getLogger(ItemQuantityValidationServiceImpl.class);

	// CAP-39046 Dependency injection for new wrapper objects
	private final ManageItemsInterfaceLocatorService manageItemsInterfaceLocatorService;

	public ItemQuantityValidationServiceImpl(TranslationService translationService, ManageItemsInterfaceLocatorService manageItemsInterfaceLocatorService) {
		super(translationService);
		this.manageItemsInterfaceLocatorService = manageItemsInterfaceLocatorService;
	}

	ItemQtyValidation itemQtyValidation = new ItemQtyValidation();

	@Override
	public ItemRptVO getItemDetailWithQuantity(SessionContainer sc, ItemQtyValidationRequest request)
			throws AtWinXSException {
		// CAP-39046 Create wrapper object for ManageItemsInterfaceLocator
		// CAP-41120 Extracted this code into a method to prevent code duplicate
		IManageItemsInterface itemInterface = getItemInterface(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		return itemInterface.getWCSSItem(request.getVendorItemNumber()); // CAP-37089 - always retrieve it here
	}

	// CAP-47679 - fixing bundle and other cases where uom/qty not applicable
	protected void setValidationSuccess(ItemQtyValidationResponse response) {
		response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
		response.setStatusMessage(RouteConstants.VALIDATION_SUCCESS);
	}

	protected String getItemClassification(AppSessionBean asb, ItemQtyValidationRequest request) throws AtWinXSException {
		IManageItemsInterface itemInterface = getItemInterface(asb.getCustomToken());
		return itemInterface.checkItemClassification(asb.getSiteID(), request.getItemNumber(), request.getVendorItemNumber());
	}

	protected boolean alwaysPassScenario(ItemQtyValidationRequest request, AppSessionBean asb) {
		try {
			if (Util.isBlankOrNull(request.getUomDesc())) {
				return true;
			}
			String itemClass = getItemClassification(asb, request);
			return (ItemConstants.ITEM_CLASS_BUNDLE.equals(itemClass));
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	/* Added validation in util package and call this method */
	@Override
	public ItemQtyValidationResponse getQtyValidation(SessionContainer sc, ItemQtyValidationRequest request)
			throws AtWinXSException {
		ItemQtyValidationResponse validationResponse = new ItemQtyValidationResponse();
		// CAP-47679 - fix bundle and other cases where uom/qty not applicable (like eventually EFD only items)
		if (alwaysPassScenario(request, sc.getApplicationSession().getAppSessionBean())) {
			setValidationSuccess(validationResponse);
			return validationResponse;
		}
		// CAP-41120 Added UOM validation
		String errMsg = null;
		boolean uomIsValid = false;
		if (!Util.isBlankOrNull(request.getUomDesc())) {
			uomIsValid = uomDescIsValid(sc, request.getVendorItemNumber(), request.getUomDesc(), request.getItemNumber());
		}
		if (!Util.isBlankOrNull(request.getOrderQty()) && uomIsValid) { // CAP-37637
			errMsg = validateOrderQuantityWithValidUOM(sc, request);
		// CAP-41120 Validate uomDesc and orderQty if empty or format is invalid.
		} else {
			errMsg = validateUOMandOrderQuantity(sc, request, uomIsValid);
		}

		if (errMsg != null) {
			validationResponse.setStatus(RouteConstants.REST_RESPONSE_FAIL);
			validationResponse.setStatusMessage(errMsg);
		} else {
			setValidationSuccess(validationResponse);
		}

		return validationResponse;
	}

	// refactoring for junit coverage
	protected String validateOrderQuantityWithValidUOM(SessionContainer sc, ItemQtyValidationRequest request)
			throws AtWinXSException {
		String errMsg = null;
		// UOM And Quantity validation
		String[] uom = request.getUomDesc().split("_");
		// CAP-39046 Resolve concurrency issues in ItemQuantityValidation Service
		String uomDescription = uom[0];
		int uomFactor = Integer.parseInt(uom[1]);
		// CAP-39046 Resolve concurrency issues in ItemQuantityValidation Service
		ItemRptVO itemRptVO = getItemDetailWithQuantity(sc, request);
		// CAP-38255 - add validation to check if above max-int
		long qtyLong = Util.safeStringToDefaultLong(request.getOrderQty(), -1L);

		// CAP-39335 Ternary operator condition changed to if else condition
		if (((qtyLong < 0L) || (uomFactor == 0)) || (qtyLong > Integer.MAX_VALUE)) {
			if ((qtyLong < 0L) || (uomFactor == 0)) {
				// CAP-39046 Create wrapper object for TranslationTextTag
				// CAP-41120 Simplify this code block
				errMsg = getErrorMessage(sc, SFTranslationTextConstants.INVALID_QTY);

			} else {
				// CAP-39046 Create wrapper object for TranslationTextTag
				// CAP-41120 Simplify this code block
				errMsg = getErrorMessage(sc, SFTranslationTextConstants.PREFIX_SF + SFTranslationTextConstants.VALIDATION_MAX_MSG) + " "
						+ ((int) itemRptVO.getMaxinumOrderQty() / uomFactor) + " " + ItemUtility.getUOMAcronyms(
								uomDescription, false, sc.getApplicationSession().getAppSessionBean());
			}
		} else {
			errMsg = itemQtyValidation.validateItemQuantity(sc.getApplicationSession().getAppSessionBean(),
					(int) qtyLong, uomFactor, itemRptVO, ItemUtility.getUOMAcronyms(uomDescription, false,
							sc.getApplicationSession().getAppSessionBean())); // CAP-39335 Replaced itemNumer,minQty,maxQty,mulQty params with  itemRptVO
		}

		return errMsg;
	}

	// CAP-41120 Validate uomDesc and orderQty
	private String validateUOMandOrderQuantity(SessionContainer sc, ItemQtyValidationRequest request, boolean uomIsValid)
			throws AtWinXSException {
		String errMsg = null;

		if (!Util.isBlankOrNull(request.getOrderQty()) && !Util.isNumeric(request.getOrderQty())) {
			errMsg = getErrorMessage(sc, SFTranslationTextConstants.INVALID_QTY);
		} else {
			if (!Util.isBlankOrNull(request.getUomDesc()) && !uomIsValid) {
				errMsg = getErrorMessage(sc, SFTranslationTextConstants.INVALID_UOM);
			}
		}
		return errMsg;
	}

	private String getErrorMessage(SessionContainer sc, String errorName) throws AtWinXSException {
		return translationService.processMessage(
				sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
				sc.getApplicationSession().getAppSessionBean().getCustomToken(),
				errorName);
	}

	private boolean uomDescIsValid(SessionContainer sc, String vendorItemNumber, String uomDesc, String itemNumber) throws AtWinXSException {
		OEOrderSessionBean osb = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		SearchResult sr = new SearchResult();
		IManageItemsInterface itemInterface = getItemInterface(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		String itemClassification = itemInterface.checkItemClassification(sc.getApplicationSession().getAppSessionBean().getSiteID(), vendorItemNumber, itemNumber);
		ItemUtility.setItemUOMOptions(sr, sc.getApplicationSession().getAppSessionBean(), itemClassification, false,
				sc.getApplicationVolatileSession().getVolatileSessionBean(), vendorItemNumber, null, itemInterface,
				osb.getUserSettings(), null);

		if (CollectionUtils.isNotEmpty(sr.getUomArrLst())) {
			return sr.getUomArrLst().stream().anyMatch(uomItems -> uomItems.getUnitName().equalsIgnoreCase(uomDesc));
		}

		return false;
	}

	private IManageItemsInterface getItemInterface(CustomizationToken token) throws AtWinXSException {
		return manageItemsInterfaceLocatorService.locate(token);
	}
}
