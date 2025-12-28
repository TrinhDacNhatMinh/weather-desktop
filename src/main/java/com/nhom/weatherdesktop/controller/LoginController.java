package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.enums.AccessChannel;
import com.nhom.weatherdesktop.service.AuthService;
import com.nhom.weatherdesktop.session.SessionContext;
import com.nhom.weatherdesktop.util.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private CheckBox rememberMeCheckbox;
    
    @FXML
    private Hyperlink forgotPasswordLink;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button signUpButton;
    
    private final AuthService authService;
    
    public LoginController() {
        this.authService = new AuthService();
    }
    
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (usernameField != null) {
                usernameField.requestFocus();
            }
        });
        
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                     "Please enter both username and password.");
            return;
        }
        
        setUIDisabled(true);
        loginButton.setText("Signing in...");
        
        Task<LoginResponse> loginTask = new Task<>() {
            @Override
            protected LoginResponse call() throws Exception {
                LoginRequest request = new LoginRequest(username, password, AccessChannel.DESKTOP);
                return authService.login(request);
            }
        };
        
        loginTask.setOnSucceeded(event -> {
            LoginResponse response = loginTask.getValue();
            if (response != null) {
                logger.info("Login successful! User: {}, Email: {}", response.name(), response.email());
                
                // Store access token in session for WebSocket authentication
                SessionContext.setAccessToken(response.accessToken());
                
                navigateToMainScreen();
            }
        });
        
        loginTask.setOnFailed(event -> {
            Throwable exception = loginTask.getException();
            String errorMessage = exception != null ? exception.getMessage() : "Unknown error occurred";
            
            showAlert(Alert.AlertType.ERROR, "Login Failed", errorMessage);
            setUIDisabled(false);
            loginButton.setText("Sign In");
            passwordField.clear();
        });
        
        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    @FXML
    private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", 
                 "Password recovery functionality will be implemented soon!");
    }
    
    @FXML
    private void handleSignUp() {
        showAlert(Alert.AlertType.INFORMATION, "Sign Up", 
                 "Sign up functionality will be implemented soon!");
    }
    
    private void navigateToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/layout/main.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Get current window size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            // Create scene with current window size
            Scene scene = new Scene(root, width, height);
            
            AppConfig config = AppConfig.getInstance();
            stage.setScene(scene);
            stage.setTitle(config.getAppTitle());
            stage.setMaximized(true);  // Maximize window (keeps title bar with minimize/maximize/close)
            
            logger.info("Successfully navigated to main screen");
            
        } catch (IOException e) {
            logger.error("Error loading main screen: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Failed to load main application screen.");
        }
    }
    
    private void setUIDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        rememberMeCheckbox.setDisable(disabled);
        forgotPasswordLink.setDisable(disabled);
        loginButton.setDisable(disabled);
        signUpButton.setDisable(disabled);
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

