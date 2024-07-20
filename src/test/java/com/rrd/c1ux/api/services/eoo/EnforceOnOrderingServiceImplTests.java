/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/01/24	Satishkumar A		CAP-46675				C1UX BE - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 */
package com.rrd.c1ux.api.services.eoo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import com.rrd.c1ux.api.BaseServiceTest;
import com.rrd.c1ux.api.models.eoo.ValidateCheckoutResponse;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.admin.vo.SiteAttributesVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchExtendedVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.ao.DynamicItemAttributeVO;
import com.wallace.atwinxs.orderentry.ao.EnforceOrderFormBean;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;

@WithMockUser
class EnforceOnOrderingServiceImplTests  extends BaseServiceTest {

	@InjectMocks
	private EnforceOnOrderingServiceImpl serviceToTest;
	
	@Mock
	private HttpServletRequest mockHttpServletRequest;
	
	@Mock
	private EnforceOrderFormBean mockEnforceOrderFormBean;
	
	@BeforeEach
	public void setup() throws AtWinXSException {

	}
	
	@Test
	void that_validateCheckout_return_success() throws Exception {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);

		when(mockOESessionBean.isEOOSelectionCompleted()).thenReturn(true);
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
		
		List<DynamicItemAttributeVO> temp = new ArrayList<>();
		UserGroupSearchVO searchOptions = new UserGroupSearchVO(0, 0, ENCRYPTED_SESSION_ID, 0, CURRENT_CART_ERR_FRENCH, CURRENT_CART_ERR_ENGLISH, false, false, 0, BLANK_NOT_ALLOWED_ERR_FRENCH, 0, BLANK_NOT_ALLOWED_ERR_ENGLISH, null, false);
		DynamicItemAttributeVO attributeVO = new DynamicItemAttributeVO(searchOptions, temp, temp, false);
		temp.add(attributeVO);
		ValidateCheckoutResponse response = new ValidateCheckoutResponse();

