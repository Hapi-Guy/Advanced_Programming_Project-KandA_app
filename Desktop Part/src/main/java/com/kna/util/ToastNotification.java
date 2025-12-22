package com.kna.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * ToastNotification - Utility for showing toast/snackbar style notifications
 */
public class ToastNotification {

    public enum NotificationType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Show a toast notification
     * @param message Message to display
     * @param type Type of notification
     */
    public static void show(String message, NotificationType type) {
        show(message, type, 3);
    }

    /**
     * Show a toast notification with custom duration
     * @param message Message to display
     * @param type Type of notification
     * @param durationSeconds Duration in seconds
     */
    public static void show(String message, NotificationType type, int durationSeconds) {
        Stage toastStage = new Stage();
        toastStage.initModality(Modality.NONE);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        Label label = new Label(message);
        label.setStyle(getStyleForType(type));
        label.setMaxWidth(400);
        label.setWrapText(true);

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.BOTTOM_CENTER);

        Scene scene = new Scene(root, 400, 100);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toastStage.setScene(scene);

        // Position at bottom center
        toastStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() / 2 - 200);
        toastStage.setY(javafx.stage.Screen.getPrimary().getVisualBounds().getHeight() - 150);

        toastStage.show();

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Auto close after duration
        PauseTransition delay = new PauseTransition(Duration.seconds(durationSeconds));
        delay.setOnFinished(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> toastStage.close());
            fadeOut.play();
        });
        delay.play();
    }

    /**
     * Get CSS style for notification type
     */
    private static String getStyleForType(NotificationType type) {
        String baseStyle = "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 15px 25px; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);";

        String color;
        switch (type) {
            case SUCCESS:
                color = "#4CAF50";
                break;
            case ERROR:
                color = "#f44336";
                break;
            case WARNING:
                color = "#FF9800";
                break;
            case INFO:
            default:
                color = "#2196F3";
                break;
        }

        return String.format(baseStyle, color);
    }

    /**
     * Show success notification
     */
    public static void showSuccess(String message) {
        show(message, NotificationType.SUCCESS);
    }

    /**
     * Show error notification
     */
    public static void showError(String message) {
        show(message, NotificationType.ERROR);
    }

    /**
     * Show warning notification
     */
    public static void showWarning(String message) {
        show(message, NotificationType.WARNING);
    }

    /**
     * Show info notification
     */
    public static void showInfo(String message) {
        show(message, NotificationType.INFO);
    }
}
