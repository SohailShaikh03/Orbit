package com.orbit.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.orbit.model.MatchResult;
import com.orbit.model.User;
import com.orbit.service.MatchService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller: MatchController
 * 
 * RESPONSIBILITY:
 * Handles the "ORBIT MATCH" discovery feature.
 * Calculates dynamic compatibility between logged-in user and all candidate users.
 * Mapped to: /api/match
 */
@WebServlet("/api/match")
public class MatchController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MatchService matchService = new MatchService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"Please log in to discover Orbit matches.\"}");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        List<MatchResult> matches = matchService.findMatchesForUser(currentUser);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"matches\":[");

        for (int i = 0; i < matches.size(); i++) {
            MatchResult m = matches.get(i);
            User u = m.getTargetUser();
            sb.append("{")
              .append("\"userId\":").append(u.getId()).append(",")
              .append("\"name\":\"").append(escapeJson(u.getuName())).append("\",")
              .append("\"bio\":\"").append(escapeJson(u.getBio())).append("\",")
              .append("\"interest\":\"").append(escapeJson(u.getInterest())).append("\",")
              .append("\"matchPercentage\":").append(m.getMatchPercentage()).append(",")
              .append("\"connectionStatus\":\"").append(m.getConnectionStatus()).append("\",")
              .append("\"commonInterests\":[");

            List<String> common = m.getCommonInterests();
            for (int j = 0; j < common.size(); j++) {
                sb.append("\"").append(escapeJson(common.get(j))).append("\"");
                if (j < common.size() - 1) sb.append(",");
            }
            sb.append("]}");

            if (i < matches.size() - 1) sb.append(",");
        }

        sb.append("]}");
        out.print(sb.toString());
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
