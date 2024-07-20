package com.rrd.c1ux.api.models.orders.ordersearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="GenericEncodedRequest", description = "Request Class to view specfic Email/Order file in HTML for a encoded parameter retrieved"
		+ "against each email in the API '/api/orders/getorderfilecontent'", type = "object")
public class GenericEncodedRequest {
	@Schema(name ="a", description = "Encoded(BASE64 ascii) value from parameter 'a' for a specfic email", type = "String", example="p8nGvN...")
	private String a;
}
