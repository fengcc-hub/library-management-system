package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * Paginated query with category name joined
     */
    IPage<Book> selectBookPage(Page<Book> page, @Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("author") String author);

    /**
     * Get book by id with category name
     */
    Book selectBookById(@Param("id") Long id);

    /**
     * Decrement available count (atomic operation)
     * @return affected rows (0 means out of stock)
     */
    int decrementAvailableCount(@Param("id") Long id);

    /**
     * Increment available count (on return)
     */
    int incrementAvailableCount(@Param("id") Long id);

    /**
     * Get hot books (most borrowed) - used for Redis cache fallback
     */
    List<Book> selectHotBooks(@Param("limit") int limit);

    /**
     * Get book count by category (for statistics)
     */
    List<Map<String, Object>> selectCountByCategory();
}
