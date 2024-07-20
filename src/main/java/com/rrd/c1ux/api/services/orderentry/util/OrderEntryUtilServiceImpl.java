/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/16/2023  N Caceres		CAP-39045	Initial version
 *	06/07/23	A Boomker		CAP-38154	Methods converted from CP for C1UX validation
 */
package com.rrd.c1ux.api.services.orderentry.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryServiceImpl;
import com.rrd.c1ux.api.services.translation.C1UXTranslationService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.entity.Order;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.orderentry.component.OEManageOrdersHelper;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.locator.OEShoppingCartComponentLocator;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.KitComponentOrderLinePlusVO;
import com.wallace.atwinxs.orderentry.vo.KitMasterOrderLineVO;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderLineExtended2VO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVOKey;

@Service
public class OrderEntryUtilServiceImpl implements OrderEntryUtilService {

	private static final Logger logger = LoggerFactory.getLogger(OrderEntryUtilServiceImpl.class);

	public ObjectMapFactoryService getObjectMapFactoryService() {
		return new ObjectMapFactoryServiceImpl();
	}

	public TranslationService getTranslationService() {
		return new C1UXTranslationService();
	}

	@Override
	public boolean currentlyWithinKit(OrderEntrySession oeSession, VolatileSessionBean volatileSessionBean)
	{
		return OrderEntryUtil.currentlyWithinKit(oeSession, volatileSessionBean);
	}

	// CAP-38154 - AAB - copied from OEManageOrdersComponent for translation handling
	/**
	 * This method will enforce state restrictions on an order.  Based on the state restrictions, it will determine the lines that do not meet them.
	 * If validateOnly is true, it will send a warning on the appropriate lines. If validateOnly is false, it will remove the appropriate order lines
	 * from an order if they do not pass the state restriction check.
	 * @param orderHeaderVO - The OrderHeaderVO object for the order being checked.
	 * @param shipToAddressVO - The OrderAddressVO object for the ship to address.
	 * @param orderLineVOs - The OrderLineExtended2VO[] array for the order lines for the order
	 * @param userSettings - The OEResolvedUserSettingsSessionBean object with user settings.
	 * @param user - The String of the user peforming the state restriction test.
	 * @param volatileSessionBean
	 * @return - Returns the OrderHeaderWithBillingVO object with an updated scenario.
	 * @throws AtWinXSException
	 */
	public boolean enforceStateRestrictions(
			int orderID,
		AppSessionBean asb,
		ApplicationVolatileSession volatileSession,
		boolean validateOnly,
		Collection<String> errMsgs)
	throws AtWinXSException
	{
		boolean invalidLinesFound = false;
		// Get the Order Header for the order
		IOEManageOrdersComponent moComponent = getOEManageOrdersComponent(asb);
		OrderHeaderVOKey headerKey = new OrderHeaderVOKey(orderID);
		OrderHeaderVO orderHeaderVO = moComponent.getOrderHeader(headerKey);
		OrderAddressVO shipToAddressVO = moComponent.getShippingAddress(new OrderShippingVOKey(orderID));
		// remove invalid items due to state restrictions

		//DTS 8471 only check state restrictions on order scenarios not using Dist. Lists
		if ((shipToAddressVO != null)					//If order scenario 0, 1, 9, 10, 11, 12, 15 validate the state restrictions
			&& OEManageOrdersHelper.stateRestrictionsApply(orderHeaderVO.getOrderScenarioNum()))
		{
			OrderLineExtended2VO[] orderLineVOs = moComponent.getOrderLinesExtended2(headerKey);

			String stateCode = shipToAddressVO.getState().trim();
			OrderLineVO[] invalidLines = OEManageOrdersHelper.checkStateRestrictions(asb.getSiteID(),
					stateCode,
					orderLineVOs,
					asb.getCustomToken());

			// if invalid items exist, loop through and delete from order
			if (invalidLines != null && (invalidLines.length > 0))
			{
				invalidLinesFound = true;
				Collection<OrderLineVO> finalList = getFinalInvalidLineNumbers(orderLineVOs, invalidLines, headerKey);

				if (validateOnly)
				{ // only warn - do not do the delete immediately
					generateStateRestrictionWarning(finalList, asb, true, errMsgs, stateCode);
				}
				else
				{  // go ahead and delete lines
					removeInvalidLines(finalList, asb, moComponent);
					updateModifiedOrderScenario(orderHeaderVO.getOrderID().intValue(), volatileSession, asb);
					generateStateRestrictionWarning(finalList, asb, false, errMsgs, stateCode);
				} // end else when have to remove invalid items
			} // end if invalid items exist
		} // end if shipping address not null

		// return the order header vo here
		return invalidLinesFound;
	}

