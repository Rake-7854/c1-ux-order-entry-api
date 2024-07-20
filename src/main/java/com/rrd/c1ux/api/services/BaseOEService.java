/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions:
 *  Date        Modified By     Jira                        Description
 *  --------    -----------     -----------------------     --------------------------------
 *	04/03/23	A Boomker		CAP-39512					Initial Version
 *	05/18/23	Sakthi M		CAP-40547					C1UX BE - API Fixes - Error checks for valid and non-submitted order not coded in Delivery and Order info load APIs
 *	05/16/23	A Boomker		CAP-40687					Moved validateOrder from OrderSummary
 *	06/07/23	A Boomker		CAP-38154					Methods converted from CP for C1UX validation
 *	06/22/23	L De Leon		CAP-41373					Added methods for quick copy order validations
 *	06/28/23	L De Leon		CAP-41373					Refactored code for JUnits
 *	07/11/23	Satishkumar A	CAP-41970					C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 *  07/28/23	Sakthi M		CAP-42545					C1UX BE - Create API to cancel the current order (cancelOrderInProgress)
 *	08/01/23	L De Leon		CAP-42519					Refactored code for JUnits
 *  08/09/23	Krishna Natarajan	CAP-42803					Added a null check on coutry bean zip label to replace with Zip/Postal Code
 *	08/15/23	L De Leon		CAP-42519					Refactored code for additional validation
 *	08/29/23	L De Leon		CAP-43197					Added buildFeatureMap() method
 *	02/12/24	Krishna Natarajan	CAP-47109 				Added another constructor to accommodate changes for tests
 *	02/13/24	L De Leon		CAP-46960					Added buildI18nBean() method
 *	05/13/24	L De Leon		CAP-48938/CAP-48977			Modified methods to populate delivery options list
 *	05/28/24	A Boomker		CAP-48604					Moved instantiateULReqBean() to base so Cust docs can access too
 *	06/25/24	Krishna Natarajan	CAP-50471				Added a new method to return the File delivery options
 */
package com.rrd.c1ux.api.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.BaseResponse;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.catalogitems.FileDeliveryOption;
import com.rrd.c1ux.api.services.factory.OEAssemblerFactoryService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorService;
import com.rrd.c1ux.api.services.items.locator.OEManageOrdersComponentLocatorServiceImpl;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.gwt.common.util.CountryBean;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails;
import com.wallace.atwinxs.catalogs.vo.FeatureFavoriteItemData;
import com.wallace.atwinxs.catalogs.vo.FeaturedItemsCompositeVO;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.framework.util.fileupload.FileDownload;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.lists.ao.UploadListAssembler;
import com.wallace.atwinxs.lists.ao.UploadListRequestBean;
import com.wallace.atwinxs.lists.session.ManageListsSession;
import com.wallace.atwinxs.lists.util.ManageListsUtil;
import com.wallace.atwinxs.locale.ao.I18nBean;
import com.wallace.atwinxs.locale.ao.StateBean;
import com.wallace.atwinxs.orderentry.ao.OESavedOrderAssembler;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderstatus.util.OrderStatusConstants;

public abstract class BaseOEService extends BaseService {

	private static final Logger baseOELogger = LoggerFactory.getLogger("BaseOEService");

	protected static final String PHONE_CHARS_VALID = "0123456789 ()-.+";
	protected static final String PHONE_CHARS_SPECIAL = " ()-.";
	protected static final String ERR_MESSAGE_CANNOT_RETRIVE_ORD="Could not retrieve order header record for orderID ";

	protected final OEManageOrdersComponentLocatorService oeManageOrdersLocatorService;

	protected BaseOEService(TranslationService translationService) {
		super(translationService);
		this.oeManageOrdersLocatorService = new OEManageOrdersComponentLocatorServiceImpl();
	}

	protected BaseOEService(TranslationService translationService, OEManageOrdersComponentLocatorService oeManageOrdersLocatorService) {
		super(translationService);
		this.oeManageOrdersLocatorService = oeManageOrdersLocatorService;
	}

