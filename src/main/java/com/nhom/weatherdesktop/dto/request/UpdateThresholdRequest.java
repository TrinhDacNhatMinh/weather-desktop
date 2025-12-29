package com.nhom.weatherdesktop.dto.request;


public record UpdateThresholdRequest(
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
        Boolean dustActive
) {
}
