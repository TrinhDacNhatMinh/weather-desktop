package com.nhom.weatherdesktop.dto.request;

import com.nhom.weatherdesktop.enums.AccessChannel;

public record LoginRequest(
        String username,
        String password,
        AccessChannel accessChannel
) {
}
