package com.rrd.c1ux.api.services.session;

import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface ProfileReader {

    ProfileVO getProfileVO(LoginVO loginVO, String profileID) throws AtWinXSException;
}
