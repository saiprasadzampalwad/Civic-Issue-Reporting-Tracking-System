/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.controller;

import com.civicissues.dao.UserDAO;
import com.civicissues.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginServlet.java — Handles GET (show login page) and POST (process login).
 *
 * SESSION MANAGEMENT FLOW:
 *  1. User submits email + password via POST to /login.
 *  2. UserDAO.authenticateUser() queries the DB with a PreparedStatement.
 *  3. On success: a new HttpSession is created; the User object is stored as
 *     session attribute "loggedInUser".
 *  4. The servlet reads the user's role and redirects:
 *       CITIZEN          -> /citizen/dashboard
 *       MUNICIPAL_ADMIN  -> /admin/dashboard
 *       MAINTENANCE_CREW -> /admin/dashboard  (crew use the same dashboard)
 *  5. On failure: forward back to login.jsp with an error message attribute.
 *
 * URL Mapping: /login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        // Initialize DAO once when servlet is first loaded
        userDAO = new UserDAO();
    }

    // -----------------------------------------------------------------------
    // GET /login — Display the login form
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already logged in, redirect them to their dashboard
        HttpSession session = request.getSession(false); // false = don't create a new session
        if (session != null && session.getAttribute("loggedInUser") != null) {
            User user = (User) session.getAttribute("loggedInUser");
            redirectByRole(user.getRole(), request, response);
            return;
        }

        // Otherwise, forward to the login JSP
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    // -----------------------------------------------------------------------
    // POST /login — Process login credentials
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Read form parameters (trim whitespace)
        String email    = request.getParameter("email") != null
                          ? request.getParameter("email").trim() : "";
        String password = request.getParameter("password") != null
                          ? request.getParameter("password").trim() : "";

        // 2. Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        // 3. Authenticate via DAO (PreparedStatement inside)
        User user = userDAO.authenticateUser(email, password);

        if (user == null) {
            // Authentication failed
            request.setAttribute("errorMessage", "Invalid email or password. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        // 4. Authentication succeeded — create a NEW session (invalidate old one first
        //    to prevent session fixation attacks)
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = request.getSession(true); // true = create new session
        newSession.setMaxInactiveInterval(30 * 60);        // 30-minute session timeout
        newSession.setAttribute("loggedInUser", user);    // Store full User POJO

        System.out.println("[LoginServlet] User logged in: " + user);

        // 5. Redirect based on role
        redirectByRole(user.getRole(), request, response);
    }

    // -----------------------------------------------------------------------
    // Helper — redirect to the correct dashboard based on role
    // -----------------------------------------------------------------------
    private void redirectByRole(String role, HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String contextPath = req.getContextPath(); // e.g. "/CivicIssueSystem"
        switch (role) {
            case "CITIZEN":
                res.sendRedirect(contextPath + "/citizen/dashboard");
                break;
            case "MUNICIPAL_ADMIN":
            case "MAINTENANCE_CREW":
                res.sendRedirect(contextPath + "/admin/dashboard");
                break;
            default:
                res.sendRedirect(contextPath + "/login");
        }
    }
}