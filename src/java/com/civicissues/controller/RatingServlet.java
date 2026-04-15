package com.civicissues.controller;

import com.civicissues.dao.RatingDAO;
import com.civicissues.model.User;
import com.civicissues.util.CsrfUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/issue/rate")
public class RatingServlet extends HttpServlet {
    private RatingDAO ratingDAO;
    @Override public void init() { ratingDAO = new RatingDAO(); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!CsrfUtil.isValid(req)) {
            res.sendError(403);
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");
        if (!"CITIZEN".equals(user.getRole())) {
            res.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        int issueId = Integer.parseInt(req.getParameter("issueId"));
        int stars = Integer.parseInt(req.getParameter("stars"));
        String feedback = req.getParameter("feedback").trim();

        ratingDAO.submitRating(issueId, user.getUserId(), stars, feedback);
        req.getSession().setAttribute("successMessage", "Thank you for rating!");

        res.sendRedirect(req.getContextPath() + "/citizen/dashboard");
    }
}
