package com.rrd.c1ux.api.models.alert;

import java.util.Collection;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlertsResponse", description = "Alert Response Object", type = "object")
public class AlertsResponse extends BaseResponse {
	
	@Schema(name ="alertsCategory", description = "Collection of Alert Category", type = "array")
	private Collection<AlertsCategory> alertsCategory;

}
