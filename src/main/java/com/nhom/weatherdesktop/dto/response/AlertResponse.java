package com.nhom.weatherdesktop.dto.response;

import java.time.LocalDateTime;

public record AlertResponse(
    Long id,
    Long stationId,
    String type,
    String message,
    String severity,
    LocalDateTime timestamp
) {
}
