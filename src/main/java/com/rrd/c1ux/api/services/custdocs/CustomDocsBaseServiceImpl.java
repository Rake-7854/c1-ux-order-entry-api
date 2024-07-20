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
 * 	07/25/23	A Boomker			CAP-92223		Initial version
 * 	07/26/23	A Boomker			CAP-92225		Added redirect routing to be used when the result of this call should indicate routing
 *	08/22/23	A Boomker			CAP-43223		Moving things around for junits
 *	11/07/23	A Boomker			CAP-44427 		Refactored some things for working proofs
 *	11/13/23	A Boomker			CAP-44426		Added handling for update working proof
 *	12/04/23	A Boomker			CAP-45654		Added method to set dirty flag
 *	02/14/24	A Boomker			CAP-46309		Moved some methods here
 *	02/27/24	A Boomker			CAP-47446		Moved checkIfBUAllowsEPS() here
 *	03/12/24	A Boomker			CAP-46490		Refactoring to allow for bundles
 * 	03/29/24	A Boomker			CAP-46493/CAP-46494	fixes for navigation
 *  04/03/24	A Boomker			CAP-46494					Proofing overrides for bundle
 *  04/12/24	Krishna Natarajan	CAP-48606		Added a parameter referring the ShoppingCartServiceImpl constructor
 * 	04/25/24	A Boomker			CAP-46498		Handling for lists in getUIPage API
 *  06/28/24	A Boomker			CAP-46503		Refactored alt profile code to base for reuse
 * 	07/01/24	A Boomker			CAP-46488		Added handling for kits
 * 	07/02/24	A Boomker			CAP-46489, CAP-49556	Added redirect to kit template editor
 *	07/09/24	A Boomker			CAP-46538		Refactored some handling to base for imprint history
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXAlternateProfileOptions;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataPage;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocWorkingProofResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXImprintHistoryOptions;
import com.rrd.c1ux.api.models.custdocs.C1uxCustDocListDetails;
import com.rrd.c1ux.api.models.shoppingcart.ContinueShoppingResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorService;
import com.rrd.c1ux.api.services.items.locator.ManageItemsInterfaceLocatorServiceImpl;
import com.rrd.c1ux.api.services.locators.ListsAdminLocatorService;
import com.rrd.c1ux.api.services.locators.ListsAdminLocatorServiceImpl;
import com.rrd.c1ux.api.services.orderentry.locator.OEShoppingCartComponentLocatorServiceImpl;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.shoppingcart.ShoppingCartServiceImpl;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.bundle.BundleUserInterface;
import com.rrd.custompoint.orderentry.customdocs.ImprintHistorySelector;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.ProfileOption;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.EntryPoint;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.MergeOption;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.Variable;
import com.rrd.custompoint.orderentry.customdocs.Variable.PageflexType;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.MergeListUploadFile;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.ui.upload.UploadFile;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileHostedResource;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileImage;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileInsert;
import com.rrd.custompoint.orderentry.ui.upload.UploadFileUpload;
import com.wallace.atwinxs.admin.locator.ProfileComponentLocator;
import com.wallace.atwinxs.admin.locator.ProfileDefinitionComponentLocator;
import com.wallace.atwinxs.admin.vo.ProfileDefinitionVO;
import com.wallace.atwinxs.admin.vo.ProfileDefinitionVOKey;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.IManageItemsInterface;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.IManageListAdmin;
import com.wallace.atwinxs.interfaces.IOEListComponent;
import com.wallace.atwinxs.interfaces.IProfileDefinition;
import com.wallace.atwinxs.interfaces.IProfileInterface;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.lists.locator.ManageListsLocator;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVO;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVOKey;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.lists.vo.OrderListVO;
import com.wallace.atwinxs.orderentry.lists.vo.OrderListVOKey;
import com.wallace.atwinxs.orderentry.locator.OEListLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public abstract class CustomDocsBaseServiceImpl extends BaseOEService implements CustomDocsBaseService {
    private static final Logger baseLogger = LoggerFactory.getLogger(CustomDocsBaseServiceImpl.class);
    private final SessionHandlerService sessionHandlerService;
	public static final String EVENT_ACTION_PARAM = "eventAction";
	public static final String TRUE_VAL = "true";
	public static final String FATAL_ERROR_PREFIX_PROOFING = "FATAL";

	public static final String ROUTING_WAIT = "wait";

	protected CustomizationToken tokenForSession = AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN;

	public void setCustomizationTokenForSession(CustomizationToken token) {
		this.tokenForSession = token;
	}

	public TranslationService getTranslationService() {
		return this.translationService;
	}

	public ObjectMapFactoryService getObjectMapFactoryService() {
		return this.objectMapFactoryService;
	}

	public SessionHandlerService getSessionHandlerService() {
		return this.sessionHandlerService;
	}

	public ManageItemsInterfaceLocatorService getManageItemsService() {
		return this.manageItemsInterfaceLocatorService;
	}

	// CAP-46490 - this should only be required for initialize UI
	private ManageItemsInterfaceLocatorService manageItemsInterfaceLocatorService;

	public String getItemClassification(AppSessionBean asb, Map<String, String> initializeParams) throws AtWinXSException {
		IManageItemsInterface itemInterface = getItemInterface(asb.getCustomToken());
		return itemInterface.checkItemClassification(asb.getSiteID(), initializeParams.get(OrderAdminConstants.CUST_ITEM_NR),
				initializeParams.get(OrderAdminConstants.ITEM_NUMBER));
	}

	private IManageItemsInterface getItemInterface(CustomizationToken token) throws AtWinXSException {
		return manageItemsInterfaceLocatorService.locate(token);
	}

	public String getItemClassFromSession(SessionContainer sc) {
		this.tokenForSession = sc.getApplicationSession().getAppSessionBean().getCustomToken();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeOrderSessionBean.getCurrentCustomDocumentItem();
		if (item != null) {
			if (item.getEntryPoint() == EntryPoint.NEW_REQUEST) {
				return ENTRY_POINT_NEW_REQUEST_STRING;
			}
			else if (item.getEntryPoint() == EntryPoint.ORDER_QUESTIONS) {
				return ENTRY_POINT_ORDER_QUESTIONS_STRING;
			}
			return item.getItemClassification();
		}
		// if it is none of those, then it shouldn't be in custom document flow!
		return null;
	}

	public String getItemClassFromRequest(SessionContainer sc, Map<String, String> requestParams) {
		this.tokenForSession = sc.getApplicationSession().getAppSessionBean().getCustomToken();
		try {
			if (requestParams.containsKey(OrderEntryConstants.CUST_DOC_ENTRY_POINT)) {
				if (ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST.equals(requestParams.get(OrderEntryConstants.CUST_DOC_ENTRY_POINT))) {
					return ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST;
				} else if (ICustomDocsAdminConstants.ENTRY_POINT_ORDER_QUESTIONS.equals(requestParams.get(OrderEntryConstants.CUST_DOC_ENTRY_POINT))) {
					return ICustomDocsAdminConstants.ENTRY_POINT_ORDER_QUESTIONS;
				}
			}
			String classification = getItemClassification(sc.getApplicationSession().getAppSessionBean(), requestParams);
			if ((!Util.isBlankOrNull(classification)) && ((ItemConstants.ITEM_CLASS_BUNDLE.equals(classification))
					|| (ItemConstants.ITEM_CLASS_CAMPAIGN.equals(classification))
					|| (ItemConstants.ITEM_CLASS_PROMOTIONAL.equals(classification))
					|| (ItemConstants.ITEM_CLASS_SPECIAL_ITEM.equals(classification))
					|| (ItemConstants.ITEM_CLASS_CUSTOM_DOC.equals(classification)))) {
				return classification;
			}
		} catch(Exception e) {
			// if anything goes wrong with this check, do not initialize
			baseLogger.error(e.toString());
		}

		// if it is none of those, then it shouldn't be in custom document flow!
		return null;
	}

	protected CustomDocsBaseServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
	        SessionHandlerService sessionService) {
		super(translationService, objectMapFacService);
		this.sessionHandlerService = sessionService;
		this.manageItemsInterfaceLocatorService = new ManageItemsInterfaceLocatorServiceImpl();
	}

	protected CustomDocsBaseServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService,
	        SessionHandlerService sessionService,
	        ManageItemsInterfaceLocatorService manageItemsInterfaceLocatorService) {
		super(translationService, objectMapFacService);
		this.sessionHandlerService = sessionService;
		this.manageItemsInterfaceLocatorService = manageItemsInterfaceLocatorService;
	}

	public void setItemLocator(ManageItemsInterfaceLocatorService locator) {
		this.manageItemsInterfaceLocatorService = locator;
	}

	public void setDirtyFlag(Map<String, String> uiRequest) {
		uiRequest.put(OrderEntryConstants.DATA_IS_DIRTY, TRUE_VAL);
		uiRequest.put(OrderEntryConstants.DATA_HAS_CHANGED_FROM_ORIG, TRUE_VAL);
	}

	public UserInterface.UIEvent lookupEvent(String code)
	{
		for(UserInterface.UIEvent option : UserInterface.UIEvent.values())
		{
			if(option.toString().equalsIgnoreCase(code))
			{
				return option;
			}
		}

		return UserInterface.UIEvent.STAY; // default to reset call for the page - no save should be done
	}

	public UserInterface.ProofType lookupProofType(String code)
	{
		for(UserInterface.ProofType option : UserInterface.ProofType.values())
		{
			if(option.toString().equalsIgnoreCase(code))
			{
				return option;
			}
		}

		return UserInterface.ProofType.IMAGE; // default to image proof call for the UI
	}

	// converted from UserInterfaceImpl method
	public String getCancelURL(CustomDocumentItem item, // String previousSearchResultsPage,
			UserInterface ui, SessionContainer sc)
	{
		if ((item == null) || (ui == null) || (sc == null))
		{
			return null;
		}
		// depending on entry point, determine the return/cancel destination
		switch(ui.getEntryPoint())
		{
			case CATALOG_NORMAL: // returns to session location
			case CATALOG_EXPRESS:
				return getReturnDestinationFromSession(sc);

			case CART_EDIT:
			case CONTINUE_VERSION:
			case CART_ADD_FROM_STUB: // this would be a stub order line in the cart that does not have a complete specification on it (may be the result of a save and exit)
			case UNABLE_TO_DETERMINE: // this will mean an error is thrown if the request does not contain enough information to know where to go back to
			case ORDER_QUESTIONS:
			case ROUTING_AND_APPROVAL:
				return RouteConstants.CART_ENTRY_ROUTING_URL;

				// CAP-49556
			case KIT_TEMP: // returns to kit template OE for usability
				return RouteConstants.KIT_EDITOR_ROUTING;

			case TRY_UI: // at this point, C1UX would have to cause an autologin to CP to even get back here, but this would be the URL
				// to enter the UI once it's logged in

				// the cases below are not supported yet or destination is unknown at this time
			case NEW_REQUEST:
			case DMIS_ADD: // returns to punchout DMIS system with a new spec
			case DMIS_EDIT: // returns to punchout DMIS system with same spec
			case DMIS_INSPECT: // does not return anywhere
			case KIT: // returns to manage kits
			case ROUTING_AND_APPROVAL_BATCH:
			case SUBSCRIPTION_MOD: //CAP-349 EIQ return to new Action Subscription Order Details Page
			case ORDER_FROM_FILE_ADD: // returns to order from file
			case ORDER_FROM_FILE_EDIT: // returns to order from file

			default:
				return "home";
		}
	}

	// converted from UserInterfaceImpl method
	public String getForwardToURL(CustomDocumentItem item, UserInterface ui, SessionContainer sc)
	{
		if ((item == null) || (ui == null) || (sc == null))
		{
			return null;
		}
		// depending on entry point, determine the return/cancel destination
		switch(ui.getEntryPoint())
		{
			case NEW_REQUEST:
				return RouteConstants.HOME;

			case CATALOG_NORMAL: // returns to session location?
			case CATALOG_EXPRESS:
				return RouteConstants.CART_ENTRY_ROUTING_URL; // maybe switch to getReturnDestinationFromSession

			case CART_EDIT:
			case CONTINUE_VERSION:
			case CART_ADD_FROM_STUB: // this would be a stub order line in the cart that does not have a complete specification on it (may be the result of a save and exit)
			case UNABLE_TO_DETERMINE: // this will mean an error is thrown if the request does not contain enough information to know where to go back to
			case ORDER_QUESTIONS:
			case ROUTING_AND_APPROVAL:
				return RouteConstants.CART_ENTRY_ROUTING_URL;

				// CAP-46489
			case KIT_TEMP: // returns to kit template OE for usability
				return RouteConstants.KIT_EDITOR_ROUTING;

			case TRY_UI: // at this point, C1UX would have to cause an autologin to CP to even get back here, but this would be the URL
				// to enter the UI once it's logged in

				// the cases below are not supported yet or destination is unknown at this time
			case DMIS_ADD: // returns to punchout DMIS system with a new spec
			case DMIS_EDIT: // returns to punchout DMIS system with same spec
			case DMIS_INSPECT: // does not return anywhere
			case KIT: // returns to manage kits
			case ROUTING_AND_APPROVAL_BATCH:
			case SUBSCRIPTION_MOD: //CAP-349 EIQ return to new Action Subscription Order Details Page
			case ORDER_FROM_FILE_ADD: // returns to order from file
			case ORDER_FROM_FILE_EDIT: // returns to order from file

			default:
				return RouteConstants.CART_ENTRY_ROUTING_URL;

		}
	}

	protected String getReturnDestinationFromSession(SessionContainer sc)
	{
		ShoppingCartServiceImpl service = new ShoppingCartServiceImpl(translationService, new OEShoppingCartComponentLocatorServiceImpl(),
				objectMapFactoryService, oeAssemblerFactoryService, new ListsAdminLocatorServiceImpl());//CAP-48606

		try {
			ContinueShoppingResponse cdResponse = service.determineContinueDestination(sc);
			if (!Util.isBlankOrNull(cdResponse.getReturnLinkURL()))
			{
				return cdResponse.getReturnLinkURL();
			}
		}
		catch(Exception e)
		{
			baseLogger.error(e.toString());
		}
		return RouteConstants.HOME;
	}

	protected ProofType getProofType(UserInterface ui)
	{
		String imageProofJobNm = Util.nullToEmpty(ui.getImageProofJobName());
		if(ui.isExternalESP())
		{
			return ProofType.XERT;
		}
		else if(ui.isPrecisionDialogue() && imageProofJobNm.equalsIgnoreCase(ICustomDocsAdminConstants.JOB_TYPE_PRCSN_DLG))
		{
			return ProofType.PRECISIONDIALOGUE;
		}

		return ProofType.IMAGE;
	}

	public void saveFullOESessionInfo(OrderEntrySession sess, int sessionID) throws AtWinXSException {
		saveFullSessionInfo(sess, sessionID, AtWinXSConstant.ORDERS_SERVICE_ID);
	}

	public void saveFullSessionInfo(BaseSession sess, int sessionID, int serviceID) throws AtWinXSException {
		sessionHandlerService.saveFullSessionInfo(sess, sessionID, serviceID);
	}

	protected String getLocaleDateFormat(AppSessionBean appSessionBean)
	{
		return Util.getDateFormatForLocale(appSessionBean.getDefaultLocale());
	}

	protected boolean onEditableFirstPageOE(UserInterface ui, int pageNum)
	{
		return (!ui.isTryUI() && (pageNum == ui.getPages().get(0).getPageNumber()) && (ui.isFieldsEditable())
				&& (!ui.isNewRequestFlow()));
	}

	protected boolean onEditableFirstPageOE(UserInterface ui)
	{
		return (!ui.isTryUI() && (ui.getNextPageNumber() == ui.getPages().get(0).getPageNumber()) && (ui.isFieldsEditable())
				&& (!ui.isNewRequestFlow()));
	}

	protected Page getCurrentUIPage(UserInterface ui) {
		return ui.getPage(ui.getNextPageNumber());
	}

	protected boolean loadWorkingProof(C1UXCustDocBaseResponse sfBean, UserInterface ui, Page thisPage, AppSessionBean asb) {
		if (ui.isWorkingProof() && (thisPage.getPageNumber() < UserInterface.NEXT_PAGE_NUMBER_LIST)) {
			Collection<String> urls = ui.getUIImageWorkingProofC1UX(asb, thisPage);
			if (!urls.isEmpty()) {
				setSuccessfulWorkingProofValues(sfBean, asb, thisPage, ui, urls);
			}
			else {
				defaultFailedWorkingProofValues(sfBean, asb);
			}
			return true;
		}
		return false;
	}

	protected Collection<String> getDefaultProofFailedURLs() {
		Collection<String> failURLs = new ArrayList<>();
		failURLs.add(ModelConstants.CUST_DOC_PROOF_FILE_NOT_FOUND_PATH);
		return failURLs;
	}

	protected Collection<String> getDefaultProofFailedLabels(AppSessionBean asb) {
		Collection<String> failLabels = new ArrayList<>();
		failLabels.add(getTranslation(asb, SFTranslationTextConstants.CUST_DOC_PROOF_FAILED, "Proof Failed"));
		return failLabels;
	}

	protected void defaultFailedWorkingProofValues(C1UXCustDocBaseResponse sfBean, AppSessionBean asb) {
		if (sfBean instanceof C1UXCustDocPageBean) {
			((C1UXCustDocPageBean) sfBean).setWorkingProofURLs(getDefaultProofFailedURLs());
			((C1UXCustDocPageBean) sfBean).setWorkingProofLabels(getDefaultProofFailedLabels(asb));
		}
		else if (sfBean instanceof C1UXCustDocWorkingProofResponse) {
			((C1UXCustDocWorkingProofResponse) sfBean).setWorkingProofURLs(getDefaultProofFailedURLs());
			((C1UXCustDocWorkingProofResponse) sfBean).setWorkingProofLabels(getDefaultProofFailedLabels(asb));
		}
	}

	protected void setSuccessfulWorkingProofValues(C1UXCustDocBaseResponse sfBean, AppSessionBean asb, Page thisPage, UserInterface ui,
			Collection<String> proofURLs) {
		if (sfBean instanceof C1UXCustDocPageBean) {
			((C1UXCustDocPageBean) sfBean).setWorkingProofURLs(proofURLs);
			((C1UXCustDocPageBean) sfBean).setWorkingProofLabels(getSuccessfulWorkingProofLabels(ui, asb, thisPage));
		}
		else if (sfBean instanceof C1UXCustDocWorkingProofResponse) {
			((C1UXCustDocWorkingProofResponse) sfBean).setWorkingProofURLs(proofURLs);
			((C1UXCustDocWorkingProofResponse) sfBean).setWorkingProofLabels(getSuccessfulWorkingProofLabels(ui, asb, thisPage));
		}

	}

	// CAP-44427 - this will assume you have a valid transaction ID. It should NEVER be called unless that is the case
	public Collection<String> getSuccessfulWorkingProofLabels(UserInterface ui, AppSessionBean asb, Page currentPage)
	{
		Collection<String> labels = new ArrayList<>();
		String prefix = getTranslation(asb, SFTranslationTextConstants.CUST_DOC_PROOF_PAGE_PREFIX, "Proof Page") + AtWinXSConstant.BLANK_SPACE;
		if (!currentPage.isWorkingProofAllPages())
		{
			labels.add(prefix + currentPage.getWorkingProofPageNumber());
		}
		else if ((ui.getPagesToProof() != null) && (!ui.getPagesToProof().isEmpty()))
		{
			HashSet<Integer> setNums = ui.getPagesToProof();
			Integer[] proofPageNums = setNums.toArray(new Integer[setNums.size()]);
			Arrays.sort(proofPageNums);
			for (Integer proofPage : proofPageNums)
			{
				labels.add(prefix + proofPage);
			}
		}
		else
		{
			for (int i = 1; i <= ui.getTemplatePageCount(); i++)
			{
				labels.add(prefix + i);
			}
		}
		return labels;
	}

	// CAP-44839 - added method for when giving real error details would be too useful to hackers, so make it generic
	public void setGenericHeaderError(C1UXCustDocBaseResponse sfBean, AppSessionBean asb) {
		sfBean.setMessage(getTranslation(asb, SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_KEY, SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_DEFAULT));
	}

	// CAP-44839 - added method useful for file streaming, validation, and more on UI
	protected boolean isVarValueUploadOrSearch(String varValue) {
		return (Util.nullToEmpty(varValue).contains("^"))
				&& (((varValue.startsWith(OrderEntryConstants.IDENTIFIER_UPLOAD_SHORT)) && (varValue.contains(OrderEntryConstants.IDENTIFIER_UPLOAD)))
						|| ((varValue.startsWith(OrderEntryConstants.IDENTIFIER_SEARCH_SHORT)) && (varValue.contains(OrderEntryConstants.IDENTIFIER_SEARCH))));
	}

	// CAP-46309 - needed for variable handling too
	protected UploadFile getUploadFileObject(PageflexType varType, Variable cdVar, AppSessionBean appSessionBean) {
		switch(varType) {
		case IMAGE :
			UploadFileImage image = objectMapFactoryService.getEntityObjectMap().getEntity(UploadFileImage.class, appSessionBean.getCustomToken());
			updateEPSExtensions(cdVar, image, appSessionBean);
			return image;
		case INSERTION_GROUP :
			return objectMapFactoryService.getEntityObjectMap().getEntity(UploadFileInsert.class, appSessionBean.getCustomToken());

		case FILE_UPLOAD :
			return objectMapFactoryService.getEntityObjectMap().getEntity(UploadFileUpload.class, appSessionBean.getCustomToken());

		case HOSTED_RESOURCE :
			return objectMapFactoryService.getEntityObjectMap().getEntity(UploadFileHostedResource.class, appSessionBean.getCustomToken());

		default :
			return null;
		}
	}

	protected void updateEPSExtensions(Variable cdVar, UploadFileImage uploadFile, AppSessionBean asb)
	{
		Map<String, String> validExtensions = null;

		if (!cdVar.isAllowSTDImg() && (cdVar.isAllowEPSImg() || checkIfBUAllowsEPS(asb)))
		{
			validExtensions = new HashMap<>();
		}
		else if (cdVar.isAllowSTDImg() && (cdVar.isAllowEPSImg() || checkIfBUAllowsEPS(asb)))
		{
			validExtensions = uploadFile.getValidExtensions();
		}

		if(validExtensions != null)
		{
			validExtensions.put(UploadFile.FILE_EXT_EPS, UploadFile.FILE_EXT_EPS);
			uploadFile.setValidExtensions(validExtensions);
		}
	}

	protected String getWebserviceProperty(AppSessionBean asb, String key) {
		try {
			XSProperties cdProps = PropertyUtil.getProperties(ICustomDocsAdminConstants.WEBSERVICES_PROPERTIES,
				asb.getCurrentEnvCd());
			return Util.nullToEmpty(cdProps.getProperty(key));
		} catch(Exception e) {
			return AtWinXSConstant.EMPTY_STRING;
		}
	}

	protected boolean checkIfBUAllowsEPS(AppSessionBean asb) {
		String busAllowed = getWebserviceProperty(asb, UploadFile.BUS_UNITS_ALLOWED_EPS_UPLOAD).trim();
		boolean allowed = false;
		if (!"".equals(busAllowed)) {
			String thisBU = String.valueOf(asb.getBuID());
			String[] allowedIDs = Util.buildStringArray(busAllowed, ",");
			for (String bu : allowedIDs)
			{
				if (bu.equals(thisBU))
				{
					allowed = true;
					break;
				}
			}
		}
		return allowed;
	}

	protected void stripFatalFlag(C1UXCustDocBaseResponse formBean, Exception e) {
		String msg = (e instanceof AtWinXSMsgException) ? ((AtWinXSMsgException)e).getMsg().getErrGeneralMsg() : e.getMessage();
		if ((msg != null) && (msg.startsWith(FATAL_ERROR_PREFIX_PROOFING)))
		{
			msg = msg.substring(5);
		}
		formBean.setMessage(msg);
	}

	protected String getCurrentBundleComponentProofID(BundleUserInterface ui) {
		ProofType pt = getProofType(ui);
		return getCurrentBundleComponentProofID(ui, pt);
	}

	protected String getCurrentBundleComponentProofID(BundleUserInterface ui, ProofType pt) {
		return ui.getComponentProofID(pt, ui.getCurrentBundleComponent());
	}


	protected CustomDocumentItem getBundleComponentFromMap(Map<Integer, CustomDocumentItem> cdItems, int num) {
		if(cdItems != null)
		{
			for(Map.Entry<Integer, CustomDocumentItem> cdItem : cdItems.entrySet())
			{
				if(cdItem.getKey() == num)
				{
					return cdItem.getValue();
				}
			}
		}
		return null;
	}

	protected String generateNewBundleComponentProofID(BundleUserInterface ui, SessionContainer sc, Map<Integer, CustomDocumentItem> cdItems, ProofType pt)
			throws AtWinXSException {
		// CAP-10074 TH - Added return type for this call to get the proof type of the component
		 // CAP-19773 - need to catch ECE error in case we need to go to the wait page
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession vs = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = vs.getVolatileSessionBean();
		Collection<String> errors = new ArrayList<>();
		// This will be the flag to determine what is the event to be loaded
		// We are saving the proofSeqNumber in the UI, use it as the one coming from the request is always zero (0).
		String bundleComponentSeqNum = String.valueOf(ui.getProofSeqFromRequest());
		pt = ui.generateNewProof(pt, appSessionBean, volatileSessionBean, errors, cdItems, oeSessionBean, bundleComponentSeqNum);
		return ui.getComponentProofID(pt, ui.getCurrentBundleComponent());
	}

	// CAP-46498
	protected ManageListsBusinessUnitPropsVO getBUListAdminSettings(AppSessionBean appSessionBean) throws AtWinXSException {
		ListsAdminLocatorService listsAdminLocatorService = new ListsAdminLocatorServiceImpl();
		IManageListAdmin listAdmin = listsAdminLocatorService.locate(appSessionBean.getCustomToken());
		return listAdmin.getBuListDetails(new ManageListsBusinessUnitPropsVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID()));
	}

	 protected boolean getListPrivacySettings(AppSessionBean appSessionBean)
	 {
		 boolean privateListsOnly = false;
		 try
		 {
			 ManageListsBusinessUnitPropsVO businessUnitPropsVO = getBUListAdminSettings(appSessionBean);
			 if (businessUnitPropsVO != null)
			 {
				 privateListsOnly = businessUnitPropsVO.isDoNotShareListsInd();
			 }
		 }
		 catch(AtWinXSException e)
		 {
			 baseLogger.error("Error trying to get User's list privacy settings in getListPrivacySettings", e);
		 }
		 return privateListsOnly;
	}

	// CAP-46498
	protected IOEListComponent getOEListComponent(AppSessionBean appSessionBean) throws AtWinXSException {
		return OEListLocator.locate((appSessionBean != null) ? appSessionBean.getCustomToken() : AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
	}

	protected IManageList getManageListComponent(AppSessionBean appSessionBean) throws AtWinXSException {
		return ManageListsLocator.locate(appSessionBean.getCustomToken());
	}

	protected C1uxCustDocListDetails makeListDetailObjectFromVO(ListVO listVO, UserInterface ui, AppSessionBean asb) {
		C1uxCustDocListDetails list = new C1uxCustDocListDetails();
		list.setListId(listVO.getListID());
		list.setListName(listVO.getListName());
		list.setNumRecords(listVO.getRecordCount());
		list.setSelected(isListSelected(ui, listVO.getListID()));
		list.setUpload(true);
		list.setMapped(isListMapped(ui, listVO.getListID()));
		list.setListDescription(listVO.getListDescription());
		list.setLastUsedDate(getFormattedDate(listVO.getLastUsedDate(), asb));
		list.setUploadDate(getFormattedDate(listVO.getUploadedDate(), asb));
		return list;
	}

	protected String getFormattedDate(java.util.Date day, AppSessionBean asb) {
		return Util.getDateStringFromTimestamp(day, asb.getDefaultLocale());
	}

	protected OrderListVO getOrderListVO(UserInterface ui, AppSessionBean asb) {
		try {
			if (ui.getOrderListID() > 0) {
				return getOEListComponent(asb).getOrderListVO(new OrderListVOKey(ui.getOrderListID()));
			}
		} catch (Exception e) {
			baseLogger.error(e.toString());
		}
		return null;
	}

	protected C1UXCustDocMappedDataPage loadListDataPage(int pageNum, boolean invalid, OrderListVO orderListVO, UserInterface ui) throws AtWinXSException {
		C1UXCustDocMappedDataPage data = new C1UXCustDocMappedDataPage();
		MergeListUploadFile mergeList = objectMapFactoryService.getEntityObjectMap().getEntity(MergeListUploadFile.class, AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
		data.setMergeVarDisplayLabels(mergeList.getColumnHeadersC1UX(ui));
		data.setMergeData(mergeList.getSavedDataC1UX(new OrderListVOKey(orderListVO.getOrderListID()), invalid, pageNum, ModelConstants.NUM_MAPPED_DATA_RECORDS_PER_PAGE_DISPLAYED));
		return data;
	}

	protected boolean isListMapped(UserInterface ui, int listID) {
		return ((ui.getListMappings() != null) && ui.getListMappings().containsKey(String.valueOf(listID)));
	}

	protected boolean isListSelected(UserInterface ui, int listID) {
		return ((ui.getSelectedListIDs() != null) && ui.getSelectedListIDs().contains(String.valueOf(listID)));
	}

	protected int getSingleSelectedListID(UserInterface ui) {
		int id = AtWinXSConstant.INVALID_ID;
		if ((ui.getSelectedListIDs() != null) && (!ui.getSelectedListIDs().isEmpty())) {
			String idString = ui.getSelectedListIDs().toArray(new String[1])[0];
			id = Util.safeStringToDefaultInt(idString, AtWinXSConstant.INVALID_ID);
		}
		return id;
	}

	protected String getOrderListSourceType(UserInterface ui) {
		if (ui.getOrderListSource() != null) {
			return ui.getOrderListSource().toString();
		}
		return AtWinXSConstant.EMPTY_STRING;
	}

	protected boolean isReviewOnlyApproval(UserInterface ui, OEOrderSessionBean sessionBean) {
		return ((ui.getEntryPoint() == EntryPoint.ROUTING_AND_APPROVAL)
				&& (Util.nullToEmpty(sessionBean.getUserSettings().getApproverReviewLevel()).equalsIgnoreCase(OrderEntryConstants.APPROVE_ONLY_USER)));

	}

	protected boolean validateCustDocMergeUI(UserInterface ui) {
		return (ui != null) && ((MergeOption.MAIL_MERGE == ui.getMergeOption()) || (MergeOption.MERGE == ui.getMergeOption()));
	}

	protected StringBuilder processErrorStrings(Collection<String> errorStrings) {
		StringBuilder errorsString = new StringBuilder();
		int count = 0;
		for (String singleError : errorStrings) {
			if (count > 0) {
				errorsString.append("<br />");
			}
			// CAP-42817 - getting rid of the prefix caret for C1UX
			if (singleError.startsWith(FATAL_ERROR_PREFIX_PROOFING)) {
				singleError = singleError.substring(5);
			}
			errorsString.append(singleError).append("\n");
			count++;
		}
		return errorsString;
	}

	public void combineErrors(Collection<String> errorStrings, C1UXCustDocBaseResponse formBean) {
		if (!errorStrings.isEmpty()) {
			StringBuilder errorsString = processErrorStrings(errorStrings);
			formBean.setMessage(errorsString.toString());
		}
	}

	// CAP-46503 - refactored moving this here for reuse
	protected IProfileDefinition getProfileDefinitionComponent(AppSessionBean asb) throws AtWinXSException {
		return ProfileDefinitionComponentLocator.locate(asb.getCustomToken());
	}
	protected IProfileInterface getProfileComponent(AppSessionBean asb) throws AtWinXSException {
		return ProfileComponentLocator.locate(asb.getCustomToken());
	}

	protected void loadAlternateProfiles(UserInterface ui, int currentPageNumber, C1UXCustDocPageBean sfBean, AppSessionBean asb, VolatileSessionBean vsb) {
		if (currentPageNumber < UserInterface.NEXT_PAGE_NUMBER_LIST) {
			try {
				HashMap<String,ArrayList<Integer>> profileDropdowns = ui.getAlternateProfileDropdownsC1UX(asb, currentPageNumber, vsb.getSelectedSiteAttribute());
				if ((profileDropdowns != null) && (!profileDropdowns.isEmpty())) {
					IProfileDefinition profileDefAdmin = getProfileDefinitionComponent(asb);
					ProfileDefinitionVOKey profileDefVOKey = new ProfileDefinitionVOKey(asb.getSiteID(), asb.getBuID(), AtWinXSConstant.INVALID_ID);
					ProfileDefinitionVO profileDefVO = null;
					for (Map.Entry<String, ArrayList<Integer>> type : profileDropdowns.entrySet())
	    			{
	    				profileDefVO = profileDefAdmin.getProfileDefinitionByType(profileDefVOKey, type.getKey());
	    				List<ProfileOption> currentProfileOptions = ui.getAlternateProfileSelector().loadProfileOptions(type.getKey(), type.getValue(),
	    						ui.getUiAltProfileSelections(), ui.getProfileSelection(), profileDefVO.getProfileDefinitionID());
	    				if ((currentProfileOptions != null) && (currentProfileOptions.size() >1))  { //only show if options available
	    					sfBean.getAlternateProfiles().add(makeC1UXAlternateProfileOptions(profileDefVO, ui, currentProfileOptions, asb));
	    				}
	    			}
				}
			} catch (AtWinXSException e) {
				baseLogger.error(e.getMessage());
			}
		}
	}

	protected C1UXAlternateProfileOptions makeC1UXAlternateProfileOptions(ProfileDefinitionVO profileDefVO,
			UserInterface ui, List<ProfileOption> currentProfileOptions, AppSessionBean asb) throws AtWinXSException {
		C1UXAlternateProfileOptions bean = new C1UXAlternateProfileOptions();
		bean.setLabel(profileDefVO.getProfileType());
		bean.setDefinitionID(profileDefVO.getProfileDefinitionID());
		bean.setProfileOptions(currentProfileOptions);
		Integer selectedProfileNumber = ui.getUiAltProfileSelections().get(profileDefVO.getProfileType());
		if ((selectedProfileNumber != null) && (selectedProfileNumber.intValue() > 0)) {
			IProfileInterface profileAdmin = getProfileComponent(asb);
			ProfileVO vo = profileAdmin.getProfileByProfileNumber(profileDefVO.getSiteID(), selectedProfileNumber);
			bean.setCurrentProfileNumber(selectedProfileNumber);
			bean.setCurrentProfileID(vo.getProfileID());
		}
		return bean;
	}

	public int getCurrentPageOutsideSubmit(UserInterface ui)
	{
		Map<String, String> c1uxParams = new HashMap<>();
		return ui.getCurrentPageC1UX(c1uxParams);
	}

	protected KitSession getKitSession(AppSessionBean appSessionBean)	{
		try {
			return (KitSession) sessionHandlerService.loadSession(appSessionBean.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
		} catch (Exception eofex) {
			baseLogger.error(this.getClass().getName() + " - " + eofex.getMessage(), eofex);
			return null;
		}
	}

	protected C1UXImprintHistoryOptions loadImprintHistory(UserInterface ui, int pageNum, AppSessionBean asb, boolean searching) throws AtWinXSException {
		if (onEditableFirstPageOE(ui, pageNum)) {
			ImprintHistorySelector selector = ui.getImprintHistorySelector();
			if ((selector != null) && (selector.isShown())) {
				if (selector.getHistoryOptions() == null) {
					selector.loadOrderHistoryOptions();
				}
				if (searching || selector.isOrdersFound() || asb.isAllowImpHistSrchInd()) {
					return new C1UXImprintHistoryOptions(selector);
				}
			}
		}
		return null;
	}

}
