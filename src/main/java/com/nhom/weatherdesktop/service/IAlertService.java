package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;

import java.util.List;

/**
 * Interface for Alert-related operations
 * Enables loose coupling and easier testing with mock implementations
 */
public interface IAlertService {
    
    /**
     * Get paginated alerts for current user
     */
    PageResponse<AlertResponse> getMyAlerts(int page, int size) throws Exception;
    
    /**
     * Get all alerts for current user (multiple pages if needed)
     */
    List<AlertResponse> getAllMyAlerts() throws Exception;
}
