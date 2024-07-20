/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/15/22	Satish kumar A	CAP-35429- support page 	Initial creation
 *	11/11/22 	Krishna Natarajan	CAP-37102				default id set to -2
 */

package com.rrd.c1ux.api.models.help;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportContacts {

	private int id=-2;
	private String name;
}
