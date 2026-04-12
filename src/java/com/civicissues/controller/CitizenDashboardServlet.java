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
 * CitizenDashboardServlet.java
 * Loads the citizen's reported issues and forwards to citizen_dashboard.jsp.
 * URL: /citizen/dashboard
 */
@WebServlet("/citizen/dashboard")
public class CitizenDashboardServlet extends HttpServlet {

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
        if (!"CITIZEN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Fetch this citizen's issues and expose to JSP via request attribute
        List<Issue> myIssues = issueDAO.getIssuesByCitizen(user.getUserId());
        request.setAttribute("myIssues", myIssues);

        // Pick up and clear any one-time success message from the session
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        request.getRequestDispatcher("/WEB-INF/views/citizen_dashboard.jsp")
               .forward(request, response);
    }
}