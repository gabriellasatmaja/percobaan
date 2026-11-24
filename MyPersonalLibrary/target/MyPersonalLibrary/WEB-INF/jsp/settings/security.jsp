<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Security Settings" scope="request"/>
<c:set var="activeNav" value="settings" scope="request"/>
<%@ include file="../_layout_top.jsp" %>
<section class="page-heading"><h1>Security Settings</h1><p>Change your password.</p></section>
<div class="panel" style="max-width:520px;">
    <form action="${pageContext.request.contextPath}/settings/security" method="POST">
        <div class="form-group">
            <label for="current_password">Current Password *</label>
            <input class="form-control" type="password" id="current_password" name="current_password" required>
        </div>
        <div class="form-group">
            <label for="password">New Password * <span class="muted">(min 8 characters)</span></label>
            <input class="form-control" type="password" id="password" name="password"
                   required minlength="8">
        </div>
        <div class="form-group">
            <label for="password_confirmation">Confirm New Password *</label>
            <input class="form-control" type="password" id="password_confirmation"
                   name="password_confirmation" required minlength="8">
        </div>
        <div class="form-actions">
            <button type="submit" class="primary-btn">Update Password</button>
        </div>
    </form>
</div>
<%@ include file="../_layout_bottom.jsp" %>
