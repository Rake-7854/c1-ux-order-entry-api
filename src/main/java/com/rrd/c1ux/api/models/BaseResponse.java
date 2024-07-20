/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 *  Date                    Modified By             JIRA#                       Description
 *  --------                -----------             -----------------------     --------------------------------
 *  03/13/23                C Porter                CAP-39179                   Refactor setSuccess for BaseResponse
 *	06/07/23				A Boomker				CAP-38154					Made json tostring
 *	03/01/24				S Ramachandran			CAP-47629					Added Warning Message
 */
package com.rrd.c1ux.api.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wallace.atwinxs.framework.util.AtWinXSConstant;
import com.wallace.atwinxs.framework.util.Util;

public abstract class BaseResponse {
  private boolean success=false;
  public final boolean isSuccess() {
      return success;
  }

  public void setSuccess(boolean value) {
    this.success = value;
  }

  private String message = AtWinXSConstant.EMPTY_STRING;
  public final void setMessage(String s) {
      message = s;
  }
  public final String getMessage() {
      return message;
  }
  
  private String warningMessage = AtWinXSConstant.EMPTY_STRING;
  public final void setWarningMessage(String s) {
	  warningMessage = s;
  }
  public final String getWarningMessage() {
      return warningMessage;
  }

  private Map<String, String> fieldMessages = new HashMap<>();
  public Map<String, String> getFieldMessages() {
      return fieldMessages;
  }

  public void setFieldMessages(Map<String, String> mess)
  {
      fieldMessages = mess;
  }
  public void setFieldMessage(String key, String mess)
  {
      if ((fieldMessages.containsKey(key)) && (!Util.isBlankOrNull(mess)))
      {
          fieldMessages.put(key, fieldMessages.get(key) + AtWinXSConstant.BLANK_SPACE + mess);
      }
      else if (!Util.isBlankOrNull(mess))
      {
          fieldMessages.put(key, mess);
      }
  }

  public String getFieldMessage(String key)
  {
      return (fieldMessages.containsKey(key)) ? fieldMessages.get(key) : AtWinXSConstant.EMPTY_STRING;
  }

  // CAP-38154 - made for debugging
  public String toString() {
	  try {
	      ObjectMapper mapper = new ObjectMapper();
	      mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	      ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	      return ow.writeValueAsString(this);
	  } catch(JsonProcessingException jpe)
	  {
		  return super.toString();
	  }
  }
}
