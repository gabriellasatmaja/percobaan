package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import com.mylibrary.model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * /goals/*
 *   GET  /goals           → list
 *   GET  /goals/create    → form
 *   POST /goals/create    → store
 *   GET  /goals/{id}/edit → form
 *   POST /goals/{id}/edit → update
 *   POST /goals/{id}/toggle → toggle complete
 *   POST /goals/{id}/delete → delete
 */
public class GoalServlet extends HttpServlet {

    private final ReadingGoalDAO goalDAO = new ReadingGoalDAO();
    private final BookDAO        bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req);
        try {
            if (path.isEmpty() || path.equals("/")) {
                req.setAttribute("goals", goalDAO.findByUser(userId));
                forward(req, res, "/WEB-INF/jsp/goals/index.jsp");
            } else if (path.equals("/create")) {
                req.setAttribute("books", bookDAO.findByUser(userId, null, null));
                forward(req, res, "/WEB-INF/jsp/goals/create.jsp");
            } else if (path.matches("/\\d+/edit")) {
                int goalId = id(path);
                ReadingGoal goal = goalDAO.findById(goalId, userId);
                if (goal == null) { res.sendError(403); return; }
                req.setAttribute("goal",  goal);
                req.setAttribute("books", bookDAO.findByUser(userId, null, null));
                forward(req, res, "/WEB-INF/jsp/goals/edit.jsp");
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
                String title    = req.getParameter("title");
                String type     = req.getParameter("goal_type");
                String tgtStr   = req.getParameter("target_books");
                String bkStr    = req.getParameter("book_id");
                String deadStr  = req.getParameter("deadline");

                String err = validateGoal(title, type, tgtStr, bkStr, userId);
                if (err != null) {
                    req.setAttribute("error", err);
                    req.setAttribute("books", bookDAO.findByUser(userId, null, null));
                    forward(req, res, "/WEB-INF/jsp/goals/create.jsp"); return;
                }
                goalDAO.create(userId, title.trim(), type,
                        "books_count".equals(type) ? Integer.parseInt(tgtStr.trim()) : null,
                        "finish_book".equals(type) ? Integer.parseInt(bkStr.trim()) : null,
                        (deadStr != null && !deadStr.isBlank()) ? Date.valueOf(deadStr) : null);
                flash(req, "Goal created.");
                res.sendRedirect(req.getContextPath() + "/goals");
            } else if (path.matches("/\\d+/edit")) {
                int goalId = id(path);
                ReadingGoal goal = goalDAO.findById(goalId, userId);
                if (goal == null) { res.sendError(403); return; }
                String title   = req.getParameter("title");
                String type    = req.getParameter("goal_type");
                String tgtStr  = req.getParameter("target_books");
                String bkStr   = req.getParameter("book_id");
                String deadStr = req.getParameter("deadline");
                boolean done   = "true".equals(req.getParameter("is_completed"));
                String err = validateGoal(title, type, tgtStr, bkStr, userId);
                if (err != null) {
                    req.setAttribute("error", err);
                    req.setAttribute("goal",  goal);
                    req.setAttribute("books", bookDAO.findByUser(userId, null, null));
                    forward(req, res, "/WEB-INF/jsp/goals/edit.jsp"); return;
                }
                goalDAO.update(goalId, userId, title.trim(), type,
                        "books_count".equals(type) ? Integer.parseInt(tgtStr.trim()) : null,
                        "finish_book".equals(type) ? Integer.parseInt(bkStr.trim()) : null,
                        (deadStr != null && !deadStr.isBlank()) ? Date.valueOf(deadStr) : null,
                        done);
                flash(req, "Goal updated.");
                res.sendRedirect(req.getContextPath() + "/goals");
            } else if (path.matches("/\\d+/toggle")) {
                int goalId = id(path);
                ReadingGoal goal = goalDAO.findById(goalId, userId);
                if (goal == null) { res.sendError(403); return; }
                goalDAO.toggleComplete(goalId, userId, !goal.isCompleted());
                flash(req, goal.isCompleted() ? "Goal marked incomplete." : "Goal marked complete!");
                res.sendRedirect(req.getContextPath() + "/goals");
            } else if (path.matches("/\\d+/delete")) {
                int goalId = id(path);
                ReadingGoal goal = goalDAO.findById(goalId, userId);
                if (goal == null) { res.sendError(403); return; }
                goalDAO.delete(goalId, userId);
                flash(req, "Goal deleted.");
                res.sendRedirect(req.getContextPath() + "/goals");
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private String validateGoal(String title, String type, String tgtStr, String bkStr, int userId)
            throws Exception {
        if (title == null || title.isBlank()) return "Title is required.";
        if (!"books_count".equals(type) && !"finish_book".equals(type))
            return "Please select a goal type.";
        if ("books_count".equals(type)) {
            if (tgtStr == null || tgtStr.isBlank()) return "Target number of books is required.";
            try {
                int t = Integer.parseInt(tgtStr.trim());
                if (t <= 0) return "Target must be greater than zero.";
            } catch (NumberFormatException e) { return "Target must be a number."; }
        }
        if ("finish_book".equals(type)) {
            if (bkStr == null || bkStr.isBlank()) return "Please select a book.";
            int bk;
            try { bk = Integer.parseInt(bkStr.trim()); }
            catch (NumberFormatException e) { return "Invalid book selection."; }
            if (bookDAO.findById(bk, userId) == null) return "Selected book not found.";
        }
        return null;
    }

    private int uid(HttpServletRequest r) { return (int) r.getSession().getAttribute("userId"); }
    private int id(String p) { return Integer.parseInt(p.split("/")[1]); }
    private String sub(HttpServletRequest r) {
        String u = r.getRequestURI().substring(r.getContextPath().length() + "/goals".length());
        return u.isEmpty() ? "/" : u;
    }
    private void forward(HttpServletRequest q, HttpServletResponse s, String jsp)
            throws ServletException, IOException { q.getRequestDispatcher(jsp).forward(q, s); }
    private void flash(HttpServletRequest r, String m) { r.getSession().setAttribute("flash", m); }
}
