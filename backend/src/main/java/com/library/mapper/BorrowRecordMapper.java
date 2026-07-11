package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * Paginated query with user and book info joined
     */
    IPage<BorrowRecord> selectRecordPage(Page<BorrowRecord> page,
                                         @Param("userId") Long userId,
                                         @Param("bookId") Long bookId,
                                         @Param("status") Integer status,
                                         @Param("keyword") String keyword);

    /**
     * Get record by id with joined info
     */
    BorrowRecord selectRecordById(@Param("id") Long id);

    /**
     * Get active borrow records for a user (status 0 or 2)
     */
    List<BorrowRecord> selectActiveBorrowsByUser(@Param("userId") Long userId);

    /**
     * Count active borrows for a user
     */
    int countActiveBorrows(@Param("userId") Long userId);

    /**
     * Get records that are overdue but not yet marked (status = 0 and due_date < now)
     */
    List<BorrowRecord> selectOverdueUnmarked();

    /**
     * Batch update overdue records to status = 2
     */
    int batchMarkOverdue(@Param("ids") List<Long> ids);

    /**
     * Monthly borrow count statistics (last N months)
     */
    List<Map<String, Object>> selectMonthlyBorrowCount(@Param("months") int months);

    /**
     * Daily borrow count statistics (last N days)
     */
    List<Map<String, Object>> selectDailyBorrowCount(@Param("days") int days);

    /**
     * Top borrowed books
     */
    List<Map<String, Object>> selectTopBorrowedBooks(@Param("limit") int limit);

    /**
     * Reader activity ranking
     */
    List<Map<String, Object>> selectReaderActivityRanking(@Param("limit") int limit);

    /**
     * Overall statistics: total borrows, active borrows, overdue count
     */
    Map<String, Object> selectOverallStats();
}
