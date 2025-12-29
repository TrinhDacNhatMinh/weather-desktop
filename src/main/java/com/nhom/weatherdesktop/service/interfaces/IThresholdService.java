package com.nhom.weatherdesktop.service.interfaces;

import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;

public interface IThresholdService {

    /**
     * Get threshold configuration for a station
     */
    ThresholdResponse getThresholdByStationId(Long stationId);

    /**
     * Update threshold configuration
     */
    ThresholdResponse updateThreshold(Long id, UpdateThresholdRequest request);

}
