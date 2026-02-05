package com.newshub.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private String content;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private LocalDateTime createdAt;
    
    // Transient fields for display
    private String username;
    private String avatar;
}

