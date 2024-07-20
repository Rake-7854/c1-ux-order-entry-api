/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
  *	03/29/23	A Boomker			CAP-39510		Initial Version
 *  03/31/23    M Sakthi            CAP-39581 		API Build - Get Password Requirements
 *	04/03/23	S Ramachandran		CAP-39610		Get Profile Definition of User Type in Self Admin
 * 	04/10/23	A Boomker			CAP-37890		Adding handling for update basic profile API
 *  04/13/23	A Boomker			CAP-39904 		Add handling in updateBasicProfile()
 *  04/24/23	A Boomker			CAP-40002		Moved getErrorPrefix() to BaseService
 *  04/26/23	A Boomker			CAP-39904		Refactored validateBasicProfile() 403 exception wasn't caught by the 422 try/catch
 *  06/20/23	M Sakthi			CAP-40705		API Change-Updating basic profile information must update the AppSessionBean in session
 *  07/13/23	S Ramachandran		CAP-42258		Added service handling for update extended Personal Profile
 *  07/18/23	N Caceres			CAP-42342		Add handling for Update Company Profile API
 *  08/09/23    M Sakthi			CAP-42562       C1UX BE - Self Admin – Update Profile User Defined Fields (API Build)
 *  28/29/23	M Sakthi			CAP-42562		Modified the code to check the UDF text is not null (comment added here by Krishna. N)
 *	09/07/23	L De Leon			CAP-43631		Added deletePABAddress() method
 *  09/07/23	S Ramachandran		CAP-43630		Added service to return PAB entries - all or a search
 *  09/28/23	N Caceres			CAP-42806		Fix inconsistent translation in update extended Personal Profile
 *	10/06/23	L De Leon			CAP-44422		Added exportPABAddresses() method
 *  10/06/23	M Sakthi			CAP-44387 		Added service to ImportPAB address
 *  11/07/23	S Ramachandran		CAP-44961 		USPS validation in save PAB and to show suggested address 
 *  11/29/23	Satishkumar A		CAP-45375		C1UX BE - Modify the errors returned from the USPS validation to be translated 
 *  11/27/23	S Ramachandran		CAP-45374 		USPS validated Suggested Address need to be split of address over 30 characters
 *  12/07/23    S Ramachandran  	CAP-45485   	Fix code to only search/use originator profile when doing self administration 
 *	01/10/24	Krishna Natarajan	CAP-46333		validateAccessToPAB conditions changed
 *	01/11/24	Krishna Natarajan	CAP-46389 		changed appSessionBean.isSharedID() to appSessionBean.getOriginatorProfile().isSharedID() in getPasswordRule method
 *	02/01/24	S Ramachandran		CAP-46801		Added save site attribute service for a user in Self Admin
 *  03/26/24	Krishna Natarajan	CAP-48207		Added conditional check in validateUDFAssignedAttributeValue
 *  06/28/24	Rakesh KM			CAP-50381		UDF Fields with an Assigned Attribute List Should Ignore the Field Length validation
 */

package com.rrd.c1ux.api.services.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.C1UXAddress;
import com.rrd.c1ux.api.models.admin.C1UXUserDefinedFieldImpl;
import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributeValue;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesRequest;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesResponse;
import com.rrd.c1ux.api.models.admin.CORetrievePasswordRuleResponse;
import com.rrd.c1ux.api.models.admin.PABDeleteRequest;
import com.rrd.c1ux.api.models.admin.PABDeleteResponse;
import com.rrd.c1ux.api.models.admin.PABExportResponse;
import com.rrd.c1ux.api.models.admin.PABImportRequest;
import com.rrd.c1ux.api.models.admin.PABImportResponse;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.admin.PABSearchRequest;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.models.admin.ProfileDefinitionResponse;
import com.rrd.c1ux.api.models.admin.UpdateBasicProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateBasicProfileResponse;
import com.rrd.c1ux.api.models.admin.UpdateCompanyProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateCompanyProfileResponse;
import com.rrd.c1ux.api.models.admin.UpdateExtendedProfileRequest;
import com.rrd.c1ux.api.models.admin.UpdateExtendedProfileResponse;
import com.rrd.c1ux.api.models.admin.UpdatePasswordRequest;
import com.rrd.c1ux.api.models.admin.UpdatePasswordResponse;
import com.rrd.c1ux.api.models.admin.UserDefinedFieldsRequest;
import com.rrd.c1ux.api.models.admin.UserDefinedfieldsResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.admin.locators.CountryLocatorService;
import com.rrd.c1ux.api.services.admin.locators.ProfileAttributeComponentLocatorService;
import com.rrd.c1ux.api.services.admin.locators.ProfileComponentLocatorService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.services.users.UserFullProfileProcessorImpl;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.admin.entity.SiteAttributeValue;
import com.rrd.custompoint.admin.entity.SiteAttributeValues;
import com.rrd.custompoint.admin.profile.dao.UserDefinedFieldDAO;
import com.rrd.custompoint.admin.profile.entity.AddressBook;
import com.rrd.custompoint.admin.profile.entity.AddressBookImpExp;
import com.rrd.custompoint.admin.profile.entity.AddressBookRecord;
import com.rrd.custompoint.admin.profile.entity.CorporateProfile;
import com.rrd.custompoint.admin.profile.entity.ExtendedProfile;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;
import com.rrd.custompoint.admin.profile.entity.ProfileImpl;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.User;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.rrd.custompoint.admin.profile.entity.UserDefinedFieldImpl;
import com.rrd.custompoint.admin.profile.util.AddressBookImportResult;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.orderentry.entity.AddressSourceSettings;
import com.wallace.atwinxs.admin.locator.PasswordComponentLocator;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.admin.vo.BUPrflAttrVO;
import com.wallace.atwinxs.admin.vo.PersonalAddressVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.admin.vo.ProfileVOKey;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IPassword;
import com.wallace.atwinxs.interfaces.IProfileAttribute;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.vo.USPSAddressInfoServiceParmsVO;

@Service

public class SelfAdminServiceImpl extends BaseOEService implements SelfAdminService {
	private static final Logger logger = LoggerFactory.getLogger(SelfAdminServiceImpl.class);
	private final String className =  this.getClass().getName();

	protected final CountryLocatorService countryLocatorService;
	protected final UserFullProfileProcessorImpl userFullProfileProcessorImpl;
	protected final ProfileComponentLocatorService profileComponentLocatorService;
	protected final ProfileAttributeComponentLocatorService profileAttributeComponentLocatorService;
	
	 
	
	public SelfAdminServiceImpl(TranslationService translationService,ObjectMapFactoryService objectMapFactoryService,
			CountryLocatorService countryLocatorService,UserFullProfileProcessorImpl userFullProfileProcessorImpl,
			ProfileComponentLocatorService profileComponentLocatorService, 
			ProfileAttributeComponentLocatorService profileAttributeComponentLocatorService) {
	      super(translationService,objectMapFactoryService);
	      this.countryLocatorService = countryLocatorService;
	      this.userFullProfileProcessorImpl = userFullProfileProcessorImpl;
	      this.profileComponentLocatorService = profileComponentLocatorService; 
	      this.profileAttributeComponentLocatorService = profileAttributeComponentLocatorService;
	    }

	SelfAdminUtil selfAdminUtil = new com.rrd.c1ux.api.util.SelfAdminUtil();
	
	public UpdatePasswordResponse updatePassword(UpdatePasswordRequest request, SessionContainer sc) throws AtWinXSException
	{
		UpdatePasswordResponse response = new UpdatePasswordResponse();

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		//CAP-45485 - Force to use originator profile in Self Admin for OOB Mode 
		boolean useOriginatorProfile = true;
		boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile,asb);
		
