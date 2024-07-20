/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	07/04/2022 	Krishna Natarajan		CAP-35204				adding the logger in debug and error levels
 *	10/31/2022	Krishna Natarajan		CAP-36794				updated the getters to match fields for the FE calls  (getCatalogLineNumber, getSelectedUom, getItemQuantity)
 *	12/12/22	A Boomker				CAP-37637				In addItemToCart(), only validate if both are not empty/null
 *	05/11/22	N Caceres				CAP-39045				Resolve concurrency issues in ItemAddToCart Service
 *	01/06/23	Krishna Natarajan		CAP-41083				Added to validate the availabilityCode sent via request
 *	07/25/23	A Boomker				CAP-42223				Handling redirect to cust doc UI added
 *	08/28/23	A Boomker				CAP-43223				Removed creation of customized item URLs as none of that will work in C1UX
 *	08/30/23	A Boomker		CAP-43405					Fixing item in cart flags for customizable items
 *	01/17/24	Krishna Natarajan		CAP-46342				Handled method to give an error on EOO
 *  02/06/24	Krishna Natarajan		CAP-46920				Handled price declaration with nullToEmpty on itemAddToCartRequest.getPrice()
 *	03/11/24	A Boomker				CAP-46495				Redirect bundles to cust doc UI too
 *  04/02/24	T Harmon				CAP-48396				Fixed issue when you don't have a budget to change add to cart to not allow if you have budgets turned on
 *  06/05/24	C Codina				CAP-49893				Validation added to check if item is a kit template
 *  06/14/24	S Ramachandran			CAP-50031				Reformat better messages for Kit Template errors					
 */
package com.rrd.c1ux.api.services.addtocart;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartRequest;
import com.rrd.c1ux.api.models.addtocart.ItemAddToCartResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.kits.MKComponentInterfaceLocatorService;
import com.rrd.c1ux.api.services.orderentry.locator.OESavedOrderComponentLocatorService;
import com.rrd.c1ux.api.services.orderentry.util.OrderEntryUtilService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.gwt.common.exception.CPRPCRedirectException;
import com.rrd.custompoint.orderentry.entity.CatalogItem;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderLine;
import com.rrd.custompoint.orderentry.entity.OrderLine.ComponentType;
import com.rrd.custompoint.orderentry.entity.OrderLines;
import com.wallace.atwinxs.campaigns.locator.CampaignComponentLocator;
import com.wallace.atwinxs.campaigns.util.CampaignConstants;
import com.wallace.atwinxs.campaigns.vo.CampaignCompositeVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICampaignComponent;
import com.wallace.atwinxs.interfaces.IOECampaignComponent;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOESavedOrderComponent;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.interfaces.MKComponentInterface;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.ao.MKTemplateOrderAssembler;
import com.wallace.atwinxs.kits.vo.KitTemplateVOKey;
import com.wallace.atwinxs.kits.vo.MKKitTemplateCompositeVO;
import com.wallace.atwinxs.orderentry.admin.dao.AllocationQuantitiesDAO;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.locator.OECampaignComponentLocator;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.locator.OEShoppingCartComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderLineExtendedUOMInfoVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.orderstatus.util.OrderStatusConstants;

@Service
public class ItemAddToCartServiceImpl extends BaseService implements ItemAddToCartService {

    private static final Logger logger = LoggerFactory.getLogger(ItemAddToCartServiceImpl.class);

    public static final String CATALOG_LINE_NUMBER		= "catalogLnNbr";
    public static final String ITEM_NUMBER		= "itemNumber";
    public static final String VENDOR_ITEM_NUMBER		= "vendorItemNumber";
    public static final String STATUS_SUCCESS		= "Success";
    public static final String STATUS_FAILURE		= "Failed";
    
    // CAP-50031
  	protected MKComponentInterfaceLocatorService  mkitsComponentInterfaceLocatorService;
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    // CAP-39045 Dependency injection for new wrapper objects
    private final OrderEntryUtilService orderEntryUtilService;
    private final OESavedOrderComponentLocatorService oeSavedOrderComponentLocatorService;

