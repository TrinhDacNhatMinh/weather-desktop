package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.dto.request.AddStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.interfaces.IStationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class StationService implements IStationService {

    private static final Logger logger = LoggerFactory.getLogger(StationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private final HttpClientService httpClient;

    public StationService() {
        this.httpClient = HttpClientService.getInstance();
    }

    @Override
    public PageResponse<StationResponse> getMyStations(int page, int size) {
        try {
            String endpoint = "/stations/user/me/stations?page=" + page + "&size=" + size;
            logger.debug("Calling endpoint: {}", endpoint);
            
            // API returns { "data": [ StationResponse, ... ] }
            com.nhom.weatherdesktop.dto.response.ApiResponse<List<StationResponse>> response = 
                httpClient.get(
                    endpoint,
                    new TypeReference<com.nhom.weatherdesktop.dto.response.ApiResponse<List<StationResponse>>>() {},
                    true  // requiresAuth = true
                );
            
            logger.debug("Got response, extracting data...");
            List<StationResponse> stations = response.data();
            logger.info("Successfully fetched {} stations", stations.size());
            
            // Wrap in PageResponse for interface compatibility
            PageResponse<StationResponse> result = new PageResponse<>(
                stations,
                page,
                size,
                stations.size(),
                1,
                true
            );
            
            return result;
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP error while fetching stations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stations: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Network error while fetching stations: {}", e.getMessage(), e);
            throw new RuntimeException("Network error while fetching stations: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching stations: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public StationResponse getStationById(Long id) {
        return null;
    }

    @Override
    public StationResponse addStationToUser(AddStationRequest request) {
        return null;
    }

    @Override
    public StationResponse updateStation(Long id, UpdateStationRequest request) {
        return null;
    }

    @Override
    public StationResponse updateStationSharing(Long id) {
        return null;
    }

    @Override
    public void detachStationFromUser(Long id) {

    }

}
