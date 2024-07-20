/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	04/03/24				M Sakthi				CAP-48202					 C1UX BE - Create API to save columns to map
 *	04/04/24				M Sakthi				CAP-48202					 Review comments changes
*/

package com.rrd.c1ux.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.gwt.listscommon.mapping.AltColumnNameWrapperCell;
import com.rrd.custompoint.gwt.listscommon.mapping.AltColumnNameWrapperCellData;
import com.rrd.custompoint.gwt.listscommon.mapping.ColumnNameWrapperCell;
import com.rrd.custompoint.gwt.listscommon.mapping.ColumnNameWrapperCellData;
import com.rrd.custompoint.gwt.listscommon.mapping.LMapperData;
import com.rrd.custompoint.gwt.listscommon.mapping.ListMapperData;
import com.rrd.custompoint.orderentry.ao.OEMappedVariableResponseBean;
import com.rrd.custompoint.orderentry.entity.DistributionListDetails;
import com.rrd.custompoint.orderentry.entity.OrderFromaFileListDetails;
import com.rrd.custompoint.orderentry.entity.SiteListMapping;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.AtWinXSWrpException;
import com.wallace.atwinxs.framework.util.Message;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.framework.util.mapper.ColumnNameWrapper;
import com.wallace.atwinxs.framework.util.mapper.Mapper;
import com.wallace.atwinxs.framework.util.mapper.MapperConstants;
import com.wallace.atwinxs.framework.util.mapper.MapperData;
import com.wallace.atwinxs.framework.util.mapper.MapperInput;
import com.wallace.atwinxs.framework.util.mapper.MapperInputFactory;
import com.wallace.atwinxs.lists.ao.ListsBaseAssembler;
import com.wallace.atwinxs.lists.ao.ManageListsResponseBean;
import com.wallace.atwinxs.lists.util.ManageListsConstants;
import com.wallace.atwinxs.lists.vo.ListVO;
import com.wallace.atwinxs.orderentry.ao.OEListAssembler;
import com.wallace.atwinxs.orderentry.session.OECustomDocOrderLineMapSessionBean;
import com.wallace.atwinxs.orderentry.session.OEDistributionListBean;
import com.wallace.atwinxs.orderentry.session.OEOrderFromFileBean;
import com.wallace.atwinxs.orderentry.session.OEOrderSessionBean;
import com.wallace.atwinxs.orderentry.util.DeliveryOptionsFormBean;
import com.wallace.atwinxs.orderentry.util.OrderEntryConstants;
import com.wallace.atwinxs.orderentry.util.OrderEntryUtil;

public class COListsUtil{
	
	private static final Logger logger = LoggerFactory.getLogger(COListsUtil.class);
	
	private COListsUtil() {
		
	}
	
	/**
	 * This method will get the multipart params from the request
	 * @param volatileSessionBean
	 * @param appSessionBean
	 * @return OEListAssembler
	 */
	public static OEListAssembler getAssembler(VolatileSessionBean volatileSessionBean, AppSessionBean appSessionBean)
    {
        return new OEListAssembler(volatileSessionBean, appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale());
    }
	
	
	
