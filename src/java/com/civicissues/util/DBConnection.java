package com.civicissues.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * DBConnection.java - Singleton JDBC connection
 * Compatible with: NetBeans 8.2 + MySQL 8.x + Connector/J 8.x
 */
public class DBConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/civic_issues_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&useUnicode=true&characterEncoding=UTF-8";

    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private static DBConnection instance = null;
    private Connection connection = null;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DBConnection] MySQL connection OK");
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    public static DBConnection getInstance() {
        if (instance == null || instance.connection == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
