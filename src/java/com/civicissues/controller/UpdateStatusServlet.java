/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.dao.UserDAO;
import com.civicissues.model.Issue;
import com.civicissues.model.User;
import com.civicissues.util.CsrfUtil;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** URL: /admin/updateStatus — includes dept/crew assignment + email notification */
@WebServlet("/admin/updateStatus")
public class UpdateStatusServlet extends HttpServlet {

    private static final List<String> VALID = Arrays.asList("ASSIGNED","IN_PROGRESS","RESOLVED","CLOSED");
    private IssueDAO issueDAO;
    private UserDAO  userDAO;

    @Override
    public void init() { issueDAO = new IssueDAO(); userDAO = new UserDAO(); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) { res.sendRedirect(req.getContextPath()+"/login"); return; }
        User admin = (User) session.getAttribute("loggedInUser");
        if ("CITIZEN".equals(admin.getRole())) { res.sendRedirect(req.getContextPath()+"/citizen/dashboard"); return; }
        if (!CsrfUtil.isValid(req)) { res.sendError(403); return; }

        String issueIdParam = req.getParameter("issueId");
        String newStatus    = req.getParameter("newStatus");
        String notes        = req.getParameter("notes");
        int    deptId       = 0;
        int    crewId       = 0;
        try { deptId = Integer.parseInt(req.getParameter("assignedDeptId")); } catch (Exception e) {}
        try { crewId = Integer.parseInt(req.getParameter("assignedCrewId")); } catch (Exception e) {}

        int issueId;
        try { issueId = Integer.parseInt(issueIdParam.trim()); }
        catch (Exception e) { session.setAttribute("successMessage","Error: Invalid issue ID."); res.sendRedirect(req.getContextPath()+"/admin/dashboard"); return; }

        if (!VALID.contains(newStatus != null ? newStatus.trim().toUpperCase() : "")) {
            session.setAttribute("successMessage","Error: Invalid status."); res.sendRedirect(req.getContextPath()+"/admin/dashboard"); return;
        }

        boolean ok = issueDAO.updateIssueStatus(issueId, newStatus.trim().toUpperCase(), admin.getUserId(), deptId, crewId, notes);

        if (ok) {
            // Email notification simulated (no email feature)
            System.out.println("[Simulated Notification] Status update for issue #" + issueId + " to " + newStatus.trim().toUpperCase());
            session.setAttribute("successMessage","Issue #"+issueId+" updated to "+newStatus+".");
        } else {
            session.setAttribute("successMessage","Failed to update Issue #"+issueId+". Try again.");
        }
        res.sendRedirect(req.getContextPath()+"/admin/dashboard");
    }
}