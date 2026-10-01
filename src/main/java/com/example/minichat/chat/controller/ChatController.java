package com.example.minichat.chat.controller;

import com.example.minichat.chat.entity.ChatRoom;
import com.example.minichat.chat.entity.ChatRoomType;
import com.example.minichat.chat.service.ChatRoomService;
import com.example.minichat.chat.entity.Message;
import com.example.minichat.chat.service.MessageService;
import com.example.minichat.friend.entity.Friend;
import com.example.minichat.friend.service.FriendService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final FriendService friendService;
    private final MessageService messageService;

    public ChatController(
            ChatRoomService chatRoomService,
            FriendService friendService,
            MessageService messageService
    ) {
        this.chatRoomService = chatRoomService;
        this.friendService = friendService;
        this.messageService = messageService;
    }

    @GetMapping("/main/make")
    public String makePage(
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        List<Friend> friends =
                friendService.getFriends(username);

        model.addAttribute("friends", friends);

        return "make";
    }

    @PostMapping("/main/room")
    public String createRoom(
            @RequestParam String name,
            @RequestParam ChatRoomType type,
            @RequestParam List<String> friendUsername,
            Authentication authentication
    ) {
        String username = authentication.getName();

        chatRoomService.createRoom(
                name,
                type,
                username,
                friendUsername
        );

        return "redirect:/main";
    }

    @GetMapping("/main/room/{roomId}")
    public String enterRoom(
            @PathVariable Long roomId,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        ChatRoom room =
                chatRoomService.getRoom(roomId, username);

        List<Message> messages =
                messageService.getMessages(room);

        model.addAttribute("room", room);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUsername", username);

        return "chat";
    }

    @GetMapping("/main/room/{roomId}/fragment")
    public String chatFragment(
            @PathVariable Long roomId,
            Authentication authentication,
            Model model
    ) {

        String username =
                authentication.getName();


        ChatRoom room =
                chatRoomService.getRoom(
                        roomId,
                        username
                );


        List<Message> messages =
                messageService.getMessages(
                        room
                );


        model.addAttribute(
                "room",
                room
        );


        model.addAttribute(
                "messages",
                messages
        );


        model.addAttribute(
                "currentUsername",
                username
        );


        return "chat-fragment";
    }



}