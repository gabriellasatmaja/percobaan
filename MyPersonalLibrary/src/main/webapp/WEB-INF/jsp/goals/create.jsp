<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="New Reading Goal" scope="request"/>
<c:set var="activeNav" value="goals" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>New Reading Goal</h1></section>
<div class="panel">
    <form action="${pageContext.request.contextPath}/goals/create" method="POST" id="goalForm">
        <div class="form-group">
            <label for="title">Goal Title *</label>
            <input class="form-control" type="text" id="title" name="title"
                   value="${param.title}" required placeholder="e.g. Read 12 books in 2025">
        </div>
        <div class="form-group">
            <label for="goal_type">Goal Type *</label>
            <select class="form-control" id="goal_type" name="goal_type" required onchange="toggleGoalFields()">
                <option value="">— Select type —</option>
                <option value="books_count" ${param.goal_type == 'books_count' ? 'selected' : ''}>Read a number of books</option>
                <option value="finish_book" ${param.goal_type == 'finish_book' ? 'selected' : ''}>Finish a specific book</option>
            </select>
        </div>
        <div class="form-group" id="field_target" style="display:none;">
            <label for="target_books">Target Number of Books *</label>
            <input class="form-control" type="number" id="target_books" name="target_books"
                   min="1" value="${param.target_books}" placeholder="e.g. 12">
        </div>
        <div class="form-group" id="field_book" style="display:none;">
            <label for="book_id">Select Book *</label>
            <select class="form-control" id="book_id" name="book_id">
                <option value="">— Select a book —</option>
                <c:forEach var="b" items="${books}">
                    <option value="${b.bookId}" ${param.book_id == b.bookId ? 'selected' : ''}>${b.title}</option>
                </c:forEach>
            </select>
        </div>
        <div class="form-group">
            <label for="deadline">Deadline <span class="muted">(Optional)</span></label>
            <input class="form-control" type="date" id="deadline" name="deadline" value="${param.deadline}">
        </div>
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/goals" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Create Goal</button>
        </div>
    </form>
</div>
<script>
function toggleGoalFields() {
    var type = document.getElementById('goal_type').value;
    document.getElementById('field_target').style.display = type === 'books_count' ? '' : 'none';
    document.getElementById('field_book').style.display   = type === 'finish_book'  ? '' : 'none';
}
toggleGoalFields();
</script>
<%@ include file="../_layout_bottom.jsp" %>