  protected BaseOEService(TranslationService translationService, ObjectMapFactoryService objService) {
		super(translationService, objService);
		this.oeManageOrdersLocatorService = new OEManageOrdersComponentLocatorServiceImpl();
	}

//CAP-47109
	protected BaseOEService(TranslationService translationService, ObjectMapFactoryService objService,
			OEAssemblerFactoryService oeAssemblerFactoryService) {
		super(translationService, objService, oeAssemblerFactoryService);
		this.oeManageOrdersLocatorService = new OEManageOrdersComponentLocatorServiceImpl();
	}

	// CAP-42519
	protected BaseOEService(TranslationService translationService, ObjectMapFactoryService objService,
			OEManageOrdersComponentLocatorService oeManageOrdersLocatorService) {
		super(translationService, objService);
		this.oeManageOrdersLocatorService = oeManageOrdersLocatorService;
	}

// CAP-39512 - Need a common location for making sure the order is not submitted and similar error messages
  // they may be used in multiple areas of the application
  // routed orders will not be returned here as they can be edited
  public boolean isOrderSubmittedCannotEdit(int orderID)
  {
	  try {
		  IOEManageOrdersComponent manageOrder = OEManageOrdersComponentLocator.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		  OrderHeaderVOKey key = new OrderHeaderVO(orderID);
		  OrderHeaderVO headerVo = manageOrder.getOrderHeader(key);
		  if (headerVo != null) {
			  switch(headerVo.getOrderXSStatusCode())
			  {
			  	case OrderStatusConstants.SUBMITTED_ORDER:
			  	case OrderStatusConstants.REQUESTED_ORDER:
			  	case OrderStatusConstants.DENIED_ORDER:
			  	case OrderStatusConstants.FUTURE_DATED_ORDER:
			  	case OrderStatusConstants.CANCELED_ORDER:
			  	case OrderStatusConstants.PENDING_CAMPAIGN_ORDER:
			  	case OrderStatusConstants.PENDING_HOLD_ORDER:
			  	case OrderStatusConstants.PENDING_SUBMISSION_ORDER:
			  	case OrderStatusConstants.RMS_STATUS_M:
			  	case OrderStatusConstants.RMS_STATUS_R:
			  		return true; // C1UX checkout cannot modify these
			default:
			  		return (headerVo.getOrderXSStatusCode().startsWith("B"));
			  }
		  }
	  }
	  catch(Exception e)
	  { // if we can't find it, assume it's submitted
		  baseOELogger.error(ERR_MESSAGE_CANNOT_RETRIVE_ORD + orderID, e);
	  }
	  return true; // assume cannot edit if cannot find it.
  }

  protected boolean isSessionOrderSubmittedCannotEdit(SessionContainer sc)
  {
		if (hasValidCart(sc))
		{
			return isOrderSubmittedCannotEdit(sc.getApplicationVolatileSession().getVolatileSessionBean().getOrderId().intValue());
		}
		return false;
  }

