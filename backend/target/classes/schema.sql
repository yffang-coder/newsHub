-- Create Database
-- CREATE DATABASE IF NOT EXISTS newshub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- USE newshub;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    `role` VARCHAR(20) DEFAULT 'USER',
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Articles Table (Core for high volume)
-- Partitioning by range (publish_time) could be considered for very large datasets,
-- but for "keep 3 days" simple deletion is enough.
CREATE TABLE IF NOT EXISTS articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(500),
    content LONGTEXT, -- Full content
    cover_image VARCHAR(255),
    author_id BIGINT,
    category_id BIGINT,
    source_url VARCHAR(500), -- For crawled news
    source_name VARCHAR(100),
    publish_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    views BIGINT DEFAULT 0,
    likes BIGINT DEFAULT 0,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    -- Optimization for 3-day retention query
    INDEX idx_publish_time (publish_time),
    -- Optimization for frontend queries
    INDEX idx_category_status (category_id, status),
    INDEX idx_views (views DESC),
    -- Added optimizations
    INDEX idx_source_url (source_url(255)),
    FULLTEXT INDEX idx_fulltext_search (title, summary) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tags Table
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Article Tags Relation
CREATE TABLE IF NOT EXISTS article_tags (
    article_id BIGINT,
    tag_id BIGINT,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comments Table
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT, -- For nested comments
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_article_created (article_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Settings Table
CREATE TABLE IF NOT EXISTS settings (
    `key` VARCHAR(50) PRIMARY KEY,
    `value` TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Favorites Table
CREATE TABLE IF NOT EXISTS favorites (
    user_id BIGINT,
    article_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, article_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    type VARCHAR(50) DEFAULT 'SYSTEM', -- SYSTEM, COMMENT, LIKE, etc.
    is_read BOOLEAN DEFAULT FALSE,
    related_id BIGINT, -- ID of related entity (article_id, etc.)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Initial Data

-- Note: Password is 'password' encoded with BCrypt

INSERT IGNORE INTO categories (name, slug) VALUES 
('Technology', 'tech'),
('Finance', 'finance'),
('World', 'world'),
('Culture', 'culture');

REPLACE INTO settings (`key`, `value`) VALUES 
('site_name', 'NewsHub'),
('site_description', 'A modern news aggregation platform'),
('footer_text', '© 2024 NewsHub. All rights reserved.'),
('retention_days', '3'),
('about_content', '# 关于我们\n\nNewsHub 是一个致力于提供最新、最热新闻聚合的平台。\n\n## 我们的使命\n\n连接世界，传递价值。\n\n## 我们的愿景\n\n成为全球领先的新闻聚合服务提供商。'),
('contact_content', '# 联系方式\n\n如果您有任何问题或建议，请通过以下方式联系我们：\n\n- 邮箱: support@newshub.com\n- 电话: +86 123 4567 8901\n- 地址: 北京市朝阳区科技园'),
('careers_content', '# 加入我们\n\n我们正在寻找优秀的你！\n\n## 开放职位\n\n### Java 后端工程师\n- 负责后端服务的设计与开发\n- 熟悉 Spring Boot, MySQL, Redis\n\n### 前端工程师\n- 负责前端界面的开发\n- 熟悉 Vue 3, TypeScript'),
('privacy_content', '# 隐私政策\n\n我们重视您的隐私。\n\n1. **信息收集**: 我们仅收集必要的信息以提供服务。\n2. **信息使用**: 您的信息将仅用于改善用户体验。\n3. **信息保护**: 我们采取严格的安全措施保护您的数据。');
