package com.newshub.backend.application.service;

import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataCleanupService {

    @Autowired
    private ArticleMapper articleMapper;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldNews() {
        log.info("Starting data retention cleanup task...");
        int deletedCount = articleMapper.deleteOldArticles();
        log.info("Deleted {} old articles (older than today).", deletedCount);
    }
}

