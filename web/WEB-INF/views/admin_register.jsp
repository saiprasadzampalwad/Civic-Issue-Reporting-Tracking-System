<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Admin Register — Municipal Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
body{background:linear-gradient(135deg,#1e3a28,#059669);min-height:100vh;display:flex;align-items:center;justify-content:center}
.card{border:none;border-radius:16px;box-shadow:0 8px 32px rgba(0,0,0,.3)}
.card-header{background:#1e3a28;color:#fff;padding:20px 28px;border-radius:16px 16px 0 0;text-align:center}
.card-header h2{font-size:1.3rem;margin:0}
.card-body{padding:28px}
.btn-primary{background:#1e3a28;border-color:#1e3a28}.btn-primary:hover{background:#166534}
.form-select:focus{border-color:#059669;box-shadow:0 0 0 .2rem rgba(5,150,105,.25)}
</style>
</head>
<body>
<div class="container"><div class="row justify-content-center"><div class="col-md-6 col-sm-10">
<div class="card">
  <div class="card-header"><h2 style="font-size:1.3rem;margin:0">Admin/Crew Account Setup</h2></div>
  <div class="card-body">
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2"><c:out value="${errorMessage}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/admin/register" method="post">
      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
      <div class="mb-3">
        <label class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
        <input type="text" class="form-control" name="name" value="${param.name}" required autofocus>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold">Phone <span class="text-danger">*</span></label>
        <input type="tel" class="form-control" name="phone" value="${param.phone}" required>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold">Role <span class="text-danger">*</span></label>
        <select class="form-select" name="role" required>
          <option value="">Select Role</option>
          <option value="CITIZEN" ${param.role == 'CITIZEN' ? 'selected' : ''}>Citizen</option>
          <option value="MUNICIPAL_ADMIN" ${param.role == 'MUNICIPAL_ADMIN' ? 'selected' : ''}>Municipal Admin</option>
          <option value="MAINTENANCE_CREW" ${param.role == 'MAINTENANCE_CREW' ? 'selected' : ''}>Maintenance Crew</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold">Department (for Admin/Crew) <span class="text-danger">*</span></label>
        <select class="form-select" name="department_id">
          <option value="">No Department</option>
          <c:forEach var="dept" items="${departments}">
            <option value="${dept.id}" ${param.department_id == dept.id ? 'selected' : ''}>${dept.name}</option>
          </c:forEach>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
        <input type="password" class="form-control" name="password" minlength="6" required>
        <div class="form-text">Minimum 6 characters</div>
      </div>
      <div class="mb-4">
        <label class="form-label fw-semibold">Confirm Password <span class="text-danger">*</span></label>
        <input type="password" class="form-control" name="confirm" required>
      </div>
      <div class="d-grid mb-3"><button type="submit" class="btn btn-primary btn-lg">Register</button></div>
    </form>
    <p class="text-center mb-0" style="font-size:.85rem">
      Have account? <a href="${pageContext.request.contextPath}/admin/login">Admin Sign In</a>
    </p>
  </div>
</div>
</div></div></div>
</body>
</html>

