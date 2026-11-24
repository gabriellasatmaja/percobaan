package com.mylibrary.dao;

import com.mylibrary.model.ReadingGoal;
import com.mylibrary.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReadingGoalDAO {

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    public List<ReadingGoal> findByUser(int userId) throws SQLException {
        String sql =
            "SELECT g.goal_id, g.user_id, g.title, g.goal_type, g.target_books, " +
            "g.book_id, g.deadline, g.is_completed, g.created_at, g.updated_at, " +
            "b.title AS book_title " +
            "FROM reading_goals g " +
            "LEFT JOIN books b ON g.book_id = b.book_id AND g.user_id = b.user_id " +
            "WHERE g.user_id = ? ORDER BY g.is_completed ASC, g.deadline ASC, g.created_at DESC";
        List<ReadingGoal> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public ReadingGoal findById(int goalId, int userId) throws SQLException {
        String sql =
            "SELECT g.goal_id, g.user_id, g.title, g.goal_type, g.target_books, " +
            "g.book_id, g.deadline, g.is_completed, g.created_at, g.updated_at, " +
            "b.title AS book_title " +
            "FROM reading_goals g " +
            "LEFT JOIN books b ON g.book_id = b.book_id AND g.user_id = b.user_id " +
            "WHERE g.goal_id = ? AND g.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goalId);
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

    public void create(int userId, String title, String goalType,
                       Integer targetBooks, Integer bookId, Date deadline) throws SQLException {
        String sql =
            "INSERT INTO reading_goals (user_id, title, goal_type, target_books, book_id, deadline) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, goalType);
            setNullableInt(ps, 4, targetBooks);
            setNullableInt(ps, 5, bookId);
            if (deadline != null) ps.setDate(6, deadline);
            else                  ps.setNull(6, Types.DATE);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public void update(int goalId, int userId, String title, String goalType,
                       Integer targetBooks, Integer bookId, Date deadline,
                       boolean isCompleted) throws SQLException {
        String sql =
            "UPDATE reading_goals SET title=?, goal_type=?, target_books=?, " +
            "book_id=?, deadline=?, is_completed=? " +
            "WHERE goal_id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, goalType);
            setNullableInt(ps, 3, targetBooks);
            setNullableInt(ps, 4, bookId);
            if (deadline != null) ps.setDate(5, deadline);
            else                  ps.setNull(5, Types.DATE);
            ps.setBoolean(6, isCompleted);
            ps.setInt(7, goalId);
            ps.setInt(8, userId);
            ps.executeUpdate();
        }
    }

    public void toggleComplete(int goalId, int userId, boolean completed) throws SQLException {
        String sql = "UPDATE reading_goals SET is_completed = ? WHERE goal_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, completed);
            ps.setInt(2, goalId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    public void delete(int goalId, int userId) throws SQLException {
        String sql = "DELETE FROM reading_goals WHERE goal_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goalId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private ReadingGoal mapRow(ResultSet rs) throws SQLException {
        ReadingGoal g = new ReadingGoal();
        g.setGoalId(rs.getInt("goal_id"));
        g.setUserId(rs.getInt("user_id"));
        g.setTitle(rs.getString("title"));
        g.setGoalType(ReadingGoal.GoalType.fromString(rs.getString("goal_type")));
        int tb = rs.getInt("target_books");
        g.setTargetBooks(rs.wasNull() ? null : tb);
        int bk = rs.getInt("book_id");
        g.setBookId(rs.wasNull() ? null : bk);
        Date dl = rs.getDate("deadline");
        if (dl != null) g.setDeadline(dl.toLocalDate());
        g.setCompleted(rs.getBoolean("is_completed"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) g.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) g.setUpdatedAt(ua.toLocalDateTime());
        try { g.setBookTitle(rs.getString("book_title")); }
        catch (SQLException ignored) {}
        return g;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else             ps.setInt(idx, val);
    }
}
