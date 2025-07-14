package com.whatsapp.whatsapp.service;

import com.whatsapp.whatsapp.entity.Chatroom;
import com.whatsapp.whatsapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ChatroomService {
    Page<Chatroom> listChatroomsForUser(Long userId, Pageable pageable);
    Chatroom createChatroom(Long creatorId, String name, Boolean isGroup, List<Long> memberIds);
    Optional<Chatroom> getChatroomForUser(Long chatroomId, Long userId);
    List<User> addMembers(Long chatroomId, Long userId, List<Long> memberIds);
} 