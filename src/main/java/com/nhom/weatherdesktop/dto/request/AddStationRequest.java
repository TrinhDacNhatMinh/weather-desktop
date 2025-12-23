package com.nhom.weatherdesktop.dto.request;

import java.math.BigDecimal;

public record AddStationRequest(
    String name,
    String apiKey,
    BigDecimal latitude,
    BigDecimal longitude
) {
}
