package com.nhom.weatherdesktop.ui.controller;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.enums.AccessChannel;
import com.nhom.weatherdesktop.exception.AuthException;
import com.nhom.weatherdesktop.service.AuthService;
import com.nhom.weatherdesktop.session.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
            goToMainLayout();

        } catch (Exception e) {
            log.error("Login succeeded but failed to open main screen", e);
            showError("Cannot open main screen");
        }
    }

    private void goToMainLayout() {
        try {
            var url = getClass().getResource("/ui/view/main-layout.fxml");
            if (url == null) {
                throw new RuntimeException("main-layout.fxml NOT FOUND");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Weather Desktop");

            log.info("Switched to main layout view");

        } catch (Exception e) {
            log.error("Failed to load main layout.fxml", e);
            showError("Failed to load main layout");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
