package com.newshub.backend.interfaces.rest;

import com.newshub.backend.application.service.CrawlerService;
import com.newshub.backend.application.service.CacheService;
import com.newshub.backend.application.service.IpLocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public")
@Slf4j
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
        String xRealIp = request.getHeader("X-Real-IP");
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();

        log.info("IP Headers - X-Real-IP: {}, X-Forwarded-For: {}, RemoteAddr: {}", xRealIp, xForwardedFor, remoteAddr);

        String clientIp = extractClientIp(xRealIp, xForwardedFor, remoteAddr);
        log.info("Resolved client IP: {}", clientIp);
        
        String city = ipLocationService.getCityFromIp(clientIp);

        // Default to Shanghai if city cannot be determined
        if (city == null || city.isEmpty()) {
            city = "上海";
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

    private static String extractClientIp(String xRealIp, String xForwardedFor, String remoteAddr) {
        String candidate = firstNonEmpty(
                sanitizeIp(firstIpFromXff(xForwardedFor)),
                sanitizeIp(xRealIp),
                sanitizeIp(remoteAddr)
        );
        return candidate == null ? "" : candidate;
    }

    private static String firstIpFromXff(String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.isEmpty()) {
            return null;
        }
        String[] parts = xForwardedFor.split(",");
        for (String part : parts) {
            String ip = part == null ? "" : part.trim();
            if (!ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return null;
    }

    private static String sanitizeIp(String ip) {
        if (ip == null) {
            return null;
        }
        String trimmed = ip.trim();
        if (trimmed.isEmpty() || "unknown".equalsIgnoreCase(trimmed)) {
            return null;
        }
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex >= 0) {
            trimmed = trimmed.substring(0, commaIndex).trim();
        }
        if (trimmed.matches("^\\d{1,3}(?:\\.\\d{1,3}){3}:\\d{1,5}$")) {
            return trimmed.substring(0, trimmed.indexOf(':'));
        }
        return trimmed;
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isEmpty()) {
                return v;
            }
        }
        return null;
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

