package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.*;
import com.whatsapp.whatsapp.repository.*;
import com.whatsapp.whatsapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/chatrooms/{chatroomId}/messages")
//@RequiredArgsConstructor
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Value("${attachment.picture.dir:src/main/resources/static/picture}")
    private String pictureDir;
    @Value("${attachment.video.dir:src/main/resources/static/video}")
    private String videoDir;
    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }

    // List messages in chatroom (paginated)
    @GetMapping
    public ResponseEntity<?> listMessages(@RequestHeader("X-USERNAME") String usernameHeader,
                                          @PathVariable Long chatroomId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = messageService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<Message> messages = messageService.listMessages(chatroomId, userId, pageable);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // Send a message (text or attachment)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> sendMessage(@RequestHeader("X-USERNAME") String usernameHeader,
                                         @PathVariable Long chatroomId,
                                         @RequestParam(value = "content", required = false) String content,
                                         @RequestParam(value = "attachment", required = false) MultipartFile attachment) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = messageService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
        try {
            Message message = messageService.sendMessage(chatroomId, userId, content, attachment);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload error");
        }
    }
} 