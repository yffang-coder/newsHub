package com.newshub.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private Long authorId;
    private Long categoryId;
    private String sourceUrl;
    private String sourceName;
    private LocalDateTime publishTime;
    private Long views;
    private Long likes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields
    private String categoryName;
}