	//CP-13549
	/**
	 * Method doMapcolumnNames
	 * 
	 * @param mapBeans
	 * @param volatileSessionBean
	 * @param loginID
	 * @param isNewFile - CP-11537 RAR
	 * @throws AtWinXSException 
	 */
	public static String doMapcolumnNamesOFF(
		OECustomDocOrderLineMapSessionBean[] mapBeans,
		VolatileSessionBean volatileSessionBean,
		String loginID,
		OrderFromaFileListDetails offListDetails,
		AppSessionBean appSessionBean) throws AtWinXSException
	{
		Properties propColumn = new Properties();
		
		for (int i = 0; i < mapBeans.length; i++)
		{
			//Set value for varName
			String varName = mapBeans[i].getPageFlxVariableName();
			if (varName!=null)
			{
				//Cust ref and po will be saved as CUST_REF and PO_NUM in db
				//Others will be saved as labels (existing)
				String key = mapBeans[i].getPageFlxVariableLabel();
				if(varName.startsWith(ManageListsConstants.PARAM_CUST_REF_MAPPING) || varName.startsWith(ManageListsConstants.PARAM_PO_NUM_MAPPING))
				{
					key = mapBeans[i].getPageFlxVariableName();
				}
				String conIntToStr=Integer.toString(i);
				propColumn.setProperty(key,conIntToStr);
			}
		}
		
		//CP-11537 RAR - Pass the isNewFile indicator.
		return offListDetails.mapColumns(propColumn, volatileSessionBean, loginID, appSessionBean);
	}
	
	/**
	 * Method doMapcolumnNames
	 * 
	 * @param mapBeans
	 * @param volatileSessionBean
	 * @param loginID
	 * @param isNewFile - CP-11537 RAR
	 * @throws AtWinXSException 
	 */
	public static void doMapcolumnNames(
		OECustomDocOrderLineMapSessionBean[] mapBeans,
		VolatileSessionBean volatileSessionBean,
		String loginID,
		DistributionListDetails distributionListDetails,
		AppSessionBean appSessionBean,
		OEDistributionListBean distListBean, boolean isNewFile) throws AtWinXSException
	{
		Properties propColumn = new Properties();
		
		for (int i = 0; i < mapBeans.length; i++)
		{
			//Set value for varName
			String varName = mapBeans[i].getPageFlxVariableName();
			if (varName!=null)
			{
				//Cust ref and po will be saved as CUST_REF and PO_NUM in db
				//Others will be saved as labels (existing)
				String key = mapBeans[i].getPageFlxVariableLabel();
				if(varName.startsWith(ManageListsConstants.PARAM_CUST_REF_MAPPING) || varName.startsWith(ManageListsConstants.PARAM_PO_NUM_MAPPING))
				{
					key = mapBeans[i].getPageFlxVariableName();
				}
				String conIntToStr=Integer.toString(i);
				propColumn.setProperty(key,conIntToStr);
			}
		}
		
		//CP-11537 RAR - Pass the isNewFile indicator.
		distributionListDetails.mapColumns(propColumn, volatileSessionBean, loginID, appSessionBean, distListBean, isNewFile);
	}
	
	public static void setDistributionListBean(ManageListsResponseBean manageLists, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean) throws AtWinXSException {

		OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
		try {

			if (distListBean == null) {

				distListBean = new OEDistributionListBean(manageLists.getListID(), manageLists.getListName(),
						appSessionBean.getGlobalFileUploadPath(), String.valueOf(manageLists.isPrivate()),
						manageLists.getListDescription(), manageLists.getUploadedDate(), manageLists.getLastUsedDate(),
						manageLists.getRecordCount(), String.valueOf(manageLists.isContainsHeadings()),
						manageLists.getLoginID(), manageLists.getCustomerFileName(), manageLists.getSourceFileName(),
						manageLists.getFileSize(), String.valueOf(manageLists.isEncrytedInd())// CAP-29070
				);

			} else {

				distListBean.setListID(manageLists.getListID());
				distListBean.setListName(manageLists.getListName());
				distListBean.setListPath(appSessionBean.getGlobalFileUploadPath());
				distListBean.setIsPrivateList(String.valueOf(manageLists.isPrivate()));
				distListBean.setListDescription(manageLists.getListDescription());
				distListBean.setListDateCreated(manageLists.getUploadedDate());
				distListBean.setListLastUsed(manageLists.getLastUsedDate());
				distListBean.setListEntries(manageLists.getRecordCount());
				distListBean.setHasListHeadings(String.valueOf(manageLists.isContainsHeadings()));
				distListBean.setListOwner(manageLists.getLoginID());
				distListBean.setListFileName(manageLists.getCustomerFileName());
				distListBean.setListSysFileName(manageLists.getSourceFileName());
				distListBean.setListSize(manageLists.getFileSize());
				distListBean.setIsEncrytedInd(String.valueOf(manageLists.isEncrytedInd()));// CAP-29070
			}
		} catch (AtWinXSException e) {

			// CAP-18657 Replaced printstacktrace() call with Logger
			logger.error(Util.class.getName() + " - " + e.getMessage(), e);
		}
		oeSessionBean.setDistributionListBean(distListBean);
	}
	
