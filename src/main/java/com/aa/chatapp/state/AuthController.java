package com.aa.chatapp.state;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRegistration userRegistrationService;

    public AuthController(UserRegistration userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    static class UserRegistrationRequest {

        @NotBlank(message = "Username is required")
        public String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        public String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        public String password;

        // Getters and setters
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        User registeredUser = userRegistrationService.registerUser(
                request.username, request.email, request.password
        );

        if (registeredUser == null) {
            // Appropriate error handling here
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(registeredUser);
    }


}
