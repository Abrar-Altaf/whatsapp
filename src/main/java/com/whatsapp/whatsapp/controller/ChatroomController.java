package com.whatsapp.whatsapp.controller;

import com.whatsapp.whatsapp.entity.Chatroom;
import com.whatsapp.whatsapp.entity.ChatroomMember;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.ChatroomMemberRepository;
import com.whatsapp.whatsapp.repository.ChatroomRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.ChatroomService;
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
    private final ChatroomService chatroomService;

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
        return ResponseEntity.ok(chatroomService.listChatroomsForUser(userId, pageable).getContent());
    }

    // Create a new chatroom (1:1 or group)
    @PostMapping
    public ResponseEntity<?> createChatroom(@RequestHeader("X-USER-ID") String userIdHeader,
                                            @RequestBody CreateChatroomRequest req) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        try {
            Chatroom chatroom = chatroomService.createChatroom(userId, req.getName(), req.getIsGroup(), req.getMemberIds());
            return ResponseEntity.ok(chatroom);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get chatroom details
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatroom(@RequestHeader("X-USER-ID") String userIdHeader,
                                         @PathVariable Long id) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        Optional<Chatroom> chatroomOpt = chatroomService.getChatroomForUser(id, userId);
        if (chatroomOpt.isPresent()) {
            return ResponseEntity.ok(chatroomOpt.get());
        } else {
            // Could be forbidden or not found, but we don't distinguish here
            return ResponseEntity.status(403).body("Not a member or not found");
        }
    }

    // Add member(s) to chatroom
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMembers(@RequestHeader("X-USER-ID") String userIdHeader,
                                        @PathVariable Long id,
                                        @RequestBody AddMembersRequest req) {
        Long userId = getUserIdFromHeader(userIdHeader);
        if (userId == null) return ResponseEntity.badRequest().body("Invalid user id");
        try {
            List<User> added = chatroomService.addMembers(id, userId, req.getMemberIds());
            return ResponseEntity.ok(added);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
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