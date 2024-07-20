package com.rrd.c1ux.api.util;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import com.rrd.c1ux.api.controllers.RouteConstants;

public class ItemUOMAcronyms {
	/* CAP-34647 This method used to replace existing UOM Acronyms to full Acronyms description */
	public String getUOMAcronyms(String strDesc,boolean fullDesc) {
		Map<String,String> uomAcroMap=getFullTextforUomAcronyms();
		String acronymDesc="";
		for (Map.Entry<String,String> entry : uomAcroMap.entrySet()) {
			  if(strDesc.contains(entry.getKey())) {
				  acronymDesc= (fullDesc?strDesc.replaceFirst(entry.getKey(), entry.getValue()):strDesc.replace(strDesc, entry.getValue()));
			}
		}	  
		return acronymDesc;
	}
	
	/* Assign the RouteConstant value into the Map */
	public Map<String, String> getFullTextforUomAcronyms() {
		Map<String,String> uomDesc=new HashedMap();
	 	uomDesc.put(RouteConstants.C1UX_UOM_EA,RouteConstants.C1UX_UOM_EA_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_CS,RouteConstants.C1UX_UOM_CS_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_RL,RouteConstants.C1UX_UOM_RL_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_M,RouteConstants.C1UX_UOM_M_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_BX,RouteConstants.C1UX_UOM_BX_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_CT,RouteConstants.C1UX_UOM_CT_VALUE);
		//CAP-35069 additional UOM for catalog line items with full text retrieved from WCSS
		uomDesc.put(RouteConstants.C1UX_UOM_BG,RouteConstants.C1UX_UOM_BG_VALUE); 
		uomDesc.put(RouteConstants.C1UX_UOM_BK,RouteConstants.C1UX_UOM_BK_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_BL,RouteConstants.C1UX_UOM_BL_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_BR,RouteConstants.C1UX_UOM_BR_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_C,RouteConstants.C1UX_UOM_C_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_DZ,RouteConstants.C1UX_UOM_DZ_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_FM,RouteConstants.C1UX_UOM_FM_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_GR,RouteConstants.C1UX_UOM_GR_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_JK,RouteConstants.C1UX_UOM_JK_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_KT,RouteConstants.C1UX_UOM_KT_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_LR,RouteConstants.C1UX_UOM_LR_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_LT,RouteConstants.C1UX_UOM_LT_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_PD,RouteConstants.C1UX_UOM_PD_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_PK,RouteConstants.C1UX_UOM_PK_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_PL,RouteConstants.C1UX_UOM_PL_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_PR,RouteConstants.C1UX_UOM_PR_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_RM,RouteConstants.C1UX_UOM_RM_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_SH,RouteConstants.C1UX_UOM_SH_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_ST,RouteConstants.C1UX_UOM_ST_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_TB,RouteConstants.C1UX_UOM_TB_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_TT,RouteConstants.C1UX_UOM_TT_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_US,RouteConstants.C1UX_UOM_US_VALUE);
		uomDesc.put(RouteConstants.C1UX_UOM_UT,RouteConstants.C1UX_UOM_UT_VALUE);
		return uomDesc;
	}
}
