/*
 * Updated for Phone Auth - No Email/Verification/Reset
 */
package com.civicissues.dao;

import com.civicissues.model.User;
import com.civicissues.util.BCryptUtil;
import com.civicissues.util.DBConnection;
import java.sql.*;

/**
 * UserDAO.java - Phone auth, registration, lockout (no email/verify/reset).
 */
public class UserDAO {

    private static final int MAX_FAILED   = 5;
    private static final int LOCK_MINUTES = 15;

    private Connection conn;
    public UserDAO() { this.conn = DBConnection.getInstance().getConnection(); }

    // -----------------------------------------------------------------------
    // Authenticate with BCrypt + lockout support (by phone)
    // -----------------------------------------------------------------------
    public User authenticateUser(String phone, String password) {
        String sql = "SELECT * FROM Users WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User user = mapRow(rs);

                // Check account lock
                if (user.getLockedUntil() != null &&
                    user.getLockedUntil().after(new Timestamp(System.currentTimeMillis()))) {
                    return null; // Still locked
                }

                // BCrypt password check
                if (!BCryptUtil.verify(password, user.getPasswordHash())) {
                    incrementFailedAttempts(user.getUserId(), user.getFailedAttempts() + 1);
                    return null;
                }

                // Success - reset failed attempts
                resetFailedAttempts(user.getUserId());
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticateUser: " + e.getMessage());
        }
        return null;
    }

    private void incrementFailedAttempts(int userId, int newCount) {
        String sql = newCount >= MAX_FAILED
            ? "UPDATE Users SET failed_attempts=?, locked_until=DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE user_id=?"
            : "UPDATE Users SET failed_attempts=? WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCount);
            if (newCount >= MAX_FAILED) { ps.setInt(2, LOCK_MINUTES); ps.setInt(3, userId); }
            else ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[UserDAO] incrementFailed: " + e.getMessage()); }
    }

    private void resetFailedAttempts(int userId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Users SET failed_attempts=0, locked_until=NULL WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[UserDAO] resetFailed: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Register new citizen (phone auth, auto active)
    // -----------------------------------------------------------------------
    public boolean registerUser(String name, String phone, String password) {
        String sql = "INSERT INTO Users (name,phone,password_hash,role) VALUES (?,?,?,'CITIZEN')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, BCryptUtil.hash(password));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] registerUser: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Register admin/crew user with role and optional department
    // -----------------------------------------------------------------------
    public boolean registerAdmin(String name, String phone, String password, String role, Integer departmentId) {
        if (name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        if (phoneExists(phone)) {
            return false;
        }
        if (password.length() < 6) {
            return false;
        }

        String[] validRoles = {"CITIZEN", "MUNICIPAL_ADMIN", "MAINTENANCE_CREW"};
        boolean validRole = false;
        for (String r : validRoles) {
            if (r.equals(role)) {
                validRole = true;
                break;
            }
        }
        if (!validRole) {
            return false;
        }

        if (!"CITIZEN".equals(role) && departmentId == null) {
            return false; // Require dept for admins/crews
        }

        String sql = "INSERT INTO Users (name, phone, password_hash, role, department_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, phone.trim());
            ps.setString(3, BCryptUtil.hash(password));
            ps.setString(4, role);
            if (departmentId != null) {
                ps.setInt(5, departmentId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] registerAdmin: " + e.getMessage());
            return false;
        }
    }

    public boolean phoneExists(String phone) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Users WHERE phone=?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    public User getUserById(int userId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapRow(rs); }
        } catch (SQLException e) { System.err.println("[UserDAO] getById: " + e.getMessage()); }
        return null;
    }

    public User getUserByPhone(String phone) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE phone=?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapRow(rs); }
        } catch (SQLException e) { System.err.println("[UserDAO] getByPhone: " + e.getMessage()); }
        return null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setDepartmentId(rs.getInt("department_id"));
        u.setFailedAttempts(rs.getInt("failed_attempts"));
        u.setLockedUntil(rs.getTimestamp("locked_until"));
        return u;
    }
}

