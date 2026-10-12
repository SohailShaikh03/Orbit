package com.orbit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.orbit.model.User;
import com.orbit.util.DBConnection;

/**
 * UserDao (Data Access Object)
 * 
 * RESPONSIBILITY:
 * Handles all JDBC database operations for the 'users' table in MySQL.
 */
public class UserDao {

    /**
     * Registers a new user into the database.
     * 
     * @param user The user entity to register.
     * @return The generated user ID, or -1 if registration fails.
     */
    public int register(User user) {
        String sql = "INSERT INTO users (u_name, email, password, bio, interest) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getuName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getBio() != null ? user.getBio() : "");
            ps.setString(5, user.getInterest() != null ? user.getInterest() : "");

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        user.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDao.register Error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Authenticates a user by email and password.
     * 
     * @param email The user's email.
     * @param password The user's password.
     * @return User object if credentials are valid, or null.
     */
    public User login(String email, String password) {
        String sql = "SELECT id, u_name, email, password, bio, interest FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDao.login Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds a user by ID.
     */
    public User getUserById(int id) {
        String sql = "SELECT id, u_name, email, password, bio, interest FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDao.getUserById Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("UserDao.emailExists Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all users except the current logged-in user.
     * Used by MatchService to find candidates for interest comparison.
     */
    public List<User> getAllUsersExcept(int currentUserId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, u_name, email, password, bio, interest FROM users WHERE id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDao.getAllUsersExcept Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates user's profile bio and interests.
     */
    public boolean updateProfile(int userId, String bio, String interest) {
        String sql = "UPDATE users SET bio = ?, interest = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bio);
            ps.setString(2, interest);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDao.updateProfile Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Resets the password for a given email address.
     * 
     * @param email The registered user's email.
     * @param newPassword The new plain-text password to set.
     * @return true if password was successfully updated, false otherwise.
     */
    public boolean resetPassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, email);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDao.resetPassword Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Helper method to map a ResultSet row to a User entity.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setuName(rs.getString("u_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setBio(rs.getString("bio"));
        user.setInterest(rs.getString("interest"));
        return user;
    }
}
