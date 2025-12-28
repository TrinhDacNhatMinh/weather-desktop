package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class AlertNotificationDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationDialogController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @FXML
    private Text stationNameText;
    
    @FXML
    private Text messageText;
    
    @FXML
    private Text createdAtText;
    
    private Stage dialogStage;
    private AlertResponse alert;
    private Runnable onDisableCallback;
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setAlertData(AlertResponse alert, String stationName) {
        this.alert = alert;
        stationNameText.setText(stationName);
        messageText.setText(alert.message());
        
        // Convert Instant to LocalDateTime for formatting
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(
            alert.createdAt(), 
            java.time.ZoneId.systemDefault()
        );
        createdAtText.setText(dateTime.format(DATE_FORMATTER));
    }
    
    public void setOnDisableCallback(Runnable callback) {
        this.onDisableCallback = callback;
    }
    
    @FXML
    private void handleDisableAlert() {
        logger.info("User disabled alerts from dialog");
        SessionContext.setAlertsEnabled(false);
        
        if (onDisableCallback != null) {
            onDisableCallback.run();
        }
        
        handleClose();
    }
    
    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
