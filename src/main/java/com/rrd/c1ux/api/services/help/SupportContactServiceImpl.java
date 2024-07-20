/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By			Jira #						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/15/22	Satish kumar A		CAP-35429- support page 	Initial creation
 *  09/28/22	Satish kumar A		CAP-35430- support page 	Send email with support page form data
 *  11/02/22	Krishna Natarajan 	CAP-36947 					changing the formBean.getDefaultCPEmail() to appSessionBean.getSupportEmail() to pick up the
 *  11/02/22	Krishna Natarajan 	CAP-36948 					adding a method to check the fullname to get replaced with description
 *  11/11/22	Krishna Natarajan	CAP-37102					defaulted user ‘Questions Regarding Storefront’
 *  12/23/2022	E Anderson			CAP-36154		            BE Updates for translation.
 *  01/10/2023  E Anderson      	CAP-36154                   Derive the appNamePrefix from XST522.
 *  01/24/23	A Boomker			CAP-38339					Fix logic around shared IDs and profile numbers
 *  02/27/23    C Porter       		CAP-38708                   Refactor translation service into base service object.
 *  04/24/23	Nathaniel Caceres	CAP-39246					Backend updates for translation.
 *  06/23/2023	S Ramachandran		CAP-40614					Sonar fix
 *	06/29/23	A Boomker			CAP-41796					Fix code for default support contact
 */

package com.rrd.c1ux.api.services.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.help.SupportContactRequest;
import com.rrd.c1ux.api.models.help.SupportContactResponse;
import com.rrd.c1ux.api.models.help.SupportContacts;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.admin.vo.SupportContactVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.otherservices.ao.SupportContactAssembler;
import com.wallace.atwinxs.otherservices.ao.SupportContactEmail;
import com.wallace.atwinxs.otherservices.ao.SupportContactEmailFormBean;

@Service
public class SupportContactServiceImpl extends BaseService implements SupportContactService {

    public SupportContactServiceImpl(TranslationService translationService) {
      super(translationService);
    }

	//CAP-35429- To load support page information
	/**
	 * @param sc {@link SessionContainer}
	 * @return {@link SupportContactResponse}
	 * @throws AtWinXSException
	 */
	@Override
	public SupportContactResponse populateSupportContactDetails(SessionContainer sc) throws AtWinXSException {

		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		return populateModel(appSessionBean);

	}


	//This method is copied from CustomPointWeb - com.rrd.custompoint.controller.help.SupportContactController.populateModel method.
	/**
	 * Populate the model with the list of send to information
	 *
	 * @param appSessionBean {@link AppSessionBean}
	 * @return {@link SupportContactResponse}
	 * @throws AtWinXSException
	 */
	protected SupportContactResponse populateModel(AppSessionBean appSessionBean) throws AtWinXSException
	{
		SupportContactResponse supportContactResponse =  new SupportContactResponse();

		try {
		SupportContactAssembler assembler = new SupportContactAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());

		int siteID = appSessionBean.isMasterSite() ? AtWinXSConstant.DEFAULT_SITE_ID : appSessionBean.getSiteID();

		SupportContactEmailFormBean formBean =
			assembler.getSupportContactsPageInfo(
				siteID,
				appSessionBean.getBuID(),
				appSessionBean.getBuName(),
				appSessionBean.getFirstName(),
				appSessionBean.getLastName(),
				appSessionBean.getEmailAddress(),
				appSessionBean.getPhoneNumber());


		supportContactResponse = generateResponse( appSessionBean , formBean , supportContactResponse);
		//CAP-39246 Backend updates for translations.
		supportContactResponse.setSuccessMessage(getTextTranslation(appSessionBean, SFTranslationTextConstants.SC_SUCCESS_MESSAGE_TEXT_NAME, SFTranslationTextConstants.SC_SUCCESS_MESSAGE_TEXT));
		supportContactResponse.setErrorMessage("");
		supportContactResponse.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);

