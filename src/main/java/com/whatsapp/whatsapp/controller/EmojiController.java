package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.Message;
import com.whatsapp.whatsapp.entity.MessageEmoji;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.MessageEmojiRepository;
import com.whatsapp.whatsapp.repository.MessageRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messages/{messageId}/emoji")
@RequiredArgsConstructor
public class EmojiController {
    private final MessageEmojiRepository messageEmojiRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private Long getUserIdFromHeader(String userIdHeader) {
        try {
            return Long.parseLong(userIdHeader);
        } catch (Exception e) {
            return null;
        }
    }

    // Add or replace emoji reaction
    @PostMapping
    public ResponseEntity<?> addOrReplaceEmoji(@RequestHeader("X-USER-ID") String userIdHeader,
                                               @PathVariable Long messageId,
                                               @RequestBody EmojiRequest req) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) return ResponseEntity.notFound().build();
        MessageEmoji.EmojiType emojiType;
        try {
            emojiType = MessageEmoji.EmojiType.valueOf(req.getEmojiType().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid emoji type");
        }
        // Only allow specific emojis
        if (!EnumSet.of(MessageEmoji.EmojiType.THUMBUP, MessageEmoji.EmojiType.LOVE, MessageEmoji.EmojiType.CRYING, MessageEmoji.EmojiType.SURPRISED).contains(emojiType)) {
            return ResponseEntity.badRequest().body("Unsupported emoji type");
        }
        // Remove existing reaction by this user
        messageEmojiRepository.findAll().stream()
                .filter(e -> e.getMessage().getId().equals(messageId) && e.getUser().getId().equals(userId))
                .findFirst()
                .ifPresent(messageEmojiRepository::delete);
        // Add new reaction
        MessageEmoji emoji = MessageEmoji.builder()
                .message(messageOpt.get())
                .user(userOpt.get())
                .emojiType(emojiType)
                .build();
        messageEmojiRepository.save(emoji);
        return ResponseEntity.ok(emoji);
    }

    // Get emoji reactions for a message
    @GetMapping
    public ResponseEntity<?> getEmojis(@PathVariable Long messageId) {
        List<MessageEmoji> emojis = messageEmojiRepository.findAll();
        List<MessageEmoji> filtered = new ArrayList<>();
        for (MessageEmoji e : emojis) {
            if (e.getMessage().getId().equals(messageId)) {
                filtered.add(e);
            }
        }
        return ResponseEntity.ok(filtered);
    }

    @lombok.Data
    public static class EmojiRequest {
        private String emojiType;
    }
} 