package com.profitrack.auth.service;

import com.profitrack.auth.domain.User;
import com.profitrack.auth.domain.UserSession;
import com.profitrack.auth.exception.AuthException;
import com.profitrack.auth.repository.UserRepository;
import com.profitrack.auth.repository.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserSessionRepository sessionRepo;
    private final UserRepository userRepo;

    @Value("${app.jwt.access-expiration-seconds}")
    private long accessExp;
    @Value("${app.jwt.refresh-expiration-seconds}")
    private long refreshExp;

    @Transactional
    public void createSession(User user, String deviceInfo,
                              HttpServletResponse res) {
        String sessionId = UUID.randomUUID().toString();
        String rawRefresh = UUID.randomUUID().toString();

        sessionRepo.save(UserSession.builder()
                .userId(user.getId())
                .sessionId(sessionId)
                .refreshTokenHash(hash(rawRefresh))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshExp))
                .build());

        String accessToken = buildJwt(user, sessionId);
        setAccessCookie(res, accessToken);
        setRefreshCookie(res, rawRefresh);
    }

    @Transactional
    public void rotateSession(String rawRefresh, HttpServletResponse res) {
        UserSession session = sessionRepo.findByRefreshTokenHash(hash(rawRefresh))
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (session.isRevoked() || session.getExpiresAt().isBefore(Instant.now()))
            throw new AuthException("Session expired or revoked");

        String newRawRefresh = UUID.randomUUID().toString();
        session.setRefreshTokenHash(hash(newRawRefresh));
        session.setExpiresAt(Instant.now().plusSeconds(refreshExp));
        sessionRepo.save(session);

        User user = userRepo.findById(session.getUserId())
                .orElseThrow(() -> new AuthException("User not found"));

        setAccessCookie(res, buildJwt(user, session.getSessionId()));
        setRefreshCookie(res, newRawRefresh);
    }

    @Transactional
    public void revokeSession(String sessionId) {
        sessionRepo.revokeBySessionId(sessionId);
    }

    @Transactional
    public void revokeAllSessions(Long userId) {
        sessionRepo.revokeAllByUserId(userId);
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    // ── Helpers ──────────────────────────────────────────────

    private String buildJwt(User user, String sessionId) {
        Instant now = Instant.now();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("auth-app")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExp))
                .claim("userId", user.getId())
                .claim("sessionId", sessionId)
                .claim("role", user.getRole().name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private void setAccessCookie(HttpServletResponse res, String token) {
        Cookie c = new Cookie("access_token", token);
        c.setHttpOnly(true);
        c.setSecure(false); // true en producción (HTTPS)
        c.setPath("/");
        c.setMaxAge((int) accessExp);
        res.addCookie(c);
    }

    private void setRefreshCookie(HttpServletResponse res, String token) {
        Cookie c = new Cookie("refresh_token", token);
        c.setHttpOnly(true);
        c.setSecure(false);
        c.setPath("/api/auth/refresh");
        c.setMaxAge((int) refreshExp);
        res.addCookie(c);
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}
