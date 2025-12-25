package com.nhom.weatherdesktop.ui.component;

import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.service.IThresholdService;
import com.nhom.weatherdesktop.util.AlertSnoozeManager;
import com.nhom.weatherdesktop.util.AsyncTaskRunner;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Custom alert notification dialog with snooze combobox and disable action
 */
public class AlertNotificationDialog {
    
    private static Stage currentDialog = null;
    private static final Object LOCK = new Object();
    private static long lastClosedTime = 0;
    private static final long COOLDOWN_MS = 10000; // 10 seconds cooldown between dialogs
    
    private final Long stationId;
    private final String stationName;
    private final String message;
    private final IThresholdService thresholdService;
    
    public AlertNotificationDialog(
        Long stationId,
        String stationName,
        String message,
        IThresholdService thresholdService
    ) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.message = message;
        this.thresholdService = thresholdService;
    }
    
    public void show() {
        Platform.runLater(() -> {
            synchronized (LOCK) {
                // Check if dialog already showing
                if (currentDialog != null && currentDialog.isShowing()) {
                    return;
                }
                
                // Check cooldown
                long timeSince = System.currentTimeMillis() - lastClosedTime;
                if (timeSince < COOLDOWN_MS && lastClosedTime > 0) {
                    return;
                }
                
                Stage dialog = createDialog();
                currentDialog = dialog;
                dialog.show();
            }
        });
    }
    
    private Stage createDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        dialog.setTitle("⚠ Weather Alert");
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(true);
        
        // Main container
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(400);
        
        // Header
        Label header = new Label("⚠ Alert from " + stationName);
        header.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #DC2626;"
        );
        
        // Message
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(360);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        
        // Separator
        Separator sep = new Separator();
        
        // Snooze row
        HBox snoozeBox = new HBox(10);
        snoozeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label snoozeLabel = new Label("Snooze for:");
        snoozeLabel.setStyle("-fx-font-size: 12px;");
        
        ComboBox<String> snoozeCombo = new ComboBox<>();
        snoozeCombo.getItems().addAll("5 minutes", "10 minutes", "30 minutes", "1 hour");
        snoozeCombo.setValue("10 minutes");
        snoozeCombo.setPrefWidth(150);
        
        Button snoozeBtn = new Button("Snooze");
        snoozeBtn.setStyle(
            "-fx-background-color: #3B82F6; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 6 16;"
        );
        
        snoozeBox.getChildren().addAll(snoozeLabel, snoozeCombo, snoozeBtn);
        
        // Action buttons
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        
        Button disableBtn = new Button("Disable All Alerts");
        disableBtn.setStyle(
            "-fx-background-color: #EF4444; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 8 16;"
        );
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
            "-fx-background-color: #6B7280; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 8 16;"
        );
        
        actionsBox.getChildren().addAll(disableBtn, closeBtn);
        
        // Add all to root
        root.getChildren().addAll(header, msgLabel, sep, snoozeBox, actionsBox);
        
        // Event handlers
        snoozeBtn.setOnAction(e -> {
            System.out.println("DEBUG: Snooze button clicked!");
            String selected = snoozeCombo.getValue();
            int minutes = parseMinutes(selected);
            if (minutes > 0) {
                AlertSnoozeManager.getInstance().snoozeStation(stationId, minutes);
                dialog.close();
                
                // Show confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Alert Snoozed");
                alert.setHeaderText(null);
                alert.setContentText("Alerts from " + stationName + " snoozed for " + selected);
                alert.showAndWait();
            }
        });
        
        disableBtn.setOnAction(e -> {
            System.out.println("DEBUG: Disable button clicked!");
            handleDisableAlert(dialog);
        });
        
        closeBtn.setOnAction(e -> {
            System.out.println("DEBUG: Close button clicked!");
            dialog.close();
        });
        
        // On hidden
        dialog.setOnHidden(e -> {
            synchronized (LOCK) {
                if (currentDialog == dialog) {
                    currentDialog = null;
                    lastClosedTime = System.currentTimeMillis();
                }
            }
        });
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        
        return dialog;
    }
    
    private int parseMinutes(String selection) {
        if (selection == null) return 10;
        
        if (selection.contains("5 minutes")) return 5;
        if (selection.contains("10 minutes")) return 10;
        if (selection.contains("30 minutes")) return 30;
        if (selection.contains("1 hour")) return 60;
        
        return 10;
    }
    
    private void handleDisableAlert(Stage dialog) {
        System.out.println("DEBUG: handleDisableAlert called");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Disable Alerts");
        confirm.setHeaderText("Disable all alerts for " + stationName + "?");
        confirm.setContentText("This will turn off all threshold monitoring.");
        
        System.out.println("DEBUG: Showing confirmation dialog");
        confirm.showAndWait().ifPresent(response -> {
            System.out.println("DEBUG: Response received: " + response);
            if (response == ButtonType.OK) {
                System.out.println("DEBUG: User confirmed, calling disableAlertsAsync");
                disableAlertsAsync(dialog);
            } else {
                System.out.println("DEBUG: User cancelled");
            }
        });
        System.out.println("DEBUG: handleDisableAlert finished");
    }
    
    private void disableAlertsAsync(Stage dialog) {
        System.out.println("DEBUG: disableAlertsAsync started");
        AsyncTaskRunner.runAsync(
            () -> {
                System.out.println("DEBUG: Background task running");
                try {
                    ThresholdResponse current = thresholdService.getThresholdByStationId(stationId);
                    System.out.println("DEBUG: Got threshold: " + current);
                    UpdateThresholdRequest request = new UpdateThresholdRequest(
                        current.temperatureMin(), current.temperatureMax(),
                        current.humidityMin(), current.humidityMax(),
                        current.rainfallMax(), current.windSpeedMax(),
                        current.dustMax(),
                        false, false, false, false, false
                    );
                    thresholdService.updateThreshold(current.id(), request);
                    System.out.println("DEBUG: Threshold updated successfully");
                    return null;
                } catch (Exception e) {
                    System.err.println("DEBUG: Exception in background task: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed: " + e.getMessage(), e);
                }
            },
            result -> {
                System.out.println("DEBUG: Success callback");
                dialog.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("All alerts disabled for " + stationName);
                alert.showAndWait();
            },
            error -> {
                System.err.println("DEBUG: Error callback: " + error.getMessage());
                error.printStackTrace();
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to disable alerts: " + error.getMessage());
                alert.showAndWait();
            }
        );
    }
}
