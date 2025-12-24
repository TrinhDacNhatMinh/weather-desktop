package com.nhom.weatherdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ThresholdResponse(
    Long id,
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
    Boolean dustActive,
    Long stationId
) {
}
