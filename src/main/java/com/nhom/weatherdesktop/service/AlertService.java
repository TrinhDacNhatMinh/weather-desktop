package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhom.weatherdesktop.config.AppConfig;
import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;

import java.net.http.HttpResponse;
import java.util.List;

public class AlertService implements IAlertService {
    
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final String baseUrl;
    
    public AlertService() {
        this.baseUrl = AppConfig.getApiBaseUrl() + "/alerts";
    }
    
    /**
     * Get paginated alerts of current user
     */
    public PageResponse<AlertResponse> getMyAlerts(int page, int size) throws Exception {
        String url = baseUrl + "/me?page=" + page + "&size=" + size;
        
        var httpRequest = HttpRequestBuilder
                .createWithFullUrl(url)
                .withAuth()
                .get()
                .build();
        
        HttpResponse<String> response = HttpRequestBuilder.sendWithRefresh(httpRequest);
        
        if (response.statusCode() == 200) {
            return MAPPER.readValue(
                response.body(),
                MAPPER.getTypeFactory().constructParametricType(
                    PageResponse.class,
                    AlertResponse.class
                )
            );
        } else {
            throw new RuntimeException("Failed to fetch alerts: " + response.statusCode());
        }
    }
    
    /**
     * Get all alerts of current user (multiple pages if needed)
     */
    public List<AlertResponse> getAllMyAlerts() throws Exception {
        PageResponse<AlertResponse> firstPage = getMyAlerts(0, 100);
        return firstPage.content();
    }
    
    /**
     * Mark an alert as seen
     */
    public AlertResponse markAlertAsSeen(Long alertId) throws Exception {
        String url = baseUrl + "/" + alertId + "/seen";
        
        var httpRequest = HttpRequestBuilder
                .createWithFullUrl(url)
                .withAuth()
                .patch("")
                .build();
        
        HttpResponse<String> response = HttpRequestBuilder.sendWithRefresh(httpRequest);
        
        if (response.statusCode() == 200) {
            return MAPPER.readValue(response.body(), AlertResponse.class);
        } else {
            throw new RuntimeException("Failed to mark alert as seen: " + response.statusCode());
        }
    }
    
    /**
     * Delete an alert by ID
     */
    public void deleteAlert(Long alertId) throws Exception {
        String url = baseUrl + "/" + alertId;
        
        var httpRequest = HttpRequestBuilder
                .createWithFullUrl(url)
                .withAuth()
                .delete()
                .build();
        
        HttpResponse<String> response = HttpRequestBuilder.sendWithRefresh(httpRequest);
        
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new RuntimeException("Failed to delete alert: " + response.statusCode());
        }
    }
}