	public ItemAddToCartServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService, OrderEntryUtilService orderEntryUtilService,
			OESavedOrderComponentLocatorService oeSavedOrderComponentLocatorService,
			MKComponentInterfaceLocatorService  mkitsComponentInterfaceLocatorService
			) {
	      super(translationService, objectMapFactoryService);
		this.orderEntryUtilService = orderEntryUtilService;
		this.oeSavedOrderComponentLocatorService = oeSavedOrderComponentLocatorService;
		this.mkitsComponentInterfaceLocatorService = mkitsComponentInterfaceLocatorService;
	}

	@Override
	public ItemAddToCartResponse addItemToCart(SessionContainer sc,ItemAddToCartRequest itemAddToCartRequest) throws AtWinXSException, CPRPCRedirectException, CPRPCException {
		logger.debug(" Inside addItemToCart method - ItemAddToCartServiceImpl ");

		String price=Util.nullToEmpty(itemAddToCartRequest.getPrice());
		String priceTypeCode="";
		String itemNumber = itemAddToCartRequest.getItemNumber();
		String vendorItemNumber = itemAddToCartRequest.getVendorItemNumber();
		int catalogLnNbr = itemAddToCartRequest.getCatalogLineNumber();
		String selectedUOM = Util.nullToEmpty(itemAddToCartRequest.getSelectedUom());
		int quantity = itemAddToCartRequest.getItemQuantity();
		String availabilityCode = evaluateAvailabilityCode(itemAddToCartRequest.getAvailabilityCode());//CAP-41083 evaluating the code and assigning it
		boolean isRolloverOrDetails = false;
		Map<String, String> orderLinesUOMQtyMap = null ;
		ItemAddToCartResponse response = new ItemAddToCartResponse();
		response.setStatus(STATUS_FAILURE);

		boolean isAddedToCart = false;

		try
		{
			logger.debug(" Inside Try - to add Item To Cart ");

			boolean canItemBeAddedToCart = true;
			// CAP-39045 Resolve concurrency issues in ItemAddToCart Service
			ApplicationSession appSession = sc.getApplicationSession();
			AppSessionBean appSessionBean = appSession.getAppSessionBean();

			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
			VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
			OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
			PunchoutSessionBean punchoutSessionBean = appSession.getPunchoutSessionBean();

			// CAP-1861 Th - Null out approvalsummaryinfo here if we are adding something to the cart
			nullApprovalSummaryInfo(oeSessionBean); //CAP-30487 SRN Extracted for reuse

			setOrderId( punchoutSessionBean, volatileSessionBean);

			//CAP-7013 RAR - When adding items to cart, then reset the order in session.
			resetPromoMasterOrderLine(oeSessionBean); //CAP-30487 SRN Extracted for reuse

			//CP-9939
			appSessionBean.setInRequestorMode(volatileSessionBean.getOrderOnBehalf().isInRequestorMode());

			//CAP-310
			double dblPrice = -1.0;
			logger.debug(" Price or PriceTypeCode empty check ");
			if (!price.isEmpty())
			{
					// CAP-10080 TH- Changed the currency conversion because it isn't working for non-US currency locales
				dblPrice = Util.getCurrencyFromString(price, appSessionBean.getCurrencyLocale());
			}
			else if(priceTypeCode.isEmpty())
			{
				priceTypeCode = OrderEntryConstants.PRICING_TYPE_NOT_FOUND;
			}

			logger.debug(" Price or PriceTypeCode empty check done");

			// CAP-39045 Create wrapper object for ObjectMapFactory
			CatalogItem catalogItem = objectMapFactoryService.getEntityObjectMap().getEntity(CatalogItem.class, appSessionBean.getCustomToken());
			catalogItem.populate(appSessionBean, oeSessionBean, itemNumber, vendorItemNumber, catalogLnNbr, true);

			//CP-9197 , initial selected site attribute
			//Initialize enfoce on ordering settings, CP-9197
			setSkippedEOOAttributeValues(appSessionBean, oeSessionBean, volatileSessionBean);	//CAP-30487 SRN Extracted for reuse

			OrderLineVO cartLineVO = this.buildOrderLineVO(catalogItem, selectedUOM, quantity, appSessionBean, volatileSessionBean, oeSession, dblPrice, priceTypeCode, availabilityCode); //CAP-310  //CAP-22332

			logger.debug(" can item be added to cart - boolean check");
			//CAP-46342 Added additional parameter
			canItemBeAddedToCart = validateAddToCart(catalogItem, appSessionBean, volatileSessionBean, oeSessionBean, userSettings, punchoutSessionBean, cartLineVO, quantity, isRolloverOrDetails,response);//CAP-22029 //CAP-22756

			//CP-11689, if in the cart, we need to set the batchapproval session to null, do not confuse with batch approval work flow
			setBatchApprovalToNull(oeSession); //CAP-30487 SRN Extracted for reuse

			if(canItemBeAddedToCart)
			{
				logger.debug(" item be added to cart - boolean value is found true ");

				//CAP-2469
				volatileSessionBean.setOrderLinesUOMQtyMap(orderLinesUOMQtyMap);

				//If the Item is valid (canItemBeAddedToCart == true) AND the item needs to be fulfilled in other
				//pages before it can be added to the cart, then build the URL where the item needs to be redirected
				//by throwing a CPRPCRedirectException.
				if(isCustomizableItem(catalogItem.getItemClassification(), catalogItem.getVendorItemNumber(), appSessionBean, appSession.getPunchoutSessionBean()))
				{
					logger.debug("item - is customizable");

					//CP-9175 RAR - Pass the selected UOM.
					oeSessionBean.setContinueShopping(true); // CP-11248 should return to search results when done
					if (oeSessionBean.getSearchCriteriaBean() != null)
					{ // wipe this out so that when we return to CatalogServiceImpl.getCatalogTree(), we go back to the last category

							SessionHandler.persistServiceInSession(oeSession, appSessionBean.getEncodedSessionId(), AtWinXSConstant.ORDERS_SERVICE_ID);
					}
					// CAP-42223 and CAP-46495
					if ((ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(catalogItem.getItemClassification()))
							|| (ItemConstants.ITEM_CLASS_BUNDLE.equals(catalogItem.getItemClassification())))
					{
						response.setRedirectToCustomDocumentUI(true);
			
					}
					// CAP-49893
					else if (ItemConstants.ITEM_CLASS_KIT_TEMPLATE.equals(catalogItem.getItemClassification()))
					{
						response.setRedirectToKitBuilder(true);
					}
					return response;
					// if not a cust doc, this is not handled
				}
				else
				{
					logger.debug("item - is not customizable");

					volatileSessionBean.addToShoppingCart(appSessionBean, cartLineVO, null);

					isAddedToCart = true;

					//CAP-7598 SRN - Call saveSystemSavedOrder to make sure system saved order is saved and shared with teams
					saveSystemSavedOrder(appSessionBean, oeSessionBean, volatileSessionBean, userSettings); //CAP-31063 SRN Extracted for reuse

					this.persistInSession( applicationVolatileSession); // CAP-3449

					//C1UX - BE - Add total item Count response attribute addtocart API
					response.setItemCountInShopingCart(String.valueOf(applicationVolatileSession.getVolatileSessionBean().getShoppingCartCount()));

					response.setStatus(STATUS_SUCCESS);
					response.setMessage("This item is added to the shopping cart.");

				}
			}
		}
		catch (AtWinXSException e)
		{
			logger.error("AtWinXSException, as item is already in cart: "+ e.getStackTrace());
			response.setMessage(e.getMessage());
		}
		response.setItemAddedToCart(isAddedToCart);
		logger.debug("item - is added to cart");

		return response;

	}

	public void setOrderId(PunchoutSessionBean punchoutSessionBean,VolatileSessionBean volatileSessionBean) {

		if ((punchoutSessionBean != null) && (volatileSessionBean.getOrderId() == null)) // CP-8971
		{
			logger.debug("setting Order ID");
			volatileSessionBean.setOrderId(punchoutSessionBean.getQuoteID());
		}
	}

	/**
	 * CAP-30487 Extracted method for reuse
	 * This method will reset the approval summary info
	 * @param oeSessionBean
	 */
	private void nullApprovalSummaryInfo(OEOrderSessionBean oeSessionBean)
	{
		if (oeSessionBean.getApprovalSummaryInfo() != null)
		{
			logger.debug("setting ApprovalSummaryInfo null");
			oeSessionBean.setApprovalSummaryInfo(null);
			oeSessionBean.setApprovalCheckoutSession(null);
			oeSessionBean.setPrepopulatedSource(null);
		}
	}

	/**
	 * CAP-30487 Extracted method for reuse
	 * This method will reset the promo master order line
	 * @param oeSessionBean
	 */
	private void resetPromoMasterOrderLine(OEOrderSessionBean oeSessionBean)
	{
		if(null != oeSessionBean.getPromoMasterOrderLine())
		{
			logger.debug("setting PromoMasterOrderLine null");
			oeSessionBean.setPromoMasterOrderLine(null);
		}
	}

	/**
	 * CAP-30487 SRN Extracted for reuse
	 * This will set the EOO Attribute Values
	 * @param appSessionBean
	 * @param oeSessionBean
	 * @param volatileSessionBean
	 * @throws AtWinXSException
	 */
	private void setSkippedEOOAttributeValues(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		if (appSessionBean.hasEnforceOnOrdering() && volatileSessionBean.getSelectedSiteAttribute() == null)
		{
			logger.debug("setting Skipped EOOAtributeValues");
			OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
			assembler.setSkippedEOOAttributeValues(appSessionBean,volatileSessionBean, oeSessionBean ,false);
		}
	}

	/**
	 * Method buildOrderLineVO()
	 *
	 * This method will build the {@link OrderLineVO} to be used in adding the item in the cart.
	 *
	 * @param catalogItem
	 * @param selectedUOM
	 * @param quantity
	 * @param oeSession
	 * @param volatileSessionBean
	 * @param appSessionBean
	 * @return OrderLineVO
	 * @throws AtWinXSException
	 */
	private OrderLineVO buildOrderLineVO(CatalogItem catalogItem, String selectedUOM, int quantity, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession, double price, String priceTypeCode, String availabilityCode) throws AtWinXSException //CAP-310 //CAP-22332
	{
		// CAP-39045 Create wrapper object for OrderEntryUtil
		boolean withinKit = orderEntryUtilService.currentlyWithinKit(oeSession, volatileSessionBean);
	    String replacesItemNumber = volatileSessionBean.getItemNumberBeingReplaced(appSessionBean.getSiteID(),  catalogItem.getItemNumber(), withinKit);

	    String uomCode = Util.isBlankOrNull(selectedUOM) ? OrderEntryConstants.DEFAULT_UOM_CODE : selectedUOM.substring(0, selectedUOM.indexOf("_"));
	    int uomFactor = Util.isBlankOrNull(selectedUOM) ? OrderEntryConstants.DEFAULT_UOM_FACTOR : Integer.parseInt(selectedUOM.substring((selectedUOM.indexOf("_")+1), selectedUOM.length()));

		OrderLineVO orderLineVO =
			new OrderLineVO(
				catalogItem.getItemNumber(),
				catalogItem.getVendorItemNumber(),
				catalogItem.isAlwaysRouteInd(),
				catalogItem.getApprovalQuantity(),
				catalogItem.getDescription(),
				catalogItem.getEDocURL(),
				OrderStatusConstants.COMPLETE_ORDER_LINE, //orderLineStatusCode
				appSessionBean.getSiteDefaultQty(),
				catalogItem.getDeliveryOption(),
				catalogItem.isRouteIndicatorLineReview(),
				catalogItem.getApprovalQuantityLineReview(),
				uomCode,
				uomFactor,
				replacesItemNumber,
				false, //hasItemStub
				true, //isInspectReady
				AtWinXSConstant.INVALID_ID, //destinationID
				price, //CAP-310
				priceTypeCode,
				AtWinXSConstant.DEFAULT_ADDR_CT, // CAP-1595/CAP-2185 lineAddressCount
				ComponentType.NoType); //CAP-7163 RAR

		//We need to set the Quantity based on the user input.
		orderLineVO.setOrderQty(quantity);

		//CAP-22332
		if (Util.isBlankOrNull(availabilityCode) ||
			availabilityCode.equals("A") ||
			availabilityCode.equals("B"))
		{
			if (availabilityCode!=null)
			{
				orderLineVO.setItemAvailabilityAvailabilityCode(availabilityCode);
			}
		} else {
			orderLineVO.setItemAvailabilityDpodCode(availabilityCode);
		}

		if (!withinKit)
		{
			volatileSessionBean.setItemNumberBeingReplaced("");
		}
		logger.debug("OrderLineVO built");
		return orderLineVO;
	}

	/**
	 * @deprecated CAP-4954 RAR - Use AddToCartProcessor.validate()
	 *
	 * Method validateAddToCart()
	 *
	 * This method will call the existing validations that we have for the item being added to the cart.
	 * @param catalogItem
	 * @param userSettings
	 * @param oeSessionBean
	 * @param volatileSessionBean
	 * @param appSessionBean
	 * @param punchoutSessionBean
	 * @return boolean
	 * @throws AtWinXSException
	 */
	@Deprecated
	//CAP-46342 added additional parameter
	private boolean validateAddToCart(CatalogItem catalogItem, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean, OEResolvedUserSettingsSessionBean userSettings, PunchoutSessionBean punchoutSessionBean, OrderLineVO cartLineVO, int quantity,  boolean isRolloverOrDetails, ItemAddToCartResponse response ) throws AtWinXSException //CAP-22029 //CAP-22756
	{
		boolean canItemBeAddedToCart = true;

		try
		{
			logger.debug("validating AddToCart request");
			//CP-10955 OFF should not allow any other items to be added
			validateIfOrderFromAFile(appSessionBean, oeSessionBean, false); //CAP-30487 SRN Extracted for reuse
			OEShoppingCartAssembler shoppingCartssembler = this.getShoppingCartAssembler(appSessionBean);
			String campaignType = "";

			// CAP-39045 Add null check
			if (null != catalogItem.getDeliveryOption() && null != catalogItem.getItemNumber() &&
					null != catalogItem.getVendorItemNumber()) {
				shoppingCartssembler.doShoppingCartValidation(catalogItem.getDeliveryOption(),
						catalogItem.getItemNumber(), catalogItem.getVendorItemNumber(), volatileSessionBean,
						userSettings, appSessionBean);
				shoppingCartssembler.canAddItemToCart(oeSessionBean, catalogItem.getItemNumber(),
						catalogItem.getVendorItemNumber(), catalogItem.getItemClassification(), appSessionBean,
						volatileSessionBean);
			}

			String replaceText = Util.isBlankOrNull(catalogItem.getItemNumber())?catalogItem.getDescription():catalogItem.getItemNumber(); //CAP-22190
			//CAP-22029
			if (!Util.isBlankOrNull(catalogItem.getVendorItemNumber()) && quantity > 0 &&	cartLineVO.getUomFactorNum() > 0 && !cartLineVO.getUomCode().isEmpty() && isRolloverOrDetails) //CAP-22321 CAP-22353 //CAP-22756
			{
				validateItemQuantity(catalogItem.getVendorItemNumber(), quantity, cartLineVO, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), replaceText); //CAP-22190
			}

			//CAP-33964 - split for code refactoring
			validateCampaign(catalogItem,appSessionBean,volatileSessionBean,punchoutSessionBean);
			validateKitTemplate(catalogItem,appSessionBean,punchoutSessionBean);

			//Validate for Mix Cart Rules
			shoppingCartssembler.validateCartForMixCartRule(appSessionBean, volatileSessionBean, catalogItem.getItemClassification(), appSessionBean.getSiteID(), catalogItem.getItemNumber(), campaignType);

			//Allocation Validation
			if (userSettings.isAllowAllocationsInd())
			{
				 ArrayList<String> errors = new ArrayList<>();

				shoppingCartssembler.validateItemQtyAllocation(catalogItem.getItemNumber(),
																					  catalogItem.getVendorItemNumber(),
																					  userSettings,
																					  errors,
																					  appSessionBean);


				shoppingCartssembler.validateBudgetAllocation(campaignType,
																					 userSettings,
																					 errors,
																					 "", //Pass "" for eventID
																					 oeSessionBean.getOrderScenarioNumber(),
																					 punchoutSessionBean,
																					 appSessionBean);

				//CAP-48396
				validateBudgetAllocations(userSettings, appSessionBean);
				
				if(!errors.isEmpty())
				{
					 Message mess = new Message();

			        	if(errors.size() == 1)
			        	{
							mess.setErrGeneralMsg(errors.get(0));
			        	}
			        	else
			        	{
							mess.setErrGeneralMsg("The following errors were found:<br>");
							mess.setErrMsgItems(errors);
			        	}
						throw new AtWinXSMsgException(mess, this.getClass().getName());
				}
			}
		}
		catch(AtWinXSException e)
		{
			logger.error("Item cannot be added to cart: "+e.getStackTrace());
			//CAP-46342
			response.setStatus(STATUS_FAILURE);
			response.setItemCountInShopingCart(String.valueOf(volatileSessionBean.getShoppingCartCount()));
			String message = buildErrorMessage(SFTranslationTextConstants.ITEM_CANNOT_ADDED2CART, appSessionBean, null)
					+ RouteConstants.SINGLE_SPACE + e.getMessage();
			response.setMessage(message);
			canItemBeAddedToCart = false;
		}

		return canItemBeAddedToCart;
	}

	// CAP-48396
	protected void validateBudgetAllocations(OEResolvedUserSettingsSessionBean userSettings, AppSessionBean appSessionBean) throws AtWinXSException
	{
		if (userSettings.isAllowBudgetAllocations() && !userSettings.isAllowOrderingWithoutBudget())
		{
			String errMsg = "";
			ErrorCode errorCode = null;
			
			// Get the budget for the user
			AllocationQuantitiesDAO quantitiesDAO = new AllocationQuantitiesDAO();
		    AllocationQuantitiesCompositeVO quantitiesVO = null;
		    //CP-2263 - ALLOC - Use budget allocation grp and level cd to get current budget
		    quantitiesVO = quantitiesDAO.getAllocationQuantities(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getProfileNumber(), userSettings.getBudgetAllocationGrp(), 
		            						userSettings.getBudgetAllocationLevelCd(), "", "", OrderEntryConstants.ALLOC_TIMEFRAME_CURRENT, 
		            						OrderAdminConstants.ALLOCATIONS_BUDGET_TYPE_CODE);
		    
		    if (quantitiesVO == null)
		    {
		    	errMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "err_place_ords_budget_amt");
	            
	            //CP-2909 Create the ErrorCode object with correct context and errorName
		        errorCode = new ErrorCode("global","noBudgetAlloc",null);
		        
		        // CP-2909 4/26 Set the ErrorCode object in Message Object
		        Message msg = new Message();
		        msg.setErrGeneralMsg(errMsg);
		        msg.setErrorCode(errorCode);
		        throw new AtWinXSMsgException(msg, this.getClass().getName());
		    }
		}
	}
	
	public void validateCampaign(CatalogItem catalogItem,AppSessionBean appSessionBean,VolatileSessionBean volatileSessionBean,PunchoutSessionBean punchoutSessionBean) throws AtWinXSException {

		if(ItemConstants.ITEM_CLASS_CAMPAIGN.equals(catalogItem.getItemClassification()))
	    {
			if (punchoutSessionBean != null)
			{ // CP-8971 - campaigns not orderable through punchout
				Message mess = new Message();
				mess.setErrGeneralMsg(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_PUNCHOUT_NO_CAMPAIGNS));
				logger.error("Error while checking if punchout session bean is null: "+mess, this.getClass().getName());
				throw new AtWinXSMsgException(mess,	this.getClass().getName());
			}
			MKTemplateOrderAssembler mkTemplateAssembler = this.getMKTemplateOrderAssembler(appSessionBean);
			boolean itemIsWithinDates = mkTemplateAssembler.validateItemWithinAvailAndExpDates(appSessionBean.getSiteID(), catalogItem.getItemNumber(), this.getCurrentDate());

			//CP-9218 RAR - Fixed the checked to NOT itemIsWithinDates.
			if(!itemIsWithinDates)
			{
				Message mess = new Message();
				mess.setErrGeneralMsg(CampaignConstants.ORDERABLE_DATE_ERR_MSG);
				logger.error("Error while checking if is within dates: "+mess, this.getClass().getName());
				throw new AtWinXSMsgException(mess,	this.getClass().getName());
			}

			boolean itemIsOrderable = mkTemplateAssembler.validateItemIsOrderable(appSessionBean.getSiteID(), catalogItem.getItemNumber());

			if(!itemIsOrderable)
			{
				Message mess = new Message();
				mess.setErrGeneralMsg(CampaignConstants.MSG_CAMPAIGN_NOT_ORDERABLE);
				logger.error("Error while checking if item is orderable: "+mess, this.getClass().getName());
				throw new AtWinXSMsgException(mess,	this.getClass().getName());
			}

			ICampaignComponent campaign = CampaignComponentLocator.locate(appSessionBean.getCustomToken());
			CampaignCompositeVO campaignVO = campaign.getCampaignVOByCustItemNum(appSessionBean.getSiteID(), catalogItem.getItemNumber());

			if(CampaignConstants.CAMPAIGN_TYPE_SUBSCRIPTION.equals(campaignVO.getCampaignType()))
			{
				//CAP-63 RAR - Check if the subscription campaign is already in the cart.
				boolean isItemAddedToCart = isSubscriptionAlreadyInCart(volatileSessionBean.getOrderId(), catalogItem.getItemNumber(), appSessionBean.getCustomToken());

				if(isItemAddedToCart)
				{
					String message = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "subscriptionOrderNotAddedMsg");
					message += " " + TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "itemAlreadyInCartMsg");
					Message mess = new Message();
					mess.setErrGeneralMsg(message);
					logger.error("Error while checking if item is already added to cart: "+mess, this.getClass().getName());
					throw new AtWinXSMsgException(mess,	this.getClass().getName());
				}

				//CAP-63 RAR - Check if user has not subscribed to the subscription campaign.
				boolean isUserSubscribed = isUserSubscribed(appSessionBean.getSiteID(), appSessionBean.getLoginID(), appSessionBean.getProfileNumber(), catalogItem.getItemNumber(), appSessionBean.getCustomToken());

				if(isUserSubscribed)
				{
					String message = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "subscriptionOrderNotAddedMsg");
					message += " " + TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "alreadySubscribedMsg");
					Message mess = new Message();
					mess.setErrGeneralMsg(message);
					logger.error("Error while checking if user is subscribed: "+mess, this.getClass().getName());
					throw new AtWinXSMsgException(mess,	this.getClass().getName());
				}
			}
	    }

	}

	
	// CAP-50031
	public void validateKitTemplate(CatalogItem catalogItem,AppSessionBean appSessionBean,PunchoutSessionBean punchoutSessionBean) throws AtWinXSException {
	// CP-8971
	if (ItemConstants.ITEM_CLASS_KIT_TEMPLATE.equals(catalogItem.getItemClassification()))
    {
		if ((Util.isBlankOrNull(catalogItem.getVendorItemNumber())) && (punchoutSessionBean != null))
		{ // CP-8971 - non-VM kit templates not orderable through punchout
			Message mess = new Message();
			mess.setErrGeneralMsg(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_PUNCHOUT_NO_OLD_KIT_TEMPLATES));
			logger.error("Error while checking if Item class is Kit Template: "+mess, this.getClass().getName());
			throw new AtWinXSMsgException(mess,	this.getClass().getName());
		}
		// CAP-10878 - need to validate kit template is within orderable dates
		MKTemplateOrderAssembler mkTemplateAssembler = this.getMKTemplateOrderAssembler(appSessionBean);
		boolean itemIsWithinDates = mkTemplateAssembler.validateItemWithinAvailAndExpDates(appSessionBean.getSiteID(), catalogItem.getItemNumber(), this.getCurrentDate());
		
		MKComponentInterface iKits = mkitsComponentInterfaceLocatorService
				.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		KitTemplateVOKey kitTemplateVOKey = new KitTemplateVOKey(appSessionBean.getSiteID(),
				catalogItem.getItemNumber());
		MKKitTemplateCompositeVO mkKitTemplateCompositeVO = iKits.getKitTemplate(kitTemplateVOKey);

		boolean itemIsOrderable = ("A".equals(mkKitTemplateCompositeVO.getKitTemplate().getKitTmpltApprovedCd()));

		if (!itemIsOrderable) {

			Message mess = new Message();
			mess.setErrGeneralMsg(
					buildErrorMessage(SFTranslationTextConstants.KIT_TEMP_ITEM_NOT_APPROVED_ERR, appSessionBean, null));
			throw new AtWinXSMsgException(mess, this.getClass().getName());

		} else if (!itemIsWithinDates) {

			// Compare the default empty date as well as before and after.
			ParsePosition pos = new ParsePosition(0);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			java.util.Date defaultDate = sdf.parse(ItemConstants.DEFAULT_EMPTY_DATE, pos); // "01/01/1970"

			Message mess = new Message();
			if (this.getCurrentDate() == null) {

				throw new AtWinXSWrpException(new Exception("A date to validate was not specified."),
						this.getClass().getName());
			} else if (validateEarliestOrderDate(mkKitTemplateCompositeVO.getEarliestOrderDate(), this.getCurrentDate(),
					defaultDate)) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.EARLIEST_POSSIBLE_DATE_REPLACEMENT_TAG,
						Util.getStringFromDate(mkKitTemplateCompositeVO.getEarliestOrderDate(),
								appSessionBean.getDefaultLocale()));
				mess.setErrGeneralMsg(
						buildErrorMessage(SFTranslationTextConstants.KIT_TEMP_ITEM_EARLIEST_POSSIBLE_ORDER_ERR,
								appSessionBean, replaceMap));
				throw new AtWinXSMsgException(mess, this.getClass().getName());
			} else if (validatelastPossibleOrderDate(mkKitTemplateCompositeVO.getLastPossibleOrderDate(),
					this.getCurrentDate(), defaultDate)) {

				mess.setErrGeneralMsg(buildErrorMessage(SFTranslationTextConstants.KIT_TEMP_ITEM_EXPIRED_ORDER_ERR,
						appSessionBean, null));
				throw new AtWinXSMsgException(mess, this.getClass().getName());
			}
		}
	}
	}
	
	// CAP-50031
	private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {
	
		return Util.nullToEmpty(
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), errorKey, replaceMap));
	}
	
	// CAP-50031
	protected String getLocaleDateFormat(AppSessionBean appSessionBean) {
	
		return Util.getDateFormatForLocale(appSessionBean.getDefaultLocale());
	}
	
	// CAP-50031 - validate kit template item for Earliest Order Date
	protected boolean validateEarliestOrderDate(Date availabilityDate, Date dateToValidate, Date defaultDate) {
	
		boolean validationPassed = false;
		if (availabilityDate != null && (availabilityDate.equals(defaultDate) || dateToValidate.before(availabilityDate)
				|| dateToValidate.equals(availabilityDate))) {
	
			validationPassed = true;
		}
		return validationPassed;
	}
	
	// CAP-50031 - validate kit template item for expiration Order Date
	protected boolean validatelastPossibleOrderDate(Date expirationDate, Date dateToValidate, Date defaultDate) {
	
		boolean validationPassed = false;
		if (expirationDate != null && (expirationDate.equals(defaultDate) || dateToValidate.after(expirationDate)
				|| dateToValidate.equals(expirationDate))) {
			validationPassed = true;
		}
		return validationPassed;
	} 

	/**
	 * CAP-30487 Extracted method for reuse
	 * This methd will check if scenario is order from a file
	 * @param appSessionBean
	 * @param oeSessionBean
	 * @throws AtWinXSMsgException
	 * @throws AtWinXSException
	 */
	@SuppressWarnings("deprecation")
	private void validateIfOrderFromAFile(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, boolean isBulkAtcValidation) throws  AtWinXSException
	{
		if(oeSessionBean.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE)
		{
			String errorMessage = isBulkAtcValidation? "off_blkatc_errmsg" : "not_orderable_off_in_prcs";
			logger.error("Error while checking if order is from file: "+errorMessage, this.getClass().getName());
			throw new AtWinXSMsgException(new Message(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), errorMessage), null), this.getClass().getName());
		}
	}

	/**
	 * Method  getShoppingCartAssembler()
	 *
	 * This method will return a new instance of {@link OEShoppingCartAssembler}.
	 * @param appSessionBean
	 *
	 * @return OEShoppingCartAssembler
	 */
	private OEShoppingCartAssembler getShoppingCartAssembler(AppSessionBean appSessionBean)
	{
		return new OEShoppingCartAssembler(appSessionBean.getCustomToken(),
																  appSessionBean.getDefaultLocale(),
																  appSessionBean.getApplyExchangeRate());
	}

	//CAP-22029
	private void validateItemQuantity(String vendorItemNumber, int quantity, OrderLineVO cartLineVO, CustomizationToken token, Locale locale, String replaceText) throws AtWinXSException //CAP-22190
	{
		logger.debug("validating Item Quantity");
		IOEManageOrdersComponent oeService = OEManageOrdersComponentLocator.locate(token);
		OrderLineExtendedUOMInfoVO lineItemInfo = oeService.getLineItemInfo(vendorItemNumber);
		if (lineItemInfo!=null)
		{
			IOEShoppingCartComponent shoppingCartComp = OEShoppingCartComponentLocator.locate(token);
			Message msg = shoppingCartComp.
				validateItemQuantity(
					quantity,
					cartLineVO.getUomFactorNum(),
					cartLineVO.getUomCode(),
					lineItemInfo.getStockUOM(),
					lineItemInfo.getMininumOrderQuantity(),
					lineItemInfo.getMaximumOrderQuantity(),
					lineItemInfo.getMultipleOrderQuantity());
			if (msg!=null)
			{
				//CAP-22190
				if (msg.getErrorCode()!=null)
				{
					String msgName = msg.getErrorCode().getErrorName() + "wItem";
					Map<String, Object> replaceMap = msg.getErrorCode().getReplaceMap();
					if (replaceMap==null)
					{
						replaceMap = new HashMap<>();
					}
					replaceMap.put("{item}", replaceText);
					String messageText = TranslationTextTag.processMessage(locale, token, msgName, replaceMap);
					msg.setErrGeneralMsg(messageText);
					msg.setErrorCode(null);
				}
				logger.error("Error while validating Item Quantity"+msg,this.getClass().getName());
				throw new AtWinXSMsgException(
						msg,
						this.getClass().getName());
			}
		}
	}

	/**
	 * Method getMKTemplateOrderAssembler()
	 *
	 * This method will return a new instance of {@link MKTemplateOrderAssembler}.
	 * @param appSessionBean
	 *
	 * @return MKTemplateOrderAssembler
	 */
	private MKTemplateOrderAssembler getMKTemplateOrderAssembler(AppSessionBean appSessionBean)
	{
		return new MKTemplateOrderAssembler(appSessionBean.getCustomToken(),
																	 appSessionBean.getDefaultLocale());
	}

	/**
	 * Method getCurrentDate()
	 *
	 * This method will return the current date.
	 *
	 * @return Date
	 */
	protected Date getCurrentDate()
	{
        GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		return cal.getTime();
	}

	//CAP-63 RAR
	/**
	 * Method isSubscriptionAlreadyInCart()
	 *
	 * This method will check if the subscription item being added to the cart
	 * already exists in the cart.
	 *
	 * @param orderId
	 * @param itemNumber
	 * @param customToken
	 * @return boolean
	 * @throws AtWinXSException
	 */
	protected boolean isSubscriptionAlreadyInCart(Integer orderId, String itemNumber, CustomizationToken customToken) throws AtWinXSException
	{
		if(null != orderId && orderId > -1)
		{
			Order order = ObjectMapFactory.getEntityObjectMap().getEntity(Order.class, customToken);
			order.populate(orderId);
			OrderLines orderlines = order.getOrderLines();

			if(null != orderlines && (null != orderlines.getOrderLines() && !orderlines.getOrderLines().isEmpty()))
			{
				for(OrderLine orderLine : orderlines.getOrderLines())
				{
					if(orderLine.getCustomerItemNumber().equals(itemNumber))
					{
						return true;
					}
				}
			}
		}

		return false;
	}


	//CAP-63 RAR
	/**
	 * Method isUserSubscribed()
	 *
	 * This method will check if the logged in user has an existing subscription to the campaign.
	 *
	 * @param siteID
	 * @param loginID
	 * @param profileNumber
	 * @param customerItemNumber
	 * @param customizationToken
	 * @return boolean
	 * @throws AtWinXSException
	 */
	protected boolean isUserSubscribed(int siteID, String loginID, int profileNumber, String customerItemNumber, CustomizationToken customizationToken) throws AtWinXSException
	{
		IOECampaignComponent campaign = OECampaignComponentLocator.locate(customizationToken);
		return campaign.isUserSubscribed(siteID, loginID, profileNumber, customerItemNumber);
	}

	/**
	 * CAP-30487 SRN Extracted for reuse
	 * This will set the batch approval session into null
	 * @param oeSession
	 */
	private void setBatchApprovalToNull(OrderEntrySession oeSession)
	{
		if (oeSession.getOESessionBean().getBatchApprovalSessionBean() != null)
		{
			oeSession.getOESessionBean().setBatchApprovalSessionBean(null);
		}
	}

	/**
	 * CAP-31063 SRN Extracted method for reuse
	 * This will ensure that the order is saved and shared within teams
	 * @param appSessionBean
	 * @param oeSessionBean
	 * @param volatileSessionBean
	 * @param userSettings
	 * @throws AtWinXSException
	 */
	private void saveSystemSavedOrder(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			VolatileSessionBean volatileSessionBean, OEResolvedUserSettingsSessionBean userSettings)
			throws AtWinXSException
	{
		// CAP-39045 Create wrapper object for OESavedOrderComponentLocator
		IOESavedOrderComponent savedOrderComponent = oeSavedOrderComponentLocatorService.locate(appSessionBean.getCustomToken());
		savedOrderComponent.saveSystemSavedOrder(oeSessionBean.getProfileSelections(), appSessionBean, volatileSessionBean, userSettings);
	}

	/**
	 * Method persistInSession()
	 *
	 * This method will persist the changes in the session.
	 * @param ttSession
	 * @param applicationVolatileSession
	 * @throws AtWinXSException
	 * @throws CPRPCException
	 */
	private void persistInSession( ApplicationVolatileSession applicationVolatileSession) throws AtWinXSException   // CAP-3449
	{
		applicationVolatileSession.setIsDirty(true);
		SessionHandler.saveSession(applicationVolatileSession, applicationVolatileSession.getSessionID(),  AtWinXSConstant.APPVOLATILESESSIONID);
	}

	//CAP-41083 added to validate the availabilityCode sent via request
	private String evaluateAvailabilityCode(String availabilityCode) {
		if (!OrderEntryConstants.AVAIL_CODE_AVAILABLE.equals(availabilityCode)
				&& !OrderEntryConstants.AVAIL_CODE_NOT_AVAILABLE.equals(availabilityCode)
				&& !OrderEntryConstants.AVAIL_CODE_JIT.equals(availabilityCode)
				&& !Util.isBlankOrNull(availabilityCode)) {
			availabilityCode="";
			logger.debug("The availabilityCode sent in the request was not legal value and set to empty");
		}
		return availabilityCode;
	}

}
