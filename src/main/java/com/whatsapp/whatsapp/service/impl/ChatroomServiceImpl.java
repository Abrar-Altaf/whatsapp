package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.Chatroom;
import com.whatsapp.whatsapp.entity.ChatroomMember;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.ChatroomMemberRepository;
import com.whatsapp.whatsapp.repository.ChatroomRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.ChatroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatroomServiceImpl implements ChatroomService {
    private static final Logger logger = LoggerFactory.getLogger(ChatroomServiceImpl.class);
    @Autowired
    private ChatroomRepository chatroomRepository;
    @Autowired
    private ChatroomMemberRepository chatroomMemberRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<Chatroom> listChatroomsForUser(Long userId, Pageable pageable) {
        try {
            Page<ChatroomMember> memberPage = chatroomMemberRepository.findAllByUserId(userId, pageable);
            return memberPage.map(ChatroomMember::getChatroom);
        } catch (Exception e) {
            logger.error("Exception in ChatroomServiceImpl", e);
            return Page.empty();
        }
    }

    @Override
    public Chatroom createChatroom(Long creatorId, String name, Boolean isGroup, List<Long> memberIds) {
        try {
            Optional<User> creatorOpt = userRepository.findById(creatorId);
            if (creatorOpt.isEmpty()) throw new RuntimeException("User not found");
            Chatroom chatroom = Chatroom.builder()
                    .name(name)
                    .isGroup(isGroup)
                    .build();
            Chatroom savedChatroom = chatroomRepository.save(chatroom);
            chatroomMemberRepository.save(ChatroomMember.builder().chatroom(savedChatroom).user(creatorOpt.get()).build());
            if (memberIds != null) {
                for (Long memberId : memberIds) {
                    if (!memberId.equals(creatorId)) {
                        userRepository.findById(memberId).ifPresent(user ->
                                chatroomMemberRepository.save(ChatroomMember.builder().chatroom(savedChatroom).user(user).build())
                        );
                    }
                }
            }
            return savedChatroom;
        } catch (Exception e) {
            logger.error("Exception in ChatroomServiceImpl", e);
            return null;
        }
    }

    @Override
    public Optional<Chatroom> getChatroomForUser(Long chatroomId, Long userId) {
        try {
            if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
                return Optional.empty();
            }
            return chatroomRepository.findById(chatroomId);
        } catch (Exception e) {
            logger.error("Exception in ChatroomServiceImpl", e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> addMembers(Long chatroomId, Long userId, List<Long> memberIds) {
        try {
            if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
                throw new RuntimeException("Not a member");
            }
            Optional<Chatroom> chatroomOpt = chatroomRepository.findById(chatroomId);
            if (chatroomOpt.isEmpty()) throw new RuntimeException("Chatroom not found");
            Chatroom chatroom = chatroomOpt.get();
            List<User> added = new ArrayList<>();
            for (Long memberId : memberIds) {
                if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, memberId)) {
                    userRepository.findById(memberId).ifPresent(user -> {
                        chatroomMemberRepository.save(ChatroomMember.builder().chatroom(chatroom).user(user).build());
                        added.add(user);
                    });
                }
            }
            return added;
        } catch (Exception e) {
            logger.error("Exception in ChatroomServiceImpl", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Exception in ChatroomServiceImpl", e);
            return Optional.empty();
        }
    }
} 