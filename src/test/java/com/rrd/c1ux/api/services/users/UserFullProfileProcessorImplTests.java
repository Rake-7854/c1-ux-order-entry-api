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
 *  02/01/24    Sakthi M		  	CAP-46634   Initial Creation, Get site attributes for the user
 */
package com.rrd.c1ux.api.services.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.users.C1UXProfileImpl;
import com.rrd.custompoint.admin.entity.SiteAttributeValue;
import com.rrd.custompoint.admin.entity.SiteAttributeValueImpl;
import com.rrd.custompoint.admin.entity.SiteAttributeValues;
import com.rrd.custompoint.admin.profile.entity.Profile;
import com.rrd.custompoint.admin.profile.entity.ReferenceFieldDefinition.DisplayType;
import com.rrd.custompoint.admin.profile.entity.SiteAttribute;
import com.rrd.custompoint.admin.profile.entity.SiteAttributeImpl;
import com.rrd.custompoint.admin.profile.entity.SiteAttributes;

class UserFullProfileProcessorImplTests extends BaseServiceTest{

	@InjectMocks
	private UserFullProfileProcessorImpl serviceToTest;
	
	@Mock
	Profile mockProfile;
	
	@Mock
	private Properties mockProperties;
	
	@Mock
	private Map<String, String> mockTranslationMap;
	
	@Mock
	SiteAttributes mockSiteAttributes;
	
	@Mock
	List<SiteAttribute> mockAttrs;
	
	@Mock
	List<SiteAttributeValue> mockSiteAttrsValue;
	
	@Mock
	SiteAttributeImpl mockSiteAttrs;
	
	@Mock
	DisplayType mockDisplayType;
	
	@Mock
	SiteAttributeValues mockSiteAttributeValues;
	
	
	@BeforeEach
	public void setUp() throws Exception {
	}
	
	@Test
	void that_get_user_full_profile_returns_success() throws Exception {
		C1UXProfileImpl c1uxprofile=new C1UXProfileImpl();
		List<SiteAttribute> siteAttrs=new ArrayList<>();
		siteAttrs.add(mockSiteAttrs); 
		
		List<SiteAttributeValue> siteAttrs1=new ArrayList<>();
		SiteAttributeValueImpl  stAtt=new SiteAttributeValueImpl();
		stAtt.setSiteAttrValID(1);
		stAtt.setSiteAttrValDesc("Test color");
		stAtt.setAssigned(false);
		siteAttrs1.add(stAtt);
		
		when(mockProfile.getSiteAttributes()).thenReturn(mockSiteAttributes);
		when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttrs);
		when(mockSiteAttrs.getAttrID()).thenReturn(1);
		when(mockSiteAttrs.getAttrDisplayName()).thenReturn("Test Attr Display Name");
		when(mockSiteAttrs.getDisplayType()).thenReturn(mockDisplayType);
		when(mockDisplayType.name()).thenReturn("Editable");
		when(mockSiteAttrs.getMinRequired()).thenReturn(1);
		when(mockSiteAttrs.getMaxRequired()).thenReturn(0);
		when(mockSiteAttrs.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues); 
		when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttrs1);
		when(mockSiteAttributeValues.getAssignedSiteAttributeValues()).thenReturn(siteAttrs1);
		serviceToTest.getSiteAttributes(mockProfile, c1uxprofile);
		assertEquals(1, c1uxprofile.getSelfAdminSiteAttributes().get(0).getAttributeID());
	}	
	
	
	@Test
	void that_get_user_full_profile_returns_editable_success() throws Exception {
		C1UXProfileImpl c1uxprofile=new C1UXProfileImpl();
		List<SiteAttribute> siteAttrs=new ArrayList<>();
		siteAttrs.add(mockSiteAttrs); 
		
		List<SiteAttributeValue> siteAttrs1=new ArrayList<>();
		SiteAttributeValueImpl  stAtt=new SiteAttributeValueImpl();
		stAtt.setSiteAttrValID(1);
		stAtt.setSiteAttrValDesc("Test color");
		stAtt.setAssigned(true);
		siteAttrs1.add(stAtt);
		
		when(mockProfile.getSiteAttributes()).thenReturn(mockSiteAttributes);
		when(mockSiteAttributes.getSiteAttrs()).thenReturn(siteAttrs);
		when(mockSiteAttrs.getAttrID()).thenReturn(1);
		when(mockSiteAttrs.getAttrDisplayName()).thenReturn("Test Attr Display Name");
		when(mockSiteAttrs.getDisplayType()).thenReturn(mockDisplayType);
		when(mockDisplayType.name()).thenReturn("EditableRequired");
		when(mockSiteAttrs.getMinRequired()).thenReturn(1);
		when(mockSiteAttrs.getMaxRequired()).thenReturn(0);
		when(mockSiteAttrs.getSiteAttributeValuesForProfile()).thenReturn(mockSiteAttributeValues); 
		when(mockSiteAttributeValues.getSiteAttributeValues()).thenReturn(siteAttrs1);
		when(mockSiteAttributeValues.getAssignedSiteAttributeValues()).thenReturn(siteAttrs1);
		serviceToTest.getSiteAttributes(mockProfile, c1uxprofile);
		assertEquals(1, c1uxprofile.getSelfAdminSiteAttributes().get(0).getAttributeID());
	}	
	
	

}
