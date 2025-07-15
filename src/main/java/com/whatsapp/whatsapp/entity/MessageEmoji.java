package com.whatsapp.whatsapp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.whatsapp.whatsapp.enums.EmojiType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message_emojis", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEmoji {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "emoji_type")
    @JsonProperty("emoji_type")
    private EmojiType emojiType;

  
} 