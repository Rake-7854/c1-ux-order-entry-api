package com.rrd.c1ux.api.models.shoppingcart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContinueShoppingResponse {
	private String status;
	private String returnLinkText;
	private String returnLinkURL;
}