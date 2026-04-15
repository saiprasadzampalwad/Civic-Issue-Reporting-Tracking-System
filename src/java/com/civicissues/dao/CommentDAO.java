package com.civicissues.dao;

import com.civicissues.model.Comment;
import com.civicissues.util.DBConnection;

import java.sql.*;
import java.util.*;

public class CommentDAO {

    private Connection conn;
    public CommentDAO() { this.conn = DBConnection.getInstance().getConnection(); }

    public boolean addComment(int issueId, int userId, String text) {
        String sql = "INSERT INTO Comments (issue_id, user_id, comment) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issueId); ps.setInt(2, userId); ps.setString(3, text);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CommentDAO] addComment: " + e.getMessage());
            return false;
        }
    }

    public List<Comment> getCommentsByIssue(int issueId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS user_name FROM Comments c " +
                     "JOIN Users u ON c.user_id = u.user_id " +
                     "WHERE c.issue_id = ? ORDER BY c.created_at ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setCommentId(rs.getInt("comment_id"));
                    c.setIssueId(rs.getInt("issue_id"));
                    c.setUserId(rs.getInt("user_id"));
                    c.setComment(rs.getString("comment"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setUserName(rs.getString("user_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) { System.err.println("[CommentDAO] get: " + e.getMessage()); }
        return list;
    }
}
