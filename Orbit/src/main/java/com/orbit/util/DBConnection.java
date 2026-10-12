package com.orbit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection Utility Class
 * 
 * RESPONSIBILITY:
 * This class belongs to the 'util' (utility) layer.
 * Its single responsibility is to establish and provide active JDBC database 
 * connections to the MySQL database named 'Orbit'.
 * 
 * WHY USE A UTILITY CLASS?
 * Instead of writing connection strings, driver registration, and credentials 
 * across every DAO class, we centralize database connection logic here.
 * If database credentials or host change, we only modify this single file.
 */
public class DBConnection {

    // Database Connection Parameters
    private static final String URL = "jdbc:mysql://localhost:3306/Orbit?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root"; // Update this with your local MySQL password if different

    // MySQL 8+ JDBC Driver class name
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            // Load and register the MySQL JDBC Driver into the JVM
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found! Ensure mysql-connector-j JAR is in WEB-INF/lib.");
            e.printStackTrace();
        }
    }

    /**
     * Obtains a new active connection to the Orbit database.
     * 
     * @return Connection object to execute SQL statements.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Utility method to safely close a database connection.
     * 
     * @param connection The Connection object to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test method to verify database connectivity directly from Eclipse or terminal.
     * Run this as a Java Application (Right-click -> Run As -> Java Application).
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("           ORBIT DATABASE CONNECTIVITY TEST       ");
        System.out.println("==================================================");
        System.out.println("Connecting to URL: " + URL);
        System.out.println("Using User: " + USERNAME);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Connected to MySQL database 'Orbit' successfully!");
                System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database Version: " + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("FAILURE: Unable to connect to MySQL database!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            System.out.println("\n💡 HINT: If password is incorrect, update PASSWORD in DBConnection.java.");
        }
        System.out.println("==================================================");
    }
}
