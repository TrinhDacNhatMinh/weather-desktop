package com.nhom.weatherdesktop.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {
}
