package com.rrd.c1ux.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="FakeRequest", description = "Demo class which just contains a flag to change what you get in the FakeResponse", type = "object")
public class FakeRequest {
	@Schema(name ="failThis", description = "Boolean value which if true will trigger a Bad Request. If false, you'll get back a success.", type = "object")
	private boolean failThis = false;
}
