package com.rrd.c1ux.api.models.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class OrderWizardSelectedAttributes {
	
	@Schema(name ="attributeID", description = "The int holding the key attribute ID for the wizard", type = "Integer")
	int attributeID;
	@Schema(name ="attributeValueID", description = "The int holding the attribute value id for the wizard", type = "Integer")
	int attributeValueID;
	

}
