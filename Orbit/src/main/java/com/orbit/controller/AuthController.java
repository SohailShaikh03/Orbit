package com.orbit.controller;

import java.io.IOException;
import java.io.PrintWriter;

import com.orbit.model.User;
import com.orbit.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller: AuthController
 * 
 * Handles User Registration, Login, Logout, and Session State.
 * Mapped to: /api/auth
 */
@WebServlet("/api/auth")
public class AuthController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "me";

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if ("me".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                User user = (User) session.getAttribute("currentUser");
                Boolean isGuest = (Boolean) session.getAttribute("isGuest");
                if (Boolean.TRUE.equals(isGuest) || user.getId() <= 0) {
                    out.print(userToJson(user, true, true));
                } else {
                    // Refresh user state from DB
                    User freshUser = userService.getUserById(user.getId());
                    if (freshUser != null) {
                        session.setAttribute("currentUser", freshUser);
                        user = freshUser;
                    }
                    out.print(userToJson(user, true, false));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,\"message\":\"Not authenticated\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Unknown GET action\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if ("register".equalsIgnoreCase(action)) {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String bio = request.getParameter("bio");
            String interest = request.getParameter("interest");

            String result = userService.register(name, email, password, bio, interest);
            if ("SUCCESS".equals(result)) {
                User loggedIn = userService.login(email, password);
                if (loggedIn != null) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("currentUser", loggedIn);
                    session.setAttribute("isGuest", false);
                    out.print("{\"success\":true,\"message\":\"Registered successfully!\",\"user\":" + userToJson(loggedIn, false, false) + "}");
                } else {
                    out.print("{\"success\":true,\"message\":\"Registration successful! Please login.\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"" + escapeJson(result) + "\"}");
            }

        } else if ("login".equalsIgnoreCase(action)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            User user = userService.login(email, password);
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("currentUser", user);
                session.setAttribute("isGuest", false);
                out.print("{\"success\":true,\"message\":\"Login successful! Welcome back " + escapeJson(user.getuName()) + "\",\"user\":" + userToJson(user, false, false) + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,\"message\":\"Invalid email or password.\"}");
            }

        } else if ("guestLogin".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(true);
            User guest = new User();
            guest.setId(0);
            guest.setuName("Guest Explorer");
            guest.setEmail("guest@orbit.local");
            guest.setBio("Browsing Orbit in Guest Mode. Explore matches, feeds, and circles freely!");
            guest.setInterest("Tech, AI, Space, Coding");

            session.setAttribute("currentUser", guest);
            session.setAttribute("isGuest", true);

            out.print("{\"success\":true,\"message\":\"Welcome, Guest Explorer! Enjoy previewing Orbit.\",\"isGuest\":true,\"user\":" + userToJson(guest, false, true) + "}");

        } else if ("forgotPassword".equalsIgnoreCase(action) || "resetPassword".equalsIgnoreCase(action)) {
            String email = request.getParameter("email");
            String newPassword = request.getParameter("newPassword");

            String result = userService.resetPassword(email, newPassword);
            if ("SUCCESS".equals(result)) {
                out.print("{\"success\":true,\"message\":\"Password reset successfully! Please sign in with your new password.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"" + escapeJson(result) + "\"}");
            }

        } else if ("logout".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            out.print("{\"success\":true,\"message\":\"Logged out successfully.\"}");

        } else if ("updateProfile".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                Boolean isGuest = (Boolean) session.getAttribute("isGuest");
                if (Boolean.TRUE.equals(isGuest)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"success\":false,\"message\":\"Guests cannot edit profile. Please create an account to customize your profile!\"}");
                    return;
                }

                User user = (User) session.getAttribute("currentUser");
                String bio = request.getParameter("bio");
                String interest = request.getParameter("interest");

                boolean updated = userService.updateProfile(user.getId(), bio, interest);
                if (updated) {
                    user.setBio(bio);
                    user.setInterest(interest);
                    session.setAttribute("currentUser", user);
                    out.print("{\"success\":true,\"message\":\"Profile updated successfully!\",\"user\":" + userToJson(user, false, false) + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Could not update profile.\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,\"message\":\"Please login first.\"}");
            }

        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Unknown POST action\"}");
        }
    }

    private String userToJson(User u, boolean wrapSuccess, boolean isGuest) {
        StringBuilder sb = new StringBuilder();
        if (wrapSuccess) {
            sb.append("{\"success\":true,\"isGuest\":").append(isGuest).append(",\"user\":");
        }
        sb.append("{")
          .append("\"id\":").append(u.getId()).append(",")
          .append("\"name\":\"").append(escapeJson(u.getuName())).append("\",")
          .append("\"email\":\"").append(escapeJson(u.getEmail())).append("\",")
          .append("\"bio\":\"").append(escapeJson(u.getBio())).append("\",")
          .append("\"interest\":\"").append(escapeJson(u.getInterest())).append("\",")
          .append("\"isGuest\":").append(isGuest)
          .append("}");
        if (wrapSuccess) {
            sb.append("}");
        }
        return sb.toString();
    }

    private String userToJson(User u, boolean wrapSuccess) {
        return userToJson(u, wrapSuccess, u != null && u.getId() <= 0);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
