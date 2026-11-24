package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import com.mylibrary.model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * /notes/*
 *   POST /notes/create?bookId={id}  → add note
 *   GET  /notes/{id}/edit           → edit form
 *   POST /notes/{id}/edit           → update
 *   POST /notes/{id}/delete         → delete
 */
public class NoteServlet extends HttpServlet {

    private final BookNoteDAO noteDAO = new BookNoteDAO();
    private final BookDAO     bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req);
        try {
            if (path.matches("/\\d+/edit")) {
                int noteId = id(path);
                BookNote note = noteDAO.findById(noteId, userId);
                if (note == null) { res.sendError(403); return; }
                req.setAttribute("note", note);
                req.getRequestDispatcher("/WEB-INF/jsp/notes/edit.jsp").forward(req, res);
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
                String bookIdStr = req.getParameter("bookId");
                String content   = req.getParameter("content");
                int bookId = Integer.parseInt(bookIdStr);
                if (bookDAO.findById(bookId, userId) == null) { res.sendError(403); return; }
                if (content == null || content.isBlank()) {
                    flash(req, "Note content cannot be empty.");
                    res.sendRedirect(req.getContextPath() + "/books/" + bookId); return;
                }
                noteDAO.create(bookId, userId, content.trim());
                flash(req, "Note added.");
                res.sendRedirect(req.getContextPath() + "/books/" + bookId);
            } else if (path.matches("/\\d+/edit")) {
                int noteId  = id(path);
                BookNote note = noteDAO.findById(noteId, userId);
                if (note == null) { res.sendError(403); return; }
                String content = req.getParameter("content");
                if (content == null || content.isBlank()) {
                    req.setAttribute("error", "Note content cannot be empty.");
                    req.setAttribute("note", note);
                    req.getRequestDispatcher("/WEB-INF/jsp/notes/edit.jsp").forward(req, res); return;
                }
                noteDAO.update(noteId, userId, content.trim());
                flash(req, "Note updated.");
                res.sendRedirect(req.getContextPath() + "/books/" + note.getBookId());
            } else if (path.matches("/\\d+/delete")) {
                int noteId = id(path);
                BookNote note = noteDAO.findById(noteId, userId);
                if (note == null) { res.sendError(403); return; }
                int bookId = note.getBookId();
                noteDAO.delete(noteId, userId);
                flash(req, "Note deleted.");
                res.sendRedirect(req.getContextPath() + "/books/" + bookId);
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private int uid(HttpServletRequest r) { return (int) r.getSession().getAttribute("userId"); }
    private int id(String p) { return Integer.parseInt(p.split("/")[1]); }
    private String sub(HttpServletRequest r) {
        String u = r.getRequestURI().substring(r.getContextPath().length() + "/notes".length());
        return u.isEmpty() ? "/" : u;
    }
    private void flash(HttpServletRequest r, String m) { r.getSession().setAttribute("flash", m); }
}
