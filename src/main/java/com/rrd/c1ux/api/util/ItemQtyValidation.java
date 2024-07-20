/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 * 14/09/2022	Aarthi r		CAP-35669					Fixing validation error messages - Remove Item Number
 * 04/25/23     Sakthi M		CAP-39335   				Validation labels in Item Quantity Validation API to make/use translation text values		
 */
package com.rrd.c1ux.api.util;

import java.util.HashMap;
import java.util.Map;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

public class ItemQtyValidation {
	/** 
	 * Checks basic min, max and mults based on values passed in.
	 * 
	 * @param int orderQty 
	 * @param int uomFactor 
	 * @param String uomCOde
	 * @param String stockUOMCode, // added for DTS #9800
	 * @param double minQty 
	 * @param double maxQty 
	 * @param double multQty 
	 * @return Message
	 */
	public String validateItemQuantity(AppSessionBean appSessionBean,
		int orderQty,
		int uomFactor,
		ItemRptVO itemRptVO,
		String uomDescription) throws AtWinXSException
	{
		double minQty=itemRptVO.getItemMininumOrderQty();
		double maxQty=itemRptVO.getMaxinumOrderQty();
		double multQty=itemRptVO.getItemMultipleOrderQty();
		//String errMsg; //CP-2911
		
		//CP-2911 start: use new implementation of message handling - use ErrorCode instead of using the actual message string
		//DTS 8565 cast orderQty and uomFactor to doubles so the product will be a double, not int.
		double actualQty = (double)orderQty * (double)uomFactor;
		if (actualQty <= 0.0)
		{
				return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.INVALID_QTY);
		}
		
		/* DTS 7668 - If max order qty is zero, set it to the max. */
		if (maxQty == 0)
		{
			maxQty = OrderEntryConstants.DEFAULT_MAX_ORDER_QTY;
		}

		if((double)uomFactor > maxQty)
		{
				return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.NOT_ORDERABLE);
		}

		// check the min limit
		if (actualQty < minQty)
		{
			//DTS 8156 Find the minimum number of the selected UOM that needs to be ordered to reach the minimum order qty.
			int minInUom = (int)minQty / uomFactor;
			if((minInUom * uomFactor) < minQty)
			{
				minInUom++;
			}
			return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VALIDATION_MIN_MSG)+" "+minInUom +" "+uomDescription ;
		}

		// check the max limit
		/* DTS 7341 - Ignore qty limit when max qty is zero. */
		if (actualQty > maxQty 
			&& maxQty > 0)
		{
			//DTS 8156 Find the maximum number of the selected UOM that can to be ordered to stay within the maximum order qty.
			int maxInUom = (int)maxQty / uomFactor;
			return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VALIDATION_MAX_MSG)+" "+maxInUom +" "+uomDescription;
		}

		// check the multiple
		double remainder = Math.IEEEremainder(actualQty, multQty);
		
		Map<String, Object> replaceMap = new HashMap<String, Object>();
		replaceMap.put("{multiple}", (int) multQty);
		if (remainder != 0.0)
		{
			return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),SFTranslationTextConstants.MULTIPLE_QTY,replaceMap);
		}
		//CP-2911 end
		return null;
	}
	
}
