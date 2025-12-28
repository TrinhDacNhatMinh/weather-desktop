package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.util.TimeUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AlertItemController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertItemController.class);
    
    @FXML
    private HBox root;
    
    @FXML
    private Text stationNameText;
    
    @FXML
    private Text messageText;
    
    @FXML
    private Text timeAgoText;
    
    private AlertResponse alert;
    private Runnable onDialogClosed;
    
    public void setAlert(AlertResponse alert) {
        this.alert = alert;
        
        // Set station name
        stationNameText.setText(alert.stationName());
        
        // Set message - bold if status is NEW
        messageText.setText(alert.message());
        if ("NEW".equals(alert.status())) {
            messageText.setStyle("-fx-font-weight: bold;");
        }
        
        // Set time ago
        String timeAgo = TimeUtil.getTimeAgo(alert.createdAt());
        timeAgoText.setText(timeAgo);
        
        // Add click handler
        root.setOnMouseClicked(event -> handleAlertClick());
        root.setStyle(root.getStyle() + "; -fx-cursor: hand;");
        
        logger.debug("Alert item loaded: station={}, status={}, time={}",
            alert.stationName(), alert.status(), timeAgo);
    }
    
    private void handleAlertClick() {
        try {
            logger.info("Alert item clicked: {}", alert.id());
            
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dialogs/alert_detail_dialog.fxml")
            );
            VBox dialogRoot = loader.load();
            
            AlertDetailDialogController controller = loader.getController();
            
            // Create modal dialog
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Alert Details");
            dialogStage.setResizable(true);
            
            Scene scene = new Scene(dialogRoot);
            dialogStage.setScene(scene);
            
            // Pass data to controller
            controller.setDialogStage(dialogStage);
            controller.setAlert(alert);
            
            // Show dialog and wait for it to close
            dialogStage.showAndWait();
            
            // After dialog closes, trigger refresh if callback is set
            if (onDialogClosed != null) {
                onDialogClosed.run();
            }
            
        } catch (IOException e) {
            logger.error("Failed to open alert detail dialog: {}", e.getMessage(), e);
        }
    }
    
    public void setOnDialogClosed(Runnable callback) {
        this.onDialogClosed = callback;
    }
    
    public AlertResponse getAlert() {
        return alert;
    }
}
