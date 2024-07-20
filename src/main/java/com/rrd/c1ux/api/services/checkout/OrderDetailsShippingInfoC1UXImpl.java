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

import java.io.Serializable;
import java.util.List;

import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.util.NameValuePair;

public class OrderDetailsShippingInfoC1UXImpl implements OrderDetailsShippingInfoC1UX, Serializable {
	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = -3757259468458205694L;

	private String carrierServiceLevel;
	private String thirdPartyAccountNumber;
	private String requiredSignatureCR;
	private String messageText;
	private String carrierUrl;
	private List<NameValuePair> carrierServiceList;
	private String showThirdPartyAccountNumber;
	private boolean isThirdPartyRequired;
	private boolean isShowDefaultSignature;
	private boolean isShowMessageOnCarrierChange;
	private boolean isShowServiceLevelField;
	private String requestedShipDate;
	private boolean showRequestedShipDate;
	
	//CAP-46294
	private boolean showOrderDueDate;
	private boolean isOrderDueDateRequired;
	private boolean showShipNowLater;
	private String orderDueDate;
	
	//CAP-49122
	private String specialShippingInstructions;
	private boolean showSpecialShippingInstructions;
	
	public String getSpecialShippingInstructions() {
		return specialShippingInstructions;
	}

	public void setSpecialShippingInstructions(String specialShippingInstructions) {
		this.specialShippingInstructions = specialShippingInstructions;
	}

	public boolean isShowSpecialShippingInstructions() {
		return showSpecialShippingInstructions;
	}

	public void setShowSpecialShippingInstructions(boolean showSpecialShippingInstructions) {
		this.showSpecialShippingInstructions = showSpecialShippingInstructions;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getCarrierUrl() {
		return carrierUrl;
	}

	public void setCarrierUrl(String carrierUrl) {
		this.carrierUrl = carrierUrl;
	}

	public String getRequiredSignatureCR() {
		return requiredSignatureCR;
	}

	public void setRequiredSignatureCR(String requiredSignatureCR) {
		this.requiredSignatureCR = requiredSignatureCR;
	}

	public String getCarrierServiceLevel() {
		return Util.nullToEmpty(carrierServiceLevel);
	}

	public void setCarrierServiceLevel(String carrierServiceLevel) {
		this.carrierServiceLevel = Util.nullToEmpty(carrierServiceLevel);
	}
	public String getThirdPartyAccountNumber() {
		return thirdPartyAccountNumber;
	}

	public void setThirdPartyAccountNumber(String thirdPartyAccountNumber) {
		this.thirdPartyAccountNumber = thirdPartyAccountNumber;
	}

	public List<NameValuePair> getCarrierServiceList() {
		return carrierServiceList;
	}

	public void setCarrierServiceList(List<NameValuePair> carrierServiceList) {
		this.carrierServiceList = carrierServiceList;
	}

	public String getShowThirdPartyAccountNumber() {
		return showThirdPartyAccountNumber;
	}

	public void setShowThirdPartyAccountNumber(String showThirdPartyAccountNumber) {
		this.showThirdPartyAccountNumber = showThirdPartyAccountNumber;
	}

	public boolean isThirdPartyRequired() {
		return isThirdPartyRequired;
	}

	public void setThirdPartyRequired(boolean isThirdPartyRequired) {
		this.isThirdPartyRequired = isThirdPartyRequired;
	}

	public boolean isShowDefaultSignature() {
		return isShowDefaultSignature;
	}

	public void setShowDefaultSignature(boolean isShowDefaultSignature) {
		this.isShowDefaultSignature = isShowDefaultSignature;
	}

	public boolean isShowMessageOnCarrierChange() {
		return isShowMessageOnCarrierChange;
	}

	public void setShowMessageOnCarrierChange(boolean isShowMessageOnCarrierChange) {
		this.isShowMessageOnCarrierChange = isShowMessageOnCarrierChange;
	}

	public boolean isShowServiceLevelField() {
		return isShowServiceLevelField;
	}

	public void setShowServiceLevelField(boolean isShowServiceLevelField) {
		this.isShowServiceLevelField = isShowServiceLevelField;
	}

	@Override
	public CustomizationToken getToken() {
		return null;
	}

	@Override
	public void setToken(CustomizationToken token) {
		// Just cause
	}

	public String getRequestedShipDate() {
		return requestedShipDate;
	}

	public void setRequestedShipDate(String requestedShipDate) {
		this.requestedShipDate = requestedShipDate;
	}

	public boolean isShowRequestedShipDate() {
		return showRequestedShipDate;
	}

	public void setShowRequestedShipDate(boolean showRequestedShipDate) {
		this.showRequestedShipDate = showRequestedShipDate;
	}
	
	public boolean isShowOrderDueDate() {
		return showOrderDueDate;
	}

	public void setShowOrderDueDate(boolean showOrderDueDate) {
		this.showOrderDueDate = showOrderDueDate;
	}

	public boolean getisOrderDueDateRequired() {
		return isOrderDueDateRequired;
	}

	public void setOrderDueDateRequired(boolean isOrderDueDateRequired) {
		this.isOrderDueDateRequired = isOrderDueDateRequired;
	}

	public boolean isShowShipNowLater() {
		return showShipNowLater;
	}

	public void setShowShipNowLater(boolean showShipNowLater) {
		this.showShipNowLater = showShipNowLater;
	}

	public String getOrderDueDate() {
		return orderDueDate;
	}

	public void setOrderDueDate(String orderDueDate) {
		this.orderDueDate = orderDueDate;
	}

}
