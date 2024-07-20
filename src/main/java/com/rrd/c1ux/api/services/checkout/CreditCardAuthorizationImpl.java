/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By			JIRA#			Description
 * 	--------	-----------			----------		--------------------------------
 *  10/06/23	T Harmon			CAP-44417		Added credit card authorization
 */

package com.rrd.c1ux.api.services.checkout;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.orderentry.entity.CreditCardDecline;
import com.rrd.custompoint.orderentry.entity.ProfileCreditCard;
import com.rrd.custompoint.services.util.WebServicesConstants;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.orderentry.util.OrderDetailsBillingInfoFormBean;
import com.wallace.atwinxs.orderentry.util.OrderDetailsFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

import paymetric.xipaysoap30.message.ArrayOfInfoItem;
import paymetric.xipaysoap30.message.ITransactionHeader;
import paymetric.xipaysoap30.message.InfoItem;
import paymetric.xipaysoap30.message.ObjectFactory;
import primesys.xipaysoap.message.BasicHttpBinding_IXiPayProxy;

@Service
public class CreditCardAuthorizationImpl extends BaseOEService implements CreditCardAuthorization {

	private static final Logger logger = LoggerFactory.getLogger(CreditCardAuthorization.class);
	
	@Value("${paymetric.wsdl}")
	private String paymetricWsdl;
	
	protected CreditCardAuthorizationImpl(TranslationService translationService) {
		super(translationService);
	}
	
