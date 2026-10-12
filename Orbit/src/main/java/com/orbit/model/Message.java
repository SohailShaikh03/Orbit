package com.orbit.model;

import java.io.Serializable;

/**
 * Model Class: Message
 * 
 * RESPONSIBILITY:
 * Represents a direct 1-to-1 message between two connected users,
 * mapping directly to the 'messages' table in MySQL.
 * Powers the "CIRCLE" 1-to-1 messaging feature.
 * 
 * DATABASE MAPPING:
 * Table: messages
 * - message_id INT AUTO_INCREMENT -> messageId
 * - sender_id INT                 -> senderId (foreign key to users.id)
 * - receiver_id INT               -> receiverId (foreign key to users.id)
 * - message_text TEXT             -> messageText
 * - is_read BOOLEAN               -> isRead (default false)
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private int messageId;
    private int senderId;
    private int receiverId;
    private String messageText;
    private boolean isRead;

    // Helper field for UI display
    private String senderName;

    // Default No-Arg Constructor
    public Message() {
    }

    // Constructor without ID (Used when sending a new message)
    public Message(int senderId, int receiverId, String messageText) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.isRead = false;
    }

    // Full Constructor
    public Message(int messageId, int senderId, int receiverId, String messageText, boolean isRead) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.isRead = isRead;
    }

    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
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

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public String toString() {
        return "Message [messageId=" + messageId + ", senderId=" + senderId + ", receiverId=" + receiverId
                + ", isRead=" + isRead + "]";
    }
}
