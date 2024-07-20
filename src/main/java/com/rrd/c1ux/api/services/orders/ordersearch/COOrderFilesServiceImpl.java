
/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            	Description
 *	--------    -----------         ----------      	------------------------------------
 *  01/03/23	Sakthi M        	CAP-37757	       	Created class for the CAP-37757 Code copied from existing Custompoint source
 *  01/12/23  	S Ramachandran		CAP-37781       	Get Order file email details
 *  03/21/23    Satishkumar A       CAP-38738           API Standardization - Order Files response in Order Search conversion to standards
 *  05/29/23	C Codina			CAP-39338			API Change - Header labels in Order File list API to make/use translation text values
 *  03/11/24	N Caceres			CAP-47732			Show a file link to the distribution list
 * 	03/13/24	S Ramachandran		CAP-47841			Added service method to download Order file from order in Order Search
 * 	05/02/24	Krishna Natarajan	CAP-49115			Changed the BU ID parameter of populating the List object
 *	05/17/24	L De Leon			CAP-49280			Modified getOrderFilesContent() to populate EFD fields
 *	06/12/24	Krishna Natarajan	CAP-49128			Modified code to set list name and link empty, based on the settings
 *	06/13/24	Krishna Natarajan	CAP-49128			Changed the default translation to the emailsOrderFilesPopUpTitle as default causes change in header
*/

package com.rrd.c1ux.api.services.orders.ordersearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
//CAP-16460 Import logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.controllers.orders.DownloadOrderFileResponse;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderEmailDetails;
import com.rrd.c1ux.api.models.orders.ordersearch.COOrderFileResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.GenericEncodedRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.OrderFileEmailDetailResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.SiteComponentLocatorService;
import com.rrd.c1ux.api.services.locators.ManageListsLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.file.CPFileReader;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderentry.entity.EFDTracking;
import com.rrd.custompoint.orderentry.entity.List;
import com.rrd.custompoint.orderentry.entity.Order;
import com.rrd.custompoint.orderentry.entity.OrderLine;
import com.rrd.custompoint.orderentry.entity.emails.WCSSEmailTracking;
import com.wallace.atwinxs.admin.util.LookAndFeelFeature;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.admin.vo.SiteVOKey;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CPUrlBuilder;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.ISite;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.lists.vo.ListVOKey;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.EFDDestinationOptionsVO;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSession;
import com.wallace.atwinxs.orderstatus.session.OrderStatusSessionBean;
import com.wallace.atwinxs.otherservices.util.OtherServicesConstants;

@Service
public class COOrderFilesServiceImpl extends BaseService implements COOrderFilesService
{
	private static final String PARAM_SALES_REF_NUM = "salesReferenceNumber";

	private final SiteComponentLocatorService siteComponentLocatorService;
	private final ManageListsLocatorService manageListsLocatorService;
	private final OEManageOrdersComponentLocatorService oeManageOrdersLocatorService;

	protected COOrderFilesServiceImpl(TranslationService translationService, 
			ObjectMapFactoryService objectMapFactoryService,
			SiteComponentLocatorService siteComponentLocatorService,
			ManageListsLocatorService manageListsLocatorService,
			OEManageOrdersComponentLocatorService oeManageOrdersLocatorService) {
		
		super(translationService, objectMapFactoryService);
		this.siteComponentLocatorService = siteComponentLocatorService;
		this.manageListsLocatorService = manageListsLocatorService;
		this.oeManageOrdersLocatorService = oeManageOrdersLocatorService;
	}

	private static final long serialVersionUID = -6414188403686719856L;
	
	public static final String PROP_BUNDLE_ORDER_STATUS = "orderstatus";//CP-13507
	
