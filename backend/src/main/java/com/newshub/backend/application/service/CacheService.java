package com.newshub.backend.application.service;

import java.util.concurrent.TimeUnit;

public interface CacheService {
    void set(String key, Object value, long timeout, TimeUnit unit);
    Object get(String key);
}

