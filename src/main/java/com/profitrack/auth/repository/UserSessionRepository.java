package com.profitrack.auth.repository;

import com.profitrack.auth.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findBySessionId(String sessionId);
    Optional<UserSession> findByRefreshTokenHash(String hash);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true WHERE s.userId = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true WHERE s.sessionId = :sessionId")
    void revokeBySessionId(@Param("sessionId") String sessionId);
}