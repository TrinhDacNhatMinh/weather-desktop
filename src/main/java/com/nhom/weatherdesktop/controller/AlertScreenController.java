package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.service.AlertService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AlertScreenController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertScreenController.class);
    
    @FXML
    private VBox alertListContainer;
    
    @FXML
    private VBox emptyState;
    
    private AlertService alertService;
    
    @FXML
    public void initialize() {
        this.alertService = new AlertService();
        loadAlerts();
    }
    
    private void loadAlerts() {
        new Thread(() -> {
            try {
                logger.debug("Loading alerts...");
                PageResponse<AlertResponse> response = alertService.getMyAlerts(0, 100);
                
                Platform.runLater(() -> {
                    alertListContainer.getChildren().clear();
                    
                    if (response.content().isEmpty()) {
                        // Show empty state
                        emptyState.setVisible(true);
                        emptyState.setManaged(true);
                        alertListContainer.setVisible(false);
                        alertListContainer.setManaged(false);
                        logger.info("No alerts to display");
                    } else {
                        // Hide empty state and show alerts
                        emptyState.setVisible(false);
                        emptyState.setManaged(false);
                        alertListContainer.setVisible(true);
                        alertListContainer.setManaged(true);
                        
                        // Load alert items
                        for (AlertResponse alert : response.content()) {
                            try {
                                FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/fxml/components/alert_item.fxml")
                                );
                                HBox alertItem = loader.load();
                                AlertItemController controller = loader.getController();
                                controller.setAlert(alert);
                                alertListContainer.getChildren().add(alertItem);
                            } catch (IOException e) {
                                logger.error("Failed to load alert item: {}", e.getMessage(), e);
                            }
                        }
                        
                        logger.info("Loaded {} alerts successfully", response.content().size());
                    }
                });
                
            } catch (Exception e) {
                logger.error("Failed to load alerts: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    // Show error state
                    emptyState.setVisible(true);
                    emptyState.setManaged(true);
                    alertListContainer.setVisible(false);
                    alertListContainer.setManaged(false);
                });
            }
        }).start();
    }
    
    @FXML
    private void handleRefresh() {
        logger.info("Refreshing alerts...");
        loadAlerts();
    }
}
