package com.newshub.backend.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class IpLocationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public IpLocationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getCityFromIp(String ipAddress) {
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "127.0.0.1".equals(ipAddress)) {
            // For local development, return a default city or null
            return "上海"; // Default city for local development
        }

        String apiUrl = "http://ip-api.com/json/" + ipAddress + "?lang=zh-CN";
        try {
            String response = restTemplate.getForObject(apiUrl, String.class);
            JsonNode root = objectMapper.readTree(response);
            if ("success".equals(root.path("status").asText())) {
                String city = root.path("city").asText();
                log.info("IP {} resolved to city: {}", ipAddress, city);
                return city;
            } else {
                log.warn("Failed to resolve IP {}: {}", ipAddress, root.path("message").asText());
            }
        } catch (Exception e) {
            log.error("Error resolving IP {}: {}", ipAddress, e.getMessage());
        }
        return null;
    }
}