	public static Message buildAndValidateList(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeSessionBean, DeliveryOptionsFormBean dlvryOptionsBean) throws AtWinXSException {
		Message msg = new Message();

		LMapperData dlmapper = buildLMapper(appSessionBean, volatileSessionBean, oeSessionBean,
				dlvryOptionsBean.getSelectedListID());

		ArrayList<String> errors = new ArrayList<>();
		OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
		oeSessionBean.setSendToDistListUsingQtyFromFile(true);

		if (distListBean == null) {
			errors.add("Order with Distribution List information is null");
		}

		OECustomDocOrderLineMapSessionBean[] mapBeans = null;
		ArrayList<OECustomDocOrderLineMapSessionBean> array = new ArrayList<>();

		SiteListMapping siteListMappingObj = ObjectMapFactory.getEntityObjectMap().getEntity(SiteListMapping.class,
				appSessionBean.getCustomToken());
		siteListMappingObj.populate(appSessionBean, distListBean, appSessionBean.getLoginID());
		siteListMappingObj.getMappingFromTable(siteListMappingObj);

		//CAP-48365
		processListMapperHeadings(dlmapper, array, siteListMappingObj);

		mapBeans = new OECustomDocOrderLineMapSessionBean[array.size()];
		mapBeans = array.toArray(mapBeans);

		// CP-11537 RAR - If not all items are valid, then do try to validate mapping
		// anymore
		if (null != distListBean)												
		{
			distListBean.setMapBeans(mapBeans);
			distListBean.setListID(dlvryOptionsBean.getSelectedListID());

			OEListAssembler assembler = getAssembler(volatileSessionBean, appSessionBean);

			boolean hasPassedValidation = assembler.doValidateMappingsFromFile(errors, mapBeans, oeSessionBean,
					appSessionBean.getGlobalFileUploadPath(), appSessionBean.getCorporateNumber());

			if (hasPassedValidation) {
				assembler.setMappedCustRefFieldsToSession(oeSessionBean);
			}
		}

		if (!errors.isEmpty()) {
			Collection<String> errMsgs = errors;
			String distListErrorGenMsg = TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(),
					appSessionBean.getCustomToken(), "dl_error_gen_msg");

			if ((!Util.isBlankOrNull(msg.getErrInstructionMsg()))// CAP-19456
					&& (null != msg.getErrMsgItems() && !msg.getErrMsgItems().isEmpty())) {
				distListErrorGenMsg = AtWinXSConstant.EMPTY_STRING;
				errMsgs = msg.getErrMsgItems();

				for (String error : errors) {
					errMsgs.add(error);
				}
			}

			if (!Util.isBlankOrNull(distListErrorGenMsg)) {

				msg.setErrInstructionMsg(distListErrorGenMsg + "<br>");
			}

			msg.setErrMsgItems(errMsgs);
		}
		// RAR - Save the mapping to tables only if there are no mapping validation
		// errors.
		else {

			DistributionListDetails distributionListDetails = ObjectMapFactory.getEntityObjectMap()
					.getEntity(DistributionListDetails.class, appSessionBean.getCustomToken());
			doMapcolumnNames(mapBeans, volatileSessionBean, appSessionBean.getLoginID(), distributionListDetails,
					appSessionBean, distListBean, false);
		}

