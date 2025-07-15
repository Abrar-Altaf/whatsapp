package com.whatsapp.whatsapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.requests.UpdateProfileRequest;
import com.whatsapp.whatsapp.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserService userService;

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("username") String usernameHeader) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestHeader("username") String usernameHeader,
                                           @Valid @RequestBody UpdateProfileRequest update) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        try {
            User users = userOpt.get();
            users.setDisplayName(update.getDisplayName());
            User user = userService.updateUser(users.getId(), users);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestHeader("username") String usernameHeader,
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
            String url = "/" + filename;
            userService.updateProfileUrl(user.getId(), url);
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("profileUrl", url));
        } catch (IOException ex) {
            return ResponseEntity.status(500).body("File upload error");
        }
    }

    @GetMapping("/fetch-users-by-mobile-numbers")
    public ResponseEntity<?> getUsersByMobileNumbers(@RequestParam("mobile_numbers") List<String> mobileNumbers) {
        if (mobileNumbers == null || mobileNumbers.isEmpty()) {
            return ResponseEntity.badRequest().body("mobileNumbers parameter is required");
        }
        List<User> users = userService.getUsersByMobileNumbers(mobileNumbers);
        return ResponseEntity.ok(users);
    }
} 