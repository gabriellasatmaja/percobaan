package com.mylibrary.servlet;

import com.mylibrary.dao.UserDAO;
import com.mylibrary.model.User;
import com.mylibrary.util.PasswordUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * /settings/*
 *   GET  /settings/profile   → profile form
 *   POST /settings/profile   → update name/email
 *   GET  /settings/security  → password form
 *   POST /settings/security  → update password
 *   POST /settings/delete    → delete account (transactional)
 */
public class SettingsServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = sub(req);
        if (path.equals("/profile") || path.equals("/")) {
            req.getRequestDispatcher("/WEB-INF/jsp/settings/profile.jsp").forward(req, res);
        } else if (path.equals("/security")) {
            req.getRequestDispatcher("/WEB-INF/jsp/settings/security.jsp").forward(req, res);
        } else {
            res.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req);
        try {
            if (path.equals("/profile")) {
                String name  = req.getParameter("name");
                String email = req.getParameter("email");
                if (name == null || name.isBlank()) {
                    req.setAttribute("error", "Name is required.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/profile.jsp").forward(req, res); return;
                }
                if (email == null || !email.contains("@")) {
                    req.setAttribute("error", "A valid email is required.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/profile.jsp").forward(req, res); return;
                }
                if (userDAO.emailExistsForOtherUser(email.trim().toLowerCase(), userId)) {
                    req.setAttribute("error", "This email is already in use by another account.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/profile.jsp").forward(req, res); return;
                }
                userDAO.updateProfile(userId, name.trim(), email.trim().toLowerCase());
                // Refresh session
                User updated = userDAO.findById(userId);
                req.getSession().setAttribute("userName",    updated.getName());
                req.getSession().setAttribute("userEmail",   updated.getEmail());
                req.getSession().setAttribute("currentUser", updated);
                flash(req, "Profile updated.");
                res.sendRedirect(req.getContextPath() + "/settings/profile");
            } else if (path.equals("/security")) {
                String current = req.getParameter("current_password");
                String newPw   = req.getParameter("password");
                String confirm = req.getParameter("password_confirmation");
                User user = userDAO.findById(userId);
                if (!PasswordUtil.verify(current, user.getPassword())) {
                    req.setAttribute("error", "Current password is incorrect.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/security.jsp").forward(req, res); return;
                }
                if (newPw == null || newPw.length() < 8) {
                    req.setAttribute("error", "New password must be at least 8 characters.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/security.jsp").forward(req, res); return;
                }
                if (!newPw.equals(confirm)) {
                    req.setAttribute("error", "Passwords do not match.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/security.jsp").forward(req, res); return;
                }
                userDAO.updatePassword(userId, PasswordUtil.hash(newPw));
                flash(req, "Password updated successfully.");
                res.sendRedirect(req.getContextPath() + "/settings/security");
            } else if (path.equals("/delete")) {
                String confirm = req.getParameter("confirm_delete");
                if (!"DELETE".equals(confirm)) {
                    req.setAttribute("error", "Please type DELETE to confirm account deletion.");
                    req.getRequestDispatcher("/WEB-INF/jsp/settings/profile.jsp").forward(req, res); return;
                }
                // Transactional deletion — see UserDAO.deleteAccount()
                userDAO.deleteAccount(userId);
                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                res.sendRedirect(req.getContextPath() + "/login");
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private int uid(HttpServletRequest r) { return (int) r.getSession().getAttribute("userId"); }
    private String sub(HttpServletRequest r) {
        String u = r.getRequestURI().substring(r.getContextPath().length() + "/settings".length());
        return u.isEmpty() ? "/" : u;
    }
    private void flash(HttpServletRequest r, String m) { r.getSession().setAttribute("flash", m); }
}
