package com.nhom.weatherdesktop.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

import com.nhom.weatherdesktop.dto.request.RegisterRequest;
import com.nhom.weatherdesktop.service.AuthService;
import com.nhom.weatherdesktop.service.interfaces.IAuthService;

public class SignUpController {
    
    private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);
    
    // Email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private TextField passwordTextField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField confirmPasswordTextField;
    
    @FXML
    private Button togglePasswordButton;
    
    @FXML
    private Button toggleConfirmPasswordButton;
    
    @FXML
    private Button signUpButton;
    
    @FXML
    private Hyperlink signInLink;
    
    @FXML
    private Text emailErrorText;
    
    @FXML
    private Text passwordMatchText;

    @FXML
    private VBox passwordRequirementsBox;
    
    @FXML
    private ImageView passwordToggleIcon;
    
    @FXML
    private ImageView confirmPasswordToggleIcon;
    
    @FXML
    private Text reqLength;
    
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    @FXML
    public void initialize() {
        // Set focus to full name field
        Platform.runLater(() -> {
            if (fullNameField != null) {
                fullNameField.requestFocus();
            }
        });
        
        // Real-time email validation
        if (emailField != null) {
            emailField.textProperty().addListener((obs, oldVal, newVal) -> {
                validateEmail();
            });
        }
        
        // Real-time password strength validation
        if (passwordField != null && passwordTextField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePasswordStrength(newVal);
                validatePasswordMatch();
            });
            passwordTextField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePasswordStrength(newVal);
                validatePasswordMatch();
            });
        }
        
        // Real-time password match validation
        if (confirmPasswordField != null && confirmPasswordTextField != null) {
            confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePasswordMatch();
            });
            confirmPasswordTextField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePasswordMatch();
            });
        }
        
        // Sync password fields
        bindPasswordFields();
    }
    
    private void bindPasswordFields() {
        // Sync password field with password text field
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(passwordTextField.getText())) {
                passwordTextField.setText(newVal);
            }
        });
        passwordTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(passwordField.getText())) {
                passwordField.setText(newVal);
            }
        });
        
        // Sync confirm password field with confirm password text field
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(confirmPasswordTextField.getText())) {
                confirmPasswordTextField.setText(newVal);
            }
        });
        confirmPasswordTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(confirmPasswordField.getText())) {
                confirmPasswordField.setText(newVal);
            }
        });
    }
    
    @FXML
    private void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;
        passwordField.setVisible(!isPasswordVisible);
        passwordTextField.setVisible(isPasswordVisible);
        updateToggleIcon(passwordToggleIcon, isPasswordVisible);
        
        // Keep focus on the visible field
        if (isPasswordVisible) {
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }
    
    @FXML
    private void handleToggleConfirmPassword() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        confirmPasswordField.setVisible(!isConfirmPasswordVisible);
        confirmPasswordTextField.setVisible(isConfirmPasswordVisible);
        updateToggleIcon(confirmPasswordToggleIcon, isConfirmPasswordVisible);
        
        // Keep focus on the visible field
        if (isConfirmPasswordVisible) {
            confirmPasswordTextField.requestFocus();
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
        } else {
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
        }
    }

    private void updateToggleIcon(ImageView icon, boolean isVisible) {
        String iconName = isVisible ? "visibility_off.png" : "visibility.png";
        try {
            String imagePath = "/icons/" + iconName;
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            icon.setImage(image);
        } catch (Exception e) {
            logger.error("Error loading icon: {}", iconName, e);
        }
    }
    
    private boolean validateEmail() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            emailErrorText.setVisible(false);
            return false;
        }
        
        boolean isValid = EMAIL_PATTERN.matcher(email).matches();
        
        if (!isValid) {
            emailErrorText.setText("Please enter a valid email address");
            emailErrorText.setVisible(true);
        } else {
            emailErrorText.setVisible(false);
        }
        
        return isValid;
    }
    
    private void validatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordRequirementsBox.setVisible(false);
            passwordRequirementsBox.setManaged(false);
            return;
        }
        
        passwordRequirementsBox.setVisible(true);
        passwordRequirementsBox.setManaged(true);

        // Check length (at least 6 characters)
        boolean hasLength = password.length() >= 6;
        updateRequirement(reqLength, hasLength, "At least 6 characters");
    }
    
    private void updateRequirement(Text requirementText, boolean met, String label) {
        if (met) {
            requirementText.setText("✓ " + label);
            requirementText.setStyle("-fx-fill: #10b981;"); // Green
        } else {
            requirementText.setText("✗ " + label);
            requirementText.setStyle("-fx-fill: #6b7280;"); // Gray
        }
    }
    
    private boolean validatePasswordMatch() {
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
        
        if (confirmPassword.isEmpty()) {
            passwordMatchText.setVisible(false);
            return false;
        }
        
        boolean matches = password.equals(confirmPassword);
        
        if (!matches) {
            passwordMatchText.setText("Passwords do not match");
            passwordMatchText.setVisible(true);
        } else {
            passwordMatchText.setVisible(false);
        }
        
        return matches;
    }
    
    private boolean isPasswordStrong(String password) {
        return password.length() >= 6;
    }
    
    private final IAuthService authService = new AuthService();

    @FXML
    private void handleSignUp() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
        
        // Validate all fields
        if (fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your full name");
            return;
        }

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a username");
            return;
        }
        
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your email address");
            return;
        }
        
        if (!validateEmail()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address");
            return;
        }
        
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please create a password");
            return;
        }
        
        if (!isPasswordStrong(password)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                     "Password does not meet security requirements");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Passwords do not match");
            return;
        }
        
        try {
            logger.info("Attempting to register user: {}", username);
            RegisterRequest request = new RegisterRequest(fullName, username, password, email);
            authService.register(request);
            
            // Show verification dialog and wait for user to dismiss it
            showSuccessDialogAndNavigateToLogin();
            
        } catch (Exception e) {
            logger.error("Registration failed", e);
            showAlert(Alert.AlertType.ERROR, "Registration Failed", e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) signInLink.getScene().getWindow();
            
            // Get current window size
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            
            logger.info("Navigated back to login screen");
            
        } catch (IOException e) {
            logger.error("Error loading login screen: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Failed to return to login screen.");
        }
    }
    
    private void showSuccessDialogAndNavigateToLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Your account has been created successfully.\n\n" +
                           "Please check your email to verify your account before logging in.");
        
        // Wait for user to click OK, then navigate
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleBackToLogin();
            }
        });
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
