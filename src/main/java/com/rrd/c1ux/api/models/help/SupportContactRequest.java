/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  09/28/22	Satish kumar A	CAP-35430- support page 	Initial creation, Send email with support page form data
 */

package com.rrd.c1ux.api.models.help;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportContactRequest {

	private String userName = "";
	private String userEmail = "";
	private String userPhone = "";
	private String messageSubject = "";
	private String messageText = "";
	private String messageSendTo = "";

}
