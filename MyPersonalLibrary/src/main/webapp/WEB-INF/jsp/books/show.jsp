<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${book.title}" scope="request"/>
<c:set var="activeNav" value="books" scope="request"/>
<%@ include file="../_layout_top.jsp" %>

<section class="page-heading">
    <h1>Book Details</h1>
    <p>View the details of your book.</p>
</section>

<div class="panel book-detail">
    <c:choose>
        <c:when test="${not empty book.coverImage}">
            <img src="${pageContext.request.contextPath}/${book.coverImage}"
                 alt="Cover of ${book.title}" class="book-detail-cover">
        </c:when>
        <c:otherwise>
            <div class="book-detail-cover book-cover-placeholder">No Cover Available</div>
        </c:otherwise>
    </c:choose>

    <div class="book-detail-info">
        <h2>${book.title}</h2>
        <p class="muted">by ${book.authorName}</p>

        <p><strong>Category:</strong>
            <c:choose>
                <c:when test="${not empty book.categoryName}">${book.categoryName}</c:when>
                <c:otherwise>Not specified</c:otherwise>
            </c:choose>
        </p>
        <p><strong>Status:</strong> <span class="book-status">${book.status.display}</span></p>
        <p><strong>Pages:</strong>
            <c:choose>
                <c:when test="${book.currentPage != null and book.totalPages != null}">
                    ${book.currentPage} / ${book.totalPages} (${book.progressPercent}%)
                </c:when>
                <c:when test="${book.currentPage != null}">
                    Page ${book.currentPage} (total unknown)
                </c:when>
                <c:otherwise>Not tracked</c:otherwise>
            </c:choose>
        </p>

        <c:if test="${not empty book.summary}">
            <div class="book-summary">
                <h3>Summary</h3>
                <p>${book.summary}</p>
            </div>
        </c:if>

        <div class="book-file-section">
            <h3>Book File</h3>
            <c:choose>
                <c:when test="${book.hasFile()}">
                    <p><span class="file-info">${book.originalFilename}</span></p>
                    <a href="${pageContext.request.contextPath}/books/${book.bookId}/read"
                       class="primary-btn" style="margin-top:10px;display:inline-flex;">
                        📖 Read Book
                    </a>
                </c:when>
                <c:otherwise>
                    <p class="muted">No book file uploaded yet.</p>
                    <a href="${pageContext.request.contextPath}/books/${book.bookId}/edit"
                       class="secondary-btn" style="margin-top:8px;display:inline-flex;">
                        Upload a File
                    </a>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="form-actions" style="justify-content:flex-start;flex-wrap:wrap;gap:10px;">
            <a href="${pageContext.request.contextPath}/books" class="secondary-btn">← My Books</a>
            <form action="${pageContext.request.contextPath}/books/${book.bookId}/favorite" method="POST">
                <button type="submit" class="secondary-btn">
                    ${isFavorite ? '★ Remove from Favorites' : '☆ Add to Favorites'}
                </button>
            </form>
            <a href="${pageContext.request.contextPath}/books/${book.bookId}/edit" class="primary-btn">Edit Book</a>
            <form action="${pageContext.request.contextPath}/books/${book.bookId}/delete" method="POST"
                  onsubmit="return confirm('Delete this book? Notes, logs, and goals will also be removed.')">
                <button type="submit" class="btn-danger">Delete Book</button>
            </form>
        </div>
    </div>
</div>

<%-- ============ Notes section ============ --%>
<section class="panel" style="margin-top:24px;">
    <div class="section-heading">
        <h2>Private Notes</h2>
    </div>

    <form action="${pageContext.request.contextPath}/notes/create?bookId=${book.bookId}"
          method="POST" class="note-form">
        <div class="form-group">
            <textarea class="form-control" name="content" rows="3"
                      placeholder="Write a private note about this book..." required></textarea>
        </div>
        <button type="submit" class="primary-btn">Add Note</button>
    </form>

    <c:choose>
        <c:when test="${empty notes}">
            <p class="muted empty-message" style="margin-top:16px;">No notes yet.</p>
        </c:when>
        <c:otherwise>
            <div class="notes-list" style="margin-top:16px;">
                <c:forEach var="note" items="${notes}">
                    <div class="note-item">
                        <p class="note-content">${note.content}</p>
                        <div class="note-meta">
                            <span class="muted" style="font-size:12px;">${note.createdAt}</span>
                            <div class="note-actions">
                                <a href="${pageContext.request.contextPath}/notes/${note.noteId}/edit">Edit</a>
                                <form action="${pageContext.request.contextPath}/notes/${note.noteId}/delete"
                                      method="POST" style="display:inline;"
                                      onsubmit="return confirm('Delete this note?')">
                                    <button type="submit" class="btn-link-danger">Delete</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%-- ============ Log reading session ============ --%>
<section class="panel" style="margin-top:24px;">
    <div class="section-heading">
        <h2>Log a Reading Session</h2>
    </div>
    <form action="${pageContext.request.contextPath}/logs/create?bookId=${book.bookId}"
          method="POST" class="log-form">
        <div class="form-row">
            <div class="form-group">
                <label for="log_date">Date</label>
                <input class="form-control" type="date" id="log_date" name="log_date"
                       required value="${java.time.LocalDate.now()}">
            </div>
            <div class="form-group">
                <label for="pages_read">Pages Read</label>
                <input class="form-control" type="number" id="pages_read" name="pages_read"
                       min="1" required placeholder="e.g. 30">
            </div>
        </div>
        <div class="form-group">
            <label for="log_notes">Session Notes <span class="muted">(Optional)</span></label>
            <input class="form-control" type="text" id="log_notes" name="notes"
                   placeholder="Quick note about this session">
        </div>
        <button type="submit" class="primary-btn">Log Session</button>
    </form>
</section>

<%@ include file="../_layout_bottom.jsp" %>
