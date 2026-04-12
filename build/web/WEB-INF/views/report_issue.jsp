<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Report Issue — Civic Issue System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .navbar { background: #1a3c6e !important; }
        .navbar-brand, .nav-link, .navbar-text { color: #fff !important; }
        .form-card { max-width: 700px; margin: 40px auto; }
        .form-card .card { border:none; border-radius:14px; box-shadow:0 2px 18px rgba(0,0,0,0.1); }
        .form-card .card-header {
            background: #1a3c6e; color:#fff;
            border-radius: 14px 14px 0 0;
            padding: 18px 28px;
            font-size: 1.15rem; font-weight: 600;
        }
        .form-card .card-body { padding: 28px; }
        .btn-primary { background: #1a3c6e; border-color: #1a3c6e; }
        .btn-primary:hover { background: #14305a; }
        #gpsStatus { font-size: 0.82rem; }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg">
    <div class="container">
        <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal</a>
        <a href="${pageContext.request.contextPath}/citizen/dashboard" class="btn btn-outline-light btn-sm ms-auto">
            &larr; Back to Dashboard
        </a>
    </div>
</nav>

<div class="container form-card">
    <div class="card">
        <div class="card-header">&#43; Report a Civic Issue</div>
        <div class="card-body">

            <%-- Error message --%>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
            </c:if>

            <%--
                IMPORTANT: enctype="multipart/form-data" is REQUIRED for file upload.
                Without it, the photo Part will be empty in ReportIssueServlet.
            --%>
            <form action="${pageContext.request.contextPath}/citizen/reportIssue"
                  method="post"
                  enctype="multipart/form-data"
                  novalidate>

                <%-- Issue Category --%>
                <div class="mb-3">
                    <label for="category" class="form-label fw-semibold">Issue Category <span class="text-danger">*</span></label>
                    <select class="form-select" id="category" name="category" required>
                        <option value="" disabled selected>Select a category...</option>
                        <option value="Road Damage">Road Damage / Potholes</option>
                        <option value="Streetlight">Streetlight Not Working</option>
                        <option value="Garbage">Garbage / Waste Collection</option>
                        <option value="Water Supply">Water Supply Problem</option>
                        <option value="Sewage">Sewage / Drainage Issue</option>
                        <option value="Park">Park / Public Space</option>
                        <option value="Noise">Noise Complaint</option>
                        <option value="Other">Other</option>
                    </select>
                </div>

                <%-- Description --%>
                <div class="mb-3">
                    <label for="description" class="form-label fw-semibold">Description <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="description" name="description"
                              rows="4" placeholder="Describe the issue in detail..." required></textarea>
                </div>

                <%-- GPS Location (auto-detect + manual) --%>
                <div class="mb-3">
                    <label for="gpsLocation" class="form-label fw-semibold">GPS Location <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <input type="text" class="form-control" id="gpsLocation" name="gpsLocation"
                               placeholder="e.g. 21.1458, 79.0882" required>
                        <button type="button" class="btn btn-outline-secondary" onclick="detectLocation()">
                            &#128205; Detect
                        </button>
                    </div>
                    <div id="gpsStatus" class="text-muted mt-1"></div>
                </div>

                <%-- Photo Upload --%>
                <div class="mb-4">
                    <label for="photo" class="form-label fw-semibold">Photo (optional, max 5 MB)</label>
                    <input type="file" class="form-control" id="photo" name="photo"
                           accept="image/jpeg,image/png,image/gif,image/webp">
                    <div class="form-text">Accepted formats: JPG, PNG, GIF, WEBP</div>
                </div>

                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-primary px-4">Submit Issue</button>
                    <a href="${pageContext.request.contextPath}/citizen/dashboard"
                       class="btn btn-outline-secondary">Cancel</a>
                </div>

            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Auto-detect GPS coordinates using the browser's Geolocation API
    function detectLocation() {
        var statusEl = document.getElementById('gpsStatus');
        var inputEl  = document.getElementById('gpsLocation');

        if (!navigator.geolocation) {
            statusEl.textContent = 'Geolocation is not supported by your browser.';
            statusEl.className = 'text-danger mt-1';
            return;
        }

        statusEl.textContent = 'Detecting location...';
        statusEl.className = 'text-muted mt-1';

        navigator.geolocation.getCurrentPosition(
            function(position) {
                var lat = position.coords.latitude.toFixed(6);
                var lng = position.coords.longitude.toFixed(6);
                inputEl.value = lat + ', ' + lng;
                statusEl.textContent = 'Location detected successfully.';
                statusEl.className = 'text-success mt-1';
            },
            function(error) {
                statusEl.textContent = 'Could not detect location: ' + error.message;
                statusEl.className = 'text-danger mt-1';
            }
        );
    }
</script>
</body>
</html>