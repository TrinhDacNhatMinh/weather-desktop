package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.response.DailyWeatherSummaryResponse;
import java.util.List;

/**
 * Interface for Weather Data related operations
 */
public interface IWeatherDataService {
    
    /**
     * Get daily weather summary for a station
     * @param stationId Station ID
     * @param days Number of days (default 7)
     * @return List of daily summaries
     */
    List<DailyWeatherSummaryResponse> getDailySummary(Long stationId, int days) throws Exception;
}
