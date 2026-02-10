package com.newshub.backend.interfaces.rest;

import com.newshub.backend.domain.model.Article;
import com.newshub.backend.application.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/news")
@Tag(name = "News API", description = "Endpoints for accessing news content")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Operation(summary = "Get latest news", description = "Returns a list of latest published articles")
    @GetMapping("/latest")
    public ResponseEntity<List<Article>> getLatestNews(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(newsService.getLatestNews(limit));
    }

    @Operation(summary = "Get article details", description = "Returns full details of an article by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticle(@PathVariable Long id) {
        Article article = newsService.getArticleById(id);
        if (article != null) {
            return ResponseEntity.ok(article);
        }
        return ResponseEntity.notFound().build();
    }
    
    @Operation(summary = "Get news by category", description = "Returns list of articles for a specific category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Article>> getNewsByCategory(@PathVariable Long categoryId, 
                                                         @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(newsService.getNewsByCategory(categoryId, limit));
    }

    @Operation(summary = "Get related news", description = "Returns related articles for a given article ID")
    @GetMapping("/{id}/related")
    public ResponseEntity<List<Article>> getRelatedNews(@PathVariable Long id, 
                                                      @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(newsService.getRelatedArticles(id, limit));
    }

    @Operation(summary = "Search news", description = "Search articles by title or summary")
    @GetMapping("/search")
    public ResponseEntity<List<Article>> searchNews(@RequestParam String q) {
        return ResponseEntity.ok(newsService.searchArticles(q));
    }
}

