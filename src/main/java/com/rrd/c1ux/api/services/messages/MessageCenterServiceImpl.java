/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	09/12/22	A Boomker		CAP-35436				    Adding service for returning messages for message center
 *  09/13/22 	Krishna Natarajan CAP-35708				    Adding service for returning messages for message flags 
 *  03/08/23 	Sumit Kumar		CAP-38711					Add Translation to ResponseObject for /api/messagecenter/getMessages
 *  04/27/23    Satishkumar A   CAP-39247          			API Change - Modify Message Center Response API to make/use new translation text values 
 */
package com.rrd.c1ux.api.services.messages;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.models.messages.MessageCategory;
import com.rrd.c1ux.api.models.messages.MessageCenterMessage;
import com.rrd.c1ux.api.models.messages.MessageCenterResponse;
import com.rrd.c1ux.api.models.messages.ShowMessageCenterResponse;
import com.rrd.c1ux.api.services.BaseService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.admin.entity.Message;
import com.rrd.custompoint.admin.entity.MessageCategoryImage;
import com.rrd.custompoint.admin.entity.PluggableWidgetAssignment;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.rrd.custompoint.framework.taglib.TranslationTextTag;
import com.rrd.custompoint.framework.util.objectfactory.ObjectMapFactory;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.session.VolatileSessionBean;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ProfileParserUtil;
import com.wallace.atwinxs.framework.util.Util;

@Service
public class MessageCenterServiceImpl extends BaseService implements MessageCenterService{

	protected MessageCenterServiceImpl(TranslationService translationService) {
		super(translationService);
	}

	@Override
	public ShowMessageCenterResponse getShowResponse(SessionContainer sc) throws AtWinXSException {

		ShowMessageCenterResponse showMesRes = new ShowMessageCenterResponse();
		UserGroup ug = ObjectMapFactory.getEntityObjectMap().getEntity(UserGroup.class,
				sc.getApplicationSession().getAppSessionBean().getCustomToken());
		ug.setKey(new UserGroupVOKey(sc.getApplicationSession().getAppSessionBean().getSiteID(),
				sc.getApplicationSession().getAppSessionBean().getBuID(),
				sc.getApplicationSession().getAppSessionBean().getGroupName()), false);
		Collection<PluggableWidgetAssignment> pwdg = ug.getHomePageWidgets();
		Collection<com.rrd.custompoint.admin.entity.Message> regMsg = ug.getNonHighValueMessages().asCollection();
		Collection<com.rrd.custompoint.admin.entity.Message> hvMsg = ug.getHighValueMessages().asCollection();

		for (PluggableWidgetAssignment loopthrough : pwdg) {
			if (RouteConstants.MESSAGE_BOARD_WIDGET_NAME.equals(loopthrough.getWidgetName())) {
				if (!regMsg.isEmpty()) {
					showMesRes.setShowMessageCenter(RouteConstants.YES_FLAG);
				}

				if (!hvMsg.isEmpty()) {
					showMesRes.setShowMessageCenter(RouteConstants.YES_FLAG);
					showMesRes.setShowImportant(RouteConstants.YES_FLAG);
				}
			}
		}

		return showMesRes;
	}
	
private static final Logger logger = LoggerFactory.getLogger(MessageCenterServiceImpl.class);
	
	public MessageCenterResponse getMessagesResponse(SessionContainer sc) throws AtWinXSException
	{
	    AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
	    VolatileSessionBean vsb = sc.getApplicationVolatileSession().getVolatileSessionBean();
		UserGroup ug = ObjectMapFactory.getEntityObjectMap().getEntity(UserGroup.class, appSessionBean.getCustomToken());
		MessageCenterResponse response = new MessageCenterResponse();
		try {
			ug.setKey(new UserGroupVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(), appSessionBean.getGroupName()), false);
			Collection<com.rrd.custompoint.admin.entity.Message> regMsg = ug.getNonHighValueMessages().asCollection();
			Collection<com.rrd.custompoint.admin.entity.Message> hvMsg = ug.getHighValueMessages().asCollection();
			MessageCenterMessage newMsg = null;
			ProfileParserUtil parser = new ProfileParserUtil();
			//CAP-40616 - date format bug 
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
			if ((hvMsg != null) && (!hvMsg.isEmpty()))
			{
				MessageCategory important = new MessageCategory(); // default to high-value
				//CAP-39247
				String importantLabel = Util.nullToEmpty(TranslationTextTag.processMessage(appSessionBean.getDefaultLocale(), appSessionBean.getCustomToken(), SFTranslationTextConstants.IMPORTANT_LABEL));
				important.setCategoryLabel(importantLabel);
				for (com.rrd.custompoint.admin.entity.Message hv : hvMsg)
				{
					newMsg = new MessageCenterMessage();
					populateMessage(hv, newMsg, parser, formatter, appSessionBean, vsb);
					important.getMessages().add(newMsg);
				}
				response.getCategories().add(important);
			}
			
			if ((regMsg != null) && (!regMsg.isEmpty()))
			{
				MessageCategory regular = null;
				for (com.rrd.custompoint.admin.entity.Message reg : regMsg)
				{
					if ((regular == null) || (!regular.getCategoryLabel().equals(reg.getMessageCategoryName())))
					{
						if (regular != null)
						{
							response.getCategories().add(regular);							
						}
						MessageCategoryImage img = ObjectMapFactory.getEntityObjectMap().getEntity(
								MessageCategoryImage.class, AtWinXSConstant.NO_CUSTOMIZATION_ALLOWED_TOKEN);
						img.populateForCategory(reg.getMessageCategoryID());

						// for non-high-value, need to pass in name and path
						regular = new MessageCategory(reg.getMessageCategoryName(), Util.nullToEmpty(img.getMessageCategoryImagePath())); 
					}
					newMsg = new MessageCenterMessage();
					populateMessage(reg, newMsg, parser, formatter, appSessionBean, vsb);
					regular.getMessages().add(newMsg);
				}
				
				// add the last category still in progress at the end of the loop
				if (regular != null)
				{
					response.getCategories().add(regular);							
				}
			}
		}  
		catch(AtWinXSException ae)
		{
			logger.error("Error loading message categories and messages: " + ae.getMessage(), ae);
		}
		
		//CAP-38711
		Properties messageCenterNavBundleProps = translationService.getResourceBundle(appSessionBean, "messagecenter");
		response.setTranslation(translationService.convertResourceBundlePropsToMap(messageCenterNavBundleProps));
		
		return response;
	}

	private void populateMessage(Message hv, MessageCenterMessage newMsg, ProfileParserUtil parser, SimpleDateFormat formatter, AppSessionBean appSessionBean, VolatileSessionBean vsb) 
	{
		newMsg.setMsgID(String.valueOf(hv.getMessageID()));
		newMsg.setTitle(hv.getMessageTitle());
		newMsg.setEffectiveDate(formatter.format(hv.getEffectiveDate()));
		newMsg.setContent(parser.parseHTML(RouteConstants.C1UX_CONTEXT_PATH, appSessionBean, vsb, hv.getMessageBody()));
	}

}
