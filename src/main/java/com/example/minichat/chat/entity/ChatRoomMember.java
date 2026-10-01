package com.example.minichat.chat.entity;

import com.example.minichat.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "CHAT_ROOM_MEMBERS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_ROOM_MEMBER",
                        columnNames = {"ROOM_ID", "USER_ID"}
                )
        }
)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "JOINED_AT", nullable = false)
    private LocalDateTime joinedAt;

    protected ChatRoomMember() {
    }

    public ChatRoomMember(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}