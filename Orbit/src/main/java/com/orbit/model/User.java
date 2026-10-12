package com.orbit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Model Class: User
 * 
 * RESPONSIBILITY:
 * Represents a single user entity mapping directly to the 'users' table in MySQL.
 * 
 * WHY A POJO (Plain Old Java Object)?
 * In a layered architecture, data moves across layers (Database -> DAO -> Service -> Controller -> View).
 * Instead of passing loose variables or ResultSets, we encapsulate all user attributes 
 * inside this strongly-typed User object.
 * 
 * DATABASE MAPPING:
 * Table: users
 * - id INT AUTO_INCREMENT -> id
 * - u_name VARCHAR(100)   -> uName
 * - email VARCHAR(150)    -> email
 * - password VARCHAR(255) -> password
 * - bio VARCHAR(500)      -> bio
 * - interest VARCHAR(500) -> interest (e.g. "Java,AI,Gaming,Movies")
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String uName;
    private String email;
    private String password;
    private String bio;
    private String interest;

    // Default No-Arg Constructor (Required for JavaBean conventions)
    public User() {
    }

    // Constructor without ID (Used when registering/creating a new user before DB generates the ID)
    public User(String uName, String email, String password, String bio, String interest) {
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.interest = interest;
    }

    // Full Constructor (Used when retrieving an existing user from the database)
    public User(int id, String uName, String email, String password, String bio, String interest) {
        this.id = id;
        this.uName = uName;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.interest = interest;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    /**
     * Helper method to split the comma-separated interest string into clean individual interests.
     * E.g., "Java, AI, Gaming" -> ["Java", "AI", "Gaming"]
     * 
     * @return List of trimmed interest strings.
     */
    public List<String> getInterestsList() {
        List<String> list = new ArrayList<>();
        if (this.interest != null && !this.interest.trim().isEmpty()) {
            String[] tokens = this.interest.split(",");
            for (String token : tokens) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }

    @Override
    public String toString() {
        // Exclude sensitive password from toString() for security
        return "User [id=" + id + ", uName=" + uName + ", email=" + email + ", bio=" + bio + ", interest=" + interest + "]";
    }
}
