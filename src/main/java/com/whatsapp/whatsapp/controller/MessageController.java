package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.*;
import com.whatsapp.whatsapp.repository.*;
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

@RestController
@RequestMapping("/api/chatrooms/{chatroomId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageRepository messageRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatroomMemberRepository chatroomMemberRepository;
    private final UserRepository userRepository;

    @Value("${attachment.picture.dir:src/main/resources/static/picture}")
    private String pictureDir;
    @Value("${attachment.video.dir:src/main/resources/static/video}")
    private String videoDir;
    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB

    private Long getUserIdFromHeader(String userIdHeader) {
        try {
            return Long.parseLong(userIdHeader);
        } catch (Exception e) {
            return null;
        }
    }

    // List messages in chatroom (paginated)
    @GetMapping
    public ResponseEntity<?> listMessages(@RequestHeader("X-USER-ID") String userIdHeader,
                                          @PathVariable Long chatroomId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
            return ResponseEntity.status(403).body("Not a member");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findAllByChatroomIdOrderByCreatedAtAsc(chatroomId, pageable);
        return ResponseEntity.ok(messages);
    }

    // Send a message (text or attachment)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> sendMessage(@RequestHeader("X-USER-ID") String userIdHeader,
                                         @PathVariable Long chatroomId,
                                         @RequestParam(value = "content", required = false) String content,
                                         @RequestParam(value = "attachment", required = false) MultipartFile attachment) throws IOException {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<User> senderOpt = userRepository.findById(userId);
        if (senderOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
            return ResponseEntity.status(403).body("Not a member");
        }
        Message.AttachmentType attachmentType = Message.AttachmentType.NONE;
        String attachmentUrl = null;
        if (attachment != null && !attachment.isEmpty()) {
            if (attachment.getSize() > MAX_ATTACHMENT_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Attachment too large (max 10MB)");
            }
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(attachment.getOriginalFilename()));
            String ext = StringUtils.getFilenameExtension(originalFilename);
            String uuid = UUID.randomUUID().toString();
            String filename = uuid + (ext != null ? "." + ext : "");
            String subdir;
            if (isPicture(ext)) {
                subdir = pictureDir;
                attachmentType = Message.AttachmentType.PICTURE;
            } else if (isVideo(ext)) {
                subdir = videoDir;
                attachmentType = Message.AttachmentType.VIDEO;
            } else {
                return ResponseEntity.badRequest().body("Unsupported attachment type");
            }
            Path dirPath = Paths.get(subdir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(filename);
            attachment.transferTo(filePath);
            attachmentUrl = "/static/" + (attachmentType == Message.AttachmentType.PICTURE ? "picture/" : "video/") + filename;
        }
        Message message = Message.builder()
                .chatroom(chatroomRepository.getReferenceById(chatroomId))
                .sender(senderOpt.get())
                .content(content)
                .attachmentType(attachmentType)
                .attachmentUrl(attachmentUrl)
                .createdAt(Instant.now())
                .build();
        message = messageRepository.save(message);
        return ResponseEntity.ok(message);
    }

    private boolean isPicture(String ext) {
        if (ext == null) return false;
        String e = ext.toLowerCase();
        return e.equals("jpg") || e.equals("jpeg") || e.equals("png") || e.equals("gif") || e.equals("bmp") || e.equals("webp");
    }
    private boolean isVideo(String ext) {
        if (ext == null) return false;
        String e = ext.toLowerCase();
        return e.equals("mp4") || e.equals("mov") || e.equals("avi") || e.equals("mkv") || e.equals("webm");
    }
} 