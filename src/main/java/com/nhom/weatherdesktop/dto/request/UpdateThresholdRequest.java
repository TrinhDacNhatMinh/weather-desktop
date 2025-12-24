package com.nhom.weatherdesktop.dto.request;

import java.math.BigDecimal;

public record UpdateThresholdRequest(
    BigDecimal temperatureMin,
    BigDecimal temperatureMax,
    BigDecimal humidityMin,
    BigDecimal humidityMax,
    BigDecimal rainfallMax,
    BigDecimal windSpeedMax,
    BigDecimal dustMax,
    Boolean temperatureActive,
    Boolean humidityActive,
    Boolean rainfallActive,
    Boolean windSpeedActive,
    Boolean dustActive
) {
}
