/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *	09/22/23	L De Leon			CAP-44032		Initial Version
 *	09/22/23	S Ramachandran		CAP-44048		added service method to validate/save payment info during checkout
 *	10/03/23	L De Leon			CAP-44348		Added fixes for billing method and save profile card details
 *  10/06/23	T Harmon			CAP-44417		Added credit card authorization
 *  10/16/23	T Harmon			CAP-44417		Modified ObjectMapFactory for better testing
 *	03/15/24	L De Leon			CAP-47894		Modified methods to pass and check for if force CC option is on
 *	04/02/24	Krishna Natarajan	CAP-48380		Modified method and added a parameter to populateSelectedPaymentMethod to set payment method
 */
package com.rrd.c1ux.api.services.checkout;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.checkout.PaymentInformationResponse;
import com.rrd.c1ux.api.models.checkout.PaymentSaveRequest;
import com.rrd.c1ux.api.models.checkout.PaymentSaveResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.orderentry.entity.OrderDetailsBillingInfo;
import com.rrd.custompoint.orderentry.entity.ProfileCreditCard;
import com.wallace.atwinxs.framework.dao.ConnectionFactory;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderDetailsBillingInfoFormBean;
import com.wallace.atwinxs.orderentry.util.OrderDetailsFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.PaymentVerificationVO;
import com.wallace.atwinxs.orderentry.vo.PaymentVerificationVOKey;

@Service
public class PaymentInformationServiceImpl extends BaseOEService implements PaymentInformationService {

	private static final Logger logger = LoggerFactory.getLogger(PaymentInformationServiceImpl.class);
	
	// CAP-44417
	private final CreditCardAuthorization creditCardAuthorization;

