package com.whatsapp.whatsapp.repository;

import com.whatsapp.whatsapp.entity.ChatroomMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, Long> {
    Page<ChatroomMember> findAllByUserId(Long userId, Pageable pageable);
    boolean existsByChatroomIdAndUserId(Long chatroomId, Long userId);
} 