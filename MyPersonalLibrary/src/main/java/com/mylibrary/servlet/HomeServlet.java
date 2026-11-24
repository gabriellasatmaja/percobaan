package com.mylibrary.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Redirects / to dashboard if logged in, otherwise to welcome page. */
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            res.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            req.getRequestDispatcher("/WEB-INF/jsp/welcome.jsp").forward(req, res);
        }
    }
}
