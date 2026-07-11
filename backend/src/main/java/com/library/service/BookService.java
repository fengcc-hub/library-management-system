package com.library.service;

import com.library.entity.Book;

import java.util.List;
import java.util.Map;

public interface BookService {

    /** Paginated book list with search */
    Map<String, Object> getBookPage(int pageNum, int pageSize, String keyword, Long categoryId, String author);

    /** Get book by id (with cache) */
    Book getBookById(Long id);

    /** Add book */
    Book addBook(Book book);

    /** Update book */
    Book updateBook(Book book);

    /** Delete book (logical) */
    void deleteBook(Long id);

    /** Get hot books (from Redis cache with DB fallback) */
    List<Book> getHotBooks();

    /** Refresh hot book cache */
    void refreshHotBooksCache();

    /** Get book detail by ISBN */
    Book getBookByIsbn(String isbn);
}
