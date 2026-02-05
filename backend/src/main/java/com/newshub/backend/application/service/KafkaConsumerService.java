package com.newshub.backend.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import com.newshub.backend.domain.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

@Service
@Slf4j
@Profile("!local")
public class KafkaConsumerService {

    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "news-crawler-topic", groupId = "news-crawler-group-v2")
    public void consumeNews(String message) {
        try {
            Article article = objectMapper.readValue(message, Article.class);
            
            // Sanitize Summary (Defensive coding to handle legacy/bad messages in queue)
            if (article.getSummary() != null) {
                String rawSummary = article.getSummary();
                // Basic HTML tag stripping
                String cleanSummary = rawSummary.replaceAll("<[^>]*>", "").trim();
                // Truncate to avoid database errors (assuming VARCHAR(255))
                if (cleanSummary.length() > 250) {
                    cleanSummary = cleanSummary.substring(0, 247) + "...";
                }
                article.setSummary(cleanSummary);
            }
            
            // Check deduplication
            if (articleMapper.countBySourceUrl(article.getSourceUrl()) == 0) {
                articleMapper.insert(article);
                log.info("Saved news to DB: {}", article.getTitle());
            } else {
                log.debug("Duplicate news skipped: {}", article.getSourceUrl());
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing article", e);
        }
    }
}

