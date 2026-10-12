package com.orbit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.orbit.model.Comment;
import com.orbit.util.DBConnection;

/**
 * CommentDao (Data Access Object)
 * 
 * RESPONSIBILITY:
 * Handles all JDBC database operations for the 'comments' table in MySQL.
 */
public class CommentDao {

    /**
     * Adds a new comment to a post.
     */
    public int addComment(Comment comment) {
        String sql = "INSERT INTO comments (post_id, user_id, comment_text) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, comment.getPostId());
            ps.setInt(2, comment.getUserId());
            ps.setString(3, comment.getCommentText());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        comment.setCommentId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("CommentDao.addComment Error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves all comments for a given post, ordered chronologically.
     */
    public List<Comment> getCommentsByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.post_id, c.user_id, c.comment_text, u.u_name as author_name " +
                     "FROM comments c JOIN users u ON c.user_id = u.id " +
                     "WHERE c.post_id = ? ORDER BY c.comment_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(rs.getInt("comment_id"));
                    comment.setPostId(rs.getInt("post_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setCommentText(rs.getString("comment_text"));
                    comment.setAuthorName(rs.getString("author_name"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("CommentDao.getCommentsByPostId Error: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }
}
