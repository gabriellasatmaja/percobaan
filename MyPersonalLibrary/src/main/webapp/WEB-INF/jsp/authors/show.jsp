<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${author.name}" scope="request"/>
<c:set var="activeNav" value="authors" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading">
    <h1>${author.name}</h1>
    <p>${not empty author.bio ? author.bio : 'No biography available.'}</p>
</section>
<div class="form-actions" style="justify-content:flex-start;margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/authors" class="secondary-btn">← Authors</a>
    <a href="${pageContext.request.contextPath}/authors/${author.authorId}/edit" class="primary-btn">Edit</a>
</div>
<div class="panel">
    <div class="section-heading"><h2>Books by ${author.name}</h2></div>
    <c:choose>
        <c:when test="${empty books}">
            <p class="muted">No books yet for this author.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="b" items="${books}">
                <a href="${pageContext.request.contextPath}/books/${b.bookId}" class="recent-book">
                    <div class="recent-book-info">
                        <h3>${b.title}</h3>
                        <span class="book-status">${b.status.display}</span>
                    </div>
                </a>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="../_layout_bottom.jsp" %>
