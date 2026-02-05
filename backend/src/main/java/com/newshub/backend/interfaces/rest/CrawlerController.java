package com.newshub.backend.interfaces.rest;

import com.newshub.backend.application.service.CrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/crawler")
@Tag(name = "Crawler API", description = "Endpoints for managing news crawler")
@ConditionalOnProperty(name = "app.crawler.enabled", havingValue = "true")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @Operation(summary = "Trigger news crawler", description = "Manually triggers the news crawler to fetch latest news from RSS feeds")
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerCrawler() {
        CompletableFuture.runAsync(() -> crawlerService.crawlNews());
        return ResponseEntity.ok("Crawler triggered successfully");
    }
}

