package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.service.interfaces.IThresholdService;

public class ThresholdService implements IThresholdService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ThresholdService.class);
    private final HttpClientService httpClient;

    public ThresholdService() {
        this.httpClient = HttpClientService.getInstance();
    }

    @Override
    public ThresholdResponse getThresholdByStationId(Long stationId) {
        try {
            logger.debug("Fetching threshold for station id: {}", stationId);
            // Call GET /api/stations/{stationId}/threshold
            ThresholdResponse response = httpClient.get(
                "/stations/" + stationId + "/threshold",
                new com.fasterxml.jackson.core.type.TypeReference<ThresholdResponse>() {},
                true
            );
            logger.info("Successfully fetched threshold for station {}", stationId);
            return response;
        } catch (Exception e) {
            logger.error("Failed to fetch threshold: {}", e.getMessage());
            // Return null or throw exception depending on requirements
            // For now, return null to handle gracefully in UI
            return null;
        }
    }

    @Override
    public ThresholdResponse updateThreshold(Long id, UpdateThresholdRequest request) {
        try {
            logger.debug("Updating threshold id: {}", id);
            ThresholdResponse response = httpClient.put(
                "/thresholds/" + id,
                request,
                ThresholdResponse.class,
                true
            );
            logger.info("Successfully updated threshold id: {}", id);
            return response;
        } catch (Exception e) {
            logger.error("Failed to update threshold: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update threshold: " + e.getMessage(), e);
        }
    }

}
