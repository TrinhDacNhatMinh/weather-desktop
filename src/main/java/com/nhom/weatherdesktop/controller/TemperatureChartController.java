package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

public class TemperatureChartController {
    
    @FXML
    private LineChart<String, Number> temperatureChart;
    
    @FXML
    public void initialize() {
        // Populate with dummy data
        populateDummyData();
    }
    
    private void populateDummyData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Temperature");
        
        // Add 24 hours of dummy temperature data
        String[] hours = {"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", 
                         "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"};
        double[] temps = {18, 17, 16, 18, 21, 24, 27, 29, 28, 25, 22, 19};
        
        for (int i = 0; i < hours.length; i++) {
            series.getData().add(new XYChart.Data<>(hours[i], temps[i]));
        }
        
        temperatureChart.getData().add(series);
    }
}
