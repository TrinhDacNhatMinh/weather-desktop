package com.nhom.weatherdesktop.ui.component;

import com.nhom.weatherdesktop.model.Alert;
import com.nhom.weatherdesktop.service.IAlertService;
import com.nhom.weatherdesktop.ui.controller.dialog.AlertDetailDialogController;
import com.nhom.weatherdesktop.util.AsyncTaskRunner;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.time.Duration;
import java.time.Instant;

import static com.nhom.weatherdesktop.config.TimezoneConfig.*;

public class AlertCard extends HBox {
    
    private final Alert alert;
    private final Label iconLabel;
    private final Label newBadge;
    private final IAlertService alertService;
    private final Runnable onAlertUpdated;
    
    public AlertCard(Alert alert, IAlertService alertService, Runnable onAlertUpdated) {
        this.alert = alert;
        this.alertService = alertService;
        this.onAlertUpdated = onAlertUpdated;
        
        // Style the card
        getStyleClass().add("alert-card");
        if (alert.isNew()) {
            getStyleClass().add("alert-unread");
        }
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);
        setPadding(new Insets(16, 20, 16, 20));
        
        // Warning icon
        iconLabel = new Label("⚠");
        iconLabel.getStyleClass().add("alert-icon");
        if (alert.isNew()) {
            iconLabel.getStyleClass().add("alert-icon-new");
        } else {
            iconLabel.getStyleClass().add("alert-icon-seen");
        }
        
        // Content container
        VBox content = new VBox(6);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        // Header with station name and badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label stationLabel = new Label(alert.getStationName());
        stationLabel.getStyleClass().add("alert-station");
        
        // NEW badge
        newBadge = new Label("NEW");
        newBadge.getStyleClass().add("alert-new-badge");
        newBadge.setVisible(alert.isNew());
        newBadge.setManaged(alert.isNew());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(stationLabel, newBadge);
        
        // Message
        Label messageLabel = new Label(alert.getMessage());
        messageLabel.getStyleClass().add("alert-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Timestamp
        Label timestampLabel = new Label(formatTimestamp(alert.getCreatedAt()));
        timestampLabel.getStyleClass().add("alert-timestamp");
        
        content.getChildren().addAll(header, messageLabel, timestampLabel);
        
        getChildren().addAll(iconLabel, content);
        
        // Click to show dialog and mark as seen
        setOnMouseClicked(event -> showDetailAndMarkAsSeen());
    }
    
    private String formatTimestamp(Instant instant) {
        // WORKAROUND: Server returns timestamp in +07:00 timezone but marks it as UTC (Z)
        // We need to ADD offset to get the actual local time for comparison
        Instant adjustedInstant = instant.plusSeconds(getServerTimezoneOffsetSeconds());
        
        Instant now = Instant.now();
        Duration duration = Duration.between(adjustedInstant, now);
        
        if (duration.isNegative()) {
            return "just now";
        }
        
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return seconds + (seconds == 1 ? " second ago" : " seconds ago");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }
    
    private void showDetailAndMarkAsSeen() {
        // Show detail dialog immediately
        Window window = getScene().getWindow();
        AlertDetailDialogController dialogController = new AlertDetailDialogController(
            alert, 
            alertService, 
            onAlertUpdated
        );
        dialogController.showDialog(window);
        
        // Mark as seen if it's new
        if (alert.isNew() && alertService != null) {
            AsyncTaskRunner.runAsync(
                // Background task: call API
                () -> {
                    try {
                        alertService.markAlertAsSeen(alert.getId());
                        return null;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to mark alert as seen: " + e.getMessage(), e);
                    }
                },
                // On success: update UI
                result -> {
                    alert.markAsSeen();
                    updateUIToSeen();
                    if (onAlertUpdated != null) {
                        onAlertUpdated.run();
                    }
                },
                // On error: silently ignore, don't show to user
                error -> {
                    // Silently ignore errors (alert may have been deleted)
                }
            );
        }
    }
    
    private void updateUIToSeen() {
        getStyleClass().remove("alert-unread");
        iconLabel.getStyleClass().remove("alert-icon-new");
        iconLabel.getStyleClass().add("alert-icon-seen");
        newBadge.setVisible(false);
        newBadge.setManaged(false);
    }
    
    public Alert getAlert() {
        return alert;
    }
}
