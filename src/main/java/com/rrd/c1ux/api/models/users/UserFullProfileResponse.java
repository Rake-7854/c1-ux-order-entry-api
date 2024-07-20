package com.rrd.c1ux.api.models.users;

import com.rrd.c1ux.api.models.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="UserFullProfileResponse", description = "Response Class for User Full Profile", type = "object")
public class UserFullProfileResponse extends BaseResponse{
	@Schema(name ="c1uxProfile", description = "Display the Profile", type = "object")
	private C1UXProfile c1uxProfile;
	
}
