package com.newshub.backend.interfaces.rest;

import com.newshub.backend.application.service.CrawlerService;
import com.newshub.backend.application.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public")
public class WeatherController {

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private CacheService cacheService;

    private static final String WEATHER_CACHE_KEY = "weather:data";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/weather")
    public ResponseEntity<Object> getWeather() {
        Object data = cacheService.get(WEATHER_CACHE_KEY);
        return ResponseEntity.ok(data != null ? data : Collections.emptyList());
    }

    @PostMapping("/weather/update")
    public ResponseEntity<String> updateWeather(@RequestBody List<Map<String, Object>> weatherData) {
        cacheService.set(WEATHER_CACHE_KEY, weatherData, 2, TimeUnit.HOURS);
        return ResponseEntity.ok("Updated");
    }

    @PostMapping("/weather/refresh")
    public ResponseEntity<String> refreshWeather() {
        crawlerService.crawlWeather();
        return ResponseEntity.ok("Weather refresh triggered");
    }
}

