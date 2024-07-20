/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/29/23				A Salcedo				CAP-39396					Initial Version
 *  09/01/23				Krishna Natarajan		CAP-43382					added new field showpayment for billing info
 */
package com.rrd.c1ux.api.services.checkout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.rrd.custompoint.orderentry.entity.OrderDetailsHeaderCustRef;
import com.rrd.custompoint.orderentry.entity.OrderDetailsMessages;
import com.rrd.custompoint.orderentry.entity.OrderDetailsTeamSharing;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.orderentry.admin.util.OrderReferenceFieldList;

public class OrderDetailsHeaderInfoC1UXImpl  implements OrderDetailsHeaderInfoC1UX, Serializable
{

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = 1827523079490986515L;
	
	private String orderTitle;
	private boolean enableOrderTitle;
	private boolean orderTitleReq;
	private boolean orderTitleEnterable;
	private boolean useGroupOrderTitleInd;
	private List<OrderReferenceFieldList> orderTitleList;
	
	private List<OrderReferenceFieldList> poNumbers;
	private boolean allowPONumberEdit;
	private boolean isInApproval;
	private boolean autoGenPO;
	private boolean useGroupDefaultPONumber;
	
	boolean firstVisit;
	boolean badAdminPOnumber;

	private Collection<OrderDetailsHeaderCustRef> orderDetailsHeaderCustRefs = new ArrayList<>();
	private OrderDetailsMessages orderDetailsMessages;
	
	private OrderDetailsTeamSharing orderDetailsTeamSharing;
	
	private Map<String, Integer> userOrderReferenceFields;
	
	private Collection<String> badCustRefSetupDetected;
	private boolean hasBadAdminCustRef;
	
	private String overrideSoldToNum;
	private String overrideCorpToNum;
	private Integer poNumMaxLength;
	private Integer orderTitleMaxLength;
	
	//CP Order Object fields
	private String contactName;
	private String contactPhone;
	private String contactEmail;
	private int orderID;
	private String corporateNumber;
	private String soldToNumber;
	private String poNumber;
	private String poLabel; 
	private int scenarioNumber;
	private boolean showpayment=false;
	
	public String getOrderTitle()
	{
		return orderTitle;
	}


	public void setOrderTitle(String orderTitle)
	{
		this.orderTitle = orderTitle;
	}

	public Collection<OrderDetailsHeaderCustRef> getOrderDetailsHeaderCustRefs()
	{
		return orderDetailsHeaderCustRefs;
	}


	public void setOrderDetailsHeaderCustRefs(Collection<OrderDetailsHeaderCustRef> orderDetailsHeaderCustRefs)
	{
		this.orderDetailsHeaderCustRefs = orderDetailsHeaderCustRefs;
	}


	public OrderDetailsMessages getOrderDetailsMessages()
	{
		return orderDetailsMessages;
	}


	public void setOrderDetailsMessages(OrderDetailsMessages orderDetailsMessages)
	{
		this.orderDetailsMessages = orderDetailsMessages;
	}

	public boolean isEnableOrderTitle()
	{
		return enableOrderTitle;
	}

	public void setEnableOrderTitle(boolean enableOrderTitle)
	{
		this.enableOrderTitle = enableOrderTitle;
	}

	public boolean isOrderTitleReq()
	{
		return orderTitleReq;
	}

	public void setOrderTitleReq(boolean orderTitleReq)
	{
		this.orderTitleReq = orderTitleReq;
	}

	public boolean isOrderTitleEnterable()
	{
		return orderTitleEnterable;
	}

	public void setOrderTitleEnterable(boolean orderTitleEnterable)
	{
		this.orderTitleEnterable = orderTitleEnterable;
	}

	public boolean isUseGroupOrderTitleInd()
	{
		return useGroupOrderTitleInd;
	}

	public void setUseGroupOrderTitleInd(boolean useGroupOrderTitleInd)
	{
		this.useGroupOrderTitleInd = useGroupOrderTitleInd;
	}

	public List<OrderReferenceFieldList> getOrderTitleList()
	{
		return orderTitleList;
	}

	public void setOrderTitleList(List<OrderReferenceFieldList> orderTitleList)
	{
		this.orderTitleList = orderTitleList;
	}
	
	public OrderDetailsTeamSharing getOrderDetailsTeamSharing()
	{
		return orderDetailsTeamSharing;
	}

	public void setOrderDetailsTeamSharing(OrderDetailsTeamSharing orderDetailsTeamSharing)
	{
		this.orderDetailsTeamSharing = orderDetailsTeamSharing;
	}

	public List<OrderReferenceFieldList> getPoNumbers()
	{
		return poNumbers;
	}

	public void setPoNumbers(List<OrderReferenceFieldList> poNumbers)
	{
		this.poNumbers = poNumbers;
	}

	public boolean isAllowPONumberEdit()
	{
		return allowPONumberEdit;
	}

