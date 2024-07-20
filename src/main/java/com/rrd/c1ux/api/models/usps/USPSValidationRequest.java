package com.rrd.c1ux.api.models.usps;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "USPSValidationRequest", description = "Request Class of US Address for USPS Validation", type = "object")
public class USPSValidationRequest {

	@Schema(name = "address1", description = "Address1", type = "string", example = "JosephR k")
	@Size(min=0, max=35)
	private String address1;

	@Schema(name = "address2", description = "Address2", type = "string", example = "Biden")
	@Size(min=0, max=35)
	private String address2;

	@Schema(name = "city", description = "City", type = "string", example = "Chicago")
	@Size(min=0, max=30)
	private String city;

	@Schema(name = "state", description = "Uppercased State Code", type = "string", example = "AL")
	@Size(min=0, max=4)
	private String state;

	@Schema(name = "zip", description = "Zip", type = "string", example = "123245")
	@Size(min=0, max=12)
	private String zip;
	
	@Schema(name = "country", description = "Uppercased Country Code", type = "String", example = "USA")
	@Size(min = 0, max = 3)
	private String country; 
}


