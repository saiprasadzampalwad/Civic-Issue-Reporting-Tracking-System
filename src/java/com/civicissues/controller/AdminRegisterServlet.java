/*
 * Admin Registration Servlet - /admin/register
 * Supports role/dept selection for MUNICIPAL_ADMIN/MAINTENANCE_CREW
 */
package com.civicissues.controller;

import com.civicissues.dao.DepartmentDAO;
import com.civicissues.dao.UserDAO;
import com.civicissues.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/register")
public class AdminRegisterServlet extends HttpServlet {

    private UserDAO userDAO;
    private DepartmentDAO deptDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
        deptDAO = new DepartmentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        CsrfUtil.getOrCreate(req.getSession(true));
        List<Map<String, Object>> departments = deptDAO.getAllDepartments();
        req.setAttribute("departments", departments);
        req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) {
            res.sendError(403, "Invalid CSRF token");
            return;
        }

        String name = req.getParameter("name") != null ? req.getParameter("name").trim() : "";
        String phone = req.getParameter("phone") != null ? req.getParameter("phone").trim() : "";
        String password = req.getParameter("password") != null ? req.getParameter("password").trim() : "";
        String confirm = req.getParameter("confirm") != null ? req.getParameter("confirm").trim() : "";
        String role = req.getParameter("role");
        String deptStr = req.getParameter("department_id");

        Integer departmentId = null;
        if (deptStr != null && !deptStr.isEmpty()) {
            try {
                departmentId = Integer.parseInt(deptStr);
            } catch (NumberFormatException e) {
                // Invalid dept
            }
        }

        // Validation
        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || role == null) {
            req.setAttribute("errorMessage", "All fields are required.");
            populateDepts(req);
            req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
            return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("errorMessage", "Passwords do not match.");
            populateDepts(req);
            req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
            return;
        }
        if (password.length() < 6) {
            req.setAttribute("errorMessage", "Password must be at least 6 characters.");
            populateDepts(req);
            req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
            return;
        }
        if (!role.matches("^(CITIZEN|MUNICIPAL_ADMIN|MAINTENANCE_CREW)$")) {
            req.setAttribute("errorMessage", "Invalid role selected.");
            populateDepts(req);
            req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
            return;
        }
        if (("MUNICIPAL_ADMIN".equals(role) || "MAINTENANCE_CREW".equals(role)) && departmentId == null) {
            req.setAttribute("errorMessage", "Department is required for Admin/Crew roles.");
            populateDepts(req);
            req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
            return;
        }

        boolean ok = userDAO.registerAdmin(name, phone, password, role, departmentId);
        if (ok) {
            req.setAttribute("successMessage", "Registration successful! You can sign in now.");
            req.getRequestDispatcher("/WEB-INF/views/admin_login.jsp").forward(req, res);
        } else {
            req.setAttribute("errorMessage", "Registration failed (phone may exist). Please try again.");
            populateDepts(req);
            req.getRequestDispatcher("/WEB-INF/views/admin_register.jsp").forward(req, res);
        }
    }

    private void populateDepts(HttpServletRequest req) throws ServletException {
        List<Map<String, Object>> departments = deptDAO.getAllDepartments();
        req.setAttribute("departments", departments);
    }
}

