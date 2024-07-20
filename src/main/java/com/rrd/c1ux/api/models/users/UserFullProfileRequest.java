package com.rrd.c1ux.api.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFullProfileRequest {
	
	private int siteID;
	private int businessUnitID;
	private int profileNumber;

}	