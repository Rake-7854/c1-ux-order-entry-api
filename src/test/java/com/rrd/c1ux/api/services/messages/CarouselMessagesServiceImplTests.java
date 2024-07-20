/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date					Modified By				JIRA#						Description
 * 	--------				-----------				-----------------------		--------------------------------
 *	11/03/23				C Codina				CAP-44742					Initial Version
*/
package com.rrd.c1ux.api.services.messages;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.rrd.c1ux.api.BaseOEServiceTest;
import com.rrd.c1ux.api.exceptions.AccessForbiddenException;
import com.rrd.c1ux.api.models.messages.CarouselItem;
import com.rrd.c1ux.api.models.messages.CarouselResponse;
import com.rrd.custompoint.admin.entity.Messages;
import com.rrd.custompoint.admin.entity.PluggableWidgetAssignment;
import com.rrd.custompoint.admin.entity.PluggableWidgetAssignmentImpl;
import com.rrd.custompoint.admin.entity.UserGroup;
import com.wallace.atwinxs.admin.vo.UserGroupVOKey;
import com.wallace.atwinxs.framework.util.CustomizationFactory.CustomizationToken;

class CarouselMessagesServiceImplTests extends BaseOEServiceTest {


	private CarouselResponse carouselResponse;

	@Mock
	private UserGroup mockUserGroup;

	@Mock
	private Messages mockMessage;

	@Mock
	private PluggableWidgetAssignment mockWidget;

	@Mock
	private UserGroupVOKey mockUgVoKey;

	@Mock
	CarouselItem mockCarouselItem;

	@InjectMocks
	private CarouselMessagesServiceImpl serviceToTest;

	@Test
	void that_getCarouselMessage_success() throws Exception{
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserGroup);

		when(mockAppSessionBean.getBuID()).thenReturn(1234);

		PluggableWidgetAssignment widget = new PluggableWidgetAssignmentImpl();
		widget.setWidgetName("pw_msg_carousel");
		Collection<PluggableWidgetAssignment> getHomePageWidgets = new ArrayList<>();
		getHomePageWidgets.add(widget);

		when(mockUserGroup.getHomePageWidgets()).thenReturn(getHomePageWidgets);
		when(mockUserGroup.getGeneralMessages()).thenReturn(mockMessage);

		carouselResponse = serviceToTest.getCarouselMessages(mockSessionContainer);
		assertNotNull(carouselResponse);
		assertTrue(carouselResponse.isSuccess());

	}

	@Test
	void that_getCarouselMessage_fails() throws Exception {
		when(mockSessionContainer.getApplicationSession()).thenReturn(mockApplicationSession);
		when(mockApplicationSession.getAppSessionBean()).thenReturn(mockAppSessionBean);
		when(mockObjectMapFactoryService.getEntityObjectMap()).thenReturn(mockEntityObjectMap);
		when(mockAppSessionBean.getCustomToken()).thenReturn(mockToken);
		when(mockEntityObjectMap.getEntity(any(), isA(CustomizationToken.class))).thenReturn(mockUserGroup);

		when(mockAppSessionBean.getBuID()).thenReturn(1234);

		when(mockUserGroup.getHomePageWidgets()).thenReturn(null);
		Exception exception = assertThrows(AccessForbiddenException.class, () ->{
			serviceToTest.getCarouselMessages(mockSessionContainer);
		});
		assertTrue(exception.getMessage().contains(EXPECTED_403MESSAGE));


	}
}


