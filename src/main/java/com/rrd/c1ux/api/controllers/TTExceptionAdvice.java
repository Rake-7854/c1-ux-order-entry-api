/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *  Date        Modified By     Issue #     Description
 *  --------    -----------     ----------  -----------------------------------------------------------
 *  11/09/22	C Porter 		CAP-37006	Exception handling changes to split logging exceptions into specific usable files
 *  02/24/23    C Porter        CAP-38897   HTTP 403 response handling  
 *  03/22/23    C Porter        CAP-39295   Handle "Could not load session" Exception
 * 
 */

package com.rrd.c1ux.api.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.exceptions.AtWinXSSessionException;
import com.rrd.c1ux.api.exceptions.BadRequestException;
import com.rrd.c1ux.api.models.ExceptionResponse;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@ControllerAdvice
public class TTExceptionAdvice {
	
	private static final String EXCEPTION_ADVICE_CAUGHT = "exceptionAdvice caught: ";
	
	private static final Logger UNHANDLED_EXCEPTION_LOGGER = LoggerFactory.getLogger("unhandledException");
	private static final Logger BAD_REQUEST_LOGGER = LoggerFactory.getLogger("badRequest");
	private static final Logger AUTH_FAILURE_LOGGER = LoggerFactory.getLogger("authFailure");
	
	static final String MSG_NO_SESSION = "Session not found or session expired.";
	
    @ResponseBody
    @ExceptionHandler(AtWinXSSessionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleAtWinXsSessionExceptions(AtWinXSSessionException ex) {
    	
      AUTH_FAILURE_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
      return new ExceptionResponse(MSG_NO_SESSION, MSG_NO_SESSION);
    }

    @ResponseBody
    @ExceptionHandler(AtWinXSException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleAtWinXsExceptions(AtWinXSException ex) {
    	
      BAD_REQUEST_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
      return new ExceptionResponse(ex.getMessage(), ex.getMessageExt());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionResponse handleValidationExceptions(
        MethodArgumentNotValidException ex
    ) {
        BAD_REQUEST_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        });
        return new ExceptionResponse(ex.getMessage(), errors.toString());
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleBadRequestExceptions(BadRequestException ex) {
      BAD_REQUEST_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
      return new ExceptionResponse(ex.getMessage(), ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    	
        BAD_REQUEST_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
        return new ExceptionResponse(ex.getMessage(), ex.getMessage());
    }
    
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessForbiddenException.class)
    public ExceptionResponse handleAccessForbiddenException(AccessForbiddenException ex) {
      AUTH_FAILURE_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
      return new ExceptionResponse(ex.getMessage(), ex.getMessage());
    }
    
    /*
     * catch anything that does not match the other handlers
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {
    	
    	UNHANDLED_EXCEPTION_LOGGER.error(EXCEPTION_ADVICE_CAUGHT, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        		.body(new ExceptionResponse(ex.getMessage(), ex.getMessage()));
    	
    }
}
