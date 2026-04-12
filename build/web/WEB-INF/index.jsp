<%-- 
    Document   : index
    Created on : 12 Apr, 2026, 8:22:04 PM
    Author     : HP
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Redirect root URL to the login servlet --%>
<% response.sendRedirect(request.getContextPath() + "/login"); %>
