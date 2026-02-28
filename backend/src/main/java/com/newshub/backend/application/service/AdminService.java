package com.newshub.backend.application.service;

import com.newshub.backend.domain.model.Settings;
import com.newshub.backend.domain.model.User;
import com.newshub.backend.infrastructure.persistence.ArticleMapper;
import com.newshub.backend.infrastructure.persistence.SettingsMapper;
import com.newshub.backend.infrastructure.persistence.UserMapper;
import com.newshub.backend.infrastructure.persistence.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private com.newshub.backend.infrastructure.persistence.CategoryMapper categoryMapper;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.count());
        stats.put("totalArticles", articleMapper.count());
        stats.put("totalVisits", articleMapper.sumViews());
        stats.put("todayComments", commentMapper.countToday());
        
        // Add charts data
        stats.put("articleTrend", articleMapper.countByDate(7));
        stats.put("commentTrend", commentMapper.countByDate(7));
        stats.put("categoryDistribution", articleMapper.countByCategory());
        stats.put("visitsByCategory", articleMapper.sumViewsByCategory());

        // Add recent activity
        stats.put("recentArticles", articleMapper.findPaged(0, 5, null, null, null, null, null));
        stats.put("recentComments", commentMapper.findAllPaged(0, 5));

        stats.put("topArticlesByViews", articleMapper.findTrending(5));
        
        return stats;
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    public void updateUserRole(Long id, String role) {
        userMapper.updateRole(id, role);
    }

    public Map<String, String> getSettings() {
        List<Settings> settingsList = settingsMapper.findAll();
        Map<String, String> settingsMap = new HashMap<>();
        for (Settings s : settingsList) {
            settingsMap.put(s.getKey(), s.getValue());
        }
        return settingsMap;
    }

    public void updateSettings(Map<String, String> newSettings) {
        for (Map.Entry<String, String> entry : newSettings.entrySet()) {
            Settings settings = new Settings();
            settings.setKey(entry.getKey());
            settings.setValue(entry.getValue());
            settingsMapper.save(settings);
        }
    }

    public Map<String, Object> getCommentsPaged(int page, int size) {
        int offset = (page - 1) * size;
        java.util.List<com.newshub.backend.domain.model.Comment> list = commentMapper.findAllPaged(offset, size);
        long total = commentMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("items", list);
        result.put("total", total);
        return result;
    }

    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }

    public Map<String, Object> getArticlesPaged(int page, int size, String keyword, String startDate, String endDate, String sortField, String sortOrder) {
        int offset = (page - 1) * size;
        
        // Map frontend field names to database columns
        String dbSortField = null;
        if ("publishTime".equals(sortField)) {
            dbSortField = "a.publish_time";
        }
        
        // Sanitize sort order
        String dbSortOrder = null;
        if ("ascending".equals(sortOrder)) {
            dbSortOrder = "ASC";
        } else if ("descending".equals(sortOrder)) {
            dbSortOrder = "DESC";
        }

        java.util.List<com.newshub.backend.domain.model.Article> list = articleMapper.findPaged(offset, size, keyword, startDate, endDate, dbSortField, dbSortOrder);
        long total = articleMapper.countByKeyword(keyword, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("items", list);
        result.put("total", total);
        return result;
    }

    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
        clearNewsCache(id);
    }

    public void createArticle(com.newshub.backend.domain.model.Article article) {
        if (article.getPublishTime() == null) {
            article.setPublishTime(java.time.LocalDateTime.now());
        }
        articleMapper.insert(article);
        clearNewsCache(article.getId());
    }

    public void updateArticle(com.newshub.backend.domain.model.Article article) {
        articleMapper.update(article);
        clearNewsCache(article.getId());
    }

    private void clearNewsCache(Long articleId) {
        cacheService.delete("news:latest:1");
        cacheService.delete("news:latest:5");
        cacheService.delete("news:latest:10");
        if (articleId != null) {
            cacheService.delete("news:article:" + articleId);
        }
    }

    public List<com.newshub.backend.domain.model.Category> getAllCategories() {
        return categoryMapper.findAll();
    }
}
