package com.rrd.c1ux.api.models.items;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Krishna Natarajan
 *
 */
@Data
@AllArgsConstructor
public class ItemRpt implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String itemDescription;
	private String itemMasterIndicator;
	private String priceUOMCode;
	private double pricingConversionFactor;
	private String stockingUOM;
	private double stockUOMConversionFactor;
	private String masterPackUOM;
	private double masterPackUOMConversionFactor;
	private String innerPackUOM;
	private double innerPackUOMConversionFactor;
	private double itemReorderPoint;
	private double itemReorderQty;
	private String itemStatusCode;
	private String itemTypeCode;
	private double itemMininumOrderQty;
	private double itemMultipleOrderQty;
	private double maxinumOrderQty;
	private String contractNumber;
	private String itemProductClassificationCode;
	private String itemPrintOnDemandTypeCode;
	private String itemSKUDesc;
	private String jobComboProductCode;
	private String productSuffixCode;
	private java.util.Date itemExpirationDate;
	private java.util.Date itemReorderDate;
	private String plantNumber;
	private String corporateNumber;
	private String soldToNumber;
	private String customerItemNumber;
	private String shortItemNumber;
	private String customerItemRef1TxtFld;
	private String customerItemRef2TxtFld;
	private ItemRptKey key;
	private String wallaceItemNbr;
	private String wallaceItemDesc;
	private java.util.Date createTimeStamp;
	private java.util.Date changeTimeStamp;
	private String virtualMasterIndicator;
	private String cancelBOIndicator;

	} // End class
