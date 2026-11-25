<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Favorites" scope="request"/>
<c:set var="activeNav" value="favorites" scope="request"/>
<%@ include file="../_layout_top.jsp" %>

<section class="page-heading">
    <h1>My Favorites</h1>
    <p>Books you have starred in your personal collection.</p>
</section>

<c:choose>
    <c:when test="${empty books}">
        <div class="panel empty-books">
            <h2>No favorites yet</h2>
            <p class="muted">Star a book on the My Books page or its detail page to add it here.</p>
            <a href="${pageContext.request.contextPath}/books" class="primary-btn">Browse My Books</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="books-grid">
            <c:forEach var="book" items="${books}">
                <article class="book-card">
                    <c:choose>
                        <c:when test="${not empty book.coverImage}">
                            <div class="book-cover">
                                <img src="${pageContext.request.contextPath}/${book.coverImage}"
                                     alt="Cover of ${book.title}">
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="book-cover book-cover-placeholder">No Cover Available</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="book-info">
                        <h2 class="book-title">${book.title}</h2>
                        <p class="book-author">${book.authorName}</p>
                        <span class="book-status">${book.status.display}</span>
                        <div class="book-actions">
                            <a href="${pageContext.request.contextPath}/books/${book.bookId}/edit">
                                    Edit
                                </a>
                            <form action="${pageContext.request.contextPath}/books/${book.bookId}/favorite"
                                method="POST">
                                <button type="submit" title="Remove from favorites">
                                    ★ Remove
                                </button>
                            </form>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="../_layout_bottom.jsp" %>
