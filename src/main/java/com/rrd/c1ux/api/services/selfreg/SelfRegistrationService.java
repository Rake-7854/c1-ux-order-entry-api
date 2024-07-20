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
 *	02/26/24	Krishna Natarajan	CAP-47337				Added a new method to validate pattern after user
 *	02/28/24	M Sakthi			CAP-47450				C1UX BE - Create API to validate the extended profile fields
 *	02/29/24	L De Leon			CAP-47516				Added validateCorporateProfile() method
 *	02/28/24	S Ramachandran		CAP-47410				Added service method to save self-registration info for basic profile fields
 *	03/02/24    Satishkumar A		CAP-47592				C1UX BE - Create validation story for Password validation
 *	03/01/24	S Ramachandran		CAP-47629				Added service method to validate basic profile fields 
 *	03/05/24	Satishkumar A		CAP-47616				Added service method to validate the User Defined Fields
 *	03/06/24	Satishkumar A		CAP-47672				C1UX BE - Create Validation method for UDF fields for Self Registration
 *	03/07/24	M Sakthi			CAP-47657				C1UX BE - Create Validation method for Attributes for Self Registration
 */
package com.rrd.c1ux.api.services.selfreg;

import java.lang.reflect.InvocationTargetException;

import com.rrd.c1ux.api.models.selfreg.SelfRegistrationPatternAfterResponse;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveRequest;
import com.rrd.c1ux.api.models.selfreg.SelfRegistrationSaveResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SelfRegistrationService {

	public SelfRegistrationPatternAfterResponse getPatternAfterUsers(SessionContainer sc) throws AtWinXSException;

	SelfRegistrationPatternAfterResponse getInitialSelfRegUser(SessionContainer sc, String patternAfterUser) throws AtWinXSException, IllegalAccessException, InvocationTargetException;
	
	SelfRegistrationSaveResponse validatePatternAfter(SessionContainer sc, String patternAfterUser) throws AtWinXSException, IllegalAccessException, InvocationTargetException;
	
	SelfRegistrationSaveResponse validateExtendedProfile(SessionContainer sc, SelfRegistrationSaveRequest request) throws AtWinXSException;

	SelfRegistrationSaveResponse validateCorporateProfile(SessionContainer sc, SelfRegistrationSaveRequest request)
			throws AtWinXSException;
	
	public SelfRegistrationSaveResponse saveSelfRegistration(SessionContainer sc, 
		SelfRegistrationSaveRequest selfRegistrationSaveRequest) throws AtWinXSException;

	// CAP-47592
	public SelfRegistrationSaveResponse validatePasswordAndPatternAfterUser(SessionContainer sc, SelfRegistrationSaveRequest request)
			throws AtWinXSException;
	
	//CAP-47629
	SelfRegistrationSaveResponse validateBasicProfile(SessionContainer sc, SelfRegistrationSaveRequest request) 
			throws AtWinXSException;

	//CAP-47616 CAP-47672
	SelfRegistrationSaveResponse validateUserDefinedFields(SessionContainer sc, SelfRegistrationSaveRequest request, SelfRegistrationSaveResponse response) throws AtWinXSException, IllegalAccessException, InvocationTargetException;
	
	//CAP-47657
	public SelfRegistrationSaveResponse validateAttributes(SessionContainer sc,SelfRegistrationSaveRequest selfRegistrationSaveRequest) throws AtWinXSException;
	
}
