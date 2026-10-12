/**
 * Package: com.orbit.controller
 * 
 * RESPONSIBILITY (Servlet / Controller Layer):
 * Handles incoming HTTP client requests (GET, POST), interacts with the Service layer,
 * and produces responses (HTML, JSON, or redirects).
 * - Servlets extending HttpServlet (using Jakarta EE 10 / Tomcat 10.x imports: jakarta.servlet.*)
 * - Session tracking (HttpServletRequest.getSession())
 * 
 * RULES:
 * - Does NOT perform direct SQL queries (delegates to Service -> DAO).
 * - Uses jakarta.servlet.http.HttpServlet.
 */
package com.orbit.controller;
