package com.mylibrary.servlet;

import com.mylibrary.dao.UserDAO;
import com.mylibrary.model.User;
import com.mylibrary.util.PasswordUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            res.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String name     = req.getParameter("name");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        String confirm  = req.getParameter("password_confirmation");

        // Validation
        if (name == null || name.isBlank()) {
            req.setAttribute("error", "Name is required.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
            return;
        }
        if (email == null || !email.contains("@")) {
            req.setAttribute("error", "A valid email is required.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
            return;
        }
        if (password == null || password.length() < 8) {
            req.setAttribute("error", "Password must be at least 8 characters.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
            return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("error", "Passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                req.setAttribute("error", "An account with this email already exists.");
                req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
                return;
            }
            User user = userDAO.create(name.trim(), email.trim().toLowerCase(),
                                       PasswordUtil.hash(password));
            // Auto-login after registration
            HttpSession session = req.getSession(true);
            session.setAttribute("userId",    user.getUserId());
            session.setAttribute("userName",  user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("currentUser", user);
            res.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (Exception e) {
            req.setAttribute("error", "Registration failed. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, res);
        }
    }
}
