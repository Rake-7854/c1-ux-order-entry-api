/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------------------
 *  08/01/22    S Ramachandran  CAP-35025   Initial creation, added for update shopping cart connectone ServiceImpl
 *  08/18/22    S Ramachandran  CAP-35559   Refactor shopping cart service to include load, update, remove method
 *  09/08/2022  Sakthi M        CAP-35437   Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
 *  23/09/22	Satishkumar A   CAP-36164   Added getUOMAndQuantityErrorMessage method to add default UOM, Quantity error message to response
 *  29/09/22	A Boomker		CAP-36158	Added updateForPunchout() and the call to it in loadShoppingCart()
 *  10/12/22	A Boomker		CAP-36437	Lombok messes up any variables beginning uppercase
 *  10/25/22	A Boomker		CAP-36153	Fix redirect to specific category ID for continue shopping
 *  10/25/22	A Boomker		CAP-36793	Fix null pointer if no order ID
 *  10/25/22	A Boomker		CAP-36803	In processRemove...(), do not validate in load at the end.
 *  11/09/22	A Boomker		CAP-36874	In update and getting UOMs, use UOM_UOMF format
 *  12/06/22	A Boomker		CAP-36945	Update parameters for pricing change in CP
 *  12/13/22	Krishna Natarajan CAP-37645 Add a method to check backordered status + removing the <br> tag
 *	01/20/23	A Boomker		CAP-38253	Modified checkBackOrderToSetStatus() and updateCartFromRequest() to fix issue with BO message
 *	02/14/23	A Salcedo		CAP-38173	Updated for OEShoppingCartFormBean.setItems() for latest bundle.
 *  02/27/23    M Sakthi         CAP-38710  API Fix - Add Translation to ResponseObject for /api/items/getcartitemdetail
 *  03/23/23 	Sakthi M		CAP-38561 	Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
 *  05/10/23	C Codina		CAP-39336	API Change - Success/Error messages in Remove Item from Cart API to make/use translation text values
 *  05/01/23	N Caceres		CAP-39334	Use translation text values for return to labels
 *  05/05/23	A Boomker		CAP-40333	Added code to tell FE whether to show save order button in cart
 *  05/22/23	A Boomker		CAP-40779	Remove slow pna call from load cart method
 *  06/13/23	N Caceres		CAP-40882	Validate if item is existing in cart for removal
 *  06/16/23	S Ramachandran	CAP-41136	Shopping cart total price in displayable format
 *  07/13/23	Satishkkumar A	CAP-42213	C1UX BE - Shopping Cart - Default Quantity (Admin Setting at Site Level)
 *  08/11/23	A Boomker		CAP-42295	Added initialize edit from cart API for cust docs
 *	08/22/23	L De Leon		CAP-42663 	Added buildItemRoutingMessages() method and called in loadShoppingCart(0 method
 *	09/14/23	A Boomker		CAP-43843	Modifications for edit cust doc junits
 *	12/20/23	C Codina		CAP-45934	C1UX BE - Modify USPS address validation to allow a backorder warning from WCSS to go through
 *	02/12/24	Krishna Natarajan CAP-47109 Blocking the unnecessary call to buildItemRoutingMessages with a null check on OrderID, to inturn avoid calling enforceStateRestrictions checking orderID, to stop throwing null pointer exception
 *  02/17/24	Krishna Natarajan CAP-47085 	Created a new method to overwrite the UOM acronyms to full words
 *  03/07/24	T Harmon		CAP-46340	Fixed issue with remove item for eoo
 *	03/27/24	R Ruth			CAP-46491	Modify edit from cart initialize UI to work for bundle items
 *  03/28/24	N Caceres		CAP-47795	Add validation for Budget Allocation
 *	04/01/24	Krishna Natarajan CAP-48376	Added condition and set new translation message unableRetrieveCartValueMsg
 * 	04/11/24	Krishna Natarajan	CAP-48606		Added a new checkDistListSharedOrPrivate field doNotShareListsInd to update BU Manage List admin settings
 *	05/13/24	L De Leon		CAP-48977	Modified methods to populate delivery options list
 *  05/24/24	Krishna Natarajan	CAP-49673 	Added methods to update the efdCharges description to currency desc
 *  05/28/24	N Caceres		CAP-49693	Modify /api/items/getcartitemdetail method to move component order lines for bundles to the componentDisplayLines object in the OEShoppingCartLineFormBean
 *  05/28/24	Krishna Natarajan	CAP-49728	Added methods to get the delivery method updated as selected from FE
 *	05/29/24	A Boomker		CAP-46469	Change createInitializeUIEditRequest() for stub items
 *	05/30/24	Krishna Natarajan	CAP-49787 Added method to handle formbean to update the delivery method 
 *	06/03/24	Krishna Natarajan	CAP-49811 Added method to handle formbean to update the delivery method - for updateShoppingCart - OEShoppingCartFormBean
 *	06/06/24	Krishna Natarajan	CAP-50003 Rewrote the logic to handle the response object to retain the UOM-soldAs values for the FE 
 *	06/05/24	Krishna Natarajan	CAP-49903 Added a new line of code in order to update the itemImageUrl with SF no image URL, if empty, on updateshoppingcart
 *  06/13/24	Krishna Natarajan	CAP-50144 Rearraged a line of code to get the soldAs updated properly in the getcartitemdetail call
 *  06/13/24	Krishna Natarajan	CAP-50143 Added a line of code to get the bundle components wrapped up on updateshoppingcart
 *  06/13/24	Krishna Natarajan	CAP-50176 Added a new method and lines of code to existing method to update the efdcharges label under currency description for bundle items
 *  06/17/24	Krishna Natarajan	CAP-50204 Added a new method to update price for componentDisplayLines
 *  06/24/24	Krishna Natarajan	CAP-50471 Added new methods to update the price correctly for EFD items
 *  06/28/24	Krishna Natarajan	CAP-49811 Added new method to handle the itemTotalPricing based on deliverymethods
 *  07/08/24	Krishna Natarajan	CAP-50835 Added a logic to check the delivery method and update it
 */

package com.rrd.c1ux.api.services.shoppingcart;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.models.items.UOMForCartItems;
import com.rrd.c1ux.api.models.items.UOMItems;
import com.rrd.c1ux.api.models.shoppingcart.CORemoveSpecificItemRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartLineFormBean;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartRequest;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.rrd.c1ux.api.models.shoppingcart.ContinueShoppingResponse;
import com.rrd.c1ux.api.models.shoppingcart.CustDocEditCartRequest;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.locators.ListsAdminLocatorService;
import com.rrd.c1ux.api.services.orderentry.locator.OEShoppingCartComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.orderentry.entity.CartLine;
import com.rrd.custompoint.orderentry.entity.KitCartLine;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderLine.ComponentType;
import com.rrd.custompoint.orderentry.entity.ProfileSelection;
import com.rrd.custompoint.validator.orderentry.ShoppingCartValidator;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.interfaces.IManageListAdmin;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVO;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVOKey;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartLineFormBean;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.session.AllocationSummaryBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OEUomSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.CartUpdateLineVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.orderentry.vo.RoutingInfoCompositeVO;

@Service
public class ShoppingCartServiceImpl extends BaseOEService implements ShoppingCartService {

	private final String className =  this.getClass().getName();
	// CAP-40882 Dependency injection for new wrapper objects
	private final OEShoppingCartComponentLocatorService oeShoppingCartComponentLocatorService;

	private final ListsAdminLocatorService listsAdminLocatorService;//CAP-48606

	//CAP-47109
	public ShoppingCartServiceImpl(TranslationService translationService,
			OEShoppingCartComponentLocatorService oeShoppingCartComponentLocatorService,
			ObjectMapFactoryService objectMapFactoryService, OEAssemblerFactoryService oeAssemblerFactoryService,
			ListsAdminLocatorService listsAdminLocatorService) {
		super(translationService, objectMapFactoryService, oeAssemblerFactoryService);
		this.oeShoppingCartComponentLocatorService = oeShoppingCartComponentLocatorService;
		this.listsAdminLocatorService = listsAdminLocatorService;
	}

	private static final String DEFAULT_UOMF_STRING = String.valueOf(OrderEntryConstants.DEFAULT_UOM_FACTOR);
	private static final Logger logger = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);
	private static final String ZERO_PRICE="0.00";

	@Override
	/*loadShoppingCart and its supported methods are copied from CustomPoint from ShoppingCartBaseController*/
	public OEShoppingCartFormBean loadShoppingCart(SessionContainer sc, CartUpdateLineVO[] cartLinesWithErrors,
			boolean validateItems) throws AtWinXSException
	{
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();


		OEShoppingCartFormBean formBean;

		OEShoppingCartAssembler assembler = oeAssemblerFactoryService.getShoppingCartAssembler(
				appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate());

		// CAP-40779 - remove redundant pricing call

		// Get Shopping Cart here
		if (punchoutSessionBean != null)
		{
			//DTS 8140 - Added flag to check for invalid items.
			// This is a punchout order, load the shopping cart with punchout rules
			formBean = assembler.getPunchoutShoppingCart(
					volatileSessionBean.getOrderId(),
					oeOrderSessionBean,
					appSessionBean.getCorporateNumber(),
					cartLinesWithErrors,
					appSessionBean,
					punchoutSessionBean,
					validateItems,
					volatileSessionBean,
					appSessionBean.isCustomPointSite()); //DTS 10133
		}
		else
		{
			//DTS 8140 - Added flag to check for invalid items.
			formBean = assembler.getShoppingCart(
					volatileSessionBean.getOrderId(),
					//getOrderSessionBean(),
					oeOrderSessionBean,
					appSessionBean.getCorporateNumber(),
					cartLinesWithErrors,
					appSessionBean,
					validateItems,
					volatileSessionBean,
					appSessionBean.isCustomPointSite()); //DTS 10133
		}
		boolean backorderedItems = oeOrderSessionBean.getUserSettings().isShowOrderLineAvailability() && checkForBackorderedItems(formBean); // CAP-40779
		formBean.setBackorderedItems(backorderedItems);

		// CAP-42663
		if(null!=volatileSessionBean.getOrderId()) {//CAP-47109
		buildItemRoutingMessages(appSessionBean, volatileSessionBean, oeOrderSessionBean, formBean, assembler);
		}

		int orderScenarioNr = oeOrderSessionBean.getOrderScenarioNumber();
		//Flags to display cust doc instance name or disclaimer notes.
		boolean hideInstanceName = assembler.isCampaignSubscription(orderScenarioNr);
		boolean isMergeCamp = assembler.isMergeCamp(orderScenarioNr);
		boolean isSubcript = assembler.isSubscription(orderScenarioNr);

		formBean.setHideInstanceName(hideInstanceName);
		formBean.setMergCamp(isMergeCamp);
		formBean.setSubscript(isSubcript);

		// CAP-40779 - removing commented out code about calculating allocations for allocation summary, bundle component rules, and validating subscriptions

		// CAP-36158 - set flags correctly for punchout scenarios
		updateForPunchout(formBean, punchoutSessionBean, appSessionBean);
		//CAP-42213
		defaultQuantities(formBean,appSessionBean);
		return formBean;
	}

	// CAP-47795
	protected void validateBudgetAllocation(SessionContainer sc, OEShoppingCartAssembler assembler, OEShoppingCartFormBean formBean)
			throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		AllocationSummaryBean allocSummaryBean = null;
		int orderScenarioNr = oeOrderSessionBean.getOrderScenarioNumber();
		OECheckoutAssembler checkoutAssembler = oeAssemblerFactoryService.getCheckoutAssembler(volatileSessionBean, appSessionBean);
		AllocationQuantitiesCompositeVO allocQty = checkoutAssembler.getAllocationQuantities(oeOrderSessionBean, appSessionBean);
		oeSession.getOESessionBean().setForceCCOptionAllocation(false);
		if (allocQty != null && !formBean.isTbd())
		{
			allocSummaryBean = assembler.calculateRemainingBudget(formBean, punchoutSessionBean, userSettings, appSessionBean, volatileSessionBean, orderScenarioNr);
			formBean.setShowAllocation(true);
			if((allocQty.getRemainingQuantity() <= 0 && userSettings.isForceCreditCardOverAllcBudget() && formBean.getItems() != null && formBean.getItems().length > 0) ||
					(userSettings.isForceCreditCardOverAllcBudget() && allocSummaryBean != null && allocSummaryBean.getEstRemainingBudgetDoubleVal() < 0))
			{
				formBean.setBudgetAllocationWarnMsg(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.EXCEED_BUDGET_WARNING));
				formBean.setExceedBudgetWarned(true);
				oeSession.getOESessionBean().setForceCCOptionAllocation(true);
			}

			if (!userSettings.isForceCreditCardOverAllcBudget() && allocSummaryBean != null && allocSummaryBean.getEstRemainingBudgetDoubleVal() < 0)
			{
				formBean.setBudgetAllocationErrMsg(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.INSUFFICIENT_REMAINING_BUDGET));
				formBean.setExceedBudgetWarned(true);
				oeSession.getOESessionBean().setForceCCOptionAllocation(true);
			}
		}
		else if(userSettings.isAllowBudgetAllocations() && userSettings.isForceCreditCardOverAllcBudget()
				&& !userSettings.isAllowOrderingWithoutBudget() && formBean.getItems() != null && formBean.getItems().length > 0)
		{
			formBean.setBudgetAllocationWarnMsg(translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.EXCEED_BUDGET_WARNING));
			formBean.setExceedBudgetWarned(true);
			oeSession.getOESessionBean().setForceCCOptionAllocation(true);
		}
		else if (userSettings.isAllowBudgetAllocations() && !userSettings.isAllowUnavailablePriceOrd()//CAP-48376
				&& formBean.isTbd() && formBean.getItems() != null && formBean.getItems().length > 0) {
			formBean.setBudgetAllocationErrMsg(translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.UNABLE_RETRIEVE_CART_VALUE_MSG));
			oeSession.getOESessionBean().setForceCCOptionAllocation(true);
		}
	}

	protected void buildItemRoutingMessages(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeOrderSessionBean, OEShoppingCartFormBean formBean, OEShoppingCartAssembler assembler)
			throws AtWinXSException {
		OECheckoutAssembler checkoutAssembler = oeAssemblerFactoryService.getCheckoutAssembler(volatileSessionBean, appSessionBean);

		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		if (!appSessionBean.isDemoUser() && userSettings.isRoutingAvailable()
				&& (userSettings.isSubjToRnA() || oeOrderSessionBean.isAllowCstmListUpload())) {

			RoutingInfoCompositeVO routingInfo = checkoutAssembler.checkOrderForRouting(appSessionBean,
					oeOrderSessionBean, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, false);
			if (routingInfo != null) {
				boolean hasCLURoutingOnly = assembler.hasCstmListUploadRoutingOnly(routingInfo);
				oeOrderSessionBean.setCLURoutingOnly(hasCLURoutingOnly);

				assembler.buildOrderRoutingWarningMsg(formBean, oeOrderSessionBean, appSessionBean.getCurrencyLocale(),
						appSessionBean.getSiteID(), routingInfo, appSessionBean.getDefaultLocale(), // CP-5165
						appSessionBean.getDefaultLanguage(), volatileSessionBean, userSettings);
			}
		}
	}
	//CAP-42213
	protected OEShoppingCartFormBean defaultQuantities(OEShoppingCartFormBean formBean, AppSessionBean appSessionBean) {

		if((appSessionBean.getSiteDefaultQty() > 0) && (formBean != null) && (formBean.getItems() != null) && (formBean.getItems().length > 0)) {

			for (OEShoppingCartLineFormBean item: formBean.getItems())
			{
				if (DEFAULT_UOMF_STRING.equals(item.getItemQuantity()))
					item.setItemQuantity(String.valueOf(appSessionBean.getSiteDefaultQty()));
			}
		}

		return formBean;

	}
	// CAP-40779
	protected boolean checkForBackorderedItems(OEShoppingCartFormBean formBean) {
		boolean backorders = false;
		if ((formBean != null) && (formBean.getItems() != null) && (formBean.getItems().length > 0))
		{
			for (OEShoppingCartLineFormBean item: formBean.getItems())
			{
				if (OrderEntryConstants.AVAIL_CODE_NOT_AVAILABLE.equals(item.getAvailabilityCode()))
				{
					backorders = true;
				}
			}
		}
		return backorders;
	}

	@Override
	public COShoppingCartResponse removedSpecificItem(SessionContainer sc, CORemoveSpecificItemRequest req, OEShoppingCartFormBean oeShoppingCartFormBean,
			String actionId) throws AtWinXSException
	{
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		COShoppingCartResponse response = new COShoppingCartResponse();

		// CAP-40882 Wrap in service for easier unit testing
		OEShoppingCartAssembler assembler = oeAssemblerFactoryService.getShoppingCartAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate());

		//CAP-35024 - Commented out
		//OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
		//		appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

		//CAP-35024
		if(oeOrderSessionBean.getOrderScenarioNumber() != OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE)
		{
			//CP-10960 RAR - Now that we can have a Dist List with Item and Quantities option in the cart,
			//we need to make sure that we have the correct Order Scenario Number on form submit.
			recalculateOrderScenarioNumber(oeOrderSessionBean, oeShoppingCartFormBean.isSendToDistListUsingQtyFromFile(), oeShoppingCartFormBean.isLineItemShipping(), appSessionBean, volatileSessionBean); //CAP-29105
		}

		//CAP-35024 - Out of scope for now.
		//oeShoppingCartFormBean.setSendToDistributionList(checkoutAssembler.isDistributionListOrder(oeSession.getOESessionBean().getOrderScenarioNumber()));

		//CAP-35024
		try {
			oeShoppingCartFormBean = processRemoveItemFromShoppingCart(sc, assembler, req, actionId, oeShoppingCartFormBean);

			this.persistInSession(sc.getApplicationVolatileSession()); // CAP-3449
			response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
			//CAP-39336
			response.setStatusMessage(getTranslation(appSessionBean, SFTranslationTextConstants.SC_RESPONSE_SUCESS_VAL, SFTranslationTextConstants.SC_RESPONSE_SUCCESS_TEXT));
			response.setOeShoppingCartFormBean(oeShoppingCartFormBean);
			loadDisclaimers(sc, response);//CAP-36082
			// CAP-40333 - also determine if save order button should show
		    response.setShowSaveOrderButton(shouldShowSaveButton(sc));
		} catch (AtWinXSException e) {
			logger.error(e.getMessage());
			response.setStatus(RouteConstants.REST_RESPONSE_FAIL);
			//CAP-39336
			response.setStatusMessage(getTranslation(appSessionBean, SFTranslationTextConstants.SC_RESPONSE_ERROR_VAL, SFTranslationTextConstants.SC_RESPONSE_ERROR_TEXT));
			response.setOeShoppingCartFormBean(oeShoppingCartFormBean);
		}
		response.setUomForCartItems(getUOMForShoppingCartItems(oeShoppingCartFormBean,appSessionBean));//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
		response.setShowVendorItemNum(sc.getApplicationSession().getAppSessionBean().showWCSSItemNumber());//CAP-37191 to add the vendor flag in the response

		response.setDeliveryOptionsList(populateDeliveryOptionsList(oeOrderSessionBean.getUserSettings(), appSessionBean)); // CAP-48977
		setTBDForEFD(oeShoppingCartFormBean, appSessionBean);//CAP-50471

		return response;
	}

	/**
	 * This method process requested action.
	 *
	 * @param assembler
	 * @param actionID
	 * @param shoppingCartFormBean
	 * @return
	 * @throws AtWinXSException
	 */
	//CAP-35024 Removed modelMap and HTTPRequest.
	// CAP-40882 Resolve sonarqube issues
	public OEShoppingCartFormBean processRemoveItemFromShoppingCart(SessionContainer sc, OEShoppingCartAssembler assembler,
			CORemoveSpecificItemRequest req, String actionID,
			OEShoppingCartFormBean shoppingCartFormBean) throws AtWinXSException {

		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		OrderEntrySession oeSession = ((OrderEntrySession) sc.getModuleSession());
		oeSession.setSessionID(volatileSession.getSessionID());
		// CP-10960 RAR - Do not validate OOF and Dist List.
		boolean doValidate = false; // CAP-36803
		boolean allowZeroQuantity = false;
		boolean preventZeroPriceTransfer = false;
		boolean allowZeroPricePCS = false;

		// change value of passed parameters based on actionID
		// values are based on Desktop version's events: CART_UPDATE, CART_CHECKOUT and
		// EVT_SAVE_ORDER_FROM_CHECKOUT
		// CP-9486 RAR - When UPDATE CART event, validate the items.
		// CAP-30455 Added Remove All Item action in if condition
		if (OrderEntryConstants.REMOVE_ITEM_FROM_CART_ACTION.equals(actionID)
				|| OrderEntryConstants.REMOVE_ALL_ITEM_FROM_CART_ACTION.equals(actionID)) {
			// CAP-40882 Validate lineNumber if exists
			if (shoppingCartFormBean.getCartLines().stream().noneMatch(cartLine -> cartLine.getLineNumber().equals(req.getLineNumber()))) {
				throw new AtWinXSException(actionID, req.getLineNumber());
			}
			// CP-10711 RAR - If there is lineItemNumber passed, means that we are only deleting a single item.
			setIsToBeRemoved(req.getLineNumber(), shoppingCartFormBean.getCartLines());
		}

		// CAP-40882 Removed unused parameter
		CartUpdateLineVO[] invalid = updateShoppingCart(assembler, oeSession, doValidate, req.getUserIPAddress(), shoppingCartFormBean,
				allowZeroQuantity, preventZeroPriceTransfer, allowZeroPricePCS, volatileSession.getVolatileSessionBean(), sc.getApplicationSession().getAppSessionBean()); // CP-11853

		shoppingCartFormBean = loadShoppingCart(sc, invalid, false); // CAP-36803 - do not validate on remove
		shoppingCartFormBean.setItems(sortBundleComponents(shoppingCartFormBean.getItems()), sc.getApplicationSession().getAppSessionBean());//CAP-50143

		// CAP-46340 TH - Fixed issue with remove item for eoo
		// Add code to reset eoo information
		assembler.resetEOOBasedOnCartItems(shoppingCartFormBean.getItems(),  volatileSession.getVolatileSessionBean(),   sc.getApplicationSession().getAppSessionBean(),  oeSession.getOESessionBean(), null); //CP-11183
		oeSession.getOESessionBean().setEOOSelectionCompleted(false);
		SessionHandler.saveSession(oeSession, sc.getApplicationSession().getAppSessionBean().getSessionID(),
				AtWinXSConstant.ORDERS_SERVICE_ID);

		//assembler.checkEOO(request, volatileSession.getVolatileSessionBean(), oeSession.getOESessionBean(), sc.getApplicationSession().getAppSessionBean());


		return shoppingCartFormBean;
	}

	// CAP-40882 Extracted method from processRemoveItemFromShoppingCart
	private void setIsToBeRemoved(String lineItemNumber, List<OEShoppingCartLineFormBean> cartLines) {
		if (!Util.isBlankOrNull(lineItemNumber)) {
			for (KitCartLine cartLine : cartLines) {
				// CP-12958 8.1.6 ACL - Added code to also mark the selected bundle item's components for deletion
				boolean isPartOfBundleMasterItem = false;
				int currentLineNumber = Integer.parseInt(cartLine.getLineNumber());

				if (currentLineNumber > Integer.parseInt(lineItemNumber)
						&& cartLine.getBundleComponentTypeCode().equals("M")) {
					isPartOfBundleMasterItem = false;
				} else if ((currentLineNumber > Integer.parseInt(lineItemNumber)
						&& (cartLine.getBundleComponentTypeCode().equals("C")
								|| cartLine.getComponentType().equals(ComponentType.PromoComponent.toString()))) // CAP-6715
						&& (Util.safeStringToInt(lineItemNumber) == cartLine.getBundleParentOrderLine())) // CP-13559 - make sure part of THIS master item
				{
					isPartOfBundleMasterItem = true;
				}

				// CAP-40882 Simplify code block
				cartLine.setIsToBeRemoved(cartLine.getLineNumber().equals(lineItemNumber) || isPartOfBundleMasterItem);
			}
		}
	}

	// CAP-40882 Remove unused parameter
	public CartUpdateLineVO[] updateShoppingCart(OEShoppingCartAssembler assembler, OrderEntrySession oeSession,
			boolean doValidate, String userIPAddress, OEShoppingCartFormBean shoppingCart,
			boolean allowZeroQuantity, boolean preventZeroPriceTransfer, boolean allowZeroPricePCS,
			VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean) throws AtWinXSException // CP-11853
	{
		// CAP-40882 Immediately return this expression instead of assigning it to a temporary variable
		return  assembler.updateShoppingCart(oeSession.getSessionID(), doValidate, userIPAddress,
				shoppingCart, oeSession.getOESessionBean(), volatileSessionBean, allowZeroQuantity,
				preventZeroPriceTransfer, allowZeroPricePCS, appSessionBean.isCustomPointSite(),
				appSessionBean.isMobileSession(), appSessionBean.isPreSelectAltProfile(), true); // CAP-17512, CAP-36945
	}

