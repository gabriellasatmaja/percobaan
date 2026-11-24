<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Authors" scope="request"/>
<c:set var="activeNav" value="authors" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading">
    <h1>Authors</h1>
    <p>Manage the authors in your personal library.</p>
</section>
<div style="display:flex;justify-content:flex-end;margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/authors/create" class="primary-btn">+ Add Author</a>
</div>
<c:choose>
    <c:when test="${empty authors}">
        <div class="panel empty-books">
            <h2>No authors yet</h2>
            <p class="muted">Add an author to link to your books.</p>
            <a href="${pageContext.request.contextPath}/authors/create" class="primary-btn">Add First Author</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="panel">
            <table class="data-table">
                <thead><tr><th>Name</th><th>Bio</th><th>Actions</th></tr></thead>
                <tbody>
                <c:forEach var="a" items="${authors}">
                    <tr>
                        <td><a href="${pageContext.request.contextPath}/authors/${a.authorId}"><strong>${a.name}</strong></a></td>
                        <td class="muted">${not empty a.bio ? a.bio : '—'}</td>
                        <td class="table-actions">
                            <a href="${pageContext.request.contextPath}/authors/${a.authorId}/edit">Edit</a>
                            <form action="${pageContext.request.contextPath}/authors/${a.authorId}/delete"
                                  method="POST" style="display:inline;"
                                  onsubmit="return confirm('Delete author \'${a.name}\'? This will fail if they still have books.')">
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
