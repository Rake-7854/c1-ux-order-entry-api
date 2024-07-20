/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	12/27/23	L De Leon			CAP-45907				Initial Version
 *	01/04/24	Satishkumar A		CAP-45908				C1UX API - Create api to retrieve initial user/profile information
 *	01/12/24	Satishkumar A		CAP-46380				C1UX BE - Create api to retrieve initial user/profile information
 *	02/22/24	S Ramachandran		CAP-47198				Added api for save self-registration data for basic profile fields		
 *	02/26/24	Krishna Natarajan	CAP-47337				Added api for validate pattern after user
 *  02/27/24	M Sakthi			CAP-47375				Added api for validate extended profile
 *	02/28/24	L De Leon			CAP-47376				Added validate corporate profile API
 *	02/29/24	Satishkumar A		CAP-47448				Added API for validate password
 *  02/28/24	S Ramachandran		CAP-47410				Integrated save self-regs service method with API controller saveSelfRegUser
 *  03/04/24	M Sakthi			CAP-47617				Added API for attributes
 *	03/02/24    Satishkumar A		CAP-47592				C1UX BE - Create validation story for Password validation
 *  03/01/24	S Ramachandran		CAP-47629				Added API for basic profile validation API
 *  03/05/24	Satishkumar A		CAP-47616				Added API for UDF fields validation
 *  03/06/24	Satishkumar A		CAP-47672				C1UX BE - Create Validation method for UDF fields for Self Registration
 *  04/25/24	Krishna Natarajan	CAP-48805				Changed the getServiceID to return HOMEPAGE_SERVICE_ID instead of ORDERS_SERVICE_ID
 */
package com.rrd.c1ux.api.controllers.selfreg;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationIntialUserRequest;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationPatternAfterResponse;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveRequest;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveResponse;
import com.rrd.c1ux.api.services.selfreg.SelfRegistrationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("SelfRegistrationController")
@Tag(name = "Self Registration API")
public class SelfRegistrationController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SelfRegistrationController.class);

	private final SelfRegistrationService selfRegistrationService;

	protected SelfRegistrationController(TokenReader tokenReader, CPSessionReader sessionReader,
			SelfRegistrationService selfRegistrationService) {
		super(tokenReader, sessionReader);
		this.selfRegistrationService = selfRegistrationService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.HOMEPAGE_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@GetMapping(value = RouteConstants.GET_PATTERN_AFTER_USERS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve Pattern After Users based on Self Registration User in Session")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationPatternAfterResponse> getPatternAfterUsers(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getPatternAfterUsers()");
		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationPatternAfterResponse response = selfRegistrationService.getPatternAfterUsers(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-45908
	@PostMapping(value = RouteConstants.GET_INITIAL_SELF_REG_USER, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Retrieve initial user/profile information for Self Registration")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationPatternAfterResponse> getInitialSelfRegUser(
			@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestBody SelfRegistrationIntialUserRequest request) throws AtWinXSException, IllegalAccessException, InvocationTargetException {

		LOGGER.debug("In getInitialSelfRegUser()");
		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationPatternAfterResponse response = selfRegistrationService.getInitialSelfRegUser(sc, request.getPatternAfterUser());

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-47198
	@PostMapping(value = RouteConstants.SAVE_SELF_REG_USER, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save user/profile information for Self Registration")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationSaveResponse> saveSelfRegUser(
			@RequestHeader(value = "ttsession", required = false) String ttsession, 
			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In saveSelfRegUser()");
		
		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationSaveResponse response = selfRegistrationService.saveSelfRegistration(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-47337
	@PostMapping(value = RouteConstants.VALIDATE_PATTERN_AFTER, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate pattern after user")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationSaveResponse> validatePatternAfter(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException, IllegalAccessException, InvocationTargetException {

		LOGGER.debug("In validatePatternAfter()");
		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationSaveResponse response = selfRegistrationService.validatePatternAfter(sc, request.getSelectedPatternAfter());

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	// CAP-47375
	@PostMapping(value = RouteConstants.VALIDATE_EXTENDED_PROFILE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate extended profile")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationSaveResponse> validateExtendedProfile(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In validateExtendedProfile()");
		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationSaveResponse response=selfRegistrationService.validateExtendedProfile(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-47376
	@PostMapping(value = RouteConstants.VALIDATE_CORPORATE_PROFILE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate Corporate Profile")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationSaveResponse> validateCorporateProfile(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In validateCorporateProfile()");
		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationSaveResponse response = selfRegistrationService.validateCorporateProfile(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-47448 CAP-47592
	@PostMapping(value = RouteConstants.VALIDATE_PASSWORD, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate Corporate Profile")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationSaveResponse> validatePassword(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In validatePassword()");

		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationSaveResponse response=selfRegistrationService.validatePasswordAndPatternAfterUser(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	// CAP-47617
	@PostMapping(value = RouteConstants.VALIDATE_SELF_REG_ATTRIBUTES, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Validate Attributes")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<SelfRegistrationSaveResponse> validateAttributes(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In validateAttributes()");

		SessionContainer sc = getSessionContainerNoService(ttsession);
		SelfRegistrationSaveResponse response = selfRegistrationService.validateAttributes(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-47629
 	@PostMapping(value = RouteConstants.VALIDATE_BASIC_PROFILE, produces = { MediaType.APPLICATION_JSON_VALUE,
 			MediaType.APPLICATION_XML_VALUE })
 	@Operation(summary = "Validate basic profile")
 	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
 	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
 	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
 	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
 	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
 	public ResponseEntity<SelfRegistrationSaveResponse> validateBasicProfile(
 			@RequestHeader(value = "ttsession", required = false) String ttsession,
 			@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException {
 
 		LOGGER.debug("In validateBasicProfile()");
 		SessionContainer sc = getSessionContainerNoService(ttsession);
 		SelfRegistrationSaveResponse response=selfRegistrationService.validateBasicProfile(sc, request);
 		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
 	
 	// CAP-47616 CAP-47672
 	@PostMapping(value = RouteConstants.VALIDATE_USER_DEFINED_FIELDS, produces = { MediaType.APPLICATION_JSON_VALUE,
 	MediaType.APPLICATION_XML_VALUE })
 	@Operation(summary = "Validate user defined fields")
 	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
 	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
 	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
 	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
 	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
 	public ResponseEntity<SelfRegistrationSaveResponse> validateUserDefinedFields(
 	@RequestHeader(value = "ttsession", required = false) String ttsession,
 	@RequestBody SelfRegistrationSaveRequest request) throws AtWinXSException, IllegalAccessException, InvocationTargetException {

 	LOGGER.debug("In validateUserDefinedFields()");
 	SessionContainer sc = getSessionContainerNoService(ttsession);
 	SelfRegistrationSaveResponse response = new SelfRegistrationSaveResponse();
 	response.setSuccess(true);
 	response = selfRegistrationService.validateUserDefinedFields(sc, request, response);
 	return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
 	}
}