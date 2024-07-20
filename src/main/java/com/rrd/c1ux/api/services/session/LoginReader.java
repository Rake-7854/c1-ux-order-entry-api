package com.rrd.c1ux.api.services.session;

import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface LoginReader {
    
    LoginVO getLoginVO(int siteID, String userName) throws AtWinXSException;
}
