package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.request.AddStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;

/**
 * Interface for Station-related operations
 * Enables loose coupling and easier testing with mock implementations
 */
public interface IStationService {
    
    /**
     * Get paginated list of user's stations
     */
    PageResponse<StationResponse> getMyStations(int page, int size);
    
    /**
     * Add/attach a station to current user
     */
    StationResponse addStationToUser(AddStationRequest request);
    
    /**
     * Update station information
     */
    StationResponse updateStation(Long id, UpdateStationRequest request);
    
    /**
     * Toggle station public/private sharing status
     */
    StationResponse updateStationSharing(Long id);
    
    /**
     * Get station by ID
     */
    StationResponse getStationById(Long id);
    
    /**
     * Detach station from current user
     */
    void detachStationFromUser(Long id);
}
