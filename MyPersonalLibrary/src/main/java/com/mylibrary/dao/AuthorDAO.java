package com.mylibrary.dao;

import com.mylibrary.model.Author;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the authors table.
 * All queries are scoped to the calling user (user_id = ?) — personal library.
 */
public class AuthorDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    public List<Author> findAllByUser(int userId) throws SQLException {
        String sql = "SELECT author_id, user_id, name, bio, created_at " +
                     "FROM authors WHERE user_id = ? ORDER BY name ASC";
        List<Author> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Author findById(int authorId, int userId) throws SQLException {
        String sql = "SELECT author_id, user_id, name, bio, created_at " +
                     "FROM authors WHERE author_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Count of books belonging to this author (for the author detail page). */
    public int countBooks(int authorId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE author_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public Author create(int userId, String name, String bio) throws SQLException {
        String sql = "INSERT INTO authors (user_id, name, bio) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, bio);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(keys.getInt(1), userId);
            }
        }
        throw new SQLException("Author creation failed.");
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void update(int authorId, int userId, String name, String bio) throws SQLException {
        String sql = "UPDATE authors SET name = ?, bio = ? WHERE author_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, bio);
            ps.setInt(3, authorId);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    /**
     * Returns false if books still reference this author (RESTRICT violation).
     * Returns true on successful deletion.
     */
    public boolean delete(int authorId, int userId) throws SQLException {
        // Check if any books still reference this author
        if (countBooks(authorId, userId) > 0) {
            return false;
        }
        String sql = "DELETE FROM authors WHERE author_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        return true;
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private Author mapRow(ResultSet rs) throws SQLException {
        Author a = new Author();
        a.setAuthorId(rs.getInt("author_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setName(rs.getString("name"));
        a.setBio(rs.getString("bio"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        return a;
    }
}
