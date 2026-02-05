package com.newshub.backend.application.service;

import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import com.newshub.backend.domain.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("local")
public class LocalProducerService implements NewsProducer {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void sendNews(Article article) {
        log.info("[Local] Saving news directly to DB (skipping Kafka): {}", article.getTitle());
        // Simple deduplication check
        if (articleMapper.countBySourceUrl(article.getSourceUrl()) == 0) {
            articleMapper.insert(article);
        }
    }
}

