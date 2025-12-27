package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class WeatherCardController {
    
    @FXML
    private Text temperatureValue;
    
    @FXML
    private Text humidityValue;
    
    @FXML
    private Text windSpeedValue;
    
    @FXML
    private Text rainfallValue;
    
    @FXML
    private Text dustValue;
    
    @FXML
    public void initialize() {
        // Data already set in FXML for demo
    }
    
    public void updateWeatherData(double temp, double humidity, double windSpeed, double rainfall, double dust) {
        temperatureValue.setText(String.format("%.1f", temp));
        humidityValue.setText(String.format("%.0f", humidity));
        windSpeedValue.setText(String.format("%.1f", windSpeed));
        rainfallValue.setText(String.format("%.1f", rainfall));
        dustValue.setText(String.format("%.0f", dust));
    }
}
