package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorites(user_id, article_id) VALUES(#{userId}, #{articleId})")
    int addFavorite(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND article_id = #{articleId}")
    int removeFavorite(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    @Select("SELECT COUNT(*) > 0 FROM favorites WHERE user_id = #{userId} AND article_id = #{articleId}")
    boolean isFavorite(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    @Select("SELECT a.* FROM articles a JOIN favorites f ON a.id = f.article_id WHERE f.user_id = #{userId} ORDER BY f.created_at DESC")
    List<Article> findFavoritesByUserId(@Param("userId") Long userId);
}
