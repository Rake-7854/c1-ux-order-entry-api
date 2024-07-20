package com.rrd.c1ux.api.services.session;

import java.util.HashMap;
import java.util.Map;

import com.rrd.c1ux.api.exceptions.ErrorCodeHandler;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.LoginVOKey;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.interfaces.ILoginInterface;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LoginReaderImpl implements LoginReader {

    private static final String FIELD_NAME_USER_NAME = "User Name";
    protected static final int MAX_LENGTH_USER_NAME = 16;

    private TokenReader mTokenReader;
    private IAdminComponentLocator mAdminComponentLocator;

    public LoginReaderImpl(
        TokenReader tokenReader,
        IAdminComponentLocator adminComponentLocator
    ) {
        super();
        mTokenReader = tokenReader;
        mAdminComponentLocator = adminComponentLocator;
    }

    @Override
    public LoginVO getLoginVO(int siteID, String userName) throws AtWinXSException {
        // Set the error code and login vo to null
        ErrorCode errorCode = null;
        LoginVO loginVO = null;
        
        // Get Login interface
        ILoginInterface iLogin = mAdminComponentLocator.locateLoginComponent(mTokenReader.getToken());
        
        if (StringUtils.isBlank(userName))
        {
            // Create an error message with the appropriate error code.
            Map<String, Object> replaceMap = new HashMap<>();
            replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_USER_NAME);
            errorCode = new ErrorCode(ErrorCodeConstants.CTX_GLOBAL, 
                                        ErrorCodeConstants.ERR_NAME_NO_BLANKS,
                                        replaceMap);
        }
        else
        {
            // Put an error check in for length - account cannot be greater than 10
            if (userName.length() > MAX_LENGTH_USER_NAME)
            {
                // Create an error message with the appropriate error code.
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_USER_NAME);
                replaceMap.put(ErrorCodeConstants.REP_TAG_CHAR_LIMIT, Integer.toString(MAX_LENGTH_USER_NAME));
                errorCode = new ErrorCode(ErrorCodeConstants.CTX_GLOBAL_ERROR, 
                                            ErrorCodeConstants.ERR_NAME_CHAR_LIMIT,
                                            replaceMap);
            }
            
            if (errorCode == null)
            {			
                // Retrieve the login VO here
                loginVO = iLogin.getLogin(new LoginVOKey(siteID, userName.toUpperCase()));
                
                // If the loginVO is null, the user is invalid
                if (loginVO == null)
                {
                    // Create an error message with the appropriate error code.
                    Map<String, Object> replaceMap = new HashMap<>();
                    replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_VALUE, userName);
                    replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_USER_NAME);
                    errorCode = new ErrorCode(ErrorCodeConstants.CTX_GLOBAL_ERROR, 
                                                ErrorCodeConstants.ERR_NAME_INVALID_FIELD_VALUE,
                                                replaceMap);
                }
            }
        }
        
        // If the error code isn't null, throw an error here
        if (errorCode != null)
        {
            ErrorCodeHandler.throwFromErrorCode(errorCode, getClass());
        }
        
        // Return the loginVO here
        return loginVO;
    }
    
}
