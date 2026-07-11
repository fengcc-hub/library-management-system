package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * Get all categories with book count
     */
    List<Category> selectAllWithBookCount();

    /**
     * Get category by id with book count
     */
    Category selectCategoryById(@Param("id") Long id);

    /**
     * Get categories by parent id
     */
    List<Category> selectByParentId(@Param("parentId") Long parentId);
}
