package com.orbit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.orbit.model.User;
import com.orbit.util.DBConnection;

/**
 * ConnectionDao (Data Access Object)
 * 
 * RESPONSIBILITY:
 * Handles all JDBC database operations for the 'connections' table in MySQL.
 * Powers connection requests, status checks, and Circle friends.
 */
public class ConnectionDao {

    /**
     * Sends a connection request between sender and receiver.
     */
    public boolean sendRequest(int senderId, int receiverId) {
        String checkSql = "SELECT connection_id, status FROM connections WHERE " +
                          "(sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        String insertSql = "INSERT INTO connections (sender_id, receiver_id, status) VALUES (?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection()) {
            // Check if connection or request already exists
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, senderId);
                psCheck.setInt(2, receiverId);
                psCheck.setInt(3, receiverId);
                psCheck.setInt(4, senderId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        // Already exists
                        return false;
                    }
                }
            }

            // Insert new pending request
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, senderId);
                psInsert.setInt(2, receiverId);
                return psInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("ConnectionDao.sendRequest Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the status of an existing connection (e.g. ACCEPTED, REJECTED, BLOCKED).
     */
    public boolean updateStatus(int connectionId, String newStatus) {
        String sql = "UPDATE connections SET status = ? WHERE connection_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, connectionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ConnectionDao.updateStatus Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates status between two specific users (e.g. accepting request from sender).
     */
    public boolean updateStatusBetween(int senderId, int receiverId, String newStatus) {
        String sql = "UPDATE connections SET status = ? WHERE sender_id = ? AND receiver_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, senderId);
            ps.setInt(3, receiverId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ConnectionDao.updateStatusBetween Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks relationship status between two users.
     * Returns: "NONE", "PENDING_SENT" (current user sent), "PENDING_RECEIVED" (current user received), "ACCEPTED", "REJECTED", "BLOCKED"
     */
    public String getConnectionStatus(int currentUserId, int otherUserId) {
        String sql = "SELECT sender_id, receiver_id, status FROM connections WHERE " +
                     "(sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.setInt(2, otherUserId);
            ps.setInt(3, otherUserId);
            ps.setInt(4, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int sender = rs.getInt("sender_id");
                    String status = rs.getString("status");
                    if ("ACCEPTED".equalsIgnoreCase(status)) {
                        return "ACCEPTED";
                    }
                    if ("PENDING".equalsIgnoreCase(status)) {
                        return (sender == currentUserId) ? "PENDING_SENT" : "PENDING_RECEIVED";
                    }
                    return status;
                }
            }
        } catch (SQLException e) {
            System.err.println("ConnectionDao.getConnectionStatus Error: " + e.getMessage());
            e.printStackTrace();
        }
        return "NONE";
    }

    /**
     * Retrieves all accepted connections (friends in user's Circle) with their user profile details.
     */
    public List<User> getAcceptedConnections(int userId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.id, u.u_name, u.email, u.password, u.bio, u.interest FROM users u " +
                     "JOIN connections c ON ( (c.sender_id = u.id AND c.receiver_id = ?) OR " +
                     "                        (c.receiver_id = u.id AND c.sender_id = ?) ) " +
                     "WHERE c.status = 'ACCEPTED' AND u.id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setuName(rs.getString("u_name"));
                    u.setEmail(rs.getString("email"));
                    u.setBio(rs.getString("bio"));
                    u.setInterest(rs.getString("interest"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("ConnectionDao.getAcceptedConnections Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves pending incoming connection requests for a user.
     */
    public List<com.orbit.model.Connection> getPendingRequests(int receiverId) {
        List<com.orbit.model.Connection> list = new ArrayList<>();
        String sql = "SELECT c.connection_id, c.sender_id, c.receiver_id, c.status, u.u_name as sender_name " +
                     "FROM connections c JOIN users u ON c.sender_id = u.id " +
                     "WHERE c.receiver_id = ? AND c.status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.orbit.model.Connection c = new com.orbit.model.Connection();
                    c.setConnectionId(rs.getInt("connection_id"));
                    c.setSenderId(rs.getInt("sender_id"));
                    c.setReceiverId(rs.getInt("receiver_id"));
                    c.setStatus(rs.getString("status"));
                    c.setSenderName(rs.getString("sender_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("ConnectionDao.getPendingRequests Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
