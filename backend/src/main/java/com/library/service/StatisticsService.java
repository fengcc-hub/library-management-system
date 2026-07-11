package com.library.service;

import java.util.Map;

public interface StatisticsService {

    /** Dashboard overview stats */
    Map<String, Object> getOverview();

    /** Monthly borrow trend (last N months) */
    Map<String, Object> getMonthlyBorrowTrend(int months);

    /** Daily borrow trend (last N days) */
    Map<String, Object> getDailyBorrowTrend(int days);

    /** Book count by category (pie chart data) */
    Map<String, Object> getCategoryDistribution();

    /** Top borrowed books */
    Map<String, Object> getTopBorrowedBooks(int limit);

    /** Reader activity ranking */
    Map<String, Object> getReaderActivityRanking(int limit);
}
