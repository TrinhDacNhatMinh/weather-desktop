package com.nhom.weatherdesktop.dto.response;

import java.time.Instant;

public record AlertResponse(
    Long id,
    String message,
    String status,
    Instant createdAt,
    Long weatherDataId,
    Long stationId,
    String stationName
) {
}
