<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Author" scope="request"/>
<c:set var="activeNav" value="authors" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>Edit Author</h1></section>
<div class="panel">
    <form action="${pageContext.request.contextPath}/authors/${author.authorId}/edit" method="POST">
        <div class="form-group">
            <label for="name">Author Name *</label>
            <input class="form-control" type="text" id="name" name="name"
                   value="${author.name}" required>
        </div>
        <div class="form-group">
            <label for="bio">Biography</label>
            <textarea class="form-control" id="bio" name="bio" rows="4">${author.bio}</textarea>
        </div>
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/authors/${author.authorId}" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Save Changes</button>
        </div>
    </form>
</div>
<%@ include file="../_layout_bottom.jsp" %>