//  CAP-35024 - EOO is out of scope.
//	public boolean needResetEOOSetting(OEShoppingCartLineFormBean[] items) {
//		boolean needReset = false;
//		for (OEShoppingCartLineFormBean item : items) {
//			if (item.isToBeRemoved() == true) {
//				needReset = true;
//				break;
//			}
//		}
//		return needReset;
//	}

//  CAP-35024 - EOO is out of scope. Should attrSearchOptList go into the  API response?
//  CP-8788, load dynamic attribute list
//	public void setDynamicAttributeList(HttpServletRequest request, Map<String, Object> modelMap,
//			OrderEntrySession oeSession, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
//			throws AtWinXSException {
//		DynamicItemAttributeVO[] attrSearchOptList = null;
//		if (!oeSession.getOESessionBean().isEOOSelectionCompleted()) {
//			attrSearchOptList = buildAttributeListOptions(oeSession, oeSession.getOESessionBean().getUsrSrchOptions(),
//					(EnforceOrderFormBean) request.getAttribute(OrderEntryConstants.ENFORCE_FORM_BEAN), appSessionBean,
//					volatileSessionBean);
//		}
//
//		modelMap.put("attrSearchOptList", attrSearchOptList);
//
//	}

//  CAP-35024 - Out of scope.
//	public DynamicItemAttributeVO[] buildAttributeListOptions(OrderEntrySession oeSession, List searchOptions,
//			EnforceOrderFormBean bean, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
//			throws AtWinXSException {
//		DynamicItemAttributeVO[] result = null;
//
//		// CP-2216: make sure hasEnforceOnCatalog is set properly
//		boolean hasEnforceOnCatalog = appSessionBean.hasEnforceOnCatalog();
//		HashMap profileAttrs = appSessionBean.getProfileAttributes();
//
//		HashMap selectedSiteAttrValueMap = volatileSessionBean.getSelectedSiteAttribute();
//
//		// CAP-1203 removed reference to OESession's master attributes. That data should
//		// be removed from session, as it is expensive to carry around for large
//		// accounts
////	        HashMap masterAttributes = oeSession.getOESessionBean().getMasterAttributes();
//
//		Set familyMemberAttrSet = volatileSessionBean.getFamilyMemberSiteAttrIDs();
//
//		if (familyMemberAttrSet == null || familyMemberAttrSet.isEmpty()) {
//			OECatalogAssembler catalogAsm = new OECatalogAssembler(appSessionBean.getCustomToken(),
//					appSessionBean.getDefaultLocale());
//
//			Set familyMemberAttrIds = catalogAsm.getHasFamilyItemSearchAttrs(searchOptions, appSessionBean);
//
//			volatileSessionBean.setFamilyMemberSiteAttrIDs(familyMemberAttrIds);
//		}
//
//		HashMap prflBasedAttrValues = oeSession.getOESessionBean().getPrflBasedAttrValues();
//
//		if (bean != null) {
//			List temp = new ArrayList();
//			SiteAttributesVO[] siteAttr = bean.getSiteAttr();
//			HashMap selectedSiteAttrVal = volatileSessionBean.getSelectedSiteAttribute();
//			if (bean.getSelectedSiteAttributeValueMap() != null && bean.getSelectedSiteAttributeValueMap().size() > 0) {
//				selectedSiteAttrVal = bean.getSelectedSiteAttributeValueMap();
//			}
//
//			if (siteAttr != null && siteAttr.length > 0) {
//				Map siteAttrVals = bean.getSiteAttributeValueMap();
//				for (int i = 0; i < siteAttr.length; i++) {
//					if (siteAttrVals != null && !siteAttrVals.isEmpty()) {
//						List attrValList = new ArrayList();
//						// CP-1804 START: prioritize getting from profile based filtered attributes
//						// if at least one attribute is enforced on catalog
//						SiteAttributesVO siteAttrVO = siteAttr[i];
//
//						if (hasEnforceOnCatalog) {
//							// CP-2126 RBA - Added checking prflBasedAttrValues != null before using the
//							// variable
//							if (prflBasedAttrValues != null) {
//								attrValList = (ArrayList) prflBasedAttrValues.get(siteAttrVO);
//							}
//
//							if (attrValList == null || attrValList.size() == 0) {
//								// CP-1722/CP-1763 START: use profile setting if enforce on catalog is on
//								if (searchOptions != null) {
//									for (int j = 0; j < searchOptions.size(); j++) {
//										UserGroupSearchVO ugsVO = (UserGroupSearchVO) searchOptions.get(j);
//										if (siteAttr[i].getAttrID() == ugsVO.getAttrID()) {
//											if (ugsVO.isEnforceOnCatalog() && profileAttrs != null) {
//												attrValList = (ArrayList) profileAttrs.get(siteAttr[i]);
//												break;
//											}
//										}
//									}
//								}
//								// CP-1722/CP-1763 END
//							}
//						}
//
//						if (attrValList == null || attrValList.size() <= 0) {
//							attrValList = (ArrayList) siteAttrVals.get(new Integer(siteAttr[i].getAttrID()));
//						}
//
//						// CP-10858 8.1.2 [VEC] Per onshore discussion, removed the implementation of
//						// filterItemAttributesBasedOnKeyProfiles
//
//						List attValSelected = new ArrayList();
//
//						Iterator it = attrValList.iterator();
//						while (it.hasNext()) {
//							SiteAttrValuesVO attrVal = (SiteAttrValuesVO) it.next();
//
//							SiteAttrValuesVO[] selectedSiteAttributeValues = null;
//							if (selectedSiteAttrVal != null) {
//								selectedSiteAttributeValues = (SiteAttrValuesVO[]) selectedSiteAttrVal
//										.get(new Integer(attrVal.getAttrID()));
//							}
//
//							// CP-11410 , need to check length > 0 to indicate there are selected value
//							if (selectedSiteAttributeValues != null && selectedSiteAttributeValues.length > 0
//									&& selectedSiteAttributeValues[0] != null
//									&& selectedSiteAttributeValues[0].getAttrValID() == attrVal.getAttrValID()) {
//								attValSelected.add(attrVal);
//							}
//						}
//
//						attValSelected = (attValSelected != null && !attValSelected.isEmpty()) ? attValSelected : null;
//						// CP-1743 start
//						boolean hasFamily = (familyMemberAttrSet != null && !familyMemberAttrSet.isEmpty())
//								? familyMemberAttrSet.contains(new Integer(siteAttr[i].getAttrID()))
//								: false;
//						temp.add(new DynamicItemAttributeVO(siteAttr[i], attrValList, attValSelected, hasFamily));
//						// CP-1743 end
//					}
//				}
//			}
//
//			result = (DynamicItemAttributeVO[]) temp.toArray(new DynamicItemAttributeVO[temp.size()]);
//
//		}
//
//		return result;
//	}

	//CAP-35024 - Bundles out of scope.
	// CP-12958 RAR
	/**
	 * Method applyBundleComponentRules()
	 *
	 * This method will apply the {@link BundleRules} specified for each of the
	 * {@link BundleComponent}.
	 *
	 * @param formBean
	 * @param allowPrintOverride
	 * @throws AtWinXSException
	 */
