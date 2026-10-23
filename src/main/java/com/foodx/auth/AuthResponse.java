package com.foodx.auth;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String fullName,
        String role,
        String accessToken,
        String refreshToken) {
}
