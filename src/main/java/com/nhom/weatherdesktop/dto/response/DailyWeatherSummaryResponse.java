package com.nhom.weatherdesktop.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DailyWeatherSummaryResponse(
    String date,
    BigDecimal minTemperature,
    BigDecimal maxTemperature,
    BigDecimal avgTemperature,
    BigDecimal minHumidity,
    BigDecimal maxHumidity,
    BigDecimal avgHumidity,
    BigDecimal minWindSpeed,
    BigDecimal maxWindSpeed,
    BigDecimal avgWindSpeed,
    BigDecimal minDust,
    BigDecimal maxDust,
    BigDecimal avgDust,
    BigDecimal totalRainfall
) {
}