//	public void applyBundleComponentRules(OEShoppingCartFormBean formBean, boolean allowPrintOverride)
//			throws AtWinXSException// cap-6234 change to protected
//	{
//		boolean hasBundle = checkIfOrderContainsBundle(formBean);
//
//		if (hasBundle) {
//			Collection<OEShoppingCartLineFormBean> masterBundleItems = getMasterBundleItems(formBean);
//
//			if (null != masterBundleItems) {
//				for (OEShoppingCartLineFormBean bundleItem : masterBundleItems) {
//					CustomDocumentOrderLine custDocOrdLn = ObjectMapFactory.getEntityObjectMap()
//							.getEntity(CustomDocumentOrderLine.class, appSessionBean.getCustomToken());
//					custDocOrdLn.populate(Util.safeStringToInt(bundleItem.getCustDocLineId()));
//
//					Bundle bundle = ObjectMapFactory.getEntityObjectMap().getEntity(Bundle.class,
//							appSessionBean.getCustomToken());
//					bundle.populateBundleByCustItemNr(bundleItem.getItemNumber(), appSessionBean.getSiteID(),
//							appSessionBean); // CAP-1494
//
//					BundleComponents bundleComponents = bundle.getComponents();
//
//					if (null != bundleComponents && (null != bundleComponents.getBundleComponents()
//							&& !bundleComponents.getBundleComponents().isEmpty())) {
//						for (BundleComponent component : bundleComponents.getBundleComponents()) {
//							for (OEShoppingCartLineFormBean item : formBean.getItems()) {
//								if (component.getWalVendorRrdItem().equals(item.getVendorItemNumber()) && bundleItem
//										.getLineNumber().equals(String.valueOf(item.getBundleParentOrderLine()))) {
//									// CP-13524 RAR - Passed siteID and itemNumber
//									BundleDeliveryOptions bundleDeliveryOptions = component.evaluateDeliveryOptions(
//											custDocOrdLn.getVariableDataXmlTxt(), appSessionBean.getSiteID(),
//											item.getItemNumber());
//									item.setBundleDeliveryOptionsCode(bundleDeliveryOptions);
//
//									// CP-13524 RAR
//									if (null != bundleDeliveryOptions
//											&& bundleDeliveryOptions.isIseDeliveryConfigured()) {
//										updateEFDChargesBasedOnBundleRules(item, bundleDeliveryOptions,
//												allowPrintOverride);
//									}
//
//									break;
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}

	//CAP-35024 - Out of scope.
//	public void validateSubscription(OEShoppingCartFormBean formBean, OEOrderSessionBean oeSessionBean)
//			throws AtWinXSException {
//		boolean isSubscription = oeSessionBean
//				.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_SUBSCRIPTION_ONLY;
//
//		if (isSubscription) {
//			OEShoppingCartLineFormBean[] items = formBean.getItems();
//
//			for (int i = 0; i < items.length; i++) {
//				IOECampaignComponent campaign = OECampaignComponentLocator.locate(appSessionBean.getCustomToken());
//				boolean isUserSubscribed = campaign.isUserSubscribed(appSessionBean.getSiteID(),
//						appSessionBean.getLoginID(), appSessionBean.getProfileNumber(), items[i].getItemNumber());
//
//				if (isUserSubscribed) {
//					formBean.setExpiredDataItemCount(formBean.getExpiredDataItemCount() + 1);
//					items[i].setErrorMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
//							appSessionBean.getCustomToken(), "alreadySubscribedMsg"));
//				}
//			}
//		}
//	}

	//CAP-35024 - Out of scope.
//	public boolean checkIfOrderContainsBundle(OEShoppingCartFormBean formBean) {
//		for (OEShoppingCartLineFormBean item : formBean.getItems()) {
//			if (!Util.isBlankOrNull(item.getBundleComponentTypeCode())) {
//				return true;
//			}
//		}
//
//		return false;
//	}

	//CAP-35024 - Out of scope.
	// CP-12958 RAR
	/**
	 * Method getBundleItem()
	 *
	 * This method will get the Bundle Master.
	 *
	 * @param formBean
	 * @return Collection<OEShoppingCartLineFormBean>
	 */
//	public Collection<OEShoppingCartLineFormBean> getMasterBundleItems(OEShoppingCartFormBean formBean) {
//		Collection<OEShoppingCartLineFormBean> bundleItem = null;
//
//		for (OEShoppingCartLineFormBean item : formBean.getItems()) {
//			if (item.getBundleComponentTypeCode().equals("M")) {
//				if (null == bundleItem) {
//					bundleItem = new ArrayList<OEShoppingCartLineFormBean>();
//				}
//
//				bundleItem.add(item);
//			}
//		}
//
//		return bundleItem;
//	}

	//CAP-35024 - Out of scope.
//	public void updateEFDChargesBasedOnBundleRules(OEShoppingCartLineFormBean item,
//			BundleDeliveryOptions bundleDeliveryOptions, boolean allowPrintOverride) throws AtWinXSException {
//		boolean hasNoValidOption = (!bundleDeliveryOptions.isPrintEnabled()
//				&& !bundleDeliveryOptions.isIseDeliveryEnabled())
//				|| !bundleDeliveryOptions.isIseDeliveryEnabled() && !allowPrintOverride;
//
//		if (null == bundleDeliveryOptions || hasNoValidOption) {
//			return;
//		}
//
//		Map<String, XSCurrency> allEFDCharges = getAllItemCharges(item.getItemNumber(), item.getVendorItemNumber());
//
//		if (null != allEFDCharges && !allEFDCharges.isEmpty()) {
//			Collection<String> efdChargesToRemove = new ArrayList<String>();
//			Collection<String> efdChargesToAdd = new ArrayList<String>();
//			double totalEfdChargeToSubtract = 0;
//			double totalEfdChargeToAdd = 0;
//
//			for (String serviceChargeCode : allEFDCharges.keySet()) {
//				if (serviceChargeCode.endsWith(CatalogConstant.SERVICE_CHARGE_CODE_PDF)) {
//					if (null != bundleDeliveryOptions.isShowPDF() && !bundleDeliveryOptions.isShowPDF()) {
//						// CP-13558 RAR - Added null check.
//						if (null != item.getEfdCharges() && item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToSubtract += item.getEfdCharges().get(serviceChargeCode).getAmountValue();
//							efdChargesToRemove.add(serviceChargeCode);
//						}
//					} else {
//						// CP-13558 RAR - Added null check.
//						if (null == item.getEfdCharges()) {
//							item.setEfdCharges(new HashMap<String, XSCurrency>());
//						}
//
//						if (!item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToAdd += allEFDCharges.get(serviceChargeCode).getAmountValue();
//							efdChargesToAdd.add(serviceChargeCode);
//						}
//					}
//				}
//
//				if (serviceChargeCode.endsWith(CatalogConstant.SERVICE_CHARGE_CODE_DIGIMAG)) {
//					if (null != bundleDeliveryOptions.isShowDigimag() && !bundleDeliveryOptions.isShowDigimag()) {
//						// CP-13558 RAR - Added null check.
//						if (null != item.getEfdCharges() && item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToSubtract += item.getEfdCharges().get(serviceChargeCode).getAmountValue();
//							efdChargesToRemove.add(serviceChargeCode);
//						}
//					} else {
//						// CP-13558 RAR - Added null check.
//						if (null == item.getEfdCharges()) {
//							item.setEfdCharges(new HashMap<String, XSCurrency>());
//						}
//
//						if (!item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToAdd += allEFDCharges.get(serviceChargeCode).getAmountValue();
//							efdChargesToAdd.add(serviceChargeCode);
//						}
//
//					}
//				}
//
//				if (serviceChargeCode.endsWith(CatalogConstant.SERVICE_CHARGE_CODE_FTP)) {
//					if (null != bundleDeliveryOptions.isShowFTP() && !bundleDeliveryOptions.isShowFTP()) {
//						// CP-13558 RAR - Added null check.
//						if (null != item.getEfdCharges() && item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToSubtract += item.getEfdCharges().get(serviceChargeCode).getAmountValue();
//							efdChargesToRemove.add(serviceChargeCode);
//						}
//					} else {
//						// CP-13558 RAR - Added null check.
//						if (null == item.getEfdCharges()) {
//							item.setEfdCharges(new HashMap<String, XSCurrency>());
//						}
//
//						if (!item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToAdd += allEFDCharges.get(serviceChargeCode).getAmountValue();
//							efdChargesToAdd.add(serviceChargeCode);
//						}
//					}
//				}
//
//				if (serviceChargeCode.endsWith(CatalogConstant.SERVICE_CHARGE_CODE_STATIC)) {
//					if (null != bundleDeliveryOptions.isShowStatic() && !bundleDeliveryOptions.isShowStatic()) {
//						// CP-13558 RAR - Added null check.
//						if (null != item.getEfdCharges() && item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToSubtract += item.getEfdCharges().get(serviceChargeCode).getAmountValue();
//							efdChargesToRemove.add(serviceChargeCode);
//						}
//					} else {
//						// CP-13558 RAR - Added null check.
//						if (null == item.getEfdCharges()) {
//							item.setEfdCharges(new HashMap<String, XSCurrency>());
//						}
//
//						if (!item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToAdd += allEFDCharges.get(serviceChargeCode).getAmountValue();
//							efdChargesToAdd.add(serviceChargeCode);
//						}
//					}
//				}
//
//				if (serviceChargeCode.endsWith(CatalogConstant.SERVICE_CHARGE_CODE_EDOC)) {
//					if (null != bundleDeliveryOptions.isShowEdoc() && !bundleDeliveryOptions.isShowEdoc()) {
//						// CP-13558 RAR - Added null check.
//						if (null != item.getEfdCharges() && item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToSubtract += item.getEfdCharges().get(serviceChargeCode).getAmountValue();
//							efdChargesToRemove.add(serviceChargeCode);
//						}
//					} else {
//						// CP-13558 RAR - Added null check.
//						if (null == item.getEfdCharges()) {
//							item.setEfdCharges(new HashMap<String, XSCurrency>());
//						}
//
//						if (!item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToAdd += allEFDCharges.get(serviceChargeCode).getAmountValue();
//							efdChargesToAdd.add(serviceChargeCode);
//						}
//					}
//				}
//
//				if (serviceChargeCode.endsWith(CatalogConstant.SERVICE_CHARGE_CODE_JELLY_VISION)) {
//					if (null != bundleDeliveryOptions.isShowJellyvision()
//							&& !bundleDeliveryOptions.isShowJellyvision()) {
//						// CP-13558 RAR - Added null check.
//						if (null != item.getEfdCharges() && item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToSubtract += item.getEfdCharges().get(serviceChargeCode).getAmountValue();
//							efdChargesToRemove.add(serviceChargeCode);
//						}
//					} else {
//						// CP-13558 RAR - Added null check.
//						if (null == item.getEfdCharges()) {
//							item.setEfdCharges(new HashMap<String, XSCurrency>());
//						}
//
//						if (!item.getEfdCharges().containsKey(serviceChargeCode)) {
//							totalEfdChargeToAdd += allEFDCharges.get(serviceChargeCode).getAmountValue();
//							efdChargesToAdd.add(serviceChargeCode);
//						}
//					}
//				}
//			}
//
//			if (!efdChargesToRemove.isEmpty()) {
//				item.setEfdCharge(item.getEfdCharge() - totalEfdChargeToSubtract);
//
//				for (String key : efdChargesToRemove) {
//					item.getEfdCharges().remove(key);
//				}
//			}
//
//			if (!efdChargesToAdd.isEmpty()) {
//				item.setEfdCharge(item.getEfdCharge() + totalEfdChargeToAdd);
//
//				for (String serviceChargeCode : allEFDCharges.keySet()) {
//					for (String serviceChargeCodeToAdd : efdChargesToAdd) {
//						if (serviceChargeCodeToAdd.equals(serviceChargeCode)) {
//							item.getEfdCharges().put(serviceChargeCode, allEFDCharges.get(serviceChargeCode));
//						}
//					}
//				}
//			}
//		}
//	}

	//CAP-35024 - Out of scope.
	// CP-13524 RAR
	/**
	 * Method getAllItemCharges()
	 *
	 * This method will get all the EFD Charges for the item.
	 *
	 * @param customerItemNr
	 * @param vendorItemNr
	 * @return Map<String, XSCurrency>
	 * @throws AtWinXSException
	 */
