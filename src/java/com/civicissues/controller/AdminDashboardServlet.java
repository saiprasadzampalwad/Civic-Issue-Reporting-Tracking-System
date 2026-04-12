/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.Issue;
import com.civicissues.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * AdminDashboardServlet.java
 * Loads all open/assigned/in-progress issues for the Admin Dashboard.
 * Accessible by MUNICIPAL_ADMIN and MAINTENANCE_CREW roles.
 * URL: /admin/dashboard
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private IssueDAO issueDAO;

    @Override
    public void init() throws ServletException {
        issueDAO = new IssueDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session guard
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("loggedInUser");
        if ("CITIZEN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/citizen/dashboard");
            return;
        }

        // Fetch all active issues
        List<Issue> openIssues = issueDAO.getAllOpenIssues();
        request.setAttribute("openIssues", openIssues);

        // One-time feedback message
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        request.getRequestDispatcher("/WEB-INF/views/admin_dashboard.jsp")
               .forward(request, response);
    }
}