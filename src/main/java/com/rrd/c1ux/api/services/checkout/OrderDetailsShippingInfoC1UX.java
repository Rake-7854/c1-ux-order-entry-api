/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision 
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  09/12/2023  C Codina		CAP-42170	Initial version
 *  11/03/2023	N Caceres		CAP-44840	Add requested ship date to the get order header info service
 *  01/09/2024	S Ramachandran	CAP-46294	Add order due date to Checkout pages
 *  05/13/24	C Codina		CAP-49122	Added a variable for specialInstructions
 */

package com.rrd.c1ux.api.services.checkout;

import java.util.List;

import com.rrd.custompoint.framework.customizable.ICustomizable;
import com.wallace.atwinxs.orderentry.util.NameValuePair;

public interface OrderDetailsShippingInfoC1UX extends ICustomizable {

	String getMessageText();

	void setMessageText(String messageText);

	String getCarrierUrl();

	void setCarrierUrl(String carrierUrl);

	String getRequiredSignatureCR();

	void setRequiredSignatureCR(String requiredSignatureCR);

	String getCarrierServiceLevel();

	void setCarrierServiceLevel(String carrierServiceLevel);

	String getThirdPartyAccountNumber();

	public List<NameValuePair> getCarrierServiceList();

	public void setCarrierServiceList(List<NameValuePair> carrierServiceList);

	public String getShowThirdPartyAccountNumber();

	public void setShowThirdPartyAccountNumber(String showThirdPartyAccountNumber);

	public boolean isThirdPartyRequired();

	public void setThirdPartyRequired(boolean isThirdPartyRequired);

	public boolean isShowDefaultSignature();

	public void setShowDefaultSignature(boolean isShowDefaultSignature);

	public boolean isShowMessageOnCarrierChange();

	public void setShowMessageOnCarrierChange(boolean isShowMessageOnCarrierChange);

	public void setThirdPartyAccountNumber(String thirdPartyAccountNumber);

	public boolean isShowServiceLevelField();

	public void setShowServiceLevelField(boolean isShowServiceLevelField);
	
	public String getRequestedShipDate();
	
	public void setRequestedShipDate(String requestedShipDate);
	
	public boolean isShowRequestedShipDate();
	
	public void setShowRequestedShipDate(boolean showRequestedShipDate);

	public boolean isShowOrderDueDate();

	public void setShowOrderDueDate(boolean showOrderDueDate);

	public boolean getisOrderDueDateRequired();

	public void setOrderDueDateRequired(boolean isOrderDueDateRequired);

	public boolean isShowShipNowLater();

	public void setShowShipNowLater(boolean showShipNowLater);

	public String getOrderDueDate();

	public void setOrderDueDate(String orderDueDate);
	
	public String getSpecialShippingInstructions();

	public void setSpecialShippingInstructions(String specialShippingInstructions);

	public boolean isShowSpecialShippingInstructions();

	public void setShowSpecialShippingInstructions(boolean showSpecialShippingInstructions);
	
	
}
