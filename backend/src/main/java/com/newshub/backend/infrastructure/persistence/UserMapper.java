package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE username = #{username} OR email = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO users(username, password, email, role, avatar, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{email}, #{role}, #{avatar}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM users")
    java.util.List<User> findAll();

    @Select("SELECT count(*) FROM users")
    long count();

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);

    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    void updateRole(@Param("id") Long id, @Param("role") String role);

    @Update("UPDATE users SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    void update(User user);
}

