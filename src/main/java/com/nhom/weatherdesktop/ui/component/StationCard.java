package com.nhom.weatherdesktop.ui.component;

import com.nhom.weatherdesktop.dto.response.StationResponse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class StationCard extends VBox {
    
    private Consumer<StationResponse> onEdit;
    private Consumer<StationResponse> onDetach;
    
    public StationCard(StationResponse station) {
        super();
        this.setStyle("-fx-padding: 16; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-background-radius: 8; -fx-cursor: hand;");
        
        // Main content
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        
        // Icon
        Label icon = new Label("📍");
        icon.setStyle("-fx-font-size: 24px;");
        
        // Info VBox
        VBox info = new VBox(4);
        
        // Name
        Label nameLabel = new Label(station.name());
        nameLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: white; -fx-font-size: 15px;");
        
        // Location
        Label locationLabel = new Label("Location: " + station.location());
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");
        
        // Status
        String statusText = "ONLINE".equalsIgnoreCase(station.status()) ? "Online" : "Offline";
        String statusColor = "ONLINE".equalsIgnoreCase(station.status()) ? "#10B981" : "#EF4444";
        Label statusLabel = new Label("Status: " + statusText);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + statusColor + ";");
        
        info.getChildren().addAll(nameLabel, locationLabel, statusLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Action buttons container (initially hidden)
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setVisible(false);
        actionButtons.setManaged(false);
        
        // Update button
        Button updateBtn = new Button("✏️");
        updateBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        updateBtn.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(station);
            }
        });
        
        // Detach button
        Button detachBtn = new Button("🗑️");
        detachBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        detachBtn.setOnAction(e -> {
            if (onDetach != null) {
                onDetach.accept(station);
            }
        });
        
        actionButtons.getChildren().addAll(updateBtn, detachBtn);
        
        content.getChildren().addAll(icon, info, spacer, actionButtons);
        this.getChildren().add(content);
        
        // Hover effect to show/hide action buttons
        this.setOnMouseEntered(e -> {
            this.setStyle("-fx-padding: 16; -fx-background-color: rgba(59, 130, 246, 0.25); -fx-background-radius: 8; -fx-cursor: hand;");
            actionButtons.setVisible(true);
            actionButtons.setManaged(true);
        });
        
        this.setOnMouseExited(e -> {
            this.setStyle("-fx-padding: 16; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-background-radius: 8; -fx-cursor: hand;");
            actionButtons.setVisible(false);
            actionButtons.setManaged(false);
        });
    }
    
    public void setOnEdit(Consumer<StationResponse> handler) {
        this.onEdit = handler;
    }
    
    public void setOnDetach(Consumer<StationResponse> handler) {
        this.onDetach = handler;
    }
}
