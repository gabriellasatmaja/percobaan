package com.mylibrary.dao;

import com.mylibrary.model.User;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object for the users table.
 * All PreparedStatements prevent SQL injection.
 */
public class UserDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password, created_at, updated_at " +
                     "FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email, password, created_at, updated_at " +
                     "FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExistsForOtherUser(String email, int userId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? AND user_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    /** Insert a new user. The password must already be BCrypt-hashed. */
    public User create(String name, String email, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1));
                }
            }
        }
        throw new SQLException("User creation failed — no key returned.");
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void updateProfile(int userId, String name, String email) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void updatePassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE — transactional sequence required (RESTRICT on authors)
    // ----------------------------------------------------------------

    /**
     * Deletes a user and ALL their data in the correct order to satisfy
     * FK RESTRICT on books.author_id:
     *
     *   1. NULL-out books.category_id for this user (category FK is SET NULL
     *      but we do it manually here to stay explicit in the transaction).
     *   2. Delete all this user's books (cascades notes, logs, favorites, goals).
     *   3. Delete authors (now no books reference them).
     *   4. Delete categories (now no books reference them).
     *   5. Delete the user (cascades any remaining session data).
     *
     * All steps run in a single transaction.
     */
    public void deleteAccount(int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Step 1: NULL category on user's books
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE books SET category_id = NULL WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Step 2: Delete books (cascades book_notes, reading_logs,
            //         reading_goals, favorites)
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM books WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Step 3: Delete authors (no books reference them now)
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM authors WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Step 4: Delete categories
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM categories WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Step 5: Delete user
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            DBConnection.rollbackAndClose(conn);
            conn = null;
            throw e;
        } finally {
            DBConnection.close(conn);
        }
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) u.setUpdatedAt(ua.toLocalDateTime());
        return u;
    }
}
