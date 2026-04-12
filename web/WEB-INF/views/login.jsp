<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Civic Issue Reporting System</title>
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #1a3c6e 0%, #2d6a9f 100%);
            min-height: 100vh;
            display: flex; align-items: center; justify-content: center;
        }
        .login-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 8px 32px rgba(0,0,0,0.3);
        }
        .login-header {
            background: #1a3c6e;
            color: #fff;
            border-radius: 16px 16px 0 0;
            padding: 28px 32px 20px;
            text-align: center;
        }
        .login-header h2 { font-size: 1.5rem; margin-bottom: 4px; }
        .login-header p  { font-size: 0.88rem; opacity: 0.75; margin: 0; }
        .login-body { padding: 32px; }
        .btn-primary { background: #1a3c6e; border-color: #1a3c6e; }
        .btn-primary:hover { background: #14305a; }
    </style>
</head>
<body>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-5 col-sm-9">
            <div class="card login-card">

                <!-- Header -->
                <div class="login-header">
                    <h2>&#127981; Civic Issue Portal</h2>
                    <p>Municipal Reporting System — Sign In</p>
                </div>

                <!-- Body -->
                <div class="login-body">

                    <%-- Show error message if authentication failed --%>
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger py-2" role="alert">
                            <c:out value="${errorMessage}"/>
                        </div>
                    </c:if>

                    <%-- Login Form — posts to LoginServlet --%>
                    <form action="${pageContext.request.contextPath}/login" method="post" novalidate>

                        <div class="mb-3">
                            <label for="email" class="form-label fw-semibold">Email Address</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="you@example.com" required autofocus>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label fw-semibold">Password</label>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="Enter your password" required>
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary btn-lg">
                                Sign In &rarr;
                            </button>
                        </div>
                    </form>

                </div><!-- /login-body -->
            </div><!-- /card -->
            <p class="text-center text-white mt-3" style="font-size:0.82rem; opacity:0.7;">
                Civic Issue Reporting System &copy; 2024
            </p>
        </div>
    </div>
</div>
</body>
</html>
