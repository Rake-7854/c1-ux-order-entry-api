/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/03/23				L De Leon				CAP-38053					Initial Version
 *	03/24/23				L De Leon				CAP-39371					Added call to service to populate response
 *	04/10/23				S Ramachandran			CAP-38159					Added API to Company Master Address search
 *	04/20/23				S Ramachandran			CAP-39973					Request & Response integration Address search
 *	03/24/23				L De Leon				CAP-38151					Added save delivery information
 *	03/24/23				L De Leon				CAP-40324					Added call to service to save delivery information
 *	06/02/23				L De Leon				CAP-40324					Removed catch block from saveDeliveryInformation() method
 *	09/12/23				M Sakthi				CAP-41591					Added oesearchPAB() method
 *	11/20/23				Satishkumar A			CAP-38135					C1UX BE - Modification of Manual Enter Address to use new USPS validation
 *  12/18/23				M Sakthi				CAP-45544					C1UX BE - Fix code to only search/edit/save for the originator PAB addresses in OOB mode
 *  03/12/24				M Sakthi				CAP-47386					C1UX API - Create API to retrieve list of Distribution Lists.
 *  03/15/24				M Sakthi				CAP-47777					C1UX API - Create API to save dist lists fields to ManagedListSession.
 *  03/15/24				C Codina				CAP-47778					C1UX API - Create API to get Dist List count.
 *  03/15/24				S Ramachandran			CAP-47387					Added controller API handler to get worksheets and upload file to CP
 *  03/21/24				Satishkumar A			CAP-47389					C1UX API - Create API to retrieve Dist List addresses.
 *  03/18/24				S Ramachandran			CAP-48002					Integrated controller handler with service method to get worksheets and upload file to CP  	
 *  03/26/24				M Sakthi				CAP-47390					C1UX API - Create API to save columns to map
 *  03/26/24				S Ramachandran			CAP-47388					Added controller API handler to upload Dist List and return mapperData.	
 *	03/29/24				Krishna Natarajan		CAP-47391					Added controller API handler to update Dist List info.	
 *  03/27/24				S Ramachandran			CAP-48277					Integrated controller handler to call service method uploadDistList and return mapperData.		
 *	04/01/24				Satishkumar A			CAP-48123					C1UX BE - Create API to retrieve Dist List addresses.
 *	04/03/24				M Sakthi				CAP-48202					C1UX BE - Create API to save columns to map
 *	04/08/24				S Ramachandran			CAP-48434					Modified uploadDistList api handler to go into a specific List for mapping
 *	04/17/24				M Sakthi				CAP-48582					C1UX API - Create new API to start the save order template process
 */
