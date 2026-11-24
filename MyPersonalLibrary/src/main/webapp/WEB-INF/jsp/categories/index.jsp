<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Categories" scope="request"/>
<c:set var="activeNav" value="categories" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>Categories</h1><p>Manage your book categories.</p></section>
<div style="display:flex;justify-content:flex-end;margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/categories/create" class="primary-btn">+ Add Category</a>
</div>
<c:choose>
    <c:when test="${empty categories}">
        <div class="panel empty-books">
            <h2>No categories yet</h2>
            <a href="${pageContext.request.contextPath}/categories/create" class="primary-btn">Add First Category</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="panel">
            <table class="data-table">
                <thead><tr><th>Name</th><th>Description</th><th>Actions</th></tr></thead>
                <tbody>
                <c:forEach var="c" items="${categories}">
                    <tr>
                        <td><a href="${pageContext.request.contextPath}/categories/${c.categoryId}"><strong>${c.name}</strong></a></td>
                        <td class="muted">${not empty c.description ? c.description : '—'}</td>
                        <td class="table-actions">
                            <a href="${pageContext.request.contextPath}/categories/${c.categoryId}/edit">Edit</a>
                            <form action="${pageContext.request.contextPath}/categories/${c.categoryId}/delete"
                                  method="POST" style="display:inline;"
                                  onsubmit="return confirm('Delete category \'${c.name}\'? Books in this category will be un-categorized.')">
                                <button type="submit" class="btn-link-danger">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>
<%@ include file="../_layout_bottom.jsp" %>
