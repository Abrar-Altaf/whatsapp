package com.whatsapp.whatsapp.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.whatsapp.whatsapp.dto.EmojiDTO;
import com.whatsapp.whatsapp.dto.MessageWithEmojisDTO;
import com.whatsapp.whatsapp.entity.Message;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.MessageEmojiRepository;
import com.whatsapp.whatsapp.service.MessageService;

@RestController
@RequestMapping("/chatrooms/{chatroomId}/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageEmojiRepository messageEmojiRepository;

    @Value("${attachment.picture.dir:src/main/resources/static/picture}")
    private String pictureDir;
    @Value("${attachment.video.dir:src/main/resources/static/video}")
    private String videoDir;

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }
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

   
} 