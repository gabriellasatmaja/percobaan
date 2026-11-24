package com.mylibrary.servlet;

import com.mylibrary.dao.*;
import com.mylibrary.model.*;
import com.mylibrary.util.FileUploadUtil;
import com.mylibrary.util.UploadConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Handles all /books/* routes.
 * URL patterns:
 *   GET  /books           → list
 *   GET  /books/create    → show create form
 *   POST /books/create    → store new book
 *   GET  /books/{id}      → detail
 *   GET  /books/{id}/edit → show edit form
 *   POST /books/{id}/edit → update book
 *   POST /books/{id}/delete  → delete book
 *   POST /books/{id}/favorite → toggle favorite
 *   GET  /books/{id}/read    → in-browser reader page
 *   GET  /books/{id}/file    → stream the book file
 */
public class BookServlet extends HttpServlet {

    private final BookDAO     bookDAO     = new BookDAO();
    private final AuthorDAO   authorDAO   = new AuthorDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BookNoteDAO noteDAO     = new BookNoteDAO();

    private String uploadDir;

    @Override
    public void init() {
        uploadDir = UploadConfig.resolve(getServletContext());
        log("[BookServlet] Using upload directory: " + uploadDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = currentUserId(req);
        String path = subPath(req);

        try {
            if (path == null || path.isEmpty() || path.equals("/")) {
                listBooks(req, res, userId);
            } else if (path.equals("/create")) {
                showCreateForm(req, res, userId);
            } else if (path.matches("/\\d+")) {
                showDetail(req, res, userId, bookId(path));
            } else if (path.matches("/\\d+/edit")) {
                showEditForm(req, res, userId, bookIdFromSegment(path, 1));
            } else if (path.matches("/\\d+/read")) {
                showReader(req, res, userId, bookIdFromSegment(path, 1));
            } else if (path.matches("/\\d+/file")) {
                serveFile(req, res, userId, bookIdFromSegment(path, 1));
            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int userId = currentUserId(req);
        String path = subPath(req);

        try {
            if (path.equals("/create")) {
                storeBook(req, res, userId);
            } else if (path.matches("/\\d+/edit")) {
                updateBook(req, res, userId, bookIdFromSegment(path, 1));
            } else if (path.matches("/\\d+/delete")) {
                deleteBook(req, res, userId, bookIdFromSegment(path, 1));
            } else if (path.matches("/\\d+/favorite")) {
                toggleFavorite(req, res, userId, bookIdFromSegment(path, 1));
            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // ----------------------------------------------------------------
    // List
    // ----------------------------------------------------------------

    private void listBooks(HttpServletRequest req, HttpServletResponse res, int userId)
            throws Exception {
        String search = req.getParameter("search");
        String status = req.getParameter("status");
        List<Book> books = bookDAO.findByUser(userId, search, status);
        req.setAttribute("books",  books);
        req.setAttribute("search", search);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/jsp/books/index.jsp").forward(req, res);
    }

    // ----------------------------------------------------------------
    // Create
    // ----------------------------------------------------------------

    private void showCreateForm(HttpServletRequest req, HttpServletResponse res, int userId)
            throws Exception {
        req.setAttribute("authors",    authorDAO.findAllByUser(userId));
        req.setAttribute("categories", categoryDAO.findAllByUser(userId));
        req.getRequestDispatcher("/WEB-INF/jsp/books/create.jsp").forward(req, res);
    }

    private void storeBook(HttpServletRequest req, HttpServletResponse res, int userId)
            throws Exception {
        String title      = req.getParameter("title");
        String authorIdStr = req.getParameter("author_id");
        String categoryIdStr = req.getParameter("category_id");
        String status     = req.getParameter("status");
        String currentPageStr = req.getParameter("current_page");
        String totalPagesStr  = req.getParameter("total_pages");
        String summary    = req.getParameter("summary");

        // Validation
        StringBuilder err = new StringBuilder();
        if (title == null || title.isBlank())    err.append("Title is required. ");
        if (authorIdStr == null || authorIdStr.isBlank()) err.append("Author is required. ");

        Integer authorId = null;
        if (authorIdStr != null && !authorIdStr.isBlank()) {
            try { authorId = Integer.parseInt(authorIdStr); }
            catch (NumberFormatException e) { err.append("Invalid author. "); }
        }

        // Verify ownership of author
        if (authorId != null && authorDAO.findById(authorId, userId) == null) {
            err.append("Selected author not found. ");
        }

        Integer categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isBlank()) {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
                // Ownership validation for category
                if (categoryDAO.findById(categoryId, userId) == null) {
                    err.append("Selected category not found. ");
                    categoryId = null;
                }
            } catch (NumberFormatException e) {
                err.append("Invalid category. ");
            }
        }

        Integer currentPage = parseNullableInt(currentPageStr);
        Integer totalPages  = parseNullableInt(totalPagesStr);
        if (currentPage != null && totalPages != null && currentPage > totalPages) {
            err.append("Current page cannot exceed total pages. ");
        }

        if (err.length() > 0) {
            req.setAttribute("error",      err.toString().trim());
            req.setAttribute("authors",    authorDAO.findAllByUser(userId));
            req.setAttribute("categories", categoryDAO.findAllByUser(userId));
            req.getRequestDispatcher("/WEB-INF/jsp/books/create.jsp").forward(req, res);
            return;
        }

        // File uploads
        String coverPath = null;
        String bookFilePath = null;
        String originalFilename = null;
        try {
            Part coverPart = req.getPart("cover_image");
            coverPath = FileUploadUtil.saveCover(coverPart, uploadDir);

            Part bookPart = req.getPart("book_file");
            bookFilePath = FileUploadUtil.saveBookFile(bookPart, uploadDir);
            if (bookPart != null && bookPart.getSize() > 0) {
                originalFilename = FileUploadUtil.getSubmittedFileName(bookPart);
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("authors",    authorDAO.findAllByUser(userId));
            req.setAttribute("categories", categoryDAO.findAllByUser(userId));
            req.getRequestDispatcher("/WEB-INF/jsp/books/create.jsp").forward(req, res);
            return;
        }

        bookDAO.create(userId, authorId, categoryId, title.trim(), status,
                       coverPath, bookFilePath, originalFilename,
                       currentPage, totalPages,
                       (summary != null && !summary.isBlank()) ? summary.trim() : null);

        req.getSession().setAttribute("flash", "Book added successfully.");
        res.sendRedirect(req.getContextPath() + "/books");
    }

    // ----------------------------------------------------------------
    // Detail
    // ----------------------------------------------------------------

    private void showDetail(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book book = bookDAO.findById(bookId, userId);
        if (book == null) { res.sendError(403); return; }
        req.setAttribute("book",       book);
        req.setAttribute("isFavorite", bookDAO.isFavorite(bookId, userId));
        req.setAttribute("notes",      noteDAO.findByBook(bookId, userId));
        req.getRequestDispatcher("/WEB-INF/jsp/books/show.jsp").forward(req, res);
    }

    // ----------------------------------------------------------------
    // Edit / Update
    // ----------------------------------------------------------------

    private void showEditForm(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book book = bookDAO.findById(bookId, userId);
        if (book == null) { res.sendError(403); return; }
        req.setAttribute("book",       book);
        req.setAttribute("authors",    authorDAO.findAllByUser(userId));
        req.setAttribute("categories", categoryDAO.findAllByUser(userId));
        req.getRequestDispatcher("/WEB-INF/jsp/books/edit.jsp").forward(req, res);
    }

    private void updateBook(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book existing = bookDAO.findById(bookId, userId);
        if (existing == null) { res.sendError(403); return; }

        String title         = req.getParameter("title");
        String authorIdStr   = req.getParameter("author_id");
        String categoryIdStr = req.getParameter("category_id");
        String status        = req.getParameter("status");
        String currentPageStr = req.getParameter("current_page");
        String totalPagesStr  = req.getParameter("total_pages");
        String summary       = req.getParameter("summary");
        boolean removeCover  = "true".equals(req.getParameter("remove_cover"));
        boolean removeFile   = "true".equals(req.getParameter("remove_file"));

        StringBuilder err = new StringBuilder();
        if (title == null || title.isBlank()) err.append("Title is required. ");

        Integer authorId = null;
        if (authorIdStr != null && !authorIdStr.isBlank()) {
            try { authorId = Integer.parseInt(authorIdStr); }
            catch (NumberFormatException e) { err.append("Invalid author. "); }
        } else {
            err.append("Author is required. ");
        }
        if (authorId != null && authorDAO.findById(authorId, userId) == null) {
            err.append("Selected author not found. ");
        }

        Integer categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isBlank()) {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
                if (categoryDAO.findById(categoryId, userId) == null) {
                    err.append("Selected category not found. ");
                    categoryId = null;
                }
            } catch (NumberFormatException e) {
                err.append("Invalid category. ");
            }
        }

        Integer currentPage = parseNullableInt(currentPageStr);
        Integer totalPages  = parseNullableInt(totalPagesStr);
        if (currentPage != null && totalPages != null && currentPage > totalPages) {
            err.append("Current page cannot exceed total pages. ");
        }

        if (err.length() > 0) {
            req.setAttribute("error",      err.toString().trim());
            req.setAttribute("book",       existing);
            req.setAttribute("authors",    authorDAO.findAllByUser(userId));
            req.setAttribute("categories", categoryDAO.findAllByUser(userId));
            req.getRequestDispatcher("/WEB-INF/jsp/books/edit.jsp").forward(req, res);
            return;
        }

        // Handle file uploads / removals
        String newCoverPath    = existing.getCoverImage();
        String newBookFilePath = existing.getBookFilePath();
        String newOrigFilename = existing.getOriginalFilename();

        if (removeCover) {
            FileUploadUtil.delete(existing.getCoverImage(), uploadDir);
            newCoverPath = null;
        }
        if (removeFile) {
            FileUploadUtil.delete(existing.getBookFilePath(), uploadDir);
            newBookFilePath = null;
            newOrigFilename = null;
        }

        try {
            Part coverPart = req.getPart("cover_image");
            if (coverPart != null && coverPart.getSize() > 0) {
                String saved = FileUploadUtil.saveCover(coverPart, uploadDir);
                if (saved != null) {
                    FileUploadUtil.delete(existing.getCoverImage(), uploadDir);
                    newCoverPath = saved;
                }
            }
            Part bookPart = req.getPart("book_file");
            if (bookPart != null && bookPart.getSize() > 0) {
                String saved = FileUploadUtil.saveBookFile(bookPart, uploadDir);
                if (saved != null) {
                    FileUploadUtil.delete(existing.getBookFilePath(), uploadDir);
                    newBookFilePath = saved;
                    newOrigFilename = FileUploadUtil.getSubmittedFileName(bookPart);
                }
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("book",       existing);
            req.setAttribute("authors",    authorDAO.findAllByUser(userId));
            req.setAttribute("categories", categoryDAO.findAllByUser(userId));
            req.getRequestDispatcher("/WEB-INF/jsp/books/edit.jsp").forward(req, res);
            return;
        }

        bookDAO.update(bookId, userId, authorId, categoryId, title.trim(), status,
                       newCoverPath, newBookFilePath, newOrigFilename,
                       currentPage, totalPages,
                       (summary != null && !summary.isBlank()) ? summary.trim() : null);

        req.getSession().setAttribute("flash", "Book updated successfully.");
        res.sendRedirect(req.getContextPath() + "/books/" + bookId);
    }

    // ----------------------------------------------------------------
    // Delete
    // ----------------------------------------------------------------

    private void deleteBook(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book book = bookDAO.findById(bookId, userId);
        if (book == null) { res.sendError(403); return; }
        FileUploadUtil.delete(book.getCoverImage(),   uploadDir);
        FileUploadUtil.delete(book.getBookFilePath(), uploadDir);
        bookDAO.delete(bookId, userId);
        req.getSession().setAttribute("flash", "Book deleted successfully.");
        res.sendRedirect(req.getContextPath() + "/books");
    }

    // ----------------------------------------------------------------
    // Favorite toggle
    // ----------------------------------------------------------------

    private void toggleFavorite(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book book = bookDAO.findById(bookId, userId);
        if (book == null) { res.sendError(403); return; }
        if (bookDAO.isFavorite(bookId, userId)) {
            bookDAO.removeFavorite(bookId, userId);
            req.getSession().setAttribute("flash", "Removed from your favorites.");
        } else {
            bookDAO.addFavorite(bookId, userId);
            req.getSession().setAttribute("flash", "Added to your favorites.");
        }
        String referer = req.getHeader("Referer");
        res.sendRedirect(referer != null ? referer : req.getContextPath() + "/books/" + bookId);
    }

    // ----------------------------------------------------------------
    // Reader
    // ----------------------------------------------------------------

    private void showReader(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book book = bookDAO.findById(bookId, userId);
        if (book == null) { res.sendError(403); return; }
        if (!book.hasFile()) {
            req.getSession().setAttribute("flash", "This book has no uploaded file yet.");
            res.sendRedirect(req.getContextPath() + "/books/" + bookId);
            return;
        }
        req.setAttribute("book", book);
        req.getRequestDispatcher("/WEB-INF/jsp/books/read.jsp").forward(req, res);
    }

    private void serveFile(HttpServletRequest req, HttpServletResponse res, int userId, int bookId)
            throws Exception {
        Book book = bookDAO.findById(bookId, userId);
        if (book == null || !book.hasFile()) { res.sendError(404); return; }

        Path filePath = Paths.get(uploadDir, book.getBookFilePath()
                .replace('/', java.io.File.separatorChar));
        if (!Files.exists(filePath)) { res.sendError(404); return; }

        String ext = book.getBookFilePath().toLowerCase();
        String mime = ext.endsWith(".pdf") ? "application/pdf" : "text/plain; charset=UTF-8";
        String filename = book.getOriginalFilename() != null
                ? book.getOriginalFilename()
                : "book." + (ext.endsWith(".pdf") ? "pdf" : "txt");

        res.setContentType(mime);
        res.setHeader("Content-Disposition", "inline; filename=\"" +
                filename.replace("\"", "\\\"") + "\"");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Cache-Control", "private, no-store");
        Files.copy(filePath, res.getOutputStream());
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private int currentUserId(HttpServletRequest req) {
        return (int) req.getSession().getAttribute("userId");
    }

    private String subPath(HttpServletRequest req) {
        String full    = req.getRequestURI();
        String context = req.getContextPath();
        String servlet = "/books";
        String path    = full.substring(context.length() + servlet.length());
        return path.isEmpty() ? "/" : path;
    }

    private int bookId(String path) {
        return Integer.parseInt(path.replaceAll("[^0-9]", ""));
    }

    private int bookIdFromSegment(String path, int segment) {
        // path like "/42/edit" — first segment after leading /
        String[] parts = path.split("/");
        return Integer.parseInt(parts[1]);
    }

    private Integer parseNullableInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
