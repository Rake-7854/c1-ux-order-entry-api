/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/18/23				N Caceres				CAP-42342					Initial version
 *	09/07/23				S Ramachandran			CAP-43630					Added method to validate PAB request
 *  09/20/23                M Sakthi                CAP-44003					Null pointer issue fix
 *  09/28/23				N Caceres				CAP-42806					Fix inconsistent translation in update extended Personal Profile
 *  11/07/23				S Ramachandran			CAP-44961 					USPS validation in save PAB and to show suggested address
 *  11/29/23				Satishkumar A			CAP-45375					C1UX BE - Modify the errors returned from the USPS validation to be translated
 *  11/30/23				S Ramachandran			CAP-45631 					PAB Addresses save zip validation and error corrected as like in CP
 *  12/07/23    			S Ramachandran  		CAP-45485   				Fix code to only search/use originator profile when doing self administration
 *  12/22/23				S Ramachandran			CAP-46081					Added methods for USPS validation request attributes 
 *  01/01/24				M Sakthi				CAP-45913					C1UX BE - Modify and test new API to pre-populate fields for Corporate Profile
 */
package com.rrd.c1ux.api.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.models.admin.UpdateCompanyProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateCompanyProfileResponse;
import com.rrd.c1ux.api.models.usps.USPSValidationRequest;
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.admin.locators.CountryLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.interfaces.ICountryComponent;
import com.wallace.atwinxs.locale.ao.CountryBean;
import com.wallace.atwinxs.locale.ao.StateBean;
import com.wallace.atwinxs.locale.vo.CountryVOKey;
import com.wallace.atwinxs.locale.vo.CountryWithNameVO;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

public class SelfAdminUtil {

	private static final int SELF_ADMIN_CHAR_SIZE_5 = 5;
	private static final int SELF_ADMIN_MAX_SIZE_3 = 3;
	private static final int SELF_ADMIN_MAX_SIZE_4 = 4;
	private static final int SELF_ADMIN_MAX_SIZE_12 = 12;
	private static final int SELF_ADMIN_MAX_SIZE_24 = 24;
	private static final int SELF_ADMIN_MAX_SIZE_30 = 30;
	private static final int SELF_ADMIN_MAX_SIZE_35 = 35;
	private static final int SELF_ADMIN_MAX_SIZE_50 = 50;
	private static final int SELF_ADMIN_MAX_SIZE_255 = 255;
	public static final String SELF_ADMIN_NAME_1 = "name1";
	public static final String SELF_ADMIN_NAME_2 = "name2";
	public static final String SELF_ADMIN_TITLE = "title";
	public static final String SELF_ADMIN_FAX_NUMBER = "faxNumber";
	public static final String SELF_ADMIN_PHONE_NUMBER = "phoneNumber";
	public static final String SELF_ADMIN_WEB_URL = "webUrl";
	public static final String SELF_ADMIN_IMAGE_URL = "imageUrl";
	public static final String SELF_ADMIN_DEPARTMENT = "department";
	public static final String SELF_ADMIN_DIVISION = "division";
	public static final String SELF_ADMIN_COUNTRY_CODE = "countryCd";
	public static final String SELF_ADMIN_ADDRESS_LINE_1 = "addressLine1";
	public static final String SELF_ADMIN_ADDRESS_LINE_2 = "addressLine2";
	public static final String SELF_ADMIN_ADDRESS_LINE_3 = "addressLine3";
	public static final String SELF_ADMIN_CITY = "city";
	public static final String SELF_ADMIN_STATE_CODE = "stateCd";
	public static final String SELF_ADMIN_ZIP_CODE = "zipCd";
	public static final String COMPANY_AS_PREFFERED_ADDR_CODE = "C";
	public static final String SELF_ADMIN_EXT_COUNTRY_CODE = "countryCode";
	public static final String SELF_ADMIN_EXT_STATE_CODE = "stateCd";
	public static final String SELF_ADMIN_NO = "No";
	public static final String SELF_ADMIN_NA = "NA";
	public static final String SELF_ADMIN_YES = "Yes";
	public static final String SELF_ADMIN_COUNTRY_EXISTS = "countryExist";
	public static final String SELF_ADMIN_COUNTRY_NAME_LBL = "countryName";
	public static final String SELF_ADMIN_STATE_EXISTS = "stateExist";
	public static final String SELF_ADMIN_STATE_NAME_LBL = "stateName";
	public static final String SELF_ADMIN_NO_PROFILE_SELECTED_ERROR = " is attempting to change their profile but does not have one selected.";
	public static final String SELF_ADMIN_NO_PERMISSION_ERROR = " is attempting to change their profile but does not have those permissions.";
	public static final String SELF_ADMIN_RETRIEVE_PROFILE_ERROR = " could not load profile definition.";
	
