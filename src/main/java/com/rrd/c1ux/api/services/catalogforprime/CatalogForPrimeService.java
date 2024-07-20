package com.rrd.c1ux.api.services.catalogforprime;

import java.util.ArrayList;
import java.util.Collection;

import com.wallace.atwinxs.catalogs.vo.TreeNodeVO;

public interface CatalogForPrimeService {
	ArrayList<Object> getCatalogMenuForPrime(Collection<TreeNodeVO> calalogs);
}
