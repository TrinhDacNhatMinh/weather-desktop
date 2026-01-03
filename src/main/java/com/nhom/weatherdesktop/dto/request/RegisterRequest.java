package com.nhom.weatherdesktop.dto.request;

public record RegisterRequest(
    String name,
    String username,
    String password,
    String email
) {
}
