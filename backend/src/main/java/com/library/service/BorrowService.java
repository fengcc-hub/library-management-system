package com.library.service;

import com.library.entity.BorrowRecord;

import java.util.List;
import java.util.Map;

public interface BorrowService {

    /** Borrow a book (transactional) */
    BorrowRecord borrowBook(Long userId, Long bookId);

    /** Return a book (transactional, with fine calculation) */
    BorrowRecord returnBook(Long recordId);

    /** Renew a book (extend due date) */
    BorrowRecord renewBook(Long recordId);

    /** Paginated borrow records */
    Map<String, Object> getRecordPage(int pageNum, int pageSize, Long userId, Long bookId, Integer status, String keyword);

    /** Get record by id */
    BorrowRecord getRecordById(Long id);

    /** Get active borrows for a user */
    List<BorrowRecord> getActiveBorrows(Long userId);

    /** Check and mark overdue records (scheduled) */
    int checkAndMarkOverdue();

    /** Pay fine */
    BorrowRecord payFine(Long recordId);
}
