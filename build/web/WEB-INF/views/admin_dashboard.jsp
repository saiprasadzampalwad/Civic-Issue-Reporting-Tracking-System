<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — Civic Issue System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .navbar { background: #1a3c6e !important; }
        .navbar-brand, .navbar-text { color: #fff !important; }
        .page-header { background:#fff; border-bottom:3px solid #c0392b; padding:20px 0; margin-bottom:28px; }
        th { background:#1a3c6e; color:#fff; white-space:nowrap; }
        .status-badge-OPEN        { background:#dc3545; color:#fff; }
        .status-badge-ASSIGNED    { background:#fd7e14; color:#fff; }
        .status-badge-IN_PROGRESS { background:#0d6efd; color:#fff; }
        .status-badge-RESOLVED    { background:#198754; color:#fff; }
        .status-badge-CLOSED      { background:#6c757d; color:#fff; }
        .status-badge { padding:3px 10px; border-radius:20px; font-size:0.78rem; font-weight:600; }
        .card { border:none; border-radius:12px; box-shadow:0 2px 12px rgba(0,0,0,0.07); }
        .update-form select { min-width:130px; font-size:0.85rem; }
        .update-form input[type=text] { font-size:0.85rem; }
        .update-form .btn { font-size:0.82rem; }
    </style>
</head>
<body>

<%-- ===================== NAVBAR ===================== --%>
<nav class="navbar navbar-expand-lg">
    <div class="container">
        <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal</a>
        <span class="navbar-text ms-auto me-3">
            <c:out value="${sessionScope.loggedInUser.name}"/>
            &nbsp;<span class="badge bg-warning text-dark">
                <c:out value="${sessionScope.loggedInUser.role}"/>
            </span>
        </span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">Logout</a>
    </div>
</nav>

<%-- ===================== PAGE HEADER ===================== --%>
<div class="page-header">
    <div class="container">
        <h4 class="mb-0 fw-bold">Active Issues — Admin Dashboard</h4>
        <p class="text-muted mb-0" style="font-size:0.9rem;">Review and update the status of all open civic issues</p>
    </div>
</div>

<div class="container">

    <%-- Feedback message (success or failure after status update) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <c:out value="${successMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card">
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${empty openIssues}">
                    <div class="text-center py-5">
                        <p class="text-muted fs-5">&#10003; No active issues at the moment.</p>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Issue #</th>
                                    <th>Reported By</th>
                                    <th>Category</th>
                                    <th>Description</th>
                                    <th>GPS</th>
                                    <th>Current Status</th>
                                    <th>Photo</th>
                                    <th style="min-width:340px;">Update Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%--
                                    c:forEach iterates over openIssues (List<Issue>) set by AdminDashboardServlet.
                                    Each row has an inline form that posts to UpdateStatusServlet.
                                --%>
                                <c:forEach var="issue" items="${openIssues}">
                                    <tr>
                                        <td class="text-muted fw-semibold">#${issue.issueId}</td>
                                        <td><c:out value="${issue.citizenName}"/></td>
                                        <td><strong><c:out value="${issue.category}"/></strong></td>
                                        <td style="max-width:180px; font-size:0.87rem;">
                                            <c:out value="${issue.description}"/>
                                        </td>
                                        <td style="font-size:0.82rem; color:#555;">
                                            <c:out value="${issue.gpsLocation}"/>
                                        </td>
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
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>

                                        <%-- Inline update form for each issue row --%>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/admin/updateStatus"
                                                  method="post"
                                                  class="update-form d-flex gap-1 flex-wrap">

                                                <%-- Hidden field passes the issue ID --%>
                                                <input type="hidden" name="issueId" value="${issue.issueId}"/>

                                                <%-- Status dropdown --%>
                                                <select name="newStatus" class="form-select form-select-sm" required>
                                                    <option value="" disabled selected>-- Set Status --</option>
                                                    <option value="ASSIGNED">ASSIGNED</option>
                                                    <option value="IN_PROGRESS">IN PROGRESS</option>
                                                    <option value="RESOLVED">RESOLVED</option>
                                                    <option value="CLOSED">CLOSED</option>
                                                </select>

                                                <%-- Optional notes input --%>
                                                <input type="text" name="notes" class="form-control form-control-sm"
                                                       placeholder="Notes (optional)" style="min-width:110px;">

                                                <button type="submit" class="btn btn-primary btn-sm">
                                                    Update
                                                </button>
                                            </form>
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
        Active issues shown: <strong>${openIssues.size()}</strong>
    </p>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>


