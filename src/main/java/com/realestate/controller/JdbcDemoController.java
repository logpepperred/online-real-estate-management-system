package com.realestate.controller;

import com.realestate.concurrency.SystemMetricsTracker;
import com.realestate.jdbc.PropertyJdbcDao;
import com.realestate.util.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating JDBC database operations, Multithreading metrics,
 * and Generics (ApiResponse<T>) for project review.
 */
@RestController
@RequestMapping("/api/jdbc")
public class JdbcDemoController {

    private final PropertyJdbcDao propertyJdbcDao;
    private final SystemMetricsTracker metricsTracker;

    public JdbcDemoController(PropertyJdbcDao propertyJdbcDao, SystemMetricsTracker metricsTracker) {
        this.propertyJdbcDao = propertyJdbcDao;
        this.metricsTracker = metricsTracker;
    }

    /**
     * Demonstrates JDBC PreparedStatement execution & Generic response wrapper.
     */
    @GetMapping("/filter")
    public ApiResponse<List<Map<String, Object>>> filterByPriceJdbc(
            @RequestParam(value = "min", defaultValue = "0") double min,
            @RequestParam(value = "max", defaultValue = "100000") double max) {

        metricsTracker.incrementSearchQueries();
        List<Map<String, Object>> results = propertyJdbcDao.findPropertiesByPriceRange(min, max);
        return ApiResponse.success("Properties fetched via direct JDBC PreparedStatement", results);
    }

    /**
     * Demonstrates JDBC Aggregate calculation and Multithreading counters.
     */
    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getLiveSystemMetrics() {
        metricsTracker.incrementPageRequests();

        Map<String, Object> combinedMetrics = new HashMap<>();
        combinedMetrics.put("jdbcFinancialSummary", propertyJdbcDao.getFinancialSummaryJdbc());

        Map<String, Object> threadMetrics = new HashMap<>();
        threadMetrics.put("totalPageRequests", metricsTracker.getTotalPageRequests());
        threadMetrics.put("totalSearchQueries", metricsTracker.getTotalSearchQueries());
        threadMetrics.put("activeUserSessions", metricsTracker.getActiveUserSessions());
        threadMetrics.put("trackerStatus", "Thread-Safe AtomicLong & Synchronized Lock Active");

        combinedMetrics.put("concurrencyMetrics", threadMetrics);

        return ApiResponse.success("Live JDBC & Concurrency Metrics computed successfully", combinedMetrics);
    }
}
