/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/24/23				L De Leon				CAP-39371					Initial Version
 *	04/20/23				S Ramachandran			CAP-39973					added Company Master Address search
 *	05/15/23				L De Leon				CAP-40324					Added saveDeliveryInformation() method
 *	11/16/23				L De Leon				CAP-45180					Added getCountries() method
 * 	11/16/23				Krishna Natarajan		CAP-45181					Added saveDeliveryInformation() method
 *	11/20/23				Satishkumar A			CAP-38135					C1UX BE - Modification of Manual Enter Address to use new USPS validation
 *	03/15/24				C Codina				CAP-47778					Added getDistributionListRecordCount() method
 *	03/18/24				S Ramachandran			CAP-48002					Added service method to upload new Distlist file and to get worksheet names
 *	03/21/24				M Sakthi				CAP-47998					1UX BE - Create API to save dist lists fields to ManagedListSession.
 *	03/21/24				Satishkumar A			CAP-47389					C1UX API - Create API to retrieve Dist List addresses
 *	03/28/24				S Ramachandran			CAP-48277					declared service method to uploadDistList and return mapperData.
 *	03/29/24				Krishna Natarajan		CAP-47391					Added a new method processUseThisList
 *	04/01/24				Satishkumar A			CAP-48123					C1UX BE - Create API to retrieve Dist List addresses.
 *	04/02/24				N Caceres				CAP-48365					Added throw exception for processUseThisList
 *	04/08/24				S Ramachandran			CAP-48434					Modified uploadDistList service to go into a specific List for mapping
 */
package com.rrd.c1ux.api.services.checkout;

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
import com.rrd.c1ux.api.models.orders.ordersearch.CountriesResponse;
import com.rrd.c1ux.api.models.orders.ordersearch.CountryCodeRequest;
import com.rrd.c1ux.api.models.orders.ordersearch.StatesResponse;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;

public interface CODeliveryInformationService {

	public CODeliveryInformationResponse getDeliveryInformation(SessionContainer sc) throws AtWinXSException;
	public CompanyMasterAddressSearchResponse searchCompanyMasterAddresses(SessionContainer sc, 
			CompanyMasterAddressSearchRequest request) throws AtWinXSException;

	// CAP-40324 //CAP-38135
	public CODeliveryInformationSaveResponse saveDeliveryInformation(SessionContainer sc,
			CODeliveryInformationSaveRequest deliveryInfoSaveRequest, boolean validationUSPS) throws AtWinXSException;

	// CAP-45180
	public CountriesResponse getCountries(OEOrderSessionBean oeSessionBean) throws AtWinXSException;
	
	// CAP-45181
	public StatesResponse getStates(CountryCodeRequest request) throws AtWinXSException;
	
	//CAP-47840
	public DistributionListResponse getManagedLists(SessionContainer sc, boolean isOrderFromaFile) throws AtWinXSException;

	//CAP-47778
	public DistListCountResponse getDistributionListRecordCount(SessionContainer sc, DistListCountRequest request)throws AtWinXSException;
	
	// CAP-48002
	public WorksheetResponse uploadDistListFileAndGetWorksheetName(SessionContainer sc, WorksheetRequest request)
			throws AtWinXSException;

	//CAP-47998
	public SaveManagedFieldsResponse saveManagedListFields(SessionContainer sc, CreateListVarsRequest request)throws AtWinXSException;
	
	//CAP-47389
	public DistListAddressResponse getDistributionListAddresses(SessionContainer sc, DistListAddressRequest request)throws AtWinXSException, CPRPCException;
	
	// CAP-48277, CAP-48434
	public UploadDistListResponse uploadDistList(SessionContainer sc, UploadDistListRequest uploadDistListRequest,
			boolean isOrderFromaFile, boolean isDistListWithItemQty) throws AtWinXSException;
	
	//CAP-47391
	public DistListUpdateResponse processUseThisList(DistListUpdateRequest distListUpdateRequest, SessionContainer sc) throws AtWinXSException;
	
	//CAP-48202
	public SaveMappingResponse saveListMappings(SessionContainer sc, SaveMappingRequest request, boolean isDistListWithItemQty, boolean isOrderFromaFile, String listID) throws AtWinXSException, CPRPCException;

}
