<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    _layout.jsp — shared sidebar + header shell.
    Usage: include at top of every protected page.
    Caller must set: pageTitle, activeNav
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} — My Personal Library</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="app">
    <aside class="sidebar">
        <div class="profile">
            <div class="profile-avatar">
                ${sessionScope.currentUser.initials}
            </div>
            <div>
                <div class="profile-name">${sessionScope.userName}</div>
                <div class="profile-handle">${sessionScope.currentUser.handle}</div>
            </div>
        </div>

        <nav class="nav-links">
            <a href="${pageContext.request.contextPath}/dashboard"
               class="nav-link ${activeNav == 'dashboard' ? 'active' : ''}">
                Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/books"
               class="nav-link ${activeNav == 'books' ? 'active' : ''}">
                My Books
            </a>
            <a href="${pageContext.request.contextPath}/books/create"
               class="nav-link ${activeNav == 'addbook' ? 'active' : ''}">
                Add Book
            </a>
            <a href="${pageContext.request.contextPath}/favorites"
               class="nav-link ${activeNav == 'favorites' ? 'active' : ''}">
                Favorites
            </a>
            <a href="${pageContext.request.contextPath}/authors"
               class="nav-link ${activeNav == 'authors' ? 'active' : ''}">
                Authors
            </a>
            <a href="${pageContext.request.contextPath}/categories"
               class="nav-link ${activeNav == 'categories' ? 'active' : ''}">
                Categories
            </a>
            <a href="${pageContext.request.contextPath}/goals"
               class="nav-link ${activeNav == 'goals' ? 'active' : ''}">
                Goals
            </a>
            <a href="${pageContext.request.contextPath}/settings/profile"
               class="nav-link ${activeNav == 'settings' ? 'active' : ''}">
                Settings
            </a>
        </nav>

        <div class="logout">
            <form method="POST" action="${pageContext.request.contextPath}/logout">
                <button type="submit">Logout</button>
            </form>
        </div>
    </aside>

    <main class="main">
        <header class="topbar">
            <form class="global-search" action="${pageContext.request.contextPath}/books" method="GET">
                <input type="search" name="search" placeholder="Search books, authors, or categories..."
                       value="${param.search}" aria-label="Search books">
            </form>
        </header>

        <div class="page-content">
            <%-- Flash message --%>
            <c:if test="${not empty sessionScope.flash}">
                <div class="alert-success">${sessionScope.flash}</div>
                <c:remove var="flash" scope="session"/>
            </c:if>
            <%-- Error message passed as request attribute --%>
            <c:if test="${not empty requestScope.error}">
                <div class="alert-error">${requestScope.error}</div>
            </c:if>
