package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.service.AuthService;
import com.nhom.weatherdesktop.service.interfaces.IAuthService;
import com.nhom.weatherdesktop.util.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AccountController {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    
    @FXML
    private Text nameText;
    
    @FXML
    private Text emailText;
    
    @FXML
    private Button signOutButton;
    
    private final IAuthService authService = new AuthService();
    private final UserSession userSession = UserSession.getInstance();
    
    @FXML
    public void initialize() {
        loadUserInfo();
    }
    
    private void loadUserInfo() {
        Platform.runLater(() -> {
            String name = userSession.getName();
            String email = userSession.getEmail();
            
            nameText.setText(name != null ? name : "N/A");
            emailText.setText(email != null ? email : "N/A");
        });
    }
    
    @FXML
    private void handleSignOut() {
        logger.info("User signing out");
        
        try {
            // Clear session
            authService.logout();
            
            // Navigate to login screen
            navigateToLogin();
            
        } catch (Exception e) {
            logger.error("Error during sign out", e);
            showAlert(Alert.AlertType.ERROR, "Sign Out Error", "Failed to sign out properly.");
        }
    }
    
    @FXML
    private void handleBack() {
        try {
            // Navigate back to main screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/layout/main.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            
            // Get current window size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            logger.info("Navigated back to main screen");
        } catch (Exception e) {
            logger.error("Error navigating back", e);
        }
    }
    
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            
            // Get current window size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            logger.info("Navigated to login screen");
            
        } catch (IOException e) {
            logger.error("Error loading login screen: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Failed to load login screen.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
