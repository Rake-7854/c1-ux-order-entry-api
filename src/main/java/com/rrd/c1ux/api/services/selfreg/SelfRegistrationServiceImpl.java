/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/27/23	L De Leon			CAP-45907				Initial Version
 *	01/12/24	Satishkumar A		CAP-46380				C1UX BE - Create api to retrieve initial user/profile information
 *	01/01/24	M Sakthi			CAP-45913				C1UX BE - Modify and test new API to pre-populate fields for Corporate Profile
 *	01/08/24	N Caceres			CAP-45910				C1UX BE - Modify and test new API to pre-populate fields for Extended Profile
 *	01/19/24    Satishkumar A		CAP-46380				C1UX BE - Create api to retrieve initial user/profile information (Added Translation)
 *	02/26/24	Krishna Natarajan	CAP-47337				Added a new method to validate pattern after user
 *	02/28/24	N Caceres			CAP-47449				C1UX BE - Retrieve password rules
 *	02/28/24	M Sakthi			CAP-47450				C1UX BE - Create API to validate the extended profile fields
 *	02/29/24	L De Leon			CAP-47516				Added validate corporate profile implementation
 *	02/28/24	S Ramachandran		CAP-47410				Added service for for save self-registration data for basic profile fields
 *	02/29/24	C Codina			CAP-47324				C1UX BE - Self Registration save corporate profile information
 *	03/02/24	M Sakthi			CAP-47323				Added service for for save self-registration data for Extended profile fields
 *	03/02/24    Satishkumar A		CAP-47592				C1UX BE - Create validation story for Password validation 
 *	03/01/24	S Ramachandran		CAP-47629				Added serviceImpl method to validate basic profile fields
 *	03/05/24	Satishkumar A		CAP-47616				Added service method to validate the User Defined Fields
 *	03/05/24	N Caceres			CAP-47670				Added method for saving UDFs for self registration
 *	03/06/24	Satishkumar A		CAP-47672				C1UX BE - Create Validation method for UDF fields for Self Registration	
 *  03/07/24	M Sakthi			CAP-47657				C1UX BE - Create Validation method for Attributes for Self Registration	
 *	03/07/24	L De Leon			CAP-47615				Modified getInitialSelfRegUser() to add call to populate profile site attributes
 *	03/06/24	Satishkumar A		CAP-47672				C1UX BE - Create Validation method for UDF fields for Self Registration	
 *	03/07/24	C Codina			CAP-47669				C1UX BE - Modify Existing API to save the Attribute Information for a Self Regitration
 *	03/07/24	Krishna Natarajan	CAP-47671				Added a new lines of code to set the loginUrl in response
 *	03/11/24	Satishkumar A		CAP-47592				Password max length error message changed
 *	03/12/24	L De Leon			CAP-47775				Added methods to validate and set default address code value
 *	03/12/24	Krishna Natarajan	CAP-47671				Added a method to generate the tbomb + refined the existing code
 *	03/12/24	Satishkumar A		CAP-47842				C1UX BE - API Fix - saveselfregistration -  it returns a 500 status code when all of the values for the corporate profile fields are passed
 *	03/13/24	C Codina			CAP-47853				C1UX BE - API Fix - saveselfregistration - It does not return max length validation error message for the state field.
 *	03/13/24	Krishna Natarajan	CAP-47671				Added few extra lines of code to set the ORIG-tstamp and ORIG-tbomb		
 *  03/20/24	T Harmon			CAP-48077				Fixed issue to pass back user id when coming in via failureAction=USR	
 *  03/21/24	Krishna Natarajan	CAP-48126				Added a nulltoempty conversion on the value passed for validation in validateField method
 *  04/02/24	Krishna Natarajan	CAP-48395				Added a logic in else if block in validateStateField method to restrict state field validation
 *  05/09/24	Krishna Natarajan	CAP-49057				Added a logic in validating Pattern after user in validate basic profile method
 */
package com.rrd.c1ux.api.services.selfreg;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.velocity.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributeValue;
import com.rrd.c1ux.api.models.admin.UserDefinedfieldsResponse;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationPatternAfterResponse;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveRequest;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveResponse;
import com.rrd.c1ux.api.models.users.C1UXProfile;
import com.rrd.c1ux.api.models.users.C1UXProfileImpl;
import com.rrd.c1ux.api.models.users.SelfAdminSiteAttributeValues;
import com.rrd.c1ux.api.models.users.SelfAdminSiteAttributes;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.admin.locators.PasswordComponentLocatorService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.admin.entity.SelfRegistration;
import com.rrd.custompoint.admin.entity.SiteAttributeValue;
import com.rrd.custompoint.admin.entity.SiteAttributeValues;
import com.rrd.custompoint.admin.profile.dao.ProfileDAO;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;
import com.rrd.custompoint.admin.profile.entity.ProfileImpl;
import com.rrd.custompoint.admin.profile.entity.ReferenceFieldDefinition.DisplayType;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.rrd.custompoint.framework.login.LoginConstants;
import com.wallace.atwinxs.admin.util.PasswordSecuritySettings;
import com.wallace.atwinxs.admin.vo.BusinessUnitVO;
import com.wallace.atwinxs.admin.vo.BusinessUnitVOKey;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.systemfunctions.SystemFunctionsConstants;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PingFederationConstants;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.Util.ContextPath;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.IBusinessUnit;
import com.wallace.atwinxs.interfaces.ILoginInterface;
import com.wallace.atwinxs.interfaces.IPassword;
import com.wallace.atwinxs.locale.ao.CountryBean;

@Service
public class SelfRegistrationServiceImpl extends BaseOEService implements SelfRegistrationService {

	private static final String USA = "USA";
	private static final Logger logger = LoggerFactory.getLogger(SelfRegistrationServiceImpl.class);
	// CAP-45910
	public static final int MAX_LENGTH_1 = 1;
	public static final int MAX_LENGTH_3 = 3;
	public static final int MAX_LENGTH_4 = 4;
	public static final int MAX_LENGTH_12 = 12;
	public static final int MAX_LENGTH_10 = 10;
	public static final int MAX_LENGTH_15 = 15;
	public static final int MAX_LENGTH_24 = 24;
	public static final int MAX_LENGTH_30 = 30;
	public static final int MAX_LENGTH_35 = 35;
	public static final int MAX_LENGTH_50 = 50;
	public static final int MAX_LENGTH_255 = 255;
	public static final String CP_PREFIX = "cp";
	//CAP-47592
	private static final String VALIDATE_PASSWORD = "validatePassword";
	private static final String PASSWORD_LBL = "password_lbl";

	// CAP-47775
	public static final String DEFAULT_ADDRESS_CD = "defaultAddressCd";
	public static final String DEFAULT_CORPORATE_CD = "C";
	public static final String DEFAULT_EXTENDED_CD = "E";
	

	private IAdminComponentLocator adminComponentLocator;
	private PasswordComponentLocatorService passwordComponentLocatorService;
	private SelfAdminService selfAdminService;

