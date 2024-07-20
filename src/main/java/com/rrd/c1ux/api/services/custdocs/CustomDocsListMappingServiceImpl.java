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
 * 	05/15/24	R Ruth				CAP-42228		Initial version
 * 	05/16/24	A Boomker			CAP-42228		Additions for CP changes
 * 	05/30/24	R Ruth/A Boomker	CAP-42230		Add saving list mapping changes
 * 	05/31/24	A Boomker			CAP-42233		Add changes for navigation on next from list selection
 * 	06/04/24	A Boomker			CAP-42231		Adding get mapped data page
 */
package com.rrd.c1ux.api.services.custdocs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.common.GenericNameValuePair;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocListForMappingResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataPage;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocMappedDataResponse;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingBean;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingRequest;
import com.rrd.c1ux.api.models.custdocs.C1UXCustDocSaveListMappingResponse;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.session.SessionHandlerService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.orderentry.customdocs.UserInterface;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.UiListMapper;
import com.rrd.custompoint.orderentry.customdocs.ui.mergelist.UiMappedVariables;
import com.rrd.custompoint.orderentry.entity.CustomDocumentItem;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.mapper.ColumnNameWrapper;
import com.wallace.atwinxs.framework.util.mapper.MapperData;
import com.wallace.atwinxs.interfaces.IManageList;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.customdocs.util.CustomDocsUtil;
import com.wallace.atwinxs.orderentry.lists.vo.OrderListVO;
import com.wallace.atwinxs.orderentry.session.OECustomDocOrderLineMapSessionBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.session.OrderEntrySession;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;

@Service
public class CustomDocsListMappingServiceImpl extends CustomDocsBaseServiceImpl implements CustomDocsListMappingService
{
	private static final Logger logger = LoggerFactory.getLogger(CustomDocsListMappingServiceImpl.class);

