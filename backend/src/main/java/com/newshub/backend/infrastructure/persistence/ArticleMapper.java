package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    @Insert("INSERT INTO articles(title, summary, content, cover_image, author_id, category_id, source_url, source_name, publish_time, status, created_at, updated_at) " +
            "VALUES(#{title}, #{summary}, #{content}, #{coverImage}, #{authorId}, #{categoryId}, #{sourceUrl}, #{sourceName}, #{publishTime}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);

    @Select("SELECT * FROM articles WHERE id = #{id}")
    Article findById(Long id);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' ORDER BY publish_time DESC LIMIT #{limit}")
    List<Article> findLatest(int limit);
    
    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' AND category_id = #{categoryId} ORDER BY publish_time DESC LIMIT #{limit}")
    List<Article> findByCategory(Long categoryId, int limit);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' AND MATCH(title, summary) AGAINST(#{keyword} IN BOOLEAN MODE) LIMIT 20")
    List<Article> search(String keyword);

    @Select("SELECT COUNT(*) FROM articles WHERE source_url = #{url}")
    int countBySourceUrl(String url);

    @Delete("DELETE FROM articles WHERE created_at < DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    int deleteOldArticles();

    @Select("SELECT count(*) FROM articles")
    long count();

    @Select("SELECT COALESCE(SUM(views),0) FROM articles")
    long sumViews();

    @Select("SELECT * FROM articles WHERE (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%')) ORDER BY publish_time DESC LIMIT #{limit} OFFSET #{offset}")
    java.util.List<Article> findPaged(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword);

    @Select("SELECT count(*) FROM articles WHERE (#{keyword} IS NULL OR title LIKE CONCAT('%', #{keyword}, '%'))")
    long countByKeyword(@Param("keyword") String keyword);

    @Delete("DELETE FROM articles WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE articles SET views = COALESCE(views,0) + 1 WHERE id = #{id}")
    int incrementViews(Long id);
}

