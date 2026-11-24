package com.mylibrary.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class providing JDBC connections from db.properties.
 * Each call to getConnection() opens a new connection.
 * Always close connections in a finally block or try-with-resources.
 */
public class DBConnection {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new ExceptionInInitializerError(
                    "db.properties not found on the classpath. " +
                    "Copy WEB-INF/db.properties to src/main/resources/.");
            }
            props.load(in);
            Class.forName(props.getProperty("db.driver",
                    "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {}

    /**
     * Returns a new JDBC connection. Caller is responsible for closing it.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password"));
    }

    /**
     * Silently close a connection; logs errors to stderr.
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Failed to close connection: " + e.getMessage());
            }
        }
    }

    /**
     * Roll back and close in one call; used in catch blocks.
     */
    public static void rollbackAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("[DBConnection] Rollback failed: " + ex.getMessage());
            }
            close(conn);
        }
    }
}
