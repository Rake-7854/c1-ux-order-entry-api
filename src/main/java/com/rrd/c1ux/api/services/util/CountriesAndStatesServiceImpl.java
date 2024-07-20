/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date        Created By         DTS#            Description
 *	--------    -----------         ----------      -----------------------------------------------------------
 * 	07/11/23	Satishkumar A      CAP-41970		C1UX BE - Self Admin/PAB – Get State/Country List for Address entry (API Build)
 *  08/09/23	Krishna Natarajan	CAP-42803		Changed the translation from tag process to service
 */
package com.rrd.c1ux.api.services.util;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.models.util.CountriesAndStatesResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.gwt.common.util.CountryBean;
import com.rrd.custompoint.gwt.common.util.NameValuePair;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;

@Service
public class CountriesAndStatesServiceImpl extends BaseOEService implements CountriesAndStatesService {

	protected CountriesAndStatesServiceImpl(TranslationService translationService) {
		super(translationService);
		
	}
	
	//CAP-41970
	public CountriesAndStatesResponse getCountriesAndStatesOrProvincesList(SessionContainer sc) throws AtWinXSException {
		
		CountriesAndStatesResponse response = new CountriesAndStatesResponse();
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		try {
		
		List<NameValuePair<CountryBean>> countries = getGeographicLabelsAndStateList(appSessionBean);
		
		response.setCountriesAndStates(countries);
		response.setSuccess(true);
				
		} catch (Exception e ) {
			response.setSuccess(false);
			response.setMessage(translationService.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), "sf.requestFailed"));
		}
		return response;
		
	}

}
