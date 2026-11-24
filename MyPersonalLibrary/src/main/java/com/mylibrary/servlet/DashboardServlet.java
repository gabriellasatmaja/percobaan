package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class DashboardServlet extends HttpServlet {

    private final BookDAO       bookDAO       = new BookDAO();
    private final ReadingLogDAO logDAO        = new ReadingLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");

        try {
            int totalBooks        = bookDAO.countByUser(userId);
            int currentlyReading  = bookDAO.countByStatus(userId, "Currently Reading");
            int finishedReading   = bookDAO.countByStatus(userId, "Finished Reading");
            int completionPct     = totalBooks > 0
                    ? (int) Math.round((finishedReading * 100.0) / totalBooks) : 0;
            Integer avgProgress   = bookDAO.avgProgress(userId);

            req.setAttribute("totalBooks",       totalBooks);
            req.setAttribute("currentlyReading", currentlyReading);
            req.setAttribute("finishedReading",  finishedReading);
            req.setAttribute("completionPct",    completionPct);
            req.setAttribute("avgProgress",      avgProgress);
            req.setAttribute("recentBooks",      bookDAO.findRecentByUser(userId, 4));
            req.setAttribute("favoriteBooks",    bookDAO.findFavoritesByUser(userId));
            req.setAttribute("recentLogs",       logDAO.findRecentByUser(userId, 5));

            req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, res);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
