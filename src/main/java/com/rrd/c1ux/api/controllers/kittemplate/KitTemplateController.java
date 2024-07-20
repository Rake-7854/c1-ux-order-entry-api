/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/07/24	N Caceres			CAP-50006				Initial Version
 *	06/11/24	Satishkumar A		CAP-50007				C1UX API - Create new api to remove component kit item from kit
 *	06/10/24	L De Leon			CAP-49882				Added initKitTemplate() method
 *	06/18/24	N Caceres			CAP-50186				Create new API for adding wild card component to kit template
 *	06/19/24	M Sakthi			CAP-50145				Create new API for When adding components for a kit, we need to create an API to add the components to our order
 *	06/25/24	Satishkumar A		CAP-50308				C1UX API - Creation of service to reload KitSession when coming back to kit editor from search or custom docs
 *	06/27/24	M Sakthi			CAP-50330				C1UX BE - When adding components for a kit, we need to create an API to add the components to our order
 *	06/26/24	N Caceres			CAP-50309				Create new API for browsing catalog from Kit Editor
 *	06/26/24	L De Leon			CAP-50359				Added cancelKitTemplate() method
 *	07/03/24	Satishkumar A		CAP-50560				Added catalogSearchForKitTemplates() method
 *	07/04/24	C Codina			CAP-46486				C1UX API - Kit Template OE integrate with BE API to enter custom doc and redirect
 */
package com.rrd.c1ux.api.controllers.kittemplate;

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

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateRequest;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateResponse;
import com.rrd.c1ux.api.models.kittemplate.KitCatalogBrowseResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateCancelResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateEditCustomDocRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateEditCustomDocResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateSearchResponse;
import com.rrd.c1ux.api.services.kittemplate.KitTemplateService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("KitTemplateController")
@Tag(name = "Kit Template API")
public class KitTemplateController extends BaseCPApiController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KitTemplateController.class);
	private final KitTemplateService kitTemplateService;
	
	protected KitTemplateController(TokenReader tokenReader, CPSessionReader sessionReader,
			KitTemplateService kitTemplateService) {
		super(tokenReader, sessionReader);
		this.kitTemplateService = kitTemplateService;
	}

	// CAP-50006
	@PostMapping(value = RouteConstants.KIT_ADD_COMPONENT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Add kit component")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitTemplateAddCompResponse> addKitComponent(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateAddCompRequest request) throws AtWinXSException {
		LOGGER.debug("In addKitComponent()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateAddCompResponse response = kitTemplateService.addKitComponent(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-50007
	@PostMapping(value = RouteConstants.KIT_REMOVE_COMPONENT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Remove Kit Component")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitTemplateRemoveCompResponse> removeKitComponent(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateRemoveCompRequest request) throws AtWinXSException {
		LOGGER.debug("In removeKitComponent()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateRemoveCompResponse response = kitTemplateService.removeKitComponent(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID()) && asb.hasService(AtWinXSConstant.KITS_SERVICE_ID);
	}

	// CAP-49882
	@PostMapping(value = RouteConstants.INIT_KIT_TEMPLATE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Initialize kit template information")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<InitKitTemplateResponse> initKitTemplate(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody InitKitTemplateRequest request) throws AtWinXSException {

		LOGGER.debug("In initKitTemplate()");
		SessionContainer sc = getSessionContainer(ttsession);
		InitKitTemplateResponse response = kitTemplateService.initKitTemplate(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-50308
	@GetMapping(value = RouteConstants.RELOAD_KIT_TEMPLATE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Reload kit template information")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<InitKitTemplateResponse> reloadKitTemplate(
			@RequestHeader(value = "ttsession", required = false) String ttsession
			) throws AtWinXSException {

		LOGGER.debug("In reloadKitTemplate()");
		SessionContainer sc = getSessionContainer(ttsession);
		InitKitTemplateResponse response = kitTemplateService.reloadKitTemplate(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-50186
	@PostMapping(value = RouteConstants.KIT_ADD_WILD_CARD_COMPONENT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Add wild card component to Kit Template")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitTemplateAddCompResponse> addWildCardComponent(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateAddCompRequest request) throws AtWinXSException {
		LOGGER.debug("In addWildCardComponent()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateAddCompResponse response = kitTemplateService.addWildCardComponent(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-50145
	@PostMapping(value = RouteConstants.KIT_ADD_TO_CART, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Add to cart Kit Component")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitTemplateAddToCartResponse> addToCartKitComponent(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateAddToCartRequest request) throws AtWinXSException {
		LOGGER.debug("In addToCartKitComponent()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateAddToCartResponse response = kitTemplateService.addToCartKitTemplate(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-50309
	@PostMapping(value = RouteConstants.KIT_CATALOG_BROWSE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Kit catalog browse")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitCatalogBrowseResponse> kitBrowseCatalog(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateAddToCartRequest request) throws AtWinXSException {
		LOGGER.debug("In kitBrowseCatalog()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitCatalogBrowseResponse response = kitTemplateService.kitBrowseCatalog(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-50359
	@GetMapping(value = RouteConstants.RELOAD_KIT_CANCEL, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Cancel kit template editing process")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<KitTemplateCancelResponse> cancelKitTemplate(
			@RequestHeader(value = "ttsession", required = false) String ttsession
			) throws AtWinXSException {

		LOGGER.debug("In cancelKitTemplate()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateCancelResponse response = kitTemplateService.cancelKitTemplate(sc);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-50560
	@PostMapping(value = RouteConstants.KIT_CATALOG_SEARCH, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Catalog Search for Kit Templates")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitTemplateSearchResponse> catalogSearchForKitTemplates(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateAddToCartRequest request) throws AtWinXSException {
		LOGGER.debug("In catalogSearchForKitTemplates()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateSearchResponse response = kitTemplateService.catalogSearchForKitTemplates(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	//CAP-46486
	@PostMapping(value = RouteConstants.KIT_EDIT_CUSTOM_DOC, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Kit Component Index")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<KitTemplateEditCustomDocResponse> getKitComponentIndex(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody KitTemplateEditCustomDocRequest request) throws AtWinXSException {
		LOGGER.debug("In getKitComponentIndex()");
		SessionContainer sc = getSessionContainer(ttsession);
		KitTemplateEditCustomDocResponse response = kitTemplateService.getKitComponentIndex(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}
