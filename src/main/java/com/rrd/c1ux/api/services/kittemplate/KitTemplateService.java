/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *	Date		Modified By			JIRA#					Description
 *	--------	-----------			----------------		--------------------------------
 *	06/07/24	N Caceres			CAP-50006				Initial Version
 *	06/10/24	L De Leon			CAP-49882				Added initKitTemplate() method
 *  06/11/24	Satishkumar A		CAP-50007				C1UX API - Create new api to remove component kit item from kit
 *  06/18/24	N Caceres			CAP-50186				Create new method for adding wild card component to kit template
 *  06/25/24	Satishkumar A		CAP-50308				C1UX API - Creation of service to reload KitSession when coming back to kit editor from search or custom docs
 *  06/27/24	M Sakthi			CAP-50330				C1UX BE - When adding components for a kit, we need to create an API to add the components to our order
 *  06/26/24	N Caceres			CAP-50309				Create new method for browsing catalog from Kit Editor
 *	06/26/24	L De Leon			CAP-50359				Added cancelKitTemplate() method
 *	07/03/24	Satishkumar A		CAP-50560				Added catalogSearchForKitTemplates() method
 *	07/04/24	C Codina			CAP-46486				C1UX API - Kit Template OE integrate with BE API to enter custom doc and redirect
 */
package com.rrd.c1ux.api.services.kittemplate;

import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateRequest;
import com.rrd.c1ux.api.models.kittemplate.InitKitTemplateResponse;
import com.rrd.c1ux.api.models.kittemplate.KitCatalogBrowseResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateAddToCartResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateCancelResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateEditCustomDocRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateEditCustomDocResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompRequest;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateRemoveCompResponse;
import com.rrd.c1ux.api.models.kittemplate.KitTemplateSearchResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface KitTemplateService {
	
	public KitTemplateAddCompResponse addKitComponent(SessionContainer sc, KitTemplateAddCompRequest kitTemplateAddCompRequest) throws AtWinXSException;
	
	//CAP-50007
	public KitTemplateRemoveCompResponse removeKitComponent(SessionContainer sc, KitTemplateRemoveCompRequest kitTemplateRemoveCompRequest) throws AtWinXSException;

	public InitKitTemplateResponse initKitTemplate(SessionContainer sc, InitKitTemplateRequest request) throws AtWinXSException; // CAP-49882
	
	// CAP-50186
	public KitTemplateAddCompResponse addWildCardComponent(SessionContainer sc, KitTemplateAddCompRequest request) throws AtWinXSException;
	
	//CAP-50308
	public InitKitTemplateResponse reloadKitTemplate(SessionContainer sc) throws AtWinXSException; 
	
	//CAP-50330
	public KitTemplateAddToCartResponse addToCartKitTemplate(SessionContainer sc, KitTemplateAddToCartRequest request) throws AtWinXSException;

	public KitCatalogBrowseResponse kitBrowseCatalog(SessionContainer sc, KitTemplateAddToCartRequest request) throws AtWinXSException; // CAP-50309

	// CAP-50359
	public KitTemplateCancelResponse cancelKitTemplate(SessionContainer sc) throws AtWinXSException;
	
	//CAP-50560
	public KitTemplateSearchResponse catalogSearchForKitTemplates(SessionContainer sc, KitTemplateAddToCartRequest request) throws AtWinXSException;
	
	//CAP-46486
	public KitTemplateEditCustomDocResponse getKitComponentIndex(SessionContainer sc, KitTemplateEditCustomDocRequest request) throws AtWinXSException;

}
