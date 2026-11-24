<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Add a New Book" scope="request"/>
<c:set var="activeNav" value="addbook" scope="request"/>
<%@ include file="../_layout_top.jsp" %>

<section class="page-heading">
    <h1>Add a New Book</h1>
    <p>Add a book to your personal library.</p>
</section>

<div class="panel">
    <form action="${pageContext.request.contextPath}/books/create" method="POST"
          enctype="multipart/form-data">

        <div class="form-group">
            <label for="title">Book Title *</label>
            <input class="form-control" type="text" id="title" name="title"
                   value="${param.title}" placeholder="Enter the book title" required>
        </div>

        <div class="form-group">
            <label for="author_id">Author *</label>
            <select class="form-control" id="author_id" name="author_id" required>
                <option value="">— Select an author —</option>
                <c:forEach var="author" items="${authors}">
                    <option value="${author.authorId}"
                            ${param.author_id == author.authorId ? 'selected' : ''}>
                        ${author.name}
                    </option>
                </c:forEach>
            </select>
            <p class="muted" style="font-size:12px;">
                <a href="${pageContext.request.contextPath}/authors/create">+ Add a new author</a>
            </p>
        </div>

        <div class="form-group">
            <label for="category_id">Category <span class="muted">(Optional)</span></label>
            <select class="form-control" id="category_id" name="category_id">
                <option value="">— No category —</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}"
                            ${param.category_id == cat.categoryId ? 'selected' : ''}>
                        ${cat.name}
                    </option>
                </c:forEach>
            </select>
            <p class="muted" style="font-size:12px;">
                <a href="${pageContext.request.contextPath}/categories/create">+ Add a new category</a>
            </p>
        </div>

        <div class="form-group">
            <label for="status">Reading Status *</label>
            <select class="form-control" id="status" name="status" required>
                <option value="Want to Read"      ${param.status == 'Want to Read'      ? 'selected' : ''}>Want to Read</option>
                <option value="Currently Reading" ${param.status == 'Currently Reading' ? 'selected' : ''}>Currently Reading</option>
                <option value="Finished Reading"  ${param.status == 'Finished Reading'  ? 'selected' : ''}>Finished Reading</option>
            </select>
        </div>

        <div class="form-group">
            <label for="cover_image">Book Cover <span class="muted">(Optional)</span></label>
            <input class="form-control" type="file" id="cover_image" name="cover_image"
                   accept="image/jpeg,image/jpg,image/png,image/webp">
            <p class="muted">JPEG, PNG, or WebP, maximum 2 MB.</p>
        </div>

        <div class="form-group">
            <label for="book_file">Book File <span class="muted">(Optional)</span></label>
            <input class="form-control" type="file" id="book_file" name="book_file"
                   accept=".pdf,.txt">
            <p class="muted">PDF or TXT, maximum 20 MB.</p>
        </div>

        <div class="form-row">
            <div class="form-group">
                <label for="current_page">Current Page <span class="muted">(Optional)</span></label>
                <input class="form-control" type="number" id="current_page" name="current_page"
                       min="0" value="${param.current_page}" placeholder="0">
            </div>
            <div class="form-group">
                <label for="total_pages">Total Pages <span class="muted">(Optional)</span></label>
                <input class="form-control" type="number" id="total_pages" name="total_pages"
                       min="1" value="${param.total_pages}" placeholder="e.g. 320">
            </div>
        </div>

        <div class="form-group">
            <label for="summary">Short Summary <span class="muted">(Optional)</span></label>
            <textarea class="form-control" id="summary" name="summary" rows="5"
                      placeholder="Write a short summary...">${param.summary}</textarea>
        </div>

        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/books" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Add Book</button>
        </div>
    </form>
</div>

<%@ include file="../_layout_bottom.jsp" %>
