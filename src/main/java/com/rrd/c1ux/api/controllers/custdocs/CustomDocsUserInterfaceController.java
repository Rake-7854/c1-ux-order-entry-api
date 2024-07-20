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
 *	09/18/23				A Boomker				CAP-42298					Add cancel methods direct access
 *  10/17/23				AKJ Omisol				CAP-43024					Added @PostMapping getImprintHistory
 *  11/13/23				A Boomker				CAP-44426					Added handling for update working proof
 *	03/12/24				A Boomker				CAP-46490					Refactoring to allow for bundles
 *	03/14/24				A Boomker				CAP-46526					Initialize UI from CP URL
 *  04/08/24				A Boomker				CAP-48500					Fix for re-initializing back to cust docs
 * 	07/01/24				A Boomker				CAP-46488					Added handling for kits
 *	07/09/24				A Boomker				CAP-46538					Refactored some handling to base for imprint history
 */
package com.rrd.c1ux.api.controllers.custdocs;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocBaseResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocEditKTOERequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeFromURLRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocInitializeRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocPageBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocUIPageSubmitRequest;
import com.rrd.c1ux.api.services.custdocs.CustomDocsBaseService;
import com.rrd.c1ux.api.services.custdocs.CustomDocsService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.items.util.ItemConstants;
import com.wallace.atwinxs.kits.util.KitsConstants;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.tt.vo.TTSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CustomDocsUserInterfaceController")
@Tag(name = "Cust Docs Main User Interface API Controller")
public class CustomDocsUserInterfaceController  extends CustDocsBaseController {

	private static final Logger logger = LoggerFactory.getLogger(CustomDocsUserInterfaceController.class);

	protected CustomDocsService custDocsService;
	@Override
	protected CustomDocsBaseService getService() {
		return custDocsService;
	}

	protected void setService(CustomDocsService newService) {
		this.custDocsService = newService;
	}

	public CustomDocsUserInterfaceController(TokenReader tokenReader, CPSessionReader sessionReader, CustomDocsService service) {
		super(tokenReader, sessionReader);
		custDocsService = service;
	}


