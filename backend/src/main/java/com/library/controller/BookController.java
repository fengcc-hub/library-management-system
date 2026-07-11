package com.library.controller;

import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Book Management Controller
 */
@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String author) {
        return Result.success(bookService.getBookPage(pageNum, pageSize, keyword, categoryId, author));
    }

    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    public Result<Book> getByIsbn(@PathVariable String isbn) {
        return Result.success(bookService.getBookByIsbn(isbn));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Book> add(@RequestBody Book book) {
        return Result.success("添加成功", bookService.addBook(book));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Book> update(@RequestBody Book book) {
        return Result.success("更新成功", bookService.updateBook(book));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @GetMapping("/hot")
    public Result<List<Book>> getHotBooks() {
        return Result.success(bookService.getHotBooks());
    }

    @PostMapping("/hot/refresh")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> refreshHotBooks() {
        bookService.refreshHotBooksCache();
        return Result.success();
    }
}
