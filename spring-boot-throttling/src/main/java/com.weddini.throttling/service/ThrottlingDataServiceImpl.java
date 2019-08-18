package com.weddini.throttling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.weddini.throttling.ThrottlingGauge;
import com.weddini.throttling.ThrottlingKey;
import com.weddini.throttling.cache.Cache;
import com.weddini.throttling.cache.CacheBuilder;
import com.weddini.throttling.cache.CacheLoader;

import java.util.concurrent.ExecutionException;


public class ThrottlingDataServiceImpl implements ThrottlingDataService {
    private static final Logger log = LoggerFactory.getLogger(ThrottlingDataServiceImpl.class);

    private final Cache<ThrottlingKey, ThrottlingGauge> cache;
    private final CacheLoader<ThrottlingKey, ThrottlingGauge> gaugeLoader = key -> new ThrottlingGauge(key.getTimeUnit(), key.getLimit());


    public ThrottlingDataServiceImpl(int cacheSize) {
        this.cache = CacheBuilder.<ThrottlingKey, ThrottlingGauge>builder()
                .setMaximumWeight(cacheSize)
                .build();
    }

    @Override
    public boolean throttle(ThrottlingKey key, String evaluatedValue) {

        try {

            ThrottlingGauge gauge = cache.computeIfAbsent(key, gaugeLoader);
            gauge.removeEldest();
            return gauge.throttle();

        } catch (ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error("exception occurred while calculating throttle value", e);
            }
        }

        return true;
    }

}
