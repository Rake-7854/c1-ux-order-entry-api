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
 *  08/04/22    Satishkumar A   CAP-35247   Initial Creation, Add availability status in Price & Availability API
 *  11/03/22	Krishna Natarajan CAP-36981 making the null check on availability codes
 *  12/16/22    Sakthi M         CAP-35911  getPNA service must return Translation text values for Status availability
 */

package com.rrd.c1ux.api.util;

import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

public class PricingAndAvailabilityUtil {

	//CAP-35247 - Method is  used to get status message based on flag value or availabilityCode
	public String getItemAvailabilityStatus(String availabilityCode,AppSessionBean asb) throws AtWinXSException{
		
		String availStatus;
		
		if(OrderEntryConstants.AVAIL_CODE_AVAILABLE.equals(availabilityCode))
			availStatus=TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), TranslationTextConstants.TRANS_NM_AVAILABILITY_IN_STOCK_MSG, null);
		else if(OrderEntryConstants.AVAIL_CODE_NOT_AVAILABLE.equals(availabilityCode))
			availStatus =TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), "backOrder", null);//TranslationTextConstant not having backOrder 
		else if(OrderEntryConstants.AVAIL_CODE_JIT.equals(availabilityCode))
			availStatus =TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), TranslationTextConstants.TRANS_NM_AVAILABILITY_JIT_MSG, null);
		else
			availStatus =TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), "unableToDetermine", null); //TranslationTextConstant not having unableToDetermine
	
		return availStatus;
	}
}
