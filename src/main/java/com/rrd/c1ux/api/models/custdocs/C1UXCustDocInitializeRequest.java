/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date			Modified By			JIRA#					Description
 * 	--------		-----------			------------			--------------------------------
 *	07/25/23		A Boomker			CAP-42223				Initialize request
 */

package com.rrd.c1ux.api.models.custdocs;

import com.rrd.c1ux.api.models.addtocart.ItemAddToCartRequest;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.customdocs.util.ICustomDocsAdminConstants;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXCustDocInitializeRequest", description = "Request to initialize a User Interface", type = "object")
public class C1UXCustDocInitializeRequest extends ItemAddToCartRequest {
//	@Schema(name = "itemNumber", description = "Customer Item Number for Custom Document", type = "String", example = "10.1875 WIDTH")
//	@Schema(name = "vendorItemNumber", description = "Vendor Item Number for Custom Document", type = "String", example = "74101875X1000")
//	@Schema(name = "catalogLineNumber", description = "Catalog Line Number for Custom Document to be added to the cart", type = "String", example = "313963")

	@Schema(name = "orderLineNumber", description = "The order line number of the custom document being edited. This only applies when not adding to the cart and entering the UI for a specific item. Default value is -1.", type = "number", example="-1")
	protected int orderLineNumber = AtWinXSConstant.INVALID_ID;
	@Schema(name = "customDocumentOrderLineNumber", description = "The custom document order line number of the custom document being edited. This only applies when not adding to the cart and entering the UI for a specific item. Default value is -1.", type = "number", example="-1")
	protected int customDocumentOrderLineNumber = AtWinXSConstant.INVALID_ID;
	@Schema(name = "viewOnly", description = "Value indicating the UI should be locked into view only mode. Default value is false.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean viewOnly = false;
	@Schema(name = "kitActivity", description = "Activity Class in Manage Kits - applicable for UI entry point Kit only.", type = "String", example = AtWinXSConstant.EMPTY_STRING)
	protected String kitActivity = null;
	@Schema(name = "orderFromFileIndex", description = "Item index in order from file file - applicable for UI entry points for Order From File only. Default value is -1.", type = "String", example = "-1")
	protected int orderFromFileIndex = AtWinXSConstant.INVALID_ID;
	@Schema(name = "orderQuestionsFromRouting", description = "Code that can indicating entry to order questions from routing justification - applicable for UI entry point order questions only. Default value is null.", type = "String")
	protected String orderQuestionsFromRouting = null;
	@Schema(name = "orderQuestionsProjectID", description = "Order Questions UI project ID - applicable for UI entry point order questions only. Default value is -1.", type = "number", example="-1")
	protected int orderQuestionsProjectID = AtWinXSConstant.INVALID_ID;
	@Schema(name = "orderQuestionsUINumber", description = "Order Questions UI template number - applicable for UI entry point order questions only. Default value is -1.", type = "number", example="-1")
	protected int orderQuestionsUINumber = AtWinXSConstant.INVALID_ID;

	@Schema(name = "entryPoint", description = "Entry point for this UI", type = "String", example = ICustomDocsAdminConstants.ENTRY_POINT_CATALOG_EXPRESS,
	allowableValues = {ICustomDocsAdminConstants.ENTRY_POINT_CATALOG_EXPRESS,
			ICustomDocsAdminConstants.ENTRY_POINT_CATALOG_NORMAL,
			ICustomDocsAdminConstants.ENTRY_POINT_CART_ADD_FROM_STUB,
			ICustomDocsAdminConstants.ENTRY_POINT_TRY_UI,
			ICustomDocsAdminConstants.ENTRY_POINT_CART_EDIT,
			ICustomDocsAdminConstants.ENTRY_POINT_ROUTING_AND_APPROVAL,
			ICustomDocsAdminConstants.ENTRY_POINT_ORDER_FROM_FILE_ADD,
			ICustomDocsAdminConstants.ENTRY_POINT_ORDER_FROM_FILE_EDIT,
			ICustomDocsAdminConstants.ENTRY_POINT_DMIS_ADD,
			ICustomDocsAdminConstants.ENTRY_POINT_DMIS_EDIT,
			ICustomDocsAdminConstants.ENTRY_POINT_DMIS_INSPECT,
			ICustomDocsAdminConstants.ENTRY_POINT_KIT,
			ICustomDocsAdminConstants.ENTRY_POINT_KIT_TEMPLATE,
			ICustomDocsAdminConstants.ENTRY_POINT_ORDER_QUESTIONS,
			ICustomDocsAdminConstants.ENTRY_POINT_SUBSCR_MOD,
			ICustomDocsAdminConstants.ENTRY_POINT_CONTINUE_VERSION,
			ICustomDocsAdminConstants.ENTRY_POINT_NEW_REQUEST})
	protected String entryPoint = ICustomDocsAdminConstants.ENTRY_POINT_CATALOG_EXPRESS;

	public C1UXCustDocInitializeRequest()
	{
		super();
	}

	public C1UXCustDocInitializeRequest(ItemAddToCartRequest atc)
	{
		setAvailabilityCode(atc.getAvailabilityCode());
		setCatalogLineNumber(atc.getCatalogLineNumber());
		setItemNumber(atc.getItemNumber());
		setItemQuantity(atc.getItemQuantity());
		setPrice(atc.getPrice());
		setSelectedUom(atc.getSelectedUom());
		setVendorItemNumber(atc.getVendorItemNumber());
	}

	public int getOrderLineNumber() {
		return orderLineNumber;
	}

	public void setOrderLineNumber(int orderLineNumber) {
		this.orderLineNumber = orderLineNumber;
	}

	public int getCustomDocumentOrderLineNumber() {
		return customDocumentOrderLineNumber;
	}

	public void setCustomDocumentOrderLineNumber(int customDocumentOrderLineNumber) {
		this.customDocumentOrderLineNumber = customDocumentOrderLineNumber;
	}

	public boolean isViewOnly() {
		return viewOnly;
	}

	public void setViewOnly(boolean viewOnly) {
		this.viewOnly = viewOnly;
	}

	public String getKitActivity() {
		return kitActivity;
	}

	public void setKitActivity(String kitActivity) {
		this.kitActivity = kitActivity;
	}

	public int getOrderFromFileIndex() {
		return orderFromFileIndex;
	}

	public void setOrderFromFileIndex(int orderFromFileIndex) {
		this.orderFromFileIndex = orderFromFileIndex;
	}

	public String getOrderQuestionsFromRouting() {
		return orderQuestionsFromRouting;
	}

	public void setOrderQuestionsFromRouting(String orderQuestionsFromRouting) {
		this.orderQuestionsFromRouting = orderQuestionsFromRouting;
	}

	public int getOrderQuestionsProjectID() {
		return orderQuestionsProjectID;
	}

	public void setOrderQuestionsProjectID(int orderQuestionsProjectID) {
		this.orderQuestionsProjectID = orderQuestionsProjectID;
	}

	public int getOrderQuestionsUINumber() {
		return orderQuestionsUINumber;
	}

	public void setOrderQuestionsUINumber(int orderQuestionsUINumber) {
		this.orderQuestionsUINumber = orderQuestionsUINumber;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if ((obj == null) || (obj.getClass() == this.getClass())) {
			return false;
		}

		C1UXCustDocInitializeRequest fobj = (C1UXCustDocInitializeRequest) obj;
		return ((super.equals(fobj))
				&& (this.orderLineNumber == fobj.getOrderLineNumber())
				&& (this.customDocumentOrderLineNumber == fobj.getCustomDocumentOrderLineNumber())
				&& (this.viewOnly == fobj.isViewOnly())
				&& (Util.nullToEmpty(this.kitActivity).equals(Util.nullToEmpty(fobj.getKitActivity())))
				&& (this.orderFromFileIndex == fobj.getOrderFromFileIndex())
				&& (Util.nullToEmpty(this.orderQuestionsFromRouting).equals(Util.nullToEmpty(fobj.getOrderQuestionsFromRouting())))
				&& (this.orderQuestionsProjectID == fobj.getOrderQuestionsProjectID())
				&& (this.orderQuestionsUINumber == fobj.getOrderQuestionsUINumber()));
	}

	@Override
	public int hashCode() {
		return super.hashCode();

	}
}
