package com.example.minichat.chat.dto;

import java.time.LocalDateTime;

public class ChatMessage {

    private Long roomId;
    private String sender;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(
            Long roomId,
            String sender,
            String nickname,
            String content
    ) {
        this.roomId = roomId;
        this.sender = sender;
        this.nickname = nickname;
        this.content = content;
    }

    public ChatMessage(Long roomId, String sender, String nickname, String content, LocalDateTime createdAt) {
        this(roomId, sender, nickname, content);
        this.createdAt = createdAt;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
