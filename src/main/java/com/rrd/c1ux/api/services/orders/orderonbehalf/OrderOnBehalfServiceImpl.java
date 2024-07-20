/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	11/27/23	L De Leon			CAP-44467				Initial Version
 *	12/19/23	L De Leon			CAP-45939				Added list of shared ID users
 *	12/22/23 	Satishkumar A		CAP-45709				C1UX BE - Set OOB Mode for CustomPoint session
 *	01/03/24	Krishna Natarajan	CAP-46227 				Change to check the profile to execute 
 *	01/05/24	S Ramachandran		CAP-46273				OOB search to return requestor results if exist on empty search with 422
 *	02/08/24 	Satishkumar A		CAP-46959				C1UX BE - Handle OrderOnBehalf (API- toggleoob) to swap to requestors without checking isAllowOrderOnBehalf flag				
 */
package com.rrd.c1ux.api.services.orders.orderonbehalf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.orders.oob.OOBRequest;
import com.rrd.c1ux.api.models.orders.oob.OOBResponse;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchRequest;
import com.rrd.c1ux.api.models.orders.oob.OrderOnBehalfSearchResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.gwt.orderonbehalf.client.OrderOnBehalfSearchCriteria;
import com.rrd.custompoint.gwt.orderonbehalf.client.OrderOnBehalfSearchResult;
import com.rrd.custompoint.orderentry.entity.OrderOnBehalfRecords;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.admin.vo.ProfileSearchCriteriaVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ILoginInterface;
import com.wallace.atwinxs.interfaces.IOrderOnBehalfComponent;
import com.wallace.atwinxs.orderentry.ao.OrderOnBehalfAssembler;
import com.wallace.atwinxs.orderentry.ao.OrderOnBehalfUtil;
import com.wallace.atwinxs.orderentry.locator.OrderOnBehalfLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderonbehalf.session.OrderOnBehalfSearchFormBean;
import com.wallace.atwinxs.orderonbehalf.session.OrderOnBehalfSessionBean;

@Service
public class OrderOnBehalfServiceImpl extends BaseOEService implements OrderOnBehalfService {

	private static final Logger logger = LoggerFactory.getLogger(OrderOnBehalfServiceImpl.class);
	//CAP-45709
	private IAdminComponentLocator mAdminComponentLocator;
	
