package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.session.SessionContext;
import com.nhom.weatherdesktop.util.AlertNotificationManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsScreenController {
    
    private static final Logger logger = LoggerFactory.getLogger(SettingsScreenController.class);
    
    @FXML
    private ComboBox<String> temperatureUnitCombo;
    
    @FXML
    private ComboBox<String> windSpeedUnitCombo;
    
    @FXML
    private ComboBox<String> themeCombo;
    
    @FXML
    private ComboBox<String> languageCombo;
    
    @FXML
    private CheckBox alertNotificationsCheckbox;
    
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
            "m/s",
            "km/h"
        );
        windSpeedUnitCombo.setValue("m/s");
        
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
        
        // Initialize alert notifications checkbox from SessionContext
        alertNotificationsCheckbox.setSelected(SessionContext.areAlertsEnabled());
        
        // Register callback to update checkbox when disabled from notification dialog
        AlertNotificationManager.getInstance().setOnUIUpdateCallback(() -> {
            javafx.application.Platform.runLater(() -> {
                alertNotificationsCheckbox.setSelected(SessionContext.areAlertsEnabled());
                logger.debug("Checkbox updated from external trigger: {}", SessionContext.areAlertsEnabled());
            });
        });
        
        logger.info("Settings initialized - Alert notifications: {}", 
                   SessionContext.areAlertsEnabled() ? "Enabled" : "Disabled");
    }
    
    @FXML
    private void handleAlertNotificationsToggle() {
        boolean enabled = alertNotificationsCheckbox.isSelected();
        SessionContext.setAlertsEnabled(enabled);
        
        if (!enabled) {
            logger.info("Alerts disabled - unsubscribing from all topics");
            Runnable unsubscribeCallback = AlertNotificationManager.getInstance().getOnDisableCallback();
            if (unsubscribeCallback != null) {
                unsubscribeCallback.run();
            }
        } else {
            logger.info("Alerts enabled - resubscribing to all topics");
            Runnable enableCallback = AlertNotificationManager.getInstance().getOnEnableCallback();
            if (enableCallback != null) {
                enableCallback.run();
            }
        }
    }
}
