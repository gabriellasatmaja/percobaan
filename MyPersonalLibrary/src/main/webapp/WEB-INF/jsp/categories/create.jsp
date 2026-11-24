<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Add Category" scope="request"/>
<c:set var="activeNav" value="categories" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>Add Category</h1></section>
<div class="panel">
    <form action="${pageContext.request.contextPath}/categories/create" method="POST">
        <div class="form-group">
            <label for="name">Category Name *</label>
            <input class="form-control" type="text" id="name" name="name"
                   value="${param.name}" required placeholder="e.g. Fiction">
        </div>
        <div class="form-group">
            <label for="description">Description <span class="muted">(Optional)</span></label>
            <textarea class="form-control" id="description" name="description" rows="3"
                      placeholder="Short description...">${param.description}</textarea>
        </div>
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/categories" class="secondary-btn">Cancel</a>
            <button type="submit" class="primary-btn">Add Category</button>
        </div>
    </form>
</div>
<%@ include file="../_layout_bottom.jsp" %>
