package com.orbit.model;

import java.io.Serializable;

/**
 * Model Class: Connection
 * 
 * RESPONSIBILITY:
 * Represents a connection or connection request between two users,
 * mapping directly to the 'connections' table in MySQL.
 * 
 * DATABASE MAPPING:
 * Table: connections
 * - connection_id INT AUTO_INCREMENT -> connectionId
 * - sender_id INT                   -> senderId (foreign key to users.id)
 * - receiver_id INT                 -> receiverId (foreign key to users.id)
 * - status VARCHAR(20)              -> status ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED')
 */
public class Connection implements Serializable {
    private static final long serialVersionUID = 1L;

    private int connectionId;
    private int senderId;
    private int receiverId;
    private String status; // PENDING, ACCEPTED, REJECTED, BLOCKED

    // Optional helper fields for UI display (populated via JOIN queries if needed)
    private String senderName;
    private String receiverName;

    // Default No-Arg Constructor
    public Connection() {
    }

    // Constructor without ID (Used when sending a new connection request)
    public Connection(int senderId, int receiverId, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
    }

    // Full Constructor
    public Connection(int connectionId, int senderId, int receiverId, String status) {
        this.connectionId = connectionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
    }

    // Getters and Setters
    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    @Override
    public String toString() {
        return "Connection [connectionId=" + connectionId + ", senderId=" + senderId + ", receiverId=" + receiverId
                + ", status=" + status + "]";
    }
}
