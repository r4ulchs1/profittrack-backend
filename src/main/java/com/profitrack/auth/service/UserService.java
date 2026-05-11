package com.profitrack.auth.service;

import com.profitrack.auth.domain.User;
import com.profitrack.auth.dto.RegisterRequest;
import com.profitrack.auth.exception.AuthException;
import com.profitrack.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest req) {
        if (userRepo.findByEmail(req.email()).isPresent())
            throw new AuthException("Email already in use");

        return userRepo.save(User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(User.Role.USER)
                .enabled(true)
                .build());
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));
    }
}