	//CAP-43630 - PAB search key
	public static final String PAB_SN1 = "shiptoname1";
	public static final String PAB_STOATTN = "shiptoattention";
	public static final String PAB_CTY = "city";
	public static final String PAB_CT = "country";
	public static final String PAB_ST = "state";
	public static final String PAB_ZIP = "zip";

	//CAP-43598
	private static final String PAB_NAME = "name";
	private static final String PAB_ADDRESS_1 = "address1";
	private static final String PAB_ADDRESS_2 = "address2";
	private static final String PAB_ADDRESS_3 = "address3";
	public static final String PAB_SHIP_TO_ATTN="shipToAttn";
	public static final String PAB_COUNTRY_USA="USA";
	
	// CAP-42806
	public static final String SELF_ADMIN_MIDDLE_INITIAL_LBL = "middle";
	public static final String SELF_ADMIN_MIDDLE_INITIAL_FLD_NM = "middleInitial";
	public static final String SELF_ADMIN_SUFFIX_FLD_NM = "suffix";
	public static final String SELF_ADMIN_FAX_LBL = "fax";
	public static final String SELF_ADMIN_WEB_URL_LBL = "webURL";
	public static final String SELF_ADMIN_PHOTO_URL_LBL = "photoURL";
	public static final String SELF_ADMIN_PAGER_NUMBER_FLD_NM = "pagerNumber";
	public static final String SELF_ADMIN_TOLL_FREE_NUMBER_FLD_NM = "tollFreeNumber";
	
	// CAP-42342 - Validate country code and get country name
	public Map<String, String> validateAndGetCountryStateName(String countryCode, String stateCode) {
		Map<String, String> validCountryAndState = new HashMap<>();
		Iterator<String> countries = UtilCountryInfo.getCountriesByName();
		String countryExist = SELF_ADMIN_NO;
		String countryName = AtWinXSConstant.EMPTY_STRING;
		CountryBean countryBean = null;
		while (countries.hasNext()) {
			countryBean = UtilCountryInfo.getCountryByName(countries.next());
			if (countryBean.getCountryCode().equals(countryCode)) {
				countryExist = SELF_ADMIN_YES;
				countryName = countryBean.getCountryName();
				break;
			}
		}
		validCountryAndState.put(SELF_ADMIN_COUNTRY_EXISTS, countryExist);
		validCountryAndState.put(SELF_ADMIN_COUNTRY_NAME_LBL, countryName);
		validCountryAndState = validateAndGetStateName(validCountryAndState, countryBean, stateCode);
		return validCountryAndState;
	}

