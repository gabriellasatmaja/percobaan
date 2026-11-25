package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import com.mylibrary.model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * /categories/*
 *   GET  /categories         → list
 *   GET  /categories/create  → form
 *   POST /categories/create  → store
 *   GET  /categories/{id}    → detail
 *   GET  /categories/{id}/edit → form
 *   POST /categories/{id}/edit → update
 *   POST /categories/{id}/delete → delete (explicit NULL update + DB cascade)
 */
public class CategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BookDAO     bookDAO     = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req, "/categories");
        try {
            if (path.isEmpty() || path.equals("/")) {
                req.setAttribute("categories", categoryDAO.findAllByUser(userId));
                forward(req, res, "/WEB-INF/jsp/categories/index.jsp");
            } else if (path.equals("/create")) {
                forward(req, res, "/WEB-INF/jsp/categories/create.jsp");
            } else if (path.matches("/\\d+")) {
                int id = id(path);
                Category c = categoryDAO.findById(id, userId);
                if (c == null) { res.sendError(403); return; }
                req.setAttribute("category", c);
                req.setAttribute("books", bookDAO.findByUser(userId, null, null)
                        .stream().filter(b -> b.getCategoryId() != null && b.getCategoryId() == id).toList());
                forward(req, res, "/WEB-INF/jsp/categories/show.jsp");
            } else if (path.matches("/\\d+/edit")) {
                int id = id(path);
                Category c = categoryDAO.findById(id, userId);
                if (c == null) { res.sendError(403); return; }
                req.setAttribute("category", c);
                forward(req, res, "/WEB-INF/jsp/categories/edit.jsp");
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = uid(req);
        String path = sub(req, "/categories");
        try {
            if (path.equals("/create")) {
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                if (name == null || name.isBlank()) {
                    req.setAttribute("error", "Name is required.");
                    forward(req, res, "/WEB-INF/jsp/categories/create.jsp"); return;
                }
                if (categoryDAO.nameExistsForUser(name.trim(), userId)) {
                    req.setAttribute("error", "You already have a category with this name.");
                    forward(req, res, "/WEB-INF/jsp/categories/create.jsp"); return;
                }
                categoryDAO.create(userId, name.trim(), desc);
                flash(req, "Category added successfully.");
                res.sendRedirect(req.getContextPath() + "/categories");
            } else if (path.matches("/\\d+/edit")) {
                int id = id(path);
                Category c = categoryDAO.findById(id, userId);
                if (c == null) { res.sendError(403); return; }
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                if (name == null || name.isBlank()) {
                    req.setAttribute("error", "Name is required.");
                    req.setAttribute("category", c);
                    forward(req, res, "/WEB-INF/jsp/categories/edit.jsp"); return;
                }
                if (categoryDAO.nameExistsForOtherCategory(name.trim(), userId, id)) {
                    req.setAttribute("error", "You already have another category with this name.");
                    req.setAttribute("category", c);
                    forward(req, res, "/WEB-INF/jsp/categories/edit.jsp"); return;
                }
                categoryDAO.update(id, userId, name.trim(), desc);
                flash(req, "Category updated successfully.");
                res.sendRedirect(req.getContextPath() + "/categories/" + id);
            } else if (path.matches("/\\d+/delete")) {
                int id = id(path);
                // CategoryDAO.delete() NULLs books first, then deletes — explicit + DB cascade
                categoryDAO.delete(id, userId);
                flash(req, "Category deleted. Books in this category have been un-categorized.");
                res.sendRedirect(req.getContextPath() + "/categories");
            } else {
                res.sendError(404);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private int uid(HttpServletRequest r) { return (int) r.getSession().getAttribute("userId"); }
    private int id(String path) { return Integer.parseInt(path.split("/")[1]); }
    private String sub(HttpServletRequest r, String prefix) {
        String u = r.getRequestURI().substring(r.getContextPath().length() + prefix.length());
        return u.isEmpty() ? "/" : u;
    }
    private void forward(HttpServletRequest q, HttpServletResponse s, String jsp)
            throws ServletException, IOException { q.getRequestDispatcher(jsp).forward(q, s); }
    private void flash(HttpServletRequest r, String msg) {
        r.getSession().setAttribute("flash", msg);
    }
}
