package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.ApiResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.service.interfaces.IAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class AlertService implements IAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private final HttpClientService httpClient;
    
    public AlertService() {
        this.httpClient = HttpClientService.getInstance();
    }

    @Override
    public PageResponse<AlertResponse> getMyAlerts(int page, int size) {
        try {
            String endpoint = "/alerts/me?page=" + page + "&size=" + size;
            logger.debug("Fetching alerts: page={}, size={}", page, size);
            
            // API returns data as array directly, not PageResponse
            ApiResponse<List<AlertResponse>> response = httpClient.get(
                endpoint,
                new TypeReference<ApiResponse<List<AlertResponse>>>() {},
                true  // requiresAuth = true
            );
            
            List<AlertResponse> alerts = response.data();
            logger.info("Successfully fetched {} alerts (page {})", alerts.size(), page);
            
            // Create PageResponse manually since API returns array
            PageResponse<AlertResponse> pageResponse = new PageResponse<>(
                alerts,
                page,
                size,
                alerts.size(),
                1,
                true
            );
            
            return pageResponse;
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP error while fetching alerts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch alerts: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Network error while fetching alerts: {}", e.getMessage(), e);
            throw new RuntimeException("Network error while fetching alerts: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching alerts: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AlertResponse> getAllMyAlerts() {
        return List.of();
    }

    @Override
    public AlertResponse markAlertAsSeen(Long alertId) {
        return null;
    }

    @Override
    public void deleteAlert(Long alertId) {

    }

    @Override
    public void deleteAllMyAlerts() {

    }

}
