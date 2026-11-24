package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import com.mylibrary.model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;

/**
 * /logs/*
 *   GET  /logs?bookId={id}       → list logs for a book (redirects to book detail)
 *   POST /logs/create?bookId={id} → add log
 *   GET  /logs/{id}/edit          → edit form
 *   POST /logs/{id}/edit          → update
 *   POST /logs/{id}/delete        → delete
 */
public class ReadingLogServlet extends HttpServlet {

    private final ReadingLogDAO logDAO  = new ReadingLogDAO();
    private final BookDAO       bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req);
        try {
            if (path.matches("/\\d+/edit")) {
                int logId = id(path);
                ReadingLog log = logDAO.findById(logId, userId);
                if (log == null) { res.sendError(403); return; }
                req.setAttribute("log", log);
                req.getRequestDispatcher("/WEB-INF/jsp/logs/edit.jsp").forward(req, res);
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req);
        try {
            if (path.equals("/create")) {
                int bookId = Integer.parseInt(req.getParameter("bookId"));
                if (bookDAO.findById(bookId, userId) == null) { res.sendError(403); return; }
                String pagesStr  = req.getParameter("pages_read");
                String dateStr   = req.getParameter("log_date");
                String notes     = req.getParameter("notes");
                int pagesRead;
                try {
                    pagesRead = Integer.parseInt(pagesStr.trim());
                    if (pagesRead <= 0) throw new NumberFormatException();
                } catch (Exception e) {
                    flash(req, "Pages read must be a number greater than zero.");
                    res.sendRedirect(req.getContextPath() + "/books/" + bookId); return;
                }
                Date logDate;
                try { logDate = Date.valueOf(dateStr); }
                catch (Exception e) {
                    flash(req, "Invalid date format.");
                    res.sendRedirect(req.getContextPath() + "/books/" + bookId); return;
                }
                logDAO.create(bookId, userId, pagesRead, logDate, notes);
                flash(req, "Reading session logged.");
                res.sendRedirect(req.getContextPath() + "/books/" + bookId);
            } else if (path.matches("/\\d+/edit")) {
                int logId = id(path);
                ReadingLog log = logDAO.findById(logId, userId);
                if (log == null) { res.sendError(403); return; }
                String pagesStr = req.getParameter("pages_read");
                String dateStr  = req.getParameter("log_date");
                String notes    = req.getParameter("notes");
                int pagesRead;
                try {
                    pagesRead = Integer.parseInt(pagesStr.trim());
                    if (pagesRead <= 0) throw new NumberFormatException();
                } catch (Exception e) {
                    req.setAttribute("error", "Pages read must be greater than zero.");
                    req.setAttribute("log", log);
                    req.getRequestDispatcher("/WEB-INF/jsp/logs/edit.jsp").forward(req, res); return;
                }
                Date logDate;
                try { logDate = Date.valueOf(dateStr); }
                catch (Exception e) {
                    req.setAttribute("error", "Invalid date.");
                    req.setAttribute("log", log);
                    req.getRequestDispatcher("/WEB-INF/jsp/logs/edit.jsp").forward(req, res); return;
                }
                logDAO.update(logId, userId, pagesRead, logDate, notes);
                flash(req, "Reading log updated.");
                res.sendRedirect(req.getContextPath() + "/books/" + log.getBookId());
            } else if (path.matches("/\\d+/delete")) {
                int logId = id(path);
                ReadingLog log = logDAO.findById(logId, userId);
                if (log == null) { res.sendError(403); return; }
                int bookId = log.getBookId();
                logDAO.delete(logId, userId);
                flash(req, "Reading log deleted.");
                res.sendRedirect(req.getContextPath() + "/books/" + bookId);
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private int uid(HttpServletRequest r) { return (int) r.getSession().getAttribute("userId"); }
    private int id(String p) { return Integer.parseInt(p.split("/")[1]); }
    private String sub(HttpServletRequest r) {
        String u = r.getRequestURI().substring(r.getContextPath().length() + "/logs".length());
        return u.isEmpty() ? "/" : u;
    }
    private void flash(HttpServletRequest r, String m) { r.getSession().setAttribute("flash", m); }
}
