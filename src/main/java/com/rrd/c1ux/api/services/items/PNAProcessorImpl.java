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
 *  12/16/22    Sakthi M        CAP-35911  getPNA service must return Translation text values for Status availability
 *  06/15/22	C Codina		SonarQube: Possible NullPointerException bug in PNAProcessorImpl - DEV Only
 *  10/10/23	Krishna Natarajan	CAP-44347 Add the method to return available quantity divided by the UOM factor
 *  04/03/24    Krishna Natarajan	CAP-48388 Added new method checkBudgetAllocations
 */

package com.rrd.c1ux.api.services.items;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.items.PNARequest;
import com.rrd.c1ux.api.models.items.PNAResponse;
import com.rrd.c1ux.api.services.common.exception.CORPCException;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.AdminUtilService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.OEPricingAndAvailabilityComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.PricingAndAvailabilityUtil;
import com.rrd.custompoint.standardoptions.entity.StandardOption;
import com.wallace.atwinxs.catalogs.dao.CatalogDefaultDAO;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOEPricingAndAvailability;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.OrderFilterVO;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

@Component
public class PNAProcessorImpl implements PNAProcessor {
	
	@Autowired
	private TranslationService translationService;
	
	private static final Logger logger = LoggerFactory.getLogger(PNAProcessorImpl.class);
	
	public static final DecimalFormat decimalFormatter = new DecimalFormat("###,###,###,###");
	
	//CAP-40613 Dependency injection for new wrapper objects
	private final OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService;
	
	private final AdminUtilService adminUtil;
	
	private final OEPricingAndAvailabilityComponentLocatorService oePricingAndAvailabilityComponentLocatorService;
	
	private final ObjectMapFactoryService objectMapFactoryService;
	
	protected final OEAssemblerFactoryService oeAssemblerFactoryService;

	public PNAProcessorImpl(OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService,
			AdminUtilService adminUtil,
			OEPricingAndAvailabilityComponentLocatorService oePricingAndAvailabilityComponentLocatorService,
			ObjectMapFactoryService objectMapFactoryService, OEAssemblerFactoryService oeAssemblerFactoryService) {
		this.oeManageOrdersComponentLocatorService = oeManageOrdersComponentLocatorService;
		this.adminUtil = adminUtil;
		this.oePricingAndAvailabilityComponentLocatorService = oePricingAndAvailabilityComponentLocatorService;
		this.objectMapFactoryService = objectMapFactoryService;
		this.oeAssemblerFactoryService = oeAssemblerFactoryService;//CAP-48388
	}
	
