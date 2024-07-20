package com.rrd.c1ux.api.models.singleitem.mappers;


import org.mapstruct.Mapper;

import com.rrd.c1ux.api.models.singleitem.SingleItemDetailsResponse;

@Mapper(componentModel = "spring")
public interface SingleItemDetailsMapper {
    
	SingleItemDetailsResponse getSingleItemDetails(SingleItemDetailsResponse details);
}