	protected SelfRegistrationServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService, IAdminComponentLocator adminComponentLocator,
			PasswordComponentLocatorService passwordComponentLocatorService, SelfAdminService selfAdminService) {
		super(translationService, objectMapFactoryService);
		this.adminComponentLocator = adminComponentLocator;
		this.passwordComponentLocatorService = passwordComponentLocatorService;
		this.selfAdminService = selfAdminService;
	}

	@Override
	public SelfRegistrationPatternAfterResponse getPatternAfterUsers(SessionContainer sc) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

		SelfRegistrationPatternAfterResponse response = new SelfRegistrationPatternAfterResponse();
		response.setSuccess(true);
		List<LoginVOKey> patternAfterUsers = new ArrayList<>();

		try {
			patternAfterUsers = retrievePatternAfterUsers(appSessionBean);
		} catch (AtWinXSException e) {
			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}

		response.setPatternAfterUsers(patternAfterUsers);

		return response;
	}

	protected List<LoginVOKey> retrievePatternAfterUsers(AppSessionBean appSessionBean) throws AtWinXSException {
		SelfRegistration selfRegistration = objectMapFactoryService.getEntityObjectMap()
				.getEntity(SelfRegistration.class, appSessionBean.getCustomToken());
		selfRegistration.init(appSessionBean.getLoginQueryString(), appSessionBean.getSiteID(),
				appSessionBean.getLoginID());

		int paBU = selfRegistration.getPaBU();

		if (paBU > -1) {
			IBusinessUnit buComponent = adminComponentLocator
					.locateBusinessUnitComponent(appSessionBean.getCustomToken());
			BusinessUnitVO businessUnit = buComponent.findByBuID(paBU);

			if (null == businessUnit) {
				paBU = -1;
			}
		}

		ILoginInterface loginComponent = adminComponentLocator.locateLoginComponent(appSessionBean.getCustomToken());
		Collection<LoginVOKey> patternAfterUsers = loginComponent.getPAAccounts(appSessionBean.getSiteID(), paBU);

		return patternAfterUsers.stream().collect(Collectors.toList());
	}
	
	//CAP-46380
	@Override
	public SelfRegistrationPatternAfterResponse getInitialSelfRegUser(SessionContainer sc, String patternAfterUser) throws AtWinXSException, IllegalAccessException, InvocationTargetException {
		
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		SelfRegistrationPatternAfterResponse response = new SelfRegistrationPatternAfterResponse();
		response.setSuccess(true);
		try {
		SelfRegistration selfRegistration = objectMapFactoryService.getEntityObjectMap()
				.getEntity(SelfRegistration.class, appSessionBean.getCustomToken());
		
		
		String prepend = AtWinXSConstant.AUTO_URL_SELFREG_PREPEND;
		
		// Determine if this is a module = SR request - if so, we don't need to prepend
		String checkParm = Util.nullToEmpty(appSessionBean.getLoginQueryString().getProperty(prepend + AtWinXSConstant.AUTO_URL_USERNAME));
		if (checkParm.isEmpty())
		{
			prepend = AtWinXSConstant.EMPTY_STRING;
		}
		int paBU = Util.safeStringToDefaultInt(appSessionBean.getLoginQueryString().getProperty(prepend + "PABU"), -1); // CAP-3970
		
		selfRegistration.setSiteID(appSessionBean.getSiteID());
		selfRegistration.setPaBU(paBU);
		selfRegistration.setSelectedPatternAfterUser(patternAfterUser);
		selfRegistration.populatePatternAfterUsers();

		if(null == selfRegistration.getPatternAfterUsers() || selfRegistration.getPatternAfterUsers().isEmpty()) {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.SELF_REGISTRATION_ERROR));
			return response;
		}
		boolean patternAfterUserValid = false;
		for(User user : selfRegistration.getPatternAfterUsers()) {
			if(patternAfterUser.equalsIgnoreCase(user.getLoginID())) {
				patternAfterUserValid = true;
				break;
			}
		}
		if(!patternAfterUserValid) {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.SELF_REGISTRATION_ERROR));
			return response;
		}
			
		selfRegistration.init(appSessionBean.getLoginQueryString(), appSessionBean.getSiteID(),
				appSessionBean.getLoginID());
		
		String paModule = Util.nullToEmpty(appSessionBean.getLoginQueryString().getProperty(prepend + "Module"));
		if(LoginConstants.ENTRY_PT_SELF_REG.equalsIgnoreCase(paModule)) {
			response.setEditablePassword(true);
			response.setEditableUser(true);
		} else {
			response.setEditablePassword(false);
			response.setEditableUser(false);

		}
		
		//CAP-45913
		if (selfRegistration.getProfile().getProfileDefinition().isCanUserEditCorporateProfile()) {
				validateCorporateProfile(selfRegistration);
		} else {
				clearCorporateProfile(selfRegistration);
		}
		
		// CAP-45910
		if (selfRegistration.getProfile().getProfileDefinition().isCanUserEditExtendedProfile()) {
			validateExtendedProfile(selfRegistration);
		} else {
			clearExtendedProfile(selfRegistration);
		}
		
		// CAP-47449
		populatePasswordRules(response, appSessionBean.getCustomToken(), selfRegistration.getPatternAfterUserSelected());
				
		C1UXProfile c1uxProfile = new C1UXProfileImpl();
		BeanUtils.copyProperties(c1uxProfile, selfRegistration.getProfile());

		// CAP-47615
		populateProfileSiteAttributes(selfRegistration, c1uxProfile);

		response.setC1uxProfile(c1uxProfile);
		response.setLoginID(Util.nullToEmpty(selfRegistration.getLoginID()));  // CAP-48077
		} catch(Exception e) {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.SELF_REGISTRATION_ERROR));
		}
		
		return response; 
	}

	// CAP-47615
	protected void populateProfileSiteAttributes(SelfRegistration selfRegistration, C1UXProfile c1uxProfile)
			throws AtWinXSException {
		if (selfRegistration.getProfile().hasSiteAttributes()) {
			SiteAttributes siteAttributes = selfRegistration.getProfile().getSiteAttributes();
			List<SelfAdminSiteAttributes> selfAdminSiteAttributes = populateSiteAttributeList(siteAttributes);
			c1uxProfile.setSelfAdminSiteAttributes(selfAdminSiteAttributes);
		}
	}

	// CAP-47615
	protected List<SelfAdminSiteAttributes> populateSiteAttributeList(SiteAttributes siteAttributes)
			throws AtWinXSException {
		List<SelfAdminSiteAttributes> selfAdminSiteAttributes = new ArrayList<>();
		if (null != siteAttributes && null != siteAttributes.getSiteAttrs()) {
			for (SiteAttribute tempSiteAttribute : siteAttributes.getSiteAttrs()) {
				SelfAdminSiteAttributes siteAttribute = new SelfAdminSiteAttributes();
				siteAttribute.setAttributeID(tempSiteAttribute.getAttrID());
				siteAttribute.setAttributeDisplayName(tempSiteAttribute.getAttrDisplayName());
				siteAttribute.setDisplayType(tempSiteAttribute.getDisplayType().name());
				siteAttribute.setMaxAttributeValues(tempSiteAttribute.getMaxRequired());
				siteAttribute.setMinAttributeValues(tempSiteAttribute.getMinRequired());
				siteAttribute.setViewOnly(tempSiteAttribute.getDisplayType().equals(DisplayType.ViewOnly));
				setSiteAttributes(tempSiteAttribute, siteAttribute);

				selfAdminSiteAttributes.add(siteAttribute);
			}
		}
		return selfAdminSiteAttributes;
	}

	// CAP-47615
	protected void setSiteAttributes(SiteAttribute tempSiteAttribute, SelfAdminSiteAttributes siteAttribute)
			throws AtWinXSException {
		List<SiteAttributeValue> tempSiteAttrValues =  tempSiteAttribute.getSiteAttributeValuesForProfile()
				.getSiteAttributeValues();
		if (null == tempSiteAttrValues) {
			tempSiteAttrValues = new ArrayList<>();
		}

		List<SiteAttributeValue> tempAssignedValues = tempSiteAttrValues.stream()
				.filter(attrValue -> Boolean.valueOf(attrValue.isAssigned())).collect(Collectors.toList());
		List<SelfAdminSiteAttributeValues> assignedAttributes = populateSiteAttributeValueList(
				tempAssignedValues);
		siteAttribute.setAssignedAttributes(assignedAttributes);
		
		List<SiteAttributeValue> tempAvailableValues = tempSiteAttrValues.stream()
				.filter(attrValue -> !attrValue.isAssigned()).collect(Collectors.toList());
		List<SelfAdminSiteAttributeValues> availableAttributes = populateSiteAttributeValueList(
				tempAvailableValues);
		siteAttribute.setAvailableAttributes(availableAttributes);
	}

	// CAP-47615
	protected List<SelfAdminSiteAttributeValues> populateSiteAttributeValueList(
			List<SiteAttributeValue> tempSiteAttributeValues) {
		List<SelfAdminSiteAttributeValues> siteAttributeValues = new ArrayList<>();
		for (SiteAttributeValue tempAttrValue : tempSiteAttributeValues) {
			SelfAdminSiteAttributeValues attrValue = new SelfAdminSiteAttributeValues();
			attrValue.setAttributeValueID(tempAttrValue.getSiteAttrValID());
			attrValue.setAttribtueValueDescription(tempAttrValue.getSiteAttrValDesc());
			siteAttributeValues.add(attrValue);
		}
		return siteAttributeValues;
	}
	
	
	// CAP-45910
	protected void validateExtendedProfile(SelfRegistration selfRegistration) throws AtWinXSException {
		validateExtendedName(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactName2(), false);
		validateExtendedAddressOne(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getLine1Address(), false);
		validateExtendedAddressTwo(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getLine2Address(), false);
		validateExtendedCity(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getCityName(), false);
		validateExtendedState(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getStateCd(), false);
		validateExtendedZip(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getZipCd(), false);
		validateExtendedCountry(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getCountryCd(), false);
		validateExtendedMiddleInitial(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactMiddleInitialName(), false);
		validateExtendedTitle(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactTitleName(), false);
		validateExtendedSuffix(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactSuffixName(), false);
		validateExtendedFaxNumber(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactFaxNumber(), false);
		validateExtendedMobileNumber(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactMobileNumber(), false);
		validateExtendedPagerNumber(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactPagerNumber(), false);
		validateExtendedTollFreeNumber(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactTollFreePhoneNumber(), false);
		validateExtendedWebURL(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactWebAddress(), false);
		validateExtendedPhotoURL(selfRegistration, selfRegistration.getProfile().getExtendedProfile().getContactPhotoURLAddress(), false);
	}
	
	protected void clearExtendedProfile(SelfRegistration selfRegistration) throws AtWinXSException {
		validateExtendedName(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedAddressOne(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedAddressTwo(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedCity(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedState(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedZip(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedCountry(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedMiddleInitial(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedTitle(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedSuffix(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedFaxNumber(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedMobileNumber(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedPagerNumber(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedTollFreeNumber(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedWebURL(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateExtendedPhotoURL(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
	}

	private void validateExtendedPhotoURL(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactPhotoURLAddress(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_255, value));
		}
	}

	private void validateExtendedWebURL(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactWebAddress(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_255, value));
		}
	}

	private void validateExtendedTollFreeNumber(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactTollFreePhoneNumber(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_24, value));
		}
	}

	private void validateExtendedPagerNumber(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactPagerNumber(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_24, value));
		}
	}

	private void validateExtendedMobileNumber(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactMobileNumber(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_24, value));
		}
	}

	private void validateExtendedFaxNumber(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactFaxNumber(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_24, value));
		}
	}

	private void validateExtendedSuffix(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactSuffixName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_15, value));
		}
	}

	private void validateExtendedTitle(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactTitleName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_10, value));
		}
	}

	private void validateExtendedMiddleInitial(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactMiddleInitialName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_1, value));
		}
	}

	private void validateExtendedCountry(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) && !shouldClearData) {
			CountryBean country = UtilCountryInfo.getCountry(value);
			if (null != country) {
				selfRegistration.getProfile().getExtendedProfile().setCountryCd(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_30, value));
			} else {
				selfRegistration.getProfile().getExtendedProfile().setCountryCd(USA);
			}
		} else {
			selfRegistration.getProfile().getExtendedProfile().setCountryCd(value);
		}
	}

	private void validateExtendedZip(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setZipCd(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_12, value));
		}
	}

	private void validateExtendedState(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setStateCd(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_4, value));
		}
	}

	private void validateExtendedCity(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setCityName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_30, value));
		}
	}

	private void validateExtendedAddressTwo(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setLine2Address(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_35, value));
		}
	}

	private void validateExtendedAddressOne(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setLine1Address(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_35, value));
		}
	}

	private void validateExtendedName(SelfRegistration selfRegistration, String value, boolean shouldClearData)
			throws AtWinXSException {
		if (!Util.isBlankOrNull(value) || shouldClearData) {
			selfRegistration.getProfile().getExtendedProfile().setContactName2(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_50, value));
		}
	}
	
	
	//CAP-45913
	protected void validateCorporateProfile(SelfRegistration selfRegistration) throws AtWinXSException {
		validateCorporateCompanyName(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyName(), false);
		validateCorporateCompanyName2(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyName2(), false);
		validateCorporateLineAddr1(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getLine1Address(), false);
		validateCorporateLineAddr2(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getLine2Address(), false);
		validateCorporateCiteName(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCityName(), false);
		validateCorporateStateCd(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getStateCd(), false);
		validateCorporateZipCd(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getZipCd(), false);
		validateCorporateCountry(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCountryCd(), false);
		validateCorporatePhoneNumber(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyPhoneNumber(), false);
		validateCorporateFaxNumber(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyFaxNumber(), false);
		validateCorporateTitleText(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyTitleText(), false);
		validateCorporateDivisionName(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyDivisionName(), false);
		validateCorporateDeportmentName(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyDepartmentName(), false);
		validateCorporateWebURL(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyURLAddress(), false);
		validateCorporateLogoURL(selfRegistration, selfRegistration.getProfile().getCorporateProfile().getCompanyLogoURLAddress(), false);
	}	
			
	protected void clearCorporateProfile(SelfRegistration selfRegistration) throws AtWinXSException {
		validateCorporateCompanyName(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateCompanyName2(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateLineAddr1(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateLineAddr2(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateCiteName(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateStateCd(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateZipCd(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateCountry(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporatePhoneNumber(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateFaxNumber(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateTitleText(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateDivisionName(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateDeportmentName(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateWebURL(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
		validateCorporateLogoURL(selfRegistration, AtWinXSConstant.EMPTY_STRING, true);
	}	
		
		//Validate corporate Profile
		private void validateCorporateCompanyName(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_35,value));
			}
		}
		
		private void validateCorporateCompanyName2(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyName2(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_35,value));
			}
		}
		
		private void validateCorporateLineAddr1(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setLine1Address(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_35,value));
			}
		}

		private void validateCorporateLineAddr2(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setLine2Address(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_35,value));
			}
		}
		
		private void validateCorporateCiteName(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCityName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_30,value));
			}
		}
		
		private void validateCorporateStateCd(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setStateCd(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_4,value));
			}
		}
		
		private void validateCorporateZipCd(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setZipCd(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_12,value));
			}
		}
		
		
		private void validateCorporateCountry(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) && !shouldClearData) {
				CountryBean country = UtilCountryInfo.getCountry(value);
				if (null != country) {
					selfRegistration.getProfile().getCorporateProfile().setCountryCd(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_30, value));
				} else {
					selfRegistration.getProfile().getCorporateProfile().setCountryCd(USA);
				}
			} else {
				selfRegistration.getProfile().getExtendedProfile().setCountryCd(value);
			}
		}
		
		private void validateCorporatePhoneNumber(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyPhoneNumber(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_24, value));
			}
		}
		
		private void validateCorporateFaxNumber(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyFaxNumber(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_24, value));
			}
		}
		
		private void validateCorporateTitleText(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyTitleText(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_50, value));
			}
		}
		
		private void validateCorporateDivisionName(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyDivisionName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_30, value));
			}
		}
		
		private void validateCorporateDeportmentName(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyDepartmentName(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_30, value));
			}
		}
		
		private void validateCorporateWebURL(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyURLAddress(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_255, value));
			}
		}
		
		private void validateCorporateLogoURL(SelfRegistration selfRegistration, String value, boolean shouldClearData)
				throws AtWinXSException {
			if (!Util.isBlankOrNull(value) || shouldClearData) {
				selfRegistration.getProfile().getCorporateProfile().setCompanyLogoURLAddress(SelfAdminUtil.splitMaxCharecter(MAX_LENGTH_255, value));
			}
		}
		
		// CAP-47337
		public SelfRegistrationSaveResponse validatePatternAfter(SessionContainer sc, String patternAfterUser)
				throws AtWinXSException {

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			SelfRegistrationSaveResponse response = new SelfRegistrationSaveResponse();
			response.setSuccess(false);
			try {
				if (patternAfterUser.length() > ModelConstants.NUMERIC_16) {
					response.setSuccess(false);
					response.setFieldMessage("selectedPatternAfter",translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_LENGTH_PATTERN));
				} else {
					boolean verifyPatternAfterBU = checkPAUser(appSessionBean, patternAfterUser);
					if (verifyPatternAfterBU) {
						response.setSuccess(true);
					} else {
						response.setSuccess(false);
						response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
								appSessionBean.getCustomToken(), SFTranslationTextConstants.INVALID_PATTERN_AFTER));
					}
				}
			} catch (Exception e) {
				response.setSuccess(false);
				response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), SFTranslationTextConstants.INVALID_PATTERN_AFTER));
			}
			return response;
		}
		
		// CAP-47337
		public boolean checkPAUser(AppSessionBean appSessionBean, String patternAfterUser) throws AtWinXSException {
			boolean verifyPatternAfterBU = false;
			SelfRegistration selfRegistration = objectMapFactoryService.getEntityObjectMap()
					.getEntity(SelfRegistration.class, appSessionBean.getCustomToken());
			selfRegistration.init(appSessionBean.getLoginQueryString(), appSessionBean.getSiteID(),
					appSessionBean.getLoginID());

			int paBU = selfRegistration.getPaBU();

			if (paBU > -1) {
				IBusinessUnit buComponent = adminComponentLocator
						.locateBusinessUnitComponent(appSessionBean.getCustomToken());
				BusinessUnitVO businessUnit = buComponent.findByBuID(paBU);

				if (null == businessUnit) {
					paBU = -1;
				}
			}

			ILoginInterface loginComponent = adminComponentLocator
					.locateLoginComponent(appSessionBean.getCustomToken());
			Collection<LoginVOKey> patternAfterUsers = loginComponent.getPAAccounts(appSessionBean.getSiteID(), paBU);

			if (patternAfterUsers != null && !patternAfterUsers.isEmpty()) {
				for (LoginVOKey login : patternAfterUsers) {
					if (login.getLoginID().equalsIgnoreCase(patternAfterUser)) {
						verifyPatternAfterBU = true;
					}
				}
			}
			return verifyPatternAfterBU;
			
		}
		// CAP-47449
		private void populatePasswordRules(SelfRegistrationPatternAfterResponse response, CustomizationToken token, 
				User patternAfterUser) throws AtWinXSException {
			IPassword password = passwordComponentLocatorService.locate(token);
			PasswordSecuritySettings passwordRule = password.getPasswordSecuritySettings(
					new BusinessUnitVOKey(patternAfterUser.getSiteID(), patternAfterUser.getBusinessUnitID()),
					new UserGroupVOKey(patternAfterUser.getSiteID(), patternAfterUser.getBusinessUnitID(), patternAfterUser.getUserGroupName()),
					new LoginVOKey(patternAfterUser.getSiteID(), patternAfterUser.getLoginID()));
			response.setMinimumPasswordChars(passwordRule.getMinimumPasswordChars());
			response.setMinimumPasswordNumericChars(passwordRule.getMinimumPasswordNumericChars());
			response.setMinimumSpecialChars(passwordRule.getMinimumSpecialChars());
			response.setMinimumUpperCaseChars(passwordRule.getMinimumUpperCaseChars());
		}
		
		
		//CAP-47450
		@Override
		public SelfRegistrationSaveResponse validateExtendedProfile(SessionContainer sc,
				SelfRegistrationSaveRequest request) throws AtWinXSException {

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			SelfRegistrationSaveResponse response = validatePatternAfter(sc, request.getSelectedPatternAfter());

			if (response.isSuccess()) {
				validateExtendedProfile(request, appSessionBean, response);
			}
			return response;
		}

		// CAP-47450
		protected void validateExtendedProfile(SelfRegistrationSaveRequest request, AppSessionBean appSessionBean,
				SelfRegistrationSaveResponse response) throws AtWinXSException {
			validateExtendedProfile(request, appSessionBean, response, null);
		}

		// CAP-47450
		protected void validateExtendedProfile(SelfRegistrationSaveRequest request, AppSessionBean appSessionBean,
				SelfRegistrationSaveResponse response, SelfRegistration selfRegistration) throws AtWinXSException {
			if (null == selfRegistration) {
				selfRegistration = getSelfRegistration(appSessionBean, request.getSelectedPatternAfter());
			}

			ProfileDefinition profileDefinition = selfRegistration.getProfile().getProfileDefinition();

			if (appSessionBean.getProfileNumber() <= 0) {

				logger.error(getErrorPrefix(appSessionBean), SelfAdminUtil.SELF_ADMIN_NO_PROFILE_SELECTED_ERROR);
				throw new AccessForbiddenException(this.getClass().getName());
			}

			if (profileDefinition.isExtendedProfileExists()) {
				validateExtendedProfiles(request, response, appSessionBean, profileDefinition);
			}

			response.setSuccess(response.getFieldMessages().isEmpty());
		}

		public void validateExtendedProfiles(SelfRegistrationSaveRequest request,SelfRegistrationSaveResponse response,AppSessionBean asb,ProfileDefinition profileDefinition) throws AtWinXSException {
			if(profileDefinition.isExtendedProfileExists()) {
				Properties translationProps = translationService.getResourceBundle(asb,
						SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
					Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);
					
					SelfAdminUtil selfAdminUtil = new com.rrd.c1ux.api.util.SelfAdminUtil();
					Map<String, String> validStateAndCountry = selfAdminUtil.validateAndGetCountryStateName(request.getEpCountry(),
							request.getEpState());
					
					String countryExist = validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS);
					String stateExist = validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS);
					validateEpContactName(request, response, asb);
					validateEpAddress1(request, response, asb,profileDefinition, translationMap);
					validateEpAddress2(request, response, asb);
					validateEpAddress3(request, response, asb);
					validateEpCityName(request, response,asb,profileDefinition,translationMap);
					validateEpCountry(request, countryExist,  response, asb, profileDefinition,translationMap);
					validateEpState(request, countryExist, stateExist, profileDefinition, response, asb, translationMap);
					validateEpZip(request, response, asb,profileDefinition,translationMap);
					validateEpMinitial(request, response,asb,profileDefinition,translationMap);
					validateEpTitle(request, response,asb,profileDefinition, translationMap);
					validateEpSuffix(request, response,asb,profileDefinition, translationMap);
					validateEpFax(request, response,asb,profileDefinition, translationMap);
					validateEpMobileNo(request, response,asb,profileDefinition, translationMap);
					validateEpPagernum(request, response,asb,profileDefinition, translationMap);
					validateEpTollFreenum(request, response,asb,profileDefinition, translationMap);
					validateEpWeburl(request, response,asb,profileDefinition, translationMap);
					validateEpPhotoUrl(request, response,asb,profileDefinition, translationMap);
					// CAP-47775
					validateDefaultAddressField(response, asb, translationMap, request.getDefaultAddressSourceCd(),
							DEFAULT_ADDRESS_CD, profileDefinition);
			}	
		}		
		
		
		protected void validateEpContactName(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb) throws AtWinXSException {
			String value = request.getEpName2();
			String fieldName = "epName2";
			if (value.length() > ModelConstants.NUMERIC_30) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_30 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}
		
		

		protected void validateEpAddress1(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
			String value = request.getEpAddr1();
			String fieldName = "epAddr1";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			
			else if (value.length() > ModelConstants.NUMERIC_30) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_30 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpAddress2(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb) throws AtWinXSException {
			String value = request.getEpAddr2();
			String fieldName = "epAddr2";
			if (value.length() > ModelConstants.NUMERIC_30) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_30 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpAddress3(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb) throws AtWinXSException {
			String value = request.getEpAddr3();
			String fieldName = "epAddr3";
			if (value.length() > ModelConstants.NUMERIC_30) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_30 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}
		
		
		protected void validateEpCountry(SelfRegistrationSaveRequest request,String countryExist, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
			
			String value = request.getEpCountry();
			String fieldName = "epCountry";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			
			else if (value.length() > ModelConstants.NUMERIC_3) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_3 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}else if ((value.length() > 0 && value.length() <= ModelConstants.NUMERIC_3)
					&& countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO)) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
			}
		}

		protected void validateEpCityName(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
			String value = request.getEpCity();
			String fieldName = "epCity";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			
			else if (value.length() > ModelConstants.NUMERIC_30) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_30 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}
		
		
		
		protected void validateEpState(SelfRegistrationSaveRequest request, String countryExist, String stateExist,
				ProfileDefinition def, SelfRegistrationSaveResponse response, AppSessionBean asb,
				Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpState();
			String fieldName = "epState";
			String label = Util.nullToEmpty(translationMap.get(fieldName));
			if (def.isAddressRequired() && countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NA)
					&& (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			} else if (countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NA)
					&& value.length() > ModelConstants.NUMERIC_4) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_4 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			} else if ((value.length() > 0 && value.length() <= ModelConstants.NUMERIC_4)
					&& ((countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NA))
							|| (countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_YES) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO)))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
			}
		}

		protected void validateEpZip(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
			String value = request.getEpZip();
			String fieldName = "epZip";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_12) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_12 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpMinitial(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpMinitial();
			String fieldName = "mInitial";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isMiddleInitialRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_1) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_1 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpTitle(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpTitle();
			String fieldName = "epTitle";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isTitleRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_10) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_10 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpSuffix(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpSuffix();
			String fieldName = "epSuffix";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isSuffixRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_15) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_15 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpFax(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpFaxnum();
			String fieldName = "epFaxnum";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isFaxRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_24) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_24 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpMobileNo(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpMobilenum();
			String fieldName = "epMobilenum";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isMobileRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_24) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_24 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpPagernum(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpPagernum();
			String fieldName = "epPagernum";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isPagerRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_24) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_24 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpTollFreenum(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpTollfreenum();
			String fieldName = "epTollfreenum";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isTollFreeRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_24) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_24 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpWeburl(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpWeburl();
			String fieldName = "epWeburl";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isWebRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_255) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_255 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		protected void validateEpPhotoUrl(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
				AppSessionBean asb,ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

			String value = request.getEpPhotourl();
			String fieldName = "epPhotourl";
			String label = Util.nullToEmpty(translationMap.get(fieldName));

			if (def.isPhotoURLRequired() && (value == null || Util.isBlank(value.trim()))) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			else if (value.length() > ModelConstants.NUMERIC_255) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_255 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fieldName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}

		private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
				throws AtWinXSException {

			return Util.nullToEmpty(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
					errorKey, replaceMap));
		}

	// CAP-47516
	@Override
	public SelfRegistrationSaveResponse validateCorporateProfile(SessionContainer sc,
			SelfRegistrationSaveRequest request) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		SelfRegistrationSaveResponse response = validatePatternAfter(sc, request.getSelectedPatternAfter());

		if (response.isSuccess()) {
			validateCorporateProfile(request, appSessionBean, response);
		}
		return response;
	}

	// CAP-47516
	protected void validateCorporateProfile(SelfRegistrationSaveRequest request, AppSessionBean appSessionBean,
			SelfRegistrationSaveResponse response) throws AtWinXSException {
		validateCorporateProfile(request, appSessionBean, response, null);
	}
	
	//CAP-47324
	protected void populateSelfRegistrationCorporateProfile(SelfRegistrationSaveRequest request,
			SelfRegistration selfRegistration) throws AtWinXSException {

		selfRegistration.getProfile().getCorporateProfile().setCompanyName(request.getCpName1());
		selfRegistration.getProfile().getCorporateProfile().setCompanyName2(request.getCpName2());
		selfRegistration.getProfile().getCorporateProfile().setLine1Address(request.getCpAddressLine1());
		selfRegistration.getProfile().getCorporateProfile().setLine2Address(request.getCpAddressLine2());
		selfRegistration.getProfile().getCorporateProfile().setLine3Address(request.getCpAddressLine3());
		selfRegistration.getProfile().getCorporateProfile().setCityName(request.getCpCity());
		selfRegistration.getProfile().getCorporateProfile().setStateCd(request.getCpStateCd());
		selfRegistration.getProfile().getCorporateProfile().setZipCd(request.getCpZipCd());
		selfRegistration.getProfile().getCorporateProfile().setCountryCd(request.getCpCountryCd());
		selfRegistration.getProfile().getCorporateProfile().setCompanyPhoneNumber(request.getCpPhoneNumber());
		selfRegistration.getProfile().getCorporateProfile().setCompanyFaxNumber(request.getCpFaxNumber());
		selfRegistration.getProfile().getCorporateProfile().setCompanyTitleText(request.getCpTitle());
		selfRegistration.getProfile().getCorporateProfile().setCompanyDivisionName(request.getCpDivision());
		selfRegistration.getProfile().getCorporateProfile().setCompanyDepartmentName(request.getCpDepartment());
		selfRegistration.getProfile().getCorporateProfile().setCompanyURLAddress(request.getCpWebUrl());
		selfRegistration.getProfile().getCorporateProfile().setCompanyLogoURLAddress(request.getCpImageUrl());

	}
	//CAP-47669
	protected void buildSiteAttributes(SelfRegistrationSaveRequest request, SelfRegistration selfRegistration) 
			throws AtWinXSException {

		List<C1UserSiteAttribute> c1UserSiteAttributesList = request.getC1UserSiteAttributes();

		SiteAttributes siteAttributes = selfRegistration.getProfile().getSiteAttributes();

		//CAP-47842
		if (null != siteAttributes && null != c1UserSiteAttributesList) {
			List<SiteAttribute> siteAttributeList = siteAttributes.getSiteAttrs();
			if (null != siteAttributeList && !siteAttributeList.isEmpty()) {
				for (SiteAttribute siteAttribute : siteAttributes.getSiteAttrs()) {
					C1UserSiteAttribute userSiteAttribute = c1UserSiteAttributesList.stream().
							filter(siteAttr -> siteAttribute.getAttrID() == siteAttr.getAttributeID())
							.findAny().orElse(null);

					if (null != userSiteAttribute) {	
						populateSiteAttributeValueID(siteAttribute, userSiteAttribute);
					}

				}
				selfRegistration.getProfile().setSiteAttributes(siteAttributes);

			}
		}
	}
	//CAP-47669
	private void populateSiteAttributeValueID(SiteAttribute siteAttribute, C1UserSiteAttribute userSiteAttribute)
			throws AtWinXSException {
		SiteAttributeValues siteAttrValues =  siteAttribute.getSiteAttributeValuesForProfile();
		for (SiteAttributeValue siteAttributeValue : siteAttrValues.getSiteAttributeValues()) {
			List<C1UserSiteAttributeValue> userSiteAttributeValues = userSiteAttribute.getC1UserSiteAttributeValues();
			if (null != userSiteAttributeValues && !userSiteAttributeValues.isEmpty()){
				C1UserSiteAttributeValue userSiteAttributeValue = userSiteAttributeValues.stream()
						.filter(siteAttrValueID -> siteAttributeValue.getSiteAttrValID() == 
						siteAttrValueID.getAttributeValueID()).findAny().orElse(null);

				siteAttributeValue.setAssigned(null != userSiteAttributeValue);						 

			}

		}
	}

	// CAP-47516
	protected void validateCorporateProfile(SelfRegistrationSaveRequest request, AppSessionBean appSessionBean,
			SelfRegistrationSaveResponse response, SelfRegistration selfRegistration) throws AtWinXSException {
		if (null == selfRegistration) {
			selfRegistration = getSelfRegistration(appSessionBean, request.getSelectedPatternAfter());
		}

		ProfileDefinition profileDefinition = selfRegistration.getProfile().getProfileDefinition();

		if (appSessionBean.getProfileNumber() <= 0) {

			logger.error(getErrorPrefix(appSessionBean), SelfAdminUtil.SELF_ADMIN_NO_PROFILE_SELECTED_ERROR);
			throw new AccessForbiddenException(this.getClass().getName());
		}

		if (profileDefinition.isUseCorporateProfile()) {
			validateCorporateProfileFields(request, response, appSessionBean, profileDefinition);
		}

		response.setSuccess(response.getFieldMessages().isEmpty());
	}

	// CAP-47516
	public void validateCorporateProfileFields(SelfRegistrationSaveRequest request,
			SelfRegistrationSaveResponse response, AppSessionBean appSessionBean, ProfileDefinition def) {
		Properties translationProps = translationService.getResourceBundle(appSessionBean,
				SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);
		CountryBean selectedCountry = UtilCountryInfo.getCountry(request.getCpCountryCd());

		validateField(response, appSessionBean, def.isCompanyNameRequired(), translationMap, request.getCpName1(),
				SelfAdminUtil.SELF_ADMIN_NAME_1, MAX_LENGTH_35);
		validateField(response, appSessionBean, false, translationMap, request.getCpName2(),
				SelfAdminUtil.SELF_ADMIN_NAME_2, MAX_LENGTH_35);
		validateField(response, appSessionBean, def.isCompanyTitleRequired(), translationMap, request.getCpTitle(),
				SelfAdminUtil.SELF_ADMIN_TITLE, MAX_LENGTH_50);
		validateField(response, appSessionBean, def.isCompanyAddressRequired(), translationMap,
				request.getCpAddressLine1(), SelfAdminUtil.SELF_ADMIN_ADDRESS_LINE_1, MAX_LENGTH_35);
		validateField(response, appSessionBean, false, translationMap, request.getCpAddressLine2(),
				SelfAdminUtil.SELF_ADMIN_ADDRESS_LINE_2, MAX_LENGTH_35);
		validateField(response, appSessionBean, false, translationMap, request.getCpAddressLine3(),
				SelfAdminUtil.SELF_ADMIN_ADDRESS_LINE_3, MAX_LENGTH_35);
		validateField(response, appSessionBean, def.isCompanyAddressRequired(), translationMap, request.getCpCity(),
				SelfAdminUtil.SELF_ADMIN_CITY, MAX_LENGTH_30);
		validateField(response, appSessionBean, def.isCompanyAddressRequired(), translationMap, request.getCpZipCd(),
				SelfAdminUtil.SELF_ADMIN_ZIP_CODE, MAX_LENGTH_12);
		validateCountryField(response, appSessionBean, def.isCompanyAddressRequired(), request.getCpCountryCd(),
				null != selectedCountry, translationMap, MAX_LENGTH_3);
		validateStateField(response, appSessionBean, def.isCompanyAddressRequired(),
				selectedCountry, request, MAX_LENGTH_4);
		validateField(response, appSessionBean, def.isCompanyFaxRequired(), translationMap, request.getCpFaxNumber(),
				SelfAdminUtil.SELF_ADMIN_FAX_NUMBER, MAX_LENGTH_24);
		validateField(response, appSessionBean, def.isCompanyPhoneNumberRequired(), translationMap,
				request.getCpPhoneNumber(), SelfAdminUtil.SELF_ADMIN_PHONE_NUMBER, MAX_LENGTH_24);
		validateField(response, appSessionBean, def.isCompanyURLRequired(), translationMap, request.getCpWebUrl(),
				SelfAdminUtil.SELF_ADMIN_WEB_URL, MAX_LENGTH_255);
		validateField(response, appSessionBean, def.isCompanyLogoRequired(), translationMap, request.getCpImageUrl(),
				SelfAdminUtil.SELF_ADMIN_IMAGE_URL, MAX_LENGTH_255);
		validateField(response, appSessionBean, def.isDepartmentRequired(), translationMap, request.getCpDepartment(),
				SelfAdminUtil.SELF_ADMIN_DEPARTMENT, MAX_LENGTH_30);
		validateField(response, appSessionBean, def.isDivisionRequired(), translationMap, request.getCpDivision(),
				SelfAdminUtil.SELF_ADMIN_DIVISION, MAX_LENGTH_30);
		// CAP-47775
		validateDefaultAddressField(response, appSessionBean, translationMap, request.getDefaultAddressSourceCd(),
				DEFAULT_ADDRESS_CD, def);
	}

	// CAP-47775
	protected void validateDefaultAddressField(SelfRegistrationSaveResponse response, AppSessionBean appSessionBean,
			Map<String, String> translationMap, String value, String fieldName, ProfileDefinition def) {
		int maxSize = MAX_LENGTH_1;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (value.length() > maxSize) {
			String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
			response.getFieldMessages().put(fieldName, message);
		} else if (!Util.isBlankOrNull(value) && !((def.isUseCorporateProfile() && DEFAULT_CORPORATE_CD.equals(value))
				|| (def.isExtendedProfileExists() && DEFAULT_EXTENDED_CD.equals(value)))) {
			String message = getInvalidErrorMessage(appSessionBean, label);
			response.getFieldMessages().put(fieldName, message);
		}
	}

	// CAP-47516
	protected void validateField(SelfRegistrationSaveResponse response, AppSessionBean appSessionBean,
			boolean isRequired, Map<String, String> translationMap, String value, String fieldNameKey, int maxSize) {
		String label = Util.nullToEmpty(translationMap.get(fieldNameKey));
		String fieldName = new StringBuilder(CP_PREFIX).append(StringUtils.capitalizeFirstLetter(fieldNameKey))
				.toString();
		if (isRequired && Util.isBlankOrNull(value)) {
			String message = getRequiredErrorMessage(appSessionBean, label);
			response.getFieldMessages().put(fieldName, message);
		} else if (Util.nullToEmpty(value).length() > maxSize) {
			String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
			response.getFieldMessages().put(fieldName, message);
		}
	}

	// CAP-47516
	protected void validateStateField(SelfRegistrationSaveResponse response, AppSessionBean appSessionBean,
			boolean isRequired, CountryBean selectedCountry, SelfRegistrationSaveRequest request, int maxSize) {
		String fieldName = new StringBuilder(CP_PREFIX)
				.append(StringUtils.capitalizeFirstLetter(SelfAdminUtil.SELF_ADMIN_STATE_CODE)).toString();
		if ((null != selectedCountry && selectedCountry.getCountryHasStates())) {
			if (isRequired && Util.isBlankOrNull(request.getCpStateCd())) {
				String message = getRequiredErrorMessage(appSessionBean, selectedCountry.getStateLabelText());
				response.getFieldMessages().put(fieldName, message);
			} else if (request.getCpStateCd().trim().length() > maxSize) {
				String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
				response.getFieldMessages().put(fieldName, message);
			} else if (!Util.isBlankOrNull(request.getCpStateCd())
					&& null == selectedCountry.getStateInCountry(request.getCpStateCd().toUpperCase())) {// CAP-48395
				String message = getInvalidErrorMessage(appSessionBean, selectedCountry.getStateLabelText());
				response.getFieldMessages().put(fieldName, message);
			}
		} else {
			if (request.getCpStateCd().trim().length() > maxSize) {
				request.setCpStateCd(AtWinXSConstant.EMPTY_STRING);
				String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
				response.getFieldMessages().put(fieldName, message);
			}
		}
	}

	// CAP-47516
	protected void validateCountryField(SelfRegistrationSaveResponse response, AppSessionBean appSessionBean,
			boolean isRequired, String country, boolean isCountryValid, Map<String, String> translationMap,
			int maxSize) {
		String fieldName = new StringBuilder(CP_PREFIX)
				.append(StringUtils.capitalizeFirstLetter(SelfAdminUtil.SELF_ADMIN_COUNTRY_CODE)).toString();
		String label = Util.nullToEmpty(translationMap.get(ModelConstants.COUNTRY_FIELD));
		if (Util.isBlankOrNull(country)) {
			if (isRequired) {
				String message = getRequiredErrorMessage(appSessionBean, label);
				response.getFieldMessages().put(fieldName, message);
			}
		} else if (country.length() > maxSize) {
			String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
			response.getFieldMessages().put(fieldName, message);
		} else if (!isCountryValid) {
			String message = getInvalidErrorMessage(appSessionBean, label);
			response.getFieldMessages().put(fieldName, message);
		}
	}

	protected SelfRegistration getSelfRegistration(AppSessionBean appSessionBean, String selectedPatternAfterUser)
			throws AtWinXSException {
		SelfRegistration selfRegistration = objectMapFactoryService.getEntityObjectMap()
				.getEntity(SelfRegistration.class, appSessionBean.getCustomToken());
		selfRegistration.init(appSessionBean.getLoginQueryString(), appSessionBean.getSiteID(),
				appSessionBean.getLoginID());
		selfRegistration.populatePatternAfterUsers();
		if (!Util.isBlankOrNull(selectedPatternAfterUser)) {
			selfRegistration.setSelectedPatternAfterUser(selectedPatternAfterUser);
			selfRegistration.init(appSessionBean.getLoginQueryString(), appSessionBean.getSiteID(),
					appSessionBean.getLoginID());
		}

		return selfRegistration;
	}
	
	// CAP-47410
	@Override
	public SelfRegistrationSaveResponse saveSelfRegistration(SessionContainer sc, SelfRegistrationSaveRequest request)
			throws AtWinXSException {

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();

		SelfRegistrationSaveResponse response = new SelfRegistrationSaveResponse();
		response.setSuccess(true);
		request.setCheckWarning(false);
		try {

			// validate pattern after user
			response = validatePatternAfter(sc, request.getSelectedPatternAfter());
			if (!response.isSuccess()) {

				return response;
			}
			
			// populate profile of selected pattern after User type
			SelfRegistration selfRegistration = getSelfRegistration(asb, request.getSelectedPatternAfter());
			ProfileDefinition profileDefinition = selfRegistration.getProfile().getProfileDefinition();
			//CAP-47915
			List<UserDefinedField> udfList = selfRegistration.getProfile().getUserDefinedFields().getUserDefinedFields();

			// Validate profile information for Self-Registration
			validateBasicProfile(request, response, asb, profileDefinition);
			
			//Validate Extended Profile for Self-Registration
			validateExtendedProfiles(request, response, asb,profileDefinition);
			
			//Validate corporate Profile
			validateCorporateProfile(request, asb, response, selfRegistration);
			
			//Validate Site Attributes
			validateSiteAttributes(asb, response, request.getC1UserSiteAttributes());
			
			if (response.getFieldMessages().size() > 0) {

				response.setSuccess(false);
				return response;
			}
			
			setBasicProfile(request, selfRegistration);
			
			if(profileDefinition.isExtendedProfileExists()) 
			{
				setExtendedProfile( request, selfRegistration);//Extended Profile
			}
			
			if(profileDefinition.isUseCorporateProfile() && profileDefinition.isCanUserEditCorporateProfile())
			{
				populateSelfRegistrationCorporateProfile(request, selfRegistration);
			}

			// CAP-47775
			setDefaultAddressSourceCode(request, selfRegistration, profileDefinition);
			
			if (selfRegistration.getProfile().hasSiteAttributes()){
				buildSiteAttributes(request, selfRegistration);
			}
			
			// validate and save UDFs to self registration
			UserDefinedfieldsResponse udfResponse = new UserDefinedfieldsResponse();
			
	        //CAP-47915
			if (selfAdminService.validateUserDefinedFields(udfList, request.getC1UserDefinedFields(),
	                udfResponse, sc.getApplicationSession().getAppSessionBean())) {
	        	setUserDefinedFields(request.getC1UserDefinedFields(), selfRegistration, asb);
	        }
			
			// save profile information for Self-Registration
			selfRegistration.createAll();
			
			//CAP-47671
			selfRegistration.copyAdminSettings(selfRegistration.getPatternAfterUserSelected());
			response.setLoginUrl(loginQueryString(asb,selfRegistration));

		} catch (AtWinXSException e) {

			logger.error(getErrorPrefix(asb), " Saving self registration basic profile failed with ", e.toString(), e);
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
					SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		} catch (NoClassDefFoundError ex) {

			logger.error(getErrorPrefix(asb), " Saving self registration basic profile failed with ", ex.toString(),
					ex);
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
					SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}

		return response;
	}
	
	
	// CAP-47410
	protected void validateBasicProfile(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
			AppSessionBean asb, ProfileDefinition def) throws AtWinXSException {

		Properties translationProps = translationService.getResourceBundle(asb,
				SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);

		validateUserID(request, response, asb);
		validateFirstName(request, response, asb, def, translationMap);
		validateLastName(request, response, asb, def, translationMap);
		validateEmail(request, response, asb, def, translationMap);
		validateBasicPhone(request, response, asb, def, translationMap);
	}

	// CAP-47410
	protected void setBasicProfile(SelfRegistrationSaveRequest request, SelfRegistration selfRegistration)
			throws AtWinXSException {

		// set Basic profile info for Self Registration
		selfRegistration.setLoginID(request.getUserID());
		selfRegistration.setPasswordCode(request.getPassword());
		selfRegistration.getProfile().setContactFirstName(request.getFirstName());
		selfRegistration.getProfile().setContactLastName(request.getLastName());
		selfRegistration.getProfile().setContactEmailAddress(request.getEmail());
		selfRegistration.getProfile().setContactPhoneNumber(request.getPhone());
	}

	// CAP-47410
	protected void validateUserID(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
			AppSessionBean asb) throws AtWinXSException {
		String value = request.getUserID();
		String fieldName = "userID";
		String label = Util.nullToEmpty(buildErrorMessage("userid_lbl", asb, null));

		if (Util.isBlankOrNull(value)) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));

		} else if (value.length() > ModelConstants.NUMERIC_16) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					ModelConstants.NUMERIC_16 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} 
		//CAP-47629
		else if (value.contains(RouteConstants.SINGLE_SPACE)) {
			
			response.getFieldMessages().put(fieldName, 
					buildErrorMessage(SFTranslationTextConstants.USERID_NO_SPACE, asb, null));
		} 
		else {

			User user = objectMapFactoryService.getEntityObjectMap().getEntity(User.class, null);
			user.setSiteLoginID(asb.getLoginID());
			user.populate(asb.getSiteID(), request.getUserID());
			if (user.isExisting()) {

				response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
						+ buildErrorMessage(SFTranslationTextConstants.DUPLICATE_ERR_MSG, asb, null));
			}
		}
	}

	// CAP-47410
	protected void validateFirstName(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
		String value = request.getFirstName();
		String fieldName = "firstName";
		String label = Util.nullToEmpty(translationMap.get(fieldName));

		if (def.isFirstNameRequired() && Util.isBlankOrNull(value)) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));

		} else if (value.length() > ModelConstants.NUMERIC_25) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					ModelConstants.NUMERIC_25 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	// CAP-47410
	protected void validateLastName(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getLastName();
		String fieldName = "lastName";
		String label = Util.nullToEmpty(translationMap.get(fieldName));

		if (def.isLastNameRequired() && Util.isBlankOrNull(value)) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));

		} else if (value.length() > ModelConstants.NUMERIC_25) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					ModelConstants.NUMERIC_25 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	// CAP-47410
	protected void validateEmail(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getEmail();
		String fieldName = "email";
		String label = Util.nullToEmpty(translationMap.get("emailAddress"));
		if (def.isEmailRequired() && Util.isBlankOrNull(value)) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_EMAIL) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_EMAIL + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} else if (!Util.isBlankOrNull(value) && !Util.isValidEmailFormat(value)) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		
		} 
		//CAP-47629
		else if(request.isCheckWarning()) {
			
			ProfileDAO dao = objectMapFactoryService.getDAOObjectMap().getObject(ProfileDAO.class, asb.getCustomToken());
			if(dao.checkEmailIfExisting(asb.getSiteID(), value)) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.EMAIL_ADDRESS_PARAM, value);
				response.getFieldMessages().put(fieldName, 
						buildErrorMessage(SFTranslationTextConstants.EXISTING_EMAIL_ERROR_MSG, asb, replaceMap));
				response.setWarningMessage(
						buildErrorMessage(SFTranslationTextConstants.EXISTING_EMAIL_ERROR_MSG, asb, replaceMap));
			}	
		}
	}

	// CAP-47410
	protected void validateBasicPhone(SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getPhone();
		String fieldName = "phone";
		String label = Util.nullToEmpty(translationMap.get(fieldName));

		if (def.isPhoneRequired() && Util.isBlankOrNull(value)) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_PHONE) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_PHONE + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	// CAP-47775
	public void setDefaultAddressSourceCode(SelfRegistrationSaveRequest request, SelfRegistration selfRegistration,
			ProfileDefinition profileDefinition) throws AtWinXSException {
		if (!Util.isBlankOrNull(request.getDefaultAddressSourceCd())
				&& ((profileDefinition.isExtendedProfileExists() && profileDefinition.isCanUserEditExtendedProfile())
				|| (profileDefinition.isUseCorporateProfile() && profileDefinition.isCanUserEditCorporateProfile()))) {
			selfRegistration.getProfile().setDefaultAddressSourceCd(request.getDefaultAddressSourceCd());
		}
	}
	
	// CAP-47323
	public void setExtendedProfile(SelfRegistrationSaveRequest request, SelfRegistration selfRegistration)
			throws AtWinXSException {

		// set Extended profile info for Self Registration
		SelfAdminUtil selfAdminUtil = new com.rrd.c1ux.api.util.SelfAdminUtil();
		Map<String, String> validStateAndCountry = selfAdminUtil.validateAndGetCountryStateName(request.getEpCountry(),
				request.getEpState());
		selfRegistration.getProfile().getExtendedProfile().setContactName2(request.getEpName2());
		selfRegistration.getProfile().getExtendedProfile().setLine1Address(request.getEpAddr1());
		selfRegistration.getProfile().getExtendedProfile().setLine2Address(request.getEpAddr2());
		selfRegistration.getProfile().getExtendedProfile().setLine3Address(request.getEpAddr3());
		selfRegistration.getProfile().getExtendedProfile().setCityName(request.getEpCity());
		selfRegistration.getProfile().getExtendedProfile().setStateCd(checkValue(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS), request.getEpState()));
		selfRegistration.getProfile().getExtendedProfile().setStateName(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_NAME_LBL));
		selfRegistration.getProfile().getExtendedProfile().setZipCd(request.getEpZip());
		selfRegistration.getProfile().getExtendedProfile().setCountryCd(checkValue(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS), request.getEpCountry()));
		selfRegistration.getProfile().getExtendedProfile().setCountryName(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL));
		selfRegistration.getProfile().getExtendedProfile().setContactMiddleInitialName(request.getEpMinitial());
		selfRegistration.getProfile().getExtendedProfile().setContactTitleName(request.getEpTitle());
		selfRegistration.getProfile().getExtendedProfile().setContactSuffixName(request.getEpSuffix());
		selfRegistration.getProfile().getExtendedProfile().setContactFaxNumber(request.getEpFaxnum());
		selfRegistration.getProfile().getExtendedProfile().setContactMobileNumber(request.getEpMobilenum());
		selfRegistration.getProfile().getExtendedProfile().setContactPagerNumber(request.getEpPagernum());
		selfRegistration.getProfile().getExtendedProfile().setContactTollFreePhoneNumber(request.getEpTollfreenum());
		selfRegistration.getProfile().getExtendedProfile().setContactWebAddress(request.getEpWeburl());
		selfRegistration.getProfile().getExtendedProfile().setContactPhotoURLAddress(request.getEpPhotourl());

	}
	
	private String checkValue(String flag, String value) {
		return flag.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_YES) ? value : AtWinXSConstant.EMPTY_STRING;
	}

		// CAP-47592
	public SelfRegistrationSaveResponse validatePasswordAndPatternAfterUser(SessionContainer sc, SelfRegistrationSaveRequest request)
			throws AtWinXSException {

		// validate pattern after user
		SelfRegistrationSaveResponse response = validatePatternAfter(sc, request.getSelectedPatternAfter());

		if (!response.isSuccess()) {
			return response;
		} 

		return validatePassword( sc,  request, response);

	}

	// CAP-47592
	public SelfRegistrationSaveResponse validatePassword(SessionContainer sc, SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response)
			throws AtWinXSException {

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		response.setSuccess(false);

		SelfRegistration selfRegistration = getSelfRegistration(asb, request.getSelectedPatternAfter());

		com.rrd.custompoint.admin.entity.PasswordSecuritySettings entityPSSettings = selfRegistration.getPasswordSecuritySettings();
		com.wallace.atwinxs.admin.util.PasswordSecuritySettings utilPSSettings = new PasswordSecuritySettings();

		try {
			BeanUtils.copyProperties(utilPSSettings, entityPSSettings);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			IPassword pwComponent = passwordComponentLocatorService.locate(asb.getCustomToken());
			
			if(!Util.maxLength(request.getPassword(), ModelConstants.NUMERIC_16)) {

				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						ModelConstants.NUMERIC_16 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(VALIDATE_PASSWORD, translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), PASSWORD_LBL) + AtWinXSConstant.BLANK_SPACE + buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
				response.setSuccess(false);
				return response;

			}

			boolean isValid = pwComponent.validatePassword( asb.getLoginID(), request.getPassword(),
					"",  utilPSSettings,true,false,"false",false);
			
			if(isValid && !(request.getPassword().equalsIgnoreCase(request.getValidatePassword())))
				isValid = false;

			if (isValid)
			{
				response.setSuccess(true);
				response.setMessage("");
			}
			else
			{
				logger.error("Confirm Password doesn't match New ");
				response.setSuccess(false);
				response.setFieldMessage(VALIDATE_PASSWORD, translationService.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), SFTranslationTextConstants.INVALID_PASSWORD_ERROR));
			}
		}
		catch(AtWinXSMsgException msg)
		{
			logger.error(msg.getMessage(), msg);
			response.setSuccess(false);
			response.setFieldMessage(VALIDATE_PASSWORD, translationService.processMessage(asb.getDefaultLocale(),
					asb.getCustomToken(), SFTranslationTextConstants.INVALID_PASSWORD_ERROR));

		}
		catch(AtWinXSException e)
		{
			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setFieldMessage(VALIDATE_PASSWORD, translationService.processMessage(asb.getDefaultLocale(),
					asb.getCustomToken(), SFTranslationTextConstants.INVALID_PASSWORD_ERROR));
		}

		return response;

	}

	// CAP-47629
	@Override
	public SelfRegistrationSaveResponse validateBasicProfile(SessionContainer sc,
			SelfRegistrationSaveRequest request) throws AtWinXSException {

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		SelfRegistrationSaveResponse response = new SelfRegistrationSaveResponse();
		response.setSuccess(false);
		
		try {
			// populate profile of selected pattern after User type
			//CAP-49057
			SelfRegistrationPatternAfterResponse res=getPatternAfterUsers(sc);
			List<LoginVOKey> patternAftUsers=res.getPatternAfterUsers();
			for (LoginVOKey loopThroughPatternAfterUsers : patternAftUsers) {//CAP-49057
				if (loopThroughPatternAfterUsers.getLoginID().equals(request.getSelectedPatternAfter())) {
					response.setSuccess(true);
					break;
				} 
			}//CAP-49057
			if (response.isSuccess()) {
			SelfRegistration selfRegistration = getSelfRegistration(asb, request.getSelectedPatternAfter());
			ProfileDefinition profileDefinition = selfRegistration.getProfile().getProfileDefinition();
			
			// validate pattern after user
			response = validatePatternAfter(sc, request.getSelectedPatternAfter());
			if (!response.isSuccess()) {

				return response;
			}

			// Validate profile information for Self-Registration
			validateBasicProfile(request, response, asb, profileDefinition);

			}else {//CAP-49057
				response.setSuccess(false);
				response.setMessage(translationService.processMessage(
						sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
						sc.getApplicationSession().getAppSessionBean().getCustomToken(),
						SFTranslationTextConstants.INVALID_PATTERN_AFTER));
			}//CAP-49057
			if (!response.getFieldMessages().isEmpty() || response.getWarningMessage().length() > 0) {

				response.setSuccess(false);
				return response;
			}
		} catch (AtWinXSException e) {

			logger.error(getErrorPrefix(asb), " Validate self registration basic profile failed with ", e.toString(), e);
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
					SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}
		return response;
	}
	
	//CAP-47616 CAP-47672
	public SelfRegistrationSaveResponse validateUserDefinedFields(SessionContainer sc, SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response)
			throws AtWinXSException {

		// validate pattern after user
		SelfRegistrationSaveResponse response1 = validatePatternAfter(sc, request.getSelectedPatternAfter());

		if (!response1.isSuccess()) {
			return response1;
		} 

		// populate profile of selected pattern after User type
		SelfRegistration selfRegistration = getSelfRegistration(sc.getApplicationSession().getAppSessionBean(), request.getSelectedPatternAfter());

		if(selfRegistration.getProfile().getUserDefinedFields()!=null) {
			
			//CAP-47915
			List<UserDefinedField> udfList = selfRegistration.getProfile().getUserDefinedFields().getUserDefinedFields();
			
		   UserDefinedfieldsResponse udfResponse = new UserDefinedfieldsResponse();
		
		   boolean checkValidUDF = selfAdminService.validateUserDefinedFields( udfList, request.getC1UserDefinedFields(),
                udfResponse, sc.getApplicationSession().getAppSessionBean());

			if(!checkValidUDF) {
				Map<String, String> fieldMessages = udfResponse.getFieldMessages();
				for(Entry<String, String> entry :  fieldMessages.entrySet()) {
					response.setFieldMessage(entry.getKey(), entry.getValue());
				}
				response.setSuccess(false);
			}
		}
		return response;
		}
	
	// CAP-47657- Start here
	@Override
	public SelfRegistrationSaveResponse validateAttributes(SessionContainer sc,
					SelfRegistrationSaveRequest selfRegistrationSaveRequest) throws AtWinXSException {

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		SelfRegistrationSaveResponse response = new SelfRegistrationSaveResponse();
		response.setSuccess(true);

		try {
			// validate pattern after user
			response = validatePatternAfter(sc, selfRegistrationSaveRequest.getSelectedPatternAfter());
			if (!response.isSuccess()) {

				response.setSuccess(response.getFieldMessages().isEmpty());
			}

			
				List<C1UserSiteAttribute> c1UserSiteAttributesLst = selfRegistrationSaveRequest
							.getC1UserSiteAttributes();

			validateSiteAttributes(asb, response, c1UserSiteAttributesLst);

	} catch (AtWinXSException e) {

			logger.error(getErrorPrefix(asb), " Validate self registration basic profile failed with ",
							e.toString(), e);
			response.setSuccess(false);
			response.setFieldMessage(translationService.processMessage(asb.getDefaultLocale(),asb.getCustomToken(), SFTranslationTextConstants.ATTR_NAME), translationService.processMessage(asb.getDefaultLocale(),
							asb.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}

		return response;
	}
	// CAP-47657- End Here
	
	// CAP-47670
	private void setUserDefinedFields(ArrayList<C1UserDefinedField> c1UserDefinedFields, SelfRegistration selfRegistration, AppSessionBean asb) throws AtWinXSException {
		List<UserDefinedField> userDefinedFields = selfRegistration.getProfile().getUserDefinedFields().getUserDefinedFields();
		
		//CAP-47842
		if(null != c1UserDefinedFields) {
			
		for (C1UserDefinedField c1UDF : c1UserDefinedFields) {
		
			UserDefinedField userDef = objectMapFactoryService.getEntityObjectMap().getEntity(UserDefinedField.class, asb.getCustomToken());
			userDef.populate(asb.getSiteID(), asb.getBuID(), asb.getProfileNumber(), c1UDF.getUdfFieldNumber());
			
			userDefinedFields.stream().filter(udf -> udf.getUdfFieldNumber() == c1UDF.getUdfFieldNumber()).findFirst()
					.ifPresent(cpUDF -> {
						cpUDF.setSiteID(asb.getSiteID());
						cpUDF.setBusinessUnitID(asb.getBuID());
						cpUDF.setProfileNumber(asb.getProfileNumber());
						cpUDF.setCreateProgramID(this.getClass().getName());
						cpUDF.setCreateUserID(selfRegistration.getCreateUserID());
						cpUDF.setCreateTimestamp(new Date());
						cpUDF.setUdfFieldNumber(c1UDF.getUdfFieldNumber());
						
						try {
							if (userDef.getProfileUDFDefinition().isCanUserEdit()) {
								cpUDF.setUdfValueText(c1UDF.getUdfValueText());
							}
						} catch (AtWinXSException e) {
							logger.error(e.getMessage(), e);
						}
					});
		}
		}
		
		selfRegistration.getProfile().getUserDefinedFields().setUserDefinedFields(userDefinedFields);
	}
// CAP-47657- End Here

	private void validateSiteAttributes(AppSessionBean asb, SelfRegistrationSaveResponse response,
			List<C1UserSiteAttribute> c1UserSiteAttributesLst) throws AtWinXSException {
		if ((null != c1UserSiteAttributesLst && !c1UserSiteAttributesLst.isEmpty())) {

			Profile profile = new ProfileImpl();
			profile.setSiteID(asb.getSiteID());
			profile.setBusinessUnitID(asb.getBuID());
			profile.setProfileNumber(asb.getProfileNumber());
			SiteAttributes siteAttributesForProfileLst = profile.getSiteAttributes();

			String errorMessage = translationService.processMessage(asb.getDefaultLocale(),
					asb.getCustomToken(), SFTranslationTextConstants.UNABLE_TO_SAVE_SITE_ATTR);

			List<C1UserSiteAttribute> editableSiteAttributesLst = new ArrayList<>();

			if (!selfAdminService.validateSiteAttrIDAndAttrValueID(siteAttributesForProfileLst,
								c1UserSiteAttributesLst, editableSiteAttributesLst)) {

				response.setSuccess(false);
				response.setFieldMessage(translationService.processMessage(asb.getDefaultLocale(),asb.getCustomToken(), SFTranslationTextConstants.ATTR_NAME), errorMessage);
			}
			
			else {

				Collection<String> errMsgs = selfAdminService.validateSiteAttributeMinMax(asb,
						siteAttributesForProfileLst, editableSiteAttributesLst);

				if (!errMsgs.isEmpty()) {
					response.setSuccess(false);
					response.setFieldMessage(translationService.processMessage(asb.getDefaultLocale(),asb.getCustomToken(), SFTranslationTextConstants.ATTR_NAME), 
							errMsgs.stream().collect(Collectors.joining(" ")));
				}
			}
		}
	}

	//CAP-47671
	public String getContextPath() {
		return Util.getContextPath(ContextPath.Classic);
	}
	
	//CAP-47671
	public String createTimeBomb() throws AtWinXSException {
		// Get a properties so we can get timeout value for auto login strings
		XSProperties systemProperties = PropertyUtil.getProperties("system");
		int timeout = Util.safeStringToDefaultInt(systemProperties.getProperty(LoginConstants.PROP_AUTO_LOGIN_TIMEBOMB),60);
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Using today's date
		c.add(Calendar.SECOND, timeout); // Adding 60 seconds
		// Return string
		return Long.toString(c.getTimeInMillis());
	}
	
	//CAP-47671
	public String loginQueryString(AppSessionBean asb, SelfRegistration selfRegistration) throws AtWinXSException {
		XSProperties orderEntryProperties = PropertyUtil
				.getProperties(SystemFunctionsConstants.PROP_GLOBAL_BUNDLE_NAME);
		String serverName = orderEntryProperties.getProperty("SERVERNAME");
		String baseURL = AtWinXSConstant.HTTPS_PREFIX + serverName + getContextPath();
		Properties loginQueryParams = asb.getLoginQueryString();
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.zzz");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// get the current date here
		Date date = new Date();
		// add the formated date to our tstamp variable here
		if (selfRegistration.isFromAutologin()) {
			loginQueryParams.setProperty("ORIG-tbomb", createTimeBomb());
			loginQueryParams.setProperty("ORIG-tstamp", dateFormat.format(date) + "T" + timeFormat.format(date));
		} else {
			loginQueryParams.setProperty("tbomb", createTimeBomb());
			loginQueryParams.setProperty(PingFederationConstants.SAML_TIMESTAMP,
					dateFormat.format(date) + "T" + timeFormat.format(date));
		}
		return selfRegistration.buildAutoLoginURL(baseURL, loginQueryParams);
	}
	
}
