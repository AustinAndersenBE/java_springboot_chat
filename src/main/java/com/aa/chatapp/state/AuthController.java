package com.aa.chatapp.state;

import com.aa.chatapp.logic.Helpers;
import com.aa.chatapp.state.model.User; // Import the User class
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthService userRegistrationService;

    public AuthController(AuthService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    // acts as a DTO
    static class UserRegistrationRequest {

        @NotBlank(message = "Username is required")
        public String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        public String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        User registeredUser = userRegistrationService.registerUser(
                request.username, request.email, request.password
        );

        if (registeredUser == null) {
            // Appropriate error handling here
            return ResponseEntity.badRequest().body("Registration failed due to a conflict or invalid data.");
        }

        Data.JwtConfig jwtConfig = new Data.JwtConfig("YourSecretKey", 3600000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", registeredUser.getId()); // Assuming User class has getId method
        String token = Helpers.createJwtToken(jwtConfig, registeredUser);

        HttpHeaders headers = new HttpHeaders();
        String cookieValue = "token=" + token + "; Max-Age=" + jwtConfig.validityInMilliseconds()/1000;
        cookieValue += "; HttpOnly; Path=/";
        // Omit Secure; SameSite=None for local development without HTTPS

        headers.add(HttpHeaders.SET_COOKIE, cookieValue);

        return ResponseEntity.ok().headers(headers).body(registeredUser);

    }
}
