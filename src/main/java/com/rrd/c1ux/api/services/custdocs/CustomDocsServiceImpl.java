/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/18/23				A Boomker				CAP-42295					Initial Version
 *	07/25/23				A Boomker				CAP-42225					More changes for actual init
 *	07/27/23				A Boomker				CAP-42223					Changes for save
 *	07/31/23				A Boomker				CAP-42224					Add event actions map to variables
 *	08/22/23				A Boomker				CAP-43223		            Moving things around for junits
 *  08/22/23                E Anderson              CAP-42843                   Changes for Cancel
 *  08/30/23				A Boomker				CAP-42841					Changes for sample PDFs
 *  09/08/23				A Boomker				CAP-42865					Message missing when add to cart fails
 *  09/11/23				A Boomker				CAP-43528					Add handling for checkbox field of checked state
 *	09/14/23				A Boomker				CAP-43660					Added dateFormat for calendar date input type variables
 *	09/18/23				A Boomker				CAP-42298					Add cancel methods direct access
 *	09/19/23				A Boomker				CAP-42817 					In processErrorMessages(), getting rid of the prefix caret for C1UX
 *	09/21/23				A Boomker				CAP-44047					In initializeUIOnly(), move things into try block to prevent 500
 *	09/29/23				A Boomker				CAP-44304					Variables hidden and blanked by show hide need to send the front end their default values
 *	09/29/23				A Boomker				CAP-43827					Checkboxes are never required
 *	10/02/23				A Boomker				CAP-44307					Times where order ID can change, must save volatile session
 *	10/17/23				AKJ Omisol				CAP-43024					Added getImprintHistory method for Imprint History
 *	10/25/23				A Boomker				CAP-44908					In reformatFunction(), add open parentheses to regex to prevent partial matches
 *	11/07/23				A Boomker			CAP-44427 and CAP-44463			Added changes to getUIPage for user profile search and working proof handling
 *  11/13/23				A Boomker				CAP-44426					Added handling for update working proof
 *	11/15/23				A Boomker				CAP-44460 					Never return CP path for no image
 *	11/21/23				A Boomker				CAP-44780					Adding variable upload info
 *	11/21/23				A Boomker				CAP-44549					Added handling for save and search
 *	12/04/23				A Boomker				CAP-45654					Force save when event calls for always save
 *	12/12/23				A Boomker				CAP-44490					Save volatile session separately when order ID may change
 *	12/18/23				A Boomker				CAP-45488					Changes to getUIPage to fix formatted paragraphs and external source lists
 *	01/11/24				A Boomker				CAP-43031					Added showThumbnailLabels
 *	02/05/24				R Ruth					CAP-46524					Added changes for CAP-46524
 *	02/13/24				A Boomker				CAP-46309					Added changes for file upload
 *	02/19/24				A Boomker				CAP-44837					Changes to list options to indicate uploads
 *	02/26/24				A Boomker				CAP-47392					In setVariableUncopiableValues(), unencode html instructions
 *	02/27/24				A Boomker				CAP-47446					Moved checkIfBUAllowsEPS() to base
 *	02/27/24				A Boomker				CAP-47446					Uploaded list options should default to selected
 *	03/12/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 *	03/15/24				A Boomker				CAP-46513					Fix checkbox BE population for insertion groups
 * 	03/29/24				A Boomker				CAP-46493/CAP-46494			fixes for navigation
 *	04/03/24				R Ruth					CAP-46492					Modify generation of customization steps link for bundle items
 *  04/08/24				A Boomker				CAP-48464					Save pulled out to its own method for bundle to extend
 *  04/08/24				A Boomker				CAP-48500					Add constructor for re-initializing back to cust docs
 *  04/16/24				R Ruth					CAP-46520					Clear error strings.
 * 	04/25/24				A Boomker				CAP-46498					Handling for lists in getUIPage API
 * 	05/10/24				A Boomker				CAP-49273					Redirect on initialize for proof/merge only
 *	05/20/24				A Boomker				CAP-46519					Changes in setVarFieldIDsAndNames() for selection lists only
 * 	05/30/24				A Boomker				CAP-46522					Send selected uploads for multiple selects
 *  05/31/24				A Boomker				CAP-42233					Clear original list ID records after add to cart if new one created
 *  06/03/24				A Boomker				CAP-46501					Add testing for alternate profile handling
 *  06/28/24				A Boomker				CAP-46503					Refactored alt profile code to base for reuse
 * 	07/01/24				A Boomker				CAP-46488					Added handling for kits
 *	07/02/24				A Boomker				CAP-46489					Added KIT_EDITOR_ROUTING redirect for add to cart
 *	07/02/24				R Ruth					CAP-46487					Removed hardcoded false for image cropper
 *	07/09/24				A Boomker				CAP-46538					Refactored some handling to base for imprint history
 */
package com.rrd.c1ux.api.services.custdocs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocGridAssignment;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocGroupBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXDisplayControl;
import com.rrd.c1ux.api.models.custdocs.C1UXDisplayControls;
import com.rrd.c1ux.api.models.custdocs.C1UXUIList;
import com.rrd.c1ux.api.models.custdocs.C1UXUIListOption;
import com.rrd.c1ux.api.models.custdocs.C1UXUserProfileSearchOptions;
import com.rrd.c1ux.api.models.custdocs.C1UXVariableBean;
import com.rrd.c1ux.api.models.custdocs.C1uxCustDocListDetails;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.customdocs.ui.BaseSelectionListRendererImpl;
import com.rrd.custompoint.customdocs.ui.CheckboxRendererImpl;
import com.rrd.custompoint.customdocs.ui.GridGroup;
import com.rrd.custompoint.customdocs.ui.InsertionGroupVariableImpl;
import com.rrd.custompoint.customdocs.ui.variables.FileUploadVariable;
import com.rrd.custompoint.customdocs.ui.variables.FormattedParagraphVariable;
import com.rrd.custompoint.customdocs.utils.entity.FormattedParagraphProcessor;
import com.rrd.custompoint.orderentry.customdocs.C1UXCustDocGridRow;
import com.rrd.custompoint.orderentry.customdocs.DisplayControl;
import com.rrd.custompoint.orderentry.customdocs.DisplayControls;
import com.rrd.custompoint.orderentry.customdocs.Group;
import com.rrd.custompoint.orderentry.customdocs.Page;
import com.rrd.custompoint.orderentry.customdocs.ProfileSelector;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.EntryPoint;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.OrderListSource;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.ProofType;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.UIEvent;
import com.rrd.custompoint.orderentry.customdocs.Variable;
import com.rrd.custompoint.orderentry.customdocs.Variable.InputType;
import com.rrd.custompoint.orderentry.customdocs.Variable.PageflexType;
import com.rrd.custompoint.orderentry.customdocs.renderers.CheckboxRendererInput;
import com.rrd.custompoint.orderentry.customdocs.renderers.Renderer;
import com.rrd.custompoint.orderentry.customdocs.renderers.RendererInput;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UIList;
import com.rrd.custompoint.orderentry.customdocs.ui.list.UIListOption;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocListDataFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocListSelectionFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocProofFormBean;
import com.rrd.custompoint.orderentry.customdocs.util.CustDocUIFormBean;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.ItemInstructions.InstructionsDisplayOption;
import com.rrd.custompoint.orderentry.entity.RoutedOrder;
import com.rrd.custompoint.orderentry.messagedocs.MessageDocsUserInterface;
import com.rrd.custompoint.orderentry.ui.upload.UploadFile;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.interfaces.ICustomDocsUpload;
import com.wallace.atwinxs.interfaces.ICustomDocsUserInterface;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.IOECustomDocumentComponent;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.kits.ao.MKBaseAssembler;
import com.wallace.atwinxs.kits.ao.MKTemplateOrderAssembler;
import com.wallace.atwinxs.kits.session.KitSession;
import com.wallace.atwinxs.kits.session.MKComponentInfo;
import com.wallace.atwinxs.kits.util.KitsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.lists.vo.ListVOKey;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.customdocs.locator.CustomDocsAdminLocator;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDMaskVO;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDMaskVOKey;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDTemplateUserInterfaceVersionVO;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDTemplateUserInterfaceVersionVOKey;
import com.wallace.atwinxs.orderentry.customdocs.vo.CDUploadVO;
import com.wallace.atwinxs.orderentry.lists.vo.OrderListVO;
import com.wallace.atwinxs.orderentry.locator.OECustomDocLocator;
import com.wallace.atwinxs.orderentry.locator.OEShoppingCartComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineExtendedVO;
import com.wallace.atwinxs.orderentry.vo.OrderLineVOKey;

