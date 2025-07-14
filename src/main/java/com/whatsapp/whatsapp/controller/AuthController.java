package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> register(@RequestBody User user) {
        // Only username and displayName are required
        if (user.getUsername() == null || user.getDisplayName() == null) {
            return ResponseEntity.badRequest().build();
        }
        // Optionally check for duplicate username
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(409).build(); // Conflict
        }
        User created = userService.createUser(user);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest req) {
        return userService.getUserByUsername(req.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
    }
} 