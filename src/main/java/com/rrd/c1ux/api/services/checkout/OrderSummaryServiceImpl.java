/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/03/23				A Boomker				CAP-39512					Initial Version
 *	05/18/23				Sakthi M				CAP-40547					C1UX BE - API Fixes - Error checks for valid and non-submitted order not coded in Delivery and Order info load APIs
 *	05/16/23				A Boomker				CAP-40687					Moved validateOrder to BaseOEService
 *  05/17/23				A Boomker				CAP-40687					Created constants for simple checkout view names
 *  06/13/23				A Boomker				CAP-38154					Changed to use the view name FE applied for order info section
 *	11/10/23				L De Leon				CAP-44841					Added code to update Submit Order label in order summary translation map if demo user
 *	04/15/24				N Caceres				CAP-48487					Added code to display expedited fee if required
 *	04/23/24				Krishna Natarajan		CAP-48866					Added method getNotesTranslationMap to get notes added on validating Is OderRequest flag 
 *	05/01/24				L De Leon				CAP-48972					Modified populateSummaryFromOrder() method to set allowOrderTemplate in the response
 *	05/03/24				Krishna Natarajan		CAP-49130					Handled replaceMap for notes in getNotesTranslationMap 
 *	07/01/24				Krishna Natarajan		CAP-49811					Handled the total and grandtotal to get updated as TBD if the printing price is less than 0
 */
package com.rrd.c1ux.api.services.checkout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.checkout.OrderSummaryResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderLine;
import com.rrd.custompoint.orderentry.entity.OrderPricing;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSCurrency;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderDueDateVO;

@Service
public class OrderSummaryServiceImpl extends BaseOEService implements OrderSummaryService {

	private static final Logger logger = LoggerFactory.getLogger(OrderSummaryServiceImpl.class);

	private static final String TBD_PRICE = "TBD";
	
	protected final OEManageOrdersComponentLocatorService manageOrdersComponentLocatorService;

	protected OrderSummaryServiceImpl(TranslationService translationService, 
			OEManageOrdersComponentLocatorService manageOrdersComponentLocatorService) {
		super(translationService);
		this.manageOrdersComponentLocatorService = manageOrdersComponentLocatorService;
	}

	@Override
	public OrderSummaryResponse populateSummaryFromOrder(Order order, boolean review, SessionContainer sc)  throws AtWinXSException {
		OrderSummaryResponse response = new OrderSummaryResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		if (!validateOrder(response, sc, appSessionBean))
		{
			return response;
		}

		try {
			
			// punchout cannot go through checkout so must always be treated as non-Review mode
			if (sc.getApplicationSession().getPunchoutSessionBean() != null)
			{
				review = false;
			}

			if (order == null)
			{
				order = updatePrices(sc, null, review);
			}
			response.setOrderRequest(order.isOrderRequest());//CAP-48866
			
			populateOrderSummaryTranslation(appSessionBean, response);

			response.setItemCount(order.getOrderLines().getNonKitComponentCount());

			OEResolvedUserSettingsSessionBean userSettings = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean().getUserSettings();
			if (userSettings.isShowOrderLinePrice())
			{
				response.setDisclaimers(userSettings.getDisclaimer());

				OrderPricing pricing = order.getOrderPricing();
				if (pricing != null)
				{
					populateGeneralPricingFields(response, pricing, review);

					if (review)
					{
						populateSummaryPricingFields(response, pricing);
					}
				}
				// CAP-48487
				populateExpeditedFee(appSessionBean, response, order.getOrderID());
				//CAP-49216
				populateGrandTotalAmount(response, appSessionBean);
				checkAndSetTBDForTotals(order,response);//CAP-49811
			}
			response.setAllowOrderTemplate(userSettings.isShowTemplatesLink()); // CAP-48972
			response.setSuccess(true);
		}
		catch(AtWinXSException e)
		{
			genericResponseFailure(response, appSessionBean);
		}
		return response;
	}
	
	//CAP-49811
	private void checkAndSetTBDForTotals(Order order, OrderSummaryResponse response) throws AtWinXSException {
		for (OrderLine ordLns: order.getOrderLines().getOrderLines()) {
			if(ordLns.getExtendedSellPrice()<0) {
				response.setTotalItemPrice(TBD_PRICE);
				response.setGrandTotalPrice(TBD_PRICE);
				break;
			}
		}
	}

	private void populateGeneralPricingFields(OrderSummaryResponse response, OrderPricing pricing, boolean review) {
		response.setTotalItemPrice((pricing.getTotalItemPrice() != null) ? pricing.getTotalItemPrice().getAmountText() : TBD_PRICE);
		if (!review)
		{
			response.setGrandTotalPrice(response.getTotalItemPrice());
		}
		else
		{
			response.setGrandTotalPrice((pricing.getGrandTotalPrice() != null) ? pricing.getGrandTotalPrice().getAmountText() : TBD_PRICE);
		}
	}

