/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		JIRA#						Description
 * 	--------	-----------		-----------		--------------------------------
*	09/12/22	A Boomker		CAP-35436		Adding service for returning messages for message center
*	05/11/23    Satishkumar A   CAP-39247       API Change - Modify Message Center Response API to make/use new translation text values
 */
package com.rrd.c1ux.api.models.messages;

import java.util.ArrayList;
import com.rrd.c1ux.api.models.ModelConstants;

import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.Util;

public class MessageCategory {
	protected String categoryLabel = AtWinXSConstant.EMPTY_STRING;
	protected String categoryImageURL = AtWinXSConstant.EMPTY_STRING;
	protected ArrayList<MessageCenterMessage> messages = new ArrayList<MessageCenterMessage>();

	// if no values are passed in, assume it is the high value group
	public MessageCategory()
	{
		categoryImageURL = ModelConstants.HIGH_VALUE_MESSAGE_CATEGORY_IMAGE_PATH;
	}
	
	public MessageCategory(String label, String image)
	{
		categoryLabel = label;
		convertCategoryImage(image);
	}
	
	protected void convertCategoryImage(String cpImagePath)
	{
		String path = Util.nullToEmpty(cpImagePath);
		for (int i = 0; i < ModelConstants.MESSAGE_CATEGORY_IMG_MAP_CP_TO_C1UX.length; i++)
		{
			if (ModelConstants.MESSAGE_CATEGORY_IMG_MAP_CP_TO_C1UX[i][0].equals(path))
			{
				categoryImageURL = ModelConstants.MESSAGE_CATEGORY_IMG_MAP_CP_TO_C1UX[i][1];
			}
		}
		
		if (Util.isBlank(categoryImageURL))
		{
			categoryImageURL = cpImagePath; // if we passed in c1ux somehow, use it
		}
	}
	public String getCategoryLabel() {
		return categoryLabel;
	}

	public void setCategoryLabel(String categoryLabel) {
		this.categoryLabel = categoryLabel;
	}

	public String getCategoryImageURL() {
		return categoryImageURL;
	}

	public void setCategoryImageURL(String categoryImageURL) {
		this.categoryImageURL = categoryImageURL;
	}

	public ArrayList<MessageCenterMessage> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<MessageCenterMessage> messages) {
		this.messages = messages;
	}


}