		if ((asb.isSharedID()) || (!asb.canChangePassword()))
		{
			logger.error(getErrorPrefix(asb), " is attempting to change their password but does not have permissions.");
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {
		  IPassword pwComponent = PasswordComponentLocator.locate(asb.getCustomToken());
		  if (pwComponent.validateAndSavePasswordC1UX(asb.getSiteID(), asb.getLoginID(), request.getOldPassword(),
				  request.getNewPassword(), request.getConfirmPassword(), asb.getPasswordSecuritySettings(), asb))
		  {
			  response.setSuccess(true);
			  response.setMessage(TranslationTextTag.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_PASSWORD_CHANGED_MSG));
		  }
		  else
		  {
			  logger.error("No error thrown, but validation returned false.");
			  response.setSuccess(false);
			  response.setMessage(TranslationTextTag.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		  }
		}
		catch(AtWinXSMsgException msg)
		{
			logger.error(msg.getMessage(), msg);
			response.setSuccess(false);
			response.setMessage(msg.getMsg().getErrGeneralMsg());
		}
		catch(AtWinXSException e)
		{
			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		//CAP-45485 - Revert originator to Requestor profile after Self Admin process in OOB Mode 
		SelfAdminUtil.revertOriginatorProfileInSelfAdmin(asb, requestorModeModified);
		return response;
	}

	@Override
	public CORetrievePasswordRuleResponse getPasswordRule(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		CORetrievePasswordRuleResponse response=new CORetrievePasswordRuleResponse();

		if ((appSessionBean.getOriginatorProfile().isSharedID()) || (!appSessionBean.canChangePassword()))//CAP-46389
		{
			logger.error(getErrorPrefix(appSessionBean) , " is attempting to change their password but does not have permissions.");
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try	{
			response.setMinimumPasswordChars(appSessionBean.getPasswordSecuritySettings().getMinimumPasswordChars());
			response.setMinimumPasswordNumericChars(appSessionBean.getPasswordSecuritySettings().getMinimumPasswordNumericChars());
			response.setMinimumUpperCaseChars(appSessionBean.getPasswordSecuritySettings().getMinimumUpperCaseChars());
			response.setMinimumSpecialChars(appSessionBean.getPasswordSecuritySettings().getMinimumSpecialChars());
			response.setSuccess(true);
		} catch(Exception e)	{
			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		
		//CAP-40607
		Properties props = translationService.getResourceBundle(appSessionBean, "changePassword");
		response.setTranslationChangePassword(translationService.convertResourceBundlePropsToMap(props));
		
		return response;
	}


	//CAP-39610 - Self Admin - Get Profile Definition of User Type
	@Override
	public ProfileDefinitionResponse getProfileDefinition(SessionContainer sc) throws AtWinXSException

	{

		ProfileDefinitionResponse response = new ProfileDefinitionResponse();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();


		// throw 403 error if profile not found for named or shared user
		if (asb.getProfileNumber() <= AtWinXSConstant.INVALID_ID)
		{
			logger.error(getErrorPrefix(asb), " is not allowed to access this service. getProfileDefinition()");
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {

			ProfileDefinition profileDefinition = ObjectMapFactory.getEntityObjectMap().getEntity(
					ProfileDefinition.class, asb.getCustomToken());

			//method to populate profile definition of User type
			profileDefinition.populate(asb.getSiteID(),asb.getBuID(),asb.getProfileDefId());

			//method to populate ProfileUDFDefinitions through getter
			profileDefinition.getCustFieldsProfielUDFDefinitions();

			response.setProfileDefinition(profileDefinition);
			response.setSuccess(true);

		}
		catch(AtWinXSException errorMessage)
		{
			logger.error(errorMessage.getMessage(), errorMessage);
			response.setSuccess(false);
			response.setMessage(errorMessage.getMessage());
		}

		return response;
	}

	// CAP-37890 - Adding this method for creating API response
	public UpdateBasicProfileResponse updateBasicProfile(UpdateBasicProfileRequest request, SessionContainer sc) throws AtWinXSException
	{
		UpdateBasicProfileResponse response = new UpdateBasicProfileResponse();

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		//CAP-45485 - Force to use originator profile in Self Admin for OOB Mode 
		boolean useOriginatorProfile = true;
		boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile,asb);
		
		if ((asb.getProfileNumber() <= 0))
		{
			logger.error(getErrorPrefix(asb), SelfAdminUtil.SELF_ADMIN_NO_PROFILE_SELECTED_ERROR);
			throw new AccessForbiddenException(this.getClass().getName());
		}
		// CAP-39904 - refactored so 403 exception wasn't caught by the 422 try/catch
		ProfileDefinition def = objectMapFactoryService.getEntityObjectMap().getEntity(ProfileDefinition.class, asb.getCustomToken());
		try {
			//method to populate profile definition of User type
			def.populate(asb.getSiteID(),asb.getBuID(),asb.getProfileDefId());

			if ((!def.isCanUserEditProfile()))
			{
				logger.error(getErrorPrefix(asb), SelfAdminUtil.SELF_ADMIN_NO_PERMISSION_ERROR);
				throw new AccessForbiddenException(this.getClass().getName());
			}
		}
		catch(AtWinXSException e)
		{ 
			logger.error(getErrorPrefix(asb), SelfAdminUtil.SELF_ADMIN_RETRIEVE_PROFILE_ERROR);
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {
			
			if (validateBasicProfile(response, request, asb, def))
			{
				
				Profile prfl = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class, asb.getCustomToken());
				prfl.populate(asb.getProfileNumber());
				prfl.setContactFirstName(request.getFirstName());
				prfl.setContactLastName(request.getLastName());
				prfl.setContactEmailAddress(request.getEmail());
				prfl.setContactPhoneNumber(request.getPhone());
				prfl.updateAll(false, false, false, false);
				
				//CAP-40705
				asb.setFirstName(request.getFirstName());
				asb.setLastName(request.getLastName());
				asb.setPhoneNumber(request.getPhone());
				asb.setEmailAddress(request.getEmail());
				
				//CAP-45485 - Revert originator to Requestor profile after Self Admin process in OOB Mode 
				SelfAdminUtil.revertOriginatorProfileInSelfAdmin(asb, requestorModeModified);
				
				SessionHandler.saveSession(sc.getApplicationSession(), asb.getSessionID(), AtWinXSConstant.APPSESSIONSERVICEID);
			}
		}
		catch(AtWinXSException e)
		{
			logger.error(getErrorPrefix(asb), " updating basic profile failed with ", e.toString(), e);
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}
		return response;
	}

	// CAP-39904 - add validation here
	public boolean validateBasicProfile(UpdateBasicProfileResponse response, UpdateBasicProfileRequest request, AppSessionBean asb, ProfileDefinition def) throws AtWinXSException
	{
		response.setSuccess(false);

		Properties translationProps = translationService.getResourceBundle(asb, SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);

		validateFirstName(request, response, asb, translationMap);
		validateLastName(request, response, asb, translationMap);
		validateEmail(request, response, asb, def, translationMap);
		validateBasicPhone(request, response, asb, def, translationMap);

		if (response.getFieldMessages().size() > 0) {
			return false;
		}

		response.setSuccess(true);
		return true;
	}

	protected void validateFirstName(UpdateBasicProfileRequest request, UpdateBasicProfileResponse response,
			AppSessionBean asb, Map<String, String> translationMap) throws AtWinXSException {
		String value = request.getFirstName();
		String fieldName = "firstName";
		String label = Util.nullToEmpty(translationMap.get(fieldName));

		if (value == null || Util.isBlank(value.trim())) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE +
					buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > ModelConstants.SELF_ADMIN_MAX_SIZE_FIRST_NAME) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.SELF_ADMIN_MAX_SIZE_FIRST_NAME + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateLastName(UpdateBasicProfileRequest request, UpdateBasicProfileResponse response,
			AppSessionBean asb, Map<String, String> translationMap) throws AtWinXSException {
		String value = request.getLastName();
		String fieldName = "lastName";
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (value == null || Util.isBlank(value.trim())) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE +
					buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > ModelConstants.SELF_ADMIN_MAX_SIZE_LAST_NAME) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.SELF_ADMIN_MAX_SIZE_LAST_NAME + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateEmail(UpdateBasicProfileRequest request, UpdateBasicProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
		String value = request.getEmail();
		String fieldName = "email";
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isEmailRequired()
				&& (value == null || Util.isBlank(value.trim()))) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE +
					buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_EMAIL) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_EMAIL + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
		// DTS 6502 Validate email address format
		else if (!Util.isBlankOrNull(value) && !Util.isValidEmailFormat(value)) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE +
					buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
	}

	protected void validateBasicPhone(UpdateBasicProfileRequest request, UpdateBasicProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {
		String value = request.getPhone();
		String fieldName = "phone";
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isPhoneRequired()
				&& (value == null || Util.isBlank(value.trim()))) {
			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE +
					buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_PHONE) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.SELF_ADMIN_MAX_SIZE_BASIC_PHONE + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName, buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}
	
	// CAP-42258 - method to validate and update extended profile
	@Override
	public UpdateExtendedProfileResponse updateExtendedProfile(UpdateExtendedProfileRequest request,
			SessionContainer sc) throws AtWinXSException {
		UpdateExtendedProfileResponse response = new UpdateExtendedProfileResponse();

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		//CAP-45485 - Force to use originator profile in Self Admin for OOB Mode 
		boolean useOriginatorProfile = true;
		boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile,asb);

		if ((asb.getProfileNumber() <= 0)) {
			logger.error(getErrorPrefix(asb), SelfAdminUtil.SELF_ADMIN_NO_PROFILE_SELECTED_ERROR);
			throw new AccessForbiddenException(this.getClass().getName());
		}

		ProfileDefinition def = objectMapFactoryService.getEntityObjectMap().getEntity(ProfileDefinition.class,
				asb.getCustomToken());

		try {
			// method to populate profile definition of User type
			def.populate(asb.getSiteID(), asb.getBuID(), asb.getProfileDefId());

			if ((!def.isExtendedProfileExists() || !def.isCanUserEditExtendedProfile())) {
				logger.error(getErrorPrefix(asb),
						SelfAdminUtil.SELF_ADMIN_NO_PERMISSION_ERROR);
				throw new AccessForbiddenException(this.getClass().getName());
			}
		} catch (AtWinXSException e) {
			logger.error(getErrorPrefix(asb), SelfAdminUtil.SELF_ADMIN_RETRIEVE_PROFILE_ERROR);
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {

			Map<String, String> validStateAndCountry = selfAdminUtil.validateAndGetCountryStateName(request.getCountryCd(),
					request.getStateCd());

			String countryExist = validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS);
			String countryName = validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL);

			String stateExist = validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS);
			String stateName = validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_NAME_LBL);

			if (validateExtendedProfile(response, request, asb, def, countryExist, stateExist)) {
				Profile prfl = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class,
						asb.getCustomToken());
				prfl.populate(asb.getProfileNumber());
				ExtendedProfile ext = prfl.getExtendedProfile();

				if (request.isUseAsdefaultPreferredShipping()) {

					prfl.setDefaultAddressSourceCd(EXTENDED_AS_PREFFERED_ADDR_CODE);
				} else if (prfl.getDefaultAddressSourceCd().equals(EXTENDED_AS_PREFFERED_ADDR_CODE)) {

					prfl.setDefaultAddressSourceCd(AtWinXSConstant.EMPTY_STRING);
				}

				ext.setContactName2(request.getName2());
				ext.setContactMiddleInitialName(request.getMiddleInitial());
				ext.setContactTitleName(request.getTitle());
				ext.setContactSuffixName(request.getSuffix());
				ext.setContactFaxNumber(request.getFaxNumber());
				ext.setContactMobileNumber(request.getPhoneNumber());
				ext.setContactWebAddress(request.getWebUrl());
				ext.setContactPhotoURLAddress(request.getImageUrl());
				ext.setContactPagerNumber(request.getPagerNumber());
				ext.setContactTollFreePhoneNumber(request.getTollFreeNumber());
				ext.setLine1Address(request.getAddressLine1());
				ext.setLine2Address(request.getAddressLine2());
				ext.setLine3Address(request.getAddressLine3());
				ext.setCityName(request.getCity());
				ext.setZipCd(request.getZip());
				ext.setCountryCd(countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_YES) ? request.getCountryCd() : AtWinXSConstant.EMPTY_STRING);
				ext.setCountryName(countryName);
				ext.setStateCd(stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_YES) ? request.getStateCd() : AtWinXSConstant.EMPTY_STRING);
				ext.setStateName(stateName);

				prfl.updateAll(false, false, false, true);

				// CAP-42258 - update app session with new extended profile values
				asb.getPersonalProfile().setContactName2(ext.getContactName2());
				asb.getPersonalProfile().setInitial(ext.getContactMiddleInitialName());
				asb.getPersonalProfile().setTitle(ext.getContactTitleName());
				asb.getPersonalProfile().setSuffix(ext.getContactSuffixName());
				asb.getPersonalProfile().setFax(ext.getContactFaxNumber());
				asb.getPersonalProfile().setMobile(ext.getContactMobileNumber());
				asb.getPersonalProfile().setWebURL(ext.getContactWebAddress());
				asb.getPersonalProfile().setPager(ext.getContactPagerNumber());
				asb.getPersonalProfile().setTollFree(ext.getContactTollFreePhoneNumber());
				asb.getPersonalProfile().setAddress1(ext.getLine1Address());
				asb.getPersonalProfile().setAddress2(ext.getLine2Address());
				asb.getPersonalProfile().setAddress3(ext.getLine2Address());
				asb.getPersonalProfile().setCity(ext.getCityName());
				asb.getPersonalProfile().setZip(ext.getZipCd());
				asb.getPersonalProfile().setCountryCode(ext.getCountryCd());
				asb.getPersonalProfile().setCountryName(ext.getCountryName());
				ext.setStateCd(request.getStateCd());
				ext.setStateName(stateName);

				//CAP-45485 - Revert originator to Requestor profile after Self Admin process in OOB Mode 
				SelfAdminUtil.revertOriginatorProfileInSelfAdmin(asb, requestorModeModified);

				SessionHandler.saveSession(sc.getApplicationSession(), asb.getSessionID(),
						AtWinXSConstant.APPSESSIONSERVICEID);
			}

		} catch (AtWinXSException e) {
			logger.error(getErrorPrefix(asb), " updating Extended profile failed with ", e.toString(), e);
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
					SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}
		return response;

	}

	// CAP-42258 - validate extended profile fields and set translation error
	// message
	@Override
	public boolean validateExtendedProfile(UpdateExtendedProfileResponse response, UpdateExtendedProfileRequest request,
			AppSessionBean asb, ProfileDefinition def, String countryExist, String stateExist) throws AtWinXSException {

		response.setSuccess(false);

		Properties translationProps = translationService.getResourceBundle(asb,
				SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);

		validateName2(request, response, asb);
		validateMiddleInitial(request, response, asb, def, translationMap);
		validateTitle(request, response, asb, def, translationMap);
		validateSuffix(request, response, asb, def, translationMap);
		validateFaxNumber(request, response, asb, def, translationMap);
		validatePhoneNumber(request, response, asb, def, translationMap);
		validateWebUrl(request, response, asb, def, translationMap);
		validateImageUrl(request, response, asb, def, translationMap);
		validatePagerNumber(request, response, asb, def, translationMap);
		validateTollFreeNumber(request, response, asb, def, translationMap);
		validateAddressLine1(request, response, asb, def);
		validateAddressLine2(request, response, asb);
		validateAddressLine3(request, response, asb);
		validateCity(request, response, asb, def, translationMap);
		validateZip(request, response, asb, def, translationMap);
		validateCountry(request, countryExist, def, response, asb, translationMap);
		validateState(request, countryExist, stateExist, def, response, asb, translationMap);
		if (response.getFieldMessages().size() > 0) {
			return false;
		}

		response.setSuccess(true);
		return true;

	}

	protected void validateName2(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb) throws AtWinXSException {

		String value = request.getName2();
		String fieldName = SelfAdminUtil.SELF_ADMIN_NAME_2;
		if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_NAME2) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_NAME2 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateMiddleInitial(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getMiddleInitial();
		String fieldName = SelfAdminUtil.SELF_ADMIN_MIDDLE_INITIAL_FLD_NM;
		String label = Util.nullToEmpty(translationMap.get(SelfAdminUtil.SELF_ADMIN_MIDDLE_INITIAL_LBL));
		if (def.isMiddleInitialRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_MID_INIT) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_MID_INIT + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateTitle(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getTitle();
		String fieldName = SelfAdminUtil.SELF_ADMIN_TITLE;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isTitleRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_TITLE) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_TITLE + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateSuffix(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getSuffix();
		String fieldName = SelfAdminUtil.SELF_ADMIN_SUFFIX_FLD_NM;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isSuffixRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_SUFFIX) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_SUFFIX + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateFaxNumber(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getFaxNumber();
		String fieldName = SelfAdminUtil.SELF_ADMIN_FAX_NUMBER;
		String label = Util.nullToEmpty(translationMap.get(SelfAdminUtil.SELF_ADMIN_FAX_LBL));
		if (def.isFaxRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_FAX) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_FAX + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validatePhoneNumber(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getPhoneNumber();
		String fieldName = SelfAdminUtil.SELF_ADMIN_PHONE_NUMBER;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isMobileRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_MOBILE) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_MOBILE + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateWebUrl(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getWebUrl();
		String fieldName = SelfAdminUtil.SELF_ADMIN_WEB_URL;
		String label = Util.nullToEmpty(translationMap.get(SelfAdminUtil.SELF_ADMIN_WEB_URL_LBL));
		if (def.isWebRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_WEB_URL) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_WEB_URL + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateImageUrl(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getImageUrl();
		String fieldName = SelfAdminUtil.SELF_ADMIN_IMAGE_URL;
		String label = Util.nullToEmpty(translationMap.get(SelfAdminUtil.SELF_ADMIN_PHOTO_URL_LBL));
		if (def.isPhotoURLRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_IMG_URL) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_IMG_URL + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validatePagerNumber(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.pagerNumber;
		String fieldName = SelfAdminUtil.SELF_ADMIN_PAGER_NUMBER_FLD_NM;
		String label = getTranslation(asb, SFTranslationTextConstants.PAGER_NUMBER_LBL, SFTranslationTextConstants.PAGER_NUMBER_DFLT_VAL);
		if (def.isPagerRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_PAGER) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_PAGER + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateTollFreeNumber(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getTollFreeNumber();
		String fieldName = SelfAdminUtil.SELF_ADMIN_TOLL_FREE_NUMBER_FLD_NM;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isTollFreeRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_TOLL_FREE) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_TOLL_FREE + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateAddressLine1(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def) throws AtWinXSException {

		String value = request.getAddressLine1();
		String fieldName = SelfAdminUtil.SELF_ADMIN_ADDRESS_LINE_1;
		String label = getTranslation(asb, SFTranslationTextConstants.PREFIX_SF + fieldName, SFTranslationTextConstants.ADDRESS_LINE_1_DFLT_VAL);
		if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_LINE1) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_LINE1 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateAddressLine2(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb) throws AtWinXSException {

		String value = request.getAddressLine2();
		String fieldName = SelfAdminUtil.SELF_ADMIN_ADDRESS_LINE_2;
		if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_LINE2) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_LINE2 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateAddressLine3(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb) throws AtWinXSException {

		String value = request.getAddressLine3();
		String fieldName = SelfAdminUtil.SELF_ADMIN_ADDRESS_LINE_3;
		if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_LINE3) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_LINE3 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateCity(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getCity();
		String fieldName = SelfAdminUtil.SELF_ADMIN_CITY;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_CITY) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_CITY + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}

	protected void validateCountry(UpdateExtendedProfileRequest request, String countryExist, ProfileDefinition def,
			UpdateExtendedProfileResponse response, AppSessionBean asb, Map<String, String> translationMap)
			throws AtWinXSException {

		String value = request.getCountryCd();
		String fieldName = SelfAdminUtil.SELF_ADMIN_COUNTRY_CODE;
		String label = Util.nullToEmpty(translationMap.get(SelfAdminUtil.PAB_CT));
		if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_COUNTRY_CD) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_COUNTRY_CD + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} else if ((value.length() > 0 && value.length() <= SELF_ADMIN_MAX_SIZE_EXT_COUNTRY_CD)
				&& countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO)) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
	}

	protected void validateState(UpdateExtendedProfileRequest request, String countryExist, String stateExist,
			ProfileDefinition def, UpdateExtendedProfileResponse response, AppSessionBean asb,
			Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getStateCd();
		String fieldName = SelfAdminUtil.SELF_ADMIN_EXT_STATE_CODE;
		String label = Util.nullToEmpty(translationMap.get(SelfAdminUtil.PAB_ST));
		if (def.isAddressRequired() && countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NA)
				&& (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NA)
				&& value.length() > SELF_ADMIN_MAX_SIZE_EXT_STATE_CD) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_STATE_CD + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} else if ((value.length() > 0 && value.length() <= SELF_ADMIN_MAX_SIZE_EXT_STATE_CD)
				&& ((countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NA))
						|| (countryExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_YES) && stateExist.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_NO)))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
	}

	protected void validateZip(UpdateExtendedProfileRequest request, UpdateExtendedProfileResponse response,
			AppSessionBean asb, ProfileDefinition def, Map<String, String> translationMap) throws AtWinXSException {

		String value = request.getZip();
		String fieldName = SelfAdminUtil.PAB_ZIP;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (def.isAddressRequired() && (value == null || Util.isBlank(value.trim()))) {

			response.getFieldMessages().put(fieldName, label + AtWinXSConstant.BLANK_SPACE
					+ buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (value.length() > SELF_ADMIN_MAX_SIZE_EXT_ZIP) {

			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					SELF_ADMIN_MAX_SIZE_EXT_ZIP + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fieldName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
	}
	
	private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap) throws AtWinXSException
	{
		return Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(),
				asb.getCustomToken(), errorKey, replaceMap));
	}
	
	// CAP-42342 - Update company profile
	public UpdateCompanyProfileResponse updateCompanyProfile(UpdateCompanyProfileRequest request, SessionContainer sc) throws AtWinXSException
	{
		UpdateCompanyProfileResponse response = new UpdateCompanyProfileResponse();

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		//CAP-45485 - Force to use originator profile in Self Admin for OOB Mode 
		boolean useOriginatorProfile = true;
		boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile,asb);
		
		if ((asb.getProfileNumber() <= 0))
		{
			logErrorMessage(asb, SelfAdminUtil.SELF_ADMIN_NO_PROFILE_SELECTED_ERROR);
		}

		ProfileDefinition profileDefinition = objectMapFactoryService.getEntityObjectMap().getEntity(ProfileDefinition.class, asb.getCustomToken());
		try {
			//method to populate profile definition of User type
			profileDefinition.populate(asb.getSiteID(),asb.getBuID(),asb.getProfileDefId());

			if ((!profileDefinition.isCanUserEditCorporateProfile()))
			{
				logErrorMessage(asb, SelfAdminUtil.SELF_ADMIN_NO_PERMISSION_ERROR);
			}
		}
		catch(AtWinXSException e)
		{ 
			logErrorMessage(asb, SelfAdminUtil.SELF_ADMIN_RETRIEVE_PROFILE_ERROR);
		}

		try {
			
			Map<String, String> validStateAndCountry = selfAdminUtil.validateAndGetCountryStateName(request.getCountryCd(),
					request.getStateCd());
			
			if (validateCompanyProfile(response, request, asb, profileDefinition, 
					validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS), validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS))) {
				
				Profile profile = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class, asb.getCustomToken());
				profile.populate(asb.getProfileNumber());
				CorporateProfile corporateProfile = profile.getCorporateProfile();
				
				if (request.isUseAsdefaultPreferredShipping()) {
					profile.setDefaultAddressSourceCd(SelfAdminUtil.COMPANY_AS_PREFFERED_ADDR_CODE);
				} else {
					if (SelfAdminUtil.COMPANY_AS_PREFFERED_ADDR_CODE.equalsIgnoreCase(profile.getDefaultAddressSourceCd())) {
						profile.setDefaultAddressSourceCd(AtWinXSConstant.EMPTY_STRING);
					}
				}
				
				corporateProfile.setCompanyName(request.getName1());
				corporateProfile.setCompanyName2(request.getName2());
				corporateProfile.setLine1Address(request.getAddressLine1());
				corporateProfile.setLine2Address(request.getAddressLine2());
				corporateProfile.setLine3Address(request.getAddressLine3());
				corporateProfile.setCityName(request.getCity());
				corporateProfile.setStateCd(checkValue(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_EXISTS), request.getStateCd()));
				corporateProfile.setStateName(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_STATE_NAME_LBL));
				corporateProfile.setZipCd(request.getZipCd());
				corporateProfile.setCountryCd(checkValue(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_EXISTS), request.getCountryCd()));
				corporateProfile.setCountryName(validStateAndCountry.get(SelfAdminUtil.SELF_ADMIN_COUNTRY_NAME_LBL));
				corporateProfile.setCompanyPhoneNumber(request.getPhoneNumber());
				corporateProfile.setCompanyFaxNumber(request.getFaxNumber());
				corporateProfile.setCompanyURLAddress(request.getWebUrl());
				corporateProfile.setCompanyLogoURLAddress(request.getImageUrl());
				corporateProfile.setCompanyTitleText(request.getTitle());
				corporateProfile.setCompanyDepartmentName(request.getDepartment());
				corporateProfile.setCompanyDivisionName(request.getDivision());
				
				profile.updateAll(false, false, true, false);
				
				asb.getCorporateProfile().setName(corporateProfile.getCompanyName());
				asb.getCorporateProfile().setCompanyName2(corporateProfile.getCompanyName2());
				asb.getCorporateProfile().setAddress1(corporateProfile.getLine1Address());
				asb.getCorporateProfile().setAddress2(corporateProfile.getLine2Address());
				asb.getCorporateProfile().setAddress3(corporateProfile.getLine3Address());
				asb.getCorporateProfile().setCity(corporateProfile.getCityName());
				asb.getCorporateProfile().setState(corporateProfile.getStateCd());
				asb.getCorporateProfile().setZip(corporateProfile.getZipCd());
				asb.getCorporateProfile().setCountryCode(corporateProfile.getCountryCd());
				asb.getCorporateProfile().setCountryName(corporateProfile.getCountryName());
				asb.getCorporateProfile().setPhone(corporateProfile.getCompanyPhoneNumber());
				asb.getCorporateProfile().setFax(corporateProfile.getCompanyFaxNumber());
				asb.getCorporateProfile().setWebURL(corporateProfile.getCompanyURLAddress());
				asb.getCorporateProfile().setLogoURL(corporateProfile.getCompanyLogoURLAddress());
				asb.getCorporateProfile().setTitle(corporateProfile.getCompanyTitleText());
				asb.getCorporateProfile().setDepartment(corporateProfile.getCompanyDepartmentName());
				asb.getCorporateProfile().setDivision(corporateProfile.getCompanyDivisionName());
				
				//CAP-45485 - Revert originator to Requestor profile after Self Admin process in OOB Mode 
				SelfAdminUtil.revertOriginatorProfileInSelfAdmin(asb, requestorModeModified);
				
				SessionHandler.saveSession(sc.getApplicationSession(), asb.getSessionID(), AtWinXSConstant.APPSESSIONSERVICEID);
			}
		}
		catch(AtWinXSException e)
		{
			logger.error(getErrorPrefix(asb), " updating corporate profile failed with ", e.toString(), e);
			response.setSuccess(false);
			response.setMessage(TranslationTextTag.processMessage(asb.getDefaultLocale(),
						asb.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		}
		return response;
	}

	// CAP-42342 - Validate company profile fields and set translation error message
	public boolean validateCompanyProfile(UpdateCompanyProfileResponse response, UpdateCompanyProfileRequest request,
			AppSessionBean asb, ProfileDefinition def, String countryExist, String stateExist) throws AtWinXSException {
		response.setSuccess(false);
		Properties translationProps = translationService.getResourceBundle(asb,
				SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);
		selfAdminUtil.validateCompanyProfileFields(response, request, asb, def, countryExist, stateExist, translationMap);
		if (response.getFieldMessages().size() > 0) {
			return false;
		}
		response.setSuccess(true);
		return true;
	}
	
	private String checkValue(String flag, String value) {
		return flag.equalsIgnoreCase(SelfAdminUtil.SELF_ADMIN_YES) ? value : AtWinXSConstant.EMPTY_STRING;
	}
	
	private void logErrorMessage(AppSessionBean asb, String errorMessage) throws AccessForbiddenException {
		logger.error(getErrorPrefix(asb), errorMessage);
		throw new AccessForbiddenException(this.getClass().getName());
	}
	
			//CAP-42562	-Start
			@Override
			public UserDefinedfieldsResponse updateUserDefinedFields(UserDefinedFieldsRequest updateUserDefinedFieldsRequest,
					SessionContainer sc) throws AtWinXSException {
				AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
				//CAP-45485 - Force to use originator profile in Self Admin for OOB Mode 
				boolean useOriginatorProfile = true;
				boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile,asb);

				ArrayList<C1UserDefinedField> userDefinedFields=updateUserDefinedFieldsRequest.getC1UserDefinedFields();
				UserDefinedfieldsResponse response = new UserDefinedfieldsResponse();
				response.setSuccess(false);
				if ((asb.getProfileNumber() <= 0))
				{
					logger.error(getErrorPrefix(asb), " is attempting to change their profile but does not have one selected.");
					throw new AccessForbiddenException(this.getClass().getName());
				}
				
				try {
					//CAP-47915
					List<UserDefinedField> udfList = getProfileUDFs(asb);
		        	if (validateUserDefinedFields(udfList, userDefinedFields,response,asb)){
		        		update(userDefinedFields,asb);
		          		response.setSuccess(true);
			       	 }
		        }catch(AtWinXSException e){
		       	    	logger.error(getErrorPrefix(asb), " updating UDF failed with ", e.toString(), e);
		    			response.setSuccess(false);
		    			response.setMessage(translationService.processMessage(asb.getDefaultLocale(),
		    						asb.getCustomToken(), SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR));
		       	     }
		       	
				//CAP-45485 - Revert originator to Requestor profile after Self Admin process in OOB Mode 
				SelfAdminUtil.revertOriginatorProfileInSelfAdmin(asb, requestorModeModified);

		        return response;
			}
			
			
			
			public void update(List<C1UserDefinedField> c1UserDefinedFields,AppSessionBean asb) throws AtWinXSException
			{
				if (c1UserDefinedFields != null && !c1UserDefinedFields.isEmpty())
				{
					for (C1UserDefinedField currentValue : c1UserDefinedFields)
					{
						UserDefinedField userDef = objectMapFactoryService.getEntityObjectMap().getEntity(UserDefinedField.class, asb.getCustomToken());
						userDef.populate(asb.getSiteID(), asb.getBuID(), asb.getProfileNumber(), currentValue.getUdfFieldNumber() );
						C1UXUserDefinedFieldImpl userDefinedField=new C1UXUserDefinedFieldImpl();
						userDefinedField.setSiteID(asb.getSiteID());
						userDefinedField.setBusinessUnitID(asb.getBuID());
						userDefinedField.setProfileNumber(asb.getProfileNumber());
						userDefinedField.setCreateProgramID(this.getClass().getName());
						userDefinedField.setCreateUserID(getUser());
						userDefinedField.setCreateTimestamp(new Date());
						userDefinedField.setUdfFieldNumber(currentValue.getUdfFieldNumber());
						if(userDef.getProfileUDFDefinition().isCanUserEdit()) {
							userDefinedField.setUdfValueText(currentValue.getUdfValueText());
						}else {
							userDefinedField.setUdfValueText(userDef.getUdfValueText());
						}
						updateUDF(userDefinedField,asb);
					}
				}
			}

			public void updateUDF(C1UXUserDefinedFieldImpl udf,AppSessionBean asb) throws AtWinXSException
			{
				UserDefinedFieldDAO userDefinedFieldDAO = objectMapFactoryService.getDAOObjectMap().getObject(UserDefinedFieldDAO.class, getUser(), asb.getCustomToken());
				
				try
				{
					UserDefinedFieldImpl cpUDF = new UserDefinedFieldImpl();
					BeanUtils.copyProperties(cpUDF, udf);
					userDefinedFieldDAO.update(cpUDF);
				}
				catch(Exception ex)
				{
					logger.error(ex.getMessage());
				}
			}
			
			protected String getUser()
			{
				return Util.left(this.getClass().getName(), 16);
			}
			
			//CAP-47915
			public boolean validateUserDefinedFields(List<UserDefinedField> udfList, List<C1UserDefinedField> c1UserDefinedFields,UserDefinedfieldsResponse udfResponse,AppSessionBean asb) throws AtWinXSException
			{
				Map<String, Object> replaceMap = new HashMap<>();
				if (c1UserDefinedFields != null && !c1UserDefinedFields.isEmpty())
				{
					for (C1UserDefinedField currentValue : c1UserDefinedFields)
					{ 	
						UserDefinedField userDef = objectMapFactoryService.getEntityObjectMap().getEntity(UserDefinedField.class, asb.getCustomToken());
						userDef.populate(asb.getSiteID(), asb.getBuID(), asb.getProfileNumber(), currentValue.getUdfFieldNumber() );
						//CAP-47616
						if(null == currentValue.getUdfValueText()) {
							currentValue.setUdfValueText("");
						}
						//CAP-47915
						if(userDef.isExisting()) {
							checkUDFLengthAndRequired(currentValue, replaceMap, userDef, udfResponse, asb);
							//CAP-47776
							if(userDef.getProfileUDFDefinition().isAssignAttributeList() && !validateUDFAssignedAttributeValue(userDef, currentValue) ) {

									udfResponse.getFieldMessages().put("UDF"+ currentValue.getUdfFieldNumber(), userDef.getProfileUDFDefinition().getUDFLabelText() + RouteConstants.SINGLE_SPACE+ translationService.processMessage(asb.getDefaultLocale(),
			    						asb.getCustomToken(), SFTranslationTextConstants.INVALID_VALUE_ERROR_MSG));
							}
						}
					}
				}
				//CAP-47915
				verifyAllRequiredUdfsExistInRequest( udfList, c1UserDefinedFields, udfResponse,asb);
				return udfResponse.getFieldMessages().size() <= 0 ;
				
			}
			
			protected boolean validateUDFAssignedAttributeValue(UserDefinedField userDef,
					C1UserDefinedField currentValue) throws AtWinXSException {

				if (!userDef.getProfileUDFDefinition().isUDFRequired()//CAP-48207
						|| Util.isBlankOrNull(currentValue.getUdfValueText())) {
					return true;
				}

				List<SiteAttributeValue> list = userDef.getProfileUDFDefinition().getSiteAttributeValues()
						.getSiteAttributeValues();

				SiteAttributeValue selectedSiteAttrValue = list.stream().filter(attr -> Util
						.nullToEmpty(currentValue.getUdfValueText()).equalsIgnoreCase(attr.getSiteAttrVal())).findAny()
						.orElse(null);
				return null != selectedSiteAttrValue;

			}
			
			void checkUDFLengthAndRequired(C1UserDefinedField currentValue,Map<String, Object> replaceMap,UserDefinedField userDef,UserDefinedfieldsResponse udfResponse,AppSessionBean asb) throws AtWinXSException {
				// CAP-50381
				if(!userDef.getProfileUDFDefinition().isAssignAttributeList() && (currentValue.getUdfValueText().length()  > userDef.getProfileUDFDefinition().getUDFLengthNumber())) {
					replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,userDef.getProfileUDFDefinition().getUDFLengthNumber()+ AtWinXSConstant.EMPTY_STRING);
					udfResponse.getFieldMessages().put(userDef.getProfileUDFDefinition().getUDFLabelText(),buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
				} 
				if(userDef.getProfileUDFDefinition().isUDFRequired() && (currentValue.getUdfValueText() == null || Util.isBlank(currentValue.getUdfValueText().trim()))){
					replaceMap.put(SFTranslationTextConstants.MUST_NOT_BE_BLANK_KEY,userDef.getProfileUDFDefinition().getUDFLabelText()+ AtWinXSConstant.EMPTY_STRING);
					udfResponse.getFieldMessages().put(userDef.getProfileUDFDefinition().getUDFLabelText(),buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_LABEL, asb, replaceMap));
				}
			}
			//CAP-42562	-End
			//CAP-47915
			void verifyAllRequiredUdfsExistInRequest(List<UserDefinedField> udfList, List<C1UserDefinedField> c1UserDefinedFields, UserDefinedfieldsResponse udfResponse, AppSessionBean asb) throws AtWinXSException {
				Map<String, Object> replaceMap = new HashMap<>();
				List<UserDefinedField> requiredUDFs = udfList.stream().
						filter(udf -> {
							try {
								return (udf!=null && udf.getProfileUDFDefinition()!=null && udf.getProfileUDFDefinition().isUDFRequired());
							} catch (AtWinXSException e) {
								logger.error(e.getMessage());
							}
							return false;
						}).collect(Collectors.toList());

				List<UserDefinedField> missingRequiredUDFs = null; 
				if(c1UserDefinedFields != null) {
				    missingRequiredUDFs = requiredUDFs.stream()
				    .filter(two -> c1UserDefinedFields.stream()
				        .noneMatch(one -> (one.getUdfFieldNumber() == two.getUdfFieldNumber())))
				    .collect(Collectors.toList());
				}else {
					missingRequiredUDFs = requiredUDFs;
				}
			    for(UserDefinedField missedUDF : missingRequiredUDFs) {
				    replaceMap.put(SFTranslationTextConstants.MUST_NOT_BE_BLANK_KEY,missedUDF.getProfileUDFDefinition().getUDFLabelText()+ AtWinXSConstant.EMPTY_STRING);
			    	udfResponse.setFieldMessage("UDF"+missedUDF.getUdfFieldNumber(), buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_LABEL, asb, replaceMap));
			    
			    }
			}
	//CAP-47915		
	public List<UserDefinedField> getProfileUDFs(AppSessionBean appSessionBean) throws AccessForbiddenException{
		// populate Profile Info
		Profile profile = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class, appSessionBean.getCustomToken());
		List<UserDefinedField> udfList =null;
		if (appSessionBean.getProfileNumber() == AtWinXSConstant.INVALID_PROFILE_NUMBER)
		{
			logger.error(getErrorPrefix(appSessionBean), "User does not have a profile number");
			throw new AccessForbiddenException(SelfAdminServiceImpl.class.getName());
		}
		try 
		{
			profile.populate(appSessionBean.getProfileNumber());
			profile.getProfileDefinition().getCustFieldsProfielUDFDefinitions();//CAP-42465 added to include the Profile UDF definition
			//CAP-42465 added to include the Profile UDF definition
			udfList = profile.getUserDefinedFields().getUserDefinedFields();
		} 
		catch (AtWinXSException e) 
		{
			logger.error(e.getMessage());
		}
		return udfList;

	}

	// CAP-43631
	@Override
	public PABDeleteResponse deletePABAddress(PABDeleteRequest request, SessionContainer sc,boolean useOriginator)
			throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		if(!useOriginator) {
			appSessionBean.setProfileNumber(appSessionBean.getOriginatorProfile().getProfileNumber());
		}

		validateAccessToPAB(appSessionBean);

		PABDeleteResponse response = new PABDeleteResponse();
		response.setSuccess(true);

		// validate that the requested list of address IDs is not empty
		if (null == request.getAddressIds() || request.getAddressIds().isEmpty()) {
			response.setSuccess(false);
			response.setMessage(
					getTranslation(appSessionBean, SFTranslationTextConstants.NO_SELECTED_ADDR_ERR_MSG,
							SFTranslationTextConstants.NO_SELECTED_ADDR_DEF_ERR_MSG));
		}

		if (response.isSuccess()) {

			Profile profile = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class,
					appSessionBean.getCustomToken());
			profile.populate(appSessionBean.getProfileNumber());
			int deletedCount = 0;

			AddressBook addressBook = profile.getAddressBook();

			if (null != addressBook.getAddressBook() && !addressBook.getAddressBook().isEmpty()) {
				processDelete(request, appSessionBean, response, profile, deletedCount, addressBook);
			} else {
				setFailedEToDeleteErrorMessage(appSessionBean, response);
			}
		}

		return response;
	}

	protected void processDelete(PABDeleteRequest request, AppSessionBean appSessionBean,
			PABDeleteResponse response, Profile profile, int deletedCount, AddressBook addressBook)
			throws AtWinXSException {

		Map<Integer, Integer> addressBookMap = addressBook.getAddressBook().stream().collect(Collectors
				.toMap(AddressBookRecord::getPersonalAddressID, AddressBookRecord::getPersonalAddressID));

		List<Integer> addressIdsToDelete = request.getAddressIds().stream().distinct().collect(Collectors.toList());

		for (int addressId : addressIdsToDelete) {

			if (addressBookMap.containsKey(addressId)) {

				AddressBookRecord addressBookRecord = objectMapFactoryService.getEntityObjectMap()
						.getEntity(AddressBookRecord.class, appSessionBean.getCustomToken());

				if (profile.getDefaultPersonalAddressID() == addressId) {
					profile.setDefaultPersonalAddressID(0);
					profile.save();
				}

				try {
					addressBookRecord.delete(profile.getSiteID(), profile.getBusinessUnitID(),
							profile.getProfileNumber(), addressId);
					deletedCount++;
				} catch (AtWinXSException e) {
					logger.error(e.getMessage());
					setFailedEToDeleteErrorMessage(appSessionBean, response);
					break;
				}
			}
		}

		response.setDeletedAddressCount(deletedCount);
	}

	protected void setFailedEToDeleteErrorMessage(AppSessionBean appSessionBean, PABDeleteResponse response) {
		response.setSuccess(false);
		response.setMessage(
				getTranslation(appSessionBean, SFTranslationTextConstants.FAILED_TO_DELETE_ADDR_ERR_MSG,
						SFTranslationTextConstants.FAILED_TO_DELETE_ADDR_DEF_ERR_MSG));
	}

	protected void validateAccessToPAB(AppSessionBean appSessionBean) throws AccessForbiddenException {
		if ((null != appSessionBean.getOriginatorOrdProp()
				&& !appSessionBean.getOriginatorOrdProp().isUsePersonalAddrBook())
				|| (appSessionBean.isSharedID() && AtWinXSConstant.INVALID_ID == appSessionBean.getProfileNumber())) {//CAP-46333
			logger.error(getErrorPrefix(appSessionBean), " is not allowed to access this service.");
			throw new AccessForbiddenException(this.getClass().getName());
		}
	}
	// CAP-43631 End
	
	
	//CAP-43630 - Self Admin - search Personal Address Book of Logged-in User
	@Override
	public PABSearchResponse searchPAB(SessionContainer sc, PABSearchRequest pabSearchRequest,boolean useOriginator) 
			throws AtWinXSException {
		
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession avs = sc.getApplicationVolatileSession();
		VolatileSessionBean vsb = avs.getVolatileSessionBean();
		
		Map<String, Object> searchCriteriaMap = new HashMap<>();
		
		PABSearchResponse pabSearchResponse = new PABSearchResponse();
		pabSearchResponse.setSuccess(true);
		
		if(!useOriginator) {
			asb.setProfileNumber(asb.getOriginatorProfile().getProfileNumber());
			asb.setLoginID(asb.getOriginatorProfile().getLoginID());
		}
		
		//Access check for logged-in User, if Shared ID and Invalid Profile Number
		//Access check for logged-in User, if Not have access for PAB
		validateAccessToPAB(asb);
		
		try {
			
			String searchKey =  AtWinXSConstant.EMPTY_STRING;
			String searchValue = AtWinXSConstant.EMPTY_STRING;
			
			if(null != pabSearchRequest.getGenericSearchCriteria() 
					&& !pabSearchRequest.getGenericSearchCriteria().isEmpty()) {
				
				searchKey = Util
						.nullToEmpty(pabSearchRequest.getGenericSearchCriteria().get(0).getCriteriaFieldKey());
				searchValue = Util
						.nullToEmpty(pabSearchRequest.getGenericSearchCriteria().get(0).getCriteriaFieldValue());
			}		
			
			if(selfAdminUtil.validatePABSearchRequest(pabSearchResponse, searchKey, searchValue, asb)) {
			
				searchCriteriaMap.put(searchKey, searchValue);
				retrievePAB(asb,vsb, searchCriteriaMap, pabSearchResponse);
			}
			else {
			
				Collection<C1UXAddress> c1uxAddresses = new ArrayList<>();
				pabSearchResponse.setC1uxAddresses(c1uxAddresses);
			}
				
			
		} catch (AtWinXSException e) {

			logger.error(e.getMessage(), e);
			pabSearchResponse.setSuccess(false);
			pabSearchResponse.setMessage(e.getMessage());
		}

		return pabSearchResponse;
	}	
	
	
	//CAP-43630 - Self Admin - retrieve Personal Address Book for search criteria in map
	protected void retrievePAB(AppSessionBean asb,VolatileSessionBean vsb, Map<String, Object> searchCriteriaMap, PABSearchResponse pabSearchResponse) 
			throws AtWinXSException {

		AddressSourceSettings addressSourceSettings = objectMapFactoryService.getEntityObjectMap()
				.getEntity(AddressSourceSettings.class, asb.getCustomToken());
		
		//CAP-43630 - Added blank parameter to search Personal Address Book ( CP Refs: CAP-6517, CAP-9199 )
		Collection<PersonalAddressVO> addresses = addressSourceSettings.searchPersonalAddressBook(searchCriteriaMap,
				asb, "", vsb.getSelectedSiteAttribute(), false); 

		Collection<C1UXAddress> c1uxAddresses = new ArrayList<>();
		
		if (null != addresses && !addresses.isEmpty()) {
			
			User user = objectMapFactoryService.getEntityObjectMap().getEntity(User.class, asb.getCustomToken());
			user.populate(asb.getSiteID(), asb.getLoginID());
			
			//CP-13100 RAR - Set Shared ID.
			if(user.isSharedID()) {
				
				user.setProfileNumber(asb.getProfileNumber());
			}

			for (PersonalAddressVO address : addresses) {

				// refactor Address
				C1UXAddress c1uxAddress = new C1UXAddress();
				c1uxAddress.setId(address.getPersonalAddressID());
				c1uxAddress.setShipToName1(address.getPersonalAddressName());
				c1uxAddress.setShipToName2(address.getPersonalAddressName2());
				c1uxAddress.setAddress1(address.getAddress1());
				c1uxAddress.setAddress2(address.getAddress2());
				c1uxAddress.setAddress3(address.getAddress3());
				c1uxAddress.setCity(address.getCity());
				c1uxAddress.setState(address.getState());
				c1uxAddress.setCountry(address.getCountry());
				c1uxAddress.setZip(address.getZip());
				c1uxAddress.setShipToAttention(address.getShipToAttn());
				c1uxAddress.setPhone(address.getPhoneNumber());

				if (address.getPersonalAddressID() != -1 && address.getPersonalAddressID() != -2) {
					c1uxAddress.setType("P");
				}

				if (address.getPersonalAddressID() == user.getProfile().getDefaultPersonalAddressID()) {
					c1uxAddress.setDefaultFlag(true);
				}

				c1uxAddresses.add(c1uxAddress);
			}
			pabSearchResponse.setPabCount(addresses.size());
		}
		pabSearchResponse.setC1uxAddresses(c1uxAddresses);
	}

	//CAP-41593- Start
	@Override
	public PABSaveResponse savePABAddress(SessionContainer sc, PABSaveRequest request, boolean uspsValidationFlag,boolean useOriginator) throws AtWinXSException 
	{
		PABSaveResponse response=new PABSaveResponse();
		try
		{
			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			User user = objectMapFactoryService.getEntityObjectMap().getEntity(User.class, appSessionBean.getCustomToken());
			
			//CAP-45544
			if(!useOriginator) {
				user.populate(appSessionBean.getSiteID(), appSessionBean.getOriginatorProfile().getLoginID());
				appSessionBean.setProfileNumber(appSessionBean.getOriginatorProfile().getProfileNumber());
			}else {
				user.populate(appSessionBean.getSiteID(), appSessionBean.getLoginID());
			}
			
			response.setSuccess(false);
			validateAccessToPAB(appSessionBean);
			// CAP-632
			if (user.isSharedID())
			{
				user.setProfileNumber(appSessionBean.getProfileNumber());
			}
			if ((appSessionBean.getProfileNumber() <= 0))
			{
				throw new AccessForbiddenException(this.getClass().getName());
			}
			
		  int personalAddressID = 0;
		  if(validateSavePAB(sc,response,request,appSessionBean, uspsValidationFlag)) {	
			if(request.getAddressID() > -1)
			{
				AddressBookRecord address = objectMapFactoryService.getEntityObjectMap().getEntity(AddressBookRecord.class, appSessionBean.getCustomToken());
				address.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getProfileNumber(), request.getAddressID());
				savePAB(address,request,appSessionBean);
				response.setSuccess(true);
				personalAddressID = address.getPersonalAddressID();
			}
			else if(request.getAddressID() == -1)
			{
				personalAddressID = -1;
			}
			else if(request.getAddressID() == -2)
			{
				personalAddressID = -2;
			}
			
			saveDafaultAddressCheck(request,user,personalAddressID);
		  }
		  else{
				response.setSuccess(false);
			}
	
		} 
		
		catch(AtWinXSException e)
		{
			response.setSuccess(false);
			throw e;
		}
		
		return response;
	}
	
	
	public boolean validateSavePAB(SessionContainer sc, PABSaveResponse response, PABSaveRequest request,
						AppSessionBean asb, boolean uspsValidationFlag) throws AtWinXSException {
		response.setSuccess(false);
		Properties translationProps = translationService.getResourceBundle(asb,
				SFTranslationTextConstants.SELF_ADMIN_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);
		selfAdminUtil.validatePABFields(sc,response, request, asb, translationMap, countryLocatorService, uspsValidationFlag);

		if(uspsValidationFlag 
				&& Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(SelfAdminUtil.PAB_COUNTRY_USA) 
				&& !request.isHasPassedZipValidation()) {
			
			selfAdminUtil.validatePABFieldsUSPSV1(response, request, asb, translationService);	
		}
		
		if (response.getFieldMessages().size() > 0) {
				return false;
		}
		else if(uspsValidationFlag 
				&& Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(SelfAdminUtil.PAB_COUNTRY_USA) 
				&& !request.isHasPassedZipValidation()) {

			validateUSPSAddressV1(sc,request,response);
			if (!response.isSuccess()) {
				return false;
			}	
		}
		response.setSuccess(true);
		return true;
    }
	
	//CAP-44961: USPS address validation - New Version(v1)  
	public void validateUSPSAddressV1(SessionContainer sc, PABSaveRequest address,PABSaveResponse response) throws AtWinXSException {
		
		try {	
			
			ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
			VolatileSessionBean vsb = applicationVolatileSession.getVolatileSessionBean();
			ApplicationSession appSession = sc.getApplicationSession();
			AppSessionBean appSessionBean = appSession.getAppSessionBean();
			
			OENewAddressFormBean addressBean = new OENewAddressFormBean();

			if (address != null) {
				
				addressBean.setAddressLine1(SelfAdminUtil.replaceAddressSpecialChar(Util.nullToEmpty(address.getAddress1())));
				addressBean.setAddressLine2(SelfAdminUtil.replaceAddressSpecialChar(Util.nullToEmpty(address.getAddress2())));
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
				if(zip.length() > 5) {
					
					zip = zip.substring(0, 5);
				}
				addressBean.setZip(zip);
				addressBean.setCountry(Util.nullToEmpty(address.getCountry()));
			}
			
			OECheckoutAssembler assembler = oeAssemblerFactoryService.getCheckoutAssembler(vsb, appSessionBean);
			USPSAddressInfoServiceParmsVO uspsAddressInfo = assembler.validateAddressUSPS(addressBean);
			
			if (!Util.isBlankOrNull(uspsAddressInfo.getErrorMessage())) {

				response.setShowSuggestedAddress(false);
				//CAP-45375
				response.setMessage(SelfAdminUtil.getTranslationMessageForUSPSErrors(uspsAddressInfo.getErrorMessage(),appSessionBean,translationService));
				response.setSuccess(false);
			}
			else if(validateSuggestedAddressC1UXV1(uspsAddressInfo, addressBean,response)) {
				
				response.setShowSuggestedAddress(true);
				response.setSuccess(false);
			}	
			else {
				
				response.setShowSuggestedAddress(false);
				response.setSuccess(true);
			}
		}
		catch(AtWinXSException e)
		{
			Util.asCPRPCException(e);
		}
	}
	

	//CAP-44961: validation for USPS suggested address if there is no error
	public boolean validateSuggestedAddressC1UXV1(USPSAddressInfoServiceParmsVO uspsAddressInfo, 
			OENewAddressFormBean addressBean, PABSaveResponse response)  {

		boolean res=false;
		
		uspsAddressInfo.setAddress1(Util.nullToEmpty(uspsAddressInfo.getAddress1()));
		uspsAddressInfo.setAddress2(Util.nullToEmpty(uspsAddressInfo.getAddress2()));
		uspsAddressInfo.setCity(Util.nullToEmpty(uspsAddressInfo.getCity()));
		uspsAddressInfo.setState(Util.nullToEmpty(uspsAddressInfo.getState()));
		uspsAddressInfo.setZipcode(Util.nullToEmpty(uspsAddressInfo.getZipcode()));
		if(!uspsAddressInfo.getAddress1().equalsIgnoreCase((addressBean.getAddressLine1() 
			+ AtWinXSConstant.BLANK_SPACE + addressBean.getAddressLine2()).trim()) 
				|| (Util.isBlank(addressBean.getAddressLine1()) 
						&& !uspsAddressInfo.getAddress2().equalsIgnoreCase(addressBean.getAddressLine2()))
				|| !uspsAddressInfo.getCity().equalsIgnoreCase(addressBean.getCity())
				|| !uspsAddressInfo.getState().equalsIgnoreCase(addressBean.getState())
				|| (uspsAddressInfo.getZipcode().length()>4 
						&& addressBean.getZip().length()>4 
						&& !uspsAddressInfo.getZipcode().substring(0,5).equalsIgnoreCase(addressBean.getZip().substring(0,5)))) {  

			//CAP-45374 -	USPS validation Suggested Address need to be split of over 30 characters 
			if (uspsAddressInfo.getAddress1().length()>30 || uspsAddressInfo.getAddress2().length()>30) {
				
				String addressToSplit = uspsAddressInfo.getAddress1() + AtWinXSConstant.BLANK_SPACE + uspsAddressInfo.getAddress2() ;
				//CAP-45374 - split USPS suggested address without breaking Secondary Address Unit Designator (addressLineFlag=true)   
				String addressLine1 = SelfAdminUtil.splitUSPSAddressByDesignator(addressToSplit, true);
				//CAP-45374 - split USPS suggested address without breaking word alone (addressLineFlag=false)
				String addressLine2 = SelfAdminUtil.splitUSPSAddressByDesignator(addressToSplit.substring(addressLine1.length()+1), false);
				response.setSuggestedAddress1(addressLine1);
				response.setSuggestedAddress2(addressLine2);
			}
			else {
				
				response.setSuggestedAddress1(Util.nullToEmpty(uspsAddressInfo.getAddress1()));
				response.setSuggestedAddress2(Util.nullToEmpty(uspsAddressInfo.getAddress2()));
			}
			response.setSuggestedCity(Util.nullToEmpty(uspsAddressInfo.getCity()));
			response.setSuggestedState(Util.nullToEmpty(uspsAddressInfo.getState()));
			response.setSuggestedZip(Util.nullToEmpty(uspsAddressInfo.getZipcode()));
			res=true;
		}
		return res;
	}
	
	public void saveDafaultAddressCheck(PABSaveRequest request,User user,int personalAddressID) throws AtWinXSException {
		if(request.isDefaultAddress())
		{
			user.getProfile().setDefaultPersonalAddressID(personalAddressID);
			user.getProfile().save();
		}
		else if (user.getProfile().getDefaultPersonalAddressID() == personalAddressID) // CAP-2628
		{
			// remove default address setting
			user.getProfile().setDefaultPersonalAddressID(0);
			user.getProfile().save();
		}
		
	}


	  public void savePAB(AddressBookRecord address,PABSaveRequest request,AppSessionBean appSessionBean) throws AtWinXSException {
			address.setSiteID(appSessionBean.getSiteID());
			address.setBusinessUnitID(appSessionBean.getBuID());
			address.setProfileNumber(appSessionBean.getProfileNumber());
			address.setPersonalAddressID(request.getAddressID());
			address.setPersonalAddressName(request.getName());
			address.setPersonalAddressName2(request.getName2());
			address.setLine1Address(request.getAddress1());
			address.setLine2Address(request.getAddress2());
			address.setLine3Address(request.getAddress3());
			address.setCityName(request.getCity());
			address.setStateCd(request.getState());
			address.setZipCd(request.getZip());
			address.setCountryCd(request.getCountry());
			address.setShipToAttentionTx(request.getShipToAttn());
			address.setZipValidInd(request.isHasPassedZipValidation());
			address.setPhoneNumber(request.getPhoneNumber());
			
			if(request.getAddressID() > 0)
			{
				address.setChangeUserID(appSessionBean.getLoginID());
			}
			else
			{
				address.setCreateUserID(appSessionBean.getLoginID());
			}
			
			address.save();
	 }
							
			//CAP-41593 End	
	  
	//CAP-44387 - Start 
	  
	  public PABImportResponse importAllPABAddresses(SessionContainer sc, PABImportRequest importRequest,boolean useOriginator) throws AtWinXSException, CPRPCException{
	  		  
	  		  PABImportResponse pabImportResult = new PABImportResponse();
	  		  String genericErrMsg = "";
	  		  try
	  			{
	  			  
	  			  
	  				ApplicationSession appSession = sc.getApplicationSession();
	  				AppSessionBean appSessionBean = appSession.getAppSessionBean();
	  				//CAP-45544
	  				if(!useOriginator) {
	  					appSessionBean.setProfileNumber(appSessionBean.getOriginatorProfile().getProfileNumber());
	  				}
	  				
	  				
	  				validateAccessToPAB(appSessionBean);
	  				genericErrMsg = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),appSessionBean.getCustomToken(), TranslationTextConstants.ADDRESS_IMPORT_FAIL_GENERIC, null));
	  				if(importRequest.getFile()==null || importRequest.getFile().isEmpty()) {
	  			    	pabImportResult.getFieldMessages().put(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
	  							appSessionBean.getCustomToken(),SFTranslationTextConstants.IMPORT_FAILED),"" + AtWinXSConstant.BLANK_SPACE
	  									+ buildErrorMessage(SFTranslationTextConstants.FILE_NOT_FOUND, appSessionBean, null));
	  			    }else {	
	  					importRequest.getFile().getOriginalFilename();
	  					validateFileExtension(importRequest.getFile().getOriginalFilename(), appSessionBean, pabImportResult);
	  					validateUploadType(importRequest, appSessionBean, pabImportResult);
	  			    }
	  				if (pabImportResult.getFieldMessages().size() > 0) {
	  					pabImportResult.setSuccess(false);
	  					pabImportResult.setFieldMessages(pabImportResult.getFieldMessages());
	  				}
	  				else {
	  				User user = ObjectMapFactory.getEntityObjectMap().getEntity(User.class, appSessionBean.getCustomToken());
	  				user.populate(appSessionBean.getSiteID(), appSessionBean.getLoginID());
	  				if (user.isSharedID())
	  				{
	  					user.setProfileNumber(appSessionBean.getProfileNumber());
	  				}
	  				if ((appSessionBean.getProfileNumber() <= 0))
	  				{
	  					throw new AccessForbiddenException(this.getClass().getName());
	  				}
	  				
	  				File importFile=createAndCopyFile(importRequest.getFile());
	  				String uploadType = importRequest.getUploadType();
	  				boolean reload =ModelConstants.UPLOAD_TYPE_REPLACE.equals(uploadType);
	  				AddressBookImpExp pabImport= ObjectMapFactory.getEntityObjectMap().getEntity(AddressBookImpExp.class, appSessionBean.getCustomToken());
	  				AddressBookImportResult importResult = pabImport.importAddress(appSessionBean.getProfileNumber(), // CAP-13950 - pass selected profile, not -1 if profile selected on shared user
	  						ModelConstants.PROFILE_TYPE_USER, user.getSiteID(), user.getBusinessUnitID(), reload, importFile, appSessionBean);
	  				
	  				
	  				//Codes for testing onSuccess and onFailure events in ImportAllPABAddressPopup
	  				if(importResult.isSuccess())
	  				{
	  					Message success = new Message();
	  					String sucessMsg = importResult.getSuccessMsg(appSessionBean);
	  					success.setSuccessMsg(sucessMsg);
	  					pabImportResult.setSuccess(true);
	  					pabImportResult.setSuccessMsg(sucessMsg);
	  				}
	  				else 
	  				{
	  					
	  					pabImportResult.setSuccess(false);
	  					String errorMsg = importResult.getErrorMsg(appSessionBean); 					
	  					pabImportResult.setFieldMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
	  							appSessionBean.getCustomToken(),SFTranslationTextConstants.IMPORT_FAILED),errorMsg);
	  				}
	  				
	  				populateImportResultBean(importResult, pabImportResult);
	  			}	
	  				return pabImportResult;
	  		  }catch(AtWinXSException atWinEx)
	  		  	{
		  			logger.error("SelfMaintenanceServiceImpl.importAllPABAddresses()", atWinEx);
		  			throw Util.asCPRPCException(new Exception(genericErrMsg));
	  		  	}
		  		catch(Exception ex)
		  		{
		  			throw Util.asCPRPCException(new Exception(genericErrMsg));
		  		}
	  			
	  		}
	  		
	  		private void populateImportResultBean(AddressBookImportResult source, PABImportResponse target)
	  		{
	  			target.setFailed(source.isFailed());
	  			target.setNumOfTotalRows(source.getNumOfTotalRows());
	  			target.setNumWorksheets(source.getNumWorksheets());
	  			target.setRowsImported(source.getRowsImported());
	  			
	  		}
	  		
	  		protected String getClassName() {
	  			return className;
	  		}
	  		
	  		private File createAndCopyFile(MultipartFile file){
	  			String newFileName=appendCurrentDateTimeToFile(file.getOriginalFilename());
	  			File importFile = new File(AdminConstant.UPLOAD_FOLDER, newFileName);
	  			try {
	  				file.transferTo(importFile);
	  			} catch (IllegalStateException | IOException e) {
	  				logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
	  			}
	  			return importFile;
	  		}
	  	  
	  		
	  		public String appendCurrentDateTimeToFile(String fileName) {
	  			int lastIndex=fileName.lastIndexOf('.');
	  			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	  			return fileName.substring(0, lastIndex ) + "_"+ dateFormat.format(new Date()) + fileName.substring(lastIndex);
	  		}
	  		
	  		
	  		public void validateFileExtension(String fullName,AppSessionBean appSessionBean,PABImportResponse pabImportResult) throws AtWinXSException {
	  			String[] fileExtnCode= {"xls","xlsx"};
	  			String fileName = new File(fullName).getName();
	  		    int dotIndex = fileName.lastIndexOf('.');
	  		    if(!Arrays.asList(fileExtnCode).contains(fileName.substring(dotIndex + 1))) {
	  		    	pabImportResult.getFieldMessages().put(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
	  						appSessionBean.getCustomToken(),SFTranslationTextConstants.IMPORT_FAILED),"" + AtWinXSConstant.BLANK_SPACE
	  								+ buildErrorMessage(SFTranslationTextConstants.INVALID_FILE_EXTN, appSessionBean, null));
	  		   
	  		    }
	  		}
	  		
	  		public void validateFileNotNull(PABImportRequest importRequest,AppSessionBean appSessionBean,PABImportResponse pabImportResult) throws AtWinXSException {
	  			    if(importRequest.getFile()==null) {
	  			    	pabImportResult.getFieldMessages().put(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
	  							appSessionBean.getCustomToken(),SFTranslationTextConstants.IMPORT_FAILED),"" + AtWinXSConstant.BLANK_SPACE
	  									+ buildErrorMessage(SFTranslationTextConstants.FILE_NOT_FOUND, appSessionBean, null));
	  			    }
	  			    
	  			}
	  		
	  		
	  		public void validateUploadType(PABImportRequest importRequest,AppSessionBean appSessionBean,PABImportResponse pabImportResult) throws AtWinXSException {
	  			String[] uploadTypeCode = {"A","R"};
	  			if( importRequest.getUploadType() == null || importRequest.getUploadType().isEmpty()) {
	  				pabImportResult.getFieldMessages().put(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
	  						appSessionBean.getCustomToken(),SFTranslationTextConstants.IMPORT_FAILED),"" + AtWinXSConstant.BLANK_SPACE
	  								+ buildErrorMessage(SFTranslationTextConstants.IMPORT_UPLOAD_TYPE, appSessionBean, null));
	  			}
	  			
	  			if((!importRequest.getUploadType().isEmpty() && importRequest.getUploadType() != null) && !Arrays.asList(uploadTypeCode).contains(importRequest.getUploadType())) {
	  				pabImportResult.getFieldMessages().put(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
	  						appSessionBean.getCustomToken(),SFTranslationTextConstants.IMPORT_FAILED),"" + AtWinXSConstant.BLANK_SPACE
	  						+ buildErrorMessage(SFTranslationTextConstants.INPUT_FIELD_INVALID, appSessionBean, null));
	  			}
	  		}
	  	  
	  	  //CAP-44387 - End

	// CAP-44422
	@Override
	public PABExportResponse exportPABAddresses(SessionContainer sc, HttpServletResponse httpServletResponse,boolean useOriginator)
			throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		//CAP-45544
		if(!useOriginator) {
			appSessionBean.setProfileNumber(appSessionBean.getOriginatorProfile().getProfileNumber());
		}

		validateAccessToPAB(appSessionBean);

		PABExportResponse response = new PABExportResponse();
		response.setSuccess(true);

		File exportedFile = getExportedFile(appSessionBean, response);

		if (null != exportedFile) {
			processWriteToResponseStream(httpServletResponse, response, exportedFile);
		}

		return response;
	}

	protected void processWriteToResponseStream(HttpServletResponse httpServletResponse,
			PABExportResponse response, File exportedFile) {
		try (ServletOutputStream out = httpServletResponse.getOutputStream();
				FileInputStream in = getFileInputStream(exportedFile)) {

			httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + exportedFile.getName() + "\"");

			byte[] bytes = new byte[AtWinXSConstant.TWO_KB];
			int bytesRead;

			// Loop through our file and stream to the user here
			while ((bytesRead = in.read(bytes)) != -1) {
				out.write(bytes, 0, bytesRead);
			}

			out.flush();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		} finally {
			try {
				deleteTempExportFile(exportedFile);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected File getExportedFile(AppSessionBean appSessionBean, PABExportResponse response) {

		AddressBookImpExp addressBookImpExp = objectMapFactoryService.getEntityObjectMap()
				.getEntity(AddressBookImpExp.class, appSessionBean.getCustomToken());
		File exportedFile = null;

		try {
			exportedFile = addressBookImpExp.exportAddress(appSessionBean.getSiteID(), appSessionBean.getBuID(),
					appSessionBean.getProfileNumber());
		} catch (AtWinXSException | IOException e) {
			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		return exportedFile;
	}
	// CAP-44422 End
	
	
	//CAP-46801 - Save user selected site attributes from list of available site attribute for user profile at Site/BU level
	@Override
	public C1UserSiteAttributesResponse saveUserSiteAttributes(C1UserSiteAttributesRequest c1UserSiteAttributesRequest,
			SessionContainer sc) throws AtWinXSException {
		
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		
		//Force to use originator profile in Self Admin for OOB Mode 
		boolean useOriginatorProfile = true;
		boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile,asb);
		
		if ((asb.getProfileNumber() <= 0))
		{
			logger.error(getErrorPrefix(asb), " is attempting to change their profile but does not have one selected.");
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		C1UserSiteAttributesResponse response = new C1UserSiteAttributesResponse();
		response.setSuccess(true);
		
		List<C1UserSiteAttribute> c1UserSiteAttributesLst = c1UserSiteAttributesRequest.getC1UserSiteAttributes();
		
		if((null != c1UserSiteAttributesLst && !c1UserSiteAttributesLst.isEmpty())) {
			
			Profile profile = new ProfileImpl();
			profile.setSiteID(asb.getSiteID());
			profile.setBusinessUnitID(asb.getBuID());
			profile.setProfileNumber(asb.getProfileNumber());
			SiteAttributes siteAttributesForProfileLst = profile.getSiteAttributes();
			
			
			String errorMessage = translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
					SFTranslationTextConstants.UNABLE_TO_SAVE_SITE_ATTR); 
			
			
			List<C1UserSiteAttribute> editableSiteAttributesLst = new ArrayList<>(); 
			
			if(!validateSiteAttrIDAndAttrValueID(siteAttributesForProfileLst,c1UserSiteAttributesLst, editableSiteAttributesLst)) {
				
				response.setSuccess(false);
				response.setMessage(errorMessage);
				return response;
			}
			
			Collection<String> errMsgs = validateSiteAttributeMinMax(asb , siteAttributesForProfileLst, editableSiteAttributesLst);

			if(!errMsgs.isEmpty()) {
				
				response.setSuccess(false);
				response.setMessage(errMsgs.stream().collect(Collectors.joining(" ")));
				return response;
			}
			
			try {

				//Delete and Save Complete set of attribute if editable else ignore
				IProfileInterface profileAdmin = profileComponentLocatorService.locate(asb.getCustomToken());
				ProfileVO profileVO = profileAdmin.getProfile(new ProfileVOKey(asb.getSiteID(), asb.getBuID(), asb.getProfileNumber()));
				deleteAndSaveSiteAttributes(asb, profileAdmin, profileVO, editableSiteAttributesLst, asb.getLoginID()); 
			
			} catch (AtWinXSException e) {
				
				logger.error(e.getMessage(), e);
				response.setSuccess(false);
				response.setMessage(errorMessage);
			}
		}
		
		//Revert originator to Requestor profile after Self Admin process in OOB Mode 
		SelfAdminUtil.revertOriginatorProfileInSelfAdmin(asb, requestorModeModified);
		
        return response;
	}
	
	//CAP-46801 - validate user selected editable site attribute and value(s) at the profile level  
	@Override
	public boolean validateSiteAttrIDAndAttrValueID(SiteAttributes siteAttributesForProfileLst,
			List<C1UserSiteAttribute> c1UserSiteAttributesLst, 
			List<C1UserSiteAttribute> editableSiteAttributesLst) 
					throws AtWinXSException {
		
		for (C1UserSiteAttribute c1UserSiteAttribute : c1UserSiteAttributesLst) {

			int attrIDNew = c1UserSiteAttribute.getAttributeID();
			
			for(SiteAttribute siteAttribute : siteAttributesForProfileLst.getSiteAttrs()) {
				
				if(siteAttribute.getAttrID()==attrIDNew && 
						(siteAttribute.getDisplayType().name().equalsIgnoreCase(ModelConstants.ATTR_DISP_TYPE_EDITABLE) || 
						 siteAttribute.getDisplayType().name().equalsIgnoreCase(ModelConstants.ATTR_DISP_TYPE_EDITABLEREQ))) {
					
					// Prepare final list of editable attribute
					editableSiteAttributesLst.add(c1UserSiteAttribute);
					if(!validateSiteAttrValueID(siteAttribute, c1UserSiteAttribute)) {
					
						return false;
					}	
					break;
				}
			}
		}	
		return true;
	}
	
	
	//CAP-46801 - validate user assigned site attribute Value ID indeed assigned to respective attribute ID  
	@Override
	public boolean validateSiteAttrValueID(SiteAttribute siteAttribute,
			C1UserSiteAttribute c1UserSiteAttribute) throws AtWinXSException {
		
		List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst = c1UserSiteAttribute.getC1UserSiteAttributeValues();
		
		SiteAttributeValues siteAttributeValues = siteAttribute.getSiteAttributeValuesForProfile();
		List<SiteAttributeValue> siteAttributeValuesLst = siteAttributeValues.getSiteAttributeValues();
		
		boolean isValidAttrValID = true;
		
		if ((c1UserSiteAttributeValueLst != null) && !c1UserSiteAttributeValueLst.isEmpty()) {
			
			for (C1UserSiteAttributeValue c1UserSiteAttributeValue : c1UserSiteAttributeValueLst) {

				isValidAttrValID = false;
			
				int attrValIDNew = c1UserSiteAttributeValue.getAttributeValueID();
		
				for(SiteAttributeValue siteAttributeValue : siteAttributeValuesLst) {
				
					if(siteAttributeValue.getSiteAttrValID()==attrValIDNew) {
					
						siteAttributeValue.setAssigned(true);
						isValidAttrValID = true;
						break;
					}
				}
				if(!isValidAttrValID) {
					return isValidAttrValID; 
				}
			}
		}
		return isValidAttrValID;	
	}

	
	//CAP-46801 - validate each site attribute in the List of site attribute rule set for user profile at Site/BU level
	@Override
	public Collection<String>  validateSiteAttributeMinMax(AppSessionBean asb , SiteAttributes siteAttributes, 
			List<C1UserSiteAttribute> c1UserSiteAttributesLst) throws AtWinXSException {

		Collection<String> errMsgs = new ArrayList<>();
		
		if ((c1UserSiteAttributesLst != null) && !c1UserSiteAttributesLst.isEmpty()) {
			
			for (C1UserSiteAttribute c1UserSiteAttribute : c1UserSiteAttributesLst) {
			
				int attrIDNew = c1UserSiteAttribute.getAttributeID();
				for(SiteAttribute siteAttribute : siteAttributes.getSiteAttrs()) {
					
					validateMinMax(asb , siteAttribute, c1UserSiteAttribute, attrIDNew, errMsgs); 
				}
			}	
		}	
		return errMsgs;
	}
	
	//CAP-46801 - validate site attribute for Min, Max , Min and Max rule 
	@Override
	public void  validateMinMax(AppSessionBean asb , SiteAttribute siteAttribute, 
			C1UserSiteAttribute c1UserSiteAttribute, int attrIDNew, Collection<String> errMsgs) throws AtWinXSException {
		
		if(	(siteAttribute.getAttrID()==attrIDNew ) &&
			((siteAttribute.getMinRequired() > 0 && 
					c1UserSiteAttribute.getC1UserSiteAttributeValues().size() < siteAttribute.getMinRequired()) || 
			 (siteAttribute.getMaxRequired() > 0 && 
					c1UserSiteAttribute.getC1UserSiteAttributeValues().size() > siteAttribute.getMaxRequired()))) {
		
			String message = "";
			
			if(siteAttribute.getMinRequired() == 0) {
				
				message = 	translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.SITE_ATTR_LBL) + " " + siteAttribute.getAttrDisplayName() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.MUST_HAVE_MAX) + " " + siteAttribute.getMaxRequired() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.VALS_LBL) + ".";
			}
			else if(siteAttribute.getMaxRequired() == 0) {

				message = 	translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.SITE_ATTR_LBL) + " " + siteAttribute.getAttrDisplayName() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.MUST_HAVE_MIN) + " " + siteAttribute.getMinRequired() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.VALS_LBL) + ".";
			}
			else
			{
				message = 	translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.SITE_ATTR_LBL) + " " + siteAttribute.getAttrDisplayName() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.MUST_HAVE_MIN) + " " + siteAttribute.getMinRequired() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.VALS_LBL) + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.AND_MAX_OF) + " " + siteAttribute.getMaxRequired() + " " + 
							translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), 
								SFTranslationTextConstants.VALS_LBL) + ".";
			}
			
			if(message.length() > 0)
			{
				errMsgs.add(message);
			}
		}
	}
	
	//CAP-46801 - Delete and Save Complete set of attribute/value if editable else ignore
	private void deleteAndSaveSiteAttributes(AppSessionBean asb, IProfileInterface profileAdmin, ProfileVO profileVO, 
			List<C1UserSiteAttribute> editableSiteAttributesLst, String loggedInUser) throws AtWinXSException {
		
		if ((editableSiteAttributesLst != null) && !editableSiteAttributesLst.isEmpty()) {
			
			for (C1UserSiteAttribute c1UserSiteAttribute : editableSiteAttributesLst) {
			
				int attrID = c1UserSiteAttribute.getAttributeID();
				
				//Delete only editable attribute and associated attribute values   
				IProfileAttribute profileAttr = profileAttributeComponentLocatorService.locate(asb.getCustomToken());
				profileAttr.deleteProfileAttrValues(null, asb.getSiteID(), asb.getBuID(), attrID, asb.getProfileNumber());
				
				List<C1UserSiteAttributeValue> c1UserSiteAttributeValueLst = c1UserSiteAttribute.getC1UserSiteAttributeValues();
					
				if (c1UserSiteAttributeValueLst != null  && !c1UserSiteAttributeValueLst.isEmpty()) {
					
					for (C1UserSiteAttributeValue c1UserSiteAttributeValue : c1UserSiteAttributeValueLst) {
							
						int attrValID = c1UserSiteAttributeValue.getAttributeValueID();
						
						//Create new set of selected attribute/values
						profileAdmin.createProfileAttrAssignment(null, new BUPrflAttrVO(profileVO.getSiteID(), profileVO.getBuID(),
									profileVO.getProfileNumber(), attrID, attrValID, null), loggedInUser); 					
					}
				}
			}
		}
	}	
}