package com.example.minichat.friend.controller;

import com.example.minichat.chat.service.ChatRoomService;
import com.example.minichat.friend.entity.Friend;
import com.example.minichat.friend.service.FriendService;
import com.example.minichat.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FriendController {

    private final FriendService friendService;
    private final ChatRoomService chatRoomService;

    public FriendController(
            FriendService friendService,
            ChatRoomService chatRoomService
    ) {
        this.friendService = friendService;
        this.chatRoomService = chatRoomService;
    }

    @GetMapping("/main")
    public String main(
            Authentication authentication,
            @RequestParam(required = false) Long roomId,
            Model model
    ) {
        String username = authentication.getName();

        List<Friend> friends =
                friendService.getFriends(username);

        model.addAttribute("friends", friends);

        model.addAttribute(
                "rooms",
                chatRoomService.getMyRooms(username)
        );

        model.addAttribute(
                "currentUsername",
                username
        );
        model.addAttribute("openRoomId", roomId);

        return "main";
    }

    @GetMapping("/main/friend/search")
    public String searchFriend(
            @RequestParam String username,
            Authentication authentication,
            Model model
    ) {
        String currentUsername = authentication.getName();

        User user = friendService.findUser(username);

        model.addAttribute(
                "friends",
                friendService.getFriends(currentUsername)
        );

        model.addAttribute(
                "rooms",
                chatRoomService.getMyRooms(currentUsername)
        );

        model.addAttribute(
                "currentUsername",
                currentUsername
        );
        model.addAttribute("openRoomId", null);

        model.addAttribute(
                "searchUsername",
                username
        );

        model.addAttribute(
                "searchResult",
                user
        );

        // 검색한 사용자가 존재하고, 자기 자신이 아닌 경우
        if (user != null
                && !user.getUsername().equals(currentUsername)) {

            boolean alreadyFriend =
                    friendService.isFriend(
                            currentUsername,
                            user.getUsername()
                    );

            model.addAttribute(
                    "alreadyFriend",
                    alreadyFriend
            );
        }

        return "main";
    }

    @PostMapping("/main/friend/add")
    public String addFriend(
            @RequestParam String username,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();

        friendService.addFriend(currentUsername, username);

        return "redirect:/main";
    }

    @PostMapping("/main/friend/chat")
    public String openDirectChat(
            @RequestParam String username,
            Authentication authentication
    ) {
        var room = chatRoomService.getOrCreateDirectRoom(authentication.getName(), username);
        return "redirect:/main?roomId=" + room.getId();
    }
}
