package com.orbit.service;

import java.util.List;

import com.orbit.dao.CommentDao;
import com.orbit.dao.PostDao;
import com.orbit.model.Comment;
import com.orbit.model.Post;

/**
 * Service Class: FeedService
 * 
 * RESPONSIBILITY:
 * Handles business logic for "MYVIBE" social feed (posts & comments).
 */
public class FeedService {

    private final PostDao postDao = new PostDao();
    private final CommentDao commentDao = new CommentDao();

    /**
     * Publishes a new post to the MyVibe feed.
     */
    public boolean publishPost(int userId, String content, String mediaUrl) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        Post post = new Post(userId, content.trim(), mediaUrl != null ? mediaUrl.trim() : "");
        return postDao.createPost(post) > 0;
    }

    /**
     * Retrieves all posts for the global feed.
     */
    public List<Post> getFeedPosts() {
        return postDao.getAllPosts();
    }

    /**
     * Adds a comment to a post.
     */
    public boolean addComment(int postId, int userId, String commentText) {
        if (commentText == null || commentText.trim().isEmpty()) {
            return false;
        }
        Comment comment = new Comment(postId, userId, commentText.trim());
        return commentDao.addComment(comment) > 0;
    }

    /**
     * Retrieves comments for a post.
     */
    public List<Comment> getPostComments(int postId) {
        return commentDao.getCommentsByPostId(postId);
    }
}