	protected OrderOnBehalfServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService, IAdminComponentLocator adminComponentLocator) {
		super(translationService, objectMapFactoryService);
		mAdminComponentLocator = adminComponentLocator;
	}

	@Override
	public OrderOnBehalfSearchResponse getOOBInfo(SessionContainer sc, OrderOnBehalfSearchRequest request)
			throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		if (!appSessionBean.isInRequestorMode() && !oeOrderSessionBean.getUserSettings().isAllowOrderOnBehalf()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		OrderOnBehalfSearchResponse response = new OrderOnBehalfSearchResponse();
		response.setSuccess(true);

		if (hasValidCart(sc)) {
			response.setSuccess(false);
			response.setMessage(
					getTranslation(appSessionBean, SFTranslationTextConstants.OOB_CANNOT_CHANGE_REQUESTOR_MSG,
							SFTranslationTextConstants.OOB_CANNOT_CHANGE_REQUESTOR_DEFAULT_MSG));
		} else {
			validateAndDoOOBSearch(request, appSessionBean, response);
		}

		//CAP-46273 - display requestor results if exist irrespective of search results/validation     
		if (appSessionBean.isInRequestorMode()) {
			response.setUserID(appSessionBean.getLoginID());
			response.setProfileID(appSessionBean.getProfileID());
			response.setFirstName(appSessionBean.getFirstName());
			response.setLastName(appSessionBean.getLastName());
			response.setEmailAddr(appSessionBean.getEmailAddress());
			response.setPhone(appSessionBean.getPhoneNumber());
			response.setInOOBMode(true);
		}

		return response;
	}

	protected boolean validateAndDoOOBSearch(OrderOnBehalfSearchRequest request, AppSessionBean appSessionBean,
			OrderOnBehalfSearchResponse response) {

		boolean hasValidationPassed = true;
		boolean isSearchSuccess = true;

		if (request.isSearch()) {

			if (Util.isBlankOrNull(request.getUserID()) && Util.isBlankOrNull(request.getProfileID())
					&& Util.isBlankOrNull(request.getFirstName()) && Util.isBlankOrNull(request.getLastName())) {
				response.setSuccess(false);
				response.setMessage(
						getTranslation(appSessionBean, SFTranslationTextConstants.SPECIFY_SEARCH_CRITERIA_MSG,
								SFTranslationTextConstants.SPECIFY_SEARCH_CRITERIA_DEFAULT_MSG));
				hasValidationPassed = false;

			} else {

				Map<String, String> fieldErrors = new HashMap<>();

				// Validate user ID criteria
				validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getUserID()),
						ModelConstants.OOB_USER_ID_CRITERIA_FIELD_NAME, ModelConstants.OOB_USER_ID_CRITERIA_MAX_LENGTH,
						fieldErrors);

				// Validate profile ID criteria
				validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getProfileID()),
						ModelConstants.OOB_PROFILE_ID_CRITERIA_FIELD_NAME, ModelConstants.OOB_PROFILE_ID_CRITERIA_MAX_LENGTH,
						fieldErrors);

				// Validate First Name criteria
				validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getFirstName()),
						ModelConstants.OOB_FIRST_NAME_CRITERIA_FIELD_NAME, ModelConstants.OOB_NAME_CRITERIA_MAX_LENGTH,
						fieldErrors);

				// Validate Last Name criteria
				validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getLastName()),
						ModelConstants.OOB_LAST_NAME_CRITERIA_FIELD_NAME, ModelConstants.OOB_NAME_CRITERIA_MAX_LENGTH,
						fieldErrors);

				if(fieldErrors.isEmpty()) {
					OrderOnBehalfSearchCriteria criteria = populateSearchCriteria(request, appSessionBean);
					isSearchSuccess = retrieveAndSetOOBUsers(appSessionBean, response, criteria);
				} else {
					response.setSuccess(false);
					response.setFieldMessages(fieldErrors);
					hasValidationPassed = false;
				}
			}
		}

		return hasValidationPassed && isSearchSuccess;
	}

	protected boolean retrieveAndSetOOBUsers(AppSessionBean appSessionBean, OrderOnBehalfSearchResponse response,
			OrderOnBehalfSearchCriteria criteria) {
		boolean isSuccess = true;
		OrderOnBehalfRecords ordOnBehalfEntity = objectMapFactoryService.getEntityObjectMap()
				.getEntity(OrderOnBehalfRecords.class, appSessionBean.getCustomToken());
		List<OrderOnBehalfSearchResult> oobUsers = new ArrayList<>();
		List<NameValuePair<String>> oobSharedIdUsers = new ArrayList<>();

		try {
			oobUsers = (List<OrderOnBehalfSearchResult>) ordOnBehalfEntity.populate(criteria);
			populateOobShareIdUsers(appSessionBean, criteria, oobUsers, oobSharedIdUsers); // CAP-45939
		} catch (AtWinXSException e) {
			logger.error(e.getMessage(), e);
			isSuccess = false;
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}

		response.setCount(oobUsers.size());
		response.setOobUsers(oobUsers);
		response.setOobSharedIdUsers(oobSharedIdUsers); // CAP-45939

		return isSuccess;
	}

	// CAP-45939
	protected void populateOobShareIdUsers(AppSessionBean appSessionBean, OrderOnBehalfSearchCriteria criteria,
			List<OrderOnBehalfSearchResult> oobUsers, List<NameValuePair<String>> oobSharedIdUsers)
			throws AtWinXSException {
		if (!oobUsers.isEmpty() && oobUsers.stream().anyMatch(user -> Util.isBlankOrNull(user.getUserID()))) {
			IOrderOnBehalfComponent component = OrderOnBehalfLocator.locate(appSessionBean.getCustomToken());
			ProfileSearchCriteriaVO vo = new ProfileSearchCriteriaVO(criteria.getSiteID(), criteria.getBuID(),
					criteria.getProfileNum(), criteria.getUserGroupName(), null,
					null, null, false, null, false, 0, "", false);
			for (String[] loginId : component.getOOBSharedID(vo)) {
				oobSharedIdUsers.add(new NameValuePair<>(loginId[0], loginId[1]));
			}
		}
	}

	protected OrderOnBehalfSearchCriteria populateSearchCriteria(OrderOnBehalfSearchRequest request,
			AppSessionBean appSessionBean) {
		OrderOnBehalfSearchCriteria criteria = new OrderOnBehalfSearchCriteria();
		criteria.setUserID(request.getUserID());
		criteria.setProfileID(request.getProfileID());
		criteria.setFirstName(request.getFirstName());
		criteria.setLastName(request.getLastName());
		criteria.setSiteID(appSessionBean.getSiteID());
		criteria.setBuID(appSessionBean.getBuID());
		criteria.setProfileNum(appSessionBean.getOriginatorProfile().getProfileNumber());
		criteria.setUserGroupName(appSessionBean.getOriginatorProfile().getGroupName());
		return criteria;
	}

	protected void validateFieldForMaxChar(AppSessionBean appSessionBean, String fieldValue, String fieldName,
			int maxSize, Map<String, String> fieldErrors) {
		if (!Util.isBlankOrNull(fieldValue) && fieldValue.length() > maxSize) {
			String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
			fieldErrors.put(fieldName, message);
		}
	}

	//CAP-45709
	@Override
	public OOBResponse setOrderForSelfOrOOBMode(SessionContainer sc, OOBRequest request)
			throws AtWinXSException {

		ApplicationSession applicationSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = applicationSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();

		OOBResponse response = new OOBResponse();
		response.setSuccess(true);

		if (hasValidCart(sc)) {
			response.setSuccess(false);
			response.setMessage(
					getTranslation(appSessionBean, SFTranslationTextConstants.OOB_CANNOT_CHANGE_REQUESTOR_MSG,
							SFTranslationTextConstants.OOB_CANNOT_CHANGE_REQUESTOR_DEFAULT_MSG));
			return response;
		}

		boolean isOrderForSelf = request.isOrderForSelf();

		// If we click the Order for Self link, remove OOB requestor values
		if(isOrderForSelf) {
			setOrderOnBehalfSelf(applicationSession, appSessionBean, oeSession, applicationVolatileSession, response);
		} else {
			setOOBMode(applicationSession, oeSession, applicationVolatileSession, request, response);
		}

		return response;
	}	
	//CAP-45709
	//This method is copied from CP OrderOnBehalfSearchController setOrderOnBehalfSelf method.
	/**
	 * Method setOrderOnBehalfSelf. This will set Originator in AppSEssionBean
	 * 
	 * @throws AtWinXSException
	 */
	protected OOBResponse setOrderOnBehalfSelf(ApplicationSession applicationSession,AppSessionBean appSessionBean, OrderEntrySession oeSession, ApplicationVolatileSession applicationVolatileSession, OOBResponse response) throws AtWinXSException
	{

		try {
			VolatileSessionBean volatileSessionBean =  applicationVolatileSession.getVolatileSessionBean();
			appSessionBean.setRequestorProfile(null);
			appSessionBean.setRequestorOrdProp(null);
			appSessionBean.setInRequestorMode(false);

			// Set the Volatile session firstname, last name to show on banner pages
			volatileSessionBean.clearOOBCriteria();

			// Rebuild OE Session for Requestor
			OrderEntrySession selfOESession = new OrderEntrySession();
			selfOESession.init(appSessionBean);

			oeSession.setOESessionBean(selfOESession.getOESessionBean());

			SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);

			//Removed ApplicationSession.buildMenuGroups logic as it is not required in ConnectOne

			// Save AppSession
			SessionHandler.saveSession(applicationSession, appSessionBean.getSessionID(), applicationSession.getServiceID());

			//Added in C1UX - As CP handle this in session.
			SessionHandler.saveSession(applicationVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);

		}catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
			return response;
		}
		response.setSuccess(true);
		return response;
	}
	//CAP-45709
	protected OOBResponse setOOBMode(ApplicationSession applicationSession, OrderEntrySession oeSession, ApplicationVolatileSession applicationVolatileSession, OOBRequest request, OOBResponse response) throws AtWinXSException
	{

			AppSessionBean appSessionBean = applicationSession.getAppSessionBean();
			//CAP-46959
			OEResolvedUserSettingsSessionBean settings = AdminUtil.getUserSettings(new LoginVOKey(appSessionBean.getSiteID(), appSessionBean.getOriginatorProfile().getLoginID()), appSessionBean.getSessionID(), appSessionBean.getCustomToken());

			if (!settings.isAllowOrderOnBehalf()) {
				throw new AccessForbiddenException(this.getClass().getName());
			}
			try {
			response = validate(appSessionBean, request, response);

			if(!response.isSuccess())
				return response;

			String loginId = request.getLoginID();
			int profileNumber = Integer.parseInt(request.getProfileNumber());

			//Reset the criteria
			oeSession.getOESessionBean().getOobSessionBean().setOobSearchCriteria(new OrderOnBehalfSearchFormBean());

			//Removed the method putUserSettingsInRequest(request) as it is not required at this point of time.

			ILoginInterface iLogin = mAdminComponentLocator.locateLoginComponent(appSessionBean.getCustomToken());
			// Get Login details
			LoginVO login = iLogin.getLogin(new LoginVOKey(appSessionBean.getSiteID(), loginId));

			if(login == null || (!login.isSharedID() && profileNumber != login.getProfileNumber())) {
				Message msg = new Message();
				msg.setErrGeneralMsg("Profile or Login is invalid. Please make a selection again.");
				throw new AtWinXSMsgException(msg, this.getClass().getName());

			}

			// Added logic for unattached profile
			if(login.isSharedID())
			{
				// Check if we have more than one Login/Shared ID
				boolean isValidLoginID = validateSharedId(appSessionBean, oeSession, loginId, profileNumber);
				if(!isValidLoginID) {
					response.setSuccess(false);
					response.setMessage("Profile or Login is invalid. Please make a selection again.");
				} else {
					setOOBRequestor(applicationSession, appSessionBean, oeSession, applicationVolatileSession, loginId, profileNumber);
				}
			}

			else
			{
				// Refactored code for reuse
				setOOBRequestor(applicationSession, appSessionBean, oeSession, applicationVolatileSession, loginId, profileNumber);
			}

		}catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		return response;
	}
	//CAP-45709
	protected boolean validateSharedId(AppSessionBean appSessionBean, OrderEntrySession oeSession, String loginId, int profileNumber) throws AtWinXSException {

		// If no login id available, go to the Select a User page
		OrderOnBehalfAssembler assembler = getOOBAssembler(appSessionBean, oeSession.getOESessionBean().getOobSessionBean());
		OrderOnBehalfSearchFormBean bean = new OrderOnBehalfSearchFormBean();
		bean.setProfileNum(appSessionBean.getOriginatorProfile().getProfileNumber());//CAP-46227 change to check the profile to execute always as expected
		ProfileSearchCriteriaVO vo = assembler.createSearchCriteriaVO(bean);
		ArrayList<String[]> loginIDList = assembler.getOOBSharedID(vo);
		oeSession.getOESessionBean().getOobSessionBean().getOobSelectUserCriteria().setLoginIDList(loginIDList);

		oeSession.putParameter("PRFL_NR", profileNumber);
		oeSession.setClearParameters(false);

		boolean isValidLoginID = false;

		if(!loginIDList.isEmpty()) {
			for(String[] tempLoginID : loginIDList) {
				if(tempLoginID[0].contains(loginId)) {
					isValidLoginID = true;
					break;
				}
			}
		}
		return isValidLoginID;
	}
	//CAP-45709
	protected OOBResponse validate(AppSessionBean appSessionBean, OOBRequest request, OOBResponse response) {
		Map<String, String> fieldErrors = new HashMap<>();

		if (Util.isBlankOrNull(request.getLoginID())) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_NOT_BLANK_MSG, ModelConstants.OOB_LOGIN_ID_CRITERIA_FIELD_NAME);
			String message = getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_NOT_BLANK_MSG,
					"LoginID must not be blank", replaceMap);
			fieldErrors.put(ModelConstants.OOB_LOGIN_ID_CRITERIA_FIELD_NAME, message);
		}

		if (Util.isBlankOrNull(request.getProfileNumber())) {
			Map<String, Object> replaceMap = new HashMap<>();
			replaceMap.put(TranslationTextConstants.TRANS_NM_REP_TAG_NOT_BLANK_MSG, ModelConstants.OOB_PROFILE_NUMBER_CRITERIA_FIELD_NAME);
			String message = getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_NOT_BLANK_MSG,
					"Profile Number must not be blank", replaceMap);
			fieldErrors.put(ModelConstants.OOB_PROFILE_NUMBER_CRITERIA_FIELD_NAME, message);
		} else if(!request.getProfileNumber().matches(ModelConstants.NUMBER_REGEX)) {
			fieldErrors.put(ModelConstants.OOB_PROFILE_NUMBER_CRITERIA_FIELD_NAME, getTranslation(appSessionBean, SFTranslationTextConstants.NUMERIC_ERROR,
					SFTranslationTextConstants.NUMERIC_ERROR_DEF));
		}

		// Validate login ID criteria
		validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getLoginID()),
				ModelConstants.OOB_LOGIN_ID_CRITERIA_FIELD_NAME, ModelConstants.OOB_LOGIN_ID_CRITERIA_MAX_LENGTH,
				fieldErrors);

		if(!fieldErrors.isEmpty()) {
			response.setFieldMessages(fieldErrors);
			response.setSuccess(false);
		}
		return response;

	}
	//CAP-45709
	/**
	 * This method will return the OrderOnBehalfAssembler
	 * @return OrderOnBehalfAssembler
	 */
	protected OrderOnBehalfAssembler getOOBAssembler(AppSessionBean appSessionBean, OrderOnBehalfSessionBean ofoSessionBean)
	{
		return new OrderOnBehalfAssembler(appSessionBean, ofoSessionBean, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
	}
	//CAP-45709
	/**
	 * Refactored code to assign OOB requestor
	 * @param request
	 * @param modelMap
	 * @param loginId
	 * @param profileNumber
	 * @return
	 * @throws AtWinXSException
	 */
	private void setOOBRequestor(ApplicationSession applicationSession, AppSessionBean appSessionBean, OrderEntrySession oeSession, ApplicationVolatileSession applicationVolatileSession, String loginId, Integer profileNumber) throws AtWinXSException
	{
		//Load up Profile for Requestor
		setOrderOnBehalfRequestor(applicationSession, appSessionBean, applicationVolatileSession, profileNumber, loginId);

		//Removed ApplicationSession.buildMenuGroups logic as it is not required in ConnectOne

		//Separate out OESession loading so that Auto-login can call only ASB loading
		setRequestorOESession(appSessionBean, oeSession);

	}
	//CAP-45709
	/**
	 * Method setOrderOnBehalfRequestor. This will set selected Requestor in
	 * AppSEssionBean
	 * 
	 * @param profileNum
	 * @param loginID
	 * @throws AtWinXSException
	 */
	protected void setOrderOnBehalfRequestor(ApplicationSession applicationSession, AppSessionBean appSessionBean, ApplicationVolatileSession applicationVolatileSession, int profileNum, String loginID)
			throws AtWinXSException
	{
		VolatileSessionBean volatileSessionBean =  applicationVolatileSession.getVolatileSessionBean();
		OrderOnBehalfUtil oobUtil = new OrderOnBehalfUtil();

		// Call method to load requestor data in ASB
		oobUtil.loadASBForRequestor(appSessionBean, profileNum, loginID);

		// update AppSessionBean to use Requestor mode so that correct Order
		// Entry bean is built.
		appSessionBean.setInRequestorMode(true);

		// Save AppSession
		SessionHandler.saveSession(applicationSession, appSessionBean.getSessionID(), applicationSession.getServiceID());



		// Set the Volatile session firstname, last name to show on banner pages
		volatileSessionBean.getOrderOnBehalf().setInRequestorMode(true); //set to true so the right catalogs will load
		volatileSessionBean.getOrderOnBehalf().setRequestorProfileNumber(profileNum);
		volatileSessionBean.getOrderOnBehalf().setRequestorFirstName(appSessionBean
				.getRequestorProfile().getFirstName());
		volatileSessionBean.getOrderOnBehalf().setRequestorLastName(appSessionBean
				.getRequestorProfile().getLastName());
		volatileSessionBean.getOrderOnBehalf().setRequestorUserID(appSessionBean.getRequestorProfile().getLoginID());
		volatileSessionBean.getOrderOnBehalf().setRequestorProfileID(appSessionBean.getRequestorProfile().getProfileID());
		volatileSessionBean.getOrderOnBehalf().setRequestorEmail(appSessionBean.getRequestorProfile().getEmailAddress());
		volatileSessionBean.getOrderOnBehalf().setRequestorPhone(appSessionBean.getRequestorProfile().getPhoneNumber());
		volatileSessionBean.setIsDirty(true);

		//Added in C1UX - As CP handle this in session.
		SessionHandler.saveSession(applicationVolatileSession, appSessionBean.getSessionID(), AtWinXSConstant.APPVOLATILESESSIONID);

	}
	//CAP-45709
	/**
	 * Method setRequestorOESession. This will set selected Requestor's OE
	 * Session
	 * 
	 * @throws AtWinXSException
	 */
	protected void setRequestorOESession(AppSessionBean appSessionBean, OrderEntrySession oeSession) throws AtWinXSException
	{
		// Rebuild OE Session for Requestor
		OrderEntrySession requestorOESession = new OrderEntrySession();
		requestorOESession.init(appSessionBean);

		oeSession.setOESessionBean(requestorOESession.getOESessionBean());

		//CP-9893 Rebuild Menus using requestor settings
		SessionHandler.saveSession(oeSession, appSessionBean.getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);

		//Removed ApplicationSession.buildMenuGroups logic as it is not required in ConnectOne

	}

}