@Service
public class CustomDocsServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsService {
	protected static final String failedAtWinXSExceptionPrefix = "Failed with AtWinXSException";
	protected static final String failedNonAtWinXSExceptionPrefix = "Failed with non-AtWinXSException";
	protected static final String INVALID_STRING = "-1";
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsServiceImpl.class);

	@Value("${uri.custdocsjs.root}")
	protected String oeJavascriptServerAccessPath;

	public CustomDocsServiceImpl(TranslationService translationService, ObjectMapFactoryService objectMapFacService,
			SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	public static final String N = "N";
	public static final String Y = "Y";



	@Override
	public C1UXCustDocPageBean getCurrentPageUI(SessionContainer sc) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		CustDocUIFormBean formBean = getBeanForPage(item);
		try {
			// now be sure to populate the formBean from the item
			item.populateFormBean(formBean, appSessionBean.getDefaultLocale());
			convertToC1UXObject(sc, item, formBean, sfBean);
		} catch (Exception e) {
			logger.error("Custom Document Item not in session", e);
			failResponse(sfBean, appSessionBean, false, formBean);
		}

		return sfBean;
	}

	protected void failResponse(C1UXCustDocPageBean sfBean, AppSessionBean appSessionBean, boolean initializeFailure,
			CustDocUIFormBean formBean) {
		sfBean.setSuccess(false);
		sfBean.setHardStopFailure(true);
		sfBean.setInitializeFailure(initializeFailure);
		if (initializeFailure) {
			sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		} else if (Util.isBlankOrNull(sfBean.getMessage())) {
			if ((formBean != null) && (!Util.isBlankOrNull(formBean.getError()))) {
				sfBean.setMessage(formBean.getError());
			} else {
				sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
						TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
			}
		}
	}

	protected CustDocUIFormBean getBeanForPage(CustomDocumentItem item) {
		UserInterface ui = item.getUserInterface();
		int pageNum = (ui != null) ? ui.getNextPageNumber() : AtWinXSConstant.INVALID_ID;
		if ((pageNum > AtWinXSConstant.INVALID_ID) && (pageNum < UserInterface.NEXT_PAGE_NUMBER_LIST)) {
			return new CustDocUIFormBean();
		}

		switch (pageNum) {
		case UserInterface.NEXT_PAGE_NUMBER_LIST:
			return new CustDocListSelectionFormBean();
		case UserInterface.NEXT_PAGE_NUMBER_LIST_DATA:
			return new CustDocListDataFormBean();
		case UserInterface.NEXT_PAGE_NUMBER_PROOF:
			return new CustDocProofFormBean();
		case UserInterface.NEXT_PAGE_NUMBER_KEY_PROFILES:
			return new CustDocUIFormBean();
		default:
			return null;
		}
	}

	protected void populateCPBeanForPage(CustomDocumentItem item, UserInterface ui, Page thisPage,
			CustDocUIFormBean bean, AppSessionBean asb, VolatileSessionBean vsb, OrderEntrySession oeSession)
			throws AtWinXSException {
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		buildCustDocUIFormBean(ui, thisPage, bean, asb, vsb);
		loadItemInstructionsJavascript(item, bean, asb);

		if ((thisPage.getPageNumber() > AtWinXSConstant.INVALID_ID) && (thisPage.getPageNumber() < UserInterface.NEXT_PAGE_NUMBER_LIST)) {
			loadUIPageJavascript(ui, thisPage, bean, asb);

		} else {
			switch (thisPage.getPageNumber()) {
			case UserInterface.NEXT_PAGE_NUMBER_LIST:
				loadListSelectionPageJavascript(item, ui, thisPage, bean, asb, vsb, oeSessionBean);
				break;
			case UserInterface.NEXT_PAGE_NUMBER_LIST_DATA:
				loadListDataPageJavascript(item, ui, thisPage, bean, asb, vsb, oeSessionBean);
				break;
			case UserInterface.NEXT_PAGE_NUMBER_PROOF:
				finalizeProofModelDetails(item, ui, (CustDocProofFormBean) bean, asb, vsb, oeSession);
				loadProofPageJavascript(item, ui, thisPage, bean, asb, vsb, oeSessionBean);
				break;
			case UserInterface.NEXT_PAGE_NUMBER_KEY_PROFILES:
				loadKeyPageJavascript(item, ui, thisPage, bean, asb, vsb, oeSessionBean);
				break;
			default:

			}
		}
	}

	protected void loadListSelectionPageJavascript(CustomDocumentItem item, UserInterface ui, Page thisPage,
			CustDocUIFormBean bean, AppSessionBean asb, VolatileSessionBean vsb, OEOrderSessionBean oeSessionBean) {
		// for now, do nothing as this is not yet supported
	}

	protected void loadListDataPageJavascript(CustomDocumentItem item, UserInterface ui, Page thisPage,
			CustDocUIFormBean bean, AppSessionBean asb, VolatileSessionBean vsb, OEOrderSessionBean oeSessionBean) {
		// for now, do nothing as this is not yet supported
	}

	protected void loadKeyPageJavascript(CustomDocumentItem item, UserInterface ui, Page thisPage,
			CustDocUIFormBean bean, AppSessionBean asb, VolatileSessionBean vsb, OEOrderSessionBean oeSessionBean) {
		// for now, do nothing as this is not yet supported
	}

	protected void loadProofPageJavascript(CustomDocumentItem item, UserInterface ui, Page thisPage,
			CustDocUIFormBean bean, AppSessionBean asb, VolatileSessionBean vsb, OEOrderSessionBean oeSessionBean) {
		// for now, do nothing as this is not yet supported
	}

	protected CustomDocumentItem createAndInitItem(Map<String, String> requestParams, OEOrderSessionBean orderSession,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			PunchoutSessionBean punchoutSessionBean, Collection<String> errors) throws AtWinXSException {
		CustomDocumentItem item = objectMapFactoryService.getEntityObjectMap().getEntity(CustomDocumentItem.class,
				appSessionBean.getCustomToken()); // CAP-1434 create new item every time in initialize
		item.initializeItemC1UX(requestParams, appSessionBean, volatileSessionBean, -1, orderSession.getUserSettings(),
				orderSession, errors, punchoutSessionBean); // CAP-20338
		return item;
	}

	protected CustomDocumentItem initializeItem(Map<String, String> requestParams, CustDocUIFormBean formBean,
			OEOrderSessionBean orderSession, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			PunchoutSessionBean punchoutSessionBean, Collection<String> errors) throws AtWinXSException {
		CustomDocumentItem item = createAndInitItem(requestParams, orderSession, appSessionBean, volatileSessionBean,
				punchoutSessionBean, errors); // CAP-20338
		UserInterface ui = item.getUserInterface();
		orderSession.setCurrentCustomDocumentItem(item);
		orderSession.setCurrentCustomDocumentUserInterface(ui);

		// now be sure to populate the formBean from the item
		item.populateFormBean(formBean, appSessionBean.getDefaultLocale());

		// CAP-17072 - make sure to keep starting page
		if (!ui.isSkipDynamicDataCalls()) {
			// CAP-17630 - don't set starting to true if skipping ahead
			formBean.setStarting(true);
			ui.setNextPageNumberToStart(); // initialize to page 1 // CP-10658
			ui.refreshDynamicLists(new ArrayList<>(), ui.getNextPageNumber()); // CP-11371 - need to pass correct page
		}

		// CAP-44463 - initialize profile options if needed
		initializeUIProfileOptions(ui.getProfileSelector(), appSessionBean.getProfileNumber());

		adjustFirstPage(ui); // CAP-49273

		return item;
	}

	protected void initializeUIProfileOptions(ProfileSelector ps, int profileNum) throws AtWinXSException {
		if ((ps != null) && (ps.isShown())) {
			ps.loadProfileOptions();
			ps.setSelectedProfile(profileNum);
		}
	}

	public void convertToC1UXObject(SessionContainer sc, CustomDocumentItem item, CustDocUIFormBean bean,
			C1UXCustDocPageBean sfBean) throws AtWinXSException {
		if ((bean.isHardStopFailure()) && (bean.isInitializeFailure())) {
			AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
			sfBean.setSuccess(false);
			sfBean.setMessage(getTranslation(asb.getDefaultLocale(), asb.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		} else {
			UserInterface ui = item.getUserInterface();
			Page thisPage = getCurrentUIPage(ui);
			sfBean.setTranslationMap(getCustDocTranslationMap(sc));
			loadC1UXPageBean(sfBean, item, ui, thisPage, bean, sc);
		}
	}

	protected Map<String, String> getCustDocTranslationMap(SessionContainer sc) {
		Properties props = translationService.getResourceBundle(sc.getApplicationSession().getAppSessionBean(),
				SFTranslationTextConstants.CUSTOM_DOCS_VIEW_NAME);
		return translationService.convertResourceBundlePropsToMap(props);
	}

	protected void loadC1UXPageBean(C1UXCustDocPageBean sfBean, CustomDocumentItem item, UserInterface ui,
			Page thisPage, CustDocUIFormBean bean, SessionContainer sc) throws AtWinXSException {
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		VolatileSessionBean vsb = sc.getApplicationVolatileSession().getVolatileSessionBean();

		populateCPBeanForPage(item, ui, thisPage, bean, asb, vsb, oeSession);

		if (!Util.isBlankOrNull(bean.getError())) {
			sfBean.setMessage(bean.getError());
			sfBean.setSuccess(false);
		}

		sfBean.setHdnUIStepPageNumber(Integer.toString(thisPage.getPageNumber()));
		sfBean.setItemInstructions(item.getItemInstructions());
		sfBean.setItemNumber(item.getItemNumber());
		sfBean.setItemDescription(item.getDescription());
		loadPageImages(sfBean, thisPage, asb, ui);

		// CAP-46492 - add OEOrderSessionBean and pass to prepareBeanToReturn
		prepareBeanToReturn(sfBean, bean, thisPage, asb, ui, item, vsb,oeSession.getOESessionBean());

		// CAP-46524
		retrieveStoredError(sc, sfBean);
	}

	protected void loadPageImages(C1UXCustDocPageBean sfBean, Page thisPage, AppSessionBean asb, UserInterface ui) {
		String url = null;
		// CAP-42841
		sfBean.setSampleImageURL(thisPage.getSampleImageURLC1UX());
		sfBean.setSamplePDFURL(thisPage.getSamplePdfURLC1UX());
		if (!loadWorkingProof(sfBean, ui, thisPage, asb)) {
			if (!Util.isBlankOrNull(sfBean.getSampleImageURL())) {
				url = sfBean.getSampleImageURL();
			}

			if (Util.isBlankOrNull(url)) {
				url = getC1UXItemImage(ui); // CAP-44460 - never return CP path for no image
			}
			sfBean.setItemImageURL(url);
		}
	}

	// CAP-44460 - never return CP path for no image
	protected String getC1UXItemImage(UserInterface ui) {
		String cpImage = Util.nullToEmpty(ui.getItemImageURL());
		if (cpImage.contains(ModelConstants.CP_NO_IMAGE_NO_CONTEXT)) {
			return AtWinXSConstant.EMPTY_STRING;
		}
		return cpImage;
	}

	// CAP-46492 - add a parameter of OEOrderSessionBean
	protected void prepareBeanToReturn(C1UXCustDocPageBean sfBean, CustDocUIFormBean bean, Page thisPage,
			AppSessionBean asb, UserInterface ui, CustomDocumentItem item, VolatileSessionBean vsb, OEOrderSessionBean oeOrderBean) {
		try {
			BeanUtils.copyProperties(sfBean, bean);
			sfBean.setHdnPageFlexIndicator(ui.isPageflex());
			buildGroups(thisPage, sfBean, ui, asb); // CAP-42224
			populateSFSpecificFields(sfBean, thisPage, asb, ui, item, vsb, oeOrderBean);
		} catch (Exception e) {
			logger.error("Failed to copy/populate form bean with exeption", e);
		}
	}

	// CAP-46492 - add a parameter of OEOrderSessionBean
	protected void populateSFSpecificFields(C1UXCustDocPageBean sfBean, Page thisPage, AppSessionBean asb,
			UserInterface ui, CustomDocumentItem item, VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeOrderBean) {
		try {
			sfBean.setUiStepOptions(ui.buildStepOptionsC1UX(thisPage, asb));
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
			logger.error("Failed to get storefront data for cust doc OE", e);
		} catch (Exception e) {
			logger.error("Failed to create javascript file for cust doc OE", e);
		}

	}

	// CAP-46498
	protected void populateListInformation(C1UXCustDocPageBean sfBean, Page thisPage, AppSessionBean asb,
			UserInterface ui, OEOrderSessionBean oeOrderBean) {
		sfBean.setAllowListSecuritySelection(!getListPrivacySettings(asb));
		int listID = getSingleSelectedListID(ui);
		sfBean.setListMapped(isListMapped(ui, listID));
		sfBean.setSelectedListId(listID);
		sfBean.setSelectedListType(getOrderListSourceType(ui));
		sfBean.setCurrentlySelectedList(loadListDetails(listID, ui, asb));
		loadListDataSaved(sfBean, thisPage, asb, ui);
	}

	protected C1uxCustDocListDetails loadListDetails(int listID, UserInterface ui, AppSessionBean asb) {
		C1uxCustDocListDetails list = null;
		try {
			IManageList listComp = getManageListComponent(asb);
			ListVO listVO = listComp.retrieveAList(new ListVOKey(asb.getSiteID(), asb.getBuID(), listID));
			if (listVO != null) {
				list = makeListDetailObjectFromVO(listVO, ui, asb);
			}
		}
		catch(AtWinXSException e) {
			logger.error("Could not retrieve list info for list ID ", listID, e.toString());
		}
		return list;
	}

	protected void loadListDataSaved(C1UXCustDocPageBean formBean, Page thisPage, AppSessionBean asb, UserInterface ui) {
	    OrderListVO orderListVO = getOrderListVO(ui, asb);
		formBean.setValidRecordCount(orderListVO == null? 0: orderListVO.getGoodRecCount());
		formBean.setInvalidRecordCount(orderListVO == null? 0: orderListVO.getValidFailCount());
		if (thisPage.getPageNumber() == UserInterface.NEXT_PAGE_NUMBER_LIST_DATA)
		{
			try {
				if ((orderListVO == null) || (orderListVO.getGoodRecCount() < 1)) {
					formBean.setHardStopFailure(true);
					formBean.setInitialRecords((orderListVO == null) ? null : loadListDataPage(1, true, orderListVO, ui));
				}
				else {
					formBean.setInitialRecords(loadListDataPage(1, false, orderListVO, ui));
				}
			} catch(AtWinXSException e) {
				logger.error(e.toString());
				setGenericHeaderError(formBean, asb);
			}
			formBean.setListDataSaved(formBean.getInitialRecords() != null);
		} else {
			formBean.setListDataSaved((formBean.getValidRecordCount() + formBean.getInvalidRecordCount()) > 0);
		}

	}

	protected boolean isUIEditing(UserInterface ui, CustomDocumentItem item) {
		return ((item != null) && (ui.getEntryPoint() != UserInterface.EntryPoint.CART_ADD_FROM_STUB)
				&& (ui.getEntryPoint() != UserInterface.EntryPoint.CATALOG_EXPRESS)
				&& (ui.getEntryPoint() != UserInterface.EntryPoint.CATALOG_NORMAL)
				&& (ui.getEntryPoint() != UserInterface.EntryPoint.DMIS_ADD)
				&& (ui.getEntryPoint() != UserInterface.EntryPoint.ORDER_FROM_FILE_ADD) && (!ui.isNewRequest()));
	}

	protected void setProofFields(UserInterface ui, C1UXCustDocPageBean sfBean, int pageNumber, AppSessionBean asb)
			throws AtWinXSException {
		if (pageNumber == UserInterface.NEXT_PAGE_NUMBER_PROOF) {
			sfBean.setPdfProofAvailable(ui.isPdfProofAvailable());
			sfBean.setJellyVisionProofAvailable(ui.isJellyVisionProofAvailable());
			List<Integer> proofPages = loadPagesToProof(ui);
			sfBean.setPagesToProof(proofPages.isEmpty() ? null : proofPages);
			String transID = ui.getProofID(ProofType.IMAGE);
			if (!Util.isBlankOrNull(transID)) {
				sfBean.setImageProofUrl(ui.getProofURL(ProofType.IMAGE, asb.getCurrentEnvCd(), ui.isAlwaysSsl(),
						transID, AtWinXSConstant.EMPTY_STRING));
			}
		}
	}

	protected boolean getPageAllowsBackButton(Page page, UserInterface ui) {
		return ((!ui.isProofOnly()
				&& (!ui.isMergeOnly() || page.getPageNumber() != UserInterface.NEXT_PAGE_NUMBER_LIST)) // CP-11317 Proof
																										// Only,
																										// CAP-2464
																										// merge only
				&& ((page.getPageNumber() != ui.getPages().get(0).getPageNumber())
						|| (ui.isKeyProfileSelectionsRequired()) || (ui.isOrderQuestions())));
	}

	protected boolean getPageAllowsSave(Page thisPage, UserInterface ui, CustomDocumentItem item) {
		return ((item != null) && (!item.isCampaign()) && (ui.entryPointAllowsSave())
				&& (thisPage.getPageNumber() < UserInterface.NEXT_PAGE_NUMBER_LIST));
	}

	protected boolean getPageAllowsAddToCartButton(Page page, UserInterface ui, CustomDocumentItem item) {
		return ((item != null) && ((ui.isProofOnly()) || (!ui.isPageflex() && ui.isLastPage(page.getPageNumber())) // CP-11317
																													// Proof
																													// Only,
																													// CAP-2464
																													// merge
																													// only
				|| (page.getPageNumber() == UserInterface.NEXT_PAGE_NUMBER_PROOF)));
	}

	protected void createJSFile(C1UXCustDocPageBean sfBean, AppSessionBean asb) {
		try {
			String jsFileName = getScriptFileName(asb.getSessionID());
			PrintWriter writer = new PrintWriter(
					new BufferedWriter(new FileWriter(getOeJavascriptServerAccessPath() + jsFileName, true)));
			writeJavscriptToFile(sfBean, writer);
			writer.println("");
			writer.close();
			sfBean.setJavascriptFilePath(jsFileName);
			clearJSFromBean(sfBean);
		} catch (IOException ioe) { // just skip the log if writer can't be opened.
			logger.error(this.getClass().getName() + " - " + ioe.getMessage(), ioe);
		}
	}

	protected void clearJSFromBean(C1UXCustDocPageBean sfBean) {
		sfBean.setShowHideJavascript(AtWinXSConstant.EMPTY_STRING);
		sfBean.setShowHideAltProfSelJS(AtWinXSConstant.EMPTY_STRING);
		sfBean.setSkipProofJS(AtWinXSConstant.EMPTY_STRING);

		sfBean.setInstructionsJavascript(AtWinXSConstant.EMPTY_STRING);
		sfBean.setVariablesSection(AtWinXSConstant.EMPTY_STRING);
		sfBean.setPageValidation(AtWinXSConstant.EMPTY_STRING);
		sfBean.setDragDropJS(AtWinXSConstant.EMPTY_STRING);
	}

	protected String getScriptFileName(int sessionID) throws IOException {
		SimpleDateFormat formatter = new SimpleDateFormat(OrderEntryConstants.CP_SHIPPING_LIST_DATE_FORMAT);
		String jsFileName = "cd_" + sessionID + formatter.format(new java.util.Date()) + ".js";
		File path = new File(getOeJavascriptServerAccessPath());
		if (!path.exists()) {
			path.mkdir();
		}
		File script = new File(getOeJavascriptServerAccessPath() + jsFileName);
		if (script.exists()) {
			Files.delete(script.toPath());
			if (!script.createNewFile()) {
				throw new IOException("Failed to create new file after deleting original");
			}
		}
		return jsFileName;
	}

	protected void writeJavscriptToFile(C1UXCustDocPageBean sfBean, PrintWriter writer) {
		Set<String> allFunctionNames = new HashSet<>();

		writer.println(formatJavaScriptForFE(sfBean.getShowHideJavascript(), allFunctionNames));
		writer.println(formatJavaScriptForFE(sfBean.getShowHideAltProfSelJS(), allFunctionNames));
		writer.println(formatJavaScriptForFE(sfBean.getSkipProofJS(), allFunctionNames));

		writer.println(formatJavaScriptForFE(sfBean.getInstructionsJavascript(), allFunctionNames));
		writer.println(formatJavaScriptForFE(sfBean.getVariablesSection(), allFunctionNames));

		writer.println(formatJavaScriptForFE(sfBean.getPageValidation(), allFunctionNames));
		writer.println(formatJavaScriptForFE(sfBean.getDragDropJS(), allFunctionNames));

		// cdFn
		writer.println("var cdFn = function() {");
		writer.println("return {");
		boolean addComma = false;
		for (String n : allFunctionNames) {
			if (addComma) {
				writer.print(",");
			} else {
				addComma = true;
			}
			writer.println(n + ": " + n);
		}
		writer.println("}");
		writer.println("}();");
	}

	protected String formatJavaScriptForFE(String js, Set<String> functionNames) {
		String javaScript = "";

		if (!Util.isBlankOrNull(js)) {
			javaScript = removeScriptTags(js);
			javaScript = cleanForFE(javaScript);
			javaScript = reformatFunction(javaScript, functionNames);
		}

		return javaScript;
	}

	public static final String START_SCRIPT_TAG = "<script";
	public static final String END_SCRIPT_TAG = "</script>";

	protected String removeScriptTags(String javascript) {
		if (Util.isBlankOrNull(javascript)) {
			return AtWinXSConstant.EMPTY_STRING;
		}
		javascript = javascript.replace("SCRIPT", "script");
		javascript = javascript.replace(END_SCRIPT_TAG, AtWinXSConstant.EMPTY_STRING);
		int index = javascript.indexOf(START_SCRIPT_TAG);
		int endIndex = -1;
		StringBuilder finalScript = new StringBuilder();
		while (index >= 0) {
			endIndex = javascript.indexOf(">", index);
			if (index > 0) {
				finalScript.append(javascript.substring(0, index));
			}
			if (endIndex > 0) {
				javascript = javascript.substring(endIndex);
				if (javascript.length() > 1) {
					javascript = javascript.substring(1);
				}
			}

			index = javascript.indexOf(START_SCRIPT_TAG);
		}
		finalScript.append(javascript);
		return finalScript.toString();
	}

	protected String cleanForFE(String javaScript) {
		String newScript = javaScript;

		// Remove any location.href entries.
		try {
			newScript = javaScript.replaceAll("location.href=(.*?);", "");
		} catch (PatternSyntaxException pse) {
			logger.error(this.getClass().getName() + " - " + pse.getMessage(), pse);
		}

		return newScript;
	}

	public static final String OPEN_PARENS_REGEX = "\\(";

	protected String reformatFunction(String javaScript, Set<String> allFunctionNames) {
		String newScript = javaScript;

		// Create a Pattern and Matcher to do the search for function names
		Pattern pattern = Pattern.compile("function\\s*(.*?)\\(");
		Matcher matcher = pattern.matcher(javaScript);

		// Get all of the function names
		Set<String> names = new HashSet<>();
		while (matcher.find()) {
			names.add(matcher.group(1));
		}
		allFunctionNames.addAll(names);

		// Reformat the functions
		for (String name : names) { // CAP-44908 - need the open parentheses to not cause partial matches
			newScript = newScript.replaceFirst("function\\s*" + name + OPEN_PARENS_REGEX,
					"var " + name + " = function " + name + OPEN_PARENS_REGEX);
		}

		return newScript;
	}

	protected void buildGroups(Page thisPage, C1UXCustDocPageBean sfBean, UserInterface ui, AppSessionBean asb)
			throws IllegalAccessException, InvocationTargetException {
		Map<Integer, Map<String, String>> varActionsMap = thisPage.getEventActionsJavascriptC1UX(asb,
				ui.getAllVariables(), ui.isFieldsEditable(), ui); // CAP-42224

		if ((thisPage.getGroups() != null) && (!thisPage.getGroups().isEmpty())) {
			List<C1UXCustDocGroupBean> groups = new ArrayList<>();
			for (Group grp : thisPage.getGroups()) {
				groups.add(createC1UXGroupBean(grp, varActionsMap, sfBean, asb));
			}
			sfBean.setGroups(groups);
			loadPageTranslations(ui, sfBean, asb);
		}
	}

	protected void loadPageTranslations(UserInterface ui, C1UXCustDocPageBean sfBean, AppSessionBean asb) {
		if (ui instanceof MessageDocsUserInterface) {
			sfBean.setAddRowButtonText(getTranslation(asb, "addFundButtonLbl", "Add Fund"));
		} else {
			sfBean.setAddRowButtonText(getTranslation(asb, "addRowButtonLbl", "Add Row"));
		}
	}

	/**
	 * @param item
	 * @param ui
	 * @param page
	 * @param formBean
	 * @param appSessionBean
	 * @param volatileSessionBean
	 * @param oeSessionBean
	 * @throws AtWinXSException
	 */
	protected void buildCustDocUIFormBean(UserInterface ui, Page page, CustDocUIFormBean formBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException {
		formBean.setHdnVar(ui.getRefreshListTriggerVariable()); // CP-10390
		// CP-13307
		boolean orderFromFile = volatileSessionBean
				.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE;
		formBean.setOrderFromFile(AtWinXSConstant.EMPTY_STRING + (orderFromFile));

		int currentPageNumber = page.getPageNumber();
		boolean beforeLists = (currentPageNumber < UserInterface.NEXT_PAGE_NUMBER_LIST);
		if (beforeLists) {
			ui.resetNonWorkingProofs();
		}
		// now render left side
		formBean.setItemImages(AtWinXSConstant.EMPTY_STRING); // user interface has method
																// renderUIImageSection(appSessionBean, page,
																// beforeLists, item))
		formBean.setItemInfo(AtWinXSConstant.EMPTY_STRING); // user interface has method renderItemInfoSection(page,
															// item))
		// now render buttons
		// CAP-10232 Pass in isShowSaveOrders check.
		formBean.setOptionalButtons(AtWinXSConstant.EMPTY_STRING); // user interface has method
																	// renderOptionalButtons(appSessionBean,
																	// currentPageNumber, item, orderFromFile,
																	// oeSessionBean.getUserSettings().isShowSaveOrders()))

		// now render navigation
		formBean.setHeaderNavigation(AtWinXSConstant.EMPTY_STRING);
		formBean.setLeftNavigation(AtWinXSConstant.EMPTY_STRING);
		formBean.setFooterNavigation(AtWinXSConstant.EMPTY_STRING); // CP-11317
																										// Proof Only
		boolean allowFailedProofs = getAllowFailedProofs();
		formBean.setAllowFailedProof(ui.isTryUI() || allowFailedProofs); // CAP-30048
		setBlankFields(formBean);
		setFieldsNoLogic(formBean, ui, page, appSessionBean);
	}

	public void finalizeProofModelDetails(CustomDocumentItem item, UserInterface ui, CustDocProofFormBean formBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean, OrderEntrySession oeSession)
			throws AtWinXSException {
		ICustomDocsUserInterface uiServer = getCDInterface(appSessionBean);
		CDTemplateUserInterfaceVersionVOKey uiVersionKey = ui.getUiKey().getVOKey();
		CDTemplateUserInterfaceVersionVO uiVersionVO = uiServer.getTempUIVersionVO(uiVersionKey);
		item.setXertEnabled(uiVersionVO.isXertCompositionEnabled());

		item.setPrecisionDialogueEnabled(uiVersionVO.isPrecisionDialogueEnabled());
		formBean.setProofingButtons(ui.renderSpecialtyProofButtons(item).toString());

		formBean.setProofView(ui);

		formBean.setDefaultEmailAddess(appSessionBean.getEmailAddress());
		setTermsConditionsKeys(formBean);
		generateNextProof(ui, formBean, appSessionBean, volatileSessionBean);
		saveFullOESessionInfo(oeSession, appSessionBean.getSessionID());
	}

	protected List<Integer> loadPagesToProof(UserInterface ui) {
		if ((ui.getPagesToProof() == null) || (ui.getPagesToProof().isEmpty())) {
			return Collections.emptyList();
		}
		Integer[] unorderedList = ui.getPagesToProof().toArray(new Integer[0]);
		Arrays.sort(unorderedList);
		List<Integer> orderedList = new ArrayList<>();
		Collections.addAll(orderedList, unorderedList);
		return orderedList;
	}

	protected void generateNextProof(UserInterface ui, CustDocProofFormBean formBean, AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException {
		// this must be extended for other item classifications!
		ProofType pt = getProofType(ui);

		// CP-9752 TH - Change to only call to get proof. The proof page will handle
		// display
		String proofID = ui.getProofID(pt);// CP-12601-ELA
		boolean hardStopFailure = formBean.isHardStopFailure();
		boolean preprocessingNeeded = false; // CAP-6538

		// not dealing with wait page now

		if (Util.isBlankOrNull(proofID)) {
			Collection<String> errors = new ArrayList<>();
			try {
				preprocessingNeeded = ui.generateNewProof(pt, appSessionBean, volatileSessionBean, errors);// CP-12601-ELA,
																											// CAP-6538

				if (!preprocessingNeeded && !hardStopFailure) { // CAP-6538
					proofID = ui.getProofID(pt);// CP-12601-ELA
					if (proofID != null) { // CAP-6538 - make sure errors during proof creation get shown to the user
						formBean.setHiddenTransactionID(proofID);
					}
				} else if (preprocessingNeeded) {
					// we are not dealing with wait page right now
				}
			} catch (Exception e) {
				// CAP-19287 - all initial failures are hard stops now
				hardStopFailure = true;
				logger.error("Failed in CustomDocsServiceImpl ", e); // CAP-16459
				String msg = e.getMessage();
				if (msg.startsWith(FATAL_ERROR_PREFIX_PROOFING))
				{
					msg = msg.substring(5);
				}
				formBean.setError(msg);
			}

			showProofErrors(errors, formBean);
		} else { // CAP-6538 show normal buttons
			formBean.setHiddenTransactionID(proofID);
		}

		formBean.setHardStopFailure(hardStopFailure);
	}


	public void showProofErrors(Collection<String> errorStrings, CustDocUIFormBean formBean) {
		if (!errorStrings.isEmpty()) {
			StringBuilder errorsString = processErrorStrings(errorStrings);

			if (!Util.isBlankOrNull(formBean.getError())) {
				if (formBean.getError().startsWith(FATAL_ERROR_PREFIX_PROOFING)) {
					formBean.setError(formBean.getError().substring(5));
				}
				formBean.setError(formBean.getError() + errorsString.toString());
			} else {
				formBean.setError(errorsString.toString());
			}
		}
	}

	protected void setTermsConditionsKeys(CustDocProofFormBean formBean) {
		formBean.setTermsConditionsText(getTermsConditionsText());
		formBean.setTermsProofPopupWaringText1(getTermsProofPopupWarningText1());
		formBean.setTermsProofPopupWaringText2(getTermsProofPopupWarningText2());
	}

	protected String getTermsConditionsText() {
		return TranslationTextConstants.TRANS_NM_PROOF_TERMS;
	}

	protected String getTermsProofPopupWarningText1() {
		return TranslationTextConstants.TRANS_NM_PROOF_WARNING_PART1;
	}

	protected String getTermsProofPopupWarningText2() {
		return TranslationTextConstants.TRANS_NM_PROOF_WARNING_PART2;
	}

	protected ICustomDocsUserInterface getCDInterface(AppSessionBean appSessionBean) throws AtWinXSException {
		return CustomDocsAdminLocator.locateCustomDocumentsUserInterfaceComponent(appSessionBean.getCustomToken());
	}

	protected void loadItemInstructionsJavascript(CustomDocumentItem item, CustDocUIFormBean formBean,
			AppSessionBean appSessionBean) {
		if ((item != null) && (item.getItemInstructionsObj() != null) // CAP-1291 - only use the already initialized
																		// item instructions object
				&& ((!Util.isBlankOrNull(item.getItemInstructionsObj().getButtonLink()))
						|| ((!item.getItemInstructionsObj().isTextEmpty()) && (InstructionsDisplayOption.POPUP == item
								.getItemInstructionsObj().getInstructionsDisplayOption())))) {
			formBean.setInstructionsJavascript(item.renderInstructionsJavascript(appSessionBean.getSiteLoginID())); // CAP-36790
		}
	}

	protected void loadUIPageJavascript(UserInterface ui, Page page, CustDocUIFormBean formBean,
			AppSessionBean appSessionBean) throws AtWinXSException {
		formBean.setVariablesSection(ui.replaceIcons(
				page.renderDisplayC1UX(appSessionBean, ui.getAllVariables(), ui.isFieldsEditable(), ui).toString())); // CAP-32869
		formBean.setShowHideJavascript(page.loadShowHideJavascript().toString());
		formBean.setShowHideAltProfSelJS(page.loadShowHideAltProfSelJS().toString()); // CAP-3377
		formBean.setPageValidation(page.getPageValidation());
		formBean.setSkipProofJS(page.loadSkipProofJavascript(ui, page.getPageNumber()).toString()); // CAP-11103
	}

	protected void setFieldsNoLogic(CustDocUIFormBean formBean, UserInterface ui, Page page,
			AppSessionBean appSessionBean) {
		formBean.setDatePattern(getLocaleDateFormat(appSessionBean)); // CP-10750
		formBean.setHdnFillKeyValue(false); // reset this after external source call done
		// additional text
		formBean.setPageTitle(page.getStepName());
		// CAP-25569
		formBean.setHdnUIStepPageNumber(Integer.toString(page.getPageNumber()));
		formBean.setNewRequest(AtWinXSConstant.EMPTY_STRING + ui.isNewRequest());
		formBean.setOrderQuestions(AtWinXSConstant.EMPTY_STRING + ui.isOrderQuestions());
		formBean.setVariablePage(AtWinXSConstant.EMPTY_STRING + ui.isVariablePage());
		formBean.setWorkingProof(AtWinXSConstant.EMPTY_STRING + ui.isWorkingProof());
		formBean.setExpandTooltip(ui.getExpandTooltip());
		formBean.setCollapseTooltip(ui.getCollapseTooltip());
		formBean.setAllKeyVariablesPopulated(Boolean.toString(ui.areAllKeyVariablesPopulated()));
		// CAP-29897
		formBean.setSaveSucceeded(getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_SAVE_SUCCEEDED_MSG,
				"Saved successfully."));
		formBean.setSaveFailed(
				getTranslation(appSessionBean, TranslationTextConstants.TRANS_NM_SAVE_FAILED_MSG, "Save failed."));

		formBean.setHdnIsDirty(page.isStatusSaved()); // CP-11273
	}

	protected void setBlankFields(CustDocUIFormBean formBean) {
		formBean.setPopupHTML(AtWinXSConstant.EMPTY_STRING);
		formBean.setProfileLabel(AtWinXSConstant.EMPTY_STRING); // (ui.getImprintHistorySelector() != null) ?
																// Util.htmlEncodeQuotes(ui.getImprintHistorySelector().getProfileIDLabel())
																// : "Profile ID"); // CP-12638
		formBean.setInstanceNameLabel(AtWinXSConstant.EMPTY_STRING);
		formBean.setDragDropJS(AtWinXSConstant.EMPTY_STRING);
		formBean.setLoadSaveAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setLoadWorkingProofAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setDownloadInsertUploadAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setLoadAemImagesAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setLoadBrandfolderImagesAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setUploadAemImagesAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setUploadBrandfolderImagesAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setSaveImageAjaxUrl(AtWinXSConstant.EMPTY_STRING);
		formBean.setDeleteTempImageUrl(AtWinXSConstant.EMPTY_STRING);
	}

	protected boolean getAllowFailedProofs() throws AtWinXSException {
		// CAP-32686
		XSProperties prop = PropertyUtil.getProperties("order_entry");
		return Util.yToBool(prop.getProperty("allowFailedProofs"));
	}

	protected C1UXDisplayControl createC1UXDisplayControl(DisplayControl dc)
			throws IllegalAccessException, InvocationTargetException {
		C1UXDisplayControl control = new C1UXDisplayControl();
		BeanUtils.copyProperties(control, dc);
		control.setDisplayVariableInputTypeCode(dc.getDisplayVariable().getInputTypeCode().toString());
		control.setDisplayVariableListValue(dc.getDisplayVariable().getListValue());
		control.setDisplayVariableTextValue(dc.getDisplayVariable().getTextValue());
		return control;
	}

	protected C1UXDisplayControls createC1UXDisplayControls(DisplayControls dc)
			throws IllegalAccessException, InvocationTargetException {
		C1UXDisplayControls controls = new C1UXDisplayControls();
		BeanUtils.copyProperties(controls, dc);
		HashMap<Integer, List<C1UXDisplayControl>> list = new HashMap<>();
		Set<Map.Entry<Variable, List<DisplayControl>>> set = dc.getDisplayControls().entrySet();
		List<DisplayControl> existingList = null;
		for (Map.Entry<Variable, List<DisplayControl>> entry : set) {
			List<C1UXDisplayControl> controlList = new ArrayList<>();
			existingList = entry.getValue();
			if (!existingList.isEmpty()) {
				for (DisplayControl c : existingList) {
					controlList.add(createC1UXDisplayControl(c));
				}
			}
			list.put(entry.getKey().getNumber(), controlList);
		}

		controls.setDisplayControlsMap(list);
		controls.setDisplayCriteriaMetAsOfLoad(dc.isDisplayCriteriaMet());
		return controls;
	}

	protected C1UXVariableBean createC1UXVariableBean(Variable cdvar, Map<Integer, Map<String, String>> varActionsMap,
			AppSessionBean asb, Group grp, C1UXCustDocGroupBean groupBean) throws IllegalAccessException, InvocationTargetException {
		C1UXVariableBean bean = new C1UXVariableBean();
		BeanUtils.copyProperties(bean, cdvar);
		if ((cdvar.getDependentVarPages() != null) && (!cdvar.getDependentVarPages().isEmpty())) {
			bean.setDependentVarPages(cdvar.getDependentVarPages().toArray(new Integer[0]));
		}
		setVariableUncopiableValues(bean, cdvar);
		setVariableShowHide(bean, cdvar, grp);
		setUpSpecialVariableCases(bean, cdvar, asb, grp); // CAP-44304
		setVarFieldIDsAndNames(bean, cdvar);
		setVariableGridAssignment(grp, groupBean, bean); // CAP-46336
		bean.setDivID("Var" + cdvar.getNumber() + "Div");
		bean.setHiddenShowHideFieldID("showVar" + cdvar.getNumber());
		bean.setEventActionJavascript(varActionsMap.get(cdvar.getNumber())); // CAP-42224
		return bean;
	}

	protected void setVariableUncopiableValues(C1UXVariableBean bean, Variable cdvar) {
		bean.setInputTypeCode(cdvar.getInputTypeCode().toString());
		bean.setInstructionText(Util.htmlUnencodeQuotes(cdvar.getInstructionsText()));
		bean.setVariablePageflexTypeCode(cdvar.getVariablePageflexType().toString());
		bean.setDefaultValue(cdvar.getDefault());
		bean.setTextBoxSize(cdvar.getTextAreaColumns());
		bean.setInstructionTypeCode(cdvar.getInstructionsTypeCode().toString());
		bean.setShowThumbnailLabels(cdvar.isShowImageLabels()); // CAP-43031
	}

	// CAP-44304 - special variable cases are getting large
	protected void setUpSpecialVariableCases(C1UXVariableBean bean, Variable cdvar, AppSessionBean asb, Group grp) {
		setCheckboxVariableInfo(bean, cdvar); // CAP-43528
		setCalendarVariableInfo(bean, cdvar, asb.getDefaultLocale()); // CAP-43660
		setFormattedParagraphHandling(bean, cdvar); // CAP-45488
		setUpBlankedVariables(bean, cdvar, grp);
		setUploadVariableInfo(bean, cdvar, asb); // CAP-44780
	}

	// CAP-45488 - adjust handling for formatted paragraphs for Angular
	public FormattedParagraphProcessor getFormattedParagraphProcessor()
	{
		return this.objectMapFactoryService.getEntityObjectMap().getEntity(FormattedParagraphProcessor.class, AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
	}

	public String getOEParagraphValue(String original, String style) throws AtWinXSException {
		return OrderEntryUtil.getParagraphValue(original, style);
	}

	protected void setFormattedParagraphHandling(C1UXVariableBean bean, Variable cdvar)  {
		if (cdvar.getVariablePageflexType() == PageflexType.FORMATTED_PARAGRAPH) {
			try {
				String textValue = cdvar.getTextValue();
				if (cdvar.isUseNewFormatting()) { // strip out normal pageflex xml
					FormattedParagraphProcessor fpp = getFormattedParagraphProcessor();
					fpp.populateFromXml(textValue, cdvar.isWordProcess(), cdvar.getCustomStyle(), (FormattedParagraphVariable) cdvar);
					textValue = fpp.getHtmlText();
				} else if (cdvar.isWordProcess()) { // strip out encoded pageflex xml
					textValue = getOEParagraphValue(textValue, cdvar.getCustomStyle());
				}
				bean.setTextValue(textValue);
			} catch(AtWinXSException e) {
				logger.error(e.getMessage());
			}
		}
	}

	// CAP-44780 - upload information
	protected void setUploadVariableInfo(C1UXVariableBean bean, Variable cdvar, AppSessionBean asb) {
		if ((cdvar.isUploadAllowedInd()) || (Variable.PageflexType.FILE_UPLOAD == cdvar.getVariablePageflexType())
				|| (Variable.InputType.UPLOAD_ONLY == cdvar.getInputTypeCode())) {
			bean.setUploadAllowedInd(true);
			setTypeSpecificValues(bean, cdvar, asb);
			setUploadListOptions(bean, cdvar);
		}
		bean.setImageSearchAllowed(false); // out of scope as of Q4 2023
	}

	protected void setUploadListOptions(C1UXVariableBean bean, Variable cdvar) {
		Collection<C1UXUIListOption> selectedUploadedListValues = new ArrayList<>();
		bean.setUploadListOptionName("varList" + cdvar.getName() + INVALID_STRING);
		if (!Util.isBlankOrNull(cdvar.getListValue())) {
			// CAP-44837 - we will need multiple list option names for the else for this
			if ((Variable.PageflexType.INSERTION_GROUP != cdvar.getVariablePageflexType()) || (cdvar.getGroupMaxInserts() == 1)) {
				C1UXUIListOption listOption = buildSingleUploadedListOption(cdvar);
				if (listOption != null) {
					selectedUploadedListValues.add(listOption);
				}
			} else if (Variable.PageflexType.INSERTION_GROUP == cdvar.getVariablePageflexType()) {
				// multiple select inserts
				buildMultiSelectUploads(cdvar, selectedUploadedListValues);
			}
		}
		bean.setSelectedUploadedListValues(selectedUploadedListValues);
	}

	protected void buildMultiSelectUploads(Variable cdvar, Collection<C1UXUIListOption> selectedUploadedListValues) {
		List<C1UXUIListOption> listOptions = buildMultipleUploadedListOptions(cdvar.getTextValue());
		if ((listOptions != null) && (!listOptions.isEmpty())) {
			for (C1UXUIListOption option: listOptions) {
				selectedUploadedListValues.add(option);
			}
		}
	}

	// CAP-46522
	protected List<C1UXUIListOption> buildMultipleUploadedListOptions(String listValue) {
		List<C1UXUIListOption> options = new ArrayList<>();
		StringTokenizer tokens = new StringTokenizer(listValue, "|");
		String currentValue = null;
		C1UXUIListOption option = null;
		// return the values as a collection here
		while (tokens.hasMoreTokens())
		{
			currentValue = tokens.nextToken();
			if (currentValue.startsWith("U"))
			{
				option = new C1UXUIListOption();
				if (!currentValue.contains(OrderEntryConstants.SEPARATOR_STRING_ATOM)) {
					// if the file name isn't there, we need to get it
					currentValue = lookupUploadValueByID(currentValue);
				}
				String label = currentValue.substring(currentValue.indexOf(OrderEntryConstants.SEPARATOR_STRING_ATOM) +1);
				String listID = currentValue.substring(0, currentValue.indexOf(OrderEntryConstants.SEPARATOR_STRING_ATOM));
				option.setProofValue(AtWinXSConstant.EMPTY_STRING);
				option.setPlantValue(currentValue);
				option.setListId(listID);
				option.setHtmlValue(currentValue);
				option.setHtmlLabel(label);
				option.setUpload(true);
				option.setSelected(true);
				option.setTextValue(currentValue);
				options.add(option);
			}
		}
		return options;
	}

	protected String lookupUploadValueByID(String currentValue) {
		try
		{
			ICustomDocsUpload server = CustomDocsAdminLocator.locateCustomDocsUpdateComponent(AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
			int fileID = Integer.parseInt(currentValue.substring(1));
			CDUploadVO uploadItem = server.getUpload(fileID);
			return currentValue + OrderEntryConstants.SEPARATOR_STRING_ATOM + OrderEntryConstants.IDENTIFIER_UPLOAD + uploadItem.getFileNameOriginal();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return currentValue + OrderEntryConstants.SEPARATOR_STRING_ATOM + OrderEntryConstants.IDENTIFIER_UPLOAD + OrderAdminConstants.NOT_AVAILABLE;
		}
	}

	protected C1UXUIListOption buildSingleUploadedListOption(Variable cdvar) {
		if ((!Util.isBlankOrNull(cdvar.getListValue())) && isVarValueUploadOrSearch(cdvar.getListValue())) {
			String listValue = cdvar.getListValue();
			C1UXUIListOption option = new C1UXUIListOption();
			String label = listValue.substring(listValue.indexOf(OrderEntryConstants.SEPARATOR_STRING_ATOM) +1, listValue.length());
			String listID = listValue.substring(0, listValue.indexOf(OrderEntryConstants.SEPARATOR_STRING_ATOM));
			option.setProofValue(cdvar.getProofValue());
			option.setPlantValue(cdvar.getTextValue());
			option.setListId(listID);
			option.setHtmlValue(listValue);
			option.setHtmlLabel(label);
			option.setUpload(true);
			option.setSelected(true);
			option.setTextValue(listValue);
			return option;
		}
		return null;
	}

	protected void setTypeSpecificValues(C1UXVariableBean bean, Variable cdvar, AppSessionBean asb) {
		if (Variable.PageflexType.INSERTION_GROUP.equals(cdvar.getVariablePageflexType())) {
			bean.setUploadFileFormatsCode(UPLOAD_FILE_FORMATS_INSERTS);
			bean.setMinFiles(cdvar.getGroupMinInserts());
			bean.setMaxFiles(cdvar.getGroupMaxInserts());
		}
		else {
			bean.setMinFiles(1);
			bean.setMaxFiles(1);
			if ((!handleNewRequests(bean, cdvar, asb)) && (Variable.PageflexType.IMAGE.equals(cdvar.getVariablePageflexType()))) {
				if ((cdvar.isAllowEPSImg()) || checkIfBUAllowsEPS(asb)) {
					bean.setUploadFileFormatsCode(cdvar.isAllowSTDImg() ? UPLOAD_FILE_FORMATS_ALL_IMAGES : UPLOAD_FILE_FORMATS_IMAGES_EPS_ONLY);
				} else {
					bean.setUploadFileFormatsCode(UPLOAD_FILE_FORMATS_IMAGES_STANDARD_ONLY);
				}
				
				// CAP-46487 - change image cropper to true if it meets criteria
				if(cdvar.getCropImageWidth() > 0 && cdvar.getCropImageHeight() > 0){
					bean.setImageCropperAllowed(true);
				}
				
			} else if (Variable.PageflexType.HOSTED_RESOURCE.equals(cdvar.getVariablePageflexType())) {
				bean.setUploadFileFormatsCode(UPLOAD_FILE_FORMATS_HOSTED_RESOURCE);
			}
		}
	}

	protected String getNewRequestLink(Variable cdvar, AppSessionBean asb) {
		if ((!Util.isBlankOrNull(cdvar.getListValue())) && isVarValueUploadOrSearch(cdvar.getListValue())) {
			UploadFile uploadFile = getUploadFileObject(cdvar.getVariablePageflexType(), cdvar, asb);
			String sFileId = Util.head(cdvar.getListValue(), OrderEntryConstants.SEPARATOR_STRING_ATOM);
			sFileId = sFileId.substring(1);
			int fileId = Util.safeStringToInt(sFileId);
			return uploadFile.generateGetAFileLinkURLC1UX(fileId);
		}
		return AtWinXSConstant.EMPTY_STRING;
	}

	protected boolean handleNewRequests(C1UXVariableBean bean, Variable cdvar, AppSessionBean asb) {
		if ((Variable.PageflexType.FILE_UPLOAD.equals(cdvar.getVariablePageflexType()))) {
			FileUploadVariable fuvar = (FileUploadVariable) cdvar;

			if (fuvar.isNewRequestFlow()) {
				bean.setAlternateDownloadLinkURL(getNewRequestLink(cdvar, asb));

				if (OrderEntryUtil.allowZipUploadsInNewRequests(asb.getSiteID())) {
					bean.setUploadFileFormatsCode(UPLOAD_FILE_FORMATS_NEW_REQUEST_ZIP);
				} else {
					bean.setUploadFileFormatsCode(UPLOAD_FILE_FORMATS_NEW_REQUEST);
				}
			} else {
				bean.setUploadFileFormatsCode(UPLOAD_FILE_FORMATS_FILE_UPLOAD);
			}
			return true;
		}
		return false;
	}

	// CAP-44304 - variables blanked need to be set to their defaults
	protected void setUpBlankedVariables(C1UXVariableBean bean, Variable cdvar, Group grp) {
		// if the variable is currently hidden and is blanked by show hide, we have to
		// send the default to the FE to be displayed
		if ((!grp.isShown() || N.equals(bean.getHiddenShowHideFieldValue())) && cdvar.isBlankedByShowHide()) {
			// note that I never need to worry about this being a checkbox as that input
			// type is NEVER set to blanked by show hide
			bean.setTextValue(
					Util.isBlankOrNull(cdvar.getDefaultTextValue()) ? cdvar.getDefault() : cdvar.getDefaultTextValue());
			bean.setListValue(cdvar.getDefaultListValue());
		}
	}

	// CAP-43528 - initialize only for checkboxes
	protected void setCheckboxVariableInfo(C1UXVariableBean bean, Variable cdvar) {
		if (cdvar.getInputTypeCode() == InputType.CHECKBOX) {
			CheckboxRendererInput varInput = CheckboxRendererImpl.getCheckboxRendererInput((RendererInput) cdvar);
			bean.setCurrentlyChecked(varInput.isChecked());
			bean.setInputRequired(false); // CAP-43827
			// CAP-46513 - inserts need the values pulled from different places
			if ((Variable.PageflexType.INSERTION_GROUP == cdvar.getVariablePageflexType())
					&& (cdvar instanceof InsertionGroupVariableImpl)) {
				InsertionGroupVariableImpl igVar = (InsertionGroupVariableImpl) cdvar;
				bean.setDefaultValue(String.valueOf(igVar.getCheckedDefaultInsertID()));
				bean.setUncheckedValue(String.valueOf(igVar.getUncheckedDefaultInsertID()));
			}
		}
	}

	// CAP-43660 - initialize only for calendar date
	protected void setCalendarVariableInfo(C1UXVariableBean bean, Variable cdvar, Locale locale) {
		if (cdvar.getInputTypeCode() == InputType.CALENDAR_DATE) {
			String defaultMaskFormat = Util.getDateFormatForLocale(locale);
			String maskFormat = defaultMaskFormat;
			try {
				CDMaskVO vo = getMask(cdvar);
				if (vo != null) {
					String maskName = vo.getMaskName();
					if ((maskName != null) && (maskName.indexOf("Date") > -1)) {
						maskFormat = maskName.substring(maskName.indexOf("Date") + 5, maskName.length());
					}
				}
			} catch (AtWinXSException e) {
				logger.error(e.toString());
			}
			maskFormat = convertDateFormatForPrimeNG(maskFormat, defaultMaskFormat);
			bean.setDateFormat(maskFormat);
		}
	}

	protected String convertDateFormatForPrimeNG(String maskFormat, String defaultMaskFormat) {
		// CAP-31527 - make 9's masks apply locale
		String userDateDefault = defaultMaskFormat.toLowerCase();
		if (maskFormat.equals("99-99-9999")) {
			if (userDateDefault.indexOf("m") < userDateDefault.indexOf("d")) {
				maskFormat = "mm-dd-yy";
			} else {
				maskFormat = "dd-mm-yy";
			}
		} else if (maskFormat.equals("99/99/9999")) {
			if (userDateDefault.indexOf("m") < userDateDefault.indexOf("d")) {
				maskFormat = "mm/dd/yy";
			} else {
				maskFormat = "dd/mm/yy";
			}
		} else {
			maskFormat = maskFormat.toLowerCase();
			maskFormat = maskFormat.replace("yy", "y"); // reduce number of year digits from mask
		}
		return maskFormat;
	}

	protected IOECustomDocumentComponent getOECustDocComponent(CustomizationToken token) throws AtWinXSException {
		return OECustomDocLocator.locate(token != null ? token : AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
	}

	protected CDMaskVO getMask(Variable cdvar) throws AtWinXSException {
		IOECustomDocumentComponent custDocServer = getOECustDocComponent(null); // this retrieval is simple and should
																				// not need to be customized
		return custDocServer.getMask(new CDMaskVOKey(cdvar.getMaskNumber()));
	}

	protected void setVarFieldIDsAndNames(C1UXVariableBean bean, Variable cdvar) {
		// ID that must be used on the div for the variable for show/hide to work
		// correctly = 'Var' + varNum + 'Div'", type = "string")
		bean.setDivID("Var" + cdvar.getNumber() + "Div");
		// ID and name that must be used on the hidden input for the variable for
		// show/hide to work correctly on the back end during save = 'showVar' +
		// varNum", type = "string")
		bean.setHiddenShowHideFieldID("showVar" + cdvar.getNumber());
		Renderer renderer = cdvar.getRenderer();
		// ID and name that must be used on the input for a text field variable for
		// javascript to work correctly = 'varVal' + varName", type = "string")
		bean.setFormInputName(renderer.getFormInputName(cdvar.getName()));
		// ID and name that must be used on the input for a list input variable for
		// javascript to work correctly = 'varList' + varName", type = "string")
		bean.setFormListInputName(renderer.getFormListInputName(cdvar.getName()));
		// ID and name that must be used on the hidden input for every variable for
		// javascript to work correctly and save to work correctly = 'varHdn' +
		// varName", type = "string")
		bean.setFormInputHiddenName(renderer.getFormInputHiddenName(cdvar.getName()));
		if (renderer instanceof BaseSelectionListRendererImpl) { // CAP-46519
			// selection lists specifically use the number instead of the name
			bean.setVariableErrorDivName("selectList_error_" + cdvar.getNumber());
			bean.setVariableErrorTextDivName("selectList_error_text_" + cdvar.getNumber());
		} else {
			// ID and name that must be used on the div that shows under a field when the
			// field has an error at the variable level for javascript to work correctly =
			// 'err' + varName", type = "string")
			bean.setVariableErrorDivName(renderer.getVariableErrorDivName(cdvar.getName()));
			// ID and name that must be used on the div that will contain the error text
			// within the error div for when the field has an error at the variable level
			// for javascript to work correctly = 'errText' + varName", type = "string")
			bean.setVariableErrorTextDivName("errText" + cdvar.getName());
		}
		// ID and name that must be used on the hidden input for every KEY variable for
		// javascript to work correctly and save to work correctly = 'varValHdnKeyValue'
		// + varName", type = "string")
		if (cdvar.isKeyValueInd()) {
			bean.setKeyInputName("varValHdnKeyValue" + cdvar.getName());
		}
	}

	protected C1UXCustDocGroupBean createC1UXGroupBean(Group grp, Map<Integer, Map<String, String>> varActionsMap,
			C1UXCustDocPageBean pageBean, AppSessionBean asb) throws IllegalAccessException, InvocationTargetException {
		C1UXCustDocGroupBean groupBean = new C1UXCustDocGroupBean();
		BeanUtils.copyProperties(groupBean, grp);
		boolean groupShown = grp.getDisplayControls().isDisplayCriteriaMet();
		groupBean.setHiddenShowHideFieldValue(groupShown ? Y : N);
		setGridInformation(grp, groupBean);
		if ((grp.getVariables() != null) && (!grp.getVariables().isEmpty())) {
			List<C1UXVariableBean> vars = groupBean.getC1uxVariables();
			Collection<Variable> originalVars = grp.getVariables();
			C1UXVariableBean varBean = null;
			for (Variable cdvar : originalVars) {
				varBean = createC1UXVariableBean(cdvar, varActionsMap, asb, grp, groupBean);
				setVariableListReferences(varBean, cdvar, pageBean);
				vars.add(varBean);
			}
		}
		groupBean.setGroupDisplayControls(createC1UXDisplayControls(grp.getDisplayControls()));
		groupBean.setDivID(OrderEntryConstants.CD_GROUP_DIV_ID + grp.getGroupNumber());
		groupBean.setHiddenShowHideFieldID("showGroup" + grp.getGroupNumber());
		return groupBean;
	}

	protected void setGridInformation(Group grp, C1UXCustDocGroupBean groupBean) {
		if ((grp.isGrid()) && (grp instanceof GridGroup)) {
			GridGroup grid = (GridGroup) grp;
			groupBean.setRows(grid.getRowsC1UX());
			groupBean.setShowRowHeaders(grid.isAnyRowHeaders());
			groupBean.setShowGridAddRowButton(grid.isAnyRowsHide() && !grid.isGroupGridNeverAddRowsInd());
			groupBean.setColumns(grid.getColumnsC1UX());
			groupBean.setEmptyCellAssignments(buildEmptyCells(grid, groupBean));
		}
	}

	protected C1UXCustDocGridAssignment getAssignment(GridGroup grid, int row, int col) {
		C1UXCustDocGridAssignment asn = new C1UXCustDocGridAssignment();
		asn.setColumn(col);
		asn.setRow(row);
		asn.setColumnSpan(grid.getColspanForCellC1UX(row, col));
		asn.setGridCellId("td" + grid.getGroupNumber() + "Col" + col + "Row" + row);
		return asn;
	}

	protected List<C1UXCustDocGridAssignment> buildEmptyCells(GridGroup grid, C1UXCustDocGroupBean groupBean) {
		Map<Integer, List<Integer>> cellMap = grid.getEmptyVariablesC1UX();
		List<C1UXCustDocGridAssignment> cells = new ArrayList<>();
		if ((cellMap != null) && (!cellMap.isEmpty())) {
			for (C1UXCustDocGridRow row : groupBean.getRows()) {
				if (cellMap.containsKey(row.getNumber())) {
					int rowNum = row.getNumber();
					List<Integer> spots = cellMap.get(rowNum);
					for (Integer col: spots) {
						cells.add(getAssignment(grid, rowNum, col.intValue()));
					}
				}
			}
		}
		return cells;
	}

	protected void setVariableGridAssignment(Group grp, C1UXCustDocGroupBean groupBean, C1UXVariableBean varBean) {
		if ((groupBean.getRows() != null) && (!groupBean.getRows().isEmpty())) {
			for (C1UXCustDocGridRow row : groupBean.getRows()) {
				for (int i = 0; i < groupBean.getColumns().size(); i++) {
					if (varBean.getNumber() == row.getVarNumbers()[i])
					{
						varBean.setGridAssignment(getAssignment((GridGroup)grp, row.getNumber(), i+1));
					}
				}
			}
		}
	}

	protected void setVariableListReferences(C1UXVariableBean varBean, Variable cdvar, C1UXCustDocPageBean pageBean) {
		// listTypeCode_listDataType_listId is the key format
		if (cdvar.getUiList() != null) {
			UIList list = cdvar.getUiList();
			String key = createListKey(cdvar, list); // CAP-45488
			if (!pageBean.getUiListMap().containsKey(key)) {
				pageBean.getUiListMap().put(key, createC1UXListBean(list));
			}
			varBean.setListKey(key);
		}
	}

	// CAP-45488 - improve handling for lists with no ID
	private String createListKey(Variable cdvar, UIList list) {
		String key = list.getListType().toString() + RouteConstants.UNDERSCORE + list.getDataType()	+ RouteConstants.UNDERSCORE;
		// since the code for external source in CP is literally blank, make it start with X
		if (list.getListType() == UIList.ListType.EXTERNAL_SOURCE) {
			key = "X" + key;
		}

		if (list.getListId() > 0) {
			key += list.getListId();
		} else {
			key += cdvar.getNumber();
		}
		return key;
	}

	protected C1UXUIList createC1UXListBean(UIList list) {
		C1UXUIList listBean = new C1UXUIList();
		listBean.setDataTypeCode(list.getDataType().toString());
		listBean.setListId(String.valueOf(list.getListId()));
		listBean.setListTypeCode(list.getListType().toString());
		for (UIListOption option : list.getOptions()) {
			listBean.getListOptions().add(createC1UXListOption(option));
		}
		return listBean;
	}

	protected C1UXUIListOption createC1UXListOption(UIListOption option) {
		C1UXUIListOption opt = new C1UXUIListOption();
		try {
			BeanUtils.copyProperties(opt, option);
		} catch (Exception e) {
			logger.error("failed to copy this ui list option", e);
		}
		return opt;
	}

	protected void setVariableShowHide(C1UXVariableBean varBean, Variable cdvar, Group grp) {
		varBean.setHiddenShowHideFieldValue(((grp.getVariableDisplayControls().containsKey(cdvar.getNumber()))
				&& (!grp.getVariableDisplayControls().get(cdvar.getNumber()).isDisplayCriteriaMet())) ? N : Y);
	}

	@Override
	public C1UXCustDocBaseResponse initializeUIOnly(SessionContainer sc, Map<String, String> params)
			throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		Collection<String> errors = new ArrayList<>();
		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		try {
			CustDocUIFormBean formBean = new CustDocUIFormBean();
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
			PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();

			initializeItem(params, formBean, oeSessionBean, appSessionBean, volatileSessionBean, punchoutSessionBean,
					errors);
			if ((!formBean.isHardStopFailure()) && (!formBean.isInitializeFailure())) {
				saveFullOESessionInfo(oeSession, appSessionBean.getSessionID());
				sfBean.setMessage(getCombinedMessage(errors));

				// CAP-46524
				storeErrorForLater(sfBean, sc);

				sfBean.setSuccess(true);
			} else {
				sfBean.setSuccess(false);
				sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
						TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
			}
		} catch (Exception e) {
			String error = "Failed in initializeItem for requestParms " + params.toString() + " with error "
					+ e.toString();
			logger.error(error, e);
			failResponse(sfBean, appSessionBean, true, null);
		}

		return createBaseResponseFromUIBean(sfBean);
	}

	protected C1UXCustDocBaseResponse createBaseResponseFromUIBean(C1UXCustDocPageBean sfBean) {
		C1UXCustDocBaseResponse bean = new C1UXCustDocBaseResponse();
		bean.setSuccess(sfBean.isSuccess());
		bean.setMessage(sfBean.getMessage());
		bean.setFieldMessages(sfBean.getFieldMessages());
		bean.setRedirectRouting(sfBean.getRedirectRouting());// CAP-42843
		return bean;
	}

	@Override
	public C1UXCustDocBaseResponse performPageSubmitAction(SessionContainer sc, Map<String, String> uiRequest)
			throws AtWinXSException {
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();

		try {
			// now be sure to populate the formBean from the item
			item.populateFormBean(formBean, appSessionBean.getDefaultLocale());

			handleSpecificEvent(uiRequest, sc, formBean, item, sfBean);

			saveAllUpdatedSessions(oeSession, sc.getApplicationVolatileSession(), sc.getApplicationSession(), appSessionBean.getSessionID()); // CAP-44490
		} catch (AtWinXSException e) {
			logger.error(failedAtWinXSExceptionPrefix, e);
			sfBean.setSuccess(false);
			sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		} catch (Exception e) {
			logger.error(failedNonAtWinXSExceptionPrefix, e);
			sfBean.setSuccess(false);
			sfBean.setHardStopFailure(true);
			sfBean.setInitializeFailure(true);
			sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		}

		if (eventSendsBaseResponseOrRouting(uiRequest, sfBean)) {
			return createBaseResponseFromUIBean(sfBean);
		} else {
			convertToC1UXObject(sc, item, formBean, sfBean);
		}

		return sfBean;
	}

	//CAP-46524 - added REFRESH
	protected boolean isSaveAndDoEvent(UserInterface.UIEvent direction){
		return (direction == UserInterface.UIEvent.SAVE_AND_CONTINUE || direction == UserInterface.UIEvent.SAVE_AND_SEARCH || direction == UserInterface.UIEvent.REFRESH);
	}

	protected boolean eventSendsBaseResponseOrRouting(Map<String, String> uiRequest, C1UXCustDocPageBean sfBean) {
		if (!Util.isBlankOrNull(sfBean.getRedirectRouting())) {
			return true;
		}

		// navigation doesn't work the same way in angular, just return the base
		// response for next/prev/nav
		UserInterface.UIEvent direction = lookupEvent(uiRequest.get(EVENT_ACTION_PARAM));

		// CAP-46524 - added REFRESH
		return ((direction == UserInterface.UIEvent.CANCEL) || (direction == UserInterface.UIEvent.CANCEL_ORDER)
				|| (direction == UserInterface.UIEvent.SAVE_AND_EXIT) || (direction == UserInterface.UIEvent.SAVE_AND_SEARCH) // CAP-44549
				|| (direction == UserInterface.UIEvent.PREVIOUS_CHECKOUT) // cP-10840
				|| (direction == UserInterface.UIEvent.SAVE_ORDER) || (direction == UserInterface.UIEvent.NEXT)
				|| (direction == UserInterface.UIEvent.PREVIOUS)
				 || (direction == UserInterface.UIEvent.SAVE_AND_CONTINUE)  || (direction == UserInterface.UIEvent.WORKING_PROOF)
				|| (direction == UserInterface.UIEvent.NAVIGATE_PAGES)
				|| (direction == UserInterface.UIEvent.REFRESH));
	}

	protected void handleSpecificEvent(Map<String, String> uiRequest, SessionContainer sc, CustDocUIFormBean formBean,CustomDocumentItem item, C1UXCustDocPageBean sfBean) throws AtWinXSException {
		UserInterface ui = item.getUserInterface();
		Collection<String> errorStrings = new ArrayList<>();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession orderSession = (OrderEntrySession) sc.getModuleSession();
		updateCPFormBean(formBean, uiRequest);
		UserInterface.UIEvent direction = lookupEvent(uiRequest.get(EVENT_ACTION_PARAM));

		if ((!handleLeavingEvents(direction, item, sc, sfBean, uiRequest, errorStrings, formBean))
				&& (!handleUnsupportedEvents(direction))) {
			// CAP-46524
			if (isSaveAndDoEvent(direction)) {
				setDirtyFlag(uiRequest);
				ui.readPageC1UX(uiRequest, errorStrings, false, direction, false);
				int currentPage = ui.getCurrentPageC1UX(uiRequest);
				if (direction == UIEvent.SAVE_AND_CONTINUE) {
					saveIncompleteOrderLine(item, errorStrings, sc, currentPage, sfBean);
					saveAllUpdatedSessions(orderSession, sc.getApplicationVolatileSession(), sc.getApplicationSession(),appSessionBean.getSessionID()); // CAP-44307
				}
				// CAP-26025/CAP-26026 - add new save and search event
				else if (direction == UserInterface.UIEvent.SAVE_AND_SEARCH) { // values currently on the page are saved and search
					ui.setNextPageNumber(currentPage);
					// CAP-44549
					ui.doVariableLevelExternalSearchC1UX(uiRequest, errorStrings);
				}
				else if(direction == UserInterface.UIEvent.REFRESH) {
					// CAP-46524
					ui.refreshDynamicLists(errorStrings);
				}

				combineErrors(errorStrings, sfBean);
				if (direction == UIEvent.REFRESH) {
					storeErrorForLater(sfBean, sc);
				}
				checkForSuccessBase(sfBean);
			}
			else { // this should be next, navigate pages, or previous only
				navigateForward(uiRequest, formBean, errorStrings, direction, item, sfBean, sc);
			}
		}
	}

	// CAP-48464 - refactoring existing logic out to allow for override
	protected void saveIncompleteOrderLine(CustomDocumentItem item, Collection<String> errorStrings, SessionContainer sc, int currentPage, C1UXCustDocBaseResponse sfBean) throws AtWinXSException {
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession orderSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = orderSession.getOESessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		errorStrings.clear(); // CP will have returned errors, but we cannot show them.
		OrderLineVOKey cartLineKey = item.saveOrderLine(appSessionBean, volatileSessionBean, errorStrings, true, oeSessionBean, punchoutSessionBean, currentPage, "", ""); // CP-13598/CAP-37
		if ((cartLineKey != null) && (cartLineKey.getLineNum() > 0) && (errorStrings.isEmpty()))
		{
			sfBean.setSuccess(true);
		}
	}

	// CAP-46524
	protected void storeErrorForLater(C1UXCustDocPageBean sfBean, SessionContainer sc) {
		// if there is an error in sfBean, strip out the error and store it in OrderEntrySession like in this block below. Make sure to save the session.
		// Make sure that the error is removed from sfBean so that the bean will return success.
		if (!Util.isBlankOrNull(sfBean.getMessage())) {
			OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			Message msg = new Message();
			msg.setErrGeneralMsg(sfBean.getMessage());
			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();

			oeSessionBean.setUsabilityRedirectErrorMessage(msg);
			try {
				saveFullOESessionInfo(oeSession, appSessionBean.getSessionID()); //save the session
			}
			catch(AtWinXSException ae) {
				logger.error("Failed in storeErrorForLater " + ae.getMessage(), ae);
			}
		}
	}

	// CAP-46524
	protected void retrieveStoredError(SessionContainer sc, C1UXCustDocPageBean sfBean) {
		// if there is an error in OrderEntrySession in that UsabilityRedirectErrorMessage() section, then pull it out and store it in the Message field of sfBean.
		// Make sure that it doesn’t wipe out any existing errors. If one is already there, then the error from the redirect should append to that it.
		// Then wipe the redirect error from session and save the session.
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		if(oeSessionBean.getUsabilityRedirectErrorMessage() != null) {
			Message msg = oeSessionBean.getUsabilityRedirectErrorMessage();
			String exisatingMsg = sfBean.getMessage();

			if(Util.isBlankOrNull(exisatingMsg)){
				sfBean.setMessage(msg.getErrGeneralMsg());
			}
			else {
				sfBean.setMessage(exisatingMsg + " " + msg.getErrGeneralMsg());
			}

			oeSessionBean.setUsabilityRedirectErrorMessage(null);
			try {
				saveFullOESessionInfo(oeSession, appSessionBean.getSessionID()); //save the session
			}
			catch(AtWinXSException ae) {
				logger.error("Failed in storeErrorForLater " + ae.getMessage(), ae);
			}
		}

	}

	// all those hidden fields need to actually be populated the way spring would automatically
	protected void updateCPFormBean(CustDocUIFormBean formBean, Map<String, String> uiRequest) {
		formBean.setHdnIsDirty(valueIsTrue(uiRequest.get(OrderEntryConstants.DATA_IS_DIRTY)));
	}

	public boolean valueIsTrue(String value)
	{
		return ((value != null) && ("true".equalsIgnoreCase(value)));
	}

	protected boolean handleUnsupportedEvents(UIEvent direction) {
		// CAP-46524 - removed REFRESH from unsupported events
		return ((direction == UserInterface.UIEvent.APPLY_COLOR_CHANGE) // applying color to all of UI from														// font/color set
				|| (direction == UserInterface.UIEvent.PROFILE_SEARCH)
				|| (direction == UserInterface.UIEvent.HISTORY_SEARCH)
				|| (direction == UserInterface.UIEvent.PROFILE_SELECT)
				|| (direction == UserInterface.UIEvent.ALTERNATE_PROFILE_SELECT)
				|| (direction == UserInterface.UIEvent.KEY_PROFILE_SELECT) // CP-10858
				|| (direction == UserInterface.UIEvent.HISTORY_SELECT));
	}

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
		} else if ((direction == UserInterface.UIEvent.SAVE_AND_EXIT)
				|| (direction == UserInterface.UIEvent.PREVIOUS_CHECKOUT) // cP-10840
				|| (direction == UserInterface.UIEvent.SAVE_ORDER)) // CP-10842
		{ // this should only appear when on an item, not try UI
			// then we need to create the cust doc order line and exit to catalog order
			// entry
			if ((direction != UserInterface.UIEvent.PREVIOUS_CHECKOUT) || (formBean.isHdnIsDirty())) { // CP-11273 -
																										// don't save
																										// changes if
																										// user
																										// requested not
																										// to
				ui.readPageC1UX(uiRequest, errorStrings, false, direction, false); // no validating
			}
			if (direction == UserInterface.UIEvent.SAVE_AND_EXIT) {
				int currentPage = ui.getCurrentPageC1UX(uiRequest);
				saveIncompleteOrderLine(item, errorStrings, sc, currentPage, sfBean);
			} else if ((direction == UserInterface.UIEvent.SAVE_ORDER) || (formBean.isHdnIsDirty())) // CP-11273 - only
																										// save on
																										// previous if
																										// supposed to
			{
				// CAP-181 RAR - Pass the UIKey so we can save the ui information to order
				// questions table.
				ui.saveOrderQuestions(errorStrings, oeSessionBean, appSessionBean, volatileSessionBean, ui.getUiKey());
			}
			// should redirecting be true?
			// should redirectingOutsideCustDocs be true?
			errorStrings.clear();
			String nextpage = AtWinXSConstant.EMPTY_STRING;

			if (direction == UserInterface.UIEvent.SAVE_AND_EXIT) { // then we need to create the cust doc order line
																	// and exit to cancel location
				errorStrings.clear();
				// CP-8980 Support multiple EARs
				nextpage = RouteConstants.CART_ENTRY_ROUTING_URL;
				resetUIAndItem(volatileSessionBean, oeSessionBean);
				saveAllUpdatedSessions(orderSession, sc.getApplicationVolatileSession(), sc.getApplicationSession(),
						appSessionBean.getSessionID()); // CAP-44307
			} else if (direction == UserInterface.UIEvent.SAVE_ORDER) {
				// what does this direction mean?
			} else // in CP this checks for UserInterface.UIEvent.PREVIOUS_CHECKOUT)
			{ // CP-10840 - moving here since now we need to save upon previous in order
				// questions
				// CP-8980 Support multiple EARs
				nextpage = RouteConstants.CART_ENTRY_ROUTING_URL; // this old way will not work
																	// appSessionBean.encodeURL(ui.getCancelURL(Util.getContextPath(ContextPath.Classic),
																	// Util.getContextPath(ContextPath.Usability), item,
																	//  and the call to getPreviousSearchResultsPage on order session
																	// //CP-12320
				ui.cancelWizard(volatileSessionBean, item);
			}
			sfBean.setRedirectRouting(nextpage);
			sfBean.setSuccess(true);
			return true;
		}

		return false;
	}

	protected void navigateForward(Map<String, String> uiRequest, CustDocUIFormBean uiBean,
			Collection<String> errorStrings, UIEvent direction, CustomDocumentItem item, C1UXCustDocPageBean sfBean,
			SessionContainer sc) throws AtWinXSException {
		UserInterface ui = item.getUserInterface();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		// this should be next, navigate pages, or previous only
		readSubmittedData(direction, uiBean, ui, uiRequest, errorStrings);
		if (!isStaySituation(direction, ui, errorStrings)) { // New requests are out of scope for now. See
																// BaseUIController method of same name
			int currentPageNum = ui.getCurrentPageC1UX(uiRequest);
			// bundles and campaigns are out of scope for now.
			int nextPage = ui.getNextPageNumberC1UX(currentPageNum, direction, uiRequest, appSessionBean); // CP-10444

			// now handling if we go to lists, proofing, or just to another page in the UI
			if (nextPage == UserInterface.NEXT_PAGE_NUMBER_EXIT) {
				handleExitNonPF(currentPageNum, sc, item, errorStrings, sfBean);
			}
			else if ((currentPageNum == UserInterface.NEXT_PAGE_NUMBER_PROOF) && (nextPage < currentPageNum)) {
				ui.resetAllProofs();
			}
			determineNavigatePages(uiRequest, direction, currentPageNum, nextPage, sfBean);
		}

		showProofErrors(errorStrings, uiBean);

		if (!Util.isBlankOrNull(uiBean.getError())) {
			sfBean.setMessage(uiBean.getError());
			sfBean.setSuccess(false);
		}

		checkForSuccess(sfBean);
	}

	protected void handleExitNonPF(int currentPageNum, SessionContainer sc, CustomDocumentItem item,
			Collection<String> errorStrings, C1UXCustDocPageBean sfBean) throws AtWinXSException {
		// non-Pageflex need to exit - where to redirect to?
		UserInterface ui = item.getUserInterface();
		if (!ui.isOrderQuestions()) { // order questions are not in scope yet
			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
			PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();

			// CAP-19287 - need to handle when file generation fails
			try {
				int cdOrderLineNumber = -1;

				// CAP-723 RAR - Do this if NOT in TRY UI.
				if (!ui.isTryUI()) {
					// CAP-11268
					if (ui.isLastPage(currentPageNum) && ui.isSkipProofing()) {
						ui.generateSkippedProofFile(appSessionBean, volatileSessionBean, errorStrings);
					}
					OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
					OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

					item.saveOrderLine(appSessionBean, volatileSessionBean, errorStrings, false, oeSessionBean,
							punchoutSessionBean, // CP-13598/CAP-37
							ICustomDocsAdminConstants.UI_REENTRY_PAGE_NOT_APPLICABLE, "", ""); // CAP-17259 - not
																								// expiring the line
																								// //CAP-30732
					cdOrderLineNumber = item.getCustomDocOrderLineID();
				}

				// CAP-723 RAR - By pass cdOrderLineNumber check if TRY UI.
				if (cdOrderLineNumber > 0 || ui.isTryUI()) {
					sfBean.setRedirectRouting(getForwardToURL(item, ui, sc));
				}
			}
			// CAP-19287 - catch directory creation errors
			catch (AtWinXSException awe) {
				logger.error(
						"CustDocUIController.handleFormSubmission() threw an error trying to generate directories/files for skip proofing. "
								+ awe.getMessage(),
						awe);
				ui.setNextPageNumber(currentPageNum);
				// make sure the error ends up in the list of errors. Add it if missing
				errorStrings.add(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
						TranslationTextConstants.TRANS_NM_ECE_FAILURE_ERR_MSG));
			}
		} // end else when next page was greater than or equal to a list page so redirect
			// was needed
	}

	protected boolean isStaySituation(UIEvent direction, UserInterface ui, Collection<String> errorStrings) {
		return (((ui.isNewRequestFlow()) && (direction == UserInterface.UIEvent.STAY)) || (!errorStrings.isEmpty())
				|| (direction == UserInterface.UIEvent.SAVE_AND_CONTINUE) || (direction == UserInterface.UIEvent.WORKING_PROOF));
	}

	protected void readSubmittedData(UIEvent direction, CustDocUIFormBean uiBean, UserInterface ui,
			Map<String, String> uiRequest, Collection<String> errorStrings) throws AtWinXSException {
		// CAP-46524 - added in REFRESH
		if ((direction == UserInterface.UIEvent.NEXT) || (uiBean.isHdnIsDirty()) || (direction == UserInterface.UIEvent.REFRESH)) { // CP-11273 - don't save changes if
																					// user requested not to
			ui.readPageC1UX(uiRequest, errorStrings, true, direction, false);
		}
	}

	protected void checkForSuccess(C1UXCustDocPageBean sfBean) {
		if ((!sfBean.isHardStopFailure()) && (Util.isBlankOrNull(sfBean.getMessage()))
				&& (sfBean.getFieldMessages().isEmpty())) {
			sfBean.setSuccess(true);
		}
	}

	protected void checkForSuccessBase(C1UXCustDocBaseResponse sfBean) {
		if ((Util.isBlankOrNull(sfBean.getMessage())) && (sfBean.getFieldMessages().isEmpty())) {
			sfBean.setSuccess(true);
		}
	}

	protected void determineNavigatePages(Map<String, String> uiRequest, UIEvent direction, int currentPageNum,
			int nextPage, C1UXCustDocPageBean sfBean) {
		if (direction == UIEvent.NAVIGATE_PAGES) {
			int nextPageDesired = Util.safeStringToDefaultInt(uiRequest.get("navPage"), currentPageNum);
			if (nextPage != nextPageDesired) {
				sfBean.setMessage(
						"Hardcoded String - The system was unable to return you to the chosen page. Please complete specifications from this point forward.");
			}
		}
	}

	/**
	 * @param request
	 * @param orderSession
	 * @param ui
	 * @param item
	 * @param direction
	 * @param sfBean
	 * @throws AtWinXSException
	 */
	protected void performCancelOrCancelOrder(OrderEntrySession orderSession, UserInterface ui, CustomDocumentItem item,
			UserInterface.UIEvent direction, SessionContainer sc, C1UXCustDocPageBean sfBean) throws AtWinXSException {
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean vsb = sc.getApplicationVolatileSession().getVolatileSessionBean();
		String nextPage = null;

		int orderId = item.getOrderID();
		// there may be additional handling for entry point
		// UserInterface.EntryPoint.SUBSCRIPTION_MOD but this is out of scope right now

		if (direction == UserInterface.UIEvent.CANCEL_ORDER) {
			// need to initialize the cancel parameters - if they choose not to cancel the
			// order, they still cannot return to the UI
			orderSession.putParameter("continueURL", RouteConstants.CART_ENTRY_ROUTING_URL);
			orderSession.putParameter("cancelURL", RouteConstants.HOME);
			// I am not sure if this is anything like we should do at this point
			// CP uses the request object and OrderSummaryDetails to build the URL
		}

		String cancelUrl = getCancelURL(item, // orderSession.getOESessionBean().getPreviousSearchResultsPage(),
				ui, sc); // CP-12320
		// there may be additional handling for entry point
		// UserInterface.EntryPoint.SUBSCRIPTION_MOD but this is out of scope right now
		nextPage = cancelUrl;

		OEOrderSessionBean oeSessionBean = orderSession.getOESessionBean();
		// CP-11183 Reset EOO after cancel from cust doc.
		setupDirectionCancel(oeSessionBean, ui, new OrderHeaderVO(orderId), vsb, asb, item);
		sfBean.setRedirectRouting(nextPage);
		sfBean.setSuccess(true);// CAP-42843
		// CAP-3758
		oeSessionBean.setPreviouslyUploadedFiles(new ArrayList<>());
		oeSessionBean.setPreviouslyUploadedFileVersions(new ArrayList<>());

		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		resetUIAndItem(volatileSessionBean, oeSessionBean);
	}

	protected void setupDirectionCancel(OEOrderSessionBean orderSession, UserInterface ui, OrderHeaderVO orderKey,
			VolatileSessionBean vsb, AppSessionBean asb, CustomDocumentItem item) throws AtWinXSException {
		IOEShoppingCartComponent scComponent = OEShoppingCartComponentLocator.locate(asb.getCustomToken());
		OrderLineExtendedVO[] lines = scComponent.getCartLines(orderKey);
		OEShoppingCartAssembler assembler = OEAssemblerFactory.getShoppingCartAssembler(asb.getCustomToken(),
				asb.getDefaultLocale(), asb.getApplyExchangeRate());
		assembler.resetEOOBasedOnCartItems(null, vsb, asb, orderSession, lines);

		ui.cancelWizard(vsb, item);
	}

	@Override
	public C1UXCustDocBaseResponse addToCart(SessionContainer sc) throws AtWinXSException {
		CustDocUIFormBean formBean = new CustDocUIFormBean();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocBaseResponse sfBean = new C1UXCustDocBaseResponse();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();

		try {
			// now be sure to populate the formBean from the item
			item.populateFormBean(formBean, appSessionBean.getDefaultLocale());

			doAddToCart(sc, item, sfBean);
		} catch (AtWinXSMsgException e) { // CAP-46489 - keep kit specific errors
			logger.error(e.getMessage());
			sfBean.setMessage(e.getMsg().getErrGeneralMsg());
		} catch (AtWinXSException e) {
			logger.error(failedAtWinXSExceptionPrefix, e);
		} catch (Exception e) {
			logger.error(failedNonAtWinXSExceptionPrefix, e);
		}
// CAP-42865 - set message when there is no success // CAP-46489 - do not overwrite more specific errors
		if ((!sfBean.isSuccess()) && (Util.isBlankOrNull(sfBean.getMessage()))) {
			setGenericHeaderError(sfBean, appSessionBean);
		}

		return sfBean;
	}

	protected void doAddToCart(SessionContainer sc, CustomDocumentItem item, C1UXCustDocBaseResponse sfBean)
			throws AtWinXSException {
		ApplicationSession appSession = sc.getApplicationSession();
		AppSessionBean appSessionBean = appSession.getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		ApplicationVolatileSession vs = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = vs.getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = appSession.getPunchoutSessionBean();
		UserInterface ui = item.getUserInterface();

		String redirect = getForwardToURL(item, ui, sc);

		Collection<String> errorStrings = new ArrayList<>();

		boolean isCustomListRequest = false;
		if (oeSessionBean.getApprovalSummaryInfo() != null) {
			RoutedOrder routedOrder = oeSessionBean.getApprovalSummaryInfo().getRoutedOrder();
			if (routedOrder != null) {
				isCustomListRequest = routedOrder.isCustomListRouting();
			}
		}

		// CAP-723 RAR - Not needed to execute when on try ui.
		if ((!ui.isTryUI()) && (ui.isFieldsEditable() || ui.getOriginalOrderListSource().equals(OrderListSource.CUSTOM)
				|| isCustomListRequest)) {
			item.saveOrderLine(appSessionBean, volatileSessionBean, errorStrings, false, oeSessionBean,
					punchoutSessionBean, ICustomDocsAdminConstants.UI_REENTRY_PAGE_NOT_APPLICABLE,
					ui.getOrderDeliveryOptionCD(), ui.getOrderDeliveryOptionCD()); // CAP-30395 //CP-13598/CAP-37
			// CAP-46489
			updateKitSessionInfo(item, ui, appSessionBean);
		}

		ui.clearOrderListInfoC1UX(true); // CAP-42233
		resetUIAndItem(volatileSessionBean, oeSessionBean);
		saveAllUpdatedSessions(oeSession, vs, appSession, appSessionBean.getSessionID()); // CAP-44307

		if (ui.isTryUI() || (item.isInNewKit()) || (item.getOrderLineNumber() > 0)) {
			sfBean.setSuccess(true);
			sfBean.setRedirectRouting(redirect);
		}
	}

	// CAP-44307 - this is needed not just for add to cart but other similar
	// scenarios
	protected void saveAllUpdatedSessions(OrderEntrySession oeSession, ApplicationVolatileSession vs,
			ApplicationSession appSession, int sessionID) throws AtWinXSException {
		saveFullOESessionInfo(oeSession, sessionID);
		saveFullSessionInfo(vs, sessionID, AtWinXSConstant.APPVOLATILESESSIONID);
		saveFullSessionInfo(appSession, sessionID, AtWinXSConstant.APPSESSIONSERVICEID);
	}

	protected void resetUIAndItem(VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean) {
		if (oeSessionBean.getCurrentCampaignUserInterface() != null && oeSessionBean.getCurrentCampaignUserInterface()
				.getEntryPoint() == UserInterface.EntryPoint.SUBSCRIPTION_MOD) {
			volatileSessionBean.setOrderId(AtWinXSConstant.INVALID_ID);
		}

		oeSessionBean.setCurrentCustomDocumentItem(null);
		oeSessionBean.setCurrentCustomDocumentUserInterface(null);
		volatileSessionBean.removeProcess();
	}


	 // CAP-44463
	protected C1UXUserProfileSearchOptions loadUserProfileSearch(UserInterface ui, int pageNum) {
		if (onEditableFirstPageOE(ui, pageNum)) {
			ProfileSelector selector = ui.getProfileSelector();
			if ((selector != null) && (selector.isShown())) {
				 return new C1UXUserProfileSearchOptions(selector);
			}
		}
		return null;
	}

	// CAP-42298 Add cancel methods direct access
	@Override
	public C1UXCustDocBaseResponse cancelAction(SessionContainer sc, boolean basicCancel) throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		C1UXCustDocPageBean sfBean = new C1UXCustDocPageBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

		try {
			performCancelOrCancelOrder(oeSession, ui, item,
					basicCancel ? UserInterface.UIEvent.CANCEL : UserInterface.UIEvent.CANCEL_ORDER, sc, sfBean);

			saveAllUpdatedSessions(oeSession, sc.getApplicationVolatileSession(), sc.getApplicationSession(), appSessionBean.getSessionID()); // CAP-44490
		} catch (AtWinXSException e) {
			logger.error(failedAtWinXSExceptionPrefix, e);
			sfBean.setSuccess(false);
			sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		} catch (Exception e) {
			logger.error(failedNonAtWinXSExceptionPrefix, e);
			sfBean.setSuccess(false);
			sfBean.setHardStopFailure(true);
			sfBean.setInitializeFailure(true);
			sfBean.setMessage(getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		}

		return createBaseResponseFromUIBean(sfBean);
	}

	public String getOeJavascriptServerAccessPath() {
		return oeJavascriptServerAccessPath;
	}


	public void setOeJavascriptServerAccessPath(String path) {
		this.oeJavascriptServerAccessPath = path;
	}

	 // CAP-49273
	protected void adjustFirstPage(UserInterface ui) {
		if (ui.isProofOnly()) {
			ui.setNextPageNumber(UserInterface.NEXT_PAGE_NUMBER_PROOF);
		} else if (ui.isMergeOnly()) {
			ui.setNextPageNumber(UserInterface.NEXT_PAGE_NUMBER_LIST);
		}
	}

	// CAP-46488 - new initialize for kit template components
	 public C1UXCustDocBaseResponse initializeFromKitComponent(SessionContainer sc, int index) throws AtWinXSException {
		Map<String, String> uiRequest = new HashMap<>();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		KitSession kitSession = getKitSession(asb);
		if (!validateKitComponent(kitSession, uiRequest, index)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
		if (!validateKitComponentUI(uiRequest, asb)) {
			return invalidUIError(asb);
		}
		C1UXCustDocBaseResponse response = initializeUIOnly(sc, uiRequest);
		updateKitComponent(response, sc, asb, index);
		return response;
	 }

	protected boolean validateKitComponentUI(Map<String, String> uiRequest, AppSessionBean asb) {
		MKBaseAssembler asm = new MKTemplateOrderAssembler(asb.getCustomToken(), asb.getDefaultLocale());
		return asm.doesCustDocHaveValidUI(asb.getSiteID(), uiRequest.get(CustomDocsService.UI_INIT_PARAM_VENDOR_ITEM_NR));
	}

	protected C1UXCustDocBaseResponse invalidUIError(AppSessionBean asb) {
		C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
		response.setMessage(getTranslation(asb.getDefaultLocale(), asb.getCustomToken(),
				TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
		response.setHardStopFailure(true);
		return response;
	}

	protected void updateKitComponent(C1UXCustDocBaseResponse response, SessionContainer sc, AppSessionBean asb, int index) {
		if (response.isSuccess()) {
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			oeSessionBean.getCurrentCustomDocumentItem().setKitComponentIndex(index);
			try {
				saveFullOESessionInfo(oeSession, asb.getSessionID());
			} catch (Exception e) {
				logger.error(e.getMessage());
				response.setSuccess(false);
				response.setMessage(getTranslation(asb.getDefaultLocale(), asb.getCustomToken(),
						TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE));
			}
		}
	}

	protected boolean validateKitComponent(KitSession session, Map<String, String> uiRequest, int index) {
		if ((session != null) && (session.getComponents() != null) && (index >= 0) && (session.getComponents().length > index)) {
			MKComponentInfo component = session.getComponents()[index];
			if ((component != null) && (component.isCustomDoc()) && (!Util.isBlankOrNull(component.getWCSSItemNum()))) {
				getParametersForKitComponent(uiRequest, index, component);
				return true;
			}
		}
		return false;
	}

	protected Map<String, String> getParametersForKitComponent(Map<String, String> c1uxParams, int index, MKComponentInfo component) {
			c1uxParams.put(KitsConstants.COMPONENT_INDEX, String.valueOf(index));

			c1uxParams.put(CustomDocsService.UI_INIT_PARAM_ITEM_NR, Util.nullToEmpty(component.getCustomerItemNum()));
			c1uxParams.put(CustomDocsService.UI_INIT_PARAM_VENDOR_ITEM_NR, Util.nullToEmpty(component.getWCSSItemNum()));
			c1uxParams.put(CustomDocsService.UI_INIT_PARAM_CATALOG_LINE_NR, INVALID_STRING);
			c1uxParams.put(OrderEntryConstants.PARAM_CUST_DOC_ORDER_LINE_ID, component.getCurrentCustomDocID());
			c1uxParams.put(OrderEntryConstants.ORDER_LINE_NUM, component.getOrderLineID());

			c1uxParams.put(OrderEntryConstants.CUST_DOC_ENTRY_POINT, ICustomDocsAdminConstants.ENTRY_POINT_KIT_TEMPLATE);
			c1uxParams.put(OrderEntryConstants.VIEW_ONLY_FLAG, N);

			return c1uxParams;
		}

	// CAP-46489 - add method to update the kit session with the custom doc spec if applicable
	protected void updateKitSessionInfo(CustomDocumentItem item, UserInterface ui, AppSessionBean appSessionBean) throws AtWinXSMsgException {
		if ((ui.getEntryPoint() == EntryPoint.KIT_TEMP) && (item.getCustomDocOrderLineID() > 0)) {
			try { // we have to try to update the kit session
				KitSession kitSession = getKitSession(appSessionBean);
				kitSession.getComponents()[item.getKitComponentIndex()]
						.setNewCustomDocID(String.valueOf(item.getCustomDocOrderLineID()));
				saveKitSession(appSessionBean, kitSession);
			} catch(Exception e) {
				// if this fails, this is fatal - the kit template will not have the info, so we have to fail the add to cart
				item.setCustomDocOrderLineID(AtWinXSConstant.INVALID_ID); // reset this if we cannot fix session
				String error = getTranslation(appSessionBean,
						SFTranslationTextConstants.CANNOT_UPDATE_KIT_TEMPLATE_ERROR,
						SFTranslationTextConstants.CANNOT_UPDATE_KIT_TEMPLATE_ERROR_DEFAULT);
				Message msg = new Message();
				msg.setErrGeneralMsg(error);
				throw new AtWinXSMsgException(msg, this.getClass().getName());
			}
		}
	}

	protected void saveKitSession(AppSessionBean appSessionBean, KitSession kitSession) throws AtWinXSException {
		saveFullSessionInfo(kitSession, appSessionBean.getSessionID(), AtWinXSConstant.KITS_SERVICE_ID);
	}


}
