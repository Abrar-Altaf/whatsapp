package com.whatsapp.whatsapp.repository;

import com.whatsapp.whatsapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findAllByChatroomIdOrderByCreatedAtAsc(Long chatroomId, Pageable pageable);
} 