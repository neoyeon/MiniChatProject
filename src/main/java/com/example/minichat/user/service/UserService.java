package com.example.minichat.user.service;

import com.example.minichat.user.entity.User;
import com.example.minichat.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(String username, String password, String nickname) {

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(
                username,
                encodedPassword,
                nickname
        );

        return userRepository.save(user);
    }

    public User login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다.")
                );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        return user;
    }
}