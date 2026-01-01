package com.kna;

import com.kna.util.DatabaseManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the KnA application
 */
public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            
            // Initialize database and create tables if needed
            DatabaseManager.getInstance().initializeDatabase();
            
            // Load login screen
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(root);
            
            // Load CSS
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            stage.setTitle("KnA - Knowledge and Answers");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    /**
     * Get primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Switch to a different scene
     */
    public static void switchScene(String fxmlPath, String title) {
        try {
            // Save current window state BEFORE changing scene
            boolean wasMaximized = primaryStage.isMaximized();
            boolean wasFullScreen = primaryStage.isFullScreen();
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            double currentX = primaryStage.getX();
            double currentY = primaryStage.getY();
            
            Parent root = FXMLLoader.load(Main.class.getResource(fxmlPath));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/application.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            
            // Restore window state AFTER scene is set
            if (!wasFullScreen && !wasMaximized) {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
                primaryStage.setX(currentX);
                primaryStage.setY(currentY);
            }
            
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }
            
            if (wasFullScreen) {
                primaryStage.setFullScreen(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to switch scene: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Close database connection
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
