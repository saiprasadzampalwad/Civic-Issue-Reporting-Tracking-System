<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>My Dashboard — Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
body{background:#f0f4f8}.navbar{background:#1a3c6e!important}.navbar-brand,.navbar-text{color:#fff!important}
.status-OPEN{background:#dc3545;color:#fff}.status-ASSIGNED{background:#fd7e14;color:#fff}
.status-IN_PROGRESS{background:#0d6efd;color:#fff}.status-RESOLVED{background:#198754;color:#fff}
.status-CLOSED{background:#6c757d;color:#fff}
.status-badge{padding:3px 10px;border-radius:20px;font-size:.75rem;font-weight:600;white-space:nowrap}
.card{border:none;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.07)}
.stat-card{background:#fff;border:none;border-radius:10px;box-shadow:0 1px 8px rgba(0,0,0,.06);text-align:center;padding:14px}
th{background:#1a3c6e;color:#fff}
.star{color:#f5a623;font-size:1.1rem}
.sla-breach{background:#fff3cd!important}
</style>
</head>
<body>
<nav class="navbar navbar-expand-lg">
  <div class="container">
    <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal</a>
    <span class="navbar-text ms-auto me-3">
      <c:out value="${sessionScope.loggedInUser.name}"/> <span class="badge bg-light text-dark">CITIZEN</span>
    </span>
    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">Logout</a>
  </div>
</nav>

<div class="container py-4">
  <%-- Messages --%>
  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show"><c:out value="${successMessage}"/>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
  </c:if>

  <%-- Header row --%>
  <div class="d-flex align-items-center justify-content-between mb-4">
    <h4 class="mb-0 fw-bold">My Reported Issues</h4>
    <a href="${pageContext.request.contextPath}/citizen/reportIssue" class="btn btn-success">&#43; Report Issue</a>
  </div>

  <%-- Filter bar --%>
  <form method="get" action="${pageContext.request.contextPath}/citizen/dashboard" class="row g-2 mb-4">
    <div class="col-auto">
      <select name="filterStatus" class="form-select form-select-sm">
        <option value="">All Statuses</option>
        <option value="OPEN"        <c:if test="${filterStatus eq 'OPEN'}">selected</c:if>>Open</option>
        <option value="ASSIGNED"    <c:if test="${filterStatus eq 'ASSIGNED'}">selected</c:if>>Assigned</option>
        <option value="IN_PROGRESS" <c:if test="${filterStatus eq 'IN_PROGRESS'}">selected</c:if>>In Progress</option>
        <option value="RESOLVED"    <c:if test="${filterStatus eq 'RESOLVED'}">selected</c:if>>Resolved</option>
        <option value="CLOSED"      <c:if test="${filterStatus eq 'CLOSED'}">selected</c:if>>Closed</option>
      </select>
    </div>
    <div class="col-auto">
      <select name="filterCategory" class="form-select form-select-sm">
        <option value="">All Categories</option>
        <option>Road Damage</option><option>Streetlight</option><option>Garbage</option>
        <option>Water Supply</option><option>Sewage</option><option>Park</option><option>Noise</option><option>Other</option>
      </select>
    </div>
    <div class="col-auto"><button class="btn btn-outline-secondary btn-sm" type="submit">Filter</button></div>
    <div class="col-auto"><a href="${pageContext.request.contextPath}/citizen/dashboard" class="btn btn-link btn-sm">Clear</a></div>
  </form>

  <%-- Issues table --%>
  <div class="card">
    <div class="card-body p-0">
      <c:choose>
        <c:when test="${empty myIssues}">
          <div class="text-center py-5">
            <p class="text-muted fs-5">No issues found.</p>
            <a href="${pageContext.request.contextPath}/citizen/reportIssue" class="btn btn-primary">Report Your First Issue</a>
          </div>
        </c:when>
        <c:otherwise>
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead><tr><th>#</th><th>Category</th><th>Description</th><th>GPS</th><th>Status</th><th>Photos</th><th>Rate / Feedback</th></tr></thead>
              <tbody>
                <c:forEach var="issue" items="${myIssues}">
                  <tr>
                    <td class="text-muted">#<c:out value="${issue.issueId}"/></td>
                    <td><strong><c:out value="${issue.category}"/></strong></td>
                    <td style="max-width:200px;font-size:.87rem"><c:out value="${issue.description}"/></td>
                    <td style="font-size:.82rem"><c:out value="${issue.gpsLocation}"/></td>
                    <td><span class="status-badge status-${issue.status}"><c:out value="${issue.status}"/></span></td>
                    <td>
                      <c:choose>
                        <c:when test="${not empty issue.photoUrls}">
                          <c:forEach var="url" items="${issue.photoUrls}" varStatus="s">
                            <a href="${pageContext.request.contextPath}/${url}" target="_blank" class="btn btn-sm btn-outline-secondary">
                              Photo <c:out value="${s.index+1}"/>
                            </a>
                          </c:forEach>
                        </c:when>
                        <c:otherwise><span class="text-muted">—</span></c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <%-- Rating: only show for RESOLVED issues --%>
                      <c:if test="${issue.status eq 'RESOLVED'}">
                        <c:choose>
                          <c:when test="${not empty issue.rating}">
                            <c:forEach begin="1" end="${issue.rating.stars}"><span class="star">&#9733;</span></c:forEach>
                          </c:when>
                          <c:otherwise>
                            <button class="btn btn-sm btn-outline-warning" data-bs-toggle="modal"
                              data-bs-target="#rateModal" data-issueid="${issue.issueId}">Rate</button>
                          </c:otherwise>
                        </c:choose>
                      </c:if>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
          <%-- Pagination --%>
          <c:if test="${totalPages > 1}">
            <div class="d-flex justify-content-center py-3 gap-2">
              <c:if test="${currentPage > 1}">
                <a href="?page=${currentPage-1}&filterStatus=${filterStatus}&filterCategory=${filterCategory}" class="btn btn-sm btn-outline-secondary">&laquo; Prev</a>
              </c:if>
              <span class="btn btn-sm btn-secondary disabled">Page ${currentPage} / ${totalPages}</span>
              <c:if test="${currentPage < totalPages}">
                <a href="?page=${currentPage+1}&filterStatus=${filterStatus}&filterCategory=${filterCategory}" class="btn btn-sm btn-outline-secondary">Next &raquo;</a>
              </c:if>
            </div>
          </c:if>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <%-- Rating Modal --%>
  <div class="modal fade" id="rateModal" tabindex="-1">
    <div class="modal-dialog modal-sm">
      <div class="modal-content">
        <div class="modal-header"><h5 class="modal-title">Rate this Issue</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
        <form action="${pageContext.request.contextPath}/issue/rate" method="post">
          <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
          <input type="hidden" name="issueId"   id="rateIssueId"/>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label fw-semibold">Stars (1–5)</label>
              <select class="form-select" name="stars" required>
                <option value="5">&#9733;&#9733;&#9733;&#9733;&#9733; Excellent</option>
                <option value="4">&#9733;&#9733;&#9733;&#9733; Good</option>
                <option value="3">&#9733;&#9733;&#9733; Average</option>
                <option value="2">&#9733;&#9733; Poor</option>
                <option value="1">&#9733; Very Poor</option>
              </select>
            </div>
            <div class="mb-2">
              <label class="form-label fw-semibold">Feedback (optional)</label>
              <textarea class="form-control" name="feedback" rows="3"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="submit" class="btn btn-warning">Submit Rating</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
var rateModal = document.getElementById('rateModal');
if(rateModal){rateModal.addEventListener('show.bs.modal',function(e){
  document.getElementById('rateIssueId').value = e.relatedTarget.getAttribute('data-issueid');
});}
</script>
</body></html>