	private void genericResponseFailure(OrderSummaryResponse response, AppSessionBean appSessionBean) throws AtWinXSException {
		response.setSuccess(false);
		response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				"sf.cannotCompleteRequest"));
	}

	private void populateSummaryPricingFields(OrderSummaryResponse response, OrderPricing pricing) {
		if (pricing.getTaxPrice() != null)
		{
			response.setTaxPrice((pricing.getTaxPrice() != null) ? pricing.getTaxPrice().getAmountText() : TBD_PRICE);
		}

		if (pricing.getShippingPrice() != null)
		{
			response.setShippingPrice((pricing.getShippingPrice() != null) ? pricing.getShippingPrice().getAmountText() : TBD_PRICE);
		}

		if (pricing.getOrderAndLinePrice() != null)
		{
			response.setOrderAndLinePrice((pricing.getOrderAndLinePrice() != null) ? pricing.getOrderAndLinePrice().getAmountText() : TBD_PRICE);
		}
	}

	@Override
	public Order updatePrices(SessionContainer sc, Order order, boolean review) throws AtWinXSException {
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();

		if (hasValidCart(sc))
		{
			if (order == null)
			{
				order = ObjectMapFactory.getEntityObjectMap().getEntity(Order.class, appSessionBean.getCustomToken());
				order.populateWithLatest(volatileSessionBean.getOrderId());
				order.getOrderLines();
			}

			OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
			order.setUpdatePriceOnValidLinesOnly(true);
			if (!userSettings.isShowOrderLinePrice())
			{
				return order;
			}

			if (review)
			{
				calculateReviewPricing(userSettings, order, appSessionBean);
			}
			else
			{
				calculateNonReviewPricing(userSettings, order, appSessionBean);
			}
			return order;
		}
		return null;
	}

	private void calculateReviewPricing(OEResolvedUserSettingsSessionBean userSettings, Order order,
			AppSessionBean appSessionBean) {
		try {
			order.calculatePricing(appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getSiteID(), appSessionBean.getDefaultLocale(),
					userSettings, appSessionBean.getLoginID());
		}
		catch (Exception e)
		{
			logger.error("Failed to calculate review pricing" + e.getMessage(), e);
			OrderPricing pricing = ObjectMapFactory.getEntityObjectMap().getEntity(OrderPricing.class, appSessionBean.getCustomToken());
			order.setOrderPricing(pricing);
		}
	}

	private void calculateNonReviewPricing(OEResolvedUserSettingsSessionBean userSettings, Order order,
			AppSessionBean appSessionBean) {
		OrderPricing pricing = order.getOrderPricing();
		if (pricing == null)
		{
			pricing = ObjectMapFactory.getEntityObjectMap().getEntity(OrderPricing.class, appSessionBean.getCustomToken());
		}
		order.setOrderPricing(pricing);

		try {
			pricing.calculateTotalItemPriceOnlyC1UX(appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getSiteID(),
					appSessionBean.getDefaultLocale(), userSettings.isShowPostage(), order, userSettings);
		}
		catch (Exception e)
		{
			logger.error("Failed to calculate review pricing" + e.getMessage(), e);
		}
	}

	protected void populateOrderSummaryTranslation(AppSessionBean asb, OrderSummaryResponse response) throws AtWinXSException
	{
		// CAP-40687 - updated to use constants
		Properties resourceBundlePropsDelivery = translationService.getResourceBundle(asb, SFTranslationTextConstants.CHECKOUT_DELIVERY_SECTION_VIEW_NAME);
		Properties resourceBundlePropsFramework = translationService.getResourceBundle(asb, SFTranslationTextConstants.CHECKOUT_FRAMEWORK_SECTION_VIEW_NAME);
		Properties resourceBundlePropsInfo = translationService.getResourceBundle(asb, SFTranslationTextConstants.DELIVERY_INFO_VIEW_NAME); // CAP-38154 - changing to view name used
		Properties resourceBundlePropsSummary = translationService.getResourceBundle(asb, SFTranslationTextConstants.CHECKOUT_SUMMARY_SECTION_VIEW_NAME);
		Properties translationNotes= translationService.getResourceBundle(asb, "translationNotes");
		response.setTranslationDelivery(translationService.convertResourceBundlePropsToMap(resourceBundlePropsDelivery));
		response.setTranslationFramework(translationService.convertResourceBundlePropsToMap(resourceBundlePropsFramework));
		response.setTranslationInfo(translationService.convertResourceBundlePropsToMap(resourceBundlePropsInfo));
		response.setTranslationSummary(getOrderSummaryTranslationMap(asb, resourceBundlePropsSummary, response)); // CAP-44841
		
		if (response.isOrderRequest()) {//CAP-48866
			response.setTranslationNotes(getNotesTranslationMap(asb, translationNotes));
		}else {
			response.setTranslationNotes(Collections.emptyMap());
		}
	}

	// CAP-44841
	protected Map<String, String> getOrderSummaryTranslationMap(AppSessionBean asb,
			Properties resourceBundlePropsSummary, OrderSummaryResponse response) {

		Map<String, String> summaryTranslationMap = translationService
				.convertResourceBundlePropsToMap(resourceBundlePropsSummary);
		if (asb.isDemoUser()) {
			summaryTranslationMap.put(SFTranslationTextConstants.SUBMIT_ORDER_TRANS_NAME,
					getTranslation(asb, SFTranslationTextConstants.DEMO_SUBMIT_ORDER_LABEL,
							SFTranslationTextConstants.DEMO_SUBMIT_ORDER_DEF_TXT));
		} else if(response.isOrderRequest()){
			summaryTranslationMap.put(TranslationTextConstants.TRANS_NM_SUBMIT_ORDER_REQ_BTN,
					getTranslation(asb, TranslationTextConstants.TRANS_NM_SUBMIT_ORDER_REQ_BTN,
							TranslationTextConstants.TRANS_NM_SUBMIT_ORDER_REQ_BTN));
		}
		return summaryTranslationMap;
	}
	
	// CAP-48487
	protected void populateExpeditedFee(AppSessionBean appSessionBean, OrderSummaryResponse response, int orderID) throws AtWinXSException {
		IOEManageOrdersComponent ordersService = manageOrdersComponentLocatorService.locate(appSessionBean.getCustomToken());
		OrderDueDateVO orderDueDate = ordersService.getOrderDueDate(orderID);
		
		if (null != orderDueDate && orderDueDate.isExpeditedOrder() && orderDueDate.getExpeditedOrderFee() > 0) 
		{
			response.setExpeditedOrderFee(Util.getStringFromCurrency(orderDueDate.getExpeditedOrderFee(),
					appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate(), 2).getAmountText());
			response.setDisclaimers(response.getDisclaimers() + RouteConstants.SINGLE_SPACE + translateExpeditedDisclaimer(appSessionBean));
		}
	}

	private String translateExpeditedDisclaimer(AppSessionBean appSessionBean) throws AtWinXSException {
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.EXPEDITED_DATE_LABEL_TAG, translationService.processMessage(appSessionBean.getDefaultLocale(), 
				appSessionBean.getCustomToken(), SFTranslationTextConstants.EXPEDITED_DATE_LABEL));
		return translationService.processMessage(appSessionBean.getDefaultLocale(), 
				appSessionBean.getCustomToken(), SFTranslationTextConstants.EXPEDITED_DISCLAIMER_MSG, replaceMap);
	}
	
	// CAP-48866 //CAP-49130
	protected Map<String, String> getNotesTranslationMap(AppSessionBean asb, Properties translationNotes)
			throws AtWinXSException {
		Map<String, String> summaryTranslationMap = translationService
				.convertResourceBundlePropsToMap(translationNotes);
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put("{isDemo}",
				asb.isDemoUser() 
						? translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
								TranslationTextConstants.TRANS_NM_DEMO_LBL) + " "
						: "");
		summaryTranslationMap.put(TranslationTextConstants.TRANS_NM_ORDER_REQ_DISCLAIM_MSG,
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
						TranslationTextConstants.TRANS_NM_ORDER_REQ_DISCLAIM_MSG, replaceMap));
		summaryTranslationMap.put(TranslationTextConstants.TRANS_NM_LIST_DET_FIRST_CLASS_MSG,
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
						TranslationTextConstants.TRANS_NM_LIST_DET_FIRST_CLASS_MSG));
		summaryTranslationMap.put(TranslationTextConstants.TRANS_NM_LIST_DET_PREMIUM_SRVC_MSG,
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
						TranslationTextConstants.TRANS_NM_LIST_DET_PREMIUM_SRVC_MSG));
		return summaryTranslationMap;
	}
	
	//CAP-49216
	protected void populateGrandTotalAmount(OrderSummaryResponse response, AppSessionBean appSessionBean)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(response.getExpeditedOrderFee()) && Util
				.getCurrencyFromString(response.getExpeditedOrderFee(), appSessionBean.getCurrencyLocale()) > 0) {
			double itemtot = Util.getCurrencyFromString(response.getTotalItemPrice(),
					appSessionBean.getCurrencyLocale());
			double expfees = Util.getCurrencyFromString(response.getExpeditedOrderFee(),
					appSessionBean.getCurrencyLocale());
			double grandtot = itemtot + expfees;
			XSCurrency printGrandtot = Util.getStringFromCurrency(grandtot, appSessionBean.getCurrencyLocale(), false);
			response.setGrandTotalPrice(printGrandtot.getAmountText());
		}
	}
}
