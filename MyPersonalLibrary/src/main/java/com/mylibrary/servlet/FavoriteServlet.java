package com.mylibrary.servlet;

import com.mylibrary.dao.BookDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class FavoriteServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        try {
            req.setAttribute("books", bookDAO.findFavoritesByUser(userId));
            req.getRequestDispatcher("/WEB-INF/jsp/favorites/index.jsp").forward(req, res);
        } catch (Exception e) { throw new ServletException(e); }
    }
}
