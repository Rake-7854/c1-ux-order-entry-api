/*
 * Copyright (c) RR Donnelley. All Rights Reserved.
 * This software is the confidential and proprietary information of RR Donnelley.
 * You shall not disclose such confidential information.
 *
 * Revisions:
 * 	Date		Modified By		DTS#										Description
 * 	--------	-----------		----------------------------------------	------------------------------
 *	06/05/2024	Sakthi M		CAP-49782	               					 Initial creation
 *
 */

package com.rrd.c1ux.api.models.items;

public class COSuggestedItemSearchResult extends  SearchResult implements COSuggestedItemThumbnailCellData  {
	
	private String suggestedBecause;
	private double price;
	private String inStock;
	
	public COSuggestedItemSearchResult() {}
	public COSuggestedItemSearchResult(String itemNumber, String itemDescription)
	{
		super(itemNumber, itemDescription);
	}
	
	public String getSuggestedBecauseCol() 
	{
		return suggestedBecause;
	}
	
	public void setSuggestedBecauseCol(String suggestedBecause) 
	{
		this.suggestedBecause = suggestedBecause;
	}
	
	public String getInStockCol() 
	{
		return inStock;
	}
	
	public void setInStockCol(String inStock) 
	{
		this.inStock = inStock;
	}
	
	@Override
	public double getPrice()
	{
		return price;
	}
	
	public void setPrice(double price)
	{
		this.price = price;
	}

}
