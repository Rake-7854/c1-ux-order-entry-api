package com.rrd.c1ux.api.models.items.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rrd.c1ux.api.models.items.ItemRpt;
import com.wallace.atwinxs.reports.vo.ItemRptVO;

/**
 * @author Krishna Natarajan
 *
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemRptMapper {
		ItemRpt mapItemDetail(ItemRptVO itemRptVO);
}
