package com.example.minichat.chat.service;

import com.example.minichat.chat.entity.ChatRoom;
import com.example.minichat.chat.entity.ChatRoomMember;
import com.example.minichat.chat.entity.ChatRoomType;
import com.example.minichat.chat.repository.ChatRoomMemberRepository;
import com.example.minichat.chat.repository.ChatRoomRepository;
import com.example.minichat.friend.repository.FriendRepository;
import com.example.minichat.user.entity.User;
import com.example.minichat.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            UserRepository userRepository,
            FriendRepository friendRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public List<ChatRoom> getRooms() {
        return chatRoomRepository.findAll();
    }

    @Transactional
    public void createRoom(
            String name,
            ChatRoomType type,
            String username,
            List<String> friendUsernames
    ) {
        String roomName = name == null ? "" : name.trim();
        if (roomName.isEmpty() || roomName.length() > 100) {
            throw new IllegalArgumentException("채팅방 이름은 1자 이상 100자 이하로 입력해주세요.");
        }
        if (type == null) {
            throw new IllegalArgumentException("채팅방 종류를 선택해주세요.");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        if (friendUsernames == null || friendUsernames.isEmpty()) {
            throw new IllegalArgumentException(
                    "채팅할 친구를 한 명 이상 선택해주세요."
            );
        }

        if (type == ChatRoomType.DIRECT && friendUsernames.size() != 1) {
            throw new IllegalArgumentException("1:1 채팅은 친구 한 명만 선택할 수 있습니다.");
        }

        ChatRoom chatRoom =
                new ChatRoom(roomName, type, user);

        chatRoomRepository.save(chatRoom);

        // 방을 만든 사람을 먼저 멤버로 추가
        ChatRoomMember ownerMember =
                new ChatRoomMember(chatRoom, user);

        chatRoomMemberRepository.save(ownerMember);

        // 선택한 친구들을 멤버로 추가
        for (String friendUsername : friendUsernames) {

            User friend = userRepository.findByUsername(friendUsername)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "친구를 찾을 수 없습니다: " + friendUsername
                            )
                    );

            if (friend.getId().equals(user.getId())) {
                continue;
            }

            if (!friendRepository.existsByUserAndFriend(user, friend)) {
                throw new IllegalArgumentException("친구로 등록된 사용자만 채팅방에 초대할 수 있습니다.");
            }

            if (!chatRoomMemberRepository
                    .existsByRoomAndUser(chatRoom, friend)) {

                ChatRoomMember member =
                        new ChatRoomMember(chatRoom, friend);

                chatRoomMemberRepository.save(member);
            }
        }
    }

    public List<ChatRoom> getMyRooms(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        return chatRoomMemberRepository.findByUser(user)
                .stream()
                .map(ChatRoomMember::getRoom)
                .toList();
    }

    @Transactional
    public ChatRoom getOrCreateDirectRoom(String username, String friendUsername) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("친구를 찾을 수 없습니다."));

        if (user.getId().equals(friend.getId())) {
            throw new IllegalArgumentException("자기 자신과 1:1 채팅을 시작할 수 없습니다.");
        }
        if (!friendRepository.existsByUserAndFriend(user, friend)) {
            throw new IllegalArgumentException("친구로 등록된 사용자와만 대화할 수 있습니다.");
        }

        for (ChatRoomMember membership : chatRoomMemberRepository.findByUser(user)) {
            ChatRoom room = membership.getRoom();
            if (room.getType() != ChatRoomType.DIRECT) {
                continue;
            }

            List<ChatRoomMember> members = chatRoomMemberRepository.findByRoom(room);
            if (members.size() == 2 && members.stream()
                    .anyMatch(member -> member.getUser().getId().equals(friend.getId()))) {
                return room;
            }
        }

        ChatRoom room = chatRoomRepository.save(
                new ChatRoom(user.getNickname() + " · " + friend.getNickname(), ChatRoomType.DIRECT, user)
        );
        chatRoomMemberRepository.save(new ChatRoomMember(room, user));
        chatRoomMemberRepository.save(new ChatRoomMember(room, friend));
        return room;
    }

    public ChatRoom getRoom(Long roomId, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() ->
                        new IllegalArgumentException("채팅방을 찾을 수 없습니다.")
                );

        boolean isMember =
                chatRoomMemberRepository.existsByRoomAndUser(room, user);

        if (!isMember) {
            throw new IllegalArgumentException(
                    "이 채팅방에 참여하고 있지 않습니다."
            );
        }

        return room;
    }
}
