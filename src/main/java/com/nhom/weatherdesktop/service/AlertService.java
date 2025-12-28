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
            logger.error("HTTP {} error while fetching alerts: {}", e.getStatusCode(), e.getMessage(), e);
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
        try {
            String endpoint = "/alerts/" + alertId + "/seen";
            logger.debug("Marking alert {} as seen", alertId);
            
            ApiResponse<AlertResponse> response = httpClient.patch(
                endpoint,
                null, // No request body needed
                new TypeReference<ApiResponse<AlertResponse>>() {},
                true  // requiresAuth = true
            );
            
            AlertResponse updatedAlert = response.data();
            logger.info("Successfully marked alert {} as seen", alertId);
            return updatedAlert;
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP {} error while marking alert as seen: {}", e.getStatusCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to mark alert as seen: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Network error while marking alert as seen: {}", e.getMessage(), e);
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while marking alert as seen: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAlert(Long alertId) {
        try {
            String endpoint = "/alerts/" + alertId;
            logger.debug("Deleting alert {}", alertId);
            
            httpClient.delete(endpoint, true);
            
            logger.info("Successfully deleted alert {}", alertId);
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP {} error while deleting alert: {}", e.getStatusCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to delete alert: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Network error while deleting alert: {}", e.getMessage(), e);
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting alert: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllMyAlerts() {
        try {
            String endpoint = "/alerts/me";
            logger.debug("Deleting all alerts for current user");
            
            httpClient.delete(endpoint, true);
            
            logger.info("Successfully deleted all alerts");
            
        } catch (HttpClientService.HttpException e) {
            logger.error("HTTP {} error while deleting all alerts: {}", e.getStatusCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to delete all alerts: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Network error while deleting all alerts: {}", e.getMessage(), e);
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting all alerts: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Check if there are any NEW (unread) alerts
     * @return true if there are any alerts with status "NEW", false otherwise
     */
    public boolean hasNewAlerts() {
        try {
            String endpoint = "/alerts/me?page=0&size=100";
            logger.debug("Checking for new alerts...");
            
            ApiResponse<List<AlertResponse>> response = httpClient.get(
                endpoint,
                new TypeReference<ApiResponse<List<AlertResponse>>>() {},
                true  // requiresAuth = true
            );
            
            List<AlertResponse> alerts = response.data();
            
            // Check if any alert has status "NEW"
            boolean hasNew = alerts.stream()
                    .anyMatch(alert -> "NEW".equalsIgnoreCase(alert.status()));
            
            logger.debug("Has new alerts: {}", hasNew);
            return hasNew;
            
        } catch (Exception e) {
            logger.error("Failed to check new alerts: {}", e.getMessage(), e);
            // Return false on error to avoid showing unread icon when we can't verify
            return false;
        }
    }

}
