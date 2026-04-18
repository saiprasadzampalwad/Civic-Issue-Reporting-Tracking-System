<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Report Issue — Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
body{background:#f0f4f8}.navbar{background:#1a3c6e!important}.navbar-brand,.navbar-text{color:#fff!important}
.card{border:none;border-radius:14px;box-shadow:0 2px 18px rgba(0,0,0,.1)}
.card-header{background:#1a3c6e;color:#fff;border-radius:14px 14px 0 0;padding:16px 24px;font-size:1.05rem;font-weight:600}
.btn-primary{background:#1a3c6e;border-color:#1a3c6e}.btn-primary:hover{background:#14305a}
</style>
</head>
<body>
<nav class="navbar navbar-expand-lg">
  <div class="container">
    <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal</a>
    <a href="${pageContext.request.contextPath}/citizen/dashboard" class="btn btn-outline-light btn-sm ms-auto">&larr; Dashboard</a>
  </div>
</nav>
<div class="container py-4" style="max-width:680px">
  <div class="card">
    <div class="card-header">&#43; Report a Civic Issue</div>
    <div class="card-body p-4">
      <c:if test="${not empty errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}"/></div></c:if>
      <%-- enctype MUST be multipart/form-data for file uploads --%>
      <form action="${pageContext.request.contextPath}/citizen/reportIssue" method="post" enctype="multipart/form-data">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>

        <div class="mb-3">
          <label class="form-label fw-semibold">Category <span class="text-danger">*</span></label>
          <select class="form-select" name="category" required>
            <option value="" disabled selected>Select category...</option>
            <option>Road Damage</option><option>Streetlight</option><option>Garbage</option>
            <option>Water Supply</option><option>Sewage</option><option>Park</option>
            <option>Noise</option><option>Other</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">Description <span class="text-danger">*</span></label>
          <textarea class="form-control" name="description" rows="4" required placeholder="Describe the issue..."></textarea>
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">GPS Location <span class="text-danger">*</span></label>
          <div class="input-group">
            <input type="text" class="form-control" id="gpsLocation" name="gpsLocation" placeholder="19.8758, 75.3393" required>
            <button type="button" class="btn btn-outline-secondary" onclick="detectGps()">&#128205; Detect</button>
          </div>
          <div id="gpsStatus" class="form-text"></div>
        </div>

        <div class="mb-3">
          <label class="form-label fw-semibold">Photos (up to 3, max 5 MB each)</label>
          <input type="file" class="form-control mb-2" name="photo1" accept="image/*">
          <input type="file" class="form-control mb-2" name="photo2" accept="image/*">
          <input type="file" class="form-control"      name="photo3" accept="image/*">
        </div>

        <div class="d-flex gap-2 mt-4">
          <button type="submit" class="btn btn-primary px-4">Submit Issue</button>
          <a href="${pageContext.request.contextPath}/citizen/dashboard" class="btn btn-outline-secondary">Cancel</a>
        </div>
      </form>
    </div>
  </div>
</div>
<script>
function detectGps(){
  var s=document.getElementById('gpsStatus'), i=document.getElementById('gpsLocation');
  if(!navigator.geolocation){s.textContent='Not supported.';return;}
  s.textContent='Detecting...';
  navigator.geolocation.getCurrentPosition(function(p){
    i.value=p.coords.latitude.toFixed(6)+', '+p.coords.longitude.toFixed(6);
    s.textContent='Location detected.';s.className='form-text text-success';
  },function(e){s.textContent='Error: '+e.message;s.className='form-text text-danger';});
}
</script>
</body></html>