//	public Map<String, XSCurrency> getAllItemCharges(String customerItemNr, String vendorItemNr)
//			throws AtWinXSException {
//		Map<String, XSCurrency> efdCharges = new HashMap<String, XSCurrency>();
//		Map<String, String> efdSrcText = initEFDSrcCodesMap();
//
//		SiteServiceChargesCompositeVO[] efdSiteServiceCharges = getAllEFDItemCharges(customerItemNr, vendorItemNr);
//
//		if (efdSiteServiceCharges != null) {
//			for (SiteServiceChargesCompositeVO efdSiteServiceCharge : efdSiteServiceCharges) {
//				// CP-13524 RAR - Make sure that the Service Charge is assigned to the item.
//				boolean isServiceAssigned = isServiceAssigned(
//						EFDSourceCode.getValue(efdSiteServiceCharge.getServiceChargeCode()), customerItemNr,
//						vendorItemNr);
//				if (efdSiteServiceCharge.getServiceChargeAmount() > 0
//						&& efdSrcText.containsKey(efdSiteServiceCharge.getServiceChargeCode()) && isServiceAssigned) {
//					efdCharges.put(efdSrcText.get(efdSiteServiceCharge.getServiceChargeCode()),
//							Util.getStringFromCurrency(efdSiteServiceCharge.getServiceChargeAmount(),
//									appSessionBean.getDefaultLocale(), false));
//				}
//			}
//		}
//
//		return efdCharges;
//	}

	//CAP-35024 - Out of scope.
//	public boolean isServiceAssigned(String code, String customerItemNr, String vendorItemNr) throws AtWinXSException {
//		ICatalog iCatalog = CMCatalogComponentLocator.locate(appSessionBean.getCustomToken());
//		Collection<EFDDefaultSettingsCompositeVO> efdDefaultSettings = iCatalog
//				.getCatalogDefaultEFDSettings(new EFDDefaultSettingsVOKey(appSessionBean.getSiteID(), vendorItemNr,
//						customerItemNr, AtWinXSConstant.INVALID_ID));
//
//		for (EFDDefaultSettingsCompositeVO efdDefaultSetting : efdDefaultSettings) {
//			if (code.equals(efdDefaultSetting.getSourceCode())) {
//				return true;
//			}
//		}
//
//		return false;
//	}

	//CAP-35024 - Out of scope.
	// CP-13524 RAR
	/**
	 * Method getAllEFDItemCharges()
	 *
	 * This method will get all the EFD Charges for the item.
	 *
	 * @param customerItemNr
	 * @param vendorItemNr
	 * @return
	 * @throws AtWinXSException
	 */
//	public SiteServiceChargesCompositeVO[] getAllEFDItemCharges(String customerItemNr, String vendorItemNr)
//			throws AtWinXSException {
//		IServiceInterface admin = ServiceComponentLocator.locate(appSessionBean.getCustomToken());
//
//		SiteVOKey siteVOKey = new SiteVOKey(appSessionBean.getSiteID());
//
//		Collection<SiteServiceChargesCompositeVO> availableServiceCharges = null;
//		availableServiceCharges = admin.retrieveServiceCharges(siteVOKey);
//
//		SiteServiceChargesCompositeVO[] serviceChargeVOs = null;
//
//		List<SiteServiceChargesVO> availableServiceChargesList = new ArrayList();
//
//		for (SiteServiceChargesCompositeVO asc : availableServiceCharges) {
//			Object serviceCharge = null;
//			Collection<SiteServiceChargesVO> serviceChargeVO = null;
//
//			serviceCharge = ObjectFactory.getObject(asc.getServiceChargeImpl());
//
//			if (serviceCharge instanceof EFDItemServiceCharge) {
//				serviceChargeVO = ((EFDItemServiceCharge) serviceCharge).getItemServiceCharge(asc, customerItemNr,
//						vendorItemNr, "", null, null, -1, -1, true);
//
//				if (serviceChargeVO != null) {
//					for (SiteServiceChargesVO ssc : serviceChargeVO) {
//						availableServiceChargesList.add(ssc);
//					}
//				}
//			}
//		}
//
//		if (availableServiceChargesList != null && availableServiceChargesList.size() > 0) {
//			serviceChargeVOs = (SiteServiceChargesCompositeVO[]) availableServiceChargesList
//					.toArray(new SiteServiceChargesCompositeVO[] {});
//		}
//
//		return serviceChargeVOs;
//	}

	//CAP-35024 - Out of scope.
	// CP-13524 RAR
//	public Map<String, String> initEFDSrcCodesMap() {
//		Map<String, String> efdSrcCodes = new HashMap<String, String>();
//		efdSrcCodes.put("EDP", "efd_charge_cart_lbl_EDP");
//		efdSrcCodes.put("EDD", "efd_charge_cart_lbl_EDD");
//		efdSrcCodes.put("EDF", "efd_charge_cart_lbl_EDF");
//		efdSrcCodes.put("EDS", "efd_charge_cart_lbl_EDS");
//		efdSrcCodes.put("EDE", "efd_charge_cart_lbl_EDE");
//		efdSrcCodes.put("EDJ", "efd_charge_cart_lbl_EDJ");
//		return efdSrcCodes;
//	}

	//CAP-35024 - Out of scope.
	// CP-13524 RAR
