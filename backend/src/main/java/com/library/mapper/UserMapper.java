package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * Paginated query with role info joined
     */
    IPage<User> selectUserPage(Page<User> page, @Param("keyword") String keyword);

    /**
     * Get user by username (with role info)
     */
    User selectByUsername(@Param("username") String username);

    /**
     * Get user by id (with role info)
     */
    User selectUserById(@Param("id") Long id);

    /**
     * Get all readers (role_id = 3)
     */
    List<User> selectAllReaders();
}
