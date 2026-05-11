package com.profitrack.auth.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    public static TokenResponse of(String token, long expiresIn) {
        return new TokenResponse(token, "Bearer", expiresIn);
    }
}