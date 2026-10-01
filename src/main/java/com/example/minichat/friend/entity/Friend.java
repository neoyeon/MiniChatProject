package com.example.minichat.friend.entity;

import com.example.minichat.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "FRIENDS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_FRIEND",
                        columnNames = {"USER_ID", "FRIEND_ID"}
                )
        }
)
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FRIEND_ID", nullable = false)
    private User friend;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected Friend() {
    }

    public Friend(User user, User friend) {
        this.user = user;
        this.friend = friend;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public User getFriend() {
        return friend;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}