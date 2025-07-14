package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.Chatroom;
import com.whatsapp.whatsapp.entity.ChatroomMember;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.ChatroomMemberRepository;
import com.whatsapp.whatsapp.repository.ChatroomRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatroomController {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomMemberRepository chatroomMemberRepository;
    private final UserRepository userRepository;

    private Long getUserIdFromHeader(String userIdHeader) {
        try {
            return Long.parseLong(userIdHeader);
        } catch (Exception e) {
            return null;
        }
    }

    // List my chatrooms (paginated)
    @GetMapping
    public ResponseEntity<?> listChatrooms(@RequestHeader("X-USER-ID") String userIdHeader,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatroomMember> memberPage = chatroomMemberRepository.findAllByUserId(userId, pageable);
        List<Chatroom> chatrooms = memberPage.map(ChatroomMember::getChatroom).getContent();
        return ResponseEntity.ok(chatrooms);
    }

    // Create a new chatroom (1:1 or group)
    @PostMapping
    public ResponseEntity<?> createChatroom(@RequestHeader("X-USER-ID") String userIdHeader,
                                            @RequestBody CreateChatroomRequest req) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<User> creatorOpt = userRepository.findById(userId);
        if (creatorOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        Chatroom chatroom = Chatroom.builder()
                .name(req.getName())
                .isGroup(req.getIsGroup())
                .build();
        Chatroom savedChatroom = chatroomRepository.save(chatroom);
        // Add creator as member
        chatroomMemberRepository.save(ChatroomMember.builder().chatroom(savedChatroom).user(creatorOpt.get()).build());
        // Add other members
        if (req.getMemberIds() != null) {
            for (Long memberId : req.getMemberIds()) {
                if (!memberId.equals(userId)) {
                    userRepository.findById(memberId).ifPresent(user ->
                        chatroomMemberRepository.save(ChatroomMember.builder().chatroom(savedChatroom).user(user).build())
                    );
                }
            }
        }
        return ResponseEntity.ok(savedChatroom);
    }

    // Get chatroom details
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatroom(@RequestHeader("X-USER-ID") String userIdHeader,
                                         @PathVariable Long id) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<Chatroom> chatroomOpt = chatroomRepository.findById(id);
        if (chatroomOpt.isEmpty()) return ResponseEntity.notFound().build();
        // Check membership
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(id, userId)) {
            return ResponseEntity.status(403).body("Not a member");
        }
        return ResponseEntity.ok(chatroomOpt.get());
    }

    // Add member(s) to chatroom
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMembers(@RequestHeader("X-USER-ID") String userIdHeader,
                                        @PathVariable Long id,
                                        @RequestBody AddMembersRequest req) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        if (!chatroomMemberRepository.existsByChatroomIdAndUserId(id, userId)) {
            return ResponseEntity.status(403).body("Not a member");
        }
        Optional<Chatroom> chatroomOpt = chatroomRepository.findById(id);
        if (chatroomOpt.isEmpty()) return ResponseEntity.notFound().build();
        Chatroom chatroom = chatroomOpt.get();
        List<User> added = new ArrayList<>();
        for (Long memberId : req.getMemberIds()) {
            if (!chatroomMemberRepository.existsByChatroomIdAndUserId(id, memberId)) {
                userRepository.findById(memberId).ifPresent(user -> {
                    chatroomMemberRepository.save(ChatroomMember.builder().chatroom(chatroom).user(user).build());
                    added.add(user);
                });
            }
        }
        return ResponseEntity.ok(added);
    }

    // DTOs
    @lombok.Data
    public static class CreateChatroomRequest {
        private String name;
        private Boolean isGroup;
        private List<Long> memberIds;
    }
    @lombok.Data
    public static class AddMembersRequest {
        private List<Long> memberIds;
    }
} 