	@Override
	public boolean authorizeCreditCard(OrderDetailsFormBean formBean, Message errMsg, CustomizationToken customToken,
			Locale locale, AppSessionBean appSessionBean) throws AtWinXSException {
		
		XSProperties orderEntryProperties = PropertyUtil.getProperties(AtWinXSConstant.PROP_ORDER_ENTRY);		
		String endpointUrl = orderEntryProperties.getProperty("PayMetrics.EndpointUrl");
		
		//default values for dates and address fields, AVS validation is turned off so these values won't be validated
		String defaultDate = "1899-12-30T00:00:00";
		String defaultAddress1 = "text1";
		String defaultCity = "text1";
		String defaultCountry = "USA";
		String defaultState = "FL";
		String defaultZip = "00000";
		String defaultCurrency = "USD";
		
		ITransactionHeader xiTranRQ = new ITransactionHeader();
		ITransactionHeader xiTranRS = null;
		boolean isAuthorized = false;
		
		ObjectFactory factory = new ObjectFactory();

		// Initialize The XiPay Transaction Object
		try
		{
			DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			xiTranRQ.setAuthorizationDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setBillingDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setCaptureDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setCreationDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setLastModificationDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setOrderDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setSettlementDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));
			xiTranRQ.setShippingCaptureDate(datatypeFactory.newXMLGregorianCalendar(defaultDate));			
		} catch (DatatypeConfigurationException e)
		{
			throw new AtWinXSException(e.getMessage(),"OrderDetailsUtils");
		}
		
		xiTranRQ.setCardPresent(0);
		xiTranRQ.setModifiedStatus(0);
		xiTranRQ.setPacketOperation(0);
		xiTranRQ.setStatusCode(0);
		xiTranRQ.setMerchantID(factory.createITransactionHeaderMerchantID(setMerchantID(formBean.getBillingInfoFormBean().getCreditCardType(), orderEntryProperties)));
		xiTranRQ.setAmount(factory.createITransactionHeaderAmount(setAuthAmount(formBean.getBillingInfoFormBean().getCreditCardType(), orderEntryProperties)));
		xiTranRQ.setCardExpirationDate(factory.createITransactionHeaderCardExpirationDate(formBean.getBillingInfoFormBean().getCreditCardExpMonth() + formBean.getBillingInfoFormBean().getCreditCardExpYear()));
		xiTranRQ.setCardHolderAddress1(factory.createITransactionHeaderCardHolderAddress1(defaultAddress1));
		xiTranRQ.setCardHolderCity(factory.createITransactionHeaderCardHolderCity(defaultCity));
		xiTranRQ.setCardHolderCountry(factory.createITransactionHeaderCardHolderCountry(defaultCountry));
		xiTranRQ.setCardHolderName(factory.createITransactionHeaderCardHolderName(formBean.getBillingInfoFormBean().getCardHolderName()));
		xiTranRQ.setCardHolderState(factory.createITransactionHeaderCardHolderState(defaultState));
		xiTranRQ.setCardHolderZip(factory.createITransactionHeaderCardHolderZip(defaultZip));
		xiTranRQ.setCardNumber(factory.createITransactionHeaderCardNumber(formBean.getBillingInfoFormBean().getCreditCardToken()));
		String cardTypeCode = formBean.getBillingInfoFormBean().getCreditCardType();
		xiTranRQ.setCardType(factory.createITransactionHeaderCardType(getCardTypeCode(cardTypeCode, orderEntryProperties)));
		xiTranRQ.setCurrencyKey(factory.createITransactionHeaderCurrencyKey(defaultCurrency));
		//CAP-33032
		xiTranRQ.setCardDataSource(factory.createITransactionHeaderCardDataSource(OrderEntryConstants.PAYMETRIC_CARD_DATA_SOURCE));
				
		ArrayOfInfoItem arrayOfInfoItem = factory.createArrayOfInfoItem();
				
		arrayOfInfoItem.getInfoItem().add(newInfoItem(factory, "POS", "WCSS"));
		arrayOfInfoItem.getInfoItem().add(newInfoItem(factory, "TR_CARD_CIDIND", "0"));
		
		// CAP-27105 - add new parameter for Visa or Mastercard only at this time
		if ((OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD.equals(cardTypeCode))
				|| (OrderEntryConstants.CREDIT_CARD_TYPE_VISA.equals(cardTypeCode)))
		{
			String transTypeMsgCode = getTransMsgTypeCode(formBean.getBillingInfoFormBean(), appSessionBean);
			logger.info("Authorizing card of type " + cardTypeCode + " with param msgType " + transTypeMsgCode +" for profile " + appSessionBean.getProfileNumber());
			arrayOfInfoItem.getInfoItem().add(newInfoItem(factory, OrderEntryConstants.PAYMETRIC_REQUEST_TRANS_MSGTYPE, transTypeMsgCode));
		}
		xiTranRQ.setInfoItems(factory.createArrayOfInfoItem(arrayOfInfoItem));

		primesys.xipaysoap.message.XiPay.class.getClassLoader();
		URL wsdlUrl = primesys.xipaysoap.message.XiPay.class.getClassLoader().getResource(paymetricWsdl);
		String serviceName = "http://Primesys/XiPaySoap/message";
		String nameSpace = "XiPay";
	        
		BasicHttpBinding_IXiPayProxy iXiPayService = new BasicHttpBinding_IXiPayProxy(wsdlUrl, serviceName, nameSpace);
		Map<String, Object> requestContext = ((BindingProvider)iXiPayService._getDescriptor().getProxy()).getRequestContext();
		requestContext.put("connection_timeout" , WebServicesConstants.CLIENT_CONNECTION_TIMEOUT_PROPERTY);
		requestContext.put("write_timeout" , WebServicesConstants.CLIENT_WRITE_TIMEOUT_PROPERTY);
		requestContext.put("timeout" , WebServicesConstants.CLIENT_RESPONSE_TIMEOUT_PROPERTY);
		iXiPayService._getDescriptor().setEndpoint(endpointUrl);
		
		// Call XiPay Web Service and Perform an Authorization
		try
		{
			xiTranRS = iXiPayService.authorize(xiTranRQ);
			isAuthorized = xiTranRS.getStatusCode() == 100 ? true : false;	
			
			if(!isAuthorized)
			{
				logger.info("Card Authorization failed for card for profile " + appSessionBean.getProfileNumber()); //CAP-33032
				String failedMessage = Util.nullToEmpty(TranslationTextTag.processMessage(locale, customToken, TranslationTextConstants.TRANS_NM_PAYMETRIC_AUTH_ERRMSG));
				failedMessage += " " + xiTranRS.getMessage().getValue();
				
				errMsg.getErrMsgItems().add(failedMessage);
				
				//CAP-18138 RDG
				doLogCCDeclineMessage(appSessionBean, formBean.getBillingInfoFormBean(), failedMessage);
			} else {
				logger.info("Authorization successful for transaction ID " + xiTranRS.getTransactionID().getValue() + " for profile " + appSessionBean.getProfileNumber()); //CAP-33032
				// CAP-27105
				if ((OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD.equals(cardTypeCode))
						|| (OrderEntryConstants.CREDIT_CARD_TYPE_VISA.equals(cardTypeCode)))
				{
					JAXBElement<ArrayOfInfoItem> infoResponse = xiTranRS.getInfoItems();
					if ((infoResponse != null) && (infoResponse.getValue() != null) 
							&& (infoResponse.getValue().getInfoItem() != null) && (!infoResponse.getValue().getInfoItem().isEmpty()))
					{
						List<InfoItem> items = infoResponse.getValue().getInfoItem();
						for (InfoItem property : items)
						{
							if (OrderEntryConstants.PAYMETRIC_RESPONSE_TRANSID.equals(property.getKey().getValue()))
							{
								formBean.getBillingInfoFormBean().setPaymetricTransactionID(property.getValue().getValue());
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			String failedMessage = Util.nullToEmpty(TranslationTextTag.processMessage(locale, customToken, "credit_card_token_err"));
			errMsg.getErrMsgItems().add(failedMessage);
			
			logger.debug("Paymetric webservice error with message : " + e.getMessage());
			logger.error("Paymetric webservice error with message : " + e.getMessage());
		}
		
		return isAuthorized;
	}
	
	// CAP-27105 - add new parameter for Visa or Mastercard only at this time
		private static String getTransMsgTypeCode(OrderDetailsBillingInfoFormBean bean, AppSessionBean appSessionBean) throws AtWinXSException 
		{
			ProfileCreditCard profileCreditCard = ObjectMapFactory.getEntityObjectMap().getEntity(ProfileCreditCard.class, AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			profileCreditCard.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getProfileNumber(), appSessionBean.getLoginID(), bean);
			boolean saved = profileCreditCard.alreadySaved();
			if (saved)
			{
				return OrderEntryConstants.CREDIT_CARD_USE_TYPE_ALREADY_STORED;				
			}
			if (bean.isSaveCardforFutureUse())
			{
				return OrderEntryConstants.CREDIT_CARD_USE_TYPE_WILL_STORE;
			}
			return OrderEntryConstants.CREDIT_CARD_USE_TYPE_ONCE;
		}

		private static InfoItem newInfoItem(ObjectFactory factory, String key, String value)
		{
			InfoItem infoItem = factory.createInfoItem();
			infoItem.setKey(factory.createInfoItemKey(key));
			infoItem.setValue(factory.createInfoItemValue(value));
			
			return infoItem;		
		}
		
		private static String getCardTypeCode(String cardType, XSProperties orderEntryProperties)
		{
			String cardTypeCode = "";
			
			String[] cardTypeCodes = orderEntryProperties.getProperty("PayMetrics.CardType").split("\\|");
			
			if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX)) cardTypeCode = cardTypeCodes[0];
			else if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD)) cardTypeCode = cardTypeCodes[1];
			else if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_VISA)) cardTypeCode = cardTypeCodes[2];
			
			return cardTypeCode;
		}
		
		//set authorization amount based on card type
		private static String setAuthAmount(String cardType, XSProperties orderEntryProperties)
		{
			String amount = "";
			
			String[] authAmounts = orderEntryProperties.getProperty("PayMetrics.AuthAmount").split("\\|");
			
			if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX)) amount = authAmounts[0];
			else if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD)) amount = authAmounts[1];
			else if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_VISA)) amount = authAmounts[2];
			
			return amount;
		}
		
		//set merchantID based on card type
		private static String setMerchantID(String cardType, XSProperties orderEntryProperties)
		{
			String merchantID = "";
			
			String[] merchantIds = orderEntryProperties.getProperty("PayMetrics.MerchantID").split("\\|");

			if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_AMEX)) merchantID = merchantIds[0];
			else if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_MASTERCARD)) merchantID = merchantIds[1];
			else if(cardType.equals(OrderEntryConstants.CREDIT_CARD_TYPE_VISA)) merchantID = merchantIds[2];

			return merchantID;
		}
		
		/**
		 * This method will insert a log for declined credit card in XST497 table
		 * CAP-18138 RDG
		 * @param asb
		 * @param billingformBean
		 * @param failedMessage
		 * @throws AtWinXSException
		 */
		public static void doLogCCDeclineMessage(AppSessionBean asb, OrderDetailsBillingInfoFormBean billingformBean, String failedMessage) throws AtWinXSException
		{
			CreditCardDecline ccDecline = ObjectMapFactory.getEntityObjectMap().getEntity(CreditCardDecline.class, asb.getCustomToken());
			ccDecline.setSiteID(asb.getSiteID());
			ccDecline.setBuID(asb.getBuID());
			ccDecline.setLoginID(asb.getLoginID());
			ccDecline.setCardType(billingformBean.getCreditCardType());
			ccDecline.setCreditCardToken(billingformBean.getCreditCardToken());
			ccDecline.setDeclineMessage(failedMessage);
			ccDecline.create();
		}
		
		public String displayErrorMsg(Message msg)
		{
			StringBuilder errMsg = new StringBuilder();
			if (msg != null)
			{
				if (!Util.isBlankOrNull(msg.getErrGeneralMsg()))
				{
					errMsg.append(msg.getErrGeneralMsg());
				}
				
				if (msg.getErrMsgItems() != null)
				{
					for (String errorItem : msg.getErrMsgItems())
					{						
						errMsg.append(errorItem);
					}
				}
			}
			return errMsg.toString();
		}

}
