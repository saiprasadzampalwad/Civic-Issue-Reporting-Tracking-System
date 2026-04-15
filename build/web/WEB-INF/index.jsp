<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Redirect root URL to the login servlet --%>
<% response.sendRedirect(request.getContextPath() + "/login"); %>

