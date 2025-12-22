package com.kna.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseManager - Singleton class for managing SQLite database connections
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_PATH = "kna_database.db";

    private DatabaseManager() {
        initializeConnection();
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize database connection
     */
    private void initializeConnection() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            connection.setAutoCommit(true);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            
            System.out.println("Database connection established successfully!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    /**
     * Initialize database by checking if tables exist and creating them if needed
     */
    public void initializeDatabase() {
        try {
            if (!tableExists("users")) {
                System.out.println("Tables not found. Initializing database from schema.sql...");
                createSchemaFromResource();
                System.out.println("Database initialized successfully with test data!");
            } else {
                System.out.println("Database tables already exist.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize database!");
            e.printStackTrace();
        }
    }

    /**
     * Check if a table exists in the database
     */
    private boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * Create database schema from resource file
     */
    private void createSchemaFromResource() {
        try (InputStream is = getClass().getResourceAsStream("/database/schema.sql")) {
            if (is == null) {
                System.err.println("schema.sql file not found in resources/database/");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            // Read the entire file
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line).append(" ");
            }
            
            // Split by semicolon and execute each statement
            String[] sqlStatements = sqlBuilder.toString().split(";");
            
            try (Statement stmt = connection.createStatement()) {
                for (String sql : sqlStatements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }
            
            System.out.println("Database schema created and test data inserted successfully!");
            
        } catch (Exception e) {
            System.err.println("Failed to execute schema script!");
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is valid
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                connection.setAutoCommit(true);
                
                // Enable foreign keys
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection!");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection!");
            e.printStackTrace();
        }
    }

    /**
     * Execute a query and return ResultSet
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        
        // Set parameters
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        
        return pstmt.executeQuery();
    }

    /**
     * Execute an update (INSERT, UPDATE, DELETE)
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            return pstmt.executeUpdate();
        }
    }

    /**
     * Execute an update and return generated key
     */
    public int executeUpdateWithKey(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Begin transaction
     */
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    /**
     * Commit transaction
     */
    public void commit() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Rollback transaction
     */
    public void rollback() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }
}
