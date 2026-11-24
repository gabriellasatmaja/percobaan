package com.mylibrary.dao;

import com.mylibrary.model.Book;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the books table.
 * All queries include WHERE user_id = ? — ownership-enforced at every level.
 * Composite FK (author_id, user_id) and single FK (category_id) are enforced by DB.
 * Application layer also validates ownership before every mutation.
 */
public class BookDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    /**
     * Returns all books for a user with optional search text and status filter.
     * Joins authors and categories for display names.
     */
    public List<Book> findByUser(int userId, String search, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT b.book_id, b.user_id, b.author_id, b.category_id, b.title, " +
            "b.status, b.cover_image, b.book_file_path, b.original_filename, " +
            "b.current_page, b.total_pages, b.summary, b.created_at, b.updated_at, " +
            "a.name AS author_name, c.name AS category_name " +
            "FROM books b " +
            "JOIN authors a ON b.author_id = a.author_id " +
            "LEFT JOIN categories c ON b.category_id = c.category_id " +
            "WHERE b.user_id = ?");

        if (search != null && !search.isBlank()) {
            sql.append(" AND (b.title LIKE ? OR a.name LIKE ? OR c.name LIKE ?)");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND b.status = ?");
        }
        sql.append(" ORDER BY b.created_at DESC");

        List<Book> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, userId);
            if (search != null && !search.isBlank()) {
                String like = "%" + search + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Book findById(int bookId, int userId) throws SQLException {
        String sql =
            "SELECT b.book_id, b.user_id, b.author_id, b.category_id, b.title, " +
            "b.status, b.cover_image, b.book_file_path, b.original_filename, " +
            "b.current_page, b.total_pages, b.summary, b.created_at, b.updated_at, " +
            "a.name AS author_name, c.name AS category_name " +
            "FROM books b " +
            "JOIN authors a ON b.author_id = a.author_id " +
            "LEFT JOIN categories c ON b.category_id = c.category_id " +
            "WHERE b.book_id = ? AND b.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Returns all favorited books for the user. */
    public List<Book> findFavoritesByUser(int userId) throws SQLException {
        String sql =
            "SELECT b.book_id, b.user_id, b.author_id, b.category_id, b.title, " +
            "b.status, b.cover_image, b.book_file_path, b.original_filename, " +
            "b.current_page, b.total_pages, b.summary, b.created_at, b.updated_at, " +
            "a.name AS author_name, c.name AS category_name " +
            "FROM books b " +
            "JOIN favorites f ON b.book_id = f.book_id AND b.user_id = f.user_id " +
            "JOIN authors a ON b.author_id = a.author_id " +
            "LEFT JOIN categories c ON b.category_id = c.category_id " +
            "WHERE b.user_id = ? " +
            "ORDER BY f.created_at DESC";
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Recent N books, used by dashboard. */
    public List<Book> findRecentByUser(int userId, int limit) throws SQLException {
        String sql =
            "SELECT b.book_id, b.user_id, b.author_id, b.category_id, b.title, " +
            "b.status, b.cover_image, b.book_file_path, b.original_filename, " +
            "b.current_page, b.total_pages, b.summary, b.created_at, b.updated_at, " +
            "a.name AS author_name, c.name AS category_name " +
            "FROM books b " +
            "JOIN authors a ON b.author_id = a.author_id " +
            "LEFT JOIN categories c ON b.category_id = c.category_id " +
            "WHERE b.user_id = ? ORDER BY b.created_at DESC LIMIT ?";
        List<Book> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countByStatus(int userId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE user_id = ? AND status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Average page progress (%) across books with both current_page and total_pages set. */
    public Integer avgProgress(int userId) throws SQLException {
        String sql =
            "SELECT ROUND(AVG(current_page * 100.0 / total_pages)) AS avg_progress " +
            "FROM books WHERE user_id = ? AND current_page IS NOT NULL " +
            "AND total_pages IS NOT NULL AND total_pages > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt("avg_progress");
                    return rs.wasNull() ? null : v;
                }
            }
        }
        return null;
    }

    public boolean isFavorite(int bookId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM favorites WHERE book_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public Book create(int userId, int authorId, Integer categoryId, String title,
                       String status, String coverImage, String bookFilePath,
                       String originalFilename, Integer currentPage, Integer totalPages,
                       String summary) throws SQLException {
        String sql =
            "INSERT INTO books (user_id, author_id, category_id, title, status, " +
            "cover_image, book_file_path, original_filename, current_page, total_pages, summary) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, authorId);
            setNullableInt(ps, 3, categoryId);
            ps.setString(4, title);
            ps.setString(5, status);
            ps.setString(6, coverImage);
            ps.setString(7, bookFilePath);
            ps.setString(8, originalFilename);
            setNullableInt(ps, 9, currentPage);
            setNullableInt(ps, 10, totalPages);
            ps.setString(11, summary);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(keys.getInt(1), userId);
            }
        }
        throw new SQLException("Book creation failed.");
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void update(int bookId, int userId, int authorId, Integer categoryId,
                       String title, String status, String coverImage,
                       String bookFilePath, String originalFilename,
                       Integer currentPage, Integer totalPages, String summary) throws SQLException {
        String sql =
            "UPDATE books SET author_id=?, category_id=?, title=?, status=?, " +
            "cover_image=?, book_file_path=?, original_filename=?, " +
            "current_page=?, total_pages=?, summary=? " +
            "WHERE book_id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            setNullableInt(ps, 2, categoryId);
            ps.setString(3, title);
            ps.setString(4, status);
            ps.setString(5, coverImage);
            ps.setString(6, bookFilePath);
            ps.setString(7, originalFilename);
            setNullableInt(ps, 8, currentPage);
            setNullableInt(ps, 9, totalPages);
            ps.setString(10, summary);
            ps.setInt(11, bookId);
            ps.setInt(12, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    public void delete(int bookId, int userId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // FAVORITES
    // ----------------------------------------------------------------

    public void addFavorite(int bookId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO favorites (user_id, book_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    public void removeFavorite(int bookId, int userId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE book_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setAuthorId(rs.getInt("author_id"));
        int catId = rs.getInt("category_id");
        b.setCategoryId(rs.wasNull() ? null : catId);
        b.setTitle(rs.getString("title"));
        b.setStatus(Book.Status.fromString(rs.getString("status")));
        b.setCoverImage(rs.getString("cover_image"));
        b.setBookFilePath(rs.getString("book_file_path"));
        b.setOriginalFilename(rs.getString("original_filename"));
        int cp = rs.getInt("current_page");
        b.setCurrentPage(rs.wasNull() ? null : cp);
        int tp = rs.getInt("total_pages");
        b.setTotalPages(rs.wasNull() ? null : tp);
        b.setSummary(rs.getString("summary"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) b.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) b.setUpdatedAt(ua.toLocalDateTime());
        b.setAuthorName(rs.getString("author_name"));
        try { b.setCategoryName(rs.getString("category_name")); }
        catch (SQLException ignored) {}
        return b;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else             ps.setInt(idx, val);
    }
}
