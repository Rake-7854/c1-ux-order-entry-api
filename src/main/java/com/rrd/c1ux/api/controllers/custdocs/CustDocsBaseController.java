/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				DTS#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	08/07/23				A Boomker			    CAP-42225 					Controller for file stream requests
 *	09/21/23				A Boomker				CAP-44047					Added missing checkAccessAllowed method
 *	11/13/23				A Boomker				CAP-44426					Moved getParametersFromCustDocAPICall() to base
 *	03/13/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 *  04/03/24				A Boomker				CAP-46494					Proofing overrides for bundle
 *  04/08/24				A Boomker				CAP-48500					Fix for re-initializing back to cust docs
 *  04/23/24				R Ruth					CAP-42226					Added for CustomDocsListService
 *  05/15/24				R Ruth					CAP-42228					Added for CustomDocsListMappingService
 *  07/09/24				A Boomker				CAP-46538					Added handling for imprint history service
 */

package com.rrd.c1ux.api.controllers.custdocs;

import java.util.HashMap;
import java.util.Map;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUIPageSubmitRequest;
import com.rrd.c1ux.api.services.custdocs.BundleProofingServiceImpl;
import com.rrd.c1ux.api.services.custdocs.BundleServiceImpl;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsFileStreamService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsImprintHistoryService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsListMappingService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsListService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProfileService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProofingService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsProofingServiceImpl;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsServiceImpl;
import com.rrd.c1ux.api.services.custdocs.NewRequestServiceImpl;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;


//@RestController("CustDocsBaseController")
//@Tag(name = "Cust Docs Base")
public abstract class CustDocsBaseController extends BaseCPApiController {

	protected CustDocsBaseController(TokenReader tokenReader, CPSessionReader sessionReader) {
		super(tokenReader, sessionReader);
	}

	abstract protected CustomDocsBaseService getService();

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}
// CAP-44047 - was missing this method
	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	/** This method should throw 403 unauthorized exceptions if the user cannot do custom docs.
	 *
	 * @param sc
	 * @throws AccessForbiddenException
	 */
	public void validateAuthorization(SessionContainer sc) throws AccessForbiddenException
	{
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		if (!oeSessionBean.getUserSettings().isAllowCustomDocumentsInd()) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
	}
	/** This method should throw 403 unauthorized exceptions if the user has no UI in session.
	 * It should only be called after initialize has returned a success but not after add to cart has cleared the UI from session.
	 *
	 * @param sc
	 * @throws AccessForbiddenException
	 */
	public void validateSession(SessionContainer sc) throws AccessForbiddenException
	{
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();

		if (oeSessionBean.getCurrentCustomDocumentUserInterface() == null) {
			throw new AccessForbiddenException(this.getClass().getName());
		}
	}

	/**
	 * This version of the method should be called for submits of a regular UI page.
	 * @param request
	 * @return
	 */
	protected Map<String, String> getParametersFromCustDocAPICall(C1UXCustDocUIPageSubmitRequest request) {
		HashMap<String, String> c1uxParams = new HashMap<>();
		if (!Util.isBlankOrNull(request.getForm1()))
		{
			c1uxParams.putAll(Util.parseQueryString(request.getForm1()));
		}

		if (!Util.isBlankOrNull(request.getOtherSerializedForm()))
		{
			c1uxParams.putAll(Util.parseQueryString(request.getOtherSerializedForm()));
		}

		if (!Util.isBlankOrNull(request.getEventAction()))
		{
			c1uxParams.put("eventAction", request.getEventAction());
		}

		c1uxParams.put("validateRequest", Util.boolToY(request.isValidate()));

		return c1uxParams;
	}

	// CAP-46490 - these methods should be added to whenever there is an extension created for the class!
	protected CustomDocsService getClassSpecificService(CustomDocsService service, SessionContainer sc) {
		String itemClass = service.getItemClassFromSession(sc);
		if (itemClass != null) {
			switch(itemClass) {
				case ItemConstants.ITEM_CLASS_BUNDLE:
					return makeBundleServiceImpl(service, sc);
				case CustomDocsBaseService.ENTRY_POINT_NEW_REQUEST_STRING:
					return makeNewRequestServiceImpl(service, sc);
				default:
			}
		}
		return makeCustomDocsServiceImpl(service, sc);
	}

	protected CustomDocsService makeCustomDocsServiceImpl(CustomDocsService service, SessionContainer sc) {
		String path = service.getOeJavascriptServerAccessPath();
		CustomDocsService newService = new CustomDocsServiceImpl(service.getTranslationService(), service.getObjectMapFactoryService(), service.getSessionHandlerService());
		newService.setOeJavascriptServerAccessPath(path);
		newService.setCustomizationTokenForSession(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		return newService;
	}

	protected CustomDocsProofingService makeCustomDocsProofingServiceImpl(CustomDocsProofingService service, SessionContainer sc) {
		CustomDocsProofingService newService = new CustomDocsProofingServiceImpl(service.getTranslationService(), service.getObjectMapFactoryService(), service.getSessionHandlerService());
		newService.setCustomizationTokenForSession(sc.getApplicationSession().getAppSessionBean().getCustomToken());
		return newService;
	}

	protected CustomDocsService makeBundleServiceImpl(CustomDocsService service, SessionContainer sc) {
		return new BundleServiceImpl(service, sc);
	}

	protected CustomDocsProofingService makeBundleProofingServiceImpl(CustomDocsProofingService service, SessionContainer sc) {
		return new BundleProofingServiceImpl(service, sc);
	}

	protected CustomDocsService makeNewRequestServiceImpl(CustomDocsService service, SessionContainer sc) {
		return new NewRequestServiceImpl(service, sc);
	}

	// CAP-46490 - these methods should be added to whenever there is an extension created for the class!
	protected CustomDocsFileStreamService getClassSpecificService(CustomDocsFileStreamService service, SessionContainer sc) {
		return service;
	}

	// CAP-46490 - these methods should be added to whenever there is an extension created for the class!
	protected CustomDocsProfileService getClassSpecificService(CustomDocsProfileService service, SessionContainer sc) {
		return service;
	}

	// CAP-42226 - for get lists
	protected CustomDocsListService getClassSpecificService(CustomDocsListService service, SessionContainer sc) {
		return service;
	}

	// CAP-42228 - for get lists mappings
	protected CustomDocsListMappingService getClassSpecificService(CustomDocsListMappingService service, SessionContainer sc) {
		return service;
	}

	// CAP-46490 - these methods should be added to whenever there is an extension created for the class!
	protected CustomDocsProofingService getClassSpecificService(CustomDocsProofingService service, SessionContainer sc) {
		String itemClass = service.getItemClassFromSession(sc);
		if (itemClass != null) {
			switch(itemClass) {
				case ItemConstants.ITEM_CLASS_BUNDLE:
					return makeBundleProofingServiceImpl(service, sc);
				case ItemConstants.ITEM_CLASS_CAMPAIGN:
				case ItemConstants.ITEM_CLASS_PROMOTIONAL:
				default:
			}
		}
		return makeCustomDocsProofingServiceImpl(service, sc);
	}

	// CAP-46490 - these methods should be added to whenever there is an extension created for the class!
	protected CustomDocsImprintHistoryService getClassSpecificService(CustomDocsImprintHistoryService service, SessionContainer sc) {
		return service;
	}

}
