package com.rrd.c1ux.api.models.messages;

import com.rrd.c1ux.api.controllers.RouteConstants;

import io.swagger.v3.oas.annotations.media.Schema;

public class ShowMessageCenterResponse  {

	//CAP-39247
	@Schema(name ="showMessageCenter", description = "if the User group has \"pw_msg_board\" widget and either NonHighValueMessages or HighValueMessages not empty then 'Y' else 'N' ", type = "string",  example="N")
	private String showMessageCenter=RouteConstants.NO_FLAG;
	//CAP-39247
	@Schema(name ="showImportant", description = "if the User group has \"pw_msg_board\" widget and HighValueMessages not empty then 'Y' else 'N' ", type = "string",  example="N")
	private String showImportant=RouteConstants.NO_FLAG;

	
	public String getShowMessageCenter() {
		return showMessageCenter;
	}
	public void setShowMessageCenter(String showMessageCenter) {
		this.showMessageCenter = showMessageCenter;
	}
	public String getShowImportant() {
		return showImportant;
	}
	public void setShowImportant(String showImportant) {
		this.showImportant = showImportant;
	}

}
