/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 *  Revisions: 
 * 	Date		Modified By		Jira						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *  12/23/2022	E Anderson		CAP-36154					Initial.  BE Updates for translation.
 */
package com.rrd.c1ux.api.config;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.rrd.c1ux.api.C1UXResourceBundleMessageSource;
import com.rrd.custompoint.framework.entity.TranslationText;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;

@Configuration
public class TranslationResourcesConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranslationResourcesConfig.class);
			
	@Value("${translation.files.root}")
	private String translationFilesRoot;
	
	@Value("${translation.basename.root}")
	private String translationBasenameRoot;

	private Map<String, ReloadableResourceBundleMessageSource> resourceBundles = new HashMap<>();
	
	@PostConstruct
	private void init() {
		String[] subDirectories = getSubDirectories();
		if(null != subDirectories && subDirectories.length > 0) {
			for(String dir: subDirectories) {
				//Find all files without an underscore
				DirectoryStream<Path> ds = null;
				try {
					ds = Files.newDirectoryStream(Paths.get(translationFilesRoot + File.separator + dir));
					//Get the baseNames
					Set<String> baseNames = getBaseNames(ds, dir);
					//Create the ResourceBundle
					createResourceBundles(dir, baseNames);
				} catch (IOException e) {
					LOGGER.error("Error creating ResourceBundle for dir:" + dir, e);
				} finally {
					try {
						if(null != ds) {
							ds.close();
						}
					} catch (IOException e) {
						LOGGER.error("Error closing DirectoryStream for dir:" + dir, e);
					}
				}
			}
		}
	}
	
	private Set<String> getBaseNames(DirectoryStream<Path> ds, String dir) {
		Set<String> baseNames = new HashSet<>();
		
		ds.forEach(new Consumer<Path>() {
			@Override
			public void accept(Path p) {
				if(!p.toString().contains("_")) {
					String fileNameOnly = FilenameUtils.getBaseName(p.toFile().getName());
					baseNames.add(translationBasenameRoot + dir + "/" + fileNameOnly);
				}
			}
		});

		return baseNames;
	}
	
	private void createResourceBundles(String dir, Set<String> baseNames) {
		if(null != baseNames && baseNames.size() > 0) {
			TranslationText transText = ObjectMapFactory.getEntityObjectMap().getEntity(TranslationText.class, null);
			ReloadableResourceBundleMessageSource rb = new C1UXResourceBundleMessageSource(transText);
			rb.setBasenames(baseNames.toArray(new String[baseNames.size()]));
			rb.setDefaultEncoding("UTF-8");
			rb.setUseCodeAsDefaultMessage(true);
			resourceBundles.put(dir, rb);
		} else {
			LOGGER.error("No baseNames found for dir:" + dir);			
		}
	}
	
	private String[] getSubDirectories() {
		File file = new File(translationFilesRoot);
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		return directories;
	}
	
	@Bean(name = "translationResourceBundles")
	public Map<String, ReloadableResourceBundleMessageSource> getTranslationResourceBundle() {
		return resourceBundles;
	}
	  
}
