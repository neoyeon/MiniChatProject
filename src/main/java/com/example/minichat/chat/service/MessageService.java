package com.example.minichat.chat.service;

import com.example.minichat.chat.entity.ChatRoom;
import com.example.minichat.chat.entity.Message;
import com.example.minichat.chat.repository.ChatRoomMemberRepository;
import com.example.minichat.chat.repository.MessageRepository;
import com.example.minichat.chat.service.ChatRoomService;
import com.example.minichat.user.entity.User;
import com.example.minichat.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            ChatRoomService chatRoomService
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRoomService = chatRoomService;
    }

    @Transactional
    public Message sendMessage(
            Long roomId,
            String username,
            String content
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        // 사용자가 해당 채팅방의 멤버인지 확인
        ChatRoom room = chatRoomService.getRoom(roomId, username);

        // 빈 메시지는 저장하지 않음
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지를 입력해주세요.");
        }

        Message message =
                new Message(room, user, content);

        return messageRepository.save(message);
    }

    public List<Message> getMessages(ChatRoom room) {
        return messageRepository.findByRoomOrderByCreatedAtAsc(room);
    }
}
