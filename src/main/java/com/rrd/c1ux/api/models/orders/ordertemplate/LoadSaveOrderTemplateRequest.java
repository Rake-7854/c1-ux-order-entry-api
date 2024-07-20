package com.rrd.c1ux.api.models.orders.ordertemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class LoadSaveOrderTemplateRequest {
	private String orderTemplateID;
}
