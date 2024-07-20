/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	------------------------------------------------------------------------------------
 *	02/14/24	S Ramachandran		 CAP-47145	Initial Version. created service to return list of orders used in current allocation
 */

package com.rrd.c1ux.api.services.singleitem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.singleitem.ItemAllocationOrder;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsRequest;
import com.rrd.c1ux.api.models.singleitem.SingleItemDetailAllocationsResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.ItemServiceComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.ItemValidationComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.OAOrderAdminLocatorService;
import com.rrd.c1ux.api.services.items.locator.SiteComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.orderentry.entity.ItemAllocation;
import com.rrd.custompoint.orderentry.entity.ItemAllocations;
import com.rrd.custompoint.services.interfaces.IItemServicesComponent;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.SiteBUGroupLoginProfileVO;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IItemValidation;
import com.wallace.atwinxs.interfaces.IOrderAdmin;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.orderentry.admin.vo.UserGroupOrderPropertiesVO;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

@Service
public class SingleItemDetailAllocationsServiceImpl extends BaseOEService  implements SingleItemDetailAllocationsService {

	private static final Logger logger = LoggerFactory.getLogger(SingleItemDetailAllocationsServiceImpl.class);
	
	private final OAOrderAdminLocatorService oaOrderAdminLocatorService;
	private final SiteComponentLocatorService siteComponentLocatorService;
	private final ItemServiceComponentLocatorService itemServiceComponentLocatorService;
	private final ItemValidationComponentLocatorService itemValidationComponentLocatorService;

	protected SingleItemDetailAllocationsServiceImpl(TranslationService translationService, 
			ObjectMapFactoryService objectMapFactoryService, 
			OAOrderAdminLocatorService oaOrderAdminLocatorService,
			SiteComponentLocatorService siteComponentLocatorService,
			ItemServiceComponentLocatorService itemServiceComponentLocatorService,
			ItemValidationComponentLocatorService itemValidationComponentLocatorService
			) {
		super(translationService, objectMapFactoryService);
		this.oaOrderAdminLocatorService = oaOrderAdminLocatorService;
		this.siteComponentLocatorService = siteComponentLocatorService;
		this.itemServiceComponentLocatorService = itemServiceComponentLocatorService;
		this.itemValidationComponentLocatorService = itemValidationComponentLocatorService;
	}

