package com.orbit.service;

import java.util.List;

import com.orbit.dao.ConnectionDao;
import com.orbit.dao.MessageDao;
import com.orbit.model.Connection;
import com.orbit.model.Message;
import com.orbit.model.User;

/**
 * Service Class: CircleService
 * 
 * RESPONSIBILITY:
 * Handles business logic for "CIRCLE" (connections, requests, and 1-to-1 messaging).
 */
public class CircleService {

    private final ConnectionDao connectionDao = new ConnectionDao();
    private final MessageDao messageDao = new MessageDao();

    /**
     * Sends a connection request to another user.
     */
    public boolean sendConnectionRequest(int senderId, int receiverId) {
        if (senderId == receiverId) {
            return false;
        }
        return connectionDao.sendRequest(senderId, receiverId);
    }

    /**
     * Accepts an incoming connection request.
     */
    public boolean acceptConnection(int senderId, int receiverId) {
        return connectionDao.updateStatusBetween(senderId, receiverId, "ACCEPTED");
    }

    /**
     * Rejects an incoming connection request.
     */
    public boolean rejectConnection(int senderId, int receiverId) {
        return connectionDao.updateStatusBetween(senderId, receiverId, "REJECTED");
    }

    /**
     * Returns all accepted connections (friends in circle) for the user.
     */
    public List<User> getCircleFriends(int userId) {
        return connectionDao.getAcceptedConnections(userId);
    }

    /**
     * Returns pending incoming requests for the user.
     */
    public List<Connection> getPendingRequests(int userId) {
        return connectionDao.getPendingRequests(userId);
    }

    /**
     * Sends a 1-to-1 direct message to a user.
     */
    public boolean sendMessage(int senderId, int receiverId, String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        Message msg = new Message(senderId, receiverId, text.trim());
        return messageDao.sendMessage(msg) > 0;
    }

    /**
     * Retrieves the chat conversation between two users and marks unread as read.
     */
    public List<Message> getConversation(int user1Id, int user2Id) {
        // Mark messages sent by user2 to user1 as read
        messageDao.markAsRead(user2Id, user1Id);
        return messageDao.getConversation(user1Id, user2Id);
    }
}
