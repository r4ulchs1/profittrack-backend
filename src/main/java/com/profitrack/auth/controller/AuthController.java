package com.profitrack.auth.controller;

import com.profitrack.auth.domain.User;
import com.profitrack.auth.dto.LoginRequest;
import com.profitrack.auth.dto.RegisterRequest;
import com.profitrack.auth.exception.AuthException;
import com.profitrack.auth.service.TokenService;
import com.profitrack.auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return ResponseEntity.status(201).body(Map.of("message", "User registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req,
                                                     HttpServletRequest httpReq,
                                                     HttpServletResponse httpRes) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userService.findByEmail(req.email());
        tokenService.createSession(user, httpReq.getHeader("User-Agent"), httpRes);

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest req,
                                                       HttpServletResponse res) {
        String rawRefresh = Arrays.stream(
                        Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AuthException("No refresh token"));

        tokenService.rotateSession(rawRefresh, res);
        return ResponseEntity.ok(Map.of("message", "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        String token = Arrays.stream(
                        Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

        if (token != null) {
            Jwt jwt = tokenService.decode(token);
            tokenService.revokeSession(jwt.getClaimAsString("sessionId"));
        }

        clearCookies(res);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest req, HttpServletResponse res) {
        String token = Arrays.stream(
                        Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "access_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

        if (token != null) {
            Jwt jwt = tokenService.decode(token);
            tokenService.revokeAllSessions(((Number) jwt.getClaim("userId")).longValue());
        }

        clearCookies(res);
        return ResponseEntity.noContent().build();
    }

    private void clearCookies(HttpServletResponse res) {
        Stream.of("access_token", "refresh_token").forEach(name -> {
            Cookie c = new Cookie(name, "");
            c.setMaxAge(0);
            c.setPath("/");
            c.setHttpOnly(true);
            res.addCookie(c);
        });
    }
}