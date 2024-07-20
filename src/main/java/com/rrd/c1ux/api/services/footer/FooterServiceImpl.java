/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/14/22	Sumit kumar		CAP-35547					Create API service to return footer text, links, and text for those linked pages
 *  04/06/23	M Sakthi		CAP-39244   				Change Footer API response handling to make and use new translation text values
 */
package com.rrd.c1ux.api.services.footer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.footer.FooterResponse;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;

@Service
public class FooterServiceImpl implements FooterService {

	@Override
	public FooterResponse loadFooter(SessionContainer sc) throws AtWinXSException {

		Logger logger = LoggerFactory.getLogger(FooterServiceImpl.class);
		FooterResponse footerResponse = new FooterResponse();
		try {

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			boolean isShowVenderReference = appSessionBean.isShowWallaceReferences();
			//CAP-39244 Changes
			footerResponse.setCookiePolicyPopupHtml(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(),SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.COOKIE_POLICY_POPUP_HTML));
			footerResponse.setCookiePolicyConsentButtonText(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.COOKIE_POLICY_CONSENT));
			footerResponse.setCookiePolicyDissentButtonText(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.COOKIE_POLICY_DISSENT));
			footerResponse.setTermsLinkText(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.TERM_LINK_TEXT));
			footerResponse.setPrivacyLinkText(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.PRIVACY_POLICY_TEXT));
			footerResponse.setWcagLinkText(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.WCAG_LINK_TEXT));
			footerResponse.setWcagFullText(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.WCAG_FULL_TEXT));
			footerResponse.setVendorCopyrightInfo(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VENDOR_COPY_RIGHT_INFO));
			footerResponse.setVendorTrademarkInfo1(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VENDOR_TRADEMARK_INFO1));
			footerResponse.setVendorTrademarkInfo2(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VENDOR_TRADEMARK_INFO2));
			footerResponse.setVendorAddressLine1(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VENDOR_ADDRESS_LINE1));
			footerResponse.setVendorAddressLine2(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VENDOR_ADDRESS_LINE2));
			footerResponse.setVendorPhone(TranslationTextTag.processMessage(sc.getApplicationSession().getAppSessionBean().getDefaultLocale(), sc.getApplicationSession().getAppSessionBean().getCustomToken(), SFTranslationTextConstants.PREFIX_SF+SFTranslationTextConstants.VENDOR_PHONE));
			if (isShowVenderReference) {
				footerResponse.setShowVendorReferences("Y");
			}

		} catch (Exception exp) {
			logger.error(Util.class.getName() + " - " + exp.getMessage(), exp);

		}

		return footerResponse;
	}

}
