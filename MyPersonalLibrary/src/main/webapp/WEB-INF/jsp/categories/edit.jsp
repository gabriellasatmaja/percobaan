<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Category" scope="request"/>
<c:set var="activeNav" value="categories" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>Edit Category</h1></section>
<div class="panel">
    <form action="${pageContext.request.contextPath}/categories/${category.categoryId}/edit" method="POST">
        <div class="form-group">
            <label for="name">Category Name *</label>
            <input class="form-control" type="text" id="name" name="name"
                   value="${category.name}" required>
        </div>
        <div class="form-group">
            <label for="description">Description</label>
            <textarea class="form-control" id="description" name="description" rows="3">${category.description}</textarea>
        </div>
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/categories/${category.categoryId}" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Save Changes</button>
        </div>
    </form>
</div>
<%@ include file="../_layout_bottom.jsp" %>
