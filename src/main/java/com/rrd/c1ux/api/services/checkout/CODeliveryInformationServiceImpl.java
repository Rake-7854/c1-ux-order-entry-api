/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	03/24/23				L De Leon				CAP-39371					Initial Version - Recreated methods from CP to retrieve delivery information
 *	04/20/23				S Ramachandran			CAP-39973					added Company Master Address search
 *	04/27/23				A Salcedo				CAP-40193					Added translation to response.
 *	05/18/23				L De Leon				CAP-40324					Added save delivery information implementation
 *	05/18/23				Sakthi M				CAP-40547					C1UX BE - API Fixes - Error checks for valid and non-submitted order not coded in Delivery and Order info load APIs
 *	06/02/23				L De Leon				CAP-40324					Added same changes in CAP-40324 in saveDeliveryInformation() method
 *	06/06/23				A Boomker				CAP-41092					Change validateSaveRequest() to exclude validation of CML ship to address fields
 *	06/08/23				A Boomker				CAP-41266					Add handling for PRV address source
 *	06/16/23				A Boomker				CAP-41394					Add handling for previous address loading from DB
 *	06/16/23				Satishkumar A			CAP-41094					API Change - Add overrideUSPSWarning boolean flag to request in Delivery Save API to override USPS failures and save
 *	07/11/23				Satishkumar A			CAP-41970					Moved getGeographicLabelsAndStateList(AppSessionBean appSessionBean) method from this class to super class BaseOEService.
 *	07/07/23				A Boomker				CAP-41972					Changes to add bill tos to get delivery response
 *	07/24/23				Krishna Natarajan		CAP-42453 					Added additional condition to check the WcssShipToNbr is not null
 * 	07/25/23				Krishna Natarajan		CAP-42241					Update USPS correct state/city/zip code back into request and response objects
 * 	08/07/23				Krishna Natarajan		CAP-42675					updated constants and method to handle the multiple/ mixed USPS error
 *  08/11/23				Krishna Natarajan		CAP-42862					Added condition to handle the Address Not Found - USPS error
 *  08/14/23				C Codina				CAP-41550					API Change - Change save delivery Info to support new address source type of PAB
 *	09/11/23				Krishna Natarajan		CAP-43698					Adding showPayment boolean flag to the response
 *  10/03/23				Krishna Natarajan		CAP-44343					Changing the check on invalid ID to profile ID >= -2 to enable using extended address while saving delivery info
 *  10/03/23				Krishna Natarajan		CAP-44363					Handling allowPAB flag according to allowManual flag
 *  10/05/23				T Harmon				CAP-44416					Added code to save address to PAB from manually enter address
 *  10/25/23				Krishna Natarajan		CAP-44865					Added code to handle nulltoempty for the preferred addresses
 *  11/08/23				Krishna Natarajan		CAP-45143 					Removed the section of code that adds the default billTo address in preferred shipping address
 *	11/16/23				L De Leon				CAP-45180					Added getCountries() method
 *  11/07/23				S Ramachandran			CAP-44961 					USPS validation in save PAB and to show suggested address
 *	11/16/23				Krishna Natarajan		CAP-45181					Added getStates() method
 *	11/20/23				Satishkumar A			CAP-38135					C1UX BE - Modification of Manual Enter Address to use new USPS validation
 *	11/29/23				Satishkumar A			CAP-45375					C1UX BE - Modify the errors returned from the USPS validation to be translated
 *	11/29/23				Satishkumar A			CAP-45611					C1UX_Story_Checkout details - On second call of "Use this address" user cannot skip USPS Validation and route to Delivery page
 *	11/27/23				S Ramachandran			CAP-45374 					USPS validated Suggested Address need to be split of address over 30 characters
 *	12/18/23				M Sakthi				CAP-45544					C1UX BE - Fix code to only search/edit/save for the originator PAB addresses in OOB mode
 *	12/22/23				S Ramachandran			CAP-46081					Modify existing validateUSAddressV1 method to use common method from uspsValidationService for USPS validation
 *	01/02/24				Krishna Natarajan		CAP-46134 					corrected block as WCSS Ship To Number - Not Populated for DEF
 *  01/22/24				T Harmon				CAP-46651					Added code to fix delta issue in regards to manual address
 *  03/14/24				M Sakthi				CAP-47840					C1UX BE - Create API to retrieve list of Distribution Lists.
 *  03/18/24				S Ramachandran			CAP-48002					Added service method to upload new Distlist file and get worksheet names
 *  03/21/24				M Sakthi				CAP-47998					1UX BE - Create API to save dist lists fields to ManagedListSession.
 *  03/21/24				Satishkumar A			CAP-47389					C1UX API - Create API to retrieve Dist List addresses.
 *  03/28/24				S Ramachandran			CAP-48277					Added service method Impl to uploadDistList and return mapperData.
 *	03/29/24				Krishna Natarajan		CAP-47391					Added a skeleton for the new method processUseThisList
 *	04/01/24				Satishkumar A			CAP-48123					C1UX BE - Create API to retrieve Dist List addresses.
 *	04/02/24				Krishna Natarajan		CAP-48380					Modified method and added logic to processShowPaymentInfo to set setShowpayment flag
 *	04/02/24				N Caceres				CAP-48365					Added business logic for processUseThisList
 *	04/03/24				M Sakthi				CAP-48202					C1UX BE - Create API to save columns to map
 *	04/04/24				S Ramachandran			CAP-48412					Validate list has mapping records in XST413 Table and add a flag.
 *	04/08/24				S Ramachandran			CAP-48434					Modified uploadDistList service to go into a specific List for mapping
 *  04/12/24				Krishna Natarajan		CAP-48606					Added a new checkDistListSharedOrPrivate field doNotShareListsInd to update BU Manage List admin settings
 *  04/16/24				Krishna Natarajan		CAP-48728					Added response in the parameter in the other called method processExcelCSV and throw 422 for invalid file
 *  04/17/24				A Salcedo				CAP-48725					Update saveDeliveryInformation() to update order header scenario number.
 *  04/18/24				Krishna Natarajan		CAP-48744					Added a method to check and update the address source to D if scenario is updated as 3 as to dist list
 *  04/18/24				Krishna Natarajan		CAP-48777                   Added a method validateListNameCharacters to validate no special characters on List Name
 *  04/19/24				A Salcedo				CAP-48794 					Remove recordCnt check.
 *  04/22/24				Krishna Natarajan		CAP-48846					Added line of code to set a field DistributionListTypeCode
 *  04/19/24				S Ramachandran			CAP-48277					Revoked sonar issue fix by modifying Arraylist to Vector for CP Code Compatibility
 *  04/23/24				S Ramachandran			CAP-48855					Update ORGN_EXCEL_FILE_NM and IS_PVT_IN in XST030
 *  04/23/24				A Salcedo				CAP-48898					Update rowNotAdded logic.
 *  04/25/24				A Salcedo				CAP-48794					Fix getDistListRecordCount for CSV/1sheet excel.
 *	05/09/24				A Boomker				CAP-46517, CAP-46518, CAP-42227 	Add check for valid cust doc merge UI if not in distribution
 *  05/09/24				T Harmon				CAP-48794					Another fix due to issue in test
 * 	05/28/24				A Boomker				CAP-48604					Moved some methods to base for cust doc reuse.
 *  05/29/24				S Ramachandran			CAP-49714					Fix - to getlist Mapping Details from CSV file upload cannot be mapped
 *  05/30/24				A Salcedo				CAP-49757					Fix updateDistributionListDetails() for non-dist list orders.  
 *  06/05/24				Krishna Natarajan		CAP-49902					Added a new method to get the preferred shipping address (irrespective of option selected) in case of EDF only order
 *  06/26/24				Krishna Natarajan		CAP-49902					Added a new variable and set if order is efdOnly 
 */
package com.rrd.c1ux.api.services.checkout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.ModelConstants;
import com.rrd.c1ux.api.models.admin.PABSaveRequest;
import com.rrd.c1ux.api.models.admin.PABSaveResponse;
import com.rrd.c1ux.api.models.checkout.COAltColumnNameWrapperCellData;
import com.rrd.c1ux.api.models.checkout.COColumnNameWrapperCellData;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationResponse;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveRequest;
import com.rrd.c1ux.api.models.checkout.CODeliveryInformationSaveResponse;
import com.rrd.c1ux.api.models.checkout.COListDataMapper;
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
import com.rrd.c1ux.api.models.usps.USPSValidationResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.admin.SelfAdminService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.locators.ManageListsLocatorService;
import com.rrd.c1ux.api.services.orderentry.locator.OEShoppingCartComponentLocatorService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.c1ux.api.services.usps.USPSValidationService;
import com.rrd.c1ux.api.util.COListsUtil;
import com.rrd.c1ux.api.util.DeliveryOptionsUtil;
import com.rrd.c1ux.api.util.SelfAdminUtil;
import com.rrd.custompoint.admin.profile.entity.CorporateProfile;
import com.rrd.custompoint.admin.profile.entity.ExtendedProfile;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.common.entity.Address;
import com.rrd.custompoint.gwt.common.entity.AddressSearchResult;
import com.rrd.custompoint.gwt.common.entity.Item;
import com.rrd.custompoint.gwt.common.entity.MessageBean;
import com.rrd.custompoint.gwt.common.exception.CPRPCException;
import com.rrd.custompoint.gwt.common.util.CountryBean;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.rrd.custompoint.gwt.deliveryoptions.client.DeliveryOptionsConstants;
import com.rrd.custompoint.gwt.deliveryoptions.widget.BillToAddress;
import com.rrd.custompoint.gwt.listscommon.entity.AddressCheckOutSettings;
import com.rrd.custompoint.gwt.listscommon.entity.LocationCodeOption;
import com.rrd.custompoint.gwt.listscommon.lists.ManagedList;
import com.rrd.custompoint.gwt.listscommon.lists.ManagedListImpl;
import com.rrd.custompoint.gwt.listscommon.mapping.AltColumnNameWrapperCell;
import com.rrd.custompoint.gwt.listscommon.mapping.AltColumnNameWrapperCellData;
import com.rrd.custompoint.gwt.listscommon.mapping.ColumnNameWrapperCell;
import com.rrd.custompoint.gwt.listscommon.mapping.ColumnNameWrapperCellData;
import com.rrd.custompoint.gwt.listscommon.mapping.LMapperData;
import com.rrd.custompoint.gwt.listscommon.mapping.ListMapperData;
import com.rrd.custompoint.orderentry.ao.KnownAddressDetailsFormBean;
import com.rrd.custompoint.orderentry.ao.KnownAddressListFormBean;
import com.rrd.custompoint.orderentry.ao.OEMappedVariableResponseBean;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.UserInterface.MergeOption;
import com.rrd.custompoint.orderentry.entity.AddressSourceSettings;
import com.rrd.custompoint.orderentry.entity.AddressUIFields;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.rrd.custompoint.orderentry.entity.DeliveryOptions;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails.AltColumnNames;
import com.rrd.custompoint.orderentry.entity.DistributionListDetailsImpl;
import com.rrd.custompoint.orderentry.entity.OrderDetailsBillingInfo;
import com.rrd.custompoint.orderentry.entity.OrderFromaFileListDetails;
import com.rrd.custompoint.orderentry.entity.OrderLines;
import com.rrd.custompoint.orderentry.entity.ProfileSelection;
import com.rrd.custompoint.orderentry.entity.SiteListMapping;
import com.rrd.custompoint.orderentry.entity.SiteListMappingImpl;
import com.rrd.custompoint.orderentry.util.OrderAddressSearchCriteria;
import com.wallace.atwinxs.admin.locator.PersonalAddressComponentLocator;
import com.wallace.atwinxs.admin.util.AdminConstant;
import com.wallace.atwinxs.admin.vo.PersonalAddressVO;
import com.wallace.atwinxs.admin.vo.PersonalAddressVOKey;
import com.wallace.atwinxs.admin.vo.ProfileVOKey;
import com.wallace.atwinxs.catalogs.locator.CMCatalogComponentLocator;
import com.wallace.atwinxs.catalogs.session.ItemImagesVOFilter;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVO;
import com.wallace.atwinxs.catalogs.vo.ItemImagesVOKey;
import com.wallace.atwinxs.framework.session.ApplicationVolatileSession;
import com.wallace.atwinxs.framework.session.BaseSession;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.SessionHandler;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.CorporateProfileBean;
import com.wallace.atwinxs.framework.util.FileUtils;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.PersonalProfileBean;
import com.wallace.atwinxs.framework.util.PropertyUtil;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.TranslationTextConstants;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.UtilCountryInfo;
import com.wallace.atwinxs.framework.util.XSProperties;
import com.wallace.atwinxs.framework.util.fileupload.FileDownload;
import com.wallace.atwinxs.framework.util.fileupload.FileUploadConstants;
import com.wallace.atwinxs.framework.util.mapper.ColumnNameWrapper;
import com.wallace.atwinxs.framework.util.mapper.Mapper;
import com.wallace.atwinxs.framework.util.mapper.MapperConstants;
import com.wallace.atwinxs.framework.util.mapper.MapperData;
import com.wallace.atwinxs.framework.util.mapper.MapperInput;
import com.wallace.atwinxs.framework.util.mapper.MapperInputFactory;
import com.wallace.atwinxs.interfaces.ICatalog;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.interfaces.IManageListAdmin;
import com.wallace.atwinxs.interfaces.IOEManageOrdersComponent;
import com.wallace.atwinxs.interfaces.IOEShoppingCartComponent;
import com.wallace.atwinxs.interfaces.IPersonalAddress;
import com.wallace.atwinxs.lists.ao.ListsBaseAssembler;
import com.wallace.atwinxs.lists.ao.ManageListsResponseBean;
import com.wallace.atwinxs.lists.ao.UploadListAssembler;
import com.wallace.atwinxs.lists.ao.UploadListRequestBean;
import com.wallace.atwinxs.lists.ao.UploadListResponseBean;
import com.wallace.atwinxs.lists.ao.ViewListsAssembler;
import com.wallace.atwinxs.lists.ao.ViewListsRequestBean;
import com.wallace.atwinxs.lists.ao.ViewListsResponseBean;
import com.wallace.atwinxs.lists.locator.ListsAdminLocator;
import com.wallace.atwinxs.lists.locator.ManageListsLocator;
import com.wallace.atwinxs.lists.session.ManageListsAdminSessionBean;
import com.wallace.atwinxs.lists.session.ManageListsSession;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.util.ManageListsUtil;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.lists.vo.ListVOKey;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVO;
import com.wallace.atwinxs.lists.vo.ManageListsBusinessUnitPropsVOKey;
import com.wallace.atwinxs.locale.ao.StateBean;
import com.wallace.atwinxs.orderentry.admin.util.OrderReferenceFieldList;
import com.wallace.atwinxs.orderentry.ao.OEAddressSearchFormBean;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OEBillToAddressResponseBean;
import com.wallace.atwinxs.orderentry.ao.OECheckoutAssembler;
import com.wallace.atwinxs.orderentry.ao.OEDistributionListDetailsFormBean;
import com.wallace.atwinxs.orderentry.ao.OEListAssembler;
import com.wallace.atwinxs.orderentry.ao.OENewAddressFormBean;
import com.wallace.atwinxs.orderentry.ao.OEPrepopulatedAddressesResponseBean;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;
import com.wallace.atwinxs.orderentry.dao.OrderAddressDAO;
import com.wallace.atwinxs.orderentry.dao.OrderHeaderDAO;
import com.wallace.atwinxs.orderentry.locator.OEManageOrdersComponentLocator;
import com.wallace.atwinxs.orderentry.session.OEApprovalOrderCheckoutSessionBean;
import com.wallace.atwinxs.orderentry.session.OECustomDocOrderLineMapSessionBean;
import com.wallace.atwinxs.orderentry.session.OEDistributionListBean;
import com.wallace.atwinxs.orderentry.session.OEOrderFromFileBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OEResolvedUserSettingsSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.session.SubscriptionCheckoutSessionBean;
import com.wallace.atwinxs.orderentry.util.DeliveryOptionsFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;
import com.wallace.atwinxs.orderentry.vo.BillAndShipAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVO;
import com.wallace.atwinxs.orderentry.vo.OrderAddressVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderBillingVO;
import com.wallace.atwinxs.orderentry.vo.OrderFilterVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVO;
import com.wallace.atwinxs.orderentry.vo.OrderHeaderVOKey;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVO;
import com.wallace.atwinxs.orderentry.vo.OrderShippingVOKey;

@Service
public class CODeliveryInformationServiceImpl extends BaseOEService implements CODeliveryInformationService {

	private static final String DIST_LIST_ITEMS = "DLI";
	private static final String DIST_LIST_ADDRESS = "DLA";
	private static final Logger logger = LoggerFactory.getLogger(CODeliveryInformationServiceImpl.class);
	private static final String ERROR_LBL = "Error: ";

	private static final String htmlTxt="&raquo;";
	private static final String DIST_LIST_ID_URL_PARAM = "&distListID=";
	// CAP-48777
	private static final int[] INVALID_CHARACTERS = new int[] { ',', ';', '"', '^', '<', '>', '?', '[', ']', ':', '*',
			'!', '~', '`', '@', '$', '%', '&', '=', '+', '{', '}', '|', '/', '\\' };

	//CAP-44416
	private final SelfAdminService selfAdminService;
	private final USPSValidationService uspsValidationService;
	private final ManageListsLocatorService manageListsLocatorService;

	//CAp-48497
	private final OEShoppingCartComponentLocatorService oeShoppingCartComponentLocatorService;

	enum AddressSources {
		PRV(DeliveryOptionsConstants.ADDR_SRC_PREV_ADDR_CD), DEF(DeliveryOptionsConstants.ADDR_SRC_DEFAULT_ADDR_CD),
		MEN(DeliveryOptionsConstants.ADDR_SRC_MANUAL_ENTER_ADDR_CD),
		CML(DeliveryOptionsConstants.ADDR_SRC_COMPANY_MASTER_LIST_ADDR_CD), UNABLE_TO_DETERMINE(""),
		PAB(DeliveryOptionsConstants.ADDR_SRC_ALT_PERSONAL_ADDR_CD); // this will mean
																										// an error is
																										// thrown if the
																										// request does
																										// not contain
																										// enough
																										// information
																										// to know where
																										// to go back to

		private String code;

		private AddressSources(String code) {
			this.code = code;
		}

		@Override
		public String toString() {
			return code;
		}
	}

	enum ZipRequiredCountries {
		USA, CAN, AUS, MEX, CHL, BRA
	}

	protected CODeliveryInformationServiceImpl(TranslationService translationService, SelfAdminService pSelfServiceAdmin,
			USPSValidationService pUSPSValidationService,ObjectMapFactoryService objectMapFactoryService,
			OEShoppingCartComponentLocatorService poeShoppingCartComponentLocatorService,
			ManageListsLocatorService manageListsLocatorService) {
		super(translationService,objectMapFactoryService);
		selfAdminService = pSelfServiceAdmin; // CAP-44416
		uspsValidationService = pUSPSValidationService;
		oeShoppingCartComponentLocatorService = poeShoppingCartComponentLocatorService;
		this.manageListsLocatorService = manageListsLocatorService;

	}

	@Override
	public CODeliveryInformationResponse getDeliveryInformation(SessionContainer sc) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		CODeliveryInformationResponse response = new CODeliveryInformationResponse();

		// CAP-40547-C1UX BE - API Fixes - Error checks for valid and non-submitted
		// order not coded in Delivery and Order info load APIs
		if (!validateOrder(response, sc, appSessionBean)) {
			return response;
		}

		OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean,
				appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());
		IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());

		DeliveryOptionsFormBean deliveryOptions = populateDeliveryOptions(checkoutAssembler, volatileSessionBean,
				punchoutSessionBean, oeOrderSessionBean, appSessionBean);

		AddressCheckOutSettings checkoutSettings = getCheckOutSettings(sc, ordersService, volatileSessionBean,
				oeOrderSessionBean, appSessionBean);

		List<Address> preferredShippingAddress = getDefaultAddress(volatileSessionBean, oeOrderSessionBean,
				appSessionBean);

		List<BillToAddress> billToAddresses = new ArrayList<>();
		if (isRetrieveBillToList(checkoutSettings, preferredShippingAddress)) {
			billToAddresses = getBillToList(volatileSessionBean, oeOrderSessionBean, appSessionBean);
		}

		List<NameValuePair<String>> countryOptions = new ArrayList<>();
		if (checkoutSettings.isAllowManual()
				|| (checkoutSettings.isAllowMasterList() && checkoutSettings.isAllowMLCountry())
				|| !preferredShippingAddress.isEmpty()) {
			countryOptions = getCountryList(oeOrderSessionBean);
		}

		List<NameValuePair<CountryBean>> countriesAndStates = new ArrayList<>();
		if (checkoutSettings.isAllowManual()
				|| (checkoutSettings.isAllowMasterList() && checkoutSettings.isAllowMLState())
				|| !preferredShippingAddress.isEmpty()) {
			countriesAndStates = getGeographicLabelsAndStateList(appSessionBean);
		}

		List<Address> companyMasterListAddresses = new ArrayList<>();
		if (checkoutSettings.isAllowMasterList() && !checkoutSettings.isAllowMasterListSearchCriteria()) {
			companyMasterListAddresses = searchMasterList(volatileSessionBean, oeOrderSessionBean, appSessionBean,
					null);
		}

		// CAP-41972 - make sure bill to is present
		updateMissingBillTo(deliveryOptions, appSessionBean, volatileSessionBean);

		response.setDeliveryOptions(deliveryOptions);
		response.setCheckoutSettings(checkoutSettings);
		response.setPreferredShippingAddress(preferredShippingAddress);
		response.setBillToAddresses(billToAddresses);
		response.setCountryOptions(countryOptions);
		response.setCountriesAndStates(countriesAndStates);
		response.setCompanyMasterListAddresses(companyMasterListAddresses);
		// CAP-41972
		response.setDefaultBillToAddressForNewShipTo(getDefaultBillToAddressForNewShipTo(checkoutSettings,
				appSessionBean, oeOrderSessionBean, volatileSessionBean));
		response.setCurrentBillToAddress(getCurrentBillToAddress(appSessionBean, deliveryOptions, oeOrderSessionBean));
		response.setSuccess(true);

		// CAP-40193
		Properties props = translationService.getResourceBundle(appSessionBean,
				SFTranslationTextConstants.DELIVERY_INFO_VIEW_NAME);
		response.setTranslationDeliveryInfo(translationService.convertResourceBundlePropsToMap(props));

		// CAP-43698
		processShowPaymentInfo(response, sc);

		//CAP-48606
		checkDistListSharedOrPrivate(response, appSessionBean);

		//CAP-48744
		checkAndUpdateDistListInfo(response, oeOrderSessionBean);

		//get the order line VO
		checkAndUpdatePreferredAddress(response,appSessionBean,volatileSessionBean);//CAP-49902
		return response;
	}
	
	// CAP-49902
	protected void checkAndUpdatePreferredAddress(CODeliveryInformationResponse response, AppSessionBean appSessionBean,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException {
		if (volatileSessionBean.getOrderId() != null) {
			OrderLines orderLines = ObjectMapFactory.getEntityObjectMap().getEntity(OrderLines.class,
					appSessionBean.getCustomToken());
			orderLines.populate(volatileSessionBean.getOrderId());
			response.setEfdOnly(orderLines.isEDeliveryOnly());//CAP-49902
			if (orderLines.isEDeliveryOnly() && response.getPreferredShippingAddress().isEmpty()) {
				CorporateProfileBean corporateProfile = appSessionBean.getCorporateProfile();
				if (corporateProfile != null) {
					Address preferredAddr = DeliveryOptionsUtil.convertCorporateProfileBeanToAddress(corporateProfile);
					preferredAddr.setSourceDefAddr(DeliveryOptionsConstants.ADDR_SRC_DEF_PREFERRED);
					response.getPreferredShippingAddress().add(preferredAddr);
				}
				response.getCheckoutSettings().setDefaultAddressSourceCode("DEF");
			}
		}
	}

	// CAP-41972 - added method to deal with missing info on bill tos
	protected void updateMissingBillTo(DeliveryOptionsFormBean deliveryOptions, AppSessionBean appSessionBean,
			VolatileSessionBean vsb) throws AtWinXSException {
		if ((Util.isBlankOrNull(deliveryOptions.getBillToCode())) && (deliveryOptions.isHasPreviousAddress())) { // if
																													// we
																													// have
																													// a
																													// previous
																													// address,
																													// we
																													// must
																													// have
																													// a
																													// bill
																													// to
																													// code
			try {
				IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator
						.locate(appSessionBean.getCustomToken());
				OrderHeaderVO currentHeader = ordersService
						.getOrderHeader(new OrderHeaderVOKey(vsb.getOrderId().intValue()));
				if (currentHeader != null) {
					deliveryOptions.setBillToCode(
							currentHeader.getCorporateNum() + AtWinXSConstant.COLON + currentHeader.getSoldToNum());
				}
			} catch (AtWinXSException e) {
				logger.error(e.getMessage());
			}
		}
	}

	// CAP-41972 - added method to deal with missing info on bill tos
	protected OrderAddressVO getCurrentBillToAddress(AppSessionBean appSessionBean,
			DeliveryOptionsFormBean deliveryOptions, OEOrderSessionBean oeSessionBean) {
		if (!Util.isBlankOrNull(deliveryOptions.getBillToCode())) {
			try {
				IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator
						.locate(appSessionBean.getCustomToken());
				String selectedBillToCode = deliveryOptions.getBillToCode();
				String corpNum = selectedBillToCode.substring(0, 10);
				String soldToNum = selectedBillToCode.substring(11, selectedBillToCode.length());

				return ordersService.getAddress(new OrderAddressVOKey(corpNum, soldToNum, null),
						oeSessionBean.getOrderScenarioNumber());
			} catch (AtWinXSException e) {
				logger.error(e.getMessage());
			}
		}

		return null;
	}

	// CAP-41972 - added method to deal with missing info on bill tos
	protected OrderAddressVO getDefaultBillToAddressForNewShipTo(AddressCheckOutSettings checkoutSettings,
			AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean, VolatileSessionBean volatileSessionBean) {
		if (!Util.isBlankOrNull(checkoutSettings.getDefaultSoldToForNewAddress())) {
			try {
				IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator
						.locate(appSessionBean.getCustomToken());
				AddressSourceSettings addressSourceSettings = ObjectMapFactory.getEntityObjectMap()
						.getEntity(AddressSourceSettings.class, appSessionBean.getCustomToken());
				OrderFilterVO[] filterVOs = addressSourceSettings.getOrderFilters(oeSessionBean, ordersService);

				String defaultSoldToAddress = oeSessionBean.getUserSettings().getDefaultSoldToForNewAddress();
				String corpNum = defaultSoldToAddress.length() >= 10 ? defaultSoldToAddress.substring(0, 10)
						: appSessionBean.getCorporateNumber();
				String soldToNum = defaultSoldToAddress.length() >= 11 ? defaultSoldToAddress.substring(10)
						: AtWinXSConstant.EMPTY_STRING;

				String defaultBillToCode = getDefaultBillToCode(volatileSessionBean, appSessionBean, filterVOs,
						ordersService, corpNum, soldToNum);

				corpNum = defaultBillToCode.substring(0, 10);
				soldToNum = defaultBillToCode.substring(11, defaultBillToCode.length());

				return ordersService.getAddress(new OrderAddressVOKey(corpNum, soldToNum, null),
						oeSessionBean.getOrderScenarioNumber());
			} catch (AtWinXSException e) {
				logger.error(e.getMessage());
			}
		}
		return null;
	}

	protected boolean isRetrieveBillToList(AddressCheckOutSettings checkoutSettings,
			List<Address> preferredShippingAddress) {
		return (checkoutSettings.isAllowManual() && (checkoutSettings.getDefaultSoldToForNewAddress().isEmpty()
				|| checkoutSettings.isSoldToEditableForNewAddress()))
				|| (checkoutSettings.isAllowMasterList() && checkoutSettings.isAllowMLBillTo())
				|| !preferredShippingAddress.isEmpty()
				|| (!checkoutSettings.getDefaultSoldToForNewAddress().isEmpty() && !checkoutSettings.isSoldToEditableForNewAddress());  // CAP-46651 - Added last or condition here
	}

	protected DeliveryOptionsFormBean populateDeliveryOptions(OECheckoutAssembler checkoutAssembler,
			VolatileSessionBean volatileSessionBean, PunchoutSessionBean punchoutSessionBean,
			OEOrderSessionBean oeSession, AppSessionBean appSessionBean) throws AtWinXSException {

		DeliveryOptionsFormBean formBean = (DeliveryOptionsFormBean) objectMapFactoryService.getEntityObjectMap()
				.getEntity(DeliveryOptions.class, appSessionBean.getCustomToken());

		try {
			populateDeliveryOptionsWithPunchoutSettings(formBean, punchoutSessionBean, appSessionBean);

			if (oeSession != null && oeSession.getUserSettings() != null) {
				OEResolvedUserSettingsSessionBean userSettings = oeSession.getUserSettings();
				formBean.setAllowDisplayBillTo(userSettings.isShowBillToInfo() && !formBean.isPunchout());
				formBean.setAllowUserChangeDefBillTo(
						userSettings.isSoldToEditableForNewAddress() && !formBean.isPunchout());
				formBean.setShowAddressLine3(appSessionBean.isShowAddressLine3());
				formBean.setRequiredShipToAttn(Util.yToBool(oeSession.getUserSettings().getShipToAttnOptionCode()));

				if (Integer.valueOf(userSettings.getBillToAttnOption()) > 0) {
					OrderBillingVO orderBillVO = checkoutAssembler.getBillToAttentionText();

					if (orderBillVO != null) {
						formBean.setBillToAttention(orderBillVO.getSoldToAttentionTxt());
					}
				}
			}

			if (appSessionBean.getDefaultLocale().getISO3Country() != null) {
				formBean.setDefaultLocale(appSessionBean.getDefaultLocale().getISO3Country());
			}

			OrderHeaderVO headerInfo = null;
			headerInfo = getOrderHeaderInfo(checkoutAssembler, volatileSessionBean, oeSession, appSessionBean);

			formBean.setScenarioNumber(headerInfo.getOrderScenarioNum());
			formBean.setActionID("next");

			// CAP-41394 - look up specifically for saved addresses
			loadSavedOrderAddress(checkoutAssembler, volatileSessionBean.getOrderId().intValue(), formBean);
		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

		return formBean;
	}

	protected OrderHeaderVO getOrderHeaderInfo(OECheckoutAssembler checkoutAssembler,
			VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSession, AppSessionBean appSessionBean)
			throws AtWinXSException {
		OrderHeaderVO headerInfo;
		try {
			headerInfo = getOrderHeaderVo(checkoutAssembler, volatileSessionBean);
		} catch (AtWinXSMsgException mess) {
			if (oeSession != null) {
				oeSession.setApprovalCheckoutSession(null);
			}
			checkoutAssembler.clearAddressInfo(oeSession, appSessionBean.getLoginID());
			headerInfo = getOrderHeaderVo(checkoutAssembler, volatileSessionBean);
		}
		return headerInfo;
	}

	protected AddressCheckOutSettings getCheckOutSettings(SessionContainer sc, IOEManageOrdersComponent ordersService,
			VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean)
			throws AtWinXSException {

		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		AddressCheckOutSettings settings = new AddressCheckOutSettings();

		AddressSourceSettings addressSourceSettings = null;

		try {
			if (sc.getModuleSession().getParameter("addressSourceSettings") instanceof AddressSourceSettings) {
				addressSourceSettings = (AddressSourceSettings) sc.getModuleSession()
						.getParameter("addressSourceSettings");
			}

			if (addressSourceSettings == null) {
				addressSourceSettings = ObjectMapFactory.getEntityObjectMap().getEntity(AddressSourceSettings.class,
						appSessionBean.getCustomToken());
				addressSourceSettings.populate(appSessionBean.getEncodedSessionId(), userSettings, volatileSessionBean,
						appSessionBean, oeSessionBean);
			}

			settings.setDefaultSoldToForNewAddress(userSettings.getDefaultSoldToForNewAddress());
			settings.setSoldToEditableForNewAddress(userSettings.isSoldToEditableForNewAddress());
			settings.setAllowSingleKnownAddr(addressSourceSettings.isHasSingleKnownAddress());
			settings.setAllowMultipleKnownAddr(addressSourceSettings.isHasMultipleKnownAddress());

			/*
			 * Out of Scope for Phase 1B if settings isAllowSingleKnownAddr and !settings
			 * isAllowBillToAddress and addressSourceSettings isSkipToOrderDetails
			 * saveSingleKnownAddress checkoutAssembler, addressSourceSettings
			 * getKnownAddresses, appSessionBean, oeSessionBean
			 */

			settings.setAllowDefault(addressSourceSettings.isHasDefaultAddress());
			settings.setAllowDistListAddr(addressSourceSettings.isHasDistListAddr());
			settings.setAllowDistListItems(addressSourceSettings.isHasDistListItemsQty());
			settings.setAllowManual(addressSourceSettings.isHasManualAddAddress());
			settings.setAllowMasterList(addressSourceSettings.isHasMasterListAddress());
			//CAP-44363
			if(!addressSourceSettings.isHasManualAddAddress()) {
			settings.setAllowPAB(false);
			}else {
				settings.setAllowPAB(addressSourceSettings.isHasPAB());
			}
			settings.setKeyProfilesSelected(DeliveryOptionsUtil.convertKeyProfileSelection(appSessionBean,
					oeSessionBean.getProfileSelections()));
			settings.setLoggedInProfileNumber(appSessionBean.getProfileNumber());
			settings.setAllowCRM(addressSourceSettings.isHasCRM());
			settings.setCRMName(addressSourceSettings.getCrmName());
			settings.setAllowEditableCRMAdd(addressSourceSettings.isAllowEditableCRMAdd());
			settings.setBadAdminError(addressSourceSettings.isBadAdminError());
			settings.setAllowSkipToOrderDetails(addressSourceSettings.isSkipToOrderDetails());
			settings.setAllowBillToAddress(addressSourceSettings.isHasBillToAddress());

			// Criteria
			settings.setAllowMLCountry(addressSourceSettings.isHasMasterListCountryCriteria());
			settings.setAllowMLName1(addressSourceSettings.isHasMasterListName1Criteria());
			settings.setAllowMLState(addressSourceSettings.isHasMasterListStateCriteria());
			settings.setAllowMLZip(addressSourceSettings.isHasMasterListZipCriteria());
			settings.setAllowMLBillTo(addressSourceSettings.isHasMasterListBillToCriteria());
			settings.setAllowMLName2(addressSourceSettings.isHasMasterListName2Criteria());

			setLocationCodeOptions(settings, appSessionBean, oeSessionBean);
			settings.setAllowMLLocationCode1(addressSourceSettings.isHasMasterListLocationCode1Criteria());
			settings.setAllowMLLocationCode2(addressSourceSettings.isHasMasterListLocationCode2Criteria());
			settings.setAllowMLLocationCode3(addressSourceSettings.isHasMasterListLocationCode3Criteria());
			settings.setAllowMasterListSearchCriteria(addressSourceSettings.isHasMasterListSearchCriteria());

			// Ship To Attention
			settings.setDefaultShipToAttnForAddrInd(addressSourceSettings.isDefaultShipToAttnForAddrInd());
			settings.setShipToAttnLabel(addressSourceSettings.getShipToAttnLabel());
			settings.setShipToAttnOptionCode(addressSourceSettings.getShipToAttnOptionCode());
			settings.setShipToNames(addressSourceSettings.getShipToAttnList());
			settings.setForceShipToAttn(addressSourceSettings.isForceShipToAttn());
			settings.setShipToAttnVal(addressSourceSettings.getShipToAttnVal());
			settings.setOrderFromFile(addressSourceSettings.isOrderFromFile());
			settings.setBillToAttLabel(addressSourceSettings.getBillToAttLabel());
			settings.setBillToAttOption(addressSourceSettings.getBillToAttOption());
			settings.setShowBillToAddress(addressSourceSettings.isShowBillToInfo());
			settings.setDefShipToAttnOrderRefVal(addressSourceSettings.getDefShipToAttnOrderRefVal());

			if (!Util.isBlankOrNull(appSessionBean.getSalesforceSignedRequestJson())) {
				settings.setSalesforceSignedRequestJson(true);
			}

			settings.setDefaultAddressSourceCode(addressSourceSettings.getDefaultAddressSourceCode());

			OrderFilterVO[] filterVOs = addressSourceSettings.getOrderFilters(oeSessionBean, ordersService);
			settings.setHasOrderFilter(filterVOs.length > 0);

			settings.setAllowCRMAlternateAddress(addressSourceSettings.isAllowCRMAlternateAddress());

			settings.setNameSplit(userSettings.isNameSplit());
			settings.setDfltAltProfAddBk(userSettings.isDfltAltProfAddBk());

			settings.setMultipleRecipientsEnabled(addressSourceSettings.isMultipleRecipientsEnabled());

			settings.setBuildAList(addressSourceSettings.isBuildAList());

			IManageListAdmin listAdmin = ListsAdminLocator.locate(appSessionBean.getCustomToken());
			ManageListsBusinessUnitPropsVO businessUnitPropsVO = listAdmin.getBuListDetails(
					new ManageListsBusinessUnitPropsVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID()));

			if (businessUnitPropsVO != null) {
				settings.setAllowSharedLists(!businessUnitPropsVO.isDoNotShareListsInd());
			}

			settings.setLineItemShipping(oeSessionBean.isLineItemShipping());
		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

		return settings;
	}

	protected void populateDeliveryOptionsWithPunchoutSettings(DeliveryOptionsFormBean formBean,
			PunchoutSessionBean punchoutSessionBean, AppSessionBean appSessionBean) throws AtWinXSException {
		if (punchoutSessionBean != null) {
			formBean.setPunchout(true);
			/*
			 * Out of Scope for Phase 1B if punchoutSessionBean isAllowFreightSelect and
			 * oeSession != null and userSettings != null and userSettings
			 * isAllowCarrierMethodSelection then setPunchoutButtonText
			 * TranslationTextConstants.TRANS_NM_PUNCHOUT_NEXT_CARRIER_BUTTON else
			 */ if (!Util.isBlankOrNull(punchoutSessionBean.getTransferText())) {
				formBean.setPunchoutButtonText(punchoutSessionBean.getTransferText());
			} else {
				formBean.setPunchoutButtonText(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
						appSessionBean.getCustomToken(),
						TranslationTextConstants.TRANS_NM_PUNCHOUT_DEFAULT_TRANSFER_BUTTON));
			}

			if (!Util.isBlankOrNull(punchoutSessionBean.getCancelButtonText())) {
				formBean.setPunchoutCancelButtonText(punchoutSessionBean.getCancelButtonText());
			} else {
				formBean.setPunchoutCancelButtonText(
						TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
								appSessionBean.getCustomToken(), TranslationTextConstants.TRANS_NM_CANCEL_BUTTON));
			}
		}
	}

	protected boolean hasPreviouslySelectedAddress(OECheckoutAssembler checkoutAssembler,
			IOEManageOrdersComponent ordersService, AppSessionBean appSessionBean, OEOrderSessionBean oeSession,
			PunchoutSessionBean punchoutSessionBean) throws AtWinXSException {

		boolean hasPresetAddress = false;

		OEPrepopulatedAddressesResponseBean presetAddressBean = null;
		String prepopulatedSrc = oeSession.getPrepopulatedSource();
		boolean hasPrepopulatedAddress = !Util.isBlankOrNull(prepopulatedSrc);

		KnownAddressListFormBean knowAddrListBean = ordersService.getKnownAddressListFormBean(appSessionBean, null,
				true);
		Collection<KnownAddressDetailsFormBean> knownAddressDetailsBean = knowAddrListBean
				.getKnownAddrDetailsFormBean();
		int knownAddressNum = (knownAddressDetailsBean != null) ? knownAddressDetailsBean.size() : -1;
		boolean hasSingleKnownAddr = (knownAddressNum == 1);

		if ((hasPrepopulatedAddress && !hasSingleKnownAddr) || (punchoutSessionBean != null)) {
			presetAddressBean = checkoutAssembler.getPrepopulatedAddressInfo(oeSession, false);
		}

		if (punchoutSessionBean == null) {
			hasPresetAddress = checkoutAssembler.hasPresetAddress(oeSession, presetAddressBean);
		} else if (presetAddressBean.getShippingAddress() != null) {
			hasPresetAddress = true;
		}

		if (!Util.isBlankOrNull(prepopulatedSrc)
				&& (presetAddressBean != null && (presetAddressBean.getShippingAddress() == null
						|| Util.isBlankOrNull(presetAddressBean.getShippingAddress().getAddressLine1())))) {
			return false;
		}

		return hasPresetAddress;
	}

	// CAP-41394 - logic needs to check just for saved address on order
	protected void loadSavedOrderAddress(OECheckoutAssembler checkoutAssembler, int orderID,
			DeliveryOptionsFormBean formBean) throws AtWinXSException {
		try {
			OrderAddressVO shippingAddress = checkoutAssembler.getShippingAddress(new OrderShippingVOKey(orderID));
			boolean hasPreviouslySelectedAddress = ((null != shippingAddress)
					&& (!Util.isBlankOrNull(shippingAddress.getCountry()))
					&& (!Util.isBlankOrNull(shippingAddress.getCity()))
					&& (!Util.isBlankOrNull(shippingAddress.getAddressLine1()))
					&& (!Util.isBlankOrNull(shippingAddress.getCustomerName1())));

			formBean.setHasPreviousAddress(hasPreviouslySelectedAddress);

			if (null != shippingAddress) {
				formBean.setSelected(true);
				formBean.setAddressLine1(Util.replaceCarriageReturns(shippingAddress.getAddressLine1()));
				formBean.setAddressLine2(Util.replaceCarriageReturns(shippingAddress.getAddressLine2()));
				formBean.setAddressLine3(Util.replaceCarriageReturns(shippingAddress.getAddressLine3()));
				formBean.setAddressSource(shippingAddress.getAddressSource());
				formBean.setShipToAttention(Util.replaceCarriageReturns(shippingAddress.getAttention()));
				formBean.setCity(Util.replaceCarriageReturns(shippingAddress.getCity()));
				formBean.setCountry(shippingAddress.getCountry());
				formBean.setShipToName1(Util.replaceCarriageReturns(shippingAddress.getCustomerName1()));
				formBean.setShipToName2(Util.replaceCarriageReturns(shippingAddress.getCustomerName2()));
				formBean.setCorporateNumber(shippingAddress.getKey().getCorporateNumber());
				formBean.setSoldToNumber(shippingAddress.getKey().getSoldToNumber());
				formBean.setShipToNum(shippingAddress.getKey().getWcssShipToNumber());
				formBean.setPhoneNumber(shippingAddress.getPhoneNumber());
				formBean.setStateOrProvince(shippingAddress.getState());
				formBean.setPostalCode(shippingAddress.getZip());
				formBean.setHasPassedZipValidation(shippingAddress.isValidAddress());
				formBean.setCrmRecordID(shippingAddress.getCrmRecordID());
				formBean.setCustomerRef1(shippingAddress.getCustomerRef1());
				formBean.setName1(Util.replaceCarriageReturns(shippingAddress.getFirstName()));
				formBean.setName2(Util.replaceCarriageReturns(shippingAddress.getLastName()));
			}
		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

	}

	// CAP-41266 - new version of the above method to be used to validate PRV
	// address source on save request
	protected OrderAddressVO getPreviouslySelectedAddress(OECheckoutAssembler checkoutAssembler,
			AppSessionBean appSessionBean, OEOrderSessionBean oeSession, PunchoutSessionBean punchoutSessionBean,
			int orderID, CODeliveryInformationSaveRequest request) throws AtWinXSException {

		OrderAddressVO shippingAddress = checkoutAssembler.getShippingAddress(new OrderShippingVOKey(orderID));
		if ((null != shippingAddress) && (!Util.isBlankOrNull(shippingAddress.getCountry()))
				&& (!Util.isBlankOrNull(shippingAddress.getCity()))
				&& (!Util.isBlankOrNull(shippingAddress.getAddressLine1()))
				&& (!Util.isBlankOrNull(shippingAddress.getCustomerName1()))) {
			OrderShippingVO shipVO = checkoutAssembler.getOrderShipping(orderID);
			if (shipVO != null) {
				request.setWcssShipToNbr(shipVO.getWcssShipToNum());
			}
			return shippingAddress;
		}

		OEPrepopulatedAddressesResponseBean presetAddressBean = null;
		String prepopulatedSrc = oeSession.getPrepopulatedSource();
		boolean hasPrepopulatedAddress = !Util.isBlankOrNull(prepopulatedSrc);

		IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());
		KnownAddressListFormBean knowAddrListBean = ordersService.getKnownAddressListFormBean(appSessionBean, null,
				true);
		Collection<KnownAddressDetailsFormBean> knownAddressDetailsBean = knowAddrListBean
				.getKnownAddrDetailsFormBean();
		int knownAddressNum = (knownAddressDetailsBean != null) ? knownAddressDetailsBean.size() : -1;
		boolean hasSingleKnownAddr = (knownAddressNum == 1);

		if ((hasPrepopulatedAddress && !hasSingleKnownAddr) || (punchoutSessionBean != null)) {
			presetAddressBean = checkoutAssembler.getPrepopulatedAddressInfo(oeSession, false);
		}

		if (punchoutSessionBean == null) {
			checkoutAssembler.hasPresetAddress(oeSession, presetAddressBean);
		}

		if ((presetAddressBean == null) || (presetAddressBean.getShippingAddress() == null)
				|| (Util.isBlankOrNull(presetAddressBean.getShippingAddress().getAddressLine1()))) {
			return null;
		}

		return new OrderAddressVO(AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING,
				AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING,
				presetAddressBean.getShippingAddress().getCustomerName1(),
				presetAddressBean.getShippingAddress().getCustomerName2(),
				presetAddressBean.getShippingAddress().getAddressLine1(),
				presetAddressBean.getShippingAddress().getAddressLine2(),
				presetAddressBean.getShippingAddress().getAddressLine3(),
				presetAddressBean.getShippingAddress().getCity(), presetAddressBean.getShippingAddress().getState(),
				presetAddressBean.getShippingAddress().getZip(), presetAddressBean.getShippingAddress().getCountry(),
				presetAddressBean.getShippingAddress().getPhoneNumber(),
				presetAddressBean.getShippingAddress().isValidAddress(),
				presetAddressBean.getShippingAddress().getAddressSource(),
				Util.safeStringToDefaultInt(presetAddressBean.getShippingAddress().getAddressReferenceID(),
						AtWinXSConstant.INVALID_ID),
				presetAddressBean.getShippingAddress().getCrmRecordId());
	}

	protected OrderHeaderVO getOrderHeaderVo(OECheckoutAssembler checkoutAssembler,
			VolatileSessionBean volatileSessionBean) throws AtWinXSException {

		return checkoutAssembler.getOrderHeader(volatileSessionBean.getOrderId());
	}

	protected void saveSingleKnownAddress(OECheckoutAssembler checkoutAssembler, KnownAddressListFormBean knownAddress,
			AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean) throws AtWinXSException {

		StringBuilder filterBillTo = new StringBuilder(32);
		OrderAddressVO dfltBillToFilterAddr = checkoutAssembler.retrieveDefaultBillTo(oeSessionBean);
		filterBillTo = checkoutAssembler.buildOrderFilterBillTo(filterBillTo, dfltBillToFilterAddr);
		checkoutAssembler.saveInfoForSingleKnownAddress(null, oeSessionBean, appSessionBean, knownAddress, filterBillTo,
				oeSessionBean.getProfileNumber());
	}

	protected void setLocationCodeOptions(AddressCheckOutSettings settings, AppSessionBean appSessionBean,
			OEOrderSessionBean oeSessionBean) {

		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
		boolean isMailMergeOnly = false;

		try {
			IOEManageOrdersComponent oeComp = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());
			isMailMergeOnly = oeComp.isMailMergeOnly(oeSessionBean.getOrderScenarioNumber());
		} catch (AtWinXSException ae) {
			isMailMergeOnly = false;
		}

		boolean canSearchByLocation1 = userSettings.isAllowSearchByCustRef1() && !isMailMergeOnly;
		boolean canSearchByLocation2 = userSettings.isAllowSearchByCustRef2() && !isMailMergeOnly;
		boolean canSearchByLocation3 = userSettings.isAllowSearchByCustRef3() && !isMailMergeOnly;

		OrderReferenceFieldList[] locationCode1List = new OrderReferenceFieldList[] {};
		OrderReferenceFieldList[] locationCode2List = new OrderReferenceFieldList[] {};
		OrderReferenceFieldList[] locationCode3List = new OrderReferenceFieldList[] {};
		if (userSettings != null) {
			if (userSettings.getLocationCode1List() != null) {
				locationCode1List = userSettings.getLocationCode1List();
			}
			if (userSettings.getLocationCode2List() != null) {
				locationCode2List = userSettings.getLocationCode2List();
			}
			if (userSettings.getLocationCode3List() != null) {
				locationCode3List = userSettings.getLocationCode3List();
			}
		}

		Collection<LocationCodeOption> locCode1ValueList = new ArrayList<>();
		boolean hasLocCd1PrimaryDef = hasLocCDPrimaryDefault(locationCode1List, locCode1ValueList);
		settings.setPrimaryLocationCode1Default(hasLocCd1PrimaryDef);

		Collection<LocationCodeOption> locCode2ValueList = new ArrayList<>();
		boolean hasLocCd2PrimaryDef = hasLocCDPrimaryDefault(locationCode2List, locCode2ValueList);
		settings.setPrimaryLocationCode2Default(hasLocCd2PrimaryDef);

		Collection<LocationCodeOption> locCode3ValueList = new ArrayList<>();
		boolean hasLocCd3PrimaryDef = hasLocCDPrimaryDefault(locationCode3List, locCode3ValueList);
		settings.setPrimaryLocationCode3Default(hasLocCd3PrimaryDef);

		settings.setAllowMLLocationCode1(canSearchByLocation1);
		settings.setAllowMLLocationCode2(canSearchByLocation2);
		settings.setAllowMLLocationCode3(canSearchByLocation3);
		settings.setLocationCode1List(locCode1ValueList);
		settings.setLocationCode2List(locCode2ValueList);
		settings.setLocationCode3List(locCode3ValueList);
		settings.setShowLocationCode1(
				isShowLocationCode(userSettings.isShowCustLoc1(), canSearchByLocation1, locationCode1List));
		settings.setShowLocationCode2(
				isShowLocationCode(userSettings.isShowCustLoc2(), canSearchByLocation2, locationCode2List));
		settings.setShowLocationCode3(
				isShowLocationCode(userSettings.isShowCustLoc3(), canSearchByLocation3, locationCode3List));

		settings.setShowLocationCode1NonSearch(userSettings.isShowCustLoc1());

		settings.setLocationCode1Label(userSettings.getCustLoc1Label());
		settings.setLocationCode2Label(userSettings.getCustLoc2Label());
		settings.setLocationCode3Label(userSettings.getCustLoc3Label());
	}

	protected boolean isShowLocationCode(boolean isShowCustLoc, boolean canSearchByLocation,
			OrderReferenceFieldList[] locationCodeList) {
		return isShowCustLoc && (canSearchByLocation || locationCodeList.length > 0);
	}

	protected boolean hasLocCDPrimaryDefault(OrderReferenceFieldList[] locationCodeList,
			Collection<LocationCodeOption> locCode1ValueList) {

		boolean hasLocCdPrimaryDef = false;
		for (OrderReferenceFieldList val : locationCodeList) {
			hasLocCdPrimaryDef = hasLocCdPrimaryDef || val.isPrimaryDefault();
			locCode1ValueList.add(new LocationCodeOption(val.getReferenceFieldValue(), val.isPrimaryDefault()));
		}
		return hasLocCdPrimaryDef;
	}

	public List<Address> getDefaultAddress(VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean) throws AtWinXSException {

		AddressSourceSettings addressSourceSettings = ObjectMapFactory.getEntityObjectMap()
				.getEntity(AddressSourceSettings.class, appSessionBean.getCustomToken());

		List<Address> defaultAddress = new ArrayList<>();
		boolean hasDefaultFilter = false;
		boolean hasValidDefaultFilter = false;

		try {
			IOEManageOrdersComponent ioeManageOrderService = OEManageOrdersComponentLocator
					.locate(appSessionBean.getCustomToken());
			addressSourceSettings.checkDefaultFilter(oeSessionBean, ioeManageOrderService, appSessionBean.getSiteID(),
					volatileSessionBean.getSelectedSiteAttribute());
			hasDefaultFilter = addressSourceSettings.isHasDefaultOrderFilter();
			hasValidDefaultFilter = addressSourceSettings.isHasValidDefaultOrderFilter();

			int profileNumber = appSessionBean.getProfileNumber();
			OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
			ProfileVOKey pabProfileKey = new ProfileVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(),
					profileNumber);

			// Calls the method that would build the Default Address if there's any
			Address pabAddress = buildAddressBookDefault(appSessionBean, oeSessionBean, hasValidDefaultFilter,
					userSettings, pabProfileKey, profileNumber);

			// the list of Default Addresses
			if (pabAddress != null && !(Util.isBlankOrNull(pabAddress.getShipToName1())
					&& Util.isBlankOrNull(pabAddress.getShipToName2()))) {
				defaultAddress.add(pabAddress);
			}

			getDefaultAddressFromAlternateProfile(oeSessionBean, appSessionBean, defaultAddress, hasValidDefaultFilter,
					userSettings);

			getDefaultAddressFromUserProfile(appSessionBean, defaultAddress, hasValidDefaultFilter, pabProfileKey);

			// DEFAULT ADDRESS FROM ORDER FILTER
			IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator
					.locate(appSessionBean.getCustomToken());

			OrderFilterVO[] filterVOs = addressSourceSettings.getOrderFilters(oeSessionBean, ordersService);

			OrderAddressVO defaultshipToAddressVO = ordersService.getDefaultShippingAddress(filterVOs);
			OrderAddressVO defaultbillToAddressVO = ordersService.getDefaultBillingAddress(filterVOs);

			// checks if user has Default Ship To Address in Order Filter
			if (defaultshipToAddressVO != null && hasValidDefaultFilter) {
				Address orderFilterAddr = DeliveryOptionsUtil.convertOrderAddVOToAddress(defaultshipToAddressVO);
				orderFilterAddr.setAddressType("S");
				orderFilterAddr.setSourceDefAddr(DeliveryOptionsConstants.ADDR_SRC_DEF_ORDER_FILTER);
				orderFilterAddr.setHasDefaultFilter(hasDefaultFilter);
				orderFilterAddr.setHasValidDefaultFilter(hasValidDefaultFilter);

				defaultAddress.add(orderFilterAddr);
			}

			//CAP-45143 - removed the section of code that adds the default billTo address here

		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

		return defaultAddress;
	}

	protected void getDefaultAddressFromUserProfile(AppSessionBean appSessionBean, Collection<Address> defaultAddress,
			boolean hasValidDefaultFilter, ProfileVOKey pabProfileKey) throws AtWinXSException {
		// DEFAULT ADDRESS from USER's PROFILE (Preferred Shipping Address)
		String profilePreferredShipToAddSrc = appSessionBean.getDfltShipToAddrSrc();
		// Check default from user profile (if user has a preferred shipping address)
		if (!Util.isBlankOrNull(profilePreferredShipToAddSrc)) {
			IPersonalAddress iPersonalAddress = PersonalAddressComponentLocator.locate(appSessionBean.getCustomToken());
			int defaultPersonalID = iPersonalAddress.getDefaultAddressValue(pabProfileKey);
			if (profilePreferredShipToAddSrc.equals(AdminConstant.DFLT_SHIP_TO_CORP) && defaultPersonalID != -2) {
				CorporateProfileBean corporateProfile = appSessionBean.getCorporateProfile();

				if (corporateProfile != null && (!Util.isBlankOrNull(corporateProfile.getName()))) {
					Address preferredAddr = DeliveryOptionsUtil.convertCorporateProfileBeanToAddress(corporateProfile);
					preferredAddr.setSourceDefAddr(DeliveryOptionsConstants.ADDR_SRC_DEF_PREFERRED);
					preferredAddr.setAddressType("S");
					preferredAddr.setHasDefaultFilter(hasValidDefaultFilter);
					preferredAddr.setHasValidDefaultFilter(hasValidDefaultFilter);

					defaultAddress.add(preferredAddr);
				}
			} else if (profilePreferredShipToAddSrc.equals(AdminConstant.DFLT_SHIP_TO_EXT_PROF)
					&& defaultPersonalID != -1) {
				PersonalProfileBean personalProfile = appSessionBean.getPersonalProfile();

				if (personalProfile != null && !(Util.isBlankOrNull(appSessionBean.getFirstName())
						&& Util.isBlankOrNull(appSessionBean.getLastName()))) {
					Address preferredAddr = DeliveryOptionsUtil.convertPersonalProfileBeanToAddress(personalProfile);
					preferredAddr.setSourceDefAddr(DeliveryOptionsConstants.ADDR_SRC_DEF_PREFERRED);
					preferredAddr.setAddressType("S");
					preferredAddr.setHasDefaultFilter(hasValidDefaultFilter);
					preferredAddr.setHasValidDefaultFilter(hasValidDefaultFilter);
					preferredAddr.setShipToName1(appSessionBean.getFirstName() + " " + appSessionBean.getLastName());

					defaultAddress.add(preferredAddr);
				}
			}
		}
	}

	protected void getDefaultAddressFromAlternateProfile(OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean, Collection<Address> defaultAddress, boolean hasValidDefaultFilter,
			OEResolvedUserSettingsSessionBean userSettings) throws AtWinXSException {
		int profileNumber;
		// Checks if the selected Alternate Profile has a default address
		ProfileSelection profileSelection = oeSessionBean.getProfileSelections();
		if (profileSelection != null) {
			// Retrieve map object of profileSelection
			Map<String, Integer> profileSelections = profileSelection.getProfileSelections();

			// Perform null check
			// If not null, loop through the profile selections.
			if (profileSelections != null) {
				for (Entry<String, Integer> profileSelected : profileSelections.entrySet()) {
					profileNumber = profileSelected.getValue();

					// build ProfileVOKey
					ProfileVOKey altProfileKey = new ProfileVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(),
							profileNumber);

					// Calls the method that would build the Default Address if there's any
					Address altAddress = buildAddressBookDefault(appSessionBean, oeSessionBean, hasValidDefaultFilter,
							userSettings, altProfileKey, profileNumber);
					if (altAddress != null) {
						defaultAddress.add(altAddress);
					}
				}
			}
		}
	}

	protected Address buildAddressBookDefault(AppSessionBean appSessionBean, OEOrderSessionBean oeSessionBean,
			boolean hasValidDefaultFilter, OEResolvedUserSettingsSessionBean userSettings, ProfileVOKey profileKey,
			int profileNumber) throws AtWinXSException {
		Address pabAddr = null;
		IPersonalAddress iPersonalAddress = PersonalAddressComponentLocator.locate(appSessionBean.getCustomToken());
		int defaultPersonalID = iPersonalAddress.getDefaultAddressValue(profileKey);

		// DEFAULT ADDRESS IN PAB & Selected Alternate Profile
		if (defaultPersonalID != 0) {

			PersonalAddressVO personalAddressVO = getPersonalAddressVO(appSessionBean, profileNumber, iPersonalAddress,
					defaultPersonalID);

			// PAB setting must NOT affect the
			// display of Key Alternate Profile Address Book. Key Alternate Profile should
			// always display regardless of user PAB setting.
			// If the profile number is the same as the logged in user, add the checking if
			// isAllowNewShipToAddress and isUsePersonalAddrBook are turned on
			if (profileNumber == appSessionBean.getProfileNumber()) {

				if (personalAddressVO != null && userSettings.isAllowNewShipToAddress()
						&& oeSessionBean.getProfileSelections() != null && appSessionBean.isUsePersonalAddrBook(null)
						&& appSessionBean.getProfileNumber() != AtWinXSConstant.INVALID_ID) {
					pabAddr = DeliveryOptionsUtil.convertPersonalAddVOToAddress(personalAddressVO);
					pabAddr.setSourceDefAddr(DeliveryOptionsConstants.ADDR_SRC_DEF_PAB);
					pabAddr.setAddressType("S");
					pabAddr.setHasDefaultFilter(hasValidDefaultFilter);
					pabAddr.setHasValidDefaultFilter(hasValidDefaultFilter);
				}
			}

			// else, the corresponding profile number came from an alternate profile so the
			// PAB condition should be removed
			else {
				if (personalAddressVO != null && userSettings.isAllowNewShipToAddress()
						&& oeSessionBean.getProfileSelections() != null
						&& appSessionBean.getProfileNumber() != AtWinXSConstant.INVALID_ID) {
					pabAddr = DeliveryOptionsUtil.convertPersonalAddVOToAddress(personalAddressVO);
					pabAddr.setSourceDefAddr(DeliveryOptionsConstants.ADDR_SRC_DEF_PAB);
					pabAddr.setAddressType("S");
					pabAddr.setHasDefaultFilter(hasValidDefaultFilter);
					pabAddr.setHasValidDefaultFilter(hasValidDefaultFilter);
				}
			}

		}

		return pabAddr;
	}

	protected PersonalAddressVO getPersonalAddressVO(AppSessionBean appSessionBean, int profileNumber,
			IPersonalAddress iPersonalAddress, int defaultPersonalID) throws AtWinXSException {
		PersonalAddressVO personalAddressVO = null;
		if (defaultPersonalID > 0) {
			personalAddressVO = iPersonalAddress.getPersonalAddress(new PersonalAddressVOKey(appSessionBean.getSiteID(),
					appSessionBean.getBuID(), profileNumber, defaultPersonalID));
		} else {
			if (defaultPersonalID == -1) {
				ExtendedProfile ext = ObjectMapFactory.getEntityObjectMap().getEntity(ExtendedProfile.class,
						appSessionBean.getCustomToken());
				ext.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), profileNumber);

				personalAddressVO = new PersonalAddressVO(appSessionBean.getSiteID(), appSessionBean.getBuID(),
						profileNumber, defaultPersonalID, appSessionBean.getFirstName(), appSessionBean.getLastName(),
						Util.nullToEmpty(ext.getLine1Address()), Util.nullToEmpty(ext.getLine2Address()),
						Util.nullToEmpty(ext.getLine3Address()), Util.nullToEmpty(ext.getCityName()),
						Util.nullToEmpty(ext.getStateCd()), Util.nullToEmpty(ext.getZipCd()),
						Util.nullToEmpty(ext.getCountryCd()), appSessionBean.getLoginID(), "",
						Util.nullToEmpty(ext.getContactMobileNumber()), false);
			} else if (defaultPersonalID == -2) {
				CorporateProfile corp = ObjectMapFactory.getEntityObjectMap().getEntity(CorporateProfile.class,
						appSessionBean.getCustomToken());
				corp.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), profileNumber);

				personalAddressVO = new PersonalAddressVO(appSessionBean.getSiteID(), appSessionBean.getBuID(),
						profileNumber, defaultPersonalID, appSessionBean.getFirstName(), appSessionBean.getLastName(),
						Util.nullToEmpty(corp.getLine1Address()), Util.nullToEmpty(corp.getLine2Address()),
						Util.nullToEmpty(corp.getLine3Address()), Util.nullToEmpty(corp.getCityName()),
						Util.nullToEmpty(corp.getStateCd()), Util.nullToEmpty(corp.getZipCd()),
						Util.nullToEmpty(corp.getCountryCd()), appSessionBean.getLoginID(), "",
						Util.nullToEmpty(corp.getCompanyPhoneNumber()), false);
			}
		}
		return personalAddressVO;
	}

	public List<BillToAddress> getBillToList(VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean) throws AtWinXSException {

		List<BillToAddress> billToAddresses = new ArrayList<>();

		try {
			OECheckoutAssembler assembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean,
					appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
					appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

			IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator
					.locate(appSessionBean.getCustomToken());
			AddressSourceSettings addressSourceSettings = ObjectMapFactory.getEntityObjectMap()
					.getEntity(AddressSourceSettings.class, appSessionBean.getCustomToken());
			OEAddressSearchFormBean formBean = assembler.getAddressSearchForm(oeSessionBean,
					appSessionBean.getCorporateNumber());
			OrderFilterVO[] filterVOs = addressSourceSettings.getOrderFilters(oeSessionBean, ordersService);
			boolean showCorpBillToCode = Util.isShowCorpBillToInDropdown(appSessionBean);

			// RAR - Check first the Default Bill To from UG before checking Order Filters.
			String defaultSoldToAddress = oeSessionBean.getUserSettings().getDefaultSoldToForNewAddress();
			String corpNum = defaultSoldToAddress.length() >= 10 ? defaultSoldToAddress.substring(0, 10)
					: appSessionBean.getCorporateNumber();
			String soldToNum = defaultSoldToAddress.length() >= 11 ? defaultSoldToAddress.substring(10)
					: AtWinXSConstant.EMPTY_STRING;

			String defaultBillToCode = getDefaultBillToCode(volatileSessionBean, appSessionBean, filterVOs,
					ordersService, corpNum, soldToNum);

			// CP-8989 formBean.getBillToList will be only be populated in the method
			// assembler.getAddressSearchForm
			// if user has allowed CMF search. We need to get the bill to list for other
			// Address sources like PAB, Default Address, etc.
			if (formBean.getBillToList() == null) {
				// CP-10511 fixed logic to get the default bill to if they have one specified,
				// but can't change it
				if (oeSessionBean.getUserSettings().isSoldToEditableForNewAddress()
						|| Util.isBlankOrNull(oeSessionBean.getUserSettings().getDefaultSoldToForNewAddress())) {
					OENewAddressFormBean tempForm = new OENewAddressFormBean();
					assembler._setNewAddressFormArrays(oeSessionBean, appSessionBean.getCorporateNumber(), tempForm);
					formBean.setBillToList(tempForm.getBillToList());
				} else {
					OrderAddressVO addressvo = ordersService.getAddress(new OrderAddressVOKey(corpNum, soldToNum, null),
							oeSessionBean.getOrderScenarioNumber());
					if (addressvo != null) {
						OEBillToAddressResponseBean[] addressbean = new OEBillToAddressResponseBean[1];
						addressbean[0] = new OEBillToAddressResponseBean(addressvo);
						formBean.setBillToList(addressbean);
					}
				}
			}

			populateBillToList(billToAddresses, formBean, showCorpBillToCode, defaultBillToCode);

		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

		return billToAddresses;
	}

	protected void populateBillToList(List<BillToAddress> billToAddresses, OEAddressSearchFormBean formBean,
			boolean showCorpBillToCode, String defaultBillToCode) {
		for (OEBillToAddressResponseBean bean : formBean.getBillToList()) {
			BillToAddress billToAddr = new BillToAddress();

			// Bill to Address Panel
			Address address = new AddressSearchResult();
			address.setAddressLine1(Util.nullToEmpty(bean.getAddressLine1()));
			address.setAddressLine2(Util.nullToEmpty(bean.getAddressLine2()));
			address.setAddressLine3(Util.nullToEmpty(bean.getAddressLine3()));
			address.setCity(Util.nullToEmpty(bean.getCity()));
			address.setStateOrProvince(Util.nullToEmpty(bean.getState()));
			address.setPostalCode(Util.nullToEmpty(bean.getZip()));
			address.setCountry(Util.nullToEmpty(bean.getCountry()));
			address.setShipToName1(Util.nullToEmpty(bean.getCustomerName1()));
			address.setShipToName2(Util.nullToEmpty(bean.getCustomerName2()));
			billToAddr.setBillToAddress(address);
			String listDisplayValue = bean.getAddressForList();
			if (showCorpBillToCode) {
				listDisplayValue += " (" + Util.replace(bean.getBillToCode(), ":", "-") + ")";
			}
			billToAddr.setBillToCodeValue(new NameValuePair<>(listDisplayValue, bean.getBillToCode()));
			billToAddr.setAddressForDisplay(bean.getAddressForDisplay());

			if (defaultBillToCode.equals(bean.getBillToCode())) {
				billToAddr.setDefaultBillToAddr(true);
			}

			billToAddresses.add(billToAddr);
		}
	}

	protected String getDefaultBillToCode(VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean,
			OrderFilterVO[] filterVOs, IOEManageOrdersComponent ordersService, String corpNum, String soldToNum)
			throws AtWinXSException {
		OrderAddressVO defaultbillToAddressVO;
		String defaultBillToCode = corpNum + AtWinXSConstant.COLON + soldToNum;

		// If not Default Bill To from UG, then check Order Filters.
		if (Util.isBlankOrNull(defaultBillToCode)) {
			defaultbillToAddressVO = ordersService.getDefaultBillingAddress(filterVOs);
			if (defaultbillToAddressVO != null) {
				defaultBillToCode = defaultbillToAddressVO.getKey().getCorporateNumber() + AtWinXSConstant.COLON
						+ defaultbillToAddressVO.getKey().getSoldToNumber();
			}
		}

		defaultBillToCode = DeliveryOptionsUtil.getPreviouslySelectedBillToCode(appSessionBean, volatileSessionBean,
				defaultBillToCode);
		return defaultBillToCode;
	}

	public List<NameValuePair<String>> getCountryList(OEOrderSessionBean oeSessionBean) {

		List<NameValuePair<String>> countryOptions = new ArrayList<>();
		Collection<com.wallace.atwinxs.locale.ao.CountryBean> countryList = UtilCountryInfo
				.getFilteredCountryNames(oeSessionBean.getUserSettings().getCountryNames());

		for (com.wallace.atwinxs.locale.ao.CountryBean bean : countryList) {
			countryOptions.add(new NameValuePair<>(bean.getCountryName(), bean.getCountryCode()));
		}

		return countryOptions;
	}

	public List<Address> searchMasterList(VolatileSessionBean volatileSessionBean, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean, Map<String, Object> searchCriteria) throws AtWinXSException {

		List<Address> searchResults = new ArrayList<>();

		try {
			// Get Master List Address
			AddressSourceSettings addressSourceSettings = ObjectMapFactory.getEntityObjectMap()
					.getEntity(AddressSourceSettings.class, appSessionBean.getCustomToken());
			BillAndShipAddressVO[] searchResultsVOs = addressSourceSettings.searchMasterListAddress(searchCriteria,
					oeSessionBean, appSessionBean, volatileSessionBean.getSelectedSiteAttribute());

			// Loop through the search results and add it to the collection of Addresses
			if (searchResultsVOs != null && searchResultsVOs.length > 0) {
				for (BillAndShipAddressVO vo : searchResultsVOs) {
					if (vo.getShipToAddress() != null) {
						Address address = DeliveryOptionsUtil.convertBillAndShipAddVOToAddress(vo);
						searchResults.add(address);
					}
				}
			}

		} catch (Exception e) {
			throw new AtWinXSException(e.getMessage(), this.getClass().getName());
		}

		return searchResults;
	}

	// CAP-39973 - Company Master Address search service based on search request
	// criteria
	@Override
	public CompanyMasterAddressSearchResponse searchCompanyMasterAddresses(SessionContainer sc,
			CompanyMasterAddressSearchRequest companyMasterAddressSearchRequest) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();

		Collection<Address> searchResults = new ArrayList<>();

		CompanyMasterAddressSearchResponse companyMasterAddressSearchResponse = new CompanyMasterAddressSearchResponse();
		Map<String, Object> searchCriteria = new HashMap<>();

		String searchKey = Util
				.nullToEmpty(companyMasterAddressSearchRequest.getGenericSearchCriteria().get(0).getCriteriaFieldKey());
		String searchValue = Util.nullToEmpty(
				companyMasterAddressSearchRequest.getGenericSearchCriteria().get(0).getCriteriaFieldValue());

		// validate empty search key
		if (AtWinXSConstant.EMPTY_STRING.equals(searchKey)
				|| !searchKey.matches("country|state|zip|shiptoname1|shiptoname2|billToCode|loc1|loc2|loc3")
				|| (AtWinXSConstant.EMPTY_STRING.equals(searchValue))) {

			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.SEARCH_CRITERIA_ERR));
			return companyMasterAddressSearchResponse;
		} else {

			searchCriteria.put("", "");
			searchCriteria.put(searchKey, searchValue);
		}

		ApplicationVolatileSession applicationVolatileSession = sc.getApplicationVolatileSession();
		VolatileSessionBean volatileSessionBean = applicationVolatileSession.getVolatileSessionBean();

		IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());

		AddressCheckOutSettings checkoutSettings = getCheckOutSettings(sc, ordersService, volatileSessionBean,
				oeOrderSessionBean, appSessionBean);

		companyMasterAddressSearchResponse.setSuccess(true);

		// validate access to search key validation
		validateAccessCompanyMasterAddressV1(volatileSessionBean, checkoutSettings, searchKey);
		validateAccessCompanyMasterAddressV2(checkoutSettings, searchKey);

		try {

			// validate empty search value and require length
			validateReqParamCompanyMasterAddressV3(searchKey, searchValue, companyMasterAddressSearchResponse,
					appSessionBean);
			validateReqParamCompanyMasterAddressV4(searchKey, searchValue, companyMasterAddressSearchResponse,
					appSessionBean);
			if (!companyMasterAddressSearchResponse.isSuccess()) {
				return companyMasterAddressSearchResponse;
			}

			BillAndShipAddressVO[] searchResultsVOs = searchMasterListAddressASSIC1UX1(searchCriteria,
					oeOrderSessionBean, appSessionBean, ModelConstants.MAX_ADDRESS_RESULTS_COUNT);

			// Loop through the search results and add it to the collection of Addresses
			if (searchResultsVOs != null && searchResultsVOs.length > 0) {

				int arraySize = searchResultsVOs.length;

				// if searchResultsVOs more than the addresses limit, set flag to true else
				// default false
				if (arraySize >= ModelConstants.MAX_ADDRESS_RESULTS_COUNT) {

					companyMasterAddressSearchResponse.setCompanyMasterAddressesExceedLimit(true);
				}
				for (BillAndShipAddressVO vo : searchResultsVOs) {
					if (vo.getShipToAddress() != null) {

						// Filter the addresses when in Build a List scenario
						Address address = DeliveryOptionsUtil.convertBillAndShipAddVOToAddress(vo);
						searchResults.add(address);
					}
				}
			}

			companyMasterAddressSearchResponse.setCompanyMasterAddresses(searchResults);
		} catch (AtWinXSException e) {

			logger.error(e.getMessage(), e);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse.setMessage(e.getMessage());
		}

		return companyMasterAddressSearchResponse;

	}

	// CAP-39973 - validate access to search key validation1
	protected void validateAccessCompanyMasterAddressV1(VolatileSessionBean volatileSessionBean,
			AddressCheckOutSettings checkoutSettings, String searchKey) throws AtWinXSException {

		if ((null == volatileSessionBean.getOrderId() || AtWinXSConstant.INVALID_ID == volatileSessionBean.getOrderId()
				|| volatileSessionBean.getShoppingCartCount() <= 0)
				|| (!checkoutSettings.isAllowMasterList() || !checkoutSettings.isAllowMasterListSearchCriteria())
				|| (ModelConstants.CMALS_CT.equals(searchKey) && !checkoutSettings
						.isAllowMLCountry())
				|| (ModelConstants.CMALS_ST.equals(searchKey) && !checkoutSettings.isAllowMLState()) /*
																										 * &&
																										 * !isNoStateProvince
																										 */
				|| (ModelConstants.CMALS_ZIP.equals(searchKey) && !checkoutSettings.isAllowMLZip())) {

			throw new AccessForbiddenException("Access to this service is not allowed", this.getClass().getName());
		}
	}

	// CAP-39973 - validate access to search key validation2
	protected void validateAccessCompanyMasterAddressV2(AddressCheckOutSettings checkoutSettings, String searchKey)
			throws AtWinXSException {

		if ((ModelConstants.CMALS_SN1.equals(searchKey) && !checkoutSettings.isAllowMLName1())
				|| (ModelConstants.CMALS_SN2.equals(searchKey) && !checkoutSettings.isAllowMLName2())
				|| (ModelConstants.CMALS_B2C.equals(searchKey) && !checkoutSettings.isAllowMLBillTo())
				|| (ModelConstants.CMALS_LC1.equals(searchKey) && !checkoutSettings.isAllowMLLocationCode1())
				|| (ModelConstants.CMALS_LC2.equals(searchKey) && !checkoutSettings.isAllowMLLocationCode2())
				|| (ModelConstants.CMALS_LC3.equals(searchKey) && !checkoutSettings.isAllowMLLocationCode3())) {

			throw new AccessForbiddenException("Access to this service is not allowed", this.getClass().getName());
		}
	}

	// CAP-39973 - validate empty search value and require length
	protected void validateReqParamCompanyMasterAddressV3(String searchKey, String searchValue,
			CompanyMasterAddressSearchResponse companyMasterAddressSearchResponse, AppSessionBean appSessionBean)
			throws AtWinXSException {

		Map<String, Object> replaceMap = new HashMap<>();

		if (ModelConstants.CMALS_CT.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() != ModelConstants.NUMERIC_3) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_3);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.EXACT_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_ST.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_4) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_4);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_ZIP.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_12) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_12);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_SN1.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_35) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_35);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_SN2.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_35) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_35);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

	}

	// CAP-39973 - validate empty search value and require length
	protected void validateReqParamCompanyMasterAddressV4(String searchKey, String searchValue,
			CompanyMasterAddressSearchResponse companyMasterAddressSearchResponse, AppSessionBean appSessionBean)
			throws AtWinXSException {

		Map<String, Object> replaceMap = new HashMap<>();

		if (ModelConstants.CMALS_B2C.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() != ModelConstants.NUMERIC_16) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_16);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.EXACT_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_LC1.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_25) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_25);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_LC2.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_25) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_25);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

		if (ModelConstants.CMALS_LC3.equals(searchKey) && !Util.isBlankOrNull(searchValue)
				&& searchValue.trim().length() > ModelConstants.NUMERIC_25) {

			replaceMap.put(SFTranslationTextConstants.MAX_CHARS_REPLACEMENT_TAG, ModelConstants.NUMERIC_25);
			companyMasterAddressSearchResponse.setSuccess(false);
			companyMasterAddressSearchResponse
					.setMessage(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.MAX_CHARS_ERR, replaceMap));
		}

	}

	// CAP-39973 - Method copied from AddressSourceSettingsImpl to use the bunch of
	// existing CP logics and to pass maxAddressResultsCount
	// Split into two to reduce cognitive complexity
	public BillAndShipAddressVO[] searchMasterListAddressASSIC1UX1(Map<String, Object> searchCriteria,
			OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean, int maxAddressResultsCount)
			throws AtWinXSException {

		OEApprovalOrderCheckoutSessionBean approvalCheckoutSession = oeSessionBean.getApprovalCheckoutSession();
		SubscriptionCheckoutSessionBean subscriptionCheckoutSession = oeSessionBean.getSubscriptionCheckoutSession();

		BillAndShipAddressVO[] searchResultsVOs = null;

		int orderScenario = -1;
		if (approvalCheckoutSession == null && subscriptionCheckoutSession == null) {

			orderScenario = oeSessionBean.getOrderScenarioNumber();
		} else if (subscriptionCheckoutSession != null) {

			orderScenario = subscriptionCheckoutSession.getOrderScenarioNumber();
		} else {

			try {

				orderScenario = Integer.parseInt(approvalCheckoutSession.getOrderScenario());
			} catch (Exception ignore) {

				logger.error("If error thrown, ignore: ", ignore);
			}
		}

		searchResultsVOs = searchMasterListAddressASSIC1UX2(searchCriteria, oeSessionBean, appSessionBean,
				orderScenario, maxAddressResultsCount);

		return searchResultsVOs;
	}

	// CAP-39973 - Method copied from AddressSourceSettingsImpl to use the bunch of
	// existing CP logics and to pass maxAddressResultsCount
	// Split into two to reduce cognitive complexity
	public BillAndShipAddressVO[] searchMasterListAddressASSIC1UX2(Map<String, Object> searchCriteria,
			OEOrderSessionBean oeSessionBean, AppSessionBean appSessionBean, int orderScenario,
			int maxAddressResultsCount) throws AtWinXSException {

		IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();

		BillAndShipAddressVO[] searchResultsVOs = null;

		OrderFilterVO[] filterVOs = oeSessionBean.getOrderFilters();

		String corpNumber = null;
		String soldToNumber = null;
		String regionOption = "";
		String regionValue = "";
		String locationCode1 = "";
		String locationCode2 = "";
		String locationCode3 = "";
		String country = "";
		String name1 = "";
		String name2 = "";
		String city = "";

		String billToCode = Util.nullToEmpty((String) searchCriteria.get("billToCode"));
		if ((billToCode != null) && (billToCode.trim().length() > 11)) {

			corpNumber = billToCode.substring(0, 10);
			soldToNumber = billToCode.substring(11, billToCode.length());
		}

		String state = Util.nullToEmpty((String) searchCriteria.get("state"));
		String zip = Util.nullToEmpty((String) searchCriteria.get("zip"));
		country = Util.nullToEmpty((String) searchCriteria.get("country"));
		name1 = Util.nullToEmpty((String) searchCriteria.get("shiptoname1"));
		name2 = Util.nullToEmpty((String) searchCriteria.get("shiptoname2"));
		city = Util.nullToEmpty((String) searchCriteria.get("city"));
		locationCode1 = Util.nullToEmpty((String) searchCriteria.get("loc1"));
		locationCode2 = Util.nullToEmpty((String) searchCriteria.get("loc2"));
		locationCode3 = Util.nullToEmpty((String) searchCriteria.get("loc3"));

		if (!Util.isBlankOrNull(state)) {

			regionOption = OrderEntryConstants.ADDRESS_SEARCH_REGION_STATE;
			regionValue = state;
		}

		if (!Util.isBlankOrNull(zip)) {

			if (Util.isBlankOrNull(regionOption)) {

				regionOption = OrderEntryConstants.ADDRESS_SEARCH_REGION_ZIP;
				regionValue = zip;
			} else {
				// append if more than one search term
				regionOption += ":" + OrderEntryConstants.ADDRESS_SEARCH_REGION_ZIP;
				regionValue += ":" + zip;
			}
		}

		// If there is no regionOption set, set country to be the region option
		if (!Util.isBlankOrNull(country)) {
			if (Util.isBlankOrNull(regionOption)) {

				regionOption = OrderEntryConstants.ADDRESS_SEARCH_REGION_COUNTRY;
				regionValue = country;
			}
		} else if (Util.isBlankOrNull(country)) {

			// No country found, country to default back to user's locale.
			country = appSessionBean.getDefaultLocale().getISO3Country();
		}

		OrderAddressSearchCriteria orderAddressSearchCriteria = new OrderAddressSearchCriteria();
		orderAddressSearchCriteria.setAllowSearchByCustRef1(userSettings.isAllowSearchByCustRef1());
		orderAddressSearchCriteria.setAllowSearchByCustRef2(userSettings.isAllowSearchByCustRef2());
		orderAddressSearchCriteria.setAllowSearchByCustRef3(userSettings.isAllowSearchByCustRef3());
		orderAddressSearchCriteria.setShowBillToInfo(true);
		orderAddressSearchCriteria.setLocationCode1List(userSettings.getLocationCode1List());
		orderAddressSearchCriteria.setLocationCode2List(userSettings.getLocationCode2List());
		orderAddressSearchCriteria.setLocationCode3List(userSettings.getLocationCode3List());
		orderAddressSearchCriteria.setParentCorporateNumber(appSessionBean.getCorporateNumber());
		orderAddressSearchCriteria.setOrderFilters(filterVOs);
		orderAddressSearchCriteria.setRegionOption(regionOption);
		orderAddressSearchCriteria.setRegionValue(regionValue);
		orderAddressSearchCriteria.setCorporateNumber(corpNumber);
		orderAddressSearchCriteria.setBillToCode(soldToNumber);
		orderAddressSearchCriteria.setLocation1Code(locationCode1);
		orderAddressSearchCriteria.setLocation2Code(locationCode2);
		orderAddressSearchCriteria.setLocation3Code(locationCode3);
		orderAddressSearchCriteria.setCountryValue(country);
		orderAddressSearchCriteria.setName1(name1);
		orderAddressSearchCriteria.setName2(name2);
		orderAddressSearchCriteria.setCity(city);
		orderAddressSearchCriteria.setMaxAddressResultsCount(maxAddressResultsCount);

		searchResultsVOs = searchAddressOEMC1UX(ordersService, orderScenario, orderAddressSearchCriteria);
		return searchResultsVOs;
	}

	// CAP-39973 - Method copied from OEManageOrdersComponent to use the bunch of
	// existing CP logics and to pass maxAddressResultsCount
	public BillAndShipAddressVO[] searchAddressOEMC1UX(IOEManageOrdersComponent ordersService, int orderScenarioNumber,
			OrderAddressSearchCriteria orderAddressSearchCriteria) throws AtWinXSException {

		orderAddressSearchCriteria.setDistributionListOnly(false);

		// determine whether or not the order scenario is such that only the pre-defined
		// distribution list address is valid
		orderAddressSearchCriteria.setDistributionListOnly(ordersService.useDistributionAddress(orderScenarioNumber));

		// exclude campaign bulk ship for mailMergeOnly
		boolean mailMergeOnly = (ordersService.isMailMergeOnly(orderScenarioNumber)
				|| (ordersService.isCampaignOrder(orderScenarioNumber)
						&& !ordersService.isCampaignBulkShipOrder(orderScenarioNumber)));

		OrderAddressDAO dao = new OrderAddressDAO();
		BillAndShipAddressVO[] addresses = dao.searchAddress(orderAddressSearchCriteria);

		// When Mail Merge Only order, we only want to return 1 address that matches the
		// "DISTRIBUTION" ship to
		// If multiple addresses are found, just return the first.
		if (addresses != null && addresses.length > 1 && mailMergeOnly) {

			addresses = new BillAndShipAddressVO[] { addresses[0] };
		}

		return addresses;
	}

	// CAP-40324 //CAP-38135
	@Override
	public CODeliveryInformationSaveResponse saveDeliveryInformation(SessionContainer sc,
			CODeliveryInformationSaveRequest request, boolean validationUSPS) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();

		OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean,
				appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

		// Validate order id and cart count
		CODeliveryInformationSaveResponse response = new CODeliveryInformationSaveResponse();
		if (!validateOrder(response, sc, appSessionBean)) {
			return response;
		}

		// CAP-41266 - store this here in case previous is selected
		int orderID = volatileSessionBean.getOrderId().intValue();
		OrderAddressVO priorAddress = null;
		if (AddressSources.PRV.name().equalsIgnoreCase(request.getAddressSource())) {
			priorAddress = getPreviouslySelectedAddress(checkoutAssembler, appSessionBean, oeOrderSessionBean,
					sc.getApplicationSession().getPunchoutSessionBean(), orderID, request);
		}

		response = validateSaveRequest(request, appSessionBean, userSettings, checkoutAssembler, priorAddress, validationUSPS); // CAP-41266
																												// -
																												// prior
																												// only
																												// populated
																												// for
																												// PRV

		if (response.isSuccess()) {
			OENewAddressFormBean formBean = populateNewAddressFormBean(request, userSettings, priorAddress); // CAP-41266
																												// -
																												// prior
																												// only
																												// populated
																												// for
																												// PRV
			try {
				checkoutAssembler.setNewAddress(oeOrderSessionBean, appSessionBean.getCorporateNumber(), formBean, true,
						true);
				// CP-9072 if address source is master's list, we need to set the proper wcss
				// ship to number
				if (!Util.isBlankOrNull(request.getWcssShipToNbr())) {
					StringBuilder selectedShipToCode = new StringBuilder();
					selectedShipToCode.append(request.getBillToCode());
					selectedShipToCode.append(AtWinXSConstant.COLON).append(request.getWcssShipToNbr());
					String updateLoginID = appSessionBean.getLoginID();
					checkoutAssembler.setShipToCode(oeOrderSessionBean, selectedShipToCode.toString(), updateLoginID);
				}
				checkoutAssembler.saveBillToAttention(request.getBillToAttention(), oeOrderSessionBean.getLoginID());

				// CAP-44416 TH - Save to PAB if needed
				saveAsPABAddress(formBean, request, oeOrderSessionBean, volatileSessionBean, appSessionBean, sc);

				//CAP-48725
				updateDistributionListDetails(appSessionBean, volatileSessionBean, oeOrderSessionBean, String.valueOf(-1), String.valueOf(0));
				updateOrderScenarioNumber(appSessionBean, volatileSessionBean, request.getAddressSource(), oeOrderSessionBean);

				response.setSuccess(true);
				response.setMessage(
						getTranslation(appSessionBean, SFTranslationTextConstants.SAVE_DELIVERY_INFO_SUCCESS_MSG,
								SFTranslationTextConstants.SAVE_DELIVERY_INFO_DEF_SUCCESS_MSG));
			} catch (AtWinXSException e) {
				response.setSuccess(false);
				response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.GENERIC_SAVE_FAILED_ERR,
						SFTranslationTextConstants.GENERIC_SAVE_FAILED_DEF_ERR));
			}
		}

		return response;
	}

	// CAP-44416 TH - Added code to save to PAB if manually enter address and the request has the save to PAB checked
	protected void saveAsPABAddress(OENewAddressFormBean formBean, CODeliveryInformationSaveRequest request, OEOrderSessionBean oeOrderSessionBean,
			VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean, SessionContainer sc)
	{
		try
		{
			// Only attempt to save as pab address is manually enter address
			if (AddressSources.MEN.name().equalsIgnoreCase(request.getAddressSource()) && request.isAddToPAB())
			{
				IOEManageOrdersComponent ordersService = OEManageOrdersComponentLocator.locate(appSessionBean.getCustomToken());

				AddressCheckOutSettings checkoutSettings = getCheckOutSettings(sc, ordersService, volatileSessionBean,
						oeOrderSessionBean, appSessionBean);

				if (checkoutSettings.isAllowPAB())
				{
					PABSaveRequest pabRequest = new PABSaveRequest();

					pabRequest.setAddress1(formBean.getAddressLine1());
					pabRequest.setAddress2(formBean.getAddressLine2());
					pabRequest.setAddress3(formBean.getAddressLine3());
					pabRequest.setCity(formBean.getCity());
					pabRequest.setCountry(formBean.getCountry());
					pabRequest.setDefaultAddress(request.isPersonalAddressDefault());
					pabRequest.setName(formBean.getCompanyName());
					pabRequest.setName2(formBean.getCompanyName2());
					pabRequest.setPhoneNumber(formBean.getPhoneNumber());
					pabRequest.setShipToAttn(formBean.getShipToAttention());
					pabRequest.setState(formBean.getState());
					pabRequest.setZip(formBean.getZip());

					// CAP-44961 - added uspsValidationFlag=false to use older version savePAB
					// Save PAB address

					//CAP-45544
					PABSaveResponse response = savePABAdderessForDeliveryInfo(sc, appSessionBean, oeOrderSessionBean, pabRequest);

					if (!response.isSuccess())
					{
						logger.info("Could not save address to PAB.  Save failed");
					}
				}
			}
		}
		catch(Exception ex)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Could not save address to PAB.  Save failed. ").append(ex.getStackTrace());
		}
	}

	//CAP-41550
	protected void validatePabId(CODeliveryInformationSaveRequest request, AppSessionBean appSessionBean,Map<String, String> fieldErrors) {
		if (AddressSources.PAB.name().equalsIgnoreCase(request.getAddressSource()) && (request.getPabId() < -2)) {//CAP-44343
			String message = getInvalidErrorMessage(appSessionBean, AtWinXSConstant.EMPTY_STRING);
			fieldErrors.put(ModelConstants.PAB_ID_FIELD, message.trim());
		}
	}

	//CAP-38135
	// CAP-40324, // CAP-41266 - priorpersonalAddressIDAddress only populated for PRV
	protected CODeliveryInformationSaveResponse validateSaveRequest(CODeliveryInformationSaveRequest request,
			AppSessionBean appSessionBean, OEResolvedUserSettingsSessionBean userSettings,
			OECheckoutAssembler checkoutAssembler, OrderAddressVO priorAddress, boolean uspsValidationFlag) throws AtWinXSException {

		Properties translationProps = translationService.getResourceBundle(appSessionBean,
				SFTranslationTextConstants.DELIVERY_INFO_VIEW_NAME);
		Map<String, String> translationMap = translationService.convertResourceBundlePropsToMap(translationProps);

		// Validate Address Source
		CODeliveryInformationSaveResponse response = validateAddressSource(appSessionBean, request, translationMap,
				priorAddress);

		// Validate required fields
		if (response.isSuccess()) {
			Map<String, String> fieldErrors = new HashMap<>();

			// CAP-41092 - only validate ship to address fields for non CML addresses //CAP-42453 - added additional condition to check the WcssShipToNbr is not null
			boolean masterListAddress = (AddressSources.CML.name().equalsIgnoreCase(request.getAddressSource()) || (!Util.isBlankOrNull(request.getWcssShipToNbr())));
			if (priorAddress == null) {
				if (!masterListAddress) {
					// move this off to another method for clarity
					validateFullShipToAddress(request, appSessionBean, fieldErrors, userSettings, translationMap);
				} // end if address source that skips validation
				else { // CAP-41092 - only CML should validate this
						// validate wcss ship to number
					validateWcssShipToNbrField(appSessionBean, request, fieldErrors);
				}
			}
			// CAP-41092 - bill to and corp/sold to should validate no matter the source
			// validate ship to attention always
			validateShipToAttention(appSessionBean, Util.nullToEmpty(request.getShipToAttention()), userSettings,
					fieldErrors);

			// validate bill to attention
			validateBillToAttention(appSessionBean, Util.nullToEmpty(request.getBillToAttention()), userSettings,
					fieldErrors);

			// validate bill to code
			validateBillToCodeField(appSessionBean, request, fieldErrors);

			// validate corporate number
			validateFieldForExactCharRequired(appSessionBean, Util.nullToEmpty(request.getCorporateNbr()),
					ModelConstants.CORP_NBR_FIELD, SFTranslationTextConstants.CORP_NBR_LBL,
					ModelConstants.DELIVERY_INFO_REQ_SIZE_CORPORATE_NUMBER, fieldErrors);

			// validate sold to number
			validateFieldForExactCharRequired(appSessionBean, Util.nullToEmpty(request.getSoldToNbr()),
					ModelConstants.SOLD_TO_NBR_FIELD, SFTranslationTextConstants.SOLD_TO_NBR_LBL,
					ModelConstants.DELIVERY_INFO_REQ_SIZE_SOLD_TO_NUMBER, fieldErrors);
			//CAP-41550: validate PAB ID
			validatePabId(request, appSessionBean, fieldErrors);

			if (fieldErrors.isEmpty()) {
				// CAP-41092 - only call USPS validation if not a master list address
				if (!masterListAddress && (priorAddress == null)) {
					//CAP-38135
					if(uspsValidationFlag && ZipRequiredCountries.USA.name().equalsIgnoreCase(request.getCountry().trim())) {

						//CAP-46081
						Address newAddress = populateAddressSearchResult(request);
						USPSValidationResponse uspsValidationResponse = uspsValidationService.validateUSAddressV1(newAddress,
								appSessionBean, userSettings, checkoutAssembler, request.isOverrideUSPSErrors());

						response.setSuggestedAddress1(uspsValidationResponse.getSuggestedAddress1());
						response.setSuggestedAddress2(uspsValidationResponse.getSuggestedAddress2());
						response.setSuggestedCity(uspsValidationResponse.getSuggestedCity());
						response.setSuggestedState(uspsValidationResponse.getSuggestedState());
						response.setSuggestedZip(uspsValidationResponse.getSuggestedZip());
						response.setShowSuggestedAddress(uspsValidationResponse.isShowSuggestedAddress());
						response.setSuccess(uspsValidationResponse.isSuccess());
						response.setMessage(uspsValidationResponse.getMessage());
					}
					else {

						validateUSAddress(request, appSessionBean, userSettings, checkoutAssembler, response);
					}
				}
			} else {
				response.setSuccess(false);
				response.setFieldMessages(fieldErrors);
			}
		}
		//CAP-42241 update the response object here
		response.setCity(request.getCity());
		response.setState(request.getStateOrProvince());
		response.setZip(request.getPostalCode());
		return response;
	}

	protected void validateFullShipToAddress(CODeliveryInformationSaveRequest request, AppSessionBean appSessionBean,
			Map<String, String> fieldErrors, OEResolvedUserSettingsSessionBean userSettings,
			Map<String, String> translationMap) {
		Collection<com.wallace.atwinxs.locale.ao.CountryBean> countryList = UtilCountryInfo
				.getFilteredCountryNames(userSettings.getCountryNames());

		com.wallace.atwinxs.locale.ao.CountryBean selectedCountry = countryList.stream()
				.filter(country -> Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(country.getCountryCode()))
				.findAny().orElse(null);
		boolean isCountryValid = null != selectedCountry;

		// validate country
		validateCountryField(appSessionBean, Util.nullToEmpty(request.getCountry()), isCountryValid, fieldErrors,
				translationMap);

		// validate state
		validateStateField(appSessionBean, request, selectedCountry, fieldErrors);

		// validate city
		validateField(appSessionBean, Util.nullToEmpty(request.getCity()), ModelConstants.CITY_FIELD,
				Util.nullToEmpty(translationMap.get(ModelConstants.CITY_FIELD)),
				ModelConstants.DELIVERY_INFO_MAX_SIZE_CITY, fieldErrors);

		// validate address line 1
		validateField(appSessionBean, Util.nullToEmpty(request.getAddressLine1()), ModelConstants.ADDR_LINE_1_FIELD,
				Util.nullToEmpty(translationMap.get(ModelConstants.ADDR_LINE_1_FIELD)),
				ModelConstants.DELIVERY_INFO_MAX_SIZE_ADDRESS_LINE_1, fieldErrors);

		// validate address line 2
		validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getAddressLine2()),
				ModelConstants.ADDR_LINE_2_FIELD, ModelConstants.DELIVERY_INFO_MAX_SIZE_ADDRESS_LINE_2, fieldErrors);

		// validate address line 3
		validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getAddressLine3()),
				ModelConstants.ADDR_LINE_3_FIELD, ModelConstants.DELIVERY_INFO_MAX_SIZE_ADDRESS_LINE_3, fieldErrors);

		// validate ship to name
		validateField(appSessionBean, Util.nullToEmpty(request.getShipToName()), ModelConstants.SHIP_TO_NAME_FIELD,
				Util.nullToEmpty(translationMap.get(ModelConstants.SHIP_TO_NAME_FIELD)),
				ModelConstants.DELIVERY_INFO_MAX_SIZE_SHIP_TO_NAME, fieldErrors);

		// validate ship to name 2
		validateFieldForMaxChar(appSessionBean, Util.nullToEmpty(request.getShipToName2()),
				ModelConstants.SHIP_TO_NAME_2_FIELD, ModelConstants.DELIVERY_INFO_MAX_SIZE_SHIP_TO_NAME_2, fieldErrors);

		// validate zip
		validateZipField(appSessionBean, request, selectedCountry, fieldErrors);

		// validate phone number
		validatePhoneNumber(appSessionBean, Util.nullToEmpty(request.getPhoneNumber()), fieldErrors, translationMap);
	}

	// CAP-40324
	protected void validateUSAddress(CODeliveryInformationSaveRequest request, AppSessionBean appSessionBean,
			OEResolvedUserSettingsSessionBean userSettings, OECheckoutAssembler checkoutAssembler,
			CODeliveryInformationSaveResponse response) throws AtWinXSException {
		if (ZipRequiredCountries.USA.name().equalsIgnoreCase(request.getCountry().trim())) {
			Address newAddress = populateAddressSearchResult(request);
			MessageBean validation = DeliveryOptionsUtil.validateAddressHelper(newAddress, checkoutAssembler,
					appSessionBean, userSettings);
			// CAP-42241 copied the following code from CP validateAddressHelper to set the addressBean
			OENewAddressFormBean addressBean = new OENewAddressFormBean();

			AddressUIFields addressUIFields = ObjectMapFactory.getEntityObjectMap().getObject(AddressUIFields.class,
					appSessionBean.getCustomToken());
			addressUIFields.populate(OrderEntryConstants.ADDR_SRC_NEW);

			addressBean.setAddressLine1(newAddress.getAddressLine1());
			addressBean.setAddressLine2(newAddress.getAddressLine2());
			addressBean.setAddToPAB(newAddress.isAddToPAB());
			addressBean.setCity(newAddress.getCity());
			addressBean.setCompanyName(newAddress.getShipToName1());

			// temporary
			// to do validation if country has states
			addressBean.setCountry("USA");

			addressBean.setMakeDefault(newAddress.isFavorite());
			addressBean.setPersonalAddressID(newAddress.getId());
			addressBean.setPhoneNumber(newAddress.getPhoneNumber());
			addressBean.setShipToAttention(newAddress.getShipToAttention());
			addressBean.setState(newAddress.getStateOrProvince());
			String zip = Util.nullToEmpty(newAddress.getPostalCode());
			if (zip.length() > 5) {
				zip = zip.substring(0, 5);
			}
			addressBean.setZip(zip);
			addressBean.setCompanyName2(newAddress.getShipToName2());
			addressBean.setAddressLine3(AtWinXSConstant.EMPTY_STRING);
			addressBean.setShipToNameNumValid(addressUIFields.isShipToNameNumValid());
			addressBean.setShipToName2NumValid(addressUIFields.isShipToName2NumValid());

			if (userSettings.isNameSplit()) {
				String name = newAddress.getFirstName() + AtWinXSConstant.BLANK_SPACE + newAddress.getLastName();
				addressBean.setCompanyName(name);
			}
			// CAP-42241 copying ends here the following code from CP validateAddressHelper to set the addressBean
			 String errMsg= checkoutAssembler.validateAddress(addressBean); // CAP-42241 calling CP method to get the errMessage
			// CAP-42241 added the method to update the new address to request and response object
			updateAddressWithUSPSChanges(newAddress, request, errMsg);
			response.setSuccess(validation.isSuccess() || request.isOverrideUSPSErrors());
			response.setMessage(validation.getMsg());
		}
	}

	// CAP-40324 //CAP-38135
	protected Address populateAddressSearchResult(CODeliveryInformationSaveRequest request) {
		Address newAddress = new AddressSearchResult();
		newAddress.setAddressLine1(Util.nullToEmpty(SelfAdminUtil.replaceAddressSpecialChar(request.getAddressLine1())));
		newAddress.setAddressLine2(Util.nullToEmpty(SelfAdminUtil.replaceAddressSpecialChar(request.getAddressLine2())));
		newAddress.setAddressLine3(Util.nullToEmpty(request.getAddressLine3()));
		newAddress.setCity(Util.nullToEmpty(request.getCity()));
		newAddress.setShipToName1(Util.nullToEmpty(request.getShipToName()));
		newAddress.setShipToName2(Util.nullToEmpty(request.getShipToName2()));
		newAddress.setCountry(Util.nullToEmpty(request.getCountry().toUpperCase()));
		newAddress.setPhoneNumber(Util.nullToEmpty(request.getPhoneNumber()));
		newAddress.setPostalCode(Util.nullToEmpty(request.getPostalCode()));
		newAddress.setShipToAttention(Util.nullToEmpty(request.getShipToAttention()));
		newAddress.setStateOrProvince(Util.nullToEmpty(request.getStateOrProvince().toUpperCase()));
		newAddress.setAddressSource(Util.nullToEmpty(request.getAddressSource().toUpperCase()));
		if (!Util.isBlankOrNull(request.getShipToName())) {
			// handle split for mononym
			String name1 = request.getShipToName().trim();
			if (name1.indexOf(AtWinXSConstant.BLANK_SPACE) >= 0
					&& name1.indexOf(AtWinXSConstant.BLANK_SPACE) < name1.length() - 1) {
				// Name has embedded space
				String[] names = request.getShipToName().split(AtWinXSConstant.BLANK_SPACE, 2);
				newAddress.setFirstName(names[0]);
				newAddress.setLastName(names[1]);
			} else {
				// Name doesnt have space, or first or last char is space. Per Missy put it in
				// first name
				newAddress.setFirstName(name1);
			}
		}
		return newAddress;
	}

	// CAP-40324
	protected void validateWcssShipToNbrField(AppSessionBean appSessionBean, CODeliveryInformationSaveRequest request,
			Map<String, String> fieldErrors) {
		String fieldName = ModelConstants.WCSS_SHIP_TO_NBR_FIELD;
		if (AddressSources.CML.name().equalsIgnoreCase(request.getAddressSource())) {
			if (Util.isBlankOrNull(request.getWcssShipToNbr())) {
				String message = getRequiredErrorMessage(appSessionBean,
						SFTranslationTextConstants.WCSS_SHIP_TO_NBR_LBL);
				fieldErrors.put(fieldName, message);
			} else if (request.getWcssShipToNbr().trim()
					.length() != ModelConstants.DELIVERY_INFO_REQ_SIZE_WCSS_SHIP_TO_NUMBER) {
				String message = getExactRequiredErrorMessage(appSessionBean,
						ModelConstants.DELIVERY_INFO_REQ_SIZE_WCSS_SHIP_TO_NUMBER);
				fieldErrors.put(fieldName, message);
			}
		} else if (AddressSources.DEF.name().equalsIgnoreCase(request.getAddressSource())) {// CAP-46134 WCSS Ship To
																							// Number - Not Populated
																							// for DEF (order filter)
																							// Address Source
			if (Util.isBlankOrNull(request.getWcssShipToNbr())) {
				request.setWcssShipToNbr(AtWinXSConstant.EMPTY_STRING);
			}
		} else {
			request.setWcssShipToNbr(AtWinXSConstant.EMPTY_STRING);
		}
	}

	// CAP-40324
	protected void validateBillToCodeField(AppSessionBean appSessionBean, CODeliveryInformationSaveRequest request,
			Map<String, String> fieldErrors) {
		String fieldName = ModelConstants.BILL_TO_CD_FIELD;
		String label = SFTranslationTextConstants.BILL_TO_CODE_LBL;
		String expectedBillToCd = new StringBuilder(Util.nullToEmpty(request.getCorporateNbr()))
				.append(AtWinXSConstant.COLON).append(Util.nullToEmpty(request.getSoldToNbr())).toString();
		if (Util.isBlankOrNull(request.getBillToCode())) {
			String message = getRequiredErrorMessage(appSessionBean, label);
			fieldErrors.put(fieldName, message);
		} else if (request.getBillToCode().trim().length() != ModelConstants.DELIVERY_INFO_REQ_SIZE_BILL_TO_CODE) {
			String message = getExactRequiredErrorMessage(appSessionBean,
					ModelConstants.DELIVERY_INFO_REQ_SIZE_BILL_TO_CODE);
			fieldErrors.put(fieldName, message);
		} else if (!expectedBillToCd.equalsIgnoreCase(request.getBillToCode().trim())) {
			String message = getInvalidErrorMessage(appSessionBean, label);
			fieldErrors.put(fieldName, message);
		}
	}

	// CAP-40324
	protected void validateShipToAttention(AppSessionBean appSessionBean, String shipToAttention,
			OEResolvedUserSettingsSessionBean userSettings, Map<String, String> fieldErrors) {
		if (Util.isBlankOrNull(shipToAttention) && Util.yToBool(userSettings.getShipToAttnOptionCode())) {
			String message = getRequiredErrorMessage(appSessionBean, userSettings.getShipToAttnLabel());
			fieldErrors.put(ModelConstants.SHIP_TO_ATTN_FIELD, message);
		} else if (shipToAttention.length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_SHIP_TO_ATTENTION) {
			String message = getMustNotExceedErrorMessage(appSessionBean,
					ModelConstants.DELIVERY_INFO_MAX_SIZE_SHIP_TO_ATTENTION);
			fieldErrors.put(ModelConstants.SHIP_TO_ATTN_FIELD, message);
		}
	}

	// CAP-40324
	protected void validateBillToAttention(AppSessionBean appSessionBean, String billToAttention,
			OEResolvedUserSettingsSessionBean userSettings, Map<String, String> fieldErrors) {
		if (Util.isBlankOrNull(billToAttention)
				&& OrderEntryConstants.BILL_TO_ATTN_REQUIRED.equals(userSettings.getBillToAttnOption())) {
			String message = getRequiredErrorMessage(appSessionBean, userSettings.getBillToAttnLabel());
			fieldErrors.put(ModelConstants.BILL_TO_ATTN_FIELD, message);
		} else if (billToAttention.length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_BILL_TO_ATTENTION) {
			String message = getMustNotExceedErrorMessage(appSessionBean,
					ModelConstants.DELIVERY_INFO_MAX_SIZE_BILL_TO_ATTENTION);
			fieldErrors.put(ModelConstants.BILL_TO_ATTN_FIELD, message);
		}
	}

	// CAP-40324
	protected void validatePhoneNumber(AppSessionBean appSessionBean, String phoneNumber,
			Map<String, String> fieldErrors, Map<String, String> translationMap) {
		if (!Util.isBlankOrNull(phoneNumber)) {
			String fieldName = ModelConstants.PHONE_FIELD;
			if (!Pattern.matches(ModelConstants.PHONE_NBR_REGEX, phoneNumber)) {
				String message = getInvalidErrorMessage(appSessionBean,
						Util.nullToEmpty(translationMap.get(fieldName)));
				fieldErrors.put(fieldName, message);
			} else if (phoneNumber.length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_PHONE) {
				String message = getMustNotExceedErrorMessage(appSessionBean,
						ModelConstants.DELIVERY_INFO_MAX_SIZE_PHONE);
				fieldErrors.put(fieldName, message);
			}
		}
	}

	// CAP-40324
	protected void validateZipField(AppSessionBean appSessionBean, CODeliveryInformationSaveRequest request,
			com.wallace.atwinxs.locale.ao.CountryBean selectedCountry, Map<String, String> fieldErrors) {
		if (Util.isBlankOrNull(request.getPostalCode()) && Stream.of(ZipRequiredCountries.values())
				.anyMatch(country -> Util.nullToEmpty(request.getCountry()).equalsIgnoreCase(country.name()))) {
			String message = getRequiredErrorMessage(appSessionBean, selectedCountry.getZipLabelText());
			fieldErrors.put(ModelConstants.POSTAL_FIELD, message);
		} else if (Util.nullToEmpty(request.getPostalCode()).trim()
				.length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_ZIP_CD) {
			String message = getMustNotExceedErrorMessage(appSessionBean, ModelConstants.DELIVERY_INFO_MAX_SIZE_ZIP_CD);
			fieldErrors.put(ModelConstants.POSTAL_FIELD, message);
		}
	}

	// CAP-40324
	protected void validateFieldForExactCharRequired(AppSessionBean appSessionBean, String fieldValue, String fieldName,
			String label, int requiredSize, Map<String, String> fieldErrors) {
		if (Util.isBlankOrNull(fieldValue)) {
			String message = getRequiredErrorMessage(appSessionBean, label);
			fieldErrors.put(fieldName, message);
		} else if (fieldValue.length() != requiredSize) {
			String message = getExactRequiredErrorMessage(appSessionBean, requiredSize);
			fieldErrors.put(fieldName, message);
		}
	}

	// CAP-40324
	protected void validateField(AppSessionBean appSessionBean, String fieldValue, String fieldName, String label,
			int maxSize, Map<String, String> fieldErrors) {
		if (Util.isBlankOrNull(fieldValue)) {
			String message = getRequiredErrorMessage(appSessionBean, label);
			fieldErrors.put(fieldName, message);
		} else if (fieldValue.length() > maxSize) {
			String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
			fieldErrors.put(fieldName, message);
		}
	}

	// CAP-40324
	protected void validateFieldForMaxChar(AppSessionBean appSessionBean, String fieldValue, String fieldName,
			int maxSize, Map<String, String> fieldErrors) {
		if (!Util.isBlankOrNull(fieldValue) && fieldValue.length() > maxSize) {
			String message = getMustNotExceedErrorMessage(appSessionBean, maxSize);
			fieldErrors.put(fieldName, message);
		}
	}

	// CAP-40324
	protected void validateStateField(AppSessionBean appSessionBean, CODeliveryInformationSaveRequest request,
			com.wallace.atwinxs.locale.ao.CountryBean selectedCountry, Map<String, String> fieldErrors) {
		if ((null != selectedCountry && selectedCountry.getCountryHasStates())) {
			if (Util.isBlankOrNull(request.getStateOrProvince())) {
				String message = getRequiredErrorMessage(appSessionBean, selectedCountry.getStateLabelText());
				fieldErrors.put(ModelConstants.STATE_FIELD, message);
			} else if (request.getStateOrProvince().trim().length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_STATE_CD) {
				String message = getMustNotExceedErrorMessage(appSessionBean,
						ModelConstants.DELIVERY_INFO_MAX_SIZE_STATE_CD);
				fieldErrors.put(ModelConstants.STATE_FIELD, message);
			} else if (null == selectedCountry.getStateInCountry(request.getStateOrProvince().toUpperCase())) {
				String message = getInvalidErrorMessage(appSessionBean, selectedCountry.getStateLabelText());
				fieldErrors.put(ModelConstants.STATE_FIELD, message);
			}
		} else {
			request.setStateOrProvince(AtWinXSConstant.EMPTY_STRING);
		}
	}

	// CAP-40324
	protected void validateCountryField(AppSessionBean appSessionBean, String country, boolean isCountryValid,
			Map<String, String> fieldErrors, Map<String, String> translationMap) {
		String fieldName = ModelConstants.COUNTRY_FIELD;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		if (Util.isBlankOrNull(country)) {
			String message = getRequiredErrorMessage(appSessionBean, label);
			fieldErrors.put(fieldName, message);
		} else if (country.length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_COUNTRY_CD) {
			String message = getMustNotExceedErrorMessage(appSessionBean,
					ModelConstants.DELIVERY_INFO_MAX_SIZE_COUNTRY_CD);
			fieldErrors.put(fieldName, message);
		} else if (!isCountryValid) {
			String message = getInvalidErrorMessage(appSessionBean, label);
			fieldErrors.put(fieldName, message);
		}
	}

	// CAP-40324
	protected CODeliveryInformationSaveResponse validateAddressSource(AppSessionBean appSessionBean,
			CODeliveryInformationSaveRequest request, Map<String, String> translationMap, OrderAddressVO priorAddress) {
		CODeliveryInformationSaveResponse response = new CODeliveryInformationSaveResponse();
		String fieldName = ModelConstants.ADDR_SRC_FIELD;
		String label = Util.nullToEmpty(translationMap.get(fieldName));
		String message = AtWinXSConstant.EMPTY_STRING;

		if (Util.isBlankOrNull(request.getAddressSource())) {
			message = getRequiredErrorMessage(appSessionBean, label);
		} else if (request.getAddressSource().trim().length() > ModelConstants.DELIVERY_INFO_MAX_SIZE_COUNTRY_CD) {
			message = getMustNotExceedErrorMessage(appSessionBean, ModelConstants.DELIVERY_INFO_MAX_SIZE_COUNTRY_CD);
		} else if (Stream.of(AddressSources.values())
				.noneMatch(addressSrc -> request.getAddressSource().equalsIgnoreCase(addressSrc.name()))) {
			message = getInvalidErrorMessage(appSessionBean, label);
		}

		// CAP-41266 - add previous address handling - if there is no previous address,
		// that source is not valid
		if ((AddressSources.PRV.name().equalsIgnoreCase(request.getAddressSource())) && (priorAddress == null)) {
			message = getInvalidErrorMessage(appSessionBean, label);
		}

		if (!Util.isBlankOrNull(message)) {
			response.setSuccess(false);
			response.getFieldMessages().put(fieldName, message);
		} else {
			response.setSuccess(true);
		}

		return response;
	}

	// CAP-40324
	public OENewAddressFormBean populateNewAddressFormBean(CODeliveryInformationSaveRequest request,
			OEResolvedUserSettingsSessionBean userSettings, OrderAddressVO priorAddress) {
		OENewAddressFormBean form = new OENewAddressFormBean();

		int pabId = AtWinXSConstant.INVALID_ID;
		String addressSource = request.getAddressSource().toUpperCase();
		int addressChoice = OrderEntryConstants.CREATE_NEW_ADDR_CHOICE;
		//CAP-41550
		if (AddressSources.PAB.name().equalsIgnoreCase(addressSource)) {
			pabId = request.getPabId();
			addressChoice = OrderEntryConstants.PRSN_ADDR_CHOICE;
		}
		form.setAddressSource(addressSource);
		form.setAddressChoice(addressChoice);
		form.setPersonalAddressID(String.valueOf(pabId));
		if (priorAddress == null) {
			populateAddressFormFromRequest(request, form);
		} else {
			populateAddressFormFromPriorVO(priorAddress, form);
		}
		if (!Util.isBlankOrNull(request.getShipToName()) && userSettings.isNameSplit()) {
			// handle split for mononym
			String name1 = request.getShipToName().trim();
			if (name1.indexOf(AtWinXSConstant.BLANK_SPACE) >= 0
					&& name1.indexOf(AtWinXSConstant.BLANK_SPACE) < name1.length() - 1) {
				// Name has embedded space
				String[] names = request.getShipToName().split(AtWinXSConstant.BLANK_SPACE, 2);
				form.setFirstName(names[0]);
				form.setLastName(names[1]);
			} else {
				// Name doesnt have space, or first or last char is space. Per Missy put it in
				// first name
				form.setFirstName(name1);
			}
		}

		form.setShipToAttention(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getShipToAttention())));
		form.setSelectedBillToCode(Util.nullToEmpty(request.getBillToCode()));

		return form;
	}

	protected void populateAddressFormFromPriorVO(OrderAddressVO priorAddress, OENewAddressFormBean form) {
		form.setCompanyName(priorAddress.getCustomerName1());
		form.setCompanyName2(priorAddress.getCustomerName2());
		form.setAddressLine1(priorAddress.getAddressLine1());
		form.setAddressLine2(priorAddress.getAddressLine2());
		form.setAddressLine3(priorAddress.getAddressLine3());
		form.setCity(priorAddress.getCity());
		form.setZip(priorAddress.getZip());
		form.setCountry(priorAddress.getCountry().toUpperCase());
		form.setState(priorAddress.getState());
		form.setPhoneNumber(priorAddress.getPhoneNumber());
		if (ZipRequiredCountries.USA.name().equalsIgnoreCase(form.getCountry())) {
			form.setShowValidationIcon(priorAddress.isValidAddress());
			form.setHasPassedZipValidation(priorAddress.isValidAddress());
		}
		form.setFirstName(priorAddress.getFirstName());
		form.setLastName(priorAddress.getLastName());
	}

	protected void populateAddressFormFromRequest(CODeliveryInformationSaveRequest request, OENewAddressFormBean form) {
		form.setCompanyName(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getShipToName())));
		form.setCompanyName2(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getShipToName2())));
		form.setAddressLine1(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getAddressLine1())));
		form.setAddressLine2(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getAddressLine2())));
		form.setAddressLine3(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getAddressLine3())));
		form.setCity(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getCity())));
		form.setZip(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getPostalCode())));
		form.setCountry(request.getCountry().toUpperCase());
		form.setState(Util.nullToEmpty(request.getStateOrProvince()).toUpperCase());
		if ("null".equals(request.getPhoneNumber())) {
			request.setPhoneNumber(AtWinXSConstant.EMPTY_STRING);
		}
		form.setPhoneNumber(Util.htmlUnencodeQuotes(Util.nullToEmpty(request.getPhoneNumber())));
		if (ZipRequiredCountries.USA.name().equalsIgnoreCase(form.getCountry())) {
			form.setShowValidationIcon(true);
			form.setHasPassedZipValidation(true);
		}
	}

	// CAP-42241 adding to update new address fields to the request and response objects
	protected void updateAddressWithUSPSChanges(Address newAddress, CODeliveryInformationSaveRequest request,
			String errMsg) {
		request.setCity(newAddress.getCity());
		request.setStateOrProvince(newAddress.getStateOrProvince());
		request.setPostalCode(newAddress.getPostalCode());
		if (errMsg.startsWith(ModelConstants.USPS_RETURNING_CITY_AND_STATE_MESSAGE)) {
			String[] splitcityandstate = errMsg.replace(ModelConstants.USPS_RETURNING_CITY_AND_STATE_MESSAGE, "")
					.replace(ModelConstants.USPS_FOR_THE_ENTERED_ZIPCODE_MESSAGE, "").split(ModelConstants.USPS_MESSAGE_AND);
			request.setCity(splitcityandstate[0]);
			request.setStateOrProvince(splitcityandstate[1]);
		} else if (errMsg.startsWith(ModelConstants.USPS_INVALID_CITY_MESSAGE)) {
			String[] splitcityandstate = errMsg.replace(ModelConstants.USPS_INVALID_CITY_MESSAGE, "")
					.replace(ModelConstants.USPS_FOR_THE_ENTERED_ZIPCODE_MESSAGE, "").split(ModelConstants.USPS_MESSAGE_AND);
			request.setCity(splitcityandstate[0]);
			request.setStateOrProvince(splitcityandstate[1]);
		} else if (errMsg.startsWith(ModelConstants.USPS_INVALID_ZIPCODE_MESSAGE)) {
			//CAP-42675 updated constants and method to handle the multiple/ mixed USPS error
			if(errMsg.contains(ModelConstants.USPS_RETURNING_CITY_AND_STATE_MESSAGE)) {
				String[] splitzipcityandstate = errMsg.replace(ModelConstants.USPS_INVALID_ZIPCODE_MESSAGE, "")
						.replace(ModelConstants.USPS_MIXED_MESSAGE_ZIP_STATE_AND_CITY, ModelConstants.USPS_MESSAGE_AND)
						.replace(ModelConstants.USPS_RETURNING_CITY_AND_STATE_MESSAGE, "")
						.replace(ModelConstants.USPS_FOR_THE_ENTERED_ZIPCODE_MESSAGE, "").split(ModelConstants.USPS_MESSAGE_AND);
				request.setCity(splitzipcityandstate[1]);
				request.setStateOrProvince(splitzipcityandstate[2]);
				request.setPostalCode(splitzipcityandstate[0]);
			}else {
			String correctzip = errMsg.replace(ModelConstants.USPS_INVALID_ZIPCODE_MESSAGE, "")
					.replace(ModelConstants.USPS_ERROR_OF_ZIPCODE_AND_NUM_OF_CHARS_MESSAGE, "")
					.replace(ModelConstants.USPS_FIELDS_CORRECTION_MESSAGE, "")
					.replace(ModelConstants.USPS_FOR_THE_ENTERED_INFORMATION_MESSAGE, "");
			request.setPostalCode(correctzip);
			}
		} else if (errMsg.startsWith(ModelConstants.USPS_ADDRESS_NOT_FOUND)) {//CAP-42862 added condition to handle the Address Not Found - USPS error
			String[] splitcityandstate = errMsg.replace(ModelConstants.USPS_ADDRESS_NOT_FOUND, "")
					.replace(ModelConstants.USPS_FOR_THE_ENTERED_ZIPCODE_MESSAGE, "")
					.split(ModelConstants.USPS_MESSAGE_AND);
			request.setCity(splitcityandstate[0]);
			request.setStateOrProvince(splitcityandstate[1]);
		}
	}

	//CAP-43698
	protected void processShowPaymentInfo(CODeliveryInformationResponse response, SessionContainer sc) throws AtWinXSException {
		OrderDetailsBillingInfo billingInfo = objectMapFactoryService.getEntityObjectMap().getEntity(
				OrderDetailsBillingInfo.class, sc.getApplicationSession().getAppSessionBean().getCustomToken());
		billingInfo.populate(sc.getApplicationVolatileSession().getVolatileSessionBean(),
				sc.getApplicationSession().getAppSessionBean(),
				((OrderEntrySession) sc.getModuleSession()).getOESessionBean());

		OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();//CAP-48380
		OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();

		if (!billingInfo.isCreditCardRequired() && !billingInfo.isCreditCardOptional()) {
			response.setShowpayment(false);
		} else if (billingInfo.isCreditCardRequired() || billingInfo.isCreditCardOptional()) {
			response.setShowpayment(true);
		}

		if(oeOrderSessionBean.isForceCCOptionAllocation()) {// CAP-48380
			response.setShowpayment(true);
		}
	}

	// CAP-45180
	public CountriesResponse getCountries(OEOrderSessionBean oeSessionBean) throws AtWinXSException {

		CountriesResponse countriesResponse = new CountriesResponse();
		countriesResponse.setSuccess(true);

		List<NameValuePair<String>> countryOptions = new ArrayList<>();

		try {
			countryOptions = getCountryList(oeSessionBean);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			countriesResponse.setSuccess(false);
			countriesResponse.setMessage(e.getMessage());
		}
		countriesResponse.setCountryOptions(countryOptions);

		return countriesResponse;
	}

	//CAP-45181
	public StatesResponse getStates(CountryCodeRequest request) {
		StatesResponse statesResponse = new StatesResponse();
		statesResponse.setSuccess(true);
		List<NameValuePair<String>> stateOptions = new ArrayList<>();
		if (Util.isBlankOrNull(request.getCountryCode())) {
			request.setCountryCode("USA");
		}
		try {
			Iterator<String> countryIterator = UtilCountryInfo.getCountriesByName();
			while (countryIterator.hasNext()) {
				com.wallace.atwinxs.locale.ao.CountryBean cBean = UtilCountryInfo
						.getCountryByName(countryIterator.next());
				if (cBean.getCountryCode().equals(request.getCountryCode()) && cBean.getCountryHasStates()) {
					Iterator<String> states = cBean.getStatesInCountryByName();
					while (states.hasNext()) {
						StateBean sBean = cBean.getStateInCountryByName(states.next());
						stateOptions.add(new NameValuePair<>(sBean.getStateCodeText(), sBean.getStateCode()));
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			statesResponse.setSuccess(false);
			statesResponse.setMessage(e.getMessage());
		}
		statesResponse.setStateOptions(stateOptions);
		return statesResponse;
	}

		//CAP-45544 Save PAB Address
		public PABSaveResponse savePABAdderessForDeliveryInfo(SessionContainer sc, AppSessionBean appSessionBean,OEOrderSessionBean oeOrderSessionBean,PABSaveRequest pabRequest) throws AtWinXSException {
			boolean useOriginator=false;
			if (appSessionBean.isInRequestorMode() && oeOrderSessionBean.getUserSettings().isAllowOrderOnBehalf()) {
				useOriginator=true;
			}
			return selfAdminService.savePABAddress(sc, pabRequest,false,useOriginator);
		}


		//CAP-47840- Start Here

		@Override
		public DistributionListResponse getManagedLists(SessionContainer sc, boolean isOrderFromaFile) throws AtWinXSException //CP-12688
		{

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
			OEOrderSessionBean oeSessionBean = ((OrderEntrySession)sc.getModuleSession()).getOESessionBean();
			Collection<ManagedList> returnVal = new ArrayList<>();
			DistributionListResponse response=new DistributionListResponse();
			response.setSuccess(false);
			OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
			ManageListsSession mlistsSession=initManageListSession(sc);
			//Throw 403 if the user not having option in Distribution address list
			if(!validateDistributionListEnabled(userSettings)) {
				throw new AccessForbiddenException(this.getClass().getName());
			}

			//CP-12688
			boolean skipLists = isOrderFromaFile && volatileSessionBean.getShoppingCartCount()>0; //CP-12733

			// if already in an OFF order, just don't return any lists.  We put up a warning message already.
			if (!skipLists)
			{
				ViewListsAssembler assembler = new ViewListsAssembler(appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
				ViewListsRequestBean aVLReqBean = new ViewListsRequestBean();

				aVLReqBean.setSiteID(appSessionBean.getSiteID());
				aVLReqBean.setBuID(appSessionBean.getBuID());
				aVLReqBean.setLoginID(appSessionBean.getLoginID());
				aVLReqBean.setProfileID(appSessionBean.getProfileID());
				aVLReqBean.setBreadcrumbText(AtWinXSConstant.EMPTY_STRING);
				aVLReqBean.setIsWorkflow(false);
				aVLReqBean.setModuleName(AtWinXSConstant.EMPTY_STRING);
				aVLReqBean.setPrevURL(AtWinXSConstant.EMPTY_STRING);
				aVLReqBean.setNextURL(AtWinXSConstant.EMPTY_STRING);
				aVLReqBean.setSortBy("LIST_ID");
				aVLReqBean.setSelectedListID("-1"); // CP-8795
				aVLReqBean.setSingleListOnly(false); // CP-8795 - retrieve only the OFF list used
				aVLReqBean.setSelectedListsIDs(null);

					ViewListsResponseBean aVLResBean = assembler.performRetrieveList(aVLReqBean);

					ManageListsResponseBean[] manageListsResponse = aVLResBean.getListsArray();
					mlistsSession.putParameter("manageListsResponse", manageListsResponse);
					DistributionListDetails distributionListDetails = objectMapFactoryService.getEntityObjectMap().getEntity(
							DistributionListDetails.class, appSessionBean.getCustomToken());
					for (ManageListsResponseBean manageLists : manageListsResponse)
					{
						ManagedListImpl distList = new ManagedListImpl();


						distList.setBUID(Integer.toString(appSessionBean.getBuID()));
						distList.setSiteID(Integer.toString(appSessionBean.getSiteID()));
						distList.setListName(manageLists.getListName());
						distList.setRecordCount(manageLists.getRecordCount());

						//CAP-48412 - validate and set flag in description field if Listmap exists in XST413
						if (hasListMap(appSessionBean, distributionListDetails, manageLists.getListID())) {

							distList.setListDescription(Util.nullToEmpty(manageLists.getListDescription()));
						} else {

							distList.setListDescription(OrderEntryConstants.NOT_MAPPED_COL);
						}
						distList.setListID(Util.encryptString(DIST_LIST_ID_URL_PARAM + manageLists.getListID()));//CAP-47840
						returnVal.add(distList);
					}
				}
			persistSession(sc, mlistsSession);
			response.setManagedList(returnVal);
			response.setSuccess(true);
			return response;
		}

		// CAP-48412 - validate mapped List from 413 Table
		public boolean hasListMap(AppSessionBean asb, DistributionListDetails distributionListDetails, String listID) {

			SiteListMapping siteListMapping = new SiteListMappingImpl();
			siteListMapping.setBuID(asb.getBuID());
			siteListMapping.setSiteID(asb.getSiteID());
			siteListMapping.setListID(Integer.parseInt(listID));
			return distributionListDetails.hasRecordInListMapTable(siteListMapping);
		}

		// check if Admin enabled distribution list in BU & UG
		public boolean validateDistributionListEnabled(OEResolvedUserSettingsSessionBean userSettings) {
			return ((userSettings != null) && userSettings.isAllowListOrders() && userSettings.isEnableDistrListFile());
		}

		//CAP-47840- End Here


		@Override
		public DistListCountResponse getDistributionListRecordCount(SessionContainer sc, DistListCountRequest request)
				throws AtWinXSException{

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
			//Throw 403 if the user not having option in Distribution address list
			// CAP-46517, CAP-46518, CAP-42227 - add check for valid cust doc merge UI if not in distribution
			if (invalidAuthorizationForListProcess(sc)) {
				throw new AccessForbiddenException(this.getClass().getName());
			}

			// load listsSession
			ManageListsSession mlistsSession = null;
			DistributionListDetails distributionListDetails = null;
			try {
				mlistsSession = (ManageListsSession) loadListSession(sc);

				sc.addReferenceSession(AtWinXSConstant.LISTS_SERVICE_ID, mlistsSession);
				if (null != mlistsSession) {
					distributionListDetails = (DistributionListDetails) mlistsSession
							.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT);
				}
			} catch (Exception se) { // if we fail to make this session or it is the wrong type, they shouldn't have it - access forbidden
				throw new AccessForbiddenException(this.getClass().getName());
			}

			if (null == distributionListDetails) {
				throw new AccessForbiddenException(this.getClass().getName());
			}

			DistListCountResponse response = new DistListCountResponse();
			response.setSuccess(true);

			Integer recordCnt = distributionListDetails.getRecordCount();

			if (!Util.isBlankOrNull(distributionListDetails.getFileName())) {
				distributionListDetails.setSourceFileName(distributionListDetails.getFileName());
			}

			try
			{
				if(!Util.isBlankOrNull(request.getWorksheetName()))//CAP-48794
				{
					// CAP-48794 TH
					moveOriginalFileToCserve(appSessionBean, distributionListDetails);

					recordCnt = distributionListDetails.getRecordCountForDistributionList(distributionListDetails,
							request.getWorksheetName(), appSessionBean, mlistsSession,
							oeSessionBean.getDistributionListBean());

					distributionListDetails.setRecordCount(recordCnt);
					distributionListDetails.setSheetName(request.getWorksheetName());
				}
				// CAP-49054 TH - Fixed issue with xlxs with one sheet and distribution list
				else
				{
					distributionListDetails.setSourceFileName(distributionListDetails.getFileName());
				}
				mlistsSession.putParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT, distributionListDetails);
				SessionHandler.saveSession(mlistsSession, sc.getApplicationSession().getAppSessionBean().getSessionID(),
						AtWinXSConstant.LISTS_SERVICE_ID);

			} catch (AtWinXSException msgexc) {

				try {
					FileDownload.performDelete(appSessionBean.getGlobalFileUploadPath(),
							distributionListDetails.getSourceFileName());
				} catch (AtWinXSException e) {
					logger.error(e.getMessage());
					response.setSuccess(false);
				}
				logger.error(msgexc.getMessageExt());
				response.setSuccess(false);
			}

			response.setRecordCount(recordCnt);
			return response;

		}

		// CAP-48794 TH
		protected void moveOriginalFileToCserve(AppSessionBean appSessionBean,  DistributionListDetails distributionListDetails) throws AtWinXSException
		{
			XSProperties props = PropertyUtil.getProperties(ICustomDocsAdminConstants.WEBSERVICES_PROPERTIES);
			String tempPath = Util.nullToEmpty(props.getProperty(ICustomDocsAdminConstants.CONVERT_EXCEL_TEMP_DIR));

			File origFile = new File(appSessionBean.getGlobalFileUploadPath() + distributionListDetails.getOrigExcelFileName());
			File moveFile = new File(tempPath + distributionListDetails.getOrigExcelFileName());

			try
			{
				Files.deleteIfExists(moveFile.toPath());
			}
			catch(IOException ioEx)
			{
				// Just log the error here
				logger.debug(new StringBuffer("Could not delete file ").append(moveFile.getName()).toString());
			}

			FileUtils.copyFile(origFile, moveFile, false);
		}

		//CAP-47998
		@Override
		public SaveManagedFieldsResponse saveManagedListFields(SessionContainer sc, CreateListVarsRequest request)
				throws AtWinXSException {
			//Throw 403 if the user not having option in Distribution address list
			// CAP-46517, CAP-46518, CAP-42227 - add check for valid cust doc merge UI if not in distribution
			if (invalidAuthorizationForListProcess(sc)) {
				throw new AccessForbiddenException(this.getClass().getName());
			}

			SaveManagedFieldsResponse response=new SaveManagedFieldsResponse();
			ManageListsSession mlistsSession=initManageListSession(sc);

			Map<String, Object> multipartReqParams =new HashMap<>();
			multipartReqParams.put(FileUploadConstants.FILESIZE, 100L);

			//Duplication check
			DistributionListDetails distributionListDetails = objectMapFactoryService.getEntityObjectMap().getEntity(DistributionListDetails.class, sc.getApplicationSession().getAppSessionBean().getCustomToken());
			mlistsSession.putParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT, distributionListDetails);

			//CAP-48855 - isSharedList()=false set as Private else Shared
			distributionListDetails.populate(request.getListName(), request.getListDescription(), request.isHeaders(), !request.isSharedList(), "",
					"", 0, multipartReqParams, sc.getApplicationSession().getAppSessionBean());



			response.setSuccess(false);

			checkListNameAndDesEmptyOrMaxLen(sc, request, response);
			validateListNameCharacters(sc,request,response);//CAP-48777
			if(response.getFieldMessages().isEmpty()) {
				int duplicateCheck=performDuplicateCheck(sc.getApplicationSession().getAppSessionBean(), distributionListDetails);
				if (duplicateCheck == ManageListsConstants.DUPLICATE_SAME_OWNER
						|| duplicateCheck == ManageListsConstants.DUPLICATE_NOT_OWNER) {
					Map<String, Object> attrReplaceMap = new HashMap<>();
					attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_LABEL, ModelConstants.LIST_NAME_LBL);
					response.setFieldMessage(ModelConstants.LIST_NAME_LBL,
							translationService.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
									sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.LIST_NAME_DUPLICATE_MSG));
				}else{

					ManageListsResponseBean manageLists = new ManageListsResponseBean();
					manageLists.setListName(request.getListName());

					//CAP-48855 - isSharedList()=false set as Private else Shared
					manageLists.setIsPrivate(!request.isSharedList());
					manageLists.setListDescription(request.getListDescription());
					manageLists.setContainsHeadings(request.isHeaders());
					manageLists.setLoginID(sc.getApplicationSession().getAppSessionBean().getLoginID());
					mlistsSession.putParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT, manageLists);
					mlistsSession.putParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT, distributionListDetails);
					SessionHandler.saveSession(mlistsSession, sc.getApplicationSession().getAppSessionBean().getSessionID(),
			                AtWinXSConstant.LISTS_SERVICE_ID);
					response.setSuccess(true);
				}

			}
			return response;
		}


		public void checkListNameAndDesEmptyOrMaxLen(SessionContainer sc, CreateListVarsRequest request,SaveManagedFieldsResponse response) throws AtWinXSException{
			Map<String, Object> attrReplaceMap = new HashMap<>();
			if(request.getListName()==null || request.getListName().isEmpty()){
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_LABEL, request.getListName());
				response.setFieldMessage(ModelConstants.LIST_NAME_LBL,translationService.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
						sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.LIST_NAME_EMPTY_MSG));
			}

			if(request.getListName()!=null && request.getListName().length()>ModelConstants.MAX_LENGTH_LIST_NAME) {
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_LABEL, ModelConstants.LIST_NAME_LBL);
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_MAX_CHARS, Integer.toString(ModelConstants.MAX_LENGTH_LIST_NAME));
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_CUR_NUM, Integer.toString(request.getListName().length()));
				response.getFieldMessages().put(ModelConstants.LIST_NAME_LBL, getTranslation(sc.getApplicationSession().getAppSessionBean(), SFTranslationTextConstants.LIST_ERROR_MSG,
						SFTranslationTextConstants.LIST_ERROR_MSG_DESC, attrReplaceMap));

			}

			if(request.getListDescription().length()>ModelConstants.MAX_LENGTH_LIST_DESC) {
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_LABEL, ModelConstants.LIST_DESC_LBL);
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_MAX_CHARS, Integer.toString(ModelConstants.MAX_LENGTH_LIST_DESC));
				attrReplaceMap.put(TranslationTextConstants.TRANS_NM_VAR_CUR_NUM, Integer.toString(request.getListDescription().length()));
				response.getFieldMessages().put(ModelConstants.LIST_DESC_LBL, getTranslation(sc.getApplicationSession().getAppSessionBean(), SFTranslationTextConstants.LIST_ERROR_MSG,
						SFTranslationTextConstants.LIST_ERROR_MSG_DESC,attrReplaceMap));

			}

		}


		public int performDuplicateCheck(AppSessionBean appSessionBean,
				DistributionListDetails distributionListDetails)
				throws AtWinXSException {

			DistributionListDetailsImpl distImpl = new DistributionListDetailsImpl();
			ListVO aListVO = null;
			int isDuplicate = 0;
			try {

				aListVO = distImpl.instantiateListVO(appSessionBean, distributionListDetails);
				IManageList anIMLinstance = getManageListsLocator(appSessionBean);
				isDuplicate = anIMLinstance.checkDuplicateListName(aListVO,
						ManageListsConstants.UPLOAD_LIST_VERIFICATION_EVENT);
			} catch (AtWinXSException e) {
				throw new AtWinXSException(e.getMessage(), e.getClass().getName());
			}
			return isDuplicate;
		}


		public ManageListsSession initManageListSession(SessionContainer sc) throws AtWinXSException {
			BaseSession listSession = createManageListSession(sc.getApplicationSession().getAppSessionBean(), sc.getApplicationSession().getSessionID());
			sc.addReferenceSession(AtWinXSConstant.LISTS_SERVICE_ID, listSession);
			return (ManageListsSession) listSession;
		}

		public BaseSession createManageListSession(AppSessionBean appSessionBean, int sID) throws AtWinXSException
		{
			ManageListsSession mls = new ManageListsSession();
			mls.init(appSessionBean);
			SessionHandler.createSession(mls, sID, AtWinXSConstant.LISTS_SERVICE_ID, appSessionBean.getSiteID(), appSessionBean.getLoginID());
			return mls;
		}

		public IManageList getManageListsLocator(AppSessionBean appSessionBean) throws AtWinXSException {
			return ManageListsLocator.locate(appSessionBean.getCustomToken());
		}

		//CAP-47389 CAP-48123
		//This code copied from CP code ListsServiceImpl.getDistributionListAddresses method
		/**
		 * This method will get the Addresses from selected Distribution List
		 * @param sessionID
		 * @param selectedListID
		 * @param selectedListIDs
		 * @return DistListAddressResponse
		 * @throws AtWinXSException
		 */
		@Override
		public DistListAddressResponse getDistributionListAddresses(SessionContainer sc, DistListAddressRequest request) throws CPRPCException, AtWinXSException
		{
			DistListAddressResponse response = new DistListAddressResponse();
			boolean isDistListWithItemQty = false;
			String encSelectedListID = request.getDistListID();
			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
			OEOrderSessionBean oeSessionBean = ((OrderEntrySession)sc.getModuleSession()).getOESessionBean();
			OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();
			String listNotExistErrMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), SFTranslationTextConstants.DLIST_NOT_EXIST);


			String selectedListID ="";

			try {
				selectedListID = Util.decryptString(encSelectedListID);
				selectedListID = selectedListID.replace(DIST_LIST_ID_URL_PARAM, "");

			}catch (Exception e) {
				response.setSuccess(false);
				response.setMessage(listNotExistErrMsg);
				return response;
			}

			try {
				sc.addReferenceSession(AtWinXSConstant.LISTS_SERVICE_ID, loadListSession(sc));
			}catch (Exception e) {
				throw new AccessForbiddenException(this.getClass().getName());
			}
			Object tmpMlistsSession = sc.getReferenceSessions().get(AtWinXSConstant.LISTS_SERVICE_ID);

			//Throw 403 if the user not having option in Distribution address list OR if tmpMlistsSession not available
			if (!validateDistributionListEnabled(userSettings) || null == tmpMlistsSession) {

				throw new AccessForbiddenException( this.getClass().getName());
			}

			ManageListsSession mlistsSession = (ManageListsSession) tmpMlistsSession;

			ManageListsAdminSessionBean mlistsAdminSession = (ManageListsAdminSessionBean) mlistsSession.getSessionBean().getManageListsAdminSession();
			boolean isAllowListFeedPreview = mlistsAdminSession.isBUAllowListFeedPreview();

			Collection<Address> returnVal = new ArrayList<>();
			DistributionListDetails distributionListDetails = objectMapFactoryService.getEntityObjectMap().getEntity(DistributionListDetails.class, appSessionBean.getCustomToken());
			try
			{
				mlistsSession.putParameter("distributionListDetails", distributionListDetails);
			}
			catch(AtWinXSException ae)
			{
				response.setSuccess(false);
				response.setMessage(listNotExistErrMsg);
				return response;

			}
			MapperData mapperdata;

			boolean isListExist = false;
			boolean hasAddressContents = false;
			//CP-9626
			boolean isFromListFeed = false;
			ManageListsResponseBean[] manageListsResponse = (ManageListsResponseBean[])mlistsSession.getParameter("manageListsResponse");
			if(manageListsResponse != null)
			{
				for (ManageListsResponseBean manageLists : manageListsResponse)
				{
					if (manageLists.getListID().equalsIgnoreCase(selectedListID))
					{
						setDistributionListBean(manageLists, oeSessionBean, appSessionBean);
						isListExist = true;
						isFromListFeed = manageLists.isListFeedInd();
						break;
					}
				}
			}
			OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
			oeSessionBean.setSendToDistListUsingQtyFromFile(isDistListWithItemQty);

			if (isListExist)
			{
				try
				{
					if (distributionListDetails.validatefile(distListBean) || isFromListFeed)
					{
						SiteListMapping siteListMappingObj = objectMapFactoryService.getEntityObjectMap().getEntity(SiteListMapping.class, appSessionBean.getCustomToken());
						siteListMappingObj.populate(appSessionBean, distListBean, appSessionBean.getLoginID());

						//CP-9481 Updated condition
						if(!distributionListDetails.hasRecordInListMapTable(siteListMappingObj))
						{
							response.setAddresses(returnVal);
							response.setSuccess(false);
							response.setMessage(listNotExistErrMsg);
							return response;

						}

						mapperdata = getOrderwithDistListPreview(
									appSessionBean.getGlobalFileUploadPath(),
									oeSessionBean,
									appSessionBean,
									volatileSessionBean);
						distributionListDetails.populate(mapperdata);

			            boolean showAddress = true;
			            if(isFromListFeed && !isAllowListFeedPreview)
			            {
			            	showAddress = false;
			            }
						for (String[] array : distributionListDetails.getData())
						{
							hasAddressContents = true;
							if (distributionListDetails.getHeadings() != null || mapperdata.getAltColumnNames() != null)
							{
								AddressSearchResult addressReturn = new AddressSearchResult();
								//CP-10934
								Map<String, Item> items = new HashMap<>();
								if (array.length == distributionListDetails.getMaxCols())
								{
									String mappedColNames = siteListMappingObj.getMappedColumnNamesStr();
									String[] mappedColNamesArr = mappedColNames != null && !"".equals(mappedColNames) ? mappedColNames.split(":") : new String[0];

									if (mappedColNamesArr != null && mappedColNamesArr.length > 0)
									{
										String column = "";
										int colIndex = 0;
										for (String mappedColumn : mappedColNamesArr)
										{
											String[] colNameArr = mappedColumn.split("=");
											if (colNameArr != null && colNameArr.length == 2)
											{
												column = colNameArr[0];
												colIndex = Integer.valueOf(colNameArr[1].trim());

												if (column.equalsIgnoreCase(AltColumnNames.Name1.toString()))
													addressReturn.setShipToName1(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.Name2.toString()))
													addressReturn.setShipToName2(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.ShipToAttention.toString()))
													addressReturn.setShipToName1(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.Address1.toString()))
													addressReturn.setAddressLine1(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.Address2.toString()))
													addressReturn.setAddressLine2(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.Address3.toString()))
													addressReturn.setAddressLine3(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.City.toString()))
													addressReturn.setCity(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.Country.toString()))
													addressReturn.setCountry(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.Zip.toString()))
													addressReturn.setPostalCode(array[colIndex]);
												else if (column.equalsIgnoreCase(AltColumnNames.State.toString()))
													addressReturn.setStateOrProvince(array[colIndex]);
												// For Item and Qty
												//CP-10934 changed how Item/Qty are built
												else if (column.startsWith(AltColumnNames.ItemPrefix.toString()))
												{
													Item item = items.get(column);
													if(item == null)
													{
														item = new Item();
														items.put(column, item);
													}
													item.setItem(array[colIndex]);
												}
												else if (column.startsWith(AltColumnNames.QuantityPrefix.toString()))
												{
													String key = "Item " + column.substring(AltColumnNames.QuantityPrefix.toString().length());
													Item item = items.get(key);
													if(item == null)
													{
														item = new Item();
														items.put(key, item);
													}
													item.setQty(array[colIndex]);
												}
												//CP-10934 added UOM
												else if(column.startsWith(AltColumnNames.UOMCodePrefix.toString()))
												{
													String key = "Item " + column.substring(AltColumnNames.UOMCodePrefix.toString().length());
													Item item = items.get(key);
													if(item == null)
													{
														item = new Item();
														items.put(key, item);
													}
													item.setUom(array[colIndex]);
												}

											}

										}
									}

									//CP-10934 changed how item image is added and how items are sorted
									Collection<Item> sortedItems = new ArrayList<>();

									SortedSet<String> keys = new TreeSet<>(items.keySet());
									for(String key : keys)
									{
										Item item = items.get(key);
										sortedItems.add(item);
									}

									loadItemImages(sortedItems, appSessionBean);

									addressReturn.setItems(sortedItems);
									addressReturn.setDisplayAddress(showAddress);
									returnVal.add(addressReturn);
								}
							}
						}
					}

				}
				catch (AtWinXSException e)
				{

					response.setSuccess(false);
					response.setMessage(listNotExistErrMsg);
					return response;

				}
			}
			else
			{
				response.setSuccess(false);
				response.setMessage(listNotExistErrMsg);
				return response;

			}

			// Throw error message if no address was found.
			if (!hasAddressContents)
			{
				response.setSuccess(false);
				response.setMessage(listNotExistErrMsg);
				return response;

			}
		    persistSession(sc,mlistsSession);

			response.setAddresses(returnVal);
			response.setSuccess(true);
			return response;

		}

		//CP-10934 CAP-48123 listutil
		protected void loadItemImages(Collection<Item> sortedItems, AppSessionBean asb) throws AtWinXSException
		{
			Map<String, String> itemImages = new HashMap<>();
			ICatalog catComp = CMCatalogComponentLocator.locate(asb.getCustomToken());
			for(Item item : sortedItems)
			{
				String itemNum = item.getItem();
				String itemImage = itemImages.get(itemNum);
				if(itemImage == null)
				{
					ItemImagesVO imageVO = catComp.getImagesForItem(new ItemImagesVOKey(asb.getSiteID(), itemNum, AtWinXSConstant.EMPTY_STRING));

					if (imageVO != null)
					{
						ItemImagesVOFilter image = new ItemImagesVOFilter(imageVO);
						itemImage = image.getQualifiedItemMedImgLocURL(asb);
					}
					else
					{
						itemImage = asb.getGlobalFileUploadPath() + "/images/global/NoImageAvailable.png";
					}


					itemImages.put(itemNum, itemImage);
				}

				item.setIconLink(itemImage);
			}
		}


	// CAP-48002 - Step2 distList upload and return Worksheet name(s)
	@Override
	public WorksheetResponse uploadDistListFileAndGetWorksheetName(SessionContainer sc, WorksheetRequest request)
			throws AtWinXSException {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		//Throw 403 if the user not having option in Distribution address list
		// CAP-46517, CAP-46518, CAP-42227 - add check for valid cust doc merge UI if not in distribution
		if (invalidAuthorizationForListProcess(sc)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		List<String> workSheetNames = new ArrayList<>();
		WorksheetResponse response = new WorksheetResponse();
		response.setSuccess(true);

		String errorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), SFTranslationTextConstants.GENERIC_INVALID_REQUEST_ERROR_KEY);

		try {
			//load listsSession
			sc.addReferenceSession(AtWinXSConstant.LISTS_SERVICE_ID, loadListSession(sc));

			Object tmplistsSession = sc.getReferenceSessions().get(AtWinXSConstant.LISTS_SERVICE_ID);

			// Throw 403 if tmpMlistsSession not available
			if (null == tmplistsSession) {

				throw new AccessForbiddenException(errorMessage, this.getClass().getName());
			}

			ManageListsSession mlistsSession = (ManageListsSession) tmplistsSession;
			ManageListsResponseBean manageLists = (ManageListsResponseBean) mlistsSession
					.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT);
			DistributionListDetails distributionListDetails = (DistributionListDetails) mlistsSession
					.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT);

			if (null == distributionListDetails) {
				throw new AccessForbiddenException(this.getClass().getName());
			}

			int duplicateCheck = performDuplicateCheck(appSessionBean, distributionListDetails);

			if (duplicateCheck == ManageListsConstants.DUPLICATE_SAME_OWNER
						|| duplicateCheck == ManageListsConstants.DUPLICATE_NOT_OWNER) {

				throw new AccessForbiddenException(errorMessage, this.getClass().getName());
			}

			// Throw 422, if validation error exists
			validateUploadedFile(request, appSessionBean, response);
			if (response.getFieldMessages().size() > 0) {

				response.setSuccess(false);
				response.setFieldMessages(response.getFieldMessages());
				return response;
			}

			// set Unique filename for Custlist file and upload to GlobalFileUploadPath
			String uniqueOriginalFilename = convertFileNameWithUniqueID(Util.nullToEmpty(request.getDistFile().getOriginalFilename()));
			uploadDistListFile(request.getDistFile(), uniqueOriginalFilename, appSessionBean);

			// append manageLists session object with file upload details
			manageLists.setCustomerFileName(request.getDistFile().getOriginalFilename());
			manageLists.setSourceFileName(uniqueOriginalFilename);
			manageLists.setFileSize(String.valueOf(request.getDistFile().getSize()));

			// set distListDetails, distListBean with additional uploadfile info
			distributionListDetails.setCustomerFileName(manageLists.getCustomerFileName());
			distributionListDetails.setSourceFileName(manageLists.getSourceFileName());
			// CAP-49714 - add file name to update it to XST030_LIST=>SRC_FILE_LOC_TX
			distributionListDetails.setFileName(manageLists.getSourceFileName());
			distributionListDetails.setFilesize(Integer.parseInt(manageLists.getFileSize()));

			// CAP-48855
			distributionListDetails.setOrigExcelFileName(uniqueOriginalFilename);
			if (ManageListsUtil.isConvertExcel()) {

				distributionListDetails.setIsExcel(
						ManageListsConstants.isExcel2007Document(distributionListDetails.getCustomerFileName()));
			} else {

				distributionListDetails.setIsExcel(
						ManageListsConstants.isExcelDocument(distributionListDetails.getCustomerFileName()));
			}
			setDistributionListBean(manageLists, oeSessionBean, appSessionBean);

			// get worksheet(s) names for xls,xlsx OR
			// set record count in getDistributionListBean if xls, xlsx, csv file with only one sheet
			distributionListDetails.setPafDetails(distributionListDetails, appSessionBean.getSiteID(),
					appSessionBean.getBuID(), appSessionBean.getProfileNumber());

			workSheetNames = processExcelCSV(oeSessionBean.getDistributionListBean(),
					appSessionBean, mlistsSession, distributionListDetails, response);//CAP-48728

			// save session multipartReqParams and mlistsSession with manageLists,
			// distributionListDetails objects
			mlistsSession.putParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT, manageLists);
			mlistsSession.putParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT, distributionListDetails);
			persistSession(sc, mlistsSession);
		} catch (AtWinXSException e) {

			logger.error(e.getMessage(), e);
			throw new AccessForbiddenException(errorMessage, AtWinXSConstant.EMPTY_STRING);
		}


		response.setWorkSheetNames(workSheetNames);

		return response;
	}

	// CAP-48002 - added scoped code from ListsUtil.java->performUploadListVerification and resolve sonar issue
	public List<String> processExcelCSV(OEDistributionListBean distListBean, AppSessionBean appSessionBean,
			ManageListsSession mlistsSession, DistributionListDetails distributionListDetails, WorksheetResponse response)
			throws AtWinXSException {

		List<String> returnVal = new ArrayList<>();

		try {

			if (distributionListDetails.getIsExcel()) {

					returnVal = distributionListDetails.processConvertedExcelSheetNames(distListBean, appSessionBean,
							mlistsSession, distributionListDetails);
			} else {

				// CP-4282 Convert Excel
				if (ManageListsConstants.isCSVFile(distListBean.getListSysFileName())) {

					// count set on the bean
					distributionListDetails.setCVSRecordCount(distListBean, appSessionBean, distributionListDetails);
					if (distributionListDetails.getRecordCount() < 1) {
						response.setSuccess(false);// CAP-48728
						response.getFieldMessages()
								.put(translationService.processMessage(appSessionBean.getDefaultLocale(),
										appSessionBean.getCustomToken(),
										SFTranslationTextConstants.DLIST_UPLOAD_FAILED),
										ManageListsConstants.ERROR_INVALID_FILE);// CAP-48728
						returnVal.add(ERROR_LBL + ManageListsConstants.ERROR_INVALID_FILE);
					}
				}
			}

		} catch (AtWinXSException e) {

			FileDownload.performDelete(appSessionBean.getGlobalFileUploadPath(), distListBean.getListSysFileName());
			logger.error(Util.class.getName() + " - " + e.getMessage(), e);
		} catch (IOException e) {

			// CAP-18657 Replaced printstacktrace() call with Logger
			logger.error(Util.class.getName() + " - " + e.getMessage(), e);
		}
		return returnVal;

	}

	// CAP-48002 - added method from ListsUtil.java and resolve sonar issue
	public void setDistributionListBean(ManageListsResponseBean manageLists, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean) throws AtWinXSException {
		COListsUtil.setDistributionListBean(manageLists, oeSessionBean, appSessionBean);
	}

	// CAP-48002 - check and add error message if distlist file is Empty/null
	private void validateUploadedFile(WorksheetRequest request, AppSessionBean appSessionBean,
			WorksheetResponse response) throws AtWinXSException {

		if (request.getDistFile() == null || request.getDistFile().isEmpty()) {

			response.getFieldMessages().put(
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.DLIST_UPLOAD_FAILED),
					AtWinXSConstant.BLANK_SPACE + buildErrorMessage(SFTranslationTextConstants.FILE_NOT_FOUND, appSessionBean, null));
		} else {

			validateFileExtension(request.getDistFile().getOriginalFilename(), appSessionBean, response);
		}
	}

	// CAP-48002 - check and add error message if file extension other than xls,
	// xlsx, csv
	private void validateFileExtension(String fullName, AppSessionBean appSessionBean, WorksheetResponse response)
			throws AtWinXSException {

		String[] fileExtnCode = { "csv", "xls", "xlsx" };
		String fileName = new File(fullName).getName();
		int dotIndex = fileName.lastIndexOf('.');

		if (!Arrays.asList(fileExtnCode).contains(fileName.substring(dotIndex + 1))) {

			response.getFieldMessages().put(
					translationService.processMessage(appSessionBean.getDefaultLocale(),
							appSessionBean.getCustomToken(), SFTranslationTextConstants.DLIST_UPLOAD_FAILED),
					AtWinXSConstant.BLANK_SPACE + buildErrorMessage(SFTranslationTextConstants.UPLOAD_DLIST_ERR_MSG,
							appSessionBean, null));
		}
	}

	// CAP-48002
	private void uploadDistListFile(MultipartFile file, String uniqueOriginalFilename,
			AppSessionBean appSessionBean) throws AtWinXSException {

		File importFile = new File(appSessionBean.getGlobalFileUploadPath(), uniqueOriginalFilename);
		try {

			file.transferTo(importFile);

		} catch (IllegalStateException | IOException e) {

			logger.error(this.getClass().getName() + " - " + e.getMessage(), e);
		}
	}

	// CAP-48002
	public String convertFileNameWithUniqueID(String fileName) {

		return FileUploadConstants.getUniqueID() + fileName;
	}

	// CAP-48002
	private String buildErrorMessage(String errorKey, AppSessionBean asb, Map<String, Object> replaceMap)
			throws AtWinXSException {

		return Util.nullToEmpty(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
				errorKey, replaceMap));
	}

	// CAP-48002
	private void persistSession(SessionContainer sc, ManageListsSession mlistsSession) throws AtWinXSException {

		SessionHandler.saveSession(sc.getModuleSession(),
				sc.getApplicationSession().getAppSessionBean().getSessionID(), AtWinXSConstant.ORDERS_SERVICE_ID);
		SessionHandler.saveSession(mlistsSession, sc.getApplicationSession().getAppSessionBean().getSessionID(),
				AtWinXSConstant.LISTS_SERVICE_ID);
	}

	//CAP-47391
	@Override
	public DistListUpdateResponse processUseThisList(DistListUpdateRequest distListUpdateRequest, SessionContainer sc) throws AtWinXSException {
		DistListUpdateResponse response = new DistListUpdateResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		OEOrderSessionBean oeOrderSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeOrderSessionBean.getUserSettings();

		if (!validateDistributionListEnabled(userSettings)) {
			response.setSuccess(false);
			throw new AccessForbiddenException(this.getClass().getName());
		}

		OECheckoutAssembler checkoutAssembler = OEAssemblerFactory.getCheckoutAssembler(volatileSessionBean,
				appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
				appSessionBean.getApplyExchangeRate(), appSessionBean.getCurrencyLocale());

		DeliveryOptionsFormBean deliveryOptions = populateDeliveryOptions(checkoutAssembler, volatileSessionBean,
				punchoutSessionBean, oeOrderSessionBean, appSessionBean);

		String listID = decryptListID(distListUpdateRequest.getDistListID());
		com.rrd.custompoint.orderentry.entity.List list = objectMapFactoryService.getEntityObjectMap().getEntity(com.rrd.custompoint.orderentry.entity.List.class, appSessionBean.getCustomToken());
        list.populate(appSessionBean.getSiteID(), appSessionBean.getBuID(), Integer.parseInt(listID));

		buildDeliveryOptionsBean(distListUpdateRequest, deliveryOptions, list);
		saveAddress(appSessionBean, deliveryOptions, oeOrderSessionBean, checkoutAssembler);

		Message msg = validateList(oeOrderSessionBean, deliveryOptions, appSessionBean, volatileSessionBean);

		if (msg != null
				&& ((!Util.isBlankOrNull(msg.getErrInstructionMsg()))
				|| (null != msg.getErrMsgItems() && !CollectionUtils.isEmpty(msg.getErrMsgItems()))))
		{
			StringBuilder sbMsg = new StringBuilder();
			sbMsg.append(msg.getErrInstructionMsg());

			if(null != msg.getErrMsgItems() && !CollectionUtils.isEmpty(msg.getErrMsgItems()))
			{
				for(String errorMsg : msg.getErrMsgItems())
				{
					sbMsg.append("<br>");
					sbMsg.append("&raquo; " + errorMsg);
				}
			}

			deliveryOptions.setDisplayListSuggestionPopup(true);
			deliveryOptions.setListSuggestionErrMsg(sbMsg.toString());

			throw new AtWinXSMsgException(msg, this.getClass().getName());
		}

		updateDistributionListDetails(appSessionBean, volatileSessionBean, oeOrderSessionBean,
				deliveryOptions.getSelectedListID(), String.valueOf(deliveryOptions.getRecordCount()));
		updateListByLastUseDate(appSessionBean, deliveryOptions);
		updateOrderScenarioNumber(appSessionBean, volatileSessionBean, deliveryOptions.getAddressSource(), oeOrderSessionBean);

		if (userSettings.isEnforceStateRestrictions()) {
			checkoutAssembler.enforceStateRestrictions(appSessionBean, oeOrderSessionBean);
		}

		SessionHandler.saveSession(sc.getModuleSession(), sc.getApplicationSession().getAppSessionBean().getSessionID(),
				AtWinXSConstant.ORDERS_SERVICE_ID);

		response.setSuccess(true);
		return response;
	}

	private void buildDeliveryOptionsBean(DistListUpdateRequest distListUpdateRequest,
			DeliveryOptionsFormBean deliveryOptions, com.rrd.custompoint.orderentry.entity.List list) {
		deliveryOptions.setAddressSource(DIST_LIST_ADDRESS);
		deliveryOptions.setSelectedListID(String.valueOf(list.getListID()));
		deliveryOptions.setShipToAttention(distListUpdateRequest.getShipToAttention());
		deliveryOptions.setRecordCount(String.valueOf(list.getRecordCount()));
	}

	private String decryptListID(String encryptedListID) {
		String decryptedListID = AtWinXSConstant.EMPTY_STRING;
		if (!Util.isBlankOrNull(encryptedListID)) {
			decryptedListID= Util.decryptString(encryptedListID);
			decryptedListID = decryptedListID.replace(DIST_LIST_ID_URL_PARAM, "");
		}
		return decryptedListID;
	}

	// CAP-48277 - CP Method from ListsServiceImpl will upload a file for the Distribution List
	// CAP-48434
	@Override
	public UploadDistListResponse uploadDistList(SessionContainer sc, UploadDistListRequest uploadDistListRequest,
			boolean isOrderFromaFile, boolean isDistListWithItemQty) throws AtWinXSException {

		UploadDistListResponse response = new UploadDistListResponse();
		response.setSuccess(true);

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		OEResolvedUserSettingsSessionBean userSettings = oeSessionBean.getUserSettings();

		String errorMessage = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), SFTranslationTextConstants.GENERIC_SERVICE_FAILED_ERR);

		// Throw 403 if the user not enabled to use Distribution address list,
		if (!validateDistributionListEnabled(userSettings)) {

			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {
			// load listsSession
			sc.addReferenceSession(AtWinXSConstant.LISTS_SERVICE_ID, loadListSession(sc));

			Object tmplistsSession = sc.getReferenceSessions().get(AtWinXSConstant.LISTS_SERVICE_ID);

			// Throw 422 if save Dist List is called before step1, step2 , step3 API
			if (null == tmplistsSession) {

				response.setSuccess(false);
				response.setMessage(errorMessage);
				return response;
			}

			ManageListsSession mlistsSession = (ManageListsSession) tmplistsSession;

			ManageListsResponseBean manageLists = (ManageListsResponseBean) mlistsSession
					.getParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT);
			DistributionListDetails distributionListDetails = (DistributionListDetails) mlistsSession
					.getParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT);

			String selectedListID = getDecryptedListID(appSessionBean, uploadDistListRequest.getListID(), response);
			// If encrypted List ID is Not Empty and Invalid
			if(!response.isSuccess()) {
				return response;
			}

			// Throw 422 if save Dist List is called before step1, step2 , step3 API
			if ((null == manageLists || null == distributionListDetails)
					&& Util.isBlankOrNull(selectedListID)) {
				response.setSuccess(false);
				response.setMessage(errorMessage);
				return response;

			}

			// Throw 422 if save Dist in XST030 and return List ID, List ID is Empty
			if(Util.isBlankOrNull(selectedListID)) {

				selectedListID = addList(distributionListDetails, appSessionBean);
			}
			else {

			// Initiate session object to re-populate Map Dist List details
			distributionListDetails = objectMapFactoryService.getEntityObjectMap().getEntity(
					DistributionListDetails.class, appSessionBean.getCustomToken());
			manageLists = new ManageListsResponseBean();

			}

			distributionListDetails.setListID(selectedListID);
			distributionListDetails.setRecordCount(uploadDistListRequest.getRecordCount());//CAP-48898

			LMapperData lMapperData = null;

			if (!isOrderFromaFile) {

				lMapperData = mapDistList(sc, selectedListID, isDistListWithItemQty);
			} else {
				// CP mapOFFList() not used for CAP-48277 scope, will be used in future
				lMapperData = mapOFFList(sc, selectedListID);
			}

			COListDataMapper cplmapper = new COListDataMapper();
			BeanUtils.copyProperties(cplmapper, lMapperData);
			response.setLmapper(cplmapper);

			mlistsSession.putParameter(ModelConstants.MANAGE_LIST_SESSION_OBJECT, manageLists);
			mlistsSession.putParameter(ModelConstants.DISTRIBUTION_LIST_SESSION_OBJECT, distributionListDetails);
			persistSession(sc, mlistsSession);

		} catch (AtWinXSException | CPRPCException | IllegalAccessException | InvocationTargetException e) {

			logger.error(e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage(errorMessage);
			return response;

		}

		return response;
	}

	// CAP-48434
	private String getDecryptedListID(AppSessionBean asb, String encryptedListID, UploadDistListResponse response)
			throws AtWinXSException {

		String listNotExistErrMsg = translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
				SFTranslationTextConstants.DLIST_NOT_EXIST);

		String listID = "";

		try {

			if(!Util.isBlankOrNull(encryptedListID)) {
				listID = Util.decryptString(encryptedListID);
				listID = listID.replace(DIST_LIST_ID_URL_PARAM, "");
			}
		} catch (Exception e) {

			response.setSuccess(false);
			response.setMessage(listNotExistErrMsg);
		}
		return listID;
	}

	private String addList(DistributionListDetails distributionListDetails, AppSessionBean appSessionBean)
			throws AtWinXSException {

		UploadListRequestBean anULReqBean = instantiateULReqBean(distributionListDetails, appSessionBean);

		if (distributionListDetails.getIsExcel()) {
			moveFileIfNotConvertingExcel(distributionListDetails, appSessionBean);
		} else {

			distributionListDetails
					.setModifiedFileName(Util.replace(distributionListDetails.getSourceFileName(), " ", "%20"));
		}

		anULReqBean.setModifiedFileName(distributionListDetails.getModifiedFileName()); // CP-10186

		UploadListAssembler anULAssembler = new UploadListAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		UploadListResponseBean anULResBean = anULAssembler.performAddList(anULReqBean,
				ManageListsConstants.UPLOAD_LIST_CONFIRMATION_EVENT);

		return anULResBean.getListID();
	}


	// CAP-48277 - CP Method from ListsServiceImpl to map DistList
	public LMapperData mapDistList(SessionContainer sc, String selectedListID, boolean isDistListWithItemQty)
			throws CPRPCException, AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();
		ManageListsSession mlistsSession = (ManageListsSession) sc.getReferenceSessions().get(AtWinXSConstant.LISTS_SERVICE_ID);
		ManageListsAdminSessionBean mlistsAdminSession = (ManageListsAdminSessionBean) mlistsSession.getSessionBean()
				.getManageListsAdminSession();
		boolean isAllowListFeedPreview = mlistsAdminSession.isBUAllowListFeedPreview();

		int listID = Util.safeStringToInt(selectedListID);

		ListsBaseAssembler listBaseAsm = new ListsBaseAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		ListVO listVO = new ListVO(appSessionBean.getSiteID(), appSessionBean.getBuID(), listID);
		try {

			listVO = listBaseAsm.getList(listID, appSessionBean.getSiteID(), appSessionBean.getBuID());
		} catch (AtWinXSException e1) {// CAP-16460 call to logger.

			logger.error(this.getClass().getName() + " - " + e1.getMessage(), e1);
		}

		ManageListsResponseBean manageLists = (ManageListsResponseBean) mlistsSession.getParameter("manageLists");

		if (listVO != null) {
			manageLists = new ManageListsResponseBean();
			manageLists.setListID(selectedListID);
			manageLists.setListName(listVO.getListName());
			manageLists.setIsPrivate(listVO.isPrivate());
			manageLists.setListDescription(listVO.getListDescription());
			manageLists.setUploadedDate(listVO.getUploadedDate().toString());
			manageLists.setLastUsedDate(listVO.getUploadedDate().toString());
			manageLists.setRecordCount(String.valueOf(listVO.getRecordCount()));
			manageLists.setContainsHeadings(listVO.getContainsHeadings());
			manageLists.setLoginID(listVO.getLoginID());
			manageLists.setCustomerFileName(listVO.getCustomerFileName());
			manageLists.setSourceFileName(listVO.getSourceFileName());
			manageLists.setFileSize(String.valueOf(listVO.getFileSize()));
			manageLists.setIsListFeedInd(listVO.isListFeeedInd());
			manageLists.setEncrytedInd(listVO.isEncrytedInd());// CAP-29163
		}

		setDistributionListBean(manageLists, oeSessionBean, appSessionBean);

		OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
		DistributionListDetails distributionListDetails = objectMapFactoryService.getEntityObjectMap()
				.getEntity(DistributionListDetails.class, appSessionBean.getCustomToken());
		MapperData mapperdata = new MapperData();
		LMapperData lmapper = new ListMapperData();

		oeSessionBean.setSendToDistListUsingQtyFromFile(isDistListWithItemQty);

		try {

			if (distributionListDetails.validatefile(distListBean) || manageLists.isListFeedInd()) {

				mapperdata = getOrderwithDistListPreview(appSessionBean.getGlobalFileUploadPath(), oeSessionBean,
						appSessionBean, volatileSessionBean);
				distributionListDetails.populate(mapperdata);
			}
			int maxCols = mapperdata.getMaxColumns();

			setLMapperData(appSessionBean, lmapper, manageLists, distributionListDetails, maxCols, isAllowListFeedPreview);

			mlistsSession.putParameter("distributionListDetails", distributionListDetails);

		} catch (AtWinXSException e) {

			throw Util.asCPRPCException(e);
		}

		persistSession(sc, mlistsSession);

		return lmapper;

	}

	// CAP-48277 - refactor mapDistList() for sonar to reduce Complexity
	public void setLMapperData(AppSessionBean appSessionBean, LMapperData lmapper,
			ManageListsResponseBean manageLists, DistributionListDetails distributionListDetails, int maxCols,
			boolean isAllowListFeedPreview) throws   AtWinXSException {

		Collection<ColumnNameWrapperCell> heading = new ArrayList<>();
		Collection<AltColumnNameWrapperCell> altColumnNames = new ArrayList<>();
		Collection<ArrayList<String>> dataVector = new ArrayList<>();


		if (null == distributionListDetails.getHeadings()
				|| distributionListDetails.getHeadings().size() < maxCols) {

			Collection<ColumnNameWrapper> headings = distributionListDetails.getHeadings();
			int size = (null == headings) ? maxCols : (maxCols - headings.size());
			for (int colIdx = 1; colIdx <= size; colIdx++) {
				ColumnNameWrapperCell column = new ColumnNameWrapperCellData();
				column.setColumnName("Col " + colIdx);
				heading.add(column);
			}
		} else {

			for (ColumnNameWrapper header : distributionListDetails.getHeadings()) {
				ColumnNameWrapperCell column = new ColumnNameWrapperCellData();
				column.setColumnName(header.getColumnName());
				column.setRequired(header.isRequired());
				heading.add(column);
			}
		}

		for (String[] dataArray : distributionListDetails.getData()) {

			dataVector.add(new ArrayList<>(Arrays.asList(dataArray)));
		}

		OEMappedVariableResponseBean[] variables = (OEMappedVariableResponseBean[]) distributionListDetails
				.getnewColNames().toArray(new OEMappedVariableResponseBean[] {});

		if (distributionListDetails.getnewColNames() != null) {

			for (int j = 0; j < variables.length; j++) {
				AltColumnNameWrapperCell temp = new AltColumnNameWrapperCellData();
				OEMappedVariableResponseBean variableBean = variables[j];
				temp.setTextVariableName(variableBean.getTextVariableName());
				temp.setDisplayLabel(variableBean.getDisplayLabel());
				temp.setRequired(variableBean.isRequired());
				altColumnNames.add(temp);
			}
		}

		lmapper.setAltColumnNames(altColumnNames);
		lmapper.setDataVector(dataVector);
		lmapper.setHeadings(heading);
		lmapper.setRowCount(distributionListDetails.getMaxRows());
		lmapper.setDistListSuggestedMessages((HashMap<String, String>) getDistListSuggestedMessages(appSessionBean));
		lmapper.setDisplayData(manageLists.isListFeedInd() && !isAllowListFeedPreview);
	}

	// CP mapOFFList() not used for CAP-48277 scope, will be used in future
	// CAP-48277 - CP Method from ListsServiceImpl to map OFFList (Order From File)
	public LMapperData mapOFFList(SessionContainer sc, String selectedListID) throws CPRPCException, AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
		OEOrderSessionBean oeSessionBean = ((OrderEntrySession) sc.getModuleSession()).getOESessionBean();

		int listID = Util.safeStringToDefaultInt(selectedListID, -1);
		OECustomDocOrderLineMapSessionBean[] mapBeans = null;
		OEOrderFromFileBean offBean = oeSessionBean.getOrderFromFileBean();

		if (offBean == null || !offBean.getListID().equals(listID + "")) {

			offBean = initOFFBean(appSessionBean, listID, mapBeans);
			offBean.setListID(listID + "");
			oeSessionBean.setOrderFromFileBean(offBean);
		} else {

			mapBeans = offBean.getMapBeans();
		}

		LMapperData offMapper = new ListMapperData();
		MapperData mapperdata = null;
		try {

			mapperdata = getOrderFromaFilePreview(appSessionBean.getGlobalFileUploadPath(), oeSessionBean,
					appSessionBean, volatileSessionBean);
		} catch (AtWinXSException ae) {

			StringBuilder html = new StringBuilder();
			html.append(
					"The following selected list could not be found on the server. Please select another list or contact the system administrator.");
			html.append("<br>List named: ").append(offBean.getListName()).append(" with ID #").append(listID)
					.append(" could not be found for site ID #").append(appSessionBean.getSiteID())
					.append(" and business unit ID #").append(appSessionBean.getBuID()).append(".");

			Message msg = new Message();
			msg.setErrGeneralMsg(html.toString());
			throw Util.asCPRPCException(new AtWinXSMsgException(msg, this.getClass().getName()));
		}

		oeSessionBean.setOrderFromFileBean(offBean);
		int maxCols = mapperdata.getMaxColumns();

		setOFFMapper(appSessionBean, offMapper, mapperdata, maxCols, mapBeans);

		// Save the session to persist the OEOrderFromFileBean
		SessionHandler.saveSession(sc.getModuleSession(), sc.getApplicationSession().getAppSessionBean().getSessionID(),
				AtWinXSConstant.ORDERS_SERVICE_ID);

		return offMapper;
	}

	// CP mapOFFList() not used for CAP-48277 scope, will be used for future
	// CAP-48277 - refactor mapOFFList() for sonar to reduce Complexity
	public void setOFFMapper(AppSessionBean appSessionBean, LMapperData offMapper, MapperData mapperdata,
			int maxCols, OECustomDocOrderLineMapSessionBean[] mapBeans) throws AtWinXSException {

		Collection<ColumnNameWrapperCell> heading = new ArrayList<>();
		Collection<AltColumnNameWrapperCell> altColumnNames = new ArrayList<>();
		Collection<ArrayList<String>> dataVector = new ArrayList<>();

		if (null == mapperdata.getHeadings() || mapperdata.getHeadings().size() < maxCols) {

			Collection<ColumnNameWrapper> headings = mapperdata.getHeadings();
			int size = (null == headings) ? maxCols : (maxCols - headings.size());
			for (int colIdx = 1; colIdx <= size; colIdx++) {

				ColumnNameWrapperCell column = new ColumnNameWrapperCellData();
				column.setColumnName("Col " + colIdx);
				heading.add(column);
			}
		} else {

			for (ColumnNameWrapper header : mapperdata.getHeadings()) {

				ColumnNameWrapperCell column = new ColumnNameWrapperCellData();
				column.setColumnName(header.getColumnName());
				column.setRequired(header.isRequired());
				heading.add(column);
			}
		}

		for (String[] dataArray : mapperdata.getDataVector()) {

			dataVector.add(new ArrayList<>(Arrays.asList(dataArray)));
		}

		OEMappedVariableResponseBean[] variables = null;
		variables = mapperdata.getAltColumnNames()
				.toArray(new OEMappedVariableResponseBean[] {});
		Collection<String> requiredVars = new ArrayList<>();
		if (variables != null) {

			for (int j = 0; j < variables.length; j++) {

				AltColumnNameWrapperCell temp = new AltColumnNameWrapperCellData();
				OEMappedVariableResponseBean variableBean = variables[j];
				temp.setTextVariableName(variableBean.getTextVariableName());
				temp.setDisplayLabel(variableBean.getDisplayLabel());
				temp.setRequired(variableBean.isRequired());
				if (temp.isRequired()) {
					requiredVars.add(temp.getTextVariableName());
				}
				altColumnNames.add(temp);
			}
		}

		if (mapBeans != null && mapBeans.length > 0) {

			HashMap<String, String> mappedBeans = new HashMap<>();
			String label = "";
			for (OECustomDocOrderLineMapSessionBean mapBean : mapBeans) {
				if (mapBean.getPageFlxVariableName() != null) {
					label = mapBean.getPageFlxVariableLabel();
					if (requiredVars.contains(mapBean.getPageFlxVariableName())) {

						label = (new StringBuilder()).append(label+"*").toString();
					}
					mappedBeans.put(mapBean.getMapColName(), label);
				}
			}
			offMapper.setMappedBeans(mappedBeans);
		}

		offMapper.setAltColumnNames(altColumnNames);
		offMapper.setDataVector(dataVector);
		offMapper.setHeadings(heading);
		offMapper.setRowCount(mapperdata.getRowcount());
		offMapper.setDisplayData(true);
		offMapper.setDistListSuggestedMessages((HashMap<String, String>) getDistListSuggestedMessages(appSessionBean));
	}

	// CP mapOFFList() not used for CAP-48277 scope, will be used in future
	// CAP-48277 - CP Method from ListsServiceImpl to Initialize Order From File
	public OEOrderFromFileBean initOFFBean(AppSessionBean asb, int listID,
			OECustomDocOrderLineMapSessionBean[] mapBeans)
			throws AtWinXSException {
		IManageList manageList = ManageListsLocator.locate(asb.getCustomToken());
		ListVO vo = manageList.retrieveAList(new ListVOKey(asb.getSiteID(), asb.getBuID(), listID));

		OEOrderFromFileBean offBean = new OEOrderFromFileBean (
				listID +"",
				vo.getListName(),
				asb.getGlobalFileUploadPath(),
				Util.boolToY(vo.isPrivate()),
				vo.getListDescription(),
				vo.getLastModifiedDate().toString(),
				vo.getLastUsedDate().toString(),
				"",
				Util.boolToY(vo.isContainsHeadings()),
				vo.getLoginID(),
				vo.getCustomerFileName(),
				vo.getSourceFileName(),
				vo.getFileSize()+"");

		offBean.setMapBeans(mapBeans);
		offBean.setAddressCount(String.valueOf(vo.getRecordCount())); // CAP-1595

		return offBean;
	}

	// CAP-48277 - CP Method from ListsServiceImpl to get the translated text for Distribution List suggested messages
	public Map<String, String> getDistListSuggestedMessages(AppSessionBean appSessionBean) throws AtWinXSException {

		String distListOrderSgnMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dist_list_order_sgn_msg");
		String dlErrorDesc1Msg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_error_desc1_msg");
		String dlErrorDesc2Msg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_error_desc2_msg");
		String dlErrorInsstructionMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_error_instruction_msg");
		String dlSuggestionHeaderMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_suggestion_header_msg");
		String dlItemNumTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_item_num_title_msg");
		String dlItemNumDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_item_num_desc_msg");
		String dlItemNumDescMsg2 = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "sug_for_offf_and_iandq");
		String dlItemNumDescMsg3 = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "sug_for_off_and_iandq_txt");
		String dlUnitMsrTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_unit_msr_title_msg");
		String dlUnitMsrDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_unit_msr_desc_msg");
		String dlQtyTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_qty_title_msg");
		String dlQtyDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_qty_desc_msg");
		String dlQtyDescMsg2 = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_qty_desc_msg2");
		String dlQtyDescMsg3 = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_qty_desc_msg3");
		String dlKitTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_kit_title_msg");
		String dlKitDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_kit_desc_msg");
		String dlItemOrderedTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_item_ordered_title_msg");
		String dlItemOrderedDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_item_ordered_desc_msg");
		String dlItemMappingTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_item_mapping_title_msg");
		String dlItemMappingDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_item_mapping_desc_msg");
		String dlCustRefTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_cust_ref_title_msg");
		String dlCustRefDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_cust_ref_desc_msg");
		String dlEmailAddTitleMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_email_add_title_msg");
		String dlEmailAddDescMsg = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "dl_email_add_desc_msg");
		String dlClose = translationService.processMessage(appSessionBean.getDefaultLocale(),
				appSessionBean.getCustomToken(), "TRANS_NM_CLOSE"); // CP-12732

		HashMap<String, String> suggestedMessages = new HashMap<>();
		suggestedMessages.put("dist_list_order_sgn_msg", distListOrderSgnMsg);
		suggestedMessages.put("dl_error_desc1_msg", dlErrorDesc1Msg);
		suggestedMessages.put("dl_error_desc2_msg", dlErrorDesc2Msg);
		suggestedMessages.put("dl_error_instruction_msg", dlErrorInsstructionMsg);
		suggestedMessages.put("dl_suggestion_header_msg", dlSuggestionHeaderMsg);
		suggestedMessages.put("dl_item_num_title_msg", dlItemNumTitleMsg);
		suggestedMessages.put("dl_item_num_desc_msg", dlItemNumDescMsg);

		suggestedMessages.put("sug_for_offf_and_iandq", dlItemNumDescMsg2);
		suggestedMessages.put("sug_for_off_and_iandq_txt", dlItemNumDescMsg3);
		suggestedMessages.put("dl_unit_msr_title_msg", dlUnitMsrTitleMsg);
		suggestedMessages.put("dl_unit_msr_desc_msg", dlUnitMsrDescMsg);
		suggestedMessages.put("dl_qty_title_msg", dlQtyTitleMsg);
		suggestedMessages.put("dl_qty_desc_msg", dlQtyDescMsg);

		suggestedMessages.put("dl_qty_desc_msg2", dlQtyDescMsg2);
		suggestedMessages.put("dl_qty_desc_msg3", dlQtyDescMsg3);
		suggestedMessages.put("dl_kit_title_msg", dlKitTitleMsg);
		suggestedMessages.put("dl_kit_desc_msg", dlKitDescMsg);
		suggestedMessages.put("dl_item_ordered_title_msg", dlItemOrderedTitleMsg);
		suggestedMessages.put("dl_item_ordered_desc_msg", dlItemOrderedDescMsg);
		suggestedMessages.put("dl_item_mapping_title_msg", dlItemMappingTitleMsg);
		suggestedMessages.put("dl_item_mapping_desc_msg", dlItemMappingDescMsg);
		suggestedMessages.put("dl_cust_ref_title_msg", dlCustRefTitleMsg);
		suggestedMessages.put("dl_cust_ref_desc_msg", dlCustRefDescMsg);

		suggestedMessages.put("dl_email_add_title_msg", dlEmailAddTitleMsg);
		suggestedMessages.put("dl_email_add_desc_msg", dlEmailAddDescMsg);
		suggestedMessages.put("dl_close", dlClose);

		return suggestedMessages;
	}

	// CAP-48277 - CP Method from ListsUtil builds preview information for order from a file mapping.
	public MapperData getOrderwithDistListPreview(String globalFileUploadPath, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
			throws AtWinXSException {
		OEDistributionListBean distributionListBean = oeSessionBean.getDistributionListBean();

		if (distributionListBean == null) {

			throw new AtWinXSWrpException(new NullPointerException("Order with DistributionList information is null"),
					CODeliveryInformationServiceImpl.class.getName());
		}

		if (Util.isBlank(Util.nullToEmpty(distributionListBean.getListID()))) {

			throw new AtWinXSWrpException(new NullPointerException("List ID is null"),
					CODeliveryInformationServiceImpl.class.getName());
		}

		String fileName = Util.nullToEmpty(distributionListBean.getListSysFileName());

		if (Util.isBlank(fileName)) {

			throw new AtWinXSWrpException(new NullPointerException("List file name is null"),
					CODeliveryInformationServiceImpl.class.getName());
		}

		boolean hasHeadings = Boolean.parseBoolean(distributionListBean.getHasListHeadings());

		MapperInput input = null;

		if (distributionListBean.getIsEncrytedInd().equalsIgnoreCase("true")) {

			input = processEncryptedFile(fileName, hasHeadings, appSessionBean);
		} else {

			input = MapperInputFactory.getMapperInputInstance(globalFileUploadPath, fileName, hasHeadings,
					MapperConstants.DEFAULT_DELIMITER, "");
		}

		try {

			Mapper mapper = new Mapper();
			MapperData mapperdata = mapper.getDataPreview(input);
			Vector<OEMappedVariableResponseBean> columnNames = null;

			if (oeSessionBean.isSendToDistListUsingQtyFromFile()) {

				boolean isOrderFromFile = false;
				int orderID = OrderEntryConstants.INVALID_ORDER_ID;
				if (volatileSessionBean.getOrderId() != null) {

					orderID = volatileSessionBean.getOrderId().intValue();
				}
				columnNames = getOrderFromFileColumnNames(mapperdata, oeSessionBean, appSessionBean,
						volatileSessionBean, orderID, isOrderFromFile);
			} else {

				columnNames = getOrderwithDistListColumnNames(mapperdata, oeSessionBean, appSessionBean,
						volatileSessionBean);
			}

			mapperdata.setAltColumnNames(columnNames);
			return mapperdata;

		} catch (AtWinXSException e) {

			String listName = "";

			OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
			OEOrderFromFileBean orderFromFile = oeSessionBean.getOrderFromFileBean();

			if (distListBean != null) {

				listName = distListBean.getListName();
			} else if (orderFromFile != null) {

				listName = orderFromFile.getListName();
			}

			throw OrderEntryUtil.wrapCustomDateThrownException(e, listName, "DeliveryOptionsServiceImpl");
		}
	}

	// CAP-48277 - CP Method from ListsUtil to get Mapper Input Instance
	public  MapperInput processEncryptedFile(String fileName, boolean hasHeadings, AppSessionBean appSessionBean)
			throws AtWinXSException {

		return MapperInputFactory.getMapperInputInstance(
				Util.getGlobalWorkingFileUploadPath(appSessionBean.getSiteLoginID()), fileName, hasHeadings,
				MapperConstants.DEFAULT_DELIMITER, "");
	}

	// CP mapOFFList() not used for CAP-48277 scope, will be used for future
	// CAP-48277 - CP Method from ListsUtil builds preview information for order from a file mapping.
	public MapperData getOrderFromaFilePreview(String globalFileUploadPath, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException {
		OEOrderFromFileBean offBean = oeSessionBean.getOrderFromFileBean();

		if (offBean == null) {

			throw new AtWinXSWrpException(new NullPointerException("Order From a File information is null"),
					CODeliveryInformationServiceImpl.class.getName());
		}

		if (Util.isBlank(Util.nullToEmpty(offBean.getListID()))) {

			throw new AtWinXSWrpException(new NullPointerException("List ID is null"),
					CODeliveryInformationServiceImpl.class.getName());
		}
		String fileName = Util.nullToEmpty(offBean.getListSysFileName());
		if (Util.isBlank(fileName)) {

			throw new AtWinXSWrpException(new NullPointerException("List file name is null"),
					CODeliveryInformationServiceImpl.class.getName());
		}

		boolean hasHeadings = Util.yToBool(offBean.getHasListHeadings());

		MapperInput input = MapperInputFactory.getMapperInputInstance(globalFileUploadPath, fileName, hasHeadings,
				MapperConstants.DEFAULT_DELIMITER, "");

		Mapper mapper = new Mapper();
		MapperData mapperdata = mapper.getDataPreview(input);
		Vector<OEMappedVariableResponseBean> columnNames = null;

		boolean isOrderFromFile = true;
		int orderID = OrderEntryConstants.INVALID_ORDER_ID;

		if (volatileSessionBean.getOrderId() != null) {

			orderID = volatileSessionBean.getOrderId().intValue();
		}

		columnNames = getOrderFromFileColumnNames(mapperdata, oeSessionBean, appSessionBean, volatileSessionBean,
				orderID, isOrderFromFile);

		mapperdata.setAltColumnNames(columnNames);

		return mapperdata;
	}

	// CAP-48277 - CP Method from ListsUtil sets the default column names for order with Distribution List mapping.
	public Vector<OEMappedVariableResponseBean> getOrderwithDistListColumnNames(MapperData mapperdata,
			OEOrderSessionBean oeSession, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
			throws AtWinXSException {

		Vector<OEMappedVariableResponseBean> cols = new Vector<>();
		OEMappedVariableResponseBean responseBean = null;

		if (mapperdata != null && mapperdata.getMaxColumns() > 0) {

			responseBean = new OEMappedVariableResponseBean("NM_1", "Name 1", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("NM_2", "Name 2", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AD_1", "Address 1", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AD_2", "Address 2", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AD_3", "Address 3", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("CITY_NM", "City", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("STATE_NM", "State", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("ZIP_CD", "ZIP", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("COUNTRY_CD", "Country Code", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AREA_CD", "Area Code", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("PHONE_NR", "Phone", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("EMAIL_NM", "E-mail", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("SHIP_ATN_NM", "Ship To Attention", false);
			cols.add(responseBean);

			OEListAssembler assembler = getAssembler(volatileSessionBean, appSessionBean);
			String soldToNumber = OrderEntryConstants.DEFAULT_SOLD_TO_NUM;

			// CAP-48277 - Cast cols (ArrayList) to Vector as CP method used Vector Argument
			assembler.getCustomerReferenceFieldMappings(cols, oeSession, appSessionBean.getCorporateNumber(),
					soldToNumber);

			// CAP-48277 - Cast cols (ArrayList) to Vector as CP method used Vector Argument
			assembler.getPONumberMapping(cols, oeSession, appSessionBean.getCorporateNumber(), soldToNumber);
		}
		return cols;
	}

	// CAP-48277 - CP Method from ListsUtil sets the default column names for order from a file mapping.
	protected Vector<OEMappedVariableResponseBean> getOrderFromFileColumnNames(MapperData mapperdata,
			OEOrderSessionBean oeSession, AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			int orderID, boolean isOrderFromFile) throws AtWinXSException {

		Vector<OEMappedVariableResponseBean> cols = new Vector<>();
		OEMappedVariableResponseBean responseBean = null;

		if (mapperdata != null && mapperdata.getMaxColumns() > 0) {

			int itemCount = (mapperdata.getMaxColumns() - 7) / 2;

			responseBean = new OEMappedVariableResponseBean("NM_1", "Name 1", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("NM_2", "Name 2", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AD_1", "Address 1", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AD_2", "Address 2", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AD_3", "Address 3", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("CITY_NM", "City", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("STATE_NM", "State", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("ZIP_CD", "ZIP", true);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("COUNTRY_CD", "Country Code", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("AREA_CD", "Area Code", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("PHONE_NR", "Phone", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("EMAIL_NM", "E-mail", false);
			cols.add(responseBean);
			responseBean = new OEMappedVariableResponseBean("SHIP_ATN_NM", "Ship To Attention", false);
			cols.add(responseBean);

			OEListAssembler assembler = getAssembler(volatileSessionBean, appSessionBean);
			try {

				assembler.getItemColumnMappings(cols, itemCount, oeSession.isSendToDistListUsingQtyFromFile(),
						isOrderFromFile, orderID, oeSession);
			} catch (AtWinXSException e) {

				throw new AtWinXSException("There are errors encountered while getting the cart line item mappings.",
						"OEListBroker");
			}

			// CP-2865 - DLO - Get customer reference fields to map, passed corp num, sold to, num param
			String soldToNumber = OrderEntryConstants.DEFAULT_SOLD_TO_NUM;

			// CAP-48277 - Cast cols (ArrayList) to Vector as CP method used Vector Argument
			assembler.getCustomerReferenceFieldMappings(cols, oeSession,
					appSessionBean.getCorporateNumber(), soldToNumber);

			// CAP-48277 - Cast cols(ArrayList) to Vector as CP method used Vector Argument
			// CP-2865 - DLO - Get the po number to map, passed corp num, sold to num param
			assembler.getPONumberMapping(cols, oeSession, appSessionBean.getCorporateNumber(),
					soldToNumber);
		}

		return cols;
	}

	// CAP-48277 - CP Method from ListsUtil will get the multipart params from the request
	public OEListAssembler getAssembler(VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean) {

		return new OEListAssembler(volatileSessionBean, appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
	}

	// CAP-48365 - START
	protected void saveAddress(AppSessionBean appSessionBean, DeliveryOptionsFormBean deliveryOptionsBean, OEOrderSessionBean oeSession,
			OECheckoutAssembler checkoutAssembler) throws AtWinXSException {
		OENewAddressFormBean newAddressBean = getAddressFromCommandObject(appSessionBean, deliveryOptionsBean, oeSession , checkoutAssembler);
		checkoutAssembler.setNewAddress(oeSession, appSessionBean.getCorporateNumber(), newAddressBean, true, true);
	}

	private OENewAddressFormBean getAddressFromCommandObject(AppSessionBean appSessionBean, DeliveryOptionsFormBean deliveryOptionsBean,
			OEOrderSessionBean oeSession, OECheckoutAssembler checkoutAssembler) throws AtWinXSException
	{
		return checkoutAssembler.getAddressFromCommandObject(deliveryOptionsBean, oeSession, null, appSessionBean);
	}

	private Message validateList(OEOrderSessionBean oeSession, DeliveryOptionsFormBean deliveryOptionsBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean) throws AtWinXSException
	{
		Message msg = null;

		//CAP-676 MGP - Added Distribution List with Addresses (DLA)
		if (deliveryOptionsBean.getAddressSource().equals(DIST_LIST_ITEMS) || deliveryOptionsBean.getAddressSource().equals(DIST_LIST_ADDRESS))
		{
			msg = COListsUtil.buildAndValidateList(appSessionBean, volatileSessionBean, oeSession, deliveryOptionsBean);
		}

		return msg;
	}

	private void updateDistributionListDetails(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeSession, String listID, String addressCount) throws AtWinXSException {
		OEListAssembler listAssembler = new OEListAssembler(volatileSessionBean, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
		OEDistributionListDetailsFormBean listDetailsBean = new OEDistributionListDetailsFormBean();
		// build listDetailsBean
		listDetailsBean.setListID(listID);
		listDetailsBean.setAddressCount(addressCount);
		listDetailsBean.setCrmFlag(ModelConstants.OPTION_NO);
		listDetailsBean.setListRejectActionText(AtWinXSConstant.EMPTY_STRING);
		listDetailsBean.setBadAddressLimit("0");
		//CAP-49757
		if(Util.safeStringToDefaultInt(listID, -1) > 0)
		{
			listDetailsBean.setDistributionListTypeCode(OrderEntryConstants.DIST_LIST_ORD_TYPE_CD_CART);//CAP-48846
		}
		listAssembler.saveDistributionListDetails(listDetailsBean, appSessionBean, oeSession, true);
	}

	private void updateListByLastUseDate(AppSessionBean appSessionBean, DeliveryOptionsFormBean deliveryOptionsBean) throws NumberFormatException, AtWinXSException {
		IManageList listmanager = manageListsLocatorService.locate(appSessionBean.getCustomToken());
		listmanager.updateLastUseDate(new ListVO(appSessionBean.getSiteID(), appSessionBean.getBuID(), Integer.parseInt(deliveryOptionsBean.getSelectedListID())));
	}

	private void updateOrderScenarioNumber(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			String addressSource, OEOrderSessionBean oeSessionBean) throws AtWinXSException
	{
		boolean orderSentToDistributionList = true;
		boolean recalculateOrderScenarioNumber = false;


		// Clear out dist list bean if not dist list address source
		if (oeSessionBean != null &&
				(!(DeliveryOptionsConstants.ADDR_SRC_DIST_LIST_ADDRONLY_ADDR_CD.equals(addressSource) ||
				DeliveryOptionsConstants.ADDR_SRC_DIST_LIST_ITEMQTY_ADDR_CD.equals(addressSource) ||
				DeliveryOptionsConstants.ADDR_SRC_SF_CAMPAIGN.equals(addressSource) ||
				(oeSessionBean.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_MAIL_MERGE_ONLY))))
		{
			oeSessionBean.setDistributionListBean(null);
			orderSentToDistributionList = false;
			//RAR - If previous Order Scenario Number is a Dist List, then we need to recalculate Order Scenario Number.
			if (oeSessionBean.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_KIT_AND_DIST_LIST
					|| oeSessionBean.getOrderScenarioNumber() == OrderEntryConstants.SCENARIO_NO_KIT_AND_DIST_LIST) {
				recalculateOrderScenarioNumber = true;
			}
		}

		else
		{
			recalculateOrderScenarioNumber = true;
		}

		if(oeSessionBean != null && recalculateOrderScenarioNumber)
		{
			IOEShoppingCartComponent oeShoppingCartComp = oeShoppingCartComponentLocatorService.locate(appSessionBean.getCustomToken());
			OrderHeaderVO orderHeaderVO = new OrderHeaderVO(volatileSessionBean.getOrderId());
			int scenarioNumber = oeShoppingCartComp.getOrderScenarioNumber(orderHeaderVO, orderSentToDistributionList, oeSessionBean.isLineItemShipping(), null);
			oeSessionBean.setOrderScenarioNumber(scenarioNumber);

			//RAR - Persist also the Order Scenario Number in the Database, so when we go back to the cart we know what's the Order Scenario Number.
			oeShoppingCartComp.saveOrderScenarioNumber(orderHeaderVO.getKey(), scenarioNumber);
		}
	}
	// CAP-48365 - END

	//CAP-48202

		@Override
		public SaveMappingResponse saveListMappings(SessionContainer sc, SaveMappingRequest request, boolean isDistListWithItemQty, boolean isOrderFromaFile, String listID) throws AtWinXSException, CPRPCException
		{
			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			VolatileSessionBean volatileSessionBean = sc.getApplicationVolatileSession().getVolatileSessionBean();
			OEOrderSessionBean oeSessionBean = ((OrderEntrySession)sc.getModuleSession()).getOESessionBean();

			//load listsSession
			sc.addReferenceSession(AtWinXSConstant.LISTS_SERVICE_ID, loadListSession(sc));

			ManageListsSession mlistsSession = (ManageListsSession)sc.getReferenceSessions().get(AtWinXSConstant.LISTS_SERVICE_ID);

			SaveMappingResponse response=new SaveMappingResponse();

			DistributionListDetails distributionListDetails = (DistributionListDetails)mlistsSession.getParameter("distributionListDetails");

			HashMap<String, String> returnVal = new HashMap<>();

			ArrayList<String> errors = new ArrayList<>();
			//Check for exception conditions
			OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
			OEOrderFromFileBean offBean = oeSessionBean.getOrderFromFileBean();
			oeSessionBean.setSendToDistListUsingQtyFromFile(isDistListWithItemQty);

			if (!isOrderFromaFile && distListBean == null)
			{
				errors.add("Order with Distribution List information is null");
			}

			int maxCols = request.getLmapper().getAltColumnNames().size();

			// get mappings
			OECustomDocOrderLineMapSessionBean[] mapBeans = null;
			ArrayList<OECustomDocOrderLineMapSessionBean> array = new ArrayList<>();
			String headingName = "";
			String mapName = "";
			String variableName = "";

			Iterator<COColumnNameWrapperCellData> headingIter = request.getLmapper().getHeadings().iterator();
			Iterator<COAltColumnNameWrapperCellData> altColIter = request.getLmapper().getAltColumnNames().iterator();

			//CAP-48497 -mandatory and Duplication check
			List<OEMappedVariableResponseBean> mandateList=getMandateOrderwithDistListColumnNames();

			response.setSuccess(true);
			response=validateMandateField(mandateList,request,appSessionBean,response);
			response=validateDuplicateField(request, appSessionBean, response);

			if(response.isSuccess()) {
				for(int i = 0; i < maxCols; i++)
				{
					headingName = headingIter.next().getColumnName();
					COAltColumnNameWrapperCellData altCol = altColIter.next();
					mapName = altCol.getDisplayLabel();
					variableName = altCol.getTextVariableName();

					//if no column headings in the file, set heading name to column seq. number
					if (Util.isBlankOrNull(headingName))
					{
						headingName = Integer.toString(i+1);
					}

					//For cust ref and PO, mapped column name will be the same as the variable name
					OECustomDocOrderLineMapSessionBean bean = new OECustomDocOrderLineMapSessionBean();

					if(!Util.isBlank(mapName))//CAP-19456
					{
						bean.setMapColName(headingName);
						bean.setPageFlxVariableLabel(mapName);
						bean.setPageFlxVariableName(variableName);
					}
					else
					{
						bean.setMapColName(headingName);
						bean.setPageFlxVariableLabel(OrderEntryConstants.NOT_MAPPED_COL);
					}

					array.add(bean);
				}

				// convert array list to array
				mapBeans = new OECustomDocOrderLineMapSessionBean[array.size()];
				mapBeans = array.toArray(mapBeans);

				// cache the updated mapbeans
				if (isOrderFromaFile)
				{
					offBean.setMapBeans(mapBeans);
					oeSessionBean.setOrderScenarioNumber(OrderEntryConstants.SCENARIO_ORDER_FROM_A_FILE); //CP-12769
				} else {
					if(distListBean!=null) {
						distListBean.setMapBeans(mapBeans);
						if(listID == null || listID.isEmpty() || Integer.valueOf(listID) <= 0 )
						{
							distListBean.setListID(distributionListDetails.getListID());
						}
						else
						{
							distListBean.setListID(listID);
						}
					}
					//CAP-4335 - Retrieve correct scenario number and store it in session.
					try
					{
						OrderHeaderVOKey orderKey = new OrderHeaderVOKey(volatileSessionBean.getOrderId());
						IOEShoppingCartComponent oeShoppingCartComp = oeShoppingCartComponentLocatorService.locate(appSessionBean.getCustomToken());
						OrderHeaderVO orderHeaderVO = new OrderHeaderDAO().retrieve(orderKey);
						oeSessionBean.setOrderScenarioNumber(oeShoppingCartComp.getOrderScenarioNumber(orderHeaderVO, !isOrderFromaFile, false, null)); //CAP-29105
					}
					catch(Exception e)
					{
						//Handled exception. Allow code to continue.
					}
				}

				OEListAssembler assembler = getAssembler(volatileSessionBean, appSessionBean);

				boolean hasPassedValidation = false;
				if (returnVal.get("msg")==null) //CP-8977
				{
					try
					{
						hasPassedValidation = assembler.doValidateMappingsFromFile(errors, mapBeans, oeSessionBean, appSessionBean.getGlobalFileUploadPath(), appSessionBean.getCorporateNumber());
					}
					catch (AtWinXSException e)
					{
						Util.asCPRPCException(e);
					}
				}

				if(hasPassedValidation)
				{
					assembler.setMappedCustRefFieldsToSession(oeSessionBean);
				}

				int columnMapped = 0;
				int columnNotMapped = 0;
				int rowAdded = 0;
				int rowNotAdded = 0;
				if((errors == null || errors.isEmpty()) && returnVal.get("msg")==null) //CP-8977
				{
					//RAR - Only save the mapping if there are no validation errors.
					try
					{
						//CP-11537 RAR - Pass true for isNewFile to save the mapping in XST413 table.
						if (isOrderFromaFile) //CP-13549
						{
							OrderFromaFileListDetails offListDetails = ObjectMapFactory.getEntityObjectMap().getEntity(OrderFromaFileListDetails.class, appSessionBean.getCustomToken());
							oeSessionBean.setOffMappingXML(COListsUtil.doMapcolumnNamesOFF(mapBeans, volatileSessionBean, appSessionBean.getLoginID(), offListDetails, appSessionBean));
						} else {
							COListsUtil.doMapcolumnNames(mapBeans, volatileSessionBean, appSessionBean.getLoginID(), distributionListDetails, appSessionBean, distListBean, true);
						}
					}
					catch (AtWinXSException e)
					{
						Util.asCPRPCException(e);
					}

					for (OECustomDocOrderLineMapSessionBean bean : mapBeans)
					{
						if (bean.getPageFlxVariableLabel().equalsIgnoreCase("(NOT MAPPED)") ||
							bean.getPageFlxVariableLabel().equalsIgnoreCase("Do not map")) //CP-12788
						{
							columnNotMapped++;
						}
						else
						{
							columnMapped++;
						}
					}

					//CAP-48898 use correct count from session.
					if (null != distributionListDetails)
					{
						rowAdded = distributionListDetails.getRecordCount();
					}

					returnVal.put("colMapped", String.valueOf(columnMapped));
					returnVal.put("colNotMapped", String.valueOf(columnNotMapped));
					returnVal.put("rowsAdded", String.valueOf(rowAdded));
					returnVal.put("rowsNotAdded", String.valueOf(rowNotAdded));
					response.setSuccess(true);
				}
				else
				{
					//CP-11525 changed "Errors" key to be "msg" to work with ListMappingTable.java
					//CP-11501 RAR - Display all the Errors that we got from validation.
					String finalMsg = "";

					if(null != returnVal.get("msg"))
					{
						finalMsg += htmlTxt + returnVal.get("msg") + "<br>";
					}
					else
					{
						String dlErrorGenMsg = "";

						try
						{
							dlErrorGenMsg = translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "dl_error_gen_msg");
						}
						catch (AtWinXSException e)
						{
							//DO NOTHING
						}

						finalMsg += dlErrorGenMsg + "<br><br>";
					}

					for(String err : errors)
					{
						finalMsg += htmlTxt + err + "<br>";
					}

					returnVal.put("msg", finalMsg);
					response.setSuccess(false);
				}

				if (isOrderFromaFile) //CP-12769
				{
					oeSessionBean.setOrderScenarioNumber(-1);
				}

				persistSession(sc,mlistsSession);

				response.setMap(returnVal);
			}
			return response;

		}

		//CAP-48497 -Get mandatory column
		public List<OEMappedVariableResponseBean> getMandateOrderwithDistListColumnNames() {
			ArrayList<OEMappedVariableResponseBean> cols = new ArrayList<>();
			OEMappedVariableResponseBean responseBean = null;
				responseBean = new OEMappedVariableResponseBean(ModelConstants.LMAP_LIST_NAME1,
						ModelConstants.LMAP_LIST_NAME1_VALUE, true);
				cols.add(responseBean);
				responseBean = new OEMappedVariableResponseBean(ModelConstants.LMAP_LIST_ADDR1,
						ModelConstants.LMAP_LIST_ADDR1_VALUE, true);
				cols.add(responseBean);
				responseBean = new OEMappedVariableResponseBean(ModelConstants.LMAP_LIST_CITYNM,
						ModelConstants.LMAP_LIST_CITYNM_VALUE, true);
				cols.add(responseBean);
				responseBean = new OEMappedVariableResponseBean(ModelConstants.LMAP_LIST_STATENM,
						ModelConstants.LMAP_LIST_STATENM_VALUE, true);
				cols.add(responseBean);
				responseBean = new OEMappedVariableResponseBean(ModelConstants.LMAP_LIST_ZIPCD,
						ModelConstants.LMAP_LIST_ZIPCD_VALUE, true);
				cols.add(responseBean);
				return cols;
		}

		// CAP-48497 -mandatory and Duplication check
		public SaveMappingResponse validateDuplicateField(SaveMappingRequest request, AppSessionBean asb,
				SaveMappingResponse response) throws AtWinXSException {
			int maxCols = request.getLmapper().getAltColumnNames().size();
			Iterator<COAltColumnNameWrapperCellData> altColIter = request.getLmapper().getAltColumnNames().iterator();
			Map<String, Integer> repeatationMap = new HashMap<>();
			for (int i = 0; i < maxCols; i++) {
				COAltColumnNameWrapperCellData altCol = altColIter.next();
			if(!altCol.getDisplayLabel().isBlank() && !altCol.getDisplayLabel().equalsIgnoreCase("Do not map"))	{
				if (repeatationMap.containsKey(altCol.getDisplayLabel())){
					repeatationMap.put(altCol.getDisplayLabel(), repeatationMap.get(altCol.getDisplayLabel()) + 1);
				} else {
					repeatationMap.put(altCol.getDisplayLabel(), 1);
				}
			 }
			}
			for (int repatCount : repeatationMap.values()) {
				if (repatCount > 1) {
					response.setSuccess(false);
					response.setMessage(translationService.processMessage(asb.getDefaultLocale(), asb.getCustomToken(),
							SFTranslationTextConstants.DUBLICATE_COL_MAPPED));
				}
			}
			return response;
		}

		public SaveMappingResponse validateMandateField(List<OEMappedVariableResponseBean> mandateList,
				SaveMappingRequest request, AppSessionBean asb, SaveMappingResponse response) throws AtWinXSException {
			for (OEMappedVariableResponseBean ls : mandateList) {
				int maxCols = request.getLmapper().getAltColumnNames().size();
				Iterator<COAltColumnNameWrapperCellData> altColIter = request.getLmapper().getAltColumnNames().iterator();
				boolean available=false;
				for (int i = 0; i < maxCols; i++) {
					COAltColumnNameWrapperCellData altCol = altColIter.next();
					if (ls.getDisplayLabel().equalsIgnoreCase(altCol.getDisplayLabel())) {
						available=true;
					}
				}
				if (!available) {
					response.setSuccess(false);
					response.getFieldMessages().put(ls.getDisplayLabel(),
							buildErrorMessage(SFTranslationTextConstants.REQUIRED_COL_MAPPING, asb, null)
									+ AtWinXSConstant.BLANK_SPACE + ls.getDisplayLabel());

				}
			}
			return response;
		}

		//CAP-48606
		public void checkDistListSharedOrPrivate(CODeliveryInformationResponse response, AppSessionBean appSessionBean) throws AtWinXSException {
			IManageListAdmin listAdmin = ListsAdminLocator.locate(appSessionBean.getCustomToken());
			ManageListsBusinessUnitPropsVO businessUnitPropsVO = listAdmin.getBuListDetails(
					new ManageListsBusinessUnitPropsVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID()));
			if (businessUnitPropsVO != null) {
				response.setDoNotShareListsInd(businessUnitPropsVO.isDoNotShareListsInd());
			}
		}

		//CAP-48744
		public void checkAndUpdateDistListInfo(CODeliveryInformationResponse response, OEOrderSessionBean oeOrderSessionBean ) {
			if(oeOrderSessionBean.getOrderScenarioNumber()==3) {
				response.getCheckoutSettings().setAddressSource("D");
				response.getDeliveryOptions().setAddressSource("D");
				response.getDeliveryOptions().setHasPreviousAddress(false);
			}
		}

		// CAP-48777
		public void validateListNameCharacters(SessionContainer sc, CreateListVarsRequest request,
				SaveManagedFieldsResponse response) throws AtWinXSException {
			if (!Util.isBlankOrNull(request.getListName())) {
				for (int currentChar : INVALID_CHARACTERS) {
					if (request.getListName().indexOf(currentChar) >= 0) {
						response.setFieldMessage(ModelConstants.LIST_NAME_LBL,
								translationService.processMessage(
										sc.getApplicationSession().getAppSessionBean().getDefaultLocale(),
										sc.getApplicationSession().getAppSessionBean().getCustomToken(),
										ModelConstants.TRANS_NM_LIST_UPLOAD_INVALID_CHARS));
						break;
					}
				}
			}
		}

		// CAP-46517, CAP-46518, CAP-42227 - add check for valid cust doc merge UI
		protected boolean validCustDocMergeUI(OEOrderSessionBean oeOrderSessionBean) {
			CustomDocumentItem item = oeOrderSessionBean.getCurrentCustomDocumentItem();
			UserInterface ui = oeOrderSessionBean.getCurrentCustomDocumentUserInterface();
			return ((item != null) && (ui != null) && (item.getUserInterface().equals(ui))
					&& ((MergeOption.MAIL_MERGE == ui.getMergeOption()) || (MergeOption.MERGE == ui.getMergeOption())));
		}

		protected boolean invalidAuthorizationForListProcess(SessionContainer sc) {
			OrderEntrySession oeSession = (OrderEntrySession) sc.getModuleSession();
			OEOrderSessionBean oeOrderSessionBean = oeSession.getOESessionBean();
			return ((!validateDistributionListEnabled(oeOrderSessionBean.getUserSettings())) && (!validCustDocMergeUI(oeOrderSessionBean)));
		}
}
