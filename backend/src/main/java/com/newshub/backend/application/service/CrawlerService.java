package com.newshub.backend.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.nio.file.Paths;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.crawler.enabled", havingValue = "true")
public class CrawlerService {

    @Autowired
    private ArticleMapper articleMapper;

    @Scheduled(cron = "0 0 0 * * ?") // Run at 00:00 every day
    public void cleanupOldNews() {
        log.info("Starting cleanup of old news...");
        int deletedCount = articleMapper.deleteOldArticles();
        log.info("Cleanup completed. Deleted " + deletedCount + " old articles.");
    }

    @Value("${app.crawler.script-path:/app/crawler-python}")
    private String scriptPath;

    @Value("${app.crawler.python-command:python3}")
    private String pythonCommand;

    @Scheduled(fixedRateString = "${app.crawler.interval}")
    public void crawlNews() {
        log.info("Triggering Python crawler...");
        try {
            // Run Python script
            String script = Paths.get(scriptPath, "main.py").toString();
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, script);
            Map<String, String> env = pb.environment();
            env.put("CRAWLER_LOOP", "false"); // Ensure it runs only once
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Read output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Python Crawler: " + line);
                }
            }
            
            int exitCode = process.waitFor();
            log.info("Python crawler exited with code: " + exitCode);
            
        } catch (Exception e) {
            log.error("Error running Python crawler", e);
        }
    }

    @Scheduled(fixedRateString = "${app.crawler.interval}")
    public void crawlWeather() {
        log.info("Triggering Weather crawler...");
        new Thread(() -> {
            try {
                // Run Weather Python script
                String script = Paths.get(scriptPath, "weather_crawler.py").toString();
                ProcessBuilder pb = new ProcessBuilder(pythonCommand, script);
                pb.redirectErrorStream(true);
                
                Process process = pb.start();
                
                // Read output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("Weather Crawler: " + line);
                    }
                }
                
                int exitCode = process.waitFor();
                log.info("Weather crawler exited with code: " + exitCode);
                
            } catch (Exception e) {
                log.error("Error running Weather crawler", e);
            }
        }).start();
    }
    
    @PostConstruct
    public void init() {
        // Run crawlers on startup
        log.info("Application Ready: Triggering initial weather crawl...");
        crawlWeather();
    }
}

