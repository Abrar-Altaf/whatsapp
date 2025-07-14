package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.Chatroom;
import com.whatsapp.whatsapp.entity.ChatroomMember;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.ChatroomMemberRepository;
import com.whatsapp.whatsapp.repository.ChatroomRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.ChatroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatroomServiceImpl implements ChatroomService {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomMemberRepository chatroomMemberRepository;
    private final UserRepository userRepository;

    @Override
    public Page<Chatroom> listChatroomsForUser(Long userId, Pageable pageable) {
        Page<ChatroomMember> memberPage = chatroomMemberRepository.findAllByUserId(userId, pageable);
        return memberPage.map(ChatroomMember::getChatroom);
    }

    @Override
    public Chatroom createChatroom(Long creatorId, String name, Boolean isGroup, List<Long> memberIds) {
        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) throw new RuntimeException("User not found");
        Chatroom chatroom = Chatroom.builder()
                .name(name)
                .isGroup(isGroup)
                .build();
        Chatroom savedChatroom = chatroomRepository.save(chatroom);
        // Add creator as member
        chatroomMemberRepository.save(ChatroomMember.builder().chatroom(savedChatroom).user(creatorOpt.get()).build());
        // Add other members
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
    }

    @Override
    public Optional<Chatroom> getChatroomForUser(Long chatroomId, Long userId) {
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(chatroomId, userId)) {
            return Optional.empty();
        }
        return chatroomRepository.findById(chatroomId);
    }

    @Override
    public List<User> addMembers(Long chatroomId, Long userId, List<Long> memberIds) {
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
    }
} 