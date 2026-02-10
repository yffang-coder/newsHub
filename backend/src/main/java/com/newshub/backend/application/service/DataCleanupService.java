package com.newshub.backend.application.service;

import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import com.newshub.backend.infrastructure.persistence.SettingsMapper;
import com.newshub.backend.domain.model.Settings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataCleanupService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private SettingsMapper settingsMapper;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldNews() {
        log.info("Starting data retention cleanup task...");
        
        int days = 3; // Default
        Settings retentionSetting = settingsMapper.findByKey("retention_days");
        if (retentionSetting != null) {
            try {
                days = Integer.parseInt(retentionSetting.getValue());
            } catch (NumberFormatException e) {
                log.warn("Invalid retention_days value, using default: 3");
            }
        }
        
        int deletedCount = articleMapper.deleteOldArticles(days);
        log.info("Deleted {} old articles (older than {} days).", deletedCount, days);
    }
}

