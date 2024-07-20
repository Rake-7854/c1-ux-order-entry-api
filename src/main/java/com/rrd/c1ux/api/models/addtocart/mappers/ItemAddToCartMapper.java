package com.rrd.c1ux.api.models.addtocart.mappers;


import org.mapstruct.Mapper;

import com.rrd.c1ux.api.models.addtocart.ItemAddToCartResponse;

@Mapper(componentModel = "spring")
public interface ItemAddToCartMapper {
    
	ItemAddToCartResponse isItemAddedToCart(ItemAddToCartResponse details);
}
