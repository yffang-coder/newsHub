package com.newshub.backend.interfaces.rest;

import com.newshub.backend.application.service.FavoriteService;
import com.newshub.backend.domain.model.Article;
import com.newshub.backend.domain.model.User;
import com.newshub.backend.infrastructure.persistence.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserMapper userMapper;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }
        String username = authentication.getName();
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user.getId();
    }

    @PostMapping("/toggle/{articleId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long articleId) {
        try {
            Long userId = getCurrentUserId();
            return ResponseEntity.ok(favoriteService.toggleFavorite(userId, articleId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check/{articleId}")
    public ResponseEntity<?> checkFavorite(@PathVariable Long articleId) {
        try {
            Long userId = getCurrentUserId();
            return ResponseEntity.ok(favoriteService.checkFavorite(userId, articleId));
        } catch (RuntimeException e) {
             // If not logged in, just return false
             return ResponseEntity.ok(Map.of("favorited", false));
        }
    }

    @GetMapping
    public ResponseEntity<?> getFavorites() {
        try {
            Long userId = getCurrentUserId();
            return ResponseEntity.ok(favoriteService.getFavorites(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
