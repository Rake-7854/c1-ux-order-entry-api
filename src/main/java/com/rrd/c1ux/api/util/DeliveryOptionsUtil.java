/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/24/23				L De Leon				CAP-39371					Initial Version
 *	05/15/23				L De Leon				CAP-40324					Copied validateAddressHelper() method from DeliveryOptionsUtil in CP with modifications
 *	11/15/23				Satishkumar A			CAP-38135					Copied validateAddressHelperUSPS() method from DeliveryOptionsUtil in CP with modifications
 */
package com.rrd.c1ux.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.entity.Address;
import com.rrd.custompoint.gwt.common.entity.AddressSearchResult;
import com.rrd.custompoint.gwt.common.entity.MessageBean;
import com.rrd.custompoint.gwt.listscommon.entity.KeyProfileSelected;
import com.rrd.custompoint.orderentry.ao.KnownAddressDetailsFormBean;
import com.rrd.custompoint.orderentry.ao.KnownAddressListFormBean;
import com.rrd.custompoint.orderentry.entity.AddressUIFields;
import com.rrd.custompoint.orderentry.entity.KeyProfileSelection;
import com.rrd.custompoint.orderentry.entity.ProfileSelection;
import com.wallace.atwinxs.admin.vo.PersonalAddressExtendedVO;
import com.wallace.atwinxs.admin.vo.PersonalAddressVO;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CorporateProfileBean;
import com.wallace.atwinxs.framework.util.PersonalProfileBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.locale.util.LocaleEditorHelper;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.BillAndShipAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.USPSAddressInfoServiceParmsVO;

public final class DeliveryOptionsUtil {

	private DeliveryOptionsUtil() {
		// not called
	}

	public static Address convertCorporateProfileBeanToAddress(CorporateProfileBean corporateProfile) {

		AddressSearchResult addressResult = new AddressSearchResult();
		addressResult.setCity(corporateProfile.getCity());
		addressResult.setAddressLine1(corporateProfile.getAddress1());
		addressResult.setAddressLine2(corporateProfile.getAddress2());
		addressResult.setCountry(corporateProfile.getCountryCode());
		addressResult.setShipToName1(corporateProfile.getName());
		addressResult.setStateOrProvince(corporateProfile.getState());
		addressResult.setPostalCode(corporateProfile.getZip());
		return addressResult;
	}

	public static Address convertPersonalProfileBeanToAddress(PersonalProfileBean personalProfile) {

		AddressSearchResult addressResult = new AddressSearchResult();
		addressResult.setCity(personalProfile.getCity());
		addressResult.setAddressLine1(personalProfile.getAddress1());
		addressResult.setAddressLine2(personalProfile.getAddress2());
		addressResult.setCountry(personalProfile.getCountryCode());
		addressResult.setStateOrProvince(personalProfile.getState());
		addressResult.setPostalCode(personalProfile.getZip());
		return addressResult;
	}

	public static Address convertOrderAddVOToAddress(OrderAddressVO orderAddresVO) {

		AddressSearchResult orderFilter = new AddressSearchResult();

		orderFilter.setCity(orderAddresVO.getCity());
		orderFilter.setAddressLine1(orderAddresVO.getAddressLine1());
		orderFilter.setAddressLine2(orderAddresVO.getAddressLine2());
		orderFilter.setAddressLine3(orderAddresVO.getAddressLine3());
		orderFilter.setCountry(orderAddresVO.getCountry());
		orderFilter.setStateOrProvince(orderAddresVO.getState());
		orderFilter.setPostalCode(orderAddresVO.getZip());
		orderFilter.setShipToName1(orderAddresVO.getCustomerName1());
		orderFilter.setShipToName2(orderAddresVO.getCustomerName2());
		orderFilter.setCorporateNumber(orderAddresVO.getKey().getCorporateNumber());
		orderFilter.setSoldToNumber(orderAddresVO.getKey().getSoldToNumber());
		orderFilter.setShipToNum(orderAddresVO.getKey().getWcssShipToNumber());

		return orderFilter;
	}
	
