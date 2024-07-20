package com.rrd.c1ux.api.services.session;

import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface SiteReader {

    SiteVO getSiteForAccount(String account) throws AtWinXSException;
}
