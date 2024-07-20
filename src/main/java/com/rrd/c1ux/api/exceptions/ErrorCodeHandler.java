package com.rrd.c1ux.api.exceptions;

import com.wallace.atwinxs.framework.util.AtWinXSMsgException;
import com.wallace.atwinxs.framework.util.ErrorCode;
import com.wallace.atwinxs.framework.util.Message;

public class ErrorCodeHandler {
    
    private ErrorCodeHandler() {
        // private to ensure only static method calls
    }

    /***
     * This method will handle the actual creation of a new AtWinXSMsgException from the passed in
     * ErrorCode object.
     * @param errorCode - The ErrorCode object containing the error information.
     * @throws AtWinXSMsgException - Always throws an AtWinXSMsgException encapsulating the ErrorCode
     * object.
     */
    public static void throwFromErrorCode(ErrorCode errorCode, Class<?> classz) throws AtWinXSMsgException
    {
        Message errorMessage = new Message();
        errorMessage.setErrorCode(errorCode);
        
        throw new AtWinXSMsgException(errorMessage, classz.getName());
    }
}
