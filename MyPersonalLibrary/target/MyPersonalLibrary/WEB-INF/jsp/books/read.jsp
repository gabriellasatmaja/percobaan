<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reading: ${book.title}" scope="request"/>
<c:set var="activeNav" value="books" scope="request"/>
<%@ include file="../_layout_top.jsp" %>

<div class="reader-header">
    <a href="${pageContext.request.contextPath}/books/${book.bookId}" class="secondary-btn">
        ← Back to Book Details
    </a>
    <div>
        <h1 style="margin:0;font-size:20px;font-weight:700;">${book.title}</h1>
        <p style="margin:4px 0 0;color:var(--library-muted);font-size:13px;">by ${book.authorName}</p>
    </div>
</div>

<iframe src="${pageContext.request.contextPath}/books/${book.bookId}/file"
        class="reader-frame"
        title="Reading: ${book.title}">
    <p class="muted">
        Your browser cannot display this file inline.
        <a href="${pageContext.request.contextPath}/books/${book.bookId}/file">Download it instead.</a>
    </p>
</iframe>

<%@ include file="../_layout_bottom.jsp" %>
