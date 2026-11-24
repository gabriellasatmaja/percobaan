<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log in — My Personal Library</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="auth-page">
<div class="auth-container">
    <div class="auth-card">
        <h1 class="auth-title">Log in to your account</h1>
        <p class="auth-subtitle">Enter your email and password below to log in</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form method="POST" action="${pageContext.request.contextPath}/login" class="auth-form">
            <div class="form-group">
                <label for="email">Email address</label>
                <input class="form-control" type="email" id="email" name="email"
                       value="${param.email}" required autofocus placeholder="email@example.com">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input class="form-control" type="password" id="password" name="password"
                       required placeholder="Password">
            </div>
            <button type="submit" class="primary-btn auth-submit">Log in</button>
        </form>

        <p class="auth-footer">
            Don't have an account?
            <a href="${pageContext.request.contextPath}/register">Sign up</a>
        </p>
    </div>
</div>
</body>
</html>
