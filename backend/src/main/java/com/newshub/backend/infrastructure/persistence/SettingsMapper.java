package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.Settings;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SettingsMapper {
    @Select("SELECT * FROM settings")
    List<Settings> findAll();

    @Select("SELECT * FROM settings WHERE `key` = #{key}")
    Settings findByKey(String key);

    @Insert("INSERT INTO settings(`key`, `value`, updated_at) VALUES(#{key}, #{value}, NOW()) " +
            "ON DUPLICATE KEY UPDATE `value` = #{value}, updated_at = NOW()")
    int save(Settings settings);
}
