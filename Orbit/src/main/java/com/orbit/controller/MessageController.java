package com.orbit.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.orbit.model.Message;
import com.orbit.model.User;
import com.orbit.service.CircleService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller: MessageController
 * 
 * RESPONSIBILITY:
 * Handles 1-to-1 direct messaging between connected users.
 * Mapped to: /api/messages
 */
@WebServlet("/api/messages")
public class MessageController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final CircleService circleService = new CircleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"Please log in first.\"}");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        String otherUserIdStr = request.getParameter("userId");

        if (otherUserIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Missing userId parameter.\"}");
            return;
        }

        try {
            int otherUserId = Integer.parseInt(otherUserIdStr);
            List<Message> messages = circleService.getConversation(currentUser.getId(), otherUserId);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"messages\":[");
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                sb.append("{")
                  .append("\"messageId\":").append(m.getMessageId()).append(",")
                  .append("\"senderId\":").append(m.getSenderId()).append(",")
                  .append("\"receiverId\":").append(m.getReceiverId()).append(",")
                  .append("\"senderName\":\"").append(escapeJson(m.getSenderName())).append("\",")
                  .append("\"messageText\":\"").append(escapeJson(m.getMessageText())).append("\",")
                  .append("\"isRead\":").append(m.isRead())
                  .append("}");
                if (i < messages.size() - 1) sb.append(",");
            }
            sb.append("]}");
            out.print(sb.toString());

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid userId format.\"}");
        }
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
            out.print("{\"success\":false,\"message\":\"Guests cannot send direct messages. Please sign in or register to chat with connections!\"}");
            return;
        }

        String receiverIdStr = request.getParameter("receiverId");
        String text = request.getParameter("messageText");

        if (receiverIdStr == null || text == null || text.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Receiver and message text are required.\"}");
            return;
        }

        try {
            int receiverId = Integer.parseInt(receiverIdStr);
            boolean ok = circleService.sendMessage(currentUser.getId(), receiverId, text);
            if (ok) {
                out.print("{\"success\":true,\"message\":\"Message sent!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"message\":\"Failed to send message.\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid receiver ID.\"}");
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
