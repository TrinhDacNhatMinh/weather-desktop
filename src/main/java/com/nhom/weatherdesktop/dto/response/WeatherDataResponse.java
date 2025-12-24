package com.nhom.weatherdesktop.dto.response;

import java.time.Instant;

public record WeatherDataResponse(
    Long id,
    Float temperature,
    Float humidity,
    Float rainfall,
    Float windSpeed,
    Float dust,
    Instant recordAt,
    Long stationId,
    String stationName
) {
}
