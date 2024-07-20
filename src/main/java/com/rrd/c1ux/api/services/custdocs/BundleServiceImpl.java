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
 * 	03/11/24	A Boomker			CAP-46490		Initial version
 * 	03/29/24	A Boomker			CAP-46493/CAP-46494	fixes for navigation
 *  04/03/24	A Boomker			CAP-46494		Proofing overrides for bundle
 * 	04/03/24	R Ruth				CAP-46492		Modify generation of customization steps link for bundle items
 *  04/08/24	A Boomker			CAP-48464		Save pulled out to its own method for bundle to extend
 *  05/31/24	A Boomker			CAP-42233		Clear original list ID records after add to cart if new one created
 *  06/03/24	A Boomker			CAP-46501		Add testing for alternate profile handling
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.customdocs.bundle.entity.BundleComponent;
import com.rrd.custompoint.orderentry.bundle.BundleUserInterface;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.UIEvent;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocProofFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.BundleItem;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.interfaces.ICustomDocsUserInterface;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDTemplateUserInterfaceVersionVO;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDTemplateUserInterfaceVersionVOKey;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.vo.OrderLineVOKey;

public class BundleServiceImpl extends CustomDocsServiceImpl implements BundleUIService  {

	private static final Logger logger = LoggerFactory.getLogger(BundleServiceImpl.class);

