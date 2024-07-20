
/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		     DTS#		Description
 * 	--------	-----------		     ----------	-----------------------------------------------------------
 *	10/24/22	Sakthi				 CAP-36418	Initial creation-create keepAlive API call to help front end 
 *												implement session timeout - update session timestamp.
 *  11/10/22    M Sakthi             CAP-37029  create timeout API call to help front end implement session timeout - update session status on table
 *  03/14/23    C Porter             CAP-37146  Spring Session
 *	
 */
package com.rrd.c1ux.api.controllers.sessionlive;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.catalogitems.CacheResponse;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.tt.arch.TTException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController("SessionLiveController")
public class SessionLiveController extends BaseCPApiController{

	protected SessionLiveController(TokenReader tokenReader, CPSessionReader sessionReader) {
		super(tokenReader, sessionReader);
	}

	  @Override
	    protected int getServiceID() {
			return AtWinXSConstant.APPSESSIONSERVICEID;
	    }
	
   //CAP-36418	Initial creation-create keepAlive API call to help front end implement session timeout - update session timestamp	
	@PostMapping(value=RouteConstants.KEEP_SESSION_ALIVE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	   @Tag(name = "/api/sessionlive/sessionlive")
	    @Operation(
	        summary = "Update the session last update time in the table")
	    public CacheResponse keepAlive(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid) throws AtWinXSException, TTException{
	        SessionContainer sc = getSessionContainer(ttsessionid);
	       	CacheResponse response=new CacheResponse();
	       	if(mSessionReader.updateTTSession(sc)) {
       	    	response.setSuccess("Y");
       	    }
       	    else {
       	    	response.setSuccess("N");
       	    }
	        return response;
	    }
	
	//CAP-37029  create timeout API call to help front end implement session timeout - update session status on table 
	@PostMapping(value=RouteConstants.SESSION_TIMEOUT_STATUS_UPDATE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	   @Tag(name = "/api/timeoutSession")
	    @Operation(
	        summary = "Update the session timeout status in the table")
	    public CacheResponse timeoutSession(@RequestHeader(value = "ttsessionid", required=false) String ttsessionid) throws AtWinXSException, TTException{
	        SessionContainer sc = getSessionContainer(ttsessionid);
	       	CacheResponse response=new CacheResponse();
	       	if(mSessionReader.ttSessionTimeout(sc)) {
    	    	response.setSuccess("Y");
    	    }
    	    else {
    	    	response.setSuccess("N");
    	    }
	        return response;
	    }
}




