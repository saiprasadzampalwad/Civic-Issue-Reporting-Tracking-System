/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.dao;

import com.civicissues.model.User;
import com.civicissues.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO.java - Data Access Object for the `Users` table.
 *
 * SECURITY NOTE: Passwords should be hashed with BCrypt before storage.
 * This DAO compares the incoming password against the stored hash.
 * For simplicity here we use MD5/SHA via MySQL or plain comparison —
 * in production replace with BCrypt (e.g. mindrot jBCrypt library).
 */
public class UserDAO {

    private Connection conn;

    public UserDAO() {
        // Obtain the singleton JDBC connection
        this.conn = DBConnection.getInstance().getConnection();
    }

    /**
     * Authenticates a user by email and password.
     * Uses PreparedStatement to prevent SQL injection.
     *
     * @param email    the user's email address
     * @param password the plain-text password submitted by the login form
     * @return User object if credentials are valid, null otherwise
     */
    public User authenticateUser(String email, String password) {
        // We compare against password_hash stored in DB.
        // In production: hash the incoming password with BCrypt and compare.
        // Here we use MD5 via MySQL's MD5() function as a simple demonstration.
        String sql = "SELECT user_id, name, phone, email, role, department_id "
                   + "FROM Users "
                   + "WHERE email = ? AND password_hash = MD5(?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Credentials matched — map ResultSet to User POJO
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setPhone(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setDepartmentId(rs.getInt("department_id"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticateUser error: " + e.getMessage());
        }

        return null; // Authentication failed
    }

    /**
     * Fetches a single user by their primary key.
     * Useful for looking up crew member names during status updates.
     *
     * @param userId the PK of the user
     * @return User POJO or null if not found
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, name, phone, email, role, department_id "
                   + "FROM Users WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setPhone(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setDepartmentId(rs.getInt("department_id"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }
}