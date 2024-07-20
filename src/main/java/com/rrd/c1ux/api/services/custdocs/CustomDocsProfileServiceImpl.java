/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         JIRA #            Description
 *	--------    -----------        ----------      -----------------------------------------------------------
 * 	11/08/23	A Boomker			CAP-44486		Initial version
 * 	11/10/23	A Boomker			CAP-44487		Added load user profile
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 *	06/26/24	R Ruth				CAP-46503		Added loadAltProfiles
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXAlternateProfileOptions;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadAltProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocLoadProfileResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSearchResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocProfileSelectionBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUserProfileSearchRequest;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.ProfileOption;
import com.rrd.custompoint.orderentry.customdocs.ProfileSelector;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.admin.locator.ProfileComponentLocator;
import com.wallace.atwinxs.admin.locator.ProfileDefinitionComponentLocator;
import com.wallace.atwinxs.admin.vo.ProfileDefinitionVO;
import com.wallace.atwinxs.admin.vo.ProfileDefinitionVOKey;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.admin.vo.ProfileVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.IProfileDefinition;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;

@Service
public class CustomDocsProfileServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsProfileService {
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsProfileServiceImpl.class);

	public CustomDocsProfileServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
	        SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	public C1UXCustDocProfileSearchResponse searchUserProfiles(SessionContainer sc, C1UXCustDocUserProfileSearchRequest request)
			throws AtWinXSException {
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

		C1UXCustDocProfileSearchResponse response = new C1UXCustDocProfileSearchResponse();
		try {
			if ((ui.getProfileSelector() != null) && (ui.getProfileSelector().isShown()))
			{
				doSearch(ui.getProfileSelector(), request, response);
			}
			else
			{
				setNoResultsResponse(response);
			}
		} catch(Exception e) {
			logger.error("Exception thrown attempting to search user profiles", e);
			setNoResultsResponse(response);
		}
		response.setSuccess(true);

		return response;
	}

	@Override
	public C1UXCustDocLoadProfileResponse loadUserProfile(SessionContainer sc, C1UXCustDocLoadProfileRequest request)
			throws AtWinXSException {
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

		if ((ui.getProfileSelector() == null) || (!ui.getProfileSelector().isShown())) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		C1UXCustDocLoadProfileResponse response = new C1UXCustDocLoadProfileResponse();
		if (validateUserProfileSelection(request.getProfileNumber(), asb, response))
		{
			loadValidUserProfile(oeSession, ui, request.getProfileNumber(), asb, response);
		}

		return response;
	}

	protected void loadValidUserProfile(OrderEntrySession oeSession, UserInterface ui, int profileNumber,
			AppSessionBean asb, C1UXCustDocLoadProfileResponse response) {
		try {
			ui.populateFromProfileSelection(asb, oeSession.getOESessionBean(), profileNumber);
			saveFullOESessionInfo(oeSession, asb.getSessionID());
			response.setSuccess(true);
		} catch(Exception e) {
			logger.error(e.getMessage());
			response.setMessage(getTranslation(asb, TranslationTextConstants.TRANS_NM_UI_NO_PRFL, SFTranslationTextConstants.TRANS_NM_UI_NO_PRFL_FOUND_DEFAULT));
		}
	}

	protected void doSearch(ProfileSelector ps, C1UXCustDocUserProfileSearchRequest request,
			C1UXCustDocProfileSearchResponse response) throws AtWinXSException {
		response.setCurrentProfileNumber(ps.getSelectedProfile());
		request.cleanUpRequest();
		response.setProfileOptions(ps.doSearchC1UX(request.getValue1(), request.getValue2(), request.getValue3(),
				request.getTerm1(), request.getTerm2(), request.getTerm3()));
	}

	protected void setNoResultsResponse(C1UXCustDocProfileSearchResponse response) {
		response.setProfileOptions(new ArrayList<>());
		response.setCurrentProfileNumber(AtWinXSConstant.INVALID_ID);
	}

	protected int getUserProfileDefinitionNumber(AppSessionBean appSessionBean) throws AtWinXSException {
		IProfileDefinition profileDefAdmin = ProfileDefinitionComponentLocator.locate(appSessionBean.getCustomToken());
		ProfileDefinitionVOKey profileDefVOKey = new ProfileDefinitionVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(), AtWinXSConstant.INVALID_ID);
		ProfileDefinitionVO vo = profileDefAdmin.getProfileDefinitionByType(profileDefVOKey, ModelConstants.PROFILE_TYPE_USER);
		return (vo != null) ? vo.getProfileDefinitionID() : ModelConstants.DEFAULT_USER_PROFILE_DEFINITION_NUMBER;
	}

	protected ProfileVO getProfileVO(int profileNumber, AppSessionBean appSessionBean) throws AtWinXSException
	{
		IProfileInterface profileComp = ProfileComponentLocator.locate(appSessionBean.getCustomToken());
		return profileComp.getProfile(new ProfileVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(), profileNumber));
	}

	protected boolean confirmUserTypeProfileNumber(int profileNumber, AppSessionBean appSessionBean) throws AtWinXSException
	{
		ProfileVO profileVO = getProfileVO(profileNumber, appSessionBean);
		int definition = getUserProfileDefinitionNumber(appSessionBean);
		return ((profileVO != null)
			&& ((profileVO.getProfileDefinitionID() == definition)
					|| (profileVO.getProfileDefinitionID() == ModelConstants.DEFAULT_USER_PROFILE_DEFINITION_NUMBER)));
	}

	protected boolean validateUserProfileSelection(int profileNumber, AppSessionBean appSessionBean, C1UXCustDocLoadProfileResponse response)
	{
		boolean success = false;
		if (profileNumber>0)
		{
			try {
				success = confirmUserTypeProfileNumber(profileNumber, appSessionBean);
			} catch (AtWinXSException e) {
				logger.error(e.getMessage());
			}

			if (!success) {
				response.setMessage(getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_UI_NO_PRFL, SFTranslationTextConstants.TRANS_NM_UI_NO_PRFL_FOUND_DEFAULT));
			}
		} else {
			response.setMessage(getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_UI_SEL_PRFL, SFTranslationTextConstants.TRANS_NM_UI_SEL_PRFL_DEFAULT));
		}
		return success;
	}

	protected List<C1UXAlternateProfileOptions> getAltProfileOptions(UserInterface ui, AppSessionBean asb, VolatileSessionBean vsb) {
		C1UXCustDocPageBean pb = new C1UXCustDocPageBean();
		int pageNumber = getCurrentPageOutsideSubmit(ui);
		this.loadAlternateProfiles(ui, pageNumber, pb, asb, vsb);
		return pb.getAlternateProfiles();
	}

	public C1UXCustDocLoadAltProfileResponse loadAltProfile(SessionContainer sc, C1UXCustDocLoadAltProfileRequest request) throws AtWinXSException {
		C1UXCustDocLoadAltProfileResponse response=new C1UXCustDocLoadAltProfileResponse();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();
		List<C1UXAlternateProfileOptions> profileOptions = getAltProfileOptions(ui, asb, volatileSessionBean);

		validateLoadAlternateProfileRequest(profileOptions, request);

		try {
			boolean profilesSelected = false;
			boolean profileFailed = false;
			for(C1UXCustDocProfileSelectionBean bean : request.getAltProfileSelections()) {
				// If there is no profile number passed that isn’t > 0, then TranslationTextConstants.TRANS_NM_UI_SEL_PRFL should be used for the translation key for the error.
				if (bean.getProfileNum() > 0) {
					profilesSelected = true;
					if (findAltProfileNumberInList(bean.getDefinitionId(), bean.getProfileNum(), profileOptions)) {
						ui.getUiAltProfileSelections().put(getAltProfileDefinitionType(bean.getDefinitionId(), profileOptions), bean.getProfileNum());
					} else { // this should never be hit except in swagger or hackers because users cannot pass profiles that are not in their list
						generateSpecificValidationError(bean.getDefinitionId(), response, profileOptions, asb);
						profileFailed = true;
					}
				}
			}

			if (!profilesSelected) {
				response.setMessage(getTranslation(asb, TranslationTextConstants.TRANS_NM_UI_SEL_PRFL, "Please select a profile."));
			} else if (!profileFailed) {
				ui.populateFromAlternateProfileSelection(asb);
				saveFullOESessionInfo(oeSession, asb.getSessionID());
				response.setSuccess(true);
			}
		}
		catch (AtWinXSMsgException me) {
			logger.error(me.getMessage(), me);
			response.setMessage(me.getMessage());
		}
		catch (Exception e) {
			logger.error(e.toString());
			setGenericHeaderError(response, asb);
		}

		return response;
	}

	protected void generateSpecificValidationError(int defID, C1UXCustDocLoadAltProfileResponse response, List<C1UXAlternateProfileOptions> profileOptions,
			AppSessionBean asb) {
		String error = getAltProfileDefinitionType(defID, profileOptions) + AtWinXSConstant.BLANK_SPACE
			+ getTranslation(asb, TranslationTextConstants.TRANS_NM_UI_NO_PRFL, "Profile does not exist.");

		if (!Util.isBlankOrNull(response.getMessage())) {
			response.setMessage(response.getMessage() + error);
		} else {
			response.setMessage(error);
		}
	}

	protected boolean findAltProfileDefinitionInList(int defID, List<C1UXAlternateProfileOptions> profileOptions) {
		for (C1UXAlternateProfileOptions opts: profileOptions) {
			if (opts.getDefinitionID() == defID) {
				return true;
			}
		}
		return false;
	}

	protected String getAltProfileDefinitionType(int defID, List<C1UXAlternateProfileOptions> profileOptions) {
		for (C1UXAlternateProfileOptions opts: profileOptions) {
			if (opts.getDefinitionID() == defID) {
				return opts.getLabel();
			}
		}
		return String.valueOf(defID);
	}

	protected boolean findAltProfileNumberInList(int defID, int number, List<C1UXAlternateProfileOptions> profileOptions) {
		for (C1UXAlternateProfileOptions opts: profileOptions) {
			if (opts.getDefinitionID() == defID) {
				for (ProfileOption optNumber: opts.getProfileOptions()) {
					if (optNumber.getProfileNumber() == number) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void validateLoadAlternateProfileRequest(List<C1UXAlternateProfileOptions> profileOptions,
			C1UXCustDocLoadAltProfileRequest request) throws AccessForbiddenException {
		boolean fails = false;
		if ((profileOptions == null) || (profileOptions.isEmpty()) || (request.getAltProfileSelections() == null)
				|| (request.getAltProfileSelections().isEmpty())) { // none passed in or none on the current page
			fails = true;
		} else {
			for (C1UXCustDocProfileSelectionBean selection : request.getAltProfileSelections()) {
				if ((selection.getDefinitionId() <= 0) // not a valid profile definition
						|| (!findAltProfileDefinitionInList(selection.getDefinitionId(), profileOptions))) { // not a definition shown on the page
					fails = true;
				}
			}
		}

		if (fails) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
	}

	protected void validationError(AppSessionBean asb) throws AtWinXSException {
		Message msg = new Message();
		msg.setErrGeneralMsg(getTranslation(asb, TranslationTextConstants.TRANS_NM_UI_SEL_PRFL, TranslationTextConstants.TRANS_NM_UI_SEL_PRFL));
		throw new AtWinXSMsgException(msg, this.getClass().getName());
	}

}