	public static Address convertPersonalAddVOToAddress(PersonalAddressVO personalAddVO) throws AtWinXSException {

		AddressSearchResult address = new AddressSearchResult();
		address.setShipToName1(personalAddVO.getPersonalAddressName());
		address.setShipToName2(personalAddVO.getPersonalAddressName2());
		address.setAddressLine1(personalAddVO.getAddress1());
		address.setAddressLine2(personalAddVO.getAddress2());
		address.setAddressLine3(personalAddVO.getAddress3());
		address.setCity(personalAddVO.getCity());
		address.setStateOrProvince(personalAddVO.getState());
		address.setPostalCode(personalAddVO.getZip());

		if (AtWinXSConstant.EMPTY_STRING.equals(Util.nullToEmpty(personalAddVO.getCountry()))
				&& personalAddVO instanceof PersonalAddressExtendedVO) {
			String countryName = ((PersonalAddressExtendedVO) personalAddVO).getCountryName();

			if (!AtWinXSConstant.EMPTY_STRING.equals(Util.nullToEmpty(countryName))) {
				LocaleEditorHelper helper = new LocaleEditorHelper();

				String tempCountryValue = helper.EditCountryForCode(countryName.toUpperCase(), false);
				Locale currLocale = new Locale(AtWinXSConstant.EMPTY_STRING, tempCountryValue);
				String extCountryCode = currLocale.getISO3Country();
				address.setCountry(extCountryCode);
			} else {
				address.setCountry(personalAddVO.getCountry());
			}
		} else {
			address.setCountry(personalAddVO.getCountry());
		}

		address.setShipToAttention(personalAddVO.getShipToAttn());

		return address;
	}

	/**
	 * This converts ProfileSelections value from oeSessionBean to
	 * KeyProfileSelected used in GWT
	 * 
	 * @param appSessionBean
	 * @param profSelection
	 * @return
	 * @throws AtWinXSException
	 */
	public static Collection<KeyProfileSelected> convertKeyProfileSelection(AppSessionBean appSessionBean,
			ProfileSelection profSelection) throws AtWinXSException {

		Collection<KeyProfileSelected> keyProfilesSelected = new ArrayList<>();

		// Get the ProfileSelection entity and create a new list with the filtered
		// selected key profiles
		List<KeyProfileSelection> filteredKeyProfiles = null;

		if (null != profSelection) {
			filteredKeyProfiles = profSelection.getSelectedKeyProfiles(appSessionBean);
		}

		if (filteredKeyProfiles != null) {
			// Loop through the filteredKeyProfiles
			for (KeyProfileSelection current : filteredKeyProfiles) {
				// Set the Profile Definition Name, Profile Number, and Sequence Number to the
				// KeyProfileSelected collection
				KeyProfileSelected key = new KeyProfileSelected();
				key.setProfileDefinitionName(current.getProfileDefinitionName());
				key.setAlternateProfileDefinitionName(current.getAlternateProfileDefinitionName());
				key.setProfileNumber(current.getProfileNumber());
				key.setSequenceNumber(current.getSequenceNumber());

				keyProfilesSelected.add(key);
			}
		}

		return keyProfilesSelected;
	}

