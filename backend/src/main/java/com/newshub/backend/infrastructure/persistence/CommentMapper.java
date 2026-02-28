package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("SELECT c.*, u.username, u.avatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} ORDER BY c.created_at DESC")
    List<Comment> findByArticleId(Long articleId);

    @Insert("INSERT INTO comments (content, article_id, user_id, parent_id, created_at) VALUES (#{content}, #{articleId}, #{userId}, #{parentId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Comment comment);

    @Select("SELECT c.*, u.username, u.avatar FROM comments c LEFT JOIN users u ON c.user_id = u.id ORDER BY c.created_at DESC LIMIT #{limit} OFFSET #{offset}")
    java.util.List<Comment> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT count(*) FROM comments")
    long countAll();

    @Delete("DELETE FROM comments WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT count(*) FROM comments WHERE DATE(created_at) = CURRENT_DATE")
    long countToday();

    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as date, COUNT(*) as count FROM comments WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) GROUP BY date ORDER BY date ASC")
    List<java.util.Map<String, Object>> countByDate(int days);
}

