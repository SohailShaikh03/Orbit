/**
 * Package: com.orbit.dao
 * 
 * RESPONSIBILITY (Data Access Object Layer):
 * Contains DAO classes responsible for direct communication with the MySQL database via JDBC.
 * - UserDao.java
 * - ConnectionDao.java
 * - PostDao.java
 * - CommentDao.java
 * - MessageDao.java
 * 
 * RULES:
 * - Only SQL statements (SELECT, INSERT, UPDATE, DELETE) and PreparedStatement mappings live here.
 * - No HTTP or business logic should ever be written in this layer.
 */
package com.orbit.dao;
