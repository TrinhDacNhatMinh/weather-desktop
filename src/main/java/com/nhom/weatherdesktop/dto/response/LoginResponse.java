package com.nhom.weatherdesktop.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String name,
        String email
) {
}
