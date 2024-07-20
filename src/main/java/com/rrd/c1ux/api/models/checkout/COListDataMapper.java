package com.rrd.c1ux.api.models.checkout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class COListDataMapper {
	
	private Collection<COColumnNameWrapperCellData> headings;
	private Collection<COAltColumnNameWrapperCellData> altColumnNames;
	private Collection<ArrayList<String>> dataVector;
	private int rowCount = 0;
	private boolean isDisplayData;
	private Map<String,String> mappedBeans;
	private HashMap<String, String> distListSuggestedMessages = new HashMap<>();	

}
