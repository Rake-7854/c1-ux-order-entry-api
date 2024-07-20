package com.rrd.c1ux.api.models.items.mappers;

import org.mapstruct.Mapper;
import com.rrd.c1ux.api.models.items.FavoriteRequest;

@Mapper(componentModel = "spring")
public interface FavoriteRequestMapper {
FavoriteRequest fromRequest(FavoriteRequest favorites);
}
