package com.example.minichat.friend.repository;

import com.example.minichat.friend.entity.Friend;
import com.example.minichat.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    boolean existsByUserAndFriend(User user, User friend);
}