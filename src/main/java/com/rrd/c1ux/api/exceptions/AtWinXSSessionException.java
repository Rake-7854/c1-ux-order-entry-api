/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *  Date        Modified By     Issue #     Description
 *  --------    -----------     ----------  -----------------------------------------------------------
 *  10/28/22	C Porter 		CAP-36720	Initial.
 * 
 */
package com.rrd.c1ux.api.exceptions;

import com.wallace.atwinxs.framework.util.AtWinXSException;

public class AtWinXSSessionException extends AtWinXSException {

	private static final long serialVersionUID = -6394009747625356606L;

	public AtWinXSSessionException(Exception exception, String className) {
		super(exception.getMessage(), className);
	}

}
