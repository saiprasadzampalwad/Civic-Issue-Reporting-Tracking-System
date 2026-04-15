/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.dao;

import com.civicissues.model.Issue;
import com.civicissues.model.Rating;
import com.civicissues.util.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * IssueDAO.java - Full CRUD + photos + SLA + search/filter + pagination + analytics + CSV.
 */
public class IssueDAO {

    private static final int SLA_HOURS = 72; // issues breach SLA after 72 hours
    private final Connection conn;

    public IssueDAO() { this.conn = DBConnection.getInstance().getConnection(); }

    // -----------------------------------------------------------------------
    // createIssue — saves issue + multiple photos + sets SLA deadline
    // -----------------------------------------------------------------------
    public boolean createIssue(Issue issue, List<String> photoUrls) {
        String sql = "INSERT INTO Issues (citizen_id,category,gps_location,description,status,sla_deadline) "
                   + "VALUES (?,?,?,?,'OPEN', DATE_ADD(NOW(), INTERVAL ? HOUR))";
        try {
            conn.setAutoCommit(false);
            int newId;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, issue.getCitizenId());
                ps.setString(2, issue.getCategory());
                ps.setString(3, issue.getGpsLocation());
                ps.setString(4, issue.getDescription());
                ps.setInt(5, SLA_HOURS);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) { conn.rollback(); return false; }
                    newId = keys.getInt(1);
                }
            }
            if (photoUrls != null && !photoUrls.isEmpty()) {
                try (PreparedStatement ph = conn.prepareStatement(
                        "INSERT INTO Issue_Photos (issue_id, photo_url) VALUES (?,?)")) {
                    for (String url : photoUrls) {
                        ph.setInt(1, newId); ph.setString(2, url); ph.addBatch();
                    }
                    ph.executeBatch();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("[IssueDAO] createIssue: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // -----------------------------------------------------------------------
    // getIssuesByCitizen — with optional filter + pagination
    // -----------------------------------------------------------------------
    public List<Issue> getIssuesByCitizen(int citizenId, String filterStatus,
                                           String filterCategory, int page, int pageSize) {
        List<Issue> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(
            "SELECT i.*, u.name AS citizen_name, d.department_name " +
            "FROM Issues i " +
            "LEFT JOIN Users u ON i.citizen_id = u.user_id " +
            "LEFT JOIN Departments d ON i.assigned_department_id = d.department_id " +
            "WHERE i.citizen_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(citizenId);
        if (filterStatus != null && !filterStatus.isEmpty()) { sb.append(" AND i.status=?"); params.add(filterStatus); }
        if (filterCategory != null && !filterCategory.isEmpty()) { sb.append(" AND i.category=?"); params.add(filterCategory); }
        sb.append(" ORDER BY i.issue_id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("[IssueDAO] getByCitizen: " + e.getMessage()); }
        attachPhotos(list);
        attachRatings(list);
        return list;
    }

    public int countByCitizen(int citizenId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Issues WHERE citizen_id=?")) {
            ps.setInt(1, citizenId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) {}
        return 0;
    }

    // -----------------------------------------------------------------------
    // getAllOpenIssues — admin view with filter + pagination
    // -----------------------------------------------------------------------
    public List<Issue> getAllOpenIssues(String filterStatus, String filterCategory,
                                        int page, int pageSize) {
        List<Issue> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(
            "SELECT i.*, u.name AS citizen_name, d.department_name " +
            "FROM Issues i " +
            "LEFT JOIN Users u ON i.citizen_id = u.user_id " +
            "LEFT JOIN Departments d ON i.assigned_department_id = d.department_id " +
            "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (filterStatus != null && !filterStatus.isEmpty()) { sb.append(" AND i.status=?"); params.add(filterStatus); }
        else sb.append(" AND i.status IN ('OPEN','ASSIGNED','IN_PROGRESS')");
        if (filterCategory != null && !filterCategory.isEmpty()) { sb.append(" AND i.category=?"); params.add(filterCategory); }
        sb.append(" ORDER BY i.sla_deadline ASC, i.issue_id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Issue issue = mapRow(rs);
                    issue.setSlaBreached(issue.getSlaDeadline() != null &&
                        issue.getSlaDeadline().before(new Timestamp(System.currentTimeMillis())));
                    list.add(issue);
                }
            }
        } catch (SQLException e) { System.err.println("[IssueDAO] getAllOpen: " + e.getMessage()); }
        attachPhotos(list);
        return list;
    }

    public int countAllOpen() {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM Issues WHERE status IN ('OPEN','ASSIGNED','IN_PROGRESS')")) {
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) {}
        return 0;
    }

    // -----------------------------------------------------------------------
    // updateIssueStatus — transactional: update Issues + insert Status_Updates + email
    // -----------------------------------------------------------------------
    public boolean updateIssueStatus(int issueId, String newStatus, int updatedBy,
                                      int assignedDeptId, int assignedCrewId, String notes) {
        String selectSql = "SELECT status FROM Issues WHERE issue_id=?";
        String updateSql = "UPDATE Issues SET status=?, assigned_department_id=?, assigned_crew_id=? WHERE issue_id=?";
        String insertSql = "INSERT INTO Status_Updates (issue_id,updated_by,previous_status,new_status,notes,update_time) VALUES (?,?,?,?,?,NOW())";
        String prevStatus = null;
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement s = conn.prepareStatement(selectSql)) {
                s.setInt(1, issueId);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) { conn.rollback(); return false; }
                    prevStatus = rs.getString(1);
                }
            }
            try (PreparedStatement u = conn.prepareStatement(updateSql)) {
                u.setString(1, newStatus);
                u.setObject(2, assignedDeptId > 0 ? assignedDeptId : null);
                u.setObject(3, assignedCrewId > 0 ? assignedCrewId : null);
                u.setInt(4, issueId);
                u.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                ins.setInt(1, issueId); ins.setInt(2, updatedBy);
                ins.setString(3, prevStatus); ins.setString(4, newStatus);
                ins.setString(5, notes != null ? notes : "");
                ins.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("[IssueDAO] updateStatus: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // -----------------------------------------------------------------------
    // Analytics stats map
    // -----------------------------------------------------------------------
    public Map<String, Object> getAnalyticsStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM vw_issue_stats");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalIssues",       rs.getInt("total_issues"));
                stats.put("openCount",          rs.getInt("open_count"));
                stats.put("inProgressCount",    rs.getInt("in_progress_count"));
                stats.put("resolvedCount",      rs.getInt("resolved_count"));
                stats.put("closedCount",        rs.getInt("closed_count"));
                stats.put("avgResolutionHours", rs.getObject("avg_resolution_hours"));
            }
        } catch (SQLException e) { System.err.println("[IssueDAO] analytics: " + e.getMessage()); }

        // Issues by category
        List<Map<String, Object>> catStats = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT category, COUNT(*) AS cnt FROM Issues GROUP BY category ORDER BY cnt DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("category", rs.getString("category"));
                row.put("count",    rs.getInt("cnt"));
                catStats.add(row);
            }
        } catch (SQLException e) {}
        stats.put("byCategory", catStats);

        // SLA breached count
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM Issues WHERE sla_deadline < NOW() AND status IN ('OPEN','ASSIGNED','IN_PROGRESS')");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) stats.put("slaBreachedCount", rs.getInt(1));
        } catch (SQLException e) {}

        return stats;
    }

    // -----------------------------------------------------------------------
    // Get all issues for CSV export
    // -----------------------------------------------------------------------
    public List<Issue> getAllIssuesForExport() {
        List<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, u.name AS citizen_name, d.department_name " +
                     "FROM Issues i " +
                     "LEFT JOIN Users u ON i.citizen_id = u.user_id " +
                     "LEFT JOIN Departments d ON i.assigned_department_id = d.department_id " +
                     "ORDER BY i.issue_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[IssueDAO] export: " + e.getMessage()); }
        return list;
    }

    // -----------------------------------------------------------------------
    // SLA breach check (called by SlaCheckerServlet)
    // -----------------------------------------------------------------------
    public List<Issue> getSlaBreachedIssues() {
        List<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, u.name AS citizen_name, d.department_name " +
                     "FROM Issues i " +
                     "LEFT JOIN Users u ON i.citizen_id = u.user_id " +
                     "LEFT JOIN Departments d ON i.assigned_department_id = d.department_id " +
                     "WHERE i.sla_deadline < NOW() AND i.status IN ('OPEN','ASSIGNED','IN_PROGRESS')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[IssueDAO] slaBreached: " + e.getMessage()); }
        return list;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------
    private Issue mapRow(ResultSet rs) throws SQLException {
        Issue i = new Issue();
        i.setIssueId(rs.getInt("issue_id"));
        i.setCitizenId(rs.getInt("citizen_id"));
        i.setCategory(rs.getString("category"));
        i.setGpsLocation(rs.getString("gps_location"));
        i.setDescription(rs.getString("description"));
        i.setStatus(rs.getString("status"));
        i.setAssignedDepartmentId(rs.getInt("assigned_department_id"));
        i.setAssignedCrewId(rs.getInt("assigned_crew_id"));
        i.setSlaDeadline(rs.getTimestamp("sla_deadline"));
        i.setCreatedAt(rs.getTimestamp("created_at"));
        try { i.setCitizenName(rs.getString("citizen_name")); } catch (SQLException ignored) {}
        try { i.setDepartmentName(rs.getString("department_name")); } catch (SQLException ignored) {}
        return i;
    }

    private void attachPhotos(List<Issue> issues) {
        if (issues.isEmpty()) return;
        StringBuilder ids = new StringBuilder("SELECT issue_id, photo_url FROM Issue_Photos WHERE issue_id IN (");
        for (int i = 0; i < issues.size(); i++) { ids.append(i > 0 ? "," : "").append("?"); }
        ids.append(") ORDER BY photo_id");
        Map<Integer, List<String>> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(ids.toString())) {
            for (int i = 0; i < issues.size(); i++) ps.setInt(i + 1, issues.get(i).getIssueId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    map.computeIfAbsent(id, k -> new ArrayList<>()).add(rs.getString(2));
                }
            }
        } catch (SQLException e) {}
        issues.forEach((issue) -> {
            issue.setPhotoUrls(map.getOrDefault(issue.getIssueId(), new ArrayList<>()));
        });
    }

    private void attachRatings(List<Issue> issues) {
        if (issues.isEmpty()) return;
        StringBuilder ids = new StringBuilder("SELECT * FROM Ratings WHERE issue_id IN (");
        for (int i = 0; i < issues.size(); i++) { ids.append(i > 0 ? "," : "").append("?"); }
        ids.append(")");
        Map<Integer, Rating> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(ids.toString())) {
            for (int i = 0; i < issues.size(); i++) ps.setInt(i + 1, issues.get(i).getIssueId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rating r = new Rating();
                    r.setRatingId(rs.getInt("rating_id"));
                    r.setIssueId(rs.getInt("issue_id"));
                    r.setStars(rs.getInt("stars"));
                    r.setFeedback(rs.getString("feedback"));
                    map.put(r.getIssueId(), r);
                }
            }
        } catch (SQLException e) {}
        issues.forEach((issue) -> {
            issue.setRating(map.get(issue.getIssueId()));
        });
    }
}