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
 *  09/27/22    S Ramachandran  CAP-35439   Punchout Transfer Cart validation
 *	09/30/2022 	Sumit kumar		CAP-35440  	Get and compose punchout transfer cart URL
 *	10/12/22	A Boomker		CAP-36437	Remove "redirect/" prefix
 */

package com.rrd.c1ux.api.services.punchout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rrd.c1ux.api.controllers.RouteConstants;
import com.rrd.c1ux.api.models.shoppingcart.COShoppingCartResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AppProperties;
import com.wallace.atwinxs.framework.util.AppSessionBean;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.PunchoutSessionBean;
import com.wallace.atwinxs.framework.util.Util;
import com.wallace.atwinxs.orderentry.ao.OEAssemblerFactory;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartAssembler;
import com.wallace.atwinxs.orderentry.ao.OEShoppingCartFormBean;
import com.wallace.atwinxs.framework.session.ApplicationSession;
import com.wallace.atwinxs.orderentry.util.PunchoutUtil;

@Service
public class PunchoutServiceImpl implements PunchoutService {

	private final Logger logger = LoggerFactory.getLogger(PunchoutServiceImpl.class);

	// CAP-35439 - get update cart items error message status & punchout order items
	// error validation status
	public COShoppingCartResponse validatePunchoutTransferCart(SessionContainer sc,
			COShoppingCartResponse objCOShoppingCartResponse) throws AtWinXSException {

		try {

			AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
			PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
			OEShoppingCartFormBean formBean = objCOShoppingCartResponse.getOeShoppingCartFormBean();

			OEShoppingCartAssembler assembler = OEAssemblerFactory.getShoppingCartAssembler(
					appSessionBean.getCustomToken(), appSessionBean.getDefaultLocale(),
					appSessionBean.getApplyExchangeRate());

			if (Util.yToBool(objCOShoppingCartResponse.getUpdateCartNoError())) {

				// CAP-35439 - get punchout order items error validation status
				boolean punchoutTransferCartValidation = assembler.validatePunchoutItems(formBean, punchoutSessionBean,
						appSessionBean);
				objCOShoppingCartResponse
						.setPunchoutTransferCartValidationSuccess(Util.boolToY(punchoutTransferCartValidation));

				if (punchoutTransferCartValidation)
					objCOShoppingCartResponse.setStatusMessage(RouteConstants.TRANSFER_CART_VALIDATION_NOERROR);
				else
					objCOShoppingCartResponse.setStatusMessage(RouteConstants.TRANSFER_CART_VALIDATION_ERROR);
			}
			objCOShoppingCartResponse.setStatus(RouteConstants.REST_RESPONSE_SUCCESS);

		} catch (AtWinXSException e) {

			logger.error(e.getMessage());
			objCOShoppingCartResponse.setStatus(RouteConstants.REST_RESPONSE_FAIL);
			objCOShoppingCartResponse.setStatusMessage(RouteConstants.TRANSFER_CART_REST_MESSAGE_FAIL);
		}

		return objCOShoppingCartResponse;
	}

	/**
	 * @param sc - {@link SessionContainer}
	 * @return -punchout transfer Cart URL{@link String}
	 * @throws AtWinXSException
	 */

	// CAP-35440 Service to get the Transfer Punchout Cart Url
	@Override
	public String transferPunchoutCart(SessionContainer sc) throws AtWinXSException {

		AppSessionBean appSessionBean = sc.getApplicationSession().getAppSessionBean();
		PunchoutSessionBean punchoutSessionBean = sc.getApplicationSession().getPunchoutSessionBean();
		ApplicationSession applicationSsession = sc.getApplicationSession();

		String punchoutTransUrl = PunchoutUtil.transferPunchoutCart(appSessionBean, punchoutSessionBean,
				applicationSsession);
		punchoutTransUrl = "https://" + AppProperties.getServerName() + punchoutTransUrl;

		return punchoutTransUrl;

	}

}
