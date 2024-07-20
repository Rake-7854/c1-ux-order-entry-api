/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  12/23/2022	E Anderson		CAP-36154					Initial.  BE Updates for translation.
 *  01/06/2023  E Anderson      CAP-36154                   Fix locale handling.
 *  06/15/2023  E Anderson      CAP-41262                   Fix variant handling.
 *  04/18/2024	T Harmon		CAP-48772					Fixed issue with business unit translation
 */
package com.rrd.c1ux.api;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.rrd.custompoint.framework.entity.TranslationText;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public class C1UXResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(C1UXResourceBundleMessageSource.class);
	
	private TranslationText transText;
		
	// Cache to hold already loaded properties per baseName and locale
	private final ConcurrentMap<String, HashMap<Locale, PropertiesHolder>> cachedMergedBasenameLocaleProperties = new ConcurrentHashMap<>();
	
	
    public C1UXResourceBundleMessageSource(TranslationText transText) {
    	this.transText = transText;
	}

    /**
     * Called from C1UXTranslationService for a getResourceBundle request
     * 
     * @param baseNameRoot The file system root
     * @param baseName The viewName
     * @param asb The AppSessionBean
     * @return A resourceBundle from the cache or freshly loaded for the given baseName(viewName) and AppSessionBean
     */
	public Properties getMergedBasenameProperties(String baseNameRoot, String baseName, AppSessionBean asb) {
        synchronized (cachedMergedBasenameLocaleProperties) {
        	Properties baseNameProps = null;
        	
        	//Is there a variant for the viewName
        	Integer variant = getVariant(asb, baseName);
        	
        	//If there is a variant, create a variant specific locale.  If there is no variant, the default will be returned.
        	Locale localeVariant = getLocaleWithVariant(asb, variant);
        	
        	//Is the locale specific baseName cached
            PropertiesHolder mergedHolder = (null == cachedMergedBasenameLocaleProperties.get(baseName)) ? null : cachedMergedBasenameLocaleProperties.get(baseName).get(localeVariant);
            if (mergedHolder != null) {
            	//locale specific baseName found in the cache
            	baseNameProps = mergedHolder.getProperties();
            } else {
            	//locale specific baseName not found in the cach.
            	//Generate the files for the cache then update the cache.
	            mergedHolder = genAllMergedPropertiesHolder(baseNameRoot, baseName, localeVariant);
	            updateCache(mergedHolder, baseName, localeVariant);
	            baseNameProps = mergedHolder.getProperties();
            }
        	
            return baseNameProps;
        }
    }
	
	/**
	 * Determine if there is a variant for the baseName
	 * 
	 * @param asb AppSessionBean
	 * @param baseName The viewName
	 * @return The variant if found, otherwise null.
	 */
	private Integer getVariant(AppSessionBean asb, String baseName) {
		Integer variant = null;
		
		try {
			//See if there is a variant for locale, site, ug, bu
			variant = transText.getLocaleVariantForViewName(asb.getDefaultLocale().toString(), asb.getSiteID(), asb.getBuID(), asb.getGroupName(), baseName);
			
			// CAP-48772 TH 
			if (null == variant)
			{
				variant = transText.getLocaleVariantForViewName(asb.getDefaultLocale().toString(), asb.getSiteID(), asb.getBuID(), "", baseName);
			}
			
			//None found.  See if there is a variant for locale and site
			if(null == variant) {
				variant = transText.getLocaleVariantForViewName(asb.getDefaultLocale().toString(), asb.getSiteID(), -1, "", baseName);				
			}
			
		} catch (AtWinXSException e) {
			LOGGER.info("Error getting Translation variant for locale:" + asb.getDefaultLocale() + " , baseName:" + baseName, e);
		}

		return variant;
	}
	
	/**
	 * Generate a variant specific locale
	 * 
	 * @param asb ApplicationSessionBean
	 * @param variant The variant for the baseName (viewName)
	 * @return
	 */
	private Locale getLocaleWithVariant(AppSessionBean asb, Integer variant) {
		Locale locale = asb.getDefaultLocale();
		
		if(null != variant) {
			try {
				//Use the found variant to create a variant specific locale
				locale = new Locale(asb.getDefaultLocale().getLanguage(), asb.getDefaultLocale().getCountry(), variant.toString());
			} catch (Exception e) {
				LOGGER.info("Unable to create locale for language:" + asb.getDefaultLocale() + " , country:" + asb.getDefaultLocale().getCountry() + " , variant:" + variant, e);
			}
		}
		
		return locale;
		
	}
	
	/**
	 * Updates the cache with the locale specific baseName(viewName) translations
	 * 
	 * @param mergedHolder All of the translations
	 * @param baseName The viewName
	 * @param locale The locale to strore the baseName with.
	 */
	private void updateCache(PropertiesHolder mergedHolder, String baseName, Locale locale) {
        //If the baseName doesn't exist
        if(null == cachedMergedBasenameLocaleProperties.get(baseName)) {
        	//Create the initial data structure that associates the calculated filename holder to a locale.
        	HashMap<Locale, PropertiesHolder> localeProps = new HashMap<>();
        	//Associate the calculated filename holder (incuding variants) with the locale.
        	localeProps.put(locale, mergedHolder);
        	
        	cachedMergedBasenameLocaleProperties.put(baseName, localeProps);
        } else {
        	//If the baseName exists but there is no locale, get the previously created HashMap<locale, PropertiesHolder>
        	//and associate the calculated filename holder (including variants) with the locale. 
            cachedMergedBasenameLocaleProperties.get(baseName).putIfAbsent(locale, mergedHolder);                	
        }                
	}
	
	/**
	 * Generates all of the filenames then loads them into a merged property holder
	 * 
	 * @param baseNameRoot The file system root where the translations reside
	 * @param baseName The viewName
	 * @param locale The locale used to find all of the files
	 * @return A merged holder of all the translations from the files that were found
	 */
	private PropertiesHolder genAllMergedPropertiesHolder(String baseNameRoot, String baseName, Locale locale) {
        Properties mergedProps = new Properties();
        PropertiesHolder mergedHolder = new PropertiesHolder(mergedProps, -1);
        
        //Generate the filenames for the base and default locale
        List<String> filenames = getCalculatedFiles(baseNameRoot, baseName, locale);
        
		//Load the properties into a PropertiesHolder
        filenames.sort(Comparator.comparingInt(String::length));
		for(String filename: filenames) {
            PropertiesHolder propHolder = getProperties(filename);
            if (propHolder.getProperties() != null) {
                mergedProps.putAll(propHolder.getProperties());
            }			
		}
    	
        return mergedHolder;
	}
	
	/**
	 * Generate the filenames for the base and default locale
	 * Note:  This was created to expose calculateAllFilenames for Unit Testing
	 * 
	 * @param baseNameRoot The file system root where the translations reside
	 * @param baseName The viewName
	 * @param locale The locale used to find all of the files
	 * @return A List of files
	 */
	protected List<String> getCalculatedFiles(String baseNameRoot, String baseName, Locale locale) {
		return calculateAllFilenames(baseNameRoot + baseName, locale);
	}
}
