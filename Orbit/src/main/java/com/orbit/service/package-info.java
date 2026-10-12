/**
 * Package: com.orbit.service
 * 
 * RESPONSIBILITY (Business Logic Layer):
 * Encapsulates the core business rules and algorithms of ORBIT.
 * - MatchService.java (Calculates Orbit Match compatibility, common interests)
 * - UserService.java (User registration validation, authentication)
 * - FeedService.java (MyVibe feed generation, post management)
 * - CircleService.java (Connection requests, message handling)
 * 
 * RULES:
 * - Decouples controllers from direct database calls.
 * - In V1, the dynamic Match score calculation resides here.
 */
package com.orbit.service;
