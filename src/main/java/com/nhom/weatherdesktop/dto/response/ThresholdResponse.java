package com.nhom.weatherdesktop.dto.response;

public record ThresholdResponse(
        Long id,
        Float temperatureMin,
        Float temperatureMax,
        Float humidityMin,
        Float humidityMax,
        Float rainfallMax,
        Float windSpeedMax,
        Float dustMax,
        Boolean temperatureActive,
        Boolean humidityActive,
        Boolean rainfallActive,
        Boolean windSpeedActive,
        Boolean dustActive,
        Long stationId
) {
}
