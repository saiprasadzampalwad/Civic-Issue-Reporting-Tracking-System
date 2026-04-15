<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Register — Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
body{background:linear-gradient(135deg,#1a3c6e,#2d6a9f);min-height:100vh;display:flex;align-items:center;justify-content:center}
.card{border:none;border-radius:16px;box-shadow:0 8px 32px rgba(0,0,0,.3)}
.card-header{background:#1a3c6e;color:#fff;padding:20px 28px;border-radius:16px 16px 0 0;text-align:center}
.card-body{padding:28px}
.btn-primary{background:#1a3c6e;border-color:#1a3c6e}.btn-primary:hover{background:#14305a}
</style>
</head>
<body>
<div class="container"><div class="row justify-content-center"><div class="col-md-5 col-sm-10">
<div class="card">
  <div class="card-header"><h2 style="font-size:1.3rem;margin:0">Create Account</h2></div>
  <div class="card-body">
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2"><c:out value="${errorMessage}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/register" method="post">
      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
      <div class="mb-3">
        <label class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
        <input type="text" class="form-control" name="name" required autofocus>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold">Phone <span class="text-danger">*</span></label>
        <input type="tel" class="form-control" name="phone" required autofocus>
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
      Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a>
    </p>
  </div>
</div>
</div></div></div>
</body></html>
