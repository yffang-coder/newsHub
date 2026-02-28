package com.newshub.backend.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Profile("local")
public class LocalCacheService implements CacheService {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        cache.put(key, value);
        // Expiration not implemented for simplicity in local mode
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }
}

