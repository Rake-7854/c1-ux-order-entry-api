package com.rrd.c1ux.api.models.kittemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentItemErrors {
	
	private int kitLineNumber;
	private String errorDescription;

}