	@GetMapping(value = RouteConstants.CUST_DOCS_UI_API, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Get current UI Page for custom document already initialized in session")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocPageBean> getUIPage(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocPageBean response = custDocsService.getCurrentPageUI(sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@GetMapping(value = RouteConstants.CUST_DOCS_ADD_TO_CART_API, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Add to cart for custom document already initialized in session")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> addToCart(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocBaseResponse response = custDocsService.addToCart(sc);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@PostMapping(value = RouteConstants.CUST_DOCS_UI_PAGE_SUBMIT_API, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Submit current UI Page for custom document already initialized in session. Specific action may be specified. Bean returned will be either the page bean (from the get) or the base response bean depending on action passed.")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> submitUIPage(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocUIPageSubmitRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        Map<String, String> uiRequest = getParametersFromCustDocAPICall(request);
        C1UXCustDocBaseResponse response = custDocsService.performPageSubmitAction(sc, uiRequest);
         return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@PostMapping(value = RouteConstants.CUST_DOCS_INITIALIZE_API, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Initialize session for UI requested")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> initializeUI(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocInitializeRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        Map<String, String> uiRequest = getParametersFromCustDocAPICall(request);
        checkSessionForClassInitialize(sc, uiRequest);
        C1UXCustDocBaseResponse response = custDocsService.initializeUIOnly(sc, uiRequest);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	// CAP-46488
	@PostMapping(value = RouteConstants.CUST_DOCS_INITIALIZE_API_FOR_KIT, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Initialize session for UI requested")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<C1UXCustDocBaseResponse> editCustDocInKit(@RequestHeader(value = "ttsession", required=false) String ttsession,
			@RequestBody(required = true) C1UXCustDocEditKTOERequest itemInputs) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
		validateAuthorization(sc);
        C1UXCustDocBaseResponse response = custDocsService.initializeFromKitComponent(sc, itemInputs.getIndex());
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-46526
	@PostMapping(value = RouteConstants.CUST_DOCS_INITIALIZE_API_FROM_URL, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Initialize session for UI requested")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> initializeUIFromURL(@RequestHeader(value = "ttsession", required = false) String ttsession,
    		@RequestBody C1UXCustDocInitializeFromURLRequest request) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
		HashMap<String, String> c1uxParams = new HashMap<>();
		C1UXCustDocBaseResponse response = validateUrl(sc, request, c1uxParams);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	protected static final String URL_INITIALIZE_PREFIX_REQUIRED = "/ui.cp?";
	protected static final int URL_INITIALIZE_PREFIX_REQUIRED_SIZE = URL_INITIALIZE_PREFIX_REQUIRED.length();
	protected static final String URL_INDICATING_NEW_REQUEST = OrderEntryConstants.CUST_DOC_ENTRY_POINT
			+ "=" + ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST;

	protected C1UXCustDocBaseResponse validateUrl(SessionContainer sc, C1UXCustDocInitializeFromURLRequest request,
			HashMap<String, String> c1uxParams) throws AccessForbiddenException {
		C1UXCustDocBaseResponse response = new C1UXCustDocBaseResponse();
		if ((!Util.isBlankOrNull(request.getUrl())) && ((request.getUrl().contains(URL_INITIALIZE_PREFIX_REQUIRED))
				&& (request.getUrl().contains(AtWinXSConstant.TT_SESSION_ID)))) {
			int paramStart = request.getUrl().indexOf(URL_INITIALIZE_PREFIX_REQUIRED) + URL_INITIALIZE_PREFIX_REQUIRED_SIZE;
			if (request.getUrl().length() > paramStart) { // there is no more string after the prefix, don't try to parse
				c1uxParams.putAll(Util.parseQueryString(request.getUrl().substring(paramStart)));
				compareSessionIDs(sc, c1uxParams);
		        checkSessionForClassInitialize(sc, c1uxParams);
		        try {
		        	response = custDocsService.initializeUIOnly(sc, c1uxParams);
		        } catch(Exception e) {
					logger.error(e.getMessage());
		        }
			}
		}

		if (!response.isSuccess() && Util.isBlankOrNull(response.getMessage())) {
			// this URL will be considered in an improper format and must be given a C1UX specific error
			response.setMessage(getInitializeUrlFailureMessage(sc.getApplicationSession().getAppSessionBean(), request.getUrl()));
		}
		return response;
	}

	protected void compareSessionIDs(SessionContainer sc, HashMap<String, String> c1uxParams) throws AccessForbiddenException {
		if ((!c1uxParams.containsKey(AtWinXSConstant.TT_SESSION_ID))
				|| (Util.isBlank(c1uxParams.get(AtWinXSConstant.TT_SESSION_ID)))) {
			throw new AccessForbiddenException(this.getClass().getName()); // CP URLs had to have a session on them to be valid
		}
		boolean matchingSession = false;
		try {
			int thisSessionID = sc.getApplicationSession().getAppSessionBean().getSessionID();
			TTSession cpSession = mSessionReader.getTTSession(c1uxParams.get(AtWinXSConstant.TT_SESSION_ID));
			matchingSession = (cpSession.getId() == thisSessionID);
		} catch(Exception e) {
			logger.error(e.getMessage());
		}

		if (!matchingSession) {
			throw new AccessForbiddenException(this.getClass().getName()); // this URL does not belong to this user!
		}
	}

	protected String getInitializeUrlFailureMessage(AppSessionBean appSessionBean, String url) {
		if ((!Util.isBlankOrNull(url)) && (url.contains(URL_INDICATING_NEW_REQUEST))) {
			return custDocsService.getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
					TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE);
		}
		return custDocsService.getTranslation(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(),
				TranslationTextConstants.TRANS_NM_UI_NOT_ORDERABLE);
	}
	/**
	 * This version of the method should be called for initializing a UI.
	 * @param request
	 * @return
	 */
	protected Map<String, String> getParametersFromCustDocAPICall(C1UXCustDocInitializeRequest request) {
		HashMap<String, String> c1uxParams = new HashMap<>();
		c1uxParams.put(CustomDocsService.UI_INIT_PARAM_ITEM_NR, Util.nullToEmpty(request.getItemNumber()));
		c1uxParams.put(CustomDocsService.UI_INIT_PARAM_VENDOR_ITEM_NR, Util.nullToEmpty(request.getVendorItemNumber()));
		c1uxParams.put(CustomDocsService.UI_INIT_PARAM_CATALOG_LINE_NR, String.valueOf(request.getCatalogLineNumber()));
		c1uxParams.put(OrderEntryConstants.PARAM_CUST_DOC_ORDER_LINE_ID, String.valueOf(request.getCustomDocumentOrderLineNumber()));
		c1uxParams.put(OrderEntryConstants.ORDER_LINE_NUM, String.valueOf(request.getOrderLineNumber()));

		c1uxParams.put(OrderEntryConstants.CUST_DOC_ENTRY_POINT, Util.isBlankOrNull(request.getEntryPoint()) ?
				ICustomDocsAdminConstants.ENTRY_POINT_CATALOG_EXPRESS : request.getEntryPoint());
		c1uxParams.put(OrderEntryConstants.VIEW_ONLY_FLAG, Util.boolToY(request.isViewOnly()));
		c1uxParams.put(OrderEntryConstants.PARAM_ITEM_UOM, Util.nullToEmpty(request.getSelectedUom()));
		c1uxParams.put(OrderEntryConstants.PARAM_ORDER_QTY, String.valueOf(request.getItemQuantity()));

		// entry point specific parameters
		c1uxParams.put(KitsConstants.ACTIVITY_CLASS, Util.nullToEmpty(request.getKitActivity()));
		c1uxParams.put(OrderEntryConstants.PARAM_ITEM_INX, String.valueOf(request.getOrderFromFileIndex()));
		c1uxParams.put(ICustomDocsAdminConstants.ORDER_QUESTION_ENTRY_SOURCE, Util.nullToEmpty(request.getOrderQuestionsFromRouting()));
		c1uxParams.put(ICustomDocsAdminConstants.ORDER_QUESTION_PROJECT_ID, String.valueOf(request.getOrderQuestionsProjectID()));
		c1uxParams.put(ICustomDocsAdminConstants.ORDER_QUESTION_TEMPLATE_NR, String.valueOf(request.getOrderQuestionsUINumber()));

		return c1uxParams;
	}

	@GetMapping(value = RouteConstants.CUST_DOCS_CANCEL_API, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Cancel custom document already initialized in session. Specific event action is CANCEL.")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> cancel(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocBaseResponse response = custDocsService.cancelAction(sc, true);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	@GetMapping(value = RouteConstants.CUST_DOCS_CANCEL_ORDER_API, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Cancel custom document already initialized in session. Specific event action is CANCEL_ORDER.")
    @ApiResponse(responseCode = RouteConstants.HTTP_OK)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
    @ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
    @ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
    public ResponseEntity<C1UXCustDocBaseResponse> cancelOrder(@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {
        SessionContainer sc = getSessionContainer(ttsession);
        checkSessionForClass(sc);
        C1UXCustDocBaseResponse response = custDocsService.cancelAction(sc, true);
        return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
    }

	protected void checkSessionForClass(SessionContainer sc) throws AccessForbiddenException {
        validateAuthorization(sc);
        validateSession(sc);
        setService(getClassSpecificService(custDocsService, sc));
	}

	// CAP-46490 - these methods should be added to whenever there is an extension created for the class!
	protected void checkSessionForClassInitialize(SessionContainer sc, Map<String, String> uiRequest) throws AccessForbiddenException {
		validateAuthorization(sc);
		String itemClass = custDocsService.getItemClassFromRequest(sc, uiRequest);
		if (itemClass != null) {
			switch(itemClass) {
				case ItemConstants.ITEM_CLASS_BUNDLE:
					setService(makeBundleServiceImpl(custDocsService, sc));
					break;
				case CustomDocsBaseService.ENTRY_POINT_NEW_REQUEST_STRING:
					setService(makeNewRequestServiceImpl(custDocsService, sc));
					break;
				default:
					setService(makeCustomDocsServiceImpl(custDocsService, sc));
			}
		}
	}

}
