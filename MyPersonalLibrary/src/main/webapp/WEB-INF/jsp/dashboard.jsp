<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
<%@ include file="_layout_top.jsp" %>

<section class="page-heading">
    <h1>Hi, ${sessionScope.userName}</h1>
    <p>Here's a quick look at your reading collection.</p>
</section>

<section class="dashboard-stats">
    <article class="panel stat-card">
        <p class="stat-label">Total Books</p>
        <h2 class="stat-number">${totalBooks}</h2>
        <p class="muted">Books in your collection</p>
    </article>
    <article class="panel stat-card">
        <p class="stat-label">Currently Reading</p>
        <h2 class="stat-number">${currentlyReading}</h2>
        <p class="muted">Books you're reading now</p>
    </article>
    <article class="panel stat-card">
        <p class="stat-label">Finished Reading</p>
        <h2 class="stat-number">${finishedReading}</h2>
        <p class="muted">Books you've completed</p>
    </article>
</section>

<section class="dashboard-columns">
    <div class="panel">
        <div class="section-heading">
            <h2>Recently Added</h2>
            <a href="${pageContext.request.contextPath}/books">View all</a>
        </div>
        <c:choose>
            <c:when test="${empty recentBooks}">
                <p class="muted empty-message">You haven't added any books yet.</p>
                <a href="${pageContext.request.contextPath}/books/create" class="primary-btn">Add Your First Book</a>
            </c:when>
            <c:otherwise>
                <c:forEach var="book" items="${recentBooks}">
                    <a href="${pageContext.request.contextPath}/books/${book.bookId}" class="recent-book">
                        <c:choose>
                            <c:when test="${not empty book.coverImage}">
                                <img src="${pageContext.request.contextPath}/${book.coverImage}"
                                     alt="Cover of ${book.title}" class="recent-book-cover">
                            </c:when>
                            <c:otherwise>
                                <div class="recent-book-cover recent-book-placeholder">No Cover</div>
                            </c:otherwise>
                        </c:choose>
                        <div class="recent-book-info">
                            <h3>${book.title}</h3>
                            <p>${book.authorName}</p>
                            <span class="book-status">${book.status.display}</span>
                        </div>
                    </a>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="panel">
        <div class="section-heading"><h2>Quick Actions</h2></div>
        <div class="quick-actions">
            <a href="${pageContext.request.contextPath}/books/create" class="quick-action">
                <span class="quick-action-symbol">＋</span>
                <span><strong>Add New Book</strong><small>Add a book to your collection</small></span>
            </a>
            <a href="${pageContext.request.contextPath}/books" class="quick-action">
                <span class="quick-action-symbol">▤</span>
                <span><strong>View All Books</strong><small>Browse your collection</small></span>
            </a>
            <a href="${pageContext.request.contextPath}/goals" class="quick-action">
                <span class="quick-action-symbol">🎯</span>
                <span><strong>Reading Goals</strong><small>Manage your reading targets</small></span>
            </a>
            <a href="${pageContext.request.contextPath}/books?status=Currently+Reading" class="quick-action">
                <span class="quick-action-symbol">📖</span>
                <span><strong>Currently Reading</strong><small>Books in progress</small></span>
            </a>
        </div>
    </div>
</section>

<section class="dashboard-columns">
    <div class="panel">
        <div class="section-heading"><h2>Reading Progress</h2></div>
        <p class="muted">Completed books in your collection</p>
        <div class="progress-track">
            <div class="progress-fill" style="width:${completionPct}%"></div>
        </div>
        <div class="progress-details">
            <strong>${completionPct}%</strong>
            <span class="muted">${finishedReading} of ${totalBooks} books finished</span>
        </div>
        <c:if test="${not empty avgProgress}">
            <p class="muted" style="margin-top:14px;font-size:13px;">
                Average page progress across tracked books: <strong>${avgProgress}%</strong>
            </p>
        </c:if>
    </div>

    <div class="panel">
        <div class="section-heading">
            <h2>My Favorites</h2>
            <a href="${pageContext.request.contextPath}/favorites">View all</a>
        </div>
        <c:choose>
            <c:when test="${empty favoriteBooks}">
                <p class="muted empty-message">Star a book to see it here.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="book" items="${favoriteBooks}" end="3">
                    <a href="${pageContext.request.contextPath}/books/${book.bookId}" class="favorite-book">
                        <c:choose>
                            <c:when test="${not empty book.coverImage}">
                                <img src="${pageContext.request.contextPath}/${book.coverImage}"
                                     alt="Cover" class="favorite-book-cover">
                            </c:when>
                            <c:otherwise>
                                <div class="favorite-book-cover recent-book-placeholder">No Cover</div>
                            </c:otherwise>
                        </c:choose>
                        <div>
                            <h3>${book.title}</h3>
                            <p class="muted">${book.authorName}</p>
                        </div>
                    </a>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%@ include file="_layout_bottom.jsp" %>