		response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());
		
		when(mockEnforceOrderFormBean.getSiteAttr()).thenReturn(null);
		when(mockOESessionBean.isEOOSelectionCompleted()).thenReturn(false);
		when(mockHttpServletRequest.getAttribute(any())).thenReturn(mockEnforceOrderFormBean);
		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					
				})) {

		response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());

		}

		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(false);
		response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.isSuccess());

	}
	
	@Test
	void that_validateCheckout_return_422() throws Exception {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUsrSrchOptions()).thenReturn(null);
		when(mockOESessionBean.getPrflBasedAttrValues()).thenReturn(getPrflBasedAttrValues());
		
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
		when(mockAppSessionBean.hasEnforceOnCatalog()).thenReturn(true);
		when(mockAppSessionBean.getProfileAttributes()).thenReturn(getProfileAttributes());

		when(mockVolatileSessionBean.getFamilyMemberSiteAttrIDs()).thenReturn(getFamilyMemberSiteAttrIDs());

		
		when(mockHttpServletRequest.getAttribute(any())).thenReturn(mockEnforceOrderFormBean);
		when(mockEnforceOrderFormBean.getSiteAttr()).thenReturn(getSiteAttrArray());
		when(mockEnforceOrderFormBean.getSelectedSiteAttributeValueMap()).thenReturn(getSelectedSiteAttributeValue());
		when(mockEnforceOrderFormBean.getSiteAttributeValueMap()).thenReturn(getSiteAttributeValue());

		
		List<DynamicItemAttributeVO> temp = new ArrayList<>();
		UserGroupSearchVO searchOptions = new UserGroupSearchVO(0, 0, ENCRYPTED_SESSION_ID, 0, CURRENT_CART_ERR_FRENCH, CURRENT_CART_ERR_ENGLISH, false, false, 0, BLANK_NOT_ALLOWED_ERR_FRENCH, 0, BLANK_NOT_ALLOWED_ERR_ENGLISH, null, false);
		DynamicItemAttributeVO attributeVO = new DynamicItemAttributeVO(searchOptions, temp, temp, false);
		temp.add(attributeVO);
		ValidateCheckoutResponse response = new ValidateCheckoutResponse();

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {

			response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
			
			when(mockVolatileSessionBean.getFamilyMemberSiteAttrIDs()).thenReturn(getFamilyMemberSiteAttrIDs_invalid());
			when(mockEnforceOrderFormBean.getSiteAttr()).thenReturn(getSiteAttrArray());
			when(mockOESessionBean.getUsrSrchOptions()).thenReturn( getSearchOptions());
			response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());

		}
	}
	
	@Test
	void that_validateCheckout_return_422_enforce_form_bean_null() throws Exception {
		
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockSessionContainer.getApplicationVolatileSession()).thenReturn(mockApplicationVolatileSession);
		when(mockApplicationVolatileSession.getVolatileSessionBean()).thenReturn(mockVolatileSessionBean);
		when(mockSessionContainer.getModuleSession()).thenReturn(mockOESession);
		when(mockOESession.getOESessionBean()).thenReturn(mockOESessionBean);
		when(mockOESessionBean.getUsrSrchOptions()).thenReturn(null);
		when(mockOESessionBean.getPrflBasedAttrValues()).thenReturn(getPrflBasedAttrValues());
		
		when(mockAppSessionBean.hasEnforceOnOrdering()).thenReturn(true);
		when(mockAppSessionBean.hasEnforceOnCatalog()).thenReturn(true);
		when(mockAppSessionBean.getProfileAttributes()).thenReturn(getProfileAttributes());

		when(mockVolatileSessionBean.getFamilyMemberSiteAttrIDs()).thenReturn(getFamilyMemberSiteAttrIDs());

		
		when(mockHttpServletRequest.getAttribute(any())).thenReturn(null);
		when(mockEnforceOrderFormBean.getSiteAttr()).thenReturn(getSiteAttrArray());
		when(mockEnforceOrderFormBean.getSiteAttributeValueMap()).thenReturn(getSiteAttributeValue());

		
		List<DynamicItemAttributeVO> temp = new ArrayList<>();
		UserGroupSearchVO searchOptions = new UserGroupSearchVO(0, 0, ENCRYPTED_SESSION_ID, 0, CURRENT_CART_ERR_FRENCH, CURRENT_CART_ERR_ENGLISH, false, false, 0, BLANK_NOT_ALLOWED_ERR_FRENCH, 0, BLANK_NOT_ALLOWED_ERR_ENGLISH, null, false);
		DynamicItemAttributeVO attributeVO = new DynamicItemAttributeVO(searchOptions, temp, temp, false);
		temp.add(attributeVO);
		ValidateCheckoutResponse response = new ValidateCheckoutResponse();

		try (MockedConstruction<OECatalogAssembler> mockedCheckout = Mockito.mockConstruction(OECatalogAssembler.class,
				(mock, context) -> {
					when(mock.setUnsetFavoriteItem(any(), any(), any())).thenReturn(true);
				})) {

			response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
			
			when(mockHttpServletRequest.getAttribute(any())).thenReturn(mockEnforceOrderFormBean);
			when(mockEnforceOrderFormBean.getSelectedSiteAttributeValueMap()).thenReturn(null);

			when(mockVolatileSessionBean.getFamilyMemberSiteAttrIDs()).thenReturn(null);
			when(mockEnforceOrderFormBean.getSiteAttr()).thenReturn(getSiteAttrArray());
			when(mockOESessionBean.getUsrSrchOptions()).thenReturn( getSearchOptions());
			response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());
			
			when(mockVolatileSessionBean.getFamilyMemberSiteAttrIDs()).thenReturn(getFamilyMemberSiteAttrIDs_empty());
			when(mockEnforceOrderFormBean.getSelectedSiteAttributeValueMap()).thenReturn(getSelectedSiteAttributeValue_empty());
			response = serviceToTest.validateCheckout(mockSessionContainer, mockHttpServletRequest);

			Assertions.assertNotNull(response);
			Assertions.assertEquals(true, response.isSuccess());


		}
	}
	private HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> getProfileAttributes() {
		return getSelectedSiteAttributeValue();
	}
	private ArrayList<UserGroupSearchVO> getSearchOptions() {
		ArrayList<UserGroupSearchVO> a = new ArrayList<>();
		
		UserGroupSearchExtendedVO vo = new UserGroupSearchExtendedVO(4366, 7125, "C1UXCompareEOO", 4366, "Midwest Region", "L", true, true, 1, "SC Midwest Region", 0, "", new Date(), true, false);
		a.add(vo);
		return a;
		
	}
	private SiteAttributesVO[] getSiteAttrArray() {
		
		SiteAttributesVO[] attArray = new SiteAttributesVO[1];
		
		SiteAttributesVO siteAttributesVO = new SiteAttributesVO(4849, 4366);
		siteAttributesVO.setAttrDisplayName("SC Midwest Region");
		siteAttributesVO.setAttrName("Midwest Region");
		attArray[0]=siteAttributesVO;
		
		return attArray;
		
	}
	
	private HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> getSelectedSiteAttributeValue() {
		HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> map = new HashMap<>();
		SiteAttributesVO siteAttributesVO = new SiteAttributesVO(4849, 4366);
		siteAttributesVO.setAttrDisplayName("SC Midwest Region");
		siteAttributesVO.setAttrName("Midwest Region");
		ArrayList<SiteAttrValuesVO> arrayList = new ArrayList<>();
		map.put(siteAttributesVO, arrayList);
		arrayList = new ArrayList<>();
		siteAttributesVO = new SiteAttributesVO(4993, 4366);
		siteAttributesVO.setAttrDisplayName("Red");
		siteAttributesVO.setAttrName("Red DESC");

		
		SiteAttrValuesVO attrValuesVO = new SiteAttrValuesVO(4366, 4993, 257594, "Red", "Red DESC", 1);
		arrayList.add(attrValuesVO);

		attrValuesVO = new SiteAttrValuesVO(4366, 4993, 257595, "Yellow", "Yellow", 2);
		arrayList.add(attrValuesVO);

		map.put(siteAttributesVO, arrayList);

		return map;
	}
	
	private HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> getSelectedSiteAttributeValue_empty() {
		HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> map = new HashMap<>();
		return map;
	}
	
	private HashMap getSiteAttributeValue() {
		HashMap map = new HashMap<>();
		map.put("key", "value");
		return map;
	}
	private HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> getPrflBasedAttrValues() {
		HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> map = new HashMap<>();
		SiteAttributesVO siteAttributesVO = new SiteAttributesVO(4849, 4366);
		siteAttributesVO.setAttrDisplayName("SC Midwest Region");
		siteAttributesVO.setAttrName("Midwest Region");
		
		ArrayList<SiteAttrValuesVO> arrayList = new ArrayList<>();
		SiteAttrValuesVO attrValuesVO = new SiteAttrValuesVO(4366, 4849, 257294, "IL", "Illinois", 1);
		arrayList.add(attrValuesVO);
		
		map.put(siteAttributesVO, arrayList);
		return map;
	}
	private Set<Integer> getFamilyMemberSiteAttrIDs() {
		Set<Integer> set = new HashSet<>();
		set.add(4849);
		set.add(4993);
		set.add(4366);
		return set;
	}
	
	private Set<Integer> getFamilyMemberSiteAttrIDs_empty() {
		Set<Integer> set = new HashSet<>();

		return set;
	}
	private Set<Integer> getFamilyMemberSiteAttrIDs_invalid() {
		Set<Integer> set = new HashSet<>();
		set.add(1234);
		return set;
	}
	
}
