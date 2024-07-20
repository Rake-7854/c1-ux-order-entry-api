/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	02/01/24	Satishkumar A		CAP-46675				C1UX BE - Create new API to check for EOO attributes and if we need to send back a list of attributes and values which will tell the front-end they have to select values
 *  02/05/24	T Harmon			CAP-47039				Fixed small issue with returning 200 for validateEOOCheckout
 *  03/07/24	T Harmon			CAP-46340				Fixed issue with remove item
 */
package com.rrd.c1ux.api.services.eoo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.admin.C1SiteAttribute;
import com.rrd.c1ux.api.models.eoo.EOOAttribute;
import com.rrd.c1ux.api.models.eoo.EOOAttributeValue;
import com.rrd.c1ux.api.models.eoo.ValidateCheckoutResponse;
import com.rrd.c1ux.api.models.shoppingcart.SaveSelectedAttributesRequest;
import com.rrd.c1ux.api.models.shoppingcart.SaveSelectedAttributesResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.wallace.atwinxs.admin.ao.SiteAssembler;
import com.wallace.atwinxs.admin.locator.AdministrationLocator;
import com.wallace.atwinxs.admin.locator.SiteAttributeComponentLocator;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesExtendedVO;
import com.wallace.atwinxs.admin.vo.SiteAttrValuesVO;
import com.wallace.atwinxs.admin.vo.SiteAttributesVO;
import com.wallace.atwinxs.admin.vo.UserGroupSearchVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IAdministration;
import com.wallace.atwinxs.interfaces.ISiteAttribute;
import com.wallace.atwinxs.orderentry.ao.DynamicItemAttributeVO;
import com.wallace.atwinxs.orderentry.ao.EnforceOrderFormBean;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OECatalogAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.customdocs.util.XMLAdapter;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class EnforceOnOrderingServiceImpl extends BaseOEService implements EnforceOnOrderingService {

	// CAP-46543 TH
	private SessionHandlerService sessionHandlerService;
	
	// CAP-46543 TH
	protected EnforceOnOrderingServiceImpl(TranslationService translationService, SessionHandlerService sessionHandlerService) {
		super(translationService);
		this.sessionHandlerService = sessionHandlerService;  	// CAP-46543 TH
	}

	@Override
	public ValidateCheckoutResponse validateCheckout(SessionContainer sc, HttpServletRequest request) throws AtWinXSException {

		ApplicationSession applicationSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = applicationSession.getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ValidateCheckoutResponse response = new ValidateCheckoutResponse();
		response.setSuccess(true);

		if(appSessionBean.hasEnforceOnOrdering()) {

			OEShoppingCartAssembler assembler = 
					OEAssemblerFactory.getShoppingCartAssembler(appSessionBean.getCustomToken(), 
							appSessionBean.getDefaultLocale(),
							appSessionBean.getApplyExchangeRate());
			assembler.checkEOO(request, volatileSessionBean, oeSession.getOESessionBean(), appSessionBean);
			setDynamicAttributeList(request, oeSession, appSessionBean, volatileSessionBean, response);

		}

		return response;

	}

	// CAP-46543 TH
	// CAP-46340 TH - Refactored for remove item
	@Override
	public SaveSelectedAttributesResponse saveEooAttributes(SessionContainer sc, SaveSelectedAttributesRequest request) throws AtWinXSException 
	{	
		// First, get the values from the request here
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean vsb = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OrderEntrySession oeSession = ((OrderEntrySession) sc.getModuleSession());
		
		// Make sure we have the attributes here				
		String searchCriteriaString = buildEOOSearchCriteriaString(request);
		
		int changeAttributeID = request.getChangedAttributeID();
		boolean saveAttributes = false;				
		
		SaveSelectedAttributesResponse response = new SaveSelectedAttributesResponse();
		
		// Check the changedAttribute ID - if -1, that means we are trying to finish and save
		if (changeAttributeID <= 0)
		{
			changeAttributeID = request.getC1SiteAttributes()[0].getAttributeID();
			saveAttributes = true;
			response.setSuccess(true);
		}
		
		// Check the search criteria string - if blank/null, we have a problem - throw an Error
		if (Util.isBlankOrNull(searchCriteriaString))
		{
			response.setSuccess(false);
			response.setMessage("Saving Attribute Values failed.");
		}

		// If we are done, we have to save all attributes here
		if (response.isSuccess() && saveAttributes)
		{
			applyEooAttributes(response, asb, vsb, oeSession, request.getC1SiteAttributes());
			sc.getApplicationVolatileSession().setIsDirty(true);
			sessionHandlerService.saveFullSessionInfo(sc.getApplicationVolatileSession(), sc.getApplicationVolatileSession().getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);
		}
		else
		{
		
			SiteAssembler siteAssembler = new SiteAssembler(asb.getCustomToken(),
					asb.getDefaultLocale());
			
			// Now, get the attributes family information here
			String strXmlDocument = siteAssembler.getAttributeValueRelationships(asb.getSiteID(), changeAttributeID, searchCriteriaString, asb.getGroupName(), vsb.getSelectedSiteAttribute());
		    
			// Now, we need to convert the xml document into our response object
			response = convertXmlToResponse(strXmlDocument, request.getC1SiteAttributes());				
		}
		
		return response;
	}
	
	protected SaveSelectedAttributesResponse convertXmlToResponse(String strXmlDocument, C1SiteAttribute[] selectedAttributes) throws AtWinXSException
	{
		SaveSelectedAttributesResponse response = new SaveSelectedAttributesResponse();
		StringReader reader = new StringReader(strXmlDocument);
		
		Document xmlDocument = XMLAdapter.readDocument(reader);
		
		// Get the attributes object
		Element elFamily =  xmlDocument.getRootElement().getChild("family");
		Element attributesList = elFamily.getChild("attributes");
		List<Element> attributes = attributesList.getChildren("attribute");
		List<EOOAttribute> outAttributeList = new ArrayList<EOOAttribute>();
		for (Element currentElement : attributes)
		{
			EOOAttribute eooAttribute = new EOOAttribute();
			eooAttribute.setAttributeID(Integer.parseInt(currentElement.getChild("attrID").getText()));
			eooAttribute.setAttributeDesc(currentElement.getChild("attrDispNm").getText());
			eooAttribute.setAttributeValues(getAttributeValuesForResponse(eooAttribute.getAttributeID(), currentElement.getChild("attrValues"), selectedAttributes));
			
			outAttributeList.add(eooAttribute);						
		}
		
		// Set the attributes here
		response.setAttributes(outAttributeList);
				
		// Check to see if all values are selected, if so, we can save, if not we return 422 with a list of the attributes and what is selected
		response.setSuccess(checkAllAttributesSelected(outAttributeList));				
		
		return response;
	}
	
	protected void applyEooAttributes(SaveSelectedAttributesResponse response, AppSessionBean asb, VolatileSessionBean vsb, OrderEntrySession oeSession, C1SiteAttribute[] selectedAttributes)
	{
		OECatalogAssembler catAssembler = new OECatalogAssembler(asb.getCustomToken(),
				asb.getDefaultLocale());
	
		try
		{
			vsb.setBatchVDPMode(false);
			vsb.setBatchVDPWorkFlowCode(""); 
			completeEooSelections(selectedAttributes, asb, vsb, oeSession.getOESessionBean());
			
			oeSession.getOESessionBean().setSiteAttrFilterSQL(catAssembler.reloadSiteAttrFilterSQL(
					vsb.getSelectedSiteAttribute(), //selectedAttrValForSQL,
		            asb.getProfileAttributes(),
		            oeSession.getOESessionBean().getUsrSrchOptions(), 
		            asb.getSiteID() , true));
		
			sessionHandlerService.saveFullSessionInfo(oeSession, asb.getSessionID(),
					AtWinXSConstant.ORDERS_SERVICE_ID);															   
		} catch (Exception e)
		{
			response.setSuccess(false);
		}
	}
	
	protected boolean checkAllAttributesSelected(List<EOOAttribute> attributes)
	{		
		for (EOOAttribute currentAttribute : attributes)
		{
			boolean hasSelectedValue = false;
			for (EOOAttributeValue currentValue : currentAttribute.getAttributeValues())
			{
				if (currentValue.isSelected())
				{
					hasSelectedValue = true;
					break;
				}
			}
			
			if (!hasSelectedValue)
			{
				return true;
			}
		}
		
		return true;
	}
	
	protected List<EOOAttributeValue> getAttributeValuesForResponse(int attributeID, Element attributeValues, C1SiteAttribute[] selectedAttributes)
	{
		List<EOOAttributeValue> attributeList = new ArrayList<EOOAttributeValue>();
		List<Element> xmlValues = attributeValues.getChildren("attrValue");
		
		for (Element currentValue : xmlValues)
		{
			EOOAttributeValue eooValue = new EOOAttributeValue();
			eooValue.setAttributeValID(Integer.parseInt(currentValue.getChild("attrValID").getText()));
			eooValue.setAttributeValDesc(currentValue.getChildText("attrValDesc"));
			int selectedValue = getSelectedAttributeID(attributeID, selectedAttributes);
			if (selectedValue == eooValue.getAttributeValID())
			{
				eooValue.setSelected(true);
			}
			
			attributeList.add(eooValue);
		}
		
		return attributeList;
	}
	
	protected int getSelectedAttributeID(int attributeID, C1SiteAttribute[] selectedAttributes)
	{
		for (C1SiteAttribute currentAttribute : selectedAttributes)
		{
			if (currentAttribute.getAttributeID() == attributeID)
			{
				return currentAttribute.getAttributeValueID();				
			}
		}
		
		return AtWinXSConstant.INVALID_ID;
	}
	
	protected String buildEOOSearchCriteriaString(SaveSelectedAttributesRequest request) throws AtWinXSException
	{		
		// Get the array list here for the selected attributes
		C1SiteAttribute[] attributes = request.getC1SiteAttributes();
		StringBuilder searchCriteria = new StringBuilder();
		
		if (attributes != null)
		{
			for (C1SiteAttribute currentAttribute : attributes)
			{
				searchCriteria.append(currentAttribute.getAttributeID()).append("~").append(currentAttribute.getAttributeValueID()).append("|");
			}
		
			return searchCriteria.toString();
		}
		else
		{
			return null;
		}
	}				
	
	//CP-8788, load dynamic attribute list
	protected void setDynamicAttributeList(HttpServletRequest request, OrderEntrySession oeSession, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, ValidateCheckoutResponse response) throws AtWinXSException
	{
		DynamicItemAttributeVO[] attrSearchOptList = null;
		if (!oeSession.getOESessionBean().isEOOSelectionCompleted())
		{	
			attrSearchOptList = buildAttributeListOptions(oeSession.getOESessionBean().getUsrSrchOptions(), (EnforceOrderFormBean)request.getAttribute(OrderEntryConstants.ENFORCE_FORM_BEAN), oeSession, appSessionBean, volatileSessionBean);
			if( attrSearchOptList!=null && attrSearchOptList.length > 0) {
				convertResponse(attrSearchOptList, response);
			}

		}	


	}

	protected DynamicItemAttributeVO[] buildAttributeListOptions(List<UserGroupSearchVO> searchOptions, EnforceOrderFormBean bean, OrderEntrySession oeSession, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		DynamicItemAttributeVO[] result = null;

		HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> profileAttrs = appSessionBean.getProfileAttributes();

		Set<Integer> familyMemberAttrSet = volatileSessionBean.getFamilyMemberSiteAttrIDs();

		setFamilyMemberAttrSetInSession(appSessionBean, volatileSessionBean,familyMemberAttrSet, searchOptions);

		if(bean != null)
		{
			List<DynamicItemAttributeVO> temp = new ArrayList<>();
			SiteAttributesVO[] siteAttr =bean.getSiteAttr();
			HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttrVal = volatileSessionBean.getSelectedSiteAttribute();
			if(bean.getSelectedSiteAttributeValueMap() != null && bean.getSelectedSiteAttributeValueMap().size() > 0)
			{
				selectedSiteAttrVal = bean.getSelectedSiteAttributeValueMap();
			}

			if(siteAttr != null && siteAttr.length > 0)
			{
				Map<Integer, ArrayList<SiteAttrValuesExtendedVO>> siteAttrVals = bean.getSiteAttributeValueMap();
				for(int i = 0; i < siteAttr.length; i++)
				{
					if(siteAttrVals != null && !siteAttrVals.isEmpty())
					{
						SiteAttributesVO siteAttrVO = siteAttr[i];
						List<SiteAttrValuesVO>  attrValList = getAttrValList(searchOptions, siteAttrVO, profileAttrs, oeSession, appSessionBean);

						attrValList = nullCheckAttrValList(attrValList, siteAttrVals, siteAttrVO.getAttrID());
						
						List<SiteAttrValuesVO> attValSelected = getAttValSelected(attrValList, selectedSiteAttrVal);

						boolean hasFamily =  hasFamily(familyMemberAttrSet, siteAttr[i].getAttrID());

						temp.add(new DynamicItemAttributeVO(siteAttr[i], attrValList, attValSelected, hasFamily));
						//CP-1743 end	                    
					}
				}
			}

			result = temp.toArray(new DynamicItemAttributeVO[temp.size()]);

		}

		return result;
	}

	protected void setFamilyMemberAttrSetInSession(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,Set<Integer> familyMemberAttrSet, List<UserGroupSearchVO> searchOptions ) throws AtWinXSException {

		if(familyMemberAttrSet == null || familyMemberAttrSet.isEmpty())
		{
			OECatalogAssembler catalogAsm = new OECatalogAssembler(appSessionBean.getCustomToken(), 
					appSessionBean.getDefaultLocale());

			Set<Integer> familyMemberAttrIds = catalogAsm.getHasFamilyItemSearchAttrs(searchOptions, appSessionBean);

			volatileSessionBean.setFamilyMemberSiteAttrIDs(familyMemberAttrIds);
		}

	}
	protected boolean hasFamily(Set<Integer> familyMemberAttrSet, int siteAttrID) {

		return (familyMemberAttrSet != null && !familyMemberAttrSet.isEmpty()) 
				&& familyMemberAttrSet.contains(siteAttrID);

	}
	protected List<SiteAttrValuesVO> getAttrValList(List<UserGroupSearchVO> searchOptions, SiteAttributesVO siteAttrVO, HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>> profileAttrs, OrderEntrySession oeSession, AppSessionBean appSessionBean) {

		List<SiteAttrValuesVO> attrValList = new ArrayList<>();

		if(appSessionBean.hasEnforceOnCatalog()) {
			HashMap<SiteAttributesVO, ArrayList<SiteAttrValuesVO>>  prflBasedAttrValues = oeSession.getOESessionBean().getPrflBasedAttrValues(); 

			//CP-2126 RBA - Added checking prflBasedAttrValues != null before using the variable
			if(prflBasedAttrValues != null)
			{
				attrValList = prflBasedAttrValues.get(siteAttrVO);
			}

			if((attrValList == null || attrValList.isEmpty()) && searchOptions != null)
			{
				//CP-1722/CP-1763 START: use profile setting if enforce on catalog is on

				for(int j = 0; j < searchOptions.size(); j++)
				{
					UserGroupSearchVO ugsVO = searchOptions.get(j);
					if((siteAttrVO.getAttrID() == ugsVO.getAttrID()) && (ugsVO.isEnforceOnCatalog() && profileAttrs != null))
					{
						attrValList =  profileAttrs.get(siteAttrVO);
						break;

					}
				}

				//CP-1722/CP-1763 END
			}
		}

		return attrValList;
	}

	protected List<SiteAttrValuesVO> nullCheckAttrValList(List<SiteAttrValuesVO> attrValList, Map<Integer, ArrayList<SiteAttrValuesExtendedVO>> siteAttrVals, int attrID) {
		if(attrValList == null || attrValList.isEmpty())
		{
			attrValList = (ArrayList)siteAttrVals.get(attrID);
		}
		return attrValList;

	}

	protected List<SiteAttrValuesVO> getAttValSelected(List<SiteAttrValuesVO> attrValList, HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttrVal) {

		List<SiteAttrValuesVO> attValSelected =  new ArrayList<>();

		Iterator<SiteAttrValuesVO> it = attrValList.iterator();
		while(it.hasNext())
		{
			SiteAttrValuesVO attrVal = it.next();

			SiteAttrValuesVO[] selectedSiteAttributeValues = null;
			if (selectedSiteAttrVal != null)
			{
				selectedSiteAttributeValues =selectedSiteAttrVal.get(attrVal.getAttrID());
			} 

			//CP-11410 , need to check length > 0 to indicate there are selected value
			if(selectedSiteAttributeValues != null && selectedSiteAttributeValues.length > 0
					&& selectedSiteAttributeValues[0] != null 
					&& selectedSiteAttributeValues[0].getAttrValID() == attrVal.getAttrValID())
			{
				attValSelected.add(attrVal);
			}
		}

		return (!attValSelected.isEmpty()) ? attValSelected : null;

	}


	protected ValidateCheckoutResponse convertResponse(DynamicItemAttributeVO[] attributeVOs, ValidateCheckoutResponse response) {
		List<EOOAttribute> attrList = new ArrayList<>();

		for(DynamicItemAttributeVO att : attributeVOs) {
			EOOAttribute eoAttr = new EOOAttribute();
			eoAttr.setAttributeID(att.getSiteAttr().getAttrID());
			eoAttr.setAttributeDesc(att.getSiteAttr().getAttrDisplayName());
			Iterator<SiteAttrValuesVO> it = att.getSiteAttrValueVOList().iterator();
			List<EOOAttributeValue> attrValList = new ArrayList<>();
			while(it.hasNext()){
				SiteAttrValuesVO attrVal = it.next();
				EOOAttributeValue eooAttributeValue = new EOOAttributeValue();
				eooAttributeValue.setAttributeValID(attrVal.getAttrValID());
				eooAttributeValue.setAttributeValDesc(attrVal.getAttrValDesc());
				attrValList.add(eooAttributeValue);
			}
			eoAttr.setAttributeValues(attrValList);
			attrList.add(eoAttr);
		}

		response.setAttributes(attrList);
		response.setSuccess(true); // CAP-47039
		return response;
	}
	
	
	
//	//CP-8346
//	public void checkEOO(VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean) throws AtWinXSException
//	{
//		if (!oeSessionBean.isEOOSelectionCompleted())
//		{
//			OEShoppingCartAssembler oeShoppingAssembler = new OEShoppingCartAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), false);
//			if (oeShoppingAssembler.onlyOneSelectionPerEOOAttribute(appSessionBean, volatileSessionBean))
//				{
//					oeShoppingAssembler.setBatchVDP(volatileSessionBean, oeSessionBean, volatileSessionBean.getSelectedSiteAttribute(), appSessionBean  );
//					oeSessionBean.setEOOSelectionCompleted(true);
//				} else {
//					OECatalogAssembler assembler = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
//					//CAP-12084 Added code to bypass EOO if BUID is not in the property file and no attribute value is selected
//					ArrayList<UserGroupSearchVO> userSearchOptions = oeSessionBean.getUsrSrchOptions();
//					HashMap<Integer, SiteAttrValuesVO[]> selectedSiteAttributes = volatileSessionBean.getSelectedSiteAttribute();
//					
//					if(OrderEntryUtil.isSuppressBackorderedStatusForBU(appSessionBean.getBuID()))
//					{
//						for (int i = 0; userSearchOptions != null && i < userSearchOptions.size(); i++)
//						{
//							UserGroupSearchExtendedVO vo = (UserGroupSearchExtendedVO) userSearchOptions.get(i);
//
//							if (selectedSiteAttributes != null)
//							{
//								if (selectedSiteAttributes.containsKey(vo.getAttrID()))
//								{
//									SiteAttrValuesVO[] selectedValues = (SiteAttrValuesVO[]) selectedSiteAttributes.get(new Integer(vo.getAttrID()));
//									if (selectedValues.length > 0)
//									{
//										doEnforceOrdering(appSessionBean, oeSessionBean, volatileSessionBean, 5, assembler, "");
//									}
//								}
//							}
//						}
//
//					}
//					else
//					{
//						doEnforceOrdering(
//								appSessionBean, 
//								oeSessionBean, 								
//								volatileSessionBean,								
//								5, 
//								assembler); 
//					}
//				}
//			}
//		}		
//	
//	
//	/**
//	 * CP-947: This method is used when it has enforce ordering and items in cart is empty
//	 * 
//	 * @param appSessionBean
//	 * @param oeSessionBean
//	 * @param request
//	 * @param volatileSessionBean
//	 * @param forwardPage
//	 * @return String
//	 * @throws AtWinXSException
//	 */
//	public String doEnforceOrdering(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, 
//	        						VolatileSessionBean volatileSessionBean, int forwardPage, OECatalogAssembler catalogAssembler, String searchParam) throws AtWinXSException
//	{
//	    HashMap profileAttributes = appSessionBean.getProfileAttributes();
//	    ArrayList userSearchOptions = oeSessionBean.getUsrSrchOptions();
//	    
//	    
//        //CP-5081 Added volatileSessionBean
//	    EnforceOrderFormBean bean = this.getEnforceOrderFormBean(profileAttributes, 
//	            								userSearchOptions, appSessionBean.getSiteID(), 
//	            								forwardPage, volatileSessionBean, appSessionBean, searchParam);
//	    
//	    //request.setAttribute(AtWinXSConstant.VOLATILE_SESSION_BEAN, volatileSessionBean); 
//	    //request.setAttribute(OrderEntryConstants.ENFORCE_FORM_BEAN, bean);
//	    
//		//CP-1743 start
//	    Set famMemSiteAttrIDs = volatileSessionBean.getFamilyMemberSiteAttrIDs();
//	    
//	    if(famMemSiteAttrIDs == null || famMemSiteAttrIDs.isEmpty())
//	    {
//	        if(bean != null)
//	        {
//	            SiteAttributesVO[] enforceOrdfields = bean.getSiteAttr();
//	            
//	            if(enforceOrdfields != null && enforceOrdfields.length > 0)
//	            {
//	                famMemSiteAttrIDs = catalogAssembler.getHasFamilySiteAttrsForEnforceOrdering(
//								                        enforceOrdfields, 
//								                        appSessionBean.getSiteID());
//	            }
//	        }
//	        volatileSessionBean.setFamilyMemberSiteAttrIDs(famMemSiteAttrIDs);
//	    }
//        //CP-1743 end        
//	    
//	    return OrderEntryConstants.ENFORCE_ORDERING_PAGE;
//	}
//	//CP-947 : End
//	
//	//CP-947 : Start
//	/** 
//     * Method used to populate the EnforceOrderFormBean
//     * 
//     * @param serviceChargesEnabled
//     * @return Document
//	 * @throws AtWinXSException
//     * @throws AtWinXSException
//     */
//     //CP-5081 Added volatileSessionBean
//	public EnforceOrderFormBean getEnforceOrderFormBean(HashMap profileAttributes, ArrayList userSearchOptions, 
//	        						int siteID, int forwardPage, VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean, String searchParam) throws AtWinXSException
//	{
//	    
//	    HashMap siteAttrValues = new HashMap();
//	    HashMap selectedSiteAttrVal = new HashMap();
//	    ArrayList attributeVO = new ArrayList();
//	    //CAp-9472
//	    ISiteAttribute admin = SiteAttributeComponentLocator.locate(appSessionBean.getCustomToken());
//	    
//	    String search = searchParam;
//		boolean isSearch = OrderEntryConstants.CATALOG_SEARCH_PARAM.equals(search);
//		boolean multipleValuesForEOO;
//		HashMap selectedSiteAttributes = volatileSessionBean.getSelectedSiteAttribute(); 
//	    for(int i = 0; userSearchOptions != null && i < userSearchOptions.size(); i++)
//	    {
//	        //CP-5081 PTB change vo type
//	    	multipleValuesForEOO = true;
//	    	//CP-11410 , need to check length > 0 to indicate there are selected value
//	    	boolean valueSetByCart = true;
//	    	UserGroupSearchExtendedVO vo = (UserGroupSearchExtendedVO)userSearchOptions.get(i);
//	    	
//    		if (selectedSiteAttributes!=null)
//    		{
//    			if (selectedSiteAttributes.containsKey(vo.getAttrID()))
//    			{
//    				SiteAttrValuesVO[] selectedValues = (SiteAttrValuesVO[])selectedSiteAttributes.get(new Integer(vo.getAttrID()));
//	    			if (selectedValues.length==1)
//	    			{ 
//	    				 multipleValuesForEOO = false;
//	    			}
//	    			//CP-11410 , need to check length > 0 to indicate there are selected value
//	    			if (selectedValues.length == 0)
//	    			{
//	    				valueSetByCart = false;
//	    			}
//    			}
//    		}
//	        	
//	    	
//	        SiteAttributesVO siteAttributesVO = null;
//	        
//	        if(vo.isEnforceOnOrdering() &&	(multipleValuesForEOO || forwardPage!=5)) //CP-6979 if eoo && (multiple || !cartEOOcheck)
//	        {
//	        	siteAttributesVO = 
//	        		new SiteAttributesVO(
//	        			siteID, 
//	        			vo.getAttrID(),
//	        			vo.getAttrName(),
//	        			vo.getAttrDispName(),
//	        			vo.isMassAttr()); //CP-5081 PTB - include mass attr indicator
//	            //CP-5081 dont get values for mass attr, too big to handle
//	            SiteAttributeValuesBean beanValues = null;
//	            ArrayList siteAttrValuesList = null;
//	            if (vo.isMassAttr())
//	            {
//	            	siteAttrValuesList = new ArrayList();	            	
//		        } else {
//		        	//CAP-9472
//		            beanValues= admin.getSiteAttributeValues(siteAttributesVO.getKey(), -1, null, null); // CP-806 - get all attribute values across all pages
//		            siteAttrValuesList =beanValues.getValues();
//		        }
//		            
//	            siteAttrValues.put(new Integer(siteAttributesVO.getAttrID()), siteAttrValuesList);
//	            attributeVO.add(siteAttributesVO);
//	                
//	            if(vo.isEnforceOnOrdering() || vo.isEnforceOnCatalog())  //CP-8621
//	            { 
//	            	//CP-11410 , need to check length > 0 to indicate there are selected value
//	               if (selectedSiteAttributes!= null && selectedSiteAttributes.containsKey(vo.getAttrID()) && valueSetByCart && volatileSessionBean.getShoppingCartCount() != 0)  
//	               { 
//	            	   SiteAttrValuesVO[] selectedSiteAttrVals = ((SiteAttrValuesVO [])selectedSiteAttributes.get(vo.getAttrID())); 
//			           ArrayList<SiteAttrValuesVO> valList = new ArrayList<SiteAttrValuesVO>(Arrays.asList(selectedSiteAttrVals)); 
//			           siteAttrValues.put(new Integer(vo.getAttrID()), valList); 
//	               } else if(profileAttributes != null && vo.isEnforceOnCatalog()) //CP-11410
//	               { 
//	                   Set profileAttrSet = profileAttributes.keySet(); 
//	                   Iterator it = profileAttrSet.iterator(); 
//	                   while(it.hasNext()) 
//	                   { 
//	                      if(it.next().equals(siteAttributesVO)) 
//	                      { 
//	                         siteAttrValues.put(new Integer(siteAttributesVO.getAttrID()), 
//	                        (ArrayList)profileAttributes.get(siteAttributesVO)); 
//	                      } 
//			           } 
//	               } 
//	            } 
//	        }
//	    }
//	    
//	    // AC 12.03.08 - Updated params to remove null for selectedSiteAttrValSQL
//        //CP-5081 Added volatileSessionBean
//	    this.selectedSiteAttributeFromRequest(request, selectedSiteAttrVal, siteID, volatileSessionBean);
//	    //this.selectedSiteAttributeFromRequest(request, selectedSiteAttrVal, null, siteID);
//	    SiteAttributesVO[] siteAttr = (SiteAttributesVO[]) attributeVO.toArray(new SiteAttributesVO[]{});
//	    
//	    EnforceOrderFormBean bean = new EnforceOrderFormBean(siteAttr, 
//	    		siteAttrValues, 
//				forwardPage, 
//				selectedSiteAttrVal,
//	    		isSearch
//	    		);
//	   	   
//        return bean;
//	    
//	}
//	
//	
//	// AC 12.03.08 - Updated method arguments to remove Hashmap selectedAttrValForSQL
//    //CP-5081 Added volatileSessionBean	
//	public void selectedSiteAttributeFromRequest(AppSessionBean appSessionBean, HashMap selectedAttrVal, int siteID,VolatileSessionBean volatileSessionBean) throws AtWinXSException
//	{
//	  
//	    int attrCount = Util.safeStringToInt(request.getParameter(OrderEntryConstants.ATTRIBUTE_COUNT));
//	    HashMap attrValues = new HashMap();
//	    //CP-5081 VM Start Added hashmap
//	    HashMap selectedMASSSiteAttribute = new HashMap<String, String>();
//	    boolean rememberMASSAttrValues = false;
//	    //CP-5081 VM End
//	    //cp-5081 ptb used ISiteAttribute
//	    ISiteAttribute admin = SiteAttributeComponentLocator.locate(appSessionBean.getCustomToken());
//	    
//	    //CP-947 Save the selected attribute to HashMap
//	    for(int i = 0; i < attrCount; i++)
//	    {
//	        String selectedAttr = request.getParameter(OrderEntryConstants.ATTRIBUTE_NAME + i);
//	        String [] attrIDAndAttrValID = selectedAttr.split(",");
//	        int attrID = Util.safeStringToInt(attrIDAndAttrValID [0]);
//	       
//	        if(attrIDAndAttrValID.length > 1)  //CP-1722 check if attribute has a selected attr value
//	        {
//	        	//CP-5081 PTB set the 2nd param to String value
//                String attrValStr = Util.nullToEmpty(attrIDAndAttrValID [1]);
//                //initialize to 0
//                int attrValID = 0;
//                
//                //check for ~
//                if (attrValStr.indexOf("~") > -1)
//                {
//                	//Check if attrValStr.length > 1 -- This is just to ensure that we have value attached to ~
//                    if (attrValStr.length() > 0)
//                    {
//                        // Parse the textValue (Ex. ACGroup)
//                    	attrValStr = attrValStr.substring(attrValStr.indexOf("~") + 1);
//                    	//CP-5081 Start VM Store entered values in session to remember during navigation
//                    	selectedMASSSiteAttribute.put(new Integer(attrID).toString(), attrValStr); 
//                    	rememberMASSAttrValues = true;
//                    	//CP-5081 VM End
//                    
//                        // Call a method that checks the value in 216 table
//                    	SiteAttrValuesVO siteAttrValVO = admin.getSiteAttrValueByName(siteID, attrValStr, 
//                    			new SiteAttributesVO(siteID, attrID));
//                    	// If the value is valid,
//                    	if (siteAttrValVO != null)
//                    	{
//                    		//get the attrValID and set it to attrValID.
//                    		attrValID = siteAttrValVO.getAttrValID();
//                    	}
//                    	else
//                    	{
//                    		attrValID = -1;
//                    	}
//                        //else continue, ignore the line
//                    }
//                } 
//                else 
//                {
//                     attrValID = Util.safeStringToInt(attrValStr);
//                }
//                //end of CP-5081
//
//		        if(attrValID >= -1)
//		        { 
//			        attrValues.put(new Integer(attrID), new Integer(attrValID));      
//		        }
//	        }
//	    }
//	    
//	    // AC 12.03.08 Updated param to remove 
//	    this.loadSelectedSiteAttrValues(attrValues, selectedAttrVal, siteID);
//	    //this.loadSelectedSiteAttrValues(attrValues, selectedAttrVal, selectedAttrValForSQL, siteID);
//	    
//	    //CP-5081 VM Start store mass attr value in session
//	    if (rememberMASSAttrValues){
//	    	volatileSessionBean.setSelectedMASSSiteAttribute(selectedMASSSiteAttribute);	
//	    }	    
//	    //CP-5081 VM end
//	}
//
//	// AC 12.03.08 Updated method arguments to remove HashMap selectedAttrValForSQL
//	public void loadSelectedSiteAttrValues(HashMap attrValues, HashMap selectedAttrVal, int siteID) throws AtWinXSException
//	{
//	    Set keySet = attrValues.keySet();
//	    Iterator it = keySet.iterator();
//	    
//	    while(it.hasNext())
//	    {
//	        Integer attrIDObj = (Integer)it.next();
//	        int attrID = attrIDObj.intValue();
//	        int attrValID = ((Integer)attrValues.get(attrIDObj)).intValue();
//		    // AC 12.03.08 - Disabled ArrayList selectedAttributeArray
//	        //ArrayList selectedAttributeArray = new ArrayList();
//	        IAdministration admin = AdministrationLocator.locate(this.getToken());
//	        SiteAttrValuesVO[] selectedSiteAttrValues = new SiteAttrValuesVO [1];
//	        SiteAttributesVO siteAttrVO = new SiteAttributesVO(siteID, attrID);
//	        SiteAttrValuesVO siteAttributeValVO = admin.getSiteAttrValueByID(siteID, attrValID, 
//	                														 siteAttrVO);
//		    // AC 12.03.08 - Disabled ArrayList selectedAttributeArray
//	        // selectedAttributeArray.add(siteAttributeValVO);
//	        selectedSiteAttrValues[0] = siteAttributeValVO;
//	        
//	        if(selectedAttrVal != null)
//	        {
//	            selectedAttrVal.put(new Integer(attrID), selectedSiteAttrValues);
//	        }
//	        
//	        // AC 12.03.08 Disabled logic below.
//	        /*if(selectedAttrValForSQL != null)
//	        {
//	            selectedAttrValForSQL.put(siteAttrVO, selectedAttributeArray);
//	        }*/
//		}
//       
//	}	
	
	// CAP-46543 TH BEGIN
	protected void completeEooSelections(C1SiteAttribute[] selectedAttributes, AppSessionBean asb, VolatileSessionBean vsb, OEOrderSessionBean oeSessionBean) throws AtWinXSException
	{
		OECatalogAssembler catalogAsm = new OECatalogAssembler(asb.getCustomToken(), asb.getDefaultLocale());
	    HashMap selectedAttrVal = new HashMap();
	    selectedSiteAttributeFromRequest(selectedAttributes, selectedAttrVal, asb.getSiteID(), vsb, asb);
	    Set keySet = selectedAttrVal.keySet();
	    Iterator it = keySet.iterator();
	    HashMap selectedSiteAttributes = vsb.getSelectedSiteAttribute();
	    while(it.hasNext())
	    {
	        Integer attrIDObj = (Integer)it.next();
        	//CP-12471 Removed If statement, so we always set the site attribute values with the answers from the popup.
	        //if (selectedSiteAttributes.containsKey(attrIDObj) || appSessionBean.isNewLookNFeel()) //CP-8788 allow selectedSiteAttr to be blank for no specified values
	        //{
	        	selectedSiteAttributes.put(attrIDObj, selectedAttrVal.get(attrIDObj));
	        //}
	    }

		//this.setBatchVDP(volatileSessionBean, oeSession.getOESessionBean(), selectedAttrVal);//CP-7310
		this.setBatchVDP(vsb, oeSessionBean, selectedSiteAttributes, asb);//CP-7660
		oeSessionBean.setEOOSelectionCompleted(true);
	    vsb.setSelectedSiteAttribute(selectedSiteAttributes);
		vsb.saveOrderAttributes(asb, vsb.getOrderId().intValue()); //CP-7595
	}
	
	//CP-7310
	public void setBatchVDP(VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean, HashMap selectedAttrVal, AppSessionBean appSessionBean) throws AtWinXSException
	{
		volatileSessionBean.setBatchVDPMode(false);
	    volatileSessionBean.setBatchVDPWorkFlowCode("");
		OECatalogAssembler catalogAsm = new OECatalogAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
	    catalogAsm.setBatchVDPMode(volatileSessionBean, selectedAttrVal, appSessionBean.getSiteID(), appSessionBean.isDemoUser());
	    catalogAsm.overrideCarrierSelection(oeSessionBean.getUserSettings(), selectedAttrVal, OrderEntryConstants.INVALID_ORDER_ID, appSessionBean.getSiteID(), appSessionBean.getBuID());
	}
	
	
	/** 
     * Method used to populate the HashMap selectedAttrVal from the request
     * 
     * @param request
     * @param selectedAttrVal
     * @param selectedAttrValForSQL
     * @param appSessionBean
     * @return 
     * @throws AtWinXSException
     */
	// AC 12.03.08 - Updated method arguments to remove Hashmap selectedAttrValForSQL
    //CP-5081 Added volatileSessionBean	
	public void selectedSiteAttributeFromRequest(C1SiteAttribute[] selectedAttributes, HashMap selectedAttrVal, int siteID,VolatileSessionBean volatileSessionBean, AppSessionBean asb) throws AtWinXSException
	{
	  
	    int attrCount = selectedAttributes.length;
	    HashMap attrValues = new HashMap();
	    //CP-5081 VM Start Added hashmap
	    HashMap selectedMASSSiteAttribute = new HashMap<String, String>();
	    boolean rememberMASSAttrValues = false;
	    //CP-5081 VM End
	    //cp-5081 ptb used ISiteAttribute
	    ISiteAttribute admin = SiteAttributeComponentLocator.locate(asb.getCustomToken());
	    
	    //CP-947 Save the selected attribute to HashMap
	    for(int i = 0; i < attrCount; i++)
	    {	       
	        int attrID = selectedAttributes[i].getAttributeID();
	       
	        if(selectedAttributes[i].getAttributeValueID() > 0)  //CP-1722 check if attribute has a selected attr value
	        {	        	
                int attrValID = selectedAttributes[i].getAttributeValueID();
                                
		        if(attrValID >= -1)
		        { 
			        attrValues.put(new Integer(attrID), new Integer(attrValID));      
		        }
	        }
	    }
	    
	    loadSelectedSiteAttrValues(attrValues, selectedAttrVal, siteID, asb);
	    
	    if (rememberMASSAttrValues){
	    	volatileSessionBean.setSelectedMASSSiteAttribute(selectedMASSSiteAttribute);	
	    }	    
	    //CP-5081 VM end
	}
	
	/** 
     * CP-947 : Method used to populate the HashMap selectedAttrVal and make it reusable.
     * 
     * HashMap attrValues should have a key-value pair of attribute ID and attribute value ID respectively.
     * 
     * HashMap selectedAttrVal can be an empty HashMap. The passed reference will be populated in this method.
     * 
     * HashMap selectedAttrValForSQL can be null. It will only needed by the reloadSiteAttrFilterSQL() method 
     * of OECatalogAssembler
     * 
     * @param attrValues
     * @param selectedAttrVal
     * @param selectedAttrValForSQL
     * @param siteID
     * @return 
     * @throws AtWinXSException
     */
	// AC 12.03.08 Updated method arguments to remove HashMap selectedAttrValForSQL
	protected void loadSelectedSiteAttrValues(HashMap attrValues, HashMap selectedAttrVal, int siteID, AppSessionBean asb) throws AtWinXSException
	{
	    Set keySet = attrValues.keySet();
	    Iterator it = keySet.iterator();
	    
	    while(it.hasNext())
	    {
	        Integer attrIDObj = (Integer)it.next();
	        int attrID = attrIDObj.intValue();
	        int attrValID = ((Integer)attrValues.get(attrIDObj)).intValue();
		    // AC 12.03.08 - Disabled ArrayList selectedAttributeArray
	        //ArrayList selectedAttributeArray = new ArrayList();
	        IAdministration admin = AdministrationLocator.locate(asb.getCustomToken());
	        SiteAttrValuesVO[] selectedSiteAttrValues = new SiteAttrValuesVO [1];
	        SiteAttributesVO siteAttrVO = new SiteAttributesVO(siteID, attrID);
	        SiteAttrValuesVO siteAttributeValVO = admin.getSiteAttrValueByID(siteID, attrValID, 
	                														 siteAttrVO);
		    // AC 12.03.08 - Disabled ArrayList selectedAttributeArray
	        // selectedAttributeArray.add(siteAttributeValVO);
	        selectedSiteAttrValues[0] = siteAttributeValVO;
	        
	        if(selectedAttrVal != null)
	        {
	            selectedAttrVal.put(new Integer(attrID), selectedSiteAttrValues);
	        }
	        
	        // AC 12.03.08 Disabled logic below.
	        /*if(selectedAttrValForSQL != null)
	        {
	            selectedAttrValForSQL.put(siteAttrVO, selectedAttributeArray);
	        }*/
		}
       
	}
	// CAP-46543 TH END
}
