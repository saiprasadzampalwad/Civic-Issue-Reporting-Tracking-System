/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * UpdateStatusServlet.java — Admin/Crew update the status of an issue.
 *
 * TRANSACTION NOTE:
 *   The actual UPDATE (Issues table) + INSERT (Status_Updates table) is performed
 *   as a single JDBC transaction inside IssueDAO.updateIssueStatus().
 *   If either operation fails, the transaction is rolled back.
 *
 * Accessible by: MUNICIPAL_ADMIN, MAINTENANCE_CREW
 * URL Mapping: /admin/updateStatus  (POST only)
 */
@WebServlet("/admin/updateStatus")
public class UpdateStatusServlet extends HttpServlet {

    // Allowed status transitions (whitelist)
    private static final List<String> VALID_STATUSES =
            Arrays.asList("ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private IssueDAO issueDAO;

    @Override
    public void init() throws ServletException {
        issueDAO = new IssueDAO();
    }

    // Only POST is supported — form submission from admin_dashboard.jsp
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Session / role guard
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User admin = (User) session.getAttribute("loggedInUser");
        if ("CITIZEN".equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/citizen/dashboard");
            return;
        }

        // 2. Parse and validate parameters
        String issueIdParam  = request.getParameter("issueId");
        String newStatus     = request.getParameter("newStatus");
        String notes         = request.getParameter("notes");

        if (issueIdParam == null || newStatus == null) {
            request.getSession().setAttribute("successMessage",
                    "Error: Missing issueId or newStatus parameter.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int issueId;
        try {
            issueId = Integer.parseInt(issueIdParam.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("successMessage", "Error: Invalid issue ID.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // 3. Whitelist check — prevent arbitrary status strings from reaching the DB
        if (!VALID_STATUSES.contains(newStatus.trim().toUpperCase())) {
            session.setAttribute("successMessage", "Error: Invalid status value.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // 4. Perform transactional update via DAO
        boolean success = issueDAO.updateIssueStatus(
                issueId,
                newStatus.trim().toUpperCase(),
                admin.getUserId(),
                notes
        );

        // 5. Set feedback message and redirect (POST-Redirect-GET)
        if (success) {
            session.setAttribute("successMessage",
                    "Issue #" + issueId + " status updated to " + newStatus + ".");
        } else {
            session.setAttribute("successMessage",
                    "Failed to update Issue #" + issueId + ". Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}