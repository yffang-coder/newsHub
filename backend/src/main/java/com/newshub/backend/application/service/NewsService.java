package com.newshub.backend.application.service;

import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import com.newshub.backend.domain.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class NewsService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CacheService cacheService;

    private static final String KEY_LATEST_NEWS = "news:latest";
    private static final String KEY_ARTICLE_PREFIX = "news:article:";

    public List<Article> getLatestNews(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        String key = KEY_LATEST_NEWS + ":" + page + ":" + pageSize;
        
        // Try to get from cache
        List<Article> cachedNews = (List<Article>) cacheService.get(key);
        if (cachedNews != null) {
            return cachedNews;
        }

        // Get from DB
        List<Article> news = articleMapper.findLatest(pageSize, offset);
        
        // Cache for 5 minutes (Short TTL for high concurrency "latest" updates)
        if (news != null && !news.isEmpty()) {
            cacheService.set(key, news, 5, TimeUnit.MINUTES);
        }
        
        return news;
    }

    public List<Article> getDailyHighlights(int limit) {
        String key = "news:daily-highlights:" + limit;
        List<Article> cachedHighlights = (List<Article>) cacheService.get(key);
        if (cachedHighlights != null) {
            return cachedHighlights;
        }

        List<Article> highlights = articleMapper.findDailyHighlights(limit);
        if (highlights != null && !highlights.isEmpty()) {
            cacheService.set(key, highlights, 30, TimeUnit.MINUTES); // Cache for 30 minutes
        }
        return highlights;
    }

    public int getTotalPublishedArticlesCount() {
        return articleMapper.countPublishedArticles();
    }

    public Article getArticleById(Long id) {
        String key = KEY_ARTICLE_PREFIX + id;
        Article cachedArticle = (Article) cacheService.get(key);
        if (cachedArticle != null) {
            articleMapper.incrementViews(id);
            Long v = cachedArticle.getViews();
            cachedArticle.setViews(v == null ? 1L : v + 1);
            cacheService.set(key, cachedArticle, 1, TimeUnit.HOURS);
            return cachedArticle;
        }

        Article article = articleMapper.findById(id);
        if (article != null) {
            articleMapper.incrementViews(id);
            Long v = article.getViews();
            article.setViews(v == null ? 1L : v + 1);
            cacheService.set(key, article, 1, TimeUnit.HOURS);
        }

        return article;
    }

    public List<Article> getNewsByCategory(Long categoryId, int limit) {
        return articleMapper.findByCategory(categoryId, limit);
    }

    public List<Article> getRelatedArticles(Long articleId, int limit) {
        Article article = articleMapper.findById(articleId);
        if (article == null) {
            return List.of();
        }
        return articleMapper.findRelated(article.getCategoryId(), articleId, limit);
    }

    public List<Article> searchArticles(String keyword) {
        return articleMapper.search(keyword);
    }

    public List<Article> getTrendingNews(int limit) {
        String key = "news:trending:" + limit;
        List<Article> cachedNews = (List<Article>) cacheService.get(key);
        if (cachedNews != null) {
            return cachedNews;
        }

        List<Article> news = articleMapper.findTrending(limit);
        if (news != null && !news.isEmpty()) {
            cacheService.set(key, news, 10, TimeUnit.MINUTES); // Cache for 10 minutes
        }
        return news;
    }
}

