package com.newshub.backend.application.service;

import com.newshub.backend.domain.model.Article;
import com.newshub.backend.infrastructure.persistence.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private NotificationService notificationService;

    public Map<String, Boolean> toggleFavorite(Long userId, Long articleId) {
        boolean exists = favoriteMapper.isFavorite(userId, articleId);
        if (exists) {
            favoriteMapper.removeFavorite(userId, articleId);
        } else {
            favoriteMapper.addFavorite(userId, articleId);
            // Send notification to user
            notificationService.createNotification(
                userId, 
                "收藏成功", 
                "您已成功收藏文章 #" + articleId, 
                "SYSTEM", 
                articleId
            );
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", !exists);
        return result;
    }

    public Map<String, Boolean> checkFavorite(Long userId, Long articleId) {
        boolean exists = favoriteMapper.isFavorite(userId, articleId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", exists);
        return result;
    }

    public List<Article> getFavorites(Long userId) {
        return favoriteMapper.findFavoritesByUserId(userId);
    }
}
