package com.rrd.c1ux.api.services.footer;

import com.rrd.c1ux.api.models.footer.FooterResponse;
import com.wallace.atwinxs.framework.session.SessionContainer;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface FooterService {
	public FooterResponse loadFooter(SessionContainer sc) throws AtWinXSException;

}
