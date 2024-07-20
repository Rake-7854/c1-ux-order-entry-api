/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	07/20/23				A Boomker				CAP-42295					Initial Version
 *	02/19/24				A Boomker				CAP-44837					Adding flag indicating list option was an upload
 */
package com.rrd.c1ux.api.models.custdocs;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="C1UXUIListOption", description = "Response Class equivalent to UIListOption in CP - info to display a single list option", type = "object")
public class C1UXUIListOption {
	@Schema(name="listId", description = "Unique number for this specific option on the list. Also referred to as list value ID.", type="string")
	protected String listId;
	@Schema(name="textValue", description = "Text value for this specific option on the list. For file lists, this will be the same as plant value.", type="string")
	protected String textValue;
	@Schema(name="proofValue", description = "Proof file name value for this specific option on the list if it is a file list.", type="string")
	protected String proofValue;
	@Schema(name="plantValue", description = "Plant file name value for this specific option on the list if it is a file list.", type="string")
	protected String plantValue;
	@Schema(name="thumbnailValue", description = "Thumbnail file name value for this specific option on the list if it is a file list.", type="string")
	protected String thumbnailValue;
	@Schema(name="htmlValue", description = "Html valid value for this specific option on the list. This may differ from the text value if there are characters that cannot display in HTML in the text value.", type="string")
	protected String htmlValue; 			// this goes in the value="" HTML attribute
	@Schema(name="htmlLabel", description = "Html valid Label for this specific option on the list.", type="string")
	protected String htmlLabel; 			// this goes in the label spot in the <option> element
	@Schema(name="defaults", description = "Flag indicating that this option defaults for the list.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean defaults = false;
	@Schema(name="selected", description = "Flag indicating that this option is seleted for the list.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean selected = false;
	@Schema(name="docInsertId", description = "If this is an insert list, each list option will have an insert ID corresponding to the unique insert used for the files.", type = "number")
	protected int docInsertId = AtWinXSConstant.INVALID_ID;
	@Schema(name="upload", description = "Flag indicating that this option was an upload for the list and should link to the upload API.", type = "boolean", allowableValues = {"false", "true"})
	protected boolean upload = false;

	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getTextValue() {
		return textValue;
	}
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}
	public String getProofValue() {
		return proofValue;
	}
	public void setProofValue(String proofValue) {
		this.proofValue = proofValue;
	}
	public String getPlantValue() {
		return plantValue;
	}
	public void setPlantValue(String plantValue) {
		this.plantValue = plantValue;
	}
	public String getThumbnailValue() {
		return thumbnailValue;
	}
	public void setThumbnailValue(String thumbnailValue) {
		this.thumbnailValue = thumbnailValue;
	}
	public String getHtmlValue() {
		return htmlValue;
	}
	public void setHtmlValue(String htmlValue) {
		this.htmlValue = htmlValue;
	}
	public String getHtmlLabel() {
		return htmlLabel;
	}
	public void setHtmlLabel(String htmlLabel) {
		this.htmlLabel = htmlLabel;
	}
	public boolean isDefaults() {
		return defaults;
	}
	public void setDefaults(boolean defaults) {
		this.defaults = defaults;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public int getDocInsertId() {
		return docInsertId;
	}
	public void setDocInsertId(int docInsertId) {
		this.docInsertId = docInsertId;
	}
	public boolean isUpload() {
		return upload;
	}
	public void setUpload(boolean upload) {
		this.upload = upload;
	}

}
