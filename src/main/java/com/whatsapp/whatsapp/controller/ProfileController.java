package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.Optional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/profile")
//@RequiredArgsConstructor
public class ProfileController {
    @Autowired
    private UserService userService;

    // Simulate authentication: username from header
    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("X-USERNAME") String usernameHeader) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestHeader("X-USERNAME") String usernameHeader,
                                           @Valid @RequestBody UpdateProfileRequest update) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        try {
            User users = userOpt.get();
            users.setDisplayName(update.getDisplayName());
            if (update.getProfileUrl() != null) {
                users.setProfileUrl(update.getProfileUrl());
            }
            User user = userService.updateUser(users.getId(), users);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestHeader("X-USERNAME") String usernameHeader,
                                                  @RequestParam("file") MultipartFile file) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded");
        String ext = org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null) return ResponseEntity.badRequest().body("File must have an extension");
        String e = ext.toLowerCase();
        if (!(e.equals("jpg") || e.equals("jpeg") || e.equals("png"))) {
            return ResponseEntity.badRequest().body("Only jpg, jpeg, png allowed");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File too large (max 10MB)");
        }
        String uuid = UUID.randomUUID().toString();
        String filename = uuid + "." + ext;
        String subdir = "src/main/resources/static/profile";
        try {
            Path dirPath = Paths.get(subdir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(filename);
            file.transferTo(filePath);
            String url = "/static/profile/" + filename;
            userService.updateProfileUrl(user.getId(), url);
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("profileUrl", url));
        } catch (IOException ex) {
            return ResponseEntity.status(500).body("File upload error");
        }
    }

    // DTO for profile update
    @lombok.Data
    public static class UpdateProfileRequest {
        @NotBlank
        private String displayName;
        private String profileUrl;
    }
} 