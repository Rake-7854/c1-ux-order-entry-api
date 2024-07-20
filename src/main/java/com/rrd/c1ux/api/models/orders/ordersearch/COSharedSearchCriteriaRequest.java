package com.rrd.c1ux.api.models.orders.ordersearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name ="COSharedSearchCriteriaRequest", description = "Class for search info for an individual search term being used", type = "object")
public class COSharedSearchCriteriaRequest {
	@Schema(name ="criteriaName", description = "Code indicating which search term was selected", type = "string", example="CriteriaSalesRef", allowableValues = {"CriteriaPONumber", "CriteriaSalesRef", "CriteriaStatusCode", "CriteriaItemNumber", "CriteriaOrderNumber"})
	String criteriaName;
	@Schema(name ="criteriaFieldValue", description = "Value to use for the selected search term", type = "string", example="80031326")
	String criteriaFieldValue;
	@Schema(name ="criteriaFieldModifier", description = "Modifier to use for the selected search term, only applicable for some criteria", type = "string", example=">=")
	String criteriaFieldModifier;//CAP-38497 correcting name of the field
}
