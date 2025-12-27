package com.nhom.weatherdesktop.dto.request;

import java.math.BigDecimal;

public record UpdateStationRequest(
        String name,
        String location,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
