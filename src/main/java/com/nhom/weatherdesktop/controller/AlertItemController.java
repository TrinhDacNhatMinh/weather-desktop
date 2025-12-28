package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.util.TimeUtil;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        logger.debug("Alert item loaded: station={}, status={}, time={}",
            alert.stationName(), alert.status(), timeAgo);
    }
    
    public AlertResponse getAlert() {
        return alert;
    }
}
