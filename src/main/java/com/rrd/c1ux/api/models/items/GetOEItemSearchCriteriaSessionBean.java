package com.rrd.c1ux.api.models.items;

import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.orderentry.session.OEItemSearchCriteriaSessionBean;

/**
 * @author Krishna Natarajan
 *
 */
public class GetOEItemSearchCriteriaSessionBean {

	/**
	 * The method to set search criteria from session beans
	 * @param appSessBean {@link AppSessionBean}
	 * @return searchCriteria {@link OEItemSearchCriteriaSessionBean}
	 */
	public OEItemSearchCriteriaSessionBean getOEItemSearchCriteriaSessionBean(AppSessionBean appSessBean) {
		OEItemSearchCriteriaSessionBean searchCriteria = new OEItemSearchCriteriaSessionBean();
		searchCriteria.setBuID(appSessBean.getBuID());
		searchCriteria.setLoginID(appSessBean.getLoginID());
		searchCriteria.setSiteID(appSessBean.getSiteID());
		searchCriteria.setProfileNr(appSessBean.getProfileNumber());
		searchCriteria.setFaveItemsOnly(true);
		searchCriteria.setAdvancedSearch(false);
		searchCriteria.setAliasSetting("N");
		searchCriteria.setAlternateCatalogDescInd(false);
		searchCriteria.setAttributesCriteria(null);
		searchCriteria.setAttributesProfileCriteria(null);
		searchCriteria.setBCategoryOnly(false);
		searchCriteria.setBContainsOnlyBu(false);
		searchCriteria.setCanSearchOutsideCatalog(false);
		searchCriteria.setCanSearchStockItems(false);
		searchCriteria.setCatalogName("");
		searchCriteria.setContainsForDescription(true);
		searchCriteria.setContainsForItemNumber(false);
		searchCriteria.setCorporateNum("");
		searchCriteria.setCustomerItemAliases(null);
		searchCriteria.setDescription("");
		searchCriteria.setDisplayUnassigned(true);
		searchCriteria.setDynamicItemAttributeList(null);
		searchCriteria.getDynamicItemAttributeListMap().put("", null);
		searchCriteria.setFeaturedItemnames(null);
		searchCriteria.setFeaturedItemsOnly(false);
		searchCriteria.getFeaturedItemsSearch().put("", null);
		searchCriteria.setFeaturedItemTypes("");
		searchCriteria.setBrowse(false);
		searchCriteria.setIncludeViewOnlyOnSearch(true);
		searchCriteria.setIsQuickItemEntry(false);
		searchCriteria.setValidAttributeUdfSearch(false);
		searchCriteria.setItemFilters(null);
		searchCriteria.setItemNumber("");
		searchCriteria.setItemType("");
		searchCriteria.setNameOption("");
		searchCriteria.setNewItemsDays(15);
		searchCriteria.setNewItemsOnly(false);
		searchCriteria.setOrderWizard(false);
		searchCriteria.setOriginalCustItemNum(null);
		searchCriteria.getPreviouslySelectedPeers().put(0, null);
		searchCriteria.setRepeatSearch(false);
		searchCriteria.setReplacementSearch(false);
		searchCriteria.setSearchAppliance(null);
		searchCriteria.setSearchOptions(null);
		searchCriteria.setSelectedCategoryId(-1);
		searchCriteria.setSelectedCategoryPeers(null);
		searchCriteria.setSortCode("");
		searchCriteria.setStandardAttributes(null);
		searchCriteria.setUdfCriteria(null);
		searchCriteria.setUnifiedSearch(true);
		searchCriteria.setUnifiedSearchCriteria("");
		searchCriteria.setUniversalSearchAutoComplete(false);
		searchCriteria.setUseFeaturedSearchOROperand(false);
		searchCriteria.setUserGroup(appSessBean.getGroupName());
		searchCriteria.setVItemNumber("");
		searchCriteria.setWizardTermSearch(null);
		return searchCriteria;
	}
}
