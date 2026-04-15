/*
 * Updated for Phone Auth - No Email Verification
 */
package com.civicissues.controller;

import com.civicissues.dao.UserDAO;
import com.civicissues.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/** URL: /register */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO;
    @Override public void init() { userDAO = new UserDAO(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        CsrfUtil.getOrCreate(req.getSession(true));
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) { res.sendError(403); return; }

        String name     = req.getParameter("name")     != null ? req.getParameter("name").trim()     : "";
        String phone    = req.getParameter("phone")    != null ? req.getParameter("phone").trim()    : "";
        String password = req.getParameter("password") != null ? req.getParameter("password").trim() : "";
        String confirm  = req.getParameter("confirm")  != null ? req.getParameter("confirm").trim()  : "";

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            req.setAttribute("errorMessage", "Name, phone and password are required.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res); return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("errorMessage", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res); return;
        }
        if (password.length() < 6) {
            req.setAttribute("errorMessage", "Password must be at least 6 characters.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res); return;
        }
        if (userDAO.phoneExists(phone)) {
            req.setAttribute("errorMessage", "Phone already registered.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res); return;
        }

        boolean ok = userDAO.registerUser(name, phone, password);
        if (ok) {
            req.setAttribute("successMessage", "Registration successful! You can login now.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
        } else {
            req.setAttribute("errorMessage", "Registration failed. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, res);
        }
    }
}

