package com.rrd.c1ux.api.models.catalogitems.mappers;

import org.mapstruct.Mapper;

import com.rrd.c1ux.api.models.catalogitems.CatalogItemsResponse;

@Mapper(componentModel = "spring")
public interface CatalogItemsMapper {
    
    CatalogItemsResponse getCatalogItems(CatalogItemsResponse item);
}

