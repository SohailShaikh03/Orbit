package com.orbit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.orbit.model.Post;
import com.orbit.util.DBConnection;

/**
 * PostDao (Data Access Object)
 * 
 * RESPONSIBILITY:
 * Handles all JDBC database operations for the 'posts' table in MySQL.
 * Powers the "MYVIBE" social feed.
 */
public class PostDao {

    /**
     * Inserts a new post into the database.
     */
    public int createPost(Post post) {
        String sql = "INSERT INTO posts (user_id, content, media_url) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getMediaUrl() != null ? post.getMediaUrl() : "");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        post.setPostId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("PostDao.createPost Error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves all posts for the global MyVibe feed, ordered newest first.
     * Joins with users table to fetch the author's name.
     */
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.post_id, p.user_id, p.content, p.media_url, u.u_name as author_name " +
                     "FROM posts p JOIN users u ON p.user_id = u.id " +
                     "ORDER BY p.post_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Post post = new Post();
                post.setPostId(rs.getInt("post_id"));
                post.setUserId(rs.getInt("user_id"));
                post.setContent(rs.getString("content"));
                post.setMediaUrl(rs.getString("media_url"));
                post.setAuthorName(rs.getString("author_name"));
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("PostDao.getAllPosts Error: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }

    /**
     * Retrieves posts published by a specific user.
     */
    public List<Post> getPostsByUserId(int userId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.post_id, p.user_id, p.content, p.media_url, u.u_name as author_name " +
                     "FROM posts p JOIN users u ON p.user_id = u.id " +
                     "WHERE p.user_id = ? ORDER BY p.post_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setPostId(rs.getInt("post_id"));
                    post.setUserId(rs.getInt("user_id"));
                    post.setContent(rs.getString("content"));
                    post.setMediaUrl(rs.getString("media_url"));
                    post.setAuthorName(rs.getString("author_name"));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            System.err.println("PostDao.getPostsByUserId Error: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }
}
