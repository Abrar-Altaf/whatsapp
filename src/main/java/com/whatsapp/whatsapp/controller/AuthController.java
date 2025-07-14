package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api")
//@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getCountryCode() == null || req.getMobileNumber() == null || req.getDisplayName() == null) {
            return ResponseEntity.badRequest().body("countryCode, mobileNumber, displayName required");
        }
        // Check if user already exists
        if (userService.getUserByCountryCodeAndMobileNumber(req.getCountryCode(), req.getMobileNumber()).isPresent()) {
            return ResponseEntity.status(409).body("User already exists");
        }
        // Generate unique username
        String generatedUsername = userService.generateUniqueUsername();
        User user = User.builder()
                .countryCode(req.getCountryCode())
                .mobileNumber(req.getMobileNumber())
                .displayName(req.getDisplayName())
                .username(generatedUsername)
                .build();
        User created = userService.createUser(user);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getCountryCode() == null || req.getMobileNumber() == null) {
            return ResponseEntity.badRequest().body("countryCode and mobileNumber required");
        }
        var userOpt = userService.getUserByCountryCodeAndMobileNumber(req.getCountryCode(), req.getMobileNumber());
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @lombok.Data
    public static class RegisterRequest {
        private String countryCode;
        private String mobileNumber;
        private String displayName;
    }
    @lombok.Data
    public static class LoginRequest {
        private String countryCode;
        private String mobileNumber;
    }
} 