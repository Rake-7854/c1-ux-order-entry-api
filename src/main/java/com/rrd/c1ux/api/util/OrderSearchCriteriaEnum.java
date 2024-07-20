/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  11/28/23	N Caceres		CAP-45133	Initial version
 */
package com.rrd.c1ux.api.util;

import java.util.stream.Stream;

public enum OrderSearchCriteriaEnum {
	
	PO_NUMBER("CriteriaPONumber", "sf.purchaseOrderNum", 20, true),
	SALES_REF("CriteriaSalesRef", "CriteriaSalesRef_desc", 20, true),
	ORDER_NUMBER("CriteriaOrderNumber", "CriteriaOrderNumber_desc", 8, true),
	STATUS_CODE("CriteriaStatusCode", "CriteriaStatusCode_desc", 4, false),
	INVOICE_NUMBER("CriteriaInvoiceNumber", "CriteriaInvoiceNumber_desc", 9, false),
	ORDER_NAME("CriteriaOrderTitle", "CriteriaOrderTitle_desc", 150, false),
	SHIP_TO_NAME("CriteriaShipToName", "CriteriaShipToName_desc", 140, false),
	SHIP_TO_ATTENTION("CriteriaShipToAttention", "CriteriaShipToAttention_desc", 35, false),
	SHIP_TO_COUNTRY("CriteriaShipToCountry", "CriteriaShipToCountry_desc", 3, false),
	SHIP_TO_STATE("CriteriaShipToState", "CriteriaShipToState_desc", 4, false),
	SHIP_TO_ZIP("CriteriaShipToZip", "CriteriaShipToZip_desc", 9, false),
	ITEM_NUMBER("CriteriaItemNumber", "CriteriaItemNumber_desc", 30, true);
	
	private final String criteriaName;
	private final String translationName;
	private final int criteriaSize;
	private final boolean quickSearch;
	
	private OrderSearchCriteriaEnum(String criteriaName, String translationName, int criteriaSize, boolean quickSearch) {
		this.criteriaName = criteriaName;
		this.translationName = translationName;
		this.criteriaSize = criteriaSize;
		this.quickSearch = quickSearch;
	}
	public String getCriteriaName() {
		return criteriaName;
	}
	public String getTranslationName() {
		return translationName;
	}
	public int getCriteriaSize() {
		return criteriaSize;
	}
	public boolean isQuickSearch() {
		return quickSearch;
	}
	
	public static Stream<OrderSearchCriteriaEnum> stream() {
        return Stream.of(OrderSearchCriteriaEnum.values()); 
    }

}
