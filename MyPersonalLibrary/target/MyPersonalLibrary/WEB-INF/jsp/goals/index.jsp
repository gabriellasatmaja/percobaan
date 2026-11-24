<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reading Goals" scope="request"/>
<c:set var="activeNav" value="goals" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading">
    <h1>Reading Goals</h1>
    <p>Set and track your personal reading targets.</p>
</section>
<div style="display:flex;justify-content:flex-end;margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/goals/create" class="primary-btn">+ New Goal</a>
</div>
<c:choose>
    <c:when test="${empty goals}">
        <div class="panel empty-books">
            <h2>No goals yet</h2>
            <p class="muted">Create your first reading goal to stay motivated.</p>
            <a href="${pageContext.request.contextPath}/goals/create" class="primary-btn">Create Goal</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="goals-list">
            <c:forEach var="g" items="${goals}">
                <div class="panel goal-card ${g.completed ? 'goal-completed' : ''}">
                    <div class="goal-header">
                        <h3 class="goal-title">${g.completed ? '✅ ' : ''}${g.title}</h3>
                        <span class="goal-type-badge">${g.goalType == 'BOOKS_COUNT' ? 'Count Goal' : 'Finish Book'}</span>
                    </div>
                    <c:if test="${g.goalType.dbValue == 'books_count'}">
                        <p class="muted">Target: <strong>${g.targetBooks} books</strong></p>
                    </c:if>
                    <c:if test="${g.goalType.dbValue == 'finish_book'}">
                        <p class="muted">Book: <a href="${pageContext.request.contextPath}/books/${g.bookId}"><strong>${g.bookTitle}</strong></a></p>
                    </c:if>
                    <c:if test="${not empty g.deadline}">
                        <p class="muted">Deadline: <strong>${g.deadline}</strong></p>
                    </c:if>
                    <div class="goal-actions">
                        <form action="${pageContext.request.contextPath}/goals/${g.goalId}/toggle" method="POST">
                            <button type="submit" class="secondary-btn">
                                ${g.completed ? 'Mark Incomplete' : 'Mark Complete'}
                            </button>
                        </form>
                        <a href="${pageContext.request.contextPath}/goals/${g.goalId}/edit" class="secondary-btn">Edit</a>
                        <form action="${pageContext.request.contextPath}/goals/${g.goalId}/delete"
                              method="POST" style="display:inline;"
                              onsubmit="return confirm('Delete this goal?')">
                            <button type="submit" class="btn-danger">Delete</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>
<%@ include file="../_layout_bottom.jsp" %>
