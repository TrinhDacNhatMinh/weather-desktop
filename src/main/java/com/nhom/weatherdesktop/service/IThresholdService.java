package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;

/**
 * Interface for Threshold-related operations
 * Enables loose coupling and easier testing with mock implementations
 */
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
