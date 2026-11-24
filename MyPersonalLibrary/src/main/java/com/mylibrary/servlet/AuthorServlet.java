package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import com.mylibrary.model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * /authors/*
 *   GET  /authors         → list
 *   GET  /authors/create  → form
 *   POST /authors/create  → store
 *   GET  /authors/{id}    → detail
 *   GET  /authors/{id}/edit → form
 *   POST /authors/{id}/edit → update
 *   POST /authors/{id}/delete → delete
 */
public class AuthorServlet extends HttpServlet {

    private final AuthorDAO authorDAO = new AuthorDAO();
    private final BookDAO   bookDAO   = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req, "/authors");
        try {
            if (path.isEmpty() || path.equals("/")) {
                req.setAttribute("authors", authorDAO.findAllByUser(userId));
                forward(req, res, "/WEB-INF/jsp/authors/index.jsp");
            } else if (path.equals("/create")) {
                forward(req, res, "/WEB-INF/jsp/authors/create.jsp");
            } else if (path.matches("/\\d+")) {
                int id = id(path);
                Author a = authorDAO.findById(id, userId);
                if (a == null) { res.sendError(403); return; }
                req.setAttribute("author", a);
                req.setAttribute("books",  bookDAO.findByUser(userId, null, null)
                        .stream().filter(b -> b.getAuthorId() == id).toList());
                forward(req, res, "/WEB-INF/jsp/authors/show.jsp");
            } else if (path.matches("/\\d+/edit")) {
                int id = id(path);
                Author a = authorDAO.findById(id, userId);
                if (a == null) { res.sendError(403); return; }
                req.setAttribute("author", a);
                forward(req, res, "/WEB-INF/jsp/authors/edit.jsp");
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req, "/authors");
        try {
            if (path.equals("/create")) {
                String name = req.getParameter("name");
                String bio  = req.getParameter("bio");
                if (name == null || name.isBlank()) {
                    req.setAttribute("error", "Name is required.");
                    forward(req, res, "/WEB-INF/jsp/authors/create.jsp"); return;
                }
                authorDAO.create(userId, name.trim(), bio);
                flash(req, "Author added successfully.");
                res.sendRedirect(req.getContextPath() + "/authors");
            } else if (path.matches("/\\d+/edit")) {
                int id = id(path);
                Author a = authorDAO.findById(id, userId);
                if (a == null) { res.sendError(403); return; }
                String name = req.getParameter("name");
                String bio  = req.getParameter("bio");
                if (name == null || name.isBlank()) {
                    req.setAttribute("error", "Name is required.");
                    req.setAttribute("author", a);
                    forward(req, res, "/WEB-INF/jsp/authors/edit.jsp"); return;
                }
                authorDAO.update(id, userId, name.trim(), bio);
                flash(req, "Author updated successfully.");
                res.sendRedirect(req.getContextPath() + "/authors/" + id);
            } else if (path.matches("/\\d+/delete")) {
                int id = id(path);
                boolean deleted = authorDAO.delete(id, userId);
                if (!deleted) {
                    flash(req, "Cannot delete this author — they still have books. " +
                               "Reassign or delete those books first.");
                } else {
                    flash(req, "Author deleted.");
                }
                res.sendRedirect(req.getContextPath() + "/authors");
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private int uid(HttpServletRequest r) { return (int) r.getSession().getAttribute("userId"); }
    private int id(String path) { return Integer.parseInt(path.replaceAll("[^0-9].*", "").substring(1)); }
    private String sub(HttpServletRequest r, String prefix) {
        String u = r.getRequestURI().substring(r.getContextPath().length() + prefix.length());
        return u.isEmpty() ? "/" : u;
    }
    private void forward(HttpServletRequest q, HttpServletResponse s, String jsp)
            throws ServletException, IOException {
        q.getRequestDispatcher(jsp).forward(q, s);
    }
    private void flash(HttpServletRequest r, String msg) {
        r.getSession().setAttribute("flash", msg);
    }
}
