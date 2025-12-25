package com.nhom.weatherdesktop.ui.controller.dialog;

import com.nhom.weatherdesktop.model.Alert;
import com.nhom.weatherdesktop.service.IAlertService;
import com.nhom.weatherdesktop.util.AsyncTaskRunner;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.nhom.weatherdesktop.config.TimezoneConfig.*;

public class AlertDetailDialogController {
    
    private final Alert alert;
    private final IAlertService alertService;
    private final Runnable onDelete;
    
    public AlertDetailDialogController(Alert alert, IAlertService alertService, Runnable onDelete) {
        this.alert = alert;
        this.alertService = alertService;
        this.onDelete = onDelete;
    }
    
    public void showDialog(Window owner) {
        // Create stage
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Alert Details");
        dialog.setResizable(false);
        
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #FFFFFF;");
        mainContainer.setPrefWidth(500);
        
        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label("⚠");
        iconLabel.setStyle(
            "-fx-font-size: 36px; " +
            "-fx-text-fill: " + (alert.isNew() ? "#F59E0B" : "#9CA3AF") + ";"
        );
        
        VBox headerText = new VBox(5);
        Label titleLabel = new Label("Weather Alert");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Label stationLabel = new Label(alert.getStationName());
        stationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        
        headerText.getChildren().addAll(titleLabel, stationLabel);
        header.getChildren().addAll(iconLabel, headerText);
        
        // Divider
        Region divider1 = new Region();
        divider1.setPrefHeight(1);
        divider1.setStyle("-fx-background-color: #E5E7EB;");
        
        // Content section
        VBox contentSection = new VBox(15);
        
        // Message
        VBox messageBox = createInfoBox("Message", alert.getMessage());
        
        // Created time
        String formattedTime = formatCreatedTime(alert.getCreatedAt());
        VBox timeBox = createInfoBox("Created At", formattedTime);
        
        contentSection.getChildren().addAll(messageBox, timeBox);
        
        // Divider
        Region divider2 = new Region();
        divider2.setPrefHeight(1);
        divider2.setStyle("-fx-background-color: #E5E7EB;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle(
            "-fx-background-color: #EF4444; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 30; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(e -> handleDeleteAlert(dialog));
        
        Button closeButton = new Button("Close");
        closeButton.setStyle(
            "-fx-background-color: #3B82F6; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 30; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(deleteButton, closeButton);
        
        // Add all to main container
        mainContainer.getChildren().addAll(
            header,
            divider1,
            contentSection,
            divider2,
            buttonBox
        );
        
        // Create scene and show
        Scene scene = new Scene(mainContainer);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private VBox createInfoBox(String label, String value) {
        VBox box = new VBox(6);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-text-fill: #111827;");
        valueText.setWrapText(true);
        
        box.getChildren().addAll(labelText, valueText);
        return box;
    }
    
    private String formatCreatedTime(java.time.Instant instant) {
        // WORKAROUND: Server returns timestamp in +07:00 timezone but marks it as UTC (Z)
        // We need to subtract offset to get the actual UTC time
        java.time.Instant adjustedInstant = instant.minusSeconds(getServerTimezoneOffsetSeconds());
        
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());
        
        return formatter.format(adjustedInstant);
    }
    
    private void handleDeleteAlert(Stage dialog) {
        // Show confirmation
        boolean confirmed = com.nhom.weatherdesktop.util.AlertService.confirm(
            "Delete Alert",
            "Are you sure you want to delete this alert? This action cannot be undone."
        );
        
        if (!confirmed) {
            return;
        }
        
        // Call API to delete alert
        AsyncTaskRunner.runAsync(
            // Background task
            () -> {
                try {
                    alertService.deleteAlert(alert.getId());
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to delete alert: " + e.getMessage(), e);
                }
            },
            // On success
            result -> {
                // Close dialog first
                dialog.close();
                
                // Refresh alerts list
                if (onDelete != null) {
                    onDelete.run();
                }
                
                // Show success message after a small delay to ensure dialog is fully closed
                Platform.runLater(() -> {
                    com.nhom.weatherdesktop.util.AlertService.showSuccess("Alert deleted successfully!");
                });
            },
            // On error
            error -> {
                com.nhom.weatherdesktop.util.AlertService.showError(
                    "Failed to delete alert: " + error.getMessage()
                );
            }
        );
    }
}
