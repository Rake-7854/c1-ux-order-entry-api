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
 *  04/04/2023	A Boomker		CAP-39512					Added NO_ORDER_IN_PROGRESS_KEY
 *  05/25/2023	C Codina		CAP-39338					Added processMessage for CP TranslationText
 *  05/29/2023	N Caceres		CAP-39046					Create wrapper for TranslationTextTag.processMessage
 */
package com.rrd.c1ux.api.services.translation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

public interface TranslationService {
	// CAP-39512 - error to be used in possibly multiple services if no order is in session
	public static final String NO_ORDER_IN_PROGRESS_KEY = "sf.noOrderInProgressErr";
	/**
	 * Get the ResourceBundle for the given viewName and locale
	 *
	 * @param appSessionBean Has the locale and other session info
	 * @param viewName The view/bundle name to lookup
	 * @return The Resource bundle properties for the view/bundle name
	 */
	public Properties getResourceBundle(AppSessionBean appSessionBean, String viewName);


    /**
     * Convert ResourceBundle into a Map object.
     *
     * @param resource a resource bundle to convert.
     * @return Map a map version of the resource bundle.
     */
    public default Map<String, String> convertResourceBundlePropsToMap(Properties resourceBundleProps) {
        Map<String, String> map = new HashMap<>();

        if(null != resourceBundleProps) {
			map = resourceBundleProps.entrySet().stream().collect(
			Collectors.toMap(
				e -> String.valueOf(e.getKey()),
				e -> String.valueOf(e.getValue()),
				(prev, next) -> next, HashMap::new
			));
        }

        return map;
    }
    //CAP-39338: For CP TranslationText
    String processMessage(Locale locale, CustomizationToken token, String elementKey, Map<String, Object> replaceMap)throws AtWinXSException;
    
    // CAP-39046 Wrap static method for easier unit testing
    String processMessage(Locale locale, CustomizationToken token, String elementKey) throws AtWinXSException;
}
