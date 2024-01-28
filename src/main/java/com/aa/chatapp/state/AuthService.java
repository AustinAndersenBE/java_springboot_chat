package com.aa.chatapp.state;

import com.aa.chatapp.state.model.User;
import com.aa.chatapp.state.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            // will do some frontend returning later
        }
        if (userRepository.existsByEmail(email)) {
            // will do some frontend returning later
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setHashedPassword(hashedPassword);
        
        return userRepository.save(user);
}
}
