/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/13/24	L De Leon			CAP-46960				Initial Version
 *	02/26/24	Satishkumar A		CAP-47325				C1UX BE - Create API - Remaining Budget Allocations Order Entry
 *	03/28/24	N Caceres			CAP-47795				Add validation for Budget Allocation	
 *	03/21/24	L De Leon			CAP-47969				Refactored code into method getBudgetAllocation()
 *	04/05/24	Krishna Natarajan	CAP-48419				Added additional check on getBannerMessage method to throw error based on admin settings
 *  04/09/24	Krishna Natarajan	CAP-48511				Removed access forbidden check on remaining budget
 *  04/23/24	T Harmon			CAP-48796				Added new method for budget allocation
 *  05/16/24	Krishna Natarajan	CAP-49429				Added logic for exceed budget message and setting session value
 */
package com.rrd.c1ux.api.services.budgetallocation;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.budgetallocation.AllocationSummaryResponse;
import com.rrd.c1ux.api.models.budgetallocation.BudgetAllocationResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.entity.BudgetAllocation;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.orderentry.admin.vo.AllocationQuantitiesCompositeVO;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEOrderSummaryResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.session.AllocationSummaryBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class BudgetAllocationServiceImpl extends BaseOEService implements BudgetAllocationService {

	private static final Logger logger = LoggerFactory.getLogger(BudgetAllocationServiceImpl.class);
	//CAP-47325
	private final ShoppingCartService mShoppingCartService;

	protected BudgetAllocationServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService, ShoppingCartService shoppingCartService) {
		super(translationService, objectMapFactoryService);
		mShoppingCartService = shoppingCartService;
	}

	@Override
	public BudgetAllocationResponse getBannerMessage(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();

		if (!userSettings.isAllowBudgetAllocations() || !userSettings.isAllowAllocationsInd()) {//CAP-48419
			throw new AccessForbiddenException(this.getClass().getName());
		}

		BudgetAllocationResponse response = new BudgetAllocationResponse();
		response.setSuccess(true);

		// CAP-47969 refactored code into a method for reusability
		BudgetAllocation budgetAllocation = getBudgetAllocation(appSessionBean, userSettings);

		// These are the actual Budget Amount and Period values. Changed to Currency Locale
		String currentBudgetAmt = Util.getStringFromCurrency(budgetAllocation.getCurrentBudgetAmount(),
				appSessionBean.getCurrencyLocale(), appSessionBean.getApplyExchangeRate(), 2).getAmountText();
		String periodStartDate = Util.getStringFromDate(budgetAllocation.getPeriodStartDate(),
				appSessionBean.getDefaultLocale());
		String periodEndDate = Util.getStringFromDate(budgetAllocation.getPeriodEndDate(),
				appSessionBean.getDefaultLocale());

		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.BUDGET_AMOUNT_REPLACE_TAG, currentBudgetAmt);

		String budgetMessage = getTranslation(appSessionBean, SFTranslationTextConstants.BUDGET_ALLOCATION_MSG,
				SFTranslationTextConstants.BUDGET_ALLOCATION_DEF_MSG, replaceMap);
		response.setBudgetMessage(budgetMessage);

		if (!Util.isBlankOrNull(periodStartDate) && !Util.isBlankOrNull(periodEndDate)) {
			replaceMap.put(SFTranslationTextConstants.START_DATE_REPLACE_TAG, periodStartDate);
			replaceMap.put(SFTranslationTextConstants.END_DATE_REPLACE_TAG, periodEndDate);

			String periodMessage = getTranslation(appSessionBean,
					SFTranslationTextConstants.BUDGET_ALLOCATION_PERIOD_MSG,
					SFTranslationTextConstants.BUDGET_ALLOCATION_PERIOD_DEF_MSG, replaceMap);
			response.setTimeframeMessage(periodMessage);
		}

		return response;
	}

	// CAP-47969
	public BudgetAllocation getBudgetAllocation(AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings) {
		int siteID = appSessionBean.getSiteID();
		int buID = appSessionBean.getBuID();
		int profileNumber = appSessionBean.getProfileNumber();

		int allocGroupAttrId = userSettings.getBudgetAllocGroupAttrId();
		String allocationsLevelCd = userSettings.getAllocationsLevelCode();
		String timeframeCd = userSettings.getBudgetAllocTimeFrameCode();

		String budgetAllocationLvl = userSettings.getBudgetAllocationLevelCd();

		BudgetAllocation budgetAllocation = null;
		try {
			budgetAllocation = objectMapFactoryService.getEntityObjectMap()
			.getEntity(BudgetAllocation.class, appSessionBean.getCustomToken());
			budgetAllocation.populateWithNoOrder(siteID, buID, profileNumber, allocGroupAttrId, allocationsLevelCd,
					timeframeCd, buildI18nBean(appSessionBean), budgetAllocationLvl);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return budgetAllocation;
	}
	
	//CAP-47325
	@Override
	public AllocationSummaryResponse getRemainingBudgetAllocations(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		int orderScenarioNr = oeOrderSessionBean.getOrderScenarioNumber();
		
		AllocationSummaryResponse response = new AllocationSummaryResponse();
		response.setSuccess(true);

		OEShoppingCartFormBean formBean = mShoppingCartService.loadShoppingCart(sc, null, false);
		
		//Calculate remaining budget
		AllocationSummaryBean allocSummaryBean = null;

		AllocationQuantitiesCompositeVO allocQty = getAllocQty(volatileSessionBean, appSessionBean, oeOrderSessionBean, oeAssemblerFactoryService);

		if (allocQty != null)
		{
			OEShoppingCartAssembler assembler = 
					oeAssemblerFactoryService.getShoppingCartAssembler(appSessionBean.getCustomToken(), 
																appSessionBean.getDefaultLocale(),
																appSessionBean.getApplyExchangeRate());

			allocSummaryBean = assembler.calculateRemainingBudget(formBean, punchoutSessionBean, userSettings, appSessionBean, volatileSessionBean, orderScenarioNr);
			formBean.setShowAllocation(true);
			//CAP-23031
			if((allocQty.getRemainingQuantity() <= 0 && userSettings.isForceCreditCardOverAllcBudget() && formBean.getItems() != null && formBean.getItems().length > 0) ||
					(userSettings.isForceCreditCardOverAllcBudget() && allocSummaryBean != null && allocSummaryBean.getEstRemainingBudgetDoubleVal() < 0))
			{
				oeSession.getOESessionBean().setForceCCOptionAllocation(true);//CAP-23237
				SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
				
				response.setBudgetAllocationWarnMsg(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "exceedBudgetWarnMsg"));
				response.setExceedBudgetWarned(true);
				response.setSuccess(true);//CAP-48380

			}
		}
		//CAP-23031 New scenario when user has no allocations, display warning right away.
		else if(userSettings.isAllowBudgetAllocations() && userSettings.isForceCreditCardOverAllcBudget() && !userSettings.isAllowOrderingWithoutBudget() && formBean.getItems() != null && formBean.getItems().length > 0)
		{
			oeSession.getOESessionBean().setForceCCOptionAllocation(true);//CAP-23237
			SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
			
			response.setBudgetAllocationWarnMsg(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "exceedBudgetWarnMsg"));
			response.setExceedBudgetWarned(true);
			response.setSuccess(false);

		}else {
			oeSession.getOESessionBean().setForceCCOptionAllocation(false);//CAP-49429	
			SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
		}
		
		response.setAllocationSummaryBean(allocSummaryBean);
		return response;
	}
	
	//CAP-48796 TH - New method for budget allocation
	@Override
	public AllocationSummaryResponse getRemainingBudgetAllocationsSummary(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		int orderScenarioNr = oeOrderSessionBean.getOrderScenarioNumber();
				
		AllocationSummaryResponse response = new AllocationSummaryResponse();
		response.setSuccess(true);		
		response.setBudgetAllocationWarnMsg("");
		
		//Calculate remaining budget
		AllocationSummaryBean allocSummaryBean = null;

		AllocationQuantitiesCompositeVO allocQty = getAllocQty(volatileSessionBean, appSessionBean, oeOrderSessionBean, oeAssemblerFactoryService);

		OEShoppingCartFormBean formBean = mShoppingCartService.loadShoppingCart(sc, null, false);
		boolean isCreditCardOrder = checkForCreditCardOrder(volatileSessionBean.getOrderId(), appSessionBean);					
		
		if (allocQty != null)
		{	
			allocSummaryBean = calculateRemainingBudgetSummary(oeOrderSessionBean, formBean, punchoutSessionBean, userSettings, appSessionBean, volatileSessionBean, orderScenarioNr);
			
			formBean.setShowAllocation(true);
			
			//CAP-23031
			if((allocQty.getRemainingQuantity() <= 0 && userSettings.isForceCreditCardOverAllcBudget() && formBean.getItems() != null && formBean.getItems().length > 0 && !isCreditCardOrder) ||
					(userSettings.isForceCreditCardOverAllcBudget() && allocSummaryBean != null && allocSummaryBean.getEstRemainingBudgetDoubleVal() < 0 && !isCreditCardOrder))
			{			
				oeOrderSessionBean.setForceCCOptionAllocation(true);
				SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);//CAP-49429
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), "exceedBudgetErrMsg")
						+AtWinXSConstant.BLANK_SPACE+ translationService.processMessage(appSessionBean.getDefaultLocale(),
								appSessionBean.getCustomToken(), SFTranslationTextConstants.EXCEED_BUDGET_ERR_MSG_FOR_CC));//CAP-49429
				response.setExceedBudgetWarned(true);
				response.setSuccess(false);//CAP-48380

			}			
			else if((allocQty.getRemainingQuantity() <= 0 && !userSettings.isForceCreditCardOverAllcBudget() && formBean.getItems() != null && formBean.getItems().length > 0) ||
				(!userSettings.isForceCreditCardOverAllcBudget() && allocSummaryBean != null && allocSummaryBean.getEstRemainingBudgetDoubleVal() < 0))		
			{			
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "sf.insufficientRemainingBudget"));
				response.setExceedBudgetWarned(true);
				response.setSuccess(false);
			}
		}
		
		response.setAllocationSummaryBean(allocSummaryBean);
		return response;
	}
	
	//CAP-48796 - New method for budget allocation
	protected boolean checkForCreditCardOrder(int orderID, AppSessionBean appSessionBean) throws AtWinXSException
	{
		Order currentOrder = objectMapFactoryService.getEntityObjectMap()
				.getEntity(Order.class, appSessionBean.getCustomToken());
		
		currentOrder.populate(orderID);
		
		boolean isCreditCardOrder = false;
		if (currentOrder.getPaymentVerification() != null && !Util.isBlankOrNull(currentOrder.getPaymentVerification().getTypeCode()))
		{
			isCreditCardOrder = true;
		}
		
		return isCreditCardOrder;
	}
	
	//CAP-48796 TH - new method for summary budget allocation
	protected AllocationSummaryBean calculateRemainingBudgetSummary(OEOrderSessionBean oeSessionBean, OEShoppingCartFormBean formBean, PunchoutSessionBean punchoutSessionBean, OEResolvedUserSettingsSessionBean userSettings, 
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, int orderScenarioNumber) throws AtWinXSException
	{
	    AllocationSummaryBean summaryBean = null;
        //CP-2520 don't get allocation summary bean for punchout, since budget allocations are out of scope
        if (formBean !=null && punchoutSessionBean == null)
        {
        	// CP-2911 - moved broker's codes here.
            //check order if subscription, order from a file and/or distribution list
        	IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());        	
        	boolean isInvalidBudgetScenario = ordersService.isInvalidBudgetOrderScenario(orderScenarioNumber);        	        
        	
        	// CP-2911 - Added condition here to calculate remaining budget
            if((userSettings.isAllowAllocationsInd() && userSettings.isAllowBudgetAllocations()) &&
    		        !isInvalidBudgetScenario)
    		{
    	        OECheckoutAssembler checkoutAssembler = new OECheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), 
    	                appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
    	
    	    	// We need to get the summary bean here, so it gets the remaining budget against the full order
            	// get form field values from DB
                OEOrderSummaryResponseBean oeSummaryBean = checkoutAssembler.getOrderSummary(
                        appSessionBean, 
                        oeSessionBean, 
                        true); // CP-160 added to calculate postage
    	        
    	        summaryBean = checkoutAssembler.calculateRemainingBudget(null, oeSummaryBean, userSettings, appSessionBean);
    		}
        }
        
        return summaryBean;
	}
	
	//CAP-47325
	protected AllocationQuantitiesCompositeVO getAllocQty(VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSessionBean, OEAssemblerFactoryService oeAssemblerFactoryService) throws AtWinXSException {
		
		OECheckoutAssembler checkoutAssembler = oeAssemblerFactoryService.getCheckoutAssembler(volatileSessionBean, appSessionBean);
		return checkoutAssembler.getAllocationQuantities(oeOrderSessionBean, appSessionBean);
	}

	// CAP-47969
	public String validateBudgetAllocations(OEOrderSessionBean oeOrderSessionBean, AppSessionBean appSessionBean,
			boolean isRedirectToCart) {
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		String budgetDepletedErrorMsg = AtWinXSConstant.EMPTY_STRING;
		if(userSettings.isAllowAllocationsInd() && isRedirectToCart) {
			BudgetAllocation budgetAllocation = getBudgetAllocation(appSessionBean, userSettings);

			if (null != budgetAllocation && null != budgetAllocation.getCurrentBudget()
					&& budgetAllocation.getCurrentBudget().getRemainingQuantity() <= 0
					&& !userSettings.isForceCreditCardOverAllcBudget()) {
				String periodStartDate = Util.getStringFromDate(budgetAllocation.getPeriodStartDate(),
						appSessionBean.getDefaultLocale());
				String periodEndDate = Util.getStringFromDate(budgetAllocation.getPeriodEndDate(),
						appSessionBean.getDefaultLocale());

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.START_DATE_REPLACE_TAG, periodStartDate);
				replaceMap.put(SFTranslationTextConstants.END_DATE_REPLACE_TAG, periodEndDate);
				budgetDepletedErrorMsg = getTranslation(appSessionBean,
						SFTranslationTextConstants.BUDGET_ALLOCATION_DEPLETED_ERR_MSG,
						SFTranslationTextConstants.BUDGET_ALLOCATION_DEPLETED_DEF_ERR_MSG, replaceMap);
			}
		}
		return budgetDepletedErrorMsg;
	}
	
	
}