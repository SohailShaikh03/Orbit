package com.orbit.model;

import java.io.Serializable;

/**
 * Model Class: Post
 * 
 * RESPONSIBILITY:
 * Represents a post created by a user, mapping directly to the 'posts' table in MySQL.
 * Powers the "MYVIBE" social feed feature.
 * 
 * DATABASE MAPPING:
 * Table: posts
 * - post_id INT AUTO_INCREMENT -> postId
 * - user_id INT                -> userId (foreign key to users.id)
 * - content TEXT               -> content
 * - media_url VARCHAR(500)     -> mediaUrl (optional image/media link)
 */
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private int postId;
    private int userId;
    private String content;
    private String mediaUrl;

    // Helper field: Author's name for displaying directly on the feed
    private String authorName;

    // Default No-Arg Constructor
    public Post() {
    }

    // Constructor without ID (Used when creating a new post)
    public Post(int userId, String content, String mediaUrl) {
        this.userId = userId;
        this.content = content;
        this.mediaUrl = mediaUrl;
    }

    // Full Constructor
    public Post(int postId, int userId, String content, String mediaUrl) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.mediaUrl = mediaUrl;
    }

    // Getters and Setters
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public String toString() {
        return "Post [postId=" + postId + ", userId=" + userId + ", content=" + content + ", mediaUrl=" + mediaUrl + "]";
    }
}