	public void setAllowPONumberEdit(boolean allowPONumberEdit)
	{
		this.allowPONumberEdit = allowPONumberEdit;
	}

	public boolean isInApproval()
	{
		return isInApproval;
	}

	public void setInApproval(boolean isInApproval)
	{
		this.isInApproval = isInApproval;
	}

	public boolean isAutoGenPO()
	{
		return autoGenPO;
	}

	public void setAutoGenPO(boolean autoGenPO)
	{
		this.autoGenPO = autoGenPO;
	}

	public boolean isUseGroupDefaultPONumber()
	{
		return useGroupDefaultPONumber;
	}

	public void setUseGroupDefaultPONumber(boolean useGroupDefaultPONumber)
	{
		this.useGroupDefaultPONumber = useGroupDefaultPONumber;
	}

	public Map<String, Integer> getUserOrderReferenceFields()
	{
		return userOrderReferenceFields;
	}

	public void setUserOrderReferenceFields(Map<String, Integer> userOrderReferenceFields)
	{
		this.userOrderReferenceFields = userOrderReferenceFields;
	}
	
	//CAP-39396
	public boolean isFirstVisit()
	{
		return firstVisit;
	}

	public void setFirstVisit(boolean firstVisit)
	{
		this.firstVisit = firstVisit;
	}
	
	public boolean isBadAdminPOnumber()
	{
		return badAdminPOnumber;
	}

	public void setBadAdminPOnumber(boolean badAdminPOnumber)
	{
		this.badAdminPOnumber = badAdminPOnumber;
	}
	
	public Collection<String> getBadCustRefSetupDetected()
	{
		return badCustRefSetupDetected;
	}

	public void setBadCustRefSetupDetected(Collection<String> badCustRefSetupDetected)
	{
		this.badCustRefSetupDetected = badCustRefSetupDetected;
	}

	public boolean isHasBadAdminCustRef()
	{
		return hasBadAdminCustRef;
	}

	public void setHasBadAdminCustRef(boolean hasBadAdminCustRef)
	{
		this.hasBadAdminCustRef = hasBadAdminCustRef;
	}

	public String getOverrideSoldToNum()
	{
		return overrideSoldToNum;
	}

	public void setOverrideSoldToNum(String overrideSoldToNum)
	{
		this.overrideSoldToNum = overrideSoldToNum;
	}

	public String getOverrideCorpToNum()
	{
		return overrideCorpToNum;
	}

	public void setOverrideCorpToNum(String overrideCorpToNum)
	{
		this.overrideCorpToNum = overrideCorpToNum;
	}
	
	public Integer getpoNumMaxLength()
	{
		return poNumMaxLength;
	}

	public void setpoNumMaxLength(Integer poNumMaxLength)
	{
		this.poNumMaxLength = poNumMaxLength;
	}
	
	public Integer getOrderTitleMaxLength()
	{
		return orderTitleMaxLength;
	}

	public void setOrderTitleMaxLength(Integer orderTitleMaxLength)
	{
		this.orderTitleMaxLength = orderTitleMaxLength;
	}

	public String getContactName() 
	{
		return contactName;
	}

	public void setContactName(String contactName) 
	{
		this.contactName = contactName;
	}

	public String getContactPhone() 
	{
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) 
	{
		this.contactPhone =contactPhone;
	}

	public String getContactEmail() 
	{
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) 
	{
		this.contactEmail = contactEmail;
	}

	public int getOrderID() 
	{
		return orderID;
	}

	public void setOrderID(int orderID) 
	{
		this.orderID = orderID;
	}

	public String getCorporateNumber() 
	{
		return corporateNumber;
	}

	public void setCorporateNumber(String corporateNumber) 
	{
		this.corporateNumber = corporateNumber;
	}

	public String getSoldToNumber() 
	{
		return soldToNumber;
	}

	public void setSoldToNumber(String soldToNumber) 
	{
		this.soldToNumber = soldToNumber;
	}

	public String getPoNumber() 
	{
		return poNumber;
	}

	public void setPoNumber(String poNumber) 
	{
		this.poNumber = poNumber;
	}

	public int getScenarioNumber() 
	{
		return scenarioNumber;
	}

	public void setScenarioNumber(int scenarioNumber) 
	{
		this.scenarioNumber = scenarioNumber;
	}

	public String getPoLabel() 
	{
		return poLabel;
	}

	public void setPoLabel(String poLabel) 
	{
		this.poLabel = poLabel;
	}

	@Override
	public CustomizationToken getToken() 
	{
		return null;
	}

	@Override
	public void setToken(CustomizationToken arg0) 
	{
		// Just cause
	}


	@Override
	public boolean isShowpayment() {
		return showpayment;
	}


	@Override
	public void setShowpayment(boolean showpayment) {
		this.showpayment=showpayment;
	}
}
