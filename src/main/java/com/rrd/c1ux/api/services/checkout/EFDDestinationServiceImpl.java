/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By				JIRA#			Description
 * 	--------	-----------				----------		--------------------------------
 *	05/13/24	Ramachandran S			CAP-49326		Initial Version
 *	05/13/24	N Caceres				CAP-49344		Get EFD Options API
 *	05/16/24	Satishkumar A			CAP-49311		C1UX BE - Create new API to save EFD information
 *	05/30/24	Krishna Natarajan		CAP-49748		Added few email flags to the getEFDOptions - removed Email required validation for CAP-49172
 *	05/29/24	Krishna Natarajan		CAP-49326		Removed the decryption for the emailStyleID
 *  05/31/24	Krishna Natarajan		CAP-49814		Added email source type map value to the getEFDOptions
 *  06/05/24	Krishna Natarajan		CAP-49974 		Removed the empty email message check - which is unnecessary
 *  06/06/24	Krishna Natarajan		CAP-49903		Added line of code to check CP itemImageURL and replace with the one for SF
 *  06/10/24	Krishna Natarajan		CAP-50078		Added code to update with FTP delivery method in XST239
 *  06/26/24	Krishna Natarajan		CAP-50547		Added a method to loop through all the lines to getEFDSources - 
 *  06/26/24	Krishna Natarajan		CAP-50435		Handle the site and BU ID if the intActiveEmailStyleID is 0
 *  06/28/24	Krishna Natarajan		CAP-50547		Handle code to set efddestiantion to FTP - if empty/only FTP
 */

package com.rrd.c1ux.api.services.checkout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.checkout.EFDLineItem;
import com.rrd.c1ux.api.models.checkout.EFDOptionsResponse;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationRequest;
import com.rrd.c1ux.api.models.checkout.EFDStyleInformationResponse;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationRequest;
import com.rrd.c1ux.api.models.checkout.SaveEfdInformationResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.orderentry.locator.OECustomEmailComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.ao.EFDDestinationOptionsFormBean;
import com.rrd.custompoint.orderentry.entity.EFDCRMTracking;
import com.rrd.custompoint.orderentry.entity.EFDCRMTrackingRecord;
import com.rrd.custompoint.orderentry.entity.EFDCartLine;
import com.wallace.atwinxs.catalogs.vo.CatalogDefaultVO;
import com.wallace.atwinxs.catalogs.vo.EFDSourceSetting;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICustomEmail;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.orderentry.admin.ao.ExtendedEmailStyleBean;
import com.wallace.atwinxs.orderentry.admin.ao.OACustomEmailAssembler;
import com.wallace.atwinxs.orderentry.admin.ao.OACustomEmailFormBean;
import com.wallace.atwinxs.orderentry.admin.vo.EmailStyleVO;
import com.wallace.atwinxs.orderentry.admin.vo.EmailStyleVOKey;
import com.wallace.atwinxs.orderentry.ao.EFDDestinationsFormBean;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.EFDDestinationOptionsVO;

@Service
public class EFDDestinationServiceImpl extends BaseOEService implements EFDDestinationService {

	private static final String FTP = "FTP";
	private static final String COMMA = ",";
	private static final String BACK = "BACK";
	private static final String INIT = "INIT";
	private static final Logger logger = LoggerFactory.getLogger(EFDDestinationServiceImpl.class);
	
	private final OECustomEmailComponentLocatorService oeCustomEmailComponentLocatorService;
	private final OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService;
	private static final String STYLE_ID_URL_PARAM = "&styleID=";
	
	private static final String EMAIL_MESSAGE = "emailMessage";
	private static final String EMAIL_ADDRESSES = "emailAddresses";