	// CAP-42342 - Validate if country has valid state code get state name
	public Map<String, String> validateAndGetStateName(Map<String, String> validCountryAndState,
			CountryBean countryBean, String stateCode) {
		String stateExist = SELF_ADMIN_NA;
		String stateName = AtWinXSConstant.EMPTY_STRING;
		if (countryBean.getCountryHasStates()) {
			stateExist = SELF_ADMIN_NO;
			Iterator<String> states = countryBean.getStatesInCountryByName();
			while (states.hasNext()) {
				StateBean stateBean = countryBean.getStateInCountryByName(states.next());
				if (stateBean.getStateCode().equals(stateCode)) {
					stateExist = SELF_ADMIN_YES;
					stateName = stateBean.getStateCodeText();
					break;
				}
			}
		}
		validCountryAndState.put(SELF_ADMIN_STATE_EXISTS, stateExist);
		validCountryAndState.put(SELF_ADMIN_STATE_NAME_LBL, stateName);
		return validCountryAndState;
	}

	public void validateCompanyProfileFields(UpdateCompanyProfileResponse response, UpdateCompanyProfileRequest request,
			AppSessionBean asb, ProfileDefinition def, String countryExist, String stateExist,
			Map<String, String> translationMap) throws AtWinXSException {
		validateField(response, asb, def.isCompanyNameRequired(), translationMap, request.getName1(), SELF_ADMIN_NAME_1, SELF_ADMIN_MAX_SIZE_35);
		validateField(response, asb, request.getName2(), SELF_ADMIN_NAME_2, SELF_ADMIN_MAX_SIZE_35);
		validateField(response, asb, def.isCompanyTitleRequired(), translationMap, request.getTitle(), SELF_ADMIN_TITLE, SELF_ADMIN_MAX_SIZE_50);
		validateField(response, asb, def.isCompanyAddressRequired(), translationMap, request.getAddressLine1(), SELF_ADMIN_ADDRESS_LINE_1, SELF_ADMIN_MAX_SIZE_35);
		validateField(response, asb, request.getAddressLine2(), SELF_ADMIN_ADDRESS_LINE_2, SELF_ADMIN_MAX_SIZE_35);
		validateField(response, asb, request.getAddressLine3(), SELF_ADMIN_ADDRESS_LINE_3, SELF_ADMIN_MAX_SIZE_35);
		validateField(response, asb, def.isCompanyFaxRequired(), translationMap, request.getFaxNumber(), SELF_ADMIN_FAX_NUMBER, SELF_ADMIN_MAX_SIZE_24);
		validateField(response, asb, def.isCompanyPhoneNumberRequired(), translationMap, request.getPhoneNumber(), SELF_ADMIN_PHONE_NUMBER, SELF_ADMIN_MAX_SIZE_24);
		validateField(response, asb, def.isCompanyURLRequired(), translationMap, request.getWebUrl(), SELF_ADMIN_WEB_URL, SELF_ADMIN_MAX_SIZE_255);
		validateField(response, asb, def.isCompanyLogoRequired(), translationMap, request.getImageUrl(), SELF_ADMIN_IMAGE_URL, SELF_ADMIN_MAX_SIZE_255);
		validateField(response, asb, def.isCompanyAddressRequired(), translationMap, request.getCity(), SELF_ADMIN_CITY, SELF_ADMIN_MAX_SIZE_30);
		validateField(response, asb, def.isCompanyAddressRequired(), translationMap, request.getZipCd(), SELF_ADMIN_ZIP_CODE, SELF_ADMIN_MAX_SIZE_12);
		validateField(response, asb, def.isDepartmentRequired(), translationMap, request.getDepartment(), SELF_ADMIN_DEPARTMENT, SELF_ADMIN_MAX_SIZE_30);
		validateField(response, asb, def.isDivisionRequired(), translationMap, request.getDepartment(), SELF_ADMIN_DIVISION, SELF_ADMIN_MAX_SIZE_30);
		validateCountry(response, asb, def.isCompanyAddressRequired(), translationMap, countryExist, request.getCountryCd());
		validateState(response, asb, def.isCompanyAddressRequired(), translationMap, countryExist, stateExist, request.getStateCd());
	}
	
