package com.nhom.weatherdesktop.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertService {
    
    public static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }
    
    public static void showError(String message) {
        showError("Error", message);
    }
    
    public static void showSuccess(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }
    
    public static void showSuccess(String message) {
        showSuccess("Success", message);
    }
    
    public static void showInfo(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }
    
    public static void showWarning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }
    
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait()
            .filter(response -> response == ButtonType.OK)
            .isPresent();
    }
    
    public static boolean confirm(String message) {
        return confirm("Confirmation", message);
    }
    
    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