	// CAP-44417
	protected PaymentInformationServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService, CreditCardAuthorization pCreditCardAuthorization) {
		super(translationService, objectMapFactoryService);
		creditCardAuthorization = pCreditCardAuthorization;  // CAP-44417
	}

	@Override
	public PaymentInformationResponse getPaymentInformation(SessionContainer sc) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();
		String profileCreditCard = oeOrderSessionBean.getCardTypeAndNumber();

		PaymentInformationResponse response = new PaymentInformationResponse();
		response.setSuccess(validateOrder(response, sc, appSessionBean));

		OrderDetailsBillingInfo billingInfo = null;

		if (response.isSuccess()) {

			try {
				billingInfo = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsBillingInfo.class,
						appSessionBean.getCustomToken());
				billingInfo.setOrderID(volatileSessionBean.getOrderId());
				billingInfo.populate(volatileSessionBean, appSessionBean, oeOrderSessionBean);
			} catch (AtWinXSException e) {
				logger.error(
						"PaymentInformationServiceImpl - getPaymentInformation() call to billingInfo.populate(), failed to populate billingInfo",
						e);
				response.setSuccess(false);
			}

			if (response.isSuccess()) {

				// CAP-47894 Pass isForceCCOption as a parameter to validate and populatePaymentInformationResponse
				boolean isForceCCOption = oeOrderSessionBean.isForceCCOptionAllocation();
				validateAccessToService(billingInfo, isForceCCOption);
				populatePaymentInformationResponse(appSessionBean, userSettings, response, billingInfo,
						profileCreditCard, isForceCCOption);
			}
		}

		return response;
	}

	// CAP-47894 Add condition to check if isForceCCOption is turned on
	protected void validateAccessToService(OrderDetailsBillingInfo billingInfo, boolean isForceCCOption) throws AccessForbiddenException {
		if (!isForceCCOption 
				&& !billingInfo.isCreditCardRequired() && !billingInfo.isCreditCardOptional()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
	}

	// CAP-47894 Pass isForceCCOption as a parameter
	protected void populatePaymentInformationResponse(AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, PaymentInformationResponse response,
			OrderDetailsBillingInfo billingInfo, String profileCreditCard, boolean isForceCCOption) {

		response.setPaymentMethods(populatePaymentMethods(appSessionBean, billingInfo, isForceCCOption));
		response.setSelectedPaymentMethod(populateSelectedPaymentMethod(billingInfo, profileCreditCard, isForceCCOption));//CAP-48380
		response.setCardOptions(populateCardOptions(billingInfo));
		response.setExpirationDateMonths(populateExpirationDateMonths(billingInfo));
		response.setExpirationDateYears(populateExpirationDateYears(billingInfo));

		// Get last 4 digits of credit card if payment type is CC 
		setCreditCardDetailsLast4(appSessionBean,response, billingInfo);
		
		setCreditCardDetails(response, billingInfo);

		response.setShowSaveCreditCardFlag(appSessionBean.getProfileNumber() > AtWinXSConstant.INVALID_ID);
		response.setSaveCreditCardFlag(false);

		// CAP-47894 If credit card is not normally turned on, the user will not get email options
		if(!isForceCCOption) {
			response.setShowEmailReceipt(isShowEmailReceipt(userSettings));
			response.setEmailCreditReceiptSetting(isEmailReceiptRequired(userSettings));
			response.setEmailCreditReceiptFlag(populateEmailCreditReceiptFlag(billingInfo));
			response.setEmailCreditReceiptName(populateCreditReceiptEmail(billingInfo));
		}

		setSavedCreditCardDetails(billingInfo, response);
	}
	
	// Get last 4 digits of credit card if payment type is CC
	protected void setCreditCardDetailsLast4(AppSessionBean appSessionBean,
			PaymentInformationResponse response, OrderDetailsBillingInfo billingInfo) {
		
	    if (response.getSelectedPaymentMethod().equals(ModelConstants.CREDIT_CARD_CD)) {
	        
	    	try {
	            
	        	IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator
	            		.locate(appSessionBean.getCustomToken());
	            PaymentVerificationVO paymentVO = ordersService.getPaymentInfo(
	            		new PaymentVerificationVOKey(new OrderHeaderVOKey(billingInfo.getOrderID())));
	            if(null != paymentVO) {
	            	
	            	billingInfo.setCreditCardLast4(paymentVO.getCreditCardLast4());
	            }
		    } catch (AtWinXSException e) {
				logger.error(
						"PaymentInformationServiceImpl - setCreditCardDetailsLast4() call to "
						+ "IOEManageOrdersComponent.getPaymentInfo(), failed to populate getPaymentInfo",
						e);
				response.setSuccess(false);
			}
	    }
	}
	
	// CAP-47894 Pass isForceCCOption as a parameter
	protected List<NameValuePair<String>> populatePaymentMethods(AppSessionBean appSessionBean,
			OrderDetailsBillingInfo billingInfo, boolean isForceCCOption) {

		List<NameValuePair<String>> paymentMethods = new ArrayList<>();

		// CAP-47894 Add condition to check if isForceCCOption is turned on
		if (!isForceCCOption
				&& !billingInfo.isCreditCardRequired() && billingInfo.isCreditCardOptional()) {
			String invoiceAccount = Util
					.nullToEmpty(getTranslation(appSessionBean, SFTranslationTextConstants.INVOICE_ACCOUNT_LBL,
							SFTranslationTextConstants.INVOICE_ACCOUNT_DEF_LBL));
			paymentMethods.add(new NameValuePair<>(invoiceAccount, ModelConstants.INVOICE_ACCOUNT_CD));
		}

		// CAP-47894 Add condition to check if isForceCCOption is turned on
		if (isForceCCOption 
				|| billingInfo.isCreditCardRequired() || billingInfo.isCreditCardOptional()) {

			for (ProfileCreditCard profileCreditCard : billingInfo.getProfileCreditCards()) {
				if (hasProfileCreditCard(profileCreditCard)) {
					Map<String, Object> payByCardsReplaceMap = new HashMap<>();
					payByCardsReplaceMap.put(SFTranslationTextConstants.EXISTING_CARD_TYPE_REPLACE_TXT,
							profileCreditCard.getCardTypeOption());
					payByCardsReplaceMap.put(SFTranslationTextConstants.EXISTING_CARD_LAST_FOUR_DIGITS_REPLACE_TXT,
							profileCreditCard.getCardLastFourDigit());

					String payBySavedCC = Util
							.nullToEmpty(getTranslation(appSessionBean, SFTranslationTextConstants.PAY_BY_SAVED_CC_LBL,
									SFTranslationTextConstants.PAY_BY_SAVED_CC_DEF_LBL, payByCardsReplaceMap));
					paymentMethods
							.add(new NameValuePair<>(payBySavedCC, getPayBySavedCreditCardCode(profileCreditCard)));
				}
			}

			String payByNewCC = Util.nullToEmpty(getTranslation(appSessionBean,
					SFTranslationTextConstants.PAY_BY_NEW_CC_LBL, SFTranslationTextConstants.PAY_BY_NEW_CC_DEF_LBL));
			paymentMethods.add(new NameValuePair<>(payByNewCC, ModelConstants.CREDIT_CARD_CD));
		}
		return paymentMethods;
	}

	protected String getPayBySavedCreditCardCode(ProfileCreditCard profileCreditCard) {
		return new StringBuilder(profileCreditCard.getCardType()).append(profileCreditCard.getCardLastFourDigit())
				.toString();
	}

	protected boolean hasProfileCreditCard(ProfileCreditCard profileCreditCard) {
		return !Util.isBlankOrNull(profileCreditCard.getCardTypeOption())
				&& !Util.isBlankOrNull(profileCreditCard.getCardLastFourDigit());
	}

	protected String populateSelectedPaymentMethod(OrderDetailsBillingInfo billingInfo,
			String selectedProfileCreditCard, boolean isForceCCOption) {//CAP-48380

		String selectedPaymentOption = AtWinXSConstant.EMPTY_STRING;
		ProfileCreditCard profileCreditCard = billingInfo.getProfileCreditCards().get(0);

		if (!Util.isBlankOrNull(selectedProfileCreditCard)) {
			if (hasProfileCreditCard(profileCreditCard)
					&& selectedProfileCreditCard.equals(getPayBySavedCreditCardCode(profileCreditCard))) {
				selectedPaymentOption = selectedProfileCreditCard;
			}
		} else if (!Util.isBlankOrNull(billingInfo.getCreditCardType()) || billingInfo.isCreditCardRequired()) {
			selectedPaymentOption = ModelConstants.CREDIT_CARD_CD;
		} else if (billingInfo.isCreditCardOptional()) {
			selectedPaymentOption = ModelConstants.INVOICE_ACCOUNT_CD;
		}
		
		if(isForceCCOption) {//CAP-48380
			selectedPaymentOption = ModelConstants.CREDIT_CARD_CD;
		}

		return selectedPaymentOption;
	}

	protected List<NameValuePair<String>> populateCardOptions(OrderDetailsBillingInfo billingInfo) {

		return billingInfo.getCardTypeOptions().stream()
				.map(nameValuePair -> new NameValuePair<>(nameValuePair.getName(), nameValuePair.getValue().toString()))
				.collect(Collectors.toList());
	}

	protected List<NameValuePair<String>> populateExpirationDateMonths(OrderDetailsBillingInfo billingInfo) {

		return billingInfo.getCardTypeExpMonthOptions().stream()
				.map(nameValuePair -> new NameValuePair<>(nameValuePair.getName(), nameValuePair.getValue().toString()))
				.collect(Collectors.toList());
	}

	protected List<NameValuePair<String>> populateExpirationDateYears(OrderDetailsBillingInfo billingInfo) {

		return billingInfo.getCardTypeExpYearOptions().stream()
				.map(nameValuePair -> new NameValuePair<>(nameValuePair.getName(), nameValuePair.getValue().toString()))
				.collect(Collectors.toList());
	}

	protected void setCreditCardDetails(PaymentInformationResponse response, OrderDetailsBillingInfo billingInfo) {

		response.setPaymentName(Util.nullToEmpty(billingInfo.getCardHolderName()));
		response.setSelectedCardOption(Util.nullToEmpty(billingInfo.getCreditCardType()));
		response.setCardNumber(populateCardNumber(billingInfo.getCreditCardType(), billingInfo.getCreditCardLast4()));
		response.setSelectedExpirationDateMonth(Util.nullToEmpty(billingInfo.getCreditCardExpMonth()));
		response.setSelectedExpirationDateYear(Util.nullToEmpty(billingInfo.getCreditCardExpYear()));
	}

	protected boolean isShowEmailReceipt(OEResolvedUserSettingsSessionBean userSettings) {
		return !RouteConstants.NO_FLAG.equalsIgnoreCase(userSettings.getReceiptEmailOption());
	}

	protected boolean isEmailReceiptRequired(OEResolvedUserSettingsSessionBean userSettings) {
		return Util.yToBool(userSettings.getReceiptEmailOption());
	}

	protected boolean populateEmailCreditReceiptFlag(OrderDetailsBillingInfo billingInfo) {
		return Util.yToBool(((OrderDetailsBillingInfoFormBean) billingInfo).getCcEmailReceipt());
	}

	protected String populateCreditReceiptEmail(OrderDetailsBillingInfo billingInfo) {

		return Util.nullToEmpty(((OrderDetailsBillingInfoFormBean) billingInfo).getCcEmailReceiptAddress());
	}

	protected void setSavedCreditCardDetails(OrderDetailsBillingInfo billingInfo, PaymentInformationResponse response) {

		String paymentName = AtWinXSConstant.EMPTY_STRING;
		String cardNumber = AtWinXSConstant.EMPTY_STRING;
		String expirationDate = AtWinXSConstant.EMPTY_STRING;
		String cardTypeOption = AtWinXSConstant.EMPTY_STRING;

		ProfileCreditCard profileCreditCard = billingInfo.getProfileCreditCards().get(0);

		if (hasProfileCreditCard(profileCreditCard)) {
			paymentName = Util.nullToEmpty(profileCreditCard.getCardHolderName());
			cardNumber = populateCardNumber(profileCreditCard.getCardType(), profileCreditCard.getCardLastFourDigit());
			expirationDate = populateSavedExpirationDate(profileCreditCard);
			cardTypeOption = Util.nullToEmpty(profileCreditCard.getCardTypeOption());
		}

		response.setSavedCreditCardName(paymentName);
		response.setSavedCreditLast4Digits(cardNumber);
		response.setSavedCreditExpiration(expirationDate);
		response.setSavedCreditCardType(cardTypeOption);
	}

	protected String populateCardNumber(String cardType, String cardLastFourDigits) {
		StringBuilder cardNumber = new StringBuilder();
		if (!Util.isBlankOrNull(cardLastFourDigits)) {
			cardNumber.append("***********");
			if (OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD.equals(cardType)
					|| OrderEntryConstants.CREDIT_CARD_TYPE_VISA.equals(cardType)) {
				cardNumber.append("*");
			}
			cardNumber.append(cardLastFourDigits);
		}
		return cardNumber.toString();
	}

	protected String populateSavedExpirationDate(ProfileCreditCard profileCreditCard) {
        String expirationDate = AtWinXSConstant.EMPTY_STRING;
        if (!Util.isBlankOrNull(profileCreditCard.getCardExpiryMonth())
                && !Util.isBlankOrNull(profileCreditCard.getCardExpiryYear())) {            
            expirationDate = new StringBuilder(profileCreditCard.getCardExpiryMonth())
                    .append(AtWinXSConstant.FORWARD_SLASH).append(profileCreditCard.getCardExpiryYear()).toString();
        }
        return expirationDate;
    }
	
	//CAP-44048 - Method Logic used from CP:OrderDetailsAjaxController=>doHandleFormSubmission()
	@Override
	public PaymentSaveResponse savePaymentInformation(SessionContainer sc, PaymentSaveRequest paymentSaveRequest) 
			throws AtWinXSException {
		
		ApplicationVolatileSession volatileSession = sc.getApplicationVolatileSession();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession)sc.getModuleSession()).getOESessionBean();
		VolatileSessionBean volatileSessionBean = volatileSession.getVolatileSessionBean();
		
		Message errMsg = new Message();
		List<String> errMsgs = new ArrayList<>();
		errMsg.setErrMsgItems(errMsgs);
		
		PaymentSaveResponse paymentSaveResponse = new PaymentSaveResponse();
		paymentSaveResponse.setSuccess(validateOrder(paymentSaveResponse, sc, appSessionBean));

		OrderDetailsBillingInfo billingInfo = null;
		if (paymentSaveResponse.isSuccess()) {
			try {
				
				billingInfo = objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsBillingInfo.class,
						appSessionBean.getCustomToken());
				billingInfo.setOrderID(volatileSessionBean.getOrderId());
				billingInfo.populate(volatileSessionBean, appSessionBean, oeOrderSessionBean);

			} catch (AtWinXSException e) {
				logger.error("PaymentInformationServiceImpl - savePaymentInformation() call to billingInfo.populate(), "
						+ "failed to populate billingInfo",e);
				paymentSaveResponse.setSuccess(false);
			}
				
		}
		
		if (paymentSaveResponse.isSuccess()) { 
			
			validateAccessToService(billingInfo, oeOrderSessionBean.isForceCCOptionAllocation()); // CAP-47894
			
			//Validate Payment Method and request parameter for Payment Method Type VXXXX
			validatePaymentMethod(paymentSaveRequest, paymentSaveResponse,appSessionBean,billingInfo);
			if (paymentSaveResponse.getFieldMessages().size() > 0) {
				
				paymentSaveResponse.setSuccess(false);
				return paymentSaveResponse;
			}
			
				
			if (ModelConstants.CREDIT_CARD_CD.equals(paymentSaveRequest.getSelectedPaymentMethod())) {
				
				//Validate request parameter for Payment Method Type CC 
				validateBillingInfo1(paymentSaveRequest, paymentSaveResponse, oeOrderSessionBean, appSessionBean);
				
				//validate payment card Number Non-Blank and All card are numeric 
				validateBillingInfo2(paymentSaveRequest, paymentSaveResponse, oeOrderSessionBean, appSessionBean);
				
				//if validate BillingInfo2 true, then validate payment Card Number length AE,Masterm,VISA
				if (paymentSaveResponse.getFieldMessages().size() == 0) {
					
					validateBillingInfo3(paymentSaveRequest, paymentSaveResponse, oeOrderSessionBean, appSessionBean);
				}

				//validate card Month and Year
				validateBillingInfo4(paymentSaveRequest, paymentSaveResponse, oeOrderSessionBean, appSessionBean);
				
				//validate credit card email for New CC
				validateBillingInfo5(paymentSaveRequest, paymentSaveResponse, oeOrderSessionBean, appSessionBean);				
								
				// CAP-44417 - Added a check for the paymetricID.  If paymetricID is a value or empty string, the value is good and we can continue.
				// If null, we have a bad credit card.
				if (paymentSaveResponse.getFieldMessages().size() > 0) {
					
					paymentSaveResponse.setSuccess(false);
					return paymentSaveResponse;
				}				
				
				// CAP-44417 TH - Call authorize credit card
				String paymetricID = authorizeCreditCard(paymentSaveRequest, paymentSaveResponse, oeOrderSessionBean, appSessionBean);
				
				if (paymetricID == null)
				{
					paymentSaveResponse.setSuccess(false);
					return paymentSaveResponse;
				}
							
				retrieveNewCCAndSave(appSessionBean, volatileSessionBean, oeOrderSessionBean, paymentSaveRequest, billingInfo, paymetricID);
			}
			else {
				
				retrieveStoredCCAndSave(appSessionBean, volatileSessionBean, oeOrderSessionBean, paymentSaveRequest, billingInfo);
			}
			
		}

		SessionHandler.saveSession(sc.getModuleSession(),appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
		
		return paymentSaveResponse;
	}
	
	// CAP-44417 TH
	public String authorizeCreditCard(PaymentSaveRequest paymentSaveRequest, PaymentSaveResponse paymentSaveResponse, OEOrderSessionBean oeOrderSessionBean, AppSessionBean appSessionBean) throws AtWinXSException
	{					
		if (paymentSaveRequest.getSelectedPaymentMethod().equals(ModelConstants.CREDIT_CARD_CD))
		{
			OrderDetailsFormBean formBean = convertSaveRequestToFormBean(paymentSaveRequest, appSessionBean.getCustomToken());
			
			Message errMsg = new Message();
			List<String> errMsgs = new ArrayList<>();
			errMsg.setErrMsgItems(errMsgs);
			
			boolean isValid = creditCardAuthorization.authorizeCreditCard(formBean, errMsg, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(), appSessionBean);
			
			// If the credit card is not valid, throw an error
			if (!isValid)
			{
				if (errMsg.getErrMsgItems() != null && errMsg.getErrMsgItems().size() > 0)
				{
					paymentSaveResponse.setMessage(creditCardAuthorization.displayErrorMsg(errMsg));	
				}
				paymentSaveResponse.setSuccess(false);
				
				return null;
			}		
			else
			{
				return formBean.getBillingInfoFormBean().getPaymetricTransactionID();
			}
		}
		
		// Return empty string - this will signify there is no paymetric id and blank means still good
		return AtWinXSConstant.EMPTY_STRING;
		
	}
	
	// CAP-44417
	public OrderDetailsFormBean convertSaveRequestToFormBean(PaymentSaveRequest paymentSaveRequest, CustomizationToken customToken)
	{
		OrderDetailsFormBean formBean = new OrderDetailsFormBean();
		formBean.setBillingInfoFormBean((OrderDetailsBillingInfoFormBean)objectMapFactoryService.getEntityObjectMap().getEntity(OrderDetailsBillingInfo.class, customToken));
		formBean.getBillingInfoFormBean().setCreditCardType(paymentSaveRequest.getSelectedCardOption());
		formBean.getBillingInfoFormBean().setCreditCardExpMonth(paymentSaveRequest.getSelectedExpirationDateMonth());
		if (paymentSaveRequest.getSelectedExpirationDateYear().length() == 4)
		{
			formBean.getBillingInfoFormBean().setCreditCardExpYear(paymentSaveRequest.getSelectedExpirationDateYear().substring(2));
		}
		else
		{
			formBean.getBillingInfoFormBean().setCreditCardExpYear(paymentSaveRequest.getSelectedExpirationDateYear());
		}
		formBean.getBillingInfoFormBean().setCardHolderName(paymentSaveRequest.getPaymentName());
		formBean.getBillingInfoFormBean().setCreditCardToken(paymentSaveRequest.getCreditCardToken());		
		formBean.getBillingInfoFormBean().setSaveCardforFutureUse(paymentSaveRequest.isSaveCreditCardFlag());
		
		return formBean;
	}
	
	protected void retrieveNewCCAndSave(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeOrderSessionBean, PaymentSaveRequest paymentSaveRequest, 
			OrderDetailsBillingInfo billingInfo, String paymetricID) throws AtWinXSException {  

		//set ODbillingInfo values to save
		if (null != billingInfo) {
				
			billingInfo.setOrderID(volatileSessionBean.getOrderId());
			billingInfo.setCardHolderName(paymentSaveRequest.getPaymentName());
			billingInfo.setCreditCardType(paymentSaveRequest.getSelectedCardOption());
			billingInfo.setCreditCardExpMonth(paymentSaveRequest.getSelectedExpirationDateMonth());
			billingInfo.setCreditCardExpYear(paymentSaveRequest.getSelectedExpirationDateYear());
			//If saved for future use, store in XST490 else only in XST073
			billingInfo.setSaveCardforFutureUse(paymentSaveRequest.isSaveCreditCardFlag());
			
			//Year format changes to 2 digit
			String ccExpYear4 =paymentSaveRequest.getSelectedExpirationDateYear();
			if(ccExpYear4.length() > 2)  {
			
				billingInfo.setCreditCardExpYear(ccExpYear4.substring(ccExpYear4.length() - 2, ccExpYear4.length()));
			}
			
			//Not to use credit card any where and to use last 4 digit 
			billingInfo.setCreditCardNumber(AtWinXSConstant.EMPTY_STRING);
			String ccNumber = paymentSaveRequest.getCardNumber();
			if(ccNumber.length() > 4)  {
			
				billingInfo.setCreditCardLast4(ccNumber.substring(ccNumber.length() - 4));
			}
		}
		
		//set ODBillingInfoFormbean values to save
		OrderDetailsBillingInfoFormBean billingInfoFormbean = (OrderDetailsBillingInfoFormBean)billingInfo;
		
		if (null != billingInfoFormbean) {

			billingInfoFormbean.setCreditCardToken(paymentSaveRequest.getCreditCardToken());
			billingInfoFormbean.setPaymetricTransactionID(AtWinXSConstant.EMPTY_STRING);
			
			billingInfoFormbean.setCcEmailReceipt(Util.boolToY(paymentSaveRequest.isEmailCreditReceiptFlag()));
			if (paymentSaveRequest.isEmailCreditReceiptFlag())			{
				
				billingInfoFormbean.setCcEmailReceiptAddress(paymentSaveRequest.getEmailCreditReceiptName());
			}
			else {	

				billingInfoFormbean.setCcEmailReceiptAddress(AtWinXSConstant.EMPTY_STRING);
			}
		}
		
		//If saved card flag initialize ProfileCreditCard
		if (null != billingInfo && billingInfo.isSaveCardforFutureUse()) {

			ProfileCreditCard profileCreditCard = objectMapFactoryService.getEntityObjectMap().getEntity(ProfileCreditCard.class, 
					appSessionBean.getCustomToken());
			profileCreditCard.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getProfileNumber(), 
					appSessionBean.getLoginID(), billingInfoFormbean);
			
			//CAP-44417 TH - Added paymetric id to saved credit card
			if (!Util.isBlankOrNull(paymetricID))
			{
				profileCreditCard.setPaymetricTransactionID(paymetricID);
			}
			
			List<ProfileCreditCard> profileCreditCards = new ArrayList<>();
			profileCreditCards.add(profileCreditCard);
			billingInfo.setProfileCreditCards(profileCreditCards);
		}
		
		savePayment(billingInfoFormbean, appSessionBean, volatileSessionBean, billingInfo);
		setBillingMethodInSession(oeOrderSessionBean, paymentSaveRequest, billingInfoFormbean);
	}
	
	
	protected void retrieveStoredCCAndSave(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeOrderSessionBean, PaymentSaveRequest paymentSaveRequest,  
			OrderDetailsBillingInfo billingInfo) throws AtWinXSException {  

		OrderDetailsBillingInfoFormBean billingInfoFormbean = (OrderDetailsBillingInfoFormBean)billingInfo;
		if(!billingInfoFormbean.getProfileCreditCards().isEmpty()) {
			ProfileCreditCard profileCC = billingInfoFormbean.getProfileCreditCards().get(0);
		
			billingInfo.setCardHolderName(profileCC.getCardHolderName());
			billingInfo.setCreditCardType(profileCC.getCardType());
			billingInfo.setCreditCardExpMonth(profileCC.getCardExpiryMonth());
			billingInfo.setCreditCardExpYear(profileCC.getCardExpiryYear());
		
			billingInfo.setCreditCardNumber(AtWinXSConstant.EMPTY_STRING);
			billingInfo.setCreditCardLast4(profileCC.getCardLastFourDigit());
			billingInfoFormbean.setCreditCardToken(profileCC.getCardToken());
		}	
		
		//set ODBillingInfoFormbean values to save
		billingInfoFormbean.setCcEmailReceipt(Util.boolToY(paymentSaveRequest.isEmailCreditReceiptFlag()));
		billingInfoFormbean.setPaymetricTransactionID(AtWinXSConstant.EMPTY_STRING);
		if (paymentSaveRequest.isEmailCreditReceiptFlag())			{
				
			billingInfoFormbean.setCcEmailReceiptAddress(paymentSaveRequest.getEmailCreditReceiptName());
		}
		else {	

			billingInfoFormbean.setCcEmailReceiptAddress(AtWinXSConstant.EMPTY_STRING);
		}
			
		//If IV, delete order record in XST073 if exist, If VXXXX, restore record from XST490 to XST073 with/without email
		String billingMethod = paymentSaveRequest.getSelectedPaymentMethod();
		
		if ( (null != billingMethod) && ModelConstants.INVOICE_ACCOUNT_CD.equals(billingMethod)) {
	
			savePayment(null, appSessionBean, volatileSessionBean, billingInfo);
		}
		else {
			
			savePayment(billingInfoFormbean, appSessionBean, volatileSessionBean, billingInfo);
		}
		
		setBillingMethodInSession(oeOrderSessionBean, paymentSaveRequest, billingInfoFormbean);
	}
	
	
	public void setBillingMethodInSession(OEOrderSessionBean oeOrderSessionBean, PaymentSaveRequest paymentSaveRequest,
			OrderDetailsBillingInfoFormBean billingInfoFormbean ) {
		
		String billingMethod = paymentSaveRequest.getSelectedPaymentMethod();
		String cardTypeAndNumber = AtWinXSConstant.EMPTY_STRING;
		
		//if billing Method is pay by saved credit card, set billing method to CC
		if ( (null != billingMethod) && !ModelConstants.INVOICE_ACCOUNT_CD.equals(billingMethod) 
				&& !ModelConstants.CREDIT_CARD_CD.equals(billingMethod)) {
			
			cardTypeAndNumber = billingInfoFormbean.getCreditCardType() + billingInfoFormbean.getCreditCardLast4();
		}
		
		if ( (null != billingMethod) && ModelConstants.CREDIT_CARD_CD.equals(billingMethod)
				&& billingInfoFormbean.isSaveCardforFutureUse() ) {
		
		cardTypeAndNumber = billingInfoFormbean.getCreditCardType() + billingInfoFormbean.getCreditCardLast4();
		}
		oeOrderSessionBean.setCardTypeAndNumber(cardTypeAndNumber);
	}
	
	
	protected void savePayment(OrderDetailsBillingInfoFormBean billingInfoFormbean, AppSessionBean appSessionBean, 
		VolatileSessionBean volatileSessionBean, OrderDetailsBillingInfo billingInfo) throws AtWinXSException {  

		Connection passedConnection = null;
		OrderHeaderVOKey orderKey = new OrderHeaderVOKey(volatileSessionBean.getOrderId());
		PaymentVerificationVO paymentInfo = getPaymentVerificationVO(billingInfoFormbean, volatileSessionBean);
				
		try {
		
			passedConnection = ConnectionFactory.createConnection();
			passedConnection.setAutoCommit(false);
			
			//Existing CP PaymentVerificationDAO is used save where BillToAttention not needed so set to false    
			billingInfo.save(paymentInfo, passedConnection, appSessionBean, orderKey, false, AtWinXSConstant.EMPTY_STRING);
		}
		catch (AtWinXSMsgException me){ 
			
			try	{
				
				if(passedConnection !=null) passedConnection.rollback();
			}
			catch (SQLException e) {
				
				// call to logger instead of empty brackets
				logger.debug(this.getClass().getName() + " - " + e.getMessage(),e);
			}
			throw me;
		}
		catch (AtWinXSWrpException ex){
			
			try {
				
				if(passedConnection !=null) passedConnection.rollback();
			}
			catch (SQLException e) {
				
				// call to logger instead of empty brackets
				logger.debug(this.getClass().getName() + " - " + e.getMessage(),e);
			}
			throw new AtWinXSWrpException(ex, this.getClass().getName());
		}
		catch (AtWinXSException e) {
			
			try {
			
				if(passedConnection !=null) passedConnection.rollback();
			}
			catch (SQLException ignored) {
	
				logger.debug("This error is meant to be ignored",ignored);
			}
			throw new AtWinXSWrpException(e, this.getClass().getName());
		}
		catch (SQLException e) {
			
			try {
					
				if(passedConnection !=null) passedConnection.rollback();
			}
			catch (SQLException ignored) {
	
				logger.debug("This error is meant to be ignored",ignored);
			}
			throw new AtWinXSWrpException(e, this.getClass().getName());
		}catch (Exception e) {
			
			try	{
			
				if(passedConnection !=null) passedConnection.rollback();
			}catch (SQLException ignored) {
			
				// added logger.debug call to previously empty catch block
				logger.debug("This error is meant to be ignored",ignored);
			}
			throw new AtWinXSWrpException(e, this.getClass().getName());
		}
		finally {
			
			if(passedConnection !=null) ConnectionFactory.returnConnection(passedConnection);
			
		}
	}
	
	
	protected PaymentVerificationVO getPaymentVerificationVO(OrderDetailsBillingInfoFormBean formBean, VolatileSessionBean volatileSessionBean )
	{
		
		//Set CreditCardToken to Empty until 
		PaymentVerificationVO vo = null;
		if (formBean != null) {
			vo = new PaymentVerificationVO(volatileSessionBean.getOrderId(), 
					formBean.getCardHolderName(), 		// cardHolderName
					formBean.getCreditCardType(), 		// creditCardTypeCode
					AtWinXSConstant.EMPTY_STRING,		// formBean.getCreditCardNumber() creditCardNum
					AtWinXSConstant.EMPTY_STRING,		// formBean.getSaltValue() saltValue
					formBean.getCreditCardExpMonth(), 	// creditCardExpMonth
					formBean.getCreditCardExpYear(), 	// creditCardExpYear
					formBean.getCreditCardNumEnc(), 	// creditCardNumEnc
					formBean.getCreditCardLast4(), 		// creditCardLast4
					(Util.yToBool(formBean.getCcEmailReceipt())
							|| Util.yToBool(formBean.getCcEmailReceiptOption())) ? 
									formBean.getCcEmailReceiptAddress() : "", 
					formBean.getCreditCardToken(),  	// creditCardToken
					AtWinXSConstant.INVALID_ID			// Routing Id placeholder.
					);
		}	
		return vo;
	}

	//CAP-44048 - Method to validate Payment Method and request parameter for Payment Method Type VXXXX 
	protected void validatePaymentMethod(PaymentSaveRequest request, PaymentSaveResponse response, 
			AppSessionBean asb,OrderDetailsBillingInfo billingInfo) throws AtWinXSException {

		String fNamePaymentMethod = PAY_PAYMENTMETHOD;
		
		String fNameEmailCreditReceiptName = PAY_CCEMAILRECEIPTNAME;
		
		String paymentMethod=Util.nullToEmpty(request.getSelectedPaymentMethod());
		String ccEmail=Util.nullToEmpty(request.getEmailCreditReceiptName());
		boolean ccEmailFlag=request.isEmailCreditReceiptFlag();
		
		String cardTypeAndNumber = AtWinXSConstant.EMPTY_STRING;
		OrderDetailsBillingInfoFormBean billingInfoFormbean = (OrderDetailsBillingInfoFormBean)billingInfo;
		if(!billingInfoFormbean.getProfileCreditCards().isEmpty()) {

			ProfileCreditCard profileCC = billingInfoFormbean.getProfileCreditCards().get(0);
			cardTypeAndNumber = profileCC.getCardType() + profileCC.getCardLastFourDigit();
		}
		
		if (Util.isBlank(paymentMethod)) {
			response.getFieldMessages().put(fNamePaymentMethod, 
					 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		} else if (paymentMethod.length() > PAY_PAYMENT_METHOD_MAX_SIZE_5) {
			
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					PAY_PAYMENT_METHOD_MAX_SIZE_5 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fNamePaymentMethod,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		} 
		else if (paymentMethod.length() == 2 && (!paymentMethod.equalsIgnoreCase("CC") 
				&& !paymentMethod.equalsIgnoreCase("IV"))) {
			
			response.getFieldMessages().put(fNamePaymentMethod, 
					 buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
		else if (paymentMethod.length() == 5 && !paymentMethod.equalsIgnoreCase(cardTypeAndNumber)) {
			
			response.getFieldMessages().put(fNamePaymentMethod, 
					 buildErrorMessage(SFTranslationTextConstants.NOT_VALID_ERR, asb, null));
		}
		else if (paymentMethod.equalsIgnoreCase(cardTypeAndNumber)
				&& ccEmailFlag && Util.isBlank(ccEmail)) {
			
			response.getFieldMessages().put(fNameEmailCreditReceiptName, 
					 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
		}
		else if (paymentMethod.equalsIgnoreCase(cardTypeAndNumber)
				&& ccEmailFlag && ccEmail.length() > PAY_CC_EMAIL_MAX_SIZE_650) {
			
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
					PAY_CC_EMAIL_MAX_SIZE_650 + AtWinXSConstant.EMPTY_STRING);
			response.getFieldMessages().put(fNameEmailCreditReceiptName,
					buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
		}
		
		if (!ccEmailFlag) {
			
			request.setEmailCreditReceiptName(AtWinXSConstant.EMPTY_STRING);
		}
	}
	
	//CAP-44048 - //Validate request parameter for Payment Method Type CC
	protected void validateBillingInfo1(PaymentSaveRequest request, PaymentSaveResponse response,
			OEOrderSessionBean oeSessionBean, AppSessionBean asb) throws AtWinXSException {

		String procurementCardOption = oeSessionBean.getUserSettings().getProcurementCardOption();
		
		String cardType = Util.nullToEmpty(request.getSelectedCardOption());
		String cardHolderName = Util.nullToEmpty(request.getPaymentName());
		String cardToken = Util.nullToEmpty(request.getCreditCardToken());
		
		String fNameCardType = PAY_CARDTYPE;
		String fNameCardHolderName = PAY_CARDHOLDERNAME;
		String fNameCardToken = PAY_CARDTOKEN;
		

		if (procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED) 
				|| procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_OPTIONAL)) {
			
			if (Util.isBlank(cardType)) {
				
				response.getFieldMessages().put(fNameCardType, 
						 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			} else if (cardType.length() > PAY_CARD_OPTION_MAX_SIZE_1) {
			
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						PAY_CARD_OPTION_MAX_SIZE_1 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fNameCardType,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}   
			
			if (Util.isBlank(cardHolderName)) {
			
				response.getFieldMessages().put(fNameCardHolderName, 
						 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			} else if (cardHolderName.length() > PAY_PAYMENT_NAME_MAX_SIZE_25) {
				
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						PAY_PAYMENT_NAME_MAX_SIZE_25 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fNameCardHolderName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}   
			
			
			if (Util.isBlank(cardToken)) {
				
				response.getFieldMessages().put(fNameCardToken, 
						 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
				
			} else if (cardToken.length() > PAY_CC_TOKEN_MAX_SIZE_16) {
			
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						PAY_CC_TOKEN_MAX_SIZE_16 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fNameCardToken,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
		}
	}
	
	//CAP-44048 - Method to validate Payment Card Number 
	protected void validateBillingInfo2(PaymentSaveRequest paymentSaveRequest, PaymentSaveResponse response, 
			OEOrderSessionBean oeSession, AppSessionBean asb) throws AtWinXSException {
		
		String procurementCardOption = oeSession.getUserSettings().getProcurementCardOption();
		
		String cardNumber = Util.nullToEmpty(paymentSaveRequest.getCardNumber());

		String fNameCardNumber = PAY_CARDNUMBER;
		
		if (procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED) 
				|| procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_OPTIONAL)) {
		
			
			if (Util.isBlank(cardNumber)) {
				
				response.getFieldMessages().put(fNameCardNumber, 
						 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			// Credit Card should contain masked for other char except last 4 digit 
			else if (!Util.isBlank(cardNumber)) {
				
				boolean isValid = false;
				try {
					
					Long.parseLong(cardNumber);
				} 
				catch (NumberFormatException e) {
					
					isValid = true;
				}
				
				if(!isValid) {
					
					response.getFieldMessages().put(fNameCardNumber, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
							asb.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_CARD_NUM_ERROR)));
				}
			}	
		}
	}
	
	//CAP-44048 - Method to validate Payment Card Number
	protected void validateBillingInfo3(PaymentSaveRequest paymentSaveRequest, PaymentSaveResponse response, 
			OEOrderSessionBean oeSession, AppSessionBean asb) throws AtWinXSException {
		
		String procurementCardOption = oeSession.getUserSettings().getProcurementCardOption();
		
		String labelCardType;
		
		String cardType = Util.nullToEmpty(paymentSaveRequest.getSelectedCardOption());
		String cardNumber = Util.nullToEmpty(paymentSaveRequest.getCardNumber());
		String fNameCardNumber = PAY_CARDNUMBER;
		
		if (procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED) 
				|| procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_OPTIONAL)) {
		
			// In StoreFront , American Express Credit Card should contain 16 numbers.
			if (cardNumber.length() > 0 && cardType.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX)
					&& cardNumber.length() != OrderEntryConstants.CREDIT_CARD_LENGTH_MC) {

				response.getFieldMessages().put(fNameCardNumber, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_CARD_LENGTH_AMEX_ERROR)));
			}
			// Master Card Credit Card should contain 16 numbers.
			else if (cardNumber.length() > 0 && cardType.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD)
					&& cardNumber.length() != OrderEntryConstants.CREDIT_CARD_LENGTH_MC) {
				
				Map<String, Object> attrReplaceMap = new HashMap<>();
				labelCardType = Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_MASTERCARD_LBL));
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_CREDIT_CARD, labelCardType);
				
				response.getFieldMessages().put(fNameCardNumber, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_CARD_LENGTH_ERROR_MSG, attrReplaceMap)));
			}
			// VISA Credit Card should contain 16 numbers.
			else if (cardNumber.length() > 0 && cardType.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_TYPE_VISA)
					&& cardNumber.length() != OrderEntryConstants.CREDIT_CARD_LENGTH_VISA) {
				
				Map<String, Object> attrReplaceMap = new HashMap<>();
				
				labelCardType = Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_VISA_LBL));
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_CREDIT_CARD, labelCardType);

				
				response.getFieldMessages().put(fNameCardNumber, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_CARD_LENGTH_ERROR_MSG, attrReplaceMap)));
			}
		}
	}
	
	//CAP-44048 - Method to validate Payment Card Month and year
	protected void validateBillingInfo4(PaymentSaveRequest paymentSaveRequest, PaymentSaveResponse response, 
			OEOrderSessionBean oeSession, AppSessionBean asb) throws AtWinXSException {
		
		String procurementCardOption = oeSession.getUserSettings().getProcurementCardOption();

		String expMonth = Util.nullToEmpty(paymentSaveRequest.getSelectedExpirationDateMonth());
		String expYear = Util.nullToEmpty(paymentSaveRequest.getSelectedExpirationDateYear());
		
		String fNameCardNumber = PAY_CARDNUMBER;
		String fNameExpMonth = PAY_EXPMONTH;
		String fNameExpYear = PAY_EXPYEAR;
		
		
		if (procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED) 
				|| procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_OPTIONAL)) {

			// Check expiration date
			try {
				
				Calendar now = Calendar.getInstance();

				// what century are we in?
				int currentYear = now.get(Calendar.YEAR);
				int currentCentury = currentYear / 100;
				currentCentury = currentCentury * 100;
				int iExpYear = Integer.parseInt(expYear);

				// make sure the expiration year contains the century
				if ((expYear.length() < Integer.toString(currentYear).length()) && (iExpYear < currentCentury)) {
					
					iExpYear += currentCentury;
				}

				int iExpMonth = Integer.parseInt(expMonth);
				int currentMonth = now.get(Calendar.MONTH);

				// check that year-month is not prior to current
				// year-month validation month should be 01 - 12, year current to current + 10
				if ( (iExpYear < currentYear) || (iExpYear >= currentYear + 10) ) {
					
					response.getFieldMessages().put(fNameExpYear, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
							asb.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR)));
				}
				else if (((iExpYear == currentYear) && (iExpMonth < currentMonth + 1 )) || (iExpMonth < 1 || iExpMonth > 12)) {
					
					response.getFieldMessages().put(fNameExpMonth, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
							asb.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR)));
					
				}
			}
			catch (NumberFormatException e) {
				
				response.getFieldMessages().put(fNameExpYear, Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), 
						asb.getCustomToken(), TranslationTextConstants.TRANS_NM_ENTER_VALID_EXP_DATE_ERROR)));
			}
		}
	}
	
	//CAP-44048 - Method to validate credit card email
	protected void validateBillingInfo5(PaymentSaveRequest request, PaymentSaveResponse response,
			OEOrderSessionBean oeSessionBean, AppSessionBean asb) throws AtWinXSException {

		String procurementCardOption = oeSessionBean.getUserSettings().getProcurementCardOption();
		
		String fNameEmailCreditReceiptName = PAY_CCEMAILRECEIPTNAME;
		
		String ccEmail=Util.nullToEmpty(request.getEmailCreditReceiptName());
		boolean ccEmailFlag=request.isEmailCreditReceiptFlag();
		
		if (procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_REQUIRED) 
				|| procurementCardOption.equalsIgnoreCase(OrderEntryConstants.CREDIT_CARD_INFO_OPTIONAL)) {
						
			if (ccEmailFlag && Util.isBlank(ccEmail)) {
				response.getFieldMessages().put(fNameEmailCreditReceiptName, 
						 buildErrorMessage(SFTranslationTextConstants.MUST_NOT_BE_BLANK_ERR, asb, null));
			}
			if (ccEmailFlag && ccEmail.length() > PAY_CC_EMAIL_MAX_SIZE_650) {
				Map<String, Object> replaceMap = new HashMap<>();
				replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG,
						PAY_CC_EMAIL_MAX_SIZE_650 + AtWinXSConstant.EMPTY_STRING);
				response.getFieldMessages().put(fNameEmailCreditReceiptName,
						buildErrorMessage(SFTranslationTextConstants.MAX_CHARS_ERR, asb, replaceMap));
			}
			
		}
	}
	
	private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {
		return Util.nullToEmpty(TranslationTextTag.processMessage(asb.getDefaultLocale(), asb.getCustomToken(), errorKey, replaceMap));
	}
	
}