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
        // Dark background for better contrast
        this.setStyle("-fx-padding: 16; -fx-background-color: rgba(30, 41, 59, 0.95); -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(59, 130, 246, 0.3); -fx-border-width: 1; -fx-border-radius: 8;");
        
        // Main content
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        
        // Icon
        Label icon = new Label("📍");
        icon.setStyle("-fx-font-size: 24px;");
        
        // Info VBox
        VBox info = new VBox(4);
        
        // Name - bright white for good contrast
        Label nameLabel = new Label(station.name());
        nameLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #FFFFFF; -fx-font-size: 16px;");
        
        // Location - light gray for good readability
        Label locationLabel = new Label("📍 " + station.location());
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #D1D5DB; -fx-font-weight: 500;");
        
        // Status with better colors
        String statusText = "ONLINE".equalsIgnoreCase(station.status()) ? "● Online" : "● Offline";
        String statusColor = "ONLINE".equalsIgnoreCase(station.status()) ? "#10B981" : "#F87171";
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + statusColor + "; -fx-font-weight: 600;");
        
        info.getChildren().addAll(nameLabel, locationLabel, statusLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Action buttons container - ALWAYS visible but with opacity
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setOpacity(0); // Start invisible
        actionButtons.setStyle("-fx-min-width: 100;"); // Reserve space
        
        // Update button - vibrant blue
        Button updateBtn = new Button("✏️");
        updateBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-padding: 8 14; -fx-background-radius: 6; -fx-cursor: hand;");
        updateBtn.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(station);
            }
        });
        
        // Detach button - vibrant red
        Button detachBtn = new Button("🗑️");
        detachBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-padding: 8 14; -fx-background-radius: 6; -fx-cursor: hand;");
        detachBtn.setOnAction(e -> {
            if (onDetach != null) {
                onDetach.accept(station);
            }
        });
        
        actionButtons.getChildren().addAll(updateBtn, detachBtn);
        
        content.getChildren().addAll(icon, info, spacer, actionButtons);
        this.getChildren().add(content);
        
        // Hover effect - lighter background with blue glow
        this.setOnMouseEntered(e -> {
            this.setStyle("-fx-padding: 16; -fx-background-color: rgba(51, 65, 85, 1); -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(59, 130, 246, 0.6); -fx-border-width: 1; -fx-border-radius: 8;");
            actionButtons.setOpacity(1); // Fade in
        });
        
        this.setOnMouseExited(e -> {
            this.setStyle("-fx-padding: 16; -fx-background-color: rgba(30, 41, 59, 0.95); -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(59, 130, 246, 0.3); -fx-border-width: 1; -fx-border-radius: 8;");
            actionButtons.setOpacity(0); // Fade out
        });
    }
    
    public void setOnEdit(Consumer<StationResponse> handler) {
        this.onEdit = handler;
    }
    
    public void setOnDetach(Consumer<StationResponse> handler) {
        this.onDetach = handler;
    }
}
