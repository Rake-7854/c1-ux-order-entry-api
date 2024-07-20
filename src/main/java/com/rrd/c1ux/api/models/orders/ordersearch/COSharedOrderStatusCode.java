/*
 * Copyright (c) RR Donnelley, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * RR Donnelley, Inc.
 * You shall not disclose such confidential information.
 * 
 *	Revisions: 
 *	Date        Created By          DTS#            	Description
 *	--------    -----------         ----------      	------------------------------------
 *  12/08/22	Sakthi M        	CAP-37295 	       Created class for the CAP-37295
 */
package com.rrd.c1ux.api.models.orders.ordersearch;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name ="COSharedOrderStatusCode", description = "Response Class for order status code", type = "object")
public class COSharedOrderStatusCode 
{
	@Schema(name ="code", description = "Order Status Code", type = "string", example="PROC")
	protected String code;
	@Schema(name ="simpleDescription", description = "Simple Description for Order status", type = "string", example="Punchout Partially Fulfilled")
	protected String simpleDescription;
	@Schema(name ="advancedDescription", description = "Advanced Description for Order status", type = "string", example="Punchout Partially Fulfilled")
	protected String advancedDescription;
	@Schema(name ="definition", description = "Definition for Order status", type = "string", example="A punchout quote that has some line items submitted to the Vendor Order Entry System.")
	protected String definition;
	@Schema(name ="simpleDefinition", description = "Simple Definition for Order status", type = "string", example="A punchout quote that has some line items submitted to the Vendor Order Entry System.")
	protected String simpleDefinition;
	@Schema(name ="webToOne", description = "Flag indicating status is unique to Web To One ", type = "boolean", example="false")
	protected boolean webToOne;
	@Schema(name ="weight", description = "Weight AKA priority", type = "int", example="500")
	protected int weight = 0;
	@Schema(name ="simpleDescriptionTag", description = "Simple Description Tranaslation text Tag for Order status", type = "string", example="OSSimplePunchoutPartial")
	protected String simpleDescriptionTag;
	@Schema(name ="advancedDescriptionTag", description = "Advanced Description Tranaslation text Tag for Order status", type = "string", example="OSAdvncPunchoutPartial")
	protected String advancedDescriptionTag;
	@Schema(name ="displayTracking", description = " Flag indicating whether to Display tracking", type = "boolean", example="false")
	protected boolean displayTracking;
	@Schema(name ="wcssGreaterSearch", description = "WCSS status code to use if modifier is Greater Search", type = "string", example="00")
	protected String wcssGreaterSearch;
	@Schema(name ="wcssLessSearch", description = "WCSS status code to use if modifier is Less Search", type = "string", example="50")
	protected String wcssLessSearch;
	@Schema(name ="wcssEqualSearch", description = "WCSS status code to use if modifier is Equal Search", type = "string", example="00")
	protected String wcssEqualSearch;
	
	@Schema(name ="simpleTagTranslation", description = "Simple Tag Translation for Order status", type = "string", example="Punchout Partially Fulfilled")
	protected String simpleTagTranslation;
	@Schema(name ="advancedTagTranslation", description = "Advanced Tag Translation for Order status", type = "string", example="Punchout Partially Fulfilled")
	protected String advancedTagTranslation;
	
	public COSharedOrderStatusCode()
	{
	}
	
	public COSharedOrderStatusCode(String code, String simpleDescription, String advancedDescription, String definition, String simpleDefinition, boolean webToOne, int weight,
			String simpleDescriptionTag, String advancedDescriptionTag, boolean displayTracking, String wcssGreaterSearch, String wcssLessSearch, String wcssEqualSearch)
	{
		super();
		this.code = code;
		this.simpleDescription = simpleDescription;
		this.advancedDescription = advancedDescription;
		this.definition = definition;
		this.simpleDefinition = simpleDefinition;
		this.webToOne = webToOne;
		this.weight = weight;
		this.simpleDescriptionTag = simpleDescriptionTag;
		this.advancedDescriptionTag = advancedDescriptionTag;
		this.displayTracking = displayTracking;
		this.wcssGreaterSearch = wcssGreaterSearch;
		this.wcssLessSearch = wcssLessSearch;
		this.wcssEqualSearch = wcssEqualSearch;
	}

	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	public String getSimpleDescription()
	{
		return simpleDescription;
	}
	public void setSimpleDescription(String simpleDescription)
	{
		this.simpleDescription = simpleDescription;
	}
	public String getAdvancedDescription()
	{
		return advancedDescription;
	}
	public void setAdvancedDescription(String advancedDescription)
	{
		this.advancedDescription = advancedDescription;
	}
	public String getDefinition()
	{
		return definition;
	}
	public void setDefinition(String definition)
	{
		this.definition = definition;
	}
	public String getSimpleDefinition()
	{
		return simpleDefinition;
	}
	public void setSimpleDefinition(String simpleDefinition)
	{
		this.simpleDefinition = simpleDefinition;
	}
	public boolean isWebToOne()
	{
		return webToOne;
	}
	public void setWebToOne(boolean webToOne)
	{
		this.webToOne = webToOne;
	}
	
	public int getWeight()
	{
		return weight;
	}
	public void setWeight(int weight)
	{
		this.weight = weight;
	}
	public String getSimpleDescriptionTag()
	{
		return simpleDescriptionTag;
	}
	public void setSimpleDescriptionTag(String simpleDescriptionTag)
	{
		this.simpleDescriptionTag = simpleDescriptionTag;
	}
	public String getAdvancedDescriptionTag()
	{
		return advancedDescriptionTag;
	}
	public void setAdvancedDescriptionTag(String advancedDescriptionTag)
	{
		this.advancedDescriptionTag = advancedDescriptionTag;
	}
	public boolean isDisplayTracking()
	{
		return displayTracking;
	}
	public void setDisplayTracking(boolean displayTracking)
	{
		this.displayTracking = displayTracking;
	}
	public String getWcssGreaterSearch()
	{
		return wcssGreaterSearch;
	}
	public void setWcssGreaterSearch(String wcssGreaterSearch)
	{
		this.wcssGreaterSearch = wcssGreaterSearch;
	}
	public String getWcssLessSearch()
	{
		return wcssLessSearch;
	}
	public void setWcssLessSearch(String wcssLessSearch)
	{
		this.wcssLessSearch = wcssLessSearch;
	}
	public String getWcssEqualSearch()
	{
		return wcssEqualSearch;
	}
	public void setWcssEqualSearch(String wcssEqualSearch)
	{
		this.wcssEqualSearch = wcssEqualSearch;
	}

	public String getSimpleTagTranslation()
	{
		return simpleTagTranslation;
	}

	public void setSimpleTagTranslation(String simpleTagTranslation)
	{
		this.simpleTagTranslation = simpleTagTranslation;
	}

	public String getAdvancedTagTranslation()
	{
		return advancedTagTranslation;
	}

	public void setAdvancedTagTranslation(String advancedTagTranslation)
	{
		this.advancedTagTranslation = advancedTagTranslation;
	}
}
