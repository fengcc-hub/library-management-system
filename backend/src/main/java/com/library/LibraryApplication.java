package com.library;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.library.mapper")
@EnableTransactionManagement
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  Library Management System Started!\n" +
                "  Backend API: http://localhost:8080/api\n" +
                "========================================\n");
    }
}
