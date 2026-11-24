<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Profile Settings" scope="request"/>
<c:set var="activeNav" value="settings" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>Profile Settings</h1><p>Update your name and email address.</p></section>
<div class="panel" style="max-width:520px;">
    <form action="${pageContext.request.contextPath}/settings/profile" method="POST">
        <div class="form-group">
            <label for="name">Full Name *</label>
            <input class="form-control" type="text" id="name" name="name"
                   value="${sessionScope.userName}" required>
        </div>
        <div class="form-group">
            <label for="email">Email Address *</label>
            <input class="form-control" type="email" id="email" name="email"
                   value="${sessionScope.userEmail}" required>
        </div>
        <div class="form-actions">
            <button type="submit" class="primary-btn">Save Changes</button>
        </div>
    </form>
</div>

<div class="panel" style="max-width:520px;margin-top:24px;border-color:#e5c5c5;">
    <h2 style="color:#c0392b;margin:0 0 12px;">Delete Account</h2>
    <p class="muted">This will permanently delete your account and all your books, notes, and reading data.</p>
    <form action="${pageContext.request.contextPath}/settings/delete" method="POST"
          onsubmit="return confirm('This will permanently delete your account and all data. This cannot be undone. Are you sure?')">
        <div class="form-group">
            <label for="confirm_delete">Type <strong>DELETE</strong> to confirm</label>
            <input class="form-control" type="text" id="confirm_delete" name="confirm_delete"
                   required placeholder="DELETE">
        </div>
        <button type="submit" class="btn-danger">Delete My Account</button>
    </form>
</div>
<%@ include file="../_layout_bottom.jsp" %>
