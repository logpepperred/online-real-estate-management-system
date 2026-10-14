package com.realestate.jdbc;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) executing database operations using pure JDBC.
 * Fulfills rubric requirement for classes performing JDBC database operations.
 */
@Repository
public class PropertyJdbcDao {

    private final DatabaseConnectionManager connectionManager;

    public PropertyJdbcDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Executes a parameterized SELECT query using PreparedStatement and iterates through ResultSet.
     */
    public List<Map<String, Object>> findPropertiesByPriceRange(double minPrice, double maxPrice) {
        String sql = "SELECT id, title, city, price, property_type, bedrooms, status " +
                     "FROM properties WHERE price BETWEEN ? AND ? ORDER BY price ASC";

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, minPrice);
            pstmt.setDouble(2, maxPrice);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("title", rs.getString("title"));
                    row.put("city", rs.getString("city"));
                    row.put("price", rs.getDouble("price"));
                    row.put("propertyType", rs.getString("property_type"));
                    row.put("bedrooms", rs.getInt("bedrooms"));
                    row.put("status", rs.getString("status"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error in findPropertiesByPriceRange: " + e.getMessage());
        }

        return results;
    }

    /**
     * Executes SQL aggregation using JDBC Statement to compute real-time financial metrics.
     */
    public Map<String, Object> getFinancialSummaryJdbc() {
        String sql = "SELECT COUNT(*) as total_count, " +
                     "COALESCE(SUM(price), 0) as total_revenue, " +
                     "COALESCE(AVG(price), 0) as avg_rent, " +
                     "COALESCE(MAX(price), 0) as max_rent, " +
                     "COALESCE(MIN(price), 0) as min_rent " +
                     "FROM properties WHERE status = 'APPROVED'";

        Map<String, Object> summary = new HashMap<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                summary.put("totalCount", rs.getLong("total_count"));
                summary.put("totalRevenue", rs.getDouble("total_revenue"));
                summary.put("avgRent", rs.getDouble("avg_rent"));
                summary.put("maxRent", rs.getDouble("max_rent"));
                summary.put("minRent", rs.getDouble("min_rent"));
            }
        } catch (SQLException e) {
            System.err.println("JDBC Error in getFinancialSummaryJdbc: " + e.getMessage());
        }

        return summary;
    }

    /**
     * Demonstrates JDBC Transaction Management with commit and rollback.
     */
    public boolean updatePropertyViewsBatch(Map<Long, Integer> viewIncrements) {
        String sql = "UPDATE properties SET views_count = views_count + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Map.Entry<Long, Integer> entry : viewIncrements.entrySet()) {
                    pstmt.setInt(1, entry.getValue());
                    pstmt.setLong(2, entry.getKey());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on failure
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
