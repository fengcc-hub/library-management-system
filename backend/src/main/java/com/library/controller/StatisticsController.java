package com.library.controller;

import com.library.common.Result;
import com.library.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Statistics Controller
 * Provides data for ECharts visualizations:
 * - Dashboard overview
 * - Borrow trend charts
 * - Category distribution pie chart
 * - Top borrowed books bar chart
 * - Reader activity ranking
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/borrow/monthly")
    public Result<Map<String, Object>> monthlyTrend(@RequestParam(defaultValue = "6") int months) {
        return Result.success(statisticsService.getMonthlyBorrowTrend(months));
    }

    @GetMapping("/borrow/daily")
    public Result<Map<String, Object>> dailyTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.success(statisticsService.getDailyBorrowTrend(days));
    }

    @GetMapping("/category-distribution")
    public Result<Map<String, Object>> categoryDistribution() {
        return Result.success(statisticsService.getCategoryDistribution());
    }

    @GetMapping("/top-books")
    public Result<Map<String, Object>> topBooks(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getTopBorrowedBooks(limit));
    }

    @GetMapping("/reader-ranking")
    public Result<Map<String, Object>> readerRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getReaderActivityRanking(limit));
    }
}
