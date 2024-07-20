/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				A Salcedo				CAP-39396					Initial Version
 *  09/01/23				Krishna Natarajan		CAP-43382					added new getter and setter for showpayment for billing info
 */
package com.rrd.c1ux.api.services.checkout;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rrd.custompoint.framework.customizable.ICustomizable;
import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderCustRef;
import com.rrd.custompoint.orderentry.entity.OrderDetailsMessages;
import com.wallace.atwinxs.orderentry.admin.util.OrderReferenceFieldList;

public interface OrderDetailsHeaderInfoC1UX extends ICustomizable
{
	String getContactName();

	void setContactName(String contactName);

	String getContactPhone();

	void setContactPhone(String contactPhone);

	String getContactEmail();

	void setContactEmail(String contactEmail);

	String getOrderTitle();

	void setOrderTitle(String orderTitle);

	Collection<OrderDetailsHeaderCustRef> getOrderDetailsHeaderCustRefs();
	
	void setOrderDetailsHeaderCustRefs(Collection<OrderDetailsHeaderCustRef> orderDetailsHeaderCustRefs);
	
	OrderDetailsMessages getOrderDetailsMessages();
	
	void setOrderDetailsMessages(OrderDetailsMessages orderDetailsHeaderCustRefs);
	
	int getOrderID();

	void setOrderID(int orderID);

	String getCorporateNumber();

	void setCorporateNumber(String corporateNumber);

	String getSoldToNumber();

	void setSoldToNumber(String soldToNumber);

	String getPoNumber();

	void setPoNumber(String poNumber);

	int getScenarioNumber();

	void setScenarioNumber(int scenarioNumber);
	
	boolean isEnableOrderTitle();

	void setEnableOrderTitle(boolean enableOrderTitle);

	boolean isOrderTitleReq();

	void setOrderTitleReq(boolean orderTitleReq);

	boolean isOrderTitleEnterable();

	void setOrderTitleEnterable(boolean orderTitleEnterable);

	boolean isUseGroupOrderTitleInd();

	void setUseGroupOrderTitleInd(boolean useGroupOrderTitleInd);

	List<OrderReferenceFieldList> getOrderTitleList();

	void setOrderTitleList(List<OrderReferenceFieldList> orderTitleList);
	
	String getPoLabel();

	void setPoLabel(String poLabel);

	List<OrderReferenceFieldList> getPoNumbers();

	void setPoNumbers(List<OrderReferenceFieldList> poNumbers);

	boolean isAllowPONumberEdit();

	void setAllowPONumberEdit(boolean allowPONumberEdit);

	boolean isInApproval();

	void setInApproval(boolean isInApproval);
	
	boolean isAutoGenPO();

	void setAutoGenPO(boolean autoGenPO);
	
	boolean isUseGroupDefaultPONumber();

	void setUseGroupDefaultPONumber(boolean useGroupDefaultPONumber);
	
	public Map<String, Integer> getUserOrderReferenceFields();

	public void setUserOrderReferenceFields(Map<String, Integer> userOrderReferenceFields);
	
	//CAP-39396
	boolean isFirstVisit();

	void setFirstVisit(boolean firstVisit);
	
	boolean isBadAdminPOnumber();

	void setBadAdminPOnumber(boolean badAdminPOnumber);
	
	public Collection<String> getBadCustRefSetupDetected();

	public void setBadCustRefSetupDetected(Collection<String> badCustRefSetupDetected);
	
	public boolean isHasBadAdminCustRef();

	public void setHasBadAdminCustRef(boolean hasBadAdminCustRef);
	
	public String getOverrideSoldToNum();

	public void setOverrideSoldToNum(String overrideSoldToNum);

	public String getOverrideCorpToNum();

	public void setOverrideCorpToNum(String overrideCorpToNum);

	public Integer getpoNumMaxLength();

	public void setpoNumMaxLength(Integer poNumMaxLength);
	
	public Integer getOrderTitleMaxLength();

	public void setOrderTitleMaxLength(Integer orderTitleMaxLength);
	
	public boolean isShowpayment() ;

	public void setShowpayment(boolean showpayment);
}
