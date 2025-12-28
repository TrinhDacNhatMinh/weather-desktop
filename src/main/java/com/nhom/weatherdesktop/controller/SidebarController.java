package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.service.AlertService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SidebarController {
    
    private static final Logger logger = LoggerFactory.getLogger(SidebarController.class);
    
    @FXML
    private Button myStationBtn;
    
    @FXML
    private Button alertsBtn;
    
    @FXML
    private Button settingsBtn;
    
    @FXML
    private ImageView alertsIconView;
    
    private Consumer<String> onNavigate;
    private AlertService alertService;
    
    @FXML
    public void initialize() {
        // Initialize AlertService
        this.alertService = new AlertService();
        
        // Default: My Station is active
        setActiveButton(myStationBtn);
        
        // Update alert icon based on status
        updateAlertIcon();
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
    
    /**
     * Update alert icon based on whether there are NEW alerts
     * Call this method whenever alerts might have changed (after viewing, deleting, etc.)
     */
    public void updateAlertIcon() {
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                boolean hasNew = alertService.hasNewAlerts();
                
                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    String iconPath = hasNew 
                        ? "/icons/sidebar/notifications_unread.png"
                        : "/icons/sidebar/notifications.png";
                    
                    try {
                        Image newIcon = new Image(getClass().getResourceAsStream(iconPath));
                        alertsIconView.setImage(newIcon);
                        logger.debug("Updated alert icon: hasNew={}, icon={}", hasNew, iconPath);
                    } catch (Exception e) {
                        logger.error("Failed to load alert icon: {}", iconPath, e);
                    }
                });
                
            } catch (Exception e) {
                logger.error("Failed to update alert icon: {}", e.getMessage(), e);
            }
        }).start();
    }
}
