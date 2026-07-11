package com.library.service.impl;

import com.library.common.ResultCode;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Borrow Service Implementation
 *
 * Core business logic with transaction management:
 * - borrowBook: decrements book stock + creates borrow record (atomic)
 * - returnBook: marks return + increments stock + calculates fine (atomic)
 * - renewBook: extends due date
 *
 * All multi-table operations are wrapped in @Transactional to prevent data inconsistency.
 */
@Slf4j
@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    private static final int MAX_BORROW_LIMIT = 5;
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final int MAX_RENEW_COUNT = 2;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    /**
     * Borrow a book
     * Transaction wraps: borrow record creation + book stock decrement
     * If either fails, the entire operation rolls back
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 1. Validate user exists and is active
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 2. Check borrow limit
        int activeCount = borrowRecordMapper.countActiveBorrows(userId);
        if (activeCount >= MAX_BORROW_LIMIT) {
            throw new BusinessException(ResultCode.BORROW_LIMIT_EXCEEDED);
        }

        // 3. Check for overdue books or unpaid fines
        List<BorrowRecord> activeBorrows = borrowRecordMapper.selectActiveBorrowsByUser(userId);
        boolean hasOverdue = activeBorrows.stream().anyMatch(r -> r.getStatus() == 2);
        if (hasOverdue) {
            throw new BusinessException(ResultCode.BORROW_OVERDUE);
        }
        boolean hasUnpaidFine = activeBorrows.stream()
                .anyMatch(r -> r.getFineStatus() != null && r.getFineStatus() == 1);
        if (hasUnpaidFine) {
            throw new BusinessException(ResultCode.FINE_UNPAID);
        }

        // 4. Validate book
        Book book = bookMapper.selectBookById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        if (book.getStatus() != null && book.getStatus() == 0) {
            throw new BusinessException(ResultCode.BOOK_OFF_SHELF);
        }

        // 5. Decrement available count (atomic SQL, returns 0 if out of stock)
        int rows = bookMapper.decrementAvailableCount(bookId);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BOOK_OUT_OF_STOCK);
        }

        // 6. Create borrow record
        BorrowRecord record = new BorrowRecord();
        record.setRecordNo(generateRecordNo());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(LOAN_PERIOD_DAYS));
        record.setStatus(0);
        record.setRenewCount(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFineStatus(0);

        borrowRecordMapper.insert(record);
        log.info("Book borrowed: user={}, book={}, record={}", userId, bookId, record.getRecordNo());

        return borrowRecordMapper.selectRecordById(record.getId());
    }

    /**
     * Return a book
     * Transaction wraps: stock increment + record update + fine calculation
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecord returnBook(Long recordId) {
        // 1. Get record
        BorrowRecord record = borrowRecordMapper.selectRecordById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_FOUND);
        }

        // 2. Check if already returned
        if (record.getStatus() == 1) {
            throw new BusinessException(ResultCode.BORROW_ALREADY_RETURNED);
        }

        // 3. Calculate fine if overdue
        LocalDateTime now = LocalDateTime.now();
        BigDecimal fine = BigDecimal.ZERO;
        int fineStatus = 0;

        if (now.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate().toLocalDate(), now.toLocalDate());
            fine = FINE_PER_DAY.multiply(new BigDecimal(overdueDays));
            fineStatus = 1; // pending payment
            log.info("Overdue return: record={}, overdueDays={}, fine={}", record.getRecordNo(), overdueDays, fine);
        }

        // 4. Update record
        record.setReturnDate(now);
        record.setStatus(1);
        record.setFineAmount(fine);
        record.setFineStatus(fineStatus);
        borrowRecordMapper.updateById(record);

        // 5. Increment book stock
        bookMapper.incrementAvailableCount(record.getBookId());

        log.info("Book returned: record={}, fine={}", record.getRecordNo(), fine);
        return borrowRecordMapper.selectRecordById(recordId);
    }

    /**
     * Renew a book (extend due date by LOAN_PERIOD_DAYS)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecord renewBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectRecordById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_FOUND);
        }
        if (record.getStatus() == 1) {
            throw new BusinessException(ResultCode.BORROW_ALREADY_RETURNED);
        }
        if (record.getRenewCount() >= MAX_RENEW_COUNT) {
            throw new BusinessException(ResultCode.RENEW_LIMIT_EXCEEDED);
        }

        // Extend due date from current due date (or from now if already overdue)
        LocalDateTime baseDate = record.getDueDate().isAfter(LocalDateTime.now())
                ? record.getDueDate()
                : LocalDateTime.now();
        record.setDueDate(baseDate.plusDays(LOAN_PERIOD_DAYS));
        record.setRenewCount(record.getRenewCount() + 1);
        // If was overdue, reset to borrowing status
        if (record.getStatus() == 2) {
            record.setStatus(0);
        }

        borrowRecordMapper.updateById(record);
        log.info("Book renewed: record={}, newDueDate={}", record.getRecordNo(), record.getDueDate());
        return borrowRecordMapper.selectRecordById(recordId);
    }

    @Override
    public Map<String, Object> getRecordPage(int pageNum, int pageSize, Long userId, Long bookId, Integer status, String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BorrowRecord> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.metadata.IPage<BorrowRecord> result =
                borrowRecordMapper.selectRecordPage(page, userId, bookId, status, keyword);

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("pageNum", result.getCurrent());
        map.put("pageSize", result.getSize());
        map.put("records", result.getRecords());
        return map;
    }

    @Override
    public BorrowRecord getRecordById(Long id) {
        BorrowRecord record = borrowRecordMapper.selectRecordById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_FOUND);
        }
        return record;
    }

    @Override
    public List<BorrowRecord> getActiveBorrows(Long userId) {
        return borrowRecordMapper.selectActiveBorrowsByUser(userId);
    }

    /**
     * Scheduled task: check and mark overdue records
     * @return number of records marked as overdue
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndMarkOverdue() {
        List<BorrowRecord> overdueRecords = borrowRecordMapper.selectOverdueUnmarked();
        if (overdueRecords.isEmpty()) {
            return 0;
        }
        List<Long> ids = overdueRecords.stream()
                .map(BorrowRecord::getId)
                .collect(Collectors.toList());
        int updated = borrowRecordMapper.batchMarkOverdue(ids);

        // Calculate and set fines for overdue records
        for (BorrowRecord record : overdueRecords) {
            long overdueDays = ChronoUnit.DAYS.between(
                    record.getDueDate().toLocalDate(), LocalDateTime.now().toLocalDate());
            if (overdueDays > 0) {
                BigDecimal fine = FINE_PER_DAY.multiply(new BigDecimal(overdueDays));
                record.setFineAmount(fine);
                record.setFineStatus(1);
                borrowRecordMapper.updateById(record);
            }
        }

        log.info("Marked {} records as overdue", updated);
        return updated;
    }

    /**
     * Pay fine for a record
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecord payFine(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectRecordById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_FOUND);
        }
        if (record.getFineStatus() == null || record.getFineStatus() != 1) {
            throw new BusinessException("该记录无待缴罚款");
        }
        record.setFineStatus(2); // paid
        borrowRecordMapper.updateById(record);
        log.info("Fine paid: record={}, amount={}", record.getRecordNo(), record.getFineAmount());
        return borrowRecordMapper.selectRecordById(recordId);
    }

    private String generateRecordNo() {
        return "BR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
