/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By			DTS#		Description
 *	--------	-----------			----------	-----------------------------------------------------------
 *  05/05/22    S Ramachandran  	CAP-34048   Initial Creation, Get User Full Profile 
 *  05/11/23    Sakthi M        	CAP-40524	C1UX BE - API Change - Convert Full Profile API to only return the user's own information
 *  07/26/23	Krishna Natarajan	CAP-42465	Added a line of code to include the Get Profile Definition - Profile UDF definition (updated UserDefinedFields on 07/27)
 *  12/07/23    S Ramachandran  	CAP-45485   Fix code to only search/use originator profile when doing self administration
 *  02/01/24	Sakthi M			CAP-46634	C1UX BE - Modify /api/users/get-fullprofileoforiginator method to return Attributes for Profile
 */

package com.rrd.c1ux.api.services.users;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.users.C1UXProfile;
import com.rrd.c1ux.api.models.users.C1UXProfileImpl;
import com.rrd.c1ux.api.models.users.SelfAdminSiteAttributeValues;
import com.rrd.c1ux.api.models.users.SelfAdminSiteAttributes;
import com.rrd.c1ux.api.models.users.UserFullProfileResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.admin.entity.SiteAttributeValue;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.ProfileUDFDefinition;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;
import com.rrd.custompoint.admin.profile.entity.UserDefinedField;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@Service
public class UserFullProfileProcessorImpl extends BaseService implements UserFullProfileProcessor {
	
	protected UserFullProfileProcessorImpl(TranslationService translationService) {
		super(translationService);
	}

	private static final Logger logger = LoggerFactory.getLogger(UserFullProfileProcessorImpl.class);

	public UserFullProfileResponse processUserFullProfile(SessionContainer mainSession, boolean useOriginatorProfile) 
			throws AtWinXSException, IllegalAccessException, InvocationTargetException {
		
		AppSessionBean appSessionBean = mainSession.getApplicationSession().getAppSessionBean();
		//CAP-45485
		boolean requestorModeModified = SelfAdminUtil.modifyOriginatorProfileInSelfAdmin(useOriginatorProfile, appSessionBean);
		
		UserFullProfileResponse response = new UserFullProfileResponse();
		
				// populate Profile Info
				Profile profile = objectMapFactoryService.getEntityObjectMap().getEntity(Profile.class, appSessionBean.getCustomToken());
			
				if (appSessionBean.getProfileNumber() == AtWinXSConstant.INVALID_PROFILE_NUMBER)
				{
					logger.error(getErrorPrefix(appSessionBean), "User does not have a profile number");
					throw new AccessForbiddenException(UserFullProfileProcessorImpl.class.getName());
				}	
				try 
				{
					profile.populate(appSessionBean.getProfileNumber());
					profile.getProfileDefinition().getCustFieldsProfielUDFDefinitions();//CAP-42465 added to include the Profile UDF definition
					//CAP-42465 added to include the Profile UDF definition
					Collection<ProfileUDFDefinition> profileUDFDefinitions = profile.getProfileDefinition().getCustFieldsProfielUDFDefinitions().getProfileUDFDefinitions();
					List<UserDefinedField> udfList = profile.getUserDefinedFields().getUserDefinedFields();
					for (UserDefinedField udf : udfList) {
						for (ProfileUDFDefinition udfDef : profileUDFDefinitions) {
							if (udfDef.getProfileDefinitionID() == udf.getProfileUDFDefinition()
									.getProfileDefinitionID() && udfDef.getUdfFieldNumber() == udf.getUdfFieldNumber()) {
								udf.getProfileUDFDefinition().setGroupName(udfDef.getGroupName());
								udf.getProfileUDFDefinition().setGroupId(udfDef.getGroupId());
								udf.getProfileUDFDefinition().setDisplaySeq(udfDef.getDisplaySeq());
							}
						}
					}
				} 
				catch (AtWinXSException e) 
				{
					logger.error(e.getMessage());
				}
				
				processFullProfillInfo(profile, response);
				
		//CAP-45485
		SelfAdminUtil.revertOriginatorProfileInSelfAdmin(appSessionBean, requestorModeModified);		
		return response;
	}
	
