package com.newshub.backend.infrastructure.persistence;

import com.newshub.backend.domain.model.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM categories")
    List<Category> findAll();

    @Select("SELECT * FROM categories WHERE id = #{id}")
    Category findById(Long id);
    
    @Select("SELECT * FROM categories WHERE slug = #{slug}")
    Category findBySlug(String slug);
}
