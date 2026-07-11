package com.library.controller;

import com.library.common.Result;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Borrow Management Controller
 *
 * Handles borrowing, returning, renewing, and fine payment
 */
@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    /**
     * Borrow a book (all authenticated users)
     */
    @PostMapping("/borrow")
    public Result<BorrowRecord> borrow(@RequestBody Map<String, Long> body, Authentication auth) {
        Long userId = body.get("userId");
        Long bookId = body.get("bookId");
        if (userId == null) {
            // Use current user
            return Result.error("请指定读者");
        }
        BorrowRecord record = borrowService.borrowBook(userId, bookId);
        return Result.success("借阅成功", record);
    }

    /**
     * Return a book
     */
    @PutMapping("/return/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<BorrowRecord> returnBook(@PathVariable Long recordId) {
        BorrowRecord record = borrowService.returnBook(recordId);
        return Result.success("归还成功", record);
    }

    /**
     * Renew a book
     */
    @PutMapping("/renew/{recordId}")
    public Result<BorrowRecord> renewBook(@PathVariable Long recordId) {
        BorrowRecord record = borrowService.renewBook(recordId);
        return Result.success("续借成功", record);
    }

    /**
     * Pay fine
     */
    @PutMapping("/pay-fine/{recordId}")
    public Result<BorrowRecord> payFine(@PathVariable Long recordId) {
        BorrowRecord record = borrowService.payFine(recordId);
        return Result.success("罚款已缴清", record);
    }

    /**
     * Paginated borrow records
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(borrowService.getRecordPage(pageNum, pageSize, userId, bookId, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<BorrowRecord> getById(@PathVariable Long id) {
        return Result.success(borrowService.getRecordById(id));
    }

    /**
     * Get active borrows for a user
     */
    @GetMapping("/active/{userId}")
    public Result<List<BorrowRecord>> getActiveBorrows(@PathVariable Long userId) {
        return Result.success(borrowService.getActiveBorrows(userId));
    }

    /**
     * Check overdue (admin/manual trigger)
     */
    @PostMapping("/check-overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Integer> checkOverdue() {
        int count = borrowService.checkAndMarkOverdue();
        return Result.success("检查完成，标记" + count + "条逾期记录", count);
    }
}