	//CAP-16460 create logger
	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	@Override
	public COOrderFileResponse getOrderFilesContent(SessionContainer sc, String salesRefNum, String orderNumber) throws AtWinXSException
	{
		ArrayList<COOrderEmailDetails> emailList=new ArrayList<COOrderEmailDetails>();
		COOrderFileResponse response=new COOrderFileResponse();

		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		
		boolean isEmailVisibilityEnabled = appSessionBean.isShowFeature(LookAndFeelFeature.FeatureNames.UXEV.toString(), 
										   Util.ContextPath.Usability.getContextPath());
		
		if(isEmailVisibilityEnabled)
		{
			populateEmailContents(salesRefNum, appSessionBean, orderNumber,emailList);
			response.setIsShowEmailPanel("Y");
		}
		else
		{
			response.setIsShowEmailPanel("N");
		}
		//CAP-39338
		StringBuilder sb = new StringBuilder();
		sb.append(getTranslation(appSessionBean, SFTranslationTextConstants.EMAIL_ORDER_FILES_TITLE, //CAP-49128
				SFTranslationTextConstants.EMAIL_ORDER_FILES_TITLE)).append(AtWinXSConstant.BLANK_SPACE).append(salesRefNum);
		response.setHeaderTxt(sb.toString());
		response.setEmailsHeaderTxt(ModelConstants.HEADER_EMAIL_TEXT);
	    response.setCoOrderEmailDetailsList(emailList);
	    
	    // CAP-47732
	    encryptURLForListFile(salesRefNum, response, appSessionBean, sc);
	   
		populateEFDContents(salesRefNum, response, appSessionBean); // CAP-49280

	return response;
	}

	// CAP-49280
	protected void populateEFDContents(String salesRefNum, COOrderFileResponse response, AppSessionBean appSessionBean)
			throws AtWinXSException {
		try {
			Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class,
					appSessionBean.getCustomToken());
			order.populateBySalesRefNumber(salesRefNum);

			response.setHasEfd(order.isEfdOrder());

			EFDTracking efdTracking = order.getEFDTracking();

			if (order.isEfdOrder() && null != efdTracking) {
				boolean isDice = efdTracking.getEmailAddress().equals(ModelConstants.DICE);
				String email = getEmailForDice(appSessionBean, order, isDice);

				Map<String, String> efdMapContents = efdTracking.getEFDContents(salesRefNum,
						appSessionBean.getSiteLoginID(), isDice, appSessionBean.getSiteID(), email);

				response.setEfdEmailLink(Util.nullToEmpty(efdMapContents.get(ModelConstants.LINK_TO_EMAIL)));

				if (!isDice) {
					response.setEfdLandingPageLink(
							Util.nullToEmpty(efdMapContents.get(ModelConstants.LINK_TO_LANDING_PAGE)));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	// CAP-49280
	protected String getEmailForDice(AppSessionBean appSessionBean, Order order, boolean isDice)
			throws AtWinXSException {
		String email = AtWinXSConstant.EMPTY_STRING;
		if (isDice) {
			IOEManageOrdersComponent ordersService = oeManageOrdersLocatorService
					.locate(appSessionBean.getCustomToken());
			for (OrderLine orderLine : order.getOrderLines().getOrderLines()) {
				EFDDestinationOptionsVO efdDestinationVO = ordersService
						.getEFDDestination(orderLine.getOrderID(), orderLine.getOrderLineNr());
				if (null != efdDestinationVO && !Util.isBlankOrNull(efdDestinationVO.getEmailOthers())) {
					email = efdDestinationVO.getEmailOthers();
					break;
				}
			}
		}
		return email;
	}

	// CAP-47732
	private void encryptURLForListFile(String salesRefNum, COOrderFileResponse response,
			AppSessionBean appSessionBean, SessionContainer sc) throws AtWinXSException {
		Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class, appSessionBean.getCustomToken());
	    order.populateBySalesRefNumber(salesRefNum);
		
	    if (order.getListID() > AtWinXSConstant.INVALID_ID) {
			List list = objectMapFactoryService.getEntityObjectMap().getEntity(List.class, appSessionBean.getCustomToken());
		    list.populate(appSessionBean.getSiteID(), order.getBuID(), order.getListID());//CAP-49115
			
			CPUrlBuilder urlBuilder = new CPUrlBuilder();
		    urlBuilder.setBaseUrl(RouteConstants.DOWNLOAD_ORDER_FILES);
		    urlBuilder.addParameter(PARAM_SALES_REF_NUM, salesRefNum);
		    
		    OrderStatusSession sess = (OrderStatusSession) sc.getModuleSession();
			OrderStatusSessionBean osBean = sess.getOrderStatusSettingsBean();
		    
			if (osBean.isShowListSearch()) {// CAP-49128
				response.setOrderListFileName(list.getListName());
				if (!list.isPrivate() || (list.isPrivate() && list.getLoginID().equals(appSessionBean.getLoginID()))) {
					response.setOrderListLink(urlBuilder.getUrl(true));
				} else {
					response.setOrderListLink(AtWinXSConstant.EMPTY_STRING);
				}
			} else {
				response.setOrderListNoEmailMsg(translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), "noOrderFilesErrMsg"));
				response.setOrderListFileName(AtWinXSConstant.EMPTY_STRING);
				response.setOrderListLink(AtWinXSConstant.EMPTY_STRING);
			}
		}
	}


