package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.Issue;
import com.civicissues.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * ReportIssueServlet.java — Allows a logged-in CITIZEN to submit a new civic issue.
 *
 * FILE UPLOAD FLOW (using Servlet 3.0 @MultipartConfig):
 *  1. @MultipartConfig tells the container to parse multipart/form-data requests.
 *  2. The photo file is accessed via request.getPart("photo").
 *  3. A UUID-based filename is generated to prevent collisions and path-traversal attacks.
 *  4. The file is saved to a designated upload directory on the server's filesystem.
 *  5. Only the relative URL (e.g., "uploads/photos/abc123.jpg") is stored in the DB,
 *     not the full path.
 *
 * ACCESS CONTROL:
 *  - Only users with role CITIZEN may access this servlet.
 *  - Session is checked; unauthenticated users are redirected to /login.
 *
 * URL Mapping: /citizen/reportIssue
 */
@WebServlet("/citizen/reportIssue")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB — buffer in memory before writing to disk
    maxFileSize       = 5 * 1024 * 1024,  // 5 MB max per uploaded file
    maxRequestSize    = 10 * 1024 * 1024  // 10 MB max total request size
)
public class ReportIssueServlet extends HttpServlet {

    // Subdirectory under the web application root where photos are stored
    private static final String UPLOAD_DIR = "uploads" + File.separator + "photos";

    private IssueDAO issueDAO;

    @Override
    public void init() throws ServletException {
        issueDAO = new IssueDAO();
    }

    // -----------------------------------------------------------------------
    // GET /citizen/reportIssue — Show the report form
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ensure user is logged in and is a CITIZEN
        if (!isAuthorizedCitizen(request, response)) return;

        request.getRequestDispatcher("/WEB-INF/views/report_issue.jsp")
               .forward(request, response);
    }

    // -----------------------------------------------------------------------
    // POST /citizen/reportIssue — Process the submitted form + file
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Authorization check
        if (!isAuthorizedCitizen(request, response)) return;

        HttpSession session     = request.getSession(false);
        User loggedInUser       = (User) session.getAttribute("loggedInUser");

        // 2. Read text form parameters
        String category    = request.getParameter("category");
        String gpsLocation = request.getParameter("gpsLocation");
        String description = request.getParameter("description");

        // 3. --- Handle Photo Upload ---
        String photoUrl = null;
        Part photoPart  = request.getPart("photo"); // "photo" = input name in report_issue.jsp

        if (photoPart != null && photoPart.getSize() > 0) {
            // 3a. Determine the absolute path on the server to save photos
            //     getRealPath gives the physical path on the server's filesystem.
            String appPath    = getServletContext().getRealPath("");
            String uploadPath = appPath + File.separator + UPLOAD_DIR;

            // 3b. Create the directory if it does not exist
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 3c. Extract the original filename from the Part header
            String submittedFileName = extractFileName(photoPart);
            String fileExtension     = "";
            if (submittedFileName != null && submittedFileName.contains(".")) {
                fileExtension = submittedFileName.substring(submittedFileName.lastIndexOf("."));
            }

            // 3d. Generate a unique filename using UUID to prevent overwrite + path traversal
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            String fullFilePath   = uploadPath + File.separator + uniqueFileName;

            // 3e. Write the uploaded file bytes to disk
            try (InputStream fileStream = photoPart.getInputStream()) {
                Files.copy(fileStream, Paths.get(fullFilePath), StandardCopyOption.REPLACE_EXISTING);
            }

            // 3f. Store only the relative URL in the DB (browser-accessible path)
            photoUrl = UPLOAD_DIR.replace(File.separator, "/") + "/" + uniqueFileName;
            System.out.println("[ReportIssueServlet] Photo saved: " + photoUrl);
        }

        // 4. Build the Issue POJO
        Issue issue = new Issue();
        issue.setCitizenId(loggedInUser.getUserId());
        issue.setCategory(category);
        issue.setGpsLocation(gpsLocation);
        issue.setDescription(description);
        issue.setPhotoUrl(photoUrl);
        // Status defaults to 'OPEN' inside IssueDAO.createIssue()

        // 5. Persist the issue via DAO
        boolean success = issueDAO.createIssue(issue);

        if (success) {
            // Success: redirect to citizen dashboard (POST-Redirect-GET pattern)
            request.getSession().setAttribute("successMessage",
                    "Your issue has been reported successfully!");
            response.sendRedirect(request.getContextPath() + "/citizen/dashboard");
        } else {
            // Failure: show error on the form page
            request.setAttribute("errorMessage",
                    "Failed to submit your issue. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/report_issue.jsp")
                   .forward(request, response);
        }
    }

    // -----------------------------------------------------------------------
    // Helper — Extract filename from the Content-Disposition header of a Part
    // -----------------------------------------------------------------------
    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) return null;
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                // Format: filename="example.jpg"
                String fname = token.substring(token.indexOf('=') + 1).trim()
                                    .replace("\"", "");
                // Return only the base filename (strip any path the browser may have sent)
                return Paths.get(fname).getFileName().toString();
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Helper — Check session and role; redirect to /login if unauthorized
    // -----------------------------------------------------------------------
    private boolean isAuthorizedCitizen(HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        User user = (User) session.getAttribute("loggedInUser");
        if (!"CITIZEN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