	protected EFDDestinationServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFactoryService, 
			OECustomEmailComponentLocatorService oeCustomEmailComponentLocatorService,
			OEManageOrdersComponentLocatorService oeManageOrdersComponentLocatorService) {
		super(translationService, objectMapFactoryService);
		this.oeCustomEmailComponentLocatorService = oeCustomEmailComponentLocatorService;
		this.oeManageOrdersComponentLocatorService = oeManageOrdersComponentLocatorService;
		
	}

	// CAP-49326
	@Override
	public EFDStyleInformationResponse getEfdStyleInformationForOrder(SessionContainer sc,
			EFDStyleInformationRequest request) throws AtWinXSException {

		EFDStyleInformationResponse response = new EFDStyleInformationResponse();

		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean vsb = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean osb = oeSession.getOESessionBean();

		// throw 422 error if order is not available 
		if (!validateOrder(response, sc, asb)) {
			return response;
		}
		
		// throw 422 error if EFD NOT Allowed
		if(!osb.getUserSettings().isAllowEFD()) {
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_NOT_ALLOWED, asb, null));
			return response;
		}

		// throw 422 error if Style ID is Not Numeric
		if (Util.isBlankOrNull(request.getStyleID())) {

			response.getFieldMessages().put(ModelConstants.STYLE_ID_FIELDNAME, AtWinXSConstant.BLANK_SPACE
					+ buildTranslationMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			response.setSuccess(false);
			return response;
		}

		try {

			// Decrypt and get active Email Style ID
			int	intActiveEmailStyleID = Integer.parseInt(request.getStyleID());//CAP-49326
			EmailStyleVOKey voKeyStyle = new EmailStyleVOKey(osb.getSiteID(),
					osb.getBusinessUnitID(), OrderAdminConstants.EmailTypeEnum.EFD.getTypeCode(),
					intActiveEmailStyleID);
	
			ICustomEmail compLocator = oeCustomEmailComponentLocatorService.locate(asb.getCustomToken());
			Collection<EmailStyleVO>  colActiveEmailStyle = compLocator.retrieveAllEmailStyles(voKeyStyle);
			EmailStyleVO activeEmailStyle = compLocator.getEmailStyleByID(voKeyStyle);

			if(checkStyleIdExist(colActiveEmailStyle, intActiveEmailStyleID)) {
			
				ExtendedEmailStyleBean emailStyleBean = buildEmailStyleBeans(asb, vsb, oeSession, osb, activeEmailStyle);
				
				response.setFromName(emailStyleBean.getFromNameDisplay());
				response.setReplyTo(emailStyleBean.getReplyToAddressDisplay());
				response.setEmailSubject(emailStyleBean.getEmailSubjectDisplay());
				response.setEmailContent(emailStyleBean.getEmailContent());
				response.setEmailStyle(emailStyleBean.getEmailStyleName());
				response.setShowEmailMessage(emailStyleBean.isAllowEfdMsg());
				response.setSuccess(true);
			} 
			else { 
				response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_STYLE_INFO_ERR, asb, null));
				response.setSuccess(false);
			}
		} catch (NumberFormatException e) {

			logger.error("EFD - getEfdStyleInformationForOrder(), non-numeric Style ID", e);
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_STYLE_INFO_ERR, asb, null));
			return response;
		} catch (Exception e) {

			logger.error("EFD - getEfdStyleInformationForOrder(), unable to retrieve Style Info", e);
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_STYLE_INFO_ERR, asb, null));
			return response;
		}
		return response;
	}
	
	
	// CAP-49326 - Decrypt and return Email Style ID 
	public int decryptAndGetStyleID(String  encActiveEmailStyleID) throws NumberFormatException {
		
		String strActiveEmailStyleID = Util.decryptString(encActiveEmailStyleID);
		strActiveEmailStyleID = strActiveEmailStyleID.replace(STYLE_ID_URL_PARAM, "");
		return Integer.parseInt(strActiveEmailStyleID);
	}
	
	// CAP-49326 - check active Style ID exist for Site ID, BUID, EmailTypeEnum 
	public boolean checkStyleIdExist(Collection<EmailStyleVO> colActiveEmailStyle, int intActiveEmailStyleID)
			throws NumberFormatException {

		boolean flagStyleIdExist = false;
		Iterator<EmailStyleVO> lstEmailStyleVO = colActiveEmailStyle.iterator();
		while (lstEmailStyleVO.hasNext()) {

			EmailStyleVO emailStyle = lstEmailStyleVO.next();
			if (emailStyle.getEmailStyleId() == intActiveEmailStyleID) {
				flagStyleIdExist = true;
				break;
			}
		}
		return flagStyleIdExist;
	}

	// CAP-49326 - common method to build error message
	private String buildTranslationMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {

		return Util.nullToEmpty(
				translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), errorKey, replaceMap));
	}
	
	// CAP-49326 - Copied from CP =>OECheckoutAssembler
	public ExtendedEmailStyleBean buildEmailStyleBeans(AppSessionBean asb, VolatileSessionBean vsb, OrderEntrySession oeSession,
			OEOrderSessionBean osb, EmailStyleVO emailStyle)
			throws AtWinXSException {
		
		EFDDestinationsFormBean formBean = new EFDDestinationsFormBean();
		List<String> errMsgs = new ArrayList<>();
		OECheckoutAssembler checkoutAssembler = new OECheckoutAssembler(vsb, asb.getCustomToken(), 
				asb.getDefaultLocale(), asb.getApplyExchangeRate(), asb.getCurrencyLocale());

		EFDDestinationOptionsFormBean oldFormBean = checkoutAssembler.getEFDFormInfo(asb, osb, vsb.getOrderId().intValue());
		formBean.populate(vsb, asb,  osb, oldFormBean, errMsgs, checkoutAssembler, oeSession);
		
		OACustomEmailAssembler assembler = new OACustomEmailAssembler(asb.getCustomToken(), asb.getDefaultLocale());
		OACustomEmailFormBean emailFormBean = assembler.getDefaultEfdEmail(AtWinXSConstant.DEFAULT_SITE_ID,
				AtWinXSConstant.DEFAULT_BU_ID);
		ExtendedEmailStyleBean emailStyleDefaultBean = getEmailStyleDefaultBean(asb, emailFormBean);
		ExtendedEmailStyleBean emailStyleBean = getEmailStyleBeanCollection(asb, emailStyle, emailStyleDefaultBean);
		
		prepareFromNameDisplayValues(asb, formBean, emailStyleBean);
		prepareReplyToDisplayValues(asb, formBean,emailStyleBean);
		prepareEmailSubjectDisplayValues(formBean,emailStyleBean);
		prepareEmailContentDisplayValues(formBean, emailStyleBean); 
		
		return emailStyleBean;
	}	
	
	// CAP-49326 - Copied from CP =>OECheckoutAssembler 
	public ExtendedEmailStyleBean getEmailStyleDefaultBean(AppSessionBean asb, OACustomEmailFormBean emailFormBean) throws AtWinXSException {	

		ExtendedEmailStyleBean emailStyleDefaultBean = new ExtendedEmailStyleBean();
		emailStyleDefaultBean.setEmailStyleId(emailFormBean.getEmailStyleId());
		emailStyleDefaultBean.setEmailStyleName(emailFormBean.getEmailStyleName());
		emailStyleDefaultBean.setFromName(emailFormBean.getEmailFromName());
		emailStyleDefaultBean.setReplyToAddress(emailFormBean.getEmailReplyToAddress());
		emailStyleDefaultBean.setEmailSubject(emailFormBean.getEmailSubject());
		emailStyleDefaultBean.setEmailSubjectDisplay(emailFormBean.getEmailSubject()); 
		emailStyleDefaultBean.setEmailContent(buildTranslationMessage("emailContentDefault", asb, null)); 
		emailStyleDefaultBean.setAllowOverride(emailFormBean.isAllowOverride());
		emailStyleDefaultBean.setEmailBody(Util.encodeQuotesForHTMLtoJS(Util.nullToEmpty(emailFormBean.getEmailBody())));
		emailStyleDefaultBean.setEmailHyperLink(emailFormBean.getEmailHyperLink());		 
		
		return emailStyleDefaultBean;
	}
	
	// CAP-49326 - Copied logics from CP=>OECheckoutAssembler  
	public ExtendedEmailStyleBean getEmailStyleBeanCollection(AppSessionBean asb, EmailStyleVO emailStyle,
			   ExtendedEmailStyleBean emailStyleDefaultBean) throws AtWinXSException {
		String emailContent = getEmailContent(asb, emailStyle);			  			  			  
		
		ExtendedEmailStyleBean emailStyleBean = new ExtendedEmailStyleBean();
		emailStyleBean.setEmailStyleId(emailStyle.getEmailStyleId());
		emailStyleBean.setEmailStyleName(safeHTMLString(emailStyle.getEmailStyleName()));

		emailStyleBean.setAllowOverride(emailStyle.isAllowOverride());
		
		if(emailStyle.getDfltFromNameInd()) {
			
			emailStyleBean.setFromName(emailStyleDefaultBean.getFromName());  
		}
		else {
			
			emailStyleBean.setFromName(emailStyle.getFromName());  
		}
		
		if(emailStyle.getDfltReplyToAddressInd()) {
			
			emailStyleBean.setReplyToAddress(emailStyleDefaultBean.getReplyToAddress());  
		}
		else {
			
			emailStyleBean.setReplyToAddress(emailStyle.getReplyToAddress());
		}
		
		if(emailStyle.getDfltEmailSubjectInd()) {
			
			emailStyleBean.setEmailSubject(emailStyleDefaultBean.getEmailSubject());
			emailStyleBean.setEmailSubjectDisplay(emailStyleDefaultBean.getEmailSubject());
			emailStyleBean.setEmailSubjectHTMLEncoded(Util.htmlEncodeQuotes(emailStyleDefaultBean.getEmailSubject()));
		}
		else {
			
			emailStyleBean.setEmailSubject(emailStyle.getEmailSubject());
			emailStyleBean.setEmailSubjectDisplay(emailStyle.getEmailSubject());
			emailStyleBean.setEmailSubjectHTMLEncoded(Util.htmlEncodeQuotes(emailStyle.getEmailSubject()));
		}

		if(emailStyle.getDfltEmailBodyInd().equals(OrderAdminConstants.EMAIL_STYLE_BODY_DEFAULT)) {
			
			emailStyleBean.setEmailBody(emailStyleDefaultBean.getEmailBody());
			emailStyleBean.setEmailBodyUnencoded(emailStyleDefaultBean.getEmailBody());
		}
		else {
			
			emailStyleBean.setEmailBody(safeHTMLString(emailStyle.getEmailBody()));
			emailStyleBean.setEmailBodyUnencoded(emailStyle.getEmailBody());
		}
		
		if(emailStyle.getDfltEmailHyperLinkInd()) {
			
			emailStyleBean.setEmailHyperLink(emailStyleDefaultBean.getEmailHyperLink());
			emailStyleBean.setEmailHyperLinkHTMLEncoded(Util.htmlEncodeQuotes(emailStyleDefaultBean.getEmailHyperLink()));
			}
		else {
			
			emailStyleBean.setEmailHyperLink(emailStyle.getEmailHyperLink());
			emailStyleBean.setEmailHyperLinkHTMLEncoded(Util.htmlEncodeQuotes(emailStyle.getEmailHyperLink()));
		}					  
		
		emailStyleBean.setEmailContent(safeHTMLString(emailContent));
		emailStyleBean.setAllowEfdMsg(emailStyle.isAllowEfdMsgInd()); 

		return emailStyleBean;	
	}	

	
	// CAP-49326 - Copied logics from CP Code 
	// EFDDestinationsFormBean=>prepareDisplayValues,
	// EFDDestinationController=>putTranslatedStringsOnModel
	public void prepareFromNameDisplayValues(AppSessionBean asb, EFDDestinationsFormBean formBean,
			ExtendedEmailStyleBean emailStyleBean) throws AtWinXSException {

		HashMap<String, Object> emailReplaceMap = new HashMap<>();
		emailReplaceMap.put("{efdDefaultEmail}", formBean.getOldFormBean().getDefaultSytemEmailFromName());

		if ((!Util.isBlankOrNull(formBean.getFromNameOverride()))) {
			if (OrderAdminConstants.EFD_STANDARD_EMAIL.equals(formBean.getFromNameOverride())) {

				emailStyleBean.setFromNameDisplay(buildTranslationMessage(
						TranslationTextConstants.TRANS_NM_SYSTEM_DEFAULT_OPTION, asb, emailReplaceMap));
			} else {

				emailStyleBean.setFromNameDisplay(formBean.getFromNameOverride());
			}
		} else if (OrderAdminConstants.EFD_STANDARD_EMAIL.equals(emailStyleBean.getFromName())) {

			emailStyleBean.setFromNameDisplay(
					buildTranslationMessage(TranslationTextConstants.TRANS_NM_SYSTEM_DEFAULT_OPTION, asb, emailReplaceMap));
		}
	}	
	
	// CAP-49326 - Copied logics from CP code 
	// EFDDestinationsFormBean=>prepareDisplayValues,
	// EFDDestinationController=>putTranslatedStringsOnModel
	public void prepareReplyToDisplayValues(AppSessionBean asb, EFDDestinationsFormBean formBean,
			ExtendedEmailStyleBean emailStyleBean) throws AtWinXSException {

		HashMap<String, Object> defaultReplyToEmailMap = new HashMap<>();
		defaultReplyToEmailMap.put("{efdDefaultEmail}", OrderAdminConstants.EFD_NO_REPLYTO_TXT);

		if ((!Util.isBlankOrNull(formBean.getReplyToOverride()))) {

			if (OrderAdminConstants.EFD_STANDARD_EMAIL.equals(formBean.getReplyToOverride())) {

				emailStyleBean.setReplyToAddressDisplay(buildTranslationMessage(
						TranslationTextConstants.TRANS_NM_SYSTEM_DEFAULT_OPTION, asb, defaultReplyToEmailMap));
			} else if (OrderAdminConstants.EFD_ORIGINATOR_EMAIL.equals(formBean.getReplyToOverride())) {

				emailStyleBean.setReplyToAddressDisplay(
						buildTranslationMessage(TranslationTextConstants.TRANS_NM_USE_MY_ADDRESS_OPTION, asb, null));
			} else {

				emailStyleBean.setReplyToAddressDisplay(formBean.getReplyToOverride());
			}
		} else if (OrderAdminConstants.EFD_STANDARD_EMAIL.equals(emailStyleBean.getReplyToAddress())) {

			emailStyleBean.setReplyToAddressDisplay(buildTranslationMessage(
					TranslationTextConstants.TRANS_NM_SYSTEM_DEFAULT_OPTION, asb, defaultReplyToEmailMap));
		} else if (OrderAdminConstants.EFD_ORIGINATOR_EMAIL.equals(emailStyleBean.getReplyToAddress())) {

			emailStyleBean.setReplyToAddressDisplay(
					buildTranslationMessage(TranslationTextConstants.TRANS_NM_USE_MY_ADDRESS_OPTION, asb, null));
		}
	}	
	
	// CAP-49326 - Copied logics from CP=>EFDDestinationsFormBean=>prepareDisplayValues
	public void prepareEmailSubjectDisplayValues(EFDDestinationsFormBean formBean,
			ExtendedEmailStyleBean emailStyleBean) {

		if ((!Util.isBlankOrNull(formBean.getEmailSubjectOverride()))) {

			emailStyleBean.setEmailSubjectDisplay(formBean.getEmailSubjectOverride());
		}
	}

	// CAP-49326 - Copied logics from CP=>EFDDestinationsFormBean=>prepareDisplayValues
	public void prepareEmailContentDisplayValues(EFDDestinationsFormBean formBean,
			ExtendedEmailStyleBean emailStyleBean) {

		if ((!Util.isBlankOrNull(formBean.getEmailContentOverride()))) {

			String htmlBodyHeader = formBean.getEmailContentOverride();

			if (htmlBodyHeader != null) {

				boolean regExStandard = htmlBodyHeader.matches("(?s).*\\<body\\>\\s*STANDARD\\s*\\</body\\>.*");
				if (regExStandard) {

					emailStyleBean.setEmailContent("Use Default");
				} else {

					emailStyleBean.setEmailContent("Customized");
				}
			} else {

				emailStyleBean.setEmailContent("Customized");
			}
		}
	}
	
	// CAP-49326 - Copied from CP CP OECheckoutAssembler=>safeHTMLString
	public String safeHTMLString(String param) {
		
		return Util.encodeQuotesForHTMLtoJS(Util.nullToEmpty(param));
	}
	
	// CAP-49326 - Copied from CP OECheckoutAssembler=>getEmailContent
	public String getEmailContent(AppSessionBean asb, EmailStyleVO emailStyle) throws AtWinXSException
	{
		boolean isCustomized = false;		
		String emailContent = "";
		
		if(!emailStyle.getDfltEmailSubjectInd() ||
				!emailStyle.getDfltEmailBodyInd().equals(OrderAdminConstants.EMAIL_STYLE_BODY_DEFAULT) ||
				!emailStyle.getDfltEmailHyperLinkInd() ||
				!emailStyle.getDfltEmailFooterInd()) {
			
			isCustomized = true;
		}
		
		if(isCustomized) {
			
			emailContent = buildTranslationMessage("customize_lbl", asb, null); 
		}
		else {
			
			emailContent = buildTranslationMessage("emailContentDefault", asb, null); 
		}
		
		return emailContent;
	}
	
	// CAP-49344
	public EFDOptionsResponse getEFDOptions(SessionContainer sc) throws AtWinXSException {
		EFDOptionsResponse efdOptionsResponse = new EFDOptionsResponse();
		EFDDestinationsFormBean efdLines = populateEFDLines(sc, efdOptionsResponse);
		for (EFDCartLine efdCartLines : efdLines.getEfdLines()) {//CAP-49903
			if (efdCartLines.getItemImageURL().contains(ModelConstants.CP_NO_IMAGE_NO_CONTEXT)) {
				efdCartLines.setItemImageURL(ModelConstants.C1UX_NO_IMAGE_MEDIUM);
			}
		}

		efdOptionsResponse.setEfdLines(efdLines);
		efdOptionsResponse.setEfdLinesExists(CollectionUtils.isNotEmpty(efdLines.getEfdLines()));
		efdOptionsResponse.setFtpOnly(isFTPOnly(efdLines));
		//CAP-49748
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		efdOptionsResponse.setAllowEditEmail(userSettings.isCanUserEditOwnEmail());
		efdOptionsResponse.setAllowMultipleEmails(checkAllowMultipleEmails(userSettings.isCanUserEditOwnEmail(),
				userSettings.getAllowMultipleEmailsCode()));
		efdOptionsResponse.setDefaultEmailAddress(checkAndGetDefaultEmail(userSettings.getEfdEmailAddress(), sc.getApplicationSession().getAppSessionBean()));
		
		//CAP-49748
		efdOptionsResponse.setEmailSourceTypes(emailSourceTypes(sc.getApplicationSession().getAppSessionBean()));//CAP-49814
		efdOptionsResponse.setSuccess(true);
		return efdOptionsResponse;
	}
	
	protected EFDDestinationsFormBean populateEFDLines(SessionContainer sc, EFDOptionsResponse efdOptionsResponse) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();

		EFDDestinationsFormBean formBean = new EFDDestinationsFormBean();
		Message msg = new Message();
		ArrayList<String> errMsgs = new ArrayList<>();
		OECheckoutAssembler assembler = new OECheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(),
				appSessionBean.getCurrencyLocale());
		EFDDestinationOptionsFormBean oldFormBean = null;

		if (null != volatileSessionBean.getOrderId()) {
			oldFormBean = assembler.getEFDFormInfo(appSessionBean, oeOrderSessionBean,
					volatileSessionBean.getOrderId().intValue());
		}

		if (oldFormBean == null) { // throw error - cannot get EFD lines!
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.ORDER_ID_TAG, volatileSessionBean.getOrderId());
			efdOptionsResponse.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_LOAD_DESTINATION_ERROR, appSessionBean, replaceMap));
			efdOptionsResponse.setSuccess(false);
			msg.setErrGeneralMsg(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), 
					SFTranslationTextConstants.EFD_LOAD_INFO_ERROR));
			throw new AtWinXSMsgException(msg, this.getClass().getName());
		}

		formBean.populate(volatileSessionBean, appSessionBean, oeOrderSessionBean, oldFormBean, errMsgs,
				assembler, sc.getModuleSession());

		// This will set the display mode of Salesforce email to INIT. No Saved emails
		// will be displayed
		formBean.setSalesforceEmailDisplayMode(INIT);

		boolean hasExistingInfo = hasExistingEFDInfo(appSessionBean, volatileSessionBean.getOrderId().intValue());

		// This will load all the previously saved salesforce CRM IDs and emails
		if (hasExistingInfo) {
			// This will set the display mode of Salesforce email to BACK. Previously saved
			// salesforce emails will be displayed
			formBean.setSalesforceEmailDisplayMode(BACK);

			EFDCRMTracking eFDCRMTracking = objectMapFactoryService.getEntityObjectMap().getEntity(EFDCRMTracking.class,
					appSessionBean.getCustomToken());
			eFDCRMTracking.populate(formBean.getOrderID());

			// Get previously saved salesforce emails
			Collection<EFDCRMTrackingRecord> eFDCRMTrackingRecords = eFDCRMTracking.getRecords();
			buildSFEmails(formBean, eFDCRMTrackingRecords);
		}

		if (!errMsgs.isEmpty()) {
			efdOptionsResponse.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), 
					SFTranslationTextConstants.ERRORS_FOUND) + errMsgs);
			efdOptionsResponse.setSuccess(false);
		}

		// CP-12640 ACL Set Default Locale for SF user that doesn't have country
		if (appSessionBean.getDefaultLocale().getISO3Country() != null) {
			formBean.setDefaultLocale(appSessionBean.getDefaultLocale().getISO3Country());
		}

		// CP-13003 ACL Added attribute to determine if coming from Salesforce or not
		formBean.setFromSalesforceEntryPoint(!Util.isBlankOrNull(appSessionBean.getSalesforceSignedRequestJson()));

		return formBean;
	}

	protected void buildSFEmails(EFDDestinationsFormBean formBean,
			Collection<EFDCRMTrackingRecord> eFDCRMTrackingRecords) {
		if (null != eFDCRMTrackingRecords && !eFDCRMTrackingRecords.isEmpty()) {
			StringBuilder salesforceEmails = new StringBuilder();
			StringBuilder salesforceCrmIDs = new StringBuilder();

			for (EFDCRMTrackingRecord trackingRecord : eFDCRMTrackingRecords) {
				salesforceEmails.append(trackingRecord.getCrmEmailAddress());
				salesforceEmails.append(COMMA);

				salesforceCrmIDs.append(trackingRecord.getCrmRecordID());
				salesforceCrmIDs.append(COMMA);
			}

			// Set salesforce emails to form bean emails
			formBean.setSalesforceEmailCrmID(salesforceCrmIDs.toString());
			formBean.setSalesforceEmailTo(salesforceEmails.toString());
		}
	}

	private boolean hasExistingEFDInfo(AppSessionBean appSessionBean, int orderID) throws AtWinXSException {
		IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService.locate(appSessionBean.getCustomToken());
		EFDDestinationOptionsVO[] efdVOs = ordersService.getEFDDestinations(orderID);
		return (null != efdVOs && efdVOs.length > 0);
	}

	protected boolean isFTPOnly(EFDDestinationsFormBean formBean) {
		boolean isFTPOnly = false;
		if (CollectionUtils.isNotEmpty(formBean.getEfdLines())) {
			for (EFDCartLine efdLine : formBean.getEfdLines()) {
				if (efdLine.getDestination().getEFDMethod().size() == 1
						&& efdLine.getDestination().getEFDMethod().contains(FTP)) {
					isFTPOnly = true;
				} else {
					return false;
				}
			}
		}
		return isFTPOnly;
	}

	//CAP-49311
	public SaveEfdInformationResponse saveEfdInformation(SessionContainer sc, SaveEfdInformationRequest efdInformationRequest) throws AtWinXSException {


		ApplicationSession applicationSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = applicationSession.getAppSessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSession = oeSession.getOESessionBean();

		SaveEfdInformationResponse response = new SaveEfdInformationResponse();
		response.setSuccess(true);

		//CAP-40547-C1UX BE - API Fixes - Error checks for valid and non-submitted order not coded in Delivery and Order info load APIs
		if (!validateOrder(response, sc, appSessionBean)){
			return response;
		}
		if(!oeOrderSession.getUserSettings().isAllowEFD()) {
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_NOT_ALLOWED, appSessionBean, null));
			return response;
		}

		try
		{
			EFDDestinationsFormBean efdFormBean = prepopulateEFDDetails(appSessionBean, volatileSessionBean, oeSession, oeOrderSession);

			boolean isSingle = !Util.yToBool(oeOrderSession.getUserSettings().getAllowMultipleEmailsCode()); //CAP-6759


			//CP-13020 [PDN] if single only and salesforce email is selected, delete the classic's email to 
			if (!Util.isBlankOrNull(efdFormBean.getSalesForceEmailSelected()) && Boolean.parseBoolean(efdFormBean.getSalesForceEmailSelected()) && isSingle)
			{
				efdFormBean.setEmailTo("");
			}

			efdFormBean.setSingleOnly(isSingle);
			response = validateRequest( appSessionBean, oeOrderSession, efdFormBean, efdInformationRequest,response);

			if(!response.isSuccess())
				return response;

			OECheckoutAssembler assembler = new OECheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(), 
					appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

			checkAndUpdateDelMethod(efdFormBean);//CAP-50078
			
			assembler.setEFDFormInfo(efdFormBean.getOldFormBean(), appSessionBean, oeOrderSession);

			//CAP-7372 JBS
			efdFormBean.saveSingleLineEmailAndNames(appSessionBean);

			//CP-12640 Save EFD CRM Tracking
			//CP-13056 RAR - Check as well if the selected is Salesforce for single selection scenarios.
			if(!Util.isBlankOrNull(efdFormBean.getSalesforceEmailTo()) && 
					((Boolean.parseBoolean(efdFormBean.getSalesForceEmailSelected()) && isSingle) || !isSingle))
			{						

				List<String> salesforceEmailList = Arrays.asList(efdFormBean.getSalesforceEmailTo().split(","));
				List<String> salesforceCrmIDList = Arrays.asList(efdFormBean.getSalesforceEmailCrmID().split(","));

				int index = -1;


				List<EFDCRMTrackingRecord> eFDCRMTrackingRecords = new ArrayList<>();
				for (String salesforceEmail :  salesforceEmailList)
				{
					index++;
					EFDCRMTrackingRecord eFDCRMTrackingRecord = objectMapFactoryService.getEntityObjectMap().getEntity(EFDCRMTrackingRecord.class, appSessionBean.getCustomToken());
					eFDCRMTrackingRecord.populate(efdFormBean.getOrderID(), salesforceEmail, salesforceCrmIDList.get(index), appSessionBean.getLoginID());
					eFDCRMTrackingRecords.add(eFDCRMTrackingRecord);
				}

				EFDCRMTracking eFDCRMTracking = objectMapFactoryService.getEntityObjectMap().getEntity(EFDCRMTracking.class, appSessionBean.getCustomToken());
				eFDCRMTracking.setRecords(eFDCRMTrackingRecords);

				eFDCRMTracking.save(efdFormBean.getOrderID());
			}
			else
			{
				List<EFDCRMTrackingRecord> eFDCRMTrackingRecords = new ArrayList<>();
				EFDCRMTracking eFDCRMTracking = objectMapFactoryService.getEntityObjectMap().getEntity(EFDCRMTracking.class, appSessionBean.getCustomToken());
				eFDCRMTracking.setRecords(eFDCRMTrackingRecords);

				eFDCRMTracking.save(efdFormBean.getOrderID());
			}

		}
		catch (Exception e)
		{
			logger.error("EFD saveEfdInformation() method - " , e);
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_INFO, appSessionBean, null));
		}

		return response;

	}
	
	protected void checkAndUpdateDelMethod(EFDDestinationsFormBean efdFormBean) {
		if (!efdFormBean.getEfdLines().isEmpty()) {// CAP-50078
			for (EFDCartLine efdlines : efdFormBean.getEfdLines()) {
				if (efdlines.getDestination().getFtpID() > -1) {
					efdlines.getDestination().getEFDMethod().add(FTP);
				}
			}
		}
	}
	
	//CAP-49311 prepopulateCommandObject
	protected EFDDestinationsFormBean prepopulateEFDDetails(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession, OEOrderSessionBean oeOrderSession) throws AtWinXSException
	{
		EFDDestinationsFormBean formBean = new EFDDestinationsFormBean();
		Message msg = new Message();
		List<String> errMsgs = new ArrayList<>();
		try {
			OECheckoutAssembler assembler = new OECheckoutAssembler(volatileSessionBean, appSessionBean.getCustomToken(), 
					appSessionBean.getDefaultLocale(), appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
			EFDDestinationOptionsFormBean oldFormBean = assembler.getEFDFormInfo(appSessionBean, oeOrderSession, volatileSessionBean.getOrderId());

			if (oldFormBean == null)
			{	// throw error - cannot get EFD lines!
				logger.debug("EFD - prepopulateData(), EFDDestinationsFormBean - could not get EFD destination line information for order ");
				msg.setErrGeneralMsg("Could not load EFD line information!");
				throw new AtWinXSMsgException(msg, "EFDDestinationController");
			}
			//CAP-430 Passed BaseSession in the parameter
			formBean.populate(volatileSessionBean, appSessionBean,  oeOrderSession, /*request,*/ oldFormBean, errMsgs, assembler, oeSession);

			//This will set the display mode of Salesforce email to INIT. No Saved emails will be displayed
			formBean.setSalesforceEmailDisplayMode("INIT");

			//CP-12640 RAR - Check if we have records in XST239, that means that we are in a BACK scenario.
			boolean hasExistingInfo = hasExistingEFDInfo(volatileSessionBean.getOrderId(), appSessionBean);

			//This will load all the previously saved salesforce CRM IDs and emails
			if (hasExistingInfo)
			{
				//This will set the display mode of Salesforce email to BACK. Previously saved salesforce emails will be displayed
				formBean.setSalesforceEmailDisplayMode("BACK");

				EFDCRMTracking eFDCRMTracking = objectMapFactoryService.getEntityObjectMap().getEntity(EFDCRMTracking.class, appSessionBean.getCustomToken());
				eFDCRMTracking.populate(formBean.getOrderID());

				//Get previously saved salesforce emails
				Collection<EFDCRMTrackingRecord> eFDCRMTrackingRecords = eFDCRMTracking.getRecords();

				if (null != eFDCRMTrackingRecords && !eFDCRMTrackingRecords.isEmpty())
				{
					StringBuilder salesforceEmails = new StringBuilder();
					StringBuilder salesforceCrmIDs = new StringBuilder();

					for (EFDCRMTrackingRecord record1 : eFDCRMTrackingRecords)
					{
						salesforceEmails.append(record1.getCrmEmailAddress());
						salesforceEmails.append(",");

						salesforceCrmIDs.append(record1.getCrmRecordID());
						salesforceCrmIDs.append(",");
					}

					//Set salesforce emails to form bean emails
					formBean.setSalesforceEmailCrmID(salesforceCrmIDs.toString());
					formBean.setSalesforceEmailTo(salesforceEmails.toString());
				}


			}

			// CP-12640 ACL Set Default Locale for SF user that doesn't have country
			if(appSessionBean.getDefaultLocale().getISO3Country() != null)
			{
				formBean.setDefaultLocale(appSessionBean.getDefaultLocale().getISO3Country());
			}

			// CP-13003 ACL Added attribute to determine if coming from Salesforce or not
			formBean.setFromSalesforceEntryPoint(!appSessionBean.getSalesforceSignedRequestJson().isEmpty() || null == appSessionBean.getSalesforceSignedRequestJson());

		}catch (Exception e) {
			throw new AtWinXSException(e.getMessage(),"" );
		}
		return formBean;
	}
	
	//CAP-49311
	/**
	 * Method hasExistingEFDInfo()
	 * 
	 * This method will look up the XST239 table if we have records for the passed order ID,
	 * if we have that means taht we are in a BACK scenario.
	 * 
	 * @param orderID
	 * @return boolean
	 * @throws AtWinXSException
	 */
	private boolean hasExistingEFDInfo(int orderID, AppSessionBean appSessionBean) throws AtWinXSException
	{
		IOEManageOrdersComponent ordersService = oeManageOrdersComponentLocatorService.locate(appSessionBean.getCustomToken());
		EFDDestinationOptionsVO[] efdVOs = ordersService.getEFDDestinations(orderID);
		return (null != efdVOs && efdVOs.length > 0);
	}
	
	//CAP-49311
	/**
	 * This takes the current values in this form bean and populates the
	 * oldFormBean which will be passed into the old code in order to be saved.
	 * @throws AtWinXSException 
	 */
	public SaveEfdInformationResponse validateRequest(AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSession,EFDDestinationsFormBean efdFormBean, SaveEfdInformationRequest efdInformationRequest, SaveEfdInformationResponse response) throws AtWinXSException
	{
		String overrideFrom = "";
		String overrideTo = "";
		String overrideSubject = "";
		String overrideContent = "";
		String overrideLink = "";
		boolean isDefaultEmailSettings = true;
		
		 if(!validateEfdLineItems(appSessionBean, efdFormBean, efdInformationRequest, response)) 
			 return response;
		
		if(!validateEmailStyleIDAndEmailMsg(efdInformationRequest, appSessionBean, oeOrderSession, efdFormBean, response))
			return response;

		if(!validateEmailAddresses(appSessionBean, oeOrderSession, efdFormBean, efdInformationRequest, response))
			return response;

		
		//CP-12640 [PDN] if single and salesforce email is selected. Salesforce email only will be saved to the bean
		
		if (efdFormBean.getEmailStyleId() == efdFormBean.getOverrideStyleId())
		{
			overrideFrom = efdFormBean.getFromNameOverride();
			overrideTo = efdFormBean.getReplyToOverride();
			overrideSubject = efdFormBean.getEmailSubjectOverride();
			overrideContent = efdFormBean.getEmailContentOverride();
			overrideLink = efdFormBean.getHyperlinkTextOverride();
			if ((!Util.isBlankOrNull(overrideFrom)) || (!Util.isBlankOrNull(overrideTo)) || (!Util.isBlankOrNull(overrideSubject)) || (!Util.isBlankOrNull(overrideContent))
					|| (!Util.isBlankOrNull(overrideLink)))
			{
				isDefaultEmailSettings = false;
			}
		}
		String defaultEmailString = Util.boolToY(isDefaultEmailSettings);

		EFDDestinationOptionsVO[] vos = efdFormBean.getOldFormBean().getEFDDestinationsVO(); 
		for (int i = 0; i < vos.length; i++)
		{ // loop over old lines, replacing them
			EFDDestinationOptionsVO oldVO = vos[i];

			for (EFDCartLine line : efdFormBean.getEfdLines())
			{ // loop over new lines
				if (line.getLineNumber() == oldVO.getOrderLineNum())
				{ // if they match, then populate the old with the new

					vos[i] = new EFDDestinationOptionsVO(efdFormBean.getOrderID(), line.getLineNumber(), efdFormBean.getEmailTo(), line.getFtpDestinationID(), line.getDestination().getEFDMethod(), overrideFrom, overrideTo,
							overrideSubject, overrideContent, overrideLink, efdFormBean.getEmailStyleId(), oldVO.getDigiMagUrl(), oldVO.getFromEmailName(), defaultEmailString, efdFormBean.getEmailMessage()); //CAP-6764
				
				} // end if we have a match
			} // end loop over new data lines
		} // end for loop over old VOs
		return response;
	}
	
	//CAP-49311
	public boolean validateEmailAddresses(AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSession,EFDDestinationsFormBean efdFormBean, SaveEfdInformationRequest efdInformationRequest, SaveEfdInformationResponse response) throws AtWinXSException
	{
		if(isFTPOnly(efdFormBean)) {
			return true;
		}
		
		boolean isSingle = "N".equalsIgnoreCase(oeOrderSession.getUserSettings().getAllowMultipleEmailsCode());
		boolean emailEditable = oeOrderSession.getUserSettings().isCanUserEditOwnEmail();
		List<String> addresses = efdInformationRequest.getEmailAddresses();
		StringBuilder emailAndName = new StringBuilder();

		if(!emailEditable){
			//email not editable
			efdFormBean.setEfdEmailsAndNames(efdFormBean.getEmailTo()+"^ ");
			return true;
		}
		
		if( isSingle) {
			// editable and one email
			if( (addresses.size() > ModelConstants.NUMERIC_1) || ((addresses.size() == ModelConstants.NUMERIC_1) && !Util.isValidEmailFormat(addresses.get(0)))) 
			{
				response.setSuccess(false);
				response.getFieldMessages().put(EMAIL_ADDRESSES, buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_EMAIL, appSessionBean, null));
				return false;

			} else if(addresses.get(0).length() > 128 )
			{
				response.setSuccess(false);
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, 128 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(EMAIL_ADDRESSES, buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, appSessionBean, replaceMap));
				return false;

			}
			efdFormBean.setEfdEmailsAndNames(addresses.get(0)+"^ ");
			return true;
		}

		//editable and multiple email

		for(String email : addresses)
		{
			if(!Util.isValidEmailFormat(email)) {
				response.setSuccess(false);
				response.getFieldMessages().put(EMAIL_ADDRESSES, buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_EMAIL, appSessionBean, null));
				return false;

			}
			if(email.length() > 128 )
			{
				response.setSuccess(false);
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, 128 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(EMAIL_ADDRESSES, buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, appSessionBean, replaceMap));
				return false;

			}
			emailAndName.append(email+"^ |");

		}
		String emailAndName1 = emailAndName.substring(0, emailAndName.length()-1);
		efdFormBean.setEfdEmailsAndNames(emailAndName1);
		return true;
	}
	
	//CAP-49311
	public boolean validateFtpID(AppSessionBean appSessionBean, String ftpID, EFDCartLine line, SaveEfdInformationResponse response) throws AtWinXSException 
	{
		int ftpId = Util.safeStringToDefaultInt(ftpID, -1);
		if(line.isAllowsFtpOutput() && ftpId < 0) {
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_FTP, appSessionBean, null));
			return false;
		}
		line.setFtpDestinationID(line.isAllowsFtpOutput() ? ftpId : -1);
		return true;	
	}

	//CAP-49311
	public boolean validateEfdDestinations(AppSessionBean appSessionBean, List<String> efdDestReq, EFDCartLine line, SaveEfdInformationResponse response, EFDDestinationsFormBean efdFormBean) throws AtWinXSException 
	{
		List<String> efdMethod = getEFDSources(efdFormBean.getOldFormBean().getCatalogDefaultSettings());// CAP-50547
		List<String> validEfdTypes = new ArrayList<>();//CAP-50547
		boolean hasValidEfd = false;
		if (efdMethod.size() == 1 && efdMethod.get(0).equalsIgnoreCase("FTP")) {
			hasValidEfd = true;
			validEfdTypes.add(OrderEntryConstants.EFD_SOURCES_FTP);
		} else {
			for (String efdDestination : efdDestReq) {
				if (efdMethod.contains(efdDestination)) {
					hasValidEfd = true;
					validEfdTypes.add(efdDestination);
				} else {
					hasValidEfd = false;
					break;
				}
			}
		}
		
		if(!hasValidEfd) {
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_TYPE, appSessionBean, null));
			return false;
		}
		line.getDestination().setEFDMethod(validEfdTypes);
		if ((line.isAllowsDigimagOutput() || line.isAllowsPdfOutput() || line.isAllowsJellyvisionOutput()) && efdDestReq!=null )
		{ 
			// if variable choices available, then read in the values from the request
			line.setPdfChosen(efdDestReq.contains("PDF"));
			line.setDigimagChosen(efdDestReq.contains("DIG"));
			line.setJellyvisionChosen(efdDestReq.contains("JEL"));

		}
		return true;
	}
	
	//CAP-50547
	public List<String> getEFDSources(Collection<CatalogDefaultVO> catalogDefaultSettings) {
		List<String> sources = new ArrayList<>();
		int index = 0;
		for (CatalogDefaultVO catalogdef : catalogDefaultSettings) {
			if (index == 0) {
				for (EFDSourceSetting sourceSetting : catalogdef.getEfdSourceSettings()) {
					sources.add(sourceSetting.getEfdSource().getSourceCode());
				}
				index++;
			}
		}
		return sources;
	}

	//CAP-49311
	public boolean validateEfdLineItems(AppSessionBean appSessionBean, EFDDestinationsFormBean efdFormBean, SaveEfdInformationRequest efdInformationRequest, SaveEfdInformationResponse response) throws AtWinXSException 
	{
		if(efdFormBean.getEfdLines().isEmpty()) {
			response.setSuccess(true);
			response.setMessage("");
			return true;
		}

		for(EFDLineItem efdLineFromRequest : efdInformationRequest.getEfdLineItems()) {
			boolean efdLineValid = false;
			for (EFDCartLine line : efdFormBean.getEfdLines()) {
				
				if (line.getLineNumber() == efdLineFromRequest.getLineNumber()) {
					efdLineValid = true;
					
					if(!validateFtpID(appSessionBean,  efdLineFromRequest.getFtpID(),  line,  response) || !validateEfdDestinations(appSessionBean, efdLineFromRequest.getEfdDestinations(), line, response,efdFormBean))
						return false;
										
				}
				
			}
			if(!validateItemLineNumber(appSessionBean, efdLineValid, efdInformationRequest,  response))
				return false;

		}
		return true;
	}
	
	//CAP-49311
	public boolean validateItemLineNumber(AppSessionBean appSessionBean, boolean efdLineValid, SaveEfdInformationRequest efdInformationRequest,  SaveEfdInformationResponse response) throws AtWinXSException
	{
		if(!efdLineValid && !efdInformationRequest.getEfdLineItems().isEmpty()) {
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_INFO, appSessionBean, null));
			return false;
		}
		return true;
	}
	
	//CAP-49311
	public boolean validateEmailStyleIDAndEmailMsg(SaveEfdInformationRequest efdInformationRequest, AppSessionBean appSessionBean, OEOrderSessionBean oeOrderSession, EFDDestinationsFormBean efdFormBean, SaveEfdInformationResponse response) throws AtWinXSException 
	{
		//CAP-50547
		if(isFTPOnly(efdFormBean)) {
			return true;
		}
		// Decrypt and get active Email Style ID
		int	intActiveEmailStyleID = Integer.parseInt(efdInformationRequest.getEmailStyleID());//CAP-49326
		
		//CAP-50435
		int siteID = oeOrderSession.getSiteID();
        int buID = oeOrderSession.getBusinessUnitID();
        if (intActiveEmailStyleID == 0)
        {
            siteID = AtWinXSConstant.DEFAULT_SITE_ID;
            buID = AtWinXSConstant.DEFAULT_BU_ID;
        }
        EmailStyleVOKey voKeyStyle = new EmailStyleVOKey(siteID,
                buID, OrderAdminConstants.EmailTypeEnum.EFD.getTypeCode(),
                intActiveEmailStyleID);
        //CAP-50435
        
		ICustomEmail compLocator = oeCustomEmailComponentLocatorService.locate(appSessionBean.getCustomToken());
		Collection<EmailStyleVO>  colActiveEmailStyle = compLocator.retrieveAllEmailStyles(voKeyStyle);
		EmailStyleVO activeEmailStyle = compLocator.getEmailStyleByID(voKeyStyle);
		if(!checkStyleIdExist(colActiveEmailStyle, intActiveEmailStyleID)) {
			response.setSuccess(false);
			response.setMessage(buildTranslationMessage(SFTranslationTextConstants.EFD_INVALID_STYLE_INFO_ERR, appSessionBean, null));
			return false;
		}
		efdFormBean.setEmailStyleId(intActiveEmailStyleID);
		
		if(activeEmailStyle.isAllowEfdMsgInd()) {
			String emailMsg = efdInformationRequest.getEmailMessage();
			//CAP-49974 - removed the empty email message check - which is unnecessary
			if(emailMsg.length() > 1000) {
				response.setSuccess(false);
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						1000 + AtWinXSConstant.EMPTY_STRING);
				
				response.getFieldMessages().put(EMAIL_MESSAGE, buildTranslationMessage(SFTranslationTextConstants.MAX_CHARS_ERR, appSessionBean, replaceMap));
				return false;
			}
			efdFormBean.setEmailMessage(efdInformationRequest.getEmailMessage());
		}
		
		return true;
		
	}
	
	// CAP-49748
	public boolean checkAllowMultipleEmails(boolean userCanEditOwnEmail, String userAllowMultipleEmailsCode) {
		return userCanEditOwnEmail && !userAllowMultipleEmailsCode.equals("N");
	}

	// CAP-49748
	public String checkAndGetDefaultEmail(String efdEmailAddress, AppSessionBean appSessionBean) {
		switch (efdEmailAddress) {
		case "[--BLANK--]":
			return AtWinXSConstant.EMPTY_STRING;
		case "[--O USEREMAIL--]":
			return appSessionBean.getEmailAddress();
		case "[--O REQEMAIL--]":
			return appSessionBean.getRequestorProfile() != null ? appSessionBean.getRequestorProfile().getEmailAddress()
					: appSessionBean.getEmailAddress();
		case "":
			return appSessionBean.getEmailAddress();
		default:
			return efdEmailAddress;
		}
	}
	
	//CAP-49814
		protected Map<String,String> emailSourceTypes(AppSessionBean appSessionBean) throws AtWinXSException {
			Map<String,String> emailSrcTyp= new HashMap<>();
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_PDF, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_DIGITAL_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_DIGIMAG, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_DIGIMAG_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_JELLY_VISION, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_OTHER_OE_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_STATIC_CONTENT, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_STATIC_CONTENT_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_EDOC, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_EDOC_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_EXACT_TARGET, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_EXACT_TARGET_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_FTP, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_FTP_LBL));
			
			emailSrcTyp.put(OrderEntryConstants.EFD_SOURCES_EGC, translationService.processMessage(appSessionBean.getDefaultLocale(), 
					appSessionBean.getCustomToken(), 
					TranslationTextConstants.TRANS_NM_EFD_SOURCE_EGC));
			
			return emailSrcTyp;
		}
	
}	
	
