/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	04/19/23	Satishkumar A      CAP-39934	   Saved Order – Getting the list of saved orders into the saved order page
 * 	04/24/23	A Boomker			CAP-40002		Added multiple methods for saved order expansion
 *	04/26/23	A Boomker			CAP-39340		Add API to delete saved order
 *	04/26/23	A Boomker			CAP-39341		Add API to resume saved order
 *	04/28/23	A Boomker			CAP-40204/CAP-40206	Add handling for resume and delete saved order
 *	05/03/23	A Salcedo			CAP-39068		Updated translation viewName.
 *	05/04/23    Satishkumar A   	CAP-37503       API Build - Save Order assuming all data already saved
 *	05/17/23  	Satishkumar A   	CAP-40617		API Build - Save Order assuming all data already saved
 *	05/25/23	Satishkumar A   	CAP-40539		C1UX BE - API Fix - Punchout sessions and Shared IDs without a profile should throw 403 errors for saved order list requests
 *	05/31/23  	Satishkumar A   	CAP-40617		API Build - Save Order assuming all data already saved
 *	06/02/23	Satishkumar A   	CAP-40734		API Fix - Resume saved order must allow process to continue if order ID is present but no items are in the cart
 *	06/20/23	A Boomker			CAP-41121		API Change - resume warning message for stock items may render differently
 *	06/22/23	Satishkumar A		CAP-41308		API Fix - Saved Order Expand - Item image for items without an image should return null
 *	06/26/23	A Boomker			CAP-41640		Fix expand to not check invalid items
 */

package com.rrd.c1ux.api.services.orders.savedorders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.savedorders.SaveOrderRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SaveOrderResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderDeleteRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderDeleteResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderExpansionResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeRequest;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrderResumeResponse;
import com.rrd.c1ux.api.models.orders.savedorders.SavedOrdersResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.ItemUtility;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.framework.util.SessionOverrideBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OEExtendedItemQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEExtendedQuantityResponseBean;
import com.wallace.atwinxs.orderentry.ao.OEOrderAddressSessionBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderDetailsResponseBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderFormBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrdersFormBean;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.ao.OrderLineVOFilter;
import com.wallace.atwinxs.orderentry.dao.OrderLineDAO;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEReplacementsSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.session.SubscriptionCheckoutSessionBean;
import com.wallace.atwinxs.orderentry.session.SubscriptionSummarySessionBean;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVOKey;

//CAP-40617
@Service
public class SavedOrderServiceImpl extends BaseOEService  implements SavedOrderService {

	protected SavedOrderServiceImpl(TranslationService translationService) {
		super(translationService);
	}

	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	//CAP-39934
	@Override
	public SavedOrdersResponse getSavedOrdersList(SessionContainer sc) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		SavedOrdersResponse response = new SavedOrdersResponse();

		//CAP-40539
		validateSavedOrdersAccessible(sc, appSessionBean, oeSession);

		//CAP-39934 - Irrespective of Admin settings, it should retrieve "My Saved Orders Only".
		oeSession.getOESessionBean().getUserSettings().setSavedOrderVisibilityCd(OrderAdminConstants.SAVED_ORDER_VISIBILITY_OWN);
		OESavedOrdersFormBean savedOrdersBean = getSavedOrders(appSessionBean, oeSession, volatileSessionBean);
		response.setOeSavedOrdersFormBean(savedOrdersBean);

		/*
		// Commented this and kept it for future reference.
		boolean cartEmpty = volatileSessionBean.getShoppingCartCount()==0;
		Locale defaultLocale = appSessionBean.getDefaultLocale();
		//CP-8970 changed token from String to an Object
		CustomizationToken customToken = appSessionBean.getCustomToken();

		if (!cartEmpty)
		{
			// set error message to blank on initial load
			String savedOrderDeleteErrorMsg = Util.nullToEmpty(TranslationTextTag.processMessage(defaultLocale, customToken, ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR1_MSG));

			model.put(OrderEntryConstants.SAVED_ORDER_DELETE_ERROR, savedOrderDeleteErrorMsg);
		}
		else
		{
			model.put(OrderEntryConstants.SAVED_ORDER_DELETE_ERROR, "");
		}

		String deleteSuccessMessage = "";

		if (null != request.getParameter(OrderEntryConstants.SAVED_ORDER_DELETE_SUCCESS))
		{
			deleteSuccessMessage = Util.nullToEmpty(request.getParameter(OrderEntryConstants.SAVED_ORDER_DELETE_SUCCESS).toString());
			model.put(OrderEntryConstants.SAVED_ORDER_DELETE_SUCCESS, "");
		}

		String saveSuccessMessage = "";

		if (null != request.getParameter(OrderEntryConstants.SAVED_ORDER_SAVE_SUCCESS))
		{
			saveSuccessMessage = Util.nullToEmpty(request.getParameter(OrderEntryConstants.SAVED_ORDER_SAVE_SUCCESS).toString());
			model.put(OrderEntryConstants.SAVED_ORDER_SAVE_SUCCESS, "");
		}

		// set success message to blank on initial load
		model.put(OrderEntryConstants.SAVED_ORDER_DELETE_SUCCESS, deleteSuccessMessage);
		model.put(OrderEntryConstants.SAVED_ORDER_SAVE_SUCCESS, saveSuccessMessage);

		//CAP-11728
		String visibilityScopeLbl = Util.nullToEmpty(TranslationTextTag.processMessage(defaultLocale, customToken, TranslationTextConstants.TRANS_NM_SAVED_ORDERS_VISIBILITY_LBL));
		model.put("visibilityScopeLbl", visibilityScopeLbl);

		String mySavedOrdsLbl = Util.nullToEmpty(TranslationTextTag.processMessage(defaultLocale, customToken, TranslationTextConstants.TRANS_NM_MY_SAVED_ORDERS_LBL));
		model.put("mySavedOrdsLbl", mySavedOrdsLbl);

		String teamSavedOrdsLbl = Util.nullToEmpty(TranslationTextTag.processMessage(defaultLocale, customToken, TranslationTextConstants.TRANS_NM_TEAM_SHARED_ORDER_LBL));
		model.put("teamSavedOrdsLbl", teamSavedOrdsLbl);

		model.put("displaySavedOrdersVisDropdown", savedOrdersBean.isDisplaySavedOrdersVisDropdown());

		//CAP-12286
		int orderAttrID = oeSession.getOESessionBean().getUserSettings().getOrderAttributeSelected();

		model.put("orderAttrID", orderAttrID);

		String visibilityCd = oeSession.getOESessionBean().getUserSettings().getSavedOrderVisibilityCd();

		model.put("visibilityCd", visibilityCd);

		String ugOrderAttribute = getUGOrderAttributes(appSessionBean,volatileSessionBean, orderAttrID);


		model.put("ugOrderAttribute", ugOrderAttribute);

		//CAP-13413
		String visibilityOptionSelected = Util.nullToEmpty(request.getParameter(OrderEntryConstants.VISIBILITY_OPTION_SELECTED));
		model.put(OrderEntryConstants.VISIBILITY_OPTION_SELECTED, visibilityOptionSelected);
		*/

