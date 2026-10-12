package com.orbit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.orbit.model.Message;
import com.orbit.util.DBConnection;

/**
 * MessageDao (Data Access Object)
 * 
 * RESPONSIBILITY:
 * Handles all JDBC database operations for the 'messages' table in MySQL.
 * Powers the 1-to-1 direct messaging feature in "CIRCLE".
 */
public class MessageDao {

    /**
     * Inserts a new direct message into the database.
     */
    public int sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, is_read) VALUES (?, ?, ?, false)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            ps.setString(3, message.getMessageText());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        message.setMessageId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("MessageDao.sendMessage Error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves all messages exchanged between two users in chronological order.
     */
    public List<Message> getConversation(int user1Id, int user2Id) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.message_id, m.sender_id, m.receiver_id, m.message_text, m.is_read, u.u_name as sender_name " +
                     "FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.message_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user1Id);
            ps.setInt(2, user2Id);
            ps.setInt(3, user2Id);
            ps.setInt(4, user1Id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setMessageId(rs.getInt("message_id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setReceiverId(rs.getInt("receiver_id"));
                    m.setMessageText(rs.getString("message_text"));
                    m.setRead(rs.getBoolean("is_read"));
                    m.setSenderName(rs.getString("sender_name"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("MessageDao.getConversation Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Marks unread messages sent to the receiver as read.
     */
    public boolean markAsRead(int senderId, int receiverId) {
        String sql = "UPDATE messages SET is_read = true WHERE sender_id = ? AND receiver_id = ? AND is_read = false";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MessageDao.markAsRead Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
