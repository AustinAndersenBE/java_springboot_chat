package com.aa.chatapp.state;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class User {
    public String username;
    public String email;
    public String hashedPassword;

}

public class UserRegistration {

    private EntityManager entityManager;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserRegistration(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public User registerUser(String username, String email, String password) {
        String hashedPassword = hashPassword(password);

        User user = new User();
        user.username = username;
        user.email = email;
        user.hashedPassword = hashedPassword;

        // Persist the new user using the EntityManager
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            entityManager.persist(user);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            // Handle exception (e.g., username already exists, etc.)
            return null; // Or handle it differently
        }

        return user;
    }

    private String hashPassword(String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        return hashedPassword;
    }
}

