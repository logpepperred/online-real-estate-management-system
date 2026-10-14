package com.realestate.jdbc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Demonstrates Database Connectivity using standard JDBC API (java.sql.*).
 * Implements Connection Factory pattern supporting both DataSource and direct DriverManager.
 */
@Component
public class DatabaseConnectionManager {

    private final DataSource dataSource;

    @Value("${spring.datasource.url:jdbc:h2:file:./data/realestatedb}")
    private String dbUrl;

    @Value("${spring.datasource.username:sa}")
    private String dbUser;

    @Value("${spring.datasource.password:password}")
    private String dbPassword;

    public DatabaseConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Obtains a standard JDBC Connection from the application DataSource.
     * @return java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        // Direct DriverManager fallback demonstrating explicit JDBC driver loading
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Direct connection using DriverManager.getConnection explicitly.
     */
    public Connection getDirectDriverConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 JDBC Driver not found", e);
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
