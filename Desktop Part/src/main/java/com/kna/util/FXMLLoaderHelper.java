package com.kna.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * FXMLLoaderHelper - Utility class for loading FXML files
 */
public class FXMLLoaderHelper {

    /**
     * Load FXML file and return Parent node
     * @param fxmlPath Path to FXML file (relative to resources)
     * @return Parent node
     */
    public static Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLLoaderHelper.class.getResource(fxmlPath));
        return loader.load();
    }

    /**
     * Load FXML file with controller and return loader
     * @param fxmlPath Path to FXML file (relative to resources)
     * @return FXMLLoader instance
     */
    public static FXMLLoader getFXMLLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLLoaderHelper.class.getResource(fxmlPath));
        return loader;
    }

    /**
     * Load FXML and set scene on stage
     * @param stage The stage to set the scene on
     * @param fxmlPath Path to FXML file
     * @param title Window title
     */
    public static void loadScene(Stage stage, String fxmlPath, String title) throws IOException {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
    }

    /**
     * Load FXML and create new stage
     * @param fxmlPath Path to FXML file
     * @param title Window title
     * @return New stage with loaded scene
     */
    public static Stage loadNewStage(String fxmlPath, String title) throws IOException {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        return stage;
    }

    /**
     * Load FXML with controller and return both
     * @param fxmlPath Path to FXML file
     * @return Array with [Parent, Controller]
     */
    public static Object[] loadFXMLWithController(String fxmlPath) throws IOException {
        FXMLLoader loader = getFXMLLoader(fxmlPath);
        Parent root = loader.load();
        Object controller = loader.getController();
        return new Object[]{root, controller};
    }
}
