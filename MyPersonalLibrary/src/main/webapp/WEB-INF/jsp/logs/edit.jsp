<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Reading Log" scope="request"/>
<c:set var="activeNav" value="books" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading">
    <h1>Edit Reading Log</h1>
    <p>For: <em>${log.bookTitle}</em></p>
</section>
<div class="panel">
    <form action="${pageContext.request.contextPath}/logs/${log.logId}/edit" method="POST">
        <div class="form-row">
            <div class="form-group">
                <label for="log_date">Date *</label>
                <input class="form-control" type="date" id="log_date" name="log_date"
                       value="${log.logDate}" required>
            </div>
            <div class="form-group">
                <label for="pages_read">Pages Read *</label>
                <input class="form-control" type="number" id="pages_read" name="pages_read"
                       min="1" value="${log.pagesRead}" required>
            </div>
        </div>
        <div class="form-group">
            <label for="notes">Session Notes <span class="muted">(Optional)</span></label>
            <input class="form-control" type="text" id="notes" name="notes"
                   value="${log.notes}">
        </div>
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/books/${log.bookId}" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Save Changes</button>
        </div>
    </form>
</div>
<%@ include file="../_layout_bottom.jsp" %>
