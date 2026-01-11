package com.kna.controller;

import java.math.BigDecimal;

import com.kna.model.User;
import com.kna.service.AuthService;
import com.kna.service.CoinService;
import com.kna.util.SessionManager;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * CoinPurchaseController - Handles coin purchasing
 */
public class CoinPurchaseController {
    
    @FXML private Label currentBalanceLabel;
    @FXML private HBox packagesContainer;
    @FXML private VBox purchaseSummary;
    @FXML private Label selectedPackageLabel;
    @FXML private Label selectedCoinsLabel;
    @FXML private Label selectedPriceLabel;
    @FXML private Button confirmPurchaseButton;
    @FXML private Label statusLabel;
    
    private final CoinService coinService;
    private final AuthService authService;
    private User currentUser;
    private String selectedPackageName;
    private int selectedCoins;
    private String selectedPrice;

    public CoinPurchaseController() {
        this.coinService = new CoinService();
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentBalanceLabel != null && currentUser != null) {
            currentBalanceLabel.setText(String.valueOf(currentUser.getCoins()));
        }
        
        // Hide purchase summary initially
        if (purchaseSummary != null) {
            purchaseSummary.setVisible(false);
            purchaseSummary.setManaged(false);
        }
    }
    
    /**
     * Select starter package.
     */
    @FXML
    private void selectStarterPackage() {
        selectPackageDetails("Starter", 50, "$4.99");
    }
    
    /**
     * Select value package.
     */
    @FXML
    private void selectValuePackage() {
        selectPackageDetails("Value Pack", 150, "$9.99");
    }
    
    /**
     * Select premium package.
     */
    @FXML
    private void selectPremiumPackage() {
        selectPackageDetails("Premium", 400, "$19.99");
    }
    
    /**
     * Select ultimate package.
     */
    @FXML
    private void selectUltimatePackage() {
        selectPackageDetails("Ultimate", 1000, "$39.99");
    }
    
    /**
     * Select a package from clicking on the card.
     */
    @FXML
    private void selectPackage(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof javafx.scene.layout.VBox card) {
            String userData = (String) card.getUserData();
            switch (userData) {
                case "starter" -> selectStarterPackage();
                case "popular" -> selectValuePackage();
                case "premium" -> selectPremiumPackage();
                case "ultimate" -> selectUltimatePackage();
            }
        }
    }
    
    private void selectPackageDetails(String name, int coins, String price) {
        selectedPackageName = name;
        selectedCoins = coins;
        selectedPrice = price;
        
        if (selectedPackageLabel != null) selectedPackageLabel.setText(name);
        if (selectedCoinsLabel != null) selectedCoinsLabel.setText(coins + " Coins");
        if (selectedPriceLabel != null) selectedPriceLabel.setText(price);
        
        if (purchaseSummary != null) {
            purchaseSummary.setVisible(true);
            purchaseSummary.setManaged(true);
        }
    }

    /**
     * Confirm purchase action.
     */
    @FXML
    private void confirmPurchase() {
        if (selectedPackageName == null) {
            if (statusLabel != null) {
                statusLabel.setText("Please select a package first.");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }
            return;
        }
        
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Purchase");
        confirmAlert.setHeaderText("Purchase " + selectedCoins + " Coins?");
        confirmAlert.setContentText("You will be charged " + selectedPrice + ".");
        
        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }
        
        // Process the purchase
        simulatePayment();
    }

    private void simulatePayment() {
        // Create a blank white screen
        Stage paymentStage = new Stage();
        paymentStage.initModality(Modality.APPLICATION_MODAL);
        paymentStage.initStyle(StageStyle.UNDECORATED);
        
        StackPane whiteScreen = new StackPane();
        whiteScreen.setStyle("-fx-background-color: white;");
        
        Label processingLabel = new Label("Processing payment...");
        processingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575;");
        
        ProgressIndicator progressIndicator = new ProgressIndicator();
        
        VBox content = new VBox(20, progressIndicator, processingLabel);
        content.setAlignment(Pos.CENTER);
        
        whiteScreen.getChildren().add(content);
        
        Scene scene = new Scene(whiteScreen, 400, 300);
        paymentStage.setScene(scene);
        paymentStage.show();
        
        // Auto close after 2 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            paymentStage.close();
            completePurchase();
        });
        delay.play();
    }

    private void completePurchase() {
        try {
            // Process purchase
            coinService.purchaseCoins(selectedCoins, new BigDecimal(selectedPrice.replace("$", "")));
            
            // Update UI
            currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentBalanceLabel != null) {
                currentBalanceLabel.setText(String.valueOf(currentUser.getCoins()));
            }
            
            // Show success
            if (statusLabel != null) {
                statusLabel.setText("✓ Purchase successful! You now have " + currentUser.getCoins() + " coins.");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            }
            
            // Reset selection
            if (purchaseSummary != null) {
                purchaseSummary.setVisible(false);
                purchaseSummary.setManaged(false);
            }
            selectedPackageName = null;
            
            // Refresh user data
            authService.refreshCurrentUser();
            
        } catch (Exception e) {
            if (statusLabel != null) {
                statusLabel.setText("✗ Purchase failed: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #f44336;");
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        if (purchaseSummary != null) {
            purchaseSummary.setVisible(false);
            purchaseSummary.setManaged(false);
        }
        selectedPackageName = null;
    }
}
