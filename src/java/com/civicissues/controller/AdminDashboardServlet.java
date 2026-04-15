/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.controller;

import com.civicissues.dao.*;
import com.civicissues.model.Issue;
import com.civicissues.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** URL: /admin/dashboard — filter + pagination + analytics + dept/crew dropdowns */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final int PAGE_SIZE = 15;
    private IssueDAO      issueDAO;
    private DepartmentDAO deptDAO;
    private RatingDAO     ratingDAO;

    @Override
    public void init() {
        issueDAO  = new IssueDAO();
        deptDAO   = new DepartmentDAO();
        ratingDAO = new RatingDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) { res.sendRedirect(req.getContextPath()+"/login"); return; }
        User user = (User) session.getAttribute("loggedInUser");
        if ("CITIZEN".equals(user.getRole())) { res.sendRedirect(req.getContextPath()+"/citizen/dashboard"); return; }

        String filterStatus   = req.getParameter("filterStatus");
        String filterCategory = req.getParameter("filterCategory");
        int page = 1;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception e) {}
        if (page < 1) page = 1;

        List<Issue> issues     = issueDAO.getAllOpenIssues(filterStatus, filterCategory, page, PAGE_SIZE);
        int totalCount         = issueDAO.countAllOpen();
        int totalPages         = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        Map<String, Object> stats = issueDAO.getAnalyticsStats();
        List<Map<String, Object>> depts   = deptDAO.getAllDepartments();
        List<Map<String, Object>> crew    = deptDAO.getAllCrew();
        List<Map<String, Object>> ratings = ratingDAO.getAvgRatingByDepartment();

        req.setAttribute("openIssues",     issues);
        req.setAttribute("currentPage",    page);
        req.setAttribute("totalPages",     totalPages);
        req.setAttribute("filterStatus",   filterStatus   != null ? filterStatus   : "");
        req.setAttribute("filterCategory", filterCategory != null ? filterCategory : "");
        req.setAttribute("stats",          stats);
        req.setAttribute("departments",    depts);
        req.setAttribute("crewList",       crew);
        req.setAttribute("deptRatings",    ratings);

        String msg = (String) session.getAttribute("successMessage");
        if (msg != null) { req.setAttribute("successMessage", msg); session.removeAttribute("successMessage"); }

        req.getRequestDispatcher("/WEB-INF/views/admin_dashboard.jsp").forward(req, res);
    }
}