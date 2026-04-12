/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java - Singleton pattern for JDBC MySQL connection.
 * Ensures only one Connection object is reused throughout the app lifecycle.
 * NOTE: For production/multi-threaded use, consider a connection pool (e.g. c3p0 or HikariCP).
 */
public class DBConnection {

    // --- Database Configuration ---
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/civic_issues_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "password";

    // Singleton instance
    private static DBConnection instance = null;
    private Connection connection        = null;

    /**
     * Private constructor — loads the MySQL JDBC driver and opens connection.
     */
    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL Connector/J 8.x driver
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DBConnection] MySQL connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found: " + e.getMessage());
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to connect to the database.", e);
        }
    }

    /**
     * Returns the singleton DBConnection instance (lazy initialization).
     * Re-creates connection if it has been closed.
     */
    public static DBConnection getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error checking connection state: " + e.getMessage());
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Returns the active java.sql.Connection object for use in DAOs.
     */
    public Connection getConnection() {
        return connection;
    }
}