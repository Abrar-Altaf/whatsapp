package com.whatsapp.whatsapp.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whatsapp.whatsapp.entity.MessageEmoji;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.requests.EmojiRequest;
import com.whatsapp.whatsapp.service.EmojiService;

@RestController
@RequestMapping("/messages/{messageId}/emoji")
public class EmojiController {
    @Autowired
    private EmojiService emojiService;

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }
    @PostMapping
    public ResponseEntity<?> addOrReplaceEmoji(@RequestHeader("username") String usernameHeader,
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
    @GetMapping
    public ResponseEntity<?> getEmojis(@PathVariable Long messageId) {
        return ResponseEntity.ok(emojiService.getEmojisForMessage(messageId));
    }


} 