package com.whatsapp.whatsapp.repository;

import com.whatsapp.whatsapp.entity.MessageEmoji;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageEmojiRepository extends JpaRepository<MessageEmoji, Long> {
} 