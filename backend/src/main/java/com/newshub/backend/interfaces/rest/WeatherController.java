package com.newshub.backend.interfaces.rest;

import com.newshub.backend.application.service.CrawlerService;
import com.newshub.backend.application.service.CacheService;
import com.newshub.backend.application.service.IpLocationService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private IpLocationService ipLocationService;

    private static final String WEATHER_CACHE_KEY_PREFIX = "weather:data:";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/weather")
    public ResponseEntity<Object> getWeather(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Real-IP");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Forwarded-For");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        
        String city = ipLocationService.getCityFromIp(clientIp);

        if (city == null) {
            return ResponseEntity.ok(Collections.emptyList()); // Return empty if city cannot be determined
        }

        String cacheKey = WEATHER_CACHE_KEY_PREFIX + city;
        Object data = cacheService.get(cacheKey);
        
        if (data != null) {
            return ResponseEntity.ok(data);
        } else {
            // Trigger weather crawl for the city if not in cache
            crawlerService.crawlWeather(city);
            // For immediate response, return empty and let frontend retry or show loading
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/weather/update")
    public ResponseEntity<String> updateWeather(@RequestBody List<Map<String, Object>> weatherData, @RequestParam String city) {
        String cacheKey = WEATHER_CACHE_KEY_PREFIX + city;
        cacheService.set(cacheKey, weatherData, 2, TimeUnit.HOURS);
        return ResponseEntity.ok("Updated weather for " + city);
    }

    @PostMapping("/weather/refresh")
    public ResponseEntity<String> refreshWeather(@RequestParam(required = false) String city) {
        if (city != null && !city.isEmpty()) {
            crawlerService.crawlWeather(city);
            return ResponseEntity.ok("Weather refresh triggered for " + city);
        } else {
            // Default behavior if no city is specified (e.g., for scheduled tasks)
            crawlerService.crawlWeather("上海"); // Default city for general refresh
            return ResponseEntity.ok("General weather refresh triggered");
        }
    }
}

