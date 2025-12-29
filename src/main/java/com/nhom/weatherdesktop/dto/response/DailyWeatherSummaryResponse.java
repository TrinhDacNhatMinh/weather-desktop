package com.nhom.weatherdesktop.dto.response;

public record DailyWeatherSummaryResponse(
        String date,
        Float minTemperature,
        Float maxTemperature,
        Float avgTemperature,
        Float minHumidity,
        Float maxHumidity,
        Float avgHumidity,
        Float minWindSpeed,
        Float maxWindSpeed,
        Float avgWindSpeed,
        Float minDust,
        Float maxDust,
        Float avgDust,
        Float totalRainfall
) {
}