		return msg;
	}
	
	private static void processListMapperHeadings(LMapperData dlmapper, ArrayList<OECustomDocOrderLineMapSessionBean> array, SiteListMapping siteListMappingObj)
	{
		String mappedColNames = siteListMappingObj.getMappedColumnNamesStr();
		String[] mappedColNamesArr = mappedColNames != null && !mappedColNames.equals(AtWinXSConstant.EMPTY_STRING) ? mappedColNames.split(":")
				: new String[0];

		int headingIndex = 0;
		for (ColumnNameWrapperCell heading : dlmapper.getHeadings()) {
			String headingName = heading.getColumnName();
			String mapName = AtWinXSConstant.EMPTY_STRING;
			String variableName = AtWinXSConstant.EMPTY_STRING;

			boolean found = false;
			for (String mappedColumn : mappedColNamesArr) {
				String mappedColumnAltColumnName = mappedColumn.substring(0, mappedColumn.indexOf("="));
				String mappedColumnHeadingIndex = mappedColumn.substring((mappedColumn.indexOf("=") + 2),
						mappedColumn.length());

				if (Integer.parseInt(mappedColumnHeadingIndex) == headingIndex) {
					for (AltColumnNameWrapperCell altColumn : dlmapper.getAltColumnNames()) {
						String compareDisplayName = altColumn.getDisplayLabel();

						// Display Labels that contains ":" are Item #, Qty and UOM. So, we need to stip
						// out everything from ":" to the end of the text before comparing it against
						// the mappedColumnAltColumnName.
						if (altColumn.getDisplayLabel().indexOf(":") > 0) {
							compareDisplayName = altColumn.getDisplayLabel().substring(0,
									altColumn.getDisplayLabel().indexOf(":"));
						}

						if (mappedColumnAltColumnName.equals(altColumn.getTextVariableName())
								|| mappedColumnAltColumnName.equals(compareDisplayName)) {
							found = true;
							mapName = altColumn.getDisplayLabel();
							variableName = altColumn.getTextVariableName();
							break;
						}
					}
				}

				if (found) {
					break;
				}
			}

			if (Util.isBlankOrNull(headingName)) {
				headingName = Integer.toString(headingIndex + 1);
			}

			OECustomDocOrderLineMapSessionBean bean = new OECustomDocOrderLineMapSessionBean();

			if (!Util.isBlank(mapName))// CAP-19456
			{
				bean.setMapColName(headingName);
				bean.setPageFlxVariableLabel(mapName);
				bean.setPageFlxVariableName(variableName);
			} else {
				bean.setMapColName(headingName);
				bean.setPageFlxVariableLabel(OrderEntryConstants.NOT_MAPPED_COL);
			}

			array.add(bean);

			headingIndex++;
		}
	}
	
	private static LMapperData buildLMapper(AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean,
			OEOrderSessionBean oeSessionBean, String selectedListID)
			throws AtWinXSException {
		int listID = Util.safeStringToInt(selectedListID);

		ListsBaseAssembler listBaseAsm = new ListsBaseAssembler(appSessionBean.getCustomToken(),
				appSessionBean.getDefaultLocale());
		ListVO listVO = new ListVO(appSessionBean.getSiteID(), appSessionBean.getBuID(), listID);

		try {
			listVO = listBaseAsm.getList(listID, appSessionBean.getSiteID(), appSessionBean.getBuID());
		} catch (AtWinXSException e1) {
			// CAP-18657 Replaced printstacktrace() call with Logger
			logger.error(COListsUtil.class.getName() + " - " + e1.getMessage(), e1);
		}

		ManageListsResponseBean manageLists = new ManageListsResponseBean();

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
		}

		setDistributionListBean(manageLists, oeSessionBean, appSessionBean);
		OEDistributionListBean distListBean = oeSessionBean.getDistributionListBean();
		DistributionListDetails distributionListDetails = ObjectMapFactory.getEntityObjectMap()
				.getEntity(DistributionListDetails.class, appSessionBean.getCustomToken());
		MapperData mapperdata = new MapperData();
		LMapperData dlmapper = new ListMapperData();

		if (distributionListDetails.validatefile(distListBean) || manageLists.isListFeedInd()) {
			mapperdata = getOrderwithDistListPreview(appSessionBean.getGlobalFileUploadPath(), oeSessionBean,
					appSessionBean, volatileSessionBean);
			distributionListDetails.populate(mapperdata);
		}

		Collection<ColumnNameWrapperCell> heading = new ArrayList<>();
		Collection<AltColumnNameWrapperCell> altColumnNames = new ArrayList<>();
		Collection<ArrayList<String>> dataVector = new ArrayList<>();
		int maxCols = mapperdata.getMaxColumns();

		if (null == distributionListDetails.getHeadings() || distributionListDetails.getHeadings().size() < maxCols) {
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

		setDataVector(distributionListDetails, dataVector);

		OEMappedVariableResponseBean[] variables = null;
		variables = (OEMappedVariableResponseBean[]) distributionListDetails.getnewColNames()
				.toArray(new OEMappedVariableResponseBean[] {});

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

		dlmapper.setAltColumnNames(altColumnNames);
		dlmapper.setDataVector(dataVector);
		dlmapper.setHeadings(heading);
		dlmapper.setRowCount(distributionListDetails.getMaxRows());

		return dlmapper;
	}
	
	private static void setDataVector(DistributionListDetails distributionListDetails, Collection<ArrayList<String>> dataVector)
	{
		for (String[] dataArray : distributionListDetails.getData()) {
			dataVector.add(new ArrayList<>(Arrays.asList(dataArray)));
		}
	}
	
	public static MapperData getOrderwithDistListPreview(String globalFileUploadPath, OEOrderSessionBean oeSessionBean,
			AppSessionBean appSessionBean, VolatileSessionBean volatileSessionBean)
			throws AtWinXSException {
		OEDistributionListBean distributionListBean = oeSessionBean.getDistributionListBean();

		String className = "CODelivertInformationServiceImpl";
		if (distributionListBean == null) {
			throw new AtWinXSWrpException(new NullPointerException("Order with DistributionList information is null"),
					className);
		}

		if (Util.isBlank(Util.nullToEmpty(distributionListBean.getListID())))// CAP-19456
		{
			throw new AtWinXSWrpException(new NullPointerException("List ID is null"), className);
		}
		String fileName = Util.nullToEmpty(distributionListBean.getListSysFileName());
		if (Util.isBlank(fileName))// CAP-19456
		{
			throw new AtWinXSWrpException(new NullPointerException("List file name is null"),
					className);
		}

		boolean hasHeadings = Boolean.parseBoolean(distributionListBean.getHasListHeadings());

		MapperInput input = null;
		
		input = MapperInputFactory.getMapperInputInstance(globalFileUploadPath, fileName, hasHeadings,
					MapperConstants.DEFAULT_DELIMITER, "");

		try {
			Mapper mapper = new Mapper();
			MapperData mapperdata = mapper.getDataPreview(input);
			Vector<OEMappedVariableResponseBean> columnNames = null;

			columnNames = getOrderwithDistListColumnNames(mapperdata, oeSessionBean, appSessionBean,
						volatileSessionBean);
			
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

			throw OrderEntryUtil.wrapCustomDateThrownException(e, listName, className);
		}
	}
	
	public static Vector<OEMappedVariableResponseBean> getOrderwithDistListColumnNames(MapperData mapperdata,
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
			assembler.getCustomerReferenceFieldMappings(cols, oeSession, appSessionBean.getCorporateNumber(),
					soldToNumber);
			assembler.getPONumberMapping(cols, oeSession, appSessionBean.getCorporateNumber(), soldToNumber);
		}
		return cols;
	}
}

