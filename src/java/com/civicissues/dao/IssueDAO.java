/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.dao;

import com.civicissues.model.Issue;
import com.civicissues.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * IssueDAO.java - Data Access Object for the `Issues` and `Status_Updates` tables.
 *
 * Key design decisions:
 *  - All queries use PreparedStatement to prevent SQL injection.
 *  - updateIssueStatus() uses a manual JDBC transaction (commit/rollback)
 *    to atomically update Issues and insert into Status_Updates.
 */
public class IssueDAO {

    private Connection conn;

    public IssueDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // -----------------------------------------------------------------------
    // 1. createIssue — INSERT a new civic issue reported by a citizen
    // -----------------------------------------------------------------------
    /**
     * Inserts a new Issue into the database.
     *
     * @param issue  populated Issue POJO (citizenId, category, gpsLocation,
     *               description, photoUrl must be set; status defaults to 'OPEN')
     * @return true if insert succeeded, false otherwise
     */
    public boolean createIssue(Issue issue) {
        String sql = "INSERT INTO Issues "
                   + "(citizen_id, category, gps_location, description, photo_url, status, "
                   + " assigned_department_id, assigned_crew_id) "
                   + "VALUES (?, ?, ?, ?, ?, 'OPEN', NULL, NULL)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issue.getCitizenId());
            ps.setString(2, issue.getCategory());
            ps.setString(3, issue.getGpsLocation());
            ps.setString(4, issue.getDescription());
            ps.setString(5, issue.getPhotoUrl());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("[IssueDAO] createIssue error: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // 2. getAllOpenIssues — for the Admin Dashboard
    // -----------------------------------------------------------------------
    /**
     * Retrieves all issues with status OPEN or ASSIGNED (active issues).
     * Uses LEFT JOINs to also fetch citizen name and department name.
     *
     * @return List of Issue POJOs with display fields populated
     */
    public List<Issue> getAllOpenIssues() {
        List<Issue> issues = new ArrayList<>();

        String sql = "SELECT i.issue_id, i.citizen_id, i.category, i.gps_location, "
                   + "       i.description, i.photo_url, i.status, "
                   + "       i.assigned_department_id, i.assigned_crew_id, "
                   + "       u.name AS citizen_name, "
                   + "       d.department_name "
                   + "FROM Issues i "
                   + "LEFT JOIN Users u ON i.citizen_id = u.user_id "
                   + "LEFT JOIN Departments d ON i.assigned_department_id = d.department_id "
                   + "WHERE i.status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS') "
                   + "ORDER BY i.issue_id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                issues.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[IssueDAO] getAllOpenIssues error: " + e.getMessage());
        }

        return issues;
    }

