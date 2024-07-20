/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * RR Donnelley
 * You shall not disclose such confidential information.
 *
 *	Revisions:
 *	Date		Modified By		DTS#		Description
 *	--------	-----------		----------	-----------------------------------------------------------
 *  02/24/23	A Boomker		CAP-38912	Updated vendorCopyrightInfo and vendorTrademarkInfo1 per Karen Bach
 *  04/06/23	M Sakthi		CAP-39244   Change Footer API response handling to make and use new translation text values
 */
package com.rrd.c1ux.api.models.footer;

import com.rrd.c1ux.api.controllers.RouteConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="FooterResponse", description = "Response Class for Footer Response to load the Footer text", type = "object")
public class FooterResponse {
	@Schema(name ="cookiePolicyCurrentlyAccepted", description = "Display the Cookie Policy Currently Accepted", type = "string", example="Y/N")
	String cookiePolicyCurrentlyAccepted="N";
	@Schema(name ="cookiePolicyPopupHtml", description = "Display the Cookie Policy Popup Html", type = "string", example="<p>We use cookies to measure and improve the performance of this site, to personalize content, and to analyze the web traffic to this site. \r\n"
			+ "We also share information about your use of our site with our analytics partners. \r\n"
			+ "If you do not consent to this, we will only use the cookies that are strictly necessary for this site to function.\r\n"
			+ "You can change your preferences at any time by clicking on the Cookie Policy link in the footer of this website.\r\n"
			+ "<a id=\\\"cookiescript_readmore\\\" href=\\\"https://dev.custompoint.rrd.com/cp/system/cookies.cp?locale=\\\" target=\\\"_blank\\\">Read more</a></p>")
	String cookiePolicyPopupHtml;
	@Schema(name ="cookiePolicyConsentButtonText", description = "Display the Cookie Policy Consent Button Text", type = "string", example="I Agree")
	String cookiePolicyConsentButtonText;
	@Schema(name ="cookiePolicyDissentButtonText", description = "Display the Cookie Policy Dissent Button Text", type = "string", example="I Disagree")
	String cookiePolicyDissentButtonText;
	@Schema(name ="termsLinkText", description = "Display the Terms Link Text", type = "string", example="Terms of Use")
	String termsLinkText;
	@Schema(name ="termsFullText", description = "Display the Terms Full Text", type = "string", example="https://custompoint.rrd.com/xs2/global/disclaimers/termsofuse.htm")
	String termsFullText=RouteConstants.TERMS_FULL_TEXT_LINK;
	@Schema(name ="privacyLinkText", description = "Display the Privacy Link Text", type = "string", example="Privacy Policy")
	String privacyLinkText;
	@Schema(name ="privacyFullText", description = "Display the Privacy Full Text", type = "string", example="https://www.rrd.com/privacy-policy")
	String privacyFullText=RouteConstants.PRIVACY_FULL_TEXT_LINK;
	@Schema(name ="cookieLinkText", description = "Display the Cookie Link Text", type = "string", example="")
	String cookieLinkText="";
	@Schema(name ="cookieFullText", description = "Display the Cookie Full Text", type = "string", example="")
	String cookieFullText="";
	@Schema(name ="wcagLinkText", description = "Display the wcag Link Text", type = "string", example="Accessibility Statement")
	String wcagLinkText;
	@Schema(name ="wcagFullText", description = "Display the wcag Full Text", type = "string", example="We are committed to ensuring that our website is accessible to people with disabilities,\r\n"
			+ "to the extent possible, and we continue to update the content on our website to meet Web Content Accessibility \r\n"
			+ "Guidelines (WCAG) 2.0, Level AA conformance. Please be aware that our efforts are ongoing.\r\n"
			+ "Any issues should be reported by sending an email \r\n"
			+ "to <a href=\\\"mailto:rrdonnelley.webmaster@rrd.com\\\" target=\\\"_blank\\\">rrdonnelley.webmaster@rrd.com</a>.")
	String wcagFullText;
	@Schema(name ="showVendorReferences", description = "Display the Show Vendor References", type = "string", example="Y/N")
	String showVendorReferences="N";
	@Schema(name ="vendorCopyrightInfo", description = "Display the Vendor Copy Right Info", type = "string", example="&copy; 2023 R.R. Donnelley & Sons Company, all rights reserved.")
	String vendorCopyrightInfo;
	@Schema(name ="vendorTrademarkInfo1", description = "Display the Vendor Trademark Info1", type = "string", example="RR DONNELLEY, RRD, RRD (Stylized) and ConnectOne Storefront are trademarks or registered trademarks of")
	String vendorTrademarkInfo1;
	@Schema(name ="vendorTrademarkInfo2", description = "Display the Vendor Trademark Info2", type = "string", example="R. R. Donnelley & Sons Company. All other trademarks are the property of R. R. Donnelley or their respective owners.")
	String vendorTrademarkInfo2;
	@Schema(name ="vendorPhone", description = "Display the Vendor Phone", type = "string", example="1.800.782.4892")
	String vendorPhone;
	@Schema(name ="vendorAddressLine1", description = "Display the Vendor Address Line1", type = "string", example="35 West Wacker Drive")
	String vendorAddressLine1;
	@Schema(name ="vendorAddressLine2", description = "Display the Vendor Address Line2", type = "string", example="Chicago, Illinois 60601")
	String vendorAddressLine2;


}
