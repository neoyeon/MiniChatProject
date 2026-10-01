package com.example.minichat.friend.service;

import com.example.minichat.friend.entity.Friend;
import com.example.minichat.friend.repository.FriendRepository;
import com.example.minichat.user.entity.User;
import com.example.minichat.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendService(
            FriendRepository friendRepository,
            UserRepository userRepository
    ) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    // 아이디로 사용자 검색
    public User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    // 친구 목록 조회
    public List<Friend> getFriends(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        return friendRepository.findByUser(user);
    }

    // 친구 추가
    public void addFriend(String username, String friendUsername) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다.")
                );

        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException("친구를 찾을 수 없습니다.")
                );

        // 자기 자신을 친구로 추가하는 것 방지
        if (user.getId().equals(friend.getId())) {
            throw new IllegalArgumentException("자기 자신은 친구로 추가할 수 없습니다.");
        }

        // 이미 친구인지 확인
        if (friendRepository.existsByUserAndFriend(user, friend)) {
            throw new IllegalArgumentException("이미 친구입니다.");
        }

        // 서로 친구로 등록
        Friend friendship1 = new Friend(user, friend);
        Friend friendship2 = new Friend(friend, user);

        friendRepository.save(friendship1);
        friendRepository.save(friendship2);
    }

    public boolean isFriend(
            String username,
            String friendUsername
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "친구를 찾을 수 없습니다."
                        )
                );

        return friendRepository.existsByUserAndFriend(
                user,
                friend
        );
    }


}