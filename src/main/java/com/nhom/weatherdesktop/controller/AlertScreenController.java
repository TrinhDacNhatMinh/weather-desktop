package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.AlertService;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.util.AppConfig;
import com.nhom.weatherdesktop.websocket.StompClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AlertScreenController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertScreenController.class);
    
    @FXML
    private VBox alertListContainer;
    
    @FXML
    private StackPane emptyState;
    
    private AlertService alertService;
    private SidebarController sidebarController;
    
    // WebSocket
    private static StompClient stompClient;
    private StationService stationService;
    
    @FXML
    public void initialize() {
        this.alertService = new AlertService();
        this.stationService = new StationService();
        
        // Initialize WebSocket if not already done
        initializeWebSocket();
        
        // Subscribe to alert topics
        subscribeToAlerts();
        
        loadAlerts();
    }
    
    private void initializeWebSocket() {
        if (stompClient == null) {
            stompClient = new StompClient();
            stompClient.setAlertHandler(this::handleNewAlert);
            
            AppConfig config = AppConfig.getInstance();
            String wsUrl = config.getWebSocketUrl();
            stompClient.connect(wsUrl);
            
            logger.info("WebSocket initialized for alerts");
        }
    }
    
    private void subscribeToAlerts() {
        // Load user stations and subscribe to their alert topics
        new Thread(() -> {
            try {
                // Wait for WebSocket connection
                int retries = 30;
                while (!stompClient.isConnected() && retries > 0) {
                    Thread.sleep(100);
                    retries--;
                }
                
                if (!stompClient.isConnected()) {
                    logger.warn("WebSocket not connected, cannot subscribe to alerts");
                    return;
                }
                
                // Get user stations
                PageResponse<StationResponse> response = stationService.getMyStations(0, 100);
                
                Platform.runLater(() -> {
                    // Subscribe to alert topic for each station
                    for (StationResponse station : response.content()) {
                        String alertTopic = "/topic/stations/" + station.id() + "/alerts";
                        stompClient.subscribe(alertTopic, "alert");
                        logger.debug("Subscribed to alert topic: {}", alertTopic);
                    }
                    logger.info("Subscribed to {} station alert topics", response.content().size());
                });
                
            } catch (Exception e) {
                logger.error("Failed to subscribe to alert topics: {}", e.getMessage(), e);
            }
        }).start();
    }
    
    private void handleNewAlert(AlertResponse alert) {
        logger.info("Received new alert for station {}: {}", alert.stationId(), alert.message());
        
        // Reload alerts list to show new alert
        Platform.runLater(this::loadAlerts);
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
                                
                                // Set callback to refresh alerts when dialog closes
                                controller.setOnDialogClosed(this::loadAlerts);
                                
                                alertListContainer.getChildren().add(alertItem);
                            } catch (IOException e) {
                                logger.error("Failed to load alert item: {}", e.getMessage(), e);
                            }
                        }
                        
                        logger.info("Loaded {} alerts successfully", response.content().size());
                        
                        // Update sidebar icon after loading alerts
                        updateSidebarIcon();
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
    
    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }
    
    @FXML
    private void handleRefresh() {
        logger.info("Refreshing alerts...");
        loadAlerts();
    }
    
    @FXML
    private void handleDeleteAll() {
        logger.info("Delete all alerts requested");
        
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.WARNING
        );
        confirmAlert.setTitle("Confirm Delete All");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete ALL alerts? This action cannot be undone.");
        
        confirmAlert.getButtonTypes().setAll(
            javafx.scene.control.ButtonType.OK,
            javafx.scene.control.ButtonType.CANCEL
        );
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // User confirmed, delete all alerts
                new Thread(() -> {
                    try {
                        logger.debug("Deleting all alerts...");
                        alertService.deleteAllMyAlerts();
                        
                        Platform.runLater(() -> {
                            logger.info("All alerts deleted successfully!");
                            
                            // Reload alerts (will show empty state)
                            loadAlerts();
                            
                            // Update sidebar icon
                            updateSidebarIcon();
                            
                            // Show success message
                            javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION
                            );
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("All alerts deleted successfully!");
                            successAlert.show();
                        });
                        
                    } catch (Exception e) {
                        logger.error("Failed to delete all alerts: {}", e.getMessage(), e);
                        Platform.runLater(() -> {
                            javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR
                            );
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Failed to delete alerts: " + e.getMessage());
                            errorAlert.showAndWait();
                        });
                    }
                }).start();
            } else {
                logger.debug("Delete all alerts cancelled by user");
            }
        });
    }
    
    private void updateSidebarIcon() {
        if (sidebarController != null) {
            sidebarController.updateAlertIcon();
        }
    }
}