//	public enum EFDSourceCode {
//		EDP("EDP", "PDF"), EDD("EDD", "DIG"), EDF("EDF", "FTP"), EDS("EDS", "STC"), EDE("EDE", "EDC"),
//		EDJ("EDJ", "JEL");
//
//		private String efdSource;
//		private String name;
//
//		private EFDSourceCode(String name, String efdSource) {
//			this.name = name;
//			this.efdSource = efdSource;
//		}
//
//		private static String getValue(String code) {
//			for (EFDSourceCode currCode : EFDSourceCode.values()) {
//				if (currCode.name.toString().equals(code)) {
//					return currCode.getValue();
//				}
//			}
//
//			return code;
//		}
//
//		private String getValue() {
//			return efdSource;
//		}
//
//		private String getName() {
//			return name;
//		}
//
//		public String toString() {
//			return getValue();
//		}
//	}

	/**
	 * Method persistInSession()
	 *
	 * This method will persist the changes in the session.
	 *
	 * @param ttSession
	 * @param applicationVolatileSession
	 * @throws AtWinXSException
	 * @throws CPRPCException
	 */
	private void persistInSession(ApplicationVolatileSession applicationVolatileSession) throws AtWinXSException // CAP-3449
	{
		applicationVolatileSession.setIsDirty(true);
		SessionHandler.saveSession(applicationVolatileSession, applicationVolatileSession.getSessionID(),
				AtWinXSConstant.APPVOLATILESESSIONID);
	}

	//CAP-35024 - Copied from ShoppingCartBaseController
	/**
	 * Method recalculateOrderScenarioNumber()
	 *
	 * This method will recalculate the Order Scenario Number and set it to @link{OEOrderSessionBean}.
	 *
	 * @param oeSessionBean
	 * @param orderSentToDistributionList
	 * @throws AtWinXSException
	 */
	protected void recalculateOrderScenarioNumber(OEOrderSessionBean oeSessionBean, boolean orderSentToDistributionList, boolean shipToMultipleAddresses,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException //CAP-29105//CAP-6234 change to protected
	{
		// CAP-40882 Wrap in service for easier unit testing
		IOEShoppingCartComponent oeShoppingCartComp = oeShoppingCartComponentLocatorService.locate(appSessionBean.getCustomToken());
		OrderHeaderVO orderHeaderVO = new OrderHeaderVO(volatileSessionBean.getOrderId());
		int scenarioNumber = oeShoppingCartComp.getOrderScenarioNumber(orderHeaderVO, orderSentToDistributionList, shipToMultipleAddresses, null);  //CAP-29105
		oeSessionBean.setOrderScenarioNumber(scenarioNumber);
	}

	// CAP-36874 - moved here to reduce complexity
	public ArrayList<OEShoppingCartLineFormBean> updateCartFromRequest(OEShoppingCartFormBean shoppingCartFormBean,
			COShoppingCartRequest scRequest) {
		//CAP-35559 Update items attribute with qty, factor, uom transfer to OEShoppingCartLineFormBean
		ArrayList<OEShoppingCartLineFormBean> itemBeans = new ArrayList<OEShoppingCartLineFormBean>();
		String selectedUOM = null;
		String uomCode = null;
		String uomFactor = null;
		for(COShoppingCartLineFormBean coLineitem : scRequest.getCoLineItems())
		{
			selectedUOM = coLineitem.getUomCode(); // CAP-36874 - parse full UOM_UOMF
			for(OEShoppingCartLineFormBean cpLineItem : shoppingCartFormBean.getItems())
			{
				if(cpLineItem.getLineNumber().equals(coLineitem.getLineNumber()))
				{
					cpLineItem.setItemQuantity(coLineitem.getItemQuantity());
					// CAP-36874 - parse UOM_UOMF if passed
					if ((!Util.isBlankOrNull(selectedUOM)) && (selectedUOM.indexOf("_") > 0))
					{
						uomCode = selectedUOM.substring(0, selectedUOM.indexOf("_"));
						uomFactor = selectedUOM.substring((selectedUOM.indexOf("_")+1), selectedUOM.length());
					}
					else
					{
						uomCode = OrderEntryConstants.DEFAULT_UOM_CODE;
						uomFactor = DEFAULT_UOMF_STRING;
					}
					cpLineItem.setUOMFactor(uomFactor);
					cpLineItem.setUOMCode(uomCode);
					cpLineItem.setErrorMessage(AtWinXSConstant.EMPTY_STRING); // CAP-38253 - reset errors on lines with updated quantities
					
					callValidationToSetDeliveryOption(coLineitem.getSelectedDeliveryOption(),cpLineItem);//CAP-49728
					itemBeans.add(cpLineItem);
					break;
				}
			}
		}
		return itemBeans;
	}
	
	/**
 	 *
 	 * @param mainSession - {@link SessionContainer}
 	 * @param request -  {@link COShoppingCartRequest}
 	 * @return - This will return COShoppingCartResponse with updated items error, if any withstatus
 	 * 				{@link COShoppingCartResponse}
 	 * @throws AtWinXSException
	 */
	//CAP-35025   Method logic copied from CustomPointWeb handleSaveOrder.doAction()
	@SuppressWarnings("static-access")
	public COShoppingCartResponse processUpdateShoppingCart(SessionContainer mainSession,
			COShoppingCartRequest scRequest)
			throws AtWinXSException
	{
		final Logger logger = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

		AppSessionBean appSessionBean = mainSession.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = mainSession.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = mainSession.getApplicationSession().getPunchoutSessionBean();

		COShoppingCartResponse shoppingCartResponse = new COShoppingCartResponse();

		//CAP-35025 - Start ShoppingCartBaseController->prepopulateCommandObject -
		//            This method prepares the command object for the Shopping Card.
		volatileSessionBean.setServiceID(AtWinXSConstant.ORDERS_SERVICE_ID); // CP-12549
		OEShoppingCartAssembler assembler =
			OEAssemblerFactory.getShoppingCartAssembler(appSessionBean.getCustomToken(),
														appSessionBean.getDefaultLocale(),
														appSessionBean.getApplyExchangeRate());

		/*
		 * safe to set validateItems to false (for cart load)
		 * because even if this method is called when the form submits,
		 * that will be handled by handleSubmit()->processShoppingCart()->loadShoppingCart()
		 */
		boolean validateItems = false;
		CartUpdateLineVO[] cartLinesWithErrors = null;
		OrderEntrySession oeSession = ((OrderEntrySession) mainSession.getModuleSession());

		OEShoppingCartFormBean shoppingCartFormBean = loadShoppingCart(mainSession, cartLinesWithErrors, validateItems);

		// CAP-36874 - do the update in other method
		ArrayList<OEShoppingCartLineFormBean> itemBeans = updateCartFromRequest(shoppingCartFormBean, scRequest);
		
		//CAP-35559 Convert it to Array and pass it in the cart OEShoppingCartFormBean
		OEShoppingCartLineFormBean[] convertedItemBeans = new OEShoppingCartLineFormBean[itemBeans.size()];
		convertedItemBeans = (OEShoppingCartLineFormBean[])itemBeans.toArray(convertedItemBeans);
		//CAP-38173 Removed duplicate code

		//CAP-35559 - formBean set to convertedItemBeans for further validation and update
		shoppingCartFormBean.setItems(convertedItemBeans, appSessionBean); //CAP-38173

		/* //CAP-35025 - Start comment
		String returnPage = updateForPunchout(formBean, request); // CP-8971
		if (!Util.isBlankOrNull(returnPage))
		{
			formBean = (OEShoppingCartFormBean) request.getAttribute(FORM_NAME);
			if (returnPage.indexOf("shoppingcart") == -1)
			{ // if this needs us to redirect somewhere else, then do it
				ModelAndView nextView = new ModelAndView(new RedirectView(returnPage), new HashMap<String, Object>());
				try
				{
					SessionHandler.saveSession(getModuleSession(), appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
				}
				catch(AtWinXSException ae)
				{
					logger.error("Failed in ShoppingCartController " + ae.getMessage(), ae); // CAP-16459
				}
				CPController.persistMessageForViewException(nextView, appVolatileSession);
				throw new ModelAndViewDefiningException(nextView);
			}
		}
		//CP-9303 JRA - START

		if(!OrderEntryConstants.UPDATE_CART_ACTION.equals(formBean.getFormAction()) && !OrderEntryConstants.CHECKOUT_ORDER_ACTION.equals(formBean.getFormAction())
				&& formBean.isBackorderedItems())
		{
			OEShoppingCartLineFormBean[] lines = formBean.getItems();

			for(OEShoppingCartLineFormBean line : lines)
			{
				boolean mailMergeLine = (ICustomDocsAdminConstants.TMPLT_UI_MERGE_OPT_CD_M.equals(line.getMergeCode()) ||
						 ItemConstants.ITEM_CLASS_CAMPAIGN.equals(line.getItemClassCode()));

				//CP-10087 RAR - Clear the Line Error Message ONLY IF it is a Back Order and NOT Custom Doc.
				if(line.isBackordered() && !((OrderEntryConstants.EFD_METHOD.equals(line.getFileDeliveryMethod()))
						|| (formBean.isMailMergeOnly()) || (mailMergeLine)))
				{
					line.setErrorMessage("");
				}
			}

			formBean.setItems(lines);
			formBean.setBackorderedItems(false);
		}
		//CP-9303 JRA - END
		*/ //CAP-35025 - End comment


		//CAP-35559 Out of scope.
		// CP-8858 ACL Validate if Split Order checkout option should be displayed
		//formBean = checkSplitDisplay(formBean);

		/* //CAP-35025 - Start comment
		//this method doesn't automatically set the command object in the request so set it manually
		request.setAttribute(FORM_NAME, formBean);

		updateForPunchout(formBean, request); // CP-8971
		*/ //CAP-35025 - End comment
		//CAP-35025 - End ShoppingCartBaseController->prepopulateCommandObject


		//CAP-35025 - Start ShoppingCartBaseController->handleFormSubmission

		OEShoppingCartFormBean shoppingCart = shoppingCartFormBean;

		//CAP-35559 - set Formaction for update shopingcart
		shoppingCart.setFormAction("UPDATE_CART_ACTION");

		String actionID = shoppingCart.getFormAction();

		//CP-11500 RAR - If in Order From File, then do not recalculate the Order Scenario Number since the user is supposed to be locked
		//by this point onwards.
		if(oeSession.getOESessionBean().getOrderScenarioNumber() != OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE)
		{
			//CP-10960 RAR - Now that we can have a Dist List with Item and Quantities option in the cart,
			//we need to make sure that we have the correct Order Scenario Number on form submit.
			recalculateOrderScenarioNumber(oeSession.getOESessionBean(), shoppingCart.isSendToDistListUsingQtyFromFile(), shoppingCart.isLineItemShipping(), appSessionBean, volatileSessionBean); //CAP-29105
		}

		//CP-9573, CP-9170, when no distribution list assigned, we need to set the flag
		OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

		shoppingCart.setSendToDistributionList(checkoutAssembler.isDistributionListOrder(oeSession.getOESessionBean().getOrderScenarioNumber()));

		boolean isUpdateOrCheckout = OrderEntryConstants.UPDATE_CART_ACTION.equals(actionID) ||
		OrderEntryConstants.CHECKOUT_ORDER_ACTION.equals(actionID) || OrderEntryConstants.SHOW_EOO_ACTION.equals(actionID);
		Locale defaultLocale = appSessionBean.getDefaultLocale();

		// CAP-2469
		volatileSessionBean.setOrderLinesUOMQtyMap(null);
		CartUpdateLineVO[] invalid = null;
		boolean validateFeed = false;

		if (isUpdateOrCheckout)
		{
			shoppingCart = removeSelectedForDelete(shoppingCart);
			//CP-9362 Returns true if error was found for an item.
			//CP-8970 changed token from String to an Object
			//CAP-11577
			//CAP-33184
			ShoppingCartValidator scValidator = new ShoppingCartValidator(); //CAP-35025 : one line comment// getShoppingCartValidator(request);
			boolean isError = scValidator.validateCartItemsCp(shoppingCart, defaultLocale, appSessionBean.getCustomToken(), oeSession.getOESessionBean().getOrderScenarioNumber(),
					appSessionBean.isNewLookNFeel(), punchoutSessionBean, appSessionBean.getCurrencyLocale(), oeSession.getOESessionBean().getUserSettings().isAllowRMS()); //CP-10008 PTB need to pass new look and feel boolean // CP-8971

			if(isError)
			{
				//CP-9486 RAR - On error, reload the shoppingCart to make sure that pricing will be updated
				//when the UOM or Qty was set to invalid value.
				cartLinesWithErrors = reloadShoppingCartFormBean(true, scRequest, shoppingCart, appSessionBean, volatileSessionBean, oeSession);
				shoppingCart = loadShoppingCart(mainSession, cartLinesWithErrors, true);

				//CP-9653 RAR - If going back to the Cart, then make sure that we display the "Save to New Order" when needed to display.
				//CAP-35559 Out of Scope.
				//shoppingCart = checkSplitDisplay(shoppingCart);
				//populateEFDMessage(shoppingCart, oeSession.getOESessionBean().getOrderScenarioNumber());
				//CP-4616 added parameter to indicate whether to load short messages or not
				//CPValidator.handleBindException(msg, errors, true);

				//CAP-36164 - Validation Error Message - UOM error message fix.
				  scValidator.validateCartItemsCp(shoppingCart, defaultLocale, appSessionBean.getCustomToken(), oeSession.getOESessionBean().getOrderScenarioNumber(),
						appSessionBean.isNewLookNFeel(), punchoutSessionBean, appSessionBean.getCurrencyLocale(), oeSession.getOESessionBean().getUserSettings().isAllowRMS()); //CP-10008 PTB need to pass new look and feel boolean // CP-8971

			}
			else
			{
				//CP-10960 RAR - Set the session variable that determines if we are doing Dist List with Item and Quantities.
				oeSession.getOESessionBean().setSendToDistListUsingQtyFromFile(shoppingCart.isSendToDistListUsingQtyFromFile());
				//CAP-29752 - Merrill - Multi-ship Addresses Checkbox on Shopping Cart - Modify Addresses
				oeSession.getOESessionBean().setLineItemShipping(shoppingCart.isLineItemShipping()); //CAP-29105
				//CP-11056 RAR - Save Profle Selections when skipping the Order Selections popup.
				if(OrderEntryConstants.CHECKOUT_ORDER_ACTION.equals(actionID))
				{
					ProfileSelection profileSelectionObj = oeSession.getOESessionBean().getProfileSelections();

					if(null != profileSelectionObj && null != profileSelectionObj.getProfileSelections() && !profileSelectionObj.getProfileSelections().isEmpty()) //CAP-21613
					{
						profileSelectionObj.saveAll(volatileSessionBean.getOrderId(), appSessionBean.getSiteID(), appSessionBean.getBuID());
					}
				}

				//CP-10960 RAR - Do not validate OOF and Dist List.
				boolean doValidate = !shoppingCartFormBean.isOrderFromFile() && !shoppingCartFormBean.isSendToDistListUsingQtyFromFile();
				boolean allowZeroQuantity = false;//CAP-35531 ValidateItemQuantity - Error Translation tag to be replace with Actual Message
				boolean preventZeroPriceTransfer = false;
				boolean allowZeroPricePCS = false;
				validateItems = true;

				// CAP-40882 Remove unused parameter
				invalid = updateShoppingCart(assembler, oeSession, doValidate, scRequest.getUserIPAddress(), shoppingCartFormBean,
						allowZeroQuantity, preventZeroPriceTransfer, allowZeroPricePCS, volatileSessionBean, appSessionBean);

				//CAP-35559 Out of scope.
//				if (appSessionBean.hasEnforceOnOrdering() && needResetEOOSetting(shoppingCartFormBean.getItems()))
//				{
//					//CP-8788 if the cart is changing, we need to reset the enforce on ordering based on cart
//					assembler.resetEOOBasedOnCartItems(shoppingCartFormBean.getItems(),  volatileSessionBean,   appSessionBean,  oeSession.getOESessionBean(), null); //CP-11183
//					assembler.checkEOO(request, volatileSessionBean, oeSession.getOESessionBean(), appSessionBean);
//					setDynamicAttributeList(request, modelMap);
//					modelMap.put("eOOSelectionCompleted", oeSession.getOESessionBean().isEOOSelectionCompleted());
//
//				}

				// CAP-12108 Moved code after resetting the EOO process to correctly know if order is a batch order
				//CAP-35025 : one line comment: duplicate declaration // OEShoppingCartFormBean
				shoppingCart = loadShoppingCart(mainSession, invalid, validateItems);

				// CAP-47795 - validate budget allocation
				if (scRequest.isCheckBudgetWarning()) {
					validateBudgetAllocation(mainSession, assembler, shoppingCart);
				}

				if ((scRequest.isBackOrderWarned()) && (shoppingCart.isBackorderedItems()))
				{
					shoppingCart.setBackordersWarned(true);
				}

				//this method doesn't automatically set the command object in the request so set it manually


				//CAP-35025 : one line comment// return allItemsValid;


				//CAP-35025 - End ShoppingCartBaseController->processShoppingCart()

			}
		}
		// CAP-36437 - move out of if block
		if ((scRequest.isBackOrderWarned()) && (shoppingCart.isBackorderedItems()))
		{
			shoppingCart.setBackordersWarned(true);
		}

		boolean allItemsValid = putShoppingCartInRequest(invalid,
				validateItems,
				validateFeed,
				shoppingCart,
				false,
				actionID,
				mainSession);

		//CAP-35025 - End ShoppingCartBaseController->handleFormSubmission

		//OEResolvedUserSettingsSessionBean userSettings = getOrderSessionBean().getUserSettings();

		//CAP-35025 : one line comment: duplicate declaration  //OEShoppingCartAssembler
		assembler =
			OEAssemblerFactory.getShoppingCartAssembler(appSessionBean.getCustomToken(),
														appSessionBean.getDefaultLocale(),
														appSessionBean.getApplyExchangeRate());

		//CAP-35559 Out of scope.
		//boolean isSplit = OrderEntryConstants.EVT_SPLIT_ORDER_FROM_CART.equals(actionID);

		//Collection<String> lineNumbers = new ArrayList<String>();
		//CAP-31318
	//	oeSession.getOESessionBean().setLineItemShipping(shoppingCartFormBean.isLineItemShipping());

		//CAP-35559 Out of scope.
//		if(isSplit)
//		{
//			// CP-8858 ACL Get all the selected items
//			ArrayList<OEShoppingCartLineFormBean> itemBeans = new ArrayList<OEShoppingCartLineFormBean>();
//
//			for(OEShoppingCartLineFormBean lineItem : shoppingCartFormBean.getItems())
//			{
//				if(lineItem.isToBeRemoved())
//				{
//					lineNumbers.add(lineItem.getLineNumber());
//				}
//
//				// Selected items should not be set as removed
//				lineItem.setIsToBeRemoved(false);
//				itemBeans.add(lineItem);
//			}
//
//			// Convert it to Array and pass it in the cart OEShoppingCartFormBean
//			OEShoppingCartLineFormBean[] convertedItemBeans = new OEShoppingCartLineFormBean[itemBeans.size()];
//			convertedItemBeans = (OEShoppingCartLineFormBean[])itemBeans.toArray(convertedItemBeans);
//			shoppingCartFormBean.setItems(convertedItemBeans);
//
//			// convert line numbers array lsit into array
//			String[] lineItemNumbers = new String[lineNumbers.size()];
//			lineItemNumbers = (String[])lineNumbers.toArray(lineItemNumbers);
//
//			//CAP-35025 : one line comment// modelMap.put("lineItemNumbers", lineItemNumbers);
//
//			//CAP-26548
//			volatileSession.putParameter("lineItemNumbers", lineItemNumbers); //CAP-26548
//			volatileSession.setClearParameters(false);
//			volatileSession.setIsDirty(true); // CP-11840
//		}

		////CAP-47085 calling the new method to overwrite the UOM acronyms to full words
		convertAcronymsToWordsOnErrMsg(shoppingCart,appSessionBean);

		//CAP-35244 Add UOM for Cart Items
		shoppingCartResponse.setOeShoppingCartFormBean(shoppingCart);
		shoppingCartResponse.setUomForCartItems(getUOMForShoppingCartItems(shoppingCart,appSessionBean));//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
		shoppingCart.setItems(sortBundleComponents(shoppingCart.getItems()), appSessionBean);//CAP-50143
		loadDisclaimers(mainSession, shoppingCartResponse);//CAP-36082
		// CAP-40333 - also determine if save order button should show
		shoppingCartResponse.setShowSaveOrderButton(shouldShowSaveButton(mainSession));
		getUOMAndQuantityErrorMessage(mainSession, shoppingCartResponse);


		shoppingCartResponse.setStatusMessage(RouteConstants.UPDATE_CART_VALIDATION_NOERROR);
		shoppingCartResponse.setUpdateCartNoError("Y");
		// CAP-36437 - depending on the flag passed in, this may be an error or not
		//CAP-37645 Added checkBackOrderToSetStatus copied code to that method to check backordered status + removing the <br> tag
		checkBackOrderToSetStatus(appSessionBean, scRequest, shoppingCart, shoppingCartResponse, true);

		shoppingCartResponse.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
		shoppingCartResponse.setShowVendorItemNum(appSessionBean.showWCSSItemNumber());//CAP-37191 to add the vendor flag in the response

		// CAP-47795
		if (!Util.isBlankOrNull(shoppingCart.getBudgetAllocationErrMsg())) {
			shoppingCartResponse.setStatus(RouteConstants.REST_RESPONSE_FAIL);
			shoppingCartResponse.setStatusMessage(shoppingCart.getBudgetAllocationErrMsg());
		}

		//CAP-48606
		checkDistListSharedOrPrivate(shoppingCartResponse, appSessionBean);

		// CAP-48977
		shoppingCartResponse.setDeliveryOptionsList(
				populateDeliveryOptionsList(oeSession.getOESessionBean().getUserSettings(), appSessionBean));

		setEFDChargesCurrDesc(shoppingCartResponse, appSessionBean);//CAP-49811 //CAP-50003
		setEFDChargesCurrDescForBundles(shoppingCartResponse, appSessionBean);//CAP-50176
		setTBD(shoppingCartResponse.getOeShoppingCartFormBean(), appSessionBean);//CAP-50204
		setTBDForEFD(shoppingCartResponse.getOeShoppingCartFormBean(),appSessionBean);//CAP-50471
		return shoppingCartResponse;
	}


	//CAP-37645 Added checkBackOrderToSetStatus
	protected void checkBackOrderToSetStatus(AppSessionBean appSessionBean, COShoppingCartRequest scRequest,
			OEShoppingCartFormBean shoppingCart, COShoppingCartResponse shoppingCartResponse, boolean update)
			throws AtWinXSException {
		String backorderWarning = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_BACK_ORDER_WARNING_MSG); // CAP-38253 - remove BR
		//CAP-45934
		String warning = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), SFTranslationTextConstants.ITEM_WILL_BE_REMOVED_WARNING);
		for (OEShoppingCartLineFormBean cpLineItem : shoppingCart.getItems()) {
			if (update && Util.yToBool(shoppingCartResponse.getUpdateCartNoError())
					&& cpLineItem.getErrorMessage().length() > 0
					&& ((!cpLineItem.getErrorMessage().startsWith(backorderWarning)) && !cpLineItem.getErrorMessage().startsWith(warning)
							|| !scRequest.isBackOrderWarned())) { // CAP-38253 - compare with startsWith
				shoppingCartResponse.setUpdateCartNoError("N");
				shoppingCartResponse.setStatusMessage(RouteConstants.UPDATE_CART_VALIDATION_ERROR);
				break;
			}
			if (!Util.isBlankOrNull(cpLineItem.getErrorMessage())) {
				cpLineItem.setErrorMessage(cpLineItem.getErrorMessage().replace("<br>", " "));
			}
		}
		//added code to handle removal of <br> in an unlikely to happen scenario
		for (CartLine cartLineItems : shoppingCartResponse.getOeShoppingCartFormBean().getCartLines()) {
			if (!Util.isBlankOrNull(shoppingCartResponse.getShoppingCartQuantityErrorMsg())
					&& !Util.isBlankOrNull(shoppingCartResponse.getShoppingCartUomErrorMsg()))
				cartLineItems.setErrorMessage(cartLineItems.getErrorMessage().replace("<br>", " "));
		}
	}

	protected CartUpdateLineVO[] reloadShoppingCartFormBean(boolean validateItems, COShoppingCartRequest request, OEShoppingCartFormBean formBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession) throws AtWinXSException //CAP-6234 change to protected
	{
		final OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		OEShoppingCartAssembler assembler =
			OEAssemblerFactory.getShoppingCartAssembler(appSessionBean.getCustomToken(),
														appSessionBean.getDefaultLocale(),
														appSessionBean.getApplyExchangeRate());

		boolean doValidate = validateItems;
		boolean allowZeroQuantity = false;
		boolean preventZeroPriceTransfer = false;
		boolean allowZeroPricePCS = false;

		String ip = request.getUserIPAddress();
		CartUpdateLineVO[] cartLinesWithErrors = assembler.updateShoppingCart(oeSession.getSessionID(), doValidate, ip, formBean, oeOrderSessionBean,
				volatileSessionBean, allowZeroQuantity, preventZeroPriceTransfer, allowZeroPricePCS, appSessionBean.isCustomPointSite(), appSessionBean.isMobileSession(),
				appSessionBean.isPreSelectAltProfile(), true); //CAP-17512, CAP-36945

		return cartLinesWithErrors;
	}

	public void setShoppingCartCount(VolatileSessionBean volatileSessionBean, CustomizationToken customizationToken) throws AtWinXSException
	{
		Order order = ObjectMapFactory.getEntityObjectMap().getEntity(Order.class, customizationToken);
		order.populate(volatileSessionBean.getOrderId());
		volatileSessionBean.setShoppingCartCount(order.getShoppingCartCount());
	}

	protected boolean putShoppingCartInRequest(CartUpdateLineVO[] cartLinesWithErrors,
			boolean validateItems,
			boolean validateFeed,
			OEShoppingCartFormBean shoppingCartFormBean,
			boolean throwMsg,
			String actionID,
			SessionContainer mainSession) throws AtWinXSException
	{
		AppSessionBean appSessionBean = mainSession.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = mainSession.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = mainSession.getApplicationSession().getPunchoutSessionBean();
		OrderEntrySession oeSession = ((OrderEntrySession) mainSession.getModuleSession());
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		OEShoppingCartAssembler assembler =
			OEAssemblerFactory.getShoppingCartAssembler(appSessionBean.getCustomToken(),
														appSessionBean.getDefaultLocale(),
														appSessionBean.getApplyExchangeRate());

		boolean allItemsValid = true;

		OEShoppingCartFormBean formBean = shoppingCartFormBean;
		Message errMessages = new Message();

		Message promoCodeMsgObj = assembler.validatePromoCode(formBean, oeOrderSessionBean);
		if(promoCodeMsgObj != null && promoCodeMsgObj.getErrorCode() != null)
		{
			//CP-8970 changed token from String to an Object
			String promoCodeMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					promoCodeMsgObj.getErrorCode().getErrorName(),promoCodeMsgObj.getErrorCode().getReplaceMap());

	        if(formBean != null)
	        {
	          formBean.setPromoCodeErrMsg(promoCodeMsg);
	        }

		}

		//CAP-23031
		if(null != formBean && formBean.isExceedBudgetWarned())
		{
			oeSession.getOESessionBean().setForceCCOptionAllocation(true);//CAP-23237
		}
		else
		{
			oeSession.getOESessionBean().setForceCCOptionAllocation(false);//CAP-23237
			oeSession.getOESessionBean().setForceCCOptionFromOrderSum(false);//CAP-23238
		}

		//CP-9486 RAR - Refactored to move all Validation inside this IF condition so we can control
		//on when to run the validation of the items.
		if(validateItems)
		{
			//CAP-35559 Routing out of scope.
//			try {
//			//CP-9271
//				assembler.setRoutingMessageOnShoppingCart(request, appSessionBean, oeSession.getOESessionBean(), formBean, volatileSessionBean);
//			} catch(AtWinXSMsgException mess) {
//				//CAP-35025 : one line comment// modelMap.put("ERROR_MSG", mess.getMsg().getErrGeneralMsg());
//				OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
//						appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
//				oeSession.getOESessionBean().setApprovalCheckoutSession(null);
//				checkoutAssembler.clearAddressInfo(oeSession.getOESessionBean(), appSessionBean.getLoginID());
//				assembler.setRoutingMessageOnShoppingCart(request, appSessionBean, oeSession.getOESessionBean(), formBean, volatileSessionBean);
//			}

			if(cartLinesWithErrors != null && cartLinesWithErrors.length > 0 &&
					throwMsg)
			{
				Map replaceMap = new HashMap();
				replaceMap.put("{invalidCount}", Integer.toString(cartLinesWithErrors.length));
				errMessages.setErrorCode(new ErrorCode("/orderentry/shoppingcart.jsp", "invalidItems", replaceMap));
			}

			//CP-2263 - ALLOC START
			OEResolvedUserSettingsSessionBean userSettings = oeSession.getOESessionBean().getUserSettings();

			int orderScenarioNr = oeSession.getOESessionBean().getOrderScenarioNumber();

			//CAP-35559 Allocations out of scope.
			// Validate Budget Allocation here
			//parameters errMessages, formBean are pass-by-reference params
//			try
//			{
//				//CP-3341 - Only validate the budget allocation on update cart or checkout.
//				if(OrderEntryConstants.UPDATE_CART_ACTION.equals(actionID) || OrderEntryConstants.CHECKOUT_ORDER_ACTION.equals(actionID))
//				{
//					assembler.validateBudgetAllocation(
//							appSessionBean,
//							userSettings,
//							formBean,
//							orderScenarioNr,
//							actionID,
//							punchoutSessionBean); //CP-2911 removed errMessages as parameter
//				}
//			}
//			catch(AtWinXSMsgException e)
//	        {
//				//CAP-23031
//				if(formBean != null && e.getMsg() != null && e.getMsg().getErrorCode() != null && OrderEntryConstants.BUDGET_EXCEED_WARN_ERRORCODE.equalsIgnoreCase(e.getMsg().getErrorCode().getErrorName()))
//				{
//					boolean exceedBudgetWarned = Boolean.parseBoolean(Util.nullToEmpty((String)request.getParameter(OrderEntryConstants.BUDGET_EXCEED_MESSAGE_SHOWN)));
//
//					if(!exceedBudgetWarned)
//					{
//						allItemsValid = false;
//					}
//					formBean.setBudgetAllocationWarnMsg(e.getMsg().getErrGeneralMsg());
//
//					//CAP-35025 : one line comment// modelMap.put(OrderEntryConstants.BUDGET_EXCEED_MESSAGE_SHOWN, "true");
//					oeSession.getOESessionBean().setForceCCOptionAllocation(true);//CAP-23237
//				}
//				else if(formBean != null && e.getMsg() != null)		// If there's an allocation error, we're always going to the catch in the broker/controller.
//		        {
//					formBean.setBudgetAllocationErrMsg(e.getMsg().getErrGeneralMsg());
//
//		        	Message msg = e.getMsg();
//		        	assembler.addErrorMessage(msg.getErrGeneralMsg(), errMessages, msg.getErrorCode());
//		        	allItemsValid = false;
//		        }
//	        }
//
//			// Calculate remaining budget
//			AllocationSummaryBean	allocSummaryBean = assembler.calculateRemainingBudget(formBean, punchoutSessionBean, userSettings, appSessionBean, volatileSessionBean, orderScenarioNr);

			//call refactored method to set canContinue
			//parameters volatileSessionBean, formbean, errMessages are pass-by-reference parameters
			allItemsValid = assembler.isReadyForCheckout(allItemsValid,
												cartLinesWithErrors,
												volatileSessionBean,
												formBean,
												punchoutSessionBean,
												errMessages,
												promoCodeMsgObj,
												orderScenarioNr,
												validateFeed,
												appSessionBean,
												actionID);

			// CP-3146 Start - RVM Get form bean from handleFormSubmission to access validator error messages
			int errorCount = 0;
			OEShoppingCartLineFormBean[] formBeanLines = formBean.getItems();
			OEShoppingCartFormBean validatorFormBean = formBean;
			ArrayList validatorErrLineNum = new ArrayList();

			ShoppingCartValidator scValidator = new ShoppingCartValidator(); //CAP-35025 : one line comment// getShoppingCartValidator(request);
			boolean isError = scValidator.validateCartItemsCp(validatorFormBean, appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), oeSession.getOESessionBean().getOrderScenarioNumber(),
					appSessionBean.isNewLookNFeel(), punchoutSessionBean, appSessionBean.getCurrencyLocale(), oeSession.getOESessionBean().getUserSettings().isAllowRMS());

			if(validatorFormBean != null)
			{
				OEShoppingCartLineFormBean[] validatorFormBeanLines = validatorFormBean.getItems();
				for(int i=0; i<validatorFormBean.getItems().length; i++)
				{
					// Set quantity value from validator bean since invalid quantities have been set to 1 by getCartLineUpdateVO method in OEShoppingCartLineFormBean
					formBeanLines[i].setItemQuantity(validatorFormBeanLines[i].getItemQuantity());
					if(!Util.isBlankOrNull(validatorFormBeanLines[i].getErrorMessage()))
					{
						// Set error message if item does not have backorder warning
						// CP-11880 NKM - Translation Text for hardcoded values
						if(!validatorFormBeanLines[i].getErrorMessage().equals(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "unavailableItemMsg")))
						{
							formBeanLines[i].setErrorMessage(validatorFormBeanLines[i].getErrorMessage());
							validatorErrLineNum.add(formBeanLines[i].getLineNumber());
							errorCount++;
						}
					}
				}
			}
			// CP-3146 End

			//4/16 RI start put cartline error messages
			if(cartLinesWithErrors != null)
			{
				formBeanLines = formBean.getItems();
				for(int i = 0; i < cartLinesWithErrors.length; i++)
				{
					for(int j = 0; j < formBeanLines.length; j++)
					{
						if(cartLinesWithErrors[i].getLineNum() == Util.safeStringToInt(formBeanLines[j].getLineNumber()))
						{
							// CP-3146 - RVM Set error message if there's no error message set previously
							if(Util.isBlankOrNull(formBeanLines[j].getErrorMessage()))
							{
								formBeanLines[j].setErrorMessage(cartLinesWithErrors[i].getErrorMessage());
							}
							// CP-3146 - RVM Increment error count if cart line error is not added in validator error count
							if(!validatorErrLineNum.contains(formBeanLines[j].getLineNumber()))
							{
								++errorCount;
							}
						}
					}
				}
				formBean.setItems(formBeanLines, appSessionBean); //CAP-38173
			}
			//4/16 RI end

			// CP-3146 Start - RVM Set error code for invalid items when error count is not 0
			if(errorCount > 0)
			{
				//RI 4/23 start
				//show the user specific no. of errors in their cart
				Map replaceMap = new HashMap();
				replaceMap.put("{errorCount}", new Integer(errorCount));

				//Message msg = new Message();
				errMessages.setErrorCode(new ErrorCode("ShoppingCartValidator", "invalidItemsInCart", replaceMap));
				//modelMap.put(AtWinXSConstant.MSG_ATTRIB_NAME, msg);
				//RI 4/23 end
			}
			// CP-3146 End

			// CP-3146 Start - RVM Moved backorder warning error code setting here
