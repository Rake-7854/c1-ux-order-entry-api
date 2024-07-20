package com.rrd.c1ux.api.services.session;

import java.util.HashMap;
import java.util.Map;

import com.rrd.c1ux.api.exceptions.ErrorCodeHandler;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.wallace.atwinxs.admin.vo.SiteVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.interfaces.ISite;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SiteReaderImpl implements SiteReader {

    private static final String FIELD_NAME_ACCOUNT = "Account";
    protected static final int MAX_LENGTH_ACCOUNT = 10;

    private TokenReader mTokenReader;
    private IAdminComponentLocator mAdminComponentLocator;

    public SiteReaderImpl(
        TokenReader tokenReader,
        IAdminComponentLocator adminComponentLocator
    ) {
        super();
        mTokenReader = tokenReader;
        mAdminComponentLocator = adminComponentLocator;
    }

    @Override
    public SiteVO getSiteForAccount(String account) throws AtWinXSException {

        ErrorCode errorCode = null;
        SiteVO siteVO = null;
        
        // Get site interface here
        ISite iSite = mAdminComponentLocator.locateSiteComponent(mTokenReader.getToken());
        
        // Check the see if the siteLoginID is not blank or null
        if (StringUtils.isBlank(account))
        {
            // Create an error message with the appropriate error code.
            Map<String, Object> replaceMap = new HashMap<>();
            replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_ACCOUNT);
            errorCode = new ErrorCode(ErrorCodeConstants.CTX_GLOBAL, 
                                        ErrorCodeConstants.ERR_NAME_NO_BLANKS,
                                        replaceMap);
        }
        else
        {
            // Put an error check in for length - account cannot be greater than 10
            if (account.length() > MAX_LENGTH_ACCOUNT)
            {
                // Create an error message with the appropriate error code.
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_ACCOUNT);
                replaceMap.put(ErrorCodeConstants.REP_TAG_CHAR_LIMIT, Integer.toString(MAX_LENGTH_ACCOUNT));
                errorCode = new ErrorCode(ErrorCodeConstants.CTX_GLOBAL_ERROR, 
                                            ErrorCodeConstants.ERR_NAME_CHAR_LIMIT,
                                            replaceMap);
            }
            
            if (errorCode == null)
            {
                // Retrieve the site here.  The calling method will 
                siteVO = iSite.getSiteVOByAccount(account.toUpperCase());
        
                // If the site is null, throw an error indicating the site passed in is invalid
                if (siteVO == null)
                {
                    // Create an error message with the appropriate error code.
                    Map<String, Object> replaceMap = new HashMap<>();
                    replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_VALUE, account);
                    replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_ACCOUNT);
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
        
        // return the siteVO here
        return siteVO;
    }
    
}
