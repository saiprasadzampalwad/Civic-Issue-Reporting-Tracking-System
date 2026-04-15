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

/** URL: /citizen/dashboard — with filter + pagination */
@WebServlet("/citizen/dashboard")
public class CitizenDashboardServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private IssueDAO issueDAO;
    @Override public void init() { issueDAO = new IssueDAO(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) { res.sendRedirect(req.getContextPath()+"/login"); return; }
        User user = (User) session.getAttribute("loggedInUser");
        if (!"CITIZEN".equals(user.getRole())) { res.sendRedirect(req.getContextPath()+"/login"); return; }

        String filterStatus   = req.getParameter("filterStatus");
        String filterCategory = req.getParameter("filterCategory");
        int page = 1;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception e) {}
        if (page < 1) page = 1;

        List<Issue> myIssues = issueDAO.getIssuesByCitizen(user.getUserId(), filterStatus, filterCategory, page, PAGE_SIZE);
        int totalCount = issueDAO.countByCitizen(user.getUserId());
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        req.setAttribute("myIssues",       myIssues);
        req.setAttribute("currentPage",    page);
        req.setAttribute("totalPages",     totalPages);
        req.setAttribute("filterStatus",   filterStatus != null ? filterStatus : "");
        req.setAttribute("filterCategory", filterCategory != null ? filterCategory : "");

        String msg = (String) session.getAttribute("successMessage");
        if (msg != null) { req.setAttribute("successMessage", msg); session.removeAttribute("successMessage"); }

        req.getRequestDispatcher("/WEB-INF/views/citizen_dashboard.jsp").forward(req, res);
    }
}