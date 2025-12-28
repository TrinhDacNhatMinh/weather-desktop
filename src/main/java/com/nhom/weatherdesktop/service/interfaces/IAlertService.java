package com.nhom.weatherdesktop.service.interfaces;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;

import java.util.List;

public interface IAlertService {

    /**
     * Get paginated alerts for current user
     */
    PageResponse<AlertResponse> getMyAlerts(int page, int size);

    /**
     * Get all alerts for current user (multiple pages if needed)
     */
    List<AlertResponse> getAllMyAlerts();

    /**
     * Mark an alert as seen
     */
    AlertResponse markAlertAsSeen(Long alertId);

    /**
     * Delete an alert by ID
     */
    void deleteAlert(Long alertId);

    /**
     * Delete all alerts of current user
     */
    void deleteAllMyAlerts();

}
