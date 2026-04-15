package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.Issue;
import com.civicissues.model.User;
import com.civicissues.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** URL: /citizen/reportIssue — supports up to 3 photo uploads */
@WebServlet("/citizen/reportIssue")
@MultipartConfig(fileSizeThreshold=1024*1024, maxFileSize=5*1024*1024, maxRequestSize=20*1024*1024)
public class ReportIssueServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads" + File.separator + "photos";
    private IssueDAO issueDAO;
    @Override public void init() { issueDAO = new IssueDAO(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!isAuthorized(req, res)) return;
        CsrfUtil.getOrCreate(req.getSession(false));
        req.getRequestDispatcher("/WEB-INF/views/report_issue.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!isAuthorized(req, res)) return;
        if (!CsrfUtil.isValid(req)) { res.sendError(403); return; }

        HttpSession session   = req.getSession(false);
        User user             = (User) session.getAttribute("loggedInUser");

        String category    = req.getParameter("category");
        String gpsLocation = req.getParameter("gpsLocation");
        String description = req.getParameter("description");

        // Handle multiple photos (up to 3 parts named photo1, photo2, photo3)
        List<String> photoUrls = new ArrayList<>();
        String appPath    = getServletContext().getRealPath("");
        String uploadPath = appPath + File.separator + UPLOAD_DIR;
        new File(uploadPath).mkdirs();

        for (String partName : new String[]{"photo1","photo2","photo3"}) {
            Part part = req.getPart(partName);
            if (part != null && part.getSize() > 0) {
                String ext  = "";
                String orig = extractFileName(part);
                if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf("."));
                String fname = UUID.randomUUID().toString() + ext;
                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, Paths.get(uploadPath + File.separator + fname),
                               StandardCopyOption.REPLACE_EXISTING);
                }
                photoUrls.add(UPLOAD_DIR.replace(File.separator, "/") + "/" + fname);
            }
        }

        Issue issue = new Issue();
        issue.setCitizenId(user.getUserId());
        issue.setCategory(category);
        issue.setGpsLocation(gpsLocation);
        issue.setDescription(description);

        if (issueDAO.createIssue(issue, photoUrls)) {
            req.getSession().setAttribute("successMessage", "Issue reported successfully!");
            res.sendRedirect(req.getContextPath() + "/citizen/dashboard");
        } else {
            req.setAttribute("errorMessage", "Failed to submit. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/report_issue.jsp").forward(req, res);
        }
    }

    private String extractFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String t : cd.split(";")) {
            if (t.trim().startsWith("filename")) {
                String fname = t.substring(t.indexOf('=') + 1).trim().replace("\"","");
                return Paths.get(fname).getFileName().toString();
            }
        }
        return null;
    }

    private boolean isAuthorized(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("loggedInUser") == null) { res.sendRedirect(req.getContextPath()+"/login"); return false; }
        User u = (User) s.getAttribute("loggedInUser");
        if (!"CITIZEN".equals(u.getRole())) { res.sendRedirect(req.getContextPath()+"/login"); return false; }
        return true;
    }
}