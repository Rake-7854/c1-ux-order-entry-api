package com.rrd.c1ux.api.models.items;

/**
 * @author Krishna Natarajan
 *
 */
public class ItemRptKey implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String wallaceItemNumber;
	public ItemRptKey(String wallaceItemNumber)
	{
		this.setWallaceItemNumber(wallaceItemNumber);
	}
	public String getWallaceItemNumber() {
		return wallaceItemNumber;
	}
	public void setWallaceItemNumber(String wallaceItemNumber) {
		this.wallaceItemNumber = wallaceItemNumber;
	} 

}
