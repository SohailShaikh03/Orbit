package com.orbit.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Diagnostic Utility: VerifyDBSchema
 * 
 * Verifies that all 5 required tables (users, connections, posts, comments, messages)
 * exist in the MySQL 'Orbit' database and lists their column details.
 */
public class VerifyDBSchema {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("         ORBIT DATABASE SCHEMA VERIFICATION       ");
        System.out.println("==================================================");

        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String[] expectedTables = {"users", "connections", "posts", "comments", "messages"};

            for (String tableName : expectedTables) {
                System.out.println("\nChecking Table: [" + tableName + "]");
                try (ResultSet rs = metaData.getTables("Orbit", null, tableName, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        System.out.println("  Status: EXISTS");
                        // List columns
                        try (ResultSet cols = metaData.getColumns("Orbit", null, tableName, "%")) {
                            while (cols.next()) {
                                String colName = cols.getString("COLUMN_NAME");
                                String typeName = cols.getString("TYPE_NAME");
                                int colSize = cols.getInt("COLUMN_SIZE");
                                System.out.println("    - " + colName + " (" + typeName + "[" + colSize + "])");
                            }
                        }
                    } else {
                        System.out.println("  Status: NOT FOUND in 'Orbit' database!");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Verification Failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("\n==================================================");
    }
}
