package com.nhom.weatherdesktop.ui.component;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends HBox {
    
    private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);
    private final TranslateTransition transition;
    private final Rectangle background;
    private final Circle thumb;
    
    public ToggleSwitch() {
        // Background
        background = new Rectangle(50, 25);
        background.setArcWidth(25);
        background.setArcHeight(25);
        background.setStyle("-fx-fill: #4B5563;");
        
        // Thumb (circle)
        thumb = new Circle(10);
        thumb.setStyle("-fx-fill: white;");
        thumb.setTranslateX(5);
        thumb.setTranslateY(12.5);
        
        // Container
        Pane switchPane = new Pane(background, thumb);
        switchPane.setPrefSize(50, 25);
        
        getChildren().add(switchPane);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setSpacing(10);
        
        // Animation
        transition = new TranslateTransition(Duration.millis(200), thumb);
        
        // Click handler
        setOnMouseClicked(event -> {
            switchedOn.set(!switchedOn.get());
        });
        
        // Property listener
        switchedOn.addListener((obs, oldValue, newValue) -> {
            boolean isOn = newValue;
            transition.setToX(isOn ? 35 : 5);
            transition.play();
            
            if (isOn) {
                background.setStyle("-fx-fill: #3B82F6;"); // Blue when ON
            } else {
                background.setStyle("-fx-fill: #4B5563;"); // Gray when OFF
            }
        });
        
        setStyle("-fx-cursor: hand;");
    }
    
    public BooleanProperty switchedOnProperty() {
        return switchedOn;
    }
    
    public boolean isSwitchedOn() {
        return switchedOn.get();
    }
    
    public void setSwitchedOn(boolean value) {
        switchedOn.set(value);
    }
}
