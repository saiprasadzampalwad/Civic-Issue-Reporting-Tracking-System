package com.civicissues.dao;

import com.civicissues.util.DBConnection;
import java.sql.*;
import java.util.*;

public class DepartmentDAO {

    private Connection conn;
    public DepartmentDAO() { this.conn = DBConnection.getInstance().getConnection(); }

    public List<Map<String, Object>> getAllDepartments() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT department_id, department_name FROM Departments ORDER BY department_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id",   rs.getInt("department_id"));
                row.put("name", rs.getString("department_name"));
                list.add(row);
            }
        } catch (SQLException e) { System.err.println("[DeptDAO] getAll: " + e.getMessage()); }
        return list;
    }

    public List<Map<String, Object>> getCrewByDepartment(int deptId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT user_id, name FROM Users WHERE role='MAINTENANCE_CREW' AND department_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",   rs.getInt("user_id"));
                    row.put("name", rs.getString("name"));
                    list.add(row);
                }
            }
        } catch (SQLException e) { System.err.println("[DeptDAO] getCrew: " + e.getMessage()); }
        return list;
    }

    public List<Map<String, Object>> getAllCrew() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT user_id, name FROM Users WHERE role='MAINTENANCE_CREW' ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id",   rs.getInt("user_id"));
                row.put("name", rs.getString("name"));
                list.add(row);
            }
        } catch (SQLException e) { System.err.println("[DeptDAO] getAllCrew: " + e.getMessage()); }
        return list;
    }
}