  public boolean hasValidCart(SessionContainer sc)
  {
	  VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
	  boolean validCart = (volatileSessionBean.getOrderId() != null);
	  validCart = validCart && (volatileSessionBean.getOrderId().intValue() > 0) && (volatileSessionBean.getShoppingCartCount() > 0);
	  return validCart;
  }
  protected String getOrderAlreadySubmittedMsg(AppSessionBean appSessionBean) throws AtWinXSException
  {
	 return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				"orderalreadysubmitted");
  }

	protected String getNoOrderErrorMessage(AppSessionBean appSessionBean) throws AtWinXSException {
		return TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), TranslationService.NO_ORDER_IN_PROGRESS_KEY);
	}

	// CAP-40687 - moved from order summary
	public boolean validateOrder(BaseResponse response, SessionContainer sc, AppSessionBean appSessionBean) throws AtWinXSException {
		if (!hasValidCart(sc))
		{
			response.setSuccess(false);
			response.setMessage(getNoOrderErrorMessage(appSessionBean));
			return false;
		}
		else if (isSessionOrderSubmittedCannotEdit(sc))
		{
			response.setSuccess(false);
			response.setMessage(getOrderAlreadySubmittedMsg(appSessionBean));
			return false;
		}
		return true;
	}
	// CAP-38154 - split this to reduce cognitive complexity from original CP method
	protected String replaceDelimiters(String value, String delimiter)
	{
		String email = value.replace(";", delimiter);
		email = email.replace(":", delimiter);
		email = email.replace("\r", delimiter);
		email = email.replace("\n", delimiter);
		email = email.replaceAll("\\s{2,}", delimiter);
		email = email.replaceAll("[, ]+", delimiter);
		return email.trim();
	}
	// CAP-38154 - copied from OrderDetailsValidator.validateMultipleEmailAddresses(
	/**
	 * This version of the method will return a null if the value will not parse successfully. The parsed version is returned if this was successful.
	 * @param value
	 * @return
	 */
	protected String parseDelimitedEmails(String value)
	{
		String email = Util.nullToEmpty(value).trim();
		if (Util.isEmailDelimited(email))
		{
			String delimiter = ",";
			email = replaceDelimiters(email, delimiter);

			if(email.indexOf(delimiter) > -1)
			{
				return splitAndValidateMultipleEmails(delimiter, email);
			}
			return AtWinXSConstant.EMPTY_STRING;
		}
		else if (!Util.isValidEmailFormat(email))
		{
			return null;
		}
		return email;
	}

	private String splitAndValidateMultipleEmails(String delimiter, String email)
	{
		StringBuilder tempEmail = new StringBuilder();
		String[] parsedString = email.split(delimiter, -1);
		for(int i = 0; i<parsedString.length; i++)
		{
			if(!Util.isValidEmailFormat(parsedString[i].trim()))
			{
				return null;
			}
			else
			{
				tempEmail.append(parsedString[i].trim());
				if(i != parsedString.length-1) tempEmail.append(",");
			}
		}
		return tempEmail.toString();
	}

	// CAP-38154 - copied from OEManageOrdersHelper
	/**
	 * Method formatPhone.
	 * Checks for invalid characters, and if found returns null.  Otherwise returns the
	 * number with special characters removed.
	 *
	 * @param String number
	 * @return String
	 *
	 */
	public String formatPhone(String number)
	{
		if (number == null)
		{
			return null;
		}

		number = number.trim();
		int length = number.length();
		StringBuilder bufFormattedNumber = new StringBuilder(length);

		for (int i = 0; i < length; i++)
		{
			char tempChar = number.charAt(i);

			// if a character is not valid, return null
			if (PHONE_CHARS_VALID.indexOf(tempChar) == -1)
			{
				return null;
			}

			// if a character is not special, append to the final result
			if (PHONE_CHARS_SPECIAL.indexOf(tempChar) == -1)
			{
				bufFormattedNumber.append(tempChar);
			}
		}

		return bufFormattedNumber.toString();
	}

	// CAP-41373
	public boolean validateNoOrderInProgress(BaseResponse response, SessionContainer sc, AppSessionBean appSessionBean) {

		boolean isOrderInProgress = hasValidCart(sc);

		if (isOrderInProgress) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_IN_PROGRESS_ERR_MSG,
					SFTranslationTextConstants.ORDER_IN_PROGRESS_DEF_ERR_MSG));
		}
		return !isOrderInProgress;
	}

	// CAP-41373
	public boolean validateOrderExistandSubmitted(BaseResponse response, int orderID, AppSessionBean appSessionBean) {

		boolean isOrderExistingAndSubmitted = isOrderExistingAndSubmitted(orderID);

		if (!isOrderExistingAndSubmitted) {
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.ORDER_NOT_SUBMITTED_ERR_MSG,
					SFTranslationTextConstants.ORDER_NOT_SUBMITTED_DEF_ERR_MSG));
		}

		return isOrderExistingAndSubmitted;
	}

	protected boolean isOrderExistingAndSubmitted(int orderID) {
		OrderHeaderVO headerVo = getOrderHeader(orderID);
		return null != headerVo;
	}

	// CAP-42519
	public OrderHeaderVO getOrderHeader(int orderID) {
		OrderHeaderVO headerVo = null;
		if (orderID > AtWinXSConstant.INVALID_ID) {
			try {
				IOEManageOrdersComponent manageOrder = oeManageOrdersLocatorService
						.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
				OrderHeaderVOKey key = new OrderHeaderVO(orderID);
				headerVo = manageOrder.getOrderHeader(key);
			} catch (AtWinXSException ex) {
				// failed to retrieve headerVo, assume order does not exist
				baseOELogger.error(new StringBuilder(ERR_MESSAGE_CANNOT_RETRIVE_ORD).append(orderID).toString(), ex);
			}
		}
		return headerVo;
	}

	public OESavedOrderAssembler getSavedOrderAssembler(AppSessionBean applicationSessionBean,
			VolatileSessionBean volatileSessionBean) {

		return (new OESavedOrderAssembler(volatileSessionBean, applicationSessionBean.getCustomToken(),
				applicationSessionBean.getDefaultLocale(), applicationSessionBean.getApplyExchangeRate(),
				applicationSessionBean.getCurrencyLocale()));
	}

	public List<NameValuePair<CountryBean>> getGeographicLabelsAndStateList(AppSessionBean appSessionBean)
			throws AtWinXSException {

		List<NameValuePair<CountryBean>> countries = new ArrayList<>();

		try {
			Iterator<String> countryIterator = UtilCountryInfo.getCountriesByName();
			while (countryIterator.hasNext()) {
				com.wallace.atwinxs.locale.ao.CountryBean cBean = UtilCountryInfo
						.getCountryByName(countryIterator.next());
				CountryBean gwtBean = new CountryBean();
				gwtBean.setHasStates(cBean.getCountryHasStates());

				//CAP-41970 - CountryName and CountryCode mapping
				gwtBean.setCountryName(cBean.getCountryName());
				gwtBean.setCountryCode(cBean.getCountryCode());

				String stateLabelText = translationService.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(), cBean.getStateLabelTag());

				String zipLabelText = null;
				if (!Util.isBlankOrNull(cBean.getZipLabelTag())) {
					zipLabelText = translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), cBean.getZipLabelTag());
				} else {
					zipLabelText = translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), ModelConstants.ZIP_POSTAL_LABEL);
				}

				gwtBean.setStateLabel(stateLabelText);
				gwtBean.setZipLabel(zipLabelText);

				gwtBean.setCountryCodeShort(cBean.getCountryCodeShort());
				if (cBean.getCountryHasStates()) {
					Collection<NameValuePair<String>> stateList = new ArrayList<>();
					Iterator<String> states = cBean.getStatesInCountryByName();
					while (states.hasNext()) {
						StateBean sBean = cBean.getStateInCountryByName(states.next());
						stateList.add(new NameValuePair<>(sBean.getStateCodeText(), sBean.getStateCode()));
					}
					gwtBean.setStates(stateList);
				}
				countries.add(new NameValuePair<>(cBean.getCountryCode(), gwtBean));
			}

		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

		return countries;
	}

	//CAP-42454 Changes
	 public boolean isCancelNewOrderOnly(int orderID)
	  {
		  try {
			  IOEManageOrdersComponent manageOrder = OEManageOrdersComponentLocator.locate(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			  OrderHeaderVOKey key = new OrderHeaderVO(orderID);
			  OrderHeaderVO headerVo = manageOrder.getOrderHeader(key);
			  if (headerVo != null) {
				 if(headerVo.getOrderXSStatusCode().equals(OrderStatusConstants.NEW_ORDER)){
				  		return true; // C1UX checkout cannot modify these
				  }
				 else {
				  		return (headerVo.getOrderXSStatusCode().startsWith("B"));
				  }
			  }
		  }
		  catch(Exception e)
		  {
			  baseOELogger.error(ERR_MESSAGE_CANNOT_RETRIVE_ORD + orderID, e);
		  }
		  return false; // assume cannot edit if cannot find it.
	  }

	public void saveSession(BaseSession session, int sessionID, int serviceID) throws AtWinXSException
	{
		SessionHandler.saveSession(session, sessionID, serviceID);
	}

	// CAP-43197
	public Map<String, String> buildFeatureMap(FeatureFavoriteItemData featureFavoriteItemData) {

		Map<String, String> featureMap = null;
		Collection<FeaturedItemsCompositeVO> featureItemData = featureFavoriteItemData.getFeaturedItems();

		// CAP-43197 LDL - Did not include retrieval of feature items based on
		// relationship id. If needed in future enhancements, check
		// ItemHelper.buildFeatureMap() in CustomPoint

		if (null != featureItemData && !featureItemData.isEmpty()) {

			featureMap = new HashMap<>();

			for (FeaturedItemsCompositeVO vo : featureItemData) {
				featureMap.put(vo.getFeaturedMouseOverText(), vo.getFeaturedSFIconPath());
			}
		}

		return featureMap;
	}

	// CAP-46960
	protected I18nBean buildI18nBean(AppSessionBean appSessionBean)
	{
		return new I18nBean(appSessionBean.getDefaultLocale(),
							appSessionBean.getDefaultLanguage(),
							appSessionBean.getDefaultTimeZone(),
							appSessionBean.getApplyExchangeRate(),
							appSessionBean.getCurrencyLocale());
	}

	// CAP-47516
	public String getInvalidErrorMessage(AppSessionBean appSessionBean, String label) {
		String errorMsg = getTranslation(appSessionBean, SFTranslationTextConstants.NOT_VALID_ERR,
				SFTranslationTextConstants.INVALID_FLD_VAL_DEF_ERR_MSG);
		return getBaseErrorMessage(label, new StringBuilder(AtWinXSConstant.BLANK_SPACE).append(errorMsg).toString());
	}

	// CAP-47516
	public String getRequiredErrorMessage(AppSessionBean appSessionBean, String label) {
		String errorMsg = getTranslation(appSessionBean, SFTranslationTextConstants.REQUIRED_FLD_ERR_MSG,
				SFTranslationTextConstants.REQUIRED_FLD_DEF_ERR_MSG);
		return getBaseErrorMessage(label, new StringBuilder(AtWinXSConstant.BLANK_SPACE).append(errorMsg).toString());
	}

	// CAP-47516
	public String getExactRequiredErrorMessage(AppSessionBean appSessionBean, int requiredSize) {
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, Integer.toString(requiredSize));
		String errorMsg = getTranslation(appSessionBean, SFTranslationTextConstants.EXACT_CHARS_ERR,
				SFTranslationTextConstants.EXACT_CHARS_DEF_ERR, replaceMap);
		return getBaseErrorMessage(AtWinXSConstant.EMPTY_STRING, errorMsg);
	}

	// CAP-47516
	public String getMustNotExceedErrorMessage(AppSessionBean appSessionBean, int maxSize) {
		Map<String, Object> replaceMap = new HashMap<>();
		replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, Integer.toString(maxSize));
		String errorMsg = getTranslation(appSessionBean, SFTranslationTextConstants.MAX_CHARS_ERR,
				SFTranslationTextConstants.MAX_CHARS_DEF_ERR, replaceMap);
		return getBaseErrorMessage(AtWinXSConstant.EMPTY_STRING, errorMsg);
	}

	// CAP-47516
	public String getBaseErrorMessage(String label, String errorMsg) {
		return new StringBuilder(label).append(errorMsg).toString();
	}

	// CAP-49015 // CAP-48938 CAP-48977 refactored for reusability
	public String getFileDeliveryLabel(AppSessionBean appSessionBean, String deliveryOpt) {
		FileDeliveryOption deliveryOption = populateDeliveryOption(null, appSessionBean, deliveryOpt, false);
		return deliveryOption.getDisplayLabel();
	}

	// CAP-48938 // CAP-48977
	public FileDeliveryOption populateDeliveryOption(OEResolvedUserSettingsSessionBean userSettings,
			AppSessionBean appSessionBean, String deliveryOpt, boolean isLoadDisplayOptions) {
		FileDeliveryOption deliveryOption = new FileDeliveryOption();
		String deliveryLabel = AtWinXSConstant.EMPTY_STRING;
		List<NameValuePair<String>> displayOptions = new ArrayList<>();
		switch (deliveryOpt) {
		case OrderEntryConstants.EFD_METHOD:
			deliveryLabel = getTranslation(appSessionBean, SFTranslationTextConstants.EFD_METHOD_LBL,
					SFTranslationTextConstants.EFD_METHOD_DEF_LBL);
			if (isLoadDisplayOptions) {
				getEDeliveryOption(appSessionBean, displayOptions);
			}
			break;
		case OrderEntryConstants.PRINT_ENABLED_OPTION:
			deliveryLabel = getTranslation(appSessionBean, SFTranslationTextConstants.PRINT_ENABLED_OPTION_LBL,
					SFTranslationTextConstants.PRINT_ENABLED_OPTION_DEF_LBL);
			if (isLoadDisplayOptions) {
				displayOptions
						.add(new NameValuePair<>(getTranslation(appSessionBean, SFTranslationTextConstants.PRINT_LBL,
								SFTranslationTextConstants.PRINT_DEF_LBL), OrderEntryConstants.PRINT_METHOD));
			}
			break;
		case OrderEntryConstants.EFD_PRINT_ENABLED_OPTION:
			deliveryLabel = getTranslation(appSessionBean, SFTranslationTextConstants.EFD_PRINT_ENABLED_OPTION_LBL,
					SFTranslationTextConstants.EFD_PRINT_ENABLED_OPTION_DEF_LBL);
			if (isLoadDisplayOptions) {
				getAllOptionsEfdPreferred(appSessionBean, displayOptions);
			}
			break;
		case OrderEntryConstants.PRINT_EFD_ENABLED_OPTION:
			deliveryLabel = getTranslation(appSessionBean, SFTranslationTextConstants.PRINT_EFD_ENABLED_OPTION_LBL,
					SFTranslationTextConstants.PRINT_EFD_ENABLED_OPTION_DEF_LBL);
			if (isLoadDisplayOptions) {
				getEDeliveryOption(appSessionBean, displayOptions);
				displayOptions
						.add(new NameValuePair<>(
								getTranslation(appSessionBean, SFTranslationTextConstants.PRINT_PREF_LBL,
										SFTranslationTextConstants.PRINT_PREF_DEF_LBL),
								OrderEntryConstants.PRINT_METHOD));
				displayOptions.add(new NameValuePair<>(
						getTranslation(appSessionBean, SFTranslationTextConstants.PRINT_EDELIVERY_LBL,
								SFTranslationTextConstants.PRINT_EDELIVERY_DEF_LBL),
						OrderEntryConstants.PRINT_EFD_METHOD));
			}
			break;
		case OrderEntryConstants.EFD_PRINT_OVERRIDE_OPTION:
			deliveryLabel = getTranslation(appSessionBean, SFTranslationTextConstants.EFD_PRINT_OVERRIDE_OPTION_LBL,
					SFTranslationTextConstants.EFD_PRINT_OVERRIDE_OPTION_DEF_LBL);
			if (isLoadDisplayOptions) {
				getEFDOnlyPrintOverrideOptions(userSettings, appSessionBean, displayOptions);
			}
			break;
		case OrderEntryConstants.EFD_REQ_PRINT_PREF_OPTION:
			deliveryLabel = getTranslation(appSessionBean, SFTranslationTextConstants.EFD_REQ_PRINT_PREF_OPTION_LBL,
					SFTranslationTextConstants.EFD_REQ_PRINT_PREF_OPTION_DEF_LBL);
			if (isLoadDisplayOptions) {
				displayOptions
						.add(new NameValuePair<>(
								getTranslation(appSessionBean, SFTranslationTextConstants.EDELIVERY_PREF_LBL,
										SFTranslationTextConstants.EDELIVERY_PREF_DEF_LBL),
								OrderEntryConstants.EFD_METHOD));
				displayOptions.add(new NameValuePair<>(
						getTranslation(appSessionBean, SFTranslationTextConstants.EDELIVERY_PRINT_LBL,
								SFTranslationTextConstants.EDELIVERY_PRINT_DEF_LBL),
						OrderEntryConstants.EFD_PRINT_METHOD));
			}
			break;
		default:
			break;
		}

		deliveryOption.setOptionCode(deliveryOpt);
		deliveryOption.setDisplayLabel(deliveryLabel);
		deliveryOption.setDisplayOptions(displayOptions);
		return deliveryOption;
	}

	protected void getEFDOnlyPrintOverrideOptions(OEResolvedUserSettingsSessionBean userSettings,
			AppSessionBean appSessionBean, List<NameValuePair<String>> displayOptions) {
		if (userSettings.isAllowPrintOverride()) {
			getAllOptionsEfdPreferred(appSessionBean, displayOptions);
		} else {
			getEDeliveryOption(appSessionBean, displayOptions);
		}
	}

	protected void getEDeliveryOption(AppSessionBean appSessionBean, List<NameValuePair<String>> displayOptions) {
		displayOptions.add(new NameValuePair<>(getTranslation(appSessionBean, SFTranslationTextConstants.EDELIVERY_LBL,
				SFTranslationTextConstants.EDELIVERY_DEF_LBL), OrderEntryConstants.EFD_METHOD));
	}

	// CAP-48938 CAP-48977
	protected void getAllOptionsEfdPreferred(AppSessionBean appSessionBean,
			List<NameValuePair<String>> displayOptions) {
		displayOptions
				.add(new NameValuePair<>(getTranslation(appSessionBean, SFTranslationTextConstants.EDELIVERY_PREF_LBL,
						SFTranslationTextConstants.EDELIVERY_PREF_DEF_LBL), OrderEntryConstants.EFD_METHOD));
		displayOptions.add(new NameValuePair<>(getTranslation(appSessionBean, SFTranslationTextConstants.PRINT_LBL,
				SFTranslationTextConstants.PRINT_DEF_LBL), OrderEntryConstants.PRINT_METHOD));
		displayOptions
				.add(new NameValuePair<>(
						getTranslation(appSessionBean, SFTranslationTextConstants.EDELIVERY_PRINT_LBL,
								SFTranslationTextConstants.EDELIVERY_PRINT_DEF_LBL),
						OrderEntryConstants.EFD_PRINT_METHOD));
	}

	// CAP-48938 CAP-48977
	public List<FileDeliveryOption> populateDeliveryOptionsList(OEResolvedUserSettingsSessionBean userSettings,
			AppSessionBean appSessionBean) {
		return populateDeliveryOptionsList(userSettings, appSessionBean, true);
	}

	// CAP-48938 CAP-48977
	public List<FileDeliveryOption> populateDeliveryOptionsList(OEResolvedUserSettingsSessionBean userSettings,
			AppSessionBean appSessionBean, boolean isLoadDisplayOptions) {
		List<FileDeliveryOption> deliveryOptionsList = new ArrayList<>();
		deliveryOptionsList.add(populateDeliveryOption(userSettings, appSessionBean,
				OrderEntryConstants.EFD_ENABLED_OPTION, isLoadDisplayOptions));
		deliveryOptionsList.add(populateDeliveryOption(userSettings, appSessionBean,
				OrderEntryConstants.PRINT_ENABLED_OPTION, isLoadDisplayOptions));
		deliveryOptionsList.add(populateDeliveryOption(userSettings, appSessionBean,
				OrderEntryConstants.EFD_PRINT_ENABLED_OPTION, isLoadDisplayOptions));
		deliveryOptionsList.add(populateDeliveryOption(userSettings, appSessionBean,
				OrderEntryConstants.PRINT_EFD_ENABLED_OPTION, isLoadDisplayOptions));
		deliveryOptionsList.add(populateDeliveryOption(userSettings, appSessionBean,
				OrderEntryConstants.EFD_PRINT_OVERRIDE_OPTION, isLoadDisplayOptions));
		deliveryOptionsList.add(populateDeliveryOption(userSettings, appSessionBean,
				OrderEntryConstants.EFD_REQ_PRINT_PREF_OPTION, isLoadDisplayOptions));
		return deliveryOptionsList;
	}
