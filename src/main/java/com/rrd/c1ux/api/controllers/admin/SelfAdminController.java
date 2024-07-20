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
 * 	03/27/23	A Boomker			CAP-37891		Initial version
 *  03/27/23	S Ramachandran		CAP-39201 		API Build - Get Profile Definition of User Type
 *  03/31/23    M Sakthi            CAP-39581 		API Build - Get Password Requirements
 *	03/29/23	A Boomker			CAP-39510		Adding handling for change password functionality
 *	04/03/23	S Ramachandran		CAP-39610		Get Profile Definition of User Type in Self Admin
 * 	04/10/23	A Boomker			CAP-37890		Adding handling for update basic profile API
 * 	07/04/23	N Caceres			CAP-37898		Adding handling for Update Company Profile API
 *	07/11/23	M Sakthi			CAP-37901	    C1UX API - Self Admin – Update Profile User Defined Fields (API Build)
 *	07/13/23	S Ramachandran		CAP-42258		Added handling for for update extended Personal Profile 
 *  08/09/23    M Sakthi			CAP-42562       C1UX BE - Self Admin – Update Profile User Defined Fields (API Build)
 *  01/29/24	S Ramachandran		CAP-46635		C1UX API - save site attribute information for a user
 *  02/01/24	S Ramachandran		CAP-46801		Integrated save site attribute Service to API call in Self Admin
 */

package com.rrd.c1ux.api.controllers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesRequest;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttributesResponse;
import com.rrd.c1ux.api.models.admin.CORetrievePasswordRuleResponse;
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
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("SelfAdminController")
@Tag(name = "Self Admin APIs")
public class SelfAdminController extends BaseCPApiController {

	private static final Logger logger = LoggerFactory.getLogger(SelfAdminController.class);

	@Autowired
	SelfAdminService mSelfAdminService;

	/**
	 * @param tokenReader   {@link TokenReader}
	 * @param sessionReader {@link CPSessionReader}
	 */
	protected SelfAdminController(TokenReader tokenReader, CPSessionReader sessionReader,SelfAdminService selfAdminService) {
		super(tokenReader, sessionReader);
		mSelfAdminService = selfAdminService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ADMIN_SERVICE_ID;
	}

	@PostMapping(value = RouteConstants.UPDATE_PW_API, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate and Update User's own password")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<UpdatePasswordResponse> updatePassword(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UpdatePasswordRequest updatePasswordRequest) throws AtWinXSException {

		SessionContainer sc = getSessionContainer(ttsession);

		UpdatePasswordResponse response = mSelfAdminService.updatePassword(updatePasswordRequest, sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@GetMapping(value = RouteConstants.GET_PASSWORD_RULE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Password rule")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CORetrievePasswordRuleResponse> getPasswordRules(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
		logger.debug("Get Password rule");
		SessionContainer sc = getSessionContainer(ttsession);
		CORetrievePasswordRuleResponse response = mSelfAdminService.getPasswordRule(sc);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}


	/**
	 * @param ttsession {@link String}
	 * @return ProfileDefinitionResponse {@link ProfileDefinitionResponse}
	 * @throws AtWinXSException
	 */
	//CAP-39610 - Self Admin - Get Profile Definition of User Type
	@GetMapping(value=RouteConstants.PROFILE_DEFINITION, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve Profile definition of logged-in user type")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<ProfileDefinitionResponse> getProfileDefinition(@RequestHeader(value = "ttsession", required=false) String ttsession)
			throws AtWinXSException {

		logger.debug("In getProfileDefinition()");

		SessionContainer sc = getSessionContainer(ttsession);

		ProfileDefinitionResponse response = mSelfAdminService.getProfileDefinition(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {

		return asb.hasService(getServiceID());
	}

	@PostMapping(value = RouteConstants.UPDATE_BASIC_PROFILE_API, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate and Update User's own basic profile fields")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<UpdateBasicProfileResponse> updatePassword(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UpdateBasicProfileRequest updateProfileRequest) throws AtWinXSException {

		SessionContainer sc = getSessionContainer(ttsession);

		UpdateBasicProfileResponse response = mSelfAdminService.updateBasicProfile(updateProfileRequest, sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
		
	@PostMapping(value = RouteConstants.UPDATE_USER_DEFINE_FIELDS_API, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate and Update User Define fields")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<UserDefinedfieldsResponse> updateUserDefinedFields(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UserDefinedFieldsRequest updateUserDefinedFieldsRequest) throws AtWinXSException {

		logger.debug("In SelfAdminController - updateUserDefineFields() ");
		
		SessionContainer sc = getSessionContainer(ttsession);
		UserDefinedfieldsResponse response = mSelfAdminService.updateUserDefinedFields(updateUserDefinedFieldsRequest, sc);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	@PostMapping(value = RouteConstants.UPDATE_EXTENDED_PROFILE_API, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate and Update User's own extended profile fields")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<UpdateExtendedProfileResponse> updateExtendedProfile(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UpdateExtendedProfileRequest updateExtendedRequest) throws AtWinXSException {

		logger.debug("In SelfAdminController - updateExtendedProfile() ");
		
		SessionContainer sc = getSessionContainer(ttsession);
		UpdateExtendedProfileResponse response = mSelfAdminService.updateExtendedProfile(updateExtendedRequest, sc);	

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	@PostMapping(value = RouteConstants.UPDATE_COMPANY_PROFILE_API, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate and Update User's own company profile fields")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<UpdateCompanyProfileResponse> updateCompanyProfile(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UpdateCompanyProfileRequest updateCompanyProfileRequest) throws AtWinXSException {

		logger.debug("In SelfAdminController - updateCompanyProfile() ");
		
		SessionContainer sc = getSessionContainer(ttsession);
		UpdateCompanyProfileResponse response = mSelfAdminService.updateCompanyProfile(updateCompanyProfileRequest, sc);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	@PostMapping(value = RouteConstants.SAVE_SITE_ATTRIBUTES, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate and Save Site Attributes")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<C1UserSiteAttributesResponse> saveSiteAttributes(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody C1UserSiteAttributesRequest c1UserSiteAttributesRequest)  throws AtWinXSException {

		logger.debug("In SelfAdminController - saveSiteAttributes() ");
		
		SessionContainer sc = getSessionContainer(ttsession);
		C1UserSiteAttributesResponse response = mSelfAdminService.saveUserSiteAttributes(c1UserSiteAttributesRequest, sc);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
