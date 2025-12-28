package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class SettingsScreenController {
    
    @FXML
    private ComboBox<String> temperatureUnitCombo;
    
    @FXML
    private ComboBox<String> windSpeedUnitCombo;
    
    @FXML
    private ComboBox<String> themeCombo;
    
    @FXML
    private ComboBox<String> languageCombo;
    
    @FXML
    public void initialize() {
        // Temperature Unit
        temperatureUnitCombo.getItems().addAll(
            "°C",
            "°F"
        );
        temperatureUnitCombo.setValue("°C");
        
        // Wind Speed Unit
        windSpeedUnitCombo.getItems().addAll(
            "km/h",
            "m/s"
        );
        windSpeedUnitCombo.setValue("km/h");
        
        // Theme
        themeCombo.getItems().addAll(
            "Light",
            "Dark"
        );
        themeCombo.setValue("Light");
        
        // Language
        languageCombo.getItems().addAll(
            "English",
            "Tiếng Việt"
        );
        languageCombo.setValue("English");
    }
}
