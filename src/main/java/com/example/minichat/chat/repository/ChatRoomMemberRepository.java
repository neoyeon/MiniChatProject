package com.example.minichat.chat.repository;

import com.example.minichat.chat.entity.ChatRoom;
import com.example.minichat.chat.entity.ChatRoomMember;
import com.example.minichat.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository
        extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByUser(User user);

    List<ChatRoomMember> findByRoom(ChatRoom room);

    //이 사용자가 채팅방의 멤버인가?
    boolean existsByRoomAndUser(ChatRoom room, User user);
}
