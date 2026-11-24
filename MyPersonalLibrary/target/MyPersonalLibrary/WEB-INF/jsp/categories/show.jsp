<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${category.name}" scope="request"/>
<c:set var="activeNav" value="categories" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading">
    <h1>${category.name}</h1>
    <p>${not empty category.description ? category.description : 'No description.'}</p>
</section>
<div class="form-actions" style="justify-content:flex-start;margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/categories" class="secondary-btn">← Categories</a>
    <a href="${pageContext.request.contextPath}/categories/${category.categoryId}/edit" class="primary-btn">Edit</a>
</div>
<div class="panel">
    <div class="section-heading"><h2>Books in ${category.name}</h2></div>
    <c:choose>
        <c:when test="${empty books}">
            <p class="muted">No books in this category yet.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="b" items="${books}">
                <a href="${pageContext.request.contextPath}/books/${b.bookId}" class="recent-book">
                    <div class="recent-book-info">
                        <h3>${b.title}</h3>
                        <p class="muted">${b.authorName}</p>
                        <span class="book-status">${b.status.display}</span>
                    </div>
                </a>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="../_layout_bottom.jsp" %>