	private void generateStateRestrictionWarning(Collection<OrderLineVO> finalList, AppSessionBean asb, boolean warningOnly,
			Collection<String> errMsgs, String stateCode) throws AtWinXSException {
		TranslationService service = getTranslationService();
		for (OrderLineVO invalidLine : finalList)
		{
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.STATE_RESTRICTION_FIELD_TAG_STATE, stateCode);
			replaceMap.put(SFTranslationTextConstants.STATE_RESTRICTION_FIELD_TAG_ITEM, invalidLine.getItemDescriptionTxt());
			if (OrderEntryConstants.KIT_LN_TYPE_CD_MASTER.equals(invalidLine.getKitLineTypeCode()))
			{
				if (warningOnly)
				{
					errMsgs.add(service.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
							SFTranslationTextConstants.STATE_RESTRICTION_WARNING_KIT_MASTER, replaceMap));
				}
				else
				{
					errMsgs.add(service.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
							SFTranslationTextConstants.STATE_RESTRICTION_NOTIFICATION_KIT_MASTER, replaceMap));
				}
			}
			else if (Util.isBlankOrNull(invalidLine.getKitLineTypeCode()))
			{
				if (warningOnly)
				{
					errMsgs.add(service.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
							SFTranslationTextConstants.STATE_RESTRICTION_WARNING_NON_KIT, replaceMap));
				}
				else
				{
					errMsgs.add(service.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
							SFTranslationTextConstants.STATE_RESTRICTION_NOTIFICATION_NON_KIT, replaceMap));
				}
			}
		}
	}

	private void removeInvalidLines(Collection<OrderLineVO> finalList, AppSessionBean asb, IOEManageOrdersComponent moComponent) throws AtWinXSException {
		String user = asb.getLoginID();
		for (OrderLineVO invalidLine : finalList)
		{

			// call method to delete the line from the order
			try
			{
				// DTS #10026 - added logging of all possible deletes of the custom doc order line
				if ((invalidLine.getCustomDocLineKey() != null) && (invalidLine.getCustomDocLineKey().getCustomDocLineID() > 0))
				{
					logDeletingCustDocLine(invalidLine, asb);
				}
				moComponent.deleteOrderLine(invalidLine.getKey(), user);
			}
			catch (AtWinXSWrpException we)
			{
				if (AtWinXSException.WAL_NORECORDSAFFECTED_SQLEXCEPTION != we.getExtendedErrorCode())
				{
					throw we;
				}
			}
		}
	}

	private void logDeletingCustDocLine(OrderLineVO invalidLine, AppSessionBean asb) {
		StringBuilder logThis = new StringBuilder("CDOrderLine DELETE: C1UX enforcing state restrictions will be calling line delete() for order#:");
		logThis.append(String.valueOf(invalidLine.getOrderID()))
			.append(" and order line#")
				.append(String.valueOf(invalidLine.getLineNum()))
						.append(" and cust doc order line#")
								.append(String.valueOf(invalidLine.getCustomDocLineKey().getCustomDocLineID()))
										.append(" by login:")
												.append(asb.getLoginID())
														.append(" on site:")
																.append(String.valueOf(asb.getSiteID()))
																		.append(" with sessionID:")
																				.append(String.valueOf(asb.getSessionID()));
		String messageToLog = logThis.toString();
		logger.info(messageToLog);
	}

	public IOEManageOrdersComponent getOEManageOrdersComponent(AppSessionBean asb) throws AtWinXSException {
		return OEManageOrdersComponentLocator.locate(asb.getCustomToken());
	}

	protected void updateModifiedOrderScenario(int orderID, ApplicationVolatileSession volatileSession, AppSessionBean asb) throws AtWinXSException {
		Order order = getObjectMapFactoryService().getEntityObjectMap().getEntity(Order.class, asb.getCustomToken());
		order.populate(orderID);
		order.calculateOrderScenarioNumberForOrderService();
		IOEShoppingCartComponent scComponent = OEShoppingCartComponentLocator.locate(asb.getCustomToken());
		scComponent.saveOrderScenarioNumber(new OrderHeaderVOKey(orderID), order.getScenarioNumber());
		if ((volatileSession != null) && (volatileSession.getVolatileSessionBean() != null))
		{
			volatileSession.getVolatileSessionBean().setShoppingCartCount(order.getShoppingCartCount());
			SessionHandler.saveSession(volatileSession, asb.getSessionID(), AtWinXSConstant. APPVOLATILESESSIONID);
		}
	}

	protected Collection<OrderLineVO> getFinalInvalidLineNumbers(OrderLineExtended2VO[] orderLineVOs, OrderLineVO[] invalidLines, OrderHeaderVOKey orderKey) throws AtWinXSException
	{
		Map<Integer, OrderLineVO> finalInvalidLineNumbers = new HashMap<>();
		identifyFinalInvalidLines(finalInvalidLineNumbers, invalidLines, orderKey, orderLineVOs);

		Collection<OrderLineVO> finalList = new ArrayList<>();
        Iterator<Integer> iterator = finalInvalidLineNumbers.keySet().iterator();
        while(iterator.hasNext())
        {
        	finalList.add(finalInvalidLineNumbers.get(iterator.next()));
        }

		return finalList;
	}

	private void handleKitInvalidLines(Map<Integer, OrderLineVO> finalInvalidLineNumbers, OrderLineVO invalidLineVO,
			OrderHeaderVOKey orderKey, OrderLineExtended2VO[] orderLineVOs) throws AtWinXSException {
		KitMasterOrderLineVO[] kitOrderMasterVOs = OEManageOrdersHelper.getKitMasterLinesForOrder(orderKey);
		if (kitOrderMasterVOs != null)
		{
			KitComponentOrderLinePlusVO[] kitOrderComponentVOs = OEManageOrdersHelper.getKitComponentsForOrder(orderKey);
			int invalidLineNum = invalidLineVO.getLineNum();

		// find the component's master order line number
			if ((kitOrderComponentVOs != null) && (kitOrderComponentVOs.length > 0))
			{
				for (KitComponentOrderLinePlusVO compVO : kitOrderComponentVOs)
				{
					if (compVO.getOrderLineNum() == invalidLineNum)
					{
						invalidLineNum = compVO.getMasterOrderLineNum();
						invalidLineVO = findMasterForComponent(orderLineVOs, invalidLineNum);
						break;
					}
				}
			}
			finalInvalidLineNumbers.putIfAbsent(invalidLineNum, invalidLineVO);
		}
	}

	private OrderLineVO findMasterForComponent(OrderLineExtended2VO[] orderLineVOs, int invalidLineNum) {
		for (OrderLineVO masterLine : orderLineVOs)
		{
			if (invalidLineNum == masterLine.getLineNum())
			{
				return masterLine;
			}
		}
		return null;
	}

	private void identifyFinalInvalidLines(Map<Integer, OrderLineVO> finalInvalidLineNumbers,
			OrderLineVO[] invalidLines, OrderHeaderVOKey orderKey, OrderLineExtended2VO[] orderLineVOs) throws AtWinXSException {
		for (int i = 0; i < invalidLines.length; i++)
		{
			int invalidLineNum = invalidLines[i].getLineNum();
			OrderLineVO invalidLineVO = invalidLines[i];

			// if this is a kit component, find its kit master line
			String kitType = invalidLines[i].getKitLineTypeCode();
			if (kitType.equalsIgnoreCase(OrderEntryConstants.KIT_LN_TYPE_CD_COMPONENT)
				|| kitType.equalsIgnoreCase(OrderEntryConstants.KIT_LN_TYPE_CD_PACKAGE))
			{
				handleKitInvalidLines(finalInvalidLineNumbers, invalidLineVO, orderKey, orderLineVOs);
			}
			else
			{
				finalInvalidLineNumbers.putIfAbsent(invalidLineNum, invalidLineVO);
			}
		}
	}

}
