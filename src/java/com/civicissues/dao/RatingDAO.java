/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.dao;

import com.civicissues.util.DBConnection;
import java.sql.*;

public class RatingDAO {

    private Connection conn;
    public RatingDAO() { this.conn = DBConnection.getInstance().getConnection(); }

    public boolean submitRating(int issueId, int citizenId, int stars, String feedback) {
        String sql = "INSERT INTO Ratings (issue_id, citizen_id, stars, feedback) VALUES (?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE stars=VALUES(stars), feedback=VALUES(feedback)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issueId); ps.setInt(2, citizenId);
            ps.setInt(3, stars);   ps.setString(4, feedback);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RatingDAO] submit: " + e.getMessage());
            return false;
        }
    }

    /** Average star rating per department for admin dashboard. */
    public java.util.List<java.util.Map<String, Object>> getAvgRatingByDepartment() {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        String sql = "SELECT d.department_name, ROUND(AVG(r.stars),1) AS avg_stars, COUNT(*) AS total " +
                     "FROM Ratings r " +
                     "JOIN Issues i ON r.issue_id = i.issue_id " +
                     "JOIN Departments d ON i.assigned_department_id = d.department_id " +
                     "GROUP BY d.department_name ORDER BY avg_stars DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("department", rs.getString("department_name"));
                row.put("avgStars",   rs.getObject("avg_stars"));
                row.put("total",      rs.getInt("total"));
                list.add(row);
            }
        } catch (SQLException e) { System.err.println("[RatingDAO] avgByDept: " + e.getMessage()); }
        return list;
    }
}