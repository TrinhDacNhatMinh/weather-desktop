package com.nhom.weatherdesktop.dto.response;

public record RegisterResponse(
    Long userId,
    String username,
    String email,
    String message,
    Boolean active
) {
}