	//CAP-19873 SRN START
	/**
	 * Method that gets the timestamp and content for each email
	 * @param salesRefNum
	 * @param map
	 * @param appSessionBean
	 * @param orderNumber
	 */
	public void populateEmailContents(String salesRefNum, AppSessionBean appSessionBean, String orderNumber,ArrayList<COOrderEmailDetails> emailList)
	{
		//Get the number of days before an email is archived from the property file
		int numOfDaysToArchiveEmail = Util.getNumberOfDaysToArchiveEmail();

		try 
		{
			//call dao here to retrieve from xst489 table
			WCSSEmailTracking emaiTracking = ObjectMapFactory.getEntityObjectMap().getEntity(WCSSEmailTracking.class, appSessionBean.getCustomToken());
			emaiTracking.setSalesRefNum(salesRefNum);
			Collection<WCSSEmailTracking> emails = emaiTracking.getEmails(numOfDaysToArchiveEmail);

			//add each to the List
			if(null != emails && !emails.isEmpty())
			{
				putEmailsToTheList(appSessionBean, emails,emailList);
			}
		} 
		catch (AtWinXSException e) 
		{
			logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
		}
	}

	/**
	 * Method that puts each email to the main map
	 * @param map
	 * @param appSessionBean
	 * @param emails
	 * @throws AtWinXSException 
	 */
	protected void putEmailsToTheList(AppSessionBean appSessionBean,
			Collection<WCSSEmailTracking> emails,ArrayList<COOrderEmailDetails> emailList) throws AtWinXSException 
	{
		//CAP-20383 SRN Added new variables for indexing
		int confirmIndex = 0;
		int routingIndex = 0;
		int deniedIndex = 0;
		int shippingIndex = 0;
		int creditCardIndex = 0;
		int purchaseIndex = 0;
		
		for(WCSSEmailTracking email : emails)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", appSessionBean.getDefaultLocale());
			String timestamp = formatter.format(email.getCreateTimestamp());
			COOrderEmailDetails coEmail=new COOrderEmailDetails();
			//CAP-19880
			String filePath = email.getFullEmailPath(appSessionBean);
			String linkToEmail = AtWinXSConstant.EMPTY_STRING;
			CPUrlBuilder linkUrl = null;
			
			//If filePath blank, File doesn't exist.
			if (!Util.isBlankOrNull(filePath))
			{
				try 
				{
					linkUrl = email.createEmailLink(filePath, appSessionBean);
					
					if(linkUrl != null)
					{
						linkToEmail = linkUrl.getUrl();
					}
				} 
				catch (AtWinXSException e) 
				{
					logger.error(this.getClass().getName() + " - " + e.getMessage(),e);
				}
			
				//CAP-20383 SRN Added index as the keys
				if(OrderEntryConstants.EMAIL_TYPE_ORDER_CONFIRM.equalsIgnoreCase(email.getTypeCode().trim()))
				{
					coEmail.setEmailCode(OrderEntryConstants.EMAIL_TYPE_CONFIRMATION_KEY + confirmIndex);
					coEmail.setEmailTime(timestamp);
					coEmail.setEmailDescription(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "orderConfirmationEmailLbl"));
					coEmail.setLinkUrl(ModelConstants.HTTPS_PROTOCAL_DESC + AppProperties.getServerName()+linkToEmail);
			
					confirmIndex++;
				}
				else if(OrderEntryConstants.EMAIL_TYPE_ROUTING.equalsIgnoreCase(email.getTypeCode().trim()))
				{
					coEmail.setEmailCode(OrderEntryConstants.EMAIL_TYPE_ROUTE_KEY + routingIndex);
					coEmail.setEmailTime(timestamp);
					coEmail.setEmailDescription(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "routedOrderEmailLbl"));
					coEmail.setLinkUrl(ModelConstants.HTTPS_PROTOCAL_DESC + AppProperties.getServerName()+linkToEmail);
					
					routingIndex++; 
				}
				else if(OrderEntryConstants.EMAIL_TYPE_DENIED.equalsIgnoreCase(email.getTypeCode().trim()))
				{
					coEmail.setEmailCode(OrderEntryConstants.EMAIL_TYPE_DENIED_KEY + deniedIndex);
					coEmail.setEmailTime(timestamp);
					coEmail.setEmailDescription(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "order_req_denied_lbl"));
					coEmail.setLinkUrl(ModelConstants.HTTPS_PROTOCAL_DESC + AppProperties.getServerName()+linkToEmail);
		
