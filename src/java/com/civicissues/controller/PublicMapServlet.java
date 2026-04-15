package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.Issue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/map")
public class PublicMapServlet extends HttpServlet {
    private IssueDAO issueDAO;
    @Override public void init() { issueDAO = new IssueDAO(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        List<Issue> issues = issueDAO.getAllOpenIssues(null, null, 1, 500);
        req.setAttribute("mapIssues", issues);
        req.getRequestDispatcher("/WEB-INF/views/public_map.jsp").forward(req, res);
    }
}
