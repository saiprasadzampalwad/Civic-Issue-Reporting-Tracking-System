<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Admin Dashboard — Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<style>
body{background:#f0f4f8}.navbar{background:#1a3c6e!important}.navbar-brand,.navbar-text{color:#fff!important}
.status-OPEN{background:#dc3545;color:#fff}.status-ASSIGNED{background:#fd7e14;color:#fff}
.status-IN_PROGRESS{background:#0d6efd;color:#fff}.status-RESOLVED{background:#198754;color:#fff}
.status-CLOSED{background:#6c757d;color:#fff}
.status-badge{padding:3px 10px;border-radius:20px;font-size:.75rem;font-weight:600}
.card{border:none;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,.07)}
.stat-card{background:#fff;border:none;border-radius:10px;box-shadow:0 1px 8px rgba(0,0,0,.06);padding:16px;text-align:center}
.stat-num{font-size:1.8rem;font-weight:600;color:#1a3c6e}.stat-lbl{font-size:.8rem;color:#888}
th{background:#1a3c6e;color:#fff;white-space:nowrap}
.sla-breach{background:#fff3cd!important}
.star{color:#f5a623}
</style>
</head>
<body>
<nav class="navbar navbar-expand-lg">
  <div class="container-fluid px-4">
    <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal</a>
    <span class="navbar-text ms-auto me-3">
      <c:out value="${sessionScope.loggedInUser.name}"/>
      <span class="badge bg-warning text-dark"><c:out value="${sessionScope.loggedInUser.role}"/></span>
    </span>
    <a href="${pageContext.request.contextPath}/admin/exportCsv" class="btn btn-outline-light btn-sm me-2">&#8595; Export CSV</a>
    <a href="${pageContext.request.contextPath}/map" class="btn btn-outline-light btn-sm me-2">&#128506; Map</a>
    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">Logout</a>
  </div>
</nav>

<div class="container-fluid px-4 py-4">

  <%-- Feedback --%>
  <c:if test="${not empty successMessage}">
    <div class="alert alert-info alert-dismissible fade show"><c:out value="${successMessage}"/>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
  </c:if>

  <%-- Analytics Stats Row --%>
  <div class="row g-3 mb-4">
    <div class="col-6 col-md-2"><div class="stat-card"><div class="stat-num">${stats.totalIssues}</div><div class="stat-lbl">Total Issues</div></div></div>
    <div class="col-6 col-md-2"><div class="stat-card"><div class="stat-num text-danger">${stats.openCount}</div><div class="stat-lbl">Open</div></div></div>
    <div class="col-6 col-md-2"><div class="stat-card"><div class="stat-num text-primary">${stats.inProgressCount}</div><div class="stat-lbl">In Progress</div></div></div>
    <div class="col-6 col-md-2"><div class="stat-card"><div class="stat-num text-success">${stats.resolvedCount}</div><div class="stat-lbl">Resolved</div></div></div>
    <div class="col-6 col-md-2"><div class="stat-card"><div class="stat-num text-warning">${stats.slaBreachedCount}</div><div class="stat-lbl">SLA Breached</div></div></div>
    <div class="col-6 col-md-2"><div class="stat-card"><div class="stat-num" style="font-size:1.3rem">${stats.avgResolutionHours}h</div><div class="stat-lbl">Avg. Resolution</div></div></div>
  </div>

  <%-- Charts Row --%>
  <div class="row g-3 mb-4">
    <div class="col-md-5">
      <div class="card p-3">
        <h6 class="fw-semibold mb-3">Issues by Category</h6>
        <canvas id="catChart" height="180"></canvas>
      </div>
    </div>
    <div class="col-md-7">
      <div class="card p-3">
        <h6 class="fw-semibold mb-3">Dept. Satisfaction Ratings</h6>
        <c:choose>
          <c:when test="${empty deptRatings}"><p class="text-muted" style="font-size:.87rem">No ratings yet.</p></c:when>
          <c:otherwise>
            <table class="table table-sm mb-0">
              <thead><tr><th>Department</th><th>Avg Stars</th><th>Ratings</th></tr></thead>
              <tbody>
                <c:forEach var="dr" items="${deptRatings}">
                  <tr>
                    <td><c:out value="${dr.department}"/></td>
                    <td><span class="star">&#9733;</span> <c:out value="${dr.avgStars}"/></td>
                    <td><c:out value="${dr.total}"/></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

  <%-- Filter --%>
  <form method="get" action="${pageContext.request.contextPath}/admin/dashboard" class="row g-2 mb-3">
    <div class="col-auto">
      <select name="filterStatus" class="form-select form-select-sm">
        <option value="">Active Issues</option>
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
    <div class="col-auto"><button class="btn btn-secondary btn-sm" type="submit">Filter</button></div>
    <div class="col-auto"><a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-link btn-sm">Clear</a></div>
  </form>

  <%-- Issues Table --%>
  <div class="card">
    <div class="card-body p-0">
      <c:choose>
        <c:when test="${empty openIssues}">
          <div class="text-center py-5"><p class="text-muted fs-5">&#10003; No active issues.</p></div>
        </c:when>
        <c:otherwise>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead><tr><th>#</th><th>Reporter</th><th>Category</th><th>Description</th><th>GPS</th><th>Status</th><th>SLA</th><th>Photos</th><th style="min-width:380px">Update Status</th></tr></thead>
              <tbody>
                <c:forEach var="issue" items="${openIssues}">
                  <tr class="${issue.slaBreached ? 'sla-breach' : ''}">
                    <td class="text-muted fw-semibold">#${issue.issueId}</td>
                    <td><c:out value="${issue.citizenName}"/></td>
                    <td><strong><c:out value="${issue.category}"/></strong></td>
                    <td style="max-width:160px;font-size:.85rem"><c:out value="${issue.description}"/></td>
                    <td style="font-size:.8rem"><c:out value="${issue.gpsLocation}"/></td>
                    <td><span class="status-badge status-${issue.status}"><c:out value="${issue.status}"/></span></td>
                    <td style="font-size:.78rem">
                      <c:choose>
                        <c:when test="${issue.slaBreached}"><span class="text-danger fw-bold">BREACHED</span></c:when>
                        <c:when test="${not empty issue.slaDeadline}">${issue.slaDeadline}</c:when>
                        <c:otherwise>—</c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <c:choose>
                        <c:when test="${not empty issue.photoUrls}">
                          <c:forEach var="url" items="${issue.photoUrls}" varStatus="s">
                            <a href="${pageContext.request.contextPath}/${url}" target="_blank" class="btn btn-xs btn-outline-secondary" style="font-size:.75rem;padding:2px 6px">P${s.index+1}</a>
                          </c:forEach>
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <form action="${pageContext.request.contextPath}/admin/updateStatus" method="post" class="d-flex gap-1 flex-wrap">
                        <input type="hidden" name="csrfToken"       value="${sessionScope.csrfToken}"/>
                        <input type="hidden" name="issueId"         value="${issue.issueId}"/>
                        <select name="newStatus" class="form-select form-select-sm" style="min-width:120px" required>
                          <option value="" disabled selected>-- Status --</option>
                          <option value="ASSIGNED">Assigned</option>
                          <option value="IN_PROGRESS">In Progress</option>
                          <option value="RESOLVED">Resolved</option>
                          <option value="CLOSED">Closed</option>
                        </select>
                        <select name="assignedDeptId" class="form-select form-select-sm" style="min-width:110px">
                          <option value="0">-- Dept --</option>
                          <c:forEach var="d" items="${departments}">
                            <option value="${d.id}" <c:if test="${d.id == issue.assignedDepartmentId}">selected</c:if>><c:out value="${d.name}"/></option>
                          </c:forEach>
                        </select>
                        <select name="assignedCrewId" class="form-select form-select-sm" style="min-width:100px">
                          <option value="0">-- Crew --</option>
                          <c:forEach var="c" items="${crewList}">
                            <option value="${c.id}" <c:if test="${c.id == issue.assignedCrewId}">selected</c:if>><c:out value="${c.name}"/></option>
                          </c:forEach>
                        </select>
                        <input type="text" name="notes" class="form-control form-control-sm" placeholder="Notes" style="min-width:90px">
                        <button type="submit" class="btn btn-primary btn-sm">Update</button>
                      </form>
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
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
// Build category chart from server data
var catData = [
  <c:forEach var="c" items="${stats.byCategory}" varStatus="s">
    {label:'<c:out value="${c.category}"/>', count:${c.count}}<c:if test="${!s.last}">,</c:if>
  </c:forEach>
];
if(catData.length > 0){
  new Chart(document.getElementById('catChart'),{
    type:'bar',
    data:{
      labels:catData.map(function(d){return d.label;}),
      datasets:[{data:catData.map(function(d){return d.count;}),
        backgroundColor:'#1a3c6e',borderRadius:4}]
    },
    options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,ticks:{stepSize:1}}}}
  });
}
</script>
</body></html>
