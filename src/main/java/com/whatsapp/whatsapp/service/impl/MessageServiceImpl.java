package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.Message;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.enums.AttachmentType;
import com.whatsapp.whatsapp.repository.ChatroomMemberRepository;
import com.whatsapp.whatsapp.repository.ChatroomRepository;
import com.whatsapp.whatsapp.repository.MessageRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatroomRepository chatroomRepository;
    @Autowired
    private ChatroomMemberRepository chatroomMemberRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${attachment.picture.dir:src/main/resources/static/picture}")
    private String pictureDir;
    @Value("${attachment.video.dir:src/main/resources/static/video}")
    private String videoDir;
    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public Page<Message> listMessages(Long chatroomId, Long userId, Pageable pageable) {
        try {
            if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
                throw new RuntimeException("Not a member");
            }
            return messageRepository.findAllByChatroomIdOrderByCreatedAtAsc(chatroomId, pageable);
        } catch (Exception e) {
            logger.error("Exception in MessageServiceImpl", e);
            return Page.empty();
        }
    }

    @Override
    public Message sendMessage(Long chatroomId, Long userId, String content, MultipartFile attachment) throws IOException {
        try {
            Optional<User> senderOpt = userRepository.findById(userId);
            if (senderOpt.isEmpty()) throw new RuntimeException("User not found");
            if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
                throw new RuntimeException("Not a member");
            }
            AttachmentType attachmentType = AttachmentType.NONE;
            String attachmentUrl = null;
            if (attachment != null && !attachment.isEmpty()) {
                if (attachment.getSize() > MAX_ATTACHMENT_SIZE) {
                    throw new RuntimeException("Attachment too large (max 10MB)");
                }
                String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(attachment.getOriginalFilename()));
                String ext = StringUtils.getFilenameExtension(originalFilename);
                String uuid = UUID.randomUUID().toString();
                String filename = uuid + (ext != null ? "." + ext : "");
                String subdir;
                if (isPicture(ext)) {
                    subdir = pictureDir;
                    attachmentType = AttachmentType.PICTURE;
                } else if (isVideo(ext)) {
                    subdir = videoDir;
                    attachmentType = AttachmentType.VIDEO;
                } else {
                    throw new RuntimeException("Unsupported attachment type");
                }
                Path dirPath = Paths.get(subdir);
                Files.createDirectories(dirPath);
                Path filePath = dirPath.resolve(filename);
                attachment.transferTo(filePath);
                attachmentUrl = "/" + filename;
            }
            Message message = Message.builder()
                    .chatroom(chatroomRepository.getReferenceById(chatroomId))
                    .sender(senderOpt.get())
                    .content(content)
                    .attachmentType(attachmentType)
                    .attachmentUrl(attachmentUrl)
                    .createdAt(Instant.now())
                    .build();
            return messageRepository.save(message);
        } catch (Exception e) {
            logger.error("Exception in MessageServiceImpl", e);
            return null;
        }
    }

    @Override
    public Optional<Message> getMessageById(Long messageId) {
        try {
            return messageRepository.findById(messageId);
        } catch (Exception e) {
            logger.error("Exception in MessageServiceImpl", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<com.whatsapp.whatsapp.entity.User> getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Exception in MessageServiceImpl", e);
            return Optional.empty();
        }
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