	//CAP-34140   Method logic copied from CustomPointWeb PricingAndAvailabilityServiceImpl.getPricingAndAvailability()
	public PNAResponse processPNA(PNARequest pnaRequest, SessionContainer mainSession)
			throws CORPCException {
		
		AppSessionBean asb = null; 
		OEOrderSessionBean osb = null;
		
		String itemNumber="";
		String vendItemNum="";
		int quantity=0;
		String selectedUOM="";
		boolean isCustomDoc=false;
		
		PNAResponse response=null;
		
		try {
			
			asb = mainSession.getApplicationSession().getAppSessionBean();
			osb = ((OrderEntrySession) mainSession.getModuleSession()).getOESessionBean();
			
			itemNumber=pnaRequest.getItemNumber();
			vendItemNum=pnaRequest.getVendItemNum();
			quantity=pnaRequest.getQuantity();
			selectedUOM=pnaRequest.getSelectedUOM();
			isCustomDoc=pnaRequest.isCustomDoc();
			//CAP-40613 Create wrapper object for OEManageOrdersComponentLocator
			IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService.locate(asb.getCustomToken());
			OrderFilterVO[] filterVOs = osb.getOrderFilters();
			int uomFactor =0;

			// if the filters have not yet been saved to session, get them from service
			if (filterVOs == null) {
				filterVOs =	
					ordersService.getOrderFilters(
						osb.getSiteID(),
						osb.getBusinessUnitID(),
						osb.getUserGroupID(),
						osb.getLoginID());

				osb.setOrderFilters(filterVOs);
			}
			
			OEResolvedUserSettingsSessionBean userSettings = osb.getUserSettings();
			String corpNumForCall = OrderEntryUtil.getPricingCorpNumber(asb.getCorporateNumber(), osb.getOrderFilters());
			
			if(quantity <= 0) {
				
				quantity = 1;
			}
			else {
				
				 uomFactor = parseSelectedUOM(selectedUOM);
				
				quantity = quantity * uomFactor;
			}
			
			String priceClass = adminUtil.getWCSSPriceClassCd(userSettings, asb);			
			
			IOEPricingAndAvailability pna = oePricingAndAvailabilityComponentLocatorService.locate(asb.getCustomToken());
			PriceAndAvailabilityVO vo = pna.getPriceAndAvailability(userSettings.isShowOrderLinePrice(), userSettings.isShowOrderLineAvailability(), 
										vendItemNum, OrderEntryConstants.ORDER_TYPE_CODE_EDI, 
										quantity, corpNumForCall, "     ", true, userSettings.isUseCSSListPrice(), 
										userSettings.isUseLastJLJobPrice(), userSettings.isUseCustomersCustomerPrice(), 
										userSettings.isUseXSItemPrice(), asb.getSiteID(), itemNumber, userSettings.getLastJLJobPriceMarkup(), "", 2,
										userSettings.isUseTPP(),priceClass, false); // CP-12496
			
			//CAP-190 retrieve catalog default item property in checking show available qty
			CatalogDefaultDAO dao = new CatalogDefaultDAO();
			CatalogDefaultVO catDefVO = dao.retrieve(new CatalogDefaultVOKey(asb.getSiteID(), vendItemNum, itemNumber));
			
			//CAP-190 Add method that retrieves available qty
			int availableQty = 0;
			//CAP-40613 - Resolve the possible NullPointer in catDefVO
			if (catDefVO != null) {
				if (catDefVO.isShowQuantityAvailable()) {

					availableQty = pna.retrieveAvailableQty(vendItemNum, itemNumber, asb.getCorporateNumber(),
							asb.getLoginID(), false);
				}
				
				HashMap<Object,Object> addFlagsAndUOM = new HashMap<>();//CAP-44347
				addFlagsAndUOM.put("availableQty", availableQty);
				addFlagsAndUOM.put("itemNumber", itemNumber);
				addFlagsAndUOM.put("uomFactor", uomFactor);
				
				//CAP-190 add available qty
				response = buildResponse(vo, asb, userSettings.isShowOrderLinePrice(),
						userSettings.isShowOrderLineAvailability(), isCustomDoc, catDefVO.isShowQuantityAvailable(),
						addFlagsAndUOM);//CAP-44347
			}
			
			checkBudgetAllocations(response, userSettings, vo,asb, mainSession, osb);//CAP-48388
		}
		catch(AtWinXSException ae) {
			logger.error(this.getClass().getName() + " - " + ae.getMessage(),ae);
			throw new CORPCException("An error has occurred.");
		}
		return response;
	}
	//CAP-40613 - Resolve Sonar Qube issue
	private int parseSelectedUOM(String selectedUOM) {
		//CP-9064 - expand quantity using the UOM factor specified to get in terms of EA
		String [] uomString = selectedUOM.split("_");
		int uomFactor = 1;
		
		try {
			
			uomFactor = Integer.parseInt(uomString[1]);
		} catch (Exception e) {
			
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
		return uomFactor;
	}
	
	//CAP-34140   Method logic copied from CustomPointWeb PricingAndAvailabilityServiceImpl.buildResponse()
	public PNAResponse buildResponse(PriceAndAvailabilityVO vo, AppSessionBean asb, boolean showPrice, boolean showAvailability, boolean isCustomDoc, 
			boolean showAvailableQty, Map<?,?> addFlagsAndUOM) throws AtWinXSException {
		
		PNAResponse response = new PNAResponse();
		//CAP-44347
		int availableQty = (int) addFlagsAndUOM.get("availableQty");
		String itemNumber = (String) addFlagsAndUOM.get("itemNumber");
		int uomFactor = (int) addFlagsAndUOM.get("uomFactor");
		
		if(showPrice) {
			try {
				response.setPriceLabel(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), "price_lbl"));
			}
			catch(AtWinXSException ae) {
				logger.error(this.getClass().getName() + " - " + ae.getMessage(),ae);
				response.setPriceLabel("Price");
			}
			
			if ((vo.getItemExtendedSellPrice() > 0) || (OrderEntryConstants.PRICING_TYPE_WCSS_ZERO_PRICE.equals(vo.getPriceType()))) {
				
				response.setItemExtendedSellPrice(Util.getStringFromCurrency(vo.getItemExtendedSellPrice(), asb.getCurrencyLocale(), false).getAmountText());
				response.setPriceTypeCode(vo.getPriceType()); //CAP-310
			}
			else {
				//CAP-35911 Translation text changes
				response.setItemExtendedSellPrice(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),TranslationTextConstants.PRICE_NOT_FOUND_LABEL));
			}
		}
		
		//CAP-44347
		checkAvailability(showAvailability, response, asb, vo, isCustomDoc, itemNumber);
		
		if(showAvailableQty) {
			
			try {
				response.setAvailQtyLabel(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), "availQtyLbl"));
			}
			catch(AtWinXSException ae) {
				
				logger.error(this.getClass().getName() + " - " + ae.getMessage(),ae);
			}
			response.setAvailableQty(Integer.toString(availableQty));
			checkAndShowAvailability(uomFactor, availableQty, response);
		}
		//response.setSuccess(true);//CAP-44347
		return response;
	}
	
	//CAP-44347
	public void checkAndShowAvailability(int uomFactor, int availableQty, PNAResponse response) {
		if (uomFactor>0 && availableQty>0) {
			try {
				int dividedQty = availableQty / uomFactor;
				response.setAvailableQty(decimalFormatter.format(dividedQty));
			} catch (Exception ex) {
				logger.error(this.getClass().getName() + " - " + ex.getMessage(),ex);
			}
		}
	}
	
	//CAP-44347
	public void checkAvailability(boolean showAvailability, PNAResponse response, AppSessionBean asb,
			PriceAndAvailabilityVO vo, boolean isCustomDoc, String itemNumber) throws AtWinXSException {
		if (showAvailability) {

			try {
				response.setAvailabilityLabel(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), "availability_lbl"));
			} catch (AtWinXSException ae) {
				logger.error(this.getClass().getName() + " - " + ae.getMessage(), ae);
				response.setAvailabilityLabel("Status");
			}

			String availCode = "";
			// CAP-35247
			PricingAndAvailabilityUtil paUtil = new PricingAndAvailabilityUtil();

			try {

				IOEPricingAndAvailability pna = oePricingAndAvailabilityComponentLocatorService.locate(asb.getCustomToken());
				availCode = pna.getAvailableCode(vo, isCustomDoc);
			} catch (AtWinXSException ae) {
				logger.error(this.getClass().getName() + " - " + ae.getMessage(), ae);
				availCode = vo.getAvailabilityCode();
			}

			response.setAvailabilityCode(availCode);
			// CAP-35247 - Method is used to get AvailabilityStatus based on
			// availabilityCode
			// CAP-35911- added additional parameter to fetch the translation text into the
			// property file
			response.setAvailabilityStatus(paUtil.getItemAvailabilityStatus(availCode, asb));

			if (availCode.equals(OrderEntryConstants.AVAIL_CODE_NOT_AVAILABLE)) {

				boolean isHideBackorderedMsg = false;
				StandardOption standardOption = objectMapFactoryService.getEntityObjectMap().getEntity(StandardOption.class,
						asb.getCustomToken());

				try {

					isHideBackorderedMsg = OrderEntryUtil.isSuppressBackorderedStatusForBU(asb.getBuID())
							&& standardOption.isBatchOrder(itemNumber, asb.getSiteID());
				} catch (AtWinXSException e) {
					logger.error(this.getClass().getName() + " - " + e.getMessage(), e);
				}
				response.setIsReplaceBackorderedMsgFlag(Util.boolToY(isHideBackorderedMsg));
			}
		}
	}
	
	// CAP-48388
	public void checkBudgetAllocations(PNAResponse response, OEResolvedUserSettingsSessionBean userSettings,
			PriceAndAvailabilityVO vo, AppSessionBean asb, SessionContainer mainSession, OEOrderSessionBean osb)
			throws AtWinXSException {
		if (userSettings.isShowOrderLinePrice() && userSettings.isAllowBudgetAllocations()) {
			response.setEnabledAddToCartBudgetMessage("");
			OECheckoutAssembler checkoutAssembler = oeAssemblerFactoryService
					.getCheckoutAssembler(mainSession.getApplicationVolatileSession().getVolatileSessionBean(), asb);
			AllocationQuantitiesCompositeVO allocQty = checkoutAssembler.getAllocationQuantities(osb, asb);

			if (null == allocQty && !userSettings.isAllowOrderingWithoutBudget()) {
				response.setEnabledAddToCartBudget(false);
				response.setEnabledAddToCartBudgetMessage(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), "err_place_ords_budget_amt"));
			} else if (null == allocQty && userSettings.isAllowOrderingWithoutBudget()) {
				response.setEnabledAddToCartBudget(true);
			} else if (!userSettings.isAllowUnavailablePriceOrd()
					&& vo.getPriceType().equals(OrderEntryConstants.PRICING_TYPE_NOT_FOUND)
					&& vo.getItemSellPrice() < 0) {
				response.setEnabledAddToCartBudget(false);
				response.setEnabledAddToCartBudgetMessage(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), SFTranslationTextConstants.UNABLE_RETRIEVE_CART_VALUE_MSG));
			} else if (userSettings.isAllowUnavailablePriceOrd()
					&& vo.getPriceType().equals(OrderEntryConstants.PRICING_TYPE_NOT_FOUND)
					&& vo.getItemSellPrice() < 0) {
				response.setEnabledAddToCartBudget(true);
			} else if (vo.getItemSellPrice() >= 0
					&& (null != allocQty && (allocQty.getRemainingQuantity() - vo.getItemExtendedSellPrice() <= 0))
					&& !userSettings.isForceCreditCardOverAllcBudget()) {
				response.setEnabledAddToCartBudget(false);
				response.setEnabledAddToCartBudgetMessage(translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), SFTranslationTextConstants.INSUFFICIENT_REMAINING_BUDGET));
			} else if (userSettings.isForceCreditCardOverAllcBudget()) {
				response.setEnabledAddToCartBudget(true);
			}
		}
	}
}