	public static Address convertBillAndShipAddVOToAddress(BillAndShipAddressVO vo) {

		AddressSearchResult address = new AddressSearchResult();
		StringBuilder sb = new StringBuilder();
		sb.append(vo.getShipToAddress().getCustomerName1());
		sb.append(AtWinXSConstant.BLANK_SPACE);
		sb.append(vo.getShipToAddress().getCustomerName2());
		address.setShipToAttention(vo.getShipToAddress().getAttention());
		address.setShipToName1(sb.toString());
		address.setAddressLine1(vo.getShipToAddress().getAddressLine1());
		address.setAddressLine2(vo.getShipToAddress().getAddressLine2());
		address.setAddressLine3(vo.getShipToAddress().getAddressLine3());
		address.setCity(vo.getShipToAddress().getCity());
		address.setStateOrProvince(vo.getShipToAddress().getState());
		address.setPostalCode(vo.getShipToAddress().getZip());
		address.setCountry(vo.getShipToAddress().getCountry());
		address.setCorporateNumber(vo.getKey().getCorporateNumber().trim());
		address.setSoldToNumber(vo.getKey().getSoldToNumber().trim());
		address.setShipToNum(vo.getKey().getWcssShipToNumber().trim());
		address.setCustomerRef1(vo.getShipToAddress().getCustomerRef1());

		if (vo.getBillToAddress() != null) {
			Address billToAddr = new AddressSearchResult();
			billToAddr.setShipToAttention(vo.getBillToAddress().getAttention());
			billToAddr.setShipToName1(vo.getBillToAddress().getCustomerName1().trim());
			billToAddr.setShipToName2(vo.getBillToAddress().getCustomerName2());
			billToAddr.setAddressLine1(vo.getBillToAddress().getAddressLine1());
			billToAddr.setAddressLine2(vo.getBillToAddress().getAddressLine2());
			billToAddr.setAddressLine3(vo.getBillToAddress().getAddressLine3());
			billToAddr.setCity(vo.getBillToAddress().getCity());
			billToAddr.setStateOrProvince(vo.getBillToAddress().getState());
			billToAddr.setPostalCode(vo.getBillToAddress().getZip());
			billToAddr.setCountry(vo.getBillToAddress().getCountry());
			billToAddr.setCorporateNumber(vo.getKey().getCorporateNumber().trim());
			billToAddr.setSoldToNumber(vo.getKey().getSoldToNumber().trim());
			billToAddr.setAddressType("B");

			address.setBillToAddress(billToAddr);
		}

		return address;
	}

	public static String getPreviouslySelectedBillToCode(AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean, String defaultBillToCode) throws AtWinXSException {

		// Check for single or multiple known addresses.
		IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());
		KnownAddressListFormBean knownAddresses = ordersService.getKnownAddressListFormBean(appSessionBean, null, true);
		Collection<KnownAddressDetailsFormBean> knownAddressDetailsBean = knownAddresses.getKnownAddrDetailsFormBean();
		int knownAddressNum = (knownAddressDetailsBean != null) ? knownAddressDetailsBean.size() : -1;
		boolean hasSingleKnownAddr = (knownAddressNum == 1);

		OrderAddressVO headerAddressVO = ordersService
				.getBillingAddress(new OrderHeaderVOKey(volatileSessionBean.getOrderId()));
		String soldToNumber = (headerAddressVO != null && headerAddressVO.getKey() != null
				&& headerAddressVO.getKey().getSoldToNumber() != null ? headerAddressVO.getKey().getSoldToNumber()
						: AtWinXSConstant.EMPTY_STRING);

		int orderScenarioNum = volatileSessionBean.getOrderScenarioNumber();
		// Preselect the previously selected for other order checkout scenario
		// (Mail Merge, Campaign, Subscription
		if (!soldToNumber.isEmpty() && (orderScenarioNum == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY
				|| orderScenarioNum == OrderEntryConstants.SCENARIO_CAMPAIGN_ONLY
				|| orderScenarioNum == OrderEntryConstants.SCENARIO_SUBSCRIPTION_ONLY || hasSingleKnownAddr)) {
			defaultBillToCode = appSessionBean.getCorporateNumber() + ":" + soldToNumber;
		}