		Properties resourceBundleProps = translationService.getResourceBundle(appSessionBean, "helpandcontact");
		supportContactResponse.setTranslation(translationService.convertResourceBundlePropsToMap(resourceBundleProps));

		}catch (Exception e) {
			supportContactResponse.setSuccessMessage("");
			//CAP-39246 Backend updates for translations.
			supportContactResponse.setErrorMessage(getTextTranslation(appSessionBean, SFTranslationTextConstants.SC_ERROR_MESSAGE_TEXT_NAME, SFTranslationTextConstants.SC_ERROR_MESSAGE_TEXT));
			supportContactResponse.setStatus(RouteConstants.REST_RESPONSE_FAIL);
		}

		return supportContactResponse;
	}

	/**
	 * To set response values to form
	 *
	 * @param appSessionBean {@link AppSessionBean}
	 * @param formBean {@link SupportContactEmailFormBean}
	 * @param supportContactResponse {@link SupportContactResponse}
	 * @return {@link SupportContactResponse}
	 */
	public SupportContactResponse generateResponse(AppSessionBean appSessionBean , SupportContactEmailFormBean formBean , SupportContactResponse supportContactResponse) {

		String fullname = Util.nullToEmpty(appSessionBean.getFirstName()) + " " + Util.nullToEmpty(appSessionBean.getLastName());
		String supportPhoneTextValue = appSessionBean.getSupportPhone();
		String userEmailTextValue = appSessionBean.getSupportEmail();//CAP-36947

		String showContactForm = (appSessionBean.getService(AtWinXSConstant.SUPPORT_SERVICE_ID) != null)?"Y":"N";
		int defaultSelectedContactNumber =0;

		if(!formBean.getSupportContacts().isEmpty() )
		{
			SupportContactVO avo = (SupportContactVO) formBean.getSupportContacts().get(0);
			if(avo.getPriorityCode() == 3)
				defaultSelectedContactNumber = avo.getKey().getSupportContactID();
		}

		// check on profile number
		if ((appSessionBean.isSharedID()) && (appSessionBean.getProfileNumber() <=0)) { // CAP-38339
			supportContactResponse.setAccountUser(appSessionBean.getSharedIDDesc());
			supportContactResponse.setDefaultFullName("");
		} else {
			supportContactResponse.setAccountUser(fullname);
			supportContactResponse.setDefaultFullName(fullname);
		}
		supportContactResponse.setAccountCustomer(appSessionBean.getBuName());
		supportContactResponse.setAccountUserID(appSessionBean.getLoginID());
		supportContactResponse.setDefaultEmailAddress(appSessionBean.getEmailAddress());
		supportContactResponse.setDefaultPhoneNumber(appSessionBean.getPhoneNumber());
		supportContactResponse.setSupportPhoneTextValue(supportPhoneTextValue);
		supportContactResponse.setUserEmailTextValue(userEmailTextValue);
		supportContactResponse.setCustomSupportHTML(appSessionBean.getCustomSupportHTML());
		supportContactResponse.setShowContactForm(showContactForm);
		supportContactResponse.setHeaderSupportPhone(appSessionBean.getSupportPhone());
		supportContactResponse.setDefaultSelectedContactNumber(defaultSelectedContactNumber+"");
		//CAP-39246 Backend updates for translations.
		supportContactResponse.setSupportPhoneText(getTextTranslation(appSessionBean, SFTranslationTextConstants.SUPPORT_PHONE_TEXT_NAME, SFTranslationTextConstants.SUPPORT_PHONE_TEXT));
		supportContactResponse.setUserEmailText(getTextTranslation(appSessionBean, SFTranslationTextConstants.USER_EMAIL_TEXT_NAME, SFTranslationTextConstants.USER_EMAIL_TEXT));


		ArrayList<SupportContacts> contactsList = new ArrayList<>();
		if(formBean.getSupportContacts().isEmpty()) {
			SupportContacts cts = new SupportContacts();
			cts.setId(0); // CAP-41796 - send 0, not -2
			//CAP-39246 Backend updates for translations.
			cts.setName(getTextTranslation(appSessionBean, SFTranslationTextConstants.DEFAULT_USER_TEXT_NAME, ModelConstants.DEFAULT_USER));
			contactsList.add(cts);
		}else {
			for(int i=0; i<formBean.getSupportContacts().size(); i++) {

				SupportContactVO supportVO = (SupportContactVO) formBean.getSupportContacts().get(i);
				SupportContacts cts = new SupportContacts();
				cts.setId(supportVO.getKey().getSupportContactID());
				cts.setName(supportVO.getContactName());
				contactsList.add(cts);
			}
		}

		//put the support Contacts in the response.
		supportContactResponse.setContacts(contactsList);

		return supportContactResponse;

	}

	//CAP-39246 Backend updates for translations.
	protected String getTextTranslation(AppSessionBean appSessionBean, String translationName, String defaultValue) {
		String translationValue = AtWinXSConstant.EMPTY_STRING;
		try {
			translationValue = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), translationName);
		} catch (AtWinXSException e) {
			translationValue = defaultValue;
		}
		return translationValue;
	}

	//CAP-35430 - Support Page - Handle submission of email information and sending of email
	/**
	 * @param sc {@link SessionContainer}
	 * @param scRequest {@link SupportContactRequest}
	 * @return {@link SupportContactResponse}
	 * @throws AtWinXSException
	 */
	@Override
	public SupportContactResponse sendMailToSupportContactDetails(SessionContainer sc, SupportContactRequest scRequest) throws AtWinXSException {

		SupportContactResponse supportContactResponse =  new SupportContactResponse();

		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();


		StringBuilder errorMessage = new StringBuilder("");
		String successMessage = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_SUCC_MSG);
		//CAP-542 EIQ catch and display error message thrown
		SupportContactEmailFormBean formBean = null;
		try
		{
			//logic of sending the email on submission
			formBean = performSendMessage(scRequest, errorMessage,appSessionBean);
		}
		catch(AtWinXSException e)
		{
			errorMessage.append(e.getMessage());
		}

		supportContactResponse = generateResponse( appSessionBean , formBean , supportContactResponse);

		if(!Util.isBlank(errorMessage.toString()))//CAP-19456
		{
			supportContactResponse.setErrorMessage(errorMessage.toString());
			supportContactResponse.setSuccessMessage("");
		}
		else
		{
			supportContactResponse.setSuccessMessage(successMessage);
			supportContactResponse.setErrorMessage("");
			supportContactResponse.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);
		}

		return supportContactResponse;
	}

	//This method is copied and modified from CustomPointWeb - com.rrd.custompoint.controller.help.SupportContactController.performSendMessage method .
	/**
	 * @param supportContactRequest {@link SupportContactRequest}
	 * @param errorMsg {@link StringBuilder}
	 * @param appSessionBean {@link AppSessionBean}
	 * @return {@link SupportContactResponse}
	 * @throws AtWinXSException
	 */
	protected SupportContactEmailFormBean performSendMessage(SupportContactRequest supportContactRequest, StringBuilder errorMsg, AppSessionBean appSessionBean) throws AtWinXSException
	{
		SupportContactEmailFormBean beanFromBackend = null;
		SupportContactAssembler assembler = new SupportContactAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());

		int siteID = appSessionBean.isMasterSite() ? AtWinXSConstant.DEFAULT_SITE_ID : appSessionBean.getSiteID();
		String siteName = "";
		if (appSessionBean.isMasterSite())
		{
			siteName = assembler.getMasterSiteName(siteID);
		}
		else
		{
			siteName = appSessionBean.getSiteName();
		}

		if (beanFromBackend == null)
		{
			beanFromBackend =
				assembler.getSupportContactsPageInfo(
					siteID,
					appSessionBean.getBuID(),
					appSessionBean.getBuName(),
					appSessionBean.getFirstName(),
					appSessionBean.getLastName(),
					appSessionBean.getEmailAddress(),
					appSessionBean.getPhoneNumber());
		}

		if (Util.isBlankOrNull(supportContactRequest.getUserEmail()))
		{
			beanFromBackend.setUserEmail(AppProperties.getSenderEmailAddress(appSessionBean.isCustomPointSite()));
		}
		else
		{
			beanFromBackend.setUserEmail(Util.nullToEmpty(supportContactRequest.getUserEmail()));
		}

		//If profile number is not -1 then get the id from admin.
		int profileNum = appSessionBean.getProfileNumber();
		String profileID = "";
		if (AdminConstant.INVALID_PROFILE_NUMBER != profileNum)
		{
			profileID = assembler.getProfileID(siteID, appSessionBean.getBuID(), profileNum);
		}

		beanFromBackend.setUserPhone(supportContactRequest.getUserPhone());
		beanFromBackend.setUserName(supportContactRequest.getUserName());
		beanFromBackend.setMessageSubject(supportContactRequest.getMessageSubject());
		beanFromBackend.setMessageText(supportContactRequest.getMessageText());

		beanFromBackend.setMessageSendTo(this.getMessageSendToAddress(beanFromBackend, supportContactRequest.getMessageSendTo(), appSessionBean));

		String supportContactName = "";
		int supportContactNum = (Util.safeStringToInt((supportContactRequest.getMessageSendTo())));

		if (supportContactNum != 0)
		{
			supportContactName = assembler.getSupportContactName(supportContactNum);
		}
		//CAP-542 EIQ
		String fromLbl = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "from_lbl");
		String toYouLbl = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "to_you_lbl");

		//Retrieve correct application name; XS or CustomPoint
		//String applicationName = AppProperties.getApplicationName() + (appSessionBean.isCustomPointSite() ? "" : " 2.0");
		//CAP-39246 Backend updates for translations.
		String applicationName = getTextTranslation(appSessionBean, SFTranslationTextConstants.APPLICATION_TEXT_NAME, SFTranslationTextConstants.APPLICATION_TEXT);
		String appText = appSessionBean.showWallaceReferences() ? fromLbl+" " + applicationName : toYouLbl;
		//CAP-542 EIQ
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put("{apptext}", appText);
		String senderDisc = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "senderDiscMsg", replaceMap);

		String messageText = Util.forHTML(supportContactRequest.getMessageText()) +
							"<br/>" + "-------------------------------------------------------" +
							"<br/>" + senderDisc;


		if (!validateEmailAddress(beanFromBackend.getUserEmail()))
		{
			errorMsg.append(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INV_EMAIL_ERR_MSG));
		}
		else if (!validateEmailAddress(beanFromBackend.getMessageSendTo()))
		{
			errorMsg.append(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_INV_SENDTO_ERR_MSG));
		}

		//send email if there are no errors
		if(Util.isBlank(errorMsg.toString()))//CAP-19456
		{
			//CAP-542 EIQ call new entity for sending email
			String userEmail = beanFromBackend.getUserEmail();
			beanFromBackend.setUserEmail("\"" + beanFromBackend.getUserName() + "\" <" + beanFromBackend.getUserEmail() + ">");


			SupportContactEmail email = ObjectMapFactory.getEntityObjectMap().getEntity(SupportContactEmail.class, appSessionBean.getCustomToken());

			email.setAppSessionBean(appSessionBean);
			email.setRecipients(new String[] { beanFromBackend.getMessageSendTo()});
			email.setSupportContactName(supportContactName);
			email.setSiteName(siteName);
			email.setProfileId(profileID);
			email.setUserName(supportContactRequest.getUserName());
			email.setUserPhone(supportContactRequest.getUserPhone());
			email.setUserEmail(userEmail);

			email.sendSupportContactEmail(beanFromBackend.getMessageSubject(),
										messageText,
										beanFromBackend.getUserName(),
										beanFromBackend.getUserEmail());

		}
		return beanFromBackend;
	}

	//This method is copied and modified from CustomPointWeb - com.rrd.custompoint.controller.help.SupportContactController.getMessageSendToAddress method.
	// To get the sendTo parameter value
	protected String getMessageSendToAddress(SupportContactEmailFormBean bean, String supportContactID, AppSessionBean appSessionBean)
	{
		ArrayList list = new ArrayList();
		String sendTo = "";
		int sprtIDNum = Util.safeStringToInt(supportContactID);
		if (bean != null)
		{
			list = bean.getSupportContacts();
		}
		int len = list.size();
		if (len > 0)
		{
			for (int i=0; i < len; i++)
			{
				SupportContactVO vo = (SupportContactVO) list.get(i);
				if (vo == null)
				{
					continue;
				}
				if (len == 1 || sprtIDNum == vo.getSupportContactID())
				{
					sendTo = vo.getEmailAddress();
				}
			}
		}
		else if (bean != null)
		{
			if (appSessionBean.isCustomPointSite())
			{
				sendTo = bean.getDefaultCPEmail();
			}
			else
			{
				sendTo = bean.getDefaultXSEmail();

			}
		}

		return sendTo;
	}

	//This method is copied and modified from CustomPointWeb - com.rrd.custompoint.controller.help.SupportContactController.validateEmailAddress metho .
	// To validate the email address
	protected boolean validateEmailAddress(String emailAddress)
	{
		boolean isValidFormat = true;

		// Check for @ sign
		int nAtPosition = emailAddress.indexOf('@');
		if (nAtPosition < 2)
		{
			isValidFormat = false;
		}
		// Check for blanks
		else if (emailAddress.trim().indexOf(" ") > 0)
		{
			isValidFormat = false;
		}
		// Check for % signs
		else if (emailAddress.indexOf('%') > 0)
		{
			isValidFormat = false;
		}
		// Check for existence of period sign
		else if (emailAddress.lastIndexOf('.') < nAtPosition)
		{
			isValidFormat = false;
		}
		// check for more than one @ sign
		else if (emailAddress.lastIndexOf('@') > nAtPosition)
		{
			isValidFormat = false;
		}
		else
		{
			// Check for alphanumeric characters before and after the @ character
			// and in the last character of the address
			char charBeforeAt = emailAddress.charAt(nAtPosition - 1);
			char charAfterAt = emailAddress.charAt(nAtPosition + 1);
			char charLast = emailAddress.charAt(emailAddress.trim().length() - 1);

			//Allow numbers in email address.
			if (!Character.isLetterOrDigit(charBeforeAt) || !Character.isLetterOrDigit(charAfterAt) || !Character.isLetterOrDigit(charLast))
			{
				isValidFormat = false;
			}
		}

		return isValidFormat;
	}

	//CAP-35429 Commented this method as the TranslationText not in scope for c1ux phase 1a.
	/**
	 * get the page labels necessary for the page
	 * @param model
	 * @throws AtWinXSException
	 */
	/*
	 * protected void getPageLabels(Map<String, Object> model) throws
	 * AtWinXSException { Locale defaultLocale = appSessionBean.getDefaultLocale();
	 * CustomizationToken customToken = appSessionBean.getCustomToken();
	 *
	 * Map<String, Object> sendToReplaceMap = new HashMap<String, Object>();
	 * Map<String, Object> yourNameReplaceMap = new HashMap<String, Object>();
	 * Map<String, Object> yourEmailReplaceMap = new HashMap<String, Object>();
	 * Map<String, Object> yourPhoneReplaceMap = new HashMap<String, Object>();
	 * Map<String, Object> subjectReplaceMap = new HashMap<String, Object>();
	 * Map<String, Object> messageReplaceMap = new HashMap<String, Object>();
	 *
	 *
	 * sendToReplaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_NOT_BLANK_MSG,
	 * Util.nullToEmpty( TranslationTextTag.processMessage(defaultLocale,
	 * customToken, TranslationTextConstants.TRANS_NM_SEND_TO)));
	 * model.put("sendToReplaceMap", sendToReplaceMap);
	 *
	 * yourNameReplaceMap.put(TranslationTextConstants.
	 * TRANS_NM_REP_TAG_NOT_BLANK_MSG, Util.nullToEmpty(
	 * TranslationTextTag.processMessage(defaultLocale, customToken,
	 * TranslationTextConstants.TRANS_NM_YOUR_NAME)));
	 * model.put("yourNameReplaceMap", yourNameReplaceMap);
	 *
	 * yourEmailReplaceMap.put(TranslationTextConstants.
	 * TRANS_NM_REP_TAG_NOT_BLANK_MSG, Util.nullToEmpty(
	 * TranslationTextTag.processMessage(defaultLocale, customToken,
	 * TranslationTextConstants.TRANS_NM_YOUR_EMAIL)));
	 * model.put("yourEmailReplaceMap", yourEmailReplaceMap);
	 *
	 * yourPhoneReplaceMap.put(TranslationTextConstants.
	 * TRANS_NM_REP_TAG_NOT_BLANK_MSG, Util.nullToEmpty(
	 * TranslationTextTag.processMessage(defaultLocale, customToken,
	 * TranslationTextConstants.TRANS_NM_YOUR_PHONE)));
	 * model.put("yourPhoneReplaceMap", yourPhoneReplaceMap);
	 *
	 * subjectReplaceMap.put(TranslationTextConstants.
	 * TRANS_NM_REP_TAG_NOT_BLANK_MSG, Util.nullToEmpty(
	 * TranslationTextTag.processMessage(defaultLocale, customToken,
	 * TranslationTextConstants.TRANS_NM_SUBJECT))); model.put("subjectReplaceMap",
	 * subjectReplaceMap);
	 *
	 * messageReplaceMap.put(TranslationTextConstants.
	 * TRANS_NM_REP_TAG_NOT_BLANK_MSG, Util.nullToEmpty(
	 * TranslationTextTag.processMessage(defaultLocale, customToken,
	 * TranslationTextConstants.TRANS_NM_MESSAGE))); model.put("messageReplaceMap",
	 * messageReplaceMap); }
	 */


}
