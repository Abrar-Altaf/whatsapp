package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.Message;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.ChatroomMemberRepository;
import com.whatsapp.whatsapp.repository.ChatroomRepository;
import com.whatsapp.whatsapp.repository.MessageRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.MessageService;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatroomMemberRepository chatroomMemberRepository;
    private final UserRepository userRepository;

    @Value("${attachment.picture.dir:src/main/resources/static/picture}")
    private String pictureDir;
    @Value("${attachment.video.dir:src/main/resources/static/video}")
    private String videoDir;
    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public Page<Message> listMessages(Long chatroomId, Long userId, Pageable pageable) {
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
            throw new RuntimeException("Not a member");
        }
        return messageRepository.findAllByChatroomIdOrderByCreatedAtAsc(chatroomId, pageable);
    }

    @Override
    public Message sendMessage(Long chatroomId, Long userId, String content, MultipartFile attachment) throws IOException {
        Optional<User> senderOpt = userRepository.findById(userId);
        if (senderOpt.isEmpty()) throw new RuntimeException("User not found");
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
            throw new RuntimeException("Not a member");
        }
        Message.AttachmentType attachmentType = Message.AttachmentType.NONE;
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
                attachmentType = Message.AttachmentType.PICTURE;
            } else if (isVideo(ext)) {
                subdir = videoDir;
                attachmentType = Message.AttachmentType.VIDEO;
            } else {
                throw new RuntimeException("Unsupported attachment type");
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
        return messageRepository.save(message);
    }

    @Override
    public Optional<Message> getMessageById(Long messageId) {
        return messageRepository.findById(messageId);
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