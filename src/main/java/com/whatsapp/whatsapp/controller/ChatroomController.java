package com.whatsapp.whatsapp.controller;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chatrooms")
//@RequiredArgsConstructor
public class ChatroomController {
    @Autowired
    private ChatroomService chatroomService;

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }

    // List my chatrooms (paginated)
    @GetMapping
    public ResponseEntity<?> listChatrooms(@RequestHeader("username") String usernameHeader,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = chatroomService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chatroomService.listChatroomsForUser(userId, pageable).getContent());
    }

    // Create a new chatroom (1:1 or group)
    @PostMapping
    public ResponseEntity<?> createChatroom(@RequestHeader("username") String usernameHeader,
                                            @RequestBody CreateChatroomRequest req) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = chatroomService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
        try {
            Chatroom chatroom = chatroomService.createChatroom(userId, req.getName(), req.getIsGroup(), req.getMemberIds());
            return ResponseEntity.ok(chatroom);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get chatroom details
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatroom(@RequestHeader("username") String usernameHeader,
                                         @PathVariable Long id) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = chatroomService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
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
    public ResponseEntity<?> addMembers(@RequestHeader("username") String usernameHeader,
                                        @PathVariable Long id,
                                        @RequestBody AddMembersRequest req) {
        String username = getUsernameFromHeader(usernameHeader);
        if (username == null || username.isEmpty()) return ResponseEntity.badRequest().body("Invalid username");
        Optional<User> userOpt = chatroomService.getUserByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long userId = userOpt.get().getId();
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