/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions: 
 * 	Date		Modified By		DTS#						Description
 * 	--------	-----------		-----------------------		--------------------------------
 *	CAP-35537	T Harmon									Changes for SAML
 *  10/25/22	A Boomker		CAP-36153					Add entry point
 */


package com.rrd.c1ux.api.services.security;

import java.util.ArrayList;
import java.util.List;

import com.rrd.c1ux.api.SecurityConfig;
import com.rrd.c1ux.api.models.users.UserContext;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.tt.arch.TTException;
import com.wallace.tt.arch.web.TTLoginException;
import com.wallace.tt.dao.TTSiteI;
import com.wallace.tt.dao.TTUserI;
import com.wallace.tt.utils.TTPasswordValidator;
import com.wallace.tt.vo.TTSession;
import com.wallace.tt.vo.TTSite;
import com.wallace.tt.vo.TTUser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * NOTE: this is for initial development purposes only!!! It is NOT complete!!
 * 
 * TODO: see com.wallace.tt.servlet.TTLoginServlet in CustomPoint's TTArch project for complete implementation.
 */
@Service
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    private static final String USER_ACCOUNT_SEPARATOR = ";";

    @Autowired
    private Environment env;

    public CustomAuthenticationProvider() {
        super();
    }

    @Autowired
    private TTUserI userI;

    @Autowired
    private TTSiteI siteI;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (SecurityConfig.isBasicAuth(env)) {

            return authenticateBasicAuth(authentication);
        }

        return authenticateUsingCustomPoint(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    /**
     * Parses username and account from the basic auth username.
     * @param basicUsername
     * @return
     */
    public static UserContext getUserContext(String basicUsername) {

        if (StringUtils.isBlank(basicUsername)) return new UserContext();

        int indexOfSep = basicUsername.indexOf(USER_ACCOUNT_SEPARATOR);

        if (indexOfSep < 0) return new UserContext(basicUsername, AtWinXSConstant.EMPTY_STRING, null, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING);  // CAP-35537, CAP-36153

        return new UserContext(
            basicUsername.substring(0, indexOfSep), 
            basicUsername.substring(indexOfSep + 1), 
            null,
            AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING, AtWinXSConstant.EMPTY_STRING);  // CAP-35537, CAP-36153
    }

    private boolean isUserStatusOk(TTUser userVO) {

        //TODO: check password expired, etc, here
        return userVO.isActive();
    }

    private Authentication authenticateUsingCustomPoint(Authentication authentication) {

        final String accountAndUsername = authentication.getName();
        final String password = authentication.getCredentials().toString();
        UserContext userContext = getUserContext(accountAndUsername);
        String account = userContext.getAccount();
        String username = userContext.getUserName(); 

        // validate username, account, password
        TTSite siteVO = new TTSite();
        TTUser userVO = new TTUser();

        // Set the session fields to the values entered (or passed in) by the user.
        // This way they are in the session record for auditing purposes.
        TTSession ttsession = new TTSession();
        ttsession.setUserid(username);
        ttsession.setSiteLogin(account);

        siteVO.setSiteLogin(account);
        try {
            siteVO = siteI.getTTSiteWithSession(siteVO, ttsession);
        }
        catch (TTException e) {
            logger.info("Exception calling siteI.getTTSiteWithSession() for account: {}.  Exception: {}", 
                account, e.getExplanation());
            return null;
        }

        //Update the session fields with the relevent site fields.....
        ttsession.setSite(siteVO.getId());

        // if we are here, the site was good, use it and the user entered to set the user VO...
        userVO.setId(username);
        userVO.setSite(siteVO.getId());

        // retrieve the user info...
        try {
            userVO = userI.getTTUser(userVO);
        } catch (TTLoginException e) {
            logger.info("Login exception calling siteI.getTTUser() for username: {}.  Exception: {}", 
                username, e.getExplanation());
            return null;
        } catch (TTException e) {
            logger.info("TT exception calling siteI.getTTUser() for username: {}.  Exception: {}", 
                username, e.getExplanation());
            return null;
        }

        // check user's status
        if (!isUserStatusOk(userVO)) {
            return null;
        }

        int pswdEncryptOpt = -1;
        String ttencryptseed = null;

        boolean passwordIsValid = 
            new TTPasswordValidator(
                pswdEncryptOpt, 
                password.trim(), 
                ttencryptseed, 
                userVO.getPassword(), 
                siteVO.isCaseInsensitiveLogin(),
                siteVO.getEncryptKey(),
                siteVO.getEncryptSalt(),
                userVO.getHashEncryptedPassword()).isPasswordValid();

        if (passwordIsValid) {

            final List<GrantedAuthority> grantedAuths = new ArrayList<>();
            grantedAuths.add(new SimpleGrantedAuthority(SecurityConfig.DEFAULT_ROLE));
            final UserDetails principal = new User(accountAndUsername, password, grantedAuths);

            return new UsernamePasswordAuthenticationToken(principal, password, grantedAuths);
        } else {
            return null;
        }
    }

    private Authentication authenticateBasicAuth(Authentication authentication) {

        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) return null;

        String validUsername = SecurityConfig.getBasicAuthUserName(env);
        String validPW = SecurityConfig.getBasicAuthPassword(env);

        if (username.trim().equalsIgnoreCase(validUsername)
            && password.equals(validPW)) {

            final List<GrantedAuthority> grantedAuths = new ArrayList<>();
            grantedAuths.add(new SimpleGrantedAuthority(SecurityConfig.DEFAULT_ROLE));
            final UserDetails principal = new User(username, password, grantedAuths);

            return new UsernamePasswordAuthenticationToken(principal, password, grantedAuths);
        }
        return null;
    }
    
}
