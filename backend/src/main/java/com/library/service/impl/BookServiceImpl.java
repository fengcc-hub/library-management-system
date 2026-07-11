package com.library.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.ResultCode;
import com.library.entity.Book;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.key.hot-book}")
    private String hotBookKey;

    private static final int HOT_BOOK_CACHE_LIMIT = 10;
    private static final long HOT_BOOK_CACHE_TTL = 30; // minutes

    @Override
    public Map<String, Object> getBookPage(int pageNum, int pageSize, String keyword, Long categoryId, String author) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        IPage<Book> result = bookMapper.selectBookPage(page, keyword, categoryId, author);

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("pageNum", result.getCurrent());
        map.put("pageSize", result.getSize());
        map.put("records", result.getRecords());
        return map;
    }

    @Override
    public Book getBookById(Long id) {
        Book book = bookMapper.selectBookById(id);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        return book;
    }

    @Override
    @Transactional
    public Book addBook(Book book) {
        // Validate ISBN uniqueness
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsbn, book.getIsbn());
        if (bookMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("ISBN已存在");
        }
        if (book.getAvailableCount() == null) {
            book.setAvailableCount(book.getTotalCount());
        }
        if (book.getStatus() == null) {
            book.setStatus(1);
        }
        bookMapper.insert(book);
        // Invalidate hot book cache
        invalidateHotBooksCache();
        return bookMapper.selectBookById(book.getId());
    }

    @Override
    @Transactional
    public Book updateBook(Book book) {
        Book existing = bookMapper.selectBookById(book.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        // Ensure available_count <= total_count
        if (book.getTotalCount() != null && book.getAvailableCount() != null
                && book.getAvailableCount() > book.getTotalCount()) {
            book.setAvailableCount(book.getTotalCount());
        }
        bookMapper.updateById(book);
        invalidateHotBooksCache();
        return bookMapper.selectBookById(book.getId());
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookMapper.selectBookById(id);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        bookMapper.deleteById(id);
        invalidateHotBooksCache();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Book> getHotBooks() {
        // Try Redis cache first
        Object cached = redisTemplate.opsForValue().get(hotBookKey);
        if (cached != null) {
            log.debug("Hot books cache hit");
            return (List<Book>) cached;
        }

        // Cache miss: query DB
        log.debug("Hot books cache miss, querying DB");
        List<Book> hotBooks = bookMapper.selectHotBooks(HOT_BOOK_CACHE_LIMIT);

        // Store in Redis with TTL
        redisTemplate.opsForValue().set(hotBookKey, hotBooks, HOT_BOOK_CACHE_TTL, TimeUnit.MINUTES);
        return hotBooks;
    }

    @Override
    public void refreshHotBooksCache() {
        log.info("Manually refreshing hot books cache");
        List<Book> hotBooks = bookMapper.selectHotBooks(HOT_BOOK_CACHE_LIMIT);
        redisTemplate.opsForValue().set(hotBookKey, hotBooks, HOT_BOOK_CACHE_TTL, TimeUnit.MINUTES);
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsbn, isbn);
        Book book = bookMapper.selectOne(wrapper);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_FOUND);
        }
        return bookMapper.selectBookById(book.getId());
    }

    private void invalidateHotBooksCache() {
        redisTemplate.delete(hotBookKey);
        log.debug("Hot books cache invalidated");
    }
}
