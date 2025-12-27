package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.function.Consumer;

public class SidebarController {
    
    @FXML
    private Button myStationBtn;
    
    @FXML
    private Button alertsBtn;
    
    @FXML
    private Button settingsBtn;
    
    private Consumer<String> onNavigate;
    
    @FXML
    public void initialize() {
        // Default: My Station is active
        setActiveButton(myStationBtn);
    }
    
    public void setOnNavigate(Consumer<String> callback) {
        this.onNavigate = callback;
    }
    
    @FXML
    private void handleMyStation() {
        setActiveButton(myStationBtn);
        if (onNavigate != null) {
            onNavigate.accept("My Station");
        }
    }
    
    @FXML
    private void handleAlerts() {
        setActiveButton(alertsBtn);
        if (onNavigate != null) {
            onNavigate.accept("Alerts");
        }
    }
    
    @FXML
    private void handleSettings() {
        setActiveButton(settingsBtn);
        if (onNavigate != null) {
            onNavigate.accept("Settings");
        }
    }
    
    private void setActiveButton(Button activeBtn) {
        // Remove active class from all buttons
        myStationBtn.getStyleClass().remove("active");
        alertsBtn.getStyleClass().remove("active");
        settingsBtn.getStyleClass().remove("active");
        
        // Add active class to selected button
        activeBtn.getStyleClass().add("active");
    }
}
