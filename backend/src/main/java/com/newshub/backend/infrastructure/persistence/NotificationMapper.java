package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notifications(user_id, title, content, type, is_read, related_id) " +
            "VALUES(#{userId}, #{title}, #{content}, #{type}, #{isRead}, #{relatedId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Notification> findByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = false")
    int countUnread(Long userId);

    @Update("UPDATE notifications SET is_read = true WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notifications SET is_read = true WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);

    @Select("SELECT * FROM notifications ORDER BY created_at DESC LIMIT 100")
    List<Notification> findAll();
    
    @Delete("DELETE FROM notifications WHERE user_id = #{userId}")
    int deleteAllByUserId(Long userId);
}
