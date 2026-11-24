package com.mylibrary.dao;

import com.mylibrary.model.Category;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the categories table.
 * All queries are scoped to the calling user — personal library.
 * Category deletion uses SET NULL (handled via ON DELETE SET NULL FK).
 */
public class CategoryDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    public List<Category> findAllByUser(int userId) throws SQLException {
        String sql = "SELECT category_id, user_id, name, description, created_at " +
                     "FROM categories WHERE user_id = ? ORDER BY name ASC";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Category findById(int categoryId, int userId) throws SQLException {
        String sql = "SELECT category_id, user_id, name, description, created_at " +
                     "FROM categories WHERE category_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean nameExistsForUser(String name, int userId) throws SQLException {
        String sql = "SELECT 1 FROM categories WHERE user_id = ? AND name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean nameExistsForOtherCategory(String name, int userId, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM categories WHERE user_id = ? AND name = ? AND category_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setInt(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public Category create(int userId, String name, String description) throws SQLException {
        String sql = "INSERT INTO categories (user_id, name, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return findById(keys.getInt(1), userId);
            }
        }
        throw new SQLException("Category creation failed.");
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void update(int categoryId, int userId, String name, String description) throws SQLException {
        String sql = "UPDATE categories SET name = ?, description = ? " +
                     "WHERE category_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, categoryId);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    /**
     * Deletes a category.
     * Per design decision: ON DELETE SET NULL on books.category_id means
     * the DB will automatically NULL out books that reference this category.
     * We do it explicitly here as an extra safety step (matches the
     * application-layer validation requirement).
     */
    public void delete(int categoryId, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Explicitly NULL out books referencing this category for this user
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE books SET category_id = NULL " +
                    "WHERE category_id = ? AND user_id = ?")) {
                ps.setInt(1, categoryId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Now delete the category (ON DELETE SET NULL would handle it
            // automatically but we already nulled above, so this is clean)
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM categories WHERE category_id = ? AND user_id = ?")) {
                ps.setInt(1, categoryId);
                ps.setInt(2, userId);
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

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        return c;
    }
}
