package com.orbit.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.orbit.model.Connection;
import com.orbit.model.User;
import com.orbit.service.CircleService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller: CircleController
 * 
 * RESPONSIBILITY:
 * Handles the "CIRCLE" connection network (managing requests, accepted friends).
 * Mapped to: /api/circle
 */
@WebServlet("/api/circle")
public class CircleController extends HttpServlet {
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
        String action = request.getParameter("action");
        if (action == null) action = "friends";

        if ("friends".equalsIgnoreCase(action)) {
            List<User> friends = circleService.getCircleFriends(currentUser.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"friends\":[");
            for (int i = 0; i < friends.size(); i++) {
                User u = friends.get(i);
                sb.append("{")
                  .append("\"id\":").append(u.getId()).append(",")
                  .append("\"name\":\"").append(escapeJson(u.getuName())).append("\",")
                  .append("\"bio\":\"").append(escapeJson(u.getBio())).append("\",")
                  .append("\"interest\":\"").append(escapeJson(u.getInterest())).append("\"")
                  .append("}");
                if (i < friends.size() - 1) sb.append(",");
            }
            sb.append("]}");
            out.print(sb.toString());

        } else if ("requests".equalsIgnoreCase(action)) {
            List<Connection> requests = circleService.getPendingRequests(currentUser.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"requests\":[");
            for (int i = 0; i < requests.size(); i++) {
                Connection c = requests.get(i);
                sb.append("{")
                  .append("\"connectionId\":").append(c.getConnectionId()).append(",")
                  .append("\"senderId\":").append(c.getSenderId()).append(",")
                  .append("\"senderName\":\"").append(escapeJson(c.getSenderName())).append("\",")
                  .append("\"status\":\"").append(c.getStatus()).append("\"")
                  .append("}");
                if (i < requests.size() - 1) sb.append(",");
            }
            sb.append("]}");
            out.print(sb.toString());
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Unknown action.\"}");
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
            out.print("{\"success\":false,\"message\":\"Guests cannot send or accept circle connections. Please sign in or register to build your circle!\"}");
            return;
        }

        String action = request.getParameter("action");

        if ("connect".equalsIgnoreCase(action)) {
            String targetIdStr = request.getParameter("targetUserId");
            try {
                int targetId = Integer.parseInt(targetIdStr);
                boolean sent = circleService.sendConnectionRequest(currentUser.getId(), targetId);
                if (sent) {
                    out.print("{\"success\":true,\"message\":\"Connection request sent!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Could not send request or request already pending.\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid target user ID.\"}");
            }

        } else if ("accept".equalsIgnoreCase(action)) {
            String senderIdStr = request.getParameter("senderId");
            try {
                int senderId = Integer.parseInt(senderIdStr);
                boolean ok = circleService.acceptConnection(senderId, currentUser.getId());
                if (ok) {
                    out.print("{\"success\":true,\"message\":\"Connection accepted! Added to your Circle.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Failed to accept connection.\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid sender ID.\"}");
            }

        } else if ("reject".equalsIgnoreCase(action)) {
            String senderIdStr = request.getParameter("senderId");
            try {
                int senderId = Integer.parseInt(senderIdStr);
                boolean ok = circleService.rejectConnection(senderId, currentUser.getId());
                if (ok) {
                    out.print("{\"success\":true,\"message\":\"Connection request rejected.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Failed to reject request.\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid sender ID.\"}");
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
