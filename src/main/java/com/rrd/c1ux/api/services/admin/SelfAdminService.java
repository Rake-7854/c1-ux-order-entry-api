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
 *	03/29/23	A Boomker		   CAP-39510		Initial Version
 *  03/31/23    M Sakthi           CAP-39581 		API Build - Get Password Requirements
 *	04/03/23	S Ramachandran		CAP-39610		Get Profile Definition of User Type in Self Admin
 * 	04/10/23	A Boomker			CAP-37890		Adding handling for update basic profile API
 *  07/13/23	S Ramachandran		CAP-42258		Added methods & constant for update extended Personal Profile
 * 	07/18/23	N Caceres			CAP-42342		Add handling for Update Company Profile API
 *  08/09/23    M Sakthi			CAP-42562       C1UX BE - Self Admin – Update Profile User Defined Fields (API Build)
 *	09/07/23	L De Leon			CAP-43631		Added deletePABAddress() method
 *  09/07/23	S Ramachandran		CAP-43630		Added service to return PAB entries - all or a search
 *  09/08/23    M Sakthi            CAP-43598       Added savePABAddress() method 
 *	10/06/23	L De Leon			CAP-44422		Added exportPABAddresses() method
 *	11/07/23	S Ramachandran		CAP-44961 		Added usps validation flag for save PAB and to show suggested address
 *	12/18/23	M Sakthi			CAP-45544		Added useOriginator flag for save/search/edit PAB
 *	01/29/24	S Ramachandran		CAP-42258		C1UX BE - save site attribute information for a user
 */
package com.rrd.c1ux.api.services.admin;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.rrd.c1ux.api.models.admin.C1UserDefinedField;
import com.rrd.c1ux.api.models.admin.C1UserSiteAttribute;
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
import com.rrd.custompoint.admin.profile.entity.ProfileDefinition;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SelfAdminService {

	public static final int SELF_ADMIN_MAX_SIZE_EXT_TITLE = 10;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_MID_INIT = 1;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_SUFFIX = 15;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_FAX = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_PAGER = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_MOBILE = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_TOLL_FREE = 24;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_WEB_URL = 255;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_IMG_URL = 255;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_NAME2 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_LINE1 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_LINE2 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_LINE3 = 35;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_CITY = 30;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_STATE_CD = 4;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_STATE = 40;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_ZIP = 12;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_COUNTRY_CD = 3;
	public static final int SELF_ADMIN_MAX_SIZE_EXT_COUNTRY = 30;

	public static final String EXTENDED_AS_PREFFERED_ADDR_CODE="E";
	
	public UpdatePasswordResponse updatePassword(UpdatePasswordRequest request, SessionContainer sc) throws AtWinXSException;
	public CORetrievePasswordRuleResponse getPasswordRule(SessionContainer sc) throws AtWinXSException;
	public ProfileDefinitionResponse getProfileDefinition(SessionContainer sc) throws AtWinXSException;
	public UpdateBasicProfileResponse updateBasicProfile(UpdateBasicProfileRequest updateProfileRequest,
			SessionContainer sc) throws AtWinXSException;
	public boolean validateBasicProfile(UpdateBasicProfileResponse response, UpdateBasicProfileRequest request,
			AppSessionBean asb, ProfileDefinition def) throws AtWinXSException;

	public UpdateExtendedProfileResponse updateExtendedProfile(UpdateExtendedProfileRequest updateProfileRequest,
			SessionContainer sc) throws AtWinXSException;
	
	public boolean validateExtendedProfile(UpdateExtendedProfileResponse response, UpdateExtendedProfileRequest request,
			AppSessionBean asb, ProfileDefinition def, String countryExist, String stateExist) throws AtWinXSException;
	
 	public UpdateCompanyProfileResponse updateCompanyProfile(UpdateCompanyProfileRequest request, SessionContainer sc) throws AtWinXSException;
	public boolean validateCompanyProfile(UpdateCompanyProfileResponse response, UpdateCompanyProfileRequest request,
			AppSessionBean asb, ProfileDefinition def, String countryExist, String stateExist) throws AtWinXSException;
	public UserDefinedfieldsResponse updateUserDefinedFields(UserDefinedFieldsRequest updateUserDefinedFieldsRequest,SessionContainer sc) throws AtWinXSException;
	// CAP-43631
	public PABDeleteResponse deletePABAddress(PABDeleteRequest request, SessionContainer sc,boolean useOriginator) throws AtWinXSException;
	
	public PABSearchResponse searchPAB(SessionContainer sc, PABSearchRequest pabRequest,boolean useOriginator) 
			throws AtWinXSException;
	//CAP-43598, CAP-44961 
	public PABSaveResponse savePABAddress(SessionContainer sc, PABSaveRequest address, boolean uspsValidationFlag,boolean useOriginator) throws AtWinXSException;

	// CAP-44422
	public PABExportResponse exportPABAddresses(SessionContainer sc, HttpServletResponse httpServletResponse,boolean useOriginator) throws AtWinXSException;
	
	//CAP-44387
	public PABImportResponse importAllPABAddresses(SessionContainer sc, PABImportRequest importRequest,boolean useOriginator) throws AtWinXSException,CPRPCException;
	
	//CAP-42258
	public C1UserSiteAttributesResponse saveUserSiteAttributes(C1UserSiteAttributesRequest c1userSiteAttributesRequest, SessionContainer sc) 
			throws AtWinXSException;
	
	//CAP-47657
	public boolean validateSiteAttrIDAndAttrValueID(SiteAttributes siteAttributesForProfileLst,List<C1UserSiteAttribute> c1UserSiteAttributesLst, 
			List<C1UserSiteAttribute> editableSiteAttributesLst) throws AtWinXSException;
	
	public boolean validateSiteAttrValueID(SiteAttribute siteAttribute,	C1UserSiteAttribute c1UserSiteAttribute) throws AtWinXSException;
	
	public Collection<String>  validateSiteAttributeMinMax(AppSessionBean asb , SiteAttributes siteAttributes, 
			List<C1UserSiteAttribute> c1UserSiteAttributesLst) throws AtWinXSException;
	
	public void  validateMinMax(AppSessionBean asb , SiteAttribute siteAttribute, 
			C1UserSiteAttribute c1UserSiteAttribute, int attrIDNew, Collection<String> errMsgs) throws AtWinXSException;
	//CAP-47915
	public boolean validateUserDefinedFields(List<UserDefinedField> udfList, List<C1UserDefinedField> c1UserDefinedFields,UserDefinedfieldsResponse udfResponse,AppSessionBean asb) throws AtWinXSException;
}
