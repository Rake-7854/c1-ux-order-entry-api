/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		JIRA#						Description
 * 	--------	-----------		-----------		--------------------------------
 *	09/12/22	A Boomker		CAP-35436		Adding service for returning messages for message center
 *  03/08/23 	Sumit Kumar		CAP-38711		Add Translation to ResponseObject for /api/messagecenter/getMessages
 *  04/27/23    Satishkumar A   CAP-39247       API Change - Modify Message Center Response API to make/use new translation text values 
 */
package com.rrd.c1ux.api.models.messages;

import java.util.ArrayList;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public class MessageCenterResponse {
	
	//CAP-39247
	@Schema(name ="categories", description = "List of MessageCategory class objects.", type = "string",  	example="\"categories\":     { \"categoryLabel\": \"C1UX\",     \"categoryImageURL\": \"assets/images/Dollar-24px.svg\",\"messages\": [{ \"msgID\": \"3204\", \"title\": \"FAQ22\",\"effectiveDate\": \"02 Nov 2022\", \"content\": \"testing 2\" } ]  } ")
	protected ArrayList<MessageCategory> categories = new ArrayList<MessageCategory>();
	//CAP-38711
	@Schema(name ="translation", description = "Messages from \"messagecenter\" translation file will load here.", type = "string",  example="\"translation\": { \"messageCenterHeader\": \"Message Center\"}")
	private Map<String, String> translation;
	

	public ArrayList<MessageCategory> getCategories() {
		return categories;
	}
	public void setCategories(ArrayList<MessageCategory> categories) {
		this.categories = categories;
	}
	public Map<String, String> getTranslation() {
		return translation;
	}
	public void setTranslation(Map<String, String> translation) {
		this.translation = translation;
	}
}