    // -----------------------------------------------------------------------
    // 3. getIssuesByCitizen — for the Citizen Dashboard
    // -----------------------------------------------------------------------
    /**
     * Retrieves all issues reported by a specific citizen.
     *
     * @param citizenId  the user_id of the logged-in citizen
     * @return List of Issue POJOs
     */
    public List<Issue> getIssuesByCitizen(int citizenId) {
        List<Issue> issues = new ArrayList<>();

        String sql = "SELECT i.issue_id, i.citizen_id, i.category, i.gps_location, "
                   + "       i.description, i.photo_url, i.status, "
                   + "       i.assigned_department_id, i.assigned_crew_id, "
                   + "       u.name AS citizen_name, "
                   + "       d.department_name "
                   + "FROM Issues i "
                   + "LEFT JOIN Users u ON i.citizen_id = u.user_id "
                   + "LEFT JOIN Departments d ON i.assigned_department_id = d.department_id "
                   + "WHERE i.citizen_id = ? "
                   + "ORDER BY i.issue_id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, citizenId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    issues.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[IssueDAO] getIssuesByCitizen error: " + e.getMessage());
        }

        return issues;
    }

    // -----------------------------------------------------------------------
    // 4. updateIssueStatus — TRANSACTIONAL update + status history insert
    // -----------------------------------------------------------------------
    /**
     * Updates the status of an issue AND inserts an audit record into Status_Updates.
     * Both operations are wrapped in a single JDBC transaction:
     *   - If either fails, a rollback is performed to keep data consistent.
     *   - On success, the transaction is committed.
     *
     * @param issueId    PK of the issue to update
     * @param newStatus  target status string ('ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')
     * @param updatedBy  user_id of the admin/crew performing the update
     * @param notes      optional notes describing the status change
     * @return true if both operations succeeded and were committed
     */
    public boolean updateIssueStatus(int issueId, String newStatus, int updatedBy, String notes) {

        // SQL to read the current status before changing it (needed for Status_Updates record)
        String selectSql = "SELECT status FROM Issues WHERE issue_id = ?";

        // SQL to update the Issues table
        String updateSql = "UPDATE Issues SET status = ? WHERE issue_id = ?";

        // SQL to insert audit trail into Status_Updates
        String insertSql = "INSERT INTO Status_Updates "
                         + "(issue_id, updated_by, previous_status, new_status, notes, timestamp) "
                         + "VALUES (?, ?, ?, ?, ?, NOW())";

        String previousStatus = null;

        try {
            // --- Step 1: Disable auto-commit to start the transaction ---
            conn.setAutoCommit(false);

            // --- Step 2: Read the current status ---
            try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                selectPs.setInt(1, issueId);
                try (ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        previousStatus = rs.getString("status");
                    } else {
                        // Issue not found — abort
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false;
                    }
                }
            }

            // --- Step 3: Update the Issues row ---
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, newStatus);
                updatePs.setInt(2, issueId);
                updatePs.executeUpdate();
            }

            // --- Step 4: Insert into Status_Updates (audit trail) ---
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, issueId);
                insertPs.setInt(2, updatedBy);
                insertPs.setString(3, previousStatus);
                insertPs.setString(4, newStatus);
                insertPs.setString(5, notes != null ? notes : "");
                insertPs.executeUpdate();
            }

            // --- Step 5: COMMIT both operations atomically ---
            conn.commit();
            System.out.println("[IssueDAO] Status update committed: issue " + issueId
                               + " -> " + newStatus);
            return true;

        } catch (SQLException e) {
            // --- ROLLBACK on any failure ---
            System.err.println("[IssueDAO] updateIssueStatus transaction failed: " + e.getMessage());
            try {
                conn.rollback();
                System.out.println("[IssueDAO] Transaction rolled back.");
            } catch (SQLException rollbackEx) {
                System.err.println("[IssueDAO] Rollback failed: " + rollbackEx.getMessage());
            }
            return false;

        } finally {
            // Always restore auto-commit mode
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("[IssueDAO] Failed to restore auto-commit: " + ex.getMessage());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private helper — maps a ResultSet row to an Issue POJO
    // -----------------------------------------------------------------------
    private Issue mapRow(ResultSet rs) throws SQLException {
        Issue issue = new Issue();
        issue.setIssueId(rs.getInt("issue_id"));
        issue.setCitizenId(rs.getInt("citizen_id"));
        issue.setCategory(rs.getString("category"));
        issue.setGpsLocation(rs.getString("gps_location"));
        issue.setDescription(rs.getString("description"));
        issue.setPhotoUrl(rs.getString("photo_url"));
        issue.setStatus(rs.getString("status"));
        issue.setAssignedDepartmentId(rs.getInt("assigned_department_id"));
        issue.setAssignedCrewId(rs.getInt("assigned_crew_id"));

        // Display fields from JOIN (may be null)
        issue.setCitizenName(rs.getString("citizen_name"));

        try { issue.setDepartmentName(rs.getString("department_name")); }
        catch (SQLException ignored) {} // column may not exist in all queries

        return issue;
    }
}