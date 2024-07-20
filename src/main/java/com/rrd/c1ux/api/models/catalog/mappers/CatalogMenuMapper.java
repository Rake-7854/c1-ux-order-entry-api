package com.rrd.c1ux.api.models.catalog.mappers;


import org.mapstruct.Mapper;

import com.rrd.c1ux.api.models.catalog.CatalogTreeResponse;

@Mapper(componentModel = "spring")
public interface CatalogMenuMapper {
    
	CatalogTreeResponse getCatalogMenu(CatalogTreeResponse cat);
}
