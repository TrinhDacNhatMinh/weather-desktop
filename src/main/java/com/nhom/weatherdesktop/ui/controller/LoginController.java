package com.nhom.weatherdesktop.ui.controller;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.enums.AccessChannel;
import com.nhom.weatherdesktop.exception.AuthException;
import com.nhom.weatherdesktop.service.AuthService;
import com.nhom.weatherdesktop.session.SessionContext;
import com.nhom.weatherdesktop.util.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

    private static final Logger log =
            LoggerFactory.getLogger(LoginController.class);

    private final AuthService authService = new AuthService();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label loginLogo;
    
    @FXML
    private Label usernameIcon;
    
    @FXML
    private Label passwordIcon;
    
    @FXML
    public void initialize() {
        // Set logo icon
        FontIcon logoIcon = new FontIcon(Material2OutlinedAL.CLOUD);
        logoIcon.setIconSize(48);
        logoIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        loginLogo.setGraphic(logoIcon);
        
        // Set Material icons for form fields
        FontIcon userIcon = new FontIcon(Material2OutlinedMZ.PERSON);
        userIcon.setIconSize(20);
        userIcon.setIconColor(javafx.scene.paint.Color.web("rgba(255, 255, 255, 0.7)"));
        usernameIcon.setGraphic(userIcon);
        
        FontIcon lockIcon = new FontIcon(Material2OutlinedAL.LOCK);
        lockIcon.setIconSize(20);
        lockIcon.setIconColor(javafx.scene.paint.Color.web("rgba(255, 255, 255, 0.7)"));
        passwordIcon.setGraphic(lockIcon);
        
        // Handle Enter key on password field
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        if (!validateInput()) {
            return;
        }

        try {
            LoginResponse response = doLogin();
            onLoginSuccess(response);

        } catch (AuthException ex) {
            log.warn("Login failed: {}", ex.getMessage());
            showError(ex.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error during login", ex);
            showError("System error. Please try again later.");
        }
    }

    private boolean validateInput() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            showError("Username and password are required");
            return false;
        }
        return true;
    }

    private LoginResponse doLogin() {
        LoginRequest request = new LoginRequest(
                usernameField.getText(),
                passwordField.getText(),
                AccessChannel.DESKTOP
        );

        log.debug("Sending login request for user={}", usernameField.getText());
        return authService.login(request);
    }

    private void onLoginSuccess(LoginResponse response) {
        try {
            SessionContext.set(response);
            errorLabel.setText("");
            NavigationService.navigateToMainLayout(usernameField);
            log.info("Switched to main layout view");
            
        } catch (Exception e) {
            log.error("Login succeeded but failed to open main screen", e);
            showError("Cannot open main screen");
        }
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
