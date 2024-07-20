package com.rrd.c1ux.api.models.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "C1UXAddress", description = "Personal Address of C1UX", type = "object")
public class C1UXAddress {

	@Schema(name = "id", description = "ID for a Personal Address", type = "int", example = "16260")
	private int id;

	@Schema(name = "type", description = "Type of a Personal Address", type = "string", example = "p")
	private String type;

	@Schema(name = "shipToName1", description = "Ship To Name1 in a Personal Address", type = "string", example = "JosephR k")
	private String shipToName1;

	@Schema(name = "shipToName2", description = "Ship To Name2 in a Personal Address", type = "string", example = "Biden")
	private String shipToName2;

	@Schema(name = "shipToAttention", description = "Ship To Attention in a Personal Address", type = "string", example = "Biden")
	private String shipToAttention;

	@Schema(name = "address1", description = "Address1 in a Personal Address", type = "string", example = "35 W Wacker Dr")
	private String address1;

	@Schema(name = "address2", description = "Address2 in a Personal Address", type = "string", example = "35 W Wacker Dr")
	private String address2;

	@Schema(name = "address3", description = "Address3 in a Personal Address", type = "string", example = "35 W Wacker Dr")
	private String address3;

	@Schema(name = "city", description = "City in a Personal Address", type = "string", example = "Chicago")
	private String city;

	@Schema(name = "state", description = "State in a Personal Address", type = "string", example = "AL")
	private String state;

	@Schema(name = "zip", description = "Zip in a Personal Address", type = "string", example = "123245")
	private String zip;

	@Schema(name = "country", description = "Country in a Personal Address", type = "string", example = "USA")
	private String country;

	@Schema(name = "phone", description = "Phone Number in a Personal Address", type = "string", example = "1-800-588-2300")
	private String phone;

	@Schema(name = "defaultFlag", description = "Boolean flag inticating address is a default or not", type = "boolean", 
			allowableValues = {"false", "true" })
	private boolean defaultFlag;

}