package com.rrd.c1ux.api.controllers.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.c1ux.api.controllers.BaseCPApiController;
import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.admin.PABSearchRequest;
import com.rrd.c1ux.api.models.admin.PABSearchResponse;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationResponse;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveRequest;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveResponse;
import com.rrd.c1ux.api.models.checkout.CompanyMasterAddressSearchRequest;
import com.rrd.c1ux.api.models.checkout.CompanyMasterAddressSearchResponse;
import com.rrd.c1ux.api.models.checkout.CreateListVarsRequest;
import com.rrd.c1ux.api.models.checkout.DistListAddressRequest;
import com.rrd.c1ux.api.models.checkout.DistListAddressResponse;
import com.rrd.c1ux.api.models.checkout.DistListCountRequest;
import com.rrd.c1ux.api.models.checkout.DistListCountResponse;
import com.rrd.c1ux.api.models.checkout.DistListUpdateRequest;
import com.rrd.c1ux.api.models.checkout.DistListUpdateResponse;
import com.rrd.c1ux.api.models.checkout.DistributionListResponse;
import com.rrd.c1ux.api.models.checkout.SaveManagedFieldsResponse;
import com.rrd.c1ux.api.models.checkout.SaveMappingRequest;
import com.rrd.c1ux.api.models.checkout.SaveMappingResponse;
import com.rrd.c1ux.api.models.checkout.UploadDistListRequest;
import com.rrd.c1ux.api.models.checkout.UploadDistListResponse;
import com.rrd.c1ux.api.models.checkout.WorksheetRequest;
import com.rrd.c1ux.api.models.checkout.WorksheetResponse;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.checkout.CODeliveryInformationService;
import com.rrd.c1ux.api.services.session.CPSessionReader;
import com.rrd.c1ux.api.services.session.TokenReader;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.admin.util.AdminUtil;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("CODeliveryInformationController")
@Tag(name = "Delivery Information API")
public class CODeliveryInformationController extends BaseCPApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CODeliveryInformationController.class);

	@Autowired
	CODeliveryInformationService deliveryInformationService;
	
   private final  SelfAdminService mSelfAdminService;

	protected CODeliveryInformationController(TokenReader tokenReader, CPSessionReader sessionReader,SelfAdminService selfAdminService) {
		super(tokenReader, sessionReader);
		mSelfAdminService=selfAdminService;
	}

	@Override
	protected int getServiceID() {
		return AtWinXSConstant.ORDERS_SERVICE_ID;
	}

	@Override
	protected boolean checkAccessAllowed(AppSessionBean asb) {
		return asb.hasService(getServiceID());
	}

	@GetMapping(value = RouteConstants.GET_DELIVERY_INFO, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Delivery Information for the Delivery Section during checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<CODeliveryInformationResponse> getDeliveryInformation(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getDeliveryInformation()");
		SessionContainer sc = getSessionContainer(ttsession);

		CODeliveryInformationResponse response = new CODeliveryInformationResponse();
		try {
			response = deliveryInformationService.getDeliveryInformation(sc);
		} catch (AtWinXSException e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	@PostMapping(value = RouteConstants.GET_COMPANY_MASTER_ADDR, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get list of Company Master Address for a Search Criteria in Delivery Section during checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CompanyMasterAddressSearchResponse> searchCompanyMasterAddresses(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody CompanyMasterAddressSearchRequest request) throws AtWinXSException {

		LOGGER.debug("In searchCompanyMasterAddresses()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		
		CompanyMasterAddressSearchResponse response = deliveryInformationService.searchCompanyMasterAddresses(sc,request);
	
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}

	// CAP-38151
	@PostMapping(value = RouteConstants.SAVE_DELIVERY_INFO, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save selected address for checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CODeliveryInformationSaveResponse> saveDeliveryInformation(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody CODeliveryInformationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In saveDeliveryInformation()");
		SessionContainer sc = getSessionContainer(ttsession);
		boolean uspsValidationFlag = false;

		CODeliveryInformationSaveResponse response = deliveryInformationService.saveDeliveryInformation(sc, request,uspsValidationFlag);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-38151 //CAP-38135
	@PostMapping(value = RouteConstants.SAVE_DELIVERY_INFO_V1, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save selected address for checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<CODeliveryInformationSaveResponse> saveDeliveryInformationV1(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody CODeliveryInformationSaveRequest request) throws AtWinXSException {

		LOGGER.debug("In saveDeliveryInformation()");
		SessionContainer sc = getSessionContainer(ttsession);
		boolean uspsValidationFlag = true;
		CODeliveryInformationSaveResponse response = deliveryInformationService.saveDeliveryInformation(sc, request,uspsValidationFlag);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
 //CAP-41591	
	@PostMapping(value = RouteConstants.SEARCH_OE_PAB, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get list of PAB for a Search Criteria in Delivery Section during checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	public ResponseEntity<PABSearchResponse> oesearchPAB(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody PABSearchRequest request) throws AtWinXSException {
			LOGGER.debug("In getPAB()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		
		//CAP-45544
		boolean useOriginator=false;
		OEResolvedUserSettingsSessionBean settings = AdminUtil.getUserSettings(new LoginVOKey(appSessionBean.getSiteID(), appSessionBean.getOriginatorProfile().getLoginID()), appSessionBean.getSessionID(), appSessionBean.getCustomToken());
		if (settings.isAllowOrderOnBehalf() && appSessionBean.getRequestorProfile()!=null) {
			useOriginator=true;
		}
		
		PABSearchResponse response = mSelfAdminService.searchPAB(sc, request,useOriginator);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	//CAP-47386
	@GetMapping(value = RouteConstants.GET_DISTRIBUTION_LIST, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Distribution List during checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<DistributionListResponse> getDistributionList(
			@RequestHeader(value = "ttsession", required = false) String ttsession) throws AtWinXSException {

		LOGGER.debug("In getDistributionList()");
	
		SessionContainer sc = getSessionContainer(ttsession);
		DistributionListResponse response=deliveryInformationService.getManagedLists(sc, false);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	// CAP-47777
	@PostMapping(value = RouteConstants.CREATE_LIST_VARS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "In saveValueIntoManagedListSession()")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<SaveManagedFieldsResponse> saveValueIntoManagedListSession(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody CreateListVarsRequest request) throws AtWinXSException{

		LOGGER.debug("In saveValueIntoManagedListSession()");

		SessionContainer sc = getSessionContainer(ttsession);
		SaveManagedFieldsResponse response=deliveryInformationService.saveManagedListFields(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	
	//CAP-47778
	@PostMapping(value = RouteConstants.GET_DISTRIBUTION_LIST_RECORD_COUNT, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Distribution List Record Count")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	public ResponseEntity<DistListCountResponse> getDistributionListCount(
			@RequestHeader(value = "ttsession", required = false)String ttsession,
			@RequestBody DistListCountRequest request) throws AtWinXSException{
		
		LOGGER.debug("In getDistributionListCount()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		DistListCountResponse response = deliveryInformationService.getDistributionListRecordCount(sc, request);	
	
		return new ResponseEntity<>(response,(response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	
	}
	
	//CAP-47389
	@PostMapping(value = RouteConstants.GET_DISTRIBUTION_LIST_ADDRESSES, produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Get Distribution List addresses during checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<DistListAddressResponse> getDistributionListAddresses(
			@RequestHeader(value = "ttsession", required = false) String ttsession, @RequestBody DistListAddressRequest request) throws AtWinXSException, CPRPCException {

		LOGGER.debug("In getDistributionListAddresses()");
	
		SessionContainer sc = getSessionContainer(ttsession);
		DistListAddressResponse response=deliveryInformationService.getDistributionListAddresses(sc, request);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-47387, CAP-48002
	@PostMapping(value = RouteConstants.GET_WORKSHEETS, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Upload Dist List file to CP path and get worksheets names")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<WorksheetResponse> uploadDistributionListFile(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@ModelAttribute WorksheetRequest request) throws AtWinXSException {

		LOGGER.debug("In uploadDistributionListFile()");

		SessionContainer sc = getSessionContainer(ttsession);
		WorksheetResponse response = deliveryInformationService.uploadDistListFileAndGetWorksheetName(sc, request);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	//CAP-47390
	@PostMapping(value = RouteConstants.SAVE_LIST_MAPPINGS, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Save list mapping during checkout")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<SaveMappingResponse> saveListMappings(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody SaveMappingRequest request) throws AtWinXSException, CPRPCException {

		LOGGER.debug("In saveListMappings()");
		SessionContainer sc = getSessionContainer(ttsession);
		SaveMappingResponse response = deliveryInformationService.saveListMappings(sc, request, false, false, "");
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-47388, CAP-48277, CAP-48434
	@PostMapping(value = RouteConstants.UPLOAD_DIST_LIST, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Upload Distribution list and get MapperData")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<UploadDistListResponse> uploadDistListAndGetMapperData(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody UploadDistListRequest request) throws AtWinXSException {

		LOGGER.debug("In uploadDistListAndGetMapperData()");
		
		SessionContainer sc = getSessionContainer(ttsession);
		UploadDistListResponse response = deliveryInformationService.uploadDistList(sc, request, false, false);

		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
	
	// CAP-47391
	@PostMapping(value = RouteConstants.UPDATE_DIST_LIST_INFO, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(summary = "Update Distribution list info")
	@ApiResponse(responseCode = RouteConstants.HTTP_OK)
	@ApiResponse(responseCode = RouteConstants.HTTP_FORBIDDEN)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNAUTHORIZED)
	@ApiResponse(responseCode = RouteConstants.HTTP_BAD_REQUEST)
	@ApiResponse(responseCode = RouteConstants.HTTP_UNPROCESSABLE_ENTITY)
	public ResponseEntity<DistListUpdateResponse> uploadDistListInfo(
			@RequestHeader(value = "ttsession", required = false) String ttsession,
			@RequestBody DistListUpdateRequest request) throws AtWinXSException {

		LOGGER.debug("In updateDistList()");
		SessionContainer sc = getSessionContainer(ttsession);
		DistListUpdateResponse response = deliveryInformationService.processUseThisList(request, sc);
		response.setSuccess(true);
		return new ResponseEntity<>(response, (response.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY));
	}
}