	public CustomDocsListMappingServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFacService, SessionHandlerService sessionService) {
		super(translationService, objectMapFacService, sessionService);
	}

	@Override
	public C1UXCustDocListForMappingResponse  getListMappings(SessionContainer sc, int id) throws AtWinXSException {
		C1UXCustDocListForMappingResponse response = new C1UXCustDocListForMappingResponse();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

		if(!validateCustDocMergeUI(ui)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {
			// get the list
			IManageList listComp = getManageListComponent(asb);
			ListVO list = listComp.retrieveSingleList(asb.getSiteID(), asb.getBuID(), id);
			if ((list == null) || (!validateListVisibility(list, asb))) {
				// if the list is not valid or they cannot see it, they don't get to know the difference
				throw new AccessForbiddenException(this.getClass().getName());
			}

			UiListMapper uiListMapper = getListMapper(asb, ui, false, id);
			populateResponseFromMapper(uiListMapper, response, ui, id);
			response.setSuccess(true);
		} catch (AccessForbiddenException e) {
				throw e;
		} catch (AtWinXSMsgException e) {
			logger.error(e.toString());
			response.setMessage(e.getMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			response.setMessage(getTranslation(asb, SFTranslationTextConstants.DLIST_NOT_EXIST, SFTranslationTextConstants.DLIST_NOT_EXIST_DEFAULT_ERR));
		}

		return response;
	}

	protected void populateResponseFromMapper(UiListMapper uiListMapper, C1UXCustDocListForMappingResponse response, UserInterface ui, int id) throws AtWinXSException {
		MapperData mapperData = uiListMapper.getListMapData(id, true);
		response.setSampleData(loadSampleData(mapperData));
		response.setListHeaders(loadHeaders(mapperData));
		response.setFileColumns(mapperData.getMaxColumns());
		response.setMergeVars(loadMergeVars(uiListMapper));
		response.setExistingMapping(loadMappingFromUI(ui, id, response.getListHeaders()));
	}

	protected List<GenericNameValuePair> loadMergeVars(UiListMapper uiListMapper) throws AtWinXSException {
		ArrayList<GenericNameValuePair> vars = new ArrayList<>();
		ArrayList<UiMappedVariables> uiMappedVariables = uiListMapper.getUiMappedVariablesC1UX();
		if (uiMappedVariables != null)
		{
			String tempVariableLabel = null;
			for (UiMappedVariables mappedVar:uiMappedVariables)
			{
				GenericNameValuePair mergeVar = new GenericNameValuePair();
				mergeVar.setValue(mappedVar.getTextVariableName());
				tempVariableLabel = mappedVar.getDisplayLabel();
				tempVariableLabel = mappedVar.isRequired() ? tempVariableLabel + "*" : tempVariableLabel;
				mergeVar.setName(tempVariableLabel);
				vars.add(mergeVar);
			}
		}
		return vars;
	}

	protected List<String> loadHeaders(MapperData data) {
		ArrayList<String> headers = new ArrayList<>();
		Collection<ColumnNameWrapper> headings = data.getHeadings();
		if ((headings != null) && (!headings.isEmpty())) {
			for (ColumnNameWrapper text : headings) {
				headers.add(text.getColumnName());
			}
		} else {
			int maxCols = data.getMaxColumns();
			for (int i = 1; i < maxCols + 1; i++) {
				headers.add(Integer.toString(i));
			}
		}
		return headers;
	}

	protected List<ArrayList<String>> loadSampleData(MapperData fileData) {
		Collection<String[]> data = fileData.getDataVector();
		int maxCols = fileData.getMaxColumns();
		int numRows = (data != null) ? data.size() : 0;
		if (numRows>3)
		{
			numRows=3;
		}

		ArrayList<ArrayList<String>> results = new ArrayList<>();
		if (numRows > 0)
		{
			int countRows = 0;
			for (int col = 0; col < maxCols; col++) {
				ArrayList<String> colData = new ArrayList<>();
				// The logic of having to restrict rows because more than three rows may have been read into the data vector is based in CustomPoint
				// I don't know if this is really needed any more, but I kept it in case
				countRows = 0;
				for (String[] row : data) {
					if (countRows == numRows) {
						break;
					}
					colData.add(row[col]);
					countRows++;
				}
				results.add(colData);
			}
		}
		return results;
	}

	protected List<String> loadMappingFromUI(UserInterface ui, int id, List<String> headers) {
		// this must be done after setting the headers on the response
		Collection<OECustomDocOrderLineMapSessionBean> mapBeans = ui.getListMappings().get(Integer.toString(id));
		ArrayList<String> mappings = new ArrayList<>();
		if ((mapBeans != null) && (!mapBeans.isEmpty())) {
			String mappedVariableName = null;
			for (String heading : headers) {
				mappedVariableName = null;
				for (OECustomDocOrderLineMapSessionBean mapBean: mapBeans)
				{	// if found, remember the previously selected variable name
					if (mapBean != null && mapBean.getMapColName().equals(heading))
					{
						mappedVariableName = mapBean.getPageFlxVariableName();
						break;
					}
				}
				mappings.add(Util.nullToEmpty(mappedVariableName));
			}
		}

		return mappings;
	}

	protected UiListMapper getListMapper(AppSessionBean asb, UserInterface ui, boolean isViewOnly, int id) throws AtWinXSException {
		UiListMapper uiListMapper = objectMapFactoryService.getEntityObjectMap().getEntity(UiListMapper.class, asb.getCustomToken());
		uiListMapper.initialize(asb, ui.getUiKey(), ui.getCurrentCustomDocOrderLineID(), id, AtWinXSConstant.EMPTY_STRING, isViewOnly); // CAP-9535
		return uiListMapper;
	}

	protected boolean validateListVisibility(ListVO list, AppSessionBean asb) {
		return ((!"D".equals(list.getStatusCode())) && (!list.isPrivate()
					|| ((Util.isBlankOrNull(asb.getProfileID())) && (list.getLoginID().equals(asb.getLoginID())))
					|| ((!Util.isBlankOrNull(asb.getProfileID())) && (list.getProfileID().equals(asb.getProfileID())))));
	}

	@Override
	public C1UXCustDocSaveListMappingResponse saveListMapping(SessionContainer sc, C1UXCustDocSaveListMappingRequest request) throws AtWinXSException {
		C1UXCustDocSaveListMappingResponse response = new C1UXCustDocSaveListMappingResponse();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

		if(!validateCustDocMergeUI(ui)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		try {
			int listID = request.getListId();
			IManageList listComp = getManageListComponent(asb);
			ListVO list = listComp.retrieveSingleList(asb.getSiteID(), asb.getBuID(), listID);
			if ((list == null) || (!validateListVisibility(list, asb))) {
				throw new AccessForbiddenException(this.getClass().getName());
			}

			createMappingBeans(response, ui, request, list, asb);
			if (response.isSuccess()) {
				saveFullOESessionInfo(oeSession, asb.getSessionID());
			}
		}
		catch (AccessForbiddenException e) {
			throw e;
		}
		catch (AtWinXSMsgException e) {
			logger.error(e.toString());
			response.setMessage(e.getMessage());
		}
		catch (Exception e) {
			logger.error(e.toString());
			response.setMessage(getTranslation(asb, SFTranslationTextConstants.DLIST_NOT_EXIST, SFTranslationTextConstants.DLIST_NOT_EXIST_DEFAULT_ERR));
		}

		return response;
	}

	protected void checkForDuplicateMappings(OECustomDocOrderLineMapSessionBean bean, ArrayList<UiMappedVariables> uiMappedVariables,
			Set<String> mappedVarNames, List<String> duplicateMappings, Set<UiMappedVariables> requiredMappedVars, List<String> errors, AppSessionBean asb) {
		UiMappedVariables thisVar = identifyMergeVariable(bean.getPageFlxVariableName(), uiMappedVariables);
		if (thisVar == null) {
			errors.add(getTranslation(asb, "sf.invalidColMapping", "invalid column mapped"));
		} else {
			if (mappedVarNames.contains(bean.getPageFlxVariableName())) {
				if (!duplicateMappings.contains(bean.getPageFlxVariableName())) {
					duplicateMappings.add(bean.getPageFlxVariableLabel());
				}
			} else {
				mappedVarNames.add(bean.getPageFlxVariableName());
				if (thisVar.isRequired()) {
					requiredMappedVars.add(thisVar);
				}
			}
		}
	}

	protected OECustomDocOrderLineMapSessionBean makeMapBean(int listID, C1UXCustDocSaveListMappingBean column, ArrayList<UiMappedVariables> uiMappedVariables,
			Set<String> mappedVarNames, List<String> duplicateMappings, Set<UiMappedVariables> requiredMappedVars, List<String> errors, AppSessionBean asb) {
		OECustomDocOrderLineMapSessionBean bean = new OECustomDocOrderLineMapSessionBean();
		bean.setListID(listID);
		bean.setMapColName(column.getColumnHeading());
		if(!Util.isBlankOrNull(column.getVariableName()))
		{
			bean.setPageFlxVariableLabel(column.getVariableDisplayLabel().replace("*", AtWinXSConstant.EMPTY_STRING));
			bean.setPageFlxVariableName(column.getVariableName());
			checkForDuplicateMappings(bean, uiMappedVariables, mappedVarNames, duplicateMappings, requiredMappedVars, errors, asb);
		}
		else
		{
			bean.setPageFlxVariableLabel(OrderEntryConstants.NOT_MAPPED_COL);
		}
		return bean;
	}

	protected int countMappedColumns(List<OECustomDocOrderLineMapSessionBean> mapBeans) {
		int mappedColumns = 0;
		for (OECustomDocOrderLineMapSessionBean bean : mapBeans) {
			if (!Util.isBlankOrNull(bean.getPageFlxVariableName())) {
				mappedColumns++;
			}
		}
		return mappedColumns;
	}

	protected ArrayList<OECustomDocOrderLineMapSessionBean> buildAndValidateMapping(UserInterface ui,
			C1UXCustDocSaveListMappingRequest request, ListVO list, AppSessionBean asb,	ArrayList<String> errors) throws AtWinXSException {
		UiListMapper uiListMapper = getListMapper(asb, ui, false, list.getListID());
		ArrayList<UiMappedVariables> uiMappedVariables = uiListMapper.getUiMappedVariablesC1UX();
		Set<String> variableNamesSet = new HashSet<>();
		Set<UiMappedVariables> requiredMappedVars = new HashSet<>();
		List<String> duplicateMappings = new ArrayList<>();

		List<C1UXCustDocSaveListMappingBean> listMapping=request.getListColumnMap();
		ArrayList<OECustomDocOrderLineMapSessionBean> mapping = new ArrayList<>();
		for(C1UXCustDocSaveListMappingBean bean: listMapping) {
			mapping.add(makeMapBean(list.getListID(), bean, uiMappedVariables, variableNamesSet, duplicateMappings, requiredMappedVars, errors, asb));
		}

		checkForMissingColumns(request.getMaxColumns(), mapping.size(), errors);
		checkForMissingRequiredMappings(variableNamesSet, uiMappedVariables, errors, asb);
		makeDuplicationErrors(duplicateMappings, errors, asb);
		return mapping;
	}

	protected void checkForMissingColumns(int columnsNeeded, int columnsReceived, ArrayList<String> errors) {
		if (columnsNeeded != columnsReceived) { // this should never be seen by a user - test scenario only for swagger
			errors.add("Incorrect number of columns sent in mapping save request. Please send the correct number of columns for the list.");
		}
	}

	protected void createMappingBeans(C1UXCustDocSaveListMappingResponse response, UserInterface ui, C1UXCustDocSaveListMappingRequest request, ListVO list, AppSessionBean asb) throws AtWinXSException {
		ArrayList<String> errors=new ArrayList<>();
		ArrayList<OECustomDocOrderLineMapSessionBean> mapping = buildAndValidateMapping(ui, request, list, asb, errors);
		int numColumnsMapped = countMappedColumns(mapping);

		// validation
		if (numColumnsMapped == 0) {
			errors.add(getTranslation(asb, SFTranslationTextConstants.TRANS_NM_LIST_NO_COL_MAPPED_ERROR, SFTranslationTextConstants.TRANS_NM_LIST_NO_COL_MAPPED_ERROR_DEFAULT));
		}

		if (!errors.isEmpty()) {
			combineErrors(errors, response);
		} else {
			response.setNumColumnsMapped(numColumnsMapped);
			response.setNumColumnsNotMapped(request.getMaxColumns() - numColumnsMapped);
			response.setTotalColumns(request.getMaxColumns());
			response.setTotalNumDataRows(list.getRecordCount());
			updateUIMapping(ui, mapping, list);
			response.setSuccess(true);
		}
	}

	protected boolean compareMappings(ArrayList<OECustomDocOrderLineMapSessionBean> mapping, ArrayList<OECustomDocOrderLineMapSessionBean> savedMapBeans) throws AtWinXSException {
		return CustomDocsUtil.hasChangedMappedColumns(mapping, savedMapBeans);
	}

	protected void updateSavedMappingFlags(UserInterface ui, ArrayList<OECustomDocOrderLineMapSessionBean> mapping, String mappedListID) throws AtWinXSException {
		Map<String, List<OECustomDocOrderLineMapSessionBean>> allListMappings = ui.getListMappings();
		Map<String, Boolean> savedMappingChanged = ui.getSavedMappingChanged();
		if (allListMappings.containsKey(mappedListID))
		{
			ArrayList<OECustomDocOrderLineMapSessionBean> savedMapBeans = (ArrayList<OECustomDocOrderLineMapSessionBean>) allListMappings.get(mappedListID);
			if (!savedMapBeans.isEmpty())
			{
				savedMappingChanged.put(mappedListID, compareMappings(mapping, savedMapBeans));
			} else {
				savedMappingChanged.put(mappedListID, Boolean.FALSE);
			}
		} else {
			savedMappingChanged.put(mappedListID, Boolean.FALSE);
		}
	}

	protected void updateUIMapping(UserInterface ui, ArrayList<OECustomDocOrderLineMapSessionBean> mapping, ListVO list) throws AtWinXSException {
		String mappedListID = Integer.toString(list.getListID());
		// CAP-42233 - navigation requires this flag to be updated
		updateSavedMappingFlags(ui, mapping, mappedListID);
		ui.getListMappings().put(mappedListID, mapping);

		ArrayList<String> selectedIDs = new ArrayList<>();
		selectedIDs.add(mappedListID);
		ui.setSelectedListIDs(selectedIDs);
	}

	protected void makeDuplicationErrors(List<String> duplicateMappings, ArrayList<String> errors, AppSessionBean asb) {
		if (!duplicateMappings.isEmpty()) {
			errors.add(getTranslation(asb, "dupColsMapped", "Duplicate columns mapped") + AtWinXSConstant.COLON + AtWinXSConstant.EMPTY_STRING + StringUtils.join(duplicateMappings));
		}
	}

	protected void checkForMissingRequiredMappings(Set<String> variableNamesSet, ArrayList<UiMappedVariables> uiMappedVariables,
			ArrayList<String> errors, AppSessionBean asb) {
		for(UiMappedVariables mergeVar : uiMappedVariables)
		{
			if ((mergeVar.isRequired()) && (!variableNamesSet.contains(mergeVar.getTextVariableName()))) {
				errors.add(mergeVar.getDisplayLabel() +  AtWinXSConstant.BLANK_SPACE + getTranslation(asb, "coMappingErr", " mapping is required"));
			}
		}
	}

	protected UiMappedVariables identifyMergeVariable(String variableName, ArrayList<UiMappedVariables> uiMappedVariables) {
		UiMappedVariables thisVar = null;
		if ((uiMappedVariables != null) && (!Util.isBlankOrNull(variableName)))
		{
			for (UiMappedVariables mappedVar:uiMappedVariables)
			{
				if (mappedVar.getTextVariableName().equals(variableName)) {
					thisVar = mappedVar;
				}
			}
		}
		return thisVar;
	}

	@Override
	public C1UXCustDocMappedDataResponse getMappedDataPage(SessionContainer sc, C1UXCustDocMappedDataRequest request) throws AtWinXSException {
		C1UXCustDocMappedDataResponse response = new C1UXCustDocMappedDataResponse();
		AppSessionBean asb = sc.getApplicationSession().getAppSessionBean();
		OrderEntrySession oeSession = (OrderEntrySession)sc.getModuleSession();
		OEOrderSessionBean oeSessionBean = oeSession.getOESessionBean();
		CustomDocumentItem item = oeSessionBean.getCurrentCustomDocumentItem();
		UserInterface ui = item.getUserInterface();

	    OrderListVO orderListVO = getOrderListVO(ui, asb);
		if ((!validateCustDocMergeUI(ui)) || (orderListVO == null)
				|| (ui.getNextPageNumber() != UserInterface.NEXT_PAGE_NUMBER_LIST_DATA)) {
			throw new AccessForbiddenException(this.getClass().getName());
		}

		loadListDataRequested(orderListVO, request, ui, response);

		return response;
	}

	protected void loadListDataRequested(OrderListVO orderListVO, C1UXCustDocMappedDataRequest request,
			UserInterface ui, C1UXCustDocMappedDataResponse response) {
		C1UXCustDocMappedDataPage page = new C1UXCustDocMappedDataPage();
		try {
			page = loadListDataPage(request.getPageNum(), !request.isValidValues(), orderListVO, ui);
			response.setSuccess(true);
		} catch(AtWinXSException e) {
			logger.error(e.toString());
		}
		response.setRecords(page);
	}

}
