package com.civicissues.controller;

import com.civicissues.dao.CommentDAO;
import com.civicissues.model.User;
import com.civicissues.util.CsrfUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/issue/comment")
public class CommentServlet extends HttpServlet {
    private CommentDAO commentDAO;
    @Override public void init() { commentDAO = new CommentDAO(); }

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
        int issueId = Integer.parseInt(req.getParameter("issueId"));
        String text = req.getParameter("comment").trim();

        commentDAO.addComment(issueId, user.getUserId(), text);

        String redirect = "CITIZEN".equals(user.getRole()) ? "/citizen/dashboard" : "/admin/dashboard";
        res.sendRedirect(req.getContextPath() + redirect);
    }
}
