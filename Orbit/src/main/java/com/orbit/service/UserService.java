package com.orbit.service;

import com.orbit.dao.UserDao;
import com.orbit.model.User;

/**
 * Service Class: UserService
 * 
 * RESPONSIBILITY:
 * Handles business logic, validations, and user operations.
 */
public class UserService {

    private final UserDao userDao = new UserDao();

    /**
     * Registers a new user with input validation.
     */
    public String register(String name, String email, String password, String bio, String interest) {
        if (name == null || name.trim().isEmpty()) {
            return "Name cannot be empty.";
        }
        if (email == null || !email.contains("@")) {
            return "Invalid email address.";
        }
        if (password == null || password.length() < 3) {
            return "Password must be at least 3 characters.";
        }
        if (userDao.emailExists(email.trim().toLowerCase())) {
            return "An account with this email already exists.";
        }

        User user = new User(
            name.trim(),
            email.trim().toLowerCase(),
            password.trim(),
            bio != null ? bio.trim() : "",
            interest != null ? interest.trim() : ""
        );

        int id = userDao.register(user);
        if (id > 0) {
            return "SUCCESS";
        }
        return "Registration failed due to a database error.";
    }

    /**
     * Authenticates login credentials.
     */
    public User login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        return userDao.login(email.trim().toLowerCase(), password.trim());
    }

    /**
     * Retrieves user by id.
     */
    public User getUserById(int id) {
        return userDao.getUserById(id);
    }

    /**
     * Updates user's bio and interest.
     */
    public boolean updateProfile(int userId, String bio, String interest) {
        return userDao.updateProfile(userId, bio != null ? bio.trim() : "", interest != null ? interest.trim() : "");
    }

    /**
     * Validates input and resets password for an existing registered email.
     */
    public String resetPassword(String email, String newPassword) {
        if (email == null || !email.contains("@")) {
            return "Please enter a valid email address.";
        }
        if (newPassword == null || newPassword.length() < 3) {
            return "Password must be at least 3 characters.";
        }
        String cleanEmail = email.trim().toLowerCase();
        if (!userDao.emailExists(cleanEmail)) {
            return "No account found registered with this email address.";
        }
        boolean ok = userDao.resetPassword(cleanEmail, newPassword.trim());
        if (ok) {
            return "SUCCESS";
        }
        return "Failed to reset password due to a server error.";
    }
}

