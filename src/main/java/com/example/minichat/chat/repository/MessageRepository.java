package com.example.minichat.chat.repository;

import com.example.minichat.chat.entity.Message;
import com.example.minichat.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByRoomOrderByCreatedAtAsc(ChatRoom room);
}