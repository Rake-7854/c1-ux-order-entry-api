/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	05/11/23	Alex Salcedo		 CAP-39210	Initial creation
 *	06/20/23	A Salcedo			 CAP-39313	Updated entryPoint validation.
 *	06/23/23	A Salcedo			 CAP-41241	Decrypt entryPoint for CP redirect.
 *	07/19/23	A Salcedo			 CAP-42336  Encode reports entryPoint.
 *	10/23/23	A Salcedo			 CAP-44686	New CP redirect API w params.
 */
package com.rrd.c1ux.api.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gwt.dom.client.TagName;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.CPRedirectResponse;
import com.rrd.c1ux.api.services.redirect.saml.RedirectService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController("CPRedirectController")
@TagName("CP Redirect APIs")
public class CPRedirectController extends BaseCPApiController 
{
	private RedirectService redirectService;
	
	private static final Logger logger = LoggerFactory.getLogger(CPRedirectController.class);
	
	protected CPRedirectController(TokenReader tokenReader, CPSessionReader sessionReader, RedirectService mRedirectService) {
		super(tokenReader, sessionReader);
		redirectService = mRedirectService;
	}
	
	@Override
	protected int getServiceID() 
	{
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
	
	/**
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 */
	@GetMapping(value = RouteConstants.CP_REDIRECT, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Base CP Redirect")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CPRedirectResponse>  redirectToCP(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException 
	{
		logger.debug("Redirecting to CustomPoint");

		SessionContainer sc = getSessionContainer(ttsession);
		
		if(!sc.getApplicationSession().getAppSessionBean().isC1uxSession() || !checkAdminRedirectAllowed(sc)) 
		{
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		CPRedirectResponse response = redirectService.getSamlUrl(sc.getApplicationSession().getAppSessionBean(), AtWinXSConstant.EMPTY_STRING);
		
		String debugString = "About to redirect to CustomPoint: " + response.getRedirectURL();
		logger.debug(debugString);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));

	}
	
	/**
	 * @param entryPoint
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 */
	@GetMapping(value = RouteConstants.CP_REDIRECT_EP, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "EntryPoint CP Redirect")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CPRedirectResponse>  redirectToCP(@PathVariable (value= "entryPoint", required = false) String entryPoint, @RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException 
	{
		logger.debug("Redirecting to CustomPoint EntryPoint");

		SessionContainer sc = getSessionContainer(ttsession);
		
		if(!sc.getApplicationSession().getAppSessionBean().isC1uxSession()) 
		{
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		//CAP-41242
		if(!Util.isBlankOrNull(entryPoint))
		{
			try
			{
				entryPoint = Util.decryptString(entryPoint);//CAP-42336
			}
			catch(Exception ex)
			{
				throw new AccessForbiddenException(this.getClass().getName());
			}
		}	
		
		//CAP-39313
		CPRedirectResponse response = redirectService.getSamlUrl(sc.getApplicationSession().getAppSessionBean(), checkEntryPointAccess(sc.getApplicationSession().getAppSessionBean(), entryPoint));
		
		String debugString = "About to redirect to CustomPoint with EntryPoint: " + response.getRedirectURL();
		logger.debug(debugString);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));

	}
	
	//CAP-44686
	/**
	 * @param ttsessionid
	 * @param response
	 * @return
	 * @throws AtWinXSException
	 */
	@GetMapping(value = RouteConstants.CP_REDIRECT_PARAMS, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Param CP Redirect")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CPRedirectResponse>  redirectionToCP(@RequestParam (value= "a", required = false) String encryptedParms, @RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException 
	{
		logger.debug("Redirecting to CustomPoint EntryPoint with params");

		SessionContainer sc = getSessionContainer(ttsession);
		
		if(!sc.getApplicationSession().getAppSessionBean().isC1uxSession()) 
		{
			throw new AccessForbiddenException(this.getClass().getName());
		}
		
		Map<String, String> parameters = new HashMap<>();
		
		//Get any encrypted parameters here
		try
		{
			if (!Util.isBlankOrNull(encryptedParms))
		    {
				//decodeURL to replace %
			    encryptedParms = Util.decodeURL(encryptedParms);
		    	encryptedParms = Util.replace(encryptedParms, " ", "+");
		    	String decryptedParms = Util.decryptString(encryptedParms);    	        	    
		    
		    	// Get all the parameters for the action here
		    	parameters = Util.parseQueryString(decryptedParms);
		    }
		}
		catch(Exception ex)
		{
			//Throw an unknown error
			String errorMsg = "Invalid Parameters for CPRedirect: " + 
				ex.getMessage();
			logger.error(errorMsg);
			
			//Throw new exception
			throw new AtWinXSException(errorMsg, this.getClass().getName());
		}
		
		//CAP-39313
		CPRedirectResponse response = redirectService.getSamlUrl(sc.getApplicationSession().getAppSessionBean(), parameters);
		
		String debugString = "About to redirect to CustomPoint with EntryPoint: " + response.getRedirectURL();
		logger.debug(debugString);
		
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));

	}
	
	
	//CAP-39313
	private String checkEntryPointAccess(AppSessionBean asb, String entryPoint)
	{
		return asb.hasService(AtWinXSConstant.REPORTS_SERVICE_ID) ? entryPoint : AtWinXSConstant.EMPTY_STRING;
	}
}
