package com.profitrack.dominio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false, unique = true)
    private String refreshTokenHash;

    private String userType;

    @Builder.Default
    private boolean revoked = false;

    private String deviceInfo;

    private Instant createdAt;
    private Instant expiresAt;
}
