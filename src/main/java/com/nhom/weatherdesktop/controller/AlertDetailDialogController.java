package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.service.AlertService;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AlertDetailDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertDetailDialogController.class);
    private static final DateTimeFormatter FULL_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @FXML
    private Text stationNameText;
    
    @FXML
    private Text fullTimestampText;
    
    @FXML
    private Text messageText;
    
    private AlertResponse alert;
    private final AlertService alertService;
    private Stage dialogStage;
    
    public AlertDetailDialogController() {
        this.alertService = new AlertService();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setAlert(AlertResponse alert) {
        this.alert = alert;
        populateDialog();
        markAsSeen();
    }
    
    private void populateDialog() {
        if (alert == null) return;
        
        // Set station name
        stationNameText.setText(alert.stationName());
        
        // Set full timestamp - convert from API's VN time to display format
        String fullTime = alert.createdAt()
            .atZone(ZoneId.of("UTC"))
            .withZoneSameLocal(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(FULL_TIME_FORMATTER);
        fullTimestampText.setText(fullTime);
        
        // Set message
        messageText.setText(alert.message());
        
        logger.debug("Alert dialog populated for alert ID: {}", alert.id());
    }
    
    private void markAsSeen() {
        if (alert == null || alert.id() == null) return;
        
        try {
            alertService.markAlertAsSeen(alert.id());
            logger.info("Alert {} marked as seen", alert.id());
        } catch (Exception e) {
            logger.error("Failed to mark alert as seen: {}", e.getMessage(), e);
            // Continue anyway - don't block user from viewing the alert
        }
    }
    
    @FXML
    private void handleDelete() {
        if (alert == null || alert.id() == null) return;
        
        try {
            logger.info("Deleting alert: {}", alert.id());
            alertService.deleteAlert(alert.id());
            logger.info("Alert {} deleted successfully", alert.id());
            
            // Close dialog after successful delete
            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (Exception e) {
            logger.error("Failed to delete alert: {}", e.getMessage(), e);
        }
    }
    
    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