//			boolean backOrderWarned = new Boolean(Util.nullToEmpty(request.getParameter(OrderEntryConstants.PARAM_BACKORDER_MESSAGE_SHOWN))).booleanValue();
//
//			//4/12 RI start: put back order warning if needed
//			if(!isFromPopulateMethod && (formBean.isBackorderedItems() || formBean.isHasNonCriticalBackOrder()) && !backOrderWarned && OrderEntryConstants.CHECKOUT_ORDER_ACTION.equals(actionID)
//					&& errorCount == 0)
//			{
//				// CP-3146 RVM Changed error list to error code for backorder warning
//				errMessages.setErrorCode(new ErrorCode("ShoppingCartController", "backOrderWarning", null));
//			}
			//4/12 RI end
			// CP-3146 End

			//save the needed attributes in the model
			//CAP-35025 : one line comment// modelMap.put(OrderEntryConstants.ALLOCATION_SUMMARY_BEAN, allocSummaryBean);
		}
		//CP-10486 RAR - Removed ELSE block which clears the line error messages.  Since we're not calling WCSS onload of the Shopping Cart,
		//the error messages, if there are, are most probably the messages we want to show onload.

		//CP-2911 4/28 start: RI encode itemNum and vendorItemNum then put in model
		if(formBean != null && formBean.getItems() != null)
		{
			OEShoppingCartLineFormBean[] lines = formBean.getItems();
			String[][] encodedParams = new String[lines.length][3];
			for(int i = 0; i < lines.length; i++)
			{
				try
				{
					encodedParams[i][0] = URLEncoder.encode(lines[i].getItemNumber(), "UTF-8");
					encodedParams[i][1]	= URLEncoder.encode(lines[i].getVendorItemNumber(), "UTF-8");
					encodedParams[i][2]	= URLEncoder.encode(lines[i].getItemDescription(), "UTF-8");
				}
				catch(UnsupportedEncodingException ignore)
				{
					//CAP-18389 - added logger.debug call to previously empty catch block
					logger.debug("This error is meant to be ignored",ignore);
				} //do the same logic as in OrderEntryUtil.addRowsToForm()
			}
			//CAP-35025 : one line comment// modelMap.put(OrderEntryConstants.ENCODED_PARAMS, encodedParams);
		}
		//CP-2911 4/28 end

		return allItemsValid;
	}

	//CAP-6234 Change to protected
	protected OEShoppingCartFormBean removeSelectedForDelete(OEShoppingCartFormBean formBean)
	{

		for (int ctr = 0; ctr < formBean.getItems().length; ctr++)
		{
			formBean.getItems()[ctr].setIsToBeRemoved(false);
		}

		return formBean;
	}

	/**
	 * Validate if Split Order checkout option should be displayed
	 * @param formBean
	 * @return
	 * @throws AtWinXSException
	 */
	//CAP-35559 - Out of scope.
//	protected OEShoppingCartFormBean checkSplitDisplay(OEShoppingCartFormBean formBean)//CAP-6234 change to protected
//	{
//		// CP-8858 ACL Validate if Split Order checkout option should be displayed
//		OEOrderSessionBean oeOrderSession = ((OrderEntrySession)moduleSession).getOESessionBean();
//		boolean isPunchout = oeOrderSession != null && oeOrderSession.isPunchout();
//		if(!isPunchout)
//		{
//			if (oeOrderSession.getOrderScenarioNumber() != OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE)
//			{
//				if(formBean.getItems() != null && formBean.getItems().length > 1)
//				{
//					formBean.setShowSplitOrder(true);
//				}
//			}
//
//		}
//
//		return formBean;
//	}

	//CAP-35559 Out of scope.
