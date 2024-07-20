/*
 *  Copyright (c)  RR Donnelley. All Rights Reserved.
 *  This software is the confidential and proprietary information of RR Donnelley.
 *  You shall not disclose such confidential information.
 *
 *  Revision
 *  Date		Modified By     JIRA#		Description
 *  ----------  -----------     ----------  -----------------------------------------
 *  05/16/2023  N Caceres		CAP-39045	Initial version
 *  06/01/23	A Boomker		CAP-40687	Making other object maps available here for easier junits
 */
package com.rrd.c1ux.api.services.factory;

import org.springframework.stereotype.Service;

import com.rrd.custompoint.framework.util.objectfactory.ComponentObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.DAOObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMap;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.rrd.custompoint.framework.util.objectfactory.impl.EntityObjectMap;

@Service
public class ObjectMapFactoryServiceImpl implements ObjectMapFactoryService {

	@Override
	public EntityObjectMap getEntityObjectMap() {
		return ObjectMapFactory.getEntityObjectMap();
	}

	@Override
	public DAOObjectMap getDAOObjectMap() {
		return ObjectMapFactory.getDAOObjectMap();
	}

	@Override
	public ComponentObjectMap getComponentObjectMap() {
		return ObjectMapFactory.getComponentObjectMap();
	}

	@Override
	public ObjectMap getGenericObjectMap() {
		return ObjectMapFactory.getGenericObjectMap();
	}

}