	protected void validateField(UpdateCompanyProfileResponse response, AppSessionBean asb, boolean isRequired,
			Map<String, String> translationMap, String value, String fieldName, int maxSize) throws AtWinXSException {
		if (isRequired && (Util.isBlank(value))) {
			response.getFieldMessages().put(fieldName,
					Util.nullToEmpty(translationMap.get(fieldName)) + AtWinXSConstant.BLANK_SPACE
							+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else {
			validateField(response, asb, value, fieldName, maxSize);
		}
	}
	
	protected void validateField(UpdateCompanyProfileResponse response, AppSessionBean asb, String value,
			String fieldName, int maxSize) throws AtWinXSException {
		if (value.length() > maxSize) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					maxSize + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}
	
	protected void validateCountry(UpdateCompanyProfileResponse response, AppSessionBean asb, boolean isRequired,
			Map<String, String> translationMap, String countryExist, String value)
			throws AtWinXSException {
		String fieldName = SELF_ADMIN_COUNTRY_CODE;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (isRequired && (Util.isBlank(value))) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_3) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_3 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} else if ((value.length() > 0 && value.length() <= SELF_ADMIN_MAX_SIZE_3) && countryExist.equalsIgnoreCase(SELF_ADMIN_NO)) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
	}

	protected void validateState(UpdateCompanyProfileResponse response, AppSessionBean asb, boolean isRequired,
			Map<String, String> translationMap, String countryExist, String stateExist, String value) throws AtWinXSException {
		String fieldName = SELF_ADMIN_STATE_CODE;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (isRequired && countryExist.equalsIgnoreCase(SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SELF_ADMIN_NA)
				&& (value == null || Util.isBlank(value.trim()))) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (countryExist.equalsIgnoreCase(SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SELF_ADMIN_NA)
				&& value.length() > SELF_ADMIN_MAX_SIZE_4) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_4 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} else if ((value.length() > 0 && value.length() <= SELF_ADMIN_MAX_SIZE_4)
				&& ((countryExist.equalsIgnoreCase(SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SELF_ADMIN_NA))
						|| (countryExist.equalsIgnoreCase(SELF_ADMIN_YES) && stateExist.equalsIgnoreCase(SELF_ADMIN_NO)))) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
	}

	
	//CAP-43630	- Validate PAB search request
	public boolean validatePABSearchRequest(PABSearchResponse response,  String searchKey, String searchValue, AppSessionBean asb) 
			throws AtWinXSException {

		if (!AtWinXSConstant.EMPTY_STRING.equals(searchKey)
				&& !searchKey.matches("shiptoname1|shiptoattention|city|country|state|zip")) {
			
			response.setSuccess(false);
			response
					.setMessage(TranslationTextTag.processMessage(asb.getDefaultLocale(),
							asb.getCustomToken(), SFTranslationTextConstants.VALID_SEARCH_CRITERIA_ERR));
			return false;
		}
		else if(searchKey.equals(PAB_SN1)) 
			validatePABField(response, asb, searchValue, PAB_SN1, SELF_ADMIN_MAX_SIZE_35);
		else if(searchKey.equals(PAB_STOATTN)) 
			validatePABField(response, asb, searchValue, PAB_STOATTN, SELF_ADMIN_MAX_SIZE_35);
		else if(searchKey.equals(PAB_CTY))
			validatePABField(response, asb, searchValue, PAB_CTY, SELF_ADMIN_MAX_SIZE_35);
		else if(searchKey.equals(PAB_CT)) 
			validatePABField(response, asb, searchValue, PAB_CT, SELF_ADMIN_MAX_SIZE_3);
		else if(searchKey.equals(PAB_ST)) 
			validatePABField(response, asb, searchValue, PAB_ST, SELF_ADMIN_MAX_SIZE_4);
		else if(searchKey.equals(PAB_ZIP)) 
			validatePABField(response, asb, searchValue, PAB_ZIP, SELF_ADMIN_MAX_SIZE_12);
	
		return response.getFieldMessages().size() <= 0;
	}

	//CAP-43630 - Validate PAB search criteria value
	protected void validatePABField(PABSearchResponse response, AppSessionBean asb, String value, String fieldName,
			int maxSize) throws AtWinXSException {
		if (value.length() > maxSize) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					maxSize + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}
	

	private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {
		return Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), errorKey, replaceMap));
	}
	
	
   //CAP-41593	Start here
	public boolean validateAddress(SessionContainer sc, PABSaveRequest address,PABSaveResponse response) throws AtWinXSException // CAP-3449
	{
		String errorMessage = "";
		boolean res=false;
		
		try
		{
			ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
			VolatileSessionBean vsb = applicationVolatileSession.getVolatileSessionBean();
			ApplicationSession appSession = sc.getApplicationSession();
			AppSessionBean appSessionBean = appSession.getAppSessionBean();

			OECheckoutAssembler assembler = OEAssemblerFactory.getCheckoutAssembler(vsb, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
					appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
			
			OENewAddressFormBean addressBean = new OENewAddressFormBean();

			if (address != null)
			{
				//CP-12082 RAR - If null, then set empty "".
				addressBean.setAddressLine1(Util.nullToEmpty(address.getAddress1()));
				addressBean.setAddressLine2(Util.nullToEmpty(address.getAddress2()));
				addressBean.setAddressLine3(Util.nullToEmpty(address.getAddress3()));
				addressBean.setAddToPAB(true);
				addressBean.setCity(Util.nullToEmpty(address.getCity()));
				addressBean.setCompanyName(Util.nullToEmpty(address.getName()));
				addressBean.setCompanyName2(Util.nullToEmpty(address.getName2()));
				addressBean.setCountry(Util.nullToEmpty(address.getCountry()));
				addressBean.setMakeDefault(address.isDefaultAddress());
				addressBean.setPersonalAddressID(String.valueOf(address.getAddressID()));
				addressBean.setPhoneNumber(address.getPhoneNumber());
				addressBean.setShipToAttention(Util.nullToEmpty(address.getShipToAttn()));
				addressBean.setState(Util.nullToEmpty(address.getState()));
				String zip = Util.nullToEmpty(address.getZip());
				//CP-13286
				if(zip.length() > 5)
				{
					zip = zip.substring(0, 5);
				}
				addressBean.setZip(zip);
			}
			
			errorMessage = assembler.validateAddress(addressBean);
			if (!errorMessage.isEmpty()) {
				response.getFieldMessages().put(PAB_ZIP,errorMessage);
			}	
		}
		catch(AtWinXSException e)
		{
			Util.asCPRPCException(e);
		}
		
		
		if (response.getFieldMessages().size() > 0) {
			res=false;
	    }	
		else {
		  res=true;
		} 
		return res;
	  }
	//CAP-41593	Stop
	
	//CAP-43598 Start
	protected void validateField(PABSaveResponse response, AppSessionBean asb,boolean isRequired,
			Map<String, String> translationMap, String value, String fieldName, int maxSize) throws AtWinXSException {
		if (isRequired && Util.isBlank(value)) {
			response.getFieldMessages().put(fieldName,
					Util.nullToEmpty(translationMap.get(fieldName)) + AtWinXSConstant.BLANK_SPACE
							+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else {
			validateField(response, asb, value, fieldName, maxSize);
		}
	}
	
	protected void validateField(PABSaveResponse response, AppSessionBean asb, String value,
			String fieldName, int maxSize) throws AtWinXSException {
		if (value.length() > maxSize) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					maxSize + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
		
	}
	
	public void validatePABFields(SessionContainer sc, PABSaveResponse response, PABSaveRequest request,
		AppSessionBean asb,	Map<String, String> translationMap,
		CountryLocatorService countryLocatorService, boolean uspsValidationFlag) throws AtWinXSException {
			
				validateField(response, asb, true, translationMap, Util.nullToEmpty(request.getName()), PAB_NAME, SELF_ADMIN_MAX_SIZE_35);
				validateField(response, asb, Util.nullToEmpty(request.getName2()), SELF_ADMIN_NAME_2, SELF_ADMIN_MAX_SIZE_35);
				validateField(response, asb, true, translationMap, Util.nullToEmpty(request.getAddress1()), PAB_ADDRESS_1, SELF_ADMIN_MAX_SIZE_35);
				validateField(response, asb, Util.nullToEmpty(request.getAddress2()), PAB_ADDRESS_2, SELF_ADMIN_MAX_SIZE_35);
				validateField(response, asb, Util.nullToEmpty(request.getAddress3()), PAB_ADDRESS_3, SELF_ADMIN_MAX_SIZE_35);
				validateField(response, asb, true, translationMap, Util.nullToEmpty(request.getCity()), SELF_ADMIN_CITY, SELF_ADMIN_MAX_SIZE_30);
				validateField(response, asb, true, translationMap, Util.nullToEmpty(request.getCountry()), PAB_CT, SELF_ADMIN_MAX_SIZE_3);
				if(getStateCheck(Util.nullToEmpty(request.getCountry()),asb, countryLocatorService)){
					validateField(response, asb, true, translationMap,Util.nullToEmpty(request.getState()), PAB_ST, SELF_ADMIN_MAX_SIZE_4);
				}else {
					validateField(response, asb, Util.nullToEmpty(request.getState()), PAB_ST , SELF_ADMIN_MAX_SIZE_4);
				}
				validateField(response, asb, Util.nullToEmpty(request.getPhoneNumber()), SELF_ADMIN_PHONE_NUMBER, SELF_ADMIN_MAX_SIZE_24);
				validateField(response, asb, Util.nullToEmpty(request.getShipToAttn()), PAB_SHIP_TO_ATTN, SELF_ADMIN_MAX_SIZE_35);
				if(Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(PAB_COUNTRY_USA)) {
					validateField(response, asb, true, translationMap, Util.nullToEmpty(request.getZip()), PAB_ZIP, SELF_ADMIN_MAX_SIZE_12);
				}else {
					validateField(response, asb, Util.nullToEmpty(request.getZip()), PAB_ZIP, SELF_ADMIN_MAX_SIZE_12);
				}
				if(!uspsValidationFlag 
						&& (Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(PAB_COUNTRY_USA)) && request.isHasPassedZipValidation()){
					validateAddress(sc,request,response);
				}
	}
	
	//CAP-44961: USPS specific PAB fields validation (DeliveryOptionsUtil=>validateAddressHelperUSPS()
	public void validatePABFieldsUSPSV1(PABSaveResponse response, PABSaveRequest request,
			AppSessionBean asb,	TranslationService translationService) throws AtWinXSException {
			
		if(Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(PAB_COUNTRY_USA)
				&& (Util.nullToEmpty(request.getZip()).trim().length() < SELF_ADMIN_CHAR_SIZE_5
						|| !Util.isInt(Util.nullToEmpty(request.getZip()).trim().substring(0,5)))) {
				
				response.getFieldMessages().put(PAB_ZIP,translationService.processMessage(asb.getDefaultLocale(), 
		        		asb.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_ZIP));
		}	
	}
	
	//CAP-44961: replace special character before sending to USPS validation
	public static String replaceAddressSpecialChar(String str) {
        
        if(!Util.isBlankOrNull(str)) {
            
            str = str.replace("&", " AND ");
            str = str.replace("<", "");
            str = str.replace(">", "");
            str = str.replace("%", "");
        }
        return str;
    }
	
	public boolean getStateCheck(String countryCd,AppSessionBean asb, 
			CountryLocatorService countryLocatorService) throws AtWinXSException
	{
		boolean stateFlag=false;
		ICountryComponent countryComp = countryLocatorService.locate(asb.getCustomToken());
		CountryWithNameVO countryNameVO = countryComp.getCountryWithName(new CountryVOKey(countryCd));
		
		if (countryNameVO != null && countryNameVO.getCountryNameLong() != null)
		{
			stateFlag = countryNameVO.getCountryHasStatesIn();
		}
		
		return stateFlag;
	}
	
	//CAP-43598 End
	
	//CAP-45374 -	USPS validation Suggested Address need to be split of over 30 characters 
	//			 	based on Word wrap. Address Line1 with Secondary Address Unit Designators 
	public static String splitUSPSAddressByDesignator(String address, boolean addressLineFlag) {
		
		StringBuilder addressBuilder = new StringBuilder();
		String tmpAddressToken="";
		StringTokenizer strAddrTokens = new StringTokenizer(address);
		
		while (strAddrTokens.hasMoreTokens()) {

			tmpAddressToken = strAddrTokens.nextToken();

			if (addressLineFlag && 
				(tmpAddressToken.equals("APT") || tmpAddressToken.equals("BLDG")|| tmpAddressToken.equals("FL")
					|| tmpAddressToken.equals("STE") || tmpAddressToken.equals("UNIT") || tmpAddressToken.equals("RM")
					|| tmpAddressToken.equals("DEPT")) 
				&& strAddrTokens.hasMoreTokens()) { 
				
				tmpAddressToken = tmpAddressToken.concat(AtWinXSConstant.BLANK_SPACE + strAddrTokens.nextToken());
			}	
			
			if ((addressBuilder.length() + tmpAddressToken.length()) >30) { 
	
				address = addressBuilder.toString();
				break;
			}
			else {
		
				addressBuilder.append(tmpAddressToken);
				addressBuilder.append(AtWinXSConstant.BLANK_SPACE);
			}
	    }
		return address.trim();
	}	

	//CAP-45375
	public static String getTranslationMessageForUSPSErrors(String errorMessage, AppSessionBean appSessionBean, TranslationService translationService) throws AtWinXSException {
		
		String translatedErrorMessage = errorMessage;
		
		if(errorMessage.startsWith("Invalid Address") || errorMessage.startsWith("Address Not Found")) { 
			translatedErrorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.USPS_VALIDATION_ADDRESS_NOT_FOUND);
		} else if(errorMessage.startsWith("Invalid Zip Code")) {
			translatedErrorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.USPS_VALIDATION_INVALID_ZIPCODE);
		} else if(errorMessage.startsWith("Invalid State Code")) {
			translatedErrorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.USPS_VALIDATION_STATE_NOT_VALID);
		} else if(errorMessage.startsWith("Invalid City")) {
			translatedErrorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.USPS_VALIDATION_INVALID_CITY);
		} else if(errorMessage.startsWith("Multiple addresses were found")) {
			translatedErrorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.USPS_VALIDATION_MULTIPLE_ADDRESSES);
		} else if(errorMessage.startsWith("Default address: The address you")) {
			translatedErrorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.USPS_VALIDATION_MORE_INFO);
		}
		return translatedErrorMessage;
		
	}
	
	//CAP-45485 - method to modify Requestor to Originator Profile in OOB Mode for Self Admin actions    
	public static boolean modifyOriginatorProfileInSelfAdmin(boolean useOriginatorProfile, AppSessionBean appSessionBean)  
			throws AtWinXSException { 
		OEResolvedUserSettingsSessionBean settings = AdminUtil.getUserSettings(new LoginVOKey(appSessionBean.getSiteID(), 
				appSessionBean.getOriginatorProfile().getLoginID()), appSessionBean.getSessionID(), appSessionBean.getCustomToken());

		if(useOriginatorProfile && settings.isAllowOrderOnBehalf() && appSessionBean.isInRequestorMode()) { 
			
			appSessionBean.setInRequestorMode(false);
			return true;
		}
		return false;
	}
	
	//CAP-45485 - method to revert Originator to Requestor Profile in OOB Mode for Self Admin actions
	public static void revertOriginatorProfileInSelfAdmin(AppSessionBean appSessionBean, boolean requestorModeModified) { 
		
		if(requestorModeModified) { 
		
			appSessionBean.setInRequestorMode(true);
		}
	}
	
	//CAP-46081
	public void validateUSPSAttributes(USPSValidationResponse response, USPSValidationRequest request,
			AppSessionBean asb,	Map<String, String> translationMap, CountryLocatorService countryLocatorService) throws AtWinXSException {
			
				validateUSPSField(response, asb, true, translationMap, Util.nullToEmpty(request.getAddress1()), PAB_ADDRESS_1, SELF_ADMIN_MAX_SIZE_35);
				validateUSPSField(response, asb, Util.nullToEmpty(request.getAddress2()), PAB_ADDRESS_2, SELF_ADMIN_MAX_SIZE_35);
				validateUSPSField(response, asb, true, translationMap, Util.nullToEmpty(request.getCity()), SELF_ADMIN_CITY, SELF_ADMIN_MAX_SIZE_30);
				validateUSPSField(response, asb, true, translationMap, Util.nullToEmpty(request.getCountry()), PAB_CT, SELF_ADMIN_MAX_SIZE_3);
				if(getStateCheck(Util.nullToEmpty(request.getCountry()),asb, countryLocatorService)){
					validateUSPSField(response, asb, true, translationMap,Util.nullToEmpty(request.getState()), PAB_ST, SELF_ADMIN_MAX_SIZE_4);
				}else {
					validateUSPSField(response, asb, Util.nullToEmpty(request.getState()), PAB_ST , SELF_ADMIN_MAX_SIZE_4);
				}
				if(Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(PAB_COUNTRY_USA)) {
					validateUSPSField(response, asb, true, translationMap, Util.nullToEmpty(request.getZip()), PAB_ZIP, SELF_ADMIN_MAX_SIZE_12);
				}else {
					validateUSPSField(response, asb, Util.nullToEmpty(request.getZip()), PAB_ZIP, SELF_ADMIN_MAX_SIZE_12);
				}
	}
	
	//CAP-46081
	protected void validateUSPSField(USPSValidationResponse response, AppSessionBean asb,boolean isRequired,
			Map<String, String> translationMap, String value, String fieldName, int maxSize) throws AtWinXSException {
		if (isRequired && Util.isBlank(value)) {
			response.getFieldMessages().put(fieldName,
					Util.nullToEmpty(translationMap.get(fieldName)) + AtWinXSConstant.BLANK_SPACE
							+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else {
			validateUSPSField(response, asb, value, fieldName, maxSize);
		}
	}
	
	//CAP-46081
	protected void validateUSPSField(USPSValidationResponse response, AppSessionBean asb, String value,
			String fieldName, int maxSize) throws AtWinXSException {
		if (value.length() > maxSize) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					maxSize + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
		
	}
	
	//CAP-46081
	public void validateUSPSAttributeZip(USPSValidationResponse response, USPSValidationRequest request,
			AppSessionBean asb,	TranslationService translationService) throws AtWinXSException {
			
		if(Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(PAB_COUNTRY_USA)
				&& (Util.nullToEmpty(request.getZip()).trim().length() < SELF_ADMIN_CHAR_SIZE_5
						|| !Util.isInt(Util.nullToEmpty(request.getZip()).trim().substring(0,5)))) {
				
				response.getFieldMessages().put(PAB_ZIP,translationService.processMessage(asb.getDefaultLocale(), 
		        		asb.getCustomToken(), TranslationTextConstants.TRANS_NM_INVALID_ZIP));
		}	
	}
	
	//CAP-45913
	public static String splitMaxCharecter(int maxLen,String fieldValue) {
		String respValue=fieldValue;
		if(fieldValue.length()>maxLen)
			respValue=fieldValue.substring(0, maxLen);
		return respValue;
	}
	 
}