					deniedIndex++;
				}
				//CAP-19883 SRN Added ASN email to the map
				else if(OrderEntryConstants.EMAIL_TYPE_SHIPPING_NOTIF.equalsIgnoreCase(email.getTypeCode().trim()))
				{
					coEmail.setEmailCode(OrderEntryConstants.EMAIL_TYPE_SHIPPING_NOTIF_KEY + shippingIndex);
					coEmail.setEmailTime(timestamp);
					coEmail.setEmailDescription(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "asnEmailLbl"));
					coEmail.setLinkUrl(ModelConstants.HTTPS_PROTOCAL_DESC + AppProperties.getServerName()+linkToEmail);
					
					shippingIndex++;
				}
				//CAP-19884 SRN Added Credit Card email to the map
				else if(OrderEntryConstants.EMAIL_TYPE_CREDIT_CARD.equalsIgnoreCase(email.getTypeCode().trim()))
				{
					coEmail.setEmailCode(OrderEntryConstants.EMAIL_TYPE_CREDIT_CARD_KEY + creditCardIndex);
					coEmail.setEmailTime(timestamp);
					coEmail.setEmailDescription(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "asnEmailLbl"));
					coEmail.setLinkUrl(ModelConstants.HTTPS_PROTOCAL_DESC + AppProperties.getServerName()+linkToEmail); 
				
					creditCardIndex++;
				}
				//CAP-19882 Added Purchase Order email to the map
				else if(OrderEntryConstants.EMAIL_TYPE_PURCHASE_ORDER.equalsIgnoreCase(email.getTypeCode().trim()))
				{
					coEmail.setEmailCode(OrderEntryConstants.EMAIL_TYPE_PURCHASE_ORDER_KEY + purchaseIndex);
					coEmail.setEmailTime(timestamp);
					coEmail.setEmailDescription(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "poackEmailLbl"));
					coEmail.setLinkUrl(ModelConstants.HTTPS_PROTOCAL_DESC + AppProperties.getServerName()+linkToEmail); 
					purchaseIndex++;
				}
				
				emailList.add(coEmail);
			}
			
		}
	}
	//CAP-19873 SRN END
	
	
	/**
	 * Method that puts each email to the main map
	 * @param sc {@link SessionContainer}
	 * @param genericEncodedRequest {@link SessionContainer}
	 * @return orderFileEmailDetailResponse {@link OrderFileEmailDetailResponse}
	 * @throws AtWinXSException 
	 */
	//CAP-37781 - Service method to get order file email details. Method Logic copied from CP OrderEmailAjaxController.buildEmail
	public OrderFileEmailDetailResponse buildEmail(SessionContainer sc, GenericEncodedRequest genericEncodedRequest) throws AtWinXSException
	{
		
		OrderFileEmailDetailResponse orderFileEmailDetailResponse = new OrderFileEmailDetailResponse();
		//CAP-38738
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		try
		{
			//pull out "String a" from request, and decrypt via the decryptString() method.
		    String encryptedParms = genericEncodedRequest.getA();
		    
		    //decodeURL to replace %
		    encryptedParms = Util.decodeURL(encryptedParms);
		    encryptedParms = Util.replace(encryptedParms, " ", "+");
		    
		    String decryptedParms = Util.decryptString(encryptedParms);    	        	    
		    
		    // Get all the parameters for the action here
		    Map<String, String> parmList = Util.parseQueryString(decryptedParms);
		    String filePath = parmList.get(OtherServicesConstants.PARM_FILE_PATH);
	    
		
			getHTMLEmailInfo(sc, orderFileEmailDetailResponse, parmList);
			
			//read file based on filepath
			File file = new File(filePath);
			CPFileReader reader = new CPFileReader(file);
			
			String htmlFileText = reader.readAll();
			orderFileEmailDetailResponse.setEmailHtml(htmlFileText);
			
			orderFileEmailDetailResponse.setStatus("Success");
			orderFileEmailDetailResponse.setErrorMessage("Order file email details retrieval success");
			//CAP-38738
			orderFileEmailDetailResponse.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.EMAIL_RETRIVAL_SUCCESS));
			orderFileEmailDetailResponse.setSuccess(true);
			
			return orderFileEmailDetailResponse;
	    }
		catch(Exception e)
		{
			logger.error("Failed in COOrderFilesServiceImpl.buildEmail " + e.getMessage(), e); // CAP-16459
			orderFileEmailDetailResponse.setStatus("Fail");
			orderFileEmailDetailResponse.setErrorMessage("Request could not be completed at this time.");
			//CAP-38738
			orderFileEmailDetailResponse.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), RouteConstants.CANNOT_COMPLETE_REQUEST));
			orderFileEmailDetailResponse.setSuccess(false);

		}
		return orderFileEmailDetailResponse;
	}
	
	//CAP-37781 - to retrieves order file email details
	private OrderFileEmailDetailResponse getHTMLEmailInfo(SessionContainer sc, OrderFileEmailDetailResponse orderFileEmailDetailResponse, Map<String, String> parmList) throws AtWinXSException
	{
		
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		String fromAddress = parmList.get(OtherServicesConstants.PARM_FROM_ADDR);
	    String replyToAddress = parmList.get(OtherServicesConstants.PARM_REPLY_ADDR);
	    String emailAddress = parmList.get(OtherServicesConstants.PARM_EMAIL_ADDR);
	    String emailSubject = parmList.get(OtherServicesConstants.PARM_EMAIL_SUBJ);
	    
	  	String emailInfoLbl = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),TranslationTextConstants.TRANS_EMAIL_INFO_LBL);
	  	orderFileEmailDetailResponse.setHeaderLabel(emailInfoLbl);

		if(!Util.isBlankOrNull(fromAddress))
		{
			orderFileEmailDetailResponse.setFromLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "newFromAddrLbl", null));
			orderFileEmailDetailResponse.setFromValue(Util.replace(fromAddress, ",", ", "));
		}
		if(!Util.isBlankOrNull(replyToAddress))
		{
			orderFileEmailDetailResponse.setReplyLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "newReplyToAddrLbl", null));
			orderFileEmailDetailResponse.setReplyValue(Util.replace(replyToAddress, ",", ", "));
		}
		if(!Util.isBlankOrNull(emailAddress))
		{
			orderFileEmailDetailResponse.setToLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "emailToLbl", null));
			orderFileEmailDetailResponse.setToValue(Util.replace(emailAddress, ",", ", "));
		}
		if(!Util.isBlankOrNull(emailSubject))
		{
			orderFileEmailDetailResponse.setSubjectLabel(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "newEmailSubjLbl", null));
			orderFileEmailDetailResponse.setSubjectValue(StringEscapeUtils.escapeHtml(emailSubject));
		}
		return orderFileEmailDetailResponse;
	}
	
	//CAP-47841
	@Override
	public DownloadOrderFileResponse downloadOrderFileFromOS(SessionContainer sc, 
			HttpServletResponse httpServletResponse, String encryptedParms) throws AtWinXSException {
	
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		
		String errorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(), 
				appSessionBean.getCustomToken(), 
				SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_KEY);
		
		DownloadOrderFileResponse response = new DownloadOrderFileResponse();
		response.setSuccess(true);

		try
		{
			
			//Get any encrypted parameters here
			if (!Util.isBlankOrNull(encryptedParms)) {
				
				//decodeURL to replace %
				encryptedParms = Util.decodeURL(encryptedParms);
				encryptedParms = Util.replace(encryptedParms, " ", "+");
		    	String decryptedParms = Util.nullToEmpty(Util.decryptString(encryptedParms)); 
		    	Map<String, String> parameters = Util.parseQueryString(decryptedParms);
		    	
		    	//Get sales reference Number from parameter
		    	if(!parameters.isEmpty() && !Util.isBlankOrNull(parameters.get(PARAM_SALES_REF_NUM))) {
		    		
		    		String salesRefNumber = parameters.get(PARAM_SALES_REF_NUM);
		    		processOrderFile(salesRefNumber, httpServletResponse, appSessionBean, errorMessage);
		    	} 
		    	else {
		    	
		    		throw new AccessForbiddenException(errorMessage, AtWinXSConstant.EMPTY_STRING);
		    	}
			} else {
				
				throw new AccessForbiddenException(errorMessage, AtWinXSConstant.EMPTY_STRING);
			}
		} catch (AtWinXSException | IOException e) {
			
			logger.error(e.getMessage(), e);
			throw new AccessForbiddenException(errorMessage, AtWinXSConstant.EMPTY_STRING);
		}
	
		return response;
	}
	
	//CAP-47841
	public void processOrderFile(String salesRefNumber, HttpServletResponse httpServletResponse, AppSessionBean appSessionBean, 
			String errorMessage) throws AtWinXSException, IOException {

    	Order order = objectMapFactoryService.getEntityObjectMap().getEntity(Order.class, appSessionBean.getCustomToken());
	    order.populateBySalesRefNumber(salesRefNumber);
		
	    if (order.getListID() > AtWinXSConstant.INVALID_ID && 
	    		order.getSiteID() == appSessionBean.getSiteID() &&
	    				order.getBuID()	== appSessionBean.getBuID()) {

		    ListVO list = getList(order.getListID(), appSessionBean);
			// make sure that user owns the list or it is shared
			if ( list != null && 
					(list.getLoginID().equals(appSessionBean.getLoginID()) || !list.isPrivate())) {
				
				SiteVO site = getSite(appSessionBean);
				
				// use actual file name as SourceFileName(CSV) if exists else OrigExcelFileName (XLS)   
				String actualFileName = Util.isBlankOrNull(list.getOrigExcelFileName()) ? list.getSourceFileName() : list.getOrigExcelFileName();

				// compose file path with file name 
				String listFileName = getFileFullPath(site.getSiteLoginID()) + actualFileName;
				
				String filename = list.getCustomerFileName();
				File orderFile = new File(listFileName);

				writeToOutPutStreamForFileDownload(httpServletResponse, orderFile, filename);
			}
			else {
				
				throw new AccessForbiddenException(errorMessage, AtWinXSConstant.EMPTY_STRING);
			}
			
	    }
	    else {
	    	
	    	throw new AccessForbiddenException(errorMessage, AtWinXSConstant.EMPTY_STRING);
	    }
	}
	
	//CAP-47841
	private String getFileFullPath(String siteLoginID) 	{
		
		String path = Util.nullToEmpty(AppProperties.getGlobalFileUploadPath());
		return Util.replace(path, AtWinXSConstant.SITE_LOGIN_ID_REPLACE_TXT, siteLoginID);
	}		
	
	//CAP-47841
	protected SiteVO getSite(AppSessionBean appSessionBean) throws AtWinXSException {
		
		ISite siteComp = siteComponentLocatorService.locate(appSessionBean.getCustomToken());
		return siteComp.getSite(new SiteVOKey(appSessionBean.getSiteID()));
	}
	
	//CAP-47841
	protected ListVO getList(int listID, AppSessionBean appSessionBean) throws AtWinXSException {
		
		IManageList listComp = manageListsLocatorService.locate(appSessionBean.getCustomToken());
		return listComp.retrieveAList(new ListVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(), listID));
	}

	//CAP-47841
	protected void writeToOutPutStreamForFileDownload(HttpServletResponse httpServletResponse, File fileToDownload, String filename) throws IOException {

		httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		httpServletResponse.setHeader("Cache-Control","private");
		httpServletResponse.setHeader("Pragma", "private");
			
		FileInputStream in = getFileInputStream(fileToDownload);
		ServletOutputStream out = httpServletResponse.getOutputStream();
		byte[] bytes = new byte[AtWinXSConstant.TWO_KB];
		int bytesRead;

		// Loop through our file and stream to the user here
		while((bytesRead = in.read(bytes)) != -1) {
				out.write(bytes, 0, bytesRead);
		}
		out.flush();
	}	

}
