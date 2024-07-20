package com.rrd.c1ux.api.config;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventLogger implements CacheEventListener<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(CacheEventLogger.class);

    @Override
    public void onEvent(
      CacheEvent<? extends Object, ? extends Object> cacheEvent) {

        logger.info("Cache event {} for {}", cacheEvent.getType(), cacheEvent.getKey());
    }
}
