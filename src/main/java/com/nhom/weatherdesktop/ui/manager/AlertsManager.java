package com.nhom.weatherdesktop.ui.manager;

import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.model.Alert;
import com.nhom.weatherdesktop.service.IAlertService;
import com.nhom.weatherdesktop.ui.component.AlertCard;
import com.nhom.weatherdesktop.util.AsyncTaskRunner;
import com.nhom.weatherdesktop.util.ErrorHandler;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages alerts loading and display
 */
public class AlertsManager {
    
    private final VBox alertsListContainer;
    private final Label unreadCountLabel;
    private final VBox emptyAlertsState;
    private final IAlertService alertService;
    
    private Consumer<String> errorHandler;
    
    public AlertsManager(
        VBox alertsListContainer,
        Label unreadCountLabel,
        VBox emptyAlertsState,
        IAlertService alertService
    ) {
        this.alertsListContainer = alertsListContainer;
        this.unreadCountLabel = unreadCountLabel;
        this.emptyAlertsState = emptyAlertsState;
        this.alertService = alertService;
    }
    
    public void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    /**
     * Load alerts from API asynchronously
     */
    public void loadAlerts() {
        AsyncTaskRunner.runAsync(
            // Background task
            () -> {
                try {
                    List<AlertResponse> alertResponses = alertService.getAllMyAlerts();
                    return alertResponses.stream()
                            .map(Alert::fromResponse)
                            .toList();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load alerts: " + e.getMessage(), e);
                }
            },
            // On success
            this::displayAlerts,
            // On error
            e -> {
                if (errorHandler != null) {
                    errorHandler.accept(ErrorHandler.getUserMessage(e));
                }
            }
        );
    }
    
    /**
     * Display list of alerts in UI
     */
    public void displayAlerts(List<Alert> alerts) {
        alertsListContainer.getChildren().clear();
        
        if (alerts == null || alerts.isEmpty()) {
            emptyAlertsState.setVisible(true);
            unreadCountLabel.setVisible(false);
            return;
        }
        
        emptyAlertsState.setVisible(false);
        
        // Count unread alerts
        long unreadCount = alerts.stream().filter(Alert::isNew).count();
        if (unreadCount > 0) {
            unreadCountLabel.setText(String.valueOf(unreadCount));
            unreadCountLabel.setVisible(true);
        } else {
            unreadCountLabel.setVisible(false);
        }
        
        // Display alerts
        for (Alert alert : alerts) {
            AlertCard card = new AlertCard(alert, alertService, this::loadAlerts);
            alertsListContainer.getChildren().add(card);
        }
    }
}
