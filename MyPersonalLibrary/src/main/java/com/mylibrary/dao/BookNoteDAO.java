package com.mylibrary.dao;

import com.mylibrary.model.BookNote;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the book_notes table.
 * Composite FK (book_id, user_id) on the table enforces that only the
 * book owner can add notes. All queries also include user_id in WHERE.
 */
public class BookNoteDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    public List<BookNote> findByBook(int bookId, int userId) throws SQLException {
        String sql =
            "SELECT n.note_id, n.book_id, n.user_id, n.content, n.created_at, n.updated_at, " +
            "b.title AS book_title " +
            "FROM book_notes n JOIN books b ON n.book_id = b.book_id " +
            "WHERE n.book_id = ? AND n.user_id = ? ORDER BY n.created_at DESC";
        List<BookNote> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public BookNote findById(int noteId, int userId) throws SQLException {
        String sql =
            "SELECT n.note_id, n.book_id, n.user_id, n.content, n.created_at, n.updated_at, " +
            "b.title AS book_title " +
            "FROM book_notes n JOIN books b ON n.book_id = b.book_id " +
            "WHERE n.note_id = ? AND n.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public void create(int bookId, int userId, String content) throws SQLException {
        String sql = "INSERT INTO book_notes (book_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void update(int noteId, int userId, String content) throws SQLException {
        String sql = "UPDATE book_notes SET content = ? WHERE note_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, noteId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    public void delete(int noteId, int userId) throws SQLException {
        String sql = "DELETE FROM book_notes WHERE note_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private BookNote mapRow(ResultSet rs) throws SQLException {
        BookNote n = new BookNote();
        n.setNoteId(rs.getInt("note_id"));
        n.setBookId(rs.getInt("book_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setContent(rs.getString("content"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) n.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) n.setUpdatedAt(ua.toLocalDateTime());
        try { n.setBookTitle(rs.getString("book_title")); }
        catch (SQLException ignored) {}
        return n;
    }
}