		Properties translationSavedOrders = translationService.getResourceBundle(appSessionBean, "savedOrders");//CAP-39068
		response.setTranslationSavedOrders(translationService.convertResourceBundlePropsToMap(translationSavedOrders));

		response.setSuccess(true);
		return response;
	}
	/**
	 * This method should check whether the user have access to saved orders.
	 *
	 * @param SessionContainer sc
	 * @param AppSessionBean appSessionBean
	 * @param OrderEntrySession oeSession
	 * @throws AccessForbiddenException
	 */
	//CAP-40539
	private void validateSavedOrdersAccessible(SessionContainer sc,AppSessionBean appSessionBean,OrderEntrySession oeSession) throws AccessForbiddenException {

		//CAP-40539
		if(isPunchoutSession(sc)) {
			logger.error(getErrorPrefix(appSessionBean), "from Punchout scenario, so don't have access to save order ", -1);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}
		if ((appSessionBean.isSharedID()) && (appSessionBean.getProfileNumber() == AtWinXSConstant.INVALID_PROFILE_NUMBER)) {
			logger.error(getErrorPrefix(appSessionBean), "is a SharedID and no profile selected, so don't have access to save order ", -1);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}
		if(!oeSession.getOESessionBean().getUserSettings().isShowSaveOrders()) {
			logger.error(getErrorPrefix(appSessionBean), "has isShowSaveOrders falg set to value 'No', so don't have access to save order ", -1);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}
		}

	/**
	 * This method gets the list of Saved Orders.
	 *
	 * @param SessionContainer sc
	 * @return Object
	 * @throws AtWinXSException
	 */
	//CAP-26547
	protected OESavedOrdersFormBean getSavedOrders(AppSessionBean appSessionBean, OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		OESavedOrdersFormBean savedOrdersBean = getSavedOrdersBean(appSessionBean, oeSession, volatileSessionBean, OrderEntryConstants.SAVED_ORDERS_SAVED_TIME_DESC);

		volatileSessionBean.setServiceID(AtWinXSConstant.ORDERS_SERVICE_ID); // CP-12549

		// Commented this and kept it for future reference.
		/*//CAP-11728
		boolean displaySavedOrdersVisDropdown = false;

		String visibilityCd = oeSession.getOESessionBean().getUserSettings().getSavedOrderVisibilityCd();

		if(visibilityCd.equals(OrderAdminConstants.SAVED_ORDER_VISIBILITY_ALL) || (savedOrdersBean.isAllowOrderSharing() && !visibilityCd.equals(OrderAdminConstants.SAVED_ORDER_VISIBILITY_OWN)))
		{
			displaySavedOrdersVisDropdown = true;
		}

		if(visibilityCd.equals(OrderAdminConstants.SAVED_ORDER_VISIBILITY_ALL))
		{
			savedOrdersBean.setReadOnly("Y");
		}

		savedOrdersBean.setDisplaySavedOrdersVisDropdown(displaySavedOrdersVisDropdown);
		*/
		return savedOrdersBean;

	}

	//CAP-39934
	/**
	 * This get the command class(OESavedOrdersFormBean)
	 *
	 * @return
	 * @throws AtWinXSException
	 */
	private OESavedOrdersFormBean getSavedOrdersBean(AppSessionBean appSessionBean, OrderEntrySession oeSession,VolatileSessionBean volatileSessionBean,   String sortBy) throws AtWinXSException
	{

		OESavedOrderAssembler assembler = new OESavedOrderAssembler(
				volatileSessionBean,
				appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());


		int currentOrderID = Integer.parseInt(OrderEntryConstants.ORDERID_DEFAULT_VALUDE);
		if(volatileSessionBean != null && volatileSessionBean.getOrderId() != null)
		{
			currentOrderID = volatileSessionBean.getOrderId().intValue();
		}

		OESavedOrdersFormBean savedOrdersBean = assembler.getSavedOrders(
				appSessionBean.getSiteID(),
				appSessionBean.getLoginID(),
				appSessionBean.getProfileNumber(),
				sortBy,
				currentOrderID,
				appSessionBean,
				oeSession.getOESessionBean().getUserSettings().getTeamOrdSharingSiteAttr(),
				oeSession.getOESessionBean().getUserSettings().getSavedOrderVisibilityCd()); //CAP-11728

		oeSession.setClearParameters(false);

		return savedOrdersBean;
	}

	//CAP-12286 PDN Get the attribute on UG for the Saved All Orders
	/*private String getUGOrderAttributes(AppSessionBean appSessionBean,VolatileSessionBean volatileSessionBean,int attrID) throws AtWinXSException
	{
		String option = "";
		//CAP-13383, for invalid ID -1, do not need to check
		if (attrID == AtWinXSConstant.INVALID_ID)
			return option;

		OESavedOrderAssembler assembler = getAssembler(appSessionBean, volatileSessionBean);
		SiteAttributesVO saVO = assembler.getUGAllSavedOrderAttribute(appSessionBean.getSiteID(), attrID);
		//CAP-13383, add null check
		if (saVO != null)
		{
			String allLbl = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_ALL_LBL));
			String viewOnlyLbl =  caseFirst(Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_VIEW_ONLY_LBL)).toLowerCase());
			option = allLbl + " " + saVO.getAttrDisplayName() + " (" + viewOnlyLbl + ")";
		}

		return option;
	}*/

	/**
	 * Returns the assembler
	 *
	 * @return
	 */
	protected OESavedOrderAssembler getAssembler(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
	{
		return new OESavedOrderAssembler(volatileSessionBean,
				appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());
	}

	/*private String caseFirst(String givenString) {
        String[] a= givenString.split(" ");
        StringBuilder s= new StringBuilder();
        for (int i = 0; i < a.length; i++) {
        s.append(Character.toUpperCase(a[i].charAt(0))).append(a[i].substring(1)).append(" ");
        }
      return s.toString().trim();
    }*/

	protected void validateOrderBelongsToUser(int orderID,AppSessionBean appSessionBean,
			OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		//CAP-39934 - Irrespective of Admin settings, it should retrieve "My Saved Orders Only".
		oeSession.getOESessionBean().getUserSettings().setSavedOrderVisibilityCd(OrderAdminConstants.SAVED_ORDER_VISIBILITY_OWN);

		if (appSessionBean.getProfileNumber() == AtWinXSConstant.INVALID_PROFILE_NUMBER)
		{
			logger.error(getErrorPrefix(appSessionBean), "has no profile and cannot get the expansion of order ID ", orderID);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}

		if (orderID == OrderEntryConstants.INVALID_ORDER_ID)
		{
			logger.error(getErrorPrefix(appSessionBean), "cannot get the expansion of order ID -1");
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}

		if (!isSavedOrderVisibleToUser(orderID, appSessionBean, oeSession, volatileSessionBean))
		{
			logger.error(getErrorPrefix(appSessionBean), "cannot view as a saved order ID ", orderID);
			throw new AccessForbiddenException(SavedOrderServiceImpl.class.getName());
		}
	}

	protected boolean isSavedOrderVisibleToUser(int orderID, AppSessionBean appSessionBean,
			OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean)
		throws AtWinXSException
	{
		if (!oeSession.getOESessionBean().getUserSettings().isShowSaveOrders())
		{
			return false;
		}

		try
		{
			String orderString = String.valueOf(orderID);
			OESavedOrdersFormBean savedOrdersBean = getSavedOrdersBean(appSessionBean, oeSession, volatileSessionBean, OrderEntryConstants.SAVED_ORDERS_SAVED_TIME_DESC);

			for (OESavedOrderFormBean savedOrder : savedOrdersBean.getOrders())
			{
				if (orderString.equals(savedOrder.getOrderID()))
				{
					return true;
				}
			}
		}
		catch(AtWinXSException e)
		{
			logger.error(getErrorPrefix(appSessionBean), "could not retrieve saved order list to look for specific saved order for session ", appSessionBean.getSessionID(), e);
		}
		return false;
	}

	protected OESavedOrderDetailsResponseBean loadSavedOrderDetails(int orderID, AppSessionBean appSessionBean,
			OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean, boolean checkInvalid) throws AtWinXSException // CAP-41640
	{
		OESavedOrderAssembler assembler = getAssembler(appSessionBean, volatileSessionBean);
		OESavedOrderDetailsResponseBean bean = assembler.getSavedOrderDetail(orderID,
				AtWinXSConstant.EMPTY_STRING, oeSession.getOESessionBean(), appSessionBean, appSessionBean.getDefaultTimeZone(), appSessionBean.getProfileNumber());
		bean.setOrderID(AtWinXSConstant.EMPTY_STRING + orderID);
		loadOrderNameOnSavedDetails(orderID, appSessionBean, bean, assembler);
		// CAP-41640 - only check invalid on resume saved order
		if (checkInvalid)
		{
			checkForBadLines(orderID, appSessionBean, oeSession, bean, assembler);
		}
		return bean;
	}

	protected void loadOrderNameOnSavedDetails(int orderID, AppSessionBean appSessionBean, OESavedOrderDetailsResponseBean orderDetailBean,
			OESavedOrderAssembler assembler) throws AtWinXSException
	{
		// CAP-40002 - moved from CP - if order name passed in is empty, we have to retrieve DB data again to generate it
		String orderName = orderDetailBean.getOrderName();
		if (Util.isBlankOrNull(orderName))
		{
			OrderHeaderVO orderHeader = assembler.getOrderHeader(new OrderHeaderVOKey(orderID));
			orderName = OrderEntryUtil.getSavedOrderName(orderHeader.getOrderName(), orderHeader.getLastChangeTimestamp(),
					appSessionBean.getDefaultLocale(), appSessionBean.getDefaultTimeZone());
			orderDetailBean.setOrderName(orderName);
		}
	}

	protected void checkForBadLines(int orderID, AppSessionBean appSessionBean, OrderEntrySession oeSession,
			OESavedOrderDetailsResponseBean orderDetailBean, OESavedOrderAssembler assembler) throws AtWinXSException
	{
			//CAP-15100
			ArrayList<OrderLineVOKey> badOrderLines = new ArrayList<>();
			OrderLineDAO orderLineDAO = new OrderLineDAO();
			OrderLineVO[] orderLines = orderLineDAO.getNonComponentOrderLines(new OrderHeaderVOKey(orderID));
			ArrayList<String> errorList = new ArrayList<>();
			assembler.checkForBadOrderLines(appSessionBean, appSessionBean.getLoginID(), oeSession.getOESessionBean().getUserSettings(),
					orderLines, errorList, badOrderLines);
			//CAP-15100
			if (!badOrderLines.isEmpty())
			{
			    OEExtendedQuantityResponseBean allItems  = orderDetailBean.getItems();
			    OEExtendedQuantityResponseBean badItems  = new OEExtendedQuantityResponseBean();
			    badItems.setXertComposition(allItems.isXertComposition());
			    ArrayList<OEExtendedItemQuantityResponseBean> orderableItems = new ArrayList<>();
			    ArrayList<OEExtendedItemQuantityResponseBean> itemsToBeRemoved = new ArrayList<>();

				// rearranged this to get in proper order of the order
				for (OEExtendedItemQuantityResponseBean item: orderDetailBean.getItems().getItems())
				{
					OrderLineVOKey key = new OrderLineVOKey(orderID, Util.safeStringToInt(item.getLineNumber()));
					if (!badOrderLines.contains(key))
					{
						setAddressCount(orderableItems, item, allItems);
					}
				}

				// CAP-15101 - bad order lines are in correct order
				for (OrderLineVOKey badOrderLine:badOrderLines)
				{
					setBadOrderLines(badOrderLine, orderDetailBean.getItems().getItems(), itemsToBeRemoved, badItems, appSessionBean, assembler, orderID);
				}

				OEExtendedItemQuantityResponseBean[] items = new OEExtendedItemQuantityResponseBean[orderableItems.size()];
			    allItems.setItems(orderableItems.toArray(items));
				items = new OEExtendedItemQuantityResponseBean[itemsToBeRemoved.size()];
			    badItems.setItems(itemsToBeRemoved.toArray(items));
			    orderDetailBean.setItems(allItems);
			    orderDetailBean.setItemsToBeRemoved(badItems);
			}
		}

	private void setBadOrderLines(OrderLineVOKey badOrderLine, OEExtendedItemQuantityResponseBean[] items,
			ArrayList<OEExtendedItemQuantityResponseBean> itemsToBeRemoved, OEExtendedQuantityResponseBean badItems,
			AppSessionBean appSessionBean, OESavedOrderAssembler assembler, int orderID) throws AtWinXSException {
		boolean foundThis = false;
		for (OEExtendedItemQuantityResponseBean item: items)
		{
			if (badOrderLine.getLineNum() == Util.safeStringToInt(item.getLineNumber()))
			{
				setAddressCount(itemsToBeRemoved, item, badItems);
				foundThis = true;
				break;
			}
		}
		loadMasterRecord(!foundThis, orderID, appSessionBean, badOrderLine, itemsToBeRemoved, assembler);
	}

	private void loadMasterRecord(boolean masterNotFound, int orderID, AppSessionBean appSessionBean, OrderLineVOKey badOrderLine,
			ArrayList<OEExtendedItemQuantityResponseBean> itemsToBeRemoved, OESavedOrderAssembler assembler) throws AtWinXSException {
		// add handling for masters to show in table
		if (masterNotFound)
		{
			OrderLineVO masterVO = assembler.getOrderLineDetails(orderID, badOrderLine.getLineNum());
			OrderLineVOFilter masterFilter = new OrderLineVOFilter(masterVO, appSessionBean.getDefaultLocale(), false, appSessionBean.getCurrencyLocale());
			OEExtendedItemQuantityResponseBean masterItem = new OEExtendedItemQuantityResponseBean(masterFilter);
			masterItem.setItemClassCode("MASTER"); // it doesn't matter if bundle or promo, just flag as master
			itemsToBeRemoved.add(masterItem);
		}
	}

	protected void setAddressCount(ArrayList<OEExtendedItemQuantityResponseBean> itemList, OEExtendedItemQuantityResponseBean item,
			OEExtendedQuantityResponseBean beanList) {
		itemList.add(item);
		if (item.getAddressCount()>0)
		{
			beanList.setAddressListCount(item.getAddressCount()+"");
			beanList.setListItemExists(true);
		}
	}

	protected void getSavedOrderExpansionDetails(SavedOrderExpansionResponse response, int orderID, AppSessionBean appSessionBean,
			OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		OESavedOrderDetailsResponseBean orderDetailBean = loadSavedOrderDetails(orderID, appSessionBean, oeSession,
				volatileSessionBean, false); 		// CAP-41640 - only check invalid on resume saved order
		response.setExpansion(orderDetailBean);
		updateImagesAndUOMsForC1UX(response, appSessionBean);
	}

	protected void updateImagesAndUOMsForC1UX(SavedOrderExpansionResponse response, AppSessionBean asb) throws AtWinXSException
	{
		OEExtendedItemQuantityResponseBean[] items = response.getExpansion().getItems().getItems();
		if ((items != null) && (items.length > 0))
		{
			Map<String, String> uomMap = new HashMap<>();
			response.setFullTextUomMap(uomMap);
			String ofLbl = AtWinXSConstant.BLANK_SPACE
					+ translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), "ofLbl")
					+ AtWinXSConstant.BLANK_SPACE;

			for (OEExtendedItemQuantityResponseBean item : items)
			{
				// Item Image path must reference C1UX no image instead of CP no image
				String itemImagePath = item.getItemImageURL();
				//CAP-41308
				if (Util.isBlankOrNull(itemImagePath) || ModelConstants.CP_NO_IMAGE.equalsIgnoreCase(itemImagePath)
						|| itemImagePath.contains(ModelConstants.CP_NO_IMAGE_NO_CONTEXT)) {
					item.setItemImageURL(null);
				}
				mapUOMForItem(uomMap, item, asb, ofLbl);
			}
		}
	}

	protected void mapUOMForItem(Map<String, String> uomMap, OEExtendedItemQuantityResponseBean item,
			AppSessionBean asb, String ofLbl) throws AtWinXSException {
		if (!Util.isBlankOrNull(item.getUOMCode()))
		{
			String uomCodeTranslated = ItemUtility.getUOMAcronyms(item.getUOMCode(), false, asb);
			uomMap.put(item.getLineNumber(), uomCodeTranslated + ofLbl + item.getUOMFactor());
		}
		else
		{
			uomMap.put(item.getLineNumber(), AtWinXSConstant.EMPTY_STRING);
		}
	}

	protected void updateDisplayLabels(SavedOrderExpansionResponse response, OEResolvedUserSettingsSessionBean settings)
	{
		if (response.getExpansion().getAddresses() != null)
		{
			determineShowBillingAttention(response.getExpansion().getAddresses().getBillingAddress(), response, settings);
			determineShowShippingAttention(response.getExpansion().getAddresses().getShippingAddress(), response, settings);
		}
		else
		{
			response.setShowBillToAttention(false);
			response.setShowShipToAttention(false);
		}
	}

	private void determineShowBillingAttention(OEOrderAddressSessionBean billingAddress,
			SavedOrderExpansionResponse response, OEResolvedUserSettingsSessionBean settings) {
		if ((billingAddress != null) && (settings.isShowBillToInfo())
				&& (!OrderEntryConstants.BILL_TO_ATTN_HIDDEN.equals(settings.getBillToAttnOption()))
				&& (!Util.isBlankOrNull(billingAddress.getAttention())))
		{
			response.setShowBillToAttention(true);
			response.setBillToAttentionLabel(settings.getBillToAttnLabel());
		}
		else
		{
			response.setShowBillToAttention(false);
		}
	}

	private void determineShowShippingAttention(OEOrderAddressSessionBean shippingAddress,
			SavedOrderExpansionResponse response, OEResolvedUserSettingsSessionBean settings) {
		if ((shippingAddress != null) && (settings.isShowShipToAttentionInd())
				&& (!Util.isBlankOrNull(shippingAddress.getAttention())))
		{
			response.setShowShipToAttention(true);
			response.setShipToAttentionLabel(settings.getShipToAttnLabel());
		}
		else
		{
			response.setShowShipToAttention(false);
		}
	}

	/**
	 * createExpansionDetails will take an order ID and attempt to get the expanded details after validating the requesting session has the rights to do so
	 * @param request
	 * @param sc
	 * @return SavedOrderExpansionResponse
	 */
	public SavedOrderExpansionResponse createExpansionDetails(SavedOrderExpansionRequest request, SessionContainer sc) throws AtWinXSException
	{
		SavedOrderExpansionResponse response = new SavedOrderExpansionResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		validateOrderBelongsToUser(request.getOrder(), appSessionBean, oeSession, volatileSessionBean);
		// By using the same logic CP did to validate that a saved order was visible to the user, this confirms it is in a valid
		// status that it would be shown to the user in the saved orders list. That already excludes punchout, submitted, and
		// other statuses that will not be visible in the saved orders list. So no separate status validation is needed
		try {
			getSavedOrderExpansionDetails(response, request.getOrder(), appSessionBean, oeSession, volatileSessionBean);
			updateDisplayLabels(response, oeSession.getOESessionBean().getUserSettings());
			response.setSuccess(true);
		}
		catch(AtWinXSException e)
		{
			logger.error(getErrorPrefix(appSessionBean), "could not retrieve saved order details for session ", appSessionBean.getSessionID(), e);
		}

		return response;
	}

	// CAP-39340
	/**
	 * deleteOrder will take an order ID and attempt to delete it after validating the requesting session has the rights to do so
	 * @param request
	 * @param sc
	 * @return SavedOrderDeleteResponse
	 */
	@Override
	public SavedOrderDeleteResponse deleteOrder(SavedOrderDeleteRequest request, SessionContainer sc)
			throws AtWinXSException {
		SavedOrderDeleteResponse response = new SavedOrderDeleteResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		validateOrderBelongsToUser(request.getOrder(), appSessionBean, oeSession, volatileSessionBean);
		// By using the same logic CP did to validate that a saved order was visible to the user, this confirms it is in a valid
		// status that it would be shown to the user in the saved orders list. That already excludes punchout, submitted, and
		// other statuses that will not be visible in the saved orders list. So no separate status validation is needed
		try {
			deleteSavedOrder(request.getOrder(), appSessionBean, volatileSessionBean);
			response.setSuccess(true);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_SAVED_ORDER_SUCCESS_MSG));
		}
		catch(AtWinXSException e)
		{
			logger.error(getErrorPrefix(appSessionBean), "could not delete saved order ID ", request.getOrder(), e);
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					ErrorCodeConstants.ERR_NAME_COPY_ORDER_ERROR1_MSG));
		}
		return response;
	}

	protected void deleteSavedOrder(int orderID, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		OESavedOrderAssembler assembler = getAssembler(appSessionBean, volatileSessionBean);
		assembler.deleteSavedOrder(orderID);
	}

	// CAP-39341
	/**
	 * resumeOrder will take an order ID and attempt to resume it after validating the requesting session has the rights to do so
	 * @param request
	 * @param sc
	 * @return SavedOrderResumeResponse
	 */
	@Override
	public SavedOrderResumeResponse resumeOrder(SavedOrderResumeRequest request, SessionContainer sc)
			throws AtWinXSException {
		SavedOrderResumeResponse response = new SavedOrderResumeResponse();
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		validateOrderBelongsToUser(request.getOrder(), appSessionBean, oeSession, volatileSessionBean);
		// By using the same logic CP did to validate that a saved order was visible to the user, this confirms it is in a valid
		// status that it would be shown to the user in the saved orders list. That already excludes punchout, submitted, and
		// other statuses that will not be visible in the saved orders list. So no separate status validation is needed
		//CAP-40734
		OESavedOrderDetailsResponseBean orderDetailBean = validateResumeOrder(response, request.getOrder(), appSessionBean, oeSession,
				volatileSessionBean, request.isOverrideWarning(),sc);
		if (orderDetailBean != null)
		{
			try {
				resumeSavedOrder(request.getOrder(), orderDetailBean, appSessionBean, oeSession, volatileSessionBean, appSession);
				response.setSuccess(true);
			}
			catch(AtWinXSException e)
			{
				setResumeOrderFailureCodes(response, true);
				logger.error(getErrorPrefix(appSessionBean), "could not delete saved order ID ", request.getOrder(), e);
			}
		}
		return response;
	}

	protected void setResumeOrderFailureCodes(SavedOrderResumeResponse response, boolean hardStop)
	{
		response.setSuccess(false);
		response.setHardStop(hardStop);
		response.setPromptForContinue(!hardStop);
	}

	protected OESavedOrderDetailsResponseBean validateResumeOrder(SavedOrderResumeResponse response, int orderID, AppSessionBean appSessionBean,
			OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean, boolean overrideWarning, SessionContainer sc)
		throws AtWinXSException
	{
		//CAP-40734
		if ((hasValidCart(sc)))
		{
			setResumeOrderFailureCodes(response, true);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					SFTranslationTextConstants.SAVED_ORDER_CURRENT_CART_ERR));
			return null;
		}

		try
		{
			OESavedOrderDetailsResponseBean orderDetailBean = loadSavedOrderDetails(orderID, appSessionBean, oeSession,
					volatileSessionBean, true); 		// CAP-41640 - only check invalid on resume saved order
			if (orderDetailBean != null)
			{
				if ((orderDetailBean.getItems() == null) || (orderDetailBean.getItems().getItems() == null)
						|| (orderDetailBean.getItems().getItems().length == 0))
				{
					setResumeOrderFailureCodes(response, true);
					response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
							SFTranslationTextConstants.SAVED_ORDER_CANNOT_CONTINUE_ERR));
					return null;
				}
				else if (!overrideWarning &&(orderDetailBean.getItemsToBeRemoved() != null) && (orderDetailBean.getItemsToBeRemoved().getItems() != null)
						&& (orderDetailBean.getItemsToBeRemoved().getItems().length > 0))
				{
					setResumeOrderFailureCodes(response, false);
					buildErrorForInvalidItems(orderDetailBean, response, appSessionBean);
					return null;
				}
				else
				{
					return orderDetailBean;
				}
			}
		}
		catch(AtWinXSException e)
		{
			logger.error(getErrorPrefix(appSessionBean), "could not retrieve saved order list to look for specific saved order for session ", appSessionBean.getSessionID(), e);
		}
		setResumeOrderFailureCodes(response, true);
		response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				SFTranslationTextConstants.LOAD_ORDER_ERR));
		return null;
	}

	protected void buildErrorForInvalidItems(OESavedOrderDetailsResponseBean orderDetailBean,
			SavedOrderResumeResponse response, AppSessionBean appSessionBean) {
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.SAVED_ORDER_CONTINUE_ORD_NAME_TAG, orderDetailBean.getOrderName());
		replaceMap.put(SFTranslationTextConstants.SAVED_ORDER_CONTINUE_ITEM_LIST_TAG,
				buildErrorItemList(orderDetailBean.getItemsToBeRemoved().getItems(), appSessionBean.showWCSSItemNumber())); // CAP-41121

		response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.SAVED_ORDER_CONTINUE_PROMPT_ERR,
				SFTranslationTextConstants.SAVED_ORDER_CONTINUE_PROMPT_ERR_DEF, replaceMap));
	}

	protected String buildErrorItemList(OEExtendedItemQuantityResponseBean[] items, boolean showVendorItemNumber)  // CAP-41121
	{
		StringBuilder error = new StringBuilder();
		for (OEExtendedItemQuantityResponseBean item : items)
		{
			if (error.length() > 0)
			{
				error.append(", ");
			}
			// CAP-41121 - change logic to handle stock items too
			error.append(item.getItemDescription());
			String itemNumberToDisplay = item.getItemNumber();
			if ((Util.isBlankOrNull(itemNumberToDisplay)) && showVendorItemNumber && (!Util.isBlankOrNull(item.getVendorItemNumber())))
			{
				itemNumberToDisplay = item.getVendorItemNumber();
			}

			if (!Util.isBlankOrNull(itemNumberToDisplay))
			{
				error.append(AtWinXSConstant.BLANK_SPACE)
					.append("(").append(itemNumberToDisplay).append(")");
			}
		}
		return error.toString();
	}

	// CAP-40206 - based on SavedOrderDetailsController.handleContinueOrder(…)
	protected void resumeSavedOrder(int orderID, OESavedOrderDetailsResponseBean orderDetailBean, AppSessionBean appSessionBean,
			OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean, ApplicationSession appSession) throws AtWinXSException
	{
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		clearSubscriptionSession(oeSessionBean);
		//determine the checkout page to be shown.
		oeSessionBean.setContinuingSavedOrder(true);
		oeSessionBean.setOrderSharingDefaultsHaveBeenSet(true);

		// for now did not copy ItemsByAddresses section due to scope of 1b release

		OESavedOrderAssembler assembler = getAssembler(appSessionBean, volatileSessionBean);
		processOrderOverrides(orderDetailBean.getOrderID(), appSession, assembler);

		// for now did not copy enforce on ordering section due to scope of 1b release
		// for now did not copy budget allocation section due to scope of 1b release

		String orderName = Util.nullToEmpty(orderDetailBean.getOrderName());
		String orderSavedByInd = Util.nullToEmpty(orderDetailBean.getFormSelectedOrderSavedByInd());

					//load up originator login and profile
		String originatorLoginID = Util.nullToEmpty(appSessionBean.getOriginatorProfile().getLoginID());
		int originatorProfileNum = appSessionBean.getOriginatorProfile().getProfileNumber();

		    //Set order id only if order is good for ordering.
	    volatileSessionBean.setOrderId(orderID);
		OEReplacementsSessionBean replacements = new OEReplacementsSessionBean(
							OrderEntryConstants.REPLACEMENT_SELECTION_TYPE_SAVED, volatileSessionBean.getOrderId().intValue(),
							orderName, orderSavedByInd);
		ArrayList<String> errorList = new ArrayList<>();

	    assembler.continueSavedOrder(oeSessionBean, orderName, orderSavedByInd, appSessionBean.getProfileNumber(),
				    		originatorLoginID, originatorProfileNum,
							replacements, appSessionBean, volatileSessionBean, errorList, true); //CAP-10599

		// for now did not copy refresh catalog for profile selections with enforce on catalog section due to scope of 1b release

		if (replacements.getItems().isEmpty() && !errorList.isEmpty())
		{ //forward to shopping cart page
			logger.error("Errors found on continue order despite allowing continue override!", errorList);
			throw new AtWinXSException(errorList.toString(), this.getClass().getName());
		}
		else if (!replacements.getItems().isEmpty())
		{ //CAP-24710
			oeSession.setReplacements(replacements);
			logger.error("Replacements found on continue order despite allowing continue override!", errorList);
			throw new AtWinXSException(errorList.toString(), this.getClass().getName());
		}

		OEShoppingCartAssembler asm = new OEShoppingCartAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate());
		asm.setShoppingCartCount(volatileSessionBean, appSessionBean.getCustomToken());
		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
		//Save the session to persist changes
		ApplicationVolatileSession appVolatileSession = (ApplicationVolatileSession)SessionHandler.loadSession(appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
		appVolatileSession.getVolatileSessionBean().setOrderId(orderID);
		appVolatileSession.getVolatileSessionBean().setShoppingCartCount(volatileSessionBean.getShoppingCartCount());
		SessionHandler.saveSession(appVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
		SessionHandler.saveSession(appSession, appSessionBean.getSessionID(), AtWinXSConstant.APPSESSIONSERVICEID);

	}

	/**
	 * This method will determine if we need to retrieve session overrides for the order or if we can use
	 * the existing login session overrides.
	 * @param orderId - The String holding the orderId we are looking for order overrides for.
	 * @param assembler - The {@link OESavedOrderAssembler} that we will use for processing overrides.
	 * @throws AtWinXSException
	 */
	private void processOrderOverrides(String orderId, ApplicationSession applicationSession, OESavedOrderAssembler assembler) throws AtWinXSException
	{
		AppSessionBean appSessionBean = applicationSession.getAppSessionBean();
		// Check the order to see if it has a token - if so, we need to load up session overrides for the order
		SessionOverrideBean orderSessionOverrides = assembler.getOrderSessionOverrides(orderId, appSessionBean);

		// If orderSessionOverrides is not null, save it to  here
		if (orderSessionOverrides != null)
		{
			// Set the session
			appSessionBean.setOrderSessionOverrides(orderSessionOverrides);
			appSessionBean.setUseLoginSessionOverrides(false);

			SessionHandler.saveSession(
					applicationSession,
					appSessionBean.getSessionID(),
					applicationSession.getServiceID());
		}
	}

	/**
	 * Clear subscription from the session
	 *
	 * @param oeSession
	 */
	private void clearSubscriptionSession(OEOrderSessionBean oeSession)
	{
		SubscriptionSummarySessionBean subscriptionSession = oeSession.getSubscriptionSummarySession();
		if (subscriptionSession != null)
		{
		    oeSession.setSubscriptionSummarySession(null);
			SubscriptionCheckoutSessionBean subscriptionCheckoutSession = oeSession.getSubscriptionCheckoutSession();
			if (subscriptionCheckoutSession != null)
			{
				oeSession.setSubscriptionCheckoutSession(null);
			}
		}
	}

	//CAP-37503
	//CAP-40617
	/**
	 * saveOrder will take an order name and attempt to save it after validating the requesting session has the rights to do so
	 * @param request
	 * @param sc
	 * @return SaveOrderResponse
	 */
	@Override
	public SaveOrderResponse saveOrder(SaveOrderRequest request, SessionContainer sc)
			throws AtWinXSException {
		SaveOrderResponse response = new SaveOrderResponse();
		//CAP-40617
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		ApplicationVolatileSession appVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = appVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		String orderName = request.getOrderName();
		OESavedOrderAssembler assembler = getAssembler(appSessionBean,volatileSessionBean);

		//CAP-40617
		validateSavedOrdersAccessible(sc, appSessionBean, oeSession);

		if(!validateOrder(response,sc,appSessionBean))
			return response;
		if(!validateSavedOrderName(response, appSessionBean,  orderName))
			return response;

		assembler.saveOrder(oeOrderSessionBean, orderName, appSessionBean, null, -1);
		oeSession.clearOrder(volatileSessionBean, appSessionBean);

		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant. ORDERS_SERVICE_ID);
		appVolatileSession.getVolatileSessionBean().setOrderId(null);
		appVolatileSession.getVolatileSessionBean().setShoppingCartCount(0);
		SessionHandler.saveSession(appVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant. APPVOLATILESESSIONID);
		SessionHandler.saveSession(appSession, appSessionBean.getSessionID(), AtWinXSConstant. APPSESSIONSERVICEID);

		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.SAVED_ORDER_NAME_REPLACEMENT_TAG, orderName);

		response.setMessage(buildErrorMessage(TranslationTextConstants.TRANS_NM_SAVED_ORDER_UPDATE_SUCCESS_MSG,appSessionBean,replaceMap));
		response.setSuccess(true);

		return response;
}
	//CAP-40617
	private boolean validateOrder(SaveOrderResponse response, SessionContainer sc, AppSessionBean appSessionBean) throws AtWinXSException {
		if (!hasValidCart(sc))
		{
			response.setSuccess(false);
			response.setMessage(buildErrorMessage(SFTranslationTextConstants.NO_ITEM_SELECTED_ERR,appSessionBean,null));
			return false;
		}
		else if (isSessionOrderSubmittedCannotEdit(sc))
		{
			response.setSuccess(false);
			response.setMessage(buildErrorMessage(SFTranslationTextConstants.ORDER_NOT_ELIGIBLE_TO_SAVE_ERR,appSessionBean,null));
			return false;
		}
		return true;
	}
		//CAP-40617
		protected boolean isPunchoutSession(SessionContainer sc)  {
			return sc.getApplicationSession().getAppSessionBean().isPunchout();
		}
		//CAP-40617
		protected boolean validateSavedOrderName(SaveOrderResponse response, AppSessionBean appSessionBean, String orderName) throws AtWinXSException {
			if ((orderName == null) || (orderName.trim().length() == 0))
			{
				response.setSuccess(false);
				response.setMessage(buildErrorMessage(ErrorCodeConstants.ERR_SAVED_ORDER_NO_NAME_MSG,appSessionBean,null));
				return false;
			} else if(orderName.trim().length() > ModelConstants.SAVED_ORDER_NAME_VALID_MAX_LENGTH ) {
				response.setSuccess(false);
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.SAVED_ORDER_NAME_VALID_MAX_LENGTH + AtWinXSConstant.EMPTY_STRING);
				response.setMessage(buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR,appSessionBean,replaceMap));

				return false;

			}
			return true;

		}
		//CAP-40617
		private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap) throws AtWinXSException
		{
			return Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(),
					asb.getCustomToken(), errorKey, replaceMap));
		}

}
