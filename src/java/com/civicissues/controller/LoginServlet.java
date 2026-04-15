/*
 * Updated for Phone Auth
 */
package com.civicissues.controller;

import com.civicissues.dao.UserDAO;
import com.civicissues.model.User;
import com.civicissues.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet({"/login", "/admin/login"})
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;
    @Override public void init() { userDAO = new UserDAO(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            User u = (User) session.getAttribute("loggedInUser");
            redirectByRole(u.getRole(), req, res); return;
        }
        CsrfUtil.getOrCreate(req.getSession(true));
        String jsp = req.getServletPath().startsWith("/admin/") ? "/WEB-INF/views/admin_login.jsp" : "/WEB-INF/views/login.jsp";
        req.getRequestDispatcher(jsp).forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) { res.sendError(403, "Invalid CSRF token"); return; }
        String phone    = req.getParameter("phone")    != null ? req.getParameter("phone").trim()    : "";
        String password = req.getParameter("password") != null ? req.getParameter("password").trim() : "";

        if (phone.isEmpty() || password.isEmpty()) {
            req.setAttribute("errorMessage", "Phone and password are required.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res); return;
        }

        User user = userDAO.authenticateUser(phone, password);
        if (user == null) {
            req.setAttribute("errorMessage", "Invalid credentials or account is temporarily locked.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res); return;
        }

        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession newSession = req.getSession(true);
        newSession.setMaxInactiveInterval(30 * 60);
        newSession.setAttribute("loggedInUser", user);
        CsrfUtil.getOrCreate(newSession);
        redirectByRole(user.getRole(), req, res);
    }

    private void redirectByRole(String role, HttpServletRequest req, HttpServletResponse res) throws IOException {
        String cp = req.getContextPath();
        switch (role) {
            case "CITIZEN":          res.sendRedirect(cp + "/citizen/dashboard"); break;
            case "MUNICIPAL_ADMIN":
            case "MAINTENANCE_CREW": res.sendRedirect(cp + "/admin/dashboard");   break;
            default:                 res.sendRedirect(cp + "/login");
        }
    }
}

