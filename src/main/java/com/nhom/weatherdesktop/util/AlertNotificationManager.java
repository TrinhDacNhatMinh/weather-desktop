package com.nhom.weatherdesktop.util;

import com.nhom.weatherdesktop.controller.AlertNotificationDialogController;
import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.session.SessionContext;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton manager for alert notification dialogs
 * Prevents spam and manages dialog display
 */
public class AlertNotificationManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationManager.class);
    private static AlertNotificationManager instance;
    
    private boolean isDialogShowing = false;
    private String currentScreen = "My Station"; // Default
    private final StationService stationService;
    private Runnable onDisableCallback;
    private Runnable onEnableCallback;
    private Runnable onUIUpdateCallback;
    
    private AlertNotificationManager() {
        this.stationService = new StationService();
    }
    
    public static AlertNotificationManager getInstance() {
        if (instance == null) {
            instance = new AlertNotificationManager();
        }
        return instance;
    }
    
    public void setCurrentScreen(String screenName) {
        this.currentScreen = screenName;
        logger.debug("Current screen updated to: {}", screenName);
    }
    
    public void setOnDisableCallback(Runnable callback) {
        this.onDisableCallback = callback;
    }
    
    public Runnable getOnDisableCallback() {
        return onDisableCallback;
    }
    
    public void setOnEnableCallback(Runnable callback) {
        this.onEnableCallback = callback;
    }
    
    public Runnable getOnEnableCallback() {
        return onEnableCallback;
    }
    
    public void setOnUIUpdateCallback(Runnable callback) {
        this.onUIUpdateCallback = callback;
    }
    
    public Runnable getOnUIUpdateCallback() {
        return onUIUpdateCallback;
    }
    
    public void showAlert(AlertResponse alert) {
        // Check if alerts are enabled
        if (!SessionContext.areAlertsEnabled()) {
            logger.debug("Alerts disabled, not showing dialog");
            return;
        }
        
        // Don't show on Alert screen
        if ("Alerts".equalsIgnoreCase(currentScreen)) {
            logger.debug("On Alert screen, not showing dialog");
            return;
        }
        
        // Prevent spam - only 1 dialog at a time
        if (isDialogShowing) {
            logger.debug("Dialog already showing, ignoring new alert");
            return;
        }
        
        Platform.runLater(() -> {
            try {
                isDialogShowing = true;
                
                // Load dialog FXML
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dialogs/alert_notification_dialog.fxml")
                );
                VBox dialogRoot = loader.load();
                
                // Get controller
                AlertNotificationDialogController controller = loader.getController();
                
                // Create stage
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.NONE); // Non-blocking
                dialogStage.initStyle(StageStyle.UTILITY);
                dialogStage.setTitle("New Alert");
                dialogStage.setResizable(false);
                dialogStage.setScene(new Scene(dialogRoot));
                
                // Set controller data
                controller.setDialogStage(dialogStage);
                
                // Get station name
                String stationName = getStationName(alert.stationId());
                controller.setAlertData(alert, stationName);
                controller.setOnDisableCallback(onDisableCallback);
                
                // Reset flag when dialog closes
                dialogStage.setOnHidden(event -> {
                    isDialogShowing = false;
                    logger.debug("Alert dialog closed");
                });
                
                // Show dialog
                dialogStage.show();
                logger.info("Showing alert dialog for station: {}", stationName);
                
            } catch (Exception e) {
                logger.error("Failed to show alert dialog: {}", e.getMessage(), e);
                isDialogShowing = false;
            }
        });
    }
    
    private String getStationName(Long stationId) {
        try {
            // Get stations and find the one with matching ID
            var response = stationService.getMyStations(0, 100);
            return response.content().stream()
                .filter(s -> s.id().equals(stationId))
                .findFirst()
                .map(StationResponse::name)
                .orElse("Station #" + stationId);
        } catch (Exception e) {
            logger.error("Failed to fetch station name: {}", e.getMessage());
            return "Station #" + stationId;
        }
    }
}