	private void processFullProfillInfo(Profile profile, UserFullProfileResponse response) throws IllegalAccessException, InvocationTargetException, AtWinXSException
	{
		C1UXProfile c1uxProfile = new C1UXProfileImpl();
		getSiteAttributes(profile,(C1UXProfileImpl) c1uxProfile);//CAP-46634
		BeanUtils.copyProperties(c1uxProfile, profile);
		response.setSuccess(true);
		response.setC1uxProfile(c1uxProfile);
		
	}

	//CAP-46634
	public void getSiteAttributes(Profile profile, C1UXProfileImpl c1uxProfile) throws AtWinXSException {
			
			SiteAttributes siteAttr = profile.getSiteAttributes();
			List<SiteAttribute> siteAttrs = siteAttr.getSiteAttrs();
			List<SelfAdminSiteAttributes> selfAdminSiteAttrList = new ArrayList<>();
			for (SiteAttribute siteAtt : siteAttrs) {
				SelfAdminSiteAttributes selfAdminSiteAttr = new SelfAdminSiteAttributes();
				List<SelfAdminSiteAttributeValues> availableAttrValue = new ArrayList<>();
				List<SelfAdminSiteAttributeValues> assignedAttrValue = new ArrayList<>();

				if (siteAtt.getDisplayType().name().equalsIgnoreCase(ModelConstants.ATTR_DISP_TYPE_EDITABLE)
						|| siteAtt.getDisplayType().name().equalsIgnoreCase(ModelConstants.ATTR_DISP_TYPE_EDITABLEREQ)
						|| siteAtt.getDisplayType().name().equalsIgnoreCase(ModelConstants.ATTR_DISP_TYPE_VIEWONLY)) {

					selfAdminSiteAttr.setAttributeID(siteAtt.getAttrID());
					selfAdminSiteAttr.setAttributeDisplayName(siteAtt.getAttrDisplayName());
					selfAdminSiteAttr.setMinAttributeValues(siteAtt.getMinRequired());
					selfAdminSiteAttr.setMaxAttributeValues(siteAtt.getMaxRequired());
					selfAdminSiteAttr.setViewOnly(getViewOnlyCheck(siteAtt.getDisplayType().name()));
					selfAdminSiteAttr.setDisplayType(siteAtt.getDisplayType().name());

					selfAdminSiteAttr.setAvailableAttributes(getAvailableSiteAttributes(siteAtt, availableAttrValue));
					selfAdminSiteAttr.setAssignedAttributes(getAssignedSiteAttributes(siteAtt, assignedAttrValue));
					selfAdminSiteAttrList.add(selfAdminSiteAttr);
				}

			}
			c1uxProfile.setSelfAdminSiteAttributes(selfAdminSiteAttrList);
	}
	
	public boolean getViewOnlyCheck(String displayName) {
		boolean viewOnlyCheck;
		if(displayName.equalsIgnoreCase(ModelConstants.ATTR_DISP_TYPE_VIEWONLY)) {
			viewOnlyCheck= true;
		 }else {
			 viewOnlyCheck=false;
		 } 
		return viewOnlyCheck;
	}
	
	public List<SelfAdminSiteAttributeValues> getAvailableSiteAttributes(SiteAttribute siteAtt,List<SelfAdminSiteAttributeValues> availableAttrValue) throws AtWinXSException{
		
		 for(SiteAttributeValue stValue:siteAtt.getSiteAttributeValuesForProfile().getSiteAttributeValues()) {
				if(!stValue.isAssigned()) {
					 SelfAdminSiteAttributeValues attrValue=new SelfAdminSiteAttributeValues();
					 attrValue.setAttributeValueID(stValue.getSiteAttrValID());
					 attrValue.setAttribtueValueDescription(stValue.getSiteAttrValDesc());
					 availableAttrValue.add(attrValue);
				} 
			 }
		
		return availableAttrValue;
	}
	
	
	public List<SelfAdminSiteAttributeValues> getAssignedSiteAttributes(SiteAttribute siteAtt,List<SelfAdminSiteAttributeValues> assignedAttrValue) throws AtWinXSException{
		
		 for(SiteAttributeValue stValue:siteAtt.getSiteAttributeValuesForProfile().getAssignedSiteAttributeValues()) {
					 SelfAdminSiteAttributeValues attrValue=new SelfAdminSiteAttributeValues();
					 attrValue.setAttributeValueID(stValue.getSiteAttrValID());
					 attrValue.setAttribtueValueDescription(stValue.getSiteAttrValDesc());
					 assignedAttrValue.add(attrValue);
		 }
		return assignedAttrValue;
	}
}