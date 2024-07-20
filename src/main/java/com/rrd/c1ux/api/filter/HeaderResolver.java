package com.rrd.c1ux.api.filter;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.rrd.custompoint.services.vo.KeyValuePair;
import com.wallace.atwinxs.framework.util.AtWinXSException;

public interface HeaderResolver {

	Optional<KeyValuePair> resolve(HttpServletRequest request) throws AtWinXSException;
	
}
