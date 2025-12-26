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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.function.Consumer;

public class StationCard extends VBox {
    
    private Consumer<StationResponse> onEdit;
    private Consumer<StationResponse> onDetach;
    
    public StationCard(StationResponse station) {
        super();
        // Light background for modern clean look
        this.setStyle("-fx-padding: 16; -fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 8;");
        
        // Main content
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        
        
        // Info VBox
        VBox info = new VBox(4);
        
        // Name - dark for good contrast on light background
        Label nameLabel = new Label(station.name());
        nameLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #1E293B; -fx-font-size: 16px;");
        
        // Location - light gray for good readability
        HBox locationBox = new HBox(6);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon locationIcon = new FontIcon(Material2OutlinedAL.LOCATION_ON);
        locationIcon.setIconSize(13);
        locationIcon.setIconColor(javafx.scene.paint.Color.web("#9CA3AF"));
        Label locationText = new Label(station.location());
        locationText.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-font-weight: 500;");
        locationBox.getChildren().addAll(locationIcon, locationText);
        
        
        // Status with Material dot icon
        HBox statusBox = new HBox(6);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        boolean isOnline = "ONLINE".equalsIgnoreCase(station.status());
        
        // Using simple text dot for status
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-font-size: 14px; -fx-text-fill: " +
            (isOnline ? "#10B981" : "#EF4444") + ";");
        
        Label statusLabel = new Label(isOnline ? "Online" : "Offline");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + 
            (isOnline ? "#10B981" : "#EF4444") + "; -fx-font-weight: 600;");
        
        statusBox.getChildren().addAll(statusDot, statusLabel);
        
        info.getChildren().addAll(nameLabel, locationBox, statusBox);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Action buttons container - ALWAYS visible but with opacity
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setOpacity(0); // Start invisible
        actionButtons.setStyle("-fx-min-width: 100;"); // Reserve space
        
        // Update button - vibrant blue
        Button updateBtn = new Button();
        FontIcon editIcon = new FontIcon(Material2OutlinedAL.EDIT);
        editIcon.setIconSize(16);
        updateBtn.setGraphic(editIcon);
        updateBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                          "-fx-padding: 8 14; -fx-background-radius: 6; -fx-cursor: hand;");
        updateBtn.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(station);
            }
        });
        
        // Detach button - vibrant red
        Button detachBtn = new Button();
        FontIcon deleteIcon = new FontIcon(Material2OutlinedAL.DELETE);
        deleteIcon.setIconSize(16);
        detachBtn.setGraphic(deleteIcon);
        detachBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; " +
                          "-fx-padding: 8 14; -fx-background-radius: 6; -fx-cursor: hand;");
        detachBtn.setOnAction(e -> {
            if (onDetach != null) {
                onDetach.accept(station);
            }
        });
        
        actionButtons.getChildren().addAll(updateBtn, detachBtn);
        
        content.getChildren().addAll(info, spacer, actionButtons);
        this.getChildren().add(content);
        
        // Hover effect - lighter background with blue border
        this.setOnMouseEntered(e -> {
            this.setStyle("-fx-padding: 16; -fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #3B82F6; -fx-border-width: 1; -fx-border-radius: 8;");
            actionButtons.setOpacity(1); // Fade in
        });
        
        this.setOnMouseExited(e -> {
            this.setStyle("-fx-padding: 16; -fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 8;");
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
