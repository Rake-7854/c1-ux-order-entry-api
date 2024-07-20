package com.rrd.c1ux.api.models.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemNameToGetItemDetailWithQuantityRequest {
	private String wallaceItemNumber;
}
