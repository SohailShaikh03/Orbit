package com.orbit.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.orbit.model.Comment;
import com.orbit.model.Post;
import com.orbit.model.User;
import com.orbit.service.FeedService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller: FeedController
 * 
 * RESPONSIBILITY:
 * Handles the "MYVIBE" social feed (fetching posts, publishing posts, adding comments).
 * Mapped to: /api/feed
 */
@WebServlet("/api/feed")
public class FeedController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final FeedService feedService = new FeedService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        List<Post> posts = feedService.getFeedPosts();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"posts\":[");

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            sb.append("{")
              .append("\"postId\":").append(p.getPostId()).append(",")
              .append("\"userId\":").append(p.getUserId()).append(",")
              .append("\"authorName\":\"").append(escapeJson(p.getAuthorName())).append("\",")
              .append("\"content\":\"").append(escapeJson(p.getContent())).append("\",")
              .append("\"mediaUrl\":\"").append(escapeJson(p.getMediaUrl())).append("\",")
              .append("\"comments\":[");

            List<Comment> comments = feedService.getPostComments(p.getPostId());
            for (int j = 0; j < comments.size(); j++) {
                Comment c = comments.get(j);
                sb.append("{")
                  .append("\"commentId\":").append(c.getCommentId()).append(",")
                  .append("\"userId\":").append(c.getUserId()).append(",")
                  .append("\"authorName\":\"").append(escapeJson(c.getAuthorName())).append("\",")
                  .append("\"commentText\":\"").append(escapeJson(c.getCommentText())).append("\"")
                  .append("}");
                if (j < comments.size() - 1) sb.append(",");
            }
            sb.append("]}");

            if (i < posts.size() - 1) sb.append(",");
        }

        sb.append("]}");
        out.print(sb.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"Please log in first.\"}");
            return;
        }

        Boolean isGuest = (Boolean) session.getAttribute("isGuest");
        User currentUser = (User) session.getAttribute("currentUser");
        if (Boolean.TRUE.equals(isGuest) || currentUser.getId() <= 0) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"success\":false,\"message\":\"Guests cannot publish posts or comments. Please sign in or register to join the conversation!\"}");
            return;
        }

        String action = request.getParameter("action");

        if ("post".equalsIgnoreCase(action)) {
            String content = request.getParameter("content");
            String mediaUrl = request.getParameter("mediaUrl");

            if (content == null || content.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Post content cannot be empty.\"}");
                return;
            }

            boolean ok = feedService.publishPost(currentUser.getId(), content, mediaUrl);
            if (ok) {
                out.print("{\"success\":true,\"message\":\"Post shared on MyVibe!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"message\":\"Failed to publish post.\"}");
            }

        } else if ("comment".equalsIgnoreCase(action)) {
            String postIdStr = request.getParameter("postId");
            String text = request.getParameter("commentText");

            if (postIdStr == null || text == null || text.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Comment text cannot be empty.\"}");
                return;
            }

            try {
                int postId = Integer.parseInt(postIdStr);
                boolean ok = feedService.addComment(postId, currentUser.getId(), text);
                if (ok) {
                    out.print("{\"success\":true,\"message\":\"Comment posted!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Could not save comment.\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid post ID.\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid action.\"}");
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
