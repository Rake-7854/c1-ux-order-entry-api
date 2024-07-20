/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By					JIRA#			Description
 * 	--------	---------------------		-----------		------------------------------------------------
 *	12/22/23	S Ramachandran				CAP-46081		Service for USPS validation
 */

package com.rrd.c1ux.api.services.usps;

import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.usps.USPSValidationRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.admin.locators.CountryLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.DeliveryOptionsUtil;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.gwt.common.entity.Address;
import com.rrd.custompoint.gwt.common.entity.AddressSearchResult;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.USPSAddressInfoServiceParmsVO;

@Service
public class USPSValidationServiceImpl extends BaseOEService implements USPSValidationService {

	protected final SelfAdminUtil selfAdminUtil = new com.rrd.c1ux.api.util.SelfAdminUtil();
	protected final CountryLocatorService countryLocatorService;
	
	enum ZipRequiredCountries {
		USA, CAN, AUS, MEX, CHL, BRA
	}

	protected USPSValidationServiceImpl(TranslationService translationService,
			CountryLocatorService countryLocatorService) {
		super(translationService);
		this.countryLocatorService = countryLocatorService;
				
	}
	
	//CAP-46081 - USPS address validation method 
	public USPSValidationResponse validateUSPS(SessionContainer sc, USPSValidationRequest request) throws AtWinXSException {
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		
		
		OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean,
				appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
		
		USPSValidationResponse response = new USPSValidationResponse();

		if (!validateOrder(response, sc, appSessionBean)) {
			return response;
		}
		
		// Validate required request fields
		Properties translationProps = translationService.getResourceBundle(appSessionBean,
				SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);
		selfAdminUtil.validateUSPSAttributes(response, request, appSessionBean, translationMap, countryLocatorService);
		selfAdminUtil.validateUSPSAttributeZip(response, request, appSessionBean, translationService);
		
		if (response.getFieldMessages().size() > 0) {
			
			response.setSuccess(false);
		}
		else if(ZipRequiredCountries.USA.name().equalsIgnoreCase(request.getCountry().trim())) {
				Address newAddress = populateAddressSearchCriteria(request);
				response  = validateUSAddressV1(newAddress, appSessionBean, userSettings, checkoutAssembler, false);
		} else {
		
			response.setSuccess(true);
		}
		return response;
	}
	
	//CAP-46081 - populate Address Search Criteria for USPS validation 
	protected Address populateAddressSearchCriteria(USPSValidationRequest request) {
		
		Address newAddress = new AddressSearchResult();
		newAddress.setAddressLine1(Util.nullToEmpty(SelfAdminUtil.replaceAddressSpecialChar(request.getAddress1())));
		newAddress.setAddressLine2(Util.nullToEmpty(SelfAdminUtil.replaceAddressSpecialChar(request.getAddress2())));
		newAddress.setCity(Util.nullToEmpty(request.getCity()));
		newAddress.setCountry(Util.nullToEmpty(request.getCountry().toUpperCase()));
		newAddress.setPostalCode(Util.nullToEmpty(request.getZip()));
		newAddress.setStateOrProvince(Util.nullToEmpty(request.getState().toUpperCase()));
		return newAddress;
	}
	
	//CAP-46081 - Moved validateUSAddressV1 method from CODeliveryInformationServiceImpl and altered to use as a Common method
	//CAP-38135
	public USPSValidationResponse validateUSAddressV1(Address newAddress, AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, OECheckoutAssembler checkoutAssembler,
			boolean isOverrideUSPSErrors) throws AtWinXSException {
		
		USPSValidationResponse response = new USPSValidationResponse();
		
		USPSAddressInfoServiceParmsVO validation = DeliveryOptionsUtil.validateAddressHelperUSPS(newAddress, checkoutAssembler,
				appSessionBean, userSettings);
		String address1 = newAddress.getAddressLine1();
		if(!(Util.nullToEmpty(newAddress.getAddressLine2()).equalsIgnoreCase(""))) {
		
			address1 = newAddress.getAddressLine1() + " " + newAddress.getAddressLine2();
		}	 
			 
		if(Util.isBlankOrNull(validation.getErrorMessage()) || isOverrideUSPSErrors) {
			
			//CAP-45611
			if(!isOverrideUSPSErrors 
					&& !( SelfAdminUtil.replaceAddressSpecialChar(address1.trim()).equalsIgnoreCase(validation.getAddress1().trim()) 
							&& newAddress.getCity().equalsIgnoreCase(validation.getCity()) 
							&& newAddress.getStateOrProvince().equalsIgnoreCase(validation.getState()) 
							&& ( ((newAddress.getPostalCode().length() == 5) && newAddress.getPostalCode().equalsIgnoreCase(validation.getZipcode().substring(0,5))) 
									|| newAddress.getPostalCode().equalsIgnoreCase(validation.getZipcode())))) {

				//CAP-45374 -	USPS validation Suggested Address need to be split of over 30 characters 
				splitUSPSAddress(validation, response);
				response.setSuggestedCity(Util.nullToEmpty(validation.getCity()));
				response.setSuggestedState(Util.nullToEmpty(validation.getState()));
				response.setSuggestedZip(Util.nullToEmpty(validation.getZipcode()));
				response.setShowSuggestedAddress(true);
				response.setSuccess(false);
			} 
			else {
				
				response.setSuccess(true);
			}

		}
		else {
			response.setSuccess(false);
			//CAP-45375
			response.setMessage(SelfAdminUtil.getTranslationMessageForUSPSErrors(validation.getErrorMessage(),appSessionBean,translationService));
		}
		return response;
	}

	//CAP-46081 - Moved splitUSPSAddress Method from CODeliveryInformationServiceImpl
	//CAP-45374 - USPS validation Suggested Address need to be split of over 30 characters
	protected void splitUSPSAddress(USPSAddressInfoServiceParmsVO validation, USPSValidationResponse response)  {
		
		validation.setAddress1(Util.nullToEmpty(validation.getAddress1()));
		validation.setAddress2(Util.nullToEmpty(validation.getAddress2()));
		if (validation.getAddress1().length()>30 || validation.getAddress2().length()>30) {
			
			String addressToSplit = validation.getAddress1() + AtWinXSConstant.BLANK_SPACE + validation.getAddress2() ;
			//CAP-45374 - split USPS suggested address without breaking Secondary Address Unit Designator (addressLineFlag=true)
			String addressLine1 = SelfAdminUtil.splitUSPSAddressByDesignator(addressToSplit, true);
			//CAP-45374 - split USPS suggested address without breaking word alone (addressLineFlag=false)
			String addressLine2 = SelfAdminUtil.splitUSPSAddressByDesignator(addressToSplit.substring(addressLine1.length()+1), false);
			response.setSuggestedAddress1(addressLine1);
			response.setSuggestedAddress2(addressLine2);
		}
		else {
			
			response.setSuggestedAddress1(validation.getAddress1());
			response.setSuggestedAddress2(validation.getAddress2());
		}
	}
	
}
