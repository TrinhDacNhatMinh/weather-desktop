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
            Scene scene = new Scene(loader.load());
            
            Stage stage = (Stage) currentNode.getScene().getWindow();
            stage.setScene(scene);
            
            if (stageConfig != null) {
                stageConfig.accept(stage);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to " + fxmlPath, e);
        }
    }
    
    public static void navigateToMainLayout(Node currentNode) {
        navigateTo("/ui/view/main-layout.fxml", currentNode, stage -> {
            stage.setTitle(AppConfig.getAppTitle());
            stage.setMaximized(true);
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
