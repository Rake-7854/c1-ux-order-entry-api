/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  12/23/2022	E Anderson		CAP-36154					Initial.  BE Updates for translation.
 *  01/10/2023  E Anderson      CAP-36154                   Derive the appNamePrefix from XST522.
 *  02/14/2023	A Salcedo		CAP-38173					Updated findByKey() for latest bundle.
 *  5/25/2023	C Codina		CAP-39338					Added processMessage for CP TranslationText
 *  05/29/2023	N Caceres		CAP-39046					Create wrapper for TranslationTextTag.processMessage
 */
package com.rrd.c1ux.api.services.translation;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.C1UXResourceBundleMessageSource;
import com.rrd.custompoint.admin.entity.SiteBUGroupProperty;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;
import com.wallace.atwinxs.framework.util.Util;

@Service
public class C1UXTranslationService implements TranslationService {
	
	@Value("${translation.basename.root}")
	private String translationBasenameRoot;

	@Autowired
	Map<String, ReloadableResourceBundleMessageSource> translationResourceBundles;
	
	@Override
	public Properties getResourceBundle(AppSessionBean appSessionBean, String baseName) {
		Properties resourceBundleProps = null;
		
		//CAP-36154
		//TODO: A solution is needed for the appNamePrefix.  Currently deriving from XST522.  This will eventually need to be derived from the appSession.
		int siteID = appSessionBean.isMasterSite() ? AtWinXSConstant.DEFAULT_SITE_ID : appSessionBean.getSiteID();
		SiteBUGroupProperty prop = this.findSiteBUGroupProp(siteID, -1, "", "global", "c1ux_appName_prefix");
		String appNamePrefix = (prop == null) ? "sf" : prop.getPropertyValue();		
		
		C1UXResourceBundleMessageSource rbms = (C1UXResourceBundleMessageSource)translationResourceBundles.get(appNamePrefix);
		if(null != rbms) {
			resourceBundleProps = rbms.getMergedBasenameProperties(translationBasenameRoot + appNamePrefix + "/", baseName, appSessionBean);
		}
		
		return resourceBundleProps;
	}
	
	private SiteBUGroupProperty findSiteBUGroupProp(int siteID, int buID, String groupName, String propertyType, String propertyKey) {		
		SiteBUGroupProperty sbgp = null;		
		
		if(siteID > 0 && buID > 0 && !Util.isBlankOrNull(groupName) && !Util.isBlankOrNull(propertyType) && !Util.isBlankOrNull(propertyKey)) {
			try {
				sbgp = ObjectMapFactory.getEntityObjectMap().getEntity(SiteBUGroupProperty.class, null);
				SiteBUGroupProperty newSbgp = sbgp.findByKey(siteID, buID, groupName, propertyType, propertyKey, AtWinXSConstant.EMPTY_STRING); //CAP-38173
				sbgp = newSbgp;
			} catch (AtWinXSException e) {
				//logger.error("findByKey threw exception", e);
			}
		}
		
		return sbgp;
	}
	//CAP-39338: For CP TranslationText
	@Override
	public String processMessage(Locale locale, CustomizationToken token, String elementKey, Map<String, Object> replaceMap)throws AtWinXSException{
		return TranslationTextTag.processMessage(locale, token, elementKey, replaceMap);
	}
	
	// CAP-39046 Wrap static method for easier unit testing 
	public String processMessage(Locale locale, CustomizationToken token, String elementKey) throws AtWinXSException {
		return TranslationTextTag.processMessage(locale, token, elementKey);
	}

	
}
