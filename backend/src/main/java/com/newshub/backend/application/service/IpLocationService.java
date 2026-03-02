package com.newshub.backend.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newshub.backend.application.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class IpLocationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    public IpLocationService(RestTemplate restTemplate, ObjectMapper objectMapper, CacheService cacheService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
    }

    public String getCityFromIp(String ipAddress) {
        String normalizedIp = normalizeIp(ipAddress);
        if (normalizedIp == null || "0:0:0:0:0:0:0:1".equals(normalizedIp) || "127.0.0.1".equals(normalizedIp)) {
            return "上海";
        }
        if (isPrivateIpv4(normalizedIp)) {
            return null;
        }

        // Check cache first to avoid redundant API calls
        String cacheKey = "ip:city:" + normalizedIp;
        String cachedCity = (String) cacheService.get(cacheKey);
        if (cachedCity != null) {
            return cachedCity;
        }

        String apiUrl = "http://ip-api.com/json/" + normalizedIp + "?lang=zh-CN";
        try {
            String response = restTemplate.getForObject(apiUrl, String.class);
            JsonNode root = objectMapper.readTree(response);
            if ("success".equals(root.path("status").asText())) {
                String city = root.path("city").asText();
                log.info("IP {} resolved to city: {}", normalizedIp, city);
                // Cache the result for 24 hours
                cacheService.set(cacheKey, city, 24, java.util.concurrent.TimeUnit.HOURS);
                return city;
            } else {
                log.warn("Failed to resolve IP {}: {}", normalizedIp, root.path("message").asText());
            }
        } catch (Exception e) {
            log.error("Error resolving IP {}: {}", normalizedIp, e.getMessage());
        }
        return null;
    }

    private static String normalizeIp(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        String trimmed = ipAddress.trim();
        if (trimmed.isEmpty() || "unknown".equalsIgnoreCase(trimmed)) {
            return null;
        }
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex >= 0) {
            trimmed = trimmed.substring(0, commaIndex).trim();
        }
        if (trimmed.matches("^\\d{1,3}(?:\\.\\d{1,3}){3}:\\d{1,5}$")) {
            trimmed = trimmed.substring(0, trimmed.indexOf(':'));
        }
        return trimmed;
    }

    private static boolean isPrivateIpv4(String ip) {
        if (!ip.matches("^\\d{1,3}(?:\\.\\d{1,3}){3}$")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);

        if (a == 10) {
            return true;
        }
        if (a == 127) {
            return true;
        }
        if (a == 192 && b == 168) {
            return true;
        }
        if (a == 172 && b >= 16 && b <= 31) {
            return true;
        }
        if (a == 169 && b == 254) {
            return true;
        }
        return false;
    }
}
