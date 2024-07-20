/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		JIRA#						Description
 * 	--------	-----------		-----------		--------------------------------
*	09/12/22	A Boomker		CAP-35436		Adding service for returning messages for message center
 */
package com.rrd.c1ux.api.models.messages;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;

public class MessageCenterMessage {
	protected String msgID = "0";
	protected String title = AtWinXSConstant.EMPTY_STRING;
	protected String effectiveDate = AtWinXSConstant.EMPTY_STRING;
	protected String content = AtWinXSConstant.EMPTY_STRING;
	public String getMsgID() {
		return msgID;
	}
	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
 
}