// CAP-48604 - moved here from checkout for reuse by custom docs
	// CAP-48277 - CP Method from ListsServiceImpl to instantiate Upload List Request Bean
	public UploadListRequestBean instantiateULReqBean(DistributionListDetails distributionListDetails,
			AppSessionBean appSessionBean) {

		return new UploadListRequestBean(appSessionBean.getSiteID(),
					appSessionBean.getBuID(),
					distributionListDetails.getListName(),
					"",
					distributionListDetails.getIsPrivate(),
					distributionListDetails.getUploadedDate(),
					distributionListDetails.getUploadedDate(),
					distributionListDetails.getRecordCount(),
					appSessionBean.getLoginID(),
					distributionListDetails.getFilesize(),
					distributionListDetails.getCustomerFileName(),
					distributionListDetails.getHasHeadings(),
					distributionListDetails.getSourceFileName(),
					distributionListDetails.getModifiedFileName(),
					false, //isVisible,
					distributionListDetails.getSheetName(),
					"", //statusCode,
					appSessionBean.getLoginID(),
					distributionListDetails.getListDescription(),
					"", //updateUser,
					distributionListDetails.getUploadedDate(),
					distributionListDetails.getIsExcel(),
					appSessionBean.getProfileID(),
					distributionListDetails.getMoveUpdateEnabled(),
					distributionListDetails.getListOwner(),
					distributionListDetails.getPafID(),
					distributionListDetails.getPafExpiration(),
					distributionListDetails.getOrigExcelFileName());
	}

	public ManageListsSession loadListSession(SessionContainer sc) throws AtWinXSException {
		return (ManageListsSession) SessionHandler.loadSession(sc.getApplicationSession().getAppSessionBean().getSessionID(), AtWinXSConstant.LISTS_SERVICE_ID);
	}

	public UploadListAssembler getUploadListAssembler(AppSessionBean appSessionBean) {
		return new UploadListAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
	}

	public boolean isConvertingExcel() throws AtWinXSException {
		return ManageListsUtil.isConvertExcel();
	}

	public void moveFileIfNotConvertingExcel(DistributionListDetails distributionListDetails, AppSessionBean appSessionBean) throws AtWinXSException {
		if (!isConvertingExcel()) {
			String dupFileName = distributionListDetails.createDuplicateFileWithXSID(distributionListDetails, appSessionBean);
			FileDownload.performDelete(appSessionBean.getGlobalFileUploadPath(), dupFileName);
			distributionListDetails.setModifiedFileName(dupFileName);
		}

	}
	
	//CAP-50471
	public Map<String,String> populateFileDeliveryOption() {
		HashMap<String,String> deliveryOptions = new HashMap<>();
		deliveryOptions.put(OrderEntryConstants.EFD_ENABLED_OPTION,SFTranslationTextConstants.EFD_METHOD_DEF_LBL);
		deliveryOptions.put(OrderEntryConstants.PRINT_ENABLED_OPTION,SFTranslationTextConstants.PRINT_ENABLED_OPTION_DEF_LBL);
		deliveryOptions.put(OrderEntryConstants.EFD_PRINT_ENABLED_OPTION,SFTranslationTextConstants.EFD_PRINT_ENABLED_OPTION_DEF_LBL);
		deliveryOptions.put(OrderEntryConstants.PRINT_EFD_ENABLED_OPTION,SFTranslationTextConstants.PRINT_EFD_ENABLED_OPTION_DEF_LBL);
		deliveryOptions.put(OrderEntryConstants.EFD_PRINT_OVERRIDE_OPTION,SFTranslationTextConstants.EFD_PRINT_OVERRIDE_OPTION_DEF_LBL);
		deliveryOptions.put(OrderEntryConstants.EFD_REQ_PRINT_PREF_OPTION,SFTranslationTextConstants.EFD_REQ_PRINT_PREF_OPTION_DEF_LBL);
		return deliveryOptions;
	}	
}