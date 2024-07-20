/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	10/25/23				C Codina				CAP-44742					Initial Version
*/
package com.rrd.c1ux.api.services.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.SFTranslationTextConstants;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.messages.CarouselItem;
import com.rrd.c1ux.api.models.messages.CarouselResponse;
import com.rrd.c1ux.api.services.BaseOEService;
import com.rrd.c1ux.api.services.factory.ObjectMapFactoryService;
import com.rrd.c1ux.api.services.translation.TranslationService;
import com.rrd.custompoint.admin.entity.Message;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.Util;

@Service
public class CarouselMessagesServiceImpl extends BaseOEService implements CarouselMessagesService {

	private static final Logger logger = LoggerFactory.getLogger(CarouselMessagesServiceImpl.class);
	
	private static final String MSG_CAROUSEL_WIDGET_NAME = "pw_msg_carousel";

	protected CarouselMessagesServiceImpl(TranslationService translationService,
			ObjectMapFactoryService objectMapFactoryService) {
		super(translationService, objectMapFactoryService);
	}

	@Override
	public CarouselResponse getCarouselMessages(SessionContainer sc) throws AtWinXSException  {
		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		CarouselResponse response = new CarouselResponse();
		List<CarouselItem> carouselItems = new ArrayList<>();
		UserGroup ug = objectMapFactoryService.getEntityObjectMap().getEntity(UserGroup.class,
				appSessionBean.getCustomToken());
		ug.setKey(new UserGroupVOKey(appSessionBean.getSiteID(), appSessionBean.getBuID(),
				appSessionBean.getGroupName()), false);

		if (ug.getHomePageWidgets() == null || ug.getHomePageWidgets().isEmpty() ||  (ug.getHomePageWidgets().stream()
					.noneMatch(widgets -> widgets.getWidgetName().equals(MSG_CAROUSEL_WIDGET_NAME)))) {
				throw new AccessForbiddenException(this.getClass().getName());
			
		}
		try {

			Collection<Message> msgCol = ug.getGeneralMessages().asCollection();
			for (Message msg : msgCol) {
				CarouselItem carouselItem = new CarouselItem();
				carouselItem.setMsgTitle(msg.getMessageTitle());
				carouselItem.setMsgEffDate(
						Util.getDateStringFromTimestamp(msg.getEffectiveDate(), appSessionBean.getDefaultLocale()));
				carouselItem.setMsgContent(msg.getMessageBody());

				carouselItems.add(carouselItem);

			}
			response.setSuccess(true);
			response.setCarouselItem(carouselItems);
		} catch (AtWinXSException e) {
			logger.error("CarouselMessages - geCarousellMessages() failed to retrieve carouselMessages", e);
			response.setSuccess(false);
			response.setMessage(getTranslation(appSessionBean, SFTranslationTextConstants.CAROUSEL_MESSAGE_ERR_MSG_LBL, 
					SFTranslationTextConstants.CAROUSEL_MESSAGE_ERR_MSG));
		}
		return response;
	}
}
