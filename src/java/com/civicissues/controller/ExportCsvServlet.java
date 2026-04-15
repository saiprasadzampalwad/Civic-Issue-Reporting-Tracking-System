package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.Issue;
import com.civicissues.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/admin/exportCsv")
public class ExportCsvServlet extends HttpServlet {
    private IssueDAO issueDAO;
    @Override public void init() { issueDAO = new IssueDAO(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("loggedInUser");
        if ("CITIZEN".equals(user.getRole())) {
            res.sendRedirect(req.getContextPath() + "/citizen/dashboard");
            return;
        }

        List<Issue> issues = issueDAO.getAllIssuesForExport();

        res.setContentType("text/csv;charset=UTF-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"civic_issues.csv\"");

        try (PrintWriter pw = res.getWriter()) {
            pw.println("IssueID,ReportedBy,Category,GPS,Description,Status,Department,Date");
            for (Issue i : issues) {
                pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    i.getIssueId(),
                    i.getCitizenName() != null ? i.getCitizenName().replace("\"", "\"\"") : "",
                    i.getCategory() != null ? i.getCategory().replace("\"", "\"\"") : "",
                    i.getGpsLocation() != null ? i.getGpsLocation().replace("\"", "\"\"") : "",
                    i.getDescription() != null ? i.getDescription().replace("\"", "\"\"") : "",
                    i.getStatus(),
                    i.getDepartmentName() != null ? i.getDepartmentName().replace("\"", "\"\"") : "",
                    i.getCreatedAt());
            }
        }
    }
}
