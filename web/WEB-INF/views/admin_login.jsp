<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Admin Login — Municipal Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
body{background:linear-gradient(135deg,#1e3a28,#059669);min-height:100vh;display:flex;align-items:center;justify-content:center}
.card{border:none;border-radius:16px;box-shadow:0 8px 32px rgba(0,0,0,.3);overflow:hidden}
.card-header{background:#1e3a28;color:#fff;padding:24px 28px;text-align:center}
.card-header h2{font-size:1.4rem;margin:0 0 4px}
.card-header p{font-size:.82rem;opacity:.7;margin:0}
.card-body{padding:28px}
.btn-primary{background:#1e3a28;border-color:#1e3a28}.btn-primary:hover{background:#166534}
</style>
</head>
<body>
<div class="container"><div class="row justify-content-center"><div class="col-md-5 col-sm-10">
<div class="card">
  <div class="card-header"><h2>&#128475;&#127981; Municipal Admin Portal</h2><p>Dashboard Access</p></div>
  <div class="card-body">
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2"><c:out value="${errorMessage}"/></div></c:if>
    <c:if test="${not empty successMessage}"><div class="alert alert-success py-2"><c:out value="${successMessage}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/login" method="post">
      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
      <div class="mb-3">
        <label class="form-label fw-semibold">Admin Phone</label>
        <input type="tel" class="form-control" name="phone" autofocus placeholder="+1234567890" required>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold">Password</label>
        <input type="password" class="form-control" name="password" required>
      </div>
      <div class="d-grid mb-3"><button type="submit" class="btn btn-primary btn-lg">Admin Sign In</button></div>
    </form>
    <div class="text-center" style="font-size:.85rem">
      <a href="${pageContext.request.contextPath}/login" class="text-decoration-none">Citizen login</a> |
      <a href="${pageContext.request.contextPath}/admin/register" class="text-decoration-none">Register Admin/Crew</a>
    </div>
    <hr><p class="text-center mb-0" style="font-size:.78rem;color:#888">
      <a href="${pageContext.request.contextPath}/map" class="text-decoration-none text-muted">&#128506; View public issue map</a>
    </p>
  </div>
</div>
</div></div></div>
</body>
</html>

