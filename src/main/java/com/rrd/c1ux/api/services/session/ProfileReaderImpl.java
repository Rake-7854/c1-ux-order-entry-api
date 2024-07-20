package com.rrd.c1ux.api.services.session;

import java.util.HashMap;
import java.util.Map;

import com.rrd.c1ux.api.exceptions.ErrorCodeHandler;
import com.rrd.c1ux.api.services.locators.IAdminComponentLocator;
import com.wallace.atwinxs.admin.vo.BusinessUnitVOKey;
import com.wallace.atwinxs.admin.vo.LoginVO;
import com.wallace.atwinxs.admin.vo.ProfileVO;
import com.wallace.atwinxs.framework.util.AtWinXSException;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.ErrorCodeConstants;
import com.wallace.atwinxs.interfaces.IProfileInterface;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ProfileReaderImpl implements ProfileReader {

    private static final String FIELD_NAME_PROFILE_ID = "Profile ID";
    protected static final int MAX_LENGTH_PROFILE_ID = 128;

    private TokenReader mTokenReader;
    private IAdminComponentLocator mAdminComponentLocator;

    public ProfileReaderImpl(
        TokenReader tokenReader,
        IAdminComponentLocator adminComponentLocator
    ) {
        super();
        mTokenReader = tokenReader;
        mAdminComponentLocator = adminComponentLocator;
    }

    @Override
    public ProfileVO getProfileVO(LoginVO loginVO, String profileID) throws AtWinXSException {
        // Set the error code and proifle vo to null
        ErrorCode errorCode = null;
        ProfileVO profileVO = null;
        
        // Get profile interface here
        IProfileInterface iProfile = mAdminComponentLocator.locateProfileComponent(mTokenReader.getToken());

        // Put an error check in for length
        if ((profileID != null) && (profileID.length() > MAX_LENGTH_PROFILE_ID))
        {
            // Create an error message with the appropriate error code.
            Map<String, Object> replaceMap = new HashMap<>();
            replaceMap.put(ErrorCodeConstants.REP_TAG_FIELD_NAME, FIELD_NAME_PROFILE_ID);
            replaceMap.put(ErrorCodeConstants.REP_TAG_CHAR_LIMIT, Integer.toString(MAX_LENGTH_PROFILE_ID));
            errorCode = new ErrorCode(ErrorCodeConstants.CTX_GLOBAL_ERROR, 
                                        ErrorCodeConstants.ERR_NAME_CHAR_LIMIT,
                                        replaceMap);
        }
        
        if (errorCode == null)
        {
            // Check the login to see if the user is shared - if so, we need to use the profileID to get the profile vo.
			if (loginVO.isSharedID()) {
				// For a shared user, the profile ID cannot be blank.
				if (!StringUtils.isBlank(profileID)) {
					profileVO = iProfile.getProfileByProfileID(
							new BusinessUnitVOKey(loginVO.getSiteID(), loginVO.getBusinessUnitID()), profileID);
				}
			}
            else
            {
                profileVO = iProfile.getProfileByLoginID(loginVO.getSiteID(), loginVO.getLoginID(), loginVO.getBusinessUnitID());
            }
        }
        
        // Check the profileVO - if null, we need to creat an error
        // If the error code isn't null, throw an error here
        if (errorCode != null)
        {
            ErrorCodeHandler.throwFromErrorCode(errorCode, getClass());
        }
        
        // Return the profileVO here
        return profileVO;
    }
}