	//CAP-47145
	@Override
	public SingleItemDetailAllocationsResponse retrieveItemAllocations(SessionContainer sc, 
			SingleItemDetailAllocationsRequest request) throws AtWinXSException {
		
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		
		SingleItemDetailAllocationsResponse response = new SingleItemDetailAllocationsResponse();
		response.setSuccess(true);
		
		try 
		{
			
			if (asb.getProfileNumber() > AtWinXSConstant.INVALID_ID && 
					oeSessionBean.getUserSettings().isAllowItemQtyAllocation()) { 
			
				//validate customer item number and vendor item number
				if(!validateItemInformation(asb, request, response)) {
					
					if (response.getFieldMessages().size() > 0) {
						return response;
					}	
					else {	
						response.setMessage(buildTranslationMessage(SFTranslationTextConstants.INVALID_ITEM_SERVICE_ERR, asb, null));
						response.setSuccess(false);
						return response;
					}
				}
					
				ItemAllocations itemAllocs = objectMapFactoryService.getEntityObjectMap().getEntity(ItemAllocations.class, 
						asb.getCustomToken());
				itemAllocs.populateItemAllocations(asb, oeSessionBean.getUserSettings(), 
						request.getCustomerItemNumber(), request.getVendorItemNumber());
				if(null != itemAllocs.getItemAllocations() && !itemAllocs.getItemAllocations().isEmpty())	{
	
					List<ItemAllocationOrder> itemAllocationOrderLst = convertToResult(asb, itemAllocs.getItemAllocations());
					response.setItemAllocationOrderList(itemAllocationOrderLst);
				}
			}

		} catch(AtWinXSMsgException msgEx) {
			
			logger.error(this.getClass().getName() + " - " + msgEx.getMessage(),msgEx);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.INVALID_ITEM_SERVICE_ERR, asb, null));
			response.setSuccess(false);
		}
		return response;
	}
	
	
	//CAP-47145
	private List<ItemAllocationOrder> convertToResult(AppSessionBean appSessionBean, 
			Collection<ItemAllocation> itemAllocations)
	{
		String dateFormat = Util.getDateFormatForLocale(appSessionBean.getDefaultLocale());
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		SimpleDateFormat sdfInDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<ItemAllocationOrder> itemAllocationOrderLst = new ArrayList<>();

		for(ItemAllocation itemAlloc : itemAllocations)
		{
			ItemAllocationOrder itemAllocationOrder = new ItemAllocationOrder();
			itemAllocationOrder.setSalesRefNumber(Util.nullToEmpty(itemAlloc.getSalesRefNo()));
			itemAllocationOrder.setRequestorName(Util.nullToEmpty(itemAlloc.getRequestorName()));
			
			Integer itemQtyInEachOfOneFactor = itemAlloc.getUOMFactor() * itemAlloc.getOrderQt();
			itemAllocationOrder.setQuantityOrdered(itemQtyInEachOfOneFactor.toString());
			itemAllocationOrder.setOrderDate(sdfInDateTime.format(itemAlloc.getOrderedDate()));
			itemAllocationOrder.setOrderDateDisplay(sdf.format(itemAlloc.getOrderedDate()));
			
			itemAllocationOrderLst.add(itemAllocationOrder);
		}
		return itemAllocationOrderLst;
	}
	
	//CAP-47145
	private boolean validateItemInformation(AppSessionBean asb, SingleItemDetailAllocationsRequest request, 
			SingleItemDetailAllocationsResponse response) throws AtWinXSException {
		
		String fieldNameCust = ModelConstants.ALLOC_CUST_ITEM_NUMBER_FIELD_NAME;
		String labelCust = buildTranslationMessage(SFTranslationTextConstants.CUST_ITEM_NUMBER_LBL, asb, null);
		
		String fieldNameVendor = ModelConstants.ALLOC_VENDOR_ITEM_NUMBER_FIELD_NAME;
		String labelVendor = buildTranslationMessage(SFTranslationTextConstants.VENDOR_ITEM_NUMBER_LBL, asb, null);
		
		if ((Util.isBlankOrNull(request.getCustomerItemNumber()) && Util.isBlankOrNull(request.getVendorItemNumber())) ||
				(!Util.isBlankOrNull(request.getCustomerItemNumber()) && !Util.isBlankOrNull(request.getVendorItemNumber()))) {
		
			if(Util.isBlankOrNull(request.getCustomerItemNumber())) {
			
				response.getFieldMessages().put(fieldNameCust, labelCust + AtWinXSConstant.BLANK_SPACE + 
						buildTranslationMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if(request.getCustomerItemNumber().length() > ModelConstants.MAX_LENGTH_CUSTOMER_ITEM_NUMBER) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, 
						ModelConstants.MAX_LENGTH_CUSTOMER_ITEM_NUMBER + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldNameCust, 
						labelCust + AtWinXSConstant.BLANK_SPACE +
						buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
			
			if(Util.isBlankOrNull(request.getVendorItemNumber())) {
				
				response.getFieldMessages().put(fieldNameVendor, 
						labelVendor + AtWinXSConstant.BLANK_SPACE + 
						buildTranslationMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if(request.getVendorItemNumber().length() > ModelConstants.MAX_LENGTH_VENDOR_ITEM_NUMBER) {
				
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.MAX_LENGTH_VENDOR_ITEM_NUMBER + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldNameVendor, 
						labelVendor + AtWinXSConstant.BLANK_SPACE +
						buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}
		 	
		if(!Util.isBlankOrNull(request.getCustomerItemNumber()) &&  
				request.getCustomerItemNumber().length() > ModelConstants.MAX_LENGTH_CUSTOMER_ITEM_NUMBER) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, 
					ModelConstants.MAX_LENGTH_CUSTOMER_ITEM_NUMBER + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldNameCust, 
					labelCust + AtWinXSConstant.BLANK_SPACE +
					buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
		
		if(!Util.isBlankOrNull(request.getVendorItemNumber()) &&
				request.getVendorItemNumber().length() > ModelConstants.MAX_LENGTH_VENDOR_ITEM_NUMBER) {
			
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					ModelConstants.MAX_LENGTH_VENDOR_ITEM_NUMBER + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldNameVendor, 
					labelVendor + AtWinXSConstant.BLANK_SPACE +
					buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
		
		if(response.getFieldMessages().size() > 0) {
			response.setSuccess(false);
			return false;
		}
		
		ISite site = siteComponentLocatorService.locate(asb.getCustomToken());
		LoginVOKey key = new LoginVOKey(asb.getSiteID(), asb.getLoginID());
		SiteBUGroupLoginProfileVO userSettings = site.getSessionSettings(key);
		
		// We first need to get both the customer item number and vendor item number.
		IItemServicesComponent iItemServiceComponent = itemServiceComponentLocatorService.locate(asb.getCustomToken());			
		ItemRptVO itemRptVO = iItemServiceComponent.getWcssItemInformation(userSettings, 
				request.getVendorItemNumber().toUpperCase(), request.getCustomerItemNumber().toUpperCase());
		
		
		// Declare a collection to hold CP error codes
		Collection<ErrorCode> errorCodes = new ArrayList<>();
		// Get the ItemValidationComponent here
		IItemValidation iItemValidation = itemValidationComponentLocatorService.locate(asb.getCustomToken());
		
		
		// Get user group order properties VO here
		IOrderAdmin orderAdmin = oaOrderAdminLocatorService.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		UserGroupVOKey userGroupVOKey = new UserGroupVOKey(asb.getSiteID(), asb.getBuID(),
				asb.getGroupName());
		UserGroupOrderPropertiesVO ugOrderProperties = orderAdmin.getGroupPlaceOrderDetails(userGroupVOKey);
		
		// Validate the item available for user's Catalog here
		return iItemValidation.isItemValidForCatalog(userSettings.getSiteID(), 
				userSettings.getBuID(), 
				userSettings.getLoginID(),
				userSettings.getProfileNumber(),
				userSettings.getUserGroupName(), 
				itemRptVO.getCustomerItemNumber(), 
				itemRptVO.getWallaceItemNumber(), 
				false,	// Pass false for isForEventOrder, since services won't take event orders directly 
				ugOrderProperties.isSearchCatalogItemsOnlyInd(), 
				errorCodes);
 	}
	
	protected String buildTranslationMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap) 
		throws AtWinXSException {
		
		return Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(),
				asb.getCustomToken(), errorKey, replaceMap));
	}
	
}
