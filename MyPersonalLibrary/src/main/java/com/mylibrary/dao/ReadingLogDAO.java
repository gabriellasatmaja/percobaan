package com.mylibrary.dao;

import com.mylibrary.model.ReadingLog;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReadingLogDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    public List<ReadingLog> findByBook(int bookId, int userId) throws SQLException {
        String sql =
            "SELECT l.log_id, l.book_id, l.user_id, l.pages_read, l.log_date, " +
            "l.notes, l.created_at, b.title AS book_title " +
            "FROM reading_logs l JOIN books b ON l.book_id = b.book_id " +
            "WHERE l.book_id = ? AND l.user_id = ? ORDER BY l.log_date DESC";
        List<ReadingLog> list = new ArrayList<>();
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

    public List<ReadingLog> findRecentByUser(int userId, int limit) throws SQLException {
        String sql =
            "SELECT l.log_id, l.book_id, l.user_id, l.pages_read, l.log_date, " +
            "l.notes, l.created_at, b.title AS book_title " +
            "FROM reading_logs l JOIN books b ON l.book_id = b.book_id " +
            "WHERE l.user_id = ? ORDER BY l.log_date DESC, l.created_at DESC LIMIT ?";
        List<ReadingLog> list = new ArrayList<>();
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

    public ReadingLog findById(int logId, int userId) throws SQLException {
        String sql =
            "SELECT l.log_id, l.book_id, l.user_id, l.pages_read, l.log_date, " +
            "l.notes, l.created_at, b.title AS book_title " +
            "FROM reading_logs l JOIN books b ON l.book_id = b.book_id " +
            "WHERE l.log_id = ? AND l.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, logId);
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

    public void create(int bookId, int userId, int pagesRead, Date logDate, String notes)
            throws SQLException {
        String sql = "INSERT INTO reading_logs (book_id, user_id, pages_read, log_date, notes) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.setInt(3, pagesRead);
            ps.setDate(4, logDate);
            ps.setString(5, notes);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void update(int logId, int userId, int pagesRead, Date logDate, String notes)
            throws SQLException {
        String sql = "UPDATE reading_logs SET pages_read = ?, log_date = ?, notes = ? " +
                     "WHERE log_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pagesRead);
            ps.setDate(2, logDate);
            ps.setString(3, notes);
            ps.setInt(4, logId);
            ps.setInt(5, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    public void delete(int logId, int userId) throws SQLException {
        String sql = "DELETE FROM reading_logs WHERE log_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, logId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private ReadingLog mapRow(ResultSet rs) throws SQLException {
        ReadingLog l = new ReadingLog();
        l.setLogId(rs.getInt("log_id"));
        l.setBookId(rs.getInt("book_id"));
        l.setUserId(rs.getInt("user_id"));
        l.setPagesRead(rs.getInt("pages_read"));
        Date d = rs.getDate("log_date");
        if (d != null) l.setLogDate(d.toLocalDate());
        l.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) l.setCreatedAt(ca.toLocalDateTime());
        try { l.setBookTitle(rs.getString("book_title")); }
        catch (SQLException ignored) {}
        return l;
    }
}
