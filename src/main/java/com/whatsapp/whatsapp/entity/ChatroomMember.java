package com.whatsapp.whatsapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "chatroom_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatroomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private Chatroom chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false, name = "joined_at")
    @JsonProperty("joined_at")
    private Instant joinedAt;

    @PrePersist
    public void prePersist() {
        this.joinedAt = Instant.now();
    }
} 