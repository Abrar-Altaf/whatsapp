package com.whatsapp.whatsapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.whatsapp.whatsapp.entity.Chatroom;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.requests.AddMembersRequest;
import com.whatsapp.whatsapp.requests.CreateChatroomRequest;
import com.whatsapp.whatsapp.service.ChatroomService;

@RestController
@RequestMapping("/chatrooms")
public class ChatroomController {
    @Autowired
    private ChatroomService chatroomService;

    private String getUsernameFromHeader(String usernameHeader) {
        return usernameHeader;
    }
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
            return ResponseEntity.status(403).body("Not a member or not found");
        }
    }
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
 
} 