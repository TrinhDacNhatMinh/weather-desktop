package com.nhom.weatherdesktop.util;

import com.nhom.weatherdesktop.config.AppConfig;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class NavigationService {
    
    public static void navigateTo(String fxmlPath, Node currentNode, Consumer<Stage> stageConfig) {
        try {
            var url = NavigationService.class.getResource(fxmlPath);
            if (url == null) {
                throw new RuntimeException(fxmlPath + " NOT FOUND");
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load(), 1200, 800);
            
            Stage stage = (Stage) currentNode.getScene().getWindow();
            stage.setScene(scene);
            
            // Force show first to ensure scene is rendered
            if (!stage.isShowing()) {
                stage.show();
            }
            
            // Apply custom stage configuration AFTER stage is shown
            if (stageConfig != null) {
                stageConfig.accept(stage);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to " + fxmlPath + ": " + e.getMessage(), e);
        }
    }
    
    public static void navigateToMainLayout(Node currentNode) {
        navigateTo("/ui/view/main-layout.fxml", currentNode, stage -> {
            stage.setTitle(AppConfig.getAppTitle());
            
            // Force reset maximize state - set to false first, then true
            // This fixes JavaFX bug where setScene() loses actual maximize but property stays true
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(false);
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                });
            });
        });
    }
    
    public static void navigateToLogin(Node currentNode) {
        navigateTo("/ui/view/login.fxml", currentNode, stage -> {
            stage.setTitle("Login - " + AppConfig.getAppTitle());
            stage.setWidth(400);
            stage.setHeight(500);
            stage.centerOnScreen();
        });
    }
}
