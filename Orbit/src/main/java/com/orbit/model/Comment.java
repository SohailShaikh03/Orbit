package com.orbit.model;

import java.io.Serializable;

/**
 * Model Class: Comment
 * 
 * RESPONSIBILITY:
 * Represents a user's comment on a post, mapping directly to the 'comments' table in MySQL.
 * 
 * DATABASE MAPPING:
 * Table: comments
 * - comment_id INT AUTO_INCREMENT -> commentId
 * - post_id INT                   -> postId (foreign key to posts.post_id)
 * - user_id INT                   -> userId (foreign key to users.id)
 * - comment_text TEXT             -> commentText
 */
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int commentId;
    private int postId;
    private int userId;
    private String commentText;

    // Helper field for UI display
    private String authorName;

    // Default No-Arg Constructor
    public Comment() {
    }

    // Constructor without ID (Used when writing a new comment)
    public Comment(int postId, int userId, String commentText) {
        this.postId = postId;
        this.userId = userId;
        this.commentText = commentText;
    }

    // Full Constructor
    public Comment(int commentId, int postId, int userId, String commentText) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.commentText = commentText;
    }

    // Getters and Setters
    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public String toString() {
        return "Comment [commentId=" + commentId + ", postId=" + postId + ", userId=" + userId + ", commentText="
                + commentText + "]";
    }
}
