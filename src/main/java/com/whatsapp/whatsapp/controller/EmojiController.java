package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.Message;
import com.whatsapp.whatsapp.entity.MessageEmoji;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.MessageEmojiRepository;
import com.whatsapp.whatsapp.repository.MessageRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.EmojiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messages/{messageId}/emoji")
//@RequiredArgsConstructor
public class EmojiController {
    @Autowired
    private EmojiService emojiService;

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }

    // Add or replace emoji reaction
    @PostMapping
    public ResponseEntity<?> addOrReplaceEmoji(@RequestHeader("X-USERNAME") String usernameHeader,
                                               @PathVariable Long messageId,
                                               @RequestBody EmojiRequest req) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = emojiService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
        try {
            MessageEmoji emoji = emojiService.addOrReplaceEmoji(messageId, userId, req.getEmojiType());
            return ResponseEntity.ok(emoji);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get emoji reactions for a message
    @GetMapping
    public ResponseEntity<?> getEmojis(@PathVariable Long messageId) {
        return ResponseEntity.ok(emojiService.getEmojisForMessage(messageId));
    }

    @lombok.Data
    public static class EmojiRequest {
        private String emojiType;
    }
} 