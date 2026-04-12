<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Dashboard — Civic Issue System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .navbar { background: #1a3c6e !important; }
        .navbar-brand, .nav-link, .navbar-text { color: #fff !important; }
        .page-header { background: #fff; border-bottom: 3px solid #1a3c6e; padding: 20px 0; margin-bottom: 28px; }
        .status-badge-OPEN        { background:#dc3545; color:#fff; }
        .status-badge-ASSIGNED    { background:#fd7e14; color:#fff; }
        .status-badge-IN_PROGRESS { background:#0d6efd; color:#fff; }
        .status-badge-RESOLVED    { background:#198754; color:#fff; }
        .status-badge-CLOSED      { background:#6c757d; color:#fff; }
        .status-badge { padding: 3px 10px; border-radius: 20px; font-size:0.78rem; font-weight:600; }
        .card { border: none; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.07); }
        th { background: #1a3c6e; color: #fff; }
    </style>
</head>
<body>

<%-- ===================== NAVBAR ===================== --%>
<nav class="navbar navbar-expand-lg">
    <div class="container">
        <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal</a>
        <span class="navbar-text ms-auto me-3">
            Welcome, <strong><c:out value="${sessionScope.loggedInUser.name}"/></strong>
            &nbsp;<span class="badge bg-light text-dark">CITIZEN</span>
        </span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">
            Logout
        </a>
    </div>
</nav>

<%-- ===================== PAGE HEADER ===================== --%>
<div class="page-header">
    <div class="container d-flex align-items-center justify-content-between">
        <div>
            <h4 class="mb-0 fw-bold text-dark">My Reported Issues</h4>
            <p class="text-muted mb-0" style="font-size:0.9rem;">Track the status of your submissions</p>
        </div>
        <a href="${pageContext.request.contextPath}/citizen/reportIssue"
           class="btn btn-success btn-lg">
            &#43; Report New Issue
        </a>
    </div>
</div>

<div class="container">

    <%-- One-time success message (e.g., after submitting a new issue) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <c:out value="${successMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card">
        <div class="card-body p-0">
            <c:choose>
                <%-- No issues yet --%>
                <c:when test="${empty myIssues}">
                    <div class="text-center py-5">
                        <p class="text-muted fs-5">You haven&apos;t reported any issues yet.</p>
                        <a href="${pageContext.request.contextPath}/citizen/reportIssue"
                           class="btn btn-primary">Report Your First Issue</a>
                    </div>
                </c:when>

                <%-- Issues table --%>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Category</th>
                                    <th>Description</th>
                                    <th>GPS Location</th>
                                    <th>Status</th>
                                    <th>Photo</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%-- JSTL c:forEach iterates over the myIssues List set by CitizenDashboardServlet --%>
                                <c:forEach var="issue" items="${myIssues}">
                                    <tr>
                                        <td class="text-muted">#<c:out value="${issue.issueId}"/></td>
                                        <td><strong><c:out value="${issue.category}"/></strong></td>
                                        <td style="max-width:220px;">
                                            <span title="${issue.description}">
                                                <%-- Truncate long descriptions with CSS --%>
                                                <c:out value="${issue.description}"/>
                                            </span>
                                        </td>
                                        <td><c:out value="${issue.gpsLocation}"/></td>
                                        <td>
                                            <span class="status-badge status-badge-${issue.status}">
                                                <c:out value="${issue.status}"/>
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty issue.photoUrl}">
                                                    <a href="${pageContext.request.contextPath}/${issue.photoUrl}"
                                                       target="_blank" class="btn btn-sm btn-outline-secondary">
                                                        View
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <p class="text-muted mt-3" style="font-size:0.82rem;">
        Total issues reported: <strong>${myIssues.size()}</strong>
    </p>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>