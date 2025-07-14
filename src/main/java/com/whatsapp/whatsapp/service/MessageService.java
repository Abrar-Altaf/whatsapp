package com.whatsapp.whatsapp.service;

import com.whatsapp.whatsapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface MessageService {
    Page<Message> listMessages(Long chatroomId, Long userId, Pageable pageable);
    Message sendMessage(Long chatroomId, Long userId, String content, MultipartFile attachment) throws IOException;
    Optional<Message> getMessageById(Long messageId);
    Optional<com.whatsapp.whatsapp.entity.User> getUserByUsername(String username);
} 