package com.whatsapp.whatsapp.repository;

import com.whatsapp.whatsapp.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
} 