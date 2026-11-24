<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Book" scope="request"/>
<c:set var="activeNav" value="books" scope="request"/>
<%@ include file="../_layout_top.jsp" %>

<section class="page-heading">
    <h1>Edit Book</h1>
    <p>Update the details of <em>${book.title}</em>.</p>
</section>

<div class="panel">
    <form action="${pageContext.request.contextPath}/books/${book.bookId}/edit" method="POST"
          enctype="multipart/form-data">

        <div class="form-group">
            <label for="title">Book Title *</label>
            <input class="form-control" type="text" id="title" name="title"
                   value="${book.title}" required>
        </div>

        <div class="form-group">
            <label for="author_id">Author *</label>
            <select class="form-control" id="author_id" name="author_id" required>
                <option value="">— Select an author —</option>
                <c:forEach var="a" items="${authors}">
                    <option value="${a.authorId}" ${book.authorId == a.authorId ? 'selected' : ''}>
                        ${a.name}
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="form-group">
            <label for="category_id">Category <span class="muted">(Optional)</span></label>
            <select class="form-control" id="category_id" name="category_id">
                <option value="">— No category —</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}"
                            ${book.categoryId == cat.categoryId ? 'selected' : ''}>
                        ${cat.name}
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="form-group">
            <label for="status">Reading Status *</label>
            <select class="form-control" id="status" name="status" required>
                <option value="Want to Read"      ${book.status.display == 'Want to Read'      ? 'selected' : ''}>Want to Read</option>
                <option value="Currently Reading" ${book.status.display == 'Currently Reading' ? 'selected' : ''}>Currently Reading</option>
                <option value="Finished Reading"  ${book.status.display == 'Finished Reading'  ? 'selected' : ''}>Finished Reading</option>
            </select>
        </div>

        <%-- Cover image --%>
        <div class="form-group">
            <label>Book Cover</label>
            <c:if test="${not empty book.coverImage}">
                <div class="current-cover">
                    <p class="muted">Current cover:</p>
                    <img src="${pageContext.request.contextPath}/${book.coverImage}"
                         alt="Cover" class="edit-cover-preview">
                    <label class="checkbox-label" style="margin-top:8px;">
                        <input type="checkbox" name="remove_cover" value="true"> Remove current cover
                    </label>
                </div>
            </c:if>
            <input class="form-control" type="file" id="cover_image" name="cover_image"
                   accept="image/jpeg,image/jpg,image/png,image/webp">
            <p class="muted">JPEG, PNG, or WebP, max 2 MB. Leave empty to keep current cover.</p>
        </div>

        <%-- Book file --%>
        <div class="form-group">
            <label>Book File</label>
            <c:if test="${not empty book.originalFilename}">
                <p class="muted">Current file: <span class="file-info">${book.originalFilename}</span></p>
                <label class="checkbox-label">
                    <input type="checkbox" name="remove_file" value="true"> Remove current file
                </label>
            </c:if>
            <input class="form-control" type="file" id="book_file" name="book_file"
                   accept=".pdf,.txt">
            <p class="muted">PDF or TXT, max 20 MB. Leave empty to keep current file.</p>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label for="current_page">Current Page</label>
                <input class="form-control" type="number" id="current_page" name="current_page"
                       min="0" value="${book.currentPage}">
            </div>
            <div class="form-group">
                <label for="total_pages">Total Pages</label>
                <input class="form-control" type="number" id="total_pages" name="total_pages"
                       min="1" value="${book.totalPages}">
            </div>
        </div>

        <div class="form-group">
            <label for="summary">Short Summary</label>
            <textarea class="form-control" id="summary" name="summary" rows="5">${book.summary}</textarea>
        </div>

        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/books/${book.bookId}" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Save Changes</button>
        </div>
    </form>
</div>

<%@ include file="../_layout_bottom.jsp" %>