	public BundleServiceImpl(CustomDocsService service, SessionContainer sc) {
		super(service.getTranslationService(), service.getObjectMapFactoryService(), service.getSessionHandlerService());
		this.setCustomizationTokenForSession(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		this.setOeJavascriptServerAccessPath(service.getOeJavascriptServerAccessPath());
	}

	// the next constructor version is ONLY to be used for junits!
	public BundleServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFacService,
			SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	protected CustomDocumentItem createAndInitItem(Map<String, String> requestParams, OEOrderSessionBean orderSession,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			PunchoutSessionBean punchoutSessionBean, Collection<String> errors) throws AtWinXSException {
		BundleItem item = objectMapFactoryService.getEntityObjectMap().getEntity(BundleItem.class,
				appSessionBean.getCustomToken()); // CAP-1434 create new item every time in initialize
		item.initializeItemC1UX(requestParams, appSessionBean, volatileSessionBean, -1, orderSession.getUserSettings(),
				orderSession, errors, punchoutSessionBean); // CAP-20338
		return item;
	}

	// this is currently copied from Custom Docs and will have to change
	@Override
	protected void setProofFields(UserInterface ui, C1UXCustDocPageBean sfBean, int pageNumber, AppSessionBean asb)
			throws AtWinXSException {
		if (pageNumber == UserInterface.NEXT_PAGE_NUMBER_PROOF) {
			BundleUserInterface bui = (BundleUserInterface) ui;
			ProofType pt = getProofType(bui);
			CustomDocumentItem component = getBundleComponentFromMap(bui.getConvertedCustDocItemsMap(), bui.getBundleComponentSequenceCtr());
			sfBean.setComponentDescription(component.getDescription());
			sfBean.setEventSeqNum(String.valueOf(bui.getBundleComponentSequenceCtr()));

			String transID = bui.getComponentProofID(pt, bui.getCurrentBundleComponent());
			if (!Util.isBlankOrNull(transID)) {
				sfBean.setImageProofUrl(ui.getProofURL(ProofType.IMAGE, asb.getCurrentEnvCd(), ui.isAlwaysSsl(),
						transID, AtWinXSConstant.EMPTY_STRING));
			}

			UserInterface componentUI = getComponentUserInterface(bui, bui.getConvertedCustDocItemsMap(), false);
			sfBean.setPdfProofAvailable(componentUI.isPdfProofAvailable());
			sfBean.setJellyVisionProofAvailable(componentUI.isJellyVisionProofAvailable());
			List<Integer> proofPages = loadPagesToProof(componentUI);
			sfBean.setPagesToProof(proofPages.isEmpty() ? null : proofPages);
		}
	}


	// this is currently copied from Custom Docs and will have to change
	@Override
	public void finalizeProofModelDetails(CustomDocumentItem item, UserInterface ui, CustDocProofFormBean formBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession)
			throws AtWinXSException {
		BundleUserInterface bui = (BundleUserInterface) ui;
		UserInterface componentUI = getComponentUserInterface(bui, bui.getConvertedCustDocItemsMap(), false);

		ICustomDocsUserInterface uiServer = getCDInterface(appSessionBean);
		CDTemplateUserInterfaceVersionVOKey uiVersionKey = componentUI.getUiKey().getVOKey();
		CDTemplateUserInterfaceVersionVO uiVersionVO = uiServer.getTempUIVersionVO(uiVersionKey);
		item.setXertEnabled(uiVersionVO.isXertCompositionEnabled());
		item.setPrecisionDialogueEnabled(uiVersionVO.isPrecisionDialogueEnabled());
		formBean.setProofView(componentUI);
		formBean.setDefaultEmailAddess(appSessionBean.getEmailAddress());
		setTermsConditionsKeys(formBean);
	}

	// this is currently copied from Custom Docs and will have to change
	@Override
	protected void generateNextProof(UserInterface ui, CustDocProofFormBean formBean, AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException {
		// do nothing as this was generated in navigation
	}

		// this is currently copied from Custom Docs and will have to change
	@Override
	protected boolean handleLeavingEvents(UIEvent direction, CustomDocumentItem item, SessionContainer sc,
			C1UXCustDocPageBean sfBean, Map<String, String> uiRequest, Collection<String> errorStrings,
			CustDocUIFormBean formBean) throws AtWinXSException {
		UserInterface ui = item.getUserInterface();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession orderSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = orderSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();

		if ((direction == UserInterface.UIEvent.CANCEL) || (direction == UserInterface.UIEvent.CANCEL_ORDER)) // cP-10842
		{ // these should just redirect
			performCancelOrCancelOrder(orderSession, ui, item, direction, sc, sfBean);
			return true;
		}
		else if (direction == UserInterface.UIEvent.SAVE_AND_EXIT)
		{
			ui.readPageC1UX(uiRequest, errorStrings, false, direction, false); // no validating
			int currentPage = ui.getCurrentPageC1UX(uiRequest);
			saveIncompleteOrderLine(item, errorStrings, sc, currentPage, sfBean); // CAP-48464 - for some reason bundle save and exit uses base cust doc

			errorStrings.clear();
			String nextpage = RouteConstants.CART_ENTRY_ROUTING_URL;
				resetUIAndItem(volatileSessionBean, oeSessionBean);
				saveAllUpdatedSessions(orderSession, sc.getApplicationVolatileSession(), sc.getApplicationSession(),
						appSessionBean.getSessionID()); // CAP-44307
  			sfBean.setRedirectRouting(nextpage);
			sfBean.setSuccess(true);
			return true;
		}

		return false;
	}

	// this is currently copied from Custom Docs and will have to change
	@Override
	protected void navigateForward(Map<String, String> uiRequest, CustDocUIFormBean uiBean,
			Collection<String> errorStrings, UIEvent direction, CustomDocumentItem item, C1UXCustDocPageBean sfBean,
			SessionContainer sc) throws AtWinXSException {
		BundleUserInterface ui = (BundleUserInterface) item.getUserInterface();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		// this should be next, navigate pages, or previous only
		readSubmittedData(direction, uiBean, ui, uiRequest, errorStrings);
		if (!isStaySituation(direction, ui, errorStrings)) { // New requests are out of scope for now. See
																// BaseUIController method of same name
			int currentPageNum = ui.getCurrentPageC1UX(uiRequest);
			if (currentPageNum != UserInterface.NEXT_PAGE_NUMBER_PROOF) {
				ui.resetAllProofsC1UX();
			}
			// bundles and campaigns are out of scope for now.
			int nextPage = ui.getNextPageNumberC1UX(currentPageNum, direction, uiRequest, appSessionBean); // CP-10444

			if (nextPage == UserInterface.NEXT_PAGE_NUMBER_PROOF) {
				nextPage = navigateProofs(uiRequest, sfBean, ui, errorStrings, sc, currentPageNum, direction);
			}

			// now handling if we go to lists, proofing, or just to another page in the UI
			if (nextPage == UserInterface.NEXT_PAGE_NUMBER_EXIT) {
				handleExitNonPF(currentPageNum, sc, item, errorStrings, sfBean);
			}
			else {
				if ((currentPageNum == UserInterface.NEXT_PAGE_NUMBER_PROOF) && (nextPage < currentPageNum)) {
					ui.resetAllProofs();
				}
				determineNavigatePages(uiRequest, direction, currentPageNum, nextPage, sfBean);
				ui.setNextPageNumber(nextPage);
			}
		}

		showProofErrors(errorStrings, uiBean);

		if (!Util.isBlankOrNull(uiBean.getError())) {
			sfBean.setMessage(uiBean.getError());
			sfBean.setSuccess(false);
		}

		checkForSuccess(sfBean);
	}

	protected int navigateProofs(Map<String, String> uiRequest, C1UXCustDocPageBean sfBean,
			BundleUserInterface ui, Collection<String> errorStrings, SessionContainer sc, int currentPageNum, UIEvent direction) {
		int trueNextPage = UserInterface.NEXT_PAGE_NUMBER_PROOF;
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession vs = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = vs.getVolatileSessionBean();
		try {
			boolean hasOrderedCDComponents = true;
			if (currentPageNum < UserInterface.NEXT_PAGE_NUMBER_PROOF) {
				Collection<BundleComponent> orderedComponents = ui.getOrderedBundleItemComponentsC1UX();

				// CP-13104 8.1.6 [VEC] If we don't have an ordered component OR a customdoc ordered component, go straight to the Shopping Cart
				if(orderedComponents != null && !orderedComponents.isEmpty())
				{
					hasOrderedCDComponents = ui.initializeOrderedBundleComponentsC1UX(appSessionBean, oeSessionBean, volatileSessionBean, oeSessionBean.getUserSettings(), orderedComponents);
				} else {
					sfBean.setMessage(getTranslation(appSessionBean,
							"sf.noBundleComponentsErr",
							"The current selections do not result in any items. Please review your selections and make changes before trying again."));
					return currentPageNum;
				}
			}
			else {
				setNewSequenceNumberFromProofPage(uiRequest, direction, ui);
			}

			if (doneProofingAllComponents(ui, hasOrderedCDComponents)) {
				return UserInterface.NEXT_PAGE_NUMBER_EXIT;
			}
			generateComponentProof(ui, sfBean, sc);

		} catch(Exception e) {
			trueNextPage = currentPageNum;
			logger.error(e.getMessage());
			combineErrors(errorStrings, sfBean);
			if (Util.isBlankOrNull(sfBean.getMessage())) {
				setGenericHeaderError(sfBean, appSessionBean);
			}
		}
		return trueNextPage;
	}


	protected void setNewSequenceNumberFromProofPage(Map<String, String> uiRequest, UIEvent direction, BundleUserInterface ui) {
		ui.setProofSeqFromRequest(Util.safeStringToInt(uiRequest.get("eventSeqNum")));
		if ((ui.getConvertedCustDocItemsMap() != null) && (ui.getConvertedCustDocItemsMap().containsKey(ui.getBundleComponentSequenceCtr()))
				&& (direction == UIEvent.NEXT)) {
			ui.setProofSeqFromRequest(ui.getBundleComponentSequenceCtr() + 1);
		} else if (direction == UIEvent.PREVIOUS) {
			if(ui.getProofSeqFromRequest() > 1) { // Verify if selected proof is not the first proof
				ui.setProofSeqFromRequest(ui.getProofSeqFromRequest() - 1);
				ui.setBundleComponentSequenceCtr(ui.getProofSeqFromRequest());
			} else if (ui.getBundleComponentSequenceCtr() > 0) {
				ui.setProofSeqFromRequest(ui.getBundleComponentSequenceCtr() - 1);
				ui.setBundleComponentSequenceCtr(ui.getProofSeqFromRequest());
			} else {
				ui.setBundleComponentSequenceCtr(0);
			}
		}
	}

	// CAP-48464 - add override for save of expired line
	@Override
	protected void saveIncompleteOrderLine(CustomDocumentItem item, Collection<String> errorStrings, SessionContainer sc, int currentPage, C1UXCustDocBaseResponse sfBean) throws AtWinXSException {
		saveBundleItem(sc, (BundleItem) item, sfBean, false);
	}

	protected boolean doneProofingAllComponents(BundleUserInterface ui, boolean hasOrderedCDComponents) {
		return ((!hasOrderedCDComponents) || (ui.getConvertedCustDocItemsMap().size() == ui.getBundleComponentSequenceCtr()));
	}

	protected UserInterface getComponentUserInterface(BundleUserInterface bundleUI, Map<Integer, CustomDocumentItem> cdItems, boolean next)
	{
		int proofSeqNum = bundleUI.getBundleComponentSequenceCtr();
		if (next) {
			proofSeqNum++;
		}

		CustomDocumentItem component = getBundleComponentFromMap(cdItems, proofSeqNum);
		return (component != null) ? component.getUserInterface() : bundleUI;
	}

	protected ProofType getComponentProofType(BundleUserInterface ui, Map<Integer, CustomDocumentItem> cdItems) {
		// CAP-10074 TH - Get the component UI that we are working on here
		UserInterface componentUI = getComponentUserInterface(ui, cdItems, true);

		// CAP-10074 TH - Added code to get proof type for component (not master)
		return getProofType(componentUI);
	}

	protected String generateNewComponentProofID(BundleUserInterface ui, SessionContainer sc, Map<Integer, CustomDocumentItem> cdItems)
			throws AtWinXSException {
		ProofType pt = getComponentProofType(ui, cdItems);
		return generateNewBundleComponentProofID(ui, sc, cdItems, pt);
	}

	protected void updateComponentCounter(BundleUserInterface bui) {
		Map<Integer, CustomDocumentItem> comps = bui.getConvertedCustDocItemsMap();
		if ((comps != null) && (!comps.isEmpty()) && (bui.getCurrentBundleComponent() != null)) {
			for (Entry<Integer, CustomDocumentItem> comp : comps.entrySet()) {
				if (comp.getValue().getVendorItemNumber().equals(bui.getCurrentBundleComponent().getWalVendorRrdItem())) {
					bui.setBundleComponentSequenceCtr(comp.getKey().intValue());
				}
			}
		}
	}

	protected void generateComponentProof(BundleUserInterface ui, C1UXCustDocBaseResponse formBean, SessionContainer sc) {
		ui.determineComponentToProofC1UX();
		Map<Integer, CustomDocumentItem> cdItems = ui.getConvertedCustDocItemsMap();

		String proofID = getCurrentBundleComponentProofID(ui);
		boolean preprocessingNeeded = false;
		if (Util.isBlankOrNull(proofID) && (cdItems.size() >= ui.getBundleComponentSequenceCtr()))
		{
			try
			{
				proofID = generateNewComponentProofID(ui, sc, cdItems);
			}
			catch(AtWinXSMsgException e)
			{
				preprocessingNeeded = handleProofMsgException(ui, e, formBean);
			}
			catch(Exception e)
			{
				identifyHardStopFailureProofing(ui, e, formBean);
			}
		}
		updateComponentCounter(ui);

		confirmProofSuccess(formBean, proofID, preprocessingNeeded);
	}

	protected void identifyHardStopFailureProofing(BundleUserInterface ui, Exception e, C1UXCustDocBaseResponse formBean) {
		if ((ui.isEceHardStop()) || (ui.isDlPagerHardStop())
				|| ((e instanceof AtWinXSMsgException)
						&& (((AtWinXSMsgException)e).getMsg().getErrGeneralMsg().indexOf("FATAL") == 0)))
		{
			formBean.setHardStopFailure(true);
		}
		logger.error("ui.generateNewProof() for bundle component failed in BundleServiceImpl " + e.getMessage(), e); // CAP-16459
		stripFatalFlag(formBean, e);
	}

	protected boolean handleProofMsgException(BundleUserInterface ui, AtWinXSMsgException e, C1UXCustDocBaseResponse formBean) {
		logger.error("BundleServiceImpl call to ui.generateNewProof(); threw error - " + e.getMsg(), e);
		if ("PREPROCESSING".equals(e.getMsg().getErrGeneralMsg()))
		{
			formBean.setRedirectRouting(ROUTING_WAIT);
			return true;
		}
		identifyHardStopFailureProofing(ui, e, formBean);
		return false;
	}

	protected void confirmProofSuccess(C1UXCustDocBaseResponse formBean, String proofID, boolean preprocessingNeeded) {
		formBean.setSuccess((!Util.isBlankOrNull(proofID)) || preprocessingNeeded);
	}

	// this is currently copied from Custom Docs and will have to change
	@Override
	protected void handleExitNonPF(int currentPageNum, SessionContainer sc, CustomDocumentItem item,
			Collection<String> errorStrings, C1UXCustDocPageBean sfBean) throws AtWinXSException {
		doAddToCart(sc, item, sfBean);
	}

	// CAP-46494
	@Override
	protected void doAddToCart(SessionContainer sc, CustomDocumentItem item, C1UXCustDocBaseResponse sfBean)
			throws AtWinXSException {
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession vs = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = vs.getVolatileSessionBean();
		UserInterface ui = item.getUserInterface();

		String redirect = getForwardToURL(item, ui, sc);

		saveBundleItem(sc, (BundleItem) item, sfBean, true);

		if (sfBean.isSuccess()) {
			ui.clearOrderListInfoC1UX(true); // CAP-42233
			resetUIAndItem(volatileSessionBean, oeSessionBean);
			saveAllUpdatedSessions(oeSession, vs, appSession, appSessionBean.getSessionID());
			sfBean.setRedirectRouting(redirect);
		} else if (Util.isBlankOrNull(sfBean.getMessage())) {
			setGenericHeaderError(sfBean, appSessionBean);
		}
	}

	protected void saveBundleItem(SessionContainer sc, BundleItem bundle, C1UXCustDocBaseResponse sfBean, boolean fullAddToCart) throws AtWinXSException {
		Collection<String> errorStrings = new ArrayList<>();
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession vs = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = vs.getVolatileSessionBean();
		BundleUserInterface ui = bundle.getUserInterface();

		OrderLineVOKey cartLineKey = ui.saveMasterBundleItem(appSessionBean, volatileSessionBean, oeSession, errorStrings, bundle, oeSessionBean, !fullAddToCart, ui.getCurrentPageC1UX(new HashMap<>()));

		if ((cartLineKey != null) && (cartLineKey.getLineNum() > 0) && (errorStrings.isEmpty()))
		{
			sfBean.setSuccess(true);
		}
	}

	@Override
	protected boolean getPageAllowsAddToCartButton(Page page, UserInterface ui, CustomDocumentItem item) {
		return ((item != null) && (page.getPageNumber() == UserInterface.NEXT_PAGE_NUMBER_PROOF)
				&& (doneProofingAllComponents((BundleUserInterface) ui, true)));
	}


	// CAP-46492 - take a parameter of OEOrderSessionBean
	@Override
	protected void populateSFSpecificFields(C1UXCustDocPageBean sfBean, Page thisPage, AppSessionBean asb,
				UserInterface ui, CustomDocumentItem item, VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeOrderBean) {
			try {
				BundleUserInterface bui=((BundleUserInterface)ui);
				sfBean.setUiStepOptions( bui.buildStepOptionsC1UX(thisPage, asb, oeOrderBean));
				sfBean.setPageAllowsSave(getPageAllowsSave(thisPage, ui, item));
				sfBean.setPageAllowsBackButton(getPageAllowsBackButton(thisPage, ui));
				sfBean.setPageAllowsAddToCartButton(getPageAllowsAddToCartButton(thisPage, ui, item));
				sfBean.setEditing(isUIEditing(ui, item));
				sfBean.setCustDocpromptValue("N".equals(volatileSessionBean.getCustDocPromptValue()));
				sfBean.setHdnHasChanged(AtWinXSConstant.EMPTY_STRING);
				sfBean.setPageLoadedDirty(sfBean.isHdnIsDirty());
				setProofFields(ui, sfBean, thisPage.getPageNumber(), asb);
				sfBean.setImprintHistory(loadImprintHistory(ui, thisPage.getPageNumber(), asb, false)); // CAP-46538 - refactored to base
				sfBean.setUserProfileSearch(loadUserProfileSearch(ui, thisPage.getPageNumber())); // CAP-44463
				loadAlternateProfiles(ui, thisPage.getPageNumber(), sfBean, asb, volatileSessionBean); // CAP-44463
				sfBean.setReviewOnly(isReviewOnlyApproval(ui, oeOrderBean)); // CAP=46498
				populateListInformation(sfBean, thisPage, asb, ui, oeOrderBean); // CAP-46498
				createJSFile(sfBean, asb);
				sfBean.setSuccess(true);
			} catch (AtWinXSException e) {
				logger.error("Failed to get storefront data for bundle OE", e);
			} catch (Exception e) {
				logger.error("Failed with unhandled exception for bundle OE", e);
			}

	}
}
