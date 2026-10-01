package com.example.minichat.chat.entity;

import com.example.minichat.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ROOMS")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChatRoomType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private User createdBy;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected ChatRoom() {
    }

    public ChatRoom(String name, ChatRoomType type, User createdBy) {
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatRoomType getType() {
        return type;
    }

    public void setType(ChatRoomType type) {
        this.type = type;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}