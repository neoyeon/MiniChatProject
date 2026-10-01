package com.example.minichat.chat.controller;

import com.example.minichat.chat.dto.ChatMessage;
import com.example.minichat.chat.service.MessageService;
import com.example.minichat.user.entity.User;
import com.example.minichat.user.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(
            MessageService messageService,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
            ChatMessage chatMessage,
            Authentication authentication
    ) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // DB에 메시지 저장
        var savedMessage = messageService.sendMessage(
                chatMessage.getRoomId(),
                username,
                chatMessage.getContent()
        );

        // 해당 채팅방의 모든 사용자에게 전송
        ChatMessage response =
                new ChatMessage(
                        chatMessage.getRoomId(),
                        username,
                        user.getNickname(),
                        chatMessage.getContent(),
                        savedMessage.getCreatedAt()
                );

        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                response
        );
    }
}
