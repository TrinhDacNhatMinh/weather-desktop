package com.nhom.weatherdesktop.ui.manager;

import com.nhom.weatherdesktop.dto.response.WeatherDataResponse;
import javafx.scene.control.Label;

/**
 * Manages weather data display on the UI
 * Encapsulates all weather-related labels and their update logic
 */
public class WeatherDisplayManager {
    
    private final Label temperatureValue;
    private final Label humidityValue;
    private final Label windspeedValue;
    private final Label rainfallValue;
    private final Label dustValue;
    
    public WeatherDisplayManager(
        Label temperatureValue,
        Label humidityValue,
        Label windspeedValue,
        Label rainfallValue,
        Label dustValue
    ) {
        this.temperatureValue = temperatureValue;
        this.humidityValue = humidityValue;
        this.windspeedValue = windspeedValue;
        this.rainfallValue = rainfallValue;
        this.dustValue = dustValue;
    }
    
    /**
     * Update weather data display from WebSocket response
     */
    public void updateWeatherData(WeatherDataResponse data) {
        if (data.temperature() != null) {
            temperatureValue.setText(String.format("%.1f", data.temperature()));
        }
        if (data.humidity() != null) {
            humidityValue.setText(String.format("%.0f", data.humidity()));
        }
        if (data.windSpeed() != null) {
            windspeedValue.setText(String.format("%.1f", data.windSpeed()));
        }
        if (data.rainfall() != null) {
            rainfallValue.setText(String.format("%.1f", data.rainfall()));
        }
        if (data.dust() != null) {
            dustValue.setText(String.format("%.1f", data.dust()));
        }
    }
    
    /**
     * Reset all weather values to default "--"
     */
    public void reset() {
        temperatureValue.setText("--");
        humidityValue.setText("--");
        windspeedValue.setText("--");
        rainfallValue.setText("--");
        dustValue.setText("--");
    }
}
