/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/03/23				L De Leon				CAP-38053					Initial Version
 *  03/13/23                C Porter                CAP-39179                   Refactor setSuccess for BaseResponse
 *	03/24/23				L De Leon				CAP-39371					Modified deliveryOptions and added companyMasterListAddresses
 *	04/27/23				A Salcedo				CAP-40193					Added translation.
 *	07/07/23				A Boomker				CAP-41972					Adding current and default bill to addresses
 *	09/11/23				Krishna Natarajan		CAP-43698					Adding showPayment boolean variable
 *  04/12/24				Krishna Natarajan		CAP-48606					Added a new checkDistListSharedOrPrivate field doNotShareListsInd to update BU Manage List admin settings
 *  06/26/24				Krishna Natarajan		CAP-49902					Added a new variable to tell if order is efdOnly
 */
package com.rrd.c1ux.api.models.checkout;

import java.util.List;
import java.util.Map;

import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.custompoint.gwt.common.entity.Address;
import com.rrd.custompoint.gwt.common.util.CountryBean;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.gwt.deliveryoptions.widget.BillToAddress;
import com.rrd.custompoint.gwt.listscommon.entity.AddressCheckOutSettings;
import com.wallace.atwinxs.orderentry.util.DeliveryOptionsFormBean;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="CODeliveryInformationResponse", description = "Response Class for Delivery Options, including Address Source options and details needed for each option", type = "object")
public class CODeliveryInformationResponse extends BaseResponse {
	@Schema(name ="deliveryOptions", description = "Class that contains all information necessary for the static display and dynamic display of the delivery section", type = "object")
	private DeliveryOptionsFormBean deliveryOptions;

	@Schema(name ="checkoutSettings", description = "Class that contains information to determine address sources and other settings", type = "object")
	private AddressCheckOutSettings checkoutSettings;

	@Schema(name ="billToAddresses", description = "List of bill to addresses", type = "array")
	private List<BillToAddress> billToAddresses;

	@Schema(name ="defaultBillToAddressForNewShipTo", description = "Default bill to address to use for new ship to address sources", type = "object")
	private OrderAddressVO defaultBillToAddressForNewShipTo;

	@Schema(name ="currentBillToAddress", description = "Current bill to address for currently selected ship to address", type = "object")
	private OrderAddressVO currentBillToAddress;

	@Schema(name ="countryOptions", description = "List of countries and their corresponding country codes", example = "[{\"name\":\"Canada\", \"value\":\"CA\"}, {...}]", type = "array")
	private List<NameValuePair<String>> countryOptions;

	@Schema(name ="countriesAndStates", description = "List of country codes and their corresponding country information, including the list of states or provinces", type = "array")
	private List<NameValuePair<CountryBean>> countriesAndStates;

	@Schema(name ="preferredShippingAddress", description = "List of default shipping addresses", type = "array")
	private List<Address> preferredShippingAddress;

	@Schema(name ="companyMasterListAddresses", description = "List of addresses from the company master list", type = "array")
	private List<Address> companyMasterListAddresses;

	//CAP-40193
	@Schema(name ="translationDeliveryInfo", description = "Messages from \"deliveryInfo\" translation file will load here.", type = "string",  example="\"translation\": { \"nameLabel\": \"Your Name\"}")
	private Map<String, String> translationDeliveryInfo;
	
	//CAP-43698
	@Schema(name ="showpayment", description = "Payment information to say true or false based on card required flag and optional flag", type = "boolean",  example="true")
	private boolean showpayment=false;
	//CAP-48606
	@Schema(name="doNotShareListsInd", description="Determination of whether show Dist List Shared settings at the BU level", type="boolean", example="false")
	private boolean doNotShareListsInd =false;
	
	//CAP-49902
	@Schema(name="efdOnly", description="Determination of whether order is efdOnly", type="boolean", example="false")
	private boolean efdOnly=false;
}