		return defaultBillToCode;
	}

	//CAP-38135 - Copied this method from CP DeliveryOptionsUtil class.
	//CP-10925 RAR - Changed return from String to MessageBean.
	/**
	 * This method will validate an address from USA
	 * 
	 */
	//CAP-40953
	public static USPSAddressInfoServiceParmsVO validateAddressHelperUSPS(Address newAddress, OECheckoutAssembler assembler, AppSessionBean appSessionBean, OEResolvedUserSettingsSessionBean userSettings) throws AtWinXSException
	{
		OENewAddressFormBean addressBean = new OENewAddressFormBean();
		
		// CAP-3207 NKM
		AddressUIFields addressUIFields = ObjectMapFactory.getEntityObjectMap().getObject(AddressUIFields.class, appSessionBean.getCustomToken());
		addressUIFields.populate(OrderEntryConstants.ADDR_SRC_NEW);

		if (newAddress != null)
		{
			addressBean.setAddressLine1(newAddress.getAddressLine1());
			addressBean.setAddressLine2(newAddress.getAddressLine2());
			addressBean.setAddToPAB(newAddress.isAddToPAB());
			addressBean.setCity(newAddress.getCity());
			addressBean.setCompanyName(newAddress.getShipToName1());

			// temporary
			// to do validation if country has states
			addressBean.setCountry("USA");

			addressBean.setMakeDefault(newAddress.isFavorite());
			addressBean.setPersonalAddressID(newAddress.getId());
			addressBean.setPhoneNumber(newAddress.getPhoneNumber());
			addressBean.setShipToAttention(newAddress.getShipToAttention());
			addressBean.setState(newAddress.getStateOrProvince());
			//CP-13286
			String zip = Util.nullToEmpty(newAddress.getPostalCode());
			addressBean.setZip(zip);
			addressBean.setCompanyName2(newAddress.getShipToName2()); // CAP-3207
			addressBean.setAddressLine3("");
			// CAP-3207 NKM
			addressBean.setShipToNameNumValid(addressUIFields.isShipToNameNumValid());
			addressBean.setShipToName2NumValid(addressUIFields.isShipToName2NumValid());
			
			if(userSettings.isNameSplit())
			{
				String name = newAddress.getFirstName() + " " + newAddress.getLastName();
				addressBean.setCompanyName(name);
			}
		}

		//if user clicks Validate Address Button
		USPSAddressInfoServiceParmsVO uspsAddressInfo = assembler.validateAddressUSPS(addressBean);
		
		// CAP-3207 NKM - Do not display success message if numbers are not allowed
		String errmsg = uspsAddressInfo.getErrorMessage();
		if(!Util.isBlankOrNull(errmsg))
		{
	        uspsAddressInfo.setErrorMessage(errmsg);
		} //CAP-42132
		else if((addressBean.getCompanyName()!=null && !addressBean.getCompanyName().isEmpty()) && 
		   (!addressBean.isShipToNameNumValid() && !addressBean.getCompanyName().matches(ModelConstants.NON_DEGIT_REGEX)))
		{
			uspsAddressInfo.setErrorMessage("Company Name must not contain numbers");
		} //CAP-42132
		else if((addressBean.getCompanyName2() != null && !addressBean.getCompanyName2().isEmpty()) &&
				(!addressBean.isShipToName2NumValid() && !addressBean.getCompanyName2().matches(ModelConstants.NON_DEGIT_REGEX)))
		{
			uspsAddressInfo.setErrorMessage("Company Name2 must not contain numbers");
		}
		
		//CP-10925 RAR - If there are no validation error message, then supply success message.
		else
		{
			String successMsg = "";
			
			if (Util.isBlankOrNull(addressBean.getAddressLine1())
	    			&& Util.isBlankOrNull(addressBean.getAddressLine2())) 
	    	{
				successMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_CITY_STATE_VALID);
	    	} 
	    	else 
	    	{
	    		successMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_ADDRESS_VALID);
	    	}
			
			uspsAddressInfo.setSuccessMessage(successMsg);
		}
		
		return uspsAddressInfo;
	}

	// CAP-40324
	public static MessageBean validateAddressHelper(Address newAddress, OECheckoutAssembler assembler,
			AppSessionBean appSessionBean, OEResolvedUserSettingsSessionBean userSettings) throws AtWinXSException {

		OENewAddressFormBean addressBean = new OENewAddressFormBean();

		AddressUIFields addressUIFields = ObjectMapFactory.getEntityObjectMap().getObject(AddressUIFields.class,
				appSessionBean.getCustomToken());
		addressUIFields.populate(OrderEntryConstants.ADDR_SRC_NEW);

		addressBean.setAddressLine1(newAddress.getAddressLine1());
		addressBean.setAddressLine2(newAddress.getAddressLine2());
		addressBean.setAddToPAB(newAddress.isAddToPAB());
		addressBean.setCity(newAddress.getCity());
		addressBean.setCompanyName(newAddress.getShipToName1());

		// temporary
		// to do validation if country has states
		addressBean.setCountry("USA");

		addressBean.setMakeDefault(newAddress.isFavorite());
		addressBean.setPersonalAddressID(newAddress.getId());
		addressBean.setPhoneNumber(newAddress.getPhoneNumber());
		addressBean.setShipToAttention(newAddress.getShipToAttention());
		addressBean.setState(newAddress.getStateOrProvince());
		String zip = Util.nullToEmpty(newAddress.getPostalCode());
		if (zip.length() > 5) {
			zip = zip.substring(0, 5);
		}
		addressBean.setZip(zip);
		addressBean.setCompanyName2(newAddress.getShipToName2());
		addressBean.setAddressLine3(AtWinXSConstant.EMPTY_STRING);
		addressBean.setShipToNameNumValid(addressUIFields.isShipToNameNumValid());
		addressBean.setShipToName2NumValid(addressUIFields.isShipToName2NumValid());

		if (userSettings.isNameSplit()) {
			String name = newAddress.getFirstName() + AtWinXSConstant.BLANK_SPACE + newAddress.getLastName();
			addressBean.setCompanyName(name);
		}

		String errmsg = assembler.validateAddress(addressBean);

		return populateAddressErrorMessage(newAddress, appSessionBean, addressBean, errmsg);
	}

	protected static MessageBean populateAddressErrorMessage(Address newAddress, AppSessionBean appSessionBean,
			OENewAddressFormBean addressBean, String errmsg) throws AtWinXSException {
		MessageBean msg = new MessageBean();
		if (!Util.isBlankOrNull(errmsg)) {
			StringBuilder errMsgs = new StringBuilder(errmsg);
			if ("USA".equals(newAddress.getCountry()) && (addressBean.getZip().trim().length() < 5
					|| (!Util.isInt(addressBean.getZip().trim().substring(0, 5))))) {
				errMsgs.append("<br>");
				errMsgs.append(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INSTRUCTION_MSG));
				errMsgs.append("<br>");
				errMsgs.append("&#187;");
				errMsgs.append(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_ZIP));
			}

			msg.setSuccess(false);
			msg.setMsg(errMsgs.toString());
		} else if ((!addressBean.getCompanyName().isEmpty() || addressBean.getCompanyName() != null)
				&& (!addressBean.isShipToNameNumValid() && !addressBean.getCompanyName().matches(ModelConstants.NUMBER_REGEX))) {
			msg.setSuccess(false);
		} else if ((!addressBean.getCompanyName2().isEmpty() || addressBean.getCompanyName2() != null)
				&& (!addressBean.isShipToName2NumValid() && !addressBean.getCompanyName2().matches(ModelConstants.NUMBER_REGEX))) {
			msg.setSuccess(false);
		} else {
			String successMsg = getAddressValidationSuccessMessage(appSessionBean, addressBean);

			msg.setSuccess(true);
			msg.setMsg(successMsg);
		}
		return msg;
	}

	protected static String getAddressValidationSuccessMessage(AppSessionBean appSessionBean,
			OENewAddressFormBean addressBean) throws AtWinXSException {
		String successMsg;

		if (Util.isBlankOrNull(addressBean.getAddressLine1())
				&& Util.isBlankOrNull(addressBean.getAddressLine2())) {
			successMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_CITY_STATE_VALID);
		} else {
			successMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_ADDRESS_VALID);
		}
		return successMsg;
	}
}