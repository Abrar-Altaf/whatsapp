package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;

    // Simulate authentication: userId from header
    private Long getUserIdFromHeader(String userIdHeader) {
        try {
            return Long.parseLong(userIdHeader);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("X-USER-ID") String userIdHeader) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<User> user = userRepository.findById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestHeader("X-USER-ID") String userIdHeader,
                                           @RequestBody User update) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        user.setDisplayName(update.getDisplayName());
        user.setAvatarUrl(update.getAvatarUrl());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
} 