//	protected void populateEFDMessage(OEShoppingCartFormBean formBean, int orderScenario) throws AtWinXSException//CAP-6234 Change to protected
//	{
//		boolean isOrderToList = ( orderScenario == OrderEntryConstants.SCENARIO_CAMPAIGN_ONLY
//				|| orderScenario == OrderEntryConstants.SCENARIO_KIT_AND_DIST_LIST
//				|| orderScenario == OrderEntryConstants.SCENARIO_NO_KIT_AND_DIST_LIST
//				|| orderScenario == OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS
//				|| orderScenario == OrderEntryConstants.SCENARIO_MAIL_MERGE_AND_OTHERS_KITTED
//				|| orderScenario == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY
//				|| orderScenario == OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE
//				|| orderScenario == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY_KITTED
//				|| orderScenario == OrderEntryConstants.SCENARIO_SUBSCRIPTION_ONLY);
//
//		if (!isOrderToList)
//		{
//			return;
//		}
//
//		for (int i = 0; i < formBean.getCartLines().size(); i++)
//		{
//
//			OEShoppingCartLineFormBean cartLine = formBean.getItems()[i];
//
//				//validate EFD options it item is EFD 431
//			if (OrderEntryConstants.EFD_METHOD.equals(cartLine.getFileDeliveryMethod()) || OrderEntryConstants.EFD_PRINT_METHOD.equals(cartLine.getFileDeliveryMethod()))
//			{
//				String efdErrorMsg = "";
//
//				//CAP-1226 MGP - Add new flag for replacing EFD validation message when IQ dist list checkbox is selected
//				boolean isReplaceEdeliveryPrintMsg = false;
//
//				if (cartLine.getFileDeliveryOption().equalsIgnoreCase(OrderEntryConstants.PRINT_EFD_ENABLED_OPTION)
//						||cartLine.getFileDeliveryOption().equalsIgnoreCase(OrderEntryConstants.EFD_PRINT_ENABLED_OPTION)
//								|| cartLine.getFileDeliveryOption().equalsIgnoreCase(OrderEntryConstants.EFD_PRINT_OVERRIDE_OPTION))
//				{
//					//CP-8970 changed token from String to an Object
//					efdErrorMsg = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "cartEdeliveryOptionErrMsg"));
//
//					//CAP-1226 MGP - Check if IQ dist list checkbox is selected then set isReplaceEdeliveryPrintMsg to true
//					if (formBean.isSendToDistListUsingQtyFromFile())
//					{
//						isReplaceEdeliveryPrintMsg = true;
//					}
//
//				}
//				else if (cartLine.getFileDeliveryOption().equalsIgnoreCase(OrderEntryConstants.EFD_ENABLED_OPTION))
//				{
//					//CP-10008 PTB retain old message for classic look and feel, use new message for Usability
//					if(appSessionBean.isNewLookNFeel())
//					{
//						//CP-8970 changed token from String to an Object
//						efdErrorMsg = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "cartEdeliveryItemNewLookErrMsg"));
//					}
//					else
//					{
//						//CP-8970 changed token from String to an Object
//						efdErrorMsg = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "cartEdeliveryItemErrMsg"));
//					}
//				}
//				if (!Util.isBlankOrNull(efdErrorMsg))
//				{
//					String errMsg = cartLine.getErrorMessage();
//					if (!Util.isBlankOrNull(errMsg))
//					{
//						errMsg = errMsg + "<br>";
//					}
//
//					//CAP-1226 - Fix double message issue
//					if (null != errMsg && errMsg.indexOf(efdErrorMsg) == -1)
//					{
//						cartLine.setErrorMessage(errMsg + efdErrorMsg);
//					}
//
//					//CAP-1226 - Replace inaccurate message with the new message for eDelivery + Print.
//					if (isReplaceEdeliveryPrintMsg)
//					{
//						cartLine.setErrorMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "cartEDlvryOptIqDstLstErrMsg"));
//					}
//				}
//			}
//
//		}
//
//	}

	//CAP-35244-Add parameters with full text on UOM & sold as acronyms in the existing API for get shopping cart
	public List<UOMForCartItems> getUOMForShoppingCartItems(OEShoppingCartFormBean formBean,AppSessionBean appSessionBean) throws AtWinXSException{
	   ArrayList<UOMForCartItems> uomcartItems=new ArrayList<>();
		for (OEShoppingCartLineFormBean oeShoppingCartLineFormBean: formBean.getItems()) { // CAP-36874 - loop over items
			UOMForCartItems uomCart=new UOMForCartItems();
			uomCart.setCustomerItemNumber(oeShoppingCartLineFormBean.getItemNumber());
			uomCart.setVendorItemNumber(oeShoppingCartLineFormBean.getVendorItemNumber());
			uomCart.setLineNumeber(oeShoppingCartLineFormBean.getLineNumber());
			List<UOMItems> uomArrList=new ArrayList<>();
			 for(OEUomSessionBean oeUomSessionBean:oeShoppingCartLineFormBean.getUomBeanList()) {
				    UOMItems uomitm=new UOMItems();
				    uomitm.setUnitName(oeUomSessionBean.getUomCode() +"_" + oeUomSessionBean.getUomFactor()); // CAP-36874 - set to full UOM_UOMF
					uomitm.setUnitValue(ItemUtility.getUOMAcronyms(oeUomSessionBean.getUomCode()+" "+TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),"ofLbl")
										+" "+oeUomSessionBean.getUomFactor(),true,appSessionBean));//CAP-38561-Change UOM lookup handling to use new translation text values for full names - 11 APIs affected
					uomitm.setSoldAs(ItemUtility.getUOMAcronyms(oeUomSessionBean.getUomCode(),false,appSessionBean));
					uomArrList.add(uomitm);
					oeUomSessionBean.setUomCode(oeUomSessionBean.getUomCode() +"_" + oeUomSessionBean.getUomFactor());
			}
		 oeShoppingCartLineFormBean.setUOMCode(oeShoppingCartLineFormBean.getUOMCode() + "_" + oeShoppingCartLineFormBean.getUOMFactor()); // CAP-36874 - set to full UOM_UOMF

		 updatePriceTypeCodeC1UX(oeShoppingCartLineFormBean); //CAP-41136
		 uomCart.setUomList(uomArrList);
		 uomcartItems.add(uomCart);
		}

		formBean.getItems();  //forces cart recalculation
		return uomcartItems;
	}

	//CAP-41136 - update PriceTypeCode tag based on conditional
	protected void updatePriceTypeCodeC1UX(OEShoppingCartLineFormBean oeShoppingCartLineFormBean) {
		if (Util.isBlankOrNull(oeShoppingCartLineFormBean.getPriceTypeCode())
				&& (!Util.isBlankOrNull(oeShoppingCartLineFormBean.getUOMCode()))
				&& (!Util.isBlankOrNull(oeShoppingCartLineFormBean.getUOMFactor()))
				&& (!Util.isBlankOrNull(oeShoppingCartLineFormBean.getItemQuantity()))
				&& (!Util.isBlankOrNull(oeShoppingCartLineFormBean.getItemExtendedSellPrice()))
				&& (!"TBD".equals(oeShoppingCartLineFormBean.getItemExtendedSellPrice()))
				&& (oeShoppingCartLineFormBean.getTotalItemPrice()!=null))	{

			if (oeShoppingCartLineFormBean.getTotalItemPrice().doubleValue() > 0) {

				oeShoppingCartLineFormBean.setPriceTypeCode(ModelConstants.PRICING_TYPE_IT);
			}
			else if(oeShoppingCartLineFormBean.getTotalItemPrice().doubleValue() == 0) {

				oeShoppingCartLineFormBean.setPriceTypeCode(OrderEntryConstants.PRICING_TYPE_WCSS_ZERO_PRICE);
			}
			else {

				oeShoppingCartLineFormBean.setPriceTypeCode(OrderEntryConstants.PRICING_TYPE_NOT_FOUND);
			}
		}
	}


	//CAP-35437-Create 3 API services - one to return URL for destination for continue shopping and 2 for repeats
	@Override
	public ContinueShoppingResponse determineContinueDestination(SessionContainer sc) {
		boolean isselectedCatagoryId = false;
		boolean isUnifiedSearchCreteria = false;
		// CAP-39334 Use translation text values for return to labels
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		ContinueShoppingResponse response = new ContinueShoppingResponse();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		if (oeOrderSessionBean.getSearchCriteriaBean() != null) {
			// CAP-39334 Remove the unnecessary boolean literals
			isselectedCatagoryId = (oeOrderSessionBean.getSearchCriteriaBean().getSelectedCategoryId() > 0
					&& oeOrderSessionBean.getSearchCriteriaBean().isSearchSelectedCategory());
			isUnifiedSearchCreteria = (oeOrderSessionBean.getSearchCriteriaBean().getUnifiedSearchCriteria() != null
					&& !oeOrderSessionBean.getSearchCriteriaBean().getUnifiedSearchCriteria().isEmpty());

			if (isselectedCatagoryId) {
				response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
				// CAP-39334 Use translation text values for return to labels
				response.setReturnLinkText(getTranslation(appSessionBean, SFTranslationTextConstants.RETURN_TO_CATALOG_TEXT,
								SFTranslationTextConstants.RETURN_TO_CATALOG));
				response.setReturnLinkURL(RouteConstants.RETURN_TO_CATALOG_URL + "/0"); // CAP-36153
			} else if (isUnifiedSearchCreteria) {
				response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
				// CAP-39334 Use translation text values for return to labels
				response.setReturnLinkText(getTranslation(appSessionBean, SFTranslationTextConstants.RETURN_TO_RESULTS_TEXT,
								SFTranslationTextConstants.RETURN_TO_RESULTS));
				response.setReturnLinkURL(RouteConstants.RETURN_TO_RESULTS_URL);
			}
		}
		if (!isselectedCatagoryId && !isUnifiedSearchCreteria) {
			response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
			response.setReturnLinkText(RouteConstants.RETURN_TO_EMPTY);
			response.setReturnLinkURL(RouteConstants.HOME_PAGE_URL);
		}
		return response;
	}

	// CAP-36082 added a new method
	@Override
	public COShoppingCartResponse loadDisclaimers(SessionContainer sc, COShoppingCartResponse coresponseObject) {
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		if (userSettings.isShowOrderLinePrice()) {
			coresponseObject.setDisclaimers(userSettings.getDisclaimer());
		}
		return coresponseObject;
	}

	// CAP-40333 - must meet several criteria to show save
	protected boolean shouldShowSaveButton(SessionContainer sc) {
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		return (!(appSessionBean.isSharedID() && appSessionBean.getProfileNumber() == AtWinXSConstant.INVALID_PROFILE_NUMBER) // must have profile
				&& (sc.getApplicationSession().getPunchoutSessionBean() == null)  // must not be punchout
				&& (userSettings.isShowSaveOrders()) // must have the right to see saved orders
				&& (volatileSessionBean.getShoppingCartCount() > 0) // must have at least one item in the cart
				&& (volatileSessionBean.getOrderScenarioNumber() != OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE)); // must not be order from a file
	}

	//CAP-36164 - Basic Error Message - for FE validation.
	@Override
	public COShoppingCartResponse getUOMAndQuantityErrorMessage(SessionContainer mainSession, COShoppingCartResponse coresponseObject) throws AtWinXSException {

		AppSessionBean appSessionBean = mainSession.getApplicationSession().getAppSessionBean();

		//CP-8970 changed token from String to an Object
		String shoppingCartUomErrorMsg = Util.nullToEmpty(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_SHOPPINGCART_UOM_ERROR_MSG));
		//CP-8970 changed token from String to an Object
		String shoppingCartQuantityErrorMsg = Util.nullToEmpty(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_SHOPPINGCART_QTY_ERROR_MSG));

		coresponseObject.setShoppingCartUomErrorMsg(shoppingCartUomErrorMsg);
		coresponseObject.setShoppingCartQuantityErrorMsg(shoppingCartQuantityErrorMsg);
		return coresponseObject;
	}

	// CAP-36158 - set form bean for punchout
	protected void updateForPunchout(OEShoppingCartFormBean formBean, PunchoutSessionBean punchoutSessionBean, AppSessionBean appSessionBean) throws AtWinXSException
	{
		if (punchoutSessionBean != null)
		{
			formBean.setPunchout(true);

			formBean.setPunchoutDoneButtonText(translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_TEMPLATE_DONE_BTN));

			if (punchoutSessionBean.isAllowNewShipTo())
			{
				formBean.setPunchoutButtonText(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_PUNCHOUT_NEXT_SHIPPING_BUTTON));
			}
			else if (punchoutSessionBean.isAllowFreightSelect())
			{
				formBean.setPunchoutButtonText(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_PUNCHOUT_NEXT_CARRIER_BUTTON));
			}
			else if (!Util.isBlankOrNull(punchoutSessionBean.getTransferText()))
			{
				formBean.setPunchoutButtonText(punchoutSessionBean.getTransferText());
			}
			else
			{
				formBean.setPunchoutButtonText(translationService.processMessage(appSessionBean.getDefaultLocale(),
									appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_PUNCHOUT_DEFAULT_TRANSFER_BUTTON));
			}

			if (!Util.isBlankOrNull(punchoutSessionBean.getCancelButtonText()))
			{
				formBean.setPunchoutCancelButtonText(punchoutSessionBean.getCancelButtonText());
			}
			else
			{
				formBean.setPunchoutCancelButtonText(translationService.processMessage(appSessionBean.getDefaultLocale(),
									appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_CANCEL_BUTTON));
			}

			formBean.setPunchoutInspect(punchoutSessionBean.getOperation().equals(OrderEntryConstants.PUNCHOUT_OPERATION_INSPECT));
		}
	}


	//CAP-38710  API Fix - Add Translation to ResponseObject for /api/items/getcartitemdetail
	public COShoppingCartResponse getShoppingCartItemDetail(SessionContainer sc) throws AtWinXSException {

	      OEShoppingCartFormBean formBean = this.loadShoppingCart(sc, null, true);
	      loopThroughFormBean(formBean);//CAP-49787
	      // CAP-49693
	      AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
	      COShoppingCartResponse response = new COShoppingCartResponse();
	      response.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
	      response.setOeShoppingCartFormBean(formBean);
	      response.setUomForCartItems(this.getUOMForShoppingCartItems(formBean, appSessionBean));
	      formBean.setItems(sortBundleComponents(formBean.getItems()), appSessionBean);//CAP-50144
	      response.setShowVendorItemNum(appSessionBean.showWCSSItemNumber());//CAP-37191 to add the vendor flag in the response
	      this.loadDisclaimers(sc, response); // CAP-36082 added this line to - Modify getCartItemDetail API to add
			// CAP-40333 - also determine if save order button should show
	      response.setShowSaveOrderButton(shouldShowSaveButton(sc));
	                                              // disclaimers to response
	      //CAP-36164 - Basic Error Message - for FE validation.
	      this.getUOMAndQuantityErrorMessage(sc,response);

	     //CAP-38710- API Fix - Add Translation to ResponseObject for /api/items/getcartitemdetail
	      Properties resourceBundleProps = translationService.getResourceBundle(appSessionBean, "shoppingcart");
	      response.setTranslation(translationService.convertResourceBundlePropsToMap(resourceBundleProps));

	      // CAP-48977
	      OEResolvedUserSettingsSessionBean userSettings = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean().getUserSettings();
	      response.setDeliveryOptionsList(populateDeliveryOptionsList(userSettings, appSessionBean));
	      setEFDChargesDes(formBean, appSessionBean);
	      setTBD(formBean,appSessionBean);
	      setTBDForEFD(formBean, appSessionBean);//CAP-50471
	      return response;

		}
	
		//CAP-49673
		public void setEFDChargesDes(OEShoppingCartFormBean formBean, AppSessionBean asb) throws AtWinXSException {
			OEShoppingCartLineFormBean[] shoppingcartlines = formBean.getItems();
			for (OEShoppingCartLineFormBean shoppingcartlineitem : shoppingcartlines) {
				if(shoppingcartlineitem.getEfdCharges()!=null) {
				setEFDChargesCurrencyDesc(shoppingcartlineitem.getEfdCharges(), asb);
				}
				if(shoppingcartlineitem.getComponentDisplayLines()!=null) {//CAP-50176
					for (OEShoppingCartLineFormBean shoppingcartlineitemindisplaylines : shoppingcartlineitem.getComponentDisplayLines()) {
						if(shoppingcartlineitemindisplaylines.getEfdCharges()!=null) {
							setEFDChargesCurrencyDesc(shoppingcartlineitemindisplaylines.getEfdCharges(), asb);
							}
					}
				}
			}
		}
		
		//CAP-50204
		public void setTBD(OEShoppingCartFormBean formBean, AppSessionBean asb) {
			OEShoppingCartLineFormBean[] shoppingcartlines = formBean.getItems();
			for (OEShoppingCartLineFormBean shoppingcartlineitem : shoppingcartlines) {
				if (shoppingcartlineitem.getComponentDisplayLines() != null) {
					for (OEShoppingCartLineFormBean shoppingcartlineitemindisplaylines : shoppingcartlineitem
							.getComponentDisplayLines()) {
						if (shoppingcartlineitemindisplaylines.getEfdCharges() != null
								&& shoppingcartlineitemindisplaylines.getEfdCharge() > 0) {
							shoppingcartlineitemindisplaylines.setItemExtendedSellPrice(
									Util.getStringFromCurrency(shoppingcartlineitemindisplaylines.getEfdCharge(),asb.getCurrencyLocale(),false,2).getAmountText()
									 + AtWinXSConstant.EMPTY_STRING);
						}
						if( checkItmExtPrcZeroOrSpace(shoppingcartlineitemindisplaylines.getItemExtendedSellPrice())) {
							shoppingcartlineitemindisplaylines.setItemExtendedSellPrice("TBD");
						}
					}
				}
			}
		}
		
		//CAP-50471
		public boolean checkItmExtPrcZeroOrSpace(String itemExtSellPrice) {
			return itemExtSellPrice.contains(ZERO_PRICE)|| itemExtSellPrice.equals(AtWinXSConstant.BLANK_SPACE);
		}
		
		//CAP-50471//CAP-49811
		public void setTBDForEFD(OEShoppingCartFormBean formBean, AppSessionBean asb) {
			OEShoppingCartLineFormBean[] shoppingcartlines = formBean.getItems();
			for (OEShoppingCartLineFormBean shoppingcartlineitem : shoppingcartlines) {
				double extPrice=shoppingcartlineitem.getItemExtendedCurrency().getAmountValue();
				double efdCharge=shoppingcartlineitem.getEfdCharge();
				shoppingcartlineitem.setItemTotalPrice(Util.getStringFromCurrency(Double.parseDouble(shoppingcartlineitem.getTotalItemPrice()+""),
									asb.getCurrencyLocale(), false, 2).getAmountText() + AtWinXSConstant.EMPTY_STRING);
				
				if (shoppingcartlineitem.getEfdCharges() != null && shoppingcartlineitem.getEfdCharge() > 0
						&& (shoppingcartlineitem.getItemExtendedSellPrice().equals("TBD")
								|| (shoppingcartlineitem.getItemExtendedSellPrice().contains(ZERO_PRICE)
										&& shoppingcartlineitem.getItemExtendedSellPrice().indexOf(ZERO_PRICE) == 1))) {
					shoppingcartlineitem
							.setItemExtendedSellPrice(Util.getStringFromCurrency(shoppingcartlineitem.getEfdCharge(),
									asb.getCurrencyLocale(), false, 2).getAmountText() + AtWinXSConstant.EMPTY_STRING);
				}
				if (shoppingcartlineitem.getItemExtendedSellPrice().contains(ZERO_PRICE)
						|| shoppingcartlineitem.getItemExtendedSellPrice().equals(AtWinXSConstant.BLANK_SPACE)) {
					shoppingcartlineitem.setItemExtendedSellPrice("TBD");
				}
				setItemTotalPrice(shoppingcartlineitem,extPrice,efdCharge,asb);
			}
		}
		
		//CAP-49811
		public void setItemTotalPrice(OEShoppingCartLineFormBean shoppingcartlineitem, double extPrice,
				double efdCharge, AppSessionBean asb) {
			if ((shoppingcartlineitem.getFileDeliveryMethod().equals("P") && extPrice < 0)
					|| (shoppingcartlineitem.getFileDeliveryMethod().equals("E") && efdCharge < 0)
					|| shoppingcartlineitem.getFileDeliveryMethod().equals("B") && (extPrice < 0 || efdCharge < 0)) {
				shoppingcartlineitem.getItemExtendedCurrency().setAmountText("TBD");
				shoppingcartlineitem.setItemExtendedSellPrice("TBD");
				shoppingcartlineitem.setItemTotalPrice("TBD");
			} else if (shoppingcartlineitem.getFileDeliveryMethod().equals("B") && (extPrice >= 0 && efdCharge >= 0)) {
				shoppingcartlineitem.setItemTotalPrice(
						Util.getStringFromCurrency(extPrice + efdCharge, asb.getCurrencyLocale(), false, 2)
								.getAmountText() + AtWinXSConstant.EMPTY_STRING);
			}
		}
		
		//CAP-49673
		public void setEFDChargesCurrencyDesc(Map<String, XSCurrency> edfcharges, AppSessionBean asb)
				throws AtWinXSException {
			for (Entry<String, XSCurrency> entry : edfcharges.entrySet()) {
				entry.getValue().setCurrencyDesc(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), entry.getKey()));
			}
		}

	// CAP-42295
	public C1UXCustDocInitializeRequest createInitializeUIEditRequest(SessionContainer sc,
			CustDocEditCartRequest itemInputs) throws AtWinXSException
	{
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		C1UXCustDocInitializeRequest request = new C1UXCustDocInitializeRequest();

		if (isSessionOrderSubmittedCannotEdit(sc)) {
			throw new AccessForbiddenException(getClassName());
		}

		// CAP-40882 Wrap in service for easier unit testing
		OEShoppingCartAssembler assembler = oeAssemblerFactoryService.getShoppingCartAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate());
		OrderLineVO line = assembler.getSpecialItemOrderLine(volatileSessionBean.getOrderId().intValue(),
				itemInputs.getOrderLineNumber(), appSessionBean.getCurrencyLocale());
		if ((line != null) && (line.getCustomDocLineKey() != null) && (line.getCustomDocLineKey().getCustomDocLineID() > 0)) {
			// CAP-46491 - check for a non-blank value in the bundle component field
			if(!Util.isBlankOrNull(line.getBundleComponentType()) && line.getBundleComponentType().equalsIgnoreCase("C")) {
				String errorMsg = this.getTranslation(appSessionBean, "sf.bundleCompCannotEditApart", "This item may be modified within the bundle only.");
				throw new AtWinXSException(errorMsg, getClassName());
			}

			request.setAvailabilityCode(line.getItemAvailabilityAvailabilityCode());
			request.setItemNumber(line.getCustomerItemNum());
			request.setItemQuantity(line.getOrderQty());
			request.setPrice(String.valueOf(line.getItemSellPrice()));
			request.setSelectedUom(line.getUomCode());
			request.setVendorItemNumber(line.getWallaceItemNum());
			request.setEntryPoint((line.isHasItemStub()) ? ICustomDocsAdminConstants.ENTRY_POINT_CART_ADD_FROM_STUB : ICustomDocsAdminConstants.ENTRY_POINT_CART_EDIT); // CAP-46469
			request.setOrderLineNumber(line.getLineNum());
			request.setCustomDocumentOrderLineNumber(line.getCustomDocLineKey().getCustomDocLineID());
		}
		else
		{
			String errorMsg = this.getTranslation(appSessionBean, "custDocNotValidMsg", "Your custom document is no longer valid.");
			throw new AtWinXSException(errorMsg, getClassName());
		}
		return request;
	}

	protected String getClassName() {
		return className;
	}

	//CAP-47085
	public void convertAcronymsToWordsOnErrMsg(OEShoppingCartFormBean shoppingCart, AppSessionBean appSessionBean)
			throws AtWinXSException {
		for (OEShoppingCartLineFormBean shoppingCartLine : shoppingCart.getCartLines()) {
			String acronymsConvertedForErrorMessage = "";
			String trimChars = "";
			if (!Util.isBlankOrNull(shoppingCartLine.getErrorMessage())
					&& (shoppingCartLine.getErrorMessage().startsWith("Minimum order")
							|| shoppingCartLine.getErrorMessage().startsWith("Maximum order"))) {
				trimChars = shoppingCartLine.getErrorMessage()
						.substring(shoppingCartLine.getErrorMessage().lastIndexOf(AtWinXSConstant.BLANK_SPACE),
								shoppingCartLine.getErrorMessage().lastIndexOf(ModelConstants.PERIOD))
						.trim().replace(ModelConstants.PERIOD, AtWinXSConstant.EMPTY_STRING);
				acronymsConvertedForErrorMessage = shoppingCartLine.getErrorMessage()
						.replace(trimChars + ModelConstants.PERIOD, AtWinXSConstant.EMPTY_STRING)
						+ AtWinXSConstant.EMPTY_STRING + ItemUtility.getUOMAcronyms(trimChars, true, appSessionBean);
				shoppingCartLine.setErrorMessage(acronymsConvertedForErrorMessage);
			}
		}
	}

	//CAP-48606
	public void checkDistListSharedOrPrivate(COShoppingCartResponse response, AppSessionBean appSessionBean) throws AtWinXSException {
		IManageListAdmin listAdmin = listsAdminLocatorService.locate(appSessionBean.getCustomToken());
		ManageListsBusinessUnitPropsVO businessUnitPropsVO = listAdmin.getBuListDetails(
				new ManageListsBusinessUnitPropsVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID()));
		if (businessUnitPropsVO != null) {
			response.setDoNotShareListsInd(businessUnitPropsVO.isDoNotShareListsInd());
		}
	}
	
	// CAP-49728
	public void setFileDeliveryMethod(OEShoppingCartLineFormBean cpLineItem, String delMethodFromRequest) {
		cpLineItem.setFileDeliveryMethod(delMethodFromRequest);
	}

	// CAP-49728
	public boolean validateAndSetDeliveryOption(String delMethodFromRequest, String delOption) {
		return (delMethodFromRequest != null
				&& (((delOption.equals("EP") || delOption.equals("PE") || delOption.equals("P") || delOption.equals(""))
						&& delMethodFromRequest.equals("P"))
						|| ((delOption.equals("EP") || delOption.equals("PE") || delOption.equals("EA") || delOption.equals("PO"))//CAP-50835
								&& delMethodFromRequest.equals("B"))
						|| (!delOption.equals("P") && delMethodFromRequest.equals("E"))));
	}

	// CAP-49728
	public void callValidationToSetDeliveryOption(String selectedDeliveryOption,
			OEShoppingCartLineFormBean cpLineItem) {
		if (validateAndSetDeliveryOption(selectedDeliveryOption, // CAP-49728
				cpLineItem.getFileDeliveryOption())) {
			setFileDeliveryMethod(cpLineItem, selectedDeliveryOption);
		}
	}
	
	// CAP-49787
	public void loopThroughFormBean(OEShoppingCartFormBean formBean) {
		for (OEShoppingCartLineFormBean oe : formBean.getCartLines()) {
			if (oe.getFileDeliveryOption().equals("EA") && oe.getFileDeliveryMethod().equals("P")) {
				oe.setFileDeliveryMethod("B");
			}
		}
	}
	
	// CAP-49693
	protected OEShoppingCartLineFormBean[] sortBundleComponents(OEShoppingCartLineFormBean[] items) {
		if (null != items) {
			List<OEShoppingCartLineFormBean> itemList = Arrays.asList(items);
			Optional<OEShoppingCartLineFormBean> bundle = itemList.stream()
					.filter(i -> (i.getBundleComponentTypeCode().equals(ComponentType.GenericComponent.toString())))
					.findAny();
			if (bundle.isPresent()) {
				List<OEShoppingCartLineFormBean> bundleMasterList = itemList.stream()
						.filter(i -> (i.getBundleParentOrderLine() == AtWinXSConstant.INVALID_ID
										|| i.getBundleParentOrderLine() == 0))
						.collect(Collectors.toList());
				List<OEShoppingCartLineFormBean> sortedBundleList = new ArrayList<>();
				for (OEShoppingCartLineFormBean masterItem : bundleMasterList) {
					if (masterItem.getBundleComponentTypeCode().equalsIgnoreCase(ComponentType.GenericMaster.toString())) {
						List<OEShoppingCartLineFormBean> bundleComponentList = itemList.stream()
								.filter(i -> i.getBundleParentOrderLine() == Integer.valueOf(masterItem.getLineNumber()))
								.collect(Collectors.toList());
						OEShoppingCartLineFormBean[] componentDisplayLines = bundleComponentList.toArray(new OEShoppingCartLineFormBean[bundleComponentList.size()]);
						masterItem.setComponentDisplayLines(componentDisplayLines);
					}
					sortedBundleList.add(masterItem);
				}
				items = sortedBundleList.toArray(new OEShoppingCartLineFormBean[sortedBundleList.size()]);
			}
		}
		return items;
	}
	
	// CAP-49811 //CAP-50003
	public void setEFDChargesCurrDesc(COShoppingCartResponse coShopCartResponse, AppSessionBean asb)
			throws AtWinXSException {
		for (OEShoppingCartLineFormBean shoppingcartlineitem : coShopCartResponse.getOeShoppingCartFormBean()
				.getItems()) {
			if (shoppingcartlineitem.getEfdCharges() != null) {
				for (Entry<String, XSCurrency> entry : shoppingcartlineitem.getEfdCharges().entrySet()) {
					entry.getValue().setCurrencyDesc(translationService.processMessage(asb.getDefaultLocale(),
							asb.getCustomToken(), entry.getKey()));
				}
			}
			if(Util.isBlankOrNull(shoppingcartlineitem.getItemImageURL())) {//CAP-49903
				shoppingcartlineitem.setItemImageURL(ModelConstants.C1UX_NO_IMAGE_MEDIUM);
			}
		}
	}
	
	//CAP-50176
	public void setEFDChargesCurrDescForBundles(COShoppingCartResponse coShopCartResponse, AppSessionBean asb)
			throws AtWinXSException {
		for (OEShoppingCartLineFormBean shoppingcartlineitem : coShopCartResponse.getOeShoppingCartFormBean()
				.getItems()) {
			if (shoppingcartlineitem.getComponentDisplayLines() != null) {
				for (OEShoppingCartLineFormBean shoppingcartlineitemindisplaylines : shoppingcartlineitem
						.getComponentDisplayLines()) {
					if (shoppingcartlineitemindisplaylines.getEfdCharges() != null) {
						setEFDChargesCurrencyDesc(shoppingcartlineitemindisplaylines.getEfdCharges(), asb);

					}
				}
			}
		}
	}
}

