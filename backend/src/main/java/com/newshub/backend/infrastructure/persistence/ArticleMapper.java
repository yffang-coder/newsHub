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

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' ORDER BY publish_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Article> findLatest(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED'")
    int countPublishedArticles();
    
    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' AND category_id = #{categoryId} ORDER BY publish_time DESC LIMIT #{limit}")
    List<Article> findByCategory(Long categoryId, int limit);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' AND category_id = #{categoryId} AND id != #{excludeId} ORDER BY publish_time DESC LIMIT #{limit}")
    List<Article> findRelated(@Param("categoryId") Long categoryId, @Param("excludeId") Long excludeId, @Param("limit") int limit);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' AND MATCH(title, summary) AGAINST(#{keyword} IN BOOLEAN MODE) LIMIT 20")
    List<Article> search(String keyword);

    @Select("SELECT COUNT(*) FROM articles WHERE source_url = #{url}")
    int countBySourceUrl(String url);

    @Delete("DELETE FROM articles WHERE created_at < DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    int deleteOldArticles(int days);

    @Select("SELECT count(*) FROM articles")
    long count();

    @Select("SELECT COALESCE(SUM(views),0) FROM articles")
    long sumViews();

    @Select("<script>" +
            "SELECT a.*, c.name as categoryName FROM articles a " +
            "LEFT JOIN categories c ON a.category_id = c.id " +
            "WHERE (#{keyword} IS NULL OR a.title LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{startDate} IS NULL OR a.publish_time &gt;= #{startDate}) " +
            "AND (#{endDate} IS NULL OR a.publish_time &lt;= #{endDate}) " +
            "ORDER BY " +
            "<choose>" +
            "  <when test='sortField != null and sortOrder != null'>" +
            "    ${sortField} ${sortOrder}" +
            "  </when>" +
            "  <otherwise>" +
            "    a.publish_time DESC" +
            "  </otherwise>" +
            "</choose>" +
            " LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    java.util.List<Article> findPaged(@Param("offset") int offset, 
                                      @Param("limit") int limit, 
                                      @Param("keyword") String keyword,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("sortField") String sortField,
                                      @Param("sortOrder") String sortOrder);

    @Select("SELECT count(*) FROM articles a " +
            "WHERE (#{keyword} IS NULL OR a.title LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{startDate} IS NULL OR a.publish_time >= #{startDate}) " +
            "AND (#{endDate} IS NULL OR a.publish_time <= #{endDate})")
    long countByKeyword(@Param("keyword") String keyword,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate);

    @Update("UPDATE articles SET title=#{title}, summary=#{summary}, content=#{content}, " +
            "cover_image=#{coverImage}, category_id=#{categoryId}, status=#{status}, publish_time=#{publishTime}, updated_at=NOW() " +
            "WHERE id=#{id}")
    int update(Article article);

    @Delete("DELETE FROM articles WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE articles SET views = COALESCE(views,0) + 1 WHERE id = #{id}")
    int incrementViews(Long id);

    @Select("SELECT c.name as name, COUNT(a.id) as value FROM categories c LEFT JOIN articles a ON c.id = a.category_id GROUP BY c.id")
    List<java.util.Map<String, Object>> countByCategory();

    @Select("SELECT DATE_FORMAT(publish_time, '%Y-%m-%d') as date, COUNT(*) as count FROM articles WHERE publish_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) GROUP BY date ORDER BY date ASC")
    List<java.util.Map<String, Object>> countByDate(int days);

    @Select("SELECT c.name as name, COALESCE(SUM(a.views), 0) as value FROM categories c LEFT JOIN articles a ON c.id = a.category_id GROUP BY c.id")
    List<java.util.Map<String, Object>> sumViewsByCategory();

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' ORDER BY COALESCE(views, 0) DESC, publish_time DESC LIMIT #{limit}")
    List<Article> findTrending(int limit);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' AND DATE(publish_time) = CURDATE() ORDER BY publish_time DESC LIMIT #{limit}")
    List<Article> findDailyHighlights(int limit);
}

