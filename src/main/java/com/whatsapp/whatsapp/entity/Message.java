package com.whatsapp.whatsapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private Chatroom chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private AttachmentType attachmentType; // PICTURE, VIDEO, NONE

    private String attachmentUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public enum AttachmentType {
        NONE, PICTURE, VIDEO
    }
} 