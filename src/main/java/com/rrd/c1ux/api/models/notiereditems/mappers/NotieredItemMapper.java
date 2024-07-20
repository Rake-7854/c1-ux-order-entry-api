package com.rrd.c1ux.api.models.notiereditems.mappers;

import org.mapstruct.Mapper;
import com.wallace.atwinxs.orderentry.vo.PriceAndAvailabilityVO;

@Mapper(componentModel = "spring")
public interface NotieredItemMapper {
	
	PriceAndAvailabilityVO getNoTieredItemPricing(PriceAndAvailabilityVO item);

}
