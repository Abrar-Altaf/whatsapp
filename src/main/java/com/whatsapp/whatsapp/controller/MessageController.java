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
import com.whatsapp.whatsapp.entity.MessageEmoji;
import com.whatsapp.whatsapp.repository.MessageEmojiRepository;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatrooms/{chatroomId}/messages")
//@RequiredArgsConstructor
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageEmojiRepository messageEmojiRepository;

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
    public ResponseEntity<?> listMessages(@RequestHeader("username") String usernameHeader,
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
            // Aggregate emojis for each message
            List<MessageWithEmojisDTO> result = messages.getContent().stream().map(msg -> {
                List<EmojiDTO> emojis = messageEmojiRepository.findAll().stream()
                    .filter(e -> e.getMessage().getId().equals(msg.getId()))
                    .map(e -> new EmojiDTO(e.getUser().getId(), e.getEmojiType().name()))
                    .collect(Collectors.toList());
                return new MessageWithEmojisDTO(msg, emojis);
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // Send a message (text or attachment)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> sendMessage(@RequestHeader("username") String usernameHeader,
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

    // DTOs for message+emojis
    public static class MessageWithEmojisDTO {
        public Long id;
        public String content;
        public String attachmentUrl;
        public String attachmentType;
        public Long senderId;
        public Long chatroomId;
        public String createdAt;
        public List<EmojiDTO> emojis;
        public MessageWithEmojisDTO(Message msg, List<EmojiDTO> emojis) {
            this.id = msg.getId();
            this.content = msg.getContent();
            this.attachmentUrl = msg.getAttachmentUrl();
            this.attachmentType = msg.getAttachmentType() != null ? msg.getAttachmentType().name() : null;
            this.senderId = msg.getSender() != null ? msg.getSender().getId() : null;
            this.chatroomId = msg.getChatroom() != null ? msg.getChatroom().getId() : null;
            this.createdAt = msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null;
            this.emojis = emojis;
        }
    }
    public static class EmojiDTO {
        public Long userId;
        public String emojiType;
        public EmojiDTO(Long userId, String emojiType) {
            this.userId = userId;
            this.emojiType = emojiType;
        }
    }
} 